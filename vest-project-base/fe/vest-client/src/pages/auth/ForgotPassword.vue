<template>
  <div class="fp-page">
    <div class="fp-card">
      <div class="fp-title">Quên mật khẩu</div>
      <div class="fp-subtitle">Nhập email để nhận OTP.</div>

      <form class="fp-form" @submit.prevent="sendOtp">
        <div v-if="error" class="alert">{{ error }}</div>
        <div v-if="success" class="success">{{ success }}</div>

        <div class="form-group">
          <label for="email">Email</label>
          <input
            v-model.trim="email"
            id="email"
            type="email"
            class="form-control"
            placeholder="Nhập email đã đăng ký"
            required
            autocomplete="email"
          />
        </div>

        <button class="btn-primary" type="submit" :disabled="loading || !email">
          {{ loading ? "Đang gửi..." : "Gửi OTP" }}
        </button>

        <div class="row-actions">
          <button type="button" class="btn-link" @click="goLogin">
            Quay lại đăng nhập
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

const router = useRouter();
const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

const email = ref("");
const loading = ref(false);
const error = ref("");
const success = ref("");

function goLogin() {
  router.push("/login"); // đổi theo route dự án bạn
}

async function sendOtp() {
  error.value = "";
  success.value = "";
  loading.value = true;

  try {
    const res = await axios.post(
      `${BASE_URL}/api/client/auth/forgot-password-otp`,
      {
        email: email.value,
      },
    );

    // lưu email để trang OTP dùng
    sessionStorage.setItem("FP_EMAIL", email.value);
    sessionStorage.removeItem("FP_OTP");

    success.value = res?.data || "OK";
    router.push("/otp-verify"); // route tới OtpVerify.vue
  } catch (e) {
    error.value =
      e?.response?.data?.message ||
      e?.response?.data?.error ||
      e?.response?.data ||
      e?.message ||
      "Gửi OTP thất bại.";
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.fp-page {
  min-height: calc(100vh - 140px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 12px;
}
.fp-card {
  width: 100%;
  max-width: 460px;
  border: 1px solid #eee;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.06);
  background: #fff;
}
.fp-title {
  font-size: 22px;
  font-weight: 800;
  margin-bottom: 6px;
  text-align: center;
}
.fp-subtitle {
  font-size: 14px;
  color: #666;
  text-align: center;
  margin-bottom: 14px;
}
.fp-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.alert {
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #ffd1d1;
  background: #fff5f5;
  color: #b42318;
  font-size: 14px;
}
.success {
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #c7f5d3;
  background: #f1fff5;
  color: #067647;
  font-size: 14px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.form-control {
  height: 42px;
  border: 1px solid #ddd;
  border-radius: 10px;
  padding: 0 12px;
  outline: none;
}
.btn-primary {
  height: 44px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 800;
  background: #0c63e7;
  color: #fff;
}
.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
.row-actions {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 14px;
}
.btn-link {
  background: transparent;
  border: none;
  padding: 0;
  color: #111;
  cursor: pointer;
  opacity: 0.85;
  text-decoration: underline;
}
</style>
