<script setup>
import { ref, computed, onMounted, watch } from "vue"
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, OfficeBuilding, Plus, View, Edit, Lock, Unlock } from '@element-plus/icons-vue'
import { useLocaleStore } from '@/locales/locale'
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue"
import MentorFormDialog from "@/components/mentor/MentorFormDialog.vue"
import { getMentors, createMentor, updateMentor, toggleUserStatus } from '@/api/user'
import { getDepartments } from '@/api/department'

const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)

const mentors = ref([])
const departments = ref([])
const currentPage = ref(1)
const pageSize = 10
const totalItems = ref(0)
const searchName = ref("")
const filterStatus = ref("")
const filterDepartment = ref("")
const showMentorForm = ref(false)
const showMentorDetail = ref(false)
const selectedMentor = ref(null)
const loading = ref(false)

async function fetchMentors() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      limit: pageSize,
      keyword: searchName.value || undefined,
      department_id: filterDepartment.value || undefined,
      is_active: filterStatus.value === 'ACTIVE' ? true : filterStatus.value === 'LOCKED' ? false : undefined
    }
    const res = await getMentors(params)
    mentors.value = res.data?.data?.items || []
    totalItems.value = res.data?.data?.totalItems || 0
  } catch (error) {
    ElMessage.error(t.value('mentorManagement.messages.loadError'))
  } finally {
    loading.value = false
  }
}

async function fetchDepartments() {
  try {
    const res = await getDepartments({ limit: 100 })
    departments.value = res.data?.data?.items || []
    console.log(res.data.data.items)
  } catch (error) {
    console.error("Failed to load departments:", error)
  }
}

function openAddMentor() {
  selectedMentor.value = null
  showMentorForm.value = true
}

function openEditMentor(mentor) {
  selectedMentor.value = mentor
  showMentorForm.value = true
}

function openDetailMentor(mentor) {
  selectedMentor.value = mentor
  showMentorDetail.value = true
}

function closeMentorDetail() {
  showMentorDetail.value = false
}

async function handleSaveMentor(payload, done) {
  try {
    if (selectedMentor.value) {
      await updateMentor(selectedMentor.value.id, payload)
      ElMessage.success(t.value('mentorManagement.messages.updateSuccess'))
    } else {
      await createMentor(payload)
      ElMessage.success(t.value('mentorManagement.messages.createSuccess'))
    }
    fetchMentors()
    showMentorForm.value = false
  } catch (error) {
    console.error("Save error:", error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else if (error.response?.data) {
      ElMessage.error(t.value('mentorManagement.messages.saveError'))
    } else {
      ElMessage.error(t.value('mentorManagement.messages.saveError'))
    }
  } finally {
    done?.()
  }
}

async function handleToggleStatus(mentor) {
  const confirmMessage = mentor.active 
    ? t.value('mentorManagement.confirm.lockAccount').replace('{name}', mentor.fullName)
    : t.value('mentorManagement.confirm.unlockAccount').replace('{name}', mentor.fullName)
  
  try {
    await ElMessageBox.confirm(
      confirmMessage,
      t.value('mentorManagement.confirm.title'),
      { 
        confirmButtonText: t.value('mentorManagement.confirm.ok'), 
        cancelButtonText: t.value('mentorManagement.confirm.cancel'), 
        type: 'warning' 
      }
    )
    const res = await toggleUserStatus(mentor.id)
    if (res.data && res.data.data) {
      const index = mentors.value.findIndex(m => m.id === mentor.id)
      if (index !== -1) {
        mentors.value[index] = res.data.data
      }
    }
    ElMessage.success(t.value('mentorManagement.messages.toggleSuccess'))
  } catch (error) {
    if (error !== 'cancel') {
      console.error("Toggle status error:", error)
      ElMessage.error(t.value('mentorManagement.messages.toggleError'))
    }
  }
}

function handlePageChange(page) {
  currentPage.value = page
  fetchMentors()
}

function handleSearch() {
  currentPage.value = 1
  fetchMentors()
}

watch([searchName, filterStatus, filterDepartment], () => {
  handleSearch()
})

onMounted(() => {
  fetchMentors()
  fetchDepartments()
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
            />
            <el-select 
              v-model="filterStatus" 
              :placeholder="t('mentorManagement.allStatus')"
              clearable
              class="filter-select"
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
            >
              <el-option :label="t('mentorManagement.allDepartments')" value="" />
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
            @click="openAddMentor"
          >
            {{ t('mentorManagement.addNew') }}
          </el-button>
        </div>

        <el-table 
          :data="mentors" 
          stripe 
          style="width: 100%"
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
        v-model:visible="showMentorForm"
        :mentor="selectedMentor"
        :departments="departments"
        @save="handleSaveMentor"
      />

      <el-dialog 
        v-model="showMentorDetail" 
        :title="t('mentorManagement.detail.title')"
        width="450px"
      >
        <el-descriptions :column="1" border>
          <el-descriptions-item :label="t('mentorManagement.detail.mentorName')">
            {{ selectedMentor?.fullName }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('mentorManagement.table.email')">
            {{ selectedMentor?.email }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('mentorManagement.detail.department')">
            <el-tag type="info" v-if="selectedMentor?.department">
              {{ selectedMentor.department.title }}
            </el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item :label="t('mentorManagement.table.status')">
            <el-tag :type="selectedMentor?.active ? 'success' : 'danger'">
              {{ selectedMentor?.active ? t('mentorManagement.status.active') : t('mentorManagement.status.locked') }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="t('mentorManagement.detail.internCount')">
            {{ selectedMentor?.internCount || 0 }}
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
