<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useActivityStreamStore } from '@/stores/activityStream'
import AdminSidebar from '@/components/layout/AdminSidebar.vue'
import AdminHeader from '@/components/layout/AdminHeader.vue'

const activityStore = useActivityStreamStore()

onMounted(() => {
  activityStore.registerLayout()
})

onUnmounted(() => {
  activityStore.unregisterLayout()
})
</script>

<template>
  <div class="admin-layout">
    <AdminSidebar />
    <div class="main-wrapper">
      <AdminHeader />
      <main class="main-content">
        <slot />
      </main>
    </div>
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #f3f4f6;
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
}
</style>
