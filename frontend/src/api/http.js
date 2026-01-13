import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

const API_BASE_URL = 'http://localhost:8080'

const http = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json'
    },
    withCredentials: true
})

http.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config

        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true

            try {
                const authStore = useAuthStore()
                await authStore.refreshToken()
                return http(originalRequest)
            } catch (refreshError) {
                const authStore = useAuthStore()
                authStore.logout()
                return Promise.reject(refreshError)
            }
        }

        return Promise.reject(error)
    }
)

export default http