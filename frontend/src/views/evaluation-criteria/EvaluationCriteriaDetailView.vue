<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useLocaleStore } from '@/locales/locale'
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue"
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getEvaluationCriteriaById, 
  createEvaluationCriteria, 
  updateEvaluationCriteria,
  getCriteriaGroups,
  getMainCriteria
} from '@/api/evaluation-criteria'
import EvaluationCriteriaScoreDefinitions from './EvaluationCriteriaScoreDefinitions.vue'
import { createEvaluationCriteriaCreationRequest, createEvaluationCriteriaUpdateRequest } from '@/types/evaluationCriteria'

const route = useRoute()
const router = useRouter()
const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)

const criteriaId = route.params.id
const isEdit = computed(() => !!criteriaId)
const loading = ref(false)
const saving = ref(false)

// Check if this is a parent criteria (no parentId)
const isParentCriteria = computed(() => isEdit.value && !formData.parentId)

// Check if creating a child (has parentId in query)
const isCreatingChild = computed(() => !isEdit.value && !!route.query.parentId)

// Check if creating a parent (no parentId in query)
const isCreatingParent = computed(() => !isEdit.value && !route.query.parentId)

// Check if editing a child criteria
const isChildCriteria = computed(() => isEdit.value && !!formData.parentId)



const formData = reactive(createEvaluationCriteriaCreationRequest())

// Dropdown options
const criteriaGroups = ref([])
const mainCriteriaList = ref([])

// Filtered main criteria based on selected group
const filteredMainCriteria = computed(() => {
  if (!formData.groupId) return []
  return mainCriteriaList.value.filter(c => c.groupId === formData.groupId)
})

async function loadDropdownData() {
  try {
    const [groupsRes, mainRes] = await Promise.all([
      getCriteriaGroups(),
      getMainCriteria()
    ])
    criteriaGroups.value = groupsRes.data?.data || []
    mainCriteriaList.value = mainRes.data?.data || []
  } catch (error) {
   
  }
}

async function fetchCriteria() {
  if (!isEdit.value) {
    const parentId = route.query.parentId ? parseInt(route.query.parentId) : null
    const groupId = route.query.groupId ? parseInt(route.query.groupId) : null
    Object.assign(formData, createEvaluationCriteriaCreationRequest(groupId, parentId))
    return
  }
  
  loading.value = true
  try {
    const res = await getEvaluationCriteriaById(criteriaId)
    const data = res.data?.data
    if (data) {
      Object.assign(formData, {
        ...createEvaluationCriteriaUpdateRequest(),
        groupId: data.groupId,
        name: data.name,
        description: data.description || '',
        weight: data.weight || 1.0,
        parentId: data.parentId || null,
        displayOrder: data.displayOrder || 1,
        isActive: data.isActive !== false
      })
    }
    
  } catch (error) {
    ElMessage.error(t.value('evaluationCriteria.messages.loadError'))
    router.push('/admin/evaluation-criteria')
  } finally {
    loading.value = false
  }
}

async function handleSaveCriteria() {
  console.log("parent", formData.parentId)
  if (!formData.groupId || !formData.name.trim()) {
    ElMessage.warning(t.value('evaluationCriteria.messages.validationError') || 'Please fill required fields')
    return
  }
  
  saving.value = true
  try {
    const payload = {
      groupId: formData.groupId,
      name: formData.name,
      description: formData.description,
      weight: formData.weight,
      parentId: formData.parentId,
      displayOrder: formData.displayOrder,
      isActive: formData.isActive
    }
    console.log("payload", payload)
    
    if (isEdit.value) {
      await updateEvaluationCriteria(criteriaId, payload)
      ElMessage.success(t.value('evaluationCriteria.messages.updateSuccess'))
      fetchCriteria()
    } else {
      const res = await createEvaluationCriteria(payload)
      ElMessage.success(t.value('evaluationCriteria.messages.createSuccess'))
      const newId = res.data?.data?.id
      if (newId) {
        router.push(`/admin/evaluation-criteria/${newId}`)
      } else {
        router.push('/admin/evaluation-criteria')
      }
    }
  } catch (error) {
    
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await loadDropdownData()
  await fetchCriteria()
})
</script>

<template>
  <AdminLayout>
    <div class="evaluation-detail-view" v-loading="loading">
      <div class="header-section">
        <h2 class="page-title">
          <template v-if="isEdit">
            {{ isParentCriteria ? t('evaluationCriteria.form.editParentTitle') || 'Edit Parent Criteria' : t('evaluationCriteria.form.editTitle') }}
          </template>
          <template v-else>
            {{ isCreatingChild ? t('evaluationCriteria.form.createChildTitle') || 'Create Child Criteria' : t('evaluationCriteria.form.createParentTitle') || 'Create Parent Criteria' }}
          </template>
        </h2>
        <el-button @click="router.push('/admin/evaluation-criteria')">
          {{ t('evaluationCriteria.form.cancel') }}
        </el-button>
      </div>

      <!-- Info alert for parent criteria -->
      <el-alert 
        v-if="isParentCriteria" 
        :title="t('evaluationCriteria.form.parentInfo') || 'This is a parent criteria. You can only edit basic info. Score definitions are managed on child criteria.'"
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 20px;"
      />

      <div class="content-grid">
        <!-- Main Form -->
        <el-card shadow="never" class="form-card">
          <template #header>
            <div class="card-title">{{ t('evaluationCriteria.form.basicInfo') || 'Basic Information' }}</div>
          </template>
          
          <el-form label-position="top" :model="formData">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item :label="t('evaluationCriteria.form.group')" required>
                  <el-select 
                    v-model="formData.groupId" 
                    :placeholder="t('evaluationCriteria.form.selectGroup')" 
                    :disabled="isCreatingChild || isChildCriteria"
                    style="width: 100%"
                    @change="formData.parentId = null"
                  >
                    <el-option 
                      v-for="group in criteriaGroups" 
                      :key="group.id" 
                      :label="group.name" 
                      :value="group.id" 
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <!-- Parent selection: only for child criteria (creating or editing) -->
                <el-form-item 
                  v-if="isCreatingChild || isChildCriteria" 
                  :label="t('evaluationCriteria.form.parentCriteria')"
                  required
                >
                  <el-select 
                    v-model="formData.parentId" 
                    :placeholder="t('evaluationCriteria.form.selectParent')" 
                    :disabled="isCreatingChild"
                    style="width: 100%"
                  >
                    <el-option 
                      v-for="main in filteredMainCriteria" 
                      :key="main.id" 
                      :label="main.name" 
                      :value="main.id" 
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item :label="t('evaluationCriteria.form.name')" required>
              <el-input v-model="formData.name" :placeholder="t('evaluationCriteria.form.namePlaceholder') || 'Enter criteria name'" />
            </el-form-item>
            
            <el-form-item :label="t('evaluationCriteria.form.description')">
              <el-input v-model="formData.description" type="textarea" :rows="3" />
            </el-form-item>

            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item :label="t('evaluationCriteria.form.weight')">
                  <el-input-number v-model="formData.weight" :min="0" :max="10" :precision="2" :step="0.1" style="width: 100%"/>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item :label="t('evaluationCriteria.form.displayOrder')">
                  <el-input-number v-model="formData.displayOrder" :min="1" :step="1" style="width: 100%"/>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item :label="t('evaluationCriteria.form.isActive')">
                  <el-switch v-model="formData.isActive" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <div class="form-actions">
              <el-button type="primary" @click="handleSaveCriteria" :loading="saving">
                {{ isEdit ? t('evaluationCriteria.form.save') : t('evaluationCriteria.form.create') }}
              </el-button>
            </div>
          </el-form>
        </el-card>

        <!-- Score Definitions: Only show for child criteria (has parentId) -->
        <EvaluationCriteriaScoreDefinitions 
          v-if="isEdit && !isParentCriteria" 
          :criteriaId="criteriaId" 
        />
      </div>
    </div>
  </AdminLayout>
</template>

<style scoped>
.evaluation-detail-view {
  max-width: 900px;
  margin: 0 auto;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.content-grid {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #374151;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}

:deep(.el-card) {
  border-radius: 12px;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  background: #f9fafb;
}
</style>
