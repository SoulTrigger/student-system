<template>
  <div class="openings-container">
    <!-- Course catalog for opening new courses -->
    <el-card style="margin-bottom: 20px">
      <template #header>
        <div class="card-header">
          <span>开设课程</span>
        </div>
      </template>
      <el-form :inline="true" :model="openForm" label-width="80px">
        <el-form-item label="课程">
          <el-select v-model="openForm.courseId" placeholder="选择课程" filterable style="width: 240px">
            <el-option v-for="c in courses" :key="c.id" :label="`${c.name} (${c.credit}学分)`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学期">
          <el-input v-model="openForm.semester" placeholder="如: 2025-2026-1" style="width: 160px" />
        </el-form-item>
        <el-form-item>
          <el-button type="success" :loading="openingLoading" :disabled="!openForm.courseId || !openForm.semester" @click="handleOpen">开设</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- My openings -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的已开课程</span>
          <el-button @click="fetchMyOpenings">刷新</el-button>
        </div>
      </template>
      <el-table :data="myOpenings" v-loading="loading" stripe>
        <el-table-column prop="id" label="开课ID" width="90" />
        <el-table-column label="课程" min-width="120">
          <template #default="{ row }">{{ courseMap.get(row.courseId) || row.courseId }}</template>
        </el-table-column>
        <el-table-column prop="semester" label="学期" width="140" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyOpenings, addOpening, deleteOpening } from '../../api/opening'
import { getCourses } from '../../api/course'

const loading = ref(false)
const openingLoading = ref(false)
const myOpenings = ref([])
const courses = ref([])

const courseMap = computed(() => {
  const m = new Map()
  courses.value.forEach(c => m.set(c.id, c.name))
  return m
})

const openForm = reactive({ courseId: null, semester: '' })

async function fetchCourses() {
  try {
    const res = await getCourses({ size: 999 })
    courses.value = res.data?.records || res.data || []
  } catch (e) { /* ignore */ }
}

async function fetchMyOpenings() {
  loading.value = true
  try {
    const res = await getMyOpenings({ size: 100 })
    myOpenings.value = res.data?.records || res.data || []
  } catch (e) {
    ElMessage.error('获取已开课程失败')
  } finally {
    loading.value = false
  }
}

async function handleOpen() {
  openingLoading.value = true
  try {
    await addOpening({ courseId: openForm.courseId, semester: openForm.semester })
    ElMessage.success('开设成功')
    openForm.courseId = null
    openForm.semester = ''
    fetchMyOpenings()
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '开设失败，可能已重复开设')
  } finally {
    openingLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('删除开课将级联删除相关选课和成绩记录，确认删除？', '警告', { type: 'warning' })
    await deleteOpening(row.id)
    ElMessage.success('删除成功')
    fetchMyOpenings()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(() => { fetchCourses(); fetchMyOpenings() })
</script>

<style scoped>
.openings-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
