<template>
  <div class="dashboard-page">
    <!-- ===== AI智能辅导 对话窗口（占满全屏） ===== -->
    <div class="tutor-section">
      <div class="tutor-chat-wrapper">
        <AgentChat
          title="AI智能辅导"
          subtitle="你可以问我任何学习相关的问题，我会根据你的学习画像提供个性化辅导。"
          placeholder="输入你的学习问题..."
          agent-type="tutor"
          show-welcome
        />
      </div>
    </div>

    <!-- 5个智能体功能入口 -->
    <div class="agents-section">
      <h2 class="section-title">AI 智能体</h2>
      <p class="section-desc">选择智能体，开启个性化学习之旅</p>
      <div class="agents-grid">
        <el-card
          v-for="agent in agents"
          :key="agent.path"
          class="agent-card"
          :class="agent.colorClass"
          shadow="hover"
          @click="goToAgent(agent.path)"
        >
          <div class="card-content">
            <div class="card-icon-wrapper">
              <el-icon :size="48" :color="agent.iconColor"><component :is="agent.icon" /></el-icon>
            </div>
            <h3 class="agent-name">{{ agent.name }}</h3>
            <p class="agent-desc">{{ agent.description }}</p>
          </div>
          <div class="card-action">
            <el-button :type="agent.btnType" size="small" round>
              立即使用 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import {
  ChatDotRound, Monitor, Document,
  Edit, Share, ArrowRight, Collection
} from '@element-plus/icons-vue'
import AgentChat from '@/components/AgentChat.vue'

const router = useRouter()

const agents = [
  {
    name: '多模态问答导师',
    description: '输入你的问题，AI将提供结构化解答、图解建议和智能追问',
    path: '/agent-tutor',
    icon: 'ChatDotRound',
    iconColor: '#19c37d',
    colorClass: 'card-tutor',
    btnType: 'success'
  },
  {
    name: 'PPT智能生成师',
    description: '输入主题或需求，AI将生成完整的PPT大纲和每页内容建议',
    path: '/agent-ppt',
    icon: 'Monitor',
    iconColor: '#e6a23c',
    colorClass: 'card-ppt',
    btnType: 'warning'
  },
  {
    name: '知识点深度讲解文档生成师',
    description: '输入需要讲解的知识点，AI将生成背景、原理、推导、应用、误区等深度文档',
    path: '/agent-doc',
    icon: 'Document',
    iconColor: '#409eff',
    colorClass: 'card-doc',
    btnType: 'primary'
  },
  {
    name: '同类考题智能生成师',
    description: '输入原题或知识点，AI将分析题型并生成同类练习题及详细解析',
    path: '/agent-quiz',
    icon: 'Edit',
    iconColor: '#f56c6c',
    colorClass: 'card-quiz',
    btnType: 'danger'
  },
  {
    name: '知识框架思维导图生成师',
    description: '输入主题或文档，AI将生成结构化的思维导图（Markdown/Mermaid格式）',
    path: '/agent-mindmap',
    icon: 'Share',
    iconColor: '#673ab7',
    colorClass: 'card-mindmap',
    btnType: 'info'
  }
]

const goToAgent = (path) => {
  router.push(path)
}
</script>

<style scoped>
.dashboard-page {
  width: 100%;
  min-height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
}

/* ===== AI智能辅导 对话区域 ===== */
.tutor-section {
  flex: 1;
  min-height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
}

.tutor-chat-wrapper {
  flex: 1;
  min-height: calc(100vh - 100px);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.tutor-chat-wrapper :deep(.agent-chat) {
  height: calc(100vh - 100px);
  min-height: calc(100vh - 100px);
}

/* ===== 智能体区域 ===== */
.agents-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 6px 0;
  text-align: center;
}

.section-desc {
  color: #909399;
  margin: 0 0 24px 0;
  font-size: 18px;
  text-align: center;
}

.agents-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

/* ===== 智能体卡片 ===== */
.agent-card {
  border-radius: 14px;
  cursor: pointer;
  transition: transform 0.25s, box-shadow 0.25s;
  border: 1px solid transparent;
  overflow: hidden;
  position: relative;
}

.agent-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
}

/* 卡片顶部彩色装饰条 */
.agent-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
}

.card-tutor::before { background: linear-gradient(90deg, #19c37d, #48c774); }
.card-ppt::before { background: linear-gradient(90deg, #e6a23c, #f0c040); }
.card-doc::before { background: linear-gradient(90deg, #409eff, #79bbff); }
.card-quiz::before { background: linear-gradient(90deg, #f56c6c, #fa8c8c); }
.card-mindmap::before { background: linear-gradient(90deg, #673ab7, #9575cd); }

.card-content {
  padding: 30px 24px 16px;
  text-align: center;
}

.card-icon-wrapper {
  margin-bottom: 16px;
  display: flex;
  justify-content: center;
}

.agent-name {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 10px 0;
}

.agent-desc {
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
  margin: 0;
  min-height: 42px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-action {
  text-align: center;
  padding: 0 24px 24px;
}
</style>
