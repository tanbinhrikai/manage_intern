<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useLocaleStore } from '@/locales/locale'
import { useAuthStore } from '@/stores/auth'
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue"
import MentorLayout from "@/layouts/dashboard/MentorLayout.vue"
import { getInternById, updateIntern } from '@/api/intern'
import { getPositions } from '@/api/position'
import { getMentors } from '@/api/user'

const route = useRoute()
const router = useRouter()
const localeStore = useLocaleStore()
const authStore = useAuthStore()
const t = computed(() => localeStore.t)

const internId = route.params.id
const intern = ref({}) 
const positions = ref([])
const mentors = ref([])
const loading = ref(false)
const activeTab = ref('general')
const saving = ref(false)

const isAdmin = computed(() => authStore.userRole === 'ADMIN')
const layoutComponent = computed(() => isAdmin.value ? AdminLayout : MentorLayout)

const statusOptions = ['ACTIVE', 'WARNING', 'COMPLETE', 'DROPPED']

const formData = reactive({
  fullName: '',
  positionId: '',
  mentorId: '',
  startDate: '',
  endDate: '',
  internStatus: ''
})

async function fetchInternDetail() {
  loading.value = true
  try {
    const res = await getInternById(internId)
    intern.value = res.data?.data || {}
    
    formData.fullName = intern.value.fullName || ''
    formData.positionId = intern.value.position?.id || ''
    formData.mentorId = intern.value.mentor?.id || ''
    formData.startDate = intern.value.startDate || ''
    formData.endDate = intern.value.endDate || ''
    formData.internStatus = intern.value.internStatus || 'ACTIVE'

  } catch (error) {
    console.error("Failed to load intern detail:", error)
    ElMessage.error(t.value('internManagement.messages.loadError'))
  } finally {
    loading.value = false
  }
}

async function fetchPositions() {
  try {
    const res = await getPositions()
    positions.value = res.data?.data || []
  } catch (error) {
    console.error("Failed to load positions:", error)
  }
}

async function fetchMentors() {
  if (!isAdmin.value) return

  try {
    const res = await getMentors()
    const items = res.data?.data?.items || []
    mentors.value = items.filter(u => u.role?.roleName === 'MENTOR')
  } catch (error) {
    console.error("Failed to load mentors:", error)
  }
}

async function handleSave() {
  saving.value = true
  try {
    const payload = {
      fullName: formData.fullName,
      positionId: formData.positionId,
      mentorId: formData.mentorId,
      startDate: formData.startDate,
      endDate: formData.endDate,
      internStatus: formData.internStatus
    }
    
    await updateIntern(internId, payload)
    ElMessage.success(t.value('internManagement.messages.updateSuccess'))
    
    await fetchInternDetail()
  } catch (error) {
    console.error("Update error:", error)
    ElMessage.error(t.value('internManagement.messages.saveError'))
  } finally {
    saving.value = false
  }
}

const getStatusType = (status) => {
  const map = {
    ACTIVE: 'success',
    WARNING: 'warning',
    COMPLETE: 'primary',
    DROPPED: 'danger'
  }
  return map[status] || 'info'
}

const calculateDuration = (start, end) => {
  if (!start || !end) return ''
  const startDate = new Date(start)
  const endDate = new Date(end)
  const diffTime = Math.abs(endDate - startDate)
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) 
  const weeks = Math.floor(diffDays / 7)
  return `${weeks} ${t.value('common.weeks')} (${formatDate(start)} - ${formatDate(end)})`
}

function formatDate(date) {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleDateString('en-GB')
}

onMounted(() => {
  fetchMentors()
  fetchPositions()
  fetchInternDetail()
})
</script>

<template>
  <component :is="layoutComponent">
    <div class="intern-edit-view" v-loading="loading">
      <div class="header-section">
        <h1 class="page-title">
          {{ t('internDetail.profileTitle') }}: {{ intern.fullName }} - {{ intern.position?.title }}
        </h1>
      </div>

      <el-tabs v-model="activeTab" class="detail-tabs">
        <el-tab-pane :label="t('internDetail.tabs.general')" name="general">
          <div class="tab-content">
            <el-card shadow="never" class="detail-card">
              <div class="form-grid">
                <!-- Row 1 -->
                <div class="form-group full-name">
                  <label>{{ t('internManagement.form.fullName') }}</label>
                  <el-input v-model="formData.fullName" class="custom-input" />
                </div>
                
                <div class="form-group position">
                  <label>{{ t('internManagement.form.position') }}</label>
                  <el-select 
                    v-model="formData.positionId" 
                    class="custom-select"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="pos in positions"
                      :key="pos.id"
                      :label="pos.title"
                      :value="pos.id"
                    />
                  </el-select>
                </div>

                <div class="form-group mentor">
                  <label>{{ t('internManagement.form.mentor') }}</label>
    
                  <el-select 
                    v-if="isAdmin"
                    v-model="formData.mentorId" 
                    style="width: 100%"
                    placeholder="Select Mentor"
                  >
                    <el-option
                      v-for="m in mentors"
                      :key="m.id || m.email"
                      :label="m.fullName"
                      :value="m.id"
                    />
                  </el-select>
                  
                  <el-input 
                    v-else 
                    :model-value="intern.mentor?.fullName" 
                    disabled 
                    class="custom-input read-only" 
                  />
                </div>

                <div class="form-group duration">
                  <label>{{ t('internDetail.fields.duration') }}</label>
                  <el-input :model-value="calculateDuration(formData.startDate, formData.endDate)" disabled class="custom-input read-only" />
                </div>

              
                <div class="form-group status">
                  <label>{{ t('internDetail.fields.status') }}</label>
                  <div class="status-edit-container">
                    <el-select 
                      v-model="formData.internStatus" 
                      class="status-select"
                    >
                      <el-option
                        v-for="status in statusOptions"
                        :key="status"
                        :label="t('internManagement.status.' + status)"
                        :value="status"
                      />
                    </el-select>
                    <el-tag :type="getStatusType(formData.internStatus)" class="ml-2">
                       {{ t('internManagement.status.' + formData.internStatus) }}
                    </el-tag>
                  </div>
                </div>

                <div class="form-group start-date">
                  <label>{{ t('internDetail.fields.startDate') }}</label>
                  <el-date-picker 
                    v-model="formData.startDate" 
                    type="date" 
                    format="DD/MM/YYYY"
                    value-format="YYYY-MM-DD"
                    class="custom-date-picker"
                  />
                </div>

                <div class="form-group end-date">
                  <label>{{ t('internDetail.fields.endDate') }}</label>
                  <el-date-picker 
                    v-model="formData.endDate" 
                    type="date" 
                    format="DD/MM/YYYY"
                    value-format="YYYY-MM-DD"
                    class="custom-date-picker"
                  />
                </div>
              </div>

              <div class="actions">
                <el-button type="primary" @click="handleSave" :loading="saving">
                  {{ t('internDetail.saveChanges') }}
                </el-button>
              </div>
            </el-card>
          </div>
        </el-tab-pane>
        <el-tab-pane :label="t('internDetail.tabs.reports')" name="reports">
          <div class="tab-content placeholder-content">
            {{ t('internDetail.tabs.reports') }} Content
          </div>
        </el-tab-pane>
        <el-tab-pane :label="t('internDetail.tabs.performance')" name="performance">
          <div class="tab-content placeholder-content">
             {{ t('internDetail.tabs.performance') }} Content
          </div>
        </el-tab-pane>
        <el-tab-pane :label="t('internDetail.tabs.history')" name="history">
          <div class="tab-content placeholder-content">
             {{ t('internDetail.tabs.history') }} Content
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </component>
</template>

<style scoped>
.intern-edit-view {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.header-section {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.detail-tabs :deep(.el-tabs__header) {
  margin-bottom: 24px;
}

.detail-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 600;
  color: #606266;
}

.detail-tabs :deep(.el-tabs__item.is-active) {
  color: #6466dd;
}

.detail-tabs :deep(.el-tabs__active-bar) {
  background-color: #6466dd;
}

.detail-card {
  border-radius: 8px;
  border: none;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 13px;
  font-weight: 700;
  color: #2c3e50;
}

.custom-input :deep(.el-input__wrapper) {
  background-color: #f5f7fa;
  box-shadow: none !important;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.status-edit-container {
    display: flex;
    align-items: center;
    gap: 8px;
}

.status-select {
    width: 120px;
}
.ml-2 {
    margin-left: 8px;
}

.placeholder-content {
  padding: 40px;
  text-align: center;
  color: #909399;
  background: white;
  border-radius: 8px;
}
</style>
