<template>
  <div class="container-fluid py-3">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-ticket-perforated fs-4"></i>
        <h5 class="mb-0">Quản lý phiếu giảm giá</h5>
      </div>

      <div class="d-flex align-items-center gap-2">
        <button class="btn btn-outline-primary btn-sm" @click="exportExcel">
          <i class="bi bi-file-earmark-excel me-1"></i> Xuất Excel (trang)
        </button>
        <button class="btn btn-outline-secondary btn-sm" @click="goCreate">
          <i class="bi bi-plus-lg me-1"></i> Thêm mới
        </button>
      </div>
    </div>

    <!-- Filters (GIỐNG HÓA ĐƠN: header collapse màu xanh) -->
    <div class="card shadow-sm mb-3 filter-card">
      <!-- Header (click để thu gọn/mở rộng) -->
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

      <!-- Body -->
      <div id="filterBody" class="collapse show">
        <div class="card-body filter-body">
          <div class="row g-3">
            <!-- keyword -->
            <div class="col-12 col-lg-6">
              <label class="form-label">Tìm kiếm</label>
              <input
                v-model.trim="filters.keyword"
                type="text"
                class="form-control"
                placeholder="Nhập mã / tên phiếu giảm giá..."
                @input="autoApplyFilters()"
                @keyup.enter="applyFilters"
              />
            </div>

            <!-- loai giam -->
            <div class="col-12 col-lg-3">
              <label class="form-label">Loại giảm</label>
              <select v-model="filters.loai" class="form-select" @change="applyFilters">
                <option value="">Tất cả</option>
                <option value="PERCENT">Giảm phần trăm</option>
                <option value="MONEY">Giảm tiền</option>
              </select>
            </div>

            <!-- biz status -->
            <div class="col-12 col-lg-3">
              <label class="form-label">Trạng thái</label>
              <select v-model="filters.bizStatus" class="form-select" @change="applyFilters">
                <option value="">Tất cả</option>
                <option value="UPCOMING">Sắp diễn ra</option>
                <option value="ACTIVE">Đang áp dụng</option>
                <option value="EXPIRED">Kết thúc</option>
              </select>
            </div>

            <!-- ✅ Loại phiếu (RADIO) -->
            <div class="col-12 col-lg-6">
              <label class="form-label">Loại phiếu</label>
              <div class="d-flex align-items-center gap-3 mt-2 flex-wrap">
                <div class="form-check">
                  <input
                    class="form-check-input"
                    type="radio"
                    id="lp_all"
                    value=""
                    v-model="filters.loaiPhieu"
                    @change="applyFilters"
                  />
                  <label class="form-check-label" for="lp_all">Tất cả</label>
                </div>

                <div class="form-check">
                  <input
                    class="form-check-input"
                    type="radio"
                    id="lp_public"
                    value="CONG_KHAI"
                    v-model="filters.loaiPhieu"
                    @change="applyFilters"
                  />
                  <label class="form-check-label" for="lp_public">Công khai</label>
                </div>

                <div class="form-check">
                  <input
                    class="form-check-input"
                    type="radio"
                    id="lp_personal"
                    value="CA_NHAN"
                    v-model="filters.loaiPhieu"
                    @change="applyFilters"
                  />
                  <label class="form-check-label" for="lp_personal">Cá nhân</label>
                </div>
              </div>
            </div>

            <!-- From date -->
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

            <!-- To date -->
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

            <!-- actions -->
            <div class="col-12 d-flex justify-content-end gap-2">
              <button class="btn btn-light" @click="resetFilters">
                <i class="bi bi-arrow-counterclockwise me-1"></i> Reset
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Table -->
    <div class="card shadow-sm">
      <div class="card-body">
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border" role="status"></div>
          <div class="mt-2 text-muted">Đang tải...</div>
        </div>

        <div v-else class="table-responsive table-wrap">
          <table class="table align-middle mb-0">
            <thead class="thead-dark-custom">
              <tr>
                <th style="width: 60px">STT</th>
                <th style="width: 160px">Mã giảm giá</th>
                <th>Tên giảm giá</th>
                <th style="width: 140px">Loại phiếu</th>
                <th style="width: 140px">Giá trị giảm</th>
                <th style="width: 110px">Số lượng</th>
                <th style="width: 170px">Ngày bắt đầu</th>
                <th style="width: 170px">Ngày kết thúc</th>
                <th style="width: 140px">Trạng thái</th>
                <th style="width: 160px" class="text-end">Hành động</th>
              </tr>
            </thead>

            <tbody>
              <tr v-if="pagedItems.length === 0">
                <td colspan="10" class="text-center text-muted py-4">Không có dữ liệu</td>
              </tr>

              <tr v-for="(v, idx) in pagedItems" :key="v.id">
                <td>{{ page.page * page.size + idx + 1 }}</td>

                <td class="fw-semibold">
                  <span class="ma-ellipsis" :title="v.maGiamGia">{{ v.maGiamGia }}</span>
                </td>

                <td class="fw-semibold">{{ v.tenGiamGia }}</td>

                <!-- ✅ MÀU LOẠI PHIẾU (pill như cũ) -->
                <td>
                  <span class="pill" :class="isPersonal(v) ? 'pill-personal' : 'pill-public'">
                    {{ isPersonal(v) ? "Cá nhân" : "Công khai" }}
                  </span>
                </td>

                <td class="fw-semibold">{{ renderGiaTriGiamRow(v) }}</td>
                <td>{{ v.soLuong ?? 0 }}</td>
                <td>{{ formatDate(v.ngayBatDau) }}</td>
                <td>{{ formatDate(v.ngayKetThuc) }}</td>

                <!-- ✅ TRẠNG THÁI CÓ MÀU -->
                <td>
                  <span class="badge" :class="bizBadgeClass(getBizStatusText(v))">
                    {{ getBizStatusText(v) }}
                  </span>
                </td>

                <td class="text-end">
                  <button class="btn btn-outline-primary btn-sm me-2" @click="openDetail(v.id)" title="Chi tiết">
                    <i class="bi bi-eye"></i>
                  </button>

                  <button
                    class="btn btn-outline-warning btn-sm me-2"
                    @click="goEdit(v.id)"
                    :disabled="isEditDisabled(v)"
                    :title="isEditDisabled(v) ? 'Không thể sửa khi đã tắt/kết thúc' : 'Sửa'"
                  >
                    <i class="bi bi-pencil-square"></i>
                  </button>

                  <!-- Switch (FE-only) -->
                  <label
                    class="switch"
                    :title="
                      isEnded(v)
                        ? 'Đã kết thúc'
                        : isActive(v)
                          ? 'Tắt phiếu (kết thúc ngay)'
                          : 'Bật để bắt đầu áp dụng ngay'
                    "
                  >
                    <input type="checkbox" :checked="isActive(v)" :disabled="isEnded(v)" @change="onToggleBiz(v, $event)" />
                    <span class="slider"></span>
                  </label>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- ✅ Pagination giống hóa đơn (Trang + prev/next + page size) -->
        <div class="d-flex align-items-center mt-3 flex-column flex-md-row gap-2" v-if="totalPages > 0">
          <!-- Left -->
          <div class="text-muted flex-grow-1">
            Hiển thị {{ pagedItems.length }} / tổng {{ totalElements }} bản ghi
          </div>

          <!-- Center -->
          <div class="d-flex align-items-center gap-2 justify-content-center flex-grow-1">
            <button class="btn btn-outline-secondary btn-sm" :disabled="page.page === 0" @click="setPage(page.page - 1)">
              <i class="bi bi-chevron-left"></i>
            </button>

            <div class="input-group input-group-sm" style="width: 110px">
              <span class="input-group-text">Trang</span>
              <input
                type="number"
                min="1"
                :max="totalPages || 1"
                class="form-control"
                v-model.number="pageInput"
                @keyup.enter="jumpPage"
              />
            </div>

            <button
              class="btn btn-outline-secondary btn-sm"
              :disabled="page.page >= totalPages - 1"
              @click="setPage(page.page + 1)"
            >
              <i class="bi bi-chevron-right"></i>
            </button>
          </div>

          <!-- Right -->
          <div class="d-flex justify-content-md-end flex-grow-1">
            <select class="form-select form-select-sm" style="width: 180px" v-model.number="page.size" @change="onChangeSize">
              <option :value="10">10 bản ghi / trang</option>
              <option :value="20">20 bản ghi / trang</option>
              <option :value="50">50 bản ghi / trang</option>
            </select>
          </div>
        </div>

        <div v-if="error" class="alert alert-danger mt-3 mb-0">
          {{ error }}
        </div>
      </div>
    </div>

    <!-- ✅ CONFIRM MODAL -->
    <div v-if="showConfirm" class="modal-overlay" @click.self="closeConfirm">
      <div class="modal-card">
        <h3 class="modal-title">Xác nhận</h3>
        <p class="modal-desc">{{ confirmText }}</p>

        <div class="modal-actions">
          <button class="btn btn-outline" :disabled="confirmLoading" @click="closeConfirm">Hủy</button>
          <button class="btn btn-primary" :disabled="confirmLoading" @click="confirmYes">
            {{ confirmLoading ? "Đang xử lý..." : "Đồng ý" }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, onBeforeUnmount, nextTick } from "vue";
import axios from "axios";
import { useRouter, useRoute } from "vue-router";
import * as XLSX from "xlsx";
import { useToast } from "@/composables/useToast"; 
/** ✅ flatpickr */
import flatpickr from "flatpickr";
import { Vietnamese } from "flatpickr/dist/l10n/vn.js";
import "flatpickr/dist/flatpickr.css";

const router = useRouter();
const route = useRoute();
const toast = useToast();
const API = "http://localhost:8080/api/pgg";
const getAllPhieuGiamGia = async () => (await axios.get(API)).data;

// ===== UI state =====
const loading = ref(false);
const error = ref("");
const items = ref([]);

// ===== Confirm modal =====
const showConfirm = ref(false);
const confirmText = ref("Bạn chắc chắn chứ?");
const confirmLoading = ref(false);
let pendingAction = null;

function openConfirm(text, action) {
  confirmText.value = text || "Bạn chắc chắn chứ?";
  pendingAction = typeof action === "function" ? action : null;
  showConfirm.value = true;
}
function closeConfirm() {
  if (confirmLoading.value) return;
  showConfirm.value = false;
  pendingAction = null;
}
async function confirmYes() {
  if (!pendingAction) {
    showConfirm.value = false;
    return;
  }
  confirmLoading.value = true;
  try {
    await pendingAction();
  } finally {
    confirmLoading.value = false;
    showConfirm.value = false;
    pendingAction = null;
  }
}

// ===== Filters =====
const filters = reactive({
  keyword: "",
  loai: "", // PERCENT | MONEY | ""
  bizStatus: "", // UPCOMING | ACTIVE | EXPIRED | ""
  loaiPhieu: "", // "" | "CONG_KHAI" | "CA_NHAN"
  from: "", // yyyy-MM-dd
  to: "", // yyyy-MM-dd
});

// ===== Pagination (GIỐNG HÓA ĐƠN) =====
const page = reactive({
  page: 0, // 0-based
  size: 10,
});
const pageInput = ref(1);

// ===== Query sync: GIỮ FILTER khi đi detail/sửa rồi quay lại =====
let applyingQuery = false;

function buildListQuery() {
  return {
    keyword: filters.keyword || "",
    loai: filters.loai || "",
    bizStatus: filters.bizStatus || "",
    loaiPhieu: filters.loaiPhieu || "",
    from: filters.from || "",
    to: filters.to || "",
    page: String(page.page || 0),
    size: String(page.size || 10),
  };
}

function syncQueryToUrl() {
  if (applyingQuery) return;
  router.replace({ query: buildListQuery() });
}

function restoreFromQuery(q = route.query) {
  applyingQuery = true;

  filters.keyword = String(q.keyword ?? "");
  filters.loai = String(q.loai ?? "");
  filters.bizStatus = String(q.bizStatus ?? "");
  filters.loaiPhieu = String(q.loaiPhieu ?? "");
  filters.from = String(q.from ?? "");
  filters.to = String(q.to ?? "");

  const p = Number(q.page);
  const s = Number(q.size);

  page.page = Number.isFinite(p) ? p : 0;
  page.size = Number.isFinite(s) ? s : 10;
  pageInput.value = page.page + 1;

  nextTick(() => {
    applyingQuery = false;
  });
}

// browser back/forward
watch(
  () => route.query,
  (q) => {
    if (applyingQuery) return;
    restoreFromQuery(q);
    nextTick(() => {
      fpFrom?.setDate(parseYMD(filters.from), false);
      fpTo?.setDate(parseYMD(filters.to), false);
    });
  }
);

// =======================
// Auto search (debounce)
// =======================
let autoTimer = null;
function autoApplyFilters(delay = 450) {
  clearTimeout(autoTimer);
  autoTimer = setTimeout(() => {
    applyFilters();
  }, delay);
}

function applyFilters() {
  page.page = 0;
  pageInput.value = 1;
  syncQueryToUrl();
}

// đổi size giống hóa đơn: reset về trang 1
function onChangeSize() {
  page.page = 0;
  pageInput.value = 1;
  syncQueryToUrl();
}

// ===== Navigation: luôn mang query hiện tại =====
function goCreate() {
  router.push({ path: "/vouchers/create", query: buildListQuery() });
}
function goEdit(id) {
  router.push({ path: `/vouchers/update/${id}`, query: buildListQuery() });
}
function openDetail(id) {
  router.push({ path: `/vouchers/${id}`, query: buildListQuery() });
}

// ===== Date helpers =====
function toDate(v) {
  if (!v) return null;
  const d = new Date(String(v));
  return Number.isNaN(d.getTime()) ? null : d;
}

function formatDate(v) {
  const d = toDate(v);
  if (!d) return "-";
  const dd = String(d.getDate()).padStart(2, "0");
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const yyyy = d.getFullYear();
  const hh = String(d.getHours()).padStart(2, "0");
  const mi = String(d.getMinutes()).padStart(2, "0");
  return `${dd}/${mm}/${yyyy} ${hh}:${mi}`;
}

function dateFromYMD(ymd, endOfDay = false) {
  if (!ymd) return null;
  const d = new Date(`${ymd}T00:00:00`);
  if (endOfDay) d.setHours(23, 59, 59, 999);
  return d;
}

// ===== Biz status =====
function getBizStatusText(v) {
  const start = toDate(v.ngayBatDau);
  const end = toDate(v.ngayKetThuc);
  const now = new Date();

  if (start && now < start) return "Sắp diễn ra";
  if (end && now > end) return "Kết thúc";
  return "Đang áp dụng";
}
function isEnded(v) {
  return getBizStatusText(v) === "Kết thúc";
}
function isActive(v) {
  return getBizStatusText(v) === "Đang áp dụng";
}
function isUpcoming(v) {
  return getBizStatusText(v) === "Sắp diễn ra";
}

// ✅ Badge màu
function bizBadgeClass(text) {
  if (text === "Đang áp dụng") return "badge-success";
  if (text === "Sắp diễn ra") return "badge-warning";
  if (text === "Kết thúc") return "badge-muted";
  return "badge-muted";
}

function isEditDisabled(v) {
  return v.trangThai === false || isEnded(v);
}

function isPersonal(v) {
  const lp = v?.loaiPhieu;
  if (lp === true) return true;
  if (lp === false) return false;
  return String(lp || "").toUpperCase() === "CA_NHAN";
}

// ===== normalize BE fields =====
function normalizeRow(x) {
  return {
    ...x,
    id: x.id,

    loaiPhieu: x.loaiPhieu ?? x.loai_phieu ?? "CONG_KHAI",
    ngayBatDau: x.ngayBatDau ?? x.ngay_bat_dau ?? null,
    ngayKetThuc: x.ngayKetThuc ?? x.ngay_ket_thuc ?? null,
    ngayTao: x.ngayTao ?? x.ngay_tao ?? null,

    maGiamGia: x.maGiamGia ?? x.ma_giam_gia ?? x.ma ?? null,
    tenGiamGia: x.tenGiamGia ?? x.ten_giam_gia ?? null,

    trangThai: x.trangThai ?? x.trang_thai ?? true,

    loaiGiam: x.loaiGiam ?? x.loai_giam ?? true,
    giaTriPhanTram: x.giaTriPhanTram ?? x.gia_tri_phan_tram ?? null,
    giaTriTienMat: x.giaTriTienMat ?? x.gia_tri_tien_mat ?? null,
    soLuong: x.soLuong ?? x.so_luong ?? 0,
  };
}

// ===== Filtering FE =====
const filteredItems = computed(() => {
  const kw = String(filters.keyword || "").trim().toLowerCase();
  const loai = String(filters.loai || "");
  const biz = String(filters.bizStatus || "");
  const lp = String(filters.loaiPhieu || "").toUpperCase();
  const from = String(filters.from || "");
  const to = String(filters.to || "");
  const now = new Date();

  return (items.value || [])
    .filter((v) => v.trangThai === true)

    .filter((v) => {
      if (!kw) return true;
      const ma = String(v.maGiamGia ?? "").toLowerCase();
      const ten = String(v.tenGiamGia ?? "").toLowerCase();
      return ma.includes(kw) || ten.includes(kw);
    })

    .filter((v) => {
      if (!loai) return true;
      if (loai === "PERCENT") return v.loaiGiam === true;
      if (loai === "MONEY") return v.loaiGiam === false;
      return true;
    })

    // ✅ lọc loại phiếu theo radio
    .filter((v) => {
      if (!lp) return true;

      const isCaNhan = v?.loaiPhieu === true || String(v?.loaiPhieu || "").toUpperCase() === "CA_NHAN";
      const isCongKhai = v?.loaiPhieu === false || String(v?.loaiPhieu || "").toUpperCase() === "CONG_KHAI";

      if (lp === "CA_NHAN") return isCaNhan;
      if (lp === "CONG_KHAI") return isCongKhai;
      return true;
    })

    .filter((v) => {
      const fromD = dateFromYMD(from, false);
      const toD = dateFromYMD(to, true);
      if (!fromD && !toD) return true;

      const start = toDate(v.ngayBatDau);
      const end = toDate(v.ngayKetThuc);

      if (fromD && !start) return false;
      if (toD && !end) return false;

      if (fromD && start < fromD) return false;
      if (toD && end > toD) return false;

      return true;
    })

    .filter((v) => {
      if (!biz) return true;
      const start = toDate(v.ngayBatDau);
      const end = toDate(v.ngayKetThuc);

      if (biz === "UPCOMING") return start && now < start;
      if (biz === "EXPIRED") return end && now > end;
      return (!start || now >= start) && (!end || now <= end);
    });
});

const sortedItems = computed(() => {
  return [...filteredItems.value].sort((a, b) => {
    const ta = String(a.ngayTao ?? "");
    const tb = String(b.ngayTao ?? "");
    const byTime = tb.localeCompare(ta);
    if (byTime !== 0) return byTime;
    return Number(b.id ?? 0) - Number(a.id ?? 0);
  });
});

const totalElements = computed(() => filteredItems.value.length);
const totalPages = computed(() => Math.ceil(totalElements.value / page.size));

const pagedItems = computed(() => {
  const start = page.page * page.size;
  return sortedItems.value.slice(start, start + page.size);
});

// ===== Pagination actions =====
function setPage(p) {
  if (p < 0) return;
  if (totalPages.value && p > totalPages.value - 1) return;
  page.page = p;
  pageInput.value = page.page + 1;
  syncQueryToUrl();
}

function jumpPage() {
  const max = Math.max(1, totalPages.value || 1);
  const target = Math.min(Math.max(1, pageInput.value || 1), max);
  page.page = target - 1;
  pageInput.value = target;
  syncQueryToUrl();
}

// ===== Money render =====
function formatMoney(v) {
  const n = Number(v);
  if (Number.isNaN(n)) return String(v ?? "-");
  return n.toLocaleString("vi-VN") + " ₫";
}

function renderGiaTriGiamRow(v) {
  if (v?.loaiGiam === true) {
    const pct = Number(v?.giaTriPhanTram ?? 0);
    return `${pct}%`;
  }
  const money = Number(v?.giaTriTienMat ?? 0);
  return formatMoney(money);
}

// ===== Switch FE-only =====
function patchFieldLocal(v, patch) {
  const idx = (items.value || []).findIndex((x) => x.id === v.id);
  if (idx !== -1) items.value[idx] = { ...items.value[idx], ...patch };
  Object.assign(v, patch);
}
function startNowLocal(v) {
  const nowIso = new Date().toISOString();
  patchFieldLocal(v, { ngayBatDau: nowIso });
}
function endNowLocal(v) {
  const nowIso = new Date().toISOString();
  patchFieldLocal(v, { ngayKetThuc: nowIso });
}
async function onToggleBiz(v, evt) {
  const checked = evt?.target?.checked === true;

  const wasActive = isActive(v);
  const wasUpcoming = isUpcoming(v);

  const label = `${v?.maGiamGia || ""}${v?.tenGiamGia ? " - " + v.tenGiamGia : ""}`;

  // gạt BẬT
  if (checked) {
    if (wasUpcoming) {
      evt.target.checked = false;

      openConfirm("Bạn có chắc muốn BẮT ĐẦU áp dụng phiếu này ngay không?", async () => {
        startNowLocal(v); 
        toast.success(`✅ Đã bắt đầu áp dụng mã phiếu giảm giá: ${label}`);
      });
      return;
    }

    evt.target.checked = true;
    toast.success(`✅ Đã mở mã phiếu giảm giá:  ${label}`);
    return;
  }

  // gạt TẮT
  if (!checked) {
    if (wasActive) {
      evt.target.checked = true;

      openConfirm("Bạn có chắc muốn KẾT THÚC phiếu giảm giá này ngay?", async () => {
        endNowLocal(v);
        toast.info(`✅ Đã kết thúc mã giảm giá:  ${label}`);
      });
      return;
    }

    evt.target.checked = false;
    toast.info(`✅ Đã tắt ${label}`);
  }
}

// ===== Flatpickr =====
const fromPickerRef = ref(null);
const toPickerRef = ref(null);
let fpFrom = null;
let fpTo = null;

function parseYMD(ymd) {
  if (!ymd) return null;
  const [y, m, d] = String(ymd).split("-").map(Number);
  if (!y || !m || !d) return null;
  return new Date(y, m - 1, d);
}

function initPickers() {
  if (fromPickerRef.value && !fpFrom) {
    fpFrom = flatpickr(fromPickerRef.value, {
      locale: Vietnamese,
      dateFormat: "d/m/Y",
      allowInput: true,
      defaultDate: parseYMD(filters.from),
      onChange: (selectedDates) => {
        const d = selectedDates?.[0] || null;
        filters.from = d ? flatpickr.formatDate(d, "Y-m-d") : "";
        applyFilters();
      },
    });
  }

  if (toPickerRef.value && !fpTo) {
    fpTo = flatpickr(toPickerRef.value, {
      locale: Vietnamese,
      dateFormat: "d/m/Y",
      allowInput: true,
      defaultDate: parseYMD(filters.to),
      onChange: (selectedDates) => {
        const d = selectedDates?.[0] || null;
        filters.to = d ? flatpickr.formatDate(d, "Y-m-d") : "";
        applyFilters();
      },
    });
  }
}

function openFromPicker() {
  fpFrom?.open();
}
function openToPicker() {
  fpTo?.open();
}
function clearFromDate() {
  filters.from = "";
  fpFrom?.clear();
  applyFilters();
}
function clearToDate() {
  filters.to = "";
  fpTo?.clear();
  applyFilters();
}

// khóa range giống hóa đơn
watch(
  () => filters.from,
  (v) => {
    if (fpTo) fpTo.set("minDate", v ? parseYMD(v) : null);
  }
);
watch(
  () => filters.to,
  (v) => {
    if (fpFrom) fpFrom.set("maxDate", v ? parseYMD(v) : null);
  }
);

// ===== Reset =====
function resetFilters() {
  filters.keyword = "";
  filters.loai = "";
  filters.bizStatus = "";
  filters.loaiPhieu = "";
  filters.from = "";
  filters.to = "";

  fpFrom?.clear();
  fpTo?.clear();
  if (fpTo) fpTo.set("minDate", null);
  if (fpFrom) fpFrom.set("maxDate", null);

  page.page = 0;
  page.size = 10;
  pageInput.value = 1;
  syncQueryToUrl();
}

// ===== Load data =====
async function reload() {
  loading.value = true;
  error.value = "";
  try {
    const data = await getAllPhieuGiamGia();
    items.value = (Array.isArray(data) ? data : []).map(normalizeRow);
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || "Không tải được dữ liệu";
  } finally {
    loading.value = false;
  }
}

// ===== Export Excel (XLSX) - TRANG HIỆN TẠI =====
function exportExcel() {
  const data = pagedItems.value.map((v, idx) => ({
    "#": page.page * page.size + idx + 1,
    "Mã giảm giá": v.maGiamGia ?? "",
    "Tên giảm giá": v.tenGiamGia ?? "",
    "Loại phiếu": isPersonal(v) ? "Cá nhân" : "Công khai",
    "Loại giảm": v.loaiGiam ? "Giảm %" : "Giảm tiền",
    "Giá trị giảm": v.loaiGiam ? `${Number(v.giaTriPhanTram ?? 0)}%` : Number(v.giaTriTienMat ?? 0),
    "Số lượng": Number(v.soLuong ?? 0),
    "Ngày bắt đầu": formatDate(v.ngayBatDau),
    "Ngày kết thúc": formatDate(v.ngayKetThuc),
    "Trạng thái": getBizStatusText(v),
  }));

  const ws = XLSX.utils.json_to_sheet(data);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, "Vouchers");
  XLSX.writeFile(wb, `phieu_giam_gia_trang_${page.page + 1}.xlsx`);
}

// ===== mount =====
onMounted(async () => {
  restoreFromQuery(route.query);

  await reload();

  await nextTick();
  initPickers();

  fpFrom?.setDate(parseYMD(filters.from), false);
  fpTo?.setDate(parseYMD(filters.to), false);

  syncQueryToUrl();
});

onBeforeUnmount(() => {
  try {
    fpFrom?.destroy();
  } catch {}
  try {
    fpTo?.destroy();
  } catch {}
});
</script>

<style scoped>
.card {
  border-radius: 14px;
}

.table thead th {
  font-weight: 600;
}

/* ✅ BẢNG “BÌNH THƯỜNG”: bỏ bo tròn của khung bảng */
.table-wrap {
  border: 1px solid #dee2e6;
  border-radius: 0; /* <-- bỏ tròn */
  overflow: auto; /* vẫn scroll ngang nếu cần */
}

.thead-dark-custom th {
  background-color: #1f2a44 !important;
  color: #fff !important;
  border-color: rgba(255, 255, 255, 0.15) !important;
}

.table td,
.table th {
  border-color: #e9ecef;
}

/* ===== Filter giống hóa đơn ===== */
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
  line-height: 1;
  transition: transform 0.2s ease;
}

/* Icon xoay khi collapse đóng */
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

.ma-ellipsis {
  display: inline-block;
  max-width: 160px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: bottom;
}

/* ✅ Pill LOẠI PHIẾU (màu như cũ) */
.pill {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}
.pill-public {
  background: #dcfce7;
  color: #166534;
}
.pill-personal {
  background: #fef3c7;
  color: #92400e;
}

/* ✅ Badge màu trạng thái */
.badge-success {
  background: #dcfce7;
  color: #166534;
}
.badge-warning {
  background: #fef3c7;
  color: #92400e;
}
.badge-muted {
  background: #e5e7eb;
  color: #374151;
}

/* ✅ Modal confirm */
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
  width: min(420px, calc(100% - 32px));
  background: #fff;
  border-radius: 14px;
  padding: 18px 18px 14px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}
.modal-title {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 700;
}
.modal-desc {
  margin: 0 0 14px;
  color: #555;
  line-height: 1.4;
}
.modal-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

/* ===== Switch ===== */
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
.switch input:disabled + .slider {
  opacity: 0.55;
  cursor: not-allowed;
}
</style>
