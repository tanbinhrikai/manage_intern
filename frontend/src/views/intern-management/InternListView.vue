<script setup>
import { ref, computed, onMounted, onBeforeUnmount, onDeactivated, watch } from "vue";
import { useRouter, onBeforeRouteLeave } from "vue-router";
import {
  Search,
  Plus,
  View,
  Edit,
  Delete,
  Calendar,
  ArrowDown,
  ArrowUp,
  Filter,
  Refresh
} from "@element-plus/icons-vue";
import { useLocaleStore } from "@/locales/locale";
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue";
import InternFormDialog from "@/components/intern/InternFormDialog.vue";
import {
  getInterns,
  createIntern,
  updateIntern,
  deleteIntern,
} from "@/api/intern";
import { usePagination, useLoading, useApi, useDropdownData, useDialog, useStatus, useDateFormat, useConfirm } from "@/composables";

const localeStore = useLocaleStore();
const router = useRouter();
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
  showSuccessMessage: true
});

// Composables
const { positions, mentors, internshipBatches, fetchPositions, fetchMentors, fetchInternshipBatches } = useDropdownData()
const internFormDialog = useDialog()
const internDetailDialog = useDialog()
const { getStatusType, statusOptions } = useStatus()
const { formatDate } = useDateFormat()
const { confirmDelete } = useConfirm()

// Data
const interns = ref([]);
const searchName = ref("");
const filterStatus = ref("");
const filterPosition = ref("");
const filterMentor = ref("");
const filterStartDate = ref(null);
const filterEndDate = ref(null);
const showAdvancedFilters = ref(false);

/**
 * Fetch interns from backend with current filters and pagination
 */
async function fetchInterns() {
  await withLoading(async () => {
    const params = {
      ...pagination.apiParams.value,
      keyword: searchName.value || undefined,
      status: filterStatus.value || undefined,
      position_id: filterPosition.value || undefined,
      mentor_id: filterMentor.value || undefined,
      start_date: filterStartDate.value || undefined,
      end_date: filterEndDate.value || undefined,
    };
    
    const res = await executeApi(
      () => getInterns(params),
      null,
      "internManagement.messages.loadError"
    );
    
    interns.value = res.data?.data?.items || [];
    pagination.setTotalItems(res.data?.data?.totalItems || 0);
  });
}

/**
 * Open form dialog for creating a new intern
 */
function openAddIntern() {
  internFormDialog.open();
}

/**
 * Navigate to edit page for an intern
 * @param {Intern} intern - Intern to edit
 */
function openEditIntern(intern) {
  router.push(`/admin/interns/${intern.id}/edit`);
}

/**
 * Navigate to detail page for an intern
 * @param {Intern} intern - Intern to view
 */
function openDetailIntern(intern) {
  router.push(`/admin/interns/${intern.id}`);
}

/**
 * Handle save intern (create or update)
 * @param {Object} payload - Intern data to save
 * @param {Function} done - Callback function
 */
async function handleSaveIntern(payload, done) {
  try {
    if (internFormDialog.selectedItem) {
      await executeApi(
        () => updateIntern(internFormDialog.selectedItem.id, payload),
        "internManagement.messages.updateSuccess",
        false
      );
    } else {
      await executeApi(
        () => createIntern(payload),
        "internManagement.messages.createSuccess",
        false
      );
    }
    fetchInterns();
    internFormDialog.close();
  } catch (error) {
    // Error already handled by useApi
  } finally {
    done?.();
  }
}

/**
 * Handle delete intern with confirmation
 * @param {Intern} intern - Intern to delete
 */
function handleDeleteIntern(intern) {
  const confirmMessage = t
    .value("internManagement.confirm.delete")
    .replace("{name}", intern.fullName);

  confirmDelete({
    message: confirmMessage,
    title: t.value("internManagement.confirm.title"),
    onConfirm: async () => {
      await executeApi(
        () => deleteIntern(intern.id),
        "internManagement.messages.deleteSuccess",
        true
      );
      fetchInterns();
    }
  });
}

/**
 * Handle pagination page size change
 * @param {number} size - New page size
 */
function handleSizeChange(size) {
  pagination.setPageSize(size);
  pagination.firstPage();
  fetchInterns();
}

/**
 * Handle pagination page change
 * @param {number} page - New page number
 */
function handlePageChange(page) {
  pagination.setPage(page);
  fetchInterns();
}


/**
 * Handle search - reset to first page and fetch
 */
function handleSearch() {
  pagination.firstPage();
  fetchInterns();
}
/**
 * Clear all filters and search
 */
function clearFilters() {
  searchName.value = "";
  filterStatus.value = "";
  filterPosition.value = "";
  filterMentor.value = "";
  filterStartDate.value = null;
  filterEndDate.value = null;

  handleSearch();
}

onMounted(() => {
  fetchInterns();
  fetchPositions();
  fetchMentors({ is_active: true });
  fetchInternshipBatches();
});

onBeforeUnmount(() => {
  internFormDialog.reset();
  internDetailDialog.reset();
});

onDeactivated(() => {
  internFormDialog.reset();
  internDetailDialog.reset();
});

onBeforeRouteLeave(() => {
  internFormDialog.reset();
  internDetailDialog.reset();
});
</script>

<template>
  <AdminLayout>
    <div class="intern-list-view">
      <el-card class="main-card" shadow="never">
        <template #header>
          <div class="card-header">
            <h2 class="page-title">{{ t("internManagement.title") }}</h2>
          </div>
        </template>

        <div class="toolbar">
          <div class="basic-filters">
            <el-input
              v-model="searchName"
              :placeholder="t('internManagement.searchByName')"
              :prefix-icon="Search"
              clearable
              class="search-input"
              @keyup.enter="handleSearch"
            />

            <el-select
              v-model="filterPosition"
              :placeholder="t('internManagement.allPositions')"
              clearable
              class="filter-select"
              @change="handleSearch"
            >
              <el-option :label="t('internManagement.allPositions')" value="" />
              <el-option
                v-for="pos in positions"
                :key="pos.id"
                :label="pos.title"
                :value="pos.id"
              />
            </el-select>

            <el-select
              v-model="filterMentor"
              :placeholder="t('internManagement.allMentors')"
              clearable
              class="filter-select"
              @change="handleSearch"
            >
              <el-option :label="t('internManagement.allMentors')" value="" />
              <el-option
                v-for="mentor in mentors"
                :key="mentor.id"
                :label="mentor.fullName"
                :value="mentor.id"
              />
            </el-select>

            <div class="filter-buttons">
              <el-button
                type="info" 
                plain
                :icon="showAdvancedFilters ? ArrowUp : ArrowDown"
                @click="showAdvancedFilters = !showAdvancedFilters"
                class="toggle-filters-btn"
              >
                {{
                  showAdvancedFilters
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

          <el-button type="primary" :icon="Plus" @click="openAddIntern">
            {{ t("internManagement.addNew") }}
          </el-button>
        </div>

        <el-collapse-transition>
          <div v-show="showAdvancedFilters" class="advanced-filters">
            <el-select
              v-model="filterStatus"
              :placeholder="t('internManagement.allStatus')"
              clearable
              class="filter-select"
              @change="handleSearch"
            >
              <el-option :label="t('internManagement.allStatus')" value="" />
              <el-option
                v-for="status in statusOptions"
                :key="status"
                :label="t('internManagement.status.' + status)"
                :value="status"
              />
            </el-select>
            <el-date-picker
              v-model="filterStartDate"
              type="date"
              :placeholder="t('internManagement.startDateFrom')"
              clearable
              value-format="YYYY-MM-DD"
              class="date-picker"
              @change="handleSearch"
            />
            <el-date-picker
              v-model="filterEndDate"
              type="date"
              :placeholder="t('internManagement.endDateTo')"
              clearable
              value-format="YYYY-MM-DD"
              class="date-picker"
              @change="handleSearch"
            />
          </div>
        </el-collapse-transition>

        <div class="table-section">
          <el-table
              :data="interns"
              stripe
              style="width: 100%"
              height="100%"
              v-loading="loading"
          >
            <el-table-column
              prop="fullName"
              :label="t('internManagement.table.fullName')"
              min-width="150"
            />
            <el-table-column
              :label="t('internManagement.table.position')"
              min-width="140"
            >
              <template #default="scope">
                <el-tag type="info" v-if="scope.row.position">
                  {{ scope.row.position.title }}
                </el-tag>
                <span v-else class="text-muted">-</span>
              </template>
            </el-table-column>
            <el-table-column
              :label="t('internManagement.table.mentor')"
              min-width="150"
            >
              <template #default="scope">
                {{ scope.row.mentor?.fullName || "-" }}
              </template>
            </el-table-column>
            <el-table-column
              :label="t('internManagement.table.startDate')"
              min-width="120"
            >
              <template #default="scope">
                {{ formatDate(scope.row.startDate) }}
              </template>
            </el-table-column>
            <el-table-column
              :label="t('internManagement.table.endDate')"
              min-width="120"
            >
              <template #default="scope">
                {{ formatDate(scope.row.endDate) }}
              </template>
            </el-table-column>
            <el-table-column
              :label="t('internManagement.table.status')"
              min-width="120"
              align="center"
            >
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.internStatus)">
                  {{ t("internManagement.status." + scope.row.internStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column
              :label="t('internManagement.table.actions')"
              min-width="150"
              fixed="right"
              align="center"
            >
              <template #default="scope">
                <el-button
                  type="primary"
                  :icon="View"
                  size="small"
                  circle
                  @click="openDetailIntern(scope.row)"
                />
                <el-button
                  type="warning"
                  :icon="Edit"
                  size="small"
                  circle
                  @click="openEditIntern(scope.row)"
                />
                <el-button
                  type="danger"
                  :icon="Delete"
                  size="small"
                  circle
                  @click="handleDeleteIntern(scope.row)"
                />
              </template>
            </el-table-column>
          </el-table>
        </div>
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
            layout="prev, pager, next"
            :total="pagination.totalItems.value"
            @current-change="handlePageChange"
          />
        </div>
      </el-card>

      <InternFormDialog
        :visible="internFormDialog.visible"
        :intern="internFormDialog.selectedItem"
        :internshipBatches="internshipBatches"
        :positions="positions"
        :mentors="mentors"
        @update:visible="internFormDialog.visible = $event"
        @save="handleSaveIntern"
      />
    </div>
  </AdminLayout>
</template>

<style scoped>
.intern-list-view {
  height: calc(100vh - 60px);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.main-card {
  border-radius: 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.main-card :deep(.el-card__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #ebeef5;
}

.main-card :deep(.el-card__body) {
  padding: 24px;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.el-pagination {
  justify-content: flex-start;
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
  margin-bottom: 16px;
  gap: 16px;
}

.basic-filters {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.advanced-filters {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  background-color: transparent;
  border-radius: 8px;
  margin-bottom: 16px;
}

.toggle-filters-btn {
  color: #6b7280;
}

.toggle-filters-btn:hover,
.toggle-filters-btn:focus {
  color: #fff;
}

.search-input {
  width: 200px;
}

.filter-select {
  width: 160px;
}

.date-picker {
  width: 160px;
}

.text-muted {
  color: #9ca3af;
}

.pagination-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  flex-shrink: 0;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-buttons {
  display: flex;
  gap: 12px;
  align-items: center;
}

.filter-buttons :deep(.el-button + .el-button) {
  margin-left: 0;
}

.table-section {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

:deep(.el-table) {
  border-radius: 8px;
}

:deep(.el-table th) {
  background-color: #f9fafb !important;
  font-weight: 600;
  color: #374151;
}

:deep(.el-button.is-circle) {
  margin: 0 4px;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #ebeef5;
  padding: 16px 20px;
  margin: 0;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #ebeef5;
  padding: 16px 20px;
}
</style>
