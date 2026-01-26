<template>
  <div class="product-detail-page">
    <!-- Header giống ảnh -->
    <div class="page-header">
      <div class="page-title">
        <h2>Quản lý sản phẩm / Danh sách biến thể</h2>
      </div>

      <div class="page-actions">
        <button class="btn btn-outline-secondary btn-sm" type="button">
          <i class="bi bi-qr-code me-1"></i> Quét QR
        </button>

        <button class="btn btn-outline-primary btn-sm" type="button" @click="downloadExcel">
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

    <div v-if="loading" class="text-center">Đang tải dữ liệu...</div>

    <div v-else-if="product" class="content-wrapper">

      <!-- FILTER giống ảnh + có thu gọn/mở rộng -->
      <div class="filter-panel">
        <div class="filter-toggle" @click="isFilterOpen = !isFilterOpen">
          <div class="filter-title">
            <i class="bi bi-funnel me-2"></i>
            <span>Bộ lọc tìm kiếm</span>
          </div>
          <div class="filter-hint">Nhấn để thu gọn/mở rộng</div>
        </div>

        <div v-show="isFilterOpen" class="filter-body">
          <div class="filter-grid">
            <!-- Row 1 -->
            <div class="filter-item filter-search">
              <label>Tìm kiếm</label>
              <input
                type="text"
                v-model="filters.keyword"
                class="form-input"
                placeholder="Tìm theo mã, màu, kích cỡ..."
              />
            </div>

            <div class="filter-item">
              <label>Màu sắc</label>
              <select v-model="filters.color" class="form-input">
                <option value="">-- Chọn Màu sắc --</option>
                <option v-for="c in attributes.mauSac" :key="c.id" :value="c.ten">
                  {{ c.ten }}
                </option>
              </select>
            </div>

            <div class="filter-item">
              <label>Số lượng tồn</label>
              <select v-model="filters.stock" class="form-input">
                <option value="">-- Chọn Số lượng tồn --</option>
                <option value="0">Hết hàng (0)</option>
                <option value="1-10">1 - 10</option>
                <option value="11-50">11 - 50</option>
                <option value="51-200">51 - 200</option>
                <option value="200+">Trên 200</option>
              </select>
            </div>

            <!-- Row 2 -->
            <div class="filter-item filter-price">
              <label>
                Khoảng giá:
                <span class="price-text">{{ formatMoney(filters.minPrice) }} - {{ formatMoney(filters.maxPrice) }}</span>
              </label>
              <!-- giống ảnh: 1 thanh slider (max) -->
              <input
                type="range"
                class="range-input"
                :min="0"
                :max="priceMaxLimit"
                :step="priceStep"
                v-model.number="filters.maxPrice"
              />
            </div>

            <div class="filter-item">
              <label>Kích cỡ</label>
              <select v-model="filters.size" class="form-input">
                <option value="">-- Chọn Kích cỡ --</option>
                <option v-for="s in attributes.kichCo" :key="s.id" :value="s.soSize">
                  {{ s.soSize }}
                </option>
              </select>
            </div>

            <div class="filter-item filter-status">
              <div class="status-row">
                <div class="status-left">
                  <label>Trạng thái</label>
                  <div class="radio-line">
                    <label class="radio-item">
                      <input type="radio" value="" v-model="filters.status" />
                      Tất cả
                    </label>
                    <label class="radio-item">
                      <input type="radio" value="in" v-model="filters.status" />
                      Còn hàng
                    </label>
                    <label class="radio-item">
                      <input type="radio" value="out" v-model="filters.status" />
                      Hết hàng
                    </label>
                  </div>
                </div>

                <button class="btn btn-reset" type="button" @click="resetFilters">
                  <i class="bi bi-arrow-counterclockwise me-1"></i> Reset
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Variants Table (GIỮ NGUYÊN FORM BẢNG) -->
      <div class="card table-card">
        <div class="table-responsive">
          <table class="table">
            <thead>
              <tr>
                <th class="text-center"> STT</th>
                <th class="text-center">Mã sản phẩm chi tiết</th>
                <th  class="text-center">Ảnh</th>
                <th class="text-center"> Tên sản phẩm</th>
                <th class="text-center"> Màu sắc</th>
                <th class="text-center">Kích cỡ</th>
                <th class="text-center">Số lượng tồn</th>
                <th class="text-center">Giá bán</th>
                <th class="text-center">Trạng thái</th>
                <th class="text-center">Hành động</th>
              </tr>
            </thead>

            <tbody>
              <tr v-for="(variant, index) in filteredVariants" :key="variant.id">
                <td class="text-center">>{{ index + 1 }}</td>
                <td class="text-center">>{{ variant.maSanPhamChiTiet }}</td>
                <td class="text-center">>
                  <img 
                    v-if="variant.anh"
                    :src="'http://localhost:8080' + variant.anh"
                    style="width: 200px; height: 200px; object-fit: cover; border-radius: 4px; border: 1px solid #eee;"
                  />
                  <span v-else class="no-img">No Img</span>
                </td>

                <td class="text-center" style="font-weight: 500;">{{ product.tenSanPham }}</td>

                <td class="text-center">
                  <span class="color-dot" :style="{ backgroundColor: getColorCode(variant.tenMauSac) }"></span>
                  {{ variant.tenMauSac }}
                </td>

                <td class="text-center">{{ variant.tenKichCo }}</td>
                <td class="text-center">{{ variant.soLuongTon }}</td>
                <td class="text-highlight">{{ formatPrice(variant.donGia) }}</td>

                <td class="status-cell">
                  <span :class="['badge', variant.trangThai ? 'badge-success' : 'badge-danger']">
                    {{ variant.trangThai ? 'Còn hàng' : 'Hết hàng' }}
                  </span>
                </td>

                <!-- ✅ CHỈ SỬA HÀNH ĐỘNG: nút sửa + switch giống ảnh + popup confirm -->
                <td>
                  <div class="action-buttons">
                    <button class="btn-edit" type="button" @click="editVariant(variant)" title="Sửa">
                      <i class="bi bi-pencil"></i>
                    </button>

                    <label class="switch" title="Đổi trạng thái">
                      <input
                        type="checkbox"
                        :checked="!!variant.trangThai"
                        @click.prevent="openStatusConfirm(variant)"
                      />
                      <span class="slider"></span>
                    </label>
                  </div>
                </td>
              </tr>

              <tr v-if="filteredVariants.length === 0">
                <td colspan="10" class="text-center">Không tìm thấy biến thể nào.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div v-else class="text-center error-text">
      {{ globalError || 'Không tìm thấy sản phẩm' }}
    </div>

    <!-- ✅ POPUP XÁC NHẬN ĐỔI TRẠNG THÁI -->
    <div v-if="showStatusModal" class="modal-overlay" @click.self="closeStatusModal">
      <div class="confirm-modal">
        <div class="modal-header">
          <h3>Xác nhận</h3>
          <button class="close-btn" type="button" @click="closeStatusModal">×</button>
        </div>

        <div class="modal-body">
          <p style="margin:0;">
            Bạn có chắc muốn đổi trạng thái biến thể
            <b>{{ statusTarget?.maSanPhamChiTiet }}</b>
            sang <b>{{ nextStatusText }}</b> không?
          </p>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" type="button" @click="closeStatusModal">Hủy</button>
          <button class="btn btn-primary" type="button" @click="confirmToggleStatus" :disabled="statusChanging">
            {{ statusChanging ? 'Đang cập nhật...' : 'Xác nhận' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Add Variant Modal (giữ nguyên) -->
    <div v-if="showAddModal" class="modal-overlay">
      <div class="modals">
        <div class="modal-header">
          <h3>Thêm biến thể mới</h3>
          <button class="close-btn" @click="closeModal">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Kích cỡ</label>
            <select v-model="newVariant.idKichCo" class="form-input">
              <option value="">-- Chọn Size --</option>
              <option v-for="s in attributes.kichCo" :key="s.id" :value="s.id">{{ s.soSize }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>Màu sắc</label>
            <select v-model="newVariant.idMauSac" class="form-input">
              <option value="">-- Chọn Màu --</option>
              <option v-for="c in attributes.mauSac" :key="c.id" :value="c.id">{{ c.ten }}</option>
            </select>
          </div>
          <div class="form-row">
            <div class="form-group half">
              <label>Số lượng</label>
              <input type="number" v-model="newVariant.soLuongTon" class="form-input" min="0">
            </div>
            <div class="form-group half">
              <label>Đơn giá</label>
              <input type="number" v-model="newVariant.donGia" class="form-input" min="0">
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeModal">Hủy</button>
          <button class="btn btn-primary" @click="submitVariant">Lưu</button>
        </div>
      </div>
    </div>

    <!-- Edit Modal (giữ nguyên) -->
    <div v-if="showEditModal" class="modal-overlay">
      <div class="modals">
        <div class="modal-header">
          <h3>Sửa biến thể: {{ editingVariant.maSanPhamChiTiet }}</h3>
          <button class="close-btn" @click="closeEditModal">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Kích cỡ</label>
            <select v-model="editingVariant.idKichCo" class="form-input">
              <option v-for="s in attributes.kichCo" :key="s.id" :value="s.id">{{ s.soSize }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>Màu sắc</label>
            <select v-model="editingVariant.idMauSac" class="form-input">
              <option v-for="c in attributes.mauSac" :key="c.id" :value="c.id">{{ c.ten }}</option>
            </select>
          </div>
          <div class="form-row">
            <div class="form-group half">
              <label>Số lượng</label>
              <input type="number" v-model="editingVariant.soLuongTon" class="form-input" min="0">
            </div>
            <div class="form-group half">
              <label>Đơn giá</label>
              <input type="number" v-model="editingVariant.donGia" class="form-input" min="0">
            </div>
          </div>
          <div class="form-group">
            <label>Trạng thái</label>
            <div class="radio-group" style="display:flex; gap:15px; margin-top:5px;">
              <label><input type="radio" :value="true" v-model="editingVariant.trangThai"> Còn hàng</label>
              <label><input type="radio" :value="false" v-model="editingVariant.trangThai"> Hết hàng</label>
            </div>
          </div>
          <div class="form-group">
            <label>Ảnh biến thể</label>
            <input type="file" @change="handleFileUpload" class="form-input" accept="image/*">
            <div v-if="editingVariant.anh" style="margin-top: 10px;">
              <img
                :src="'http://localhost:8080' + editingVariant.anh"
                alt="Preview"
                style="max-width: 200px; max-height: 200px; object-fit: cover; border: 1px solid #ccc; border-radius: 4px;"
              >
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
import { ref, onMounted, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import attributeService from '../../services/attributeService'
import { getByProductId, createDetail, updateDetail, uploadImage } from '../../services/sanPhamChiTietApi'
import { useToast } from '../../composables/useToast'

const { success, error } = useToast()

const props = defineProps(['id'])
const router = useRouter()
const product = ref(null)
const variants = ref([])
const loading = ref(false)
const showAddModal = ref(false)
const globalError = ref('')

const attributes = reactive({
  kichCo: [],
  mauSac: []
})

/** ✅ FILTER giống ảnh */
const isFilterOpen = ref(true)
const priceMaxLimit = 10000000
const priceStep = 50000

const filters = reactive({
  keyword: '',
  color: '',
  size: '',
  stock: '',
  status: '',
  minPrice: 0,
  maxPrice: priceMaxLimit
})

const newVariant = reactive({
  idSanPham: props.id,
  idKichCo: '',
  idMauSac: '',
  soLuongTon: 10,
  donGia: 0,
  ghiChu: '',
  trangThai: true
})

// Mock color mapping
function getColorCode(colorName) {
  if (!colorName) return '#ccc'
  const map = {
    'Xanh navy': '#1e3a8a',
    'Xanh dương': 'blue',
    'Đỏ': 'red',
    'Trắng': 'white',
    'Đen': 'black',
    'Vàng': 'yellow'
  }
  return map[colorName] || '#ccc'
}

const filteredVariants = computed(() => {
  return variants.value.filter(v => {
    const kw = (filters.keyword || '').trim().toLowerCase()

    const ma = (v.maSanPhamChiTiet || '').toLowerCase()
    const mau = (v.tenMauSac || '').toLowerCase()
    const sizeText = (v.tenKichCo || '').toLowerCase()

    const matchKeyword = !kw || ma.includes(kw) || mau.includes(kw) || sizeText.includes(kw)

    const matchColor = !filters.color || v.tenMauSac === filters.color
    const matchSize = !filters.size || String(v.tenKichCo) === String(filters.size)

    // stock filter
    const sl = Number(v.soLuongTon ?? 0)
    const matchStock =
      !filters.stock ||
      (filters.stock === '0' && sl === 0) ||
      (filters.stock === '1-10' && sl >= 1 && sl <= 10) ||
      (filters.stock === '11-50' && sl >= 11 && sl <= 50) ||
      (filters.stock === '51-200' && sl >= 51 && sl <= 200) ||
      (filters.stock === '200+' && sl > 200)

    // status radio
    const matchStatus =
      !filters.status ||
      (filters.status === 'in' && !!v.trangThai) ||
      (filters.status === 'out' && !v.trangThai)

    // price range
    const price = Number(v.donGia ?? 0)
    const matchPrice = price >= Number(filters.minPrice) && price <= Number(filters.maxPrice)

    return matchKeyword && matchColor && matchSize && matchStock && matchStatus && matchPrice
  })
})

onMounted(getData)

async function getData() {
  loading.value = true
  try {
    const resProd = await attributeService.getById('san-pham', props.id)
    product.value = resProd.data

    await loadVariants()

    const resSize = await attributeService.getAllList('kich-co')
    attributes.kichCo = resSize.data
    const resColor = await attributeService.getAllList('mau-sac')
    attributes.mauSac = resColor.data
  } catch (e) {
    console.error("Load failed", e)
    globalError.value = 'Lỗi tải dữ liệu'
  } finally {
    loading.value = false
  }
}

async function loadVariants() {
  const res = await getByProductId(props.id)
  variants.value = res.data
}

function goBack() {
  router.push('/products')
}

function goToGlobalList() {
  router.push('/variants')
}

function resetFilters() {
  filters.keyword = ''
  filters.color = ''
  filters.size = ''
  filters.stock = ''
  filters.status = ''
  filters.minPrice = 0
  filters.maxPrice = priceMaxLimit
}

function downloadExcel() {
  // TODO: gắn api export nếu có
  success('Chức năng Tải Excel chưa được gắn API.')
}

/** Edit Modal State (giữ nguyên) */
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

async function handleFileUpload(event) {
  const file = event.target.files[0]
  if (!file) return
  try {
    const res = await uploadImage(file)
    editingVariant.anh = res.data.url
    alert("Upload ảnh thành công!")
  } catch (e) {
    console.error(e)
    alert("Lỗi upload ảnh")
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
    alert("Cập nhật thành công")
    showEditModal.value = false
    await loadVariants()
  } catch (e) {
    console.error(e)
    alert("Cập nhật thất bại")
  }
}

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

function editVariant(variant) {
  openEditModal(variant)
}

function closeModal() {
  showAddModal.value = false
  newVariant.idKichCo = ''
  newVariant.idMauSac = ''
}

async function submitVariant() {
  if (!newVariant.idKichCo || !newVariant.idMauSac) {
    alert("Vui lòng chọn đủ thông tin")
    return
  }
  try {
    await createDetail({ ...newVariant, idSanPham: props.id })
    alert("Thêm thành công")
    closeModal()
    await loadVariants()
  } catch (e) {
    alert("Lỗi thêm biến thể")
  }
}

/** ✅ POPUP confirm đổi trạng thái */
const showStatusModal = ref(false)
const statusTarget = ref(null)
const statusChanging = ref(false)

const nextStatusText = computed(() => {
  if (!statusTarget.value) return ''
  return statusTarget.value.trangThai ? 'Hết hàng' : 'Còn hàng'
})

function openStatusConfirm(variant) {
  statusTarget.value = variant
  showStatusModal.value = true
}

function closeStatusModal() {
  showStatusModal.value = false
  statusTarget.value = null
}

async function confirmToggleStatus() {
  if (!statusTarget.value) return
  if (statusChanging.value) return

  statusChanging.value = true
  try {
    await toggleStatus(statusTarget.value)
    closeStatusModal()
  } finally {
    statusChanging.value = false
  }
}

async function toggleStatus(variant) {
  try {
    const updatedVariant = { ...variant, trangThai: !variant.trangThai }
    await updateDetail(variant.id, {
      ...updatedVariant,
      idSanPham: updatedVariant.idSanPham,
      idKichCo: updatedVariant.idKichCo,
      idMauSac: updatedVariant.idMauSac,
      soLuongTon: updatedVariant.soLuongTon,
      donGia: updatedVariant.donGia,
      ghiChu: updatedVariant.ghiChu,
      trangThai: updatedVariant.trangThai,
      anh: updatedVariant.anh
    })

    await loadVariants()
    const statusText = updatedVariant.trangThai ? 'Còn hàng' : 'Hết hàng'
    success(`Đã đổi trạng thái biến thể thành ${statusText}`)
  } catch (e) {
    console.error(e)
    error("Lỗi cập nhật trạng thái")
  }
}

function formatPrice(val) {
  if (val === null || val === undefined) return '0 đ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

function formatMoney(val) {
  const n = Number(val ?? 0)
  return new Intl.NumberFormat('vi-VN').format(n) + ' đ'
}
</script>

<style scoped>
.product-detail-page {
  padding: 20px;
  background-color: #f3f4f6;
  min-height: 100vh;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* Header giống ảnh */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.page-title h2 {
  font-size: 1.1rem;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

.page-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

/* mini btn giống ảnh (fallback nếu không có bootstrap) */
.btn-sm {
  padding: 6px 10px;
  font-size: 12px;
  border-radius: 6px;
  font-weight: 600;
}

.btn-outline-secondary {
  background: #fff;
  border: 1px solid #d1d5db;
  color: #374151;
}

.btn-outline-secondary:hover {
  background: #f9fafb;
}

.btn-outline-primary {
  background: #fff;
  border: 1px solid #2563eb;
  color: #2563eb;
}

.btn-outline-primary:hover {
  background: #eff6ff;
}

/* card chung (giữ nguyên của bạn) */
.card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* FILTER giống ảnh */
.filter-panel {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  margin-bottom: 16px;
  background: #fff;
}

.filter-toggle {
  background: #0f172a;
  color: #fff;
  padding: 10px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  user-select: none;
}

.filter-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 700;
  font-size: 13px;
}

.filter-hint {
  font-size: 12px;
  opacity: 0.85;
}

.filter-body {
  padding: 12px 14px;
}

.filter-grid {
  display: grid;
  grid-template-columns: 2.2fr 1fr 1fr;
  gap: 12px 14px;
  align-items: end;
}

.filter-item label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 6px;
  display: block;
}

.filter-search { grid-column: 1 / 2; }
.filter-price  { grid-column: 1 / 2; }

.form-input {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.875rem;
  width: 100%;
  box-sizing: border-box;
}

.range-input {
  width: 100%;
  accent-color: #16a34a; /* xanh giống ảnh */
}

.price-text {
  color: #16a34a;
  font-weight: 700;
}

/* status + reset giống ảnh */
.filter-status .status-row {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
}

.radio-line {
  display: flex;
  gap: 14px;
  margin-top: 6px;
  flex-wrap: wrap;
}

.radio-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #374151;
  white-space: nowrap;
}

.btn-reset {
  background: transparent;
  border: none;
  color: #6b7280;
  font-weight: 600;
  padding: 6px 8px;
  border-radius: 6px;
  cursor: pointer;
}

.btn-reset:hover {
  background: #f3f4f6;
}

/* Table styles (giữ nguyên của bạn) */
.table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
}

.table th {
  background: #fef3c7;
  color: #1f2937;
  padding: 12px;
  text-align: left;
  font-weight: 600;
  border-bottom: 1px solid #e5e7eb;
}

.table td {
  padding: 12px;
  border-bottom: 1px solid #f3f4f6;
  vertical-align: middle;
  color: #4b5563;
}

.color-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 1px solid #e5e7eb;
  margin-right: 5px;
}

.text-center { text-align: center; }
.text-highlight { color: #047857; font-weight: 600;text-align: center; }

.status-cell {
  text-align: center;
  vertical-align: middle;
  white-space: nowrap;
}

.badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
}

.badge-success { background: #d1fae5; color: #065f46; }
.badge-danger  { background: #fee2e2; color: #991b1b; }

/* ✅ Hành động giống ảnh */
.action-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-edit {
  width: 28px;
  height: 28px;
  border-radius: 4px;
  border: 1px solid #f59e0b;
  background: #fff;
  color: #f59e0b;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.btn-edit:hover {
  background: #fff7ed;
}

/* Switch (giữ của bạn) */
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

/* Modal chung */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modals {
  background: white;
  padding: 20px;
  border-radius: 8px;
  width: 400px;
}

/* ✅ confirm modal */
.confirm-modal {
  background: #fff;
  padding: 18px;
  border-radius: 10px;
  width: 420px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
}

.form-row { display: flex; gap: 10px; }
.half { flex: 1; }

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}

.no-img {
  font-size: 0.7rem;
  color: #999;
  background: #eee;
  padding: 2px 4px;
  border-radius: 4px;
}
</style>
