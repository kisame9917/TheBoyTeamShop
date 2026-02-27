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
          <input
            v-model="form.matKhau"
            id="matKhau"
            type="password"
            class="form-control"
            placeholder="Nhập mật khẩu"
            required
            autocomplete="current-password"
          />
        </div>

        <div class="form-row">
          <label class="remember">
            <input v-model="form.remember" type="checkbox" />
            Ghi nhớ đăng nhập
          </label>

          <a class="forgot" href="#" @click.prevent>Quên mật khẩu?</a>
        </div>

        <button class="btn-submit" type="submit" :disabled="loading">
          {{ loading ? "Đang đăng nhập..." : "Đăng nhập" }}
        </button>

        <div class="divider"><span>hoặc</span></div>

        <button class="btn-google" type="button" disabled>
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

const form = reactive({
  taiKhoan: "",
  matKhau: "",
  remember: false,
});

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

    const store = form.remember ? localStorage : sessionStorage;

    // clear cũ
    localStorage.removeItem("USER_ACCESS_TOKEN");
    sessionStorage.removeItem("USER_ACCESS_TOKEN");
    localStorage.removeItem("USER_NAME");
    sessionStorage.removeItem("USER_NAME");

    // save
    store.setItem("USER_ACCESS_TOKEN", token);
    store.setItem("USER_NAME", data.tenKhachHang || "");

    // redirect nếu có
    const redirect = route.query.redirect || "/";
    await router.replace(redirect);
  } catch (e) {
    error.value =
      e?.response?.data?.message ||
      e?.response?.data?.error ||
      e?.message ||
      "Đăng nhập thất bại.";
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
  cursor: not-allowed;
  font-weight: 600;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}
</style>