<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useLocaleStore } from '@/locales/locale'
import { useToastStore } from '@/stores/toast'
import LanguageSwitcher from '@/components/ui/LanguageSwitcher.vue'

const router = useRouter()
const authStore = useAuthStore()
const localeStore = useLocaleStore()
const toastStore = useToastStore()
const t = computed(() => localeStore.t)

const showUserDropdown = ref(false)

const toggleUserDropdown = () => {
  showUserDropdown.value = !showUserDropdown.value
}

const closeDropdowns = () => {
  showUserDropdown.value = false
}

const goToProfile = () => {
  showUserDropdown.value = false
  router.push('/mentor/profile')
}

const handleLogout = () => {
  showUserDropdown.value = false
  toastStore.success(t.value('toast.logoutSuccess'))
  authStore.logout()
}
</script>

<template>
  <header class="header" @click="closeDropdowns">
    <div class="header-content">
      <h1 class="page-title">{{ t('mentorDashboard.title') }}</h1>
      
      <div class="header-right" @click.stop>
        
        <div class="language-wrapper">
          <LanguageSwitcher />
        </div>
        
        <div class="user-menu">
          <button class="user-btn" @click="toggleUserDropdown">
            <svg class="user-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <span class="user-name">{{ authStore.user?.fullName || 'Mentor' }}</span>
            <svg class="chevron-icon" :class="{ rotate: showUserDropdown }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M6 9l6 6 6-6"/>
            </svg>
          </button>
          <div v-if="showUserDropdown" class="dropdown user-dropdown">
            <button class="dropdown-item" @click="goToProfile">
              <svg class="menu-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              {{ t('header.profile') }}
            </button>
            <button class="dropdown-item logout-item" @click="handleLogout">
              <svg class="menu-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                <polyline points="16 17 21 12 16 7"/>
                <line x1="21" y1="12" x2="9" y2="12"/>
              </svg>
              {{ t('header.logout') }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<style scoped>
.header {
  height: 60px;
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  padding: 0 24px;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.notification-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.notification-btn:hover {
  background: #f1f5f9;
}

.bell-icon {
  width: 20px;
  height: 20px;
  color: #64748b;
}

.user-menu {
  position: relative;
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: transparent;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #374151;
  transition: all 0.2s;
}

.user-btn:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
}

.user-icon {
  width: 18px;
  height: 18px;
  color: #64748b;
}

.chevron-icon {
  width: 14px;
  height: 14px;
  color: #94a3b8;
  transition: transform 0.2s;
}

.chevron-icon.rotate {
  transform: rotate(180deg);
}

.user-name {
  font-weight: 500;
}

.dropdown {
  position: absolute;
  top: calc(100% + 4px);
  right: 0;
  min-width: 160px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  z-index: 100;
}

.dropdown-item {
  width: 100%;
  padding: 10px 14px;
  text-align: left;
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 13px;
  color: #374151;
  transition: background 0.15s;
  display: flex;
  align-items: center;
  gap: 10px;
}

.dropdown-item:hover {
  background: #f3f4f6;
}

.logout-item:hover {
  background: #fef2f2;
  color: #dc2626;
}

.menu-icon {
  width: 16px;
  height: 16px;
  color: #64748b;
}

.logout-item:hover .menu-icon {
  color: #dc2626;
}
</style>
