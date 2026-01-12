<template>
  <header class="topbar">
    <div style="font-weight:600;">{{ title }}</div>
    <div style="font-size: 14px; color:#6b7280;">{{ subtitle }}</div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => {
  switch (route.name) {
    case 'dashboard': return 'Dashboard'
    case 'products': return 'Sản phẩm'
    case 'product-detail': return 'Chi tiết sản phẩm'
    case 'orders': return 'Hóa đơn'
    case 'customers': return 'Khách hàng'
    case 'staff': return 'Nhân viên'
    case 'vouchers': return 'Phiếu giảm giá'
    case 'payments': return 'Thanh toán'
    default: return 'Vest Shop'
  }
})

const subtitle = computed(() => new Date().toLocaleString())
</script>
