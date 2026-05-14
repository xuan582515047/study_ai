<template>
  <div class="profile-center">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人中心</span>
          <el-button type="primary" @click="toggleEdit">{{ isEditing ? '取消' : '编辑资料' }}</el-button>
        </div>
      </template>

      <div class="profile-avatar" v-if="!isEditing">
        <el-avatar :size="80" :src="userInfo.avatar || defaultAvatar" />
        <h3>{{ userInfo.nickname || userInfo.username }}</h3>
        <p class="username">@{{ userInfo.username }}</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" class="profile-form" v-if="isEditing">
        <el-form-item label="头像">
          <div class="avatar-upload-wrapper">
            <div class="avatar-upload" @click="triggerUpload">
              <el-avatar :size="80" :src="form.avatar || defaultAvatar" />
              <div class="avatar-mask">
                <el-icon><Camera /></el-icon>
                <span>更换头像</span>
              </div>
            </div>
            <input
              type="file"
              ref="fileInput"
              style="display: none"
              accept="image/jpeg,image/png,image/gif,image/webp,image/bmp"
              @change="handleFileChange"
            />
            <el-progress
              v-if="uploadProgress > 0 && uploadProgress < 100"
              :percentage="uploadProgress"
              :stroke-width="4"
              style="width: 80px; margin-top: 8px;"
            />
          </div>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="年龄">
          <el-input-number v-model="form.age" :min="1" :max="120" placeholder="请输入年龄" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="0">女</el-radio>
            <el-radio :label="2">保密</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="form.phone" placeholder="请输入联系方式" />
        </el-form-item>
        <el-form-item label="大学专业">
          <el-input v-model="form.major" placeholder="请输入专业" />
        </el-form-item>
        <el-form-item label="学校">
          <el-input v-model="form.school" placeholder="请输入学校" />
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="form.grade" placeholder="请选择年级" style="width: 100%">
            <el-option label="大一" value="大一" />
            <el-option label="大二" value="大二" />
            <el-option label="大三" value="大三" />
            <el-option label="大四" value="大四" />
            <el-option label="研一" value="研一" />
            <el-option label="研二" value="研二" />
            <el-option label="研三" value="研三" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
        </el-form-item>
      </el-form>

      <el-descriptions :column="2" border v-else-if="userInfo">
        <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ userInfo.nickname || '-' }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ userInfo.age || '-' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ genderText(userInfo.gender) }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ userInfo.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ userInfo.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="大学专业">{{ userInfo.major || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学校">{{ userInfo.school || '-' }}</el-descriptions-item>
        <el-descriptions-item label="年级">{{ userInfo.grade || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/userStore'
import { uploadAvatar } from '@/api/auth'

const userStore = useUserStore()
const formRef = ref()
const fileInput = ref()
const isEditing = ref(false)
const saving = ref(false)
const uploadProgress = ref(0)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const userInfo = ref({})
const form = ref({})

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

const genderText = (val) => {
  const map = { 0: '女', 1: '男', 2: '保密' }
  return map[val] || '-'
}

const loadUserInfo = async () => {
  await userStore.fetchUserInfo()
  userInfo.value = { ...userStore.userInfo }
  form.value = { ...userStore.userInfo }
}

const toggleEdit = () => {
  isEditing.value = !isEditing.value
  if (isEditing.value) {
    form.value = { ...userInfo.value }
  }
}

const triggerUpload = () => {
  if (fileInput.value) {
    fileInput.value.click()
  }
}

const handleFileChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return

  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'image/bmp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('仅支持 JPG、PNG、GIF、WebP、BMP 格式的图片')
    e.target.value = ''
    return
  }

  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 10MB')
    e.target.value = ''
    return
  }

  uploadProgress.value = 0
  try {
    const res = await uploadAvatar(file)
    form.value.avatar = res.data
    userInfo.value.avatar = res.data
    ElMessage.success('头像上传成功')
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '上传失败')
  } finally {
    uploadProgress.value = 0
    e.target.value = ''
  }
}

const handleSave = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    const { id, username, ...updateData } = form.value
    await userStore.updateProfileAction(updateData)
    userInfo.value = { ...userStore.userInfo }
    ElMessage.success('保存成功')
    isEditing.value = false
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-center {
  max-width: 800px;
  margin: 0 auto;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}
.profile-avatar {
  text-align: center;
  margin-bottom: 24px;
}
.profile-avatar h3 {
  margin: 10px 0 4px;
}
.profile-avatar .username {
  color: #909399;
}
.profile-form {
  max-width: 600px;
}
.avatar-upload-wrapper {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.avatar-upload {
  position: relative;
  display: inline-block;
  cursor: pointer;
}
.avatar-upload .avatar-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.3s;
  font-size: 12px;
}
.avatar-upload:hover .avatar-mask {
  opacity: 1;
}
.avatar-upload .avatar-mask .el-icon {
  font-size: 20px;
  margin-bottom: 2px;
}
</style>
