import apiClient from '@/lib/apiClient.js'

export const createOfficialLetter = async (data) => {
  const response = await apiClient.post(
    '/officialLetter',

    {
      name: data.name,
      surname: data.surname,
      comName: data.companyName,
      date: new Date(),
      pdf: ''
    }
  )
  return response.data
}
