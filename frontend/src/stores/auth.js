import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as authApi from '@/api/auth'
import router from '@/routers'

const USER_KEY = 'user_data'

export const useAuthStore = defineStore('auth', () => {
    const user = ref(JSON.parse(localStorage.getItem(USER_KEY) || 'null'))

    const isAuthenticated = computed(() => !!user.value)
    const userRole = computed(() => user.value?.role?.roleName || null)

    const login = async (email, password) => {
        const response = await authApi.login(email, password)
        const { data } = response.data

        if (data.userResponse) {
            user.value = data.userResponse
            localStorage.setItem(USER_KEY, JSON.stringify(data.userResponse))
        }

        return response.data
    }

    const refreshToken = async () => {
        try {
            await authApi.refreshToken()
        } catch (error) {
            logout()
            throw error
        }
    }

    const logout = async () => {
        try {
            await authApi.logout()
        } catch (error) {
            console.error(error)
        } finally {
            user.value = null
            localStorage.removeItem(USER_KEY)
            router.push('/login')
        }
    }

    return {
        user,
        isAuthenticated,
        userRole,
        login,
        logout,
        refreshToken
    }
})