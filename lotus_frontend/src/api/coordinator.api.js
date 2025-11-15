import apiClient from '@/lib/axios.js'

export const loginCoordinator = async (username, password) => {
  const response = await apiClient.post('/coordinator/login', {
    username,
    password
  })
  return response.status
}

export const assignCoordinator = async (username, password) => {
  const response = await apiClient.post('/coordinator/assign', {
    username,
    password
  })
  return response.data
}
export const getCoordinatorById = async (id) => {
  const response = await apiClient.get(`/student/${id}`)
  return response.data
}
