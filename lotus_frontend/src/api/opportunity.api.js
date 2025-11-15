import apiClient from '@/lib/apiClient.js'
const generateId = () => Number((Math.random() * 1000000).toFixed(0))

export const putOpportunity = async (opportunity) => {
  const response = await apiClient.post(`/opportunitie`, {
    location: opportunity.location,
    id: generateId(),
    url: opportunity.url,
    deadline: opportunity.deadline,
    companyName: opportunity.companyName,
    title: opportunity.title
  })
  return response.data
}

export const getOpportunityById = async (id) => {
  const response = await apiClient.get(`/opportunities/${id}`)
  return response.data
}

export const getOpportunities = async () => {
  const response = await apiClient.get('/opportunities')
  return response.data
}

export const deleteOpportunity = async (id) => {
  const response = await apiClient.delete(`/opportunities/${id}`)
  return response.data
}

export const updateOpportunity = async (opportunity) => {
  const response = await apiClient.put(`/opportunities/${opportunity.id}`, {
    location: opportunity.location,
    url: opportunity.url,
    deadline: opportunity.deadline,
    companyName: opportunity.companyName,
    title: opportunity.title
  })
  return response.data
}
