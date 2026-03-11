<template>
  <div class="search-page container py-4">
    <nav aria-label="breadcrumb" class="mb-3">
      <ol class="breadcrumb mb-0">
        <li class="breadcrumb-item">
          <router-link to="/" class="text-muted text-decoration-none">
            Trang chủ
          </router-link>
        </li>
        <li class="breadcrumb-item active text-dark" aria-current="page">
          Sản phẩm
        </li>
      </ol>
    </nav>

    <div class="row g-4">
      <div class="col-lg-3">
        <div class="filter-box">
          <div class="filter-title">Bộ lọc</div>

          <div class="mb-3">
            <label class="form-label">Tìm kiếm</label>
            <input
              v-model="keyword"
              type="text"
              class="form-control"
              placeholder="Tên sản phẩm..."
            />
          </div>

          <div class="mb-3">
            <label class="form-label">Loại sản phẩm</label>
            <select v-model="selectedCategory" class="form-select">
              <option value="">Tất cả</option>
              <option
                v-for="item in categories"
                :key="item.id"
                :value="item.id"
              >
                {{
                  item.tenLoaiSanPham ||
                  item.name ||
                  item.ten ||
                  `Loại ${item.id}`
                }}
              </option>
            </select>
          </div>

          <div class="mb-3">
            <label class="form-label">Kích thước</label>
            <select v-model="selectedSize" class="form-select">
              <option value="">Tất cả</option>
              <option v-for="size in allSizes" :key="size" :value="size">
                {{ size }}
              </option>
            </select>
          </div>

          <div class="mb-3">
            <label class="form-label">Giá tối đa</label>
            <input
              v-model.number="maxPriceFilter"
              type="range"
              class="form-range"
              min="0"
              :max="safeMaxDbPrice"
              step="10000"
            />
            <div class="small text-muted">{{ money(maxPriceFilter) }} đ</div>
          </div>

          <button class="btn btn-dark w-100" type="button" @click="resetFilter">
            Xóa bộ lọc
          </button>
        </div>
      </div>

      <div class="col-lg-9">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h1 class="page-title mb-0">Danh sách sản phẩm</h1>
          <div class="text-muted small">
            {{ filteredProducts.length }} sản phẩm
          </div>
        </div>

        <div v-if="loading" class="state-box">Đang tải sản phẩm...</div>

        <div v-else-if="error" class="state-box text-danger">
          {{ error }}
        </div>

        <div v-else-if="filteredProducts.length === 0" class="state-box">
          Không có sản phẩm phù hợp.
        </div>

        <div v-else class="row g-4">
          <div
  v-for="item in pagedProducts"
  :key="item.id"
  class="col-sm-6 col-xl-4"
>

            <div class="product-card" @click="goDetail(item.id)">
              <div class="product-image-wrap">
                <img
                  :src="item.displayImage"
                  class="product-img"
                  :alt="item.displayName"
                  @error="onImgError"
                />
              </div>

              <div class="product-body">
                <div class="product-name">
                  {{ item.displayName }}
                </div>

                <div class="product-desc">
                  {{ item.displayDescription }}
                </div>

                <div class="product-price">
                  {{ money(item.displayPrice) }} đ
                </div>

                <div v-if="item.colors.length" class="meta-block">
                  <div class="meta-label">Màu sắc</div>
                  <div class="chip-wrap">
                    <span
                      v-for="color in item.colors.slice(0, 4)"
                      :key="color"
                      class="chip color-chip"
                      :title="color"
                      :style="{ backgroundColor: getColorCode(color) }"
                    ></span>
                  </div>
                </div>

                <div v-if="item.sizes.length" class="meta-block">
                  <div class="meta-label">Kích thước</div>
                  <div class="chip-wrap">
                    <span
                      v-for="size in item.sizes.slice(0, 5)"
                      :key="size"
                      class="chip chip-light"
                    >
                      {{ size }}
                    </span>
                  </div>
                </div>

                <div class="product-footer">
                
<button
  class="btn-theme btn-theme-sm"
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
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import {
  getProducts,
  getGiaMaxDb,
  getLoaiSanPhamList,
  getProductVariantsByProductId,
} from "../../services/productClientApi";
import {
  pickProductImage,
  pickVariantImage,
  resolveMediaUrl,
  sortNewestFirst,
} from "../../utils/media";
const router = useRouter();
const route = useRoute();

const loading = ref(false);
const error = ref("");

const rawProducts = ref([]);
const products = ref([]);
const categories = ref([]);

const page = ref(0);
const size = ref(12);
const totalPages = ref(0);

const keyword = ref("");
const selectedCategory = ref("");
const selectedColor = ref("");
const selectedSize = ref("");
const maxDbPrice = ref(0);
const maxPriceFilter = ref(0);

const fallbackImage =
  "data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='400' height='460'%3E%3Crect width='100%25' height='100%25' fill='%23f1f3f5'/%3E%3Ctext x='50%25' y='52%25' dominant-baseline='middle' text-anchor='middle' fill='%2399a1aa' font-size='18'%3ENo Image%3C/text%3E%3C/svg%3E";

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
  if (key.includes("navy") || key.includes("than"))
    return COLOR_MAP["xanh navy"];
  if (key.includes("xanh") && key.includes("la")) return COLOR_MAP["xanh la"];
  if (key.includes("xanh") && key.includes("duong"))
    return COLOR_MAP["xanh duong"];
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

function baseProductName(item) {
  return item.tenSanPham || item.name || item.title || "Sản phẩm";
}

function baseProductDesc(item) {
  return item.moTa || item.description || "";
}
function slugify(text) {
  return String(text || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .trim()
    .replace(/đ/g, "d")
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

  const matchedCategory = (categories.value || []).find((item) => {
    const name =
      item.tenLoaiSanPham || item.name || item.ten || `Loai ${item.id}`;
    return slugify(name) === cat;
  });

  selectedCategory.value = matchedCategory ? String(matchedCategory.id) : "";
}
watch(
  () => route.query,
  async () => {
    page.value = 0;
    syncFilterFromQuery();
    await fetchProductsData();
  },
  { deep: true },
);
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

  return {
    idSanPhamChiTiet: v.id,
    color: colorName,
    size: sizeName,
    price: Number(v.donGia ?? v.giaBan ?? v.price ?? 0),
    stock: Number(v.soLuongTon ?? v.soLuong ?? 0),
    image: pickVariantImage(v, fallbackImage),
    code: v.maSanPhamChiTiet || v.code || "",
  };
}

function enrichProduct(product, variants) {
  const mappedVariants = (variants || []).map(mapVariant);

  const prices = mappedVariants
    .map((v) => Number(v.price || 0))
    .filter((p) => p > 0);

  const images = mappedVariants.map((v) => v.image).filter(Boolean);

  const colors = [
    ...new Set(mappedVariants.map((v) => v.color).filter(Boolean)),
  ];
  const sizes = [...new Set(mappedVariants.map((v) => v.size).filter(Boolean))];
  const totalStock = mappedVariants.reduce(
    (sum, v) => sum + Number(v.stock || 0),
    0,
  );

  return {
    ...product,
    variants: mappedVariants,
    displayName: baseProductName(product),
    displayDescription: baseProductDesc(product),
    displayImage: pickProductImage(product, mappedVariants, fallbackImage),
    displayPrice:
      prices.length > 0
        ? Math.min(...prices)
        : Number(product.giaBan || product.price || 0),
    colors,
    sizes,
    totalStock,
  };
}

const safeMaxDbPrice = computed(() => {
  const v = Number(maxDbPrice.value) || 0;
  return v > 0 ? v : 10000000;
});

const allColors = computed(() => {
  return [...new Set(products.value.flatMap((p) => p.colors || []))];
});

const allSizes = computed(() => {
  return [...new Set(products.value.flatMap((p) => p.sizes || []))];
});

const filteredProducts = computed(() => {
  return (products.value || []).filter((item) => {
    const name = String(item.displayName || "").toLowerCase();
    const kw = keyword.value.trim().toLowerCase();

    const matchKeyword = !kw || name.includes(kw);

    const productCategoryId =
      item.idLoaiSanPham || item.loaiSanPhamId || item.loaiSanPham?.id || null;

    const matchCategory =
      !selectedCategory.value ||
      String(productCategoryId) === String(selectedCategory.value);

    const matchColor =
      !selectedColor.value || (item.colors || []).includes(selectedColor.value);

    const matchSize =
      !selectedSize.value || (item.sizes || []).includes(selectedSize.value);

    const matchPrice =
      Number(item.displayPrice || 0) <= Number(maxPriceFilter.value || 0);

    return (
      matchKeyword && matchCategory && matchColor && matchSize && matchPrice
    );
  });
});

const pagedProducts = computed(() => {
  const start = page.value * size.value;
  const end = start + size.value;
  return filteredProducts.value.slice(start, end);
});
watch(
  filteredProducts,
  (list) => {
    totalPages.value = Math.max(1, Math.ceil(list.length / size.value));

    if (page.value >= totalPages.value) {
      page.value = 0;
    }
  },
  { immediate: true },
);

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
  maxPriceFilter.value = safeMaxDbPrice.value;

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

async function fetchMaxPrice() {
  try {
    const data = await getGiaMaxDb();
    maxDbPrice.value = Number(data) || 0;
    maxPriceFilter.value = Number(data) || 0;
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
}



onMounted(async () => {
  await Promise.all([fetchCategories(), fetchMaxPrice()]);

  syncFilterFromQuery();
  await fetchProductsData();
});
</script>

<style scoped>
.search-page {
  background: #f7f7f7;
  min-height: 100vh;
}

.page-title {
  font-size: 38px;
  font-weight: 750;
  color: #111;
  margin: 0;
}

.filter-box {
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 14px;
  padding: 16px;
  background: #fff;
  position: sticky;
  top: 20px;
}

.filter-title {
  font-size: 24px;
  font-weight: 750;
  margin-bottom: 16px;
  color: #111;
}

.form-label {
  font-size: 14px;
  font-weight: 700;
  color: #111;
}

.state-box {
  padding: 24px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 14px;
  background: #fff;
}

.product-card {
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  height: 100%;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.product-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.08);
}

.product-image-wrap {
  background: #f3f4f6;
}

.product-img {
  width: 100%;
  height: 320px;
  object-fit: cover;
  display: block;
  background: #f1f3f5;
}

.product-body {
  padding: 16px;
}

.product-name {
  font-size: 18px;
  font-weight: 750;
  color: #111;
  line-height: 1.35;
  min-height: 48px;
}

.product-desc {
  font-size: 14px;
  color: #6c757d;
  margin-top: 8px;
  min-height: 42px;
  line-height: 1.5;
}

.product-price {
  font-size: 24px;
  font-weight: 750;
  color: #c1121f;
  margin-top: 12px;
}

.meta-block {
  margin-top: 12px;
}

.meta-label {
  font-size: 12px;
  font-weight: 700;
  color: #6c757d;
  margin-bottom: 6px;
  text-transform: uppercase;
}

.chip-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.chip {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  background: #111827;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}

.chip-light {
  background: #eef2f7;
  color: #111;
}

.product-footer {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.stock-text {
  font-size: 13px;
  color: #6c757d;
}

@media (max-width: 575.98px) {
  .page-title {
    font-size: 28px;
  }

  .product-img {
    height: 260px;
  }
}
.color-chip {
  width: 20px;
  height: 20px;
  padding: 0;
  border-radius: 999px;
  border: 1px solid #d1d5db;
  display: inline-block;
}
.btn-theme,
.btn-theme-sm {
  border: none;
  background: #000f51;
  color: #fff;
  font-weight: 750;
  border-radius: 14px;
  transition: all 0.2s ease;
  box-shadow: 0 14px 28px rgba(0, 15, 81, 0.18);
}

.btn-theme {
  min-height: 46px;
  padding: 0 18px;
  width: 100%;
}

.btn-theme-sm {
  min-height: 36px;
  padding: 0 14px;
}

.btn-theme:hover,
.btn-theme-sm:hover {
  background: #001a72;
  color: #fff;
}
.pagination-wrap {
  margin-top: 28px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.page-number,
.page-arrow {
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  color: #111;
  border-radius: 10px;
  font-weight: 700;
  transition: all 0.2s ease;
}

.page-number:hover,
.page-arrow:hover:not(:disabled) {
  background: #f1f5f9;
}

.page-number.active {
  background: #000;
  color: #fff;
}

.page-arrow:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

</style>
