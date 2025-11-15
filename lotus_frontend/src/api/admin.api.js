import apiClient from '@/lib/axios.js'

export const loginAdmin = async (username, password) => {
  const response = await apiClient.post('/admin/login', {
    username,
    password
  })
  return response.status
}
export const getAdminById = async (id) => {
  const response = await apiClient.get(`/admin/${id}`)
  return response.data
}
