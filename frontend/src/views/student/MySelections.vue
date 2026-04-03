<template>
  <div class="my-selections">
    <h2>已选课程</h2>
    <el-table :data="results" border stripe v-loading="loading">
      <el-table-column prop="openingId" label="课号" width="100" />
      <el-table-column prop="courseId" label="课程号" width="100" />
      <el-table-column prop="courseName" label="课程名" />
      <el-table-column prop="teacherId" label="教师号" width="100" />
      <el-table-column prop="teacherName" label="教师名" width="120" />
      <el-table-column prop="credit" label="学分" width="80" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="handleWithdraw(row)">退课</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrapper" v-if="total > 0">
      <span class="total-count">共 {{ total }} 条，总学分：{{ totalCredits }}</span>
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="pageSize" :current-page="currentPage" @current-change="handlePageChange" />
    </div>
    <div v-else-if="!loading" class="total-info">总学分：{{ totalCredits }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const results = ref([])
const loading = ref(false)
const total = ref(0)
const totalCredits = ref(0)
const currentPage = ref(1)
const pageSize = 10

const doSearch = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/student/selections/mine', { params: { page: currentPage.value, size: pageSize } })
    results.value = res.data.selections.records || res.data.selections
    total.value = res.data.selections.total || res.data.selections.length
    totalCredits.value = res.data.totalCredits || 0
  } catch (e) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handleWithdraw = async (row) => {
  try {
    await ElMessageBox.confirm(`确认退选 ${row.courseName}？`, '提示', { type: 'warning' })
    await axios.delete(`/api/student/selections/${row.selectionId}`)
    ElMessage.success('退课成功')
    doSearch()
  } catch (e) {
    if (e !== 'cancel' && e?.response) {
      ElMessage.error(e.response?.data?.message || '退课失败')
    }
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
.total-info { margin-top: 10px; color: #666; }
</style>
