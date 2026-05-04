<template>
  <div class="product-detail-page">
    <section class="detail-hero">
      <div class="container">
      

        <div class="hero-title-box">
          <div>
            <div class="hero-kicker">The Boy Team</div>
            <h1>Chi tiết sản phẩm</h1>
            <p>
              Kiểm tra đầy đủ màu sắc, kích thước, tồn kho và giá bán trước khi
              thêm sản phẩm vào giỏ hàng.
            </p>
          </div>

          <router-link to="/search" class="back-shop-btn">
            <i class="bi bi-arrow-left"></i>
            Quay lại sản phẩm
          </router-link>
        </div>
      </div>
    </section>

    <main class="container detail-main">
      <div v-if="loading" class="state-box">
        <div class="state-spinner"></div>
        <span>Đang tải chi tiết sản phẩm...</span>
      </div>

      <div v-else-if="loadError" class="state-box state-box-error">
        <i class="bi bi-exclamation-triangle"></i>
        <span>{{ loadError }}</span>
      </div>

      <div v-else-if="product" class="product-detail-card">
        <div class="row g-4 align-items-stretch">
          <div class="col-lg-6">
            <section class="gallery-card">
              <div class="main-image-wrap">
                <img
                    :src="currentImage"
                    :alt="productTitle"
                    class="main-image"
                    @error="onImgError"
                />
              </div>

              <div v-if="galleryImages.length > 1" class="thumb-list">
                <button
                    v-for="img in galleryImages"
                    :key="img"
                    type="button"
                    class="thumb-btn"
                    :class="{ active: currentImage === img }"
                    @click="manualImage = img"
                >
                  <img :src="img" :alt="productTitle" @error="onThumbError" />
                </button>
              </div>

              <div class="gallery-extra">
                <div class="gallery-extra__grid">
                  <div class="gallery-extra__item">
                    <span>Màu sắc</span>
                    <strong>{{ availableColors.length }}</strong>
                  </div>

                  <div class="gallery-extra__item">
                    <span>Kích thước</span>
                    <strong>{{ availableSizes.length }}</strong>
                  </div>

                  <div class="gallery-extra__item">
                    <span>Mẫu còn hàng</span>
                    <strong>{{ availableVariants.length }}</strong>
                  </div>

                  <div class="gallery-extra__item">
                    <span>Tổng số lượng</span>
                    <strong>{{ totalAvailableStock }}</strong>
                  </div>
                </div>

                <div class="gallery-extra__note">
                  <div class="gallery-extra__note-icon">
                    <i class="bi bi-patch-check"></i>
                  </div>

                  <div class="gallery-extra__note-content">
                    <div class="gallery-extra__note-title">
                      Thông tin sản phẩm
                    </div>
                    <div class="gallery-extra__note-text">
                      Hình ảnh, màu sắc, kích thước và số lượng được cập nhật
                      theo từng mẫu sản phẩm còn hàng.
                    </div>
                  </div>
                </div>
              </div>
            </section>
          </div>

          <div class="col-lg-6">
            <section class="info-card">
              <div class="product-brand">The Boy Team</div>

              <h1 class="product-title">
                {{ productTitle }}
              </h1>

              <div class="code-row">
  <span
      class="total-stock-badge"
      :class="{ out: selectedVariantStock <= 0 }"
  >
    {{
      selectedVariantStock > 0
          ? `${selectedVariantStock} sản phẩm còn hàng`
          : "Hết hàng"
    }}
  </span>
</div>

              <div class="price-wrap">
                <div>
                  <div class="price-label">Giá bán</div>
                  <div class="product-price">
                    {{ formatCurrency(displayPrice) }}
                  </div>
                </div>
              </div>

              <div class="selected-summary">
                <div>
                  <span>Màu đang chọn</span>
                  <strong>{{ selectedColorName || "Chưa chọn" }}</strong>
                </div>

                <div>
                  <span>Kích thước</span>
                  <strong>{{ selectedSize || "Chưa chọn" }}</strong>
                </div>

                <div>
                  <span>Số lượng còn</span>
                  <strong>{{ selectedVariant?.stock ?? 0 }}</strong>
                </div>
              </div>

              <div v-if="productDescription" class="desc-box">
                {{ productDescription }}
              </div>

              <div class="variant-section" v-if="availableColors.length">
                <div class="variant-header">
                  <div>
                    <div class="variant-label">Màu sắc</div>
                  </div>
                </div>

                <div class="variant-options">
                  <button
                      v-for="color in availableColors"
                      :key="color.key"
                      type="button"
                      class="color-swatch-btn"
                      :class="{
                      active: String(selectedColor) === String(color.key),
                    }"
                      :title="color.name"
                      @click="selectColor(color.key)"
                  >
                    <span :style="{ backgroundColor: color.code }"></span>
                  </button>
                </div>
              </div>

              <div class="variant-section" v-if="availableSizes.length">
                <div class="variant-header">
                  <div>
                    <div class="variant-label">Kích thước</div>
                  </div>
                </div>

                <div class="variant-options">
                  <button
                      v-for="size in availableSizes"
                      :key="size"
                      type="button"
                      class="size-btn"
                      :class="{ active: selectedSize === size }"
                      @click="selectSize(size)"
                  >
                    {{ size }}
                  </button>
                </div>
              </div>

              <div class="qty-section">
                <div class="variant-header">
                  <div>
                    <div class="variant-label">Số lượng</div>
                  </div>
                </div>

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
                <i class="bi bi-exclamation-circle"></i>
                <span>{{ warningMessage }}</span>
              </div>

              <div class="action-wrap">
                <button
                    type="button"
                    class="btn-add-cart"
                    :disabled="!canAddToCart"
                    @click="handleAddToCart"
                >
                  <i class="bi bi-cart-plus"></i>
                  Thêm vào giỏ hàng
                </button>

                <router-link to="/cart" class="btn-view-cart">
                  <i class="bi bi-bag-check"></i>
                  Xem giỏ hàng
                </router-link>
              </div>

              <div class="extra-info">
                <div class="extra-item">
                  <i class="bi bi-shield-check"></i>
                  <span>Thanh toán an toàn</span>
                </div>

                <div class="extra-item">
                  <i class="bi bi-truck"></i>
                  <span>Giao hàng toàn quốc</span>
                </div>

                <div class="extra-item">
                  <i class="bi bi-arrow-repeat"></i>
                  <span>Hỗ trợ đổi trả theo chính sách</span>
                </div>
              </div>
            </section>
          </div>
        </div>

        <section class="detail-extra-sections">
          <div class="row g-4">
            <div class="col-lg-8">
              <div class="detail-block">
                <div class="detail-block__header">
                  <span>Thông tin sản phẩm</span>
                </div>

                <div class="detail-block__body">
                  <div class="detail-description">
                    {{ productDescription || defaultDescription }}
                  </div>

                  <div class="spec-grid">
                    <div class="spec-item">
                      <span class="spec-label">Tên sản phẩm</span>
                      <strong>{{ productTitle || "Đang cập nhật" }}</strong>
                    </div>

                    <div class="spec-item">
                      <span class="spec-label">Mã sản phẩm</span>
                      <strong>{{ productCode || "Đang cập nhật" }}</strong>
                    </div>

                    <div class="spec-item">
                      <span class="spec-label">Màu đang chọn</span>
                      <strong>{{ selectedColorName || "Chưa chọn" }}</strong>
                    </div>

                    <div class="spec-item">
                      <span class="spec-label">Kích thước</span>
                      <strong>{{ selectedSize || "Chưa chọn" }}</strong>
                    </div>

                    <div class="spec-item">
                      <span class="spec-label">Mã sản phẩm chi tiết</span>
                      <strong>{{
                          selectedVariant?.code || "Đang cập nhật"
                        }}</strong>
                    </div>

                    <div class="spec-item">
                      <span class="spec-label">Tồn kho sản phẩm chi tiết</span>
                      <strong>{{ selectedVariant?.stock ?? 0 }}</strong>
                    </div>
                  </div>
                </div>
              </div>

              <div class="detail-block mt-4">
                <div class="detail-block__header">
                  <span>Các mẫu sản phẩm</span>
                </div>

                <div class="detail-block__body">
                  <div
                      v-if="availableVariants.length"
                      class="variant-table-wrap"
                  >
                    <table class="variant-table">
                      <thead>
                      <tr>
                        <th>Màu sắc</th>
                        <th>Size</th>
                        <th>Giá</th>
                        <th>Số lượng</th>
                        <th>Mã SPCT</th>
                      </tr>
                      </thead>

                      <tbody>
                      <tr
                          v-for="item in availableVariants"
                          :key="item.idSanPhamChiTiet"
                      >
                        <td>
                            <span class="table-color">
                              <i
                                  :style="{
                                  backgroundColor: getColorCode(
                                    item.colorKey,
                                    item.color,
                                  ),
                                }"
                              ></i>
                              {{ item.color || "Đang cập nhật" }}
                            </span>
                        </td>

                        <td>{{ item.size || "Đang cập nhật" }}</td>
                        <td>{{ formatCurrency(item.price) }}</td>
                        <td>{{ item.stock }}</td>
                        <td>{{ item.code || "Đang cập nhật" }}</td>
                      </tr>
                      </tbody>
                    </table>
                  </div>

                  <div v-else class="empty-variant">
                    Sản phẩm hiện chưa có biến thể còn hàng.
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
                          Hoàn thiện form dáng lịch lãm, phù hợp phong cách vest
                          nam hiện đại.
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
                          Hỗ trợ chọn màu sắc và kích cỡ phù hợp với vóc dáng
                          khách hàng.
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
                          Sản phẩm được kiểm tra và đóng gói kỹ trước khi giao
                          hàng.
                        </div>
                      </div>
                    </div>

                    <div class="service-card">
                      <div class="service-icon">
                        <i class="bi bi-headset"></i>
                      </div>
                      <div>
                        <div class="service-title">Hỗ trợ nhanh</div>
                        <div class="service-text">
                          Tư vấn qua hotline, chat hỗ trợ và các kênh mạng xã
                          hội của cửa hàng.
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
                  <span>Thông tin mua hàng</span>
                </div>

                <div class="detail-block__body">
                  <div class="highlight-list">
                    <div class="highlight-item">
                      <i class="bi bi-stars"></i>
                      <span>Phong cách nam tính, lịch lãm, dễ phối đồ.</span>
                    </div>

                    <div class="highlight-item">
                      <i class="bi bi-shield-check"></i>
                      <span>Thông tin rõ ràng theo màu, size và tồn kho.</span>
                    </div>

                    <div class="highlight-item">
                      <i class="bi bi-wallet2"></i>
                      <span
                      >Mức giá hiển thị minh bạch theo từng biến thể.</span
                      >
                    </div>

                    <div class="highlight-item">
                      <i class="bi bi-box-seam"></i>
                      <span>Đóng gói cẩn thận trước khi giao hàng.</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>

      <div v-else class="state-box">Không tìm thấy sản phẩm.</div>
    </main>

    <ChatWidget />
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import ChatWidget from "../../components/ClientChatWidget.vue";
import { useCart } from "../../composables/useCart";
import {
  getMauSacList,
  getProductById,
  getProductVariantsByProductId,
} from "../../services/productClientApi";
import {
  pickProductImage,
  pickVariantImage,
  resolveMediaUrl,
} from "../../utils/media";

const route = useRoute();
const { addToCart } = useCart();

const fallbackImage =
    "data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='700' height='820'%3E%3Crect width='100%25' height='100%25' fill='%23f1f3f5'/%3E%3Ctext x='50%25' y='52%25' dominant-baseline='middle' text-anchor='middle' fill='%2399a1aa' font-size='30'%3ENo Image%3C/text%3E%3C/svg%3E";

const FALLBACK_COLOR_MAP = {
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

const loading = ref(false);
const loadError = ref("");
const product = ref(null);
const variants = ref([]);
const dbColors = ref([]);

const selectedColor = ref("");
const selectedSize = ref("");
const quantity = ref(1);
const manualImage = ref("");

const productId = computed(() => route.params.id);
const selectedVariantId = computed(() => {
  const id = Number(route.query.variantId);
  return Number.isFinite(id) && id > 0 ? id : null;
});

const productTitle = computed(() => {
  if (!product.value) return "";

  return (
      product.value.tenSanPham ||
      product.value.title ||
      product.value.name ||
      "Sản phẩm"
  );
});

const productCode = computed(() => {
  return product.value?.maSanPham || product.value?.code || "";
});

const productDescription = computed(() => {
  if (!product.value) return "";
  return product.value.moTa || product.value.description || "";
});

const defaultDescription =
    "Sản phẩm được thiết kế theo phong cách lịch lãm, phù hợp cho môi trường công sở, sự kiện, cưới hỏi và các dịp cần hình ảnh chỉn chu, sang trọng.";

const normalizedVariants = computed(() => {
  return (variants.value || []).map(mapVariant);
});

const availableVariants = computed(() => {
  return normalizedVariants.value.filter((item) => Number(item.stock || 0) > 0);
});

const availableColors = computed(() => {
  const map = new Map();

  availableVariants.value.forEach((v) => {
    if (!v.colorKey || !v.color) return;

    if (!map.has(v.colorKey)) {
      map.set(v.colorKey, {
        key: v.colorKey,
        id: v.colorId,
        name: v.color,
        code: getColorCode(v.colorKey, v.color),
      });
    }
  });

  return [...map.values()];
});

const selectedColorName = computed(() => {
  if (!selectedColor.value) return "";

  const found = availableColors.value.find(
      (item) => String(item.key) === String(selectedColor.value),
  );

  return found?.name || "";
});

const filteredSizesByColor = computed(() => {
  if (!selectedColor.value) return availableVariants.value;

  return availableVariants.value.filter(
      (v) => String(v.colorKey) === String(selectedColor.value),
  );
});

const availableSizes = computed(() => {
  const sizes = filteredSizesByColor.value.map((v) => v.size).filter(Boolean);
  return [...new Set(sizes)].sort(sortSize);
});

const selectedVariant = computed(() => {
  return (
      availableVariants.value.find((v) => {
        const matchColor = selectedColor.value
            ? String(v.colorKey) === String(selectedColor.value)
            : true;

        const matchSize = selectedSize.value
            ? String(v.size) === String(selectedSize.value)
            : true;

        return matchColor && matchSize;
      }) || null
  );
});

const displayPrice = computed(() => {
  if (selectedVariant.value?.price) return selectedVariant.value.price;

  const firstPrice = availableVariants.value[0]?.price;
  if (firstPrice) return firstPrice;

  return Number(product.value?.giaBan || product.value?.price || 0);
});

const currentImage = computed(() => {
  if (manualImage.value) return manualImage.value;
  if (selectedVariant.value?.image) return selectedVariant.value.image;

  return pickProductImage(
      product.value,
      normalizedVariants.value,
      fallbackImage,
  );
});

const galleryImages = computed(() => {
  const list = [];

  const productImage = pickProductImage(
      product.value,
      normalizedVariants.value,
      "",
  );

  if (productImage) list.push(productImage);

  normalizedVariants.value.forEach((item) => {
    if (item.image && !list.includes(item.image)) {
      list.push(item.image);
    }
  });

  return list.length ? list : [fallbackImage];
});

const maxQty = computed(() => {
  return selectedVariant.value?.stock > 0 ? selectedVariant.value.stock : 1;
});

const totalAvailableStock = computed(() => {
  return availableVariants.value.reduce((sum, item) => {
    return sum + Number(item.stock || 0);
  }, 0);
});
const selectedVariantStock = computed(() => {
  return Number(selectedVariant.value?.stock || 0);
});
const warningMessage = computed(() => {
  if (!normalizedVariants.value.length) {
    return "Sản phẩm hiện chưa có biến thể khả dụng.";
  }

  if (!availableVariants.value.length) {
    return "Sản phẩm hiện đã hết hàng.";
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

watch(selectedColor, () => {
  manualImage.value = "";
});

watch(selectedSize, () => {
  manualImage.value = "";
});

watch(quantity, (val) => {
  if (!val || val < 1) {
    quantity.value = 1;
    return;
  }

  if (selectedVariant.value?.stock && val > selectedVariant.value.stock) {
    quantity.value = selectedVariant.value.stock;
  }
});

function normalizeText(text) {
  return String(text || "")
      .trim()
      .toLowerCase()
      .replace(/đ/g, "d")
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .replace(/\s+/g, " ");
}

function normalizeColorName(name) {
  return normalizeText(name)
      .replace(/\(.*?\)/g, "")
      .trim();
}

function normalizeColorCode(value) {
  const raw = String(value || "").trim();

  if (!raw) return "";
  if (raw.startsWith("#")) return raw;
  if (/^[0-9a-fA-F]{3}$/.test(raw) || /^[0-9a-fA-F]{6}$/.test(raw)) {
    return `#${raw}`;
  }

  if (/^(rgb|rgba|hsl|hsla)\(/i.test(raw)) return raw;

  return "";
}

function colorByName(colorName = "") {
  const key = normalizeColorName(colorName);

  if (FALLBACK_COLOR_MAP[key]) return FALLBACK_COLOR_MAP[key];

  if (key.includes("navy") || key.includes("than")) {
    return FALLBACK_COLOR_MAP["xanh navy"];
  }

  if (key.includes("xanh") && key.includes("la")) {
    return FALLBACK_COLOR_MAP["xanh la"];
  }

  if (key.includes("xanh") && key.includes("duong")) {
    return FALLBACK_COLOR_MAP["xanh duong"];
  }

  if (key.includes("xanh")) return FALLBACK_COLOR_MAP["xanh duong"];
  if (key.includes("den")) return FALLBACK_COLOR_MAP.den;
  if (key.includes("trang")) return FALLBACK_COLOR_MAP.trang;
  if (key.includes("xam") || key.includes("ghi")) return FALLBACK_COLOR_MAP.xam;
  if (key.includes("do")) return FALLBACK_COLOR_MAP.do;
  if (key.includes("vang")) return FALLBACK_COLOR_MAP.vang;
  if (key.includes("cam")) return FALLBACK_COLOR_MAP.cam;
  if (key.includes("hong")) return FALLBACK_COLOR_MAP.hong;
  if (key.includes("tim")) return FALLBACK_COLOR_MAP.tim;
  if (key.includes("nau")) return FALLBACK_COLOR_MAP.nau;
  if (key.includes("be") || key.includes("kem")) return FALLBACK_COLOR_MAP.be;

  return "#d1d5db";
}

function resolveColorCode(value, colorName = "") {
  return normalizeColorCode(value) || colorByName(colorName);
}

function findDbColor(colorId, colorName = "") {
  const byId = dbColors.value.find(
      (item) => String(item.id) === String(colorId),
  );

  if (byId) return byId;

  const normalizedName = normalizeColorName(colorName);

  return dbColors.value.find((item) => {
    return normalizeColorName(item.ten || item.name || "") === normalizedName;
  });
}

function getColorCode(colorKey, colorName = "") {
  const dbColor = findDbColor(colorKey, colorName);

  return resolveColorCode(
      dbColor?.ma || dbColor?.code || dbColor?.maMau || dbColor?.colorCode || "",
      dbColor?.ten || dbColor?.name || colorName,
  );
}

function normalizeSizeName(raw) {
  if (!raw) return "";
  if (typeof raw === "string") return raw;

  return raw.tenKichCo || raw.kichCo || raw.size || raw.name || raw.value || "";
}

function sortSize(a, b) {
  const order = ["XS", "S", "M", "L", "XL", "XXL", "XXXL"];
  const aa = String(a || "").toUpperCase();
  const bb = String(b || "").toUpperCase();
  const ia = order.indexOf(aa);
  const ib = order.indexOf(bb);

  if (ia !== -1 && ib !== -1) return ia - ib;
  if (ia !== -1) return -1;
  if (ib !== -1) return 1;

  return String(a).localeCompare(String(b), "vi", { numeric: true });
}

function normalizeImage(value) {
  return resolveMediaUrl(value) || fallbackImage;
}

function mapVariant(v) {
  const colorObj = typeof v.mauSac === "object" ? v.mauSac : null;

  const rawColorId =
      v.idMauSac ??
      v.id_mau_sac ??
      v.mauSacId ??
      v.id_mau ??
      colorObj?.id ??
      null;

  const rawColorName =
      v.tenMauSac ||
      v.ten_mau_sac ||
      v.mauSacTen ||
      v.mau_sac_ten ||
      colorObj?.ten ||
      colorObj?.name ||
      v.mau ||
      "";

  const dbColor = findDbColor(rawColorId, rawColorName);

  const colorId =
      rawColorId != null && rawColorId !== ""
          ? String(rawColorId)
          : dbColor?.id != null
              ? String(dbColor.id)
              : "";

  const colorName =
      dbColor?.ten || dbColor?.name || rawColorName || "Đang cập nhật";

  const colorKey = colorId || `name:${normalizeColorName(colorName)}`;

  const colorCode = resolveColorCode(
      v.maMauSac ||
      v.ma_mau_sac ||
      v.colorCode ||
      v.color_code ||
      colorObj?.ma ||
      colorObj?.code ||
      colorObj?.hex ||
      dbColor?.ma ||
      dbColor?.code ||
      "",
      colorName,
  );

  const sizeName =
      normalizeSizeName(v.kichCo) ||
      normalizeSizeName(v.tenKichCo) ||
      normalizeSizeName(v.size) ||
      "";

  const image =
      pickVariantImage(v) ||
      pickProductImage(product.value, [], fallbackImage) ||
      normalizeImage(v.anh || v.imageUrl || v.image) ||
      fallbackImage;

  return {
    idSanPhamChiTiet: v.id,
    productId: v.idSanPham || v.sanPhamId || productId.value,
    colorId,
    colorKey,
    color: colorName,
    colorCode,
    size: sizeName,
    price: Number(v.donGia ?? v.giaBan ?? v.price ?? 0),
    stock: Number(v.soLuongTon ?? v.soLuong ?? 0),
    image,
    code: v.maSanPhamChiTiet || v.code || "",
  };
}

function formatCurrency(value) {
  return Number(value || 0).toLocaleString("vi-VN") + " đ";
}

function onImgError(e) {
  e.target.src = fallbackImage;
}

function onThumbError(e) {
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

function selectColor(colorKey) {
  selectedColor.value = String(colorKey || "");

  const validSizes = availableVariants.value
      .filter((v) => String(v.colorKey) === String(colorKey))
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

async function fetchColors() {
  try {
    const data = await getMauSacList();

    if (Array.isArray(data)) {
      dbColors.value = data;
    } else if (Array.isArray(data?.content)) {
      dbColors.value = data.content;
    } else if (Array.isArray(data?.data)) {
      dbColors.value = data.data;
    } else {
      dbColors.value = [];
    }
  } catch (err) {
    console.error("fetchColors error:", err);
    dbColors.value = [];
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

    const targetVariant = selectedVariantId.value
        ? availableVariants.value.find(
            (v) => Number(v.idSanPhamChiTiet) === selectedVariantId.value,
        ) ||
        normalizedVariants.value.find(
            (v) => Number(v.idSanPhamChiTiet) === selectedVariantId.value,
        ) ||
        null
        : null;

    const firstVariant =
        targetVariant || availableVariants.value[0] || normalizedVariants.value[0] || null;

    if (firstVariant) {
      selectedColor.value = firstVariant.colorKey || "";
      selectedSize.value = firstVariant.size || "";
    } else {
      selectedColor.value = "";
      selectedSize.value = "";
    }

    quantity.value = 1;
    manualImage.value = "";
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

onMounted(async () => {
  await fetchColors();
  await fetchProductDetail();
});

watch(
    () => [route.params.id, route.query.variantId],
    () => {
      fetchProductDetail();
    },
);
</script>

<style scoped>
.product-detail-page {
  min-height: 100vh;
  background:
      radial-gradient(
          circle at top left,
          rgba(37, 99, 235, 0.08),
          transparent 34%
      ),
      linear-gradient(180deg, #f5f7fc 0%, #f8fafc 52%, #ffffff 100%);
  color: #0f172a;
}

.detail-hero {
  padding: 22px 0 78px;
  background: linear-gradient(135deg, #06164d 0%, #0a2168 52%, #143c9f 100%);
}

.detail-breadcrumb {
  margin-bottom: 20px;
}

.detail-breadcrumb .breadcrumb-item,
.detail-breadcrumb .breadcrumb-item a {
  color: rgba(255, 255, 255, 0.78);
}

.detail-breadcrumb .breadcrumb-item.active {
  color: #ffffff;
}

.hero-title-box {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  color: #ffffff;
}

.hero-kicker {
  margin-bottom: 8px;
  color: #93c5fd;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.hero-title-box h1 {
  margin: 0;
  font-size: clamp(32px, 4vw, 52px);
  line-height: 1.05;
  font-weight: 850;
  letter-spacing: -0.03em;
}

.hero-title-box p {
  max-width: 680px;
  margin: 12px 0 0;
  color: rgba(255, 255, 255, 0.78);
  line-height: 1.7;
}

.back-shop-btn {
  min-height: 44px;
  display: inline-flex;
  align-items: center;
  gap: 9px;
  padding: 0 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.12);
  color: #ffffff;
  text-decoration: none;
  font-weight: 750;
  border: 1px solid rgba(255, 255, 255, 0.18);
}

.back-shop-btn:hover {
  background: rgba(255, 255, 255, 0.18);
  color: #ffffff;
}

.detail-main {
  margin-top: -48px;
  padding-bottom: 58px;
  position: relative;
  z-index: 2;
}

.state-box {
  min-height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 28px;
  border-radius: 22px;
  background: #ffffff;
  border: 1px solid #e5eaf2;
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.08);
  color: #475569;
  font-weight: 750;
}

.state-box-error {
  color: #b91c1c;
  background: #fff1f2;
}

.state-spinner {
  width: 28px;
  height: 28px;
  border: 3px solid rgba(37, 99, 235, 0.18);
  border-top-color: #2563eb;
  border-radius: 999px;
  animation: spin 0.8s linear infinite;
}

.product-detail-card {
  background: transparent;
}

.gallery-card,
.info-card,
.detail-block {
  background: #ffffff;
  border-radius: 24px;
  border: 1px solid #e5eaf2;
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.08);
  overflow: hidden;
}

.gallery-card {
  padding: 18px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.main-image-wrap {
  position: relative;
  overflow: hidden;
  border-radius: 20px;
  background: #f1f5f9;
}

.main-image {
  width: 100%;
  height: 640px;
  object-fit: cover;
  display: block;
}

.image-badge {
  position: absolute;
  left: 14px;
  top: 14px;
  min-height: 34px;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 0 12px;
  border-radius: 999px;
  color: #0f172a;
  background: rgba(255, 255, 255, 0.92);
  font-size: 12px;
  font-weight: 800;
  backdrop-filter: blur(10px);
}

.thumb-list {
  display: flex;
  gap: 10px;
  margin-top: 14px;
  overflow-x: auto;
  padding-bottom: 4px;
}

.thumb-btn {
  width: 74px;
  height: 86px;
  flex: 0 0 auto;
  overflow: hidden;
  border: 2px solid transparent;
  border-radius: 14px;
  background: #f8fafc;
  padding: 0;
}

.thumb-btn.active {
  border-color: #07143f;
}

.thumb-btn img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.info-card {
  padding: 28px;
  height: 100%;
}

.product-brand {
  margin-bottom: 10px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 850;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.product-title {
  margin: 0 0 12px;
  color: #07143f;
  font-size: clamp(28px, 3vw, 40px);
  line-height: 1.16;
  font-weight: 850;
  letter-spacing: -0.03em;
}

.code-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.product-code,
.total-stock-badge {
  min-height: 32px;
  display: inline-flex;
  align-items: center;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 750;
}

.product-code {
  background: #eef2ff;
  color: #1d4ed8;
}

.total-stock-badge {
  background: #dcfce7;
  color: #15803d;
}

.total-stock-badge.out {
  background: #fee2e2;
  color: #b91c1c;
}

.price-wrap {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  padding: 20px;
  border-radius: 20px;
  background: linear-gradient(135deg, #f8fbff 0%, #eef4ff 100%);
  border: 1px solid rgba(37, 99, 235, 0.12);
}

.price-label {
  margin-bottom: 5px;
  color: #64748b;
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
}

.product-price {
  color: #dc2626;
  font-size: clamp(30px, 3vw, 40px);
  line-height: 1;
  font-weight: 850;
}

.price-side {
  min-width: 92px;
  padding: 11px;
  border-radius: 16px;
  background: #ffffff;
  border: 1px solid #e5eaf2;
  text-align: center;
}

.price-side span,
.price-side strong {
  display: block;
}

.price-side span {
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
}

.price-side strong {
  margin-top: 4px;
  color: #07143f;
  font-size: 18px;
  font-weight: 850;
}

.selected-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 16px;
}

.selected-summary div {
  padding: 12px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid #e5eaf2;
}

.selected-summary span {
  display: block;
  margin-bottom: 5px;
  color: #64748b;
  font-size: 11px;
  font-weight: 750;
}

.selected-summary strong {
  color: #0f172a;
  font-size: 14px;
  font-weight: 850;
}

.desc-box {
  margin-bottom: 22px;
  padding: 16px 18px;
  border-radius: 18px;
  background: #f8fafc;
  color: #475569;
  line-height: 1.75;
  border: 1px solid #e5eaf2;
}

.variant-section + .variant-section,
.qty-section {
  margin-top: 20px;
}

.variant-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.variant-label {
  color: #0f172a;
  font-size: 15px;
  font-weight: 850;
}

.variant-helper {
  margin-top: 3px;
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
}

.variant-header > span {
  color: #1d4ed8;
  font-size: 13px;
  font-weight: 800;
}

.variant-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.color-swatch-btn {
  width: 42px;
  height: 42px;
  padding: 4px;
  border-radius: 999px;
  border: 1px solid #d8e0ea;
  background: #ffffff;
  transition: all 0.2s ease;
}

.color-swatch-btn span {
  width: 100%;
  height: 100%;
  display: block;
  border-radius: 999px;
  border: 1px solid rgba(15, 23, 42, 0.15);
}

.color-swatch-btn:hover,
.color-swatch-btn.active {
  border-color: #07143f;
  box-shadow: 0 0 0 3px rgba(7, 20, 63, 0.12);
  transform: translateY(-1px);
}

.size-btn {
  min-width: 64px;
  min-height: 46px;
  padding: 0 16px;
  border-radius: 14px;
  border: 1px solid #d8e0ea;
  background: #ffffff;
  color: #0f172a;
  font-weight: 850;
  transition: all 0.2s ease;
}

.size-btn:hover,
.size-btn.active {
  color: #ffffff;
  background: #07143f;
  border-color: #07143f;
  box-shadow: 0 10px 20px rgba(7, 20, 63, 0.16);
}

.qty-wrap {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px;
  border: 1px solid #d8e0ea;
  border-radius: 16px;
  background: #f8fafc;
}

.qty-btn {
  width: 42px;
  height: 42px;
  border: 0;
  border-radius: 12px;
  background: #e9edf7;
  color: #0f172a;
  font-size: 21px;
  font-weight: 850;
  transition: all 0.2s ease;
}

.qty-btn:hover:not(:disabled) {
  background: #07143f;
  color: #ffffff;
}

.qty-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.qty-input {
  width: 68px;
  height: 42px;
  border: 0;
  outline: 0;
  background: #ffffff;
  border-radius: 12px;
  text-align: center;
  color: #0f172a;
  font-weight: 850;
}

.warning-box {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-top: 18px;
  padding: 14px 16px;
  border-radius: 16px;
  background: #fff7ed;
  color: #c2410c;
  border: 1px solid #fed7aa;
  font-weight: 650;
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
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  padding: 0 22px;
  border-radius: 16px;
  font-weight: 850;
  text-decoration: none;
  transition: all 0.2s ease;
}

.btn-add-cart {
  border: 0;
  background: #07143f;
  color: #ffffff;
  box-shadow: 0 14px 28px rgba(7, 20, 63, 0.2);
}

.btn-add-cart:hover:not(:disabled) {
  background: #0b1b55;
  transform: translateY(-1px);
}

.btn-add-cart:disabled {
  opacity: 0.58;
  cursor: not-allowed;
}

.btn-view-cart {
  background: #ffffff;
  color: #07143f;
  border: 1px solid #d8e0ea;
}

.btn-view-cart:hover {
  color: #ffffff;
  background: #07143f;
  border-color: #07143f;
}

.extra-info {
  display: grid;
  gap: 10px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px dashed rgba(100, 116, 139, 0.35);
}

.extra-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #475569;
  font-weight: 650;
}

.extra-item i {
  color: #07143f;
  font-size: 16px;
}

.detail-extra-sections {
  margin-top: 28px;
}

.detail-block__header {
  padding: 16px 20px;
  background: linear-gradient(90deg, #07143f 0%, #12348f 100%);
  color: #ffffff;
  font-size: 16px;
  font-weight: 850;
}

.detail-block__body {
  padding: 22px 20px;
}

.detail-description {
  margin-bottom: 18px;
  color: #475569;
  line-height: 1.8;
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
  border: 1px solid #e5eaf2;
}

.spec-label {
  display: block;
  margin-bottom: 6px;
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
}

.spec-item strong {
  color: #0f172a;
  font-size: 15px;
  font-weight: 850;
}

.variant-table-wrap {
  width: 100%;
  overflow-x: auto;
}

.variant-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
}

.variant-table th,
.variant-table td {
  padding: 13px 12px;
  border-bottom: 1px solid #e5eaf2;
  color: #334155;
  font-size: 14px;
  vertical-align: middle;
  white-space: nowrap;
}

.variant-table th {
  color: #0f172a;
  background: #f8fafc;
  font-weight: 850;
}

.table-color {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 750;
}

.table-color i {
  width: 17px;
  height: 17px;
  border-radius: 999px;
  border: 1px solid rgba(15, 23, 42, 0.18);
}

.empty-variant {
  padding: 18px;
  border-radius: 16px;
  color: #64748b;
  background: #f8fafc;
  border: 1px dashed #cbd5e1;
  text-align: center;
  font-weight: 700;
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
  border: 1px solid #e5eaf2;
}

.service-icon {
  flex: 0 0 44px;
  width: 44px;
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: #eaf0ff;
  color: #07143f;
  font-size: 18px;
}

.service-title {
  margin-bottom: 4px;
  color: #0f172a;
  font-weight: 850;
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
  border: 1px solid #e5eaf2;
}

.highlight-item i {
  color: #07143f;
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

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 991.98px) {
  .detail-highlight {
    position: static;
  }

  .hero-title-box {
    align-items: flex-start;
    flex-direction: column;
  }

  .main-image {
    height: 480px;
  }

  .selected-summary,
  .spec-grid,
  .service-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 767.98px) {
  .detail-hero {
    padding-bottom: 68px;
  }

  .detail-main {
    margin-top: -38px;
  }

  .gallery-card,
  .info-card,
  .detail-block {
    border-radius: 20px;
  }

  .main-image {
    height: 420px;
  }

  .info-card {
    padding: 22px;
  }

  .price-wrap {
    align-items: flex-start;
    flex-direction: column;
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
  .main-image {
    height: 360px;
  }
  .gallery-extra__grid {
    grid-template-columns: 1fr;
  }
  .product-title {
    font-size: 26px;
  }

  .product-price {
    font-size: 28px;
  }

  .thumb-btn {
    width: 62px;
    height: 72px;
  }
}
.gallery-extra {
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.gallery-extra__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.gallery-extra__item {
  padding: 14px 16px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid #e5eaf2;
}

.gallery-extra__item span {
  display: block;
  margin-bottom: 6px;
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
}

.gallery-extra__item strong {
  color: #07143f;
  font-size: 18px;
  font-weight: 850;
}

.gallery-extra__note {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border-radius: 18px;
  background: linear-gradient(135deg, #f8fbff 0%, #eef4ff 100%);
  border: 1px solid rgba(37, 99, 235, 0.12);
}

.gallery-extra__note-icon {
  width: 42px;
  height: 42px;
  flex: 0 0 42px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: #eaf0ff;
  color: #07143f;
  font-size: 18px;
}

.gallery-extra__note-title {
  color: #07143f;
  font-weight: 850;
  margin-bottom: 4px;
}

.gallery-extra__note-text {
  color: #64748b;
  line-height: 1.65;
  font-size: 14px;
}
</style>
