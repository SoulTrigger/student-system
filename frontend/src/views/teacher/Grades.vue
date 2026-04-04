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
            <el-button type="primary" @click="showBatchDialog" :disabled="!selectedRows.length">批量录入 ({{ selectedRows.length }})</el-button>
          </div>
        </div>
      </template>

      <el-table :data="filteredData" v-loading="loading" stripe @selection-change="handleSelection">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="gradeId" label="成绩ID" width="90" />
        <el-table-column label="学生" min-width="100">
          <template #default="{ row }">{{ row.studentName || row.studentId }}</template>
        </el-table-column>
        <el-table-column label="课程" min-width="120">
          <template #default="{ row }">{{ row.courseName || row.courseId }}</template>
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

    <!-- Single edit dialog -->
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

    <!-- Batch input dialog -->
    <el-dialog v-model="batchVisible" title="批量录入成绩" width="600px" @closed="resetBatch">
      <el-table :data="batchList" max-height="400">
        <el-table-column label="学生" min-width="100">
          <template #default="{ row }">{{ row.studentName || row.studentId }}</template>
        </el-table-column>
        <el-table-column label="课程" min-width="100">
          <template #default="{ row }">{{ row.courseName || row.courseId }}</template>
        </el-table-column>
        <el-table-column label="成绩" width="160">
          <template #default="{ row }">
            <el-input-number v-model="row.score" :min="0" :max="100" :precision="0" size="small" style="width: 120px" />
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="batchVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchSubmitting" @click="handleBatchSave">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTeacherGrades, updateGradeScore, batchSaveGrades } from '../../api/grade'

const loading = ref(false)
const tableData = ref([])
const searchSemester = ref('')
const selectedRows = ref([])

const editVisible = ref(false)
const submitting = ref(false)
const editForm = reactive({ gradeId: null, studentName: '', courseName: '', semester: '', score: 0 })

const batchVisible = ref(false)
const batchSubmitting = ref(false)
const batchList = ref([])

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
    const res = await getTeacherGrades({ semester: searchSemester.value || undefined })
    tableData.value = res.data || []
  } catch (e) {
    ElMessage.error('获取成绩列表失败')
  } finally {
    loading.value = false
  }
}

function handleSelection(rows) {
  selectedRows.value = rows
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
}

async function handleUpdate() {
  submitting.value = true
  try {
    await updateGradeScore(editForm.gradeId, editForm.score)
    ElMessage.success('修改成功')
    editVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '修改失败')
  } finally {
    submitting.value = false
  }
}

function showBatchDialog() {
  batchList.value = selectedRows.value.map(r => ({ ...r, score: r.score ?? 0 }))
  batchVisible.value = true
}

function resetBatch() {
  batchList.value = []
}

async function handleBatchSave() {
  batchSubmitting.value = true
  try {
    const grades = batchList.value.map(r => ({
      id: r.gradeId,
      selectionId: r.selectionId,
      score: r.score,
      semester: r.semester
    }))
    await batchSaveGrades(grades)
    ElMessage.success('批量录入成功')
    batchVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '批量录入失败')
  } finally {
    batchSubmitting.value = false
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
.grades-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; align-items: center; }
</style>
