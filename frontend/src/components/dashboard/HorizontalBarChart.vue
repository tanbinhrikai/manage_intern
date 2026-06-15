<script setup>
import { computed, onMounted, ref, watch } from "vue";

const props = defineProps({
  data: {
    type: Array,
    required: true,
    default: () => [],
  },
  height: {
    type: Number,
    default: 250,
  },
});

const canvasRef = ref(null);
const maxValue = computed(() => {
  if (!props.data || props.data.length === 0) return 1;
  return Math.max(...props.data.map((item) => item.value || 0));
});

// Color palette for bars
const colors = [
  "#3b82f6",
  "#8b5cf6",
  "#ec4899",
  "#f59e0b",
  "#10b981",
  "#06b6d4",
  "#ef4444",
  "#6366f1",
  "#14b8a6",
  "#f97316",
];

const getColor = (index) => {
  return colors[index % colors.length];
};

onMounted(() => {
  drawChart();
});

watch(
  () => props.data,
  () => {
    drawChart();
  },
  { deep: true }
);

const drawChart = () => {
  const canvas = canvasRef.value;
  if (!canvas || !props.data || props.data.length === 0) return;

  const ctx = canvas.getContext("2d");
  const paddingLeft = 90; // Large padding on the left for labels
  const paddingRight = 40;
  const paddingTop = 20;
  const paddingBottom = 40;

  const chartWidth = canvas.width - paddingLeft - paddingRight;
  const chartHeight = canvas.height - paddingTop - paddingBottom;
  const barHeight = (chartHeight / props.data.length) * 0.6;
  const barSpacing = (chartHeight / props.data.length) * 0.4;

  ctx.clearRect(0, 0, canvas.width, canvas.height);

  // Draw bars
  props.data.forEach((item, index) => {
    const y = paddingTop + index * (barHeight + barSpacing) + barSpacing / 2;
    const barWidth = maxValue.value > 0 ? (item.value / maxValue.value) * chartWidth : 0;

    // Draw bar with color from palette
    ctx.fillStyle = getColor(index);
    ctx.fillRect(paddingLeft, y, barWidth, barHeight);

    // Draw value to the right of the bar
    ctx.fillStyle = "#1f2937";
    ctx.font = "bold 12px Inter, sans-serif";
    ctx.textAlign = "left";
    ctx.textBaseline = "middle";
    ctx.fillText(item.value.toString(), paddingLeft + barWidth + 8, y + barHeight / 2);

    // Draw label to the left of the bar
    ctx.fillStyle = "#4b5563";
    ctx.font = "bold 11px Inter, sans-serif";
    ctx.textAlign = "right";
    ctx.textBaseline = "middle";

    // Truncate long labels
    const label =
      item.label.length > 12 ? item.label.substring(0, 10) + "..." : item.label;
    ctx.fillText(label, paddingLeft - 10, y + barHeight / 2);
  });

  // Draw X-axis line at the bottom
  ctx.strokeStyle = "#e5e7eb";
  ctx.lineWidth = 1;
  ctx.beginPath();
  ctx.moveTo(paddingLeft, canvas.height - paddingBottom);
  ctx.lineTo(canvas.width - paddingRight, canvas.height - paddingBottom);
  ctx.stroke();

  // Draw X-axis ticks/labels
  const xAxisSteps = 5;
  ctx.fillStyle = "#9ca3af";
  ctx.font = "10px Inter, sans-serif";
  ctx.textAlign = "center";
  ctx.textBaseline = "top";

  for (let i = 0; i <= xAxisSteps; i++) {
    const value = Math.round((maxValue.value / xAxisSteps) * i);
    const x = paddingLeft + (chartWidth / xAxisSteps) * i;
    ctx.fillText(value.toString(), x, canvas.height - paddingBottom + 8);

    // Draw vertical grid line
    if (i > 0) {
      ctx.strokeStyle = "#f3f4f6";
      ctx.lineWidth = 1;
      ctx.beginPath();
      ctx.moveTo(x, paddingTop);
      ctx.lineTo(x, canvas.height - paddingBottom);
      ctx.stroke();
    }
  }
};
</script>

<template>
  <div class="bar-chart-container">
    <canvas ref="canvasRef" width="450" height="250"></canvas>
  </div>
</template>

<style scoped>
.bar-chart-container {
  width: 100%;
  display: flex;
  justify-content: center;
  overflow-x: auto;
}

canvas {
  display: block;
}
</style>
