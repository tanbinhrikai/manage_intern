<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useLocaleStore } from '@/locales/locale'
import { useAuthStore } from '@/stores/auth'
import { updateIntern } from '@/api/intern'
import { getPositions } from '@/api/position'
import { getMentors } from '@/api/user'

const props = defineProps({
  intern: {
    type: Object,
    required: true,
    default: () => ({})
  }
})

const emit = defineEmits(['saved'])

const localeStore = useLocaleStore()
const authStore = useAuthStore()
const t = computed(() => localeStore.t)

const positions = ref([])
const mentors = ref([])
const saving = ref(false)

const isAdmin = computed(() => authStore.userRole === 'ADMIN')

const statusOptions = ['ACTIVE', 'WARNING', 'COMPLETE', 'DROPPED']

const formData = reactive({
  fullName: '',
  positionId: '',
  mentorId: '',
  startDate: '',
  endDate: '',
  internStatus: ''
})

watch(() => props.intern, (newVal) => {
  if (newVal) {
    formData.fullName = newVal.fullName || ''
    formData.positionId = newVal.position?.id || ''
    formData.mentorId = newVal.mentor?.id || ''
    formData.startDate = newVal.startDate || ''
    formData.endDate = newVal.endDate || ''
    formData.internStatus = newVal.internStatus || 'ACTIVE'
  }
}, { immediate: true, deep: true })

async function fetchPositions() {
  try {
    const res = await getPositions({ limit: 100 })
    positions.value = res.data?.data?.items || []
  } catch (error) {
    console.error("Failed to load positions:", error)
  }
}

async function fetchMentors() {
  if (!isAdmin.value) return

  try {
    const res = await getMentors({ limit: 100 })
    mentors.value = res.data?.data?.items || []
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
    
    await updateIntern(props.intern.id, payload)
    ElMessage.success(t.value('internManagement.messages.updateSuccess'))
    emit('saved')
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
  fetchPositions()
  fetchMentors()
})
</script>

<template>
  <div class="tab-content">
    <el-card shadow="never" class="detail-card">
      <div class="form-grid">
        <!-- Row 1 -->
        <div class="form-group full-name">
          <label>{{ t('internManagement.form.fullName') }}</label>
          <el-input v-model="formData.fullName"  />
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
</template>

<style scoped>
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
</style>
