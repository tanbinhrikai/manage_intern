<template>
  <AdminLayout>
    <div class="roadmap-builder-container">
      <el-row :gutter="20" class="main-layout-row">

        <!-- Section 1: Roadmap Configuration -->
        <el-col :span="10" class="layout-col">

          <el-card class="box-card full-height-card">
            <template #header>
              <div class="card-header">
                <h2 class="title">Roadmap Configuration</h2>
                <el-button :style="{marginTop: '10px'}" type="warning" plain @click="resetForm">Create New Roadmap</el-button>
              </div>
            </template>
            
            <el-form :model="configForm" label-width="150px" class="config-form" label-position="top">
              <el-form-item label="Load roadmaps">
                <el-select v-model="selectedRoadmapId" @change="loadSelectedRoadmap" placeholder="-- Select a roadmap --" style="width: 100%" clearable>
                  <el-option
                    v-for="roadmap in roadmaps"
                    :key="roadmap.roadmapId"
                    :label="roadmap.title"
                    :value="roadmap.roadmapId"
                  >
                    <div style="display: flex; justify-content: space-between; align-items: center; width: 100%">
                      <span style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 250px;">{{ roadmap.title }}</span>
                      <el-tag
                        :type="roadmap.status === 'PUBLISHED' ? 'success' : 'info'"
                        size="small"
                        effect="plain"
                        style="margin-left: 10px; flex-shrink: 0;"
                      >
                        {{ roadmap.status === 'PUBLISHED' ? 'PUBLISHED' : 'DRAFT' }}
                      </el-tag>
                    </div>
                  </el-option>
                </el-select>
              </el-form-item>
              
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="ID" required :style="{display: 'none'}">
                    <el-select v-model="configForm.id" style="width: 100%">
                      <el-option
                        v-for="item in positions"
                        :key="item.id"
                        :label="item.id"
                        :value="item.id"
                      />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="Position" required>
                    <el-select v-model="configForm.positionId" placeholder="Select Position" style="width: 100%">
                      <el-option
                        v-for="item in positions"
                        :key="item.id"
                        :label="item.title"
                        :value="item.id"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="Internship Batch" required>
                    <el-select v-model="configForm.batchId" placeholder="Select Batch" style="width: 100%">
                      <el-option
                        v-for="item in batches"
                        :key="item.id"
                        :label="item.name"
                        :value="item.id"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-form-item label="Duration" required>
                <el-radio-group v-model="configForm.duration">
                  <el-radio :value="2">2 Months</el-radio>
                  <el-radio :value="4">4 Months</el-radio>
                  <el-radio :value="6">6 Months</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="Prompt">
                <el-input 
                  v-model="configForm.prompt" 
                  type="textarea" 
                  :rows="3" 
                  placeholder="Enter specific requirements for AI (optional)" 
                />
              </el-form-item>
              
              <el-form-item>
                <el-button 
                  type="primary" 
                  @click="generateRoadmap" 
                  style="width: 100%;"
                  :disabled="isTreeVisible || isGenerating"
                >
                  Generate AI Roadmap
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>

        <!-- Section 2: Roadmap Tree Editor -->
        <el-col :span="14" class="layout-col">
          <el-card class="box-card flex-1 full-height-card" v-if="isTreeVisible">
            <template #header>
              <div class="card-header">
                <div class="header-titles">
                  <h2 class="title">Roadmap Tree Editor</h2>
                  <div class="subtitle">
                    Kéo thả các node để sắp xếp, di chuyển. Bấm nút để thêm/sửa/xoá. | Thời gian: {{ configForm.duration }} tháng
                  </div>
                </div>
                <div class="header-actions">
                  <el-button type="danger" plain @click="cancelRoadmap">Cancel Roadmap</el-button>
                  <el-button type="info" plain @click="saveDraft">Save Draft</el-button>
                  <el-button type="success" plain @click="saveAndConfirm">Save & Confirm</el-button>
                  <el-button type="primary" @click="addRootNode">+ Add Root Node</el-button>
                </div>
              </div>
            </template>

            <el-skeleton :loading="isGenerating" animated>
              <template #template>
                <div style="padding: 14px;">
                  <el-skeleton-item variant="p" style="width: 40%; margin-bottom: 20px; height: 30px;" />
                  <div style="margin-left: 20px; margin-bottom: 16px;">
                    <el-skeleton-item variant="p" style="width: 30%; height: 20px;" />
                  </div>
                  <div style="margin-left: 20px; margin-bottom: 16px;">
                    <el-skeleton-item variant="p" style="width: 50%; height: 20px;" />
                  </div>
                  <div style="margin-left: 40px; margin-bottom: 16px;">
                    <el-skeleton-item variant="p" style="width: 40%; height: 20px;" />
                  </div>
                </div>
              </template>

              <!-- tree -->
              <template #default>
                <el-tree
                  ref="treeRef"
                  node-key="id"
                  lazy
                  :load="loadNode"
                  :props="treeProps"
                  draggable
                  :allow-drop="allowDrop"
                  @node-drop="handleDrop"
                  @node-expand="handleNodeExpand"
                  @node-collapse="handleNodeCollapse"
                  :default-expanded-keys="defaultExpandedKeys"
                  :expand-on-click-node="true"
                  class="roadmap-tree"
                >
                  <template #default="{ node, data }">
                    <div class="custom-tree-node">
                      <div class="node-content">
                        <div class="node-title">
                          {{ getDisplayTitle(node, data) }}
                          <el-tag type="info" size="small" style="margin-left: 8px;">
                            {{ data.estimatedHours || 0 }}h
                          </el-tag>
                          <el-tag 
                            v-if="data.difficulty" 
                            :type="getDifficultyTagType(data.difficulty)" 
                            size="small" 
                            style="margin-left: 8px;"
                          >
                            {{ data.difficulty }}
                          </el-tag>
                        </div>
                        <div class="node-desc" v-if="data.description">{{ data.description }}</div>
                      </div>
                      <div class="node-actions">
                        <el-button 
                          v-if="data.nodeType !== 'TASK'" 
                          size="small" 
                          type="primary" 
                          circle
                          :icon="Plus"
                          title="Thêm node con"
                          @click.stop="addChild(data)"
                        />
                        <el-button 
                          v-if="data.nodeType !== 'TASK'" 
                          size="small" 
                          type="success" 
                          circle
                          :icon="MagicStick"
                          title="Sinh AI gợi ý"
                          @click.stop="generateAI(node, data)"
                        />
                        <el-button 
                          size="small" 
                          type="info" 
                          circle
                          :icon="View"
                          title="Xem chi tiết"
                          @click.stop="viewNodeDetail(data)"
                        />
                        <el-button 
                          v-if="data.nodeType !== 'TASK'" 
                          size="small" 
                          type="default" 
                          circle
                          :icon="Lock"
                          title="Đánh dấu không thể mở rộng"
                          @click.stop="toggleExpandable(node, data, true)"
                        />
                        <el-button 
                          v-if="data.nodeType === 'TASK' && node.level < 5" 
                          size="small" 
                          type="warning" 
                          circle
                          :icon="Unlock"
                          title="Cho phép mở rộng"
                          @click.stop="toggleExpandable(node, data, false)"
                        />
                        <el-button 
                          size="small" 
                          type="warning" 
                          circle
                          :icon="Edit"
                          title="Sửa"
                          @click.stop="editNode(data)"
                        />
                        <el-button 
                          size="small" 
                          type="danger" 
                          circle
                          :icon="Delete"
                          title="Xóa"
                          @click.stop="deleteNode(node, data)"
                        />
                      </div>
                    </div>
                  </template>
                </el-tree>
              </template>
            </el-skeleton>
          </el-card>
          
          <el-empty v-else description="Vui lòng cấu hình và bấm Generate AI Roadmap để xem cây lộ trình" />
        </el-col>
      </el-row>

      <!-- Edit Dialog -->
      <el-dialog v-model="dialogVisible" title="Edit Node" width="500px">
        <el-form :model="editForm" label-width="120px" @submit.prevent="saveEdit">
          <el-form-item label="Title">
            <el-input v-model="editForm.title" />
          </el-form-item>
          <el-form-item label="Description">
            <el-input v-model="editForm.description" type="textarea" :rows="3" />
          </el-form-item>
          <el-form-item label="Estimated Hours">
            <el-input-number v-model="editForm.estimatedHours" :min="0" :precision="1" :step="0.5" style="width: 100%" />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false">Cancel</el-button>
            <el-button type="primary" @click="saveEdit">Save</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- Add Node Dialog -->
      <el-dialog v-model="addDialogVisible" title="Add Node" width="500px">
        <el-form :model="addForm" :rules="addRules" ref="addFormRef" label-width="120px" @submit.prevent="submitAddNode">
          <el-form-item label="Title" prop="title">
            <el-input v-model="addForm.title" placeholder="Nhập tiêu đề (bắt buộc)" />
          </el-form-item>
          <el-form-item label="Description" prop="description">
            <el-input v-model="addForm.description" type="textarea" :rows="3" placeholder="Nhập mô tả (không bắt buộc)" />
          </el-form-item>
          <el-form-item label="Estimated Hours" prop="estimatedHours">
            <el-input-number v-model="addForm.estimatedHours" :min="0" :precision="1" :step="0.5" style="width: 100%" />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="addDialogVisible = false">Cancel</el-button>
            <el-button type="primary" @click="submitAddNode">Save</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- Detail Dialog -->
      <el-dialog v-model="detailDialogVisible" title="Chi tiết Node" width="550px">
        <div class="node-detail-content" v-if="selectedNodeForDetail">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="Tiêu đề">{{ selectedNodeForDetail.title }}</el-descriptions-item>
            <el-descriptions-item label="Mô tả">{{ selectedNodeForDetail.description || 'Không có mô tả' }}</el-descriptions-item>
            <el-descriptions-item label="Loại Node">
              <el-tag size="small">{{ selectedNodeForDetail.nodeType }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Độ khó">
              <el-tag :type="getDifficultyTagType(selectedNodeForDetail.difficulty)" size="small">
                {{ selectedNodeForDetail.difficulty || 'Chưa xác định' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Thời lượng dự kiến">
              {{ selectedNodeForDetail.estimatedHours || 0 }} giờ
            </el-descriptions-item>
            <el-descriptions-item label="Điều kiện vượt qua">
              {{ selectedNodeForDetail.passCondition || 'Không có' }}
            </el-descriptions-item>
            <el-descriptions-item label="Kết quả đầu ra">
              {{ selectedNodeForDetail.learningOutcome || 'Không có' }}
            </el-descriptions-item>
            <el-descriptions-item label="Phương thức đánh giá">
              {{ selectedNodeForDetail.assessmentMethod || 'Không có' }}
            </el-descriptions-item>
            <el-descriptions-item label="Tags">
              <el-tag 
                v-for="tag in selectedNodeForDetail.tags" 
                :key="tag" 
                size="small" 
                style="margin-right: 5px;"
              >
                {{ tag }}
              </el-tag>
              <span v-if="!selectedNodeForDetail.tags || !selectedNodeForDetail.tags.length">Không có tag</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button type="primary" @click="detailDialogVisible = false">Đóng</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { Plus, Edit, Delete, MagicStick, View, Lock, Unlock } from "@element-plus/icons-vue";
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue";
import { ElMessageBox, ElMessage, ElLoading } from "element-plus";
import { getPositions } from "@/api/position";
import { getBatches } from "@/api/internship-batch";
import { generatePhases, expandNode, addNode, editNodeDetail, removeNode, moveNode, saveDraftRoadmap, fetchRoadmaps, fetchRoadmapById, deleteRoadmap } from "@/api/roadmap";
import { nextTick } from "vue";

const positions = ref([]);
const batches = ref([]);
const isTreeVisible = ref(false);
const isGenerating = ref(false);
const treeRef = ref(null);
const dialogVisible = ref(false);

const addDialogVisible = ref(false);
const addFormRef = ref(null);
const addForm = ref({
  title: "",
  description: "",
  estimatedHours: 1.0
});
const currentAddParentId = ref(null);
const addRules = {
  title: [
    { required: true, message: "Vui lòng nhập tiêu đề node", trigger: "blur" }
  ]
};

const roadmaps = ref([]);
const selectedRoadmapId = ref(null);
const loadedRoadmapRootNodes = ref([]);
const expandedKeys = ref([]);
const defaultExpandedKeys = ref([]);

const detailDialogVisible = ref(false);
const selectedNodeForDetail = ref(null);

const getDifficultyTagType = (difficulty) => {
  if (!difficulty) return 'info';
  switch (difficulty.toUpperCase()) {
    case 'BEGINNER': return 'success';
    case 'INTERMEDIATE': return 'warning';
    case 'ADVANCED': return 'danger';
    default: return 'info';
  }
};

const viewNodeDetail = (data) => {
  selectedNodeForDetail.value = data;
  detailDialogVisible.value = true;
};

const toggleExpandable = async (node, data, markAsLeaf) => {
  let targetType;
  if (markAsLeaf) {
    if (data.children && data.children.length > 0) {
      try {
        await ElMessageBox.confirm(
          "Đánh dấu không thể mở rộng sẽ xóa tất cả các node con hiện có của node này. Bạn có chắc chắn muốn tiếp tục?",
          "Cảnh báo",
          {
            confirmButtonText: "Đồng ý",
            cancelButtonText: "Hủy",
            type: "warning"
          }
        );
      } catch {
        return;
      }
    }
    targetType = "TASK";
  } else {
    switch (node.level) {
      case 1: targetType = "PHASE"; break;
      case 2: targetType = "MODULE"; break;
      case 3: targetType = "LESSON"; break;
      case 4: targetType = "TOPIC"; break;
      default: targetType = "TASK"; break;
    }
  }

  try {
    const res = await editNodeDetail(data.id, {
      title: data.title,
      description: data.description,
      estimatedHours: data.estimatedHours,
      nodeType: targetType
    });
    ElMessage.success(markAsLeaf ? "Đã đánh dấu node không thể mở rộng" : "Đã cho phép mở rộng node");
    await refreshTree();
  } catch (err) {
    console.error("Toggle expandable error:", err);
    ElMessage.error("Thao tác thất bại");
  }
};

const configForm = ref({
  id: null,
  positionId: null,
  batchId: null,
  duration: 2,
  prompt: ""
});

const treeData = ref([]);

const treeProps = {
  label: 'title',
  children: 'children',
  isLeaf: 'isLeaf'
};

const cleanNodeTitle = (title) => {
  let cleaned = title || "";
  const prefixRegex = /^(?:Phase|Module|Lesson|Topic|TASK|Task|Node)\s+\d+(?:\.\d+)*\s*-\s*/i;
  if (prefixRegex.test(cleaned)) {
    cleaned = cleaned.replace(prefixRegex, '');
    cleaned = cleaned.replace(/(?:\.\d+)+$/, ''); 
  }
  return cleaned;
};

const getDisplayTitle = (node, data) => {
  let typeStr = 'Node';
  if (data.nodeType) {
    if (data.nodeType === 'TASK') typeStr = 'Task';
    else typeStr = data.nodeType.charAt(0).toUpperCase() + data.nodeType.slice(1).toLowerCase();
  } else {
    // Fallback based on level (1-indexed)
    switch(node.level) {
      case 1: typeStr = 'Phase'; break;
      case 2: typeStr = 'Module'; break;
      case 3: typeStr = 'Lesson'; break;
      case 4: typeStr = 'Topic'; break;
      case 5: typeStr = 'Task'; break;
    }
  }

  let index = 0;
  if (node && node.parent && node.parent.childNodes) {
    index = node.parent.childNodes.indexOf(node);
  } else if (data.orderIndex !== undefined && data.orderIndex !== null) {
    index = data.orderIndex;
  }
  const localIndex = index + 1;
  const cleanedTitle = cleanNodeTitle(data.title);
  return `${typeStr} ${localIndex} - ${cleanedTitle}`;
};

const handleNodeExpand = (data) => {
  if (!expandedKeys.value.includes(data.id)) {
    expandedKeys.value.push(data.id);
  }
};

const handleNodeCollapse = (data) => {
  expandedKeys.value = expandedKeys.value.filter(id => id !== data.id);
};

const getPositionsList = async () => {
  try {
    const res = await getPositions();
    positions.value = res.data?.data.items || res.data || [];
  } catch (err) {
    console.error("Failed to load positions", err);
  }
}

const getInternshipBatches = async () => {
  try {
    const res = await getBatches({ status: 'ONGOING' });
    batches.value = res.data?.data.items || res.data || [];
  } catch (err) {
    console.error("Failed to load batches", err);
  }
}

const loadRoadmaps = async () => {
  try {
    const res = await fetchRoadmaps();
    roadmaps.value = res.data?.data || res.data || [];
  } catch (err) {
    console.error("Failed to load DB roadmaps", err);
  }
};

onMounted(() => {
  getPositionsList()
  getInternshipBatches()
  loadRoadmaps()
});

const loadSelectedRoadmap = async () => {
  if (!selectedRoadmapId.value) return;
  
  try {
    const res = await fetchRoadmapById(selectedRoadmapId.value);
    const roadmap = res.data?.data || res.data;
    
    configForm.value.id = roadmap.roadmapId;
    configForm.value.title = roadmap.title;
    configForm.value.prompt = roadmap.title;
    configForm.value.duration = roadmap.durationMonth;
    configForm.value.positionId = roadmap.positionId;
    configForm.value.batchId = roadmap.batchId;
    
    const mapDbNodes = (nodes) => {
      if (!nodes) return [];
      return nodes.map((n, idx) => ({
        ...n,
        title: cleanNodeTitle(n.title),
        estimatedHours: n.estimatedHours || 0.0,
        children: mapDbNodes(n.children)
      }));
    };
    
    loadedRoadmapRootNodes.value = mapDbNodes(roadmap.nodes) || [];

    expandedKeys.value = [];
    defaultExpandedKeys.value = [];
    isTreeVisible.value = false;
    nextTick(() => {
      isTreeVisible.value = true;
    });
  } catch (err) {
    ElMessage.error("Lỗi khi tải chi tiết roadmap");
  }
};

const loadNode = async (node, resolve) => {
  if (node.level === 0) {
    // load pharse
    if (loadedRoadmapRootNodes.value.length > 0) {
      // Load pre-fetched roots
      let roots = loadedRoadmapRootNodes.value;
      roots = roots.map(p => ({ ...p, isLeaf: p.nodeType === "TASK" }));
      resolve(roots);
      loadedRoadmapRootNodes.value = []; // clear after loading root
      return;
    }
  } else {
    // Nếu node đã có sẵn children trong DB đã load
    if (node.data.children && node.data.children.length > 0) {
      let children = (node.data.children || []).map((c, idx) => ({ 
        ...c, 
        title: cleanNodeTitle(c.title),
        estimatedHours: c.estimatedHours || 0.0,
        isLeaf: c.nodeType === "TASK" 
      }));
      resolve(children);
      return;
    }

    // Expand children
    try {
      const res = await expandNode({ nodeId: node.data.id });
      let children = res.data?.data?.children || res.data?.children || [];
      children = children.map((c, idx) => ({
        ...c,
        title: cleanNodeTitle(c.title),
        estimatedHours: c.estimatedHours || 0.0,
        isLeaf: c.nodeType === 'TASK'
      }));
      resolve(children);
    } catch (err) {
      console.error("Load node detail error:", err);
      ElMessage.error("Lỗi tải chi tiết cho node");
      resolve([]);
    }
  }
};

const generateRoadmap = async () => {
  if (!configForm.value.positionId || !configForm.value.batchId) {
    ElMessage.warning("Vui lòng chọn Position và Internship Batch")
    return;
  }
  isTreeVisible.value = true
  isGenerating.value = true
  try {
    const res = await generatePhases(configForm.value);
    let phases = res.data?.data || res.data || [];
    if (phases.length > 0 && phases[0].roadmapId) {
      configForm.value.id = phases[0].roadmapId;
    }
    phases = phases.map((p, idx) => ({ 
      ...p, 
      title: cleanNodeTitle(p.title),
      estimatedHours: p.estimatedHours || 0.0,
      isLeaf: p.nodeType === "TASK"  
    }));
    loadedRoadmapRootNodes.value = phases
  } catch (err) {
    console.error("Generate roadmap error:", err)
    ElMessage.error("Lỗi khi tạo roadmap phases")
  } finally {
    isGenerating.value = false
  }
};

const cancelRoadmap = () => {
  ElMessageBox.confirm(
    "Are you sure you want to cancel and delete this roadmap? This action cannot be undone.",
    "Warning",
    {
      confirmButtonText: "OK",
      cancelButtonText: "Cancel",
      type: "warning",
    }
  ).then(async () => {
    try {
      if (configForm.value.id) {
        await deleteRoadmap(configForm.value.id);
      }
      resetForm()
      await loadRoadmaps();
      ElMessage.success("Roadmap has been deleted and canceled.");
    } catch (error) {
      console.error("Cancel roadmap error:", error);
      ElMessage.error("Lỗi khi hủy/xóa roadmap");
    }
  }).catch(() => {
    // cancelled action
  });
};

const resetForm = () => {
  // reset form
  configForm.value = {
    id: null,
    positionId: null,
    batchId: null,
    duration: 2,
    prompt: ""
  };
  selectedRoadmapId.value = null;
  loadedRoadmapRootNodes.value = [];
  expandedKeys.value = [];
  defaultExpandedKeys.value = [];
  isTreeVisible.value = false;
  treeData.value = [];
  loadRoadmaps();

  ElMessage.success("Đã quay lại trạng thái ban đầu");
}

const extractTreeData = (nodes) => {
  return nodes.map((node, index) => {
    return {
      title: node.data.title,
      description: node.data.description,
      nodeType: node.data.nodeType,
      estimatedHours: node.data.estimatedHours || 0.0,
      orderIndex: index,
      children: extractTreeData(node.childNodes || [])
    };
  });
};

const saveDraft = async (isConfirm = false) => {
  if (!treeRef.value) return;
  try {
    const rootNodes = treeRef.value.root.childNodes;
    const treePayload = extractTreeData(rootNodes);
    
    const response = await saveDraftRoadmap({
      id: configForm.value.id,
      title: configForm.value.prompt,
      durationMonth: configForm.value.duration,
      positionId: configForm.value.positionId,
      batchId: configForm.value.batchId,
      publish: isConfirm,
      nodes: treePayload
    });
    
    if (response?.data?.data?.roadmapId) {
      configForm.value.id = response.data.data.roadmapId;
    }
    
    if (isConfirm) {
      ElMessage.success("Roadmap đã được lưu và xác nhận thành công!");
      resetForm();
    } else {
      ElMessage.success("Bản nháp đã được lưu thành công!");
    }
  } catch (error) {
    console.error("Save roadmap error:", error);
    ElMessage.error(isConfirm ? "Lỗi khi lưu và xác nhận roadmap" : "Lỗi khi lưu bản nháp");
  }
};

const saveAndConfirm = () => {
  saveDraft(true);
};

const editForm = ref({
  title: "",
  description: "",
  estimatedHours: 0.0,
});
let currentNodeData = null;

const generateAI = async (node, data) => {
  const hasChildren = node.childNodes && node.childNodes.length > 0;
  const message = hasChildren
    ? '<div style="color: #f56c6c; font-weight: 600; margin-bottom: 8px;">⚠️ Cảnh báo: Hành động này sẽ xóa tất cả các node con liên quan.</div><div>Nhập yêu cầu điều chỉnh cho AI (tùy chọn):</div>'
    : 'Nhập yêu cầu điều chỉnh cho AI (tùy chọn):';

  try {
    const { value: promptText } = await ElMessageBox.prompt(message, 'Sinh AI gợi ý', {
      confirmButtonText: 'Đồng ý',
      cancelButtonText: 'Hủy',
      dangerouslyUseHTMLString: true,
      inputType: 'textarea',
      inputAttributes: {
        rows: 7
      },
      inputPlaceholder: 'Ví dụ: Tập trung vào Java Spring Boot và RESTful API...',
    });

    const loadingInstance = ElLoading.service({
      lock: true,
      text: 'Đang sinh gợi ý từ AI...',
      background: 'rgba(255, 255, 255, 0.7)',
    });

    try {
      await expandNode({
        nodeId: data.id,
        prompt: promptText || ""
      });

      if (!expandedKeys.value.includes(data.id)) {
        expandedKeys.value.push(data.id);
      }

      await refreshTree();

      ElMessage.success("Sinh AI gợi ý thành công!");
    } finally {
      loadingInstance.close();
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error("AI generation error:", error);
      ElMessage.error("Lỗi khi sinh AI gợi ý");
    }
  }
};

const addRootNode = () => {
  currentAddParentId.value = null;
  addForm.value = {
    title: "",
    description: "",
    estimatedHours: 1.0
  };
  addDialogVisible.value = true;
  nextTick(() => {
    if (addFormRef.value) {
      addFormRef.value.clearValidate();
    }
  });
};

const addChild = (data) => {
  currentAddParentId.value = data.id;
  addForm.value = {
    title: "",
    description: "",
    estimatedHours: 1.0
  };
  addDialogVisible.value = true;
  nextTick(() => {
    if (addFormRef.value) {
      addFormRef.value.clearValidate();
    }
  });
};

const submitAddNode = async () => {
  if (!addFormRef.value) return;
  try {
    await addFormRef.value.validate();
    
    await addNode({
      roadmapId: configForm.value.id,
      parentId: currentAddParentId.value,
      title: addForm.value.title,
      description: addForm.value.description,
      estimatedHours: addForm.value.estimatedHours
    });
    
    if (currentAddParentId.value && !expandedKeys.value.includes(currentAddParentId.value)) {
      expandedKeys.value.push(currentAddParentId.value);
    }
    
    ElMessage.success("Thêm Node thành công");
    addDialogVisible.value = false;
    await refreshTree();
  } catch (error) {
    if (error && typeof error !== 'boolean') {
      console.error("Add node error:", error);
      ElMessage.error("Lỗi khi thêm Node");
    }
  }
};

const editNode = (data) => {
  currentNodeData = data;
  editForm.value = {
    title: data.title,
    description: data.description,
    estimatedHours: data.estimatedHours || 0.0,
  };
  dialogVisible.value = true;
};

const saveEdit = async () => {
  if (currentNodeData) {
    try {
      const res = await editNodeDetail(currentNodeData.id, editForm.value);
      const updatedNode = res.data?.data || res.data;
      currentNodeData.title = cleanNodeTitle(updatedNode.title);
      currentNodeData.description = updatedNode.description;
      currentNodeData.estimatedHours = updatedNode.estimatedHours || 0.0;
      dialogVisible.value = false;
      ElMessage.success("Cập nhật Node thành công");
    } catch (error) {
      console.error("Edit node error:", error);
      ElMessage.error("Lỗi khi cập nhật Node");
    }
  }
};

const deleteNode = (node, data) => {
  ElMessageBox.confirm(
    "Are you sure you want to delete this node?",
    "Warning",
    {
      confirmButtonText: "OK",
      cancelButtonText: "Cancel",
      type: "warning",
    }
  )
    .then(async () => {
      try {
        await removeNode(data.id);
        ElMessage.success("Xóa Node thành công");
        if (treeRef.value) {
          treeRef.value.remove(node);
        }
      } catch (error) {
        console.error("Delete node error:", error);
        ElMessage.error("Lỗi khi xóa Node");
      }
    })
    .catch(() => {
      ElMessage({
        type: "info",
        message: "Hủy xóa",
      });
    });
};

const refreshTree = async () => {
  if (!configForm.value.id) return;
  try {
    const res = await fetchRoadmapById(configForm.value.id);
    const draft = res.data?.data || res.data;
    
    const mapDbNodes = (nodes) => {
      if (!nodes) return [];
      return nodes.map((n, idx) => ({
        ...n,
        title: cleanNodeTitle(n.title),
        estimatedHours: n.estimatedHours || 0.0,
        isDbLoaded: true,
        children: mapDbNodes(n.children)
      }));
    };
    
    loadedRoadmapRootNodes.value = mapDbNodes(draft.nodes) || [];
    
    isTreeVisible.value = false;
    defaultExpandedKeys.value = [...expandedKeys.value];
    nextTick(() => {
      isTreeVisible.value = true;
    });
  } catch (err) {
    console.error("Failed to refresh tree", err);
  }
};

/**
 * Kiểm soát việc thả node:
 * - TASK không thể bị thả vào bên trong (inner) bất kỳ node nào
 * - Không thể thả bất kỳ node nào vào bên trong TASK
 */
const allowDrop = (draggingNode, dropNode, type) => {
  if (draggingNode.data.nodeType === 'TASK' && type === 'inner') {
    return false;
  }
  if (dropNode.data.nodeType === 'TASK' && type === 'inner') {
    return false;
  }
  return true;
};

// xu li move node
const handleDrop = async (draggingNode, dropNode, dropType, ev) => {
  try {
    await moveNode(draggingNode.data.id, {
      targetNodeId: dropNode.data.id,
      dropType: dropType
    });
    ElMessage.success("Di chuyển Node thành công");
    await refreshTree();
  } catch (err) {
    console.error("Move node error:", err);
    ElMessage.error("Lỗi khi di chuyển Node");
  }
};
</script>

<style scoped>
.roadmap-builder-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.main-layout-row {
  flex: 1;
  height: 100%;
  overflow: hidden;
}

.layout-col {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.full-height-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.box-card {
  display: flex;
  flex-direction: column;
}

.flex-1 {
  flex: 1;
  overflow: hidden;
}

.mb-3 {
  margin-bottom: 12px;
}

:deep(.flex-1 .el-card__body) {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.subtitle {
  color: #666;
  font-size: 13px;
  margin: 2px 0 10px 0;
}

.config-form {
  margin-top: 10px;
}

.roadmap-tree {
  margin-top: 10px;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 8px;
  padding-top: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
  min-width: 0;
}

.node-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  min-width: 0;
  padding-right: 12px;
}

.node-title {
  font-weight: 600;
  font-size: 15px;
  color: #303133;
  white-space: normal;
  word-break: break-word;
}

.node-desc {
  font-size: 13px;
  color: #909399;
  white-space: normal;
  word-break: break-word;
}

.node-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-shrink: 0;
}

:deep(.el-tree-node__content) {
  height: auto !important;
  align-items: flex-start;
}

:deep(.el-tree-node__expand-icon) {
  margin-top: 10px;
}
</style>
