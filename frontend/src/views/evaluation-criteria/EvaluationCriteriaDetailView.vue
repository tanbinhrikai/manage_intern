<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useLocaleStore } from '@/locales/locale'
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue"
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { 
  getEvaluationCriteriaById, 
  createEvaluationCriteria, 
  updateEvaluationCriteria,
  getScoreDefinitionsByCriteriaId,
  createScoreDefinition,
  updateScoreDefinition,
  deleteScoreDefinition
} from '@/api/evaluation-criteria'

const route = useRoute()
const router = useRouter()
const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)

const criteriaId = route.params.id
const isEdit = computed(() => !!criteriaId)
const loading = ref(false)
const saving = ref(false)

const formData = reactive({
  category: 'SKILLS',
  name: '',
  description: '',
  weight: 0,

  scoreDefinitions: [] 
})

const categories = ['EXPERTISE', 'MINDSET', 'SKILLS']

const showScoreDialog = ref(false)
const scoreForm = reactive({
  id: null,
  criteriaId: null,
  scoreLabel: 'Average',
  description: ''
})
const scoreLabels = ['Excellent', 'Good', 'Average', 'Weak']
const isScoreEdit = computed(() => !!scoreForm.id)
const savingScore = ref(false)

// Fixed score ranges based on ScoreLabel enum
const scoreLabelRanges = {
  Excellent: { min: 9, max: 10 },
  Good: { min: 7, max: 8 },
  Average: { min: 5, max: 6 },
  Weak: { min: 0, max: 4 }
}

const getScoreRange = (label) => {
  const range = scoreLabelRanges[label]
  return range ? `${range.min}-${range.max}` : '-'
}

async function fetchCriteria() {
  if (!isEdit.value) return
  loading.value = true
  try {
    const res = await getEvaluationCriteriaById(criteriaId)
    const data = res.data?.data
    Object.assign(formData, data)
    
  
    const scoreRes = await getScoreDefinitionsByCriteriaId(criteriaId)
    formData.scoreDefinitions = scoreRes.data?.data || []
  } catch (error) {
    ElMessage.error(t.value('evaluationCriteria.messages.loadError'))
    router.push('/admin/evaluation-criteria')
  } finally {
    loading.value = false
  }
}

async function handleSaveCriteria() {
  saving.value = true
  try {
    if (isEdit.value) {
      await updateEvaluationCriteria(criteriaId, formData)
      ElMessage.success(t.value('evaluationCriteria.messages.updateSuccess'))
      fetchCriteria() 
    } else {
      const res = await createEvaluationCriteria(formData)
      ElMessage.success(t.value('evaluationCriteria.messages.createSuccess'))
      const newId = res.data?.data?.id
      if (newId) {
        router.push(`/admin/evaluation-criteria/${newId}`)
      } else {
        router.push('/admin/evaluation-criteria')
      }
    }
  } catch (error) {
    ElMessage.error(t.value('evaluationCriteria.messages.saveError'))
  } finally {
    saving.value = false
  }
}

function openAddScore() {
  Object.assign(scoreForm, {
    id: null,
    criteriaId: parseInt(criteriaId),
    scoreLabel: 'Average',
    description: ''
  })
  showScoreDialog.value = true
}

function openEditScore(def) {
  Object.assign(scoreForm, { ...def })
  showScoreDialog.value = true
}

async function handleSaveScore() {
  savingScore.value = true
  try {
    if (isScoreEdit.value) {
      await updateScoreDefinition(scoreForm.id, scoreForm)
      ElMessage.success(t.value('evaluationCriteria.scoreDefinitions.messages.updateSuccess'))
    } else {
      await createScoreDefinition(scoreForm)
      ElMessage.success(t.value('evaluationCriteria.scoreDefinitions.messages.createSuccess'))
    }
    showScoreDialog.value = false
    fetchCriteria()
  } catch (error) {
     console.error(error)
     ElMessage.error(t.value('evaluationCriteria.messages.saveError'))
  } finally {
    savingScore.value = false
  }
}

async function handleDeleteScore(id) {
    try {
        await ElMessageBox.confirm(t.value('evaluationCriteria.messages.confirmDelete') || 'Delete this definition?', 'Warning', {
             type: 'warning'
        })
        await deleteScoreDefinition(id)
        ElMessage.success(t.value('evaluationCriteria.scoreDefinitions.messages.deleteSuccess'))
        fetchCriteria()
    } catch (e) {
      
    }
}

onMounted(fetchCriteria)
</script>

<template>
  <AdminLayout>
    <div class="evaluation-detail-view" v-loading="loading">
      <div class="header-section">
        <h2 class="page-title">
          {{ isEdit ? t('evaluationCriteria.form.editTitle') : t('evaluationCriteria.form.createTitle') }}
        </h2>
        <el-button @click="router.push('/admin/evaluation-criteria')">
           {{ t('evaluationCriteria.form.cancel') }}
        </el-button>
      </div>

      <div class="content-grid">
        <!-- Main Form -->
        <el-card shadow="never" class="form-card">
          <template #header>
            <div class="card-title">Basic Information</div>
          </template>
          
          <el-form label-position="top" :model="formData">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item :label="t('evaluationCriteria.form.name')" required>
                  <el-input v-model="formData.name" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="t('evaluationCriteria.form.category')" required>
                  <el-select v-model="formData.category" style="width: 100%">
                    <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item :label="t('evaluationCriteria.form.description')">
              <el-input v-model="formData.description" type="textarea" :rows="3" />
            </el-form-item>

            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item :label="t('evaluationCriteria.form.weight')">
                  <el-input-number v-model="formData.weight" :min="0" :precision="2" :step="0.1" style="width: 100%"/>
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

        <!-- Score Definitions (Only visible in Edit Mode) -->
        <el-card v-if="isEdit" shadow="never" class="definitions-card">
           <template #header>
            <div class="card-header">
              <div class="card-title">{{ t('evaluationCriteria.scoreDefinitions.title') }}</div>
              <el-button type="primary" size="small" :icon="Plus" @click="openAddScore">
                {{ t('evaluationCriteria.scoreDefinitions.add') }}
              </el-button>
            </div>
          </template>

          <el-table :data="formData.scoreDefinitions" stripe>
             <el-table-column prop="scoreLabel" :label="t('evaluationCriteria.scoreDefinitions.scoreLabel')" width="120">
               <template #default="{ row }">
                 <el-tag>{{ row.scoreLabel }}</el-tag>
               </template>
             </el-table-column>
             <el-table-column :label="t('evaluationCriteria.scoreDefinitions.scoreRange')" width="120" align="center">
                <template #default="{ row }">{{ getScoreRange(row.scoreLabel) }}</template>
             </el-table-column>
             <el-table-column prop="description" :label="t('evaluationCriteria.scoreDefinitions.description')" min-width="200" show-overflow-tooltip/>
             
             <el-table-column :label="t('evaluationCriteria.scoreDefinitions.actions')" width="120" fixed="right">
                <template #default="{ row }">
                   <el-button :icon="Edit" circle size="small" @click="openEditScore(row)"/>
                   <el-button type="danger" :icon="Delete" circle size="small" @click="handleDeleteScore(row.id)"/>
                </template>
             </el-table-column>
          </el-table>
        </el-card>
      </div>

      <!-- Score Definition Dialog -->
      <el-dialog v-model="showScoreDialog" :title="isScoreEdit ? t('evaluationCriteria.scoreDefinitions.form.editTitle') : t('evaluationCriteria.scoreDefinitions.form.addTitle')" width="500px">
         <el-form label-position="top" :model="scoreForm">
            <el-form-item :label="t('evaluationCriteria.scoreDefinitions.scoreLabel')" required>
               <el-select v-model="scoreForm.scoreLabel" style="width: 100%">
                 <el-option v-for="label in scoreLabels" :key="label" :label="`${label} (${getScoreRange(label)})`" :value="label"/>
               </el-select>
            </el-form-item>
             <el-form-item :label="t('evaluationCriteria.scoreDefinitions.description')">
               <el-input v-model="scoreForm.description" type="textarea" :rows="3"/>
            </el-form-item>
         </el-form>
         <template #footer>
            <el-button @click="showScoreDialog = false">{{ t('evaluationCriteria.scoreDefinitions.form.cancel') }}</el-button>
            <el-button type="primary" @click="handleSaveScore" :loading="savingScore">{{ t('evaluationCriteria.scoreDefinitions.form.save') }}</el-button>
         </template>
      </el-dialog>
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
</style>
