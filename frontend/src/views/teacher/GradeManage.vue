<template>
  <div class="grade-manage">
    <h2>成绩管理</h2>
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="学号">
        <el-input v-model="searchForm.studentId" placeholder="学号" clearable />
      </el-form-item>
      <el-form-item label="学生名">
        <el-input v-model="searchForm.studentName" placeholder="学生名" clearable />
      </el-form-item>
      <el-form-item label="课程号">
        <el-input v-model="searchForm.courseId" placeholder="课程号" clearable />
      </el-form-item>
      <el-form-item label="课程名">
        <el-input v-model="searchForm.courseName" placeholder="课程名" clearable />
      </el-form-item>
      <el-form-item label="成绩下限">
        <el-input-number v-model="searchForm.scoreMin" :min="0" :max="100" />
      </el-form-item>
      <el-form-item label="成绩上限">
        <el-input-number v-model="searchForm.scoreMax" :min="0" :max="100" />
      </el-form-item>
      <el-form-item label="学期">
        <el-select v-model="searchForm.semester" placeholder="选择学期" clearable>
          <el-option v-for="s in semesterOptions" :key="s" :label="s" :value="s" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="searchForm.fuzzy">模糊查询</el-checkbox>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button type="warning" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="results" border stripe v-loading="loading">
      <el-table-column prop="courseId" label="课程号" width="100" />
      <el-table-column prop="studentId" label="学号" width="100" />
      <el-table-column prop="courseName" label="课程名" />
      <el-table-column prop="studentName" label="学生名" />
      <el-table-column prop="score" label="成绩" width="80" />
      <el-table-column prop="semester" label="学期" width="140" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button v-if="row.score == null" size="small" type="primary" @click="handleEntry(row)">录入</el-button>
          <el-button v-else size="small" @click="handleEdit(row)">编辑</el-button>
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

    <el-dialog v-model="editDialogVisible" title="编辑成绩" width="400px">
      <el-form label-width="80px">
        <el-form-item label="课程">
          <el-input :model-value="editingRow.courseName" disabled />
        </el-form-item>
        <el-form-item label="学生">
          <el-input :model-value="editingRow.studentName" disabled />
        </el-form-item>
        <el-form-item label="成绩">
          <el-input-number v-model="editScore" :min="0" :max="100" :precision="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const searchForm = reactive({
  studentId: '', studentName: '', courseId: '', courseName: '',
  scoreMin: undefined, scoreMax: undefined, semester: '', fuzzy: false
})
const results = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10

const editDialogVisible = ref(false)
const editingRow = ref({})
const editScore = ref(0)

const getCurrentSemester = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  const term = month >= 2 && month <= 7 ? '春' : '秋'
  return `${year}${term}`
}

const semesterOptions = (() => {
  const year = new Date().getFullYear()
  return [`${year}春`, `${year}秋`, `${year-1}春`, `${year-1}秋`, `${year+1}春`, `${year+1}秋`]
})()

searchForm.semester = getCurrentSemester()

const handleSearch = () => {
  currentPage.value = 1
  doSearch()
}

const doSearch = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize }
    if (searchForm.studentId) params.studentId = searchForm.studentId
    if (searchForm.studentName) params.studentName = searchForm.studentName
    if (searchForm.courseId) params.courseId = searchForm.courseId
    if (searchForm.courseName) params.courseName = searchForm.courseName
    if (searchForm.scoreMin != null) params.scoreMin = searchForm.scoreMin
    if (searchForm.scoreMax != null) params.scoreMax = searchForm.scoreMax
    if (searchForm.semester) params.semester = searchForm.semester
    if (searchForm.fuzzy) params.fuzzy = true
    const res = await axios.get('/api/teacher/grades', { params })
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

const handleEntry = (row) => {
  editingRow.value = row
  editScore.value = 0
  editDialogVisible.value = true
}

const handleEdit = (row) => {
  editingRow.value = row
  editScore.value = row.score || 0
  editDialogVisible.value = true
}

const submitEdit = async () => {
  try {
    if (editingRow.value.score == null) {
      await axios.post('/api/teacher/grades', {
        selectionId: editingRow.value.selectionId,
        score: editScore.value
      })
      ElMessage.success('录入成功')
    } else {
      await axios.put(`/api/teacher/grades/${editingRow.value.id}`, { score: editScore.value })
      ElMessage.success('编辑成功')
    }
    editDialogVisible.value = false
    doSearch()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

const handleReset = () => {
  Object.assign(searchForm, {
    studentId: '', studentName: '', courseId: '', courseName: '',
    scoreMin: undefined, scoreMax: undefined, semester: getCurrentSemester(), fuzzy: false
  })
  results.value = []
  total.value = 0
}
</script>

<style scoped>
.search-form { margin-bottom: 20px; }
.pagination-wrapper { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; }
.total-count { color: #666; }
</style>
