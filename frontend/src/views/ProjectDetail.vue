<template>
  <div class="project-detail">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon>返回
        </el-button>
        <span class="title">{{ project?.projectName || '项目详情' }}</span>
        <el-tag :type="getStatusType(project?.status)" v-if="project">
          {{ getStatusText(project?.status) }}
        </el-tag>
      </div>
      <div class="header-actions">
        <el-button type="success" @click="handleDraw" v-if="project?.status === 0">
          <el-icon><Ticket /></el-icon>开始抽签
        </el-button>
        <el-button type="primary" @click="handleStartInterview" v-if="project?.status === 1">
          <el-icon><VideoPlay /></el-icon>开始面试
        </el-button>
        <el-button type="warning" @click="handleEndInterview" v-if="project?.status === 2">
          <el-icon><VideoCamera /></el-icon>结束面试
        </el-button>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stat-cards" v-loading="loading">
      <div class="stat-card">
        <div class="stat-icon blue">
          <el-icon :size="24"><Position /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.positionCount || 0 }}</div>
          <div class="stat-label">职位数量</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon green">
          <el-icon :size="24"><User /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.candidateCount || 0 }}</div>
          <div class="stat-label">考生总数</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon orange">
          <el-icon :size="24"><Check /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.checkedInCount || 0 }}</div>
          <div class="stat-label">已签到</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon cyan">
          <el-icon :size="24"><Finished /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.completedCount || 0 }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon purple">
          <el-icon :size="24"><Avatar /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.examinerCount || 0 }}</div>
          <div class="stat-label">考官数量</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon red">
          <el-icon :size="24"><OfficeBuilding /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.roomCount || 0 }}</div>
          <div class="stat-label">考场数量</div>
        </div>
      </div>
    </div>
    
    <!-- 项目信息 -->
    <el-card class="info-card">
      <template #header>
        <span class="card-title">项目信息</span>
      </template>
      
      <el-descriptions :column="3" border v-if="project">
        <el-descriptions-item label="项目编码">{{ project.projectCode }}</el-descriptions-item>
        <el-descriptions-item label="组织单位">{{ project.organizer }}</el-descriptions-item>
        <el-descriptions-item label="面试日期">{{ project.interviewDate }}</el-descriptions-item>
        <el-descriptions-item label="面试时间">
          {{ project.startTime }} - {{ project.endTime }}
        </el-descriptions-item>
        <el-descriptions-item label="面试地点">{{ project.location }}</el-descriptions-item>
        <el-descriptions-item label="成绩权重">
          笔试 {{ project.writtenWeight }}% / 面试 {{ project.interviewWeight }}%
        </el-descriptions-item>
        <el-descriptions-item label="去极值规则">
          {{ project.removeHighest ? '去最高分' : '' }}
          {{ project.removeHighest && project.removeLowest ? ' / ' : '' }}
          {{ project.removeLowest ? '去最低分' : '' }}
        </el-descriptions-item>
        <el-descriptions-item label="项目描述" :span="2">{{ project.description || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
    
    <!-- 快捷操作 -->
    <div class="quick-actions">
      <el-card class="action-card" @click="$router.push(`/candidates?projectId=${projectId}`)">
        <el-icon :size="32" color="#409EFF"><User /></el-icon>
        <span>考生管理</span>
      </el-card>
      
      <el-card class="action-card" @click="$router.push(`/examiners?projectId=${projectId}`)">
        <el-icon :size="32" color="#67C23A"><Avatar /></el-icon>
        <span>考官管理</span>
      </el-card>
      
      <el-card class="action-card" @click="$router.push(`/rooms?projectId=${projectId}`)">
        <el-icon :size="32" color="#E6A23C"><OfficeBuilding /></el-icon>
        <span>考场管理</span>
      </el-card>
      
      <el-card class="action-card" @click="$router.push(`/draw?projectId=${projectId}`)">
        <el-icon :size="32" color="#F56C6C"><Ticket /></el-icon>
        <span>抽签管理</span>
      </el-card>
      
      <el-card class="action-card" @click="$router.push(`/scoring?projectId=${projectId}`)">
        <el-icon :size="32" color="#909399"><Edit /></el-icon>
        <span>现场评分</span>
      </el-card>
      
      <el-card class="action-card" @click="$router.push(`/results?projectId=${projectId}`)">
        <el-icon :size="32" color="#00CED1"><Document /></el-icon>
        <span>成绩查询</span>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()

const projectId = ref(route.params.id)
const loading = ref(false)
const project = ref(null)
const stats = ref({})

const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'success' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '准备中', 1: '抽签中', 2: '面试中', 3: '已结束' }
  return texts[status] || '未知'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await api.projects.get(projectId.value)
    if (res.code === 200) {
      project.value = res.data.project
      stats.value = res.data
    }
  } catch (error) {
    console.error('加载失败', error)
  }
  loading.value = false
}

const handleDraw = async () => {
  try {
    await ElMessageBox.confirm('确定开始三盲抽签吗？抽签后将无法修改。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await api.draw.tripleBlind(projectId.value)
    if (res.code === 200) {
      ElMessage.success('抽签完成')
      await api.projects.updateStatus(projectId.value, 1)
      loadData()
    }
  } catch (e) {
    // 取消操作
  }
}

const handleStartInterview = async () => {
  try {
    await ElMessageBox.confirm('确定开始面试吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await api.projects.updateStatus(projectId.value, 2)
    ElMessage.success('面试已开始')
    loadData()
  } catch (e) {
    // 取消操作
  }
}

const handleEndInterview = async () => {
  try {
    await ElMessageBox.confirm('确定结束面试吗？结束后将计算所有成绩。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 计算所有成绩
    await api.scores.calculateAll(projectId.value)
    await api.projects.updateStatus(projectId.value, 3)
    ElMessage.success('面试已结束，成绩计算完成')
    loadData()
  } catch (e) {
    // 取消操作
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.project-detail {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .title {
        font-size: 20px;
        font-weight: 600;
      }
    }
  }
  
  .stat-cards {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 16px;
    margin-bottom: 24px;
    
    @media (max-width: 1400px) {
      grid-template-columns: repeat(3, 1fr);
    }
    
    @media (max-width: 768px) {
      grid-template-columns: repeat(2, 1fr);
    }
    
    .stat-card {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 20px;
      background: #fff;
      border-radius: 12px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      
      .stat-icon {
        width: 48px;
        height: 48px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        
        &.blue { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
        &.green { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
        &.orange { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
        &.cyan { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
        &.purple { background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%); }
        &.red { background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%); }
      }
      
      .stat-info {
        .stat-value {
          font-size: 24px;
          font-weight: 700;
          color: #303133;
        }
        
        .stat-label {
          font-size: 13px;
          color: #909399;
        }
      }
    }
  }
  
  .info-card {
    margin-bottom: 24px;
    
    .card-title {
      font-size: 16px;
      font-weight: 600;
    }
  }
  
  .quick-actions {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 16px;
    
    @media (max-width: 1200px) {
      grid-template-columns: repeat(3, 1fr);
    }
    
    @media (max-width: 768px) {
      grid-template-columns: repeat(2, 1fr);
    }
    
    .action-card {
      cursor: pointer;
      text-align: center;
      padding: 24px;
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }
      
      span {
        display: block;
        margin-top: 12px;
        font-size: 14px;
        color: #606266;
      }
    }
  }
}
</style>
