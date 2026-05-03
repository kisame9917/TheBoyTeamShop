<template>
  <div class="variant-page">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div>
        <h5 class="mb-0">Quản lý sản phẩm / Danh sách biến thể</h5>
      </div>

      <div class="d-flex gap-2 flex-wrap">
        <button class="btn btn-outline-secondary btn-sm" @click="openQrModal">
          <i class="bi bi-qr-code-scan me-1"></i>Quét QR
        </button>
        <button
          v-if="!isExportSelecting"
          class="btn btn-outline-primary btn-sm"
          type="button"
          @click="startExportSelect"
        >
          <i class="bi bi-download me-1"></i>Tải Excel
        </button>
        <button
          v-else
          class="btn btn-outline-primary btn-sm"
          type="button"
          @click="downloadExcel"
        >
          <i class="bi bi-download me-1"></i>Xuất đã chọn ({{ selectedExportCount }})
        </button>
        <button
          v-if="isExportSelecting"
          class="btn btn-outline-secondary btn-sm"
          type="button"
          @click="cancelExportSelect"
        >
          <i class="bi bi-x-circle me-1"></i>Hủy chọn
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
          <div class="col-12 col-lg-5">
            <label class="form-label small fw-semibold">Tìm kiếm</label>
            <input
                v-model="filters.keyword"
                class="form-control"
                placeholder="Tìm theo mã SP, mã SP chi tiết, tên, màu, kích cỡ..."
            />
          </div>

          <div class="col-12 col-lg-4">
            <label class="form-label small fw-semibold">Màu sắc</label>
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
            <label class="form-label small fw-semibold">Số lượng tồn</label>
            <select v-model="filters.stockRange" class="form-select" :class="{ 'placeholder-select': !filters.stockRange }">
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
          <div class="col-12 col-lg-5">
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

          <div class="col-12 col-lg-4">
            <label class="form-label small fw-semibold">Kích cỡ</label>
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
            <th v-if="isExportSelecting" class="text-center select-col">
              <input
                class="form-check-input"
                type="checkbox"
                :checked="isAllCurrentPageSelected"
                @change="toggleCurrentPageExport($event.target.checked)"
              />
            </th>
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
            <th class="text-center">QR biến thể</th>
            <th class="text-center">Hành động</th>
          </tr>
          </thead>

          <tbody>
          <tr v-for="(v, index) in pagedItems" :key="v.id">
            <td v-if="isExportSelecting" class="text-center select-col">
              <input
                class="form-check-input"
                type="checkbox"
                :checked="isVariantSelected(v)"
                @change="toggleVariantExport(v, $event.target.checked)"
              />
            </td>
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
                    isVariantAvailable(v) ? 'bg-success-subtle text-success' : 'bg-danger-subtle text-danger'
                  ]"
                >
                  {{ variantStatusText(v) }}
                </span>
            </td>

            <td class="text-center">
              <div class="d-flex justify-content-center align-items-center gap-2 flex-wrap">
                <button
                  class="btn btn-outline-dark btn-sm"
                  type="button"
                  title="Xem QR"
                  @click="previewVariantQr(v)"
                >
                  <i class="bi bi-qr-code me-1"></i>Xem QR
                </button>

                <button
                  class="btn btn-outline-success btn-sm"
                  type="button"
                  title="Tải PNG"
                  @click="downloadVariantQr(v)"
                >
                  <i class="bi bi-download me-1"></i>Tải PNG
                </button>
              </div>
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
                      :checked="isVariantActive(v)"
                      :disabled="isOutOfStock(v)"
                      @click.prevent="requestToggleStatus(v)"
                  />
                </div>
              </div>
            </td>
          </tr>

          <tr v-if="loading">
            <!-- ✅ colspan tăng 1 -->
            <td :colspan="isExportSelecting ? 13 : 12" class="text-center py-4">Đang tải dữ liệu...</td>
          </tr>
          <tr v-if="!loading && pagedItems.length === 0">
            <!-- ✅ colspan tăng 1 -->
            <td :colspan="isExportSelecting ? 13 : 12" class="text-center py-4">Không có dữ liệu</td>
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


  <div v-if="showQrModal" class="modal-overlay" @click.self="closeQrModal">
    <div class="modal-box qr-modal-box">
      <div class="modal-head">
        <h6 class="mb-0">Quét QR sản phẩm / biến thể</h6>
        <button class="btn-close" type="button" @click="closeQrModal"></button>
      </div>

      <div class="modal-body p-3">
        <div class="small text-muted mb-3">
          Quét <b>mã sản phẩm</b> để lọc ra toàn bộ biến thể của sản phẩm đó, hoặc quét <b>mã SP chi tiết</b> để lọc đúng 1 biến thể.
        </div>

        <div id="variant-qr-reader" class="qr-reader-box mb-3"></div>

        <label class="form-label small fw-semibold">Hoặc dán mã</label>
        <div class="d-flex gap-2">
          <input
            v-model.trim="qrManualInput"
            type="text"
            class="form-control"
            placeholder="Nhập mã sản phẩm hoặc mã SP chi tiết..."
            @keyup.enter="applyManualQr"
          />
          <button class="btn btn-primary btn-sm" type="button" @click="applyManualQr">
            Áp dụng
          </button>
        </div>

        <div v-if="qrErrorMessage" class="text-danger small mt-3">
          {{ qrErrorMessage }}
        </div>
      </div>
    </div>
  </div>

  <div v-if="showQrPreviewModal" class="modal-overlay" @click.self="closeQrPreviewModal">
    <div class="modal-box qr-preview-modal">
      <div class="modal-head">
        <h6 class="mb-0">QR biến thể</h6>
        <button class="btn-close" type="button" @click="closeQrPreviewModal"></button>
      </div>

      <div class="modal-body p-3 text-center">
        <div class="small text-muted mb-2">Mã SP chi tiết</div>
        <div class="fw-semibold mb-3">{{ qrPreviewCode || '—' }}</div>

        <div v-if="qrPreviewUrl" class="qr-preview-frame">
          <img :src="qrPreviewUrl" alt="QR biến thể" class="qr-preview-image" />
        </div>

        <div v-else class="text-muted small py-4">Đang tạo mã QR...</div>
      </div>

      <div class="modal-foot">
        <button class="btn btn-secondary btn-sm" type="button" @click="closeQrPreviewModal">Đóng</button>
        <button class="btn btn-success btn-sm" type="button" @click="downloadVariantQr(qrPreviewVariant)" :disabled="!qrPreviewVariant">
          Tải PNG
        </button>
      </div>
    </div>
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
            <Multiselect
              v-model="selectedEditSize"
              :options="attributes.kichCo"
              track-by="id"
              label="soSize"
              placeholder="Chọn kích cỡ"
              :searchable="true"
              :allow-empty="false"
              :show-labels="false"
              class="filter-multiselect"
            />
          </div>

          <div class="col-6">
            <label class="form-label small fw-semibold">Màu sắc</label>
            <Multiselect
              v-model="selectedEditColor"
              :options="attributes.mauSac"
              track-by="id"
              label="ten"
              placeholder="Chọn màu sắc"
              :searchable="true"
              :allow-empty="false"
              :show-labels="false"
              class="filter-multiselect"
            />
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
                <input type="radio" :value="true" v-model="editingVariant.trangThai" :disabled="Number(editingVariant.soLuongTon || 0) <= 0" /> Còn hàng
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
import { ref, reactive, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { emitTabSync, TAB_SYNC_EVENTS } from "@/utils/tabSync";
import { useRouter } from 'vue-router'
import { Html5Qrcode } from 'html5-qrcode'
import QRCode from 'qrcode'
import * as XLSX from 'xlsx'
import Multiselect from 'vue-multiselect'
import 'vue-multiselect/dist/vue-multiselect.css'
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

const selectedColorFilter = computed({
  get() {
    return attributes.mauSac.find((c) => c.ten === filters.color) || null
  },
  set(value) {
    filters.color = value?.ten || ''
    currentPage.value = 0
  }
})

const selectedSizeFilter = computed({
  get() {
    return attributes.kichCo.find((s) => String(s.soSize) === String(filters.size)) || null
  },
  set(value) {
    filters.size = value?.soSize ?? ''
    currentPage.value = 0
  }
})

const selectedEditSize = computed({
  get() {
    return attributes.kichCo.find((s) => String(s.id) === String(editingVariant.idKichCo)) || null
  },
  set(value) {
    editingVariant.idKichCo = value?.id || ''
  }
})

const selectedEditColor = computed({
  get() {
    return attributes.mauSac.find((c) => String(c.id) === String(editingVariant.idMauSac)) || null
  },
  set(value) {
    editingVariant.idMauSac = value?.id || ''
  }
})

/** modals (giữ nguyên) */
const showQrModal = ref(false)
const showExportModal = ref(false)
const showConfirmToggle = ref(false)
const pendingVariant = ref(null)
const pendingNext = ref(false)
const toggleLoading = ref(false)

const showEditModal = ref(false)
const qrManualInput = ref('')
const qrErrorMessage = ref('')
const showQrPreviewModal = ref(false)
const qrPreviewUrl = ref('')
const qrPreviewCode = ref('')
const qrPreviewVariant = ref(null)
const isExportSelecting = ref(false)
const selectedExportIds = ref([])
let variantQrScanner = null

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

function normalizeQrValue(value) {
  return String(value || '').trim().toLowerCase()
}

function buildVariantQrText(variant) {
  return String(variant?.maSanPhamChiTiet || '').trim()
}

async function makeVariantQrDataUrl(variant, width = 320) {
  const text = buildVariantQrText(variant)
  if (!text) throw new Error('Biến thể chưa có mã SP chi tiết')

  return QRCode.toDataURL(text, {
    width,
    margin: 1
  })
}

async function previewVariantQr(variant) {
  try {
    qrPreviewVariant.value = variant
    qrPreviewCode.value = buildVariantQrText(variant)
    qrPreviewUrl.value = await makeVariantQrDataUrl(variant, 320)
    showQrPreviewModal.value = true
  } catch (e) {
    console.error(e)
    error('Không tạo được mã QR cho biến thể')
  }
}

function closeQrPreviewModal() {
  showQrPreviewModal.value = false
  qrPreviewUrl.value = ''
  qrPreviewCode.value = ''
  qrPreviewVariant.value = null
}

async function downloadVariantQr(variant) {
  try {
    const text = buildVariantQrText(variant)
    if (!text) {
      error('Biến thể chưa có mã SP chi tiết')
      return
    }

    const url = await makeVariantQrDataUrl(variant, 800)
    const a = document.createElement('a')
    a.href = url
    a.download = `${text}.png`
    document.body.appendChild(a)
    a.click()
    a.remove()
    success(`Đã tải QR của ${text}`)
  } catch (e) {
    console.error(e)
    error('Không tải được mã QR')
  }
}

async function openQrModal() {
  showQrModal.value = true
  qrErrorMessage.value = ''
  qrManualInput.value = ''
  await nextTick()
  await startVariantQr()
}

async function closeQrModal() {
  await stopVariantQr()
  showQrModal.value = false
}

async function startVariantQr() {
  try {
    if (!variantQrScanner) {
      variantQrScanner = new Html5Qrcode('variant-qr-reader')
    }

    const cameras = await Html5Qrcode.getCameras()
    if (!cameras?.length) {
      qrErrorMessage.value = 'Không tìm thấy camera.'
      return
    }

    await variantQrScanner.start(
      { deviceId: { exact: cameras[0].id } },
      { fps: 10, qrbox: { width: 240, height: 240 } },
      async (decodedText) => {
        await applyQrCode(decodedText)
      }
    )
  } catch (e) {
    console.error(e)
    qrErrorMessage.value = 'Không mở được camera hoặc bị chặn quyền.'
  }
}

async function stopVariantQr() {
  try {
    if (variantQrScanner && (await variantQrScanner.getState()) === 2) {
      await variantQrScanner.stop()
      await variantQrScanner.clear()
    }
  } catch (_) {}
}

async function applyQrCode(rawValue) {
  const code = normalizeQrValue(rawValue)
  if (!code) {
    qrErrorMessage.value = 'Mã QR không hợp lệ.'
    return
  }

  const exactVariant = allItems.value.find(
    (v) => normalizeQrValue(v.maSanPhamChiTiet) === code
  )

  if (exactVariant) {
    filters.keyword = exactVariant.maSanPhamChiTiet || ''
    currentPage.value = 0
    pageInput.value = 1
    await closeQrModal()
    success(`Đã lọc theo biến thể ${exactVariant.maSanPhamChiTiet}`)
    return
  }

  const productVariants = allItems.value.filter(
    (v) => normalizeQrValue(v.maSanPham) === code
  )

  if (productVariants.length > 0) {
    filters.keyword = productVariants[0].maSanPham || ''
    currentPage.value = 0
    pageInput.value = 1
    await closeQrModal()
    success(`Đã lọc ${productVariants.length} biến thể của mã sản phẩm ${productVariants[0].maSanPham}`)
    return
  }

  qrErrorMessage.value = 'Không tìm thấy sản phẩm hoặc biến thể theo mã QR.'
}

async function applyManualQr() {
  await applyQrCode(qrManualInput.value)
}

onMounted(() => {
  loadAttributes()
  loadData()
})

onBeforeUnmount(() => {
  void stopVariantQr()
  closeQrPreviewModal()
})

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
  try {
    const resSize = await attributeService.getAllList('kich-co')
    attributes.kichCo = sortSizes(resSize.data || [])

    const resColor = await attributeService.getAllList('mau-sac')
    attributes.mauSac = resColor.data || []
  } catch (e) {
    console.error(e)
  }
}

function normalizeStatus(value) {
  if (value === undefined || value === null || value === '') return true
  if (typeof value === 'boolean') return value
  if (typeof value === 'number') return value === 1

  const normalized = String(value)
    .trim()
    .toLowerCase()
    .replace(/đ/g, 'd')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')

  if (['true', '1', 'active', 'enabled', 'hoat dong', 'con hang'].includes(normalized)) return true
  if (['false', '0', 'inactive', 'disabled', 'ngung hoat dong', 'het hang', 'tat'].includes(normalized)) return false

  return true
}

function isOutOfStock(v) {
  return Number(v?.soLuongTon ?? 0) <= 0
}

function isVariantActive(v) {
  return normalizeStatus(v?.trangThai ?? v?.trang_thai ?? v?.active ?? v?.status)
}

function isVariantAvailable(v) {
  return isVariantActive(v) && !isOutOfStock(v)
}

function variantStatusText(v) {
  return isVariantAvailable(v) ? 'Còn hàng' : 'Hết hàng'
}

function mapVariant(item) {
  const stock = Number(item?.soLuongTon ?? 0)
  const active = stock > 0
    ? normalizeStatus(item?.trangThai ?? item?.trang_thai ?? item?.active ?? item?.status)
    : false

  return {
    ...item,
    trangThai: active,
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
    if (filters.status === 'in') matchStatus = isVariantAvailable(v)
    if (filters.status === 'out') matchStatus = !isVariantAvailable(v)

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
  if (!variant?.id) return

  const next = !isVariantActive(variant)
  if (next && isOutOfStock(variant)) {
    error('Số lượng tồn = 0 nên không thể bật “Còn hàng”.')
    return
  }

  pendingVariant.value = variant
  pendingNext.value = next
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

  if (next && isOutOfStock(v)) {
    closeToggleModal()
    error('Số lượng tồn = 0 nên không thể bật “Còn hàng”.')
    toggleLoading.value = false
    return
  }

  try {
    await updateDetail(v.id, {
      idSanPham: v.idSanPham,
      idKichCo: v.idKichCo,
      idMauSac: v.idMauSac,
      soLuongTon: v.soLuongTon,
      donGia: v.donGia,
      ghiChu: v.ghiChu,
      trangThai: Number(v.soLuongTon ?? 0) > 0 ? next : false,
      anh: v.anh,
      mediaPrimaryId: v.mediaPrimaryId ?? null
    })

    emitTabSync(TAB_SYNC_EVENTS.PRODUCT_CHANGED, {
      productDetailId: v.id,
      soLuongTon: v.soLuongTon,
      donGia: v.donGia,
      trangThai: Number(v.soLuongTon ?? 0) > 0 ? next : false,
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

watch(() => editingVariant.soLuongTon, (stock) => {
  if (Number(stock || 0) <= 0) editingVariant.trangThai = false
})

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
      trangThai: Number(editingVariant.soLuongTon ?? 0) > 0 ? !!editingVariant.trangThai : false,
      anh: editingVariant.anh,
      mediaPrimaryId: editingVariant.mediaPrimaryId
          })

    emitTabSync(TAB_SYNC_EVENTS.PRODUCT_STOCK_CHANGED, {
      productDetailId: editingVariant.id,
      soLuongTon: editingVariant.soLuongTon,
      donGia: editingVariant.donGia,
      trangThai: Number(editingVariant.soLuongTon ?? 0) > 0 ? !!editingVariant.trangThai : false,
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

const selectedExportCount = computed(() => selectedExportIds.value.length)

function getVariantExportKey(variant) {
  return String(variant?.id ?? variant?.maSanPhamChiTiet ?? '')
}

function isVariantSelected(variant) {
  const key = getVariantExportKey(variant)
  return !!key && selectedExportIds.value.includes(key)
}

function toggleVariantExport(variant, checked) {
  const key = getVariantExportKey(variant)
  if (!key) return

  if (checked) {
    if (!selectedExportIds.value.includes(key)) {
      selectedExportIds.value = [...selectedExportIds.value, key]
    }
    return
  }

  selectedExportIds.value = selectedExportIds.value.filter((id) => id !== key)
}

const isAllCurrentPageSelected = computed(() => {
  if (!pagedItems.value.length) return false
  return pagedItems.value.every((item) => isVariantSelected(item))
})

function toggleCurrentPageExport(checked) {
  const currentIds = pagedItems.value
    .map((item) => getVariantExportKey(item))
    .filter(Boolean)

  if (checked) {
    selectedExportIds.value = Array.from(new Set([...selectedExportIds.value, ...currentIds]))
    return
  }

  selectedExportIds.value = selectedExportIds.value.filter((id) => !currentIds.includes(id))
}

function startExportSelect() {
  isExportSelecting.value = true
  selectedExportIds.value = []
}

function cancelExportSelect() {
  isExportSelecting.value = false
  selectedExportIds.value = []
}

function getSelectedExportItems() {
  const selectedSet = new Set(selectedExportIds.value)
  return allItems.value.filter((item) => selectedSet.has(getVariantExportKey(item)))
}

/** export */
function buildVariantExcelRows(items = []) {
  return items.map((v, i) => ({
    STT: i + 1,
    'Mã sản phẩm': v.maSanPham ?? '',
    'Tên sản phẩm': v.tenSanPham ?? '',
    'Mã SP chi tiết': v.maSanPhamChiTiet ?? '',
    'Màu sắc': v.tenMauSac ?? '',
    'Kích cỡ': v.tenKichCo ?? '',
    'Số lượng tồn': v.soLuongTon ?? 0,
    'Giá bán': v.donGia ?? 0,
    'Trạng thái': isVariantAvailable(v) ? 'Còn hàng' : 'Hết hàng',
    'Ghi chú': v.ghiChu ?? ''
  }))
}

function autoFitExcelColumns(ws, rows) {
  const headers = Object.keys(rows[0] || {})
  ws['!cols'] = headers.map((header) => {
    const maxLength = Math.max(
      String(header).length,
      ...rows.map((row) => String(row[header] ?? '').length)
    )
    return { wch: Math.min(Math.max(maxLength + 2, 10), 45) }
  })
}

function downloadExcel() {
  try {
    const selectedItems = getSelectedExportItems()
    if (!selectedItems.length) {
      error('Vui lòng chọn biến thể cần xuất Excel')
      return
    }

    const rows = buildVariantExcelRows(selectedItems)

    const ws = XLSX.utils.json_to_sheet(rows)
    autoFitExcelColumns(ws, rows)

    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, 'DanhSachBienThe')

    const arrayBuffer = XLSX.write(wb, { bookType: 'xlsx', type: 'array' })
    const blob = new Blob([arrayBuffer], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `danh-sach-bien-the-da-chon_${rows.length}_bien-the.xlsx`
    a.click()
    URL.revokeObjectURL(url)

    success(`Đã xuất ${rows.length} biến thể ra Excel`)
    cancelExportSelect()
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

.qr-modal-box {
  width: min(720px, calc(100% - 24px));
}

.qr-reader-box {
  width: 100%;
  min-height: 300px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  background: #000;
}

.qr-preview-modal {
  width: 440px;
}

.qr-preview-frame {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  background: #fff;
  padding: 14px;
}

.qr-preview-image {
  width: 280px;
  max-width: 100%;
  height: auto;
  display: block;
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
.color-dot,
.color-dot-lg {
  border: 1px solid #d1d5db;
}

.filter-multiselect {
  min-height: 38px;
}

.filter-multiselect :deep(.multiselect__tags) {
  min-height: 38px;
  border: 1px solid #d1d5db;
  border-radius: 0.375rem;
  padding-top: 7px;
}

.filter-multiselect :deep(.multiselect__placeholder),
.filter-multiselect :deep(.multiselect__single) {
  margin-bottom: 0;
  font-size: 0.95rem;
}


.select-col {
  width: 44px;
  min-width: 44px;
}
/* ===== Đồng bộ giao diện filter với màn detail ===== */
.variant-page .card {
  overflow: visible;
}

.variant-page .p-3,
.variant-page .row {
  overflow: visible;
}

.variant-page .form-select {
  font-weight: 400 !important;
  color: #374151 !important;
}

.variant-page .form-select option {
  font-weight: 400 !important;
}

.text-success,
.range-green {
  color: #059669 !important;
  accent-color: #059669 !important;
}

.range-green::-webkit-slider-thumb {
  border-color: #059669 !important;
}

.range-green::-moz-range-thumb {
  border-color: #059669 !important;
}

.filter-multiselect {
  width: 100% !important;
  min-height: 38px;
  min-width: 0 !important;
}

.filter-multiselect :deep(.multiselect),
.filter-multiselect :deep(.multiselect__tags) {
  width: 100% !important;
  min-width: 0 !important;
}

.filter-multiselect :deep(.multiselect__tags) {
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
  margin-bottom: 0;
  font-size: 0.95rem;
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

.filter-multiselect :deep(.multiselect__tags:focus-within),
.filter-multiselect.multiselect--active :deep(.multiselect__tags) {
  border-color: #2563eb !important;
  box-shadow: 0 0 0 0.2rem rgba(37, 99, 235, 0.15) !important;
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
  font-weight: 700 !important;
}

.filter-multiselect :deep(.multiselect__option--selected::after) {
  background: #dbeafe !important;
  color: #1e40af !important;
}

.filter-multiselect :deep(.multiselect__spinner::before),
.filter-multiselect :deep(.multiselect__spinner::after) {
  border-top-color: #2563eb !important;
}

.form-switch .form-check-input:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}
/* ===== FIX: canh đều filter giống màn danh sách sản phẩm ===== */
.variant-page .card {
  overflow: visible !important;
}

.variant-page .p-3 {
  padding: 16px !important;
  overflow: visible !important;
}

.variant-page .row {
  width: 100% !important;
  max-width: 100% !important;
  margin-left: 0 !important;
  margin-right: 0 !important;
  --bs-gutter-x: 18px;
  --bs-gutter-y: 18px;
  align-items: start !important;
  overflow: visible !important;
}

.variant-page .row > [class*="col-"] {
  min-width: 0 !important;
  padding-left: 6px !important;
  padding-right: 6px !important;
}

.variant-page .form-label {
  min-height: 21px !important;
  display: flex !important;
  align-items: center !important;
  margin-bottom: 6px !important;
  font-size: 0.875rem !important;
  font-weight: 500 !important;
  color: #374151 !important;
}

.variant-page .form-control,
.variant-page .form-select,
.variant-page .filter-multiselect :deep(.multiselect__tags) {
  height: 40px !important;
  min-height: 40px !important;
  border-radius: 6px !important;
}

.variant-page .form-control,
.variant-page .form-select {
  padding: 8px 12px !important;
  font-size: 0.875rem !important;
  font-weight: 400 !important;
  line-height: 1.35 !important;
  color: #374151 !important;
  background-color: #fff !important;
}

.variant-page .form-control::placeholder {
  color: #9ca3af !important;
  opacity: 1 !important;
  font-weight: 400 !important;
}

.variant-page .form-select.placeholder-select {
  color: #9ca3af !important;
  font-weight: 400 !important;
}

.variant-page .form-select,
.variant-page .form-select option {
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

.text-success,
.range-green {
  color: #059669 !important;
  accent-color: #059669 !important;
}

.text-success.fw-bold,
.fw-bold,
.fw-semibold {
  font-weight: 600 !important;
}

.range-green::-webkit-slider-thumb {
  border-color: #059669 !important;
}

.range-green::-moz-range-thumb {
  border-color: #059669 !important;
}

.variant-page .d-flex.gap-3.flex-wrap.align-items-center {
  min-height: 40px !important;
  align-items: center !important;
}

@media (min-width: 992px) {
  .variant-page .col-lg-5 {
    width: 41.666667% !important;
    flex: 0 0 auto !important;
  }

  .variant-page .col-lg-4 {
    width: 33.333333% !important;
    flex: 0 0 auto !important;
  }

  .variant-page .col-lg-3 {
    width: 25% !important;
    flex: 0 0 auto !important;
  }
}
</style>