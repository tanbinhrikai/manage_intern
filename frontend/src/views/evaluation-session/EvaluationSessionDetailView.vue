<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Edit, Delete } from '@element-plus/icons-vue'
import { useLocaleStore } from '@/locales/locale'
import { useAuthStore } from '@/stores/auth'
import AdminLayout from '@/layouts/dashboard/AdminLayout.vue'
import MentorLayout from '@/layouts/dashboard/MentorLayout.vue'
import { getEvaluationSessionById, deleteEvaluationSession } from '@/api/evaluation-session'
import { useLoading, useApi, useDateFormat, useConfirm } from '@/composables'
import { SessionType, EvaluationConclusion, CriteriaCategory } from '@/types/common'

const route = useRoute()
const router = useRouter()
const localeStore = useLocaleStore()
const authStore = useAuthStore()
const t = computed(() => localeStore.t)

const sessionId = route.params.id
const session = ref({})

const LayoutComponent = computed(() => 
  authStore.userRole === 'MENTOR' ? MentorLayout : AdminLayout
)

const { loading, withLoading } = useLoading()
const { formatDate, formatDateTime } = useDateFormat()
const { execute: executeApi } = useApi({
  showErrorMessage: true,
  showSuccessMessage: true
})
const { confirmDelete } = useConfirm()

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
 * Get conclusion type and label
 * @param {EvaluationConclusion} conclusion - Conclusion
 * @returns {Object} Type and label
 */
function getConclusionInfo(conclusion) {
  if (!conclusion) return { type: 'info', label: '-' }
  
  const map = {
    [EvaluationConclusion.PASS]: { type: 'success', label: t.value('evaluationSession.conclusion.pass') },
    [EvaluationConclusion.NEED_IMPROVEMENT]: { type: 'warning', label: t.value('evaluationSession.conclusion.needImprovement') },
    [EvaluationConclusion.FAIL]: { type: 'danger', label: t.value('evaluationSession.conclusion.fail') }
  }
  return map[conclusion] || { type: 'info', label: conclusion }
}

/**
 * Get criteria category label
 * @param {CriteriaCategory} category - Category
 * @returns {string} Label
 */
function getCategoryLabel(category) {
  const labels = {
    [CriteriaCategory.WORK_PERFORMANCE]: t.value('evaluationSession.detail.categories.workPerformance'),
    [CriteriaCategory.ATTITUDE_SOFT_SKILLS]: t.value('evaluationSession.detail.categories.attitudeSoftSkills'),
    [CriteriaCategory.KNOWLEDGE_APPLICATION]: t.value('evaluationSession.detail.categories.knowledgeApplication')
  }
  return labels[category] || category
}

/**
 * Group scores by category
 * @returns {Object} Grouped scores
 */
const groupedScores = computed(() => {
  if (!session.value.scores || !Array.isArray(session.value.scores)) {
    return {}
  }
  
  const grouped = {}
  session.value.scores.forEach(score => {
    const category = score.criteriaCategory || 'OTHER'
    if (!grouped[category]) {
      grouped[category] = []
    }
    grouped[category].push(score)
  })
  
  return grouped
})

/**
 * Fetch evaluation session detail
 */
async function fetchSessionDetail() {
  await withLoading(async () => {
    const res = await executeApi(
      () => getEvaluationSessionById(sessionId),
      null,
      'evaluationSession.messages.loadError'
    )
    session.value = res.data?.data || {}
  })
}

/**
 * Navigate back to list
 */
function goBack() {
  const prefix = authStore.userRole === 'MENTOR' ? '/mentor/evaluation-sessions' : '/admin/evaluation-sessions'
  router.push(prefix)
}

/**
 * Navigate to edit page
 */
function openEditView() {
  const prefix = authStore.userRole === 'MENTOR' ? '/mentor/evaluation-sessions' : '/admin/evaluation-sessions'
  router.push(`${prefix}/${sessionId}/edit`)
}

/**
 * Handle delete session with confirmation
 */
function handleDelete() {
  const confirmMessage = t.value('evaluationSession.confirm.delete')
    .replace('{internName}', session.value.internName || '')
    .replace('{sessionType}', getSessionTypeLabel(session.value.sessionType))

  confirmDelete({
    message: confirmMessage,
    title: t.value('evaluationSession.confirm.title'),
    onConfirm: async () => {
      await executeApi(
        () => deleteEvaluationSession(sessionId),
        'evaluationSession.messages.deleteSuccess',
        true
      )
      goBack()
    }
  })
}

onMounted(() => {
  fetchSessionDetail()
})
</script>

<template>
  <component :is="LayoutComponent">
    <div class="evaluation-session-detail-view" v-loading="loading">
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
            {{ t('evaluationSession.detail.title') }}
          </h1>
        </div>
        <div class="header-actions">
          <el-button 
            type="primary" 
            :icon="Edit" 
            @click="openEditView"
          >
            {{ t('evaluationSession.detail.edit') }}
          </el-button>
          <el-button 
            type="danger" 
            :icon="Delete" 
            @click="handleDelete"
          >
            {{ t('evaluationSession.detail.delete') }}
          </el-button>
        </div>
      </div>

      <el-row :gutter="24">
        <el-col :xs="24" :lg="16">
          <!-- Basic Information Card -->
          <el-card shadow="hover" class="detail-card mb-24">
            <template #header>
              <div class="card-header">
                <h3>{{ t('evaluationSession.detail.sections.basicInfo') }}</h3>
              </div>
            </template>
            <div class="info-list">
              <div class="info-item">
                <span class="label">{{ t('evaluationSession.table.internName') }}:</span>
                <span class="value font-medium">{{ session.internName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">{{ t('evaluationSession.table.sessionType') }}:</span>
                <div class="value">
                  <el-tag type="info">
                    {{ getSessionTypeLabel(session.sessionType) }}
                  </el-tag>
                </div>
              </div>
              <div class="info-item">
                <span class="label">{{ t('evaluationSession.table.evaluationDate') }}:</span>
                <span class="value">{{ formatDate(session.evaluationDate) }}</span>
              </div>
              <div class="info-item">
                <span class="label">{{ t('evaluationSession.table.mentorName') }}:</span>
                <span class="value">{{ session.mentorName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">{{ t('evaluationSession.table.finalScore') }}:</span>
                <span class="value font-medium">
                  <span v-if="session.finalScore !== null && session.finalScore !== undefined">
                    {{ session.finalScore.toFixed(2) }}
                  </span>
                  <span v-else class="text-muted">-</span>
                </span>
              </div>
              <div class="info-item">
                <span class="label">{{ t('evaluationSession.table.conclusion') }}:</span>
                <div class="value">
                  <el-tag :type="getConclusionInfo(session.conclusion).type">
                    {{ getConclusionInfo(session.conclusion).label }}
                  </el-tag>
                </div>
              </div>
              <div class="info-item" v-if="session.levelAssessment">
                <span class="label">{{ t('evaluationSession.detail.levelAssessment') }}:</span>
                <span class="value">{{ session.levelAssessment }}</span>
              </div>
            </div>
          </el-card>

          <!-- Scores by Category -->
          <el-card 
            v-for="(scores, category) in groupedScores" 
            :key="category"
            shadow="hover" 
            class="detail-card mb-24"
          >
            <template #header>
              <div class="card-header">
                <h3>{{ getCategoryLabel(category) }}</h3>
              </div>
            </template>
            <el-table :data="scores" stripe>
              <el-table-column 
                :label="t('evaluationSession.detail.table.criteriaName')" 
                min-width="200"
              >
                <template #default="scope">
                  {{ scope.row.criteriaName || '-' }}
                </template>
              </el-table-column>
              <el-table-column 
                :label="t('evaluationSession.detail.table.score')" 
                min-width="100"
                align="center"
              >
                <template #default="scope">
                  <span v-if="scope.row.score !== null && scope.row.score !== undefined">
                    {{ scope.row.score.toFixed(2) }}
                  </span>
                  <span v-else class="text-muted">-</span>
                </template>
              </el-table-column>
              <el-table-column 
                :label="t('evaluationSession.detail.table.comment')" 
                min-width="250"
              >
                <template #default="scope">
                  <span v-if="scope.row.comment">{{ scope.row.comment }}</span>
                  <span v-else class="text-muted">-</span>
                </template>
              </el-table-column>
            </el-table>
          </el-card>

          <!-- Overall Comment -->
          <el-card 
            v-if="session.overallComment"
            shadow="hover" 
            class="detail-card"
          >
            <template #header>
              <div class="card-header">
                <h3>{{ t('evaluationSession.detail.sections.overallComment') }}</h3>
              </div>
            </template>
            <div class="comment-content">
              {{ session.overallComment }}
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="8">
          <!-- Metadata Card -->
          <el-card shadow="hover" class="detail-card">
            <template #header>
              <div class="card-header">
                <h3>{{ t('evaluationSession.detail.sections.metadata') }}</h3>
              </div>
            </template>
            <div class="info-list">
              <div class="info-item" v-if="session.createdAt">
                <span class="label">{{ t('evaluationSession.detail.createdAt') }}:</span>
                <span class="value">{{ formatDateTime(session.createdAt) }}</span>
              </div>
              <div class="info-item" v-if="session.updatedAt">
                <span class="label">{{ t('evaluationSession.detail.updatedAt') }}:</span>
                <span class="value">{{ formatDateTime(session.updatedAt) }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </component>
</template>

<style scoped>
.evaluation-session-detail-view {
  min-height: calc(100vh - 60px);
  padding: 0 10px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
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

.header-actions {
  display: flex;
  gap: 12px;
}

.detail-card {
  border-radius: 8px;
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.mb-24 {
  margin-bottom: 24px;
}

.card-header h3 {
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
  align-items: flex-start;
  min-height: 24px;
}

.label {
  width: 160px;
  color: #909399;
  font-size: 14px;
  flex-shrink: 0;
}

.value {
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}

.font-medium {
  font-weight: 600;
}

.text-muted {
  color: #9ca3af;
}

.comment-content {
  color: #303133;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-table) {
  border-radius: 8px;
}

:deep(.el-table th) {
  background-color: #f9fafb !important;
  font-weight: 600;
  color: #374151;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .label {
    width: 120px;
  }
}
</style>
