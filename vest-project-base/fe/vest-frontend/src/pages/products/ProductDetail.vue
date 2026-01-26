<template>
  <div class="product-detail-page">
    <!-- Header -->
    <div class="page-header">
      <div class="page-title">
        <h2>Quản lý sản phẩm / Danh sách biến thể</h2>
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

    <div v-else-if="product" class="content-wrapper">
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
                v-model.trim="filters.keyword"
                type="text"
                class="form-control"
                placeholder="Tìm theo mã, màu, kích cỡ..."
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

              <div class="double-range">
                <div class="double-range__track"></div>
                <div
                  class="double-range__range"
                  :style="{ left: leftPct + '%', right: (100 - rightPct) + '%' }"
                ></div>

                <input
                  class="double-range__thumb double-range__thumb--left"
                  type="range"
                  :min="PRICE_MIN"
                  :max="PRICE_MAX"
                  :step="PRICE_STEP"
                  v-model.number="filters.priceMin"
                  @input="onPriceInput('min')"
                  @change="applyFilters"
                />

                <input
                  class="double-range__thumb double-range__thumb--right"
                  type="range"
                  :min="PRICE_MIN"
                  :max="PRICE_MAX"
                  :step="PRICE_STEP"
                  v-model.number="filters.priceMax"
                  @input="onPriceInput('max')"
                  @change="applyFilters"
                />
              </div>
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
                  <i class="bi bi-arrow-clockwise me-1"></i> Reset
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
              <tr v-for="(variant, index) in pagedVariants" :key="variant.id">
                <td class="text-center">{{ currentPage * pageSize + index + 1 }}</td>

                <td class="text-center">
                  <div class="img-cell img-cell--lg">
                    <img
                      v-if="variant.anh && !variant.__imgErr"
                      :src="buildImgUrl(variant.anh)"
                      class="variant-img variant-img--lg"
                      alt="Ảnh biến thể"
                      @error="markImgError(variant)"
                    />
                    <span v-else class="no-img no-img--lg">Ảnh biến thể</span>
                  </div>
                </td>

                <td class="text-center">{{ variant.maSanPhamChiTiet }}</td>
                <td class="text-center text-bold">{{ product.tenSanPham }}</td>

                <td class="text-center">
                  <div class="color-cell">
                    <span class="color-dot" :style="colorDotStyle(variant)"></span>
                    <span class="color-name">{{ variant.tenMauSac }}</span>
                  </div>
                </td>

                <td class="text-center">{{ variant.tenKichCo }}</td>
                <td class="text-center">{{ variant.soLuongTon }}</td>
                <td class="text-center text-highlight">{{ formatPrice(variant.donGia) }}</td>

                <td class="text-center">
                  <span class="badge-pill" :class="variant.trangThai ? 'badge-success' : 'badge-danger'">
                    {{ variant.trangThai ? 'Còn hàng' : 'Hết hàng' }}
                  </span>
                </td>

                <td class="text-center">
                  <div class="action-buttons">
                    <button
                      class="btn btn-outline-warning btn-sm edit-btn"
                      type="button"
                      title="Sửa"
                      @click="openEditModal(variant)"
                    >
                      <i class="bi bi-pencil-square"></i>
                    </button>

                    <label class="switch" title="Đổi trạng thái">
                      <input
                        type="checkbox"
                        :checked="!!variant.trangThai"
                        :disabled="togglingIds.has(variant.id)"
                        @click.prevent="openConfirmToggle(variant)"
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

    <div v-else class="text-center error-text">
      {{ globalError || 'Không tìm thấy sản phẩm' }}
    </div>

    <!-- Edit Modal -->
    <div v-if="showEditModal" class="modal-overlay" @click.self="closeEditModal">
      <div class="modals">
        <div class="modal-header">
          <h3>Sửa biến thể: {{ editingVariant.maSanPhamChiTiet }}</h3>
          <button class="close-btn" @click="closeEditModal">×</button>
        </div>

        <div class="modal-body">
          <div class="row g-3">
            <div class="col-6">
              <label class="form-label">Kích cỡ</label>
              <select v-model="editingVariant.idKichCo" class="form-select">
                <option v-for="s in attributes.kichCo" :key="s.id" :value="s.id">
                  {{ s.soSize }}
                </option>
              </select>
            </div>

            <div class="col-6">
              <label class="form-label">Màu sắc</label>
              <select v-model="editingVariant.idMauSac" class="form-select">
                <option v-for="c in attributes.mauSac" :key="c.id" :value="c.id">
                  {{ c.ten }}
                </option>
              </select>
            </div>

            <div class="col-6">
              <label class="form-label">Số lượng</label>
              <input type="number" v-model.number="editingVariant.soLuongTon" class="form-control" min="0" />
            </div>

            <div class="col-6">
              <label class="form-label">Đơn giá</label>
              <input type="number" v-model.number="editingVariant.donGia" class="form-control" min="0" />
            </div>

            <div class="col-12">
              <label class="form-label">Trạng thái</label>
              <div class="status-radio">
                <label class="me-3">
                  <input type="radio" :value="true" v-model="editingVariant.trangThai" />
                  Còn hàng
                </label>
                <label>
                  <input type="radio" :value="false" v-model="editingVariant.trangThai" />
                  Hết hàng
                </label>
              </div>
            </div>

            <div class="col-12">
              <label class="form-label">Ảnh biến thể</label>
              <input type="file" @change="handleFileUpload" class="form-control" accept="image/*" />
              <div v-if="editingVariant.anh" class="mt-2">
                <img :src="buildImgUrl(editingVariant.anh)" class="variant-img preview" alt="Preview" />
              </div>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeEditModal">Hủy</button>
          <button class="btn btn-primary" @click="submitEdit">Lưu thay đổi</button>
        </div>
      </div>
    </div>

    <!-- Confirm Toggle Modal -->
    <div v-if="showConfirmToggle" class="modal-overlay" @click.self="closeConfirmToggle">
      <div class="modals confirm-modal">
        <div class="modal-header">
          <h3>Xác nhận đổi trạng thái</h3>
          <button class="close-btn" @click="closeConfirmToggle">×</button>
        </div>

        <div class="modal-body">
          <p class="mb-2">
            Bạn có chắc muốn đổi trạng thái biến thể
            <b>{{ confirmTarget?.maSanPhamChiTiet }}</b>
            ({{ product?.tenSanPham }} - {{ confirmTarget?.tenMauSac }} - {{ confirmTarget?.tenKichCo }})
            thành
            <b>{{ confirmNext ? 'Còn hàng' : 'Hết hàng' }}</b>
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
import { useToast } from '../../composables/useToast'

const { success, error } = useToast()
const router = useRouter()

const props = defineProps({
  id: { type: [String, Number], required: true }
})

/* base url (ảnh thường nằm ngoài /api) */
const apiBaseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
const fileBaseUrl = (import.meta.env.VITE_FILE_BASE_URL || apiBaseUrl).replace(/\/api\/?$/, '')

/* constants */
const PRICE_MIN = 0
const PRICE_MAX = 10000000
const PRICE_STEP = 10000

/* state */
const product = ref(null)
const variants = ref([])
const loading = ref(false)
const globalError = ref('')
const filterOpen = ref(true)

const attributes = reactive({ kichCo: [], mauSac: [] })
const filters = reactive({
  keyword: '',
  colorId: '',
  sizeId: '',
  stock: '',
  status: '',
  priceMin: PRICE_MIN,
  priceMax: PRICE_MAX
})

/* slider percent */
const leftPct = computed(() => ((filters.priceMin - PRICE_MIN) / (PRICE_MAX - PRICE_MIN)) * 100)
const rightPct = computed(() => ((filters.priceMax - PRICE_MIN) / (PRICE_MAX - PRICE_MIN)) * 100)

function onPriceInput(which) {
  if (filters.priceMin < PRICE_MIN) filters.priceMin = PRICE_MIN
  if (filters.priceMax > PRICE_MAX) filters.priceMax = PRICE_MAX
  if (filters.priceMin > filters.priceMax) {
    if (which === 'min') filters.priceMin = filters.priceMax
    else filters.priceMax = filters.priceMin
  }
}

/* filtering */
const filteredVariants = computed(() => {
  const kw = String(filters.keyword || '').toLowerCase().trim()

  return (variants.value || []).filter((v) => {
    const okKw =
      !kw ||
      String(v.maSanPhamChiTiet || '').toLowerCase().includes(kw) ||
      String(v.tenMauSac || '').toLowerCase().includes(kw) ||
      String(v.tenKichCo || '').toLowerCase().includes(kw)

    const okColor =
      !filters.colorId ||
      String(v.idMauSac ?? '') === String(filters.colorId) ||
      String(v.tenMauSac ?? '') === String(findColorNameById(filters.colorId) ?? '')

    const okSize =
      !filters.sizeId ||
      String(v.idKichCo ?? '') === String(filters.sizeId) ||
      String(v.tenKichCo ?? '') === String(findSizeNameById(filters.sizeId) ?? '')

    const okStatus = filters.status === '' || !!v.trangThai === (filters.status === 'true')

    const sl = Number(v.soLuongTon ?? 0)
    let okStock = true
    if (filters.stock === 'lt10') okStock = sl < 10
    if (filters.stock === '10_50') okStock = sl >= 10 && sl <= 50
    if (filters.stock === '50_100') okStock = sl > 50 && sl <= 100
    if (filters.stock === 'gte100') okStock = sl >= 100

    const gia = Number(v.donGia ?? 0)
    const okPrice = gia >= Number(filters.priceMin) && gia <= Number(filters.priceMax)

    return okKw && okColor && okSize && okStatus && okStock && okPrice
  })
})

/* paging */
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
function resetFilters() {
  filters.keyword = ''
  filters.colorId = ''
  filters.sizeId = ''
  filters.stock = ''
  filters.status = ''
  filters.priceMin = PRICE_MIN
  filters.priceMax = PRICE_MAX
  applyFilters()
}

/* navigation */
function goBack() {
  router.push('/products')
}
function goToGlobalList() {
  router.push('/variants')
}

/* load data */
onMounted(getData)
watch(() => props.id, () => getData())

async function getData() {
  loading.value = true
  globalError.value = ''
  try {
    const resProd = await attributeService.getById('san-pham', props.id)
    product.value = resProd.data

    const [_, resSize, resColor] = await Promise.all([
      loadVariants(),
      attributeService.getAllList('kich-co'),
      attributeService.getAllList('mau-sac')
    ])

    attributes.kichCo = resSize.data || []
    attributes.mauSac = resColor.data || []
  } catch (e) {
    console.error(e)
    globalError.value = 'Lỗi tải dữ liệu'
  } finally {
    loading.value = false
  }
}

async function loadVariants() {
  const res = await getByProductId(props.id)
  variants.value = (res.data || []).map(v => ({ ...v, __imgErr: false }))
}

/* image */
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

/* edit modal + upload */
const showEditModal = ref(false)
const editingVariant = reactive({
  id: null,
  idSanPham: null,
  idKichCo: '',
  idMauSac: '',
  soLuongTon: 0,
  donGia: 0,
  ghiChu: '',
  trangThai: true,
  anh: '',
  maSanPhamChiTiet: ''
})

function openEditModal(v) {
  editingVariant.id = v.id
  editingVariant.idSanPham = v.idSanPham
  editingVariant.maSanPhamChiTiet = v.maSanPhamChiTiet
  editingVariant.idKichCo = v.idKichCo
  editingVariant.idMauSac = v.idMauSac
  editingVariant.soLuongTon = v.soLuongTon
  editingVariant.donGia = v.donGia
  editingVariant.ghiChu = v.ghiChu ?? ''
  editingVariant.trangThai = !!v.trangThai
  editingVariant.anh = v.anh ?? ''
  showEditModal.value = true
}

function closeEditModal() {
  showEditModal.value = false
}

async function handleFileUpload(event) {
  const file = event.target.files?.[0]
  if (!file) return
  try {
    const res = await uploadImage(file)
    const url = res?.data?.url || res?.data || res?.url
    if (!url) throw new Error('Upload không trả url')
    editingVariant.anh = url
    success('Upload ảnh thành công!')
  } catch (e) {
    console.error(e)
    error('Lỗi upload ảnh')
  }
}

async function submitEdit() {
  if (!editingVariant.id) return
  try {
    await updateDetail(editingVariant.id, {
      idSanPham: editingVariant.idSanPham,
      idKichCo: editingVariant.idKichCo,
      idMauSac: editingVariant.idMauSac,
      soLuongTon: editingVariant.soLuongTon,
      donGia: editingVariant.donGia,
      ghiChu: editingVariant.ghiChu,
      trangThai: editingVariant.trangThai,
      anh: editingVariant.anh
    })
    success('Cập nhật thành công')
    showEditModal.value = false
    await loadVariants()
  } catch (e) {
    console.error(e)
    error('Cập nhật thất bại')
  }
}

/* toggle status + confirm */
const togglingIds = reactive(new Set())

const showConfirmToggle = ref(false)
const confirmTarget = ref(null)
const confirmNext = ref(true)

function openConfirmToggle(v) {
  if (togglingIds.has(v.id)) return
  confirmTarget.value = v
  confirmNext.value = !v.trangThai
  showConfirmToggle.value = true
}

function closeConfirmToggle() {
  showConfirmToggle.value = false
  confirmTarget.value = null
}

async function confirmToggleNow() {
  const v = confirmTarget.value
  if (!v) return
  showConfirmToggle.value = false

  togglingIds.add(v.id)
  try {
    await updateDetail(v.id, {
      idSanPham: v.idSanPham,
      idKichCo: v.idKichCo,
      idMauSac: v.idMauSac,
      soLuongTon: v.soLuongTon,
      donGia: v.donGia,
      ghiChu: v.ghiChu ?? '',
      trangThai: confirmNext.value,
      anh: v.anh ?? ''
    })
    v.trangThai = confirmNext.value
    success(`Đã đổi trạng thái thành ${confirmNext.value ? 'Còn hàng' : 'Hết hàng'}`)
    await loadVariants()
  } catch (e) {
    console.error(e)
    error('Lỗi cập nhật trạng thái')
  } finally {
    togglingIds.delete(v.id)
    confirmTarget.value = null
  }
}

/* helpers */
function formatPrice(val) {
  const n = Number(val ?? 0)
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n)
}

function findColorNameById(id) {
  const it = attributes.mauSac?.find(x => String(x.id) === String(id))
  return it?.ten ?? ''
}
function findSizeNameById(id) {
  const it = attributes.kichCo?.find(x => String(x.id) === String(id))
  return it?.soSize ?? it?.ten ?? ''
}

/* =========================
   ✅ CÁCH 2: MÀU THEO TÊN
   ========================= */

/** Chuẩn hoá: bỏ dấu + đ->d + bỏ ngoặc + trim space */
function normalizeColorName(name) {
  return String(name || '')
    .trim()
    .toLowerCase()
    .replace(/đ/g, 'd')              // ✅ fix "Đen" -> "den"
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '') // bỏ dấu
    .replace(/\(.*?\)/g, '')         // bỏ phần trong ngoặc
    .replace(/\s+/g, ' ')
    .trim()
}

/** Map màu theo tên (key là tên đã chuẩn hoá) */
const COLOR_MAP = {
  den: '#000000',
  trang: '#ffffff',
  xam: '#9ca3af',
  ghi: '#9ca3af',

  do: '#ef4444',
  'do do': '#7f1d1d',
  'do ruou': '#7f1d1d',

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
  navy: '#1e3a8a',

  cyan: '#06b6d4'
}

/** Nếu bảng màu_sắc có field mã màu hex thì ưu tiên lấy */
function pickHexFromObject(obj) {
  const candidates = [obj?.maMau, obj?.maHex, obj?.hex, obj?.giaTri, obj?.value, obj?.code].filter(Boolean)
  for (const c of candidates) {
    const s = String(c).trim()
    if (/^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$/.test(s)) return s
  }
  return null
}

/** Lấy hex theo tên: match exact -> fallback theo keyword */
function hexByName(name) {
  if (!name) return '#9ca3af'
  const key = normalizeColorName(name)

  // exact match
  if (COLOR_MAP[key]) return COLOR_MAP[key]

  // fallback keyword (để đỡ phải map hết mọi biến thể)
  if (key.includes('navy') || key.includes('than')) return COLOR_MAP['xanh navy']
  if (key.includes('xanh') && (key.includes('la') || key.includes('luc'))) return COLOR_MAP['xanh la']
  if (key.includes('xanh') && key.includes('duong')) return COLOR_MAP['xanh duong']
  if (key.includes('xanh') && key.includes('ngoc')) return COLOR_MAP['xanh ngoc']

  if (key.includes('den') || key.includes('black')) return COLOR_MAP.den
  if (key.includes('trang') || key.includes('white')) return COLOR_MAP.trang
  if (key.includes('do') || key.includes('red')) return COLOR_MAP.do
  if (key.includes('vang') || key.includes('yellow')) return COLOR_MAP.vang
  if (key.includes('cam') || key.includes('orange')) return COLOR_MAP.cam
  if (key.includes('hong') || key.includes('pink')) return COLOR_MAP.hong
  if (key.includes('tim') || key.includes('purple')) return COLOR_MAP.tim
  if (key.includes('nau') || key.includes('brown')) return COLOR_MAP.nau
  if (key.includes('xam') || key.includes('ghi') || key.includes('gray') || key.includes('grey')) return COLOR_MAP.xam

  // default
  return '#9ca3af'
}

/** Style chấm màu: ưu tiên hex từ attribute.mauSac, không có thì dùng tên */
function colorDotStyle(variant) {
  const fromAttr = attributes.mauSac?.find(x => String(x.id) === String(variant.idMauSac))
  const hex = pickHexFromObject(fromAttr) || hexByName(fromAttr?.ten || variant.tenMauSac)

  // viền cho màu trắng để nhìn thấy
  const border = String(hex).toLowerCase() === '#ffffff' ? '#9ca3af' : '#e5e7eb'
  return { backgroundColor: hex, borderColor: border }
}
</script>

<style scoped>
.product-detail-page{
  padding: 20px;
  background:#f3f4f6;
  min-height: 100vh;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}

/* header */
.page-header{ display:flex; align-items:center; justify-content:space-between; gap:12px; margin-bottom:12px; }
.page-title h2{ margin:0; font-size:1.25rem; font-weight:700; color:#111827; }
.page-actions{ display:flex; gap:10px; flex-wrap:wrap; }

/* card */
.card{ background:#fff; border-radius:8px; border:1px solid #e5e7eb; box-shadow:0 1px 3px rgba(0,0,0,.08); }
.filter-card{ overflow:hidden; }
.table-card{ padding:14px; }

/* filter head */
.filter-head{ background:#1e293b; color:#fff; padding:10px 14px; display:flex; justify-content:space-between; align-items:center; cursor:pointer; }
.filter-head-left{ display:flex; align-items:center; gap:10px; font-weight:700; }
.filter-head-right{ font-size:12px; opacity:.9; }
.filter-body{ padding:14px; }

.form-label{ font-size:13px; font-weight:700; color:#111827; margin-bottom:6px; }

/* reset */
.filter-reset{ display:flex; justify-content:flex-end; padding-top:6px; }
.filter-reset .btn{ color:#111827; }
.filter-reset .btn:hover{ background:#f3f4f6; border-radius:6px; }

/* status radio */
.status-radio{ display:flex; align-items:center; gap:10px; font-size:13px; color:#111827; }
.status-radio input{ transform: translateY(1px); margin-right:6px; }

/* price slider */
.price-label{ font-size:13px; font-weight:700; color:#111827; margin-bottom:6px; }
.price-green{ color:#059669; font-weight:800; }

.double-range{ position:relative; height:26px; }
.double-range__track{ position:absolute; left:0; right:0; top:12px; height:4px; background:#e5e7eb; border-radius:999px; }
.double-range__range{ position:absolute; top:12px; height:4px; background:#16a34a; border-radius:999px; }
.double-range__thumb{ position:absolute; left:0; top:0; width:100%; pointer-events:none; -webkit-appearance:none; appearance:none; background:transparent; height:26px; margin:0; }
.double-range__thumb::-webkit-slider-thumb{
  pointer-events:auto; -webkit-appearance:none; appearance:none;
  width:14px; height:14px; border-radius:50%;
  background:#fff; border:2px solid #16a34a;
  box-shadow:0 1px 2px rgba(0,0,0,.15);
}
.double-range__thumb::-moz-range-thumb{
  pointer-events:auto;
  width:14px; height:14px; border-radius:50%;
  background:#fff; border:2px solid #16a34a;
  box-shadow:0 1px 2px rgba(0,0,0,.15);
}

/* TABLE */
.variants-table{ width:100%; table-layout:fixed; border-collapse:separate; border-spacing:0; }
.variants-table thead th{
  background:#1e293b; color:#fff;
  padding:10px 12px; text-align:center;
  font-weight:700; border-bottom:1px solid #e5e7eb;
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
.text-highlight{ color:#0f766e; font-weight:800; }

/* IMAGE (to 200x200) */
.img-cell{ display:flex; align-items:center; justify-content:center; }
.img-cell--lg{ min-height: 220px; }

.variant-img{
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  background:#fff;
}
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
  margin-top:14px;
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
