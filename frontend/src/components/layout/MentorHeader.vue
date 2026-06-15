<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useLocaleStore } from '@/locales/locale'
import { useToastStore } from '@/stores/toast'
import { EventSourcePolyfill } from 'event-source-polyfill'
import LanguageSwitcher from '@/components/ui/LanguageSwitcher.vue'
import { getMyNotifications, getUnreadNotificationCount, markNotificationAsRead } from '@/api/notification'

const props = defineProps({
  pageTitle: {
    type: String,
    default: ''
  }
})





const router = useRouter()
const authStore = useAuthStore()
const localeStore = useLocaleStore()
const toastStore = useToastStore()
const t = computed(() => localeStore.t)

const showUserDropdown = ref(false)
const showNotificationDropdown = ref(false)
const notifications = ref([])
const unreadCount = ref(0)
const loadingNotifications = ref(false)

let eventSource = null

onMounted(() => {
  fetchUnreadCount()

  eventSource = new EventSourcePolyfill('http://localhost:8080/notifications/subscribe', {withCredentials: true})

  eventSource.addEventListener('notification', (event) => {
  const notification = JSON.parse(event.data)
  console.log('Received notification via SSE:', notification)

  if (showNotificationDropdown.value) {
    notifications.value.unshift(notification)

    if (notifications.value.length > 5) {
      notifications.value.pop()
    }
  }

  if (!notification.read) {
    unreadCount.value++
  }
})
  eventSource.onerror = (err) => {
    console.error('SSE error:', err)
  }
})

onUnmounted(() => {
  if (eventSource) {
    eventSource.close()
  }
})

const toggleUserDropdown = () => {
  showUserDropdown.value = !showUserDropdown.value
  showNotificationDropdown.value = false
}

const toggleNotificationDropdown = async () => {
  showNotificationDropdown.value = !showNotificationDropdown.value
  showUserDropdown.value = false
  if (showNotificationDropdown.value) {
    await fetchNotifications()
  }
}

const closeDropdowns = () => {
  showUserDropdown.value = false
  showNotificationDropdown.value = false
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

async function fetchUnreadCount() {
  try {
    const res = await getUnreadNotificationCount()
    unreadCount.value = res.data?.data?.count ?? res.data?.data ?? 0
  } catch (e) {
    // silently ignore - keep previous count
  }
}

async function fetchNotifications() {
  loadingNotifications.value = true
  try {
    const res = await getMyNotifications({ page: 0, limit: 5 })
    notifications.value = res.data?.data?.items || []
  } catch (e) {
    notifications.value = []
  } finally {
    loadingNotifications.value = false
  }
}

async function handleNotificationClick(notification) {
  if (!notification.read) {
    try {
      await markNotificationAsRead(notification.id)
      notification.read = true
      if (unreadCount.value > 0) unreadCount.value -= 1
    } catch (e) {
      // ignore
    }
  }
  if (notification.link) {
    showNotificationDropdown.value = false
    router.push(notification.link)
  }
}

function formatNotificationTime(isoString) {
  if (!isoString) return ''
  const date = new Date(isoString)
  const now = new Date()
  const diffMs = now - date
  const diffMinutes = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMinutes / 60)
  const diffDays = Math.floor(diffHours / 24)

  if (diffMinutes < 1) return t.value('header.justNow') || 'Vừa xong'
  if (diffMinutes < 60) return `${diffMinutes} ${t.value('header.minutesAgo') || 'phút trước'}`
  if (diffHours < 24) return `${diffHours} ${t.value('header.hoursAgo') || 'giờ trước'}`
  if (diffDays < 7) return `${diffDays} ${t.value('header.daysAgo') || 'ngày trước'}`

  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const goToAllNotifications = () => {
  showNotificationDropdown.value = false
  router.push('/mentor/notifications')
}



</script>

<template>
  <header class="header" @click="closeDropdowns">
    <div class="header-content">
      <h1 class="page-title">{{ pageTitle || t('mentorDashboard.title') }}</h1>
      
      <div class="header-right" @click.stop>

        <div class="language-wrapper">
          <LanguageSwitcher />
        </div>

        <div class="notification-menu">
          <button class="notification-btn" @click="toggleNotificationDropdown">
            <svg class="bell-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9"/>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
            </svg>
            <span v-if="unreadCount > 0" class="notification-badge">
              {{ unreadCount > 99 ? '99+' : unreadCount }}
            </span>
          </button>
          <div v-if="showNotificationDropdown" class="dropdown notification-dropdown">
            <div class="dropdown-title">{{ t('header.notifications') || 'Thông báo' }}</div>
            <div class="notification-list">
              <div v-if="loadingNotifications" class="notification-empty">
                {{ t('common.loading') || 'Đang tải...' }}
              </div>
              <div v-else-if="notifications.length === 0" class="notification-empty">
                {{ t('header.noNotifications') || 'Không có thông báo mới' }}
              </div>
              <button
                v-for="item in notifications"
                :key="item.id"
                class="notification-item"
                :class="{ unread: !item.read }"
                @click="handleNotificationClick(item)"
              >
                <span class="notification-dot" v-if="!item.read"></span>
                <div class="notification-content">
                  <div class="notification-message">{{ item.title }}</div>
                  <div class="notification-detail">{{ item.content }}</div>
                  <div class="notification-time">{{ formatNotificationTime(item.createdAt) }}</div>
                </div>
              </button>
            </div>
            <button class="dropdown-footer" @click="goToAllNotifications">
              {{ t('header.viewAllNotifications') || 'Xem tất cả thông báo' }}
            </button>
          </div>
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

.notification-menu {
  position: relative;
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
  position: relative;
}

.notification-btn:hover {
  background: #f1f5f9;
}

.bell-icon {
  width: 20px;
  height: 20px;
  color: #64748b;
}

.notification-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background: #ef4444;
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  line-height: 16px;
  text-align: center;
  border-radius: 8px;
  box-shadow: 0 0 0 2px #ffffff;
}

.notification-dropdown {
  width: 480px;
  max-width: 90vw;
  max-height: 70vh;
  display: flex;
  flex-direction: column;
  padding: 0;
}

.notification-dropdown .notification-list {
  overflow-y: auto;
  max-height: 50vh;
}

.dropdown-title {
  padding: 20px 24px;
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  border-bottom: 1px solid #f1f5f9;
}

.notification-empty {
  padding: 48px 24px;
  text-align: center;
  font-size: 15px;
  color: #94a3b8;
}

.notification-item {
  width: 100%;
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 18px 24px;
  background: transparent;
  border: none;
  border-bottom: 1px solid #f8fafc;
  cursor: pointer;
  text-align: left;
  transition: background 0.15s;
}

.notification-item:hover {
  background: #f8fafc;
}

.notification-item.unread {
  background: #f0f9ff;
}

.notification-item.unread:hover {
  background: #e0f2fe;
}

.notification-dot {
  flex-shrink: 0;
  width: 11px;
  height: 11px;
  border-radius: 50%;
  background: #3b82f6;
  margin-top: 7px;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-message {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  line-height: 1.5;
  word-break: break-word;
}

.notification-detail {
  font-size: 14px;
  color: #64748b;
  line-height: 1.5;
  margin-top: 6px;
  word-break: break-word;
}

.notification-time {
  font-size: 13px;
  color: #94a3b8;
  margin-top: 8px;
}

.dropdown-footer {
  width: 100%;
  padding: 16px 24px;
  background: transparent;
  border: none;
  border-top: 1px solid #f1f5f9;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  color: #3b82f6;
  text-align: center;
  transition: background 0.15s;
}

.dropdown-footer:hover {
  background: #f8fafc;
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
