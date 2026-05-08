<template>
  <AdminLayout>
    <div class="batch-detail-container" v-loading="loading">
      <!-- Header -->
      <div class="page-header">
        <div class="header-left">
          <el-button circle @click="goBack" class="back-button">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <div>
            <h1 class="page-title">
              {{ batch.name || t("batch.detail.title") }}
            </h1>
          </div>
        </div>
        <div class="header-actions">
          <el-button v-if="userRole === 'ADMIN'" type="primary" @click="handleEdit">
            <el-icon><Edit /></el-icon>
            {{ t("batch.edit") }}
          </el-button>

          <el-button
            type="success"
            :disabled="batch.status !== 'ONGOING'"
            @click="handleGenRoadmap"
          >
            <el-icon><View /></el-icon>
            Gen Roadmap
          </el-button>
        </div>
      </div>

      <!-- Batch Info Card -->
      <el-card shadow="hover" class="info-card">
        <template #header>
          <div class="card-header">
            <h3>{{ t("batch.detail.batchInfo") }}</h3>
            <el-tag :type="getStatusType(batch.status)" size="large">
              {{ batch.status }}
            </el-tag>
          </div>
        </template>

        <el-row :gutter="24">
          <el-col :span="12">
            <div class="info-item">
              <span class="info-label">{{ t("batch.name") }}:</span>
              <span class="info-value">{{ batch.name || "-" }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <span class="info-label">{{ t("batch.duration") }}:</span>
              <span class="info-value">
                {{ formatDate(batch.startDate) }} -
                {{ formatDate(batch.endDate) }}
              </span>
            </div>
          </el-col>
          <el-col :span="24" v-if="batch.description">
            <div class="info-item">
              <span class="info-label">{{ t("batch.description") }}:</span>
              <span class="info-value description-text">{{
                batch.description
              }}</span>
            </div>
          </el-col>

          <el-col :span="12">
            <div class="info-item">
              <span class="info-label">{{ t("batch.internCount") }}:</span>
              <el-badge class="intern-badge">
                <span class="info-value"
                  >{{ batch.internCount || 0 }}
                  {{ t("batch.detail.interns") }}</span
                >
              </el-badge>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <span class="info-label"
                >{{ t("internManagement.detail.createdAt") }}:</span
              >
              <span class="info-value">{{
                formatDateTime(batch.createdAt)
              }}</span>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- Interns Table -->
      <el-card shadow="hover" class="interns-card">
        <!-- Filters -->
        <div class="filter-section">
          <el-input
            v-model="searchKeyword"
            :placeholder="t('internManagement.searchByName')"
            clearable
            style="width: 300px;"
            @keyup.enter="handleSearch"
            >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>

          <el-select
            v-model="filterStatus"
            :placeholder="t('internManagement.filterByStatus')"
            clearable
            style="width: 200px"
            @change="handleSearch"
          >
            <el-option
              v-for="(label, value) in statusOptions"
              :key="value"
              :label="label"
              :value="value" 
              />
          </el-select>
          <el-button
            type="info"
            plain
            :icon="Refresh"
            @click="clearFilters"
          >
            {{ t("internManagement.clearFilters") }}
          </el-button>
        </div>

        <!-- Table -->
        <el-table
          :data="interns"
          v-loading="loadingInterns"
          style="width: 100%"
          height="calc(100vh - 620px)"
          stripe
        >
          <el-table-column prop="id" label="ID" width="80" />

          <el-table-column
            :label="t('internManagement.table.fullName')"
            min-width="180">
            <template #default="{ row }">
              <div class="intern-name-cell">
                <el-avatar :size="32" class="avatar">
                  {{ row.fullName?.charAt(0)?.toUpperCase() }}
                </el-avatar>
                <span>{{ row.fullName }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column
            :label="t('internManagement.table.position')"
            min-width="120">
            <template #default="{ row }">
              {{ row.position?.title || "-" }}
            </template>
          </el-table-column>

          <el-table-column
            :label="t('internManagement.table.mentor')"
            min-width="150">
            <template #default="{ row }">
              {{ row.mentor?.fullName || "-" }}
            </template>
          </el-table-column>

          <el-table-column
            :label="t('internManagement.table.startDate')"
            width="120">
            <template #default="{ row }">
              {{ formatDate(row.startDate) }}
            </template>
          </el-table-column>

          <el-table-column
            :label="t('internManagement.table.endDate')"
            width="120">
            <template #default="{ row }">
              {{ formatDate(row.endDate) }}
            </template>
          </el-table-column>

          <el-table-column
            :label="t('internManagement.table.status')"
            width="120">
            <template #default="{ row }">
              <el-tag
                :type="getInternStatusType(row.internStatus)"
                size="small">
                {{ t(`internManagement.status.${row.internStatus}`) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column
            :label="t('common.actions')"
            width="120"
            fixed="right">
            <template #default="{ row }">
              <el-button
                link
                type="primary"
                size="small"
                @click="viewIntern(row)">
                <el-icon><View /></el-icon>
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- Pagination -->
        <div class="pagination-wrapper" v-if="pagination.totalItems.value > 0">
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
            layout="prev, pager, next"
            :total="pagination.totalItems.value"
            @current-change="handlePageChange"
          />
        </div>

        <!-- Empty State -->
        <el-empty
          v-if="!loadingInterns && interns.length === 0"
          :description="t('batch.detail.noInterns')"
          style="padding: 40px 0" />
      </el-card>
      <el-dialog
        v-model="dialogVisible"
        :title="isEditMode ? t('batch.edit') : t('batch.create')"
        width="600px"
        class="custom-dialog"
      >
        <el-form :model="form" ref="formRef" label-position="top">
          <el-form-item :label="t('batch.name')" prop="name">
            <el-input v-model="form.name" />
          </el-form-item>

          <div class="form-row">
            <el-form-item :label="t('batch.startDate')" prop="startDate" style="flex: 1">
              <el-date-picker
                v-model="form.startDate"
                type="date"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>

            <el-form-item :label="t('batch.endDate')" prop="endDate" style="flex: 1">
              <el-date-picker
                v-model="form.endDate"
                type="date"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </div>

          <el-form-item :label="t('batch.status')" v-if="isEditMode">
            <el-select v-model="form.status" style="width: 100%">
              <el-option label="ONGOING" value="ONGOING" />
              <el-option label="CANCEL" value="CANCEL" />
              <el-option label="COMPLETED" value="COMPLETED" />
            </el-select>
          </el-form-item>

          <el-form-item :label="t('batch.description')">
            <el-input v-model="form.description" type="textarea" :rows="4" />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="dialogVisible = false">{{ t("common.cancel") }}</el-button>
          <el-button type="primary" @click="handleSubmit">{{ t("common.submit") }}</el-button>
        </template>
      </el-dialog>
    </div>
  </AdminLayout>
</template>

<script setup>
import * as internApi from "@/api/intern";
import * as batchApi from "@/api/internship-batch";
import {
  useDateFormat,
  useLoading,
  usePagination,
  useStatus,
} from "@/composables";
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue";
import { useLocaleStore } from "@/locales/locale";
import { useAuthStore } from "@/stores/auth";
import { ArrowLeft, Edit, Search, View, Refresh } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const localeStore = useLocaleStore();
const t = computed(() => localeStore.t);
const { formatDate } = useDateFormat();
const { getStatusType } = useStatus();

const userRole = computed(() => authStore.userRole);
const batchId = computed(() => route.params.id);
const pageSizes = [20, 40, 60, 80, 100];
// Pagination composable
const pagination = usePagination({
  initialPage: 1,
  initialPageSize: 20,
});

// Loading composable
const { loading, withLoading } = useLoading();

// State
const loadingInterns = ref(false);
const batch = ref({});
const interns = ref([]);
const searchKeyword = ref("");
const filterStatus = ref("");
const dialogVisible = ref(false);
const isEditMode = ref(false);
const form = ref({
  id: null,
  name: "",
  startDate: "",
  endDate: "",
  status: "ONGOING",
  description: "",
});
const formRef = ref(null);

const handleSubmit = async () => {
  if (!formRef.value) return;

  try {
    await formRef.value.validate();

    const payload = {
      name: form.value.name,
      startDate: form.value.startDate,
      endDate: form.value.endDate,
      description: form.value.description,
      ...(isEditMode.value && { status: form.value.status }),
    };

    if (isEditMode.value) {
      await batchApi.updateBatch(form.value.id, payload);
      ElMessage.success(t.value("batch.updateSuccess"));
    }

    dialogVisible.value = false;
    await fetchBatchDetail();
  } catch (error) {
    console.error("Failed to save batch:", error);
    ElMessage.error(t.value("batch.saveError"));
  }
};

// Status options
const statusOptions = computed(() => ({
  ACTIVE: t.value("internManagement.status.ACTIVE"),
  WARNING: t.value("internManagement.status.WARNING"),
  COMPLETED: t.value("internManagement.status.COMPLETED"),
  DROPPED: t.value("internManagement.status.DROPPED"),
}));

// Fetch batch detail
const fetchBatchDetail = async () => {
  await withLoading(async () => {
    try {
      const response = await batchApi.getBatchById(batchId.value);
      batch.value = response.data?.data || {};

      // Always fetch interns separately for pagination and filtering
      await fetchInterns();
    } catch (error) {
      console.error("Failed to fetch batch detail:", error);
      ElMessage.error(t.value("batch.detail.loadError"));
    }
  });
};

// Fetch interns
const fetchInterns = async () => {
  loadingInterns.value = true;
  try {
    const params = {
      ...pagination.apiParams.value,
      ...(searchKeyword.value && { keyword: searchKeyword.value }),
      ...(filterStatus.value && { status: filterStatus.value }),
    };

    const response = await internApi.getInternsByBatch(batchId.value, params);
    const data = response.data?.data || {};
    interns.value = data.items || [];
    pagination.setTotalItems(data.totalItems || data.total || 0);
  } catch (error) {
    console.error("Failed to fetch interns:", error);
    ElMessage.error(t.value("batch.detail.loadInternsError"));
  } finally {
    loadingInterns.value = false;
  }
};

const clearFilters = () => {
  searchKeyword.value = "";
  filterStatus.value = "";
  handleSearch();
};

// Handlers
const goBack = () => {
  router.back();
};

const handleEdit = () => {
  isEditMode.value = true;
  form.value = {
    id: batch.value.id,
    name: batch.value.name || "",
    startDate: batch.value.startDate || "",
    endDate: batch.value.endDate || "",
    status: batch.value.status || "ONGOING",
    description: batch.value.description || "",
  };
  dialogVisible.value = true;
};

const handleSearch = () => {
  pagination.firstPage();
  fetchInterns();
};

const handlePageChange = (page) => {
  pagination.setPage(page);
};

const handleSizeChange = (size) => {
  pagination.setPageSize(size);
  pagination.firstPage();
  fetchInterns();
};

const viewIntern = (intern) => {
  router.push(`/admin/interns/${intern.id}`);
};

const getInternStatusType = (status) => {
  const types = {
    ACTIVE: "success",
    WARNING: "warning",
    COMPLETED: "info",
    DROPPED: "danger",
  };
  return types[status] || "info";
};

const formatDateTime = (dateString) => {
  if (!dateString) return "-";
  const date = new Date(dateString);
  return date.toLocaleString("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
};

// Lifecycle
onMounted(() => {
  fetchBatchDetail();
});

const handleGenRoadmap = () => {
  router.push({
    name: "RoadmapBuilder",
    query: { batchId: batchId.value },
  });
};
</script>

<style scoped>
.batch-detail-container {
  padding: 24px;
  background-color: #f8f9fa;
  min-height: calc(100vh - 60px);
  box-sizing: border-box;
}

.interns-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
}
/* Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-button {
  flex-shrink: 0;
}

.page-title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
}

.subtitle {
  color: #6b7280;
  font-size: 14px;
  margin-top: 4px;
  display: block;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* Cards */
.info-card,
.interns-card {
  margin-bottom: 24px;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.intern-count {
  color: #6b7280;
  font-size: 14px;
}

/* Info Items */
.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 16px;
}

.info-label {
  font-size: 13px;
  font-weight: 500;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  font-size: 15px;
  color: #1f2937;
  font-weight: 500;
}

.description-text {
  line-height: 1.6;
  color: #4b5563;
}

.intern-badge {
  display: inline-flex;
  align-items: center;
}

/* Filter Section */
.filter-section {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

/* Table */
.intern-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
}

/* Pagination */
.pagination-wrapper {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
  gap: 16px;
  flex-wrap: wrap;
  box-sizing: border-box;
}

.pagination-wrapper :deep(.el-pagination) {
  flex-shrink: 0;
}

/* Responsive */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .filter-section {
    flex-direction: column;
  }

  .pagination-wrapper {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
