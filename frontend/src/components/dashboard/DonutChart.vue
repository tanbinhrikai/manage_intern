<script setup>
import { ref, onMounted, computed, watch } from 'vue'

const props = defineProps({
  data: {
    type: Array,
    required: true
  }
})

const canvasRef = ref(null)

const total = computed(() => props.data.reduce((sum, item) => sum + item.value, 0))

onMounted(() => {
  drawChart()
})

watch(() => props.data, () => {
  drawChart()
}, { deep: true })

const drawChart = () => {
  const canvas = canvasRef.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  const centerX = canvas.width / 2
  const centerY = canvas.height / 2
  const outerRadius = Math.min(centerX, centerY) - 10
  const innerRadius = outerRadius * 0.6

  ctx.clearRect(0, 0, canvas.width, canvas.height)

  let currentAngle = -Math.PI / 2

  props.data.forEach((item) => {
    const sliceAngle = (item.value / total.value) * 2 * Math.PI

    ctx.beginPath()
    ctx.arc(centerX, centerY, outerRadius, currentAngle, currentAngle + sliceAngle)
    ctx.arc(centerX, centerY, innerRadius, currentAngle + sliceAngle, currentAngle, true)
    ctx.closePath()
    ctx.fillStyle = item.color
    ctx.fill()

    const midAngle = currentAngle + sliceAngle / 2
    const labelRadius = (outerRadius + innerRadius) / 2
    const labelX = centerX + Math.cos(midAngle) * labelRadius
    const labelY = centerY + Math.sin(midAngle) * labelRadius

    if (item.value > 0) {
      ctx.fillStyle = '#ffffff'
      ctx.font = 'bold 12px Inter, sans-serif'
      ctx.textAlign = 'center'
      ctx.textBaseline = 'middle'
      ctx.fillText(item.value.toString(), labelX, labelY)
    }

    currentAngle += sliceAngle
  })

  ctx.fillStyle = '#f8f9fa'
  ctx.beginPath()
  ctx.arc(centerX, centerY, innerRadius - 2, 0, 2 * Math.PI)
  ctx.fill()

  ctx.fillStyle = '#1f2937'
  ctx.font = 'bold 24px Inter, sans-serif'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText(total.value.toString(), centerX, centerY)
}
</script>

<template>
  <div class="chart-container">
    <canvas ref="canvasRef" width="220" height="220"></canvas>
    <div class="chart-legend">
      <div v-for="item in data" :key="item.label" class="legend-item">
        <span class="legend-color" :style="{ backgroundColor: item.color }"></span>
        <span class="legend-label">{{ item.label }}</span>
        <span class="legend-value">{{ item.value }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chart-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.chart-legend {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #6b7280;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.legend-value {
  font-weight: 600;
  color: #1f2937;
}
</style>
