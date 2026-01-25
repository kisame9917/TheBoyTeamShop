<template>
  <div class="container-fluid py-3">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-person-vcard fs-4"></i>
        <h5 class="mb-0">Quản lý nhân viên / Chi tiết</h5>
      </div>

      <div class="d-flex align-items-center gap-2">
        <button class="btn btn-outline-secondary btn-sm" type="button" @click="goBack">
          <i class="bi bi-arrow-left me-1"></i> Quay lại danh sách
        </button>

        <button class="btn btn-outline-secondary btn-sm" type="button" :disabled="!isAdmin" @click="goEdit">
          <i class="bi bi-pencil-square me-1"></i> Sửa
        </button>

        <button class="btn btn-primary btn-sm text-white" type="button" :disabled="!isAdmin" @click="askToggle">
          <i class="bi bi-toggle2-on me-1"></i> Đổi trạng thái
        </button>
      </div>
    </div>

    <div class="card shadow-sm">
      <div class="card-body">
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border" role="status"></div>
          <div class="mt-2 text-muted">Đang tải...</div>
        </div>

        <div v-else-if="errorMsg" class="alert alert-warning mb-0">
          {{ errorMsg }}
        </div>

        <template v-else>
          <!-- Top: avatar + name + badges -->
          <div class="text-center mb-4">
            <div class="avatar-wrap mx-auto mb-2" role="button" title="Ảnh đại diện">
              <img
                  v-if="resolveAvatar(staff)"
                  :src="resolveAvatar(staff)"
                  class="avatar-img"
                  alt="avatar"
                  @error="onAvatarError($event, staff)"
              />
              <div v-else class="avatar-fallback">
                {{ getInitials(staff?.tenNhanVien) }}
              </div>
            </div>

            <div class="fs-5 fw-semibold">
              {{ staff?.tenNhanVien || "-" }}
            </div>

            <div class="d-flex align-items-center justify-content-center gap-2 mt-2 flex-wrap">
              <span class="badge text-bg-light border">{{ staff?.maNhanVien || "-" }}</span>
              <span class="badge text-bg-light border">{{ getRoleText(staff) }}</span>
              <span class="badge" :class="staff?.trangThai ? 'text-bg-success' : 'text-bg-secondary'">
                {{ staff?.trangThai ? "Đang làm" : "Đã nghỉ" }}
              </span>
            </div>
          </div>

          <!-- Detail grid -->
          <div class="row g-3">
            <div class="col-12 col-lg-6">
              <div class="info-box">
                <div class="label">Email</div>
                <div class="value">{{ staff?.email || "-" }}</div>
              </div>
            </div>

            <div class="col-12 col-lg-6">
              <div class="info-box">
                <div class="label">SĐT</div>
                <div class="value">{{ staff?.soDienThoai || "-" }}</div>
              </div>
            </div>

            <div class="col-12 col-lg-6">
              <div class="info-box">
                <div class="label">CCCD</div>
                <div class="value">{{ staff?.cccd || "-" }}</div>
              </div>
            </div>

            <div class="col-12 col-lg-6">
              <div class="info-box">
                <div class="label">Ngày sinh</div>
                <div class="value">{{ formatDate(staff?.ngaySinh) }}</div>
              </div>
            </div>

            <div class="col-12 col-lg-6">
              <div class="info-box">
                <div class="label">Giới tính</div>
                <div class="value">{{ mapGender(staff?.gioiTinh) }}</div>
              </div>
            </div>

            <div class="col-12 col-lg-6">
              <div class="info-box">
                <div class="label">Tài khoản</div>
                <div class="value">{{ staff?.taiKhoan || "-" }}</div>
              </div>
            </div>

            <div class="col-12">
              <div class="info-box">
                <div class="label">Địa chỉ</div>
                <div class="value addr">{{ staff?.diaChi || "-" }}</div>
              </div>
            </div>

            <div class="col-12 col-lg-6" v-if="staff?.ngayTao">
              <div class="info-box">
                <div class="label">Ngày tạo</div>
                <div class="value">{{ formatDateTime(staff?.ngayTao) }}</div>
              </div>
            </div>

            <div class="col-12 col-lg-6" v-if="staff?.ngayCapNhat">
              <div class="info-box">
                <div class="label">Ngày cập nhật</div>
                <div class="value">{{ formatDateTime(staff?.ngayCapNhat) }}</div>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- Confirm modal (Bootstrap) -->
    <div class="modal fade" id="confirmModal" tabindex="-1" aria-hidden="true" ref="confirmModalRef">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content confirm-modal">
          <div class="modal-header">
            <h6 class="modal-title">
              <i class="bi bi-exclamation-triangle me-2"></i>Xác nhận đổi trạng thái
            </h6>
            <button type="button" class="btn-close" aria-label="Close" @click="closeConfirm"></button>
          </div>

          <div class="modal-body">
            <div class="mb-2">
              Bạn muốn đổi trạng thái nhân viên <b>{{ staff?.maNhanVien || "-" }}</b> từ
              <span class="badge ms-1" :class="staff?.trangThai ? 'text-bg-success' : 'text-bg-secondary'">
                {{ staff?.trangThai ? "Đang làm" : "Đã nghỉ" }}
              </span>
              sang
              <span class="badge ms-1" :class="pendingNext ? 'text-bg-success' : 'text-bg-secondary'">
                {{ pendingNext ? "Đang làm" : "Đã nghỉ" }}
              </span>
              ?
            </div>

            <div class="text-muted small">Hành động này sẽ cập nhật trạng thái nhân viên.</div>

            <div v-if="confirmError" class="alert alert-warning mt-3 mb-0">
              {{ confirmError }}
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn btn-light" type="button" @click="closeConfirm" :disabled="confirmLoading">Hủy</button>
            <button class="btn btn-primary text-white" type="button" @click="confirmToggle" :disabled="confirmLoading">
              <span v-if="confirmLoading" class="spinner-border spinner-border-sm me-2" role="status"></span>
              Xác nhận
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Toast -->
    <div class="toast-container position-fixed top-0 end-0 p-3" style="z-index: 9999">
      <div
          v-for="t in toast.state.items"
          :key="t.id"
          class="toast show align-items-center border-0 mb-2"
          role="alert"
          aria-live="assertive"
          aria-atomic="true"
          :class="toastClass(t.type)"
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
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import http from "@/services/http";
import { useAuthStore } from "@/stores/auth";
import { useToast } from "@/composables/useToast";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const toast = useToast();

const isAdmin = computed(() => !!auth.isAdmin);

const loading = ref(false);
const errorMsg = ref("");
const staff = ref(null);

/** ===== Helpers: normalize + role ===== */
function unwrapOne(data) {
  if (!data) return null;
  if (data.result) return data.result;
  return data;
}

function normalizeStaff(x) {
  x = x || {};
  const quyenHan = x.quyenHan || {};
  return {
    id: x.id,
    maNhanVien: x.maNhanVien ?? "",
    tenNhanVien: x.tenNhanVien ?? "",
    soDienThoai: x.soDienThoai ?? "",
    cccd: x.cccd ?? "",
    email: x.email ?? "",
    taiKhoan: x.taiKhoan ?? "",
    ngaySinh: x.ngaySinh ?? null,
    gioiTinh: x.gioiTinh ?? null,
    diaChi: x.diaChi ?? "",
    trangThai: x.trangThai ?? true,
    tenQuyenHan: x.tenQuyenHan ?? quyenHan.tenQuyenHan ?? "",
    quyenHanId: x.quyenHanId ?? quyenHan.id ?? null,
    anhDaiDien: x.anhDaiDien ?? x.anh_dai_dien ?? "",
    ngayTao: x.ngayTao ?? null,
    ngayCapNhat: x.ngayCapNhat ?? null,
  };
}

function getRoleCode(s) {
  if (Number(s?.quyenHanId) === 1) return "ADMIN";
  if (Number(s?.quyenHanId) === 2) return "NHAN_VIEN";
  const name = String(s?.tenQuyenHan || "").toUpperCase();
  if (name.includes("ADMIN")) return "ADMIN";
  return "NHAN_VIEN";
}

function getRoleText(s) {
  return getRoleCode(s) === "ADMIN" ? "Admin" : "Nhân viên";
}

/** ===== Gender + date ===== */
function mapGender(v) {
  if (v === true) return "Nam";
  if (v === false) return "Nữ";
  return "Khác";
}
function formatDate(d) {
  if (!d) return "-";
  return String(d).slice(0, 10);
}
function formatDateTime(d) {
  if (!d) return "-";
  const s = String(d);
  return s.replace("T", " ").slice(0, 19);
}

/** ===== Avatar ===== */
const FALLBACK_BACKEND = "http://localhost:8080";

function getBackendOrigin() {
  const base = String((http?.defaults?.baseURL || "")).trim();
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
  if (e?.target) e.target.src = "";
}

function getInitials(name) {
  const s = String(name || "").trim();
  if (!s) return "NV";
  const parts = s.split(/\s+/).filter(Boolean);
  const a = parts[0]?.[0] || "N";
  const b = parts[parts.length - 1]?.[0] || "V";
  return (a + b).toUpperCase();
}

/** ===== Fetch detail ===== */
async function fetchDetail() {
  loading.value = true;
  errorMsg.value = "";
  try {
    const id = route.params.id;
    const res = await http.get(`/api/nhan-vien/${id}`);
    staff.value = normalizeStaff(unwrapOne(res.data));
  } catch (e) {
    try {
      const id = Number(route.params.id);
      const res2 = await http.get(`/api/nhan-vien`);
      const arr = Array.isArray(res2.data) ? res2.data : (res2.data?.content || res2.data?.result || []);
      const found = (arr || []).find((x) => Number(x.id) === id);
      staff.value = found ? normalizeStaff(found) : null;
      if (!staff.value) errorMsg.value = "Không tìm thấy nhân viên.";
    } catch (e2) {
      console.error(e2);
      errorMsg.value = "Không tải được thông tin nhân viên.";
    }
  } finally {
    loading.value = false;
  }
}

/** ===== Buttons ===== */
function goBack() {
  try {
    router.push({ name: "staff-list" });
  } catch {
    router.back();
  }
}
function goEdit() {
  if (!isAdmin.value) {
    toast.warning("Chỉ Admin mới được sửa.");
    return;
  }
  router.push({ name: "staff-edit", params: { id: route.params.id } });
}

/** ===== Toggle status + confirm modal ===== */
const confirmModalRef = ref(null);
let bsConfirmModal = null;

const pendingNext = ref(false);
const confirmLoading = ref(false);
const confirmError = ref("");

function openConfirm() {
  const el = confirmModalRef.value;
  if (!el) return;
  document.querySelectorAll(".modal-backdrop").forEach((b) => b.remove());
  document.body.classList.remove("modal-open");
  const Modal = window.bootstrap?.Modal;
  if (Modal) {
    bsConfirmModal = Modal.getOrCreateInstance(el);
    bsConfirmModal.show();
  } else {
    el.classList.add("show");
    el.style.display = "block";
  }
}
function closeConfirm() {
  confirmError.value = "";
  confirmLoading.value = false;

  const el = confirmModalRef.value;
  if (!el) return;

  if (bsConfirmModal) {
    bsConfirmModal.hide();
    return;
  }
  el.classList.remove("show");
  el.style.display = "none";
  el.setAttribute("aria-hidden", "true");
  document.body.classList.remove("modal-open");
  const backdrop = document.querySelector(".modal-backdrop");
  if (backdrop) backdrop.remove();
}

function askToggle() {
  if (!isAdmin.value) {
    toast.warning("Chỉ Admin mới được đổi trạng thái.");
    return;
  }
  if (!staff.value) return;
  pendingNext.value = !staff.value.trangThai;
  confirmError.value = "";
  openConfirm();
}

async function confirmToggle() {
  if (!staff.value) return;

  confirmLoading.value = true;
  confirmError.value = "";
  try {
    await http.patch(`/api/nhan-vien/${staff.value.id}/trang-thai`, { trangThai: pendingNext.value });
    staff.value.trangThai = pendingNext.value;
    toast.success("Cập nhật trạng thái thành công!");
    closeConfirm();
  } catch (e) {
    console.error(e);
    confirmError.value = "Cập nhật thất bại. Vui lòng thử lại.";
    toast.error("Cập nhật trạng thái thất bại.");
  } finally {
    confirmLoading.value = false;
  }
}

/** ===== Toast style ===== */
function toastClass(type) {
  switch (String(type || "").toLowerCase()) {
    case "success":
      return "text-bg-success";
    case "error":
      return "text-bg-danger";
    case "warning":
      return "text-bg-warning";
    case "info":
    default:
      return "text-bg-primary";
  }
}

onMounted(fetchDetail);
</script>

<style scoped>
.card {
  border-radius: 14px;
}

.avatar-wrap {
  width: 92px;
  height: 92px;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eef2ff;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-fallback {
  font-weight: 800;
  color: #1d4ed8;
  font-size: 18px;
}

.info-box {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 12px 14px;
  background: #fff;
}

.label {
  font-weight: 600;
  color: #6b7280;
  margin-bottom: 6px;
}

.value {
  font-weight: 400;
  color: #111827;
}

.value.addr {
  white-space: normal;
  word-break: break-word;
}

/* ✅ CHỈ SỬA PHẦN BẠN YÊU CẦU: bỏ 2 đường kẻ trong popup confirm */
.confirm-modal .modal-header,
.confirm-modal .modal-footer {
  border: 0 !important;
}
</style>
