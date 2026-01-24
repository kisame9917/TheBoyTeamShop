<template>
  <div class="container-fluid py-3">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center gap-2">
        <h5 class="mb-0">
          Quản lý khách hàng / {{ isEdit ? "Cập nhật" : "Thêm mới" }}
        </h5>
      </div>

      <button class="btn btn-outline-secondary btn-sm" type="button" @click="goBack">
        <i class="bi bi-arrow-left me-1"></i> Quay lại danh sách
      </button>
    </div>

    <div class="card shadow-sm">
      <div class="card-body">
        <!-- Avatar center -->
        <div class="d-flex flex-column align-items-center mb-4">
          <div class="avatar-wrap" @click="triggerPickFile" title="Bấm để chọn ảnh">
            <img
                v-if="avatarPreview"
                :src="avatarPreview"
                class="avatar-img"
                alt="avatar"
                @error="onAvatarImgError"
            />
            <div v-else class="avatar-fallback">
              <i class="bi bi-person-fill fs-2"></i>
            </div>

            <button
                v-if="avatarPreview"
                type="button"
                class="avatar-remove"
                @click.stop="clearAvatar"
                title="Xóa ảnh"
            >
              ×
            </button>

            <div v-if="uploading" class="avatar-uploading">⏳</div>

            <input
                ref="fileInput"
                class="avatar-input"
                type="file"
                accept="image/png,image/jpeg,image/jpg,image/webp"
                @change="onAvatarFileChange"
            />
          </div>

          <div class="text-muted small mt-2">
            Bấm vào ảnh để chọn (png/jpg/webp). Ảnh sẽ upload lên server.
          </div>
        </div>

        <form @submit.prevent="submit">
          <div class="row g-3">
            <!-- Row 1 -->
            <div class="col-12 col-lg-6">
              <label class="form-label">Mã khách hàng</label>
              <input class="form-control" v-model="form.maKhachHang" disabled />
            </div>

            <div class="col-12 col-lg-6">
              <label class="form-label">Trạng thái</label>
              <select class="form-select" v-model="form.trangThai">
                <option :value="true">Hoạt động</option>
                <option :value="false">Không hoạt động</option>
              </select>
            </div>

            <!-- Row 2 -->
            <div class="col-12 col-lg-6">
              <label class="form-label">
                Tên khách hàng <span class="text-danger">*</span>
              </label>
              <input class="form-control" v-model="form.tenKhachHang" placeholder="Nhập tên khách hàng" />
            </div>

            <div class="col-12 col-lg-6">
              <label class="form-label">Giới tính</label>
              <div class="d-flex gap-3 mt-2">
                <div class="form-check">
                  <input class="form-check-input" type="radio" id="gt_nam" v-model="form.gioiTinh" :value="true" />
                  <label class="form-check-label" for="gt_nam">Nam</label>
                </div>
                <div class="form-check">
                  <input class="form-check-input" type="radio" id="gt_nu" v-model="form.gioiTinh" :value="false" />
                  <label class="form-check-label" for="gt_nu">Nữ</label>
                </div>
              </div>
            </div>

            <!-- Row 3 -->
            <div class="col-12 col-lg-6">
              <label class="form-label">Số điện thoại</label>
              <input class="form-control" v-model="form.soDienThoai" placeholder="VD: 0912345678" />
            </div>

            <div class="col-12 col-lg-6">
              <label class="form-label">Email</label>
              <input class="form-control" v-model="form.email" placeholder="VD: abc@gmail.com" />
            </div>

            <!-- Row 4 -->
            <div class="col-12 col-lg-6">
              <label class="form-label">
                Tài khoản <span class="text-danger">*</span>
              </label>
              <input class="form-control" v-model="form.taiKhoan" placeholder="Nhập tài khoản" />
            </div>

            <div class="col-12 col-lg-6">
              <label class="form-label">Mật khẩu</label>
              <input
                  class="form-control"
                  type="password"
                  v-model="form.matKhau"
                  :placeholder="isEdit ? '(để trống = giữ nguyên)' : 'Nhập mật khẩu'"
              />
            </div>

            <!-- Address group title -->
            <div class="col-12">
              <div class="fw-bold mt-2">Địa chỉ mặc định</div>
            </div>

            <!-- Row 5 -->
            <div class="col-12 col-lg-6">
              <label class="form-label">Người nhận</label>
              <input class="form-control" v-model="addr.receiverName" placeholder="Tên người nhận" />
            </div>

            <div class="col-12 col-lg-6">
              <label class="form-label">SĐT người nhận</label>
              <input class="form-control" v-model="addr.receiverPhone" placeholder="SĐT người nhận" />
            </div>

            <!-- Row 6 -->
            <div class="col-12 col-lg-6">
              <label class="form-label">Tỉnh/Thành phố</label>
              <select class="form-select" v-model="addr.provinceCode" @change="onProvinceChange">
                <option value="">-- Chọn Tỉnh/Thành phố --</option>
                <option v-for="p in provinces" :key="p.code" :value="String(p.code)">{{ p.name }}</option>
              </select>
            </div>

            <div class="col-12 col-lg-6">
              <label class="form-label">Quận/Huyện</label>
              <select
                  class="form-select"
                  v-model="addr.districtCode"
                  @change="onDistrictChange"
                  :disabled="!addr.provinceCode"
              >
                <option value="">-- Chọn Quận/Huyện --</option>
                <option v-for="d in districts" :key="d.code" :value="String(d.code)">{{ d.name }}</option>
              </select>
            </div>

            <!-- Row 7 -->
            <div class="col-12 col-lg-6">
              <label class="form-label">Phường/Xã</label>
              <select class="form-select" v-model="addr.wardCode" :disabled="!addr.districtCode">
                <option value="">-- Chọn Phường/Xã --</option>
                <option v-for="w in wards" :key="w.code" :value="String(w.code)">{{ w.name }}</option>
              </select>
            </div>

            <div class="col-12 col-lg-6">
              <label class="form-label">Địa chỉ chi tiết</label>
              <input class="form-control" v-model="addr.detail" placeholder="VD: 12 Cửa Đông" />
            </div>

            <!-- Error -->
            <div class="col-12" v-if="err">
              <div class="alert alert-danger mb-0">{{ err }}</div>
            </div>

            <!-- Actions -->
            <div class="col-12 d-flex justify-content-end gap-2 mt-2">
              <button class="btn btn-outline-secondary" type="button" @click="goBack">
                Hủy
              </button>

              <button class="btn btn-primary text-white" type="submit" :disabled="saving || uploading">
                {{ isEdit ? "Lưu thay đổi" : "Thêm mới" }}
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import http from "../../services/http";

const route = useRoute();
const router = useRouter();
const isEdit = computed(() => !!route.params.id);

const saving = ref(false);
const uploading = ref(false);
const err = ref("");

/** ===== Form ===== */
const form = reactive({
  id: null,
  maKhachHang: "",
  tenKhachHang: "",
  gioiTinh: true, // ảnh bạn mặc định Nam
  email: "",
  soDienThoai: "",
  taiKhoan: "",
  matKhau: "",
  trangThai: true,
  anhDaiDien: "",
});

/** ===== Address default ===== */
const provinces = ref([]);
const districts = ref([]);
const wards = ref([]);

const addr = reactive({
  receiverName: "",
  receiverPhone: "",
  provinceCode: "",
  districtCode: "",
  wardCode: "",
  detail: "",
});

async function fetchProvinces() {
  const r = await fetch("https://provinces.open-api.vn/api/p/");
  provinces.value = await r.json();
}
async function fetchDistricts(provinceCode) {
  const r = await fetch("https://provinces.open-api.vn/api/p/" + provinceCode + "?depth=2");
  const data = await r.json();
  districts.value = data?.districts || [];
}
async function fetchWards(districtCode) {
  const r = await fetch("https://provinces.open-api.vn/api/d/" + districtCode + "?depth=2");
  const data = await r.json();
  wards.value = data?.wards || [];
}

async function onProvinceChange() {
  addr.districtCode = "";
  addr.wardCode = "";
  wards.value = [];
  if (addr.provinceCode) await fetchDistricts(addr.provinceCode);
  else districts.value = [];
}
async function onDistrictChange() {
  addr.wardCode = "";
  if (addr.districtCode) await fetchWards(addr.districtCode);
  else wards.value = [];
}

/** build diaChi string */
function buildDiaChiText() {
  let p = null, d = null, w = null;

  for (let i = 0; i < provinces.value.length; i++) {
    if (String(provinces.value[i].code) === String(addr.provinceCode)) { p = provinces.value[i]; break; }
  }
  for (let i = 0; i < districts.value.length; i++) {
    if (String(districts.value[i].code) === String(addr.districtCode)) { d = districts.value[i]; break; }
  }
  for (let i = 0; i < wards.value.length; i++) {
    if (String(wards.value[i].code) === String(addr.wardCode)) { w = wards.value[i]; break; }
  }

  const parts = [];
  if (addr.detail && String(addr.detail).trim()) parts.push(String(addr.detail).trim());
  if (w?.name) parts.push(w.name);
  if (d?.name) parts.push(d.name);
  if (p?.name) parts.push(p.name);
  return parts.join(", ");
}

/** ===== Avatar ===== */
const fileInput = ref(null);
const avatarPreview = ref("");
const localBlobUrl = ref("");

function triggerPickFile() {
  fileInput.value?.click?.();
}

function revokeLocalBlob() {
  if (localBlobUrl.value && String(localBlobUrl.value).startsWith("blob:")) {
    URL.revokeObjectURL(localBlobUrl.value);
  }
  localBlobUrl.value = "";
}

function clearAvatar() {
  revokeLocalBlob();
  avatarPreview.value = "";
  form.anhDaiDien = "";
  if (fileInput.value) fileInput.value.value = "";
}

function onAvatarImgError() {
  if (localBlobUrl.value) avatarPreview.value = localBlobUrl.value;
  else avatarPreview.value = "";
}

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

async function uploadCustomerAvatar(file) {
  // ✅ nếu endpoint BE khác, đổi tại đây
  const fd = new FormData();
  fd.append("file", file);
  const res = await http.post("/api/upload/khach-hang-avatar", fd, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  const url = res?.data?.url || "";
  if (!url) throw new Error("Upload thành công nhưng không nhận được url");
  return String(url);
}

async function onAvatarFileChange(e) {
  const files = e?.target?.files || null;
  const file = files && files.length ? files[0] : null;
  if (!file) return;

  const okType = ["image/png", "image/jpeg", "image/jpg", "image/webp"].includes(file.type);
  if (!okType) return alert("Chỉ chấp nhận PNG, JPG, JPEG, WEBP");
  if (file.size > 5 * 1024 * 1024) return alert("Tối đa 5MB");

  revokeLocalBlob();
  localBlobUrl.value = URL.createObjectURL(file);
  avatarPreview.value = localBlobUrl.value;

  uploading.value = true;
  try {
    const url = await uploadCustomerAvatar(file);
    form.anhDaiDien = url;
    avatarPreview.value = resolveFileUrl(url) + "?t=" + Date.now();
  } catch (ex) {
    alert(ex?.response?.data?.message || ex?.message || "Upload thất bại");
  } finally {
    uploading.value = false;
  }
}

/** ===== API normalize ===== */
function normalizeCustomer(x) {
  x = x || {};
  return {
    id: x.id ?? null,
    maKhachHang: x.maKhachHang ?? x.ma_khach_hang ?? "",
    tenKhachHang: x.tenKhachHang ?? x.ten_khach_hang ?? "",
    gioiTinh: x.gioiTinh ?? x.gioi_tinh ?? true,
    email: x.email ?? "",
    soDienThoai: x.soDienThoai ?? x.so_dien_thoai ?? "",
    taiKhoan: x.taiKhoan ?? x.tai_khoan ?? "",
    trangThai: x.trangThai ?? x.trang_thai ?? true,
    anhDaiDien: x.anhDaiDien ?? x.anh_dai_dien ?? "",
    diaChiMacDinh: x.diaChiMacDinh ?? null, // nếu BE có
  };
}

function unwrapList(data) {
  if (!data) return [];
  if (Array.isArray(data)) return data;
  if (data.result && Array.isArray(data.result)) return data.result;
  if (data.content && Array.isArray(data.content)) return data.content;
  return [];
}

async function apiGetAllCustomers() {
  const res = await http.get("/api/khach-hang");
  return unwrapList(res?.data);
}
async function apiGetCustomerById(id) {
  const res = await http.get("/api/khach-hang/" + id);
  return res?.data || null;
}

function generateNextCode(all) {
  let maxN = 0;
  for (let i = 0; i < (all || []).length; i++) {
    const c = normalizeCustomer(all[i]);
    const m = String(c.maKhachHang || "").match(/^KH(\d+)$/i);
    if (m) {
      const n = parseInt(m[1], 10);
      if (!isNaN(n)) maxN = Math.max(maxN, n);
    }
  }
  return "KH" + String(maxN + 1).padStart(3, "0");
}

function isDigitsOnly(v) {
  const s = String(v == null ? "" : v).trim();
  return s.length > 0 && /^\d+$/.test(s);
}

function validateForm() {
  if (!String(form.tenKhachHang || "").trim()) return "Tên khách hàng không được trống";
  if (!String(form.taiKhoan || "").trim()) return "Tài khoản không được trống";
  if (!String(form.soDienThoai || "").trim() || !isDigitsOnly(form.soDienThoai)) return "Số điện thoại phải là số";

  if (!isEdit.value && !String(form.matKhau || "").trim()) return "Mật khẩu không được trống khi thêm mới";

  // địa chỉ mặc định
  if (!addr.receiverName.trim()) return "Vui lòng nhập Người nhận";
  if (!addr.receiverPhone.trim() || !isDigitsOnly(addr.receiverPhone)) return "SĐT người nhận phải là số";
  if (!addr.provinceCode || !addr.districtCode || !addr.wardCode || !String(addr.detail || "").trim()) {
    return "Vui lòng chọn đầy đủ Tỉnh/Quận/Xã và nhập địa chỉ chi tiết";
  }

  return "";
}

function goBack() {
  router.push({ name: "customer" });
}

/** ===== Load ===== */
async function loadData() {
  await fetchProvinces();
  const all = await apiGetAllCustomers();

  if (!isEdit.value) {
    form.maKhachHang = generateNextCode(all);
    form.trangThai = true;
    return;
  }

  const id = route.params.id;
  const detail = normalizeCustomer(await apiGetCustomerById(id));

  form.id = detail.id;
  form.maKhachHang = detail.maKhachHang;
  form.tenKhachHang = detail.tenKhachHang;
  form.gioiTinh = detail.gioiTinh === true || detail.gioiTinh === false ? detail.gioiTinh : true;
  form.email = detail.email || "";
  form.soDienThoai = detail.soDienThoai || "";
  form.taiKhoan = detail.taiKhoan || "";
  form.matKhau = "";
  form.trangThai = detail.trangThai === true || detail.trangThai === false ? detail.trangThai : true;
  form.anhDaiDien = detail.anhDaiDien || "";

  if (form.anhDaiDien) avatarPreview.value = resolveFileUrl(form.anhDaiDien) + "?t=" + Date.now();

  // Nếu BE trả diaChiMacDinh -> fill 2 field receiver
  if (detail.diaChiMacDinh) {
    addr.receiverName = String(detail.diaChiMacDinh.tenNguoiNhan || "");
    addr.receiverPhone = String(detail.diaChiMacDinh.soDienThoai || "");
  }
}

/** ===== Submit ===== */
async function submit() {
  err.value = "";

  const msg = validateForm();
  if (msg) {
    err.value = msg;
    return;
  }

  const actionText = isEdit.value ? "cập nhật" : "thêm mới";
  if (!confirm("Bạn có chắc chắn muốn " + actionText + " khách hàng không?")) return;

  saving.value = true;
  try {
    const payload = {
      maKhachHang: String(form.maKhachHang || "").trim(),
      tenKhachHang: String(form.tenKhachHang || "").trim(),
      gioiTinh: form.gioiTinh === true || form.gioiTinh === false ? form.gioiTinh : null,
      email: String(form.email || "").trim() ? String(form.email || "").trim() : null,
      soDienThoai: String(form.soDienThoai || "").trim(),
      taiKhoan: String(form.taiKhoan || "").trim(),
      trangThai: form.trangThai,
      anhDaiDien: form.anhDaiDien,

      // địa chỉ mặc định
      diaChi: buildDiaChiText(),
      diaChiMacDinh: {
        tenNguoiNhan: String(addr.receiverName || "").trim(),
        soDienThoai: String(addr.receiverPhone || "").trim(),
      },
    };

    if (!isEdit.value) payload.matKhau = String(form.matKhau || "").trim();
    else if (String(form.matKhau || "").trim()) payload.matKhau = String(form.matKhau || "").trim();

    if (isEdit.value) {
      await http.put("/api/khach-hang/" + route.params.id, payload);
    } else {
      await http.post("/api/khach-hang", payload);
    }

    goBack();
  } catch (e) {
    err.value = e?.response?.data?.message || e?.message || "Có lỗi xảy ra";
  } finally {
    saving.value = false;
  }
}

onMounted(loadData);
onBeforeUnmount(() => revokeLocalBlob());
</script>

<style scoped>
.card {
  border-radius: 14px;
}

/* Avatar */
.avatar-wrap {
  width: 72px;
  height: 72px;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  background: #f8fafc;
  overflow: hidden;
  position: relative;
  cursor: pointer;
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
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
}
.avatar-remove {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 22px;
  height: 22px;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  background: #fff;
  cursor: pointer;
  line-height: 18px;
}
.avatar-uploading {
  position: absolute;
  bottom: 6px;
  right: 6px;
  width: 22px;
  height: 22px;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}
.avatar-input {
  display: none;
}
</style>
