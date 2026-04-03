<template>
  <div class="course-selection">
    <h2>选课查询</h2>
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="课程名">
        <el-input v-model="searchForm.courseName" placeholder="课程名" clearable />
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="searchForm.fuzzy">模糊查询</el-checkbox>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="results" border stripe v-loading="loading">
      <el-table-column prop="openingId" label="课号" width="100" />
      <el-table-column prop="courseId" label="课程号" width="100" />
      <el-table-column prop="courseName" label="课程名" />
      <el-table-column prop="teacherId" label="教师号" width="100" />
      <el-table-column prop="teacherName" label="教师名" width="120" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="handleSelect(row)">选择</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrapper" v-if="total > 0">
      <span class="total-count">共 {{ total }} 条</span>
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="pageSize" :current-page="currentPage" @current-change="handlePageChange" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const searchForm = reactive({ courseName: '', fuzzy: false })
const results = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10

const handleSearch = () => {
  currentPage.value = 1
  doSearch()
}

const doSearch = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize }
    if (searchForm.courseName) params.courseName = searchForm.courseName
    if (searchForm.fuzzy) params.fuzzy = true
    const res = await axios.get('/api/student/selections/available', { params })
    results.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handleSelect = async (row) => {
  try {
    await axios.post('/api/student/selections', { openingId: row.openingId })
    ElMessage.success('选课成功')
    doSearch()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '选课失败')
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  doSearch()
}

const handleReset = () => {
  Object.assign(searchForm, { courseName: '', fuzzy: false })
  results.value = []
  total.value = 0
}
</script>

<style scoped>
.search-form { margin-bottom: 20px; }
.pagination-wrapper { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; }
.total-count { color: #666; }
</style>
