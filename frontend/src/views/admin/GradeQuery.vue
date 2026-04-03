<template>
  <div class="grade-query">
    <h2>成绩查询</h2>
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="学号">
        <el-input v-model="searchForm.studentId" placeholder="精确查询" clearable />
      </el-form-item>
      <el-form-item label="学生名">
        <el-input v-model="searchForm.studentName" placeholder="输入学生名" clearable />
      </el-form-item>
      <el-form-item label="工号">
        <el-input v-model="searchForm.teacherId" placeholder="精确查询" clearable />
      </el-form-item>
      <el-form-item label="教师名">
        <el-input v-model="searchForm.teacherName" placeholder="输入教师名" clearable />
      </el-form-item>
      <el-form-item label="课程号">
        <el-input v-model="searchForm.courseId" placeholder="精确查询" clearable />
      </el-form-item>
      <el-form-item label="课程名">
        <el-input v-model="searchForm.courseName" placeholder="输入课程名" clearable />
      </el-form-item>
      <el-form-item label="成绩下限">
        <el-input-number v-model="searchForm.scoreMin" :min="0" :max="100" placeholder="最低分" />
      </el-form-item>
      <el-form-item label="成绩上限">
        <el-input-number v-model="searchForm.scoreMax" :min="0" :max="100" placeholder="最高分" />
      </el-form-item>
      <el-form-item label="学期">
        <el-select v-model="searchForm.semester" placeholder="选择学期" clearable>
          <el-option v-for="s in semesterOptions" :key="s" :label="s" :value="s" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="searchForm.fuzzy">模糊查询</el-checkbox>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button type="warning" @click="handleResetSearch">重置</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="results" border stripe v-loading="loading">
      <el-table-column prop="courseId" label="课程号" width="100" />
      <el-table-column prop="courseName" label="课程名" />
      <el-table-column prop="teacherId" label="工号" width="100" />
      <el-table-column prop="teacherName" label="教师名" />
      <el-table-column prop="studentId" label="学号" width="100" />
      <el-table-column prop="studentName" label="学生名" />
      <el-table-column prop="score" label="成绩" width="80" />
      <el-table-column prop="semester" label="学期" width="140" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" @click="router.push(`/admin/grades/edit/${row.id}`)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrapper" v-if="total > 0">
      <span class="total-count">共 {{ total }} 条</span>
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const searchForm = reactive({
  studentId: '', studentName: '',
  teacherId: '', teacherName: '',
  courseId: '', courseName: '',
  scoreMin: undefined, scoreMax: undefined,
  semester: '', fuzzy: false
})
const results = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10

const getCurrentSemester = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  const term = month >= 2 && month <= 7 ? '春' : '秋'
  return `${year}${term}`
}

const semesterOptions = (() => {
  const now = new Date()
  const year = now.getFullYear()
  return [
    `${year}春`, `${year}秋`,
    `${year - 1}春`, `${year - 1}秋`,
    `${year + 1}春`, `${year + 1}秋`
  ]
})()

searchForm.semester = getCurrentSemester()

const handleSearch = () => {
  currentPage.value = 1
  doSearch()
}

const doSearch = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize }
    if (searchForm.studentId) params.studentId = searchForm.studentId
    if (searchForm.studentName) params.studentName = searchForm.studentName
    if (searchForm.teacherId) params.teacherId = searchForm.teacherId
    if (searchForm.teacherName) params.teacherName = searchForm.teacherName
    if (searchForm.courseId) params.courseId = searchForm.courseId
    if (searchForm.courseName) params.courseName = searchForm.courseName
    if (searchForm.scoreMin != null) params.scoreMin = searchForm.scoreMin
    if (searchForm.scoreMax != null) params.scoreMax = searchForm.scoreMax
    if (searchForm.semester) params.semester = searchForm.semester
    if (searchForm.fuzzy) params.fuzzy = true
    const res = await axios.get('/api/admin/grades', { params })
    results.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  doSearch()
}

const handleResetSearch = () => {
  Object.assign(searchForm, {
    studentId: '', studentName: '',
    teacherId: '', teacherName: '',
    courseId: '', courseName: '',
    scoreMin: undefined, scoreMax: undefined,
    semester: getCurrentSemester(), fuzzy: false
  })
  results.value = []
  total.value = 0
}
</script>

<style scoped>
.search-form {
  margin-bottom: 20px;
}
.pagination-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
}
.total-count {
  color: #666;
}
</style>
