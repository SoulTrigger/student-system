<template>
  <div class="teachers-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>教师管理</span>
          <div class="header-actions">
            <el-input
              v-model="searchName"
              placeholder="搜索教师姓名"
              clearable
              style="width: 200px; margin-right: 12px"
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            />
            <el-button type="primary" @click="showAddDialog">添加教师</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="工号" width="100" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑教师' : '添加教师'" width="450px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入教师姓名" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="默认123456" show-password />
        </el-form-item>
        <el-form-item v-if="isEdit" label="新密码">
          <el-input v-model="form.password" type="password" placeholder="留空则不修改" show-password />
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
import { getTeachers, addTeacher, updateTeacher, deleteTeacher } from '../../api/teacher'

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchName = ref('')

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({ name: '', password: '' })

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getTeachers({
      page: currentPage.value,
      size: pageSize.value,
      name: searchName.value || undefined
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    ElMessage.error('获取教师列表失败')
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
  dialogVisible.value = true
}

function showEditDialog(row) {
  isEdit.value = true
  editingId.value = row.id
  form.name = row.name
  form.password = ''
  dialogVisible.value = true
}

function resetForm() {
  form.name = ''
  form.password = ''
  if (formRef.value) formRef.value.resetFields()
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      const data = { name: form.name }
      if (form.password) data.password = form.password
      await updateTeacher(editingId.value, data)
      ElMessage.success('修改成功')
    } else {
      await addTeacher({ name: form.name, password: form.password || '123456' })
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
      `删除教师"${row.name}"将同时删除其开课记录、选课记录和成绩，确认删除？`,
      '级联删除确认',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
    await deleteTeacher(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
.teachers-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; align-items: center; }
</style>
