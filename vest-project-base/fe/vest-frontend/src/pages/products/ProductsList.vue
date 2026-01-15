<template>
  <div class="product-page">
    <div class="header-section">
      <h2>Quản lý sản phẩm / Danh sách sản phẩm</h2>
    </div>

    <!-- Filter Section -->
    <div class="card filter-card">
      <div class="card-header">
        <h3><span class="icon">▼</span> Bộ lọc tìm kiếm</h3>
      </div>

      <div class="filter-body">
        <div class="filter-left">
          <div class="form-group full-width">
            <label>Tìm kiếm</label>
            <input type="text" v-model="filters.keyword" placeholder="Tìm kiếm theo tên sản phẩm..." class="form-input"
              @keyup.enter="reload">
          </div>

          <div class="form-group status-group">
            <label>Trạng thái</label>
            <div class="radio-group">
              <label><input type="radio" value="" v-model="filters.status" @change="reload"> Tất cả</label>
              <label><input type="radio" value="true" v-model="filters.status" @change="reload"> Còn hàng</label>
              <label><input type="radio" value="false" v-model="filters.status" @change="reload"> Hết hàng</label>
            </div>
          </div>
        </div>

        <div class="filter-right">
          <div class="filter-grid">
            <div class="form-group">
              <label>Loại sản phẩm</label>
              <select v-model="filters.loai" class="form-input">
                <option value="">Loại sản phẩm</option>
                <!-- TODO: Fetch Type Options -->
              </select>
            </div>
            <div class="form-group">
              <label>Số lượng</label>
              <select v-model="filters.soLuong" class="form-input">
                <option value="">Số lượng</option>
              </select>
            </div>
            <div class="form-group">
              <label>Thương hiệu</label>
              <select v-model="filters.thuongHieu" class="form-input">
                <option value="">Thương hiệu</option>
                <!-- TODO: Fetch Brand Options -->
              </select>
            </div>
            <div class="form-group">
              <label>Khoảng giá</label>
              <select v-model="filters.khoangGia" class="form-input">
                <option value="">Khoảng giá</option>
              </select>
            </div>
          </div>
        </div>

        <div class="action-buttons-group">
          <button class="btn btn-outline">Tải Excel</button>
          <button class="btn btn-outline" @click="createProduct">Thêm mới</button>
          <button class="btn btn-outline">Quét QR</button>
          <button class="btn btn-outline" @click="resetFilters">Đặt lại</button>
        </div>
      </div>
    </div>

    <div style="height: 20px;"></div>

    <!-- Table Section -->
    <div class="card table-card">
      <div class="table-header-info">
        <h3>Tổng số sản phẩm {{ totalElements }}</h3>
      </div>

      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr>
              <th>STT</th>
              <th>Mã sản phẩm</th>
              <th>Tên sản phẩm</th>
              <th>Loại sản phẩm</th>
              <th>Thương hiệu</th>
              <th>Hàng tồn</th>
              <th>Khoảng giá</th>
              <th>Trạng thái</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(p, index) in items" :key="p.id">
              <td>{{ (currentPage * pageSize) + index + 1 }}</td>
              <td>{{ p.maSanPham }}</td>
              <td class="text-bold">{{ p.tenSanPham }}</td>
              <td>{{ p.tenLoaiSanPham || '-' }}</td>
              <td>{{ p.tenThuongHieu || '-' }}</td>
              <td class="text-center">{{ p.soLuongTon }}</td>
              <td>{{ formatPriceRange(p.giaMin, p.giaMax) }}</td>
              <td>
                <span :class="['badge', p.trangThai ? 'badge-success' : 'badge-danger']">
                  {{ p.trangThai ? 'Còn hàng' : 'Hết hàng' }}
                </span>
              </td>
              <td class="text-center">
                <button class="btn-icon" @click="goDetail(p.id)">
                  👁️
                </button>
              </td>
            </tr>
            <tr v-if="loading">
              <td colspan="9" class="text-center">Đang tải dữ liệu...</td>
            </tr>
            <tr v-if="!loading && items.length === 0">
              <td colspan="9" class="text-center">Không tìm thấy sản phẩm nào</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination Mockup -->
      <!-- Pagination -->
      <div class="pagination-section" v-if="totalPages > 0">
        <button class="page-btn" :disabled="currentPage === 0" @click="changePage(currentPage - 1)">
          &lt;
        </button>

        <!-- Simple Paging: List all pages (optimize later if too many) -->
        <button v-for="page in totalPages" :key="page" class="page-btn" :class="{ active: currentPage === page - 1 }"
          @click="changePage(page - 1)">
          {{ page }}
        </button>

        <button class="page-btn" :disabled="currentPage === totalPages - 1" @click="changePage(currentPage + 1)">
          &gt;
        </button>

        <span style="margin-left: 10px;">Trang {{ currentPage + 1 }}/{{ totalPages }}</span>
      </div>

      <p v-if="error" class="error-msg">{{ error }}</p>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { listSanPham } from '../../services/sanPhamApi'

const router = useRouter()
const items = ref([])
const loading = ref(false)
const error = ref('')

// Pagination state
const currentPage = ref(0)
const totalPages = ref(0)
const pageSize = ref(10)
const totalElements = ref(0)

const filters = reactive({
  keyword: '',
  status: '', // 'true' | 'false' | ''
  loai: '',
  thuongHieu: '',
  soLuong: '',
  khoangGia: ''
})

function goDetail(id) {
  router.push(`/products/${id}`)
}

function createProduct() {
  // TODO: Navigate to create page
  alert('Chức năng thêm mới chưa implement route');
}

function formatPrice(val) {
  if (!val) return '0 đ';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
}

function formatPriceRange(min, max) {
  if (min === max) {
    return formatPrice(min);
  }
  return `${formatPrice(min)} - ${formatPrice(max)}`;
}

function resetFilters() {
  filters.keyword = '';
  filters.status = '';
  filters.loai = '';
  filters.thuongHieu = '';
  reload();
}

function changePage(page) {
  if (page >= 0 && page < totalPages.value) {
    currentPage.value = page
    reload()
  }
}

async function reload() {
  loading.value = true
  error.value = ''
  try {
    // Call API with pagination params
    // Note: The BE currently supports pagination via Pageable, but filtering (keyword/status) 
    // might still be client-side if the BE search endpoint isn't fully implemented yet.
    // For now, we fetch the page. If we want client-side filter on specific page, it's tricky.
    // Ideally BE should support search params.

    // Assuming we fetch all for client-side filtering OR basic pagination without search for now:
    const res = await listSanPham(currentPage.value, pageSize.value)

    // Response structure from Spring Data Page: { content: [], totalPages: 5, totalElements: 50, ... }
    const serverItems = res.content || []
    totalPages.value = res.totalPages || 0
    totalElements.value = res.totalElements || 0

    // Client-side filtering applies to the CURRENT PAGE's data if the BE doesn't filter.
    // OR if we want proper search, we must pass params to BE. 
    // Since I haven't updated BE to take search params yet, this search only filters the *current page* returned by BE.
    // This is a limitation until BE search is implemented.
    items.value = serverItems.filter(item => {
      const matchesKeyword = !filters.keyword ||
        item.tenSanPham.toLowerCase().includes(filters.keyword.toLowerCase()) ||
        item.maSanPham.toLowerCase().includes(filters.keyword.toLowerCase());

      const matchesStatus = filters.status === '' || String(item.trangThai) === filters.status;

      return matchesKeyword && matchesStatus;
    });

  } catch (e) {
    console.error(e)
    error.value = 'Không gọi được API. Vui lòng kiểm tra Backend.'
  } finally {
    loading.value = false
  }
}

onMounted(reload)
</script>

<style scoped>
.product-page {
  padding: 20px;
  background-color: #f3f4f6;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.header-section h2 {
  font-size: 1.25rem;
  color: #374151;
  margin-bottom: 20px;
  font-weight: 600;
}

.card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 20px;
  border: 1px solid #e5e7eb;
}

.card-header h3 {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-body {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.filter-left {
  flex: 1;
  min-width: 300px;
}

.filter-right {
  flex: 1;
  min-width: 300px;
}

.action-buttons-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: flex-end;
}

.filter-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.form-group {
  margin-bottom: 12px;
}

.form-group label {
  display: block;
  font-size: 0.875rem;
  color: #374151;
  margin-bottom: 4px;
  font-weight: 500;
}

.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.875rem;
  outline: none;
  transition: border-color 0.2s;
}

.form-input:focus {
  border-color: #3b82f6;
}

.status-group .radio-group {
  display: flex;
  gap: 15px;
  margin-top: 8px;
}

.radio-group label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: normal;
  cursor: pointer;
}

.btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s;
}

.btn-outline {
  background: white;
  border-color: #d1d5db;
  color: #374151;
}

.btn-outline:hover {
  background: #f9fafb;
  border-color: #9ca3af;
}

.table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  margin-top: 10px;
}

.table th {
  background-color: #1e293b;
  /* Dark blue like screenshot */
  color: white;
  padding: 12px;
  text-align: left;
  font-size: 0.875rem;
  font-weight: 600;
}

.table td {
  padding: 12px;
  border-bottom: 1px solid #e5e7eb;
  color: #4b5563;
  font-size: 0.875rem;
}

.table tr:last-child td {
  border-bottom: none;
}

.text-bold {
  font-weight: 600;
  color: #1f2937;
}

.text-center {
  text-align: center;
}

.badge {
  padding: 4px 10px;
  border-radius: 9999px;
  /* Pill shape */
  font-size: 0.75rem;
  font-weight: 600;
}

.badge-success {
  background-color: #d1fae5;
  color: #047857;
}

.badge-danger {
  background-color: #fee2e2;
  color: #b91c1c;
}

.btn-icon {
  background: #1e3a8a;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 6px 10px;
  cursor: pointer;
}

.btn-icon:hover {
  background: #1e40af;
}

.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 5px;
  color: #6b7280;
  font-size: 0.875rem;
}

.page-btn {
  background: white;
  border: 1px solid #d1d5db;
  color: #374151;
  min-width: 32px;
  height: 32px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background-color: #f3f4f6;
  border-color: #9ca3af;
}

.page-btn.active {
  background-color: #1e3a8a;
  color: white;
  border-color: #1e3a8a;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.error-msg {
  color: #ef4444;
  margin-top: 10px;
  text-align: center;
}
</style>
