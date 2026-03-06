<template>
  <div class="product-detail-page">
    <div class="container py-4">
      <nav aria-label="breadcrumb" class="mb-3">
        <ol class="breadcrumb mb-0">
          <li class="breadcrumb-item">
            <router-link to="/" class="text-muted text-decoration-none">
              Trang chủ
            </router-link>
          </li>
          <li class="breadcrumb-item">
            <router-link to="/search" class="text-muted text-decoration-none">
              Sản phẩm
            </router-link>
          </li>
          <li class="breadcrumb-item active text-dark" aria-current="page">
            {{ productTitle || "Chi tiết sản phẩm" }}
          </li>
        </ol>
      </nav>

      <div v-if="loading" class="state-box">Đang tải chi tiết sản phẩm...</div>

      <div v-else-if="loadError" class="state-box text-danger">
        {{ loadError }}
      </div>

      <div v-else-if="product" class="product-detail-card">
        <div class="row g-4">
          <div class="col-lg-6">
            <div class="gallery-card">
              <img
                :src="currentImage"
                :alt="productTitle"
                class="main-image"
                @error="onImgError"
              />
            </div>
          </div>

          <div class="col-lg-6">
            <div class="info-card">
              <div class="product-brand">VestShop</div>

              <h1 class="product-title">
                {{ productTitle }}
              </h1>

              <div class="product-code" v-if="selectedVariant?.code">
                Mã SPCT: {{ selectedVariant.code }}
              </div>

              <div class="price-wrap">
                <div class="product-price">
                  {{ formatCurrency(displayPrice) }}
                </div>
              </div>

              <div class="stock-line" v-if="selectedVariant">
                <span
                  class="stock-badge"
                  :class="{ out: (selectedVariant.stock ?? 0) <= 0 }"
                >
                  {{
                    (selectedVariant.stock ?? 0) > 0
                      ? `Còn ${selectedVariant.stock} sản phẩm`
                      : "Hết hàng"
                  }}
                </span>
              </div>

              <div class="desc-box" v-if="productDescription">
                {{ productDescription }}
              </div>

              <div class="variant-section" v-if="availableColors.length">
                <div class="variant-label">Màu sắc</div>
                <div class="variant-options">
                  <button
                    v-for="color in availableColors"
                    :key="color"
                    type="button"
                    class="variant-btn"
                    :class="{ active: selectedColor === color }"
                    @click="selectColor(color)"
                  >
                    {{ color }}
                  </button>
                </div>
              </div>

              <div class="variant-section" v-if="availableSizes.length">
                <div class="variant-label">Kích thước</div>
                <div class="variant-options">
                  <button
                    v-for="size in availableSizes"
                    :key="size"
                    type="button"
                    class="variant-btn size-btn"
                    :class="{ active: selectedSize === size }"
                    @click="selectSize(size)"
                  >
                    {{ size }}
                  </button>
                </div>
              </div>

              <div class="qty-section">
                <div class="variant-label">Số lượng</div>
                <div class="qty-wrap">
                  <button
                    type="button"
                    class="qty-btn"
                    @click="decreaseQty"
                    :disabled="quantity <= 1"
                  >
                    -
                  </button>

                  <input
                    v-model.number="quantity"
                    type="number"
                    min="1"
                    :max="maxQty"
                    class="qty-input"
                  />

                  <button
                    type="button"
                    class="qty-btn"
                    @click="increaseQty"
                    :disabled="quantity >= maxQty"
                  >
                    +
                  </button>
                </div>
              </div>

              <div v-if="warningMessage" class="warning-box">
                {{ warningMessage }}
              </div>

              <div class="action-wrap">
                <button
                  type="button"
                  class="btn-add-cart"
                  :disabled="!canAddToCart"
                  @click="handleAddToCart"
                >
                  Thêm vào giỏ hàng
                </button>

                <router-link to="/cart" class="btn-view-cart">
                  Xem giỏ hàng
                </router-link>
              </div>

              <div class="extra-info">
                <div class="extra-item">Miễn phí tư vấn chọn size phù hợp</div>
                <div class="extra-item">Giao hàng toàn quốc</div>
                <div class="extra-item">Hỗ trợ COD và QR</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="state-box">Không tìm thấy sản phẩm.</div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { useCart } from "../../composables/useCart";
import {
  getProductById,
  getProductVariantsByProductId,
} from "../../services/productClientApi";

const route = useRoute();
const { addToCart } = useCart();

const fallbackImage =
  "data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='700' height='820'%3E%3Crect width='100%25' height='100%25' fill='%23f1f3f5'/%3E%3Ctext x='50%25' y='52%25' dominant-baseline='middle' text-anchor='middle' fill='%2399a1aa' font-size='30'%3ENo Image%3C/text%3E%3C/svg%3E";

const loading = ref(false);
const loadError = ref("");
const product = ref(null);
const variants = ref([]);

const selectedColor = ref("");
const selectedSize = ref("");
const quantity = ref(1);

const productId = computed(() => route.params.id);

const productTitle = computed(() => {
  if (!product.value) return "";
  return (
    product.value.tenSanPham ||
    product.value.title ||
    product.value.name ||
    "Sản phẩm"
  );
});

const productDescription = computed(() => {
  if (!product.value) return "";
  return product.value.moTa || product.value.description || "";
});

function normalizeImage(value) {
  if (!value) return fallbackImage;
  if (Array.isArray(value)) return value[0] || fallbackImage;
  return value;
}

function normalizeColorName(raw) {
  if (!raw) return "";
  if (typeof raw === "string") return raw;

  return raw.tenMauSac || raw.mauSac || raw.name || raw.value || "";
}

function normalizeSizeName(raw) {
  if (!raw) return "";
  if (typeof raw === "string") return raw;

  return raw.tenKichCo || raw.kichCo || raw.size || raw.name || raw.value || "";
}

function mapVariant(v) {
  const colorName =
    normalizeColorName(v.mauSac) ||
    normalizeColorName(v.tenMauSac) ||
    normalizeColorName(v.mau) ||
    "";

  const sizeName =
    normalizeSizeName(v.kichCo) ||
    normalizeSizeName(v.tenKichCo) ||
    normalizeSizeName(v.size) ||
    "";

  const image =
    normalizeImage(v.hinhAnh) ||
    normalizeImage(v.anh) ||
    normalizeImage(v.image) ||
    normalizeImage(product.value?.hinhAnh) ||
    normalizeImage(product.value?.image);

  return {
    idSanPhamChiTiet: v.id,
    productId: v.idSanPham || v.sanPhamId || productId.value,
    color: colorName,
    size: sizeName,
    price: Number(v.donGia ?? v.giaBan ?? v.price ?? 0),
    stock: Number(v.soLuongTon ?? v.soLuong ?? 0),
    image,
    code: v.maSanPhamChiTiet || v.code || "",
  };
}

const normalizedVariants = computed(() => {
  return (variants.value || []).map(mapVariant);
});

const availableColors = computed(() => {
  const colors = normalizedVariants.value.map((v) => v.color).filter(Boolean);
  return [...new Set(colors)];
});

const filteredSizesByColor = computed(() => {
  if (!selectedColor.value) return normalizedVariants.value;
  return normalizedVariants.value.filter(
    (v) => v.color === selectedColor.value,
  );
});

const availableSizes = computed(() => {
  const sizes = filteredSizesByColor.value.map((v) => v.size).filter(Boolean);
  return [...new Set(sizes)];
});

const selectedVariant = computed(() => {
  return (
    normalizedVariants.value.find((v) => {
      const matchColor = selectedColor.value
        ? v.color === selectedColor.value
        : true;

      const matchSize = selectedSize.value
        ? v.size === selectedSize.value
        : true;

      return matchColor && matchSize;
    }) || null
  );
});

const displayPrice = computed(() => {
  if (selectedVariant.value?.price) return selectedVariant.value.price;

  const firstPrice = normalizedVariants.value[0]?.price;
  if (firstPrice) return firstPrice;

  return Number(product.value?.giaBan || product.value?.price || 0);
});

const currentImage = computed(() => {
  if (selectedVariant.value?.image) return selectedVariant.value.image;

  return (
    normalizeImage(product.value?.hinhAnh) ||
    normalizeImage(product.value?.image) ||
    fallbackImage
  );
});

const maxQty = computed(() => {
  return selectedVariant.value?.stock > 0 ? selectedVariant.value.stock : 1;
});

const warningMessage = computed(() => {
  if (!normalizedVariants.value.length) {
    return "Sản phẩm hiện chưa có biến thể khả dụng.";
  }

  if (!selectedColor.value) {
    return "Vui lòng chọn màu sắc.";
  }

  if (!selectedSize.value) {
    return "Vui lòng chọn kích thước.";
  }

  if (!selectedVariant.value) {
    return "Không tìm thấy biến thể phù hợp.";
  }

  if (!selectedVariant.value.idSanPhamChiTiet) {
    return "Biến thể chưa có idSanPhamChiTiet.";
  }

  if ((selectedVariant.value.stock ?? 0) <= 0) {
    return "Sản phẩm đã hết hàng.";
  }

  if (quantity.value > (selectedVariant.value.stock ?? 0)) {
    return "Số lượng vượt quá tồn kho.";
  }

  return "";
});

const canAddToCart = computed(() => !warningMessage.value);

function formatCurrency(value) {
  return Number(value || 0).toLocaleString("vi-VN") + " đ";
}

function onImgError(e) {
  e.target.src = fallbackImage;
}

function increaseQty() {
  if (quantity.value < maxQty.value) {
    quantity.value += 1;
  }
}

function decreaseQty() {
  if (quantity.value > 1) {
    quantity.value -= 1;
  }
}

function selectColor(color) {
  selectedColor.value = color;

  const validSizes = normalizedVariants.value
    .filter((v) => v.color === color)
    .map((v) => v.size)
    .filter(Boolean);

  if (!validSizes.includes(selectedSize.value)) {
    selectedSize.value = validSizes[0] || "";
  }

  if (quantity.value > maxQty.value) {
    quantity.value = Math.max(1, maxQty.value);
  }
}

function selectSize(size) {
  selectedSize.value = size;

  if (quantity.value > maxQty.value) {
    quantity.value = Math.max(1, maxQty.value);
  }
}

async function fetchProductDetail() {
  loading.value = true;
  loadError.value = "";

  try {
    const [productRes, variantRes] = await Promise.all([
      getProductById(productId.value),
      getProductVariantsByProductId(productId.value),
    ]);

    product.value = productRes;
    variants.value = Array.isArray(variantRes) ? variantRes : [];

    const firstVariant = normalizedVariants.value[0] || null;
    if (firstVariant) {
      selectedColor.value = firstVariant.color || "";
      selectedSize.value = firstVariant.size || "";
    }
  } catch (error) {
    console.error("fetchProductDetail error:", error);
    loadError.value =
      error?.response?.data?.message || "Không tải được chi tiết sản phẩm.";
  } finally {
    loading.value = false;
  }
}

function handleAddToCart() {
  if (!selectedVariant.value) return;
  if (!selectedVariant.value.idSanPhamChiTiet) return;
  if ((selectedVariant.value.stock ?? 0) <= 0) return;

  addToCart(
    {
      idSanPhamChiTiet: selectedVariant.value.idSanPhamChiTiet,
      productId: selectedVariant.value.productId,
      name: productTitle.value,
      image: selectedVariant.value.image,
      color: selectedVariant.value.color,
      size: selectedVariant.value.size,
      price: selectedVariant.value.price,
      stock: selectedVariant.value.stock,
      code: selectedVariant.value.code,
    },
    quantity.value,
  );

  alert("Đã thêm vào giỏ hàng");
}

watch(quantity, (val) => {
  if (!val || val < 1) {
    quantity.value = 1;
    return;
  }

  if (selectedVariant.value?.stock && val > selectedVariant.value.stock) {
    quantity.value = selectedVariant.value.stock;
  }
});

onMounted(fetchProductDetail);
</script>

<style scoped>
.product-detail-page {
  background: #f6f7fb;
  min-height: 100vh;
}

.state-box {
  padding: 24px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.08);
}

.product-detail-card {
  background: transparent;
}

.gallery-card,
.info-card {
  background: #fff;
  border-radius: 20px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  overflow: hidden;
}

.gallery-card {
  padding: 20px;
  height: 100%;
}

.main-image {
  width: 100%;
  height: 650px;
  object-fit: cover;
  display: block;
  border-radius: 16px;
  background: #f1f3f5;
}

.info-card {
  padding: 28px;
  height: 100%;
}

.product-brand {
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #6b7280;
  margin-bottom: 10px;
}

.product-title {
  font-size: 34px;
  line-height: 1.2;
  font-weight: 800;
  color: #111827;
  margin-bottom: 10px;
}

.product-code {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 18px;
}

.price-wrap {
  margin-bottom: 14px;
}

.product-price {
  font-size: 32px;
  line-height: 1;
  font-weight: 800;
  color: #c1121f;
}

.stock-line {
  margin-bottom: 18px;
}

.stock-badge {
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border-radius: 999px;
  background: #ecfdf3;
  color: #027a48;
  font-size: 13px;
  font-weight: 700;
}

.stock-badge.out {
  background: #fef3f2;
  color: #b42318;
}

.desc-box {
  margin-bottom: 22px;
  font-size: 15px;
  line-height: 1.7;
  color: #4b5563;
  padding: 16px 18px;
  background: #f9fafb;
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 14px;
}

.variant-section {
  margin-bottom: 22px;
}

.variant-label {
  font-size: 14px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 10px;
}

.variant-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.variant-btn {
  min-width: 72px;
  padding: 10px 16px;
  border-radius: 12px;
  border: 1px solid #d1d5db;
  background: #fff;
  color: #111827;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.2s ease;
}

.variant-btn:hover {
  border-color: #111827;
}

.variant-btn.active {
  background: #111827;
  color: #fff;
  border-color: #111827;
  box-shadow: 0 8px 20px rgba(17, 24, 39, 0.12);
}

.size-btn {
  min-width: 58px;
  text-align: center;
}

.qty-section {
  margin-bottom: 22px;
}

.qty-wrap {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-radius: 14px;
  background: #f9fafb;
  border: 1px solid rgba(15, 23, 42, 0.08);
}

.qty-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 10px;
  background: #111827;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  transition: opacity 0.2s ease;
}

.qty-btn:hover {
  opacity: 0.9;
}

.qty-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.qty-input {
  width: 74px;
  height: 40px;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  text-align: center;
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  background: #fff;
}

.warning-box {
  margin-bottom: 18px;
  padding: 14px 16px;
  border-radius: 12px;
  background: #fef3f2;
  border: 1px solid #fecdca;
  color: #b42318;
  font-size: 14px;
  font-weight: 600;
}

.action-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 24px;
}

.btn-add-cart,
.btn-view-cart {
  min-width: 180px;
  height: 48px;
  padding: 0 20px;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  font-size: 15px;
  font-weight: 700;
  transition: all 0.2s ease;
}

.btn-add-cart {
  border: none;
  background: #0f172a;
  color: #fff;
}

.btn-add-cart:hover {
  opacity: 0.95;
  transform: translateY(-1px);
}

.btn-add-cart:disabled {
  background: #9ca3af;
  cursor: not-allowed;
  transform: none;
}

.btn-view-cart {
  border: 1px solid #cbd5e1;
  background: #fff;
  color: #111827;
}

.btn-view-cart:hover {
  border-color: #111827;
  color: #111827;
}

.extra-info {
  display: grid;
  gap: 10px;
  padding-top: 18px;
  border-top: 1px dashed rgba(15, 23, 42, 0.12);
}

.extra-item {
  font-size: 14px;
  color: #4b5563;
  position: relative;
  padding-left: 18px;
}

.extra-item::before {
  content: "";
  position: absolute;
  left: 0;
  top: 8px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #0f172a;
}

@media (max-width: 991.98px) {
  .main-image {
    height: 520px;
  }

  .product-title {
    font-size: 28px;
  }

  .product-price {
    font-size: 28px;
  }

  .info-card {
    padding: 22px;
  }
}

@media (max-width: 767.98px) {
  .main-image {
    height: 420px;
  }

  .product-title {
    font-size: 24px;
  }

  .product-price {
    font-size: 24px;
  }

  .action-wrap {
    flex-direction: column;
  }

  .btn-add-cart,
  .btn-view-cart {
    width: 100%;
  }
}

@media (max-width: 575.98px) {
  .product-detail-page {
    padding-bottom: 24px;
  }

  .main-image {
    height: 320px;
  }

  .gallery-card,
  .info-card {
    border-radius: 16px;
  }

  .gallery-card {
    padding: 14px;
  }

  .info-card {
    padding: 18px;
  }

  .product-title {
    font-size: 22px;
  }

  .product-price {
    font-size: 22px;
  }

  .variant-btn {
    min-width: auto;
    padding: 9px 14px;
    font-size: 13px;
  }

  .qty-input {
    width: 60px;
  }
}
</style>
