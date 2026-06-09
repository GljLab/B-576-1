<template>
  <div class="page-container">
    <div class="page-header">
      <span class="title">考生管理</span>
      <el-button type="primary" @click="showDialog()">
        <el-icon><Plus /></el-icon>新增考生
      </el-button>
    </div>
    
    <div class="search-bar">
      <el-select v-model="projectId" placeholder="选择项目" clearable style="width: 250px" @change="loadData">
        <el-option 
          v-for="p in projects" 
          :key="p.id" 
          :label="p.projectName" 
          :value="p.id" 
        />
      </el-select>
      
      <el-input 
        v-model="searchKeyword" 
        placeholder="搜索姓名/准考证号" 
        style="width: 200px"
        clearable
        @clear="loadData"
        @keyup.enter="loadData"
      />
      
      <el-button type="primary" @click="loadData">
        <el-icon><Search /></el-icon>搜索
      </el-button>
    </div>
    
    <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="candidateName" label="姓名" min-width="100" />
      <el-table-column prop="ticketNo" label="准考证号" min-width="130" />
      <el-table-column prop="gender" label="性别" width="70">
        <template #default="{ row }">
          {{ row.gender === 1 ? '男' : '女' }}
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="联系电话" min-width="130" />
      <el-table-column prop="writtenScore" label="笔试成绩" min-width="100" />
      <el-table-column prop="interviewOrder" label="面试序号" min-width="100">
        <template #default="{ row }">
          <span v-if="row.interviewOrder">第{{ row.interviewOrder }}号</span>
          <span v-else class="text-muted">未抽签</span>
        </template>
      </el-table-column>
      <el-table-column prop="checkInStatus" label="签到状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="row.checkInStatus === 1 ? 'success' : 'info'">
            {{ row.checkInStatus === 1 ? '已签到' : '未签到' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="interviewStatus" label="面试状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="getInterviewStatusType(row.interviewStatus)">
            {{ getInterviewStatusText(row.interviewStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="success" link @click="handleCheckIn(row)" v-if="row.checkInStatus !== 1">
            签到
          </el-button>
          <el-button type="primary" link @click="showDialog(row)">
            编辑
          </el-button>
          <el-popconfirm title="确定删除吗？" @confirm="handleDelete(row.id)">
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
    
    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑考生' : '新增考生'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="项目" prop="projectId">
              <el-select v-model="form.projectId" placeholder="选择项目" style="width: 100%">
                <el-option v-for="p in projects" :key="p.id" :label="p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="candidateName">
              <el-input v-model="form.candidateName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="准考证号" prop="ticketNo">
              <el-input v-model="form.ticketNo" placeholder="请输入准考证号" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="0">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="笔试成绩">
          <el-input-number v-model="form.writtenScore" :precision="2" :min="0" :max="100" style="width: 100%" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api'
import { ElMessage } from 'element-plus'

const route = useRoute()

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const projectId = ref(route.query.projectId ? Number(route.query.projectId) : '')
const searchKeyword = ref('')
const projects = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  projectId: '',
  candidateName: '',
  idCard: '',
  ticketNo: '',
  phone: '',
  gender: 1,
  writtenScore: null
})

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  candidateName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  ticketNo: [{ required: true, message: '请输入准考证号', trigger: 'blur' }]
}

const getInterviewStatusType = (status) => {
  const types = { 0: 'info', 1: 'warning', 2: 'success' }
  return types[status] || 'info'
}

const getInterviewStatusText = (status) => {
  const texts = { 0: '待面试', 1: '面试中', 2: '已完成' }
  return texts[status] || '未知'
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

const loadData = async () => {
  loading.value = true
  try {
    const res = await api.candidates.list({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      projectId: projectId.value,
      keyword: searchKeyword.value
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
  } else {
    Object.assign(form, {
      id: null,
      projectId: projectId.value || '',
      candidateName: '',
      idCard: '',
      ticketNo: '',
      phone: '',
      gender: 1,
      writtenScore: null
    })
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  submitting.value = true
  try {
    const res = isEdit.value 
      ? await api.candidates.update(form.id, form)
      : await api.candidates.create(form)
    
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadData()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
  submitting.value = false
}

const handleDelete = async (id) => {
  try {
    const res = await api.candidates.delete(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleCheckIn = async (row) => {
  try {
    const res = await api.candidates.checkIn(row.id)
    if (res.code === 200) {
      ElMessage.success('签到成功')
      loadData()
    }
  } catch (error) {
    ElMessage.error('签到失败')
  }
}

onMounted(() => {
  loadProjects()
  loadData()
})
</script>
