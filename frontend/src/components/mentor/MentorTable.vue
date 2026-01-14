<template>
  <div>
    <div class="overflow-x-auto">
      <table class="w-full text-left border-collapse">
        <thead>
          <tr class="bg-gray-100 text-gray-600 text-xs uppercase font-semibold tracking-wide">
            <th class="px-6 py-4 border-b border-gray-200">Tên Mentor</th>
            <th class="px-6 py-4 border-b border-gray-200">Team/Phòng ban</th>
            <th class="px-6 py-4 border-b border-gray-200">Số Intern phụ trách</th>
            <th class="px-6 py-4 border-b border-gray-200">Hành động</th>
          </tr>
        </thead>
        
        <tbody class="text-sm text-gray-700">
          <tr 
            v-for="mentor in mentors" 
            :key="mentor.id" 
            class="hover:bg-gray-50 transition-colors border-b border-gray-100 last:border-none"
          >
            <td class="px-6 py-4 font-medium text-gray-900">{{ mentor.fullName }}</td>
            <td class="px-6 py-4">{{ mentor.team || '-' }}</td>
            <td class="px-6 py-4">{{ mentor.internCount || 0 }}</td>
            <td class="px-6 py-4">
              <div class="flex items-center gap-3">
                <button 
                  class="text-blue-500 hover:text-blue-700 hover:underline font-medium transition-colors" 
                  @click="$emit('detail', mentor)"
                >
                  Chi tiết
                </button>
                <button 
                  class="text-blue-500 hover:text-blue-700 hover:underline font-medium transition-colors" 
                  @click="$emit('edit', mentor)"
                >
                  Chỉnh sửa
                </button>
              </div>
            </td>
          </tr>
          
          <tr v-if="mentors.length === 0">
            <td colspan="4" class="px-6 py-8 text-center text-gray-400">
              Không có dữ liệu hiển thị
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="flex justify-end items-center mt-6 gap-2">
      <button 
        :disabled="currentPage === 1" 
        @click="$emit('page-change', currentPage - 1)" 
        class="px-3 py-1.5 border border-gray-300 rounded text-sm text-gray-600 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed bg-white"
      >
        Trước
      </button>
      
      <button 
        v-for="page in totalPages" 
        :key="page" 
        @click="$emit('page-change', page)" 
        :class="[
          'px-3 py-1.5 border rounded text-sm font-medium transition-colors',
          currentPage === page 
            ? 'bg-blue-500 text-white border-blue-500' 
            : 'bg-white border-gray-300 text-gray-600 hover:bg-gray-50'
        ]"
      >
        {{ page }}
      </button>
      
      <button 
        :disabled="currentPage === totalPages" 
        @click="$emit('page-change', currentPage + 1)" 
        class="px-3 py-1.5 border border-gray-300 rounded text-sm text-gray-600 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed bg-white"
      >
        Tiếp
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  mentors: { type: Array, default: () => [] },
  currentPage: { type: Number, default: 1 },
  totalPages: { type: Number, default: 1 }
})
</script>