<template>
  <div class="card">
    <h2 style="margin:0 0 8px;">Chi tiết sản phẩm</h2>
    <p style="margin:0; color:#6b7280">ID: <b>{{ id }}</b></p>
    <p style="margin:8px 0 0; color:#6b7280">
      (Base) Màn này để bạn build: thông tin sản phẩm + danh sách SKU (san_pham_chi_tiet).
    </p>

    <div style="margin-top:12px">
      <button class="btn" @click="goBack">Quay lại</button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({ id: String })
const router = useRouter()

function goBack() {
  router.back()
}
</script>
