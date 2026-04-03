<template>
  <div class="grade-edit">
    <h2>编辑成绩</h2>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 500px">
      <el-form-item label="课程名">
        <el-input v-model="form.courseName" disabled />
      </el-form-item>
      <el-form-item label="教师名">
        <el-input v-model="form.teacherName" disabled />
      </el-form-item>
      <el-form-item label="学生名">
        <el-input v-model="form.studentName" disabled />
      </el-form-item>
      <el-form-item label="分数" prop="score">
        <el-input-number v-model="form.score" :min="0" :max="100" :step="1" :precision="0" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
        <el-button @click="router.push('/admin/grades')">返回</el-button>
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
const form = reactive({
  courseName: '',
  teacherName: '',
  studentName: '',
  score: 0
})

const rules = {
  score: [
    { required: true, message: '请输入分数', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value == null) return callback(new Error('请输入分数'))
        if (!Number.isInteger(value)) return callback(new Error('分数必须为整数'))
        if (value < 0 || value > 100) return callback(new Error('分数必须在0-100之间'))
        callback()
      },
      trigger: 'blur'
    }
  ]
}

onMounted(async () => {
  try {
    const gradeId = route.params.id
    const res = await axios.get('/api/admin/grades', { params: { page: 1, size: 1000 } })
    const grade = res.data.records?.find(g => String(g.id) === String(gradeId))
    if (grade) {
      form.courseName = grade.courseName
      form.teacherName = grade.teacherName
      form.studentName = grade.studentName
      form.score = grade.score
    } else {
      ElMessage.error('未找到成绩记录')
    }
  } catch (e) {
    ElMessage.error('加载成绩信息失败')
  }
})

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    await axios.put(`/api/admin/grades/${route.params.id}`, { score: form.score })
    ElMessage.success('更新成功')
    router.push('/admin/grades')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '更新失败')
  }
}
</script>
