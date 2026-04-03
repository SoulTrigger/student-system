<template>
  <div class="teacher-list">
    <div class="page-header">
      <h2>教师管理</h2>
      <el-button type="primary" @click="$router.push('/admin/teachers/add')">添加教师</el-button>
    </div>
    <el-table :data="teachers" border stripe v-loading="loading">
      <el-table-column prop="id" label="工号" width="120" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="$router.push(`/admin/teachers/edit/${row.id}`)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrapper">
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
    <el-dialog v-model="deleteDialogVisible" title="确认删除" width="400px">
      <span>确认删除教师 {{ deleteTarget?.name }} 及其所有开课和成绩记录？</span>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDelete">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const teachers = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const deleteDialogVisible = ref(false)
const deleteTarget = ref(null)

const fetchTeachers = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/admin/teachers', { params: { page: currentPage.value, size: pageSize } })
    teachers.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    ElMessage.error('获取教师列表失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchTeachers()
}

const handleDelete = (row) => {
  deleteTarget.value = row
  deleteDialogVisible.value = true
}

const confirmDelete = async () => {
  try {
    await axios.delete(`/api/admin/teachers/${deleteTarget.value.id}`)
    ElMessage.success('删除成功')
    deleteDialogVisible.value = false
    fetchTeachers()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(fetchTeachers)
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
