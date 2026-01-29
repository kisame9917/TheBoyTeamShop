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

const subtitle = computed(() => new Date().toLocaleString())
</script>
