<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  User,
  Message,
  Calendar,
  OfficeBuilding,
  Lock,
  Key
} from '@element-plus/icons-vue'
import { useLocaleStore } from '@/locales/locale'
import { useAuthStore } from '@/stores/auth'
import AdminLayout from '@/layouts/dashboard/AdminLayout.vue'
import MentorLayout from '@/layouts/dashboard/MentorLayout.vue'
import ChangePasswordForm from '@/components/profile/ChangePasswordForm.vue'
import { getMyInfo } from '@/api/user'
import { useDateFormat } from '@/composables'

const localeStore = useLocaleStore()
const authStore = useAuthStore()
const t = (key) => localeStore.t(key)

const profile = ref(null)
const loading = ref(true)
const error = ref('')

const isMentor = computed(() => authStore.userRole === 'MENTOR')
const LayoutComponent = computed(() => isMentor.value ? MentorLayout : AdminLayout)

const { formatDate } = useDateFormat()

// Change Password Dialog State
const dialogVisible = ref(false)
const changePasswordFormRef = ref(null)

function handleBeforeClose(done) {
  changePasswordFormRef.value?.resetForm()
  dialogVisible.value = false
  if (typeof done === 'function') {
    done()
  }
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
          <ChangePasswordForm
            ref="changePasswordFormRef"
            @cancel="handleBeforeClose"
            @success="dialogVisible = false"
          />
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
</style>
