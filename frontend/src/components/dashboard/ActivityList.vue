<script setup>
defineProps({
  activities: {
    type: Array,
    required: true
  }
})

// Centralized activity configuration
const ACTIVITY_CONFIG = {
  created: {
    bg: '#e8f5e9',
    color: '#2e7d32',
    label: 'Created',
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><line x1="19" y1="8" x2="19" y2="14"></line><line x1="22" y1="11" x2="16" y2="11"></line></svg>`
  },
  completed: {
    bg: '#e8f5e9',
    color: '#2e7d32',
    label: 'Completed',
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>`
  },
  updated: {
    bg: '#e3f2fd',
    color: '#1565c0',
    label: 'Updated',
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path><path d="M18.5 2.5a2.121 2.121 0 1 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path></svg>`
  },
  active: {
    bg: '#e3f2fd',
    color: '#1565c0',
    label: 'Active',
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>`
  },
  warning: {
    bg: '#fff3e0',
    color: '#ef6c00',
    label: 'Warning',
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>`
  },
  locked: {
    bg: '#ffe0b2',
    color: '#e65100',
    label: 'Locked',
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>`
  },
  unlocked: {
    bg: '#e0f2f1',
    color: '#00695c',
    label: 'Unlocked',
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 9.9-1"></path></svg>`
  },
  removed: {
    bg: '#ffebee',
    color: '#c62828',
    label: 'Deleted',
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path><line x1="10" y1="11" x2="10" y2="17"></line><line x1="14" y1="11" x2="14" y2="17"></line></svg>`
  },
  dropped: {
    bg: '#ffebee',
    color: '#c62828',
    label: 'Dropped',
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><polyline points="8 12 12 16 16 12"></polyline><line x1="12" y1="8" x2="12" y2="16"></line></svg>`
  },
  system: {
    bg: '#f3f4f6',
    color: '#4b5563',
    label: 'System',
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"></circle><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"></path></svg>`
  }
}

const getConfig = (activity) => {
  const text = (activity.text || '').toLowerCase();
  const type = (activity.type || '').toLowerCase();

  // 1. Check for removed/deleted
  if (text.includes('removed') || text.includes('deleted')) {
    return ACTIVITY_CONFIG.removed;
  }

  // 2. Check for locked/unlocked
  if (text.includes('was locked') || text.includes('has been locked')) {
    return ACTIVITY_CONFIG.locked;
  }
  if (text.includes('was unlocked') || text.includes('has been unlocked')) {
    return ACTIVITY_CONFIG.unlocked;
  }

  // 3. Check for dropped status
  if (text.includes('changed to <strong>dropped</strong>') || text.includes('dropped')) {
    return ACTIVITY_CONFIG.dropped;
  }

  // 4. Check for warning status
  if (text.includes('changed to <strong>warning</strong>') || text.includes('warning')) {
    return ACTIVITY_CONFIG.warning;
  }

  // 5. Check for active status
  if (text.includes('changed to <strong>active</strong>') || text.includes('active')) {
    return ACTIVITY_CONFIG.active;
  }

  // 6. Check for completed status
  if (type === 'completed' || text.includes('completed') || text.includes('complete')) {
    return ACTIVITY_CONFIG.completed;
  }

  // 7. Check for created/new
  if (type === 'new' || text.includes('created') || text.includes('added')) {
    return ACTIVITY_CONFIG.created;
  }

  // 8. Check for updated/edited
  if (text.includes('updated') || text.includes('edited') || text.includes('modified') || type === 'evaluation') {
    return ACTIVITY_CONFIG.updated;
  }

  return ACTIVITY_CONFIG.system;
}
</script>

<template>
  <TransitionGroup name="list" tag="div" class="activity-list">
    <div 
      v-for="(activity, index) in activities" 
      :key="activity.timestamp + '-' + (activity.text || index)" 
      class="activity-item"
    >
      <!-- Timeline stem line -->
      <div class="timeline-line" v-if="index !== activities.length - 1"></div>

      <!-- Badge Icon -->
      <div 
        class="activity-badge-container" 
        :style="{ 
          backgroundColor: getConfig(activity).bg, 
          color: getConfig(activity).color 
        }"
      >
        <span class="activity-icon-span" v-html="getConfig(activity).icon"></span>
      </div>

      <!-- Content -->
      <div class="activity-content">
        <div class="activity-header">
          <span class="activity-time">{{ activity.time }}</span>
        </div>
        <p class="activity-text" v-html="activity.text"></p>
      </div>
    </div>
  </TransitionGroup>
</template>

<style scoped>
.activity-list {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding-left: 8px;
  padding-top: 5px;
}

.activity-item {
  position: relative;
  display: flex;
  gap: 16px;
  padding: 4px 0;
}

.timeline-line {
  position: absolute;
  left: 16px; /* center of the 32px badge */
  top: 36px;
  bottom: -20px;
  width: 2px;
  background-color: #f3f4f6;
  z-index: 0;
}

.activity-badge-container {
  position: relative;
  z-index: 1;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
  transition: all 0.2s ease;
}

.activity-badge-container:hover {
  transform: scale(1.08);
}

.activity-icon-span {
  display: flex;
  align-items: center;
  justify-content: center;
}

.activity-icon-span :deep(svg) {
  width: 15px;
  height: 15px;
}

.activity-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.activity-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.action-label {
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: 2px 8px;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
}

.activity-text {
  font-size: 13px;
  color: #4b5563;
  line-height: 1.45;
  margin: 0;
}

.activity-text :deep(strong) {
  font-weight: 600;
  color: #111827;
}

.activity-time {
  font-size: 12px;
  color: #9ca3af;
  font-weight: 500;
}

/* List transition animations */
.list-enter-active,
.list-leave-active {
  transition: all 0.5s ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}
</style>
