import request from './request'

export const generateQuestions = (params) => request.post('/assessment/questions/generate', null, { params })
export const submitAssessment = (data) => request.post('/assessment/submit', data)
export const listAssessments = (studentId) => request.get(`/assessment/list/${studentId}`)
