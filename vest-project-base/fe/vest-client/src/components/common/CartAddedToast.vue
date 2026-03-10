<template>
  <transition name="cart-toast-fade">
    <div v-if="open" class="cart-toast" role="status" aria-live="polite">
      <div class="cart-toast__icon">
        <i class="bi bi-bag-check"></i>
      </div>

      <img v-if="image" :src="image" alt="Sản phẩm" class="cart-toast__image" @error="onImgError" />

      <div class="cart-toast__content">
        <div class="cart-toast__title">Đã thêm vào giỏ hàng</div>
        <div class="cart-toast__text">
          <strong>{{ itemName || 'Sản phẩm' }}</strong>
          <span v-if="qty > 0"> x{{ qty }}</span>
        </div>
      </div>

      <button type="button" class="cart-toast__action" @click="$emit('view-cart')">
        Xem giỏ hàng
      </button>
    </div>
  </transition>
</template>

<script setup>
defineProps({
  open: { type: Boolean, default: false },
  itemName: { type: String, default: '' },
  image: { type: String, default: '' },
  qty: { type: Number, default: 1 },
});

defineEmits(['view-cart']);

function onImgError(event) {
  event.target.style.display = 'none';
}
</script>

<style scoped>
.cart-toast {
  position: fixed;
  top: 88px;
  right: 24px;
  z-index: 1400;
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 320px;
  max-width: min(440px, calc(100vw - 24px));
  padding: 14px 16px;
  background: linear-gradient(135deg, #000f51 0%, #123d9b 100%);
  color: #fff;
  border-radius: 16px;
  box-shadow: 0 18px 40px rgba(0, 15, 81, 0.28);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.cart-toast__icon {
  width: 38px;
  height: 38px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex: 0 0 auto;
}

.cart-toast__image {
  width: 44px;
  height: 44px;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(255, 255, 255, 0.08);
}

.cart-toast__content {
  min-width: 0;
  flex: 1;
}

.cart-toast__title {
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 2px;
}

.cart-toast__text {
  font-size: 13px;
  opacity: 0.94;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cart-toast__action {
  border: 0;
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
  border-radius: 12px;
  padding: 10px 12px;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}

.cart-toast__action:hover {
  background: rgba(255, 255, 255, 0.22);
}

.cart-toast-fade-enter-active,
.cart-toast-fade-leave-active {
  transition: all 0.22s ease;
}

.cart-toast-fade-enter-from,
.cart-toast-fade-leave-to {
  opacity: 0;
  transform: translate3d(0, -10px, 0);
}

@media (max-width: 767.98px) {
  .cart-toast {
    top: 74px;
    right: 12px;
    left: 12px;
    min-width: 0;
    max-width: none;
    padding: 12px 14px;
  }

  .cart-toast__action {
    display: none;
  }
}
</style>
