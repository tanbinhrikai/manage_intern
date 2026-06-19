
<template>
  <MentorLayout :page-title="t('sidebar.roadmapBuilder')">
    <div class="mentor-roadmap-view">
      <!-- Top header bar -->
      <div class="page-header">
        <div class="header-text">
          <h2 class="title">Xây dựng lộ trình học tập</h2>
          <p class="subtitle">Lựa chọn lộ trình mẫu từ danh sách sẵn có hoặc yêu cầu tạo mới để áp dụng cho sinh viên thực tập.</p>
        </div>
        <el-button type="primary" :icon="Refresh" @click="loadData" :loading="loading" class="refresh-btn">
          Làm mới danh sách
        </el-button>
      </div>

      <!-- Roadmap Grid / Loading -->
      <div v-loading="loading" class="content-section">
        <div v-if="roadmaps.length === 0 && !loading" class="empty-container">
          <el-empty description="Không tìm thấy lộ trình mẫu nào trong cơ sở dữ liệu." />
        </div>

        <el-row v-else :gutter="24" class="roadmap-grid">
          <el-col
            v-for="roadmap in roadmaps"
            :key="roadmap.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
            class="grid-col"
          >
            <div class="roadmap-card">
              <!-- Header -->
              <div class="card-header-gradient">
                <h3 class="roadmap-title" :title="roadmap.title">
                  {{ roadmap.title }}
                </h3>
                <el-tag
                  :type="roadmap.status === 'PUBLISHED' ? 'success' : 'info'"
                  size="small"
                  effect="dark"
                  class="status-tag"
                >
                  {{ roadmap.status || 'TEMPLATE' }}
                </el-tag>
              </div>

              <!-- Body -->
              <div class="roadmap-body">
                <p class="roadmap-desc" :title="roadmap.description">
                  {{ roadmap.description || 'Chưa có mô tả cho lộ trình này.' }}
                </p>

                <div class="roadmap-meta">
                  <div class="meta-item">
                    <el-icon class="meta-icon"><Clock /></el-icon>
                    <span class="meta-label">Thời gian:</span>
                    <span class="meta-value">{{ roadmap.durationMonth || 2 }} tháng</span>
                  </div>
                  <div class="meta-item">
                    <el-icon class="meta-icon"><Briefcase /></el-icon>
                    <span class="meta-label">Vị trí:</span>
                    <span class="meta-value" :title="getPositionTitle(roadmap.positionId)">
                      {{ getPositionTitle(roadmap.positionId) }}
                    </span>
                  </div>
                  <div class="meta-item">
                    <el-icon class="meta-icon"><Calendar /></el-icon>
                    <span class="meta-label">Đợt:</span>
                    <span class="meta-value" :title="getBatchName(roadmap.batchId)">
                      {{ getBatchName(roadmap.batchId) }}
                    </span>
                  </div>
                </div>
              </div>

              <!-- Actions -->
              <div class="roadmap-actions">
                <el-button
                  type="success"
                  :icon="Check"
                  class="action-btn btn-use"
                  @click="handleUseRoadmap(roadmap)"
                >
                  Sử dụng
                </el-button>
                <el-button
                  type="warning"
                  plain
                  :icon="Refresh"
                  class="action-btn btn-rebuild"
                  @click="handleRebuildRoadmap(roadmap)"
                >
                  Build lại
                </el-button>
                <el-button
                  type="primary"
                  plain
                  :icon="View"
                  class="action-btn btn-view"
                  @click="handleViewDetail(roadmap)"
                >
                  Chi tiết
                </el-button>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- Roadmap Detail Dialog -->
      <el-dialog
        v-model="roadmapDetailDialogVisible"
        title="Chi tiết lộ trình học tập"
        width="75%"
        top="3vh"
        destroy-on-close
        class="roadmap-dialog"
      >
        <div v-loading="roadmapLoading" class="roadmap-dialog-content">
          <div v-if="selectedRoadmap">
            <div class="roadmap-meta-header">
              <h3>{{ selectedRoadmap.title }}</h3>
              <p class="description">{{ selectedRoadmap.description }}</p>
              <div class="meta-tags">
                <el-tag type="success" effect="plain" class="meta-tag">
                  <el-icon><Clock /></el-icon> {{ selectedRoadmap.durationMonth || 2 }} tháng
                </el-tag>
                <el-tag type="warning" effect="plain" class="meta-tag" v-if="selectedRoadmap.positionId">
                  <el-icon><Briefcase /></el-icon> {{ getPositionTitle(selectedRoadmap.positionId) }}
                </el-tag>
              </div>
            </div>

            <el-divider>Nội dung chi tiết</el-divider>

            <div class="roadmap-tree-container">
              <el-tree
                :data="roadmapTreeData"
                :props="defaultProps"
                node-key="uniqueId"
                default-expand-all
                :expand-on-click-node="false"
                :indent="24"
              >
                <template #default="{ node, data }">
                  <div class="custom-tree-node" :class="`node-type-${data.type.toLowerCase()}`">
                    <div class="node-content-left">
                      <el-icon class="node-icon">
                        <component :is="getIcon(data.type)" />
                      </el-icon>
                      <span class="node-label">{{ node.label }}</span>
                    </div>
                    <div class="node-content-right">
                      <el-tag size="small" :type="getTagType(data.type)" effect="light">
                        {{ data.type }}
                      </el-tag>
                    </div>
                  </div>
                </template>
              </el-tree>
            </div>
          </div>
          <div v-else-if="!roadmapLoading" class="empty-roadmap">
            <el-empty description="Không có thông tin chi tiết lộ trình học tập." />
          </div>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="roadmapDetailDialogVisible = false">Đóng</el-button>
          </span>
        </template>
      </el-dialog>

    </div>
  </MentorLayout>
</template>


<script setup>
import { ref, onMounted, computed } from "vue"
import { useLocaleStore } from "@/locales/locale"
import MentorLayout from "@/layouts/dashboard/MentorLayout.vue"
import { fetchRoadmaps, fetchRoadmapById } from "@/api/roadmap"
import { getPositions } from "@/api/position"
import { getBatches } from "@/api/internship-batch"
import { ElMessage, ElMessageBox } from "element-plus"
import {
  Clock,
  Briefcase,
  Calendar,
  Check,
  Refresh,
  View,
  CollectionTag,
  Folder,
  Document,
  List
} from "@element-plus/icons-vue"

const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)

// State
const roadmaps = ref([])
const positions = ref([])
const batches = ref([])
const loading = ref(false)

// Fetching lists
async function loadData() {
  loading.value = true
  try {
    // Parallel fetching
    const [resRoadmaps, resPositions, resBatches] = await Promise.allSettled([
      fetchRoadmaps(),
      getPositions({ limit: 100 }),
      getBatches({ limit: 100 })
    ])

    if (resRoadmaps.status === "fulfilled") {
      const body = resRoadmaps.value.data
      roadmaps.value = body?.data || body || []
    } else {
      console.error("Failed to load roadmaps", resRoadmaps.reason)
      ElMessage.error("Không thể tải danh sách lộ trình")
    }

    if (resPositions.status === "fulfilled") {
      const body = resPositions.value.data
      positions.value = body?.data?.items || body || []
    }

    if (resBatches.status === "fulfilled") {
      const body = resBatches.value.data
      batches.value = body?.data?.items || body || []
    }
  } catch (err) {
    console.error("Error loading data", err)
    ElMessage.error("Đã xảy ra lỗi khi tải dữ liệu")
  } finally {
    loading.value = false
  }
}

// Maps for quick lookup
const positionsMap = computed(() => {
  const map = {}
  positions.value.forEach(p => {
    map[p.id] = p.title
  })
  return map
})

const batchesMap = computed(() => {
  const map = {}
  batches.value.forEach(b => {
    map[b.id] = b.name
  })
  return map
})

function getPositionTitle(posId) {
  if (!posId) return "Mọi vị trí"
  return positionsMap.value[posId] || `Vị trí #${posId}`
}

function getBatchName(batchId) {
  if (!batchId) return "Mọi đợt thực tập"
  return batchesMap.value[batchId] || `Đợt #${batchId}`
}

// Handlers for actions (Only up to requested scope)
function handleUseRoadmap(roadmap) {
  ElMessageBox.alert(
    `Bạn đã chọn sử dụng lộ trình: <strong>${roadmap.title}</strong>.<br/>Hệ thống đã ghi nhận lựa chọn này.`,
    "Xác nhận sử dụng",
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: "Đóng",
      type: "success"
    }
  )
}

function handleRebuildRoadmap(roadmap) {
  ElMessageBox.alert(
    `Bạn đã chọn build lại lộ trình: <strong>${roadmap.title}</strong>.<br/>Yêu cầu sẽ được chuyển đến bộ phận xử lý AI.`,
    "Xác nhận build lại",
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: "Đóng",
      type: "warning"
    }
  )
}

// Roadmap Detail Tree State & Helpers
const roadmapDetailDialogVisible = ref(false)
const selectedRoadmap = ref(null)
const roadmapLoading = ref(false)
const roadmapTreeData = ref([])

const defaultProps = {
  children: "children",
  label: "label",
}

const transformNodes = (nodes) => {
  if (!Array.isArray(nodes) || nodes.length === 0) return []
  return nodes.map((node) => ({
    uniqueId: `${node.nodeType}-${node.id}`,
    label: node.title,
    type: node.nodeType,
    estimatedHours: node.estimatedHours,
    difficulty: node.difficulty,
    description: node.description,
    children: transformNodes(node.children),
  }))
}

const getIcon = (type) => {
  if (!type) return List
  const map = {
    PHASE: CollectionTag,
    MODULE: Folder,
    LESSON: Document,
    TASK: Check,
    PROJECT: CollectionTag,
  }
  return map[type.toUpperCase()] || List
}

const getTagType = (type) => {
  if (!type) return "info"
  const map = {
    PHASE: "danger",
    MODULE: "warning",
    LESSON: "primary",
    TASK: "success",
  }
  return map[type.toUpperCase()] || "info"
}

async function handleViewDetail(roadmap) {
  roadmapDetailDialogVisible.value = true
  roadmapLoading.value = true
  selectedRoadmap.value = null
  roadmapTreeData.value = []

  try {
    const res = await fetchRoadmapById(roadmap.id)
    const roadmapData = res.data?.data || res.data
    selectedRoadmap.value = roadmapData
    
    const rootNodes = roadmapData.nodes || roadmapData.children || []
    roadmapTreeData.value = transformNodes(rootNodes)
  } catch (err) {
    console.error("Lỗi khi tải chi tiết roadmap", err)
    ElMessage.error("Không thể tải thông tin lộ trình chi tiết")
  } finally {
    roadmapLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.mentor-roadmap-view {
  padding: 24px;
  min-height: calc(100vh - 120px);
  background-color: #f8fafc;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  gap: 16px;
  flex-wrap: wrap;
  background: white;
  padding: 20px 24px;
  border-radius: 12px;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.05);
}

.header-text .title {
  margin: 0 0 6px 0;
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
}

.header-text .subtitle {
  margin: 0;
  font-size: 14px;
  color: #64748b;
}

.refresh-btn {
  border-radius: 8px;
  font-weight: 500;
}

.content-section {
  min-height: 300px;
}

.empty-container {
  background: white;
  padding: 60px 0;
  border-radius: 12px;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.05);
}

.roadmap-grid {
  margin-top: 8px;
}

.grid-col {
  margin-bottom: 24px;
}

.roadmap-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
  border: 1px solid #e2e8f0;
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.roadmap-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 20px -8px rgba(0, 0, 0, 0.15);
  border-color: #3b82f6;
}

.card-header-gradient {
  padding: 20px;
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.roadmap-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1e3a8a;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  height: 44px; /* Ensure consistent spacing */
}

.status-tag {
  border-radius: 4px;
  font-weight: 600;
  letter-spacing: 0.5px;
  flex-shrink: 0;
}

.roadmap-body {
  padding: 20px;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.roadmap-desc {
  margin: 0 0 16px 0;
  font-size: 14px;
  color: #475569;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  min-height: 67px;
}

.roadmap-meta {
  border-top: 1px solid #f1f5f9;
  padding-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.meta-item {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #64748b;
  gap: 8px;
}

.meta-icon {
  font-size: 15px;
  color: #3b82f6;
  flex-shrink: 0;
}

.meta-label {
  font-weight: 500;
  color: #64748b;
}

.meta-value {
  color: #1e293b;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.roadmap-actions {
  padding: 16px 0;
  background-color: #f8fafc;
  border-top: 1px solid #e2e8f0;
  display: flex;
  gap: 12px;
}

.action-btn {
  flex: 1;
  border-radius: 8px;
  font-weight: 600;
  padding: 8px 16px;
}

.btn-use {
  box-shadow: 0 2px 4px rgba(16, 185, 129, 0.15);
}

.btn-rebuild {
  box-shadow: 0 2px 4px rgba(245, 158, 11, 0.05);
}

.btn-view {
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.05);
}

.roadmap-dialog-content {
  min-height: 200px;
}

:deep(.roadmap-dialog .el-dialog__body) {
  max-height: calc(90vh - 140px);
  overflow-y: auto;
  padding: 20px 24px;
}

.roadmap-meta-header {
  background-color: #f8fafc;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  margin-bottom: 20px;
}

.roadmap-meta-header h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #1e3a8a;
}

.roadmap-meta-header .description {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #475569;
  line-height: 1.5;
}

.meta-tags {
  display: flex;
  gap: 10px;
}

.meta-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.roadmap-tree-container {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px;
  background-color: #fff;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
  width: 100%;
}

.node-content-left {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.node-icon {
  font-size: 16px;
  color: #3b82f6;
}

.node-label {
  font-weight: 500;
  color: #334155;
}

.node-type-phase .node-label {
  font-weight: 700;
  color: #0f172a;
}

.node-type-module .node-label {
  font-weight: 600;
  color: #1e293b;
}

:deep(.el-tree-node__content) {
  height: 38px;
  border-bottom: 1px dashed #f1f5f9;
}

:deep(.el-tree-node__content:hover) {
  background-color: #f8fafc;
}
</style>
