<template>
  <div class="scoring-page">
    <div class="page-header">
      <span class="title">现场评分</span>
      <el-tag v-if="scoreItems.length > 0" type="primary" size="small">多维度评分模式</el-tag>
      <el-tag v-else size="small">总分模式</el-tag>
    </div>

    <div class="search-bar">
      <el-select v-model="projectId" placeholder="选择项目" style="width: 300px" @change="onProjectChange">
        <el-option v-for="p in projects" :key="p.id" :label="p.projectName" :value="p.id" />
      </el-select>
    </div>

    <el-row :gutter="20" v-if="projectId">
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
            <template v-if="isMultiDimensionalMode">
              <div class="score-items-wrapper">
                <div 
                  v-for="(item, index) in scoreItems" 
                  :key="item.id" 
                  class="score-item-card"
                >
                  <div class="item-header">
                    <div class="item-title">
                      <span class="item-no">{{ index + 1 }}</span>
                      <span class="item-name">{{ item.itemName }}</span>
                      <el-tag size="small" type="warning">权重 {{ item.weight }}%</el-tag>
                      <el-tag size="small" type="info">满分 {{ item.maxScore }}</el-tag>
                    </div>
                    <el-button 
                      link 
                      size="small" 
                      type="primary"
                      @click="toggleDescription(item.id)"
                      v-if="item.description"
                    >
                      <el-icon><QuestionFilled /></el-icon>
                      评分标准
                    </el-button>
                  </div>

                  <div class="item-description" v-if="expandedDesc[item.id] && item.description">
                    <el-alert :title="item.description" type="info" :closable="false" show-icon />
                  </div>

                  <div class="item-score">
                    <el-slider
                      v-model="multiScoreForm[item.id]"
                      :min="0"
                      :max="Number(item.maxScore)"
                      :step="0.5"
                      show-input
                      :marks="getSliderMarks(item.maxScore)"
                    />
                    <div class="weighted-score">
                      <span>加权分：</span>
                      <strong class="weighted-value">
                        {{ calculateWeighted(item.id, item.weight, item.maxScore) }}
                      </strong>
                    </div>
                  </div>
                </div>
              </div>

              <div class="multi-score-summary">
                <div class="summary-card">
                  <div class="summary-row">
                    <span>原始分合计：</span>
                    <strong>{{ totalRawScore.toFixed(2) }}</strong>
                    <span class="divider">/</span>
                    <span>{{ totalMaxScore.toFixed(2) }}</span>
                  </div>
                  <div class="summary-row highlight">
                    <span>加权总分：</span>
                    <strong class="total-value">{{ totalWeightedScore.toFixed(2) }}</strong>
                  </div>
                </div>
              </div>
            </template>

            <template v-else>
              <el-form label-width="120px">
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
              </el-form>
            </template>

            <el-form label-width="120px" style="margin-top: 16px;">
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

          <div class="realtime-score" v-if="currentScores.length">
            <h4>当前评分情况</h4>

            <div class="multi-dim-table" v-if="isMultiDimensionalMode">
              <el-table :data="currentScoresWithDetails" stripe size="small" border>
                <el-table-column prop="examinerName" label="考官" width="100" fixed />
                <el-table-column 
                  v-for="item in scoreItems" 
                  :key="item.id" 
                  :label="item.itemName" 
                  min-width="110"
                >
                  <template #default="{ row }">
                    <span class="item-score-cell">
                      {{ row['item_' + item.id] != null ? row['item_' + item.id] : '-' }}
                      <span class="weighted-sub" v-if="row['weighted_' + item.id] != null">
                        ({{ row['weighted_' + item.id] }})
                      </span>
                    </span>
                  </template>
                </el-table-column>
                <el-table-column prop="totalScore" label="加权总分" width="100" fixed="right">
                  <template #default="{ row }">
                    <strong :class="{ 'text-primary': row.isValid, 'text-muted': !row.isValid }">
                      {{ row.totalScore != null ? Number(row.totalScore).toFixed(2) : '-' }}
                    </strong>
                  </template>
                </el-table-column>
                <el-table-column prop="isValid" label="状态" width="80" fixed="right">
                  <template #default="{ row }">
                    <el-tag size="small" :type="row.isValid ? 'success' : 'info'">
                      {{ row.isValid ? '有效' : '去除' }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <el-table v-else :data="currentScores" stripe size="small">
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
import { ref, reactive, onMounted, onUnmounted, computed, watch } from 'vue'
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

const scoreItems = ref([])
const isMultiDimensionalMode = computed(() => scoreItems.value.length > 0)
const multiScoreForm = reactive({})
const expandedDesc = reactive({})

const scoreForm = reactive({
  score: 75,
  comment: ''
})

const totalRawScore = computed(() => {
  if (!isMultiDimensionalMode.value) return 0
  return Object.values(multiScoreForm).reduce((acc, v) => acc + Number(v || 0), 0)
})

const totalMaxScore = computed(() => {
  return scoreItems.value.reduce((acc, item) => acc + Number(item.maxScore || 0), 0)
})

const totalWeightedScore = computed(() => {
  if (!isMultiDimensionalMode.value) return 0
  return scoreItems.value.reduce((acc, item) => {
    const raw = Number(multiScoreForm[item.id] || 0)
    const weight = Number(item.weight || 0)
    const max = Number(item.maxScore || 100)
    return acc + (raw * weight / 100)
  }, 0)
})

const calculateWeighted = (itemId, weight, maxScore) => {
  const raw = Number(multiScoreForm[itemId] || 0)
  return (raw * Number(weight) / 100).toFixed(2)
}

const getSliderMarks = (max) => {
  const marks = {}
  const step = Number(max) / 4
  for (let i = 0; i <= 4; i++) {
    marks[(i * step).toFixed(1)] = (i * step).toFixed(0)
  }
  return marks
}

const toggleDescription = (id) => {
  expandedDesc[id] = !expandedDesc[id]
}

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

const realtimeAvgScore = computed(() => {
  const validScores = currentScores.value.filter(s => s.isValid)
  if (!validScores.length) return '-'
  const sum = validScores.reduce((acc, s) => acc + Number(s.totalScore || 0), 0)
  return (sum / validScores.length).toFixed(2)
})

const currentScoresWithDetails = computed(() => {
  return currentScores.value.map(score => ({ ...score }))
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

const loadScoreItems = async () => {
  if (!projectId.value) return
  try {
    const res = await api.scoreItems.list(projectId.value)
    if (res.code === 200) {
      scoreItems.value = res.data || []
      scoreItems.value.forEach(item => {
        if (multiScoreForm[item.id] == null) {
          multiScoreForm[item.id] = Math.round(Number(item.maxScore) * 0.75 * 2) / 2
        }
      })
    }
  } catch (error) {
    console.error('加载评分项失败', error)
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

const onProjectChange = () => {
  scoreItems.value = []
  Object.keys(multiScoreForm).forEach(k => delete multiScoreForm[k])
  currentCandidate.value = null
  currentScores.value = []
  loadScoreItems()
  loadCandidates()
}

const loadCandidateScoreDetails = async () => {
  if (!currentCandidate.value || !isMultiDimensionalMode.value) return
  try {
    const res = await api.scores.getCandidateDetails(currentCandidate.value.id, projectId.value)
    if (res.code === 200) {
      const { examiners, summaryByExaminer, detailByExaminerItem } = res.data
      
      const detailsWithItems = currentScores.value.map(summary => {
        const row = { ...summary }
        scoreItems.value.forEach(item => {
          const key = summary.examinerId + '_' + item.id
          const detail = detailByExaminerItem?.[key]
          if (detail) {
            row['item_' + item.id] = detail.score
            row['weighted_' + item.id] = detail.weightedScore
          }
        })
        return row
      })
      currentScores.value = detailsWithItems
    }
  } catch (error) {
    console.error('加载评分明细失败', error)
  }
}

const selectCandidate = async (candidate) => {
  currentCandidate.value = candidate
  isInterviewing.value = candidate.interviewStatus === 1

  scoreItems.value.forEach(item => {
    multiScoreForm[item.id] = Math.round(Number(item.maxScore) * 0.75 * 2) / 2
  })
  scoreForm.score = 75
  scoreForm.comment = ''

  try {
    const res = await api.scores.getCandidateScores(candidate.id)
    if (res.code === 200) {
      currentScores.value = res.data || []
      if (isMultiDimensionalMode.value) {
        await loadCandidateScoreDetails()
      }
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

    timerInterval = setInterval(() => {
      timerSeconds.value++
    }, 1000)

    ElMessage.success('面试开始')
    loadCandidates()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const validateMultiScores = () => {
  for (const item of scoreItems.value) {
    const score = multiScoreForm[item.id]
    if (score == null || isNaN(score)) {
      ElMessage.warning(`请为「${item.itemName}」输入评分`)
      return false
    }
    if (score < 0 || score > Number(item.maxScore)) {
      ElMessage.warning(`「${item.itemName}」评分必须在 0 到 ${item.maxScore} 之间`)
      return false
    }
  }
  return true
}

const submitScore = async () => {
  submitting.value = true
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const examinerId = userInfo.id || 1

    if (isMultiDimensionalMode.value) {
      if (!validateMultiScores()) {
        submitting.value = false
        return
      }

      const itemScores = scoreItems.value.map(item => ({
        scoreItemId: item.id,
        score: Number(multiScoreForm[item.id])
      }))

      const res = await api.scores.submitMulti({
        projectId: projectId.value,
        candidateId: currentCandidate.value.id,
        examinerId: examinerId,
        comment: scoreForm.comment,
        itemScores
      })

      if (res.code === 200) {
        ElMessage.success('评分提交成功')
        await selectCandidate(currentCandidate.value)
      }
    } else {
      const res = await api.scores.submit({
        projectId: projectId.value,
        candidateId: currentCandidate.value.id,
        examinerId: examinerId,
        totalScore: scoreForm.score,
        comment: scoreForm.comment
      })

      if (res.code === 200) {
        ElMessage.success('评分提交成功')
        await selectCandidate(currentCandidate.value)
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  }
  submitting.value = false
}

const endInterview = async () => {
  try {
    await api.candidates.updateInterviewStatus(currentCandidate.value.id, 2)
    await api.scores.calculate(currentCandidate.value.id, projectId.value)

    isInterviewing.value = false

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

onMounted(async () => {
  await loadProjects()
  if (projectId.value) {
    await loadScoreItems()
    await loadCandidates()
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
    .page-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 20px;

      .title {
        font-size: 20px;
        font-weight: 600;
      }
    }

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

      .score-items-wrapper {
        display: flex;
        flex-direction: column;
        gap: 16px;
      }

      .score-item-card {
        background: #fafbfc;
        border: 1px solid #ebeef5;
        border-radius: 8px;
        padding: 16px 20px;

        .item-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 12px;

          .item-title {
            display: flex;
            align-items: center;
            gap: 10px;

            .item-no {
              display: inline-flex;
              align-items: center;
              justify-content: center;
              width: 28px;
              height: 28px;
              border-radius: 50%;
              background: #409EFF;
              color: #fff;
              font-size: 14px;
              font-weight: 600;
            }

            .item-name {
              font-size: 16px;
              font-weight: 600;
              color: #303133;
            }
          }
        }

        .item-description {
          margin-bottom: 12px;
        }

        .item-score {
          .weighted-score {
            margin-top: 8px;
            text-align: right;
            font-size: 13px;
            color: #606266;

            .weighted-value {
              color: #E6A23C;
              font-size: 16px;
            }
          }
        }
      }

      .multi-score-summary {
        margin-top: 20px;
        display: flex;
        justify-content: center;

        .summary-card {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: #fff;
          border-radius: 12px;
          padding: 20px 40px;
          min-width: 320px;
          box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);

          .summary-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 8px 0;
            font-size: 14px;
            opacity: 0.9;

            &.highlight {
              border-top: 1px solid rgba(255, 255, 255, 0.2);
              margin-top: 8px;
              padding-top: 16px;

              strong {
                font-size: 32px;
              }
            }

            .total-value {
              font-size: 24px;
              color: #FFD700;
            }

            .divider {
              opacity: 0.6;
              margin: 0 4px;
            }
          }
        }
      }

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

      .multi-dim-table {
        :deep(.el-table) {
          font-size: 12px;

          .item-score-cell {
            display: flex;
            flex-direction: column;
            align-items: center;

            .weighted-sub {
              font-size: 11px;
              color: #E6A23C;
            }
          }

          .text-primary {
            color: #409EFF;
          }

          .text-muted {
            color: #909399;
          }
        }
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
