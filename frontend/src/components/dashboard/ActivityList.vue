<script setup>
defineProps({
  activities: {
    type: Array,
    required: true
  }
})

const getIconClass = (type) => {
  const types = {
    new: 'icon-blue',
    evaluation: 'icon-green',
    warning: 'icon-yellow',
    system: 'icon-purple',
    completed: 'icon-cyan',
    status_change: 'icon-purple'
  }
  return types[type] || 'icon-blue'
}
</script>

<template>
  <div class="activity-list">
    <div v-for="(activity, index) in activities" :key="index" class="activity-item">
      <div :class="['activity-icon', getIconClass(activity.type)]">
        <svg v-if="activity.type === 'new'" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
        </svg>
        <svg v-else-if="activity.type === 'evaluation'" viewBox="0 0 24 24" fill="currentColor">
          <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
        </svg>
        <svg v-else-if="activity.type === 'warning'" viewBox="0 0 24 24" fill="currentColor">
          <circle cx="12" cy="12" r="10"/>
        </svg>
        <svg v-else-if="activity.type === 'system'" viewBox="0 0 24 24" fill="currentColor">
          <circle cx="12" cy="12" r="10"/>
        </svg>
        <svg v-else-if="activity.type === 'completed'" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
        </svg>
        <svg v-else-if="activity.type === 'status_change'" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
        </svg>
      </div>
      <div class="activity-content">
        <p class="activity-text" v-html="activity.text"></p>
      </div>
      <span class="activity-time">{{ activity.time }}</span>
    </div>
  </div>
</template>

<style scoped>
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.activity-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.activity-icon svg {
  width: 16px;
  height: 16px;
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
  background: #fce7f3;
  color: #db2777;
}

.icon-cyan {
  background: #cffafe;
  color: #0891b2;
}

.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-text {
  font-size: 13px;
  color: #374151;
  line-height: 1.5;
  margin: 0;
}

.activity-text :deep(strong) {
  font-weight: 600;
  color: #1f2937;
}

.activity-time {
  font-size: 12px;
  color: #9ca3af;
  white-space: nowrap;
}
</style>
