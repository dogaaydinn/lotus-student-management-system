import apiClient from '@/lib/apiClient.js'

export const loginStudent = async (username, password) => {
  const response = await apiClient.post('/student/login', {
    username,
    password
  })
  return response.status
}

export const getStudentById = async (id) => {
  const response = await apiClient.get(`/student/${id}`)
  return response.data
}