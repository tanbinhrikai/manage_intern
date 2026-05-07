<script setup>
import { ref, computed, onMounted, onBeforeUnmount, onDeactivated, watch } from "vue"
import { useRouter, onBeforeRouteLeave } from "vue-router"
import { Search, Plus, View, Edit, Delete } from "@element-plus/icons-vue"
import { useLocaleStore } from "@/locales/locale"
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue"
import MentorLayout from "@/layouts/dashboard/MentorLayout.vue"
import {
  getEvaluationSessions,
  deleteEvaluationSession,
  generateEvaluationSession
} from "@/api/evaluation-session"
import { usePagination, useLoading, useApi, useDropdownData, useDialog, useDateFormat, useConfirm } from "@/composables"
import { SessionType, EvaluationConclusion } from "@/types/common"
import { useAuthStore } from "@/stores/auth"

const localeStore = useLocaleStore()
const authStore = useAuthStore()
const router = useRouter()
const t = computed(() => localeStore.t)

// Layout component based on role
const LayoutComponent = computed(() => 
  authStore.userRole === 'MENTOR' ? MentorLayout : AdminLayout
)

// Composables
const pagination = usePagination({
  initialPage: 1,
  initialPageSize: 10,
  onPageChange: () => fetchSessions()
})

const { loading, withLoading } = useLoading()
const { execute: executeApi } = useApi({
  showErrorMessage: true,
  showSuccessMessage: true
})

const { interns, fetchInterns } = useDropdownData()
const sessionDetailDialog = useDialog()
const { formatDate } = useDateFormat()
const { confirmDelete, confirmUpdate } = useConfirm()

// Data
const sessions = ref([])
const searchKeyword = ref("")
const filterInternId = ref("")
const filterSessionType = ref("")

// Watch filters for auto-search
watch([filterInternId, filterSessionType], () => {
  handleSearch()
})

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
 * Fetch evaluation sessions from backend
 */
async function fetchSessions() {
  await withLoading(async () => {
    const params = {
      ...pagination.apiParams.value,
      internId: filterInternId.value || undefined
    }
    
    const res = await executeApi(
      () => getEvaluationSessions(params),
      null,
      'evaluationSession.messages.loadError'
    )
    
    let items = res.data?.data?.items || []
    
    // Filter by session type on frontend (backend doesn't support this filter yet)
    if (filterSessionType.value) {
      items = items.filter(session => session.sessionType === filterSessionType.value)
    }
    
    // Filter by keyword on frontend
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      items = items.filter(session => 
        (session.internName && session.internName.toLowerCase().includes(keyword)) ||
        (session.mentorName && session.mentorName.toLowerCase().includes(keyword))
      )
    }
    
    sessions.value = items
    pagination.setTotalItems(items.length)
  })
}

/**
 * Handle search - reset to first page and fetch
 */
function handleSearch() {
  pagination.firstPage()
  fetchSessions()
}

/**
 * Navigate to detail page for a session
 * @param {EvaluationSession} session - Session to view
 */
function openDetailSession(session) {
  router.push(`/admin/evaluation-sessions/${session.id}`)
}

/**
 * Navigate to edit page for a session
 * @param {EvaluationSession} session - Session to edit
 */
function openEditSession(session) {
  router.push(`/admin/evaluation-sessions/${session.id}/edit`)
}

/**
 * Handle delete session with confirmation
 * @param {EvaluationSession} session - Session to delete
 */
function handleDeleteSession(session) {
  const confirmMessage = t.value('evaluationSession.confirm.delete')
    .replace('{internName}', session.internName || '')
    .replace('{sessionType}', getSessionTypeLabel(session.sessionType))

  confirmDelete({
    message: confirmMessage,
    title: t.value('evaluationSession.confirm.title'),
    onConfirm: async () => {
      await executeApi(
        () => deleteEvaluationSession(session.id),
        'evaluationSession.messages.deleteSuccess',
        true
      )
      fetchSessions()
    }
  })
}

/**
 * Handle generate session from weekly reports
 * @param {number} internId - Intern ID
 * @param {SessionType} sessionType - Session type
 */
async function handleGenerateSession(internId, sessionType) {
  try {
    await executeApi(
      () => generateEvaluationSession(internId, sessionType),
      'evaluationSession.messages.generateSuccess',
      true
    )
    fetchSessions()
  } catch (error) {
    // Error already handled by useApi
  }
}

/**
 * Handle generate session with confirmation
 * @param {number} internId - Intern ID
 * @param {SessionType} sessionType - Session type
 */
function handleGenerateSessionWithConfirm(internId, sessionType) {
  confirmUpdate({
    message: t.value('evaluationSession.confirm.generate') || t.value('evaluationSession.create.confirmGenerate') || 'Are you sure you want to generate this evaluation session?',
    onConfirm: () => handleGenerateSession(internId, sessionType)
  })
}

/**
 * Handle pagination page change
 * @param {number} page - New page number
 */
function handlePageChange(page) {
  pagination.setPage(page)
}

onMounted(() => {
  fetchSessions()
  fetchInterns()
})

onBeforeUnmount(() => {
  sessionDetailDialog.reset()
})

onDeactivated(() => {
  sessionDetailDialog.reset()
})

onBeforeRouteLeave(() => {
  sessionDetailDialog.reset()
})
</script>

<template>
  <component :is="LayoutComponent">
    <div class="evaluation-session-list-view">
      <el-card class="main-card" shadow="never">
        <template #header>
          <div class="card-header">
            <h2 class="page-title">{{ t('evaluationSession.title') }}</h2>
          </div>
        </template>

        <div class="toolbar">
          <div class="search-group">
            <el-input 
              v-model="searchKeyword" 
              :placeholder="t('evaluationSession.searchByKeyword')"
              :prefix-icon="Search"
              clearable
              class="search-input"
            />
            <el-select 
              v-model="filterInternId" 
              :placeholder="t('evaluationSession.allInterns')"
              clearable
              class="filter-select"
              style="width: 200px;"
            >
              <el-option :label="t('evaluationSession.allInterns')" value="" />
              <el-option
                v-for="intern in interns"
                :key="intern.id"
                :label="intern.fullName"
                :value="intern.id"
              />
            </el-select>
            <el-select 
              v-model="filterSessionType" 
              :placeholder="t('evaluationSession.allSessionTypes')"
              clearable
              class="filter-select"
            >
              <el-option :label="t('evaluationSession.allSessionTypes')" value="" />
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
          </div>
          
          <el-button 
            type="primary"
            :icon="Plus"
            @click="router.push('/admin/evaluation-sessions/create')"
          >
            {{ t('evaluationSession.addNew') }}
          </el-button>
        </div>

        <el-table 
          :data="sessions" 
          stripe 
          style="width: 100%"
          v-loading="loading"
        >
          <el-table-column 
            prop="internName" 
            :label="t('evaluationSession.table.internName')" 
            min-width="150" 
          />
          <el-table-column 
            :label="t('evaluationSession.table.sessionType')" 
            min-width="120"
          >
            <template #default="scope">
              <el-tag type="info">
                {{ getSessionTypeLabel(scope.row.sessionType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column 
            :label="t('evaluationSession.table.evaluationDate')" 
            min-width="120"
          >
            <template #default="scope">
              {{ formatDate(scope.row.evaluationDate) }}
            </template>
          </el-table-column>
          <el-table-column 
            :label="t('evaluationSession.table.finalScore')" 
            min-width="100"
            align="center"
          >
            <template #default="scope">
              <span v-if="scope.row.finalScore !== null && scope.row.finalScore !== undefined">
                {{ scope.row.finalScore.toFixed(2) }}
              </span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column 
            :label="t('evaluationSession.table.conclusion')" 
            min-width="140"
            align="center"
          >
            <template #default="scope">
              <el-tag :type="getConclusionInfo(scope.row.conclusion).type">
                {{ getConclusionInfo(scope.row.conclusion).label }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column 
            prop="mentorName" 
            :label="t('evaluationSession.table.mentorName')" 
            min-width="150" 
          />
          <el-table-column 
            :label="t('evaluationSession.table.actions')" 
            min-width="150" 
            fixed="right" 
            align="center"
          >
            <template #default="scope">
              <el-button 
                type="primary" 
                :icon="View" 
                size="small" 
                circle
                @click="openDetailSession(scope.row)"
              />
              <el-button 
                type="warning" 
                :icon="Edit" 
                size="small" 
                circle
                @click="openEditSession(scope.row)"
              />
              <el-button 
                type="danger" 
                :icon="Delete" 
                size="small" 
                circle
                @click="handleDeleteSession(scope.row)"
              />
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper" v-if="pagination.totalItems.value > pagination.pageSize.value">
          <el-pagination
            :current-page="pagination.currentPage.value"
            :page-size="pagination.pageSize.value"
            :total="pagination.totalItems.value"
            layout="prev, pager, next"
            background
            @current-change="handlePageChange"
          />
        </div>
      </el-card>
    </div>
  </component>
</template>

<style scoped>
.evaluation-session-list-view {
  max-width: 1400px;
  margin: 0 auto;
}

.main-card {
  border-radius: 12px;
}

.main-card :deep(.el-card__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #ebeef5;
}

.main-card :deep(.el-card__body) {
  padding: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 16px;
}

.search-group {
  display: flex;
  gap: 12px;
  flex: 1;
  max-width: 600px;
}

.search-input {
  width: 200px;
}

.filter-select {
  width: 180px;
}

.text-muted {
  color: #9ca3af;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

:deep(.el-table) {
  border-radius: 8px;
}

:deep(.el-table th) {
  background-color: #f9fafb !important;
  font-weight: 600;
  color: #374151;
}

:deep(.el-button.is-circle) {
  margin: 0 4px;
}
</style>
