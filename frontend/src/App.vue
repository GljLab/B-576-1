<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

onMounted(() => {
  // 检查本地存储的token
  const token = localStorage.getItem('token')
  if (token) {
    userStore.setToken(token)
    userStore.getUserInfo()
  }
})
</script>

<style>
#app {
  width: 100%;
  min-height: 100vh;
}
</style>
