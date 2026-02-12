<template>
  <div class="login-wrap">
    <div class="card">
      <!-- ✅ Logo -->
      <div class="logo-wrap">
        <img :src="logoUrl" alt="TheBoyTeam Logo" class="logo" />
      </div>

      <h2 class="title">Đăng nhập</h2>

      <form @submit.prevent="onSubmit">
        <div class="field">
          <label>Tài khoản</label>
          <input v-model.trim="taiKhoan" autocomplete="username" placeholder="vd: nv1" />
        </div>

        <div class="field">
          <label>Mật khẩu</label>

          <!-- ✅ Password input + eye button -->
          <div class="password-wrap">
            <input
              v-model="matKhau"
              :type="showPassword ? 'text' : 'password'"
              autocomplete="current-password"
              placeholder="vd: 123456"
            />

            <button
              type="button"
              class="eye-btn"
              @click="showPassword = !showPassword"
              :aria-label="showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
            >
              <!-- eye / eye-slash (inline svg) -->
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

        <button class="btn btn-primary" :disabled="loading">
          {{ loading ? 'Đang đăng nhập...' : 'Đăng nhập' }}
        </button>

        <button type="button" class="btn-link" @click="router.push('/forgot-password')">
          Quên mật khẩu?
        </button>

        <p v-if="error" class="error">{{ error }}</p>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";

import logo from "../../images/logo.jpg";

const router = useRouter();
const auth = useAuthStore();

const taiKhoan = ref("");
const matKhau = ref("");
const loading = ref(false);
const error = ref("");
const showPassword = ref(false);

const logoUrl = computed(() => logo);

async function onSubmit() {
  error.value = "";
  loading.value = true;

  try {
    const data = await auth.login({
      taiKhoan: taiKhoan.value,
      matKhau: matKhau.value,
    });

    await router.replace(data.role === "ADMIN" ? { name: "dashboard" } : { name: "dashboard" });
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || "Đăng nhập thất bại";
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

/* ✅ logo */
.logo-wrap{
  display: grid;
  place-items: center;
  margin-bottom: 10px;
}
.logo{
  width: 86px;
  height: 86px;
  object-fit: cover;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
}

.title {
  margin: 0 0 16px 0;
  text-align: center;
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

/* ✅ password with eye */
.password-wrap{
  position: relative;
  display: flex;
  align-items: center;
}
.password-wrap input{
  padding-right: 42px; /* chừa chỗ cho icon */
}
.eye-btn{
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
.eye-btn:hover{
  background: rgba(41,84,184,0.08);
}
.eye-icon{
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
  margin-top: 12px;
}

.error {
  margin-top: 12px;
  color: #dc2626;
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
</style>
