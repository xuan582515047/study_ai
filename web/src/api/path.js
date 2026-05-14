import request from './request'

export const planPath = (data) => request.post('/path/plan', data)
export const listPaths = (studentId) => request.get(`/path/list/${studentId}`)
export const getPathSteps = (pathId) => request.get(`/path/steps/${pathId}`)
