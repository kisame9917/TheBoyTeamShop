<template>
  <div class="mini-cart" role="dialog" aria-label="Giỏ hàng" @click.stop>
    <div class="mini-cart__header">
      <div class="mini-cart__title">GIỎ HÀNG</div>
      <button class="mini-cart__close" type="button" aria-label="Đóng" @click="$emit('close')">
        <i class="bi bi-x"></i>
      </button>
    </div>

    <div class="mini-cart__body">
      <div v-if="items.length === 0" class="mini-cart__empty">
        Giỏ hàng đang trống.
      </div>

      <div v-else class="mini-cart__list">
        <div v-for="it in items" :key="it.key" class="mini-cart__item">
          <img :src="it.image" class="mini-cart__img" alt="Sản phẩm" @error="onImgError" />

          <div class="mini-cart__info">
            <div class="mini-cart__name" :title="it.name">{{ it.name }}</div>

            <div class="mini-cart__meta">
              <span v-if="it.color">Màu: <span class="dot" :style="{ backgroundColor: it.color }"></span></span>
              <span v-if="it.size">Kích cỡ: {{ it.size }}</span>
            </div>

            <div class="mini-cart__row">
              <div class="mini-cart__qty">
                <button
                    class="qty-btn"
                    type="button"
                    aria-label="Giảm"
                    :disabled="Number(it.qty) <= 1"
                    @click="dec(it)"
                >
                  -
                </button>
                <input class="qty-input" type="text" :value="it.qty" readonly />
                <button class="qty-btn" type="button" aria-label="Tăng" @click="inc(it)">+</button>
              </div>

              <div class="mini-cart__price">{{ formatMoney(it.price) }} đ</div>
            </div>
          </div>

          <button class="mini-cart__remove" type="button" aria-label="Xóa" @click="askRemove(it)">
            <i class="bi bi-x"></i>
          </button>
        </div>
      </div>
    </div>

    <div class="mini-cart__footer" v-if="items.length > 0">
      <div class="mini-cart__summary">
        <div class="mini-cart__sumrow">
          <span>Tổng sản phẩm:</span>
          <strong>{{ totalQty }}</strong>
        </div>
        <div class="mini-cart__sumrow">
          <span>Tạm tính:</span>
          <strong>{{ formatMoney(subtotal) }} đ</strong>
        </div>
      </div>

      <button class="btn btn-dark w-100 fw-semibold py-2 mb-2" type="button" @click="$emit('checkout')">
        ĐẶT HÀNG
      </button>
      <button class="btn btn-outline-dark w-100 fw-semibold py-2" type="button" @click="$emit('view-cart')">
        XEM GIỎ HÀNG
      </button>
    </div>

    <ConfirmModal
        :open="confirmOpen"
        title="Xác nhận"
        message="Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng không?"
        confirmText="Đồng ý"
        cancelText="Hủy"
        @cancel="closeConfirm"
        @confirm="confirmRemove"
    />
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useCart } from "../../composables/useCart";
import ConfirmModal from "../common/ConfirmModal.vue";

defineEmits(["close", "view-cart", "checkout"]);

const { items, totalQty, subtotal, updateQty, removeItem } = useCart();

const confirmOpen = ref(false);
const pendingKey = ref(null);

function askRemove(it) {
  pendingKey.value = it?.key || null;
  confirmOpen.value = true;
}

function closeConfirm() {
  confirmOpen.value = false;
  pendingKey.value = null;
}

function confirmRemove() {
  if (pendingKey.value) removeItem(pendingKey.value);
  closeConfirm();
}

function inc(it) {
  updateQty(it.key, Number(it.qty || 0) + 1);
}

function dec(it) {
  // ✅ không cho giảm dưới 1
  if (Number(it.qty || 0) <= 1) return;
  updateQty(it.key, Number(it.qty || 0) - 1);
}

function onImgError(e) {
  e.target.src =
      "data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='120' height='120'%3E%3Crect width='100%25' height='100%25' fill='%23f1f3f5'/%3E%3Ctext x='50%25' y='52%25' dominant-baseline='middle' text-anchor='middle' fill='%2399a1aa' font-size='14'%3E%E1%BA%A2nh%3C/text%3E%3C/svg%3E";
}

function formatMoney(v) {
  const n = Number(v) || 0;
  return n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
}
</script>

<style scoped>
/* ====== FONT SCALE: MINI CART (cân hơn, không quá to) ====== */
.mini-cart {
  width: 360px;
  max-width: calc(100vw - 20px);
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  font-size: 13.5px; /* base */
}

.mini-cart__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  background: #000f51;
  color: #fff;
}

.mini-cart__title {
  font-weight: 700;
  letter-spacing: 0.3px;
  font-size: 15px; /* GIỎ HÀNG */
}

.mini-cart__close {
  border: none;
  background: transparent;
  color: #fff;
  font-size: 20px;
  line-height: 1;
  padding: 0;
}

.mini-cart__body {
  max-height: 420px;
  overflow: auto;
}

.mini-cart__empty {
  padding: 16px 12px;
  color: #6c757d;
  font-size: 13.5px;
}

.mini-cart__list {
  padding: 10px 12px 0;
}

.mini-cart__item {
  display: grid;
  grid-template-columns: 62px 1fr 28px;
  gap: 12px;
  padding: 10px 8px;
  border-radius: 10px;
}

.mini-cart__item + .mini-cart__item {
  margin-top: 6px;
  border-top: 1px dashed rgba(0, 0, 0, 0.12);
  border-top-left-radius: 0;
  border-top-right-radius: 0;
}

.mini-cart__img {
  width: 62px;
  height: 62px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: #f1f3f5;
}

.mini-cart__name {
  font-weight: 700;
  color: #111;
  font-size: 13.6px; /* tên SP */
  line-height: 1.25;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.mini-cart__meta {
  display: flex;
  gap: 10px;
  color: #6c757d;
  font-size: 12.3px; /* màu/size */
  margin-top: 3px;
}

.dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 1px solid rgba(0, 0, 0, 0.15);
  vertical-align: middle;
  margin-left: 4px;
}

.mini-cart__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
  gap: 10px;
}

.mini-cart__qty {
  display: inline-flex;
  align-items: center;
  border: 1px solid rgba(0, 0, 0, 0.12);
  border-radius: 7px;
  overflow: hidden;
}

.qty-btn {
  width: 30px;
  height: 30px;
  border: none;
  background: #f4f5f7;
  font-weight: 900;
  font-size: 14px;
}

.qty-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.qty-input {
  width: 36px;
  height: 30px;
  border: none;
  text-align: center;
  background: #fff;
  font-weight: 700;
  font-size: 13.5px;
}

.mini-cart__price {
  font-weight: 750;
  color: #000f51;
  white-space: nowrap;
  font-size: 14.2px; /* giá */
}

.mini-cart__remove {
  border: none;
  background: transparent;
  color: #111;
  font-size: 20px;
  line-height: 1;
  opacity: 0.6;
}

.mini-cart__remove:hover {
  opacity: 1;
}

.mini-cart__footer {
  padding: 12px;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
}

.mini-cart__summary {
  margin-bottom: 10px;
}

.mini-cart__sumrow {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13.2px; /* tổng/tạm tính */
  padding: 4px 0;
}
</style>