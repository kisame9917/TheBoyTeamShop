<template>
  <div class="homepage">
    <!-- Banner -->
    <section
      class="hero-banner position-relative"
      @mouseenter="stopAuto"
      @mouseleave="startAuto"
    >
      <div
        class="hero-bg d-flex align-items-center"
        :style="{ backgroundImage: `url(${currentBanner})` }"
      >
        <div class="container">
          <div class="col-md-6 text-start hero-content">
            <h1 class="display-4 fw-bold text-royal-blue mb-3">
              VEST NAM <br />
              ƯU ĐÃI <br />
              SALE LỚN<br />
              UP TO 50%
            </h1>
            <p class="fs-5 text-secondary mb-4">Thời trang vest nam ưu đãi</p>

            <!-- Mua ngay -> đưa sang shop/search -->
            <button
              class="btn btn-primary btn-lg rounded-0 px-4 py-2 fw-bold"
              @click="goShop"
            >
              MUA NGAY
            </button>
          </div>
        </div>
      </div>

      <button
        class="slider-btn prev-btn"
        aria-label="Trước"
        @click="prevBanner"
      >
        <i class="bi bi-chevron-left"></i>
      </button>
      <button class="slider-btn next-btn" aria-label="Sau" @click="nextBanner">
        <i class="bi bi-chevron-right"></i>
      </button>

      <div
        class="slider-dots text-center position-absolute w-100 bottom-0 mb-3"
      >
        <span
          v-for="(_, i) in banners"
          :key="'banner-dot-' + i"
          class="dot"
          :class="{ active: i === currentBannerIndex }"
          @click="goToBanner(i)"
        ></span>
      </div>
    </section>

    <!-- Hàng mới về -->
    <section class="container py-5">
      <h4 class="fw-bold mb-4">HÀNG MỚI VỀ</h4>
      <div class="row row-cols-2 row-cols-md-3 row-cols-lg-5 g-4">
        <div
          class="col"
          v-for="(product, index) in products"
          :key="'new-' + product.id"
        >
          <!-- Click card -> ProductDetail -->
          <div
            class="card h-100 border-0 product-card cursor-pointer"
            role="button"
            @click="goProduct(product.id)"
          >
            <div class="position-relative bg-light card-img-wrapper">
              <span class="badge sale-badge">GIẢM<br />50%</span>
              <img :src="product.image" class="card-img-top" alt="Sản phẩm" />
            </div>
            <div class="card-body px-0 text-start">
              <p class="card-title text-truncate mb-1">{{ product.name }}</p>
              <h6 class="card-text fw-bold">{{ product.price }}</h6>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Độc quyền online -->
    <section class="container py-4">
      <h4 class="fw-bold mb-4">ĐỘC QUYỀN ONLINE</h4>
      <div class="row row-cols-2 row-cols-md-3 row-cols-lg-5 g-4">
        <div
          class="col"
          v-for="(product, index) in exclusiveProducts"
          :key="'ex-' + product.id"
        >
          <div
            class="card h-100 border-0 product-card cursor-pointer"
            role="button"
            @click="goProduct(product.id)"
          >
            <div class="position-relative bg-light card-img-wrapper">
              <span class="badge sale-badge">GIẢM<br />50%</span>
              <img :src="product.image" class="card-img-top" alt="Sản phẩm" />
            </div>
            <div class="card-body px-0 text-start">
              <p class="card-title text-truncate mb-1">{{ product.name }}</p>
              <h6 class="card-text fw-bold">{{ product.price }}</h6>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Banner giữa -->
    <section class="container py-4">
      <div
        class="mid-banner bg-brilliant-azure text-white d-flex align-items-center justify-content-between rounded overflow-hidden"
      >
        <div class="p-5 ms-4">
          <h2 class="fw-bold">BỘ SƯU TẬP VEST CÔNG SỞ</h2>
          <p class="fs-5">Bộ sưu tập vest</p>
          <button
            class="btn btn-light text-primary fw-bold px-4 rounded-0 mt-3"
            @click="goShop"
          >
            MUA NGAY
          </button>
        </div>
        <div class="banner-img-group me-5">
          <img src="/uploads/logo.jpg" alt="Bộ sưu tập" class="img-fluid" />
        </div>
      </div>
    </section>

    <!-- Bộ sưu tập ảnh -->
    <section class="container py-5">
      <h4 class="fw-bold mb-4">BỘ SƯU TẬP ẢNH</h4>
      <div class="row row-cols-2 row-cols-md-4 g-3">
        <div
          class="col"
          v-for="(src, i) in galleryuploads"
          :key="'gallery-' + i"
        >
          <div class="gallery-card">
            <img :src="src" class="gallery-img" alt="Ảnh bộ sưu tập" />
          </div>
        </div>
      </div>
    </section>

    <!-- ✅ Chat widget dính góc phải dưới -->
    <ChatWidget />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";
import ChatWidget from "../../components/ClientChatWidget.vue";

const router = useRouter();

function goProduct(id) {
  router.push({ name: "ProductDetail", params: { id } });
}

function goShop() {
  router.push({ name: "Shop" }); // hoặc name: 'Search'
}

const products = ref([
  {
    id: 1,
    name: "Thời trang Vest Nam - Full Suit",
    price: "1.695.000 VND",
    image: "/upload/ao-vest-den-1.jpg",
  },
  {
    id: 2,
    name: "Thời trang Vest Nam - Lịch lãm",
    price: "1.095.000 VND",
    image: "/uploads/ao-vest-den-2.jpg",
  },
  {
    id: 3,
    name: "Quần Vest Nam - Slimfit",
    price: "539.000 VND",
    image: "/uploads/ao-vest-den-3.jpg",
  },
  {
    id: 4,
    name: "Thời trang Vest Nam - Cổ điển",
    price: "1.525.000 VND",
    image: "/uploads/ao-vest-den-4.jpg",
  },
  {
    id: 5,
    name: "Thời trang Vest Nam - Trẻ trung",
    price: "1.398.000 VND",
    image: "/uploads/ao-vest-den-5.jpg",
  },
]);

const banners = ref([
  "/uploads/logo.jpg",
  "/uploads/logo.jpg",
  "/uploads/logo.jpg",
]);

const currentBannerIndex = ref(0);
const currentBanner = computed(() => banners.value[currentBannerIndex.value]);

const intervalMs = 3500;
let timer = null;

function nextBanner() {
  currentBannerIndex.value =
    (currentBannerIndex.value + 1) % banners.value.length;
}
function prevBanner() {
  currentBannerIndex.value =
    (currentBannerIndex.value - 1 + banners.value.length) %
    banners.value.length;
}
function goToBanner(i) {
  currentBannerIndex.value = i;
}

function startAuto() {
  stopAuto();
  timer = setInterval(nextBanner, intervalMs);
}
function stopAuto() {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
}

onMounted(() => startAuto());
onBeforeUnmount(() => stopAuto());

const exclusiveProducts = ref([
  {
    id: 101,
    name: "Vest Nam Premium - Navy",
    price: "1.635.000 VND",
    image: "/uploads/ao-vest-den-1.jpg",
  },
  {
    id: 102,
    name: "Áo khoác vest ghi xám",
    price: "1.095.000 VND",
    image: "/uploads/ao-vest-den-2.jpg",
  },
  {
    id: 103,
    name: "Quần âu nam form chuẩn",
    price: "549.000 VND",
    image: "/uploads/ao-vest-den-3.jpg",
  },
  {
    id: 104,
    name: "Bộ vest đen quyền lực",
    price: "1.520.000 VND",
    image: "/uploads/ao-vest-den-4.jpg",
  },
  {
    id: 105,
    name: "Bộ vest xanh tinh tế",
    price: "1.455.000 VND",
    image: "/uploads/ao-vest-den-5.jpg",
  },
]);

const galleryuploads = ref([
  "/uploads/ao-vest-den-11.jpg",
  "/uploads/ao-vest-den-12.jpg",
  "/uploads/ao-vest-den-13.jpg",
  "/uploads/ao-vest-den-14.jpg",
]);
</script>

<style scoped>
/* (giữ nguyên phần CSS của bạn) */
:root {
  --sky-aqua: #07c8f9;
  --fresh-sky: #09a6f3;
  --brilliant-azure: #0a85ed;
  --royal-blue: #0c63e7;
  --bright-indigo: #0d41e1;
  --pale-blue-bg: #e8f6fa;
}

.cursor-pointer {
  cursor: pointer;
}

/* ... giữ nguyên các style còn lại ... */
.text-royal-blue {
  color: var(--royal-blue);
}
.bg-brilliant-azure {
  background-color: var(--brilliant-azure);
}
.hero-banner {
  height: 500px;
  background-color: var(--pale-blue-bg);
  overflow: hidden;
}
.hero-bg {
  height: 100%;
  background-position: center right;
  background-repeat: no-repeat;
  background-size: cover;
  transition: background-image 0.35s ease;
}
.hero-content {
  background: rgba(232, 246, 250, 0.8);
  padding: 2rem;
  border-radius: 8px;
}
.slider-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: transparent;
  border: none;
  font-size: 2rem;
  color: var(--royal-blue);
  cursor: pointer;
}
.prev-btn {
  left: 20px;
}
.next-btn {
  right: 20px;
}
.dot {
  display: inline-block;
  width: 30px;
  height: 4px;
  background-color: #ccc;
  margin: 0 4px;
  cursor: pointer;
}
.dot.active {
  background-color: var(--royal-blue);
}
.card-img-wrapper {
  aspect-ratio: 3/4;
  overflow: hidden;
  border-radius: 4px;
}
.card-img-top {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}
.product-card:hover .card-img-top {
  transform: scale(1.05);
}
.sale-badge {
  background-color: var(--brilliant-azure);
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 10;
  padding: 8px 10px;
  font-size: 0.75rem;
  border-radius: 0;
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.1);
}
.gallery-card {
  border-radius: 12px;
  overflow: hidden;
  background: #f1f3f5;
  height: 380px;
}
.gallery-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.25s ease;
}
.gallery-card:hover .gallery-img {
  transform: scale(1.04);
}
</style>
