<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useLocaleStore } from '@/locales/locale'
import MentorLayout from '@/layouts/dashboard/MentorLayout.vue'
import StatCard from '@/components/dashboard/StatCard.vue'
import DonutChart from '@/components/dashboard/DonutChart.vue'
import { getInternsNotEvaluatedThisWeek, getMyIntern } from '@/api/intern'
import { getMentorStatistics, getMentorInternStatusDistribution } from '@/api/dashboard'
import { useLoading, useApi } from '@/composables'
import { fetchRoadmaps, fetchRoadmapById } from "@/api/roadmap"
import { getPositions } from "@/api/position"
import { getBatches } from "@/api/internship-batch"
import { ElMessage } from "element-plus"
import {
  CollectionTag,
  Folder,
  Document,
  List,
  Check,
  Clock,
  Briefcase,
  Calendar,
  Refresh
} from "@element-plus/icons-vue"

const router = useRouter()
const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)

const { loading, withLoading } = useLoading()
const { execute: executeApi } = useApi({
  showErrorMessage: false
})

// Data refs
const statistics = ref({
  totalInterns: 0,
  activeInterns: 0,
  warningInterns: 0,
  pendingReports: 0
})
const internsNeedEvaluation = ref([])
const totalNeedEvaluation = ref(0)
const internsUnderSupervision = ref([])
const internStatusDistribution = ref([])

// Pagination for interns need evaluation
const evalCurrentPage = ref(1)
const evalPageSize = ref(5)

// Stats cards configuration
const stats = computed(() => [
  {
    key: 'totalInterns',
    value: statistics.value.totalInterns,
    icon: 'users',
    color: 'blue'
  },
  {
    key: 'activeInterns',
    value: statistics.value.activeInterns,
    icon: 'active',
    color: 'green'
  },
  {
    key: 'warningInterns',
    value: statistics.value.warningInterns,
    icon: 'warning',
    color: 'yellow'
  },
  {
    key: 'pendingReports',
    value: statistics.value.pendingReports,
    icon: 'mentor',
    color: 'purple'
  }
])

// Chart data for intern status distribution
const chartData = computed(() => {
  if (internStatusDistribution.value.length > 0) {
    const colors = {
      'ACTIVE': '#3b82f6',
      'WARNING': '#f59e0b',
      'DROPPED': '#ef4444',
      'COMPLETED': '#22c55e'
    }
    return internStatusDistribution.value.map(item => ({
      label: t.value(`internManagement.status.${item.label}`) || item.label,
      value: item.value,
      color: colors[item.label] || '#6b7280'
    }))
  }
  // Fallback to statistics data
  return [
    {
      label: t.value('internManagement.status.ACTIVE'),
      value: statistics.value.activeInterns,
      color: '#3b82f6'
    },
    {
      label: t.value('internManagement.status.WARNING'),
      value: statistics.value.warningInterns,
      color: '#f59e0b'
    }
  ]
})

const evaluationProgress = computed(() => {
  const total = internsUnderSupervision.value.length
  const needEval = totalNeedEvaluation.value
  const completed = total - needEval
  return { completed: completed >= 0 ? completed : 0, total }
})

const progressPercentage = computed(() => {
  if (evaluationProgress.value.total === 0) return 0
  return (evaluationProgress.value.completed / evaluationProgress.value.total) * 100
})

const calculateWeekNumber = (startDate) => {
  if (!startDate) return 1
  const start = new Date(startDate)
  const now = new Date()
  const diffTime = Math.abs(now - start)
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return Math.max(1, Math.ceil(diffDays / 7))
}

const handleSubmitReport = (intern) => {
  router.push(`/mentor/my-interns/${intern.id}/edit?tab=reports`)
}

async function fetchInternsNeedEvaluation() {
  const res = await executeApi(() => getInternsNotEvaluatedThisWeek({ 
    page: evalCurrentPage.value - 1, 
    limit: evalPageSize.value 
  }))
  internsNeedEvaluation.value = res.data?.data?.items || []
  totalNeedEvaluation.value = res.data?.data?.totalItems || 0
}

function handleEvalPageChange(page) {
  evalCurrentPage.value = page
  fetchInternsNeedEvaluation()
}

async function fetchData() {
  await withLoading(async () => {
    const [statisticsRes, statusDistRes, notEvalRes, myInternsRes] = await Promise.all([
      executeApi(() => getMentorStatistics()),
      executeApi(() => getMentorInternStatusDistribution()),
      executeApi(() => getInternsNotEvaluatedThisWeek({ page: 0, limit: evalPageSize.value })),
      executeApi(() => getMyIntern({ limit: 100 }))
    ])
    
    if (statisticsRes.data?.data) {
      statistics.value = statisticsRes.data.data
    }
    if (statusDistRes.data?.data?.items) {
      internStatusDistribution.value = statusDistRes.data.data.items
    }
    internsNeedEvaluation.value = notEvalRes.data?.data?.items || []
    totalNeedEvaluation.value = notEvalRes.data?.data?.totalItems || 0
    internsUnderSupervision.value = myInternsRes.data?.data?.items || []
  })
}

// Roadmap Detail State & Handlers
const roadmapDetailDialogVisible = ref(false)
const selectedIntern = ref(null)
const selectedRoadmap = ref(null)
const roadmapLoading = ref(false)
const roadmapTreeData = ref([])
const positions = ref([])
const batches = ref([])
const hasLoadedMeta = ref(false)

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

function getStatusType(status) {
  if (!status) return "info"
  const map = {
    ACTIVE: "success",
    WARNING: "warning",
    COMPLETED: "primary",
    DROPPED: "danger"
  }
  return map[status.toUpperCase()] || "info"
}

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

async function fetchMetadata() {
  if (hasLoadedMeta.value) return
  try {
    const [resPos, resBatches] = await Promise.allSettled([
      getPositions({ limit: 100 }),
      getBatches({ limit: 100 })
    ])
    if (resPos.status === "fulfilled") {
      const body = resPos.value.data
      positions.value = body?.data?.items || body || []
    }
    if (resBatches.status === "fulfilled") {
      const body = resBatches.value.data
      batches.value = body?.data?.items || body || []
    }
    hasLoadedMeta.value = true
  } catch (err) {
    console.error("Failed to load metadata", err)
  }
}

async function handleViewRoadmap(intern) {
  selectedIntern.value = intern
  roadmapDetailDialogVisible.value = true
  roadmapLoading.value = true
  selectedRoadmap.value = null
  roadmapTreeData.value = []

  try {
    await fetchMetadata()

    // 1. Fetch all roadmaps
    const listRes = await fetchRoadmaps()
    const allRoadmaps = listRes.data?.data || listRes.data || []
    
    // 2. Find roadmap for intern's position and batch
    const internPosId = intern.position?.id
    const internBatchId = intern.internshipBatch?.id || intern.batchId
    
    const matchedRoadmap = allRoadmaps.find(
      r => r.positionId === internPosId && r.batchId === internBatchId
    ) || allRoadmaps.find(
      r => r.positionId === internPosId
    )
    
    if (!matchedRoadmap) {
      ElMessage.warning("Không tìm thấy lộ trình phù hợp cho vị trí / đợt thực tập này.")
      roadmapLoading.value = false
      return
    }
    
    // 3. Fetch detailed roadmap
    const detailRes = await fetchRoadmapById(matchedRoadmap.id)
    const roadmapData = detailRes.data?.data || detailRes.data
    selectedRoadmap.value = roadmapData
    
    const rootNodes = roadmapData.nodes || roadmapData.children || []
    roadmapTreeData.value = transformNodes(rootNodes)
  } catch (err) {
    console.error("Lỗi khi tải chi tiết roadmap", err)
    ElMessage.error("Không thể tải thông tin lộ trình")
  } finally {
    roadmapLoading.value = false
  }
}

onMounted(fetchData)
</script>

<template>
  <MentorLayout>
    <div class="mentor-dashboard" v-loading="loading">

      <!-- Statistics Cards -->
      <el-row :gutter="20" class="mb-4">
        <el-col
          :xs="12"
          :sm="6"
          :md="6"
          v-for="stat in stats"
          :key="stat.key"
          class="mb-col"
        >
          <StatCard
            :title="t('mentorDashboard.statistics.' + stat.key)"
            :value="stat.value"
            :icon="stat.icon"
            :color="stat.color"
            class="stat-card-hover"
          />
        </el-col>
      </el-row>

      <!-- Charts Row -->
      <el-row :gutter="20" class="mb-4 equal-height-row">
        <el-col :xs="24" :md="12" class="mb-col">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <span class="card-title">{{ t('mentorDashboard.internsByStatus') }}</span>
            </template>
            <div class="chart-container">
              <DonutChart :data="chartData" :height="250" />
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :md="12" class="mb-col">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <span class="card-title">{{ t('mentorDashboard.evaluationProgress') }}</span>
            </template>
            <div class="progress-container">
              <div class="progress-info">
                {{ t('mentorDashboard.reportsCompleted').replace('{completed}', evaluationProgress.completed).replace('{total}', evaluationProgress.total) }}
              </div>
              <el-progress 
                :percentage="progressPercentage" 
                :stroke-width="20"
                color="#22c55e"
                :format="() => `${evaluationProgress.completed}/${evaluationProgress.total}`"
              />
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- Interns Need Evaluation -->
      <section class="section" v-if="totalNeedEvaluation > 0">
        <el-card shadow="hover" class="eval-card">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">
                {{ t('mentorDashboard.internsNeedEvaluation') }}
                <el-badge :value="totalNeedEvaluation" type="danger" class="count-badge" />
              </span>
            </div>
          </template>
          <el-table :data="internsNeedEvaluation" stripe size="small" style="width: 100%">
            <el-table-column 
              prop="fullName" 
              :label="t('mentorDashboard.table.fullName')" 
              min-width="150"
            />
            <el-table-column 
              :label="t('mentorDashboard.position')" 
              min-width="120"
            >
              <template #default="scope">
                <el-tag type="info" size="small">{{ scope.row.position?.title }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column 
              :label="t('mentorDashboard.week')" 
              min-width="80"
              align="center"
            >
              <template #default="scope">
                <span class="week-badge">{{ calculateWeekNumber(scope.row.startDate) }}</span>
              </template>
            </el-table-column>
            <el-table-column 
              :label="t('common.actions')" 
              min-width="180"
              align="center"
              fixed="right"
            >
              <template #default="scope">
                <div class="table-actions">
                  <el-button 
                    type="success" 
                    size="small"
                    @click="handleSubmitReport(scope.row)"
                  >
                    {{ t('mentorDashboard.submitReport') }}
                  </el-button>
                  <el-button 
                    type="primary" 
                    plain
                    size="small"
                    @click="handleViewRoadmap(scope.row)"
                  >
                    Xem Roadmap
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrapper" v-if="totalNeedEvaluation > evalPageSize">
            <el-pagination
              :current-page="evalCurrentPage"
              :page-size="evalPageSize"
              :total="totalNeedEvaluation"
              layout="prev, pager, next"
              background
              small
              @current-change="handleEvalPageChange"
            />
          </div>
        </el-card>
      </section>

      <!-- Interns Under Supervision -->
      <section class="section" v-if="internsUnderSupervision.length > 0">
        <el-card shadow="hover" class="eval-card">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">
                Thực tập sinh đang quản lý
                <el-badge :value="internsUnderSupervision.length" type="primary" class="count-badge" />
              </span>
            </div>
          </template>
          <el-table :data="internsUnderSupervision" stripe size="small" style="width: 100%">
            <el-table-column 
              prop="fullName" 
              label="Họ và tên" 
              min-width="150"
            />
            <el-table-column 
              label="Vị trí" 
              min-width="120"
            >
              <template #default="scope">
                <el-tag type="info" size="small">{{ scope.row.position?.title }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column 
              label="Đợt thực tập" 
              min-width="150"
            >
              <template #default="scope">
                <span>{{ scope.row.internshipBatch?.name }}</span>
              </template>
            </el-table-column>
            <el-table-column 
              label="Trạng thái" 
              min-width="120"
              align="center"
            >
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.internStatus)" size="small">
                  {{ scope.row.internStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column 
              label="Hành động" 
              min-width="180"
              align="center"
              fixed="right"
            >
              <template #default="scope">
                <div class="table-actions">
                  <el-button 
                    type="primary" 
                    plain
                    size="small"
                    @click="handleViewRoadmap(scope.row)"
                  >
                    Xem Roadmap
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </section>

      <!-- Roadmap Detail Dialog -->
      <el-dialog
        v-model="roadmapDetailDialogVisible"
        :title="`Chi tiết lộ trình học tập - ${selectedIntern?.fullName || ''}`"
        width="60%"
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
            <el-empty description="Không có lộ trình nào được tìm thấy cho thực tập sinh này." />
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

<style scoped>
.mentor-dashboard {
  max-width: 100%;
  margin: 0 auto;
  padding: 0 10px 20px 10px;
  font-family: 'Inter', sans-serif;
}

.mb-4 {
  margin-bottom: 24px;
}

.mb-col {
  margin-bottom: 16px;
}

.stat-card-hover {
  width: 100%;
  height: 100%;
  transition: transform 0.2s, box-shadow 0.2s;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.stat-card-hover:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.equal-height-row {
  display: flex;
  flex-wrap: wrap;
}

.equal-height-row .el-col {
  display: flex;
}

.chart-card,
.eval-card {
  width: 100%;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  transition: all 0.3s;
}

.chart-card:hover,
.eval-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.06);
  border-color: #bfdbfe;
}

.chart-card :deep(.el-card__header),
.eval-card :deep(.el-card__header) {
  padding: 12px 16px;
  border-bottom: 1px solid #f9fafb;
  background: #fff;
}

.chart-card :deep(.el-card__body),
.eval-card :deep(.el-card__body) {
  padding: 16px;
  flex: 1;
}

.card-header-flex {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #374151;
  display: flex;
  align-items: center;
  gap: 8px;
}

.count-badge :deep(.el-badge__content) {
  font-size: 11px;
}

.chart-container {
  min-height: 250px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.progress-container {
  min-height: 250px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 20px;
}

.progress-info {
  font-size: 16px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 20px;
  text-align: center;
}

.section {
  margin-bottom: 24px;
}

.week-badge {
  display: inline-block;
  background: #f3f4f6;
  color: #374151;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 13px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
}

:deep(.el-table) {
  border-radius: 8px;
}

:deep(.el-table th) {
  background: #f9fafb !important;
  font-weight: 600;
  color: #6b7280;
}

@media (max-width: 992px) {
  .mb-col {
    margin-bottom: 20px;
  }
  .equal-height-row {
    display: block;
  }
}

.table-actions {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.roadmap-dialog-content {
  min-height: 200px;
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
  max-height: 450px;
  overflow-y: auto;
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

