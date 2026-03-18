<template>
  <div class="checkout-success-page">
    <div class="success-wrapper">
      <div class="success-card">
        <div class="success-icon-wrap">
          <div class="success-icon">
            <i class="bi bi-check2"></i>
          </div>
        </div>

        <h1 class="success-title">Thanh toán thành công</h1>
        <p class="success-desc">
          Đơn hàng của bạn đã được xác nhận thanh toán thành công.
        </p>

        <div class="success-info-box">
          <div class="success-info-row">
            <span>Mã đơn hàng</span>
            <strong>{{ displayOrderCode }}</strong>
          </div>

          <div class="success-info-row">
            <span>Người nhận</span>
            <strong>{{ successData.customerName || "-" }}</strong>
          </div>

          <div class="success-info-row">
            <span>Số điện thoại</span>
            <strong>{{ successData.phone || "-" }}</strong>
          </div>

          <div class="success-info-row">
  <span>Email</span>
  <strong>{{ successData.email || "-" }}</strong>
</div>

          <div class="success-info-row">
            <span>Địa chỉ giao</span>
            <strong class="text-end info-address">{{ successData.address || "-" }}</strong>
          </div>

          <div class="success-info-row">
            <span>Phương thức thanh toán</span>
            <strong>{{ paymentMethodLabel }}</strong>
          </div>

          <div class="success-info-row total-row">
            <span>Tổng thanh toán</span>
            <strong class="total-amount">{{ formatCurrency(successData.total) }}</strong>
          </div>
        </div>

        <div class="success-actions">
          <router-link to="/" class="btn-home">
            Về trang chủ
          </router-link>

          <router-link to="/tra-cuu-don-hang" class="btn-detail">
            Tra cứu đơn hàng
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRoute } from "vue-router";

const route = useRoute();

function getStoredSuccessData() {
  try {
    return JSON.parse(sessionStorage.getItem("checkout_success_data") || "{}");
  } catch {
    return {};
  }
}

const stored = getStoredSuccessData();

const successData = computed(() => ({
  orderId: stored.orderId || String(route.query.orderId || ""),
  maHoaDon: stored.maHoaDon || "",
  customerName: stored.customerName || "",
  phone: stored.phone || "",
  email: stored.email || "",
  address: stored.address || "",
  paymentMethod: stored.paymentMethod || "",
  paymentLabel: stored.paymentLabel || "",
  total: Number(stored.total || 0),
}));

const displayOrderCode = computed(() => {
  return successData.value.maHoaDon || successData.value.orderId || "-";
});

const paymentMethodLabel = computed(() => {
  const method = String(successData.value.paymentMethod || "").toLowerCase();

  if (method === "cod") return "COD";
  if (method === "bank_qr" || method === "qr") return "Chuyển khoản QR";
  if (method === "vnpay") return "VNPAY";
  if (method === "momo") return "MoMo";
  if (method === "card") return "Thẻ tín dụng / ghi nợ";

  return successData.value.paymentLabel || "Thanh toán online";
});

function formatCurrency(value) {
  return new Intl.NumberFormat("vi-VN").format(Number(value || 0)) + " đ";
}
</script>

<style scoped>
.checkout-success-page {
  min-height: calc(100vh - 120px);
  background: #f4f6fb;
  padding: 40px 16px 60px;
}

.success-wrapper {
  max-width: 900px;
  margin: 0 auto;
}

.success-card {
  width: 100%;
  max-width: 560px;
  margin: 0 auto;
  background: #fff;
  border-radius: 24px;
  padding: 34px 28px 28px;
  text-align: center;
  box-shadow: 0 16px 36px rgba(10, 24, 74, 0.08);
}

.success-icon-wrap {
  display: flex;
  justify-content: center;
  margin-bottom: 18px;
}

.success-icon {
  width: 78px;
  height: 78px;
  border-radius: 50%;
  background: linear-gradient(135deg, #16a34a 0%, #22c55e 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
}

.success-title {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  color: #0f172a;
}

.success-desc {
  margin: 12px 0 22px;
  color: #64748b;
  font-size: 14px;
}

.success-info-box {
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #f8fafc;
  padding: 16px 18px;
  text-align: left;
}

.success-info-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  padding: 12px 0;
  font-size: 15px;
  color: #334155;
}

.success-info-row + .success-info-row {
  border-top: 1px solid #e2e8f0;
}

.success-info-row span {
  color: #475569;
}

.success-info-row strong {
  color: #0f172a;
  font-weight: 700;
  text-align: right;
}

.info-address {
  max-width: 280px;
  line-height: 1.5;
}

.total-row strong {
  color: #dc2626;
  font-size: 18px;
  font-weight: 800;
}

.success-actions {
  display: flex;
  justify-content: center;
  gap: 14px;
  margin-top: 22px;
  flex-wrap: wrap;
}

.btn-home,
.btn-detail {
  min-width: 180px;
  height: 46px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 800;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.btn-home {
  background: #000f51;
  color: #fff;
  border: none;
}

.btn-home:hover {
  background: #001a72;
  color: #fff;
}

.btn-detail {
  background: #fff;
  color: #0f172a;
  border: 1px solid #cbd5e1;
}

.btn-detail:hover {
  background: #f8fafc;
  color: #0f172a;
}

@media (max-width: 576px) {
  .success-card {
    padding: 26px 16px 20px;
    border-radius: 18px;
  }

  .success-title {
    font-size: 24px;
  }

  .success-info-row {
    flex-direction: column;
    gap: 6px;
  }

  .success-info-row strong {
    text-align: left;
  }

  .info-address {
    max-width: 100%;
  }

  .btn-home,
  .btn-detail {
    width: 100%;
    min-width: 100%;
  }
}
</style>