import request from './request'

export const chat = (data) => request.post('/tutor/chat', data)
export const getHistory = (studentId, sessionId) => request.get('/tutor/history', { params: { studentId, sessionId } })
