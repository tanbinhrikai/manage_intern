<script setup>
import { computed, ref, onMounted } from 'vue'
import { useLocaleStore } from '@/locales/locale'
import AdminLayout from '@/layouts/dashboard/AdminLayout.vue'
import StatCard from '@/components/dashboard/StatCard.vue'
import DonutChart from '@/components/dashboard/DonutChart.vue'
import ActivityList from '@/components/dashboard/ActivityList.vue'
import { getInternsAnalysis } from '@/api/intern'

const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)

const analysisData = ref({
  totalInterns: 0,
  totalMentors: 0,
  activeInterns: 0,
  warningInterns: 0,
  droppedInterns: 0,
  completedInterns: 0
})

const stats = computed(() => [
  { key: 'totalInterns', value: analysisData.value.totalInterns, icon: 'users', color: 'blue' },
  { key: 'internsActive', value: analysisData.value.activeInterns, icon: 'active', color: 'green' },
  { key: 'internsWarning', value: analysisData.value.warningInterns, icon: 'warning', color: 'yellow' },
  { key: 'totalMentors', value: analysisData.value.totalMentors, icon: 'mentor', color: 'purple' }
])

const chartData = computed(() => [
  { label: t.value('internManagement.status.ACTIVE'), value: analysisData.value.activeInterns, color: '#3b82f6' },
  { label: t.value('internManagement.status.WARNING'), value: analysisData.value.warningInterns, color: '#f59e0b' },
  { label: t.value('internManagement.status.DROPPED'), value: analysisData.value.droppedInterns, color: '#ef4444' },
  { label: t.value('internManagement.status.COMPLETE'), value: analysisData.value.completedInterns, color: '#22c55e' }
])

async function fetchAnalysis() {
  try {
    const res = await getInternsAnalysis()
    if (res.data && res.data.data) {
      analysisData.value = res.data.data
    }
  } catch (error) {
    console.error("Failed to load dashboard analysis:", error)
  }
}

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

onMounted(() => {
  fetchAnalysis()
})
</script>

<template>
  <AdminLayout>
    <div class="dashboard-page">
      <h1 class="page-title">{{ t('dashboard.title') }}</h1>

      <el-row :gutter="20" class="stats-row">
        <el-col :xs="24" :sm="12" :md="6" v-for="stat in stats" :key="stat.key">
          <StatCard
            :title="t('dashboard.' + stat.key)"
            :value="stat.value"
            :icon="stat.icon"
            :color="stat.color"
          />
        </el-col>
      </el-row>

      <el-row :gutter="24" class="content-row">
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card" shadow="never">
            <template #header>
              <span class="card-title">{{ t('dashboard.internsByStatus') }}</span>
            </template>
            <DonutChart :data="chartData" />
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="12">
          <el-card class="activity-card" shadow="never">
            <template #header>
              <span class="card-title">{{ t('dashboard.recentActivities') }}</span>
            </template>
            <ActivityList :activities="activities" />
          </el-card>
        </el-col>
      </el-row>
    </div>
  </AdminLayout>
</template>

<style scoped>
.dashboard-page {
  max-width: 1400px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 24px 0;
}

.stats-row {
  margin-bottom: 24px;
}

.stats-row .el-col {
  margin-bottom: 16px;
}

.content-row .el-col {
  margin-bottom: 24px;
}

.chart-card,
.activity-card {
  height: 100%;
  border-radius: 12px;
}

.chart-card :deep(.el-card__header),
.activity-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.chart-card :deep(.el-card__body),
.activity-card :deep(.el-card__body) {
  padding: 20px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}
</style>
