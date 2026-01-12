<template>
  <aside class="sidebar">
    <div style="font-weight:700; font-size:18px; margin-bottom: 12px;">
      Vest Shop
    </div>

    <nav style="display:flex; flex-direction:column; gap: 6px;">
      <RouterLink to="/">Dashboard</RouterLink>
      <RouterLink to="/products">Sản phẩm</RouterLink>
      <RouterLink to="/orders">Hóa đơn</RouterLink>
      <RouterLink to="/customers">Khách hàng</RouterLink>
      <RouterLink to="/staff">Nhân viên</RouterLink>
      <RouterLink to="/vouchers">Phiếu giảm giá</RouterLink>
      <RouterLink to="/payments">Thanh toán</RouterLink>
    </nav>

    <div style="margin-top: 16px; padding-top: 16px; border-top: 1px solid rgba(255,255,255,0.1)">
      <button class="btn" @click="logout" style="width:100%; justify-content:center;">
        Đăng xuất
      </button>
    </div>
  </aside>
</template>

<script setup>
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

const router = useRouter()
const auth = useAuthStore()

function logout() {
  auth.logout()
  router.push('/login')
}
</script>
