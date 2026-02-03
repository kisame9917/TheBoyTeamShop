<template>
  <div class="login-wrap">
    <div class="card">
      <h2 class="title">TBT VestShop</h2>

      <form @submit.prevent="onSubmit">
        <div class="field">
          <label>Tài khoản</label>
          <input v-model.trim="taiKhoan" autocomplete="username" placeholder="vd: nv1" />
        </div>

        <div class="field">
          <label>Mật khẩu</label>
          <input v-model="matKhau" type="password" autocomplete="current-password" placeholder="vd: 123456" />
        </div>

        <button class="btn btn-primary" :disabled="loading">
          {{ loading ? 'Đang đăng nhập...' : 'Đăng nhập' }}
        </button>

        <p v-if="error" class="error">{{ error }}</p>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const taiKhoan = ref('')
const matKhau = ref('')
const loading = ref(false)
const error = ref('')

async function onSubmit() {
  error.value = ''
  loading.value = true

  try {
    const data = await auth.login({
      taiKhoan: taiKhoan.value,
      matKhau: matKhau.value,
    })
    console.log('LOGIN RESPONSE:', data) // { token, role }

    // ✅ redirect đúng theo role
await router.replace(data.role === 'ADMIN' ? { name: 'dashboard' } : { name: 'sales' })
  } catch (e) {
    console.error(e)
    // ưu tiên message từ backend nếu có
    error.value =
      e?.response?.data?.message ||
      e?.message ||
      'Đăng nhập thất bại'
  } finally {
    loading.value = false
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
  margin: 0 0 16px 0;
  text-align: center;
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
  border: 1;
  cursor: pointer;
  
}
.error {
  margin-top: 12px;
  color: #dc2626;
}
</style>
