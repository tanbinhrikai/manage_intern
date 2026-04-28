<template>
  <AdminLayout>
    <div class="roadmap-list-view">
      <el-card>
        <div class="header">
          <h2>Danh sách lộ trình</h2>
          <div>
            <el-button type="primary" @click="fetchList"
              :loading="loading">Làm mới</el-button>
          </div>
        </div>
        <el-table :data="roadmaps" v-loading="loading"
          style="width: 100%; margin-top: 16px">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="Tên lộ trình"
            min-width="200" />
          <el-table-column prop="description" label="Mô tả"
            min-width="250" />
          <el-table-column prop="createdAt" label="Ngày tạo"
            min-width="160">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="Hành động" width="180">
            <template #default="{ row }">
              <el-button size="small" type="primary"
                @click="goToDetail(row.id)">Chi tiết</el-button>
              <el-button size="small" type="danger"
                @click="onDelete(row.id)"
                style="margin-left: 8px">Xóa</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div style="margin-top: 16px; text-align: right">
          <el-pagination background :current-page="page + 1"
            :page-size="size" :page-sizes="[5, 10, 20, 50]"
            :total="total" layout="sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange" />
        </div>
      </el-card>
    </div>
  </AdminLayout>
</template>

<script setup>
import { deleteRoadmap } from "@/api/roadmap";
import { fetchRoadmapList } from "@/api/roadmap-list";
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";

const roadmaps = ref([]);
const loading = ref(false);
const page = ref(0);
const size = ref(10);
const total = ref(0);
const router = useRouter();

function formatDate(dateStr) {
  if (!dateStr) return "-";
  return new Date(dateStr).toLocaleString("vi-VN");
}

async function fetchList(p = page.value, s = size.value) {
  loading.value = true;
  try {
    const res = await fetchRoadmapList(p, s);
    // backend wraps response in ApiResponse { success, message, data }
    const body = res.data;
    if (body && body.success && body.data) {
      roadmaps.value = body.data.items || [];
      total.value = body.data.totalItems || 0;
      page.value = body.data.currentPage || 0;
      size.value = body.data.pageSize || s;
    } else {
      roadmaps.value = [];
      total.value = 0;
      ElMessage.error(body?.message || "Unable to load route list");
    }
  } catch (e) {
    ElMessage.error("Unable to load route list");
  } finally {
    loading.value = false;
  }
}

function handlePageChange(newPage) {
  // el-pagination uses 1-based pages
  page.value = newPage - 1;
  fetchList();
}

function handleSizeChange(newSize) {
  size.value = newSize;
  page.value = 0;
  fetchList();
}

onMounted(() => fetchList());

function goToDetail(id) {
  router.push({ name: "AdminRoadmapDetail", params: { id } });
}

async function onDelete(id) {
  try {
    await ElMessageBox.confirm(
      "Are you sure you want to delete this route? This action will delete the entire content tree.",
      "Confirm Delete",
      { confirmButtonText: "Delete", cancelButtonText: "Cancel", type: "warning" },
    );
    loading.value = true;
    await deleteRoadmap(id);
    ElMessage.success("Route deletion successful");
    await fetchList();
  } catch (e) {
    // cancel will throw, ignore silently; other errors show message
    if (e && e.response) {
      ElMessage.error("Unable to delete route");
    }
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.roadmap-list-view {
  max-width: 900px;
  margin: 32px auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
</style>
