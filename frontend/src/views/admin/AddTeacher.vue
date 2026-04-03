<template>
  <div class="add-teacher">
    <h2>添加教师</h2>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 500px">
      <el-form-item label="教师姓名" prop="name">
        <el-input v-model="form.name" placeholder="请输入教师姓名" />
      </el-form-item>
      <el-form-item label="初始密码" prop="password">
        <el-input v-model="form.password" type="password" placeholder="请输入初始密码" />
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
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const form = reactive({ name: '', password: '123456' })

const rules = {
  name: [
    { required: true, message: '请输入教师姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度2-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6个字符', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    await axios.post('/api/admin/teachers', form)
    ElMessage.success('添加成功')
    router.push('/admin/teachers')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '添加失败')
  }
}

const handleReset = () => {
  form.name = ''
  form.password = '123456'
  formRef.value?.clearValidate()
}
</script>
