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
          <button class="btn btn-success btn-sm" :disabled="selectedIds.length === 0 || exporting" @click="exportSelectedToExcel">
            <i class="bi bi-download me-1"></i> Tải xuống ({{ selectedIds.length }})
          </button>
          <button class="btn btn-outline-secondary btn-sm" @click="cancelExportMode">Hủy</button>
        </template>
      </div>
    </div>

    <div class="card shadow-sm mb-3 filter-card">
      <div class="filter-header d-flex align-items-center justify-content-between" data-bs-toggle="collapse" data-bs-target="#myFilterBody">
        <div class="d-flex align-items-center gap-2">
          <span class="filter-icon">▼</span>
          <span class="filter-title">Bộ lọc</span>
        </div>
      </div>
      <div id="myFilterBody" class="collapse show">
        <div class="card-body filter-body">
          <div class="row g-3">
            <div class="col-12 col-md-6">
              <label class="form-label">Tìm kiếm tên ca</label>
              <input v-model="filters.keyword" type="text" class="form-control" placeholder="Nhập tên ca..." @input="applyFilters">
            </div>
            <div class="col-12 col-md-6">
              <label class="form-label">Thời gian</label>
              <div class="input-group">
                <input type="date" class="form-control" v-model="filters.from" @change="loadMySchedule">
                <span class="input-group-text bg-white">-</span>
                <input type="date" class="form-control" v-model="filters.to" @change="loadMySchedule">
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="card shadow-sm">
      <div class="card-body">
        <div class="table-wrap">
          <table class="custom-table">
            <colgroup>
              <col v-if="exportMode" style="width: 40px" />
              <col style="width: 50px" />
              <col style="width: 150px" />
              <col style="width: 150px" />
              <col style="width: 150px" />
              <col style="width: 150px" />
              <col style="width: 200px" />
            </colgroup>
            <thead>
            <tr>
              <th v-if="exportMode" class="text-center">
                <input type="checkbox" :checked="allVisibleSelected" @change="toggleSelectAllVisible($event.target.checked)" />
              </th>
              <th class="text-center">STT</th>
              <th>Ngày</th>
              <th>Tên Ca</th>
              <th>Thời gian</th>
              <th class="text-center">Trạng thái</th>
              <th>Ghi chú</th>
            </tr>
            </thead>
            <tbody>
            <tr v-if="pagedItems.length === 0"><td :colspan="exportMode ? 7 : 6" class="empty">Không có lịch làm việc</td></tr>
            <tr v-for="(item, index) in pagedItems" :key="item.id" :class="getRowClass(item)">
              <td v-if="exportMode" class="text-center">
                <input type="checkbox" :checked="isSelected(item.id)" @change="toggleSelect(item, $event.target.checked)" />
              </td>
              <td class="text-center">{{ (page.page - 1) * page.size + index + 1 }}</td>
              <td>
                <span class="fw-bold">{{ formatDate(item.ngayLamViec) }}</span>
                <span class="small text-muted ms-2">({{ getDayOfWeek(item.ngayLamViec) }})</span>
              </td>
              <td class="fw-bold text-primary">{{ item.tenCa }}</td>
              <td>{{ formatTime(item.gioBatDau) }} - {{ formatTime(item.gioKetThuc) }}</td>
              <td class="text-center">
                  <span class="badge" :class="getStatusBadge(item.ngayLamViec)">
                    {{ getStatusText(item.ngayLamViec) }}
                  </span>
              </td>
              <td class="small text-muted">{{ item.ghiChu || '-' }}</td>
            </tr>
            </tbody>
          </table>
        </div>

        <div class="d-flex align-items-center mt-3 justify-content-between" v-if="totalElements > 0">
          <small class="text-muted">Tổng {{ totalElements }} bản ghi</small>
          <nav>
            <ul class="pagination pagination-sm mb-0">
              <li class="page-item" :class="{ disabled: page.page === 1 }"><button class="page-link" @click="setPage(page.page - 1)">Trước</button></li>
              <li class="page-item active"><span class="page-link">{{ page.page }}</span></li>
              <li class="page-item" :class="{ disabled: page.page >= totalPages }"><button class="page-link" @click="setPage(page.page + 1)">Sau</button></li>
            </ul>
          </nav>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import shiftApi from '@/services/shiftApi';
import { useAuthStore } from '@/stores/auth';
import * as XLSX from "xlsx";
import { useToast } from "@/composables/useToast";

const authStore = useAuthStore();
const toast = useToast();

const rawSchedules = ref([]);
const today = new Date();
const filters = reactive({
  keyword: "",
  from: new Date(today.getFullYear(), today.getMonth(), 1).toISOString().split('T')[0],
  to: new Date(today.getFullYear(), today.getMonth() + 1, 0).toISOString().split('T')[0]
});
const page = reactive({ page: 1, size: 20 });

// Export State
const exportMode = ref(false);
const exporting = ref(false);
const selectedIds = ref([]);

onMounted(() => {
  const myId = authStore.userId || authStore.user?.id;
  if(myId) loadMySchedule(myId);
});

async function loadMySchedule(id) {
  try {
    const res = await shiftApi.getMySchedule(id, filters.from, filters.to);
    const data = res.data;
    if (Array.isArray(data)) rawSchedules.value = data;
    else if (data && Array.isArray(data.data)) rawSchedules.value = data.data;
    else rawSchedules.value = [];
  } catch (e) { console.error(e); }
}

const filteredItems = computed(() => {
  let items = rawSchedules.value || [];
  if (filters.keyword) {
    items = items.filter(i => i.tenCa.toLowerCase().includes(filters.keyword.toLowerCase()));
  }
  // Sort date ASC
  return items.sort((a,b) => new Date(a.ngayLamViec) - new Date(b.ngayLamViec));
});

const totalElements = computed(() => filteredItems.value.length);
const totalPages = computed(() => Math.ceil(totalElements.value / page.size));
const pagedItems = computed(() => filteredItems.value.slice((page.page-1)*page.size, page.page*page.size));

// Helper Display
const formatDate = (d) => d ? `${d.split('-')[2]}/${d.split('-')[1]}/${d.split('-')[0]}` : '';
const formatTime = (t) => t ? t.substring(0, 5) : '';
const getDayOfWeek = (d) => ['CN', 'Hai', 'Ba', 'Tư', 'Năm', 'Sáu', 'Bảy'][new Date(d).getDay()];

function getStatusText(dateStr) {
  const d = new Date(dateStr).setHours(0,0,0,0);
  const now = new Date().setHours(0,0,0,0);
  if (d < now) return 'Đã xong';
  if (d === now) return 'Hôm nay';
  return 'Sắp tới';
}

function getStatusBadge(dateStr) {
  const text = getStatusText(dateStr);
  if (text === 'Hôm nay') return 'bg-success';
  if (text === 'Sắp tới') return 'bg-primary';
  return 'bg-secondary';
}

function getRowClass(item) {
  return getStatusText(item.ngayLamViec) === 'Hôm nay' ? 'bg-primary bg-opacity-10' : '';
}

function setPage(p) { if(p >= 1 && p <= totalPages.value) page.page = p; }
function applyFilters() { page.page = 1; }

// Excel
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
    const dataToExport = rawSchedules.value.filter(i => selectedIds.value.includes(i.id)).map((item, idx) => ({
      "STT": idx + 1,
      "Ngày": formatDate(item.ngayLamViec),
      "Thứ": getDayOfWeek(item.ngayLamViec),
      "Ca": item.tenCa,
      "Giờ": `${formatTime(item.gioBatDau)} - ${formatTime(item.gioKetThuc)}`,
      "Trạng thái": getStatusText(item.ngayLamViec),
      "Ghi chú": item.ghiChu
    }));
    const ws = XLSX.utils.json_to_sheet(dataToExport);
    ws["!cols"] = [{wch: 5}, {wch: 12}, {wch: 8}, {wch: 15}, {wch: 15}, {wch: 12}, {wch: 20}];
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "LichCuaToi");
    XLSX.writeFile(wb, `MySchedule_${Date.now()}.xlsx`);
    toast.success("Tải xuống thành công");
    cancelExportMode();
  } catch(e) { toast.error("Lỗi tải xuống"); } finally { exporting.value = false; }
}

</script>

<style scoped>
/* Reuse Shared Styles */
.filter-card { border-radius: 14px; overflow: hidden; border: 1px solid #e9ecef; }
.filter-header { background: #1f2a44; color: #fff; padding: 12px 16px; cursor: pointer; }
.filter-title { font-weight: 700; }
.filter-body { background: #f8fafc; }
.filter-card .form-control { border-radius: 10px; }

.table-wrap { border: 1px solid #dee2e6; border-radius: 12px; overflow: auto; background: #fff; text-align: center; }
.custom-table { width: 100%; min-width: 800px; border-collapse: separate; border-spacing: 0; }
.custom-table th, .custom-table td { padding: 12px; border-bottom: 1px solid #e9ecef; vertical-align: middle; white-space: nowrap; }
.custom-table thead th { background: #1f2a44; color: #fff; font-weight: 700; }
.empty { text-align: center; padding: 20px; color: #6c757d; }
.bg-opacity-10 { background-color: rgba(13, 110, 253, 0.05) !important; }
</style>