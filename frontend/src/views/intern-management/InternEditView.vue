<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useLocaleStore } from '@/locales/locale'
import { useAuthStore } from '@/stores/auth'
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue"
import MentorLayout from "@/layouts/dashboard/MentorLayout.vue"
import { getInternById, updateIntern } from '@/api/intern'
import { getPositions } from '@/api/position'
import { getMentors } from '@/api/user'
import { getEvaluationCriteria } from '@/api/evaluation-criteria'
import { getWeeklyReportsByInternId, createWeeklyReport, updateWeeklyReport } from '@/api/weekly-report'

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

// Weekly Report data
const evaluationCriteria = ref([])
const weeklyReports = ref([])
const selectedReportId = ref(null)
const reportLoading = ref(false)
const reportSaving = ref(false)
const showReportForm = ref(false)

const reportForm = reactive({
  weekStartDate: '',
  weekEndDate: '',
  tasksAssigned: '',
  tasksCompleted: '',
  issuesRisks: '',
  mentorOverallComment: '',
  details: []
})

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

// Group criteria by category
const criteriaByCategory = computed(() => {
  const grouped = { EXPERTISE: [], MINDSET: [], SKILLS: [] }
  evaluationCriteria.value.forEach(c => {
    if (grouped[c.category]) {
      grouped[c.category].push(c)
    }
  })
  return grouped
})

// Get score definition by label
const getScoreDefinition = (criteria, label) => {
  return criteria.scoreDefinitions?.find(sd => sd.scoreLabel === label)
}

// Calculate overall average score
const getOverallAverage = computed(() => {
  let total = 0
  let count = 0
  reportForm.details.forEach(d => {
    if (d.score !== null && d.score !== '') {
      total += Number(d.score)
      count++
    }
  })
  return count > 0 ? (total / count).toFixed(1) : '-'
})

// Initialize report form with criteria
const initReportForm = (report = null) => {
  if (report) {
    reportForm.weekStartDate = report.weekStartDate || ''
    reportForm.tasksAssigned = report.tasksAssigned || ''
    reportForm.tasksCompleted = report.tasksCompleted || ''
    reportForm.issuesRisks = report.issuesRisks || ''
    reportForm.mentorOverallComment = report.mentorOverallComment || ''
    reportForm.details = evaluationCriteria.value.map(c => {
      const existing = report.details?.find(d => d.criteriaId === c.id)
      return {
        criteriaId: c.id,
        score: existing?.score ?? null,
        comment: existing?.comment || ''
      }
    })
    updateEndDate()
  } else {
    const today = new Date()
    const monday = new Date(today)
    monday.setDate(today.getDate() - today.getDay() + 1)
    reportForm.weekStartDate = monday.toISOString().split('T')[0]
    reportForm.tasksAssigned = ''
    reportForm.tasksCompleted = ''
    reportForm.issuesRisks = ''
    reportForm.mentorOverallComment = ''
    reportForm.details = evaluationCriteria.value.map(c => ({
      criteriaId: c.id,
      score: null,
      comment: ''
    }))
    updateEndDate()
  }
}

// Update end date to 5 days after start date
function updateEndDate() {
  if (reportForm.weekStartDate) {
    const startDate = new Date(reportForm.weekStartDate)
    const endDate = new Date(startDate)
    endDate.setDate(startDate.getDate() + 5)
    reportForm.weekEndDate = endDate.toISOString().split('T')[0]
  } else {
    reportForm.weekEndDate = ''
  }
}

// Fetch evaluation criteria
async function fetchEvaluationCriteria() {
  try {
    const res = await getEvaluationCriteria()
    evaluationCriteria.value = res.data?.data || []
  } catch (error) {
    console.error("Failed to load criteria:", error)
  }
}

// Fetch weekly reports for intern
async function fetchWeeklyReports() {
  reportLoading.value = true
  try {
    const res = await getWeeklyReportsByInternId(internId)
    weeklyReports.value = res.data?.data || []
  } catch (error) {
    console.error("Failed to load reports:", error)
    ElMessage.error(t.value('weeklyReport.messages.loadError'))
  } finally {
    reportLoading.value = false
  }
}

// Open new report form
function openNewReportForm() {
  selectedReportId.value = null
  initReportForm()
  showReportForm.value = true
}

// Edit existing report
function editReport(report) {
  selectedReportId.value = report.id
  initReportForm(report)
  showReportForm.value = true
}

// Cancel report form
function cancelReportForm() {
  showReportForm.value = false
  selectedReportId.value = null
}

// Save report
async function saveReport() {
  reportSaving.value = true
  try {
    const payload = {
      internId: Number(internId),
      weekStartDate: reportForm.weekStartDate,
      tasksAssigned: reportForm.tasksAssigned,
      tasksCompleted: reportForm.tasksCompleted,
      issuesRisks: reportForm.issuesRisks,
      mentorOverallComment: reportForm.mentorOverallComment,
      details: reportForm.details.filter(d => d.score !== null && d.score !== '')
    }

    if (selectedReportId.value) {
      await updateWeeklyReport(selectedReportId.value, payload)
      ElMessage.success(t.value('weeklyReport.messages.updateSuccess'))
    } else {
      await createWeeklyReport(payload)
      ElMessage.success(t.value('weeklyReport.messages.createSuccess'))
    }

    showReportForm.value = false
    await fetchWeeklyReports()
  } catch (error) {
    console.error("Save report error:", error)
    ElMessage.error(t.value('weeklyReport.messages.saveError'))
  } finally {
    reportSaving.value = false
  }
}

// Get detail for a criteria
const getDetailForCriteria = (criteriaId) => {
  return reportForm.details.find(d => d.criteriaId === criteriaId) || { score: null, comment: '' }
}

// Update score for criteria
const updateScore = (criteriaId, score) => {
  const detail = reportForm.details.find(d => d.criteriaId === criteriaId)
  if (detail) {
    detail.score = score
  }
}

// Update comment for criteria
const updateComment = (criteriaId, comment) => {
  const detail = reportForm.details.find(d => d.criteriaId === criteriaId)
  if (detail) {
    detail.comment = comment
  }
}

// Watch for tab change to load reports
watch(activeTab, async (newTab) => {
  if (newTab === 'reports' && evaluationCriteria.value.length === 0) {
    await fetchEvaluationCriteria()
    await fetchWeeklyReports()
  }
})

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
          <div class="tab-content reports-content" v-loading="reportLoading">

            <template v-if="!showReportForm">
            
              <div class="reports-header">
                <h3>{{ t('weeklyReport.title') }}</h3>
                <el-button type="primary" @click="openNewReportForm">
                  {{ t('weeklyReport.createNew') }}
                </el-button>
              </div>

              
              <div v-if="weeklyReports.length > 0" class="reports-list">
                <el-table :data="weeklyReports" stripe>
                  <el-table-column prop="weekNumber" :label="t('weeklyReport.weekNumber')" width="100">
                    <template #default="{ row }">
                      {{ t('weeklyReport.weekNumber') }} {{ row.weekNumber }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="weekStartDate" :label="t('weeklyReport.weekStartDate')" width="150">
                    <template #default="{ row }">
                      {{ formatDate(row.weekStartDate) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="averageScore" :label="t('weeklyReport.table.averageScore')" width="120">
                    <template #default="{ row }">
                      <el-tag :type="row.averageScore >= 7 ? 'success' : row.averageScore >= 5 ? 'warning' : 'danger'">
                        {{ row.averageScore?.toFixed(1) || '-' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="status" :label="t('internManagement.table.status')" width="120">
                    <template #default="{ row }">
                      <el-tag :type="row.status === 'SUBMITTED' ? 'success' : 'info'">
                        {{ t('weeklyReport.status.' + row.status) || row.status }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column :label="t('internManagement.table.actions')" width="150">
                    <template #default="{ row }">
                      <el-button text type="primary" @click="editReport(row)">
                        {{ t('weeklyReport.actions.edit') }}
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
              <div v-else class="no-reports">
                <p>{{ t('weeklyReport.noReports') }}</p>
              </div>
            </template>

            
            <template v-else>
              <div class="report-form">
                <div class="form-header">
                  <h3>{{ selectedReportId ? t('weeklyReport.title') : t('weeklyReport.createNew') }}</h3>
                  <div class="date-range">
                    <div class="date-field">
                      <label>{{ t('weeklyReport.form.startDate') }}</label>
                      <el-date-picker
                        v-model="reportForm.weekStartDate"
                        type="date"
                        :placeholder="t('weeklyReport.form.startDate')"
                        format="DD/MM/YYYY"
                        value-format="YYYY-MM-DD"
                        @change="updateEndDate"
                      />
                    </div>
                    <div class="date-field">
                      <label>{{ t('weeklyReport.form.endDate') }}</label>
                      <el-date-picker
                        v-model="reportForm.weekEndDate"
                        type="date"
                        :placeholder="t('weeklyReport.form.endDate')"
                        format="DD/MM/YYYY"
                        value-format="YYYY-MM-DD"
                        disabled
                      />
                    </div>
                  </div>
                </div>

                <el-card class="tasks-card" shadow="never">
                  <div class="tasks-grid">
                    <div class="task-field">
                      <label>{{ t('weeklyReport.form.tasksAssigned') }}</label>
                      <el-input
                        v-model="reportForm.tasksAssigned"
                        type="textarea"
                        :rows="3"
                        :placeholder="t('weeklyReport.form.tasksAssigned')"
                      />
                    </div>
                    <div class="task-field">
                      <label>{{ t('weeklyReport.form.tasksCompleted') }}</label>
                      <el-input
                        v-model="reportForm.tasksCompleted"
                        type="textarea"
                        :rows="3"
                        :placeholder="t('weeklyReport.form.tasksCompleted')"
                      />
                    </div>
                    <div class="task-field">
                      <label>{{ t('weeklyReport.form.issuesRisks') }}</label>
                      <el-input
                        v-model="reportForm.issuesRisks"
                        type="textarea"
                        :rows="3"
                        :placeholder="t('weeklyReport.form.issuesRisks')"
                      />
                    </div>
                  </div>
                </el-card>

                
                <template v-for="(categoryKey, index) in ['EXPERTISE', 'SKILLS', 'MINDSET']" :key="categoryKey">
                  <el-card class="category-card" shadow="never">
                    <template #header>
                      <div class="category-header">
                        <span class="category-number">{{ ['I', 'II', 'III'][index] }}.</span>
                        <span class="category-title">{{ t('weeklyReport.category.' + categoryKey) }}</span>
                      </div>
                    </template>

                    <el-table :data="criteriaByCategory[categoryKey]" border class="criteria-table">
                      <el-table-column :label="t('weeklyReport.table.criteria')" width="180">
                        <template #default="{ row }">
                          <div class="criteria-name">{{ row.name }}</div>
                        </template>
                      </el-table-column>
                      <el-table-column :label="t('weeklyReport.table.excellent')">
                        <template #default="{ row }">
                          <div class="score-def excellent">
                            {{ getScoreDefinition(row, 'Excellent')?.description || '-' }}
                          </div>
                        </template>
                      </el-table-column>
                      <el-table-column :label="t('weeklyReport.table.good')">
                        <template #default="{ row }">
                          <div class="score-def good">
                            {{ getScoreDefinition(row, 'Good')?.description || '-' }}
                          </div>
                        </template>
                      </el-table-column>
                      <el-table-column :label="t('weeklyReport.table.average')">
                        <template #default="{ row }">
                          <div class="score-def average">
                            {{ getScoreDefinition(row, 'Average')?.description || '-' }}
                          </div>
                        </template>
                      </el-table-column>
                      <el-table-column :label="t('weeklyReport.table.weak')">
                        <template #default="{ row }">
                          <div class="score-def weak">
                            {{ getScoreDefinition(row, 'Weak')?.description || '-' }}
                          </div>
                        </template>
                      </el-table-column>
                      <el-table-column :label="t('weeklyReport.table.score')" width="90" align="center">
                        <template #default="{ row }">
                          <el-input-number
                            :model-value="getDetailForCriteria(row.id).score"
                            @update:model-value="(val) => updateScore(row.id, val)"
                            :min="0"
                            :max="10"
                            :precision="0"
                            size="small"
                            controls-position="right"
                            class="score-input"
                          />
                        </template>
                      </el-table-column>
                      <el-table-column :label="t('weeklyReport.table.comment')" width="180">
                        <template #default="{ row }">
                          <el-input
                            :model-value="getDetailForCriteria(row.id).comment"
                            @update:model-value="(val) => updateComment(row.id, val)"
                            :placeholder="t('weeklyReport.form.commentPlaceholder')"
                            size="small"
                          />
                        </template>
                      </el-table-column>
                    </el-table>
                  </el-card>
                </template>

                <div class="overall-average">
                  <span class="average-label">{{ t('weeklyReport.table.averageScore') }}:</span>
                  <el-tag 
                    size="large"
                    :type="getOverallAverage >= 7 ? 'success' : getOverallAverage >= 5 ? 'warning' : 'info'"
                    class="average-tag"
                  >
                    {{ getOverallAverage }}
                  </el-tag>
                </div>

              
                <el-card class="comment-card" shadow="never">
                  <template #header>
                    <div class="card-header">
                      <span>{{ t('weeklyReport.form.mentorComment') }}</span>
                    </div>
                  </template>
                  <el-input
                    v-model="reportForm.mentorOverallComment"
                    type="textarea"
                    :rows="4"
                    :placeholder="t('weeklyReport.form.commentPlaceholder')"
                  />
                </el-card>

              
                <div class="form-actions">
                  <el-button @click="cancelReportForm">
                    {{ t('weeklyReport.actions.cancel') }}
                  </el-button>
                  <el-button type="primary" @click="saveReport" :loading="reportSaving">
                    {{ t('weeklyReport.actions.save') }}
                  </el-button>
                </div>
              </div>
            </template>
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

/* Weekly Report Styles */
.reports-content {
  background: white;
  border-radius: 8px;
  padding: 24px;
  min-height: 400px;
}

.reports-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.reports-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.reports-list {
  margin-bottom: 24px;
}

.no-reports {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.report-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.form-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.date-range {
  display: flex;
  gap: 16px;
}

.date-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.date-field label {
  font-size: 12px;
  font-weight: 600;
  color: #606266;
}

.category-card {
  border-radius: 8px;
  overflow: hidden;
}

.category-card :deep(.el-card__header) {
  padding: 12px 16px;
  background-color: #409eff;
  color: white;
}

.category-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-number {
  font-weight: 700;
  font-size: 16px;
}

.category-title {
  font-weight: 600;
  font-size: 15px;
}

.criteria-table {
  width: 100%;
}

.criteria-table :deep(.el-table__header th) {
  background-color: #f5f7fa;
  font-weight: 600;
  font-size: 13px;
}

.criteria-name {
  font-weight: 600;
  font-size: 13px;
  color: #2c3e50;
}

.score-def {
  font-size: 12px;
  line-height: 1.5;
  padding: 4px;
  background-color: #fff;
  color: #303133;
}

.tasks-card {
  border-radius: 8px;
  margin-bottom: 16px;
}

.tasks-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.task-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.task-field label {
  font-size: 13px;
  font-weight: 600;
  color: #2c3e50;
}

.overall-average {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  padding: 16px 0;
  font-weight: 600;
  color: #2c3e50;
}

.average-label {
  font-size: 16px;
}

.average-tag {
  font-size: 18px;
  padding: 8px 16px;
}

.score-input {
  width: 80px;
}

.comment-card {
  border-radius: 8px;
}

.comment-card :deep(.el-card__header) {
  padding: 12px 16px;
  background-color: #f5f7fa;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}
</style>
