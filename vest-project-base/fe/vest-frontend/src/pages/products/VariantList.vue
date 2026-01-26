<template>
  <div class="variant-page">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div>
        <h5 class="mb-0">Quản lý sản phẩm / Danh sách biến thể</h5>
      </div>

      <div class="d-flex gap-2 flex-wrap">
        <button class="btn btn-outline-secondary btn-sm" @click="showQrModal = true">
          <i class="bi bi-qr-code-scan me-1"></i>Quét QR
        </button>
        <button class="btn btn-outline-primary btn-sm" @click="showExportModal = true">
          <i class="bi bi-download me-1"></i>Tải Excel
        </button>
        <button class="btn btn-primary btn-sm" @click="resetFilters">
          <i class="bi bi-list-check me-1"></i>Hiển thị đầy đủ biến thể
        </button>
        <button class="btn btn-secondary btn-sm" @click="goBack">
          <i class="bi bi-arrow-left me-1"></i>Quay lại
        </button>
      </div>
    </div>

    <!-- Filter -->
    <div class="card border-0 shadow-sm mb-3">
      <div class="filter-head" @click="isFilterOpen = !isFilterOpen">
        <div class="d-flex align-items-center gap-2">
          <span class="caret" :class="{ open: isFilterOpen }">▾</span>
          <span class="fw-bold text-white">Bộ lọc tìm kiếm</span>
        </div>
        <small class="text-white-50">Nhấn để thu gọn/mở rộng</small>
      </div>

      <div v-show="isFilterOpen" class="p-3">
        <!-- Row 1 -->
        <div class="row g-3 align-items-end">
          <div class="col-12 col-lg-6">
            <label class="form-label small fw-semibold">Tìm kiếm</label>
            <input
              v-model="filters.keyword"
              class="form-control"
              placeholder="Tìm theo mã, màu, kích cỡ..."
            />
          </div>

          <div class="col-12 col-lg-3">
            <label class="form-label small fw-semibold">Màu sắc</label>
            <select v-model="filters.color" class="form-select">
              <option value="">-- Chọn Màu sắc --</option>
              <option v-for="c in attributes.mauSac" :key="c.id" :value="c.ten">
                {{ c.ten }}
              </option>
            </select>
          </div>

          <div class="col-12 col-lg-3">
            <label class="form-label small fw-semibold">Số lượng tồn</label>
            <select v-model="filters.stockRange" class="form-select">
              <option value="">-- Chọn Số lượng tồn --</option>
              <option value="0">= 0</option>
              <option value="1-10">1 - 10</option>
              <option value="11-50">11 - 50</option>
              <option value="51-200">51 - 200</option>
              <option value="200+">Trên 200</option>
            </select>
          </div>
        </div>

        <!-- Row 2 -->
        <div class="row g-3 align-items-end mt-1">
          <div class="col-12 col-lg-6">
            <div class="d-flex justify-content-between align-items-center mb-1">
              <label class="form-label small fw-semibold mb-0">
                Khoảng giá:
                <span class="text-success fw-bold">
                  {{ formatPrice(filters.priceMin) }} - {{ formatPrice(filters.priceMax) }}
                </span>
              </label>
            </div>

            <div class="dual-range">
              <input
                class="range-green"
                type="range"
                :min="PRICE_MIN"
                :max="PRICE_MAX"
                :step="PRICE_STEP"
                v-model.number="filters.priceMin"
                @input="syncPrice"
              />
              <input
                class="range-green"
                type="range"
                :min="PRICE_MIN"
                :max="PRICE_MAX"
                :step="PRICE_STEP"
                v-model.number="filters.priceMax"
                @input="syncPrice"
              />
            </div>
          </div>

          <div class="col-12 col-lg-3">
            <label class="form-label small fw-semibold">Kích cỡ</label>
            <select v-model="filters.size" class="form-select">
              <option value="">-- Chọn Kích cỡ --</option>
              <option v-for="s in attributes.kichCo" :key="s.id" :value="s.soSize">
                {{ s.soSize }}
              </option>
            </select>
          </div>

          <div class="col-12 col-lg-3 position-relative">
            <label class="form-label small fw-semibold">Trạng thái</label>

            <div class="d-flex gap-3 flex-wrap align-items-center">
              <label class="d-flex gap-2 align-items-center small mb-0">
                <input type="radio" value="all" v-model="filters.status" />
                Tất cả
              </label>
              <label class="d-flex gap-2 align-items-center small mb-0">
                <input type="radio" value="in" v-model="filters.status" />
                Còn hàng
              </label>
              <label class="d-flex gap-2 align-items-center small mb-0">
                <input type="radio" value="out" v-model="filters.status" />
                Hết hàng
              </label>
            </div>

            <button
              class="btn btn-link btn-sm p-0 reset-btn"
              type="button"
              @click="resetFilters"
              title="Reset"
            >
              <i class="bi bi-arrow-counterclockwise me-1"></i>Reset
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Table -->
    <div class="card border-0 shadow-sm">
      <div class="table-responsive">
        <table class="table align-middle mb-0">
          <thead class="thead-dark">
            <tr>
              <th class="text-center">STT</th>
              <th class="text-center">Ảnh</th>
              <th class="text-center">Mã SP chi tiết</th>
              <th class="text-center">Tên sản phẩm</th>
              <th class="text-center">Màu sắc</th>
              <th class="text-center">Kích cỡ</th>
              <th class="text-center">Số lượng tồn</th>
              <th class="text-center">Giá bán</th>
              <th class="text-center">Trạng thái</th>
              <th class="text-center">Hành động</th>
            </tr>
          </thead>

          <tbody>
            <tr v-for="(v, index) in filteredItems" :key="v.id">
              <td class="text-center">{{ currentPage * pageSize + index + 1 }}</td>

              <td class="text-center">
                <img
                  v-if="v.anh"
                  :src="'http://localhost:8080' + v.anh"
                  class="variant-img"
                />
                <span v-else class="no-img">Ảnh biến thể</span>
              </td>

              <td class="text-center">{{ v.maSanPhamChiTiet }}</td>
              <td class="text-center fw-semibold">{{ v.tenSanPham }}</td>

              <!-- ✅ chấm màu theo tên (Cách 2) -->
              <td class="text-center">
                <span
                  class="color-dot"
                  :style="{ backgroundColor: getColorCode(v.tenMauSac) }"
                  :title="v.tenMauSac"
                ></span>
                {{ v.tenMauSac }}
              </td>

              <td class="text-center">{{ v.tenKichCo }}</td>
              <td class="text-center">{{ v.soLuongTon }}</td>
              <td class="text-center fw-semibold text-dark">{{ formatPrice(v.donGia) }}</td>

              <td class="text-center">
                <span
                  :class="[
                    'badge rounded-pill px-3',
                    v.trangThai ? 'bg-success-subtle text-success' : 'bg-danger-subtle text-danger'
                  ]"
                >
                  {{ v.trangThai ? 'Còn hàng' : 'Hết hàng' }}
                </span>
              </td>

              <td class="text-center">
                <div class="d-flex justify-content-center align-items-center gap-2">
                  <button
                    class="btn btn-outline-warning btn-sm"
                    title="Sửa"
                    @click="openEditModal(v)"
                  >
                    <i class="bi bi-pencil-square"></i>
                  </button>

                  <div class="form-check form-switch m-0" title="Đổi trạng thái">
                    <input
                      class="form-check-input"
                      type="checkbox"
                      :checked="!!v.trangThai"
                      @click.prevent="requestToggleStatus(v)"
                    />
                  </div>
                </div>
              </td>
            </tr>

            <tr v-if="loading">
              <td colspan="10" class="text-center py-4">Đang tải dữ liệu...</td>
            </tr>
            <tr v-if="!loading && filteredItems.length === 0">
              <td colspan="10" class="text-center py-4">Không có dữ liệu</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="p-3" v-if="totalPages > 0">
        <div class="paging-bar">
          <div class="paging-left">
            Hiển thị {{ filteredItems.length }} / tổng {{ totalElements }} bản ghi
          </div>

          <div class="paging-center">
            <button
              type="button"
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
                :max="totalPages || 1"
                class="form-control"
                v-model.number="pageInput"
                @keyup.enter="jumpPage"
              />
            </div>

            <button
              type="button"
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
      </div>
    </div>

    <!-- (Các modal giữ nguyên như bạn, mình không sửa) -->
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAllDetails, updateDetail, uploadImage } from '../../services/sanPhamChiTietApi'
import attributeService from '../../services/attributeService'
import { useToast } from '../../composables/useToast'

const { success, error } = useToast()
const router = useRouter()

/** pagination */
const items = ref([])
const loading = ref(false)
const currentPage = ref(0)
const pageSize = ref(10)
const totalPages = ref(0)
const totalElements = ref(0)
const pageInput = ref(1)

/** filter UI */
const isFilterOpen = ref(true)
const PRICE_MIN = 0
const PRICE_MAX = 10000000
const PRICE_STEP = 50000

const attributes = reactive({
  kichCo: [],
  mauSac: []
})

const filters = reactive({
  keyword: '',
  color: '',
  size: '',
  stockRange: '',
  status: 'all', // all | in | out
  priceMin: PRICE_MIN,
  priceMax: PRICE_MAX
})

/** modals (giữ nguyên) */
const showQrModal = ref(false)
const showExportModal = ref(false)
const showConfirmToggle = ref(false)
const pendingVariant = ref(null)
const pendingNext = ref(false)
const toggleLoading = ref(false)

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

onMounted(() => {
  loadAttributes()
  loadData()
})

async function loadAttributes() {
  try {
    const resSize = await attributeService.getAllList('kich-co')
    attributes.kichCo = resSize.data

    const resColor = await attributeService.getAllList('mau-sac')
    attributes.mauSac = resColor.data
  } catch (e) {
    console.error(e)
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await getAllDetails(currentPage.value, pageSize.value)
    items.value = res.data.content
    totalPages.value = res.data.totalPages
    totalElements.value = res.data.totalElements || 0
    pageInput.value = currentPage.value + 1
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

/** filtering: client-side trên page đang load */
const filteredItems = computed(() => {
  const kw = (filters.keyword || '').trim().toLowerCase()

  return (items.value || []).filter(v => {
    const matchKeyword =
      !kw ||
      (v.maSanPhamChiTiet && v.maSanPhamChiTiet.toLowerCase().includes(kw)) ||
      (v.tenSanPham && v.tenSanPham.toLowerCase().includes(kw)) ||
      (v.tenMauSac && v.tenMauSac.toLowerCase().includes(kw)) ||
      (v.tenKichCo && String(v.tenKichCo).toLowerCase().includes(kw))

    const matchColor = !filters.color || v.tenMauSac === filters.color
    const matchSize = !filters.size || String(v.tenKichCo) === String(filters.size)

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

    const price = Number(v.donGia ?? 0)
    const matchPrice = price >= Number(filters.priceMin) && price <= Number(filters.priceMax)

    return matchKeyword && matchColor && matchSize && matchStock && matchStatus && matchPrice
  })
})

function syncPrice() {
  if (filters.priceMin > filters.priceMax) {
    const t = filters.priceMin
    filters.priceMin = filters.priceMax
    filters.priceMax = t
  }
}

/** pagination actions */
function changePage(page) {
  if (page >= 0 && page < totalPages.value) {
    currentPage.value = page
    loadData()
  }
}

function jumpPage() {
  const max = Math.max(1, totalPages.value || 1)
  const target = Math.min(Math.max(1, pageInput.value || 1), max)
  changePage(target - 1)
}

function onChangeSize() {
  currentPage.value = 0
  loadData()
}

/** reset */
async function resetFilters() {
  filters.keyword = ''
  filters.color = ''
  filters.size = ''
  filters.stockRange = ''
  filters.status = 'all'
  filters.priceMin = PRICE_MIN
  filters.priceMax = PRICE_MAX
  await loadData()
  success('Đã hiển thị tất cả biến thể')
}

function goBack() {
  router.push('/products')
}

/** status toggle with confirm modal */
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
    await loadData()
    success(`Đã đổi trạng thái biến thể thành ${next ? 'Còn hàng' : 'Hết hàng'}`)
  } catch (e) {
    console.error(e)
    error('Lỗi cập nhật trạng thái')
  } finally {
    toggleLoading.value = false
  }
}

/** edit modal */
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
    await loadData()
  } catch (e) {
    console.error(e)
    error('Cập nhật thất bại')
  }
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

/** export */
function downloadCsv() {
  try {
    const rows = filteredItems.value.map((v, i) => ({
      STT: currentPage.value * pageSize.value + i + 1,
      MaSPCT: v.maSanPhamChiTiet ?? '',
      TenSanPham: v.tenSanPham ?? '',
      MauSac: v.tenMauSac ?? '',
      KichCo: v.tenKichCo ?? '',
      SoLuongTon: v.soLuongTon ?? 0,
      GiaBan: v.donGia ?? 0,
      TrangThai: v.trangThai ? 'Còn hàng' : 'Hết hàng'
    }))

    const header = Object.keys(rows[0] || {
      STT: '', MaSPCT: '', TenSanPham: '', MauSac: '', KichCo: '', SoLuongTon: '', GiaBan: '', TrangThai: ''
    })

    const csv = [
      header.join(','),
      ...rows.map(r => header.map(h => `"${String(r[h] ?? '').replaceAll('"', '""')}"`).join(','))
    ].join('\n')

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `bien-the-${Date.now()}.csv`
    a.click()
    URL.revokeObjectURL(url)

    showExportModal.value = false
    success('Đã xuất file CSV (mở bằng Excel)')
  } catch (e) {
    console.error(e)
    error('Xuất file thất bại')
  }
}

/* ===========================
   ✅ CÁCH 2: MÀU THEO TÊN
   =========================== */

/** 1) Chuẩn hoá tên màu: bỏ dấu, bỏ ngoặc, chuẩn hoá khoảng trắng */
function normalizeColorName(name) {
  return String(name || '')
    .trim()
    .toLowerCase()
    .replace(/đ/g, 'd')              // ✅ quan trọng: đ -> d
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '') // bỏ dấu còn lại
    .replace(/\(.*?\)/g, '')
    .replace(/\s+/g, ' ')
    .trim()
}


/** 2) Bảng map: thêm đủ theo DB của bạn */
const COLOR_MAP = {
  // basic
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

  'xanh la': '#22c55e',
  'xanh luc': '#16a34a',
  'xanh ngoc': '#14b8a6',

  'xanh duong': '#3b82f6',
  'xanh navy': '#1e3a8a',
  'xanh than': '#1e3a8a',
  navy: '#1e3a8a',

  cyan: '#06b6d4'
}

/** 3) Lấy mã màu theo tên, có fallback theo keyword */
function getColorCode(colorName) {
  if (!colorName) return '#ccc'
  const key = normalizeColorName(colorName)

  // match exact
  if (COLOR_MAP[key]) return COLOR_MAP[key]

  // fallback theo từ khoá (đỡ phải liệt kê hết 100% biến thể)
  if (key.includes('navy') || key.includes('than')) return COLOR_MAP['xanh navy']
  if (key.includes('xanh') && key.includes('la')) return COLOR_MAP['xanh la']
  if (key.includes('xanh') && key.includes('duong')) return COLOR_MAP['xanh duong']
  if (key.includes('do')) return COLOR_MAP.do
  if (key.includes('vang')) return COLOR_MAP.vang
  if (key.includes('cam')) return COLOR_MAP.cam
  if (key.includes('hong')) return COLOR_MAP.hong
  if (key.includes('tim')) return COLOR_MAP.tim
  if (key.includes('nau')) return COLOR_MAP.nau
  if (key.includes('trang')) return COLOR_MAP.trang
  if (key.includes('den')) return COLOR_MAP.den

  // không biết -> màu mặc định
  return '#3b82f6'
}

/** utils */
function formatPrice(val) {
  const num = Number(val ?? 0)
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(num)
}
</script>

<style scoped>
/* giữ nguyên như bạn */
.variant-page { padding: 16px; background: #ffffff; min-height: 100vh; }

.filter-head {
  background: #0f172a;
  padding: 12px 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  border-top-left-radius: 10px;
  border-top-right-radius: 10px;
}
.caret { transition: .15s; color: #fff; }
.caret.open { transform: rotate(180deg); }

.dual-range { position: relative; height: 28px; }
.dual-range input[type="range"] { position: absolute; inset: 0; width: 100%; }

.range-green { accent-color: #22c55e; }

.reset-btn {
  position: absolute;
  right: 0;
  bottom: -2px;
  text-decoration: none;
  color: #6b7280;
}
.reset-btn:hover { color: #111827; text-decoration: underline; }

.thead-dark th {
  background: #1e293b !important;
  color: #fff !important;
  font-weight: 600;
  font-size: 0.9rem;
}

.variant-img{
  width: 200px;
  height: 200px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.no-img {
  font-size: 0.75rem;
  padding: 4px 6px;
  border-radius: 6px;
  background: #f3f4f6;
  color: #6b7280;
}

.color-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  margin-right: 6px;
}
</style>
