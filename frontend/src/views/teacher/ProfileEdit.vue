<template>
  <div class="profile-edit">
    <h2>信息修改</h2>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 500px">
      <el-form-item label="姓名">
        <el-input :model-value="auth.name" disabled />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="form.newPassword" type="password" placeholder="请输入新密码" show-password />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
        <el-button type="warning" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useAuthStore } from '../../stores/auth'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const auth = useAuthStore()
const formRef = ref(null)

const form = reactive({
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.newPassword) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    await axios.put('/api/user/profile', {
      newPassword: form.newPassword,
      confirmPassword: form.confirmPassword
    })
    ElMessage.success('密码修改成功')
    handleReset()
  } catch (e) {
    if (e.response) {
      ElMessage.error(e.response.data?.error || '修改失败')
    }
  }
}

const handleReset = () => {
  form.newPassword = ''
  form.confirmPassword = ''
  formRef.value?.resetFields()
}
</script>

<style scoped>
</style>
