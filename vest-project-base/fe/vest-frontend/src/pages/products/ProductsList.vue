<template>
  <div class="product-page">
    <!-- TOP BAR -->
    <div class="page-top">
      <div class="page-title">
        <h2>Quản lý sản phẩm / Danh sách sản phẩm</h2>
      </div>

      <div class="page-actions">
        <button class="btn btn-outline-secondary btn-sm" type="button" @click="scanQr">
          <i class="bi bi-qr-code-scan me-2"></i> Quét QR
        </button>

        <!-- Excel: bấm vào sẽ bật chế độ chọn -->
        <button
          v-if="!exportMode"
          class="btn btn-outline-primary btn-sm"
          type="button"
          @click="openExportMode"
        >
          <i class="bi bi-download me-2"></i> Tải Excel
        </button>

        <!-- Excel: đang ở chế độ chọn -->
        <template v-else>
          <button
            class="btn btn-primary btn-sm"
            type="button"
            :disabled="selectedIds.length === 0 || exporting"
            @click="exportSelectedToExcel"
          >
            <i class="bi bi-file-earmark-excel me-2"></i>
            {{ exporting ? 'Đang xuất...' : `Xuất Excel (${selectedIds.length})` }}
          </button>

          <button
            class="btn btn-outline-secondary btn-sm"
            type="button"
            :disabled="exporting"
            @click="cancelExportMode"
          >
            <i class="bi bi-x-lg me-2"></i> Hủy
          </button>
        </template>

        <button class="btn btn-outline-secondary btn-sm" type="button" @click="createProduct">
          <i class="bi bi-plus-lg me-2"></i> Thêm mới
        </button>
      </div>
    </div>

    <!-- FILTER CARD -->
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

      <div class="filter-body" v-show="filterOpen">
        <div class="filter-layout">
          <!-- Row 1 -->
          <div class="form-group fg-search">
            <label>Tìm kiếm</label>
            <input
              type="text"
              v-model="filters.keyword"
              placeholder="Tìm kiếm theo tên/mã sản phẩm..."
              class="form-input"
              @keyup.enter="reload"
            />
          </div>

          <div class="form-group fg-brand">
            <label>Thương hiệu</label>
            <select v-model="filters.thuongHieu" class="form-input" @change="onFilterChanged">
              <option value="">-- Chọn Thương hiệu --</option>
              <option v-for="b in attributes.thuongHieu" :key="b.id" :value="String(b.id)">
                {{ b.ten }}
              </option>
            </select>
          </div>

          <div class="form-group fg-qty">
            <label>Số lượng</label>
            <select v-model="filters.soLuong" class="form-input" @change="onFilterChanged">
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
              <div class="slider-range" :style="rangeStyle"></div>

              <input
                type="range"
                min="0"
                :max="priceMaxDb"
                :step="PRICE_STEP"
                v-model.number="filters.priceMin"
                @input="clampMinPrice"
                @change="onFilterChanged"
              />

              <input
                type="range"
                min="0"
                :max="priceMaxDb"
                :step="PRICE_STEP"
                v-model.number="filters.priceMax"
                @input="clampMaxPrice"
                @change="onFilterChanged"
              />
            </div>

            <small class="hint">
              Giá tối đa hiện tại: <b>{{ formatPrice(priceMaxDb) }}</b>
            </small>
          </div>

          <div class="form-group fg-type">
            <label>Loại sản phẩm</label>
            <select v-model="filters.loai" class="form-input" @change="onFilterChanged">
              <option value="">-- Chọn Loại sản phẩm --</option>
              <option v-for="t in attributes.loaiSanPham" :key="t.id" :value="String(t.id)">
                {{ t.ten }}
              </option>
            </select>
          </div>

          <div class="form-group fg-status">
            <label>Trạng thái </label>
            <div class="radio-group compact">
              <label>
                <input type="radio" value="" v-model="filters.status" @change="onFilterChanged" />
                Tất cả
              </label>
              <label>
                <input type="radio" value="true" v-model="filters.status" @change="onFilterChanged" />
                Còn hàng
              </label>
              <label>
                <input type="radio" value="false" v-model="filters.status" @change="onFilterChanged" />
                Hết hàng
              </label>
            </div>
          </div>

          <div class="fg-reset">
            <button class="btn-reset" type="button" @click.stop="resetFilters" title="Reset">
              <i class="bi bi-arrow-counterclockwise"></i>
              <span>Đặt lại</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- TABLE -->
    <div class="card table-card">
      <div class="table-header-info">
        <h3>Tổng số sản phẩm {{ totalElements }}</h3>
        <div v-if="exportMode" class="export-hint">
          Đang chọn để xuất Excel — đã chọn: <b>{{ selectedIds.length }}</b>
        </div>
      </div>

      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr>
              <th v-if="exportMode" class="text-center col-check">
                <input
                  type="checkbox"
                  :disabled="items.length === 0"
                  :checked="allVisibleSelected"
                  @change="toggleSelectAllVisible($event.target.checked)"
                  title="Chọn tất cả trang hiện tại"
                />
              </th>

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
              <td v-if="exportMode" class="text-center col-check">
                <input
                  type="checkbox"
                  :checked="isSelected(p.id)"
                  @change="toggleSelect(p, $event.target.checked)"
                />
              </td>

              <td class="text-center">{{ currentPage * pageSize + index + 1 }}</td>
              <td class="text-center"><b>{{ p.maSanPham }}</b></td>
              <td class="text-bold">{{ p.tenSanPham }}</td>
              <td class="text-center">{{ p.tenLoaiSanPham || '-' }}</td>
              <td class="text-center">{{ p.tenThuongHieu || '-' }}</td>
              <td class="text-center">{{ Number(p.soLuongTon ?? 0) }}</td>
              <td class="text-center">{{ formatPriceRange(p.giaMin, p.giaMax) }}</td>

              <!-- ✅ trạng thái theo tồn kho -->
              <td class="text-center">
                <span :class="['badge', badgeClassByStock(p)]">
                  {{ stockText(p) }}
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
              <td :colspan="tableColspan" class="text-center">Đang tải dữ liệu...</td>
            </tr>
            <tr v-else-if="items.length === 0">
              <td :colspan="tableColspan" class="text-center">Không tìm thấy sản phẩm nào</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="paging-bar" v-if="totalPages > 0">
        <div class="paging-left">Hiển thị {{ items.length }} / tổng {{ totalElements }} bản ghi</div>

        <div class="paging-center">
          <button
            class="btn btn-outline-secondary btn-sm"
            :disabled="currentPage === 0"
            @click="changePage(currentPage - 1)"
          >
            <i class="bi bi-chevron-left"></i>
          </button>

          <div class="input-group input-group-sm paging-page">
            <span class="input-group-text">Trang</span>
            <input
              type="number"
              min="1"
              :max="Math.max(totalPages, 1)"
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
import * as XLSX from 'xlsx'

import attributeService from '../../services/attributeService'
import { listSanPham, getGiaMaxDb } from '../../services/sanPhamApi'

const router = useRouter()

/** UI */
const filterOpen = ref(true)
const toggleFilter = () => (filterOpen.value = !filterOpen.value)

/** Pagination */
const currentPage = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const pageSize = ref(10)
const pageInput = ref(1)

/** Data */
const items = ref([])
const loading = ref(false)
const error = ref('')

/** Attributes */
const attributes = reactive({
  loaiSanPham: [],
  thuongHieu: []
})

/** Price */
const PRICE_STEP = 100000
const DEFAULT_MAX = 10000000
const priceMaxDb = ref(DEFAULT_MAX)

const filters = reactive({
  keyword: '',
  thuongHieu: '',
  soLuong: '',
  loai: '',
  status: '', // "" | "true" | "false"  (theo tồn kho)
  priceMin: 0,
  priceMax: DEFAULT_MAX
})

/** ✅ Helpers: tồn kho */
function isInStock(p) {
  return Number(p?.soLuongTon ?? 0) > 0
}
function stockText(p) {
  return isInStock(p) ? 'Còn hàng' : 'Hết hàng'
}
function badgeClassByStock(p) {
  return isInStock(p) ? 'badge-success' : 'badge-danger'
}

/** Export Mode (FE only) */
const exportMode = ref(false)
const exporting = ref(false)
const selectedIds = ref([])       // [id1, id2,...]
const selectedRows = reactive({}) // { [id]: productObject }

function openExportMode() {
  exportMode.value = true
}
function cancelExportMode() {
  exportMode.value = false
  selectedIds.value = []
  Object.keys(selectedRows).forEach((k) => delete selectedRows[k])
}

const tableColspan = computed(() => (exportMode.value ? 10 : 9))

function isSelected(id) {
  return selectedIds.value.includes(id)
}
function toggleSelect(row, checked) {
  const id = row.id
  if (checked) {
    if (!selectedIds.value.includes(id)) selectedIds.value.push(id)
    selectedRows[id] = { ...row } // lưu row để export kể cả khi chuyển trang
  } else {
    selectedIds.value = selectedIds.value.filter((x) => x !== id)
    delete selectedRows[id]
  }
}
const allVisibleSelected = computed(() => {
  if (!exportMode.value || items.value.length === 0) return false
  return items.value.every((p) => selectedIds.value.includes(p.id))
})
function toggleSelectAllVisible(checked) {
  items.value.forEach((p) => toggleSelect(p, checked))
}

/** Actions */
const createProduct = () => router.push('/products/add')
const goDetail = (id) => router.push(`/products/${id}`)
const scanQr = () => console.log('scan qr')

/** Price helpers */
function formatPrice(val) {
  const v = Number(val || 0)
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v)
}
function formatPriceRange(min, max) {
  const mn = Number(min || 0)
  const mx = Number(max ?? mn)
  return mn === mx ? formatPrice(mn) : `${formatPrice(mn)} - ${formatPrice(mx)}`
}
function roundUpToStep(n, step) {
  const x = Number(n || 0)
  if (x <= 0) return 0
  return Math.ceil(x / step) * step
}
function clampMinPrice() {
  if (filters.priceMin < 0) filters.priceMin = 0
  if (filters.priceMin > filters.priceMax) filters.priceMin = filters.priceMax
}
function clampMaxPrice() {
  const max = Number(priceMaxDb.value || 0)
  if (filters.priceMax > max) filters.priceMax = max
  if (filters.priceMax < filters.priceMin) filters.priceMax = filters.priceMin
}
const rangeStyle = computed(() => {
  const max = Number(priceMaxDb.value || 1)
  const minV = Math.max(0, Math.min(filters.priceMin, max))
  const maxV = Math.max(0, Math.min(filters.priceMax, max))
  const left = (minV / max) * 100
  const width = ((maxV - minV) / max) * 100
  return { left: left + '%', width: width + '%' }
})

/** Load attributes */
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

/** Load price max from DB */
async function loadPriceMaxDb() {
  try {
    const raw = await getGiaMaxDb()
    const maxDb = roundUpToStep(Number(raw || 0), PRICE_STEP) || DEFAULT_MAX
    priceMaxDb.value = maxDb

    if (!filters.priceMax || filters.priceMax === DEFAULT_MAX || filters.priceMax > maxDb) {
      filters.priceMax = maxDb
    }
    if (filters.priceMin > filters.priceMax) filters.priceMin = filters.priceMax
  } catch (e) {
    console.error(e)
    priceMaxDb.value = DEFAULT_MAX
    if (!filters.priceMax) filters.priceMax = DEFAULT_MAX
  }
}

/** Filter change */
function onFilterChanged() {
  currentPage.value = 0
  pageInput.value = 1
  reload()
}

/** Reset */
function resetFilters() {
  filters.keyword = ''
  filters.thuongHieu = ''
  filters.soLuong = ''
  filters.loai = ''
  filters.status = ''
  filters.priceMin = 0
  filters.priceMax = priceMaxDb.value
  currentPage.value = 0
  pageInput.value = 1
  reload()
}

/** Pagination */
function changePage(p) {
  if (p < 0 || p >= totalPages.value) return
  currentPage.value = p
  pageInput.value = p + 1
  reload()
}
function jumpPage() {
  const max = Math.max(totalPages.value, 1)
  const target = Math.min(Math.max(1, pageInput.value || 1), max)
  changePage(target - 1)
}
function onChangeSize() {
  currentPage.value = 0
  pageInput.value = 1
  reload()
}

/** Core load */
async function reload() {
  loading.value = true
  error.value = ''

  try {
    const resp = await listSanPham(currentPage.value, pageSize.value)
    const page = resp?.data ?? resp

    const content = page?.content || []
    totalPages.value = page?.totalPages || 0
    totalElements.value = page?.totalElements || 0
    pageInput.value = currentPage.value + 1

    const kw = (filters.keyword || '').trim().toLowerCase()

    items.value = content.filter((it) => {
      const ten = (it.tenSanPham || '').toLowerCase()
      const ma = (it.maSanPham || '').toLowerCase()

      const matchesKeyword = !kw || ten.includes(kw) || ma.includes(kw)

      // ✅ Trạng thái theo tồn kho (0 => hết hàng)
      const matchesStatus =
        filters.status === '' || String(isInStock(it)) === String(filters.status)

      const matchesLoai = !filters.loai || String(it.loaiSanPhamId) === String(filters.loai)
      const matchesBrand = !filters.thuongHieu || String(it.thuongHieuId) === String(filters.thuongHieu)

      let matchesQty = true
      if (filters.soLuong) {
        const sl = Number(it.soLuongTon || 0)
        if (filters.soLuong === '1') matchesQty = sl < 10
        else if (filters.soLuong === '2') matchesQty = sl >= 10 && sl <= 100
        else if (filters.soLuong === '3') matchesQty = sl > 100
      }

      const pMin = Number(it.giaMin || 0)
      const pMax = Number(it.giaMax ?? pMin)
      const fMin = Number(filters.priceMin || 0)
      const fMax = Number(filters.priceMax || 0)
      const matchesPrice = pMax >= fMin && pMin <= fMax

      return matchesKeyword && matchesStatus && matchesLoai && matchesBrand && matchesQty && matchesPrice
    })
  } catch (e) {
    console.error(e)
    error.value = 'Không gọi được API. Vui lòng kiểm tra Backend.'
    items.value = []
    totalPages.value = 0
    totalElements.value = 0
  } finally {
    loading.value = false
  }
}

/** Export FE: mỗi sản phẩm 1 file excel */
function toExcelRow(p) {
  const giaMin = Number(p.giaMin ?? 0)
  const giaMax = Number(p.giaMax ?? p.giaMin ?? 0)

  return {
    'Mã sản phẩm': p.maSanPham ?? '',
    'Tên sản phẩm': p.tenSanPham ?? '',
    'Loại sản phẩm': p.tenLoaiSanPham ?? '',
    'Thương hiệu': p.tenThuongHieu ?? '',
    'Hàng tồn': Number(p.soLuongTon ?? 0),
    'Giá min': giaMin,
    'Giá max': giaMax,
    // ✅ export theo tồn kho
    'Trạng thái': isInStock(p) ? 'Còn hàng' : 'Hết hàng'
  }
}

function sleep(ms) {
  return new Promise((r) => setTimeout(r, ms))
}

async function exportSelectedToExcel() {
  if (selectedIds.value.length === 0) return

  exporting.value = true
  error.value = ''

  try {
    for (const id of selectedIds.value) {
      const p = selectedRows[id]
      if (!p) continue

      const ws = XLSX.utils.json_to_sheet([toExcelRow(p)])
      const wb = XLSX.utils.book_new()
      XLSX.utils.book_append_sheet(wb, ws, 'SanPham')

      const arrayBuffer = XLSX.write(wb, { bookType: 'xlsx', type: 'array' })
      const blob = new Blob([arrayBuffer], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      })

      const safeCode = String(p.maSanPham ?? id).replace(/[^\w\-]+/g, '_')
      const safeName = String(p.tenSanPham ?? '').slice(0, 40).replace(/[^\w\-]+/g, '_')
      const filename = `san-pham_${safeCode}${safeName ? '_' + safeName : ''}.xlsx`

      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = filename
      document.body.appendChild(a)
      a.click()
      a.remove()
      URL.revokeObjectURL(url)

      await sleep(120)
    }

    cancelExportMode()
  } catch (e) {
    console.error(e)
    error.value = 'Xuất Excel thất bại (có thể bị chặn tải nhiều file).'
  } finally {
    exporting.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadAttributes(), loadPriceMaxDb()])
  clampMaxPrice()
  reload()
})
</script>

<style scoped>
.product-page {
  padding: 20px;
  background-color: #ffffff;
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
.filter-topbar-left { display: inline-flex; align-items: center; gap: 10px; }
.filter-title { font-size: 0.95rem; font-weight: 600; }
.filter-hint { font-size: 0.8rem; opacity: 0.9; }
.filter-caret {
  width: 22px; height: 22px; border-radius: 6px;
  background: rgba(255,255,255,.12);
  display: inline-flex; align-items: center; justify-content: center;
  transition: transform .15s ease;
}
.filter-caret.open { transform: rotate(180deg); }

/* filter layout */
.filter-body { padding: 16px; }
.filter-layout {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 16px;
  align-items: start;
}
.fg-search { grid-column: 1 / 2; grid-row: 1; }
.fg-brand  { grid-column: 2 / 3; grid-row: 1; }
.fg-qty    { grid-column: 3 / 4; grid-row: 1; }
.fg-price  { grid-column: 1 / 2; grid-row: 2; }
.fg-type   { grid-column: 2 / 3; grid-row: 2; }
.fg-status { grid-column: 3 / 4; grid-row: 2; }
.fg-reset  {
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

/* radio */
.radio-group { display: flex; gap: 12px; margin-top: 8px; flex-wrap: wrap; }
.radio-group.compact label { font-size: 0.85rem; }
.radio-group label { display: inline-flex; align-items: center; gap: 6px; cursor: pointer; font-weight: normal; }

/* reset */
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
.hint { display: block; margin-top: 6px; color: #6b7280; }

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

/* table */
.table-card { margin-top: 14px; padding: 16px; }
.table-header-info { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.table-header-info h3 { font-size: 1rem; font-weight: 700; color: #111827; margin: 0; }
.export-hint { color: #6b7280; font-size: 0.875rem; }

.table { width: 100%; border-collapse: separate; border-spacing: 0; }
.table th { background-color: #1e293b; color: #fff; padding: 12px; text-align: left; font-size: 0.875rem; font-weight: 600; }
.table td { padding: 12px; border-bottom: 1px solid #e5e7eb; color: #4b5563; font-size: 0.875rem; }

.col-check { width: 46px; min-width: 46px; }

.text-bold { font-weight: 600; color: #1f2937; text-align: center; }
.text-center { text-align: center; }

/* badges */
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
