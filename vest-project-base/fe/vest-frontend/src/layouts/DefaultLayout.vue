<template>
  <div class="layout">
    <AppSidebar />
    <div>
      <AppTopbar />
      <main style="padding: 24px; min-width: 0;">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup>
import AppSidebar from '../components/AppSidebar.vue'
import AppTopbar from '../components/AppTopbar.vue'
</script>
