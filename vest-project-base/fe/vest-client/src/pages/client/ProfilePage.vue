<template>
  <section class="profile-page py-4 py-lg-5">
    <div class="container">
      <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
        <div>
          <h1 class="profile-title mb-1">Hồ sơ của tôi</h1>
<!--          <p class="profile-subtitle mb-0">Quản lý thông tin cá nhân và địa chỉ nhận hàng trên kênh bán hàng online.</p>-->
        </div>
        <router-link to="/shop" class="btn btn-outline-primary">
          <i class="bi bi-bag me-2"></i>Tiếp tục mua sắm
        </router-link>
      </div>

      <div v-if="error" class="alert alert-danger mb-4">{{ error }}</div>
      <div v-if="successMessage" class="alert alert-success mb-4">{{ successMessage }}</div>

      <div v-if="loading" class="profile-loading card border-0 shadow-sm">
        <div class="card-body py-5 text-center">
          <div class="spinner-border text-primary" role="status" aria-hidden="true"></div>
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
              <h2 class="summary-name mb-1">{{ form.tenKhachHang || 'Khách hàng' }}</h2>
              <p >{{ form.email || 'Chưa cập nhật email' }}</p>

              <div class="summary-item">
                <span>Mã khách hàng</span>
                <strong>{{ profile.maKhachHang || '---' }}</strong>
              </div>
              <div class="summary-item">
                <span>Tài khoản</span>
                <strong>{{ form.taiKhoan || '---' }}</strong>
              </div>
              <div class="summary-item">
                <span>Điện thoại</span>
                <strong>{{ form.soDienThoai || 'Chưa cập nhật' }}</strong>
              </div>
              <div class="summary-item">
                <span>Địa chỉ mặc định</span>
                <strong>{{ fullAddress || 'Chưa có địa chỉ mặc định' }}</strong>
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
                  <input v-model.trim="form.tenKhachHang" type="text" class="form-control" placeholder="Nhập họ và tên" required />
                </div>

                <div class="col-md-6">
                  <label class="form-label">Tài khoản</label>
                  <input :value="form.taiKhoan" type="text" class="form-control" disabled />
                </div>

                <div class="col-md-6">
                  <label class="form-label">Email</label>
                  <input v-model.trim="form.email" type="email" class="form-control" placeholder="you@example.com" />
                </div>

                <div class="col-md-6">
                  <label class="form-label">Số điện thoại</label>
                  <input v-model.trim="form.soDienThoai" type="text" class="form-control" placeholder="Nhập số điện thoại" />
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
                  <input v-model="form.ngaySinh" type="date" class="form-control" />
                </div>

                <div class="col-12 mt-4">
                  <h3 class="section-title">Địa chỉ nhận hàng mặc định</h3>
                </div>

                <div class="col-md-6">
                  <label class="form-label">Tên người nhận</label>
                  <input v-model.trim="form.tenNguoiNhan" type="text" class="form-control" placeholder="Tên người nhận hàng" />
                </div>

                <div class="col-md-6">
                  <label class="form-label">SĐT người nhận</label>
                  <input v-model.trim="form.sdtNguoiNhan" type="text" class="form-control" placeholder="Số điện thoại người nhận" />
                </div>
                <div class="col-md-4">
                  <label class="form-label">Quốc gia</label>
                  <input v-model.trim="form.quocGia" type="text" class="form-control" placeholder="Việt Nam" />
                </div>
                <div class="col-md-4">
                  <label class="form-label">Tỉnh / Thành phố</label>
                  <select
                      v-model="form.tinhThanh"
                      class="form-select"
                      :disabled="provinceLoading"
                      @change="onProvinceChange"
                  >
                    <option value="">{{ provinceLoading ? 'Đang tải tỉnh/thành...' : 'Chọn tỉnh/thành phố' }}</option>
                    <option v-for="province in provinces" :key="province.code" :value="province.name">
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
                    <option value="">{{ wardLoading ? 'Đang tải phường/xã...' : 'Chọn phường/xã/đặc khu' }}</option>
                    <option v-for="ward in wards" :key="ward.code" :value="ward.name">
                      {{ ward.name }}
                    </option>
                  </select>
                </div>

                <div class="col-md-12">
                  <label class="form-label">Địa chỉ chi tiết</label>
                  <input v-model.trim="form.diaChiChiTiet" type="text" class="form-control" placeholder="Số nhà, tên đường..." />
                </div>



                <div class="col-12 d-flex flex-wrap gap-3 pt-3">
                  <button class="btn btn-primary px-4" type="submit" :disabled="saving">
                    <span v-if="saving" class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>
                    {{ saving ? 'Đang lưu...' : 'Lưu hồ sơ' }}
                  </button>
                  <button class="btn btn-outline-secondary px-4" type="button" :disabled="saving || loading" @click="loadProfile">
                    Tải lại
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
   <ChatWidget />
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { getClientProfile, updateClientProfile } from '../../services/Api';
import { pickFirstMediaUrl } from '../../utils/media';
import ChatWidget from '../../components/ClientChatWidget.vue';
const loading = ref(true);
const saving = ref(false);
const error = ref('');
const successMessage = ref('');
const profile = ref({});
const avatarLoadError = ref(false);

const avatarUrl = computed(() => {
  if (avatarLoadError.value) return '';

  return pickFirstMediaUrl(
      profile.value?.avatarUrl,
      profile.value?.anhDaiDien,
      profile.value?.mediaAvatar,
      profile.value?.mediaAvatarUrl
  );
});

function onAvatarError() {
  avatarLoadError.value = true;
}
const gioiTinhValue = ref('');
const provinces = ref([]);
const wards = ref([]);
const provinceLoading = ref(false);
const wardLoading = ref(false);
const form = reactive({
  id: null,
  maKhachHang: '',
  taiKhoan: '',
  tenKhachHang: '',
  email: '',
  soDienThoai: '',
  ngaySinh: '',
  tenNguoiNhan: '',
  sdtNguoiNhan: '',
  tinhThanh: '',
  quanHuyen: null,
  phuongXa: '',
  diaChiChiTiet: '',
  quocGia: 'Việt Nam',
});

const avatarText = computed(() => {
  const name = (form.tenKhachHang || 'K').trim();
  return name ? name.charAt(0).toUpperCase() : 'K';
});

const fullAddress = computed(() => {
  return [form.diaChiChiTiet, form.phuongXa, form.tinhThanh, form.quocGia]
      .map((item) => (item || '').trim())
      .filter(Boolean)
      .join(', ');
});

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
    const res = await fetch('https://provinces.open-api.vn/api/v2/p/');
    if (!res.ok) throw new Error('Không tải được tỉnh/thành');
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
    const res = await fetch(`https://provinces.open-api.vn/api/v2/w/?province=${province.code}`);
    if (!res.ok) throw new Error('Không tải được phường/xã');
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
  form.phuongXa = '';
  wards.value = [];
  if (!form.tinhThanh) return;
  await loadWardsByProvinceName(form.tinhThanh);
}

function fillForm(data = {}) {
  avatarLoadError.value = false;
  profile.value = data;
  form.id = data.id ?? null;
  form.maKhachHang = data.maKhachHang || '';
  form.taiKhoan = data.taiKhoan || '';
  form.tenKhachHang = data.tenKhachHang || '';
  form.email = data.email || '';
  form.soDienThoai = data.soDienThoai || '';
  form.ngaySinh = data.ngaySinh || '';
  form.tenNguoiNhan = data.tenNguoiNhan || data.diaChiMacDinh?.tenNguoiNhan || '';
  form.sdtNguoiNhan = data.sdtNguoiNhan || data.diaChiMacDinh?.soDienThoai || '';
  form.tinhThanh = data.tinhThanh || data.diaChiMacDinh?.tinhThanh || '';
  form.quanHuyen = null;
  form.phuongXa = data.phuongXa || data.diaChiMacDinh?.phuongXa || '';
  form.diaChiChiTiet = data.diaChiChiTiet || data.diaChiMacDinh?.diaChiChiTiet || '';
  form.quocGia = data.quocGia || data.diaChiMacDinh?.quocGia || 'Việt Nam';
  gioiTinhValue.value = data.gioiTinh === true ? 'male' : data.gioiTinh === false ? 'female' : '';
}

function syncProfileToStorage(data = {}) {
  const userName = data.tenKhachHang || '';

  if (localStorage.getItem('USER_ACCESS_TOKEN')) {
    localStorage.setItem('USER_NAME', userName);
  }
  if (sessionStorage.getItem('USER_ACCESS_TOKEN')) {
    sessionStorage.setItem('USER_NAME', userName);
  }

  const vestUserRaw = localStorage.getItem('vest_user');
  let vestUser = {};
  try {
    vestUser = vestUserRaw ? JSON.parse(vestUserRaw) : {};
  } catch (e) {
    vestUser = {};
  }

  localStorage.setItem(
    'vest_user',
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
      role: vestUser.role || 'CLIENT',
    })
  );

  window.dispatchEvent(new Event('auth-changed'));
}

async function loadProfile() {
  loading.value = true;
  error.value = '';
  successMessage.value = '';

  try {
    const { data } = await getClientProfile();
    fillForm(data || {});
    if (form.tinhThanh) {
      await loadWardsByProvinceName(form.tinhThanh);
    }
    syncProfileToStorage(data || {});
  } catch (err) {
    error.value = err?.response?.data?.message || err?.response?.data?.error || 'Không tải được hồ sơ khách hàng.';
  } finally {
    loading.value = false;
  }
}

async function submitProfile() {
  saving.value = true;
  error.value = '';
  successMessage.value = '';

  try {
    const payload = {
      tenKhachHang: form.tenKhachHang,
      email: form.email || null,
      soDienThoai: form.soDienThoai || null,
      gioiTinh: gioiTinhValue.value === '' ? null : gioiTinhValue.value === 'male',
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
    successMessage.value = 'Cập nhật hồ sơ thành công.';
  } catch (err) {
    error.value = err?.response?.data?.message || err?.response?.data?.error || 'Cập nhật hồ sơ thất bại.';
  } finally {
    saving.value = false;
  }
}

onMounted(async () => {
  await loadProvinces();
  await loadProfile();
});
</script>

<style scoped>
.profile-page {
  background: #f6f8fb;
  min-height: calc(100vh - 220px);
}

.profile-title {
  font-size: 32px;
  font-weight: 800;
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
  font-weight: 800;
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

@media (max-width: 767.98px) {
  .profile-title {
    font-size: 26px;
  }
}
</style>