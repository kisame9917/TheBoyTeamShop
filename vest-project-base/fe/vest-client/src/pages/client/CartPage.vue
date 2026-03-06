<template>
  <div class="cart-page container py-4">
    <nav aria-label="breadcrumb" class="mb-3">
      <ol class="breadcrumb mb-0">
        <li class="breadcrumb-item">
          <router-link to="/" class="text-muted text-decoration-none">
            Trang chủ
          </router-link>
        </li>
        <li class="breadcrumb-item active text-dark" aria-current="page">
          Giỏ hàng
        </li>
      </ol>
    </nav>

    <h1 class="page-title mb-4">Giỏ hàng</h1>

    <div v-if="items.length === 0" class="empty-cart">
      <div class="empty-title">Giỏ hàng của bạn đang trống</div>
      <div class="empty-desc">Hãy tiếp tục mua sắm để thêm sản phẩm.</div>
      <button class="btn btn-dark mt-3" @click="goShopping">
        Tiếp tục mua sắm
      </button>
    </div>

    <div v-else class="row g-4">
      <div class="col-lg-8">
        <div class="cart-table-wrap">
          <table class="table cart-table align-middle mb-0">
            <thead>
              <tr>
                <th>Sản phẩm</th>
                <th class="text-center">Đơn giá</th>
                <th class="text-center">Số lượng</th>
                <th class="text-end">Thành tiền</th>
                <th class="text-center">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="it in items" :key="it.idSanPhamChiTiet">
                <td>
                  <div class="prod">
                    <img
                      :src="it.image"
                      class="prod-img"
                      alt="sp"
                      @error="onImgError"
                    />
                    <div class="prod-info">
                      <div class="prod-name">{{ it.name }}</div>

                      <div class="prod-meta">
                        <span v-if="it.color">Màu: {{ it.color }}</span>
                        <span v-if="it.size">Kích cỡ: {{ it.size }}</span>
                      </div>

                      <div class="small text-muted" v-if="it.code">
                        Mã SPCT: {{ it.code }}
                      </div>

                      <div
                        class="small text-muted"
                        v-if="it.idSanPhamChiTiet"
                      >
                        ID SPCT: {{ it.idSanPhamChiTiet }}
                      </div>

                      <div class="small text-muted" v-if="it.stock >= 0">
                        Tồn kho: {{ it.stock }}
                      </div>
                    </div>
                  </div>
                </td>

                <td class="text-center">
                  {{ formatMoney(it.price) }} đ
                </td>

                <td class="text-center">
                  <div class="qty-box">
                    <button
                      class="qty-btn"
                      type="button"
                      @click="decreaseQty(it)"
                      :disabled="Number(it.qty) <= 1"
                    >
                      -
                    </button>

                    <input
                      class="qty-input"
                      type="number"
                      min="1"
                      :max="it.stock || 999"
                      :value="it.qty"
                      @change="onQtyInput(it, $event)"
                    />

                    <button
                      class="qty-btn"
                      type="button"
                      @click="increaseQty(it)"
                      :disabled="it.stock > 0 ? Number(it.qty) >= Number(it.stock) : false"
                    >
                      +
                    </button>
                  </div>
                </td>

                <td class="text-end">
                  {{ formatMoney((Number(it.price) || 0) * (Number(it.qty) || 0)) }} đ
                </td>

                <td class="text-center">
                  <button
                    class="btn btn-sm btn-outline-danger"
                    type="button"
                    @click="askRemove(it)"
                  >
                    Xóa
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="col-lg-4">
        <div class="cart-summary">
          <div class="summary-title">Tóm tắt đơn hàng</div>

          <div class="sum-line">
            <span>Tổng sản phẩm</span>
            <span>{{ totalQty }}</span>
          </div>

          <div class="sum-line">
            <span>Tạm tính</span>
            <span>{{ formatMoney(subtotal) }} đ</span>
          </div>

          <div class="sum-line total">
            <span>Tổng cộng</span>
            <span>{{ formatMoney(subtotal) }} đ</span>
          </div>

          <button class="btn btn-dark w-100 mt-3" type="button" @click="checkout">
            Tiến hành thanh toán
          </button>

          <button
            class="btn btn-outline-secondary w-100 mt-2"
            type="button"
            @click="goShopping"
          >
            Tiếp tục mua sắm
          </button>
        </div>
      </div>
    </div>

<ConfirmModal
  :open="confirmOpen"
  title="Xóa sản phẩm"
  message="Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng?"
  confirm-text="Xóa"
  cancel-text="Hủy"
  @confirm="confirmRemove"
  @cancel="closeConfirm"
/>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { useCart } from "../../composables/useCart";
import ConfirmModal from "../../components/common/ConfirmModal.vue";

const router = useRouter();

const {
  cartItems,
  totalQty,
  totalAmount,
  removeFromCart,
  updateQty,
} = useCart();

const confirmOpen = ref(false);
const pendingItemId = ref(null);

const items = computed(() => cartItems.value);
const subtotal = computed(() => totalAmount.value);

function askRemove(it) {
  pendingItemId.value = it?.idSanPhamChiTiet || null;
  confirmOpen.value = true;
}

function closeConfirm() {
  confirmOpen.value = false;
  pendingItemId.value = null;
}

function confirmRemove() {
  if (pendingItemId.value) {
    removeFromCart(pendingItemId.value);
  }
  closeConfirm();
}

function normalizeQty(value, stock = 0) {
  let qty = Number(value) || 1;
  if (qty < 1) qty = 1;

  if (Number(stock) > 0 && qty > Number(stock)) {
    qty = Number(stock);
  }

  return qty;
}

function decreaseQty(it) {
  const nextQty = normalizeQty(Number(it.qty) - 1, it.stock);
  updateQty(it.idSanPhamChiTiet, nextQty);
}

function increaseQty(it) {
  const nextQty = normalizeQty(Number(it.qty) + 1, it.stock);
  updateQty(it.idSanPhamChiTiet, nextQty);
}

function onQtyInput(it, event) {
  const nextQty = normalizeQty(event.target.value, it.stock);
  updateQty(it.idSanPhamChiTiet, nextQty);
  event.target.value = nextQty;
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

function onImgError(e) {
  e.target.src =
    "data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='120' height='120'%3E%3Crect width='100%25' height='100%25' fill='%23f1f3f5'/%3E%3Ctext x='50%25' y='52%25' dominant-baseline='middle' text-anchor='middle' fill='%2399a1aa' font-size='14'%3E%E1%BA%A2nh%3C/text%3E%3C/svg%3E";
}
</script>

<style scoped>
.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #111;
}

.empty-cart {
  padding: 48px 16px;
  text-align: center;
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
}

.empty-title {
  font-size: 22px;
  font-weight: 700;
  color: #111;
}

.empty-desc {
  margin-top: 8px;
  color: #6c757d;
  font-size: 14px;
}

.cart-table-wrap {
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  overflow: hidden;
}

.cart-table th {
  font-size: 13px;
  font-weight: 700;
  color: #111;
  border-bottom: 1px solid rgba(0, 0, 0, 0.12);
  background: #fafafa;
}

.cart-table td {
  font-size: 14px;
  color: #111;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  vertical-align: top;
  padding-top: 16px;
  padding-bottom: 16px;
}

.prod {
  display: grid;
  grid-template-columns: 72px 1fr;
  gap: 12px;
  align-items: start;
}

.prod-img {
  width: 72px;
  height: 92px;
  object-fit: cover;
  border-radius: 8px;
  background: #f1f3f5;
  border: 1px solid rgba(0, 0, 0, 0.08);
}

.prod-name {
  font-weight: 700;
  font-size: 14px;
  line-height: 1.3;
}

.prod-meta {
  margin-top: 6px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  font-size: 12.5px;
  color: #6c757d;
}

.qty-box {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.qty-btn {
  width: 30px;
  height: 30px;
  border: 1px solid #ccc;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
}

.qty-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.qty-input {
  width: 56px;
  text-align: center;
  border: 1px solid #ccc;
  border-radius: 6px;
  padding: 4px 6px;
}

.cart-summary {
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  padding: 18px;
}

.summary-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 12px;
  color: #111;
}

.sum-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
  color: #111;
}

.sum-line.total {
  font-size: 16px;
  font-weight: 700;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
  margin-top: 8px;
  padding-top: 14px;
}
</style>