<script setup>
import { ref, computed, watch, reactive, onMounted } from 'vue'
import { useLocaleStore } from '@/locales/locale'
import { createUserCreationRequest, createUserUpdateRequest } from '@/types/user'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  mentor: {
    type: Object,
    default: null
  },
  departments: {
    type: Array,
    default: () => []
  },
  userType: {
    type: String,
    default: 'MENTOR',
    validator: (value) => ['MENTOR', 'HR'].includes(value)
  }
})

const emit = defineEmits(['update:visible', 'save'])

const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)

// Dynamic translation prefix based on userType
const translationPrefix = computed(() => 
  props.userType === 'HR' ? 'hrManagement' : 'mentorManagement'
)

const formRef = ref(null)
const loading = ref(false)

const isEdit = computed(() => !!props.mentor)

const formData = reactive(createUserCreationRequest(props.userType))

const formRules = computed(() => ({
  fullName: [{ required: true, message: t.value(`${translationPrefix.value}.messages.validationError`), trigger: 'blur' }],
  email: [
    { required: true, message: t.value(`${translationPrefix.value}.messages.validationError`), trigger: 'blur' },
    { type: 'email', message: t.value('login.validation.emailInvalid'), trigger: 'blur' }
  ],
  dateOfBirth: [],
  departmentId: [{ required: true, message: t.value(`${translationPrefix.value}.messages.validationError`), trigger: 'change' }],
  password: isEdit.value ? [] : [{ required: true, message: t.value(`${translationPrefix.value}.messages.validationError`), trigger: 'blur' }]
}))

const disabledDate = (time) => {
  const date = new Date()
  date.setFullYear(date.getFullYear() - 18)
  return time.getTime() > date.getTime()
}

const defaultDate = computed(() => {
  const date = new Date()
  date.setFullYear(date.getFullYear() - 18)
  return date
})

function resetForm() {
  Object.assign(formData, createUserCreationRequest(props.userType))
}

function fillForm(mentor) {
  Object.assign(formData, {
    ...createUserUpdateRequest(mentor.department?.id),
    fullName: mentor.fullName || '',
    email: mentor.email || '',
    dateOfBirth: mentor.dateOfBirth || '',
    departmentId: mentor.department?.id || '',
    isActive: mentor.active ?? true,
    password: ''
  })
}

watch(() => props.visible, (newVal) => {
  if (newVal) {
    if (props.mentor) {
      fillForm(props.mentor)
    } else {
      resetForm()
    }
  } else {
    // Reset form when dialog closes
    resetForm()
    formRef.value?.resetFields()
  }
})

watch(() => props.mentor, (newVal) => {
  if (newVal && props.visible) {
    fillForm(newVal)
  }
})

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
    roleName: props.userType,
    active: formData.isActive
  }

  if (formData.password) {
    payload.password = formData.password
  }

  loading.value = true
  emit('save', payload, () => {
    loading.value = false
  })
}

</script>

<template>
  <el-dialog 
    :model-value="visible"
    :title="isEdit ? t(`${translationPrefix}.form.editTitle`) : t(`${translationPrefix}.form.addTitle`)"
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
      <el-form-item :label="t(`${translationPrefix}.form.fullName`)" prop="fullName">
        <el-input 
          v-model="formData.fullName" 
          :placeholder="t(`${translationPrefix}.form.fullNamePlaceholder`)"
        />
      </el-form-item>

      <el-form-item :label="t(`${translationPrefix}.form.email`)" prop="email">
        <el-input 
          v-model="formData.email" 
          type="email"
          :placeholder="t(`${translationPrefix}.form.emailPlaceholder`)"
        />
      </el-form-item>

      <el-form-item 
        :label="t(`${translationPrefix}.form.password`) + (isEdit ? ' ' + t(`${translationPrefix}.form.passwordHint`) : '')" 
        prop="password"
      >
        <el-input 
          v-model="formData.password" 
          type="password"
          :placeholder="t(`${translationPrefix}.form.passwordPlaceholder`)"
          show-password
        />
      </el-form-item>

      <el-form-item :label="t(`${translationPrefix}.form.dateOfBirth`)" prop="dateOfBirth">
        <el-date-picker
          v-model="formData.dateOfBirth"
          type="date"
          :placeholder="t(`${translationPrefix}.form.dateOfBirth`)"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%"
          :disabled-date="disabledDate"
          :default-value="defaultDate"
        />
        <div class="form-help-text">{{ t(`${translationPrefix}.form.ageRestriction`) }}</div>
      </el-form-item>

      <el-form-item :label="t(`${translationPrefix}.form.department`)" prop="departmentId">
        <el-select 
          v-model="formData.departmentId" 
          :placeholder="t(`${translationPrefix}.form.selectDepartment`)"
          style="width: 100%"
        >
          <el-option
            v-for="dept in props.departments"
            :key="dept.id"
            :label="dept.title"
            :value="dept.id"
          />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">
        {{ t(`${translationPrefix}.form.cancel`) }}
      </el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">
        {{ isEdit ? t(`${translationPrefix}.form.save`) : t(`${translationPrefix}.form.create`) }}
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
