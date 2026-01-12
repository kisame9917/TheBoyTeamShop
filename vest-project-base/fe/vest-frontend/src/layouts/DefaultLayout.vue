<template>
  <div class="layout">
    <AppSidebar />
    <div>
      <AppTopbar />
      <main class="container">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup>
import AppSidebar from '../components/AppSidebar.vue'
import AppTopbar from '../components/AppTopbar.vue'
</script>
