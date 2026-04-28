<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { useLocaleStore } from '@/locales/locale'

const localeStore = useLocaleStore()
const t = (key) => localeStore.t(key)

const props = defineProps({
  data: {
    type: Array,
    required: true,
    default: () => []
  },
  height: {
    type: Number,
    default: 300
  },
  lineColor: {
    type: String,
    default: '#3b82f6'
  },
  fillColor: {
    type: String,
    default: '#3b82f6'
  },
  showArea: {
    type: Boolean,
    default: false
  },
  showTooltip: {
    type: Boolean,
    default: true
  },
  valueUnit: {
    type: String,
    default: 'interns'
  },
  valueFormatter: {
    type: Function,
    default: null
  }
})

const canvasRef = ref(null)
const tooltipRef = ref(null)
const hoveredPoint = ref(null)
const mousePos = ref({ x: 0, y: 0 })

const maxValue = computed(() => {
  if (!props.data || props.data.length === 0) return 1
  return Math.max(...props.data.map(item => item.value || 0), 1)
})

const totalValue = computed(() => {
  if (!props.data || props.data.length === 0) return 0
  return props.data.reduce((sum, item) => sum + (item.value || 0), 0)
})

const formatValue = (value) => {
  if (props.valueFormatter) {
    return props.valueFormatter(value)
  }
  // Default formatting
  if (typeof value === 'number') {
    // If value is decimal and less than 1, format as percentage
    if (value < 1 && value % 1 !== 0) {
      return (value * 100).toFixed(1) + '%'
    }
    // If value is percentage-like (0-100), add %
    if (value <= 100 && value >= 0 && value % 1 !== 0) {
      return value.toFixed(1) + '%'
    }
    // Otherwise round to integer
    return Math.round(value)
  }
  return value
}

let drawTimeout = null
let rafId = null
let isInitialDraw = true

const scheduleDraw = () => {
  // Cancel pending draws
  if (drawTimeout) {
    clearTimeout(drawTimeout)
  }
  if (rafId) {
    cancelAnimationFrame(rafId)
  }
  
  // First draw should be immediate to ensure canvas is ready for hover detection
  if (isInitialDraw) {
    drawChart()
    isInitialDraw = false
  } else {
    // Use requestAnimationFrame for smooth rendering on subsequent draws
    rafId = requestAnimationFrame(() => {
      drawChart()
      rafId = null
    })
  }
}

onMounted(() => {
  scheduleDraw()
})

watch(() => props.data, () => {
  // When data changes, draw immediately for better UX
  if (props.data && props.data.length > 0) {
    // Cancel any pending draws
    if (rafId) {
      cancelAnimationFrame(rafId)
      rafId = null
    }
    // Draw immediately when data changes
    drawChart()
  } else {
    scheduleDraw()
  }
}, { deep: true })

const getPointAtPosition = (x, y) => {
  if (!props.data || props.data.length === 0) return null
  
  const padding = { top: 20, right: 20, bottom: 40, left: 50 }
  const chartWidth = canvasRef.value.width - padding.left - padding.right
  const chartHeight = canvasRef.value.height - padding.top - padding.bottom
  
  const points = props.data.map((item, index) => {
    const px = padding.left + (index / (props.data.length - 1 || 1)) * chartWidth
    const py = padding.top + chartHeight - (item.value / maxValue.value) * chartHeight
    return { x: px, y: py, value: item.value, label: item.label, index }
  })
  
  // Find closest point
  let closest = null
  let minDistance = Infinity
  
  points.forEach(point => {
    const distance = Math.sqrt(Math.pow(x - point.x, 2) + Math.pow(y - point.y, 2))
    if (distance < 30 && distance < minDistance) {
      minDistance = distance
      closest = point
    }
  })
  
  return closest
}

const handleMouseMove = (e) => {
  if (!canvasRef.value) return
  
  const rect = canvasRef.value.getBoundingClientRect()
  const scaleX = canvasRef.value.width / rect.width
  const scaleY = canvasRef.value.height / rect.height
  
  const x = (e.clientX - rect.left) * scaleX
  const y = (e.clientY - rect.top) * scaleY
  
  mousePos.value = { x: e.clientX, y: e.clientY }
  
  const point = getPointAtPosition(x, y)
  const previousPoint = hoveredPoint.value
  const previousIndex = previousPoint?.index
  const currentIndex = point?.index
  
  // Update hoveredPoint immediately for tooltip reactivity
  hoveredPoint.value = point
  
  // Only redraw canvas if hover state actually changed
  const hoverStateChanged = 
    (point && !previousPoint) || 
    (!point && previousPoint) || 
    (point && previousPoint && currentIndex !== previousIndex)
  
  if (hoverStateChanged) {
    scheduleDraw()
  }
}

const handleMouseLeave = () => {
  hoveredPoint.value = null
  scheduleDraw()
}

// Cleanup on unmount
onUnmounted(() => {
  if (drawTimeout) {
    clearTimeout(drawTimeout)
  }
  if (rafId) {
    cancelAnimationFrame(rafId)
  }
})

const drawChart = () => {
  const canvas = canvasRef.value
  if (!canvas || !props.data || props.data.length === 0) return

  const ctx = canvas.getContext('2d')
  const padding = { top: 20, right: 20, bottom: 40, left: 50 }
  const chartWidth = canvas.width - padding.left - padding.right
  const chartHeight = canvas.height - padding.top - padding.bottom

  ctx.clearRect(0, 0, canvas.width, canvas.height)

  // Calculate points
  const points = props.data.map((item, index) => {
    const x = padding.left + (index / (props.data.length - 1 || 1)) * chartWidth
    const y = padding.top + chartHeight - (item.value / maxValue.value) * chartHeight
    return { x, y, value: item.value, label: item.label, index }
  })

  // Draw grid lines
  ctx.strokeStyle = '#f3f4f6'
  ctx.lineWidth = 1
  const yAxisSteps = 5
  for (let i = 0; i <= yAxisSteps; i++) {
    const y = padding.top + (chartHeight / yAxisSteps) * i
    ctx.beginPath()
    ctx.moveTo(padding.left, y)
    ctx.lineTo(canvas.width - padding.right, y)
    ctx.stroke()
  }

  // Draw area (if enabled)
  if (props.showArea && points.length > 0) {
    ctx.fillStyle = props.fillColor + '20' // Add transparency
    ctx.beginPath()
    ctx.moveTo(padding.left, padding.top + chartHeight)
    
    points.forEach(point => {
      ctx.lineTo(point.x, point.y)
    })
    
    ctx.lineTo(padding.left + chartWidth, padding.top + chartHeight)
    ctx.closePath()
    ctx.fill()
  }

  // Draw line
  if (points.length > 1) {
    ctx.strokeStyle = props.lineColor
    ctx.lineWidth = 2
    ctx.beginPath()
    ctx.moveTo(points[0].x, points[0].y)
    
    for (let i = 1; i < points.length; i++) {
      ctx.lineTo(points[i].x, points[i].y)
    }
    
    ctx.stroke()
  }

  // Draw points
  points.forEach(point => {
    const isHovered = hoveredPoint.value && hoveredPoint.value.index === point.index
    
    // Outer circle (larger when hovered)
    ctx.fillStyle = '#ffffff'
    ctx.beginPath()
    ctx.arc(point.x, point.y, isHovered ? 7 : 5, 0, 2 * Math.PI)
    ctx.fill()
    
    // Inner circle
    ctx.fillStyle = props.lineColor
    ctx.beginPath()
    ctx.arc(point.x, point.y, isHovered ? 5 : 3, 0, 2 * Math.PI)
    ctx.fill()
    
    // Draw vertical line when hovered
    if (isHovered) {
      ctx.strokeStyle = props.lineColor + '40'
      ctx.lineWidth = 1
      ctx.setLineDash([5, 5])
      ctx.beginPath()
      ctx.moveTo(point.x, padding.top)
      ctx.lineTo(point.x, padding.top + chartHeight)
      ctx.stroke()
      ctx.setLineDash([])
    }
  })

  // Draw Y-axis
  ctx.strokeStyle = '#e5e7eb'
  ctx.lineWidth = 1
  ctx.beginPath()
  ctx.moveTo(padding.left, padding.top)
  ctx.lineTo(padding.left, padding.top + chartHeight)
  ctx.stroke()

  // Draw Y-axis labels
  ctx.fillStyle = '#9ca3af'
  ctx.font = '10px Inter, sans-serif'
  ctx.textAlign = 'right'
  ctx.textBaseline = 'middle'
  
  for (let i = 0; i <= yAxisSteps; i++) {
    const value = Math.round((maxValue.value / yAxisSteps) * (yAxisSteps - i))
    const y = padding.top + (chartHeight / yAxisSteps) * i
    ctx.fillText(value.toString(), padding.left - 8, y)
  }

  // Draw X-axis labels
  ctx.fillStyle = '#6b7280'
  ctx.font = '11px Inter, sans-serif'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'top'
  
  // Show every Nth label to avoid crowding
  const labelStep = Math.max(1, Math.floor(props.data.length / 6))
  points.forEach((point, index) => {
    if (index % labelStep === 0 || index === points.length - 1) {
      // Rotate label if needed
      ctx.save()
      ctx.translate(point.x, padding.top + chartHeight + 12)
      ctx.rotate(-Math.PI / 4)
      const label = point.label.length > 8 ? point.label.substring(0, 6) + '...' : point.label
      ctx.fillText(label, 0, 0)
      ctx.restore()
    }
  })
}

</script>

<template>
  <div class="line-chart-container">
    <canvas 
      ref="canvasRef" 
      :width="data.length > 0 ? Math.max(500, data.length * 70) : 500" 
      :height="height"
      style="cursor: crosshair;"
      @mousemove="handleMouseMove"
      @mouseleave="handleMouseLeave"
    ></canvas>
    
    <!-- Tooltip -->
    <Teleport to="body">
      <div 
        v-if="hoveredPoint && showTooltip" 
        ref="tooltipRef"
        class="chart-tooltip"
        :style="{
          left: mousePos.x + 'px',
          top: mousePos.y + 'px',
          pointerEvents: 'none'
        }"
      >
        <div class="tooltip-label">{{ hoveredPoint.label }}</div>
        <div class="tooltip-value">
          <strong>{{ formatValue(hoveredPoint.value) }}</strong> {{ valueUnit }}
        </div>
      </div>
    </Teleport>
    
    <!-- Summary Stats -->
    <div v-if="data.length > 0" class="chart-summary">
      <div class="summary-item">
        <span class="summary-label">{{ t('dashboard.total') }}:</span>
        <span class="summary-value">{{ formatValue(totalValue) }}</span>
      </div>
      <div class="summary-item">
        <span class="summary-label">{{ t('dashboard.average') }}:</span>
        <span class="summary-value">{{ formatValue(data.length > 0 ? totalValue / data.length : 0) }}</span>
      </div>
      <div class="summary-item">
        <span class="summary-label">{{ t('dashboard.peak') }}:</span>
        <span class="summary-value">{{ formatValue(maxValue) }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.line-chart-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  overflow-x: auto;
  position: relative;
}

canvas {
  display: block;
  max-width: 100%;
  height: auto;
}

.chart-tooltip {
  position: fixed;
  background: rgba(31, 41, 55, 0.95);
  color: white;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 12px;
  pointer-events: none;
  z-index: 1000;
  transform: translate(-50%, -100%);
  margin-top: -8px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.tooltip-label {
  font-weight: 500;
  margin-bottom: 4px;
  color: #e5e7eb;
}

.tooltip-value {
  font-size: 14px;
  color: white;
}

.tooltip-value strong {
  font-size: 16px;
  color: #60a5fa;
}

.chart-summary {
  display: flex;
  gap: 24px;
  margin-top: 16px;
  padding: 12px 16px;
  background: #f9fafb;
  border-radius: 8px;
  flex-wrap: wrap;
}

.summary-item {
  display: flex;
  gap: 8px;
  align-items: center;
}

.summary-label {
  font-size: 12px;
  color: #6b7280;
}

.summary-value {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}
</style>
