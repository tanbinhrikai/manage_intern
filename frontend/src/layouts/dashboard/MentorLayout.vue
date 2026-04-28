<script setup>
import MentorSidebar from '@/components/layout/MentorSidebar.vue'
import MentorHeader from '@/components/layout/MentorHeader.vue'
</script>

<template>
  <div class="mentor-layout">
    <MentorSidebar />
    <div class="main-wrapper">
      <MentorHeader />
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
  flex: 1;
  padding: 10px;
  overflow-y: auto;
}
</style>
