<template>
  <div class="search-page container py-4">
    <nav aria-label="breadcrumb" class="mb-4">
      <ol class="breadcrumb fs-6">
        <li class="breadcrumb-item"><router-link to="/" class="text-muted text-decoration-none">Trang chủ</router-link></li>
        <li class="breadcrumb-item active text-dark" aria-current="page">Kết quả tìm kiếm cho "vest nam"</li>
      </ol>
    </nav>

    <div class="row">
      <aside class="col-lg-3 pe-lg-4 mb-4 mb-lg-0">
        <div class="filter-section mb-4">
          <h6 class="fw-bold mb-3 d-flex justify-content-between align-items-center">
            KHOẢNG GIÁ <i class="bi bi-chevron-up"></i>
          </h6>
          <div class="form-check mb-2" v-for="(range, index) in priceRanges" :key="index">
            <input class="form-check-input custom-checkbox" type="checkbox" :id="'price' + index" v-model="range.selected">
            <label class="form-check-label text-secondary" :for="'price' + index">
              {{ range.label }}
            </label>
          </div>
        </div>

        <div class="filter-section mb-4 border-top pt-4">
          <h6 class="fw-bold mb-3 d-flex justify-content-between align-items-center">
            MÀU SẮC <i class="bi bi-chevron-up"></i>
          </h6>
          <div class="form-check mb-2 d-flex align-items-center" v-for="(color, index) in colors" :key="index">
            <input class="form-check-input custom-checkbox me-2 mt-0" type="checkbox" :id="'color' + index" v-model="color.selected">
            <label class="form-check-label text-secondary d-flex align-items-center" :for="'color' + index">
              <span class="color-box me-2" :style="{ backgroundColor: color.hex }"></span>
              {{ color.name }}
            </label>
          </div>
        </div>

        <div class="filter-section border-top pt-4">
          <h6 class="fw-bold mb-3 d-flex justify-content-between align-items-center">
            KÍCH THƯỚC <i class="bi bi-chevron-up"></i>
          </h6>
          <div class="form-check mb-2" v-for="(size, index) in sizes" :key="index">
            <input class="form-check-input custom-checkbox" type="checkbox" :id="'size' + index" v-model="size.selected">
            <label class="form-check-label text-secondary" :for="'size' + index">
              {{ size.name }}
            </label>
          </div>
        </div>
      </aside>

      <main class="col-lg-9">
        <div class="d-flex justify-content-end mb-4">
          <select class="form-select w-auto text-dark bg-light border-0 shadow-sm custom-select">
            <option selected>Sắp xếp theo</option>
            <option value="1">Giá tăng dần</option>
            <option value="2">Giá giảm dần</option>
            <option value="3">Mới nhất</option>
          </select>
        </div>

        <div class="row row-cols-1 row-cols-sm-2 row-cols-md-3 g-4">
          <div class="col" v-for="product in products" :key="product.id">
            <div class="card h-100 border-0 rounded-3 shadow-sm product-card">
              <div class="position-relative">
                <span class="badge bg-secondary bg-opacity-75 position-absolute top-0 start-0 m-2 rounded-1 px-2 py-1">SALE</span>
                <img :src="product.image" class="card-img-top rounded-top-3" alt="Vest Nam" />
              </div>
              <div class="card-body d-flex flex-column p-3">
                <h6 class="card-title text-dark mb-2" style="font-size: 0.95rem;">{{ product.name }}</h6>
                <div class="mt-auto mb-3">
                  <span class="fw-bold text-dark me-2">{{ formatPrice(product.price) }}đ</span>
                  <small class="text-decoration-line-through text-muted">{{ formatPrice(product.oldPrice) }}đ</small>
                </div>
                <button class="btn btn-cyan text-white fw-semibold w-100 rounded-2 py-2">
                  THÊM VÀO GIỎ
                </button>
              </div>
            </div>
          </div>
        </div>

        <nav class="mt-5 d-flex justify-content-center">
          <ul class="pagination gap-2">
            <li class="page-item active"><a class="page-link rounded bg-dark border-dark text-white" href="#">1</a></li>
            <li class="page-item"><a class="page-link rounded text-dark border-0 bg-light" href="#">2</a></li>
            <li class="page-item"><a class="page-link rounded text-dark border-0 bg-light" href="#">3</a></li>
            <li class="page-item disabled"><a class="page-link border-0 bg-transparent text-dark">...</a></li>
            <li class="page-item"><a class="page-link rounded text-dark border-0 bg-light" href="#"><i class="bi bi-chevron-right"></i></a></li>
          </ul>
        </nav>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

// Mock Data cho Filters
const priceRanges = ref([
  { label: '0 - 10,000', selected: true },
  { label: '2,000 - 20,000', selected: false },
  { label: '3,000 - 30,000', selected: false },
  { label: '6,000 - 10,000', selected: false },
  { label: '> 500+', selected: false },
]);

const colors = ref([
  { name: 'Black', hex: '#000000', selected: false },
  { name: 'Navy', hex: '#1B263B', selected: false },
  { name: 'Grey', hex: '#8D99AE', selected: false },
  { name: 'Brown', hex: '#8B5A2B', selected: false },
]);

const sizes = ref([
  { name: 'XS', selected: true },
  { name: 'M', selected: false },
  { name: 'L', selected: false },
  { name: 'XL', selected: false },
  { name: 'XXL', selected: false },
]);

// Sinh ra 9 sản phẩm mẫu
const products = ref(Array.from({ length: 9 }, (_, i) => ({
  id: i + 1,
  name: 'Bộ Vest Nam Công Sở Lịch Lãm',
  price: 2500000,
  oldPrice: 2500000,
  image: '/images/ao-vest-den-5.jpg'
})));

const formatPrice = (value) => {
  return value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
};
</script>

<style scoped>
/* Nút thêm vào giỏ màu xanh Cyan đồng bộ với biến CSS layout của bạn */
.btn-cyan {
  background-color: var(--sky-aqua, #07c8f9);
  border-color: var(--sky-aqua, #07c8f9);
}
.btn-cyan:hover {
  background-color: var(--fresh-sky, #09a6f3);
  border-color: var(--fresh-sky, #09a6f3);
}

/* Custom Checkbox đồng bộ màu xanh */
.custom-checkbox:checked {
  background-color: var(--sky-aqua, #07c8f9);
  border-color: var(--sky-aqua, #07c8f9);
}
.custom-checkbox:focus {
  box-shadow: 0 0 0 0.25rem rgba(7, 200, 249, 0.25);
}

.color-box {
  display: inline-block;
  width: 16px;
  height: 16px;
  border-radius: 4px;
  border: 1px solid #dee2e6;
}

.product-card {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 .5rem 1rem rgba(0,0,0,.15)!important;
}

.custom-select {
  min-width: 150px;
  cursor: pointer;
}
.page-link {
  font-weight: 500;
}
</style>