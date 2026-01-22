<template>
  <div class="customer-form-page">
    <div class="page-head">
      <div class="page-title">
        <h2>Quản lý khách hàng / <span>{{ isEdit ? 'Cập nhật' : 'Thêm mới' }}</span></h2>
      </div>

      <button type="button" class="btn-back" @click="goBack">
        ← Quay lại danh sách
      </button>
    </div>

    <div class="card">
      <form @submit.prevent="submit">
        <!-- AVATAR -->
        <div class="avatar-zone">
          <div class="avatar-wrap" @click="openFilePicker" title="Bấm để chọn ảnh">
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
          />
        </div>

        <!-- FORM GRID -->
        <div class="form-grid">
          <!-- Mã KH -->
          <div class="form-group">
            <label>Mã khách hàng</label>
            <input class="form-input input-disabled" v-model="form.maKhachHang" disabled />
          </div>

          <!-- Trạng thái -->
          <div class="form-group">
            <label>Trạng thái</label>
            <select class="form-input" v-model="form.trangThai">
              <option :value="true">Hoạt động</option>
              <option :value="false">Không hoạt động</option>
            </select>
          </div>

          <!-- Tên KH -->
          <div class="form-group">
            <label>Tên khách hàng <span class="req">*</span></label>
            <input class="form-input" v-model="form.tenKhachHang" placeholder="Nhập tên khách hàng" />
          </div>

          <!-- Giới tính -->
          <div class="form-group">
            <label>Giới tính</label>
            <div class="radio-row">
              <label class="radio-item">
                <input type="radio" :value="true" v-model="form.gioiTinh" /> Nam
              </label>
              <label class="radio-item">
                <input type="radio" :value="false" v-model="form.gioiTinh" /> Nữ
              </label>
            </div>
          </div>

          <!-- SĐT -->
          <div class="form-group">
            <label>Số điện thoại</label>
            <input class="form-input" v-model="form.soDienThoai" placeholder="VD: 0912345678" />
          </div>

          <!-- Email -->
          <div class="form-group">
            <label>Email</label>
            <input class="form-input" v-model="form.email" placeholder="VD: abc@gmail.com" />
          </div>

          <!-- Tài khoản -->
          <div class="form-group">
            <label>Tài khoản <span class="req">*</span></label>
            <input class="form-input" v-model="form.taiKhoan" placeholder="Nhập tài khoản" />
          </div>

          <!-- Mật khẩu -->
          <div class="form-group">
            <label>Mật khẩu</label>
            <input
                class="form-input"
                type="password"
                v-model="form.matKhau"
                :placeholder="isEdit ? 'Để trống nếu không muốn đổi' : 'Nhập mật khẩu'"
            />
          </div>
        </div>

        <!-- ADDRESS -->
        <div class="section-title">Địa chỉ mặc định</div>

        <div class="form-grid">
          <div class="form-group">
            <label>Người nhận</label>
            <input class="form-input" v-model="form.tenNguoiNhan" placeholder="Tên người nhận" />
          </div>

          <div class="form-group">
            <label>SĐT người nhận</label>
            <input class="form-input" v-model="form.sdtNguoiNhan" placeholder="SĐT người nhận" />
          </div>

          <div class="form-group">
            <label>Tỉnh/Thành phố</label>
            <select class="form-input" v-model="form.tinhThanh" @change="onProvinceChange">
              <option value="">-- Chọn Tỉnh/Thành phố --</option>
              <option v-for="p in provinces" :key="p.code" :value="p.name">{{ p.name }}</option>
            </select>
          </div>

          <div class="form-group">
            <label>Quận/Huyện</label>
            <select class="form-input" v-model="form.quanHuyen" @change="onDistrictChange" :disabled="!districts.length">
              <option value="">-- Chọn Quận/Huyện --</option>
              <option v-for="d in districts" :key="d.code" :value="d.name">{{ d.name }}</option>
            </select>
          </div>

          <div class="form-group">
            <label>Phường/Xã</label>
            <select class="form-input" v-model="form.phuongXa" :disabled="!wards.length">
              <option value="">-- Chọn Phường/Xã --</option>
              <option v-for="w in wards" :key="w.code" :value="w.name">{{ w.name }}</option>
            </select>
          </div>

          <div class="form-group">
            <label>Địa chỉ chi tiết</label>
            <input class="form-input" v-model="form.diaChiChiTiet" placeholder="VD: 12 Cửa Đông" />
          </div>
        </div>

        <!-- ACTIONS -->
        <div class="actions">
          <button type="button" class="btn btn-ghost" @click="onCancel" :disabled="saving">Hủy</button>
          <button type="submit" class="btn btn-primary" :disabled="saving">
            {{ saving ? 'Đang lưu...' : (isEdit ? 'Lưu thay đổi' : 'Thêm mới') }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../../services/http'

const router = useRouter()
const route = useRoute()

const id = computed(() => route.params.id)
const isEdit = computed(() => !!id.value)

const saving = ref(false)

// ===== form =====
const form = reactive({
  maKhachHang: '',
  tenKhachHang: '',
  gioiTinh: true,
  soDienThoai: '',
  email: '',
  taiKhoan: '',
  matKhau: '',
  trangThai: true,

  anhDaiDien: '',

  // address (flat fields để BE lưu dia_chi_khach_hang)
  tenNguoiNhan: '',
  sdtNguoiNhan: '',
  tinhThanh: '',
  quanHuyen: '',
  phuongXa: '',
  diaChiChiTiet: '',
  quocGia: 'Việt Nam',
})

// ===== avatar =====
const fileInput = ref(null)
const selectedFile = ref(null)
const avatarPreview = ref('')

function openFilePicker() {
  fileInput.value?.click()
}

function onFileChange(e) {
  const file = e?.target?.files?.[0]
  if (!file) return
  selectedFile.value = file
  avatarPreview.value = URL.createObjectURL(file)
}

// ===== address data (open-api.vn) =====
const provinces = ref([])
const districts = ref([])
const wards = ref([])

async function loadProvinces() {
  // dùng public API để giống combo box như form nhân viên
  const res = await fetch('https://provinces.open-api.vn/api/?depth=3')
  const data = await res.json()
  provinces.value = Array.isArray(data) ? data : []
}

function onProvinceChange() {
  const p = provinces.value.find(x => x.name === form.tinhThanh)
  districts.value = p?.districts || []
  form.quanHuyen = ''
  form.phuongXa = ''
  wards.value = []
}

function onDistrictChange() {
  const p = provinces.value.find(x => x.name === form.tinhThanh)
  const d = (p?.districts || []).find(x => x.name === form.quanHuyen)
  wards.value = d?.wards || []
  form.phuongXa = ''
}

// ===== utils =====
function resolveFileUrl(url) {
  const u = String(url || '').trim()
  if (!u) return ''
  if (u.startsWith('http://') || u.startsWith('https://') || u.startsWith('data:image')) return u
  const base = 'http://localhost:8080'
  return u.startsWith('/') ? base + u : base + '/' + u
}

function goBack() {
  router.push('/customers')
}

function onCancel() {
  if (confirm('Bạn muốn hủy thao tác và quay lại danh sách?')) goBack()
}

function validate() {
  if (!String(form.tenKhachHang || '').trim()) return 'Tên khách hàng không được để trống'
  if (!String(form.taiKhoan || '').trim()) return 'Tài khoản không được để trống'
  if (!isEdit.value && !String(form.matKhau || '').trim()) return 'Mật khẩu không được để trống'
  return ''
}

// ===== API =====
async function fetchNextCode() {
  const res = await http.get('/api/khach-hang/next-code')
  form.maKhachHang = res?.data?.maKhachHang || ''
}

async function fetchDetail() {
  const res = await http.get('/api/khach-hang/' + id.value)
  const kh = res.data || {}

  form.maKhachHang = kh.maKhachHang || ''
  form.tenKhachHang = kh.tenKhachHang || ''
  form.gioiTinh = kh.gioiTinh !== null && kh.gioiTinh !== undefined ? kh.gioiTinh : true
  form.soDienThoai = kh.soDienThoai || ''
  form.email = kh.email || ''
  form.taiKhoan = kh.taiKhoan || ''
  form.trangThai = kh.trangThai !== null && kh.trangThai !== undefined ? kh.trangThai : true
  form.anhDaiDien = kh.anhDaiDien || ''

  avatarPreview.value = resolveFileUrl(form.anhDaiDien)

  const dc = kh.diaChiMacDinh || null
  form.tenNguoiNhan = dc?.tenNguoiNhan || ''
  form.sdtNguoiNhan = dc?.soDienThoai || ''
  form.tinhThanh = dc?.tinhThanh || ''
  form.quanHuyen = dc?.quanHuyen || ''
  form.phuongXa = dc?.phuongXa || ''
  form.diaChiChiTiet = dc?.diaChiChiTiet || ''
  form.quocGia = dc?.quocGia || 'Việt Nam'

  // set combo list theo dữ liệu đang có
  onProvinceChange()
  form.quanHuyen = dc?.quanHuyen || ''
  onDistrictChange()
  form.phuongXa = dc?.phuongXa || ''
}

async function uploadAvatarIfNeeded() {
  if (!selectedFile.value) return

  const fd = new FormData()
  fd.append('file', selectedFile.value)

  const res = await http.post('/api/khach-hang/upload-avatar', fd, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })

  const url = res?.data?.url || ''
  if (url) {
    form.anhDaiDien = url
    avatarPreview.value = resolveFileUrl(url)
  }
}

async function submit() {
  const msg = validate()
  if (msg) return alert(msg)

  if (!confirm(isEdit.value ? 'Xác nhận lưu thay đổi?' : 'Xác nhận thêm mới khách hàng?')) return

  try {
    saving.value = true

    // upload ảnh trước (nếu có)
    await uploadAvatarIfNeeded()

    const payload = {
      tenKhachHang: form.tenKhachHang,
      gioiTinh: form.gioiTinh,
      soDienThoai: form.soDienThoai,
      email: form.email,
      taiKhoan: form.taiKhoan,
      matKhau: form.matKhau, // update: BE chỉ đổi nếu không rỗng
      trangThai: form.trangThai,
      anhDaiDien: form.anhDaiDien,

      // ✅ địa chỉ (flat fields)
      tenNguoiNhan: form.tenNguoiNhan,
      sdtNguoiNhan: form.sdtNguoiNhan,
      tinhThanh: form.tinhThanh,
      quanHuyen: form.quanHuyen,
      phuongXa: form.phuongXa,
      diaChiChiTiet: form.diaChiChiTiet,
      quocGia: form.quocGia || 'Việt Nam',
    }

    if (isEdit.value) {
      await http.put('/api/khach-hang/' + id.value, payload)
      alert('Cập nhật thành công')
    } else {
      await http.post('/api/khach-hang', payload)
      alert('Thêm mới thành công')
    }

    goBack()
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || 'Lỗi không xác định'
    alert(msg)
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await loadProvinces()
  if (isEdit.value) await fetchDetail()
  else await fetchNextCode()
})
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
  background: #f3f4f6; /* ✅ xám */
  cursor: pointer;
}

.card{
  background:#fff;
  border:1px solid #e5e7eb;
  border-radius:12px;
  padding: 16px;
}

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
.hidden-file{ display:none; }

.form-grid{
  display:grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 16px;
  align-items:end;
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
  background:#f3f4f6; /* ✅ xám */
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

.section-title{
  margin-top: 14px;
  margin-bottom: 10px;
  font-weight: 800;
}

.actions{
  display:flex;
  justify-content:flex-end;
  gap:10px;
  margin-top: 14px;
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
.btn-ghost{
  background:#fff;
}
.btn:disabled{
  opacity:.6;
  cursor:not-allowed;
}

@media (max-width: 980px){
  .form-grid{ grid-template-columns: 1fr; }
}
</style>
