<template>
  <div class="bg-gray-100 min-h-screen p-8 font-sans">
    <div class="bg-white rounded-lg shadow-sm p-8 min-h-[600px]">
      <h2 class="text-2xl font-bold text-gray-800 mb-8 tracking-tight">
        Quản lý Mentor
      </h2>
      <div class="flex justify-between items-center mb-6">
        <div class="flex gap-4 w-1/2">
          <input
            v-model="searchName"
            class="w-full border border-gray-300 rounded px-4 py-2 text-gray-700 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 placeholder-gray-400"
            placeholder=" " />
          <input
            v-model="searchTeam"
            class="w-full border border-gray-300 rounded px-4 py-2 text-gray-700 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 placeholder-gray-400"
            placeholder=" " />
        </div>

        <button
          @click="openAddMentor"
          class="bg-blue-500 hover:bg-blue-600 text-white px-6 py-2 rounded text-sm font-medium transition-colors duration-200 shadow-sm">
          Thêm Mentor mới
        </button>
      </div>

      <MentorTable
        :mentors="filteredMentors"
        :current-page="currentPage"
        :total-pages="totalPages"
        @edit="openEditMentor"
        @detail="openDetailMentor"
        @page-change="changePage" />
    </div>

    <MentorFormModal
      v-if="showMentorForm"
      :mentor="selectedMentor"
      @close="closeMentorForm"
      @saved="handleSaveMentor" />

    <MentorDetailModal
      v-if="showMentorDetail"
      :mentor="selectedMentor"
      @close="closeMentorDetail" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import MentorLayout from "@/layouts/mentor/MentorLayout.vue";

import { getMentors, createMentor, updateMentor } from '@/api/user'
import MentorFormModal from "../../components/mentor/MentorFormModal.vue";
import MentorDetailModal from "../../components/mentor/MentorDetailModal.vue";
import MentorTable from "../../components/mentor/MentorTable.vue";

const mentors = ref([]);
const currentPage = ref(1);
const pageSize = 8;
const totalPages = ref(1);
const searchName = ref("");
const searchTeam = ref("");
const showMentorForm = ref(false);
const showMentorDetail = ref(false);
const selectedMentor = ref(null);

const filteredMentors = computed(() => {
  let result = mentors.value;
  if (searchName.value) {
    result = result.filter((m) =>
      m.fullName.toLowerCase().includes(searchName.value.toLowerCase())
    );
  }
  if (searchTeam.value) {
    result = result.filter(
      (m) =>
        m.team && m.team.toLowerCase().includes(searchTeam.value.toLowerCase())
    );
  }
  return result.slice(
    (currentPage.value - 1) * pageSize,
    currentPage.value * pageSize
  );
});

function fetchMentors() {
  getMentors().then((res) => {
    console.log(res.data);
    mentors.value = res.data?.data?.items || [];
    console.log(mentors.value);
    totalPages.value = Math.ceil(mentors.value.length / pageSize);
    console.log("Total Pages:", totalPages.value);
  });
}

async function handleSaveMentor(formData) {
  try {
    if (selectedMentor.value) {
      // --- TRƯỜNG HỢP UPDATE ---
      // Lấy ID từ selectedMentor đang chọn
      const id = selectedMentor.value.id;
      await updateMentor(id, formData);
      alert('Cập nhật thành công!');
    } else {
      // --- TRƯỜNG HỢP TẠO MỚI ---
      await createMentor(formData);
      alert('Tạo mới thành công!');
    }
    
    // Sau khi gọi API thành công:
    // 1. Tải lại danh sách
    fetchMentors();
    // 2. Đóng form
    closeMentorForm();
    
  } catch (error) {
    console.error("Lỗi khi lưu:", error);
    // Xử lý hiển thị lỗi từ Backend trả về (nếu có)
    if (error.response && error.response.data) {
       alert('Lỗi: ' + JSON.stringify(error.response.data));
    } else {
       alert('Có lỗi xảy ra, vui lòng thử lại.');
    }
  }
}
function openAddMentor() {
  selectedMentor.value = null;
  showMentorForm.value = true;
}
function openEditMentor(mentor) {
  selectedMentor.value = mentor;
  showMentorForm.value = true;
}
function closeMentorForm() {
  showMentorForm.value = false;
}
function openDetailMentor(mentor) {
  selectedMentor.value = mentor;
  showMentorDetail.value = true;
}
function closeMentorDetail() {
  showMentorDetail.value = false;
}
function changePage(page) {
  currentPage.value = page;
}
onMounted(fetchMentors);
</script>
