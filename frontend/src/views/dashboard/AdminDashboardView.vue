<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useLocaleStore } from '@/locales/locale'
import AdminLayout from '@/layouts/dashboard/AdminLayout.vue'
import MentorLayout from '@/layouts/dashboard/MentorLayout.vue'
import StatCard from '@/components/dashboard/StatCard.vue'
import DonutChart from '@/components/dashboard/DonutChart.vue'
import ActivityList from '@/components/dashboard/ActivityList.vue'

const authStore = useAuthStore()
const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)

const isMentor = computed(() => authStore.userRole === 'MENTOR')

const stats = [
  { key: 'totalInterns', value: 150, icon: 'users', color: 'blue' },
  { key: 'internsActive', value: 120, icon: 'active', color: 'green' },
  { key: 'internsWarning', value: 10, icon: 'warning', color: 'yellow' },
  { key: 'totalMentors', value: 30, icon: 'mentor', color: 'purple' }
]

const chartData = [
  { label: 'Active', value: 120, color: '#3b82f6' },
  { label: 'Probation', value: 10, color: '#84cc16' },
  { label: 'Warning', value: 15, color: '#f59e0b' },
  { label: 'Completed', value: 5, color: '#22c55e' }
]

const activities = computed(() => [
  {
    type: 'new',
    text: '<strong>Nguyen Thi A</strong> was added',
    time: t.value('dashboard.timeAgo.minutesAgo').replace('{n}', '5')
  },
  {
    type: 'evaluation',
    text: 'Mentor <strong>Tran Van B</strong> completed weekly evaluation for <strong>Le Van C</strong>',
    time: t.value('dashboard.timeAgo.minutesAgo').replace('{n}', '30')
  },
  {
    type: 'warning',
    text: 'Status of <strong>Pham Thi D</strong> changed to <strong>Warning</strong>',
    time: t.value('dashboard.timeAgo.hourAgo')
  },
  {
    type: 'system',
    text: 'Admin updated system configuration',
    time: t.value('dashboard.timeAgo.yesterday')
  },
  {
    type: 'completed',
    text: '<strong>Hoang Van E</strong> completed internship program',
    time: t.value('dashboard.timeAgo.daysAgo').replace('{n}', '2')
  }
])

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
</script>

<template>
  <MentorLayout v-if="isMentor">
    <div class="mentor-dashboard">
      <h1 class="page-title">{{ t('mentorDashboard.title') }}</h1>

      <section class="section">
        <h2 class="section-title">{{ t('mentorDashboard.internsNeedEvaluation') }}</h2>
        <div class="intern-cards">
          <div v-for="intern in internsNeedEvaluation" :key="intern.name" class="intern-card">
            <h3 class="intern-name">{{ intern.name }}</h3>
            <p class="intern-position">
              {{ t('mentorDashboard.position') }}: <span class="position-value">{{ intern.position }}</span>
            </p>
            <p class="intern-deadline">
              {{ t('mentorDashboard.deadline') }}: {{ t('mentorDashboard.week') }} {{ intern.deadline }}
            </p>
            <button class="submit-btn">{{ t('mentorDashboard.submitReport') }}</button>
          </div>
        </div>
      </section>

      <section class="section">
        <h2 class="section-title">{{ t('mentorDashboard.evaluationProgress') }}</h2>
        <div class="progress-card">
          <div class="progress-info">
            {{ t('mentorDashboard.reportsCompleted').replace('{completed}', evaluationProgress.completed).replace('{total}', evaluationProgress.total) }}
          </div>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: progressPercentage + '%' }"></div>
          </div>
        </div>
      </section>

      <section class="section">
        <h2 class="section-title">{{ t('mentorDashboard.internsUnderSupervision') }}</h2>
        <div class="table-card">
          <table class="data-table">
            <thead>
              <tr>
                <th>{{ t('mentorDashboard.table.fullName') }}</th>
                <th>{{ t('mentorDashboard.table.position') }}</th>
                <th>{{ t('mentorDashboard.table.status') }}</th>
                <th>{{ t('mentorDashboard.table.internshipDuration') }}</th>
                <th>{{ t('mentorDashboard.table.actions') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="intern in internsUnderSupervision" :key="intern.name">
                <td>{{ intern.name }}</td>
                <td>{{ intern.position }}</td>
                <td>
                  <span :class="['status-badge', `status-${intern.status}`]">
                    {{ t('mentorDashboard.status.' + intern.status) }}
                  </span>
                </td>
                <td>{{ t('mentorDashboard.week') }} {{ intern.week }} / {{ intern.totalWeeks }}</td>
                <td>
                  <button class="action-btn">{{ t('mentorDashboard.table.viewDetails') }}</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </MentorLayout>

  <AdminLayout v-else>
    <div class="dashboard-page">
      <h1 class="page-title">{{ t('dashboard.title') }}</h1>

      <div class="stats-grid">
        <StatCard
          v-for="stat in stats"
          :key="stat.key"
          :title="t('dashboard.' + stat.key)"
          :value="stat.value"
          :icon="stat.icon"
          :color="stat.color"
        />
      </div>

      <div class="content-grid">
        <div class="chart-card">
          <h2 class="card-title">{{ t('dashboard.internsByStatus') }}</h2>
          <DonutChart :data="chartData" />
        </div>

        <div class="activity-card">
          <h2 class="card-title">{{ t('dashboard.recentActivities') }}</h2>
          <ActivityList :activities="activities" />
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<style scoped>
.dashboard-page,
.mentor-dashboard {
  max-width: 1200px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 24px 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

@media (max-width: 1024px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}

.chart-card,
.activity-card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 24px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 20px 0;
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

.intern-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

@media (max-width: 1200px) {
  .intern-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .intern-cards {
    grid-template-columns: 1fr;
  }
}

.intern-card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
}

.intern-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.intern-position {
  font-size: 13px;
  color: #6b7280;
  margin: 0 0 4px 0;
}

.position-value {
  color: #2ecc71;
  font-weight: 500;
}

.intern-deadline {
  font-size: 13px;
  color: #6b7280;
  margin: 0 0 16px 0;
}

.submit-btn {
  width: 100%;
  padding: 10px 16px;
  font-size: 14px;
  font-weight: 500;
  color: #ffffff;
  background: #2ecc71;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}

.submit-btn:hover {
  background: #27ae60;
}

.progress-card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
}

.progress-info {
  font-size: 14px;
  color: #374151;
  margin-bottom: 12px;
}

.progress-bar {
  height: 12px;
  background: #e5e7eb;
  border-radius: 6px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #2ecc71;
  border-radius: 6px;
  transition: width 0.3s;
}

.table-card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 14px 16px;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.data-table th {
  background: #f9fafb;
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
}

.data-table td {
  font-size: 14px;
  color: #374151;
}

.data-table tbody tr:last-child td {
  border-bottom: none;
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  border-radius: 4px;
}

.status-active {
  background: #2ecc71;
  color: #ffffff;
}

.status-warning {
  background: #f59e0b;
  color: #ffffff;
}

.status-completed {
  background: #3b82f6;
  color: #ffffff;
}

.action-btn {
  padding: 8px 16px;
  font-size: 13px;
  font-weight: 500;
  color: #ffffff;
  background: #2c3e50;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;
}

.action-btn:hover {
  background: #34495e;
}
</style>
