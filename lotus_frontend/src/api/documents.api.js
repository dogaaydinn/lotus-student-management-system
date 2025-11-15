import apiClient from '@/lib/axios.js'

export const getDocuments = async () => {
  const response = await apiClient.get('/documents')
  return response.data
}

export const createDocument = async (document) => {
  const response = await apiClient.post(
    '/documents',

    {
      file_name: document.file_name,
      file_data: 'desktop/n.pdf'
    }
  )
  return response.data
}

export const uploadDocument = async (document) => {
  const response = await apiClient.post(
    '/documents/upload',

    {
      file_name: document.file_name,
      file_data: document.file_data
    }
  )
  return response.data
}
