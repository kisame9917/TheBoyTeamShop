<template>
  <div class="container-fluid py-3">
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-person-vcard fs-4"></i>
        <h5 class="mb-0">Chi tiết nhân viên</h5>
      </div>

      <div class="d-flex gap-2">
        <button type="button" class="btn btn-outline-secondary btn-sm" @click="goBack">
          <i class="bi bi-arrow-left me-1"></i> Quay lại
        </button>
        <button type="button" class="btn btn-primary btn-sm text-white" @click="goEdit">
          <i class="bi bi-pencil-square me-1"></i> Chỉnh sửa
        </button>
      </div>
    </div>

    <div class="card shadow-sm border-0 detail-card" v-if="!loading && staff">
      <div class="card-body p-0">
        <div class="detail-header">
          <div class="d-flex flex-column flex-lg-row align-items-lg-center gap-3">
            <div class="avatar-wrap">
              <img
                  v-if="avatarUrl"
                  :src="avatarUrl"
                  class="avatar-img"
                  alt="avatar"
                  @error="avatarError = true"
              />
              <div v-else class="avatar-fallback">
                {{ getInitials(staff.tenNhanVien) }}
              </div>
            </div>

            <div class="flex-grow-1">
              <div class="staff-name">{{ staff.tenNhanVien || '-' }}</div>
              <div class="d-flex flex-wrap gap-2 mt-2">
                <span class="pill soft-blue">Mã NV: {{ staff.maNhanVien || '-' }}</span>
                <span class="pill soft-gray">Tài khoản: {{ staff.taiKhoan || '-' }}</span>
                <span
                    class="pill"
                    :class="staff.trangThai ? 'soft-green' : 'soft-red'"
                >
                  {{ staff.trangThai ? 'Đang hoạt động' : 'Ngừng hoạt động' }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div class="p-3 p-lg-4">
          <div class="row g-3">
            <div class="col-12 col-lg-6">
              <div class="info-box">
                <div class="label">Mã nhân viên</div>
                <div class="value">{{ staff.maNhanVien || "-" }}</div>
              </div>
            </div>

            <div class="col-12 col-lg-6">
              <div class="info-box">
                <div class="label">Tên nhân viên</div>
                <div class="value">{{ staff.tenNhanVien || "-" }}</div>
              </div>
            </div>

            <div class="col-12 col-lg-6">
              <div class="info-box">
                <div class="label">Tài khoản</div>
                <div class="value">{{ staff.taiKhoan || "-" }}</div>
              </div>
            </div>

            <div class="col-12 col-lg-6">
              <div class="info-box">
                <div class="label">Chức vụ</div>
                <div class="value">{{ staff.tenQuyenHan || "-" }}</div>
              </div>
            </div>

            <div class="col-12 col-lg-6">
              <div class="info-box">
                <div class="label">Số điện thoại</div>
                <div class="value">{{ staff.soDienThoai || "-" }}</div>
              </div>
            </div>

            <div class="col-12 col-lg-6">
              <div class="info-box">
                <div class="label">Email</div>
                <div class="value break-all">{{ staff.email || "-" }}</div>
              </div>
            </div>

            <div class="col-12 col-lg-6">
              <div class="info-box">
                <div class="label">Giới tính</div>
                <div class="value">{{ genderText }}</div>
              </div>
            </div>

            <div class="col-12 col-lg-6">
              <div class="info-box">
                <div class="label">Ngày sinh</div>
                <div class="value">{{ formatDate(staff.ngaySinh) }}</div>
              </div>
            </div>

            <div class="col-12">
              <div class="info-box">
                <div class="label">Địa chỉ</div>
                <div class="value">{{ staff.diaChi || "-" }}</div>
              </div>
            </div>

            <div class="col-12 col-lg-6">
              <div class="info-box">
                <div class="label">Ngày tạo</div>
                <div class="value">{{ formatDateTime(staff.ngayTao) }}</div>
              </div>
            </div>

            <div class="col-12 col-lg-6">
              <div class="info-box">
                <div class="label">Cập nhật lần cuối</div>
                <div class="value">{{ formatDateTime(staff.ngayCapNhat) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="loading" class="card shadow-sm border-0">
      <div class="card-body py-5 text-center text-muted">
        Đang tải dữ liệu...
      </div>
    </div>

    <div v-else class="card shadow-sm border-0">
      <div class="card-body py-5 text-center">
        <div class="mb-2 fw-semibold">Không tìm thấy nhân viên</div>
        <button type="button" class="btn btn-outline-secondary btn-sm" @click="goBack">
          Quay lại danh sách
        </button>
      </div>
    </div>

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
          <button
              type="button"
              class="btn-close btn-close-white me-2 m-auto"
              @click="toast.remove(t.id)"
          ></button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import http from "../../services/http";
import { useToast } from "@/composables/useToast";
import { resolveMediaUrl } from "@/utils/media";

const route = useRoute();
const router = useRouter();
const toast = useToast();

const loading = ref(true);
const staff = ref(null);
const avatarError = ref(false);

function unwrapObj(data) {
  if (!data) return null;
  if (data.result && typeof data.result === "object") return data.result;
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

async function loadDetail() {
  loading.value = true;
  try {
    const res = await http.get("/api/nhan-vien/" + route.params.id);
    staff.value = normalizeStaff(unwrapObj(res?.data));
  } catch (e) {
    staff.value = null;
    const m = e?.response?.data?.message || e?.message || "Không tải được chi tiết nhân viên";
    toast.error(m);
  } finally {
    loading.value = false;
  }
}

const avatarUrl = computed(() => {
  if (avatarError.value) return "";
  const url = String(staff.value?.anhDaiDien || "").trim();
  return url ? resolveMediaUrl(url) : "";
});

const genderText = computed(() => {
  if (staff.value?.gioiTinh === true) return "Nam";
  if (staff.value?.gioiTinh === false) return "Nữ";
  return "-";
});

function formatDate(v) {
  if (!v) return "-";
  const s = String(v).slice(0, 10);
  const m = s.match(/^(\d{4})-(\d{2})-(\d{2})$/);
  if (!m) return s;
  return `${m[3]}/${m[2]}/${m[1]}`;
}

function formatDateTime(v) {
  if (!v) return "-";
  const d = new Date(v);
  if (Number.isNaN(d.getTime())) return String(v);
  const dd = String(d.getDate()).padStart(2, "0");
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const yyyy = d.getFullYear();
  const hh = String(d.getHours()).padStart(2, "0");
  const mi = String(d.getMinutes()).padStart(2, "0");
  return `${dd}/${mm}/${yyyy} ${hh}:${mi}`;
}

function getInitials(name) {
  const s = String(name || "").trim();
  if (!s) return "NV";
  const parts = s.split(/\s+/).filter(Boolean);
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

function goBack() {
  router.push({ name: "staff" });
}

function goEdit() {
  router.push({ name: "staff-edit", params: { id: route.params.id } });
}

function toastClass(type) {
  const t = String(type || "info").toLowerCase();
  if (t === "success") return "text-bg-success";
  if (t === "error") return "text-bg-danger";
  if (t === "warning") return "text-bg-warning";
  return "text-bg-info";
}

onMounted(loadDetail);
</script>

<style scoped>
.detail-card {
  border-radius: 16px;
  overflow: hidden;
}

.detail-header {
  padding: 20px;
  background: linear-gradient(135deg, #eff6ff, #f8fafc);
  border-bottom: 1px solid #e5e7eb;
}

.avatar-wrap {
  width: 84px;
  height: 84px;
  min-width: 84px;
  border-radius: 999px;
  overflow: hidden;
  border: 1px solid #dbeafe;
  background: #ffffff;
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
  font-size: 24px;
  font-weight: 700;
  color: #1d4ed8;
}

.staff-name {
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
}

.pill {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 600;
}

.soft-blue {
  background: #dbeafe;
  color: #1d4ed8;
}

.soft-gray {
  background: #f3f4f6;
  color: #374151;
}

.soft-green {
  background: #dcfce7;
  color: #15803d;
}

.soft-red {
  background: #fee2e2;
  color: #b91c1c;
}

.info-box {
  height: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 14px 16px;
  background: #ffffff;
}

.label {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 6px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.value {
  font-size: 15px;
  color: #111827;
  font-weight: 600;
  line-height: 1.5;
  word-break: break-word;
}

.break-all {
  word-break: break-all;
}
</style>