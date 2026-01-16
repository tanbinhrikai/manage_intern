<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { useLocaleStore } from '@/locales/locale'
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue"
import { getEvaluationCriteria, deleteEvaluationCriteria } from '@/api/evaluation-criteria'
import { ElMessage, ElMessageBox } from 'element-plus'

const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)
const router = useRouter()

const criteriaList = ref([])
const loading = ref(false)

async function fetchCriteria() {
  loading.value = true
  try {
    const res = await getEvaluationCriteria()
    criteriaList.value = res.data?.data || []
  } catch (error) {
    ElMessage.error(t.value('evaluationCriteria.messages.loadError'))
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  router.push('/admin/evaluation-criteria/create')
}

function handleEdit(id) {
  router.push(`/admin/evaluation-criteria/${id}`)
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm(
      t.value('evaluationCriteria.messages.confirmDelete') || 'Are you sure?',
      'Warning',
      {
        confirmButtonText: 'OK',
        cancelButtonText: 'Cancel',
        type: 'warning',
      }
    )
    
    await deleteEvaluationCriteria(id)
    ElMessage.success(t.value('evaluationCriteria.messages.deleteSuccess'))
    fetchCriteria()
  } catch (error) {
    if (error !== 'cancel') {
        console.error(error)
        ElMessage.error(t.value('evaluationCriteria.messages.saveError'))
    }
  }
}

onMounted(fetchCriteria)
</script>

<template>
  <AdminLayout>
    <div class="evaluation-criteria-list">
      <el-card class="main-card" shadow="never">
        <template #header>
          <div class="card-header">
            <h2 class="page-title">{{ t('evaluationCriteria.title') }}</h2>
            <el-button type="primary" :icon="Plus" @click="handleAdd">
              {{ t('evaluationCriteria.addNew') }}
            </el-button>
          </div>
        </template>

        <el-table :data="criteriaList" v-loading="loading" stripe style="width: 100%">
          <el-table-column prop="id" :label="t('evaluationCriteria.table.id')" width="80" />
          <el-table-column prop="category" :label="t('evaluationCriteria.table.category')" width="150">
             <template #default="{ row }">
               <el-tag>{{ row.category }}</el-tag>
             </template>
          </el-table-column>
          <el-table-column prop="name" :label="t('evaluationCriteria.table.name')" min-width="200" />
          
          <el-table-column :label="t('evaluationCriteria.table.actions')" width="150" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" :icon="Edit" circle size="small" @click="handleEdit(row.id)" />
              <el-button type="danger" :icon="Delete" circle size="small" @click="handleDelete(row.id)" />
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </AdminLayout>
</template>

<style scoped>
.evaluation-criteria-list {
  max-width: 1200px;
  margin: 0 auto;
}

.main-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  margin: 0;
  color: #1f2937;
}

:deep(.el-card__header) {
  padding: 20px 24px;
}

:deep(.el-table th) {
  background-color: #f9fafb !important;
  color: #374151;
  font-weight: 600;
}
</style>
