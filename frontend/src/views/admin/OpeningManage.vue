<template>
  <div class="opening-manage">
    <h2>开课管理</h2>
    <el-form :inline="true" :model="searchForm" class="search-form">
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
      <el-form-item>
        <el-checkbox v-model="searchForm.fuzzy">模糊查询</el-checkbox>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button type="warning" @click="handleResetSearch">重置</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="results" border stripe v-loading="loading">
      <el-table-column prop="id" label="课号" width="120" />
      <el-table-column prop="courseId" label="课程号" width="120" />
      <el-table-column prop="courseName" label="课程名" />
      <el-table-column prop="teacherId" label="教师号" width="120" />
      <el-table-column prop="teacherName" label="教师名" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
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
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchForm = reactive({ teacherId: '', teacherName: '', courseId: '', courseName: '', fuzzy: false })
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
    if (searchForm.teacherId) params.teacherId = searchForm.teacherId
    if (searchForm.teacherName) {
      params.teacherName = searchForm.teacherName
    }
    if (searchForm.courseId) params.courseId = searchForm.courseId
    if (searchForm.courseName) {
      params.courseName = searchForm.courseName
    }
    if (searchForm.fuzzy) params.fuzzy = true
    const res = await axios.get('/api/admin/openings/search', { params })
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
  searchForm.teacherId = ''
  searchForm.teacherName = ''
  searchForm.courseId = ''
  searchForm.courseName = ''
  searchForm.fuzzy = false
  results.value = []
  total.value = 0
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除开课记录（课程：${row.courseName}，教师：${row.teacherName}）吗？删除后相关的选课记录和成绩记录也将被删除。`,
      '删除确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await axios.delete(`/api/admin/openings/${row.id}`)
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
