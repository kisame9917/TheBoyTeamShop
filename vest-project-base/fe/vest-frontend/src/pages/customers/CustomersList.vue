<template>
  <div class="container-fluid py-3">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-people fs-4"></i>
        <h5 class="mb-0">Quản lý khách hàng / Danh sách khách hàng</h5>
      </div>

      <button class="btn btn-primary btn-sm text-white" type="button" @click="goCreate">
        <i class="bi bi-plus-lg me-1"></i> Thêm mới
      </button>
    </div>

    <!-- Filters -->
    <div class="card shadow-sm mb-3">
      <div class="card-body">
        <div class="row g-3">
          <div class="col-12 col-lg-6">
            <label class="form-label">Tìm kiếm</label>
            <input
                class="form-control"
                v-model="filters.keyword"
                placeholder="Tìm theo mã, tên, email, SĐT, tài khoản..."
                @keyup.enter="applyFilters"
            />
          </div>

          <div class="col-12 col-lg-3">
            <label class="form-label">Email</label>
            <input class="form-control" v-model="filters.email" placeholder="Email" @keyup.enter="applyFilters" />
          </div>

          <div class="col-12 col-lg-3">
            <label class="form-label">Số điện thoại</label>
            <input class="form-control" v-model="filters.phone" placeholder="SĐT" @keyup.enter="applyFilters" />
          </div>

          <div class="col-12 col-lg-6">
            <label class="form-label">Trạng thái hiển thị</label>
            <div class="d-flex flex-wrap gap-3 mt-2">
              <div class="form-check">
                <input class="form-check-input" type="radio" id="st_on" v-model="filters.status" :value="1" />
                <label class="form-check-label" for="st_on">Hoạt động</label>
              </div>

              <div class="form-check">
                <input class="form-check-input" type="radio" id="st_off" v-model="filters.status" :value="0" />
                <label class="form-check-label" for="st_off">Không hoạt động</label>
              </div>

              <div class="form-check">
                <input class="form-check-input" type="radio" id="st_all" v-model="filters.status" :value="-1" />
                <label class="form-check-label" for="st_all">Tất cả</label>
              </div>
            </div>
          </div>

          <div class="col-12 col-lg-3">
            <label class="form-label">Mã khách hàng</label>
            <input class="form-control" v-model="filters.ma" placeholder="Mã KH" @keyup.enter="applyFilters" />
          </div>

          <div class="col-12 col-lg-3">
            <label class="form-label">Tài khoản</label>
            <input class="form-control" v-model="filters.taiKhoan" placeholder="Tài khoản" @keyup.enter="applyFilters" />
          </div>

          <div class="col-12 d-flex justify-content-end gap-2">
            <button class="btn btn-light" type="button" @click="resetFilters">
              <i class="bi bi-arrow-counterclockwise me-1"></i> Đặt lại
            </button>
            <button class="btn btn-primary btn-sm text-white" type="button" @click="applyFilters">
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
            <thead class="table-head-dark">
            <tr>
              <th style="width: 60px">#</th>
              <th style="width: 80px">Ảnh</th>
              <th style="width: 110px">Mã KH</th>
              <th style="width: 180px">Họ tên</th>
              <th style="width: 120px" class="text-center">Giới tính</th>
              <th style="width: 240px">Email</th>
              <th style="width: 140px">SĐT</th>
              <th style="width: 500px">Địa chỉ</th>
              <th style="width: 140px" class="text-center">Trạng thái</th>
              <th style="width: 170px" class="text-center">Hành động</th>
            </tr>
            </thead>

            <tbody>
            <tr v-if="pagedList.length === 0">
              <td colspan="10" class="text-center text-muted py-4">Không có dữ liệu</td>
            </tr>

            <tr v-for="(c, idx) in pagedList" :key="c.id">
              <td>{{ page.page * page.size + idx + 1 }}</td>

              <td>
                <div class="d-flex align-items-center">
                  <img
                      v-if="resolveAvatar(c)"
                      :src="resolveAvatar(c)"
                      class="avatar-img"
                      alt="avatar"
                      @error="onAvatarError($event, c)"
                  />
                  <div v-else class="avatar-fallback">
                    {{ getInitials(c.tenKhachHang) }}
                  </div>
                </div>
              </td>

              <td class="fw-semibold">{{ c.maKhachHang }}</td>
              <td>{{ c.tenKhachHang }}</td>
              <td class="text-center">{{ genderText(c.gioiTinh) }}</td>

              <td class="text-truncate" :title="c.email || ''" style="max-width: 260px">
                {{ c.email || "-" }}
              </td>

              <td>{{ c.soDienThoai || "-" }}</td>

              <td class="text-truncate" :title="c.diaChi || ''" style="max-width: 420px">
                {{ c.diaChi || "-" }}
              </td>

              <td class="text-center">
                  <span class="badge" :class="c.trangThai ? 'text-bg-primary' : 'text-bg-secondary'">
                    {{ c.trangThai ? "Hoạt động" : "Không hoạt động" }}
                  </span>
              </td>

              <td class="text-end">
                <div class="d-inline-flex align-items-center gap-2">
                  <!-- Switch đổi trạng thái -->
                  <label class="switch" title="Đổi trạng thái">
                    <input
                        type="checkbox"
                        :checked="!!c.trangThai"
                        @click.prevent="confirmToggleStatus(c)"
                    />
                    <span class="slider"></span>
                  </label>

                  <button
                      class="btn btn-outline-primary btn-sm"
                      type="button"
                      title="Xem chi tiết"
                      @click="goDetail(c.id)"
                  >
                    <i class="bi bi-eye"></i>
                  </button>

                  <button
                      class="btn btn-outline-warning btn-sm"
                      type="button"
                      title="Sửa"
                      @click="goEdit(c.id)"
                  >
                    <i class="bi bi-pencil-square"></i>
                  </button>
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination kiểu StaffList -->
        <div class="d-flex align-items-center justify-content-between mt-3">
          <div class="text-muted">
            Hiển thị {{ pagedList.length }} / tổng {{ filteredList.length }} bản ghi
          </div>

          <div class="d-flex align-items-center gap-2">
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

            <select
                class="form-select form-select-sm"
                style="width: 160px"
                v-model.number="page.size"
                @change="applyFilters"
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
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import http from "../../services/http";

const router = useRouter();

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

// paging giống StaffList: 0-based
const page = reactive({
  page: 0,
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
    gioiTinh: x.gioiTinh ?? x.gioi_tinh ?? null, // true/false/null
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

function genderText(v) {
  if (v === true) return "Nam";
  if (v === false) return "Nữ";
  return "-";
}

function getInitials(name) {
  const s = String(name || "").trim();
  if (!s) return "KH";
  const parts = s.split(/\s+/).filter(Boolean);
  const a = parts[0] ? parts[0][0] : "K";
  const b = parts[parts.length - 1] ? parts[parts.length - 1][0] : "H";
  return (a + b).toUpperCase();
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

/** ===== Data ===== */
async function fetchList() {
  loading.value = true;
  try {
    const res = await http.get("/api/khach-hang");
    list.value = unwrapList(res.data).map(normalizeCustomer);
  } finally {
    loading.value = false;
  }
}

const filteredList = computed(() => {
  const kw = safeStr(filters.keyword);
  const email = safeStr(filters.email);
  const phone = safeStr(filters.phone);
  const ma = safeStr(filters.ma);
  const acc = safeStr(filters.taiKhoan);

  return (list.value || []).filter((c) => {
    // status
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

const pageCount = computed(() => Math.max(1, Math.ceil(filteredList.value.length / (page.size || 10))));

const pagedList = computed(() => {
  const start = page.page * (page.size || 10);
  return filteredList.value.slice(start, start + (page.size || 10));
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
  const max = Math.max(1, pageCount.value);
  if (p < 0) return;
  if (p > max - 1) return;
  page.page = p;
  pageInput.value = page.page + 1;
}

function jumpPage() {
  const max = Math.max(1, pageCount.value);
  const target = Math.min(Math.max(1, pageInput.value || 1), max);
  page.page = target - 1;
}

/** ===== Routing ===== */
function goCreate() {
  router.push({ name: "customer-new" });
}
function goEdit(id) {
  router.push({ name: "customer-edit", params: { id } });
}
function goDetail(id) {
  // ✅ bạn tạo trang detail giống StaffDetail
  router.push({ name: "customer-detail", params: { id } });
}

/** ===== Toggle status ===== */
async function confirmToggleStatus(c) {
  const next = !c.trangThai;
  const txt = next ? "chuyển sang Hoạt động" : "chuyển sang Không hoạt động";
  if (!confirm("Bạn có chắc chắn " + txt + " không?")) return;

  await http.patch("/api/khach-hang/" + c.id + "/trang-thai", { trangThai: next });
  c.trangThai = next;

  // để không “mất dòng” do filter
  filters.status = -1;
  applyFilters();
}

onMounted(fetchList);
</script>

<style scoped>
.card {
  border-radius: 14px;
}
.table thead th {
  font-weight: 600;
}

/* Avatar */
.avatar-img {
  width: 40px;
  height: 40px;
  border-radius: 999px;
  object-fit: cover;
  border: 1px solid #e5e7eb;
}
.avatar-fallback {
  width: 40px;
  height: 40px;
  border-radius: 999px;
  background: #eef2ff;
  border: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #1d4ed8;
  font-size: 12px;
}

.table-head-dark th {
  background: #1f2a3a !important;
  color: #fff !important;
  font-weight: 600;
}


/* Switch (giữ nguyên UX như bạn) */
.switch {
  position: relative;
  display: inline-block;
  width: 42px;
  height: 22px;
}
.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}
.slider {
  position: absolute;
  cursor: pointer;
  inset: 0;
  background: #cbd5e1;
  transition: 0.2s;
  border-radius: 999px;
}
.slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 2px;
  top: 2px;
  background: white;
  transition: 0.2s;
  border-radius: 999px;
}
.switch input:checked + .slider {
  background: #1d4ed8;
}
.switch input:checked + .slider:before {
  transform: translateX(20px);
}
</style>
