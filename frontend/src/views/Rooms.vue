<template>
  <div class="page-container">
    <div class="page-header">
      <span class="title">考场管理</span>
      <el-button type="primary" @click="showDialog()">
        <el-icon><Plus /></el-icon>新增考场
      </el-button>
    </div>
    
    <div class="search-bar">
      <el-select v-model="projectId" placeholder="选择项目" clearable style="width: 250px" @change="loadData">
        <el-option v-for="p in projects" :key="p.id" :label="p.projectName" :value="p.id" />
      </el-select>
      
      <el-input 
        v-model="searchKeyword" 
        placeholder="搜索考场名称" 
        style="width: 200px"
        clearable
        @keyup.enter="loadData"
      />
      
      <el-button type="primary" @click="loadData">
        <el-icon><Search /></el-icon>搜索
      </el-button>
    </div>
    
    <el-table :data="tableData" v-loading="loading" stripe>
      <el-table-column prop="roomName" label="考场名称" min-width="150" />
      <el-table-column prop="roomCode" label="考场编码" width="120" />
      <el-table-column prop="location" label="位置" min-width="150" />
      <el-table-column prop="capacity" label="容纳人数" width="100" />
      <el-table-column prop="examinerCount" label="考官人数" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getRoomStatusType(row.status)">
            {{ getRoomStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="showDialog(row)">编辑</el-button>
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
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>
    
    <!-- 对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑考场' : '新增考场'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="项目" prop="projectId">
          <el-select v-model="form.projectId" placeholder="选择项目" style="width: 100%">
            <el-option v-for="p in projects" :key="p.id" :label="p.projectName" :value="p.id" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="考场名称" prop="roomName">
          <el-input v-model="form.roomName" placeholder="请输入考场名称" />
        </el-form-item>
        
        <el-form-item label="考场编码">
          <el-input v-model="form.roomCode" placeholder="请输入考场编码" />
        </el-form-item>
        
        <el-form-item label="位置">
          <el-input v-model="form.location" placeholder="请输入位置" />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="容纳人数">
              <el-input-number v-model="form.capacity" :min="1" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="考官人数">
              <el-input-number v-model="form.examinerCount" :min="1" :max="20" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
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
  roomName: '',
  roomCode: '',
  location: '',
  capacity: 30,
  examinerCount: 7
})

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  roomName: [{ required: true, message: '请输入考场名称', trigger: 'blur' }]
}

const getRoomStatusType = (status) => {
  const types = { 0: 'info', 1: 'success', 2: 'warning' }
  return types[status] || 'info'
}

const getRoomStatusText = (status) => {
  const texts = { 0: '禁用', 1: '可用', 2: '面试中' }
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
    const res = await api.rooms.list({
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
      roomName: '',
      roomCode: '',
      location: '',
      capacity: 30,
      examinerCount: 7
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
      ? await api.rooms.update(form.id, form)
      : await api.rooms.create(form)
    
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
    const res = await api.rooms.delete(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadProjects()
  loadData()
})
</script>
