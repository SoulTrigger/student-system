<template>
  <div class="add-course">
    <h2>添加课程</h2>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 500px">
      <el-form-item label="课程名" prop="name">
        <el-input v-model="form.name" placeholder="请输入课程名" />
      </el-form-item>
      <el-form-item label="学分" prop="credit">
        <el-input-number v-model="form.credit" :min="1" :max="10" />
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
const form = reactive({ name: '', credit: 1 })

const rules = {
  name: [
    { required: true, message: '请输入课程名', trigger: 'blur' },
    { min: 2, max: 50, message: '课程名长度2-50个字符', trigger: 'blur' }
  ],
  credit: [
    { required: true, message: '请输入学分', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    await axios.post('/api/admin/courses', form)
    ElMessage.success('添加成功')
    router.push('/admin/courses/search')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '添加失败')
  }
}

const handleReset = () => {
  form.name = ''
  form.credit = 1
  formRef.value?.clearValidate()
}
</script>
