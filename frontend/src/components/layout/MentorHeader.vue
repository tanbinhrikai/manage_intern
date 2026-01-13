<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useLocaleStore } from '@/locales/locale'

const router = useRouter()
const authStore = useAuthStore()
const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)

const showLanguageDropdown = ref(false)
const showUserDropdown = ref(false)

const toggleLanguageDropdown = () => {
  showLanguageDropdown.value = !showLanguageDropdown.value
  showUserDropdown.value = false
}

const toggleUserDropdown = () => {
  showUserDropdown.value = !showUserDropdown.value
  showLanguageDropdown.value = false
}

const changeLanguage = (locale) => {
  localeStore.setLocale(locale)
  showLanguageDropdown.value = false
}

const closeDropdowns = () => {
  showLanguageDropdown.value = false
  showUserDropdown.value = false
}

const goToProfile = () => {
  showUserDropdown.value = false
  router.push('/profile')
}

const handleLogout = () => {
  showUserDropdown.value = false
  authStore.logout()
}
</script>

<template>
  <header class="header" @click="closeDropdowns">
    <div class="header-content">
      <div class="header-right" @click.stop>
        <div class="user-greeting">
          {{ t('header.greeting') }}, {{ authStore.user?.fullName || 'User' }}
        </div>
        <div class="user-menu">
          <button class="user-btn" @click="toggleUserDropdown">
            <div class="user-avatar">
              <svg class="user-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
            </div>
          </button>
          <div v-if="showUserDropdown" class="dropdown user-dropdown">
            <button class="dropdown-item" @click="goToProfile">
              <svg class="menu-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              {{ t('header.profile') }}
            </button>
            <div class="dropdown-divider"></div>
            <div class="language-section">
              <button
                v-for="locale in localeStore.availableLocales"
                :key="locale"
                @click="changeLanguage(locale)"
                :class="['dropdown-item', { active: locale === localeStore.currentLocale }]"
              >
                {{ t('language.' + locale) }}
              </button>
            </div>
            <div class="dropdown-divider"></div>
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
  height: 56px;
  background: #2c3e50;
  display: flex;
  align-items: center;
  padding: 0 24px;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-greeting {
  color: #ffffff;
  font-size: 14px;
}

.user-menu {
  position: relative;
}

.user-btn {
  display: flex;
  align-items: center;
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 0;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #2ecc71;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-icon {
  width: 20px;
  height: 20px;
  color: #ffffff;
}

.dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 180px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
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

.dropdown-item.active {
  background: #eff6ff;
  color: #3b82f6;
  font-weight: 500;
}

.dropdown-divider {
  height: 1px;
  background: #e5e7eb;
  margin: 4px 0;
}

.logout-item:hover {
  background: #fef2f2;
  color: #dc2626;
}

.menu-icon {
  width: 16px;
  height: 16px;
  color: #6b7280;
}

.logout-item:hover .menu-icon {
  color: #dc2626;
}
</style>
