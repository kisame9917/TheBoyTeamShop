<template>
  <div class="discount-page">
    <section class="discount-hero">
      <div class="hero-bg hero-bg--one"></div>
      <div class="hero-bg hero-bg--two"></div>

      <div class="container position-relative">
        <div class="hero-grid">
          <div class="hero-content">
            <span class="hero-badge">
              <i class="bi bi-stars me-2"></i>
              Ưu đãi công khai đang áp dụng
            </span>

            <h1 class="hero-title">
              MÃ GIẢM GIÁ
              <span>HOT DEAL</span>
            </h1>

            <p class="hero-desc">
              Danh sách mã giảm giá công khai còn hiệu lực, có thể sao chép
              nhanh và áp dụng khi thanh toán đơn hàng.
            </p>

            <div class="hero-actions">
              <button
                class="hero-btn hero-btn--primary"
                @click="scrollToVoucherList"
              >
                <i class="bi bi-ticket-perforated me-2"></i>
                Xem mã giảm giá
              </button>

              <button class="hero-btn hero-btn--outline" @click="goShop">
                Mua sắm ngay
                <i class="bi bi-arrow-right ms-2"></i>
              </button>
            </div>
          </div>

          <div class="hero-panel">
            <div class="hero-panel__icon">
              <i class="bi bi-ticket-detailed"></i>
            </div>

            <div class="hero-panel__main">
              <span>Đang có</span>
              <strong>{{ publicActiveCount }}</strong>
              <p>mã công khai dùng được</p>
            </div>

            <div class="hero-panel__note">
              <i class="bi bi-shield-check"></i>
              Mã giảm giá của bạn ❤️
            </div>
          </div>
        </div>
      </div>
    </section>

    <section ref="voucherListRef" class="container voucher-section">
      <div class="section-head">
        <div>
          <div class="section-subtitle">Danh sách ưu đãi</div>
          <h4 class="section-title mb-0">
            {{
              activeTab === "public"
                ? "Mã công khai đang áp dụng"
                : "Mã giảm giá của tôi"
            }}
          </h4>
        </div>

        <div class="voucher-toolbar">
          <div class="tab-group">
            <button
              type="button"
              class="tab-btn"
              :class="{ active: activeTab === 'public' }"
              @click="activeTab = 'public'"
            >
              Công khai
              <span>{{ publicActiveCount }}</span>
            </button>

            <button
              type="button"
              class="tab-btn"
              :class="{ active: activeTab === 'mine' }"
              @click="activeTab = 'mine'"
            >
              Của tôi
              <span>{{ myVoucherCount }}</span>
            </button>
          </div>

          <div class="search-box">
            <i class="bi bi-search"></i>
            <input
              v-model.trim="keyword"
              type="text"
              placeholder="Tìm mã giảm giá..."
            />
          </div>
        </div>
      </div>

      <div class="info-strip">
        <div>
          Các mã giảm giá được cập nhật tự động từ hệ thống, có thể áp dụng cho
          đơn hàng nếu còn hiệu lực.
        </div>
        <span>{{ filteredVouchers.length }} mã phù hợp</span>
      </div>

      <div v-if="loading" class="state-box">
        <div class="state-spinner"></div>
        <div>Đang tải mã giảm giá...</div>
      </div>

      <div v-else-if="error" class="state-box state-box--error">
        <i class="bi bi-exclamation-triangle"></i>
        <div>{{ error }}</div>
      </div>

      <template v-else>
        <div v-if="filteredVouchers.length" class="row g-4">
          <div
            v-for="voucher in pagedVouchers"
            :key="voucher.id || voucher.maGiamGia"
            class="col-xl-4 col-md-6"
          >
            <article
              class="voucher-card"
              :class="{ 'voucher-card--mine': isPrivateVoucher(voucher) }"
            >
              <div class="voucher-card__shine"></div>

              <div class="voucher-card__head">
                <div>
                  <div class="voucher-label">
                    {{
                      isPrivateVoucher(voucher)
                        ? "Personal voucher"
                        : "Public voucher"
                    }}
                  </div>
                  <h5 class="voucher-name">
                    {{ voucher.tenGiamGia || "Ưu đãi giảm giá" }}
                  </h5>
                </div>

                <span class="voucher-status">
                  <i class="bi bi-check-circle-fill"></i>
                  Đang áp dụng
                </span>
              </div>

              <div class="voucher-ticket">
                <div class="voucher-ticket__left">
                  <span>Mã</span>
                  <strong>{{ voucher.maGiamGia }}</strong>
                </div>

                <button
                  class="copy-btn"
                  type="button"
                  @click="copyCode(voucher.maGiamGia)"
                >
                  <i class="bi bi-copy"></i>
                </button>
              </div>

              <div class="voucher-value">
                {{ formatDiscount(voucher) }}
              </div>

              <div class="voucher-meta">
                <div class="meta-item">
                  <span>Đơn tối thiểu</span>
                  <strong>{{ money(voucher.donHangToiThieu) }} đ</strong>
                </div>

                <div v-if="voucher.giaTriGiamToiDa" class="meta-item">
                  <span>Giảm tối đa</span>
                  <strong>{{ money(voucher.giaTriGiamToiDa) }} đ</strong>
                </div>

                <div v-if="voucher.soLuong !== null" class="meta-item">
                  <span>Số lượng còn</span>
                  <strong>{{ voucher.soLuong }}</strong>
                </div>

                <div class="meta-item meta-item--full">
                  <span>Hiệu lực</span>
                  <strong>
                    {{ formatDateTime(voucher.ngayBatDau) }}
                    -
                    {{ formatDateTime(voucher.ngayKetThuc) }}
                  </strong>
                </div>
              </div>

              <div class="voucher-actions">
                <button
                  class="voucher-btn voucher-btn--primary"
                  type="button"
                  @click="copyCode(voucher.maGiamGia)"
                >
                  Sao chép mã
                </button>

                <button
                  class="voucher-btn voucher-btn--outline"
                  type="button"
                  @click="goShop"
                >
                  Dùng ngay
                </button>
              </div>
            </article>
          </div>
        </div>
        <div
          v-if="filteredVouchers.length > pageSize"
          class="voucher-pagination"
        >
          <div class="voucher-page-info">
            Hiển thị {{ pagedVouchers.length }} /
            {{ filteredVouchers.length }} bản ghi
          </div>

          <div class="voucher-page-actions">
            <button
              type="button"
              class="voucher-page-btn"
              :disabled="currentPage <= 1"
              @click="prevVoucherPage"
            >
              ‹
            </button>

            <button
              v-for="page in voucherPageNumbers"
              :key="page"
              type="button"
              class="voucher-page-btn"
              :class="{ active: currentPage === page }"
              @click="goVoucherPage(page)"
            >
              {{ page }}
            </button>

            <button
              type="button"
              class="voucher-page-btn"
              :disabled="currentPage >= totalVoucherPages"
              @click="nextVoucherPage"
            >
              ›
            </button>
          </div>
        </div>
        <div v-else class="empty-box">
  <div class="empty-box__icon">
    <i :class="activeTab === 'mine' && !isLoggedIn ? 'bi bi-person-lock' : 'bi bi-ticket-perforated'"></i>
  </div>

  <template v-if="activeTab === 'mine' && !isLoggedIn">
    <h5>Đăng nhập để xem mã cá nhân</h5>
    <p>Mã giảm giá cá nhân sẽ được hiển thị sau khi bạn đăng nhập tài khoản.</p>

    <button class="hero-btn hero-btn--primary" @click="goLogin">
      Đăng nhập
    </button>
  </template>

  <template v-else-if="activeTab === 'mine'">
    <h5>Bạn chưa có mã giảm giá cá nhân</h5>
    <p>Hiện tại tài khoản của bạn chưa có mã cá nhân còn hiệu lực.</p>

    <button class="hero-btn hero-btn--primary" @click="goShop">
      Tiếp tục mua sắm
    </button>
  </template>

  <template v-else>
    <h5>Chưa có mã công khai đang áp dụng</h5>
    <p>
      Hiện tại hệ thống chưa có mã giảm giá công khai còn hiệu lực.
      Bạn có thể quay lại sau hoặc tiếp tục xem sản phẩm.
    </p>

    <button class="hero-btn hero-btn--primary" @click="goShop">
      Xem sản phẩm
    </button>
  </template>
</div>
      </template>
    </section>

    <section class="container pb-5">
      <div class="how-grid">
        <div class="how-card">
          <div class="how-step">1</div>
          <h5>Chọn mã phù hợp</h5>
          <p>Xem điều kiện đơn hàng tối thiểu và mức giảm của từng mã.</p>
        </div>

        <div class="how-card">
          <div class="how-step">2</div>
          <h5>Sao chép mã</h5>
          <p>Bấm sao chép để lưu mã và dùng nhanh tại trang thanh toán.</p>
        </div>

        <div class="how-card">
          <div class="how-step">3</div>
          <h5>Áp mã checkout</h5>
          <p>Hệ thống sẽ kiểm tra điều kiện và tính giảm giá nếu mã hợp lệ.</p>
        </div>
      </div>
    </section>

    <div v-if="copiedCode" class="copy-toast">
      <i class="bi bi-check-circle-fill me-2"></i>
      Đã sao chép mã:
      <strong>{{ copiedCode }}</strong>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const API_BASE = import.meta.env.VITE_API_BASE_URL || "";

const voucherListRef = ref(null);
const loading = ref(false);
const error = ref("");
const publicVouchers = ref([]);
const myVouchers = ref([]);
const activeTab = ref("public");
const keyword = ref("");
const copiedCode = ref("");
const currentPage = ref(1);
const pageSize = 15;
let copiedTimer = null;
const isLoggedIn = computed(() => {
  return (
    !!localStorage.getItem("USER_ACCESS_TOKEN") ||
    !!sessionStorage.getItem("USER_ACCESS_TOKEN") ||
    !!localStorage.getItem("vest_token")
  );
});

function safeJsonParse(value) {
  try {
    return value ? JSON.parse(value) : null;
  } catch {
    return null;
  }
}

function readTokenPayload(token) {
  try {
    if (!token || !token.includes(".")) return null;

    const base64 = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
    const json = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => `%${("00" + c.charCodeAt(0).toString(16)).slice(-2)}`)
        .join(""),
    );

    return JSON.parse(json);
  } catch {
    return null;
  }
}

function getStoredCustomerId() {
  const direct =
    localStorage.getItem("USER_ID") ||
    sessionStorage.getItem("USER_ID") ||
    localStorage.getItem("KHACH_HANG_ID") ||
    sessionStorage.getItem("KHACH_HANG_ID");

  if (direct && Number(direct) > 0) return Number(direct);

  const user =
    safeJsonParse(localStorage.getItem("vest_user")) ||
    safeJsonParse(sessionStorage.getItem("vest_user")) ||
    safeJsonParse(localStorage.getItem("user")) ||
    safeJsonParse(sessionStorage.getItem("user"));

  const userId =
    user?.idKhachHang ??
    user?.khachHangId ??
    user?.id_khach_hang ??
    user?.customerId ??
    user?.id;

  if (userId && Number(userId) > 0) return Number(userId);

  const token =
    localStorage.getItem("USER_ACCESS_TOKEN") ||
    sessionStorage.getItem("USER_ACCESS_TOKEN") ||
    localStorage.getItem("vest_token") ||
    sessionStorage.getItem("vest_token");

  const payload = readTokenPayload(token);

  const tokenId =
    payload?.idKhachHang ??
    payload?.khachHangId ??
    payload?.id_khach_hang ??
    payload?.customerId ??
    payload?.id ??
    payload?.sub;

  if (tokenId && Number(tokenId) > 0) return Number(tokenId);

  return null;
}

const khachHangId = computed(() => getStoredCustomerId());
const myVoucherCount = computed(() => myVouchers.value.length);

const currentVoucherList = computed(() => {
  if (activeTab.value === "mine") return myVouchers.value;
  return publicVouchers.value;
});
const publicActiveCount = computed(() => publicVouchers.value.length);

const filteredVouchers = computed(() => {
  const q = keyword.value.toLowerCase();

  if (!q) return currentVoucherList.value;

  return currentVoucherList.value.filter((item) => {
    return (
      String(item.maGiamGia || "")
        .toLowerCase()
        .includes(q) ||
      String(item.tenGiamGia || "")
        .toLowerCase()
        .includes(q)
    );
  });
});
const totalVoucherPages = computed(() => {
  return Math.max(1, Math.ceil(filteredVouchers.value.length / pageSize));
});

const pagedVouchers = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return filteredVouchers.value.slice(start, start + pageSize);
});

const voucherPageNumbers = computed(() => {
  const total = totalVoucherPages.value;
  const current = currentPage.value;
  let start = Math.max(1, current - 2);
  let end = Math.min(total, start + 4);

  if (end - start < 4) {
    start = Math.max(1, end - 4);
  }

  const pages = [];

  for (let i = start; i <= end; i++) {
    pages.push(i);
  }

  return pages;
});
function goVoucherPage(page) {
  const p = Number(page) || 1;
  currentPage.value = Math.min(Math.max(p, 1), totalVoucherPages.value);
}

function prevVoucherPage() {
  goVoucherPage(currentPage.value - 1);
}

function nextVoucherPage() {
  goVoucherPage(currentPage.value + 1);
}
function normalizeKhIds(value) {
  if (Array.isArray(value)) {
    return value
      .map((x) => Number(x?.id ?? x?.khachHangId ?? x))
      .filter((x) => Number.isFinite(x) && x > 0);
  }

  if (typeof value === "string") {
    return value
      .split(",")
      .map((x) => Number(x.trim()))
      .filter((x) => Number.isFinite(x) && x > 0);
  }

  return [];
}

function normalizeVoucher(x) {
  const khIdsRaw =
    x.khachHangIds ??
    x.khach_hang_ids ??
    x.khachHangs ??
    x.khach_hangs ??
    x.khachHangList ??
    x.khach_hang_list ??
    [];

  const khSingle =
    x.khachHangId ??
    x.khach_hang_id ??
    x.idKhachHang ??
    x.id_khach_hang ??
    null;

  return {
    id: x.id,
    maGiamGia: x.maGiamGia ?? x.ma_giam_gia ?? "",
    tenGiamGia: x.tenGiamGia ?? x.ten_giam_gia ?? "",
    trangThai: x.trangThai ?? x.trang_thai ?? true,
    soLuong: Number(x.soLuong ?? x.so_luong ?? 0),
    loaiGiam: x.loaiGiam ?? x.loai_giam ?? false,
    giaTriPhanTram: Number(x.giaTriPhanTram ?? x.gia_tri_phan_tram ?? 0),
    giaTriTienMat: Number(x.giaTriTienMat ?? x.gia_tri_tien_mat ?? 0),
    giaTriGiamToiDa: Number(x.giaTriGiamToiDa ?? x.gia_tri_giam_toi_da ?? 0),
    donHangToiThieu: Number(x.donHangToiThieu ?? x.don_hang_toi_thieu ?? 0),
    loaiPhieu: x.loaiPhieu ?? x.loai_phieu ?? null,
    khachHangIds: normalizeKhIds(khIdsRaw),
    khachHangId: khSingle != null ? Number(khSingle) : null,
    ngayBatDau: x.ngayBatDau ?? x.ngay_bat_dau ?? null,
    ngayKetThuc: x.ngayKetThuc ?? x.ngay_ket_thuc ?? null,
  };
}

watch(keyword, () => {
  currentPage.value = 1;
});

watch(activeTab, () => {
  currentPage.value = 1;
  keyword.value = "";
});

watch(filteredVouchers, () => {
  if (currentPage.value > totalVoucherPages.value) {
    currentPage.value = totalVoucherPages.value;
  }
});
function isPublicVoucher(voucher) {
  const lp = voucher?.loaiPhieu;

  if (lp === false) return true;
  if (lp === true) return false;

  const s = String(lp || "").toUpperCase();

  return s === "CONG_KHAI" || s === "PUBLIC";
}

function isPrivateVoucher(voucher) {
  const lp = voucher?.loaiPhieu;

  if (lp === true) return true;
  if (lp === false) return false;

  const s = String(lp || "").toUpperCase();

  return s === "CA_NHAN" || s === "PERSONAL";
}

function voucherBelongsToCustomer(voucher, customerId) {
  if (!isPrivateVoucher(voucher)) return true;
  if (!customerId) return false;

  if (Array.isArray(voucher.khachHangIds) && voucher.khachHangIds.length) {
    return voucher.khachHangIds.includes(Number(customerId));
  }

  if (voucher.khachHangId != null) {
    return Number(voucher.khachHangId) === Number(customerId);
  }

  return true;
}

function isVoucherActive(voucher) {
  const status = voucher?.trangThai;

  if (status === false || status === 0 || status === "0") return false;

  const statusText = String(status || "").toUpperCase();

  if (
    statusText === "FALSE" ||
    statusText === "INACTIVE" ||
    statusText === "NGUNG_AP_DUNG" ||
    statusText === "DA_HUY"
  ) {
    return false;
  }

  if (voucher.soLuong !== null && Number(voucher.soLuong) <= 0) return false;

  const now = new Date();
  const start = voucher?.ngayBatDau ? new Date(voucher.ngayBatDau) : null;
  const end = voucher?.ngayKetThuc ? new Date(voucher.ngayKetThuc) : null;

  if (start && !Number.isNaN(start.getTime()) && now < start) return false;
  if (end && !Number.isNaN(end.getTime()) && now > end) return false;

  return true;
}

function money(v) {
  const n = Number(v) || 0;
  return n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
}

function formatDateTime(value) {
  if (!value) return "--/--/----";

  const d = new Date(value);

  if (Number.isNaN(d.getTime())) return value;

  return new Intl.DateTimeFormat("vi-VN", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(d);
}

function formatDiscount(voucher) {
  const isPercent =
    voucher?.loaiGiam === true ||
    voucher?.loaiGiam === 1 ||
    voucher?.loaiGiam === "1";

  if (isPercent && voucher?.giaTriPhanTram) {
    return `Giảm ${voucher.giaTriPhanTram}%`;
  }

  if (voucher?.giaTriTienMat) {
    return `Giảm ${money(voucher.giaTriTienMat)} đ`;
  }

  return "Ưu đãi đặc biệt";
}

async function fetchVouchers() {
  try {
    loading.value = true;
    error.value = "";

    const customerId = khachHangId.value || null;
    const params = new URLSearchParams();

    if (customerId) {
      params.set("khachHangId", String(customerId));
    }

    const url = `${API_BASE}/api/pgg/pos${params.toString() ? `?${params.toString()}` : ""}`;

    console.log("DISCOUNT CUSTOMER ID =", customerId);
    console.log("DISCOUNT PGG URL =", url);

    const res = await fetch(url);
    const data = await res.json().catch(() => []);

    if (!res.ok) {
      throw new Error(data?.message || "Không tải được mã giảm giá");
    }

    const list = Array.isArray(data) ? data : [];
    const normalized = list.map(normalizeVoucher).filter(isVoucherActive);

    publicVouchers.value = normalized.filter(isPublicVoucher);
    myVouchers.value = isLoggedIn.value
      ? normalized.filter(isPrivateVoucher)
      : [];

    console.log("DISCOUNT ALL =", normalized);
    console.log("DISCOUNT PUBLIC =", publicVouchers.value);
    console.log("DISCOUNT MINE =", myVouchers.value);
  } catch (err) {
    console.error("fetchVouchers error:", err);
    error.value = err?.message || "Không tải được mã giảm giá";
    publicVouchers.value = [];
    myVouchers.value = [];
  } finally {
    loading.value = false;
  }
}
async function copyCode(code) {
  if (!code) return;

  try {
    await navigator.clipboard.writeText(code);
    copiedCode.value = code;

    if (copiedTimer) clearTimeout(copiedTimer);

    copiedTimer = setTimeout(() => {
      copiedCode.value = "";
    }, 2200);
  } catch (err) {
    console.error("copyCode error:", err);
  }
}

function scrollToVoucherList() {
  voucherListRef.value?.scrollIntoView({ behavior: "smooth", block: "start" });
}

function goShop() {
  router.push({ name: "Search" });
}
function goLogin() {
  router.push({ name: "Login", query: { redirect: "/giam-gia" } });
}
onMounted(async () => {
  await fetchVouchers();
});

onBeforeUnmount(() => {
  if (copiedTimer) {
    clearTimeout(copiedTimer);
    copiedTimer = null;
  }
});
watch(activeTab, () => {
  currentPage.value = 1;
  keyword.value = "";
});
</script>

<style scoped>
.discount-page {
  min-height: 100vh;
  background:
    radial-gradient(
      circle at top left,
      rgba(37, 99, 235, 0.12),
      transparent 34%
    ),
    linear-gradient(180deg, #f7f9ff 0%, #f3f6fb 48%, #ffffff 100%);
  color: #0f172a;
}

.discount-hero {
  position: relative;
  overflow: hidden;
  padding: 70px 0 58px;
  background: linear-gradient(
    135deg,
    rgba(2, 6, 23, 0.95) 0%,
    rgba(15, 23, 42, 0.98) 44%,
    rgba(30, 64, 175, 0.95) 100%
  );
}

.hero-bg {
  position: absolute;
  border-radius: 999px;
  filter: blur(10px);
  opacity: 0.55;
  pointer-events: none;
}

.hero-bg--one {
  width: 360px;
  height: 360px;
  right: -120px;
  top: -140px;
  background: rgba(96, 165, 250, 0.28);
}

.hero-bg--two {
  width: 260px;
  height: 260px;
  left: 8%;
  bottom: -150px;
  background: rgba(59, 130, 246, 0.18);
}

.hero-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) 410px;
  gap: 28px;
  align-items: stretch;
}

.hero-content,
.hero-panel {
  position: relative;
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(255, 255, 255, 0.08);
  border-radius: 34px;
  backdrop-filter: blur(16px);
  box-shadow: 0 28px 70px rgba(0, 0, 0, 0.22);
}

.hero-content {
  padding: 42px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  padding: 9px 15px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  color: #ffffff;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.02em;
}

.hero-title {
  margin: 18px 0;
  color: #ffffff;
  font-size: clamp(42px, 6vw, 72px);
  line-height: 0.96;
  font-weight: 900;
  letter-spacing: -0.05em;
}

.hero-title span {
  display: block;
  color: #bfdbfe;
}

.hero-desc {
  max-width: 660px;
  margin: 0 0 28px;
  color: rgba(255, 255, 255, 0.84);
  font-size: 17px;
  line-height: 1.8;
}

.hero-actions {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
}

.hero-btn {
  min-height: 50px;
  padding: 0 22px;
  border-radius: 16px;
  border: 0;
  font-size: 15px;
  font-weight: 800;
  transition: all 0.22s ease;
}

.hero-btn:hover {
  transform: translateY(-2px);
}

.hero-btn--primary {
  color: #0f172a;
  background: #ffffff;
  box-shadow: 0 16px 36px rgba(15, 23, 42, 0.16);
}

.hero-btn--outline {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.24);
}

.hero-panel {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 30px;
  overflow: hidden;
}

.hero-panel::before {
  content: "";
  position: absolute;
  width: 180px;
  height: 180px;
  right: -72px;
  top: -56px;
  border-radius: 999px;
  background: rgba(96, 165, 250, 0.22);
}

.hero-panel__icon {
  width: 72px;
  height: 72px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.15);
  color: #ffffff;
  font-size: 36px;
}

.hero-panel__main {
  margin-top: 34px;
}

.hero-panel__main span,
.hero-panel__main p {
  color: rgba(255, 255, 255, 0.78);
  margin: 0;
}

.hero-panel__main strong {
  display: block;
  color: #ffffff;
  font-size: 76px;
  line-height: 0.95;
  font-weight: 900;
  letter-spacing: -0.06em;
}

.hero-panel__note {
  margin-top: 28px;
  display: flex;
  gap: 10px;
  align-items: flex-start;
  padding: 15px;
  border-radius: 18px;
  color: rgba(255, 255, 255, 0.84);
  background: rgba(255, 255, 255, 0.1);
  line-height: 1.55;
  font-size: 14px;
}

.voucher-section {
  padding-top: 48px;
  padding-bottom: 44px;
}

.section-head {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 18px;
  flex-wrap: wrap;
  margin-bottom: 18px;
}

.section-subtitle {
  margin-bottom: 8px;
  color: #2563eb;
  font-size: 13px;
  font-weight: 900;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.section-title {
  color: #0f172a;
  font-size: 32px;
  font-weight: 900;
  letter-spacing: -0.03em;
}

.voucher-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-box {
  min-width: 280px;
  height: 48px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 15px;
  border-radius: 999px;
  background: #ffffff;
  border: 1px solid rgba(148, 163, 184, 0.28);
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.06);
}

.search-box i {
  color: #64748b;
}

.search-box input {
  width: 100%;
  border: 0;
  outline: 0;
  color: #0f172a;
  background: transparent;
  font-size: 14px;
  font-weight: 650;
}

.search-box input::placeholder {
  color: #94a3b8;
}

.info-strip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
  padding: 14px 18px;
  border-radius: 18px;
  color: #475569;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(148, 163, 184, 0.2);
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.04);
  font-size: 14px;
  line-height: 1.6;
}

.info-strip div {
  display: flex;
  align-items: center;
  gap: 9px;
}

.info-strip i {
  color: #2563eb;
}

.info-strip span {
  flex: 0 0 auto;
  color: #0f172a;
  font-weight: 850;
}

.voucher-card {
  position: relative;
  height: 100%;
  overflow: hidden;
  padding: 22px;
  border-radius: 28px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  border: 1px solid rgba(148, 163, 184, 0.24);
  box-shadow: 0 18px 42px rgba(15, 23, 42, 0.08);
  transition: all 0.25s ease;
}

.voucher-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 26px 60px rgba(15, 23, 42, 0.13);
}

.voucher-card__shine {
  position: absolute;
  width: 180px;
  height: 180px;
  top: -100px;
  right: -90px;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.1);
  pointer-events: none;
}

.voucher-card__head {
  position: relative;
  display: flex;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 18px;
}

.voucher-label {
  margin-bottom: 7px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.voucher-name {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
  line-height: 1.35;
  font-weight: 900;
}

.voucher-status {
  height: fit-content;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  flex: 0 0 auto;
  padding: 8px 11px;
  border-radius: 999px;
  color: #047857;
  background: #d1fae5;
  font-size: 12px;
  font-weight: 850;
  white-space: nowrap;
}

.voucher-ticket {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  padding: 15px;
  border-radius: 20px;
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  border: 1px dashed rgba(37, 99, 235, 0.35);
}

.voucher-ticket::before,
.voucher-ticket::after {
  content: "";
  position: absolute;
  top: 50%;
  width: 18px;
  height: 18px;
  border-radius: 999px;
  background: #ffffff;
  transform: translateY(-50%);
}

.voucher-ticket::before {
  left: -9px;
}

.voucher-ticket::after {
  right: -9px;
}

.voucher-ticket__left span {
  display: block;
  margin-bottom: 4px;
  color: #64748b;
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
}

.voucher-ticket__left strong {
  color: #1e3a8a;
  font-size: 20px;
  font-weight: 950;
  letter-spacing: 0.04em;
}

.copy-btn {
  position: relative;
  z-index: 1;
  width: 42px;
  height: 42px;
  border: 0;
  border-radius: 14px;
  color: #ffffff;
  background: #1d4ed8;
  transition: all 0.2s ease;
}

.copy-btn:hover {
  transform: scale(1.05);
  background: #1e40af;
}

.voucher-value {
  margin-bottom: 18px;
  color: #0f172a;
  font-size: 34px;
  line-height: 1.1;
  font-weight: 950;
  letter-spacing: -0.04em;
}

.voucher-meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.meta-item {
  min-height: 72px;
  padding: 13px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid rgba(148, 163, 184, 0.16);
}

.meta-item span {
  display: block;
  margin-bottom: 6px;
  color: #64748b;
  font-size: 12px;
  font-weight: 800;
}

.meta-item strong {
  display: block;
  color: #0f172a;
  font-size: 14px;
  line-height: 1.45;
  font-weight: 900;
}

.meta-item--full {
  grid-column: 1 / -1;
}

.voucher-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-top: 20px;
}

.voucher-btn {
  min-height: 46px;
  border-radius: 15px;
  font-size: 14px;
  font-weight: 900;
  transition: all 0.22s ease;
}

.voucher-btn:hover {
  transform: translateY(-2px);
}

.voucher-btn--primary {
  color: #ffffff;
  background: #0f172a;
  border: 1px solid #0f172a;
}

.voucher-btn--outline {
  color: #0f172a;
  background: #ffffff;
  border: 1px solid rgba(148, 163, 184, 0.32);
}

.how-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.how-card,
.state-box,
.empty-box {
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(148, 163, 184, 0.22);
  box-shadow: 0 18px 42px rgba(15, 23, 42, 0.06);
}

.how-card {
  padding: 24px;
}

.how-step {
  width: 46px;
  height: 46px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  border-radius: 16px;
  color: #ffffff;
  background: #1d4ed8;
  font-weight: 950;
}

.how-card h5 {
  margin-bottom: 9px;
  color: #0f172a;
  font-size: 18px;
  font-weight: 900;
}

.how-card p,
.empty-box p {
  margin: 0;
  color: #64748b;
  line-height: 1.7;
}

.state-box {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  min-height: 170px;
  padding: 28px;
  color: #475569;
  font-weight: 800;
}

.state-box--error {
  color: #b91c1c;
  background: #fff1f2;
}

.state-spinner {
  width: 24px;
  height: 24px;
  border: 3px solid rgba(37, 99, 235, 0.18);
  border-top-color: #2563eb;
  border-radius: 999px;
  animation: spin 0.8s linear infinite;
}

.empty-box {
  padding: 42px 24px;
  text-align: center;
}

.empty-box__icon {
  width: 72px;
  height: 72px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 18px;
  border-radius: 24px;
  color: #1d4ed8;
  background: #dbeafe;
  font-size: 32px;
}

.empty-box h5 {
  margin-bottom: 10px;
  color: #0f172a;
  font-weight: 900;
}

.empty-box .hero-btn {
  margin-top: 22px;
  color: #ffffff;
  background: #0f172a;
}

.copy-toast {
  position: fixed;
  right: 22px;
  bottom: 22px;
  z-index: 2000;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 15px 18px;
  border-radius: 18px;
  color: #ffffff;
  background: #0f172a;
  box-shadow: 0 22px 48px rgba(15, 23, 42, 0.28);
  font-size: 14px;
}

.copy-toast strong {
  margin-left: 4px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 1199.98px) {
  .hero-grid {
    grid-template-columns: 1fr;
  }

  .hero-panel {
    min-height: 280px;
  }
}

@media (max-width: 991.98px) {
  .how-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 767.98px) {
  .discount-hero {
    padding: 42px 0 38px;
  }

  .hero-content,
  .hero-panel {
    border-radius: 26px;
    padding: 24px;
  }

  .hero-title {
    font-size: 42px;
  }

  .hero-actions,
  .voucher-actions {
    grid-template-columns: 1fr;
    flex-direction: column;
  }

  .hero-btn {
    width: 100%;
  }

  .section-title {
    font-size: 25px;
  }

  .search-box {
    width: 100%;
    min-width: 0;
  }

  .voucher-toolbar {
    width: 100%;
  }

  .info-strip {
    align-items: flex-start;
    flex-direction: column;
  }

  .voucher-card__head {
    flex-direction: column;
  }

  .voucher-status {
    width: fit-content;
  }

  .voucher-meta {
    grid-template-columns: 1fr;
  }

  .meta-item--full {
    grid-column: auto;
  }

  .voucher-value {
    font-size: 29px;
  }

  .copy-toast {
    left: 16px;
    right: 16px;
    bottom: 16px;
    justify-content: center;
  }
}
.discount-page,
.discount-page input,
.discount-page button,
.discount-page select,
.discount-page textarea {
  font-family: var(
    --bs-body-font-family,
    system-ui,
    -apple-system,
    "Segoe UI",
    Roboto,
    "Helvetica Neue",
    Arial,
    sans-serif
  );
}

.discount-page {
  background: #f5f7fb;
  color: #111827;
}

.discount-hero {
  padding: 42px 0 74px;
  background: linear-gradient(135deg, #06164d 0%, #0a2168 52%, #143c9f 100%);
}

.hero-box,
.hero-grid {
  gap: 22px;
}

.hero-content,
.hero-side,
.hero-panel {
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.14);
  box-shadow: none;
}

.hero-content {
  padding: 30px;
}

.hero-side,
.hero-panel {
  padding: 24px;
}

.hero-badge {
  padding: 7px 13px;
  font-size: 12px;
  font-weight: 700;
}

.hero-title {
  margin: 14px 0 12px;
  font-size: 42px;
  line-height: 1.08;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.hero-desc {
  max-width: 620px;
  font-size: 15px;
  line-height: 1.7;
}

.hero-actions {
  gap: 12px;
}

.hero-btn {
  min-height: 44px;
  padding: 0 18px;
  border-radius: 13px;
  font-size: 14px;
  font-weight: 700;
}

.hero-btn--primary {
  color: #07143f;
  background: #ffffff;
  box-shadow: none;
}

.hero-btn--outline {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.24);
}

.hero-stat {
  padding: 16px;
  border-radius: 18px;
}

.hero-stat strong,
.hero-panel__main strong {
  font-size: 30px;
  font-weight: 800;
}

.hero-stat span,
.hero-panel__main span,
.hero-panel__main p {
  font-size: 13px;
  font-weight: 500;
}

.voucher-section,
section.container.py-5 {
  margin-top: -44px;
  position: relative;
  z-index: 3;
}

.section-head {
  padding: 18px 20px;
  margin-bottom: 18px;
  border-radius: 20px;
  background: #ffffff;
  border: 1px solid #e5eaf2;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.06);
}

.section-subtitle {
  color: #2563eb;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.section-title {
  font-size: 24px;
  font-weight: 750;
  letter-spacing: -0.01em;
}

.tab-group {
  gap: 8px;
}

.tab-btn {
  min-height: 40px;
  padding: 0 15px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 650;
  background: #f8fafc;
  border: 1px solid #d8e0ea;
}

.tab-btn.active {
  background: #07143f;
  border-color: #07143f;
}

.search-box {
  min-width: 280px;
  height: 42px;
  border-radius: 999px;
  background: #f8fafc;
  border: 1px solid #d8e0ea;
  box-shadow: none;
}

.search-box input {
  font-size: 13px;
  font-weight: 500;
}

.info-strip {
  margin-bottom: 18px;
  padding: 12px 15px;
  border-radius: 16px;
  background: #ffffff;
  border: 1px solid #e5eaf2;
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.04);
  font-size: 13px;
}

.info-strip span {
  font-weight: 700;
}

.voucher-card {
  padding: 18px;
  border-radius: 20px;
  background: #ffffff;
  border: 1px solid #e5eaf2;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.06);
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease;
}

.voucher-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 38px rgba(15, 23, 42, 0.11);
}

.voucher-card--mine {
  border-color: #dbeafe;
  box-shadow: 0 10px 28px rgba(37, 99, 235, 0.08);
}

.voucher-top,
.voucher-card__head {
  margin-bottom: 14px;
}

.voucher-label {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.06em;
}

.voucher-code {
  min-height: 30px;
  padding: 0 10px;
  border-radius: 999px;
  background: #eff6ff;
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 750;
}

.voucher-name {
  font-size: 18px;
  line-height: 1.35;
  font-weight: 750;
  letter-spacing: 0;
}

.voucher-status {
  padding: 7px 10px;
  font-size: 11px;
  font-weight: 700;
}

.voucher-ticket {
  margin-bottom: 14px;
  padding: 13px;
  border-radius: 16px;
}

.voucher-ticket__left span {
  font-size: 11px;
  font-weight: 700;
}

.voucher-ticket__left strong {
  font-size: 18px;
  font-weight: 800;
}

.copy-btn {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  background: #07143f;
}

.copy-btn:hover {
  background: #0b1b55;
}

.voucher-value {
  margin-bottom: 14px;
  color: #dc2626;
  font-size: 28px;
  line-height: 1.12;
  font-weight: 800;
  letter-spacing: 0;
}

.voucher-meta {
  gap: 9px;
  font-size: 13px;
  line-height: 1.55;
}

.meta-item {
  min-height: 66px;
  padding: 11px;
  border-radius: 13px;
  background: #f8fafc;
}

.meta-item span {
  font-size: 11px;
  font-weight: 700;
}

.meta-item strong {
  font-size: 13px;
  font-weight: 750;
}

.voucher-actions {
  gap: 10px;
  margin-top: 16px;
}

.voucher-btn {
  min-height: 40px;
  border-radius: 13px;
  font-size: 13px;
  font-weight: 700;
}

.voucher-btn--primary {
  background: #07143f;
}

.voucher-btn--primary:hover {
  background: #0b1b55;
}

.voucher-btn--outline {
  background: #ffffff;
  color: #07143f;
  border: 1px solid #d8e0ea;
}

.how-grid {
  gap: 16px;
}

.how-card,
.state-box,
.empty-box {
  border-radius: 20px;
  background: #ffffff;
  border: 1px solid #e5eaf2;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.06);
}

.how-card {
  padding: 20px;
}

.how-step {
  width: 42px;
  height: 42px;
  border-radius: 13px;
  background: #07143f;
  color: #ffffff;
  font-weight: 750;
}

.how-card h5 {
  font-size: 17px;
  font-weight: 750;
}

.how-card p,
.empty-box p {
  font-size: 14px;
  line-height: 1.65;
}

.empty-box {
  padding: 38px 24px;
}

.empty-box__icon {
  width: 64px;
  height: 64px;
  border-radius: 20px;
  color: #1d4ed8;
  background: #dbeafe;
  font-size: 28px;
}

.empty-box h5 {
  font-weight: 750;
}

.copy-toast {
  right: 20px;
  bottom: 20px;
  padding: 13px 16px;
  border-radius: 15px;
  background: #07143f;
  box-shadow: 0 16px 34px rgba(15, 23, 42, 0.22);
  font-size: 13px;
}

@media (max-width: 991.98px) {
  .hero-box,
  .hero-grid,
  .how-grid {
    grid-template-columns: 1fr;
  }

  .voucher-section,
  section.container.py-5 {
    margin-top: -36px;
  }

  .hero-title {
    font-size: 34px;
  }
}

@media (max-width: 767.98px) {
  .discount-hero {
    padding: 34px 0 64px;
  }

  .hero-content,
  .hero-side,
  .hero-panel {
    padding: 22px;
    border-radius: 20px;
  }

  .section-head {
    align-items: flex-start;
  }

  .search-box {
    width: 100%;
    min-width: 0;
  }

  .voucher-actions,
  .hero-actions {
    flex-direction: column;
  }

  .voucher-btn,
  .hero-btn {
    width: 100%;
  }

  .voucher-value {
    font-size: 25px;
  }
}
.voucher-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 24px;
  padding: 14px 18px;
  border-radius: 18px;
  background: #ffffff;
  border: 1px solid #e5eaf2;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.06);
}

.voucher-page-info {
  color: #64748b;
  font-size: 14px;
  font-weight: 650;
}

.voucher-page-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.voucher-page-btn {
  min-width: 38px;
  height: 38px;
  padding: 0 12px;
  border-radius: 12px;
  border: 1px solid #d8e0ea;
  background: #ffffff;
  color: #0f172a;
  font-size: 14px;
  font-weight: 750;
  transition: all 0.18s ease;
}

.voucher-page-btn:hover:not(:disabled) {
  background: #07143f;
  border-color: #07143f;
  color: #ffffff;
}

.voucher-page-btn.active {
  background: #07143f;
  border-color: #07143f;
  color: #ffffff;
}

.voucher-page-btn:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

@media (max-width: 767.98px) {
  .voucher-pagination {
    align-items: stretch;
    flex-direction: column;
  }

  .voucher-page-actions {
    justify-content: center;
    flex-wrap: wrap;
  }
}
.row.g-4 > [class*="col-"] {
  display: flex;
}

.voucher-card {
  width: 100%;
  min-height: 100%;
  display: flex;
  flex-direction: column;
}

.voucher-meta {
  margin-bottom: 0;
}

.voucher-actions {
  margin-top: auto;
  padding-top: 20px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.voucher-toolbar {
  flex-wrap: wrap;
}

.tab-group {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.tab-btn {
  min-height: 42px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 0 15px;
  border-radius: 999px;
  border: 1px solid #d8e0ea;
  background: #ffffff;
  color: #334155;
  font-size: 13px;
  font-weight: 700;
  transition: all 0.18s ease;
}

.tab-btn span {
  min-width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: #eef2ff;
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 800;
}

.tab-btn.active {
  background: #07143f;
  border-color: #07143f;
  color: #ffffff;
}

.tab-btn.active span {
  background: rgba(255, 255, 255, 0.16);
  color: #ffffff;
}

.voucher-card--mine {
  border-color: #bfdbfe;
  box-shadow: 0 10px 28px rgba(37, 99, 235, 0.08);
}

@media (max-width: 767.98px) {
  .tab-group,
  .tab-btn {
    width: 100%;
  }

  .tab-btn {
    justify-content: center;
  }
}
</style>
