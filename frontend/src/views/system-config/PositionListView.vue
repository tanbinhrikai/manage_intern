<script setup>
import { ref, computed, onMounted, watch } from "vue"
import { ElMessage } from 'element-plus'
import { Search, Plus, Edit } from '@element-plus/icons-vue'
import { useLocaleStore } from '@/locales/locale'
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue"
import { getPositions, createPosition, updatePosition } from '@/api/position'

const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)

const positions = ref([])
const searchName = ref("")
const currentPage = ref(1)
const pageSize = 10
const totalItems = ref(0)
const loading = ref(false)
const showForm = ref(false)
const selectedPosition = ref(null)
const formData = ref({ title: '' })
const saving = ref(false)

const isEdit = computed(() => !!selectedPosition.value)

async function fetchPositions() {
  loading.value = true
  try {
    const res = await getPositions({ 
      page: currentPage.value - 1, 
      limit: pageSize,
      keyword: searchName.value || undefined
    })
    positions.value = res.data?.data?.items || []
    totalItems.value = res.data?.data?.totalItems || 0
  } catch (error) {
    ElMessage.error(t.value('positionManagement.messages.loadError'))
  } finally {
    loading.value = false
  }
}

function openAdd() {
  selectedPosition.value = null
  formData.value = { title: '' }
  showForm.value = true
}

function openEdit(pos) {
  selectedPosition.value = pos
  formData.value = { title: pos.title || '' }
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  selectedPosition.value = null
  formData.value = { title: '' }
}

async function handleSave() {
  if (!formData.value.title.trim()) return
  
  saving.value = true
  try {
    if (isEdit.value) {
      await updatePosition(selectedPosition.value.id, formData.value)
      ElMessage.success(t.value('positionManagement.messages.updateSuccess'))
    } else {
      await createPosition(formData.value)
      ElMessage.success(t.value('positionManagement.messages.createSuccess'))
    }
    closeForm()
    fetchPositions()
  } catch (error) {
    console.error("Save error:", error)
    ElMessage.error(t.value('positionManagement.messages.saveError'))
  } finally {
    saving.value = false
  }
}

function handlePageChange(page) {
  currentPage.value = page
  fetchPositions()
}

function handleSearch() {
  currentPage.value = 1
  fetchPositions()
}

watch(searchName, () => {
  handleSearch()
})

onMounted(fetchPositions)
</script>

<template>
  <AdminLayout>
    <div class="position-list-view">
      <el-card class="main-card" shadow="never">
        <template #header>
          <div class="card-header">
            <h2 class="page-title">{{ t('positionManagement.title') }}</h2>
          </div>
        </template>

        <div class="toolbar">
          <el-input 
            v-model="searchName" 
            :placeholder="t('positionManagement.searchByName')"
            :prefix-icon="Search"
            clearable
            class="search-input"
          />
          
          <el-button 
            type="primary"
            :icon="Plus"
            @click="openAdd"
          >
            {{ t('positionManagement.addNew') }}
          </el-button>
        </div>

        <el-table 
          :data="positions" 
          stripe 
          style="width: 100%"
          v-loading="loading"
        >
          <el-table-column 
            prop="id" 
            :label="t('positionManagement.table.id')" 
            width="100" 
          />
          <el-table-column 
            prop="title" 
            :label="t('positionManagement.table.title')" 
            min-width="200" 
          />
          <el-table-column 
            :label="t('positionManagement.table.actions')" 
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

        <div class="pagination-wrapper" v-if="totalItems > pageSize">
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

      <el-dialog 
        v-model="showForm" 
        :title="isEdit ? t('positionManagement.form.editTitle') : t('positionManagement.form.addTitle')"
        width="400px"
        :close-on-click-modal="false"
      >
        <el-form label-position="top">
          <el-form-item :label="t('positionManagement.form.title')">
            <el-input 
              v-model="formData.title" 
              :placeholder="t('positionManagement.form.titlePlaceholder')"
            />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="closeForm">
            {{ t('positionManagement.form.cancel') }}
          </el-button>
          <el-button type="primary" @click="handleSave" :loading="saving" :disabled="!formData.title.trim()">
            {{ isEdit ? t('positionManagement.form.save') : t('positionManagement.form.create') }}
          </el-button>
        </template>
      </el-dialog>
    </div>
  </AdminLayout>
</template>

<style scoped>
.position-list-view {
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
