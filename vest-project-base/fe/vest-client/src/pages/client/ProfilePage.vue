<template>
  <section class="profile-page py-4 py-lg-5">
    <div class="container">
      <div
        class="profile-header d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4"
      >
        <div>
          <h1 class="profile-title mb-1">Hồ sơ của tôi</h1>
        </div>

        <div class="profile-header-actions">
          <button
            type="button"
            class="btn profile-change-password-btn"
            @click="openChangePasswordModal"
          >
            <i class="bi bi-shield-lock me-2"></i>Đổi mật khẩu
          </button>

          <router-link to="/shop" class="btn profile-shop-btn">
            <i class="bi bi-arrow-bar-left me-2"></i>Quay lại
          </router-link>
        </div>
      </div>

      <div v-if="error" class="alert alert-danger mb-4">{{ error }}</div>
      <div v-if="successMessage" class="alert alert-success mb-4">
        {{ successMessage }}
      </div>

      <div v-if="loading" class="profile-loading card border-0 shadow-sm">
        <div class="card-body py-5 text-center">
          <div
            class="spinner-border text-primary"
            role="status"
            aria-hidden="true"
          ></div>
          <p class="mt-3 mb-0 text-secondary">Đang tải thông tin hồ sơ...</p>
        </div>
      </div>

      <div v-else class="row g-4">
        <div class="col-lg-4">
          <div class="card border-0 shadow-sm h-100 profile-summary">
            <div class="card-body p-4">
              <div class="avatar-circle mb-3">
                <img
                  v-if="avatarUrl"
                  :src="avatarUrl"
                  alt="Avatar khách hàng"
                  class="avatar-img"
                  @error="onAvatarError"
                />
                <span v-else>{{ avatarText }}</span>
              </div>

              <h2 class="summary-name mb-1">
                {{ form.tenKhachHang || "Khách hàng" }}
              </h2>
              <p>{{ form.email || "Chưa cập nhật email" }}</p>

              <div class="summary-item">
                <span>Mã khách hàng</span>
                <strong>{{ profile.maKhachHang || "---" }}</strong>
              </div>

              <div class="summary-item">
                <span>Tài khoản</span>
                <strong>{{ form.taiKhoan || "---" }}</strong>
              </div>

              <div class="summary-item">
                <span>Điện thoại</span>
                <strong>{{ form.soDienThoai || "Chưa cập nhật" }}</strong>
              </div>

              <div class="summary-item">
                <span>Địa chỉ mặc định</span>
                <strong>{{ fullAddress || "Chưa có địa chỉ mặc định" }}</strong>
              </div>
            </div>
          </div>
        </div>

        <div class="col-lg-8">
          <div class="card border-0 shadow-sm">
            <div class="card-body p-4 p-lg-5">
              <form class="row g-3" @submit.prevent="submitProfile">
                <div class="col-12">
                  <h3 class="section-title">Thông tin cá nhân</h3>
                </div>

                <div class="col-md-6">
                  <label class="form-label">Họ và tên</label>
                  <input
                    v-model.trim="form.tenKhachHang"
                    type="text"
                    class="form-control"
                    placeholder="Nhập họ và tên"
                    required
                  />
                </div>

                <div class="col-md-6">
                  <label class="form-label">Tài khoản</label>
                  <input
                    :value="form.taiKhoan"
                    type="text"
                    class="form-control"
                    disabled
                  />
                </div>

                <div class="col-md-6">
                  <label class="form-label">Email</label>
                  <input
                    v-model.trim="form.email"
                    type="email"
                    class="form-control"
                    placeholder="you@example.com"
                  />
                </div>

                <div class="col-md-6">
                  <label class="form-label">Số điện thoại</label>
                  <input
                    v-model.trim="form.soDienThoai"
                    type="text"
                    class="form-control"
                    placeholder="Nhập số điện thoại"
                    inputmode="numeric"
                    maxlength="10"
                    @input="form.soDienThoai = normalizePhoneInput(form.soDienThoai)"
                  />
                </div>

                <div class="col-md-6">
                  <label class="form-label">Giới tính</label>
                  <select v-model="gioiTinhValue" class="form-select">
                    <option value="">Chưa chọn</option>
                    <option value="male">Nam</option>
                    <option value="female">Nữ</option>
                  </select>
                </div>

                <div class="col-md-6">
                  <label class="form-label">Ngày sinh</label>
                  <input
                    v-model="form.ngaySinh"
                    type="date"
                    class="form-control"
                  />
                </div>

                <div class="col-12 mt-4">
                  <h3 class="section-title">Địa chỉ nhận hàng mặc định</h3>
                </div>

                <div class="col-md-6">
                  <label class="form-label">Tên người nhận</label>
                  <input
                    v-model.trim="form.tenNguoiNhan"
                    type="text"
                    class="form-control"
                    placeholder="Tên người nhận hàng"
                  />
                </div>

                <div class="col-md-6">
                  <label class="form-label">SĐT người nhận</label>
                  <input
                    v-model.trim="form.sdtNguoiNhan"
                    type="text"
                    class="form-control"
                    placeholder="Số điện thoại người nhận"
                    inputmode="numeric"
                    maxlength="10"
                    @input="form.sdtNguoiNhan = normalizePhoneInput(form.sdtNguoiNhan)"
                  />
                </div>

                <div class="col-md-4">
                  <label class="form-label">Quốc gia</label>
                  <input
                    v-model.trim="form.quocGia"
                    type="text"
                    class="form-control"
                    placeholder="Việt Nam"
                  />
                </div>

                <div class="col-md-4">
                  <label class="form-label">Tỉnh / Thành phố</label>
                  <select
                    v-model="form.tinhThanh"
                    class="form-select"
                    :disabled="provinceLoading"
                    @change="onProvinceChange"
                  >
                    <option value="">
                      {{
                        provinceLoading
                          ? "Đang tải tỉnh/thành..."
                          : "Chọn tỉnh/thành phố"
                      }}
                    </option>
                    <option
                      v-for="province in provinces"
                      :key="province.code"
                      :value="province.name"
                    >
                      {{ province.name }}
                    </option>
                  </select>
                </div>

                <div class="col-md-4">
                  <label class="form-label">Phường / Xã / Đặc khu</label>
                  <select
                    v-model="form.phuongXa"
                    class="form-select"
                    :disabled="!form.tinhThanh || wardLoading"
                  >
                    <option value="">
                      {{
                        wardLoading
                          ? "Đang tải phường/xã..."
                          : "Chọn phường/xã/đặc khu"
                      }}
                    </option>
                    <option
                      v-for="ward in wards"
                      :key="ward.code"
                      :value="ward.name"
                    >
                      {{ ward.name }}
                    </option>
                  </select>
                </div>

                <div class="col-md-12">
                  <label class="form-label">Địa chỉ chi tiết</label>
                  <input
                    v-model.trim="form.diaChiChiTiet"
                    type="text"
                    class="form-control"
                    placeholder="Số nhà, tên đường..."
                  />
                </div>

                <div class="col-12 d-flex flex-wrap gap-3 pt-3">
                  <button
                    class="btn btn-primary px-4"
                    type="submit"
                    :disabled="saving"
                  >
                    <span
                      v-if="saving"
                      class="spinner-border spinner-border-sm me-2"
                      aria-hidden="true"
                    ></span>
                    {{ saving ? "Đang lưu..." : "Lưu hồ sơ" }}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>

  <div
    v-if="showChangePasswordModal"
    class="password-modal-overlay"
    @click.self="closeChangePasswordModal"
  >
    <div class="password-modal-card">
      <div class="d-flex align-items-start justify-content-between gap-3 mb-3">
        <div>
          <h3 class="password-modal-title mb-1">Đổi mật khẩu</h3>
          <p class="password-modal-desc mb-0">
            Mã OTP sẽ được gửi về email tài khoản của bạn.
          </p>
        </div>

        <button
          type="button"
          class="btn-close"
          :disabled="otpSending || passwordSubmitting"
          @click="closeChangePasswordModal"
        ></button>
      </div>

      <div class="otp-email-box mb-3">
        <div class="otp-email-icon">
          <i class="bi bi-envelope-check"></i>
        </div>
        <div>
          <div class="otp-email-label">Email nhận OTP</div>
          <div class="otp-email-value">
            {{ form.email || "Chưa cập nhật email" }}
          </div>
        </div>
      </div>

      <div class="mb-3">
        <label class="form-label">Mật khẩu mới</label>
        <div class="input-group">
          <input
            v-model.trim="passwordForm.newPassword"
            :type="showNewPassword ? 'text' : 'password'"
            class="form-control"
            placeholder="Nhập mật khẩu mới"
            autocomplete="new-password"
          />
          <button
            class="btn btn-outline-secondary"
            type="button"
            @click="showNewPassword = !showNewPassword"
          >
            <i :class="showNewPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
          </button>
        </div>
      </div>

      <div class="mb-3">
        <label class="form-label">Xác nhận mật khẩu mới</label>
        <div class="input-group">
          <input
            v-model.trim="passwordForm.confirmPassword"
            :type="showConfirmPassword ? 'text' : 'password'"
            class="form-control"
            placeholder="Nhập lại mật khẩu mới"
            autocomplete="new-password"
          />
          <button
            class="btn btn-outline-secondary"
            type="button"
            @click="showConfirmPassword = !showConfirmPassword"
          >
            <i :class="showConfirmPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
          </button>
        </div>
      </div>

      <div class="mb-3">
        <label class="form-label">Mã OTP</label>
        <div class="input-group">
          <input
            v-model.trim="passwordForm.otp"
            type="text"
            maxlength="6"
            inputmode="numeric"
            class="form-control otp-input"
            placeholder="Nhập mã OTP"
            autocomplete="one-time-code"
            @input="passwordForm.otp = onlyNumber(passwordForm.otp)"
          />

          <button
            class="btn btn-outline-primary"
            type="button"
            :disabled="otpSending || otpCooldown > 0"
            @click="sendChangePasswordOtp"
          >
            <span
              v-if="otpSending"
              class="spinner-border spinner-border-sm me-1"
            ></span>
            {{ otpButtonText }}
          </button>
        </div>

        <div class="form-text">
          Bấm gửi OTP, sau đó kiểm tra email để lấy mã xác nhận.
        </div>
      </div>

      <div class="d-flex justify-content-end gap-2 mt-4">
        <button
          type="button"
          class="btn btn-light px-4"
          :disabled="otpSending || passwordSubmitting"
          @click="closeChangePasswordModal"
        >
          Hủy
        </button>

        <button
          type="button"
          class="btn btn-primary px-4"
          :disabled="passwordSubmitting"
          @click="changePassword"
        >
          <span
            v-if="passwordSubmitting"
            class="spinner-border spinner-border-sm me-2"
          ></span>
          {{ passwordSubmitting ? "Đang xử lý..." : "Xác nhận đổi mật khẩu" }}
        </button>
      </div>
    </div>
  </div>

  <ChatWidget />
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from "vue";
import { getClientProfile, updateClientProfile } from "../../services/Api";
import { pickFirstMediaUrl } from "../../utils/media";
import ChatWidget from "../../components/ClientChatWidget.vue";

const loading = ref(true);
const saving = ref(false);
const error = ref("");
const successMessage = ref("");
const profile = ref({});
const avatarLoadError = ref(false);
const showChangePasswordModal = ref(false);
const otpSending = ref(false);
const passwordSubmitting = ref(false);
const otpCooldown = ref(0);
const otpTimer = ref(null);
const showNewPassword = ref(false);
const showConfirmPassword = ref(false);

const passwordForm = reactive({
  newPassword: "",
  confirmPassword: "",
  otp: "",
});

const form = reactive({
  id: null,
  maKhachHang: "",
  taiKhoan: "",
  tenKhachHang: "",
  email: "",
  soDienThoai: "",
  ngaySinh: "",
  tenNguoiNhan: "",
  sdtNguoiNhan: "",
  tinhThanh: "",
  quanHuyen: null,
  phuongXa: "",
  diaChiChiTiet: "",
  quocGia: "Việt Nam",
});

const gioiTinhValue = ref("");
const provinces = ref([]);
const wards = ref([]);
const provinceLoading = ref(false);
const wardLoading = ref(false);

const otpButtonText = computed(() => {
  if (otpCooldown.value > 0) return `Gửi lại sau ${otpCooldown.value}s`;
  return "Gửi OTP";
});

const avatarUrl = computed(() => {
  if (avatarLoadError.value) return "";

  return pickFirstMediaUrl(
    profile.value?.avatarUrl,
    profile.value?.anhDaiDien,
    profile.value?.mediaAvatar,
    profile.value?.mediaAvatarUrl,
  );
});

const avatarText = computed(() => {
  const name = (form.tenKhachHang || "K").trim();
  return name ? name.charAt(0).toUpperCase() : "K";
});

const fullAddress = computed(() => {
  return [form.diaChiChiTiet, form.phuongXa, form.tinhThanh, form.quocGia]
    .map((item) => (item || "").trim())
    .filter(Boolean)
    .join(", ");
});

function onAvatarError() {
  avatarLoadError.value = true;
}

function normalizeProvinceList(data = []) {
  return (Array.isArray(data) ? data : [])
    .map((item) => ({ code: item?.code, name: item?.name }))
    .filter((item) => item.code != null && item.name);
}

function normalizeWardList(data = []) {
  return (Array.isArray(data) ? data : [])
    .map((item) => ({
      code: item?.code,
      name: item?.name,
      provinceCode: item?.province_code ?? item?.provinceCode ?? null,
    }))
    .filter((item) => item.code != null && item.name);
}

async function loadProvinces() {
  provinceLoading.value = true;
  try {
    const res = await fetch("https://provinces.open-api.vn/api/v2/p/");
    if (!res.ok) throw new Error("Không tải được tỉnh/thành");
    const data = await res.json();
    provinces.value = normalizeProvinceList(data);
  } catch (err) {
    provinces.value = [];
  } finally {
    provinceLoading.value = false;
  }
}

async function loadWardsByProvinceName(provinceName) {
  const province = provinces.value.find((item) => item.name === provinceName);
  if (!province?.code) {
    wards.value = [];
    return;
  }

  wardLoading.value = true;
  try {
    const res = await fetch(
      `https://provinces.open-api.vn/api/v2/w/?province=${province.code}`,
    );
    if (!res.ok) throw new Error("Không tải được phường/xã");
    const data = await res.json();
    wards.value = normalizeWardList(data);
  } catch (err) {
    wards.value = [];
  } finally {
    wardLoading.value = false;
  }
}

async function onProvinceChange() {
  form.quanHuyen = null;
  form.phuongXa = "";
  wards.value = [];
  if (!form.tinhThanh) return;
  await loadWardsByProvinceName(form.tinhThanh);
}

function fillForm(data = {}) {
  avatarLoadError.value = false;
  profile.value = data;
  form.id = data.id ?? null;
  form.maKhachHang = data.maKhachHang || "";
  form.taiKhoan = data.taiKhoan || "";
  form.tenKhachHang = data.tenKhachHang || "";
  form.email = data.email || "";
  form.soDienThoai = data.soDienThoai || "";
  form.ngaySinh = data.ngaySinh || "";
  form.tenNguoiNhan =
    data.tenNguoiNhan || data.diaChiMacDinh?.tenNguoiNhan || "";
  form.sdtNguoiNhan =
    data.sdtNguoiNhan || data.diaChiMacDinh?.soDienThoai || "";
  form.tinhThanh = data.tinhThanh || data.diaChiMacDinh?.tinhThanh || "";
  form.quanHuyen = null;
  form.phuongXa = data.phuongXa || data.diaChiMacDinh?.phuongXa || "";
  form.diaChiChiTiet =
    data.diaChiChiTiet || data.diaChiMacDinh?.diaChiChiTiet || "";
  form.quocGia = data.quocGia || data.diaChiMacDinh?.quocGia || "Việt Nam";
  gioiTinhValue.value =
    data.gioiTinh === true ? "male" : data.gioiTinh === false ? "female" : "";
}

function syncProfileToStorage(data = {}) {
  const userName = data.tenKhachHang || "";

  if (localStorage.getItem("USER_ACCESS_TOKEN")) {
    localStorage.setItem("USER_NAME", userName);
  }

  if (sessionStorage.getItem("USER_ACCESS_TOKEN")) {
    sessionStorage.setItem("USER_NAME", userName);
  }

  const vestUserRaw = localStorage.getItem("vest_user");
  let vestUser = {};

  try {
    vestUser = vestUserRaw ? JSON.parse(vestUserRaw) : {};
  } catch (e) {
    vestUser = {};
  }

  localStorage.setItem(
    "vest_user",
    JSON.stringify({
      ...vestUser,
      id: data.id ?? vestUser.id,
      taiKhoan: data.taiKhoan ?? vestUser.taiKhoan,
      tenKhachHang: userName,
      email: data.email ?? vestUser.email,
      soDienThoai: data.soDienThoai ?? vestUser.soDienThoai,
      anhDaiDien: data.anhDaiDien ?? data.avatarUrl ?? vestUser.anhDaiDien,
      avatarUrl: data.avatarUrl ?? data.anhDaiDien ?? vestUser.avatarUrl,
      mediaAvatarId: data.mediaAvatarId ?? vestUser.mediaAvatarId,
      role: vestUser.role || "CLIENT",
    }),
  );

  window.dispatchEvent(new Event("auth-changed"));
}

async function loadProfile() {
  loading.value = true;
  error.value = "";
  successMessage.value = "";

  try {
    const { data } = await getClientProfile();
    fillForm(data || {});

    if (form.tinhThanh) {
      await loadWardsByProvinceName(form.tinhThanh);
    }

    syncProfileToStorage(data || {});
  } catch (err) {
    error.value =
      err?.response?.data?.message ||
      err?.response?.data?.error ||
      "Không tải được hồ sơ khách hàng.";
  } finally {
    loading.value = false;
  }
}

function normalizePhoneInput(value) {
  return String(value || "")
    .replace(/\D/g, "")
    .slice(0, 10);
}

function isValidPhone(value) {
  return /^0\d{9}$/.test(String(value || "").trim());
}

function isValidEmail(value) {
  const email = String(value || "").trim();

  if (!email) return false;
  if (email.length > 100) return false;
  if (email.includes("..")) return false;

  const basicRegex = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,10}$/;
  if (!basicRegex.test(email)) return false;

  const parts = email.split("@");
  if (parts.length !== 2) return false;

  const domain = parts[1].toLowerCase();
  const invalidEndings = [
    ".coma",
    ".con",
    ".comm",
    ".cmo",
    ".coom",
    ".vom",
    ".cpm",
    ".gmai",
    ".gmial",
  ];

  if (invalidEndings.some((ending) => domain.endsWith(ending))) return false;
  if (domain.includes("gmail") && domain !== "gmail.com") return false;

  return true;
}

function validateProfileForm() {
  const tenKhachHang = String(form.tenKhachHang || "").trim();
  const email = String(form.email || "").trim();
  const soDienThoai = String(form.soDienThoai || "").trim();
  const sdtNguoiNhan = String(form.sdtNguoiNhan || "").trim();

  if (!tenKhachHang) {
    return "Họ và tên không được để trống.";
  }

  if (!email) {
    return "Email không được để trống.";
  }

  if (!isValidEmail(email)) {
    return "Email không hợp lệ. Vui lòng kiểm tra lại email.";
  }

  if (!soDienThoai) {
    return "Số điện thoại không được để trống.";
  }

  if (!isValidPhone(soDienThoai)) {
    return "Số điện thoại phải gồm 10 số và bắt đầu bằng số 0.";
  }

  if (sdtNguoiNhan && !isValidPhone(sdtNguoiNhan)) {
    return "SĐT người nhận phải gồm 10 số và bắt đầu bằng số 0.";
  }

  return "";
}

async function submitProfile() {
  error.value = "";
  successMessage.value = "";

  form.email = String(form.email || "").trim();
  form.tenKhachHang = String(form.tenKhachHang || "").trim();
  form.soDienThoai = normalizePhoneInput(form.soDienThoai);
  form.sdtNguoiNhan = normalizePhoneInput(form.sdtNguoiNhan);

  const validateMessage = validateProfileForm();

  if (validateMessage) {
    error.value = validateMessage;
    return;
  }

  saving.value = true;

  try {
    const payload = {
      tenKhachHang: form.tenKhachHang,
      email: form.email || null,
      soDienThoai: form.soDienThoai || null,
      gioiTinh:
        gioiTinhValue.value === "" ? null : gioiTinhValue.value === "male",
      ngaySinh: form.ngaySinh || null,
      tenNguoiNhan: form.tenNguoiNhan || null,
      sdtNguoiNhan: form.sdtNguoiNhan || null,
      tinhThanh: form.tinhThanh || null,
      quanHuyen: null,
      phuongXa: form.phuongXa || null,
      diaChiChiTiet: form.diaChiChiTiet || null,
      quocGia: form.quocGia || null,
    };

    const { data } = await updateClientProfile(payload);
    fillForm(data || {});

    if (form.tinhThanh) {
      await loadWardsByProvinceName(form.tinhThanh);
    }

    syncProfileToStorage(data || {});
    successMessage.value = "Cập nhật hồ sơ thành công.";
  } catch (err) {
    error.value =
      err?.response?.data?.message ||
      err?.response?.data?.error ||
      "Cập nhật hồ sơ thất bại.";
  } finally {
    saving.value = false;
  }
}

function onlyNumber(value) {
  return String(value || "")
    .replace(/\D/g, "")
    .slice(0, 6);
}

function resetPasswordForm() {
  passwordForm.newPassword = "";
  passwordForm.confirmPassword = "";
  passwordForm.otp = "";
  showNewPassword.value = false;
  showConfirmPassword.value = false;
}

function openChangePasswordModal() {
  error.value = "";
  successMessage.value = "";
  resetPasswordForm();
  showChangePasswordModal.value = true;
}

function closeChangePasswordModal() {
  if (otpSending.value || passwordSubmitting.value) return;
  showChangePasswordModal.value = false;
  resetPasswordForm();
}
function forceCloseChangePasswordModal() {
  showChangePasswordModal.value = false;
  resetPasswordForm();
}
function startOtpCooldown() {
  otpCooldown.value = 60;

  if (otpTimer.value) clearInterval(otpTimer.value);

  otpTimer.value = setInterval(() => {
    otpCooldown.value -= 1;

    if (otpCooldown.value <= 0) {
      clearInterval(otpTimer.value);
      otpTimer.value = null;
      otpCooldown.value = 0;
    }
  }, 1000);
}

function validatePasswordBeforeSendOtp() {
  if (!form.email) {
    error.value = "Tài khoản của bạn chưa có email nên không thể nhận OTP.";
    return false;
  }

  if (!isValidEmail(form.email)) {
    error.value = "Email không hợp lệ. Vui lòng cập nhật lại email trước.";
    return false;
  }

  if (!passwordForm.newPassword) {
    error.value = "Vui lòng nhập mật khẩu mới.";
    return false;
  }

  if (passwordForm.newPassword.length <= 6) {
    error.value = "Mật khẩu mới phải nhiều hơn 6 ký tự.";
    return false;
  }

  if (!passwordForm.confirmPassword) {
    error.value = "Vui lòng xác nhận mật khẩu mới.";
    return false;
  }

  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    error.value = "Mật khẩu xác nhận không khớp.";
    return false;
  }

  return true;
}

function validateChangePassword() {
  if (!validatePasswordBeforeSendOtp()) return false;

  if (!passwordForm.otp) {
    error.value = "Vui lòng nhập mã OTP.";
    return false;
  }

  if (passwordForm.otp.length !== 6) {
    error.value = "Mã OTP phải gồm 6 chữ số.";
    return false;
  }

  return true;
}

async function sendChangePasswordOtp() {
  error.value = "";
  successMessage.value = "";

  form.email = String(form.email || "").trim();

  if (!validatePasswordBeforeSendOtp()) return;

  otpSending.value = true;

  try {
    const token =
      localStorage.getItem("USER_ACCESS_TOKEN") ||
      sessionStorage.getItem("USER_ACCESS_TOKEN") ||
      localStorage.getItem("token") ||
      localStorage.getItem("vest_token") ||
      "";

    const res = await fetch("/api/client/auth/forgot-password-otp", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: JSON.stringify({
        email: form.email,
      }),
    });

    const data = await res.json().catch(() => ({}));

    if (!res.ok) {
      throw new Error(data?.message || data?.error || "Không gửi được mã OTP.");
    }

    successMessage.value = "Mã OTP đã được gửi về email của bạn.";
    startOtpCooldown();
  } catch (err) {
    error.value = err?.message || "Không gửi được mã OTP.";
  } finally {
    otpSending.value = false;
  }
}

async function changePassword() {
  error.value = "";
  successMessage.value = "";

  form.email = String(form.email || "").trim();

  if (!validateChangePassword()) return;

  passwordSubmitting.value = true;

  try {
    const token =
      localStorage.getItem("USER_ACCESS_TOKEN") ||
      sessionStorage.getItem("USER_ACCESS_TOKEN") ||
      localStorage.getItem("token") ||
      localStorage.getItem("vest_token") ||
      "";

    const res = await fetch("/api/client/auth/reset-password-otp", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: JSON.stringify({
        email: form.email,
        otp: passwordForm.otp,
        newPassword: passwordForm.newPassword,
        confirmPassword: passwordForm.confirmPassword,
      }),
    });

    const data = await res.json().catch(() => ({}));

    if (!res.ok) {
      throw new Error(data?.message || data?.error || "Đổi mật khẩu thất bại.");
    }

successMessage.value = "Đổi mật khẩu thành công.";
forceCloseChangePasswordModal();
  } catch (err) {
    error.value = err?.message || "Đổi mật khẩu thất bại.";
  } finally {
    passwordSubmitting.value = false;
  }
}

onMounted(async () => {
  await loadProvinces();
  await loadProfile();
});

onBeforeUnmount(() => {
  if (otpTimer.value) {
    clearInterval(otpTimer.value);
    otpTimer.value = null;
  }
});
</script>

<style scoped>
.profile-page {
  background: #f6f8fb;
  min-height: calc(100vh - 220px);
}

.profile-title {
  font-size: 32px;
  font-weight: 700;
  color: #0f172a;
}

.profile-subtitle {
  color: #64748b;
  max-width: 680px;
}

.profile-summary {
  background: linear-gradient(180deg, #0b4fc0 0%, #083b8f 100%);
  color: #fff;
}

.avatar-circle {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 34px;
  font-weight: 700;
  background: rgba(255, 255, 255, 0.18);
  border: 2px solid rgba(255, 255, 255, 0.32);
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
  display: block;
}

.summary-name {
  font-size: 24px;
  font-weight: 700;
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 14px 0;
  border-top: 1px solid rgba(255, 255, 255, 0.18);
}

.summary-item span {
  font-size: 13px;
  opacity: 0.78;
}

.summary-item strong {
  font-size: 15px;
  line-height: 1.5;
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 0;
}

.form-label {
  font-weight: 600;
  color: #334155;
}

.form-control,
.form-select {
  min-height: 46px;
  border-radius: 12px;
  border-color: #dbe2ea;
}

.form-control:focus,
.form-select:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 0.2rem rgba(59, 130, 246, 0.14);
}

.profile-loading {
  border-radius: 18px;
}

.profile-header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.profile-change-password-btn {
  min-height: 42px;
  padding: 0 18px;
  border-radius: 999px;
   border: 1px solid #1d4ed8;
  background: #ffffff;
  color: #1d4ed8;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.profile-change-password-btn:hover {
 background: #1d4ed8;
  color: #ffffff;
  border-color: #1d4ed8;
}

.profile-shop-btn {
  min-height: 42px;
  padding: 0 18px;
  border-radius: 999px;
  border: 1px solid #1d4ed8;
  background: #ffffff;
  color: #1d4ed8;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.profile-shop-btn:hover {
  background: #1d4ed8;
  color: #ffffff;
  border-color: #1d4ed8;
}

.password-modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 99999;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.password-modal-card {
  width: min(520px, 100%);
  background: #ffffff;
  border-radius: 18px;
  padding: 22px;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.22);
  border: 1px solid rgba(226, 232, 240, 0.9);
  animation: passwordModalIn 0.16s ease-out;
}

.password-modal-title {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}

.password-modal-desc {
  color: #64748b;
  font-size: 14px;
  line-height: 1.5;
}

.otp-email-box {
  display: flex;
  gap: 12px;
  align-items: center;
  border: 1px solid #dbeafe;
  background: #eff6ff;
  border-radius: 14px;
  padding: 12px 14px;
}

.otp-email-icon {
  width: 38px;
  height: 38px;
  border-radius: 999px;
  background: #dbeafe;
  color: #1d4ed8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.otp-email-label {
  font-size: 12px;
  color: #64748b;
  font-weight: 600;
}

.otp-email-value {
  color: #0f172a;
  font-weight: 700;
  word-break: break-all;
}

.otp-input {
  font: inherit;
  letter-spacing: normal;
  font-weight: 400;
  text-transform: none;
}

.otp-input::placeholder {
  font: inherit;
  letter-spacing: normal;
  font-weight: 400;
  text-transform: none;
  color: #6c757d;
}

.password-modal-card .form-label {
  font-weight: 600;
  color: #334155;
}

.password-modal-card .form-control {
  min-height: 42px;
  border-radius: 10px;
}

.password-modal-card .input-group .btn {
  border-radius: 0 10px 10px 0;
  min-width: 46px;
}

.password-modal-card .btn-light {
  border: 1px solid #e5e7eb;
  background: #f8fafc;
  color: #334155;
}

.password-modal-card .btn-primary {
  background: #0d6efd;
  border-color: #0d6efd;
  font-weight: 600;
}

@keyframes passwordModalIn {
  from {
    opacity: 0;
    transform: translateY(8px) scale(0.98);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@media (max-width: 767.98px) {
  .profile-title {
    font-size: 26px;
  }
}

@media (max-width: 575.98px) {
  .profile-header-actions {
    width: 100%;
  }

  .profile-change-password-btn,
  .profile-shop-btn {
    flex: 1;
  }

  .password-modal-card {
    padding: 18px;
  }

  .password-modal-card .d-flex.justify-content-end {
    flex-direction: column-reverse;
  }

  .password-modal-card .d-flex.justify-content-end .btn {
    width: 100%;
  }
}
</style>