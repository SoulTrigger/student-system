<template>
  <div class="dashboard-container">
    <el-card>
      <template #header><span>个人信息</span></template>
      <el-descriptions :column="1" border style="max-width: 400px">
        <el-descriptions-item label="学号">{{ store.userId }}</el-descriptions-item>
        <el-descriptions-item label="角色">学生</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header><span>修改密码</span></template>
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="100px" style="max-width: 400px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="pwdLoading" @click="handleChangePwd">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { changePassword } from '../../api/auth'

const store = useUserStore()
const router = useRouter()
const pwdFormRef = ref(null)
const pwdLoading = ref(false)

const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) callback(new Error('两次密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

async function handleChangePwd() {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return
  pwdLoading.value = true
  try {
    await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    store.logout()
    router.push('/login')
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '密码修改失败')
  } finally {
    pwdLoading.value = false
  }
}
</script>

<style scoped>
.dashboard-container { padding: 20px; }
</style>
