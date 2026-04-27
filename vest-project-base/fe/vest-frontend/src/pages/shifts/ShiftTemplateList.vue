<template>
  <div class="container-fluid py-3">
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-clock-history fs-4"></i>
        <h5 class="mb-0">Ca làm việc</h5>
      </div>

      <div class="d-flex align-items-center gap-2">
        <button
            v-if="!exportMode"
            class="btn btn-outline-primary btn-sm"
            type="button"
            @click="openExportMode"
        >
          <i class="bi bi-file-earmark-excel me-1"></i> Xuất Excel
        </button>

        <template v-else>
          <button
              class="btn btn-success btn-sm"
              type="button"
              :disabled="selectedIds.length === 0 || exporting"
              @click="exportSelectedToExcel"
          >
            <i class="bi bi-download me-1"></i>
            {{ exporting ? "Đang xuất..." : `Tải xuống (${selectedIds.length})` }}
          </button>

          <button
              class="btn btn-outline-secondary btn-sm"
              type="button"
              :disabled="exporting"
              @click="cancelExportMode"
          >
            <i class="bi bi-x-lg me-1"></i> Hủy
          </button>
        </template>

        <button class="btn btn-outline-primary btn-sm" @click="openModal()">
          <i class="bi bi-plus-lg me-1"></i> Thêm mới
        </button>
      </div>
    </div>

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
            <div class="col-12 col-md-4">
              <label class="form-label">Tìm kiếm</label>
              <input
                  v-model.trim="filters.keyword"
                  type="text"
                  class="form-control"
                  placeholder="Nhập tên ca, mô tả..."
                  @input="applyFilters"
              />
            </div>

            <div class="col-12 col-md-4">
              <label class="form-label">Khoảng giờ bắt đầu</label>
              <div class="input-group">
                <input
                    type="time"
                    class="form-control"
                    v-model="filters.startTime"
                    @change="applyFilters"
                >
                <span class="input-group-text bg-white">-</span>
                <input
                    type="time"
                    class="form-control"
                    v-model="filters.endTime"
                    @change="applyFilters"
                >
              </div>
            </div>

            <div class="col-12 col-md-4">
              <label class="form-label">Trạng thái</label>
              <div class="d-flex align-items-center gap-3 mt-2">
                <div class="form-check">
                  <input
                      class="form-check-input"
                      type="radio"
                      name="statusFilter"
                      :value="null"
                      v-model="filters.status"
                      @change="applyFilters"
                      id="stAll"
                  />
                  <label class="form-check-label" for="stAll">Tất cả</label>
                </div>
                <div class="form-check">
                  <input
                      class="form-check-input"
                      type="radio"
                      name="statusFilter"
                      :value="1"
                      v-model="filters.status"
                      @change="applyFilters"
                      id="stActive"
                  />
                  <label class="form-check-label" for="stActive">Hoạt động</label>
                </div>
                <div class="form-check">
                  <input
                      class="form-check-input"
                      type="radio"
                      name="statusFilter"
                      :value="0"
                      v-model="filters.status"
                      @change="applyFilters"
                      id="stInactive"
                  />
                  <label class="form-check-label" for="stInactive">Ngưng</label>
                </div>
              </div>
            </div>

            <div class="col-12 d-flex justify-content-end">
              <button class="btn btn-light btn-sm" @click="resetFilters">
                <i class="bi bi-arrow-counterclockwise me-1"></i> Đặt lại bộ lọc
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="card shadow-sm">
      <div class="card-body">
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border text-primary" role="status"></div>
          <div class="mt-2 text-muted">Đang tải dữ liệu...</div>
        </div>

        <div v-else class="table-wrap">
          <table class="custom-table">
            <colgroup>
              <col v-if="exportMode" style="width: 40px" /> <col style="width: 60px" />  <col style="width: 200px" /> <col style="width: 120px" /> <col style="width: 120px" /> <col style="width: 250px" /> <col style="width: 140px" /> <col style="width: 120px" /> </colgroup>

            <thead>
            <tr>
              <th v-if="exportMode" class="text-center">
                <input
                    type="checkbox"
                    :disabled="pagedItems.length === 0"
                    :checked="allVisibleSelected"
                    @change="toggleSelectAllVisible($event.target.checked)"
                    title="Chọn tất cả trang này"
                />
              </th>

              <th class="text-center">STT</th>
              <th>Tên Ca</th>
              <th>Giờ Bắt đầu</th>
              <th>Giờ Kết thúc</th>
              <th>Mô tả</th>
              <th>Trạng thái</th>
              <th class="text-end pe-3">Hành động</th>
            </tr>
            </thead>

            <tbody>
            <tr v-if="pagedItems.length === 0">
              <td :colspan="exportMode ? 8 : 7" class="empty">Không tìm thấy dữ liệu phù hợp</td>
            </tr>

            <tr v-for="(item, index) in pagedItems" :key="item.id">
              <td v-if="exportMode" class="text-center">
                <input type="checkbox" :checked="isSelected(item.id)" @change="toggleSelect(item, $event.target.checked)" />
              </td>

              <td class="text-center">{{ (page.page - 1) * page.size + index + 1 }}</td>

              <td >{{ item.tenCa }}</td>
              <td>{{ formatTime(item.gioBatDau) }}</td>
              <td>{{ formatTime(item.gioKetThuc) }}</td>
              <td>
                <span class="ellipsis" :title="item.moTa">{{ item.moTa || '-' }}</span>
              </td>

              <td>
                  <span class="badge" :class="item.trangThai === 1 ? 'badge-success' : 'badge-muted'">
                    {{ item.trangThai === 1 ? 'Hoạt động' : 'Ngưng' }}
                  </span>
              </td>

              <td class="text-end pe-3">
                <div class="d-flex justify-content-end align-items-center gap-2">
                  <button class="btn btn-outline-warning btn-sm" @click="openModal(item)" title="Chỉnh sửa">
                    <i class="bi bi-pencil-square"></i>
                  </button>

                  <label class="switch" title="Bật/Tắt trạng thái">
                    <input
                        type="checkbox"
                        :checked="item.trangThai === 1"
                        @change="onToggleStatus(item, $event)"
                    />
                    <span class="slider"></span>
                  </label>
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

    <teleport to="body">
      <div v-if="showConfirm" class="modal-overlay modal-overlay--confirm" @click.self="closeConfirm">
        <div class="modal-card">
          <h3 class="modal-title">Xác nhận</h3>
          <p class="modal-desc">{{ confirmText }}</p>
          <div class="modal-actions">
            <button class="btn btn-outline-secondary" :disabled="confirmLoading" @click="closeConfirm">Hủy</button>
            <button class="btn btn-primary" :disabled="confirmLoading" @click="confirmYes">
              {{ confirmLoading ? "Đang xử lý..." : "Đồng ý" }}
            </button>
          </div>
        </div>
      </div>
    </teleport>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-card shadow-lg" style="width: 500px;">
        <h3 class="modal-title border-bottom pb-2 mb-3">
          {{ isEdit ? 'Cập nhật Ca Mẫu' : 'Thêm Ca Mẫu Mới' }}
        </h3>

        <form @submit.prevent="requestSubmitForm">
          <div class="mb-3">
            <label class="form-label fw-bold small">Tên Ca <span class="text-danger">*</span></label>
            <input v-model="form.tenCa" class="form-control" placeholder="VD: Ca Sáng, Ca Hành Chính..." required />
          </div>

          <div class="row">
            <div class="col-6 mb-3">
              <label class="form-label fw-bold small">Giờ Bắt đầu <span class="text-danger">*</span></label>
              <input v-model="form.gioBatDau" type="time" class="form-control" required />
            </div>
            <div class="col-6 mb-3">
              <label class="form-label fw-bold small">Giờ Kết thúc <span class="text-danger">*</span></label>
              <input v-model="form.gioKetThuc" type="time" class="form-control" required />
            </div>
          </div>

          <div class="mb-3">
            <label class="form-label fw-bold small">Mô tả / Ghi chú</label>
            <textarea v-model="form.moTa" class="form-control" rows="3"></textarea>
          </div>

          <div class="mb-3 form-check form-switch">
            <input class="form-check-input" type="checkbox" id="activeFormSwitch" v-model="isFormActive">
            <label class="form-check-label fw-bold small" for="activeFormSwitch">Trạng thái Hoạt động</label>
          </div>

          <div class="modal-actions mt-4">
            <button type="button" class="btn btn-outline-secondary" @click="closeModal">Hủy bỏ</button>
            <button type="submit" class="btn btn-primary fw-bold px-4">Lưu thông tin</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from "vue";
import shiftApi from "@/services/shiftApi";
import { useToast } from "@/composables/useToast";
import * as XLSX from "xlsx";

const toast = useToast();

// =====================
// UI State
// =====================
const loading = ref(false);
const rawTemplates = ref([]); // Dữ liệu gốc từ API
const showModal = ref(false);
const isEdit = ref(false);
const editingId = ref(null);

// =====================
// Filters & Pagination
// =====================
const filters = reactive({
  keyword: "",
  startTime: "",
  endTime: "",
  status: null, // null: All, 1: Active, 0: Inactive
});

const page = reactive({ page: 1, size: 10 });
const pageInput = ref(1);

// Debounce filter
let filterTimeout = null;
function applyFilters() {
  clearTimeout(filterTimeout);
  filterTimeout = setTimeout(() => {
    page.page = 1;
    pageInput.value = 1;
  }, 300);
}

function resetFilters() {
  filters.keyword = "";
  filters.startTime = "";
  filters.endTime = "";
  filters.status = null;
  page.page = 1;
}

// =====================
// Computed Data
// =====================
const filteredItems = computed(() => {
  let items = rawTemplates.value || [];

  // 1. Filter Keyword
  const kw = filters.keyword.toLowerCase();
  if (kw) {
    items = items.filter(i =>
        i.tenCa.toLowerCase().includes(kw) ||
        (i.moTa && i.moTa.toLowerCase().includes(kw))
    );
  }

  // 2. Filter Status
  if (filters.status !== null) {
    items = items.filter(i => i.trangThai === filters.status);
  }

  // 3. Filter Time
  if (filters.startTime) {
    items = items.filter(i => i.gioBatDau >= filters.startTime);
  }
  if (filters.endTime) {
    items = items.filter(i => i.gioKetThuc <= filters.endTime);
  }

  return items;
});

const totalElements = computed(() => filteredItems.value.length);
const totalPages = computed(() => Math.ceil(totalElements.value / page.size));

const pagedItems = computed(() => {
  const start = (page.page - 1) * page.size;
  return filteredItems.value.slice(start, start + page.size);
});

// =====================
// Confirm Logic
// =====================
const showConfirm = ref(false);
const confirmText = ref("");
const confirmLoading = ref(false);
let pendingConfirmAction = null;

function openConfirm(text, action) {
  confirmText.value = text;
  pendingConfirmAction = action;
  showConfirm.value = true;
}

function closeConfirm() {
  if (confirmLoading.value) return;
  showConfirm.value = false;
  pendingConfirmAction = null;
}

async function confirmYes() {
  if (!pendingConfirmAction) return;
  confirmLoading.value = true;
  try {
    await pendingConfirmAction();
  } finally {
    confirmLoading.value = false;
    showConfirm.value = false;
  }
}

// =====================
// Form Logic (Create/Edit)
// =====================
const form = reactive({
  tenCa: '',
  gioBatDau: '08:00',
  gioKetThuc: '12:00',
  moTa: '',
  trangThai: 1
});

const isFormActive = computed({
  get: () => form.trangThai === 1,
  set: (val) => form.trangThai = val ? 1 : 0
});

function openModal(item = null) {
  isEdit.value = !!item;
  if (item) {
    editingId.value = item.id;
    form.tenCa = item.tenCa;
    form.gioBatDau = item.gioBatDau ? item.gioBatDau.substring(0, 5) : '';
    form.gioKetThuc = item.gioKetThuc ? item.gioKetThuc.substring(0, 5) : '';
    form.moTa = item.moTa;
    form.trangThai = item.trangThai;
  } else {
    // Reset defaults
    form.tenCa = '';
    form.gioBatDau = '08:00';
    form.gioKetThuc = '12:00';
    form.moTa = '';
    form.trangThai = 1;
  }
  showModal.value = true;
}

function closeModal() {
  showModal.value = false;
}

// Popup xác nhận trước khi lưu
function requestSubmitForm() {
  const action = isEdit.value ? "cập nhật" : "thêm mới";
  openConfirm(`Bạn có chắc chắn muốn ${action} ca làm việc này?`, async () => {
    await submitFormReal();
  });
}

async function submitFormReal() {
  try {
    // Fix format time HH:mm:ss for backend
    const payload = { ...form };
    if(payload.gioBatDau.length === 5) payload.gioBatDau += ':00';
    if(payload.gioKetThuc.length === 5) payload.gioKetThuc += ':00';

    if (isEdit.value) {
      await shiftApi.updateTemplate(editingId.value, payload);
      toast.success("Cập nhật thành công!");
    } else {
      await shiftApi.createTemplate(payload);
      toast.success("Tạo ca mẫu thành công!");
    }
    closeModal();
    await loadData();
  } catch (e) {
    toast.error(e.response?.data?.message || "Có lỗi xảy ra");
  }
}

// =====================
// Actions & API
// =====================
onMounted(loadData);

async function loadData() {
  loading.value = true;
  try {
    const res = await shiftApi.getTemplates();
    const data = res.data;

    // Xử lý unwrap data
    if (Array.isArray(data)) rawTemplates.value = data;
    else if (data && Array.isArray(data.result)) rawTemplates.value = data.result;
    else if (data && Array.isArray(data.data)) rawTemplates.value = data.data;
    else rawTemplates.value = [];

  } catch (e) {
    console.error(e);
    toast.error("Lỗi tải dữ liệu");
  } finally {
    loading.value = false;
  }
}

// Switch toggle logic
async function onToggleStatus(item, event) {
  const newStatus = item.trangThai === 1 ? 0 : 1;
  // Revert UI visual change until confirmed
  event.target.checked = item.trangThai === 1;

  const actionText = newStatus === 1 ? "KÍCH HOẠT" : "NGƯNG HOẠT ĐỘNG";

  openConfirm(`Bạn có chắc muốn ${actionText} ca làm việc này?`, async () => {
    try {
      const payload = {
        tenCa: item.tenCa,
        gioBatDau: item.gioBatDau,
        gioKetThuc: item.gioKetThuc,
        moTa: item.moTa,
        trangThai: newStatus
      };
      await shiftApi.updateTemplate(item.id, payload);

      // Update local state
      item.trangThai = newStatus;
      toast.success(`Đã đổi trạng thái sang ${newStatus === 1 ? 'Hoạt động' : 'Ngưng'}`);
    } catch (e) {
      toast.error("Lỗi cập nhật trạng thái");
    }
  });
}

function setPage(p) {
  if (p < 1 || p > totalPages.value) return;
  page.page = p;
  pageInput.value = p;
}

function jumpPage() {
  const target = Math.max(1, Math.min(pageInput.value, totalPages.value));
  setPage(target);
}

// =====================
// Export Excel Logic
// =====================
const exportMode = ref(false);
const exporting = ref(false);
const selectedIds = ref([]);

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
  if (checked) {
    if (!selectedIds.value.includes(item.id)) selectedIds.value.push(item.id);
  } else {
    selectedIds.value = selectedIds.value.filter(id => id !== item.id);
  }
}

const allVisibleSelected = computed(() => {
  if (pagedItems.value.length === 0) return false;
  return pagedItems.value.every(i => selectedIds.value.includes(i.id));
});

function toggleSelectAllVisible(checked) {
  pagedItems.value.forEach(item => toggleSelect(item, checked));
}

async function exportSelectedToExcel() {
  if (selectedIds.value.length === 0) return;
  exporting.value = true;

  try {
    // Lấy data các dòng được chọn
    const dataToExport = rawTemplates.value
        .filter(item => selectedIds.value.includes(item.id))
        .map((item, index) => ({
          "STT": index + 1,
          "Tên Ca": item.tenCa,
          "Giờ Bắt Đầu": formatTime(item.gioBatDau),
          "Giờ Kết Thúc": formatTime(item.gioKetThuc),
          "Mô Tả": item.moTa || '',
          "Trạng Thái": item.trangThai === 1 ? "Hoạt động" : "Ngưng"
        }));

    const ws = XLSX.utils.json_to_sheet(dataToExport);
    // Auto width column basic
    ws["!cols"] = [
      { wch: 6 },  // STT
      { wch: 20 }, // Ten
      { wch: 12 }, // Gio BD
      { wch: 12 }, // Gio KT
      { wch: 30 }, // Mo ta
      { wch: 15 }  // Trang thai
    ];

    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "DS_CaMau");

    XLSX.writeFile(wb, `CaLamViec_${new Date().getTime()}.xlsx`);
    toast.success("Xuất Excel thành công!");
    cancelExportMode();
  } catch (e) {
    console.error(e);
    toast.error("Xuất Excel thất bại");
  } finally {
    exporting.value = false;
  }
}

// Helper
const formatTime = (t) => t ? t.substring(0, 5) : '-';

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
.filter-card .form-control,
.filter-card .form-select {
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
  min-width: 1000px; /* Đảm bảo ko bị vỡ trên mobile */
  table-layout: fixed;
  border-collapse: separate;
  border-spacing: 0;
}
.custom-table th,
.custom-table td {
  padding: 12px 12px;
  border-bottom: 1px solid #e9ecef;
  vertical-align: middle;
  white-space: nowrap;
}
.custom-table thead th {
  background: #1f2a44; /* Đồng bộ màu header */
  color: #fff;
  font-weight: 700;
}
.ellipsis {
  display: block;
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
}
.empty {
  text-align: center;
  padding: 18px 0;
  color: #6b7280;
}

/* Badges */
.badge-success {
  background: #1d4ed8;
  color: #f1f5f9;
}
.badge-muted {
  background: #e5e7eb;
  color: #374151;
}

.badge{    --bs-badge-font-weight: 600;}

/* Switch Button */
.switch {
  position: relative;
  display: inline-block;
  width: 42px;
  height: 22px;
  vertical-align: middle;
}
.switch input {
  display: none;
}
.slider {
  position: absolute;
  inset: 0;
  background-color: #e5e7eb;
  border-radius: 999px;
  cursor: pointer;
  transition: 0.2s;
}
.slider::before {
  content: "";
  position: absolute;
  width: 18px;
  height: 18px;
  left: 2px;
  top: 2px;
  background: white;
  border-radius: 999px;
  transition: 0.2s;
}
.switch input:checked + .slider {
  background-color: #1d4ed8;
}
.switch input:checked + .slider::before {
  transform: translateX(20px);
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
.modal-card {
  background: #fff;
  border-radius: 14px;
  padding: 18px 24px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  min-width: 400px;
}
.modal-title {
  margin: 0 0 12px;
  font-size: 18px;
  font-weight: 700;
}
.modal-desc {
  margin: 0 0 20px;
  color: #555;
  line-height: 1.4;
}
.modal-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
.modal-overlay--confirm {
  z-index: 10050; /* cao hơn modal form */
}
.fw-bold {
  font-weight: 500 !important;
}
</style>