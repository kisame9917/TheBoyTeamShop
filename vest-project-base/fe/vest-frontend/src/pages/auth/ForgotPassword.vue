<template>
  <div class="login-wrap">
    <div class="card">
      <h2 class="title">Quên mật khẩu</h2>
      <p class="sub">Nhập email nhân viên để nhận OTP</p>

      <form @submit.prevent="onSendOtp">
        <div class="field">
          <label>Email</label>
          <input v-model.trim="email" type="email" placeholder="vd: abc@gmail.com" />
        </div>

        <button class="btn btn-primary" :disabled="loading">
          {{ loading ? 'Đang gửi OTP...' : 'Gửi OTP' }}
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
import { ref } from "vue";
import { useRouter } from "vue-router";
import http from "@/services/http"; // axios instance của bạn

const router = useRouter();
const email = ref("");
const loading = ref(false);
const error = ref("");
const success = ref("");

async function onSendOtp() {
  error.value = "";
  success.value = "";

  if (!email.value) {
    error.value = "Vui lòng nhập email";
    return;
  }

  loading.value = true;
  try {
    await http.post("/api/auth/forgot-password-otp", { email: email.value });

    success.value = "Nếu email đúng, OTP đã được gửi. Vui lòng kiểm tra hộp thư.";
    // chuyển sang trang nhập OTP + mật khẩu mới
    setTimeout(() => {
      router.push({ path: "/reset-password", query: { email: email.value } });
    }, 600);
  } catch (e) {
    error.value = e?.response?.data || e?.message || "Gửi OTP thất bại";
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
  background: white;
}
.title {
  margin: 0 0 6px 0;
  text-align: center;
}
.sub{
  margin: 0 0 16px 0;
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
.btn-link{
  margin-top: 10px;
  width: 100%;
  background: transparent;
  border: 0;
  cursor: pointer;
  color: #2954b8;
  font-weight: 600;
}
.btn-link:hover{ text-decoration: underline; }
.error { margin-top: 12px; color: #dc2626; }
.success { margin-top: 12px; color: #16a34a; }
</style>
