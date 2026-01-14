<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
    
    <div class="bg-white rounded-lg shadow-xl w-full max-w-md overflow-hidden animate-fade-in-up">
      
      <div class="bg-gray-50 px-6 py-4 border-b border-gray-100 flex justify-between items-center">
        <h3 class="text-lg font-bold text-gray-800">
          {{ isEdit ? 'Cập nhật Mentor' : 'Thêm Mentor mới' }}
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
            class="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-1 focus:ring-blue-500 transition-colors"
            placeholder="Nguyễn Văn A"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input 
            v-model="formData.email" 
            type="email" 
            required
            class="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-1 focus:ring-blue-500 transition-colors"
            placeholder="example@rikai.com"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Ngày sinh</label>
          <input 
            v-model="formData.dateOfBirth" 
            type="date"
            required
            :max="maxDate"
            class="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-1 focus:ring-blue-500 transition-colors"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Phòng ban / Team</label>
          <select 
            v-model="formData.departmentId"
            required
            class="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-1 focus:ring-blue-500 bg-white transition-colors"
          >
            <option value="" disabled>Chọn phòng ban</option>
            <option v-for="dept in departments" :key="dept.id" :value="dept.id">
              {{ dept.name }}
            </option>
          </select>
        </div>

        <div v-if="isEdit" class="flex items-center gap-2">
            <input 
                id="isActive"
                v-model="formData.isActive" 
                type="checkbox" 
                class="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
            />
            <label for="isActive" class="text-sm font-medium text-gray-700 select-none cursor-pointer">
                Đang hoạt động (Active)
            </label>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">
            Mật khẩu {{ isEdit ? '(Để trống nếu không đổi)' : '' }}
          </label>
          <input 
            v-model="formData.password" 
            type="password" 
            :required="!isEdit"
            pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$"
            title="Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số."
            class="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-1 focus:ring-blue-500 transition-colors"
            placeholder="********"
          />
        </div>

      </form>

      <div class="bg-gray-50 px-6 py-4 flex justify-end gap-3 border-t border-gray-100">
        <button 
          @click="$emit('close')" 
          type="button"
          class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded hover:bg-gray-50 transition-colors"
        >
          Hủy bỏ
        </button>
        <button 
          @click="handleSave" 
          type="button"
          class="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded hover:bg-blue-700 shadow-sm transition-colors"
        >
          {{ isEdit ? 'Lưu thay đổi' : 'Tạo mới' }}
        </button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
// Import API lấy danh sách phòng ban
import { getDepartment } from '@/api/department' 

const props = defineProps({
  mentor: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'saved'])

// Danh sách phòng ban lấy từ API
const departments = ref([])

const isEdit = computed(() => !!props.mentor)

// Tính ngày max cho dateOfBirth (18 tuổi)
const maxDate = computed(() => {
  const today = new Date()
  const year = today.getFullYear() - 18
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
})

// Form data khớp với UserUpdateDTO
const formData = ref({
  fullName: '',
  email: '',
  password: '',
  dateOfBirth: '',
  departmentId: '', // Lưu ID của department
  isActive: true    // Mặc định active khi tạo mới
})

// Fetch danh sách Department khi Modal hiện lên
onMounted(async () => {
    try {
        const res = await getDepartment();
        // Giả sử API trả về: { data: { items: [...] } } hoặc { data: [...] }
        // Bạn cần check log response để trỏ đúng mảng departments
        departments.value = res.data?.data || []; 
    } catch (error) {
        console.error("Lỗi tải departments:", error);
    }
})

// Fill dữ liệu khi sửa (Watch prop mentor)
watch(() => props.mentor, (newVal) => {
  if (newVal) {
    // Logic lấy departmentId:
    // API list user thường trả về object department: { id: 1, name: '...' }
    // Nên ta cần check newVal.department?.id
    const deptId = newVal.department ? newVal.department.id : (newVal.departmentId || '');

    formData.value = {
      fullName: newVal.fullName,
      email: newVal.email,
      dateOfBirth: newVal.dateOfBirth,
      isActive: newVal.isActive,
      departmentId: deptId,
      password: '' // Luôn reset password khi mở form edit
    }
  } else {
    // Reset form khi tạo mới
    formData.value = { 
        fullName: '', 
        email: '', 
        password: '', 
        dateOfBirth: '', 
        departmentId: '', 
        isActive: true 
    }
  }
}, { immediate: true })

function handleSave() {
  // Validate cơ bản
  if (!formData.value.fullName || !formData.value.email || !formData.value.departmentId) {
    alert("Vui lòng điền đầy đủ thông tin!");
    return;
  }
  
  // Clone data để xử lý trước khi gửi
  const payload = { ...formData.value }

  // Xử lý logic password cho Update
  // Nếu là Edit và password rỗng -> Xóa field password khỏi payload (để Backend không update password thành chuỗi rỗng)
  if (isEdit.value && !payload.password) {
    delete payload.password; 
  }
  
  // Emit dữ liệu ra cha để gọi API create/update
  emit('saved', payload)
}
</script>