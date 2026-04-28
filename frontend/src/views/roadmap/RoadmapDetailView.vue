<template>
  <AdminLayout>
    <div class="roadmap-detail-view">
      <el-card>
        <template #header>
          <div class="card-header">
            <h2>{{ roadmap.title || "Chi tiết lộ trình" }}</h2>
            <el-button @click="goBack" plain>Quay lại</el-button>
          </div>
        </template>

        <div v-if="loading" class="loading-state">
          <el-skeleton :rows="5" animated />
        </div>

        <div v-else>
          <div class="meta-info">
            <el-descriptions border :column="1">
              <el-descriptions-item label="Mô tả">
                {{ roadmap.description }}
              </el-descriptions-item>
              <el-descriptions-item label="Ngày tạo">
                {{ formatDate(roadmap.createdAt) }}
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <el-divider content-position="left">Nội dung lộ
            trình</el-divider>

          <div class="tree-container">
            <el-tree :data="treeData" :props="defaultProps"
              node-key="uniqueId" default-expand-all
              :expand-on-click-node="false" :indent="24">
              <template #default="{ node, data }">
                <div class="custom-tree-node"
                  :class="`node-type-${data.type.toLowerCase()}`">
                  <div class="node-content">
                    <el-icon class="node-icon">
                      <component :is="getIcon(data.type)" />
                    </el-icon>

                    <span class="node-label">{{ node.label }}</span>
                  </div>

                  <el-tag size="small" :type="getTagType(data.type)"
                    effect="plain" class="node-tag">
                    {{ data.type }}
                  </el-tag>
                </div>
              </template>
            </el-tree>

            <el-empty v-if="!treeData.length"
              description="Chưa có dữ liệu lộ trình" />
          </div>
        </div>
      </el-card>
    </div>
  </AdminLayout>

</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue";
import { useRoute, useRouter } from "vue-router";
import { fetchRoadmapById } from "@/api/roadmap";
import { ElMessage } from "element-plus";
// Import Icons
import {
  CollectionTag,
  Folder,
  Document,
  List,
  Check,
} from "@element-plus/icons-vue";

const route = useRoute();
const router = useRouter();
const id = Number(route.params.id);

const roadmap = ref({});
const loading = ref(false);

const defaultProps = {
  children: "children",
  label: "label",
};

// recursive data transformation function
// This function will automatically run down to the child levels regardless of the number of levels.
const transformNodes = (nodes) => {
  if (!Array.isArray(nodes) || nodes.length === 0) return [];

  return nodes.map((node) => ({
    uniqueId: `${node.nodeType}-${node.id}`, // Tạo ID duy nhất
    label: node.title,
    type: node.nodeType,
    // Get nodeType directly from API (PHASE, MODULE...)
    // Call itself again to process the next sub-level
    children: transformNodes(node.children),
  }));
};

//data transformation logic
const treeData = computed(() => {
  // Check roadmap.value.children instead of phases
  if (!roadmap.value || !roadmap.value.children) return [];

  // Call the recursive function starting from the top level
  return transformNodes(roadmap.value.children);
});

const getIcon = (type) => {
  if (!type) return List;
  const map = {
    PHASE: CollectionTag,
    MODULE: Folder,
    LESSON: Document,
    TASK: Check,
    PROJECT: CollectionTag,
  };
  return map[type.toUpperCase()] || List;
};

const getTagType = (type) => {
  if (!type) return "info";
  const map = {
    PHASE: "danger",
    MODULE: "warning",
    LESSON: "primary",
    TASK: "success",
  };
  return map[type.toUpperCase()] || "info";
};

function formatDate(dateStr) {
  if (!dateStr) return "-";
  return new Date(dateStr).toLocaleString("vi-VN");
}

async function load() {
  loading.value = true;
  try {
    const res = await fetchRoadmapById(id);
    const body = res.data;
    if (body && body.success) {
      roadmap.value = body.data;
    } else {
      roadmap.value = body || {};
    }
  } catch (e) {
    ElMessage.error("Route details could not be loaded.");
  } finally {
    loading.value = false;
  }
}

function goBack() {
  router.back();
}

onMounted(load);
</script>

<style scoped>
.roadmap-detail-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 1.5rem;
  color: #303133;
}

.meta-info {
  margin-bottom: 24px;
}

/* --- TREE STYLING --- */
.tree-container {
  margin-top: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px;
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

.node-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.node-label {
  font-weight: 500;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 600px;
}

.node-type-phase .node-label {
  font-weight: 700;
  font-size: 15px;
  color: #303133;
}

.node-type-module .node-label {
  font-weight: 600;
  color: #303133;
}

:deep(.el-tree-node__content) {
  height: 40px;
  border-bottom: 1px dashed #f0f2f5;
}

:deep(.el-tree-node__content:hover) {
  background-color: #f5f7fa;
}
</style>
