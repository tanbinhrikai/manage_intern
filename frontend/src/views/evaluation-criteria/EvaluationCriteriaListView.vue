<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Edit, Delete, FolderAdd } from '@element-plus/icons-vue'
import { useLocaleStore } from '@/locales/locale'
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue"
import { 
  getEvaluationCriteria, 
  deleteEvaluationCriteria,
  createCriteriaGroup,
  updateCriteriaGroup,
  deleteCriteriaGroup
} from '@/api/evaluation-criteria'
import { ElMessage, ElMessageBox } from 'element-plus'

const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)
const router = useRouter()

const loading = ref(false)
const treeData = ref([])

// Group dialog state
const groupDialogVisible = ref(false)
const groupDialogMode = ref('create') // 'create' or 'edit'
const groupSaving = ref(false)
const groupForm = reactive({
  id: null,
  name: '',
  displayOrder: 1
})

function transformToTreeData(groups) {
  return groups.map(group => ({
    id: `group-${group.id}`,
    rawId: group.id,
    label: group.name,
    displayOrder: group.displayOrder,
    isGroup: true,
    children: (group.mainCriteria || []).map(main => ({
      id: main.id,
      label: main.name,
      displayOrder: main.displayOrder,
      isActive: main.isActive,
      groupName: group.name,
      groupId: group.id,
      isParent: true,
      children: (main.children || []).map(sub => ({
        id: sub.id,
        label: sub.name,
        displayOrder: sub.displayOrder,
        isActive: sub.isActive,
        groupName: group.name,
        groupId: group.id,
        parentId: main.id,
        parentName: main.name,
        isParent: false 
      }))
    }))
  }))
}

const defaultProps = {
  children: 'children',
  label: 'label'
}

async function fetchCriteria() {
  loading.value = true
  try {
    const res = await getEvaluationCriteria()
    const groups = res.data?.data || []
    treeData.value = transformToTreeData(groups)
  } catch (error) {
    ElMessage.error(t.value('evaluationCriteria.messages.loadError'))
  } finally {
    loading.value = false
  }
}

// ========== Criteria Handlers ==========
function handleAddCriteria(data) {
  console.log(data)
  if(data.isGroup) {
    router.push(`/admin/evaluation-criteria/create?groupId=${data.rawId}`)
    return
  }
  router.push('/admin/evaluation-criteria/create')
}

function handleAddChild(parentData) {
  router.push(`/admin/evaluation-criteria/create?parentId=${parentData.id}&groupId=${parentData.groupId}`)
}

function handleEdit(node, data) {
  if (data.isGroup) {
    openGroupDialog('edit', data)
    return
  }
  router.push(`/admin/evaluation-criteria/${data.id}`)
}

async function handleDelete(node, data) {
  if (data.isGroup) {
    await handleDeleteGroup(data)
    return
  }
  try {
    await ElMessageBox.confirm(
      t.value('evaluationCriteria.messages.confirmDelete') || `Delete "${data.label}"?`,
      'Warning',
      { confirmButtonText: 'OK', cancelButtonText: 'Cancel', type: 'warning' }
    )
    await deleteEvaluationCriteria(data.id)
    ElMessage.success(t.value('evaluationCriteria.messages.deleteSuccess'))
    fetchCriteria()
  } catch (error) {
    if (error !== 'cancel') {
      //console.error(error)
    }
  }
}

// ========== Group Handlers ==========
function openGroupDialog(mode, data = null) {
  groupDialogMode.value = mode
  if (mode === 'edit' && data) {
    groupForm.id = data.rawId
    groupForm.name = data.label
    groupForm.displayOrder = data.displayOrder || 1
  } else {
    groupForm.id = null
    groupForm.name = ''
    groupForm.displayOrder = treeData.value.length + 1
  }
  groupDialogVisible.value = true
}

async function handleSaveGroup() {
  if (!groupForm.name.trim()) {
    ElMessage.warning(t.value('evaluationCriteria.group.validationError') || 'Please enter group name')
    return
  }
  
  groupSaving.value = true
  try {
    const payload = {
      name: groupForm.name,
      displayOrder: groupForm.displayOrder
    }
    
    if (groupDialogMode.value === 'edit') {
      await updateCriteriaGroup(groupForm.id, payload)
      ElMessage.success(t.value('evaluationCriteria.group.updateSuccess') || 'Group updated successfully!')
    } else {
      await createCriteriaGroup(payload)
      ElMessage.success(t.value('evaluationCriteria.group.createSuccess') || 'Group created successfully!')
    }
    
    groupDialogVisible.value = false
    fetchCriteria()
  } catch (error) {
    // Error handled by global handler
  } finally {
    groupSaving.value = false
  }
}

async function handleDeleteGroup(data) {
  try {
    await ElMessageBox.confirm(
      t.value('evaluationCriteria.group.confirmDelete') || `Delete group "${data.label}" and all its criteria?`,
      'Warning',
      { confirmButtonText: 'OK', cancelButtonText: 'Cancel', type: 'warning' }
    )
    await deleteCriteriaGroup(data.rawId)
    ElMessage.success(t.value('evaluationCriteria.group.deleteSuccess') || 'Group deleted successfully!')
    fetchCriteria()
  } catch (error) {
    if (error !== 'cancel') {
      //console.error(error)
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
            <div class="header-actions">
              <el-button type="success" :icon="FolderAdd" @click="openGroupDialog('create')">
                {{ t('evaluationCriteria.group.addNew') || 'Add Group' }}
              </el-button>
              <el-button type="primary" :icon="Plus" @click="handleAddCriteria">
                {{ t('evaluationCriteria.addNew') }}
              </el-button>
            </div>
          </div>
        </template>

        <el-tree
          v-loading="loading"
          :data="treeData"
          :props="defaultProps"
          default-expand-all
          node-key="id"
          class="criteria-tree"
        >
          <template #default="{ node, data }">
            <div class="tree-node">
              <div class="node-content">
                <span class="node-label" :class="{ 'group-label': data.isGroup, 'parent-label': data.isParent && !data.isGroup }">
                  {{ node.label }}
                </span>
                <template v-if="data.isGroup">
                  <el-tag size="small" type="info">{{ t('evaluationCriteria.group.tag') || 'Group' }}</el-tag>
                </template>
                <template v-else>
                  <el-tag size="small" :type="data.isActive ? 'success' : 'info'">
                    {{ data.isActive ? t('evaluationCriteria.table.active') : t('evaluationCriteria.table.inactive') }}
                  </el-tag>
                  <el-tag v-if="data.isParent" size="small" type="warning">Parent</el-tag>
                </template>
              </div>
              <div class="node-actions">
                <!-- Group actions -->
                <template v-if="data.isGroup">
                  <el-button type="success" :icon="Plus" circle size="small" @click.stop="handleAddCriteria(data)" />
                  <el-button type="primary" :icon="Edit" circle size="small" @click.stop="handleEdit(node, data)" />
                  <el-button type="danger" :icon="Delete" circle size="small" @click.stop="handleDelete(node, data)" />
                </template>
                <!-- Criteria actions -->
                <template v-else>
                  <el-button 
                    v-if="data.isParent" 
                    type="success" 
                    :icon="Plus" 
                    circle 
                    size="small" 
                    @click.stop="handleAddChild(data)"
                    :title="t('evaluationCriteria.addChild') || 'Add Child'"
                  />
                  <el-button type="primary" :icon="Edit" circle size="small" @click.stop="handleEdit(node, data)" />
                  <el-button type="danger" :icon="Delete" circle size="small" @click.stop="handleDelete(node, data)" />
                </template>
              </div>
            </div>
          </template>
        </el-tree>
      </el-card>
    </div>

    <!-- Group Dialog -->
    <el-dialog
      v-model="groupDialogVisible"
      :title="groupDialogMode === 'edit' ? (t('evaluationCriteria.group.editTitle') || 'Edit Group') : (t('evaluationCriteria.group.addTitle') || 'Add Group')"
      width="500"
      destroy-on-close
    >
      <el-form :model="groupForm" label-position="top">
        <el-form-item :label="t('evaluationCriteria.group.name') || 'Group Name'" required>
          <el-input v-model="groupForm.name" :placeholder="t('evaluationCriteria.group.namePlaceholder') || 'Enter group name'" />
        </el-form-item>
        <el-form-item :label="t('evaluationCriteria.group.displayOrder') || 'Display Order'">
          <el-input-number v-model="groupForm.displayOrder" :min="1" :step="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="groupDialogVisible = false">{{ t('evaluationCriteria.form.cancel') }}</el-button>
          <el-button type="primary" :loading="groupSaving" @click="handleSaveGroup">
            {{ groupDialogMode === 'edit' ? t('evaluationCriteria.form.save') : (t('evaluationCriteria.group.create') || 'Create') }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </AdminLayout>
</template>

<style scoped>
.evaluation-criteria-list {
  max-width: 1000px;
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

.header-actions {
  display: flex;
  gap: 12px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  margin: 0;
  color: #1f2937;
}

.criteria-tree {
  background: transparent;
}

:deep(.el-tree-node__content) {
  height: auto;
  padding: 8px 0;
}

.tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding-right: 16px;
}

.node-content {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.node-label {
  font-size: 14px;
  color: #374151;
}

.group-label {
  font-weight: 600;
  font-size: 15px;
  color: #1f2937;
}

.parent-label {
  font-weight: 500;
  color: #1e40af;
}

.info-tag {
  background: #f3f4f6;
  border: none;
  color: #6b7280;
}

.node-actions {
  display: flex;
  gap: 8px;
}

:deep(.el-card__header) {
  padding: 20px 24px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
