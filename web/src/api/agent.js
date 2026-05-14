import request from './request'

export const invokeAgent = (agentType, data) => request.post(`/agent/${agentType}`, data)

export const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload/file', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
