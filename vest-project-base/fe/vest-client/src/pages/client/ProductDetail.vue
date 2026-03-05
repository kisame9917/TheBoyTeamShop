<template>
  <div class="product-detail-page container py-4">
    <nav aria-label="breadcrumb" class="mb-4">
      <ol class="breadcrumb">
        <li class="breadcrumb-item"><router-link to="/" class="text-muted text-decoration-none">Trang chủ</router-link></li>
        <li class="breadcrumb-item"><router-link to="/shop" class="text-muted text-decoration-none">TheBoyTeamShop</router-link></li>
        <li class="breadcrumb-item active text-dark fw-medium" aria-current="page">{{ product.title }}</li>
      </ol>
    </nav>

    <div class="row bg-white p-4 rounded-3 shadow-sm mb-5">
      <div class="col-md-6 d-flex gap-3 mb-4 mb-md-0">
        <div class="d-flex flex-column gap-2" style="width: 85px;">
          <img v-for="(img, index) in product.images" :key="index" :src="img"
               class="img-thumbnail cursor-pointer p-1"
               :class="{'border-primary': currentImage === img}"
               @click="currentImage = img" alt="Thumbnail" />
        </div>
        <div class="flex-grow-1">
          <img :src="currentImage" class="img-fluid w-100 rounded" alt="Vest Nam" />
        </div>
      </div>

      <div class="col-md-6 ps-md-5">
        <h1 class="h3 fw-bold mb-3">{{ product.title }}</h1>

        <div class="d-flex align-items-center gap-3 mb-4">
          <span class="fs-3 fw-bold text-dark">{{ formatPrice(product.price) }}đ</span>
          <span class="text-decoration-line-through text-muted fs-5">{{ formatPrice(product.oldPrice) }}đ</span>
          <span class="badge bg-primary px-2 py-1">SALE 50%</span>
        </div>

        <div class="mb-4">
          <p class="mb-2 fw-semibold">Màu sắc:</p>
          <div class="d-flex gap-2">
            <div v-for="color in product.colors" :key="color.code"
                 class="color-swatch border rounded-circle cursor-pointer"
                 :class="{'ring-active': selectedColor === color.code}"
                 :style="{ backgroundColor: color.code }"
                 @click="selectedColor = color.code">
            </div>
          </div>
        </div>

        <div class="mb-4">
          <p class="mb-2 fw-semibold">Size:</p>
          <div class="d-flex gap-2">
            <button v-for="size in product.sizes" :key="size"
                    class="btn btn-outline-secondary size-btn"
                    :class="{'active-size': selectedSize === size}"
                    @click="selectedSize = size">
              {{ size }}
            </button>
          </div>
        </div>

        <div class="d-flex gap-3 mb-5">
          <div class="input-group" style="width: 130px;">
            <button class="btn btn-outline-secondary" type="button" @click="decrease">-</button>

            <input
                type="text"
                class="form-control text-center bg-white"
                v-model="quantity"
                readonly
            />

            <button class="btn btn-outline-secondary" type="button" @click="increase">+</button>
          </div>

          <button class="btn btn-cyan text-white fw-semibold px-4" type="button" @click="addToCartNow">
            THÊM VÀO GIỎ
          </button>
        </div>

        <div class="accordion accordion-flush" id="productAccordion">
          <div class="accordion-item border-top">
            <h2 class="accordion-header">
              <button class="accordion-button px-0 fw-bold" type="button" data-bs-toggle="collapse" data-bs-target="#collapseDesc">
                Mô tả sản phẩm
              </button>
            </h2>
            <div id="collapseDesc" class="accordion-collapse collapse show" data-bs-parent="#productAccordion">
              <div class="accordion-body px-0 text-muted">
                Bộ vest nam cao cấp. Form dáng slim fit hiện đại, chất liệu vải nhập khẩu chống nhăn, giữ form tốt. Phù hợp cho môi trường công sở hoặc các sự kiện quan trọng.
              </div>
            </div>
          </div>
          <div class="accordion-item">
            <h2 class="accordion-header">
              <button class="accordion-button collapsed px-0 fw-bold" type="button" data-bs-toggle="collapse" data-bs-target="#collapsePolicy">
                Chính sách đổi trả
              </button>
            </h2>
            <div id="collapsePolicy" class="accordion-collapse collapse" data-bs-parent="#productAccordion">
              <div class="accordion-body px-0 text-muted">
                Hỗ trợ đổi size trong vòng 7 ngày. Sản phẩm phải còn nguyên tem mác và chưa qua sử dụng.
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="my-5">
      <h3 class="fw-bold mb-4">Sản phẩm tương tự</h3>
      <div class="row row-cols-2 row-cols-md-4 g-4">
        <div class="col" v-for="i in 4" :key="i">
          <div class="card h-100 border-0 shadow-sm product-card">
            <div class="position-relative">
              <span class="badge bg-primary position-absolute top-0 start-0 m-2 z-1">SALE 50%</span>
              <img src="/uploads/ao-vest-den-6.jpg" class="card-img-top rounded-top" alt="Product" />
            </div>
            <div class="card-body p-3">
              <h6 class="card-title text-truncate mb-2">Bộ Vest Nam Cao Cấp - Mẫu {{ i }}</h6>
              <div class="d-flex align-items-baseline gap-2 mb-3">
                <span class="fw-bold text-dark">2.500.000đ</span>
                <small class="text-decoration-line-through text-muted">5.000.000đ</small>
              </div>
              <button class="btn btn-cyan w-100 text-white fw-semibold">THÊM VÀO GIỎ</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useCart } from '../../composables/useCart';

const { addToCart } = useCart();


const decrease = () => {
  if (quantity.value > 1) quantity.value--;
};

const increase = () => {
  quantity.value++;
};
const product = ref({
  title: 'BỘ VEST NAM CAO CẤP',
  price: 2500000,
  oldPrice: 5000000,
  images: [
    '/images/ao-vest-den-15.jpg',
    '/images/ao-vest-den-16.jpg',
    '/images/ao-vest-den-17.jpg',
    '/images/ao-vest-den-18.jpg'
  ],
  colors: [
    { name: 'Xanh Navy', code: '#1B263B' },
    { name: 'Đen', code: '#000000' }
  ],
  sizes: ['S', 'M', 'L', 'XL']
});

const currentImage = ref(product.value.images[0]);
const selectedColor = ref(product.value.colors[0].code);
const selectedSize = ref('S');
const quantity = ref(1);

const formatPrice = (value) => {
  return value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
};

function addToCartNow() {
  addToCart({
    id: 1,
    name: product.value.title,
    price: product.value.price,
    image: currentImage.value,
    color: selectedColor.value,
    size: selectedSize.value,
  }, quantity.value);
}
</script>

<style scoped>
.cursor-pointer { cursor: pointer; }

/* Nút thêm vào giỏ màu xanh theo design */
.btn-cyan {
  background-color: var(--sky-aqua, #07c8f9);
  border-color: var(--sky-aqua, #07c8f9);
}
.btn-cyan:hover {
  background-color: var(--fresh-sky, #09a6f3);
  border-color: var(--fresh-sky, #09a6f3);
}

.color-swatch {
  width: 30px;
  height: 30px;
  display: inline-block;
  border: 2px solid transparent;
}
.ring-active {
  box-shadow: 0 0 0 2px white, 0 0 0 4px var(--royal-blue, #0c63e7);
}

.size-btn {
  min-width: 45px;
}
.active-size {
  background-color: var(--royal-blue, #0c63e7);
  color: white;
  border-color: var(--royal-blue, #0c63e7);
}

.product-card {
  transition: transform 0.2s, box-shadow 0.2s;
}
.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 .5rem 1rem rgba(0,0,0,.15)!important;
}

.accordion-button:not(.collapsed) {
  background-color: transparent;
  color: var(--royal-blue, #0c63e7);
  box-shadow: none;
}
</style>