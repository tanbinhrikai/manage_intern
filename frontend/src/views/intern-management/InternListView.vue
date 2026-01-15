<script setup>
import { ref, computed, onMounted } from "vue"
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, View, Edit, Delete } from '@element-plus/icons-vue'
import { useLocaleStore } from '@/locales/locale'
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue"
import InternFormDialog from "@/components/intern/InternFormDialog.vue"
import { getInterns, createIntern, updateIntern, deleteIntern } from '@/api/intern'
import { getPositions } from '@/api/position'
import { getMentors } from '@/api/user'

const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)

const interns = ref([])
const positions = ref([])
const mentors = ref([])
const currentPage = ref(1)
const pageSize = 10
const totalItems = ref(0)
const searchName = ref("")
const filterStatus = ref("")
const filterPosition = ref("")
const filterMentor = ref("")
const showInternForm = ref(false)
const showInternDetail = ref(false)
const selectedIntern = ref(null)
const loading = ref(false)

const statusOptions = ['ACTIVE', 'WARNING', 'COMPLETE', 'DROPPED']

const getStatusType = (status) => {
  const map = {
    ACTIVE: 'success',
    WARNING: 'warning',
    COMPLETE: 'primary',
    DROPPED: 'danger'
  }
  return map[status] || 'info'
}

const filteredInterns = computed(() => {
  let result = interns.value
  if (searchName.value) {
    result = result.filter(i => i.fullName.toLowerCase().includes(searchName.value.toLowerCase()))
  }
  if (filterStatus.value) {
    result = result.filter(i => i.internStatus === filterStatus.value)
  }
  if (filterPosition.value) {
    result = result.filter(i => i.position?.id === filterPosition.value)
  }
  if (filterMentor.value) {
    result = result.filter(i => i.mentor?.id === filterMentor.value)
  }
  return result
})

async function fetchInterns() {
  loading.value = true
  try {
    const res = await getInterns({ page: currentPage.value - 1, size: pageSize })
    interns.value = res.data?.data?.items || []
    totalItems.value = res.data?.data?.totalItems || 0
  } catch (error) {
    ElMessage.error(t.value('internManagement.messages.loadError'))
  } finally {
    loading.value = false
  }
}

async function fetchPositions() {
  try {
    const res = await getPositions()
    positions.value = res.data?.data || []
  } catch (error) {
    console.error("Failed to load positions:", error)
  }
}

async function fetchMentors() {
  try {
    const res = await getMentors()
    mentors.value = res.data?.data?.items || []
  } catch (error) {
    console.error("Failed to load mentors:", error)
  }
}

function openAddIntern() {
  selectedIntern.value = null
  showInternForm.value = true
}

function openEditIntern(intern) {
  selectedIntern.value = intern
  showInternForm.value = true
}

function openDetailIntern(intern) {
  selectedIntern.value = intern
  showInternDetail.value = true
}

function closeInternDetail() {
  showInternDetail.value = false
}

async function handleSaveIntern(payload, done) {
  try {
    if (selectedIntern.value) {
      await updateIntern(selectedIntern.value.id, payload)
      ElMessage.success(t.value('internManagement.messages.updateSuccess'))
    } else {
      await createIntern(payload)
      ElMessage.success(t.value('internManagement.messages.createSuccess'))
    }
    fetchInterns()
    showInternForm.value = false
  } catch (error) {
    console.error("Save error:", error)
    if (error.response && error.response.data) {
      ElMessage.error('Error: ' + JSON.stringify(error.response.data))
    } else {
      ElMessage.error(t.value('internManagement.messages.saveError'))
    }
  } finally {
    done?.()
  }
}

async function handleDeleteIntern(intern) {
  const confirmMessage = t.value('internManagement.confirm.delete').replace('{name}', intern.fullName)
  
  try {
    await ElMessageBox.confirm(
      confirmMessage,
      t.value('internManagement.confirm.title'),
      { 
        confirmButtonText: t.value('internManagement.confirm.ok'), 
        cancelButtonText: t.value('internManagement.confirm.cancel'), 
        type: 'warning' 
      }
    )
    await deleteIntern(intern.id)
    ElMessage.success(t.value('internManagement.messages.deleteSuccess'))
    fetchInterns()
  } catch (error) {
    if (error !== 'cancel') {
      console.error("Delete error:", error)
      ElMessage.error(t.value('internManagement.messages.saveError'))
    }
  }
}

function handlePageChange(page) {
  currentPage.value = page
  fetchInterns()
}

function formatDate(date) {
  if (!date) return '-'
  return new Date(date).toLocaleDateString()
}

onMounted(() => {
  fetchInterns()
  fetchPositions()
  fetchMentors()
})
</script>

<template>
  <AdminLayout>
    <div class="intern-list-view">
      <el-card class="main-card" shadow="never">
        <template #header>
          <div class="card-header">
            <h2 class="page-title">{{ t('internManagement.title') }}</h2>
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
            <el-select 
              v-model="filterStatus" 
              :placeholder="t('internManagement.allStatus')"
              clearable
              class="filter-select"
            >
              <el-option
                v-for="status in statusOptions"
                :key="status"
                :label="t('internManagement.status.' + status)"
                :value="status"
              />
            </el-select>
            <el-select 
              v-model="filterPosition" 
              :placeholder="t('internManagement.allPositions')"
              clearable
              class="filter-select"
            >
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
            >
              <el-option
                v-for="mentor in mentors"
                :key="mentor.id"
                :label="mentor.fullName"
                :value="mentor.id"
              />
            </el-select>
          </div>
          
          <el-button 
            type="primary"
            :icon="Plus"
            @click="openAddIntern"
          >
            {{ t('internManagement.addNew') }}
          </el-button>
        </div>

        <el-table 
          :data="filteredInterns" 
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
            :label="t('internManagement.table.mentor')" 
            min-width="150"
          >
            <template #default="scope">
              {{ scope.row.mentor?.fullName || '-' }}
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

      <InternFormDialog
        v-model:visible="showInternForm"
        :intern="selectedIntern"
        @save="handleSaveIntern"
      />

      <el-dialog 
        v-model="showInternDetail" 
        :title="t('internManagement.detail.title')"
        width="500px"
      >
        <el-descriptions :column="1" border>
          <el-descriptions-item :label="t('internManagement.detail.internName')">
            {{ selectedIntern?.fullName }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('internManagement.detail.position')">
            <el-tag type="info" v-if="selectedIntern?.position">
              {{ selectedIntern.position.title }}
            </el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item :label="t('internManagement.detail.mentor')">
            {{ selectedIntern?.mentor?.fullName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('internManagement.detail.period')">
            {{ formatDate(selectedIntern?.startDate) }} ~ {{ formatDate(selectedIntern?.endDate) }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('internManagement.detail.status')">
            <el-tag :type="getStatusType(selectedIntern?.internStatus)">
              {{ t('internManagement.status.' + (selectedIntern?.internStatus || 'ACTIVE')) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="t('internManagement.detail.createdAt')">
            {{ formatDate(selectedIntern?.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('internManagement.detail.updatedAt')">
            {{ formatDate(selectedIntern?.updatedAt) }}
          </el-descriptions-item>
        </el-descriptions>

        <template #footer>
          <el-button @click="closeInternDetail">
            {{ t('internManagement.detail.close') }}
          </el-button>
        </template>
      </el-dialog>
    </div>
  </AdminLayout>
</template>

<style scoped>
.intern-list-view {
  max-width: 1400px;
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
  width: 200px;
}

.filter-select {
  width: 160px;
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
