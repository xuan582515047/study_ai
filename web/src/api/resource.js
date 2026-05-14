import request from './request'

export const generateResources = (data) => request.post('/resource/generate', data)
export const listResources = (studentId) => request.get(`/resource/list/${studentId}`)
