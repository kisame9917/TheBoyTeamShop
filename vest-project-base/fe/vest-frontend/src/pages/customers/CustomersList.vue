<template>
  <div class="container-fluid py-3">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-people fs-4"></i>
        <h5 class="mb-0">Danh sách khách hàng</h5>
      </div>

      <button class="btn btn-primary btn-sm text-white" type="button" @click="goCreate" title="Thêm mới">
        <i class="bi bi-plus-lg me-1"></i> Thêm mới
      </button>
    </div>

    <!-- ✅ Filters (UI giống voucher, không cần Bootstrap JS) -->
    <div class="card shadow-sm mb-3 filter-card">
      <div
          class="filter-header d-flex align-items-center justify-content-between"
          data-bs-toggle="collapse"
          data-bs-target="#customerFilterBody"
          role="button"
          :aria-expanded="filterOpen ? 'true' : 'false'"
          aria-controls="customerFilterBody"
          @click="toggleFilter"
      >
        <div class="d-flex align-items-center gap-2">
          <span class="filter-icon">▼</span>
          <span class="filter-title">Bộ lọc tìm kiếm</span>
        </div>

        <small class="filter-hint">Nhấn để thu gọn/mở rộng</small>
      </div>

      <div id="customerFilterBody" class="collapse show">
        <div class="card-body filter-body">
          <div class="row g-3">
            <div class="col-12 col-lg-6">
              <label class="form-label">Tìm kiếm</label>
              <input
                  v-model.trim="filters.keyword"
                  type="text"
                  class="form-control"
                  placeholder="Tìm theo mã, tên, email, SĐT, tài khoản..."
              />
            </div>

            <!-- email -->
            <div class="col-12 col-lg-3">
              <label class="form-label">Email</label>
              <input v-model.trim="filters.email" type="text" class="form-control" placeholder="Email" />
            </div>

            <!-- phone -->
            <div class="col-12 col-lg-3">
              <label class="form-label">SĐT</label>
              <input v-model.trim="filters.phone" type="text" class="form-control" placeholder="SĐT" />
            </div>

            <!-- ma kh -->
            <div class="col-12 col-lg-3">
              <label class="form-label">Mã khách hàng</label>
              <input v-model.trim="filters.maKhachHang" type="text" class="form-control" placeholder="Mã KH" />
            </div>

            <!-- status -->
            <div class="col-12 col-lg-6">
              <label class="form-label">Trạng thái</label>
              <div class="d-flex align-items-center gap-3 mt-2">
                <div class="form-check">
                  <input class="form-check-input" type="radio" id="cst_all" value="all" v-model="filters.statusMode" />
                  <label class="form-check-label" for="cst_all">Tất cả</label>
                </div>
                <div class="form-check">
                  <input class="form-check-input" type="radio" id="cst_on" value="active" v-model="filters.statusMode" />
                  <label class="form-check-label" for="cst_on">Hoạt động</label>
                </div>
                <div class="form-check">
                  <input class="form-check-input" type="radio" id="cst_off" value="inactive" v-model="filters.statusMode" />
                  <label class="form-check-label" for="cst_off">Không hoạt động</label>
                </div>
              </div>
            </div>

            <!-- ✅ bỏ nút Lọc, chỉ còn Reset -->
            <div class="col-12 d-flex justify-content-end gap-2">
              <button class="btn btn-light" type="button" @click="resetFilters">
                <i class="bi bi-arrow-counterclockwise me-1"></i> Đặt lại
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

        <div v-else class="table-responsive">
          <div class="table-scroll">
            <table class="table align-middle table-hover">
              <thead class="table-head-dark">
              <tr>
                <th style="width: 60px">#</th>
                <th style="width: 80px">Ảnh</th>
                <th style="width: 110px">Mã KH</th>
                <th style="width: 220px">Họ tên</th>
                <th style="width: 260px">Email</th>
                <th style="width: 140px">SĐT</th>
                <th class="col-address">Địa chỉ</th>
                <th style="width: 140px" class="text-center">Trạng thái</th>
                <th style="width: 170px" class="text-center">Hành động</th>
              </tr>
              </thead>

              <tbody class="table-body-normal">
              <tr v-if="paged.length === 0">
                <td colspan="9" class="text-center text-muted py-4">Không có dữ liệu</td>
              </tr>

              <tr v-for="(c, idx) in paged" :key="c.id">
                <td>{{ page.page * page.size + idx + 1 }}</td>

                <td>
                  <div class="d-flex align-items-center">
                    <img
                        v-if="resolveAvatar(c)"
                        :src="resolveAvatar(c)"
                        class="rounded-circle border"
                        style="width: 40px; height: 40px; object-fit: cover"
                        alt="avatar"
                        @error="onAvatarError($event, c)"
                    />
                    <div
                        v-else
                        class="rounded-circle border d-flex align-items-center justify-content-center"
                        style="width: 40px; height: 40px; background: #eef2ff; color: #1d4ed8; font-weight: 700"
                    >
                      {{ getInitials(c.tenKhachHang) }}
                    </div>
                  </div>
                </td>

                <td class="fw-semibold">{{ c.maKhachHang || "-" }}</td>
                <td>{{ c.tenKhachHang || "-" }}</td>

                <td class="text-truncate" style="max-width: 260px" :title="c.email || ''">
                  {{ c.email || "-" }}
                </td>

                <td>{{ c.soDienThoai || "-" }}</td>

                <td class="addr-cell">
                  <div class="addr-full">{{ c.diaChi || "-" }}</div>
                </td>

                <td class="text-center">
                    <span class="badge fw-normal" :class="c.trangThai ? 'badge-working' : 'badge-off'">
                      {{ c.trangThai ? "Hoạt động" : "Không hoạt động" }}
                    </span>
                </td>

                <td class="text-end">
                  <div class="d-inline-flex align-items-center gap-2">
                    <button class="btn btn-outline-primary btn-sm" type="button" title="Xem chi tiết" @click="goDetail(c.id)">
                      <i class="bi bi-eye"></i>
                    </button>
                    <button class="btn btn-outline-warning btn-sm me-2" type="button" title="Sửa" @click="goEdit(c.id)">
                      <i class="bi bi-pencil-square"></i>
                    </button>
                    <div class="form-check form-switch m-0" title="Đổi trạng thái">
                      <input
                          class="form-check-input"
                          type="checkbox"
                          role="switch"
                          :checked="!!c.trangThai"
                          @change="onSwitchAttempt($event, c)"
                      />
                    </div>
                  </div>
                </td>
              </tr>
              </tbody>
            </table>

            <div class="row align-items-center mt-3 g-2">
              <div class="col-12 col-lg-4 text-muted">
                Hiển thị {{ paged.length }} / tổng {{ filtered.length }} bản ghi
              </div>

              <div class="col-12 col-lg-4 d-flex justify-content-center">
                <div class="d-flex align-items-center gap-2">
                  <button class="btn btn-outline-secondary btn-sm" :disabled="page.page === 0" @click="setPage(page.page - 1)">
                    <i class="bi bi-chevron-left"></i>
                  </button>

                  <div class="input-group input-group-sm" style="width: 140px">
                    <span class="input-group-text">Trang</span>
                    <input
                        type="number"
                        min="1"
                        :max="page.totalPages || 1"
                        class="form-control text-center"
                        v-model.number="pageInput"
                        @keyup.enter="jumpPage"
                    />
                  </div>

                  <button
                      class="btn btn-outline-secondary btn-sm"
                      :disabled="page.page >= page.totalPages - 1"
                      @click="setPage(page.page + 1)"
                  >
                    <i class="bi bi-chevron-right"></i>
                  </button>
                </div>
              </div>

              <div class="col-12 col-lg-4 d-flex justify-content-lg-end">
                <select class="form-select form-select-sm" style="width: 160px" v-model.number="page.size" @change="recalcPaging">
                  <option :value="10">10 bản ghi / trang</option>
                  <option :value="20">20 bản ghi / trang</option>
                  <option :value="50">50 bản ghi / trang</option>
                </select>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ✅ Confirm popup (custom overlay) -->
    <div v-if="confirmOpen" class="modal-overlay" @click.self="closeConfirm()">
      <div class="modal-card confirm-modal">
        <h3 class="modal-title">Xác nhận</h3>

        <p class="modal-desc" v-if="confirmState.customer">
          Bạn muốn đổi trạng thái khách hàng
          <b>{{ confirmState.customer.maKhachHang }}</b>
          từ
          <span class="badge fw-normal" :class="confirmState.customer.trangThai ? 'badge-working' : 'badge-off'">
            {{ confirmState.customer.trangThai ? "Hoạt động" : "Không hoạt động" }}
          </span>
          sang
          <span class="badge fw-normal" :class="confirmState.next ? 'badge-working' : 'badge-off'">
            {{ confirmState.next ? "Hoạt động" : "Không hoạt động" }}
          </span>
          ?
        </p>

        <div class="modal-actions">
          <button class="btn btn-outline" type="button" @click="closeConfirm()" :disabled="confirmState.loading">Hủy</button>
          <button class="btn btn-confirm" type="button" @click="confirmChangeStatus" :disabled="confirmState.loading">
            {{ confirmState.loading ? "Đang xử lý..." : "Đồng ý" }}
          </button>
        </div>
      </div>
    </div>

    <!-- Toast -->
    <div class="toast-container position-fixed top-0 end-0 p-3" style="z-index: 9999">
      <div
          v-for="t in toast.state.items"
          :key="t.id"
          class="toast show align-items-center border-0 mb-2"
          :class="toastClass(t.type)"
          role="alert"
          aria-live="assertive"
          aria-atomic="true"
      >
        <div class="d-flex">
          <div class="toast-body">
            <div v-if="t.title" class="fw-semibold mb-1">{{ t.title }}</div>
            <div>{{ t.message }}</div>
          </div>
          <button type="button" class="btn-close btn-close-white me-2 m-auto" @click="toast.remove(t.id)"></button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch, computed } from "vue";
import { useRouter } from "vue-router";
import http from "../../services/http";
import { useToast } from "@/composables/useToast";

const router = useRouter();
const toast = useToast();

const loading = ref(false);
const list = ref([]);

const filterOpen = ref(true);
function toggleFilter() {
  filterOpen.value = !filterOpen.value;
}

/** ✅ Không dùng filtersDraft nữa */
const filters = reactive({
  keyword: "",
  email: "",
  phone: "",
  maKhachHang: "",
  taiKhoan: "",
  statusMode: "all", // all | active | inactive
});

const page = reactive({
  page: 0,
  size: 10,
  totalPages: 1,
});
const pageInput = ref(1);

function unwrapList(data) {
  if (!data) return [];
  if (Array.isArray(data)) return data;
  if (Array.isArray(data.result)) return data.result;
  if (Array.isArray(data.content)) return data.content;
  return [];
}

function normalizeCustomer(x) {
  x = x || {};
  return {
    id: x.id,
    maKhachHang: x.maKhachHang ?? x.ma_khach_hang ?? "",
    tenKhachHang: x.tenKhachHang ?? x.ten_khach_hang ?? "",
    gioiTinh: x.gioiTinh ?? x.gioi_tinh ?? null,
    email: x.email ?? "",
    soDienThoai: x.soDienThoai ?? x.so_dien_thoai ?? "",
    taiKhoan: x.taiKhoan ?? x.tai_khoan ?? "",
    trangThai: x.trangThai ?? x.trang_thai ?? true,
    anhDaiDien: x.anhDaiDien ?? x.anh_dai_dien ?? "",
    diaChi: x.diaChi ?? "",
  };
}

function safeStr(v) {
  return String(v == null ? "" : v).toLowerCase().trim();
}

/** Avatar */
const FALLBACK_BACKEND = "http://localhost:8080";
function getBackendOrigin() {
  const base = String((http && http.defaults && http.defaults.baseURL) || "").trim();
  if (base.startsWith("http://") || base.startsWith("https://")) {
    try {
      return new URL(base).origin;
    } catch {
      return FALLBACK_BACKEND;
    }
  }
  return FALLBACK_BACKEND;
}
function resolveFileUrl(url) {
  const u = String(url || "").trim();
  if (!u) return "";
  if (u.startsWith("http://") || u.startsWith("https://") || u.startsWith("data:image")) return u;
  const origin = getBackendOrigin();
  return u.startsWith("/") ? origin + u : origin + "/" + u;
}
function resolveAvatar(c) {
  const url = String(c?.anhDaiDien || "").trim();
  if (!url) return "";
  return resolveFileUrl(url);
}
function onAvatarError(e, c) {
  if (c) c.anhDaiDien = "";
  if (e && e.target) e.target.src = "";
}
function getInitials(name) {
  const s = String(name || "").trim();
  if (!s) return "KH";
  const parts = s.split(/\s+/).filter(Boolean);
  const a = parts[0] ? parts[0][0] : "K";
  const b = parts[parts.length - 1] ? parts[parts.length - 1][0] : "H";
  return (a + b).toUpperCase();
}

/** Fetch */
async function fetchList() {
  loading.value = true;
  try {
    const res = await http.get("/api/khach-hang");
    list.value = unwrapList(res.data).map(normalizeCustomer);
  } catch (e) {
    console.error(e);
    toast.error("Không tải được danh sách khách hàng.");
  } finally {
    loading.value = false;
  }
}

/** Filter + Paging (lọc dựa trên filters) */
const filtered = computed(() => {
  const kw = safeStr(filters.keyword);
  const email = safeStr(filters.email);
  const phone = safeStr(filters.phone);
  const ma = safeStr(filters.maKhachHang);
  const acc = safeStr(filters.taiKhoan);

  return (list.value || []).filter((c) => {
    if (filters.statusMode !== "all") {
      if (filters.statusMode === "active" && !c.trangThai) return false;
      if (filters.statusMode === "inactive" && c.trangThai) return false;
    }

    if (email && !safeStr(c.email).includes(email)) return false;
    if (phone && !safeStr(c.soDienThoai).includes(phone)) return false;
    if (ma && !safeStr(c.maKhachHang).includes(ma)) return false;
    if (acc && !safeStr(c.taiKhoan).includes(acc)) return false;

    if (kw) {
      const blob = (
          String(c.maKhachHang || "") +
          " " +
          String(c.tenKhachHang || "") +
          " " +
          String(c.email || "") +
          " " +
          String(c.soDienThoai || "") +
          " " +
          String(c.taiKhoan || "")
      ).toLowerCase();
      if (!blob.includes(kw)) return false;
    }

    return true;
  });
});

const paged = computed(() => {
  const start = page.page * page.size;
  return filtered.value.slice(start, start + page.size);
});

function recalcPaging() {
  page.totalPages = Math.max(1, Math.ceil(filtered.value.length / page.size));
  if (page.page > page.totalPages - 1) page.page = Math.max(0, page.totalPages - 1);
  pageInput.value = page.page + 1;
}

/** ✅ Auto lọc: nhập/chọn là lọc ngay */
watch(
    filters,
    () => {
      page.page = 0;
      pageInput.value = 1;
      recalcPaging();
    },
    { deep: true }
);

function resetFilters() {
  Object.assign(filters, {
    keyword: "",
    email: "",
    phone: "",
    maKhachHang: "",
    taiKhoan: "",
    statusMode: "all",
  });
  page.page = 0;
  pageInput.value = 1;
  recalcPaging();
}

function setPage(p) {
  if (p < 0) return;
  if (page.totalPages && p > page.totalPages - 1) return;
  page.page = p;
  pageInput.value = page.page + 1;
}

function jumpPage() {
  const max = Math.max(1, page.totalPages || 1);
  const target = Math.min(Math.max(1, pageInput.value || 1), max);
  page.page = target - 1;
  pageInput.value = target;
}

/** Routing */
function goCreate() {
  router.push({ name: "customer-new" });
}
function goEdit(id) {
  router.push({ name: "customer-edit", params: { id } });
}
function goDetail(id) {
  router.push({ name: "customer-detail", params: { id } });
}

/** ✅ Confirm popup (custom overlay) */
const confirmOpen = ref(false);
const confirmState = reactive({
  customer: null,
  next: false,
  loading: false,
});

function openConfirm(customer, next) {
  confirmState.customer = customer;
  confirmState.next = !!next;
  confirmState.loading = false;
  confirmOpen.value = true;
}

/** ✅ FIX: cho phép đóng cưỡng bức khi xử lý thành công */
function closeConfirm(force = false) {
  if (confirmState.loading && !force) return;
  confirmOpen.value = false;
  confirmState.customer = null;
  confirmState.next = false;
  confirmState.loading = false;
}

function onSwitchAttempt(e, customer) {
  // revert UI ngay lập tức (chờ confirm)
  if (e?.target) e.target.checked = !!customer.trangThai;
  openConfirm(customer, !customer.trangThai);
}

/** ✅ FIX: đóng modal sau khi update thành công */
async function confirmChangeStatus() {
  if (!confirmState.customer) return;
  confirmState.loading = true;

  try {
    const id = confirmState.customer.id;
    const next = !!confirmState.next;

    await http.patch("/api/khach-hang/" + id + "/trang-thai", { trangThai: next });
    confirmState.customer.trangThai = next;

    toast.success("Đổi trạng thái thành công!");
    closeConfirm(true);
    recalcPaging();
  } catch (e) {
    console.error(e);
    toast.error("Đổi trạng thái thất bại!");
  } finally {
    confirmState.loading = false;
  }
}

/** Toast class */
function toastClass(type) {
  const t = String(type || "info").toLowerCase();
  if (t === "success") return "text-bg-success";
  if (t === "error") return "text-bg-danger";
  if (t === "warning") return "text-bg-warning";
  return "text-bg-info";
}

onMounted(async () => {
  await fetchList();
  recalcPaging();
});
</script>

<style scoped>
.table-responsive { overflow-x: visible !important; }
.table-scroll { overflow-x: visible !important; }

.table-head-dark th {
  background: #1f2a3a !important;
  color: #fff !important;
  font-weight: 600;
  border-color: #1f2a3a !important;
}
.table-body-normal td { font-weight: 400; text-transform: none; }

.addr-cell { min-width: 320px; }
.addr-full { white-space: normal; word-break: break-word; overflow: visible; }

.badge-working { background: #198754 !important; color: #fff !important; }
.badge-off { background: #e5e7eb !important; color: #6b7280 !important; }

/* ===== Filter (giống voucher) ===== */
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
.filter-title { font-weight: 700; }
.filter-hint { opacity: 0.75; }
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
.filter-header[aria-expanded="false"] .filter-icon {
  transform: rotate(-90deg);
}
.filter-body { background: #f8fafc; }
.filter-card .form-label { font-weight: 600; }
.filter-card .form-control,
.filter-card .form-select { border-radius: 10px; }

/* ===== Confirm modal overlay ===== */
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
.btn-outline {
  height: 38px;
  padding: 0 14px;
  border-radius: 10px;
  border: 1px solid #d0d7de;
  background: #fff;
  cursor: pointer;
  font-weight: 700;
}
.btn-confirm {
  height: 38px;
  padding: 0 14px;
  border-radius: 10px;
  border: 1px solid #1d4ed8;
  background: #1d4ed8;
  color: #fff;
  cursor: pointer;
  font-weight: 700;
}
.btn-outline:disabled,
.btn-confirm:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
