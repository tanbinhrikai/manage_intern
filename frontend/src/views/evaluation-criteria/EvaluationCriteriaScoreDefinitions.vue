<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useLocaleStore } from '@/locales/locale'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { 
  getScoreLabels,
  getScoreDefinitionsByCriteriaId,
  createScoreDefinition,
  updateScoreDefinition,
  deleteScoreDefinition
} from '@/api/evaluation-criteria'
import { createCriteriaScoreDefinitionCreationRequest, createCriteriaScoreDefinitionUpdateRequest } from '@/types/evaluationCriteria'

const props = defineProps({
  criteriaId: {
    type: [Number, String],
    required: true
  }
})

const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)
const loading = ref(false)
const scoreLabels = ref([])
const scoreDefinitions = ref([])

// Score Definition Dialog
const showScoreDialog = ref(false)
const scoreForm = reactive({
  id: null,
  ...createCriteriaScoreDefinitionCreationRequest(null)
})
const isScoreEdit = computed(() => !!scoreForm.id)
const savingScore = ref(false)

// Filter out score labels that are already used
const availableScoreLabels = computed(() => {
  const usedLabels = scoreDefinitions.value.map(d => d.scoreLabel)
  return scoreLabels.value.filter(label => {
    // If editing, allow the current label
    if (isScoreEdit.value && label.value === scoreForm.scoreLabel) {
      return true
    }
    // Otherwise, only show labels not yet used
    return !usedLabels.includes(label.value)
  })
})

// Check if all labels are used
const allLabelsUsed = computed(() => {
  return scoreDefinitions.value.length >= scoreLabels.value.length
})

function getScoreRange(labelValue) {
  const label = scoreLabels.value.find(l => l.value === labelValue)
  return label ? `${label.minScore}-${label.maxScore}` : '-'
}

async function loadDropdownData() {
  try {
    const res = await getScoreLabels()
    scoreLabels.value = res.data?.data || []
  } catch (error) {
    console.error('Failed to load score labels:', error)
  }
}

async function fetchScoreDefinitions() {
  if (!props.criteriaId) return
  
  loading.value = true
  try {
    const res = await getScoreDefinitionsByCriteriaId(props.criteriaId)
    scoreDefinitions.value = res.data?.data || []
  } catch (error) {
    ElMessage.error(t.value('evaluationCriteria.messages.loadError'))
  } finally {
    loading.value = false
  }
}

function openAddScore() {
  const firstAvailable = availableScoreLabels.value[0]?.value || 'AVERAGE'
  Object.assign(scoreForm, {
    id: null,
    ...createCriteriaScoreDefinitionCreationRequest(parseInt(props.criteriaId)),
    scoreLabel: firstAvailable
  })
  showScoreDialog.value = true
}

function openEditScore(def) {
  Object.assign(scoreForm, {
    id: def.id,
    ...createCriteriaScoreDefinitionUpdateRequest(def.criteriaId),
    scoreLabel: def.scoreLabel,
    description: def.description || ''
  })
  showScoreDialog.value = true
}

async function handleSaveScore() {
  savingScore.value = true
  try {
    const payload = {
      criteriaId: scoreForm.criteriaId,
      scoreLabel: scoreForm.scoreLabel,
      description: scoreForm.description
    }
    
    if (isScoreEdit.value) {
      await updateScoreDefinition(scoreForm.id, payload)
      ElMessage.success(t.value('evaluationCriteria.scoreDefinitions.messages.updateSuccess'))
    } else {
      await createScoreDefinition(payload)
      ElMessage.success(t.value('evaluationCriteria.scoreDefinitions.messages.createSuccess'))
    }
    showScoreDialog.value = false
    fetchScoreDefinitions()
  } catch (error) {
    //console.error(error)
  } finally {
    savingScore.value = false
  }
}

async function handleDeleteScore(id) {
  try {
    await ElMessageBox.confirm(
      t.value('evaluationCriteria.messages.confirmDelete') || 'Delete this definition?', 
      'Warning', 
      { type: 'warning' }
    )
    await deleteScoreDefinition(id)
    ElMessage.success(t.value('evaluationCriteria.scoreDefinitions.messages.deleteSuccess'))
    fetchScoreDefinitions()
  } catch (e) {
   // console.error(e)
  }
}

watch(() => props.criteriaId, (newVal) => {
  if (newVal) {
    fetchScoreDefinitions()
  }
})

onMounted(async () => {
  await loadDropdownData()
  await fetchScoreDefinitions()
})
</script>

<template>
  <div v-loading="loading">
    <el-card shadow="never" class="definitions-card">
      <template #header>
        <div class="card-header">
          <div class="card-title">{{ t('evaluationCriteria.scoreDefinitions.title') }}</div>
          <el-button 
            type="primary" 
            size="small" 
            :icon="Plus" 
            :disabled="allLabelsUsed"
            @click="openAddScore"
          >
            {{ t('evaluationCriteria.scoreDefinitions.add') }}
          </el-button>
        </div>
      </template>

      <el-table :data="scoreDefinitions" stripe>
        <el-table-column prop="scoreLabel" :label="t('evaluationCriteria.scoreDefinitions.scoreLabel')" width="140">
          <template #default="{ row }">
            <el-tag :type="row.scoreLabel === 'EXCELLENT' ? 'success' : row.scoreLabel === 'GOOD' ? 'primary' : row.scoreLabel === 'AVERAGE' ? 'warning' : 'danger'">
              {{ row.scoreLabel }}
            </el-tag>
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

    <!-- Score Definition Dialog -->
    <el-dialog 
      v-model="showScoreDialog" 
      :title="isScoreEdit ? t('evaluationCriteria.scoreDefinitions.form.editTitle') : t('evaluationCriteria.scoreDefinitions.form.addTitle')" 
      width="500px"
    >
      <el-form label-position="top" :model="scoreForm">
        <el-form-item :label="t('evaluationCriteria.scoreDefinitions.scoreLabel')" required>
          <el-select v-model="scoreForm.scoreLabel" style="width: 100%">
            <el-option 
              v-for="label in availableScoreLabels" 
              :key="label.value" 
              :label="`${label.value} (${label.minScore}-${label.maxScore})`" 
              :value="label.value"
            />
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
</template>

<style scoped>
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

:deep(.el-card) {
  border-radius: 12px;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  background: #f9fafb;
}
</style>
