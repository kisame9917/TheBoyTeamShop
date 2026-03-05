<template>
  <div class="cart-page container py-4">
    <nav aria-label="breadcrumb" class="mb-4">
      <ol class="breadcrumb fs-6">
        <li class="breadcrumb-item">
          <router-link to="/" class="text-muted text-decoration-none">Trang chủ</router-link>
        </li>
        <li class="breadcrumb-item active text-dark" aria-current="page">Giỏ hàng</li>
      </ol>
    </nav>

    <h2 class="cart-title mb-4">GIỎ HÀNG</h2>

    <div class="row g-4">
      <div class="col-lg-8">
        <div class="card border-0 shadow-sm rounded-3">
          <div class="card-body p-0">
            <div class="table-responsive">
              <table class="table align-middle mb-0 cart-table">
                <thead>
                <tr>
                  <th class="px-4 py-3">Sản phẩm</th>
                  <th class="py-3 text-center">Số lượng</th>
                  <th class="py-3 text-center">&nbsp;</th>
                  <th class="py-3 text-end">Giá</th>
                  <th class="px-4 py-3 text-end">Tổng tiền</th>
                </tr>
                </thead>
                <tbody v-if="items.length">
                <tr v-for="it in items" :key="it.key">
                  <td class="px-4 py-3">
                    <div class="d-flex gap-3 align-items-center">
                      <img :src="it.image" class="cart-img" alt="Sản phẩm" />
                      <div>
                        <div class="cart-name">{{ it.name }}</div>
                        <div class="cart-meta">
                          <span v-if="it.color">Màu: <span class="dot" :style="{ backgroundColor: it.color }"></span></span>
                          <span v-if="it.size">Kích cỡ: {{ it.size }}</span>
                        </div>
                      </div>
                    </div>
                  </td>

                  <td class="py-3 text-center">
                    <div class="qty">
                      <button class="qty-btn" type="button" aria-label="Giảm" :disabled="Number(it.qty) <= 1" @click="updateQty(it.key, it.qty - 1)">-</button>
                      <input class="qty-input" type="text" :value="it.qty" readonly />
                      <button class="qty-btn" type="button" aria-label="Tăng" @click="updateQty(it.key, it.qty + 1)">+</button>
                    </div>
                  </td>

                  <td class="py-3 text-center">
                    <button class="remove-icon" type="button" aria-label="Xóa" @click="askRemove(it)">
                      <i class="bi bi-trash"></i>
                    </button>
                  </td>

                  <td class="py-3 text-end fw-semibold">{{ formatMoney(it.price) }} đ</td>
                  <td class="px-4 py-3 text-end fw-bold">{{ formatMoney(it.price * it.qty) }} đ</td>
                </tr>
                </tbody>

                <tbody v-else>
                <tr>
                  <td class="px-4 py-5 text-center text-muted" colspan="5">
                    Giỏ hàng của bạn đang trống.
                  </td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>
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

        <div class="d-flex justify-content-between align-items-center mt-4">
          <button class="btn btn-outline-dark px-4 py-2 fw-semibold" type="button" @click="goShopping">
            TIẾP TỤC MUA HÀNG
          </button>

          <button class="btn btn-primary px-5 py-2 fw-semibold" type="button" :disabled="!items.length" @click="checkout">
            ĐẶT HÀNG
          </button>
        </div>
      </div>

      <div class="col-lg-4">
        <div class="card border-0 shadow-sm rounded-3">
          <div class="card-body p-4">
            <div class="sum-row">
              <span>Tổng sản phẩm</span>
              <strong>{{ totalQty }}</strong>
            </div>
            <div class="sum-row">
              <span>Tạm tính</span>
              <strong class="sum-amount">{{ formatMoney(subtotal) }} đ</strong>
            </div>
          </div>
        </div>

        <div class="promo-img mt-4 d-none d-lg-block">
          <img src="/uploads/ao-vest-den-6.jpg" alt="Banner" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useCart } from "../../composables/useCart";
import ConfirmModal from "../../components/common/ConfirmModal.vue";

const router = useRouter();
const { items, totalQty, subtotal, removeItem, updateQty } = useCart();

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

function formatMoney(v) {
  const n = Number(v) || 0;
  return n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
}

function goShopping() {
  router.push({ name: "Search" });
}

function checkout() {
  router.push({ name: "Checkout" });
}
</script>

<style scoped>
.cart-title {
  font-weight: 750;
  letter-spacing: 0.5px;
  color: #111;
}

/* tăng size chữ theo yêu cầu */
.cart-table {
  font-size: 1.05rem;
}

.cart-name {
  font-weight: 750;
  font-size: 1.05rem;
  color: #111;
}

.cart-meta {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  color: #6c757d;
  font-size: 0.95rem;
  margin-top: 4px;
}

.cart-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid rgba(0,0,0,0.08);
}

.dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 1px solid rgba(0,0,0,0.15);
  vertical-align: middle;
  margin-left: 6px;
}

.qty {
  display: inline-flex;
  align-items: center;
  border: 1px solid rgba(0,0,0,0.12);
  border-radius: 8px;
  overflow: hidden;
}

.qty-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: #f4f5f7;
  font-weight: 800;
  font-size: 1.05rem;
}

.qty-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.remove-icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  border: 1px solid rgba(0, 0, 0, 0.12);
  background: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #111;
  opacity: 0.75;
}

.remove-icon:hover {
  opacity: 1;
  border-color: rgba(0, 0, 0, 0.22);
}

.qty-input {
  width: 46px;
  height: 36px;
  border: none;
  text-align: center;
  background: #fff;
  font-weight: 750;
  font-size: 1.05rem;
}

.sum-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 1.05rem;
  padding: 8px 0;
}

.sum-amount {
  color: #000f51;
  font-size: 1.15rem;
}

.promo-img img {
  width: 100%;
  height: 420px;
  object-fit: cover;
  border-radius: 12px;
  box-shadow: 0 12px 30px rgba(0,0,0,0.12);
}
</style>
