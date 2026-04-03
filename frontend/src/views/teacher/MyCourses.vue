<template>
  <div class="my-courses">
    <h2>已开课程</h2>
    <el-table :data="results" border stripe v-loading="loading">
      <el-table-column prop="courseId" label="课程号" width="120" />
      <el-table-column prop="courseName" label="课程名" />
      <el-table-column prop="credit" label="学分" width="100" />
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
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const results = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10

const doSearch = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/teacher/openings', { params: { page: currentPage.value, size: pageSize } })
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

onMounted(doSearch)
</script>

<style scoped>
.pagination-wrapper { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; }
.total-count { color: #666; }
</style>
