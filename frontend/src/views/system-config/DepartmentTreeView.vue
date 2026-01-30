<template>
  <AdminLayout>
    <div class="board-container">
      <div class="board-header">
        <div class="header-left">
          <h2>{{ t("departmentTree.title") }}</h2>
          <span class="subtitle"
            >Manage departments, mentors, and intern assignments</span
          >
        </div>
        <div class="header-actions">
          <el-button @click="refreshBoard" :icon="Refresh" circle />
          <el-button
            type="primary"
            @click="handleCreateDept"
            v-if="canManage"
            class="btn-create"
          >
            <el-icon class="mr-1"><Plus /></el-icon>
            {{ t("departmentTree.create") }}
          </el-button>
        </div>
      </div>

      <div class="kanban-board" v-loading="loading">
        <div
          v-for="dept in departments"
          :key="dept.id"
          class="kanban-column"
          @dragover.prevent
          @drop="onDropToDepartment($event, dept)"
          @dragenter.prevent
        >
          <div class="column-header">
            <div class="header-title">
              <el-icon class="mr-1 text-gray"><OfficeBuilding /></el-icon>
              <span class="title-text">{{ dept.title }}</span>
              <span class="count-bubble">{{ dept.mentors.length }}</span>
            </div>
            <div class="header-options" v-if="canManage">
              <el-dropdown
                trigger="click"
                @command="(cmd) => handleDeptCommand(cmd, dept)"
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
                      >Delete Dept</el-dropdown-item
                    >
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <div class="column-body custom-scroll">
            <div
              v-for="mentor in dept.mentors"
              :key="mentor.id"
              class="mentor-card"
              :class="{ 'is-expanded': expandedMentorIds.has(mentor.id) }"
              :draggable="canManage"
              @dragstart.stop="onDragStartMentor($event, mentor, dept)"
              @dragend="onDragEnd"
              @dragover.prevent
              @drop.stop="onDropToMentor($event, mentor)"
            >
              <div class="mentor-header" @click="toggleMentorExpand(mentor.id)">
                <div class="mentor-header-content">
                  <div class="toggle-btn">
                    <el-icon :size="20"><CaretRight /></el-icon>
                  </div>

                  <el-avatar
                    :size="36"
                    class="mentor-avatar"
                    :style="{ backgroundColor: stringToColor(mentor.fullName) }"
                  >
                    {{ getInitials(mentor.fullName) }}
                  </el-avatar>

                  <div class="mentor-info-text">
                    <div class="mentor-name">{{ mentor.fullName }}</div>
                    <div class="mentor-meta">
                      <el-tag
                        size="small"
                        type="info"
                        effect="plain"
                        round
                        class="meta-tag"
                      >
                        {{ mentor.interns.length }} Interns
                      </el-tag>
                    </div>
                  </div>
                </div>

                <div class="mentor-delete-btn" v-if="canManage">
                  <el-button
                    link
                    type="danger"
                    @click.stop="handleDeleteMentor(mentor)"
                  >
                    <el-icon :size="18"><Delete /></el-icon>
                  </el-button>
                </div>
              </div>

              <transition name="expand">
                <div
                  v-show="expandedMentorIds.has(mentor.id)"
                  class="mentor-interns-list"
                >
                  <div
                    v-for="intern in mentor.interns"
                    :key="intern.id"
                    class="intern-item"
                    :draggable="canDragIntern(intern)"
                    @dragstart.stop="onDragStartIntern($event, intern, mentor)"
                    @dragend="onDragEnd"
                    @click.stop="handleEditIntern(intern)"
                    :class="{ 'is-staging': pendingDeleteIds.has(intern.id) }"
                  >
                    <div class="intern-mini-info">
                      <el-avatar
                        :size="24"
                        :style="{
                          backgroundColor: stringToColor(intern.fullName),
                        }"
                      >
                        {{ getInitials(intern.fullName) }}
                      </el-avatar>
                      <span class="intern-name">{{ intern.fullName }}</span>
                    </div>
                    <el-tag
                      v-if="pendingDeleteIds.has(intern.id)"
                      type="danger"
                      size="small"
                      class="staging-tag"
                      >In Bin</el-tag
                    >
                  </div>

                  <div v-if="mentor.interns.length === 0" class="empty-mentor">
                    No interns assigned
                  </div>
                </div>
              </transition>
            </div>

            <div v-if="dept.mentors.length === 0" class="empty-column">
              No mentors here
            </div>
          </div>
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
        v-model="deptDialogVisible"
        :title="isEditMode ? t('departmentTree.edit') : t('departmentTree.create')"
        width="400px"
        class="custom-dialog"
      >
        <el-form
          :model="deptForm"
          :rules="deptRules"
          ref="deptFormRef"
          label-position="top"
        >
          <el-form-item :label="t('departmentTree.name')" prop="title">
            <el-input v-model="deptForm.title" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="deptDialogVisible = false">{{
            t("common.cancel")
          }}</el-button>
          <el-button type="primary" @click="submitDept" :loading="submitting">{{
            t("common.submit")
          }}</el-button>
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
  OfficeBuilding,
  MoreFilled,
  DeleteFilled,
  Close,
  CaretRight,
  Check,
} from "@element-plus/icons-vue";
import { useAuthStore } from "@/stores/auth";
import { useLocaleStore } from "@/locales/locale";
import * as departmentApi from "@/api/department";
import * as userApi from "@/api/user";
import * as internApi from "@/api/intern";
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue";

const router = useRouter();
const authStore = useAuthStore();
const localeStore = useLocaleStore();
const t = computed(() => localeStore.t);
const userRole = computed(() => authStore.userRole);
const canManage = computed(() => userRole.value === "ADMIN");

const canEdit = (data) => {
  if (userRole.value === "ADMIN") return true;
  if (userRole.value === "HR") return data.type === "intern";
  if (userRole.value === "MENTOR" && data.type === "intern") {
    return data.mentorId === authStore.user?.id;
  }
  return false;
};
const canDragIntern = (intern) =>
  canEdit({ type: "intern", mentorId: intern.mentorId || intern.mentor?.id });

// -- State --
const loading = ref(false);
const submitting = ref(false);
const departments = ref([]);
const selectedInternIds = ref(new Set());
const draggedItem = ref(null);
const dragType = ref(null);
const sourceContainerId = ref(null);
const isDragOverBin = ref(false);
const deptDialogVisible = ref(false);
const isEditMode = ref(false);
const deptFormRef = ref(null);
const deptForm = ref({ id: null, title: "" });
const deptRules = computed(() => ({
  title: [{ required: true, message: "Required", trigger: "blur" }],
}));
const pendingDeleteItems = ref([]);
const pendingDeleteIds = computed(
  () => new Set(pendingDeleteItems.value.map((i) => i.id))
);

// --- NEW STATE: ACCORDION ---
const expandedMentorIds = ref(new Set());

const toggleMentorExpand = (mentorId) => {
  const newSet = new Set(expandedMentorIds.value);
  if (newSet.has(mentorId)) newSet.delete(mentorId);
  else newSet.add(mentorId);
  expandedMentorIds.value = newSet;
};

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

const handleDropToStaging = () => {
  isDragOverBin.value = false;
  if (!draggedItem.value) return;

  if (dragType.value === "MENTOR") {
    executeDeleteMentor(draggedItem.value);
  } else if (dragType.value === "INTERN") {
    const intern = draggedItem.value;
    if (!pendingDeleteIds.value.has(intern.id)) {
      pendingDeleteItems.value.push(intern);
    }
  }

  draggedItem.value = null;
  dragType.value = null;
};

const removeFromStaging = (item) => {
  const index = pendingDeleteItems.value.findIndex((i) => i.id === item.id);
  if (index > -1) {
    pendingDeleteItems.value.splice(index, 1);
  }
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
      "Confirm Deletion",
      {
        confirmButtonText: "Delete",
        cancelButtonText: "Cancel",
        type: "warning",
      }
    );
    await Promise.all(ids.map((id) => internApi.permanentDeleteIntern(id)));
    ElMessage.success("Deleted successfully");
    pendingDeleteItems.value = [];
    refreshBoard();
  } catch (e) {}
};

const fetchDepartmentsTree = async () => {
  loading.value = true;
  try {
    const deptRes = await departmentApi.getDepartments({ page: 0, limit: 100 });
    const rawDepts = deptRes.data.data.items || [];
    const mappedDepts = rawDepts.map((d) => ({ ...d, mentors: [] }));
    const mentorPromises = mappedDepts.map(async (dept) => {
      try {
        const mentorRes = await userApi.getMentorsByDepartment(dept.id);
        const rawMentors = mentorRes.data.data || [];
        const internPromises = rawMentors.map(async (mentor) => {
          try {
            const internRes = await internApi.getInternsByMentor(mentor.id, {
              page: 0,
              limit: 100,
            });
            mentor.interns = internRes.data.data.items || [];
          } catch (e) {
            mentor.interns = [];
          }
          return mentor;
        });
        dept.mentors = await Promise.all(internPromises);
      } catch (e) {}
    });
    await Promise.all(mentorPromises);
    departments.value = mappedDepts;
  } catch (error) {
    ElMessage.error("Failed to load board");
  } finally {
    loading.value = false;
  }
};

const refreshBoard = () => {
  selectedInternIds.value = new Set();
  fetchDepartmentsTree();
};
const onDragStartMentor = (e, m, d) => {
  dragType.value = "MENTOR";
  draggedItem.value = m;
  sourceContainerId.value = d.id;
  e.dataTransfer.effectAllowed = "move";
};
const onDragStartIntern = (e, i, m) => {
  dragType.value = "INTERN";
  if (!selectedInternIds.value.has(i.id)) {
    const s = new Set();
    s.add(i.id);
    selectedInternIds.value = s;
  }
  draggedItem.value = i;
  sourceContainerId.value = m.id;
  e.dataTransfer.effectAllowed = "move";
};
const onDragEnd = () => {
  draggedItem.value = null;
  dragType.value = null;
  sourceContainerId.value = null;
  isDragOverBin.value = false;
};

const onDropToDepartment = async (e, dept) => {
  if (dragType.value !== "MENTOR") return;
  const mentor = draggedItem.value;
  if (sourceContainerId.value === dept.id) return;
  // Optimistic UI
  const sIdx = departments.value.findIndex(
    (d) => d.id === sourceContainerId.value
  );
  const tIdx = departments.value.findIndex((d) => d.id === dept.id);
  if (sIdx > -1) {
    const mIdx = departments.value[sIdx].mentors.findIndex(
      (m) => m.id === mentor.id
    );
    if (mIdx > -1) departments.value[sIdx].mentors.splice(mIdx, 1);
  }
  departments.value[tIdx].mentors.push({ ...mentor, departmentId: dept.id });
  try {
    await userApi.updateUser(mentor.id, {
      fullName: mentor.fullName,
      departmentId: dept.id,
      isActive: true,
    });
    ElMessage.success(`Moved to ${dept.title}`);
  } catch (e) {
    refreshBoard();
  }
};

const onDropToMentor = async (e, targetMentor) => {
  if (dragType.value !== "INTERN") return;
  const intern = draggedItem.value;
  if (sourceContainerId.value === targetMentor.id) return;

  // Auto expand target mentor to show drop result
  if (!expandedMentorIds.value.has(targetMentor.id)) {
    const newSet = new Set(expandedMentorIds.value);
    newSet.add(targetMentor.id);
    expandedMentorIds.value = newSet;
  }

  try {
    const ids = Array.from(selectedInternIds.value);
    if (!ids.includes(intern.id)) ids.push(intern.id);
    const promises = ids.map(async (id) => {
      const fullInternRes = await internApi.getInternById(id);
      const fullIntern = fullInternRes.data.data;
      return internApi.updateIntern(id, {
        ...fullIntern,
        mentorId: targetMentor.id,
        positionId: fullIntern.position?.id || null,
        internShipBatchId: fullIntern.internshipBatch?.id || null,
      });
    });
    await Promise.all(promises);
    ElMessage.success(`Assigned ${ids.length} interns`);
    selectedInternIds.value = new Set();
    refreshBoard();
  } catch (e) {
    ElMessage.error("Failed");
  }
};

const executeDeleteInterns = async (ids) => {
  try {
    await ElMessageBox.confirm("Delete?", "Warning", { type: "warning" });
    await Promise.all(ids.map((id) => internApi.permanentDeleteIntern(id)));
    ElMessage.success("Deleted");
    refreshBoard();
    selectedInternIds.value = new Set();
  } catch (e) {}
};
const executeDeleteMentor = async (m) => {
  try {
    await ElMessageBox.confirm("Delete?", "Warning", { type: "warning" });
    await userApi.permanentDeleteUser(m.id);
    ElMessage.success("Deleted");
    refreshBoard();
  } catch (e) {}
};
const handleDropToBin = async () => {
  isDragOverBin.value = false;
  if (!draggedItem.value) return;
  if (dragType.value === "MENTOR") await executeDeleteMentor(draggedItem.value);
  else
    await executeDeleteInterns(
      Array.from(selectedInternIds.value).length
        ? Array.from(selectedInternIds.value)
        : [draggedItem.value.id]
    );
  draggedItem.value = null;
};
const handleBulkDeleteBtn = async () => {
  const ids = Array.from(selectedInternIds.value);
  if (ids.length) await executeDeleteInterns(ids);
};
const toggleSelection = (i) => {
  const s = new Set(selectedInternIds.value);
  if (s.has(i.id)) s.delete(i.id);
  else s.add(i.id);
  selectedInternIds.value = s;
};
const clearSelection = () => (selectedInternIds.value = new Set());
const clearSelectionOutside = (e) => {
  if (
    e.target.classList.contains("kanban-board") ||
    e.target.classList.contains("board-container")
  )
    clearSelection();
};
const handleCreateDept = () => {
  isEditMode.value = false;
  deptForm.value = { id: null, title: "" };
  deptDialogVisible.value = true;
};
const handleDeptCommand = (cmd, d) => {
  if (cmd === "edit") {
    isEditMode.value = true;
    deptForm.value = { id: d.id, title: d.title };
    deptDialogVisible.value = true;
  } else handleDeleteDept(d);
};
const handleDeleteDept = async (d) => {
  try {
    await ElMessageBox.confirm("Delete?", "Warning", { type: "warning" });
    await departmentApi.deleteDepartment(d.id);
    refreshBoard();
  } catch (e) {}
};
const submitDept = async () => {
  if (!deptFormRef.value) return;
  await deptFormRef.value.validate(async (v) => {
    if (v) {
      submitting.value = true;
      try {
        if (isEditMode.value)
          await departmentApi.updateDepartment(deptForm.value.id, {
            title: deptForm.value.title,
          });
        else
          await departmentApi.createDepartment({ title: deptForm.value.title });
        deptDialogVisible.value = false;
        refreshBoard();
      } catch (e) {
      } finally {
        submitting.value = false;
      }
    }
  });
};
const handleEditMentor = (m) => router.push(`/admin/mentors/${m.id}/edit`);
const handleEditIntern = (i) => router.push(`/admin/interns/${i.id}/edit`);
const handleDeleteMentor = (m) => executeDeleteMentor(m);

onMounted(() => fetchDepartmentsTree());
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

/* Kanban Board Layout */
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
  flex: 0 0 320px;
  max-width: 320px;
  background: #f9fafb;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  max-height: 100%; /* Important constraint */
  border: 1px solid #e5e7eb;
}

.column-header {
  padding: 16px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-radius: 12px 12px 0 0;
  flex-shrink: 0; /* Header stays fixed */
}
.title-text {
  font-weight: 600;
  color: #374151;
  font-size: 15px;
  margin-right: 8px;
}
.header-title {
  display: flex;
  align-items: center;
}
.count-bubble {
  background: #e5e7eb;
  color: #4b5563;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 12px;
}

/* COLUMN BODY (The scrollable part containing Mentors) */
.column-body {
  padding: 12px;
  padding-bottom: 80px;
  overflow-y: auto; /* Scroll ONLY the body */
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0; /* Fix flexbox overflow issue */
}

/* --- MENTOR CARD --- */
.mentor-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;  
  overflow: hidden;
  transition: all 0.2s ease-in-out;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  margin-bottom: 4px;
}
.mentor-card:hover {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  border-color: #d1d5db;
}
.mentor-card.is-expanded {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}
.mentor-card.is-expanded .toggle-btn {
  transform: rotate(90deg);
  background-color: #ecf5ff;
  color: #409eff;
}

.mentor-header {
  padding: 12px 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  background: #fff;
  user-select: none;
  position: relative;
}
.mentor-header-content {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  overflow: hidden;
}
.toggle-btn {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: #909399;
  transition: all 0.2s;
  font-size: 16px;
  flex-shrink: 0;
}
.mentor-header:hover .toggle-btn {
  background-color: #f3f4f6;
  color: #606266;
}
.mentor-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  overflow: hidden;
}

.toggle-icon {
  color: #9ca3af;
  transition: transform 0.2s;
  font-size: 14px;
  flex-shrink: 0;
}

.mentor-avatar {
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
  font-weight: 600;
  font-size: 14px;
}
.mentor-info-text {
  display: flex;
  flex-direction: column;
  justify-content: center;
  overflow: hidden;
}
.mentor-name {
  font-weight: 700;
  font-size: 15px;
  color: #1f2937;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.2;
  margin-bottom: 4px;
}
.mentor-meta {
  display: flex;
  align-items: center;
}

.meta-tag {
  height: 20px;
  line-height: 18px;
  padding: 0 8px;
  font-size: 11px;
  border: none;
  background-color: #f3f4f6;
  color: #6b7280;
}

/* DELETE BUTTON */
.mentor-delete-btn {
  opacity: 0;
  transition: opacity 0.2s;
  margin-left: 8px;
}
.mentor-card:hover .mentor-delete-btn {
  opacity: 1;
}
.mentor-header:hover .mentor-delete-btn {
  opacity: 1;
}

/* --- INTERN LIST (SCROLLABLE NOW) --- */
.mentor-interns-list {
  background-color: #f9fafb;
  border-top: 1px solid #f3f4f6;
  padding: 8px 12px 12px 12px;
  max-height: 300px;
  overflow-y: auto;
}
/* Scrollbar for intern list */
.mentor-interns-list::-webkit-scrollbar {
  width: 4px;
}
.mentor-interns-list::-webkit-scrollbar-thumb {
  background: #e5e7eb;
  border-radius: 4px;
}

/* Intern Item */
.intern-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 10px;
  margin-bottom: 6px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: grab;
  transition: all 0.15s;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);
}
.intern-item:hover {
  border-color: #409eff;
  transform: translateX(2px);
}
.intern-mini-info {
  display: flex;
  align-items: center;
  gap: 10px;
}
.intern-name {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
}

/* Checkbox */
.item-checkbox {
  position: absolute;
  left: 8px;
  top: 50%;
  transform: translateY(-50%) scale(0.8);
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 1px solid #dcdfe6;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.2s;
  color: white;
  font-size: 10px;
  z-index: 5;
}
.intern-item:hover .item-checkbox,
.item-checkbox.checked {
  opacity: 1;
  transform: translateY(-50%) scale(1);
}
.item-checkbox.checked {
  background: #409eff;
  border-color: #409eff;
}

.empty-mentor {
  font-size: 12px;
  color: #9ca3af;
  text-align: center;
  padding: 10px;
  font-style: italic;
}
.empty-column {
  text-align: center;
  color: #9ca3af;
  font-size: 13px;
  margin-top: 20px;
  font-style: italic;
}

/* Bulk & Trash styles (Keep existing) */
.bulk-actions-bar {
  position: fixed;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  background: #1f2937;
  color: white;
  padding: 12px 24px;
  border-radius: 50px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  z-index: 2000;
}
.trash-zone {
  position: fixed;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  width: 400px;
  height: 60px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px dashed #ff4d4f;
  border-radius: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  z-index: 2000;
  transition: all 0.3s;
}
.trash-zone.is-active {
  background: #ff4d4f;
  transform: translateX(-50%) scale(1.05);
}
.trash-zone.is-active .trash-content {
  color: white;
}
.trash-content {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #ff4d4f;
  font-weight: 600;
  pointer-events: none;
}
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s;
}
.slide-up-enter-from,
.slide-up-leave-to {
  transform: translate(-50%, 100px);
  opacity: 0;
}

.intern-item.is-staging {
  opacity: 0.5;
  background: #fef0f0;
  border-color: #fab6b6;
  pointer-events: none;
}
.staging-tag {
  margin-left: auto;
}

/* --- STAGING TRASH AREA --- */
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
  max-height: 400px; /* Max height before scroll */
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

/* HEADER */
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

/* LIST ITEMS */
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

/* PLACEHOLDER TEXT */
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

/* Animation */
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