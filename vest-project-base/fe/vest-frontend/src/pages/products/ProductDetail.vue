<template>
  <div class="product-detail-page">
    <!-- Header -->
    <div class="header-section">
      <div class="header-left">
        <h2>Danh sách biến thể sản phẩm</h2>
      </div>
      <div class="header-right">
        <button class="btn btn-secondary" @click="goBack">← Quay lại danh sách</button>
      </div>
    </div>

    <div v-if="loading" class="text-center py-4">Đang tải dữ liệu...</div>

    <div v-else-if="product" class="content-wrapper">
      <!-- Filter -->
      <div class="card filter-card">
        <div class="filter-row">
          <div class="form-group search-group">
            <label>Tìm kiếm biến thể</label>
            <input
              type="text"
              v-model.trim="filters.keyword"
              class="form-input"
              placeholder="Tìm mã, màu, kích cỡ..."
              @keyup.enter="applyFilters"
            />
          </div>

          <div class="form-group select-group">
            <label>Màu sắc</label>
            <select v-model="filters.color" class="form-input" @change="applyFilters">
              <option value="">Tất cả màu</option>
              <option v-for="c in attributes.mauSac" :key="c.id" :value="c.ten">
                {{ c.ten }}
              </option>
            </select>
          </div>

          <div class="form-group select-group">
            <label>Kích cỡ</label>
            <select v-model="filters.size" class="form-input" @change="applyFilters">
              <option value="">Tất cả kích cỡ</option>
              <option v-for="s in attributes.kichCo" :key="s.id" :value="s.soSize">
                {{ s.soSize }}
              </option>
            </select>
          </div>

          <div class="action-group">
            <button class="btn btn-secondary" @click="resetFilters">Đặt lại</button>
            <button class="btn btn-warning" type="button">📷 Quét QR</button>
            <button class="btn btn-primary" type="button" @click="goToGlobalList">
              Hiển thị đầy đủ biến thể
            </button>
          </div>
        </div>
      </div>

      <!-- Table -->
      <div class="card table-card">
        <div class="table-responsive">
          <table class="table">
            <thead>
              <tr>
                <th class="col-stt">STT</th>
                <th class="col-ma">Mã sản phẩm chi tiết</th>
                <th class="col-anh">Ảnh</th>
                <th class="col-ten">Tên sản phẩm</th>
                <th class="col-mau">Màu sắc</th>
                <th class="col-size">Kích cỡ</th>
                <th class="col-sl text-center">Số lượng tồn</th>
                <th class="col-gia text-center">Giá bán</th>
                <th class="col-tt text-center">Trạng thái</th>
                <th class="col-action text-center">Hành động</th>
              </tr>
            </thead>

            <tbody>
              <tr v-for="(variant, index) in pagedVariants" :key="variant.id">
                <td class="text-center">
                  {{ currentPage * pageSize + index + 1 }}
                </td>

                <td class="text-center">{{ variant.maSanPhamChiTiet }}</td>

                <td class="text-center">
                  <img
                    v-if="variant.anh"
                    :src="'http://localhost:8080' + variant.anh"
                    class="variant-img"
                    alt="Ảnh biến thể"
                  />
                  <span v-else class="no-img">No Img</span>
                </td>

                <td class="text-bold">{{ product.tenSanPham }}</td>

                <td class="text-center">
                  <span class="color-dot" :style="{ backgroundColor: getColorCode(variant.tenMauSac) }"></span>
                  {{ variant.tenMauSac }}
                </td>

                <td class="text-center">{{ variant.tenKichCo }}</td>

                <td class="text-center">{{ variant.soLuongTon }}</td>

                <td class="text-center text-highlight">{{ formatPrice(variant.donGia) }}</td>

                <td class="text-center">
                  <span class="badge-pill" :class="variant.trangThai ? 'badge-success' : 'badge-danger'">
                    {{ variant.trangThai ? 'Còn hàng' : 'Hết hàng' }}
                  </span>
                </td>

                <td class="text-center action-cell">
                  <div class="action-buttons">
                    <button class="btn-icon blue" type="button" @click="openEditModal(variant)" title="Sửa">
                      ✏️
                    </button>

                    <label class="switch" title="Đổi trạng thái">
                      <input
                        type="checkbox"
                        :checked="!!variant.trangThai"
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

        <!-- Pagination (giống trang sản phẩm) -->
        <div class="paging-bar" v-if="totalPages > 0">
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
              <span aria-hidden="true">‹</span>
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
              class="btn btn-outline-secondary btn-sm"
              :disabled="currentPage >= totalPages - 1"
              @click="setPage(currentPage + 1)"
              type="button"
            >
              <span aria-hidden="true">›</span>
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
      {{ globalError || "Không tìm thấy sản phẩm" }}
    </div>

    <!-- Edit Modal (giữ logic của bạn) -->
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
              <option v-for="s in attributes.kichCo" :key="s.id" :value="s.id">
                {{ s.soSize }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label>Màu sắc</label>
            <select v-model="editingVariant.idMauSac" class="form-input">
              <option v-for="c in attributes.mauSac" :key="c.id" :value="c.id">
                {{ c.ten }}
              </option>
            </select>
          </div>

          <div class="form-row">
            <div class="form-group half">
              <label>Số lượng</label>
              <input type="number" v-model.number="editingVariant.soLuongTon" class="form-input" min="0" />
            </div>
            <div class="form-group half">
              <label>Đơn giá</label>
              <input type="number" v-model.number="editingVariant.donGia" class="form-input" min="0" />
            </div>
          </div>

          <div class="form-group">
            <label>Trạng thái</label>
            <div class="radio-group">
              <label><input type="radio" :value="true" v-model="editingVariant.trangThai" /> Còn hàng</label>
              <label><input type="radio" :value="false" v-model="editingVariant.trangThai" /> Hết hàng</label>
            </div>
          </div>

          <div class="form-group">
            <label>Ảnh biến thể</label>
            <input type="file" @change="handleFileUpload" class="form-input" accept="image/*" />
            <div v-if="editingVariant.anh" class="mt-2">
              <img :src="'http://localhost:8080' + editingVariant.anh" class="variant-img preview" alt="Preview" />
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
import { ref, onMounted, reactive, computed } from "vue";
import { useRouter } from "vue-router";
import attributeService from "../../services/attributeService";
import { getByProductId, updateDetail, uploadImage } from "../../services/sanPhamChiTietApi";
import { useToast } from "../../composables/useToast";

const { success, error } = useToast();

const props = defineProps(["id"]);
const router = useRouter();

const product = ref(null);
const variants = ref([]);
const loading = ref(false);
const globalError = ref("");

const attributes = reactive({
  kichCo: [],
  mauSac: [],
});

const filters = reactive({
  keyword: "",
  color: "",
  size: "",
});

/* ===== Pagination giống sản phẩm ===== */
const currentPage = ref(0);
const pageSize = ref(10);
const totalPages = ref(0);
const totalElements = ref(0);
const pageInput = ref(1);

const filteredVariants = computed(() => {
  const kw = (filters.keyword || "").toLowerCase().trim();

  return (variants.value || []).filter((v) => {
    const matchKeyword =
      !kw ||
      (v.maSanPhamChiTiet || "").toLowerCase().includes(kw) ||
      (v.tenMauSac || "").toLowerCase().includes(kw) ||
      (v.tenKichCo || "").toLowerCase().includes(kw);

    const matchColor = !filters.color || v.tenMauSac === filters.color;
    const matchSize = !filters.size || String(v.tenKichCo) === String(filters.size);

    return matchKeyword && matchColor && matchSize;
  });
});

const pagedVariants = computed(() => {
  const start = currentPage.value * pageSize.value;
  return filteredVariants.value.slice(start, start + pageSize.value);
});

function recomputePaging() {
  totalElements.value = filteredVariants.value.length;
  totalPages.value = Math.max(1, Math.ceil(totalElements.value / pageSize.value));

  // giữ currentPage không vượt
  if (currentPage.value > totalPages.value - 1) currentPage.value = totalPages.value - 1;

  pageInput.value = currentPage.value + 1;
}

function applyFilters() {
  currentPage.value = 0;
  recomputePaging();
}

function onChangeSize() {
  currentPage.value = 0;
  recomputePaging();
}

function setPage(p) {
  if (p < 0) return;
  if (p > totalPages.value - 1) return;
  currentPage.value = p;
  pageInput.value = currentPage.value + 1;
}

function jumpPage() {
  const max = Math.max(1, totalPages.value || 1);
  const target = Math.min(Math.max(1, pageInput.value || 1), max);
  setPage(target - 1);
}

function resetFilters() {
  filters.keyword = "";
  filters.color = "";
  filters.size = "";
  applyFilters();
}

function goBack() {
  router.push("/products");
}

function goToGlobalList() {
  router.push("/variants");
}

/* ===== load data ===== */
onMounted(getData);

async function getData() {
  loading.value = true;
  try {
    const resProd = await attributeService.getById("san-pham", props.id);
    product.value = resProd.data;

    await loadVariants();

    const [resSize, resColor] = await Promise.all([
      attributeService.getAllList("kich-co"),
      attributeService.getAllList("mau-sac"),
    ]);

    attributes.kichCo = resSize.data || [];
    attributes.mauSac = resColor.data || [];

    recomputePaging();
  } catch (e) {
    console.error(e);
    globalError.value = "Lỗi tải dữ liệu";
  } finally {
    loading.value = false;
  }
}

async function loadVariants() {
  const res = await getByProductId(props.id);
  variants.value = res.data || [];
}

/* ===== edit modal + upload ===== */
const showEditModal = ref(false);
const editingVariant = reactive({
  id: null,
  idSanPham: null,
  idKichCo: "",
  idMauSac: "",
  soLuongTon: 0,
  donGia: 0,
  ghiChu: "",
  trangThai: true,
  anh: "",
  maSanPhamChiTiet: "",
});

function openEditModal(v) {
  editingVariant.id = v.id;
  editingVariant.idSanPham = v.idSanPham;
  editingVariant.maSanPhamChiTiet = v.maSanPhamChiTiet;

  editingVariant.idKichCo = v.idKichCo;
  editingVariant.idMauSac = v.idMauSac;
  editingVariant.soLuongTon = v.soLuongTon;
  editingVariant.donGia = v.donGia;
  editingVariant.trangThai = v.trangThai;
  editingVariant.anh = v.anh;

  showEditModal.value = true;
}

function closeEditModal() {
  showEditModal.value = false;
}

async function handleFileUpload(event) {
  const file = event.target.files?.[0];
  if (!file) return;

  try {
    const res = await uploadImage(file);
    editingVariant.anh = res.data.url;
    success("Upload ảnh thành công!");
  } catch (e) {
    console.error(e);
    error("Lỗi upload ảnh");
  }
}

async function submitEdit() {
  if (!editingVariant.id) return;

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
    });

    success("Cập nhật thành công");
    showEditModal.value = false;
    await loadVariants();
    recomputePaging();
  } catch (e) {
    console.error(e);
    error("Cập nhật thất bại");
  }
}

async function toggleStatus(variant) {
  try {
    const next = !variant.trangThai;

    await updateDetail(variant.id, {
      idSanPham: variant.idSanPham,
      idKichCo: variant.idKichCo,
      idMauSac: variant.idMauSac,
      soLuongTon: variant.soLuongTon,
      donGia: variant.donGia,
      ghiChu: variant.ghiChu,
      trangThai: next,
      anh: variant.anh,
    });

    await loadVariants();
    recomputePaging();
    success(`Đã đổi trạng thái biến thể thành ${next ? "Còn hàng" : "Hết hàng"}`);
  } catch (e) {
    console.error(e);
    error("Lỗi cập nhật trạng thái");
  }
}

/* ===== helpers ===== */
function formatPrice(val) {
  const n = Number(val ?? 0);
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(n);
}

function getColorCode(colorName) {
  if (!colorName) return "#ccc";
  const map = {
    "Xanh dương": "blue",
    "Đỏ": "red",
    "Trắng": "white",
    "Đen": "black",
    "Vàng": "yellow",
    "Xanh navy": "#0f172a",
  };
  return map[colorName] || "#ccc";
}
</script>

<style scoped>
/* ===== page ===== */
.product-detail-page {
  padding: 20px;
  background-color: #f3f4f6;
  min-height: 100vh;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-section h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

.card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

/* ===== filter ===== */
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
  min-width: 220px;
}

.select-group {
  flex: 1;
  min-width: 160px;
}

.action-group {
  display: flex;
  gap: 10px;
  padding-bottom: 2px;
  flex-wrap: wrap;
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

/* ===== button (không phá bootstrap outline) ===== */
.btn-secondary,
.btn-primary,
.btn-warning {
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
  color: #fff;
}
.btn-primary:hover {
  background: #1d4ed8;
}

.btn-warning {
  background: #f59e0b;
  color: #fff;
}

/* ===== table ===== */
.table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
}

.table thead th {
  background: #1e293b;   /* ✅ xanh đen */
  color: #fff;
  padding: 12px;
  text-align: center;
  font-weight: 600;
  border-bottom: 1px solid #e5e7eb;
  white-space: nowrap;
}

.table td {
  padding: 12px;
  border-bottom: 1px solid #e5e7eb;
  vertical-align: middle;
  color: #4b5563;
}

.text-center {
  text-align: center !important;
}

.text-bold {
  font-weight: 600;
  color: #111827;
  text-align: center;
}

.text-highlight {
  color: #047857;
  font-weight: 600;
}

/* ảnh */
.variant-img {
  width: 200px;
  height: 200px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #eee;
}
.variant-img.preview {
  width: 200px;
  height: 200px;
}

.no-img {
  font-size: 0.75rem;
  color: #999;
  background: #eee;
  padding: 2px 6px;
  border-radius: 4px;
}

/* màu dot */
.color-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 1px solid #e5e7eb;
  margin-right: 6px;
  transform: translateY(1px);
}

/* badge pill giống sản phẩm */
.badge-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  border-radius: 9999px;
  font-size: 0.75rem;
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

/* ===== action ===== */
.action-cell {
  white-space: nowrap;
}
.action-buttons {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.btn-icon {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  color: #fff;
}
.btn-icon.blue {
  background: #3b82f6;
}

/* switch */
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
  height: 18px;
  width: 18px;
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

/* ===== pagination giống sản phẩm ===== */
.paging-bar {
  margin-top: 18px;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 12px;
}

.paging-left {
  justify-self: start;
  color: #6b7280;
  font-size: 0.875rem;
}

.paging-center {
  justify-self: center;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  flex-wrap: nowrap;
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

/* ✅ đảm bảo mũi tên có border (bootstrap outline) */
.paging-center .btn.btn-outline-secondary {
  border: 1px solid #d1d5db !important;
  background: #fff !important;
  color: #374151 !important;
}
.paging-center .btn.btn-outline-secondary:hover:not(:disabled) {
  background: #f3f4f6 !important;
  border-color: #9ca3af !important;
}
.paging-center .btn.btn-outline-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ===== modal ===== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
.modals {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  width: 500px;
  max-width: 90%;
  max-height: 90vh;
  overflow: auto;
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
.form-row {
  display: flex;
  gap: 10px;
}
.half {
  flex: 1;
}
.radio-group {
  display: flex;
  gap: 16px;
  margin-top: 6px;
}
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}
</style>
