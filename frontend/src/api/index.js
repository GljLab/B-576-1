import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import router from '@/router'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          ElMessageBox.confirm('登录已过期，请重新登录', '提示', {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            localStorage.removeItem('token')
            router.push('/login')
          })
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error(data?.message || '服务器错误')
          break
        default:
          ElMessage.error(data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

// API接口定义
const api = {
  // 认证相关
  auth: {
    login: (data) => request.post('/auth/login', data),
    logout: () => request.post('/auth/logout'),
    getInfo: () => request.get('/auth/info'),
    changePassword: (data) => request.post('/auth/change-password', data)
  },
  
  // 项目相关
  projects: {
    list: (params) => request.get('/projects', { params }),
    get: (id) => request.get(`/projects/${id}`),
    create: (data) => request.post('/projects', data),
    update: (id, data) => request.put(`/projects/${id}`, data),
    delete: (id) => request.delete(`/projects/${id}`),
    updateStatus: (id, status) => request.put(`/projects/${id}/status`, { status }),
    getStatistics: (id) => request.get(`/projects/${id}/statistics`)
  },
  
  // 考生相关
  candidates: {
    list: (params) => request.get('/candidates', { params }),
    get: (id) => request.get(`/candidates/${id}`),
    create: (data) => request.post('/candidates', data),
    update: (id, data) => request.put(`/candidates/${id}`, data),
    delete: (id) => request.delete(`/candidates/${id}`),
    checkIn: (id) => request.post(`/candidates/${id}/check-in`),
    batchImport: (projectId, data) => request.post('/candidates/batch-import', data, { params: { projectId } }),
    updateInterviewStatus: (id, status) => request.put(`/candidates/${id}/interview-status`, { status }),
    getWaiting: (params) => request.get('/candidates/waiting', { params })
  },
  
  // 考官相关
  examiners: {
    list: (params) => request.get('/examiners', { params }),
    get: (id) => request.get(`/examiners/${id}`),
    create: (data) => request.post('/examiners', data),
    update: (id, data) => request.put(`/examiners/${id}`, data),
    delete: (id) => request.delete(`/examiners/${id}`),
    getByRoom: (roomId) => request.get(`/examiners/room/${roomId}`)
  },
  
  // 考场相关
  rooms: {
    list: (params) => request.get('/rooms', { params }),
    get: (id) => request.get(`/rooms/${id}`),
    create: (data) => request.post('/rooms', data),
    update: (id, data) => request.put(`/rooms/${id}`, data),
    delete: (id) => request.delete(`/rooms/${id}`),
    getByProject: (projectId) => request.get(`/rooms/project/${projectId}`)
  },
  
  // 职位相关
  positions: {
    list: (params) => request.get('/positions', { params }),
    get: (id) => request.get(`/positions/${id}`),
    create: (data) => request.post('/positions', data),
    update: (id, data) => request.put(`/positions/${id}`, data),
    delete: (id) => request.delete(`/positions/${id}`),
    getByProject: (projectId) => request.get(`/positions/project/${projectId}`)
  },
  
  // 抽签相关
  draw: {
    tripleBlind: (projectId) => request.post('/draw/triple-blind', null, { params: { projectId } }),
    drawExaminers: (projectId) => request.post('/draw/examiners', null, { params: { projectId } }),
    drawCandidates: (projectId) => request.post('/draw/candidates', null, { params: { projectId } }),
    drawPositions: (projectId) => request.post('/draw/positions', null, { params: { projectId } }),
    getResults: (params) => request.get('/draw/results', { params }),
    reset: (projectId, drawType) => request.delete('/draw/reset', { params: { projectId, drawType } })
  },
  
  // 评分相关
  scores: {
    submit: (data) => request.post('/scores', data),
    submitMulti: (data) => request.post('/scores/multi', data),
    getCandidateScores: (candidateId) => request.get(`/scores/candidate/${candidateId}`),
    getCandidateDetails: (candidateId, projectId) => request.get(`/scores/candidate/${candidateId}/details`, { params: { projectId } }),
    calculate: (candidateId, projectId) => request.post(`/scores/calculate/${candidateId}`, null, { params: { projectId } }),
    calculateAll: (projectId) => request.post('/scores/calculate-all', null, { params: { projectId } }),
    getFinal: (candidateId) => request.get(`/scores/final/${candidateId}`),
    getRanking: (params) => request.get('/scores/ranking', { params }),
    publish: (projectId) => request.post('/scores/publish', null, { params: { projectId } }),
    getStatistics: (projectId) => request.get('/scores/statistics', { params: { projectId } }),
    getRealtime: (candidateId, removeExtreme = true) => request.get(`/scores/realtime/${candidateId}`, { params: { removeExtreme } })
  },

  // 评分项管理
  scoreItems: {
    list: (projectId) => request.get(`/score-items/project/${projectId}`),
    get: (id) => request.get(`/score-items/${id}`),
    create: (data) => request.post('/score-items', data),
    update: (id, data) => request.put(`/score-items/${id}`, data),
    delete: (id) => request.delete(`/score-items/${id}`),
    batchSave: (items) => request.post('/score-items/batch', items),
    validateWeights: (projectId) => request.get(`/score-items/validate-weights/${projectId}`),
    updateSort: (projectId, itemIds) => request.post(`/score-items/sort/${projectId}`, itemIds),
    applyTemplate: (projectId, templateId, overwrite = false) => request.post(`/score-items/apply-template/${projectId}`, null, { params: { templateId, overwrite } }),
    preview: (projectId) => request.get(`/score-items/preview/${projectId}`),
    getStatistics: (projectId) => request.get(`/score-items/statistics/${projectId}`),
    getCandidateScores: (candidateId, projectId) => request.get(`/score-items/candidate/${candidateId}`, { params: { projectId } })
  },

  // 评分模板管理
  scoreTemplates: {
    list: (isSystem) => request.get('/score-templates', { params: { isSystem } }),
    get: (id) => request.get(`/score-templates/${id}`),
    create: (data) => request.post('/score-templates', data),
    update: (id, data) => request.put(`/score-templates/${id}`, data),
    delete: (id) => request.delete(`/score-templates/${id}`)
  }
}

export default api
