<script setup>
import MentorSidebar from '@/components/layout/MentorSidebar.vue'
import MentorHeader from '@/components/layout/MentorHeader.vue'

const props = defineProps({
  pageTitle: {
    type: String,
    default: ''
  }
})
</script>

<template>
  <div class="mentor-layout">
    <MentorSidebar />
    <div class="main-wrapper">
      <MentorHeader :page-title="pageTitle" />
      <main class="main-content">
        <slot />
      </main>
    </div>
  </div>
</template>

<style scoped>
.mentor-layout {
  display: flex;
  min-height: 100vh;
  background: #f8f9fa;
}

.main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.main-content {
  padding: 10px;
  overflow-y: hidden;
  min-height: 0;
  height: calc(100vh - 60px);
}
</style>
