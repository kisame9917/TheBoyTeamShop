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

    <div class="cart-page__head mb-4">
      <div>
        <h2 class="cart-title mb-1">GIỎ HÀNG</h2>
        <div class="cart-subtitle">Kiểm tra sản phẩm, số lượng và tiến hành đặt hàng</div>
      </div>
      <div class="cart-badge">{{ totalQty }} sản phẩm</div>
    </div>

    <div class="row g-4">
      <div class="col-lg-8">
        <div class="cart-shell">
          <div class="cart-table-wrap">
            <table class="table align-middle mb-0 cart-table">
              <thead>
              <tr>
                <th class="cart-head px-4 py-3">Sản phẩm</th>
                <th class="cart-head py-3 text-center">Số lượng</th>
                <th class="cart-head py-3 text-center">
<!--                  <i class="bi bi-trash3"></i>-->
                </th>
                <th class="cart-head py-3 text-end">Đơn giá</th>
                <th class="cart-head px-4 py-3 text-end">Tổng tiền</th>
              </tr>
              </thead>

              <tbody v-if="items.length">
              <tr v-for="it in items" :key="it.key" class="cart-row">
                <td class="px-4 py-3">
                  <div class="d-flex gap-3 align-items-center">
                    <div class="cart-img-wrap">
                      <img :src="it.image" class="cart-img" alt="Sản phẩm" @error="onImgError" />
                    </div>

                    <div>
                      <div class="cart-name">{{ it.name }}</div>
                      <div class="cart-meta">
                          <span v-if="it.color">
                            Màu:
                            <span class="meta-value">
                              {{ it.color }}
                            </span>
                          </span>
                        <span v-if="it.size">
                            Kích cỡ:
                            <span class="meta-value">{{ it.size }}</span>
                          </span>
                        <span v-if="it.code">
                            Mã:
                            <span class="meta-value">{{ it.code }}</span>
                          </span>
                      </div>
                    </div>
                  </div>
                </td>

                <td class="py-3 text-center">
                  <div class="qty">
                    <button
                        class="qty-btn"
                        type="button"
                        aria-label="Giảm"
                        :disabled="Number(it.qty) <= 1"
                        @click="updateQty(it.key, Number(it.qty) - 1)"
                    >
                      -
                    </button>

                    <input class="qty-input" type="text" :value="it.qty" readonly />

                    <button
                        class="qty-btn"
                        type="button"
                        aria-label="Tăng"
                        :disabled="it.stock > 0 ? Number(it.qty) >= Number(it.stock) : false"
                        @click="updateQty(it.key, Number(it.qty) + 1)"
                    >
                      +
                    </button>
                  </div>
                </td>

                <td class="py-3 text-center">
                  <button class="remove-icon" type="button" aria-label="Xóa" @click="askRemove(it)">
                    <i class="bi bi-trash"></i>
                  </button>
                </td>

                <td class="py-3 text-end fw-semibold cart-price">
                  {{ formatMoney(it.price) }} đ
                </td>

                <td class="px-4 py-3 text-end fw-bold cart-total">
                  {{ formatMoney((Number(it.price) || 0) * (Number(it.qty) || 0)) }} đ
                </td>
              </tr>
              </tbody>

              <tbody v-else>
              <tr>
                <td class="px-4 py-5 text-center text-muted cart-empty" colspan="5">
                  <div class="cart-empty__icon">
                    <i class="bi bi-bag-x"></i>
                  </div>
                  <div class="cart-empty__title">Giỏ hàng của bạn đang trống</div>
                  <div class="cart-empty__text">Hãy quay lại cửa hàng để chọn thêm sản phẩm phù hợp.</div>
                </td>
              </tr>
              </tbody>
            </table>
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

        <div class="cart-actions mt-4">
          <button class="btn-continue" type="button" @click="goShopping">
            TIẾP TỤC MUA HÀNG
          </button>

<!--          <button class="btn-checkout" type="button" :disabled="!items.length" @click="checkout">-->
<!--            ĐẶT HÀNG-->
<!--          </button>-->
        </div>
      </div>

      <div class="col-lg-4">
        <div class="summary-card">
          <div class="summary-card__head">TÓM TẮT ĐƠN HÀNG</div>

          <div class="summary-card__body">
            <div class="sum-row">
              <span>Tổng sản phẩm</span>
              <strong>{{ totalQty }}</strong>
            </div>

            <div class="sum-row">
              <span>Tạm tính</span>
              <strong>{{ formatMoney(subtotal) }} đ</strong>
            </div>

            <div class="sum-row total">
              <span>Thành tiền</span>
              <strong class="sum-amount">{{ formatMoney(subtotal) }} đ</strong>
            </div>

            <div class="summary-note">
              Giá chưa bao gồm các ưu đãi hoặc phí phát sinh khác nếu có.
            </div>

            <button class="summary-btn" type="button" :disabled="!items.length" @click="checkout">
              Tiến hành đặt hàng
            </button>
          </div>
        </div>

        <div class="summary-benefits mt-3">
          <div class="benefit-item">
            <i class="bi bi-shield-check"></i>
            <span>Thanh toán an toàn</span>
          </div>
          <div class="benefit-item">
            <i class="bi bi-truck"></i>
            <span>Giao hàng toàn quốc</span>
          </div>
          <div class="benefit-item">
            <i class="bi bi-arrow-repeat"></i>
            <span>Hỗ trợ đổi trả theo chính sách</span>
          </div>
        </div>
      </div>
    </div>
    <div class="related-cart-section mt-4">
      <div class="related-cart-card">
        <div class="related-cart-card__header">
          <span>Sản phẩm tương tự</span>
        </div>

        <div class="related-cart-card__body">
          <div class="related-cart-grid">
            <div
                class="related-cart-item"
                v-for="(it, index) in relatedProducts"
                :key="index"
            >
              <div class="related-cart-item__img-wrap">
                <img
                    :src="it.image"
                    :alt="it.name"
                    class="related-cart-item__img"
                    @error="onImgError"
                />
              </div>

              <div class="related-cart-item__content">
                <div class="related-cart-item__name">
                  {{ it.name }}
                </div>

                <div class="related-cart-item__meta">
                  {{ it.desc }}
                </div>

                <div class="related-cart-item__bottom">
                  <div class="related-cart-item__price">
                    {{ formatMoney(it.price) }} đ
                  </div>

                  <router-link
                      :to="it.link"
                      class="related-cart-item__btn"
                  >
                    Xem chi tiết
                  </router-link>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref , onMounted } from "vue";
import { useRouter } from "vue-router";
import { useCart } from "../../composables/useCart";
import ConfirmModal from "../../components/common/ConfirmModal.vue";
import { getProducts } from "../../services/productClientApi"; // sửa đúng path service của bạn
const router = useRouter();
const { items, totalQty, subtotal, removeItem, updateQty } = useCart();

const confirmOpen = ref(false);
const pendingKey = ref(null);
const relatedProducts = ref([]);

onMounted(async () => {
  try {
    const res = await getProducts({ page: 0, size: 1000 });
    const raw = res?.data?.content || res?.data || [];

    relatedProducts.value = raw.map((p) => ({
      name: p.tenSanPham || p.ten || "Sản phẩm",
      desc: p.moTaNgan || p.moTa || "Sản phẩm phù hợp với phong cách của bạn.",
      price: p.giaBan || p.donGia || 0,
      image:
        p.anh ||
        p.image ||
        p.hinhAnh ||
        "data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='300' height='360'%3E%3Crect width='100%25' height='100%25' fill='%23f1f3f5'/%3E%3Ctext x='50%25' y='52%25' dominant-baseline='middle' text-anchor='middle' fill='%2399a1aa' font-size='18'%3EProduct%3C/text%3E%3C/svg%3E",
      link: `/product/${p.id}`,
    }));
  } catch (e) {
    console.error(e);
    relatedProducts.value = [];
  }
});

function askRemove(it) {
  pendingKey.value = it?.key || it?.idSanPhamChiTiet || null;
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

function onImgError(e) {
  e.target.src =
      "data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='120' height='120'%3E%3Crect width='100%25' height='100%25' fill='%23f1f3f5'/%3E%3Ctext x='50%25' y='52%25' dominant-baseline='middle' text-anchor='middle' fill='%2399a1aa' font-size='14'%3E%E1%BA%A2nh%3C/text%3E%3C/svg%3E";
}

function goShopping() {
  router.push({ name: "Search" });
}

function checkout() {
  router.push({ name: "Checkout" });
}
</script>

<style scoped>
.cart-page {
  background: linear-gradient(180deg, #f5f7fc 0%, #f3f4f8 100%);
  min-height: 100vh;
}

.cart-page__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.cart-title {
  font-weight: 750;
  letter-spacing: 0.4px;
  color: #0f172a;
  margin: 0;
}

.cart-subtitle {
  color: #64748b;
  font-size: 14px;
}

.cart-badge {
  padding: 10px 14px;
  border-radius: 999px;
  background: #eef2ff;
  color: #1e3a8a;
  font-weight: 750;
  font-size: 14px;
}

.cart-shell,
.summary-card,
.summary-benefits {
  background: #fff;
  border-radius: 22px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 16px 36px rgba(10, 24, 74, 0.06);
  overflow: hidden;
}

.cart-table-wrap {
  overflow-x: auto;
}

.cart-table {
  border-collapse: separate;
  border-spacing: 0;
  overflow: hidden;
}

.cart-table thead tr {
  background: linear-gradient(90deg, #00145f 0%, #0f2f98 100%);
}

.cart-table thead th.cart-head {
  background: transparent;
  color: #fff;
  border: none;
  font-weight: 750;
  font-size: 15px;
  white-space: nowrap;
  vertical-align: middle;
  padding-top: 16px !important;
  padding-bottom: 16px !important;
  letter-spacing: 0.2px;
}

.cart-table thead th.cart-head:first-child {
  border-top-left-radius: 18px;
}

.cart-table thead th.cart-head:last-child {
  border-top-right-radius: 18px;
}

.cart-row td {
  border-color: #edf0f5;
  vertical-align: middle;
}

.cart-img-wrap {
  width: 88px;
  height: 88px;
  border-radius: 18px;
  overflow: hidden;
  background: #f8fafc;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.cart-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cart-name {
  font-weight: 750;
  font-size: 1.04rem;
  color: #0f172a;
  line-height: 1.4;
  margin-bottom: 4px;
}

.cart-meta {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  color: #64748b;
  font-size: 0.93rem;
}

.meta-value {
  color: #334155;
  font-weight: 700;
}

.qty {
  display: inline-flex;
  align-items: center;
  border: 1px solid #d9deea;
  border-radius: 16px;
  overflow: hidden;
  background: #f8fafc;
  padding: 4px;
  gap: 4px;
}

.qty-btn {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 10px;
  background: #e9edf7;
  color: #0f172a;
  font-weight: 750;
  font-size: 1.05rem;
  transition: all 0.2s ease;
}

.qty-btn:hover:not(:disabled) {
  background: #000f51;
  color: #fff;
}

.qty-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.qty-input {
  width: 46px;
  height: 36px;
  border: none;
  text-align: center;
  background: #fff;
  border-radius: 10px;
  font-weight: 750;
  color: #0f172a;
}

.remove-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  border: 1px solid rgba(220, 38, 38, 0.12);
  background: #fff5f5;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #dc2626;
  transition: all 0.2s ease;
}

.remove-icon:hover {
  background: #dc2626;
  color: #fff;
}

.cart-price {
  color: #334155;
}

.cart-total {
  color: #000f51;
  font-size: 1.04rem;
}

.cart-actions {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
}

.btn-checkout:hover:not(:disabled),
.summary-btn:hover:not(:disabled) {
  background: #001a72;
  transform: translateY(-1px);
}


.btn-checkout,
.summary-btn {
  border: none;
  background: linear-gradient(135deg, #000f51 0%, #0f2c9c 100%);
  color: #fff;
  box-shadow: 0 14px 28px rgba(0, 15, 81, 0.18);
}

.btn-checkout:hover:not(:disabled),
.summary-btn:hover:not(:disabled) {
  transform: translateY(-1px);
}

.btn-checkout:disabled,
.summary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.summary-card__head {
  padding: 16px 20px;
  background: linear-gradient(90deg, #000f51 0%, #12348f 100%);
  color: #fff;
  font-weight: 750;
  letter-spacing: 0.3px;
}

.summary-card__body {
  padding: 22px 20px;
}

.sum-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 11px 0;
  color: #334155;
  font-size: 1rem;
}

.sum-row + .sum-row {
  border-top: 1px dashed rgba(148, 163, 184, 0.35);
}

.sum-row.total {
  margin-top: 6px;
  padding-top: 16px;
}

.sum-amount {
  color: #000f51;
  font-size: 1.2rem;
}

.summary-note {
  margin-top: 14px;
  padding: 14px 15px;
  border-radius: 14px;
  background: #f8fafc;
  color: #64748b;
  font-size: 14px;
  line-height: 1.6;
}

.summary-btn {
  width: 100%;
  margin-top: 18px;
}

.summary-benefits {
  padding: 16px 18px;
  display: grid;
  gap: 12px;
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #475569;
  font-weight: 600;
}

.benefit-item i {
  color: #000f51;
}

.cart-empty {
  background: #fff;
}

.cart-empty__icon {
  font-size: 38px;
  color: #94a3b8;
  margin-bottom: 10px;
}

.cart-empty__title {
  font-weight: 750;
  color: #0f172a;
  font-size: 18px;
  margin-bottom: 6px;
}

.cart-empty__text {
  color: #64748b;
}

@media (max-width: 767.98px) {
  .cart-actions {
    flex-direction: column;
  }

  .btn-continue,
  .btn-checkout {
    width: 100%;
  }
}

.related-cart-section {
  margin-top: 28px;
}

.related-cart-card {
  background: #fff;
  border-radius: 22px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 16px 36px rgba(10, 24, 74, 0.06);
  overflow: hidden;
}

.related-cart-card__header {
  padding: 16px 20px;
  background: linear-gradient(90deg, #00145f 0%, #0f2f98 100%);
  color: #fff;
  font-weight: 750;
  font-size: 16px;
  letter-spacing: 0.2px;
}

.related-cart-card__body {
  padding: 20px;
}

.related-cart-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.related-cart-item {
  border-radius: 18px;
  overflow: hidden;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: #fff;
  box-shadow: 0 10px 24px rgba(10, 24, 74, 0.05);
  transition: all 0.25s ease;
}

.related-cart-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 32px rgba(10, 24, 74, 0.1);
}

.related-cart-item__img-wrap {
  padding: 12px;
  background: #f8fafc;
}

.related-cart-item__img {
  width: 100%;
  height: 240px;
  object-fit: cover;
  border-radius: 14px;
  display: block;
}

.related-cart-item__content {
  padding: 14px;
}

.related-cart-item__name {
  font-size: 16px;
  font-weight: 750;
  color: #0f172a;
  line-height: 1.4;
  margin-bottom: 8px;
}

.related-cart-item__meta {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
  min-height: 44px;
  margin-bottom: 12px;
}

.related-cart-item__bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.related-cart-item__price {
  font-size: 16px;
  font-weight: 750;
  color: #000f51;
}

.related-cart-item__btn {
  min-height: 38px;
  padding: 0 14px;
  border-radius: 12px;
  background: #000f51;
  color: #fff;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 750;
  font-size: 14px;
  transition: all 0.2s ease;
}

.related-cart-item__btn:hover {
  background: #001a72;
  color: #fff;
}

@media (max-width: 991.98px) {
  .related-cart-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767.98px) {
  .related-cart-grid {
    grid-template-columns: 1fr;
  }

  .related-cart-item__img {
    height: 220px;
  }
}
.btn-continue,
.btn-checkout,
.summary-btn {
  min-height: 50px;
  border-radius: 16px;
  padding: 0 24px;
  font-weight: 750;
  transition: all 0.2s ease;
  border: none;
  background: #000f51;
  color: #fff;
  box-shadow: 0 14px 28px rgba(0, 15, 81, 0.18);
}

.btn-continue:hover,
.btn-checkout:hover:not(:disabled),
.summary-btn:hover:not(:disabled) {
  background: #001a72;
  color: #fff;
  transform: translateY(-1px);
}

.btn-checkout:disabled,
.summary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>