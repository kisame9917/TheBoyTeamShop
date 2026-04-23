<template>
  <div class="discount-page">
    <section class="discount-hero">
      <div class="container">
        <div class="hero-box">
          <div class="hero-content">
            <span class="hero-badge">
              <i class="bi bi-ticket-perforated-fill me-2"></i>
              Ưu đãi đơn hàng
            </span>

            <h1 class="hero-title">GIẢM GIÁ</h1>

            <p class="hero-desc">
              Tổng hợp mã giảm giá hiện có để áp dụng khi thanh toán.
              Chọn mã phù hợp, sao chép nhanh và tiếp tục mua sắm.
            </p>

            <div class="hero-actions">
              <button class="hero-btn hero-btn--primary" @click="goShop">
                Mua ngay
              </button>
              <button class="hero-btn hero-btn--outline" @click="scrollToVoucherList">
                Xem mã giảm giá
              </button>
            </div>
          </div>

          <div class="hero-side">
            <div class="hero-stat">
              <strong>{{ publicActiveCount }}</strong>
              <span>Mã công khai đang dùng được</span>
            </div>
            <div class="hero-stat">
              <strong>{{ myVoucherCount }}</strong>
              <span>Mã của tôi</span>
            </div>
            <div class="hero-stat">
              <strong>3 bước</strong>
              <span>Chọn mã → mua hàng → áp dụng ở checkout</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section ref="voucherListRef" class="container py-5">
      <div class="section-head">
        <div>
          <div class="section-subtitle">Danh sách ưu đãi</div>
          <h4 class="section-title mb-0">MÃ GIẢM GIÁ</h4>
        </div>

        <div class="tab-group">
          <button
            class="tab-btn"
            :class="{ active: activeTab === 'public' }"
            @click="activeTab = 'public'"
          >
            Mã công khai
          </button>

          <button
            class="tab-btn"
            :class="{ active: activeTab === 'mine' }"
            @click="activeTab = 'mine'"
          >
            Mã của tôi
          </button>
        </div>
      </div>

      <div v-if="loading" class="state-box">Đang tải mã giảm giá...</div>
      <div v-else-if="error" class="state-box text-danger">{{ error }}</div>

      <template v-else>
        <div v-if="activeTab === 'public'">
          <div v-if="publicVouchers.length" class="row g-4">
            <div
              v-for="voucher in publicVouchers"
              :key="'public-' + voucher.id"
              class="col-lg-4 col-md-6"
            >
              <div class="voucher-card">
                <div class="voucher-top">
                  <div>
                    <div class="voucher-code">{{ voucher.maGiamGia }}</div>
                    <div class="voucher-name">{{ voucher.tenGiamGia }}</div>
                  </div>

                  <span class="voucher-status" :class="statusClass(voucher)">
                    {{ statusLabel(voucher) }}
                  </span>
                </div>

                <div class="voucher-value">
                  {{ formatDiscount(voucher) }}
                </div>

                <div class="voucher-meta">
                  <div>
                    <strong>Loại phiếu:</strong>
                    {{ voucher.loaiPhieu === "CA_NHAN" ? "Cá nhân" : "Công khai" }}
                  </div>
                  <div>
                    <strong>Đơn tối thiểu:</strong>
                    {{ money(voucher.donHangToiThieu) }} đ
                  </div>
                  <div v-if="voucher.giaTriGiamToiDa">
                    <strong>Giảm tối đa:</strong>
                    {{ money(voucher.giaTriGiamToiDa) }} đ
                  </div>
                  <div>
                    <strong>Hiệu lực:</strong>
                    {{ formatDateTime(voucher.ngayBatDau) }} - {{ formatDateTime(voucher.ngayKetThuc) }}
                  </div>
                </div>

                <div class="voucher-actions">
                  <button class="voucher-btn voucher-btn--primary" @click="copyCode(voucher.maGiamGia)">
                    Sao chép mã
                  </button>
                  <button class="voucher-btn voucher-btn--outline" @click="goShop">
                    Dùng ngay
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="empty-box">
            <div class="empty-box__icon">
              <i class="bi bi-ticket-perforated"></i>
            </div>
            <h5>Hiện chưa có mã công khai</h5>
            <p>Một số ưu đãi sẽ xuất hiện tại đây khi hệ thống cập nhật.</p>
            <button class="hero-btn hero-btn--primary" @click="goShop">
              Xem sản phẩm
            </button>
          </div>
        </div>

        <div v-else>
          <div v-if="!isLoggedIn" class="empty-box">
            <div class="empty-box__icon">
              <i class="bi bi-person-lock"></i>
            </div>
            <h5>Đăng nhập để xem mã của bạn</h5>
            <p>Mã cá nhân sẽ được hiển thị sau khi bạn đăng nhập tài khoản.</p>
            <button class="hero-btn hero-btn--primary" @click="goLogin">
              Đăng nhập
            </button>
          </div>

          <div v-else-if="myVouchers.length" class="row g-4">
            <div
              v-for="voucher in myVouchers"
              :key="'mine-' + voucher.id"
              class="col-lg-4 col-md-6"
            >
              <div class="voucher-card voucher-card--mine">
                <div class="voucher-top">
                  <div>
                    <div class="voucher-code">{{ voucher.maGiamGia }}</div>
                    <div class="voucher-name">{{ voucher.tenGiamGia }}</div>
                  </div>

                  <span class="voucher-status" :class="statusClass(voucher)">
                    {{ statusLabel(voucher) }}
                  </span>
                </div>

                <div class="voucher-value">
                  {{ formatDiscount(voucher) }}
                </div>

                <div class="voucher-meta">
                  <div>
                    <strong>Loại phiếu:</strong> Mã của tôi
                  </div>
                  <div>
                    <strong>Đơn tối thiểu:</strong>
                    {{ money(voucher.donHangToiThieu) }} đ
                  </div>
                  <div v-if="voucher.giaTriGiamToiDa">
                    <strong>Giảm tối đa:</strong>
                    {{ money(voucher.giaTriGiamToiDa) }} đ
                  </div>
                  <div>
                    <strong>Hiệu lực:</strong>
                    {{ formatDateTime(voucher.ngayBatDau) }} - {{ formatDateTime(voucher.ngayKetThuc) }}
                  </div>
                </div>

                <div class="voucher-actions">
                  <button class="voucher-btn voucher-btn--primary" @click="copyCode(voucher.maGiamGia)">
                    Sao chép mã
                  </button>
                  <button class="voucher-btn voucher-btn--outline" @click="goCheckout">
                    Đi đến thanh toán
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="empty-box">
            <div class="empty-box__icon">
              <i class="bi bi-wallet2"></i>
            </div>
            <h5>Bạn chưa có mã giảm giá cá nhân</h5>
            <p>Hãy tiếp tục mua sắm, các ưu đãi dành riêng sẽ được cập nhật khi có.</p>
            <button class="hero-btn hero-btn--primary" @click="goShop">
              Tiếp tục mua sắm
            </button>
          </div>
        </div>
      </template>
    </section>

    <section class="container pb-5">
      <div class="section-head">
        <div>
          <div class="section-subtitle">Cách sử dụng</div>
          <h4 class="section-title mb-0">ÁP MÃ NHƯ THẾ NÀO?</h4>
        </div>
      </div>

      <div class="how-grid">
        <div class="how-card">
          <div class="how-step">1</div>
          <h5>Chọn mã phù hợp</h5>
          <p>Xem điều kiện đơn hàng và sao chép mã giảm giá bạn muốn dùng.</p>
        </div>

        <div class="how-card">
          <div class="how-step">2</div>
          <h5>Thêm sản phẩm vào giỏ</h5>
          <p>Chọn sản phẩm bạn cần và tiếp tục đến bước thanh toán.</p>
        </div>

        <div class="how-card">
          <div class="how-step">3</div>
          <h5>Áp mã ở checkout</h5>
          <p>Hệ thống sẽ kiểm tra điều kiện và tính giảm giá trên đơn hàng nếu hợp lệ.</p>
        </div>
      </div>
    </section>

    <div v-if="copiedCode" class="copy-toast">
      Đã sao chép mã: <strong>{{ copiedCode }}</strong>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { getMyVouchers, getPublicVouchers } from "../../services/promotionClientApi";

const router = useRouter();
const API_BASE = import.meta.env.VITE_API_BASE_URL || "";
const voucherListRef = ref(null);
const activeTab = ref("public");

const loading = ref(false);
const error = ref("");
const publicVouchers = ref([]);
const myVouchers = ref([]);

const copiedCode = ref("");
let copiedTimer = null;

const isLoggedIn = computed(() => {
  return (
    !!localStorage.getItem("USER_ACCESS_TOKEN") ||
    !!sessionStorage.getItem("USER_ACCESS_TOKEN") ||
    !!localStorage.getItem("vest_token")
  );
});

const khachHangId = computed(() => {
  const raw =
    localStorage.getItem("USER_ID") ||
    sessionStorage.getItem("USER_ID") ||
    localStorage.getItem("KHACH_HANG_ID") ||
    sessionStorage.getItem("KHACH_HANG_ID");

  const id = Number(raw);
  return Number.isNaN(id) || id <= 0 ? null : id;
});

const publicActiveCount = computed(() => {
  return publicVouchers.value.filter((item) => isVoucherActive(item)).length;
});

const myVoucherCount = computed(() => myVouchers.value.length);

function normalizeList(list) {
  return Array.isArray(list) ? list : [];
}

function isVoucherActive(voucher) {
  if (!voucher?.trangThai) return false;

  const now = new Date();
  const start = voucher?.ngayBatDau ? new Date(voucher.ngayBatDau) : null;
  const end = voucher?.ngayKetThuc ? new Date(voucher.ngayKetThuc) : null;

  if (start && now < start) return false;
  if (end && now > end) return false;

  return true;
}

function statusLabel(voucher) {
  if (!voucher?.trangThai) return "Ngừng áp dụng";

  const now = new Date();
  const start = voucher?.ngayBatDau ? new Date(voucher.ngayBatDau) : null;
  const end = voucher?.ngayKetThuc ? new Date(voucher.ngayKetThuc) : null;

  if (start && now < start) return "Chưa bắt đầu";
  if (end && now > end) return "Hết hạn";
  return "Đang áp dụng";
}

function statusClass(voucher) {
  const label = statusLabel(voucher);
  if (label === "Đang áp dụng") return "status-active";
  if (label === "Chưa bắt đầu") return "status-pending";
  return "status-ended";
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
  const isPercent = voucher?.loaiGiam === true || voucher?.loaiGiam === 1;
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
    const url = customerId
      ? `${API_BASE}/api/pgg/pos?khachHangId=${customerId}`
      : `${API_BASE}/api/pgg/pos`;

    const res = await fetch(url);
    const data = await res.json().catch(() => []);

    if (!res.ok) {
      throw new Error(data?.message || "Không tải được mã giảm giá");
    }

    const list = Array.isArray(data) ? data : [];

    const normalized = list.map((x) => ({
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
      loaiPhieu: x.loaiPhieu ?? x.loai_phieu ?? "",
      ngayBatDau: x.ngayBatDau ?? x.ngay_bat_dau ?? null,
      ngayKetThuc: x.ngayKetThuc ?? x.ngay_ket_thuc ?? null,
    }));

    publicVouchers.value = normalized.filter((item) => item.loaiPhieu === "CONG_KHAI");
    myVouchers.value = isLoggedIn.value
      ? normalized.filter((item) => item.loaiPhieu === "CA_NHAN")
      : [];
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

function goCheckout() {
  router.push({ name: "Checkout" });
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
</script>

<style scoped>
.discount-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f7fc 0%, #f3f4f8 100%);
}

.discount-hero {
  padding: 56px 0 40px;
  background: linear-gradient(135deg, #000f51 0%, #12348f 100%);
}

.hero-box {
  display: grid;
  grid-template-columns: 1.25fr 0.75fr;
  gap: 24px;
  align-items: stretch;
}

.hero-content,
.hero-side {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 28px;
  padding: 32px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.14);
}

.hero-side {
  display: grid;
  gap: 16px;
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
}

.hero-title {
  font-size: 54px;
  line-height: 1.08;
  color: #fff;
  font-weight: 800;
  margin: 16px 0;
}

.hero-desc {
  font-size: 17px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.88);
  margin-bottom: 24px;
  max-width: 620px;
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
  font-weight: 750;
  font-size: 15px;
  transition: all 0.25s ease;
  border: none;
}

.hero-btn--primary {
  background: #fff;
  color: #000f51;
}

.hero-btn--outline {
  background: transparent;
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.35);
}

.hero-stat {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 22px;
  padding: 20px;
}

.hero-stat strong {
  display: block;
  font-size: 30px;
  line-height: 1.1;
  color: #fff;
  margin-bottom: 8px;
}

.hero-stat span {
  color: rgba(255, 255, 255, 0.86);
  line-height: 1.6;
  font-size: 14px;
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
  font-weight: 800;
}

.tab-group {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.tab-btn {
  min-height: 44px;
  padding: 0 18px;
  border-radius: 999px;
  border: 1px solid #dbe3ef;
  background: #fff;
  color: #334155;
  font-weight: 700;
}

.tab-btn.active {
  background: #000f51;
  color: #fff;
  border-color: #000f51;
}

.voucher-card {
  background: #fff;
  border-radius: 24px;
  padding: 22px;
  height: 100%;
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 14px 30px rgba(10, 24, 74, 0.05);
}

.voucher-card--mine {
  border-color: rgba(13, 110, 253, 0.18);
  box-shadow: 0 16px 36px rgba(13, 110, 253, 0.08);
}

.voucher-top {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: start;
  margin-bottom: 18px;
}

.voucher-code {
  display: inline-flex;
  align-items: center;
  min-height: 34px;
  padding: 0 12px;
  border-radius: 999px;
  background: #eaf0ff;
  color: #000f51;
  font-size: 13px;
  font-weight: 800;
  margin-bottom: 12px;
}

.voucher-name {
  font-size: 20px;
  font-weight: 800;
  color: #0f172a;
}

.voucher-status {
  white-space: nowrap;
  border-radius: 999px;
  padding: 8px 12px;
  font-size: 12px;
  font-weight: 750;
}

.status-active {
  background: #dcfce7;
  color: #166534;
}

.status-pending {
  background: #fef3c7;
  color: #92400e;
}

.status-ended {
  background: #fee2e2;
  color: #991b1b;
}

.voucher-value {
  font-size: 34px;
  line-height: 1.15;
  font-weight: 800;
  color: #000f51;
  margin-bottom: 18px;
}

.voucher-meta {
  display: grid;
  gap: 10px;
  color: #475569;
  font-size: 14px;
  line-height: 1.65;
}

.voucher-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 22px;
}

.voucher-btn {
  min-height: 44px;
  padding: 0 16px;
  border-radius: 14px;
  font-weight: 750;
  transition: all 0.2s ease;
}

.voucher-btn--primary {
  background: #000f51;
  color: #fff;
  border: none;
}

.voucher-btn--outline {
  background: #fff;
  color: #000f51;
  border: 1px solid #dbe3ef;
}

.how-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.how-card,
.state-box,
.empty-box {
  background: #fff;
  border-radius: 22px;
  padding: 24px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 14px 30px rgba(10, 24, 74, 0.05);
}

.how-step {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  background: #eaf0ff;
  color: #000f51;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  margin-bottom: 16px;
}

.how-card h5 {
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 10px;
}

.how-card p,
.empty-box p {
  color: #64748b;
  line-height: 1.7;
  margin-bottom: 0;
}

.empty-box {
  text-align: center;
}

.empty-box__icon {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  background: #eaf0ff;
  color: #000f51;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin-bottom: 16px;
}

.copy-toast {
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 2000;
  background: #0f172a;
  color: #fff;
  padding: 14px 18px;
  border-radius: 14px;
  box-shadow: 0 16px 34px rgba(0, 0, 0, 0.22);
}

@media (max-width: 991.98px) {
  .hero-box,
  .how-grid {
    grid-template-columns: 1fr;
  }

  .hero-title {
    font-size: 42px;
  }
}

@media (max-width: 767.98px) {
  .discount-hero {
    padding: 42px 0 34px;
  }

  .hero-title {
    font-size: 34px;
  }

  .hero-actions,
  .voucher-actions {
    flex-direction: column;
  }

  .hero-btn,
  .voucher-btn {
    width: 100%;
  }

  .section-title {
    font-size: 24px;
  }

  .voucher-top {
    flex-direction: column;
    align-items: start;
  }

  .voucher-value {
    font-size: 28px;
  }
}
</style>