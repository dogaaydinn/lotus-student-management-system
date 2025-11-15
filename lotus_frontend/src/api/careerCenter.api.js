import apiClient from '@/lib/apiClient.js'

export const loginCareerCenter = async (username, password) => {
  const response = await apiClient.post('/careerCenter/login', {
    username,
    password
  })
  return response.status
}

export const assignCareerCenter = async (username) => {
  const response = await apiClient.post('/careerCenter/assign', {
    username
  })
  return response.data
}

export const getCareerCenterById = async (id) => {
  const response = await apiClient.get(`/careerCenter/${id}`)
  return response.data
}
