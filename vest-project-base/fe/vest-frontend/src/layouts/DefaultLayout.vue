<template>
  <div class="layout">
    <AppSidebar />
    <div>
      <AppTopbar />
      <main style="padding: 24px; min-width: 0;">
        <RouterView />
      </main>
    </div>

    <!-- STAFF bắt buộc xác nhận ca (không cho mở tự do) -->

  </div>
</template>

<script setup>
import AppSidebar from '../components/AppSidebar.vue'
import AppTopbar from '../components/AppTopbar.vue'

import { RouterView } from 'vue-router'
import { onMounted, watch } from 'vue'
import { useShiftStore } from '@/stores/shift'
import { useAuthStore } from '@/stores/auth'

const shift = useShiftStore()
const auth = useAuthStore()
onMounted(() => {
  // bootstrap trạng thái ca khi vào app
  // (chỉ chạy khi đã có token để tránh case layout mount sớm)
  shift.bootstrap(true)
})

// Khi user vừa login / đổi tài khoản, ensure modal xuất hiện đúng
watch(
  () => [auth.isAuthenticated, auth.token, auth.role],
  ([isAuth]) => {
    if (isAuth) shift.bootstrap(true)
  },
  { immediate: true }
)
</script>
