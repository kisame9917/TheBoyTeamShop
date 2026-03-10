<template>
  <div class="mini-cart" role="dialog" aria-label="Giỏ hàng" @click.stop>
    <div class="mini-cart__header">
      <div>
        <div class="mini-cart__title">GIỎ HÀNG</div>
        <div class="mini-cart__subtitle">{{ totalQty }} sản phẩm</div>
      </div>

      <button class="mini-cart__close" type="button" aria-label="Đóng" @click="$emit('close')">
        <i class="bi bi-x"></i>
      </button>
    </div>

    <div class="mini-cart__body">
      <div v-if="cartItems.length === 0" class="mini-cart__empty">
        <div class="mini-cart__empty-icon">
          <i class="bi bi-bag-x"></i>
        </div>
        <div class="mini-cart__empty-title">Giỏ hàng đang trống</div>
        <div class="mini-cart__empty-text">Hãy thêm sản phẩm để tiếp tục mua sắm.</div>
      </div>

      <div v-else class="mini-cart__list">
        <div
            v-for="it in cartItems"
            :key="it.idSanPhamChiTiet || it.key"
            class="mini-cart__item"
        >
          <img
              :src="getItemImage(it)"
              class="mini-cart__img"
              alt="Sản phẩm"
              @error="onImgError"
          />

          <div class="mini-cart__info">
            <div class="mini-cart__name" :title="it.name">{{ it.name }}</div>

            <div class="mini-cart__meta">
              <span v-if="it.color">Màu: {{ it.color }}</span>
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

                <button
                    class="qty-btn"
                    type="button"
                    aria-label="Tăng"
                    :disabled="it.stock > 0 ? Number(it.qty) >= Number(it.stock) : false"
                    @click="inc(it)"
                >
                  +
                </button>
              </div>

              <div class="mini-cart__price">
                {{ formatMoney((Number(it.price) || 0) * (Number(it.qty) || 0)) }} đ
              </div>
            </div>
          </div>

          <button class="mini-cart__remove" type="button" aria-label="Xóa" @click="remove(it)">
            <i class="bi bi-trash"></i>
          </button>
        </div>
      </div>
    </div>

    <div class="mini-cart__footer" v-if="cartItems.length">
      <div class="mini-cart__summary">
        <div class="mini-cart__sumrow">
          <span>Tổng sản phẩm</span>
          <strong>{{ totalQty }}</strong>
        </div>
        <div class="mini-cart__sumrow mini-cart__sumrow--total">
          <span>Tạm tính</span>
          <strong>{{ formatMoney(subtotal) }} đ</strong>
        </div>
      </div>

      <div class="mini-cart__actions">
        <router-link to="/cart" class="mini-cart__btn mini-cart__btn--outline" @click="$emit('close')">
          Xem giỏ hàng
        </router-link>

        <router-link to="/checkout" class="mini-cart__btn mini-cart__btn--primary" @click="$emit('close')">
          Đặt hàng
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useCart } from "../../composables/useCart";

defineEmits(["close"]);

const { items, totalQty, subtotal, updateQty, removeItem } = useCart();

const cartItems = computed(() => items.value || []);

function getItemImage(it) {
  return (
      it?.image ||
      "data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='120' height='120'%3E%3Crect width='100%25' height='100%25' fill='%23f1f3f5'/%3E%3Ctext x='50%25' y='52%25' dominant-baseline='middle' text-anchor='middle' fill='%2399a1aa' font-size='14'%3E%E1%BA%A2nh%3C/text%3E%3C/svg%3E"
  );
}

function formatMoney(v) {
  const n = Number(v) || 0;
  return n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
}

function inc(it) {
  updateQty(it.key, Number(it.qty) + 1);
}

function dec(it) {
  updateQty(it.key, Number(it.qty) - 1);
}

function remove(it) {
  removeItem(it.key || it.idSanPhamChiTiet);
}

function onImgError(e) {
  e.target.src =
      "data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='120' height='120'%3E%3Crect width='100%25' height='100%25' fill='%23f1f3f5'/%3E%3Ctext x='50%25' y='52%25' dominant-baseline='middle' text-anchor='middle' fill='%2399a1aa' font-size='14'%3E%E1%BA%A2nh%3C/text%3E%3C/svg%3E";
}
</script>

<style scoped>
.mini-cart {
  width: 390px;
  max-width: calc(100vw - 20px);
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 22px 42px rgba(0, 15, 81, 0.18);
  font-size: 13.5px;
}

.mini-cart__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: linear-gradient(90deg, #000f51 0%, #12348f 100%);
  color: #fff;
}

.mini-cart__title {
  font-weight: 750;
  letter-spacing: 0.3px;
  font-size: 15px;
}

.mini-cart__subtitle {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.82);
  margin-top: 2px;
}

.mini-cart__close {
  border: none;
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  width: 34px;
  height: 34px;
  border-radius: 10px;
  font-size: 18px;
  line-height: 1;
}

.mini-cart__body {
  max-height: 420px;
  overflow: auto;
  background: #fff;
}

.mini-cart__empty {
  padding: 28px 18px;
  text-align: center;
}

.mini-cart__empty-icon {
  font-size: 36px;
  color: #94a3b8;
  margin-bottom: 10px;
}

.mini-cart__empty-title {
  font-weight: 750;
  color: #0f172a;
  margin-bottom: 4px;
}

.mini-cart__empty-text {
  color: #64748b;
  font-size: 13px;
}

.mini-cart__list {
  padding: 12px;
}

.mini-cart__item {
  display: grid;
  grid-template-columns: 68px 1fr 32px;
  gap: 12px;
  padding: 12px;
  border-radius: 16px;
  background: #fbfcff;
  border: 1px solid rgba(148, 163, 184, 0.12);
}

.mini-cart__item + .mini-cart__item {
  margin-top: 10px;
}

.mini-cart__img {
  width: 68px;
  height: 68px;
  object-fit: cover;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: #f1f3f5;
}

.mini-cart__name {
  font-weight: 750;
  color: #0f172a;
  font-size: 13.8px;
  line-height: 1.35;
}

.mini-cart__meta {
  display: flex;
  gap: 10px;
  color: #64748b;
  font-size: 12.2px;
  margin-top: 4px;
  flex-wrap: wrap;
}

.mini-cart__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 10px;
}

.mini-cart__qty {
  display: inline-flex;
  align-items: center;
  border: 1px solid #d9deea;
  border-radius: 999px;
  overflow: hidden;
  background: #fff;
}

.qty-btn {
  border: 0;
  background: #eef2f7;
  width: 28px;
  height: 28px;
  font-weight: 750;
  color: #0f172a;
}

.qty-btn:disabled {
  opacity: 0.45;
}

.qty-input {
  width: 34px;
  border: 0;
  text-align: center;
  background: #fff;
  font-size: 12px;
  font-weight: 750;
  color: #0f172a;
}

.mini-cart__price {
  font-weight: 750;
  color: #000f51;
  white-space: nowrap;
}

.mini-cart__remove {
  align-self: start;
  border: 0;
  background: #fff5f5;
  color: #dc2626;
  width: 32px;
  height: 32px;
  border-radius: 10px;
  font-size: 14px;
}

.mini-cart__footer {
  padding: 14px 14px 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  background: #fff;
}

.mini-cart__summary {
  margin-bottom: 14px;
}

.mini-cart__sumrow {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #475569;
  font-size: 13px;
}

.mini-cart__sumrow + .mini-cart__sumrow {
  margin-top: 8px;
}

.mini-cart__sumrow--total strong {
  color: #000f51;
  font-size: 16px;
}

.mini-cart__actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.mini-cart__btn {
  min-height: 42px;
  border-radius: 12px;
  font-weight: 750;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
}

.mini-cart__btn--outline {
  border: 1px solid #d8dfec;
  background: #fff;
  color: #0f172a;
}

.mini-cart__btn--primary {
  border: none;
  background: linear-gradient(135deg, #000f51 0%, #0f2c9c 100%);
  color: #fff;
}
</style>