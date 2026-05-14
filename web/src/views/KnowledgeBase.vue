<template>
  <div class="knowledge-base">
    <el-card>
      <template #header>
        <div class="card-header">
          <span style="font-size: 18px; font-weight: bold;">知识库管理</span>
          <el-button type="primary" @click="openCreateDialog" :icon="Plus">新建知识库</el-button>
        </div>
      </template>

      <el-empty v-if="list.length === 0" description="暂无知识库，点击右上角创建" />

      <el-row :gutter="20" v-else>
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in list" :key="item.id" style="margin-bottom: 20px;">
          <el-card shadow="hover" class="kb-card">
            <div class="kb-icon">
              <el-icon :size="48" color="#409EFF"><Collection /></el-icon>
            </div>
            <div class="kb-name" :title="item.name">{{ item.name }}</div>
            <div class="kb-desc" :title="item.description">{{ item.description || '暂无描述' }}</div>
            <div class="kb-meta">
              <span>{{ formatSize(item.fileSize) }}</span>
              <span>{{ formatDate(item.createdAt) }}</span>
            </div>
            <div class="kb-actions">
              <el-button size="small" text type="primary" @click="openEditDialog(item)" :icon="Edit">编辑</el-button>
              <el-button size="small" text type="danger" @click="handleDelete(item)" :icon="Delete">删除</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 新建/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑知识库' : '新建知识库'" width="600px" destroy-on-close>
      <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
        <el-form-item label="知识库名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：线性代数、高等数学" maxlength="128" show-word-limit />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="简要描述该知识库内容" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="内容来源">
          <el-radio-group v-model="sourceType">
            <el-radio label="manual">手动输入</el-radio>
            <el-radio label="file">上传文件</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="sourceType === 'file'">
          <el-upload
            ref="uploadRef"
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
            :show-file-list="true"
            :limit="1"
            accept=".txt,.md"
          >
            <el-button type="primary" :icon="Upload">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">仅支持 .txt / .md 文件，大小不超过 2MB</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item v-if="sourceType === 'manual' || form.content" label="知识库内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="10"
            placeholder="请输入知识库内容..."
            maxlength="8000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Upload, Collection } from '@element-plus/icons-vue'
import { listKnowledgeBases, createKnowledgeBase, updateKnowledgeBase, deleteKnowledgeBase } from '@/api/knowledgeBase'

const list = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const sourceType = ref('manual')
const uploadRef = ref(null)
const formRef = ref(null)

const form = ref({
  id: null,
  name: '',
  description: '',
  content: '',
  fileName: '',
  fileSize: null
})

const rules = {
  name: [{ required: true, message: '请输入知识库名称', trigger: 'blur' }],
  content: [{ required: true, message: '知识库内容不能为空', trigger: 'blur' }]
}

const fetchList = async () => {
  try {
    const res = await listKnowledgeBases()
    if (res.code === 200) {
      list.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取列表失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  }
}

const openCreateDialog = () => {
  isEdit.value = false
  sourceType.value = 'manual'
  form.value = { id: null, name: '', description: '', content: '', fileName: '', fileSize: null }
  dialogVisible.value = true
}

const openEditDialog = (item) => {
  isEdit.value = true
  sourceType.value = 'manual'
  form.value = {
    id: item.id,
    name: item.name,
    description: item.description || '',
    content: item.content,
    fileName: item.fileName || '',
    fileSize: item.fileSize
  }
  dialogVisible.value = true
}

const handleFileChange = (uploadFile) => {
  const file = uploadFile.raw
  if (!file) return
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 2MB')
    uploadRef.value?.clearFiles()
    return
  }
  const reader = new FileReader()
  reader.onload = (e) => {
    form.value.content = e.target.result
    form.value.fileName = file.name
    form.value.fileSize = file.size
    ElMessage.success('文件读取成功')
  }
  reader.onerror = () => {
    ElMessage.error('文件读取失败')
  }
  reader.readAsText(file, 'UTF-8')
}

const submitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  if (!form.value.content || form.value.content.trim().length === 0) {
    ElMessage.warning('知识库内容不能为空')
    return
  }

  submitting.value = true
  try {
    let res
    if (isEdit.value) {
      res = await updateKnowledgeBase(form.value.id, {
        name: form.value.name,
        description: form.value.description,
        content: form.value.content
      })
    } else {
      res = await createKnowledgeBase({
        name: form.value.name,
        description: form.value.description,
        content: form.value.content,
        fileName: form.value.fileName,
        fileSize: form.value.fileSize
      })
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      fetchList()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = (item) => {
  ElMessageBox.confirm(`确定删除知识库 "${item.name}" 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      const res = await deleteKnowledgeBase(item.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchList()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    })
    .catch(() => {})
}

const formatSize = (size) => {
  if (!size) return ''
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / (1024 * 1024)).toFixed(1) + ' MB'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleDateString('zh-CN')
}

onMounted(fetchList)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.kb-card {
  text-align: center;
  transition: transform 0.2s;
}
.kb-card:hover {
  transform: translateY(-4px);
}
.kb-icon {
  margin-bottom: 12px;
}
.kb-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.kb-desc {
  font-size: 13px;
  color: #909399;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-height: 20px;
}
.kb-meta {
  font-size: 12px;
  color: #c0c4cc;
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 12px;
}
.kb-actions {
  display: flex;
  justify-content: center;
  gap: 8px;
}
</style>
