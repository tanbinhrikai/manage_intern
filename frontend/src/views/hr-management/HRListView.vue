<script setup>
import { ref, computed, onMounted, onBeforeUnmount, onDeactivated } from "vue"
import { onBeforeRouteLeave } from "vue-router"
import { Search, OfficeBuilding, Plus, View, Edit, Lock, Unlock, Refresh } from '@element-plus/icons-vue'
import { useLocaleStore } from '@/locales/locale'
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue"
import MentorFormDialog from "@/components/mentor/MentorFormDialog.vue"
import { getHRs, createUser, updateUser, toggleUserStatus } from '@/api/user'
import { usePagination, useLoading, useApi, useDropdownData, useDialog, useConfirm } from '@/composables'


const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)
const pageSizes = [20, 40, 60, 80, 100]
// Composables
const pagination = usePagination({
  initialPage: 1,
  initialPageSize: 20,
})

const { loading, withLoading } = useLoading()
const { execute: executeApi } = useApi({
  showErrorMessage: true,
  showSuccessMessage: true
})

// Composables
const { departments, fetchDepartments } = useDropdownData()
const hrFormDialog = useDialog()
const hrDetailDialog = useDialog()
const { confirm, confirmUpdate } = useConfirm()

// Data
const hrs = ref([])
const searchName = ref("")
const filterStatus = ref("")
const filterDepartment = ref("")

/**
 * Fetch HR users from backend with current filters and pagination
 */
async function fetchHRs() {
  await withLoading(async () => {
    const params = {
      ...pagination.apiParams.value,
      keyword: searchName.value || undefined,
      department_id: filterDepartment.value || undefined,
      is_active: filterStatus.value === 'ACTIVE' ? true : filterStatus.value === 'LOCKED' ? false : undefined
    }
    
    const res = await executeApi(
      () => getHRs(params),
      null,
      'hrManagement.messages.loadError'
    )
    
    hrs.value = res.data?.data?.items || []
    pagination.setTotalItems(res.data?.data?.totalItems || 0)
  })
}

/**
 * Open form dialog for creating a new HR user
 */
function openAddHR() {
  hrFormDialog.open()
}

/**
 * Open form dialog for editing an existing HR user
 * @param {User} hr - HR user to edit
 */
function openEditHR(hr) {
  hrFormDialog.open(hr)
}

/**
 * Open detail dialog for an HR user
 * @param {User} hr - HR user to view
 */
function openDetailHR(hr) {
  hrDetailDialog.open(hr)
}

/**
 * Close HR detail dialog
 */
function closeHRDetail() {
  hrDetailDialog.close()
}

/**
 * Handle page size change
 * @param {number} size - New page size
 */
function handleSizeChange(size) {
  pagination.setPageSize(size)
  pagination.firstPage()
  fetchHRs()
}

/**
 * Handle save HR user (create or update)
 * @param {Object} payload - HR user data to save
 * @param {Function} done - Callback function
 */
async function handleSaveHR(payload, done) {
  try {
    if (hrFormDialog.selectedItem) {
      await executeApi(
        () => updateUser(hrFormDialog.selectedItem.id, payload),
        'hrManagement.messages.updateSuccess',
        false // Don't show error here, let form handle it
      )
    } else {
      await executeApi(
        () => createUser(payload),
        'hrManagement.messages.createSuccess',
        false // Don't show error here, let form handle it
      )
    }
    fetchHRs()
    hrFormDialog.close()
  } catch (error) {
    // Error already handled by useApi (if enabled)
  } finally {
    done?.()
  }
}

/**
 * Handle save HR with confirmation
 * @param {Object} payload - HR user data to save
 * @param {Function} done - Callback function
 */
function handleSaveHRWithConfirm(payload, done) {
  const isEdit = !!hrFormDialog.selectedItem
  const message = isEdit
    ? (t.value('hrManagement.confirm.update') || 'Are you sure you want to update this HR?')
    : (t.value('hrManagement.confirm.create') || 'Are you sure you want to create this HR?')
  
  confirmUpdate({
    message,
    onConfirm: () => handleSaveHR(payload, done)
  })
}

/**
 * Handle toggle HR user active status with confirmation
 * @param {User} hr - HR user to toggle status
 */
function handleToggleStatus(hr) {
  const confirmMessage = hr.isActive 
    ? t.value('hrManagement.confirm.lockAccount').replace('{name}', hr.fullName)
    : t.value('hrManagement.confirm.unlockAccount').replace('{name}', hr.fullName)
  
  confirm({
    message: confirmMessage,
    title: t.value('hrManagement.confirm.title'),
    type: 'warning',
    onConfirm: async () => {
      const res = await executeApi(
        () => toggleUserStatus(hr.id),
        'hrManagement.messages.toggleSuccess',
        true
      )
      if (res.data && res.data.data) {
        const index = hrs.value.findIndex(h => h.id === hr.id)
        if (index !== -1) {
          hrs.value[index] = res.data.data
        }
      }
    }
  })
}

/**
 * Handle pagination page change
 * @param {number} page - New page number
 */
function handlePageChange(page) {
  pagination.setPage(page)
}

/**
 * Handle search - reset to first page and fetch
 */
function handleSearch() {
  pagination.firstPage()
  fetchHRs()
}

/**
 * Clear all filters
 */
function clearFilters() {
  searchName.value = ""
  filterStatus.value = ""
  filterDepartment.value = ""

  handleSearch()
}

onMounted(() => {
  fetchHRs()
  fetchDepartments()
})

onBeforeUnmount(() => {
  hrFormDialog.reset()
  hrDetailDialog.reset()
})

onDeactivated(() => {
  hrFormDialog.reset()
  hrDetailDialog.reset()
})

onBeforeRouteLeave(() => {
  hrFormDialog.reset()
  hrDetailDialog.reset()
})
</script>

<template>
  <AdminLayout>
    <div class="hr-list-view">
      <el-card class="main-card" shadow="never">
        <template #header>
          <div class="card-header">
            <h2 class="page-title">{{ t('hrManagement.title') }}</h2>
          </div>
        </template>

        <div class="toolbar">
          <div class="search-group">
            <el-input 
              v-model="searchName" 
              :placeholder="t('hrManagement.searchByName')"
              :prefix-icon="Search"
              clearable
              class="search-input"
              @keyup.enter="handleSearch"
            />

            <el-select 
              v-model="filterStatus" 
              :placeholder="t('hrManagement.allStatus')"
              clearable
              class="filter-select"
              @change="handleSearch"
            >
              <el-option :label="t('hrManagement.allStatus')" value="" />
              <el-option :label="t('hrManagement.status.active')" value="ACTIVE" />
              <el-option :label="t('hrManagement.status.locked')" value="LOCKED" />
            </el-select>

            <el-select 
              v-model="filterDepartment" 
              :placeholder="t('hrManagement.allDepartments')"
              clearable
              class="filter-select department-select"
              @change="handleSearch"
            >
              <el-option :label="t('hrManagement.allDepartments')" value="" />
              <el-option
                v-for="dept in departments"
                :key="dept.id"
                :label="dept.title"
                :value="dept.id"
              />
            </el-select>

            <div class="filter-buttons">
              <el-button 
                type="info"
                plain
                :icon="Refresh"
                @click="clearFilters"
              >
                {{ t('hrManagement.clearFilters') }}
              </el-button>
            </div>
          </div>
          
          <el-button 
            type="primary"
            :icon="Plus"
            @click="openAddHR"
          >
            {{ t('hrManagement.addNew') }}
          </el-button>
        </div>

        <div class="table-section">
          <el-table 
            :data="hrs" 
            stripe 
            style="width: 100%"
            height="100%"
            v-loading="loading"
          >
            <el-table-column 
              prop="fullName" 
              :label="t('hrManagement.table.fullName')" 
              min-width="150" 
            />
            <el-table-column 
              prop="email" 
              :label="t('hrManagement.table.email')" 
              min-width="200" 
            />
            <el-table-column 
              :label="t('hrManagement.table.department')" 
              min-width="150"
            
            >
              <template #default="scope">
                <el-tag type="info" v-if="scope.row.department">
                  {{ scope.row.department.title }}
                </el-tag>
                <span v-else class="text-muted">-</span>
              </template>
            </el-table-column>
            <el-table-column 
              :label="t('hrManagement.table.status')" 
              min-width="120" 
              align="center"
            >
              <template #default="scope">
                <el-tag :type="scope.row.active ? 'success' : 'danger'">
                  {{ scope.row.active ? t('hrManagement.status.active') : t('hrManagement.status.locked') }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column 
              :label="t('hrManagement.table.actions')" 
              min-width="200" 
              fixed="right" 
              align="center"
            >
              <template #default="scope">
                <el-button 
                  type="primary" 
                  :icon="View" 
                  size="small" 
                  circle
                  @click="openDetailHR(scope.row)"
                />
                <el-button 
                  type="warning" 
                  :icon="Edit" 
                  size="small" 
                  circle
                  @click="openEditHR(scope.row)"
                />
                <el-button 
                  :type="scope.row.active ? 'danger' : 'success'" 
                  :icon="scope.row.active ? Lock : Unlock" 
                  size="small" 
                  circle
                  @click="handleToggleStatus(scope.row)"
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

      <MentorFormDialog
        :visible="hrFormDialog.visible"
        :mentor="hrFormDialog.selectedItem"
        :departments="departments"
        user-type="HR"
        @update:visible="hrFormDialog.visible = $event"
        @save="handleSaveHRWithConfirm"
      />

      <el-dialog 
        v-model="hrDetailDialog.visible" 
        :title="t('hrManagement.detail.title')"
        width="450px"
      >
        <el-descriptions :column="1" border>
          <el-descriptions-item :label="t('hrManagement.detail.hrName')">
            {{ hrDetailDialog.selectedItem?.fullName }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('hrManagement.table.email')">
            {{ hrDetailDialog.selectedItem?.email }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('hrManagement.detail.department')">
            <el-tag type="info" v-if="hrDetailDialog.selectedItem?.department">
              {{ hrDetailDialog.selectedItem?.department?.title }}
            </el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item :label="t('hrManagement.table.status')">
            <el-tag :type="hrDetailDialog.selectedItem?.active ? 'success' : 'danger'">
              {{ hrDetailDialog.selectedItem?.active ? t('hrManagement.status.active') : t('hrManagement.status.locked') }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <template #footer>
          <el-button @click="closeHRDetail">
            {{ t('hrManagement.detail.close') }}
          </el-button>
        </template>
      </el-dialog>
    </div>
  </AdminLayout>
</template>

<style scoped>
.hr-list-view {
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

.table-section {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
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
}

.search-group {
  display: flex;
  gap: 12px;
  flex: 1;
  flex-wrap: wrap;
  max-width: none;
}

.search-input {
  width: 200px;
}

.filter-select {
  width: 160px;
}

.department-select {
  width: 200px;
}

.text-muted {
  color: #9ca3af;
}

.filter-buttons {
  display: flex;
  gap: 12px;
  align-items: center;
}

.filter-buttons :deep(.el-button + .el-button) {
  margin-left: 0;
}

.pagination-wrapper {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  gap: 16px;
  flex-wrap: wrap;
  flex-shrink: 0;
  box-sizing: border-box;
}

.pagination-wrapper :deep(.el-pagination) {
  flex-shrink: 0;
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
