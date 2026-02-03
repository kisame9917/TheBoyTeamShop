<template>
  <Suspense>
    <template #default>
      <RouterView />
    </template>

    <template #fallback>
      <div style="padding:24px">Loading...</div>
    </template>
  </Suspense>

  <ToastContainer />
</template>

<script setup>
import ToastContainer from "@/components/ToastContainer.vue"
</script>
