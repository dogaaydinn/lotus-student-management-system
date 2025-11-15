import apiClient from '@/lib/axios.js'

export const getInstructors = async () => {
  const response = await apiClient.get('/instructors')
  return response.data
}
