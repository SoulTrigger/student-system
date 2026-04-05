<template>
  <div class="openings-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>开课管理</span>
          <div class="header-actions">
            <el-select v-model="searchCourseId" placeholder="筛选课程" clearable style="width: 160px; margin-right: 8px" @change="handleSearch">
              <el-option v-for="c in courseList" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
            <el-input v-model="searchSemester" placeholder="搜索学期" clearable style="width: 160px; margin-right: 8px" @keyup.enter="handleSearch" @clear="handleSearch" />
            <el-button type="primary" @click="showAddDialog">新增开课</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="课程" min-width="120">
          <template #default="{ row }">{{ courseMap[row.courseId]?.name || row.courseId }}</template>
        </el-table-column>
        <el-table-column label="教师" min-width="120">
          <template #default="{ row }">{{ teacherMap[row.teacherId]?.name || row.teacherId }}</template>
        </el-table-column>
        <el-table-column prop="semester" label="学期" width="150" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @current-change="fetchData"
        @size-change="fetchData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增开课" width="450px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option v-for="c in courseList" :key="c.id" :label="`${c.name} (${c.credit}学分)`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="教师" prop="teacherId">
          <el-select v-model="form.teacherId" placeholder="请选择教师" filterable style="width: 100%">
            <el-option v-for="t in teacherList" :key="t.id" :label="`${t.name} (${t.id})`" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-input v-model="form.semester" placeholder="如: 2025-2026-1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOpenings, addOpening, deleteOpening } from '../../api/opening'
import { getCourses } from '../../api/course'
import { getTeachers } from '../../api/teacher'

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchCourseId = ref(null)
const searchSemester = ref('')

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const courseList = ref([])
const teacherList = ref([])
const courseMap = computed(() => Object.fromEntries(courseList.value.map(c => [c.id, c])))
const teacherMap = computed(() => Object.fromEntries(teacherList.value.map(t => [t.id, t])))

const form = reactive({ courseId: null, teacherId: null, semester: '' })
const rules = {
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  teacherId: [{ required: true, message: '请选择教师', trigger: 'change' }],
  semester: [{ required: true, message: '请输入学期', trigger: 'blur' }]
}

async function fetchLookups() {
  try {
    const [cRes, tRes] = await Promise.all([
      getCourses({ page: 1, size: 1000 }),
      getTeachers({ page: 1, size: 1000 })
    ])
    courseList.value = cRes.data.records
    teacherList.value = tRes.data.records
  } catch (e) { /* ignore */ }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getOpenings({
      page: currentPage.value,
      size: pageSize.value,
      courseId: searchCourseId.value || undefined,
      semester: searchSemester.value || undefined
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    ElMessage.error('获取开课列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function showAddDialog() {
  dialogVisible.value = true
}

function resetForm() {
  form.courseId = null
  form.teacherId = null
  form.semester = ''
  if (formRef.value) formRef.value.resetFields()
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    await addOpening({
      courseId: form.courseId,
      teacherId: form.teacherId,
      semester: form.semester
    })
    ElMessage.success('开课成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  const cName = courseMap.value[row.courseId]?.name || row.courseId
  const tName = teacherMap.value[row.teacherId]?.name || row.teacherId
  try {
    await ElMessageBox.confirm(
      `删除开课"${cName}-${tName}-${row.semester}"将同时删除相关选课记录和成绩，确认删除？`,
      '级联删除确认',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
    await deleteOpening(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(() => { fetchLookups(); fetchData() })
</script>

<style scoped>
.openings-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; align-items: center; }
</style>
