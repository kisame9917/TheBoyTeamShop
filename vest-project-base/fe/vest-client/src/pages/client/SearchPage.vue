<template>
  <div class="search-page">
    <section class="search-hero">
      <div class="container">
       

        <div class="hero-card">
          <div>
            <span class="hero-badge">
              <i class="bi bi-bag-heart-fill me-2"></i>
              Bộ sưu tập sản phẩm
            </span>

            <h1 class="hero-title">DANH SÁCH SẢN PHẨM</h1>

            <p class="hero-desc">
              Khám phá các mẫu vest, phụ kiện và sản phẩm thời trang mới nhất.
              Bộ lọc thông minh giúp bạn tìm đúng sản phẩm theo loại, màu, size và giá.
            </p>
          </div>

          <div class="hero-stats">
            <div class="hero-stat">
              <strong>{{ availableProductCount }}</strong>
              <span>Sản phẩm còn hàng</span>
            </div>

            <div class="hero-stat">
              <strong>{{ allColors.length }}</strong>
              <span>Màu sắc</span>
            </div>

            <div class="hero-stat">
              <strong>{{ allSizes.length }}</strong>
              <span>Kích thước</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <main class="container product-section">
      <div class="row g-4">
        <div class="col-lg-3">
          <aside class="filter-box">
            <div class="filter-head">
              <div>
                <div class="filter-subtitle">Tìm kiếm nâng cao</div>
                <h5 class="filter-title">Bộ lọc sản phẩm</h5>
              </div>

              <button
                v-if="hasActiveFilter"
                class="filter-clear-mini"
                type="button"
                @click="resetFilter"
              >
                Xóa
              </button>
            </div>

            <div class="filter-group">
              <label class="filter-label">Từ khóa</label>
              <div class="filter-input-icon">
                <i class="bi bi-search"></i>
                <input
                  v-model.trim="keyword"
                  type="text"
                  class="filter-input"
                  placeholder="Tên sản phẩm..."
                />
              </div>
            </div>

            <div class="filter-group" @click.stop>
              <label class="filter-label">Loại sản phẩm</label>
              <div class="combo-box">
                <button
                  type="button"
                  class="combo-trigger"
                  :class="{ active: selectedCategory }"
                  @click="toggleCombo('category')"
                >
                  <span>{{ selectedCategoryLabel }}</span>
                  <i class="bi bi-chevron-down"></i>
                </button>

                <div v-if="openedCombo === 'category'" class="combo-menu">
                  <div class="combo-search">
                    <i class="bi bi-search"></i>
                    <input
                      v-model.trim="categoryQuery"
                      type="text"
                      placeholder="Tìm loại sản phẩm..."
                    />
                  </div>

                  <button
                    type="button"
                    class="combo-option"
                    :class="{ selected: !selectedCategory }"
                    @mousedown.prevent="pickCategory('')"
                  >
                    Tất cả loại sản phẩm
                  </button>

                  <button
                    v-for="item in filteredCategoryOptions"
                    :key="item.id"
                    type="button"
                    class="combo-option"
                    :class="{ selected: String(selectedCategory) === String(item.id) }"
                    @mousedown.prevent="pickCategory(item.id)"
                  >
                    {{ item.label }}
                  </button>

                  <div v-if="filteredCategoryOptions.length === 0" class="combo-empty">
                    Không tìm thấy loại phù hợp
                  </div>
                </div>
              </div>
            </div>

            <div class="filter-group" @click.stop>
              <label class="filter-label">Màu sắc</label>
              <div class="combo-box">
                <button
                  type="button"
                  class="combo-trigger"
                  :class="{ active: selectedColor }"
                  @click="toggleCombo('color')"
                >
                  <span class="combo-color-label">
                    <i
                      v-if="selectedColor"
                      class="color-dot"
                      :style="{ backgroundColor: getColorCode(selectedColor) }"
                    ></i>
                    {{ selectedColorLabel }}
                  </span>
                  <i class="bi bi-chevron-down"></i>
                </button>

                <div v-if="openedCombo === 'color'" class="combo-menu">
                  <div class="combo-search">
                    <i class="bi bi-search"></i>
                    <input
                      v-model.trim="colorQuery"
                      type="text"
                      placeholder="Tìm màu sắc..."
                    />
                  </div>

                  <button
                    type="button"
                    class="combo-option"
                    :class="{ selected: !selectedColor }"
                    @mousedown.prevent="pickColor('')"
                  >
                    Tất cả màu sắc
                  </button>

                  <button
                    v-for="color in filteredColorOptions"
                    :key="color.id"
                    type="button"
                    class="combo-option combo-option-color"
                    :class="{ selected: String(selectedColor) === String(color.id) }"
                    @mousedown.prevent="pickColor(color.id)"
                  >
                    <i class="color-dot" :style="{ backgroundColor: color.code }"></i>
                    {{ color.name }}
                  </button>

                  <div v-if="filteredColorOptions.length === 0" class="combo-empty">
                    Không tìm thấy màu phù hợp
                  </div>
                </div>
              </div>
            </div>

            <div class="filter-group" @click.stop>
              <label class="filter-label">Kích thước</label>
              <div class="combo-box">
                <button
                  type="button"
                  class="combo-trigger"
                  :class="{ active: selectedSize }"
                  @click="toggleCombo('size')"
                >
                  <span>{{ selectedSize || "Tất cả kích thước" }}</span>
                  <i class="bi bi-chevron-down"></i>
                </button>

                <div v-if="openedCombo === 'size'" class="combo-menu">
                  <div class="combo-search">
                    <i class="bi bi-search"></i>
                    <input
                      v-model.trim="sizeQuery"
                      type="text"
                      placeholder="Tìm kích thước..."
                    />
                  </div>

                  <button
                    type="button"
                    class="combo-option"
                    :class="{ selected: !selectedSize }"
                    @mousedown.prevent="pickSize('')"
                  >
                    Tất cả kích thước
                  </button>

                  <button
                    v-for="item in filteredSizeOptions"
                    :key="item"
                    type="button"
                    class="combo-option"
                    :class="{ selected: selectedSize === item }"
                    @mousedown.prevent="pickSize(item)"
                  >
                    {{ item }}
                  </button>

                  <div v-if="filteredSizeOptions.length === 0" class="combo-empty">
                    Không tìm thấy kích thước phù hợp
                  </div>
                </div>
              </div>
            </div>

            <div class="filter-group">
              <label class="filter-label">Khoảng giá</label>

              <div class="price-box">
                <div class="price-inputs">
                  <div class="price-field">
                    <span>Từ</span>
                    <input
                      v-model.number="minPriceFilter"
                      type="number"
                      min="0"
                      class="price-input"
                    />
                  </div>

                  <div class="price-field">
                    <span>Đến</span>
                    <input
                      v-model.number="maxPriceFilter"
                      type="number"
                      min="0"
                      class="price-input"
                    />
                  </div>
                </div>

                <input
                  v-model.number="maxPriceFilter"
                  type="range"
                  class="price-slider"
                  min="0"
                  :max="safeMaxDbPrice"
                  step="10000"
                />

                <div class="price-range-text">
                  {{ money(normalMinPrice) }} đ - {{ money(normalMaxPrice) }} đ
                </div>
              </div>
            </div>

            <div class="filter-group">
              <label class="filter-label">Tình trạng</label>
              <div class="stock-filter">
                <button
                  type="button"
                  class="stock-btn"
                  :class="{ active: selectedStock === 'all' }"
                  @click="selectedStock = 'all'"
                >
                  Tất cả
                </button>

                <button
                  type="button"
                  class="stock-btn"
                  :class="{ active: selectedStock === 'in-stock' }"
                  @click="selectedStock = 'in-stock'"
                >
                  Còn hàng
                </button>
              </div>
            </div>

            <button class="reset-btn" type="button" @click="resetFilter">
              <i class="bi bi-arrow-counterclockwise me-2"></i>
              Xóa toàn bộ bộ lọc
            </button>
          </aside>
        </div>

        <div class="col-lg-9">
          <div class="product-toolbar">
            <div>
              <div class="toolbar-subtitle">Kết quả tìm kiếm</div>
              <h2 class="toolbar-title">Sản phẩm phù hợp</h2>
            </div>

            <div class="toolbar-actions">
              <div class="result-count">
                {{ sortedProducts.length }} sản phẩm
              </div>

              <select v-model="sortBy" class="sort-select">
                <option value="newest">Mới nhất</option>
                <option value="priceAsc">Giá thấp đến cao</option>
                <option value="priceDesc">Giá cao đến thấp</option>
                <option value="nameAsc">Tên A-Z</option>
              </select>
            </div>
          </div>

          <div v-if="hasActiveFilter" class="active-filter-bar">
            <span>Bộ lọc đang dùng:</span>

            <button v-if="keyword" type="button" @click="keyword = ''">
              Từ khóa: {{ keyword }}
              <i class="bi bi-x"></i>
            </button>

            <button v-if="selectedCategory" type="button" @click="pickCategory('')">
              Loại: {{ selectedCategoryLabel }}
              <i class="bi bi-x"></i>
            </button>

            <button v-if="selectedColor" type="button" @click="pickColor('')">
              Màu: {{ selectedColorLabel }}
              <i class="bi bi-x"></i>
            </button>

            <button v-if="selectedSize" type="button" @click="pickSize('')">
              Size: {{ selectedSize }}
              <i class="bi bi-x"></i>
            </button>

            <button v-if="selectedStock !== 'all'" type="button" @click="selectedStock = 'all'">
              Còn hàng
              <i class="bi bi-x"></i>
            </button>
          </div>

          <div v-if="loading" class="state-box">
            <div class="state-spinner"></div>
            <div>Đang tải sản phẩm...</div>
          </div>

          <div v-else-if="error" class="state-box state-box-error">
            <i class="bi bi-exclamation-triangle"></i>
            <div>{{ error }}</div>
          </div>

          <div v-else-if="sortedProducts.length === 0" class="state-box empty-state">
            <div class="empty-icon">
              <i class="bi bi-search"></i>
            </div>
            <h5>Không có sản phẩm phù hợp</h5>
            <p>Hãy thử đổi từ khóa hoặc bỏ bớt bộ lọc để xem thêm sản phẩm.</p>
            <button class="reset-btn reset-btn-inline" type="button" @click="resetFilter">
              Xóa bộ lọc
            </button>
          </div>

          <div v-else class="row g-4">
            <div
              v-for="item in pagedProducts"
              :key="item.id"
              class="col-sm-6 col-xl-4"
            >
              <article class="product-card" @click="goDetail(item.id)">
                <div class="product-image-wrap">
                  <img
                    :src="item.displayImage"
                    class="product-img"
                    :alt="item.displayName"
                    @error="onImgError"
                  />

              
                </div>

                <div class="product-body">
                  <div class="product-code" v-if="item.code">
                    {{ item.code }}
                  </div>

                  <h5 class="product-name">
                    {{ item.displayName }}
                  </h5>

                  <div class="product-price-row">
                    <span class="price-label">Giá từ</span>
                    <div class="product-price">
                      {{ money(item.displayPrice) }} đ
                    </div>
                  </div>

                  <div v-if="item.colorOptions.length" class="meta-block">
                    <div class="meta-label">Màu sắc</div>
                    <div class="chip-wrap">
                      <span
                        v-for="color in item.colorOptions.slice(0, 5)"
                        :key="color.id"
                        class="color-chip"
                        :title="color.name"
                        :style="{ backgroundColor: getColorCode(color.id) }"
                      ></span>

                      <span v-if="item.colorOptions.length > 5" class="chip-more">
                        +{{ item.colorOptions.length - 5 }}
                      </span>
                    </div>
                  </div>

                  <div v-if="item.sizes.length" class="meta-block">
                    <div class="meta-label">Kích thước</div>
                    <div class="chip-wrap">
                      <span
                        v-for="itemSize in item.sizes.slice(0, 5)"
                        :key="itemSize"
                        class="size-chip"
                      >
                        {{ itemSize }}
                      </span>

                      <span v-if="item.sizes.length > 5" class="chip-more">
                        +{{ item.sizes.length - 5 }}
                      </span>
                    </div>
                  </div>

                  <div class="product-extra">
                    <div>
                      <span>Số lượng</span>
                      <strong>{{ item.totalStock }}</strong>
                    </div>

                    <div>
                      <span>Màu</span>
                      <strong>{{ item.colorOptions.length || 0 }}</strong>
                    </div>

                    <div>
                      <span>Size</span>
                      <strong>{{ item.sizes.length || 0 }}</strong>
                    </div>
                  </div>

                  <button
                    class="detail-btn"
                    type="button"
                    @click.stop="goDetail(item.id)"
                  >
                    Xem chi tiết
                    <i class="bi bi-arrow-right"></i>
                  </button>
                </div>
              </article>
            </div>
          </div>

          <div
            v-if="!loading && !error && totalPages > 1"
            class="pagination-wrap"
          >
            <button
              class="page-arrow"
              type="button"
              :disabled="page === 0"
              @click="changePage(page - 1)"
            >
              <i class="bi bi-chevron-left"></i>
            </button>

            <button
              v-for="pageNumber in totalPages"
              :key="pageNumber"
              class="page-number"
              :class="{ active: page === pageNumber - 1 }"
              type="button"
              @click="changePage(pageNumber - 1)"
            >
              {{ pageNumber }}
            </button>

            <button
              class="page-arrow"
              type="button"
              :disabled="page >= totalPages - 1"
              @click="changePage(page + 1)"
            >
              <i class="bi bi-chevron-right"></i>
            </button>
          </div>
        </div>
      </div>
    </main>

    <ChatWidget />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import ChatWidget from "../../components/ClientChatWidget.vue";
import {
  getGiaMaxDb,
  getLoaiSanPhamList,
  getMauSacList,
  getProductVariantsByProductId,
  getProducts,
} from "../../services/productClientApi";
import {
  pickProductImage,
  pickVariantImage,
  sortNewestFirst,
} from "../../utils/media";

const router = useRouter();
const route = useRoute();

const loading = ref(false);
const error = ref("");

const rawProducts = ref([]);
const products = ref([]);
const categories = ref([]);
const dbColors = ref([]);

const page = ref(0);
const size = ref(12);
const totalPages = ref(0);

const keyword = ref("");
const selectedCategory = ref("");
const selectedColor = ref("");
const selectedSize = ref("");
const selectedStock = ref("all");
const sortBy = ref("newest");

const maxDbPrice = ref(0);
const minPriceFilter = ref(0);
const maxPriceFilter = ref(0);

const openedCombo = ref("");
const categoryQuery = ref("");
const colorQuery = ref("");
const sizeQuery = ref("");

const fallbackImage =
  "data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='400' height='460'%3E%3Crect width='100%25' height='100%25' fill='%23f1f3f5'/%3E%3Ctext x='50%25' y='52%25' dominant-baseline='middle' text-anchor='middle' fill='%2399a1aa' font-size='18'%3ENo Image%3C/text%3E%3C/svg%3E";

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

const safeMaxDbPrice = computed(() => {
  const v = Number(maxDbPrice.value) || 0;
  return v > 0 ? v : 10000000;
});

const normalMinPrice = computed(() => {
  return Math.min(Number(minPriceFilter.value) || 0, Number(maxPriceFilter.value) || 0);
});

const normalMaxPrice = computed(() => {
  return Math.max(Number(minPriceFilter.value) || 0, Number(maxPriceFilter.value) || 0);
});

const availableProductCount = computed(() => {
  return products.value.filter((item) => Number(item.totalStock || 0) > 0).length;
});

const categoryOptions = computed(() => {
  return (categories.value || []).map((item) => ({
    id: String(item.id),
    label:
      item.tenLoaiSanPham ||
      item.name ||
      item.ten ||
      item.label ||
      `Loại ${item.id}`,
  }));
});

const selectedCategoryLabel = computed(() => {
  if (!selectedCategory.value) return "Tất cả loại sản phẩm";

  const found = categoryOptions.value.find(
    (item) => String(item.id) === String(selectedCategory.value),
  );

  return found?.label || "Tất cả loại sản phẩm";
});

const filteredCategoryOptions = computed(() => {
  const q = normalizeText(categoryQuery.value);

  if (!q) return categoryOptions.value;

  return categoryOptions.value.filter((item) =>
    normalizeText(item.label).includes(q),
  );
});

const allColors = computed(() => {
  const map = new Map();

  products.value.forEach((product) => {
    (product.colorOptions || []).forEach((color) => {
      const id = String(color.id || "");
      const name = String(color.name || "").trim();

      if (!id || !name) return;

      const dbColor = dbColors.value.find((item) => String(item.id) === id);
      const code = resolveColorCode(dbColor?.ma || dbColor?.code || color.code, dbColor?.ten || name);

      if (!map.has(id)) {
        map.set(id, {
          id,
          name: dbColor?.ten || name,
          code,
        });
      }
    });
  });

  return [...map.values()].sort((a, b) =>
    String(a.name).localeCompare(String(b.name), "vi"),
  );
});

const selectedColorLabel = computed(() => {
  if (!selectedColor.value) return "Tất cả màu sắc";

  const found = allColors.value.find(
    (item) => String(item.id) === String(selectedColor.value),
  );

  return found?.name || "Tất cả màu sắc";
});

const allSizes = computed(() => {
  return [...new Set(products.value.flatMap((p) => p.sizes || []))]
    .filter(Boolean)
    .sort(sortSize);
});

const filteredColorOptions = computed(() => {
  const q = normalizeText(colorQuery.value);

  if (!q) return allColors.value;

  return allColors.value.filter((item) => normalizeText(item.name).includes(q));
});

const filteredSizeOptions = computed(() => {
  const q = normalizeText(sizeQuery.value);

  if (!q) return allSizes.value;

  return allSizes.value.filter((item) => normalizeText(item).includes(q));
});

const filteredProducts = computed(() => {
  return (products.value || []).filter((item) => {
    if (Number(item.totalStock || 0) <= 0) return false;

    const kw = normalizeText(keyword.value);

    const productCategoryId =
      item.idLoaiSanPham || item.loaiSanPhamId || item.loaiSanPham?.id || null;

    const searchContent = normalizeText([
      item.displayName,
      item.categoryLabel,
      item.colors.join(" "),
      item.sizes.join(" "),
    ].join(" "));

    const matchKeyword = !kw || searchContent.includes(kw);

    const matchCategory =
      !selectedCategory.value ||
      String(productCategoryId) === String(selectedCategory.value);

    const matchColor =
      !selectedColor.value ||
      (item.colorIds || []).map(String).includes(String(selectedColor.value));

    const matchSize =
      !selectedSize.value || (item.sizes || []).includes(selectedSize.value);

    const price = Number(item.displayPrice || 0);

    const matchPrice =
      price >= normalMinPrice.value && price <= normalMaxPrice.value;

    const matchStock =
      selectedStock.value === "all" || selectedStock.value === "in-stock";

    return (
      matchKeyword &&
      matchCategory &&
      matchColor &&
      matchSize &&
      matchPrice &&
      matchStock
    );
  });
});

const sortedProducts = computed(() => {
  const list = [...filteredProducts.value];

  if (sortBy.value === "priceAsc") {
    return list.sort((a, b) => Number(a.displayPrice || 0) - Number(b.displayPrice || 0));
  }

  if (sortBy.value === "priceDesc") {
    return list.sort((a, b) => Number(b.displayPrice || 0) - Number(a.displayPrice || 0));
  }

  if (sortBy.value === "nameAsc") {
    return list.sort((a, b) =>
      String(a.displayName || "").localeCompare(String(b.displayName || ""), "vi"),
    );
  }

  return list;
});

const pagedProducts = computed(() => {
  const start = page.value * size.value;
  const end = start + size.value;
  return sortedProducts.value.slice(start, end);
});

const hasActiveFilter = computed(() => {
  return (
    !!keyword.value ||
    !!selectedCategory.value ||
    !!selectedColor.value ||
    !!selectedSize.value ||
    selectedStock.value !== "all" ||
    Number(minPriceFilter.value || 0) > 0 ||
    Number(maxPriceFilter.value || 0) < Number(safeMaxDbPrice.value || 0)
  );
});

watch(
  sortedProducts,
  (list) => {
    totalPages.value = Math.max(1, Math.ceil(list.length / size.value));

    if (page.value >= totalPages.value) {
      page.value = 0;
    }
  },
  { immediate: true },
);

watch(
  () => route.query,
  async () => {
    page.value = 0;
    syncFilterFromQuery();
    await fetchProductsData();
  },
  { deep: true },
);

watch(
  [
    keyword,
    selectedCategory,
    selectedColor,
    selectedSize,
    selectedStock,
    minPriceFilter,
    maxPriceFilter,
    sortBy,
  ],
  () => {
    page.value = 0;
  },
);

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

function resolveColorCode(value, colorName = "") {
  const raw = String(value || "").trim();

  if (raw.startsWith("#")) return raw;
  if (/^[0-9a-fA-F]{3}$/.test(raw) || /^[0-9a-fA-F]{6}$/.test(raw)) return `#${raw}`;
  if (/^(rgb|rgba|hsl|hsla)\(/i.test(raw)) return raw;

  const keyByValue = normalizeColorName(raw);
  if (FALLBACK_COLOR_MAP[keyByValue]) return FALLBACK_COLOR_MAP[keyByValue];

  const keyByName = normalizeColorName(colorName);
  if (FALLBACK_COLOR_MAP[keyByName]) return FALLBACK_COLOR_MAP[keyByName];

  if (keyByName.includes("navy") || keyByName.includes("than")) return FALLBACK_COLOR_MAP["xanh navy"];
  if (keyByName.includes("xanh") && keyByName.includes("la")) return FALLBACK_COLOR_MAP["xanh la"];
  if (keyByName.includes("xanh") && keyByName.includes("duong")) return FALLBACK_COLOR_MAP["xanh duong"];
  if (keyByName.includes("do")) return FALLBACK_COLOR_MAP.do;
  if (keyByName.includes("vang")) return FALLBACK_COLOR_MAP.vang;
  if (keyByName.includes("cam")) return FALLBACK_COLOR_MAP.cam;
  if (keyByName.includes("hong")) return FALLBACK_COLOR_MAP.hong;
  if (keyByName.includes("tim")) return FALLBACK_COLOR_MAP.tim;
  if (keyByName.includes("nau")) return FALLBACK_COLOR_MAP.nau;
  if (keyByName.includes("trang")) return FALLBACK_COLOR_MAP.trang;
  if (keyByName.includes("den")) return FALLBACK_COLOR_MAP.den;

  return "#d1d5db";
}

function getColorCode(colorId) {
  if (!colorId) return "#d1d5db";

  const foundById = allColors.value.find(
    (item) => String(item.id) === String(colorId),
  );

  if (foundById?.code) return foundById.code;

  const foundByName = allColors.value.find(
    (item) => normalizeText(item.name) === normalizeText(colorId),
  );

  return foundByName?.code || resolveColorCode("", colorId);
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

function baseProductName(item) {
  return item.tenSanPham || item.name || item.title || "Sản phẩm";
}

function baseProductCode(item) {
  return item.maSanPham || item.code || "";
}

function getCategoryLabel(product) {
  const productCategoryId =
    product.idLoaiSanPham || product.loaiSanPhamId || product.loaiSanPham?.id || null;

  const found = categoryOptions.value.find(
    (item) => String(item.id) === String(productCategoryId),
  );

  return found?.label || product.loaiSanPham?.tenLoaiSanPham || product.loaiSanPham?.ten || "Sản phẩm";
}

function slugify(text) {
  return normalizeText(text)
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
}

function syncFilterFromQuery() {
  const q = String(route.query.q || "").trim();
  const cat = String(route.query.cat || "").trim();

  keyword.value = q;

  if (!cat) {
    selectedCategory.value = "";
    return;
  }

  const matchedCategory = categoryOptions.value.find((item) => {
    return slugify(item.label) === cat;
  });

  selectedCategory.value = matchedCategory ? String(matchedCategory.id) : "";
}

function mapVariant(v) {
  const colorObj = typeof v.mauSac === "object" ? v.mauSac : null;

  const colorId =
    v.idMauSac ??
    v.id_mau_sac ??
    colorObj?.id ??
    null;

  const colorName =
    v.tenMauSac ||
    v.ten_mau_sac ||
    colorObj?.ten ||
    colorObj?.name ||
    v.mau ||
    "";

  const dbColor = dbColors.value.find(
    (item) => String(item.id) === String(colorId),
  );

  const colorCode = resolveColorCode(
    v.maMauSac ||
      v.ma_mau_sac ||
      colorObj?.ma ||
      colorObj?.code ||
      dbColor?.ma ||
      dbColor?.code ||
      "",
    dbColor?.ten || colorName,
  );

  const sizeName =
    normalizeSizeName(v.kichCo) ||
    normalizeSizeName(v.tenKichCo) ||
    normalizeSizeName(v.size) ||
    "";

  return {
    idSanPhamChiTiet: v.id,
    colorId: colorId != null ? String(colorId) : "",
    color: colorName,
    colorCode,
    size: sizeName,
    price: Number(v.donGia ?? v.giaBan ?? v.price ?? 0),
    stock: Number(v.soLuongTon ?? v.soLuong ?? 0),
    image: pickVariantImage(v, fallbackImage),
    code: v.maSanPhamChiTiet || v.code || "",
  };
}

function enrichProduct(product, variants) {
  const mappedVariants = (variants || []).map(mapVariant);

  const availableVariants = mappedVariants.filter(
    (v) => Number(v.stock || 0) > 0,
  );

  const prices = availableVariants
    .map((v) => Number(v.price || 0))
    .filter((p) => p > 0);

  const colorMap = new Map();

  availableVariants.forEach((v) => {
    const id = String(v.colorId || "");
    const name = String(v.color || "").trim();

    if (!id || !name) return;

    if (!colorMap.has(id)) {
      const dbColor = dbColors.value.find(
        (item) => String(item.id) === String(id),
      );

      colorMap.set(id, {
        id,
        name: dbColor?.ten || name,
        code: resolveColorCode(dbColor?.ma || v.colorCode || "", dbColor?.ten || name),
      });
    }
  });

  const colorOptions = [...colorMap.values()];
  const colors = colorOptions.map((item) => item.name);
  const colorIds = colorOptions.map((item) => String(item.id));

  const sizes = [
    ...new Set(availableVariants.map((v) => v.size).filter(Boolean)),
  ].sort(sortSize);

  const totalStock = availableVariants.reduce(
    (sum, v) => sum + Number(v.stock || 0),
    0,
  );

  return {
    ...product,
    variants: availableVariants,
    displayName: baseProductName(product),
    displayImage: pickProductImage(product, availableVariants, fallbackImage),
    displayPrice:
      prices.length > 0
        ? Math.min(...prices)
        : Number(product.giaBan || product.price || 0),
    colors,
    colorOptions,
    colorIds,
    sizes,
    totalStock,
    code: baseProductCode(product),
    categoryLabel: getCategoryLabel(product),
  };
}

function toggleCombo(name) {
  openedCombo.value = openedCombo.value === name ? "" : name;
}

function pickCategory(value) {
  selectedCategory.value = String(value || "");
  openedCombo.value = "";
  categoryQuery.value = "";
}

function pickColor(value) {
  selectedColor.value = value || "";
  openedCombo.value = "";
  colorQuery.value = "";
}

function pickSize(value) {
  selectedSize.value = value || "";
  openedCombo.value = "";
  sizeQuery.value = "";
}

function closeCombo() {
  openedCombo.value = "";
}

function money(v) {
  const n = Number(v) || 0;
  return n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
}

function onImgError(e) {
  e.target.src = fallbackImage;
}

function goDetail(id) {
  router.push({ name: "ProductDetail", params: { id } });
}

function resetFilter() {
  keyword.value = "";
  selectedCategory.value = "";
  selectedColor.value = "";
  selectedSize.value = "";
  selectedStock.value = "all";
  sortBy.value = "newest";
  minPriceFilter.value = 0;
  maxPriceFilter.value = safeMaxDbPrice.value;
  categoryQuery.value = "";
  colorQuery.value = "";
  sizeQuery.value = "";
  openedCombo.value = "";

  router.push({ name: "Search", query: {} });
}

async function fetchCategories() {
  try {
    const data = await getLoaiSanPhamList();
    categories.value = Array.isArray(data) ? data : [];
  } catch (err) {
    console.error("fetchCategories error:", err);
  }
}

async function fetchColors() {
  try {
    const data = await getMauSacList();
    dbColors.value = Array.isArray(data) ? data : [];
  } catch (err) {
    console.error("fetchColors error:", err);
    dbColors.value = [];
  }
}

async function fetchMaxPrice() {
  try {
    const data = await getGiaMaxDb();
    const max = Number(data) || 10000000;

    maxDbPrice.value = max;
    maxPriceFilter.value = max;
  } catch (err) {
    console.error("fetchMaxPrice error:", err);
    maxDbPrice.value = 10000000;
    maxPriceFilter.value = 10000000;
  }
}

async function fetchProductsData() {
  try {
    loading.value = true;
    error.value = "";

    const data = await getProducts({
      page: 0,
      size: 1000,
    });

    if (Array.isArray(data)) {
      rawProducts.value = data;
    } else {
      rawProducts.value = Array.isArray(data?.content) ? data.content : [];
    }

    const enriched = await Promise.all(
      rawProducts.value.map(async (product) => {
        try {
          const variants = await getProductVariantsByProductId(product.id);
          return enrichProduct(
            product,
            Array.isArray(variants) ? variants : [],
          );
        } catch (variantErr) {
          console.error("fetch variants error:", product.id, variantErr);
          return enrichProduct(product, []);
        }
      }),
    );

    products.value = sortNewestFirst(enriched);
  } catch (err) {
    console.error("fetchProductsData error:", err);
    console.error("fetchProductsData response:", err?.response?.data);

    error.value =
      err?.response?.data?.message ||
      `Không tải được danh sách sản phẩm (${err?.response?.status || "no-status"})`;
  } finally {
    loading.value = false;
  }
}

function changePage(nextPage) {
  if (nextPage < 0 || nextPage >= totalPages.value) return;
  page.value = nextPage;
  window.scrollTo({ top: 0, behavior: "smooth" });
}

onMounted(async () => {
  document.addEventListener("click", closeCombo);

  await Promise.all([fetchCategories(), fetchColors(), fetchMaxPrice()]);

  syncFilterFromQuery();
  await fetchProductsData();
});

onBeforeUnmount(() => {
  document.removeEventListener("click", closeCombo);
});
</script>

<style scoped>
.search-page,
.search-page input,
.search-page button,
.search-page select,
.search-page textarea {
  font-family: var(--bs-body-font-family, system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif);
}

.search-page {
  min-height: 100vh;
  background: #f5f7fb;
  color: #111827;
}

.search-hero {
  position: relative;
  overflow: hidden;
  padding: 34px 0 88px;
  background: linear-gradient(135deg, #06164d 0%, #0a2168 52%, #143c9f 100%);
}

.search-hero::before,
.search-hero::after {
  content: "";
  position: absolute;
  border-radius: 999px;
  pointer-events: none;
  opacity: 0.35;
}

.search-hero::before {
  width: 340px;
  height: 340px;
  right: -120px;
  top: -150px;
  background: rgba(96, 165, 250, 0.2);
}

.search-hero::after {
  width: 260px;
  height: 260px;
  left: 8%;
  bottom: -150px;
  background: rgba(59, 130, 246, 0.18);
}

.hero-card {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 24px;
  align-items: stretch;
  padding: 28px 30px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(16px);
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  padding: 7px 13px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  color: #ffffff;
  font-size: 12px;
  font-weight: 700;
}

.hero-title {
  margin: 14px 0 12px;
  color: #ffffff;
  font-size: 42px;
  line-height: 1.08;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.hero-desc {
  max-width: 620px;
  margin: 0;
  color: rgba(255, 255, 255, 0.82);
  font-size: 15px;
  line-height: 1.7;
}

.hero-stats {
  display: grid;
  gap: 10px;
}

.hero-stat {
  padding: 15px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.hero-stat strong {
  display: block;
  color: #ffffff;
  font-size: 28px;
  line-height: 1;
  font-weight: 800;
}

.hero-stat span {
  display: block;
  margin-top: 7px;
  color: rgba(255, 255, 255, 0.76);
  font-size: 13px;
  font-weight: 500;
}

.product-section {
  margin-top: -54px;
  padding-bottom: 56px;
  position: relative;
  z-index: 3;
}

.filter-box,
.product-toolbar,
.product-card,
.state-box {
  border-radius: 20px;
  border: 1px solid #e5eaf2;
  background: #ffffff;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.06);
}

.filter-box {
  position: sticky;
  top: 20px;
  padding: 18px;
}

.filter-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 20px;
}

.filter-subtitle,
.toolbar-subtitle {
  margin-bottom: 5px;
  color: #2563eb;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.filter-title {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
  font-weight: 750;
  letter-spacing: -0.01em;
}

.filter-clear-mini {
  border: 0;
  padding: 7px 11px;
  border-radius: 999px;
  color: #1d4ed8;
  background: #dbeafe;
  font-size: 12px;
  font-weight: 700;
}

.filter-group {
  margin-bottom: 18px;
}

.filter-label {
  display: block;
  margin-bottom: 8px;
  color: #0f172a;
  font-size: 13px;
  font-weight: 700;
}

.filter-input-icon {
  height: 42px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 14px;
  border-radius: 13px;
  background: #f8fafc;
  border: 1px solid #d8e0ea;
}

.filter-input-icon i {
  color: #64748b;
}

.filter-input {
  width: 100%;
  border: 0;
  outline: 0;
  color: #0f172a;
  background: transparent;
  font-size: 14px;
  font-weight: 500;
}

.combo-box {
  position: relative;
}

.combo-trigger {
  width: 100%;
  min-height: 42px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 0 14px;
  border-radius: 13px;
  color: #334155;
  background: #f8fafc;
  border: 1px solid #d8e0ea;
  font-size: 13px;
  font-weight: 500;
  text-align: left;
  transition: all 0.2s ease;
}

.combo-trigger.active {
  color: #0f172a;
  background: #f8fafc;
  border-color: #b8c7dc;
}

.combo-trigger:hover {
  border-color: rgba(37, 99, 235, 0.35);
}

.combo-color-label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.combo-menu {
  position: absolute;
  left: 0;
  right: 0;
  top: calc(100% + 8px);
  z-index: 50;
  max-height: 300px;
  overflow: auto;
  padding: 10px;
  border-radius: 16px;
  background: #ffffff;
  border: 1px solid rgba(148, 163, 184, 0.28);
  box-shadow: 0 18px 38px rgba(15, 23, 42, 0.14);
}

.combo-search {
  height: 42px;
  display: flex;
  align-items: center;
  gap: 9px;
  margin-bottom: 8px;
  padding: 0 12px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid rgba(148, 163, 184, 0.22);
}

.combo-search i {
  color: #64748b;
}

.combo-search input {
  width: 100%;
  border: 0;
  outline: 0;
  background: transparent;
  color: #0f172a;
  font-size: 13px;
  font-weight: 500;
}

.combo-option {
  width: 100%;
  min-height: 38px;
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 0 11px;
  border: 0;
  border-radius: 12px;
  color: #334155;
  background: transparent;
  font-size: 13px;
  font-weight: 500;
  text-align: left;
  transition: all 0.16s ease;
}

.combo-option:hover,
.combo-option.selected {
  color: #1d4ed8;
  background: #eff6ff;
}

.combo-empty {
  padding: 12px 8px 4px;
  color: #94a3b8;
  font-size: 13px;
  font-weight: 700;
  text-align: center;
}

.color-dot {
  width: 15px;
  height: 15px;
  flex: 0 0 auto;
  display: inline-block;
  border-radius: 999px;
  border: 1px solid rgba(15, 23, 42, 0.16);
}

.price-box {
  padding: 12px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid #d8e0ea;
}

.price-inputs {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.price-field {
  padding: 10px 12px;
  border-radius: 14px;
  background: #ffffff;
  border: 1px solid #d8e0ea;
}

.price-field span {
  display: block;
  margin-bottom: 4px;
  color: #64748b;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
}

.price-input {
  width: 100%;
  border: 0;
  outline: 0;
  background: transparent;
  color: #0f172a;
  font-size: 15px;
  font-weight: 700;
}

.price-slider {
  width: 100%;
  margin-top: 12px;
  accent-color: #2563eb;
}

.price-range-text {
  margin-top: 8px;
  color: #334155;
  font-size: 13px;
  font-weight: 700;
}

.stock-filter {
  display: grid;
  grid-template-columns: 1fr;
  gap: 8px;
}

.stock-btn {
  min-height: 38px;
  border: 1px solid #d8e0ea;
  border-radius: 12px;
  color: #334155;
  background: #f8fafc;
  font-size: 13px;
  font-weight: 650;
  transition: all 0.2s ease;
}

.stock-btn.active {
  color: #ffffff;
  background: #07143f;
  border-color: #07143f;
}

.reset-btn {
  width: 100%;
  min-height: 42px;
  border: 0;
  border-radius: 13px;
  color: #ffffff;
  background: #07143f;
  font-size: 13px;
  font-weight: 700;
  box-shadow: none;
  transition: all 0.2s ease;
}

.reset-btn:hover {
  transform: translateY(-2px);
  background: #0b1b55;
}

.product-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 18px;
  padding: 18px 20px;
}

.toolbar-title {
  margin: 0;
  color: #0f172a;
  font-size: 24px;
  font-weight: 750;
  letter-spacing: -0.01em;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.result-count {
  padding: 9px 13px;
  border-radius: 999px;
  color: #1d4ed8;
  background: #eff6ff;
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}

.sort-select {
  min-height: 40px;
  padding: 0 38px 0 13px;
  border-radius: 12px;
  color: #0f172a;
  background: #ffffff;
  border: 1px solid rgba(148, 163, 184, 0.32);
  font-size: 13px;
  font-weight: 500;
  outline: 0;
}

.active-filter-bar {
  display: flex;
  align-items: center;
  gap: 9px;
  flex-wrap: wrap;
  margin-bottom: 18px;
  padding: 12px 14px;
  border-radius: 20px;
  background: #ffffff;
  border: 1px solid rgba(148, 163, 184, 0.2);
}

.active-filter-bar span {
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
}

.active-filter-bar button {
  min-height: 32px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 0;
  border-radius: 999px;
  color: #0f172a;
  background: #f1f5f9;
  padding: 0 11px;
  font-size: 12px;
  font-weight: 700;
}

.state-box {
  min-height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 28px;
  color: #475569;
  font-weight: 700;
}

.state-box-error {
  color: #b91c1c;
  background: #fff1f2;
}

.state-spinner {
  width: 26px;
  height: 26px;
  border: 3px solid rgba(37, 99, 235, 0.18);
  border-top-color: #2563eb;
  border-radius: 999px;
  animation: spin 0.8s linear infinite;
}

.empty-state {
  flex-direction: column;
  text-align: center;
}

.empty-icon {
  width: 70px;
  height: 70px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 24px;
  color: #1d4ed8;
  background: #dbeafe;
  font-size: 30px;
}

.empty-state h5 {
  margin: 6px 0 0;
  color: #0f172a;
  font-weight: 750;
}

.empty-state p {
  margin: 0;
  color: #64748b;
  font-weight: 500;
}

.reset-btn-inline {
  width: auto;
  min-width: 150px;
  padding: 0 18px;
}

.product-card {
  height: 100%;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 38px rgba(15, 23, 42, 0.11);
}

.product-image-wrap {
  position: relative;
  overflow: hidden;
  height: 250px;
  background: #f3f6fb;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.28s ease;
}

.product-card:hover .product-img {
  transform: scale(1.04);
}

.product-badges {
  position: absolute;
  left: 10px;
  right: auto;
  top: 10px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 8px;
}

.badge-soft {
  min-height: 26px;
  display: inline-flex;
  align-items: center;
  padding: 0 9px;
  border-radius: 999px;
  color: #0f172a;
  background: rgba(255, 255, 255, 0.9);
  font-size: 11px;
  font-weight: 700;
  backdrop-filter: blur(10px);
}

.product-body {
  padding: 15px;
}

.product-code {
  margin-bottom: 7px;
  color: #2563eb;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.product-name {
  min-height: 48px;
  margin: 0 0 12px;
  color: #0f172a;
  font-size: 17px;
  line-height: 1.35;
  font-weight: 750;
  letter-spacing: 0;
}

.product-price-row {
  display: block;
  margin-top: 12px;
}

.price-label {
  display: block;
  margin-bottom: 3px;
  color: #94a3b8;
  font-size: 11px;
  font-weight: 650;
}

.product-price {
  color: #dc2626;
  font-size: 21px;
  line-height: 1.1;
  font-weight: 800;
  letter-spacing: 0;
}

.meta-block {
  margin-top: 12px;
}

.meta-label {
  margin-bottom: 6px;
  color: #64748b;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.chip-wrap {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 7px;
}

.color-chip {
  width: 21px;
  height: 21px;
  padding: 0;
  border-radius: 999px;
  border: 2px solid #ffffff;
  box-shadow: 0 0 0 1px rgba(148, 163, 184, 0.5);
}

.size-chip,
.chip-more {
  min-height: 24px;
  display: inline-flex;
  align-items: center;
  padding: 0 9px;
  border-radius: 999px;
  color: #0f172a;
  background: #f1f5f9;
  font-size: 11px;
  font-weight: 700;
}

.chip-more {
  color: #1d4ed8;
  background: #dbeafe;
}

.product-extra {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 7px;
  margin-top: 13px;
}

.product-extra div {
  padding: 8px 6px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid rgba(148, 163, 184, 0.16);
  text-align: center;
}

.product-extra span {
  display: block;
  margin-bottom: 3px;
  color: #94a3b8;
  font-size: 10px;
  font-weight: 650;
}

.product-extra strong {
  color: #0f172a;
  font-size: 13px;
  font-weight: 750;
}

.detail-btn {
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
  font-size: 13px;
  font-weight: 700;
  box-shadow: none;
  transition: all 0.2s ease;
}

.detail-btn:hover {
  background: #0b1b55;
  transform: translateY(-2px);
}

.pagination-wrap {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 9px;
  flex-wrap: wrap;
}

.page-number,
.page-arrow {
  width: 38px;
  height: 38px;
  border: 1px solid rgba(148, 163, 184, 0.22);
  background: #ffffff;
  color: #0f172a;
  border-radius: 12px;
  font-weight: 700;
  transition: all 0.2s ease;
}

.page-number:hover,
.page-arrow:hover:not(:disabled) {
  color: #1d4ed8;
  background: #eff6ff;
  border-color: rgba(37, 99, 235, 0.26);
}

.page-number.active {
  background: #07143f;
  color: #ffffff;
  border-color: #07143f;
}

.page-arrow:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 1199.98px) {
  .hero-card {
    grid-template-columns: 1fr;
  }

  .hero-stats {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 991.98px) {
  .filter-box {
    position: static;
  }

  .product-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .toolbar-actions {
    width: 100%;
    justify-content: space-between;
  }
}

@media (max-width: 767.98px) {
  .search-hero {
    padding: 28px 0 70px;
  }

  .hero-card {
    grid-template-columns: 1fr;
    padding: 22px;
    border-radius: 20px;
  }

  .hero-title {
    font-size: 31px;
  }

  .hero-stats {
    grid-template-columns: 1fr;
  }

  .product-section {
    margin-top: -42px;
  }

  .toolbar-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .result-count,
  .sort-select {
    width: 100%;
  }

  .product-image-wrap {
    height: 240px;
  }

  .price-inputs,
  .product-extra {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 575.98px) {
  .filter-box,
  .product-toolbar,
  .product-card,
  .state-box {
    border-radius: 18px;
  }
}
</style>