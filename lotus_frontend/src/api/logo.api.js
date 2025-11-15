import apiClient from '@/lib/apiClient.js'

export const uploadLogo = async (logo) => {
  const response = await apiClient.post('/logo/upload', {
    logo
  })
  return response.data
}
