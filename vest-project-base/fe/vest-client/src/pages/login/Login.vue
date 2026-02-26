<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-title">Đăng nhập</div>

      <form @submit.prevent="onSubmit" class="login-form">
        <div class="form-group">
          <label for="email">Email</label>
          <input
              v-model="form.email"
              id="email"
              type="email"
              class="form-control"
              placeholder="Nhập email"
              required
          />
        </div>

        <div class="form-group">
          <label for="password">Mật khẩu</label>
          <input
              v-model="form.password"
              id="password"
              type="password"
              class="form-control"
              placeholder="Nhập mật khẩu"
              required
          />
        </div>

        <div class="form-row">
          <label class="remember">
            <input v-model="form.remember" type="checkbox" />
            Ghi nhớ đăng nhập
          </label>

          <a class="forgot" href="#" @click.prevent>Quên mật khẩu?</a>
        </div>

        <button class="btn-submit" type="submit">Đăng nhập</button>

        <div class="divider">
          <span>hoặc</span>
        </div>

        <button class="btn-google" type="button" @click="loginWithGoogle">
          <i class="fab fa-google"></i>
          Đăng nhập với Google
        </button>

        <div class="register-hint">
          Chưa có tài khoản?
          <!-- project hiện chưa có route /register nên tránh 404 -->
          <RouterLink to="/" class="register-link">Đăng ký ngay</RouterLink>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue';

const form = reactive({
  email: '',
  password: '',
  remember: false,
});

function onSubmit() {
  // TODO: gọi API đăng nhập
  console.log('Login submit:', { ...form });
  alert('Chức năng đăng nhập đang được phát triển.');
}

function loginWithGoogle() {
  // TODO: tích hợp OAuth Google
  alert('Đăng nhập Google đang được phát triển.');
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
  content: '';
  flex: 1;
  height: 1px;
  background: #eee;
}

.btn-google {
  height: 44px;
  border: 1px solid #ddd;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 600;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.register-hint {
  margin-top: 6px;
  text-align: center;
  font-size: 14px;
  color: #444;
}

.register-link {
  margin-left: 6px;
  font-weight: 700;
  text-decoration: none;
  color: #111;
}
</style>