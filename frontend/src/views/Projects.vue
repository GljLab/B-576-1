<template>
  <div class="page-container">
    <div class="page-header">
      <span class="title">项目管理</span>
      <el-button type="primary" @click="showDialog()">
        <el-icon><Plus /></el-icon>新建项目
      </el-button>
    </div>
    
    <div class="search-bar">
      <el-input 
        v-model="searchKeyword" 
        placeholder="搜索项目名称/编码/单位" 
        style="width: 300px"
        clearable
        @clear="loadData"
        @keyup.enter="loadData"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      
      <el-select v-model="searchStatus" placeholder="项目状态" clearable style="width: 150px" @change="loadData">
        <el-option label="准备中" :value="0" />
        <el-option label="抽签中" :value="1" />
        <el-option label="面试中" :value="2" />
        <el-option label="已结束" :value="3" />
      </el-select>
      
      <el-button type="primary" @click="loadData">
        <el-icon><Search /></el-icon>搜索
      </el-button>
    </div>
    
    <el-table :data="tableData" v-loading="loading" stripe>
      <el-table-column prop="projectName" label="项目名称" min-width="200" />
      <el-table-column prop="projectCode" label="项目编码" width="130" />
      <el-table-column prop="organizer" label="组织单位" min-width="150" />
      <el-table-column prop="interviewDate" label="面试日期" width="120" />
      <el-table-column prop="location" label="面试地点" min-width="150" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="$router.push(`/projects/${row.id}`)">
            详情
          </el-button>
          <el-button type="primary" link @click="showDialog(row)">
            编辑
          </el-button>
          <el-popconfirm title="确定删除该项目吗？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button type="danger" link>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>
    
    <!-- 新建/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑项目' : '新建项目'" 
      width="700px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="项目名称" prop="projectName">
              <el-input v-model="form.projectName" placeholder="请输入项目名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目编码" prop="projectCode">
              <el-input v-model="form.projectCode" placeholder="请输入项目编码" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="组织单位" prop="organizer">
          <el-input v-model="form.organizer" placeholder="请输入组织单位" />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="面试日期" prop="interviewDate">
              <el-date-picker 
                v-model="form.interviewDate" 
                type="date" 
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="面试时间">
              <el-time-picker
                v-model="form.timeRange"
                is-range
                range-separator="至"
                start-placeholder="开始"
                end-placeholder="结束"
                format="HH:mm"
                value-format="HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="面试地点" prop="location">
          <el-input v-model="form.location" placeholder="请输入面试地点" />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="笔试权重" prop="writtenWeight">
              <el-input-number 
                v-model="form.writtenWeight" 
                :min="0" 
                :max="100" 
                :step="5"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="面试权重" prop="interviewWeight">
              <el-input-number 
                v-model="form.interviewWeight" 
                :min="0" 
                :max="100" 
                :step="5"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="去最高分">
              <el-switch v-model="form.removeHighest" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="去最低分">
              <el-switch v-model="form.removeLowest" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="项目描述">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入项目描述"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '@/api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const searchStatus = ref('')

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  projectName: '',
  projectCode: '',
  organizer: '',
  interviewDate: '',
  timeRange: null,
  startTime: '',
  endTime: '',
  location: '',
  writtenWeight: 50,
  interviewWeight: 50,
  removeHighest: 1,
  removeLowest: 1,
  scorePrecision: 2,
  description: ''
})

const rules = {
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  projectCode: [{ required: true, message: '请输入项目编码', trigger: 'blur' }],
  organizer: [{ required: true, message: '请输入组织单位', trigger: 'blur' }]
}

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
    const res = await api.projects.list({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value,
      status: searchStatus.value
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载失败', error)
  }
  loading.value = false
}

const showDialog = (row = null) => {
  isEdit.value = !!row
  if (row) {
    Object.assign(form, row)
    if (row.startTime && row.endTime) {
      form.timeRange = [row.startTime, row.endTime]
    }
  } else {
    Object.assign(form, {
      id: null,
      projectName: '',
      projectCode: '',
      organizer: '',
      interviewDate: '',
      timeRange: null,
      startTime: '',
      endTime: '',
      location: '',
      writtenWeight: 50,
      interviewWeight: 50,
      removeHighest: 1,
      removeLowest: 1,
      scorePrecision: 2,
      description: ''
    })
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  submitting.value = true
  
  const data = { ...form }
  if (data.timeRange) {
    data.startTime = data.timeRange[0]
    data.endTime = data.timeRange[1]
  }
  delete data.timeRange
  
  try {
    const res = isEdit.value 
      ? await api.projects.update(form.id, data)
      : await api.projects.create(data)
    
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
  
  submitting.value = false
}

const handleDelete = async (id) => {
  try {
    const res = await api.projects.delete(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadData()
})
</script>
