<template>
  <div class="container-fluid py-3">
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-person-badge fs-4"></i>
        <h5 class="mb-0">{{ isEdit ? 'Sửa nhân viên' : 'Thêm nhân viên' }}</h5>
      </div>

      <div class="d-flex align-items-center gap-2 flex-wrap justify-content-end">
  <button type="button" class="btn btn-outline-primary btn-sm" @click="openScanModal">
    <i class="bi bi-qr-code-scan me-1"></i> Quét QR CCCD
  </button>

  <button
    v-if="canChangeOwnPassword"
    type="button"
    class="btn btn-outline-warning btn-sm"
    @click="openChangePasswordModal"
  >
    <i class="bi bi-shield-lock me-1"></i> Đổi mật khẩu
  </button>

  <button type="button" class="btn btn-outline-secondary btn-sm" @click="goBack">
    <i class="bi bi-arrow-left me-1"></i> Quay lại danh sách
  </button>
</div>
    </div>

    <div class="card shadow-sm staff-form-card">
      <div class="card-body">
        <div class="avatar-top">
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
          <div class="small text-muted">PNG, JPG, JPEG - tối đa {{ MAX_AVATAR_MB }}MB.</div>
        </div>

        <form @submit.prevent="submit" novalidate>
          <div class="row g-3">
            <div class="col-12 col-lg-6">
              <label class="form-label">Mã nhân viên</label>
              <input class="form-control" v-model="form.maNhanVien" disabled />
            </div>

            <div class="col-12 col-lg-6">
              <label class="form-label">Chức vụ</label>
              <select class="form-select" v-model="form.quyenHanKey" :disabled="!isAdmin">
                <option value="ADMIN">Admin</option>
                <option value="NHAN_VIEN">Nhân viên</option>
              </select>
            </div>

            <div class="col-12 col-lg-6">
              <label class="form-label">Tên nhân viên</label>
              <input class="form-control" v-model="form.tenNhanVien" />
            </div>

            <div class="col-12 col-lg-6">
              <label class="form-label">Giới tính</label>
              <div class="gender-box">
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

            <div class="col-12 col-lg-6">
              <label class="form-label">Số điện thoại</label>
              <input
                  class="form-control"
                  v-model="form.soDienThoai"
                  placeholder="VD: 0912345678"
                  inputmode="numeric"
                  maxlength="10"
                  @input="form.soDienThoai = normalizePhoneInput(form.soDienThoai)"
              />
            </div>

            <div class="col-12 col-lg-6">
              <label class="form-label">Email</label>
              <input
                  class="form-control"
                  type="text"
                  v-model="form.email"
                  placeholder="VD: abc@gmail.com"
              />
            </div>

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

            <div class="col-12 col-lg-6">
              <label class="form-label">Tỉnh/Thành phố</label>
              <select class="form-select" v-model="addr.provinceCode" @change="onProvinceChange">
                <option value="">-- Chọn Tỉnh/Thành phố --</option>
                <option v-for="p in provinces" :key="p.code" :value="String(p.code)">
                  {{ p.name }}
                </option>
              </select>
            </div>

            <div class="col-12 col-lg-6">
              <label class="form-label">Phường/Xã/Đặc khu</label>
              <select class="form-select" v-model="addr.wardCode" :disabled="!addr.provinceCode">
                <option value="">-- Chọn Phường/Xã/Đặc khu --</option>
                <option v-for="w in wards" :key="w.code" :value="String(w.code)">
                  {{ w.name }}
                </option>
              </select>
            </div>

            <div class="col-12 col-lg-6">
              <label class="form-label">Tên đường</label>
              <input class="form-control" v-model="addr.detail" placeholder="Số nhà, tên đường..." />
            </div>

            <div class="col-12 col-lg-6">
              <label class="form-label">Tài khoản</label>
              <input
                  class="form-control"
                  v-model="form.taiKhoan"
                  disabled
                  :placeholder="isEdit ? '' : 'Tự động tạo và gửi qua email'"
              />
            </div>

            <div class="col-12 col-lg-6">
              <label class="form-label">Mật khẩu</label>
              <input
                  class="form-control"
                  type="password"
                  v-model="form.matKhau"
                  disabled
                  :placeholder="isEdit ? '******' : 'Tự động tạo và gửi qua email'"
              />
            </div>
          </div>

          <div class="form-footer">
            <div class="footer-note">Vui lòng điền đầy đủ các thông tin.</div>

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

    <div v-if="showScanModal" class="modal-overlay" @click.self="closeScanModal">
      <div class="modal-card scan-modal">
        <div class="d-flex align-items-start justify-content-between gap-3 mb-2">
          <div>
            <h3 class="modal-title mb-1">Quét QR CCCD</h3>
            <div class="modal-desc mb-0">
              Quét mã QR trên CCCD để tự động điền họ tên, ngày sinh, giới tính và địa chỉ.
            </div>
          </div>
          <button type="button" class="btn-close" @click="closeScanModal"></button>
        </div>

        <div class="d-flex gap-2 flex-wrap mb-3">
          <button
              type="button"
              class="btn btn-sm"
              :class="scanTab === 'camera' ? 'btn-primary text-white' : 'btn-outline-secondary'"
              @click="switchScanTab('camera')"
          >
            Camera
          </button>
          <button
              type="button"
              class="btn btn-sm"
              :class="scanTab === 'manual' ? 'btn-primary text-white' : 'btn-outline-secondary'"
              @click="switchScanTab('manual')"
          >
            Dán dữ liệu QR
          </button>
        </div>

        <div v-show="scanTab === 'camera'">
          <div id="cccd-qr-reader" class="qr-reader-box"></div>
          <div class="small text-muted mt-2">
            Đưa mã QR trên CCCD vào giữa khung hình để quét.
          </div>
        </div>

        <div v-show="scanTab === 'manual'">
          <label class="form-label">Chuỗi QR CCCD</label>
          <textarea
              class="form-control"
              rows="5"
              v-model="manualQrText"
              placeholder="Dán chuỗi QR CCCD vào đây..."
          ></textarea>
          <div class="small text-muted mt-2">
            Dùng khi bạn đã có sẵn nội dung QR và muốn điền nhanh thông tin.
          </div>
        </div>

        <div class="mt-3 d-flex justify-content-end gap-2">
          <button type="button" class="btn btn-light" @click="closeScanModal">Đóng</button>
          <button
              v-if="scanTab === 'manual'"
              type="button"
              class="btn btn-primary text-white"
              @click="applyManualQr"
          >
            Áp dụng dữ liệu QR
          </button>
        </div>
      </div>
    </div>
    <div v-if="showChangePasswordModal" class="modal-overlay" @click.self="closeChangePasswordModal">
  <div class="modal-card change-password-modal">
    <div class="d-flex align-items-start justify-content-between gap-3 mb-3">
      <div>
        <h3 class="modal-title mb-1">Đổi mật khẩu</h3>
        <div class="modal-desc mb-0">
          Mã OTP sẽ được gửi về email của tài khoản đang đăng nhập.
        </div>
      </div>
      <button
        type="button"
        class="btn-close"
        @click="closeChangePasswordModal"
        :disabled="passwordSubmitting || otpSending"
      ></button>
    </div>

    <div class="email-box mb-3">
      <div class="email-box-icon">
        <i class="bi bi-envelope-check"></i>
      </div>
      <div>
        <div class="email-box-label">Email nhận OTP</div>
        <div class="email-box-value">{{ form.email || '-' }}</div>
      </div>
    </div>

    <div class="mb-3">
      <label class="form-label">Mật khẩu hiện tại</label>
      <div class="input-group">
        <input
          :type="showOldPassword ? 'text' : 'password'"
          class="form-control"
          v-model.trim="passwordForm.oldPassword"
          placeholder="Nhập mật khẩu hiện tại"
          autocomplete="current-password"
        />
        <button class="btn btn-outline-secondary" type="button" @click="showOldPassword = !showOldPassword">
          <i :class="showOldPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
        </button>
      </div>
    </div>

    <div class="mb-3">
      <label class="form-label">Mật khẩu mới</label>
      <div class="input-group">
        <input
          :type="showNewPassword ? 'text' : 'password'"
          class="form-control"
          v-model.trim="passwordForm.newPassword"
          placeholder="Nhập mật khẩu mới"
          autocomplete="new-password"
        />
        <button class="btn btn-outline-secondary" type="button" @click="showNewPassword = !showNewPassword">
          <i :class="showNewPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
        </button>
      </div>
    </div>

    <div class="mb-3">
      <label class="form-label">Xác nhận mật khẩu mới</label>
      <div class="input-group">
        <input
          :type="showConfirmPassword ? 'text' : 'password'"
          class="form-control"
          v-model.trim="passwordForm.confirmPassword"
          placeholder="Nhập lại mật khẩu mới"
          autocomplete="new-password"
        />
        <button class="btn btn-outline-secondary" type="button" @click="showConfirmPassword = !showConfirmPassword">
          <i :class="showConfirmPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
        </button>
      </div>
    </div>

    <div class="mb-3">
      <label class="form-label">Mã OTP</label>
      <div class="input-group">
        <input
          type="text"
          class="form-control otp-input"
          v-model.trim="passwordForm.otp"
          maxlength="6"
          placeholder="Nhập mã OTP"
          autocomplete="one-time-code"
          @input="passwordForm.otp = onlyNumber(passwordForm.otp)"
        />
        <button
          class="btn btn-outline-primary"
          type="button"
          @click="sendChangePasswordOtp"
          :disabled="otpSending || otpCooldown > 0"
        >
          <span v-if="otpSending" class="spinner-border spinner-border-sm me-1"></span>
          {{ otpButtonText }}
        </button>
      </div>
      <div class="form-text">
        Bấm gửi OTP trước, sau đó kiểm tra email để lấy mã xác nhận.
      </div>
    </div>

    <div class="modal-actions mt-3">
      <button
        class="btn btn-outline"
        :disabled="passwordSubmitting || otpSending"
        @click="closeChangePasswordModal"
      >
        Hủy
      </button>

      <button
        class="btn btn-confirm-primary"
        :disabled="passwordSubmitting"
        @click="changePassword"
      >
        {{ passwordSubmitting ? 'Đang xử lý...' : 'Xác nhận đổi mật khẩu' }}
      </button>
    </div>
  </div>
</div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Html5Qrcode } from 'html5-qrcode'
import http from '../../services/http'
import { useAuthStore } from '../../stores/auth'
import { useToast } from '@/composables/useToast'
import { normalizeUploadResponse, resolveMediaUrl } from '@/utils/media'

const toast = useToast()
const MAX_AVATAR_MB = 5

const auth = useAuthStore()
const isAdmin = computed(() => !!auth.isAdmin)
const ROLE_TO_ID = { ADMIN: 1, NHAN_VIEN: 2 }
const showChangePasswordModal = ref(false)
const otpSending = ref(false)
const passwordSubmitting = ref(false)
const otpCooldown = ref(0)
const otpTimer = ref(null)

const showOldPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
  otp: ''
})

const otpButtonText = computed(() => {
  if (otpCooldown.value > 0) return `Gửi lại sau ${otpCooldown.value}s`
  return 'Gửi OTP'
})

const currentLoginUsername = computed(() => {
  const candidates = [
    auth?.user?.taiKhoan,
    auth?.user?.username,
    auth?.user?.sub,
    auth?.profile?.taiKhoan,
    auth?.profile?.username,
    auth?.account?.taiKhoan,
    auth?.taiKhoan,
    auth?.username,
    getJwtSubjectFromLocalStorage()
  ]

  return String(candidates.find(Boolean) || '').trim()
})

const canChangeOwnPassword = computed(() => {
  if (!isEdit.value) return false
  const current = String(currentLoginUsername.value || '').trim().toLowerCase()
  const target = String(form.taiKhoan || '').trim().toLowerCase()
  return !!current && !!target && current === target
})
const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const saving = ref(false)
const uploading = ref(false)

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

const form = reactive({
  id: null,
  maNhanVien: '',
  quyenHanKey: 'NHAN_VIEN',
  quyenHanId: null,
  tenNhanVien: '',
  soDienThoai: '',
  email: '',
  taiKhoan: '',
  matKhau: '',
  ngaySinh: '',
  gioiTinh: null,
  diaChi: '',
  trangThai: true,
  anhDaiDien: '',
  mediaAvatarId: null
})

const showScanModal = ref(false)
const scanTab = ref('camera')
const manualQrText = ref('')
let qrScanner = null
let qrScannerStarted = false

function openScanModal() {
  showScanModal.value = true
  scanTab.value = 'camera'
  manualQrText.value = ''
  nextTick(() => startQrScanner())
}

async function closeScanModal() {
  showScanModal.value = false
  manualQrText.value = ''
  await stopQrScanner()
}

async function switchScanTab(tab) {
  scanTab.value = tab
  if (tab === 'camera') {
    await nextTick()
    await startQrScanner()
  } else {
    await stopQrScanner()
  }
}

async function startQrScanner() {
  if (!showScanModal.value || scanTab.value !== 'camera') return
  if (qrScannerStarted) return

  try {
    if (!qrScanner) {
      qrScanner = new Html5Qrcode('cccd-qr-reader')
    }

    await qrScanner.start(
        { facingMode: 'environment' },
        { fps: 10, qrbox: 220, aspectRatio: 1.33 },
        async (decodedText) => {
          if (!decodedText) return
          const parsed = parseCccdQr(decodedText)
          if (!parsed) return

          await stopQrScanner()
          applyParsedCccd(parsed)
          showScanModal.value = false
          toast.success('Quét CCCD thành công, đã tự động điền thông tin.')
        },
        () => {}
    )

    qrScannerStarted = true
  } catch (e) {
    qrScannerStarted = false
    toast.warning('Không mở được camera để quét QR. Bạn có thể chuyển sang tab "Dán dữ liệu QR".')
  }
}

async function stopQrScanner() {
  if (!qrScanner || !qrScannerStarted) return
  try {
    await qrScanner.stop()
    await qrScanner.clear()
  } catch {
  } finally {
    qrScannerStarted = false
  }
}

function applyManualQr() {
  const parsed = parseCccdQr(manualQrText.value)
  if (!parsed) {
    toast.warning('Chuỗi QR CCCD không hợp lệ hoặc không đọc được dữ liệu.')
    return
  }

  applyParsedCccd(parsed)
  closeScanModal()
  toast.success('Đã áp dụng dữ liệu từ QR CCCD.')
}

function parseCccdQr(raw) {
  const text = String(raw || '').trim()
  if (!text) return null

  const parts = text.split('|').map(s => String(s || '').trim())
  if (parts.length < 6) return null

  const fullName = parts[2] || ''
  const dob = normalizeDobFromQr(parts[3])
  const gender = normalizeGender(parts[4])
  const address = parts[5] || ''

  if (!fullName && !dob && gender === null && !address) return null

  return {
    rawText: text,
    fullName,
    dateOfBirth: dob,
    gender,
    address
  }
}

function normalizeDobFromQr(v) {
  const s = String(v || '').trim()

  if (/^\d{8}$/.test(s)) {
    const dd = s.slice(0, 2)
    const mm = s.slice(2, 4)
    const yyyy = s.slice(4, 8)
    return validateDateParts(dd, mm, yyyy) ? `${yyyy}-${mm}-${dd}` : ''
  }

  const m = s.match(/^(\d{1,2})[\/\-\.](\d{1,2})[\/\-\.](\d{4})$/)
  if (m) {
    const dd = String(m[1]).padStart(2, '0')
    const mm = String(m[2]).padStart(2, '0')
    const yyyy = m[3]
    return validateDateParts(dd, mm, yyyy) ? `${yyyy}-${mm}-${dd}` : ''
  }

  if (/^\d{4}-\d{2}-\d{2}$/.test(s)) return s
  return ''
}

function validateDateParts(dd, mm, yyyy) {
  const d = Number(dd)
  const m = Number(mm)
  const y = Number(yyyy)
  if (!d || !m || !y) return false
  const dt = new Date(y, m - 1, d)
  return dt.getFullYear() === y && dt.getMonth() === (m - 1) && dt.getDate() === d
}

function normalizeGender(v) {
  const s = String(v || '').trim().toLowerCase()
  if (!s) return null
  if (s === 'nam' || s === 'male' || s === 'm') return true
  if (s === 'nữ' || s === 'nu' || s === 'female' || s === 'f') return false
  return null
}

async function applyParsedCccd(parsed) {
  if (parsed.fullName) form.tenNhanVien = parsed.fullName
  if (parsed.dateOfBirth) {
    form.ngaySinh = parsed.dateOfBirth
    syncDobUI()
  }
  if (parsed.gender === true || parsed.gender === false) {
    form.gioiTinh = parsed.gender
  }
  if (parsed.address) {
    form.diaChi = parsed.address
    await prefillAddressFromDiaChi(parsed.address)
  }
}

const dobPickerRef = ref(null)
const dobTextRef = ref(null)
const dobTextRaw = ref('')

function toDMY(iso) {
  const s = String(iso || '').trim()
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
    syncDobUI()
    return
  }

  form.ngaySinh = iso
  syncDobUI()
}

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

async function uploadNhanVienAvatar(file) {
  const fd = new FormData()
  fd.append('file', file)
  const res = await http.post('/api/upload/nhan-vien-avatar', fd, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  const uploaded = normalizeUploadResponse(res)
  if (!uploaded.url) throw new Error('Upload thành công nhưng không nhận được url')
  return uploaded
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
    const uploaded = await uploadNhanVienAvatar(file)
    form.anhDaiDien = uploaded.url
    form.mediaAvatarId = uploaded.mediaAssetId
    avatarPreview.value = resolveMediaUrl(uploaded.url) + '?t=' + Date.now()
    toast.success('Upload ảnh thành công!')
  } catch (ex) {
    const msg = ex?.response?.data?.message || ex?.message || 'Upload thất bại'
    toast.error(msg)
  } finally {
    uploading.value = false
  }
}

const provinces = ref([])
const wards = ref([])
const addr = reactive({
  provinceCode: '',
  wardCode: '',
  detail: ''
})

function normalizeProvinceList(data) {
  return (Array.isArray(data) ? data : [])
      .map((item) => ({
        code: item?.code,
        name: item?.name,
      }))
      .filter((item) => item.code != null && item.name)
}

function normalizeWardList(data) {
  return (Array.isArray(data) ? data : [])
      .map((item) => ({
        code: item?.code,
        name: item?.name,
        provinceCode: item?.province_code ?? item?.provinceCode ?? null,
      }))
      .filter((item) => item.code != null && item.name)
}

async function fetchProvinces() {
  const r = await fetch('https://provinces.open-api.vn/api/v2/p/')
  if (!r.ok) throw new Error('Không tải được tỉnh/thành')
  const data = await r.json()
  provinces.value = normalizeProvinceList(data)
}

async function fetchWardsByProvince(provinceCode) {
  if (!provinceCode) {
    wards.value = []
    return
  }

  const r = await fetch(`https://provinces.open-api.vn/api/v2/w/?province=${provinceCode}`)
  if (!r.ok) throw new Error('Không tải được phường/xã')
  const data = await r.json()
  wards.value = normalizeWardList(data)
}

async function onProvinceChange() {
  addr.wardCode = ''
  wards.value = []

  if (addr.provinceCode) {
    await fetchWardsByProvince(addr.provinceCode)
  }
}

function buildDiaChi() {
  const p = provinces.value.find(x => String(x.code) === String(addr.provinceCode))
  const w = wards.value.find(x => String(x.code) === String(addr.wardCode))

  const parts = []
  if (addr.detail?.trim()) parts.push(addr.detail.trim())
  if (w?.name) parts.push(w.name)
  if (p?.name) parts.push(p.name)

  return parts.join(', ')
}

function normalizeText(s) {
  return String(s || '')
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/đ/g, 'd')
      .replace(/\b(thanh pho|tp|tinh|quan|huyen|thi xa|thi tran|xa|phuong|dac khu)\b/g, '')
      .replace(/\s+/g, ' ')
      .trim()
}

async function prefillAddressFromDiaChi(diaChi) {
  const raw = String(diaChi || '').trim()
  const parts = raw
      .split(',')
      .map(x => String(x).trim())
      .filter(Boolean)

  addr.provinceCode = ''
  addr.wardCode = ''
  addr.detail = ''
  wards.value = []

  if (parts.length < 2) {
    addr.detail = raw
    return
  }

  const provinceName = parts[parts.length - 1]
  const normProvince = normalizeText(provinceName)

  const p = provinces.value.find(x => {
    const n = normalizeText(x.name)
    return n === normProvince || n.includes(normProvince) || normProvince.includes(n)
  })

  if (!p) {
    addr.detail = raw
    return
  }

  addr.provinceCode = String(p.code)
  await fetchWardsByProvince(addr.provinceCode)

  const wardCandidates = []
  if (parts.length >= 2) wardCandidates.push(parts[parts.length - 2]) // kiểu mới
  if (parts.length >= 3) wardCandidates.push(parts[parts.length - 3]) // kiểu cũ có quận/huyện

  let pickedWard = null
  for (const candidate of wardCandidates) {
    const normCandidate = normalizeText(candidate)
    const found = wards.value.find(x => {
      const n = normalizeText(x.name)
      return n === normCandidate || n.includes(normCandidate) || normCandidate.includes(n)
    })
    if (found) {
      pickedWard = found
      break
    }
  }

  if (pickedWard) {
    addr.wardCode = String(pickedWard.code)
  }

  const detailCut = pickedWard ? (parts.length >= 3 ? 2 : 1) : 1
  const detailParts = parts.slice(0, Math.max(parts.length - detailCut - 1, 0))
  addr.detail = detailParts.join(', ') || parts[0] || ''
}

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
    email: x.email ?? '',
    taiKhoan: x.taiKhoan ?? '',
    ngaySinh: x.ngaySinh ?? null,
    gioiTinh: (x.gioiTinh === true || x.gioiTinh === false) ? x.gioiTinh : null,
    diaChi: x.diaChi ?? '',
    trangThai: (x.trangThai === true || x.trangThai === false) ? x.trangThai : true,
    quyenHanId,
    tenQuyenHan,
    anhDaiDien: x.anhDaiDien ?? x.anh_dai_dien ?? '',
    mediaAvatarId: x.mediaAvatarId ?? x.idMediaAvatar ?? x.id_media_avatar ?? null
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

function normalizeSpaces(v) {
  return String(v == null ? '' : v).trim().replace(/\s+/g, ' ')
}

function normalizePhoneInput(v) {
  return String(v == null ? '' : v).replace(/\D/g, '').slice(0, 10)
}

function isValidPhone(v) {
  return /^0\d{9}$/.test(String(v || '').trim())
}

function isValidEmail(v) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(v || '').trim())
}

function hasDigitInName(v) {
  return /\d/.test(normalizeSpaces(v))
}

function hasSpecialCharInName(v) {
  const s = normalizeSpaces(v)
  return /[^\p{L}\s]/u.test(s)
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
  const email = safeStr(form.email)
  if (email) {
    for (const it of (all || [])) {
      const s = normalizeStaff(it)
      if (s.id !== excludeId && safeStr(s.email) === email) return 'Email đã tồn tại'
    }
  }
  return ''
}

function validateForm() {
  if (!String(form.maNhanVien || '').trim()) {
    return 'Mã nhân viên không được trống'
  }

  const tenNhanVien = normalizeSpaces(form.tenNhanVien)
  const soDienThoai = String(form.soDienThoai || '').trim()
  const email = String(form.email || '').trim()

  if (!tenNhanVien) {
    return 'Tên nhân viên không được trống'
  }

  if (tenNhanVien.length < 2) {
    return 'Vui lòng nhập đúng tên nhân viên'
  }

  if (hasDigitInName(tenNhanVien)) {
    return 'Tên nhân viên không được chứa số'
  }

  if (hasSpecialCharInName(tenNhanVien)) {
    return 'Tên nhân viên không được chứa ký tự đặc biệt'
  }

  if (!soDienThoai) {
    return 'Số điện thoại không được để trống'
  }

  if (!/^\d+$/.test(soDienThoai)) {
    return 'Số điện thoại không được chứa chữ'
  }

  if (!isValidPhone(soDienThoai)) {
    return 'Số điện thoại phải gồm 10 số và bắt đầu bằng số 0'
  }

  if (!email) {
    return 'Email không được trống'
  }

  if (!isValidEmail(email)) {
    return 'Email không hợp lệ'
  }

  if (!form.ngaySinh || !isAtLeast18(form.ngaySinh)) return 'Nhân viên phải đủ 18 tuổi'

  if (!addr.provinceCode || !addr.wardCode || !String(addr.detail || '').trim()) {
    return 'Vui lòng chọn đầy đủ Tỉnh/Thành, Phường/Xã/Đặc khu và nhập tên đường'
  }

  return ''
}
function getJwtSubjectFromLocalStorage() {
  try {
    const jwtRegex = /eyJ[\w-]+\.[\w-]+\.[\w-]+/g

    for (const key of Object.keys(localStorage)) {
      const value = localStorage.getItem(key)
      if (!value) continue

      const matches = String(value).match(jwtRegex)
      if (!matches || !matches.length) continue

      for (const token of matches) {
        const parts = token.split('.')
        if (parts.length !== 3) continue

        const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
        const padded = base64.padEnd(base64.length + (4 - base64.length % 4) % 4, '=')
        const payload = JSON.parse(atob(padded))

        if (payload?.sub) return payload.sub
      }
    }
  } catch {
  }

  return ''
}

function onlyNumber(value) {
  return String(value || '').replace(/\D/g, '').slice(0, 6)
}

function resetPasswordForm() {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordForm.otp = ''
  showOldPassword.value = false
  showNewPassword.value = false
  showConfirmPassword.value = false
}

function openChangePasswordModal() {
  resetPasswordForm()
  showChangePasswordModal.value = true
}

function closeChangePasswordModal() {
  if (passwordSubmitting.value || otpSending.value) return
  showChangePasswordModal.value = false
  resetPasswordForm()
}

function startOtpCooldown() {
  otpCooldown.value = 60

  if (otpTimer.value) clearInterval(otpTimer.value)

  otpTimer.value = setInterval(() => {
    otpCooldown.value -= 1

    if (otpCooldown.value <= 0) {
      clearInterval(otpTimer.value)
      otpTimer.value = null
      otpCooldown.value = 0
    }
  }, 1000)
}

function validatePasswordBeforeSendOtp() {
  if (!canChangeOwnPassword.value) {
    toast.warning('Bạn chỉ có thể đổi mật khẩu của tài khoản đang đăng nhập')
    return false
  }

  if (!passwordForm.oldPassword) {
    toast.warning('Vui lòng nhập mật khẩu hiện tại')
    return false
  }

  if (!passwordForm.newPassword) {
    toast.warning('Vui lòng nhập mật khẩu mới')
    return false
  }

  if (passwordForm.newPassword.length < 6) {
    toast.warning('Mật khẩu mới phải có ít nhất 6 ký tự')
    return false
  }

  if (passwordForm.newPassword === passwordForm.oldPassword) {
    toast.warning('Mật khẩu mới không được trùng mật khẩu hiện tại')
    return false
  }

  if (!passwordForm.confirmPassword) {
    toast.warning('Vui lòng xác nhận mật khẩu mới')
    return false
  }

  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    toast.warning('Mật khẩu xác nhận không khớp')
    return false
  }

  return true
}

function validateChangePassword() {
  if (!validatePasswordBeforeSendOtp()) return false

  if (!passwordForm.otp) {
    toast.warning('Vui lòng nhập mã OTP')
    return false
  }

  if (passwordForm.otp.length !== 6) {
    toast.warning('Mã OTP phải gồm 6 chữ số')
    return false
  }

  return true
}

async function sendChangePasswordOtp() {
  if (!validatePasswordBeforeSendOtp()) return

  otpSending.value = true

  try {
    await http.post('/api/nhan-vien/doi-mat-khau/gui-otp')
    toast.success('Mã OTP đã được gửi về email của bạn')
    startOtpCooldown()
  } catch (e) {
    const m = e?.response?.data?.message || e?.message || 'Không gửi được mã OTP'
    toast.error(m)
  } finally {
    otpSending.value = false
  }
}

async function changePassword() {
  if (!validateChangePassword()) return

  passwordSubmitting.value = true

  try {
    await http.post('/api/nhan-vien/doi-mat-khau/xac-nhan', {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword,
      otp: passwordForm.otp
    })

    toast.success('Đổi mật khẩu thành công')
    closeChangePasswordModal()
  } catch (e) {
    const m = e?.response?.data?.message || e?.message || 'Đổi mật khẩu thất bại'
    toast.error(m)
  } finally {
    passwordSubmitting.value = false
  }
}
function goBack() {
  router.push({ name: 'staff' })
}

async function loadData() {
  await fetchProvinces()
  const all = await apiGetAllStaff()

  if (!isEdit.value) {
    form.maNhanVien = generateNextCode(all)
    form.trangThai = true
    form.ngaySinh = ''
    syncDobUI()
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
    avatarPreview.value = resolveMediaUrl(form.anhDaiDien) + '?t=' + Date.now()
  }

  if (s.diaChi) {
    await prefillAddressFromDiaChi(s.diaChi)
  }

  syncDobUI()
}

async function submit() {
  const msg = validateForm()
  if (msg) {
    toast.warning(msg)
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
    toast.warning(dupMsg)
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
        email: String(form.email || '').trim(),
        ngaySinh: form.ngaySinh,
        gioiTinh: (form.gioiTinh === true || form.gioiTinh === false) ? form.gioiTinh : null,
        diaChi: buildDiaChi(),
        trangThai: isEdit.value ? form.trangThai : true,
        anhDaiDien: form.anhDaiDien,
        mediaAvatarId: form.mediaAvatarId
      }

      if (isEdit.value) {
        await http.put('/api/nhan-vien/' + route.params.id, payload)
        toast.success('Cập nhật nhân viên thành công!')
      } else {
        await http.post('/api/nhan-vien', payload)
        toast.success('Thêm nhân viên thành công! Thông tin tài khoản đã được gửi qua email.')
      }

      goBack()
    } catch (e) {
      const m = e?.response?.data?.message || e?.message || 'Có lỗi xảy ra'
      toast.error(m)
    } finally {
      saving.value = false
    }
  })
}

onMounted(loadData)

onBeforeUnmount(async () => {
  revokeLocalBlob()
  await stopQrScanner()

  if (otpTimer.value) {
    clearInterval(otpTimer.value)
    otpTimer.value = null
  }
})
</script>

<style scoped>
.staff-form-card {
  border: 0;
  border-radius: 16px;
  overflow: hidden;
}

.card-body {
  padding: 24px;
}

.avatar-top {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 28px;
}

.avatar-wrap {
  width: 88px;
  height: 88px;
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

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-fallback {
  font-weight: 700;
  font-size: 24px;
  color: #111827;
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

.gender-box {
  min-height: 42px;
  border: 1px solid #dee2e6;
  border-radius: 10px;
  padding: 9px 12px;
  display: flex;
  align-items: center;
  gap: 20px;
  background: #fff;
}

.form-label {
  font-weight: 600;
  margin-bottom: 6px;
  color: #374151;
}

.form-control,
.form-select {
  min-height: 42px;
  border-radius: 10px;
}

.date-group {
  position: relative;
}

.dob-native {
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

.form-footer {
  margin-top: 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.footer-note {
  font-style: italic;
  color: #6b7280;
}

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

.scan-modal {
  width: min(720px, calc(100% - 32px));
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

.qr-reader-box {
  width: 100%;
  min-height: 280px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  background: #000;
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

.btn-light,
.btn-primary {
  font-weight: 500;
}

@media (max-width: 991.98px) {
  .card-body {
    padding: 18px;
  }

  .form-footer {
    flex-direction: column;
    align-items: stretch;
  }

  .form-footer .d-flex {
    width: 100%;
    justify-content: flex-end;
  }
}
.change-password-modal {
  width: min(520px, calc(100% - 32px));
}

.email-box {
  display: flex;
  gap: 12px;
  align-items: center;
  border: 1px solid #dbeafe;
  background: #eff6ff;
  border-radius: 14px;
  padding: 12px 14px;
}

.email-box-icon {
  width: 38px;
  height: 38px;
  border-radius: 999px;
  background: #dbeafe;
  color: #1d4ed8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.email-box-label {
  font-size: 12px;
  color: #64748b;
  font-weight: 600;
}

.email-box-value {
  color: #0f172a;
  font-weight: 700;
  word-break: break-all;
}

.otp-input {
  letter-spacing: 4px;
  font-weight: 700;
}
</style>