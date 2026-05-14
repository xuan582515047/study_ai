<template>
  <div class="learning-path">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>个性化学习路径规划</span>
          <el-button type="primary" @click="dialogVisible = true">规划新路径</el-button>
        </div>
      </template>
      <el-timeline v-if="steps.length > 0">
        <el-timeline-item v-for="step in steps" :key="step.id" :type="stepStatus(step.status)" :icon="stepIcon(step.status)">
          <el-card>
            <h4>{{ step.title }}</h4>
            <p>{{ step.description }}</p>
            <el-tag size="small">{{ stepTypeMap[step.stepType] }}</el-tag>
            <el-tag size="small" type="info">{{ step.durationMinutes }}分钟</el-tag>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无学习路径，请先规划" />
    </el-card>

    <el-dialog v-model="dialogVisible" title="规划学习路径" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="学习目标">
          <el-input v-model="form.goal" placeholder="如：掌握动态规划" />
        </el-form-item>
        <el-form-item label="学科">
          <el-input v-model="form.subject" placeholder="如：计算机科学" />
        </el-form-item>
        <el-form-item label="计划天数">
          <el-input-number v-model="form.durationDays" :min="1" :max="90" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="planPath" :loading="loading">开始规划</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { planPath as apiPlanPath, listPaths, getPathSteps } from '../api/path'

const studentId = 1
const dialogVisible = ref(false)
const form = ref({ goal: '', subject: '', durationDays: 14 })
const paths = ref([])
const steps = ref([])
const loading = ref(false)

const stepTypeMap = { study: '学习', exercise: '练习', review: '复习', assessment: '评估' }

const stepStatus = (status) => {
  const map = { completed: 'success', in_progress: 'primary', pending: 'info' }
  return map[status] || 'info'
}
const stepIcon = (status) => {
  const map = { completed: 'Check', in_progress: 'Loading', pending: 'Timer' }
  return map[status] || 'Timer'
}

const planPath = async () => {
  if (!form.value.goal) {
    ElMessage.warning('请输入学习目标')
    return
  }
  loading.value = true
  try {
    const res = await apiPlanPath({ studentId, ...form.value })
    if (res.code === 200) {
      ElMessage.success('路径规划成功')
      dialogVisible.value = false
      await loadPaths()
    }
  } catch (e) {
    ElMessage.error('规划失败')
  } finally {
    loading.value = false
  }
}

const loadPaths = async () => {
  const res = await listPaths(studentId)
  if (res.code === 200) {
    paths.value = res.data
    if (paths.value.length > 0) {
      const stepRes = await getPathSteps(paths.value[0].id)
      if (stepRes.code === 200) steps.value = stepRes.data
    }
  }
}

onMounted(loadPaths)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
