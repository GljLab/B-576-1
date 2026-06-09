<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <div class="logo-wrapper">
        <el-icon :size="28" color="#fff"><Stamp /></el-icon>
        <span v-show="!isCollapse" class="logo-text">面试评分系统</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        class="aside-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        
        <el-menu-item index="/projects">
          <el-icon><Folder /></el-icon>
          <span>项目管理</span>
        </el-menu-item>
        
        <el-menu-item index="/candidates">
          <el-icon><User /></el-icon>
          <span>考生管理</span>
        </el-menu-item>
        
        <el-menu-item index="/examiners">
          <el-icon><Avatar /></el-icon>
          <span>考官管理</span>
        </el-menu-item>
        
        <el-menu-item index="/rooms">
          <el-icon><OfficeBuilding /></el-icon>
          <span>考场管理</span>
        </el-menu-item>
        
        <el-menu-item index="/draw">
          <el-icon><Ticket /></el-icon>
          <span>抽签管理</span>
        </el-menu-item>
        
        <el-menu-item index="/scoring">
          <el-icon><Edit /></el-icon>
          <span>现场评分</span>
        </el-menu-item>
        
        <el-menu-item index="/results">
          <el-icon><Document /></el-icon>
          <span>成绩查询</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container class="main-container">
      <!-- 头部 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon 
            :size="20" 
            class="collapse-btn"
            @click="isCollapse = !isCollapse"
          >
            <component :is="isCollapse ? 'Expand' : 'Fold'" />
          </el-icon>
          
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute.meta?.title">
              {{ currentRoute.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="36" class="user-avatar">
                {{ userStore.userInfo?.realName?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="user-name">{{ userStore.userInfo?.realName || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人信息
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon>修改密码
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 主内容区 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

const activeMenu = computed(() => {
  const { path } = route
  // 处理子路由的情况
  if (path.startsWith('/projects/')) {
    return '/projects'
  }
  return path
})

const currentRoute = computed(() => route)

const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      // 显示个人信息
      break
    case 'password':
      // 修改密码
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await userStore.logout()
        router.push('/login')
      } catch (e) {
        // 取消操作
      }
      break
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
  width: 100%;
}

.layout-aside {
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;
  
  .logo-wrapper {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    background-color: #263445;
    
    .logo-text {
      font-size: 16px;
      font-weight: 600;
      color: #fff;
      white-space: nowrap;
    }
  }
  
  .aside-menu {
    border-right: none;
    
    :deep(.el-menu-item) {
      height: 50px;
      line-height: 50px;
      
      &:hover {
        background-color: #263445 !important;
      }
      
      &.is-active {
        background-color: #409EFF !important;
        color: #fff !important;
      }
    }
  }
}

.main-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.layout-header {
  height: 60px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .collapse-btn {
      cursor: pointer;
      padding: 8px;
      border-radius: 4px;
      
      &:hover {
        background: #f5f5f5;
      }
    }
  }
  
  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 4px 8px;
      border-radius: 8px;
      
      &:hover {
        background: #f5f5f5;
      }
      
      .user-avatar {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }
      
      .user-name {
        font-size: 14px;
        color: #606266;
      }
    }
  }
}

.layout-main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
