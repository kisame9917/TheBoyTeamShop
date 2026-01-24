<template>
  <div class="product-detail-page">
    <div class="header-section">
      <!-- Breadcrumb or Title -->
      <div class="header-left">
        <h2>Danh sách biến thể sản phẩm</h2>
      </div>
      <div class="header-right">
        <button class="btn btn-secondary" @click="goBack">← Quay lại danh sách</button>
      </div>
    </div>

    <div v-if="loading" class="text-center">Đang tải dữ liệu...</div>

    <div v-else-if="product" class="content-wrapper">

      <!-- Filter Section -->
      <div class="card filter-card">
        <div class="filter-row">
          <div class="form-group search-group">
            <label>Tìm kiếm biến thể</label>
            <input type="text" v-model="filters.keyword" class="form-input" placeholder="Tìm mã, màu, kích cỡ...">
          </div>

          <div class="form-group select-group">
            <label>Màu sắc</label>
            <select v-model="filters.color" class="form-input">
              <option value="">Tất cả màu</option>
              <option v-for="c in attributes.mauSac" :key="c.id" :value="c.ten">{{ c.ten }}</option>
            </select>
          </div>

          <div class="form-group select-group">
            <label>Kích cỡ</label>
            <select v-model="filters.size" class="form-input">
              <option value="">Tất cả kích cỡ</option>
              <option v-for="s in attributes.kichCo" :key="s.id" :value="s.soSize">{{ s.soSize }}</option>
            </select>
          </div>

          <div class="action-group">
            <button class="btn btn-secondary" @click="resetFilters">Đặt lại</button>
            <button class="btn btn-warning">📷 Quét QR</button>
            <button class="btn btn-primary" @click="goToGlobalList">Hiển thị đầy đủ biến thể</button>
          </div>
        </div>
      </div>

      <!-- Variants Table -->
      <div class="card table-card">
        <div class="table-responsive">
          <table class="table">
            <thead>
              <tr>
                <th>STT</th>
                <th>Mã sản phẩm chi tiết</th>
                <th>Ảnh</th>
                <th>Tên sản phẩm</th>
                <th>Màu sắc</th>
                <th>Kích cỡ</th>
                <th>Số lượng tồn</th>
                <th>Giá bán</th>
                <th>Trạng thái</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(variant, index) in filteredVariants" :key="variant.id">
                <td>{{ index + 1 }}</td>
                <td>{{ variant.maSanPhamChiTiet }}</td>
                <td>
                  <img v-if="variant.anh" :src="'http://localhost:8080' + variant.anh"
                    style="width: 200px; height: 200px; object-fit: cover; border-radius: 4px; border: 1px solid #eee;" />
                  <span v-else class="no-img">No Img</span>
                </td>
                <td style="font-weight: 500;">{{ product.tenSanPham }}</td>
                <td>
                  <span class="color-dot" :style="{ backgroundColor: getColorCode(variant.tenMauSac) }"></span>
                  {{ variant.tenMauSac }}
                </td>
                <td>{{ variant.tenKichCo }}</td>
                <td class="text-center">{{ variant.soLuongTon }}</td>
                <td class="text-highlight">{{ formatPrice(variant.donGia) }}</td>
                <td class="status-cell">
                  <span :class="['badge', variant.trangThai ? 'badge-success' : 'badge-danger']">
                    {{ variant.trangThai ? 'Còn hàng' : 'Hết hàng' }}
                  </span>
                </td>
                <td>
                  <div class="action-buttons">
                    <button class="btn-icon blue" @click="editVariant(variant)">✏️</button>
                    <button class="btn-icon status-toggle" @click="toggleStatus(variant)"
                      title="Đổi trạng thái">🔄</button>
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

    <!-- Add Variant Modal (Hidden for now as per design focus on list, but kept for logic) -->
    <div v-if="showAddModal" class="modal-overlay">
      <div class="modals">
        <div class="modal-header">
          <h3>Thêm biến thể mới</h3>
          <button class="close-btn" @click="closeModal">×</button>
        </div>
        <div class="modal-body">
          <!-- Simplified for brevity, reusing logic -->
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

    <!-- Edit Modal -->
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
              <img :src="'http://localhost:8080' + editingVariant.anh" alt="Preview"
                style="max-width: 200px; max-height: 200px; object-fit: cover; border: 1px solid #ccc; border-radius: 4px;">
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

const filters = reactive({
  keyword: '',
  color: '',
  size: ''
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
  if (!colorName) return '#ccc';
  const map = {
    'Xanh dương': 'blue',
    'Đỏ': 'red',
    'Trắng': 'white',
    'Đen': 'black',
    'Vàng': 'yellow'
  };
  return map[colorName] || '#ccc'; // Default gray
}


const filteredVariants = computed(() => {
  return variants.value.filter(v => {
    const matchKeyword = !filters.keyword ||
      v.maSanPhamChiTiet.toLowerCase().includes(filters.keyword.toLowerCase()) ||
      v.tenMauSac.toLowerCase().includes(filters.keyword.toLowerCase()) ||
      v.tenKichCo.toLowerCase().includes(filters.keyword.toLowerCase());

    const matchColor = !filters.color || v.tenMauSac === filters.color;
    const matchSize = !filters.size || v.tenKichCo === filters.size; // Assuming simple string match

    return matchKeyword && matchColor && matchSize;
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

function resetFilters() {
  filters.keyword = '';
  filters.color = '';
  filters.size = '';
}

function goToGlobalList() {
  router.push('/variants');
}

// Edit Modal State
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
  const file = event.target.files[0];
  if (!file) return;
  try {
    const res = await uploadImage(file);
    editingVariant.anh = res.data.url;
    alert("Upload ảnh thành công!");
  } catch (e) {
    console.error(e);
    alert("Lỗi upload ảnh");
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

  // Find IDs based on names (reverse lookup) or if API provided IDs use them.
  // The current getAll response includes IDs (idKichCo, idMauSac).
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
    alert("Vui lòng chọn đủ thông tin");
    return;
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
    const statusText = updatedVariant.trangThai ? 'Hoạt động' : 'Ngừng hoạt động'
    success(`Đã đổi trạng thái biến thể thành ${statusText}`)
  } catch (e) {
    console.error(e)
    error("Lỗi cập nhật trạng thái")
  }
}

function formatPrice(val) {
  if (!val) return '0 đ';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
}
</script>

<style scoped>
.product-detail-page {
  padding: 20px;
  background-color: #f3f4f6;
  min-height: 100vh;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-section h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

.card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* Filter Styles */
.filter-row {
  display: flex;
  gap: 15px;
  align-items: flex-end;
  flex-wrap: wrap;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.search-group {
  flex: 2;
  min-width: 200px;
}

.select-group {
  flex: 1;
  min-width: 150px;
}

.action-group {
  display: flex;
  gap: 10px;
  padding-bottom: 2px;
  /* align with inputs */
}

.form-group label {
  font-size: 0.875rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 6px;
}

.form-input {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.875rem;
  width: 100%;
  box-sizing: border-box;
}

.btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  font-size: 0.875rem;
  transition: background 0.2s;
}

.btn-secondary {
  background: #e5e7eb;
  color: #374151;
}

.btn-secondary:hover {
  background: #d1d5db;
}

.btn-primary {
  background: #2563eb;
  color: white;
}

.btn-primary:hover {
  background: #1d4ed8;
}

.btn-warning {
  background: #f59e0b;
  color: white;
}

/* Table Styles */
.table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
}

.table th {
  background: #fef3c7;
  /* Light yellow/beige header like screenshot */
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

.img-placeholder {
  width: 50px;
  height: 50px;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  overflow: hidden;
}

.img-placeholder img {
  max-width: 100%;
  max-height: 100%;
}

.color-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 1px solid #e5e7eb;
  margin-right: 5px;
}

.text-center {
  text-align: center;
}

.text-highlight {
  color: #047857;
  font-weight: 600;
}

.status-cell {
  text-align: center;
  vertical-align: middle;
  white-space: nowrap;
  /* chống xuống dòng ở cell */
}

/* badge riêng, không phụ thuộc .badge */
.status-badge {
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;

  padding: 6px 12px;
  min-width: 90px;
  /* ✅ giúp “Còn hàng” không bị bó */
  border-radius: 6px;

  font-size: 12px;
  font-weight: 700;
  line-height: 1 !important;
  white-space: nowrap !important;
}

/* màu */
.status-badge.is-success {
  background: #d1fae5;
  color: #065f46;
}

.status-badge.is-danger {
  background: #fee2e2;
  color: #991b1b;
}

.badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
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
  display: flex;
  gap: 5px;
}

.btn-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  color: white;
}

.btn-icon.blue {
  background: #3b82f6;
}

.btn-icon.red {
  background: #ef4444;
}

.btn-icon.status-toggle {
  background: #f59e0b;
  /* Orange for state change */
}

.btn-icon.status-toggle:hover {
  background: #d97706;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
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

.modal-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
}

.form-row {
  display: flex;
  gap: 10px;
}

.half {
  flex: 1;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.no-img {
  font-size: 0.7rem;
  color: #999;
  background: #eee;
  padding: 2px 4px;
  border-radius: 4px;
}
</style>
