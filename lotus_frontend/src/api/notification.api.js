import apiClient from '@/lib/apiClient.js'

export const getNotifications = async () => {
  const response = await apiClient.get('/notifications')
  return response.data
}

export const createNotification = async (notification) => {
  const response = await apiClient.post(
    '/notifications',

    {
      id: notification.id,
      from: notification.from,
      to: notification.to,
      date: notification.date,
      time: notification.time,
      title: notification.title,
      text: notification.text
    }
  )
  return response.data
}

export const deleteNotification = async (id) => {
  const response = await apiClient.delete(`/notifications/${id}`)
  return response.data
}
