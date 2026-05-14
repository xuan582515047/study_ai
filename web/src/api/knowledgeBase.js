import request from './request'

export const listKnowledgeBases = () => request.get('/knowledge-base/list')

export const getKnowledgeBaseDetail = (id) => request.get(`/knowledge-base/${id}`)

export const createKnowledgeBase = (data) => request.post('/knowledge-base/create', data)

export const updateKnowledgeBase = (id, data) => request.put(`/knowledge-base/update/${id}`, data)

export const deleteKnowledgeBase = (id) => request.delete(`/knowledge-base/delete/${id}`)
