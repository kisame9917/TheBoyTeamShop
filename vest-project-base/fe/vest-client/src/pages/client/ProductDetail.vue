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
              <div class="product-brand">The Boy Team</div>

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
                <div class="price-note">Giá bán lẻ tại cửa hàng</div>
              </div>

              <!-- <div class="stock-line" v-if="selectedVariant">
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
              </div> -->

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
      class="variant-btn color-swatch-btn"
      :class="{ active: selectedColor === color }"
      :title="color"
      @click="selectColor(color)"
      :style="{ backgroundColor: getColorCode(color) }"
    ></button>
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
                <div class="extra-item">
                  <i class="bi bi-check-circle-fill"></i>
                  <span>Miễn phí tư vấn chọn size phù hợp</span>
                </div>
                <div class="extra-item">
                  <i class="bi bi-truck"></i>
                  <span>Giao hàng toàn quốc</span>
                </div>
                <div class="extra-item">
                  <i class="bi bi-credit-card-2-front"></i>
                  <span>Hỗ trợ COD và QR</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Phần bổ sung bên dưới -->
        <div class="detail-extra-sections mt-4">
          <div class="row g-4">
            <div class="col-lg-8">
              <div class="detail-block">
                <div class="detail-block__header">
                  <span>Thông tin sản phẩm</span>
                </div>

                <div class="detail-block__body">
                  <div class="detail-description" v-if="productDescription">
                    {{ productDescription }}
                  </div>
                  <div class="detail-description" v-else>
                    Sản phẩm được thiết kế theo phong cách lịch lãm, phù hợp cho
                    môi trường công sở, sự kiện, cưới hỏi và các dịp cần hình ảnh
                    chỉn chu, sang trọng.
                  </div>

                  <div class="spec-grid">
                    <div class="spec-item">
                      <span class="spec-label">Tên sản phẩm</span>
                      <strong>{{ productTitle || "Đang cập nhật" }}</strong>
                    </div>

                    <div class="spec-item">
                      <span class="spec-label">Màu đang chọn</span>
                      <strong>{{ selectedColor || "Chưa chọn" }}</strong>
                    </div>

                    <div class="spec-item">
                      <span class="spec-label">Kích thước</span>
                      <strong>{{ selectedSize || "Chưa chọn" }}</strong>
                    </div>

                    <div class="spec-item">
                      <span class="spec-label">Mã biến thể</span>
                      <strong>{{ selectedVariant?.code || "Đang cập nhật" }}</strong>
                    </div>
                  </div>
                </div>
              </div>

              <div class="detail-block mt-4">
                <div class="detail-block__header">
                  <span>Dịch vụ tại The Boy Team</span>
                </div>

                <div class="detail-block__body">
                  <div class="service-grid">
                    <div class="service-card">
                      <div class="service-icon">
                        <i class="bi bi-patch-check-fill"></i>
                      </div>
                      <div>
                        <div class="service-title">Sản phẩm chỉn chu</div>
                        <div class="service-text">
                          Hoàn thiện form dáng lịch lãm, phù hợp phong cách vest nam hiện đại.
                        </div>
                      </div>
                    </div>

                    <div class="service-card">
                      <div class="service-icon">
                        <i class="bi bi-rulers"></i>
                      </div>
                      <div>
                        <div class="service-title">Tư vấn size nhanh</div>
                        <div class="service-text">
                          Hỗ trợ chọn màu sắc và kích cỡ phù hợp với vóc dáng khách hàng.
                        </div>
                      </div>
                    </div>

                    <div class="service-card">
                      <div class="service-icon">
                        <i class="bi bi-box-seam"></i>
                      </div>
                      <div>
                        <div class="service-title">Đóng gói cẩn thận</div>
                        <div class="service-text">
                          Sản phẩm được kiểm tra và đóng gói kỹ trước khi giao hàng.
                        </div>
                      </div>
                    </div>

                    <div class="service-card">
                      <div class="service-icon">
                        <i class="bi bi-arrow-repeat"></i>
                      </div>
                      <div>
                        <div class="service-title">Hỗ trợ đổi trả</div>
                        <div class="service-text">
                          Hỗ trợ theo chính sách của cửa hàng với quy trình rõ ràng, thuận tiện.
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="col-lg-4">
              <div class="detail-block detail-highlight">
                <div class="detail-block__header">
                  <span>Vì sao nên chọn chúng tôi</span>
                </div>

                <div class="detail-block__body">
                  <div class="highlight-list">
                    <div class="highlight-item">
                      <i class="bi bi-stars"></i>
                      <span>Phong cách nam tính, lịch lãm, dễ phối.</span>
                    </div>
                    <div class="highlight-item">
                      <i class="bi bi-shield-check"></i>
                      <span>Thông tin rõ ràng theo màu và size.</span>
                    </div>
                    <div class="highlight-item">
                      <i class="bi bi-wallet2"></i>
                      <span>Mức giá hiển thị minh bạch, dễ theo dõi.</span>
                    </div>
                    <div class="highlight-item">
                      <i class="bi bi-headset"></i>
                      <span>Hỗ trợ nhanh qua hotline và mạng xã hội.</span>
                    </div>
                  </div>
                </div>
              </div>

              <div class="detail-block mt-4">
                <div class="detail-block__header">
                  <span>Lưu ý khi đặt hàng</span>
                </div>

                <div class="detail-block__body">
                  <ul class="note-list mb-0">
                    <li>Chọn đúng màu sắc và kích thước trước khi thêm vào giỏ.</li>
                    <li>Kiểm tra tồn kho hiển thị trước khi tăng số lượng.</li>
                    <li>Đơn hàng online hỗ trợ COD và thanh toán QR.</li>
                    <li>Liên hệ cửa hàng nếu cần hỗ trợ chọn form vest phù hợp.</li>
                  </ul>
                </div>
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
import { pickProductImage, pickVariantImage, resolveMediaUrl } from "../../utils/media";

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
  return resolveMediaUrl(value) || fallbackImage;
}
function normalizeColorName(name) {
  return String(name || "")
    .trim()
    .toLowerCase()
    .replace(/đ/g, "d")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/\(.*?\)/g, "")
    .replace(/\s+/g, " ")
    .trim();
}

const COLOR_MAP = {
  den: "#111827",
  trang: "#ffffff",
  xam: "#9ca3af",
  ghi: "#9ca3af",
  do: "#ef4444",
  vang: "#f59e0b",
  cam: "#f97316",
  hong: "#ec4899",
  tim: "#a855f7",
  nau: "#92400e",
  be: "#f5f5dc",
  kem: "#fff7ed",
  "xanh la": "#22c55e",
  "xanh luc": "#16a34a",
  "xanh ngoc": "#14b8a6",
  "xanh duong": "#3b82f6",
  "xanh navy": "#1e3a8a",
  "xanh than": "#1e3a8a",
  navy: "#1e3a8a",
  cyan: "#06b6d4",
};

function getColorCode(colorName) {
  if (!colorName) return "#d1d5db";
  const key = normalizeColorName(colorName);
  if (COLOR_MAP[key]) return COLOR_MAP[key];
  if (key.includes("navy") || key.includes("than")) return COLOR_MAP["xanh navy"];
  if (key.includes("xanh") && key.includes("la")) return COLOR_MAP["xanh la"];
  if (key.includes("xanh") && key.includes("duong")) return COLOR_MAP["xanh duong"];
  if (key.includes("do")) return COLOR_MAP.do;
  if (key.includes("vang")) return COLOR_MAP.vang;
  if (key.includes("cam")) return COLOR_MAP.cam;
  if (key.includes("hong")) return COLOR_MAP.hong;
  if (key.includes("tim")) return COLOR_MAP.tim;
  if (key.includes("nau")) return COLOR_MAP.nau;
  if (key.includes("trang")) return COLOR_MAP.trang;
  if (key.includes("den")) return COLOR_MAP.den;
  return "#3b82f6";
}

function normalizeSizeName(raw) {
  if (!raw) return "";
  if (typeof raw === "string") return raw;

  return raw.tenKichCo || raw.kichCo || raw.size || raw.name || raw.value || "";
}
function mapVariant(v) {
  const colorName =
    (typeof v.mauSac === "object"
      ? v.mauSac?.ten || v.mauSac?.name || ""
      : v.mauSac) ||
    v.tenMauSac ||
    v.mau ||
    "";

  const sizeName =
    normalizeSizeName(v.kichCo) ||
    normalizeSizeName(v.tenKichCo) ||
    normalizeSizeName(v.size) ||
    "";

  const image =
    pickVariantImage(v) ||
    pickProductImage(product.value, [], fallbackImage) ||
    fallbackImage;

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
  return pickProductImage(product.value, normalizedVariants.value, fallbackImage);
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

  quantity.value = 1;
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
  background:
      linear-gradient(180deg, #f5f7fc 0%, #f3f4f8 100%);
  min-height: 100vh;
}

.state-box {
  padding: 24px;
  border-radius: 18px;
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  box-shadow: 0 12px 28px rgba(7, 20, 69, 0.06);
}

.product-detail-card {
  background: transparent;
}

.gallery-card,
.info-card,
.detail-block {
  background: #fff;
  border-radius: 22px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 16px 36px rgba(10, 24, 74, 0.06);
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
  border-radius: 18px;
  background: #f1f3f5;
}

.info-card {
  padding: 30px;
  height: 100%;
}

.product-brand {
  font-size: 13px;
  font-weight: 750;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #6b7280;
  margin-bottom: 10px;
}

.product-title {
  font-size: 34px;
  line-height: 1.2;
  font-weight: 750;
  color: #0f172a;
  margin-bottom: 10px;
}

.product-code {
  display: inline-flex;
  align-items: center;
  padding: 7px 12px;
  border-radius: 999px;
  background: #eef2ff;
  color: #1e3a8a;
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 16px;
}

.price-wrap {
  margin-bottom: 16px;
  padding: 18px 20px;
  border-radius: 18px;
  background: linear-gradient(135deg, #f8fbff 0%, #eef4ff 100%);
  border: 1px solid rgba(0, 15, 81, 0.08);
}

.product-price {
  font-size: 34px;
  font-weight: 750;
  color: #000f51;
  line-height: 1.1;
}

.price-note {
  margin-top: 6px;
  color: #64748b;
  font-size: 14px;
}

.stock-line {
  margin-bottom: 16px;
}

.stock-badge {
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border-radius: 999px;
  background: #e7f8ee;
  color: #15803d;
  font-weight: 700;
  font-size: 14px;
}

.stock-badge.out {
  background: #fee2e2;
  color: #b91c1c;
}

.desc-box {
  padding: 16px 18px;
  border-radius: 16px;
  background: #f8fafc;
  color: #475569;
  line-height: 1.7;
  margin-bottom: 22px;
  border: 1px solid rgba(148, 163, 184, 0.16);
}

.variant-section + .variant-section,
.qty-section {
  margin-top: 20px;
}

.variant-label {
  font-size: 15px;
  font-weight: 750;
  color: #0f172a;
  margin-bottom: 12px;
}

.variant-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.variant-btn {
  min-width: 96px;
  padding: 12px 18px;
  border-radius: 14px;
  border: 1px solid #d7dbe7;
  background: #fff;
  color: #0f172a;
  font-weight: 700;
  transition: all 0.2s ease;
}

.variant-btn:hover {
  border-color: #001a72;
  color: #001a72;
  transform: translateY(-1px);
}

.variant-btn.active {
  background: #000f51;
  color: #fff;
  border-color: #000f51;
  box-shadow: 0 10px 20px rgba(0, 15, 81, 0.18);
}

.size-btn {
  min-width: 72px;
}

.qty-wrap {
  display: inline-flex;
  align-items: center;
  border: 1px solid #d9deea;
  border-radius: 16px;
  padding: 6px;
  background: #f8fafc;
  gap: 6px;
}

.qty-btn {
  width: 44px;
  height: 44px;
  border: none;
  border-radius: 12px;
  background: #e9edf7;
  color: #0f172a;
  font-size: 22px;
  font-weight: 750;
  transition: all 0.2s ease;
}

.qty-btn:hover:not(:disabled) {
  background: #000f51;
  color: #fff;
}

.qty-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.qty-input {
  width: 68px;
  height: 44px;
  border: none;
  background: #fff;
  border-radius: 12px;
  text-align: center;
  font-weight: 750;
  color: #0f172a;
  outline: none;
}

.warning-box {
  margin-top: 18px;
  padding: 14px 16px;
  border-radius: 14px;
  background: #fff7ed;
  color: #c2410c;
  border: 1px solid #fed7aa;
  font-weight: 600;
}

.action-wrap {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  flex-wrap: wrap;
}

.btn-add-cart,
.btn-view-cart {
  min-height: 50px;
  padding: 0 24px;
  border-radius: 16px;
  font-weight: 750;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.btn-add-cart:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.extra-info {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px dashed rgba(100, 116, 139, 0.35);
  display: grid;
  gap: 12px;
}

.extra-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #475569;
  font-weight: 600;
}
.btn-add-cart {
  border: none;
  background: #000f51;
  color: #fff;
  box-shadow: 0 14px 28px rgba(0, 15, 81, 0.2);
}

.btn-add-cart:hover:not(:disabled) {
  background: #001a72;
  transform: translateY(-1px);
}

.btn-view-cart {
  background: #000f51;
  color: #fff;
  border: 1px solid #000f51;
  box-shadow: 0 14px 28px rgba(0, 15, 81, 0.18);
}

.btn-view-cart:hover {
  background: #001a72;
  border-color: #001a72;
  color: #fff;
}
.extra-item i {
  color: #000f51;
  font-size: 16px;
}

.detail-extra-sections {
  margin-top: 28px;
}

.detail-block__header {
  padding: 16px 20px;
  background: linear-gradient(90deg, #000f51 0%, #12348f 100%);
  color: #fff;
  font-weight: 750;
  font-size: 16px;
}

.detail-block__body {
  padding: 22px 20px;
}

.detail-description {
  color: #475569;
  line-height: 1.8;
  margin-bottom: 18px;
}

.spec-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.spec-item {
  padding: 16px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid rgba(148, 163, 184, 0.16);
}

.spec-label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  color: #64748b;
}

.spec-item strong {
  color: #0f172a;
  font-size: 15px;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.service-card {
  display: flex;
  gap: 14px;
  padding: 18px;
  border-radius: 18px;
  background: #f8fafc;
  border: 1px solid rgba(148, 163, 184, 0.16);
}

.service-icon {
  flex: 0 0 44px;
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: #eaf0ff;
  color: #000f51;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.service-title {
  font-weight: 750;
  color: #0f172a;
  margin-bottom: 4px;
}

.service-text {
  color: #64748b;
  line-height: 1.65;
  font-size: 14px;
}

.detail-highlight {
  position: sticky;
  top: 90px;
}

.highlight-list {
  display: grid;
  gap: 14px;
}

.highlight-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 14px 16px;
  border-radius: 16px;
  background: #f8fafc;
  color: #334155;
  border: 1px solid rgba(148, 163, 184, 0.16);
}

.highlight-item i {
  color: #000f51;
  font-size: 16px;
  margin-top: 2px;
}

.note-list {
  padding-left: 18px;
  color: #475569;
  line-height: 1.8;
}

.note-list li + li {
  margin-top: 8px;
}

@media (max-width: 991.98px) {
  .main-image {
    height: 460px;
  }

  .detail-highlight {
    position: static;
  }
}


.color-swatch-btn {
  width: 38px;
  height: 38px;
  min-width: 38px;
  padding: 0;
  border-radius: 999px;
  border: 1px solid #d1d5db;
  box-shadow: inset 0 0 0 1px rgba(255,255,255,0.25);
}

.color-swatch-btn.active {
  outline: 2px solid #000f51;
  outline-offset: 2px;
}
@media (max-width: 767.98px) {
  .product-title {
    font-size: 28px;
  }

  .product-price {
    font-size: 28px;
  }

  .service-grid,
  .spec-grid {
    grid-template-columns: 1fr;
  }

  .action-wrap {
    flex-direction: column;
  }

  .btn-add-cart,
  .btn-view-cart {
    width: 100%;
  }
}
</style>