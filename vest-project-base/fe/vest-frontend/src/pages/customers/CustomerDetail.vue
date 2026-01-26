<template>
  <div class="container-fluid py-3">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-person-vcard fs-5"></i>
        <h6 class="mb-0 fw-semibold">Quản lý khách hàng / Chi tiết</h6>
      </div>

      <div class="d-flex align-items-center gap-2">
        <button class="btn btn-outline-secondary btn-sm" type="button" @click="goBack">
          <i class="bi bi-arrow-left me-1"></i> Quay lại danh sách
        </button>

        <button class="btn btn-outline-secondary btn-sm" type="button" @click="goEdit">
          <i class="bi bi-pencil-square me-1"></i> Sửa
        </button>

        <button class="btn btn-primary btn-sm text-white" type="button" @click="openConfirmToggle">
          <i class="bi bi-toggle-on me-1"></i> Đổi trạng thái
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
          <!-- Avatar center -->
          <div class="d-flex flex-column align-items-center mb-4">
            <div class="avatar-center">
              <img
                  v-if="resolveAvatar(customer)"
                  :src="resolveAvatar(customer)"
                  class="avatar-img"
                  alt="avatar"
                  @error="onAvatarError($event, customer)"
              />
              <div v-else class="avatar-fallback">
                {{ getInitials(customer?.tenKhachHang) }}
              </div>
            </div>

            <div class="mt-2 fw-semibold fs-5 text-center">
              {{ customer?.tenKhachHang || "-" }}
            </div>

            <div class="d-flex flex-wrap justify-content-center gap-2 mt-2">
              <span class="badge text-bg-light border">{{ customer?.maKhachHang || "-" }}</span>
              <span class="badge text-bg-light border">{{ genderText(customer?.gioiTinh) }}</span>
              <span class="badge" :class="customer?.trangThai ? 'text-bg-success' : 'text-bg-secondary'">
                {{ customer?.trangThai ? "Hoạt động" : "Không hoạt động" }}
              </span>
            </div>
          </div>

          <!-- Info grid -->
          <div class="row g-3">
            <div class="col-12 col-lg-6">
              <div class="info-box">
                <div class="info-label">Email</div>
                <div class="info-value">{{ customer?.email || "-" }}</div>
              </div>
            </div>

            <div class="col-12 col-lg-6">
              <div class="info-box">
                <div class="info-label">SĐT</div>
                <div class="info-value">{{ customer?.soDienThoai || "-" }}</div>
              </div>
            </div>

            <div class="col-12 col-lg-6">
              <div class="info-box">
                <div class="info-label">Tài khoản</div>
                <div class="info-value">{{ customer?.taiKhoan || "-" }}</div>
              </div>
            </div>

            <div class="col-12 col-lg-6">
              <div class="info-box">
                <div class="info-label">Giới tính</div>
                <div class="info-value">{{ genderText(customer?.gioiTinh) }}</div>
              </div>
            </div>

            <div class="col-12">
              <div class="info-box">
                <div class="info-label">Địa chỉ</div>
                <div class="info-value">{{ customer?.diaChi || "-" }}</div>
              </div>
            </div>

            <div class="col-12 col-lg-6">
              <div class="info-box">
                <div class="info-label">Người nhận</div>
                <div class="info-value">{{ customer?.diaChiMacDinh?.tenNguoiNhan || "-" }}</div>
              </div>
            </div>

            <div class="col-12 col-lg-6">
              <div class="info-box">
                <div class="info-label">SĐT người nhận</div>
                <div class="info-value">{{ customer?.diaChiMacDinh?.soDienThoai || "-" }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Confirm Modal -->
    <div class="modal fade" id="confirmStatusModal" tabindex="-1" aria-hidden="true" ref="confirmModalRef">
      <div class="modal-dialog modal-dialog-centered">
        <!-- ✅ thêm class để scope CSS bỏ border -->
        <div class="modal-content confirm-modal">
          <div class="modal-header">
            <h6 class="modal-title">Xác nhận</h6>
            <button type="button" class="btn-close" aria-label="Close" @click="closeConfirmModal"></button>
          </div>

          <div class="modal-body">
            Bạn có chắc chắn muốn đổi trạng thái khách hàng này không?
          </div>

          <div class="modal-footer">
            <button class="btn btn-light" type="button" @click="closeConfirmModal">Hủy</button>
            <button class="btn btn-agree" type="button" @click="confirmToggleStatus">Đồng ý</button>
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
import { useToast } from "@/composables/useToast";

const route = useRoute();
const router = useRouter();
const toast = useToast();

const loading = ref(false);
const customer = ref(null);

function goBack() {
  if (window.history.length > 1) {
    router.back();
    return;
  }
  try {
    if (router?.hasRoute && router.hasRoute("customer")) {
      router.push({ name: "customer" });
      return;
    }
  } catch {}
  router.push("/customers");
}

function goEdit() {
  router.push({ name: "customer-edit", params: { id: route.params.id } });
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

async function fetchDetail() {
  loading.value = true;
  try {
    const id = route.params.id;
    const res = await http.get("/api/khach-hang/" + id);
    customer.value = normalizeCustomer(res.data);
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || "Có lỗi xảy ra";
    toast.error(msg);
  } finally {
    loading.value = false;
  }
}

function genderText(v) {
  if (v === true) return "Nam";
  if (v === false) return "Nữ";
  return "Khác";
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
  document.body.classList.remove("modal-open");
  document.querySelector(".modal-backdrop")?.remove();
}

async function confirmToggleStatus() {
  try {
    const c = customer.value;
    if (!c) return;

    const next = !c.trangThai;
    await http.patch("/api/khach-hang/" + c.id + "/trang-thai", { trangThai: next });
    c.trangThai = next;

    toast.success("Đổi trạng thái thành công!");
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || "Có lỗi xảy ra";
    toast.error(msg);
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

.avatar-center {
  width: 86px;
  height: 86px;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
}
.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.avatar-fallback {
  width: 100%;
  height: 100%;
  background: #eef2ff;
  color: #1d4ed8;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}

.info-box {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 14px 16px;
  background: #fff;
}
.info-label {
  color: #6b7280;
  font-weight: 600;
  margin-bottom: 6px;
}
.info-value {
  font-weight: 400;
  color: #111827;
  word-break: break-word;
}

.btn-agree {
  background: #1d4ed8 !important;
  border-color: #1d4ed8 !important;
  color: #fff !important;
}
.btn-agree:hover {
  filter: brightness(0.95);
}

/* ✅ CHỈ SỬA PHẦN BẠN YÊU CẦU: bỏ 2 đường kẻ trong popup confirm */
.confirm-modal .modal-header,
.confirm-modal .modal-footer {
  border: 0 !important;
}
</style>
