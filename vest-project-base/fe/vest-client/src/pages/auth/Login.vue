<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-title">Đăng nhập</div>

      <form @submit.prevent="onSubmit" class="login-form">
        <div v-if="error" class="alert">{{ error }}</div>

        <div class="form-group">
          <label for="taiKhoan">Tài khoản</label>
          <input
            v-model.trim="form.taiKhoan"
            id="taiKhoan"
            type="text"
            class="form-control"
            placeholder="Nhập tài khoản"
            required
            autocomplete="username"
          />
        </div>

        <div class="form-group">
          <label for="matKhau">Mật khẩu</label>

          <div class="password-wrap">
            <input
              v-model="form.matKhau"
              id="matKhau"
              :type="showPassword ? 'text' : 'password'"
              class="form-control"
              placeholder="Nhập mật khẩu"
              required
              autocomplete="current-password"
            />

            <button
              type="button"
              class="toggle-eye"
              :aria-label="showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
              @click="showPassword = !showPassword"
            >
              <!-- eye -->
              <svg v-if="!showPassword" viewBox="0 0 24 24" class="eye-icon" aria-hidden="true">
                <path
                  d="M12 5c5.5 0 9.6 4.1 11 7-1.4 2.9-5.5 7-11 7S2.4 15.9 1 12c1.4-2.9 5.5-7 11-7Zm0 2C7.7 7 4.4 10 3.2 12 4.4 14 7.7 17 12 17s7.6-3 8.8-5C19.6 10 16.3 7 12 7Zm0 2.5A2.5 2.5 0 1 1 12 14a2.5 2.5 0 0 1 0-5Z"
                />
              </svg>

              <!-- eye off -->
              <svg v-else viewBox="0 0 24 24" class="eye-icon" aria-hidden="true">
                <path
                  d="M2 4.3 3.3 3 21 20.7 19.7 22l-3-3c-1.4.6-3 .9-4.7.9-5.5 0-9.6-4.1-11-7 1-2.1 3.3-4.8 6.7-6.2L2 4.3Zm6.1 4.8C6 10 4.4 11.6 3.2 13c1.2 1.4 4.5 4.4 8.8 4.4 1.1 0 2.2-.2 3.2-.5l-1.7-1.7c-.5.2-1 .3-1.5.3A3.5 3.5 0 0 1 8.1 9.1Zm9.6 7.2-2-2c.2-.4.3-.9.3-1.3A3.5 3.5 0 0 0 11 9.5c-.5 0-1 .1-1.4.3L8 8.3c1.2-.6 2.6-1 4-1 5.5 0 9.6 4.1 11 7-.7 1.4-2.2 3.4-4.3 4.9Z"
                />
              </svg>
            </button>
          </div>
        </div>

        <div class="form-row">
          <label class="remember">
            <input v-model="form.remember" type="checkbox" />
            Ghi nhớ đăng nhập
          </label>

          <a class="forgot" href="#" @click.prevent="router.push('/forgot-password')">Quên mật khẩu?</a>
        </div>

        <button class="btn-submit" type="submit" :disabled="loading">
          {{ loading ? "Đang đăng nhập..." : "Đăng nhập" }}
        </button>

        <div class="divider"><span>hoặc</span></div>

        <!-- ✅ bật nút Google để test -->
        <button class="btn-google" type="button" @click="loginWithGoogle">
          <i class="fab fa-google"></i>
          Đăng nhập với Google
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import axios from "axios";

const router = useRouter();
const route = useRoute();

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

const loading = ref(false);
const error = ref("");
const showPassword = ref(false);

const form = reactive({
  taiKhoan: "",
  matKhau: "",
  remember: false,
});

function clearAuthStorage() {
  localStorage.removeItem("USER_ACCESS_TOKEN");
  sessionStorage.removeItem("USER_ACCESS_TOKEN");
  localStorage.removeItem("USER_NAME");
  sessionStorage.removeItem("USER_NAME");
}

function saveAuth({ token, tenKhachHang }, remember) {
  const store = remember ? localStorage : sessionStorage;
  clearAuthStorage();
  store.setItem("USER_ACCESS_TOKEN", token);
  store.setItem("USER_NAME", tenKhachHang || "");
}

function loginWithGoogle() {
  // Backend endpoint bạn đã thêm trong ClientAuthController:
  // GET /api/client/auth/google -> redirect /oauth2/authorization/google
  window.location.href = `${BASE_URL}/api/client/auth/google`;
}

async function onSubmit() {
  error.value = "";
  loading.value = true;

  try {
    const res = await axios.post(`${BASE_URL}/api/client/auth/login`, {
      taiKhoan: form.taiKhoan,
      matKhau: form.matKhau,
    });

    const data = res.data || {};
    const token = data.token;
    if (!token) throw new Error("Không nhận được token từ server.");

    // lưu kiểu cũ của bạn
    saveAuth({ token, tenKhachHang: data.tenKhachHang }, form.remember);

    // ✅ lưu thêm kiểu vest_* cho chat widget
    localStorage.setItem("vest_user", JSON.stringify(data)); // nếu data có id/role...
    localStorage.setItem("vest_token", token);
    if (data.role) localStorage.setItem("vest_role", data.role);

    // ✅ báo widget cập nhật ngay
    window.dispatchEvent(new Event("auth-changed"));

    const redirect = route.query.redirect || "/";
    await router.replace(redirect);
  } catch (e) {
    error.value =
      e?.response?.data?.message || e?.response?.data?.error || e?.message || "Đăng nhập thất bại.";
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-page {
  min-height: calc(100vh - 140px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 12px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  border: 1px solid #eee;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.06);
  background: #fff;
}

.login-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 16px;
  text-align: center;
}

.login-form {
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

.form-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  font-size: 14px;
}

.remember {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  user-select: none;
}

.forgot {
  color: #111;
  text-decoration: none;
  opacity: 0.8;
}

.btn-submit {
  height: 44px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 700;
  background: #0c63e7;
  color: #fff;
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 4px 0;
  color: #777;
  font-size: 13px;
}

.divider::before,
.divider::after {
  content: "";
  flex: 1;
  height: 1px;
  background: #eee;
}

.btn-google {
  height: 44px;
  border: 1px solid #ddd;
  border-radius: 10px;
  cursor: pointer; /* ✅ bật click */
  font-weight: 600;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

/* Eye toggle */
.password-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.password-wrap .form-control {
  padding-right: 44px;
}

.toggle-eye {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  border: none;
  background: transparent;
  padding: 6px;
  cursor: pointer;
  opacity: 0.75;
}

.toggle-eye:hover {
  opacity: 1;
}

.eye-icon {
  width: 20px;
  height: 20px;
  fill: currentColor;
}
</style>