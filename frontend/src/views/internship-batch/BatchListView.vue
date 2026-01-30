<template>
  <AdminLayout>
    <div class="batch-container">
      <div class="page-header">
        <div class="header-left">
          <h2>{{ t("batch.title") }}</h2>
          <span class="subtitle">Manage internship programs and timelines</span>
        </div>
        <div class="header-actions">
          <el-button
            type="primary"
            size="large"
            class="btn-create"
            @click="handleCreate"
            v-if="userRole === 'ADMIN'"
          >
            <el-icon class="mr-2"><Plus /></el-icon> {{ t("batch.create") }}
          </el-button>
        </div>
      </div>

      <div class="filter-bar">
        <el-input
          v-model="searchKeyword"
          :placeholder="t('batch.search')"
          prefix-icon="Search"
          class="search-input"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        <el-select
          v-model="filterStatus"
          :placeholder="t('batch.filterByStatus')"
          clearable
          class="status-select"
          @change="handleSearch"
        >
          <el-option label="ONGOING" value="ONGOING" />
          <el-option label="CANCEL" value="CANCEL" />
          <el-option label="COMPLETED" value="COMPLETED" />
        </el-select>
      </div>

      <div class="batch-grid" v-loading="loading">
        <div v-for="batch in batches" :key="batch.id" class="batch-card">
          <div class="card-top">
            <div class="batch-title-group">
              <span class="batch-id">#{{ batch.id }}</span>
              <h3 class="batch-name" :title="batch.name">{{ batch.name }}</h3>
            </div>
            <el-tag
              :type="getStatusType(batch.status)"
              effect="dark"
              round
              size="small"
            >
              {{ batch.status }}
            </el-tag>
          </div>

          <div class="card-body">
            <div class="info-row">
              <el-icon><Calendar /></el-icon>
              <span class="date-text">
                {{ formatDate(batch.startDate) }} -
                {{ formatDate(batch.endDate) }}
              </span>
            </div>

            <div class="info-row description">
              <el-icon><Document /></el-icon>
              <span class="desc-text" :title="batch.description">
                {{ batch.description || t("common.noDescription") }}
              </span>
            </div>

            <div class="divider"></div>

            <div class="card-footer">
              <div class="stat-item">
                <el-icon :size="18" color="#409eff"><UserFilled /></el-icon>
                <span class="stat-value">{{ batch.internCount || 0 }}</span>
                <span class="stat-label">Interns</span>
              </div>

              <div class="action-buttons">
                <el-tooltip :content="t('common.view')" placement="top">
                  <el-button circle size="small" @click="handleView(batch)">
                    <el-icon><View /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip
                  :content="t('common.edit')"
                  placement="top"
                  v-if="userRole === 'ADMIN'"
                >
                  <el-button
                    circle
                    size="small"
                    type="primary"
                    plain
                    @click="handleEdit(batch)"
                  >
                    <el-icon><Edit /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip
                  :content="t('common.delete')"
                  placement="top"
                  v-if="userRole === 'ADMIN'"
                >
                  <el-button
                    circle
                    size="small"
                    type="danger"
                    plain
                    @click="handleDelete(batch)"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </el-tooltip>
              </div>
            </div>
          </div>
        </div>

        <div v-if="!loading && batches.length === 0" class="empty-state">
          <el-empty :description="t('common.noData')" />
        </div>
      </div>

      <div class="pagination-wrapper" v-if="totalItems > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[8, 16, 40, 80]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalItems"
          @size-change="handleSearch"
          @current-change="handleSearch"
          background
        />
      </div>

      <el-dialog
        v-model="dialogVisible"
        :title="isEditMode ? t('batch.edit') : t('batch.create')"
        width="600px"
        class="custom-dialog"
      >
        <el-form
          :model="form"
          :rules="rules"
          ref="formRef"
          label-position="top"
        >
          <el-form-item :label="t('batch.name')" prop="name">
            <el-input
              v-model="form.name"
              :placeholder="t('batch.namePlaceholder')"
            />
          </el-form-item>

          <div class="form-row">
            <el-form-item
              :label="t('batch.startDate')"
              prop="startDate"
              style="flex: 1"
            >
              <el-date-picker
                v-model="form.startDate"
                type="date"
                :placeholder="t('batch.selectDate')"
                style="width: 100%"
                format="DD/MM/YYYY"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>

            <el-form-item
              :label="t('batch.endDate')"
              prop="endDate"
              style="flex: 1"
            >
              <el-date-picker
                v-model="form.endDate"
                type="date"
                :placeholder="t('batch.selectDate')"
                style="width: 100%"
                format="DD/MM/YYYY"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </div>

          <el-form-item
            :label="t('batch.status')"
            prop="status"
            v-if="isEditMode"
          >
            <el-select v-model="form.status" style="width: 100%">
              <el-option label="ONGOING" value="ONGOING" />
              <el-option label="CANCEL" value="CANCEL" />
              <el-option label="COMPLETED" value="COMPLETED" />
            </el-select>
          </el-form-item>

          <el-form-item :label="t('batch.description')" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="4"
              :placeholder="t('batch.descriptionPlaceholder')"
            />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="dialogVisible = false">{{
            t("common.cancel")
          }}</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ t("common.submit") }}
          </el-button>
        </template>
      </el-dialog>
    </div>
  </AdminLayout>
</template>

<script setup>
import * as batchApi from "@/api/internship-batch";
import { useDateFormat } from "@/composables";
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue";
import { useLocaleStore } from "@/locales/locale";
import { useAuthStore } from "@/stores/auth";
import {
  Delete,
  Edit,
  Plus,
  Search,
  View,
  Calendar,
  Document,
  UserFilled,
} from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const authStore = useAuthStore();
const localeStore = useLocaleStore();
const t = computed(() => localeStore.t);
const { formatDate } = useDateFormat();

const userRole = computed(() => authStore.userRole);

// State
const loading = ref(false);
const submitting = ref(false);
const batches = ref([]);
const searchKeyword = ref("");
const filterStatus = ref("");
const currentPage = ref(1);
const pageSize = ref(8); // Reduced page size for grid view
const totalItems = ref(0);

// Dialog state
const dialogVisible = ref(false);
const isEditMode = ref(false);
const formRef = ref(null);
const form = ref({
  id: null,
  name: "",
  startDate: "",
  endDate: "",
  status: "ONGOING",
  description: "",
});

// Validation rules
const rules = computed(() => ({
  name: [
    { required: true, message: t.value("batch.nameRequired"), trigger: "blur" },
  ],
  startDate: [
    {
      required: true,
      message: t.value("batch.startDateRequired"),
      trigger: "change",
    },
  ],
  endDate: [
    {
      required: true,
      message: t.value("batch.endDateRequired"),
      trigger: "change",
    },
  ],
}));

// Methods
const fetchBatches = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value - 1,
      limit: pageSize.value,
      keyword: searchKeyword.value || null,
      status: filterStatus.value || null,
    };

    const response = await batchApi.getBatches(params);
    const data = response.data.data;
    batches.value = data.items;
    totalItems.value = data.totalItems;
  } catch (error) {
    console.error("Failed to fetch batches:", error);
    ElMessage.error(t.value("batch.fetchError"));
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  currentPage.value = 1;
  fetchBatches();
};

const handleCreate = () => {
  isEditMode.value = false;
  form.value = {
    id: null,
    name: "",
    startDate: "",
    endDate: "",
    status: "ONGOING",
    description: "",
  };
  dialogVisible.value = true;
};

const handleEdit = (row) => {
  isEditMode.value = true;
  form.value = {
    id: row.id,
    name: row.name,
    startDate: row.startDate,
    endDate: row.endDate,
    status: row.status,
    description: row.description,
  };
  dialogVisible.value = true;
};

const handleView = (row) => {
  router.push(`/admin/batches/${row.id}`);
};

const handleSubmit = async () => {
  if (!formRef.value) return;

  try {
    const valid = await formRef.value.validate();
    if (!valid) return;

    submitting.value = true;

    const data = {
      name: form.value.name,
      startDate: form.value.startDate,
      endDate: form.value.endDate,
      description: form.value.description,
      ...(isEditMode.value && { status: form.value.status }),
    };

    if (isEditMode.value) {
      await batchApi.updateBatch(form.value.id, data);
      ElMessage.success(t.value("batch.updateSuccess"));
    } else {
      await batchApi.createBatch(data);
      ElMessage.success(t.value("batch.createSuccess"));
    }

    dialogVisible.value = false;
    await fetchBatches();
  } catch (error) {
    console.error("Failed to save batch:", error);
    ElMessage.error(t.value("batch.saveError"));
  } finally {
    submitting.value = false;
  }
};

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      t.value("batch.deleteConfirm", { name: row.name }),
      t.value("common.warning"),
      {
        confirmButtonText: t.value("common.confirm"),
        cancelButtonText: t.value("common.cancel"),
        type: "warning",
      }
    );

    await batchApi.deleteBatch(row.id);
    ElMessage.success(t.value("batch.deleteSuccess"));
    fetchBatches();
  } catch (error) {
    if (error !== "cancel") {
      console.error("Failed to delete batch:", error);
      ElMessage.error(t.value("batch.deleteError"));
    }
  }
};

const getStatusType = (status) => {
  const types = {
    ONGOING: "info",
    CANCEL: "success",
    COMPLETED: "danger",
  };
  return types[status] || "info";
};

onMounted(() => {
  fetchBatches();
});
</script>

<style scoped>
/* Main Layout */
.batch-container {
  padding: 24px;
  background-color: #f8f9fa;
  min-height: calc(100vh - 60px);
  font-family: "Inter", sans-serif;
}

/* Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.header-left h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
}
.subtitle {
  color: #6b7280;
  font-size: 14px;
  margin-top: 4px;
  display: block;
}

/* Filters */
.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  background: #fff;
  padding: 16px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  align-items: center;
}
.search-input {
  width: 300px;
}
.status-select {
  width: 180px;
}

/* GRID LAYOUT */
.batch-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

/* BATCH CARD DESIGN */
.batch-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  position: relative;
}

.batch-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px -8px rgba(0, 0, 0, 0.1);
  border-color: #bfdbfe;
}

/* Card Top */
.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}
.batch-title-group {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}
.batch-id {
  font-size: 11px;
  font-weight: 700;
  color: #9ca3af;
  text-transform: uppercase;
  margin-bottom: 4px;
}
.batch-name {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Card Body */
.card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #4b5563;
  font-size: 13px;
}
.description {
  align-items: flex-start;
}
.desc-text {
  color: #6b7280;
  font-size: 13px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.divider {
  height: 1px;
  background: #f3f4f6;
  margin: 4px 0;
}

/* Card Footer */
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f0f9eb;
  padding: 4px 12px;
  border-radius: 20px;
}
.stat-value {
  font-size: 15px;
  font-weight: 700;
  color: #1f2937;
}
.stat-label {
  font-size: 12px;
  color: #6b7280;
}

.action-buttons {
  display: flex;
  gap: 8px;
  opacity: 0.8;
  transition: opacity 0.2s;
}
.batch-card:hover .action-buttons {
  opacity: 1;
}

/* Utility */
.empty-state {
  grid-column: 1 / -1;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  border: 1px dashed #e5e7eb;
}
.pagination-wrapper {
  margin-top: 32px;
  display: flex;
  justify-content: flex-end;
}
.form-row {
  display: flex;
  gap: 16px;
}
</style>