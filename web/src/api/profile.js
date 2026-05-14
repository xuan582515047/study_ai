import request from './request'

export const buildProfile = (data) => request.post('/profile/build', data)
export const getProfile = (studentId) => request.get(`/profile/${studentId}`)
export const getProfileStatus = () => request.get('/profile/status')
