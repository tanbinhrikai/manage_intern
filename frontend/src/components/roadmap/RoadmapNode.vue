<template>
  <div class="roadmap-node">
    <el-card class="node-card" shadow="never">
      <div class="node-title">{{ node.title }}</div>
      <div class="node-desc" v-if="node.description">
        {{ node.description }}
      </div>
      <div class="node-meta">
        <small>Type: {{ node.nodeType }}</small>
      </div>
    </el-card>
    <div class="children" v-if="node.children && node.children.length">
      <RoadmapNode
        v-for="child in node.children"
        :key="child.id"
        :node="child" />
    </div>
  </div>
</template>

<script setup>
import { defineProps } from "vue";
const props = defineProps({ node: Object });
</script>

<style scoped>
.roadmap-node {
  margin-left: 12px;
  margin-top: 8px;
}
.node-card {
  padding: 8px;
}
.node-title {
  font-weight: 600;
}
.children {
  margin-left: 12px;
}
</style>
