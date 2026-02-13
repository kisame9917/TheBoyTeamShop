<template>
  <Suspense>
    <template #default>
      <RouterView />
    </template>

    <template #fallback>
      <div style="padding:24px">Loading...</div>
    </template>
  </Suspense>

  <!-- ✅ Gate modal đặt ở ROOT để đảm bảo luôn render -->
  <ShiftGateModal />

  <ToastContainer />
</template>

<script setup>
import { onMounted, watch } from "vue";
import ToastContainer from "@/components/ToastContainer.vue";
import ShiftGateModal from "@/components/ShiftGateModal.vue";

import { useAuthStore } from "@/stores/auth";
import { useShiftStore } from "@/stores/shift";

const auth = useAuthStore();
const shift = useShiftStore();

// ✅ luôn bootstrap khi app mount
onMounted(() => {
  // nếu đã có token trong localStorage thì sẽ bật gate luôn cho STAFF
  shift.bootstrap(true);
});

</script>
