<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  User,
  Message,
  Calendar,
  OfficeBuilding,
  Lock,
  Key,
  Check,
  Close
} from '@element-plus/icons-vue'
import { useToastStore } from '@/stores/toast'
import { useLocaleStore } from '@/locales/locale'
import { useAuthStore } from '@/stores/auth'
import AdminLayout from '@/layouts/dashboard/AdminLayout.vue'
import MentorLayout from '@/layouts/dashboard/MentorLayout.vue'
import { getMyInfo, changePassword } from '@/api/user'
import { useDateFormat, useLoading } from '@/composables'
import router from '@/routers'

const localeStore = useLocaleStore()
const authStore = useAuthStore()
const toastStore = useToastStore()
const t = (key) => localeStore.t(key)

const profile = ref(null)
const loading = ref(true)
const error = ref('')

const isMentor = computed(() => authStore.userRole === 'MENTOR')
const LayoutComponent = computed(() => isMentor.value ? MentorLayout : AdminLayout)

const { formatDate } = useDateFormat()
const { loading: submitting, withLoading } = useLoading()

// Change Password Dialog & Form State
const dialogVisible = ref(false)
const formRef = ref(null)
const form = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const oldPasswordError = ref('')
const isNewPasswordFocused = ref(false)

// Password requirements checklist
const criteria = computed(() => {
  const pwd = form.value.newPassword || ''
  return {
    minLength: pwd.length >= 8 && pwd.length <= 72,
    hasUppercase: /[A-Z]/.test(pwd),
    hasLowercase: /[a-z]/.test(pwd),
    hasNumber: /\d/.test(pwd)
  }
})

// Validation rules
const rules = computed(() => ({
  oldPassword: [
    {
      required: true,
      message: t('profile.rules.oldPasswordRequired'),
      trigger: 'blur'
    }
  ],
  newPassword: [
    {
      required: true,
      message: t('profile.rules.newPasswordRequired'),
      trigger: 'blur'
    },
    {
      validator: (rule, value, callback) => {
        if (!value) return callback()
        if (value.length < 8 || value.length > 72) {
          return callback(new Error(t('profile.rules.newPasswordLength')))
        }
        const hasUpper = /[A-Z]/.test(value)
        const hasLower = /[a-z]/.test(value)
        const hasDigit = /\d/.test(value)
        if (!hasUpper || !hasLower || !hasDigit) {
          return callback(new Error(t('profile.rules.newPasswordPattern')))
        }
        if (form.value.oldPassword && value === form.value.oldPassword) {
          return callback(new Error(t('profile.rules.newPasswordSameAsOld')))
        }
        callback()
      },
      trigger: ['blur', 'change']
    }
  ],
  confirmPassword: [
    {
      required: true,
      message: t('profile.rules.confirmPasswordRequired'),
      trigger: 'blur'
    },
    {
      validator: (rule, value, callback) => {
        if (!value) return callback()
        if (value !== form.value.newPassword) {
          return callback(new Error(t('profile.rules.confirmPasswordMismatch')))
        }
        callback()
      },
      trigger: ['blur', 'change']
    }
  ]
}))

function handleOldPasswordInput() {
  if (oldPasswordError.value) {
    oldPasswordError.value = ''
  }
  
  if (form.value.newPassword) {
    formRef.value?.validateField('newPassword').catch(() => {})
  }
}

function handleNewPasswordInput() {
  if (form.value.confirmPassword) {
    formRef.value?.validateField('confirmPassword').catch(() => {})
  }
}

function resetForm() {
  form.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  oldPasswordError.value = ''
  isNewPasswordFocused.value = false
  formRef.value?.resetFields()
}

function handleBeforeClose(done) {
  resetForm()
  dialogVisible.value = false
  if (typeof done === 'function') {
    done()
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  await withLoading(async () => {
    try {
      await changePassword({
        oldPassword: form.value.oldPassword,
        newPassword: form.value.newPassword,
        confirmPassword: form.value.confirmPassword
      }, { skipErrorToast: true })

      toastStore.success(t('profile.changePasswordSuccess'), 4000)

      resetForm()
      dialogVisible.value = false

      authStore.clearAuth()
      router.push('/login')
    } catch (err) {
      const errorCode = err.response?.data?.code

      if (errorCode === 'PASSWORD_NOT_CORRECT' ) {
        oldPasswordError.value = t('profile.rules.oldPasswordIncorrect')
      } else {
        toastStore.error(t('profile.error'))
      }
    }
  })
}

async function fetchProfile() {
  loading.value = true
  error.value = ''
  try {
    const res = await getMyInfo()
    profile.value = res.data?.data || null
  } catch (err) {
    console.error('Failed to load profile:', err)
    error.value = t('profile.error')
    toastStore.error(t('profile.error'))
  } finally {
    loading.value = false
  }
}

onMounted(fetchProfile)
</script>

<template>
  <component :is="LayoutComponent">
    <div class="profile-page">
      <h1 class="page-title">{{ t('profile.title') }}</h1>

      <div v-if="loading" class="loading-state" v-loading="true">
        <p>{{ t('profile.loading') }}</p>
      </div>

      <div v-else-if="error" class="error-state">
        <el-alert :title="error" type="error" show-icon />
      </div>

      <template v-else-if="profile">
        <el-row :gutter="24">
          <el-col :xs="24" :lg="16">
            <!-- Personal Information Card -->
            <el-card class="info-card" shadow="never">
              <template #header>
                <div class="card-header">
                  <el-icon><User /></el-icon>
                  <span>{{ t('profile.personalInfo') }}</span>
                </div>
              </template>

              <el-descriptions :column="2" border>
                <el-descriptions-item :span="2">
                  <template #label>
                    <div class="label-with-icon">
                      <el-icon><User /></el-icon>
                      {{ t('profile.fullName') }}
                    </div>
                  </template>
                  <span class="value-text">{{ profile.fullName }}</span>
                </el-descriptions-item>

                <el-descriptions-item :span="2">
                  <template #label>
                    <div class="label-with-icon">
                      <el-icon><Message /></el-icon>
                      {{ t('profile.email') }}
                    </div>
                  </template>
                  {{ profile.email }}
                </el-descriptions-item>

                <el-descriptions-item>
                  <template #label>
                    <div class="label-with-icon">
                      <el-icon><Calendar /></el-icon>
                      {{ t('profile.dateOfBirth') }}
                    </div>
                  </template>
                  {{ formatDate(profile.dateOfBirth) }}
                </el-descriptions-item>

                <el-descriptions-item>
                  <template #label>
                    <div class="label-with-icon">
                      <el-icon><OfficeBuilding /></el-icon>
                      {{ t('profile.department') }}
                    </div>
                  </template>
                  <el-tag v-if="profile.department" type="info">
                    {{ profile.department.title }}
                  </el-tag>
                  <span v-else>-</span>
                </el-descriptions-item>

                <el-descriptions-item>
                  <template #label>
                    {{ t('profile.role') }}
                  </template>
                  <el-tag type="primary">
                    {{ profile.role?.roleName || '-' }}
                  </el-tag>
                </el-descriptions-item>

                <el-descriptions-item>
                  <template #label>
                    {{ t('profile.status') }}
                  </template>
                  <el-tag :type="profile.active ? 'success' : 'danger'">
                    {{ profile.active ? t('profile.active') : t('profile.inactive') }}
                  </el-tag>
                </el-descriptions-item>
              </el-descriptions>
            </el-card>

            <!-- Security & Password Card -->
            <el-card class="info-card security-card" shadow="never">
              <template #header>
                <div class="card-header">
                  <el-icon><Lock /></el-icon>
                  <span>{{ t('profile.security') }}</span>
                </div>
              </template>

              <div class="security-content">
                <div class="security-info">
                  <div class="security-title">{{ t('profile.changePassword') }}</div>
                  <div class="security-desc">{{ t('profile.securityDesc') }}</div>
                </div>
                <el-button type="primary" @click="dialogVisible = true">
                  <el-icon class="el-icon--left"><Key /></el-icon>
                  {{ t('profile.changePassword') }}
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- Change Password Dialog -->
        <el-dialog
          v-model="dialogVisible"
          :title="t('profile.changePassword')"
          width="500px"
          destroy-on-close
          :close-on-click-modal="false"
          :before-close="handleBeforeClose"
          class="change-password-dialog"
        >
          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-position="top"
            status-icon
          >
            <el-form-item
              :label="t('profile.oldPassword')"
              prop="oldPassword"
              :error="oldPasswordError"
            >
              <el-input
                v-model="form.oldPassword"
                type="password"
                show-password
                :placeholder="t('profile.oldPasswordPlaceholder')"
                autocomplete="current-password"
                @input="handleOldPasswordInput"
              />
            </el-form-item>

            <el-form-item
              :label="t('profile.newPassword')"
              prop="newPassword"
              class="new-password-item"
            >
              <el-input
                v-model="form.newPassword"
                type="password"
                show-password
                :placeholder="t('profile.newPasswordPlaceholder')"
                autocomplete="new-password"
                @focus="isNewPasswordFocused = true"
                @blur="isNewPasswordFocused = false"
                @input="handleNewPasswordInput"
              />

              <!-- Password strength criteria checklist: only displayed on focus -->
              <transition name="el-zoom-in-top">
                <div v-show="isNewPasswordFocused" class="password-criteria">
                  <div class="criteria-title">{{ t('profile.passwordRequirements') }}</div>
                  <div class="criteria-grid">
                    <div class="criteria-item" :class="{ met: criteria.minLength }">
                      <el-icon><Check v-if="criteria.minLength" /><Close v-else /></el-icon>
                      <span>{{ t('profile.reqMinLength') }}</span>
                    </div>
                    <div class="criteria-item" :class="{ met: criteria.hasUppercase }">
                      <el-icon><Check v-if="criteria.hasUppercase" /><Close v-else /></el-icon>
                      <span>{{ t('profile.reqUppercase') }}</span>
                    </div>
                    <div class="criteria-item" :class="{ met: criteria.hasLowercase }">
                      <el-icon><Check v-if="criteria.hasLowercase" /><Close v-else /></el-icon>
                      <span>{{ t('profile.reqLowercase') }}</span>
                    </div>
                    <div class="criteria-item" :class="{ met: criteria.hasNumber }">
                      <el-icon><Check v-if="criteria.hasNumber" /><Close v-else /></el-icon>
                      <span>{{ t('profile.reqNumber') }}</span>
                    </div>
                  </div>
                </div>
              </transition>
            </el-form-item>

            <el-form-item
              :label="t('profile.confirmPassword')"
              prop="confirmPassword"
            >
              <el-input
                v-model="form.confirmPassword"
                type="password"
                show-password
                :placeholder="t('profile.confirmPasswordPlaceholder')"
                autocomplete="new-password"
              />
            </el-form-item>
          </el-form>

          <template #footer>
            <div class="dialog-footer">
              <el-button @click="handleBeforeClose">
                {{ t('profile.cancel') }}
              </el-button>
              <el-button
                type="primary"
                :loading="submitting"
                @click="handleSubmit"
              >
                {{ t('profile.submit') }}
              </el-button>
            </div>
          </template>
        </el-dialog>
      </template>
    </div>
  </component>
</template>

<style scoped>
.profile-page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 24px 0;
}

.loading-state {
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.error-state {
  margin-bottom: 24px;
}

.info-card {
  margin-bottom: 24px;
  border-radius: 12px;
}

.info-card :deep(.el-card__header) {
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px 12px 0 0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.info-card :deep(.el-card__body) {
  padding: 20px;
}

.label-with-icon {
  display: flex;
  align-items: center;
  gap: 6px;
}

.value-text {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

:deep(.el-descriptions__label) {
  width: 160px;
  font-weight: 500;
  background-color: #f9fafb !important;
}

:deep(.el-descriptions__content) {
  min-width: 120px;
}

.security-card {
  margin-top: 24px;
}

.security-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.security-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.security-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.security-desc {
  font-size: 13px;
  color: #6b7280;
}

:deep(.new-password-item .el-form-item__content) {
  display: flex;
  flex-direction: column;
  align-items: stretch;
}

:deep(.new-password-item .el-form-item__error) {
  position: static;
  order: 1;
  margin-top: 4px;
}

.password-criteria {
  order: 2;
  margin-top: 6px;
  padding: 6px 10px;
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  width: 100%;
  box-sizing: border-box;
}

.criteria-title {
  font-size: 11px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 4px;
}

.criteria-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 3px 8px;
}

.criteria-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  line-height: 1.3;
  color: #94a3b8;
  transition: all 0.2s ease;
}

.criteria-item.met {
  color: #10b981;
  font-weight: 500;
}

.criteria-item .el-icon {
  font-size: 12px;
  flex-shrink: 0;
}

@media (max-width: 480px) {
  .criteria-grid {
    grid-template-columns: 1fr;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
