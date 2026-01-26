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
            <!-- Row 1 -->
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

            <!-- Row 2 -->
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

            <!-- Reset bottom-right -->
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
                <!-- ✅ Ảnh trước mã -->
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
                <td class="text-center">
                  {{ currentPage * pageSize + index + 1 }}
                </td>

                <!-- ✅ Ảnh -->
                <td class="text-center">
                  <div class="img-cell">
                    <img
                      v-if="variant.anh"
                      :src="buildImgUrl(variant.anh)"
                      class="variant-img"
                      alt="Ảnh biến thể"
                      @error="onImgError"
                    />
                    <span v-else class="no-img">Ảnh biến thể</span>
                  </div>
                </td>

                <!-- ✅ Mã -->
                <td class="text-center">{{ variant.maSanPhamChiTiet }}</td>

                <td class="text-center text-bold">
                  {{ product.tenSanPham }}
                </td>

                <td class="text-center">
                  <div class="color-cell">
                    <span class="color-dot" :style="colorDotStyle(variant)"></span>
                    <span class="color-name">{{ variant.tenMauSac }}</span>
                  </div>
                </td>

                <td class="text-center">{{ variant.tenKichCo }}</td>
                <td class="text-center">{{ variant.soLuongTon }}</td>

                <td class="text-center text-highlight">
                  {{ formatPrice(variant.donGia) }}
                </td>

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
                        @change="toggleStatus(variant)"
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
          <div class="paging-left">
            Hiển thị {{ pagedVariants.length }} / tổng {{ totalElements }} bản ghi
          </div>

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
                <img :src="buildImgUrl(editingVariant.anh)" class="variant-img preview" alt="Preview" @error="onImgError" />
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

/* ===== API ===== */
const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'

/* ===== constants ===== */
const PRICE_MIN = 0
const PRICE_MAX = 10000000
const PRICE_STEP = 10000

/* ===== state ===== */
const product = ref(null)
const variants = ref([])
const loading = ref(false)
const globalError = ref('')
const filterOpen = ref(true)

const attributes = reactive({
  kichCo: [],
  mauSac: []
})

const filters = reactive({
  keyword: '',
  colorId: '',
  sizeId: '',
  stock: '',
  status: '',
  priceMin: PRICE_MIN,
  priceMax: PRICE_MAX
})

/* ===== slider percent ===== */
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

/* ===== filtering ===== */
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

/* ===== paging ===== */
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

/* ===== navigation ===== */
function goBack() {
  router.push('/products')
}
function goToGlobalList() {
  router.push('/variants')
}

/* ===== load data ===== */
onMounted(getData)
watch(() => props.id, () => getData())

async function getData() {
  loading.value = true
  globalError.value = ''
  try {
    const resProd = await attributeService.getById('san-pham', props.id)
    product.value = resProd.data

    await loadVariants()

    const [resSize, resColor] = await Promise.all([
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
  variants.value = res.data || []
}

/* ===== edit modal + upload ===== */
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
  editingVariant.trangThai = v.trangThai
  editingVariant.anh = v.anh
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
    editingVariant.anh = res.data.url
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

/* ===== toggle status ===== */
const togglingIds = reactive(new Set())

async function toggleStatus(variant) {
  if (togglingIds.has(variant.id)) return

  const next = !variant.trangThai
  const old = !!variant.trangThai

  togglingIds.add(variant.id)
  variant.trangThai = next

  try {
    await updateDetail(variant.id, {
      idSanPham: variant.idSanPham,
      idKichCo: variant.idKichCo,
      idMauSac: variant.idMauSac,
      soLuongTon: variant.soLuongTon,
      donGia: variant.donGia,
      ghiChu: variant.ghiChu,
      trangThai: next,
      anh: variant.anh
    })
    await loadVariants()
    success(`Đã đổi trạng thái biến thể thành ${next ? 'Còn hàng' : 'Hết hàng'}`)
  } catch (e) {
    console.error(e)
    variant.trangThai = old
    error('Lỗi cập nhật trạng thái')
  } finally {
    togglingIds.delete(variant.id)
  }
}

/* ===== helpers ===== */
function formatPrice(val) {
  const n = Number(val ?? 0)
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n)
}

function buildImgUrl(path) {
  if (!path) return ''
  const p = String(path)
  if (p.startsWith('http://') || p.startsWith('https://')) return p
  const b = String(baseUrl).replace(/\/+$/, '')
  const x = p.startsWith('/') ? p : `/${p}`
  return b + x
}

function onImgError(e) {
  // ẩn ảnh lỗi -> hiện placeholder
  e.target.style.display = 'none'
}

function findColorNameById(id) {
  const it = attributes.mauSac?.find(x => String(x.id) === String(id))
  return it?.ten ?? ''
}

function findSizeNameById(id) {
  const it = attributes.kichCo?.find(x => String(x.id) === String(id))
  return it?.soSize ?? it?.ten ?? ''
}

function pickHexFromObject(obj) {
  const candidates = [
    obj?.maMau,
    obj?.maHex,
    obj?.hex,
    obj?.giaTri,
    obj?.value,
    obj?.code,
    obj?.rgb
  ].filter(Boolean)

  for (const c of candidates) {
    const s = String(c).trim()
    if (/^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$/.test(s)) return s
  }
  return null
}

function fallbackColorToHex(name) {
  if (!name) return '#9ca3af'
  const n = String(name).toLowerCase().trim()
  if (n.includes('đen') || n.includes('black')) return '#000000'
  if (n.includes('trắng') || n.includes('white')) return '#ffffff'
  if (n.includes('navy') || n.includes('xanh navy') || n.includes('xanh than')) return '#1d4ed8'
  if (n.includes('xanh dương') || n.includes('blue')) return '#2563eb'
  if (n.includes('xanh lá') || n.includes('green')) return '#16a34a'
  if (n.includes('đỏ') || n.includes('red')) return '#ef4444'
  if (n.includes('vàng') || n.includes('yellow')) return '#f59e0b'
  if (n.includes('xám') || n.includes('ghi') || n.includes('gray') || n.includes('grey')) return '#6b7280'
  if (n.includes('xanh')) return '#16a34a'
  return '#9ca3af'
}

function colorDotStyle(variant) {
  const fromAttr = attributes.mauSac?.find(x => String(x.id) === String(variant.idMauSac))
  const hex = pickHexFromObject(fromAttr) || fallbackColorToHex(variant.tenMauSac)
  const border = hex.toLowerCase() === '#ffffff' ? '#9ca3af' : '#e5e7eb'
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
.page-header{
  display:flex;
  align-items:center;
  justify-content:space-between;
  gap: 12px;
  margin-bottom: 12px;
}
.page-title h2{
  margin:0;
  font-size: 1.25rem;
  font-weight: 700;
  color:#111827;
}
.page-actions{
  display:flex;
  gap:10px;
  flex-wrap: wrap;
}

/* card */
.card{
  background:#fff;
  border-radius:8px;
  border:1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0,0,0,.08);
}
.filter-card{ overflow:hidden; }
.table-card{ padding: 14px; }

/* filter head */
.filter-head{
  background:#1e293b;
  color:#fff;
  padding: 10px 14px;
  display:flex;
  justify-content:space-between;
  align-items:center;
  cursor:pointer;
}
.filter-head-left{
  display:flex;
  align-items:center;
  gap:10px;
  font-weight:700;
}
.filter-head-right{
  font-size: 12px;
  opacity: .9;
}
.filter-body{ padding: 14px; }

.form-label{
  font-size: 13px;
  font-weight: 700;
  color:#111827;
  margin-bottom: 6px;
}

/* reset */
.filter-reset{
  display:flex;
  justify-content:flex-end;
  padding-top: 6px;
}
.filter-reset .btn{ color:#111827; }
.filter-reset .btn:hover{
  background: #f3f4f6;
  border-radius: 6px;
}

/* status radio */
.status-radio{
  display:flex;
  align-items:center;
  gap: 10px;
  font-size: 13px;
  color:#111827;
}
.status-radio input{
  transform: translateY(1px);
  margin-right: 6px;
}

/* price slider */
.price-label{
  font-size: 13px;
  font-weight: 700;
  color:#111827;
  margin-bottom: 6px;
}
.price-green{ color:#059669; font-weight: 800; }

.double-range{ position: relative; height: 26px; }
.double-range__track{
  position:absolute; left:0; right:0; top: 12px;
  height: 4px; background:#e5e7eb; border-radius: 999px;
}
.double-range__range{
  position:absolute; top: 12px;
  height: 4px; background:#16a34a; border-radius: 999px;
}
.double-range__thumb{
  position:absolute; left:0; top:0;
  width:100%;
  pointer-events:none;
  -webkit-appearance:none; appearance:none;
  background: transparent;
  height: 26px;
  margin:0;
}
.double-range__thumb::-webkit-slider-thumb{
  pointer-events:auto;
  -webkit-appearance:none; appearance:none;
  width: 14px; height: 14px;
  border-radius: 50%;
  background:#fff;
  border: 2px solid #16a34a;
  box-shadow: 0 1px 2px rgba(0,0,0,.15);
}
.double-range__thumb::-moz-range-thumb{
  pointer-events:auto;
  width: 14px; height: 14px;
  border-radius: 50%;
  background:#fff;
  border: 2px solid #16a34a;
  box-shadow: 0 1px 2px rgba(0,0,0,.15);
}

/* ===== TABLE: căn đều, không nhảy cột ===== */
.variants-table{
  width:100%;
  table-layout: fixed;
  border-collapse: separate;
  border-spacing: 0;
}
.variants-table thead th{
  background:#1e293b;
  color:#fff;
  padding: 10px 12px;
  text-align:center;
  font-weight:700;
  border-bottom: 1px solid #e5e7eb;
  white-space: nowrap;
}
.variants-table td{
  padding: 10px 12px;
  border-bottom: 1px solid #eef2f7;
  vertical-align: middle !important;
  color:#374151;
}

/* widths giống ảnh + cân đối */
.col-stt{ width: 70px; }
.col-img{ width: 130px; }     /* ✅ ảnh to vừa đủ, hàng đều */
.col-code{ width: 160px; }
.col-name{ width: 220px; }
.col-color{ width: 170px; }
.col-size{ width: 120px; }
.col-stock{ width: 120px; }
.col-price{ width: 140px; }
.col-status{ width: 120px; }
.col-action{ width: 150px; }

.text-center { text-align: center; }
.text-bold{ font-weight:700; color:#111827; }
.text-highlight{ color:#0f766e; font-weight:800; }

/* image cell fixed => hàng không lệch */
.img-cell{
  display:flex;
  align-items:center;
  justify-content:center;
  min-height: 88px;            /* ✅ đảm bảo hàng đều */
}
.variant-img{
  width: 84px;
  height: 84px;
  object-fit: cover;
  border-radius: 6px;
  border:1px solid #e5e7eb;
  background: #fff;
}
.variant-img.preview{
  width: 140px;
  height: 140px;
}
.no-img{
  width: 84px;
  height: 84px;
  display:flex;
  align-items:center;
  justify-content:center;
  text-align:center;
  font-size: 12px;
  color:#6b7280;
  background:#f3f4f6;
  border-radius: 6px;
  border:1px solid #e5e7eb;
  line-height: 1.15;
  padding: 6px;
}

/* color cell */
.color-cell{
  display:inline-flex;
  align-items:center;
  justify-content:center;
  gap: 10px;
}
.color-dot{
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 1px solid #e5e7eb;
  flex: 0 0 18px;
}
.color-name{
  max-width: 120px;
  overflow:hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* badge */
.badge-pill{
  display:inline-flex;
  align-items:center;
  justify-content:center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight:800;
  white-space: nowrap;
}
.badge-success{ background:#d1fae5; color:#065f46; }
.badge-danger{ background:#fee2e2; color:#991b1b; }

/* action */
.action-buttons{
  display:inline-flex;
  align-items:center;
  justify-content:center;
  gap: 10px;
}

/* edit button */
.edit-btn{
  width: 32px;
  height: 32px;
  padding: 0;
  display:inline-flex;
  align-items:center;
  justify-content:center;
  border-radius: 4px;
  background:#fff !important;
  border-color:#f59e0b !important;
  color:#f59e0b !important;
  line-height: 1;
}
.edit-btn:hover{
  background:#f59e0b !important;
  border-color:#f59e0b !important;
  color:#fff !important;
}

/* switch */
.switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 22px;
}
.switch input { opacity: 0; width: 0; height: 0; }
.slider {
  position: absolute;
  inset: 0;
  cursor: pointer;
  background: #d1d5db;
  transition: 0.2s;
  border-radius: 999px;
}
.slider:before {
  content: "";
  position: absolute;
  height: 18px;
  width: 18px;
  left: 2px;
  top: 2px;
  background: #fff;
  transition: 0.2s;
  border-radius: 50%;
  box-shadow: 0 1px 2px rgba(0,0,0,0.15);
}
.switch input:checked + .slider { background: #2563eb; }
.switch input:checked + .slider:before { transform: translateX(22px); }

/* paging */
.paging-bar{
  margin-top: 14px;
  display:grid;
  grid-template-columns: 1fr auto 1fr;
  align-items:center;
  gap:12px;
}
.paging-left{ justify-self:start; color:#6b7280; font-size: 13px; }
.paging-center{ justify-self:center; display:flex; align-items:center; gap:10px; }
.paging-right{ justify-self:end; }
.paging-page{ width: 120px; }
.paging-size{ width: 160px; }

/* modal */
.modal-overlay{
  position: fixed;
  inset:0;
  background: rgba(0,0,0,.5);
  display:flex;
  align-items:center;
  justify-content:center;
  z-index: 9999;
}
.modals{
  background:#fff;
  width: 560px;
  max-width: calc(100vw - 24px);
  border-radius: 10px;
  border:1px solid #e5e7eb;
  box-shadow: 0 18px 50px rgba(0,0,0,.22);
  overflow:hidden;
}
.modal-header{
  display:flex;
  justify-content:space-between;
  align-items:center;
  padding: 12px 14px;
  border-bottom:1px solid #eef2f7;
}
.modal-header h3{
  margin:0;
  font-size: 16px;
  font-weight:800;
  color:#111827;
}
.close-btn{
  border:none;
  background:transparent;
  font-size: 22px;
  line-height: 1;
  cursor:pointer;
  color:#6b7280;
}
.modal-body{ padding: 14px; }
.modal-footer{
  display:flex;
  justify-content:flex-end;
  gap:10px;
  padding: 12px 14px;
  border-top:1px solid #eef2f7;
}
</style>
