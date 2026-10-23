<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useLocaleStore } from '@/locales/locale'
import LanguageSwitcher from '@/components/ui/LanguageSwitcher.vue'
import ChangePasswordForm from '@/components/profile/ChangePasswordForm.vue'
import { InfoFilled, SwitchButton } from '@element-plus/icons-vue'

const authStore = useAuthStore()
const localeStore = useLocaleStore()

const t = computed(() => localeStore.t)

const handleLogout = async () => {
  await authStore.logout()
}
</script>

<template>
  <div class="force-change-page">
    <div class="language-wrapper">
      <LanguageSwitcher />
    </div>

    <div class="force-change-panel">
      <div class="force-change-card">
        <div class="brand-header">
          <h1 class="brand-name">InternHub</h1>
          <p class="brand-subtitle">{{ t('login.brandSubtitle') }}</p>
        </div>

        <h2 class="page-heading">{{ t('forceChangePassword.heading') }}</h2>

        <div class="info-banner">
          <el-icon class="info-icon"><InfoFilled /></el-icon>
          <span class="info-text">{{ t('forceChangePassword.description') }}</span>
        </div>

        <ChangePasswordForm :show-cancel="false" />

        <div class="footer-actions">
          <el-button link type="info" @click="handleLogout" class="logout-btn">
            <el-icon><SwitchButton /></el-icon>
            <span>{{ t('forceChangePassword.logout') }}</span>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.force-change-page {
  min-height: 100vh;
  display: flex;
  position: relative;
}

.language-wrapper {
  position: absolute;
  top: 24px;
  right: 24px;
  z-index: 10;
}

.force-change-panel {
  width: 100%;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: #f8fafc;
}

.force-change-card {
  width: 100%;
  max-width: 440px;
  background: #ffffff;
  padding: 36px 32px;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  border: 1px solid #f1f5f9;
}

.brand-header {
  text-align: center;
  margin-bottom: 24px;
}

.brand-name {
  font-family: 'Playfair Display', 'Georgia', serif;
  font-style: italic;
  font-size: 32px;
  font-weight: 700;
  color: #2c5282;
  margin: 0 0 6px 0;
}

.brand-subtitle {
  font-size: 11px;
  color: #64748b;
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 2px;
  font-weight: 500;
}

.page-heading {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 16px 0;
  text-align: center;
}

.info-banner {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  background-color: #eff6ff;
  border: 1px solid #bfdbfe;
  color: #1e40af;
  padding: 10px 14px;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 13px;
  line-height: 1.45;
}

.info-icon {
  font-size: 16px;
  margin-top: 2px;
  flex-shrink: 0;
}

.footer-actions {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  justify-content: center;
}

.logout-btn {
  font-size: 13px;
  color: #64748b;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.logout-btn:hover {
  color: #dc2626;
}
</style>

