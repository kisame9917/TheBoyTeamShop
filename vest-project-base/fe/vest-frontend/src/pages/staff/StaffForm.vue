<template>
  <div class="container-fluid py-3">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-person-badge fs-4"></i>
        <h5 class="mb-0">{{ isEdit ? 'Sửa nhân viên' : 'Thêm nhân viên' }}</h5>
      </div>
      <button type="button" class="btn btn-outline-secondary btn-sm" @click="goBack">
        <i class="bi bi-arrow-left me-1"></i> Quay lại danh sách
      </button>
    </div>

    <!-- Form card -->
    <div class="card shadow-sm">
      <div class="card-body">
        <!-- Avatar centered -->
        <div class="d-flex flex-column align-items-center mb-3">
          <div class="avatar-wrap" @click="triggerPickFile" title="Bấm để chọn ảnh">
            <img v-if="avatarPreview" :src="avatarPreview" class="avatar-img" alt="avatar" @error="onAvatarImgError" />
            <div v-else class="avatar-fallback">NV</div>
            <button v-if="avatarPreview" type="button" class="avatar-remove" @click.stop="clearAvatar" title="Xóa ảnh">×</button>
            <div v-if="uploading" class="avatar-uploading">⏳</div>
            <input
                ref="fileInput"
                class="avatar-input"
                type="file"
                accept="image/png,image/jpeg,image/jpg"
                @change="onAvatarFileChange"
            />
          </div>

          <div class="small text-muted mt-2" v-if="avatarNameText">{{ avatarNameText }}</div>
          <div class="small text-muted">PNG, JPG, JPEG - tối đa {{ MAX_AVATAR_MB }}MB.</div>
        </div>

        <form @submit.prevent="submit">
          <div class="row g-3">
            <!-- 1) Mã nhân viên -->
            <div class="col-12 col-lg-6">
              <label class="form-label">Mã nhân viên</label>
              <input class="form-control" v-model="form.maNhanVien" disabled />
            </div>

            <!-- 2) Chức vụ -->
            <div class="col-12 col-lg-6">
              <label class="form-label">Chức vụ</label>
              <select class="form-select" v-model="form.quyenHanKey" :disabled="!isAdmin">
                <option value="ADMIN">Admin</option>
                <option value="NHAN_VIEN">Nhân viên</option>
              </select>
            </div>

            <!-- 3) Tên nhân viên -->
            <div class="col-12 col-lg-6">
              <label class="form-label">Tên nhân viên</label>
              <input class="form-control" v-model="form.tenNhanVien" />
            </div>

            <!-- 4) Giới tính -->
            <div class="col-12 col-lg-6">
              <label class="form-label">Giới tính</label>
              <div class="d-flex flex-wrap gap-3 mt-2">
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

            <!-- 5) Số điện thoại -->
            <div class="col-12 col-lg-6">
              <label class="form-label">Số điện thoại</label>
              <input class="form-control" v-model="form.soDienThoai" placeholder="Chỉ nhập số" />
            </div>

            <!-- 6) Email -->
            <div class="col-12 col-lg-6">
              <label class="form-label">Email</label>
              <input class="form-control" v-model="form.email" placeholder="Email" />
            </div>

            <!-- 7) CCCD -->
            <div class="col-12 col-lg-6">
              <label class="form-label">Căn cước công dân</label>
              <input class="form-control" v-model="form.cccd" placeholder="Chỉ nhập số" />
            </div>

            <!-- 8) Ngày sinh (✅ nhập tay + mở lịch đúng vị trí) -->
            <div class="col-12 col-lg-6">
              <label class="form-label">Ngày sinh</label>

              <div class="input-group date-group">
                <input
                    ref="dobTextRef"
                    v-model="dobTextRaw"
                    type="text"
                    class="form-control"
                    placeholder="dd/mm/yyyy"
                    inputmode="numeric"
                    @blur="commitDobText"
                    @keyup.enter="commitDobText"
                />

                <!-- input date thật để show picker (KHÔNG display:none) -->
                <input
                    ref="dobPickerRef"
                    type="date"
                    class="dob-native"
                    :value="form.ngaySinh"
                    @change="onDobPicked"
                />

                <button class="btn btn-outline-secondary" type="button" @click="openDobPicker" title="Chọn ngày">
                  <i class="bi bi-calendar3"></i>
                </button>
                <button class="btn btn-outline-secondary" type="button" @click="clearDob" title="Xóa">
                  <i class="bi bi-x-lg"></i>
                </button>
              </div>
            </div>

            <!-- 9) Tỉnh/Thành phố -->
            <div class="col-12 col-lg-6">
              <label class="form-label">Tỉnh/Thành phố</label>
              <select class="form-select" v-model="addr.provinceCode" @change="onProvinceChange">
                <option value="">-- Chọn Tỉnh/Thành phố --</option>
                <option v-for="p in provinces" :key="p.code" :value="String(p.code)">{{ p.name }}</option>
              </select>
            </div>

            <!-- 10) Quận/Huyện -->
            <div class="col-12 col-lg-6">
              <label class="form-label">Quận/Huyện</label>
              <select class="form-select" v-model="addr.districtCode" @change="onDistrictChange" :disabled="!addr.provinceCode">
                <option value="">-- Chọn Quận/Huyện --</option>
                <option v-for="d in districts" :key="d.code" :value="String(d.code)">{{ d.name }}</option>
              </select>
            </div>

            <!-- 11) Xã/Phường -->
            <div class="col-12 col-lg-6">
              <label class="form-label">Xã/Phường</label>
              <select class="form-select" v-model="addr.wardCode" :disabled="!addr.districtCode">
                <option value="">-- Chọn Xã/Phường --</option>
                <option v-for="w in wards" :key="w.code" :value="String(w.code)">{{ w.name }}</option>
              </select>
            </div>

            <!-- 12) Tên đường -->
            <div class="col-12 col-lg-6">
              <label class="form-label">Tên đường</label>
              <input class="form-control" v-model="addr.detail" placeholder="Số nhà, tên đường..." />
            </div>

            <!-- 13) Tài khoản -->
            <div class="col-12 col-lg-6">
              <label class="form-label">Tài khoản</label>
              <input class="form-control" v-model="form.taiKhoan" :disabled="!isAdmin" />
            </div>

            <!-- 14) Mật khẩu -->
            <div class="col-12 col-lg-6">
              <label class="form-label">
                Mật khẩu
                <span v-if="isEdit" class="small text-muted ms-1">(để trống = giữ nguyên)</span>
              </label>
              <input
                  class="form-control"
                  type="password"
                  v-model="form.matKhau"
                  :disabled="!isAdmin"
                  :placeholder="isAdmin ? '' : 'Chỉ ADMIN được đổi mật khẩu'"
              />
            </div>
          </div>

          <!-- ✅ BỎ alert đỏ dưới form -->

          <div class="d-flex align-items-center justify-content-between gap-2 mt-3">
            <div class="me-auto fst-italic">Vui lòng điền đầy đủ các thông tin.</div>

            <div class="d-flex gap-2">
              <button type="button" class="btn btn-light" @click="goBack">Hủy</button>
              <button type="submit" class="btn btn-primary text-white" :disabled="saving || uploading">
                {{
                  uploading
                      ? 'Đang upload ảnh...'
                      : (saving ? 'Đang lưu...' : (isEdit ? 'Lưu thay đổi' : 'Lưu nhân viên'))
                }}
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>

    <!-- ✅ Confirm popup (KHÔNG dùng window.confirm) -->
    <div v-if="showConfirm" class="modal-overlay" @click.self="closeConfirm">
      <div class="modal-card">
        <h3 class="modal-title">Xác nhận</h3>
        <p class="modal-desc">{{ confirmText }}</p>
        <div class="modal-actions">
          <button class="btn btn-outline" :disabled="confirmLoading" @click="closeConfirm">Hủy</button>
          <button class="btn btn-confirm-primary" :disabled="confirmLoading" @click="confirmYes">
            {{ confirmLoading ? "Đang xử lý..." : "Đồng ý" }}
          </button>
        </div>
      </div>
    </div>

    <!-- ✅ Toast -->
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
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../../services/http'
import { useAuthStore } from '../../stores/auth'
import { useToast } from '@/composables/useToast'

const toast = useToast()
const MAX_AVATAR_MB = 5

const auth = useAuthStore()
const isAdmin = computed(() => !!auth.isAdmin)
const ROLE_TO_ID = { ADMIN: 1, NHAN_VIEN: 2 }

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const saving = ref(false)
const uploading = ref(false)

/** ===== Confirm popup state ===== */
const showConfirm = ref(false)
const confirmText = ref('Bạn chắc chắn chứ?')
const confirmLoading = ref(false)
let pendingAction = null

function openConfirm(text, action) {
  confirmText.value = text || 'Bạn chắc chắn chứ?'
  pendingAction = typeof action === 'function' ? action : null
  showConfirm.value = true
}
function closeConfirm() {
  if (confirmLoading.value) return
  showConfirm.value = false
  pendingAction = null
}
async function confirmYes() {
  if (!pendingAction) {
    showConfirm.value = false
    return
  }
  confirmLoading.value = true
  try {
    await pendingAction()
  } finally {
    confirmLoading.value = false
    showConfirm.value = false
    pendingAction = null
  }
}

/** ===== Form ===== */
const form = reactive({
  id: null,
  maNhanVien: '',
  quyenHanKey: 'NHAN_VIEN',
  quyenHanId: null, // ✅ lưu lại role id nếu BE trả về
  tenNhanVien: '',
  soDienThoai: '',
  cccd: '',
  email: '',
  taiKhoan: '',
  matKhau: '',
  ngaySinh: '', // ✅ ISO yyyy-mm-dd
  gioiTinh: null,
  diaChi: '',
  trangThai: true,
  anhDaiDien: ''
})

/** ===== Ngày sinh (✅ nhập tay dd/mm/yyyy + picker đúng vị trí) ===== */
const dobPickerRef = ref(null)
const dobTextRef = ref(null)
const dobTextRaw = ref('')

function toDMY(iso) {
  const s = String(iso || '').trim() // yyyy-mm-dd
  if (!s) return ''
  const m = s.match(/^(\d{4})-(\d{2})-(\d{2})/)
  if (!m) return ''
  return `${m[3]}/${m[2]}/${m[1]}`
}

function parseDMY(input) {
  const s = String(input || '').trim()
  const m = s.match(/^(\d{1,2})[\/\-\.](\d{1,2})[\/\-\.](\d{4})$/)
  if (!m) return ''
  const dd = parseInt(m[1], 10)
  const mm = parseInt(m[2], 10)
  const yyyy = parseInt(m[3], 10)

  if (yyyy < 1900 || yyyy > 2100) return ''
  if (mm < 1 || mm > 12) return ''
  if (dd < 1 || dd > 31) return ''

  const dt = new Date(yyyy, mm - 1, dd)
  if (dt.getFullYear() !== yyyy || dt.getMonth() !== (mm - 1) || dt.getDate() !== dd) return ''

  return `${yyyy}-${String(mm).padStart(2, '0')}-${String(dd).padStart(2, '0')}`
}

function syncDobUI() {
  dobTextRaw.value = toDMY(form.ngaySinh)
  if (dobPickerRef.value) dobPickerRef.value.value = form.ngaySinh || ''
}

function openDobPicker() {
  const el = dobPickerRef.value
  if (!el) return
  el.value = form.ngaySinh || ''
  if (typeof el.showPicker === 'function') el.showPicker()
  else {
    el.focus()
    el.click()
  }
}

function onDobPicked(e) {
  const v = e?.target?.value || ''
  form.ngaySinh = v
  syncDobUI()
}

function clearDob() {
  form.ngaySinh = ''
  syncDobUI()
}

function commitDobText() {
  const s = String(dobTextRaw.value || '').trim()
  if (!s) {
    clearDob()
    return
  }

  const iso = parseDMY(s)
  if (!iso) {
    toast.warning('Ngày sinh không hợp lệ. Vui lòng nhập theo định dạng dd/mm/yyyy')
    // trả lại giá trị hợp lệ hiện tại
    syncDobUI()
    return
  }

  form.ngaySinh = iso
  syncDobUI()
}

/** ===== Avatar ===== */
const fileInput = ref(null)
const avatarPreview = ref('')
const localBlobUrl = ref('')
const avatarFileMeta = ref('')

const avatarNameText = computed(() => {
  if (avatarFileMeta.value) return avatarFileMeta.value
  const url = String(form.anhDaiDien || '').trim()
  if (!url) return ''
  const clean = url.split('?')[0]
  const parts = clean.split('/')
  return parts[parts.length - 1] || ''
})

function triggerPickFile() {
  fileInput.value?.click()
}
function revokeLocalBlob() {
  if (localBlobUrl.value && String(localBlobUrl.value).startsWith('blob:')) {
    URL.revokeObjectURL(localBlobUrl.value)
  }
  localBlobUrl.value = ''
}
function clearAvatar() {
  revokeLocalBlob()
  avatarPreview.value = ''
  avatarFileMeta.value = ''
  form.anhDaiDien = ''
  if (fileInput.value) fileInput.value.value = ''
  toast.info('Đã xóa ảnh đại diện.')
}
function onAvatarImgError() {
  if (localBlobUrl.value) avatarPreview.value = localBlobUrl.value
  else avatarPreview.value = ''
}

/** ===== Resolve URL /uploads ===== */
const FALLBACK_BACKEND = 'http://localhost:8080'
function getBackendOrigin() {
  let base = ''
  if (http?.defaults?.baseURL) base = String(http.defaults.baseURL).trim()
  if (base.startsWith('http://') || base.startsWith('https://')) {
    try {
      return new URL(base).origin
    } catch {
      return FALLBACK_BACKEND
    }
  }
  return FALLBACK_BACKEND
}
function resolveFileUrl(url) {
  const u = String(url || '').trim()
  if (!u) return ''
  if (u.startsWith('http://') || u.startsWith('https://') || u.startsWith('data:image')) return u
  const origin = getBackendOrigin()
  return u.startsWith('/') ? origin + u : origin + '/' + u
}

async function uploadNhanVienAvatar(file) {
  const fd = new FormData()
  fd.append('file', file)
  const res = await http.post('/api/upload/nhan-vien-avatar', fd, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  const url = res?.data?.url || ''
  if (!url) throw new Error('Upload thành công nhưng không nhận được url')
  return String(url)
}

async function onAvatarFileChange(e) {
  const file = e?.target?.files?.[0]
  if (!file) return

  const okType = (file.type === 'image/png' || file.type === 'image/jpeg' || file.type === 'image/jpg')
  if (!okType) return toast.warning('Chỉ chấp nhận PNG, JPG, JPEG.')

  const maxBytes = MAX_AVATAR_MB * 1024 * 1024
  if (file.size > maxBytes) return toast.warning(`Ảnh tối đa ${MAX_AVATAR_MB}MB.`)

  revokeLocalBlob()
  localBlobUrl.value = URL.createObjectURL(file)
  avatarPreview.value = localBlobUrl.value
  avatarFileMeta.value = `${file.name} • ${(file.size / 1024 / 1024).toFixed(1)} MB`

  uploading.value = true
  try {
    const url = await uploadNhanVienAvatar(file)
    form.anhDaiDien = url
    avatarPreview.value = resolveFileUrl(url) + '?t=' + Date.now()
    toast.success('Upload ảnh thành công!')
  } catch (ex) {
    const msg = ex?.response?.data?.message || ex?.message || 'Upload thất bại'
    toast.error(msg)
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
  const r = await fetch('https://provinces.open-api.vn/api/p/')
  provinces.value = await r.json()
}
async function fetchDistricts(provinceCode) {
  const r = await fetch('https://provinces.open-api.vn/api/p/' + provinceCode + '?depth=2')
  const data = await r.json()
  districts.value = data?.districts || []
}
async function fetchWards(districtCode) {
  const r = await fetch('https://provinces.open-api.vn/api/d/' + districtCode + '?depth=2')
  const data = await r.json()
  wards.value = data?.wards || []
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
  const p = provinces.value.find(x => String(x.code) === String(addr.provinceCode))
  const d = districts.value.find(x => String(x.code) === String(addr.districtCode))
  const w = wards.value.find(x => String(x.code) === String(addr.wardCode))
  const parts = []
  if (addr.detail?.trim()) parts.push(addr.detail.trim())
  if (w?.name) parts.push(w.name)
  if (d?.name) parts.push(d.name)
  if (p?.name) parts.push(p.name)
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
  const parts = String(diaChi || '').split(',').map(x => String(x).trim()).filter(Boolean)
  if (parts.length < 4) {
    addr.detail = parts[0] || ''
    return
  }
  const provinceName = parts[parts.length - 1]
  const districtName = parts[parts.length - 2]
  const wardName = parts[parts.length - 3]
  const detail = parts.slice(0, parts.length - 3).join(', ')

  const p = provinces.value.find(x => normalizeText(x.name) === normalizeText(provinceName))
  if (!p) {
    addr.detail = detail || parts[0] || ''
    return
  }
  addr.provinceCode = String(p.code)
  await fetchDistricts(addr.provinceCode)

  const d = districts.value.find(x => normalizeText(x.name) === normalizeText(districtName))
  if (!d) {
    addr.detail = detail
    return
  }
  addr.districtCode = String(d.code)
  await fetchWards(addr.districtCode)

  const w = wards.value.find(x => normalizeText(x.name) === normalizeText(wardName))
  if (w) addr.wardCode = String(w.code)

  addr.detail = detail
}

/** ===== Helpers/API ===== */
function unwrapList(data) {
  if (!data) return []
  if (Array.isArray(data)) return data
  if (Array.isArray(data.result)) return data.result
  if (Array.isArray(data.content)) return data.content
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

/** ✅ normalize: bổ sung quyenHanId để fix đúng role ADMIN khi edit */
function normalizeStaff(x) {
  x = x || {}
  const qh = x.quyenHan || {}
  const quyenHanId = x.quyenHanId ?? qh.id ?? null
  const tenQuyenHan = x.tenQuyenHan ?? qh.tenQuyenHan ?? ''
  return {
    id: x.id ?? null,
    maNhanVien: x.maNhanVien ?? '',
    tenNhanVien: x.tenNhanVien ?? '',
    soDienThoai: x.soDienThoai ?? '',
    cccd: x.cccd ?? '',
    email: x.email ?? '',
    taiKhoan: x.taiKhoan ?? '',
    ngaySinh: x.ngaySinh ?? null,
    gioiTinh: (x.gioiTinh === true || x.gioiTinh === false) ? x.gioiTinh : null,
    diaChi: x.diaChi ?? '',
    trangThai: (x.trangThai === true || x.trangThai === false) ? x.trangThai : true,
    quyenHanId,
    tenQuyenHan,
    anhDaiDien: x.anhDaiDien ?? x.anh_dai_dien ?? ''
  }
}

function roleKeyFromStaff(s) {
  const id = Number(s?.quyenHanId ?? null)
  if (id === 1) return 'ADMIN'
  if (id === 2) return 'NHAN_VIEN'
  const name = String(s?.tenQuyenHan || '').toUpperCase()
  return name.includes('ADMIN') ? 'ADMIN' : 'NHAN_VIEN'
}

async function apiGetAllStaff() {
  const res = await http.get('/api/nhan-vien')
  return unwrapList(res?.data)
}
async function apiGetStaffById(id) {
  const res = await http.get('/api/nhan-vien/' + id)
  return unwrapObj(res?.data)
}

function generateNextCode(all) {
  let maxN = 0
  for (const it of (all || [])) {
    const s = normalizeStaff(it)
    const m = String(s.maNhanVien || '').match(/^NV(\d+)$/i)
    if (m) {
      const n = parseInt(m[1], 10)
      if (!Number.isNaN(n)) maxN = Math.max(maxN, n)
    }
  }
  return 'NV' + String(maxN + 1).padStart(3, '0')
}

function isDigitsOnly(v) {
  const s = String(v ?? '').trim()
  return s.length > 0 && /^\d+$/.test(s)
}
function isAtLeast18(dateStr) {
  if (!dateStr) return false
  const dob = new Date(dateStr)
  if (Number.isNaN(dob.getTime())) return false
  const today = new Date()
  const limit = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate())
  return dob <= limit
}

async function validateDuplicates(all) {
  const excludeId = isEdit.value ? Number(route.params.id) : null

  const username = safeStr(form.taiKhoan)
  if (username) {
    for (const it of (all || [])) {
      const s = normalizeStaff(it)
      if (s.id !== excludeId && safeStr(s.taiKhoan) === username) return 'Tài khoản đã tồn tại'
    }
  }

  const cccd = String(form.cccd ?? '').trim()
  if (cccd) {
    for (const it of (all || [])) {
      const s = normalizeStaff(it)
      if (s.id !== excludeId && String(s.cccd ?? '').trim() === cccd) return 'CCCD đã tồn tại'
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
  const all = await apiGetAllStaff()

  if (!isEdit.value) {
    form.maNhanVien = generateNextCode(all)
    form.trangThai = true
    form.ngaySinh = ''
    syncDobUI() // ✅ sync UI ngày sinh
    return
  }

  const id = route.params.id
  let detail = null
  try {
    detail = await apiGetStaffById(id)
  } catch {
    detail = null
  }

  if (!detail) {
    detail = all.find(x => String(normalizeStaff(x).id) === String(id)) || null
  }

  const s = normalizeStaff(detail || {})

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

  form.quyenHanId = s.quyenHanId
  form.quyenHanKey = roleKeyFromStaff(s)

  form.anhDaiDien = s.anhDaiDien || ''
  if (form.anhDaiDien) {
    avatarPreview.value = resolveFileUrl(form.anhDaiDien) + '?t=' + Date.now()
  }

  if (s.diaChi) {
    await prefillAddressFromDiaChi(s.diaChi)
  }

  // ✅ QUAN TRỌNG: sync UI ngày sinh sau khi form.ngaySinh đã có dữ liệu
  syncDobUI()
}

/** ===== Submit ===== */
async function submit() {
  const msg = validateForm()
  if (msg) {
    toast.warning(msg) // ✅ giống customer form
    return
  }

  let all = []
  try {
    all = await apiGetAllStaff()
  } catch {
    all = []
  }

  const dupMsg = await validateDuplicates(all)
  if (dupMsg) {
    toast.warning(dupMsg) // ✅ duplicate cũng là cảnh báo vàng
    return
  }

  const actionText = isEdit.value ? 'lưu thay đổi nhân viên' : 'thêm mới nhân viên'
  openConfirm(`Bạn có chắc chắn muốn ${actionText} không?`, async () => {
    saving.value = true
    try {
      const qhId = isAdmin.value ? ROLE_TO_ID[form.quyenHanKey] : (form.quyenHanId ?? ROLE_TO_ID[form.quyenHanKey])

      const payload = {
        quyenHanId: qhId,
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

      if (isEdit.value) {
        await http.put('/api/nhan-vien/' + route.params.id, payload)
        toast.success('Cập nhật nhân viên thành công!')
      } else {
        await http.post('/api/nhan-vien', payload)
        toast.success('Thêm nhân viên thành công!')
      }

      goBack()
    } catch (e) {
      const m = e?.response?.data?.message || e?.message || 'Có lỗi xảy ra'
      toast.error(m) // ✅ lỗi server vẫn đỏ
    } finally {
      saving.value = false
    }
  })
}

function toastClass(type) {
  const t = String(type || 'info').toLowerCase()
  if (t === 'success') return 'text-bg-success'
  if (t === 'error') return 'text-bg-danger'
  if (t === 'warning') return 'text-bg-warning'
  return 'text-bg-info'
}

onMounted(loadData)
onBeforeUnmount(() => revokeLocalBlob())
</script>

<style scoped>
.card {
  border-radius: 14px;
}

/* Avatar */
.avatar-wrap {
  width: 78px;
  height: 78px;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
  position: relative;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
.avatar-fallback { font-weight: 700; }

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

.avatar-input { display: none; }

/* ✅ Date group */
.date-group { position: relative; }

/* input date thật: KHÔNG display:none để picker tính đúng vị trí */
.dob-native{
  position: absolute;
  left: 0;
  top: 0;
  width: 40px;
  height: 40px;
  opacity: 0;
  border: 0;
  padding: 0;
  pointer-events: none;
}

/* ✅ Confirm modal (overlay) */
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
  width: min(420px, calc(100% - 32px));
  background: #fff;
  border-radius: 14px;
  padding: 18px 18px 14px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}
.modal-title {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 700;
}
.modal-desc {
  margin: 0 0 14px;
  color: #555;
  line-height: 1.4;
}
.modal-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.btn-outline {
  height: 38px;
  padding: 0 14px;
  border-radius: 10px;
  border: 1px solid #d0d7de;
  background: #fff;
  cursor: pointer;
  font-weight: 500;
}
.btn-confirm-primary {
  height: 38px;
  padding: 0 14px;
  border-radius: 10px;
  border: 1px solid #1d4ed8;
  background: #1d4ed8;
  color: #fff;
  cursor: pointer;
  font-weight: 500;
}
.btn-confirm-primary:disabled,
.btn-outline:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}
.btn-light{font-weight: 500}
.btn-primary{font-weight: 500}
.form-label{font-weight: 500}
</style>
