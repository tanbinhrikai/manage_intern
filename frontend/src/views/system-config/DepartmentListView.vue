<script setup>
import { ref, computed, onMounted } from "vue"
import { ElMessage } from 'element-plus'
import { Search, Plus, Edit } from '@element-plus/icons-vue'
import { useLocaleStore } from '@/locales/locale'
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue"
import { getDepartments, createDepartment, updateDepartment } from '@/api/department'

const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)

const departments = ref([])
const searchName = ref("")
const loading = ref(false)
const showForm = ref(false)
const selectedDepartment = ref(null)
const formData = ref({ title: '' })
const saving = ref(false)

const isEdit = computed(() => !!selectedDepartment.value)

const filteredDepartments = computed(() => {
  if (!searchName.value) return departments.value
  return departments.value.filter(d => 
    d.title?.toLowerCase().includes(searchName.value.toLowerCase())
  )
})

async function fetchDepartments() {
  loading.value = true
  try {
    const res = await getDepartments()
    departments.value = res.data?.data || []
  } catch (error) {
    ElMessage.error(t.value('departmentManagement.messages.loadError'))
  } finally {
    loading.value = false
  }
}

function openAdd() {
  selectedDepartment.value = null
  formData.value = { title: '' }
  showForm.value = true
}

function openEdit(dept) {
  selectedDepartment.value = dept
  formData.value = { title: dept.title || '' }
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  selectedDepartment.value = null
  formData.value = { title: '' }
}

async function handleSave() {
  if (!formData.value.title.trim()) return
  
  saving.value = true
  try {
    if (isEdit.value) {
      await updateDepartment(selectedDepartment.value.id, formData.value)
      ElMessage.success(t.value('departmentManagement.messages.updateSuccess'))
    } else {
      await createDepartment(formData.value)
      ElMessage.success(t.value('departmentManagement.messages.createSuccess'))
    }
    closeForm()
    fetchDepartments()
  } catch (error) {
    console.error("Save error:", error)
    ElMessage.error(t.value('departmentManagement.messages.saveError'))
  } finally {
    saving.value = false
  }
}

onMounted(fetchDepartments)
</script>

<template>
  <AdminLayout>
    <div class="department-list-view">
      <el-card class="main-card" shadow="never">
        <template #header>
          <div class="card-header">
            <h2 class="page-title">{{ t('departmentManagement.title') }}</h2>
          </div>
        </template>

        <div class="toolbar">
          <el-input 
            v-model="searchName" 
            :placeholder="t('departmentManagement.searchByName')"
            :prefix-icon="Search"
            clearable
            class="search-input"
          />
          
          <el-button 
            type="primary"
            :icon="Plus"
            @click="openAdd"
          >
            {{ t('departmentManagement.addNew') }}
          </el-button>
        </div>

        <el-table 
          :data="filteredDepartments" 
          stripe 
          style="width: 100%"
          v-loading="loading"
        >
          <el-table-column 
            prop="id" 
            :label="t('departmentManagement.table.id')" 
            width="100" 
          />
          <el-table-column 
            prop="title" 
            :label="t('departmentManagement.table.name')" 
            min-width="200" 
          />
          <el-table-column 
            :label="t('departmentManagement.table.actions')" 
            width="120" 
            fixed="right" 
            align="center"
          >
            <template #default="scope">
              <el-button 
                type="warning" 
                :icon="Edit" 
                size="small" 
                circle
                @click="openEdit(scope.row)"
              />
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-dialog 
        v-model="showForm" 
        :title="isEdit ? t('departmentManagement.form.editTitle') : t('departmentManagement.form.addTitle')"
        width="400px"
        :close-on-click-modal="false"
      >
        <el-form label-position="top">
          <el-form-item :label="t('departmentManagement.form.name')">
            <el-input 
              v-model="formData.title" 
              :placeholder="t('departmentManagement.form.namePlaceholder')"
            />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="closeForm">
            {{ t('departmentManagement.form.cancel') }}
          </el-button>
          <el-button type="primary" @click="handleSave" :loading="saving" :disabled="!formData.title.trim()">
            {{ isEdit ? t('departmentManagement.form.save') : t('departmentManagement.form.create') }}
          </el-button>
        </template>
      </el-dialog>
    </div>
  </AdminLayout>
</template>

<style scoped>
.department-list-view {
  max-width: 900px;
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

.search-input {
  max-width: 300px;
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
