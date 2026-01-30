<template>
  <div class="product-detail-page">
    <!-- Header -->
    <div class="page-header">
      <div class="page-title">
        <h2>Quản lý sản phẩm / Chi tiết biến thể</h2>
      </div>

      <div class="page-actions">
        <button class="btn btn-outline-secondary btn-sm" type="button">
          <i class="bi bi-qr-code me-1"></i> Quét QR
        </button>

        <button class="btn btn-outline-primary btn-sm" type="button">
          <i class="bi bi-download me-1"></i> Tải Excel
        </button>

        <button class="btn btn-primary btn-sm" type="button" @click="goToGlobalList">
          <i class="bi bi-list-ul me-1"></i> Hiển thị đầy đủ biến thể
        </button>

        <button class="btn btn-secondary btn-sm" type="button" @click="goBack">
          <i class="bi bi-arrow-left me-1"></i> Quay lại
        </button>
      </div>
    </div>

    <div v-if="loading" class="text-center py-4">Đang tải dữ liệu...</div>

    <div v-else-if="globalError" class="text-center error-text py-4">
      {{ globalError }}
    </div>

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
                :value="filters.keyword"
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
                  {{ formatPrice(filters.priceMin) }} - {{ formatPrice(filters.priceMax) }}
                </span>
              </div>

              <div class="range-slider">
                <div class="slider-range" :style="rangeStyle"></div>

                <input
                  type="range"
                  min="0"
                  :max="priceMaxDb"
                  :step="PRICE_STEP"
                  v-model.number="filters.priceMin"
                  @input="onPriceInput('min')"
                  @change="applyFilters"
                />

                <input
                  type="range"
                  min="0"
                  :max="priceMaxDb"
                  :step="PRICE_STEP"
                  v-model.number="filters.priceMax"
                  @input="onPriceInput('max')"
                  @change="applyFilters"
                />
              </div>

              <small class="hint">Giá tối đa hiện tại: <b>{{ formatPrice(priceMaxDb) }}</b></small>
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
                <td class="text-center">{{ currentPage * pageSize + index + 1 }}</td>

                <td class="text-center">
                  <div class="img-cell img-cell--lg">
                    <img
                      v-if="v.anh && !v.__imgErr"
                      :src="buildImgUrl(v.anh)"
                      class="variant-img variant-img--lg"
                      alt="Ảnh biến thể"
                      @error="markImgError(v)"
                    />
                    <span v-else class="no-img no-img--lg">Ảnh biến thể</span>
                  </div>
                </td>

                <td class="text-center">{{ v.maSanPhamChiTiet || '-' }}</td>
                <td class="text-center text-bold">{{ product?.tenSanPham || '-' }}</td>

                <td class="text-center">
                  <div class="color-cell">
                    <span class="color-dot" :style="colorDotStyle(v)"></span>
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
                    <button
                      class="btn btn-outline-warning btn-sm edit-btn"
                      type="button"
                      title="Sửa"
                      @click="openEditModal(v)"
                    >
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
                <td colspan="10" class="text-center py-4">Không tìm thấy biến thể nào.</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div class="paging-bar" v-if="totalElements > 0">
          <div class="paging-left">Hiển thị {{ pagedVariants.length }} / tổng {{ totalElements }} bản ghi</div>

          <div class="paging-center">
            <button
              class="btn btn-outline-secondary btn-sm"
              :disabled="currentPage === 0"
              @click="setPage(currentPage - 1)"
              type="button"
            >
              ‹
            </button>

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

            <button
              class="btn btn-outline-secondary btn-sm"
              :disabled="currentPage >= totalPages - 1"
              @click="setPage(currentPage + 1)"
              type="button"
            >
              ›
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
                <option v-for="s in attributes.kichCo" :key="s.id" :value="s.id">
                  {{ s.soSize }}
                </option>
              </select>
            </div>

            <div class="col-6">
              <label class="form-label">Màu sắc</label>
              <select v-model="edit.idMauSac" class="form-select">
                <option v-for="c in attributes.mauSac" :key="c.id" :value="c.id">
                  {{ c.ten }}
                </option>
              </select>
            </div>

            <div class="col-6">
              <label class="form-label">Số lượng</label>
              <input
                type="number"
                v-model.number="edit.soLuongTon"
                class="form-control"
                min="0"
                step="1"
                @blur="normalizeQty"
              />
            </div>

            <div class="col-6">
              <label class="form-label">Đơn giá</label>
              <input
                type="text"
                inputmode="numeric"
                class="form-control"
                placeholder="Ví dụ: 999.999"
                :value="edit.donGiaText"
                @input="onEditMoneyInput"
                @blur="normalizeEditMoney"
              />
            </div>

            <div class="col-12">
              <label class="form-label">Trạng thái</label>
              <div class="status-radio">
                <label class="me-3">
                  <input type="radio" :value="true" v-model="edit.trangThai" />
                  Còn hàng
                </label>
                <label>
                  <input type="radio" :value="false" v-model="edit.trangThai" />
                  Hết hàng
                </label>
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
            ({{ product?.tenSanPham }} - {{ confirmToggle.target?.tenMauSac }} - {{ confirmToggle.target?.tenKichCo }})
            thành
            <b>{{ confirmToggle.next ? 'Còn hàng' : 'Hết hàng' }}</b>
            không?
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
import { ref, onMounted, reactive, computed, watch } from 'vue'
import { useRouter } from 'vue-router'

import attributeService from '../../services/attributeService'
import { getByProductId, updateDetail, uploadImage } from '../../services/sanPhamChiTietApi'
import { getGiaMaxDb } from '../../services/sanPhamApi'
import { useToast } from '../../composables/useToast'

const { success, error } = useToast()
const router = useRouter()

const props = defineProps({
  id: { type: [String, Number], required: true }
})

/* base url */
const apiBaseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
const fileBaseUrl = (import.meta.env.VITE_FILE_BASE_URL || apiBaseUrl).replace(/\/api\/?$/, '')

/* price */
const PRICE_STEP = 10000
const DEFAULT_MAX = 10000000
const priceMaxDb = ref(DEFAULT_MAX)

/* state */
const loading = ref(false)
const globalError = ref('')
const filterOpen = ref(true)

const product = ref(null)
const variants = ref([])

const attributes = reactive({ kichCo: [], mauSac: [] })

const filters = reactive({
  keyword: '',
  colorId: '',
  sizeId: '',
  stock: '',
  status: '',
  priceMin: 0,
  priceMax: DEFAULT_MAX
})

/* ========= helpers ========= */
function formatPrice(val) {
  const n = Number(val ?? 0)
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n)
}

function roundUpToStep(n, step) {
  const x = Number(n || 0)
  if (!Number.isFinite(x) || x <= 0) return 0
  return Math.ceil(x / step) * step
}

function onKeywordInput(e) {
  // ✅ chặn khoảng trắng ở đầu
  const v = String(e?.target?.value ?? '')
  filters.keyword = v.replace(/^\s+/, '')
}

function onPriceInput(which) {
  const max = Number(priceMaxDb.value || DEFAULT_MAX)

  if (filters.priceMin < 0) filters.priceMin = 0
  if (filters.priceMax > max) filters.priceMax = max

  if (filters.priceMin > filters.priceMax) {
    if (which === 'min') filters.priceMin = filters.priceMax
    else filters.priceMax = filters.priceMin
  }
}

const rangeStyle = computed(() => {
  const max = Math.max(1, Number(priceMaxDb.value || 1))
  const minV = Math.max(0, Math.min(filters.priceMin, max))
  const maxV = Math.max(0, Math.min(filters.priceMax, max))
  const left = (minV / max) * 100
  const width = ((maxV - minV) / max) * 100
  return { left: left + '%', width: width + '%' }
})

/* ========= filtering + paging ========= */
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

    const okStatus = filters.status === '' || (!!v.trangThai === (filters.status === 'true'))

    const sl = Number(v.soLuongTon ?? 0)
    let okStock = true
    if (filters.stock === 'lt10') okStock = sl < 10
    if (filters.stock === '10_50') okStock = sl >= 10 && sl <= 50
    if (filters.stock === '50_100') okStock = sl > 50 && sl <= 100
    if (filters.stock === 'gte100') okStock = sl >= 100

    const gia = Number(v.donGia ?? 0)
    const okPrice = gia >= fMin && gia <= fMax

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
  filters.priceMax = priceMaxDb.value

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

/* ========= navigation ========= */
function goBack() {
  router.push('/products')
}
function goToGlobalList() {
  router.push('/variants')
}

/* ========= load ========= */
onMounted(getData)
watch(() => props.id, () => getData())

async function getData() {
  loading.value = true
  globalError.value = ''
  try {
    await Promise.all([loadAttributes(), loadVariants(), loadProduct()])
    await loadPriceMaxDb() // ✅ lấy max giá từ DB (fallback nếu lỗi)
    syncFilterToMax()
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

async function loadProduct() {
  // giữ cách bạn đang dùng (nếu project bạn có API khác thì đổi ở đây)
  try {
    const resProd = await attributeService.getById('san-pham', props.id)
    product.value = resProd?.data ?? resProd
  } catch {
    product.value = { id: props.id, tenSanPham: '' }
  }
}

async function loadPriceMaxDb() {
  try {
    // có thể BE trả số trực tiếp hoặc {data: number} hoặc {data:{max:...}}
    const raw = await getGiaMaxDb()
    const data = raw?.data ?? raw
    const num =
      typeof data === 'object'
        ? Number(data?.max ?? data?.giaMax ?? data?.value ?? 0)
        : Number(data ?? 0)

    const maxDb = roundUpToStep(num, PRICE_STEP) || DEFAULT_MAX
    priceMaxDb.value = maxDb
  } catch (e) {
    // fallback: tính từ list biến thể
    const maxLocal = Math.max(...variants.value.map((v) => Number(v.donGia ?? 0)), 0)
    const maxDb = roundUpToStep(maxLocal, PRICE_STEP) || DEFAULT_MAX
    priceMaxDb.value = maxDb
  }
}

function syncFilterToMax() {
  const max = Number(priceMaxDb.value || DEFAULT_MAX)
  if (!filters.priceMax || filters.priceMax > max || filters.priceMax === DEFAULT_MAX) filters.priceMax = max
  if (filters.priceMin > filters.priceMax) filters.priceMin = filters.priceMax
}

/* ========= image ========= */
function buildImgUrl(path) {
  if (!path) return ''
  const p = String(path).replace(/\\/g, '/')
  if (p.startsWith('http://') || p.startsWith('https://')) return p
  const b = String(fileBaseUrl).replace(/\/+$/, '')
  return b + (p.startsWith('/') ? p : `/${p}`)
}
function markImgError(v) {
  v.__imgErr = true
}

/* ========= edit modal ========= */
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
  edit.maSanPhamChiTiet = v.maSanPhamChiTiet || ''

  edit.idKichCo = v.idKichCo
  edit.idMauSac = v.idMauSac
  edit.soLuongTon = Number(v.soLuongTon ?? 0)

  edit.donGia = Number(v.donGia ?? 0)
  edit.donGiaText = formatDotsNoSymbol(edit.donGia)

  edit.ghiChu = v.ghiChu ?? ''
  edit.trangThai = !!v.trangThai
  edit.anh = v.anh ?? ''
}

function closeEditModal() {
  edit.open = false
}

function normalizeQty() {
  let n = Number(edit.soLuongTon ?? 0)
  if (!Number.isFinite(n) || n < 0) n = 0
  edit.soLuongTon = Math.floor(n)
}

/* money input (999.999) */
function stripNonDigits(s) {
  return String(s ?? '').replace(/[^\d]/g, '')
}
function formatDotsNoSymbol(n) {
  const x = Number(n ?? 0)
  if (!Number.isFinite(x) || x <= 0) return ''
  return new Intl.NumberFormat('vi-VN').format(x)
}
function setEditMoneyFromRaw(raw) {
  const digits = stripNonDigits(raw)
  const n = digits ? Number(digits) : 0
  edit.donGia = n
  edit.donGiaText = digits ? formatDotsNoSymbol(n) : ''
}
function onEditMoneyInput(e) {
  setEditMoneyFromRaw(e?.target?.value ?? '')
}
function normalizeEditMoney() {
  setEditMoneyFromRaw(edit.donGiaText)
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

  normalizeQty()
  normalizeEditMoney()

  if (!edit.idKichCo || !edit.idMauSac) {
    error('Vui lòng chọn đầy đủ Kích cỡ và Màu sắc')
    return
  }
  if (edit.donGia < 0 || !Number.isFinite(edit.donGia)) {
    error('Đơn giá không hợp lệ')
    return
  }

  edit.saving = true
  try {
    await updateDetail(edit.id, {
      idSanPham: edit.idSanPham,
      idKichCo: edit.idKichCo,
      idMauSac: edit.idMauSac,
      soLuongTon: edit.soLuongTon,
      donGia: edit.donGia, // ✅ gửi number về BE
      ghiChu: edit.ghiChu,
      trangThai: edit.trangThai,
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

/* ========= toggle status + confirm ========= */
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

/* ========= color dot ========= */
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
  den: '#000000',
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
  'xanh la': '#16a34a',
  'xanh luc': '#16a34a',
  'xanh ngoc': '#14b8a6',
  'xanh duong': '#2563eb',
  'xanh navy': '#1e3a8a',
  'xanh than': '#1e3a8a',
  navy: '#1e3a8a'
}

function pickHexFromObject(obj) {
  const candidates = [obj?.maMau, obj?.maHex, obj?.hex, obj?.giaTri, obj?.value, obj?.code].filter(Boolean)
  for (const c of candidates) {
    const s = String(c).trim()
    if (/^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$/.test(s)) return s
  }
  return null
}

function hexByName(name) {
  if (!name) return '#9ca3af'
  const key = normalizeColorName(name)
  if (COLOR_MAP[key]) return COLOR_MAP[key]

  if (key.includes('navy') || key.includes('than')) return COLOR_MAP['xanh navy']
  if (key.includes('xanh') && (key.includes('la') || key.includes('luc'))) return COLOR_MAP['xanh la']
  if (key.includes('xanh') && key.includes('duong')) return COLOR_MAP['xanh duong']

  if (key.includes('den') || key.includes('black')) return COLOR_MAP.den
  if (key.includes('trang') || key.includes('white')) return COLOR_MAP.trang
  if (key.includes('do') || key.includes('red')) return COLOR_MAP.do
  if (key.includes('vang') || key.includes('yellow')) return COLOR_MAP.vang
  if (key.includes('cam') || key.includes('orange')) return COLOR_MAP.cam
  if (key.includes('hong') || key.includes('pink')) return COLOR_MAP.hong
  if (key.includes('tim') || key.includes('purple')) return COLOR_MAP.tim
  if (key.includes('nau') || key.includes('brown')) return COLOR_MAP.nau
  if (key.includes('xam') || key.includes('ghi') || key.includes('gray') || key.includes('grey')) return COLOR_MAP.xam

  return '#9ca3af'
}

function colorDotStyle(variant) {
  const fromAttr = attributes.mauSac?.find((x) => String(x.id) === String(variant.idMauSac))
  const hex = pickHexFromObject(fromAttr) || hexByName(fromAttr?.ten || variant.tenMauSac)
  const border = String(hex).toLowerCase() === '#ffffff' ? '#9ca3af' : '#e5e7eb'
  return { backgroundColor: hex, borderColor: border }
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

/* slider like danh sách sản phẩm */
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

.variants-table{ width:100%; table-layout:fixed; border-collapse:separate; border-spacing:0; margin:0; min-width: 1530px; }
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
.color-dot{ width:18px; height:18px; border-radius:50%; border:1px solid #e5e7eb; flex:0 0 18px; }
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
  background:#fff !important;
  border-color:#f59e0b !important;
  color:#f59e0b !important;
}
.edit-btn:hover{ background:#f59e0b !important; border-color:#f59e0b !important; color:#fff !important; }

/* switch */
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
