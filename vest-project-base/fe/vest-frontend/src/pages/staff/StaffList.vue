<template>
  <div class="page">
    <div class="header-section">
      <h2>Quản lý nhân viên / Danh sách nhân viên</h2>
    </div>

    <!-- Filter -->
    <div class="card filter-card">
      <div class="card-header">
        <h3><span class="icon">▼</span> Bộ lọc tìm kiếm</h3>
      </div>

      <div class="filter-body">
        <div class="filter-left">
          <div class="form-group full-width">
            <label>Tìm kiếm</label>
            <input
                type="text"
                v-model="filters.keyword"
                placeholder="Tìm theo mã, tên, SĐT, email, CCCD, tài khoản..."
                class="form-input"
                @keyup.enter="reload"
            />
          </div>

          <div class="form-group">
            <label>Trạng thái hiển thị</label>
            <div class="radio-row">
              <label class="radio-item">
                <input type="radio" value="active" v-model="filters.status" />
                Hiển thị
              </label>
              <label class="radio-item">
                <input type="radio" value="inactive" v-model="filters.status" />
                Đã ẩn
              </label>
              <label class="radio-item">
                <input type="radio" value="all" v-model="filters.status" />
                Tất cả
              </label>
            </div>
          </div>
        </div>

        <div class="filter-right">
          <div class="filter-grid">
            <div class="form-group">
              <label>Quyền hạn</label>
              <select class="form-input" v-model="filters.role">
                <option value="">Tất cả</option>
                <option value="ADMIN">ADMIN</option>
                <option value="NHAN_VIEN">NHÂN VIÊN</option>
              </select>
            </div>

            <div class="form-group">
              <label>CCCD</label>
              <input class="form-input" v-model="filters.cccd" placeholder="CCCD" @keyup.enter="reload" />
            </div>

            <div class="form-group">
              <label>Email</label>
              <input class="form-input" v-model="filters.email" placeholder="Email" @keyup.enter="reload" />
            </div>

            <div class="form-group">
              <label>SĐT</label>
              <input class="form-input" v-model="filters.phone" placeholder="SĐT" @keyup.enter="reload" />
            </div>

            <div class="form-group">
              <label>Tài khoản</label>
              <input class="form-input" v-model="filters.username" placeholder="Tài khoản" @keyup.enter="reload" />
            </div>
          </div>
        </div>

        <div class="action-buttons-group">
          <button class="btn btn-outline btn-w-icon" @click="exportExcel">
            <span class="btn-i">📥</span> Tải Excel
          </button>

          <button class="btn btn-outline btn-w-icon" @click="openCreate">
            <span class="btn-i">➕</span> Thêm mới
          </button>

          <button class="btn btn-outline btn-w-icon" @click="resetFilters">
            <span class="btn-i">🔄</span> Đặt lại
          </button>
        </div>
      </div>
    </div>

    <div style="height: 20px;"></div>

    <!-- Table -->
    <div class="card table-card">
      <div class="table-header-info">
        <h3>Tổng số nhân viên {{ totalElements }}</h3>
      </div>

      <div class="table-responsive">
        <table class="table">
          <thead>
          <tr>
            <th>STT</th>
            <th>Mã NV</th>
            <th>Tên NV</th>
            <th>Quyền hạn</th>
            <th>CCCD</th>
            <th>SĐT</th>
            <th>Email</th>
            <th class="text-center">Hành động</th>
          </tr>
          </thead>

          <tbody>
          <tr v-for="(s, index) in items" :key="s.id">
            <td>{{ (currentPage * pageSize) + index + 1 }}</td>
            <td>{{ s.maNhanVien }}</td>
            <td class="text-bold">{{ s.tenNhanVien }}</td>
            <td>{{ roleLabelFromRow(s) }}</td>
            <td>{{ s.cccd || '-' }}</td>
            <td>{{ s.soDienThoai || '-' }}</td>
            <td>{{ s.email || '-' }}</td>
            <td class="text-center">
              <button class="btn-icon" title="Sửa" @click="openEdit(s)">✏️</button>
              <button class="btn-icon danger" title="Xóa (ẩn)" @click="softDeleteOnly(s)" style="margin-left:8px;">
                🗑️
              </button>
            </td>
          </tr>

          <tr v-if="loading">
            <td colspan="8" class="text-center">Đang tải dữ liệu...</td>
          </tr>
          <tr v-if="!loading && items.length === 0">
            <td colspan="8" class="text-center">Không tìm thấy nhân viên nào</td>
          </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="pagination-section" v-if="totalPages > 0">
        <button class="page-btn" :disabled="currentPage === 0" @click="changePage(currentPage - 1)">&lt;</button>

        <button
            v-for="page in totalPages"
            :key="page"
            class="page-btn"
            :class="{ active: currentPage === page - 1 }"
            @click="changePage(page - 1)"
        >
          {{ page }}
        </button>

        <button class="page-btn" :disabled="currentPage === totalPages - 1" @click="changePage(currentPage + 1)">&gt;</button>

        <span style="margin-left: 10px;">Trang {{ currentPage + 1 }}/{{ totalPages }}</span>
      </div>

      <p v-if="error" class="error-msg">{{ error }}</p>
    </div>

    <!-- Modal Add/Edit -->
    <div v-if="modal.open" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ modal.mode === 'create' ? 'Thêm nhân viên' : 'Sửa nhân viên' }}</h3>
          <button class="modal-close" @click="closeModal">✕</button>
        </div>

        <div class="modal-body">
          <div class="grid-2">
            <div class="form-group">
              <label>Quyền hạn *</label>
              <select class="form-input" v-model="form.quyenHanKey">
                <option value="ADMIN">ADMIN</option>
                <option value="NHAN_VIEN">NHÂN VIÊN</option>
              </select>
            </div>

            <div class="form-group">
              <label>Mã NV *</label>
              <input class="form-input" v-model="form.maNhanVien" :disabled="modal.mode==='edit'" />
            </div>

            <div class="form-group">
              <label>Tên NV *</label>
              <input class="form-input" v-model="form.tenNhanVien" />
            </div>

            <div class="form-group">
              <label>CCCD</label>
              <input class="form-input" v-model="form.cccd" />
            </div>

            <div class="form-group">
              <label>SĐT</label>
              <input class="form-input" v-model="form.soDienThoai" />
            </div>

            <div class="form-group">
              <label>Email</label>
              <input class="form-input" v-model="form.email" />
            </div>

            <div class="form-group">
              <label>Tài khoản *</label>
              <input class="form-input" v-model="form.taiKhoan" />
            </div>

            <div class="form-group">
              <label>
                Mật khẩu
                <span v-if="modal.mode==='create'">*</span>
                <span v-else class="hint">(để trống = giữ nguyên)</span>
              </label>
              <input type="password" class="form-input" v-model="form.matKhau" />
            </div>

            <div class="form-group">
              <label>Ngày sinh</label>
              <input type="date" class="form-input" v-model="form.ngaySinh" />
            </div>

            <div class="form-group">
              <label>Giới tính</label>
              <select class="form-input" v-model="form.gioiTinh">
                <option :value="null">-- Chọn --</option>
                <option :value="true">Nam</option>
                <option :value="false">Nữ</option>
              </select>
            </div>

            <div class="form-group">
              <label>Địa chỉ</label>
              <input class="form-input" v-model="form.diaChi" />
            </div>
          </div>

          <p v-if="modal.error" class="error-msg">{{ modal.error }}</p>
        </div>

        <div class="modal-footer">
          <button class="btn btn-outline" @click="closeModal">Hủy</button>
          <button class="btn btn-primary" :disabled="modal.saving" @click="submit">
            {{ modal.saving ? 'Đang lưu...' : 'Lưu' }}
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { onMounted, ref, reactive } from 'vue'
import http from '../../services/http'

// ✅ Sửa mapping ID cho đúng DB của bạn nếu khác
const ROLE_TO_ID = {
  ADMIN: 1,
  NHAN_VIEN: 2
}

const items = ref([])
const loading = ref(false)
const error = ref('')

const currentPage = ref(0)
const totalPages = ref(0)
const pageSize = ref(10)
const totalElements = ref(0)

const allCache = ref([])
const lastFilteredCache = ref([])

const filters = reactive({
  keyword: '',
  role: '',
  cccd: '',
  email: '',
  phone: '',
  username: '',
  status: 'active'
})

const modal = reactive({
  open: false,
  mode: 'create',
  saving: false,
  error: ''
})

const form = reactive({
  id: null,
  quyenHanKey: 'NHAN_VIEN',
  maNhanVien: '',
  tenNhanVien: '',
  soDienThoai: '',
  cccd: '',
  email: '',
  taiKhoan: '',
  matKhau: '',
  ngaySinh: '',
  gioiTinh: null,
  diaChi: ''
})

function confirmAction(actionText) {
  return confirm(`Bạn có chắc chắn ${actionText} nhân viên không ?`)
}

function safeStr(v) {
  return String(v ?? '').toLowerCase().trim()
}

function isDigitsOnly(v) {
  if (v === null || v === undefined) return false
  const s = String(v).trim()
  return s.length > 0 && /^\d+$/.test(s)
}

function isAtLeast18(dateStr) {
  if (!dateStr) return true // nếu không nhập thì không chặn
  const dob = new Date(dateStr)
  if (Number.isNaN(dob.getTime())) return false

  const today = new Date()
  const limit = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate())
  return dob <= limit
}

function isActiveStatus(v) {
  return v === true || v === 1 || String(v).toLowerCase() === 'true'
}

function unwrapList(data) {
  if (!data) return []
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.result)) return data.result
  if (Array.isArray(data?.content)) return data.content
  if (Array.isArray(data?.data)) return data.data
  return []
}

function normalizeStaff(x) {
  return {
    id: x?.id,
    maNhanVien: x?.maNhanVien ?? x?.ma ?? '',
    tenNhanVien: x?.tenNhanVien ?? x?.ten ?? '',
    soDienThoai: x?.soDienThoai ?? x?.sdt ?? '',
    cccd: x?.cccd ?? '',
    email: x?.email ?? '',
    taiKhoan: x?.taiKhoan ?? '',
    ngaySinh: x?.ngaySinh ?? null,
    gioiTinh: (x?.gioiTinh ?? x?.gioitinh) ?? null,
    diaChi: x?.diaChi ?? '',
    trangThai: x?.trangThai,
    // quyenHan có thể là object hoặc id
    quyenHanId: x?.quyenHanId ?? x?.idChucVu ?? x?.quyenHan?.id ?? null,
    tenQuyenHan: x?.tenQuyenHan ?? x?.quyenHan?.tenQuyenHan ?? x?.quyenHan?.ten ?? ''
  }
}

function roleLabelFromRow(row) {
  const r = normalizeStaff(row)
  const name = (r.tenQuyenHan || '').toUpperCase()
  if (name.includes('ADMIN')) return 'ADMIN'
  if (name.includes('NHAN')) return 'NHÂN VIÊN'
  if (r.quyenHanId === ROLE_TO_ID.ADMIN) return 'ADMIN'
  if (r.quyenHanId === ROLE_TO_ID.NHAN_VIEN) return 'NHÂN VIÊN'
  // fallback
  return r.tenQuyenHan || '-'
}

function applyFilters(list) {
  const kw = safeStr(filters.keyword)
  const role = safeStr(filters.role)
  const cccd = safeStr(filters.cccd)
  const email = safeStr(filters.email)
  const phone = safeStr(filters.phone)
  const username = safeStr(filters.username)
  const status = filters.status

  return (list || []).filter((raw) => {
    const s = normalizeStaff(raw)

    const active = isActiveStatus(s.trangThai)
    if (status === 'active' && !active) return false
    if (status === 'inactive' && active) return false

    const ma = safeStr(s.maNhanVien)
    const ten = safeStr(s.tenNhanVien)
    const sdt = safeStr(s.soDienThoai)
    const em = safeStr(s.email)
    const idCard = safeStr(s.cccd)
    const tk = safeStr(s.taiKhoan)
    const rl = safeStr(roleLabelFromRow(s))

    const matchKeyword =
        !kw || ma.includes(kw) || ten.includes(kw) || sdt.includes(kw) || em.includes(kw) || idCard.includes(kw) || tk.includes(kw)

    const matchRole = !role || rl.includes(role)
    const matchCccd = !cccd || idCard.includes(cccd)
    const matchEmail = !email || em.includes(email)
    const matchPhone = !phone || sdt.includes(phone)
    const matchUsername = !username || tk.includes(username)

    return matchKeyword && matchRole && matchCccd && matchEmail && matchPhone && matchUsername
  }).map(normalizeStaff)
}

async function apiGetAllStaff() {
  const res = await http.get('/api/nhan-vien')
  return unwrapList(res.data)
}

async function reload() {
  loading.value = true
  error.value = ''
  try {
    const all = await apiGetAllStaff()
    allCache.value = all

    const filtered = applyFilters(all)
    lastFilteredCache.value = filtered

    totalElements.value = filtered.length
    totalPages.value = totalElements.value === 0 ? 0 : Math.ceil(totalElements.value / pageSize.value)

    if (totalPages.value > 0 && currentPage.value > totalPages.value - 1) currentPage.value = 0

    const start = currentPage.value * pageSize.value
    items.value = filtered.slice(start, start + pageSize.value)
  } catch (e) {
    error.value = 'Không gọi được API nhân viên. Kiểm tra baseURL/port backend và token.'
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.keyword = ''
  filters.role = ''
  filters.cccd = ''
  filters.email = ''
  filters.phone = ''
  filters.username = ''
  filters.status = 'active'
  currentPage.value = 0
  reload()
}

function changePage(page) {
  if (page >= 0 && page < totalPages.value) {
    currentPage.value = page
    reload()
  }
}

function openCreate() {
  modal.open = true
  modal.mode = 'create'
  modal.error = ''
  modal.saving = false

  form.id = null
  form.quyenHanKey = 'NHAN_VIEN'
  form.maNhanVien = ''
  form.tenNhanVien = ''
  form.soDienThoai = ''
  form.cccd = ''
  form.email = ''
  form.taiKhoan = ''
  form.matKhau = ''
  form.ngaySinh = ''
  form.gioiTinh = null
  form.diaChi = ''
}

function openEdit(row) {
  const s = normalizeStaff(row)
  modal.open = true
  modal.mode = 'edit'
  modal.error = ''
  modal.saving = false

  form.id = s.id

  const roleName = (s.tenQuyenHan || '').toUpperCase()
  form.quyenHanKey = roleName.includes('ADMIN') ? 'ADMIN' : 'NHAN_VIEN'

  form.maNhanVien = s.maNhanVien || ''
  form.tenNhanVien = s.tenNhanVien || ''
  form.soDienThoai = s.soDienThoai || ''
  form.cccd = s.cccd || ''
  form.email = s.email || ''
  form.taiKhoan = s.taiKhoan || ''
  form.matKhau = ''
  form.ngaySinh = s.ngaySinh ? String(s.ngaySinh).slice(0, 10) : ''
  form.gioiTinh = (s.gioiTinh === true || s.gioiTinh === false) ? s.gioiTinh : null
  form.diaChi = s.diaChi || ''
}

function closeModal() {
  modal.open = false
  modal.error = ''
}

function validateForm() {
  if (!form.quyenHanKey) return 'Quyền hạn không được trống'
  if (!ROLE_TO_ID[form.quyenHanKey]) return 'Quyền hạn không hợp lệ'

  if (!form.maNhanVien?.trim()) return 'Mã nhân viên không được trống'
  if (!form.tenNhanVien?.trim()) return 'Tên nhân viên không được trống'
  if (!form.taiKhoan?.trim()) return 'Tài khoản không được trống'
  if (modal.mode === 'create' && !form.matKhau?.trim()) return 'Mật khẩu không được trống khi thêm mới'

  if (form.soDienThoai?.trim() && !isDigitsOnly(form.soDienThoai)) {
    return 'Số điện thoại phải là số'
  }

  if (form.cccd?.trim() && !isDigitsOnly(form.cccd)) {
    return 'CCCD phải là số'
  }

  if (form.ngaySinh && !isAtLeast18(form.ngaySinh)) {
    return 'Nhân viên phải đủ 18 tuổi'
  }

  return ''
}

async function validateDuplicates() {
  const all = allCache.value?.length ? allCache.value : await apiGetAllStaff()
  const excludeId = modal.mode === 'edit' ? form.id : null

  const username = safeStr(form.taiKhoan)
  if (username) {
    const dupUser = (all || []).some(x => {
      const n = normalizeStaff(x)
      return n.id !== excludeId && safeStr(n.taiKhoan) === username
    })
    if (dupUser) return 'Tài khoản đã tồn tại, vui lòng chọn tài khoản khác'
  }

  const cccd = String(form.cccd ?? '').trim()
  if (cccd) {
    const dupCccd = (all || []).some(x => {
      const n = normalizeStaff(x)
      return n.id !== excludeId && String(n.cccd ?? '').trim() === cccd
    })
    if (dupCccd) return 'CCCD đã tồn tại, vui lòng kiểm tra lại'
  }

  return ''
}

async function submit() {
  const msg = validateForm()
  if (msg) {
    modal.error = msg
    return
  }

  const dupMsg = await validateDuplicates()
  if (dupMsg) {
    modal.error = dupMsg
    return
  }

  if (modal.mode === 'create') {
    if (!confirmAction('thêm mới')) return
  } else {
    if (!confirmAction('sửa')) return
  }

  modal.saving = true
  modal.error = ''

  try {
    const quyenHanId = ROLE_TO_ID[form.quyenHanKey]

    const payload = {
      // entity-style
      quyenHanId,
      maNhanVien: form.maNhanVien.trim(),
      tenNhanVien: form.tenNhanVien.trim(),
      soDienThoai: form.soDienThoai?.trim() || null,
      cccd: form.cccd?.trim() || null,
      email: form.email?.trim() || null,
      taiKhoan: form.taiKhoan.trim(),
      ngaySinh: form.ngaySinh || null,
      gioiTinh: (form.gioiTinh === true || form.gioiTinh === false) ? form.gioiTinh : null,
      diaChi: form.diaChi?.trim() || null,
      trangThai: true,

      // fallback-style (nếu BE dùng idChucVu/ma/ten/sdt)
      idChucVu: quyenHanId,
      ma: form.maNhanVien.trim(),
      ten: form.tenNhanVien.trim(),
      sdt: form.soDienThoai?.trim() || null,
      gioitinh: (form.gioiTinh === true || form.gioiTinh === false) ? form.gioiTinh : null
    }

    if (modal.mode === 'create') {
      payload.matKhau = form.matKhau.trim()
      await http.post('/api/nhan-vien', payload)
    } else {
      if (form.matKhau?.trim()) payload.matKhau = form.matKhau.trim()
      await http.put(`/api/nhan-vien/${form.id}`, payload)
    }

    closeModal()
    await reload()
  } catch (e) {
    modal.error = e?.response?.data?.message || e?.response?.data?.error || e?.message || 'Có lỗi xảy ra'
  } finally {
    modal.saving = false
  }
}

async function softDeleteOnly(row) {
  try {
    if (!confirmAction('xóa')) return
    await http.delete(`/api/nhan-vien/${row.id}`)
    await reload()
  } catch (e) {
    alert(e?.response?.data?.message || e?.message || 'Có lỗi xảy ra')
  }
}

function exportExcel() {
  const data = lastFilteredCache.value || []
  if (!data.length) {
    alert('Không có dữ liệu để xuất.')
    return
  }

  const headers = ['Mã NV', 'Tên NV', 'Quyền hạn', 'CCCD', 'SĐT', 'Email', 'Tài khoản']
  const lines = [
    headers.join(','),
    ...data.map(x => {
      const row = [
        `"${String(x.maNhanVien ?? '').replaceAll('"', '""')}"`,
        `"${String(x.tenNhanVien ?? '').replaceAll('"', '""')}"`,
        `"${String(roleLabelFromRow(x) ?? '').replaceAll('"', '""')}"`,
        `"${String(x.cccd ?? '').replaceAll('"', '""')}"`,
        `"${String(x.soDienThoai ?? '').replaceAll('"', '""')}"`,
        `"${String(x.email ?? '').replaceAll('"', '""')}"`,
        `"${String(x.taiKhoan ?? '').replaceAll('"', '""')}"`
      ]
      return row.join(',')
    })
  ]

  const csv = '\ufeff' + lines.join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)

  const a = document.createElement('a')
  a.href = url
  a.download = `nhan_vien_${new Date().toISOString().slice(0,10)}.csv`
  document.body.appendChild(a)
  a.click()
  a.remove()
  URL.revokeObjectURL(url)
}

onMounted(reload)
</script>

<style scoped>
.page { padding: 20px; background-color: #f3f4f6; min-height: 100vh; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; }
.header-section h2 { font-size: 1.25rem; color: #374151; margin-bottom: 20px; font-weight: 600; }
.card { background: white; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); padding: 20px; border: 1px solid #e5e7eb; }
.card-header h3 { font-size: 1rem; font-weight: 600; margin-bottom: 15px; display: flex; align-items: center; gap: 8px; }
.filter-body { display: flex; flex-wrap: wrap; gap: 20px; }
.filter-left, .filter-right { flex: 1; min-width: 300px; }

.filter-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; }
@media (max-width: 1100px) { .filter-grid { grid-template-columns: 1fr; } }

.form-group { margin-bottom: 12px; }
.form-group label { display: block; font-size: 0.875rem; color: #374151; margin-bottom: 4px; font-weight: 500; }
.form-input { width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 0.875rem; outline: none; transition: border-color 0.2s; }
.form-input:focus { border-color: #3b82f6; }

.radio-row { display:flex; gap: 14px; align-items:center; flex-wrap: wrap; }
.radio-item { display:flex; gap: 6px; align-items:center; font-size: 0.9rem; color: #374151; }

.action-buttons-group{
  display: flex;
  flex-direction: column;
  gap: 10px;
  justify-content: space-between;
  align-self: stretch;
  min-width: 140px;
}

.btn { padding: 8px 16px; border-radius: 6px; font-size: 0.875rem; font-weight: 500; cursor: pointer; border: 1px solid transparent; transition: all 0.2s; }
.btn-outline { background: white; border-color: #d1d5db; color: #374151; }
.btn-outline:hover { background: #f9fafb; border-color: #9ca3af; }
.btn-primary { background: #1e3a8a; color: #fff; }
.btn-primary:hover { background: #1e40af; }

.btn-w-icon{
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  height: 36px;
  white-space: nowrap;
}

.btn-i{ font-size: 16px; line-height: 1; }

.table { width: 100%; border-collapse: separate; border-spacing: 0; margin-top: 10px; }
.table th { background-color: #1e293b; color: white; padding: 12px; text-align: left; font-size: 0.875rem; font-weight: 600; }
.table td { padding: 12px; border-bottom: 1px solid #e5e7eb; color: #4b5563; font-size: 0.875rem; }
.text-bold { font-weight: 600; color: #1f2937; }
.text-center { text-align: center; }

.btn-icon { background: #1e3a8a; color: white; border: none; border-radius: 4px; padding: 6px 10px; cursor: pointer; }
.btn-icon:hover { background: #1e40af; }
.btn-icon.danger { background: #b91c1c; }
.btn-icon.danger:hover { background: #991b1b; }

.pagination-section { margin-top: 20px; display: flex; justify-content: center; align-items: center; gap: 5px; color: #6b7280; font-size: 0.875rem; }
.page-btn { background: white; border: 1px solid #d1d5db; color: #374151; min-width: 32px; height: 32px; border-radius: 4px; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all 0.2s; }
.page-btn:hover:not(:disabled) { background-color: #f3f4f6; border-color: #9ca3af; }
.page-btn.active { background-color: #1e3a8a; color: white; border-color: #1e3a8a; }
.page-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.error-msg { color: #ef4444; margin-top: 10px; text-align: center; }
.hint { font-size: 0.75rem; color: #6b7280; margin-left: 6px; font-weight: 400; }

.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display:flex; align-items:center; justify-content:center; z-index: 1000; padding: 16px; }
.modal { width: 980px; max-width: 96vw; background:#fff; border-radius: 10px; border: 1px solid #e5e7eb; box-shadow: 0 10px 30px rgba(0,0,0,0.25); overflow:hidden; }
.modal-header { display:flex; align-items:center; justify-content:space-between; padding: 14px 16px; background:#f9fafb; border-bottom: 1px solid #e5e7eb; }
.modal-close { border:none; background:transparent; font-size: 18px; cursor:pointer; }
.modal-body { padding: 16px; }
.modal-footer { padding: 14px 16px; border-top: 1px solid #e5e7eb; display:flex; justify-content:flex-end; gap: 10px; }
.grid-2 { display:grid; grid-template-columns: 1fr 1fr; gap: 12px 16px; }
@media (max-width: 860px) { .grid-2 { grid-template-columns: 1fr; } }
</style>
