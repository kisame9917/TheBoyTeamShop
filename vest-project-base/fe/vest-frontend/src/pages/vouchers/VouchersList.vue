<template>
  <div class="product-page">
    <!-- Header -->
    <div class="header-section">
      <h2>Quản lý giảm giá </h2>
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
              placeholder="Tìm theo mã hoặc tên phiếu giảm giá..."
              class="form-input"
              @keyup.enter="reload"
            />
          </div>
        </div>

        <div class="filter-right">
          <div class="filter-grid">
            <!-- Loại giảm -->
            <div class="form-group">
              <label>Loại giảm</label>
              <select v-model="filters.loai" class="form-input">
                <option value="">Tất cả</option>
                <option value="PERCENT">Giảm phần trăm</option>
                <option value="MONEY">Giảm tiền</option>
              </select>
            </div>

            <!-- Trạng thái nghiệp vụ -->
            <div class="form-group">
              <label>Trạng thái </label>
              <select class="form-input" v-model="filters.bizStatus">
                <option value="">Tất cả</option>
                <option value="UPCOMING">Sắp diễn ra</option>
                <option value="ACTIVE">Đang áp dụng</option>
                <option value="EXPIRED">Kết thúc</option>
              </select>
            </div>

            <div class="form-group">
              <label>Từ ngày</label>
              <input type="date" class="form-input" v-model="filters.from" />
            </div>

            <div class="form-group">
              <label>Đến ngày</label>
              <input type="date" class="form-input" v-model="filters.to" />
            </div>
          </div>
        </div>

        <div class="action-buttons-group">
          <button class="btn btn-outline" @click="exportExcel">Xuất File Excel</button>
          <button class="btn btn-outline" @click="openCreate">Thêm mới</button>
          <button class="btn btn-outline" @click="resetFilters">Đặt lại</button>
        </div>
      </div>
    </div>

    <div style="height: 20px;"></div>

    <!-- Table -->
    <div class="card table-card">
      <div class="table-header-info">
        <h3>Tổng số phiếu giảm giá: {{ totalElements }}</h3>
      </div>

      <div class="table-responsive">
        <table class="table">
          <thead class="thead-dark">
            <tr>
              <th class="col-stt">STT</th>
              <th>Mã giảm giá</th>
              <th>Tên giảm giá</th>

              <th class="col-type">Loại</th>
              <th class="col-value">Giá trị giảm</th>

              <th class="col-qty">Số lượng</th>
              <th class="col-date">Ngày bắt đầu</th>
              <th class="col-date">Ngày kết thúc</th>

              <th class="col-status">Trạng thái</th>
              <th class="col-action">Hành động</th>
            </tr>
          </thead>

          <tbody>
            <tr v-for="(v, index) in pagedItems" :key="v.id">
              <td class="col-stt">{{ (currentPage * pageSize) + index + 1 }}</td>
              <td>{{ v.maGiamGia }}</td>
              <td class="text-bold">{{ v.tenGiamGia }}</td>

              <td class="col-type">
                <span class="pill" :class="v.loaiGiam ? 'pill-percent' : 'pill-money'">
                  {{ v.loaiGiam ? 'Phần trăm' : 'Tiền mặt' }}
                </span>
              </td>

              <td class="col-value text-bold">
                {{ renderGiaTriGiamRow(v) }}
              </td>

              <td class="col-qty">{{ v.soLuong ?? 0 }}</td>

              <td class="col-date">{{ formatDateOnly(v.ngayBatDau) }}</td>
              <td class="col-date">{{ formatDateOnly(v.ngayKetThuc) }}</td>

              <td class="col-status">
                <span :class="['badge', getBadgeClass(v)]">
                  {{ getBizStatusText(v) }}
                </span>
              </td>

              <td class="col-action">
                <div class="action-wrap">
                  <button class="btn-icon" @click="openDetail(v.id)">👁️</button>
                  <button class="btn-icon" @click="openEdit(v)">✏️</button>
                  <button class="btn-icon danger" @click="softDelete(v.id)">🗑️</button>
                </div>
              </td>
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

    <!-- Detail popup -->
    <div v-if="showDetail" class="modal-overlay" @click.self="closeDetail">
      <div class="modal">
        <h3 class="modal-title">Chi tiết phiếu giảm giá</h3>

        <div class="detail-grid" v-if="detail">
          <div><b>Mã:</b> {{ detail.maGiamGia }}</div>
          <div><b>Tên:</b> {{ detail.tenGiamGia }}</div>

          <div><b>Số lượng:</b> {{ detail.soLuong ?? 0 }}</div>
          <div><b>Trạng thái:</b> {{ detail.trangThai ? 'Hoạt động' : 'Đã xoá' }}</div>

          <div class="full"><b>Mô tả:</b> {{ detail.moTa || '-' }}</div>

          <div><b>Loại giảm:</b> {{ detail.loaiGiam ? 'Giảm %' : 'Giảm tiền' }}</div>
          <div><b>Giá trị:</b> {{ renderDetailValue(detail) }}</div>

          <div><b>Ngày bắt đầu:</b> {{ formatDate(detail.ngayBatDau) }}</div>
          <div><b>Ngày kết thúc:</b> {{ formatDate(detail.ngayKetThuc) }}</div>

          <div><b>Ngày tạo:</b> {{ formatDate(detail.ngayTao) }}</div>
          <div><b>Ngày cập nhật:</b> {{ formatDate(detail.ngayCapNhat) }}</div>
        </div>

        <div class="modal-actions">
          <button class="btn btn-outline" @click="closeDetail">Đóng</button>
        </div>
      </div>
    </div>

    <!-- Create/Edit popup -->
    <div v-if="showForm" class="modal-overlay" @click.self="closeForm">
      <div class="modal">
        <h3 class="modal-title">
          {{ formMode === 'create' ? 'Thêm phiếu giảm giá' : 'Cập nhật phiếu giảm giá' }}
        </h3>

        <div class="form-grid">
          <div class="form-group">
            <label>Mã giảm giá</label>
            <input class="form-input" v-model.trim="form.maGiamGia" />
          </div>

          <div class="form-group">
            <label>Tên giảm giá</label>
            <input class="form-input" v-model.trim="form.tenGiamGia" />
          </div>

          <div class="form-group">
            <label>Số lượng</label>
            <input type="number" class="form-input" v-model.number="form.soLuong" min="1" />
          </div>

          <div class="form-group">
            <label>Loại giảm</label>
            <select class="form-input" v-model="form.loaiGiam">
              <option :value="true">Giảm %</option>
              <option :value="false">Giảm tiền mặt (VND)</option>
            </select>
          </div>

          <div class="form-group">
            <label>Giá trị giảm</label>
            <div class="input-with-suffix">
              <input
                type="number"
                class="form-input"
                v-model.number="form.giaTriGiam"
                :min="1"
                :max="form.loaiGiam ? 100 : null"
              />
              <span class="suffix">{{ form.loaiGiam ? '%' : 'VND' }}</span>
            </div>
          </div>

          <div class="form-group">
            <label>Ngày bắt đầu</label>
            <input type="date" class="form-input" v-model="form.ngayBatDau" />
          </div>

          <div class="form-group">
            <label>Ngày kết thúc</label>
            <input type="date" class="form-input" v-model="form.ngayKetThuc" />
          </div>

          <!-- ✅ MÔ TẢ: dùng cho cả Create & Update -->
          <div class="form-group full-row">
            <label>Mô tả</label>
            <textarea
              class="form-input form-textarea"
              v-model.trim="form.moTa"
              placeholder="Nhập mô tả (không bắt buộc)..."
            ></textarea>
          </div>

          <!-- ✅ KHÔNG HIỆN TRẠNG THÁI TRONG FORM (nhưng BE vẫn nhận) -->
        </div>

        <div class="modal-actions">
          <button class="btn btn-outline" @click="closeForm">Huỷ</button>
          <button class="btn btn-primary" @click="submitForm">Lưu</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

const API = 'http://localhost:8080/api/pgg'

const getAllPhieuGiamGia = async () => (await axios.get(API)).data
const getDetailPhieuGiamGia = async (id) => (await axios.get(`${API}/${id}`)).data
const createPhieuGiamGia = async (data) => (await axios.post(`${API}/create`, data)).data
const updatePhieuGiamGia = async (id, data) => (await axios.put(`${API}/update/${id}`, data)).data

const softDeletePhieuGiamGia = async (id) => {
  try {
    return (await axios.put(`${API}/${id}/delete`)).data
  } catch (e) {
    return (await axios.put(`${API}/update/${id}`, { trangThai: false })).data
  }
}

function todayYMD() {
  const d = new Date()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${mm}-${dd}`
}
function normalizeYMD(v) {
  if (!v) return ''
  return String(v).slice(0, 10)
}
function formatDateOnly(v) {
  const s = normalizeYMD(v)
  if (!s) return '-'
  const [y, m, d] = s.split('-')
  return `${d}/${m}/${y}`
}

function getBizStatusText(v) {
  const start = normalizeYMD(v.ngayBatDau)
  const end = normalizeYMD(v.ngayKetThuc)
  const now = todayYMD()

  if (start && now < start) return 'Sắp diễn ra'
  if (end && now > end) return 'Kết thúc'
  return 'Đang áp dụng'
}
function getBadgeClass(v) {
  const st = getBizStatusText(v)
  if (st === 'Đang áp dụng') return 'badge-success'
  if (st === 'Sắp diễn ra') return 'badge-warning'
  if (st === 'Kết thúc') return 'badge-muted'
  return 'badge-muted'
}

// ====== UI state ======
const loading = ref(false)
const error = ref('')
const items = ref([])

// filters
const filters = ref({
  keyword: '',
  loai: '',
  bizStatus: '',
  from: '',
  to: ''
})

// pagination
const currentPage = ref(0)
const pageSize = ref(10)

const filteredItems = computed(() => {
  const kw = (filters.value.keyword || '').trim().toLowerCase()
  const biz = filters.value.bizStatus
  const from = filters.value.from
  const to = filters.value.to
  const today = todayYMD()
  const loai = filters.value.loai

  return (items.value || [])
    // ✅ ẩn soft delete
    .filter(v => v.trangThai === true)

    // keyword
    .filter(v => {
      if (!kw) return true
      const ma = String(v.maGiamGia ?? '').toLowerCase()
      const ten = String(v.tenGiamGia ?? '').toLowerCase()
      return ma.includes(kw) || ten.includes(kw)
    })

    // lọc loại giảm
    .filter(v => {
      if (!loai) return true
      if (loai === 'PERCENT') return v.loaiGiam === true
      if (loai === 'MONEY') return v.loaiGiam === false
      return true
    })

    // lọc theo khoảng ngày [from,to] giao với [start,end]
    .filter(v => {
      const start = normalizeYMD(v.ngayBatDau)
      const end = normalizeYMD(v.ngayKetThuc)
      if (from && end && end < from) return false
      if (to && start && start > to) return false
      return true
    })

    // lọc trạng thái nghiệp vụ
    .filter(v => {
      if (!biz) return true
      const start = normalizeYMD(v.ngayBatDau)
      const end = normalizeYMD(v.ngayKetThuc)

      if (biz === 'UPCOMING') return start && today < start
      if (biz === 'EXPIRED') return end && today > end
      return (!start || today >= start) && (!end || today <= end) // ACTIVE
    })
})

/**
 * ✅ SORT LUÔN ĐÚNG:
 * - ưu tiên ngayTao DESC
 * - fallback id DESC
 */
const sortedItems = computed(() => {
  return [...filteredItems.value].sort((a, b) => {
    const ta = String(a.ngayTao ?? '')
    const tb = String(b.ngayTao ?? '')

    const byTime = tb.localeCompare(ta)
    if (byTime !== 0) return byTime

    return Number(b.id ?? 0) - Number(a.id ?? 0)
  })
})

const totalElements = computed(() => filteredItems.value.length)
const totalPages = computed(() => Math.ceil(totalElements.value / pageSize.value))

const pagedItems = computed(() => {
  const start = currentPage.value * pageSize.value
  return sortedItems.value.slice(start, start + pageSize.value)
})

function changePage(p) {
  if (p < 0) return
  if (p > totalPages.value - 1) return
  currentPage.value = p
}

// load
async function reload() {
  loading.value = true
  error.value = ''
  try {
    const data = await getAllPhieuGiamGia()

    // ✅ map snake_case -> camelCase (BẮT BUỘC để sort theo ngayTao)
    items.value = (Array.isArray(data) ? data : []).map(x => ({
      ...x,
      ngayBatDau: x.ngayBatDau ?? x.ngay_bat_dau ?? null,
      ngayKetThuc: x.ngayKetThuc ?? x.ngay_ket_thuc ?? null,
      ngayTao: x.ngayTao ?? x.ngay_tao ?? null,
      ngayCapNhat: x.ngayCapNhat ?? x.ngay_cap_nhat ?? null
    }))

    currentPage.value = 0
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || 'Không tải được dữ liệu'
  } finally {
    loading.value = false
  }
  currentPage.value = 0
}

function resetFilters() {
  filters.value.keyword = ''
  filters.value.loai = ''
  filters.value.bizStatus = ''
  filters.value.from = ''
  filters.value.to = ''
  currentPage.value = 0
  reload()
}

function exportExcel() {
  // TODO
}

// ===== detail =====
const showDetail = ref(false)
const detail = ref(null)

async function openDetail(id) {
  loading.value = true
  error.value = ''
  try {
    detail.value = await getDetailPhieuGiamGia(id)
    showDetail.value = true
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || 'Không tải được chi tiết'
  } finally {
    loading.value = false
  }
}
function closeDetail() {
  showDetail.value = false
  detail.value = null
}

// ===== form =====
const showForm = ref(false)
const formMode = ref('create')
const editingId = ref(null)

/**
 * ✅ Form KHÔNG HIỆN trạng thái, nhưng vẫn giữ field để gửi BE
 * ✅ BỎ HẲN giaTriToiDa
 * ✅ Có moTa để create/update
 */
const form = ref({
  maGiamGia: '',
  tenGiamGia: '',
  soLuong: 1,
  loaiGiam: true,
  giaTriGiam: 1,
  ngayBatDau: '',
  ngayKetThuc: '',
  moTa: '',
  trangThai: true
})

function openCreate() {
  formMode.value = 'create'
  editingId.value = null
  form.value = {
    maGiamGia: '',
    tenGiamGia: '',
    soLuong: 1,
    loaiGiam: true,
    giaTriGiam: 1,
    ngayBatDau: '',
    ngayKetThuc: '',
    moTa: '',
    trangThai: true // ✅ create luôn true
  }
  showForm.value = true
}

function openEdit(v) {
  formMode.value = 'edit'
  editingId.value = v.id
  form.value = {
    maGiamGia: v.maGiamGia ?? '',
    tenGiamGia: v.tenGiamGia ?? '',
    soLuong: v.soLuong ?? 1,
    loaiGiam: v.loaiGiam ?? true,
    giaTriGiam: (v.loaiGiam
      ? (v.giaTriPhanTram ?? v.giaTriGiam)
      : (v.giaTriTienMat ?? v.giaTriGiam)
    ) ?? 1,
    ngayBatDau: normalizeYMD(v.ngayBatDau) || '',
    ngayKetThuc: normalizeYMD(v.ngayKetThuc) || '',
    moTa: v.moTa ?? '',
    trangThai: v.trangThai ?? true // ✅ giữ nguyên trạng thái để update không null
  }
  showForm.value = true
}

function closeForm() {
  showForm.value = false
}

async function submitForm() {
  try {
    // validate nhanh
    if (!form.value.maGiamGia?.trim()) return alert('Vui lòng nhập mã giảm giá')
    if (!form.value.tenGiamGia?.trim()) return alert('Vui lòng nhập tên giảm giá')
    if (!form.value.soLuong || form.value.soLuong < 1) return alert('Số lượng phải >= 1')
    if (!form.value.giaTriGiam || form.value.giaTriGiam < 1) return alert('Giá trị giảm phải >= 1')
    if (form.value.loaiGiam === true && form.value.giaTriGiam > 100) return alert('Giảm % tối đa 100')
    if (form.value.ngayBatDau && form.value.ngayKetThuc && form.value.ngayKetThuc < form.value.ngayBatDau)
      return alert('Ngày kết thúc phải >= ngày bắt đầu')

    // ✅ map payload theo đúng BE + luôn gửi trangThai (DB NOT NULL)
    const payload = {
      maGiamGia: form.value.maGiamGia,
      tenGiamGia: form.value.tenGiamGia,
      soLuong: form.value.soLuong,
      loaiGiam: form.value.loaiGiam,
      ngayBatDau: form.value.ngayBatDau,
      ngayKetThuc: form.value.ngayKetThuc,
      moTa: form.value.moTa ?? '',
      trangThai: (formMode.value === 'create') ? true : (form.value.trangThai ?? true)
    }

    if (payload.loaiGiam === true) {
      payload.giaTriPhanTram = form.value.giaTriGiam
      payload.giaTriTienMat = null
    } else {
      payload.giaTriTienMat = form.value.giaTriGiam
      payload.giaTriPhanTram = null
    }

    if (formMode.value === 'create') {
      await createPhieuGiamGia(payload)
    } else {
      await updatePhieuGiamGia(editingId.value, payload)
    }

    closeForm()
    await reload()
    currentPage.value = 0 // ✅ về trang đầu để thấy item mới
  } catch (e) {
    alert(e?.response?.data?.message || e?.message || 'Lưu thất bại')
  }
}

async function softDelete(id) {
  if (!confirm('Xoá phiếu giảm giá này?')) return
  try {
    await softDeletePhieuGiamGia(id)
    await reload()
  } catch (e) {
    alert(e?.response?.data?.message || e?.message || 'Xoá thất bại')
  }
}

// helpers
function formatDate(d) {
  if (!d) return '-'
  return String(d).replace('T', ' ').slice(0, 16)
}
function formatMoney(v) {
  const n = Number(v)
  if (Number.isNaN(n)) return String(v ?? '-')
  return n.toLocaleString('vi-VN') + ' ₫'
}
function renderDetailValue(d) {
  if (!d) return '-'
  if (d.loaiGiam) return `${d.giaTriPhanTram ?? d.giaTriGiam ?? '-'} %`
  return formatMoney(d.giaTriTienMat ?? d.giaTriGiam)
}
function renderGiaTriGiamRow(v) {
  if (v?.loaiGiam === true) {
    const pct = Number(v?.giaTriPhanTram ?? v?.gia_tri_phan_tram ?? 0)
    return `${pct}%`
  }
  const money = Number(v?.giaTriTienMat ?? v?.gia_tri_tien_mat ?? 0)
  return formatMoney(money)
}

onMounted(reload)
</script>

<style scoped>
.product-page { padding: 18px 22px; }
.header-section h2 { font-size: 18px; font-weight: 600; margin: 0 0 10px; }

.card {
  background: #fff;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 2px rgba(0,0,0,.04);
}

.filter-card .card-header { padding: 14px 16px 0; }
.filter-card .card-header h3 { font-size: 14px; font-weight: 600; margin: 0; color: #111827; }
.icon { margin-right: 6px; }

.filter-body {
  padding: 12px 16px 16px;
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.filter-left { flex: 1.4; min-width: 420px; }
.filter-right { flex: 1; min-width: 360px; }

.filter-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px 12px;
}

.action-buttons-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-left: auto;
}

.form-group label { display: block; font-size: 12px; color: #374151; margin-bottom: 6px; }
.form-group.full-width { width: 100%; }

.form-input {
  width: 100%;
  height: 34px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  padding: 0 10px;
  font-size: 13px;
  outline: none;
}

.btn {
  height: 34px;
  border-radius: 4px;
  border: 1px solid #d1d5db;
  background: #fff;
  padding: 0 10px;
  font-size: 13px;
  cursor: pointer;
}
.btn-outline { background: #fff; }
.btn-primary { background: #1f2a44; border-color: #1f2a44; color: #fff; }

.table-card { padding: 0 0 12px; }
.table-header-info { padding: 14px 16px; border-bottom: 1px solid #e5e7eb; }
.table-header-info h3 { margin: 0; font-size: 14px; font-weight: 600; }

.table-responsive { padding: 0 16px 10px; }
.table { width: 100%; border-collapse: collapse; font-size: 13px; }

.thead-dark tr { background: #1f2a44; }
.thead-dark th { color: #fff; font-weight: 600; padding: 10px 10px; text-align: left; }

.table td { padding: 10px 10px; border-bottom: 1px solid #e5e7eb; }

.text-bold { font-weight: 600; }
.col-date { width: 130px; white-space: nowrap; }

.badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}
.badge-success { background: #dcfce7; color: #166534; }
.badge-warning { background: #fef3c7; color: #92400e; }
.badge-muted { background: #e5e7eb; color: #374151; }

.btn-icon { background: none; border: none; cursor: pointer; font-size: 16px; margin: 0 4px; }
.btn-icon.danger { color: #dc2626; }

.pagination-section {
  padding: 10px 16px 0;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}
.page-btn {
  height: 30px;
  min-width: 30px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
}
.page-btn.active { background: #1f2a44; color: #fff; border-color: #1f2a44; }

.error-msg { padding: 6px 16px 0; color: #b91c1c; font-size: 13px; }

/* modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

/* ✅ form to hơn */
.modal {
  background: #fff;
  width: 900px;              /* tăng từ 760 -> 900 */
  max-width: 96%;
  border-radius: 10px;
  padding: 22px 24px;        /* tăng padding */
  box-shadow: 0 20px 40px rgba(0,0,0,.25);
  animation: popup .2s ease;
}

.modal-title { margin: 0 0 14px; font-size: 16px; font-weight: 700; }

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px 14px;
  font-size: 13px;
}
.detail-grid .full { grid-column: 1 / -1; }

.modal-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 14px; /* tăng nhẹ spacing */
}

/* suffix in input */
.input-with-suffix { position: relative; }
.input-with-suffix .form-input { padding-right: 64px; }
.suffix {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  color: #6b7280;
  font-weight: 600;
  pointer-events: none;
}

/* ✅ textarea */
.full-row { grid-column: 1 / -1; }
.form-textarea {
  height: 96px;
  padding: 8px 10px;
  resize: vertical;
  line-height: 1.4;
}

@keyframes popup {
  from { transform: translateY(-8px) scale(.99); opacity: 0; }
  to { transform: none; opacity: 1; }
}
</style>
