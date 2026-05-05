<script setup>
import { getInternById } from "@/api/intern";
import { useDateFormat, useStatus } from "@/composables";
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue";
import MentorLayout from "@/layouts/dashboard/MentorLayout.vue";
import { useLocaleStore } from "@/locales/locale";
import { useAuthStore } from "@/stores/auth";
import {
  DataBoard,
  Discount,
  Document,
  Edit,
  Medal,
  TrendCharts,
} from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();
const localeStore = useLocaleStore();
const authStore = useAuthStore();
const t = computed(() => localeStore.t);

const internId = route.params.id;
const intern = ref({});
const loading = ref(false);

const layoutComponent = computed(() => {
  return authStore.userRole === "MENTOR" ? MentorLayout : AdminLayout;
});

const { getStatusType } = useStatus();
const { formatDate } = useDateFormat();

async function fetchInternDetail() {
  loading.value = true;
  try {
    const res = await getInternById(internId);
    intern.value = res.data?.data || {};
  } catch (error) {
    console.error("Failed to load intern detail:", error);
    ElMessage.error(t.value("internManagement.messages.loadError"));
  } finally {
    loading.value = false;
  }
}

function openEditView() {
  const prefix =
    authStore.userRole === "MENTOR" ? "/mentor/my-interns" : "/admin/interns";
  router.push(`${prefix}/${internId}/edit`);
}

function openReportsTab() {
  const prefix =
    authStore.userRole === "MENTOR" ? "/mentor/my-interns" : "/admin/interns";
  router.push(`${prefix}/${internId}/edit?tab=reports`);
}

const calculateDuration = (start, end) => {
  if (!start || !end) return "-";
  const startDate = new Date(start);
  const endDate = new Date(end);
  const diffTime = Math.abs(endDate - startDate);
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  const weeks = Math.floor(diffDays / 7);

  return `${formatDate(start)} - ${formatDate(end)} (${weeks} ${t.value(
    "common.weeks"
  )})`;
};

onMounted(() => {
  fetchInternDetail();
});
</script>

<template>
  <component :is="layoutComponent" :page-title="t('internDetail.profileTitle') + ': ' + intern.fullName">
    <div class="intern-detail-view" v-loading="loading">
      <div class="page-header">
        <el-button type="primary" :icon="Edit" @click="openEditView">
          {{ t("internDetail.editInfo") }}
        </el-button>
      </div>

      <el-row :gutter="24">
        <el-col :span="16">
          <el-card shadow="hover" class="detail-card mb-24">
            <template #header>
              <div class="card-header">
                <h3>{{ t("internDetail.sections.basicInfo") }}</h3>
              </div>
            </template>
            <div class="info-list">
              <div class="info-item">
                <span class="label"
                  >{{ t("internDetail.fields.fullName") }}:</span
                >
                <span class="value font-medium">{{ intern.fullName }}</span>
              </div>
              <div class="info-item">
                <span class="label"
                  >{{ t("internDetail.fields.position") }}:</span
                >
                <span class="value">{{ intern.position?.title }}</span>
              </div>
              <div class="info-item">
                <span class="label"
                  >{{ t("internDetail.fields.mentor") }}:</span
                >
                <span class="value">{{ intern.mentor?.fullName }}</span>
              </div>
            </div>
          </el-card>

          <el-card shadow="hover" class="detail-card">
            <template #header>
              <div class="card-header">
                <h3>{{ t("internDetail.sections.internshipInfo") }}</h3>
              </div>
            </template>
            <div class="info-list">
              <div class="info-item">
                <span class="label"
                  >{{ t("internDetail.fields.duration") }}:</span
                >
                <span class="value">{{
                  calculateDuration(intern.startDate, intern.endDate)
                }}</span>
              </div>
              <div class="info-item">
                <span class="label"
                  >{{ t("internDetail.fields.status") }}:</span
                >
                <div class="value">
                  <el-tag
                    :type="getStatusType(intern.internStatus)"
                    effect="light"
                    class="status-tag"
                  >
                    {{ t("internManagement.status." + intern.internStatus) }}
                  </el-tag>
                </div>
              </div>
              <div class="info-item">
                <span class="label"
                  >{{ t("internDetail.fields.startDate") }}:</span
                >
                <span class="value">{{ formatDate(intern.startDate) }}</span>
              </div>
              <div class="info-item">
                <span class="label"
                  >{{ t("internDetail.fields.endDate") }}:</span
                >
                <span class="value">{{ formatDate(intern.endDate) }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="detail-card mb-24">
            <template #header>
              <div class="card-header">
                <h3>{{ t("internDetail.sections.reportsAndEval") }}</h3>
              </div>
            </template>
            <div class="action-list">
              <el-button text class="action-btn" @click="openReportsTab">
                <el-icon class="mr-2"><Document /></el-icon>
                {{ t("internDetail.actions.viewReports") }}
              </el-button>
              <el-button text class="action-btn">
                <el-icon class="mr-2"><TrendCharts /></el-icon>
                {{ t("internDetail.actions.viewEval") }}
              </el-button>
              <el-button text class="action-btn">
                <el-icon class="mr-2"><DataBoard /></el-icon>
                {{ t("internDetail.actions.viewBMM") }}
              </el-button>
            </div>
          </el-card>

          <el-card shadow="hover" class="detail-card">
            <template #header>
              <div class="card-header">
                <h3>{{ t("internDetail.sections.quickActions") }}</h3>
              </div>
            </template>
            <div class="quick-actions">
              <p class="description">{{ t("internDetail.description") }}</p>
              <el-button
                type="danger"
                class="full-width mb-12"
                :icon="Discount"
              >
                {{ t("internDetail.actions.recommendDrop") }}
              </el-button>
              <el-button type="success" class="full-width" :icon="Medal">
                {{ t("internDetail.actions.recommendOffer") }}
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </component>
</template>

<style scoped>
.intern-detail-view {
  min-height: calc(100vh - 60px);
  padding: 0 10px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: transparent;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.detail-card {
  border-radius: 8px;
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.mb-24 {
  margin-bottom: 24px;
}

.mb-12 {
  margin-bottom: 12px;
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

.status-tag {
  border-radius: 4px;
  padding: 0 12px;
  height: 24px;
  line-height: 22px;
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-btn {
  justify-content: flex-start;
  font-size: 14px;
  color: #606266;
  padding: 8px 0;
  height: auto;
}

.action-btn:hover {
  color: #409eff;
  background-color: transparent;
}

.mr-2 {
  margin-right: 8px;
}

.quick-actions {
  display: flex;
  flex-direction: column;
}

.description {
  font-size: 13px;
  color: #909399;
  margin-bottom: 16px;
  margin-top: 0;
}

.full-width {
  width: 100%;
  justify-content: center;
}

.quick-actions :deep(.el-button),
.action-list :deep(.el-button) {
  margin-left: 0;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>
