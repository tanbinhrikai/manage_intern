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

const form = ref({
  email: '',
  password: ''
})

const isLoading = ref(false)
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
    router.push('/dashboard')
  } catch (error) {
    console.error('Login error:', error)
    if (error.response?.data?.message) {
      apiError.value = error.response.data.message
    } else if (error.message) {
      apiError.value = error.message
    } else {
      apiError.value = t.value('login.error.loginFailed')
    }
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="language-wrapper">
      <LanguageSwitcher />
    </div>

    <div class="login-card">
      <div class="brand-header">
        <h1 class="brand-name">{{ t('login.brandName') }}</h1>
        <p class="brand-subtitle">{{ t('login.brandSubtitle') }}</p>
      </div>

      <h2 class="login-heading">{{ t('login.heading') }}</h2>

      <div v-if="apiError" class="error-alert">
        {{ apiError }}
      </div>

      <form @submit.prevent="handleSubmit" class="login-form" novalidate>
        <div class="form-group">
          <label for="email" class="form-label">{{ t('login.email') }}</label>
          <input
            id="email"
            v-model="form.email"
            type="text"
            autocomplete="email"
            :class="['form-input', { 'input-error': errors.email }]"
          />
          <p v-if="errors.email" class="error-text">{{ errors.email }}</p>
        </div>

        <div class="form-group">
          <label for="password" class="form-label">{{ t('login.password') }}</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            autocomplete="current-password"
            :class="['form-input', { 'input-error': errors.password }]"
          />
          <p v-if="errors.password" class="error-text">{{ errors.password }}</p>
        </div>

        <button
          type="submit"
          :disabled="isLoading"
          class="submit-btn"
        >
          <svg
            v-if="isLoading"
            class="spinner"
            fill="none"
            viewBox="0 0 24 24"
          >
            <circle class="spinner-circle" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="spinner-path" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <span>{{ isLoading ? t('login.signingIn') : t('login.loginButton') }}</span>
        </button>
      </form>

      <div class="footer-link">
        <a href="#">{{ t('login.forgotPassword') }}</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8f9fa;
  padding: 20px;
  position: relative;
}

.language-wrapper {
  position: absolute;
  top: 20px;
  right: 20px;
}

.login-card {
  width: 100%;
  max-width: 360px;
  background: #ffffff;
  padding: 40px 32px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}

.brand-header {
  text-align: center;
  margin-bottom: 32px;
}

.brand-name {
  font-family: 'Georgia', serif;
  font-style: italic;
  font-size: 32px;
  font-weight: 400;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.brand-subtitle {
  font-size: 14px;
  color: #6c757d;
  margin: 0;
}

.login-heading {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  text-align: center;
  margin: 0 0 24px 0;
}

.error-alert {
  background-color: #fee2e2;
  border: 1px solid #fecaca;
  color: #dc2626;
  padding: 12px;
  border-radius: 6px;
  font-size: 14px;
  margin-bottom: 16px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.form-input {
  width: 100%;
  padding: 12px 14px;
  font-size: 14px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background-color: #ffffff;
  color: #1f2937;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-input.input-error {
  border-color: #ef4444;
}

.form-input::placeholder {
  color: #9ca3af;
}

.error-text {
  font-size: 12px;
  color: #ef4444;
  margin: 0;
}

.submit-btn {
  width: 100%;
  padding: 12px 16px;
  font-size: 15px;
  font-weight: 600;
  color: #ffffff;
  background: linear-gradient(135deg, #4a9fd1 0%, #3b82c4 100%);
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: transform 0.2s, box-shadow 0.2s;
  margin-top: 8px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.spinner {
  width: 18px;
  height: 18px;
  animation: spin 1s linear infinite;
}

.spinner-circle {
  opacity: 0.25;
}

.spinner-path {
  opacity: 0.75;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.footer-link {
  text-align: center;
  margin-top: 20px;
}

.footer-link a {
  font-size: 14px;
  color: #6b7280;
  text-decoration: none;
  transition: color 0.2s;
}

.footer-link a:hover {
  color: #3b82f6;
}
</style>
