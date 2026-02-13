<template>
  <div class="container-fluid py-3">
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-calendar-check fs-4"></i>
        <h5 class="mb-0">Xếp Lịch Nhân Viên</h5>
      </div>

      <div class="d-flex align-items-center gap-2">
        <!-- Toggle view -->
        <div class="btn-group btn-group-sm" role="group" aria-label="Chế độ hiển thị">
          <button
              type="button"
              class="btn"
              :class="viewMode === 'calendar' ? 'btn-primary text-white' : 'btn-outline-primary'"
              @click="setViewMode('calendar')"
          >
            <i class="bi bi-grid-3x3-gap me-1"></i> Lịch biểu
          </button>
          <button
              type="button"
              class="btn"
              :class="viewMode === 'list' ? 'btn-primary text-white' : 'btn-outline-primary'"
              @click="setViewMode('list')"
          >
            <i class="bi bi-list-ul me-1"></i> Danh sách
          </button>
        </div>

        <!-- Export chỉ ở dạng Danh sách -->
        <button
            v-if="viewMode === 'list' && !exportMode"
            class="btn btn-outline-primary btn-sm"
            type="button"
            @click="openExportMode"
        >
          <i class="bi bi-file-earmark-excel me-1"></i> Xuất Excel
        </button>

        <template v-else-if="viewMode === 'list' && exportMode">
          <button
              class="btn btn-success btn-sm"
              type="button"
              :disabled="selectedIds.length === 0 || exporting"
              @click="exportSelectedToExcel"
          >
            <i class="bi bi-download me-1"></i>
            {{ exporting ? "Đang xuất..." : `Tải xuống (${selectedIds.length})` }}
          </button>
          <button class="btn btn-outline-secondary btn-sm" @click="cancelExportMode">
            <i class="bi bi-x-lg me-1"></i> Hủy
          </button>
        </template>

        <button class="btn btn-outline-secondary btn-sm" @click="openModal()">
          <i class="bi bi-plus-lg me-1"></i> Phân Ca Mới
        </button>
      </div>
    </div>

    <!-- ✅ BỘ LỌC: hiển thị cho CẢ Danh sách + Lịch biểu -->
    <div class="card shadow-sm mb-3 filter-card">
      <div
          class="filter-header d-flex align-items-center justify-content-between"
          data-bs-toggle="collapse"
          data-bs-target="#filterBody"
          role="button"
          aria-expanded="true"
          aria-controls="filterBody"
      >
        <div class="d-flex align-items-center gap-2">
          <span class="filter-icon">▼</span>
          <span class="filter-title">Bộ lọc tìm kiếm</span>
        </div>
        <small class="filter-hint">Nhấn để thu gọn/mở rộng</small>
      </div>

      <div id="filterBody" class="collapse show">
        <div class="card-body filter-body">
          <div class="row g-3">
            <div class="col-12 col-lg-3">
              <label class="form-label">Tìm nhân viên</label>
              <input
                  v-model.trim="filters.keyword"
                  type="text"
                  class="form-control"
                  placeholder="Tên hoặc Mã NV..."
                  @input="applyFilters"
              />
            </div>

            <div class="col-12 col-lg-3">
              <label class="form-label">Ca làm việc</label>
              <select v-model="filters.shiftId" class="form-select" @change="applyFilters">
                <option :value="null">Tất cả ca</option>
                <!-- vẫn cho chọn tất cả template; calendar sẽ chỉ HIỂN THỊ ca hoạt động -->
                <option v-for="t in templates" :key="t.id" :value="t.id">
                  {{ t.tenCa }}
                </option>
              </select>
            </div>

            <!-- ✅ Chỉ dùng Từ/Đến ngày cho Danh sách (vì calendar theo tuần riêng) -->
            <div class="col-12 col-lg-3" v-if="viewMode === 'list'">
              <label class="form-label">Từ ngày</label>
              <div class="input-group">
                <input ref="fromPickerRef" type="text" class="form-control" placeholder="dd/mm/yyyy" />
                <button class="btn btn-outline-secondary" type="button" @click="openFromPicker" title="Chọn ngày">
                  <i class="bi bi-calendar3"></i>
                </button>
                <button class="btn btn-outline-secondary" type="button" @click="clearFromDate" title="Xóa">
                  <i class="bi bi-x-lg"></i>
                </button>
              </div>
            </div>

            <div class="col-12 col-lg-3" v-if="viewMode === 'list'">
              <label class="form-label">Đến ngày</label>
              <div class="input-group">
                <input ref="toPickerRef" type="text" class="form-control" placeholder="dd/mm/yyyy" />
                <button class="btn btn-outline-secondary" type="button" @click="openToPicker" title="Chọn ngày">
                  <i class="bi bi-calendar3"></i>
                </button>
                <button class="btn btn-outline-secondary" type="button" @click="clearToDate" title="Xóa">
                  <i class="bi bi-x-lg"></i>
                </button>
              </div>
            </div>

            <div class="col-12 d-flex justify-content-end">
              <button class="btn btn-light btn-sm" @click="resetFilters">
                <i class="bi bi-arrow-counterclockwise me-1"></i> Đặt lại
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Toolbar tuần (chỉ ở dạng Lịch biểu) -->
    <div v-if="viewMode === 'calendar'" class="card shadow-sm mb-3 calendar-toolbar">
      <div class="card-body py-2">
        <div class="d-flex align-items-center justify-content-between flex-wrap gap-2">
          <div class="d-flex align-items-center gap-2">
            <button class="btn btn-outline-secondary btn-sm" type="button" @click="prevWeek" title="Tuần trước">
              <i class="bi bi-chevron-left"></i>
            </button>

            <input
                type="date"
                v-model="calendarAnchor"
                class="form-control form-control-sm"
                style="width: 160px"
                @change="onCalendarAnchorChange"
            />

            <button class="btn btn-outline-secondary btn-sm" type="button" @click="nextWeek" title="Tuần sau">
              <i class="bi bi-chevron-right"></i>
            </button>

            <button class="btn btn-outline-secondary btn-sm" type="button" @click="goToday">
              Hôm nay
            </button>
          </div>

          <div class="badge bg-light text-dark border">
            Tuần từ: <span class="fw-semibold">{{ calendarWeekStart }}</span> đến
            <span class="fw-semibold">{{ calendarWeekEnd }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- DANH SÁCH -->
    <div v-if="viewMode === 'list'" class="card shadow-sm">
      <div class="card-body">
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border text-primary" role="status"></div>
          <div class="mt-2 text-muted">Đang tải dữ liệu...</div>
        </div>

        <div v-else class="table-wrap">
          <table class="custom-table">
            <colgroup>
              <col v-if="exportMode" style="width: 40px" />
              <col style="width: 50px" />
              <col style="width: 100px" />
              <col style="width: 80px" />
              <col style="width: 120px" />
              <col style="width: 200px" />
              <col style="width: 120px" />
              <col style="width: 130px" />
              <col style="width: 150px" />
              <col style="width: 100px" />
            </colgroup>
            <thead>
            <tr>
              <th v-if="exportMode" class="text-center">
                <input
                    type="checkbox"
                    :checked="allVisibleSelected"
                    @change="toggleSelectAllVisible($event.target.checked)"
                />
              </th>
              <th class="text-center">STT</th>
              <th>Ngày làm</th>
              <th>Thứ</th>
              <th>Mã NV</th>
              <th>Tên nhân viên</th>
              <th>Tên Ca</th>
              <th>Khung giờ</th>
              <th>Ghi chú</th>
              <th class="text-center pe-3">Hành động</th>
            </tr>
            </thead>

            <tbody>
            <tr v-if="pagedItems.length === 0">
              <td :colspan="exportMode ? 10 : 9" class="empty">Không có dữ liệu</td>
            </tr>

            <tr v-for="(item, index) in pagedItems" :key="item.id">
              <td v-if="exportMode" class="text-center">
                <input
                    type="checkbox"
                    :checked="isSelected(item.id)"
                    @change="toggleSelect(item, $event.target.checked)"
                />
              </td>

              <td class="text-center">{{ (page.page - 1) * page.size + index + 1 }}</td>

              <td>{{ formatDate(item.ngayLamViec) }}</td>
              <td>{{ getDayOfWeek(item.ngayLamViec) }}</td>

              <td><span class="badge badge-muted text-dark border">{{ item.maNhanVien }}</span></td>
              <td>{{ item.tenNhanVien }}</td>

              <td><span class="badge bg-primary border">{{ item.tenCa }}</span></td>

              <td>{{ formatTime(item.gioBatDau) }} - {{ formatTime(item.gioKetThuc) }}</td>

              <td><span class="ellipsis" :title="item.ghiChu">{{ item.ghiChu || '-' }}</span></td>

              <td class="text-end pe-3">
                <div class="d-flex justify-content-center gap-2">
                  <button class="btn btn-outline-warning btn-sm" @click="openModal(item)" title="Sửa">
                    <i class="bi bi-pencil-square"></i>
                  </button>
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        <div class="d-flex align-items-center mt-3 flex-column flex-md-row gap-2" v-if="totalElements > 0">
          <div class="text-muted flex-grow-1">
            Hiển thị {{ pagedItems.length }} / tổng {{ totalElements }} bản ghi
          </div>

          <div class="d-flex align-items-center gap-2 justify-content-center flex-grow-1">
            <button class="btn btn-outline-secondary btn-sm" :disabled="page.page === 1" @click="setPage(page.page - 1)">
              <i class="bi bi-chevron-left"></i>
            </button>

            <div class="input-group input-group-sm" style="width: 110px">
              <span class="input-group-text">Trang</span>
              <input
                  type="number"
                  min="1"
                  :max="totalPages"
                  class="form-control"
                  v-model.number="pageInput"
                  @keyup.enter="jumpPage"
              />
            </div>

            <button class="btn btn-outline-secondary btn-sm" :disabled="page.page >= totalPages" @click="setPage(page.page + 1)">
              <i class="bi bi-chevron-right"></i>
            </button>
          </div>

          <div class="d-flex justify-content-md-end flex-grow-1">
            <select class="form-select form-select-sm" style="width: 180px" v-model.number="page.size">
              <option :value="10">10 bản ghi / trang</option>
              <option :value="20">20 bản ghi / trang</option>
              <option :value="50">50 bản ghi / trang</option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <!-- LỊCH BIỂU -->
    <div v-else class="card shadow-sm">
      <div class="card-body">
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border text-primary" role="status"></div>
          <div class="mt-2 text-muted">Đang tải dữ liệu...</div>
        </div>

        <div v-else class="calendar-wrap">
          <table class="calendar-table">
            <thead>
            <tr>
              <th class="shift-col">CA / NGÀY</th>
              <th
                  v-for="d in calendarDays"
                  :key="d.date"
                  class="day-head"
                  :class="{ today: d.isToday }"
              >
                <div class="fw-bold">{{ d.label }}</div>
                <div class="small opacity-75">{{ formatDate(d.date) }}</div>
              </th>
            </tr>
            </thead>

            <tbody>
            <tr v-if="sortedTemplates.length === 0">
              <td :colspan="8" class="empty">Chưa có ca làm việc (hoặc tất cả ca đang không hoạt động)</td>
            </tr>

            <tr v-for="ca in sortedTemplates" :key="ca.id">
              <td class="shift-col shift-info">
                <div class="fw-bold">{{ ca.tenCa }}</div>
                <div class="small text-muted">
                  {{ formatTime(ca.gioBatDau) }} - {{ formatTime(ca.gioKetThuc) }}
                </div>
              </td>

              <td
                  v-for="d in calendarDays"
                  :key="d.date"
                  class="calendar-cell"
                  :class="{ today: d.isToday }"
              >
                <!-- Ô trống: + ở giữa ô -->
                <button
                    v-if="canAddToCell(ca.id, d.date)"
                    class="btn btn-outline-primary btn-sm add-btn add-btn-center"
                    type="button"
                    title="Thêm nhân viên vào ca"
                    @click="openModalFromCalendar(ca.id, d.date)"
                >
                  <i class="bi bi-plus-lg"></i>
                </button>

                <!-- Có nhân viên -->
                <div
                    v-else
                    class="emp-badge"
                    title="Bấm để sửa"
                    @click="openModal(getPrimaryAssignment(ca.id, d.date))"
                >
                  <img
                      v-if="resolveAvatarUrl(getPrimaryAssignment(ca.id, d.date))"
                      :src="resolveAvatarUrl(getPrimaryAssignment(ca.id, d.date))"
                      class="emp-badge-avatar"
                      alt="avatar"
                      @error="onEmpAvatarError($event, getPrimaryAssignment(ca.id, d.date))"
                  />
                  <div v-else class="emp-badge-initials">
                    {{ getInitials(getPrimaryAssignment(ca.id, d.date)?.tenNhanVien) }}
                  </div>

                  <div class="emp-badge-name">
                    {{ getPrimaryAssignment(ca.id, d.date)?.tenNhanVien }}
                  </div>
                  <div class="emp-badge-code">
                    {{ getPrimaryAssignment(ca.id, d.date)?.maNhanVien || "" }}
                  </div>
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- CONFIRM -->
    <div v-if="showConfirm" class="modal-overlay confirm-overlay" @click.self="closeConfirm">
      <div class="modal-card">
        <h3 class="modal-title">Xác nhận</h3>
        <p class="modal-desc">{{ confirmText }}</p>
        <div class="modal-actions">
          <button class="btn btn-outline-secondary" :disabled="confirmLoading" @click="closeConfirm">Hủy</button>
          <button class="btn btn-danger" :disabled="confirmLoading" @click="confirmYes">
            {{ confirmLoading ? "Đang xử lý..." : "Đồng ý" }}
          </button>
        </div>
      </div>
    </div>

    <!-- MODAL -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-card shadow-lg" style="width: 500px;">
        <h3 class="modal-title border-bottom pb-2 mb-3">
          {{ isEdit ? 'Cập nhật Lịch Làm Việc' : 'Phân Ca Làm Việc Mới' }}
        </h3>

        <form @submit.prevent="submitAssign">
          <div class="mb-3">
            <label class="form-label fw-bold small">Nhân viên <span class="text-danger">*</span></label>
            <select v-model="form.idNhanVien" class="form-select" required :disabled="isEdit">
              <option :value="null" disabled>-- Chọn nhân viên --</option>
              <option v-for="nv in selectableStaffList" :key="nv.id" :value="nv.id">
                {{ nv.tenNhanVien }} ({{ nv.maNhanVien }})
              </option>
            </select>
            <div v-if="isEdit" class="form-text text-muted">Không thể thay đổi nhân viên khi chỉnh sửa.</div>
          </div>

          <div class="mb-3">
            <label class="form-label fw-bold small">Ca làm việc <span class="text-danger">*</span></label>
            <select
                v-model="form.idCaLamViec"
                class="form-select"
                required
                :disabled="lockShiftDate && !isEdit"
            >
              <option :value="null" disabled>-- Chọn ca mẫu --</option>
              <option v-for="ca in templates" :key="ca.id" :value="ca.id">
                {{ ca.tenCa }} ({{ formatTime(ca.gioBatDau) }} - {{ formatTime(ca.gioKetThuc) }})
              </option>
            </select>
          </div>

          <div class="mb-3">
            <label class="form-label fw-bold small">Ngày làm việc <span class="text-danger">*</span></label>
            <input
                v-model="form.ngayLamViec"
                type="date"
                class="form-control"
                required
                :disabled="lockShiftDate && !isEdit"
            />
          </div>

          <div class="mb-3">
            <label class="form-label fw-bold small">Ghi chú</label>
            <input v-model="form.ghiChu" type="text" class="form-control" placeholder="VD: Tăng ca, trực thay..." />
          </div>

          <div class="modal-actions mt-4">
            <button
                v-if="isEdit"
                type="button"
                class="btn btn-danger me-auto"
                @click="requestDeleteFromModal"
            >
              <i class="bi bi-trash me-1"></i> Xóa
            </button>

            <button type="button" class="btn btn-outline-secondary" @click="closeModal">Hủy</button>
            <button type="submit" class="btn btn-primary fw-bold px-4">
              {{ isEdit ? 'Cập nhật' : 'Lưu lịch' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from "vue";
import shiftApi from "@/services/shiftApi";
import * as nhanVienApi from "@/services/nhanVienApi";
import { useToast } from "@/composables/useToast";
import * as XLSX from "xlsx";
import flatpickr from "flatpickr";
import "flatpickr/dist/flatpickr.css";
import { Vietnamese } from "flatpickr/dist/l10n/vn.js";

const toast = useToast();

// ====================== VIEW MODE ======================
const viewMode = ref("list"); // 'list' | 'calendar'
function setViewMode(mode) {
  if (viewMode.value === mode) return;
  viewMode.value = mode;

  if (viewMode.value !== "list") {
    exportMode.value = false;
    selectedIds.value = [];
  }

  if (mode === "calendar") loadCalendar();
  else loadSchedule();
}

// ====================== STATE ======================
const loading = ref(false);
const rawSchedules = ref([]);
const staffList = ref([]);
const templates = ref([]);
const showModal = ref(false);
const isEdit = ref(false);
const editingId = ref(null);
const lockShiftDate = ref(false);

const selectableStaffList = computed(() => {
  return (staffList.value || []).filter((nv) => {
    const active = nv.trangThai === true || nv.trangThai === 1;
    const isAdmin =
        nv.quyenHanId === 1 || String(nv.tenQuyenHan || "").toLowerCase().includes("admin");
    return active && !isAdmin;
  });
});

// ====================== FILTERS (SHARED: list + calendar) ======================
const today = new Date();
const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
const lastDay = new Date(today.getFullYear(), today.getMonth() + 1, 0);
const toDateStr = (d) =>
    new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().split("T")[0];

const filters = reactive({
  keyword: "",
  shiftId: null,
  from: toDateStr(firstDay), // list only
  to: toDateStr(lastDay),    // list only
});

const fromPickerRef = ref(null);
const toPickerRef = ref(null);
let fromPickerInstance = null;
let toPickerInstance = null;

const isoToLocalDate = (iso) => {
  if (!iso) return null;
  const [y, m, d] = iso.split("-").map(Number);
  return new Date(y, m - 1, d);
};

function openFromPicker() { fromPickerInstance?.open(); }
function openToPicker() { toPickerInstance?.open(); }

function clearFromDate() {
  filters.from = "";
  fromPickerInstance?.clear();
  page.page = 1;
  loadSchedule();
}
function clearToDate() {
  filters.to = "";
  toPickerInstance?.clear();
  page.page = 1;
  loadSchedule();
}
function applyFilters() {
  page.page = 1;
  // Không cần gọi API lại vì lọc client-side.
  // Calendar cũng tự cập nhật vì dùng filteredSchedules + primaryMap.
}

// ====================== PAGINATION (LIST) ======================
const page = reactive({ page: 1, size: 10 });
const pageInput = ref(1);
function setPage(p) {
  if (p >= 1 && p <= totalPages.value) page.page = p;
}
function jumpPage() {
  const p = Number(pageInput.value || 1);
  setPage(Math.min(Math.max(p, 1), totalPages.value || 1));
}

// ====================== FORM (MODAL) ======================
const form = reactive({
  idNhanVien: null,
  idCaLamViec: null,
  ngayLamViec: toDateStr(new Date()),
  ghiChu: "",
});

// ====================== CONFIRM ======================
const showConfirm = ref(false);
const confirmText = ref("");
const confirmLoading = ref(false);
let pendingDeleteId = null;
const deleteFromModal = ref(false);

// ====================== EXPORT ======================
const exportMode = ref(false);
const exporting = ref(false);
const selectedIds = ref([]);

// ====================== CALENDAR (WEEK) ======================
const calendarAnchor = ref(toDateStr(new Date()));
const todayIso = toDateStr(new Date());

function startOfWeekISO(iso) {
  const d = isoToLocalDate(iso);
  if (!d) return toDateStr(new Date());
  const day = d.getDay(); // 0..6 (CN..T7)
  const diff = day === 0 ? 6 : day - 1; // Monday start
  d.setDate(d.getDate() - diff);
  return toDateStr(d);
}
function addDaysISO(iso, days) {
  const d = isoToLocalDate(iso);
  d.setDate(d.getDate() + days);
  return toDateStr(d);
}

const calendarWeekStart = computed(() => startOfWeekISO(calendarAnchor.value));
const calendarWeekEnd = computed(() => addDaysISO(calendarWeekStart.value, 6));

const calendarDays = computed(() => {
  const labels = ["CN", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7"];
  const start = calendarWeekStart.value;
  return Array.from({ length: 7 }, (_, i) => {
    const date = addDaysISO(start, i);
    const dayIdx = new Date(date).getDay();
    return {
      date,
      label: labels[dayIdx],
      isToday: date === todayIso,
    };
  });
});

function prevWeek() { calendarAnchor.value = addDaysISO(calendarWeekStart.value, -7); }
function nextWeek() { calendarAnchor.value = addDaysISO(calendarWeekStart.value, 7); }
function goToday() { calendarAnchor.value = todayIso; }
function onCalendarAnchorChange() {}

watch(calendarAnchor, () => {
  if (viewMode.value === "calendar") loadCalendar();
});

// ====================== LOAD DATA ======================
onMounted(() => {
  fromPickerInstance = flatpickr(fromPickerRef.value, {
    locale: Vietnamese,
    allowInput: true,
    dateFormat: "d/m/Y",
    defaultDate: isoToLocalDate(filters.from),
    onChange: (selectedDates) => {
      filters.from = selectedDates?.[0] ? toDateStr(selectedDates[0]) : "";
      page.page = 1;
      loadSchedule();
    },
  });

  toPickerInstance = flatpickr(toPickerRef.value, {
    locale: Vietnamese,
    allowInput: true,
    dateFormat: "d/m/Y",
    defaultDate: isoToLocalDate(filters.to),
    onChange: (selectedDates) => {
      filters.to = selectedDates?.[0] ? toDateStr(selectedDates[0]) : "";
      page.page = 1;
      loadSchedule();
    },
  });

  loadSchedule();
  loadResources();
});

async function loadSchedule() {
  loading.value = true;
  try {
    const res = await shiftApi.getSchedules(filters.from, filters.to);
    const data = res.data;
    if (Array.isArray(data)) rawSchedules.value = data;
    else if (data && Array.isArray(data.result)) rawSchedules.value = data.result;
    else if (data && Array.isArray(data.data)) rawSchedules.value = data.data;
    else rawSchedules.value = [];
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

async function loadCalendar() {
  loading.value = true;
  try {
    const res = await shiftApi.getSchedules(calendarWeekStart.value, calendarWeekEnd.value);
    const data = res.data;
    if (Array.isArray(data)) rawSchedules.value = data;
    else if (data && Array.isArray(data.result)) rawSchedules.value = data.result;
    else if (data && Array.isArray(data.data)) rawSchedules.value = data.data;
    else rawSchedules.value = [];
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

function reloadCurrentView() {
  if (viewMode.value === "calendar") return loadCalendar();
  return loadSchedule();
}

async function loadResources() {
  try {
    const resNV = await nhanVienApi.listNhanVien(0, 100);
    let rawNV = [];
    if (resNV.content && Array.isArray(resNV.content)) rawNV = resNV.content;
    else if (resNV.data && Array.isArray(resNV.data)) rawNV = resNV.data;
    else if (Array.isArray(resNV)) rawNV = resNV;

    // giữ active staff để phục vụ avatar map, v.v.
    staffList.value = rawNV.filter((nv) => nv.trangThai === 1 || nv.trangThai === true);

    const resCa = await shiftApi.getTemplates();
    const dataCa = resCa.data || resCa;
    if (Array.isArray(dataCa)) templates.value = dataCa;
    else if (dataCa.data) templates.value = dataCa.data;
    else templates.value = [];
  } catch (e) {
    console.error("Resource error", e);
  }
}

// ====================== ✅ FILTERED SCHEDULES (SHARED) ======================
// Bộ lọc keyword + ca áp dụng cho CẢ list + calendar
const filteredSchedules = computed(() => {
  let items = rawSchedules.value || [];

  // keyword
  if (filters.keyword) {
    const kw = filters.keyword.toLowerCase();
    items = items.filter(
        (i) =>
            (i.tenNhanVien || "").toLowerCase().includes(kw) ||
            (i.maNhanVien || "").toLowerCase().includes(kw)
    );
  }

  // shiftId
  if (filters.shiftId) items = items.filter((i) => i.idCaLamViec === filters.shiftId);

  return items;
});

// ====================== COMPUTED (LIST) ======================
const totalElements = computed(() => filteredSchedules.value.length);
const totalPages = computed(() => Math.ceil(totalElements.value / page.size) || 1);
const pagedItems = computed(() =>
    filteredSchedules.value.slice((page.page - 1) * page.size, page.page * page.size)
);

// ====================== ✅ COMPUTED (CALENDAR) ======================
// Chỉ hiển thị ca đang HOẠT ĐỘNG trong lịch biểu
function isTemplateActive(ca) {
  // hỗ trợ nhiều tên field khác nhau để khỏi lệ thuộc BE
  const v = ca?.trangThai ?? ca?.hoatDong ?? ca?.isActive ?? ca?.active ?? ca?.status;
  return v === true || v === 1 || v === "1" || String(v || "").toLowerCase() === "active";
}

const sortedTemplates = computed(() => {
  const list = Array.isArray(templates.value) ? [...templates.value] : [];

  // ✅ lọc ca hoạt động
  const activeOnly = list.filter(isTemplateActive);

  // sort theo giờ bắt đầu + tên
  return activeOnly.sort((a, b) => {
    const ta = (a.gioBatDau || "").toString();
    const tb = (b.gioBatDau || "").toString();
    if (ta !== tb) return ta.localeCompare(tb);
    return (a.tenCa || "").toString().localeCompare((b.tenCa || "").toString(), "vi");
  });
});

const staffById = computed(() => {
  const map = {};
  (staffList.value || []).forEach((s) => (map[s.id] = s));
  return map;
});

/**
 * primaryMap: mỗi ô (caId + ngày) chỉ lấy 1 bản ghi đầu tiên.
 * ✅ Dùng filteredSchedules để lịch biểu cũng bị ảnh hưởng bởi bộ lọc tìm kiếm.
 */
const primaryMap = computed(() => {
  const map = {};
  (filteredSchedules.value || []).forEach((item) => {
    const key = `${item.idCaLamViec}_${item.ngayLamViec}`;
    if (!map[key]) map[key] = item;
  });
  return map;
});

function getPrimaryAssignment(caId, dateIso) {
  return primaryMap.value[`${caId}_${dateIso}`] || null;
}

function canAddToCell(caId, dateIso) {
  return !getPrimaryAssignment(caId, dateIso);
}

function openModalFromCalendar(caId, dateIso) {
  isEdit.value = false;
  editingId.value = null;
  lockShiftDate.value = true;

  form.idNhanVien = null;
  form.idCaLamViec = caId;
  form.ngayLamViec = dateIso;
  form.ghiChu = "";

  showModal.value = true;
}

// ====================== ACTIONS ======================
function resetFilters() {
  filters.keyword = "";
  filters.shiftId = null;

  // list: reset date range về tháng hiện tại
  filters.from = toDateStr(firstDay);
  filters.to = toDateStr(lastDay);

  fromPickerInstance?.setDate(isoToLocalDate(filters.from), false);
  toPickerInstance?.setDate(isoToLocalDate(filters.to), false);

  page.page = 1;

  // chỉ reload API cho list (calendar là theo tuần riêng)
  if (viewMode.value === "list") loadSchedule();
}

function openModal(item = null) {
  lockShiftDate.value = false;
  isEdit.value = !!item;

  if (item) {
    editingId.value = item.id;
    form.idNhanVien = item.idNhanVien;
    form.idCaLamViec = item.idCaLamViec;
    form.ngayLamViec = item.ngayLamViec;
    form.ghiChu = item.ghiChu;
  } else {
    editingId.value = null;
    form.idNhanVien = null;
    form.idCaLamViec = null;
    form.ngayLamViec = toDateStr(new Date());
    form.ghiChu = "";
  }

  showModal.value = true;
}

function closeModal() {
  showModal.value = false;
  lockShiftDate.value = false;
}

async function submitAssign() {
  try {
    if (isEdit.value) {
      await shiftApi.updateSchedule(editingId.value, form);
      toast.success("Cập nhật lịch thành công!");
    } else {
      await shiftApi.assignSchedule(form);
      toast.success("Xếp lịch thành công!");
    }
    closeModal();
    await reloadCurrentView();
  } catch (e) {
    toast.error(e.response?.data?.message || "Lỗi xếp lịch");
  }
}

function requestDeleteFromModal() {
  if (!editingId.value) return;
  deleteFromModal.value = true;
  confirmDelete(editingId.value);
}

function confirmDelete(id) {
  confirmText.value = "Bạn có chắc chắn muốn xóa lịch này?";
  pendingDeleteId = id;
  showConfirm.value = true;
}

function closeConfirm() {
  showConfirm.value = false;
  pendingDeleteId = null;
  deleteFromModal.value = false;
}

async function confirmYes() {
  if (!pendingDeleteId) return;
  confirmLoading.value = true;
  try {
    await shiftApi.deleteSchedule(pendingDeleteId);
    toast.success("Đã xóa lịch");
    if (deleteFromModal.value) closeModal();
    await reloadCurrentView();
  } catch (e) {
    toast.error("Không thể xóa");
  } finally {
    confirmLoading.value = false;
    showConfirm.value = false;
    pendingDeleteId = null;
    deleteFromModal.value = false;
  }
}

// ====================== EXPORT ======================
function openExportMode() { exportMode.value = true; }
function cancelExportMode() { exportMode.value = false; selectedIds.value = []; }
function isSelected(id) { return selectedIds.value.includes(id); }
function toggleSelect(item, checked) {
  if (checked && !selectedIds.value.includes(item.id)) selectedIds.value.push(item.id);
  else if (!checked) selectedIds.value = selectedIds.value.filter((x) => x !== item.id);
}
const allVisibleSelected = computed(() =>
    pagedItems.value.length > 0 && pagedItems.value.every(i => selectedIds.value.includes(i.id))
);
function toggleSelectAllVisible(checked) { pagedItems.value.forEach(item => toggleSelect(item, checked)); }

async function exportSelectedToExcel() {
  if (selectedIds.value.length === 0) return;
  exporting.value = true;
  try {
    const dataToExport = rawSchedules.value
        .filter((item) => selectedIds.value.includes(item.id))
        .map((item, index) => ({
          STT: index + 1,
          "Ngày làm": formatDate(item.ngayLamViec),
          Thứ: getDayOfWeek(item.ngayLamViec),
          "Mã NV": item.maNhanVien,
          "Tên NV": item.tenNhanVien,
          Ca: item.tenCa,
          Giờ: `${formatTime(item.gioBatDau)} - ${formatTime(item.gioKetThuc)}`,
          "Ghi chú": item.ghiChu || "",
        }));

    const ws = XLSX.utils.json_to_sheet(dataToExport);
    ws["!cols"] = [{ wch: 6 }, { wch: 12 }, { wch: 10 }, { wch: 10 }, { wch: 22 }, { wch: 14 }, { wch: 16 }, { wch: 22 }];
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "LichNhanVien");
    XLSX.writeFile(wb, `LichNhanVien_${Date.now()}.xlsx`);
    toast.success("Xuất Excel thành công");
    cancelExportMode();
  } catch (e) {
    toast.error("Lỗi xuất Excel");
  } finally {
    exporting.value = false;
  }
}

// ====================== HELPERS ======================
const formatTime = (t) => (t ? t.substring(0, 5) : "-");
const formatDate = (d) => {
  if (!d) return "";
  const [y, m, day] = d.split("-");
  return `${day}/${m}/${y}`;
};
const getDayOfWeek = (d) =>
    ["Chủ nhật", "Thứ hai", "Thứ ba", "Thứ tư", "Thứ năm", "Thứ sáu", "Thứ bảy"][new Date(d).getDay()];

// Avatar helpers
function getBackendOrigin() {
  const base = String(import.meta.env.VITE_API_URL || "http://localhost:8080").trim();
  try { return new URL(base).origin; } catch { return "http://localhost:8080"; }
}
function resolveFileUrl(url) {
  const u = String(url || "").trim();
  if (!u) return "";
  if (u.startsWith("http://") || u.startsWith("https://") || u.startsWith("data:image")) return u;
  const origin = getBackendOrigin();
  return u.startsWith("/") ? origin + u : origin + "/" + u;
}
function resolveAvatarUrl(item) {
  const direct = String(item?.anhDaiDien || "").trim();
  const fromStaff = String(staffById.value?.[item?.idNhanVien]?.anhDaiDien || "").trim();
  const url = direct || fromStaff;
  if (!url) return "";
  return resolveFileUrl(url);
}
function onEmpAvatarError(e, item) {
  if (item) item.anhDaiDien = "";
  if (e?.target) e.target.src = "";
}
function getInitials(name) {
  const s = String(name || "").trim();
  if (!s) return "NV";
  const parts = s.split(/\s+/).filter(Boolean);
  const a = parts[0]?.[0] || "N";
  const b = parts[parts.length - 1]?.[0] || "V";
  return (a + b).toUpperCase();
}
</script>

<style scoped>
/* Reuse Filter & Table Styles from ShiftTemplateList */
.filter-card { border-radius: 14px; overflow: hidden; border: 1px solid #e9ecef; }
.filter-header { background: #1f2a44; color: #fff; padding: 12px 16px; cursor: pointer; user-select: none; }
.filter-title { font-weight: 700; }
.filter-body { background: #f8fafc; }
.filter-card .form-control, .filter-card .form-select { border-radius: 10px; }
.filter-hint { opacity: 0.75; }

.filter-icon{
  display:inline-flex;
  width:26px;height:26px;
  align-items:center;justify-content:center;
  border-radius:8px;
  background:rgba(255,255,255,.12);
  font-size:12px;
  transition:transform .2s ease;
}
.filter-header[aria-expanded="false"] .filter-icon{ transform: rotate(-90deg); }

.table-wrap { border: 1px solid #dee2e6; border-radius: 12px; overflow: auto; background: #fff; text-align: center; }
.custom-table { width: 100%; min-width: 1000px; border-collapse: separate; border-spacing: 0; }
.custom-table th, .custom-table td { padding: 12px; border-bottom: 1px solid #e9ecef; vertical-align: middle; white-space: nowrap; }
.custom-table thead th { background: #1f2a44; color: #fff; font-weight: 700; }
.ellipsis { display: block; overflow: hidden; text-overflow: ellipsis; max-width: 100%; }
.empty { text-align: center; padding: 20px; color: #6c757d; }
.badge-muted { background: #f8f9fa; color: #212529; font-weight: 500}
.badge{font-weight: 500}

/* ===================== CALENDAR FIX ===================== */
.calendar-toolbar .form-control { border-radius: 10px; }

.calendar-wrap{
  border: 1px solid #dee2e6;
  border-radius: 12px;
  overflow: auto;
  background: #fff;
}

.calendar-table{
  width: 100%;
  min-width: 1380px;
  table-layout: fixed;
  border-collapse: separate;
  border-spacing: 0;
}

.calendar-table th, .calendar-table td{
  border-bottom: 1px solid #e9ecef;
  border-right: 1px solid #e9ecef;
  padding: 10px;
  vertical-align: top;
  overflow: hidden;
}

.calendar-table td{
  display: table-cell !important;
  text-align: left;
}

.calendar-table thead th{
  background: #1f2a44 !important;
  color: #fff !important;
  font-weight: 700;
  position: sticky;
  top: 0;
  z-index: 3;
  text-align: center;
}

.calendar-table thead th.shift-col{
  text-align: left;
}

.shift-col{
  position: sticky;
  left: 0;
  z-index: 2;
  background: #f8fafc;
  width: 260px; min-width: 260px; max-width: 260px;
}

.calendar-table thead th.shift-col{
  left: 0;
  z-index: 4;
}

.shift-info{
  border-right: 1px solid #e9ecef;
}

.calendar-table tbody tr{
  height: 140px;
}

.emp-badge-code{
  margin-top: 2px;
  font-size: 12px;
  font-weight: 600;
  color: #6c757d;
  width: 100%;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.calendar-cell{
  position: relative;
  min-height: 140px;
  background: #fff;
}

.calendar-cell.today{
  background: #f5f9ff;
}

.day-head.today{
  box-shadow: inset 0 -2px 0 rgba(255,255,255,0.35);
}

/* ===== Ô trống: nút + ở GIỮA ô ===== */
.add-btn-center{
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  opacity: 0;
  transition: opacity .15s ease, transform .15s ease;
}
.calendar-cell:hover .add-btn-center{
  opacity: 1;
  transform: translate(-50%, -50%) scale(1.02);
}

/* ===== Badge nhân viên kiểu mẫu (card giữa ô) ===== */
.emp-badge{
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);

  width: 190px;
  max-width: calc(100% - 16px);

  background: #fff;
  border: 1px solid #e9ecef;
  border-left: 5px solid #22c55e;
  border-radius: 12px;

  padding: 10px 10px 12px;
  cursor: pointer;

  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
}

.emp-badge:hover{
  border-color: #cfe2ff;
  border-left-color: #22c55e;
}

.emp-badge-avatar{
  width: 44px;
  height: 44px;
  border-radius: 999px;
  object-fit: cover;
  border: 1px solid #dee2e6;
  margin-bottom: 6px;
}

.emp-badge-initials{
  width: 44px;
  height: 44px;
  border-radius: 999px;
  background: #1f2a44;
  color: #fff;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 6px;
}

.emp-badge-name{
  width: 100%;
  text-align: center;
  font-weight: 700;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ===================== MODAL ===================== */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 9999; display: flex; align-items: center; justify-content: center; }
.modal-card { background: #fff; border-radius: 14px; padding: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.2); min-width: 400px; }
.modal-title { margin-bottom: 15px; font-size: 18px; font-weight: 700; }
.modal-actions { display: flex; gap: 10px; justify-content: flex-end; }

/* Confirm phải nổi trên modal cập nhật */
.confirm-overlay { z-index: 10050 !important; }
</style>
