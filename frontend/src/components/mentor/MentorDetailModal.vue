<template>
  <div class="fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50">
    <div class="bg-white p-6 rounded shadow w-full max-w-md">
      <h3 class="font-bold text-lg mb-4">Chi tiết Mentor</h3>
      <div class="mb-2"><strong>Tên Mentor:</strong> {{ mentor.fullName }}</div>
      <div class="mb-2"><strong>Team/Phòng ban:</strong> {{ mentor.team || '-' }}</div>
      <div class="mb-2"><strong>Số intern phụ trách:</strong> {{ mentor.internCount || 0 }}</div>
      <div class="flex justify-end mt-4">
        <button @click="$emit('close')" class="px-4 py-1 border rounded">Đóng</button>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({ mentor: Object })
</script>
