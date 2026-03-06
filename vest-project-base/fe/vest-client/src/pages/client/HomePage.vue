<template>
  <div class="home-page">
    <section class="hero-section">
      <div class="container py-5">
        <div class="row align-items-center g-4">
          <div class="col-lg-6">
            <div class="hero-content">
              <div class="hero-badge">VestShop</div>
              <h1 class="hero-title">
                Vest nam lịch lãm cho mọi dịp quan trọng
              </h1>
              <p class="hero-desc">
                Khám phá bộ sưu tập vest mới nhất với thiết kế hiện đại, chất liệu cao cấp
                và đa dạng kiểu dáng phù hợp đi làm, đi tiệc, cưới hỏi.
              </p>

              <div class="hero-actions">
                <router-link to="/search" class="btn btn-dark btn-lg">
                  Xem sản phẩm
                </router-link>
              </div>
            </div>
          </div>

          <div class="col-lg-6">
            <div class="hero-image-wrap">
              <img
                src="https://images.unsplash.com/photo-1594938298603-c8148c4dae35?q=80&w=1200&auto=format&fit=crop"
                alt="VestShop"
                class="hero-image"
                @error="onHeroImgError"
              />
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="new-products-section">
      <div class="container py-5">
        <div class="section-head">
          <div>
            <div class="section-subtitle">Sản phẩm mới</div>
            <h2 class="section-title">8 sản phẩm mới nhất</h2>
          </div>

          <router-link to="/search" class="btn btn-outline-dark">
            Xem tất cả
          </router-link>
        </div>

        <div v-if="loading" class="state-box">
          Đang tải sản phẩm...
        </div>

        <div v-else-if="error" class="state-box text-danger">
          {{ error }}
        </div>

        <div v-else-if="products.length === 0" class="state-box">
          Hiện chưa có sản phẩm nào.
        </div>

        <div v-else class="row g-4">
          <div
            v-for="item in products"
            :key="item.id"
            class="col-6 col-md-4 col-xl-3"
          >
            <div class="product-card" @click="goDetail(item.id)">
              <img
                :src="productImage(item)"
                :alt="productName(item)"
                class="product-img"
                @error="onProductImgError"
              />

              <div class="product-body">
                <div class="product-name">
                  {{ productName(item) }}
                </div>

                <div class="product-desc">
                  {{ productDesc(item) }}
                </div>

                <div class="product-price">
                  {{ money(productPrice(item)) }} đ
                </div>

                <button
                  class="btn btn-dark btn-sm mt-3"
                  type="button"
                  @click.stop="goDetail(item.id)"
                >
                  Xem chi tiết
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="feature-section">
      <div class="container py-5">
        <div class="row g-3">
          <div class="col-md-4">
            <div class="feature-box">
              <div class="feature-title">Thiết kế hiện đại</div>
              <div class="feature-desc">
                Các mẫu vest được cập nhật theo xu hướng mới, dễ phối và sang trọng.
              </div>
            </div>
          </div>

          <div class="col-md-4">
            <div class="feature-box">
              <div class="feature-title">Chất liệu cao cấp</div>
              <div class="feature-desc">
                Chú trọng form dáng, chất vải và trải nghiệm mặc thoải mái.
              </div>
            </div>
          </div>

          <div class="col-md-4">
            <div class="feature-box">
              <div class="feature-title">Đặt hàng online nhanh</div>
              <div class="feature-desc">
                Tạo đơn online, thanh toán COD hoặc QR, đồng bộ trực tiếp về hệ thống.
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { getProducts } from "../../services/productClientApi";

const router = useRouter();

const loading = ref(false);
const error = ref("");
const products = ref([]);

const fallbackProductImage =
  "data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='400' height='460'%3E%3Crect width='100%25' height='100%25' fill='%23f1f3f5'/%3E%3Ctext x='50%25' y='52%25' dominant-baseline='middle' text-anchor='middle' fill='%2399a1aa' font-size='18'%3ENo Image%3C/text%3E%3C/svg%3E";

const fallbackHeroImage =
  "data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1200' height='900'%3E%3Crect width='100%25' height='100%25' fill='%23e9ecef'/%3E%3Ctext x='50%25' y='52%25' dominant-baseline='middle' text-anchor='middle' fill='%2399a1aa' font-size='28'%3EVestShop%3C/text%3E%3C/svg%3E";

function productName(item) {
  return item.tenSanPham || item.name || item.title || "Sản phẩm";
}

function productDesc(item) {
  return item.moTa || item.description || "Sản phẩm thời trang nam cao cấp";
}

function productPrice(item) {
  return Number(item.giaBan || item.price || 0);
}

function productImage(item) {
  if (Array.isArray(item.hinhAnh) && item.hinhAnh.length) {
    return item.hinhAnh[0];
  }
  return item.hinhAnh || item.image || fallbackProductImage;
}

function money(v) {
  const n = Number(v) || 0;
  return n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
}

function goDetail(id) {
  router.push({ name: "ProductDetail", params: { id } });
}

function onProductImgError(e) {
  e.target.src = fallbackProductImage;
}

function onHeroImgError(e) {
  e.target.src = fallbackHeroImage;
}

async function fetchLatestProducts() {
  try {
    loading.value = true;
    error.value = "";

    const data = await getProducts({
      page: 0,
      size: 8,
    });

    products.value = Array.isArray(data?.content) ? data.content : [];
  } catch (err) {
    console.error("fetchLatestProducts error:", err);
    error.value =
      err?.response?.data?.message || "Không tải được sản phẩm mới nhất";
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  fetchLatestProducts();
});
</script>

<style scoped>
.home-page {
  background: #fff;
}

.hero-section {
  background: linear-gradient(135deg, #f8f9fa 0%, #eef1f4 100%);
}

.hero-content {
  max-width: 560px;
}

.hero-badge {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.06);
  font-size: 13px;
  font-weight: 700;
  color: #111;
  margin-bottom: 16px;
}

.hero-title {
  font-size: 42px;
  line-height: 1.15;
  font-weight: 800;
  color: #111;
  margin-bottom: 16px;
}

.hero-desc {
  font-size: 16px;
  color: #5f6b76;
  line-height: 1.7;
  margin-bottom: 24px;
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-image-wrap {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.12);
  background: #fff;
}

.hero-image {
  width: 100%;
  height: 520px;
  object-fit: cover;
  display: block;
}

.section-head {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
}

.section-subtitle {
  font-size: 14px;
  font-weight: 700;
  color: #6c757d;
  margin-bottom: 6px;
}

.section-title {
  font-size: 30px;
  font-weight: 800;
  color: #111;
  margin: 0;
}

.state-box {
  padding: 24px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 14px;
  background: #fff;
}

.product-card {
  height: 100%;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 14px;
  overflow: hidden;
  background: #fff;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.product-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.08);
}

.product-img {
  width: 100%;
  height: 320px;
  object-fit: cover;
  background: #f1f3f5;
  display: block;
}

.product-body {
  padding: 16px;
}

.product-name {
  font-size: 15px;
  font-weight: 700;
  color: #111;
  line-height: 1.35;
  min-height: 42px;
}

.product-desc {
  font-size: 13px;
  color: #6c757d;
  margin-top: 8px;
  min-height: 38px;
  line-height: 1.45;
}

.product-price {
  margin-top: 12px;
  font-size: 20px;
  font-weight: 800;
  color: #c1121f;
}

.feature-section {
  background: #fafafa;
}

.feature-box {
  height: 100%;
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 14px;
  padding: 22px;
  background: #fff;
}

.feature-title {
  font-size: 18px;
  font-weight: 700;
  color: #111;
  margin-bottom: 10px;
}

.feature-desc {
  font-size: 14px;
  color: #6c757d;
  line-height: 1.6;
}

@media (max-width: 991.98px) {
  .hero-title {
    font-size: 34px;
  }

  .hero-image {
    height: 420px;
  }
}

@media (max-width: 575.98px) {
  .hero-title {
    font-size: 28px;
  }

  .hero-image {
    height: 320px;
  }

  .product-img {
    height: 240px;
  }

  .section-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>