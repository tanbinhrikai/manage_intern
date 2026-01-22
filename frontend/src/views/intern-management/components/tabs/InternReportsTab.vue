<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useLocaleStore } from '@/locales/locale'
import { getEvaluationCriteria } from '@/api/evaluation-criteria'
import { getWeeklyReportsByInternId, createWeeklyReport, updateWeeklyReport } from '@/api/weekly-report'

const props = defineProps({
  internId: {
    type: [String, Number],
    required: true
  }
})

const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)

// Raw data from API
const criteriaGroups = ref([])

// Flat list of all child criteria (only children have scoreDefinitions)
const allCriteria = computed(() => {
  const criteria = []
  criteriaGroups.value.forEach(group => {
    (group.mainCriteria || []).forEach(main => {
      (main.children || []).forEach(child => {
        criteria.push({
          ...child,
          parentId: main.id,
          groupId: group.id,
          groupName: group.name,
          parentName: main.name
        })
      })
    })
  })
  return criteria
})

// Group criteria by their group for display
const criteriaByGroup = computed(() => {
  const grouped = {}
  criteriaGroups.value.forEach(group => {
    const children = []
    ;(group.mainCriteria || []).forEach(main => {
      (main.children || []).forEach((child, index) => {
        children.push({
          ...child,
          parentName: main.name,
          isParentStart: index === 0,
          parentRowSpan: main.children.length
        })
      })
    })
    if (children.length > 0) {
      grouped[group.id] = {
        name: group.name,
        displayOrder: group.displayOrder,
        criteria: children
      }
    }
  })
  return grouped
})

// Weekly Report data
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

// Get score definition by label (UPPERCASE)
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
    reportForm.details = allCriteria.value.map(c => {
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
    reportForm.details = allCriteria.value.map(c => ({
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

// Fetch evaluation criteria (hierarchy structure)
async function fetchEvaluationCriteria() {
  try {
    const res = await getEvaluationCriteria()
    criteriaGroups.value = res.data?.data || []
  } catch (error) {
    console.error("Failed to load criteria:", error)
  }
}

// Fetch weekly reports for intern
async function fetchWeeklyReports() {
  if (!props.internId) return
  reportLoading.value = true
  try {
    const res = await getWeeklyReportsByInternId(props.internId)
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


const objectSpanMethod = ({ row, column, rowIndex, columnIndex }) => {
  if (columnIndex === 0) {
    if (row.isParentStart) {
      return {
        rowspan: row.parentRowSpan,
        colspan: 1,
      }
    } else {
      return {
        rowspan: 0,
        colspan: 0,
      }
    }
  }
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
      internId: Number(props.internId),
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

function formatDate(date) {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleDateString('en-GB')
}

// Get status type for el-tag
function getStatusType(status) {
  switch(status) {
    case 'SUBMITTED': return 'success'
    case 'PENDING': return 'warning'
    default: return 'info'
  }
}

// Initialize
onMounted(async () => {
    await fetchEvaluationCriteria()
    await fetchWeeklyReports()
})
</script>

<template>
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
                :autosize="{ minRows: 4, maxRows: 10 }"
                resize="none"
                :placeholder="t('weeklyReport.form.tasksAssigned')"
              />
            </div>
            <div class="task-field">
              <label>{{ t('weeklyReport.form.tasksCompleted') }}</label>
              <el-input
                v-model="reportForm.tasksCompleted"
                type="textarea"
                :autosize="{ minRows: 4, maxRows: 10 }"
                resize="none"
                :placeholder="t('weeklyReport.form.tasksCompleted')"
              />
            </div>
            <div class="task-field">
              <label>{{ t('weeklyReport.form.issuesRisks') }}</label>
              <el-input
                v-model="reportForm.issuesRisks"
                type="textarea"
                :autosize="{ minRows: 4, maxRows: 10 }"
                resize="none"
                :placeholder="t('weeklyReport.form.issuesRisks')"
              />
            </div>
          </div>
        </el-card>

        <!-- Criteria grouped by Group -->
        <template v-for="(group, groupId) in criteriaByGroup" :key="groupId">
          <el-card class="category-card" shadow="never">
            <template #header>
              <div class="category-header">
                <span class="category-title">{{ group.name }}</span>
              </div>
            </template>

            <el-table :data="group.criteria" border class="criteria-table" :span-method="objectSpanMethod">
              <el-table-column :label="t('weeklyReport.table.criteriaParent')" width="100" header-align="center">
                <template #default="{ row }">
                  <div class="parent-criteria-name" style="font-size: 12px">
                    {{ row.parentName }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column :label="t('weeklyReport.table.criteriaChild')" width="100" header-align="center">
                <template #default="{ row }">
                  <div class="child-criteria-name" style="font-size: 12px">{{ row.name }}</div>
                </template>
              </el-table-column>
              <el-table-column :label="t('weeklyReport.table.excellent')">
                <template #default="{ row }">
                  <div class="score-def excellent">
                    {{ getScoreDefinition(row, 'EXCELLENT')?.description || '-' }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column :label="t('weeklyReport.table.good')">
                <template #default="{ row }">
                  <div class="score-def good">
                    {{ getScoreDefinition(row, 'GOOD')?.description || '-' }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column :label="t('weeklyReport.table.average')">
                <template #default="{ row }">
                  <div class="score-def average">
                    {{ getScoreDefinition(row, 'AVERAGE')?.description || '-' }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column :label="t('weeklyReport.table.weak')">
                <template #default="{ row }">
                  <div class="score-def weak">
                    {{ getScoreDefinition(row, 'WEAK')?.description || '-' }}
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
                    type="textarea"
                    :autosize="{ minRows: 1, maxRows: 6 }"
                    resize="none"
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
</template>

<style scoped>
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

.criteria-parent {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
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
