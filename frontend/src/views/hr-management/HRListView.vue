<script setup>
import { ref, computed, onMounted, watch } from "vue"
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, OfficeBuilding, Plus, View, Edit, Lock, Unlock } from '@element-plus/icons-vue'
import { useLocaleStore } from '@/locales/locale'
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue"
import MentorFormDialog from "@/components/mentor/MentorFormDialog.vue"
import { getHRs, createUser, updateUser, toggleUserStatus } from '@/api/user'
import { getDepartments } from '@/api/department'

const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)

const hrs = ref([])
const departments = ref([])
const currentPage = ref(1)
const pageSize = 10
const totalItems = ref(0)
const searchName = ref("")
const filterStatus = ref("")
const filterDepartment = ref("")
const showHRForm = ref(false)
const showHRDetail = ref(false)
const selectedHR = ref(null)
const loading = ref(false)

async function fetchHRs() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      limit: pageSize,
      keyword: searchName.value || undefined,
      department_id: filterDepartment.value || undefined,
      is_active: filterStatus.value === 'ACTIVE' ? true : filterStatus.value === 'LOCKED' ? false : undefined
    }
    const res = await getHRs(params)
    hrs.value = res.data?.data?.items || []
    totalItems.value = res.data?.data?.totalItems || 0
  } catch (error) {
    ElMessage.error(t.value('hrManagement.messages.loadError'))
  } finally {
    loading.value = false
  }
}

async function fetchDepartments() {
  try {
    const res = await getDepartments({ limit: 100 })
    departments.value = res.data?.data?.items || []
  } catch (error) {
    console.error("Failed to load departments:", error)
  }
}

function openAddHR() {
  selectedHR.value = null
  showHRForm.value = true
}

function openEditHR(hr) {
  selectedHR.value = hr
  showHRForm.value = true
}

function openDetailHR(hr) {
  selectedHR.value = hr
  showHRDetail.value = true
}

function closeHRDetail() {
  showHRDetail.value = false
}

async function handleSaveHR(payload, done) {
  try {
    if (selectedHR.value) {
      await updateUser(selectedHR.value.id, payload)
      ElMessage.success(t.value('hrManagement.messages.updateSuccess'))
    } else {
      await createUser(payload)
      ElMessage.success(t.value('hrManagement.messages.createSuccess'))
    }
    fetchHRs()
    showHRForm.value = false
  } catch (error) {
    console.error("Save error:", error)
  } finally {
    done?.()
  }
}

async function handleToggleStatus(hr) {
  const confirmMessage = hr.active 
    ? t.value('hrManagement.confirm.lockAccount').replace('{name}', hr.fullName)
    : t.value('hrManagement.confirm.unlockAccount').replace('{name}', hr.fullName)
  
  try {
    await ElMessageBox.confirm(
      confirmMessage,
      t.value('hrManagement.confirm.title'),
      { 
        confirmButtonText: t.value('hrManagement.confirm.ok'), 
        cancelButtonText: t.value('hrManagement.confirm.cancel'), 
        type: 'warning' 
      }
    )
    const res = await toggleUserStatus(hr.id)
    if (res.data && res.data.data) {
      const index = hrs.value.findIndex(h => h.id === hr.id)
      if (index !== -1) {
        hrs.value[index] = res.data.data
      }
    }
    ElMessage.success(t.value('hrManagement.messages.toggleSuccess'))
  } catch (error) {
    if (error !== 'cancel') {
      console.error("Toggle status error:", error)
    }
  }
}

function handlePageChange(page) {
  currentPage.value = page
  fetchHRs()
}

function handleSearch() {
  currentPage.value = 1
  fetchHRs()
}

watch([searchName, filterStatus, filterDepartment], () => {
  handleSearch()
})

onMounted(() => {
  fetchHRs()
  fetchDepartments()
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
            />
            <el-select 
              v-model="filterStatus" 
              :placeholder="t('hrManagement.allStatus')"
              clearable
              class="filter-select"
            >
              <el-option :label="t('hrManagement.allStatus')" value="" />
              <el-option :label="t('hrManagement.status.active')" value="ACTIVE" />
              <el-option :label="t('hrManagement.status.locked')" value="LOCKED" />
            </el-select>
            <el-select 
              v-model="filterDepartment" 
              :placeholder="t('hrManagement.allDepartments')"
              clearable
              class="filter-select"
              style="width: 200px;"
            >
              <el-option :label="t('hrManagement.allDepartments')" value="" />
              <el-option
                v-for="dept in departments"
                :key="dept.id"
                :label="dept.title"
                :value="dept.id"
              />
            </el-select>
          </div>
          
          <el-button 
            type="primary"
            :icon="Plus"
            @click="openAddHR"
          >
            {{ t('hrManagement.addNew') }}
          </el-button>
        </div>

        <el-table 
          :data="hrs" 
          stripe 
          style="width: 100%"
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

      <MentorFormDialog
        v-model:visible="showHRForm"
        :mentor="selectedHR"
        :departments="departments"
        user-type="HR"
        @save="handleSaveHR"
      />

      <el-dialog 
        v-model="showHRDetail" 
        :title="t('hrManagement.detail.title')"
        width="450px"
      >
        <el-descriptions :column="1" border>
          <el-descriptions-item :label="t('hrManagement.detail.hrName')">
            {{ selectedHR?.fullName }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('hrManagement.table.email')">
            {{ selectedHR?.email }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('hrManagement.detail.department')">
            <el-tag type="info" v-if="selectedHR?.department">
              {{ selectedHR.department.title }}
            </el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item :label="t('hrManagement.table.status')">
            <el-tag :type="selectedHR?.active ? 'success' : 'danger'">
              {{ selectedHR?.active ? t('hrManagement.status.active') : t('hrManagement.status.locked') }}
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
}

.search-group {
  display: flex;
  gap: 12px;
  flex: 1;
  max-width: 500px;
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
  justify-content: flex-end;
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
