<template>
  <div class="grades-container">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>我的成绩</span>
          <div style="display: flex; gap: 10px; align-items: center">
            <el-tag type="success" size="large">
              平均分: <strong>{{ avgInfo.averageScore?.toFixed(1) || '0.0' }}</strong>
            </el-tag>
            <el-tag size="large">
              已出分: <strong>{{ avgInfo.courseCount || 0 }}</strong> 门
            </el-tag>
          </div>
        </div>
      </template>
      <el-table :data="gradeList" v-loading="loading" stripe>
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column prop="semester" label="学期" width="140" />
        <el-table-column label="成绩" width="100">
          <template #default="{ row }">
            <span v-if="row.score != null">{{ row.score }}</span>
            <el-tag v-else type="info" size="small">未出分</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getStudentGrades, getStudentAverage } from '../../api/grade'

const loading = ref(false)
const gradeList = ref([])
const avgInfo = reactive({ averageScore: 0, courseCount: 0 })

async function loadGrades() {
  loading.value = true
  try {
    const { data } = await getStudentGrades()
    gradeList.value = (data.data || data) || []
  } catch (e) {
    ElMessage.error('加载成绩失败')
  } finally {
    loading.value = false
  }
}

async function loadAverage() {
  try {
    const { data } = await getStudentAverage()
    Object.assign(avgInfo, data.data || data)
  } catch {}
}

onMounted(() => {
  loadGrades()
  loadAverage()
})
</script>

<style scoped>
.grades-container { padding: 20px; }
</style>
