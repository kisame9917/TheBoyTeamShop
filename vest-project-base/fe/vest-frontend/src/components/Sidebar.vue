<template>
  <div>
    <div style="display:flex; flex-direction:column; gap:4px; margin-bottom:12px;">
      <div style="font-weight:700; font-size:18px;">Vest Shop</div>
      <div class="small">Admin base (SPA)</div>
    </div>

    <nav style="display:flex; flex-direction:column; gap:6px;">
      <RouterLink class="nav-item" :class="isActive('dashboard')" :to="{ name: 'dashboard' }">Dashboard</RouterLink>
      <RouterLink class="nav-item" :class="isActive('products')" :to="{ name: 'products' }">Sản phẩm</RouterLink>
      <RouterLink class="nav-item" :class="isActive('orders')" :to="{ name: 'orders' }">Hóa đơn</RouterLink>
      <RouterLink class="nav-item" :class="isActive('customers')" :to="{ name: 'customers' }">Khách hàng</RouterLink>
      <RouterLink class="nav-item" :class="isActive('staff')" :to="{ name: 'staff' }">Nhân viên</RouterLink>
      <RouterLink class="nav-item" :class="isActive('vouchers')" :to="{ name: 'vouchers' }">Phiếu giảm giá</RouterLink>
      <RouterLink class="nav-item" :class="isActive('payments')" :to="{ name: 'payments' }">Thanh toán</RouterLink>
    </nav>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()
const isActive = (name) => (route.name === name ? 'active' : '')
</script>
