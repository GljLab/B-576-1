import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  const token = ref('')
  const userInfo = ref(null)
  
  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }
  
  const clearToken = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }
  
  const login = async (username, password) => {
    try {
      const res = await api.auth.login({ username, password })
      if (res.code === 200) {
        setToken(res.data.token)
        userInfo.value = res.data.user
        // 保存用户信息到localStorage
        localStorage.setItem('userInfo', JSON.stringify(res.data.user))
        ElMessage.success('登录成功')
        return true
      } else {
        ElMessage.error(res.message || '登录失败')
        return false
      }
    } catch (error) {
      ElMessage.error('登录失败，请检查网络')
      return false
    }
  }
  
  const logout = async () => {
    try {
      await api.auth.logout()
    } catch (error) {
      // 忽略错误
    }
    clearToken()
    ElMessage.success('已退出登录')
  }
  
  const getUserInfo = async () => {
    try {
      const res = await api.auth.getInfo()
      if (res.code === 200) {
        userInfo.value = res.data
      }
    } catch (error) {
      clearToken()
    }
  }
  
  return {
    token,
    userInfo,
    setToken,
    clearToken,
    login,
    logout,
    getUserInfo
  }
})
