<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useLocaleStore } from '@/locales/locale'
import MentorLayout from '@/layouts/dashboard/MentorLayout.vue'
import StatCard from '@/components/dashboard/StatCard.vue'
import DonutChart from '@/components/dashboard/DonutChart.vue'
import { getInternsNotEvaluatedThisWeek, getMyIntern } from '@/api/intern'
import { getMentorStatistics, getMentorInternStatusDistribution } from '@/api/dashboard'
import { useLoading, useApi } from '@/composables'

const router = useRouter()
const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)

const { loading, withLoading } = useLoading()
const { execute: executeApi } = useApi({
  showErrorMessage: false
})

// Data refs
const statistics = ref({
  totalInterns: 0,
  activeInterns: 0,
  warningInterns: 0,
  pendingReports: 0
})
const internsNeedEvaluation = ref([])
const totalNeedEvaluation = ref(0)
const internsUnderSupervision = ref([])
const internStatusDistribution = ref([])

// Pagination for interns need evaluation
const evalCurrentPage = ref(1)
const evalPageSize = ref(5)

// Stats cards configuration
const stats = computed(() => [
  {
    key: 'totalInterns',
    value: statistics.value.totalInterns,
    icon: 'users',
    color: 'blue'
  },
  {
    key: 'activeInterns',
    value: statistics.value.activeInterns,
    icon: 'active',
    color: 'green'
  },
  {
    key: 'warningInterns',
    value: statistics.value.warningInterns,
    icon: 'warning',
    color: 'yellow'
  },
  {
    key: 'pendingReports',
    value: statistics.value.pendingReports,
    icon: 'mentor',
    color: 'purple'
  }
])

// Chart data for intern status distribution
const chartData = computed(() => {
  if (internStatusDistribution.value.length > 0) {
    const colors = {
      'ACTIVE': '#3b82f6',
      'WARNING': '#f59e0b',
      'DROPPED': '#ef4444',
      'COMPLETED': '#22c55e'
    }
    return internStatusDistribution.value.map(item => ({
      label: t.value(`internManagement.status.${item.label}`) || item.label,
      value: item.value,
      color: colors[item.label] || '#6b7280'
    }))
  }
  // Fallback to statistics data
  return [
    {
      label: t.value('internManagement.status.ACTIVE'),
      value: statistics.value.activeInterns,
      color: '#3b82f6'
    },
    {
      label: t.value('internManagement.status.WARNING'),
      value: statistics.value.warningInterns,
      color: '#f59e0b'
    }
  ]
})

const evaluationProgress = computed(() => {
  const total = internsUnderSupervision.value.length
  const needEval = totalNeedEvaluation.value
  const completed = total - needEval
  return { completed: completed >= 0 ? completed : 0, total }
})

const progressPercentage = computed(() => {
  if (evaluationProgress.value.total === 0) return 0
  return (evaluationProgress.value.completed / evaluationProgress.value.total) * 100
})

const calculateWeekNumber = (startDate) => {
  if (!startDate) return 1
  const start = new Date(startDate)
  const now = new Date()
  const diffTime = Math.abs(now - start)
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return Math.max(1, Math.ceil(diffDays / 7))
}

const handleSubmitReport = (intern) => {
  router.push(`/mentor/my-interns/${intern.id}/edit?tab=reports`)
}

async function fetchInternsNeedEvaluation() {
  const res = await executeApi(() => getInternsNotEvaluatedThisWeek({ 
    page: evalCurrentPage.value - 1, 
    limit: evalPageSize.value 
  }))
  internsNeedEvaluation.value = res.data?.data?.items || []
  totalNeedEvaluation.value = res.data?.data?.totalItems || 0
}

function handleEvalPageChange(page) {
  evalCurrentPage.value = page
  fetchInternsNeedEvaluation()
}

async function fetchData() {
  await withLoading(async () => {
    const [statisticsRes, statusDistRes, notEvalRes, myInternsRes] = await Promise.all([
      executeApi(() => getMentorStatistics()),
      executeApi(() => getMentorInternStatusDistribution()),
      executeApi(() => getInternsNotEvaluatedThisWeek({ page: 0, limit: evalPageSize.value })),
      executeApi(() => getMyIntern({ limit: 100 }))
    ])
    
    if (statisticsRes.data?.data) {
      statistics.value = statisticsRes.data.data
    }
    if (statusDistRes.data?.data?.items) {
      internStatusDistribution.value = statusDistRes.data.data.items
    }
    internsNeedEvaluation.value = notEvalRes.data?.data?.items || []
    totalNeedEvaluation.value = notEvalRes.data?.data?.totalItems || 0
    internsUnderSupervision.value = myInternsRes.data?.data?.items || []
  })
}

onMounted(fetchData)
</script>

<template>
  <MentorLayout>
    <div class="mentor-dashboard" v-loading="loading">

      <!-- Statistics Cards -->
      <el-row :gutter="20" class="mb-4">
        <el-col
          :xs="12"
          :sm="6"
          :md="6"
          v-for="stat in stats"
          :key="stat.key"
          class="mb-col"
        >
          <StatCard
            :title="t('mentorDashboard.statistics.' + stat.key)"
            :value="stat.value"
            :icon="stat.icon"
            :color="stat.color"
            class="stat-card-hover"
          />
        </el-col>
      </el-row>

      <!-- Charts Row -->
      <el-row :gutter="20" class="mb-4 equal-height-row">
        <el-col :xs="24" :md="12" class="mb-col">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <span class="card-title">{{ t('mentorDashboard.internsByStatus') }}</span>
            </template>
            <div class="chart-container">
              <DonutChart :data="chartData" :height="250" />
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :md="12" class="mb-col">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <span class="card-title">{{ t('mentorDashboard.evaluationProgress') }}</span>
            </template>
            <div class="progress-container">
              <div class="progress-info">
                {{ t('mentorDashboard.reportsCompleted').replace('{completed}', evaluationProgress.completed).replace('{total}', evaluationProgress.total) }}
              </div>
              <el-progress 
                :percentage="progressPercentage" 
                :stroke-width="20"
                color="#22c55e"
                :format="() => `${evaluationProgress.completed}/${evaluationProgress.total}`"
              />
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- Interns Need Evaluation -->
      <section class="section" v-if="totalNeedEvaluation > 0">
        <el-card shadow="hover" class="eval-card">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">
                {{ t('mentorDashboard.internsNeedEvaluation') }}
                <el-badge :value="totalNeedEvaluation" type="danger" class="count-badge" />
              </span>
            </div>
          </template>
          <el-table :data="internsNeedEvaluation" stripe size="small" style="width: 100%">
            <el-table-column 
              prop="fullName" 
              :label="t('mentorDashboard.table.fullName')" 
              min-width="150"
            />
            <el-table-column 
              :label="t('mentorDashboard.position')" 
              min-width="120"
            >
              <template #default="scope">
                <el-tag type="info" size="small">{{ scope.row.position?.title }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column 
              :label="t('mentorDashboard.week')" 
              min-width="80"
              align="center"
            >
              <template #default="scope">
                <span class="week-badge">{{ calculateWeekNumber(scope.row.startDate) }}</span>
              </template>
            </el-table-column>
            <el-table-column 
              :label="t('common.actions')" 
              min-width="120"
              align="center"
              fixed="right"
            >
              <template #default="scope">
                <el-button 
                  type="success" 
                  size="small"
                  @click="handleSubmitReport(scope.row)"
                >
                  {{ t('mentorDashboard.submitReport') }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrapper" v-if="totalNeedEvaluation > evalPageSize">
            <el-pagination
              :current-page="evalCurrentPage"
              :page-size="evalPageSize"
              :total="totalNeedEvaluation"
              layout="prev, pager, next"
              background
              small
              @current-change="handleEvalPageChange"
            />
          </div>
        </el-card>
      </section>
    </div>
  </MentorLayout>
</template>

<style scoped>
.mentor-dashboard {
  max-width: 100%;
  margin: 0 auto;
  padding: 0 10px 20px 10px;
  font-family: 'Inter', sans-serif;
}

.mb-4 {
  margin-bottom: 24px;
}

.mb-col {
  margin-bottom: 16px;
}

.stat-card-hover {
  width: 100%;
  height: 100%;
  transition: transform 0.2s, box-shadow 0.2s;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.stat-card-hover:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.equal-height-row {
  display: flex;
  flex-wrap: wrap;
}

.equal-height-row .el-col {
  display: flex;
}

.chart-card,
.eval-card {
  width: 100%;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  transition: all 0.3s;
}

.chart-card:hover,
.eval-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.06);
  border-color: #bfdbfe;
}

.chart-card :deep(.el-card__header),
.eval-card :deep(.el-card__header) {
  padding: 12px 16px;
  border-bottom: 1px solid #f9fafb;
  background: #fff;
}

.chart-card :deep(.el-card__body),
.eval-card :deep(.el-card__body) {
  padding: 16px;
  flex: 1;
}

.card-header-flex {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #374151;
  display: flex;
  align-items: center;
  gap: 8px;
}

.count-badge :deep(.el-badge__content) {
  font-size: 11px;
}

.chart-container {
  min-height: 250px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.progress-container {
  min-height: 250px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 20px;
}

.progress-info {
  font-size: 16px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 20px;
  text-align: center;
}

.section {
  margin-bottom: 24px;
}

.week-badge {
  display: inline-block;
  background: #f3f4f6;
  color: #374151;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 13px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
}

:deep(.el-table) {
  border-radius: 8px;
}

:deep(.el-table th) {
  background: #f9fafb !important;
  font-weight: 600;
  color: #6b7280;
}

@media (max-width: 992px) {
  .mb-col {
    margin-bottom: 20px;
  }
  .equal-height-row {
    display: block;
  }
}
</style>

