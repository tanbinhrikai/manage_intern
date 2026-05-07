<script setup>
import { computed, ref, watch } from "vue"; // Import 'watch'
import { useRoute } from "vue-router";
import { useLocaleStore } from "@/locales/locale";
import { useAuthStore } from "@/stores/auth";

const route = useRoute();
const localeStore = useLocaleStore();
const authStore = useAuthStore();
const t = computed(() => localeStore.t);

// 1. Initialize as empty array (All closed by default)
const expandedGroups = ref([]);

// Menu Data structure
const menuGroups = [
  {
    key: "dashboard",
    path: "/admin/dashboard",
    icon: "dashboard",
    type: "link",
  },
  {
    key: "users",
    label: "sidebar.userManagement",
    icon: "users",
    type: "group",
    children: [
      { key: "interns", path: "/admin/interns" },
      { key: "mentors", path: "/admin/mentors" },
      { key: "hrs", path: "/admin/hrs" },
    ],
  },
  {
    key: "organization",
    label: "sidebar.organization",
    icon: "building",
    type: "group",
    children: [
      { key: "departments", path: "/admin/departments" },
      { key: "positions", path: "/admin/positions" },
      { key: "batches", path: "/admin/batches" },
      { key: "roadmapBuilder", path: "/admin/roadmaps/builder" },
    ],
  },
  {
    key: "evaluation",
    label: "sidebar.evaluation",
    icon: "evaluationSession",
    type: "group",
    children: [
      { key: "evaluationCriteria", path: "/admin/evaluation-criteria" },
      { key: "evaluationSessions", path: "/admin/evaluation-sessions" },
    ],
  },
  {
    key: "system",
    label: "sidebar.system",
    icon: "config",
    type: "group",
    children: [
      { key: "systemConfig", path: "/admin/system-config" },
      { key: "auditLog", path: "/admin/audit-log" },
    ],
  },
];

// Toggle logic for manual click on header
const toggleGroup = (key) => {
  const index = expandedGroups.value.indexOf(key);
  if (index === -1) {
    expandedGroups.value.push(key);
  } else {
    expandedGroups.value.splice(index, 1);
  }
};

const isExpanded = (key) => expandedGroups.value.includes(key);

// Helper to check if a specific child path is active
const isActive = (path) =>
  route.path === path || route.path.startsWith(path + "/");

// Helper to check if a group has any active children
const isGroupActive = (children) => {
  return children.some((child) => isActive(child.path));
};

// 2. Watcher: Automatically expand the group if the user navigates to a child
// 'immediate: true' ensures this runs on page load/refresh as well
watch(
  () => route.path,
  () => {
    menuGroups.forEach((group) => {
      // If group has children and one of them is currently active
      if (group.type === "group" && isGroupActive(group.children)) {
        // If not already expanded, add it to the list
        if (!expandedGroups.value.includes(group.key)) {
          expandedGroups.value.push(group.key);
        }
      }
    });
  },
  { immediate: true }
);
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <div class="logo-container">
        <span class="brand-name">InternHub</span>
      </div>
    </div>

    <nav class="sidebar-nav">
      <div v-for="group in menuGroups" :key="group.key">
        <router-link
          v-if="group.type === 'link'"
          :to="group.path"
          :class="['nav-item', { active: isActive(group.path) }]"
        >
          <div class="nav-item-content">
            <svg
              class="nav-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <rect x="3" y="3" width="7" height="7" rx="1" />
              <rect x="14" y="3" width="7" height="7" rx="1" />
              <rect x="14" y="14" width="7" height="7" rx="1" />
              <rect x="3" y="14" width="7" height="7" rx="1" />
            </svg>
            <span class="nav-label">{{ t("sidebar." + group.key) }}</span>
          </div>
        </router-link>

        <div v-else class="nav-group">
          <div
            class="nav-group-header"
            :class="{ active: isGroupActive(group.children) }"
            @click="toggleGroup(group.key)"
          >
            <div class="nav-item-content">
              <svg
                v-if="group.icon === 'users'"
                class="nav-icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                <circle cx="9" cy="7" r="4" />
                <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                <path d="M16 3.13a4 4 0 0 1 0 7.75" />
              </svg>
              <svg
                v-else-if="group.icon === 'building'"
                class="nav-icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <rect x="4" y="2" width="16" height="20" rx="2" ry="2" />
                <line x1="9" y1="22" x2="9" y2="22.01" />
                <line x1="15" y1="22" x2="15" y2="22.01" />
                <line x1="12" y1="22" x2="12" y2="22.01" />
                <line x1="12" y1="2" x2="12" y2="22" />
                <line x1="4" y1="6" x2="20" y2="6" />
                <line x1="4" y1="10" x2="20" y2="10" />
                <line x1="4" y1="14" x2="20" y2="14" />
                <line x1="4" y1="18" x2="20" y2="18" />
              </svg>
              <svg
                v-else-if="group.icon === 'evaluationSession'"
                class="nav-icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path
                  d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"
                />
                <polyline points="14 2 14 8 20 8" />
                <line x1="16" y1="13" x2="8" y2="13" />
                <line x1="16" y1="17" x2="8" y2="17" />
                <polyline points="10 9 9 9 8 9" />
              </svg>
              <svg
                v-else-if="group.icon === 'config'"
                class="nav-icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <circle cx="12" cy="12" r="3" />
                <path
                  d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"
                />
              </svg>

              <span class="nav-label">{{
                group.label.startsWith("sidebar.")
                  ? t(group.label)
                  : group.label
              }}</span>
            </div>

            <svg
              class="chevron-icon"
              :class="{ rotate: isExpanded(group.key) }"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <polyline points="6 9 12 15 18 9"></polyline>
            </svg>
          </div>

          <div
            class="nav-group-children"
            :class="{ expanded: isExpanded(group.key) }"
          >
            <router-link
              v-for="child in group.children"
              :key="child.key"
              :to="child.path"
              :class="['sub-item', { active: isActive(child.path) }]"
            >
              <span class="sub-dot"></span>
              <span>{{ t("sidebar." + child.key) }}</span>
            </router-link>
          </div>
        </div>
      </div>
    </nav>
  </aside>
</template>

<style scoped>
.sidebar {
  width: 250px;
  max-height: 100vh;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  border-right: 1px solid #eef2f6;
  font-family: "Inter", -apple-system, BlinkMacSystemFont, sans-serif;
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  border-bottom: 1px solid transparent;
}

.brand-name {
  font-weight: 800;
  font-size: 20px;
  color: #2563eb;
  letter-spacing: -0.5px;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  padding: 16px;
  gap: 4px;
  flex: 1;
  overflow-y: auto;
}

.nav-item,
.nav-group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  color: #64748b;
  transition: all 0.2s ease;
  text-decoration: none;
  margin-bottom: 2px;
}

.nav-item-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-label {
  font-size: 14px;
  font-weight: 500;
  line-height: 1.5;
}

.nav-item:hover,
.nav-group-header:hover {
  background-color: #f8fafc;
  color: #1e293b;
}

.nav-item.active {
  background-color: #eff6ff;
  color: #2563eb;
}

.nav-group-header.active {
  color: #334155;
  font-weight: 600;
}

.nav-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.chevron-icon {
  width: 16px;
  height: 16px;
  color: #94a3b8;
  transition: transform 0.3s ease;
}

.chevron-icon.rotate {
  transform: rotate(180deg);
}

.nav-group-children {
  overflow: hidden;
  max-height: 0;
  transition: max-height 0.3s ease-in-out, opacity 0.3s ease-in-out;
  opacity: 0;
  padding-left: 22px;
}

.nav-group-children.expanded {
  max-height: 500px;
  opacity: 1;
  margin-top: 4px;
  margin-bottom: 8px;
}

.sub-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  text-decoration: none;
  font-size: 13.5px;
  color: #64748b;
  border-radius: 6px;
  transition: all 0.2s;
  position: relative;
}

.sub-item:hover {
  color: #1e293b;
  background-color: #f8fafc;
}

.sub-item.active {
  color: #2563eb;
  font-weight: 500;
  background-color: #eff6ff;
}

.sub-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background-color: #cbd5e1;
  transition: all 0.2s;
}

.sub-item.active .sub-dot {
  background-color: #2563eb;
  transform: scale(1.2);
  box-shadow: 0 0 0 2px #eff6ff;
}

/* Custom scrollbar */
.sidebar-nav::-webkit-scrollbar {
  width: 4px;
}

.sidebar-nav::-webkit-scrollbar-thumb {
  background-color: #e2e8f0;
  border-radius: 4px;
}
</style>