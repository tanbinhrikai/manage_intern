<template>
  <div class="chatbox-container">
    <div class="chat-history">
      <div v-for="(msg, idx) in chatHistory" :key="idx"
        :class="['chat-msg', msg.role]">
        <el-avatar v-if="msg.role === 'user'" class="avatar"
          size="small" src="https://i.imgur.com/8QfQbFf.png" />
        <el-avatar v-else-if="msg.role === 'ai'" class="avatar"
          size="small" src="https://i.imgur.com/1XkQb6F.png" />
        <el-avatar v-else class="avatar" size="small"
          icon="el-icon-info" />
        <div class="bubble">
          <span v-if="msg.role === 'user'" class="sender">Bạn</span>
          <span v-else-if="msg.role === 'ai'" class="sender">AI</span>
          <span v-else class="sender">System</span>
          <div class="content">{{ msg.content }}</div>
        </div>
      </div>
    </div>
    <div class="input-row">
      <el-input v-model="input"
        placeholder="Nhập tin nhắn (ví dụ: 'tạo lộ trình Java')"
        @keyup.enter="send" :disabled="loading" class="input-box" />
      <el-button type="primary" @click="send" :loading="loading"
        class="send-btn">Gửi</el-button>
    </div>
  </div>
</template>

<script setup>
import { chatProcess } from "@/api/roadmap";
import { ref } from "vue";
const props = defineProps({
  sessionId: String,
  chatHistory: Array,
  loading: Boolean,
  error: String,
  routeTree: Object,
  batchId: [String, Number],
});
const emit = defineEmits([
  "update:sessionId",
  "update:chatHistory",
  "update:routeTree",
  "update:error",
  "update:loading",
]);

const input = ref("");

const send = async () => {
  if (!input.value.trim()) return;
  emit("update:loading", true);
  emit("update:error", null);
  try {
    // Always use chatProcess which handles intent & returns ChatResponseDto
    let sessionIdToSend = props.sessionId;
    const res = await chatProcess({
      message: input.value,
      positionId: null,
      duration: "6 tháng",
      batchId: props.batchId ?? null,
      sessionId: sessionIdToSend,
    });
    const chatResponseDto = res.data?.data;
    const draftData = chatResponseDto?.data;

    if (draftData?.sessionId) {
      emit("update:sessionId", draftData.sessionId);
      sessionIdToSend = draftData.sessionId;
    }
    if (draftData?.roadmapTree) {
      emit("update:routeTree", draftData.roadmapTree);
    }

    const aiMessage = chatResponseDto?.message || res.data?.message || '';
    emit("update:chatHistory", [
      ...props.chatHistory,
      { role: "user", content: input.value },
      { role: "ai", content: aiMessage },
    ]);
  } catch (e) {
    emit("update:error", e?.response?.data?.error || e.message);
  } finally {
    input.value = "";
    emit("update:loading", false);
  }
};
</script>

<style scoped>
.chatbox-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 400px;
  background: #f9f9fb;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  padding: 16px;
}

.chat-history {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 12px;
  margin-bottom: 8px;
  min-height: 320px;
}

.chat-msg {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
}

.chat-msg.user {
  flex-direction: row-reverse;
}

.chat-msg.ai {
  flex-direction: row;
}

.chat-msg.system {
  flex-direction: row;
}

.avatar {
  margin: 0 8px;
}

.bubble {
  max-width: 80%;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  padding: 10px 16px;
  font-size: 1rem;
  word-break: break-word;
  color: #222;
  position: relative;
}

.chat-msg.user .bubble {
  background: #e6f7ff;
  color: #409eff;
  align-self: flex-end;
}

.chat-msg.ai .bubble {
  background: #fffbe6;
  color: #d48806;
  align-self: flex-start;
}

.chat-msg.system .bubble {
  background: #f0f0f0;
  color: #888;
}

.sender {
  font-weight: 600;
  font-size: 0.95rem;
  margin-right: 6px;
  display: block;
  margin-bottom: 2px;
}

.content {
  white-space: pre-line;
}

.input-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}

.input-box {
  flex: 1;
  font-size: 1.1rem;
  border-radius: 8px;
}

.send-btn {
  font-size: 1.1rem;
  border-radius: 8px;
  padding: 0 18px;
}
</style>
