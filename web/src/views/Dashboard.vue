<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in stats" :key="item.title">
        <el-card class="stat-card" shadow="hover">
          <el-icon :size="40" :color="item.color"><component :is="item.icon" /></el-icon>
          <div class="stat-title">{{ item.title }}</div>
          <div class="stat-value">{{ item.value }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24" style="margin-bottom: 20px;">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>知识库</span>
              <el-button type="primary" size="small" @click="goToKnowledgeBase" :icon="Plus">添加知识库</el-button>
            </div>
          </template>
          <div v-if="knowledgeBases.length === 0" style="color: #909399; text-align: center; padding: 20px;">
            暂无知识库，点击右上角添加
          </div>
          <el-row :gutter="16" v-else>
            <el-col :span="6" v-for="kb in knowledgeBases.slice(0, 4)" :key="kb.id">
              <div class="kb-item" @click="goToKnowledgeBase">
                <el-icon :size="28" color="#409EFF"><Collection /></el-icon>
                <div class="kb-item-name">{{ kb.name }}</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card title="最近生成资源">
          <template #header><span>最近生成资源</span></template>
          <el-table :data="resources" size="small">
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="resourceType" label="类型" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ typeMap[row.resourceType] || row.resourceType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="时间" width="160" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card title="学习路径进度">
          <template #header><span>学习路径进度</span></template>
          <div v-for="path in paths" :key="path.id" class="path-item">
            <div class="path-name">{{ path.pathName }}</div>
            <el-progress :percentage="path.progressPercent" :status="path.progressPercent === 100 ? 'success' : ''" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Collection } from '@element-plus/icons-vue'
import { listResources } from '../api/resource'
import { listPaths } from '../api/path'
import { listKnowledgeBases } from '../api/knowledgeBase'

const router = useRouter()
const studentId = 1
const stats = ref([
  { title: '生成资源', value: 0, icon: 'Document', color: '#409EFF' },
  { title: '学习路径', value: 0, icon: 'MapLocation', color: '#67C23A' },
  { title: '评估次数', value: 0, icon: 'TrendCharts', color: '#E6A23C' },
  { title: '辅导对话', value: 0, icon: 'ChatDotRound', color: '#F56C6C' }
])
const resources = ref([])
const paths = ref([])
const knowledgeBases = ref([])
const typeMap = { document: '文档', mindmap: '思维导图', exercise: '练习题', codecase: '代码案例', reading: '拓展阅读', video: '视频' }

const goToKnowledgeBase = () => {
  router.push('/knowledge-base')
}

onMounted(async () => {
  const res = await listResources(studentId)
  if (res.code === 200) {
    resources.value = res.data.slice(0, 5)
    stats.value[0].value = res.data.length
  }
  const pathRes = await listPaths(studentId)
  if (pathRes.code === 200) {
    paths.value = pathRes.data
    stats.value[1].value = pathRes.data.length
  }
  const kbRes = await listKnowledgeBases()
  if (kbRes.code === 200) {
    knowledgeBases.value = kbRes.data || []
  }
})
</script>

<style scoped>
.stat-card { text-align: center; padding: 10px; }
.stat-title { margin-top: 10px; color: #666; font-size: 14px; }
.stat-value { margin-top: 5px; font-size: 24px; font-weight: bold; color: #333; }
.path-item { margin-bottom: 15px; }
.path-name { margin-bottom: 5px; font-size: 14px; }
.kb-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px;
  border-radius: 8px;
  background: #f5f7fa;
  cursor: pointer;
  transition: background 0.2s;
}
.kb-item:hover {
  background: #e6f2ff;
}
.kb-item-name {
  margin-top: 8px;
  font-size: 13px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 100%;
  text-align: center;
}
</style>
