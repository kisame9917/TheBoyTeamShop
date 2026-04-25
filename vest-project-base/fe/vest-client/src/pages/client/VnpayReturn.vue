<template>
  <div class="container py-5 text-center">
    <h2 class="mb-3">Đang xác nhận thanh toán...</h2>
    <p class="text-muted mb-4">{{ message }}</p>

    <div v-if="loading">Vui lòng chờ...</div>

    <div v-if="!loading && success" class="text-success fw-bold">
      Thanh toán thành công
    </div>

    <div v-if="!loading && !success" class="text-danger fw-bold">
      {{ message }}
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { useCart } from "../../composables/useCart";
const router = useRouter();
const cart = useCart();
const API_BASE = import.meta.env.VITE_API_BASE_URL || "";

const loading = ref(true);
const success = ref(false);
const message = ref("Hệ thống đang xử lý kết quả thanh toán...");

async function confirmPayment(orderId, amount, transactionNo) {
  const res = await fetch(
    `${API_BASE}/api/online-checkout/${orderId}/confirm-payment`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        maGiaoDich: transactionNo || "VNPAY_SANDBOX_TEST",
        soTien: amount,
        ghiChu: "Khách thanh toán VNPAY",
        paymentGateway: "VNPAY",
      }),
    },
  );

  const data = await res.json().catch(() => ({}));

  if (!res.ok) {
    throw new Error(data?.message || "Xác nhận thanh toán thất bại");
  }

  return data;
}

onMounted(async () => {
  try {
    console.log("VNPAY RETURN PAGE MOUNTED");

    const params = new URLSearchParams(window.location.search);

    const orderId = params.get("vnp_TxnRef");
    const responseCode = params.get("vnp_ResponseCode");
    const amount = Number(params.get("vnp_Amount") || 0) / 100;
    const transactionNo = params.get("vnp_TransactionNo");

    console.log("VNPAY PARAMS:", {
      orderId,
      responseCode,
      amount,
      transactionNo,
      fullUrl: window.location.href,
    });

    if (!orderId) {
      throw new Error("Không tìm thấy orderId từ VNPAY return");
    }

    if (responseCode !== "00") {
      throw new Error(
        `Thanh toán không thành công. Mã phản hồi: ${responseCode || "N/A"}`,
      );
    }

    console.log("BEFORE confirmPayment");
    await confirmPayment(orderId, amount, transactionNo);
let customerInfo = {};
try {
  customerInfo = JSON.parse(
    sessionStorage.getItem("pending_checkout_customer_info") || "{}"
  );
} catch (e) {
  customerInfo = {};
}

try {
  if (cart && typeof cart.clearCart === "function") {
    cart.clearCart();
  }
} catch (err) {
  console.error("clearCart error:", err);
}

localStorage.removeItem("cart_items");
localStorage.removeItem("VEST_CLIENT_CART");

sessionStorage.setItem(
  "checkout_success_data",
  JSON.stringify({
    orderId,
    maHoaDon: customerInfo.maHoaDon || "",
    customerName: customerInfo.customerName || "",
    phone: customerInfo.phone || "",
    email: customerInfo.email || "",
    address: customerInfo.address || "",
    paymentMethod: "vnpay",
    paymentLabel: "VNPAY",
    total: amount,
  })
);

sessionStorage.removeItem("pending_checkout_customer_info");

success.value = true;
message.value = "Thanh toán thành công";

setTimeout(() => {
  router.replace({
    path: "/checkout/success",
    query: { orderId },
  });
}, 1200);
  } catch (error) {
    console.error("VNPAY RETURN ERROR:", error);
    success.value = false;
    message.value = error.message || "Có lỗi khi xác nhận thanh toán";
  } finally {
    loading.value = false;
  }
});
</script>
