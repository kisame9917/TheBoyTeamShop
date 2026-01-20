<template>
  <teleport to="body">
    <div class="toast-wrap" aria-live="polite" aria-atomic="true">
      <div
        v-for="t in state.items"
        :key="t.id"
        class="toast-item"
        :class="t.type"
        role="status"
        @click="remove(t.id)"
      >
        <div class="toast-head">
          <div class="toast-title">{{ t.title || titleByType(t.type) }}</div>
          <button class="toast-close" type="button" aria-label="Close" @click.stop="remove(t.id)">
            ✕
          </button>
        </div>
        <div class="toast-msg">{{ t.message }}</div>
      </div>
    </div>
  </teleport>
</template>

<script setup>
import { useToast } from "@/composables/useToast"

const { state, remove } = useToast()

function titleByType(type) {
  if (type === "success") return "Thành công"
  if (type === "error") return "Lỗi"
  if (type === "warning") return "Cảnh báo"
  return "Thông báo"
}
</script>

<style scoped>
.toast-wrap{
  position: fixed;
  top: 16px;
  right: 16px;
  z-index: 99999;
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: min(360px, calc(100vw - 32px));
}

.toast-item{
  background: #fff;
  border: 1px solid #e5e7eb;
  border-left-width: 6px;
  border-radius: 12px;
  padding: 12px 12px 10px;
  box-shadow: 0 10px 30px rgba(0,0,0,.12);
  cursor: pointer;
  animation: toastIn .18s ease-out;
}

.toast-head{
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 6px;
}

.toast-title{
  font-weight: 800;
  font-size: 13px;
  color: #111827;
}

.toast-close{
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 14px;
  opacity: .7;
}
.toast-close:hover{ opacity: 1; }

.toast-msg{
  font-size: 13px;
  color: #374151;
  line-height: 1.4;
  word-break: break-word;
}

/* type colors */
.toast-item.success{ border-left-color: #22c55e; }
.toast-item.error{ border-left-color: #ef4444; }
.toast-item.warning{ border-left-color: #f59e0b; }
.toast-item.info{ border-left-color: #3b82f6; }

@keyframes toastIn{
  from{ transform: translateY(-6px); opacity: 0; }
  to{ transform: translateY(0); opacity: 1; }
}
</style>
