<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, EditPen } from '@element-plus/icons-vue'
import { useLocaleStore } from '@/locales/locale'
import { useAuthStore } from '@/stores/auth'
import AdminLayout from '@/layouts/dashboard/AdminLayout.vue'
import MentorLayout from '@/layouts/dashboard/MentorLayout.vue'
import { getEvaluationSessionById, updateEvaluationSession } from '@/api/evaluation-session'
import { getEvaluationCriteria } from '@/api/evaluation-criteria'
import { useLoading, useApi, useDateFormat } from '@/composables'
import { SessionType } from '@/types/common'
import { createEvaluationScoreRequest } from '@/types/evaluationSession'

const route = useRoute()
const router = useRouter()
const localeStore = useLocaleStore()
const authStore = useAuthStore()
const t = computed(() => localeStore.t)

const sessionId = route.params.id

const LayoutComponent = computed(() => 
  authStore.userRole === 'MENTOR' ? MentorLayout : AdminLayout
)

const { loading, withLoading } = useLoading()
const { execute: executeApi } = useApi({
  showErrorMessage: true,
  showSuccessMessage: true
})
const { formatDate } = useDateFormat()

// Form data
const form = reactive({
  sessionType: null,
  evaluationDate: '',
  overallComment: '',
  scores: []
})

const criteriaList = ref([])
const sessionData = ref({})

/**
 * Get session type label
 * @param {SessionType} type - Session type
 * @returns {string} Label
 */
function getSessionTypeLabel(type) {
  const labels = {
    [SessionType.FIRST_TERM]: t.value('evaluationSession.sessionType.firstTerm'),
    [SessionType.MID_TERM]: t.value('evaluationSession.sessionType.midTerm'),
    [SessionType.FINAL]: t.value('evaluationSession.sessionType.final')
  }
  return labels[type] || type
}

/**
 * Fetch evaluation criteria
 */
async function fetchCriteria() {
  try {
    const res = await executeApi(() => getEvaluationCriteria())
    criteriaList.value = res.data?.data || []
  } catch (error) {
    console.error('Failed to fetch criteria:', error)
  }
}

/**
 * Fetch session detail and populate form
 */
async function fetchSessionDetail() {
  await withLoading(async () => {
    const res = await executeApi(
      () => getEvaluationSessionById(sessionId),
      null,
      'evaluationSession.messages.loadError'
    )
    
    sessionData.value = res.data?.data || {}
    
    // Populate form with session data
    form.sessionType = sessionData.value.sessionType
    form.evaluationDate = sessionData.value.evaluationDate || ''
    form.overallComment = sessionData.value.overallComment || ''
    
    // Populate scores
    if (sessionData.value.scores && Array.isArray(sessionData.value.scores)) {
      // Get all main criteria (leaf nodes)
      const mainCriteria = criteriaList.value.filter(c => c.parent === null)
      
      // Create score entries for all criteria, filling in existing scores
      form.scores = mainCriteria.map(criteria => {
        const existingScore = sessionData.value.scores.find(
          s => s.criteriaId === criteria.id
        )
        
        return {
          ...createEvaluationScoreRequest(),
          criteriaId: criteria.id,
          score: existingScore?.score ?? null,
          comment: existingScore?.comment || ''
        }
      })
    } else {
      // If no scores, initialize with all criteria
      const mainCriteria = criteriaList.value.filter(c => c.parent === null)
      form.scores = mainCriteria.map(criteria => ({
        ...createEvaluationScoreRequest(),
        criteriaId: criteria.id
      }))
    }
  })
}

/**
 * Handle update session
 */
async function handleUpdate() {
  if (!form.sessionType || !form.evaluationDate) {
    ElMessage.warning(t.value('evaluationSession.create.validation.fillRequired'))
    return
  }
  
  await withLoading(async () => {
    const payload = {
      evaluationDate: form.evaluationDate,
      overallComment: form.overallComment || '',
      scores: form.scores.filter(s => s.criteriaId && (s.score !== null || s.comment))
    }
    
    await executeApi(
      () => updateEvaluationSession(sessionId, payload),
      'evaluationSession.messages.updateSuccess',
      true
    )
    
    // Navigate back to detail page
    const prefix = authStore.userRole === 'MENTOR' ? '/mentor/evaluation-sessions' : '/admin/evaluation-sessions'
    router.push(`${prefix}/${sessionId}`)
  })
}

/**
 * Navigate back
 */
function goBack() {
  const prefix = authStore.userRole === 'MENTOR' ? '/mentor/evaluation-sessions' : '/admin/evaluation-sessions'
  router.push(`${prefix}/${sessionId}`)
}

onMounted(async () => {
  await fetchCriteria()
  await fetchSessionDetail()
})
</script>

<template>
  <component :is="LayoutComponent">
    <div class="evaluation-session-edit-view" v-loading="loading">
      <div class="page-header">
        <div class="header-left">
          <el-button 
            :icon="ArrowLeft" 
            text 
            @click="goBack"
            class="back-button"
          >
            {{ t('evaluationSession.detail.backToList') }}
          </el-button>
          <h1 class="page-title">
            {{ t('evaluationSession.detail.edit') }}
          </h1>
        </div>
      </div>

      <el-row :gutter="24">
        <!-- Left Column: Session Info -->
        <el-col :xs="24" :lg="10">
          <el-card shadow="hover" class="info-card mb-24">
            <template #header>
              <h3>{{ t('evaluationSession.detail.sections.basicInfo') }}</h3>
            </template>
            
            <div class="info-list">
              <div class="info-item">
                <span class="label">{{ t('evaluationSession.table.internName') }}:</span>
                <span class="value font-medium">{{ sessionData.internName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">{{ t('evaluationSession.table.sessionType') }}:</span>
                <div class="value">
                  <el-tag type="info">
                    {{ getSessionTypeLabel(sessionData.sessionType) }}
                  </el-tag>
                </div>
              </div>
              <div class="info-item">
                <span class="label">{{ t('evaluationSession.table.mentorName') }}:</span>
                <span class="value">{{ sessionData.mentorName || '-' }}</span>
              </div>
              <div class="info-item" v-if="sessionData.finalScore !== null && sessionData.finalScore !== undefined">
                <span class="label">{{ t('evaluationSession.table.finalScore') }}:</span>
                <span class="value font-medium">{{ sessionData.finalScore.toFixed(2) }}</span>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- Right Column: Edit Form -->
        <el-col :xs="24" :lg="14">
          <el-card shadow="hover" class="form-card">
            <template #header>
              <h3>{{ t('evaluationSession.detail.edit') }}</h3>
            </template>

            <el-form :model="form" label-width="150px">
              <!-- Read-only Session Type -->
              <el-form-item :label="t('evaluationSession.create.form.sessionType')">
                <el-select 
                  v-model="form.sessionType"
                  disabled
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
                <div class="form-hint">
                  {{ t('evaluationSession.edit.cannotChangeSessionType') }}
                </div>
              </el-form-item>

              <el-form-item :label="t('evaluationSession.create.form.evaluationDate')" required>
                <el-date-picker
                  v-model="form.evaluationDate"
                  type="date"
                  format="DD/MM/YYYY"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>

              <el-form-item :label="t('evaluationSession.create.form.overallComment')">
                <el-input
                  v-model="form.overallComment"
                  type="textarea"
                  :rows="4"
                  :placeholder="t('evaluationSession.create.form.overallCommentPlaceholder')"
                />
              </el-form-item>

              <!-- Scores Section -->
              <el-form-item :label="t('evaluationSession.create.form.scores')">
                <div class="scores-section">
                  <div 
                    v-for="(score, index) in form.scores" 
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
                  @click="handleUpdate"
                  :loading="loading"
                >
                  {{ t('evaluationSession.edit.update') }}
                </el-button>
                <el-button @click="goBack">
                  {{ t('evaluationSession.create.cancel') }}
                </el-button>
                <div v-if="!form.sessionType || !form.evaluationDate" class="form-hint">
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
.evaluation-session-edit-view {
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

.info-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.label {
  font-size: 14px;
  color: #909399;
}

.value {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.font-medium {
  font-weight: 600;
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
  color: #909399;
  margin-top: 4px;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-card__body) {
  padding: 20px;
}

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
</style>
