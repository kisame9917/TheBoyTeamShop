<template>
  <div class="login-wrap">
    <div class="card">
      <h2 class="title">Nhập OTP</h2>
      <p class="sub">Nhập mã OTP đã gửi về email</p>

      <form @submit.prevent="onVerify">
       

        <div class="field">
          <label>OTP</label>
          <input v-model.trim="otp" inputmode="numeric"  />
        </div>

        <!-- ✅ nhớ type="submit" -->
        <button class="btn btn-primary" type="submit" :disabled="loading">
          {{ loading ? "Đang xác nhận..." : "Xác nhận OTP" }}
        </button>

        <!-- ✅ resend ngay tại đây -->
        <button
          type="button"
          class="btn-link"
          @click="onResend"
          :disabled="loading || resendLeft > 0"
        >
          {{ resendLeft > 0 ? `Gửi lại OTP (${resendLeft}s)` : "Gửi lại OTP" }}
        </button>

        <button type="button" class="btn-link" @click="router.push('/login')" :disabled="loading">
          Quay lại đăng nhập
        </button>

        <p v-if="success" class="success">{{ success }}</p>
        <p v-if="error" class="error">{{ error }}</p>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from "vue";
import { useRoute, useRouter } from "vue-router";
import http from "@/services/http";

const route = useRoute();
const router = useRouter();

const email = ref("");
const otp = ref("");
const loading = ref(false);
const error = ref("");
const success = ref("");

const resendLeft = ref(0);
let timer = null;

onMounted(() => {
  const q = route.query.email;
  if (typeof q === "string") email.value = q;

  // ✅ fallback: lấy từ sessionStorage nếu bạn có lưu ở ForgotPassword
  const saved = sessionStorage.getItem("fp_email");
  if (!email.value && saved) email.value = saved;
});

onBeforeUnmount(() => {
  if (timer) clearInterval(timer);
});

function startCooldown(sec = 60) {
  resendLeft.value = sec;
  if (timer) clearInterval(timer);
  timer = setInterval(() => {
    resendLeft.value -= 1;
    if (resendLeft.value <= 0) {
      clearInterval(timer);
      timer = null;
    }
  }, 1000);
}

async function onResend() {
  error.value = "";
  success.value = "";
  if (!email.value) return (error.value = "Thiếu email");
  if (loading.value || resendLeft.value > 0) return;

  loading.value = true;
  try {
    await http.post("/api/auth/forgot-password-otp", { email: email.value });
    success.value = "OTP đã được gửi lại (nếu email đúng). Vui lòng kiểm tra hộp thư.";
    startCooldown(60);
  } catch (e) {
    error.value =
      e?.response?.data?.message ||
      e?.response?.data ||
      e?.message ||
      "Gửi lại OTP thất bại";
  } finally {
    loading.value = false;
  }
}

async function onVerify() {
  error.value = "";
  success.value = "";
  if (!email.value) return (error.value = "Thiếu email");
  if (!otp.value) return (error.value = "Vui lòng nhập OTP");

  loading.value = true;
  try {
    await http.post("/api/auth/verify-otp", {
      email: email.value,
      otp: otp.value,
    });

    router.push({
      path: "/reset-password",
      query: { email: email.value, otp: otp.value },
    });
  } catch (e) {
    error.value =
      e?.response?.data?.message ||
      e?.response?.data ||
      e?.message ||
      "OTP không hợp lệ";
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-wrap {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
}
.card {
  width: 360px;
  max-width: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  background: #fff;
}
.title {
  margin: 0 0 6px;
  text-align: center;
}
.sub {
  margin: 0 0 16px;
  text-align: center;
  color: #64748b;
  font-size: 13px;
}
.field {
  margin-bottom: 12px;
  display: grid;
  gap: 6px;
}
input {
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  outline: none;
}
.btn {
  width: 100%;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid transparent;
  cursor: pointer;
}
.btn-link {
  margin-top: 10px;
  width: 100%;
  background: transparent;
  border: 0;
  cursor: pointer;
  color: #2954b8;
  font-weight: 600;
}
.btn-link:hover {
  text-decoration: underline;
}
.error {
  margin-top: 12px;
  color: #dc2626;
}
.success {
  margin-top: 12px;
  color: #16a34a;
}
</style>
