<script setup>
import { ref, computed, reactive, watch, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus, EditPen } from '@element-plus/icons-vue'
import { useLocaleStore } from '@/locales/locale'
import { useAuthStore } from '@/stores/auth'
import AdminLayout from '@/layouts/dashboard/AdminLayout.vue'
import MentorLayout from '@/layouts/dashboard/MentorLayout.vue'
import { createEvaluationSession, generateEvaluationSession, getEvaluationSessions } from '@/api/evaluation-session'
import { getEvaluationCriteria } from '@/api/evaluation-criteria'
import { useLoading, useApi, useDropdownData, useDateFormat, useConfirm } from '@/composables'
import { SessionType } from '@/types/common'
import { createEvaluationSessionCreateRequest, createEvaluationScoreRequest } from '@/types/evaluationSession'

const router = useRouter()
const localeStore = useLocaleStore()
const authStore = useAuthStore()
const t = computed(() => localeStore.t)

const LayoutComponent = computed(() => 
  authStore.userRole === 'MENTOR' ? MentorLayout : AdminLayout
)

const { loading, withLoading } = useLoading()
const { execute: executeApi } = useApi({
  showErrorMessage: true,
  showSuccessMessage: true
})
const { interns, fetchInterns } = useDropdownData()
const { formatDate } = useDateFormat()
const { confirmUpdate } = useConfirm()

// Mode: 'manual' or 'auto-generate'
const creationMode = ref('auto-generate')

// Selected intern
const selectedInternId = ref(null)
const selectedIntern = ref(null)

// Auto-generate form - internId removed, will use selectedInternId instead
const autoGenerateForm = reactive({
  sessionType: SessionType.FIRST_TERM
})

// Manual form - internId removed, will use selectedInternId instead
const manualFormBase = createEvaluationSessionCreateRequest()
delete manualFormBase.internId
const manualForm = reactive(manualFormBase)
const criteriaList = ref([])
const existingSessions = ref([])

/**
 * Calculate evaluation milestones based on internship dates
 * @returns {Object} Milestone dates
 */
const milestones = computed(() => {
  if (!selectedIntern.value?.startDate || !selectedIntern.value?.endDate) {
    return null
  }
  
  const startDate = new Date(selectedIntern.value.startDate)
  const endDate = new Date(selectedIntern.value.endDate)
  const totalDays = Math.ceil((endDate - startDate) / (1000 * 60 * 60 * 24))
  
  // FIRST_TERM: After 2 months (1/3)
  const firstTermDate = new Date(startDate)
  firstTermDate.setMonth(firstTermDate.getMonth() + 2)
  
  // MID_TERM: After 4 months (2/3)
  const midTermDate = new Date(startDate)
  midTermDate.setMonth(midTermDate.getMonth() + 4)
  
  // FINAL: End date
  const finalDate = new Date(endDate)
  
  return {
    firstTerm: {
      date: firstTermDate,
      label: t.value('evaluationSession.sessionType.firstTerm'),
      sessionType: SessionType.FIRST_TERM,
      progress: 33
    },
    midTerm: {
      date: midTermDate,
      label: t.value('evaluationSession.sessionType.midTerm'),
      sessionType: SessionType.MID_TERM,
      progress: 67
    },
    final: {
      date: finalDate,
      label: t.value('evaluationSession.sessionType.final'),
      sessionType: SessionType.FINAL,
      progress: 100
    }
  }
})

/**
 * Check if a session type already exists for selected intern
 * @param {SessionType} sessionType - Session type to check
 * @returns {boolean} True if exists
 */
const hasExistingSession = (sessionType) => {
  return existingSessions.value.some(s => s.sessionType === sessionType)
}

/**
 * Get session status for a milestone
 * @param {SessionType} sessionType - Session type
 * @returns {Object} Status info
 */
const getMilestoneStatus = (sessionType) => {
  const existing = existingSessions.value.find(s => s.sessionType === sessionType)
  if (existing) {
    return {
      exists: true,
      type: 'success',
      label: t.value('evaluationSession.create.milestone.completed')
    }
  }
  
  const milestone = Object.values(milestones.value || {}).find(m => m.sessionType === sessionType)
  if (!milestone) return { exists: false, type: 'info', label: '-' }
  
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const milestoneDate = new Date(milestone.date)
  milestoneDate.setHours(0, 0, 0, 0)
  
  if (milestoneDate <= today) {
    return {
      exists: false,
      type: 'warning',
      label: t.value('evaluationSession.create.milestone.due')
    }
  }
  
  return {
    exists: false,
    type: 'info',
    label: t.value('evaluationSession.create.milestone.upcoming')
  }
}

/**
 * Fetch evaluation criteria for manual form
 */
async function fetchCriteria() {
  try {
    const res = await executeApi(() => getEvaluationCriteria())
    criteriaList.value = res.data?.data || []
    
    // Initialize scores for each criteria
    if (criteriaList.value.length > 0 && manualForm.scores.length === 0) {
      manualForm.scores = criteriaList.value
        .filter(c => c.parent === null) // Only leaf criteria
        .map(c => ({
          ...createEvaluationScoreRequest(),
          criteriaId: c.id
        }))
    }
  } catch (error) {
    console.error('Failed to fetch criteria:', error)
  }
}

/**
 * Fetch existing sessions for selected intern
 */
async function fetchExistingSessions() {
  if (!selectedInternId.value) {
    existingSessions.value = []
    return
  }
  
  try {
    const res = await executeApi(() => getEvaluationSessions({ 
      internId: selectedInternId.value,
      limit: 100 
    }))
    existingSessions.value = res.data?.data?.items || []
  } catch (error) {
    console.error('Failed to fetch existing sessions:', error)
    existingSessions.value = []
  }
}

// Note: Sync between selects is handled by watches above

// Flag to prevent watch loops
const isUpdatingInterns = ref(false)

// Watch selectedInternId and sync all related data
watch(selectedInternId, async (newId, oldId) => {
  if (newId === oldId || isUpdatingInterns.value) return
  
  console.log('selectedInternId watch:', { 
    newId, 
    oldId, 
    newIdType: typeof newId,
    internsAvailable: interns.value.length,
    sampleInternId: interns.value[0]?.id,
    sampleInternIdType: typeof interns.value[0]?.id
  })
  
  if (newId) {
    // Ensure interns are loaded
    if (interns.value.length === 0) {
      await fetchInterns()
      await nextTick()
    }
    
    // Ensure type consistency - convert to number if needed
    const searchId = typeof newId === 'string' ? Number(newId) : newId
    const intern = interns.value.find(i => {
      const optionId = typeof i.id === 'string' ? Number(i.id) : i.id
      return optionId === searchId
    })
    
    console.log('Found intern:', intern ? intern.fullName : 'NOT FOUND', {
      searchId,
      searchIdType: typeof searchId,
      availableIds: interns.value.slice(0, 5).map(i => ({ id: i.id, idType: typeof i.id, fullName: i.fullName }))
    })
    
    if (intern) {
      isUpdatingInterns.value = true
      selectedIntern.value = intern
      
      // Ensure value type matches option value type
      const correctId = typeof intern.id === 'string' ? Number(intern.id) : intern.id
      
      // Wait for DOM to update
      await nextTick()
      
      // No need to sync - forms will use selectedInternId directly
      
      // Ensure selectedInternId has correct type
      if (selectedInternId.value !== correctId) {
        selectedInternId.value = correctId
      }
      
      await nextTick()
      
      // Set default evaluation date based on milestones
      if (milestones.value) {
        const milestone = Object.values(milestones.value).find(
          m => m.sessionType === autoGenerateForm.sessionType
        )
        if (milestone) {
          manualForm.evaluationDate = milestone.date.toISOString().split('T')[0]
        }
      }
      
      await fetchExistingSessions()
      
      isUpdatingInterns.value = false
    } else {
      console.warn('Intern not found in list:', newId, 'Available:', interns.value.map(i => i.id))
    }
  } else {
    selectedIntern.value = null
    existingSessions.value = []
  }
})

/**
 * Watch for session type changes in auto-generate mode
 */
watch(() => autoGenerateForm.sessionType, (newType) => {
  if (milestones.value && selectedInternId.value) {
    const milestone = Object.values(milestones.value).find(m => m.sessionType === newType)
    if (milestone) {
      const dateStr = milestone.date.toISOString().split('T')[0]
      manualForm.evaluationDate = dateStr
      manualForm.sessionType = newType
    }
  }
})

/**
 * Handle auto-generate
 */
async function handleAutoGenerate() {
  if (!selectedInternId.value || !autoGenerateForm.sessionType) {
    ElMessage.warning(t.value('evaluationSession.create.validation.selectInternAndType'))
    return
  }
  
  if (hasExistingSession(autoGenerateForm.sessionType)) {
    ElMessage.warning(t.value('evaluationSession.create.messages.sessionExists'))
    return
  }
  
  await withLoading(async () => {
    await executeApi(
      () => generateEvaluationSession(selectedInternId.value, autoGenerateForm.sessionType),
      'evaluationSession.messages.generateSuccess',
      true
    )
    
    router.push(authStore.userRole === 'MENTOR' 
      ? '/mentor/evaluation-sessions' 
      : '/admin/evaluation-sessions'
    )
  })
}

/**
 * Handle auto-generate with confirmation
 */
function handleAutoGenerateWithConfirm() {
  if (!selectedInternId.value || !autoGenerateForm.sessionType) {
    ElMessage.warning(t.value('evaluationSession.create.validation.selectInternAndType'))
    return
  }
  
  if (hasExistingSession(autoGenerateForm.sessionType)) {
    ElMessage.warning(t.value('evaluationSession.create.messages.sessionExists'))
    return
  }
  
  confirmUpdate({
    message: t.value('evaluationSession.confirm.generate') || t.value('evaluationSession.create.confirmGenerate') || 'Are you sure you want to generate this evaluation session?',
    onConfirm: handleAutoGenerate
  })
}

/**
 * Handle manual create
 */
async function handleManualCreate() {
  if (!selectedInternId.value || !manualForm.sessionType || !manualForm.evaluationDate) {
    ElMessage.warning(t.value('evaluationSession.create.validation.fillRequired'))
    return
  }
  
  await withLoading(async () => {
    const payload = {
      internId: selectedInternId.value,
      sessionType: manualForm.sessionType,
      evaluationDate: manualForm.evaluationDate,
      overallComment: manualForm.overallComment || '',
      scores: manualForm.scores.filter(s => s.criteriaId && (s.score !== null || s.comment))
    }
    
    await executeApi(
      () => createEvaluationSession(payload),
      'evaluationSession.messages.createSuccess',
      true
    )
    
    router.push(authStore.userRole === 'MENTOR' 
      ? '/mentor/evaluation-sessions' 
      : '/admin/evaluation-sessions'
    )
  })
}

/**
 * Handle manual create with confirmation
 */
function handleManualCreateWithConfirm() {
  if (!selectedInternId.value || !manualForm.sessionType || !manualForm.evaluationDate) {
    ElMessage.warning(t.value('evaluationSession.create.validation.fillRequired'))
    return
  }
  
  confirmUpdate({
    message: t.value('evaluationSession.confirm.create') || t.value('evaluationSession.create.confirmCreate') || 'Are you sure you want to create this evaluation session?',
    onConfirm: handleManualCreate
  })
}

/**
 * Navigate back
 */
function goBack() {
  router.push(authStore.userRole === 'MENTOR' 
    ? '/mentor/evaluation-sessions' 
    : '/admin/evaluation-sessions'
  )
}

/**
 * Select milestone for auto-generate
 */
function selectMilestone(sessionType) {
  autoGenerateForm.sessionType = sessionType
  if (milestones.value && selectedInternId.value) {
    const milestone = Object.values(milestones.value).find(m => m.sessionType === sessionType)
    if (milestone) {
      const dateStr = milestone.date.toISOString().split('T')[0]
      manualForm.evaluationDate = dateStr
      manualForm.sessionType = sessionType
    }
  }
}

onMounted(async () => {
  await fetchInterns()
  await fetchCriteria()
  
  // Debug: Log interns to check if data is loaded
  console.log('Interns loaded:', interns.value.length)
  if (interns.value.length > 0) {
    console.log('Sample intern:', interns.value[0])
    console.log('Intern ID type:', typeof interns.value[0]?.id)
    
    // If there's a selectedInternId, ensure it's properly set
    if (selectedInternId.value) {
      const intern = interns.value.find(i => i.id === selectedInternId.value)
      if (intern) {
        selectedIntern.value = intern
        // Force update by reassigning
        const currentId = selectedInternId.value
        selectedInternId.value = null
        await nextTick()
        selectedInternId.value = currentId
      }
    }
  }
})
</script>

<template>
  <component :is="LayoutComponent">
    <div class="evaluation-session-create-view" v-loading="loading">
      <div class="page-header">
        <div class="header-left">
          <el-button 
            :icon="ArrowLeft" 
            text 
            @click="goBack"
            class="back-button"
          >
            {{ t('evaluationSession.create.backToList') }}
          </el-button>
          <h1 class="page-title">
            {{ t('evaluationSession.create.title') }}
          </h1>
        </div>
      </div>

      <!-- Mode Toggle -->
      <el-card shadow="hover" class="mode-card mb-24">
        <el-radio-group v-model="creationMode" size="large">
          <el-radio-button value="auto-generate">
            <el-icon class="mr-2"><Plus /></el-icon>
            {{ t('evaluationSession.create.mode.autoGenerate') }}
          </el-radio-button>
          <el-radio-button value="manual">
            <el-icon class="mr-2"><EditPen /></el-icon>
            {{ t('evaluationSession.create.mode.manual') }}
          </el-radio-button>
        </el-radio-group>
      </el-card>

      <el-row :gutter="24">
        <!-- Left Column: Timeline & Info -->
        <el-col :xs="24" :lg="10">
          <el-card shadow="hover" class="info-card mb-24">
            <template #header>
              <h3>{{ t('evaluationSession.create.sections.internshipInfo') }}</h3>
            </template>
            
            <div class="intern-select-section">
              <label class="form-label">{{ t('evaluationSession.create.selectIntern') }}</label>
              <el-select 
                v-if="interns.length > 0"
                v-model="selectedInternId"
                :placeholder="t('evaluationSession.create.selectInternPlaceholder')"
                filterable
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="intern in interns"
                  :key="intern.id"
                  :label="intern.fullName"
                  :value="intern.id"
                />
              </el-select>
              <el-select 
                v-else
                disabled
                :placeholder="t('evaluationSession.create.selectInternPlaceholder')"
                style="width: 100%"
              />
            </div>

            <!-- Timeline Visualization -->
            <div v-if="selectedIntern && milestones" class="timeline-section">
              <h4 class="timeline-title">{{ t('evaluationSession.create.sections.timeline') }}</h4>
              
              <div class="timeline">
                <div 
                  v-for="(milestone, key) in milestones" 
                  :key="key"
                  class="timeline-item"
                  :class="{ 'active': autoGenerateForm.sessionType === milestone.sessionType }"
                  @click="selectMilestone(milestone.sessionType)"
                >
                  <div class="timeline-marker">
                    <el-tag 
                      :type="getMilestoneStatus(milestone.sessionType).type"
                      size="small"
                    >
                      {{ getMilestoneStatus(milestone.sessionType).label }}
                    </el-tag>
                  </div>
                  <div class="timeline-content">
                    <div class="milestone-label">{{ milestone.label }}</div>
                    <div class="milestone-date">{{ formatDate(milestone.date) }}</div>
                    <div class="milestone-progress">
                      <el-progress 
                        :percentage="milestone.progress" 
                        :stroke-width="4"
                        :show-text="false"
                      />
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Intern Info -->
            <div v-if="selectedIntern" class="intern-info-section">
              <div class="info-item">
                <span class="label">{{ t('evaluationSession.create.internInfo.startDate') }}:</span>
                <span class="value">{{ formatDate(selectedIntern.startDate) }}</span>
              </div>
              <div class="info-item">
                <span class="label">{{ t('evaluationSession.create.internInfo.endDate') }}:</span>
                <span class="value">{{ formatDate(selectedIntern.endDate) }}</span>
              </div>
              <div class="info-item">
                <span class="label">{{ t('evaluationSession.create.internInfo.mentor') }}:</span>
                <span class="value">{{ selectedIntern.mentor?.fullName || '-' }}</span>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- Right Column: Form -->
        <el-col :xs="24" :lg="14">
          <!-- Auto-Generate Form -->
          <el-card v-if="creationMode === 'auto-generate'" shadow="hover" class="form-card">
            <template #header>
              <h3>{{ t('evaluationSession.create.mode.autoGenerate') }}</h3>
            </template>

            <el-form :model="autoGenerateForm" label-width="150px">
              <el-alert
                v-if="!selectedInternId"
                type="info"
                :closable="false"
                show-icon
                style="margin-bottom: 20px;"
              >
                {{ t('evaluationSession.create.messages.selectInternFirst') }}
              </el-alert>

              <el-form-item :label="t('evaluationSession.create.form.sessionType')">
                <el-select 
                  v-model="autoGenerateForm.sessionType"
                  style="width: 100%"
                >
                  <el-option 
                    :label="t('evaluationSession.sessionType.firstTerm')" 
                    :value="SessionType.FIRST_TERM"
                    :disabled="hasExistingSession(SessionType.FIRST_TERM)"
                  />
                  <el-option 
                    :label="t('evaluationSession.sessionType.midTerm')" 
                    :value="SessionType.MID_TERM"
                    :disabled="hasExistingSession(SessionType.MID_TERM)"
                  />
                  <el-option 
                    :label="t('evaluationSession.sessionType.final')" 
                    :value="SessionType.FINAL"
                    :disabled="hasExistingSession(SessionType.FINAL)"
                  />
                </el-select>
                <div v-if="hasExistingSession(autoGenerateForm.sessionType)" class="form-hint">
                  {{ t('evaluationSession.create.messages.sessionExists') }}
                </div>
              </el-form-item>

              <el-form-item>
                <el-button 
                  type="primary" 
                  :icon="Plus"
                  size="large"
                  @click="handleAutoGenerateWithConfirm"
                  :disabled="!selectedInternId || !autoGenerateForm.sessionType"
                  :loading="loading"
                >
                  {{ t('evaluationSession.create.generate') }}
                </el-button>
                <br/>
                <div v-if="!selectedInternId || !autoGenerateForm.sessionType" class="form-hint">
                  {{ t('evaluationSession.create.validation.selectInternAndType') }}
                </div>
              </el-form-item>
            </el-form>
          </el-card>

          <!-- Manual Form -->
          <el-card v-else shadow="hover" class="form-card">
            <template #header>
              <h3>{{ t('evaluationSession.create.mode.manual') }}</h3>
            </template>

            <el-form :model="manualForm" label-width="150px">
              <el-alert
                v-if="!selectedInternId"
                type="info"
                :closable="false"
                show-icon
                style="margin-bottom: 20px;"
              >
                {{ t('evaluationSession.create.messages.selectInternFirst') }}
              </el-alert>

              <el-form-item :label="t('evaluationSession.create.form.sessionType')" required>
                <el-select 
                  v-model="manualForm.sessionType"
                  style="width: 100%"
                >
                  <el-option 
                    :label="t('evaluationSession.sessionType.firstTerm')" 
                    :value="SessionType.FIRST_TERM"
                  />
                  <el-option 
                    :label="t('evaluationSession.sessionType.midTerm')" 
                    :value="SessionType.MID_TERM"
                  />
                  <el-option 
                    :label="t('evaluationSession.sessionType.final')" 
                    :value="SessionType.FINAL"
                  />
                </el-select>
              </el-form-item>

              <el-form-item :label="t('evaluationSession.create.form.evaluationDate')" required>
                <el-date-picker
                  v-model="manualForm.evaluationDate"
                  type="date"
                  format="DD/MM/YYYY"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>

              <el-form-item :label="t('evaluationSession.create.form.overallComment')">
                <el-input
                  v-model="manualForm.overallComment"
                  type="textarea"
                  :rows="4"
                  :placeholder="t('evaluationSession.create.form.overallCommentPlaceholder')"
                />
              </el-form-item>

              <!-- Scores Section -->
              <el-form-item :label="t('evaluationSession.create.form.scores')">
                <div class="scores-section">
                  <div 
                    v-for="(score, index) in manualForm.scores" 
                    :key="index"
                    class="score-item"
                  >
                    <div class="score-criteria">
                      {{ criteriaList.find(c => c.id === score.criteriaId)?.name || '-' }}
                    </div>
                    <div class="score-inputs">
                      <el-input-number
                        v-model="score.score"
                        :min="0"
                        :max="10"
                        :precision="2"
                        :step="0.1"
                        :placeholder="t('evaluationSession.create.form.scorePlaceholder')"
                        style="width: 120px;"
                      />
                      <el-input
                        v-model="score.comment"
                        :placeholder="t('evaluationSession.create.form.commentPlaceholder')"
                        style="flex: 1; margin-left: 12px;"
                      />
                    </div>
                  </div>
                </div>
              </el-form-item>

              <el-form-item>
                <el-button 
                  type="primary" 
                  :icon="EditPen"
                  size="large"
                  @click="handleManualCreateWithConfirm"
                  :loading="loading"
                >
                  {{ t('evaluationSession.create.create') }}
                </el-button>
                <el-button @click="goBack">
                  {{ t('evaluationSession.create.cancel') }}
                </el-button>
                <div v-if="!selectedInternId || !manualForm.sessionType || !manualForm.evaluationDate" class="form-hint">
                  {{ t('evaluationSession.create.validation.fillRequired') }}
                </div>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </component>
</template>

<style scoped>
.evaluation-session-create-view {
  min-height: calc(100vh - 60px);
  padding: 0 10px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-button {
  padding: 0;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.mb-24 {
  margin-bottom: 24px;
}

.mr-2 {
  margin-right: 8px;
}

.mode-card {
  border-radius: 8px;
}

.mode-card :deep(.el-radio-group) {
  width: 100%;
  display: flex;
  gap: 12px;
}

.mode-card :deep(.el-radio-button) {
  flex: 1;
}

.mode-card :deep(.el-radio-button__inner) {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.info-card,
.form-card {
  border-radius: 8px;
}

.info-card :deep(.el-card__header),
.form-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.info-card :deep(.el-card__body),
.form-card :deep(.el-card__body) {
  padding: 20px;
}

.info-card h3,
.form-card h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #606266;
}

.intern-select-section {
  margin-bottom: 24px;
}

/* Force display selected value in el-select */
.intern-select-section :deep(.el-select__input),
.intern-select-section :deep(.el-input__inner),
.intern-select-section :deep(.el-select__tags-text) {
  color: #606266 !important;
  opacity: 1 !important;
  visibility: visible !important;
}

.intern-select-section :deep(.el-select .el-input__inner) {
  color: #606266 !important;
}

/* Force display selected value in all intern selects */
:deep(.el-select__selection) {
  color: #606266 !important;
  opacity: 1 !important;
  visibility: visible !important;
  display: block !important;
}

/* Fix z-index issue - placeholder has z-index: -1 which hides selected value */
:deep(.el-select__selected-item.el-select__placeholder) {
  z-index: 1 !important;
  position: relative !important;
}

:deep(.el-select__selection span),
:deep(.el-select__selection .el-select__tags-text),
:deep(.el-select__selection .el-select__tags-text span) {
  color: #606266 !important;
  opacity: 1 !important;
  visibility: visible !important;
  display: inline-block !important;
  position: relative !important;
  z-index: 1 !important;
}

:deep(.el-select .el-input__inner),
:deep(.el-select__tags-text),
:deep(.el-select .el-input__suffix),
:deep(.el-select__selected-item),
:deep(.el-select .el-input__wrapper input) {
  color: #606266 !important;
  opacity: 1 !important;
  visibility: visible !important;
}

:deep(.el-select__input) {
  color: #606266 !important;
  opacity: 1 !important;
}

:deep(.el-select .el-input__wrapper) {
  opacity: 1 !important;
}

:deep(.el-select .el-input__wrapper .el-input__inner) {
  color: #606266 !important;
  opacity: 1 !important;
  visibility: visible !important;
}

/* Hide blinking cursor in el-select when not actively filtering */
:deep(.el-select .el-select__input:not(:focus)) {
  caret-color: transparent !important;
}

/* Hide cursor when select has a value and is not focused */
:deep(.el-select:not(.is-focus) .el-select__input) {
  caret-color: transparent !important;
  pointer-events: none !important;
}

/* Allow cursor when actively filtering */
:deep(.el-select.is-filterable.is-focus .el-select__input) {
  caret-color: var(--el-text-color-regular, #606266) !important;
  pointer-events: auto !important;
}

/* Improve select cursor */
:deep(.el-select__wrapper),
:deep(.el-select__selection) {
  cursor: pointer !important;
}

/* Ensure all selects have consistent width */
:deep(.el-form-item .el-select) {
  width: 100% !important;
}

:deep(.el-form-item .el-select .el-input__wrapper) {
  width: 100% !important;
}

:deep(.el-form-item .el-select__wrapper) {
  width: 100% !important;
}

/* Fix form item width - ensure consistent width for all form items */
:deep(.el-form-item__content) {
  display: block;
  width: 100%;
  max-width: 100%;
  min-width: 0;
}

:deep(.el-form-item__content > .el-select) {
  width: 100% !important;
  max-width: 100% !important;
  flex: 1;
  min-width: 0;
}

/* Ensure form items have same width */
:deep(.el-form .el-form-item) {
  width: 100%;
}

:deep(.el-form .el-form-item__content) {
  width: 100%;
  max-width: 100%;
}

/* Fix vertical alignment for all selects */
:deep(.el-select .el-input__wrapper) {
  display: flex !important;
  align-items: center !important;
  min-height: 32px;
  flex-wrap: nowrap !important;
}

:deep(.el-select__selection) {
  display: flex !important;
  align-items: center !important;
  height: 100% !important;
  line-height: normal !important;
  flex-wrap: nowrap !important;
  overflow: hidden !important;
}

:deep(.el-select__selected-item) {
  display: flex !important;
  align-items: center !important;
  line-height: normal !important;
  height: auto !important;
  min-height: 20px !important;
  flex-shrink: 0 !important;
}

/* Hide input wrapper when not filtering - this prevents the 2-row layout issue */
:deep(.el-select:not(.is-focus) .el-select__selected-item.el-select__input-wrapper) {
  display: none !important;
}

/* Show input wrapper only when actively filtering */
:deep(.el-select.is-focus .el-select__selected-item.el-select__input-wrapper) {
  display: flex !important;
  flex: 1 !important;
  min-width: 0 !important;
}

/* Ensure placeholder/selected item doesn't conflict with input wrapper */
:deep(.el-select__selected-item.el-select__placeholder) {
  display: flex !important;
  align-items: center !important;
  line-height: normal !important;
  height: auto !important;
  top: auto !important;
  transform: none !important;
  position: relative !important;
  flex: 0 0 auto !important;
  max-width: 100% !important;
  overflow: hidden !important;
  text-overflow: ellipsis !important;
  white-space: nowrap !important;
}

/* When filtering, hide the placeholder */
:deep(.el-select.is-focus .el-select__selected-item.el-select__placeholder) {
  display: none !important;
}

:deep(.el-select__placeholder) {
  display: flex !important;
  align-items: center !important;
  line-height: normal !important;
  height: auto !important;
}

:deep(.el-select__selection span) {
  display: inline-block !important;
  line-height: 1.5 !important;
  vertical-align: middle !important;
  overflow: hidden !important;
  text-overflow: ellipsis !important;
  white-space: nowrap !important;
}

/* Improve select appearance */
:deep(.el-select) {
  cursor: pointer;
}

:deep(.el-select .el-input__wrapper) {
  cursor: pointer;
}

/* Hide caret when select has value */
:deep(.el-select:not(.is-filterable) .el-select__input),
:deep(.el-select .el-select__input[readonly]) {
  caret-color: transparent !important;
  cursor: pointer !important;
}

.timeline-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

.timeline-title {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin: 0 0 16px 0;
}

.timeline {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.timeline-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.timeline-item:hover {
  background-color: #f5f7fa;
}

.timeline-item.active {
  background-color: #ecf5ff;
  border: 1px solid #b3d8ff;
}

.timeline-marker {
  flex-shrink: 0;
}

.timeline-content {
  flex: 1;
}

.milestone-label {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.milestone-date {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.milestone-progress {
  margin-top: 8px;
}

.intern-info-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.info-item .label {
  font-size: 13px;
  color: #909399;
}

.info-item .value {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}

.scores-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: 400px;
  overflow-y: auto;
  padding: 12px;
  background-color: #f9fafb;
  border-radius: 8px;
}

.score-item {
  padding: 12px;
  background-color: white;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.score-criteria {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.score-inputs {
  display: flex;
  align-items: center;
}

.form-hint {
  font-size: 12px;
  color: #f56c6c;
  margin-top: 4px;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>
