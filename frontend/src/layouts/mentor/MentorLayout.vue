<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Mentor Layout Header -->
    <header class="bg-white shadow p-4 flex items-center justify-between">
      <h1 class="text-xl font-bold">Mentor Dashboard</h1>
      <slot name="header-actions" />
    </header>
    <!-- Main Content -->
    <main class="p-6">
      <slot />
    </main>
  </div>
</template>

<script setup>
// MentorLayout basic placeholder
</script>
