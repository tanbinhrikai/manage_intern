<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useLocaleStore } from '@/locales/locale'
import MentorLayout from '@/layouts/dashboard/MentorLayout.vue'
import { getInternsNotEvaluatedThisWeek, getMyIntern } from '@/api/intern'

const router = useRouter()
const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)

const loading = ref(false)
const internsNeedEvaluation = ref([])
const internsUnderSupervision = ref([])

const evaluationProgress = computed(() => {
  const total = internsUnderSupervision.value.length
  const needEval = internsNeedEvaluation.value.length
  const completed = total - needEval
  return { completed: completed >= 0 ? completed : 0, total }
})

const progressPercentage = computed(() => {
  if (evaluationProgress.value.total === 0) return 0
  return (evaluationProgress.value.completed / evaluationProgress.value.total) * 100
})

const getStatusType = (status) => {
  const statusMap = {
    ACTIVE: 'success',
    WARNING: 'warning',
    COMPLETED: 'primary',
    DROPPED: 'danger'
  }
  return statusMap[status] || 'info'
}

const calculateWeekNumber = (startDate) => {
  if (!startDate) return 1
  const start = new Date(startDate)
  const now = new Date()
  const diffTime = Math.abs(now - start)
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return Math.max(1, Math.ceil(diffDays / 7))
}

const calculateTotalWeeks = (startDate, endDate) => {
  if (!startDate || !endDate) return 12
  const start = new Date(startDate)
  const end = new Date(endDate)
  const diffTime = Math.abs(end - start)
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return Math.max(1, Math.ceil(diffDays / 7))
}

const handleSubmitReport = (intern) => {
  router.push(`/mentor/my-interns/${intern.id}/edit?tab=reports`)
}

const handleViewDetails = (intern) => {
  router.push(`/mentor/my-interns/${intern.id}`)
}

async function fetchData() {
  loading.value = true
  try {
    const [notEvalRes, myInternsRes] = await Promise.all([
      getInternsNotEvaluatedThisWeek({ limit: 20 }),
      getMyIntern({ limit: 100 })
    ])
    
    internsNeedEvaluation.value = notEvalRes.data?.data?.items || []
    internsUnderSupervision.value = myInternsRes.data?.data?.items || []
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<template>
  <MentorLayout>
    <div class="mentor-dashboard" v-loading="loading">

      <section class="section" v-if="internsNeedEvaluation.length > 0">
        <h2 class="section-title">{{ t('mentorDashboard.internsNeedEvaluation') }}</h2>
        <el-row :gutter="16" >
          <el-col 
            v-for="intern in internsNeedEvaluation" 
            :key="intern.id" 
            :xs="24" 
            :sm="12" 
            :md="8" 
            :lg="6"
          >
            <el-card class="intern-card" shadow="hover">
              <template #header>
                <span class="intern-name">{{ intern.fullName }}</span>
              </template>
              <el-descriptions :column="1" size="small">
                <el-descriptions-item :label="t('mentorDashboard.position')">
                  <el-tag type="success" size="small">{{ intern.position?.title }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item :label="t('mentorDashboard.deadline')">
                  {{ t('mentorDashboard.week') }} {{ calculateWeekNumber(intern.startDate) }}
                </el-descriptions-item>
              </el-descriptions>
              <el-button 
                type="success" 
                class="submit-btn"
                @click="handleSubmitReport(intern)"
              >
                {{ t('mentorDashboard.submitReport') }}
              </el-button>
            </el-card>
          </el-col>
        </el-row>
      
      </section>

      <section class="section">
        <h2 class="section-title">{{ t('mentorDashboard.evaluationProgress') }}</h2>
        <el-card shadow="hover">
          <div class="progress-info">
            {{ t('mentorDashboard.reportsCompleted').replace('{completed}', evaluationProgress.completed).replace('{total}', evaluationProgress.total) }}
          </div>
          <el-progress 
            :percentage="progressPercentage" 
            :stroke-width="16"
            color="#2ecc71"
            :format="() => `${evaluationProgress.completed}/${evaluationProgress.total}`"
          />
        </el-card>
      </section>

      <section class="section">
        <h2 class="section-title">{{ t('mentorDashboard.internsUnderSupervision') }}</h2>
        <el-card shadow="hover">
          <el-table :data="internsUnderSupervision" stripe style="width: 100%">
            <el-table-column 
              prop="fullName" 
              :label="t('mentorDashboard.table.fullName')" 
              min-width="150"
            />
            <el-table-column 
              :label="t('mentorDashboard.table.position')" 
              min-width="180"
            >
              <template #default="scope">
                {{ scope.row.position?.title }}
              </template>
            </el-table-column>
            <el-table-column 
              :label="t('mentorDashboard.table.status')" 
              min-width="120"
            >
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.internStatus)">
                  {{ t('internManagement.status.' + scope.row.internStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column 
              :label="t('mentorDashboard.table.internshipDuration')" 
              min-width="180"
            >
              <template #default="scope">
                <el-progress 
                  :percentage="(calculateWeekNumber(scope.row.startDate) / calculateTotalWeeks(scope.row.startDate, scope.row.endDate)) * 100" 
                  :stroke-width="10"
                  :show-text="false"
                  color="#2ecc71"
                  style="width: 80px; display: inline-block; margin-right: 8px;"
                />
                <span>{{ t('mentorDashboard.week') }} {{ calculateWeekNumber(scope.row.startDate) }} / {{ calculateTotalWeeks(scope.row.startDate, scope.row.endDate) }}</span>
              </template>
            </el-table-column>
            <el-table-column 
              :label="t('mentorDashboard.table.actions')" 
              min-width="120"
              fixed="right"
            >
              <template #default="scope">
                <el-button 
                  type="primary" 
                  size="small"
                  @click="handleViewDetails(scope.row)"
                >
                  {{ t('mentorDashboard.table.viewDetails') }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </section>
    </div>
  </MentorLayout>
</template>

<style scoped>
.mentor-dashboard {
  max-width: 1200px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 24px 0;
}

.section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 16px 0;
}

.intern-card {
  margin-bottom: 16px;
}

.intern-card :deep(.el-card__header) {
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.intern-name {
  font-size: 16px;
  font-weight: 600;
}

.submit-btn {
  width: 100%;
  margin-top: 16px;
}

.progress-info {
  font-size: 14px;
  color: #374151;
  margin-bottom: 12px;
}

:deep(.el-table) {
  border-radius: 8px;
}

:deep(.el-table th) {
  background: #f9fafb !important;
  font-weight: 600;
  color: #6b7280;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>
