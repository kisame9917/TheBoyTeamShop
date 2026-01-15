<template>
  <div class="modal-backdrop" @click.self="$emit('close')">
    <div class="modal card">
      <div class="page-header">
        <h3 class="page-title">
          {{ data ? 'Cập nhật phiếu giảm giá' : 'Tạo phiếu giảm giá' }}
        </h3>
        <button class="btn" @click="$emit('close')">Đóng</button>
      </div>

      <p style="color:#6b7280">
        VoucherForm đã được load thành công ✅
      </p>

      <div style="display:flex; justify-content:flex-end; gap:10px; margin-top:12px;">
        <button class="btn" @click="$emit('close')">Huỷ</button>
        <button class="btn btn-primary" @click="$emit('saved')">Lưu</button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  data: { type: Object, default: null }
})
defineEmits(['close', 'saved'])
</script>

<style scoped>
.modal-backdrop{
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.35);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 18px;
  z-index: 1000;
}
.modal{
  width: min(900px, 100%);
}
</style>
