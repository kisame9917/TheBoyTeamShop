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
          <h4 class="section-title mb-0">Sản phẩm mới về</h4>
        </div>

        <button class="section-btn" @click="goShop">
          Xem tất cả
          <i class="bi bi-arrow-right"></i>
        </button>
      </div>

      <div v-if="loading" class="state-box">Đang tải sản phẩm...</div>

      <div v-else-if="error" class="state-box text-danger">
        {{ error }}
      </div>

      <div v-else-if="products.length === 0" class="state-box">
        Hiện chưa có sản phẩm nào.
      </div>

      <div v-else-if="newProducts.length === 0" class="state-box">
        Hiện chưa có sản phẩm mới về trong {{ newBadgeDays }} ngày gần đây.
      </div>

      <div v-else class="row row-cols-1 row-cols-sm-2 row-cols-lg-3 row-cols-xl-4 g-4">
        <div class="col" v-for="product in newProducts" :key="'new-' + product.id">
          <article class="home-product-card" role="button" @click="goProduct(product.id)">
            <div class="home-product-card__image-wrap">
              <img
                :src="product.image"
                class="home-product-card__img"
                :alt="product.name"
                @error="onProductImgError"
              />

              <div class="home-product-card__badges">
                <span class="home-product-card__new">Mới về</span>
              </div>
            </div>

            <div class="home-product-card__body">
              <div v-if="product.code" class="home-product-card__code">
                {{ product.code }}
              </div>

              <h5 class="home-product-card__name">
                {{ product.name }}
              </h5>

              <div class="home-product-card__price-row">
                <span>Giá từ</span>
                <strong>{{ money(product.price) }} đ</strong>
              </div>

              <div v-if="product.colorOptions.length" class="home-product-card__meta">
                <div class="home-product-card__meta-label">Màu sắc</div>

                <div class="home-product-card__chip-wrap">
                  <span
                    v-for="color in product.colorOptions.slice(0, 5)"
                    :key="color.key"
                    class="home-color-chip"
                    :title="color.name"
                    :style="{ backgroundColor: color.code }"
                  ></span>

                  <span v-if="product.colorOptions.length > 5" class="home-chip-more">
                    +{{ product.colorOptions.length - 5 }}
                  </span>
                </div>
              </div>

              <div v-if="product.sizes.length" class="home-product-card__meta">
                <div class="home-product-card__meta-label">Kích thước</div>

                <div class="home-product-card__chip-wrap">
                  <span
                    v-for="size in product.sizes.slice(0, 5)"
                    :key="size"
                    class="home-size-chip"
                  >
                    {{ size }}
                  </span>

                  <span v-if="product.sizes.length > 5" class="home-chip-more">
                    +{{ product.sizes.length - 5 }}
                  </span>
                </div>
              </div>

              <div class="home-product-card__extra">
                <div>
                  <span>Số lượng</span>
                  <strong>{{ product.totalStock }}</strong>
                </div>

                <div>
                  <span>Màu</span>
                  <strong>{{ product.colorOptions.length }}</strong>
                </div>

                <div>
                  <span>Size</span>
                  <strong>{{ product.sizes.length }}</strong>
                </div>
              </div>

              <button class="home-detail-btn" type="button" @click.stop="goProduct(product.id)">
                Xem chi tiết
                <i class="bi bi-arrow-right"></i>
              </button>
            </div>
          </article>
        </div>
      </div>
    </section>

    <section v-if="!loading && !error && products.length > 0" class="container pb-5">
      <div class="section-head">
        <div>
          <div class="section-subtitle">Danh sách sản phẩm</div>
          <h4 class="section-title mb-0">Sản phẩm của cửa hàng</h4>
        </div>

        <button class="section-btn" @click="goShop">
          Xem tất cả
          <i class="bi bi-arrow-right"></i>
        </button>
      </div>

      <div v-if="storeProducts.length === 0" class="state-box">
        Các sản phẩm hiện tại đều đang nằm trong nhóm mới về.
      </div>

      <div v-else class="row row-cols-1 row-cols-sm-2 row-cols-lg-3 row-cols-xl-4 g-4">
        <div class="col" v-for="product in storeProducts" :key="'store-' + product.id">
          <article class="home-product-card" role="button" @click="goProduct(product.id)">
            <div class="home-product-card__image-wrap">
              <img
                :src="product.image"
                class="home-product-card__img"
                :alt="product.name"
                @error="onProductImgError"
              />

              <div class="home-product-card__badges">
                <span class="home-product-card__badge">
                  {{ product.categoryLabel }}
                </span>
              </div>
            </div>

            <div class="home-product-card__body">
              <div v-if="product.code" class="home-product-card__code">
                {{ product.code }}
              </div>

              <h5 class="home-product-card__name">
                {{ product.name }}
              </h5>

              <div class="home-product-card__price-row">
                <span>Giá từ</span>
                <strong>{{ money(product.price) }} đ</strong>
              </div>

              <div v-if="product.colorOptions.length" class="home-product-card__meta">
                <div class="home-product-card__meta-label">Màu sắc</div>

                <div class="home-product-card__chip-wrap">
                  <span
                    v-for="color in product.colorOptions.slice(0, 5)"
                    :key="color.key"
                    class="home-color-chip"
                    :title="color.name"
                    :style="{ backgroundColor: color.code }"
                  ></span>

                  <span v-if="product.colorOptions.length > 5" class="home-chip-more">
                    +{{ product.colorOptions.length - 5 }}
                  </span>
                </div>
              </div>

              <div v-if="product.sizes.length" class="home-product-card__meta">
                <div class="home-product-card__meta-label">Kích thước</div>

                <div class="home-product-card__chip-wrap">
                  <span
                    v-for="size in product.sizes.slice(0, 5)"
                    :key="size"
                    class="home-size-chip"
                  >
                    {{ size }}
                  </span>

                  <span v-if="product.sizes.length > 5" class="home-chip-more">
                    +{{ product.sizes.length - 5 }}
                  </span>
                </div>
              </div>

              <div class="home-product-card__extra">
                <div>
                  <span>Số lượng</span>
                  <strong>{{ product.totalStock }}</strong>
                </div>

                <div>
                  <span>Màu</span>
                  <strong>{{ product.colorOptions.length }}</strong>
                </div>

                <div>
                  <span>Size</span>
                  <strong>{{ product.sizes.length }}</strong>
                </div>
              </div>

              <button class="home-detail-btn" type="button" @click.stop="goProduct(product.id)">
                Xem chi tiết
                <i class="bi bi-arrow-right"></i>
              </button>
            </div>
          </article>
        </div>
      </div>
    </section>

    <section class="container py-2">
      <div class="mid-banner">
        <div class="mid-banner__content">
          <div class="mid-banner__label">Bộ sưu tập nổi bật</div>

          <h2 class="mid-banner__title">Bộ sưu tập vest công sở</h2>

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
          <h4 class="section-title mb-0">Lý do nên chọn The Boy Team</h4>
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
          <h4 class="section-title mb-0">Bộ sưu tập ảnh</h4>
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
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import ChatWidget from "../../components/ClientChatWidget.vue";
import {
  getMauSacList,
  getProducts,
  getProductVariantsByProductId,
} from "../../services/productClientApi";
import {
  parseMediaList,
  pickProductImage,
  pickVariantImage,
  resolveMediaUrl,
  sortNewestFirst,
} from "../../utils/media";

const router = useRouter();
const loading = ref(false);
const error = ref("");
const products = ref([]);
const dbColors = ref([]);
const currentBannerIndex = ref(0);

const intervalMs = 3500;
const newBadgeDays = 1;
const maxNewProducts = 10;
const maxStoreProducts = 10;

let timer = null;

const fallbackProductImage =
  "data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='400' height='460'%3E%3Crect width='100%25' height='100%25' fill='%23f1f3f5'/%3E%3Ctext x='50%25' y='52%25' dominant-baseline='middle' text-anchor='middle' fill='%2399a1aa' font-size='18'%3ENo Image%3C/text%3E%3C/svg%3E";

const fallbackBannerImage =
  "data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1280' height='720'%3E%3Crect width='100%25' height='100%25' fill='%23e8edf8'/%3E%3Ctext x='50%25' y='52%25' dominant-baseline='middle' text-anchor='middle' fill='%23000f51' font-size='34'%3EVest%20Banner%3C/text%3E%3C/svg%3E";

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

const configuredHeroBanners = computed(() =>
  parseMediaList(import.meta.env.VITE_HOME_HERO_BANNERS),
);

const configuredMidBanner = computed(() => {
  return resolveMediaUrl(import.meta.env.VITE_HOME_MID_BANNER_URL) || "";
});

const newProducts = computed(() => {
  return products.value
    .filter((product) => isNewProduct(product))
    .slice(0, maxNewProducts);
});

const storeProducts = computed(() => {
  return products.value
    .filter((product) => !isNewProduct(product))
    .slice(0, maxStoreProducts);
});

const banners = computed(() => {
  if (configuredHeroBanners.value.length) {
    return configuredHeroBanners.value.slice(0, 5);
  }

  const dynamic = products.value.map((item) => item.image).filter(Boolean);

  return dynamic.length
    ? dynamic.slice(0, 3)
    : [fallbackBannerImage, fallbackBannerImage, fallbackBannerImage];
});

const currentBanner = computed(() => {
  return banners.value[currentBannerIndex.value] || fallbackBannerImage;
});

const midBannerImage = computed(() => {
  return configuredMidBanner.value || banners.value[0] || fallbackBannerImage;
});

const galleryImages = computed(() => {
  const imgs = products.value.map((item) => item.image).filter(Boolean);

  if (imgs.length >= 4) return imgs.slice(0, 4);

  return [
    ...imgs,
    ...Array(Math.max(0, 4 - imgs.length)).fill(fallbackProductImage),
  ];
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
  return normalizeText(name).replace(/\(.*?\)/g, "").trim();
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
  if (key.includes("navy") || key.includes("than")) return FALLBACK_COLOR_MAP["xanh navy"];
  if (key.includes("xanh") && key.includes("la")) return FALLBACK_COLOR_MAP["xanh la"];
  if (key.includes("xanh") && key.includes("duong")) return FALLBACK_COLOR_MAP["xanh duong"];
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
  const byId = dbColors.value.find((item) => {
    return String(item.id) === String(colorId);
  });

  if (byId) return byId;

  const normalizedName = normalizeColorName(colorName);

  return dbColors.value.find((item) => {
    return normalizeColorName(item.ten || item.name || "") === normalizedName;
  });
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

function extractName(item) {
  return item?.tenSanPham || item?.name || item?.title || item?.ten || "Sản phẩm";
}

function extractCode(item) {
  return item?.maSanPham || item?.code || item?.ma || "";
}

function extractCategory(item) {
  return (
    item?.loaiSanPham?.tenLoaiSanPham ||
    item?.loaiSanPham?.ten ||
    item?.tenLoaiSanPham ||
    item?.categoryName ||
    item?.category ||
    "Sản phẩm"
  );
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
    .map((detail) => {
      return Number(
        detail?.price ??
          detail?.donGia ??
          detail?.giaBan ??
          detail?.gia ??
          0,
      );
    })
    .filter((n) => !Number.isNaN(n) && n > 0);

  if (prices.length) return Math.min(...prices);

  return 0;
}

function getProductCreatedAt(product) {
  return (
    product?.createdAt ||
    product?.ngayTao ||
    product?.created_at ||
    product?.raw?.ngayTao ||
    product?.raw?.createdAt ||
    product?.raw?.created_at ||
    ""
  );
}

function isNewProduct(product) {
  const createdAt = getProductCreatedAt(product);

  if (!createdAt) return false;

  const createdTime = Date.parse(createdAt);

  if (Number.isNaN(createdTime)) return false;

  const now = Date.now();
  const diff = now - createdTime;
  const limit = newBadgeDays * 24 * 60 * 60 * 1000;

  return diff >= 0 && diff <= limit;
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
    dbColor?.ten ||
    dbColor?.name ||
    rawColorName ||
    "Đang cập nhật";

  const colorKey =
    colorId || `name:${normalizeColorName(colorName)}`;

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

  return {
    idSanPhamChiTiet: v.id,
    colorId,
    colorKey,
    color: colorName,
    colorCode,
    size: sizeName,
    price: Number(v.donGia ?? v.giaBan ?? v.price ?? v.gia ?? 0),
    stock: Number(v.soLuongTon ?? v.soLuong ?? 0),
    image: pickVariantImage(v, fallbackProductImage),
    code: v.maSanPhamChiTiet || v.code || "",
  };
}

function buildColorOptions(variants) {
  const map = new Map();

  variants.forEach((item) => {
    if (!item.colorKey || !item.color) return;

    if (!map.has(item.colorKey)) {
      map.set(item.colorKey, {
        key: item.colorKey,
        id: item.colorId,
        name: item.color,
        code: item.colorCode || colorByName(item.color),
      });
    }
  });

  return [...map.values()];
}

function buildSizeOptions(variants) {
  return [...new Set(variants.map((item) => item.size).filter(Boolean))].sort(sortSize);
}

function buildTotalStock(variants) {
  return variants.reduce((sum, item) => {
    return sum + Number(item.stock || 0);
  }, 0);
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

async function fetchProducts() {
  try {
    loading.value = true;
    error.value = "";

    const data = await getProducts({ page: 0, size: 1000 });

    const raw = Array.isArray(data?.content)
      ? data.content
      : Array.isArray(data?.data?.content)
        ? data.data.content
        : Array.isArray(data?.data)
          ? data.data
          : Array.isArray(data)
            ? data
            : [];

    const sorted = sortNewestFirst(raw);

    const enriched = await Promise.all(
      sorted.map(async (item) => {
        let variants = [];

        try {
          const variantRes = await getProductVariantsByProductId(item.id);
          variants = Array.isArray(variantRes) ? variantRes : [];
        } catch (variantErr) {
          console.error("fetch home variants error:", item.id, variantErr);
        }

        const mappedVariants = variants.map(mapVariant);
        const availableVariants = mappedVariants.filter((variant) => Number(variant.stock || 0) > 0);
        const sourceVariants = availableVariants.length ? availableVariants : mappedVariants;

        return {
          id: item.id,
          name: extractName(item),
          code: extractCode(item),
          categoryLabel: extractCategory(item),
          price: extractPrice(item, sourceVariants),
          image: pickProductImage(item, variants, fallbackProductImage),
          colors: buildColorOptions(sourceVariants).map((color) => color.name),
          colorOptions: buildColorOptions(sourceVariants),
          sizes: buildSizeOptions(sourceVariants),
          totalStock: buildTotalStock(availableVariants),
          variantCount: mappedVariants.length,
          createdAt: item.ngayTao || item.createdAt || item.created_at || "",
          updatedAt: item.ngayCapNhat || item.updatedAt || item.updated_at || "",
          raw: item,
        };
      }),
    );

    products.value = sortNewestFirst(enriched);

    if (currentBannerIndex.value >= banners.value.length) {
      currentBannerIndex.value = 0;
    }
  } catch (err) {
    console.error("fetchProducts error:", err);
    error.value = err?.response?.data?.message || "Không tải được sản phẩm";
  } finally {
    loading.value = false;
  }
}

function goProduct(id) {
  router.push({ name: "ProductDetail", params: { id } });
}

function goShop() {
  router.push({ name: "Search" });
}

function money(v) {
  const n = Number(v) || 0;
  return n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
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
  await fetchColors();
  await fetchProducts();
  startAuto();
});

onBeforeUnmount(() => stopAuto());
</script>

<style scoped>
.homepage {
  background:
    radial-gradient(circle at top left, rgba(37, 99, 235, 0.08), transparent 34%),
    linear-gradient(180deg, #f5f7fc 0%, #f8fafc 48%, #ffffff 100%);
  min-height: 100vh;
  color: #0f172a;
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
    linear-gradient(90deg, rgba(6, 22, 77, 0.86) 0%, rgba(10, 33, 104, 0.54) 44%, rgba(20, 60, 159, 0.12) 100%);
}

.hero-content {
  position: relative;
  z-index: 2;
  max-width: 560px;
  padding: 42px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(12px);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.16);
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  color: #ffffff;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  margin-bottom: 18px;
}

.hero-title {
  font-size: 56px;
  line-height: 1.08;
  color: #ffffff;
  font-weight: 850;
  margin-bottom: 16px;
  letter-spacing: -0.03em;
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
  font-weight: 800;
  font-size: 15px;
  transition: all 0.25s ease;
}

.hero-btn--primary {
  border: 0;
  background: #ffffff;
  color: #07143f;
  box-shadow: 0 14px 28px rgba(0, 15, 81, 0.18);
}

.hero-btn--outline {
  border: 1px solid rgba(255, 255, 255, 0.28);
  background: rgba(255, 255, 255, 0.08);
  color: #ffffff;
}

.hero-btn--primary:hover,
.hero-btn--outline:hover {
  transform: translateY(-2px);
}

.hero-btn--primary:hover {
  background: #eff6ff;
  color: #07143f;
}

.hero-btn--outline:hover {
  background: rgba(255, 255, 255, 0.16);
  color: #ffffff;
}

.slider-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 48px;
  height: 48px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: rgba(255, 255, 255, 0.16);
  color: #ffffff;
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
  background-color: #ffffff;
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
  background: #ffffff;
  border-radius: 24px;
  padding: 18px 22px;
  box-shadow: 0 20px 40px rgba(10, 24, 74, 0.08);
  border: 1px solid #e5eaf2;
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
  width: 38px;
  height: 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 13px;
  background: #eff6ff;
  color: #07143f;
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
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: #2563eb;
  margin-bottom: 8px;
  font-weight: 750;
}

.section-title {
  font-size: 30px;
  color: #0f172a;
  font-weight: 800;
  letter-spacing: -0.02em;
  text-transform: none;
}

.section-btn {
  min-height: 44px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 0 18px;
  border-radius: 14px;
  border: 1px solid #07143f;
  background: #07143f;
  color: #ffffff;
  font-weight: 700;
  transition: all 0.25s ease;
  box-shadow: 0 14px 28px rgba(7, 20, 63, 0.18);
}

.section-btn:hover {
  background: #0b1b55;
  border-color: #0b1b55;
  color: #ffffff;
  transform: translateY(-2px);
}

.home-product-card {
  height: 100%;
  overflow: hidden;
  cursor: pointer;
  border-radius: 20px;
  border: 1px solid #e5eaf2;
  background: #ffffff;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.06);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.home-product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 38px rgba(15, 23, 42, 0.11);
}

.home-product-card__image-wrap {
  position: relative;
  overflow: hidden;
  height: 250px;
  background: #f3f6fb;
  display: flex;
  align-items: center;
  justify-content: center;
}

.home-product-card__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.28s ease;
}

.home-product-card:hover .home-product-card__img {
  transform: scale(1.04);
}

.home-product-card__badges {
  position: absolute;
  left: 10px;
  right: 10px;
  top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.home-product-card__badge,
.home-product-card__new {
  min-height: 26px;
  display: inline-flex;
  align-items: center;
  padding: 0 9px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  backdrop-filter: blur(10px);
}

.home-product-card__badge {
  color: #0f172a;
  background: rgba(255, 255, 255, 0.9);
}

.home-product-card__new {
  color: #ffffff;
  background: #07143f;
}

.home-product-card__body {
  padding: 15px;
}

.home-product-card__code {
  margin-bottom: 7px;
  color: #2563eb;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.home-product-card__name {
  min-height: 36px;
  margin: 0 0 4px;
  color: #0f172a;
  font-size: 20px;
  line-height: 1.35;
  font-weight: 750;
  letter-spacing: 0;
}

.home-product-card__price-row {
  display: block;
  margin-top: 12px;
}

.home-product-card__price-row span {
  display: block;
  margin-bottom: 3px;
  color: #94a3b8;
  font-size: 11px;
  font-weight: 700;
}

.home-product-card__price-row strong {
  color: #dc2626;
  font-size: 21px;
  line-height: 1.1;
  font-weight: 750;
  letter-spacing: 0;
}

.home-product-card__meta {
  margin-top: 12px;
}

.home-product-card__meta-label {
  margin-bottom: 6px;
  color: #64748b;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.home-product-card__chip-wrap {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 7px;
}

.home-color-chip {
  width: 21px;
  height: 21px;
  padding: 0;
  border-radius: 999px;
  border: 2px solid #ffffff;
  box-shadow: 0 0 0 1px rgba(148, 163, 184, 0.5);
}

.home-size-chip,
.home-chip-more {
  min-height: 24px;
  display: inline-flex;
  align-items: center;
  padding: 0 9px;
  border-radius: 999px;
  color: #0f172a;
  background: #f1f5f9;
  font-size: 12px;
  font-weight: 700;
}

.home-chip-more {
  color: #1d4ed8;
  background: #dbeafe;
}

.home-product-card__extra {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 7px;
  margin-top: 13px;
}

.home-product-card__extra div {
  padding: 8px 6px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid rgba(148, 163, 184, 0.16);
  text-align: center;
}

.home-product-card__extra span {
  display: block;
  margin-bottom: 3px;
  color: #94a3b8;
  font-size: 10px;
  font-weight: 700;
}

.home-product-card__extra strong {
  color: #0f172a;
  font-size: 16px;
  font-weight: 750;
}

.home-detail-btn {
  width: 100%;
  min-height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  margin-top: 14px;
  border: 0;
  border-radius: 13px;
  color: #ffffff;
  background: #07143f;
  font-size: 16px;
  font-weight: 700;
  box-shadow: none;
  transition: all 0.2s ease;
}

.home-detail-btn:hover {
  background: #0b1b55;
  transform: translateY(-2px);
}

.mid-banner {
  min-height: 380px;
  border-radius: 28px;
  overflow: hidden;
  background: linear-gradient(135deg, #06164d 0%, #0a2168 52%, #143c9f 100%);
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  align-items: center;
  box-shadow: 0 22px 44px rgba(0, 15, 81, 0.14);
}

.mid-banner__content {
  padding: 42px;
  color: #ffffff;
}

.mid-banner__label {
  display: inline-block;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 13px;
  font-weight: 800;
  margin-bottom: 16px;
}

.mid-banner__title {
  font-size: 38px;
  line-height: 1.2;
  font-weight: 850;
  margin-bottom: 14px;
  letter-spacing: -0.02em;
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
  border: 0;
  background: #ffffff;
  color: #07143f;
  font-weight: 850;
  box-shadow: 0 14px 28px rgba(0, 15, 81, 0.18);
  transition: all 0.25s ease;
}

.mid-banner__btn:hover {
  background: #eff6ff;
  transform: translateY(-2px);
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
  background: #ffffff;
  border-radius: 22px;
  padding: 24px 20px;
  border: 1px solid #e5eaf2;
  box-shadow: 0 14px 30px rgba(10, 24, 74, 0.05);
  transition: all 0.2s ease;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 38px rgba(15, 23, 42, 0.1);
}

.feature-card__icon {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #eaf0ff;
  color: #07143f;
  font-size: 20px;
  margin-bottom: 16px;
}

.feature-card__title {
  color: #0f172a;
  font-weight: 850;
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
  background: #ffffff;
  border: 1px solid #e5eaf2;
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
  border: 1px solid #e5eaf2;
  border-radius: 18px;
  background: #ffffff;
  box-shadow: 0 12px 28px rgba(7, 20, 69, 0.05);
  color: #475569;
  font-weight: 750;
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

  .slider-btn {
    display: none;
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