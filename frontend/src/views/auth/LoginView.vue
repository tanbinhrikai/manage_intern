<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useLocaleStore } from '@/locales/locale'
import { useToastStore } from '@/stores/toast'
import { Message, Lock, View, Hide } from '@element-plus/icons-vue'
import LanguageSwitcher from '@/components/ui/LanguageSwitcher.vue'

const router = useRouter()
const authStore = useAuthStore()
const localeStore = useLocaleStore()
const toastStore = useToastStore()

const t = computed(() => localeStore.t)

const form = ref({
  email: '',
  password: '',
  rememberMe: false
})

const isLoading = ref(false)
const showPassword = ref(false)
const errors = ref({})
const apiError = ref('')

const validateForm = () => {
  errors.value = {}
  
  if (!form.value.email) {
    errors.value.email = t.value('login.validation.emailRequired')
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)) {
    errors.value.email = t.value('login.validation.emailInvalid')
  }
  
  if (!form.value.password) {
    errors.value.password = t.value('login.validation.passwordRequired')
  } else if (form.value.password.length < 8) {
    errors.value.password = t.value('login.validation.passwordMinLength')
  }
  
  return Object.keys(errors.value).length === 0
}

const handleSubmit = async () => {
  if (!validateForm()) return
  
  isLoading.value = true
  apiError.value = ''
  
  try {
    await authStore.login(form.value.email, form.value.password)
    toastStore.success(t.value('toast.loginSuccess'))
    
    const role = authStore.userRole
    if (role === 'ADMIN') {
      router.push('/admin/dashboard')
    } else if (role === 'MENTOR') {
      router.push('/mentor/dashboard')
    } else {
      router.push('/login')
    }
  } catch (error) {
    console.error('Login error:', error)
    apiError.value = t.value('login.error.loginFailed')
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="language-wrapper">
      <LanguageSwitcher />
    </div>
    <div class="login-panel">
      <div class="login-card">
        <div class="brand-header">
          <h1 class="brand-name">InternHub</h1>
          <p class="brand-subtitle">{{ t('login.brandSubtitle') }}</p>
        </div>

        <h2 class="login-heading">{{ t('login.heading') }}</h2>

        <el-alert
          v-if="apiError"
          :title="apiError"
          type="error"
          show-icon
          :closable="false"
          class="error-alert"
        />

        <el-form 
          :model="form" 
          @submit.prevent="handleSubmit" 
          label-position="top"
          class="login-form"
        >
          <el-form-item :label="t('login.email')" :error="errors.email">
            <el-input
              v-model="form.email"
              type="email"
              :placeholder="'you@example.com'"
              :prefix-icon="Message"
              size="large"
            />
          </el-form-item>

          <el-form-item :label="t('login.password')" :error="errors.password">
            <el-input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="••••••••"
              :prefix-icon="Lock"
              size="large"
            >
              <template #suffix>
                <el-icon class="password-toggle" @click="showPassword = !showPassword">
                  <View v-if="!showPassword" />
                  <Hide v-else />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="form.rememberMe">Remember me</el-checkbox>
            <a href="#" class="forgot-link">{{ t('login.forgotPassword') }}</a>
          </div>

          <el-button
            type="primary"
            native-type="submit"
            :loading="isLoading"
            size="large"
            class="submit-btn"
          >
            {{ isLoading ? t('login.signingIn') : t('login.loginButton') }}
          </el-button>
        </el-form>

        <div class="footer-section">
          <p>Need assistance? <a href="#" class="support-link">Contact Support →</a></p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
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

.login-panel {
  width: 100%;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: #f8fafc;
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: #ffffff;
  padding: 40px 36px;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  border: 1px solid #f1f5f9;
}

.brand-header {
  text-align: center;
  margin-bottom: 32px;
}

.brand-name {
  font-family: 'Playfair Display', 'Georgia', serif;
  font-style: italic;
  font-size: 36px;
  font-weight: 700;
  color: #2c5282;
  margin: 0 0 8px 0;
}

.brand-subtitle {
  font-size: 12px;
  color: #64748b;
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 2px;
  font-weight: 500;
}

.login-heading {
  font-size: 22px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 24px 0;
}

.error-alert {
  margin-bottom: 20px;
}

.login-form {
  margin-bottom: 24px;
}

.login-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #374151;
  font-size: 14px;
}

.login-form :deep(.el-input__wrapper) {
  padding: 8px 12px;
  border-radius: 8px;
}

.login-form :deep(.el-input--large .el-input__wrapper) {
  padding: 12px 16px;
}

.password-toggle {
  cursor: pointer;
  color: #9ca3af;
  transition: color 0.2s;
}

.password-toggle:hover {
  color: #374151;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.forgot-link {
  font-size: 14px;
  color: #2c5282;
  text-decoration: none;
  font-weight: 500;
}

.forgot-link:hover {
  color: #1e3a5f;
  text-decoration: underline;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, #2c5282 0%, #1e3a5f 100%);
  border: none;
  border-radius: 10px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(44, 82, 130, 0.35);
}

.footer-section {
  text-align: center;
  padding-top: 24px;
  border-top: 1px solid #f1f5f9;
}

.footer-section p {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.support-link {
  color: #2c5282;
  font-weight: 500;
  text-decoration: none;
}

.support-link:hover {
  text-decoration: underline;
}
</style>
