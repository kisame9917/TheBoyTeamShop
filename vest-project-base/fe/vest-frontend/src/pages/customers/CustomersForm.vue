<template>
  <div class="customer-form-page">
    <div class="page-head">
      <div class="page-title">
        <h2>Quản lý khách hàng / <span>{{ isEdit ? "Cập nhật" : "Thêm mới" }}</span></h2>
      </div>

      <!-- ✅ yêu cầu: có popup xác nhận khi hủy -->
      <button type="button" class="btn-back" @click="onCancel">← Quay lại danh sách</button>
    </div>

    <div class="card">
      <form @submit.prevent="submit">
        <div class="avatar-zone">
          <div class="avatar-wrap" :class="{ 'avatar-wrap--disabled': isViewLocked }" @click="openFilePicker" title="Bấm để chọn ảnh">
            <img v-if="avatarPreview" :src="avatarPreview" class="avatar-img" alt="avatar" />
            <div v-else class="avatar-fallback">👤</div>
          </div>
          <div class="avatar-hint">Bấm vào ảnh để chọn (png/jpg/webp). Ảnh sẽ upload lên server.</div>

          <input
              ref="fileInput"
              type="file"
              accept="image/png,image/jpeg,image/jpg,image/webp"
              class="hidden-file"
              @change="onFileChange"
              :disabled="isViewLocked"
          />
        </div>

        <div class="form-grid">
          <!-- Row 1: Mã KH, Tên KH -->
          <div class="form-group span-3">
            <label>Mã khách hàng</label>
            <input class="form-input input-disabled" v-model="form.maKhachHang" disabled />
          </div>

          <div class="form-group span-3">
            <label>Tên khách hàng <span class="req">*</span></label>
            <input class="form-input" v-model="form.tenKhachHang" placeholder="Nhập tên khách hàng" :disabled="isViewLocked" />
          </div>

          <!-- Row 2: SĐT, Email -->
          <div class="form-group span-3">
            <label>Số điện thoại <span class="req">*</span></label>
            <input class="form-input" v-model="form.soDienThoai" placeholder="VD: 0912345678" :disabled="isViewLocked" />
          </div>

          <div class="form-group span-3">
            <label>Email</label>
            <input class="form-input" v-model="form.email" placeholder="VD: abc@gmail.com" :disabled="isViewLocked" />
          </div>

          <!-- Row 3 -->
          <div class="form-group span-3 row3-item">
            <label>Ngày sinh</label>
            <div class="input-group date-input-group">
              <input ref="dobPickerRef" type="text" class="form-control" placeholder="dd/mm/yyyy" :disabled="isViewLocked" />
              <button class="btn btn-outline-secondary" type="button" @click="openDobPicker" title="Chọn ngày" :disabled="isViewLocked">
                <i class="bi bi-calendar3"></i>
              </button>
              <button class="btn btn-outline-secondary" type="button" @click="clearDob" title="Xóa" :disabled="isViewLocked">
                <i class="bi bi-x-lg"></i>
              </button>
            </div>
          </div>

          <div class="form-group span-3 row3-item group-right">
            <div class="gs-row">
              <div class="gs-item">
                <label>Giới tính</label>
                <div class="radio-row">
                  <label class="radio-item">
                    <input type="radio" :value="true" v-model="form.gioiTinh" :disabled="isViewLocked" /> Nam
                  </label>
                  <label class="radio-item">
                    <input type="radio" :value="false" v-model="form.gioiTinh" :disabled="isViewLocked" /> Nữ
                  </label>
                </div>
              </div>

              <div class="gs-item">
                <label>Trạng thái</label>
                <div class="radio-row">
                  <label class="radio-item">
                    <input type="radio" :value="true" v-model="form.trangThai" :disabled="isViewLocked" /> Hoạt động
                  </label>
                  <label class="radio-item">
                    <input type="radio" :value="false" v-model="form.trangThai" :disabled="isViewLocked" /> Không hoạt động
                  </label>
                </div>
              </div>
            </div>
          </div>

        </div>

        <div class="section-title">Quản lý địa chỉ</div>

        <div class="addr-box">
          <div class="addr-head">
            <div>
              <div class="addr-head-title">Thêm địa chỉ mới</div>
              <div class="addr-head-sub">Nhập thông tin địa chỉ để lưu và danh sách bên dưới.</div>
            </div>

            <button class="btn-add-addr" type="button" @click="openAddDiaChi" :disabled="isViewLocked || addrBusy || diaChiList.length >= 5">
              Thêm địa chỉ
            </button>
          </div>

          <div class="addr-list">
            <div
                v-for="a in diaChiList"
                :key="a.id"
                class="addr-item"
                :class="{
                'addr-item--default': !!a.laMacDinh,
                'addr-item--selected': String(selectedDiaChiId) === String(a.id)
              }"
            >
              <label class="addr-left">
                <input
                    class="form-check-input me-2"
                    type="radio"
                    name="diachi"
                    :value="a.id"
                    v-model="selectedDiaChiId"
                    @change="onPickDiaChi(a)"
                    :disabled="isViewLocked"
                />

                <div class="addr-content">
                  <div class="addr-top">
                    <b>{{ a.tenNguoiNhan || "Người nhận" }}</b>
                    <span class="text-muted">• {{ a.soDienThoai || "-" }}</span>
                    <span v-if="a.laMacDinh" class="badge-default">Mặc định</span>
                  </div>

                  <div class="addr-sub">
                    {{ formatDiaChiText(a) }}
                  </div>
                </div>
              </label>

              <!-- ✅ yêu cầu: mỗi địa chỉ đều có nút Sửa & Xóa -->
              <div class="addr-actions">
                <button type="button" class="icon-btn" title="Sửa địa chỉ" @click.stop="openEditDiaChi(a)" :disabled="isViewLocked">
                  <i class="bi bi-pencil-square"></i>
                </button>
                <button type="button" class="icon-btn icon-btn-danger" title="Xóa địa chỉ" @click.stop="deleteDiaChi(a)" :disabled="isViewLocked">
                  <i class="bi bi-trash"></i>
                </button>
              </div>
            </div>

            <div v-if="diaChiList.length === 0" class="addr-empty">
              Chưa có địa chỉ nào. Nhấn <b>Thêm địa chỉ</b> để tạo mới.
            </div>

            <div class="addr-foot">Tối đa 5 địa chỉ.</div>
          </div>
        </div>

        <div class="actions-row">
          <div class="me-auto fst-italic">Vui lòng điền đầy đủ các thông tin.</div>
          <div class="actions">
            <!-- ✅ yêu cầu: hủy có confirm -->
            <button type="button" class="btn btn-ghost" @click="onCancel" :disabled="saving">Hủy</button>

            <button type="submit" class="btn btn-primary" :disabled="isViewLocked || saving">
              {{ saving ? "Đang lưu..." : (isEdit ? "Lưu thay đổi" : "Thêm mới") }}
            </button>
          </div>
        </div>
      </form>
    </div>

    <div v-if="addrModal.open" class="modal-overlay" @click.self="closeAddrModal()">
      <div class="modal-card" style="width: min(720px, calc(100% - 32px))">
        <h3 class="modal-title">{{ addrModal.mode === "add" ? "Thêm địa chỉ" : "Sửa địa chỉ" }}</h3>
        <p class="modal-desc">Nhập đầy đủ thông tin địa chỉ.</p>

        <div class="addr-form-grid">
          <div class="form-group">
            <label>Người nhận <span class="req">*</span></label>
            <input class="form-input" v-model="addrModal.form.tenNguoiNhan" placeholder="Tên người nhận" :disabled="isViewLocked" />
          </div>

          <div class="form-group">
            <label>SĐT người nhận <span class="req">*</span></label>
            <input class="form-input" v-model="addrModal.form.soDienThoai" placeholder="Chỉ nhập số" :disabled="isViewLocked" />
          </div>

          <div class="form-group">
            <label>Tỉnh/Thành phố <span class="req">*</span></label>
            <select class="form-input" v-model="addrModal.form.tinhThanh" @change="onAddrProvinceChange" :disabled="isViewLocked">
              <option value="">-- Chọn Tỉnh/Thành phố --</option>
              <option v-for="p in provinces" :key="p.code" :value="p.name">{{ p.name }}</option>
            </select>
          </div>

          <div class="form-group">
            <label>Quận/Huyện <span class="req">*</span></label>
            <select class="form-input" v-model="addrModal.form.quanHuyen" @change="onAddrDistrictChange" :disabled="isViewLocked || addrDistricts.length === 0">
              <option value="">-- Chọn Quận/Huyện --</option>
              <option v-for="d in addrDistricts" :key="d.code" :value="d.name">{{ d.name }}</option>
            </select>
          </div>

          <div class="form-group">
            <label>Phường/Xã <span class="req">*</span></label>
            <select class="form-input" v-model="addrModal.form.phuongXa" :disabled="isViewLocked || addrWards.length === 0">
              <option value="">-- Chọn Phường/Xã --</option>
              <option v-for="w in addrWards" :key="w.code" :value="w.name">{{ w.name }}</option>
            </select>
          </div>

          <div class="form-group">
            <label>Địa chỉ chi tiết <span class="req">*</span></label>
            <input class="form-input" v-model="addrModal.form.diaChiChiTiet" placeholder="VD: 12 Cầu Giấy" :disabled="isViewLocked" />
          </div>
        </div>

        <div class="check-row">
          <label class="check-item">
            <input type="checkbox" v-model="addrModal.form.isDefault" :disabled="isViewLocked" />
            Đặt làm mặc định
          </label>
        </div>

        <div class="modal-actions">
          <button class="btn btn-outline" type="button" @click="closeAddrModal()" :disabled="addrModal.loading">Hủy</button>
          <button class="btn btn-confirm" type="button" @click="saveDiaChi" :disabled="isViewLocked || addrModal.loading">
            {{ addrModal.loading ? "Đang lưu..." : "Lưu" }}
          </button>
        </div>
      </div>
    </div>

    <!-- ✅ Confirm popup theo yêu cầu (hủy / lưu thay đổi / xóa / lưu địa chỉ) -->
    <div v-if="confirm.open" class="modal-overlay" @click.self="closeConfirm">
      <div class="modal-card">
        <h3 class="modal-title">{{ confirm.title }}</h3>
        <p class="modal-desc">{{ confirm.message }}</p>

        <div class="modal-actions">
          <button class="btn btn-outline" type="button" @click="closeConfirm" :disabled="confirm.loading">Hủy</button>
          <button class="btn btn-confirm" type="button" @click="confirmOk" :disabled="confirm.loading">
            {{ confirm.loading ? "Đang xử lý..." : "Đồng ý" }}
          </button>
        </div>
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
          <button type="button" class="btn-close btn-close-white me-2 m-auto" @click="toast.remove(t.id)"></button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import http from "../../services/http";
import { useToast } from "@/composables/useToast";
import { useAuthStore } from "@/stores/auth";
import { useShiftStore } from "@/stores/shift";

// Date picker (UI/UX giống VoucherList)
import flatpickr from "flatpickr";
import { Vietnamese } from "flatpickr/dist/l10n/vn.js";
import "flatpickr/dist/flatpickr.css";

const router = useRouter();
const route = useRoute();
const toast = useToast();

const auth = useAuthStore();
const shift = useShiftStore();
const isViewLocked = computed(() => auth.isStaff && shift.isLocked);

function blockIfViewMode() {
  if (!isViewLocked.value) return false;
  toast.info("Bạn đang ở chế độ xem. Không thể tạo/chỉnh sửa khách hàng.");
  return true;
}

const id = computed(() => route.params.id);
const isEdit = computed(() => !!id.value);
const saving = ref(false);

/** ===== CONFIRM POPUP (theo yêu cầu) ===== */
const confirm = reactive({
  open: false,
  loading: false,
  title: "Xác nhận",
  message: "",
  onOk: null,
});

function openConfirm({ title = "Xác nhận", message = "", onOk = null } = {}) {
  confirm.title = title;
  confirm.message = message;
  confirm.onOk = onOk;
  confirm.loading = false;
  confirm.open = true;
}

function closeConfirm() {
  if (confirm.loading) return;
  confirm.open = false;
  confirm.loading = false;
  confirm.title = "Xác nhận";
  confirm.message = "";
  confirm.onOk = null;
}

async function confirmOk() {
  try {
    confirm.loading = true;
    if (typeof confirm.onOk === "function") await confirm.onOk();
  } finally {
    confirm.loading = false;
    closeConfirm();
  }
}

// ===== form =====
const form = reactive({
  maKhachHang: "",
  tenKhachHang: "",
  gioiTinh: true,
  ngaySinh: "",
  soDienThoai: "",
  email: "",
  taiKhoan: "",
  matKhau: "",
  trangThai: true,
  anhDaiDien: "",
});

// ===== Date picker: Ngày sinh (flatpickr) =====
const dobPickerRef = ref(null);
let fpDob = null;

function parseYMD(ymd) {
  if (!ymd) return null;
  const raw = String(ymd).trim();
  if (!raw) return null;
  const s = raw.includes("T") ? raw.split("T")[0] : raw;
  const [y, m, d] = String(s).split("-").map(Number);
  if (!y || !m || !d) return null;
  return new Date(y, m - 1, d);
}

function initDobPicker() {
  if (dobPickerRef.value && !fpDob) {
    fpDob = flatpickr(dobPickerRef.value, {
      locale: Vietnamese,
      dateFormat: "d/m/Y",
      allowInput: true,
      defaultDate: parseYMD(form.ngaySinh),
      onChange: (selectedDates) => {
        const d = selectedDates?.[0] || null;
        form.ngaySinh = d ? flatpickr.formatDate(d, "Y-m-d") : "";
      },
    });
  }
}

function openDobPicker() {
  if (blockIfViewMode()) return;
  initDobPicker();
  fpDob?.open();
}

function clearDob() {
  if (blockIfViewMode()) return;
  form.ngaySinh = "";
  fpDob?.clear();
}

watch(
    () => form.ngaySinh,
    (v) => {
      if (!fpDob) return;
      fpDob.setDate(parseYMD(v), false);
    }
);

// ===== avatar =====
const fileInput = ref(null);
const selectedFile = ref(null);
const avatarPreview = ref("");
let localBlobUrl = "";

function openFilePicker() {
  if (blockIfViewMode()) return;
  fileInput.value?.click();
}
function revokeBlob() {
  try {
    if (localBlobUrl && String(localBlobUrl).startsWith("blob:")) URL.revokeObjectURL(localBlobUrl);
  } catch {}
  localBlobUrl = "";
}
function onFileChange(e) {
  const file = e?.target?.files?.[0];
  if (!file) return;

  const okType = ["image/png", "image/jpeg", "image/jpg", "image/webp"].includes(file.type);
  if (!okType) return toast.warning("Chỉ chấp nhận PNG, JPG, JPEG, WEBP");
  if (file.size > 5 * 1024 * 1024) return toast.warning("Tối đa 5MB");

  selectedFile.value = file;
  revokeBlob();
  localBlobUrl = URL.createObjectURL(file);
  avatarPreview.value = localBlobUrl;
}

// ===== address data (provinces) =====
const provinces = ref([]);
const addrDistricts = ref([]);
const addrWards = ref([]);

async function loadProvinces() {
  try {
    const res = await fetch("https://provinces.open-api.vn/api/?depth=3");
    if (!res.ok) throw new Error("Load province failed");
    const data = await res.json();
    provinces.value = Array.isArray(data) ? data : [];
  } catch (e) {
    console.warn("API Tỉnh thành lỗi, dùng data rỗng");
    provinces.value = [];
  }
}

function onAddrProvinceChange() {
  const p = provinces.value.find((x) => x.name === addrModal.form.tinhThanh);
  addrDistricts.value = p?.districts || [];
  addrModal.form.quanHuyen = "";
  addrModal.form.phuongXa = "";
  addrWards.value = [];
}

function onAddrDistrictChange() {
  const p = provinces.value.find((x) => x.name === addrModal.form.tinhThanh);
  const d = (p?.districts || []).find((x) => x.name === addrModal.form.quanHuyen);
  addrWards.value = d?.wards || [];
  addrModal.form.phuongXa = "";
}

// ===== utils =====
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

// ✅ FIX LỖI BUTTON QUAY LẠI
function goBack() {
  router.back();
}

// ✅ yêu cầu: hủy có confirm
function onCancel() {
  openConfirm({
    title: "Xác nhận",
    message: "Bạn có chắc muốn hủy thao tác và quay lại danh sách không?",
    onOk: async () => {
      goBack();
    },
  });
}

function isDigitsOnly(v) {
  const s = String(v == null ? "" : v).trim();
  return s.length > 0 && /^\d+$/.test(s);
}

function validate() {
  if (!String(form.tenKhachHang || "").trim()) return "Tên khách hàng không được để trống";
  if (!String(form.soDienThoai || "").trim() || !isDigitsOnly(form.soDienThoai)) return "Số điện thoại phải là số";
  return "";
}

// ===== API (khách hàng) =====
async function fetchNextCode() {
  const res = await http.get("/api/khach-hang/next-code");
  form.maKhachHang = res?.data?.maKhachHang || "";
}

async function fetchDetail() {
  const res = await http.get("/api/khach-hang/" + id.value);
  const kh = res.data || {};

  form.maKhachHang = kh.maKhachHang || "";
  form.tenKhachHang = kh.tenKhachHang || "";
  form.gioiTinh = kh.gioiTinh !== null && kh.gioiTinh !== undefined ? kh.gioiTinh : true;
  form.ngaySinh = kh.ngaySinh || "";
  form.soDienThoai = kh.soDienThoai || "";
  form.email = kh.email || "";
  form.trangThai = kh.trangThai !== null && kh.trangThai !== undefined ? kh.trangThai : true;
  form.anhDaiDien = kh.anhDaiDien || "";

  // Sync date picker view after model is populated
  await nextTick();
  initDobPicker();
  fpDob?.setDate(parseYMD(form.ngaySinh), false);

  if (!selectedFile.value) avatarPreview.value = resolveFileUrl(form.anhDaiDien);

  await fetchDiaChiList();
}

async function uploadAvatarIfNeeded() {
  if (!selectedFile.value) return;
  const fd = new FormData();
  fd.append("file", selectedFile.value);
  const res = await http.post("/api/khach-hang/upload-avatar", fd, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  const url = res?.data?.url || "";
  if (url) {
    form.anhDaiDien = url;
    avatarPreview.value = resolveFileUrl(url) + "?t=" + Date.now();
  }
}

/** ===== ĐỊA CHỈ ===== */
const diaChiList = reactive([]);
const selectedDiaChiId = ref(null);
const addrBusy = ref(false);

function formatDiaChiText(a) {
  const parts = [a?.diaChiChiTiet, a?.phuongXa, a?.quanHuyen, a?.tinhThanh, a?.quocGia]
      .map((x) => String(x || "").trim())
      .filter(Boolean);
  return parts.join(", ");
}

function pickLocalDefault(idPick) {
  selectedDiaChiId.value = idPick;
  for (const x of diaChiList) x.laMacDinh = String(x.id) === String(idPick);
}

// API Wrappers
async function apiGetDiaChiList(khachHangId) {
  const res = await http.get(`/api/khach-hang/${khachHangId}/dia-chi`);
  const data = res?.data;
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.result)) return data.result;
  if (Array.isArray(data?.content)) return data.content;
  return [];
}
async function apiAddDiaChi(khachHangId, payload) {
  const res = await http.post(`/api/khach-hang/${khachHangId}/dia-chi`, payload);
  return res?.data;
}
async function apiUpdateDiaChi(khachHangId, diaChiId, payload) {
  const res = await http.put(`/api/khach-hang/${khachHangId}/dia-chi/${diaChiId}`, payload);
  return res?.data;
}
async function apiDeleteDiaChi(khachHangId, diaChiId) {
  await http.delete(`/api/khach-hang/${khachHangId}/dia-chi/${diaChiId}`);
}
async function apiSetDefaultDiaChi(khachHangId, diaChiId) {
  await http.put(`/api/khach-hang/${khachHangId}/dia-chi/${diaChiId}/mac-dinh`);
}

async function fetchDiaChiList() {
  if (!isEdit.value) return;
  addrBusy.value = true;
  try {
    const arr = await apiGetDiaChiList(id.value);
    diaChiList.splice(0, diaChiList.length, ...arr);
    const def = arr.find((x) => !!x.laMacDinh);
    selectedDiaChiId.value = def ? def.id : (arr[0]?.id ?? null);
  } catch (e) {
    console.error(e);
    toast.error("Không tải được địa chỉ khách hàng.");
  } finally {
    addrBusy.value = false;
  }
}

async function onPickDiaChi(a) {
  if (!a?.id) return;
  if (!isEdit.value) {
    pickLocalDefault(a.id);
    return;
  }
  try {
    await apiSetDefaultDiaChi(id.value, a.id);
    for (const x of diaChiList) x.laMacDinh = String(x.id) === String(a.id);
    toast.success("Cập nhật địa chỉ mặc định thành công!");
  } catch (e) {
    console.error(e);
    toast.error("Cập nhật địa chỉ mặc định thất bại!");
  }
}

// Modal Logic
const addrModal = reactive({
  open: false,
  mode: "add",
  loading: false,
  editingId: null,
  form: {
    tenNguoiNhan: "",
    soDienThoai: "",
    tinhThanh: "",
    quanHuyen: "",
    phuongXa: "",
    diaChiChiTiet: "",
    quocGia: "Việt Nam",
    isDefault: false,
  },
});

function resetAddrForm() {
  Object.assign(addrModal.form, {
    tenNguoiNhan: "",
    soDienThoai: "",
    tinhThanh: "",
    quanHuyen: "",
    phuongXa: "",
    diaChiChiTiet: "",
    quocGia: "Việt Nam",
    isDefault: false,
  });
  addrDistricts.value = [];
  addrWards.value = [];
}

function openAddDiaChi() {
  if (blockIfViewMode()) return;
  if (diaChiList.length >= 5) return toast.warning("Tối đa 5 địa chỉ.");
  addrModal.mode = "add";
  addrModal.editingId = null;
  addrModal.loading = false;
  resetAddrForm();
  addrModal.open = true;
}

function openEditDiaChi(a) {
  if (blockIfViewMode()) return;
  addrModal.mode = "edit";
  addrModal.editingId = a.id;
  addrModal.loading = false;
  Object.assign(addrModal.form, {
    tenNguoiNhan: a.tenNguoiNhan || "",
    soDienThoai: a.soDienThoai || "",
    tinhThanh: a.tinhThanh || "",
    quanHuyen: a.quanHuyen || "",
    phuongXa: a.phuongXa || "",
    diaChiChiTiet: a.diaChiChiTiet || "",
    quocGia: a.quocGia || "Việt Nam",
    isDefault: !!a.laMacDinh,
  });
  onAddrProvinceChange();
  addrModal.form.quanHuyen = a.quanHuyen || "";
  onAddrDistrictChange();
  addrModal.form.phuongXa = a.phuongXa || "";
  addrModal.open = true;
}

// ✅ FIX: cho phép đóng modal sau khi lưu thành công dù đang loading
function closeAddrModal(force = false) {
  if (addrModal.loading && !force) return;
  addrModal.open = false;
  addrModal.loading = false;
  addrModal.editingId = null;
}

function validateAddrForm() {
  if (!String(addrModal.form.tenNguoiNhan || "").trim()) return "Vui lòng nhập tên người nhận";
  if (!String(addrModal.form.soDienThoai || "").trim() || !isDigitsOnly(addrModal.form.soDienThoai)) return "SĐT người nhận phải là số";
  if (!String(addrModal.form.tinhThanh || "").trim()) return "Vui lòng chọn Tỉnh/Thành phố";
  if (!String(addrModal.form.quanHuyen || "").trim()) return "Vui lòng chọn Quận/Huyện";
  if (!String(addrModal.form.phuongXa || "").trim()) return "Vui lòng chọn Phường/Xã";
  if (!String(addrModal.form.diaChiChiTiet || "").trim()) return "Vui lòng nhập địa chỉ chi tiết";
  return "";
}

// ✅ FIX: sửa địa chỉ (trong chế độ thêm mới KH) không bị tự thêm địa chỉ mới
async function doSaveDiaChi() {
  addrModal.loading = true;
  try {
    const payload = {
      tenNguoiNhan: String(addrModal.form.tenNguoiNhan || "").trim(),
      soDienThoai: String(addrModal.form.soDienThoai || "").trim(),
      tinhThanh: addrModal.form.tinhThanh,
      quanHuyen: addrModal.form.quanHuyen,
      phuongXa: addrModal.form.phuongXa,
      diaChiChiTiet: String(addrModal.form.diaChiChiTiet || "").trim(),
      quocGia: addrModal.form.quocGia || "Việt Nam",
    };

    // =========================
    // CREATE CUSTOMER MODE (chưa có id) => xử lý local list
    // =========================
    if (!isEdit.value) {
      // ✅ Nếu đang sửa địa chỉ tạm -> UPDATE item, không ADD mới
      if (addrModal.mode === "edit" && addrModal.editingId != null) {
        const idx = diaChiList.findIndex((x) => String(x.id) === String(addrModal.editingId));

        if (idx >= 0) {
          // giữ id + laMacDinh hiện tại, chỉ update field
          Object.assign(diaChiList[idx], payload);

          // nếu tick "mặc định" -> set default cho đúng địa chỉ đang sửa
          if (addrModal.form.isDefault) {
            pickLocalDefault(diaChiList[idx].id);
          } else if (!selectedDiaChiId.value) {
            // nếu chưa có default nào thì tự set cái này
            pickLocalDefault(diaChiList[idx].id);
          }

          toast.success("Đã cập nhật địa chỉ (tạm).");
          closeAddrModal(true);
          return;
        }
        // fallback: nếu không tìm thấy id thì coi như add mới
      }

      // ✅ ADD mới địa chỉ tạm
      const tmpId = "tmp-" + Date.now();
      const obj = { id: tmpId, ...payload, laMacDinh: false };
      diaChiList.unshift(obj);

      if (addrModal.form.isDefault) pickLocalDefault(tmpId);
      else if (!selectedDiaChiId.value) pickLocalDefault(tmpId);

      toast.success("Đã thêm địa chỉ (tạm).");
      closeAddrModal(true);
      return;
    }

    // =========================
    // EDIT CUSTOMER MODE (đã có id) => gọi API
    // =========================
    let saved = null;

    if (addrModal.mode === "add") {
      saved = await apiAddDiaChi(id.value, payload);
      toast.success("Thêm địa chỉ thành công!");
    } else {
      const diaChiId = addrModal.editingId;
      saved = await apiUpdateDiaChi(id.value, diaChiId, payload);
      toast.success("Cập nhật địa chỉ thành công!");
    }

    // luôn reload lại list
    await fetchDiaChiList();

    // nếu tick mặc định thì set mặc định
    if (addrModal.form.isDefault && saved?.id) {
      try {
        await apiSetDefaultDiaChi(id.value, saved.id);
        await fetchDiaChiList();
      } catch {}
    }

    closeAddrModal(true);
  } catch (e) {
    console.error(e);
    const m = e?.response?.data?.message || e?.message || "Lưu địa chỉ thất bại!";
    toast.error(m);
  } finally {
    addrModal.loading = false;
  }
}


async function saveDiaChi() {
  if (blockIfViewMode()) return;
  const msg = validateAddrForm();
  if (msg) return toast.warning(msg);

  openConfirm({
    title: "Xác nhận",
    message: "Bạn có chắc muốn lưu địa chỉ này không?",
    onOk: doSaveDiaChi,
  });
}

// ✅ yêu cầu: xóa địa chỉ có confirm + fetch lại từ DB
async function doDeleteDiaChi(a) {
  if (!a?.id) return;

  if (!isEdit.value) {
    const idx = diaChiList.findIndex((x) => String(x.id) === String(a.id));
    if (idx >= 0) diaChiList.splice(idx, 1);
    if (String(selectedDiaChiId.value) === String(a.id)) {
      const next = diaChiList[0] || null;
      if (next) pickLocalDefault(next.id);
      else selectedDiaChiId.value = null;
    }
    toast.success("Đã xóa địa chỉ (tạm).");
    return;
  }

  try {
    await apiDeleteDiaChi(id.value, a.id);
    toast.success("Xóa địa chỉ thành công!");
    await fetchDiaChiList(); // ✅ lấy lại từ DB
  } catch (e) {
    console.error(e);
    toast.error("Xóa địa chỉ thất bại!");
  }
}

async function deleteDiaChi(a) {
  if (blockIfViewMode()) return;
  openConfirm({
    title: "Xác nhận",
    message: "Bạn có chắc muốn xóa địa chỉ này không?",
    onOk: () => doDeleteDiaChi(a),
  });
}

/** ===== SUBMIT (khách hàng) ===== */
async function doSubmit() {
  const msg = validate();
  if (msg) return toast.warning(msg);

  saving.value = true;
  try {
    await uploadAvatarIfNeeded();
    const taiKhoanAuto = String(form.soDienThoai || "").trim();
    const payload = {
      maKhachHang: form.maKhachHang,
      tenKhachHang: String(form.tenKhachHang || "").trim(),
      gioiTinh: form.gioiTinh,
      ngaySinh: String(form.ngaySinh || "").trim() ? String(form.ngaySinh || "").trim() : null,
      soDienThoai: String(form.soDienThoai || "").trim(),
      email: String(form.email || "").trim() ? String(form.email || "").trim() : null,
      taiKhoan: taiKhoanAuto,
      trangThai: form.trangThai,
      anhDaiDien: form.anhDaiDien,
    };

    if (!isEdit.value) {
      payload.matKhau = "123456";
      const res = await http.post("/api/khach-hang", payload);
      const created = res?.data || null;
      const newId = created?.id;

      if (newId && diaChiList.length > 0) {
        const createdAddr = [];
        for (const a of diaChiList) {
          const p = {
            tenNguoiNhan: a.tenNguoiNhan,
            soDienThoai: a.soDienThoai,
            tinhThanh: a.tinhThanh,
            quanHuyen: a.quanHuyen,
            phuongXa: a.phuongXa,
            diaChiChiTiet: a.diaChiChiTiet,
            quocGia: a.quocGia || "Việt Nam",
          };
          const saved = await apiAddDiaChi(newId, p);
          createdAddr.push(saved);
        }

        const pickId = selectedDiaChiId.value;
        let picked = null;
        if (pickId) {
          const idx = diaChiList.findIndex((x) => String(x.id) === String(pickId));
          picked = idx >= 0 ? createdAddr[idx] : null;
        }
        if (!picked) picked = createdAddr[0];
        if (picked?.id) await apiSetDefaultDiaChi(newId, picked.id);
      }

      toast.success("Thêm mới thành công!");
      goBack();
      return;
    }

    await http.put("/api/khach-hang/" + id.value, payload);
    toast.success("Cập nhật thành công!");
    goBack();
  } catch (e) {
    console.error(e);
    const m = e?.response?.data?.message || e?.message || "Có lỗi xảy ra";
    toast.error(m);
  } finally {
    saving.value = false;
  }
}

// ✅ yêu cầu: lưu thay đổi có confirm
async function submit() {
  if (blockIfViewMode()) return;
  const msg = validate();
  if (msg) return toast.warning(msg);

  openConfirm({
    title: "Xác nhận",
    message: isEdit.value ? "Bạn có chắc muốn lưu thay đổi không?" : "Bạn có chắc muốn thêm mới khách hàng không?",
    onOk: doSubmit,
  });
}

function toastClass(type) {
  const t = String(type || "info").toLowerCase();
  if (t === "success") return "text-bg-success";
  if (t === "error") return "text-bg-danger";
  if (t === "warning") return "text-bg-warning";
  return "text-bg-info";
}

onMounted(async () => {
  await loadProvinces();
  await nextTick();
  initDobPicker();
  if (isEdit.value) {
    try {
      await fetchDetail();
    } catch (e) {
      toast.error("Có lỗi xảy ra khi tải dữ liệu");
    }
  } else {
    try {
      await fetchNextCode();
    } catch (e) {
      console.warn(e);
    }
  }
});

onBeforeUnmount(() => {
  try { fpDob?.destroy(); } catch {}
  fpDob = null;
  revokeBlob();
});
</script>

<style scoped>
.customer-form-page { font-size: 15px; }

.page-head{
  display:flex;
  align-items:center;
  justify-content:space-between;
  margin-bottom:12px;
}
.page-title h2{
  margin:0;
  font-size:18px;
  font-weight:800;
}

.btn-back{
  height:34px;
  padding: 0 12px;
  border-radius: 8px;
  border: 1px solid #d0d7de;
  background: #f3f4f6;
  cursor: pointer;
}

.card{
  background:#fff;
  border:1px solid #e5e7eb;
  border-radius:12px;
  padding: 16px;
}

/* Avatar */
.avatar-zone{
  display:flex;
  flex-direction:column;
  align-items:center;
  gap:8px;
  margin: 6px 0 14px 0;
}
.avatar-wrap{
  width: 86px;
  height: 86px;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  overflow:hidden;
  cursor:pointer;
  background:#f3f4f6;
  display:flex;
  align-items:center;
  justify-content:center;
}
.avatar-img{ width:100%; height:100%; object-fit:cover; }
.avatar-fallback{ font-size: 34px; opacity: .7; }
.avatar-hint{ font-size: 12px; opacity: .7; text-align:center; }
.hidden-file{
  position: absolute;
  width: 1px;
  height: 1px;
  opacity: 0;
  overflow: hidden;
}

/* Form grid */
.form-grid{
  display:grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px 16px;
  align-items:end;
}
.span-3{ grid-column: span 3; }
.span-2{ grid-column: span 2; }

@media (max-width: 992px) {
  .form-grid{ grid-template-columns: 1fr 1fr; }
  .span-3, .span-2{ grid-column: span 1; }
}

@media (max-width: 576px) {
  .form-grid{ grid-template-columns: 1fr; }
  .span-3, .span-2{ grid-column: span 1; }
}
.form-group label{
  display:block;
  margin-bottom:6px;
  font-weight:700;
}
.req{ color: #dc2626; }
.form-input{
  width:100%;
  height: 40px;
  padding: 0 12px;
  border-radius: 8px;
  border: 1px solid #d0d7de;
  background:#fff;
  outline:none;
}
.input-disabled{
  background:#f3f4f6;
  color:#6b7280;
}
.radio-row{
  display:flex;
  gap:14px;
  align-items:center;
  height: 40px;
}
.radio-item{
  display:flex;
  gap:6px;
  align-items:center;
}

/* Date picker (giống VoucherList) */
.date-input-group .form-control{
  height: 40px;
  border-radius: 8px 0 0 8px;
  border: 1px solid #d0d7de;
}
.date-input-group .btn{
  height: 40px;
  border: 1px solid #d0d7de;
}
.date-input-group .btn:first-of-type{ border-left: 0; }
.date-input-group .btn:last-of-type{ border-radius: 0 8px 8px 0; }

.section-title{
  margin-top: 14px;
  margin-bottom: 10px;
  font-weight: 800;
}

/* Address box */
.addr-box{
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 12px;
  background: #fff;
}
.addr-head{
  display:flex;
  align-items:center;
  justify-content:space-between;
  gap: 12px;
  border: 1px solid #d1fae5;
  background: #ecfdf5;
  border-radius: 12px;
  padding: 12px 14px;
}
.addr-head-title{ font-weight: 800; color: #0d6efd; }
.addr-head-sub{ font-size: 13px; color: #5b6b6b; margin-top: 2px; }
.btn-add-addr{
  height: 36px;
  padding: 0 14px;
  border-radius: 10px;
  border: 1px solid #10b981;
  background: #10b981;
  color:#fff;
  cursor:pointer;
  font-weight:800;
}
.btn-add-addr:disabled{ opacity:.6; cursor:not-allowed; }

.addr-list{ margin-top: 10px; }
.addr-item{
  display:flex;
  align-items:center;
  justify-content:space-between;
  gap:12px;
  border:1px solid #e5e7eb;
  border-radius:12px;
  padding:12px;
  background:#fff;
  margin-top:10px;
}
.addr-item--default{
  border-color:#0d6efd;
  box-shadow: 0 0 0 2px rgba(13,110,253,.08) inset;
}
.addr-item--selected{
  background: #f8fbff;
}
.addr-left{
  display:flex;
  gap:10px;
  align-items:flex-start;
  flex: 1;
  width: auto;
  cursor:pointer;
}
.addr-content{ flex:1; }
.addr-top{ font-size:15px; display:flex; align-items:center; gap:6px; }
.addr-sub{ font-size:13px; color:#6b7280; margin-top:2px; }
.badge-default{
  margin-left: 8px;
  background: #0d6efd;
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 999px;
}
.addr-actions{
  display:flex;
  gap:8px;
  flex-shrink:0;
}
.icon-btn{
  width: 34px;
  height: 34px;
  border-radius: 10px;
  border: 1px solid #d0d7de;
  background:#fff;
  cursor:pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.icon-btn-danger{
  border-color:#fca5a5;
  color:#b91c1c;
}
.addr-empty{
  margin-top: 10px;
  color:#6b7280;
  font-size: 14px;
  padding: 10px 4px;
}
.addr-foot{
  margin-top: 10px;
  font-size: 12px;
  color:#6b7280;
}

/* Actions */
.actions-row{
  display:flex;
  align-items:center;
  justify-content:space-between;
  gap:10px;
  margin-top: 14px;
}
.actions{
  display:flex;
  justify-content:flex-end;
  gap:10px;
}
.btn{
  height: 38px;
  padding: 0 14px;
  border-radius: 8px;
  border: 1px solid #d0d7de;
  background:#fff;
  cursor:pointer;
  font-weight:700;
}
.btn-primary{
  border-color:#1d4ed8;
  background:#1d4ed8;
  color:#fff;
}
.btn-ghost{ background:#fff; }
.btn:disabled{ opacity:.6; cursor:not-allowed; }

/* Modal overlay */
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
.btn-confirm:disabled{ opacity:.6; cursor:not-allowed; }

/* Modal form */
.addr-form-grid{
  display:grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 16px;
}
.check-row{ margin-top: 10px; margin-bottom: 14px; }
.check-item{ display:flex; align-items:center; gap:8px; font-weight:700; }

@media (max-width: 980px){
  .form-grid{ grid-template-columns: 1fr; }
  .addr-form-grid{ grid-template-columns: 1fr; }
}

/* Chỉ canh TOP cho đúng riêng hàng 3 */
.row3-item{
  align-self: start;
}

/* Cụm giới tính + trạng thái nằm trên 1 hàng, căn giữa */
.group-right .gs-row{
  display: flex;
  justify-content: center;   /* bạn hỏi: dùng center ổn không -> ✅ ổn */
  align-items: flex-start;   /* ✅ để label thẳng hàng với Ngày sinh */
  gap: 240px;
  flex-wrap: wrap;
}

.group-right .gs-item{
  min-width: 0;
}
</style>
