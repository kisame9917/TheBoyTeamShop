<template>
  <div class="lookup-page">
    <section class="lookup-hero">
      <div class="container">
        <div class="lookup-hero__inner">
          <div class="lookup-badge">THE BOY TEAM</div>
          <h1 class="lookup-title">TRA CỨU ĐƠN HÀNG</h1>
          <p class="lookup-desc">
            Nhập mã đơn hàng và số điện thoại để xem trạng thái xử lý đơn của bạn.
          </p>
        </div>
      </div>
    </section>

    <section class="container py-4 py-lg-5">
      <div class="lookup-search-card">
        <div class="row g-3 align-items-end">
          <div class="col-12 col-lg-5">
            <label class="form-label">Mã đơn hàng</label>
            <input
              v-model.trim="form.maHoaDon"
              type="text"
              class="form-control lookup-input"
              placeholder="Ví dụ: HD26031712345"
            />
          </div>

          <div class="col-12 col-lg-5">
            <label class="form-label">Số điện thoại</label>
            <input
              v-model.trim="form.soDienThoai"
              type="text"
              class="form-control lookup-input"
              placeholder="Nhập số điện thoại đặt hàng"
              @keyup.enter="lookupOrder"
            />
          </div>

          <div class="col-12 col-lg-2 d-grid">
            <button
              class="lookup-btn"
              type="button"
              :disabled="loading"
              @click="lookupOrder"
            >
              {{ loading ? "ĐANG TẢI..." : "Tra cứu" }}
            </button>
          </div>
        </div>

        <div v-if="errorMessage" class="alert alert-danger mt-3 mb-0">
          {{ errorMessage }}
        </div>
      </div>

      <div v-if="order" class="lookup-result mt-4 mt-lg-5">
        <div class="lookup-head-card">
          <div class="lookup-head-card__left">
            <div class="lookup-code">Mã đơn: {{ order.maHoaDon }}</div>
            <div class="lookup-date">Đặt lúc: {{ formatDate(order.ngayTao) }}</div>
          </div>

          <div class="lookup-head-card__right">
            <span class="status-badge" :class="statusClass(order.trangThaiDon)">
              {{ order.tenTrangThaiDon }}
            </span>
          </div>
        </div>

        <div v-if="showProgress" class="progress-card mt-3">
          <div class="progress-line">
            <div
              v-for="step in progressSteps"
              :key="step.key"
              class="progress-step"
              :class="{ active: step.active }"
            >
              <div class="progress-step__dot"><span v-if="step.active">✓</span></div>
              <div class="progress-step__label">{{ step.label }}</div>
            </div>
          </div>
        </div>

        <div class="row g-4 mt-1">
          <div class="col-12 col-lg-7">
            <div class="detail-card">
              <div class="detail-card__title">Thông tin đơn hàng</div>

              <div class="detail-grid">
                <div class="detail-item">
                  <div class="detail-label">Khách hàng</div>
                  <div class="detail-value">{{ order.tenKhachHang || "-" }}</div>
                </div>

                <div class="detail-item">
                  <div class="detail-label">SĐT đặt hàng</div>
                  <div class="detail-value">{{ order.soDienThoai || "-" }}</div>
                </div>

                <div class="detail-item">
                  <div class="detail-label">Người nhận</div>
                  <div class="detail-value">{{ order.tenNguoiNhanHang || "-" }}</div>
                </div>

                <div class="detail-item">
                  <div class="detail-label">SĐT người nhận</div>
                  <div class="detail-value">{{ order.soDienThoaiNhanHang || "-" }}</div>
                </div>

                <div class="detail-item detail-item--full">
                  <div class="detail-label">Địa chỉ giao hàng</div>
                  <div class="detail-value">{{ fullAddress(order) || "-" }}</div>
                </div>

                <div class="detail-item">
                  <div class="detail-label">Thanh toán</div>
                  <div class="detail-value">
                    {{ order.paymentMethod || "-" }}
                    <span class="payment-badge ms-2" :class="paymentStatusClass(order.paymentStatus)">
                      {{ paymentStatusText(order.paymentStatus) }}
                    </span>
                  </div>
                </div>

                <div class="detail-item">
                  <div class="detail-label">Ghi chú</div>
                  <div class="detail-value">{{ order.ghiChu || "Không có" }}</div>
                </div>
              </div>
            </div>

            <div class="detail-card mt-4">
              <div class="detail-card__title">Sản phẩm trong đơn</div>

              <div v-if="!order.items?.length" class="text-muted">
                Không có sản phẩm trong đơn hàng.
              </div>

              <div v-else class="lookup-item-list">
                <div
                  class="lookup-item"
                  v-for="item in order.items"
                  :key="item.idSanPhamChiTiet || item.maSanPhamChiTiet"
                >
                  <img
                    :src="normalizeImg(item.anhDaiDien)"
                    alt="Ảnh sản phẩm"
                    class="lookup-item__img"
                    @error="onImgError"
                  />

                  <div class="lookup-item__content">
                    <div class="lookup-item__name">{{ item.tenSanPham || "Sản phẩm" }}</div>
                    <div class="lookup-item__meta">
                      <span v-if="item.mauSac">Màu: {{ item.mauSac }}</span>
                      <span v-if="item.kichCo"> / Size: {{ item.kichCo }}</span>
                    </div>
                    <div class="lookup-item__meta">SL: {{ item.soLuong || 0 }}</div>
                  </div>

                  <div class="lookup-item__price">
                    <div>{{ money(item.donGia) }} đ</div>
                    <strong>{{ money(item.thanhTien) }} đ</strong>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="col-12 col-lg-5">
            <div class="summary-card">
              <div class="summary-card__title">Tóm tắt thanh toán</div>

              <div class="sum-row">
                <span>Tiền hàng</span>
                <strong>{{ money(order.tongTien) }} đ</strong>
              </div>

              <div class="sum-row">
                <span>Giảm giá</span>
                <strong>- {{ money(order.tongTienGiam) }} đ</strong>
              </div>

              <div class="sum-row">
                <span>Phí vận chuyển</span>
                <strong>{{ money(order.phiVanChuyen) }} đ</strong>
              </div>

              <div class="sum-row total">
                <span>Tổng thanh toán</span>
                <strong>{{ money(order.tongTienSauGiam) }} đ</strong>
              </div>
            </div>

            <div
              v-if="[5, 6, 7].includes(Number(order.trangThaiDon))"
              class="status-note mt-3"
              :class="statusClass(order.trangThaiDon)"
            >
              {{ specialStatusText(order.trangThaiDon) }}
            </div>
          </div>
        </div>
      </div>
    </section>
     <ChatWidget />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import ChatWidget from '../../components/ClientChatWidget.vue';
const route = useRoute();
const router = useRouter();

const form = reactive({
  maHoaDon: "",
  soDienThoai: "",
});

const loading = ref(false);
const errorMessage = ref("");
const order = ref(null);

const showProgress = computed(() => {
  const code = Number(order.value?.trangThaiDon);
  return ![5, 6, 7, 9].includes(code) && code >= 0;
});

function getProgressIndex(code) {
  const n = Number(code);
  if (n === 0) return 0;
  if (n === 8) return 1;
  if (n === 1) return 2;
  if (n === 2) return 3;
  if (n === 3) return 4;
  if (n === 4) return 5;
  return -1;
}

const progressSteps = computed(() => {
  const currentIndex = getProgressIndex(order.value?.trangThaiDon);

  return [
    { key: "waiting", label: "Chờ xác nhận", active: currentIndex >= 0 },
    { key: "confirmed", label: "Đã xác nhận", active: currentIndex >= 1 },
    { key: "process", label: "Đang xử lý", active: currentIndex >= 2 },
    { key: "shipping", label: "Đang giao", active: currentIndex >= 3 },
    { key: "delivered", label: "Đã giao", active: currentIndex >= 4 },
    { key: "done", label: "Hoàn thành", active: currentIndex >= 5 },
  ];
});

function money(value) {
  const n = Number(value) || 0;
  return n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
}

function formatDate(value) {
  if (!value) return "-";
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleString("vi-VN");
}

function fullAddress(o) {
  return [
    o?.diaChiNhanHangChiTiet,
    o?.phuongXaNhanHang,
    o?.quanHuyenNhanHang,
    o?.tinhThanhNhanHang,
  ]
    .filter(Boolean)
    .join(", ");
}

function statusClass(code) {
  const n = Number(code);
  if (n === 4) return "status-success";
  if (n === 5) return "status-danger";
  if (n === 6 || n === 7) return "status-warning";
  if (n === 2 || n === 3) return "status-info";
  return "status-primary";
}

function paymentStatusText(value) {
  if (Number(order.value?.trangThaiDon) === 4) return "Đã thanh toán";
  const v = String(value || "").toUpperCase();
  if (v === "PAID") return "Đã thanh toán";
  if (v === "PENDING") return "Chờ thanh toán";
  return "Chưa thanh toán";
}

function paymentStatusClass(value) {
  if (Number(order.value?.trangThaiDon) === 4) return "payment-success";
  const v = String(value || "").toUpperCase();
  if (v === "PAID") return "payment-success";
  if (v === "PENDING") return "payment-warning";
  return "payment-default";
}

function specialStatusText(code) {
  const n = Number(code);
  if (n === 5) return "Đơn hàng này đã bị hủy.";
  if (n === 6) return "Đơn hàng đang ở trạng thái yêu cầu hoàn.";
  if (n === 7) return "Đơn hàng đã hoàn.";
  return "";
}

function normalizeImg(url) {
  if (!url) return "";
  if (url.startsWith("http://") || url.startsWith("https://")) return url;
  return `http://localhost:8080${url}`;
}

function onImgError(e) {
  e.target.src =
    "data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='100' height='100'%3E%3Crect width='100%25' height='100%25' fill='%23eef2f7'/%3E%3Ctext x='50%25' y='50%25' dominant-baseline='middle' text-anchor='middle' fill='%2394a3b8' font-size='13'%3EẢnh%3C/text%3E%3C/svg%3E";
}

async function lookupOrder() {
  errorMessage.value = "";
  order.value = null;

  if (!form.maHoaDon) {
    errorMessage.value = "Vui lòng nhập mã đơn hàng.";
    return;
  }

  if (!form.soDienThoai) {
    errorMessage.value = "Vui lòng nhập số điện thoại.";
    return;
  }

  try {
    loading.value = true;

    const params = new URLSearchParams({
      maHoaDon: form.maHoaDon.trim(),
      soDienThoai: form.soDienThoai.trim(),
    });

    const response = await fetch(`http://localhost:8080/api/online-checkout/lookup?${params}`);

    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
      throw new Error(data?.message || "Không tra cứu được đơn hàng");
    }

    order.value = data;

    router.replace({
      name: "OrderLookup",
      query: {
        ma: form.maHoaDon.trim(),
        phone: form.soDienThoai.trim(),
      },
    });
  } catch (err) {
    errorMessage.value = err.message || "Không tra cứu được đơn hàng";
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  form.maHoaDon = String(route.query.ma || "").trim();
  form.soDienThoai = String(route.query.phone || "").trim();

  if (form.maHoaDon && form.soDienThoai) {
    lookupOrder();
  }
});
</script>

<style scoped>
.lookup-page {
  background: linear-gradient(180deg, #f6f8fc 0%, #f8fafc 100%);
  min-height: 100vh;
}

.lookup-hero {
  background: linear-gradient(135deg, #000f51 0%, #12379d 100%);
  color: #fff;
  padding: 64px 0;
}

.lookup-hero__inner {
  max-width: 760px;
}

.lookup-badge {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 14px;
}

.lookup-title {
  font-size: 34px;
  font-weight: 800;
  margin-bottom: 12px;
}

.lookup-desc {
  font-size: 15px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.92);
}

.lookup-search-card,
.lookup-head-card,
.progress-card,
.detail-card,
.summary-card,
.status-note {
  background: #fff;
  border-radius: 24px;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.08);
  padding: 24px;
}

.lookup-input {
  min-height: 48px;
  border-radius: 14px;
  border: 1px solid #dbe2ea;
}

.lookup-input:focus {
  border-color: #0b2c86;
  box-shadow: 0 0 0 0.2rem rgba(11, 44, 134, 0.12);
}

.lookup-btn {
  border: none;
  border-radius: 14px;
  min-height: 48px;
  background: #000f51;
  color: #fff;
  font-weight: 700;
}

.lookup-btn:hover {
  background: #12379d;
}

.lookup-head-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.lookup-code {
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
}

.lookup-date {
  color: #64748b;
  margin-top: 6px;
}

.status-badge,
.payment-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  padding: 8px 14px;
  font-weight: 700;
  font-size: 13px;
}

.status-primary {
  background: #e8efff;
  color: #1242b3;
}

.status-info {
  background: #e0f2fe;
  color: #0369a1;
}

.status-success {
  background: #dcfce7;
  color: #166534;
}

.status-warning {
  background: #fef3c7;
  color: #92400e;
}

.status-danger {
  background: #fee2e2;
  color: #b91c1c;
}

.payment-success {
  background: #dcfce7;
  color: #166534;
}

.payment-warning {
  background: #fef3c7;
  color: #92400e;
}

.payment-default {
  background: #eef2f7;
  color: #475569;
}

.progress-line {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}

.progress-step {
  text-align: center;
}

.progress-step__dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #dbe2ea;
  margin: 0 auto 8px;
}

.progress-step.active .progress-step__dot {
  background: #12379d;
}

.progress-step__label {
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
}

.progress-step.active .progress-step__label {
  color: #0f172a;
  font-weight: 700;
}

.progress-step {
  position: relative;
}

.progress-step:not(:first-child)::before {
  content: "";
  position: absolute;
  top: 8px;
  left: calc(-50% - 6px);
  width: calc(100% + 12px);
  height: 2px;
  background: #dbe2ea;
  z-index: 0;
}

.progress-step.active:not(:first-child)::before {
  background: #12379d;
}

.progress-step__dot {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 11px;
  font-weight: 900;
}

.detail-card__title,
.summary-card__title {
  font-size: 20px;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 18px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.detail-item--full {
  grid-column: 1 / -1;
}

.detail-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 6px;
}

.detail-value {
  font-weight: 600;
  color: #111827;
  line-height: 1.7;
}

.lookup-item-list {
  display: grid;
  gap: 14px;
}

.lookup-item {
  display: grid;
  grid-template-columns: 88px 1fr auto;
  gap: 14px;
  align-items: center;
  border: 1px solid #e5e7eb;
  border-radius: 18px;
  padding: 14px;
}

.lookup-item__img {
  width: 88px;
  height: 88px;
  object-fit: cover;
  border-radius: 14px;
  background: #f8fafc;
}

.lookup-item__name {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 6px;
}

.lookup-item__meta {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
}

.lookup-item__price {
  text-align: right;
  color: #0f172a;
  font-size: 14px;
}

.lookup-item__price strong {
  display: block;
  margin-top: 6px;
  font-size: 16px;
}

.sum-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #eef2f7;
  color: #334155;
}

.sum-row.total {
  border-bottom: none;
  padding-top: 18px;
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
}

.status-note {
  font-weight: 700;
  line-height: 1.7;
}

@media (max-width: 991.98px) {
  .lookup-title {
    font-size: 28px;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .progress-line {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 767.98px) {
  .lookup-item {
    grid-template-columns: 1fr;
  }

  .lookup-item__img {
    width: 100%;
    height: 220px;
  }

  .lookup-item__price {
    text-align: left;
  }
}
</style>