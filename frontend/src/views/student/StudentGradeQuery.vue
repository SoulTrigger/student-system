<template>
  <div class="grade-query">
    <h2>成绩查询</h2>
    <el-form :inline="true" class="search-form">
      <el-form-item label="学期">
        <el-select v-model="semester" placeholder="选择学期" clearable @change="handleSearch">
          <el-option v-for="s in semesters" :key="s" :label="s" :value="s" />
        </el-select>
      </el-form-item>
    </el-form>
    <el-table :data="results" border stripe v-loading="loading">
      <el-table-column prop="openingId" label="课号" width="100" />
      <el-table-column prop="courseId" label="课程号" width="100" />
      <el-table-column prop="courseName" label="课程名" />
      <el-table-column prop="teacherId" label="教师号" width="100" />
      <el-table-column prop="teacherName" label="教师名" width="120" />
      <el-table-column prop="credit" label="学分" width="80" />
      <el-table-column prop="score" label="成绩" width="80" />
    </el-table>
    <div class="bottom-info">
      <span v-if="results.length" class="avg-score">平均成绩：{{ averageScore }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const semester = ref('')
const results = ref([])
const loading = ref(false)
const averageScore = ref('0.0')

const currentYear = new Date().getFullYear()
const currentMonth = new Date().getMonth() + 1
const currentSemester = currentMonth >= 2 && currentMonth <= 7 ? `${currentYear-1}-${currentYear}-2` : `${currentYear}-${currentYear+1}-1`

const semesters = []
for (let y = 2024; y <= currentYear + 1; y++) {
  semesters.push(`${y}-${y+1}-1`)
  semesters.push(`${y}-${y+1}-2`)
}

const handleSearch = async () => {
  loading.value = true
  try {
    const params = { page: 1, size: 100 }
    if (semester.value) params.semester = semester.value
    const res = await axios.get('/api/student/grades', { params })
    results.value = res.data.records || []
    averageScore.value = res.data.averageScore != null ? res.data.averageScore : '0.0'
  } catch (e) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  semester.value = currentSemester
  handleSearch()
})
</script>

<style scoped>
.search-form { margin-bottom: 20px; }
.bottom-info { margin-top: 20px; }
.avg-score { font-size: 16px; font-weight: bold; color: #409eff; }
</style>
