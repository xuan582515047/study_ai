<template>
  <div class="login-page">
    <div class="login-box">
      <div class="login-header">
        <el-icon size="40" color="#409EFF"><Reading /></el-icon>
        <h2>StudyAI 登录</h2>
        <p>基于大模型的个性化资源生成与学习多智能体系统</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="submit-btn" @click="handleLogin" :loading="loading">登录</el-button>
        </el-form-item>
        <div class="form-footer">
          <span>还没有账号？<el-link type="primary" @click="goRegister">立即注册</el-link></span>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Reading } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/userStore'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = ref({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.loginAction(form.value)
    ElMessage.success('登录成功')
    if (!userStore.hasProfile) {
      router.push('/profile')
    } else {
      router.push('/dashboard')
    }
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const goRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e3f2fd 0%, #f3e5f5 100%);
}
.login-box {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
}
.login-header {
  text-align: center;
  margin-bottom: 30px;
}
.login-header h2 {
  margin: 12px 0 6px;
  color: #303133;
  font-size: 24px;
}
.login-header p {
  color: #909399;
  font-size: 14px;
}
.submit-btn {
  width: 100%;
}
.form-footer {
  text-align: center;
  color: #606266;
  font-size: 14px;
}
</style>
