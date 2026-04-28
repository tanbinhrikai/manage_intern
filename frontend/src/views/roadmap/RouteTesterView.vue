<template>
  <div class="max-w-2xl mx-auto bg-white p-6 rounded shadow mt-8">
    <ChatBox
      :sessionId="sessionId"
      :chatHistory="chatHistory"
      :loading="loading"
      :error="error"
      :routeTree="routeTree"
      @update:sessionId="sessionId = $event"
      @update:chatHistory="chatHistory = $event"
      @update:routeTree="routeTree = $event"
      @update:error="error = $event"
      @update:loading="loading = $event" />
    <RouteTree
      v-if="routeTree"
      :sessionId="sessionId"
      :routeTree="routeTree"
      :chatHistory="chatHistory"
      @update:routeTree="routeTree = $event"
      @update:chatHistory="chatHistory = $event"
      @update:error="error = $event" />
    <el-button
      v-if="routeTree && sessionId"
      type="success"
      class="mt-4"
      @click="confirmRoute"
      :loading="loading"
      >Xác nhận lộ trình</el-button
    >
    <el-alert v-if="error" :title="error" type="error" show-icon class="mt-4" />
  </div>
</template>

<script setup>
import { confirmDraft } from "@/api/roadmap";
import ChatBox from "@/components/roadmap/ChatBox.vue";
import RouteTree from "@/components/roadmap/RouteTree.vue";
import { ref } from "vue";

const sessionId = ref(null);
const chatHistory = ref([]);
const routeTree = ref(null);
const loading = ref(false);
const error = ref(null);

const confirmRoute = async () => {
  loading.value = true;
  error.value = null;
  try {
    const res = await confirmDraft(sessionId.value);
    chatHistory.value.push({
      role: "system",
      content: "Confirmed! " + JSON.stringify(res.data),
    });
    routeTree.value = null;
    sessionId.value = null; // chỉ reset sessionId sau khi xác nhận
  } catch (e) {
    error.value = e?.response?.data?.error || e.message;
  } finally {
    loading.value = false;
  }
};
</script>
