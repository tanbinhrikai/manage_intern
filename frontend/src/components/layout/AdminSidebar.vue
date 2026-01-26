<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useLocaleStore } from '@/locales/locale'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const localeStore = useLocaleStore()
const authStore = useAuthStore()
const t = computed(() => localeStore.t)

const menuItems = [
  { key: 'dashboard', path: '/admin/dashboard', icon: 'dashboard' },
  { key: 'interns', path: '/admin/interns', icon: 'interns' },
  { key: 'mentors', path: '/admin/mentors', icon: 'mentors' },
  { key: 'hrs', path: '/admin/hrs', icon: 'hr' },
  { key: 'departments', path: '/admin/departments', icon: 'department' },
  { key: 'positions', path: '/admin/positions', icon: 'position' },
  { key: 'evaluationCriteria', path: '/admin/evaluation-criteria', icon: 'criteria' },
  { key: 'evaluationSessions', path: '/admin/evaluation-sessions', icon: 'evaluationSession' },
  { key: 'systemConfig', path: '/admin/system-config', icon: 'config' },
  { key: 'auditLog', path: '/admin/audit-log', icon: 'audit' }
]

const isActive = (path) => route.path === path || route.path.startsWith(path + '/')

const userInitials = computed(() => {
  const name = authStore.user?.fullName || 'AD'
  const parts = name.split(' ')
  if (parts.length >= 2) {
    return parts[0][0] + parts[parts.length - 1][0]
  }
  return name.substring(0, 2).toUpperCase()
})
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <span class="brand-name">InternHub</span>
    </div>
    <nav class="sidebar-nav">
      <router-link
        v-for="item in menuItems"
        :key="item.key"
        :to="item.path"
        :class="['nav-item', { active: isActive(item.path) }]"
      >
        <svg v-if="item.icon === 'dashboard'" class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="3" y="3" width="7" height="7" rx="1"/>
          <rect x="14" y="3" width="7" height="7" rx="1"/>
          <rect x="14" y="14" width="7" height="7" rx="1"/>
          <rect x="3" y="14" width="7" height="7" rx="1"/>
        </svg>
        <svg v-else-if="item.icon === 'interns'" class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
          <circle cx="9" cy="7" r="4"/>
          <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
          <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
        </svg>
        <svg v-else-if="item.icon === 'mentors'" class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
          <circle cx="12" cy="7" r="4"/>
        </svg>
        <svg v-else-if="item.icon === 'hr'" class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
          <circle cx="9" cy="7" r="4"/>
          <path d="M22 21v-2a4 4 0 0 0-3-3.87"/>
          <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
        </svg>
        <svg v-else-if="item.icon === 'department'" class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M3 21h18"/>
          <path d="M9 8h1"/>
          <path d="M9 12h1"/>
          <path d="M9 16h1"/>
          <path d="M14 8h1"/>
          <path d="M14 12h1"/>
          <path d="M14 16h1"/>
          <path d="M5 21V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v16"/>
        </svg>
        <svg v-else-if="item.icon === 'position'" class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="2" y="7" width="20" height="14" rx="2" ry="2"/>
          <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/>
        </svg>
        <svg v-else-if="item.icon === 'criteria'" class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
           <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
           <polyline points="14 2 14 8 20 8"></polyline>
           <path d="M9 15h6"></path>
           <path d="M9 19h6"></path>
           <path d="M9 11h6"></path>
        </svg>
        <svg v-else-if="item.icon === 'evaluationSession'" class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 11l3 3L22 4"/>
          <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
        </svg>
        <svg v-else-if="item.icon === 'config'" class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="3"/>
          <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/>
        </svg>
        <svg v-else-if="item.icon === 'audit'" class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <polyline points="14 2 14 8 20 8"/>
          <line x1="16" y1="13" x2="8" y2="13"/>
          <line x1="16" y1="17" x2="8" y2="17"/>
          <polyline points="10 9 9 9 8 9"/>
        </svg>
        <span>{{ t('sidebar.' + item.key) }}</span>
      </router-link>
    </nav>
  </aside>
</template>

<style scoped>
.sidebar {
  width: 220px;
  min-height: 100vh;
  background: #f8fafc;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  border-right: 1px solid #e2e8f0;
}

.sidebar-header {
  padding: 24px 20px;
}

.brand-name {
  font-family: 'Inter', sans-serif;
  font-weight: 700;
  font-style: italic;
  font-size: 22px;
  color: #3b82f6;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  padding: 8px 12px;
  gap: 4px;
  flex: 1;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  color: #64748b;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.2s;
}

.nav-item:hover {
  background: #f1f5f9;
  color: #334155;
}

.nav-item.active {
  background: #3b82f6;
  color: #ffffff;
}

.nav-item.active .nav-icon {
  color: #ffffff;
}

.nav-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  color: inherit;
}

.sidebar-footer {
  padding: 16px 12px;
  border-top: 1px solid #e2e8f0;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  border-radius: 8px;
}

.avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 14px;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.user-role {
  font-size: 12px;
  color: #64748b;
}
</style>
