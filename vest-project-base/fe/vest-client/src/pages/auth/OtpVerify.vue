<template>
  <div class="fp-page">
    <div class="fp-card">
      <div class="fp-title">Xác minh OTP</div>
      <div class="fp-subtitle">
        OTP đã được gửi (nếu email tồn tại). Kiểm tra hộp thư của bạn.
      </div>

      <form class="fp-form" @submit.prevent="verifyOtp">
        <div v-if="error" class="alert">{{ error }}</div>
        <div v-if="success" class="success">{{ success }}</div>

        <div class="hint">
          Email: <b>{{ email }}</b>
        </div>

        <div class="form-group">
          <label for="otp">Mã OTP</label>
          <input
            v-model.trim="otp"
            id="otp"
            type="text"
            class="form-control"
            inputmode="numeric"
            maxlength="6"
            placeholder="Nhập 6 chữ số"
            required
          />
        </div>

        <button class="btn-primary" type="submit" :disabled="loading || otp.length < 6">
          {{ loading ? "Đang xác minh..." : "Xác minh OTP" }}
        </button>

        <div class="row-actions">
          <button type="button" class="btn-link" @click="backToEmail" :disabled="loading">
            Sửa email
          </button>

          <button type="button" class="btn-link" @click="resendOtp" :disabled="loading || cooldown > 0">
            {{ cooldown > 0 ? `Gửi lại sau ${cooldown}s` : "Gửi lại OTP" }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

const router = useRouter();
const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

const email = ref("");
const otp = ref("");
const loading = ref(false);
const error = ref("");
const success = ref("");

const cooldown = ref(0);
let timer = null;

function startCooldown(sec = 60) {
  cooldown.value = sec;
  if (timer) clearInterval(timer);
  timer = setInterval(() => {
    cooldown.value -= 1;
    if (cooldown.value <= 0) {
      clearInterval(timer);
      timer = null;
      cooldown.value = 0;
    }
  }, 1000);
}

onBeforeUnmount(() => {
  if (timer) clearInterval(timer);
});

onMounted(() => {
  email.value = sessionStorage.getItem("FP_EMAIL") || "";
  if (!email.value) router.replace("/forgot-password");
});

function backToEmail() {
  sessionStorage.removeItem("FP_EMAIL");
  sessionStorage.removeItem("FP_OTP");
  router.push("/forgot-password");
}

async function resendOtp() {
  if (cooldown.value > 0) return;

  error.value = "";
  success.value = "";
  loading.value = true;

  try {
    const res = await axios.post(`${BASE_URL}/api/client/auth/forgot-password-otp`, {
      email: email.value,
    });
    success.value = res?.data || "OK";
    startCooldown(60);
  } catch (e) {
    error.value =
      e?.response?.data?.message ||
      e?.response?.data?.error ||
      e?.response?.data ||
      e?.message ||
      "Gửi lại OTP thất bại.";
  } finally {
    loading.value = false;
  }
}

async function verifyOtp() {
  error.value = "";
  success.value = "";
  loading.value = true;

  try {
    await axios.post(`${BASE_URL}/api/client/auth/verify-otp`, {
      email: email.value,
      otp: otp.value,
      newPassword: "",
    });

    sessionStorage.setItem("FP_OTP", otp.value);
    success.value = "OTP hợp lệ";
    router.push("/reset-password-otp");
  } catch (e) {
    error.value =
      e?.response?.data?.message ||
      e?.response?.data?.error ||
      e?.response?.data ||
      e?.message ||
      "OTP không hợp lệ.";
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

.hint {
  font-size: 13px;
  color: #555;
  background: #fafafa;
  border: 1px solid #eee;
  padding: 10px 12px;
  border-radius: 10px;
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

.btn-link:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>