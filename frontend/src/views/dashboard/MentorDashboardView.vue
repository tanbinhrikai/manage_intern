<script setup>
import { computed } from 'vue'
import { useLocaleStore } from '@/locales/locale'
import MentorLayout from '@/layouts/dashboard/MentorLayout.vue'

const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)

const internsNeedEvaluation = [
  { name: 'Le Thi C', position: 'Backend Developer', deadline: 3 },
  { name: 'Nguyen Van D', position: 'Frontend Developer', deadline: 3 },
  { name: 'Pham Thi E', position: 'Marketing Specialist', deadline: 3 },
  { name: 'Hoang Minh F', position: 'Data Analyst', deadline: 4 }
]

const evaluationProgress = {
  completed: 8,
  total: 10
}

const progressPercentage = computed(() => {
  return (evaluationProgress.completed / evaluationProgress.total) * 100
})

const internsUnderSupervision = [
  { name: 'Le Thi C', position: 'Backend Developer', status: 'active', week: 3, totalWeeks: 12 },
  { name: 'Nguyen Van D', position: 'Frontend Developer', status: 'active', week: 3, totalWeeks: 12 },
  { name: 'Pham Thi E', position: 'Marketing Specialist', status: 'active', week: 3, totalWeeks: 8 },
  { name: 'Hoang Minh F', position: 'Data Analyst', status: 'active', week: 2, totalWeeks: 10 }
]

const getStatusType = (status) => {
  const statusMap = {
    active: 'success',
    warning: 'warning',
    completed: 'primary'
  }
  return statusMap[status] || 'info'
}

const handleSubmitReport = (intern) => {
  console.log('Submit report for:', intern.name)
}

const handleViewDetails = (intern) => {
  console.log('View details for:', intern.name)
}
</script>

<template>
  <MentorLayout>
    <div class="mentor-dashboard">
      <h1 class="page-title">{{ t('mentorDashboard.title') }}</h1>

      <section class="section">
        <h2 class="section-title">{{ t('mentorDashboard.internsNeedEvaluation') }}</h2>
        <el-row :gutter="16">
          <el-col 
            v-for="intern in internsNeedEvaluation" 
            :key="intern.name" 
            :xs="24" 
            :sm="12" 
            :md="8" 
            :lg="6"
          >
            <el-card class="intern-card" shadow="hover">
              <template #header>
                <span class="intern-name">{{ intern.name }}</span>
              </template>
              <el-descriptions :column="1" size="small">
                <el-descriptions-item :label="t('mentorDashboard.position')">
                  <el-tag type="success" size="small">{{ intern.position }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item :label="t('mentorDashboard.deadline')">
                  {{ t('mentorDashboard.week') }} {{ intern.deadline }}
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
              prop="name" 
              :label="t('mentorDashboard.table.fullName')" 
              min-width="150"
            />
            <el-table-column 
              prop="position" 
              :label="t('mentorDashboard.table.position')" 
              min-width="180"
            />
            <el-table-column 
              :label="t('mentorDashboard.table.status')" 
              min-width="120"
            >
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ t('mentorDashboard.status.' + scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column 
              :label="t('mentorDashboard.table.internshipDuration')" 
              min-width="150"
            >
              <template #default="scope">
                <el-progress 
                  :percentage="(scope.row.week / scope.row.totalWeeks) * 100" 
                  :stroke-width="10"
                  :show-text="false"
                  color="#2ecc71"
                  style="width: 80px; display: inline-block; margin-right: 8px;"
                />
                <span>{{ t('mentorDashboard.week') }} {{ scope.row.week }} / {{ scope.row.totalWeeks }}</span>
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
