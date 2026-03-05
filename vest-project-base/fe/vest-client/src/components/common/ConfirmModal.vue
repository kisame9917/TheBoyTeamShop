<template>
  <div v-if="open" class="cm-overlay" @click.self="emitCancel">
    <div class="cm-modal" role="dialog" aria-modal="true" @click.stop>
      <div class="cm-header">
        <div class="cm-title">{{ title }}</div>
        <button class="cm-close" type="button" aria-label="Đóng" @click="emitCancel">
          <i class="bi bi-x"></i>
        </button>
      </div>

      <div class="cm-body">
        <div class="cm-message">{{ message }}</div>
      </div>

      <div class="cm-footer">
        <button class="btn btn-light px-4 fw-semibold" type="button" @click="emitCancel">
          {{ cancelText }}
        </button>
        <button class="btn btn-dark px-4 fw-semibold" type="button" @click="$emit('confirm')">
          {{ confirmText }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  open: { type: Boolean, default: false },
  title: { type: String, default: "Xác nhận" },
  message: { type: String, default: "Bạn có chắc chắn không?" },
  confirmText: { type: String, default: "Đồng ý" },
  cancelText: { type: String, default: "Hủy" },
});

const emit = defineEmits(["confirm", "cancel"]);

function emitCancel() {
  emit("cancel");
}
</script>

<style scoped>
.cm-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 16px;
}

.cm-modal {
  width: 440px;
  max-width: 100%;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.22);
  border: 1px solid rgba(0, 0, 0, 0.08);
}

.cm-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  background: #000f51;
  color: #fff;
}

.cm-title {
  font-weight: 900;
  letter-spacing: 0.2px;
}

.cm-close {
  border: none;
  background: transparent;
  color: #fff;
  font-size: 22px;
  line-height: 1;
  padding: 0;
}

.cm-body {
  padding: 16px 14px;
}

.cm-message {
  color: #111;
  font-size: 0.98rem;
  line-height: 1.4;
}

.cm-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding: 12px 14px 14px;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
}
</style>
