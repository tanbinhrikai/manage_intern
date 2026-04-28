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
    default: 300,
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
  const padding = 40;
  const chartWidth = canvas.width - padding * 2;
  const chartHeight = canvas.height - padding * 2;
  const barWidth = (chartWidth / props.data.length) * 0.7;
  const barSpacing = (chartWidth / props.data.length) * 0.3;

  ctx.clearRect(0, 0, canvas.width, canvas.height);

  // Draw bars
  props.data.forEach((item, index) => {
    const x = padding + index * (barWidth + barSpacing) + barSpacing / 2;
    const barHeight = (item.value / maxValue.value) * chartHeight;
    const y = canvas.height - padding - barHeight;

    // Draw bar with color from palette
    ctx.fillStyle = getColor(index);
    ctx.fillRect(x, y, barWidth, barHeight);

    // Draw value on top of bar
    ctx.fillStyle = "#1f2937";
    ctx.font = "bold 12px Inter, sans-serif";
    ctx.textAlign = "center";
    ctx.textBaseline = "bottom";
    ctx.fillText(item.value.toString(), x + barWidth / 2, y - 4);

    // Draw label below bar
    ctx.fillStyle = "#6b7280";
    ctx.font = "11px Inter, sans-serif";
    ctx.textAlign = "center";
    ctx.textBaseline = "top";

    // Truncate long labels
    const label =
      item.label.length > 12 ? item.label.substring(0, 10) + "..." : item.label;
    ctx.fillText(label, x + barWidth / 2, canvas.height - padding + 8);
  });

  // Draw Y-axis
  ctx.strokeStyle = "#e5e7eb";
  ctx.lineWidth = 1;
  ctx.beginPath();
  ctx.moveTo(padding, padding);
  ctx.lineTo(padding, canvas.height - padding);
  ctx.stroke();

  // Draw Y-axis labels
  const yAxisSteps = 5;
  ctx.fillStyle = "#9ca3af";
  ctx.font = "10px Inter, sans-serif";
  ctx.textAlign = "right";
  ctx.textBaseline = "middle";

  for (let i = 0; i <= yAxisSteps; i++) {
    const value = Math.round((maxValue.value / yAxisSteps) * (yAxisSteps - i));
    const y = padding + (chartHeight / yAxisSteps) * i;
    ctx.fillText(value.toString(), padding - 8, y);

    // Draw grid line
    if (i > 0) {
      ctx.strokeStyle = "#f3f4f6";
      ctx.lineWidth = 1;
      ctx.beginPath();
      ctx.moveTo(padding, y);
      ctx.lineTo(canvas.width - padding, y);
      ctx.stroke();
    }
  }
};
</script>

<template>
  <div class="bar-chart-container">
    <canvas
      ref="canvasRef"
      :width="data.length > 0 ? Math.max(400, data.length * 80) : 400"
      :height="height"
    ></canvas>
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
