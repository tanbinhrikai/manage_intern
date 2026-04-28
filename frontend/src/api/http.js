import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import router from '@/routers'
import { ElMessage } from 'element-plus'

const API_BASE_URL = 'http://localhost:8080'

const http = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json'
    },
    withCredentials: true
})

const refreshAxios = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json'
    },
    withCredentials: true
})

let isRefreshing = false
let failedQueue = []

const processQueue = (error, token = null) => {
    failedQueue.forEach(prom => {
        if (error) {
            prom.reject(error)
        } else {
            prom.resolve()
        }
    })
    failedQueue = []
}

http.interceptors.request.use(
    (config) => {
        const authStore = useAuthStore()
        if (authStore.accessToken) {
            config.headers.Authorization = `Bearer ${authStore.accessToken}`
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

http.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config

        if (error.response?.status === 401 && !originalRequest._retry) {
            if (isRefreshing) {
                return new Promise((resolve, reject) => {
                    failedQueue.push({ resolve, reject })
                }).then(() => {
                    return http(originalRequest)
                }).catch(err => {
                    return Promise.reject(err)
                })
            }

            originalRequest._retry = true
            isRefreshing = true

            try {
                await refreshAxios.post('/auth/refresh')
                processQueue(null)
                return http(originalRequest)
            } catch (refreshError) {
                processQueue(refreshError)

                const authStore = useAuthStore()
                authStore.clearAuth()
                router.push('/login')

                return Promise.reject(refreshError)
            } finally {
                isRefreshing = false
            }
        }

        const backendMessage = error.response?.data?.message
        if (backendMessage) {
            ElMessage.error(backendMessage)
        } else if (error.response?.status) {
            const statusMessages = {
                400: 'Bad Request',
                403: 'Access Denied',
                404: 'Not Found',
                500: 'Server Error'
            }
            const message = statusMessages[error.response.status] || `Error: ${error.response.status}`
            ElMessage.error(message)
        } else if (error.message) {
            ElMessage.error(error.message)
        }

        return Promise.reject(error)
    }
)

export default http