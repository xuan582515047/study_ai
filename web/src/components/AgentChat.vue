<template>
  <div class="agent-chat">
    <!-- 左侧历史对话侧边栏 -->
    <div class="sidebar" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
      <div class="sidebar-header">
        <el-button type="primary" class="new-chat-btn" @click="newChat">
          <el-icon><Plus /></el-icon>
          <span>开启新的对话</span>
        </el-button>
      </div>
      <div class="sidebar-list" v-loading="sessionsLoading">
        <div
          v-for="session in sessions"
          :key="session.sessionId"
          :class="['session-item', { active: currentSessionId === session.sessionId }]"
          @click="switchToSession(session.sessionId)"
        >
          <div class="session-info">
            <div class="session-title" :title="session.title">{{ session.title }}</div>
            <div class="session-meta">{{ session.messageCount }} 条消息</div>
          </div>
          <el-button
            text
            size="small"
            :icon="Delete"
            class="delete-btn"
            @click.stop="confirmDeleteSession(session.sessionId)"
          />
        </div>
        <div v-if="!sessions.length && !sessionsLoading" class="empty-sessions">
          <el-icon :size="40" color="#ccc"><ChatDotRound /></el-icon>
          <p>暂无历史对话</p>
        </div>
      </div>
    </div>

    <!-- 聊天区域 -->
    <div class="chat-container">
      <!-- Header -->
      <div class="chat-header">
        <div class="header-left">
          <el-button text class="sidebar-toggle" @click="sidebarCollapsed = !sidebarCollapsed">
            <el-icon><Fold /></el-icon>
          </el-button>
          <div class="header-icon">
            <el-avatar :size="36" icon="ChatDotRound" style="background: #19c37d" />
          </div>
          <div class="header-info">
            <div class="header-title">{{ title }}</div>
            <div v-if="subtitle" class="header-subtitle">{{ subtitle }}</div>
          </div>
        </div>
        <div class="header-right">
          <el-select
            v-model="knowledgeBaseId"
            placeholder="知识库"
            clearable
            size="small"
            style="width: 140px; margin-right: 8px;"
          >
            <el-option label="通用模式" :value="null" />
            <el-option v-for="kb in knowledgeBases" :key="kb.id" :label="kb.name" :value="kb.id" />
          </el-select>
          <el-tooltip content="清空对话">
            <el-button text :icon="Delete" circle @click="confirmClear" />
          </el-tooltip>
        </div>
      </div>

      <!-- Messages -->
      <div class="messages-area" ref="msgContainer">
        <div class="messages-list">
          <div v-for="(msg, idx) in messages" :key="idx" :class="['message-wrapper', msg.role]">
            <!-- AI Message -->
            <div v-if="msg.role === 'assistant'" class="message-row">
              <div class="msg-avatar">
                <el-avatar :size="34" icon="ChatDotRound" style="background: #19c37d" />
              </div>
              <div class="msg-body">
                <div class="msg-bubble ai">
                  <div class="markdown-body" v-html="renderMarkdown(msg.content)" />
                </div>
                <div class="msg-toolbar">
                  <el-tooltip content="复制">
                    <el-button text size="small" :icon="CopyDocument" @click="copyContent(msg.content)" />
                  </el-tooltip>
                  <el-tooltip v-if="agentType === 'doc'" content="下载 Markdown">
                    <el-button text size="small" @click="downloadAsMarkdown(msg.content)" style="font-size: 15px;padding:5px">📥</el-button>
                  </el-tooltip>
                  <el-tooltip v-if="agentType === 'ppt'" content="下载 PPTX">
                    <el-button text size="small" @click="downloadAsPPTX(msg.content)" style="font-size: 15px;padding:5px">📥</el-button>
                  </el-tooltip>
                </div>
              </div>
            </div>
            <!-- User Message -->
            <div v-else class="message-row user">
              <div class="msg-avatar">
                <el-avatar :size="34" icon="User" style="background: #5436da" />
              </div>
              <div class="msg-body user-body">
                <div class="msg-bubble user">
                  <div class="msg-plain-text">{{ msg.content }}</div>
                  <div v-if="msg.files && msg.files.length" class="msg-files">
                    <div v-for="(f, fi) in msg.files" :key="fi" class="msg-file-chip">
                      <img v-if="f.type?.startsWith('image/')" :src="f.url" class="file-thumb" />
                      <el-icon v-else :size="16"><Document /></el-icon>
                      <span>{{ f.name }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <!-- Loading -->
          <div v-if="loading" class="message-wrapper assistant">
            <div class="message-row">
              <div class="msg-avatar">
                <el-avatar :size="34" icon="ChatDotRound" style="background: #19c37d" />
              </div>
              <div class="msg-body">
                <div class="msg-bubble ai loading-bubble">
                  <div class="typing-dots"><span></span><span></span><span></span></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Input -->
      <div class="input-area">
        <div v-if="uploadedFiles.length" class="file-previews">
          <div v-for="(f, idx) in uploadedFiles" :key="idx" class="file-preview-chip">
            <img v-if="f.type?.startsWith('image/')" :src="f.url" class="preview-thumb" />
            <el-icon v-else :size="16"><Document /></el-icon>
            <span class="file-name">{{ f.name }}</span>
            <el-button text size="small" :icon="CloseBold" @click="removeFile(idx)" />
          </div>
        </div>
        <div class="input-row">
          <el-tooltip content="上传文件">
            <el-button text class="action-btn" :icon="Paperclip" @click="triggerFileUpload" />
          </el-tooltip>
          <input type="file" ref="fileInputRef" @change="onFileSelected" hidden multiple accept="image/*,.pdf,.doc,.docx,.txt,.md,.xls,.xlsx,.ppt,.pptx" />
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="1"
            placeholder="输入你的问题... (Enter 发送, Shift+Enter 换行)"
            @keydown.enter.prevent="handleKeydown"
            class="chat-input"
            resize="none"
            :disabled="loading"
          />
          <el-tooltip content="发送">
            <el-button type="primary" :icon="Promotion" :loading="loading" @click="sendMessage" class="send-btn" circle />
          </el-tooltip>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Promotion, Document, CopyDocument, Delete, Paperclip,
  CloseBold, Plus, Fold, ChatDotRound
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/userStore'
import { invokeAgent } from '@/api/agent'
import { listKnowledgeBases } from '@/api/knowledgeBase'
import { listSessions, getMessages, saveMessage, deleteSession } from '@/api/conversation'
import request from '@/api/request'
import { getToken } from '@/utils/auth'
import { marked } from 'marked'

const props = defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  placeholder: { type: String, default: '请输入你的问题...' },
  agentType: { type: String, required: true }
})

const userStore = useUserStore()

// ========== 基础响应式状态 ==========
const inputText = ref('')
const messages = ref([])
const loading = ref(false)
const knowledgeBases = ref([])
const knowledgeBaseId = ref(null)
const msgContainer = ref(null)
const fileInputRef = ref(null)
const uploadedFiles = ref([])

// ========== 侧边栏状态 ==========
const sidebarCollapsed = ref(false)
const sessions = ref([])
const sessionsLoading = ref(false)
const currentSessionId = ref('')

// ========== 计算属性 ==========
const studentId = computed(() => userStore.userInfo?.id || null)

// ========== UUID 生成 ==========
const generateUUID = () => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
    const r = Math.random() * 16 | 0
    return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16)
  })
}

// ========== 欢迎消息 ==========
const welcomeMessage = computed(() => {
  return `你好！我是 **${props.title}**。${props.subtitle ? props.subtitle : '请告诉我你的需求，我会尽力协助你。'}`
})

// ========== Markdown 渲染 ==========
marked.setOptions({ breaks: true, gfm: true })

const renderMarkdown = (content) => {
  if (!content) return ''
  return marked.parse(content)
}

// ========== 侧边栏 - 获取会话列表 ==========
const fetchSessions = async () => {
  if (!studentId.value) return
  sessionsLoading.value = true
  try {
    const res = await listSessions(props.agentType)
    if (res.code === 200) {
      sessions.value = res.data || []
    } else {
      console.warn('获取会话列表失败:', res.message)
    }
  } catch (e) {
    console.error('获取会话列表异常:', e)
  } finally {
    sessionsLoading.value = false
  }
}

// ========== 侧边栏 - 加载会话消息 ==========
const loadSessionMessages = async (sessionId) => {
  if (!studentId.value || !sessionId) return
  try {
    const res = await getMessages(sessionId)
    if (res.code === 200) {
      const historyMessages = (res.data || []).map(m => ({
        role: m.messageRole,
        content: m.messageContent
      }))
      messages.value = historyMessages
      currentSessionId.value = sessionId
    }
  } catch (e) {
    console.error('加载会话消息异常:', e)
  }
}

// ========== 侧边栏 - 切换会话 ==========
const switchToSession = (sessionId) => {
  if (sessionId === currentSessionId.value) return
  currentSessionId.value = sessionId
  loadSessionMessages(sessionId)
}

// ========== 侧边栏 - 开启新对话 ==========
const newChat = async () => {
  currentSessionId.value = generateUUID()
  messages.value = []
  messages.value.push({ role: 'assistant', content: welcomeMessage.value })
  await fetchSessions()
}

// ========== 侧边栏 - 删除会话 ==========
const confirmDeleteSession = (sessionId) => {
  ElMessageBox.confirm('确定要删除该历史对话吗？删除后不可恢复。', '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteSession(sessionId)
      ElMessage.success('对话已删除')
      // 从列表中移除
      sessions.value = sessions.value.filter(s => s.sessionId !== sessionId)
      // 如果删除的是当前会话，切换到其他会话或新建
      if (sessionId === currentSessionId.value) {
        if (sessions.value.length > 0) {
          await switchToSession(sessions.value[0].sessionId)
        } else {
          await newChat()
        }
      }
    } catch {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// ========== 知识库加载 ==========
const fetchKnowledgeBases = async () => {
  try {
    const res = await listKnowledgeBases()
    if (res.code === 200) {
      knowledgeBases.value = res.data || []
    }
  } catch { /* silent */ }
}

// ========== 滚动到底部 ==========
const scrollToBottom = async () => {
  await nextTick()
  if (msgContainer.value) {
    msgContainer.value.scrollTop = msgContainer.value.scrollHeight
  }
}

watch(messages, () => {
  scrollToBottom()
}, { deep: true })
watch(loading, () => scrollToBottom())

// ========== 构建上下文 ==========
const buildContext = () => {
  if (messages.value.length === 0) return ''
  const ctx = messages.value.map(m => {
    const role = m.role === 'user' ? '用户' : 'AI助手'
    let text = `${role}：${m.content}`
    if (m.files && m.files.length) {
      text += '\n[附件：' + m.files.map(f => f.name + (f.url ? '(' + f.url + ')' : '')).join('; ') + ']'
    }
    return text
  })
  return ctx.join('\n\n')
}

// ========== 发送消息 ==========
const sendMessage = async () => {
  const text = inputText.value.trim()
  const files = [...uploadedFiles.value]
  if (!text && files.length === 0) {
    ElMessage.warning('请输入内容或选择文件')
    return
  }
  inputText.value = ''

  // 1. 先构建上下文（使用当前消息列表）
  const context = buildContext()

  // 2. 保存用户消息到后端
  if (studentId.value) {
    try {
      await saveMessage({
        sessionId: currentSessionId.value,
        role: 'user',
        content: text,
        agentType: props.agentType
      })
    } catch (e) {
      console.error('保存用户消息失败:', e)
    }
  }

  // 3. 添加到本地显示
  const userMsg = { role: 'user', content: text }
  if (files.length) {
    userMsg.files = files.map(f => ({ name: f.name, url: f.url, type: f.type }))
  }
  messages.value.push(userMsg)
  uploadedFiles.value = []
  loading.value = true

  // 4. 调用 AI 智能体
  try {
    const payload = {
      input: text,
      context: context,
      studentId: studentId.value
    }
    if (knowledgeBaseId.value) {
      payload.knowledgeBaseId = knowledgeBaseId.value
    }
    const res = await invokeAgent(props.agentType, payload)

    const assistantContent = res.code === 200
      ? res.data
      : '抱歉，生成失败：' + (res.message || '未知错误')

    // 5. 保存 AI 回复到后端
    if (studentId.value) {
      try {
        await saveMessage({
          sessionId: currentSessionId.value,
          role: 'assistant',
          content: assistantContent,
          agentType: props.agentType
        })
      } catch (e) {
        console.error('保存AI回复失败:', e)
      }
    }

    // 6. 添加到本地显示
    messages.value.push({ role: 'assistant', content: assistantContent })

    // 7. 刷新会话列表（更新标题和时间）
    await fetchSessions()
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '请求失败，请检查网络连接后重试。' })
  } finally {
    loading.value = false
  }
}

// ========== 快捷键 ==========
const handleKeydown = (e) => {
  if (e.shiftKey) return
  sendMessage()
}

// ========== 文件上传 ==========
const triggerFileUpload = () => {
  fileInputRef.value?.click()
}

const onFileSelected = async (e) => {
  const files = e.target.files
  if (!files || !files.length) return
  e.target.value = ''

  for (const file of files) {
    try {
      if (file.size > 10 * 1024 * 1024) {
        ElMessage.warning(`文件 ${file.name} 超过10MB限制`)
        continue
      }
      const formData = new FormData()
      formData.append('file', file)
      const res = await request.post('/upload/file', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        timeout: 60000
      })
      if (res.code === 200) {
        uploadedFiles.value.push({
          name: file.name,
          url: res.data,
          type: file.type
        })
      } else {
        ElMessage.error(`上传失败：${res.message || '未知错误'}`)
      }
    } catch (err) {
      ElMessage.error(`文件 ${file.name} 上传失败`)
    }
  }
}

const removeFile = (idx) => {
  uploadedFiles.value.splice(idx, 1)
}

// ========== 工具函数 ==========
const copyContent = async (content) => {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}

const downloadAsMarkdown = (content) => {
  const blob = new Blob([content], { type: 'text/markdown;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${props.title}_${Date.now()}.md`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('Markdown 文件下载中...')
}

const downloadAsPPTX = async (content) => {
  try {
    const token = getToken()
    const response = await fetch('/api/agent/ppt/download', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? 'Bearer ' + token : ''
      },
      body: JSON.stringify({ content, title: props.title })
    })
    if (!response.ok) {
      ElMessage.error('PPTX 生成失败')
      return
    }
    const blob = await response.blob()
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${props.title}_${Date.now()}.pptx`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    ElMessage.success('PPTX 文件下载中...')
  } catch {
    ElMessage.error('PPTX 下载失败，请检查后端 PPTX 导出服务是否可用')
  }
}

// ========== 清空当前对话 ==========
const confirmClear = () => {
  if (messages.value.length === 0) return
  ElMessageBox.confirm('确定要清空当前对话吗？', '确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    messages.value = []
    uploadedFiles.value = []
    messages.value.push({ role: 'assistant', content: welcomeMessage.value })
    ElMessage.success('对话已清空')
  }).catch(() => {})
}

// ========== 生命周期 ==========
onMounted(async () => {
  fetchKnowledgeBases()

  if (studentId.value) {
    // 有登录用户 - 从后端加载会话列表
    await fetchSessions()
    if (sessions.value.length > 0) {
      // 自动加载第一个会话
      currentSessionId.value = sessions.value[0].sessionId
      await loadSessionMessages(currentSessionId.value)
    } else {
      // 无历史会话，显示欢迎消息
      currentSessionId.value = generateUUID()
      messages.value.push({ role: 'assistant', content: welcomeMessage.value })
    }
  } else {
    // 未登录，显示欢迎消息
    currentSessionId.value = generateUUID()
    messages.value.push({ role: 'assistant', content: welcomeMessage.value })
  }
})
</script>

<style scoped>
/* ========== 布局 ========== */
.agent-chat {
  height: calc(100vh - 120px);
  display: flex;
  padding: 0;
  background: #f0f2f5;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* ========== 左侧边栏 ========== */
.sidebar {
  width: 260px;
  min-width: 260px;
  background: #1e1e2d;
  display: flex;
  flex-direction: column;
  transition: width 0.3s, min-width 0.3s;
  overflow: hidden;
}

.sidebar-collapsed {
  width: 0;
  min-width: 0;
}

.sidebar-header {
  padding: 16px;
  flex-shrink: 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.new-chat-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 14px;
  border-radius: 8px;
  height: 40px;
}

.new-chat-btn .el-icon {
  font-size: 16px;
}

.sidebar-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.sidebar-list::-webkit-scrollbar {
  width: 4px;
}

.sidebar-list::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 2px;
}

.session-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  cursor: pointer;
  transition: background 0.2s;
  position: relative;
  border-left: 3px solid transparent;
}

.session-item:hover {
  background: rgba(255, 255, 255, 0.06);
}

.session-item.active {
  background: rgba(255, 255, 255, 0.1);
  border-left-color: #19c37d;
}

.session-item.active .session-title {
  color: #fff;
}

.session-info {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.session-title {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
}

.session-meta {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
  margin-top: 2px;
}

.delete-btn {
  flex-shrink: 0;
  color: rgba(255, 255, 255, 0.3) !important;
  opacity: 0;
  transition: opacity 0.2s, color 0.2s;
  margin-left: 4px;
}

.session-item:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  color: #f56c6c !important;
}

.empty-sessions {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: rgba(255, 255, 255, 0.3);
  font-size: 13px;
}

.empty-sessions p {
  margin-top: 10px;
}

/* ========== 聊天容器 ========== */
.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  min-width: 0;
}

/* Header */
.chat-header {
  padding: 14px 20px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sidebar-toggle {
  font-size: 16px;
  color: #666;
  padding: 4px;
}

.header-info .header-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.header-info .header-subtitle {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.header-right {
  display: flex;
  align-items: center;
}

/* Messages Area */
.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  background: #f7f8fa;
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.message-wrapper {
  width: 100%;
}

.message-row {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.message-row.user {
  flex-direction: row-reverse;
  justify-content: flex-start;
  gap: 10px;
}

.msg-avatar {
  flex-shrink: 0;
}

.msg-body {
  max-width: 75%;
  display: flex;
  flex-direction: column;
}

.user-body {
  align-items: flex-end;
}

.msg-bubble {
  padding: 12px 16px;
  border-radius: 14px;
  line-height: 1.6;
  font-size: 14px;
  word-wrap: break-word;
  white-space: pre-wrap;
}

.msg-bubble.ai {
  background: #fff;
  color: #333;
  border: 1px solid #e8e8e8;
  border-top-left-radius: 4px;
}

.msg-bubble.user {
  background: #e8edff;
  color: #333;
  border-top-right-radius: 4px;
  border: 1px solid #cdd9f5;
}

.msg-plain-text {
  white-space: pre-wrap;
}

.msg-files {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.msg-file-chip {
  display: flex;
  align-items: center;
  gap: 4px;
  background: rgba(0, 0, 0, 0.04);
  border-radius: 6px;
  padding: 4px 8px;
  font-size: 12px;
  color: #666;
}

.file-thumb {
  width: 24px;
  height: 24px;
  object-fit: cover;
  border-radius: 3px;
}

/* Toolbar */
.msg-toolbar {
  display: flex;
  gap: 2px;
  padding: 4px 4px 0 4px;
  opacity: 0;
  transition: opacity 0.15s;
}

.msg-body:hover .msg-toolbar {
  opacity: 1;
}

.msg-toolbar :deep(.el-button) {
  color: #999;
}

/* Loading */
.loading-bubble {
  padding: 14px 20px;
}

.typing-dots {
  display: flex;
  gap: 4px;
  align-items: center;
}

.typing-dots span {
  width: 8px;
  height: 8px;
  background: #ccc;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-dots span:nth-child(1) { animation-delay: 0s; }
.typing-dots span:nth-child(2) { animation-delay: 0.2s; }
.typing-dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

/* Input Area */
.input-area {
  padding: 14px 20px 18px;
  border-top: 1px solid #e8e8e8;
  flex-shrink: 0;
}

.file-previews {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.file-preview-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #f0f0f0;
  border-radius: 8px;
  padding: 4px 10px;
  font-size: 12px;
}

.preview-thumb {
  width: 28px;
  height: 28px;
  object-fit: cover;
  border-radius: 4px;
}

.file-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.input-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.action-btn {
  font-size: 18px;
  color: #666;
  margin-bottom: 2px;
}

.chat-input :deep(.el-textarea__inner) {
  border-radius: 10px;
  resize: none;
  padding: 10px 14px;
  font-size: 14px;
  line-height: 1.5;
  min-height: 42px;
  max-height: 120px;
  border: 1px solid #ddd;
  transition: border-color 0.2s;
}

.chat-input :deep(.el-textarea__inner:focus) {
  border-color: #5436da;
  box-shadow: 0 0 0 2px rgba(84, 54, 218, 0.1);
}

.send-btn {
  flex-shrink: 0;
}

/* Markdown Styles */
.markdown-body :deep(p) {
  margin-bottom: 8px;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4) {
  margin: 16px 0 8px;
  font-weight: 600;
  color: #1a1a1a;
}

.markdown-body :deep(h1) { font-size: 1.4em; }
.markdown-body :deep(h2) { font-size: 1.25em; }
.markdown-body :deep(h3) { font-size: 1.1em; }

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  padding-left: 20px;
  margin-bottom: 8px;
}

.markdown-body :deep(li) {
  margin-bottom: 4px;
}

.markdown-body :deep(code) {
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
  color: #e83e8c;
}

.markdown-body :deep(pre) {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 10px 0;
  font-size: 13px;
  line-height: 1.5;
}

.markdown-body :deep(pre code) {
  background: transparent;
  color: #d4d4d4;
  padding: 0;
  border-radius: 0;
}

.markdown-body :deep(blockquote) {
  border-left: 4px solid #5436da;
  margin: 10px 0;
  padding: 8px 16px;
  background: #f8f8ff;
  border-radius: 0 4px 4px 0;
  color: #555;
}

.markdown-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 10px 0;
  font-size: 13px;
}

.markdown-body :deep(th),
.markdown-body :deep(td) {
  border: 1px solid #ddd;
  padding: 8px 12px;
  text-align: left;
}

.markdown-body :deep(th) {
  background: #f5f5f5;
  font-weight: 600;
}

.markdown-body :deep(hr) {
  border: none;
  border-top: 1px solid #e0e0e0;
  margin: 16px 0;
}

.markdown-body :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 8px 0;
}

.markdown-body :deep(a) {
  color: #5436da;
  text-decoration: none;
}

.markdown-body :deep(a:hover) {
  text-decoration: underline;
}

.markdown-body :deep(strong) {
  font-weight: 600;
}
</style>
