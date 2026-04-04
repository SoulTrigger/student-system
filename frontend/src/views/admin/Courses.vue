<template>
  <div class="courses-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>课程管理</span>
          <div class="header-actions">
            <el-input
              v-model="searchName"
              placeholder="搜索课程名称"
              clearable
              style="width: 160px; margin-right: 8px"
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            />
            <el-input-number
              v-model="minCredit"
              :min="1" :max="10"
              controls-position="right"
              placeholder="最低学分"
              style="width: 110px; margin-right: 8px"
            />
            <el-input-number
              v-model="maxCredit"
              :min="1" :max="10"
              controls-position="right"
              placeholder="最高学分"
              style="width: 110px; margin-right: 8px"
            />
            <el-button @click="handleSearch" style="margin-right: 12px">查询</el-button>
            <el-button type="primary" @click="showAddDialog">添加课程</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="课程号" width="100" />
        <el-table-column prop="name" label="课程名称" />
        <el-table-column prop="credit" label="学分" width="100" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑课程' : '添加课程'" width="450px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="form.credit" :min="1" :max="10" controls-position="right" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCourses, addCourse, updateCourse, deleteCourse } from '../../api/course'

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchName = ref('')
const minCredit = ref(undefined)
const maxCredit = ref(undefined)

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({ name: '', credit: 3 })

const creditValidator = (rule, value, callback) => {
  if (value == null) callback(new Error('请输入学分'))
  else if (value < 1 || value > 10) callback(new Error('学分范围为1-10'))
  else callback()
}

const rules = {
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  credit: [{ required: true, validator: creditValidator, trigger: 'change' }]
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      name: searchName.value || undefined,
      minCredit: minCredit.value ?? undefined,
      maxCredit: maxCredit.value ?? undefined
    }
    const res = await getCourses(params)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    ElMessage.error('获取课程列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function showAddDialog() {
  isEdit.value = false
  editingId.value = null
  form.credit = 3
  dialogVisible.value = true
}

function showEditDialog(row) {
  isEdit.value = true
  editingId.value = row.id
  form.name = row.name
  form.credit = row.credit
  dialogVisible.value = true
}

function resetForm() {
  form.name = ''
  form.credit = 3
  if (formRef.value) formRef.value.resetFields()
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateCourse(editingId.value, { name: form.name, credit: form.credit })
      ElMessage.success('修改成功')
    } else {
      await addCourse({ name: form.name, credit: form.credit })
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `删除课程"${row.name}"将同时删除相关开课记录、选课记录和成绩，确认删除？`,
      '级联删除确认',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
    await deleteCourse(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
.courses-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; align-items: center; }
</style>
