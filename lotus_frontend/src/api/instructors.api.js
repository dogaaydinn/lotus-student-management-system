import apiClient from '@/lib/apiClient.js'

export const getInstructors = async () => {
  const response = await apiClient.get('/instructors')
  return response.data
}
