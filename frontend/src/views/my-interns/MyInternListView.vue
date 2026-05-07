<script setup>
import { ref, computed, onMounted } from "vue"
import { Search, View, Edit, ArrowDown, ArrowUp, Refresh } from '@element-plus/icons-vue'
import { useLocaleStore } from '@/locales/locale'
import MentorLayout from "@/layouts/dashboard/MentorLayout.vue"
import { useRouter } from 'vue-router'
import { getMyIntern } from '@/api/intern'
import { getPositions } from '@/api/position'
import { usePagination, useLoading, useApi, useStatus, useDateFormat } from '@/composables'

const router = useRouter()
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
  showSuccessMessage: false
})
const { getStatusType } = useStatus()
const { formatDate } = useDateFormat()

// Data
const interns = ref([])
const positions = ref([])

// Filters
const showFilters = ref(false)
const searchName = ref("")
const selectedStatus = ref("")
const selectedPosition = ref(null)
const startDate = ref(null)
const endDate = ref(null)

// Intern status options
const statusOptions = computed(() => [
  { value: '', label: t.value('internManagement.allStatus') },
  { value: 'ACTIVE', label: t.value('internManagement.status.ACTIVE') },
  { value: 'WARNING', label: t.value('internManagement.status.WARNING') },
  { value: 'COMPLETED', label: t.value('internManagement.status.COMPLETED') },
  { value: 'DROPPED', label: t.value('internManagement.status.DROPPED') }
])

/**
 * Fetch positions for filter dropdown
 */
async function fetchPositions() {
  const res = await executeApi(() => getPositions({ limit: 100 }))
  positions.value = res.data?.data?.items || []
}

/**
 * Fetch interns assigned to current mentor
 */
async function fetchMyInterns() {
  await withLoading(async () => {
    const params = {
      ...pagination.apiParams.value,
      keyword: searchName.value || undefined,
      status: selectedStatus.value || undefined,
      position_id: selectedPosition.value || undefined,
      start_date: startDate.value || undefined,
      end_date: endDate.value || undefined
    }
    
    const res = await executeApi(
      () => getMyIntern(params),
      null,
      'internManagement.messages.loadError'
    )
    
    interns.value = res.data?.data?.items || []
    pagination.setTotalItems(res.data?.data?.totalItems || 0)
  })
}

/**
 * Handle search - reset to first page and fetch
 */
function handleSearch() {
  pagination.firstPage()
  fetchMyInterns()
}

/**
 * Clear all filters
 */
function clearFilters() {
  searchName.value = ""
  selectedStatus.value = ""
  selectedPosition.value = null
  startDate.value = null
  endDate.value = null
  handleSearch()
}
/**
 * Handle page size change
 * @param {number} size - New page size
 */
function handleSizeChange(size) {
  pagination.setPageSize(size)
  pagination.firstPage()
  fetchMyInterns()
}

/**
 * Navigate to detail page for an intern
 * @param {Intern} intern - Intern to view
 */
function openDetailIntern(intern) {
  router.push(`/mentor/my-interns/${intern.id}`)
}

/**
 * Navigate to edit page for an intern
 * @param {Intern} intern - Intern to edit
 */
function openEditIntern(intern) {
  router.push(`/mentor/my-interns/${intern.id}/edit`)
}

/**
 * Handle pagination page change
 * @param {number} page - New page number
 */
function handlePageChange(page) {
  pagination.setPage(page)
  fetchMyInterns()
}

// Fetch data on mount
onMounted(() => {
  fetchPositions()
  fetchMyInterns()
})
</script>

<template>
  <MentorLayout :page-title="t('sidebar.myInterns')">
    <div class="my-intern-list-view">
      <el-card class="main-card" shadow="never">

        <div class="toolbar">
          <div class="filter-group">
            <el-input 
              v-model="searchName" 
              :placeholder="t('internManagement.searchByName')"
              :prefix-icon="Search"
              clearable
              class="search-input"
              @keyup.enter="handleSearch"
            />
            <el-select
              v-model="selectedStatus"
              :placeholder="t('internManagement.filterByStatus')"
              clearable
              class="filter-select"
              @change="handleSearch"
            >
              <el-option
                v-for="option in statusOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
            <el-select
              v-model="selectedPosition"
              :placeholder="t('internManagement.filterByPosition')"
              clearable
              class="filter-select"
              @change="handleSearch"
            >
              <el-option
                v-for="pos in positions"
                :key="pos.id"
                :label="pos.title"
                :value="pos.id"
              />
            </el-select>
            <div class="filter-buttons">
              <el-button 
                type="info" 
                plain
                :icon="showFilters ? ArrowUp : ArrowDown"
                @click="showFilters = !showFilters"
              >
                {{ showFilters ? t('internManagement.hideFilters') : t('internManagement.moreFilters') }}
              </el-button>

              <el-button
                type="info"
                plain
                :icon="Refresh"
                @click="clearFilters"
              >
                {{ t('internManagement.clearFilters') }}
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
                    :placeholder="t('internManagement.startDateFrom')"
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
                    :placeholder="t('internManagement.endDateTo')"
                    style="width: 100%"
                    @change="handleSearch"
                  />
                </div>
              </el-col>
            </el-row>
          </div>
        </el-collapse-transition>

        <el-table 
          :data="interns" 
          stripe 
          style="width: 100%"
          height="calc(100vh - 240px)"
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
                {{ t('internManagement.status.' + scope.row.internStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column 
            :label="t('internManagement.table.actions')" 
            min-width="100" 
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
            layout="prev, pager, next"
            :total="pagination.totalItems.value"
            @current-change="handlePageChange"
          />
        </div>
      </el-card>

    </div>
  </MentorLayout>
</template>

<style scoped>
.my-intern-list-view {
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

.search-input {
  width: 220px;
}

.filter-select {
  width: 180px;
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

.filter-item label {
  font-size: 13px;
  font-weight: 500;
  color: #6b7280;
}

.filter-actions {
  display: flex;
  align-items: flex-end;
  padding-top: 22px;
}

.text-muted {
  color: #9ca3af;
}

.pagination-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
}

.filter-buttons {
  display: flex;
  gap: 12px;
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

@media (max-width: 768px) {
  .search-input,
  .filter-select {
    width: 100%;
  }
  
  .filter-group {
    flex-direction: column;
  }
}
</style>
