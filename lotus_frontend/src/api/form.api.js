import apiClient from '@/lib/axios.js'

export const uploadForm = async (form) => {
  const response = await apiClient.post('/form/upload', {
    form
  })
  return response.data
}

export const getApplicationForms = async () => {
  const response = await apiClient.get('/form/get')
  return response.data
}
