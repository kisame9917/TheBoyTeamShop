<template>
  <div class="variant-page">
    <!-- Header -->
    <div class="page-header">
      <div class="page-title">
        <h2>Quản lý sản phẩm / Danh sách biến thể</h2>
      </div>

      <div class="page-actions">
        <button class="btn btn-outline-secondary btn-sm" type="button" @click="showQrModal = true">
          <i class="bi bi-qr-code-scan me-1"></i> Quét QR
        </button>

        <!-- Excel -->
        <button v-if="!exportMode" class="btn btn-outline-primary btn-sm" type="button" @click="openExportMode">
          <i class="bi bi-download me-1"></i> Tải Excel
        </button>

        <template v-else>
          <button
            class="btn btn-primary btn-sm"
            type="button"
            :disabled="selectedIds.length === 0 || exporting"
            @click="exportSelectedToExcel"
          >
            <i class="bi bi-file-earmark-excel me-1"></i>
            {{ exporting ? 'Đang xuất...' : `Xuất Excel (${selectedIds.length})` }}
          </button>

          <button class="btn btn-outline-secondary btn-sm" type="button" :disabled="exporting" @click="cancelExportMode">
            <i class="bi bi-x-lg me-1"></i> Hủy
          </button>
        </template>

        <button class="btn btn-primary btn-sm" type="button" @click="resetFilters">
          <i class="bi bi-list-check me-1"></i> Hiển thị đầy đủ biến thể
        </button>

        <button class="btn btn-secondary btn-sm" type="button" @click="goBack">
          <i class="bi bi-arrow-left me-1"></i> Quay lại
        </button>
      </div>
    </div>

    <!-- Filter -->
    <div class="card filter-card">
      <div class="filter-head" @click="isFilterOpen = !isFilterOpen">
        <div class="filter-head-left">
          <i class="bi" :class="isFilterOpen ? 'bi-caret-down-fill' : 'bi-caret-right-fill'"></i>
          <span>Bộ lọc tìm kiếm</span>
        </div>
        <div class="filter-head-right">Nhấn để thu gọn/mở rộng</div>
      </div>

      <div v-show="isFilterOpen" class="filter-body">
        <div class="row g-3 align-items-end">
          <!-- keyword -->
          <div class="col-12 col-lg-6">
            <label class="form-label">Tìm kiếm</label>
            <input
              v-model="filters.keyword"
              type="text"
              class="form-control"
              placeholder="Tìm theo mã, màu, kích cỡ..."
              @input="onKeywordInput"
              @keyup.enter="reload"
            />
            <small v-if="filterErrors.keyword" class="text-danger d-block mt-1">
              {{ filterErrors.keyword }}
            </small>
          </div>

          <!-- color -->
          <div class="col-12 col-lg-3">
            <label class="form-label">Màu sắc</label>
            <select v-model="filters.color" class="form-select" @change="applyFilters">
              <option value="">-- Chọn Màu sắc --</option>
              <option v-for="c in attributes.mauSac" :key="c.id" :value="String(c.id)">
                {{ c.ten }}
              </option>
            </select>
          </div>

          <!-- stock -->
          <div class="col-12 col-lg-3">
            <label class="form-label">Số lượng tồn</label>
            <select v-model="filters.stockRange" class="form-select" @change="applyFilters">
              <option value="">-- Chọn Số lượng tồn --</option>
              <option value="0">= 0</option>
              <option value="1-10">1 - 10</option>
              <option value="11-50">11 - 50</option>
              <option value="51-200">51 - 200</option>
              <option value="200+">Trên 200</option>
            </select>
          </div>

          <!-- price -->
          <div class="col-12 col-lg-6">
            <div class="price-label">
              Khoảng giá:
              <span class="price-green">
                {{ isPriceReady ? `${formatCurrency(filters.priceMin)} - ${formatCurrency(filters.priceMax)}` : 'Đang tải...' }}
              </span>
            </div>

            <div class="double-range">
              <div class="double-range__track"></div>
              <div class="double-range__range" :style="rangeStyle"></div>

              <input
                class="double-range__thumb double-range__thumb--left"
                type="range"
                :min="PRICE_MIN"
                :max="priceMaxDbSafe"
                :step="PRICE_STEP"
                v-model.number="filters.priceMin"
                :disabled="!isPriceReady"
                @input="onPriceInput('min')"
                @change="applyFilters"
              />

              <input
                class="double-range__thumb double-range__thumb--right"
                type="range"
                :min="PRICE_MIN"
                :max="priceMaxDbSafe"
                :step="PRICE_STEP"
                v-model.number="filters.priceMax"
                :disabled="!isPriceReady"
                @input="onPriceInput('max')"
                @change="applyFilters"
              />
            </div>

            <small class="text-muted d-block mt-1">
              Giá tối đa hiện tại:
              <b>{{ isPriceReady ? formatCurrency(priceMaxDbSafe) : 'Đang tải...' }}</b>
            </small>
          </div>

          <!-- size -->
          <div class="col-12 col-lg-3">
            <label class="form-label">Kích cỡ</label>
            <select v-model="filters.size" class="form-select" @change="applyFilters">
              <option value="">-- Chọn Kích cỡ --</option>
              <option v-for="s in attributes.kichCo" :key="s.id" :value="String(s.id)">
                {{ s.soSize }}
              </option>
            </select>
          </div>

          <!-- status + reset -->
          <div class="col-12 col-lg-3 position-relative">
            <label class="form-label">Trạng thái</label>
            <div class="status-radio">
              <label><input type="radio" value="all" v-model="filters.status" @change="applyFilters" /> Tất cả</label>
              <label><input type="radio" value="in" v-model="filters.status" @change="applyFilters" /> Còn hàng</label>
              <label><input type="radio" value="out" v-model="filters.status" @change="applyFilters" /> Hết hàng</label>
            </div>

            <button class="btn btn-link btn-sm reset-btn" type="button" @click="resetFilters">
              <i class="bi bi-arrow-counterclockwise me-1"></i> Đặt lại
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Table -->
    <div class="card table-card">
      <div class="table-responsive">
        <table class="table variants-table">
          <thead>
            <tr>
              <th v-if="exportMode" class="text-center col-check">
                <input
                  type="checkbox"
                  :disabled="filteredItems.length === 0"
                  :checked="allVisibleSelected"
                  @change="toggleSelectAllVisible($event.target.checked)"
                  title="Chọn tất cả dòng đang hiển thị"
                />
              </th>

              <th class="text-center col-stt">STT</th>
              <th class="text-center col-img">Ảnh</th>
              <th class="text-center col-code">Mã SP chi tiết</th>
              <th class="text-center col-name">Tên sản phẩm</th>
              <th class="text-center col-color">Màu sắc</th>
              <th class="text-center col-size">Kích cỡ</th>
              <th class="text-center col-stock">Số lượng tồn</th>
              <th class="text-center col-price">Giá bán</th>
              <th class="text-center col-status">Trạng thái</th>
              <th class="text-center col-action">Hành động</th>
            </tr>
          </thead>

          <tbody>
            <tr v-for="(v, index) in filteredItems" :key="v.id">
              <td v-if="exportMode" class="text-center col-check">
                <input type="checkbox" :checked="isSelected(v.id)" @change="toggleSelect(v, $event.target.checked)" />
              </td>

              <td class="text-center">{{ currentPage * pageSize + index + 1 }}</td>

              <td class="text-center">
                <div class="img-cell">
                  <img
                    v-if="v.anh && !v.__imgErr"
                    :src="buildImgUrl(v.anh)"
                    class="variant-img"
                    @error="v.__imgErr = true"
                  />
                  <span v-else class="no-img">Ảnh biến thể</span>
                </div>
              </td>

              <td class="text-center">{{ v.maSanPhamChiTiet }}</td>
              <td class="text-center text-bold">{{ v.tenSanPham }}</td>

              <td class="text-center">
                <span class="color-dot" :style="{ backgroundColor: getColorCode(v.tenMauSac) }"></span>
                {{ v.tenMauSac }}
              </td>

              <td class="text-center">{{ v.tenKichCo }}</td>
              <td class="text-center">{{ v.soLuongTon }}</td>
              <td class="text-center text-highlight">{{ formatCurrency(v.donGia) }}</td>

              <td class="text-center">
                <span class="badge-pill" :class="v.trangThai ? 'badge-success' : 'badge-danger'">
                  {{ v.trangThai ? 'Còn hàng' : 'Hết hàng' }}
                </span>
              </td>

              <td class="text-center">
                <div class="action-buttons">
                  <button class="btn btn-outline-warning btn-sm edit-btn" type="button" title="Sửa" @click="openEditModal(v)">
                    <i class="bi bi-pencil-square"></i>
                  </button>

                  <label class="switch" title="Đổi trạng thái">
                    <input type="checkbox" :checked="!!v.trangThai" @click.prevent="requestToggleStatus(v)" />
                    <span class="slider"></span>
                  </label>
                </div>
              </td>
            </tr>

            <tr v-if="loading">
              <td :colspan="tableColspan" class="text-center py-4">Đang tải dữ liệu...</td>
            </tr>
            <tr v-else-if="!filteredItems.length">
              <td :colspan="tableColspan" class="text-center py-4">Không có dữ liệu</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="paging-bar" v-if="totalPages > 0">
        <div class="paging-left">Hiển thị {{ filteredItems.length }} / tổng {{ totalElements }} bản ghi</div>

        <div class="paging-center">
          <button class="btn btn-outline-secondary btn-sm" :disabled="currentPage === 0" @click="changePage(currentPage - 1)" type="button">‹</button>

          <div class="input-group input-group-sm paging-page">
            <span class="input-group-text">Trang</span>
            <input
              type="number"
              min="1"
              :max="totalPages || 1"
              class="form-control"
              v-model.number="pageInput"
              @keyup.enter="jumpPage"
              @blur="jumpPage"
            />
          </div>

          <button class="btn btn-outline-secondary btn-sm" :disabled="currentPage >= totalPages - 1" @click="changePage(currentPage + 1)" type="button">›</button>
        </div>

        <div class="paging-right">
          <select class="form-select form-select-sm paging-size" v-model.number="pageSize" @change="onChangeSize">
            <option :value="10">10 bản ghi / trang</option>
            <option :value="20">20 bản ghi / trang</option>
            <option :value="50">50 bản ghi / trang</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Confirm Toggle Modal -->
    <div v-if="showConfirmToggle" class="modal-overlay" @click.self="closeToggleModal">
      <div class="modal-box modal-confirm">
        <div class="modal-head">
          <h6 class="mb-0">Xác nhận đổi trạng thái</h6>
          <button class="btn-close" type="button" @click="closeToggleModal"></button>
        </div>

        <div class="modal-body p-3">
          <div>
            Bạn có chắc muốn đổi trạng thái biến thể
            <b>{{ pendingVariant?.maSanPhamChiTiet }}</b>
            thành <b>{{ pendingNext ? 'Còn hàng' : 'Hết hàng' }}</b> không?
          </div>
        </div>

        <div class="modal-foot">
          <button class="btn btn-secondary btn-sm" type="button" @click="closeToggleModal" :disabled="toggleLoading">Hủy</button>
          <button class="btn btn-primary btn-sm" type="button" @click="confirmToggleStatus" :disabled="toggleLoading">
            {{ toggleLoading ? 'Đang xử lý...' : 'Xác nhận' }}
          </button>
        </div>
      </div>
    </div>

    <!-- (Bạn có thể gắn modal Edit/QR theo dự án nếu muốn giữ, mình bỏ bớt cho tập trung phần slider + export) -->
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import * as XLSX from 'xlsx'

import attributeService from '../../services/attributeService'
import { getAllDetails, updateDetail } from '../../services/sanPhamChiTietApi'
import { getGiaMaxDb } from '../../services/sanPhamApi'
import { useToast } from '../../composables/useToast'

const { success, error } = useToast()
const router = useRouter()

/** base url */
const apiBaseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
const fileBaseUrl = (import.meta.env.VITE_FILE_BASE_URL || apiBaseUrl).replace(/\/api\/?$/, '')

function buildImgUrl(path) {
  if (!path) return ''
  const p = String(path).replace(/\\/g, '/')
  if (p.startsWith('http://') || p.startsWith('https://')) return p
  const b = String(fileBaseUrl).replace(/\/+$/, '')
  return b + (p.startsWith('/') ? p : `/${p}`)
}

/** pagination */
const items = ref([])
const loading = ref(false)
const currentPage = ref(0)
const pageSize = ref(10)
const totalPages = ref(0)
const totalElements = ref(0)
const pageInput = ref(1)

/** filter */
const isFilterOpen = ref(true)
const PRICE_MIN = 0
const PRICE_STEP = 50000

// ✅ KHÔNG DEFAULT: chờ DB trả về
const priceMaxDb = ref(null)

const filters = reactive({
  keyword: '',
  color: '',
  size: '',
  stockRange: '',
  status: 'all', // all | in | out
  priceMin: PRICE_MIN,
  priceMax: 0 // sẽ set sau khi có DB
})

const filterErrors = reactive({ keyword: '' })

function hasLeadingSpace(s) {
  return /^\s+/.test(String(s ?? ''))
}
function onKeywordInput() {
  filterErrors.keyword = hasLeadingSpace(filters.keyword) ? 'Không được nhập khoảng trắng ở đầu' : ''
}

/** price helpers */
const isPriceReady = computed(() => Number.isFinite(Number(priceMaxDb.value)) && Number(priceMaxDb.value) > 0)
const priceMaxDbSafe = computed(() => (isPriceReady.value ? Number(priceMaxDb.value) : 0))

function clampPrice(which) {
  const max = priceMaxDbSafe.value

  if (filters.priceMin < PRICE_MIN) filters.priceMin = PRICE_MIN
  if (filters.priceMax > max) filters.priceMax = max

  if (filters.priceMin > filters.priceMax) {
    if (which === 'min') filters.priceMin = filters.priceMax
    else filters.priceMax = filters.priceMin
  }
}

function onPriceInput(which) {
  if (!isPriceReady.value) return
  clampPrice(which)
}

/** ✅ rangeStyle dùng left + width (ổn định, không bị “thanh lạ”) */
const rangeStyle = computed(() => {
  if (!isPriceReady.value) return { left: '0%', width: '0%' }

  const max = Math.max(1, priceMaxDbSafe.value - PRICE_MIN)
  const left = ((filters.priceMin - PRICE_MIN) / max) * 100
  const right = ((filters.priceMax - PRICE_MIN) / max) * 100
  const width = Math.max(0, right - left)

  return { left: left + '%', width: width + '%' }
})

function applyFilters() {
  // client-side filter (computed filteredItems)
}

/** attributes */
const attributes = reactive({ kichCo: [], mauSac: [] })

async function loadAttributes() {
  try {
    const [resSize, resColor] = await Promise.all([
      attributeService.getAllList('kich-co'),
      attributeService.getAllList('mau-sac')
    ])
    attributes.kichCo = resSize?.data || []
    attributes.mauSac = resColor?.data || []
  } catch (e) {
    console.error(e)
  }
}

/** ✅ max price DB: set thẳng filters.priceMax = maxDb, KHÔNG default */
function roundUpToStep(n, step) {
  const x = Number(n || 0)
  if (x <= 0) return 0
  return Math.ceil(x / step) * step
}

async function loadPriceMaxDb() {
  try {
    const res = await getGiaMaxDb()
    const raw = res?.data ?? res
    const maxDb = roundUpToStep(Number(raw || 0), PRICE_STEP)

    if (!maxDb) {
      // nếu DB không có giá, vẫn cho slider về 0-0
      priceMaxDb.value = 0
      filters.priceMin = PRICE_MIN
      filters.priceMax = 0
      return
    }

    priceMaxDb.value = maxDb

    // ✅ set max theo DB ngay lập tức
    filters.priceMin = PRICE_MIN
    filters.priceMax = maxDb
  } catch (e) {
    console.error(e)
    // nếu lỗi DB, cho về 0 để tránh UI lệch
    priceMaxDb.value = 0
    filters.priceMin = PRICE_MIN
    filters.priceMax = 0
  }
}

/** load data */
async function reload() {
  loading.value = true
  try {
    const res = await getAllDetails(currentPage.value, pageSize.value)
    const page = res?.data ?? res

    items.value = (page?.content || []).map(v => ({ ...v, __imgErr: false }))
    totalPages.value = page?.totalPages || 0
    totalElements.value = page?.totalElements || 0
    pageInput.value = currentPage.value + 1
  } catch (e) {
    console.error(e)
    items.value = []
    totalPages.value = 0
    totalElements.value = 0
    error('Không tải được danh sách biến thể')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadAttributes(), loadPriceMaxDb()])
  await reload()
})

/** client-side filter */
const filteredItems = computed(() => {
  const kwRaw = String(filters.keyword || '')
  if (hasLeadingSpace(kwRaw)) return []

  const kw = kwRaw.trim().toLowerCase()

  return (items.value || []).filter(v => {
    const matchKeyword =
      !kw ||
      String(v.maSanPhamChiTiet || '').toLowerCase().includes(kw) ||
      String(v.tenSanPham || '').toLowerCase().includes(kw) ||
      String(v.tenMauSac || '').toLowerCase().includes(kw) ||
      String(v.tenKichCo || '').toLowerCase().includes(kw)

    const matchColor = !filters.color || String(v.idMauSac ?? '') === String(filters.color)
    const matchSize  = !filters.size  || String(v.idKichCo ?? '') === String(filters.size)

    const stock = Number(v.soLuongTon ?? 0)
    let matchStock = true
    switch (filters.stockRange) {
      case '0': matchStock = stock === 0; break
      case '1-10': matchStock = stock >= 1 && stock <= 10; break
      case '11-50': matchStock = stock >= 11 && stock <= 50; break
      case '51-200': matchStock = stock >= 51 && stock <= 200; break
      case '200+': matchStock = stock > 200; break
      default: matchStock = true
    }

    let matchStatus = true
    if (filters.status === 'in') matchStatus = !!v.trangThai === true
    if (filters.status === 'out') matchStatus = !!v.trangThai === false

    // nếu DB lỗi => max=0 thì lọc giá chỉ cho giá=0
    const price = Number(v.donGia ?? 0)
    const matchPrice =
      !isPriceReady.value
        ? true
        : price >= Number(filters.priceMin) && price <= Number(filters.priceMax)

    return matchKeyword && matchColor && matchSize && matchStock && matchStatus && matchPrice
  })
})

/** pagination */
function changePage(p) {
  if (p < 0 || p >= totalPages.value) return
  currentPage.value = p
  pageInput.value = p + 1
  reload()
}
function jumpPage() {
  const max = Math.max(1, totalPages.value || 1)
  const target = Math.min(Math.max(1, pageInput.value || 1), max)
  changePage(target - 1)
}
function onChangeSize() {
  currentPage.value = 0
  pageInput.value = 1
  reload()
}

/** reset */
async function resetFilters() {
  filters.keyword = ''
  filters.color = ''
  filters.size = ''
  filters.stockRange = ''
  filters.status = 'all'
  filterErrors.keyword = ''

  // ✅ reset theo max DB
  filters.priceMin = PRICE_MIN
  filters.priceMax = priceMaxDbSafe.value

  currentPage.value = 0
  pageInput.value = 1
  await reload()
  success('Đã hiển thị tất cả biến thể')
}

function goBack() {
  router.push('/products')
}

/** ===== EXPORT EXCEL (FE - N files) ===== */
const exportMode = ref(false)
const exporting = ref(false)
const selectedIds = ref([])
const selectedRows = reactive({})

const tableColspan = computed(() => (exportMode.value ? 11 : 10))

function openExportMode() {
  exportMode.value = true
}
function cancelExportMode() {
  exportMode.value = false
  selectedIds.value = []
  Object.keys(selectedRows).forEach((k) => delete selectedRows[k])
}
function isSelected(id) {
  return selectedIds.value.includes(id)
}
function toggleSelect(row, checked) {
  const id = row.id
  if (checked) {
    if (!selectedIds.value.includes(id)) selectedIds.value.push(id)
    selectedRows[id] = { ...row }
  } else {
    selectedIds.value = selectedIds.value.filter((x) => x !== id)
    delete selectedRows[id]
  }
}
const allVisibleSelected = computed(() => {
  if (!exportMode.value || filteredItems.value.length === 0) return false
  return filteredItems.value.every((v) => selectedIds.value.includes(v.id))
})
function toggleSelectAllVisible(checked) {
  filteredItems.value.forEach((v) => toggleSelect(v, checked))
}
function sleep(ms) {
  return new Promise((r) => setTimeout(r, ms))
}
function safeName(s) {
  return String(s ?? '').trim().slice(0, 50).replace(/[^\w\-]+/g, '_')
}
function toExcelRow(v) {
  return {
    'Mã SP chi tiết': v.maSanPhamChiTiet ?? '',
    'Tên sản phẩm': v.tenSanPham ?? '',
    'Màu sắc': v.tenMauSac ?? '',
    'Kích cỡ': v.tenKichCo ?? '',
    'Số lượng tồn': Number(v.soLuongTon ?? 0),
    'Giá bán': Number(v.donGia ?? 0),
    'Trạng thái': v.trangThai ? 'Còn hàng' : 'Hết hàng',
    'Ảnh': v.anh ? buildImgUrl(v.anh) : ''
  }
}
async function exportSelectedToExcel() {
  if (selectedIds.value.length === 0) return

  exporting.value = true
  try {
    for (const id of selectedIds.value) {
      const v = selectedRows[id]
      if (!v) continue

      const ws = XLSX.utils.json_to_sheet([toExcelRow(v)])
      const wb = XLSX.utils.book_new()
      XLSX.utils.book_append_sheet(wb, ws, 'BienThe')

      const buf = XLSX.write(wb, { bookType: 'xlsx', type: 'array' })
      const blob = new Blob([buf], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      })

      const code = safeName(v.maSanPhamChiTiet ?? id)
      const name = safeName(v.tenSanPham ?? '')
      const fileName = `bien-the_${code}${name ? '_' + name : ''}.xlsx`

      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = fileName
      document.body.appendChild(a)
      a.click()
      a.remove()
      URL.revokeObjectURL(url)

      await sleep(120)
    }

    cancelExportMode()
    success('Xuất Excel thành công')
  } catch (e) {
    console.error(e)
    error('Xuất Excel thất bại (có thể bị chặn tải nhiều file).')
  } finally {
    exporting.value = false
  }
}

/** ===== Toggle status ===== */
const showConfirmToggle = ref(false)
const pendingVariant = ref(null)
const pendingNext = ref(false)
const toggleLoading = ref(false)
const showQrModal = ref(false)

function requestToggleStatus(variant) {
  pendingVariant.value = variant
  pendingNext.value = !variant.trangThai
  showConfirmToggle.value = true
}
function closeToggleModal() {
  showConfirmToggle.value = false
  pendingVariant.value = null
  pendingNext.value = false
}
async function confirmToggleStatus() {
  if (!pendingVariant.value?.id) return
  toggleLoading.value = true

  const v = pendingVariant.value
  const next = pendingNext.value

  try {
    await updateDetail(v.id, {
      idSanPham: v.idSanPham,
      idKichCo: v.idKichCo,
      idMauSac: v.idMauSac,
      soLuongTon: v.soLuongTon,
      donGia: v.donGia,
      ghiChu: v.ghiChu,
      trangThai: next,
      anh: v.anh
    })
    closeToggleModal()
    await reload()
    success(`Đã đổi trạng thái thành ${next ? 'Còn hàng' : 'Hết hàng'}`)
  } catch (e) {
    console.error(e)
    error('Lỗi cập nhật trạng thái')
  } finally {
    toggleLoading.value = false
  }
}

/** misc */
function formatCurrency(val) {
  const n = Number(val ?? 0)
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n)
}

/** color dot */
function normalizeColorName(name) {
  return String(name || '')
    .trim()
    .toLowerCase()
    .replace(/đ/g, 'd')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/\(.*?\)/g, '')
    .replace(/\s+/g, ' ')
    .trim()
}
const COLOR_MAP = {
  den: '#111827',
  trang: '#ffffff',
  xam: '#9ca3af',
  ghi: '#9ca3af',
  do: '#ef4444',
  vang: '#f59e0b',
  cam: '#f97316',
  hong: '#ec4899',
  tim: '#a855f7',
  nau: '#92400e',
  be: '#f5f5dc',
  kem: '#fff7ed',
  'xanh la': '#10b981',
  'xanh luc': '#10b981',
  'xanh ngoc': '#14b8a6',
  'xanh duong': '#3b82f6',
  'xanh navy': '#1e3a8a',
  'xanh than': '#1e3a8a',
  navy: '#1e3a8a'
}
function getColorCode(colorName) {
  if (!colorName) return '#9ca3af'
  const key = normalizeColorName(colorName)
  if (COLOR_MAP[key]) return COLOR_MAP[key]
  if (key.includes('navy') || key.includes('than')) return COLOR_MAP['xanh navy']
  if (key.includes('xanh') && (key.includes('la') || key.includes('luc'))) return COLOR_MAP['xanh la']
  if (key.includes('xanh') && key.includes('duong')) return COLOR_MAP['xanh duong']
  if (key.includes('den')) return COLOR_MAP.den
  if (key.includes('trang')) return COLOR_MAP.trang
  if (key.includes('do')) return COLOR_MAP.do
  return '#9ca3af'
}
</script>

<style scoped>
.variant-page{
  padding: 16px;
  background: #ffffff;
  min-height: 100vh;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}

/* header */
.page-header{ display:flex; align-items:center; justify-content:space-between; gap:12px; margin-bottom:12px; }
.page-title h2{ margin:0; font-size:1.25rem; font-weight:700; color:#111827; }
.page-actions{ display:flex; gap:10px; flex-wrap:wrap; }

/* card */
.card{ background:#fff; border-radius:8px; border:1px solid #e5e7eb; box-shadow:0 1px 3px rgba(0,0,0,.08); }
.filter-card{ overflow:hidden; margin-bottom: 12px; }
.table-card{ padding: 0; overflow:hidden; }

/* filter head */
.filter-head{ background:#1e293b; color:#fff; padding:10px 14px; display:flex; justify-content:space-between; align-items:center; cursor:pointer; }
.filter-head-left{ display:flex; align-items:center; gap:10px; font-weight:700; }
.filter-head-right{ font-size:12px; opacity:.9; }
.filter-body{ padding:14px; }

.form-label{ font-size:13px; font-weight:700; color:#111827; margin-bottom:6px; }

/* status */
.status-radio{ display:flex; align-items:center; gap:12px; font-size:13px; color:#111827; }
.status-radio input{ transform: translateY(1px); margin-right:6px; }

.reset-btn{
  position:absolute;
  right: 0;
  bottom: -2px;
  padding: 0;
  text-decoration:none;
  color:#6b7280;
}
.reset-btn:hover{ color:#111827; text-decoration: underline; }

/* price slider */
.price-label{ font-size:13px; font-weight:700; color:#111827; margin-bottom:6px; }
.price-green{ color:#059669; font-weight:800; }

.double-range{ position:relative; height:26px; }
.double-range__track{ position:absolute; left:0; right:0; top:12px; height:4px; background:#e5e7eb; border-radius:999px; }
.double-range__range{ position:absolute; top:12px; height:4px; background:#10b981; border-radius:999px; }

.double-range__thumb{ position:absolute; left:0; top:0; width:100%; pointer-events:none; -webkit-appearance:none; appearance:none; background:transparent; height:26px; margin:0; }
.double-range__thumb::-webkit-slider-thumb{
  pointer-events:auto; -webkit-appearance:none; appearance:none;
  width:14px; height:14px; border-radius:50%;
  background:#fff; border:2px solid #10b981;
  box-shadow:0 1px 2px rgba(0,0,0,.15);
}
.double-range__thumb::-moz-range-thumb{
  pointer-events:auto;
  width:14px; height:14px; border-radius:50%;
  background:#fff; border:2px solid #10b981;
  box-shadow:0 1px 2px rgba(0,0,0,.15);
}

/* TABLE */
.table-responsive{ overflow-x:auto; overflow-y:hidden; }
.variants-table{ width:100%; table-layout:fixed; border-collapse:separate; border-spacing:0; margin:0; min-width: 1250px; }

.variants-table thead th{
  background:#1e293b; color:#fff;
  padding:10px 12px;
  text-align:center;
  font-weight:600;
  border-bottom:1px solid #e5e7eb;
  white-space:nowrap;
}
.variants-table td{
  padding:12px;
  border-bottom:1px solid #eef2f7;
  vertical-align: middle !important;
  color:#374151;
}

.col-check{ width:46px; min-width:46px; }
.col-stt{ width:70px; }
.col-img{ width:260px; }
.col-code{ width:160px; }
.col-name{ width:220px; }
.col-color{ width:170px; }
.col-size{ width:120px; }
.col-stock{ width:120px; }
.col-price{ width:140px; }
.col-status{ width:120px; }
.col-action{ width:150px; }

.text-center{ text-align:center; }
.text-bold{ font-weight:700; color:#111827; }
.text-highlight{ color:#0f766e; font-weight:800; }

/* image */
.img-cell{ display:flex; align-items:center; justify-content:center; }
.variant-img{
  width:200px; height:200px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  background:#fff;
}
.no-img{
  display:flex; align-items:center; justify-content:center;
  width:200px; height:200px;
  text-align:center;
  color:#6b7280;
  background:#f3f4f6;
  border-radius:6px;
  border:1px solid #e5e7eb;
  font-size:13px;
}

/* color dot */
.color-dot{
  display:inline-block;
  width:10px; height:10px;
  border-radius:999px;
  border:1px solid #e5e7eb;
  margin-right: 6px;
}

/* badge */
.badge-pill{
  display:inline-flex; align-items:center; justify-content:center;
  padding:4px 10px; border-radius:999px;
  font-size:12px; font-weight:800; white-space:nowrap;
}
.badge-success{ background:#d1fae5; color:#065f46; }
.badge-danger{ background:#fee2e2; color:#991b1b; }

/* actions */
.action-buttons{ display:inline-flex; align-items:center; justify-content:center; gap:10px; }
.edit-btn{
  width:32px; height:32px; padding:0;
  display:inline-flex; align-items:center; justify-content:center;
  border-radius:4px;
}
.switch{ position:relative; display:inline-block; width:44px; height:22px; }
.switch input{ opacity:0; width:0; height:0; }
.slider{ position:absolute; inset:0; cursor:pointer; background:#d1d5db; transition:.2s; border-radius:999px; }
.slider:before{
  content:""; position:absolute; width:18px; height:18px;
  left:2px; top:2px; background:#fff; transition:.2s;
  border-radius:50%; box-shadow:0 1px 2px rgba(0,0,0,.15);
}
.switch input:checked + .slider{ background:#2563eb; }
.switch input:checked + .slider:before{ transform: translateX(22px); }

/* pagination */
.paging-bar{
  padding: 14px;
  display:grid;
  grid-template-columns: 1fr auto 1fr;
  align-items:center;
  gap:12px;
}
.paging-left{ justify-self:start; color:#6b7280; font-size:13px; }
.paging-center{ justify-self:center; display:flex; align-items:center; gap:10px; flex-wrap:nowrap; }
.paging-right{ justify-self:end; }
.paging-page{ width:120px; }
.paging-size{ width:170px; }

/* modal */
.modal-overlay{
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,.45);
  display:flex;
  align-items:center;
  justify-content:center;
  z-index: 9999;
}
.modal-box{
  width: 520px;
  max-width: calc(100vw - 24px);
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow:0 18px 50px rgba(0,0,0,.22);
}
.modal-confirm{ width: 520px; }
.modal-head{
  padding: 12px 14px;
  display:flex;
  align-items:center;
  justify-content:space-between;
  border-bottom: 1px solid #eef2f7;
}
.modal-foot{
  padding: 12px 14px;
  display:flex;
  justify-content:flex-end;
  gap: 10px;
  border-top: 1px solid #eef2f7;
}
</style>
