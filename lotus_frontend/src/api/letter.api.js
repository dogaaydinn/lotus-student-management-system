import apiClient from '@/lib/axios.js'

export const uploadLetter = async (letter) => {
  const response = await apiClient.post('/letter/upload', {
    letter
  })
  return response.data
}

export const createOfficialLetter = async (letter) => {
  const response = await apiClient.post('/letter/create', {
    letter
  })
  return response.data
}

export const getOfficialLetters = async () => {
  const response = await apiClient.get('/letter/get')
  return response.data
}

export const getFindByOL = async (id) => {
  const response = await apiClient.get(`/letter/get/${id}`)
  return response.data
}
