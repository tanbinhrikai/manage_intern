<template>
  <AdminLayout>
    <div class="board-container" @click="clearSelectionOutside">
      <div class="board-header">
        <div class="header-left">
          <h2>{{ t("positionTree.title") }}</h2>
          <span class="subtitle">Manage team structure and assignments</span>
        </div>
        <div class="header-actions">
          <el-button @click="refreshBoard" :icon="Refresh" circle />
          <el-button
            type="primary"
            @click="handleCreatePosition"
            v-if="canManage"
            class="btn-create"
          >
            <el-icon class="mr-1"><Plus /></el-icon>
            {{ t("positionTree.create") }}
          </el-button>
        </div>
      </div>

      <div class="kanban-board" v-loading="loading">
        <div
          v-for="position in positions"
          :key="position.id"
          class="kanban-column"
          @dragover.prevent
          @drop="onDrop($event, position)"
          @dragenter.prevent
        >
          <div class="column-header" @click="togglePositionExpand(position.id)">
            <div class="header-title">
              <div
                class="toggle-btn"
                :class="{ 'is-expanded': expandedPositionIds.has(position.id) }"
              >
                <el-icon><CaretRight /></el-icon>
              </div>

              <span class="title-text">{{ position.title }}</span>
              <el-tag size="small" effect="plain" round class="count-tag">
                {{ position.interns.length }}
              </el-tag>
            </div>

            <div class="header-options" v-if="canManage" @click.stop>
              <el-dropdown
                trigger="click"
                @command="(cmd) => handleColumnCommand(cmd, position)"
              >
                <el-icon class="option-icon"><MoreFilled /></el-icon>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="edit" :icon="Edit"
                      >Edit Name</el-dropdown-item
                    >
                    <el-dropdown-item
                      command="delete"
                      :icon="Delete"
                      divided
                      style="color: #f56c6c"
                      >Delete Position</el-dropdown-item
                    >
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <transition name="expand">
            <div
              class="column-body custom-scroll"
              v-show="expandedPositionIds.has(position.id)"
            >
              <div
                v-for="intern in position.interns"
                :key="intern.id"
                class="intern-card"
                :draggable="canDragIntern(intern)"
                @dragstart="onDragStart($event, intern, position)"
                @dragend="onDragEnd"
                @click.stop="handleEditIntern(intern)"
                :class="{
                  'is-draggable': canDragIntern(intern),
                  'is-staging': pendingDeleteIds.has(intern.id),
                }"
              >
                <div class="card-content">
                  <div class="card-avatar">
                    <el-avatar
                      :size="32"
                      :style="{
                        backgroundColor: stringToColor(intern.fullName),
                      }"
                    >
                      {{ getInitials(intern.fullName) }}
                    </el-avatar>
                  </div>
                  <div class="card-info">
                    <span class="intern-name">{{ intern.fullName }}</span>
                    <span class="intern-role" v-if="intern.mentor">
                      {{ intern.mentor.fullName }}
                    </span>
                  </div>
                </div>

                <div
                  v-if="pendingDeleteIds.has(intern.id)"
                  class="staging-badge"
                >
                  <el-tag type="danger" size="small" effect="dark"
                    >In Bin</el-tag
                  >
                </div>

                <div
                  class="card-actions"
                  v-if="canEdit(intern) && !pendingDeleteIds.has(intern.id)"
                >
                  <el-button
                    link
                    type="danger"
                    size="small"
                    @click.stop="quickDelete(intern)"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>

              <div v-if="position.interns.length === 0" class="empty-column">
                No members
              </div>
            </div>
          </transition>
        </div>
      </div>

      <transition name="slide-up">
        <div
          class="trash-staging-area"
          :class="{
            'is-active': isDragOverBin,
            'has-items': pendingDeleteItems.length > 0,
            'is-dragging': draggedItem,
          }"
          v-if="canManage && (draggedItem || pendingDeleteItems.length > 0)"
          @dragover.prevent="isDragOverBin = true"
          @dragleave="isDragOverBin = false"
          @drop="handleDropToStaging"
        >
          <div class="staging-header">
            <div class="staging-title">
              <el-icon :size="20"><DeleteFilled /></el-icon>
              <span>Recycle Bin ({{ pendingDeleteItems.length }})</span>
            </div>

            <div class="staging-actions" v-if="pendingDeleteItems.length > 0">
              <el-button size="small" text @click="cancelAllStaging"
                >Cancel All</el-button
              >
              <el-button type="danger" size="small" @click="confirmDeleteAll">
                Delete Forever
              </el-button>
            </div>
          </div>

          <div
            class="staging-list custom-scroll"
            v-if="pendingDeleteItems.length > 0"
          >
            <div
              v-for="item in pendingDeleteItems"
              :key="item.id"
              class="staging-item"
            >
              <span class="staging-name">{{ item.fullName }}</span>
              <el-icon
                class="staging-remove-btn"
                @click="removeFromStaging(item)"
              >
                <Close />
              </el-icon>
            </div>
          </div>

          <div class="staging-placeholder" v-if="draggedItem && !isDragOverBin">
            Drop here to add to bin
          </div>
          <div class="staging-placeholder active" v-if="isDragOverBin">
            Release to add
          </div>
        </div>
      </transition>

      <el-dialog
        v-model="positionDialogVisible"
        :title="isEditMode ? t('positionTree.edit') : t('positionTree.create')"
        width="400px"
        class="custom-dialog"
      >
        <el-form
          :model="positionForm"
          :rules="positionRules"
          ref="positionFormRef"
          label-position="top"
        >
          <el-form-item :label="t('positionTree.name')" prop="title">
            <el-input
              v-model="positionForm.title"
              placeholder="e.g. Java Backend Team"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="positionDialogVisible = false">{{
            t("positionTree.common.cancel")
          }}</el-button>
          <el-button
            type="primary"
            @click="submitPosition"
            :loading="submitting"
            >{{ t("positionTree.common.submit") }}</el-button
          >
        </template>
      </el-dialog>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Plus,
  Edit,
  Delete,
  Refresh,
  MoreFilled,
  DeleteFilled,
  Close,
  CaretRight,
} from "@element-plus/icons-vue";
import { useAuthStore } from "@/stores/auth";
import { useLocaleStore } from "@/locales/locale";
import * as positionApi from "@/api/position";
import * as internApi from "@/api/intern";
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue";

const router = useRouter();
const authStore = useAuthStore();
const localeStore = useLocaleStore();
const t = computed(() => localeStore.t);

const userRole = computed(() => authStore.userRole);
const canManage = computed(() => ["ADMIN", "HR"].includes(userRole.value));

const canEdit = (intern) => {
  if (userRole.value === "ADMIN") return true;
  if (userRole.value === "HR") return true;
  if (userRole.value === "MENTOR") {
    const internMentorId = intern.mentorId || intern.mentor?.id;
    return internMentorId === authStore.user?.id;
  }
  return false;
};
const canDragIntern = (intern) => canEdit(intern);

// -- State --
const loading = ref(false);
const submitting = ref(false);
const positions = ref([]);
const draggedItem = ref(null);
const sourcePositionId = ref(null);
const isDragOverBin = ref(false);

// Staging Bin State
const pendingDeleteItems = ref([]);
const pendingDeleteIds = computed(
  () => new Set(pendingDeleteItems.value.map((i) => i.id))
);

// Accordion State
const expandedPositionIds = ref(new Set());

// -- Forms --
const positionDialogVisible = ref(false);
const isEditMode = ref(false);
const positionFormRef = ref(null);
const positionForm = ref({ id: null, title: "" });
const positionRules = computed(() => ({
  title: [
    {
      required: true,
      message: t.value("positionTree.nameRequired"),
      trigger: "blur",
    },
  ],
}));

// -- Helpers --
const getInitials = (name) => {
  if (!name) return "U";
  return name
    .split(" ")
    .map((n) => n[0])
    .join("")
    .substring(0, 2)
    .toUpperCase();
};
const stringToColor = (str) => {
  if (!str) return "#409EFF";
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash);
  }
  const c = (hash & 0x00ffffff).toString(16).toUpperCase();
  return "#" + "00000".substring(0, 6 - c.length) + c;
};

// -- Data Fetching --
const fetchPositionsAndInterns = async () => {
  loading.value = true;
  try {
    const posRes = await positionApi.getPositions({ page: 0, limit: 100 });
    const rawPositions = posRes.data.data.items || [];
    const mappedPositions = rawPositions.map((p) => ({ ...p, interns: [] }));

    const promises = mappedPositions.map(async (pos) => {
      try {
        const internRes = await internApi.getInternsByPosition(pos.id, {
          page: 0,
          limit: 100,
        });
        pos.interns = internRes.data.data.items || [];
      } catch (e) {
        console.error(e);
      }
    });

    await Promise.all(promises);
    positions.value = mappedPositions;

    const allPositionIds = mappedPositions.map(p => p.id);
    expandedPositionIds.value = new Set(allPositionIds);
  } catch (error) {
    ElMessage.error("Failed to load team board");
  } finally {
    loading.value = false;
  }
};

const refreshBoard = () => {
  pendingDeleteItems.value = [];
  fetchPositionsAndInterns();
};

// -- ACCORDION LOGIC --
const togglePositionExpand = (posId) => {
  const newSet = new Set(expandedPositionIds.value);
  if (newSet.has(posId)) newSet.delete(posId);
  else newSet.add(posId);
  expandedPositionIds.value = newSet;
};

const clearSelectionOutside = (e) => {
  // Optional: Clear expanding if clicking outside (not recommended for UX usually)
  // For now, this function is just a placeholder if you need click-outside logic later
};

// -- DRAG START --
const onDragStart = (event, intern, fromPosition) => {
  draggedItem.value = intern;
  sourcePositionId.value = fromPosition.id;
  event.dataTransfer.effectAllowed = "move";
  event.dataTransfer.dropEffect = "move";
};

const onDragEnd = () => {
  draggedItem.value = null;
  sourcePositionId.value = null;
  isDragOverBin.value = false;
};

// -- STAGING BIN LOGIC --
const handleDropToStaging = () => {
  isDragOverBin.value = false;
  if (!draggedItem.value) return;

  // Add to pending list if not already there
  if (!pendingDeleteIds.value.has(draggedItem.value.id)) {
    pendingDeleteItems.value.push(draggedItem.value);
  }

  draggedItem.value = null;
  sourcePositionId.value = null;
};

const removeFromStaging = (item) => {
  const idx = pendingDeleteItems.value.findIndex((i) => i.id === item.id);
  if (idx > -1) pendingDeleteItems.value.splice(idx, 1);
};

const cancelAllStaging = () => {
  pendingDeleteItems.value = [];
};

const confirmDeleteAll = async () => {
  const ids = pendingDeleteItems.value.map((i) => i.id);
  if (ids.length === 0) return;

  try {
    await ElMessageBox.confirm(
      `Permanently delete ${ids.length} intern(s)?`,
      "Confirm Delete",
      {
        confirmButtonText: "Delete",
        cancelButtonText: "Cancel",
        type: "warning",
      }
    );

    // Optimistic UI Remove
    positions.value.forEach((pos) => {
      pos.interns = pos.interns.filter((i) => !ids.includes(i.id));
    });

    await Promise.all(ids.map((id) => internApi.permanentDeleteIntern(id)));

    ElMessage.success("Deleted successfully");
    pendingDeleteItems.value = [];
  } catch (e) {
    if (e !== "cancel") refreshBoard();
  }
};

const quickDelete = (intern) => {
  pendingDeleteItems.value.push(intern);
  // Optional: Auto open bin or just show badge
  ElMessage.info("Item added to bin");
};

// -- DROP TO COLUMN (MOVE) --
const onDrop = async (event, targetPosition) => {
  const intern = draggedItem.value;
  if (!intern || !targetPosition) return;
  if (sourcePositionId.value === targetPosition.id) return;

  // Auto expand target column
  if (!expandedPositionIds.value.has(targetPosition.id)) {
    const newSet = new Set(expandedPositionIds.value);
    newSet.add(targetPosition.id);
    expandedPositionIds.value = newSet;
  }

  // Optimistic UI Update
  const sourcePosIndex = positions.value.findIndex(
    (p) => p.id === sourcePositionId.value
  );
  const targetPosIndex = positions.value.findIndex(
    (p) => p.id === targetPosition.id
  );

  if (sourcePosIndex > -1 && targetPosIndex > -1) {
    const internIndex = positions.value[sourcePosIndex].interns.findIndex(
      (i) => i.id === intern.id
    );
    if (internIndex > -1) {
      positions.value[sourcePosIndex].interns.splice(internIndex, 1);
    }
    positions.value[targetPosIndex].interns.push({
      ...intern,
      positionId: targetPosition.id,
    });
  }

  try {
    const payload = {
      fullName: intern.fullName,
      startDate: intern.startDate,
      endDate: intern.endDate,
      internStatus: intern.internStatus,
      positionId: targetPosition.id,
      mentorId: intern.mentor?.id || null,
      internShipBatchId: intern.internshipBatch?.id || null,
    };
    await internApi.updateIntern(intern.id, payload);
    ElMessage.success(`Moved ${intern.fullName} to ${targetPosition.title}`);
  } catch (error) {
    refreshBoard();
  } finally {
    draggedItem.value = null;
    sourcePositionId.value = null;
  }
};

// -- CRUD Position --
const handleCreatePosition = () => {
  isEditMode.value = false;
  positionForm.value = { id: null, title: "" };
  positionDialogVisible.value = true;
};
const handleColumnCommand = (command, position) => {
  if (command === "edit") {
    isEditMode.value = true;
    positionForm.value = { id: position.id, title: position.title };
    positionDialogVisible.value = true;
  } else if (command === "delete") handleDeletePosition(position);
};
const handleDeletePosition = async (position) => {
  try {
    await ElMessageBox.confirm(
      `Delete position "${position.title}"?`,
      "Warning",
      { type: "warning" }
    );
    await positionApi.deletePosition(position.id);
    refreshBoard();
  } catch (e) {}
};
const submitPosition = async () => {
  if (!positionFormRef.value) return;
  await positionFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        if (isEditMode.value)
          await positionApi.updatePosition(positionForm.value.id, {
            title: positionForm.value.title,
          });
        else
          await positionApi.createPosition({ title: positionForm.value.title });
        positionDialogVisible.value = false;
        refreshBoard();
      } catch (error) {
      } finally {
        submitting.value = false;
      }
    }
  });
};
const handleEditIntern = (intern) => {
  if (canEdit(intern)) router.push(`/admin/interns/${intern.id}/edit`);
};

onMounted(() => {
  fetchPositionsAndInterns();
});
</script>

<style scoped>
/* Main Container */
.board-container {
  height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
  padding: 20px;
  background-color: #f4f6f9;
  font-family: "Inter", sans-serif;
  position: relative;
}

/* Header */
.board-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.header-left h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
}
.subtitle {
  color: #6b7280;
  font-size: 14px;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* Kanban Board */
.kanban-board {
  display: flex;
  overflow-x: auto;
  gap: 20px;
  flex: 1;
  padding-bottom: 12px;
  align-items: flex-start;
}
.custom-scroll::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
.custom-scroll::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 4px;
}

/* Columns */
.kanban-column {
  flex: 0 0 300px;
  max-width: 300px;
  background: #f9fafb;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  max-height: 100%;
  border: 1px solid #e5e7eb;
}

/* Column Header (Clickable for Accordion) */
.column-header {
  padding: 16px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-radius: 12px 12px 0 0;
  cursor: pointer;
  user-select: none;
}
.column-header:hover {
  background: #f9fafb;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}
.title-text {
  font-weight: 600;
  color: #374151;
  font-size: 15px;
}
.count-tag {
  margin-left: auto;
  margin-right: 8px;
}

/* Toggle Icon */
.toggle-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  color: #9ca3af;
  transition: all 0.2s;
}
.toggle-btn.is-expanded {
  transform: rotate(90deg);
  color: #409eff;
  background: #ecf5ff;
}

/* Options */
.option-icon {
  cursor: pointer;
  color: #9ca3af;
  transform: rotate(90deg);
}
.option-icon:hover {
  color: #3b82f6;
}

/* Column Body */
.column-body {
  padding: 12px;
  padding-bottom: 80px;
  overflow-y: auto;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0; /* Important for animation */
}

/* Accordion Animation */
.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  max-height: 1000px;
  opacity: 1;
  overflow: hidden;
}
.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
  padding-top: 0;
  padding-bottom: 0;
}

/* Cards */
.intern-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px;
  position: relative;
  transition: all 0.2s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}
.is-draggable {
  cursor: grab;
}
.is-draggable:active {
  cursor: grabbing;
}
.intern-card:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
  border-color: #bfdbfe;
}

/* Staging Style */
.intern-card.is-staging {
  opacity: 0.5;
  background: #fef0f0;
  border-color: #fab6b6;
  pointer-events: none;
}
.staging-badge {
  position: absolute;
  top: 12px;
  right: 12px;
}

.card-content {
  display: flex;
  align-items: center;
  gap: 12px;
}
.card-info {
  display: flex;
  flex-direction: column;
}
.intern-name {
  font-weight: 600;
  font-size: 14px;
  color: #111827;
}
.intern-role {
  font-size: 11px;
  color: #6b7280;
  margin-top: 2px;
}

.card-actions {
  position: absolute;
  bottom: 8px;
  right: 8px;
  opacity: 0;
  transition: opacity 0.2s;
}
.intern-card:hover .card-actions {
  opacity: 1;
}

.empty-column {
  text-align: center;
  color: #9ca3af;
  font-size: 13px;
  margin-top: 40px;
  font-style: italic;
}

/* STAGING BIN */
.trash-staging-area {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  width: 500px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  border: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  z-index: 2000;
  max-height: 400px;
}
.trash-staging-area.is-dragging {
  border: 2px dashed #d1d5db;
  transform: translateX(-50%) translateY(0);
}
.trash-staging-area.is-active {
  border-color: #f56c6c;
  background-color: #fef0f0;
  transform: translateX(-50%) scale(1.02);
}

.staging-header {
  padding: 12px 16px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.staging-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #f56c6c;
}

.staging-list {
  padding: 8px;
  overflow-y: auto;
  max-height: 200px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.staging-item {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  padding: 4px 8px 4px 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}
.staging-remove-btn {
  cursor: pointer;
  padding: 2px;
  border-radius: 50%;
  color: #9ca3af;
  transition: all 0.2s;
}
.staging-remove-btn:hover {
  background: #e5e7eb;
  color: #f56c6c;
}

.staging-placeholder {
  padding: 20px;
  text-align: center;
  color: #9ca3af;
  font-style: italic;
  font-weight: 500;
}
.staging-placeholder.active {
  color: #f56c6c;
  font-weight: 700;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s;
}
.slide-up-enter-from,
.slide-up-leave-to {
  transform: translate(-50%, 120%);
  opacity: 0;
}
</style>