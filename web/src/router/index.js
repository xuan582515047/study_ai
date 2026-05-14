import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../views/Layout.vue'
import { getToken } from '../utils/auth'
import { useUserStore } from '@/store/userStore'

const routes = [
  {
    path: '/login',
    component: () => import('../views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/register',
    component: () => import('../views/Register.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '总览' } },
      { path: 'profile', component: () => import('../views/ProfileBuild.vue'), meta: { title: '画像构建', noProfileCheck: true } },
      { path: 'resource', component: () => import('../views/ResourceGen.vue'), meta: { title: '资源生成' } },
      { path: 'path', component: () => import('../views/LearningPath.vue'), meta: { title: '学习路径' } },
      { path: 'tutor', component: () => import('../views/SmartTutor.vue'), meta: { title: '智能辅导' } },
      { path: 'assessment', component: () => import('../views/Assessment.vue'), meta: { title: '学习评估' } },
      { path: 'profile-center', component: () => import('../views/ProfileCenter.vue'), meta: { title: '个人中心' } },
      { path: 'agent-tutor', component: () => import('../views/MultiModalTutor.vue'), meta: { title: '问答导师' } },
      { path: 'agent-ppt', component: () => import('../views/PPTGenerator.vue'), meta: { title: 'PPT生成' } },
      { path: 'agent-doc', component: () => import('../views/DocGenerator.vue'), meta: { title: '文档生成' } },
      { path: 'agent-quiz', component: () => import('../views/QuizGenerator.vue'), meta: { title: '考题生成' } },
      { path: 'agent-mindmap', component: () => import('../views/MindMapGenerator.vue'), meta: { title: '思维导图' } },
      { path: 'knowledge-base', component: () => import('../views/KnowledgeBase.vue'), meta: { title: '知识库管理', noProfileCheck: true } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const token = getToken()
  if (to.meta?.public) {
    next()
    return
  }
  if (!token) {
    next('/login')
    return
  }
  const userStore = useUserStore()
  if (!to.meta?.noProfileCheck) {
    const hasProfile = await userStore.checkProfileStatus()
    if (!hasProfile && to.path !== '/profile') {
      next('/profile')
      return
    }
  }
  next()
})

export default router
