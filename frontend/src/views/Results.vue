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
      <el-select v-model="projectId" placeholder="选择项目" style="width: 300px" @change="onProjectChange">
        <el-option v-for="p in projects" :key="p.id" :label="p.projectName" :value="p.id" />
      </el-select>
      
      <el-select v-model="positionId" placeholder="筛选职位" clearable style="width: 200px" @change="loadRanking">
        <el-option v-for="pos in positions" :key="pos.id" :label="pos.positionName" :value="pos.id" />
      </el-select>

      <el-tag v-if="isMultiDimensionalMode" type="success" effect="dark">
        <el-icon><Grid /></el-icon>&nbsp;多维度评分
      </el-tag>
      <el-tag v-else-if="projectId" type="info">
        传统总分模式
      </el-tag>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stat-cards" v-if="stats.count > 0">
      <div class="stat-card">
        <div class="stat-label">已评人数</div>
        <div class="stat-value">{{ stats.count }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">面试平均分</div>
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
    
    <el-card v-if="projectId" class="main-card">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <!-- Tab1: 成绩排名 -->
        <el-tab-pane label="成绩排名" name="ranking">
          <el-table 
            :data="rankingData" 
            v-loading="loading" 
            stripe
            row-key="id"
            @expand-change="onExpandChange"
          >
            <el-table-column type="expand" width="50">
              <template #default="{ row }">
                <div class="expand-panel" v-loading="row._detailLoading">
                  <template v-if="row._detailLoaded">
                    <!-- 多维度模式：评分项明细表 -->
                    <div v-if="isMultiDimensionalMode && row._scoreDetails">
                      <div class="expand-title">
                        <el-icon><DataLine /></el-icon>&nbsp;
                        <span>评分明细 - 各考官打分</span>
                        <span class="expand-subtitle">
                          (去除极值后面试分：<b class="hl">{{ row.interviewScore?.toFixed(2) }}</b>)
                        </span>
                      </div>

                      <div class="score-matrix-wrapper">
                        <el-table :data="row._scoreDetails.examiners" border size="small" show-summary>
                          <el-table-column label="考官" width="110" fixed>
                            <template #default="{ row: ex, $index }">
                              <div class="examiner-cell">
                                <span class="examiner-idx">{{ ex.orderIndex || ($index + 1) }}</span>
                                <span class="examiner-name">{{ ex.examinerName }}</span>
                                <el-tag v-if="ex._isExtremeMax" type="danger" size="small" effect="plain">最高</el-tag>
                                <el-tag v-if="ex._isExtremeMin" type="warning" size="small" effect="plain">最低</el-tag>
                              </div>
                            </template>
                          </el-table-column>
                          
                          <el-table-column 
                            v-for="item in row._scoreDetails.items" 
                            :key="item.id"
                            :label="`${item.itemName}\n(${item.weight}%)`"
                            align="center"
                            min-width="120"
                          >
                            <template #default="{ row: ex }">
                              <span class="raw-score">{{ getRawScore(ex, item.id) }}</span>
                              <span class="weighted">({{ getWeightedScore(ex, item.id) }})</span>
                            </template>
                            <template #footer>
                              <span class="avg-cell">
                                原始:{{ getItemAvgRaw(row._scoreDetails, item.id) }}
                                <br>加权:{{ getItemAvgWeighted(row._scoreDetails, item.id) }}
                              </span>
                            </template>
                          </el-table-column>

                          <el-table-column label="加权总分" width="110" align="center" fixed="right">
                            <template #default="{ row: ex }">
                              <b class="weighted-total">{{ getExaminerWeightedTotal(ex).toFixed(2) }}</b>
                            </template>
                            <template #footer>
                              <b class="hl">{{ row.interviewScore?.toFixed(2) || '-' }}</b>
                            </template>
                          </el-table-column>
                        </el-table>
                      </div>

                      <div class="weight-formula-card">
                        <div class="formula-title">
                          <el-icon><Calculator /></el-icon>&nbsp;
                          <span>加权计算过程</span>
                        </div>
                        <div class="formula-content">
                          <span v-for="(item, idx) in row._scoreDetails.items" :key="item.id">
                            <span class="formula-item">
                              {{ getItemAvgRaw(row._scoreDetails, item.id) }}
                              <span class="op">×</span>
                              {{ item.weight }}%
                            </span>
                            <span class="op" v-if="idx < row._scoreDetails.items.length - 1">+</span>
                          </span>
                          <span class="op">=</span>
                          <b class="result">{{ row.interviewScore?.toFixed(2) || '-' }}</b>
                        </div>
                      </div>
                    </div>

                    <!-- 传统模式：考官打分列表 -->
                    <div v-else>
                      <div class="expand-title">
                        <el-icon><DataLine /></el-icon>&nbsp;
                        <span>各考官打分记录</span>
                      </div>
                      <el-table :data="row._examinerScores" border size="small">
                        <el-table-column prop="orderIndex" label="序号" width="80" align="center">
                          <template #default="{ $index }">{{ $index + 1 }}</template>
                        </el-table-column>
                        <el-table-column prop="examinerName" label="考官" width="150" />
                        <el-table-column prop="totalScore" label="打分" width="120" align="center">
                          <template #default="{ row: s }">
                            <span :class="{
                              'extreme-max': s.isValid === 0 && s._isMax,
                              'extreme-min': s.isValid === 0 && s._isMin
                            }">
                              {{ s.totalScore?.toFixed(2) || '-' }}
                              <el-tag v-if="s.isValid === 0 && s._isMax" type="danger" size="small" effect="plain" style="margin-left:4px">已去</el-tag>
                              <el-tag v-if="s.isValid === 0 && s._isMin" type="warning" size="small" effect="plain" style="margin-left:4px">已去</el-tag>
                            </span>
                          </template>
                        </el-table-column>
                        <el-table-column prop="comment" label="评语" min-width="200" />
                        <el-table-column prop="scoreTime" label="时间" width="170" />
                      </el-table>
                    </div>

                    <div class="expand-actions" style="margin-top:12px;text-align:right">
                      <el-button size="small" type="primary" @click="openDetailDialog(row)">
                        <el-icon><View /></el-icon>&nbsp;查看完整详情
                      </el-button>
                    </div>
                  </template>
                </div>
              </template>
            </el-table-column>

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
            <el-table-column prop="candidateName" label="考生姓名" width="110">
              <template #default="{ row }">
                <el-button type="primary" link @click="openDetailDialog(row)">
                  {{ row.candidateName }}
                </el-button>
              </template>
            </el-table-column>
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
                <b style="color:#67C23A">{{ row.interviewScore?.toFixed(2) || '-' }}</b>
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
        </el-tab-pane>

        <!-- Tab2: 评分项统计 -->
        <el-tab-pane label="评分项统计" name="item-stats" v-if="isMultiDimensionalMode">
          <div v-loading="statsLoading">
            <div v-if="itemStatsList.length > 0">
              <!-- 各评分项统计卡片 -->
              <div class="item-stat-cards">
                <div v-for="(stat, idx) in itemStatsList" :key="stat.itemId" 
                     class="item-stat-card" 
                     :style="{ '--card-color': itemColors[idx % itemColors.length] }">
                  <div class="item-stat-header">
                    <span class="item-stat-name">{{ stat.itemName }}</span>
                    <el-tag size="small" type="info">权重 {{ stat.weight }}%</el-tag>
                  </div>
                  <div class="item-stat-body">
                    <div class="stat-row">
                      <span class="lbl">满分</span>
                      <span class="val">{{ stat.maxScore }}</span>
                    </div>
                    <div class="stat-row">
                      <span class="lbl">参评数</span>
                      <span class="val">{{ stat.count }}</span>
                    </div>
                    <div class="stat-row highlight">
                      <span class="lbl">平均分</span>
                      <span class="val avg">{{ stat.avgScore }}</span>
                    </div>
                    <div class="stat-row">
                      <span class="lbl">最高分</span>
                      <span class="val green">{{ stat.highest }}</span>
                    </div>
                    <div class="stat-row">
                      <span class="lbl">最低分</span>
                      <span class="val red">{{ stat.lowest }}</span>
                    </div>
                    <div class="stat-row">
                      <span class="lbl">标准差</span>
                      <span class="val">{{ stat.stdDev || '-' }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <el-row :gutter="16" style="margin-top:20px">
                <el-col :span="14">
                  <el-card shadow="never">
                    <template #header>
                      <div class="chart-header">
                        <span>各评分项 - 平均/最高/最低 对比图</span>
                      </div>
                    </template>
                    <div ref="barChartRef" style="width:100%;height:380px"></div>
                  </el-card>
                </el-col>
                <el-col :span="10">
                  <el-card shadow="never">
                    <template #header>
                      <div class="chart-header">
                        <span>分数分布直方图 - </span>
                        <el-select v-model="selectedItemId" size="small" style="width:200px" @change="renderHistogram">
                          <el-option 
                            v-for="s in itemStatsList" 
                            :key="s.itemId" 
                            :label="s.itemName" 
                            :value="s.itemId" 
                          />
                        </el-select>
                      </div>
                    </template>
                    <div ref="histogramChartRef" style="width:100%;height:380px"></div>
                  </el-card>
                </el-col>
              </el-row>

              <el-card shadow="never" style="margin-top:20px">
                <template #header>
                  <div class="chart-header">
                    <span>考生多维度对比 - 雷达图</span>
                    <div>
                      <el-select v-model="radarCandidates" multiple placeholder="选择考生（最多6人）" size="small" style="width:420px" @change="renderRadar">
                        <el-option 
                          v-for="c in rankingData.slice(0, 30)" 
                          :key="c.candidateId" 
                          :label="`${c.candidateName} (综合${c.totalScore?.toFixed(2) || '-'})`" 
                          :value="c.candidateId" 
                        />
                      </el-select>
                    </div>
                  </div>
                </template>
                <div ref="radarChartRef" style="width:100%;height:440px"></div>
              </el-card>
            </div>
            <el-empty v-else description="暂无评分项统计数据（可能尚未有评分数据）" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 考生详情弹窗 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      :title="`评分详情 - ${detailCandidate?.candidateName || ''}`"
      width="1100px"
      top="5vh"
      destroy-on-close
    >
      <div v-loading="detailLoading" class="detail-content">
        <div class="detail-basic">
          <div class="basic-row">
            <div class="basic-item"><span class="lbl">考生姓名：</span><span class="val">{{ detailCandidate?.candidateName }}</span></div>
            <div class="basic-item"><span class="lbl">报考职位：</span><span class="val">{{ detailCandidate?.positionName }}</span></div>
            <div class="basic-item"><span class="lbl">笔试成绩：</span><span class="val">{{ detailCandidate?.writtenScore?.toFixed(2) || '-' }}</span></div>
          </div>
          <div class="basic-row">
            <div class="basic-item highlight">
              <span class="lbl">面试成绩：</span>
              <span class="val green big">{{ detailCandidate?.interviewScore?.toFixed(2) || '-' }}</span>
            </div>
            <div class="basic-item highlight">
              <span class="lbl">综合成绩：</span>
              <span class="val blue big">{{ detailCandidate?.totalScore?.toFixed(2) || '-' }}</span>
            </div>
            <div class="basic-item"><span class="lbl">总排名：</span><span class="val">{{ detailCandidate?.overallRank || '-' }}</span></div>
          </div>
        </div>

        <template v-if="isMultiDimensionalMode && detailScoreItems.length > 0">
          <div class="detail-section-title">
            <el-icon><Grid /></el-icon>&nbsp;评分项明细
          </div>
          <el-table :data="detailScoreItems" border size="default">
            <el-table-column label="序号" type="index" width="60" align="center" />
            <el-table-column prop="itemName" label="评分项" width="150" />
            <el-table-column label="权重" width="90" align="center">
              <template #default="{ row }">{{ row.weight }}%</template>
            </el-table-column>
            <el-table-column label="满分" width="80" align="center">
              <template #default="{ row }">{{ row.maxScore }}</template>
            </el-table-column>
            <el-table-column label="加权满分" width="100" align="center">
              <template #default="{ row }">
                {{ ((row.maxScore * row.weight) / 100).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column label="考生原始平均" width="130" align="center">
              <template #default="{ row }">
                <b class="raw-score">{{ row._avgRaw || '-' }}</b>
              </template>
            </el-table-column>
            <el-table-column label="加权得分" width="120" align="center">
              <template #default="{ row }">
                <b class="weighted-score">{{ row._weighted || '-' }}</b>
              </template>
            </el-table-column>
            <el-table-column label="项目平均" width="110" align="center">
              <template #default="{ row }">
                {{ getItemProjectAvg(row.itemId) }}
              </template>
            </el-table-column>
          </el-table>

          <div class="detail-section-title">
            <el-icon><DataLine /></el-icon>&nbsp;考官 × 评分项 矩阵
          </div>
          <div class="score-matrix-wrapper">
            <el-table :data="detailExaminersScores" border show-summary>
              <el-table-column label="考官" width="130" fixed>
                <template #default="{ row: ex, $index }">
                  <span class="examiner-idx">{{ $index + 1 }}.</span>
                  <span class="examiner-name">{{ ex.examinerName }}</span>
                  <el-tag v-if="ex._isExtremeMax" type="danger" size="small" effect="plain" style="margin-left:4px">最高</el-tag>
                  <el-tag v-if="ex._isExtremeMin" type="warning" size="small" effect="plain" style="margin-left:4px">最低</el-tag>
                </template>
              </el-table-column>
              <el-table-column 
                v-for="item in detailScoreItems" 
                :key="item.itemId"
                :label="item.itemName"
                align="center"
                min-width="110"
              >
                <template #default="{ row: ex }">
                  <span class="raw-score">{{ getRawScoreFromMatrix(ex, item.itemId) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="加权总分" width="110" align="center" fixed="right">
                <template #default="{ row: ex }">
                  <b>{{ (ex._weightedTotal || 0).toFixed(2) }}</b>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="weight-formula-card" style="margin-top:16px">
            <div class="formula-title">
              <el-icon><Calculator /></el-icon>&nbsp;
              <span>加权计算过程（去除极值后）</span>
            </div>
            <div class="formula-content">
              <span v-for="(item, idx) in detailScoreItems" :key="item.itemId">
                <span class="formula-item">
                  {{ item._avgRaw || '-' }}
                  <span class="op">×</span>
                  {{ item.weight }}%
                </span>
                <span class="op" v-if="idx < detailScoreItems.length - 1">+</span>
              </span>
              <span class="op">=</span>
              <b class="result">{{ detailCandidate?.interviewScore?.toFixed(2) || '-' }}</b>
            </div>
          </div>

          <el-row :gutter="16" style="margin-top:20px">
            <el-col :span="12">
              <el-card shadow="never">
                <template #header>
                  <span>考生能力雷达图</span>
                </template>
                <div ref="detailRadarRef" style="width:100%;height:360px"></div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card shadow="never">
                <template #header>
                  <span>各评分项得分对比</span>
                </template>
                <div ref="detailBarRef" style="width:100%;height:360px"></div>
              </el-card>
            </el-col>
          </el-row>
        </template>

        <template v-else>
          <div class="detail-section-title">
            <el-icon><List /></el-icon>&nbsp;考官评分记录
          </div>
          <el-table :data="detailExaminerList" border>
            <el-table-column label="序号" type="index" width="60" align="center" />
            <el-table-column prop="examinerName" label="考官" width="160" />
            <el-table-column prop="totalScore" label="打分" width="130" align="center">
              <template #default="{ row }">
                <b :class="{ 'extreme-max': row._tagMax, 'extreme-min': row._tagMin }">
                  {{ row.totalScore?.toFixed(2) || '-' }}
                </b>
                <el-tag v-if="row._tagMax" type="danger" size="small" effect="plain" style="margin-left:4px">最高</el-tag>
                <el-tag v-if="row._tagMin" type="warning" size="small" effect="plain" style="margin-left:4px">最低</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="comment" label="考官评语" min-width="250" />
            <el-table-column prop="scoreTime" label="评分时间" width="180" />
          </el-table>
        </template>
      </div>
    </el-dialog>
    
    <el-empty v-if="!projectId" description="请先选择项目" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'

const route = useRoute()

const projectId = ref(route.query.projectId ? Number(route.query.projectId) : '')
const positionId = ref('')
const projects = ref([])
const positions = ref([])
const loading = ref(false)
const calculating = ref(false)
const publishing = ref(false)
const rankingData = ref([])
const scoreItems = ref([])
const stats = ref({
  count: 0,
  avgScore: '-',
  maxScore: '-',
  minScore: '-'
})

const activeTab = ref('ranking')
const isMultiDimensionalMode = computed(() => scoreItems.value.length > 0)

// 评分项统计
const statsLoading = ref(false)
const itemStatsList = ref([])
const selectedItemId = ref(null)

// 图表
const barChartRef = ref(null)
const histogramChartRef = ref(null)
const radarChartRef = ref(null)
let barChartInstance = null
let histogramChartInstance = null
let radarChartInstance = null
const radarCandidates = ref([])
const itemColors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#8e44ad', '#16a085', '#d35400']

// 详情弹窗
const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const detailCandidate = ref(null)
const detailScoreItems = ref([])
const detailExaminersScores = ref([])
const detailExaminerList = ref([])
const detailRadarRef = ref(null)
const detailBarRef = ref(null)
let detailRadarInstance = null
let detailBarInstance = null

// 加载项目
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

// 加载职位
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

// 加载评分项
const loadScoreItems = async () => {
  if (!projectId.value) {
    scoreItems.value = []
    return
  }
  try {
    const res = await api.scoreItems.list(projectId.value)
    if (res.code === 200) {
      scoreItems.value = res.data || []
    }
  } catch (e) {
    console.error('加载评分项失败', e)
  }
}

// 加载评分项统计
const loadItemStatistics = async () => {
  if (!projectId.value || !isMultiDimensionalMode.value) return
  statsLoading.value = true
  try {
    const res = await api.scoreItems.getStatistics(projectId.value)
    if (res.code === 200) {
      itemStatsList.value = res.data || []
      if (itemStatsList.value.length > 0 && !selectedItemId.value) {
        selectedItemId.value = itemStatsList.value[0].itemId
      }
      await nextTick()
      if (barChartRef.value) renderBarChart()
      if (histogramChartRef.value) renderHistogram()
    }
  } catch (e) {
    console.error('加载评分项统计失败', e)
  }
  statsLoading.value = false
}

const loadRanking = async () => {
  if (!projectId.value) return
  
  loading.value = true
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

const onProjectChange = async () => {
  rankingData.value = []
  itemStatsList.value = []
  selectedItemId.value = null
  radarCandidates.value = []
  await loadScoreItems()
  loadRanking()
}

const onTabChange = async (tab) => {
  if (tab === 'item-stats') {
    await loadItemStatistics()
  }
}

// 表格行展开时加载评分明细
const onExpandChange = async (row, expandedRows) => {
  if (expandedRows.includes(row) && !row._detailLoaded) {
    row._detailLoading = true
    try {
      if (isMultiDimensionalMode.value) {
        const res = await api.scores.getCandidateDetails(row.candidateId, projectId.value)
        if (res.code === 200) {
          row._scoreDetails = processDetailData(res.data)
        }
      } else {
        const res = await api.scores.getCandidateScores(row.candidateId)
        if (res.code === 200) {
          row._examinerScores = processTraditionalScores(res.data || [])
        }
      }
      row._detailLoaded = true
    } catch (e) {
      console.error('加载明细失败', e)
    }
    row._detailLoading = false
  }
}

// 处理多维度明细数据：标记极值考官
const processDetailData = (data) => {
  if (!data) return data
  const examiners = (data.examiners || []).map(e => {
    const wt = getExaminerWeightedTotal(e)
    e._weightedTotal = wt
    return e
  })
  if (examiners.length >= 3) {
    const totals = examiners.map(e => e._weightedTotal)
    const maxT = Math.max(...totals)
    const minT = Math.min(...totals)
    examiners.forEach(e => {
      if (e._weightedTotal === maxT && totals.filter(t => t === maxT).length === 1) e._isExtremeMax = true
      if (e._weightedTotal === minT && totals.filter(t => t === minT).length === 1) e._isExtremeMin = true
    })
  }
  data.examiners = examiners
  return data
}

// 处理传统模式评分数据：标记极值
const processTraditionalScores = (scores) => {
  if (!scores || scores.length === 0) return []
  const valid = scores.filter(s => s.isValid === 1 || s.isValid == null)
  if (valid.length >= 3) {
    const totals = valid.map(s => Number(s.totalScore || 0))
    const maxT = Math.max(...totals)
    const minT = Math.min(...totals)
    const maxCount = totals.filter(t => t === maxT).length
    const minCount = totals.filter(t => t === minT).length
    scores.forEach(s => {
      if (s.isValid === 0) {
        if (Number(s.totalScore) === maxT && maxCount === 1) s._isMax = true
        else if (Number(s.totalScore) === minT && minCount === 1) s._isMin = true
      }
    })
  }
  return scores
}

// 工具函数：从考官对象取某个评分项的原始分
const getRawScore = (examiner, itemId) => {
  const entry = (examiner.itemScores || []).find(s => String(s.scoreItemId) === String(itemId))
  return entry ? Number(entry.score).toFixed(2) : '-'
}

const getRawScoreFromMatrix = (examiner, itemId) => {
  const entry = (examiner.itemScores || []).find(s => String(s.scoreItemId) === String(itemId))
  return entry ? Number(entry.score).toFixed(2) : '-'
}

const getWeightedScore = (examiner, itemId) => {
  const entry = (examiner.itemScores || []).find(s => String(s.scoreItemId) === String(itemId))
  return entry ? Number(entry.weightedScore || 0).toFixed(2) : '-'
}

// 考官加权总分（从明细条目）
const getExaminerWeightedTotal = (examiner) => {
  return (examiner.itemScores || []).reduce((acc, s) => acc + Number(s.weightedScore || 0), 0)
}

// 取某个评分项的平均原始分（去极值）
const getItemAvgRaw = (details, itemId) => {
  const validExaminers = (details.examiners || []).filter(e => !e._isExtremeMax && !e._isExtremeMin)
  if (validExaminers.length === 0) return '-'
  let sum = 0, cnt = 0
  validExaminers.forEach(e => {
    const entry = (e.itemScores || []).find(s => String(s.scoreItemId) === String(itemId))
    if (entry) {
      sum += Number(entry.score || 0)
      cnt++
    }
  })
  return cnt > 0 ? (sum / cnt).toFixed(2) : '-'
}

const getItemAvgWeighted = (details, itemId) => {
  const validExaminers = (details.examiners || []).filter(e => !e._isExtremeMax && !e._isExtremeMin)
  if (validExaminers.length === 0) return '-'
  let sum = 0, cnt = 0
  validExaminers.forEach(e => {
    const entry = (e.itemScores || []).find(s => String(s.scoreItemId) === String(itemId))
    if (entry) {
      sum += Number(entry.weightedScore || 0)
      cnt++
    }
  })
  return cnt > 0 ? (sum / cnt).toFixed(2) : '-'
}

const getItemProjectAvg = (itemId) => {
  const stat = itemStatsList.value.find(s => String(s.itemId) === String(itemId))
  return stat ? stat.avgScore : '-'
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
      if (activeTab.value === 'item-stats') loadItemStatistics()
    }
  } catch (e) {}
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
  } catch (e) {}
  publishing.value = false
}

// 打开详情弹窗
const openDetailDialog = async (row) => {
  detailCandidate.value = row
  detailDialogVisible.value = true
  detailLoading.value = true
  detailScoreItems.value = []
  detailExaminersScores.value = []
  detailExaminerList.value = []
  radarCandidates.value = []

  try {
    if (isMultiDimensionalMode.value) {
      const res = await api.scores.getCandidateDetails(row.candidateId, projectId.value)
      if (res.code === 200) {
        const data = processDetailData(res.data) || {}
        detailScoreItems.value = (data.items || []).map(it => ({
          itemId: it.id,
          itemName: it.itemName,
          weight: it.weight,
          maxScore: it.maxScore,
          _avgRaw: null,
          _weighted: null
        }))
        detailExaminersScores.value = data.examiners || []

        const validExs = detailExaminersScores.value.filter(e => !e._isExtremeMax && !e._isExtremeMin)
        detailScoreItems.value.forEach(item => {
          let sum = 0, cnt = 0
          validExs.forEach(e => {
            const entry = (e.itemScores || []).find(s => String(s.scoreItemId) === String(item.itemId))
            if (entry) {
              sum += Number(entry.score || 0)
              cnt++
            }
          })
          if (cnt > 0) {
            const avg = sum / cnt
            item._avgRaw = avg.toFixed(2)
            item._weighted = ((avg * item.weight) / 100).toFixed(2)
          }
        })
      }
    } else {
      const res = await api.scores.getCandidateScores(row.candidateId)
      if (res.code === 200) {
        const list = processTraditionalScores(res.data || [])
        detailExaminerList.value = list.map(s => {
          const obj = { ...s }
          if (s._isMax) obj._tagMax = true
          if (s._isMin) obj._tagMin = true
          if (s.isValid === 0 && s._isMax == null && s._isMin == null) {
            // 可能是其他去极值规则，简单标记
          }
          return obj
        })
      }
    }
  } catch (e) {
    console.error(e)
  }

  detailLoading.value = false

  if (isMultiDimensionalMode.value && detailScoreItems.value.length > 0) {
    await nextTick()
    renderDetailRadar()
    renderDetailBar()
  }
}

// ===== 图表相关 =====

const renderBarChart = () => {
  if (!barChartRef.value || itemStatsList.value.length === 0) return
  if (!barChartInstance) barChartInstance = echarts.init(barChartRef.value)
  const items = itemStatsList.value
  const option = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['平均分', '最高分', '最低分'], top: 0 },
    grid: { left: 50, right: 30, top: 50, bottom: 60 },
    xAxis: {
      type: 'category',
      data: items.map(i => i.itemName),
      axisLabel: { rotate: 25, interval: 0, fontSize: 12 }
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '平均分',
        type: 'bar',
        data: items.map(i => Number(i.avgScore || 0)),
        itemStyle: { color: '#409EFF' },
        label: { show: true, position: 'top', formatter: '{c}' }
      },
      {
        name: '最高分',
        type: 'bar',
        data: items.map(i => Number(i.highest || 0)),
        itemStyle: { color: '#67C23A' }
      },
      {
        name: '最低分',
        type: 'bar',
        data: items.map(i => Number(i.lowest || 0)),
        itemStyle: { color: '#F56C6C' }
      }
    ]
  }
  barChartInstance.setOption(option, true)
}

const renderHistogram = () => {
  if (!histogramChartRef.value) return
  if (!histogramChartInstance) histogramChartInstance = echarts.init(histogramChartRef.value)
  const stat = itemStatsList.value.find(s => String(s.itemId) === String(selectedItemId.value))
  const distribution = stat?.distribution || []

  const categories = distribution.length > 0 
    ? distribution.map(d => d.range || `${d.start}-${d.end}`)
    : ['0-10','10-20','20-30','30-40','40-50','50-60','60-70','70-80','80-90','90-100']
  const values = distribution.length > 0 
    ? distribution.map(d => d.count || 0)
    : new Array(10).fill(0)
  const maxVal = Math.max(...values, 1)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        const p = params[0]
        const percent = ((p.value / (stat?.count || maxVal)) * 100).toFixed(1)
        return `${p.name}<br/>人数：${p.value} 人<br/>占比：${percent}%`
      }
    },
    grid: { left: 50, right: 20, top: 30, bottom: 40 },
    xAxis: {
      type: 'category',
      data: categories,
      axisLabel: { fontSize: 11 }
    },
    yAxis: { type: 'value', minInterval: 1, name: '人数' },
    series: [{
      type: 'bar',
      data: values,
      itemStyle: {
        color: (params) => {
          const colors = ['#67C23A', '#85CE61', '#A4DA82', '#C4E6A3', '#E6A23C', '#EBBA6A', '#F0D298', '#F56C6C', '#F28D8D', '#EFADAD']
          return colors[params.dataIndex] || '#409EFF'
        },
        borderRadius: [4, 4, 0, 0]
      },
      label: { show: true, position: 'top', formatter: (p) => p.value > 0 ? p.value : '' }
    }]
  }
  histogramChartInstance.setOption(option, true)
}

const renderRadar = async () => {
  if (!radarChartRef.value) return
  if (!radarChartInstance) radarChartInstance = echarts.init(radarChartRef.value)
  if (scoreItems.value.length === 0 || radarCandidates.value.length === 0) {
    radarChartInstance.setOption({ title: { text: '请在上方选择考生进行对比', left: 'center', top: 'center', textStyle: { color: '#909399', fontSize: 16 } } }, true)
    return
  }

  const indicator = scoreItems.value.map(it => ({
    name: `${it.itemName}\n(${it.maxScore}分)`,
    max: it.maxScore
  }))

  const seriesData = []
  for (let i = 0; i < Math.min(radarCandidates.value.length, 6); i++) {
    const cid = radarCandidates.value[i]
    const row = rankingData.value.find(r => r.candidateId === cid)
    try {
      const res = await api.scoreItems.getCandidateScores(cid, projectId.value)
      if (res.code === 200 && res.data) {
        const values = scoreItems.value.map(it => {
          const entry = (res.data || []).find(s => String(s.scoreItemId) === String(it.id))
          return entry ? Number(entry.avgScore || 0) : 0
        })
        seriesData.push({
          value: values,
          name: row?.candidateName || `考生${cid}`,
          itemStyle: { color: itemColors[i % itemColors.length] },
          areaStyle: { opacity: 0.1 }
        })
      }
    } catch (e) {}
  }

  const option = {
    tooltip: {},
    legend: { data: seriesData.map(d => d.name), top: 0 },
    radar: {
      indicator: indicator,
      center: ['50%', '55%'],
      radius: '65%',
      shape: 'polygon',
      splitNumber: 5,
      axisName: { color: '#333', fontSize: 12 }
    },
    series: [{
      type: 'radar',
      data: seriesData
    }]
  }
  radarChartInstance.setOption(option, true)
}

const renderDetailRadar = () => {
  if (!detailRadarRef.value || detailScoreItems.value.length === 0) return
  if (!detailRadarInstance) detailRadarInstance = echarts.init(detailRadarRef.value)
  const option = {
    tooltip: {},
    radar: {
      indicator: detailScoreItems.value.map(it => ({
        name: `${it.itemName}(${it.maxScore})`,
        max: it.maxScore
      })),
      center: ['50%', '55%'],
      radius: '68%'
    },
    series: [{
      type: 'radar',
      data: [{
        value: detailScoreItems.value.map(it => Number(it._avgRaw || 0)),
        name: detailCandidate.value?.candidateName,
        itemStyle: { color: '#409EFF' },
        areaStyle: { opacity: 0.25, color: '#409EFF' },
        label: { show: true, formatter: (p) => p.value }
      }]
    }]
  }
  detailRadarInstance.setOption(option, true)
}

const renderDetailBar = () => {
  if (!detailBarRef.value || detailScoreItems.value.length === 0) return
  if (!detailBarInstance) detailBarInstance = echarts.init(detailBarRef.value)
  const option = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['考生平均', '项目平均'], top: 0 },
    grid: { left: 50, right: 20, top: 40, bottom: 60 },
    xAxis: {
      type: 'category',
      data: detailScoreItems.value.map(i => i.itemName),
      axisLabel: { rotate: 20, fontSize: 11 }
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '考生平均',
        type: 'bar',
        data: detailScoreItems.value.map(i => Number(i._avgRaw || 0)),
        itemStyle: { color: '#409EFF' },
        label: { show: true, position: 'top' }
      },
      {
        name: '项目平均',
        type: 'bar',
        data: detailScoreItems.value.map(i => Number(getItemProjectAvg(i.itemId) || 0)),
        itemStyle: { color: '#909399' }
      }
    ]
  }
  detailBarInstance.setOption(option, true)
}

// Resize handler
const handleResize = () => {
  barChartInstance?.resize()
  histogramChartInstance?.resize()
  radarChartInstance?.resize()
  detailRadarInstance?.resize()
  detailBarInstance?.resize()
}

window.addEventListener('resize', handleResize)

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  barChartInstance?.dispose()
  histogramChartInstance?.dispose()
  radarChartInstance?.dispose()
  detailRadarInstance?.dispose()
  detailBarInstance?.dispose()
})

onMounted(async () => {
  await loadProjects()
  if (projectId.value) {
    await loadScoreItems()
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

  .search-bar {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 20px;
    flex-wrap: wrap;
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
        
        &.green { color: #67C23A; }
        &.red { color: #F56C6C; }
      }
    }
  }
  
  .main-card {
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

  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
  }

  /* 展开面板样式 */
  .expand-panel {
    padding: 12px 20px 8px;
    background: #fafbfc;
    border-radius: 8px;
  }
  .expand-title {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 12px;
    display: flex;
    align-items: center;
    gap: 6px;
    .expand-subtitle {
      margin-left: 12px;
      font-size: 13px;
      font-weight: 400;
      color: #606266;
      .hl { color: #67C23A; }
    }
  }

  .score-matrix-wrapper {
    max-width: 100%;
    overflow-x: auto;
    :deep(.el-table) { width: max-content; min-width: 100%; }
  }

  .examiner-cell {
    display: flex;
    align-items: center;
    gap: 6px;
    flex-wrap: wrap;
    .examiner-idx {
      color: #909399;
      font-size: 12px;
    }
    .examiner-name { font-weight: 500; }
  }
  .raw-score {
    font-weight: 600;
    color: #303133;
  }
  .weighted {
    color: #67C23A;
    font-size: 12px;
    margin-left: 4px;
  }
  .weighted-total {
    color: #409EFF;
    font-size: 14px;
  }
  .avg-cell {
    font-size: 12px;
    line-height: 1.4;
  }
  .hl { color: #67C23A; font-size: 15px; }

  .weight-formula-card {
    margin-top: 14px;
    padding: 14px 18px;
    background: linear-gradient(135deg, #ecf5ff 0%, #f0f9eb 100%);
    border-radius: 8px;
    border: 1px dashed #b3d8ff;
    .formula-title {
      font-weight: 600;
      color: #409EFF;
      margin-bottom: 10px;
      display: flex;
      align-items: center;
      gap: 4px;
    }
    .formula-content {
      font-size: 14px;
      line-height: 2;
      font-family: Consolas, monospace;
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 4px 2px;
      .formula-item {
        padding: 2px 6px;
        background: #fff;
        border-radius: 4px;
        border: 1px solid #e4e7ed;
        color: #303133;
      }
      .op { color: #909399; margin: 0 4px; }
      .result {
        color: #67C23A;
        font-size: 18px;
        padding: 2px 10px;
        background: #fff;
        border-radius: 4px;
        border: 1px solid #b3e19d;
      }
    }
  }

  .extreme-max { color: #F56C6C; text-decoration: line-through; }
  .extreme-min { color: #E6A23C; text-decoration: line-through; }

  /* 评分项统计卡片 */
  .item-stat-cards {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 14px;
  }
  .item-stat-card {
    border-radius: 10px;
    background: #fff;
    box-shadow: 0 2px 10px rgba(0,0,0,0.06);
    overflow: hidden;
    border-top: 4px solid var(--card-color, #409EFF);
    .item-stat-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px 14px;
      background: rgba(64,158,255,0.04);
      .item-stat-name {
        font-weight: 600;
        font-size: 14px;
        color: var(--card-color, #409EFF);
      }
    }
    .item-stat-body {
      padding: 10px 14px 14px;
      .stat-row {
        display: flex;
        justify-content: space-between;
        padding: 4px 0;
        font-size: 13px;
        border-bottom: 1px dashed #f0f2f5;
        .lbl { color: #909399; }
        .val {
          font-weight: 600;
          color: #303133;
          &.green { color: #67C23A; }
          &.red { color: #F56C6C; }
          &.avg { color: var(--card-color, #409EFF); font-size: 15px; }
        }
        &.highlight {
          background: #f9fbff;
          margin: 4px -14px;
          padding: 8px 14px;
          border-radius: 4px;
        }
      }
      .stat-row:last-child { border-bottom: none; }
    }
  }

  /* 详情弹窗 */
  .detail-content {
    .detail-basic {
      background: #f5f7fa;
      border-radius: 8px;
      padding: 16px 20px;
      margin-bottom: 20px;
      .basic-row {
        display: flex;
        gap: 40px;
        margin-bottom: 8px;
        &:last-child { margin-bottom: 0; }
        .basic-item {
          display: flex;
          align-items: center;
          gap: 6px;
          .lbl { color: #909399; font-size: 13px; }
          .val { color: #303133; font-weight: 500; }
          &.highlight {
            .val.green { color: #67C23A; }
            .val.blue { color: #409EFF; }
            .val.big { font-size: 22px; font-weight: 700; }
          }
        }
      }
    }
    .detail-section-title {
      font-size: 15px;
      font-weight: 600;
      color: #303133;
      margin: 18px 0 12px;
      display: flex;
      align-items: center;
      gap: 6px;
      padding-left: 8px;
      border-left: 3px solid #409EFF;
    }
    .weighted-score {
      color: #67C23A;
      font-size: 15px;
    }
  }
}
</style>
