<template>
  <AdminLayout>
    <div class="batch-detail-container" v-loading="loading">
      <!-- Header -->
      <div class="page-header">
        <div class="header-left">
          <el-button 
            circle 
            @click="goBack"
            class="back-button"
          >
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <div>
            <h1 class="page-title">{{ batch.name || t('batch.detail.title') }}</h1>
          </div>
        </div>
        <div class="header-actions" v-if="userRole === 'ADMIN'">
          <el-button type="primary" @click="handleEdit">
            <el-icon><Edit /></el-icon>
            {{ t('batch.edit') }}
          </el-button>
        </div>
      </div>

      <!-- Batch Info Card -->
      <el-card shadow="hover" class="info-card">
        <template #header>
          <div class="card-header">
            <h3>{{ t('batch.detail.batchInfo') }}</h3>
            <el-tag :type="getStatusType(batch.status)" size="large">
              {{ batch.status }}
            </el-tag>
          </div>
        </template>

        <el-row :gutter="24">
          <el-col :span="12">
            <div class="info-item">
              <span class="info-label">{{ t('batch.name') }}:</span>
              <span class="info-value">{{ batch.name || '-' }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <span class="info-label">{{ t('batch.duration') }}:</span>
              <span class="info-value">
                {{ formatDate(batch.startDate) }} - {{ formatDate(batch.endDate) }}
              </span>
            </div>
          </el-col>
          <el-col :span="24" v-if="batch.description">
            <div class="info-item">
              <span class="info-label">{{ t('batch.description') }}:</span>
              <span class="info-value description-text">{{ batch.description }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <span class="info-label">{{ t('batch.internCount') }}:</span>
              <el-badge class="intern-badge">
                <span class="info-value">{{ batch.internCount || 0 }} {{ t('batch.detail.interns') }}</span>
              </el-badge>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <span class="info-label">{{ t('internManagement.detail.createdAt') }}:</span>
              <span class="info-value">{{ formatDateTime(batch.createdAt) }}</span>
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
            style="width: 300px; margin-right: 16px"
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
          >
            <el-option 
              v-for="(label, value) in statusOptions" 
              :key="value"
              :label="label" 
              :value="value" 
            />
          </el-select>
        </div>

        <!-- Table -->
        <el-table 
          :data="interns" 
          v-loading="loadingInterns"
          style="width: 100%; margin-top: 20px"
          stripe
        >
          <el-table-column prop="id" label="ID" width="80" />
          
          <el-table-column :label="t('internManagement.table.fullName')" min-width="180">
            <template #default="{ row }">
              <div class="intern-name-cell">
                <el-avatar :size="32" class="avatar">
                  {{ row.fullName?.charAt(0)?.toUpperCase() }}
                </el-avatar>
                <span>{{ row.fullName }}</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column :label="t('internManagement.table.position')" min-width="120">
            <template #default="{ row }">
              {{ row.position?.title || '-' }}
            </template>
          </el-table-column>
          
          <el-table-column :label="t('internManagement.table.mentor')" min-width="150">
            <template #default="{ row }">
              {{ row.mentor?.fullName || '-' }}
            </template>
          </el-table-column>
          
          <el-table-column :label="t('internManagement.table.startDate')" width="120">
            <template #default="{ row }">
              {{ formatDate(row.startDate) }}
            </template>
          </el-table-column>
          
          <el-table-column :label="t('internManagement.table.endDate')" width="120">
            <template #default="{ row }">
              {{ formatDate(row.endDate) }}
            </template>
          </el-table-column>
          
          <el-table-column :label="t('internManagement.table.status')" width="120">
            <template #default="{ row }">
              <el-tag :type="getInternStatusType(row.internStatus)" size="small">
                {{ t(`internManagement.status.${row.internStatus}`) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column :label="t('common.actions')" width="120" fixed="right">
            <template #default="{ row }">
              <el-button 
                link 
                type="primary" 
                size="small" 
                @click="viewIntern(row)"
              >
                <el-icon><View /></el-icon>
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- Pagination -->
        <div class="pagination-wrapper" v-if="pagination.totalItems.value > 0">
          <el-pagination
            :current-page="pagination.currentPage.value"
            :page-size="pagination.pageSize.value"
            :total="pagination.totalItems.value"
            layout="prev, pager, next"
            background
            @current-change="handlePageChange"
          />
        </div>

        <!-- Empty State -->
        <el-empty 
          v-if="!loadingInterns && interns.length === 0" 
          :description="t('batch.detail.noInterns')"
          style="padding: 40px 0"
        />
      </el-card>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Edit, View, Search } from '@element-plus/icons-vue'
import AdminLayout from '@/layouts/dashboard/AdminLayout.vue'
import { useAuthStore } from '@/stores/auth'
import { useLocaleStore } from '@/locales/locale'
import { useDateFormat, useStatus, usePagination, useLoading } from '@/composables'
import * as batchApi from '@/api/internship-batch'
import * as internApi from '@/api/intern'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)
const { formatDate } = useDateFormat()
const { getStatusType } = useStatus()

const userRole = computed(() => authStore.userRole)
const batchId = computed(() => route.params.id)

// Pagination composable
const pagination = usePagination({
  initialPage: 1,
  initialPageSize: 10,
  onPageChange: () => fetchInterns()
})

// Loading composable
const { loading, withLoading } = useLoading()

// State
const loadingInterns = ref(false)
const batch = ref({})
const interns = ref([])
const searchKeyword = ref('')
const filterStatus = ref('')

// Status options
const statusOptions = computed(() => ({
  ACTIVE: t.value('internManagement.status.ACTIVE'),
  WARNING: t.value('internManagement.status.WARNING'),
  COMPLETED: t.value('internManagement.status.COMPLETED'),
  DROPPED: t.value('internManagement.status.DROPPED')
}))

// Fetch batch detail
const fetchBatchDetail = async () => {
  await withLoading(async () => {
    try {
      const response = await batchApi.getBatchById(batchId.value)
      batch.value = response.data?.data || {}
      
      // Always fetch interns separately for pagination and filtering
      await fetchInterns()
    } catch (error) {
      console.error('Failed to fetch batch detail:', error)
      ElMessage.error(t.value('batch.detail.loadError'))
    }
  })
}

// Fetch interns
const fetchInterns = async () => {
  loadingInterns.value = true
  try {
    const params = {
      ...pagination.apiParams.value,
      ...(searchKeyword.value && { keyword: searchKeyword.value }),
      ...(filterStatus.value && { status: filterStatus.value })
    }
    
    const response = await internApi.getInternsByBatch(batchId.value, params)
    const data = response.data?.data || {}
    interns.value = data.items || []
    pagination.setTotalItems(data.totalItems || data.total || 0)
  } catch (error) {
    console.error('Failed to fetch interns:', error)
    ElMessage.error(t.value('batch.detail.loadInternsError'))
  } finally {
    loadingInterns.value = false
  }
}

// Handlers
const goBack = () => {
  router.back()
}

const handleEdit = () => {
  // Navigate back to list view - edit can be done from there
  router.push('/admin/batches')
}

const handleSearch = () => {
  pagination.firstPage()
  fetchInterns()
}

const handlePageChange = (page) => {
  pagination.setPage(page)
}

// Watch filters and auto-trigger search
watch(
  [searchKeyword, filterStatus],
  () => {
    handleSearch()
  }
)

const viewIntern = (intern) => {
  router.push(`/admin/interns/${intern.id}`)
}

const getInternStatusType = (status) => {
  const types = {
    ACTIVE: 'success',
    WARNING: 'warning',
    COMPLETED: 'info',
    DROPPED: 'danger'
  }
  return types[status] || 'info'
}

const formatDateTime = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// Lifecycle
onMounted(() => {
  fetchBatchDetail()
})
</script>

<style scoped>
.batch-detail-container {
  padding: 24px;
  background-color: #f8f9fa;
  min-height: calc(100vh - 60px);
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
}

/* Cards */
.info-card,
.interns-card {
  margin-bottom: 24px;
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
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}

:deep(.el-pagination) {
  justify-content: flex-start;
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
}
</style>
