import apiClient from '@/lib/apiClient.js'

export const getMessages = async () => {
  const response = await apiClient.get('/messages')
  return response.data
}

export const getMessageByEmailTo = async (email) => {
  const response = await apiClient.get(`/messageTo/${email}`)
  return response.data
}

export const getMessageByEmailFrom = async (email) => {
  const response = await apiClient.get(`/messageFrom/${email}`)
  return response.data
}

export const getMessageById = async (id) => {
  const response = await apiClient.get(`/message/${id}`)
  return response.data
}

export const deleteMessageById = async (id) => {
  const response = await apiClient.delete(`/messages/${id}`)
  return response.data
}

export const createMessage = async (message) => {
  const response = await apiClient.post(
    '/message',

    {
      id: 123,
      from: message.from,
      to: message.to,
      date: new Date().getDate() + '/' + new Date().getMonth() + '/' + new Date().getFullYear(),
      time: '' + new Date().getHours() + ':' + new Date().getMinutes(),
      title: message.title,
      file: '',
      text: message.text
    }
  )
  return response.data
}

export const deleteMessage = async (id) => {
  const response = await apiClient.delete(`/messages/${id}`)
  return response.data
}

export const getMsgTo = async (to) => {
  const response = await apiClient.get(`/messages/${to}`)
  return response.data
}

export const getMsgFrom = async (from) => {
  const response = await apiClient.get(`/messages/${from}`)
  return response.data
}
