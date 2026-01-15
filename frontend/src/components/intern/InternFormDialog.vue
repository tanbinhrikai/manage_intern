<script setup>
import { ref, computed, watch, reactive, onMounted } from 'vue'
import { useLocaleStore } from '@/locales/locale'
import { getPositions } from '@/api/position'
import { getMentors } from '@/api/user'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  intern: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:visible', 'save'])

const localeStore = useLocaleStore()
const t = computed(() => localeStore.t)

const formRef = ref(null)
const loading = ref(false)
const positions = ref([])
const mentors = ref([])

const isEdit = computed(() => !!props.intern)

const statusOptions = ['ACTIVE', 'WARNING', 'COMPLETE', 'DROPPED']

const formData = reactive({
  fullName: '',
  positionId: '',
  mentorId: '',
  startDate: '',
  endDate: '',
  internStatus: 'ACTIVE'
})

const formRules = computed(() => ({
  fullName: [{ required: true, message: t.value('internManagement.messages.validationError'), trigger: 'blur' }],
  positionId: [{ required: true, message: t.value('internManagement.messages.validationError'), trigger: 'change' }],
  mentorId: [{ required: true, message: t.value('internManagement.messages.validationError'), trigger: 'change' }],
  startDate: [{ required: true, message: t.value('internManagement.messages.validationError'), trigger: 'change' }],
  endDate: [{ required: true, message: t.value('internManagement.messages.validationError'), trigger: 'change' }],
  internStatus: isEdit.value ? [{ required: true, message: t.value('internManagement.messages.validationError'), trigger: 'change' }] : []
}))

function resetForm() {
  formData.fullName = ''
  formData.positionId = ''
  formData.mentorId = ''
  formData.startDate = ''
  formData.endDate = ''
  formData.internStatus = 'ACTIVE'
}

function fillForm(intern) {
  formData.fullName = intern.fullName || ''
  formData.positionId = intern.position?.id || ''
  formData.mentorId = intern.mentor?.id || ''
  formData.startDate = intern.startDate || ''
  formData.endDate = intern.endDate || ''
  formData.internStatus = intern.internStatus || 'ACTIVE'
}

watch(() => props.visible, (newVal) => {
  if (newVal) {
    if (props.intern) {
      fillForm(props.intern)
    } else {
      resetForm()
    }
  }
})

watch(() => props.intern, (newVal) => {
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
    positionId: formData.positionId,
    mentorId: formData.mentorId,
    startDate: formData.startDate,
    endDate: formData.endDate
  }

  if (isEdit.value) {
    payload.internStatus = formData.internStatus
  }

  loading.value = true
  emit('save', payload, () => {
    loading.value = false
  })
}

async function fetchPositions() {
  try {
    const res = await getPositions()
    positions.value = res.data?.data || []
  } catch (error) {
    console.error("Failed to load positions:", error)
  }
}

async function fetchMentors() {
  try {
    const res = await getMentors()
    const users = res.data?.data?.items || []
    mentors.value = users.filter(u => u.role?.roleName === 'MENTOR')
    console.log('Mentors loaded:', mentors.value)
  } catch (error) {
    console.error("Failed to load mentors:", error)
  }
}

onMounted(() => {
  fetchPositions()
  fetchMentors()
})
</script>

<template>
  <el-dialog 
    :model-value="visible"
    :title="isEdit ? t('internManagement.form.editTitle') : t('internManagement.form.addTitle')"
    width="550px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form 
      ref="formRef"
      :model="formData" 
      :rules="formRules"
      label-position="top"
    >
      <el-form-item :label="t('internManagement.form.fullName')" prop="fullName">
        <el-input 
          v-model="formData.fullName" 
          :placeholder="t('internManagement.form.fullNamePlaceholder')"
        />
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item :label="t('internManagement.form.position')" prop="positionId">
            <el-select 
              v-model="formData.positionId" 
              :placeholder="t('internManagement.form.selectPosition')"
              style="width: 100%"
            >
              <el-option
                v-for="pos in positions"
                :key="pos.id"
                :label="pos.title"
                :value="pos.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="t('internManagement.form.mentor')" prop="mentorId">
            <el-select 
              v-model="formData.mentorId" 
              :placeholder="t('internManagement.form.selectMentor')"
              style="width: 100%"
            >
              <el-option
                v-for="mentor in mentors"
                :key="mentor.id || mentor.email"
                :label="mentor.fullName"
                :value="mentor.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item :label="t('internManagement.form.startDate')" prop="startDate">
            <el-date-picker
              v-model="formData.startDate"
              type="date"
              :placeholder="t('internManagement.form.startDate')"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="t('internManagement.form.endDate')" prop="endDate">
            <el-date-picker
              v-model="formData.endDate"
              type="date"
              :placeholder="t('internManagement.form.endDate')"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item v-if="isEdit" :label="t('internManagement.form.status')" prop="internStatus">
        <el-select 
          v-model="formData.internStatus" 
          :placeholder="t('internManagement.form.selectStatus')"
          style="width: 100%"
        >
          <el-option
            v-for="status in statusOptions"
            :key="status"
            :label="t('internManagement.status.' + status)"
            :value="status"
          />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">
        {{ t('internManagement.form.cancel') }}
      </el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">
        {{ isEdit ? t('internManagement.form.save') : t('internManagement.form.create') }}
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
