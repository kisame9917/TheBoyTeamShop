<template>
  <div class="homepage">
    <section
        class="hero-banner position-relative"
        @mouseenter="stopAuto"
        @mouseleave="startAuto"
    >
      <div
          class="hero-bg d-flex align-items-center"
          :style="{ backgroundImage: `url(${currentBanner})` }"
      >
        <div class="hero-overlay"></div>

        <div class="container position-relative">
          <div class="hero-content">
            <div class="hero-badge">THE BOY TEAM</div>

            <h1 class="hero-title">
              VEST NAM <br />
              LỊCH LÃM <br />
              ƯU ĐÃI ĐẶC BIỆT
            </h1>

            <p class="hero-desc">
              Thời trang vest nam hiện đại, sang trọng, dễ lựa chọn cho công sở,
              sự kiện và các dịp quan trọng.
            </p>

            <div class="hero-actions">
              <button class="hero-btn hero-btn--primary" @click="goShop">
                Mua ngay
              </button>
              <button class="hero-btn hero-btn--outline" @click="goShop">
                Xem bộ sưu tập
              </button>
            </div>
          </div>
        </div>
      </div>

      <button class="slider-btn prev-btn" aria-label="Trước" @click="prevBanner">
        <i class="bi bi-chevron-left"></i>
      </button>
      <button class="slider-btn next-btn" aria-label="Sau" @click="nextBanner">
        <i class="bi bi-chevron-right"></i>
      </button>

      <div class="slider-dots text-center position-absolute w-100 bottom-0 mb-4">
        <span
            v-for="(_, i) in banners"
            :key="'banner-dot-' + i"
            class="dot"
            :class="{ active: i === currentBannerIndex }"
            @click="goToBanner(i)"
        ></span>
      </div>
    </section>

    <section class="container service-strip">
      <div class="service-strip__grid">
        <div class="service-strip__item">
          <i class="bi bi-shield-check"></i>
          <span>Thanh toán an toàn</span>
        </div>
        <div class="service-strip__item">
          <i class="bi bi-truck"></i>
          <span>Giao hàng toàn quốc</span>
        </div>
        <div class="service-strip__item">
          <i class="bi bi-arrow-repeat"></i>
          <span>Hỗ trợ đổi trả</span>
        </div>
        <div class="service-strip__item">
          <i class="bi bi-headset"></i>
          <span>Tư vấn chọn size</span>
        </div>
      </div>
    </section>

    <section class="container py-5">
      <div class="section-head">
        <div>
          <div class="section-subtitle">Bộ sưu tập mới</div>
          <h4 class="section-title mb-0">HÀNG MỚI VỀ</h4>
        </div>

        <button class="section-btn" @click="goShop">
          Xem tất cả
        </button>
      </div>

      <div v-if="loading" class="state-box">Đang tải sản phẩm...</div>
      <div v-else-if="error" class="state-box text-danger">{{ error }}</div>
      <div v-else-if="products.length === 0" class="state-box">
        Hiện chưa có sản phẩm nào.
      </div>

      <div v-else class="row row-cols-2 row-cols-md-3 row-cols-lg-5 g-4">
        <div class="col" v-for="product in products" :key="'new-' + product.id">
          <div
              class="product-card"
              role="button"
              @click="goProduct(product.id)"
          >
            <div class="product-card__img-wrap">
              <span class="product-card__badge">Mới về</span>
              <img
                  :src="product.image"
                  class="product-card__img"
                  :alt="product.name"
                  @error="onProductImgError"
              />
            </div>

            <div class="product-card__body">
              <div class="product-card__name">
                {{ product.name }}
              </div>
              <div class="product-card__price">
                {{ money(product.price) }} đ
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="container py-2">
      <div class="mid-banner">
        <div class="mid-banner__content">
          <div class="mid-banner__label">Bộ sưu tập nổi bật</div>
          <h2 class="mid-banner__title">BỘ SƯU TẬP VEST CÔNG SỞ</h2>
          <p class="mid-banner__desc">
            Thiết kế chỉn chu, nam tính, hiện đại. Phù hợp cho môi trường công sở
            và các dịp cần vẻ ngoài lịch lãm.
          </p>
          <button class="mid-banner__btn" @click="goShop">
            Mua ngay
          </button>
        </div>

        <div class="mid-banner__image-wrap">
          <img
              :src="midBannerImage"
              alt="Bộ sưu tập"
              class="mid-banner__image"
              @error="onMidBannerError"
          />
        </div>
      </div>
    </section>

    <section class="container py-5">
      <div class="section-head">
        <div>
          <div class="section-subtitle">Gợi ý nổi bật</div>
          <h4 class="section-title mb-0">LÝ DO CHỌN NÊN CHỌN SẢN PHẨM TỪ THE BOY TEAM</h4>
        </div>
      </div>

      <div class="feature-grid">
        <div class="feature-card">
          <div class="feature-card__icon">
            <i class="bi bi-stars"></i>
          </div>
          <div class="feature-card__title">Phong cách lịch lãm</div>
          <div class="feature-card__text">
            Thiết kế vest nam hiện đại, dễ mặc trong nhiều bối cảnh khác nhau.
          </div>
        </div>

        <div class="feature-card">
          <div class="feature-card__icon">
            <i class="bi bi-patch-check"></i>
          </div>
          <div class="feature-card__title">Thông tin rõ ràng</div>
          <div class="feature-card__text">
            Giá bán, hình ảnh và biến thể sản phẩm được hiển thị trực quan, dễ theo dõi.
          </div>
        </div>

        <div class="feature-card">
          <div class="feature-card__icon">
            <i class="bi bi-box-seam"></i>
          </div>
          <div class="feature-card__title">Đóng gói cẩn thận</div>
          <div class="feature-card__text">
            Sản phẩm được kiểm tra kỹ trước khi giao để đảm bảo trải nghiệm tốt hơn.
          </div>
        </div>

        <div class="feature-card">
          <div class="feature-card__icon">
            <i class="bi bi-headset"></i>
          </div>
          <div class="feature-card__title">Hỗ trợ nhanh chóng</div>
          <div class="feature-card__text">
            Hỗ trợ tư vấn size, màu sắc và giải đáp thông tin sản phẩm nhanh hơn.
          </div>
        </div>
      </div>
    </section>

    <section class="container py-5">
      <div class="section-head">
        <div>
          <div class="section-subtitle">Hình ảnh sản phẩm</div>
          <h4 class="section-title mb-0">BỘ SƯU TẬP ẢNH</h4>
        </div>
      </div>

      <div class="gallery-grid">
        <div class="gallery-card" v-for="(src, i) in galleryImages" :key="'gallery-' + i">
          <img
              :src="src"
              class="gallery-img"
              alt="Ảnh bộ sưu tập"
              @error="onGalleryImgError"
          />
        </div>
      </div>
    </section>

    <ChatWidget />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getProducts, getProductVariantsByProductId } from '../../services/productClientApi';
import ChatWidget from '../../components/ClientChatWidget.vue';
import {
  parseMediaList,
  pickProductImage,
  resolveMediaUrl,
  sortNewestFirst,
} from '../../utils/media';

const router = useRouter();
const loading = ref(false);
const error = ref('');
const products = ref([]);
const currentBannerIndex = ref(0);
const intervalMs = 3500;
let timer = null;

const fallbackProductImage =
    "data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='400' height='460'%3E%3Crect width='100%25' height='100%25' fill='%23f1f3f5'/%3E%3Ctext x='50%25' y='52%25' dominant-baseline='middle' text-anchor='middle' fill='%2399a1aa' font-size='18'%3ENo Image%3C/text%3E%3C/svg%3E";

const fallbackBannerImage =
    "data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1280' height='720'%3E%3Crect width='100%25' height='100%25' fill='%23e8edf8'/%3E%3Ctext x='50%25' y='52%25' dominant-baseline='middle' text-anchor='middle' fill='%23000f51' font-size='34'%3EVest%20Banner%3C/text%3E%3C/svg%3E";

const configuredHeroBanners = computed(() => parseMediaList(import.meta.env.VITE_HOME_HERO_BANNERS));
const configuredMidBanner = computed(
    () => resolveMediaUrl(import.meta.env.VITE_HOME_MID_BANNER_URL) || '',
);

function extractName(item) {
  return item?.tenSanPham || item?.name || item?.title || item?.ten || 'Sản phẩm';
}

function extractPrice(item, variants = []) {
  const directPriceCandidates = [
    item?.giaBan,
    item?.price,
    item?.gia,
    item?.giaMacDinh,
    item?.giaKhuyenMai,
    item?.giaTu,
    item?.giaMin,
    item?.donGia,
  ];

  for (const val of directPriceCandidates) {
    const num = Number(val);
    if (!Number.isNaN(num) && num > 0) return num;
  }

  const prices = (Array.isArray(variants) ? variants : [])
      .map((detail) => Number(detail?.donGia ?? detail?.giaBan ?? detail?.price ?? detail?.gia ?? 0))
      .filter((n) => !Number.isNaN(n) && n > 0);

  if (prices.length) return Math.min(...prices);
  return 0;
}

const banners = computed(() => {
  if (configuredHeroBanners.value.length) {
    return configuredHeroBanners.value.slice(0, 5);
  }

  const dynamic = products.value.map((item) => item.image).filter(Boolean);
  return dynamic.length
      ? dynamic.slice(0, 3)
      : [fallbackBannerImage, fallbackBannerImage, fallbackBannerImage];
});

const currentBanner = computed(
    () => banners.value[currentBannerIndex.value] || fallbackBannerImage,
);

const midBannerImage = computed(
    () => configuredMidBanner.value || banners.value[0] || fallbackBannerImage,
);

const galleryImages = computed(() => {
  const imgs = products.value.map((item) => item.image).filter(Boolean);
  if (imgs.length >= 4) return imgs.slice(0, 4);
  return [...imgs, ...Array(Math.max(0, 4 - imgs.length)).fill(fallbackProductImage)];
});

async function fetchProducts() {
  try {
    loading.value = true;
    error.value = '';

    const data = await getProducts({ page: 0, size: 10 });

    const raw = Array.isArray(data?.content)
        ? data.content
        : Array.isArray(data?.data?.content)
            ? data.data.content
            : Array.isArray(data?.data)
                ? data.data
                : Array.isArray(data)
                    ? data
                    : [];

    const sorted = sortNewestFirst(raw).slice(0, 10);

    const enriched = await Promise.all(
        sorted.map(async (item) => {
          let variants = [];
          try {
            const variantRes = await getProductVariantsByProductId(item.id);
            variants = Array.isArray(variantRes) ? variantRes : [];
          } catch (variantErr) {
            console.error('fetch home variants error:', item.id, variantErr);
          }

          return {
            id: item.id,
            name: extractName(item),
            price: extractPrice(item, variants),
            image: pickProductImage(item, variants, fallbackProductImage),
            raw: item,
          };
        }),
    );

    products.value = sortNewestFirst(enriched);

    if (currentBannerIndex.value >= banners.value.length) {
      currentBannerIndex.value = 0;
    }
  } catch (err) {
    console.error('fetchProducts error:', err);
    error.value = err?.response?.data?.message || 'Không tải được sản phẩm';
  } finally {
    loading.value = false;
  }
}

function goProduct(id) {
  router.push({ name: 'ProductDetail', params: { id } });
}

function goShop() {
  router.push({ name: 'Search' });
}

function money(v) {
  const n = Number(v) || 0;
  return n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.');
}

function nextBanner() {
  currentBannerIndex.value = (currentBannerIndex.value + 1) % banners.value.length;
}

function prevBanner() {
  currentBannerIndex.value =
      (currentBannerIndex.value - 1 + banners.value.length) % banners.value.length;
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

function onProductImgError(e) {
  e.target.src = fallbackProductImage;
}

function onGalleryImgError(e) {
  e.target.src = fallbackProductImage;
}

function onMidBannerError(e) {
  e.target.src = fallbackBannerImage;
}

onMounted(async () => {
  await fetchProducts();
  startAuto();
});

onBeforeUnmount(() => stopAuto());
</script>

<style scoped>
.homepage {
  background: linear-gradient(180deg, #f5f7fc 0%, #f3f4f8 100%);
  min-height: 100vh;
}

.cursor-pointer {
  cursor: pointer;
}

.hero-banner {
  height: 620px;
  position: relative;
  overflow: hidden;
  background: #e8edf8;
}

.hero-bg {
  position: relative;
  height: 100%;
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
  transition: background-image 0.35s ease;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background:
      linear-gradient(90deg, rgba(0, 15, 81, 0.82) 0%, rgba(0, 15, 81, 0.46) 42%, rgba(0, 15, 81, 0.08) 100%);
}

.hero-content {
  position: relative;
  z-index: 2;
  max-width: 560px;
  padding: 42px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.12);
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  color: #fff;
  font-size: 13px;
  font-weight: 750;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  margin-bottom: 18px;
}

.hero-title {
  font-size: 56px;
  line-height: 1.08;
  color: #fff;
  font-weight: 750;
  margin-bottom: 16px;
}

.hero-desc {
  font-size: 17px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.86);
  margin-bottom: 24px;
  max-width: 500px;
}

.hero-actions {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
}

.hero-btn {
  min-height: 52px;
  padding: 0 24px;
  border-radius: 16px;
  font-weight: 750;
  font-size: 15px;
  transition: all 0.25s ease;
}

.hero-btn--primary {
  border: none;
  background: #fff;
  color: #000f51;
}

.hero-btn--outline {
  border: 1px solid rgba(255, 255, 255, 0.32);
  background: transparent;
  color: #fff;
}

.hero-btn--primary:hover,
.hero-btn--outline:hover {
  transform: translateY(-2px);
}

.slider-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
  font-size: 24px;
  z-index: 3;
  transition: all 0.25s ease;
}

.slider-btn:hover {
  background: rgba(255, 255, 255, 0.28);
}

.prev-btn {
  left: 24px;
}

.next-btn {
  right: 24px;
}

.dot {
  display: inline-block;
  width: 34px;
  height: 5px;
  background-color: rgba(255, 255, 255, 0.35);
  margin: 0 4px;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.dot.active {
  background-color: #fff;
}

.service-strip {
  margin-top: -34px;
  position: relative;
  z-index: 4;
}

.service-strip__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  background: #fff;
  border-radius: 24px;
  padding: 18px 22px;
  box-shadow: 0 20px 40px rgba(10, 24, 74, 0.08);
  border: 1px solid rgba(15, 23, 42, 0.06);
}

.service-strip__item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #334155;
  font-weight: 750;
  min-height: 52px;
}

.service-strip__item i {
  color: #000f51;
  font-size: 18px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: end;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.section-subtitle {
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: #64748b;
  margin-bottom: 8px;
  font-weight: 750;
}

.section-title {
  font-size: 30px;
  color: #0f172a;
  font-weight: 750;
}

.section-btn {
  min-height: 46px;
  padding: 0 20px;
  border-radius: 14px;
  border: 1px solid #d8dfec;
  background: #fff;
  color: #0f172a;
  font-weight: 750;
  transition: all 0.25s ease;
}

.section-btn:hover {
  border-color: #001a72;
  color: #001a72;
}

.product-card {
  height: 100%;
  background: #fff;
  border-radius: 22px;
  overflow: hidden;
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 14px 30px rgba(10, 24, 74, 0.05);
  transition: all 0.25s ease;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 36px rgba(10, 24, 74, 0.1);
}

.product-card__img-wrap {
  position: relative;
  aspect-ratio: 3 / 4;
  overflow: hidden;
  background: #f8fafc;
  padding: 12px;
}

.product-card__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 16px;
  transition: transform 0.35s ease;
}

.product-card:hover .product-card__img {
  transform: scale(1.05);
}

.product-card__badge {
  position: absolute;
  top: 18px;
  left: 18px;
  z-index: 2;
  padding: 8px 12px;
  border-radius: 999px;
  background: linear-gradient(135deg, #000f51 0%, #0f2c9c 100%);
  color: #fff;
  font-size: 12px;
  font-weight: 750;
  box-shadow: 0 10px 20px rgba(0, 15, 81, 0.16);
}

.product-card__body {
  padding: 16px 16px 18px;
}

.product-card__name {
  font-size: 15px;
  line-height: 1.5;
  color: #0f172a;
  font-weight: 750;
  min-height: 45px;
  margin-bottom: 8px;
}

.product-card__price {
  font-size: 20px;
  color: #000f51;
  font-weight: 750;
}

.mid-banner {
  min-height: 380px;
  border-radius: 28px;
  overflow: hidden;
  background: linear-gradient(135deg, #000f51 0%, #12348f 100%);
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  align-items: center;
  box-shadow: 0 22px 44px rgba(0, 15, 81, 0.14);
}

.mid-banner__content {
  padding: 42px;
  color: #fff;
}

.mid-banner__label {
  display: inline-block;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 13px;
  font-weight: 750;
  margin-bottom: 16px;
}

.mid-banner__title {
  font-size: 38px;
  line-height: 1.2;
  font-weight: 750;
  margin-bottom: 14px;
}

.mid-banner__desc {
  font-size: 16px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.84);
  max-width: 520px;
  margin-bottom: 22px;
}

.mid-banner__btn {
  min-height: 48px;
  padding: 0 22px;
  border-radius: 14px;
  border: none;
  background: #fff;
  color: #000f51;
  font-weight: 750;
}

.mid-banner__image-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px;
}

.mid-banner__image {
  width: 100%;
  max-width: 420px;
  max-height: 320px;
  object-fit: cover;
  border-radius: 22px;
  box-shadow: 0 18px 36px rgba(0, 0, 0, 0.18);
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.feature-card {
  background: #fff;
  border-radius: 22px;
  padding: 24px 20px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 14px 30px rgba(10, 24, 74, 0.05);
}

.feature-card__icon {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #eaf0ff;
  color: #000f51;
  font-size: 20px;
  margin-bottom: 16px;
}

.feature-card__title {
  color: #0f172a;
  font-weight: 750;
  font-size: 18px;
  margin-bottom: 10px;
}

.feature-card__text {
  color: #64748b;
  line-height: 1.75;
  font-size: 14px;
}

.gallery-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.gallery-card {
  overflow: hidden;
  border-radius: 22px;
  background: #fff;
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 14px 30px rgba(10, 24, 74, 0.05);
}

.gallery-img {
  width: 100%;
  height: 340px;
  object-fit: cover;
  display: block;
  transition: transform 0.35s ease;
}

.gallery-card:hover .gallery-img {
  transform: scale(1.05);
}

.state-box {
  padding: 24px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(7, 20, 69, 0.05);
}

@media (max-width: 1199.98px) {
  .feature-grid,
  .gallery-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .mid-banner {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 991.98px) {
  .hero-banner {
    height: 560px;
  }

  .hero-title {
    font-size: 42px;
  }

  .service-strip__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767.98px) {
  .hero-banner {
    height: 520px;
  }

  .hero-content {
    padding: 24px;
    border-radius: 22px;
  }

  .hero-title {
    font-size: 34px;
  }

  .hero-desc {
    font-size: 15px;
  }

  .hero-actions {
    flex-direction: column;
  }

  .hero-btn {
    width: 100%;
  }

  .service-strip__grid,
  .feature-grid,
  .gallery-grid {
    grid-template-columns: 1fr;
  }

  .section-title {
    font-size: 24px;
  }

  .mid-banner__content {
    padding: 28px 22px;
  }

  .mid-banner__title {
    font-size: 28px;
  }

  .gallery-img {
    height: 260px;
  }
}
</style>