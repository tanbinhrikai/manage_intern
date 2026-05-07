import { defineStore } from 'pinia'
import { ElNotification } from 'element-plus'

export const useToastStore = defineStore('toast', () => {
    const success = (message, duration = 3000) => {
        ElNotification({
            title: 'Success',
            message,
            type: 'success',
            duration,
            position: 'bottom-right'
        })
    }

    const error = (message, duration = 3000) => {
        ElNotification({
            title: 'Error',
            message,
            type: 'error',
            duration,
            position: 'bottom-right'
        })
    }

    const info = (message, duration = 3000) => {
        ElNotification({
            title: 'Info',
            message,
            type: 'info',
            duration,
            position: 'bottom-right'
        })
    }

    const warning = (message, duration = 3000) => {
        ElNotification({
            title: 'Warning',
            message,
            type: 'warning',
            duration,
            position: 'bottom-right'
        })
    }

    return {
        success,
        error,
        info,
        warning
    }
})
