<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useLocaleStore } from '@/locales/locale'

const router = useRouter()
const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)

const goHome = () => {
  router.push('/login')
}
</script>

<template>
  <div class="not-found-page">
    <div class="content">
      <div class="error-code">404</div>
      <h1 class="title">{{ t('notFound.title') }}</h1>
      <p class="description">{{ t('notFound.description') }}</p>
      <el-button type="primary" size="large" @click="goHome">
        {{ t('notFound.backHome') }}
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.not-found-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  padding: 20px;
}

.content {
  text-align: center;
  color: #303133;
}

.error-code {
  font-size: 150px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 16px;
  color: #409eff;
  text-shadow: 4px 4px 0 rgba(0, 0, 0, 0.05);
}

.title {
  font-size: 32px;
  font-weight: 600;
  margin: 0 0 12px 0;
}

.description {
  font-size: 18px;
  opacity: 0.9;
  margin: 0 0 32px 0;
}

:deep(.el-button) {
  padding: 14px 32px;
  font-size: 16px;
  border-radius: 8px;
}
</style>
