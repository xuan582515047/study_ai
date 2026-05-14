<template>
  <div class="profile-build">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学习画像构建</span>
          <el-button type="primary" @click="buildProfile" :loading="loading">生成画像</el-button>
        </div>
      </template>

      <el-alert
        title="首次使用需要先完成学习画像，AI将根据你的描述为你定制个性化学习服务"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 20px;"
      />

      <div style="margin-bottom: 15px;">
        <p style="color: #606266; margin-bottom: 10px; font-weight: bold;">请在下面对话框中描述你的学习情况，可以参考以下提示：</p>
        <el-row :gutter="10">
          <el-col :span="8" v-for="tip in tips" :key="tip.label" style="margin-bottom: 8px;">
            <el-tag type="info" effect="plain" style="width: 100%; justify-content: flex-start;">
              <strong>{{ tip.label }}：</strong>{{ tip.example }}
            </el-tag>
          </el-col>
        </el-row>
      </div>

      <el-input
        v-model="conversation"
        type="textarea"
        :rows="10"
        placeholder="例如：我是一名计算机专业大二学生，对数据结构比较感兴趣但算法基础薄弱，喜欢通过代码实践来学习，希望能在3个月内提升算法能力..."
      />
    </el-card>

    <el-card v-if="profile" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>学习画像</span>
          <el-button type="success" size="small" @click="goDashboard">进入系统</el-button>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="知识基础">{{ profile.knowledgeBase }}</el-descriptions-item>
        <el-descriptions-item label="认知风格">{{ profile.cognitiveStyle }}</el-descriptions-item>
        <el-descriptions-item label="易错点">{{ profile.errorPronePoints }}</el-descriptions-item>
        <el-descriptions-item label="学习目标">{{ profile.learningGoals }}</el-descriptions-item>
        <el-descriptions-item label="专业方向">{{ profile.majorDirection }}</el-descriptions-item>
        <el-descriptions-item label="学习节奏">{{ profile.learningPace }}</el-descriptions-item>
        <el-descriptions-item label="资源偏好">{{ profile.resourcePreference }}</el-descriptions-item>
        <el-descriptions-item label="薄弱知识点">{{ profile.weakPoints }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { buildProfile as buildProfileApi } from '../api/profile'
import { useUserStore } from '@/store/userStore'

const router = useRouter()
const userStore = useUserStore()

const tips = [
  { label: '年级', example: '如大一、高三' },
  { label: '专业/学科', example: '如计算机、数学' },
  { label: '学科薄弱点', example: '如算法、几何' },
  { label: '学习习惯', example: '如刷题、看视频' },
  { label: '学习目标', example: '如考研、竞赛' },
  { label: '学习风格', example: '如视觉型、实践型' }
]

const conversation = ref('')
const profile = ref(null)
const loading = ref(false)

onMounted(async () => {
  if (!userStore.userInfo && userStore.token) {
    try {
      await userStore.fetchUserInfo()
    } catch (e) {
      console.error('获取用户信息失败', e)
    }
  }
})

const buildProfile = async () => {
  if (!conversation.value.trim()) {
    ElMessage.warning('请先输入你的学习情况描述')
    return
  }
  if (!userStore.userInfo?.id) {
    ElMessage.warning('用户信息未加载，请重新登录')
    return
  }
  loading.value = true
  try {
    const res = await buildProfileApi({ conversation: conversation.value })
    if (res.code === 200) {
      profile.value = res.data
      userStore.hasProfile = true
      ElMessage.success('画像构建成功')
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    ElMessage.error('构建失败')
  } finally {
    loading.value = false
  }
}

const goDashboard = () => {
  userStore.hasProfile = true
  router.push('/dashboard')
}

</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
