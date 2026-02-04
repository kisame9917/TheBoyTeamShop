<template>
  <div class="login-wrap">
    <div class="card">
      <h2 class="title">Đặt lại mật khẩu</h2>
      <p class="sub">Nhập OTP đã gửi về email để đổi mật khẩu</p>

      <form @submit.prevent="onReset">
        <div class="field">
          <label>Email</label>
          <input v-model.trim="email" type="email" placeholder="vd: abc@gmail.com" />
        </div>

        <div class="field">
          <label>OTP</label>
          <input v-model.trim="otp" inputmode="numeric" placeholder="6 số" />
        </div>

        <div class="field">
          <label>Mật khẩu mới</label>
          <input v-model="newPassword" type="password" placeholder="tối thiểu 6 ký tự" />
        </div>

        <button class="btn btn-primary" :disabled="loading">
          {{ loading ? 'Đang đổi mật khẩu...' : 'Đổi mật khẩu' }}
        </button>

        <button type="button" class="btn-link" @click="router.push('/forgot-password')" :disabled="loading">
          Gửi lại OTP
        </button>

        <p v-if="success" class="success">{{ success }}</p>
        <p v-if="error" class="error">{{ error }}</p>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import http from "@/services/http";

const route = useRoute();
const router = useRouter();

const email = ref("");
const otp = ref("");
const newPassword = ref("");

const loading = ref(false);
const error = ref("");
const success = ref("");

onMounted(() => {
  const qEmail = route.query.email;
  if (typeof qEmail === "string") email.value = qEmail;
});

async function onReset() {
  error.value = "";
  success.value = "";

  if (!email.value) return (error.value = "Vui lòng nhập email");
  if (!otp.value) return (error.value = "Vui lòng nhập OTP");
  if (!newPassword.value || newPassword.value.length < 6)
    return (error.value = "Mật khẩu mới tối thiểu 6 ký tự");

  loading.value = true;
  try {
    await http.post("/api/auth/reset-password-otp", {
      email: email.value,
      otp: otp.value,
      newPassword: newPassword.value,
    });

    success.value = "Đổi mật khẩu thành công. Đang chuyển về trang đăng nhập...";
    setTimeout(() => router.push("/login"), 900);
  } catch (e) {
    error.value = e?.response?.data || e?.message || "Đổi mật khẩu thất bại";
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
