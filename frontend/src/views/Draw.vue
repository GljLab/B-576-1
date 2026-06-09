<template>
  <div class="draw-page">
    <div class="page-header">
      <span class="title">抽签管理</span>
    </div>
    
    <div class="search-bar">
      <el-select v-model="projectId" placeholder="选择项目" style="width: 300px" @change="loadDrawResults">
        <el-option v-for="p in projects" :key="p.id" :label="p.projectName" :value="p.id" />
      </el-select>
    </div>
    
    <!-- 三盲抽签操作 -->
    <el-card class="draw-card" v-if="projectId">
      <template #header>
        <div class="card-header">
          <span>三盲抽签</span>
          <el-button type="primary" :loading="drawing" @click="executeTripleBlind">
            <el-icon><Ticket /></el-icon>执行三盲抽签
          </el-button>
        </div>
      </template>
      
      <div class="draw-intro">
        <p>三盲抽签包含以下三个环节：</p>
        <ul>
          <li><strong>考官抽签：</strong>随机分配考官到各考场，并确定座位号</li>
          <li><strong>考生抽签：</strong>随机确定考生的面试顺序</li>
          <li><strong>职位顺序抽签：</strong>随机确定职位的面试顺序</li>
        </ul>
        <el-alert 
          title="注意：抽签结果一经确定，不可更改。请确保所有考生、考官、考场信息已录入完成后再执行抽签。" 
          type="warning" 
          show-icon 
          :closable="false"
        />
      </div>
    </el-card>
    
    <!-- 单独抽签 -->
    <el-row :gutter="20" v-if="projectId" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card class="single-draw-card">
          <template #header>
            <div class="card-header">
              <span>考官抽签</span>
              <el-button size="small" :loading="drawingExaminer" @click="drawExaminers">
                执行
              </el-button>
            </div>
          </template>
          <p class="draw-desc">将考官随机分配到各考场</p>
          <div class="draw-count">
            已抽签: {{ examinerRecords.length }} 人
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="single-draw-card">
          <template #header>
            <div class="card-header">
              <span>考生抽签</span>
              <el-button size="small" :loading="drawingCandidate" @click="drawCandidates">
                执行
              </el-button>
            </div>
          </template>
          <p class="draw-desc">随机确定考生面试顺序</p>
          <div class="draw-count">
            已抽签: {{ candidateRecords.length }} 人
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="single-draw-card">
          <template #header>
            <div class="card-header">
              <span>职位顺序抽签</span>
              <el-button size="small" :loading="drawingPosition" @click="drawPositions">
                执行
              </el-button>
            </div>
          </template>
          <p class="draw-desc">随机确定职位面试顺序</p>
          <div class="draw-count">
            已抽签: {{ positionRecords.length }} 个
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 抽签结果 -->
    <el-tabs v-model="activeTab" class="result-tabs" v-if="projectId" style="margin-top: 20px;">
      <el-tab-pane label="考官抽签结果" name="examiner">
        <el-table :data="examinerRecords" stripe v-loading="loading">
          <el-table-column prop="targetName" label="考官姓名" width="120" />
          <el-table-column prop="originalInfo" label="原始信息" min-width="150" />
          <el-table-column prop="resultInfo" label="抽签结果" min-width="200" />
          <el-table-column prop="drawTime" label="抽签时间" width="180" />
          <el-table-column prop="operatorName" label="操作人" width="100" />
          <template #empty>
            <el-empty description="暂无考官抽签记录，请先执行抽签" :image-size="100" />
          </template>
        </el-table>
      </el-tab-pane>
      
      <el-tab-pane label="考生抽签结果" name="candidate">
        <el-table :data="candidateRecords" stripe v-loading="loading">
          <el-table-column prop="targetName" label="考生姓名" width="120" />
          <el-table-column prop="originalInfo" label="原始信息" min-width="150" />
          <el-table-column prop="resultInfo" label="面试顺序" min-width="150" />
          <el-table-column prop="drawTime" label="抽签时间" width="180" />
          <el-table-column prop="operatorName" label="操作人" width="100" />
          <template #empty>
            <el-empty description="暂无考生抽签记录，请确保考生已签到后再执行抽签" :image-size="100" />
          </template>
        </el-table>
      </el-tab-pane>
      
      <el-tab-pane label="职位顺序结果" name="position">
        <el-table :data="positionRecords" stripe v-loading="loading">
          <el-table-column prop="targetName" label="职位名称" min-width="200" />
          <el-table-column prop="originalInfo" label="原始信息" min-width="150" />
          <el-table-column prop="resultInfo" label="面试顺序" min-width="150" />
          <el-table-column prop="drawTime" label="抽签时间" width="180" />
          <el-table-column prop="operatorName" label="操作人" width="100" />
          <template #empty>
            <el-empty description="暂无职位顺序抽签记录，请先执行抽签" :image-size="100" />
          </template>
        </el-table>
      </el-tab-pane>
    </el-tabs>
    
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
const projects = ref([])
const loading = ref(false)
const drawing = ref(false)
const drawingExaminer = ref(false)
const drawingCandidate = ref(false)
const drawingPosition = ref(false)
const activeTab = ref('examiner')

const examinerRecords = ref([])
const candidateRecords = ref([])
const positionRecords = ref([])

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

const loadDrawResults = async () => {
  if (!projectId.value) return
  
  loading.value = true
  try {
    const [examinerRes, candidateRes, positionRes] = await Promise.all([
      api.draw.getResults({ projectId: projectId.value, drawType: 'EXAMINER' }),
      api.draw.getResults({ projectId: projectId.value, drawType: 'CANDIDATE' }),
      api.draw.getResults({ projectId: projectId.value, drawType: 'POSITION' })
    ])
    
    if (examinerRes.code === 200) examinerRecords.value = examinerRes.data || []
    if (candidateRes.code === 200) candidateRecords.value = candidateRes.data || []
    if (positionRes.code === 200) positionRecords.value = positionRes.data || []
  } catch (error) {
    console.error('加载抽签结果失败', error)
  }
  loading.value = false
}

const executeTripleBlind = async () => {
  try {
    await ElMessageBox.confirm(
      '确定执行三盲抽签吗？抽签后将无法撤销。\n\n注意：考生抽签仅对已签到的考生有效。',
      '确认抽签',
      { confirmButtonText: '确定抽签', cancelButtonText: '取消', type: 'warning' }
    )
    
    drawing.value = true
    const res = await api.draw.tripleBlind(projectId.value)
    if (res.code === 200) {
      const data = res.data
      const examinerCount = data.examinerRecords?.length || 0
      const candidateCount = data.candidateRecords?.length || 0
      const positionCount = data.positionRecords?.length || 0
      ElMessage.success(`三盲抽签完成！考官${examinerCount}人，考生${candidateCount}人，职位${positionCount}个`)
      loadDrawResults()
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('抽签失败，请重试')
    }
  }
  drawing.value = false
}

const drawExaminers = async () => {
  drawingExaminer.value = true
  try {
    const res = await api.draw.drawExaminers(projectId.value)
    if (res.code === 200) {
      ElMessage.success('考官抽签完成！')
      loadDrawResults()
    }
  } catch (error) {
    ElMessage.error('抽签失败')
  }
  drawingExaminer.value = false
}

const drawCandidates = async () => {
  drawingCandidate.value = true
  try {
    const res = await api.draw.drawCandidates(projectId.value)
    if (res.code === 200) {
      ElMessage.success('考生抽签完成！')
      loadDrawResults()
    }
  } catch (error) {
    ElMessage.error('抽签失败')
  }
  drawingCandidate.value = false
}

const drawPositions = async () => {
  drawingPosition.value = true
  try {
    const res = await api.draw.drawPositions(projectId.value)
    if (res.code === 200) {
      ElMessage.success('职位顺序抽签完成！')
      loadDrawResults()
    }
  } catch (error) {
    ElMessage.error('抽签失败')
  }
  drawingPosition.value = false
}

onMounted(() => {
  loadProjects()
  if (projectId.value) {
    loadDrawResults()
  }
})
</script>

<style lang="scss" scoped>
.draw-page {
  .draw-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .draw-intro {
      p {
        margin-bottom: 12px;
        color: #606266;
      }
      
      ul {
        list-style: disc;
        padding-left: 20px;
        margin-bottom: 20px;
        
        li {
          padding: 8px 0;
          color: #606266;
        }
      }
    }
  }
  
  .single-draw-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .draw-desc {
      color: #909399;
      font-size: 14px;
      margin-bottom: 12px;
    }
    
    .draw-count {
      font-size: 20px;
      font-weight: 600;
      color: #409EFF;
    }
  }
  
  .result-tabs {
    background: #fff;
    padding: 20px;
    border-radius: 12px;
  }
}
</style>
