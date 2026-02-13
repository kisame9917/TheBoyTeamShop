<template>
  <div class="container-fluid py-3">
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-person-badge-fill fs-4"></i>
        <h5 class="mb-0">Lịch Làm Việc Của Tôi</h5>
      </div>

      <div class="d-flex align-items-center gap-2">
        <button v-if="!exportMode" class="btn btn-outline-primary btn-sm" @click="openExportMode">
          <i class="bi bi-file-earmark-excel me-1"></i> Xuất Excel
        </button>

        <template v-else>
          <button
              class="btn btn-success btn-sm"
              :disabled="selectedIds.length === 0 || exporting"
              @click="exportSelectedToExcel"
          >
            <i class="bi bi-download me-1"></i> Tải xuống ({{ selectedIds.length }})
          </button>
          <button class="btn btn-outline-secondary btn-sm" @click="cancelExportMode">Hủy</button>
        </template>
      </div>
    </div>

    <!-- FILTER -->
    <div class="card shadow-sm mb-3 filter-card">
      <div
          class="filter-header d-flex align-items-center justify-content-between"
          data-bs-toggle="collapse"
          data-bs-target="#myFilterBody"
          role="button"
          aria-expanded="true"
          aria-controls="myFilterBody"
      >
        <div class="d-flex align-items-center gap-2">
          <span class="filter-icon">▼</span>
          <span class="filter-title">Bộ lọc tìm kiếm</span>
        </div>
        <small class="filter-hint">Nhấn để thu gọn/mở rộng</small>
      </div>

      <div id="myFilterBody" class="collapse show">
        <div class="card-body filter-body">
          <div class="row g-3">
            <div class="col-12 col-lg-6">
              <label class="form-label">Tìm kiếm tên ca</label>
              <input
                  v-model.trim="filters.keyword"
                  type="text"
                  class="form-control"
                  placeholder="Nhập tên ca..."
                  @input="applyFilters"
              />
            </div>

            <!-- Từ ngày (giống voucher) -->
            <div class="col-12 col-lg-3">
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

            <!-- Đến ngày (giống voucher) -->
            <div class="col-12 col-lg-3">
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

            <!-- Đặt lại: góc dưới bên phải -->
            <div class="col-12 d-flex justify-content-end">
              <button class="btn btn-light btn-sm" type="button" @click="resetFilters">
                <i class="bi bi-arrow-counterclockwise me-1"></i> Đặt lại
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- TABLE -->
    <div class="card shadow-sm">
      <div class="card-body">
        <div class="table-wrap">
          <table class="custom-table">
            <colgroup>
              <col v-if="exportMode" style="width: 40px" />
              <col style="width: 50px" />
              <col style="width: 130px" />  <!-- Ngày -->
              <col style="width: 140px" />  <!-- Thứ -->
              <col style="width: 160px" />  <!-- Tên ca -->
              <col style="width: 170px" />  <!-- Thời gian -->
              <col style="width: 150px" />  <!-- Trạng thái -->
              <col style="width: 220px" />  <!-- Ghi chú -->
            </colgroup>

            <thead>
            <tr>
              <th v-if="exportMode" class="text-center">
                <input
                    type="checkbox"
                    :checked="allVisibleSelected"
                    @change="toggleSelectAllVisible($event.target.checked)"
                    :disabled="pagedItems.length === 0"
                    title="Chọn tất cả trang này"
                />
              </th>
              <th class="text-center">STT</th>
              <th>Ngày</th>
              <th>Thứ</th>
              <th>Tên Ca</th>
              <th>Thời gian</th>
              <th class="text-center">Trạng thái</th>
              <th>Ghi chú</th>
            </tr>
            </thead>

            <tbody>
            <tr v-if="pagedItems.length === 0">
              <td :colspan="exportMode ? 8 : 7" class="empty">
                <i class="bi bi-calendar-x fs-3 d-block mb-2"></i>
                Không có lịch làm việc trong khoảng thời gian này
              </td>
            </tr>

            <tr v-for="(item, index) in pagedItems" :key="item.id" :class="getRowClass(item)">
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
              <td>{{ item.tenCa }}</td>
              <td>{{ formatTime(item.gioBatDau) }} - {{ formatTime(item.gioKetThuc) }}</td>

              <td class="text-center">
                  <span class="badge" :class="getDetailedStatus(item).class">
                    {{ getDetailedStatus(item).text }}
                  </span>
              </td>

              <td>{{ item.ghiChu || "-" }}</td>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch  } from "vue";
import shiftApi from "@/services/shiftApi";
import { useAuthStore } from "@/stores/auth";
import * as XLSX from "xlsx";
import { useToast } from "@/composables/useToast";

// Date picker (giống voucher style input + open/clear)
import flatpickr from "flatpickr";
import "flatpickr/dist/flatpickr.css";
import { Vietnamese } from "flatpickr/dist/l10n/vn.js";

const authStore = useAuthStore();
const toast = useToast();

const rawSchedules = ref([]);
const myId = ref(null);

// ===== Default date range: đầu năm -> cuối năm =====
const toDateStr = (d) => {
  const offset = d.getTimezoneOffset() * 60000;
  return new Date(d.getTime() - offset).toISOString().split("T")[0];
};

const getLastDayOfMonthISOFromDate = (d) => {
  return toDateStr(new Date(d.getFullYear(), d.getMonth() + 1, 0));
};

const today = new Date();
const defaultFrom = toDateStr(today);                 // hôm nay
const defaultTo = getLastDayOfMonthISOFromDate(today); // cuối tháng hiện tại

const filters = reactive({
  keyword: "",
  from: defaultFrom, // ISO YYYY-MM-DD
  to: defaultTo,     // ISO YYYY-MM-DD
});

const page = reactive({ page: 1, size: 10 });
const pageInput = ref(1);

// ===== Export State =====
const exportMode = ref(false);
const exporting = ref(false);
const selectedIds = ref([]);

// ===== Date pickers =====
const fromPickerRef = ref(null);
const toPickerRef = ref(null);
let fromPickerInstance = null;
let toPickerInstance = null;

const isoToLocalDate = (iso) => {
  if (!iso) return null;
  const [y, m, d] = iso.split("-").map(Number);
  return new Date(y, m - 1, d);
};

const getFromParam = () => (filters.from ? filters.from : defaultFrom);
const getToParam = () => (filters.to ? filters.to : defaultTo);

onMounted(() => {
  myId.value = authStore.userId || authStore.user?.id;

  if (!myId.value) {
    toast.error("Không tìm thấy thông tin nhân viên. Vui lòng đăng nhập lại.");
    return;
  }

  // Init pickers
  fromPickerInstance = flatpickr(fromPickerRef.value, {
    locale: Vietnamese,
    allowInput: true,
    dateFormat: "d/m/Y",
    defaultDate: isoToLocalDate(filters.from),
    onChange: (selectedDates) => {
      filters.from = selectedDates?.[0] ? toDateStr(selectedDates[0]) : "";
      page.page = 1;
      loadMySchedule();
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
      loadMySchedule();
    },
  });

  loadMySchedule();
});

function openFromPicker() {
  fromPickerInstance?.open();
}

function openToPicker() {
  toPickerInstance?.open();
}

function clearFromDate() {
  filters.from = "";
  fromPickerInstance?.clear();
  page.page = 1;
  loadMySchedule();
}

function clearToDate() {
  filters.to = "";
  toPickerInstance?.clear();
  page.page = 1;
  loadMySchedule();
}

function resetFilters() {
  filters.keyword = "";
  filters.from = defaultFrom;
  filters.to = defaultTo;

  fromPickerInstance?.setDate(isoToLocalDate(defaultFrom), true);
  toPickerInstance?.setDate(isoToLocalDate(defaultTo), true);

  page.page = 1;
  loadMySchedule();
}

async function loadMySchedule(id = myId.value) {
  try {
    const res = await shiftApi.getMySchedule(id, getFromParam(), getToParam());
    const data = res.data;
    if (Array.isArray(data)) rawSchedules.value = data;
    else if (data && Array.isArray(data.data)) rawSchedules.value = data.data;
    else rawSchedules.value = [];
  } catch (e) {
    console.error(e);
    const status = e?.response?.status;
    const msg = e?.response?.data?.message || e?.message || "Không tải được lịch làm việc";
    if (status === 403) {
      toast.error(
        "Bạn không có quyền xem lịch cá nhân (403). Cần cập nhật BE: cho phép STAFF gọi GET /api/ca-lam-viec/lich-ca-nhan (SecurityConfig)."
      );
      return;
    }
    toast.error(msg);
  }
}

// ===== Filtered (client-side) =====
const filteredItems = computed(() => {
  let items = rawSchedules.value || [];

  if (filters.keyword) {
    const kw = filters.keyword.toLowerCase();
    items = items.filter((i) => (i.tenCa || "").toLowerCase().includes(kw));
  }

  // Sắp xếp mới nhất lên đầu
  return items.sort((a, b) => {
    const timeA = new Date(`${a.ngayLamViec}T${a.gioBatDau}`);
    const timeB = new Date(`${b.ngayLamViec}T${b.gioBatDau}`);
    return timeB - timeA;
  });
});

const totalElements = computed(() => filteredItems.value.length);
const totalPages = computed(() => Math.ceil(totalElements.value / page.size));
const pagedItems = computed(() =>
    filteredItems.value.slice((page.page - 1) * page.size, page.page * page.size)
);

// ===== Trạng thái =====
function getDetailedStatus(item) {
  const startDateTime = new Date(`${item.ngayLamViec}T${item.gioBatDau}`);
  let endDateTime = new Date(`${item.ngayLamViec}T${item.gioKetThuc}`);
  // Nếu ca qua đêm (end <= start) thì coi kết thúc ở ngày hôm sau
  if (endDateTime <= startDateTime) {
    endDateTime = new Date(endDateTime.getTime() + 24 * 60 * 60 * 1000);
  }
  const now = new Date();

  if (now > endDateTime) return { text: "Đã làm", class: "bg-secondary" };
  if (now >= startDateTime && now <= endDateTime) return { text: "Đang làm", class: "bg-success" };
  return { text: "Sắp đến", class: "bg-primary" };
}

function getRowClass(item) {
  const status = getDetailedStatus(item).text;
  if (status === "Đang làm") return "bg-success bg-opacity-10";
  return "";
}

// ===== Helpers =====
const formatDate = (d) => (d ? `${d.split("-")[2]}/${d.split("-")[1]}/${d.split("-")[0]}` : "");
const formatTime = (t) => (t ? t.substring(0, 5) : "");

const getDayOfWeek = (d) => {
  const map = ["Chủ nhật", "Thứ hai", "Thứ ba", "Thứ tư", "Thứ năm", "Thứ sáu", "Thứ bảy"];
  return map[new Date(d).getDay()];
};

function setPage(p) {
  const total = totalPages.value || 1;
  if (p < 1 || p > total) return;
  page.page = p;
  pageInput.value = p;
}

function jumpPage() {
  const total = totalPages.value || 1;
  const target = Math.max(1, Math.min(pageInput.value || 1, total));
  setPage(target);
}

// Khi đổi số bản ghi / trang => về trang 1
watch(() => page.size, () => {
  page.page = 1;
  pageInput.value = 1;
});

// Nếu totalPages giảm (do filter) mà trang hiện tại vượt quá => kéo về trang cuối
watch(totalPages, (tp) => {
  const total = tp || 1;
  if (page.page > total) setPage(total);
});

function applyFilters() {
  page.page = 1;
  pageInput.value = 1;
}

// ===== Excel =====
function openExportMode() {
  exportMode.value = true;
}

function cancelExportMode() {
  exportMode.value = false;
  selectedIds.value = [];
}

function isSelected(id) {
  return selectedIds.value.includes(id);
}

function toggleSelect(item, checked) {
  if (checked && !selectedIds.value.includes(item.id)) selectedIds.value.push(item.id);
  else if (!checked) selectedIds.value = selectedIds.value.filter((x) => x !== item.id);
}

const allVisibleSelected = computed(
    () => pagedItems.value.length > 0 && pagedItems.value.every((i) => selectedIds.value.includes(i.id))
);

function toggleSelectAllVisible(checked) {
  pagedItems.value.forEach((item) => toggleSelect(item, checked));
}

async function exportSelectedToExcel() {
  if (selectedIds.value.length === 0) return;
  exporting.value = true;

  try {
    const dataToExport = rawSchedules.value
        .filter((i) => selectedIds.value.includes(i.id))
        .map((item, idx) => ({
          STT: idx + 1,
          Ngày: formatDate(item.ngayLamViec),
          Thứ: getDayOfWeek(item.ngayLamViec),
          "Tên ca": item.tenCa,
          "Thời gian": `${formatTime(item.gioBatDau)} - ${formatTime(item.gioKetThuc)}`,
          "Trạng thái": getDetailedStatus(item).text,
          "Ghi chú": item.ghiChu || "",
        }));

    const ws = XLSX.utils.json_to_sheet(dataToExport);
    ws["!cols"] = [
      { wch: 5 },
      { wch: 12 },
      { wch: 12 },
      { wch: 18 },
      { wch: 18 },
      { wch: 12 },
      { wch: 22 },
    ];

    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "LichCuaToi");
    XLSX.writeFile(wb, `MySchedule_${Date.now()}.xlsx`);

    toast.success("Tải xuống thành công");
    cancelExportMode();
  } catch (e) {
    console.error(e);
    toast.error("Lỗi tải xuống");
  } finally {
    exporting.value = false;
  }
}
</script>

<style scoped>
/* ===== Filter ===== */
.filter-card {
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid #e9ecef;
}
.filter-header {
  background: #1f2a44;
  color: #fff;
  padding: 12px 16px;
  cursor: pointer;
  user-select: none;
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
}
.filter-title {
  font-weight: 700;
}
.filter-hint {
  opacity: 0.75;
}
.filter-icon {
  display: inline-flex;
  width: 26px;
  height: 26px;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 12px;
  transition: transform 0.2s ease;
}
.filter-header[aria-expanded="false"] .filter-icon {
  transform: rotate(-90deg);
}
.filter-body {
  background: #f8fafc;
}
.filter-card .form-label {
  font-weight: 600;
}
.filter-card .form-control {
  border-radius: 10px;
}

/* ===== Table ===== */
.table-wrap {
  border: 1px solid #dee2e6;
  border-radius: 12px;
  overflow: auto;
  background: #fff;
  text-align: center;
}
.custom-table {
  width: 100%;
  min-width: 980px;
  border-collapse: separate;
  border-spacing: 0;
}
.custom-table th,
.custom-table td {
  padding: 12px;
  border-bottom: 1px solid #e9ecef;
  vertical-align: middle;
  white-space: nowrap;
}
.custom-table thead th {
  background: #1f2a44;
  color: #fff;
  font-weight: 700;
}

/* tbody chữ bình thường màu đen */
.custom-table tbody td {
  color: #212529;
  font-weight: 400;
}

.empty {
  text-align: center;
  padding: 20px;
  color: #6c757d;
}

/* Giữ highlight dòng đang làm */
.bg-opacity-10 {
  background-color: rgba(25, 135, 84, 0.1) !important;
}
</style>
