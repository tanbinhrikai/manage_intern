<template>
  <div class="roadmap-builder">
    <div class="header">
      <div>
        <h2 class="title">Roadmap Builder</h2>
        <div class="subtitle">
          Chat để tạo outline, click node để expand, kéo thả để sắp
          xếp, rồi xác
          nhận để lưu.
        </div>
      </div>
      <div class="actions">
        <el-button type="warning" plain
          :disabled="!sessionId || loading" @click="resetDraft">
          Reset draft
        </el-button>
        <el-button type="success" :disabled="!sessionId || loading"
          :loading="confirming" @click="confirmRoute">
          Confirm & Save
        </el-button>
      </div>
    </div>

    <el-alert v-if="error" :title="error" type="error" show-icon
      class="mb-3" />

    <el-alert v-if="lastSavedRootId"
      :title="`Đã lưu roadmap thành công! Root Node ID: ${lastSavedRootId}`"
      type="success" show-icon class="mb-3" />

    <div class="content">
      <div class="left">
        <ChatBox :sessionId="sessionId" :chatHistory="chatHistory"
          :loading="loading" :error="error" :routeTree="routeTree"
          :batchId="batchId" @update:sessionId="onSessionIdUpdate"
          @update:chatHistory="chatHistory = $event"
          @update:routeTree="onRouteTreeUpdate"
          @update:error="error = $event"
          @update:loading="loading = $event" />
      </div>

      <div class="right">
        <div class="tree-card">
          <div class="tree-header">
            <div class="tree-title">Roadmap Tree</div>
            <div class="tree-meta">
              <span v-if="sessionId">sessionId: {{ sessionId }}</span>
              <span v-else>Chưa có draft</span>
            </div>
          </div>

          <RouteTree v-if="routeTree" :sessionId="sessionId"
            :routeTree="routeTree" :chatHistory="chatHistory"
            @update:routeTree="onRouteTreeUpdate"
            @update:chatHistory="chatHistory = $event"
            @update:error="error = $event" />

          <div v-else class="tree-empty">
            Gửi prompt tạo lộ trình để bắt đầu. Ví dụ: “Tạo lộ trình
            Java 6
            tháng”.
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { confirmDraft } from "@/api/roadmap";
import ChatBox from "@/components/roadmap/ChatBox.vue";
import RouteTree from "@/components/roadmap/RouteTree.vue";
import { onMounted, ref } from "vue";
import { useRoute } from "vue-router";

const STORAGE_KEY = "roadmap_builder_session_id";

const sessionId = ref(null);
const chatHistory = ref([]);
const routeTree = ref(null);
const loading = ref(false);
const confirming = ref(false);
const error = ref(null);
const lastSavedRootId = ref(null);
const route = useRoute();
const batchId = ref(route.query.batchId || null);

const onSessionIdUpdate = (val) => {
  sessionId.value = val;
  if (val) sessionStorage.setItem(STORAGE_KEY, val);
  else sessionStorage.removeItem(STORAGE_KEY);
};

const onRouteTreeUpdate = (val) => {
  routeTree.value = val;
};

const resetDraft = () => {
  sessionId.value = null;
  routeTree.value = null;
  chatHistory.value = [];
  error.value = null;
  lastSavedRootId.value = null;
  sessionStorage.removeItem(STORAGE_KEY);
};

const confirmRoute = async () => {
  if (!sessionId.value) return;
  confirming.value = true;
  error.value = null;
  lastSavedRootId.value = null;
  try {
    const res = await confirmDraft(sessionId.value);
    const rootNodeId = res?.data?.rootNodeId;
    lastSavedRootId.value = rootNodeId ?? null;
    chatHistory.value.push({
      role: "system",
      content: "Đã xác nhận & lưu! " + JSON.stringify(res.data),
    });
    sessionId.value = null;
    routeTree.value = null;
    sessionStorage.removeItem(STORAGE_KEY);
  } catch (e) {
    error.value = e?.response?.data?.error || e.message;
  } finally {
    confirming.value = false;
  }
};

onMounted(() => {
  const saved = sessionStorage.getItem(STORAGE_KEY);
  if (saved) {
    sessionId.value = saved;
    // chatHistory.value.push({
    //   role: "system",
    //   content:
    //     "SessionId has been restored from sessionStorage. You can continue to expand or confirm",
    // });
  }
});
</script>

<style scoped>
.roadmap-builder {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.subtitle {
  color: #666;
  font-size: 13px;
  margin-top: 2px;
}

.actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  min-height: 520px;
}

.left,
.right {
  min-height: 520px;
}

.tree-card {
  height: 100%;
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.tree-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}

.tree-title {
  font-weight: 700;
}

.tree-meta {
  font-size: 12px;
  color: #777;
}

.tree-empty {
  color: #777;
  font-size: 13px;
  padding: 16px;
  border: 1px dashed #e2e2e2;
  border-radius: 10px;
}

.mb-3 {
  margin-bottom: 12px;
}
</style>
