<template>
  <div style="width:100%; display:flex; align-items:center; justify-content:space-between;">
    <div style="font-weight:600;">{{ title }}</div>
    <div style="display:flex; align-items:center; gap:8px;">
      <button class="btn">Settings</button>
      <RouterLink class="btn btn-primary" :to="{ name: 'login' }">Login</RouterLink>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => {
  const map = {
    dashboard: 'Dashboard',
    products: 'Sản phẩm',
    'product-detail': 'Chi tiết sản phẩm',
    orders: 'Hóa đơn',
    customers: 'Khách hàng',
    staff: 'Nhân viên',
    vouchers: 'Phiếu giảm giá',
    payments: 'Thanh toán'
  }
  return map[route.name] || 'Vest Shop'
})
</script>
