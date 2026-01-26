<script setup>
import { ref, computed, onMounted } from 'vue'
import { User, Message, Calendar, OfficeBuilding, UserFilled } from '@element-plus/icons-vue'
import { useLocaleStore } from '@/locales/locale'
import { useAuthStore } from '@/stores/auth'
import AdminLayout from '@/layouts/dashboard/AdminLayout.vue'
import MentorLayout from '@/layouts/dashboard/MentorLayout.vue'
import { getMyInfo } from '@/api/user'
import { useDateFormat } from '@/composables'

const localeStore = useLocaleStore()
const authStore = useAuthStore()
const t = computed(() => localeStore.t)

const profile = ref(null)
const loading = ref(true)
const error = ref('')

const isMentor = computed(() => authStore.userRole === 'MENTOR')
const LayoutComponent = computed(() => isMentor.value ? MentorLayout : AdminLayout)

const { formatDate, formatDateTime } = useDateFormat()

async function fetchProfile() {
  loading.value = true
  error.value = ''
  try {
    const res = await getMyInfo()
    profile.value = res.data?.data || null
  } catch (err) {
    console.error('Failed to load profile:', err)
    error.value = t.value('profile.error')
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
          <el-col :xs="24" :lg="16">
            <el-card class="info-card" shadow="never">

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
          </el-col>
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
</style>
