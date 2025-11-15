import apiClient from '@/lib/apiClient.js'

export const getFiles = async () => {
  const response = await apiClient.get('/files')
  return response.data
}

export const getFilesByID = async (id) => {
  const response = await apiClient.get(`/files/${id}`)
  return response.data
}
