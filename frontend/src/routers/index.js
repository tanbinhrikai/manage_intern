import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/auth/LoginView.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/admin',
        name: 'AdminDashboard',
        component: () => import('@/views/dashboard/AdminDashboardView.vue'),
        meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
        path: '/mentor',
        name: 'MentorDashboard',
        component: () => import('@/views/dashboard/MentorDashboardView.vue'),
        meta: { requiresAuth: true, role: 'MENTOR' }
    },
    {
        path: '/',
        redirect: '/login'
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    const authStore = useAuthStore()
    const isAuthenticated = authStore.isAuthenticated
    const userRole = authStore.userRole

    if (to.meta.requiresAuth && !isAuthenticated) {
        next({ name: 'Login' })
        return
    }

    if (to.meta.role && to.meta.role !== userRole) {
        if (userRole === 'ADMIN') {
            next({ name: 'AdminDashboard' })
        } else if (userRole === 'MENTOR') {
            next({ name: 'MentorDashboard' })
        } else {
            next({ name: 'Login' })
        }
        return
    }

    if (to.name === 'Login' && isAuthenticated) {
        if (userRole === 'ADMIN') {
            next({ name: 'AdminDashboard' })
        } else if (userRole === 'MENTOR') {
            next({ name: 'MentorDashboard' })
        } else {
            next()
        }
        return
    }

    next()
})

export default router
