<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { useLocaleStore } from '@/locales/locale'

const localeStore = useLocaleStore()
const t = (key) => localeStore.t(key)

const props = defineProps({
  labels: {
    type: Array,
    required: true,
    default: () => []
  },
  series: {
    type: Array,
    required: true,
    default: () => []
  },
  height: {
    type: Number,
    default: 300
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
    default: ''
  },
  valueFormatter: {
    type: Function,
    default: null
  },
  clickable: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['series-click'])

const canvasRef = ref(null)
const hoveredPoint = ref(null)
const mousePos = ref({ x: 0, y: 0 })
const visibleSeries = ref({})

// Color palette for series
const colors = [
  "#3b82f6", "#ef4444", "#10b981", "#f59e0b", "#8b5cf6", 
  "#ec4899", "#06b6d4", "#84cc16", "#f97316", "#6366f1"
]

// Get color for a series by index
const getSeriesColor = (index) => {
  return colors[index % colors.length]
}

// Add color to each series
const seriesWithColors = computed(() => {
  if (!props.series) return []
  return props.series.map((s, index) => ({
    ...s,
    color: s.color || getSeriesColor(index)
  }))
})

// Initialize all series as visible
watch(() => props.series, (newSeries) => {
  if (newSeries) {
    newSeries.forEach(s => {
      if (visibleSeries.value[s.name] === undefined) {
        visibleSeries.value[s.name] = true
      }
    })
  }
}, { immediate: true })

const maxValue = computed(() => {
  if (!seriesWithColors.value || seriesWithColors.value.length === 0) return 1
  let max = 0
  seriesWithColors.value.forEach(s => {
    if (visibleSeries.value[s.name] !== false) {
      s.data.forEach(v => {
        if (v !== null && v !== undefined && v > max) max = v
      })
    }
  })
  return max || 1
})

const formatValue = (value) => {
  if (value === null || value === undefined) {
    return '-'
  }
  if (props.valueFormatter) {
    return props.valueFormatter(value)
  }
  if (typeof value === 'number') {
    if (value < 1 && value > 0) {
      return (value * 100).toFixed(1) + '%'
    }
    return value.toFixed(2)
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

const toggleSeries = (name) => {
  visibleSeries.value[name] = !visibleSeries.value[name]
  scheduleDraw()
}

onMounted(() => {
  scheduleDraw()
})

// Debounce watcher to avoid multiple rapid redraws
watch([() => props.labels, () => props.series], () => {
  // When data changes, draw immediately for better UX
  if (props.labels && props.labels.length > 0 && props.series && props.series.length > 0) {
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
  if (!props.labels || props.labels.length === 0) return null
  if (!canvasRef.value) return null
  if (!seriesWithColors.value || seriesWithColors.value.length === 0) return null
  
  const padding = { top: 20, right: 20, bottom: 50, left: 60 }
  const canvas = canvasRef.value
  const chartWidth = canvas.width - padding.left - padding.right
  const chartHeight = canvas.height - padding.top - padding.bottom
  
  let closest = null
  let minDistance = Infinity
  
  seriesWithColors.value.forEach(series => {
    if (visibleSeries.value[series.name] === false) return
    if (!series.data || series.data.length === 0) return
    
    series.data.forEach((value, index) => {
      if (index >= props.labels.length) return
      if (value === null || value === undefined) return // Skip null values
      
      const px = padding.left + (index / (props.labels.length - 1 || 1)) * chartWidth
      const py = padding.top + chartHeight - (value / maxValue.value) * chartHeight
      
      const distance = Math.sqrt(Math.pow(x - px, 2) + Math.pow(y - py, 2))
      if (distance < 25 && distance < minDistance) {
        minDistance = distance
        closest = { 
          x: px, 
          y: py, 
          value, 
          label: props.labels[index], 
          seriesName: series.name,
          seriesColor: series.color,
          index 
        }
      }
    })
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
  
  // Update mouse position immediately
  mousePos.value = { x: e.clientX, y: e.clientY }
  
  // Get point at current position
  const point = getPointAtPosition(x, y)
  const previousPoint = hoveredPoint.value
  const previousIndex = previousPoint?.index
  const currentIndex = point?.index
  
  // Update hoveredPoint immediately for tooltip reactivity
  // This ensures tooltip shows/hides immediately
  hoveredPoint.value = point
  
  // Only redraw canvas if hover state actually changed
  // Tooltip doesn't depend on canvas redraw, it's reactive to hoveredPoint
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

const handleClick = (e) => {
  if (!props.clickable) return
  if (!hoveredPoint.value) return
  
  // Find the series that was clicked
  const clickedSeries = seriesWithColors.value.find(
    s => s.name === hoveredPoint.value.seriesName
  )
  
  if (clickedSeries) {
    // Find the index of the series in the original props.series
    const seriesIndex = props.series.findIndex(s => s.name === clickedSeries.name)
    emit('series-click', { 
      series: clickedSeries, 
      seriesIndex,
      point: hoveredPoint.value 
    })
  }
}

const handleLegendClick = (series, index) => {
  emit('series-click', { 
    series, 
    seriesIndex: index,
    point: null 
  })
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
  if (!canvas || !props.labels || props.labels.length === 0) return

  const ctx = canvas.getContext('2d')
  const padding = { top: 20, right: 20, bottom: 50, left: 60 }
  const chartWidth = canvas.width - padding.left - padding.right
  const chartHeight = canvas.height - padding.top - padding.bottom

  ctx.clearRect(0, 0, canvas.width, canvas.height)

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

  // Draw each series
  seriesWithColors.value.forEach(series => {
    if (visibleSeries.value[series.name] === false) return
    
    const points = series.data.map((value, index) => {
      const x = padding.left + (index / (props.labels.length - 1 || 1)) * chartWidth
      const isNull = value === null || value === undefined
      const y = isNull ? null : padding.top + chartHeight - (value / maxValue.value) * chartHeight
      return { x, y, value, isNull }
    })

    // Draw area (if enabled) - only between non-null points
    if (props.showArea) {
      const validPoints = points.filter(p => !p.isNull)
      if (validPoints.length > 0) {
        ctx.fillStyle = series.color + '15'
        ctx.beginPath()
        ctx.moveTo(validPoints[0].x, padding.top + chartHeight)
        validPoints.forEach(point => ctx.lineTo(point.x, point.y))
        ctx.lineTo(validPoints[validPoints.length - 1].x, padding.top + chartHeight)
        ctx.closePath()
        ctx.fill()
      }
    }

    // Draw line - skip null values and draw segments between non-null points
    ctx.strokeStyle = series.color
    ctx.lineWidth = 2.5
    let drawing = false
    for (let i = 0; i < points.length; i++) {
      if (points[i].isNull) {
        if (drawing) {
          ctx.stroke()
          drawing = false
        }
        continue
      }
      if (!drawing) {
        ctx.beginPath()
        ctx.moveTo(points[i].x, points[i].y)
        drawing = true
      } else {
        ctx.lineTo(points[i].x, points[i].y)
      }
    }
    if (drawing) {
      ctx.stroke()
    }

    // Draw points - only for non-null values
    points.forEach((point, index) => {
      if (point.isNull) return
      
      const isHovered = hoveredPoint.value && 
                        hoveredPoint.value.seriesName === series.name && 
                        hoveredPoint.value.index === index
      
      ctx.fillStyle = '#ffffff'
      ctx.beginPath()
      ctx.arc(point.x, point.y, isHovered ? 6 : 4, 0, 2 * Math.PI)
      ctx.fill()
      
      ctx.fillStyle = series.color
      ctx.beginPath()
      ctx.arc(point.x, point.y, isHovered ? 4 : 2.5, 0, 2 * Math.PI)
      ctx.fill()
    })
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
    const value = (maxValue.value / yAxisSteps) * (yAxisSteps - i)
    const y = padding.top + (chartHeight / yAxisSteps) * i
    ctx.fillText(formatValue(value), padding.left - 8, y)
  }

  // Draw X-axis labels
  ctx.fillStyle = '#6b7280'
  ctx.font = '10px Inter, sans-serif'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'top'
  
  const labelStep = Math.max(1, Math.floor(props.labels.length / 8))
  props.labels.forEach((label, index) => {
    if (index % labelStep === 0 || index === props.labels.length - 1) {
      const x = padding.left + (index / (props.labels.length - 1 || 1)) * chartWidth
      ctx.save()
      ctx.translate(x, padding.top + chartHeight + 10)
      ctx.rotate(-Math.PI / 6)
      const displayLabel = label.length > 10 ? label.substring(0, 8) + '...' : label
      ctx.fillText(displayLabel, 0, 0)
      ctx.restore()
    }
  })
}
</script>

<template>
  <div class="multi-line-chart-container">
    <!-- Legend -->
    <div class="chart-legend" v-if="seriesWithColors.length > 0">
      <div 
        v-for="(s, index) in seriesWithColors" 
        :key="s.name"
        class="legend-item"
        :class="{ 
          'legend-item--disabled': visibleSeries[s.name] === false,
          'legend-item--clickable': clickable
        }"
        @click="clickable ? handleLegendClick(s, index) : toggleSeries(s.name)"
      >
        <span class="legend-color" :style="{ backgroundColor: s.color }"></span>
        <span class="legend-label">{{ s.name }}</span>
      </div>
    </div>
    
    <canvas 
      ref="canvasRef" 
      :width="labels.length > 0 ? Math.max(500, labels.length * 80) : 500" 
      :height="height"
      :style="{ cursor: clickable && hoveredPoint ? 'pointer' : 'crosshair' }"
      @mousemove="handleMouseMove"
      @mouseleave="handleMouseLeave"
      @click="handleClick"
    ></canvas>
    
    <!-- Tooltip -->
    <Teleport to="body">
      <div 
        v-if="hoveredPoint && showTooltip" 
        class="chart-tooltip"
        :style="{
          left: mousePos.x + 'px',
          top: mousePos.y + 'px',
          pointerEvents: 'none'
        }"
      >
        <div class="tooltip-header">
          <span class="tooltip-color" :style="{ backgroundColor: hoveredPoint.seriesColor }"></span>
          <span class="tooltip-series">{{ hoveredPoint.seriesName }}</span>
        </div>
        <div class="tooltip-label">{{ hoveredPoint.label }}</div>
        <div class="tooltip-value">
          <strong>{{ formatValue(hoveredPoint.value) }}</strong>
          <span v-if="valueUnit">{{ valueUnit }}</span>
        </div>
      </div>
    </Teleport>
    
    <!-- No data message -->
    <div v-if="!seriesWithColors || seriesWithColors.length === 0" class="no-data">
      {{ t('dashboard.noData') }}
    </div>
  </div>
</template>

<style scoped>
.multi-line-chart-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow-x: auto;
  position: relative;
}

.chart-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
  justify-content: center;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
}

.legend-item:hover {
  background: #f3f4f6;
}

.legend-item--disabled {
  opacity: 0.4;
}

.legend-item--disabled .legend-color {
  background-color: #d1d5db !important;
}

.legend-item--clickable {
  cursor: pointer;
}

.legend-item--clickable:hover {
  background: #dbeafe;
  border-color: #3b82f6;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 3px;
  flex-shrink: 0;
}

.legend-label {
  font-size: 12px;
  color: #4b5563;
  font-weight: 500;
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
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 12px;
  pointer-events: none;
  z-index: 1000;
  transform: translate(-50%, -100%);
  margin-top: -12px;
  box-shadow: 0 4px 12px -1px rgba(0, 0, 0, 0.15);
  min-width: 120px;
}

.tooltip-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
  padding-bottom: 6px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.tooltip-color {
  width: 10px;
  height: 10px;
  border-radius: 2px;
}

.tooltip-series {
  font-weight: 600;
  color: #e5e7eb;
  font-size: 11px;
}

.tooltip-label {
  font-weight: 500;
  color: #9ca3af;
  font-size: 11px;
  margin-bottom: 4px;
}

.tooltip-value {
  font-size: 14px;
  color: white;
}

.tooltip-value strong {
  font-size: 16px;
  color: #60a5fa;
}

.no-data {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #9ca3af;
  font-size: 14px;
}
</style>
