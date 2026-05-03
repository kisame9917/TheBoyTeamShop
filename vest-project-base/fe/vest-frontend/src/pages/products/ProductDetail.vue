<template>
  <div class="product-detail-page">
    <!-- Header -->
    <div class="page-header">
      <div class="page-title">
        <h2 class="page-title-text">
          Quản lý sản phẩm / Chi tiết biến thể
          <span v-if="productCode || productName" class="title-sep">-</span>
          <span v-if="productCode" class="title-code">{{ productCode }}</span>
          <span v-if="productName" class="title-name">({{ productName }})</span>
        </h2>

        <div class="page-sub" v-if="productName">
          <span class="sub-label">Tên:</span> <b>{{ productName }}</b>
        </div>
      </div>

      <div class="page-actions">
        <button class="btn btn-outline-secondary btn-sm" type="button" @click="scanQr">
          <i class="bi bi-qr-code me-1"></i> Quét QR
        </button>

        <!-- EXCEL -->
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
          <!-- ✅ mx-0 để bỏ margin âm của bootstrap row (hay làm lệch viền) -->
          <div class="row g-3 align-items-end mx-0">
            <div class="col-12 col-lg-4">
              <label class="form-label">Tìm kiếm</label>
              <input
                  v-model="filters.keyword"
                  type="text"
                  class="form-control"
                  placeholder="Tìm theo mã SP, mã SPCT, màu, kích cỡ..."
                  @input="onKeywordInput"
                  @keyup.enter="applyFilters"
              />
            </div>

            <div class="col-12 col-lg-5">
              <label class="form-label">Màu sắc</label>
              <Multiselect
                v-model="selectedColorFilter"
                :options="attributes.mauSac"
                track-by="id"
                label="ten"
                placeholder="-- Chọn Màu sắc --"
                :searchable="true"
                :allow-empty="true"
                :show-labels="false"
                class="filter-multiselect"
              >
                <template #noResult>Không tìm thấy màu sắc</template>
                <template #noOptions>Không có màu sắc</template>
              </Multiselect>
            </div>

            <div class="col-12 col-lg-3">
              <label class="form-label">Số lượng tồn</label>
              <select v-model="filters.stock" class="form-select" :class="{ 'placeholder-select': !filters.stock }" @change="applyFilters">
                <option value="">-- Chọn Số lượng tồn --</option>
                <option value="lt10">&lt; 10</option>
                <option value="10_50">10 - 50</option>
                <option value="50_100">50 - 100</option>
                <option value="gte100">&ge; 100</option>
              </select>
            </div>

            <div class="col-12 col-lg-4">
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

            <div class="col-12 col-lg-5">
              <label class="form-label">Kích cỡ</label>
              <Multiselect
                v-model="selectedSizeFilter"
                :options="attributes.kichCo"
                track-by="id"
                label="soSize"
                placeholder="-- Chọn Kích cỡ --"
                :searchable="true"
                :allow-empty="true"
                :show-labels="false"
                class="filter-multiselect"
              >
                <template #noResult>Không tìm thấy kích cỡ</template>
                <template #noOptions>Không có kích cỡ</template>
              </Multiselect>
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

              <th class="text-center col-pcode">Mã sản phẩm</th>
              <th class="text-center col-name">Tên sản phẩm</th>
              <th class="text-center col-code">Mã SP chi tiết</th>

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

              <td class="text-center">{{ getProductCode(v) || '-' }}</td>
              <td class="text-center text-bold">{{ v.tenSanPham || productName || '-' }}</td>
              <td class="text-center">{{ v.maSanPhamChiTiet || '-' }}</td>

              <td class="text-center">
                <div class="color-cell">
                  <span class="color-dot" :style="{ backgroundColor: getColorCode(v.tenMauSac) }"></span>
                  <span class="color-name">{{ v.tenMauSac || '-' }}</span>
                </div>
              </td>

              <td class="text-center">{{ v.tenKichCo || '-' }}</td>
              <td class="text-center">{{ v.soLuongTon ?? 0 }}</td>
              <td class="text-center text-highlight">{{ formatPrice(v.donGia ?? 0) }}</td>

              <!-- ✅ tồn = 0 => hết hàng -->
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

                  <label
                      class="switch"
                      :title="isOutOfStock(v) ? 'Số lượng tồn = 0 nên luôn Hết hàng' : 'Đổi trạng thái'"
                  >
                    <input
                        type="checkbox"
                        :checked="!!v.trangThai"
                        :disabled="togglingIds.has(v.id) || isOutOfStock(v)"
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
          <!-- ✅ mx-0 để modal không bị tràn/lệch viền -->
          <div class="row g-3 mx-0">
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
                <label class="me-3"><input type="radio" :value="true" v-model="edit.trangThai" :disabled="Number(edit.soLuongTon||0) <= 0" /> Còn hàng</label>
                <label><input type="radio" :value="false" v-model="edit.trangThai" /> Hết hàng</label>
              </div>
              <small v-if="Number(edit.soLuongTon||0) <= 0" class="hint">Số lượng tồn = 0 ⇒ trạng thái tự động là “Hết hàng”.</small>
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
          <small v-if="confirmToggle.target && isOutOfStock(confirmToggle.target)" class="hint">
            Biến thể đang có số lượng tồn = 0 ⇒ không thể bật “Còn hàng”.
          </small>
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
import Multiselect from 'vue-multiselect'
import 'vue-multiselect/dist/vue-multiselect.css'

import attributeService from '../../services/attributeService'
import { getByProductId, updateDetail, uploadImage } from '../../services/sanPhamChiTietApi'
import { getGiaMaxDb } from '../../services/sanPhamApi'
import { useToast } from '../../composables/useToast'
import { normalizeUploadResponse, resolveMediaUrl } from '@/utils/media'

const { success, error } = useToast()
const router = useRouter()

const props = defineProps({
  id: { type: [String, Number], required: true }
})

function buildImgUrl(path) {
  return resolveMediaUrl(path)
}

/** ===== state ===== */
const loading = ref(false)
const globalError = ref('')
const filterOpen = ref(true)

const variants = ref([])
const attributes = reactive({ kichCo: [], mauSac: [] })

/** ===== helpers: tồn kho -> trạng thái ===== */
function isOutOfStock(v) {
  return Number(v?.soLuongTon ?? 0) <= 0
}
function normalizeVariant(v) {
  const sl = Number(v?.soLuongTon ?? 0)
  // ✅ nếu tồn = 0 => luôn hết hàng
  const trangThai = sl > 0 ? !!v?.trangThai : false
  return { ...v, trangThai, anh: v?.anh ?? v?.anhDaiDien ?? v?.primaryImageUrl ?? '', mediaPrimaryId: v?.mediaPrimaryId ?? v?.idMediaPrimary ?? v?.id_media_primary ?? null, __imgErr: false }
}

/** ===== product info ===== */
function getProductCode(v) {
  return (
      v?.maSanPham ||
      v?.sanPhamMa ||
      v?.maSP ||
      v?.sanPham?.maSanPham ||
      v?.sanPham?.ma ||
      v?.productCode ||
      ''
  )
}

const productName = computed(() => {
  const first = variants.value?.[0]
  return first?.tenSanPham || first?.sanPhamTen || ''
})
const productCode = computed(() => {
  const first = variants.value?.[0]
  return getProductCode(first) || ''
})

/** ===== PRICE slider ===== */
const PRICE_STEP = 10000
const priceMaxDb = ref(null)
const isPriceReady = computed(() => Number(priceMaxDb.value || 0) > 0)
const priceMaxSafe = computed(() => (isPriceReady.value ? Number(priceMaxDb.value) : 0))

function roundUpToStep(n, step) {
  const x = Number(n || 0)
  if (!Number.isFinite(x) || x <= 0) return 0
  return Math.ceil(x / step) * step
}

/** ===== filters ===== */
const filters = reactive({
  keyword: '',
  colorId: '',
  sizeId: '',
  stock: '',
  status: '',
  priceMin: 0,
  priceMax: 0
})

const selectedColorFilter = computed({
  get() {
    return attributes.mauSac.find((c) => String(c.id) === String(filters.colorId)) || null
  },
  set(value) {
    filters.colorId = value?.id ? String(value.id) : ''
    applyFilters()
  }
})

const selectedSizeFilter = computed({
  get() {
    return attributes.kichCo.find((s) => String(s.id) === String(filters.sizeId)) || null
  },
  set(value) {
    filters.sizeId = value?.id ? String(value.id) : ''
    applyFilters()
  }
})

function onKeywordInput() {
  filters.keyword = String(filters.keyword ?? '').replace(/^\s+/, '')
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

const rangeStyle = computed(() => {
  if (!isPriceReady.value) return { left: '0%', width: '0%' }
  const max = Math.max(1, priceMaxSafe.value)
  const minV = Math.max(0, Math.min(filters.priceMin, max))
  const maxV = Math.max(0, Math.min(filters.priceMax, max))
  const left = (minV / max) * 100
  const width = ((maxV - minV) / max) * 100
  return { left: left + '%', width: width + '%' }
})

/** ===== filtering + paging ===== */
const filteredVariants = computed(() => {
  const kw = String(filters.keyword || '').toLowerCase().trim()
  const fMin = Number(filters.priceMin || 0)
  const fMax = Number(filters.priceMax || 0)

  return (variants.value || []).filter((v) => {
    const code = String(getProductCode(v) || '').toLowerCase()
    const spct = String(v.maSanPhamChiTiet || '').toLowerCase()
    const mau = String(v.tenMauSac || '').toLowerCase()
    const size = String(v.tenKichCo || '').toLowerCase()
    const ten = String(v.tenSanPham || '').toLowerCase()

    const okKw = !kw || code.includes(kw) || spct.includes(kw) || ten.includes(kw) || mau.includes(kw) || size.includes(kw)
    const okColor = !filters.colorId || String(v.idMauSac ?? '') === String(filters.colorId)
    const okSize = !filters.sizeId || String(v.idKichCo ?? '') === String(filters.sizeId)

    // ✅ status lọc theo trạng thái đã normalize (tồn=0 => false)
    const okStatus = filters.status === '' || String(!!v.trangThai) === String(filters.status)

    const sl = Number(v.soLuongTon ?? 0)
    let okStock = true
    if (filters.stock === 'lt10') okStock = sl < 10
    if (filters.stock === '10_50') okStock = sl >= 10 && sl <= 50
    if (filters.stock === '50_100') okStock = sl > 50 && sl <= 100
    if (filters.stock === 'gte100') okStock = sl >= 100

    const gia = Number(v.donGia ?? 0)
    const okPrice = !isPriceReady.value ? true : gia >= fMin && gia <= fMax

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

/** ===== load data ===== */
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

function getSizeValue(item) {
  const raw = item?.soSize ?? item?.ten ?? ''
  const num = Number(String(raw).replace(',', '.'))
  return Number.isFinite(num) ? num : Number.MAX_SAFE_INTEGER
}

function sortSizes(list = []) {
  return [...(Array.isArray(list) ? list : [])].sort((a, b) => {
    const byNumber = getSizeValue(a) - getSizeValue(b)
    if (byNumber !== 0) return byNumber
    return String(a?.soSize ?? a?.ten ?? '').localeCompare(String(b?.soSize ?? b?.ten ?? ''), 'vi')
  })
}

async function loadAttributes() {
  const [resSize, resColor] = await Promise.all([
    attributeService.getAllList('kich-co'),
    attributeService.getAllList('mau-sac')
  ])
  const sizeArr = resSize?.data || resSize || []
  const colorArr = resColor?.data || resColor || []
  attributes.kichCo = sortSizes((sizeArr || []).filter((x) => x?.trangThai !== false))
  attributes.mauSac = (colorArr || []).filter((x) => x?.trangThai !== false)
}

async function loadVariants() {
  const res = await getByProductId(props.id)
  const arr = res?.data || res || []
  variants.value = (arr || []).map(normalizeVariant)
}

async function loadPriceMaxFromDb() {
  try {
    const res = await getGiaMaxDb()
    const raw = res?.data ?? res
    const maxNum = typeof raw === 'object'
        ? Number(raw?.max ?? raw?.giaMax ?? raw?.value ?? 0)
        : Number(raw ?? 0)

    const maxDb = roundUpToStep(maxNum, PRICE_STEP)
    if (maxDb > 0) {
      priceMaxDb.value = maxDb
      return
    }
  } catch (e) {}

  const localMax = Math.max(...(variants.value || []).map(v => Number(v.donGia ?? 0)), 0)
  const maxLocal = roundUpToStep(localMax, PRICE_STEP)
  priceMaxDb.value = maxLocal > 0 ? maxLocal : 0
}

/** ===== nav ===== */
function goBack() { router.push('/products') }
function goToGlobalList() { router.push('/variants') }
function scanQr() { console.log('scan qr') }

/** ===== format ===== */
function formatPrice(val) {
  const n = Number(val ?? 0)
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n)
}

/** ================== EXCEL EXPORT ================== */
const exportMode = ref(false)
const exporting = ref(false)
const selectedIds = ref([])
const selectedRows = reactive({})

const tableColspan = computed(() => (exportMode.value ? 12 : 11))

function openExportMode() { exportMode.value = true }
function cancelExportMode() {
  exportMode.value = false
  selectedIds.value = []
  Object.keys(selectedRows).forEach((k) => delete selectedRows[k])
}
function isSelected(id) { return selectedIds.value.includes(id) }

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
    'Mã sản phẩm': getProductCode(v) ?? '',
    'Tên sản phẩm': v.tenSanPham || productName.value || '',
    'Mã SP chi tiết': v.maSanPhamChiTiet ?? '',
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
    const rows = selectedIds.value
        .map((id) => selectedRows[id])
        .filter(Boolean)
        .map(toExcelRow)

    const ws = XLSX.utils.json_to_sheet(rows)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, 'BienThe')

    const buf = XLSX.write(wb, { bookType: 'xlsx', type: 'array' })
    const blob = new Blob([buf], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })

    const code = safeName(productCode.value || `sp_${props.id}`)
    const fileName = `bien-the_${code}.xlsx`

    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = fileName
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)

    cancelExportMode()
    success('Xuất Excel thành công')
  } catch (e) {
    console.error(e)
    error('Xuất Excel thất bại')
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
  anh: '',
  mediaPrimaryId: null
})

watch(() => edit.soLuongTon, (sl) => {
  // ✅ nếu tồn = 0 thì ép trạng thái false
  if (Number(sl ?? 0) <= 0) edit.trangThai = false
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
  edit.mediaPrimaryId = v.mediaPrimaryId ?? null
}
function closeEditModal() { edit.open = false }

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
    const uploaded = normalizeUploadResponse(await uploadImage(file))
    if (!uploaded.url) throw new Error('Upload không trả url')
    edit.anh = uploaded.url
    edit.mediaPrimaryId = uploaded.mediaAssetId
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

  // ✅ tồn = 0 => ép trangThai false
  const fixedTrangThai = sl > 0 ? !!edit.trangThai : false

  edit.saving = true
  try {
    await updateDetail(edit.id, {
      idSanPham: edit.idSanPham,
      idKichCo: edit.idKichCo,
      idMauSac: edit.idMauSac,
      soLuongTon: Math.floor(sl),
      donGia: Number(edit.donGia),
      ghiChu: edit.ghiChu,
      trangThai: fixedTrangThai,
      anh: edit.anh,
      mediaPrimaryId: edit.mediaPrimaryId
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
  if (!v?.id) return
  if (togglingIds.has(v.id)) return
  if (isOutOfStock(v)) {
    error('Số lượng tồn = 0 nên không thể bật “Còn hàng”.')
    return
  }
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

  if (isOutOfStock(v) && confirmToggle.next === true) {
    closeConfirmToggle()
    error('Số lượng tồn = 0 nên không thể bật “Còn hàng”.')
    return
  }

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
      anh: v.anh ?? '',
      mediaPrimaryId: v.mediaPrimaryId ?? null
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
    .replace(/_/g, ' ')
    .replace(/-/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
}

const COLOR_MAP = {
  // trung tính
  den: '#111827',
  black: '#111827',
  charcoal: '#1f2937',
  than: '#374151',

  trang: '#ffffff',
  white: '#ffffff',
  sua: '#fffdfa',
  ivory: '#fffff0',
  kem: '#fff7ed',
  cream: '#fff7ed',
  be: '#f5f5dc',
  beige: '#f5f5dc',
  nude: '#eec9a5',

  xam: '#9ca3af',
  ghi: '#9ca3af',
  gray: '#9ca3af',
  grey: '#9ca3af',
  'xam dam': '#6b7280',
  'ghi dam': '#6b7280',
  'xam nhat': '#d1d5db',
  'ghi nhat': '#d1d5db',

  bac: '#c0c0c0',
  silver: '#c0c0c0',

  // đỏ / hồng / tím
  do: '#ef4444',
  red: '#ef4444',
  'do tuoi': '#ff3b30',
  'do do': '#b91c1c',
  'do dam': '#b91c1c',
  burgundy: '#800020',
  bordo: '#800020',
  maroon: '#800000',
  wine: '#722f37',
  'do ruou': '#722f37',

  hong: '#ec4899',
  pink: '#ec4899',
  'hong nhat': '#f9a8d4',
  rose: '#f43f5e',
  'hong sen': '#db2777',
  magenta: '#ff00ff',
  fuchsia: '#ff00ff',

  tim: '#8b5cf6',
  purple: '#8b5cf6',
  violet: '#7c3aed',
  lavender: '#c4b5fd',
  lilac: '#c8a2c8',

  // vàng / cam / nâu
  vang: '#eab308',
  yellow: '#eab308',
  gold: '#d4af37',
  golden: '#d4af37',
  mustard: '#d97706',
  'vang chanh': '#facc15',

  cam: '#f97316',
  orange: '#f97316',
  coral: '#fb7185',
  peach: '#fdba74',

  nau: '#8b5e3c',
  brown: '#8b5e3c',
  chocolate: '#7b3f00',
  coffee: '#6f4e37',
  cafe: '#6f4e37',
  caramel: '#b45309',
  mocha: '#7c5a43',

  // xanh dương
  xanh: '#3b82f6',
  blue: '#3b82f6',
  'xanh duong': '#3b82f6',
  'xanh da troi': '#0ea5e9',
  sky: '#0ea5e9',
  skyblue: '#0ea5e9',
  'xanh coban': '#2563eb',
  cobalt: '#2563eb',
  royal: '#4169e1',
  'royal blue': '#4169e1',
  'xanh navy': '#1e3a8a',
  'xanh than': '#1e3a8a',
  navy: '#1e3a8a',
  'midnight blue': '#191970',

  // xanh lá
  'xanh la': '#22c55e',
  green: '#22c55e',
  'xanh luc': '#16a34a',
  lime: '#84cc16',
  olive: '#708238',
  mint: '#6ee7b7',
  'xanh mint': '#6ee7b7',
  'xanh reu': '#4d7c0f',
  moss: '#4d7c0f',

  // xanh ngọc / cyan
  'xanh ngoc': '#14b8a6',
  teal: '#0f766e',
  turquoise: '#40e0d0',
  cyan: '#06b6d4',
  aqua: '#06b6d4',

  // khác
  kemsua: '#fff8dc',
  'da bo': '#c68642'
}

function getColorCode(name) {
  if (!name) return '#e5e7eb'

  const raw = String(name || '').trim()
  const normalized = normalizeColorName(raw)

  // ưu tiên đọc mã màu thật nếu người dùng lưu kiểu: "Đỏ (#ff0000)"
  const hexMatch = raw.match(/#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})\b/)
  if (hexMatch) return hexMatch[0]

  // ưu tiên match exact
  if (COLOR_MAP[normalized]) return COLOR_MAP[normalized]

  // match theo từ khóa
  if (normalized.includes('navy') || normalized.includes('than')) return COLOR_MAP['xanh navy']
  if (normalized.includes('coban') || normalized.includes('cobalt')) return COLOR_MAP['xanh coban']
  if (normalized.includes('royal')) return COLOR_MAP.royal
  if (normalized.includes('da troi') || normalized.includes('sky')) return COLOR_MAP['xanh da troi']

  if (normalized.includes('xanh') && (normalized.includes('la') || normalized.includes('luc'))) return COLOR_MAP['xanh la']
  if (normalized.includes('xanh') && normalized.includes('reu')) return COLOR_MAP['xanh reu']
  if (normalized.includes('xanh') && normalized.includes('mint')) return COLOR_MAP['xanh mint']
  if (normalized.includes('xanh') && normalized.includes('ngoc')) return COLOR_MAP['xanh ngoc']
  if (normalized.includes('xanh') && normalized.includes('duong')) return COLOR_MAP['xanh duong']
  if (normalized.includes('xanh')) return COLOR_MAP.xanh

  if (normalized.includes('den') || normalized.includes('black')) return COLOR_MAP.den
  if (normalized.includes('trang') || normalized.includes('white')) return COLOR_MAP.trang
  if (normalized.includes('xam') || normalized.includes('ghi') || normalized.includes('gray') || normalized.includes('grey')) return COLOR_MAP.xam
  if (normalized.includes('bac') || normalized.includes('silver')) return COLOR_MAP.bac

  if (normalized.includes('do') || normalized.includes('red')) return COLOR_MAP.do
  if (normalized.includes('hong') || normalized.includes('pink')) return COLOR_MAP.hong
  if (normalized.includes('tim') || normalized.includes('purple') || normalized.includes('violet')) return COLOR_MAP.tim

  if (normalized.includes('vang') || normalized.includes('yellow') || normalized.includes('gold')) return COLOR_MAP.vang
  if (normalized.includes('cam') || normalized.includes('orange') || normalized.includes('coral') || normalized.includes('peach')) return COLOR_MAP.cam

  if (normalized.includes('nau') || normalized.includes('brown') || normalized.includes('cafe') || normalized.includes('coffee') || normalized.includes('chocolate') || normalized.includes('caramel') || normalized.includes('mocha')) {
    return COLOR_MAP.nau
  }

  if (normalized.includes('be') || normalized.includes('beige') || normalized.includes('kem') || normalized.includes('cream') || normalized.includes('ivory') || normalized.includes('nude')) {
    return COLOR_MAP.be
  }

  return '#e5e7eb'
}
</script>

<style scoped>
.product-detail-page {
  flex: 1 1 auto;
  min-width: 0;
  width: 100%;
  box-sizing: border-box;
  background: #ffffff;
  min-height: 100vh;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
  overflow-x: hidden;
  padding: 16px;
}

*,
*::before,
*::after {
  box-sizing: border-box;
}

.content-wrapper {
  width: 100%;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.card {
  width: 100%;
  min-width: 0;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 0;
  flex-wrap: wrap;
}

.page-title {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
  flex: 1 1 420px;
}

.page-title-text {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
  color: #111827;
  line-height: 1.35;
  overflow-wrap: anywhere;
}

.title-sep {
  margin: 0 8px;
  color: #9ca3af;
  font-weight: 700;
}

.title-code {
  color: #111827;
  font-weight: 700;
}

.title-name {
  overflow-wrap: anywhere;
}

.page-sub {
  color: #6b7280;
  font-size: 13px;
  overflow-wrap: anywhere;
}

.sub-label {
  color: #9ca3af;
}

.page-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
  align-items: center;
  max-width: 100%;
  flex: 0 1 auto;
}

.filter-card {
  overflow: visible;
}

.filter-head {
  background: #1e293b;
  color: #fff;
  padding: 10px 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-head-left {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
}

.filter-head-right {
  font-size: 12px;
  opacity: 0.9;
}

.filter-body {
  padding: 14px;
}

.form-label {
  font-size: 13px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 6px;
}

.status-radio {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: #111827;
  flex-wrap: wrap;
}

.status-radio input {
  transform: translateY(1px);
  margin-right: 6px;
}

.price-label {
  font-size: 13px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 6px;
}

.price-green {
  color: #059669;
  font-weight: 700;
}

.hint {
  display: block;
  margin-top: 6px;
  color: #6b7280;
  overflow-wrap: anywhere;
}

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
  background: #059669;
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
  background: #fff;
  border: 2px solid #059669;
  border-radius: 50%;
  cursor: pointer;
  margin-top: -7px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

.range-slider input[type="range"]::-webkit-slider-runnable-track {
  width: 100%;
  height: 4px;
  cursor: pointer;
  background: transparent;
  border-radius: 2px;
}

.filter-reset {
  display: flex;
  justify-content: flex-end;
  padding-top: 6px;
}

.filter-reset .btn {
  color: #111827;
}

.filter-reset .btn:hover {
  background: #f3f4f6;
  border-radius: 6px;
}

.table-card {
  padding: 0;
  margin-top: 0;
}

.table-responsive {
  width: 100%;
  min-width: 0;
  overflow-x: auto;
  overflow-y: hidden;
  -webkit-overflow-scrolling: touch;
}

.variants-table {
  width: 100%;
  table-layout: fixed;
  border-collapse: separate;
  border-spacing: 0;
  margin: 0;
  min-width: 1480px;
}

.variants-table thead th {
  background: #1e293b;
  color: #fff;
  padding: 10px 12px;
  text-align: center;
  font-weight: 700;
  border-bottom: 1px solid #e5e7eb;
  white-space: nowrap;
  vertical-align: middle;
}

.variants-table td {
  padding: 12px;
  border-bottom: 1px solid #eef2f7;
  vertical-align: middle !important;
  color: #374151;
  overflow-wrap: anywhere;
}

.col-check {
  width: 46px;
  min-width: 46px;
}

.col-stt {
  width: 70px;
}

.col-img {
  width: 220px;
}

.col-pcode {
  width: 150px;
}

.col-name {
  width: 220px;
}

.col-code {
  width: 160px;
}

.col-color {
  width: 160px;
}

.col-size {
  width: 110px;
}

.col-stock {
  width: 110px;
}

.col-price {
  width: 140px;
}

.col-status {
  width: 120px;
}

.col-action {
  width: 140px;
}

.text-center {
  text-align: center;
}

.text-bold {
  font-weight: 700;
  color: #111827;
}

.text-highlight {
  color: #0f766e;
  font-weight: 700;
}

.img-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.img-cell--lg {
  min-height: 180px;
}

.variant-img {
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  background: #fff;
  max-width: 100%;
  display: block;
}

.variant-img--lg {
  width: 160px;
  height: 160px;
}

.variant-img.preview {
  width: 140px;
  height: 140px;
}

.no-img {
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #6b7280;
  background: #f3f4f6;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  line-height: 1.15;
}

.no-img--lg {
  width: 160px;
  height: 160px;
  font-size: 13px;
  padding: 8px;
  max-width: 100%;
}

.color-cell {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  max-width: 100%;
}

.color-dot,
.color-dot-lg {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 1px solid #d1d5db;
  flex-shrink: 0;
}

.color-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.badge-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}

.badge-success {
  background: #d1fae5;
  color: #065f46;
}

.badge-danger {
  background: #fee2e2;
  color: #991b1b;
}

.action-buttons {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}

.edit-btn {
  width: 32px;
  height: 32px;
  padding: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
}

.switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 22px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

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
  width: 18px;
  height: 18px;
  left: 2px;
  top: 2px;
  background: #fff;
  transition: 0.2s;
  border-radius: 50%;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.15);
}

.switch input:checked + .slider {
  background: #2563eb;
}

.switch input:checked + .slider:before {
  transform: translateX(22px);
}

.paging-bar {
  margin: 14px;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 12px;
}

.paging-left {
  justify-self: start;
  color: #6b7280;
  font-size: 13px;
}

.paging-center {
  justify-self: center;
  display: flex;
  align-items: center;
  gap: 10px;
}

.paging-right {
  justify-self: end;
}

.paging-page {
  width: 120px;
}

.paging-size {
  width: 160px;
}

.error-text {
  color: #b02a37;
  font-weight: 700;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 12px;
}

.modals {
  background: #fff;
  width: 560px;
  max-width: min(560px, calc(100vw - 24px));
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 18px 50px rgba(0, 0, 0, 0.22);
  overflow: hidden;
  max-height: calc(100vh - 24px);
  overflow-y: auto;
}

.confirm-modal {
  width: 520px;
  max-width: min(520px, calc(100vw - 24px));
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  border-bottom: 1px solid #eef2f7;
  gap: 12px;
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #111827;
  overflow-wrap: anywhere;
}

.close-btn {
  border: none;
  background: transparent;
  font-size: 22px;
  line-height: 1;
  cursor: pointer;
  color: #6b7280;
  flex-shrink: 0;
}

.modal-body {
  padding: 14px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 12px 14px;
  border-top: 1px solid #eef2f7;
  flex-wrap: wrap;
}

@media (max-width: 1199.98px) {
  .variants-table {
    min-width: 1380px;
  }

  .col-img {
    width: 200px;
  }

  .variant-img--lg,
  .no-img--lg {
    width: 140px;
    height: 140px;
  }

  .img-cell--lg {
    min-height: 150px;
  }
}

@media (max-width: 991.98px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .page-actions {
    justify-content: flex-start;
  }

  .paging-bar {
    grid-template-columns: 1fr;
  }

  .paging-left,
  .paging-center,
  .paging-right {
    justify-self: stretch;
  }

  .paging-center {
    justify-content: center;
    flex-wrap: wrap;
  }

  .paging-right {
    display: flex;
    justify-content: flex-start;
  }

  .variants-table {
    min-width: 1280px;
  }
}

@media (max-width: 767.98px) {
  .product-detail-page {
    padding: 12px;
  }

  .page-title-text {
    font-size: 1.05rem;
  }

  .page-actions {
    width: 100%;
    gap: 8px;
  }

  .page-actions .btn {
    width: 100%;
  }

  .filter-head {
    align-items: flex-start;
  }

  .filter-head-right {
    width: 100%;
  }

  .status-radio {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }

  .variants-table {
    min-width: 1100px;
  }

  .variant-img.preview {
    width: 110px;
    height: 110px;
  }

  .modals,
  .confirm-modal {
    width: 100%;
    max-width: calc(100vw - 16px);
  }

  .modal-footer {
    justify-content: stretch;
  }

  .modal-footer .btn {
    flex: 1 1 100%;
  }
}

@media (max-width: 575.98px) {
  .product-detail-page {
    padding: 8px;
  }

  .filter-body,
  .modal-body,
  .modal-header,
  .modal-footer {
    padding-left: 12px;
    padding-right: 12px;
  }

  .paging-page,
  .paging-size {
    width: 100%;
  }

  .paging-center {
    width: 100%;
  }
}


.filter-multiselect {
  min-height: 38px;
}

.filter-multiselect :deep(.multiselect__tags) {
  min-height: 38px;
  border: 1px solid #ced4da;
  border-radius: 0.375rem;
  padding-top: 7px;
}

.filter-multiselect :deep(.multiselect__placeholder),
.filter-multiselect :deep(.multiselect__single) {
  margin-bottom: 0;
  font-size: 0.95rem;
}

.filter-multiselect {
  width: 100%;
  max-width: 100%;
  min-width: 0;
}

.filter-multiselect :deep(.multiselect) {
  width: 100%;
  max-width: 100%;
  min-width: 0;
}

.filter-multiselect :deep(.multiselect__tags) {
  width: 100%;
  max-width: 100%;
  min-height: 38px;
  border: 1px solid #d1d5db;
  border-radius: 0.375rem;
  padding: 7px 40px 6px 12px;
  box-sizing: border-box;
  overflow: hidden;
}

.filter-multiselect:focus-within :deep(.multiselect__tags),
.filter-multiselect :deep(.multiselect--active .multiselect__tags) {
  border-color: #2563eb !important;
  box-shadow: 0 0 0 0.2rem rgba(37, 99, 235, 0.15) !important;
}

.filter-multiselect :deep(.multiselect__placeholder),
.filter-multiselect :deep(.multiselect__single) {
  display: block;
  max-width: 100%;
  margin-bottom: 0;
  font-size: 0.95rem;
  line-height: 22px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.filter-multiselect :deep(.multiselect__input) {
  max-width: 100%;
  min-width: 0;
  margin-bottom: 0;
  font-size: 0.95rem;
  line-height: 22px;
}

.filter-multiselect :deep(.multiselect__content-wrapper) {
  z-index: 120;
  max-width: 100%;
  border-color: #2563eb;
}

.filter-multiselect :deep(.multiselect__option--highlight),
.filter-multiselect :deep(.multiselect__option--selected.multiselect__option--highlight) {
  background: #2563eb !important;
  color: #fff !important;
}

.filter-multiselect :deep(.multiselect__option--highlight::after),
.filter-multiselect :deep(.multiselect__option--selected.multiselect__option--highlight::after) {
  background: #2563eb !important;
  color: #fff !important;
}

.filter-multiselect :deep(.multiselect__option--selected) {
  background: #dbeafe !important;
  color: #1e40af !important;
  font-weight: 700;
}

.filter-multiselect :deep(.multiselect__spinner::before),
.filter-multiselect :deep(.multiselect__spinner::after) {
  border-top-color: #2563eb !important;
}

.product-detail-page {
  width: 100%;
  max-width: 100%;
  min-width: 0;
  box-sizing: border-box;
  overflow-x: hidden;
}

.product-detail-page *,
.product-detail-page *::before,
.product-detail-page *::after {
  box-sizing: border-box;
}

:global(html),
:global(body),
:global(#app) {
  max-width: 100%;
  overflow-x: hidden;
}

.page-header,
.content-wrapper,
.filter-card,
.table-card,
.filter-body,
.table-responsive {
  width: 100%;
  max-width: 100%;
  min-width: 0;
}

.page-header {
  flex-wrap: wrap;
  overflow: hidden;
}

.page-title {
  min-width: 0;
  max-width: 100%;
  flex: 1 1 420px;
}

.page-title-text {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.page-actions {
  min-width: 0;
  max-width: 100%;
  flex: 0 1 auto;
  flex-wrap: wrap;
}

.filter-card {
  overflow: visible;
}

.filter-body {
  overflow: visible;
}

.filter-body .row {
  width: 100%;
  max-width: 100%;
  margin-left: 0 !important;
  margin-right: 0 !important;
}

.filter-body .row > [class*="col-"] {
  min-width: 0;
  padding-left: 6px;
  padding-right: 6px;
}

.form-control,
.form-select,
.filter-multiselect {
  width: 100%;
  max-width: 100%;
  min-width: 0;
}

.table-card {
  overflow: hidden;
}

.table-responsive {
  display: block;
  overflow-x: auto;
  overflow-y: hidden;
  -webkit-overflow-scrolling: touch;
}

.variants-table {
  width: 100% !important;
  min-width: 1180px !important;
  max-width: 100% !important;
  table-layout: fixed !important;
}

.variants-table th,
.variants-table td {
  white-space: normal;
  overflow-wrap: anywhere;
}

.col-check {
  width: 46px !important;
}

.col-stt {
  width: 60px !important;
}

.col-img {
  width: 150px !important;
}

.col-pcode {
  width: 120px !important;
}

.col-name {
  width: 180px !important;
}

.col-code {
  width: 140px !important;
}

.col-color {
  width: 130px !important;
}

.col-size {
  width: 90px !important;
}

.col-stock {
  width: 110px !important;
}

.col-price {
  width: 130px !important;
}

.col-status {
  width: 120px !important;
}

.col-action {
  width: 120px !important;
}

.img-cell--lg {
  width: 120px;
  height: 120px;
  margin: 0 auto;
}

.variant-img--lg {
  width: 120px;
  height: 120px;
}

.price-green {
  color: #2563eb !important;
}

.slider-range {
  background: #2563eb !important;
}

.range-slider input[type="range"] {
  accent-color: #2563eb !important;
}

.range-slider input[type="range"]::-webkit-slider-thumb {
  border-color: #2563eb !important;
}

@media (max-width: 991.98px) {
  .product-detail-page {
    padding-left: 10px;
    padding-right: 10px;
  }

  .page-title-text {
    white-space: normal;
  }

  .variants-table {
    min-width: 1050px !important;
  }
}
.product-detail-page {
  width: 100% !important;
  max-width: calc(100vw - 330px) !important;
  min-width: 0 !important;
  margin-left: 0 !important;
  margin-right: 0 !important;
  overflow-x: hidden !important;
}

.content-wrapper,
.page-header,
.filter-card,
.table-card {
  width: 100% !important;
  max-width: 100% !important;
  min-width: 0 !important;
}

.page-header {
  display: flex !important;
  align-items: flex-start !important;
  justify-content: space-between !important;
  gap: 10px !important;
  flex-wrap: wrap !important;
  overflow: hidden !important;
}

.page-title {
  flex: 1 1 420px !important;
  min-width: 0 !important;
  max-width: 100% !important;
}

.page-title-text {
  max-width: 100% !important;
  overflow: hidden !important;
  text-overflow: ellipsis !important;
  white-space: nowrap !important;
}

.page-actions {
  flex: 0 1 auto !important;
  min-width: 0 !important;
  max-width: 100% !important;
  display: flex !important;
  justify-content: flex-end !important;
  flex-wrap: wrap !important;
  gap: 8px !important;
}

.page-actions .btn {
  flex: 0 0 auto !important;
  white-space: nowrap !important;
}

.filter-card {
  overflow: hidden !important;
}

.filter-head,
.filter-body {
  width: 100% !important;
  max-width: 100% !important;
  min-width: 0 !important;
}

.filter-body {
  overflow: visible !important;
  padding-left: 12px !important;
  padding-right: 12px !important;
}

.filter-body .row {
  width: 100% !important;
  max-width: 100% !important;
  min-width: 0 !important;
  margin-left: 0 !important;
  margin-right: 0 !important;
  --bs-gutter-x: 12px;
}

.filter-body .row > [class*="col-"] {
  min-width: 0 !important;
}

.filter-body .form-control,
.filter-body .form-select,
.filter-body .filter-multiselect {
  width: 100% !important;
  max-width: 100% !important;
  min-width: 0 !important;
}

.table-card {
  overflow: hidden !important;
}

.table-responsive {
  width: 100% !important;
  max-width: 100% !important;
  min-width: 0 !important;
  overflow-x: auto !important;
  overflow-y: hidden !important;
}

.variants-table {
  width: 100% !important;
  min-width: 0 !important;
  max-width: 100% !important;
  table-layout: fixed !important;
}

.variants-table th,
.variants-table td {
  overflow: hidden !important;
  text-overflow: ellipsis !important;
  overflow-wrap: anywhere !important;
}

.col-stt {
  width: 5% !important;
}

.col-img {
  width: 12% !important;
}

.col-pcode {
  width: 10% !important;
}

.col-name {
  width: 14% !important;
}

.col-code {
  width: 12% !important;
}

.col-color {
  width: 11% !important;
}

.col-size {
  width: 8% !important;
}

.col-stock {
  width: 10% !important;
}

.col-price {
  width: 11% !important;
}

.col-status {
  width: 10% !important;
}

.col-action {
  width: 9% !important;
}

.img-cell--lg {
  width: 110px !important;
  height: 110px !important;
  max-width: 100% !important;
  margin: 0 auto !important;
}

.variant-img--lg {
  width: 110px !important;
  height: 110px !important;
  max-width: 100% !important;
}

@media (max-width: 1500px) {
  .product-detail-page {
    max-width: calc(100vw - 315px) !important;
  }

  .page-title {
    flex-basis: 360px !important;
  }

  .page-actions .btn {
    padding-left: 10px !important;
    padding-right: 10px !important;
  }

  .img-cell--lg,
  .variant-img--lg {
    width: 100px !important;
    height: 100px !important;
  }
}

@media (max-width: 1200px) {
  .product-detail-page {
    max-width: 100% !important;
  }

  .page-title-text {
    white-space: normal !important;
  }

  .variants-table {
    min-width: 1050px !important;
  }
}
/* ===== FIX THEO YÊU CẦU: khoảng giá xanh lá + combobox lọc dài hơn ===== */
.price-green {
  color: #059669 !important;
}

.slider-range {
  background: #059669 !important;
}

.range-slider input[type="range"] {
  accent-color: #059669 !important;
}

.range-slider input[type="range"]::-webkit-slider-thumb {
  border-color: #059669 !important;
}

.range-slider input[type="range"]::-moz-range-thumb {
  border-color: #059669 !important;
}

.filter-multiselect {
  width: 100% !important;
  min-width: 0 !important;
}

.filter-multiselect :deep(.multiselect),
.filter-multiselect :deep(.multiselect__tags) {
  width: 100% !important;
  min-width: 0 !important;
}

@media (min-width: 992px) {
  .filter-body .col-lg-4 .filter-multiselect {
    min-width: 100% !important;
  }
}
.price-green {
  color: #059669 !important;
}

.slider-range {
  background: #059669 !important;
}

.range-slider input[type="range"] {
  accent-color: #059669 !important;
}

.range-slider input[type="range"]::-webkit-slider-thumb {
  border-color: #059669 !important;
}

.range-slider input[type="range"]::-moz-range-thumb {
  border-color: #059669 !important;
}

.filter-card {
  overflow: visible !important;
}

.filter-body {
  overflow: visible !important;
}

.filter-body .row {
  overflow: visible !important;
}

.filter-body .form-select {
  font-weight: 400 !important;
  color: #374151 !important;
  font-size: 0.95rem !important;
}

.filter-body .form-select option {
  font-weight: 400 !important;
}

.filter-multiselect {
  width: 100% !important;
  min-width: 0 !important;
}

.filter-multiselect :deep(.multiselect) {
  width: 100% !important;
  min-width: 0 !important;
}

.filter-multiselect :deep(.multiselect__tags) {
  width: 100% !important;
  min-width: 0 !important;
  padding: 7px 46px 6px 12px !important;
  overflow: visible !important;
}

.filter-multiselect :deep(.multiselect__select) {
  width: 42px !important;
  right: 0 !important;
}

.filter-multiselect :deep(.multiselect__placeholder),
.filter-multiselect :deep(.multiselect__single) {
  max-width: 100% !important;
  padding-right: 0 !important;
  overflow: hidden !important;
  text-overflow: ellipsis !important;
  white-space: nowrap !important;
}

.filter-multiselect :deep(.multiselect__content-wrapper) {
  width: 100% !important;
  min-width: 100% !important;
  max-width: none !important;
  z-index: 999 !important;
  border-color: #2563eb !important;
}

.table-responsive {
  overflow-x: auto !important;
  overflow-y: visible !important;
}

.variants-table {
  width: 100% !important;
  min-width: 1480px !important;
  max-width: none !important;
  table-layout: fixed !important;
}

.col-img {
  width: 220px !important;
}

.img-cell--lg {
  width: 200px !important;
  height: 200px !important;
  min-height: 200px !important;
  max-width: 100% !important;
  margin: 0 auto !important;
}

.variant-img--lg,
.no-img--lg {
  width: 200px !important;
  height: 200px !important;
  max-width: 100% !important;
}

@media (max-width: 1500px) {
  .variants-table {
    min-width: 1450px !important;
  }

  .col-img {
    width: 210px !important;
  }

  .img-cell--lg,
  .variant-img--lg,
  .no-img--lg {
    width: 190px !important;
    height: 190px !important;
    min-height: 190px !important;
  }
}

@media (max-width: 1200px) {
  .variants-table {
    min-width: 1280px !important;
  }
}
/* ===== FIX: canh đều filter giống màn danh sách sản phẩm ===== */
.filter-body {
  padding: 16px !important;
  overflow: visible !important;
}

.filter-body .row {
  width: 100% !important;
  max-width: 100% !important;
  margin-left: 0 !important;
  margin-right: 0 !important;
  --bs-gutter-x: 18px;
  --bs-gutter-y: 18px;
  align-items: start !important;
}

.filter-body .row > [class*="col-"] {
  min-width: 0 !important;
  padding-left: 6px !important;
  padding-right: 6px !important;
}

.filter-body .form-label,
.filter-body .price-label {
  min-height: 21px !important;
  display: flex !important;
  align-items: center !important;
  margin-bottom: 6px !important;
  font-size: 0.875rem !important;
  font-weight: 500 !important;
  color: #374151 !important;
}

.filter-body .form-control,
.filter-body .form-select,
.filter-body .filter-multiselect :deep(.multiselect__tags) {
  height: 40px !important;
  min-height: 40px !important;
  border-radius: 6px !important;
}

.filter-body .form-control,
.filter-body .form-select {
  padding: 8px 12px !important;
  font-size: 0.875rem !important;
  font-weight: 400 !important;
  line-height: 1.35 !important;
  color: #374151 !important;
  background-color: #fff !important;
}

.filter-body .form-control::placeholder {
  color: #9ca3af !important;
  opacity: 1 !important;
  font-weight: 400 !important;
}

.filter-body .form-select.placeholder-select {
  color: #9ca3af !important;
  font-weight: 400 !important;
}

.filter-body .form-select,
.filter-body .form-select option {
  font-weight: 400 !important;
}

.filter-multiselect {
  width: 100% !important;
  min-width: 0 !important;
  min-height: 40px !important;
}

.filter-multiselect :deep(.multiselect),
.filter-multiselect :deep(.multiselect__tags) {
  width: 100% !important;
  min-width: 0 !important;
}

.filter-multiselect :deep(.multiselect__tags) {
  padding: 8px 46px 7px 12px !important;
  overflow: visible !important;
  border: 1px solid #d1d5db !important;
}

.filter-multiselect :deep(.multiselect__placeholder),
.filter-multiselect :deep(.multiselect__single) {
  margin: 0 !important;
  padding: 0 !important;
  font-size: 0.875rem !important;
  font-weight: 400 !important;
  line-height: 24px !important;
  color: #9ca3af !important;
  overflow: hidden !important;
  text-overflow: ellipsis !important;
  white-space: nowrap !important;
}

.filter-multiselect :deep(.multiselect__single) {
  color: #374151 !important;
}

.filter-multiselect :deep(.multiselect__content-wrapper) {
  width: 100% !important;
  min-width: 100% !important;
  max-width: none !important;
  z-index: 999 !important;
}

.price-green {
  color: #059669 !important;
  font-weight: 700 !important;
}

.slider-range {
  background: #059669 !important;
}

.range-slider input[type="range"] {
  accent-color: #059669 !important;
}

.range-slider input[type="range"]::-webkit-slider-thumb {
  border-color: #059669 !important;
}

.range-slider input[type="range"]::-moz-range-thumb {
  border-color: #059669 !important;
}

.status-radio {
  min-height: 40px !important;
  align-items: center !important;
}

@media (min-width: 992px) {
  .filter-body .col-lg-4 {
    width: 33.333333% !important;
    flex: 0 0 auto !important;
  }

  .filter-body .col-lg-5 {
    width: 41.666667% !important;
    flex: 0 0 auto !important;
  }

  .filter-body .col-lg-3 {
    width: 25% !important;
    flex: 0 0 auto !important;
  }
}
</style>