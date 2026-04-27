<template>
  <div class="container-fluid py-3">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-people fs-4"></i>
        <h5 class="mb-0">Danh sách khách hàng</h5>
      </div>

      <div class="d-flex align-items-center gap-2">
        <!-- ✅ Export Excel mode -->
        <button
            v-if="!exportMode"
            class="btn btn-outline-primary btn-sm"
            type="button"
            @click="startExportMode"
            :disabled="loading || paged.length === 0"
        >
          <i class="bi bi-file-earmark-excel me-1"></i> Xuất Excel
        </button>

        <button
            v-else
            class="btn btn-primary btn-sm text-white"
            type="button"
            @click="doExportExcel"
            :disabled="loading || selectedCount === 0"
        >
          <i class="bi bi-file-earmark-excel me-1"></i> Xuất Excel ({{ selectedCount }})
        </button>

        <button
            v-if="exportMode"
            class="btn btn-outline-secondary btn-sm"
            type="button"
            @click="cancelExportMode"
            :disabled="loading"
        >
          <i class="bi bi-x-lg me-1"></i> Hủy
        </button>

        <button class="btn btn-outline-primary btn-sm" type="button" @click="goCreate" title="Thêm mới" :disabled="isViewLocked">
          <i class="bi bi-plus-lg me-1"></i> Thêm mới
        </button>
      </div>
    </div>

    <!-- Filters -->
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
          <div class="row g-3 align-items-end">
            <div class="col-12 col-lg-4">
              <label class="form-label">Tìm kiếm</label>
              <input
                  v-model.trim="filters.keyword"
                  type="text"
                  class="form-control"
                  placeholder="Tìm theo mã, tên, email, SĐT, tài khoản..."
              />
            </div>

            <div class="col-12 col-lg-3">
              <label class="form-label">Email</label>
              <input
                  v-model.trim="filters.email"
                  type="text"
                  class="form-control"
                  placeholder="Email"
              />
            </div>

            <div class="col-12 col-lg-2">
              <label class="form-label">SĐT</label>
              <input
                  v-model.trim="filters.phone"
                  type="text"
                  class="form-control"
                  placeholder="SĐT"
              />
            </div>

            <div class="col-12 col-lg-3">
              <label class="form-label">Trạng thái</label>
              <div class="d-flex align-items-center gap-3 flex-nowrap status-filter">
                <div class="form-check mb-0">
                  <input
                      class="form-check-input"
                      type="radio"
                      id="cst_all"
                      value="all"
                      v-model="filters.statusMode"
                  />
                  <label class="form-check-label" for="cst_all">Tất cả</label>
                </div>

                <div class="form-check mb-0">
                  <input
                      class="form-check-input"
                      type="radio"
                      id="cst_on"
                      value="active"
                      v-model="filters.statusMode"
                  />
                  <label class="form-check-label" for="cst_on">Hoạt động</label>
                </div>

                <div class="form-check mb-0">
                  <input
                      class="form-check-input"
                      type="radio"
                      id="cst_off"
                      value="inactive"
                      v-model="filters.statusMode"
                  />
                  <label class="form-check-label" for="cst_off">Không hoạt động</label>
                </div>
              </div>
            </div>

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

        <div v-else class="table-area">
          <div class="table-scroll">
            <table class="table align-middle table-hover">
              <thead class="table-head-dark">
              <tr>
                <th v-if="exportMode" style="width: 44px" class="text-center">
                  <input
                      type="checkbox"
                      :checked="isAllSelected"
                      :indeterminate.prop="isSomeSelected"
                      @change="toggleSelectAll"
                      title="Chọn tất cả (trang hiện tại)"
                  />
                </th>
                <th style="width: 60px" class="text-center">#</th>
                <th style="width: 80px" class="text-center">Ảnh</th>
                <th style="width: 110px" class="text-center">Mã KH</th>
                <th style="width: 220px" class="text-center">Họ tên</th>
                <th style="width: 140px" class="text-center">Ngày sinh</th>
                <th style="width: 260px" class="text-center">Email</th>
                <th style="width: 140px" class="text-center">SĐT</th>
                <th class="col-address text-center">Địa chỉ</th>
                <th style="width: 140px" class="text-center">Trạng thái</th>
                <th style="width: 190px" class="text-center">Hành động</th>
              </tr>
              </thead>

              <tbody class="table-body-normal">
              <tr v-if="paged.length === 0">
                <td :colspan="exportMode ? 11 : 10" class="text-center text-muted py-4">
                  Không có dữ liệu
                </td>
              </tr>

              <tr v-for="(c, idx) in paged" :key="c.id">
                <td v-if="exportMode" class="text-center">
                  <input
                      class="form-check-input m-0"
                      type="checkbox"
                      :checked="isSelected(c.id)"
                      @change="onSelectRow($event, c.id)"
                      title="Chọn dòng"
                  />
                </td>

                <td>{{ page.page * page.size + idx + 1 }}</td>

                <td>
                  <div class="d-flex align-items-center justify-content-center">
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
                <td>{{ formatNgaySinh(c.ngaySinh) }}</td>

                <td class="text-truncate" style="max-width: 260px" :title="c.email || ''">
                  {{ c.email || "-" }}
                </td>

                <td>{{ c.soDienThoai || "-" }}</td>

                <td class="addr-cell" :title="c.diaChi || ''">
                  <div class="addr-full">{{ c.diaChi || "-" }}</div>
                </td>

                <td class="text-center">
              <span class="badge fw-normal" :class="asBool(c.trangThai) ? 'badge-working' : 'badge-off'">
                {{ asBool(c.trangThai) ? "Hoạt động" : "Không hoạt động" }}
              </span>
                </td>

                <td class="text-end">
                  <div class="d-inline-flex align-items-center gap-2">
                    <button
                        class="btn btn-outline-primary btn-sm"
                        type="button"
                        title="Đổi địa chỉ"
                        @click="openAddressModal(c)"
                        :disabled="isViewLocked"
                    >
                      <i class="bi bi-geo-alt"></i>
                    </button>

                    <button
                        class="btn btn-outline-warning btn-sm"
                        type="button"
                        title="Sửa"
                        @click="goEdit(c.id)"
                        :disabled="isViewLocked"
                    >
                      <i class="bi bi-pencil-square"></i>
                    </button>

                    <div class="form-check form-switch m-0 switch-lg" title="Đổi trạng thái">
                      <input
                          class="form-check-input"
                          type="checkbox"
                          role="switch"
                          :checked="asBool(c.trangThai)"
                          @change="onSwitchAttempt($event, c)"
                          :disabled="isViewLocked"
                      />
                    </div>
                  </div>
                </td>
              </tr>
              </tbody>
            </table>
          </div>

          <!-- Phân trang đã đưa ra ngoài border của bảng -->
          <div class="table-pagination row align-items-center mt-3 g-2">
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
              <select
                  class="form-select form-select-sm"
                  style="width: 160px"
                  v-model.number="page.size"
                  @change="recalcPaging"
              >
                <option :value="10">10 bản ghi / trang</option>
                <option :value="20">20 bản ghi / trang</option>
                <option :value="50">50 bản ghi / trang</option>
              </select>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ✅ Confirm popup đổi trạng thái -->
    <div v-if="confirmOpen" class="modal-overlay" @click.self="closeConfirm()">
      <div class="modal-card confirm-modal">
        <h3 class="modal-title">Xác nhận</h3>

        <p class="modal-desc" v-if="confirmState.customer">
          Bạn muốn đổi trạng thái khách hàng
          <b>{{ confirmState.customer.maKhachHang }}</b>
          từ
          <span class="badge fw-normal" :class="confirmState.current ? 'badge-working' : 'badge-off'">
            {{ confirmState.current ? "Hoạt động" : "Không hoạt động" }}
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

    <!-- ✅ Modal đổi địa chỉ -->
    <div v-if="addrModal.open" class="modal-overlay" @click.self="closeAddressModal">
      <div class="modal-card" style="width: min(720px, calc(100% - 32px))">
        <h3 class="modal-title">Đổi địa chỉ</h3>
        <p class="modal-desc" v-if="addrModal.customer">
          Chọn địa chỉ mặc định cho khách hàng <b>{{ addrModal.customer.maKhachHang }}</b>
        </p>

        <div v-if="addrModal.loadingList" class="text-muted">Đang tải địa chỉ...</div>

        <div v-else>
          <div v-if="addrModal.list.length === 0" class="text-muted">Khách hàng chưa có địa chỉ.</div>

          <div v-else class="addr-radio-list">
            <label
                v-for="a in addrModal.list"
                :key="a.id"
                class="addr-radio-item"
                :class="{ 'addr-radio-item--checked': String(addrModal.selectedId) === String(a.id) }"
            >
              <input
                  type="radio"
                  class="form-check-input me-2"
                  name="addr_pick"
                  :value="a.id"
                  v-model="addrModal.selectedId"
                  :disabled="isViewLocked || addrModal.saving"
              />
              <div class="addr-radio-content">
                <div class="fw-semibold">
                  <!--                  {{ a.tenNguoiNhan || "Người nhận" }}-->
                  <!--                  <span class="text-muted fw-normal">• {{ a.soDienThoai || "-" }}</span>-->
                </div>
                <div class="text-muted ">
                  {{ formatDiaChiText(a) }}
                  <span v-if="asBool(a.laMacDinh)" class="badge text-bg-primary ms-2">Mặc định</span>
                </div>
              </div>
            </label>
          </div>
        </div>

        <div class="modal-actions" style="margin-top: 14px">
          <button class="btn btn-outline" type="button" @click="closeAddressModal" :disabled="addrModal.saving">Hủy</button>
          <button class="btn btn-confirm" type="button" @click="saveDefaultAddress" :disabled="isViewLocked || addrModal.saving || !addrModal.selectedId">
            {{ addrModal.saving ? "Đang lưu..." : "Lưu" }}
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
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import http from "../../services/http";
import { useToast } from "@/composables/useToast";
import { useAuthStore } from "@/stores/auth";
import { useShiftStore } from "@/stores/shift";
import { resolveMediaUrl } from "@/utils/media";
import * as XLSX from "xlsx";

const router = useRouter();
const toast = useToast();

const auth = useAuthStore();
const shift = useShiftStore();
const isViewLocked = computed(() => auth.isStaff && shift.isLocked);

function blockIfViewMode() {
  if (!isViewLocked.value) return false;
  toast.info("Bạn đang ở chế độ xem. Không thể thao tác khách hàng.");
  return true;
}

const loading = ref(false);
const list = ref([]);

const filterOpen = ref(true);
function toggleFilter() {
  filterOpen.value = !filterOpen.value;
}

const filters = reactive({
  keyword: "",
  email: "",
  phone: "",
  maKhachHang: "",
  taiKhoan: "",
  statusMode: "all",
});

const page = reactive({
  page: 0,
  size: 10,
  totalPages: 1,
});
const pageInput = ref(1);

/** ✅ Export Excel selection mode */
const exportMode = ref(false);
const selectedIds = ref(new Set());

const selectedCount = computed(() => selectedIds.value.size);

function isSelected(id) {
  return selectedIds.value.has(id);
}

function onSelectRow(e, id) {
  const next = new Set(selectedIds.value);
  if (e?.target?.checked) next.add(id);
  else next.delete(id);
  selectedIds.value = next;
}
/** ===== Helpers ===== */
function unwrapList(data) {
  if (!data) return [];
  if (Array.isArray(data)) return data;
  if (Array.isArray(data.result)) return data.result;
  if (Array.isArray(data.content)) return data.content;
  if (Array.isArray(data.data)) return data.data;
  return [];
}

function asBool(v) {
  if (v === true || v === false) return v;
  if (typeof v === "number") return v === 1;
  if (typeof v === "string") {
    const s = v.trim().toLowerCase();
    if (s === "1" || s === "true") return true;
    if (s === "0" || s === "false") return false;
  }
  return !!v;
}

function normalizeCustomer(x) {
  x = x || {};
  return {
    id: x.id,
    maKhachHang: x.maKhachHang ?? x.ma_khach_hang ?? "",
    tenKhachHang: x.tenKhachHang ?? x.ten_khach_hang ?? "",
    ngaySinh: x.ngaySinh ?? x.ngay_sinh ?? "",
    gioiTinh: x.gioiTinh ?? x.gioi_tinh ?? null,
    email: x.email ?? "",
    soDienThoai: x.soDienThoai ?? x.so_dien_thoai ?? "",
    taiKhoan: x.taiKhoan ?? x.tai_khoan ?? "",
    trangThai: x.trangThai ?? x.trang_thai ?? true,
    anhDaiDien: x.anhDaiDien ?? x.anh_dai_dien ?? "",
    mediaAvatarId: x.mediaAvatarId ?? x.idMediaAvatar ?? x.id_media_avatar ?? null,
    diaChi: x.diaChi ?? "",
    diaChiMacDinh: x.diaChiMacDinh ?? null,
  };
}

function formatNgaySinh(v) {
  const raw = String(v || "").trim();
  if (!raw) return "-";

  // BE thường trả LocalDate dạng "YYYY-MM-DD". Nếu có time thì cắt phần date.
  const ymd = raw.includes("T") ? raw.split("T")[0] : raw;
  const parts = ymd.split("-");
  if (parts.length !== 3) return raw;
  const [y, m, d] = parts;
  const dd = String(d).padStart(2, "0");
  const mm = String(m).padStart(2, "0");
  return `${dd}/${mm}/${y}`;
}

function safeStr(v) {
  return String(v == null ? "" : v).toLowerCase().trim();
}

/** ===== Avatar ===== */
function resolveAvatar(c) {
  return resolveMediaUrl(c?.anhDaiDien || c?.avatarUrl || c?.mediaAsset || "");
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

/** ===== API fallback ===== */
function isMethodNotSupported(err) {
  const status = err?.response?.status;
  const msg = String(err?.response?.data?.message || err?.message || "").toLowerCase();
  return status === 405 || msg.includes("request method") || msg.includes("not supported") || msg.includes("method");
}

async function requestWithFallback(url, { data = undefined, methods = ["patch", "put", "post"] } = {}) {
  let lastErr = null;
  for (const m of methods) {
    try {
      return await http.request({ url, method: m, data });
    } catch (e) {
      lastErr = e;
      if (isMethodNotSupported(e)) continue;
      throw e;
    }
  }
  throw lastErr;
}

/** ===== Fetch list ===== */
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

/** ===== Filter + Paging ===== */
const filtered = computed(() => {
  const kw = safeStr(filters.keyword);
  const email = safeStr(filters.email);
  const phone = safeStr(filters.phone);
  const ma = safeStr(filters.maKhachHang);
  const acc = safeStr(filters.taiKhoan);

  return (list.value || []).filter((c) => {
    const st = asBool(c.trangThai);

    if (filters.statusMode !== "all") {
      if (filters.statusMode === "active" && !st) return false;
      if (filters.statusMode === "inactive" && st) return false;
    }

    if (email && !safeStr(c.email).includes(email)) return false;
    if (phone && !safeStr(c.soDienThoai).includes(phone)) return false;
    if (ma && !safeStr(c.maKhachHang).includes(ma)) return false;
    if (acc && !safeStr(c.taiKhoan).includes(acc)) return false;

    if (kw) {
      const blob =
          (String(c.maKhachHang || "") +
              " " +
              String(c.tenKhachHang || "") +
              " " +
              String(c.email || "") +
              " " +
              String(c.soDienThoai || "") +
              " " +
              String(c.taiKhoan || "")).toLowerCase();
      if (!blob.includes(kw)) return false;
    }

    return true;
  });
});

const paged = computed(() => {
  const start = page.page * page.size;
  return filtered.value.slice(start, start + page.size);
});
const isAllSelected = computed(() => {
  const ids = (paged.value || []).map((x) => x.id);
  if (!ids.length) return false;
  return ids.every((id) => selectedIds.value.has(id));
});

const isSomeSelected = computed(() => {
  const ids = (paged.value || []).map((x) => x.id);
  if (!ids.length) return false;
  const any = ids.some((id) => selectedIds.value.has(id));
  const all = ids.every((id) => selectedIds.value.has(id));
  return any && !all;
});

function toggleSelectAll(e) {
  const checked = !!e?.target?.checked;
  const ids = (paged.value || []).map((x) => x.id);

  const next = new Set(selectedIds.value);
  if (checked) ids.forEach((id) => next.add(id));
  else ids.forEach((id) => next.delete(id));

  selectedIds.value = next;
}
function recalcPaging() {
  page.totalPages = Math.max(1, Math.ceil(filtered.value.length / page.size));
  if (page.page > page.totalPages - 1) page.page = Math.max(0, page.totalPages - 1);
  pageInput.value = page.page + 1;
}

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

/** ===== Routing ===== */
function goCreate() {
  if (blockIfViewMode()) return;
  router.push({ name: "customer-new" });
}
function goEdit(id) {
  if (blockIfViewMode()) return;
  router.push({ name: "customer-edit", params: { id } });
}

/** ✅ Confirm popup đổi trạng thái (FIX) */
const confirmOpen = ref(false);
const confirmState = reactive({
  customer: null,
  current: false,
  next: false,
  loading: false,
});

function openConfirm(customer, next) {
  const current = asBool(customer?.trangThai);
  confirmState.customer = customer;
  confirmState.current = current;
  confirmState.next = typeof next === "boolean" ? next : !current;
  confirmState.loading = false;
  confirmOpen.value = true;
}

/** ✅ FIX: thêm force để đóng được sau khi xử lý xong */
function closeConfirm(force = false) {
  if (confirmState.loading && !force) return;
  confirmOpen.value = false;
  confirmState.customer = null;
  confirmState.current = false;
  confirmState.next = false;
  confirmState.loading = false;
}

function onSwitchAttempt(e, customer) {
  if (blockIfViewMode()) {
    if (e?.target) e.target.checked = asBool(customer?.trangThai);
    return;
  }
  if (e?.target) e.target.checked = asBool(customer.trangThai);
  const current = asBool(customer.trangThai);
  const next = !current;
  openConfirm(customer, next);
}

async function confirmChangeStatus() {
  if (!confirmState.customer) return;
  confirmState.loading = true;

  try {
    const id = confirmState.customer.id;
    const next = !!confirmState.next;

    await requestWithFallback(`/api/khach-hang/${id}/trang-thai`, {
      data: { trangThai: next },
      methods: ["patch", "put", "post"],
    });

    // update data
    confirmState.customer.trangThai = next;

    toast.success("Đổi trạng thái thành công!");

    // ✅ FIX: tắt loading trước, rồi đóng force
    confirmState.loading = false;
    closeConfirm(true);

    recalcPaging();
  } catch (e) {
    console.error(e);
    const msg = e?.response?.data?.message || e?.message || "Đổi trạng thái thất bại!";
    toast.error(msg);
    confirmState.loading = false;
  }
}

/** ===== Đổi địa chỉ (modal) ===== */
const addrModal = reactive({
  open: false,
  customer: null,
  list: [],
  selectedId: null,
  loadingList: false,
  saving: false,
});

function formatDiaChiText(a) {
  const parts = [a?.diaChiChiTiet, a?.phuongXa, a?.tinhThanh, a?.quocGia]
      .map((x) => String(x || "").trim())
      .filter(Boolean);
  return parts.join(", ");
}

async function openAddressModal(customer) {
  if (blockIfViewMode()) return;
  addrModal.open = true;
  addrModal.customer = customer;
  addrModal.list = [];
  addrModal.selectedId = null;
  addrModal.saving = false;
  addrModal.loadingList = true;

  try {
    const res = await http.get(`/api/khach-hang/${customer.id}/dia-chi`);
    const arr = unwrapList(res.data);
    addrModal.list = arr;

    const def = arr.find((x) => asBool(x.laMacDinh));
    addrModal.selectedId = def ? def.id : (arr[0]?.id ?? null);
  } catch (e) {
    console.error(e);
    toast.error("Không tải được danh sách địa chỉ.");
  } finally {
    addrModal.loadingList = false;
  }
}

function closeAddressModal() {
  if (addrModal.saving) return;
  addrModal.open = false;
  addrModal.customer = null;
  addrModal.list = [];
  addrModal.selectedId = null;
  addrModal.loadingList = false;
  addrModal.saving = false;
}

async function saveDefaultAddress() {
  if (blockIfViewMode()) return;
  if (!addrModal.customer || !addrModal.selectedId) return;

  addrModal.saving = true;
  let ok = false;

  try {
    const khId = addrModal.customer.id;
    const diaChiId = addrModal.selectedId;

    const url = `/api/khach-hang/${khId}/dia-chi/${diaChiId}/mac-dinh`;

    await requestWithFallback(url, {
      data: {},
      methods: ["patch", "put", "post"],
    });

    addrModal.list = (addrModal.list || []).map((x) => ({
      ...x,
      laMacDinh: String(x.id) === String(diaChiId),
    }));

    const picked = addrModal.list.find((x) => String(x.id) === String(diaChiId));
    if (picked) {
      addrModal.customer.diaChiMacDinh = picked;
      addrModal.customer.diaChi = formatDiaChiText(picked);
    }

    toast.success("Cập nhật địa chỉ mặc định thành công!");
    ok = true;
  } catch (e) {
    console.error(e);
    const msg = e?.response?.data?.message || e?.message || "Cập nhật địa chỉ mặc định thất bại!";
    toast.error(msg);
  } finally {
    addrModal.saving = false;
    if (ok) closeAddressModal();
  }
}

/** ✅ Export Excel */
function startExportMode() {
  exportMode.value = true;
  selectedIds.value = new Set();
}

function cancelExportMode() {
  exportMode.value = false;
  selectedIds.value = new Set();
}

function doExportExcel() {
  try {
    const picked = selectedIds.value;
    const source = (list.value || []).filter((c) => picked.has(c.id));

    if (!source.length) {
      toast.warning("Bạn chưa chọn dòng nào để xuất.");
      return;
    }

    const rows = source.map((c, i) => ({
      STT: i + 1,
      "Mã KH": c.maKhachHang || "",
      "Họ tên": c.tenKhachHang || "",
      "Ngày sinh": formatNgaySinh(c.ngaySinh) === "-" ? "" : formatNgaySinh(c.ngaySinh),
      "Giới tính": c.gioiTinh === true ? "Nam" : c.gioiTinh === false ? "Nữ" : "",
      Email: c.email || "",
      "SĐT": c.soDienThoai || "",
      "Tài khoản": c.taiKhoan || "",
      "Trạng thái": asBool(c.trangThai) ? "Hoạt động" : "Không hoạt động",
      "Địa chỉ": c.diaChi || "",
    }));

    const ws = XLSX.utils.json_to_sheet(rows);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "Customers");

    const filename = `customers_selected_${Date.now()}.xlsx`;
    XLSX.writeFile(wb, filename);

    toast.success("Xuất Excel thành công!");
    cancelExportMode(); // ✅ export xong -> ẩn checkbox + về nút Xuất Excel ban đầu
  } catch (e) {
    console.error(e);
    toast.error("Xuất Excel thất bại! (Kiểm tra thư viện xlsx)");
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
/* ===== Table giống phiếu giảm giá - không scrollbar ngang, phân trang ngoài border ===== */
.table-area {
  width: 100%;
  overflow: hidden;
}

.table-scroll {
  width: 100%;
  border: 1px solid #dee2e6;
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
}

.table-scroll > table {
  width: 100% !important;
  max-width: 100% !important;
  margin-bottom: 0;
  border-collapse: separate;
  border-spacing: 0;
  table-layout: fixed;
}

.table-scroll > table th,
.table-scroll > table td {
  padding: 12px 12px;
  border-bottom: 1px solid #e9ecef;
  vertical-align: middle;
  box-sizing: border-box;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: center;
}

/* Căn giữa nội dung đặc biệt trong TD */
.table-scroll > table td .d-flex {
  justify-content: center;
}

.table-scroll > table td.text-end {
  text-align: center !important;
}

.table-scroll > table td .d-inline-flex {
  justify-content: center;
}

.table-scroll > table tbody tr:last-child td {
  border-bottom: none;
}

.table-head-dark th {
  background: #1f2a44 !important;
  color: #fff !important;
  font-weight: 700;
  border-color: #1f2a44 !important;
}

.table-body-normal td {
  font-weight: 400;
  text-transform: none;
}

/* Không cho row phân trang tạo overflow ngang */
.table-pagination {
  width: 100%;
  margin-left: 0 !important;
  margin-right: 0 !important;
}

/* Cột địa chỉ giữ 1 dòng, dài thì hiện ... */
.col-address {
  width: 500px;
}

.addr-cell {
  overflow: hidden;
}

.addr-full {
  display: block;
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.badge-working {
  background: #198754 !important;
  color: #fff !important;
}

.badge-off {
  background: #e5e7eb !important;
  color: #6b7280 !important;
}

/* Filter card */
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

/* Overlay modal */
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
  width: min(520px, calc(100% - 32px));
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
.btn-confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Address radio list in modal */
.addr-radio-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.addr-radio-item {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 12px;
  cursor: pointer;
  user-select: none;
}

.addr-radio-item--checked {
  border-color: #0d6efd;
  background: #f8fbff;
}

.addr-radio-content {
  flex: 1;
}

.badge-working { background: #198754 !important; color: #fff !important; }
.badge-off { background: #e5e7eb !important; color: #6b7280 !important; }

/* Filter card */
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
.filter-header[aria-expanded="false"] .filter-icon { transform: rotate(-90deg); }
.filter-body { background: #f8fafc; }
.filter-card .form-label { font-weight: 600; }
.filter-card .form-control,
.filter-card .form-select { border-radius: 10px; }

/* Overlay modal */
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
  width: min(520px, calc(100% - 32px));
  background: #fff;
  border-radius: 14px;
  padding: 18px 18px 14px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}
.modal-title { margin: 0 0 8px; font-size: 18px; font-weight: 700; }
.modal-desc { margin: 0 0 14px; color: #555; line-height: 1.4; }
.modal-actions { display: flex; gap: 10px; justify-content: flex-end; }

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

/* Address radio list in modal */
.addr-radio-list { display: flex; flex-direction: column; gap: 10px; }
.addr-radio-item{
  display:flex;
  gap:10px;
  align-items:flex-start;
  border:1px solid #e5e7eb;
  border-radius:12px;
  padding:12px;
  cursor:pointer;
  user-select:none;
}
.addr-radio-item--checked{
  border-color:#0d6efd;
  background:#f8fbff;
}
.addr-radio-content{ flex:1; }
.switch-lg {
  padding-left: 0;
  min-height: 0;
  display: inline-flex;
  align-items: center;
}

.switch-lg .form-check-input {
  width: 42px;
  height: 22px;
  margin: 0;
  cursor: pointer;
  border-radius: 999px;
}

.switch-lg .form-check-input:checked {
  background-color: #2563eb;
  border-color: #2563eb;
}

.switch-lg .form-check-input:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}
</style>