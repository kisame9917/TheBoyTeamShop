<template>
  <div class="product-page">
    <!-- Header -->
    <div class="header-section">
      <h2>Quản lý phiếu giảm giá</h2>
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
              <label>Trạng thái</label>
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
          <button class="btn btn-outline" @click="goCreate">Thêm mới</button>
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

              <th class="col-type">Loại phiếu</th>
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
                <span class="pill" :class="isPersonal(v) ? 'pill-personal' : 'pill-public'">
                  {{ isPersonal(v) ? 'Cá nhân' : 'Công khai' }}
                </span>
              </td>

              <td class="col-value text-bold">
                {{ renderGiaTriGiamRow(v) }}
              </td>

              <td class="col-qty">{{ v.soLuong ?? 0 }}</td>

              <td class="col-date">{{ formatDate(v.ngayBatDau) }}</td>
              <td class="col-date">{{ formatDate(v.ngayKetThuc) }}</td>

              <td class="col-status">
                <span :class="['badge', getBadgeClass(v)]">
                  {{ getBizStatusText(v) }}
                </span>
              </td>

              <td class="col-action">
                <div class="action-wrap">
                  <!-- ✅ detail -->
                  <button class="btn-icon" @click="openDetail(v.id)" title="Xem chi tiết">👁️</button>

                  <!-- ✅ edit: disable khi đã OFF hoặc đã kết thúc -->
                  <button
                    class="btn-icon"
                    :class="{ disabled: isEditDisabled(v) }"
                    :title="isEditDisabled(v) ? 'Không thể sửa khi đã tắt/kết thúc' : 'Sửa'"
                    @click="!isEditDisabled(v) && goEdit(v.id)"
                  >
                    ✏️
                  </button>

                  <!-- ✅ SWITCH: thay cho nút kết thúc -->
                  <label class="switch" :title="isEnded(v) ? 'Đã kết thúc' : 'Tắt phiếu (kết thúc ngay)'">
                    <input
                      type="checkbox"
                      :checked="!isEnded(v)"
                      :disabled="isEnded(v)"
                      @change="onToggleEnd(v, $event)"
                    />
                    <span class="slider"></span>
                  </label>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()

function goCreate() {
  router.push('/vouchers/create')
}
function goEdit(id) {
  router.push(`/vouchers/update/${id}`)
}
function openDetail(id) {
  router.push(`/vouchers/${id}`)
}

const API = 'http://localhost:8080/api/pgg'
const getAllPhieuGiamGia = async () => (await axios.get(API)).data

// ===== DateTime helpers (BE trả ISO: 2026-01-18T18:14:00) =====
function toDate(v) {
  if (!v) return null
  const d = new Date(String(v))
  return Number.isNaN(d.getTime()) ? null : d
}

function formatDate(v) {
  const d = toDate(v)
  if (!d) return '-'
  const dd = String(d.getDate()).padStart(2, '0')
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const yyyy = d.getFullYear()
  const hh = String(d.getHours()).padStart(2, '0')
  const mi = String(d.getMinutes()).padStart(2, '0')
  return `${dd}/${mm}/${yyyy} ${hh}:${mi}`
}

function dateFromYMD(ymd, endOfDay = false) {
  if (!ymd) return null
  const d = new Date(`${ymd}T00:00:00`)
  if (endOfDay) d.setHours(23, 59, 59, 999)
  return d
}

// ===== Biz status =====
function getBizStatusText(v) {
  const start = toDate(v.ngayBatDau)
  const end = toDate(v.ngayKetThuc)
  const now = new Date()

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

function isEnded(v) {
  // kết thúc theo nghiệp vụ datetime
  return getBizStatusText(v) === 'Kết thúc'
}

function isEditDisabled(v) {
  // OFF (trangThai false) hoặc đã hết hạn/kết thúc
  return v.trangThai === false || isEnded(v)
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
  const from = filters.value.from || ''
  const to = filters.value.to || ''
  const loai = filters.value.loai
  const now = new Date()

  return (items.value || [])
    .filter(v => v.trangThai === true) // (giữ nguyên logic cũ của bạn)

    .filter(v => {
      if (!kw) return true
      const ma = String(v.maGiamGia ?? '').toLowerCase()
      const ten = String(v.tenGiamGia ?? '').toLowerCase()
      return ma.includes(kw) || ten.includes(kw)
    })

    .filter(v => {
      if (!loai) return true
      if (loai === 'PERCENT') return v.loaiGiam === true
      if (loai === 'MONEY') return v.loaiGiam === false
      return true
    })

    .filter(v => {
      const fromD = dateFromYMD(from, false)
      const toD = dateFromYMD(to, true)
      if (!fromD && !toD) return true

      const start = toDate(v.ngayBatDau)
      const end = toDate(v.ngayKetThuc)

      if (fromD && !start) return false
      if (toD && !end) return false

      if (fromD && start < fromD) return false
      if (toD && end > toD) return false

      return true
    })

    .filter(v => {
      if (!biz) return true
      const start = toDate(v.ngayBatDau)
      const end = toDate(v.ngayKetThuc)

      if (biz === 'UPCOMING') return start && now < start
      if (biz === 'EXPIRED') return end && now > end
      return (!start || now >= start) && (!end || now <= end)
    })
})

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

async function reload() {
  loading.value = true
  error.value = ''
  try {
    const data = await getAllPhieuGiamGia()

    items.value = (Array.isArray(data) ? data : []).map(x => ({
      ...x,
      loaiPhieu: x.loaiPhieu ?? x.loai_phieu ?? 'CONG_KHAI',

      ngayBatDau: x.ngayBatDau ?? x.ngay_bat_dau ?? null,
      ngayKetThuc: x.ngayKetThuc ?? x.ngay_ket_thuc ?? null,
      ngayTao: x.ngayTao ?? x.ngay_tao ?? null,

      maGiamGia: x.maGiamGia ?? x.ma_giam_gia ?? x.ma ?? null,
      tenGiamGia: x.tenGiamGia ?? x.ten_giam_gia ?? null,

      trangThai: x.trangThai ?? x.trang_thai ?? true,

      loaiGiam: x.loaiGiam ?? x.loai_giam ?? true,
      giaTriPhanTram: x.giaTriPhanTram ?? x.gia_tri_phan_tram ?? null,
      giaTriTienMat: x.giaTriTienMat ?? x.gia_tri_tien_mat ?? null
    }))

    currentPage.value = 0
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || 'Không tải được dữ liệu'
  } finally {
    loading.value = false
  }
  currentPage.value = 0
}

/**
 * ✅ SWITCH handler:
 * - Switch ON nghĩa là chưa kết thúc (không làm gì)
 * - Người dùng gạt OFF => gọi /end-pgg/{id}
 * - Vì API 1 chiều, xong sẽ bị "Ended" nên switch sẽ disabled
 */
async function onToggleEnd(v, evt) {
  const checked = evt?.target?.checked === true

  // Nếu user gạt ON (checked=true) thì revert lại (vì backend không có mở lại)
  if (checked) {
    evt.target.checked = !checked
    alert('Backend hiện chỉ hỗ trợ KẾT THÚC (1 chiều), không bật lại được.')
    return
  }

  // gạt OFF => kết thúc
  const ok = confirm('Bạn có chắc muốn kết thúc phiếu giảm giá này ngay?')
  if (!ok) {
    // revert UI
    evt.target.checked = true
    return
  }

  try {
    await axios.put(`${API}/end-pgg/${v.id}`)
    alert('Đã kết thúc phiếu giảm giá')
    await reload()
  } catch (e) {
    // revert UI
    evt.target.checked = true
    alert(e?.response?.data?.message || e?.message || 'Không thể kết thúc')
  }
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

function formatMoney(v) {
  const n = Number(v)
  if (Number.isNaN(n)) return String(v ?? '-')
  return n.toLocaleString('vi-VN') + ' ₫'
}

function renderGiaTriGiamRow(v) {
  if (v?.loaiGiam === true) {
    const pct = Number(v?.giaTriPhanTram ?? v?.gia_tri_phan_tram ?? 0)
    return `${pct}%`
  }
  const money = Number(v?.giaTriTienMat ?? v?.gia_tri_tien_mat ?? 0)
  return formatMoney(money)
}

function isPersonal(v) {
  const lp = v?.loaiPhieu
  if (lp === true) return true
  if (lp === false) return false
  return String(lp || '').toUpperCase() === 'CA_NHAN'
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

.table-card { padding: 0 0 12px; }
.table-header-info { padding: 14px 16px; border-bottom: 1px solid #e5e7eb; }
.table-header-info h3 { margin: 0; font-size: 14px; font-weight: 600; }

.table-responsive { padding: 0 16px 10px; }
.table { width: 100%; border-collapse: collapse; font-size: 13px; }

.thead-dark tr { background: #1f2a44; }
.thead-dark th { color: #fff; font-weight: 600; padding: 10px 10px; text-align: left; }

.table td { padding: 10px 10px; border-bottom: 1px solid #e5e7eb; }

.text-bold { font-weight: 600; }
.col-date { width: 150px; white-space: nowrap; }

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

.btn-icon {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  margin: 0 4px;
}
.btn-icon.disabled {
  opacity: 0.35;
  pointer-events: none;
}

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

/* pill loại phiếu */
.pill {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}
.pill-public { background: #e0f2fe; color: #0369a1; }
.pill-personal { background: #ede9fe; color: #5b21b6; }

/* ===== Switch ===== */
.switch {
  position: relative;
  display: inline-block;
  width: 42px;
  height: 22px;
  vertical-align: middle;
  margin-left: 6px;
}
.switch input { display: none; }
.slider {
  position: absolute;
  inset: 0;
  background-color: #e5e7eb;
  border-radius: 999px;
  cursor: pointer;
  transition: 0.2s;
}
.slider::before {
  content: "";
  position: absolute;
  width: 18px;
  height: 18px;
  left: 2px;
  top: 2px;
  background: white;
  border-radius: 999px;
  transition: 0.2s;
}
.switch input:checked + .slider {
  background-color: #22c55e;
}
.switch input:checked + .slider::before {
  transform: translateX(20px);
}
.switch input:disabled + .slider {
  opacity: 0.55;
  cursor: not-allowed;
}
</style>
