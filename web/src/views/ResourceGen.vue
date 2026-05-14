<template>
  <div class="resource-gen-page">
    <!-- Header -->
    <div class="page-header">
      <div class="header-icon">
        <el-avatar :size="40" icon="Collection" style="background: #5436da" />
      </div>
      <div class="header-info">
        <div class="header-title">多智能体协同资源生成</div>
        <div class="header-desc">输入学科和主题，AI 将并行生成文档、思维导图、练习题等全方位学习资源</div>
      </div>
    </div>

    <!-- Generation History (Chat-like) -->
    <div v-if="generations.length > 0" class="history-area">
      <div v-for="(gen, gIdx) in generations" :key="gIdx" class="gen-block">
        <!-- User request -->
        <div class="message-row user">
          <div class="msg-bubble user">
            <div><strong>学科：</strong>{{ gen.subject }}</div>
            <div><strong>主题：</strong>{{ gen.topic }}</div>
            <div><strong>难度：</strong>{{ difficultyLabel(gen.difficulty) }}</div>
            <div><strong>资源：</strong>{{ gen.resourceTypes.map(t => typeMap[t]).join('、') }}</div>
          </div>
          <el-avatar :size="34" icon="User" style="background: #5436da;flex-shrink:0" />
        </div>
        <!-- AI response (resource cards) -->
        <div class="message-row assistant">
          <el-avatar :size="34" icon="Collection" style="background: #19c37d;flex-shrink:0" />
          <div class="resources-grid">
            <el-card v-for="(res, rIdx) in gen.resources" :key="rIdx" shadow="hover" class="resource-card" @click="showResourceDetail(res)">
              <div class="card-header-area">
                <el-tag :type="typeColor(res.resourceType)" size="small" effect="dark">
                  {{ typeMap[res.resourceType] || res.resourceType }}
                </el-tag>
                <span class="card-title">{{ res.title || res.resourceType }}</span>
              </div>
              <div class="card-preview markdown-body" v-html="renderPreview(res.content)"></div>
              <div class="card-footer">
                <el-button text size="small" @click.stop="copyContent(res.content)">📋 复制</el-button>
                <el-button text size="small" @click.stop="downloadAsMarkdown(res.content, res.title || res.resourceType)">
                  📥 下载
                </el-button>
              </div>
            </el-card>
          </div>
        </div>
      </div>
    </div>

    <!-- Input Section -->
    <div class="input-section">
      <div class="input-form">
        <div class="form-row">
          <div class="form-field" style="flex:1">
            <el-input v-model="form.subject" placeholder="学科（如：计算机科学）" clearable size="large" />
          </div>
          <div class="form-field" style="flex:2">
            <el-input v-model="form.topic" placeholder="主题（如：动态规划）" clearable size="large" />
          </div>
          <div class="form-field" style="flex:0 0 140px">
            <el-select v-model="form.difficulty" size="large" style="width:100%">
              <el-option label="简单" value="easy" />
              <el-option label="中等" value="medium" />
              <el-option label="困难" value="hard" />
            </el-select>
          </div>
        </div>
        <div class="form-row types-row">
          <el-checkbox-group v-model="form.resourceTypes" class="type-checkboxes">
            <el-checkbox label="document" border>学习文档</el-checkbox>
            <el-checkbox label="mindmap" border>思维导图</el-checkbox>
            <el-checkbox label="exercise" border>练习题</el-checkbox>
            <el-checkbox label="codecase" border>代码案例</el-checkbox>
            <el-checkbox label="reading" border>拓展阅读</el-checkbox>
            <el-checkbox label="video" border>视频脚本</el-checkbox>
          </el-checkbox-group>
        </div>
        <div class="submit-row">
          <el-button type="primary" size="large" :loading="loading" @click="generate" :icon="Promotion">
            生成资源
          </el-button>
        </div>
      </div>
    </div>

    <!-- Resource Detail Dialog -->
    <el-dialog v-model="detailVisible" :title="detailResource?.title || ''" width="800px" top="5vh" destroy-on-close>
      <div class="detail-content markdown-body" v-html="renderMarkdown(detailResource?.content || '')"></div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="downloadAsMarkdown(detailResource?.content || '', detailResource?.title)">
          下载 Markdown
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Promotion } from '@element-plus/icons-vue'
import { generateResources } from '../api/resource'
import { marked } from 'marked'

marked.setOptions({ breaks: true, gfm: true })

const form = reactive({
  subject: '',
  topic: '',
  difficulty: 'medium',
  resourceTypes: ['document', 'exercise', 'codecase']
})

const loading = ref(false)
const generations = ref([])
const detailVisible = ref(false)
const detailResource = ref(null)

const typeMap = {
  document: '学习文档', mindmap: '思维导图', exercise: '练习题',
  codecase: '代码案例', reading: '拓展阅读', video: '视频脚本'
}

const typeColor = (type) => {
  const map = { document: '', mindmap: 'success', exercise: 'warning', codecase: 'danger', reading: 'info', video: 'primary' }
  return map[type] || ''
}

const difficultyLabel = (d) => {
  const map = { easy: '简单', medium: '中等', hard: '困难' }
  return map[d] || d
}

const renderMarkdown = (content) => marked.parse(content || '')

const renderPreview = (content) => {
  if (!content) return ''
  // Show first 200 chars as preview
  const html = marked.parse(content)
  const div = document.createElement('div')
  div.innerHTML = html
  const text = div.textContent || ''
  return '<p>' + text.substring(0, 150) + (text.length > 150 ? '...' : '') + '</p>'
}

const generate = async () => {
  if (!form.subject.trim()) { ElMessage.warning('请输入学科'); return }
  if (!form.topic.trim()) { ElMessage.warning('请输入主题'); return }

  loading.value = true
  try {
    const res = await generateResources({
      subject: form.subject.trim(),
      topic: form.topic.trim(),
      difficulty: form.difficulty,
      resourceTypes: form.resourceTypes
    })
    if (res.code === 200) {
      generations.value.unshift({
        subject: form.subject.trim(),
        topic: form.topic.trim(),
        difficulty: form.difficulty,
        resourceTypes: [...form.resourceTypes],
        resources: res.data || []
      })
      ElMessage.success('资源生成成功')
    } else {
      ElMessage.error(res.message || '生成失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  } finally {
    loading.value = false
  }
}

const showResourceDetail = (res) => {
  detailResource.value = res
  detailVisible.value = true
}

const copyContent = async (content) => {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('已复制')
  } catch { ElMessage.error('复制失败') }
}

const downloadAsMarkdown = (content, title) => {
  const blob = new Blob([content], { type: 'text/markdown;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${title || 'resource'}_${Date.now()}.md`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}
</script>

<style scoped>
.resource-gen-page {
  max-width: 960px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.header-desc {
  font-size: 13px;
  color: #999;
  margin-top: 2px;
}

/* History */
.history-area {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.gen-block {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-row {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.message-row.user {
  flex-direction: row-reverse;
}

.msg-bubble.user {
  background: #5436da;
  color: #fff;
  padding: 12px 18px;
  border-radius: 14px 4px 14px 14px;
  font-size: 14px;
  line-height: 1.8;
  max-width: 70%;
}

.msg-bubble.user div {
  margin-bottom: 2px;
}

/* Resource Cards */
.resources-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 12px;
}

.resource-card {
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
  border-radius: 10px;
}

.resource-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.1);
}

.card-header-area {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.card-title {
  font-weight: 600;
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-preview {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  max-height: 80px;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
  gap: 4px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
  margin-top: 8px;
}

/* Input Form */
.input-section {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  padding: 20px 24px;
}

.input-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-row {
  display: flex;
  gap: 12px;
}

.form-field {
  flex: 1;
}

.types-row {
  flex-wrap: wrap;
}

.type-checkboxes {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.type-checkboxes :deep(.el-checkbox) {
  margin: 0;
}

.submit-row {
  display: flex;
  justify-content: center;
  padding-top: 8px;
}

/* Detail Dialog */
.detail-content {
  max-height: 65vh;
  overflow-y: auto;
}

/* Markdown styles for preview/detail */
.markdown-body :deep(p) { margin-bottom: 8px; }
.markdown-body :deep(h1), .markdown-body :deep(h2), .markdown-body :deep(h3) { margin: 12px 0 6px; }
.markdown-body :deep(ul), .markdown-body :deep(ol) { padding-left: 18px; margin-bottom: 6px; }
.markdown-body :deep(code) { background: #f0f0f0; padding: 2px 6px; border-radius: 3px; font-size: 12px; }
.markdown-body :deep(pre) { background: #1e1e1e; padding: 12px; border-radius: 6px; overflow-x: auto; font-size: 12px; }
.markdown-body :deep(pre code) { background: transparent; padding: 0; color: #d4d4d4; }
</style>
