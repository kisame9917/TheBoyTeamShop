<template>
  <div class="login-wrap">
    <div class="card">
      <h2 class="title">Đặt lại mật khẩu</h2>
      <p class="sub">Nhập mật khẩu mới của bạn</p>

      <form @submit.prevent="onReset">
        <!-- Mật khẩu mới -->
        <div class="field">
          <label>Mật khẩu mới</label>
          <div class="password-wrap">
            <input
              v-model="newPassword"
              :type="showPassword ? 'text' : 'password'"
              placeholder="tối thiểu 6 ký tự"
              autocomplete="new-password"
            />

            <button
              type="button"
              class="eye-btn"
              @click="showPassword = !showPassword"
              :aria-label="showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
            >
              <svg v-if="!showPassword" viewBox="0 0 24 24" class="eye-icon" aria-hidden="true">
                <path
                  d="M12 5c5.5 0 9.7 4.2 11 7-1.3 2.8-5.5 7-11 7S2.3 14.8 1 12c1.3-2.8 5.5-7 11-7Zm0 2C7.7 7 4.2 10.1 3.1 12 4.2 13.9 7.7 17 12 17s7.8-3.1 8.9-5C19.8 10.1 16.3 7 12 7Zm0 2.5A2.5 2.5 0 1 1 12 14a2.5 2.5 0 0 1 0-5Z"
                />
              </svg>

              <svg v-else viewBox="0 0 24 24" class="eye-icon" aria-hidden="true">
                <path
                  d="M2.1 3.5 3.5 2.1l18.4 18.4-1.4 1.4-2.1-2.1c-1.8.8-3.9 1.2-6.4 1.2-5.5 0-9.7-4.2-11-7 1-2.1 3.4-5 6.7-6.4L2.1 3.5Zm9.9 4.2a4.3 4.3 0 0 0-1.5.3l1.2 1.2a2.5 2.5 0 0 1 3.1 3.1l1.2 1.2a4.5 4.5 0 0 0-4.2-5.8ZM12 17c1.6 0 3.1-.4 4.3-1l-1.6-1.6A2.5 2.5 0 0 1 9.6 9.3L7.6 7.3C5.6 8.5 4 10.6 3.1 12 4.2 13.9 7.7 17 12 17Zm0-10c4.3 0 7.8 3.1 8.9 5-.5 1-1.5 2.4-2.9 3.6l-1.4-1.4c.9-.8 1.6-1.7 2.1-2.8C19.8 10.1 16.3 7 12 7c-.8 0-1.6.1-2.3.3L8.2 5.8C9.4 5.3 10.7 5 12 5Z"
                />
              </svg>
            </button>
          </div>
        </div>

        <!-- Nhập lại mật khẩu -->
        <div class="field">
          <label>Nhập lại mật khẩu</label>
          <div class="password-wrap">
            <input
              v-model="confirmPassword"
              :type="showPassword ? 'text' : 'password'"
              placeholder="nhập lại mật khẩu mới"
              autocomplete="new-password"
            />

            <button
              type="button"
              class="eye-btn"
              @click="showPassword = !showPassword"
              :aria-label="showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
            >
              <svg v-if="!showPassword" viewBox="0 0 24 24" class="eye-icon" aria-hidden="true">
                <path
                  d="M12 5c5.5 0 9.7 4.2 11 7-1.3 2.8-5.5 7-11 7S2.3 14.8 1 12c1.3-2.8 5.5-7 11-7Zm0 2C7.7 7 4.2 10.1 3.1 12 4.2 13.9 7.7 17 12 17s7.8-3.1 8.9-5C19.8 10.1 16.3 7 12 7Zm0 2.5A2.5 2.5 0 1 1 12 14a2.5 2.5 0 0 1 0-5Z"
                />
              </svg>

              <svg v-else viewBox="0 0 24 24" class="eye-icon" aria-hidden="true">
                <path
                  d="M2.1 3.5 3.5 2.1l18.4 18.4-1.4 1.4-2.1-2.1c-1.8.8-3.9 1.2-6.4 1.2-5.5 0-9.7-4.2-11-7 1-2.1 3.4-5 6.7-6.4L2.1 3.5Zm9.9 4.2a4.3 4.3 0 0 0-1.5.3l1.2 1.2a2.5 2.5 0 0 1 3.1 3.1l1.2 1.2a4.5 4.5 0 0 0-4.2-5.8ZM12 17c1.6 0 3.1-.4 4.3-1l-1.6-1.6A2.5 2.5 0 0 1 9.6 9.3L7.6 7.3C5.6 8.5 4 10.6 3.1 12 4.2 13.9 7.7 17 12 17Zm0-10c4.3 0 7.8 3.1 8.9 5-.5 1-1.5 2.4-2.9 3.6l-1.4-1.4c.9-.8 1.6-1.7 2.1-2.8C19.8 10.1 16.3 7 12 7c-.8 0-1.6.1-2.3.3L8.2 5.8C9.4 5.3 10.7 5 12 5Z"
                />
              </svg>
            </button>
          </div>
        </div>

        <button class="btn btn-primary" type="submit" :disabled="loading">
          {{ loading ? "Đang đổi..." : "Đổi mật khẩu" }}
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
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import http from "@/services/http";

const route = useRoute();
const router = useRouter();

const email = ref("");
const otp = ref("");
const newPassword = ref("");
const confirmPassword = ref("");

const showPassword = ref(false);
const loading = ref(false);
const error = ref("");
const success = ref("");

onMounted(() => {
  if (typeof route.query.email === "string") email.value = route.query.email;
  if (typeof route.query.otp === "string") otp.value = route.query.otp;

  // thiếu email/otp => user nhảy thẳng => quay lại quên mật khẩu
  if (!email.value || !otp.value) {
    router.replace("/forgot-password");
  }
});

async function onReset() {
  if (loading.value) return;

  error.value = "";
  success.value = "";

  if (!newPassword.value || newPassword.value.length < 6) {
    error.value = "Mật khẩu tối thiểu 6 ký tự";
    return;
  }
  if (newPassword.value !== confirmPassword.value) {
    error.value = "Mật khẩu nhập lại không khớp";
    return;
  }

  loading.value = true;
  try {
    await http.post("/api/auth/reset-password-otp", {
      email: email.value,
      otp: otp.value,
      newPassword: newPassword.value,
    });

    success.value = "Đổi mật khẩu thành công. Đang chuyển về đăng nhập...";
    setTimeout(() => router.push("/login"), 800);
  } catch (e) {
    error.value =
      e?.response?.data?.message ||
      e?.response?.data ||
      e?.message ||
      "Đổi mật khẩu thất bại";
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
  margin: 0 0 6px 0;
  text-align: center;
}
.sub {
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
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  outline: none;
}

.password-wrap {
  position: relative;
  display: flex;
  align-items: center;
}
.password-wrap input {
  padding-right: 44px;
}

.eye-btn {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  border: 0;
  background: transparent;
  cursor: pointer;
  padding: 6px;
  border-radius: 8px;
  color: #475569;
}
.eye-btn:hover {
  background: rgba(41, 84, 184, 0.08);
}
.eye-icon {
  width: 20px;
  height: 20px;
  fill: currentColor;
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
