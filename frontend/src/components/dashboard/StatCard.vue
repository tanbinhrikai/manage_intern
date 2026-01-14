<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  value: {
    type: [Number, String],
    required: true
  },
  icon: {
    type: String,
    required: true
  },
  color: {
    type: String,
    default: 'blue'
  }
})
</script>

<template>
  <div class="stat-card">
    <div :class="['stat-icon', `icon-${color}`]">
      <svg v-if="icon === 'users'" viewBox="0 0 24 24" fill="currentColor">
        <path d="M12 4a4 4 0 100 8 4 4 0 000-8zM6 8a6 6 0 1112 0A6 6 0 016 8zm2 10a3 3 0 00-3 3 1 1 0 11-2 0 5 5 0 015-5h8a5 5 0 015 5 1 1 0 11-2 0 3 3 0 00-3-3H8z"/>
      </svg>
      <svg v-else-if="icon === 'active'" viewBox="0 0 24 24" fill="currentColor">
        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
      </svg>
      <svg v-else-if="icon === 'warning'" viewBox="0 0 24 24" fill="currentColor">
        <path d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z"/>
      </svg>
      <svg v-else-if="icon === 'mentor'" viewBox="0 0 24 24" fill="currentColor">
        <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
      </svg>
    </div>
    <div class="stat-content">
      <span class="stat-title">{{ title }}</span>
      <span class="stat-value">{{ value }}</span>
    </div>
  </div>
</template>

<style scoped>
.stat-card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon svg {
  width: 24px;
  height: 24px;
}

.icon-blue {
  background: #e0f2fe;
  color: #0284c7;
}

.icon-green {
  background: #dcfce7;
  color: #16a34a;
}

.icon-yellow {
  background: #fef3c7;
  color: #d97706;
}

.icon-purple {
  background: #e0e7ff;
  color: #4f46e5;
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-title {
  font-size: 13px;
  color: #6b7280;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
}
</style>
