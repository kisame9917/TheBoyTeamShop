<template>
  <div class="product-detail-page">
    <!-- Header -->
    <div class="page-header">
      <div class="page-title">
        <h2>Quản lý sản phẩm / Chi tiết biến thể</h2>
      </div>

      <div class="page-actions">
        <button class="btn btn-outline-secondary btn-sm" type="button" @click="scanQr">
          <i class="bi bi-qr-code me-1"></i> Quét QR
        </button>

        <!-- EXCEL -->
        <button
          v-if="!exportMode"
          class="btn btn-outline-primary btn-sm"
          type="button"
          @click="openExportMode"
        >
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

        <button class="btn btn-primary btn-sm" type="button" @click="goToGlobalList">
          <i class="bi bi-list-ul me-1"></i> Hiển thị đầy đủ biến thể
        </button>

        <button class="btn btn-secondary btn-sm" type="button" @click="goBack">
          <i class="bi bi-arrow-left me-1"></i> Quay lại
        </button>
      </div>
    </div>

    <div v-if="loading" class="text-center py-4">Đang tải dữ liệu...</div>
    <div v-else-if="globalError" class="text-center error-text py-4">{{ globalError }}</div>

    <div v-else class="content-wrapper">
      <!-- Filter -->
      <div class="card filter-card">
        <div class="filter-head" @click="filterOpen = !filterOpen">
          <div class="filter-head-left">
            <i class="bi" :class="filterOpen ? 'bi-caret-down-fill' : 'bi-caret-right-fill'"></i>
            <span>Bộ lọc tìm kiếm</span>
          </div>
          <div class="filter-head-right">Nhấn để thu gọn/mở rộng</div>
        </div>

        <div v-show="filterOpen" class="filter-body">
          <div class="row g-3 align-items-end">
            <div class="col-12 col-lg-6">
              <label class="form-label">Tìm kiếm</label>
              <input
                v-model="filters.keyword"
                type="text"
                class="form-control"
                placeholder="Tìm theo mã, màu, kích cỡ..."
                @input="onKeywordInput"
                @keyup.enter="applyFilters"
              />
            </div>

            <div class="col-12 col-lg-3">
              <label class="form-label">Màu sắc</label>
              <select v-model="filters.colorId" class="form-select" @change="applyFilters">
                <option value="">-- Chọn Màu sắc --</option>
                <option v-for="c in attributes.mauSac" :key="c.id" :value="String(c.id)">
                  {{ c.ten }}
                </option>
              </select>
            </div>

            <div class="col-12 col-lg-3">
              <label class="form-label">Số lượng tồn</label>
              <select v-model="filters.stock" class="form-select" @change="applyFilters">
                <option value="">-- Chọn Số lượng tồn --</option>
                <option value="lt10">&lt; 10</option>
                <option value="10_50">10 - 50</option>
                <option value="50_100">50 - 100</option>
                <option value="gte100">&ge; 100</option>
              </select>
            </div>

            <div class="col-12 col-lg-6">
              <div class="price-label">
                Khoảng giá:
                <span class="price-green">
                  {{ isPriceReady ? `${formatPrice(filters.priceMin)} - ${formatPrice(filters.priceMax)}` : 'Đang tải...' }}
                </span>
              </div>

              <div class="range-slider">
                <div class="slider-range" :style="rangeStyle"></div>

                <input
                  type="range"
                  min="0"
                  :max="priceMaxSafe"
                  :step="PRICE_STEP"
                  v-model.number="filters.priceMin"
                  :disabled="!isPriceReady"
                  @input="onPriceInput('min')"
                  @change="applyFilters"
                />

                <input
                  type="range"
                  min="0"
                  :max="priceMaxSafe"
                  :step="PRICE_STEP"
                  v-model.number="filters.priceMax"
                  :disabled="!isPriceReady"
                  @input="onPriceInput('max')"
                  @change="applyFilters"
                />
              </div>

              <small class="hint">
                Giá tối đa hiện tại: <b>{{ isPriceReady ? formatPrice(priceMaxSafe) : 'Đang tải...' }}</b>
              </small>
            </div>

            <div class="col-12 col-lg-3">
              <label class="form-label">Kích cỡ</label>
              <select v-model="filters.sizeId" class="form-select" @change="applyFilters">
                <option value="">-- Chọn Kích cỡ --</option>
                <option v-for="s in attributes.kichCo" :key="s.id" :value="String(s.id)">
                  {{ s.soSize }}
                </option>
              </select>
            </div>

            <div class="col-12 col-lg-3">
              <label class="form-label">Trạng thái</label>
              <div class="status-radio">
                <label class="me-3">
                  <input type="radio" value="" v-model="filters.status" @change="applyFilters" />
                  Tất cả
                </label>
                <label class="me-3">
                  <input type="radio" value="true" v-model="filters.status" @change="applyFilters" />
                  Còn hàng
                </label>
                <label>
                  <input type="radio" value="false" v-model="filters.status" @change="applyFilters" />
                  Hết hàng
                </label>
              </div>
            </div>

            <div class="col-12">
              <div class="filter-reset">
                <button class="btn btn-link btn-sm text-decoration-none" type="button" @click="resetFilters">
                  <i class="bi bi-arrow-clockwise me-1"></i> Đặt lại
                </button>
              </div>
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
                    :disabled="pagedVariants.length === 0"
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
              <tr v-for="(v, index) in pagedVariants" :key="v.id">
                <td v-if="exportMode" class="text-center col-check">
                  <input type="checkbox" :checked="isSelected(v.id)" @change="toggleSelect(v, $event.target.checked)" />
                </td>

                <td class="text-center">{{ currentPage * pageSize + index + 1 }}</td>

                <td class="text-center">
                  <div class="img-cell img-cell--lg">
                    <img
                      v-if="v.anh && !v.__imgErr"
                      :src="buildImgUrl(v.anh)"
                      class="variant-img variant-img--lg"
                      alt="Ảnh biến thể"
                      @error="v.__imgErr = true"
                    />
                    <span v-else class="no-img no-img--lg">Ảnh biến thể</span>
                  </div>
                </td>

                <td class="text-center">{{ v.maSanPhamChiTiet || '-' }}</td>
                <td class="text-center text-bold">{{ productName || '-' }}</td>

                <td class="text-center">
                  <div class="color-cell">
                    <span class="color-dot" :style="{ backgroundColor: getColorCode(v.tenMauSac) }"></span>
                    <span class="color-name">{{ v.tenMauSac || '-' }}</span>
                  </div>
                </td>

                <td class="text-center">{{ v.tenKichCo || '-' }}</td>
                <td class="text-center">{{ v.soLuongTon ?? 0 }}</td>
                <td class="text-center text-highlight">{{ formatPrice(v.donGia ?? 0) }}</td>

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
                      <input
                        type="checkbox"
                        :checked="!!v.trangThai"
                        :disabled="togglingIds.has(v.id)"
                        @click.prevent="openConfirmToggle(v)"
                      />
                      <span class="slider"></span>
                    </label>
                  </div>
                </td>
              </tr>

              <tr v-if="!pagedVariants.length">
                <td :colspan="tableColspan" class="text-center py-4">Không tìm thấy biến thể nào.</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div class="paging-bar" v-if="totalElements > 0">
          <div class="paging-left">Hiển thị {{ pagedVariants.length }} / tổng {{ totalElements }} bản ghi</div>

          <div class="paging-center">
            <button class="btn btn-outline-secondary btn-sm" :disabled="currentPage === 0" @click="setPage(currentPage - 1)" type="button">‹</button>

            <div class="input-group input-group-sm paging-page">
              <span class="input-group-text">Trang</span>
              <input
                type="number"
                min="1"
                :max="totalPages"
                class="form-control"
                v-model.number="pageInput"
                @keyup.enter="jumpPage"
                @blur="jumpPage"
              />
            </div>

            <button class="btn btn-outline-secondary btn-sm" :disabled="currentPage >= totalPages - 1" @click="setPage(currentPage + 1)" type="button">›</button>
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
    </div>

    <!-- Edit Modal -->
    <div v-if="edit.open" class="modal-overlay" @click.self="closeEditModal">
      <div class="modals">
        <div class="modal-header">
          <h3>Sửa biến thể: {{ edit.maSanPhamChiTiet }}</h3>
          <button class="close-btn" @click="closeEditModal">×</button>
        </div>

        <div class="modal-body">
          <div class="row g-3">
            <div class="col-6">
              <label class="form-label">Kích cỡ</label>
              <select v-model="edit.idKichCo" class="form-select">
                <option value="" disabled>-- Chọn kích cỡ --</option>
                <option v-for="s in attributes.kichCo" :key="s.id" :value="String(s.id)">
                  {{ s.soSize }}
                </option>
              </select>
            </div>

            <div class="col-6">
              <label class="form-label">Màu sắc</label>
              <select v-model="edit.idMauSac" class="form-select">
                <option value="" disabled>-- Chọn màu sắc --</option>
                <option v-for="c in attributes.mauSac" :key="c.id" :value="String(c.id)">
                  {{ c.ten }}
                </option>
              </select>
            </div>

            <div class="col-6">
              <label class="form-label">Số lượng</label>
              <input type="number" v-model.number="edit.soLuongTon" class="form-control" min="0" step="1" />
            </div>

            <div class="col-6">
              <label class="form-label">Đơn giá</label>
              <input
                type="text"
                inputmode="numeric"
                class="form-control"
                placeholder="Ví dụ: 999.999"
                v-model="edit.donGiaText"
                @input="onEditMoneyTyping"
                @blur="onEditMoneyBlur"
              />
            </div>

            <div class="col-12">
              <label class="form-label">Trạng thái</label>
              <div class="status-radio">
                <label class="me-3"><input type="radio" :value="true" v-model="edit.trangThai" /> Còn hàng</label>
                <label><input type="radio" :value="false" v-model="edit.trangThai" /> Hết hàng</label>
              </div>
            </div>

            <div class="col-12">
              <label class="form-label">Ảnh biến thể</label>
              <input type="file" @change="handleFileUpload" class="form-control" accept="image/*" />
              <div v-if="edit.anh" class="mt-2">
                <img :src="buildImgUrl(edit.anh)" class="variant-img preview" alt="Preview" />
              </div>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeEditModal">Hủy</button>
          <button class="btn btn-primary" @click="submitEdit" :disabled="edit.saving">
            {{ edit.saving ? 'Đang lưu...' : 'Lưu thay đổi' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Confirm Toggle Modal -->
    <div v-if="confirmToggle.open" class="modal-overlay" @click.self="closeConfirmToggle">
      <div class="modals confirm-modal">
        <div class="modal-header">
          <h3>Xác nhận đổi trạng thái</h3>
          <button class="close-btn" @click="closeConfirmToggle">×</button>
        </div>

        <div class="modal-body">
          <p class="mb-2">
            Bạn có chắc muốn đổi trạng thái biến thể
            <b>{{ confirmToggle.target?.maSanPhamChiTiet }}</b>
            thành <b>{{ confirmToggle.next ? 'Còn hàng' : 'Hết hàng' }}</b> không?
          </p>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" type="button" @click="closeConfirmToggle">Hủy</button>
          <button class="btn btn-primary" type="button" @click="confirmToggleNow">Xác nhận</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as XLSX from 'xlsx'

import attributeService from '../../services/attributeService'
import { getByProductId, updateDetail, uploadImage } from '../../services/sanPhamChiTietApi'
import { getGiaMaxDb } from '../../services/sanPhamApi'
import { useToast } from '../../composables/useToast'

const { success, error } = useToast()
const router = useRouter()

const props = defineProps({
  id: { type: [String, Number], required: true }
})

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

/** state */
const loading = ref(false)
const globalError = ref('')
const filterOpen = ref(true)

const variants = ref([])
const attributes = reactive({ kichCo: [], mauSac: [] })

/** product name (lấy từ dữ liệu biến thể cho chắc) */
const productName = computed(() => {
  const first = variants.value?.[0]
  return first?.tenSanPham || first?.sanPhamTen || ''
})

/** PRICE: không default, chờ DB */
const PRICE_STEP = 10000
const priceMaxDb = ref(null) // null -> chưa sẵn sàng
const isPriceReady = computed(() => Number(priceMaxDb.value || 0) > 0)
const priceMaxSafe = computed(() => (isPriceReady.value ? Number(priceMaxDb.value) : 0))

/** filters */
const filters = reactive({
  keyword: '',
  colorId: '',
  sizeId: '',
  stock: '',
  status: '',
  priceMin: 0,
  priceMax: 0
})

function onKeywordInput() {
  // chặn khoảng trắng đầu
  filters.keyword = String(filters.keyword ?? '').replace(/^\s+/, '')
}

function roundUpToStep(n, step) {
  const x = Number(n || 0)
  if (!Number.isFinite(x) || x <= 0) return 0
  return Math.ceil(x / step) * step
}

function syncPriceFilterToMax() {
  if (!isPriceReady.value) return
  filters.priceMin = Math.max(0, Number(filters.priceMin || 0))
  filters.priceMax = Number(priceMaxSafe.value)
}

function onPriceInput(which) {
  if (!isPriceReady.value) return
  const max = priceMaxSafe.value

  if (filters.priceMin < 0) filters.priceMin = 0
  if (filters.priceMax > max) filters.priceMax = max

  if (filters.priceMin > filters.priceMax) {
    if (which === 'min') filters.priceMin = filters.priceMax
    else filters.priceMax = filters.priceMin
  }
}

/** rangeStyle: left + width (không bị lỗi thanh) */
const rangeStyle = computed(() => {
  if (!isPriceReady.value) return { left: '0%', width: '0%' }

  const max = Math.max(1, priceMaxSafe.value)
  const minV = Math.max(0, Math.min(filters.priceMin, max))
  const maxV = Math.max(0, Math.min(filters.priceMax, max))
  const left = (minV / max) * 100
  const width = ((maxV - minV) / max) * 100
  return { left: left + '%', width: width + '%' }
})

/** filtering + paging */
const filteredVariants = computed(() => {
  const kw = String(filters.keyword || '').toLowerCase().trim()
  const fMin = Number(filters.priceMin || 0)
  const fMax = Number(filters.priceMax || 0)

  return (variants.value || []).filter((v) => {
    const okKw =
      !kw ||
      String(v.maSanPhamChiTiet || '').toLowerCase().includes(kw) ||
      String(v.tenMauSac || '').toLowerCase().includes(kw) ||
      String(v.tenKichCo || '').toLowerCase().includes(kw)

    const okColor = !filters.colorId || String(v.idMauSac ?? '') === String(filters.colorId)
    const okSize = !filters.sizeId || String(v.idKichCo ?? '') === String(filters.sizeId)

    const okStatus = filters.status === '' || (String(!!v.trangThai) === String(filters.status))

    const sl = Number(v.soLuongTon ?? 0)
    let okStock = true
    if (filters.stock === 'lt10') okStock = sl < 10
    if (filters.stock === '10_50') okStock = sl >= 10 && sl <= 50
    if (filters.stock === '50_100') okStock = sl > 50 && sl <= 100
    if (filters.stock === 'gte100') okStock = sl >= 100

    const gia = Number(v.donGia ?? 0)
    const okPrice = !isPriceReady.value ? true : (gia >= fMin && gia <= fMax)

    return okKw && okColor && okSize && okStatus && okStock && okPrice
  })
})

const currentPage = ref(0)
const pageSize = ref(10)
const pageInput = ref(1)

const totalElements = computed(() => filteredVariants.value.length)
const totalPages = computed(() => Math.max(1, Math.ceil(totalElements.value / pageSize.value)))

const pagedVariants = computed(() => {
  const start = currentPage.value * pageSize.value
  return filteredVariants.value.slice(start, start + pageSize.value)
})

watch([totalElements, pageSize], () => {
  if (currentPage.value > totalPages.value - 1) currentPage.value = totalPages.value - 1
  pageInput.value = currentPage.value + 1
}, { immediate: true })

function applyFilters() {
  currentPage.value = 0
  pageInput.value = 1
}

function resetFilters() {
  filters.keyword = ''
  filters.colorId = ''
  filters.sizeId = ''
  filters.stock = ''
  filters.status = ''

  filters.priceMin = 0
  filters.priceMax = isPriceReady.value ? priceMaxSafe.value : 0

  applyFilters()
}

function onChangeSize() {
  currentPage.value = 0
  pageInput.value = 1
}

function setPage(p) {
  if (p < 0) return
  if (p > totalPages.value - 1) return
  currentPage.value = p
  pageInput.value = currentPage.value + 1
}

function jumpPage() {
  const max = Math.max(1, totalPages.value || 1)
  const target = Math.min(Math.max(1, pageInput.value || 1), max)
  setPage(target - 1)
}

/** load */
onMounted(getData)
watch(() => props.id, () => getData())

async function getData() {
  loading.value = true
  globalError.value = ''
  try {
    await Promise.all([loadAttributes(), loadVariants()])
    await loadPriceMaxFromDb()
    if (isPriceReady.value) syncPriceFilterToMax()
  } catch (e) {
    console.error(e)
    globalError.value = 'Lỗi tải dữ liệu'
  } finally {
    loading.value = false
  }
}

async function loadAttributes() {
  const [resSize, resColor] = await Promise.all([
    attributeService.getAllList('kich-co'),
    attributeService.getAllList('mau-sac')
  ])
  attributes.kichCo = (resSize?.data || resSize || []).filter((x) => x?.trangThai !== false)
  attributes.mauSac = (resColor?.data || resColor || []).filter((x) => x?.trangThai !== false)
}

async function loadVariants() {
  const res = await getByProductId(props.id)
  variants.value = (res?.data || res || []).map((v) => ({ ...v, __imgErr: false }))
}

async function loadPriceMaxFromDb() {
  try {
    const res = await getGiaMaxDb()
    const raw = res?.data ?? res
    const maxNum = typeof raw === 'object' ? Number(raw?.max ?? raw?.giaMax ?? raw?.value ?? 0) : Number(raw ?? 0)
    const maxDb = roundUpToStep(maxNum, PRICE_STEP)

    if (maxDb > 0) {
      priceMaxDb.value = maxDb
      return
    }
  } catch (e) {
    // ignore -> fallback dưới
  }

  // fallback: lấy max từ list biến thể (không default)
  const localMax = Math.max(...variants.value.map(v => Number(v.donGia ?? 0)), 0)
  const maxLocal = roundUpToStep(localMax, PRICE_STEP)
  priceMaxDb.value = maxLocal > 0 ? maxLocal : 0
}

/** nav */
function goBack() {
  router.push('/products')
}
function goToGlobalList() {
  router.push('/variants')
}
function scanQr() {
  console.log('scan qr')
}

/** format */
function formatPrice(val) {
  const n = Number(val ?? 0)
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n)
}

/** ================== EXCEL EXPORT (FE) ================== */
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
  const id = row?.id
  if (!id) return

  if (checked) {
    if (!selectedIds.value.includes(id)) selectedIds.value.push(id)
    selectedRows[id] = { ...row }
  } else {
    selectedIds.value = selectedIds.value.filter((x) => x !== id)
    delete selectedRows[id]
  }
}

const allVisibleSelected = computed(() => {
  if (!exportMode.value || pagedVariants.value.length === 0) return false
  return pagedVariants.value.every((v) => selectedIds.value.includes(v.id))
})

function toggleSelectAllVisible(checked) {
  pagedVariants.value.forEach((v) => toggleSelect(v, checked))
}

function safeName(s) {
  return String(s ?? '').trim().slice(0, 60).replace(/[^\w\-]+/g, '_')
}

function toExcelRow(v) {
  return {
    'Mã SP chi tiết': v.maSanPhamChiTiet ?? '',
    'Tên sản phẩm': productName.value ?? '',
    'Màu sắc': v.tenMauSac ?? '',
    'Kích cỡ': v.tenKichCo ?? '',
    'Số lượng tồn': Number(v.soLuongTon ?? 0),
    'Giá bán': Number(v.donGia ?? 0),
    'Trạng thái': v.trangThai ? 'Còn hàng' : 'Hết hàng',
    'Ảnh': v.anh ? buildImgUrl(v.anh) : ''
  }
}

function sleep(ms) {
  return new Promise((r) => setTimeout(r, ms))
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
      const fileName = `bien-the_${code}.xlsx`

      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = fileName
      document.body.appendChild(a)
      a.click()
      a.remove()
      URL.revokeObjectURL(url)

      // tránh browser chặn tải nhiều file quá nhanh
      await sleep(120)
    }

    cancelExportMode()
    success('Xuất Excel thành công')
  } catch (e) {
    console.error(e)
    error('Xuất Excel thất bại (có thể bị trình duyệt chặn nhiều download).')
  } finally {
    exporting.value = false
  }
}

/** ================== EDIT MODAL ================== */
const edit = reactive({
  open: false,
  saving: false,

  id: null,
  idSanPham: null,
  maSanPhamChiTiet: '',

  idKichCo: '',
  idMauSac: '',
  soLuongTon: 0,

  donGia: 0,
  donGiaText: '',

  ghiChu: '',
  trangThai: true,
  anh: ''
})

function openEditModal(v) {
  edit.open = true
  edit.saving = false

  edit.id = v.id
  edit.idSanPham = v.idSanPham
  edit.maSanPhamChiTiet = v.maSanPhamChiTiet ?? ''

  edit.idKichCo = String(v.idKichCo ?? '')
  edit.idMauSac = String(v.idMauSac ?? '')
  edit.soLuongTon = Number(v.soLuongTon ?? 0)

  edit.donGia = Number(v.donGia ?? 0)
  edit.donGiaText = formatDots(edit.donGia)

  edit.ghiChu = v.ghiChu ?? ''
  edit.trangThai = !!v.trangThai
  edit.anh = v.anh ?? ''
}

function closeEditModal() {
  edit.open = false
}

function parseDigits(text) {
  const digits = String(text || '').replace(/[^\d]/g, '')
  return digits ? Number(digits) : 0
}
function formatDots(n) {
  const x = Number(n || 0)
  if (!Number.isFinite(x) || x <= 0) return ''
  return String(Math.floor(x)).replace(/\B(?=(\d{3})+(?!\d))/g, '.')
}

function onEditMoneyTyping() {
  const num = parseDigits(edit.donGiaText)
  edit.donGia = num
  edit.donGiaText = num ? formatDots(num) : ''
}
function onEditMoneyBlur() {
  edit.donGiaText = edit.donGia ? formatDots(edit.donGia) : ''
}

async function handleFileUpload(event) {
  const file = event.target.files?.[0]
  if (!file) return
  try {
    const res = await uploadImage(file)
    const url = res?.data?.url || res?.data || res?.url
    if (!url) throw new Error('Upload không trả url')
    edit.anh = url
    success('Upload ảnh thành công!')
  } catch (e) {
    console.error(e)
    error('Lỗi upload ảnh')
  } finally {
    event.target.value = ''
  }
}

async function submitEdit() {
  if (!edit.id) return

  const sl = Number(edit.soLuongTon ?? 0)
  if (!edit.idKichCo || !edit.idMauSac) return error('Vui lòng chọn kích cỡ và màu sắc')
  if (!Number.isFinite(sl) || sl < 0) return error('Số lượng không hợp lệ')
  if (!Number.isFinite(edit.donGia) || edit.donGia < 0) return error('Đơn giá không hợp lệ')

  edit.saving = true
  try {
    await updateDetail(edit.id, {
      idSanPham: edit.idSanPham,
      idKichCo: edit.idKichCo,
      idMauSac: edit.idMauSac,
      soLuongTon: Math.floor(sl),
      donGia: Number(edit.donGia),
      ghiChu: edit.ghiChu,
      trangThai: !!edit.trangThai,
      anh: edit.anh
    })
    success('Cập nhật thành công')
    edit.open = false
    await loadVariants()
  } catch (e) {
    console.error(e)
    error('Cập nhật thất bại')
  } finally {
    edit.saving = false
  }
}

/** ================== TOGGLE STATUS ================== */
const togglingIds = reactive(new Set())

const confirmToggle = reactive({
  open: false,
  target: null,
  next: true
})

function openConfirmToggle(v) {
  if (togglingIds.has(v.id)) return
  confirmToggle.open = true
  confirmToggle.target = v
  confirmToggle.next = !v.trangThai
}

function closeConfirmToggle() {
  confirmToggle.open = false
  confirmToggle.target = null
}

async function confirmToggleNow() {
  const v = confirmToggle.target
  if (!v) return

  confirmToggle.open = false
  togglingIds.add(v.id)

  try {
    await updateDetail(v.id, {
      idSanPham: v.idSanPham,
      idKichCo: v.idKichCo,
      idMauSac: v.idMauSac,
      soLuongTon: v.soLuongTon,
      donGia: v.donGia,
      ghiChu: v.ghiChu ?? '',
      trangThai: confirmToggle.next,
      anh: v.anh ?? ''
    })
    success(`Đã đổi trạng thái thành ${confirmToggle.next ? 'Còn hàng' : 'Hết hàng'}`)
    await loadVariants()
  } catch (e) {
    console.error(e)
    error('Lỗi cập nhật trạng thái')
  } finally {
    togglingIds.delete(v.id)
    confirmToggle.target = null
  }
}

/** ================== COLOR DOT ================== */
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
.product-detail-page{
  padding: 20px;
  background:#ffffff;
  min-height: 100vh;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}

/* header */
.page-header{ display:flex; align-items:center; justify-content:space-between; gap:12px; margin-bottom:12px; }
.page-title h2{ margin:0; font-size:1.25rem; font-weight:700; color:#111827; }
.page-actions{ display:flex; gap:10px; flex-wrap:wrap; }

.card{ background:#fff; border-radius:8px; border:1px solid #e5e7eb; box-shadow:0 1px 3px rgba(0,0,0,.08); overflow:hidden; }

/* filter */
.filter-head{ background:#1e293b; color:#fff; padding:10px 14px; display:flex; justify-content:space-between; align-items:center; cursor:pointer; }
.filter-head-left{ display:flex; align-items:center; gap:10px; font-weight:700; }
.filter-head-right{ font-size:12px; opacity:.9; }
.filter-body{ padding:14px; }

.form-label{ font-size:13px; font-weight:700; color:#111827; margin-bottom:6px; }

.status-radio{ display:flex; align-items:center; gap:10px; font-size:13px; color:#111827; }
.status-radio input{ transform: translateY(1px); margin-right:6px; }

.price-label{ font-size:13px; font-weight:700; color:#111827; margin-bottom:6px; }
.price-green{ color:#059669; font-weight:800; }
.hint{ display:block; margin-top:6px; color:#6b7280; }

/* slider */
.range-slider{
  position: relative;
  width: 100%;
  height: 4px;
  margin-top: 14px;
  background: #e5e7eb;
  border-radius: 2px;
}
.slider-range{
  position:absolute;
  height:100%;
  background:#059669;
  border-radius:2px;
  z-index:1;
}
.range-slider input[type="range"]{
  position:absolute;
  width:100%;
  pointer-events:none;
  appearance:none;
  -webkit-appearance:none;
  z-index:2;
  height:5px;
  top:-1px;
  background:transparent;
  left:0;
}
.range-slider input[type="range"]::-webkit-slider-thumb{
  pointer-events:all;
  width:18px;
  height:18px;
  -webkit-appearance:none;
  background:#fff;
  border:2px solid #059669;
  border-radius:50%;
  cursor:pointer;
  margin-top:-7px;
  box-shadow:0 1px 3px rgba(0,0,0,.2);
}
.range-slider input[type="range"]::-webkit-slider-runnable-track{
  width:100%;
  height:4px;
  cursor:pointer;
  background:transparent;
  border-radius:2px;
}

.filter-reset{ display:flex; justify-content:flex-end; padding-top:6px; }
.filter-reset .btn{ color:#111827; }
.filter-reset .btn:hover{ background:#f3f4f6; border-radius:6px; }

/* table */
.table-card{ padding:0; border-radius:8px; overflow:hidden; margin-top: 12px; }
.table-responsive{ overflow-x:auto; overflow-y:hidden; }

.variants-table{ width:100%; table-layout:fixed; border-collapse:separate; border-spacing:0; margin:0; min-width: 1580px; }
.variants-table thead th{
  background:#1e293b; color:#fff;
  padding:10px 12px; text-align:center;
  font-weight:600; border-bottom:1px solid #e5e7eb;
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
.text-highlight{ color:#0f766e; font-weight:700; }

/* image */
.img-cell{ display:flex; align-items:center; justify-content:center; }
.img-cell--lg{ min-height: 220px; }
.variant-img{ object-fit: cover; border-radius: 6px; border: 1px solid #e5e7eb; background:#fff; }
.variant-img--lg{ width:200px; height:200px; }
.variant-img.preview{ width:140px; height:140px; }
.no-img{
  display:flex; align-items:center; justify-content:center;
  text-align:center;
  color:#6b7280;
  background:#f3f4f6;
  border-radius:6px;
  border:1px solid #e5e7eb;
  line-height:1.15;
}
.no-img--lg{ width:200px; height:200px; font-size:13px; padding:8px; }

/* color */
.color-cell{ display:inline-flex; align-items:center; justify-content:center; gap:10px; }
.color-dot{ width:18px; height:18px; border-radius:50%; border:1px solid #e5e7eb; }
.color-name{ max-width:120px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }

/* badge */
.badge-pill{
  display:inline-flex; align-items:center; justify-content:center;
  padding:4px 10px; border-radius:999px;
  font-size:12px; font-weight:800; white-space:nowrap;
}
.badge-success{ background:#d1fae5; color:#065f46; }
.badge-danger{ background:#fee2e2; color:#991b1b; }

/* action */
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

/* paging */
.paging-bar{
  margin:14px;
  display:grid;
  grid-template-columns: 1fr auto 1fr;
  align-items:center;
  gap:12px;
}
.paging-left{ justify-self:start; color:#6b7280; font-size:13px; }
.paging-center{ justify-self:center; display:flex; align-items:center; gap:10px; }
.paging-right{ justify-self:end; }
.paging-page{ width:120px; }
.paging-size{ width:160px; }

.error-text{ color:#b02a37; font-weight:800; }

/* modal */
.modal-overlay{
  position:fixed; inset:0;
  background: rgba(0,0,0,.5);
  display:flex; align-items:center; justify-content:center;
  z-index:9999;
}
.modals{
  background:#fff;
  width:560px;
  max-width: calc(100vw - 24px);
  border-radius:10px;
  border:1px solid #e5e7eb;
  box-shadow: 0 18px 50px rgba(0,0,0,.22);
  overflow:hidden;
}
.confirm-modal{ width: 520px; }
.modal-header{
  display:flex; justify-content:space-between; align-items:center;
  padding:12px 14px; border-bottom:1px solid #eef2f7;
}
.modal-header h3{ margin:0; font-size:16px; font-weight:800; color:#111827; }
.close-btn{ border:none; background:transparent; font-size:22px; line-height:1; cursor:pointer; color:#6b7280; }
.modal-body{ padding:14px; }
.modal-footer{
  display:flex; justify-content:flex-end; gap:10px;
  padding:12px 14px; border-top:1px solid #eef2f7;
}
</style>
