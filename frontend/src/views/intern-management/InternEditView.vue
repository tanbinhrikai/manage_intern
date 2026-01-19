<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useLocaleStore } from '@/locales/locale'
import { useAuthStore } from '@/stores/auth'
import AdminLayout from "@/layouts/dashboard/AdminLayout.vue"
import MentorLayout from "@/layouts/dashboard/MentorLayout.vue"
import { getInternById } from '@/api/intern'

import InternGeneralTab from './components/tabs/InternGeneralTab.vue'
import InternReportsTab from './components/tabs/InternReportsTab.vue'
import InternPerformanceTab from './components/tabs/InternPerformanceTab.vue'
import InternHistoryTab from './components/tabs/InternHistoryTab.vue'

const route = useRoute()
const localeStore = useLocaleStore()
const authStore = useAuthStore()
const t = computed(() => localeStore.t)

const internId = route.params.id
const intern = ref({}) 
const loading = ref(false)
const activeTab = ref('general')

const isAdmin = computed(() => authStore.userRole === 'ADMIN')
const layoutComponent = computed(() => isAdmin.value ? AdminLayout : MentorLayout)

async function fetchInternDetail() {
  loading.value = true
  try {
    const res = await getInternById(internId)
    intern.value = res.data?.data || {}
  } catch (error) {
    console.error("Failed to load intern detail:", error)
    ElMessage.error(t.value('internManagement.messages.loadError'))
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchInternDetail()
})
</script>

<template>
  <component :is="layoutComponent">
    <div class="intern-edit-view" v-loading="loading">
      <div class="header-section">
        <h1 class="page-title">
          {{ t('internDetail.profileTitle') }}: {{ intern.fullName }} - {{ intern.position?.title }}
        </h1>
      </div>

      <el-tabs v-model="activeTab" class="detail-tabs">
        <el-tab-pane :label="t('internDetail.tabs.general')" name="general">
          <InternGeneralTab 
            :intern="intern" 
            @saved="fetchInternDetail" 
          />
        </el-tab-pane>
        
        <el-tab-pane :label="t('internDetail.tabs.reports')" name="reports">
          <InternReportsTab 
            v-if="activeTab === 'reports'"
            :internId="internId" 
          />
        </el-tab-pane>

        <el-tab-pane :label="t('internDetail.tabs.performance')" name="performance">
            <InternPerformanceTab />
        </el-tab-pane>

        <el-tab-pane :label="t('internDetail.tabs.history')" name="history">
            <InternHistoryTab />
        </el-tab-pane>
      </el-tabs>
    </div>
  </component>
</template>

<style scoped>
.intern-edit-view {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.header-section {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.detail-tabs :deep(.el-tabs__header) {
  margin-bottom: 24px;
}

.detail-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 600;
  color: #606266;
}

.detail-tabs :deep(.el-tabs__item.is-active) {
  color: #6466dd;
}

.detail-tabs :deep(.el-tabs__active-bar) {
  background-color: #6466dd;
}
</style>
