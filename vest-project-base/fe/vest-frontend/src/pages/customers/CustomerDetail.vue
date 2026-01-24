<template>
  <div class="container-fluid py-3">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-person-vcard fs-4"></i>
        <h5 class="mb-0">Chi tiết khách hàng</h5>
      </div>

      <div class="d-flex align-items-center gap-2">
        <button class="btn btn-outline-secondary btn-sm" type="button" @click="goBack">
          <i class="bi bi-arrow-left me-1"></i> Quay lại
        </button>

        <button class="btn btn-outline-warning btn-sm" type="button" @click="goEdit">
          <i class="bi bi-pencil-square me-1"></i> Sửa
        </button>
      </div>
    </div>

    <div class="card shadow-sm">
      <div class="card-body">
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border" role="status"></div>
          <div class="mt-2 text-muted">Đang tải...</div>
        </div>

        <div v-else>
          <!-- ===== TOP like modal ===== -->
          <div class="detail-top">
            <div class="detail-avatar">
              <img
                  v-if="resolveAvatar(customer)"
                  :src="resolveAvatar(customer)"
                  class="detail-avatar-img"
                  alt="avatar"
                  @error="onAvatarError($event, customer)"
              />
              <div v-else class="detail-avatar-fallback">
                {{ getInitials(customer && customer.tenKhachHang) }}
              </div>
            </div>

            <div class="detail-head">
              <div class="detail-name">{{ (customer && customer.tenKhachHang) || "-" }}</div>

              <div class="detail-badges">
                <span class="badge badge-role">{{ (customer && customer.maKhachHang) || "-" }}</span>
                <span class="badge badge-role">{{ genderText(customer && customer.gioiTinh) }}</span>
                <span :class="['badge', customer && customer.trangThai ? 'badge-active' : 'badge-inactive']">
                  {{ customer && customer.trangThai ? "Hoạt động" : "Không hoạt động" }}
                </span>
              </div>
            </div>

            <div class="ms-auto d-flex align-items-center gap-2">
              <label class="switch" title="Đổi trạng thái">
                <input
                    type="checkbox"
                    :checked="!!customer?.trangThai"
                    @click.prevent="openConfirmToggle"
                />
                <span class="slider"></span>
              </label>
            </div>
          </div>

          <!-- ===== GRID like modal ===== -->
          <div class="detail-grid">
            <div class="detail-box">
              <div class="detail-label">Email</div>
              <div class="detail-value">{{ (customer && customer.email) || "-" }}</div>
            </div>

            <div class="detail-box">
              <div class="detail-label">SĐT</div>
              <div class="detail-value">{{ (customer && customer.soDienThoai) || "-" }}</div>
            </div>

            <div class="detail-box">
              <div class="detail-label">Tài khoản</div>
              <div class="detail-value">{{ (customer && customer.taiKhoan) || "-" }}</div>
            </div>

            <div class="detail-box col-full">
              <div class="detail-label">Địa chỉ</div>
              <div class="detail-value">{{ (customer && customer.diaChi) || "-" }}</div>
            </div>

            <div class="detail-box">
              <div class="detail-label">Người nhận</div>
              <div class="detail-value">
                {{ (customer && customer.diaChiMacDinh && customer.diaChiMacDinh.tenNguoiNhan) || "-" }}
              </div>
            </div>

            <div class="detail-box">
              <div class="detail-label">SĐT người nhận</div>
              <div class="detail-value">
                {{ (customer && customer.diaChiMacDinh && customer.diaChiMacDinh.soDienThoai) || "-" }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Confirm Modal -->
    <div class="modal fade" id="confirmStatusModal" tabindex="-1" aria-hidden="true" ref="confirmModalRef">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h6 class="modal-title">
              <i class="bi bi-exclamation-triangle me-2"></i>Xác nhận đổi trạng thái
            </h6>
            <button type="button" class="btn-close" aria-label="Close" @click="closeConfirmModal"></button>
          </div>

          <div class="modal-body">
            Bạn có chắc chắn muốn đổi trạng thái khách hàng này không?
          </div>

          <div class="modal-footer">
            <button class="btn btn-light" type="button" @click="closeConfirmModal">Hủy</button>
            <button class="btn btn-primary text-white" type="button" @click="confirmToggleStatus">
              Xác nhận
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import http from "../../services/http";

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const customer = ref(null);

/** ===== Normalizer (giữ đúng schema như list/modal) ===== */
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

async function fetchDetail() {
  loading.value = true;
  try {
    const id = route.params.id;
    const res = await http.get("/api/khach-hang/" + id);
    customer.value = normalizeCustomer(res.data);
  } finally {
    loading.value = false;
  }
}

/** ===== UI helpers ===== */
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

/** ===== Routing ===== */
function goBack() {
  router.push({ name: "customer" });
}

function goEdit() {
  router.push({ name: "customer-edit", params: { id: route.params.id } });
}

/** ===== Confirm modal (Bootstrap) ===== */
const confirmModalRef = ref(null);
let bsModal = null;

function openConfirmToggle() {
  const modalEl = confirmModalRef.value;
  if (!modalEl) return;

  document.querySelectorAll(".modal-backdrop").forEach((b) => b.remove());
  document.body.classList.remove("modal-open");

  const Modal = window.bootstrap?.Modal;
  if (Modal) {
    bsModal = Modal.getOrCreateInstance(modalEl);
    bsModal.show();
  } else {
    modalEl.classList.add("show");
    modalEl.style.display = "block";
  }
}

function closeConfirmModal() {
  const modalEl = confirmModalRef.value;
  if (!modalEl) return;

  if (bsModal) {
    bsModal.hide();
    return;
  }

  modalEl.classList.remove("show");
  modalEl.style.display = "none";
  modalEl.setAttribute("aria-hidden", "true");
  document.body.classList.remove("modal-open");
  const backdrop = document.querySelector(".modal-backdrop");
  if (backdrop) backdrop.remove();
}

async function confirmToggleStatus() {
  try {
    const c = customer.value;
    if (!c) return;

    const next = !c.trangThai;
    await http.patch("/api/khach-hang/" + c.id + "/trang-thai", { trangThai: next });
    c.trangThai = next;
  } finally {
    closeConfirmModal();
  }
}

onMounted(fetchDetail);
</script>

<style scoped>
.card {
  border-radius: 14px;
}

/* ===== Like your old modal style ===== */
.detail-top {
  display: flex;
  gap: 14px;
  align-items: center;
  margin-bottom: 14px;
}

.detail-avatar {
  width: 64px;
  height: 64px;
  border-radius: 999px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eef2ff;
}
.detail-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.detail-avatar-fallback {
  font-weight: 800;
  color: #1d4ed8;
}

.detail-name {
  font-size: 20px;
  font-weight: 800;
  margin-bottom: 6px;
}

.detail-badges {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-weight: 700;
  font-size: 12px;
}
.badge-role {
  background: #eef2ff;
  color: #1d4ed8;
}
.badge-active {
  background: #dbeafe;
  color: #1d4ed8;
}
.badge-inactive {
  background: #e5e7eb;
  color: #6b7280;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.detail-box {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 12px;
}
.detail-label {
  font-weight: 600;
  opacity: 0.7;
  margin-bottom: 6px;
}
.detail-value {
  font-weight: 400;
  color: #111827;
}
.col-full {
  grid-column: span 2;
}

@media (max-width: 860px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
  .col-full {
    grid-column: span 1;
  }
}

/* Switch */
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
