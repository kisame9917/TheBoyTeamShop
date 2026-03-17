<template>
  <div class="checkout-success-page">
    <div class="container py-5">
      <div class="success-card">
        <div class="success-icon">
          <i class="bi bi-check2-circle"></i>
        </div>

        <h1 class="success-title">Thanh toán thành công</h1>

        <div class="success-desc">
          Đơn hàng của bạn đã được xác nhận thanh toán thành công.
        </div>

        <div class="success-info">
          <div class="success-info__row">
            <span>Mã đơn hàng</span>
            <strong>{{ orderId || "-" }}</strong>
          </div>

          <div class="success-info__row">
            <span>Phương thức thanh toán</span>
            <strong>{{ gatewayLabel }}</strong>
          </div>

          <div v-if="amount > 0" class="success-info__row">
            <span>Số tiền</span>
            <strong>{{ formatCurrency(amount) }}</strong>
          </div>
        </div>

        <div class="success-actions">
          <router-link to="/" class="btn-home">
            Về trang chủ
          </router-link>

          <router-link to="/tra-cuu-don-hang" class="btn-lookup">
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

const orderId = computed(() => String(route.query.orderId || ""));
const amount = computed(() => Number(route.query.amount || 0));

const gateway = computed(() => {
  return String(
    route.query.gateway ||
      route.query.paymentMethod ||
      route.query.method ||
      ""
  ).toUpperCase();
});

const gatewayLabel = computed(() => {
  if (gateway.value === "VNPAY") return "VNPAY";
  if (gateway.value === "MOMO") return "MoMo";
  if (gateway.value === "CARD") return "Thẻ tín dụng / ghi nợ";
  if (gateway.value === "BANK_QR" || gateway.value === "QR") return "Chuyển khoản QR";
  return "Thanh toán online";
});

const formatCurrency = (value) => {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(Number(value || 0));
};
</script>

<style scoped>
.checkout-success-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f7fc 0%, #eef2ff 100%);
  display: flex;
  align-items: center;
}

.success-card {
  max-width: 680px;
  margin: 0 auto;
  background: #fff;
  border-radius: 24px;
  padding: 32px 24px;
  text-align: center;
  box-shadow: 0 20px 48px rgba(15, 23, 42, 0.12);
  border: 1px solid rgba(15, 23, 42, 0.08);
}

.success-icon {
  font-size: 64px;
  color: #16a34a;
  margin-bottom: 14px;
}

.success-title {
  font-size: 30px;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 10px;
}

.success-desc {
  color: #64748b;
  font-size: 15px;
  line-height: 1.6;
  margin-bottom: 24px;
}

.success-info {
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  background: #f8fafc;
  padding: 14px 16px;
  text-align: left;
}

.success-info__row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  color: #334155;
}

.success-info__row + .success-info__row {
  border-top: 1px dashed #dbe2ee;
}

.success-actions {
  margin-top: 24px;
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

.btn-home,
.btn-lookup {
  min-width: 180px;
  height: 46px;
  padding: 0 18px;
  border-radius: 12px;
  font-weight: 700;
  font-size: 15px;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.btn-home {
  background: #0f172a;
  color: #fff;
  border: 1px solid #0f172a;
}

.btn-home:hover {
  background: #1e293b;
  border-color: #1e293b;
  color: #fff;
}

.btn-lookup {
  background: #fff;
  color: #0f172a;
  border: 1px solid #cbd5e1;
}

.btn-lookup:hover {
  background: #f8fafc;
  color: #0f172a;
  border-color: #94a3b8;
}

@media (max-width: 576px) {
  .checkout-success-page {
    align-items: flex-start;
  }

  .success-card {
    padding: 24px 16px;
    border-radius: 20px;
  }

  .success-title {
    font-size: 24px;
  }

  .success-info__row {
    flex-direction: column;
    align-items: flex-start;
  }

  .btn-home,
  .btn-lookup {
    width: 100%;
  }
}
</style>