<template>
  <div class="card" style="max-width:420px; margin: 64px auto;">
    <h2 style="margin:0 0 12px;">Đăng nhập</h2>
    <form @submit.prevent="onSubmit" style="display:flex; flex-direction:column; gap:12px;">
      <div>
        <label style="display:block; margin-bottom:6px;">Tài khoản</label>
        <input class="input" v-model="username" placeholder="admin" />
      </div>
      <div>
        <label style="display:block; margin-bottom:6px;">Mật khẩu</label>
        <input class="input" type="password" v-model="password" placeholder="••••••" />
      </div>
      <button class="btn" style="justify-content:center;">Đăng nhập</button>
      <p v-if="error" style="margin:0; color:#b91c1c;">{{ error }}</p>
      <p style="margin:0; color:#6b7280; font-size:13px;">
        * Base only: tạm thời accept bất kỳ username/password nào.
      </p>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const username = ref('admin')
const password = ref('admin')
const error = ref('')

async function onSubmit() {
  try {
    error.value = ''
    await auth.login({ username: username.value, password: password.value })
    router.push('/')
  } catch (e) {
    error.value = 'Đăng nhập thất bại'
  }
}
</script>
