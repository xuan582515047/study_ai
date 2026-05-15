<template>
  <el-container class="layout">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <el-icon size="28" color="#409EFF"><Reading /></el-icon>
        <span>智创学伴</span>
      </div>
      <el-menu :default-active="$route.path" router class="menu" background-color="#001529" text-color="#fff" active-text-color="#409EFF">
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>总览</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <span>人物画像构建</span>
        </el-menu-item>
        <el-sub-menu index="/agent">
          <template #title>
            <el-icon><Cpu /></el-icon>
            <span>AI智能体</span>
          </template>
          <el-menu-item index="/agent-tutor">问答导师</el-menu-item>
          <el-menu-item index="/agent-ppt">PPT生成</el-menu-item>
          <el-menu-item index="/agent-doc">文档生成</el-menu-item>
          <el-menu-item index="/agent-quiz">考题生成</el-menu-item>
          <el-menu-item index="/agent-mindmap">思维导图</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/resource">
          <el-icon><Document /></el-icon>
          <span>资源生成</span>
        </el-menu-item>
        <el-menu-item index="/path">
          <el-icon><MapLocation /></el-icon>
          <span>学习路径</span>
        </el-menu-item>
        <el-menu-item index="/tutor">
          <el-icon><ChatDotRound /></el-icon>
          <span>智能辅导</span>
        </el-menu-item>
        <el-menu-item index="/assessment">
          <el-icon><TrendCharts /></el-icon>
          <span>学习评估</span>
        </el-menu-item>
        <el-menu-item index="/knowledge-base">
          <el-icon><Collection /></el-icon>
          <span>知识库管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <h2>{{ $route.meta.title }}</h2>
        <div class="user-area" v-if="userStore.userInfo">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo.avatar || defaultAvatar" />
              <span class="nickname">{{ userStore.userInfo.nickname || userStore.userInfo.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/userStore'
import { Collection } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

onMounted(() => {
  if (!userStore.userInfo && userStore.token) {
    userStore.fetchUserInfo().catch(() => {
      userStore.logout()
      router.push('/login')
    })
  }
})

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile-center')
  } else if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout { height: 100vh; }
.sidebar { background: #001529; }
.logo { height: 64px; display: flex; align-items: center; justify-content: center; gap: 10px; color: #fff; font-size: 20px; font-weight: bold; border-bottom: 1px solid rgba(255,255,255,0.1); }
.menu { border-right: none; }
.header { background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.1); display: flex; align-items: center; justify-content: space-between; }
.main { padding: 20px; overflow-y: auto; }
.user-area { cursor: pointer; }
.user-info { display: flex; align-items: center; gap: 8px; }
.nickname { color: #303133; font-size: 14px; }
</style>
