<template>
  <div class="page">
    <div class="header-section">
      <h2>Quản lý khách hàng / Danh sách khách hàng</h2>
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
                placeholder="Tìm theo mã, tên, SĐT, email, tài khoản..."
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
              <label>Email</label>
              <input type="text" v-model="filters.email" placeholder="Email" class="form-input" @keyup.enter="reload" />
            </div>

            <div class="form-group">
              <label>Số điện thoại</label>
              <input type="text" v-model="filters.phone" placeholder="SĐT" class="form-input" @keyup.enter="reload" />
            </div>

            <div class="form-group">
              <label>Mã khách hàng</label>
              <input type="text" v-model="filters.code" placeholder="Mã KH" class="form-input" @keyup.enter="reload" />
            </div>

            <div class="form-group">
              <label>Tài khoản</label>
              <input type="text" v-model="filters.username" placeholder="Tài khoản" class="form-input" @keyup.enter="reload" />
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
        <h3>Tổng số khách hàng {{ totalElements }}</h3>
      </div>

      <div class="table-responsive">
        <table class="table">
          <thead>
          <tr>
            <th>STT</th>
            <th>Mã KH</th>
            <th>Tên KH</th>
            <th>SĐT</th>
            <th>Email</th>
            <th>Tài khoản</th>
            <th class="text-center">Hành động</th>
          </tr>
          </thead>

          <tbody>
          <tr v-for="(c, index) in items" :key="c.id">
            <td>{{ (currentPage * pageSize) + index + 1 }}</td>
            <td>{{ c.maKhachHang }}</td>
            <td class="text-bold">{{ c.tenKhachHang }}</td>
            <td>{{ c.soDienThoai || '-' }}</td>
            <td>{{ c.email || '-' }}</td>
            <td>{{ c.taiKhoan || '-' }}</td>
            <td class="text-center">
              <button class="btn-icon" title="Sửa" @click="openEdit(c)">✏️</button>
              <button class="btn-icon danger" title="Xóa (ẩn)" @click="softDeleteOnly(c)" style="margin-left:8px;">
                🗑️
              </button>
            </td>
          </tr>

          <tr v-if="loading">
            <td colspan="7" class="text-center">Đang tải dữ liệu...</td>
          </tr>
          <tr v-if="!loading && items.length === 0">
            <td colspan="7" class="text-center">Không tìm thấy khách hàng nào</td>
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
          <h3>{{ modal.mode === 'create' ? 'Thêm khách hàng' : 'Sửa khách hàng' }}</h3>
          <button class="modal-close" @click="closeModal">✕</button>
        </div>

        <div class="modal-body">
          <div class="grid-2">
            <div class="form-group">
              <label>Mã KH *</label>
              <input class="form-input" v-model="form.maKhachHang" :disabled="modal.mode==='edit'" />
            </div>

            <div class="form-group">
              <label>Tên KH *</label>
              <input class="form-input" v-model="form.tenKhachHang" />
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

const items = ref([])
const loading = ref(false)
const error = ref('')

const currentPage = ref(0)
const totalPages = ref(0)
const pageSize = ref(10)
const totalElements = ref(0)

const allCache = ref([])        // để check trùng + export
const lastFilteredCache = ref([])

const filters = reactive({
  keyword: '',
  email: '',
  phone: '',
  code: '',
  username: '',
  status: 'active' // ✅ mặc định chỉ hiển thị
})

const modal = reactive({
  open: false,
  mode: 'create', // create | edit
  saving: false,
  error: ''
})

const form = reactive({
  id: null,
  maKhachHang: '',
  tenKhachHang: '',
  soDienThoai: '',
  taiKhoan: '',
  matKhau: '',
  email: ''
})

function confirmAction(actionText) {
  return confirm(`Bạn có chắc chắn ${actionText} khách hàng không ?`)
}

function safeStr(v) {
  return String(v ?? '').toLowerCase().trim()
}

function isDigitsOnly(v) {
  if (v === null || v === undefined) return false
  const s = String(v).trim()
  return s.length > 0 && /^\d+$/.test(s)
}

function isActiveStatus(v) {
  return v === true || v === 1 || String(v).toLowerCase() === 'true'
}

function unwrapList(data) {
  // hỗ trợ ApiResponse {result: []} hoặc trả thẳng []
  if (!data) return []
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.result)) return data.result
  if (Array.isArray(data?.content)) return data.content
  if (Array.isArray(data?.data)) return data.data
  return []
}

function normalizeCustomer(x) {
  // hỗ trợ nhiều dạng field
  return {
    id: x?.id,
    maKhachHang: x?.maKhachHang ?? x?.ma ?? '',
    tenKhachHang: x?.tenKhachHang ?? x?.ten ?? '',
    soDienThoai: x?.soDienThoai ?? x?.sdt ?? '',
    taiKhoan: x?.taiKhoan ?? x?.username ?? '',
    email: x?.email ?? '',
    trangThai: x?.trangThai
  }
}

function applyFilters(list) {
  const kw = safeStr(filters.keyword)
  const email = safeStr(filters.email)
  const phone = safeStr(filters.phone)
  const code = safeStr(filters.code)
  const username = safeStr(filters.username)

  const status = filters.status // active|inactive|all

  return (list || []).filter((raw) => {
    const c = normalizeCustomer(raw)

    // status filter
    const active = isActiveStatus(c.trangThai)
    if (status === 'active' && !active) return false
    if (status === 'inactive' && active) return false

    const ma = safeStr(c.maKhachHang)
    const ten = safeStr(c.tenKhachHang)
    const sdt = safeStr(c.soDienThoai)
    const em = safeStr(c.email)
    const tk = safeStr(c.taiKhoan)

    const matchKeyword =
        !kw || ma.includes(kw) || ten.includes(kw) || sdt.includes(kw) || em.includes(kw) || tk.includes(kw)

    const matchEmail = !email || em.includes(email)
    const matchPhone = !phone || sdt.includes(phone)
    const matchCode = !code || ma.includes(code)
    const matchUsername = !username || tk.includes(username)

    return matchKeyword && matchEmail && matchPhone && matchCode && matchUsername
  }).map(normalizeCustomer)
}

async function apiGetAllCustomers() {
  const res = await http.get('/api/khach-hang')
  return unwrapList(res.data)
}

async function reload() {
  loading.value = true
  error.value = ''
  try {
    const all = await apiGetAllCustomers()
    allCache.value = all

    const filtered = applyFilters(all)
    lastFilteredCache.value = filtered

    totalElements.value = filtered.length
    totalPages.value = totalElements.value === 0 ? 0 : Math.ceil(totalElements.value / pageSize.value)

    if (totalPages.value > 0 && currentPage.value > totalPages.value - 1) currentPage.value = 0

    const start = currentPage.value * pageSize.value
    items.value = filtered.slice(start, start + pageSize.value)
  } catch (e) {
    error.value = 'Không gọi được API khách hàng. Kiểm tra baseURL/port backend và token.'
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.keyword = ''
  filters.email = ''
  filters.phone = ''
  filters.code = ''
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
  form.maKhachHang = ''
  form.tenKhachHang = ''
  form.soDienThoai = ''
  form.taiKhoan = ''
  form.matKhau = ''
  form.email = ''
}

function openEdit(row) {
  modal.open = true
  modal.mode = 'edit'
  modal.error = ''
  modal.saving = false

  form.id = row.id
  form.maKhachHang = row.maKhachHang || ''
  form.tenKhachHang = row.tenKhachHang || ''
  form.soDienThoai = row.soDienThoai || ''
  form.taiKhoan = row.taiKhoan || ''
  form.matKhau = '' // để trống = giữ nguyên
  form.email = row.email || ''
}

function closeModal() {
  modal.open = false
  modal.error = ''
}

function validateForm() {
  if (!form.maKhachHang?.trim()) return 'Mã khách hàng không được trống'
  if (!form.tenKhachHang?.trim()) return 'Tên khách hàng không được trống'
  if (!form.taiKhoan?.trim()) return 'Tài khoản không được trống'
  if (modal.mode === 'create' && !form.matKhau?.trim()) return 'Mật khẩu không được trống khi thêm mới'

  if (form.soDienThoai?.trim() && !isDigitsOnly(form.soDienThoai)) {
    return 'Số điện thoại phải là số'
  }

  return ''
}

async function validateDuplicateUsername() {
  const all = allCache.value?.length ? allCache.value : await apiGetAllCustomers()
  const excludeId = modal.mode === 'edit' ? form.id : null

  const username = safeStr(form.taiKhoan)
  if (!username) return ''

  const dup = (all || []).some(x => {
    const n = normalizeCustomer(x)
    return n.id !== excludeId && safeStr(n.taiKhoan) === username
  })

  return dup ? 'Tài khoản đã tồn tại, vui lòng chọn tài khoản khác' : ''
}

async function submit() {
  const msg = validateForm()
  if (msg) {
    modal.error = msg
    return
  }

  // check trùng tài khoản
  const dupMsg = await validateDuplicateUsername()
  if (dupMsg) {
    modal.error = dupMsg
    return
  }

  // confirm
  if (modal.mode === 'create') {
    if (!confirmAction('thêm mới')) return
  } else {
    if (!confirmAction('sửa')) return
  }

  modal.saving = true
  modal.error = ''

  try {
    const payload = {
      // entity-style
      maKhachHang: form.maKhachHang.trim(),
      tenKhachHang: form.tenKhachHang.trim(),
      soDienThoai: form.soDienThoai?.trim() || null,
      taiKhoan: form.taiKhoan.trim(),
      email: form.email?.trim() || null,
      trangThai: true,

      // fallback-style (nếu BE dùng ma/ten/sdt)
      ma: form.maKhachHang.trim(),
      ten: form.tenKhachHang.trim(),
      sdt: form.soDienThoai?.trim() || null
    }

    if (modal.mode === 'create') {
      payload.matKhau = form.matKhau.trim()
      await http.post('/api/khach-hang', payload)
    } else {
      if (form.matKhau?.trim()) payload.matKhau = form.matKhau.trim()
      await http.put(`/api/khach-hang/${form.id}`, payload)
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
    await http.delete(`/api/khach-hang/${row.id}`)
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

  const headers = ['Mã KH', 'Tên KH', 'SĐT', 'Email', 'Tài khoản']
  const lines = [
    headers.join(','),
    ...data.map(x => {
      const row = [
        `"${String(x.maKhachHang ?? '').replaceAll('"', '""')}"`,
        `"${String(x.tenKhachHang ?? '').replaceAll('"', '""')}"`,
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
  a.download = `khach_hang_${new Date().toISOString().slice(0,10)}.csv`
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
.modal { width: 920px; max-width: 96vw; background:#fff; border-radius: 10px; border: 1px solid #e5e7eb; box-shadow: 0 10px 30px rgba(0,0,0,0.25); overflow:hidden; }
.modal-header { display:flex; align-items:center; justify-content:space-between; padding: 14px 16px; background:#f9fafb; border-bottom: 1px solid #e5e7eb; }
.modal-close { border:none; background:transparent; font-size: 18px; cursor:pointer; }
.modal-body { padding: 16px; }
.modal-footer { padding: 14px 16px; border-top: 1px solid #e5e7eb; display:flex; justify-content:flex-end; gap: 10px; }
.grid-2 { display:grid; grid-template-columns: 1fr 1fr; gap: 12px 16px; }
@media (max-width: 860px) { .grid-2 { grid-template-columns: 1fr; } }
</style>
