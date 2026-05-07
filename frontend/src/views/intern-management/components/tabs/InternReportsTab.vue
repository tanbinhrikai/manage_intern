<script setup>
import { getEvaluationCriteria } from "@/api/evaluation-criteria";
import {
  createWeeklyReport,
  getWeeklyReportsByInternId,
  updateWeeklyReport,
} from "@/api/weekly-report";
import {
  useApi,
  useDateFormat,
  useLoading,
  usePagination,
  useConfirm,
} from "@/composables";
import { useLocaleStore } from "@/locales/locale";
import {
  createWeeklyReportDetailRequest,
  createWeeklyReportRequest,
} from "@/types/weeklyReport";
import { InfoFilled, List } from "@element-plus/icons-vue";
import { computed, onMounted, reactive, ref } from "vue";

const props = defineProps({
  internId: {
    type: [String, Number],
    required: true,
  },
});

const localeStore = useLocaleStore();
const t = computed(() => localeStore.t);
const pageSizes = [20, 40, 60, 80, 100];
// Composables
const pagination = usePagination({
  initialPage: 1,
  initialPageSize: 20,
});

const {
  loading: reportLoading,
  withLoading: withReportLoading,
  setLoadingState,
  isLoading,
} = useLoading();
const { execute: executeApi } = useApi({
  showErrorMessage: true,
  showSuccessMessage: true,
});
const { formatDate } = useDateFormat();
const { confirmUpdate } = useConfirm();

const criteriaGroups = ref([]);
const weeklyReports = ref([]);
const selectedReportId = ref(null);
const showReportForm = ref(false);

const reportForm = reactive(createWeeklyReportRequest());

const criteriaByGroup = computed(() => {
  const grouped = {};
  criteriaGroups.value.forEach((group) => {
    const children = [];
    (group.mainCriteria || []).forEach((main) => {
      (main.children || []).forEach((child, index) => {
        children.push({
          ...child,
          parentId: main.id,
          parentName: main.name,
          // Flag để merge cell (span-method)
          isParentStart: index === 0,
          parentRowSpan: main.children.length,
        });
      });
    });
    if (children.length > 0) {
      grouped[group.id] = {
        name: group.name,
        criteria: children,
      };
    }
  });
  return grouped;
});

/**
 * @returns {Array<import('@/types/evaluationCriteria').EvaluationCriteria & { parentId: number, parentName: string }>}
 */
const allCriteria = computed(() => {
  const list = [];
  Object.values(criteriaByGroup.value).forEach((group) => {
    list.push(...group.criteria);
  });
  return list;
});

/**
 * @param {Object} params
 * @param {Object} params.row
 * @param {Object} params.column
 * @param {number} params.rowIndex
 * @param {number} params.columnIndex
 * @returns {Object|undefined}
 */
const objectSpanMethod = ({ row, column, rowIndex, columnIndex }) => {
  if (columnIndex === 0) {
    // First column is Parent Name - merge cells for same parent
    if (row.isParentStart) {
      return { rowspan: row.parentRowSpan, colspan: 1 };
    } else {
      return { rowspan: 0, colspan: 0 };
    }
  }
};

// --- Helper Functions ---
/**
 * @param {import('@/types/evaluationCriteria').EvaluationCriteria} criteria
 * @param {string} label
 * @returns {import('@/types/evaluationCriteria').ScoreDefinition|undefined}
 */
const getScoreDefinition = (criteria, label) => {
  return criteria.scoreDefinitions?.find((sd) => sd.scoreLabel === label);
};

/**
 * @returns {string}
 */
const getOverallAverage = computed(() => {
  let total = 0;
  let count = 0;
  reportForm.details.forEach((d) => {
    if (d.score !== null && d.score !== "") {
      total += Number(d.score);
      count++;
    }
  });
  return count > 0 ? (total / count).toFixed(1) : "-";
});

/**
 * @param {import('@/types/weeklyReport').WeeklyReport|null} report
 */
const initReportForm = (report = null) => {
  if (report) {
    // Edit mode: populate form with existing report data
    Object.assign(reportForm, {
      weekStartDate: report.weekStartDate || "",
      tasksAssigned: report.tasksAssigned || "",
      tasksCompleted: report.tasksCompleted || "",
      issuesRisks: report.issuesRisks || "",
      mentorOverallComment: report.mentorOverallComment || "",
      details: allCriteria.value.map((c) => {
        const existing = report.details?.find((d) => d.criteriaId === c.id);
        return {
          ...createWeeklyReportDetailRequest(),
          criteriaId: c.id,
          score: existing?.score ?? null,
          comment: existing?.comment || "",
        };
      }),
    });
    updateEndDate();
  } else {
    // Create mode: initialize with default values
    const today = new Date();
    const monday = new Date(today);
    monday.setDate(today.getDate() - today.getDay() + 1);
    Object.assign(reportForm, {
      ...createWeeklyReportRequest(),
      weekStartDate: monday.toISOString().split("T")[0],
      details: allCriteria.value.map((c) => ({
        ...createWeeklyReportDetailRequest(),
        criteriaId: c.id,
      })),
    });
    updateEndDate();
  }
};

function handleSizeChange(size) {
  pagination.setPageSize(size);
  pagination.firstPage();
  fetchWeeklyReports();
}

function updateEndDate() {
  if (reportForm.weekStartDate) {
    const d = new Date(reportForm.weekStartDate);
    d.setDate(d.getDate() + 5);
    reportForm.weekEndDate = d.toISOString().split("T")[0];
  }
}

async function fetchEvaluationCriteria() {
  try {
    const res = await executeApi(() => getEvaluationCriteria());
    criteriaGroups.value = res.data?.data || [];
  } catch (error) {
    // Error already handled by useApi
  }
}

async function fetchWeeklyReports() {
  if (!props.internId) return;
  await withReportLoading(async () => {
    const params = {
      ...pagination.apiParams.value,
      sort: "weekStartDate,desc",
    };

    const res = await executeApi(
      () => getWeeklyReportsByInternId(props.internId, params),
      null,
      true
    );

    weeklyReports.value = res.data?.data?.items || [];
    pagination.setTotalItems(res.data?.data?.totalItems || 0);
  });
}

/**
 * @param {number} page
 */
function handlePageChange(page) {
  pagination.setPage(page);
  fetchWeeklyReports();
}

function openNewReportForm() {
  selectedReportId.value = null;
  initReportForm();
  showReportForm.value = true;
}

/**
 * @param {import('@/types/weeklyReport').WeeklyReport} report
 */
function editReport(report) {
  selectedReportId.value = report.id;
  initReportForm(report);
  showReportForm.value = true;
}

function cancelReportForm() {
  showReportForm.value = false;
  selectedReportId.value = null;
}

async function saveReport() {
  setLoadingState("reportSaving", true);
  try {
    /** @type {WeeklyReportRequest} */
    const payload = {
      internId: Number(props.internId),
      weekStartDate: reportForm.weekStartDate,
      tasksAssigned: reportForm.tasksAssigned,
      tasksCompleted: reportForm.tasksCompleted,
      issuesRisks: reportForm.issuesRisks,
      mentorOverallComment: reportForm.mentorOverallComment,
      // Filter out details with no score (not evaluated)
      details: reportForm.details.filter(
        (d) => d.score !== null && d.score !== ""
      ),
    };

    if (selectedReportId.value) {
      await executeApi(
        () => updateWeeklyReport(selectedReportId.value, payload),
        "weeklyReport.messages.saveSuccess",
        "weeklyReport.messages.saveError"
      );
    } else {
      await executeApi(
        () => createWeeklyReport(payload),
        "weeklyReport.messages.saveSuccess",
        "weeklyReport.messages.saveError"
      );
    }

    showReportForm.value = false;
    await fetchWeeklyReports();
  } catch (error) {
    // Error already handled by useApi
  } finally {
    setLoadingState("reportSaving", false);
  }
}

function saveReportWithConfirm() {
  const message = selectedReportId.value
    ? (t.value("weeklyReport.confirm.update") || "Are you sure you want to update this weekly report?")
    : (t.value("weeklyReport.confirm.create") || "Are you sure you want to create this weekly report?");
  
  confirmUpdate({
    message,
    onConfirm: saveReport
  });
}

/**
 * @param {number} id
 * @returns {import('@/types/weeklyReport').WeeklyReportDetailRequest}
 */
const getDetail = (id) =>
  reportForm.details.find((d) => d.criteriaId === id) || {
    criteriaId: id,
    score: null,
    comment: "",
  };

/**
 * @param {number} id
 * @param {string} field
 * @param {any} val
 */
const updateDetail = (id, field, val) => {
  const d = reportForm.details.find((item) => item.criteriaId === id);
  if (d) d[field] = val;
};

/**
 * @param {number} s
 * @returns {string}
 */
const getScoreColor = (s) =>
  s >= 9 ? "#67c23a" : s >= 7 ? "#409eff" : s >= 5 ? "#e6a23c" : "#f56c6c";

onMounted(async () => {
  await fetchEvaluationCriteria();
  await fetchWeeklyReports();
});
</script>
  
  <template>
  <div class="report-container" v-loading="reportLoading">
    <template v-if="!showReportForm">
      <div class="header-actions">
        <h2 class="page-title">{{ t("weeklyReport.title") }}</h2>
        <el-button type="primary" :icon="List" @click="openNewReportForm">{{
          t("weeklyReport.createNew")
        }}</el-button>
      </div>

      <el-table
        :data="weeklyReports"
        stripe
        style="width: 100%"
        height="calc(100vh - 280px)"
        class="shadow-table"
      >
        <el-table-column
          prop="weekNumber"
          :label="t('weeklyReport.weekNumber')"
          width="120"
        />
        <el-table-column
          prop="weekStartDate"
          :label="t('weeklyReport.weekStartDate')"
        >
          <template #default="{ row }">{{
            formatDate(row.weekStartDate)
          }}</template>
        </el-table-column>
        <el-table-column
          prop="averageScore"
          :label="t('weeklyReport.table.averageScore')"
          width="150"
        >
          <template #default="{ row }">
            <span
              :style="{
                fontWeight: 'bold',
                color: getScoreColor(row.averageScore),
              }"
              >{{ row.averageScore?.toFixed(1) || "-" }}</span
            >
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="120" align="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="editReport(row)"
              >Edit</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper" v-if="pagination.totalItems.value > 0">
        <el-pagination
          v-model:current-page="pagination.currentPage.value"
          v-model:page-size="pagination.pageSize.value"
          :page-sizes="pageSizes"
          layout="total, sizes"
          :total="pagination.totalItems.value"
          @size-change="handleSizeChange"
        />

        <el-pagination
          v-model:current-page="pagination.currentPage.value"
          :page-size="pagination.pageSize.value"
          layout="prev, pager, next"
          :total="pagination.totalItems.value"
          @current-change="handlePageChange"
        />
      </div>
    </template>

    <template v-else>
      <div class="form-wrapper">
        <div class="sticky-header">
          <div class="left">
            <el-button link @click="cancelReportForm">← Back</el-button>
            <h3>{{ selectedReportId ? "Edit Report" : "New Report" }}</h3>
          </div>
          <div class="right">
            <span class="avg-score"
              >Avg:
              <b :style="{ color: getScoreColor(getOverallAverage) }">{{
                getOverallAverage
              }}</b></span
            >
            <el-button @click="cancelReportForm">Cancel</el-button>
            <el-button
              type="primary"
              @click="saveReportWithConfirm"
              :loading="isLoading('reportSaving')"
              >Save Report</el-button
            >
          </div>
        </div>

        <div class="form-content">
          <div class="section-box">
            <div class="date-row">
              <div class="field">
                <label>Start Date</label>
                <el-date-picker
                  v-model="reportForm.weekStartDate"
                  type="date"
                  @change="updateEndDate"
                  value-format="YYYY-MM-DD"
                />
              </div>
              <div class="field">
                <label>End Date</label>
                <el-date-picker
                  v-model="reportForm.weekEndDate"
                  type="date"
                  disabled
                  value-format="YYYY-MM-DD"
                />
              </div>
            </div>
            <div class="tasks-grid">
              <div class="task-col">
                <label>Tasks Assigned</label>
                <el-input
                  v-model="reportForm.tasksAssigned"
                  type="textarea"
                  :rows="3"
                  class="scroll-textarea"
                  placeholder="Input tasks..."
                />
              </div>
              <div class="task-col">
                <label>Tasks Completed</label>
                <el-input
                  v-model="reportForm.tasksCompleted"
                  type="textarea"
                  :rows="3"
                  resize="none"
                  class="scroll-textarea"
                  placeholder="Input results..."
                />
              </div>
              <div class="task-col">
                <label>Issues / Risks</label>
                <el-input
                  v-model="reportForm.issuesRisks"
                  type="textarea"
                  :rows="3"
                  resize="none"
                  class="scroll-textarea"
                  placeholder="Input blockers..."
                />
              </div>
            </div>
          </div>

          <div
            v-for="(group, groupId) in criteriaByGroup"
            :key="groupId"
            class="group-section"
          >
            <h4 class="group-title">{{ group.name }}</h4>
            <el-table
              :data="group.criteria"
              :span-method="objectSpanMethod"
              border
              class="evaluation-table"
              :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
            >
              <el-table-column label="Criteria Group" width="180">
                <template #default="{ row }">
                  <span class="parent-text">{{ row.parentName }}</span>
                </template>
              </el-table-column>

              <el-table-column label="Evaluation Criteria" min-width="200">
                <template #default="{ row }">
                  <div class="criteria-cell">
                    <span class="criteria-name">{{ row.name }}</span>

                    <el-popover
                      placement="top-start"
                      :width="350"
                      trigger="hover"
                    >
                      <template #reference>
                        <el-icon class="info-icon"><InfoFilled /></el-icon>
                      </template>
                      <div class="rubric-popup">
                        <div class="rubric-row good">
                          <span class="badge">9-10</span>
                          {{
                            getScoreDefinition(row, "EXCELLENT")?.description
                          }}
                        </div>
                        <div class="rubric-row">
                          <span class="badge">7-8</span>
                          {{ getScoreDefinition(row, "GOOD")?.description }}
                        </div>
                        <div class="rubric-row">
                          <span class="badge">5-6</span>
                          {{ getScoreDefinition(row, "AVERAGE")?.description }}
                        </div>
                        <div class="rubric-row bad">
                          <span class="badge">0-4</span>
                          {{ getScoreDefinition(row, "WEAK")?.description }}
                        </div>
                      </div>
                    </el-popover>
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="Score" width="100" align="center">
                <template #default="{ row }">
                  <el-input-number
                    :model-value="getDetail(row.id).score"
                    @update:model-value="
                      (v) => updateDetail(row.id, 'score', v)
                    "
                    :min="0"
                    :max="10"
                    :controls="false"
                    class="compact-input"
                    placeholder="0-10"
                  />
                </template>
              </el-table-column>

              <el-table-column label="Comment" min-width="250">
                <template #default="{ row }">
                  <el-input
                    :model-value="getDetail(row.id).comment"
                    @update:model-value="
                      (v) => updateDetail(row.id, 'comment', v)
                    "
                    type="textarea"
                    :rows="4"
                    class="comment-textarea"
                    placeholder="Add comment..."
                  />
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="section-box">
            <label class="final-label">Mentor Overall Comment</label>
            <el-input
              v-model="reportForm.mentorOverallComment"
              type="textarea"
              :rows="4"
              resize="none"
              class="scroll-textarea"
            />
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
  
  <style scoped>
.report-container {
  width: 100%;
  max-width: none;
  margin: 0;
  padding: 0 0 50px;
}
.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.shadow-table {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  overflow: hidden;
}
.pagination-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
  gap: 16px;
  flex-wrap: wrap;
}

.sticky-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(5px);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 20px;
}
.left {
  display: flex;
  align-items: center;
  gap: 15px;
}
.right {
  display: flex;
  align-items: center;
  gap: 15px;
}
.avg-score {
  font-size: 16px;
  margin-right: 10px;
}

.section-box {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  margin-bottom: 20px;
}
.date-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}
.field {
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.field label,
.task-col label,
.final-label {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 5px;
}

.tasks-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 20px;
}

.scroll-textarea :deep(.el-textarea__inner),
.comment-textarea :deep(.el-textarea__inner) {
  resize: none;
  overflow-y: auto;
  line-height: 1.4;
  padding: 8px;
}

.group-section {
  margin-bottom: 30px;
}
.group-title {
  margin: 0 0 10px 0;
  font-size: 15px;
  color: #409eff;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.evaluation-table {
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.parent-text {
  font-weight: 600;
  color: #606266;
  font-size: 13px;
}
.criteria-cell {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.criteria-name {
  font-weight: 500;
  font-size: 14px;
}
.info-icon {
  color: #c0c4cc;
  cursor: help;
  margin-left: 8px;
}
.info-icon:hover {
  color: #409eff;
}

.compact-input {
  width: 100%;
  max-width: 80px;
}
.compact-input :deep(.el-input__inner) {
  text-align: center;
  font-weight: 600;
  padding-left: 5px;
  padding-right: 5px;
}

.rubric-popup {
  font-size: 12px;
  line-height: 1.4;
}
.rubric-row {
  display: flex;
  gap: 8px;
  margin-bottom: 6px;
  align-items: baseline;
}
.badge {
  background: #f4f4f5;
  padding: 1px 6px;
  border-radius: 4px;
  font-weight: 700;
  min-width: 30px;
  text-align: center;
  font-size: 11px;
}
.rubric-row.good .badge {
  background: #e1f3d8;
  color: #67c23a;
}
.rubric-row.bad .badge {
  background: #fde2e2;
  color: #f56c6c;
}
</style>