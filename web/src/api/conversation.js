import request from './request'

export const listSessions = (agentType) =>
  request.get('/conversation/sessions', { params: { agentType } })

export const getMessages = (sessionId) =>
  request.get('/conversation/messages', { params: { sessionId } })

export const saveMessage = (data) =>
  request.post('/conversation/message', data)

export const deleteSession = (sessionId) =>
  request.delete(`/conversation/session/${sessionId}`)
