<template>
  <div class="product-page">
    <!-- TOP BAR: tiêu đề trái + 3 nút phải (giống hóa đơn) -->
    <div class="page-top">
      <div class="page-title">
        <h2>Quản lý sản phẩm / Danh sách sản phẩm</h2>
      </div>

      <div class="page-actions">
        <button class="btn btn-outline-secondary btn-sm" type="button" @click="scanQr">
          <i class="bi bi-qr-code-scan me-2"></i> Quét QR
        </button>

        <button class="btn btn-outline-primary btn-sm" type="button" @click="exportExcel">
          <i class="bi bi-download me-2"></i> Tải Excel
        </button>

        <button class="btn btn-outline-secondary btn-sm" type="button" @click="createProduct">
          <i class="bi bi-plus-lg me-2"></i> Thêm mới
        </button>
      </div>
    </div>

    <!-- FILTER CARD (đóng/mở như hóa đơn) -->
    <div class="card filter-card">
      <div class="filter-topbar" @click="toggleFilter">
        <div class="filter-topbar-left">
          <span class="filter-caret" :class="{ open: filterOpen }">
            <i class="bi bi-caret-down-fill"></i>
          </span>
          <span class="filter-title">Bộ lọc tìm kiếm</span>
        </div>
        <div class="filter-hint">Nhấn để thu gọn/mở rộng</div>
      </div>

      <!-- Layout đúng yêu cầu:
           - Khoảng giá nằm dưới Tìm kiếm
           - Trạng thái xuống dưới
           - Số lượng lên trên (cùng hàng với Thương hiệu)
           Hàng 1: Tìm kiếm | Thương hiệu | Số lượng
           Hàng 2: Khoảng giá | Loại sản phẩm | Trạng thái
      -->
      <div class="filter-body" v-show="filterOpen">
        <div class="filter-layout">
          <!-- Row 1 -->
          <div class="form-group fg-search">
            <label>Tìm kiếm</label>
            <input
              type="text"
              v-model="filters.keyword"
              placeholder="Tìm kiếm theo tên sản phẩm..."
              class="form-input"
              @keyup.enter="reload"
            />
          </div>

          <div class="form-group fg-brand">
            <label>Thương hiệu</label>
            <select v-model="filters.thuongHieu" class="form-input" @change="reload">
              <option value="">-- Chọn Thương hiệu --</option>
              <option v-for="item in attributes.thuongHieu" :key="item.id" :value="item.id">
                {{ item.ten }}
              </option>
            </select>
          </div>

          <div class="form-group fg-qty">
            <label>Số lượng</label>
            <select v-model="filters.soLuong" class="form-input" @change="reload">
              <option value="">-- Chọn Số lượng --</option>
              <option value="1">Dưới 10</option>
              <option value="2">10 - 100</option>
              <option value="3">Trên 100</option>
            </select>
          </div>

          <!-- Row 2 -->
          <div class="form-group fg-price price-range-group">
            <label>
              Khoảng giá:
              <span class="price-display">
                {{ formatPrice(filters.priceMin) }} - {{ formatPrice(filters.priceMax) }}
              </span>
            </label>

            <div class="range-slider">
              <div class="slider-track"></div>
              <div class="slider-range" :style="rangeStyle"></div>

              <input
                type="range"
                min="0"
                :max="priceMaxDb"
                step="100000"
                v-model.number="filters.priceMin"
                @input="validateMinPrice"
                @change="reload"
              />

              <input
                type="range"
                min="0"
                :max="priceMaxDb"
                step="100000"
                v-model.number="filters.priceMax"
                @input="validateMaxPrice"
                @change="reload"
              />
            </div>
          </div>

          <div class="form-group fg-type">
            <label>Loại sản phẩm</label>
            <select v-model="filters.loai" class="form-input" @change="reload">
              <option value="">-- Chọn Loại sản phẩm --</option>
              <option v-for="item in attributes.loaiSanPham" :key="item.id" :value="item.id">
                {{ item.ten }}
              </option>
            </select>
          </div>

          <div class="form-group fg-status">
            <label>Trạng thái</label>
            <div class="radio-group compact">
              <label><input type="radio" value="" v-model="filters.status" @change="reload" /> Tất cả</label>
              <label><input type="radio" value="true" v-model="filters.status" @change="reload" /> Còn hàng</label>
              <label><input type="radio" value="false" v-model="filters.status" @change="reload" /> Hết hàng</label>
            </div>
          </div>

          <!-- Reset góc phải dưới (giống hóa đơn) -->
          <div class="fg-reset">
            <button class="btn-reset" type="button" @click.stop="resetFilters" title="Reset">
              <i class="bi bi-arrow-counterclockwise"></i>
              <span>Reset</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- TABLE -->
    <div class="card table-card">
      <div class="table-header-info">
        <h3>Tổng số sản phẩm {{ totalElements }}</h3>
      </div>

      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr>
              <th class="text-center">STT</th>
              <th class="text-center">Mã sản phẩm</th>
              <th class="text-center">Tên sản phẩm</th>
              <th class="text-center">Loại sản phẩm</th>
              <th class="text-center">Thương hiệu</th>
              <th class="text-center">Hàng tồn</th>
              <th class="text-center">Khoảng giá</th>
              <th class="text-center">Trạng thái</th>
              <th class="text-center">Hành động</th>
            </tr>
          </thead>

          <tbody>
            <tr v-for="(p, index) in items" :key="p.id">
              <td class="text-center">{{ (currentPage * pageSize) + index + 1 }}</td>
              <td class="text-center">{{ p.maSanPham }}</td>
              <td class="text-bold">{{ p.tenSanPham }}</td>
              <td class="text-center">{{ p.tenLoaiSanPham || '-' }}</td>
              <td class="text-center">{{ p.tenThuongHieu || '-' }}</td>
              <td class="text-center">{{ p.soLuongTon }}</td>
              <td class="text-center">{{ formatPriceRange(p.giaMin, p.giaMax) }}</td>
              <td class="text-center">
                <span :class="['badge', p.trangThai ? 'badge-success' : 'badge-danger']">
                  {{ p.trangThai ? 'Còn hàng' : 'Hết hàng' }}
                </span>
              </td>
              <td class="text-center">
                <button
                  type="button"
                  class="btn btn-outline-primary btn-sm action-btn"
                  @click="goDetail(p.id)"
                  title="Chi tiết"
                >
                  <i class="bi bi-eye"></i>
                </button>
              </td>
            </tr>

            <tr v-if="loading">
              <td colspan="9" class="text-center">Đang tải dữ liệu...</td>
            </tr>
            <tr v-if="!loading && items.length === 0">
              <td colspan="9" class="text-center">Không tìm thấy sản phẩm nào</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="paging-bar" v-if="totalPages > 0">
        <div class="paging-left">Hiển thị {{ items.length }} / tổng {{ totalElements }} bản ghi</div>

        <div class="paging-center">
          <button class="btn btn-outline-secondary btn-sm" :disabled="currentPage === 0" @click="changePage(currentPage - 1)">
            <i class="bi bi-chevron-left"></i>
          </button>

          <div class="input-group input-group-sm paging-page">
            <span class="input-group-text">Trang</span>
            <input
              type="number"
              min="1"
              :max="totalPages || 1"
              class="form-control"
              v-model.number="pageInput"
              @keyup.enter="jumpPage"
            />
          </div>

          <button
            class="btn btn-outline-secondary btn-sm"
            :disabled="currentPage >= totalPages - 1"
            @click="changePage(currentPage + 1)"
          >
            <i class="bi bi-chevron-right"></i>
          </button>
        </div>

        <div class="paging-right">
          <select class="form-select form-select-sm paging-size" v-model.number="pageSize" @change="onChangeSize">
            <option :value="10">10 bản ghi / trang</option>
            <option :value="20">20 bản ghi / trang</option>
            <option :value="50">50 bản ghi / trang</option>
          </select>
        </div>
      </div>

      <p v-if="error" class="error-msg">{{ error }}</p>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { listSanPham } from '../../services/sanPhamApi'
import attributeService from '../../services/attributeService'

const router = useRouter()

// collapse
const filterOpen = ref(true)
function toggleFilter() {
  filterOpen.value = !filterOpen.value
}

// data
const items = ref([])
const loading = ref(false)
const error = ref('')

// pagination
const currentPage = ref(0)
const totalPages = ref(0)
const pageSize = ref(10)
const totalElements = ref(0)
const pageInput = ref(1)

// price 0 -> 10.000.000
const priceMaxDb = ref(10000000)

const filters = reactive({
  keyword: '',
  thuongHieu: '',
  soLuong: '',
  loai: '',
  status: '',
  priceMin: 0,
  priceMax: 10000000
})

const attributes = reactive({
  loaiSanPham: [],
  thuongHieu: []
})

function exportExcel() { console.log('export excel') }
function scanQr() { console.log('scan qr') }
function createProduct() { router.push('/products/add') }
function goDetail(id) { router.push(`/products/${id}`) }

function formatPrice(val) {
  const v = Number(val || 0)
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v)
}
function formatPriceRange(min, max) {
  const mn = Number(min || 0)
  const mx = Number(max ?? mn)
  if (mn === mx) return formatPrice(mn)
  return `${formatPrice(mn)} - ${formatPrice(mx)}`
}

const rangeStyle = computed(() => {
  const min = filters.priceMin
  const max = filters.priceMax
  const rangeMax = priceMaxDb.value || 1
  const pMin = (min / rangeMax) * 100
  const pMax = (max / rangeMax) * 100
  return { left: pMin + '%', width: (pMax - pMin) + '%' }
})

function validateMinPrice() {
  if (filters.priceMin < 0) filters.priceMin = 0
  if (filters.priceMin > filters.priceMax) filters.priceMin = filters.priceMax
}
function validateMaxPrice() {
  if (filters.priceMax > priceMaxDb.value) filters.priceMax = priceMaxDb.value
  if (filters.priceMax < filters.priceMin) filters.priceMax = filters.priceMin
}

function resetFilters() {
  filters.keyword = ''
  filters.thuongHieu = ''
  filters.soLuong = ''
  filters.loai = ''
  filters.status = ''
  filters.priceMin = 0
  filters.priceMax = priceMaxDb.value
  currentPage.value = 0
  reload()
}

function changePage(p) {
  if (p >= 0 && p < totalPages.value) {
    currentPage.value = p
    reload()
  }
}
function jumpPage() {
  const max = Math.max(1, totalPages.value || 1)
  const target = Math.min(Math.max(1, pageInput.value || 1), max)
  changePage(target - 1)
}
function onChangeSize() {
  currentPage.value = 0
  reload()
}

async function loadAttributes() {
  try {
    const [resLoai, resTH] = await Promise.all([
      attributeService.getAllList('loai-san-pham'),
      attributeService.getAllList('thuong-hieu')
    ])
    attributes.loaiSanPham = resLoai?.data || []
    attributes.thuongHieu = resTH?.data || []
  } catch (e) {
    console.error(e)
  }
}

async function reload() {
  loading.value = true
  error.value = ''
  try {
    const raw = await listSanPham(currentPage.value, pageSize.value)
    const page = raw?.data ?? raw

    const serverItems = page?.content || []
    totalPages.value = page?.totalPages || 0
    totalElements.value = page?.totalElements || 0
    pageInput.value = currentPage.value + 1

    const kw = (filters.keyword || '').trim().toLowerCase()

    items.value = serverItems.filter(item => {
      const ten = (item.tenSanPham || '').toLowerCase()
      const ma = (item.maSanPham || '').toLowerCase()

      const matchesKeyword = !kw || ten.includes(kw) || ma.includes(kw)
      const matchesStatus = filters.status === '' || String(!!item.trangThai) === filters.status
      const matchesLoai = !filters.loai || String(item.loaiSanPhamId) === String(filters.loai)
      const matchesThuongHieu = !filters.thuongHieu || String(item.thuongHieuId) === String(filters.thuongHieu)

      let matchesSoLuong = true
      if (filters.soLuong) {
        const sl = Number(item.soLuongTon || 0)
        if (filters.soLuong === '1') matchesSoLuong = sl < 10
        else if (filters.soLuong === '2') matchesSoLuong = sl >= 10 && sl <= 100
        else if (filters.soLuong === '3') matchesSoLuong = sl > 100
      }

      const pMin = Number(item.giaMin || 0)
      const pMax = Number(item.giaMax ?? pMin)
      const fMin = Number(filters.priceMin || 0)
      const fMax = Number(filters.priceMax || 0)
      const matchesGia = pMax >= fMin && pMin <= fMax

      return matchesKeyword && matchesStatus && matchesLoai && matchesThuongHieu && matchesSoLuong && matchesGia
    })
  } catch (e) {
    console.error(e)
    error.value = 'Không gọi được API. Vui lòng kiểm tra Backend.'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadAttributes()
  filters.priceMax = priceMaxDb.value
  reload()
})
</script>

<style scoped>
.product-page {
  padding: 20px;
  background-color: #f3f4f6;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* top */
.page-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}
.page-title h2 {
  font-size: 1.25rem;
  color: #374151;
  margin: 0;
  font-weight: 600;
}
.page-actions {
  display: inline-flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

/* card */
.card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,.1);
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

/* filter topbar */
.filter-topbar {
  background: #1e293b;
  color: #fff;
  padding: 12px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  user-select: none;
}
.filter-topbar-left {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}
.filter-title { font-size: 0.95rem; font-weight: 600; }
.filter-hint { font-size: 0.8rem; opacity: 0.9; }
.filter-caret {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  background: rgba(255,255,255,.12);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: transform .15s ease;
}
.filter-caret.open { transform: rotate(180deg); }

/* filter layout: 3 cột như ảnh */
.filter-body { padding: 16px; }
.filter-layout {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 16px 16px;
  align-items: start;
}

/* row 1 */
.fg-search { grid-column: 1 / 2; grid-row: 1; }
.fg-brand  { grid-column: 2 / 3; grid-row: 1; }
.fg-qty    { grid-column: 3 / 4; grid-row: 1; }

/* row 2 */
.fg-price  { grid-column: 1 / 2; grid-row: 2; }
.fg-type   { grid-column: 2 / 3; grid-row: 2; }
.fg-status { grid-column: 3 / 4; grid-row: 2; }

/* reset bottom right */
.fg-reset {
  grid-column: 3 / 4;
  grid-row: 3;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: -6px;
}

.form-group label {
  display: block;
  font-size: 0.875rem;
  color: #374151;
  margin-bottom: 6px;
  font-weight: 500;
}
.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.875rem;
  outline: none;
}
.form-input:focus { border-color: #3b82f6; }

/* radio compact */
.radio-group {
  display: flex;
  gap: 12px;
  margin-top: 8px;
  flex-wrap: wrap;
}
.radio-group.compact label { font-size: 0.85rem; }
.radio-group label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-weight: normal;
}

/* reset button */
.btn-reset {
  border: none;
  background: transparent;
  color: #111827;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  border-radius: 8px;
  font-weight: 600;
}
.btn-reset:hover { background: #f3f4f6; }

/* slider */
.price-display { font-weight: 700; color: #059669; margin-left: 6px; }
.range-slider {
  position: relative;
  width: 100%;
  height: 4px;
  margin-top: 14px;
  background: #e5e7eb;
  border-radius: 2px;
}
.slider-range {
  position: absolute;
  height: 100%;
  background-color: #059669;
  border-radius: 2px;
  z-index: 1;
}
.range-slider input[type="range"] {
  position: absolute;
  width: 100%;
  pointer-events: none;
  appearance: none;
  -webkit-appearance: none;
  z-index: 2;
  height: 5px;
  top: -1px;
  background: transparent;
  left: 0;
}
.range-slider input[type="range"]::-webkit-slider-thumb {
  pointer-events: all;
  width: 18px;
  height: 18px;
  -webkit-appearance: none;
  background-color: #fff;
  border: 2px solid #059669;
  border-radius: 50%;
  cursor: pointer;
  margin-top: -7px;
  box-shadow: 0 1px 3px rgba(0,0,0,.2);
}
.range-slider input[type="range"]::-webkit-slider-runnable-track {
  width: 100%;
  height: 4px;
  cursor: pointer;
  background: transparent;
  border-radius: 2px;
}

/* table (giữ style) */
.table-card { margin-top: 14px; padding: 16px; }
.table-header-info h3 { font-size: 1rem; font-weight: 700; color: #111827; margin: 0 0 10px 0; }
.table { width: 100%; border-collapse: separate; border-spacing: 0; }
.table th { background-color: #1e293b; color: #fff; padding: 12px; text-align: left; font-size: 0.875rem; font-weight: 600; }
.table td { padding: 12px; border-bottom: 1px solid #e5e7eb; color: #4b5563; font-size: 0.875rem; }
.text-bold { font-weight: 600; color: #1f2937;text-align: center; }
.text-center { text-align: center; }
.badge { padding: 4px 10px; border-radius: 9999px; font-size: 0.75rem; font-weight: 600; }
.badge-success { background-color: #d1fae5; color: #047857; }
.badge-danger { background-color: #fee2e2; color: #b91c1c; }
.action-btn { width: 34px; height: 34px; padding: 0 !important; display: inline-flex; align-items: center; justify-content: center; }

/* pagination */
.paging-bar {
  margin-top: 18px;
  display: grid !important;
  grid-template-columns: 1fr auto 1fr !important;
  align-items: center !important;
  gap: 12px !important;
}
.paging-left { justify-self: start !important; color: #6b7280; font-size: 0.875rem; }
.paging-center { justify-self: center !important; display: inline-flex !important; align-items: center !important; gap: 10px !important; flex-wrap: nowrap !important; }
.paging-right { justify-self: end !important; }
.paging-page { width: 120px !important; }
.paging-size { width: 170px !important; }
.error-msg { color: #ef4444; margin-top: 10px; text-align: center; }

@media (max-width: 1100px) {
  .page-top { flex-direction: column; align-items: flex-start; }
  .filter-layout { grid-template-columns: 1fr; }
  .fg-search, .fg-brand, .fg-qty, .fg-price, .fg-type, .fg-status, .fg-reset { grid-column: 1 / -1; }
  .fg-reset { justify-content: flex-end; margin-top: 0; }
}
</style>
