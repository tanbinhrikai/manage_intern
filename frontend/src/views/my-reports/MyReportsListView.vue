<script setup>
import { ref, computed, onMounted } from "vue";
import {
  Search,
  View,
  Delete,
  ArrowDown,
  ArrowUp,
  Refresh
} from "@element-plus/icons-vue";
import { useLocaleStore } from "@/locales/locale";
import MentorLayout from "@/layouts/dashboard/MentorLayout.vue";
import { useRouter } from "vue-router";
import { getMyReports, deleteWeeklyReport } from "@/api/weekly-report";
import { getMyIntern } from "@/api/intern";
import {
  usePagination,
  useLoading,
  useApi,
  useDateFormat,
  useConfirm,
  useDialog,
} from "@/composables";
import { ElMessage as message } from "element-plus";

const router = useRouter();
const localeStore = useLocaleStore();
const t = computed(() => localeStore.t);
const pageSizes = [20, 40, 60, 80, 100];
// Composables
const pagination = usePagination({
  initialPage: 1,
  initialPageSize: 20,
});

const { loading, withLoading } = useLoading();
const { execute: executeApi } = useApi({
  showErrorMessage: true,
  showSuccessMessage: false,
});
const { formatDate } = useDateFormat();
const { confirmDelete } = useConfirm();

// Data
const reports = ref([]);
const interns = ref([]);

// Filters
const showFilters = ref(false);
const selectedIntern = ref(null);
const startDate = ref(null);
const endDate = ref(null);

/**
 * Fetch interns for filter dropdown
 */
async function fetchInterns() {
  const res = await executeApi(() => getMyIntern({ limit: 100 }));
  interns.value = res.data?.data?.items || [];
}

/**
 * Fetch mentor's reports
 */
async function fetchReports() {
  await withLoading(async () => {
    const params = {
      ...pagination.apiParams.value,
      intern_id: selectedIntern.value || undefined,
      start_date: startDate.value || undefined,
      end_date: endDate.value || undefined,
    };

    const res = await executeApi(
      () => getMyReports(params),
      null,
      "weeklyReport.messages.loadError"
    );

    reports.value = res.data?.data?.items || [];
    pagination.setTotalItems(res.data?.data?.totalItems || 0);
  });
}

/**
 * Handle filter change
 */
function handleSearch() {
  pagination.firstPage();
  fetchReports();
}

/**
 * Clear all filters
 */
function clearFilters() {
  selectedIntern.value = null;
  startDate.value = null;
  endDate.value = null;
  handleSearch();
}

/**
 * Handle page size change
 * @param {number} size - New page size
 */
function handleSizeChange(size) {
  pagination.setPageSize(size)
  pagination.firstPage()
  fetchReports()
}


/**
 * Navigate to report detail
 */
function viewReport(report) {
  router.push(
    `/mentor/my-interns/${report.internId}/edit?tab=reports&reportId=${report.id}`
  );
}

/**
 * Delete a report
 */
async function handleDelete(report) {
  await confirmDelete({
    message: t.value("common.confirm.delete"),
    title: t.value("common.confirm.title"),
    onConfirm: async () => {
      const res = await executeApi(() => deleteWeeklyReport(report.id));
      if (res.data?.success) {
        message.success(t.value("weeklyReport.messages.deleteSuccess"));
        fetchReports();
      }
    }
  });
}

/**
 * Navigate to create new report
 */
function createReport() {
  if (interns.value.length > 0) {
    router.push(`/mentor/my-interns/${interns.value[0].id}/edit?tab=reports`);
  }
}

/**
 * Handle pagination page change
 */
function handlePageChange(page) {
  pagination.setPage(page);
}

// Fetch data on mount
onMounted(() => {
  fetchInterns();
  fetchReports();
});
</script>

<template>
  <MentorLayout :page-title="t('myReports.title')">
    <div class="my-reports-view">
      <el-card class="main-card" shadow="never">
        <div class="toolbar">
          <div class="filter-group">
            <el-select
              v-model="selectedIntern"
              :placeholder="t('myReports.filterByIntern')"
              clearable
              filterable
              class="filter-select"
              @change="handleSearch"
            >
              <el-option
                v-for="intern in interns"
                :key="intern.id"
                :label="intern.fullName"
                :value="intern.id"
              />
            </el-select>

            <div class="filter-buttons">
              <el-button
                type="info"
                plain
                :icon="showFilters ? ArrowUp : ArrowDown"
                @click="showFilters = !showFilters"
              >
                {{
                  showFilters
                    ? t("internManagement.hideFilters")
                    : t("internManagement.moreFilters")
                }}
              </el-button>

              <el-button
                type="info"
                plain
                :icon="Refresh"
                @click="clearFilters"
              >
                {{ t("internManagement.clearFilters") }}
              </el-button>
            </div>
          </div>
        </div>

        <!-- Extended Filters -->
        <el-collapse-transition>
          <div v-show="showFilters" class="extended-filters">
            <el-row :gutter="16">
              <el-col :xs="24" :sm="12" :md="6">
                <div class="filter-item">
                  <el-date-picker
                    v-model="startDate"
                    type="date"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    :placeholder="t('myReports.startDate')"
                    style="width: 100%"
                    @change="handleSearch"
                  />
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6">
                <div class="filter-item">
                  <el-date-picker
                    v-model="endDate"
                    type="date"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    :placeholder="t('myReports.endDate')"
                    style="width: 100%"
                    @change="handleSearch"
                  />
                </div>
              </el-col>
            </el-row>
          </div>
        </el-collapse-transition>

        <el-table
          :data="reports"
          stripe
          style="width: 100%"
          height="calc(100vh - 240px)"
          v-loading="loading"
        >
          <el-table-column
            prop="internName"
            :label="t('myReports.table.internName')"
            min-width="150"
          />
          <el-table-column
            :label="t('myReports.table.weekStartDate')"
            min-width="120"
          >
            <template #default="scope">
              {{ formatDate(scope.row.weekStartDate) }}
            </template>
          </el-table-column>
          <el-table-column
            prop="weekNumber"
            :label="t('myReports.table.weekNumber')"
            min-width="80"
            align="center"
          >
            <template #default="scope">
              <el-tag type="info" size="small">
                {{ t("mentorDashboard.week") }} {{ scope.row.weekNumber }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            :label="t('myReports.table.averageScore')"
            min-width="100"
            align="center"
          >
            <template #default="scope">
              <span :class="getScoreClass(scope.row.averageScore)">
                {{ scope.row.averageScore?.toFixed(1) || "-" }}
              </span>
            </template>
          </el-table-column>
          <el-table-column
            :label="t('myReports.table.createdAt')"
            min-width="120"
          >
            <template #default="scope">
              {{ formatDate(scope.row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column
            :label="t('common.actions')"
            min-width="120"
            fixed="right"
            align="center"
          >
            <template #default="scope">
              <el-button
                type="primary"
                :icon="View"
                size="small"
                circle
                @click="viewReport(scope.row)"
              />
              <el-button
                type="danger"
                :icon="Delete"
                size="small"
                circle
                @click="handleDelete(scope.row)"
              />
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.currentPage.value"
            v-model:page-size="pagination.pageSize.value"
            :page-sizes="pageSizes"
            layout="total, sizes"
            :total="pagination.totalItems.value"
            @size-change="handleSizeChange"
          />

          <el-pagination
            v-model:current-page="pagination.currentPage.value"
            :page-size="pagination.pageSize.value"
            :total="pagination.totalItems.value"
            layout="prev, pager, next"
            @current-change="handlePageChange"
          />
        </div>
      </el-card>
    </div>
  </MentorLayout>
</template>

<script>
export default {
  methods: {
    getScoreClass(score) {
      if (!score) return "";
      if (score >= 8) return "score-excellent";
      if (score >= 6) return "score-good";
      if (score >= 4) return "score-average";
      return "score-weak";
    },
  },
};
</script>

<style scoped>
.my-reports-view {
  min-height: calc(100vh - 60px);
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
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  gap: 12px;
  flex: 1;
  flex-wrap: wrap;
}

.filter-select {
  width: 220px;
}

.extended-filters {
  background: transparent;
  border-radius: 8px;
  margin-bottom: 20px;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
}

.score-excellent {
  color: #22c55e;
  font-weight: 600;
}

.score-good {
  color: #3b82f6;
  font-weight: 600;
}

.score-average {
  color: #f59e0b;
  font-weight: 600;
}

.score-weak {
  color: #ef4444;
  font-weight: 600;
}

.filter-buttons { 
  display: flex; 
  gap: 12px; 
  align-items: center; 
}

.filter-buttons :deep(.el-button + .el-button) {
  margin-left: 0;
}

:deep(.el-table) {
  border-radius: 8px;
}

:deep(.el-table th) {
  background-color: #f9fafb !important;
  font-weight: 600;
  color: #374151;
}

:deep(.el-select__placeholder) {
  z-index: 1;
}

@media (max-width: 768px) {
  .filter-select {
    width: 100%;
  }

  .filter-group {
    flex-direction: column;
  }
}
</style>
