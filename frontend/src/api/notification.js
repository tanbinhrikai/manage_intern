import http from './http'

export const getMyNotifications = (params) => {
  return http.get('/notifications/me', { params })
}

export const getUnreadNotificationCount = () => {
  return http.get('/notifications/unread-count')
}

export const markNotificationAsRead = (id) => {
  return http.patch(`/notifications/${id}/read`)
}