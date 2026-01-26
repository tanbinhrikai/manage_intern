<script setup>
import { ref, computed, watch, reactive, onMounted } from "vue";
import { useLocaleStore } from "@/locales/locale";
import { createInternCreationRequest, createInternUpdateRequest } from "@/types/intern";
import { useStatus } from "@/composables";

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  intern: {
    type: Object,
    default: null,
  },
  hideMentor: {
    type: Boolean,
    default: false,
  },
  positions: {
    type: Array,
    default: () => [],
  },
  mentors: {
    type: Array,
    default: () => [],
  },
  internshipBatches: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(["update:visible", "save"]);

const localeStore = useLocaleStore();
const t = computed(() => localeStore.t);

const formRef = ref(null);
const loading = ref(false);
const mentors = ref([]);

const isEdit = computed(() => !!props.intern);

const { statusOptions } = useStatus();

const formData = reactive({
  ...createInternCreationRequest(),
  internStatus: "ACTIVE"
});

const formRules = computed(() => ({
  fullName: [
    {
      required: true,
      message: t.value("internManagement.messages.validationError"),
      trigger: "blur",
    },
  ],
  positionId: [
    {
      required: true,
      message: t.value("internManagement.messages.validationError"),
      trigger: "change",
    },
  ],
  internshipBatchId: [
    {
      required: true,
      message: t.value("internManagement.messages.validationError"),
      trigger: "change",
    },
  ],
  mentorId: props.hideMentor
    ? []
    : [
        {
          required: true,
          message: t.value("internManagement.messages.validationError"),
          trigger: "change",
        },
      ],
  startDate: [
    {
      required: true,
      message: t.value("internManagement.messages.validationError"),
      trigger: "change",
    },
  ],
  endDate: [
    {
      required: true,
      message: t.value("internManagement.messages.validationError"),
      trigger: "change",
    },
  ],
  internStatus: isEdit.value
    ? [
        {
          required: true,
          message: t.value("internManagement.messages.validationError"),
          trigger: "change",
        },
      ]
    : [],
}));

function resetForm() {
  Object.assign(formData, createInternCreationRequest());
}

function fillForm(intern) {
  Object.assign(formData, {
    ...createInternUpdateRequest(),
    fullName: intern.fullName || "",
    positionId: intern.position?.id || "",
    internshipBatchId: intern.internshipBatch?.id || "",
    mentorId: intern.mentor?.id || "",
    startDate: intern.startDate || "",
    endDate: intern.endDate || "",
    internStatus: intern.internStatus || "ACTIVE",
  });
}

watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      if (props.intern) {
        fillForm(props.intern);
      } else {
        resetForm();
      }
    } else {
      // Reset form when dialog closes
      resetForm();
      formRef.value?.resetFields();
    }
  }
);

watch(
  () => props.intern,
  (newVal) => {
    if (newVal && props.visible) {
      fillForm(newVal);
    }
  }
);

function handleClose() {
  emit("update:visible", false);
  formRef.value?.resetFields();
}

async function handleSubmit() {
  if (!formRef.value) return;
  console.log(formData);
  try {
    await formRef.value.validate();
  } catch {
    return;
  }

  const payload = {
    fullName: formData.fullName,
    positionId: formData.positionId,
    internshipBatchId: formData.internshipBatchId,
    mentorId: formData.mentorId,
    startDate: formData.startDate,
    endDate: formData.endDate,
  };

  if (isEdit.value) {
    payload.internStatus = formData.internStatus;
  }

  loading.value = true;
  emit("save", payload, () => {
    loading.value = false;
  });
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    :title="
      isEdit
        ? t('internManagement.form.editTitle')
        : t('internManagement.form.addTitle')
    "
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
      <el-form-item
        :label="t('internManagement.form.fullName')"
        prop="fullName"
      >
        <el-input
          v-model="formData.fullName"
          :placeholder="t('internManagement.form.fullNamePlaceholder')"
        />
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item
            :label="t('internManagement.form.position')"
            prop="positionId"
          >
            <el-select
              v-model="formData.positionId"
              :placeholder="t('internManagement.form.selectPosition')"
              style="width: 100%"
            >
              <el-option
                v-for="pos in props.positions"
                :key="pos.id"
                :label="pos.title"
                :value="pos.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item
            :label="t('internManagement.form.internshipBatch')"
            prop="positionId"
          >
            <el-select
              v-model="formData.internshipBatchId"
              :placeholder="t('internManagement.form.selectInternshipBatch')"
              style="width: 100%"
            >
              <el-option
                v-for="internshipBatch in props.internshipBatches"
                :key="internshipBatch.id"
                :label="internshipBatch.name"
                :value="internshipBatch.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12" v-if="!hideMentor">
          <el-form-item
            :label="t('internManagement.form.mentor')"
            prop="mentorId"
          >
            <el-select
              v-model="formData.mentorId"
              :placeholder="t('internManagement.form.selectMentor')"
              style="width: 100%"
            >
              <el-option
                v-for="mentor in props.mentors"
                :key="mentor.id"
                :label="mentor.fullName"
                :value="mentor.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item
            :label="t('internManagement.form.startDate')"
            prop="startDate"
          >
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
          <el-form-item
            :label="t('internManagement.form.endDate')"
            prop="endDate"
          >
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

      <el-form-item
        v-if="isEdit"
        :label="t('internManagement.form.status')"
        prop="internStatus"
      >
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
        {{ t("internManagement.form.cancel") }}
      </el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">
        {{
          isEdit
            ? t("internManagement.form.save")
            : t("internManagement.form.create")
        }}
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
