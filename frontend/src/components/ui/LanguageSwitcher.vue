<script setup>
import { ref, computed } from 'vue'
import { useLocaleStore } from '@/locales/locale'

const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)

const showDropdown = ref(false)

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
}

const changeLanguage = (locale) => {
  localeStore.setLocale(locale)
  showDropdown.value = false
}

const closeDropdown = () => {
  showDropdown.value = false
}

defineExpose({ closeDropdown })
</script>

<template>
  <div class="language-switcher" @click.stop>
    <button class="language-btn" @click="toggleDropdown">
      <svg class="globe-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10"/>
        <path d="M2 12h20"/>
        <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
      </svg>
      <span>{{ t('language.' + localeStore.currentLocale) }}</span>
      <svg class="chevron-icon" :class="{ 'rotate': showDropdown }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M6 9l6 6 6-6"/>
      </svg>
    </button>
    <div v-if="showDropdown" class="language-dropdown">
      <button 
        v-for="locale in localeStore.availableLocales" 
        :key="locale"
        @click="changeLanguage(locale)"
        :class="['dropdown-item', { 'active': locale === localeStore.currentLocale }]"
      >
        {{ t('language.' + locale) }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.language-switcher {
  position: relative;
}

.language-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #374151;
  transition: all 0.2s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.language-btn:hover {
  background: #f9fafb;
  border-color: #d1d5db;
}

.globe-icon {
  width: 18px;
  height: 18px;
  color: #6b7280;
}

.chevron-icon {
  width: 16px;
  height: 16px;
  color: #9ca3af;
  transition: transform 0.2s;
}

.chevron-icon.rotate {
  transform: rotate(180deg);
}

.language-dropdown {
  position: absolute;
  top: calc(100% + 4px);
  right: 0;
  min-width: 140px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  z-index: 100;
}

.dropdown-item {
  width: 100%;
  padding: 10px 14px;
  text-align: left;
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 14px;
  color: #374151;
  transition: background 0.15s;
}

.dropdown-item:hover {
  background: #f3f4f6;
}

.dropdown-item.active {
  background: #eff6ff;
  color: #3b82f6;
  font-weight: 500;
}
</style>
