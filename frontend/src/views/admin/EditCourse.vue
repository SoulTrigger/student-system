<template>
  <div class="edit-course">
    <h2>编辑课程</h2>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 500px">
      <el-form-item label="课程号">
        <el-input v-model="form.id" disabled />
      </el-form-item>
      <el-form-item label="课程名" prop="name">
        <el-input v-model="form.name" placeholder="请输入课程名" />
      </el-form-item>
      <el-form-item label="学分" prop="credit">
        <el-input-number v-model="form.credit" :min="1" :max="10" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
        <el-button @click="router.push('/admin/courses/search')">返回</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const form = reactive({ id: '', name: '', credit: 1 })

const rules = {
  name: [
    { required: true, message: '请输入课程名', trigger: 'blur' },
    { min: 2, max: 50, message: '课程名长度2-50个字符', trigger: 'blur' }
  ],
  credit: [
    { required: true, message: '请输入学分', trigger: 'blur' }
  ]
}

onMounted(async () => {
  try {
    const res = await axios.get(`/api/admin/courses/search`, { params: { id: route.params.id } })
    const course = res.data.records?.[0]
    if (course) {
      form.id = course.id
      form.name = course.name
      form.credit = course.credit
    }
  } catch (e) {
    ElMessage.error('加载课程信息失败')
  }
})

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    await axios.put(`/api/admin/courses/${form.id}`, { name: form.name, credit: form.credit })
    ElMessage.success('更新成功')
    router.push('/admin/courses/search')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '更新失败')
  }
}
</script>
