<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
    
    <div class="bg-white rounded-lg shadow-xl w-full max-w-md overflow-hidden animate-fade-in-up">
      
      <div class="bg-gray-50 px-6 py-4 border-b border-gray-100 flex justify-between items-center">
        <h3 class="text-lg font-bold text-gray-800">
          {{ isEdit ? 'Update Information' : 'Register New Member' }}
        </h3>
        <button @click="$emit('close')" class="text-gray-400 hover:text-gray-600 transition-colors">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <form @submit.prevent="handleSave" class="p-6 space-y-4">
        
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Họ và tên</label>
          <input 
            v-model="formData.fullName" 
            type="text" 
            required
            class="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500 transition-colors"
            placeholder="Please enter full name"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Ngày sinh</label>
          <input 
            v-model="formData.dateOfBirth" 
            type="date"
            required
            :max="maxDate"
            class="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500 transition-colors"
          />
          <p class="text-xs text-gray-500 mt-1">Must be over 18 years old.</p>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input 
            v-model="formData.email" 
            type="email" 
            required
            class="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500 transition-colors"
            placeholder="Please enter email"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">
            Password {{ isEdit ? '(Leave blank if not changing)' : '' }}
          </label>
          <input 
            v-model="formData.password" 
            type="password" 
            :required="!isEdit"
            pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$"
            title="Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số."
            class="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500 transition-colors"
            placeholder="********"
          />
          <p class="text-xs text-gray-500 mt-1">
            Minimum 8 characters, including uppercase, lowercase, and numbers.
          </p>
        </div>

      </form>

      <div class="bg-gray-50 px-6 py-4 flex justify-end gap-3 border-t border-gray-100">
        <button 
          @click="$emit('close')" 
          type="button"
          class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded hover:bg-gray-50 transition-colors"
        >
          Cancel
        </button>
        <button 
          @click="handleSave" 
          type="button"
          class="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded hover:bg-blue-700 shadow-sm transition-colors"
        >
          {{ isEdit ? 'Save Changes' : 'Create New' }}
        </button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

// Props nhận từ cha
const props = defineProps({
  mentor: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'saved'])

// Logic kiểm tra xem đang Sửa hay Thêm mới
const isEdit = computed(() => !!props.mentor)

// Tính toán ngày tối đa cho phép (Hiện tại - 18 năm)
// Để hỗ trợ @DobConstraint(min = 18)
const maxDate = computed(() => {
  const today = new Date()
  const year = today.getFullYear() - 18
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
})

// Khởi tạo form data khớp với UserCreateDTO
const formData = ref({
  fullName: '',
  email: '',
  password: '',
  dateOfBirth: ''
})

// Nếu có mentor truyền vào (Sửa) thì fill dữ liệu
watch(() => props.mentor, (newVal) => {
  if (newVal) {
    formData.value = {
      fullName: newVal.fullName,
      email: newVal.email,
      dateOfBirth: newVal.dateOfBirth,
      password: '' // Không bao giờ điền ngược password cũ vào form vì lý do bảo mật
    }
  } else {
    // Reset form khi tạo mới
    formData.value = { fullName: '', email: '', password: '', dateOfBirth: '' }
  }
}, { immediate: true })

function handleSave() {
  // Validate cơ bản trước khi emit
  if (!formData.value.fullName || !formData.value.email || !formData.value.dateOfBirth) {
    alert("Vui lòng điền đầy đủ thông tin!");
    return;
  }

  // Nếu là tạo mới thì bắt buộc có password
  if (!isEdit.value && !formData.value.password) {
     alert("Vui lòng nhập mật khẩu!");
     return;
  }

  // Chuẩn bị payload gửi đi
  // UserCreateDTO mong đợi: email, password, fullName, dateOfBirth
  const payload = { ...formData.value }
  
  // Nếu là edit và user không nhập password, ta xóa field này để tránh gửi chuỗi rỗng lên backend
  // (Tùy thuộc backend xử lý update thế nào, nhưng DTO Create thường yêu cầu password)
  if (isEdit.value && !payload.password) {
    delete payload.password; 
  }

  console.log('Payload sent to Backend:', payload)
  emit('saved', payload)
  emit('close')
}
</script>