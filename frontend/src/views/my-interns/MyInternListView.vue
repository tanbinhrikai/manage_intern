<script setup>
import { ref, computed, onMounted, watch } from "vue"
import { ElMessage } from 'element-plus'
import { Search, View, Edit } from '@element-plus/icons-vue'
import { useLocaleStore } from '@/locales/locale'
import MentorLayout from "@/layouts/dashboard/MentorLayout.vue"
import { useRouter } from 'vue-router'
import { getMyIntern } from '@/api/intern'

const router = useRouter()
const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)

const interns = ref([])
const currentPage = ref(1)
const pageSize = 10
const totalItems = ref(0)
const searchName = ref("")
const selectedIntern = ref(null)
const loading = ref(false)

const getStatusType = (status) => {
  const map = {
    ACTIVE: 'success',
    WARNING: 'warning',
    COMPLETE: 'primary',
    DROPPED: 'danger'
  }
  return map[status] || 'info'
}

async function fetchMyInterns() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      limit: pageSize,
      keyword: searchName.value || undefined
    }
    const res = await getMyIntern(params)
    interns.value = res.data?.data?.items || []
    totalItems.value = res.data?.data?.totalItems || 0
  } catch (error) {
    ElMessage.error(t.value('internManagement.messages.loadError'))
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchMyInterns()
}

function openDetailIntern(intern) {
  router.push(`/mentor/my-interns/${intern.id}`)
}

function openEditIntern(intern) {
  router.push(`/mentor/my-interns/${intern.id}/edit`)
}

function handlePageChange(page) {
  currentPage.value = page
  fetchMyInterns()
}


function formatDate(date) {
  if (!date) return '-'
  return new Date(date).toLocaleDateString()
}

watch([searchName], () => {
  handleSearch()
})

onMounted(() => {
  fetchMyInterns()
})
</script>

<template>
  <MentorLayout>
    <div class="my-intern-list-view">
      <el-card class="main-card" shadow="never">
        <template #header>
          <div class="card-header">
            <h2 class="page-title">{{ t('sidebar.myInterns') }}</h2>
          </div>
        </template>

        <div class="toolbar">
          <div class="filter-group">
            <el-input 
              v-model="searchName" 
              :placeholder="t('internManagement.searchByName')"
              :prefix-icon="Search"
              clearable
              class="search-input"
            />
          </div>
        </div>

        <el-table 
          :data="interns" 
          stripe 
          style="width: 100%"
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
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="totalItems"
            layout="prev, pager, next"
            background
            @current-change="handlePageChange"
          />
        </div>
      </el-card>

    </div>
  </MentorLayout>
</template>

<style scoped>
.my-intern-list-view {
  max-width: 1200px;
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
  width: 250px;
}

.filter-select {
  width: 180px;
}

.text-muted {
  color: #9ca3af;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
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
</style>
