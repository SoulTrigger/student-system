<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="login-title">学生信息管理系统</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="userId">
          <el-input v-model="form.userId" placeholder="请输入用户ID" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="role">
          <el-radio-group v-model="form.role">
            <el-radio value="学生">学生</el-radio>
            <el-radio value="老师">老师</el-radio>
            <el-radio value="管理员">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="background-color: #409eff">登录</el-button>
          <el-button type="warning" @click="handleReset" style="background-color: #e6a23c; color: white">重置</el-button>
        </el-form-item>
      </el-form>
      <el-alert v-if="errorMsg" :title="errorMsg" type="error" show-icon :closable="false" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref(null)
const loading = ref(false)
const errorMsg = ref('')

const roleHomeMap = { '学生': '/student', '老师': '/teacher', '管理员': '/admin' }

const form = reactive({
  userId: '',
  password: '',
  role: '学生'
})

const rules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const handleLogin = async () => {
  errorMsg.value = ''
  try {
    await formRef.value.validate()
  } catch { return }
  loading.value = true
  try {
    await auth.login(form.userId, form.password, form.role)
    router.push(roleHomeMap[auth.role] || '/student')
  } catch (err) {
    errorMsg.value = err.response?.data?.message || err.response?.data?.error || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  formRef.value.resetFields()
  errorMsg.value = ''
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  width: 400px;
}
.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}
</style>
