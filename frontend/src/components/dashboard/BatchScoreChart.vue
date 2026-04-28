<script setup>
import { ref, computed, onMounted } from 'vue'
import { useLocaleStore } from '@/locales/locale'
import { ArrowLeft } from '@element-plus/icons-vue'
import MultiLineChart from './MultiLineChart.vue'
import { getBatchScoreTrend, getInternScoreTrendByBatch } from '@/api/dashboard'
import { useLoading, useApi } from '@/composables'

const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)

const { loading, withLoading } = useLoading()
const { execute: executeApi } = useApi({
  showErrorMessage: false
})

// State
const viewLevel = ref('batch') // 'batch' or 'intern'
const selectedBatch = ref(null)

// Data
const batchData = ref({ labels: [], series: [] })
const internData = ref({ labels: [], series: [] })

// Current chart data based on view level
const chartData = computed(() => {
  return viewLevel.value === 'batch' ? batchData.value : internData.value
})

const chartTitle = computed(() => {
  if (viewLevel.value === 'batch') {
    return t.value('dashboard.batchScoreTrend') || 'Batch Average Score Trend'
  }
  return `${selectedBatch.value?.name || 'Batch'} - ${t.value('dashboard.internScoreTrend') || 'Intern Score Trend'}`
})

// Fetch batch score trend
async function fetchBatchScoreTrend() {
  await withLoading(async () => {
    const res = await executeApi(() => getBatchScoreTrend())
    if (res.data?.data) {
      batchData.value = res.data.data
    }
  })
}

// Fetch intern score trend for a specific batch
async function fetchInternScoreTrend(batchId) {
  await withLoading(async () => {
    const res = await executeApi(() => getInternScoreTrendByBatch(batchId))
    if (res.data?.data) {
      internData.value = res.data.data
    }
  })
}

// Handle series click - drill down to intern level
function handleSeriesClick({ series, seriesIndex }) {
  if (viewLevel.value === 'batch') {
    // Drill down to intern level
    selectedBatch.value = {
      id: series.id,
      name: series.name,
      index: seriesIndex
    }
    
    // Use the batch ID from the series data
    if (series.id) {
      fetchInternScoreTrend(series.id)
      viewLevel.value = 'intern'
    } else {
      console.warn('Batch ID not found in series data')
    }
  }
}

// Go back to batch level
function goBack() {
  viewLevel.value = 'batch'
  selectedBatch.value = null
  internData.value = { labels: [], series: [] }
}

// Initialize
onMounted(() => {
  fetchBatchScoreTrend()
})

// Expose methods for parent
defineExpose({
  refresh: fetchBatchScoreTrend
})
</script>

<template>
  <div class="batch-score-chart" v-loading="loading">
    <!-- Header with title and back button -->
    <div class="chart-header">
      <div class="header-left">
        <el-button 
          v-if="viewLevel === 'intern'" 
          type="primary" 
          size="small"
          @click="goBack"
          plain
        >
          <el-icon><ArrowLeft /></el-icon>
          {{ t('common.back') || 'Back' }}
        </el-button>
        <span class="chart-title">{{ chartTitle }}</span>
      </div>
      <div class="header-right">
        <span v-if="viewLevel === 'batch'" class="hint-text">
          {{ t('dashboard.clickToViewInterns') || 'Click on a batch to view intern details' }}
        </span>
      </div>
    </div>
    
    <!-- Chart -->
    <div class="chart-content">
      <MultiLineChart
        v-if="chartData.series && chartData.series.length > 0"
        :labels="chartData.labels"
        :series="chartData.series"
        :height="400"
        :show-area="false"
        :clickable="viewLevel === 'batch'"
        value-unit=""
        :value-formatter="(v) => v !== null ? v.toFixed(2) : '-'"
        @series-click="handleSeriesClick"
      />
      <div v-else class="empty-chart">
        {{ t('dashboard.noData') || 'No data available' }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.batch-score-chart {
  width: 100%;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.header-right {
  display: flex;
  align-items: center;
}

.hint-text {
  font-size: 13px;
  color: #6b7280;
  font-style: italic;
}

.chart-content {
  min-height: 400px;
}

.empty-chart {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 400px;
  color: #9ca3af;
  font-size: 14px;
  background: #f9fafb;
  border-radius: 8px;
}
</style>
