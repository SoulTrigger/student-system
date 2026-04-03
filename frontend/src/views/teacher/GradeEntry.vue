<template>
  <div class="grade-entry">
    <h2>成绩录入</h2>
    <el-form :inline="true" class="search-form">
      <el-form-item label="选择开课">
        <el-select v-model="selectedOpeningId" placeholder="选择已开课程" @change="loadStudents" clearable>
          <el-option v-for="o in openings" :key="o.id" :label="`${o.courseName} (${o.courseId})`" :value="o.id" />
        </el-select>
      </el-form-item>
    </el-form>
    <div v-if="students.length > 0">
      <el-table :data="students" border stripe>
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column prop="studentName" label="学生名" />
        <el-table-column label="成绩" width="180">
          <template #default="{ row }">
            <el-input-number v-model="row.score" :min="0" :max="100" :precision="0" size="small" />
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 20px">
        <el-button type="primary" @click="handleSubmit" :loading="submitting">批量提交</el-button>
      </div>
    </div>
    <div class="pagination-wrapper" v-if="studentTotal > pageSize">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="studentTotal"
        :page-size="pageSize"
        :current-page="studentPage"
        @current-change="handleStudentPageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const openings = ref([])
const selectedOpeningId = ref(null)
const students = ref([])
const studentTotal = ref(0)
const studentPage = ref(1)
const pageSize = 20
const submitting = ref(false)

const loadOpenings = async () => {
  try {
    const res = await axios.get('/api/teacher/openings', { params: { page: 1, size: 100 } })
    openings.value = res.data.records
  } catch (e) {
    ElMessage.error('加载课程失败')
  }
}

const loadStudents = async () => {
  if (!selectedOpeningId.value) {
    students.value = []
    return
  }
  try {
    const res = await axios.get('/api/teacher/grades/students', {
      params: { openingId: selectedOpeningId.value, page: studentPage.value, size: pageSize }
    })
    students.value = res.data.records.map(s => ({ ...s, score: s.score ?? null }))
    studentTotal.value = res.data.total
  } catch (e) {
    ElMessage.error('加载学生失败')
  }
}

const handleStudentPageChange = (page) => {
  studentPage.value = page
  loadStudents()
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    const promises = students.value
      .filter(s => s.score != null)
      .map(s => axios.post('/api/teacher/grades', { selectionId: s.selectionId, score: s.score }))
    await Promise.all(promises)
    ElMessage.success('提交成功')
    loadStudents()
  } catch (e) {
    ElMessage.error('部分提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(loadOpenings)
</script>

<style scoped>
.search-form { margin-bottom: 20px; }
.pagination-wrapper { display: flex; justify-content: center; margin-top: 20px; }
</style>
