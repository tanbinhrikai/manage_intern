<script setup>
import { ref, computed, onMounted, onBeforeUnmount, onDeactivated, watch } from "vue"
import { onBeforeRouteLeave } from "vue-router"
import { Search, Plus, Edit } from '@element-plus/icons-vue'
import { useLocaleStore } from '@/locales/locale'
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue"
import { getDepartments, createDepartment, updateDepartment } from '@/api/department'
import { usePagination, useLoading, useApi, useDialog, useConfirm } from '@/composables'
import { createDepartmentCreationRequest, createDepartmentUpdateRequest } from '@/types/department'

const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)

// Composables
const pagination = usePagination({
  initialPage: 1,
  initialPageSize: 10,
  onPageChange: () => fetchDepartments()
})

const { loading, withLoading, setLoadingState, isLoading } = useLoading()
const { execute: executeApi } = useApi({
  showErrorMessage: true,
  showSuccessMessage: true
})
const { confirmUpdate } = useConfirm()

const departments = ref([])
const searchName = ref("")
const formDialog = useDialog()
const formData = ref(createDepartmentCreationRequest())

/** Check if form is in edit mode */
const isEdit = computed(() => formDialog.isEdit)

/**
 * Fetch departments from backend with current filters and pagination
 */
async function fetchDepartments() {
  await withLoading(async () => {
    const params = {
      ...pagination.apiParams.value,
      keyword: searchName.value || undefined
    }
    
    const res = await executeApi(
      () => getDepartments(params),
      null,
      'departmentManagement.messages.loadError'
    )
    
    departments.value = res.data?.data?.items || []
    pagination.setTotalItems(res.data?.data?.totalItems || 0)
  })
}

/**
 * Open form dialog for creating a new department
 */
function openAdd() {
  formData.value = createDepartmentCreationRequest()
  formDialog.open()
}

/**
 * Open form dialog for editing an existing department
 * @param {Department} dept - Department to edit
 */
function openEdit(dept) {
  formData.value = {
    ...createDepartmentUpdateRequest(),
    title: dept.title || ''
  }
  formDialog.open(dept)
}

/**
 * Close form dialog and reset form data
 */
function closeForm() {
  formData.value = createDepartmentCreationRequest()
  formDialog.close()
}

/**
 * Handle save department (create or update)
 */
async function handleSave() {
  if (!formData.value.title.trim()) return
  
  setLoadingState('saving', true)
  try {
    if (isEdit.value) {
      await executeApi(
        () => updateDepartment(formDialog.selectedItem.id, formData.value),
        'departmentManagement.messages.updateSuccess',
        'departmentManagement.messages.saveError'
      )
    } else {
      await executeApi(
        () => createDepartment(formData.value),
        'departmentManagement.messages.createSuccess',
        'departmentManagement.messages.saveError'
      )
    }
    closeForm()
    fetchDepartments()
  } finally {
    setLoadingState('saving', false)
  }
}

/**
 * Handle save with confirmation
 */
function handleSaveWithConfirm() {
  if (!formData.value.title.trim()) {
    return
  }
  
  const message = isEdit.value 
    ? (t.value('departmentManagement.confirm.update') || 'Are you sure you want to update this department?')
    : (t.value('departmentManagement.confirm.create') || 'Are you sure you want to create this department?')
  
  confirmUpdate({
    message,
    onConfirm: handleSave
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
  fetchDepartments()
}

watch(searchName, () => {
  handleSearch()
})

onMounted(fetchDepartments)

onBeforeUnmount(() => {
  formDialog.reset()
})

onDeactivated(() => {
  formDialog.reset()
})

onBeforeRouteLeave(() => {
  formDialog.reset()
})
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
          :data="departments" 
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

        <div class="pagination-wrapper" v-if="pagination.totalItems.value > pagination.pageSize.value">
          <el-pagination
            :current-page="pagination.currentPage.value"
            :page-size="pagination.pageSize.value"
            :total="pagination.totalItems.value"
            layout="prev, pager, next"
            background
            @current-change="handlePageChange"
          />
        </div>
      </el-card>

      <el-dialog 
        v-model="formDialog.visible" 
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
          <el-button type="primary" @click="handleSaveWithConfirm" :loading="isLoading('saving')" :disabled="!formData.title.trim()">
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
