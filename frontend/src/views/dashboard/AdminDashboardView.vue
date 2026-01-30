<script setup>
import { computed, ref, onMounted } from "vue";
import { useLocaleStore } from "@/locales/locale";
import { Bell, Timer } from "@element-plus/icons-vue";
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue";
import StatCard from "@/components/dashboard/StatCard.vue";
import DonutChart from "@/components/dashboard/DonutChart.vue";
import BarChart from "@/components/dashboard/BarChart.vue";
import ActivityList from "@/components/dashboard/ActivityList.vue";
import BatchScoreChart from "@/components/dashboard/BatchScoreChart.vue";
import { getInternsAnalysis } from "@/api/intern";
import {
  getRecentActivities,
  getInternsByDepartment,
  getInternsByPosition,
} from "@/api/dashboard";
import { useLoading, useApi } from "@/composables";

const localeStore = useLocaleStore();
const t = computed(() => localeStore.t);

const { loading, withLoading } = useLoading();
const { execute: executeApi } = useApi({
  showErrorMessage: false,
});

const analysisData = ref({
  totalInterns: 0,
  totalMentors: 0,
  activeInterns: 0,
  warningInterns: 0,
  droppedInterns: 0,
  completedInterns: 0,
});

const stats = computed(() => [
  {
    key: "totalInterns",
    value: analysisData.value.totalInterns,
    icon: "users",
    color: "blue",
  },
  {
    key: "internsActive",
    value: analysisData.value.activeInterns,
    icon: "active",
    color: "green",
  },
  {
    key: "internsWarning",
    value: analysisData.value.warningInterns,
    icon: "warning",
    color: "yellow",
  },
  {
    key: "internsCompleted",
    value: analysisData.value.completedInterns,
    icon: "completed",
    color: "teal",
  },
  {
    key: "internsDropped",
    value: analysisData.value.droppedInterns,
    icon: "dropped",
    color: "red",
  },
  {
    key: "totalMentors",
    value: analysisData.value.totalMentors,
    icon: "mentor",
    color: "purple",
  },
]);

const totalInterns = computed(() => analysisData.value.totalInterns || 0);

const chartData = computed(() => [
  {
    label: t.value("internManagement.status.ACTIVE"),
    value: analysisData.value.activeInterns,
    color: "#3b82f6",
  },
  {
    label: t.value("internManagement.status.WARNING"),
    value: analysisData.value.warningInterns,
    color: "#f59e0b",
  },
  {
    label: t.value("internManagement.status.DROPPED"),
    value: analysisData.value.droppedInterns,
    color: "#ef4444",
  },
  {
    label: t.value("internManagement.status.COMPLETE"),
    value: analysisData.value.completedInterns,
    color: "#22c55e",
  },
]);

async function fetchAnalysis() {
  await withLoading(async () => {
    const res = await executeApi(() => getInternsAnalysis());
    if (res.data?.data) {
      analysisData.value = res.data.data;
    }
  });
}

const activities = ref([]);
const internsByDepartment = ref([]);
const internsByPosition = ref([]);

function formatTimeAgo(timestamp) {
  if (!timestamp) return "";
  const time = new Date(timestamp);
  if (isNaN(time.getTime())) return "";

  const now = new Date();
  let diffMs = now.getTime() - time.getTime();
  diffMs = Math.abs(diffMs);

  const diffSecs = Math.floor(diffMs / 1000);
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMs / 3600000);
  const diffDays = Math.floor(diffMs / 86400000);

  if (diffSecs < 60) return t.value("dashboard.timeAgo.justNow") || "Just now";
  if (diffMins < 60)
    return t
      .value("dashboard.timeAgo.minutesAgo")
      .replace("{n}", diffMins.toString());
  if (diffHours < 24) {
    return diffHours === 1
      ? t.value("dashboard.timeAgo.hourAgo")
      : t
          .value("dashboard.timeAgo.hoursAgo")
          ?.replace("{n}", diffHours.toString()) || `${diffHours} hours ago`;
  }
  if (diffDays === 1) return t.value("dashboard.timeAgo.yesterday");
  if (diffDays < 7)
    return t
      .value("dashboard.timeAgo.daysAgo")
      .replace("{n}", diffDays.toString());
  return time.toLocaleDateString();
}

const formattedActivities = computed(() => {
  return activities.value.map((activity) => ({
    ...activity,
    time: formatTimeAgo(activity.timestamp),
  }));
});

async function fetchActivities() {
  await withLoading(async () => {
    const res = await executeApi(() => getRecentActivities({ limit: 100 }));
    if (res.data?.data) {
      activities.value = res.data.data;
    }
  });
}

async function fetchChartData() {
  await withLoading(async () => {
    const [deptRes, posRes] = await Promise.all([
      executeApi(() => getInternsByDepartment()),
      executeApi(() => getInternsByPosition()),
    ]);
    if (deptRes.data?.data?.items)
      internsByDepartment.value = deptRes.data.data.items;
    if (posRes.data?.data?.items)
      internsByPosition.value = posRes.data.data.items;
  });
}

onMounted(() => {
  fetchAnalysis();
  fetchActivities();
  fetchChartData();
});
</script>

<template>
  <AdminLayout>
    <div class="dashboard-page" v-loading="loading">
      <div class="header-section">
        <h1 class="page-title">{{ t("dashboard.title") }}</h1>
      </div>

      <el-row :gutter="20" class="mb-4">
        <el-col
          :xs="12"
          :sm="8"
          :md="4"
          v-for="stat in stats"
          :key="stat.key"
          class="mb-col"
        >
          <StatCard
            :title="t('dashboard.' + stat.key)"
            :value="stat.value"
            :icon="stat.icon"
            :color="stat.color"
            class="stat-card-hover"
          />
        </el-col>
      </el-row>

      <el-row :gutter="20" class="mb-4">
        <el-col :span="24">
          <el-card class="activity-card" shadow="hover">
            <template #header>
              <div class="activity-header">
                <div class="header-left">
                  <el-icon class="icon-bell"><Bell /></el-icon>
                  <span class="card-title">{{
                    t("dashboard.recentActivities")
                  }}</span>
                </div>
                <el-tag size="small" effect="light" round
                  >{{ activities.length }} new</el-tag
                >
              </div>
            </template>

            <div class="activity-scroll-area custom-scroll">
              <ActivityList :activities="formattedActivities" />
              <div v-if="activities.length === 0" class="empty-activity">
                <el-icon :size="40" color="#e5e7eb"><Timer /></el-icon>
                <p>No recent activities</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="mb-4 equal-height-row">
        <el-col :xs="24" :md="12" :lg="12" class="mb-col">
          <el-card class="chart-card" shadow="hover">
            <template #header
              ><span class="card-title">{{
                t("dashboard.internsByStatus")
              }}</span></template
            >
            <div class="chart-container">
              <DonutChart :data="chartData" :height="250" />
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :md="12" :lg="12" class="mb-col">
          <el-card class="chart-card" shadow="hover">
            <template #header
              ><span class="card-title">{{
                t("dashboard.internsByPosition")
              }}</span></template
            >
            <div class="chart-container">
              <BarChart
                v-if="internsByPosition.length"
                :data="internsByPosition"
                :height="250"
              />
              <div v-else class="empty-chart">{{ t("dashboard.noData") }}</div>
            </div>
          </el-card>
        </el-col>
        <!-- <el-col :xs="24" :md="12" :lg="8" class="mb-col">
          <el-card class="chart-card" shadow="hover">
            <template #header
              ><span class="card-title">{{
                t("dashboard.internsByDepartment")
              }}</span></template
            >
            <div class="chart-container">
              <BarChart
                v-if="internsByDepartment.length"
                :data="internsByDepartment"
                :height="250"
              />
              <div v-else class="empty-chart">{{ t("dashboard.noData") }}</div>
            </div>
          </el-card>
        </el-col> -->
      </el-row>

      <!-- Batch Score Trend - Full Row with drill-down -->
      <el-row :gutter="20" class="mb-4">
        <el-col :xs="24" class="mb-col">
          <el-card class="chart-card batch-score-card" shadow="hover">
            <BatchScoreChart />
          </el-card>
        </el-col>
      </el-row>
    </div>
  </AdminLayout>
</template>

<style scoped>
.dashboard-page {
  max-width: 100%;
  margin: 0 auto;
  padding: 0 10px 20px 10px;
  font-family: "Inter", sans-serif;
}

.header-section {
  margin-bottom: 20px;
}
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

.stats-row {
  margin-bottom: 20px;
  display: flex;
  flex-wrap: wrap;
}

.stats-row .el-col {
  display: flex;
  margin-bottom: 12px;
}

.stat-card-hover {
  width: 100%;
  height: 100%;
  transition: transform 0.2s, box-shadow 0.2s;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.stat-card-hover:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.activity-card {
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: #fff;
  height: auto;
}

.activity-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f3f4f6;
}

.activity-card :deep(.el-card__body) {
  padding: 0;
}

.activity-scroll-area {
  max-height: 200px;
  overflow-y: auto;
  padding: 10px 20px;
}

.activity-scroll-area :deep(.activity-item) {
  padding: 12px 0;
  border-bottom: 1px solid #f9fafb;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.icon-bell {
  color: #f56c6c;
  font-size: 18px;
}

.custom-scroll::-webkit-scrollbar {
  width: 6px;
}
.custom-scroll::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scroll::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 10px;
}
.custom-scroll::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}

.mb-4 {
  margin-bottom: 24px;
}
.mb-col {
  margin-bottom: 0;
}

.equal-height-row {
  display: flex;
  flex-wrap: wrap;
}
.equal-height-row .el-col {
  display: flex;
}

.chart-card {
  width: 100%;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  transition: all 0.3s;
}
.chart-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.06);
  border-color: #bfdbfe;
}
.chart-card :deep(.el-card__header) {
  padding: 12px 16px;
  border-bottom: 1px solid #f9fafb;
  background: #fff;
}
.chart-card :deep(.el-card__body) {
  padding: 16px;
  flex: 1;
}

.batch-score-card :deep(.el-card__body) {
  padding: 20px;
  min-height: 450px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #374151;
}
.card-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.chart-container {
  min-height: 250px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-link {
  font-size: 12px;
  color: #3b82f6;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  background: #eff6ff;
  padding: 2px 8px;
  border-radius: 4px;
}

.empty-chart {
  font-size: 13px;
  color: #9ca3af;
  font-style: italic;
}
.empty-activity {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 150px;
  color: #9ca3af;
  gap: 8px;
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