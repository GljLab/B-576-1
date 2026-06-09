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

    <el-tabs v-model="activeTab" class="main-tabs">
      <el-tab-pane label="项目概览" name="overview">
        <div class="overview-content">
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
      </el-tab-pane>

      <el-tab-pane label="评分项配置" name="score-items">
        <div class="score-items-content">
          <div class="score-items-header">
            <div class="header-left">
              <el-button type="primary" @click="handleAddItem">
                <el-icon><Plus /></el-icon>新增评分项
              </el-button>
              <el-button @click="openTemplateDialog">
                <el-icon><Collection /></el-icon>套用模板
              </el-button>
              <el-button @click="previewScoreItems">
                <el-icon><View /></el-icon>预览评分体系
              </el-button>
            </div>
            <div class="header-right">
              <div class="weight-info" :class="{ valid: weightValidation.isValid, invalid: !weightValidation.isValid && scoreItems.length > 0 }">
                <span>权重合计：</span>
                <strong>{{ weightValidation.totalWeight || 0 }}%</strong>
                <span v-if="!weightValidation.isValid && scoreItems.length > 0" class="weight-tip">
                  ({{ weightValidation.diff > 0 ? '超出' : '不足' }} {{ Math.abs(weightValidation.diff) }}%)
                </span>
                <el-tag v-if="weightValidation.isValid" type="success" size="small">✓ 已达标</el-tag>
                <el-tag v-else-if="scoreItems.length > 0" type="danger" size="small">需调整为100%</el-tag>
              </div>
            </div>
          </div>

          <el-table 
            :data="scoreItems" 
            v-loading="loadingItems" 
            stripe 
            border
            row-key="id"
          >
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="itemName" label="评分项名称" min-width="150">
              <template #default="{ row }">
                <el-input v-if="row._editing" v-model="row.itemName" size="small" />
                <span v-else>{{ row.itemName }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="itemCode" label="编码" width="120">
              <template #default="{ row }">
                <el-input v-if="row._editing" v-model="row.itemCode" size="small" />
                <span v-else>{{ row.itemCode || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="maxScore" label="满分值" width="100">
              <template #default="{ row }">
                <el-input-number 
                  v-if="row._editing" 
                  v-model="row.maxScore" 
                  :min="0" 
                  :max="1000" 
                  :step="10"
                  size="small" 
                  controls-position="right"
                />
                <span v-else>{{ row.maxScore }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="weight" label="权重(%)" width="110">
              <template #default="{ row }">
                <el-input-number 
                  v-if="row._editing" 
                  v-model="row.weight" 
                  :min="0" 
                  :max="100" 
                  :step="5"
                  :precision="2"
                  size="small" 
                  controls-position="right"
                />
                <span v-else>
                  <el-tag :type="getWeightTagType(row.weight)">{{ row.weight }}%</el-tag>
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="评分说明" min-width="200">
              <template #default="{ row }">
                <el-input 
                  v-if="row._editing" 
                  v-model="row.description" 
                  type="textarea" 
                  :rows="1"
                  size="small"
                />
                <el-tooltip v-else :content="row.description || '暂无说明'" placement="top">
                  <span class="desc-text">{{ row.description || '-' }}</span>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row, $index }">
                <template v-if="row._editing">
                  <el-button type="success" link size="small" @click="saveItem(row)">
                    <el-icon><Check /></el-icon>保存
                  </el-button>
                  <el-button link size="small" @click="cancelEdit(row)">取消</el-button>
                </template>
                <template v-else>
                  <el-button type="primary" link size="small" @click="editItem(row)">
                    <el-icon><Edit /></el-icon>编辑
                  </el-button>
                  <el-button link size="small" @click="moveItem($index, -1)" :disabled="$index === 0">
                    <el-icon><Top /></el-icon>上移
                  </el-button>
                  <el-button link size="small" @click="moveItem($index, 1)" :disabled="$index === scoreItems.length - 1">
                    <el-icon><Bottom /></el-icon>下移
                  </el-button>
                  <el-button type="danger" link size="small" @click="deleteItem(row)">
                    <el-icon><Delete /></el-icon>删除
                  </el-button>
                </template>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="!loadingItems && scoreItems.length === 0" description="暂无评分项，请新增或套用模板" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="templateDialogVisible" title="选择评分模板" width="700px">
      <div class="template-list" v-loading="loadingTemplates">
        <div 
          v-for="tpl in templates" 
          :key="tpl.id" 
          class="template-card"
          :class="{ active: selectedTemplateId === tpl.id }"
          @click="selectedTemplateId = tpl.id"
        >
          <div class="tpl-header">
            <span class="tpl-name">{{ tpl.templateName }}</span>
            <el-tag v-if="tpl.isSystem" type="warning" size="small">系统预置</el-tag>
            <el-tag v-else size="small">自定义</el-tag>
          </div>
          <div class="tpl-desc">{{ tpl.description }}</div>
          <div class="tpl-count">共 {{ tpl.itemCount }} 个评分项</div>
        </div>
        <el-empty v-if="!loadingTemplates && templates.length === 0" description="暂无可用模板" />
      </div>
      <template #footer>
        <el-checkbox v-model="overwriteTemplate">覆盖现有评分项</el-checkbox>
        <el-button @click="templateDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!selectedTemplateId" @click="confirmApplyTemplate">
          确认套用
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewDialogVisible" title="评分体系预览" width="800px">
      <div v-loading="loadingPreview" class="preview-content">
        <div class="preview-header" :class="{ valid: previewData?.weightValidation?.isValid }">
          <h4>评分项汇总</h4>
          <div class="preview-summary">
            <span>评分项数量：{{ previewData?.items?.length || 0 }} 项</span>
            <span>权重合计：{{ previewData?.weightValidation?.totalWeight || 0 }}%</span>
            <span>加权满分：{{ previewData?.totalMaxWeighted || 0 }}</span>
            <el-tag v-if="previewData?.weightValidation?.isValid" type="success">权重配置有效</el-tag>
            <el-tag v-else type="danger">权重需调整为100%</el-tag>
          </div>
        </div>
        <el-table :data="previewData?.items || []" stripe border size="small">
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="itemName" label="评分项" min-width="140" />
          <el-table-column prop="itemCode" label="编码" width="100" />
          <el-table-column prop="maxScore" label="满分" width="80" />
          <el-table-column prop="weight" label="权重" width="80">
            <template #default="{ row }">{{ row.weight }}%</template>
          </el-table-column>
          <el-table-column label="加权满分" width="100">
            <template #default="{ row }">
              {{ ((Number(row.maxScore) * Number(row.weight)) / 100).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="description" label="评分说明" min-width="200" />
        </el-table>
      </div>
      <template #footer>
        <el-button type="primary" @click="previewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()

const projectId = ref(route.params.id)
const loading = ref(false)
const project = ref(null)
const stats = ref({})
const activeTab = ref('overview')

const scoreItems = ref([])
const loadingItems = ref(false)
const weightValidation = reactive({
  totalWeight: 0,
  isValid: false,
  diff: 0
})

const templateDialogVisible = ref(false)
const loadingTemplates = ref(false)
const templates = ref([])
const selectedTemplateId = ref(null)
const overwriteTemplate = ref(false)

const previewDialogVisible = ref(false)
const loadingPreview = ref(false)
const previewData = ref(null)

const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'success' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '准备中', 1: '抽签中', 2: '面试中', 3: '已结束' }
  return texts[status] || '未知'
}

const getWeightTagType = (weight) => {
  if (weight >= 25) return 'danger'
  if (weight >= 15) return 'warning'
  return 'info'
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

const loadScoreItems = async () => {
  loadingItems.value = true
  try {
    const res = await api.scoreItems.list(projectId.value)
    if (res.code === 200) {
      scoreItems.value = res.data || []
      await validateWeights()
    }
  } catch (error) {
    console.error('加载评分项失败', error)
  }
  loadingItems.value = false
}

const validateWeights = async () => {
  try {
    const res = await api.scoreItems.validateWeights(projectId.value)
    if (res.code === 200) {
      Object.assign(weightValidation, res.data)
    }
  } catch (error) {
    console.error('验证权重失败', error)
  }
}

watch(activeTab, (val) => {
  if (val === 'score-items' && scoreItems.value.length === 0) {
    loadScoreItems()
  }
})

const handleAddItem = () => {
  const newItem = {
    projectId: Number(projectId.value),
    itemName: '',
    itemCode: '',
    maxScore: 100,
    weight: 0,
    description: '',
    sortOrder: scoreItems.value.length + 1,
    status: 1,
    _editing: true,
    _isNew: true
  }
  scoreItems.value.push(newItem)
}

const editItem = (row) => {
  row._editing = true
}

const cancelEdit = (row) => {
  if (row._isNew) {
    scoreItems.value = scoreItems.value.filter(i => i !== row)
  } else {
    row._editing = false
  }
}

const saveItem = async (row) => {
  if (!row.itemName || row.itemName.trim() === '') {
    ElMessage.warning('请输入评分项名称')
    return
  }
  if (row.weight == null || row.weight < 0) {
    ElMessage.warning('请输入有效的权重值')
    return
  }
  if (row.maxScore == null || row.maxScore <= 0) {
    ElMessage.warning('请输入有效的满分值')
    return
  }
  
  try {
    if (row._isNew) {
      const { _editing, _isNew, ...data } = row
      const res = await api.scoreItems.create(data)
      if (res.code === 200) {
        Object.assign(row, res.data)
        row._editing = false
        row._isNew = false
        ElMessage.success('新增成功')
        await validateWeights()
      }
    } else {
      const { _editing, ...data } = row
      const res = await api.scoreItems.update(row.id, data)
      if (res.code === 200) {
        row._editing = false
        ElMessage.success('保存成功')
        await validateWeights()
      }
    }
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const deleteItem = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除评分项「${row.itemName}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await api.scoreItems.delete(row.id)
    if (res.code === 200) {
      scoreItems.value = scoreItems.value.filter(i => i.id !== row.id)
      ElMessage.success('删除成功')
      await validateWeights()
    }
  } catch (e) {
    // 取消操作
  }
}

const moveItem = async (index, direction) => {
  const newIndex = index + direction
  if (newIndex < 0 || newIndex >= scoreItems.value.length) return
  
  const temp = scoreItems.value[index]
  scoreItems.value[index] = scoreItems.value[newIndex]
  scoreItems.value[newIndex] = temp
  
  scoreItems.value.forEach((item, i) => {
    item.sortOrder = i + 1
  })
  
  try {
    const ids = scoreItems.value.map(i => i.id).filter(Boolean)
    await api.scoreItems.updateSort(projectId.value, ids)
    ElMessage.success('排序已更新')
  } catch (error) {
    console.error('更新排序失败', error)
  }
}

const openTemplateDialog = async () => {
  templateDialogVisible.value = true
  selectedTemplateId.value = null
  overwriteTemplate.value = false
  loadingTemplates.value = true
  try {
    const res = await api.scoreTemplates.list()
    if (res.code === 200) {
      templates.value = res.data || []
    }
  } catch (error) {
    console.error('加载模板失败', error)
  }
  loadingTemplates.value = false
}

const confirmApplyTemplate = async () => {
  if (!selectedTemplateId.value) {
    ElMessage.warning('请选择模板')
    return
  }
  try {
    const res = await api.scoreItems.applyTemplate(
      projectId.value, 
      selectedTemplateId.value, 
      overwriteTemplate.value
    )
    if (res.code === 200) {
      ElMessage.success('模板套用成功')
      templateDialogVisible.value = false
      await loadScoreItems()
    }
  } catch (error) {
    ElMessage.error('套用失败')
  }
}

const previewScoreItems = async () => {
  previewDialogVisible.value = true
  loadingPreview.value = true
  try {
    const res = await api.scoreItems.preview(projectId.value)
    if (res.code === 200) {
      previewData.value = res.data
    }
  } catch (error) {
    console.error('预览失败', error)
  }
  loadingPreview.value = false
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
    margin-bottom: 20px;
    
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

  .main-tabs {
    background: #fff;
    padding: 0 20px;
    border-radius: 8px;
  }

  .overview-content {
    padding-top: 16px;
    
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

  .score-items-content {
    padding-top: 16px;
    padding-bottom: 24px;

    .score-items-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      .header-left {
        display: flex;
        gap: 12px;
      }
      
      .weight-info {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px 16px;
        border-radius: 8px;
        background: #f5f7fa;
        
        strong {
          font-size: 20px;
        }
        
        .weight-tip {
          color: #F56C6C;
          font-size: 12px;
        }
        
        &.valid {
          background: #f0f9eb;
          strong { color: #67C23A; }
        }
        
        &.invalid {
          background: #fef0f0;
          strong { color: #F56C6C; }
        }
      }
    }

    .desc-text {
      display: -webkit-box;
      -webkit-line-clamp: 1;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
  }

  .template-list {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    max-height: 500px;
    overflow-y: auto;
    
    .template-card {
      border: 2px solid #ebeef5;
      border-radius: 8px;
      padding: 16px;
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        border-color: #409EFF;
        background: #f5faff;
      }
      
      &.active {
        border-color: #409EFF;
        background: #ecf5ff;
        box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
      }
      
      .tpl-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;
        
        .tpl-name {
          font-weight: 600;
          font-size: 15px;
        }
      }
      
      .tpl-desc {
        font-size: 13px;
        color: #606266;
        margin-bottom: 8px;
        min-height: 36px;
      }
      
      .tpl-count {
        font-size: 12px;
        color: #909399;
      }
    }
  }

  .preview-content {
    .preview-header {
      padding: 16px;
      border-radius: 8px;
      background: #f5f7fa;
      margin-bottom: 16px;
      
      &.valid {
        background: #f0f9eb;
      }
      
      h4 {
        margin: 0 0 12px 0;
      }
      
      .preview-summary {
        display: flex;
        gap: 24px;
        align-items: center;
        flex-wrap: wrap;
      }
    }
  }
}
</style>
