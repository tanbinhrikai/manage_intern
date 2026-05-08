<script setup>
import { ref, computed, onMounted, onBeforeUnmount, onDeactivated } from "vue"
import { onBeforeRouteLeave } from "vue-router"
import { Search, OfficeBuilding, Plus, View, Edit, Lock, Unlock, Refresh } from '@element-plus/icons-vue'
import { useLocaleStore } from '@/locales/locale'
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue"
import MentorFormDialog from "@/components/mentor/MentorFormDialog.vue"
import { getMentors, createMentor, updateMentor, toggleUserStatus } from '@/api/user'
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
const mentorFormDialog = useDialog()
const mentorDetailDialog = useDialog()
const { confirm, confirmUpdate } = useConfirm()

// Data
const mentors = ref([])
const searchName = ref("")
const filterStatus = ref("")
const filterDepartment = ref("")

/**
 * Fetch mentors from backend with current filters and pagination
 */
async function fetchMentors() {
  await withLoading(async () => {
    const params = {
      ...pagination.apiParams.value,
      keyword: searchName.value || undefined,
      department_id: filterDepartment.value || undefined,
      is_active: filterStatus.value === 'ACTIVE' ? true : filterStatus.value === 'LOCKED' ? false : undefined
    }
    
    const res = await executeApi(
      () => getMentors(params),
      null,
      'mentorManagement.messages.loadError'
    )
    console.log(res)
    
    mentors.value = res.data?.data?.items || []
    pagination.setTotalItems(res.data?.data?.totalItems || 0)
  })
}

/**
 * Open form dialog for creating a new mentor
 */
function openAddMentor() {
  mentorFormDialog.open()
}

/**
 * Open form dialog for editing an existing mentor
 * @param {Mentor} mentor - Mentor to edit
 */
function openEditMentor(mentor) {
  mentorFormDialog.open(mentor)
}

/**
 * Open detail dialog for a mentor
 * @param {Mentor} mentor - Mentor to view
 */
function openDetailMentor(mentor) {
  mentorDetailDialog.open(mentor)
}

/**
 * Close mentor detail dialog
 */
function closeMentorDetail() {
  mentorDetailDialog.close()
}

/**
 * @param {number} size - New page size
 */
function handleSizeChange(size) {
  pagination.setPageSize(size)
  pagination.firstPage()
  fetchMentors()
}

/**
 * Handle save mentor (create or update)
 * @param {Object} payload - Mentor data to save
 * @param {Function} done - Callback function
 */
async function handleSaveMentor(payload, done) {
  try {
    if (mentorFormDialog.selectedItem) {
      await executeApi(
        () => updateMentor(mentorFormDialog.selectedItem.id, payload),
        'mentorManagement.messages.updateSuccess',
        false
      )
    } else {
      await executeApi(
        () => createMentor(payload),
        'mentorManagement.messages.createSuccess',
        false
      )
    }
    fetchMentors()
    mentorFormDialog.close()
  } catch (error) {
    // Error already handled by useApi
  } finally {
    done?.()
  }
}

/**
 * Handle save mentor with confirmation
 * @param {Object} payload - Mentor data to save
 * @param {Function} done - Callback function
 */
function handleSaveMentorWithConfirm(payload, done) {
  const isEdit = !!mentorFormDialog.selectedItem
  const message = isEdit
    ? (t.value('mentorManagement.confirm.update') || 'Are you sure you want to update this mentor?')
    : (t.value('mentorManagement.confirm.create') || 'Are you sure you want to create this mentor?')
  
  confirmUpdate({
    message,
    onConfirm: () => handleSaveMentor(payload, done)
  })
}

/**
 * Handle toggle mentor active status with confirmation
 * @param {Mentor} mentor - Mentor to toggle status
 */
function handleToggleStatus(mentor) {
  const confirmMessage = mentor.isActive 
    ? t.value('mentorManagement.confirm.lockAccount').replace('{name}', mentor.fullName)
    : t.value('mentorManagement.confirm.unlockAccount').replace('{name}', mentor.fullName)
  
  confirm({
    message: confirmMessage,
    title: t.value('mentorManagement.confirm.title'),
    type: 'warning',
    onConfirm: async () => {
      const res = await executeApi(
        () => toggleUserStatus(mentor.id),
        'mentorManagement.messages.toggleSuccess',
        true
      )
      if (res.data && res.data.data) {
        const index = mentors.value.findIndex(m => m.id === mentor.id)
        if (index !== -1) {
          mentors.value[index] = res.data.data
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
  fetchMentors()
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
  fetchMentors()
  fetchDepartments()
})

onBeforeUnmount(() => {
  mentorFormDialog.reset()
  mentorDetailDialog.reset()
})

onDeactivated(() => {
  mentorFormDialog.reset()
  mentorDetailDialog.reset()
})

onBeforeRouteLeave(() => {
  mentorFormDialog.reset()
  mentorDetailDialog.reset()
})
</script>

<template>
  <AdminLayout>
    <div class="mentor-list-view">
      <el-card class="main-card" shadow="never">
        <template #header>
          <div class="card-header">
            <h2 class="page-title">{{ t('mentorManagement.title') }}</h2>
          </div>
        </template>

        <div class="toolbar">
          <div class="search-group">
            <el-input 
              v-model="searchName" 
              :placeholder="t('mentorManagement.searchByName')"
              :prefix-icon="Search"
              clearable
              class="search-input"
              @keyup.enter="handleSearch"
            />

            <el-select 
              v-model="filterStatus" 
              :placeholder="t('mentorManagement.allStatus')"
              clearable
              class="filter-select"
              @change="handleSearch"
            >
              <el-option :label="t('mentorManagement.allStatus')" value="" />
              <el-option :label="t('mentorManagement.status.active')" value="ACTIVE" />
              <el-option :label="t('mentorManagement.status.locked')" value="LOCKED" />
            </el-select>

            <el-select 
              v-model="filterDepartment" 
              :placeholder="t('mentorManagement.allDepartments')"
              clearable
              class="filter-select"
              style="width: 200px;"
              @change="handleSearch"
            >
              <el-option :label="t('mentorManagement.allDepartments')" value="" />
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
                {{ t('mentorManagement.clearFilters') }}
              </el-button>
            </div>
          </div>
          
          <el-button 
            type="primary"
            :icon="Plus"
            @click="openAddMentor"
          >
            {{ t('mentorManagement.addNew') }}
          </el-button>
        </div>

        <div class="table-section">
          <el-table 
            :data="mentors" 
            stripe 
            style="width: 100%"
            height="100%"
            v-loading="loading"
          >
            <el-table-column 
              prop="fullName" 
              :label="t('mentorManagement.table.fullName')" 
              min-width="150" 
            />
            <el-table-column 
              prop="email" 
              :label="t('mentorManagement.table.email')" 
              min-width="200" 
            />
            <el-table-column 
              :label="t('mentorManagement.table.department')" 
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
              :label="t('mentorManagement.table.status')" 
              min-width="120" 
              align="center"
            >
              <template #default="scope">
                <el-tag :type="scope.row.active ? 'success' : 'danger'">
                  {{ scope.row.active ? t('mentorManagement.status.active') : t('mentorManagement.status.locked') }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column 
              :label="t('mentorManagement.table.actions')" 
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
                  @click="openDetailMentor(scope.row)"
                />
                <el-button 
                  type="warning" 
                  :icon="Edit" 
                  size="small" 
                  circle
                  @click="openEditMentor(scope.row)"
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
        :visible="mentorFormDialog.visible"
        :mentor="mentorFormDialog.selectedItem"
        :departments="departments"
        @update:visible="mentorFormDialog.visible = $event"
        @save="handleSaveMentorWithConfirm"
      />

      <el-dialog 
        v-model="mentorDetailDialog.visible" 
        :title="t('mentorManagement.detail.title')"
        width="450px"
      >
        <el-descriptions :column="1" border>
          <el-descriptions-item :label="t('mentorManagement.detail.mentorName')">
            {{ mentorDetailDialog.selectedItem?.fullName }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('mentorManagement.table.email')">
            {{ mentorDetailDialog.selectedItem?.email }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('mentorManagement.detail.department')">
            <el-tag type="info" v-if="mentorDetailDialog.selectedItem?.department">
              {{ mentorDetailDialog.selectedItem?.department?.title }}
            </el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item :label="t('mentorManagement.table.status')">
            <el-tag :type="mentorDetailDialog.selectedItem?.active ? 'success' : 'danger'">
              {{ mentorDetailDialog.selectedItem?.active ? t('mentorManagement.status.active') : t('mentorManagement.status.locked') }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="t('mentorManagement.detail.internCount')">
            {{ mentorDetailDialog.selectedItem?.internCount || 0 }}
          </el-descriptions-item>
        </el-descriptions>

        <template #footer>
          <el-button @click="closeMentorDetail">
            {{ t('mentorManagement.detail.close') }}
          </el-button>
        </template>
      </el-dialog>
    </div>
  </AdminLayout>
</template>

<style scoped>
.mentor-list-view {
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
