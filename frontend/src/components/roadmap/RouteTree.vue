<template>
  <div class="mt-4">
    <el-tree
      :data="[routeTree]"
      :props="defaultProps"
      node-key="id"
      highlight-current
      draggable
      :expand-on-click-node="false"
      @node-click="expandNode"
      class="bg-white rounded p-2"
    >
      <template #default="{ data }">
        <span>
          <b>{{ data.title }}</b>
          <span v-if="data.nodeType"> ({{ data.nodeType }})</span>
        </span>
      </template>
    </el-tree>
    <el-alert v-if="expanding" title="Đang mở rộng node..." type="info" show-icon class="mt-2" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { chatProcess } from '@/api/roadmap'
const props = defineProps({
  sessionId: String,
  routeTree: Object,
  chatHistory: Array
})
const emit = defineEmits(['update:routeTree', 'update:chatHistory', 'update:error'])

const defaultProps = { children: 'children', label: 'title' }
const expanding = ref(false)

const expandNode = async (data) => {
  if (!props.sessionId || !data.title) return
  expanding.value = true
  emit('update:error', null)
  try {
    const res = await chatProcess({ message: `Detailed implementation of ${data.title}`, sessionId: props.sessionId })
    if (res.data.data?.roadmapTree) emit('update:routeTree', res.data.data.roadmapTree)
    emit('update:chatHistory', [...props.chatHistory, { role: 'ai', content: res.data.message }])
  } catch (e) {
    emit('update:error', e?.response?.data?.error || e.message)
  } finally {
    expanding.value = false
  }
}
</script>