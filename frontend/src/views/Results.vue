<template>
  <div class="results-page">
    <div class="page-header">
      <span class="title">成绩查询</span>
      <div class="header-actions" v-if="projectId">
        <el-button type="success" @click="calculateAll" :loading="calculating">
          <el-icon><Refresh /></el-icon>重新计算成绩
        </el-button>
        <el-button type="primary" @click="publishScores" :loading="publishing">
          <el-icon><Promotion /></el-icon>发布成绩
        </el-button>
      </div>
    </div>
    
    <div class="search-bar">
      <el-select v-model="projectId" placeholder="选择项目" style="width: 300px" @change="loadRanking">
        <el-option v-for="p in projects" :key="p.id" :label="p.projectName" :value="p.id" />
      </el-select>
      
      <el-select v-model="positionId" placeholder="筛选职位" clearable style="width: 200px" @change="loadRanking">
        <el-option v-for="pos in positions" :key="pos.id" :label="pos.positionName" :value="pos.id" />
      </el-select>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stat-cards" v-if="stats.count > 0">
      <div class="stat-card">
        <div class="stat-label">已评人数</div>
        <div class="stat-value">{{ stats.count }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">平均分</div>
        <div class="stat-value">{{ stats.avgScore }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">最高分</div>
        <div class="stat-value green">{{ stats.maxScore }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">最低分</div>
        <div class="stat-value red">{{ stats.minScore }}</div>
      </div>
    </div>
    
    <!-- 成绩排名表 -->
    <el-card v-if="projectId" class="ranking-card">
      <template #header>
        <span>成绩排名</span>
      </template>
      
      <el-table :data="rankingData" v-loading="loading" stripe>
        <el-table-column prop="overallRank" label="总排名" width="80">
          <template #default="{ row }">
            <span :class="getRankClass(row.overallRank)">
              {{ row.overallRank || '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="positionRank" label="职位排名" width="90">
          <template #default="{ row }">
            {{ row.positionRank || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="candidateName" label="考生姓名" width="100" />
        <el-table-column prop="positionName" label="报考职位" min-width="150" />
        <el-table-column prop="writtenScore" label="笔试成绩" width="100">
          <template #default="{ row }">
            {{ row.writtenScore?.toFixed(2) || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="interviewRawScore" label="面试原始分" width="110">
          <template #default="{ row }">
            {{ row.interviewRawScore?.toFixed(2) || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="interviewScore" label="面试成绩" width="100">
          <template #default="{ row }">
            {{ row.interviewScore?.toFixed(2) || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="综合成绩" width="100">
          <template #default="{ row }">
            <strong class="total-score">{{ row.totalScore?.toFixed(2) || '-' }}</strong>
          </template>
        </el-table-column>
        <el-table-column prop="publishStatus" label="发布状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.publishStatus === 1 ? 'success' : 'info'" size="small">
              {{ row.publishStatus === 1 ? '已发布' : '未发布' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <el-empty v-if="!projectId" description="请先选择项目" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()

const projectId = ref(route.query.projectId ? Number(route.query.projectId) : '')
const positionId = ref('')
const projects = ref([])
const positions = ref([])
const loading = ref(false)
const calculating = ref(false)
const publishing = ref(false)
const rankingData = ref([])
const stats = ref({
  count: 0,
  avgScore: '-',
  maxScore: '-',
  minScore: '-'
})

const loadProjects = async () => {
  try {
    const res = await api.projects.list({ pageNum: 1, pageSize: 100 })
    if (res.code === 200) {
      projects.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载项目失败', error)
  }
}

const loadPositions = async () => {
  if (!projectId.value) return
  try {
    const res = await api.positions.getByProject(projectId.value)
    if (res.code === 200) {
      positions.value = res.data || []
    }
  } catch (error) {
    console.error('加载职位失败', error)
  }
}

const loadRanking = async () => {
  if (!projectId.value) return
  
  loading.value = true
  
  // 加载职位信息（用于筛选下拉框）
  await loadPositions()
  
  try {
    const [rankingRes, statsRes] = await Promise.all([
      api.scores.getRanking({ projectId: projectId.value, positionId: positionId.value }),
      api.scores.getStatistics(projectId.value)
    ])
    
    if (rankingRes.code === 200) {
      rankingData.value = rankingRes.data || []
    }
    
    if (statsRes.code === 200) {
      stats.value = statsRes.data
    }
  } catch (error) {
    console.error('加载排名失败', error)
  }
  
  loading.value = false
}

const getRankClass = (rank) => {
  if (rank === 1) return 'rank-gold'
  if (rank === 2) return 'rank-silver'
  if (rank === 3) return 'rank-bronze'
  return ''
}

const calculateAll = async () => {
  try {
    await ElMessageBox.confirm('确定重新计算所有成绩吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    calculating.value = true
    const res = await api.scores.calculateAll(projectId.value)
    if (res.code === 200) {
      ElMessage.success(`成功计算 ${res.data} 人的成绩`)
      loadRanking()
    }
  } catch (e) {
    // 取消操作
  }
  calculating.value = false
}

const publishScores = async () => {
  try {
    await ElMessageBox.confirm('确定发布成绩吗？发布后考生可查看成绩。', '提示', {
      confirmButtonText: '确定发布',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    publishing.value = true
    const res = await api.scores.publish(projectId.value)
    if (res.code === 200) {
      ElMessage.success('成绩发布成功')
      loadRanking()
    }
  } catch (e) {
    // 取消操作
  }
  publishing.value = false
}

onMounted(() => {
  loadProjects()
  if (projectId.value) {
    loadRanking()
  }
})
</script>

<style lang="scss" scoped>
.results-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .header-actions {
      display: flex;
      gap: 12px;
    }
  }
  
  .stat-cards {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    margin-bottom: 20px;
    
    .stat-card {
      background: #fff;
      padding: 20px;
      border-radius: 12px;
      text-align: center;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      
      .stat-label {
        font-size: 14px;
        color: #909399;
        margin-bottom: 8px;
      }
      
      .stat-value {
        font-size: 28px;
        font-weight: 700;
        color: #303133;
        
        &.green {
          color: #67C23A;
        }
        
        &.red {
          color: #F56C6C;
        }
      }
    }
  }
  
  .ranking-card {
    .total-score {
      color: #409EFF;
      font-size: 16px;
    }
    
    .rank-gold {
      color: #FFD700;
      font-weight: bold;
      font-size: 18px;
    }
    
    .rank-silver {
      color: #C0C0C0;
      font-weight: bold;
      font-size: 16px;
    }
    
    .rank-bronze {
      color: #CD7F32;
      font-weight: bold;
      font-size: 16px;
    }
  }
}
</style>
