<template>
  <div class="variant-page">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div>
        <h5 class="mb-0 ">Quản lý sản phẩm / Danh sách biến thể</h5>
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

            <!-- green dual slider -->
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

    <!-- Table (giữ nguyên) -->
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
              <td class="text-center">{{ (currentPage * pageSize) + index + 1 }}</td>

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

              <td class="text-center">
                <span class="color-dot" :style="{ backgroundColor: getColorCode(v.tenMauSac) }"></span>
                {{ v.tenMauSac }}
              </td>

              <td class="text-center">{{ v.tenKichCo }}</td>
              <td class="text-center">{{ v.soLuongTon }}</td>
              <td class="text-center fw-semibold text-dark">{{ formatPrice(v.donGia) }}</td>

              <td class="text-center">
                <span :class="['badge rounded-pill px-3', v.trangThai ? 'bg-success-subtle text-success' : 'bg-danger-subtle text-danger']">
                  {{ v.trangThai ? 'Còn hàng' : 'Hết hàng' }}
                </span>
              </td>

              <!-- Hành động: Bootstrap -->
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
                    <!-- prevent default toggle, mở modal xác nhận -->
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

      <!-- Pagination (giữ nguyên của bạn) -->
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

    <!-- Edit Modal (giữ nguyên logic) -->
    <div v-if="showEditModal" class="modal-overlay">
      <div class="modal-card">
        <div class="d-flex justify-content-between align-items-center mb-2">
          <h5 class="mb-0">Sửa biến thể: {{ editingVariant.maSanPhamChiTiet }}</h5>
          <button class="btn btn-sm btn-light" @click="closeEditModal">×</button>
        </div>

        <div class="mb-3">
          <label class="form-label small fw-semibold">Kích cỡ</label>
          <select v-model="editingVariant.idKichCo" class="form-select">
            <option v-for="s in attributes.kichCo" :key="s.id" :value="s.id">{{ s.soSize }}</option>
          </select>
        </div>

        <div class="mb-3">
          <label class="form-label small fw-semibold">Màu sắc</label>
          <select v-model="editingVariant.idMauSac" class="form-select">
            <option v-for="c in attributes.mauSac" :key="c.id" :value="c.id">{{ c.ten }}</option>
          </select>
        </div>

        <div class="row g-3 mb-3">
          <div class="col-6">
            <label class="form-label small fw-semibold">Số lượng</label>
            <input type="number" v-model="editingVariant.soLuongTon" class="form-control" min="0" />
          </div>
          <div class="col-6">
            <label class="form-label small fw-semibold">Đơn giá</label>
            <input type="number" v-model="editingVariant.donGia" class="form-control" min="0" />
          </div>
        </div>

        <div class="mb-3">
          <label class="form-label small fw-semibold">Trạng thái</label>
          <div class="d-flex gap-3 mt-1">
            <label class="d-flex gap-2 align-items-center">
              <input type="radio" :value="true" v-model="editingVariant.trangThai" />
              Còn hàng
            </label>
            <label class="d-flex gap-2 align-items-center">
              <input type="radio" :value="false" v-model="editingVariant.trangThai" />
              Hết hàng
            </label>
          </div>
        </div>

        <div class="mb-3">
          <label class="form-label small fw-semibold">Ảnh biến thể</label>
          <input type="file" @change="handleFileUpload" class="form-control" accept="image/*" />
          <div v-if="editingVariant.anh" class="mt-2">
            <img :src="'http://localhost:8080' + editingVariant.anh" class="preview-img" />
          </div>
        </div>

        <div class="d-flex justify-content-end gap-2 mt-3">
          <button class="btn btn-secondary" @click="closeEditModal">Hủy</button>
          <button class="btn btn-primary" @click="submitEdit">Lưu thay đổi</button>
        </div>
      </div>
    </div>

    <!-- Modal xác nhận đổi trạng thái -->
    <div v-if="showConfirmToggle" class="modal-overlay">
      <div class="modal-card">
        <div class="d-flex justify-content-between align-items-center mb-2">
          <h5 class="mb-0">Xác nhận</h5>
          <button class="btn btn-sm btn-light" @click="closeToggleModal">×</button>
        </div>

        <div class="text-muted">
          Bạn có chắc muốn đổi trạng thái biến thể
          <span class="fw-semibold text-dark">{{ pendingVariant?.maSanPhamChiTiet }}</span>
          thành
          <span class="fw-semibold" :class="pendingNext ? 'text-success' : 'text-danger'">
            {{ pendingNext ? 'Còn hàng' : 'Hết hàng' }}
          </span>
          không?
        </div>

        <div class="d-flex justify-content-end gap-2 mt-3">
          <button class="btn btn-secondary" @click="closeToggleModal">Hủy</button>
          <button class="btn btn-primary" :disabled="toggleLoading" @click="confirmToggleStatus">
            <span v-if="toggleLoading" class="spinner-border spinner-border-sm me-2"></span>
            Xác nhận
          </button>
        </div>
      </div>
    </div>

    <!-- QR Modal -->
    <div v-if="showQrModal" class="modal-overlay">
      <div class="modal-card">
        <div class="d-flex justify-content-between align-items-center mb-2">
          <h5 class="mb-0">Quét QR</h5>
          <button class="btn btn-sm btn-light" @click="showQrModal = false">×</button>
        </div>

        <div class="text-muted mb-2">
          (UI modal theo ảnh. Nếu bạn muốn quét thật, mình sẽ gắn html5-qrcode/camera.)
        </div>

        <div class="qr-box">Khu vực camera / preview QR</div>

        <div class="d-flex justify-content-end gap-2 mt-3">
          <button class="btn btn-secondary" @click="showQrModal = false">Đóng</button>
        </div>
      </div>
    </div>

    <!-- Export Modal -->
    <div v-if="showExportModal" class="modal-overlay">
      <div class="modal-card">
        <div class="d-flex justify-content-between align-items-center mb-2">
          <h5 class="mb-0">Tải Excel</h5>
          <button class="btn btn-sm btn-light" @click="showExportModal = false">×</button>
        </div>

        <div class="text-muted">
          Xuất danh sách đang hiển thị (đã lọc). Mình xuất CSV để Excel mở được.
        </div>

        <div class="d-flex justify-content-end gap-2 mt-3">
          <button class="btn btn-secondary" @click="showExportModal = false">Hủy</button>
          <button class="btn btn-primary" @click="downloadCsv">Tải xuống</button>
        </div>
      </div>
    </div>
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

/** modals */
const showQrModal = ref(false)
const showExportModal = ref(false)

/** toggle confirm modal */
const showConfirmToggle = ref(false)
const pendingVariant = ref(null)
const pendingNext = ref(false)
const toggleLoading = ref(false)

/** edit modal */
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

/** reset like screenshot */
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
      STT: (currentPage.value * pageSize.value) + i + 1,
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

/** utils */
function formatPrice(val) {
  const num = Number(val ?? 0)
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(num)
}

function getColorCode(colorName) {
  if (!colorName) return '#ccc'
  const map = {
    'Đen': '#111827',
    'Trắng': '#ffffff',
    'Đỏ': '#ef4444',
    'Vàng': '#f59e0b',
    'Xanh navy': '#1d4ed8',
    'Xanh Than (Navy)': '#000080'
  }
  return map[colorName] || '#3b82f6'
}
</script>

<style scoped>
.variant-page {
  padding: 16px;
  background: #ffffff;
  min-height: 100vh;
}

/* Filter header like screenshot */
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

/* dual slider */
.dual-range {
  position: relative;
  height: 28px;
}
.dual-range input[type="range"] {
  position: absolute;
  inset: 0;
  width: 100%;
}

/* GREEN slider */
.range-green {
  accent-color: #22c55e;
}
.range-green::-webkit-slider-thumb { cursor: pointer; }
.range-green::-webkit-slider-runnable-track { cursor: pointer; }
.range-green::-moz-range-thumb { cursor: pointer; }
.range-green::-moz-range-track { cursor: pointer; }

/* Reset button like right-side in screenshot */
.reset-btn {
  position: absolute;
  right: 0;
  bottom: -2px;
  text-decoration: none;
  color: #6b7280;
}
.reset-btn:hover { color: #111827; text-decoration: underline; }

/* Table head navy */
.thead-dark th {
  background: #1e293b !important;
  color: #fff !important;
  font-weight: 600;
  font-size: 0.9rem;
}

/* images */
.variant-img{
  width: 200px;
  height: 200px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.preview-img {
  max-width: 200px;
  max-height: 200px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}
.no-img {
  font-size: 0.75rem;
  padding: 4px 6px;
  border-radius: 6px;
  background: #f3f4f6;
  color: #6b7280;
}

/* color dot */
.color-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  margin-right: 6px;
}

/* Pagination keep style */
.paging-bar {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 12px;
}
.paging-left { justify-self: start; color: #6b7280; font-size: 0.875rem; }
.paging-center { justify-self: center; display: inline-flex; align-items: center; gap: 10px; }
.paging-right { justify-self: end; }
.paging-page { width: 120px; }
.paging-size { width: 160px; }

/* Custom modal overlay */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 16px;
}
.modal-card {
  width: 560px;
  max-width: 95vw;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 12px 40px rgba(0,0,0,.2);
}
.qr-box {
  margin-top: 10px;
  border: 1px dashed #d1d5db;
  border-radius: 12px;
  height: 220px;
  display: grid;
  place-items: center;
  color: #6b7280;
}
</style>
