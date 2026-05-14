import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { login, register, getUserInfo, updateProfile } from '@/api/auth'
import { getProfileStatus } from '@/api/profile'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const userInfo = ref(null)
  const hasProfile = ref(false)

  const setTokenValue = (val) => {
    token.value = val
    setToken(val)
  }

  const clearUser = () => {
    token.value = ''
    userInfo.value = null
    hasProfile.value = false
    removeToken()
  }

  const loginAction = async (loginForm) => {
    const res = await login(loginForm)
    setTokenValue(res.data.token)
    userInfo.value = res.data.user
    hasProfile.value = res.data.hasProfile || false
    return res
  }

  const registerAction = async (registerForm) => {
    return await register(registerForm)
  }

  const fetchUserInfo = async () => {
    const res = await getUserInfo()
    userInfo.value = res.data
    return res
  }

  const checkProfileStatus = async () => {
    try {
      const res = await getProfileStatus()
      hasProfile.value = res.data?.hasProfile || false
      return hasProfile.value
    } catch (e) {
      hasProfile.value = false
      return false
    }
  }

  const updateProfileAction = async (profileForm) => {
    const res = await updateProfile(profileForm)
    userInfo.value = res.data
    return res
  }

  const logout = () => {
    clearUser()
  }

  return {
    token,
    userInfo,
    hasProfile,
    setTokenValue,
    clearUser,
    loginAction,
    registerAction,
    fetchUserInfo,
    checkProfileStatus,
    updateProfileAction,
    logout
  }
})
