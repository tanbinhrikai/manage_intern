<template>
  <div>
    <div class="overflow-x-auto">
      <table class="w-full text-left border-collapse">
        <thead>
          <tr
            class="bg-gray-100 text-gray-600 text-xs uppercase font-semibold tracking-wide">
            <th class="px-6 py-4 border-b border-gray-200">Mentor Name</th>
            <th class="px-6 py-4 border-b border-gray-200">Team/Department</th>
            <th class="px-6 py-4 border-b border-gray-200 text-center">
              Status
            </th>
            <th class="px-6 py-4 border-b border-gray-200">Actions</th>
          </tr>
        </thead>

        <tbody class="text-sm text-gray-700">
          <tr
            v-for="mentor in mentors"
            :key="mentor.id"
            class="hover:bg-gray-50 transition-colors border-b border-gray-100 last:border-none">
            <td class="px-6 py-4 font-medium text-gray-900">
              <div class="flex flex-col">
                <span>{{ mentor.fullName }}</span>
                <span class="text-xs text-gray-500">{{ mentor.email }}</span>
              </div>
            </td>

            <td class="px-6 py-4">
              <span
                v-if="mentor.department"
                class="bg-blue-100 text-blue-800 text-xs font-medium px-2.5 py-0.5 rounded">
                {{ mentor.department.name }}
              </span>
              <span v-else class="text-gray-400">-</span>
            </td>

            <td class="px-6 py-4 text-center">
              <button
                @click="$emit('toggle-status', mentor)"
                :class="[
                  'inline-flex items-center gap-1.5 py-1 px-3 rounded-full text-xs font-medium border transition-colors duration-200',
                  mentor.active
                    ? 'bg-green-100 text-green-800 border-green-200 hover:bg-green-200'
                    : 'bg-red-100 text-red-800 border-red-200 hover:bg-red-200',
                ]"
                title="Click to toggle status">
                <span
                  :class="[
                    'w-1.5 h-1.5 rounded-full',
                    mentor.active ? 'bg-green-600' : 'bg-red-600',
                  ]"></span>

                {{ mentor.active ? "Active" : "Inactive" }}
              </button>
            </td>

            <td class="px-6 py-4">
              <div class="flex items-center gap-3">
                <button
                  class="text-blue-500 hover:text-blue-700 hover:underline font-medium transition-colors"
                  @click="$emit('detail', mentor)">
                  Detail
                </button>
                <button
                  class="text-blue-500 hover:text-blue-700 hover:underline font-medium transition-colors"
                  @click="$emit('edit', mentor)">
                  Edit
                </button>
              </div>
            </td>
          </tr>

          <tr v-if="mentors.length === 0">
            <td colspan="4" class="px-6 py-8 text-center text-gray-400">
              No data available.
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="flex justify-end items-center mt-6 gap-2">
      <button
        :disabled="currentPage === 1"
        @click="$emit('page-change', currentPage - 1)"
        class="px-3 py-1.5 border border-gray-300 rounded text-sm text-gray-600 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed bg-white">
        Previous
      </button>

      <button
        v-for="page in totalPages"
        :key="page"
        @click="$emit('page-change', page)"
        :class="[
          'px-3 py-1.5 border rounded text-sm font-medium transition-colors',
          currentPage === page
            ? 'bg-blue-500 text-white border-blue-500'
            : 'bg-white border-gray-300 text-gray-600 hover:bg-gray-50',
        ]">
        {{ page }}
      </button>

      <button
        :disabled="currentPage === totalPages"
        @click="$emit('page-change', currentPage + 1)"
        class="px-3 py-1.5 border border-gray-300 rounded text-sm text-gray-600 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed bg-white">
        Next
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  mentors: { type: Array, default: () => [] },
  currentPage: { type: Number, default: 1 },
  totalPages: { type: Number, default: 1 },
});

// Quan trọng: Phải khai báo toggle-status ở đây
defineEmits(["detail", "edit", "page-change", "toggle-status"]);
</script>
