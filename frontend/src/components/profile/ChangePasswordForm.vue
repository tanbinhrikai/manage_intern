<script setup>
import { ref, computed } from 'vue'
import { Check, Close } from '@element-plus/icons-vue'
import { useToastStore } from '@/stores/toast'
import { useLocaleStore } from '@/locales/locale'
import { useAuthStore } from '@/stores/auth'
import { changePassword } from '@/api/user'
import { useLoading } from '@/composables'
import {
  evaluatePasswordCriteria,
  createPasswordStrengthValidator
} from '@/utils/passwordValidation'
import router from '@/routers'

defineProps({
  showCancel: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['success', 'cancel'])

const localeStore = useLocaleStore()
const authStore = useAuthStore()
const toastStore = useToastStore()
const t = (key) => localeStore.t(key)

const { loading: submitting, withLoading } = useLoading()

const formRef = ref(null)
const form = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const oldPasswordError = ref('')
const isNewPasswordFocused = ref(false)

// Password requirements checklist
const criteria = computed(() => evaluatePasswordCriteria(form.value.newPassword))

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
      validator: createPasswordStrengthValidator(t, () => form.value.oldPassword),
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

function handleCancel() {
  resetForm()
  emit('cancel')
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
      await changePassword(
        {
          oldPassword: form.value.oldPassword,
          newPassword: form.value.newPassword,
          confirmPassword: form.value.confirmPassword
        },
        { skipErrorToast: (err) => err.response?.data?.code === 'PASSWORD_NOT_CORRECT' }
      )

      toastStore.success(t('profile.changePasswordSuccess'), 4000)

      resetForm()
      emit('success')

      authStore.clearAuth()
      router.push('/login')
    } catch (err) {
      const errorCode = err.response?.data?.code

      if (errorCode === 'PASSWORD_NOT_CORRECT') {
        oldPasswordError.value = t('profile.rules.oldPasswordIncorrect')
      }
    }
  })
}

defineExpose({ resetForm })
</script>

<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    label-position="top"
    status-icon
    @submit.prevent="handleSubmit"
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

    <div class="form-actions" :class="{ 'full-width-submit': !showCancel }">
      <el-button v-if="showCancel" @click="handleCancel">
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
  </el-form>
</template>

<style scoped>
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

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.form-actions.full-width-submit .el-button {
  width: 100%;
}
</style>

