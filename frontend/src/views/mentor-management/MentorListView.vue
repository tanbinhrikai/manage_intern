<script setup>
import { ref, computed, onMounted } from "vue"
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, OfficeBuilding, Plus, View, Edit, Lock, Unlock } from '@element-plus/icons-vue'
import { useLocaleStore } from '@/locales/locale'
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue"
import MentorFormDialog from "@/components/mentor/MentorFormDialog.vue"
import { getMentors, createMentor, updateMentor, toggleUserStatus } from '@/api/user'

const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)

const mentors = ref([])
const currentPage = ref(1)
const pageSize = 8
const searchName = ref("")
const searchTeam = ref("")
const showMentorForm = ref(false)
const showMentorDetail = ref(false)
const selectedMentor = ref(null)
const loading = ref(false)

const filteredMentors = computed(() => {
  let result = mentors.value
  if (searchName.value) {
    result = result.filter(m => m.fullName.toLowerCase().includes(searchName.value.toLowerCase()))
  }
  if (searchTeam.value) {
    result = result.filter(m => 
      m.department && 
      m.department.name.toLowerCase().includes(searchTeam.value.toLowerCase())
    )
  }
  return result.slice((currentPage.value - 1) * pageSize, currentPage.value * pageSize)
})

async function fetchMentors() {
  loading.value = true
  try {
    const res = await getMentors()
    mentors.value = res.data?.data?.items || []
  } catch (error) {
    ElMessage.error(t.value('mentorManagement.messages.loadError'))
  } finally {
    loading.value = false
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
    if (error.response && error.response.data) {
      ElMessage.error('Error: ' + JSON.stringify(error.response.data))
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

onMounted(fetchMentors)
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
            <el-input 
              v-model="searchTeam" 
              :placeholder="t('mentorManagement.searchByDepartment')"
              :prefix-icon="OfficeBuilding"
              clearable
              class="search-input"
            />
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
          :data="filteredMentors" 
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
                {{ scope.row.department.name }}
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
            :total="mentors.length"
            layout="prev, pager, next"
            background
          />
        </div>
      </el-card>

      <MentorFormDialog
        v-model:visible="showMentorForm"
        :mentor="selectedMentor"
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
              {{ selectedMentor.department.name }}
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
  width: 100%;
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
