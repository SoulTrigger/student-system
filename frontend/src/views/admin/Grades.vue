<template>
  <div class="grades-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>成绩管理</span>
          <div class="header-actions">
            <el-select v-model="searchSemester" placeholder="筛选学期" clearable style="width: 160px; margin-right: 8px" @change="fetchData">
              <el-option v-for="s in semesterOptions" :key="s" :label="s" :value="s" />
            </el-select>
            <el-button @click="fetchData">查询</el-button>
          </div>
        </div>
      </template>

      <el-table :data="filteredData" v-loading="loading" stripe>
        <el-table-column prop="gradeId" label="成绩ID" width="90" />
        <el-table-column label="学生" min-width="100">
          <template #default="{ row }">{{ row.studentName || row.studentId }}</template>
        </el-table-column>
        <el-table-column label="课程" min-width="120">
          <template #default="{ row }">{{ row.courseName || row.courseId }}</template>
        </el-table-column>
        <el-table-column label="教师" min-width="100">
          <template #default="{ row }">{{ row.teacherName || row.teacherId }}</template>
        </el-table-column>
        <el-table-column prop="semester" label="学期" width="130" />
        <el-table-column prop="score" label="成绩" width="100">
          <template #default="{ row }">{{ row.score !== null && row.score !== undefined ? row.score : '未录入' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">修改</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="editVisible" title="修改成绩" width="450px" @closed="resetEdit">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="学生">
          <el-input :model-value="editForm.studentName" disabled />
        </el-form-item>
        <el-form-item label="课程">
          <el-input :model-value="editForm.courseName" disabled />
        </el-form-item>
        <el-form-item label="学期">
          <el-input :model-value="editForm.semester" disabled />
        </el-form-item>
        <el-form-item label="成绩">
          <el-input-number v-model="editForm.score" :min="0" :max="100" :precision="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleUpdate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllGrades, updateGradeScore } from '../../api/grade'

const loading = ref(false)
const tableData = ref([])
const searchSemester = ref('')

const editVisible = ref(false)
const submitting = ref(false)
const editForm = reactive({ gradeId: null, studentName: '', courseName: '', semester: '', score: 0 })

const semesterOptions = computed(() => {
  const set = new Set(tableData.value.map(r => r.semester).filter(Boolean))
  return [...set].sort().reverse()
})

const filteredData = computed(() => {
  if (!searchSemester.value) return tableData.value
  return tableData.value.filter(r => r.semester === searchSemester.value)
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getAllGrades({ semester: searchSemester.value || undefined })
    tableData.value = res.data || []
  } catch (e) {
    ElMessage.error('获取成绩列表失败')
  } finally {
    loading.value = false
  }
}

function showEditDialog(row) {
  editForm.gradeId = row.gradeId
  editForm.studentName = row.studentName || row.studentId || ''
  editForm.courseName = row.courseName || row.courseId || ''
  editForm.semester = row.semester || ''
  editForm.score = row.score ?? 0
  editVisible.value = true
}

function resetEdit() {
  editForm.gradeId = null
  editForm.studentName = ''
  editForm.courseName = ''
  editForm.semester = ''
  editForm.score = 0
}

async function handleUpdate() {
  if (editForm.score < 0 || editForm.score > 100) {
    ElMessage.warning('成绩必须在0-100之间')
    return
  }
  submitting.value = true
  try {
    await updateGradeScore(editForm.gradeId, editForm.score)
    ElMessage.success('修改成功')
    editVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.message || '修改失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
.grades-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; align-items: center; }
</style>
