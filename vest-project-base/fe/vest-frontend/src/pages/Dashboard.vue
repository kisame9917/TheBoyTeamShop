<template>
  <div class="card">
    <h2 style="margin:0 0 8px;">Project base FE (SPA)</h2>
    <p style="margin:0; color:#6b7280">
      Đây là khung router + layout. Các màn hình hiện tại là placeholder để bạn dựng UI dần.
    </p>
  </div>

  <div style="height:12px"></div>

  <div class="card">
    <h3 style="margin:0 0 8px;">Quick links</h3>
    <div style="display:flex; gap:8px; flex-wrap:wrap">
      <RouterLink class="btn" to="/products">Sản phẩm</RouterLink>
      <RouterLink class="btn" to="/orders">Hóa đơn</RouterLink>
      <RouterLink class="btn" to="/vouchers">Phiếu giảm giá</RouterLink>
    </div>
  </div>
</template>

<script setup>
</script>
