<template>
  <div class="logs-container">
    <el-card>
      <template #header>
        <span>操作日志</span>
      </template>

      <!-- Filters -->
      <el-form :inline="true" class="filter-form">
        <el-form-item label="操作人">
          <el-input v-model="filters.operator" placeholder="操作人" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-input v-model="filters.operation" placeholder="操作类型" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="目标类型">
          <el-select v-model="filters.targetType" placeholder="全部" clearable style="width: 140px">
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
            <el-option label="课程" value="course" />
            <el-option label="开课" value="opening" />
            <el-option label="选课" value="selection" />
            <el-option label="成绩" value="grade" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="filters.startTime" type="datetime" placeholder="开始时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 200px" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="filters.endTime" type="datetime" placeholder="结束时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="role" label="角色" width="100" />
        <el-table-column prop="operation" label="操作类型" width="120" />
        <el-table-column prop="targetType" label="目标类型" width="100" />
        <el-table-column prop="targetId" label="目标ID" width="80" />
        <el-table-column prop="detail" label="详情" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="操作时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getLogs } from '../../api/log'

const loading = ref(false)
const tableData = ref([])

const filters = reactive({
  operator: '',
  operation: '',
  targetType: '',
  startTime: '',
  endTime: ''
})

async function fetchData() {
  loading.value = true
  try {
    const params = {}
    if (filters.operator) params.operator = filters.operator
    if (filters.operation) params.operation = filters.operation
    if (filters.targetType) params.targetType = filters.targetType
    if (filters.startTime) params.startTime = filters.startTime
    if (filters.endTime) params.endTime = filters.endTime
    const res = await getLogs(params)
    tableData.value = res.data || []
  } catch (e) {
    ElMessage.error('获取日志失败')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.operator = ''
  filters.operation = ''
  filters.targetType = ''
  filters.startTime = ''
  filters.endTime = ''
  fetchData()
}

onMounted(() => fetchData())
</script>

<style scoped>
.logs-container { padding: 20px; }
.filter-form { margin-bottom: 16px; }
</style>
