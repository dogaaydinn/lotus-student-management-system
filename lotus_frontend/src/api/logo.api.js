import apiClient from '@/lib/axios.js'

export const uploadLogo = async (logo) => {
  const response = await apiClient.post('/logo/upload', {
    logo
  })
  return response.data
}
