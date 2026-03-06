<template>
  <div class="checkout container py-4">
    <!-- breadcrumb -->
    <nav aria-label="breadcrumb" class="mb-3">
      <ol class="breadcrumb mb-0">
        <li class="breadcrumb-item">
          <router-link to="/" class="text-muted text-decoration-none">Trang chủ</router-link>
        </li>
        <li class="breadcrumb-item active text-dark" aria-current="page">Thanh toán</li>
      </ol>
    </nav>

    <div class="row g-4">
      <!-- LEFT -->
      <div class="col-lg-7">
        <h1 class="page-title mb-4">Thanh toán</h1>

        <!-- 1. Địa chỉ giao hàng -->
        <section class="box mb-4">
          <div class="box-title">1. Địa chỉ giao hàng</div>

          <div class="row g-3">
            <div class="col-12">
              <label class="form-label">Họ và Tên <span class="req">*</span></label>
              <input v-model="form.fullName" type="text" class="form-control" placeholder="Nhập họ tên" />
            </div>

            <div class="col-12">
              <label class="form-label">Số điện thoại <span class="req">*</span></label>
              <input v-model="form.phone" type="text" class="form-control" placeholder="Nhập số điện thoại" />
            </div>

            <div class="col-12">
              <label class="form-label">Tỉnh/Thành phố</label>
              <select v-model="form.province" class="form-select">
                <option value="">Chọn tỉnh/thành phố</option>
                <option>TP.Hà Nội</option>
                <option>TP.Hồ Chí Minh</option>
              </select>
            </div>

            <div class="col-12">
              <label class="form-label">Quận/huyện <span class="req">*</span></label>
              <select v-model="form.district" class="form-select">
                <option value="">Vui lòng chọn quận/huyện</option>
                <option>Quận 1</option>
                <option>Quận 2</option>
              </select>
            </div>

            <div class="col-12">
              <label class="form-label">Địa chỉ đường <span class="req">*</span></label>
              <input v-model="form.address" type="text" class="form-control" placeholder="Số nhà, tên đường..." />
            </div>

            <div class="col-12">
              <label class="form-label">Phường/xã <span class="req">*</span></label>
              <select v-model="form.ward" class="form-select">
                <option value="">Vui lòng chọn phường/xã</option>
                <option>Phường 1</option>
                <option>Phường 2</option>
              </select>
            </div>

            <div class="col-12">
              <label class="form-label">Ghi chú</label>
              <textarea v-model="form.note" class="form-control" rows="3" placeholder="Ghi chú"></textarea>
            </div>
          </div>
        </section>

        <!-- 2. Vận chuyển -->
        <section class="box mb-4">
          <div class="box-title">2. Vận chuyển</div>

          <label class="ship-row">
            <input class="form-check-input me-2" type="radio" value="standard" v-model="form.shippingMethod" />
            <span class="ship-name">Giao hàng - Tiêu chuẩn</span>
            <span class="ship-fee">+ {{ money(shippingFee) }} đ</span>
          </label>
        </section>

        <!-- 3. Phương thức thanh toán -->
        <section class="box mb-4">
          <div class="box-title">3. Phương thức thanh toán</div>

          <label class="pay-row">
            <input class="form-check-input me-2" type="radio" value="cod" v-model="form.paymentMethod" />
            <span>Thanh toán khi nhận hàng (COD)</span>
          </label>

          <label class="pay-row">
            <input class="form-check-input me-2" type="radio" value="vnpay" v-model="form.paymentMethod" />
            <span>Thanh toán thẻ / VNPAY (ATM, VISA, Mastercard...)</span>
          </label>
        </section>

        <!-- 4. Áp dụng mã giảm giá -->
        <section class="box mb-4">
          <div class="box-title">4. Áp dụng mã giảm giá</div>

          <div class="coupon">
            <input v-model="couponCode" class="form-control" placeholder="Nhập mã giảm giá" />
            <button class="btn btn-dark coupon-btn" type="button" @click="applyCoupon">Sử dụng</button>
          </div>

          <button class="btn btn-link p-0 mt-2 coupon-toggle" type="button">
            MÃ GIẢM GIÁ <i class="bi bi-chevron-down ms-1"></i>
          </button>
        </section>

        <!-- Thành tiền -->
        <section class="box">
          <div class="total-row">
            <span class="total-label">THÀNH TIỀN</span>
            <span class="total-value">{{ money(grandTotal) }} đ</span>
          </div>

          <label class="invoice mt-3">
            <input v-model="form.invoice" type="checkbox" class="form-check-input me-2" />
            <span>Xuất hóa đơn công ty</span>
          </label>

          <button class="btn btn-order w-100 mt-3" type="button" :disabled="items.length === 0" @click="placeOrder">
            ĐẶT HÀNG
          </button>
        </section>
      </div>

      <!-- RIGHT -->
      <div class="col-lg-5">
        <div class="summary">
          <div class="summary-title">Đơn hàng</div>

          <div v-if="items.length === 0" class="summary-empty">
            Chưa có sản phẩm trong giỏ hàng.
          </div>

          <div v-else class="summary-table-wrap">
            <table class="table summary-table align-middle mb-0">
              <thead>
              <tr>
                <th>Sản phẩm</th>
                <th class="text-center">Số lượng</th>
                <th class="text-end">Giá</th>
                <th class="text-end">Tổng tiền</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="it in items" :key="it.key">
                <td>
                  <div class="prod">
                    <img :src="it.image" class="prod-img" alt="sp" @error="onImgError" />
                    <div class="prod-info">
                      <div class="prod-name">{{ it.name }}</div>
                      <div class="prod-meta">
                        <span v-if="it.color">Màu: <span class="dot" :style="{ backgroundColor: it.color }"></span></span>
                        <span v-if="it.size">Kích cỡ: {{ it.size }}</span>
                      </div>
                    </div>
                  </div>
                </td>
                <td class="text-center">{{ it.qty }}</td>
                <td class="text-end">{{ money(it.price) }} đ</td>
                <td class="text-end">{{ money(it.price * it.qty) }} đ</td>
              </tr>
              </tbody>
            </table>
          </div>

          <div class="summary-footer">
            <div class="sum-line">
              <span>Tổng sản phẩm</span>
              <span>{{ totalQty }}</span>
            </div>
            <div class="sum-line">
              <span>Tổng tiền</span>
              <span>{{ money(subtotal) }} đ</span>
            </div>
            <div class="sum-line">
              <span>Vận chuyển</span>
              <span>{{ money(shippingFee) }} đ</span>
            </div>
            <div class="sum-line total">
              <span>THÀNH TIỀN</span>
              <span>{{ money(grandTotal) }} đ</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from "vue";
import { useCart } from "../../composables/useCart";

const { items, totalQty, subtotal } = useCart();

const form = reactive({
  fullName: "",
  phone: "",
  province: "",
  district: "",
  ward: "",
  address: "",
  note: "",
  shippingMethod: "standard",
  paymentMethod: "cod",
  invoice: false,
});

const couponCode = ref("");
const discount = ref(0); // UI trước: tạm 0

const shippingFee = computed(() => 60000); // giống ảnh mẫu: 60.000
const grandTotal = computed(() => (Number(subtotal.value) || 0) + (Number(shippingFee.value) || 0) - (Number(discount.value) || 0));

function money(v) {
  const n = Number(v) || 0;
  return n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
}

function applyCoupon() {
  // UI trước: demo
  discount.value = 0;
  alert("Mã giảm giá (demo) - bạn sẽ nối API sau.");
}

function placeOrder() {
  alert("Đặt hàng (UI demo) - bạn sẽ nối API checkout sau.");
}

function onImgError(e) {
  e.target.src =
      "data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='120' height='120'%3E%3Crect width='100%25' height='100%25' fill='%23f1f3f5'/%3E%3Ctext x='50%25' y='52%25' dominant-baseline='middle' text-anchor='middle' fill='%2399a1aa' font-size='14'%3E%E1%BA%A2nh%3C/text%3E%3C/svg%3E";
}
</script>

<style scoped>
/* Tone giống site */
.checkout {
  --navy: #000f51;
  --muted: #6c757d;
}

/* font-weight không vượt 750 */
.page-title {
  font-size: 26px;
  font-weight: 700;
  color: #111;
}

.box {
  border-top: 1px solid rgba(0,0,0,0.1);
  padding-top: 18px;
}

.box-title {
  font-size: 14px;
  font-weight: 700;
  color: #111;
  margin-bottom: 12px;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  color: #111;
}

.req { color: #d11; }

.form-control, .form-select {
  border-radius: 2px;
  background: #f3f5f7;
  border: 1px solid rgba(0,0,0,0.08);
  font-size: 13.5px;
}

.ship-row, .pay-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 0;
  font-size: 13.5px;
  color: #111;
  margin: 0;
}

.ship-name { font-weight: 600; }
.ship-fee { color: var(--muted); }

.coupon {
  display: grid;
  grid-template-columns: 1fr 110px;
  gap: 10px;
  align-items: center;
}

.coupon-btn {
  height: 38px;
  border-radius: 2px;
  font-weight: 700;
}

.coupon-toggle {
  font-size: 13px;
  font-weight: 700;
  color: #b28a2a; /* hơi vàng như OWEN */
  text-decoration: none;
}

.total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 8px;
}

.total-label {
  font-size: 13px;
  font-weight: 700;
  color: #111;
}

.total-value {
  font-size: 14px;
  font-weight: 700;
  color: #111;
}

.invoice {
  font-size: 13.5px;
  font-weight: 600;
  color: #111;
}

.btn-order {
  background: #cfcfcf;
  border: none;
  border-radius: 2px;
  height: 46px;
  font-weight: 700;
  color: #111;
}

.btn-order:disabled { opacity: 0.6; }

/* Right summary */
.summary {
  border-left: 1px solid rgba(0,0,0,0.12);
  padding-left: 18px;
}

.summary-title {
  font-size: 14px;
  font-weight: 700;
  color: #111;
  margin-top: 6px;
  margin-bottom: 10px;
}

.summary-empty {
  font-size: 13.5px;
  color: var(--muted);
  padding: 12px 0;
}

.summary-table th {
  font-size: 12.5px;
  font-weight: 700;
  color: #111;
  border-bottom: 1px solid rgba(0,0,0,0.12);
}

.summary-table td {
  font-size: 13px;
  color: #111;
  border-bottom: 1px solid rgba(0,0,0,0.08);
  vertical-align: top;
  padding-top: 12px;
  padding-bottom: 12px;
}

.prod {
  display: grid;
  grid-template-columns: 44px 1fr;
  gap: 10px;
  align-items: start;
}

.prod-img {
  width: 44px;
  height: 58px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid rgba(0,0,0,0.08);
  background: #f1f3f5;
}

.prod-name {
  font-weight: 700;
  font-size: 13px;
  line-height: 1.2;
}

.prod-meta {
  margin-top: 4px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  font-size: 12px;
  color: var(--muted);
}

.dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 1px solid rgba(0,0,0,0.2);
  vertical-align: middle;
  margin-left: 4px;
}

.summary-footer {
  padding-top: 12px;
  margin-top: 12px;
  border-top: 1px solid rgba(0,0,0,0.12);
}

.sum-line {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #111;
  padding: 6px 0;
}

.sum-line.total {
  font-weight: 700;
  padding-top: 10px;
}
</style>