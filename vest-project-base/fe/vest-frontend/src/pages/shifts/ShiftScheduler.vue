<template>
  <div class="container-fluid py-3">
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-calendar-check fs-4"></i>
        <h5 class="mb-0">Xếp Lịch Nhân Viên</h5>
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
          <button class="btn btn-outline-secondary btn-sm" @click="cancelExportMode">
            <i class="bi bi-x-lg me-1"></i> Hủy
          </button>
        </template>

        <button class="btn btn-primary btn-sm fw-bold" @click="openModal()">
          <i class="bi bi-plus-lg me-1"></i> Phân Ca Mới
        </button>
      </div>
    </div>

    <div class="card shadow-sm mb-3 filter-card">
      <div
          class="filter-header d-flex align-items-center justify-content-between"
          data-bs-toggle="collapse"
          data-bs-target="#filterBody"
      >
        <div class="d-flex align-items-center gap-2">
          <span class="filter-icon">▼</span>
          <span class="filter-title">Bộ lọc tìm kiếm</span>
        </div>
      </div>

      <div id="filterBody" class="collapse show">
        <div class="card-body filter-body">
          <div class="row g-3">
            <div class="col-12 col-md-4">
              <label class="form-label">Tìm nhân viên</label>
              <input
                  v-model.trim="filters.keyword"
                  type="text"
                  class="form-control"
                  placeholder="Tên hoặc Mã NV..."
                  @input="applyFilters"
              />
            </div>
            <div class="col-12 col-md-4">
              <label class="form-label">Ca làm việc</label>
              <select v-model="filters.shiftId" class="form-select" @change="applyFilters">
                <option :value="null">Tất cả ca</option>
                <option v-for="t in templates" :key="t.id" :value="t.id">{{ t.tenCa }}</option>
              </select>
            </div>
            <div class="col-12 col-md-4">
              <label class="form-label">Thời gian</label>
              <div class="input-group">
                <input type="date" class="form-control" v-model="filters.from" @change="loadSchedule">
                <span class="input-group-text bg-white">-</span>
                <input type="date" class="form-control" v-model="filters.to" @change="loadSchedule">
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

    <div class="card shadow-sm">
      <div class="card-body">
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border text-primary" role="status"></div>
          <div class="mt-2 text-muted">Đang tải dữ liệu...</div>
        </div>

        <div v-else class="table-wrap">
          <table class="custom-table">
            <colgroup>
              <col style="width: 50px" /> <col style="width: 100px" /> <col style="width: 80px" />  <col style="width: 100px" /> <col style="width: 180px" /> <col style="width: 120px" /> <col style="width: 130px" /> <col style="width: 180px" /> <col style="width: 100px" /> <col v-if="exportMode" style="width: 40px" /> </colgroup>
            <thead>
            <tr>
              <th class="text-center">STT</th>
              <th>Ngày làm</th>
              <th>Thứ</th>
              <th>Mã NV</th>
              <th>Tên nhân viên</th>
              <th>Tên Ca</th>
              <th>Khung giờ</th>
              <th>Ghi chú</th>
              <th class="text-end pe-3">Hành động</th>

              <th v-if="exportMode" class="text-center">
                <input
                    type="checkbox"
                    :checked="allVisibleSelected"
                    @change="toggleSelectAllVisible($event.target.checked)"
                />
              </th>
            </tr>
            </thead>
            <tbody>
            <tr v-if="pagedItems.length === 0">
              <td :colspan="exportMode ? 10 : 9" class="empty">Không có dữ liệu</td>
            </tr>
            <tr v-for="(item, index) in pagedItems" :key="item.id">
              <td class="text-center">{{ (page.page - 1) * page.size + index + 1 }}</td>

              <td class="fw-bold">{{ formatDate(item.ngayLamViec) }}</td>
              <td class="text-muted small">{{ getDayOfWeek(item.ngayLamViec) }}</td>

              <td><span class="badge badge-muted text-dark border">{{ item.maNhanVien }}</span></td>
              <td class="fw-bold text-primary">{{ item.tenNhanVien }}</td>

              <td><span class="badge bg-info bg-opacity-10 text-dark border border-info border-opacity-25">{{ item.tenCa }}</span></td>

              <td class="text-primary fw-semibold text-small">
                {{ formatTime(item.gioBatDau) }} - {{ formatTime(item.gioKetThuc) }}
              </td>

              <td><span class="ellipsis" :title="item.ghiChu">{{ item.ghiChu || '-' }}</span></td>

              <td class="text-end pe-3">
                <div class="d-flex justify-content-end gap-2">
                  <button class="btn btn-outline-warning btn-sm border-0" @click="openModal(item)" title="Sửa">
                    <i class="bi bi-pencil-square"></i>
                  </button>
                  <button class="btn btn-outline-danger btn-sm border-0" @click="confirmDelete(item.id)" title="Xóa">
                    <i class="bi bi-trash"></i>
                  </button>
                </div>
              </td>

              <td v-if="exportMode" class="text-center">
                <input type="checkbox" :checked="isSelected(item.id)" @change="toggleSelect(item, $event.target.checked)" />
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        <div class="d-flex align-items-center mt-3 flex-column flex-md-row gap-2" v-if="totalElements > 0">
          <div class="text-muted flex-grow-1">Hiển thị {{ pagedItems.length }} / {{ totalElements }} bản ghi</div>
          <div class="d-flex align-items-center gap-2 justify-content-center flex-grow-1">
            <button class="btn btn-outline-secondary btn-sm" :disabled="page.page === 1" @click="setPage(page.page - 1)"><i class="bi bi-chevron-left"></i></button>
            <span class="mx-2">Trang {{ page.page }} / {{ totalPages }}</span>
            <button class="btn btn-outline-secondary btn-sm" :disabled="page.page >= totalPages" @click="setPage(page.page + 1)"><i class="bi bi-chevron-right"></i></button>
          </div>
          <div class="d-flex justify-content-md-end flex-grow-1">
            <select class="form-select form-select-sm" style="width: 120px" v-model.number="page.size">
              <option :value="10">10 / trang</option>
              <option :value="20">20 / trang</option>
              <option :value="50">50 / trang</option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showConfirm" class="modal-overlay" @click.self="closeConfirm">
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

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-card shadow-lg" style="width: 500px;">
        <h3 class="modal-title border-bottom pb-2 mb-3">
          {{ isEdit ? 'Cập nhật Lịch Làm Việc' : 'Phân Ca Làm Việc Mới' }}
        </h3>
        <form @submit.prevent="submitAssign">
          <div class="mb-3">
            <label class="form-label fw-bold small">Nhân viên <span class="text-danger">*</span></label>
            <select v-model="form.idNhanVien" class="form-select" required :disabled="isEdit"> <option :value="null" disabled>-- Chọn nhân viên --</option>
              <option v-for="nv in staffList" :key="nv.id" :value="nv.id">
                {{ nv.tenNhanVien }} ({{ nv.maNhanVien }})
              </option>
            </select>
            <div v-if="isEdit" class="form-text text-muted">Không thể thay đổi nhân viên khi chỉnh sửa.</div>
          </div>

          <div class="mb-3">
            <label class="form-label fw-bold small">Ca làm việc <span class="text-danger">*</span></label>
            <select v-model="form.idCaLamViec" class="form-select" required>
              <option :value="null" disabled>-- Chọn ca mẫu --</option>
              <option v-for="ca in templates" :key="ca.id" :value="ca.id">
                {{ ca.tenCa }} ({{ formatTime(ca.gioBatDau) }} - {{ formatTime(ca.gioKetThuc) }})
              </option>
            </select>
          </div>

          <div class="mb-3">
            <label class="form-label fw-bold small">Ngày làm việc <span class="text-danger">*</span></label>
            <input v-model="form.ngayLamViec" type="date" class="form-control" required />
          </div>

          <div class="mb-3">
            <label class="form-label fw-bold small">Ghi chú</label>
            <input v-model="form.ghiChu" type="text" class="form-control" placeholder="VD: Tăng ca, trực thay..." />
          </div>

          <div class="modal-actions mt-4">
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
import { ref, reactive, computed, onMounted } from 'vue';
import shiftApi from '@/services/shiftApi';
import * as nhanVienApi from '@/services/nhanVienApi';
import { useToast } from "@/composables/useToast";
import * as XLSX from "xlsx";

const toast = useToast();

// State
const loading = ref(false);
const rawSchedules = ref([]);
const staffList = ref([]);
const templates = ref([]);
const showModal = ref(false);
const isEdit = ref(false);
const editingId = ref(null);

// Filters
const today = new Date();
const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
const lastDay = new Date(today.getFullYear(), today.getMonth() + 1, 0);
const toDateStr = (d) => new Date(d.getTime() - (d.getTimezoneOffset() * 60000)).toISOString().split('T')[0];

const filters = reactive({
  keyword: "",
  shiftId: null,
  from: toDateStr(firstDay),
  to: toDateStr(lastDay)
});

// Pagination
const page = reactive({ page: 1, size: 10 });

// Form
const form = reactive({
  idNhanVien: null,
  idCaLamViec: null,
  ngayLamViec: toDateStr(new Date()),
  ghiChu: ''
});

// Confirm Modal
const showConfirm = ref(false);
const confirmText = ref("");
const confirmLoading = ref(false);
let pendingDeleteId = null;

// Export State
const exportMode = ref(false);
const exporting = ref(false);
const selectedIds = ref([]);

onMounted(() => {
  loadSchedule();
  loadResources();
});

// --- DATA LOADING ---
async function loadSchedule() {
  loading.value = true;
  try {
    const res = await shiftApi.getSchedules(filters.from, filters.to);
    const data = res.data;
    if (Array.isArray(data)) rawSchedules.value = data;
    else if (data && Array.isArray(data.result)) rawSchedules.value = data.result;
    else if (data && Array.isArray(data.data)) rawSchedules.value = data.data;
    else rawSchedules.value = [];
  } catch (e) { console.error(e); } finally { loading.value = false; }
}

async function loadResources() {
  try {
    const resNV = await nhanVienApi.listNhanVien(0, 100);
    let rawNV = [];
    if (resNV.content && Array.isArray(resNV.content)) rawNV = resNV.content;
    else if (resNV.data && Array.isArray(resNV.data)) rawNV = resNV.data;
    else if (Array.isArray(resNV)) rawNV = resNV;
    staffList.value = rawNV.filter(nv => nv.trangThai === 1 || nv.trangThai === true);

    const resCa = await shiftApi.getTemplates();
    const dataCa = resCa.data || resCa;
    if (Array.isArray(dataCa)) templates.value = dataCa;
    else if (dataCa.data) templates.value = dataCa.data;
    else templates.value = [];
  } catch (e) { console.error("Resource error", e); }
}

// --- COMPUTED ---
const filteredItems = computed(() => {
  let items = rawSchedules.value || [];
  if (filters.keyword) {
    const kw = filters.keyword.toLowerCase();
    items = items.filter(i => i.tenNhanVien.toLowerCase().includes(kw) || i.maNhanVien.toLowerCase().includes(kw));
  }
  if (filters.shiftId) {
    items = items.filter(i => i.idCaLamViec === filters.shiftId);
  }
  return items;
});

const totalElements = computed(() => filteredItems.value.length);
const totalPages = computed(() => Math.ceil(totalElements.value / page.size));
const pagedItems = computed(() => filteredItems.value.slice((page.page - 1) * page.size, page.page * page.size));

// --- ACTIONS ---
function resetFilters() {
  filters.keyword = ""; filters.shiftId = null;
  filters.from = toDateStr(firstDay); filters.to = toDateStr(lastDay);
  page.page = 1; loadSchedule();
}

function openModal(item = null) {
  isEdit.value = !!item;
  if (item) {
    editingId.value = item.id;
    form.idNhanVien = item.idNhanVien;
    form.idCaLamViec = item.idCaLamViec;
    form.ngayLamViec = item.ngayLamViec;
    form.ghiChu = item.ghiChu;
  } else {
    form.idNhanVien = null;
    form.idCaLamViec = null;
    form.ngayLamViec = toDateStr(new Date());
    form.ghiChu = '';
  }
  showModal.value = true;
}

function closeModal() { showModal.value = false; }

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
    loadSchedule();
  } catch (e) {
    toast.error(e.response?.data?.message || "Lỗi xếp lịch (có thể trùng giờ)");
  }
}

function confirmDelete(id) {
  confirmText.value = "Bạn có chắc chắn muốn xóa lịch này?";
  pendingDeleteId = id;
  showConfirm.value = true;
}
function closeConfirm() { showConfirm.value = false; pendingDeleteId = null; }
async function confirmYes() {
  if (!pendingDeleteId) return;
  confirmLoading.value = true;
  try {
    await shiftApi.deleteSchedule(pendingDeleteId);
    toast.success("Đã xóa lịch");
    loadSchedule();
  } catch (e) { toast.error("Không thể xóa"); }
  finally { confirmLoading.value = false; showConfirm.value = false; }
}

// --- EXPORT ---
function openExportMode() { exportMode.value = true; }
function cancelExportMode() { exportMode.value = false; selectedIds.value = []; }
function isSelected(id) { return selectedIds.value.includes(id); }
function toggleSelect(item, checked) {
  if (checked && !selectedIds.value.includes(item.id)) selectedIds.value.push(item.id);
  else if (!checked) selectedIds.value = selectedIds.value.filter(id => id !== item.id);
}
const allVisibleSelected = computed(() => pagedItems.value.length > 0 && pagedItems.value.every(i => selectedIds.value.includes(i.id)));
function toggleSelectAllVisible(checked) { pagedItems.value.forEach(item => toggleSelect(item, checked)); }

async function exportSelectedToExcel() {
  if(selectedIds.value.length === 0) return;
  exporting.value = true;
  try {
    const dataToExport = rawSchedules.value
        .filter(item => selectedIds.value.includes(item.id))
        .map((item, index) => ({
          "STT": index + 1,
          "Ngày làm": formatDate(item.ngayLamViec),
          "Thứ": getDayOfWeek(item.ngayLamViec),
          "Mã NV": item.maNhanVien,
          "Tên NV": item.tenNhanVien,
          "Ca": item.tenCa,
          "Giờ": `${formatTime(item.gioBatDau)} - ${formatTime(item.gioKetThuc)}`,
          "Ghi chú": item.ghiChu || ''
        }));

    const ws = XLSX.utils.json_to_sheet(dataToExport);
    ws["!cols"] = [{ wch: 6 }, { wch: 12 }, { wch: 8 }, { wch: 10 }, { wch: 20 }, { wch: 15 }, { wch: 15 }, { wch: 20 }];
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "LichNhanVien");
    XLSX.writeFile(wb, `LichNhanVien_${Date.now()}.xlsx`);
    toast.success("Xuất Excel thành công");
    cancelExportMode();
  } catch (e) { toast.error("Lỗi xuất Excel"); } finally { exporting.value = false; }
}

// Helpers
function setPage(p) { if(p >= 1 && p <= totalPages.value) page.page = p; }
const formatTime = (t) => t ? t.substring(0, 5) : '-';
const formatDate = (d) => { if(!d) return ''; const [y, m, day] = d.split('-'); return `${day}/${m}/${y}`; }
const getDayOfWeek = (d) => ['CN', 'Hai', 'Ba', 'Tư', 'Năm', 'Sáu', 'Bảy'][new Date(d).getDay()];
</script>

<style scoped>
/* Reuse Filter & Table Styles from ShiftTemplateList */
.filter-card { border-radius: 14px; overflow: hidden; border: 1px solid #e9ecef; }
.filter-header { background: #1f2a44; color: #fff; padding: 12px 16px; cursor: pointer; user-select: none; }
.filter-title { font-weight: 700; }
.filter-body { background: #f8fafc; }
.filter-card .form-control, .filter-card .form-select { border-radius: 10px; }

.table-wrap { border: 1px solid #dee2e6; border-radius: 12px; overflow: auto; background: #fff; text-align: center; }
.custom-table { width: 100%; min-width: 1000px; border-collapse: separate; border-spacing: 0; }
.custom-table th, .custom-table td { padding: 12px; border-bottom: 1px solid #e9ecef; vertical-align: middle; white-space: nowrap; }
.custom-table thead th { background: #1f2a44; color: #fff; font-weight: 700; }
.ellipsis { display: block; overflow: hidden; text-overflow: ellipsis; max-width: 100%; }
.empty { text-align: center; padding: 20px; color: #6c757d; }
.badge-muted { background: #f8f9fa; color: #212529; }

/* Modal Styles */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 9999; display: flex; align-items: center; justify-content: center; }
.modal-card { background: #fff; border-radius: 14px; padding: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.2); min-width: 400px; }
.modal-title { margin-bottom: 15px; font-size: 18px; font-weight: 700; }
.modal-actions { display: flex; gap: 10px; justify-content: flex-end; }
</style>