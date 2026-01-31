<template>
  <div class="sales-page">
    <!-- Header -->
    <div class="sales-top">
      <h4 class="title">Bán hàng</h4>

      <button
        class="btn btn-create"
        type="button"
        @click="createOrder"
        :disabled="orders.length >= MAX_ORDERS"
        :title="orders.length >= MAX_ORDERS ? `Chỉ tạo tối đa ${MAX_ORDERS} đơn` : 'Tạo đơn hàng'"
      >
        + Tạo đơn hàng
      </button>
    </div>

    <!-- Main box -->
    <div class="sales-box">
      <!-- Tabs -->
      <div class="order-tabs">
        <div class="tabs-left">
          <button
            v-for="o in orders"
            :key="o.id"
            type="button"
            class="tab"
            :class="{ active: o.id === activeId }"
            @click="activeId = o.id"
          >
            {{ o.label }}
            <span class="tab-x" title="Đóng" @click.stop="closeOrder(o.id)">
              ×
            </span>
          </button>
        </div>

        <div class="tabs-right">
          <div class="hint">
            {{ orders.length }}/{{ MAX_ORDERS }} đơn
          </div>
        </div>
      </div>

      <!-- BODY -->
      <div v-if="!activeOrder" class="empty-root">
        <div class="empty-icon">👜</div>
        <div class="empty-text">No Data Found</div>
      </div>

      <div v-else class="sales-body">
        <!-- Row: Products section header + actions -->
        <div class="products-head">
          <div class="section-title">Sản phẩm</div>

          <div class="actions">
            <button class="btn btn-outline" type="button" @click="openQr">
              QUÉT QR SẢN PHẨM
            </button>
            <button class="btn btn-create" type="button" @click="openProductModal">
              THÊM SẢN PHẨM
            </button>
          </div>
        </div>

        <!-- Products content -->
        <div class="products-content">
          <!-- empty cart -->
          <div v-if="activeOrder.cart.length === 0" class="empty-cart">
            <div class="empty-cart-icon">👜</div>
            <div class="empty-cart-text">No Data Found</div>
          </div>

          <!-- cart list -->
          <div v-else class="cart-list">
            <div v-for="(it, idx) in activeOrder.cart" :key="it.key" class="cart-row">
              <input type="checkbox" class="form-check-input mt-1" />

              <img class="thumb" :src="it.image" alt="" />

              <div class="info">
                <div class="name">{{ it.name }}</div>
                <div class="meta text-muted">{{ it.meta }}</div>
              </div>

              <div class="price">
                <div class="p1">{{ money(it.price) }}</div>
                <div class="p2">{{ money(it.price) }}</div>
              </div>

              <div class="qty">
                <button class="qty-btn" @click="decQty(idx)">-</button>
                <div class="qty-val">{{ it.qty }}</div>
                <button class="qty-btn" @click="incQty(idx)">+</button>
              </div>

              <button class="trash" title="Xóa" @click="removeItem(idx)">🗑</button>
            </div>

            <div class="cart-total">
              <div class="label">Tổng tiền</div>
              <div class="value">{{ money(subTotal) }}</div>
            </div>
          </div>
        </div>

        <!-- Bottom 2 columns like screenshot -->
        <div class="bottom-grid">
          <!-- Customer -->
          <div class="card">
            <div class="card-head">
              <div class="card-title">Thông tin khách hàng</div>
              <button class="btn btn-blue" type="button" @click="openCustomerModal">
                Chọn khách hàng
              </button>
            </div>

            <div class="card-body">
              <div class="small-row">
                <span class="muted">Tên khách hàng:</span>
                <b>{{ activeOrder.customer?.name || "Khách lẻ" }}</b>
              </div>

              <!-- demo address fields like screenshot -->
              <div class="form-grid">
                <input class="in" placeholder="Số điện thoại" v-model="activeOrder.customerDraft.phone" />
                <input class="in" placeholder="Email (nếu có)" v-model="activeOrder.customerDraft.email" />
                <select class="in" v-model="activeOrder.customerDraft.province">
                  <option value="">-- Chọn Tỉnh/Thành --</option>
                  <option>Hà Nội</option>
                  <option>TP.HCM</option>
                </select>
                <select class="in" v-model="activeOrder.customerDraft.district">
                  <option value="">-- Chọn Quận/Huyện --</option>
                  <option>Quận 1</option>
                  <option>Quận 7</option>
                </select>
                <select class="in" v-model="activeOrder.customerDraft.ward">
                  <option value="">-- Chọn Xã/Phường --</option>
                  <option>Phường 1</option>
                  <option>Phường 2</option>
                </select>
                <input class="in" placeholder="Địa chỉ chi tiết" v-model="activeOrder.customerDraft.address" />
              </div>

              <div class="ghn-box">
                <div class="ghn">GHN <span>Express</span></div>
              </div>
            </div>
          </div>

          <!-- Payment -->
          <div class="card">
            <div class="card-head">
              <div class="card-title">Thông tin thanh toán</div>

              <div class="ship-toggle">
                <span class="muted">Giao hàng</span>
                <label class="switch">
                  <input type="checkbox" v-model="activeOrder.shipping" />
                  <span class="slider"></span>
                </label>
              </div>
            </div>

            <div class="card-body">
              <div class="row2">
                <input class="in" placeholder="Mã phiếu giảm giá" v-model.trim="activeOrder.voucherCode" />
                <input class="in" placeholder="%" v-model.number="activeOrder.discountPercent" inputmode="numeric" />
              </div>

              <div v-if="activeOrder.discountPercent > 0" class="promo">
                Áp dụng thành công phiếu giảm giá <b>{{ activeOrder.discountPercent }}%</b> cho đơn hàng đầu tiên
              </div>

              <div class="sum">
                <div class="sum-row">
                  <span class="muted">Tiền hàng</span>
                  <span>{{ money(subTotal) }}</span>
                </div>

                <div class="sum-row">
                  <span class="muted">Giảm giá</span>
                  <span class="danger">- {{ money(discountMoney) }}</span>
                </div>

                <div v-if="activeOrder.shipping" class="sum-row">
                  <span class="muted">Phí vận chuyển</span>
                  <span>{{ money(activeOrder.shippingFee) }}</span>
                </div>

                <div class="sum-row total">
                  <span>Tổng phải trả</span>
                  <span class="danger">{{ money(grandTotal) }}</span>
                </div>

                <div class="sum-row">
                  <span class="muted">Khách thanh toán</span>
                  <input class="in pay" v-model.number="activeOrder.paid" inputmode="numeric" />
                </div>

                <div class="sum-row">
                  <span class="muted">Tiền thừa</span>
                  <span>{{ money(changeMoney) }}</span>
                </div>
              </div>

              <button class="btn btn-confirm" :disabled="activeOrder.cart.length === 0">
                XÁC NHẬN ĐẶT HÀNG
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- toast -->
    <div v-if="toast.show" class="toastx" :class="toast.type">
      {{ toast.msg }}
    </div>

    <!-- Product modal (simple) -->
    <div v-if="showProductModal" class="modalx">
      <div class="modalx-card">
        <div class="modalx-head">
          <div class="modalx-title">Chọn biến thể để thêm vào đơn</div>
          <button class="modalx-x" @click="showProductModal = false">×</button>
        </div>

        <div class="modalx-body">
          <div class="modalx-filters">
            <input class="in" placeholder="Tìm kiếm..." v-model.trim="productKw" />
            <button class="btn btn-light" @click="productKw = ''">Đặt lại</button>
          </div>

          <div class="table-wrap">
            <table class="tablex">
              <thead>
                <tr>
                  <th style="width:60px">STT</th>
                  <th style="width:110px">Mã</th>
                  <th style="width:80px">Ảnh</th>
                  <th>Tên sản phẩm</th>
                  <th style="width:120px">Màu sắc</th>
                  <th style="width:130px">Kích cỡ</th>
                  <th style="width:110px">Số lượng</th>
                  <th style="width:120px">Giá bán</th>
                  <th style="width:110px">Hành động</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(p, i) in filteredProducts" :key="p.code">
                  <td>{{ i + 1 }}</td>
                  <td>{{ p.code }}</td>
                  <td><img :src="p.image" class="thumb-sm" /></td>
                  <td>{{ p.name }}</td>
                  <td>{{ p.color }}</td>
                  <td>{{ p.size }}</td>
                  <td>{{ p.stock }}</td>
                  <td class="text-end">{{ money(p.price) }}</td>
                  <td class="text-center">
                    <button class="btn btn-choose" @click="chooseProduct(p)">Chọn</button>
                  </td>
                </tr>

                <tr v-if="filteredProducts.length === 0">
                  <td colspan="9" class="text-center muted" style="padding:14px">Không có dữ liệu</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="modalx-foot">
            <button class="btn btn-light" @click="showProductModal = false">Đóng</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Customer modal (simple) -->
    <div v-if="showCustomerModal" class="modalx">
      <div class="modalx-card">
        <div class="modalx-head">
          <div class="modalx-title">Chọn khách hàng</div>
          <button class="modalx-x" @click="showCustomerModal = false">×</button>
        </div>

        <div class="modalx-body">
          <div class="modalx-filters">
            <input class="in" placeholder="Tìm theo tên/SĐT/địa chỉ..." v-model.trim="customerKw" />
            <button class="btn btn-light" @click="customerKw = ''">Tải lại</button>
          </div>

          <div class="table-wrap">
            <table class="tablex">
              <thead>
                <tr>
                  <th style="width:60px">#</th>
                  <th style="width:200px">Tên khách</th>
                  <th style="width:140px">Số điện thoại</th>
                  <th>Địa chỉ</th>
                  <th style="width:110px">Hành động</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(c, i) in filteredCustomers" :key="c.phone">
                  <td>{{ i + 1 }}</td>
                  <td>{{ c.name }}</td>
                  <td>{{ c.phone }}</td>
                  <td>{{ c.address }}</td>
                  <td class="text-center">
                    <button class="btn btn-choose" @click="chooseCustomer(c)">Chọn</button>
                  </td>
                </tr>

                <tr v-if="filteredCustomers.length === 0">
                  <td colspan="5" class="text-center muted" style="padding:14px">Không có dữ liệu</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="modalx-foot">
            <button class="btn btn-light" @click="showCustomerModal = false">Đóng</button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { computed, ref } from "vue";

/** giới hạn tối đa 10 hóa đơn */
const MAX_ORDERS = 10;

const orders = ref([]);
const activeId = ref(null);
const orderSeq = ref(1);

const toast = ref({ show: false, msg: "", type: "danger" });
function showToast(msg, type = "danger") {
  toast.value = { show: true, msg, type };
  window.clearTimeout(showToast._t);
  showToast._t = window.setTimeout(() => (toast.value.show = false), 2200);
}

const activeOrder = computed(() => orders.value.find(o => o.id === activeId.value) || null);

function createOrder() {
  if (orders.value.length >= MAX_ORDERS) {
    showToast(`Chỉ được tạo tối đa ${MAX_ORDERS} hóa đơn. Vui lòng đóng bớt đơn.`, "danger");
    return;
  }

  const id = Date.now() + Math.random();
  const hd = Math.floor(100 + Math.random() * 900); // 3 số
  const label = `Đơn hàng ${orderSeq.value} - HD-${hd}`;

  orders.value.push({
    id,
    label,
    cart: [],
    customer: null,
    customerDraft: { phone: "", email: "", province: "", district: "", ward: "", address: "" },
    shipping: false,
    shippingFee: 0,
    voucherCode: "",
    discountPercent: 0,
    paid: 0,
  });

  activeId.value = id;
  orderSeq.value++;
}

function closeOrder(id) {
  const idx = orders.value.findIndex(x => x.id === id);
  if (idx === -1) return;
  orders.value.splice(idx, 1);

  if (activeId.value === id) {
    activeId.value = orders.value[0]?.id ?? null;
  }
}

/** cart actions */
function incQty(i) {
  activeOrder.value.cart[i].qty += 1;
}
function decQty(i) {
  activeOrder.value.cart[i].qty = Math.max(1, activeOrder.value.cart[i].qty - 1);
}
function removeItem(i) {
  activeOrder.value.cart.splice(i, 1);
}

/** money */
function money(n) {
  const v = Number(n) || 0;
  return v.toLocaleString("vi-VN") + " đ";
}

/** totals */
const subTotal = computed(() => {
  if (!activeOrder.value) return 0;
  return activeOrder.value.cart.reduce((s, it) => s + it.price * it.qty, 0);
});

const discountMoney = computed(() => {
  if (!activeOrder.value) return 0;
  const p = Math.min(100, Math.max(0, Number(activeOrder.value.discountPercent) || 0));
  return Math.floor((subTotal.value * p) / 100);
});

const grandTotal = computed(() => {
  if (!activeOrder.value) return 0;
  const ship = activeOrder.value.shipping ? (Number(activeOrder.value.shippingFee) || 0) : 0;
  return Math.max(0, subTotal.value - discountMoney.value + ship);
});

const changeMoney = computed(() => {
  if (!activeOrder.value) return 0;
  const paid = Number(activeOrder.value.paid) || 0;
  return Math.max(0, paid - grandTotal.value);
});

/** buttons */
function openQr() {
  showToast("Demo: Chưa tích hợp quét QR trong zip của bạn.", "info");
}

/** product modal */
const showProductModal = ref(false);
const productKw = ref("");
const demoProducts = ref([
  {
    code: "CTSP310",
    name: "Balo FPT",
    color: "Trắng",
    size: "Nhỏ (20x30x10 cm)",
    stock: 10,
    price: 1000000,
    image:
      "https://via.placeholder.com/56x56.png?text=IMG",
    meta: "size Nhỏ / Trắng",
  },
  {
    code: "CTSP309",
    name: "Balo FPT",
    color: "Xanh dương",
    size: "Lớn (50x35x20 cm)",
    stock: 10,
    price: 1000000,
    image:
      "https://via.placeholder.com/56x56.png?text=IMG",
    meta: "size Lớn / Xanh dương",
  },
  {
    code: "CTSP307",
    name: "Balo Cute",
    color: "Trắng",
    size: "Trung bình (40x30x15 cm)",
    stock: 98,
    price: 5000000,
    image:
      "https://via.placeholder.com/56x56.png?text=IMG",
    meta: "size Trung bình / Trắng",
  },
]);

const filteredProducts = computed(() => {
  const kw = productKw.value.trim().toLowerCase();
  if (!kw) return demoProducts.value;
  return demoProducts.value.filter(p =>
    [p.code, p.name, p.color, p.size].some(x => String(x).toLowerCase().includes(kw))
  );
});

function openProductModal() {
  if (!activeOrder.value) return;
  showProductModal.value = true;
}

function chooseProduct(p) {
  const o = activeOrder.value;
  const existed = o.cart.find(x => x.code === p.code);
  if (existed) {
    existed.qty += 1;
  } else {
    o.cart.push({
      key: p.code + "-" + Date.now(),
      code: p.code,
      name: p.name,
      meta: p.meta,
      image: p.image,
      price: p.price,
      qty: 1,
    });
  }
  showProductModal.value = false;
}

/** customer modal */
const showCustomerModal = ref(false);
const customerKw = ref("");
const demoCustomers = ref([
  { name: "Nguyễn Văn A", phone: "0123456789", address: "125, Phường Tăng Nhơn Phú A, TP Thủ Đức" },
  { name: "Trần Thị B", phone: "0987654321", address: "234, Tân Phú, TP.HCM" },
  { name: "Phạm Văn C", phone: "0909123123", address: "789, Bình Thạnh, TP.HCM" },
]);

const filteredCustomers = computed(() => {
  const kw = customerKw.value.trim().toLowerCase();
  if (!kw) return demoCustomers.value;
  return demoCustomers.value.filter(c =>
    [c.name, c.phone, c.address].some(x => String(x).toLowerCase().includes(kw))
  );
});

function openCustomerModal() {
  if (!activeOrder.value) return;
  showCustomerModal.value = true;
}

function chooseCustomer(c) {
  activeOrder.value.customer = { ...c };
  showCustomerModal.value = false;
}
</script>

<style scoped>
/* layout */
.sales-page{ padding: 12px 14px; }
.sales-top{
  display:flex; align-items:center; justify-content:space-between;
  margin-bottom: 10px;
}
.title{ margin:0; font-weight:700; }

/* main box */
.sales-box{
  background:#fff;
  border:1px solid #e9ecef;
  border-radius:10px;
  overflow:hidden;
}

/* tabs */
.order-tabs{
  display:flex; align-items:center; justify-content:space-between;
  padding:10px 12px;
  border-bottom:1px solid #f1f3f5;
}
.tabs-left{ display:flex; gap:8px; flex-wrap:wrap; }
.tab{
  border:1px solid #f0c6a8;
  background:#fff;
  color:#d9480f;
  border-radius:6px;
  padding:4px 10px;
  font-size:12px;
  display:flex; align-items:center; gap:8px;
}
.tab.active{
  background:#fff7ed;
  border-color:#fb923c;
}
.tab-x{
  width:18px; height:18px;
  display:grid; place-items:center;
  border-radius:999px;
  line-height:18px;
  color:#6b7280;
}
.tab-x:hover{ background:rgba(0,0,0,.06); }

.hint{ font-size:12px; color:#6b7280; }

/* header + buttons */
.btn{
  border:1px solid transparent;
  border-radius:6px;
  padding:7px 12px;
  font-weight:600;
  cursor:pointer;
}
.btn:disabled{ opacity:.6; cursor:not-allowed; }
.btn-create{
  background:#f97316;
  color:#fff;
  border-color:#f97316;
}
.btn-create:hover{ background:#ea580c; border-color:#ea580c; }
.btn-outline{
  background:#fff;
  border-color:#f1c7a7;
  color:#d9480f;
}
.btn-outline:hover{ background:#fff7ed; }
.btn-blue{
  background:#3b82f6;
  color:#fff;
  border-color:#3b82f6;
}
.btn-blue:hover{ background:#2563eb; border-color:#2563eb; }
.btn-confirm{
  width:100%;
  background:#f97316;
  color:#fff;
  border-color:#f97316;
  margin-top:10px;
  padding:10px 12px;
}
.btn-confirm:disabled{ background:#fed7aa; border-color:#fed7aa; }

/* body */
.sales-body{ padding:12px; }
.products-head{
  display:flex; align-items:center; justify-content:space-between;
  margin-bottom:8px;
}
.section-title{
  font-size:13px; font-weight:700;
  color:#374151;
  padding-bottom:8px;
  border-bottom:2px solid #f97316;
  width:90px;
}
.actions{ display:flex; gap:10px; }

/* products content */
.products-content{
  border:1px solid #e9ecef;
  border-radius:10px;
  min-height:180px;
  padding:10px;
}
.empty-cart{
  height:160px;
  display:grid; place-items:center;
  color:#6b7280;
}
.empty-cart-icon{ font-size:22px; opacity:.7; }
.empty-cart-text{ font-size:12px; }

.cart-list{ display:flex; flex-direction:column; gap:10px; }
.cart-row{
  display:grid;
  grid-template-columns: 24px 56px 1fr 160px 130px 40px;
  gap:12px;
  align-items:center;
  padding:10px;
  border-bottom:1px solid #f1f3f5;
}
.thumb{ width:56px; height:56px; border-radius:8px; border:1px solid #e5e7eb; object-fit:cover; }
.info .name{ font-weight:700; font-size:13px; }
.info .meta{ font-size:12px; }
.price{ text-align:right; }
.price .p1{ font-size:12px; color:#9ca3af; text-decoration:line-through; }
.price .p2{ font-weight:800; color:#ef4444; }
.qty{ display:flex; align-items:center; justify-content:flex-end; gap:8px; }
.qty-btn{
  width:28px; height:28px;
  border:1px solid #e5e7eb;
  background:#fff;
  border-radius:6px;
}
.qty-val{ min-width:20px; text-align:center; font-weight:700; }
.trash{
  width:32px; height:32px;
  border:none;
  background:#ef4444;
  color:#fff;
  border-radius:6px;
}

/* total */
.cart-total{
  display:flex; justify-content:flex-end; gap:14px;
  padding:10px 4px 0;
}
.cart-total .label{ color:#6b7280; }
.cart-total .value{ font-weight:800; color:#ef4444; }

/* bottom grid */
.bottom-grid{
  display:grid;
  grid-template-columns: 1fr 1fr;
  gap:14px;
  margin-top:12px;
}
.card{
  border:1px solid #e9ecef;
  border-radius:10px;
  overflow:hidden;
  background:#fff;
}
.card-head{
  display:flex; align-items:center; justify-content:space-between;
  padding:10px 12px;
  border-bottom:1px solid #f1f3f5;
}
.card-title{ font-weight:800; font-size:13px; }
.card-body{ padding:12px; }

.small-row{ font-size:12px; margin-bottom:10px; }
.muted{ color:#6b7280; }

.form-grid{
  display:grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap:8px;
  margin-top:8px;
}
.in{
  width:100%;
  border:1px solid #e5e7eb;
  border-radius:6px;
  padding:8px 10px;
  font-size:12px;
  outline:none;
}
.in:focus{ border-color:#fb923c; box-shadow:0 0 0 3px rgba(251,146,60,.15); }

.ghn-box{
  margin-top:12px;
  border:1px dashed #e5e7eb;
  border-radius:10px;
  height:150px;
  display:grid; place-items:center;
}
.ghn{
  font-weight:900;
  font-size:34px;
  color:#f97316;
  letter-spacing:.5px;
}
.ghn span{ font-size:16px; font-weight:800; color:#111827; }

/* payment */
.ship-toggle{ display:flex; align-items:center; gap:10px; font-size:12px; }
.switch{ position:relative; display:inline-block; width:38px; height:20px; }
.switch input{ opacity:0; width:0; height:0; }
.slider{
  position:absolute; cursor:pointer; inset:0;
  background:#e5e7eb; border-radius:999px;
  transition:.2s;
}
.slider:before{
  content:""; position:absolute; height:16px; width:16px;
  left:2px; top:2px; background:white; border-radius:999px;
  transition:.2s;
}
.switch input:checked + .slider{ background:#f97316; }
.switch input:checked + .slider:before{ transform:translateX(18px); }

.row2{ display:grid; grid-template-columns: 1fr 80px; gap:8px; }
.promo{
  background:#eaffea;
  border:1px solid #b7f7b7;
  color:#166534;
  padding:8px 10px;
  border-radius:8px;
  font-size:12px;
  margin-top:8px;
}
.sum{ margin-top:10px; display:flex; flex-direction:column; gap:8px; }
.sum-row{ display:flex; justify-content:space-between; align-items:center; font-size:12px; }
.sum-row.total{ font-weight:900; font-size:13px; padding-top:6px; border-top:1px dashed #e5e7eb; }
.danger{ color:#ef4444; font-weight:800; }
.pay{ max-width:160px; text-align:right; }

/* empty root */
.empty-root{
  height:240px;
  display:grid;
  place-items:center;
  color:#6b7280;
}
.empty-icon{ font-size:28px; opacity:.7; }
.empty-text{ font-size:12px; }

/* toast */
.toastx{
  position:fixed;
  right:14px;
  bottom:14px;
  padding:10px 12px;
  border-radius:10px;
  border:1px solid #e5e7eb;
  background:#fff;
  box-shadow:0 6px 20px rgba(0,0,0,.10);
  font-size:13px;
}
.toastx.danger{ border-color:#fecaca; }
.toastx.info{ border-color:#bfdbfe; }

/* modal */
.modalx{
  position:fixed; inset:0;
  background:rgba(0,0,0,.35);
  display:flex; align-items:center; justify-content:center;
  z-index:9999;
}
.modalx-card{
  width:min(980px, 92vw);
  background:#fff;
  border-radius:12px;
  overflow:hidden;
  border:1px solid #e5e7eb;
}
.modalx-head{
  display:flex; align-items:center; justify-content:space-between;
  padding:10px 12px;
  border-bottom:1px solid #f1f3f5;
}
.modalx-title{ font-weight:900; }
.modalx-x{
  width:34px; height:34px;
  border:none; background:#f3f4f6;
  border-radius:10px;
  font-size:18px;
}
.modalx-body{ padding:12px; }
.modalx-filters{
  display:flex; gap:10px; align-items:center;
  margin-bottom:10px;
}
.btn-light{
  background:#f3f4f6;
  border-color:#e5e7eb;
  color:#111827;
}
.table-wrap{ max-height:420px; overflow:auto; border:1px solid #e5e7eb; border-radius:10px; }
.tablex{ width:100%; border-collapse:collapse; font-size:12px; }
.tablex th, .tablex td{ padding:10px; border-bottom:1px solid #f1f3f5; vertical-align:middle; }
.tablex thead th{ position:sticky; top:0; background:#f8fafc; z-index:1; }
.thumb-sm{ width:44px; height:44px; border-radius:8px; border:1px solid #e5e7eb; object-fit:cover; }
.btn-choose{
  background:#10b981; border-color:#10b981; color:#fff;
  padding:6px 10px; border-radius:8px; font-weight:800;
}
.modalx-foot{ display:flex; justify-content:flex-end; margin-top:10px; }

/* responsive */
@media (max-width: 1024px){
  .bottom-grid{ grid-template-columns: 1fr; }
  .cart-row{ grid-template-columns: 24px 56px 1fr 140px 110px 40px; }
  .form-grid{ grid-template-columns: 1fr 1fr; }
}
</style>
