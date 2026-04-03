<template>
  <div class="student-search">
    <h2>学生查询</h2>
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="学号">
        <el-input v-model="searchForm.id" placeholder="精确查询" clearable />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="searchForm.name" placeholder="输入姓名" clearable />
        <el-checkbox v-model="searchForm.fuzzy" style="margin-left: 10px">模糊查询</el-checkbox>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleResetSearch">重置</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="results" border stripe v-loading="loading">
      <el-table-column prop="id" label="学号" width="120" />
      <el-table-column prop="name" label="姓名" />
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
import axios from 'axios'
import { ElMessage } from 'element-plus'

const searchForm = reactive({ id: '', name: '', fuzzy: false })
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
    if (searchForm.id) params.id = searchForm.id
    if (searchForm.name) {
      params.name = searchForm.name
      params.fuzzy = searchForm.fuzzy
    }
    const res = await axios.get('/api/admin/students/search', { params })
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
  searchForm.id = ''
  searchForm.name = ''
  searchForm.fuzzy = false
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
