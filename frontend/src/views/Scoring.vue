<template>
  <div class="scoring-page">
    <div class="page-header">
      <span class="title">现场评分</span>
    </div>
    
    <div class="search-bar">
      <el-select v-model="projectId" placeholder="选择项目" style="width: 300px" @change="loadCandidates">
        <el-option v-for="p in projects" :key="p.id" :label="p.projectName" :value="p.id" />
      </el-select>
    </div>
    
    <el-row :gutter="20" v-if="projectId">
      <!-- 待面试列表 -->
      <el-col :span="8">
        <el-card class="candidate-list">
          <template #header>
            <span>待面试考生</span>
          </template>
          
          <div v-loading="loadingCandidates">
            <div 
              v-for="c in waitingCandidates" 
              :key="c.id" 
              class="candidate-item"
              :class="{ active: currentCandidate?.id === c.id }"
              @click="selectCandidate(c)"
            >
              <div class="candidate-info">
                <span class="name">{{ c.candidateName }}</span>
                <span class="order">{{ c.interviewOrder ? '第' + c.interviewOrder + '号' : '未抽签' }}</span>
              </div>
              <el-tag size="small" :type="c.interviewStatus === 1 ? 'warning' : 'info'">
                {{ c.interviewStatus === 1 ? '面试中' : '待面试' }}
              </el-tag>
            </div>
            
            <el-empty v-if="!waitingCandidates.length" description="暂无待面试考生，请确保考生已签到" :image-size="80" />
          </div>
        </el-card>
      </el-col>
      
      <!-- 评分面板 -->
      <el-col :span="16">
        <el-card class="scoring-panel" v-if="currentCandidate">
          <template #header>
            <div class="panel-header">
              <div class="candidate-detail">
                <h3>{{ currentCandidate.candidateName }}</h3>
                <p>准考证号：{{ currentCandidate.ticketNo }} | 面试序号：第{{ currentCandidate.interviewOrder }}号</p>
              </div>
              <div class="timer" v-if="isInterviewing">
                <el-icon><Clock /></el-icon>
                <span>{{ formatTime(timerSeconds) }}</span>
              </div>
            </div>
          </template>
          
          <div class="scoring-form">
            <el-form :model="scoreForm" label-width="120px">
              <el-form-item label="面试评分">
                <el-slider 
                  v-model="scoreForm.score" 
                  :min="0" 
                  :max="100" 
                  :step="0.5"
                  show-input
                  show-stops
                />
              </el-form-item>
              
              <el-form-item label="评语">
                <el-input 
                  v-model="scoreForm.comment" 
                  type="textarea" 
                  :rows="4" 
                  placeholder="请输入评语（选填）"
                />
              </el-form-item>
            </el-form>
            
            <div class="action-buttons">
              <el-button 
                type="success" 
                size="large" 
                @click="startInterview" 
                v-if="!isInterviewing"
              >
                <el-icon><VideoPlay /></el-icon>开始面试
              </el-button>
              
              <el-button 
                type="primary" 
                size="large" 
                :loading="submitting" 
                @click="submitScore"
                v-if="isInterviewing"
              >
                <el-icon><Check /></el-icon>提交评分
              </el-button>
              
              <el-button 
                type="warning" 
                size="large" 
                @click="endInterview"
                v-if="isInterviewing"
              >
                <el-icon><Close /></el-icon>结束面试
              </el-button>
            </div>
          </div>
          
          <!-- 实时分数展示 -->
          <div class="realtime-score" v-if="currentScores.length">
            <h4>当前评分情况</h4>
            <el-table :data="currentScores" stripe size="small">
              <el-table-column prop="examinerName" label="考官" width="100" />
              <el-table-column prop="totalScore" label="评分" width="80" />
              <el-table-column prop="isValid" label="状态" width="80">
                <template #default="{ row }">
                  <el-tag size="small" :type="row.isValid ? 'success' : 'info'">
                    {{ row.isValid ? '有效' : '去除' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="submitTime" label="提交时间" />
            </el-table>
            
            <div class="score-summary">
              <span>平均分（去掉最高最低）：</span>
              <strong>{{ realtimeAvgScore }}</strong>
            </div>
          </div>
        </el-card>
        
        <el-empty v-else description="请从左侧选择考生" />
      </el-col>
    </el-row>
    
    <el-empty v-if="!projectId" description="请先选择项目" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api'
import { ElMessage } from 'element-plus'

const route = useRoute()

const projectId = ref(route.query.projectId ? Number(route.query.projectId) : '')
const projects = ref([])
const loadingCandidates = ref(false)
const submitting = ref(false)
const waitingCandidates = ref([])
const currentCandidate = ref(null)
const currentScores = ref([])
const isInterviewing = ref(false)
const timerSeconds = ref(0)
let timerInterval = null

const scoreForm = reactive({
  score: 75,
  comment: ''
})

const realtimeAvgScore = computed(() => {
  const validScores = currentScores.value.filter(s => s.isValid)
  if (!validScores.length) return '-'
  const sum = validScores.reduce((acc, s) => acc + Number(s.totalScore || 0), 0)
  return (sum / validScores.length).toFixed(2)
})

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

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

const loadCandidates = async () => {
  if (!projectId.value) return
  
  loadingCandidates.value = true
  try {
    const res = await api.candidates.getWaiting({ projectId: projectId.value })
    if (res.code === 200) {
      waitingCandidates.value = res.data || []
    }
  } catch (error) {
    console.error('加载考生失败', error)
  }
  loadingCandidates.value = false
}

const selectCandidate = async (candidate) => {
  currentCandidate.value = candidate
  isInterviewing.value = candidate.interviewStatus === 1
  
  // 加载当前考生的评分
  try {
    const res = await api.scores.getCandidateScores(candidate.id)
    if (res.code === 200) {
      currentScores.value = res.data || []
    }
  } catch (error) {
    console.error('加载评分失败', error)
  }
}

const startInterview = async () => {
  try {
    await api.candidates.updateInterviewStatus(currentCandidate.value.id, 1)
    isInterviewing.value = true
    timerSeconds.value = 0
    
    // 开始计时
    timerInterval = setInterval(() => {
      timerSeconds.value++
    }, 1000)
    
    ElMessage.success('面试开始')
    loadCandidates()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const submitScore = async () => {
  submitting.value = true
  try {
    // 从用户信息获取考官ID，如无则默认为1（管理员模式）
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const examinerId = userInfo.id || 1
    
    const res = await api.scores.submit({
      projectId: projectId.value,
      candidateId: currentCandidate.value.id,
      examinerId: examinerId,
      totalScore: scoreForm.score,
      comment: scoreForm.comment
    })
    
    if (res.code === 200) {
      ElMessage.success('评分提交成功')
      // 刷新评分列表
      selectCandidate(currentCandidate.value)
    }
  } catch (error) {
    ElMessage.error('提交失败')
  }
  submitting.value = false
}

const endInterview = async () => {
  try {
    await api.candidates.updateInterviewStatus(currentCandidate.value.id, 2)
    
    // 计算最终成绩
    await api.scores.calculate(currentCandidate.value.id, projectId.value)
    
    isInterviewing.value = false
    
    // 停止计时
    if (timerInterval) {
      clearInterval(timerInterval)
      timerInterval = null
    }
    
    ElMessage.success('面试结束，成绩已计算')
    loadCandidates()
    currentCandidate.value = null
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadProjects()
  if (projectId.value) {
    loadCandidates()
  }
})

onUnmounted(() => {
  if (timerInterval) {
    clearInterval(timerInterval)
  }
})
</script>

<style lang="scss" scoped>
.scoring-page {
  .candidate-list {
    height: calc(100vh - 200px);
    overflow-y: auto;
    
    .candidate-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      border-radius: 8px;
      margin-bottom: 8px;
      cursor: pointer;
      background: #f5f7fa;
      transition: all 0.3s;
      
      &:hover {
        background: #ecf5ff;
      }
      
      &.active {
        background: #409EFF;
        color: #fff;
        
        .order {
          color: rgba(255, 255, 255, 0.8);
        }
      }
      
      .candidate-info {
        .name {
          font-weight: 600;
          margin-right: 8px;
        }
        
        .order {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
  
  .scoring-panel {
    .panel-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .candidate-detail {
        h3 {
          font-size: 18px;
          margin-bottom: 4px;
        }
        
        p {
          font-size: 13px;
          color: #909399;
        }
      }
      
      .timer {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 24px;
        font-weight: 600;
        color: #409EFF;
      }
    }
    
    .scoring-form {
      padding: 20px 0;
      
      .action-buttons {
        display: flex;
        gap: 16px;
        justify-content: center;
        margin-top: 24px;
      }
    }
    
    .realtime-score {
      padding-top: 20px;
      border-top: 1px solid #ebeef5;
      
      h4 {
        margin-bottom: 16px;
        font-size: 16px;
      }
      
      .score-summary {
        margin-top: 16px;
        text-align: right;
        font-size: 16px;
        
        strong {
          font-size: 24px;
          color: #409EFF;
        }
      }
    }
  }
}
</style>
