<template>
  <div class="edit-teacher">
    <h2>编辑教师</h2>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 500px">
      <el-form-item label="工号">
        <el-input :model-value="String(teacherId)" disabled />
      </el-form-item>
      <el-form-item label="教师姓名" prop="name">
        <el-input v-model="form.name" placeholder="请输入教师姓名" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="form.password" type="password" placeholder="请输入新密码" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
        <el-button type="warning" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const teacherId = route.params.id
const formRef = ref(null)
const form = reactive({ name: '', password: '' })

const rules = {
  name: [
    { required: true, message: '请输入教师姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度2-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6个字符', trigger: 'blur' }
  ]
}

const fetchTeacher = async () => {
  try {
    const res = await axios.get('/api/admin/teachers', { params: { page: 1, size: 1000 } })
    const found = res.data.records.find(t => String(t.id) === String(teacherId))
    if (found) {
      form.name = found.name
      form.password = ''
    }
  } catch (e) {
    ElMessage.error('获取教师信息失败')
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    await axios.put(`/api/admin/teachers/${teacherId}`, form)
    ElMessage.success('更新成功')
    router.push('/admin/teachers')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '更新失败')
  }
}

const handleReset = () => {
  form.name = ''
  form.password = ''
  formRef.value?.clearValidate()
}

onMounted(fetchTeacher)
</script>
