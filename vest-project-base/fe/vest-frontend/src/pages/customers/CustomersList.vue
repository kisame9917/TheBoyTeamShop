<template>
  <div class="container-fluid py-3">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-people fs-4"></i>
        <h5 class="mb-0">Quản lý khách hàng / Danh sách khách hàng</h5>
      </div>

      <div class="d-flex align-items-center gap-2">
        <button class="btn btn-primary btn-sm text-white" type="button" @click="goCreate">
          <i class="bi bi-plus-lg me-1"></i> Thêm mới
        </button>
      </div>
    </div>

    <!-- Filters -->
    <div class="card shadow-sm mb-3">
      <div class="card-header bg-white d-flex align-items-center gap-2 text-dark fw-semibold">
        <i class="bi bi-funnel"></i>
        <span>Bộ lọc tìm kiếm</span>
      </div>
      <div class="card-body">
        <div class="row g-3">
          <div class="col-12 col-lg-6">
            <label class="form-label">Tìm kiếm</label>
            <input
                v-model.trim="filters.keyword"
                type="text"
                class="form-control"
                placeholder="Tìm theo mã, tên, email, SĐT, tài khoản..."
                @keyup.enter="applyFilters"
            />
          </div>

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

          <div class="col-12 col-lg-3">
            <label class="form-label">Số điện thoại</label>
            <input
                v-model.trim="filters.phone"
                type="text"
                class="form-control"
                placeholder="SĐT"
                @keyup.enter="applyFilters"
            />
          </div>

          <div class="col-12 col-lg-3">
            <label class="form-label">Mã khách hàng</label>
            <input
                v-model.trim="filters.ma"
                type="text"
                class="form-control"
                placeholder="Mã KH"
                @keyup.enter="applyFilters"
            />
          </div>

<!--          <div class="col-12 col-lg-3">-->
<!--            <label class="form-label">Tài khoản</label>-->
<!--            <input-->
<!--                v-model.trim="filters.taiKhoan"-->
<!--                type="text"-->
<!--                class="form-control"-->
<!--                placeholder="Tài khoản"-->
<!--                @keyup.enter="applyFilters"-->
<!--            />-->
<!--          </div>-->

          <div class="col-12 col-lg-6">
            <label class="form-label">Trạng thái hiển thị</label>
            <div class="d-flex align-items-center gap-3 mt-2 flex-wrap">
              <div class="form-check">
                <input class="form-check-input" type="radio" id="st_active" :value="1" v-model="filters.status" />
                <label class="form-check-label" for="st_active">Hoạt động</label>
              </div>
              <div class="form-check">
                <input class="form-check-input" type="radio" id="st_inactive" :value="0" v-model="filters.status" />
                <label class="form-check-label" for="st_inactive">Không hoạt động</label>
              </div>
              <div class="form-check">
                <input class="form-check-input" type="radio" id="st_all" :value="-1" v-model="filters.status" />
                <label class="form-check-label" for="st_all">Tất cả</label>
              </div>
            </div>
          </div>

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

        <div v-else class="table-responsive">
          <table class="table align-middle table-hover">
            <thead>
            <tr>
              <th style="width: 60px">#</th>
              <th style="width: 80px">Ảnh</th>
              <th style="width: 120px">Mã KH</th>
              <th>Họ tên</th>
              <th style="width: 140px">Email</th>
              <th style="width: 120px">SĐT</th>
              <th>Địa chỉ</th>
              <th style="width: 140px">Trạng thái</th>
              <th style="width: 160px">Hành động</th>
            </tr>
            </thead>

            <tbody>
            <tr v-if="pagedList.length === 0">
              <td colspan="9" class="text-center text-muted py-4">Không có dữ liệu</td>
            </tr>

            <tr v-for="(c, idx) in pagedList" :key="c.id">
              <td>{{ page.page * page.size + idx + 1 }}</td>

              <td>
                <div class="avatar-cell">
                  <img
                      v-if="resolveAvatar(c)"
                      :src="resolveAvatar(c)"
                      class="avatar-img"
                      alt="avatar"
                      @error="onAvatarError($event, c)"
                  />
                  <div v-else class="avatar-fallback">{{ getInitials(c.tenKhachHang) }}</div>
                </div>
              </td>

              <td class="fw-semibold">{{ c.maKhachHang }}</td>
              <td>{{ c.tenKhachHang }}</td>
              <td class="text-truncate" style="max-width: 220px" :title="c.email || ''">
                {{ c.email || "-" }}
              </td>
              <td>{{ c.soDienThoai || "-" }}</td>
              <td class="text-truncate" style="max-width: 360px" :title="c.diaChi || ''">
                {{ c.diaChi || "-" }}
              </td>

              <td>
                  <span class="badge" :class="c.trangThai ? 'text-bg-success' : 'text-bg-secondary'">
                    {{ c.trangThai ? "Hoạt động" : "Không hoạt động" }}
                  </span>
              </td>

              <td class="text-end">
                <div class="d-flex align-items-center justify-content-end gap-2">
                  <!-- Switch trạng thái (giống nhân viên) -->
                  <div class="form-check form-switch m-0">
                    <input
                        class="form-check-input"
                        type="checkbox"
                        :checked="!!c.trangThai"
                        @click.prevent="confirmToggleStatus(c)"
                        title="Đổi trạng thái"
                    />
                  </div>

                  <!-- Xem chi tiết -->
                  <button
                      class="btn btn-outline-primary btn-sm"
                      type="button"
                      title="Xem chi tiết"
                      @click="goDetail(c.id)"
                  >
                    <i class="bi bi-eye"></i>
                  </button>

                  <!-- Sửa -->
                  <button
                      class="btn btn-outline-secondary btn-sm"
                      type="button"
                      title="Sửa"
                      @click="goEdit(c.id)"
                  >
                    <i class="bi bi-pencil"></i>
                  </button>
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination (đã căn giữa) -->
        <div class="d-flex align-items-center mt-3">
          <div class="text-muted flex-grow-1">
            Hiển thị {{ pagedList.length }} / tổng {{ filteredList.length }} bản ghi
          </div>

          <div class="d-flex align-items-center justify-content-center gap-2 flex-grow-1">
            <button
                class="btn btn-outline-secondary btn-sm"
                :disabled="page.page === 0"
                @click="setPage(page.page - 1)"
            >
              <i class="bi bi-chevron-left"></i>
            </button>

            <div class="input-group input-group-sm" style="width: 120px">
              <span class="input-group-text">Trang</span>
              <input
                  type="number"
                  min="1"
                  :max="pageCount"
                  class="form-control"
                  v-model.number="pageInput"
                  @keyup.enter="jumpPage"
              />
            </div>

            <button
                class="btn btn-outline-secondary btn-sm"
                :disabled="page.page >= pageCount - 1"
                @click="setPage(page.page + 1)"
            >
              <i class="bi bi-chevron-right"></i>
            </button>
          </div>

          <div class="d-flex justify-content-end flex-grow-1">
            <select class="form-select form-select-sm" style="width: 160px" v-model.number="page.size" @change="applyFilters">
              <option :value="10">10 bản ghi / trang</option>
              <option :value="20">20 bản ghi / trang</option>
              <option :value="50">50 bản ghi / trang</option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <!-- Confirm Modal -->
    <div class="modal fade" id="confirmModal" tabindex="-1" aria-hidden="true" ref="confirmModalRef">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h6 class="modal-title">{{ confirmTitle }}</h6>
            <button type="button" class="btn-close" aria-label="Close" @click="closeConfirm"></button>
          </div>

          <div class="modal-body">
            {{ confirmMessage }}
          </div>

          <div class="modal-footer">
            <button class="btn btn-light" type="button" @click="closeConfirm">Hủy</button>
            <button class="btn btn-agree" type="button" @click="onConfirmOk">Đồng ý</button>
          </div>
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

const router = useRouter();
const toast = useToast();

const loading = ref(false);
const list = ref([]);

const filters = reactive({
  keyword: "",
  email: "",
  phone: "",
  ma: "",
  taiKhoan: "",
  status: -1, // -1 tất cả, 1 active, 0 inactive
});

const page = reactive({
  page: 0, // 0-based
  size: 10,
});
const pageInput = ref(1);

watch(
    () => [filters.keyword, filters.email, filters.phone, filters.ma, filters.taiKhoan, filters.status],
    () => {
      page.page = 0;
      pageInput.value = 1;
    }
);

function unwrapList(data) {
  if (!data) return [];
  if (Array.isArray(data)) return data;
  if (data && Array.isArray(data.result)) return data.result;
  if (data && Array.isArray(data.content)) return data.content;
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
    diaChiMacDinh: x.diaChiMacDinh ?? null,
  };
}

function safeStr(v) {
  return String(v == null ? "" : v).toLowerCase().trim();
}

const filteredList = computed(() => {
  const kw = safeStr(filters.keyword);
  const email = safeStr(filters.email);
  const phone = safeStr(filters.phone);
  const ma = safeStr(filters.ma);
  const acc = safeStr(filters.taiKhoan);

  return (list.value || []).filter((c) => {
    if (filters.status !== -1) {
      if (filters.status === 1 && !c.trangThai) return false;
      if (filters.status === 0 && c.trangThai) return false;
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

const pageCount = computed(() => Math.max(1, Math.ceil(filteredList.value.length / page.size)));
const pagedList = computed(() => {
  const start = page.page * page.size;
  return filteredList.value.slice(start, start + page.size);
});

function applyFilters() {
  page.page = 0;
  pageInput.value = 1;
}

function resetFilters() {
  filters.keyword = "";
  filters.email = "";
  filters.phone = "";
  filters.ma = "";
  filters.taiKhoan = "";
  filters.status = -1;
  page.page = 0;
  pageInput.value = 1;
}

function setPage(p) {
  if (p < 0) return;
  if (p > pageCount.value - 1) return;
  page.page = p;
  pageInput.value = page.page + 1;
}

function jumpPage() {
  const max = Math.max(1, pageCount.value || 1);
  const target = Math.min(Math.max(1, pageInput.value || 1), max);
  page.page = target - 1;
}

function goCreate() {
  router.push({ name: "customer-new" });
}
function goEdit(id) {
  router.push({ name: "customer-edit", params: { id } });
}
function goDetail(id) {
  router.push({ name: "customer-detail", params: { id } });
}

/** ===== Avatar URL ===== */
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

/** ===== Data ===== */
async function fetchList() {
  loading.value = true;
  try {
    const res = await http.get("/api/khach-hang");
    list.value = unwrapList(res.data).map(normalizeCustomer);
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || "Có lỗi xảy ra";
    toast.error(msg);
  } finally {
    loading.value = false;
  }
}

/** ===== Confirm Modal (Bootstrap) ===== */
const confirmModalRef = ref(null);
const confirmTitle = ref("Xác nhận");
const confirmMessage = ref("");
let bsConfirm = null;
let _confirmOk = null;

function openConfirm({ title = "Xác nhận", message = "", onOk = null } = {}) {
  confirmTitle.value = title;
  confirmMessage.value = message;
  _confirmOk = onOk;

  const modalEl = confirmModalRef.value;
  if (!modalEl) return;

  document.querySelectorAll(".modal-backdrop").forEach((b) => b.remove());
  document.body.classList.remove("modal-open");

  const Modal = window.bootstrap?.Modal;
  if (Modal) {
    bsConfirm = Modal.getOrCreateInstance(modalEl);
    bsConfirm.show();
  } else {
    modalEl.classList.add("show");
    modalEl.style.display = "block";
  }
}

function closeConfirm() {
  const modalEl = confirmModalRef.value;
  if (!modalEl) return;

  if (bsConfirm) {
    bsConfirm.hide();
    return;
  }
  modalEl.classList.remove("show");
  modalEl.style.display = "none";
  document.body.classList.remove("modal-open");
  document.querySelector(".modal-backdrop")?.remove();
}

async function onConfirmOk() {
  try {
    if (typeof _confirmOk === "function") await _confirmOk();
  } finally {
    closeConfirm();
  }
}

function confirmToggleStatus(c) {
  const next = !c.trangThai;
  const txt = next ? "chuyển sang Hoạt động" : "chuyển sang Không hoạt động";

  openConfirm({
    title: "Xác nhận",
    message: "Bạn có chắc muốn " + txt + " không?",
    onOk: async () => {
      try {
        await http.patch("/api/khach-hang/" + c.id + "/trang-thai", { trangThai: next });
        c.trangThai = next;
        // để không “mất dòng” do filter
        filters.status = -1;
        page.page = 0;
        pageInput.value = 1;

        toast.success("Đổi trạng thái thành công!");
      } catch (e) {
        const msg = e?.response?.data?.message || e?.message || "Có lỗi xảy ra";
        toast.error(msg);
      }
    },
  });
}

onMounted(fetchList);
</script>

<style scoped>
.card {
  border-radius: 14px;
}

/* Table head giống staff (màu tối) */
.table thead th {
  background: #1f2a3a;
  color: #fff;
  font-weight: 700;
  border-color: #1f2a3a;
}

.avatar-cell {
  display: flex;
  align-items: center;
  justify-content: flex-start;
}
.avatar-img {
  width: 38px;
  height: 38px;
  border-radius: 999px;
  object-fit: cover;
  border: 1px solid #e5e7eb;
}
.avatar-fallback {
  width: 38px;
  height: 38px;
  border-radius: 999px;
  background: #eef2ff;
  border: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  color: #1d4ed8;
  font-size: 12px;
}

/* Agree button color */
.btn-agree {
  background: #1d4ed8 !important;
  border-color: #1d4ed8 !important;
  color: #fff !important;
}
.btn-agree:hover {
  filter: brightness(0.95);
}
</style>
