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
                placeholder="Tìm theo mã SP, mã SP chi tiết, tên, màu, kích cỡ..."
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
              <i class="bi bi-arrow-counterclockwise me-1"></i>Đặt lại
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

            <!-- ✅ THÊM + ĐỔI THỨ TỰ -->
            <th class="text-center">Mã sản phẩm</th>
            <th class="text-center">Tên sản phẩm</th>
            <th class="text-center">Mã SP chi tiết</th>

            <th class="text-center">Màu sắc</th>
            <th class="text-center">Kích cỡ</th>
            <th class="text-center">Số lượng tồn</th>
            <th class="text-center">Giá bán</th>
            <th class="text-center">Trạng thái</th>
            <th class="text-center">Hành động</th>
          </tr>
          </thead>

          <tbody>
          <tr v-for="(v, index) in pagedItems" :key="v.id">
            <td class="text-center">{{ currentPage * pageSize + index + 1 }}</td>

            <td class="text-center">
              <img
                  v-if="v.anh"
                  :src="resolveMediaUrl(v.anh || v.primaryImageUrl || v.mediaAsset)"
                  class="variant-img"
              />
              <span v-else class="no-img">Ảnh biến thể</span>
            </td>
                        <!-- ✅ THÊM + ĐỔI THỨ TỰ -->
            <td class="text-center">{{ v.maSanPham || '-' }}</td>
            <td class="text-center fw-semibold">{{ v.tenSanPham }}</td>
            <td class="text-center">{{ v.maSanPhamChiTiet }}</td>

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
            <!-- ✅ colspan tăng 1 -->
            <td colspan="11" class="text-center py-4">Đang tải dữ liệu...</td>
          </tr>
          <tr v-if="!loading && pagedItems.length === 0">
            <!-- ✅ colspan tăng 1 -->
            <td colspan="11" class="text-center py-4">Không có dữ liệu</td>
          </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="p-3" v-if="totalPages > 0">
        <div class="paging-bar">
          <div class="paging-left">
            Hiển thị: {{ pagedItems.length }} / tổng {{ totalElements }} bản ghi
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
            <select
                class="form-select form-select-sm paging-size"
                v-model.number="pageSize"
                @change="onChangeSize"
            >
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

  <!-- Edit Modal -->
  <div v-if="showEditModal" class="modal-overlay" @click.self="closeEditModal">
    <div class="modal-box">
      <div class="modal-head">
        <h6 class="mb-0">Sửa biến thể: {{ editingVariant.maSanPhamChiTiet }}</h6>
        <button class="btn-close" type="button" @click="closeEditModal"></button>
      </div>

      <div class="modal-body p-3">
        <div class="row g-3">
          <div class="col-6">
            <label class="form-label small fw-semibold">Kích cỡ</label>
            <select v-model="editingVariant.idKichCo" class="form-select">
              <option v-for="s in attributes.kichCo" :key="s.id" :value="s.id">
                {{ s.soSize }}
              </option>
            </select>
          </div>

          <div class="col-6">
            <label class="form-label small fw-semibold">Màu sắc</label>
            <select v-model="editingVariant.idMauSac" class="form-select">
              <option v-for="c in attributes.mauSac" :key="c.id" :value="c.id">
                {{ c.ten }}
              </option>
            </select>
          </div>

          <div class="col-6">
            <label class="form-label small fw-semibold">Số lượng tồn</label>
            <input type="number" min="0" class="form-control" v-model.number="editingVariant.soLuongTon" />
          </div>

          <div class="col-6">
  <label class="form-label small fw-semibold">Giá bán</label>
  <input
    type="text"
    class="form-control"
    :value="formatNumberInput(editingVariant.donGia)"
    inputmode="numeric"
    @input="onPriceInput"
    @blur="onPriceBlur"
    placeholder="Nhập giá bán"
  />
</div>

          <div class="col-12">
            <label class="form-label small fw-semibold">Trạng thái</label>
            <div class="d-flex gap-3">
              <label class="d-flex align-items-center gap-2 small mb-0">
                <input type="radio" :value="true" v-model="editingVariant.trangThai" /> Còn hàng
              </label>
              <label class="d-flex align-items-center gap-2 small mb-0">
                <input type="radio" :value="false" v-model="editingVariant.trangThai" /> Hết hàng
              </label>
            </div>
          </div>

          <div class="col-12">
                        <label class="form-label small fw-semibold">Ảnh biến thể</label>
            <input type="file" class="form-control" accept="image/*" @change="handleFileUpload" />
            <div v-if="editingVariant.anh" class="mt-2">
              <img :src="resolveMediaUrl(editingVariant.anh || editingVariant.primaryImageUrl || editingVariant.mediaAsset)" class="preview-img" />
            </div>
          </div>
        </div>
      </div>

      <div class="modal-foot">
        <button class="btn btn-secondary btn-sm" type="button" @click="closeEditModal">Hủy</button>
        <button class="btn btn-primary btn-sm" type="button" @click="submitEdit">Lưu</button>
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
        <button class="btn btn-secondary btn-sm" type="button" @click="closeToggleModal" :disabled="toggleLoading">
          Hủy
        </button>
        <button class="btn btn-primary btn-sm" type="button" @click="confirmToggleStatus" :disabled="toggleLoading">
          {{ toggleLoading ? 'Đang xử lý...' : 'Xác nhận' }}
        </button>
      </div>
    </div>
  </div>

</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { emitTabSync, TAB_SYNC_EVENTS } from "@/utils/tabSync";
import { useRouter } from 'vue-router'
import { getAllDetails, updateDetail, uploadImage } from '../../services/sanPhamChiTietApi'
import attributeService from '../../services/attributeService'
import { useToast } from '../../composables/useToast'
import { normalizeUploadResponse, resolveMediaUrl } from '@/utils/media'

const { success, error } = useToast()
const router = useRouter()

/** pagination */
const allItems = ref([])
const loading = ref(false)
const currentPage = ref(0)
const pageSize = ref(10)
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
  mediaPrimaryId: null,
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

function mapVariant(item) {
  return {
    ...item,
    mediaPrimaryId: item.mediaPrimaryId ?? item.idMediaPrimary ?? item.id_media_primary ?? null
  }
}

async function loadData() {
  loading.value = true
  try {
    const firstRes = await getAllDetails(0, 100)
    const firstData = firstRes?.data || {}
    const firstPageItems = (firstData.content || []).map(mapVariant)
    const pages = Number(firstData.totalPages || 1)

    if (pages <= 1) {
      allItems.value = firstPageItems
    } else {
      const rest = []
      for (let page = 1; page < pages; page++) {
        const res = await getAllDetails(page, 100)
        const data = res?.data || {}
        rest.push(...(data.content || []).map(mapVariant))
      }
      allItems.value = [...firstPageItems, ...rest]
    }

    pageInput.value = currentPage.value + 1
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function normalizeSearchText(value) {
  return String(value || '')
    .trim()
    .toLowerCase()
    .replace(/đ/g, 'd')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-z0-9]/g, '')
}

/** filtering: client-side trên toàn bộ danh sách biến thể */
const filteredItems = computed(() => {
  const kw = (filters.keyword || '').trim().toLowerCase()
  const normalizedKw = normalizeSearchText(filters.keyword)

  return (allItems.value || []).filter(v => {
    const keywordFields = [
      v.maSanPham,
      v.maSanPhamChiTiet,
      v.tenSanPham,
            v.tenMauSac,
      v.tenKichCo
    ]

    const matchKeyword =
      !kw ||
      keywordFields.some(value => {
        const raw = String(value || '').toLowerCase()
        const normalized = normalizeSearchText(value)
        return raw.includes(kw) || (!!normalizedKw && normalized.includes(normalizedKw))
      })

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

const totalElements = computed(() => filteredItems.value.length)
const totalPages = computed(() => Math.max(1, Math.ceil(totalElements.value / pageSize.value)))

const pagedItems = computed(() => {
  const start = currentPage.value * pageSize.value
  return filteredItems.value.slice(start, start + pageSize.value)
})

watch([filteredItems, pageSize], () => {
  const maxPage = Math.max(0, totalPages.value - 1)
  if (currentPage.value > maxPage) currentPage.value = maxPage
  pageInput.value = currentPage.value + 1
})

watch(currentPage, (page) => {
  pageInput.value = page + 1
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
  }
}

function jumpPage() {
  const max = Math.max(1, totalPages.value || 1)
  const target = Math.min(Math.max(1, pageInput.value || 1), max)
  changePage(target - 1)
}

function onChangeSize() {
  currentPage.value = 0
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
  currentPage.value = 0
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
      anh: v.anh,
      mediaPrimaryId: v.mediaPrimaryId ?? null
    })

    emitTabSync(TAB_SYNC_EVENTS.PRODUCT_CHANGED, {
      productDetailId: v.id,
      soLuongTon: v.soLuongTon,
      donGia: v.donGia,
      trangThai: next,
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
  editingVariant.mediaPrimaryId = v.mediaPrimaryId ?? null
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
      anh: editingVariant.anh,
      mediaPrimaryId: editingVariant.mediaPrimaryId
          })

    emitTabSync(TAB_SYNC_EVENTS.PRODUCT_STOCK_CHANGED, {
      productDetailId: editingVariant.id,
      soLuongTon: editingVariant.soLuongTon,
      donGia: editingVariant.donGia,
      trangThai: editingVariant.trangThai,
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
    const uploaded = normalizeUploadResponse(await uploadImage(file))
    editingVariant.anh = uploaded.url
    editingVariant.mediaPrimaryId = uploaded.mediaAssetId
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
      STT: i + 1,
      // ✅ thêm maSanPham + chỉnh thứ tự cho đúng yêu cầu
      MaSP: v.maSanPham ?? '',
      TenSanPham: v.tenSanPham ?? '',
      MaSPCT: v.maSanPhamChiTiet ?? '',
      MauSac: v.tenMauSac ?? '',
      KichCo: v.tenKichCo ?? '',
      SoLuongTon: v.soLuongTon ?? 0,
      GiaBan: v.donGia ?? 0,
      TrangThai: v.trangThai ? 'Còn hàng' : 'Hết hàng'
    }))

    const header = Object.keys(rows[0] || {
      STT: '', MaSP: '', TenSanPham: '', MaSPCT: '', MauSac: '', KichCo: '', SoLuongTon: '', GiaBan: '', TrangThai: ''
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
  'xanh la': '#22c55e',
  'xanh luc': '#16a34a',
  'xanh ngoc': '#14b8a6',
  'xanh duong': '#3b82f6',
  'xanh navy': '#1e3a8a',
  'xanh than': '#1e3a8a',
  navy: '#1e3a8a',
  cyan: '#06b6d4'
}

function getColorCode(colorName) {
  if (!colorName) return '#ccc'
  const key = normalizeColorName(colorName)

  if (COLOR_MAP[key]) return COLOR_MAP[key]

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

  return '#3b82f6'
}

/** utils */
function formatPrice(val) {
  const num = Number(val ?? 0)
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(num)
}
function formatNumberInput(val) {
  const num = Number(val || 0)
  return num.toLocaleString('vi-VN')
}

function parseMoneyInput(val) {
  return Number(String(val || '').replace(/[^\d]/g, '')) || 0
}

function onPriceInput(e) {
  const num = parseMoneyInput(e.target.value)
  editingVariant.donGia = num
  e.target.value = formatNumberInput(num)
}

function onPriceBlur(e) {
  e.target.value = formatNumberInput(editingVariant.donGia)
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
.modal-overlay{
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-box{
  width: 620px;
  max-width: calc(100vw - 24px);
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 18px 50px rgba(0,0,0,.22);
}

.modal-confirm{ width: 520px; }

.modal-head{
  padding: 12px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #eef2f7;
}

.modal-foot{
  padding: 12px 14px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  border-top: 1px solid #eef2f7;
}

.preview-img{
  width: 140px;
  height: 140px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}
/* ===== Paging giống ảnh mẫu ===== */
.paging-bar{
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.paging-left{
  white-space: nowrap;
  color: #6b7280;
  font-size: 0.9rem;
}

.paging-center{
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.paging-center .btn{
  width: 34px;
  height: 32px;
  padding: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.paging-page{
  width: 120px;
}

.paging-page .form-control{
  text-align: center;
}

.paging-right{
  display: flex;
  align-items: center;
  justify-content: flex-end;
  white-space: nowrap;
}

.paging-size{
  width: 170px;
  max-width: 200px;
}
/* responsive: mobile thì tự xuống dòng gọn gàng */
@media (max-width: 768px){
  .paging-bar{
    flex-wrap: wrap;
  }
  .paging-center{
    order: 3;
    width: 100%;
    justify-content: flex-start;
  }
  .paging-right{
    order: 2;
    margin-left: auto;
  }
}
</style>