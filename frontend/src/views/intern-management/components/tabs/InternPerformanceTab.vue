<script setup>
import { computed } from 'vue'
import { useLocaleStore } from '@/locales/locale'

const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)
</script>

<template>
  <div class="tab-content placeholder-content">
     {{ t('internDetail.tabs.performance') }} Content
  </div>
</template>

<style scoped>
.placeholder-content {
  padding: 40px;
  text-align: center;
  color: #909399;
  background: white;
  border-radius: 8px;
}
</style>
