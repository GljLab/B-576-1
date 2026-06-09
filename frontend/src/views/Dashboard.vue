<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h2>欢迎使用智慧面试评分系统</h2>
        <p>今天是 {{ currentDate }}，祝您工作愉快！</p>
      </div>
      <div class="welcome-actions">
        <el-button type="primary" @click="$router.push('/projects')">
          <el-icon><Folder /></el-icon>进入项目管理
        </el-button>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card blue">
        <div class="stat-icon">
          <el-icon :size="32"><Folder /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.projectCount }}</div>
          <div class="stat-title">面试项目</div>
        </div>
      </div>
      
      <div class="stat-card green">
        <div class="stat-icon">
          <el-icon :size="32"><User /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.candidateCount }}</div>
          <div class="stat-title">考生总数</div>
        </div>
      </div>
      
      <div class="stat-card orange">
        <div class="stat-icon">
          <el-icon :size="32"><Avatar /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.examinerCount }}</div>
          <div class="stat-title">考官总数</div>
        </div>
      </div>
      
      <div class="stat-card cyan">
        <div class="stat-icon">
          <el-icon :size="32"><OfficeBuilding /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.roomCount }}</div>
          <div class="stat-title">考场总数</div>
        </div>
      </div>
    </div>
    
    <!-- 最近项目 -->
    <div class="section">
      <div class="section-header">
        <h3>最近项目</h3>
        <el-button type="primary" link @click="$router.push('/projects')">
          查看全部 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      
      <el-table :data="recentProjects" v-loading="loading" stripe>
        <el-table-column prop="projectName" label="项目名称" min-width="200" />
        <el-table-column prop="projectCode" label="项目编码" width="150" />
        <el-table-column prop="organizer" label="组织单位" min-width="150" />
        <el-table-column prop="interviewDate" label="面试日期" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="$router.push(`/projects/${row.id}`)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 功能入口 -->
    <div class="section">
      <div class="section-header">
        <h3>快捷入口</h3>
      </div>
      
      <div class="quick-links">
        <div class="quick-link" @click="$router.push('/draw')">
          <div class="link-icon blue">
            <el-icon :size="28"><Ticket /></el-icon>
          </div>
          <div class="link-text">
            <h4>三盲抽签</h4>
            <p>考官、考生、职位抽签</p>
          </div>
        </div>
        
        <div class="quick-link" @click="$router.push('/scoring')">
          <div class="link-icon green">
            <el-icon :size="28"><Edit /></el-icon>
          </div>
          <div class="link-text">
            <h4>现场评分</h4>
            <p>考官在线打分评价</p>
          </div>
        </div>
        
        <div class="quick-link" @click="$router.push('/results')">
          <div class="link-icon orange">
            <el-icon :size="28"><Document /></el-icon>
          </div>
          <div class="link-text">
            <h4>成绩查询</h4>
            <p>查看面试成绩排名</p>
          </div>
        </div>
        
        <div class="quick-link" @click="$router.push('/candidates')">
          <div class="link-icon cyan">
            <el-icon :size="28"><User /></el-icon>
          </div>
          <div class="link-text">
            <h4>考生签到</h4>
            <p>考生身份核验签到</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import api from '@/api'
import dayjs from 'dayjs'

const loading = ref(false)
const recentProjects = ref([])
const stats = ref({
  projectCount: 0,
  candidateCount: 0,
  examinerCount: 0,
  roomCount: 0
})

const currentDate = computed(() => {
  return dayjs().format('YYYY年MM月DD日 dddd')
})

const getStatusType = (status) => {
  const types = {
    0: 'info',
    1: 'warning',
    2: 'primary',
    3: 'success'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    0: '准备中',
    1: '抽签中',
    2: '面试中',
    3: '已结束'
  }
  return texts[status] || '未知'
}

const loadData = async () => {
  loading.value = true
  try {
    // 获取最近项目
    const res = await api.projects.list({ pageNum: 1, pageSize: 5 })
    if (res.code === 200) {
      recentProjects.value = res.data.records || []
      stats.value.projectCount = res.data.total || 0
    }
    
    // 获取考生总数
    const candidateRes = await api.candidates.list({ pageNum: 1, pageSize: 1 })
    if (candidateRes.code === 200) {
      stats.value.candidateCount = candidateRes.data.total || 0
    }
    
    // 获取考官总数
    const examinerRes = await api.examiners.list({ pageNum: 1, pageSize: 1 })
    if (examinerRes.code === 200) {
      stats.value.examinerCount = examinerRes.data.total || 0
    }
    
    // 获取考场总数
    const roomRes = await api.rooms.list({ pageNum: 1, pageSize: 1 })
    if (roomRes.code === 200) {
      stats.value.roomCount = roomRes.data.total || 0
    }
  } catch (error) {
    console.error('加载数据失败', error)
  }
  loading.value = false
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.dashboard {
  .welcome-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 16px;
    margin-bottom: 24px;
    color: #fff;
    
    .welcome-content {
      h2 {
        font-size: 24px;
        font-weight: 600;
        margin-bottom: 8px;
      }
      
      p {
        font-size: 14px;
        opacity: 0.9;
      }
    }
    
    .welcome-actions {
      .el-button {
        background: rgba(255, 255, 255, 0.2);
        border: 1px solid rgba(255, 255, 255, 0.3);
        color: #fff;
        
        &:hover {
          background: rgba(255, 255, 255, 0.3);
        }
      }
    }
  }
  
  .stat-cards {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 24px;
    
    @media (max-width: 1200px) {
      grid-template-columns: repeat(2, 1fr);
    }
    
    @media (max-width: 600px) {
      grid-template-columns: 1fr;
    }
    
    .stat-card {
      display: flex;
      align-items: center;
      gap: 20px;
      padding: 24px;
      background: #fff;
      border-radius: 12px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      transition: transform 0.3s, box-shadow 0.3s;
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }
      
      .stat-icon {
        width: 64px;
        height: 64px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
      }
      
      &.blue .stat-icon {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }
      
      &.green .stat-icon {
        background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
      }
      
      &.orange .stat-icon {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }
      
      &.cyan .stat-icon {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      }
      
      .stat-info {
        .stat-value {
          font-size: 32px;
          font-weight: 700;
          color: #303133;
        }
        
        .stat-title {
          font-size: 14px;
          color: #909399;
          margin-top: 4px;
        }
      }
    }
  }
  
  .section {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    margin-bottom: 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      
      h3 {
        font-size: 18px;
        font-weight: 600;
        color: #303133;
      }
    }
  }
  
  .quick-links {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    
    @media (max-width: 1200px) {
      grid-template-columns: repeat(2, 1fr);
    }
    
    @media (max-width: 600px) {
      grid-template-columns: 1fr;
    }
    
    .quick-link {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 20px;
      background: #f8f9fa;
      border-radius: 12px;
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        background: #f0f2f5;
        transform: translateX(4px);
      }
      
      .link-icon {
        width: 56px;
        height: 56px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        
        &.blue {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }
        
        &.green {
          background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
        }
        
        &.orange {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        }
        
        &.cyan {
          background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        }
      }
      
      .link-text {
        h4 {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 4px;
        }
        
        p {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
}
</style>
