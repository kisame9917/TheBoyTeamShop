<template>
  <div class="staff-form-page">
    <div class="page-header">
      <h2 class="page-title">{{ isEdit ? 'Sửa nhân viên' : 'Thêm nhân viên' }}</h2>

      <button type="button" class="btn-back" @click="goBack">
        ← Quay lại danh sách
      </button>
    </div>

    <div class="form-card">
      <!-- Avatar centered -->
      <div class="avatar-center">
        <div class="avatar-wrap" @click="triggerPickFile" title="Bấm để chọn ảnh">
          <img
              v-if="avatarPreview"
              :src="avatarPreview"
              class="avatar-img"
              alt="avatar"
              @error="onAvatarImgError"
          />
          <div v-else class="avatar-fallback">NV</div>

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
              accept="image/png,image/jpeg,image/jpg"
              @change="onAvatarFileChange"
          />
        </div>

        <div class="avatar-meta" v-if="avatarNameText">{{ avatarNameText }}</div>
        <div class="avatar-hint">PNG, JPG, JPEG - tối đa 5MB.</div>
      </div>

      <form @submit.prevent="submit">
        <div class="grid-2">
          <!-- 1) Mã nhân viên -->
          <div class="form-group">
            <label>Mã nhân viên</label>
            <input class="form-input" v-model="form.maNhanVien" disabled />
          </div>

          <!-- 2) Chức vụ -->
          <div class="form-group">
            <label>Chức vụ</label>
            <select class="form-input" v-model="form.quyenHanKey" :disabled="!isAdmin">
              <option value="ADMIN">ADMIN</option>
              <option value="NHAN_VIEN">NHÂN VIÊN</option>
            </select>
          </div>

          <!-- 3) Tên nhân viên -->
          <div class="form-group">
            <label>Tên nhân viên</label>
            <input class="form-input" v-model="form.tenNhanVien" />
          </div>

          <!-- 4) Giới tính -->
          <div class="form-group">
            <label>Giới tính</label>
            <div class="radio-row">
              <label class="radio-item">
                <input type="radio" v-model="form.gioiTinh" :value="true" />
                Nam
              </label>
              <label class="radio-item">
                <input type="radio" v-model="form.gioiTinh" :value="false" />
                Nữ
              </label>
              <label class="radio-item">
                <input type="radio" v-model="form.gioiTinh" :value="null" />
                Khác
              </label>
            </div>
          </div>

          <!-- 5) Số điện thoại -->
          <div class="form-group">
            <label>Số điện thoại</label>
            <input class="form-input" v-model="form.soDienThoai" placeholder="Chỉ nhập số" />
          </div>

          <!-- 6) Email -->
          <div class="form-group">
            <label>Email</label>
            <input class="form-input" v-model="form.email" placeholder="Email" />
          </div>

          <!-- 7) CCCD -->
          <div class="form-group">
            <label>Căn cước công dân</label>
            <input class="form-input" v-model="form.cccd" placeholder="Chỉ nhập số" />
          </div>

          <!-- 8) Ngày sinh -->
          <div class="form-group">
            <label>Ngày sinh</label>
            <input class="form-input" type="date" v-model="form.ngaySinh" />
          </div>

          <!-- 9) Tỉnh/Thành phố -->
          <div class="form-group">
            <label>Tỉnh/Thành phố</label>
            <select class="form-input" v-model="addr.provinceCode" @change="onProvinceChange">
              <option value="">-- Chọn Tỉnh/Thành phố --</option>
              <option v-for="p in provinces" :key="p.code" :value="String(p.code)">
                {{ p.name }}
              </option>
            </select>
          </div>

          <!-- 10) Quận/Huyện -->
          <div class="form-group">
            <label>Quận/Huyện</label>
            <select
                class="form-input"
                v-model="addr.districtCode"
                @change="onDistrictChange"
                :disabled="!addr.provinceCode"
            >
              <option value="">-- Chọn Quận/Huyện --</option>
              <option v-for="d in districts" :key="d.code" :value="String(d.code)">
                {{ d.name }}
              </option>
            </select>
          </div>

          <!-- 11) Xã/Phường -->
          <div class="form-group">
            <label>Xã/Phường</label>
            <select class="form-input" v-model="addr.wardCode" :disabled="!addr.districtCode">
              <option value="">-- Chọn Xã/Phường --</option>
              <option v-for="w in wards" :key="w.code" :value="String(w.code)">
                {{ w.name }}
              </option>
            </select>
          </div>

          <!-- 12) Tên đường -->
          <div class="form-group">
            <label>Tên đường</label>
            <input class="form-input" v-model="addr.detail" placeholder="Số nhà, tên đường..." />
          </div>

          <!-- 13) Tài khoản -->
          <div class="form-group">
            <label>Tài khoản</label>
            <input class="form-input" v-model="form.taiKhoan" :disabled="!isAdmin" />
          </div>

          <!-- 14) Mật khẩu -->
          <div class="form-group">
            <label>
              Mật khẩu
              <span v-if="isEdit" class="hint">(để trống = giữ nguyên)</span>
            </label>
            <input
                class="form-input"
                type="password"
                v-model="form.matKhau"
                :disabled="!isAdmin"
                :placeholder="isAdmin ? '' : 'Chỉ ADMIN được đổi mật khẩu'"
            />
          </div>
        </div>

        <div v-if="err" class="error-text">{{ err }}</div>

        <div class="form-actions">
          <button type="button" class="btn-cancel" @click="goBack">Hủy</button>
          <button type="submit" class="btn-save" :disabled="saving || uploading">
            {{ uploading ? 'Đang upload ảnh...' : (saving ? 'Đang lưu...' : (isEdit ? 'Lưu thay đổi' : 'Lưu nhân viên')) }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../../services/http'
import { useAuthStore } from '../../stores/auth'


const auth = useAuthStore()
const isAdmin = computed(() => !!auth.isAdmin)
const ROLE_TO_ID = { ADMIN: 1, NHAN_VIEN: 2 }

const route = useRoute()
const router = useRouter()

const isEdit = computed(function () {
  return !!route.params.id
})

const saving = ref(false)
const err = ref('')
const uploading = ref(false)

/** ===== Form ===== */
const form = reactive({
  id: null,
  maNhanVien: '',
  quyenHanKey: 'NHAN_VIEN',
  tenNhanVien: '',
  soDienThoai: '',
  cccd: '',
  email: '',
  taiKhoan: '',
  matKhau: '',
  ngaySinh: '',
  gioiTinh: null,
  diaChi: '',
  trangThai: true,
  anhDaiDien: '' // url lưu DB: "/uploads/nhanvien/xxx.jpg"
})

/** ===== Avatar ===== */
const fileInput = ref(null)
const avatarPreview = ref('')
const localBlobUrl = ref('')
const avatarFileMeta = ref('')

const avatarNameText = computed(function () {
  if (avatarFileMeta.value) return avatarFileMeta.value
  var url = String(form.anhDaiDien || '').trim()
  if (!url) return ''
  var clean = url.split('?')[0]
  var parts = clean.split('/')
  return parts[parts.length - 1] || ''
})

function triggerPickFile() {
  if (fileInput.value) fileInput.value.click()
}

function revokeLocalBlob() {
  if (localBlobUrl.value && String(localBlobUrl.value).indexOf('blob:') === 0) {
    URL.revokeObjectURL(localBlobUrl.value)
  }
  localBlobUrl.value = ''
}

function clearAvatar() {
  revokeLocalBlob()
  avatarPreview.value = ''
  avatarFileMeta.value = ''
  form.anhDaiDien = '' // gửi "" => service normalize về default
  if (fileInput.value) fileInput.value.value = ''
}

function onAvatarImgError() {
  // nếu ảnh server lỗi, fallback về blob local (nếu có)
  if (localBlobUrl.value) avatarPreview.value = localBlobUrl.value
  else avatarPreview.value = ''
}

/** ===== Resolve URL /uploads ===== */
var FALLBACK_BACKEND = 'http://localhost:8080'

function getBackendOrigin() {
  var base = ''
  if (http && http.defaults && http.defaults.baseURL) base = String(http.defaults.baseURL).trim()

  if (base.indexOf('http://') === 0 || base.indexOf('https://') === 0) {
    try {
      var u = new URL(base)
      return u.origin
    } catch (e) {
      return FALLBACK_BACKEND
    }
  }
  return FALLBACK_BACKEND
}

function resolveFileUrl(url) {
  var u = String(url || '').trim()
  if (!u) return ''
  if (u.indexOf('http://') === 0 || u.indexOf('https://') === 0 || u.indexOf('data:image') === 0) return u
  var origin = getBackendOrigin()
  if (u[0] === '/') return origin + u
  return origin + '/' + u
}

async function uploadNhanVienAvatar(file) {
  var fd = new FormData()
  fd.append('file', file)
  var res = await http.post('/api/upload/nhan-vien-avatar', fd, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  var url = res && res.data ? res.data.url : ''
  if (!url) throw new Error('Upload thành công nhưng không nhận được url')
  return String(url)
}

async function onAvatarFileChange(e) {
  var files = e && e.target ? e.target.files : null
  var file = files && files.length ? files[0] : null
  if (!file) return

  // validate
  var okType = (file.type === 'image/png' || file.type === 'image/jpeg' || file.type === 'image/jpg')
  if (!okType) {
    alert('Chỉ chấp nhận PNG, JPG, JPEG')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    alert('Tối đa 5MB')
    return
  }

  // preview local trước
  revokeLocalBlob()
  localBlobUrl.value = URL.createObjectURL(file)
  avatarPreview.value = localBlobUrl.value
  avatarFileMeta.value = file.name + ' • ' + (file.size / 1024 / 1024).toFixed(1) + ' MB'

  uploading.value = true
  try {
    var url = await uploadNhanVienAvatar(file) // "/uploads/nhanvien/xxx.jpg"
    form.anhDaiDien = url

    // preview server + chống cache
    avatarPreview.value = resolveFileUrl(url) + '?t=' + Date.now()
  } catch (ex) {
    var msg = ''
    if (ex && ex.response && ex.response.data && ex.response.data.message) msg = ex.response.data.message
    else msg = ex && ex.message ? ex.message : 'Upload thất bại'
    alert(msg)
    // giữ preview local
  } finally {
    uploading.value = false
  }
}

/** ===== Address ===== */
const provinces = ref([])
const districts = ref([])
const wards = ref([])

const addr = reactive({
  provinceCode: '',
  districtCode: '',
  wardCode: '',
  detail: ''
})

async function fetchProvinces() {
  var r = await fetch('https://provinces.open-api.vn/api/p/')
  provinces.value = await r.json()
}

async function fetchDistricts(provinceCode) {
  var r = await fetch('https://provinces.open-api.vn/api/p/' + provinceCode + '?depth=2')
  var data = await r.json()
  districts.value = data && data.districts ? data.districts : []
}

async function fetchWards(districtCode) {
  var r = await fetch('https://provinces.open-api.vn/api/d/' + districtCode + '?depth=2')
  var data = await r.json()
  wards.value = data && data.wards ? data.wards : []
}

async function onProvinceChange() {
  addr.districtCode = ''
  addr.wardCode = ''
  wards.value = []
  if (addr.provinceCode) await fetchDistricts(addr.provinceCode)
  else districts.value = []
}

async function onDistrictChange() {
  addr.wardCode = ''
  if (addr.districtCode) await fetchWards(addr.districtCode)
  else wards.value = []
}

function buildDiaChi() {
  var p = null
  var d = null
  var w = null

  for (var i = 0; i < provinces.value.length; i++) {
    if (String(provinces.value[i].code) === String(addr.provinceCode)) { p = provinces.value[i]; break }
  }
  for (var j = 0; j < districts.value.length; j++) {
    if (String(districts.value[j].code) === String(addr.districtCode)) { d = districts.value[j]; break }
  }
  for (var k = 0; k < wards.value.length; k++) {
    if (String(wards.value[k].code) === String(addr.wardCode)) { w = wards.value[k]; break }
  }

  var parts = []
  if (addr.detail && String(addr.detail).trim()) parts.push(String(addr.detail).trim())
  if (w && w.name) parts.push(w.name)
  if (d && d.name) parts.push(d.name)
  if (p && p.name) parts.push(p.name)
  return parts.join(', ')
}

/** parse diaChi -> set lại dropdown khi edit */
function normalizeText(s) {
  return String(s || '')
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/đ/g, 'd')
      .replace(/\s+/g, ' ')
      .trim()
}

async function prefillAddressFromDiaChi(diaChi) {
  var parts = String(diaChi || '').split(',').map(function (x) { return String(x).trim() }).filter(Boolean)
  if (parts.length < 4) {
    addr.detail = parts[0] || ''
    return
  }

  var provinceName = parts[parts.length - 1]
  var districtName = parts[parts.length - 2]
  var wardName = parts[parts.length - 3]
  var detail = parts.slice(0, parts.length - 3).join(', ')

  var p = null
  for (var i = 0; i < provinces.value.length; i++) {
    if (normalizeText(provinces.value[i].name) === normalizeText(provinceName)) { p = provinces.value[i]; break }
  }
  if (!p) {
    addr.detail = detail || parts[0] || ''
    return
  }

  addr.provinceCode = String(p.code)
  await fetchDistricts(addr.provinceCode)

  var d = null
  for (var j = 0; j < districts.value.length; j++) {
    if (normalizeText(districts.value[j].name) === normalizeText(districtName)) { d = districts.value[j]; break }
  }
  if (!d) {
    addr.detail = detail
    return
  }

  addr.districtCode = String(d.code)
  await fetchWards(addr.districtCode)

  var w = null
  for (var k = 0; k < wards.value.length; k++) {
    if (normalizeText(wards.value[k].name) === normalizeText(wardName)) { w = wards.value[k]; break }
  }
  if (w) addr.wardCode = String(w.code)

  addr.detail = detail
}

/** ===== Helpers/API ===== */
function unwrapList(data) {
  if (!data) return []
  if (Array.isArray(data)) return data
  if (data.result && Array.isArray(data.result)) return data.result
  if (data.content && Array.isArray(data.content)) return data.content
  return []
}

function unwrapObj(data) {
  if (!data) return null
  if (data.result && typeof data.result === 'object') return data.result
  return data
}

function safeStr(v) {
  return String(v == null ? '' : v).toLowerCase().trim()
}

function normalizeStaff(x) {
  x = x || {}
  var tenQH = ''
  if (x.tenQuyenHan) tenQH = x.tenQuyenHan
  else if (x.quyenHan && x.quyenHan.tenQuyenHan) tenQH = x.quyenHan.tenQuyenHan

  return {
    id: x.id != null ? x.id : null,
    maNhanVien: x.maNhanVien != null ? x.maNhanVien : '',
    tenNhanVien: x.tenNhanVien != null ? x.tenNhanVien : '',
    soDienThoai: x.soDienThoai != null ? x.soDienThoai : '',
    cccd: x.cccd != null ? x.cccd : '',
    email: x.email != null ? x.email : '',
    taiKhoan: x.taiKhoan != null ? x.taiKhoan : '',
    ngaySinh: x.ngaySinh != null ? x.ngaySinh : null,
    gioiTinh: (x.gioiTinh === true || x.gioiTinh === false) ? x.gioiTinh : null,
    diaChi: x.diaChi != null ? x.diaChi : '',
    trangThai: (x.trangThai === true || x.trangThai === false) ? x.trangThai : true,
    tenQuyenHan: tenQH,
    anhDaiDien: x.anhDaiDien ? x.anhDaiDien : (x.anh_dai_dien ? x.anh_dai_dien : '')
  }
}

async function apiGetAllStaff() {
  var res = await http.get('/api/nhan-vien')
  return unwrapList(res ? res.data : null)
}

async function apiGetStaffById(id) {
  var res = await http.get('/api/nhan-vien/' + id)
  return unwrapObj(res ? res.data : null)
}

function generateNextCode(all) {
  var maxN = 0
  for (var i = 0; i < (all || []).length; i++) {
    var s = normalizeStaff(all[i])
    var m = String(s.maNhanVien || '').match(/^NV(\d+)$/i)
    if (m) {
      var n = parseInt(m[1], 10)
      if (!isNaN(n)) maxN = Math.max(maxN, n)
    }
  }
  var next = maxN + 1
  return 'NV' + String(next).padStart(3, '0')
}

function isDigitsOnly(v) {
  var s = String(v == null ? '' : v).trim()
  return s.length > 0 && /^\d+$/.test(s)
}

function isAtLeast18(dateStr) {
  if (!dateStr) return false
  var dob = new Date(dateStr)
  if (isNaN(dob.getTime())) return false
  var today = new Date()
  var limit = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate())
  return dob <= limit
}

async function validateDuplicates(all) {
  var excludeId = isEdit.value ? Number(route.params.id) : null

  var username = safeStr(form.taiKhoan)
  if (username) {
    for (var i = 0; i < (all || []).length; i++) {
      var s = normalizeStaff(all[i])
      if (s.id !== excludeId && safeStr(s.taiKhoan) === username) return 'Tài khoản đã tồn tại'
    }
  }

  var cccd = String(form.cccd == null ? '' : form.cccd).trim()
  if (cccd) {
    for (var j = 0; j < (all || []).length; j++) {
      var s2 = normalizeStaff(all[j])
      if (s2.id !== excludeId && String(s2.cccd == null ? '' : s2.cccd).trim() === cccd) return 'CCCD đã tồn tại'
    }
  }

  return ''
}

function validateForm() {
  if (!String(form.maNhanVien || '').trim()) return 'Mã nhân viên không được trống'
  if (!String(form.tenNhanVien || '').trim()) return 'Tên nhân viên không được trống'

  if (!String(form.soDienThoai || '').trim() || !isDigitsOnly(form.soDienThoai)) return 'Số điện thoại phải là số'
  if (!String(form.cccd || '').trim() || !isDigitsOnly(form.cccd)) return 'CCCD phải là số'
  if (!form.ngaySinh || !isAtLeast18(form.ngaySinh)) return 'Nhân viên phải đủ 18 tuổi'

  if (!addr.provinceCode || !addr.districtCode || !addr.wardCode || !String(addr.detail || '').trim()) {
    return 'Vui lòng chọn đầy đủ Tỉnh/Quận/Xã và nhập tên đường'
  }

  if (!String(form.taiKhoan || '').trim()) return 'Tài khoản không được trống'
  if (!isEdit.value && !String(form.matKhau || '').trim()) return 'Mật khẩu không được trống khi thêm mới'

  return ''
}

function goBack() {
  router.push({ name: 'staff' })
}

/** ===== Load ===== */
async function loadData() {
  await fetchProvinces()
  var all = await apiGetAllStaff()

  if (!isEdit.value) {
    form.maNhanVien = generateNextCode(all)
    form.trangThai = true
    return
  }

  var id = route.params.id
  var detail = null
  try {
    detail = await apiGetStaffById(id)
  } catch (e) {
    detail = null
  }

  // fallback nếu apiGetById fail
  var found = null
  if (!detail) {
    for (var i = 0; i < all.length; i++) {
      var tmp = normalizeStaff(all[i])
      if (String(tmp.id) === String(id)) { found = all[i]; break }
    }
    detail = found
  }

  var s = normalizeStaff(detail || {})

  form.id = s.id
  form.maNhanVien = s.maNhanVien
  form.tenNhanVien = s.tenNhanVien
  form.soDienThoai = s.soDienThoai
  form.cccd = s.cccd
  form.email = s.email
  form.taiKhoan = s.taiKhoan
  form.matKhau = ''
  form.ngaySinh = s.ngaySinh ? String(s.ngaySinh).slice(0, 10) : ''
  form.gioiTinh = (s.gioiTinh === true || s.gioiTinh === false) ? s.gioiTinh : null
  form.diaChi = s.diaChi || ''
  form.trangThai = (s.trangThai === true || s.trangThai === false) ? s.trangThai : true

  var roleName = String(s.tenQuyenHan || '').toUpperCase()
  form.quyenHanKey = roleName.indexOf('ADMIN') >= 0 ? 'ADMIN' : 'NHAN_VIEN'

  form.anhDaiDien = s.anhDaiDien || ''
  if (form.anhDaiDien) {
    avatarPreview.value = resolveFileUrl(form.anhDaiDien) + '?t=' + Date.now()
  }

  // ✅ mapping địa chỉ vào dropdown khi edit
  if (s.diaChi) {
    await prefillAddressFromDiaChi(s.diaChi)
  }
}

/** ===== Submit ===== */
async function submit() {
  err.value = ''

  var msg = validateForm()
  if (msg) {
    err.value = msg
    return
  }

  var all = await apiGetAllStaff()
  var dupMsg = await validateDuplicates(all)
  if (dupMsg) {
    err.value = dupMsg
    return
  }

  var actionText = isEdit.value ? 'sửa' : 'thêm mới'
  if (!confirm('Bạn có chắc chắn ' + actionText + ' nhân viên không ?')) return

  saving.value = true
  try {
    var payload = {
      quyenHanId: ROLE_TO_ID[form.quyenHanKey],
      maNhanVien: String(form.maNhanVien || '').trim(),
      tenNhanVien: String(form.tenNhanVien || '').trim(),
      soDienThoai: String(form.soDienThoai || '').trim(),
      cccd: String(form.cccd || '').trim(),
      email: String(form.email || '').trim() ? String(form.email || '').trim() : null,
      taiKhoan: String(form.taiKhoan || '').trim(),
      ngaySinh: form.ngaySinh,
      gioiTinh: (form.gioiTinh === true || form.gioiTinh === false) ? form.gioiTinh : null,
      diaChi: buildDiaChi(),
      trangThai: isEdit.value ? form.trangThai : true,
      anhDaiDien: form.anhDaiDien
    }

    // chỉ gửi mật khẩu nếu user nhập (edit)
    if (!isEdit.value) payload.matKhau = String(form.matKhau || '').trim()
    else if (String(form.matKhau || '').trim()) payload.matKhau = String(form.matKhau || '').trim()
    if (!isAdmin.value) {
      form.matKhau = ''
    }
    if (isEdit.value) {
      var id = route.params.id
      await http.put('/api/nhan-vien/' + id, payload)
    } else {
      await http.post('/api/nhan-vien', payload)
    }

    goBack()
  } catch (e) {
    var m = ''
    if (e && e.response && e.response.data && e.response.data.message) m = e.response.data.message
    else m = e && e.message ? e.message : 'Có lỗi xảy ra'
    err.value = m
  } finally {
    saving.value = false
  }
}

onMounted(loadData)
onBeforeUnmount(function () { revokeLocalBlob() })
</script>

<style scoped>
.staff-form-page { font-size: 15px; }

.page-header{
  display:flex;
  justify-content:space-between;
  align-items:center;
  margin-bottom:14px;
}
.page-title{
  margin:0;
  font-size:18px;
  font-weight:700;
}

.form-card{
  border:1px solid #e5e7eb;
  border-radius:10px;
  padding:18px;
  background:#fff;
}

/* Avatar */
.avatar-center{
  display:flex;
  flex-direction:column;
  align-items:center;
  margin-bottom:16px;
}
.avatar-wrap{
  width:78px;
  height:78px;
  border-radius:999px;
  border:1px solid #e5e7eb;
  overflow:hidden;
  position:relative;
  cursor:pointer;
  display:flex;
  align-items:center;
  justify-content:center;
}
.avatar-img{ width:100%; height:100%; object-fit:cover; }
.avatar-fallback{ font-weight:700; }

.avatar-remove{
  position:absolute;
  top:-6px;
  right:-6px;
  width:22px;
  height:22px;
  border-radius:999px;
  border:1px solid #e5e7eb;
  background:#fff;
  cursor:pointer;
  line-height:18px;
}

.avatar-uploading{
  position:absolute;
  bottom:6px;
  right:6px;
  width:22px;
  height:22px;
  border-radius:999px;
  border:1px solid #e5e7eb;
  background:#fff;
  display:flex;
  align-items:center;
  justify-content:center;
  font-size:12px;
}

.avatar-input{ display:none; }
.avatar-meta{
  margin-top:8px;
  font-size:12px;
  opacity:0.85;
}
.avatar-hint{
  margin-top:4px;
  font-size:12px;
  opacity:0.7;
}

/* Grid */
.grid-2{
  display:grid;
  grid-template-columns:1fr 1fr;
  gap:12px 18px;
}
@media (max-width:900px){
  .grid-2{ grid-template-columns:1fr; }
}

.form-group label{
  display:block;
  margin-bottom:6px;
  font-weight:600;
}
.form-input{
  width:100%;
  padding:9px 10px;
  border-radius:6px;
  border:1px solid #d0d7de;
  outline:none;
  font-size:15px;
  background:#fff;
}

/* Mã NV disabled */
.form-input:disabled{
  background:#f3f4f6;
  color:#6b7280;
}

.hint{
  margin-left:6px;
  font-weight:400;
  font-size:12px;
  opacity:0.7;
}

.radio-row{
  display:flex;
  gap:14px;
  flex-wrap:wrap;
  padding-top:6px;
}
.radio-item{
  display:inline-flex;
  align-items:center;
  gap:6px;
}

.form-actions{
  display:flex;
  justify-content:flex-end;
  gap:10px;
  margin-top:16px;
}

.btn-back{
  padding:8px 12px;
  border-radius:6px;
  cursor:pointer;
  border:1px solid #cbd5e1;
  background:#e5e7eb;
  color:#111827;
}

.btn-cancel{
  padding:9px 14px;
  border-radius:6px;
  cursor:pointer;
  border:1px solid #d0d7de;
  background:#fff;
}

.btn-save{
  padding:9px 14px;
  border-radius:6px;
  cursor:pointer;
  border:1px solid #1d4ed8;
  background:#1d4ed8;
  color:#fff;
}

.btn-save:disabled{
  opacity:0.6;
  cursor:not-allowed;
}

.error-text{
  margin-top:10px;
  color:red;
  font-weight:600;
}
</style>
