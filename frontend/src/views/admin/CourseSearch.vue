<template>
  <div class="course-search">
    <h2>课程查询</h2>
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="课程号">
        <el-input v-model="searchForm.id" placeholder="精确查询" clearable />
      </el-form-item>
      <el-form-item label="课程名">
        <el-input v-model="searchForm.name" placeholder="输入课程名" clearable />
      </el-form-item>
      <el-form-item label="学分下限">
        <el-input-number v-model="searchForm.creditMin" :min="1" :max="10" placeholder="最低学分" />
      </el-form-item>
      <el-form-item label="学分上限">
        <el-input-number v-model="searchForm.creditMax" :min="1" :max="10" placeholder="最高学分" />
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="searchForm.fuzzy">模糊查询</el-checkbox>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button type="warning" @click="handleResetSearch">重置</el-button>
      </el-form-item>
    </el-form>
    <div style="margin-bottom: 10px">
      <el-button type="success" @click="router.push('/admin/courses/add')">添加课程</el-button>
    </div>
    <el-table :data="results" border stripe v-loading="loading">
      <el-table-column prop="id" label="课程号" width="120" />
      <el-table-column prop="name" label="课程名" />
      <el-table-column prop="credit" label="学分" width="100" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="router.push(`/admin/courses/edit/${row.id}`)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const searchForm = reactive({ id: '', name: '', creditMin: undefined, creditMax: undefined, fuzzy: false })
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
    if (searchForm.creditMin != null) params.creditMin = searchForm.creditMin
    if (searchForm.creditMax != null) params.creditMax = searchForm.creditMax
    const res = await axios.get('/api/admin/courses/search', { params })
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
  searchForm.creditMin = undefined
  searchForm.creditMax = undefined
  searchForm.fuzzy = false
  results.value = []
  total.value = 0
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除课程「${row.name}」吗？删除后相关的开课记录和选课记录也将被删除。`,
      '删除确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await axios.delete(`/api/admin/courses/${row.id}`)
    ElMessage.success('删除成功')
    doSearch()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || '删除失败')
    }
  }
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
