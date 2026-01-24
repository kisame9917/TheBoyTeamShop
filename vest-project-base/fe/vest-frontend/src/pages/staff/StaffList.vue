<template>
  <div class="container-fluid py-3">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-people fs-4"></i>
        <h5 class="mb-0">Danh sách nhân viên</h5>
      </div>

      <button
          class="btn btn-primary btn-sm text-white"
          type="button"
          @click="goCreate"
          :disabled="!isAdmin"
          title="Thêm mới"
      >
        <i class="bi bi-plus-lg me-1"></i> Thêm mới
      </button>
    </div>

    <!-- Filters -->
    <div class="card shadow-sm mb-3">
      <div class="card-header bg-white d-flex align-items-center gap-2 text-dark fw-semibold">
        <i class="bi bi-funnel"></i>
        <span>Bộ lọc tìm kiếm</span>
      </div>

      <div class="card-body">
        <div class="row g-3">
          <!-- keyword -->
          <div class="col-12 col-lg-6">
            <label class="form-label">Tìm kiếm</label>
            <input
                v-model.trim="filters.keyword"
                type="text"
                class="form-control"
                placeholder="Tìm theo mã, tên, email, SĐT..."
                @keyup.enter="applyFilters"
            />
          </div>

          <!-- role -->
          <div class="col-12 col-lg-3">
            <label class="form-label">Chức vụ</label>
            <select v-model="filters.chucVu" class="form-select" @change="applyFilters">
              <option value="">Tất cả</option>
              <option value="ADMIN">Admin</option>
              <option value="NHAN_VIEN">Nhân viên</option>
            </select>
          </div>

          <!-- email -->
          <div class="col-12 col-lg-3">
            <label class="form-label">Email</label>
            <input
                v-model.trim="filters.email"
                type="text"
                class="form-control"
                placeholder="Email"
                @keyup.enter="applyFilters"
            />
          </div>

          <!-- phone -->
          <div class="col-12 col-lg-3">
            <label class="form-label">SĐT</label>
            <input
                v-model.trim="filters.phone"
                type="text"
                class="form-control"
                placeholder="SĐT"
                @keyup.enter="applyFilters"
            />
          </div>

          <!-- ma nv -->
          <div class="col-12 col-lg-3">
            <label class="form-label">Mã nhân viên</label>
            <input
                v-model.trim="filters.maNhanVien"
                type="text"
                class="form-control"
                placeholder="Mã NV"
                @keyup.enter="applyFilters"
            />
          </div>

          <!-- status -->
          <div class="col-12 col-lg-6">
            <label class="form-label">Trạng thái</label>
            <div class="d-flex align-items-center gap-3 mt-2">
              <div class="form-check">
                <input class="form-check-input" type="radio" id="st_all" value="all" v-model="filters.statusMode" />
                <label class="form-check-label" for="st_all">Tất cả</label>
              </div>
              <div class="form-check">
                <input class="form-check-input" type="radio" id="st_work" value="working" v-model="filters.statusMode" />
                <label class="form-check-label" for="st_work">Đang làm</label>
              </div>
              <div class="form-check">
                <input class="form-check-input" type="radio" id="st_off" value="off" v-model="filters.statusMode" />
                <label class="form-check-label" for="st_off">Đã nghỉ</label>
              </div>
            </div>
          </div>

          <!-- buttons bottom like orders page -->
          <div class="col-12 d-flex justify-content-end gap-2">
            <button class="btn btn-light" type="button" @click="resetFilters">
              <i class="bi bi-arrow-counterclockwise me-1"></i> Đặt lại
            </button>
            <button class="btn btn-primary text-white" type="button" @click="applyFilters">
              <i class="bi bi-search me-1"></i> Lọc
            </button>
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

        <div v-else class="table-narrow">
          <div class="table-scroll">

            <table class="table align-middle table-hover mb-0">
            <thead class="table-head-dark">
            <tr>
              <th style="width: 60px">#</th>
              <th style="width: 80px">Ảnh</th>
              <th style="width: 110px">Mã NV</th>
              <th style="width: 180px">Họ tên</th>
              <th style="width: 240px">Email</th>
              <th style="width: 140px">SĐT</th>
              <th style="width: 500px">Địa chỉ</th>
              <th style="width: 130px" class="text-center">Chức vụ</th>
              <th style="width: 140px" class="text-center">Trạng thái</th>
              <th style="width: 170px" class="text-center">Hành động</th>
            </tr>
            </thead>

            <tbody class="table-body-normal">
            <tr v-if="paged.length === 0">
              <td colspan="10" class="text-center text-muted py-4">Không có dữ liệu</td>
            </tr>

            <tr v-for="(s, idx) in paged" :key="s.id">
              <td>{{ page.page * page.size + idx + 1 }}</td>

              <td>
                <div class="d-flex align-items-center">
                  <img
                      v-if="resolveAvatar(s)"
                      :src="resolveAvatar(s)"
                      class="rounded-circle border"
                      style="width: 40px; height: 40px; object-fit: cover"
                      alt="avatar"
                      @error="onAvatarError($event, s)"
                  />
                  <div
                      v-else
                      class="rounded-circle border d-flex align-items-center justify-content-center"
                      style="width: 40px; height: 40px; background: #eef2ff; color: #1d4ed8; font-weight: 700"
                  >
                    {{ getInitials(s.tenNhanVien) }}
                  </div>
                </div>
              </td>

              <td>{{ s.maNhanVien || "-" }}</td>
              <td>{{ s.tenNhanVien || "-" }}</td>

              <td class="text-truncate" style="max-width: 240px" :title="s.email || ''">
                {{ s.email || "-" }}
              </td>

              <td>{{ s.soDienThoai || "-" }}</td>

              <td class="addr-cell">
                <div class="addr-full">
                  {{ s.diaChi || "-" }}
                </div>
              </td>

              <td class="text-center">
                  <span class="badge text-bg-light border badge-normal">
                    {{ getRoleText(s) }}
                  </span>
              </td>

              <td class="text-center">
                  <span class="badge fw-normal" :class="s.trangThai ? 'badge-working' : 'badge-off'">
                    {{ s.trangThai ? "Đang làm" : "Đã nghỉ" }}
                  </span>
              </td>

              <td class="text-end">
                <div class="d-inline-flex align-items-center gap-2">
                  <!-- Switch (FIX: dùng @change, revert ngay, mở confirm) -->
                  <div class="form-check form-switch m-0" title="Đổi trạng thái">
                    <input
                        class="form-check-input"
                        type="checkbox"
                        role="switch"
                        :checked="!!s.trangThai"
                        :disabled="!isAdmin"
                        @change="onSwitchAttempt($event, s)"
                    />
                  </div>

                  <!-- detail -->
                  <button class="btn btn-outline-primary btn-sm" type="button" title="Xem chi tiết" @click="goDetail(s.id)">
                    <i class="bi bi-eye"></i>
                  </button>

                  <!-- edit -->
                  <button
                      class="btn btn-outline-secondary btn-sm"
                      type="button"
                      title="Sửa"
                      @click="goEdit(s.id)"
                      :disabled="!isAdmin"
                  >
                    <i class="bi bi-pencil"></i>
                  </button>
                </div>
              </td>
            </tr>
            </tbody>
          </table>

          <!-- Pagination (CENTER) -->
          <div class="row align-items-center mt-3 g-2">
            <div class="col-12 col-lg-4 text-muted">
              Hiển thị {{ paged.length }} / tổng {{ filtered.length }} bản ghi
            </div>

            <div class="col-12 col-lg-4 d-flex justify-content-center">
              <div class="d-flex align-items-center gap-2">
                <button
                    class="btn btn-outline-secondary btn-sm"
                    :disabled="page.page === 0"
                    @click="setPage(page.page - 1)"
                >
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
              <select class="form-select form-select-sm" style="width: 140px" v-model.number="page.size" @change="applyFilters">
                <option :value="10">10 / trang</option>
                <option :value="20">20 / trang</option>
                <option :value="50">50 / trang</option>
              </select>
            </div>
          </div>
        </div>
        </div>
      </div>
    </div>

    <!-- Confirm Modal (Bootstrap) -->
    <div class="modal fade" tabindex="-1" ref="confirmModalRef" aria-hidden="true">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h6 class="modal-title">
              <i class="bi bi-exclamation-triangle me-2"></i>Xác nhận đổi trạng thái
            </h6>
            <button type="button" class="btn-close" aria-label="Close" @click="closeConfirm"></button>
          </div>

          <div class="modal-body">
            <div v-if="confirmState.staff">
              Bạn muốn đổi trạng thái nhân viên
              <b>{{ confirmState.staff.maNhanVien }}</b>
              từ
              <span class="badge fw-normal" :class="confirmState.staff.trangThai ? 'badge-working' : 'badge-off'">
                {{ confirmState.staff.trangThai ? "Đang làm" : "Đã nghỉ" }}
              </span>
              sang
              <span class="badge fw-normal" :class="confirmState.next ? 'badge-working' : 'badge-off'">
                {{ confirmState.next ? "Đang làm" : "Đã nghỉ" }}
              </span>
              ?
            </div>

            <div class="text-muted small mt-2">
              Hành động này sẽ cập nhật trạng thái nhân viên.
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn btn-light" type="button" @click="closeConfirm" :disabled="confirmState.loading">
              Hủy
            </button>
            <button class="btn btn-primary" type="button" @click="confirmChangeStatus" :disabled="confirmState.loading">
              <span v-if="confirmState.loading" class="spinner-border spinner-border-sm me-2" role="status"></span>
              Xác nhận
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Toast (dùng composable useToast.js của bạn) -->
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
import { computed, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import http from "../../services/http";
import { useAuthStore } from "../../stores/auth";
import { useToast } from "@/composables/useToast";

const router = useRouter();
const auth = useAuthStore();
const toast = useToast();

const isAdmin = computed(() => !!auth.isAdmin);

const loading = ref(false);
const list = ref([]);

const filters = reactive({
  keyword: "",
  chucVu: "",
  email: "",
  phone: "",
  maNhanVien: "",
  statusMode: "all", // all | working | off
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

function normalizeStaff(x) {
  x = x || {};
  const quyenHan = x.quyenHan || {};
  return {
    id: x.id,
    maNhanVien: x.maNhanVien ?? "",
    tenNhanVien: x.tenNhanVien ?? "",
    soDienThoai: x.soDienThoai ?? "",
    email: x.email ?? "",
    taiKhoan: x.taiKhoan ?? "",
    diaChi: x.diaChi ?? "",
    trangThai: x.trangThai ?? true,
    tenQuyenHan: x.tenQuyenHan ?? quyenHan.tenQuyenHan ?? "",
    quyenHanId: x.quyenHanId ?? quyenHan.id ?? null,
    anhDaiDien: x.anhDaiDien ?? x.anh_dai_dien ?? "",
  };
}

function safeStr(v) {
  return String(v == null ? "" : v).toLowerCase().trim();
}

/** ROLE (logic giữ nguyên, chỉ đổi text hiển thị) */
function getRoleCode(s) {
  const id = Number(s?.quyenHanId ?? s?.quyenHan?.id ?? null);
  if (id === 1) return "ADMIN";
  if (id === 2) return "NHAN_VIEN";

  const roleName = String(s?.tenQuyenHan ?? s?.quyenHan?.tenQuyenHan ?? "").toUpperCase();
  if (roleName.includes("ADMIN")) return "ADMIN";
  return "NHAN_VIEN";
}
function getRoleText(s) {
  return getRoleCode(s) === "ADMIN" ? "Admin" : "Nhân viên";
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
function resolveAvatar(s) {
  const url = String(s?.anhDaiDien || "").trim();
  if (!url) return "";
  return resolveFileUrl(url);
}
function onAvatarError(e, s) {
  if (s) s.anhDaiDien = "";
  if (e && e.target) e.target.src = "";
}
function getInitials(name) {
  const s = String(name || "").trim();
  if (!s) return "NV";
  const parts = s.split(/\s+/).filter(Boolean);
  const a = parts[0] ? parts[0][0] : "N";
  const b = parts[parts.length - 1] ? parts[parts.length - 1][0] : "V";
  return (a + b).toUpperCase();
}

/** Fetch */
async function fetchList() {
  loading.value = true;
  try {
    const res = await http.get("/api/nhan-vien");
    list.value = unwrapList(res.data).map(normalizeStaff);
  } catch (e) {
    console.error(e);
    toast.error("Không tải được danh sách nhân viên.");
  } finally {
    loading.value = false;
  }
}

/** Filter */
const filtered = computed(() => {
  const kw = safeStr(filters.keyword);
  const email = safeStr(filters.email);
  const phone = safeStr(filters.phone);
  const ma = safeStr(filters.maNhanVien);

  return (list.value || []).filter((s) => {
    if (filters.statusMode !== "all") {
      if (filters.statusMode === "working" && !s.trangThai) return false;
      if (filters.statusMode === "off" && s.trangThai) return false;
    }

    if (filters.chucVu) {
      if (getRoleCode(s) !== filters.chucVu) return false;
    }

    if (email && !safeStr(s.email).includes(email)) return false;
    if (phone && !safeStr(s.soDienThoai).includes(phone)) return false;
    if (ma && !safeStr(s.maNhanVien).includes(ma)) return false;

    if (kw) {
      const blob = (
          String(s.maNhanVien || "") +
          " " +
          String(s.tenNhanVien || "") +
          " " +
          String(s.email || "") +
          " " +
          String(s.soDienThoai || "")
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

function recalcPages() {
  page.totalPages = Math.max(1, Math.ceil(filtered.value.length / page.size));
  if (page.page > page.totalPages - 1) page.page = page.totalPages - 1;
  pageInput.value = page.page + 1;
}

function applyFilters() {
  page.page = 0;
  recalcPages();
}

function resetFilters() {
  filters.keyword = "";
  filters.chucVu = "";
  filters.email = "";
  filters.phone = "";
  filters.maNhanVien = "";
  filters.statusMode = "all";
  page.page = 0;
  recalcPages();
}

function setPage(p) {
  if (p < 0) return;
  if (p > page.totalPages - 1) return;
  page.page = p;
  pageInput.value = page.page + 1;
}

function jumpPage() {
  const max = Math.max(1, page.totalPages || 1);
  const target = Math.min(Math.max(1, pageInput.value || 1), max);
  page.page = target - 1;
}

/** Navigation */
function goCreate() {
  if (!isAdmin.value) return toast.warning("Chỉ ADMIN mới được thêm mới.");
  if (router.hasRoute("staff-new")) return router.push({ name: "staff-new" });
  router.push("/staff/new");
}
function goEdit(id) {
  if (!isAdmin.value) return toast.warning("Chỉ ADMIN mới được sửa.");
  if (router.hasRoute("staff-edit")) return router.push({ name: "staff-edit", params: { id } });
  router.push(`/staff/${id}/edit`);
}
function goDetail(id) {
  const candidates = ["staff-detail", "staff-view", "staff-show"];
  const found = candidates.find((n) => router.hasRoute(n));
  if (found) return router.push({ name: found, params: { id } });
  router.push(`/staff/${id}`);
}

/** Confirm Modal */
const confirmModalRef = ref(null);
let bsConfirm = null;

const confirmState = reactive({
  staff: null,
  next: false,
  loading: false,
});

function openConfirmStatus(staff, nextValue) {
  if (!isAdmin.value) return toast.warning("Chỉ ADMIN mới được đổi trạng thái.");

  confirmState.staff = staff;
  confirmState.next = !!nextValue;
  confirmState.loading = false;

  const Modal = window.bootstrap?.Modal;
  if (Modal && confirmModalRef.value) {
    bsConfirm = Modal.getOrCreateInstance(confirmModalRef.value);
    bsConfirm.show();
    return;
  }

  // fallback nếu bootstrap js chưa load
  const ok = window.confirm(
      `Bạn muốn đổi trạng thái ${staff.maNhanVien} sang ${confirmState.next ? "Đang làm" : "Đã nghỉ"}?`
  );
  if (ok) confirmChangeStatus();
}

function closeConfirm() {
  try {
    bsConfirm?.hide?.();
  } catch {}
}

async function patchStatus(id, next) {
  await http.patch(`/api/nhan-vien/${id}/trang-thai`, { trangThai: next });
  const idx = (list.value || []).findIndex((x) => x.id === id);
  if (idx !== -1) list.value[idx].trangThai = next;
}

async function confirmChangeStatus() {
  if (!confirmState.staff) return;

  confirmState.loading = true;
  try {
    const id = confirmState.staff.id;
    const next = confirmState.next;

    await patchStatus(id, next);

    toast.success("Cập nhật trạng thái thành công!");
    closeConfirm();
    recalcPages();
  } catch (e) {
    console.error(e);
    toast.error("Cập nhật trạng thái thất bại.");
  } finally {
    confirmState.loading = false;
  }
}

/** ✅ SWITCH FIX: @change -> revert ngay -> mở confirm */
function onSwitchAttempt(e, staff) {
  if (!isAdmin.value) {
    e.target.checked = !!staff.trangThai;
    return toast.warning("Chỉ ADMIN mới được đổi trạng thái.");
  }

  const nextValue = !!e.target.checked;
  // revert ngay để tránh đổi “ảo” khi chưa confirm
  e.target.checked = !!staff.trangThai;

  openConfirmStatus(staff, nextValue);
}

/** Toast classes */
function toastClass(type) {
  const t = String(type || "info").toLowerCase();
  if (t === "success") return "text-bg-success";
  if (t === "error") return "text-bg-danger";
  if (t === "warning") return "text-bg-warning";
  return "text-bg-info";
}

onMounted(async () => {
  await fetchList();
  recalcPages();
});
</script>

<style scoped>
.table-narrow {
  max-width: 98%;
  margin: 0 auto;
}

.table-head-dark th {
  background: #1f2a3a !important;
  color: #fff !important;
  font-weight: 600;
}

.table-body-normal td {
  font-weight: 400;
  text-transform: none;
}

/* chỉ table mới scroll ngang, không kéo qua paging */
.table-scroll {

}

/* địa chỉ hiển thị đầy đủ, cho phép xuống dòng */
.addr-full {
  white-space: normal;
  word-break: break-word;
  overflow: visible;
}

/* optional: nếu muốn địa chỉ rộng hơn khi phóng to */
.addr-cell {
  min-width: 320px; /* tăng/giảm tùy ý */
}

.badge-normal {
  font-weight: 400;
}

.badge-working {
  background: #198754 !important;
  color: #fff !important;
}

.badge-off {
  background: #e5e7eb !important;
  color: #6b7280 !important;
}
</style>
