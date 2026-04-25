<template>
  <div class="pos-mock-payment-page">
    <div class="payment-card">
      <div class="payment-header">
        <div class="merchant">POS Mock Payment</div>
        <div class="title">Xác nhận thanh toán QR</div>
        <div class="subtitle">
          Trang thanh toán giả lập dành cho đơn tại quầy
        </div>
      </div>

      <div class="payment-body">
        <div class="info-block">
          <div class="info-row">
            <span class="label">Mã hóa đơn</span>
            <span class="value">{{ maHoaDon || "-" }}</span>
          </div>
          <div class="info-row">
            <span class="label">Order ID</span>
            <span class="value">{{ orderId || "-" }}</span>
          </div>
          <div class="info-row">
            <span class="label">Request code</span>
            <span class="value break-all">{{ requestCode || "-" }}</span>
          </div>
          <div class="info-row">
            <span class="label">Phương thức</span>
            <span class="value">{{ method || "BANK_QR" }}</span>
          </div>
          <div class="info-row total-row">
            <span class="label">Số tiền</span>
            <span class="value total">{{ formatCurrency(amountNumber) }}</span>
          </div>
        </div>

        <div class="transaction-block">
          <label class="input-label">Mã giao dịch giả lập</label>
          <input
            v-model="generatedTransactionCode"
            class="text-input"
            type="text"
            placeholder="Tự sinh mã giao dịch"
          />

          <label class="input-label">Ghi chú</label>
          <textarea
            v-model="note"
            class="text-area"
            rows="3"
            placeholder="Nhập ghi chú thanh toán"
          />
        </div>

        <div v-if="message" class="alert" :class="messageType">
          {{ message }}
        </div>

        <div class="action-group">
          <button
            class="btn btn-secondary"
            type="button"
            @click="fillSampleNote"
            :disabled="loading || success"
          >
            Tạo ghi chú mẫu
          </button>

          <button
            class="btn btn-primary"
            type="button"
            @click="confirmPayment"
            :disabled="loading || success || !canSubmit"
          >
            {{ loading ? "Đang xác nhận..." : success ? "Đã thanh toán" : "Xác nhận thanh toán" }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import axios from "axios";

const route = useRoute();

const API_BASE = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/$/, "");

const orderId = ref("");
const requestCode = ref("");
const method = ref("BANK_QR");
const amount = ref("");
const maHoaDon = ref("");

const loading = ref(false);
const success = ref(false);
const message = ref("");
const messageType = ref("info");

const generatedTransactionCode = ref("");
const note = ref("");

const amountNumber = computed(() => {
  const n = Number(amount.value || 0);
  return Number.isNaN(n) ? 0 : n;
});

const canSubmit = computed(() => {
  return !!orderId.value && !!requestCode.value;
});

function formatCurrency(value) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(Number(value || 0));
}

function createTransactionCode() {
  const now = new Date();
  const pad = (n) => String(n).padStart(2, "0");

  return [
    "PAY",
    now.getFullYear(),
    pad(now.getMonth() + 1),
    pad(now.getDate()),
    pad(now.getHours()),
    pad(now.getMinutes()),
    pad(now.getSeconds()),
    Math.floor(Math.random() * 100000),
  ].join("");
}

function buildFakeNote() {
  return (
    note.value?.trim() ||
    `POS QR mock payment - ${method.value || "BANK_QR"} - ${maHoaDon.value || orderId.value} - ${new Date().toLocaleString("vi-VN")}`
  );
}

function fillSampleNote() {
  note.value = `Khách đã xác nhận thanh toán QR giả lập lúc ${new Date().toLocaleString("vi-VN")}`;
}

async function confirmPayment() {
  if (!canSubmit.value) {
    message.value = "Thiếu orderId hoặc requestCode.";
    messageType.value = "error";
    return;
  }

  loading.value = true;
  message.value = "";
  messageType.value = "info";

  try {
    if (!generatedTransactionCode.value) {
      generatedTransactionCode.value = createTransactionCode();
    }

    const payload = {
      requestCode: requestCode.value,
      maGiaoDich: generatedTransactionCode.value,
      soTien: amountNumber.value || 0,
      ghiChu: buildFakeNote(),
      paymentGateway: method.value || "BANK_QR",
    };

    const url = `${API_BASE}/api/hoa-don/draft/${orderId.value}/pos-qr/confirm`;

    console.log("POS MOCK API_BASE =", API_BASE);
    console.log("POS MOCK confirm url =", url);
    console.log("POS MOCK payload =", payload);

    const { data } = await axios.post(url, payload);

    success.value = true;
    messageType.value = "success";
    message.value =
      data?.message ||
      "Xác nhận thanh toán thành công. Bạn có thể quay lại màn hình bán hàng.";
  } catch (error) {
    console.error("confirmPayment error:", error);
    console.error("confirmPayment response:", error?.response?.data);
    console.error("confirmPayment status:", error?.response?.status);

    success.value = false;
    messageType.value = "error";

    if (!error?.response) {
      message.value =
        "Không gọi được API xác nhận thanh toán. Kiểm tra ngrok/Caddy/backend.";
    } else {
      message.value =
        error?.response?.data?.message ||
        error?.response?.data?.error ||
        `API lỗi ${error?.response?.status}`;
    }
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  orderId.value = route.query.orderId || "";
  requestCode.value = route.query.requestCode || "";
  method.value = route.query.method || "BANK_QR";
  amount.value = route.query.amount || 0;
  maHoaDon.value = route.query.maHoaDon || "";

  generatedTransactionCode.value = createTransactionCode();
  fillSampleNote();
});
</script>

<style scoped>
.pos-mock-payment-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8fafc 0%, #eef2ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  box-sizing: border-box;
}

.payment-card {
  width: 100%;
  max-width: 520px;
  background: #ffffff;
  border-radius: 20px;
  box-shadow: 0 12px 40px rgba(15, 23, 42, 0.12);
  overflow: hidden;
}

.payment-header {
  padding: 24px 24px 12px;
  text-align: center;
  border-bottom: 1px solid #eef2f7;
}

.merchant {
  font-size: 13px;
  font-weight: 600;
  color: #6366f1;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.title {
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 8px;
}

.subtitle {
  font-size: 14px;
  color: #64748b;
  line-height: 1.5;
}

.payment-body {
  padding: 24px;
}

.info-block,
.transaction-block {
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px dashed #e2e8f0;
}

.label {
  min-width: 120px;
  color: #64748b;
  font-size: 14px;
}

.value {
  flex: 1;
  text-align: right;
  color: #0f172a;
  font-size: 14px;
  font-weight: 600;
}

.break-all {
  word-break: break-all;
}

.total-row {
  border-bottom: none;
  padding-bottom: 0;
}

.total {
  font-size: 26px;
  font-weight: 800;
  color: #dc2626;
}

.input-label {
  display: block;
  margin-bottom: 8px;
  margin-top: 14px;
  font-size: 14px;
  font-weight: 600;
  color: #334155;
}

.text-input,
.text-area {
  width: 100%;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  padding: 12px 14px;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
  transition: all 0.2s ease;
}

.text-input:focus,
.text-area:focus {
  border-color: #6366f1;
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.12);
}

.alert {
  border-radius: 12px;
  padding: 12px 14px;
  font-size: 14px;
  margin-bottom: 18px;
  line-height: 1.5;
}

.alert.info {
  background: #eff6ff;
  color: #1d4ed8;
}

.alert.success {
  background: #ecfdf5;
  color: #047857;
}

.alert.error {
  background: #fef2f2;
  color: #dc2626;
}

.action-group {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.btn {
  flex: 1;
  min-width: 180px;
  border: none;
  border-radius: 12px;
  padding: 13px 16px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: 0.2s ease;
}

.btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.btn-primary {
  background: #2563eb;
  color: #fff;
}

.btn-primary:hover:not(:disabled) {
  background: #1d4ed8;
}

.btn-secondary {
  background: #e2e8f0;
  color: #0f172a;
}

.btn-secondary:hover:not(:disabled) {
  background: #cbd5e1;
}
</style>