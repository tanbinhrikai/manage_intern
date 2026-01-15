<script setup>
import { ref, computed, watch, reactive, onMounted } from 'vue'
import { useLocaleStore } from '@/locales/locale'
import { getDepartment } from '@/api/department'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  mentor: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:visible', 'save'])

const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)

const formRef = ref(null)
const loading = ref(false)
const departments = ref([])

const isEdit = computed(() => !!props.mentor)

const formData = reactive({
  fullName: '',
  email: '',
  password: '',
  dateOfBirth: '',
  departmentId: '',
  isActive: true
})

const formRules = computed(() => ({
  fullName: [{ required: true, message: t.value('mentorManagement.messages.validationError'), trigger: 'blur' }],
  email: [
    { required: true, message: t.value('mentorManagement.messages.validationError'), trigger: 'blur' },
    { type: 'email', message: t.value('login.validation.emailInvalid'), trigger: 'blur' }
  ],
  dateOfBirth: [{ required: true, message: t.value('mentorManagement.messages.validationError'), trigger: 'change' }],
  departmentId: [{ required: true, message: t.value('mentorManagement.messages.validationError'), trigger: 'change' }],
  password: isEdit.value ? [] : [{ required: true, message: t.value('mentorManagement.messages.validationError'), trigger: 'blur' }]
}))

const disabledDate = (time) => {
  const eighteenYearsAgo = new Date()
  eighteenYearsAgo.setFullYear(eighteenYearsAgo.getFullYear() - 18)
  return time.getTime() > eighteenYearsAgo.getTime()
}

function resetForm() {
  formData.fullName = ''
  formData.email = ''
  formData.password = ''
  formData.dateOfBirth = ''
  formData.departmentId = ''
  formData.isActive = true
}

function fillForm(mentor) {
  formData.fullName = mentor.fullName || ''
  formData.email = mentor.email || ''
  formData.dateOfBirth = mentor.dateOfBirth || ''
  formData.departmentId = mentor.department?.id || ''
  formData.isActive = mentor.active ?? true
  formData.password = ''
}

watch(() => props.visible, (newVal) => {
  if (newVal) {
    if (props.mentor) {
      fillForm(props.mentor)
    } else {
      resetForm()
    }
  }
})

watch(() => props.mentor, (newVal) => {
  if (newVal && props.visible) {
    fillForm(newVal)
  }
}, { immediate: true })

function handleClose() {
  emit('update:visible', false)
  formRef.value?.resetFields()
}

async function handleSubmit() {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  const payload = {
    fullName: formData.fullName,
    email: formData.email,
    dateOfBirth: formData.dateOfBirth,
    departmentId: formData.departmentId,
    isActive: formData.isActive
  }

  if (formData.password) {
    payload.password = formData.password
  }

  loading.value = true
  emit('save', payload, () => {
    loading.value = false
  })
}

async function fetchDepartments() {
  try {
    const res = await getDepartment()
    departments.value = res.data?.data || []
  } catch (error) {
    console.error("Failed to load departments:", error)
  }
}

onMounted(fetchDepartments)
</script>

<template>
  <el-dialog 
    :model-value="visible"
    :title="isEdit ? t('mentorManagement.form.editTitle') : t('mentorManagement.form.addTitle')"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form 
      ref="formRef"
      :model="formData" 
      :rules="formRules"
      label-position="top"
    >
      <el-form-item :label="t('mentorManagement.form.fullName')" prop="fullName">
        <el-input 
          v-model="formData.fullName" 
          :placeholder="t('mentorManagement.form.fullNamePlaceholder')"
        />
      </el-form-item>

      <el-form-item :label="t('mentorManagement.form.email')" prop="email">
        <el-input 
          v-model="formData.email" 
          type="email"
          :placeholder="t('mentorManagement.form.emailPlaceholder')"
        />
      </el-form-item>

      <el-form-item :label="t('mentorManagement.form.dateOfBirth')" prop="dateOfBirth">
        <el-date-picker
          v-model="formData.dateOfBirth"
          type="date"
          :placeholder="t('mentorManagement.form.dateOfBirth')"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%"
          :disabled-date="disabledDate"
        />
      </el-form-item>

      <el-form-item :label="t('mentorManagement.form.department')" prop="departmentId">
        <el-select 
          v-model="formData.departmentId" 
          :placeholder="t('mentorManagement.form.selectDepartment')"
          style="width: 100%"
        >
          <el-option
            v-for="dept in departments"
            :key="dept.id"
            :label="dept.name"
            :value="dept.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item v-if="isEdit">
        <el-checkbox v-model="formData.isActive">
          {{ t('mentorManagement.form.isActive') }}
        </el-checkbox>
      </el-form-item>

      <el-form-item 
        :label="t('mentorManagement.form.password') + (isEdit ? ' ' + t('mentorManagement.form.passwordHint') : '')" 
        prop="password"
      >
        <el-input 
          v-model="formData.password" 
          type="password"
          :placeholder="t('mentorManagement.form.passwordPlaceholder')"
          show-password
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">
        {{ t('mentorManagement.form.cancel') }}
      </el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">
        {{ isEdit ? t('mentorManagement.form.save') : t('mentorManagement.form.create') }}
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
:deep(.el-dialog__header) {
  border-bottom: 1px solid #ebeef5;
  padding: 16px 20px;
  margin: 0;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #ebeef5;
  padding: 16px 20px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}
</style>
