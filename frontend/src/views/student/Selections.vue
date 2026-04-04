<template>
  <div class="selections-container">
    <!-- 总学分卡片 -->
    <el-card style="margin-bottom: 20px">
      <div style="display: flex; align-items: center; gap: 20px">
        <span>已选 <strong>{{ creditInfo.courseCount }}</strong> 门课程</span>
        <span>总学分 <strong>{{ creditInfo.totalCredits }}</strong> / {{ creditInfo.maxCredits }}</span>
        <span>课程数 <strong>{{ creditInfo.courseCount }}</strong> / {{ creditInfo.maxCourses }}</span>
        <el-button size="small" @click="fetchCredits">刷新</el-button>
      </div>
    </el-card>

    <!-- 可选课程 -->
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>可选课程</span>
          <div style="display: flex; gap: 10px; align-items: center">
            <el-input v-model="semesterFilter" placeholder="学期筛选" clearable style="width: 160px" @clear="loadAvailable" />
            <el-button type="primary" @click="loadAvailable">查询</el-button>
          </div>
        </div>
      </template>
      <el-table :data="availableList" v-loading="availLoading" stripe>
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column prop="semester" label="学期" width="140" />
        <el-table-column prop="teacherId" label="教师ID" width="100" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleSelect(row)">选课</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top: 15px; justify-content: flex-end"
        v-model:current-page="availPage"
        v-model:page-size="availSize"
        :total="availTotal"
        layout="total, prev, pager, next"
        @current-change="loadAvailable"
      />
    </el-card>

    <!-- 已选课程 -->
    <el-card style="margin-top: 20px">
      <template #header><span>已选课程</span></template>
      <el-table :data="myList" v-loading="myLoading" stripe>
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column prop="semester" label="学期" width="140" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="handleDrop(row)">退课</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top: 15px; justify-content: flex-end"
        v-model:current-page="myPage"
        v-model:page-size="mySize"
        :total="myTotal"
        layout="total, prev, pager, next"
        @current-change="loadMine"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAvailableCourses, selectCourse, getMySelections, dropCourse, getMyCredits } from '../../api/selection'

const semesterFilter = ref('')

// Credits
const creditInfo = reactive({ totalCredits: 0, courseCount: 0, maxCredits: 24, maxCourses: 8 })
async function fetchCredits() {
  try {
    const { data } = await getMyCredits()
    Object.assign(creditInfo, data.data || data)
  } catch {}
}

// Available
const availLoading = ref(false)
const availableList = ref([])
const availPage = ref(1)
const availSize = ref(10)
const availTotal = ref(0)

async function loadAvailable() {
  availLoading.value = true
  try {
    const params = { page: availPage.value, size: availSize.value }
    if (semesterFilter.value) params.semester = semesterFilter.value
    const { data } = await getAvailableCourses(params)
    const res = data.data || data
    availableList.value = res.records || []
    availTotal.value = res.total || 0
  } catch (e) {
    ElMessage.error('加载可选课程失败')
  } finally {
    availLoading.value = false
  }
}

async function handleSelect(row) {
  try {
    await selectCourse({ openingId: row.openingId })
    ElMessage.success('选课成功')
    loadAvailable()
    loadMine()
    fetchCredits()
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '选课失败')
  }
}

// Mine
const myLoading = ref(false)
const myList = ref([])
const myPage = ref(1)
const mySize = ref(10)
const myTotal = ref(0)

async function loadMine() {
  myLoading.value = true
  try {
    const { data } = await getMySelections({ page: myPage.value, size: mySize.value })
    const res = data.data || data
    myList.value = res.records || []
    myTotal.value = res.total || 0
  } catch (e) {
    ElMessage.error('加载已选课程失败')
  } finally {
    myLoading.value = false
  }
}

async function handleDrop(row) {
  try {
    await ElMessageBox.confirm(
      `确定要退选「${row.courseName}」吗？相关成绩记录也将被删除。`,
      '退课确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await dropCourse(row.selectionId)
    ElMessage.success('退课成功')
    loadMine()
    loadAvailable()
    fetchCredits()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.response?.data?.msg || '退课失败')
  }
}

onMounted(() => {
  loadAvailable()
  loadMine()
  fetchCredits()
})
</script>

<style scoped>
.selections-container { padding: 20px; }
</style>
