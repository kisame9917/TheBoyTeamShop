<template>
  <div class="mock-payment-page">
    <div class="container py-4 py-md-5">
      <div class="row justify-content-center">
        <div class="col-lg-7 col-xl-6">
          <div class="mock-card">
            <div class="mock-card__header">
              <div>
                <div class="mock-card__brand">{{ gatewayLabel }}</div>
                <div class="mock-card__subtitle">
                  Cổng thanh toán mô phỏng để kiểm thử luồng thanh toán
                </div>
              </div>

              <div class="mock-card__status">
                <i class="bi bi-shield-check"></i>
                Sandbox
              </div>
            </div>

            <div class="mock-card__body">
              <div class="gateway-topbar">
                <div class="gateway-topbar__brand">
                  <span class="gateway-logo">{{ gatewayShort }}</span>
                  <div>
                    <div class="gateway-name">{{ gatewayLabel }}</div>
                    <div class="gateway-sub">Cổng thanh toán bảo mật</div>
                  </div>
                </div>

                <div class="gateway-secure">
                  <i class="bi bi-shield-lock-fill"></i>
                  Bảo mật
                </div>
              </div>

              <div class="pay-summary mt-4">
                <div class="pay-summary__row">
                  <span>Mã đơn hàng</span>
                  <strong>{{ maHoaDon || "-" }}</strong>
                </div>

                <div class="pay-summary__row">
                  <span>ID đơn</span>
                  <strong>{{ orderId || "-" }}</strong>
                </div>

                <div class="pay-summary__row">
                  <span>Mã giao dịch</span>
                  <strong>{{ generatedTransactionCode }}</strong>
                </div>

                <div class="pay-summary__row">
                  <span>Phương thức</span>
                  <strong>{{ gatewayLabel }}</strong>
                </div>

                <div class="pay-summary__row total">
                  <span>Số tiền thanh toán</span>
                  <strong>{{ formatMoney(amount) }} đ</strong>
                </div>
              </div>

              <div v-if="method === 'VNPAY'" class="gateway-box mt-4">
                <div class="gateway-box__title">Chọn ngân hàng thanh toán</div>

                <div class="gateway-options mt-3">
                  <label
                    v-for="bank in fakeBanks"
                    :key="bank.value"
                    class="gateway-option"
                    :class="{ active: selectedBank === bank.value }"
                  >
                    <input
                      v-model="selectedBank"
                      class="form-check-input"
                      type="radio"
                      :value="bank.value"
                    />
                    <span>{{ bank.label }}</span>
                  </label>
                </div>
              </div>

              <div v-if="method === 'CARD'" class="gateway-box mt-4">
                <div class="gateway-box__title">Thông tin thẻ</div>

                <div class="card-visual mb-3">
                  <div class="card-visual__chip"></div>
                  <div class="card-visual__number">{{ fakeCard.number }}</div>
                  <div class="card-visual__bottom">
                    <span>{{ fakeCard.name }}</span>
                    <span>{{ fakeCard.exp }}</span>
                  </div>
                </div>

                <div class="row g-3 mt-1">
                  <div class="col-12">
                    <label class="form-label">Số thẻ</label>
                    <input
                      v-model="fakeCard.number"
                      type="text"
                      class="form-control input-ui"
                      placeholder="9704 0000 0000 0001"
                    />
                  </div>

                  <div class="col-md-6">
                    <label class="form-label">Tên chủ thẻ</label>
                    <input
                      v-model="fakeCard.name"
                      type="text"
                      class="form-control input-ui"
                      placeholder="NGUYEN VAN A"
                    />
                  </div>

                  <div class="col-md-3">
                    <label class="form-label">MM/YY</label>
                    <input
                      v-model="fakeCard.exp"
                      type="text"
                      class="form-control input-ui"
                      placeholder="12/28"
                    />
                  </div>

                  <div class="col-md-3">
                    <label class="form-label">CVV</label>
                    <input
                      v-model="fakeCard.cvv"
                      type="password"
                      class="form-control input-ui"
                      placeholder="123"
                    />
                  </div>
                </div>
              </div>

              <div v-if="method === 'MOMO'" class="gateway-box mt-4">
                <div class="gateway-box__title">Ví điện tử MoMo</div>

                <div class="momo-wallet mt-3">
                  <div class="momo-wallet__icon">M</div>
                  <div>
                    <div class="momo-wallet__name">MoMo Sandbox Wallet</div>
                    <div class="momo-wallet__balance">
                      Số dư khả dụng: 99.999.999 đ
                    </div>
                  </div>
                </div>
              </div>

              <div class="gateway-note mt-4">
                <i class="bi bi-lock-fill"></i>
                <span>
                  Thông tin thanh toán được mã hóa và bảo mật trong suốt giao dịch.
                </span>
              </div>

              <div v-if="successMessage" class="alert alert-success mt-4 mb-0">
                {{ successMessage }}
              </div>

              <div v-if="error" class="alert alert-danger mt-4 mb-0">
                {{ error }}
              </div>

              <div class="mock-actions mt-4">
                <button
                  type="button"
                  class="btn-cancel"
                  :disabled="loading"
                  @click="goBack"
                >
                  Hủy
                </button>

                <button
                  type="button"
                  class="btn-confirm"
                  :disabled="loading || !orderId"
                  @click="confirmSuccess"
                >
                  {{ loading ? "ĐANG XỬ LÝ..." : "Xác nhận thanh toán" }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <transition name="fade">
      <div
        v-if="toast.show"
        class="app-toast"
        :class="`app-toast--${toast.type}`"
      >
        <i
          class="bi"
          :class="
            toast.type === 'success'
              ? 'bi-check-circle-fill'
              : toast.type === 'warning'
                ? 'bi-exclamation-triangle-fill'
                : 'bi-info-circle-fill'
          "
        ></i>
        <span>{{ toast.message }}</span>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useCart } from "../../composables/useCart";

const route = useRoute();
const router = useRouter();
const cart = useCart();

const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

const loading = ref(false);
const error = ref("");
const successMessage = ref("");

const orderId = route.query.orderId ? String(route.query.orderId) : "";
const method = route.query.method ? String(route.query.method).toUpperCase() : "";
const amount = Number(route.query.amount || 0);
const maHoaDon = route.query.maHoaDon ? String(route.query.maHoaDon) : "";

const selectedBank = ref("NCB");
const note = ref("Đã thanh toán giả lập thành công");

const fakeBanks = [
  { value: "NCB", label: "NCB" },
  { value: "VCB", label: "Vietcombank" },
  { value: "TCB", label: "Techcombank" },
  { value: "MB", label: "MB Bank" },
  { value: "ACB", label: "ACB" },
];

const fakeCard = reactive({
  number: "9704 0000 0000 0001",
  name: "NGUYEN VAN A",
  exp: "12/28",
  cvv: "123",
});

const toast = reactive({
  show: false,
  message: "",
  type: "success",
});

let toastTimer = null;

const gatewayLabel = computed(() => {
  if (method === "VNPAY") return "VNPAY";
  if (method === "MOMO") return "MoMo";
  if (method === "CARD") return "Thẻ tín dụng / ghi nợ";
  return "Thanh toán online";
});

const gatewayShort = computed(() => {
  if (method === "VNPAY") return "VN";
  if (method === "MOMO") return "MM";
  if (method === "CARD") return "CC";
  return "PAY";
});

const generatedTransactionCode = computed(() => {
  const prefix =
    method === "VNPAY"
      ? "VNPAY"
      : method === "MOMO"
        ? "MOMO"
        : method === "CARD"
          ? "CARD"
          : "PAY";

  return `${prefix}${Date.now()}`;
});

function formatMoney(v) {
  const n = Number(v) || 0;
  return n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
}

function showToast(message, type = "success") {
  if (!message) return;

  if (toastTimer) clearTimeout(toastTimer);

  toast.message = message;
  toast.type = type;
  toast.show = true;

  toastTimer = setTimeout(() => {
    toast.show = false;
  }, 2500);
}

function buildFakeNote() {
  if (method === "VNPAY") {
    return note.value?.trim() || `Thanh toán VNPAY giả lập thành công qua ${selectedBank.value}`;
  }

  if (method === "MOMO") {
    return note.value?.trim() || "Thanh toán MoMo giả lập thành công";
  }

  if (method === "CARD") {
    return note.value?.trim() || "Thanh toán thẻ giả lập thành công";
  }

  return note.value?.trim() || "Thanh toán online giả lập thành công";
}

function clearCheckoutCart() {
  try {
    if (cart && typeof cart.clearCart === "function") {
      cart.clearCart();
    }
  } catch (err) {
    console.error("clearCart error:", err);
  }

  localStorage.removeItem("cart");
  localStorage.removeItem("cartItems");
  localStorage.removeItem("checkout-cart");
  localStorage.removeItem("checkout_cart");
}

function buildSuccessUrl() {
  const params = new URLSearchParams();

  params.set("orderId", orderId);
  params.set("gateway", method);

  if (amount > 0) {
    params.set("amount", String(amount));
  }

  return `/checkout/success?${params.toString()}`;
}

async function confirmSuccess() {
  if (!orderId) {
    error.value = "Không tìm thấy mã đơn hàng để xác nhận thanh toán.";
    return;
  }

  try {
    loading.value = true;
    error.value = "";
    successMessage.value = "";

    const payload = {
      maGiaoDich: generatedTransactionCode.value,
      soTien: amount || 0,
      ghiChu: buildFakeNote(),
    };

    const response = await fetch(
      `${API_BASE}/api/online-checkout/${orderId}/confirm-payment`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      }
    );

    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
      throw new Error(data?.message || "Xác nhận thanh toán thất bại");
    }

    successMessage.value = "Thanh toán thành công";
    showToast("Thanh toán thành công", "success");

    clearCheckoutCart();

    const successUrl = buildSuccessUrl();

    setTimeout(() => {
      window.location.replace(successUrl);
    }, 400);
  } catch (e) {
    error.value = e?.message || "Có lỗi khi giả lập thanh toán";
    showToast(error.value, "warning");
  } finally {
    loading.value = false;
  }
}

function goBack() {
  router.replace("/checkout");
}
</script>

<style scoped>
.mock-payment-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f7fc 0%, #eef2ff 100%);
  padding: 12px 0;
}

.mock-card {
  background: #fff;
  border-radius: 24px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 20px 48px rgba(15, 23, 42, 0.12);
  overflow: hidden;
}

.mock-card__header {
  padding: 20px 22px;
  background: linear-gradient(90deg, #000f51 0%, #0f2f98 100%);
  color: #fff;
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.mock-card__brand {
  font-size: 26px;
  font-weight: 800;
  line-height: 1.2;
}

.mock-card__subtitle {
  margin-top: 6px;
  font-size: 14px;
  opacity: 0.92;
}

.mock-card__status {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}

.mock-card__body {
  padding: 22px;
}

.gateway-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.gateway-topbar__brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.gateway-logo {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  background: linear-gradient(135deg, #0f2f98 0%, #1d4ed8 100%);
  color: #fff;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}

.gateway-name {
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
}

.gateway-sub {
  font-size: 13px;
  color: #64748b;
}

.gateway-secure {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 999px;
  background: #ecfdf5;
  color: #166534;
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}

.pay-summary {
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  background: #f8fafc;
  padding: 14px 16px;
}

.pay-summary__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  font-size: 15px;
  color: #334155;
}

.pay-summary__row + .pay-summary__row {
  border-top: 1px dashed #dbe2ee;
}

.pay-summary__row.total strong {
  color: #dc2626;
  font-size: 20px;
}

.gateway-box {
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  background: #fff;
  padding: 16px;
}

.gateway-box__title {
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
}

.gateway-options {
  display: grid;
  gap: 10px;
}

.gateway-option {
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 12px 14px;
  background: #f8fafc;
  cursor: pointer;
  transition: all 0.2s ease;
}

.gateway-option:hover {
  border-color: #9db4ff;
  background: #fff;
}

.gateway-option.active {
  border-color: #0f2f98;
  background: #eef2ff;
}

.input-ui {
  min-height: 46px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid #dbe2ee;
  box-shadow: none !important;
}

.input-ui:focus {
  border-color: #9db4ff;
  background: #fff;
}

.card-visual {
  border-radius: 18px;
  padding: 18px;
  background: linear-gradient(135deg, #0f172a 0%, #1e3a8a 100%);
  color: #fff;
  min-height: 180px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.card-visual__chip {
  width: 44px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #facc15 0%, #fde68a 100%);
}

.card-visual__number {
  font-size: 22px;
  letter-spacing: 2px;
  font-weight: 700;
}

.card-visual__bottom {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 13px;
  text-transform: uppercase;
}

.momo-wallet {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border-radius: 16px;
  background: #fff1f2;
  border: 1px solid #fecdd3;
}

.momo-wallet__icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #d82d8b;
  color: #fff;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}

.momo-wallet__name {
  font-weight: 800;
  color: #111827;
}

.momo-wallet__balance {
  font-size: 13px;
  color: #64748b;
}

.gateway-note {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  color: #475569;
  font-size: 13px;
  line-height: 1.6;
}

.mock-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-cancel,
.btn-confirm {
  min-height: 46px;
  border-radius: 14px;
  padding: 0 18px;
  font-weight: 800;
  transition: all 0.2s ease;
}

.btn-cancel {
  border: 1px solid #d8dfec;
  background: #fff;
  color: #0f172a;
}

.btn-cancel:hover:not(:disabled) {
  border-color: #001a72;
  color: #001a72;
}

.btn-confirm {
  border: none;
  background: #000f51;
  color: #fff;
  box-shadow: 0 14px 28px rgba(0, 15, 81, 0.18);
}

.btn-confirm:hover:not(:disabled) {
  background: #001a72;
}

.btn-confirm:disabled,
.btn-cancel:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.app-toast {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 3000;
  min-width: 320px;
  max-width: 420px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  border-radius: 16px;
  box-shadow: 0 16px 36px rgba(2, 6, 23, 0.18);
  font-weight: 700;
}

.app-toast--success {
  background: #ecfdf5;
  border: 1px solid #86efac;
  color: #166534;
}

.app-toast--warning {
  background: #fff7ed;
  border: 1px solid #fdba74;
  color: #9a3412;
}

.app-toast--info {
  background: #eff6ff;
  border: 1px solid #93c5fd;
  color: #1d4ed8;
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@media (max-width: 767.98px) {
  .mock-card__header {
    flex-direction: column;
    align-items: stretch;
  }

  .gateway-topbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .mock-actions {
    flex-direction: column;
  }

  .btn-cancel,
  .btn-confirm {
    width: 100%;
  }

  .app-toast {
    left: 16px;
    right: 16px;
    top: 16px;
    min-width: 0;
    max-width: none;
  }
}
</style>