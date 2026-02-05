<template>
  <div class="container-fluid py-3">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-3">
      <h4 class="m-0 fw-bold">Bán hàng</h4>

      <button
        class="btn btn-outline-secondary btn-sm"
        type="button"
        @click="createOrder"
        :disabled="orders.length >= MAX_ORDERS"
        :title="
          orders.length >= MAX_ORDERS
            ? `Chỉ tạo tối đa ${MAX_ORDERS} đơn`
            : 'Tạo đơn hàng'
        "
      >
        + Tạo đơn hàng
      </button>
    </div>

    <div class="card shadow-sm">
      <!-- Tabs -->
      <div class="order-tabs">
        <div class="tabs-left nav nav-pills flex-wrap gap-2">
          <button
            v-for="o in orders"
            :key="o.id"
            type="button"
            class="nav-link order-tab"
            :class="{ active: o.id === activeId }"
            @click="activeId = o.id"
          >
            {{ o.label }}
            <span class="tab-x ms-2" title="Đóng" @click.stop="closeOrder(o.id)"
              >×</span
            >
          </button>
        </div>

        <div class="tabs-right">
          <div class="hint">{{ orders.length }}/{{ MAX_ORDERS }} đơn</div>
        </div>
      </div>

      <!-- Body -->
      <div class="card-body">
        <div v-if="!activeOrder" class="text-center py-5 text-muted">
          <div class="fs-1">👜</div>
          <div class="fw-semibold">No Data Found</div>
        </div>

        <div v-else>
          <!-- Products -->
          <div
            class="d-flex align-items-center justify-content-between flex-wrap gap-2 mb-2"
          >
            <div class="fw-bold">Sản phẩm</div>
            <div class="d-flex gap-2">
              <button
                class="btn btn-outline-secondary btn-sm"
                type="button"
                @click="openQr"
              >
                <i class="bi bi-qr-code-scan me-1"></i> Quét QR sản phẩm
              </button>
              <button
                class="btn btn-outline-secondary btn-sm"
                type="button"
                @click="openProductModal"
              >
                <i class="bi bi-plus-lg me-2"></i> Thêm sản phẩm
              </button>
            </div>
          </div>

          <div class="border rounded-3 p-2 mb-3">
            <div
              v-if="activeOrder.cart.length === 0"
              class="text-center py-4 text-muted"
            >
              <div class="fs-1">👜</div>
              <div class="fw-semibold">No Data Found</div>
            </div>

            <div v-else class="table-responsive">
              <table class="table align-middle mb-0">
                <thead class="table-light">
                  <tr>
                    <th style="width: 70px">Ảnh</th>
                    <th>Sản phẩm</th>
                    <th class="text-end" style="width: 140px">Đơn giá</th>
                    <th class="text-center" style="width: 170px">Số lượng</th>
                    <th class="text-end" style="width: 160px">Thành tiền</th>
                    <th class="text-center" style="width: 80px">Xóa</th>
                  </tr>
                </thead>

                <tbody>
                  <tr v-for="(it, idx) in activeOrder.cart" :key="it.key">
                    <td>
                      <img
                        :src="it.image"
                        class="rounded"
                        style="width: 56px; height: 56px; object-fit: cover"
                      />
                    </td>

                    <td>
                      <div class="fw-semibold">{{ it.name }}</div>
                      <div class="text-muted small">{{ it.meta }}</div>
                      <div class="text-muted small">
                        CTSP: <span class="fw-semibold">{{ it.code }}</span> •
                        Tồn: <span class="fw-semibold">{{ it.stock }}</span>
                      </div>
                    </td>

                    <td class="text-end fw-semibold">{{ money(it.price) }}</td>

                    <td class="text-center">
                      <div class="btn-group" role="group">
                        <button
                          class="btn btn-outline-secondary btn-sm"
                          @click="decQty(idx)"
                        >
                          -
                        </button>
                        <button
                          class="btn btn-outline-secondary btn-sm"
                          disabled
                          style="min-width: 46px"
                        >
                          {{ it.qty }}
                        </button>
                        <button
                          class="btn btn-outline-secondary btn-sm"
                          @click="incQty(idx)"
                        >
                          +
                        </button>
                      </div>
                    </td>

                    <td class="text-end fw-bold">
                      {{ money(it.price * it.qty) }}
                    </td>

                    <td class="text-center">
                      <button
                        class="btn btn-outline-danger btn-sm"
                        title="Xóa"
                        @click="removeItem(idx)"
                      >
                        🗑
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>

              <div class="d-flex justify-content-between border-top pt-2 mt-2">
                <div class="fw-semibold">Tổng tiền</div>
                <div class="fw-bold">{{ money(subTotal) }}</div>
              </div>
            </div>
          </div>

          <!-- Bottom grid -->
          <div class="row g-3">
            <!-- Customer -->
            <div class="col-12 col-lg-6">
              <div class="card h-100">
                <div
                  class="card-header bg-white d-flex align-items-center justify-content-between"
                >
                  <div>
                    <div class="fw-bold">Thông tin khách hàng</div>
                    <div class="text-muted small">
                      Mã HĐ:
                      <span class="fw-semibold font-monospace">{{
                        activeOrder.maHoaDon
                      }}</span>
                    </div>
                  </div>
                  <button
                    class="btn btn-customer"
                    type="button"
                    @click="openCustomerModal"
                  >
                    Chọn khách hàng
                  </button>
                </div>

                <div class="card-body">
                  <div class="mb-2">
                    <span class="text-muted">Tên khách hàng: </span>
                    <span class="fw-bold">{{
                      activeOrder.customer?.name || "Khách lẻ"
                    }}</span>
                  </div>

                  <div class="row g-2">
                    <div class="col-12 col-md-6">
                      <input
                        class="form-control"
                        placeholder="Số điện thoại"
                        v-model="activeOrder.customerDraft.phone"
                      />
                    </div>
                    <div class="col-12 col-md-6">
                      <input
                        class="form-control"
                        placeholder="Email (nếu có)"
                        v-model="activeOrder.customerDraft.email"
                      />
                    </div>

                    <div class="col-12 col-md-6">
                      <select
                        class="form-select"
                        v-model="activeOrder.customerDraft.province"
                      >
                        <option value="">-- Chọn Tỉnh/Thành --</option>
                        <option>Hà Nội</option>
                        <option>TP.HCM</option>
                      </select>
                    </div>
                    <div class="col-12 col-md-6">
                      <select
                        class="form-select"
                        v-model="activeOrder.customerDraft.district"
                      >
                        <option value="">-- Chọn Quận/Huyện --</option>
                        <option>Quận 1</option>
                        <option>Quận 7</option>
                      </select>
                    </div>

                    <div class="col-12 col-md-6">
                      <select
                        class="form-select"
                        v-model="activeOrder.customerDraft.ward"
                      >
                        <option value="">-- Chọn Xã/Phường --</option>
                        <option>Phường 1</option>
                        <option>Phường 2</option>
                      </select>
                    </div>
                    <div class="col-12 col-md-6">
                      <input
                        class="form-control"
                        placeholder="Địa chỉ chi tiết"
                        v-model="activeOrder.customerDraft.address"
                      />
                    </div>

                    <div class="col-12">
                      <textarea
                        class="form-control"
                        rows="2"
                        placeholder="Ghi chú đơn hàng (tùy chọn)..."
                        v-model="activeOrder.note"
                      ></textarea>
                    </div>
                  </div>

                  <div class="alert alert-light border mt-3 mb-0">
                    <span class="fw-bold">GHN</span>
                    <span class="text-primary fw-bold">Express</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Payment -->
            <div class="col-12 col-lg-6">
              <div class="card h-100">
                <div
                  class="card-header bg-white d-flex align-items-center justify-content-between"
                >
                  <div class="fw-bold">Thông tin thanh toán</div>

                  <div class="form-check form-switch m-0">
                    <input
                      class="form-check-input"
                      type="checkbox"
                      role="switch"
                      v-model="activeOrder.shipping"
                    />
                    <label class="form-check-label text-muted">Giao hàng</label>
                  </div>
                </div>

                <div class="card-body">
                  <!-- PGG: input + combobox cạnh nhau -->
                  <div class="mb-2">
                    <label class="form-label mb-1">Phiếu giảm giá (PGG)</label>
                    <div class="input-group">
                      <input
                        class="form-control"
                        placeholder="Nhập mã PGG"
                        v-model.trim="activeOrder.voucherCode"
                        @keyup.enter="applyPggByCode"
                        @blur="applyPggByCode"
                      />
                      <select
                        class="form-select"
                        v-model="activeOrder.pggId"
                        @change="syncVoucherFromSelect"
                        style="max-width: 220px"
                      >
                        <option :value="null">-- Chọn PGG --</option>
                        <option
                          v-for="p in availablePGGs"
                          :key="p.id"
                          :value="p.id"
                        >
                          {{ p.ten_giam_gia }} ({{ p.ma_giam_gia }})
                        </option>
                      </select>
                      <button
                        class="btn btn-outline-secondary"
                        type="button"
                        @click="clearVoucher"
                      >
                        Xóa
                      </button>
                    </div>
                    <div v-if="selectedPGG" class="small mt-1">
                      Đang áp dụng
                      <span class="fw-bold">{{
                        selectedPGG.ten_giam_gia
                      }}</span>
                      ({{ selectedPGG.ma_giam_gia }}) —
                      <span class="text-muted">{{ pggDesc(selectedPGG) }}</span>
                    </div>
                  </div>

                  <!-- % thủ công -->
                  <div class="mb-3">
                    <label class="form-label mb-1">Giảm giá thủ công (%)</label>
                    <input
                      class="form-control"
                      inputmode="numeric"
                      v-model.number="activeOrder.discountPercent"
                      :disabled="!!activeOrder.pggId"
                      :placeholder="
                        activeOrder.pggId
                          ? 'Đang chọn PGG, % thủ công bị khóa'
                          : 'Nhập % giảm giá (tùy chọn)'
                      "
                    />
                    <div
                      v-if="!selectedPGG && activeOrder.discountPercent > 0"
                      class="small mt-1"
                    >
                      Áp dụng giảm giá
                      <span class="fw-bold"
                        >{{ activeOrder.discountPercent }}%</span
                      >
                    </div>
                  </div>

                  <!-- Summary -->
                  <ul class="list-group mb-3">
                    <li class="list-group-item d-flex justify-content-between">
                      <span class="text-muted">Tiền hàng</span>
                      <span class="fw-semibold">{{ money(subTotal) }}</span>
                    </li>

                    <li class="list-group-item d-flex justify-content-between">
                      <span class="text-muted">Giảm giá</span>
                      <span class="fw-semibold text-danger"
                        >- {{ money(discountMoney) }}</span
                      >
                    </li>

                    <li
                      v-if="activeOrder.shipping"
                      class="list-group-item d-flex justify-content-between align-items-center"
                    >
                      <span class="text-muted">Phí vận chuyển</span>
                      <input
                        class="form-control form-control-sm text-end"
                        style="max-width: 180px"
                        v-model.number="activeOrder.shippingFee"
                        inputmode="numeric"
                      />
                    </li>

                    <li class="list-group-item d-flex justify-content-between">
                      <span class="fw-bold">Tổng phải trả</span>
                      <span class="fw-bold text-danger">{{
                        money(grandTotal)
                      }}</span>
                    </li>

                    <li
                      class="list-group-item d-flex justify-content-between align-items-center"
                    >
                      <span class="text-muted">Khách thanh toán</span>
                      <input
                        class="form-control form-control-sm text-end"
                        style="max-width: 180px"
                        v-model.number="activeOrder.paid"
                        inputmode="numeric"
                      />
                    </li>

                    <li class="list-group-item d-flex justify-content-between">
                      <span class="text-muted">Tiền thừa</span>
                      <span class="fw-semibold">{{ money(changeMoney) }}</span>
                    </li>
                  </ul>

                  <button
                    class="btn btn-success w-100"
                    :disabled="activeOrder.cart.length === 0"
                    @click="confirmOrder"
                  >
                    XÁC NHẬN ĐẶT HÀNG
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!-- /card-body -->
    </div>
    <!-- /card -->

    <!-- Toast (Bootstrap-ish) -->
    <div
      v-if="toast.show"
      class="position-fixed bottom-0 end-0 p-3"
      style="z-index: 1080"
    >
      <div
        class="toast show align-items-center text-white border-0"
        :class="toastBgClass"
      >
        <div class="d-flex">
          <div class="toast-body fw-semibold">
            {{ toast.msg }}
          </div>
          <button
            type="button"
            class="btn-close btn-close-white me-2 m-auto"
            @click="toast.show = false"
          ></button>
        </div>
      </div>
    </div>

    <!-- Product Modal (Bootstrap styles, Vue control) -->
    <div
      v-if="showProductModal"
      class="modal fade show d-block"
      tabindex="-1"
      role="dialog"
    >
      <div class="modal-dialog modal-xl modal-dialog-scrollable">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title fw-bold">Chọn biến thể để thêm vào đơn</h5>
            <button
              type="button"
              class="btn-close"
              @click="closeProductModal"
            ></button>
          </div>

          <div class="modal-body">
            <div class="d-flex gap-2 mb-3 flex-wrap">
              <input
                class="form-control"
                placeholder="Tìm kiếm..."
                v-model.trim="productKw"
                style="max-width: 420px"
              />
              <button
                class="btn btn-outline-secondary"
                type="button"
                @click="productKw = ''"
              >
                Đặt lại
              </button>
            </div>

            <div class="table-responsive">
              <table class="table table-bordered align-middle">
                <thead class="table-light">
                  <tr>
                    <th style="width: 60px">STT</th>
                    <th style="width: 130px">Mã</th>
                    <th style="width: 80px">Ảnh</th>
                    <th>Tên sản phẩm</th>
                    <th style="width: 120px">Màu sắc</th>
                    <th style="width: 140px">Kích cỡ</th>
                    <th style="width: 90px" class="text-end">Tồn</th>
                    <th style="width: 140px" class="text-end">Giá bán</th>
                    <th style="width: 110px" class="text-center">Hành động</th>
                  </tr>
                </thead>

                <tbody>
                  <tr v-for="(p, i) in filteredProducts" :key="p.code">
                    <td>{{ i + 1 }}</td>
                    <td class="fw-semibold">{{ p.code }}</td>
                    <td>
                      <img
                        :src="p.image"
                        class="rounded"
                        style="width: 44px; height: 44px; object-fit: cover"
                      />
                    </td>
                    <td>{{ p.name }}</td>
                    <td>{{ p.color }}</td>
                    <td>{{ p.size }}</td>
                    <td class="text-end fw-semibold">{{ p.stock }}</td>
                    <td class="text-end fw-semibold">{{ money(p.price) }}</td>
                    <td class="text-center">
                      <button
                        class="btn btn-dark btn-sm"
                        :disabled="p.stock <= 0"
                        @click="chooseProduct(p)"
                      >
                        Chọn
                      </button>
                    </td>
                  </tr>

                  <tr v-if="filteredProducts.length === 0">
                    <td colspan="9" class="text-center text-muted py-3">
                      Không có dữ liệu
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="modal-footer">
            <button
              class="btn btn-outline-secondary"
              type="button"
              @click="closeProductModal"
            >
              Đóng
            </button>
          </div>
        </div>
      </div>

      <div class="modal-backdrop fade show"></div>
    </div>

    <!-- Customer Modal -->
    <div
      v-if="showCustomerModal"
      class="modal fade show d-block"
      tabindex="-1"
      role="dialog"
    >
      <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title fw-bold">Chọn khách hàng</h5>
            <button
              type="button"
              class="btn-close"
              @click="closeCustomerModal"
            ></button>
          </div>

          <div class="modal-body">
            <div class="d-flex gap-2 mb-3 flex-wrap">
              <input
                class="form-control"
                placeholder="Tìm theo tên/SĐT/địa chỉ..."
                v-model.trim="customerKw"
              />
              <button
                class="btn btn-outline-secondary"
                type="button"
                @click="customerKw = ''"
              >
                Tải lại
              </button>
            </div>

            <div class="table-responsive">
              <table class="table table-bordered align-middle">
                <thead class="table-light">
                  <tr>
                    <th style="width: 60px">#</th>
                    <th style="width: 220px">Tên khách</th>
                    <th style="width: 160px">Số điện thoại</th>
                    <th>Địa chỉ</th>
                    <th style="width: 110px" class="text-center">Hành động</th>
                  </tr>
                </thead>

                <tbody>
                  <tr v-for="(c, i) in filteredCustomers" :key="c.phone">
                    <td>{{ i + 1 }}</td>
                    <td class="fw-semibold">{{ c.name }}</td>
                    <td>{{ c.phone }}</td>
                    <td>{{ c.address }}</td>
                    <td class="text-center">
                      <button
                        class="btn btn-dark btn-sm"
                        @click="chooseCustomer(c)"
                      >
                        Chọn
                      </button>
                    </td>
                  </tr>

                  <tr v-if="filteredCustomers.length === 0">
                    <td colspan="5" class="text-center text-muted py-3">
                      Không có dữ liệu
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="modal-footer">
            <button
              class="btn btn-outline-secondary"
              type="button"
              @click="closeCustomerModal"
            >
              Đóng
            </button>
          </div>
        </div>
      </div>

      <div class="modal-backdrop fade show"></div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch, onMounted, onBeforeUnmount } from "vue";
const customerTargetOrderId = ref(null);

// Nếu dự án có vue-router thì bật dòng này (giữ hóa đơn khi chuyển route):
let onBeforeRouteLeaveSafe = null;
try {
  // eslint-disable-next-line import/no-unresolved
  const vr = await import("vue-router");
  onBeforeRouteLeaveSafe = vr.onBeforeRouteLeave;
} catch {
  // không dùng router thì bỏ qua
}

/** ================= CONFIG ================= */
const MAX_ORDERS = 10;
const STORAGE_KEY = "tbts_sales_drafts_bootstrap_v1";

/** ================= STATE ================= */
const orders = ref([]);
const activeId = ref(null);
const orderSeq = ref(1);

/** =============== Toast =============== */
const toast = ref({ show: false, msg: "", type: "danger" });
const toastBgClass = computed(() => {
  if (toast.value.type === "success") return "bg-success";
  if (toast.value.type === "info") return "bg-primary";
  return "bg-danger";
});
function showToast(msg, type = "danger") {
  toast.value = { show: true, msg, type };
  window.clearTimeout(showToast._t);
  showToast._t = window.setTimeout(() => (toast.value.show = false), 2400);
}

const activeOrder = computed(
  () => orders.value.find((o) => o.id === activeId.value) || null,
);

/** ================= Mã hóa đơn: HD + YY + MM + DD + 5 số ================= */
function randInt(maxExclusive) {
  try {
    const u = new Uint32Array(1);
    crypto.getRandomValues(u);
    return u[0] % maxExclusive;
  } catch {
    return Math.floor(Math.random() * maxExclusive);
  }
}
function genMaHoaDon(date = new Date()) {
  const yy = String(date.getFullYear()).slice(-2);
  const mm = String(date.getMonth() + 1).padStart(2, "0");
  const dd = String(date.getDate()).padStart(2, "0");
  const r5 = String(randInt(100000)).padStart(5, "0");
  return `HD${yy}${mm}${dd}${r5}`;
}
function genUniqueMaHoaDon() {
  const existed = new Set(orders.value.map((o) => o.maHoaDon).filter(Boolean));
  let code = genMaHoaDon();
  while (existed.has(code)) code = genMaHoaDon();
  return code;
}

/** ================= Draft persistence ================= */
function normalizeOrder(o, idx = 1) {
  const ma = o?.maHoaDon || genMaHoaDon();
  return {
    id: o?.id ?? Date.now() + Math.random(),
    maHoaDon: ma,
    label: o?.label || `Đơn hàng ${idx} - ${ma}`,
    cart: Array.isArray(o?.cart) ? o.cart : [],
    customer: o?.customer ?? null,
    customerDraft: o?.customerDraft ?? {
      phone: "",
      email: "",
      province: "",
      district: "",
      ward: "",
      address: "",
    },
    shipping: !!o?.shipping,
    shippingFee: Number(o?.shippingFee) || 0,

    voucherCode: (o?.voucherCode || "").toString(),
    pggId: o?.pggId ?? null,
    discountPercent: Number(o?.discountPercent) || 0,

    paid: Number(o?.paid) || 0,
    note: o?.note || "",
  };
}

function saveDraftsNow() {
  try {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        version: 1,
        orders: orders.value,
        activeId: activeId.value,
        orderSeq: orderSeq.value,
        savedAt: new Date().toISOString(),
      }),
    );
  } catch {
    // ignore
  }
}

let _saveT = null;
function scheduleSaveDrafts() {
  window.clearTimeout(_saveT);
  _saveT = window.setTimeout(saveDraftsNow, 200);
}

function loadDrafts() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return;

    const data = JSON.parse(raw);
    if (!data || !Array.isArray(data.orders)) return;

    orders.value = data.orders.map((o, i) => normalizeOrder(o, i + 1));
    activeId.value = data.activeId ?? orders.value[0]?.id ?? null;
    orderSeq.value = Number(data.orderSeq) || orders.value.length + 1;
  } catch {
    // ignore
  }
}

onMounted(() => {
  loadDrafts();
  if (orders.value.length === 0) createOrder();

  window.addEventListener("beforeunload", saveDraftsNow);
  document.addEventListener("visibilitychange", () => {
    if (document.visibilityState === "hidden") saveDraftsNow();
  });
});

onBeforeUnmount(() => {
  window.removeEventListener("beforeunload", saveDraftsNow);
  saveDraftsNow();
});

watch(orders, scheduleSaveDrafts, { deep: true });
watch(activeId, scheduleSaveDrafts);
watch(orderSeq, scheduleSaveDrafts);

// Nếu có vue-router: lưu draft khi rời trang
if (onBeforeRouteLeaveSafe) {
  onBeforeRouteLeaveSafe(() => {
    saveDraftsNow();
    return true;
  });
}

/** ================= CRUD hóa đơn ================= */
function createOrder() {
  if (orders.value.length >= MAX_ORDERS) {
    showToast(
      `Chỉ được tạo tối đa ${MAX_ORDERS} hóa đơn. Vui lòng đóng bớt đơn.`,
      "danger",
    );
    return;
  }

  const id = Date.now() + Math.random();
  const maHoaDon = genUniqueMaHoaDon();
  const label = `Đơn hàng ${orderSeq.value} - ${maHoaDon}`;

  orders.value.push(
    normalizeOrder(
      {
        id,
        maHoaDon,
        label,
        cart: [],
        customer: null,
        customerDraft: {
          phone: "",
          email: "",
          province: "",
          district: "",
          ward: "",
          address: "",
        },
        shipping: false,
        shippingFee: 0,
        voucherCode: "",
        pggId: null,
        discountPercent: 0,
        paid: 0,
        note: "",
      },
      orderSeq.value,
    ),
  );

  activeId.value = id;
  orderSeq.value++;
}

function closeOrder(id) {
  const idx = orders.value.findIndex((x) => x.id === id);
  if (idx === -1) return;
  orders.value.splice(idx, 1);
  if (activeId.value === id) activeId.value = orders.value[0]?.id ?? null;
  saveDraftsNow();
}

/** ================= Cart actions ================= */
function incQty(i) {
  const o = activeOrder.value;
  if (!o) return;
  const it = o.cart[i];
  if (!it) return;

  const next = it.qty + 1;
  if (Number.isFinite(it.stock) && next > it.stock) {
    showToast("Số lượng vượt tồn kho.", "danger");
    return;
  }
  it.qty = next;
}

function decQty(i) {
  const o = activeOrder.value;
  if (!o) return;
  const it = o.cart[i];
  if (!it) return;
  it.qty = Math.max(1, it.qty - 1);
}

function removeItem(i) {
  const o = activeOrder.value;
  if (!o) return;
  o.cart.splice(i, 1);
}

/** ================= Helpers ================= */
function money(n) {
  const v = Number(n) || 0;
  return v.toLocaleString("vi-VN") + " đ";
}

const subTotal = computed(() => {
  const o = activeOrder.value;
  if (!o) return 0;
  return o.cart.reduce(
    (s, it) => s + (Number(it.price) || 0) * (Number(it.qty) || 0),
    0,
  );
});

/** ================= PGG (theo schema DB bạn gửi) =================
 * phieu_giam_gia:
 *  - ma_giam_gia, ten_giam_gia, loai_giam(bit), gia_tri_phan_tram, gia_tri_tien_mat,
 *  - don_hang_toi_thieu, gia_tri_giam_toi_da, ngay_bat_dau, ngay_ket_thuc, trang_thai
 */
const availablePGGs = ref([
  {
    id: 1,
    ma_giam_gia: "PGG10",
    ten_giam_gia: "Giảm 10%",
    loai_giam: true,
    gia_tri_phan_tram: 10,
    gia_tri_tien_mat: null,
    don_hang_toi_thieu: 0,
    gia_tri_giam_toi_da: 200000,
    ngay_bat_dau: "2026-01-01",
    ngay_ket_thuc: "2026-12-31",
    trang_thai: true,
  },
  {
    id: 2,
    ma_giam_gia: "PGG50K",
    ten_giam_gia: "Giảm 50.000đ",
    loai_giam: false,
    gia_tri_phan_tram: null,
    gia_tri_tien_mat: 50000,
    don_hang_toi_thieu: 300000,
    gia_tri_giam_toi_da: null,
    ngay_bat_dau: "2026-01-01",
    ngay_ket_thuc: "2026-12-31",
    trang_thai: true,
  },
]);

const selectedPGG = computed(() => {
  const o = activeOrder.value;
  if (!o?.pggId) return null;
  return availablePGGs.value.find((x) => x.id === o.pggId) || null;
});

function isPggValidNow(p) {
  if (!p?.trang_thai) return false;
  const now = new Date();
  const start = p.ngay_bat_dau ? new Date(p.ngay_bat_dau + "T00:00:00") : null;
  const end = p.ngay_ket_thuc ? new Date(p.ngay_ket_thuc + "T23:59:59") : null;
  if (start && now < start) return false;
  if (end && now > end) return false;
  return true;
}

function pggDesc(p) {
  if (!p) return "";
  const min = Number(p.don_hang_toi_thieu) || 0;
  const max = Number(p.gia_tri_giam_toi_da) || 0;

  if (p.loai_giam) {
    const percent = Number(p.gia_tri_phan_tram) || 0;
    return `Giảm ${percent}%${max > 0 ? ` (tối đa ${money(max)})` : ""}${min > 0 ? ` — đơn tối thiểu ${money(min)}` : ""}`;
  }
  const cash = Number(p.gia_tri_tien_mat) || 0;
  return `Giảm ${money(cash)}${max > 0 ? ` (tối đa ${money(max)})` : ""}${min > 0 ? ` — đơn tối thiểu ${money(min)}` : ""}`;
}

function clearVoucher() {
  const o = activeOrder.value;
  if (!o) return;
  o.voucherCode = "";
  o.pggId = null;
}

function applyPggByCode() {
  const o = activeOrder.value;
  if (!o) return;

  const code = (o.voucherCode || "").trim().toUpperCase();
  if (!code) {
    o.pggId = null;
    return;
  }

  const found = availablePGGs.value.find(
    (p) => p.ma_giam_gia.toUpperCase() === code,
  );
  if (!found || !isPggValidNow(found)) {
    o.pggId = null;
    showToast("Mã PGG không tồn tại / hết hạn / không hợp lệ.", "danger");
    return;
  }

  o.pggId = found.id;
  // nếu PGG là % thì set discountPercent để hiển thị (UI vẫn khóa khi chọn PGG)
  if (found.loai_giam) o.discountPercent = Number(found.gia_tri_phan_tram) || 0;
  else o.discountPercent = 0;

  showToast(`Đã chọn ${found.ma_giam_gia} - ${found.ten_giam_gia}`, "info");
}

function syncVoucherFromSelect() {
  const o = activeOrder.value;
  if (!o) return;

  if (!o.pggId) {
    o.voucherCode = "";
    return;
  }

  const p = availablePGGs.value.find((x) => x.id === o.pggId) || null;
  if (!p || !isPggValidNow(p)) {
    o.pggId = null;
    o.voucherCode = "";
    showToast("Phiếu giảm giá không hợp lệ.", "danger");
    return;
  }

  o.voucherCode = p.ma_giam_gia;
  if (p.loai_giam) o.discountPercent = Number(p.gia_tri_phan_tram) || 0;
  else o.discountPercent = 0;
}

const discountMoney = computed(() => {
  const o = activeOrder.value;
  if (!o) return 0;

  const p = selectedPGG.value;
  if (p) {
    const minOrder = Number(p.don_hang_toi_thieu) || 0;
    if (subTotal.value < minOrder) return 0;

    let giam = 0;
    const maxGiam = Number(p.gia_tri_giam_toi_da) || 0;

    if (p.loai_giam) {
      const percent = Number(p.gia_tri_phan_tram) || 0;
      giam = (subTotal.value * percent) / 100;
      if (maxGiam > 0) giam = Math.min(giam, maxGiam);
    } else {
      giam = Number(p.gia_tri_tien_mat) || 0;
      if (maxGiam > 0) giam = Math.min(giam, maxGiam);
    }

    giam = Math.min(giam, subTotal.value);
    return Math.floor(Math.max(0, giam));
  }

  const percent = Math.min(100, Math.max(0, Number(o.discountPercent) || 0));
  const giam = (subTotal.value * percent) / 100;
  return Math.floor(Math.min(subTotal.value, Math.max(0, giam)));
});

const grandTotal = computed(() => {
  const o = activeOrder.value;
  if (!o) return 0;
  const ship = o.shipping ? Number(o.shippingFee) || 0 : 0;
  return Math.max(0, subTotal.value - discountMoney.value + ship);
});

const changeMoney = computed(() => {
  const o = activeOrder.value;
  if (!o) return 0;
  const paid = Number(o.paid) || 0;
  return Math.max(0, paid - grandTotal.value);
});

/** ================= Actions ================= */
function openQr() {
  showToast("Demo: Chưa tích hợp quét QR.", "info");
}

function confirmOrder() {
  const o = activeOrder.value;
  if (!o) return;

  if (o.cart.length === 0) {
    showToast("Giỏ hàng trống. Vui lòng thêm sản phẩm.", "danger");
    return;
  }

  if (o.shipping) {
    const d = o.customerDraft;
    if (!d.phone || !d.province || !d.district || !d.ward || !d.address) {
      showToast(
        "Vui lòng nhập đủ thông tin giao hàng (SĐT + địa chỉ).",
        "danger",
      );
      return;
    }
  }

  for (const it of o.cart) {
    if (Number.isFinite(it.stock) && it.qty > it.stock) {
      showToast(`Sản phẩm ${it.code} vượt tồn kho.`, "danger");
      return;
    }
  }

  const payload = {
    ma_hoa_don: o.maHoaDon,
    loai_don: o.shipping ? 1 : 0,
    phi_van_chuyen: o.shipping ? Number(o.shippingFee) || 0 : 0,
    tong_tien: subTotal.value,
    tong_tien_giam: discountMoney.value,
    tong_tien_sau_giam: grandTotal.value,
    id_phieu_giam_gia: o.pggId || null,
    giam_thu_cong_percent: o.pggId ? 0 : Number(o.discountPercent) || 0,
    khach: {
      ten: o.customer?.name || "Khách lẻ",
      sdt: o.customerDraft.phone || o.customer?.phone || "",
      email: o.customerDraft.email || "",
      dia_chi: o.shipping
        ? `${o.customerDraft.address}, ${o.customerDraft.ward}, ${o.customerDraft.district}, ${o.customerDraft.province}`
        : "",
    },
    items: o.cart.map((it) => ({
      ma_spct: it.code,
      so_luong: it.qty,
      don_gia: it.price,
    })),
    paid: Number(o.paid) || 0,
    change: changeMoney.value,
    note: o.note || "",
  };

  console.log("CONFIRM ORDER PAYLOAD:", payload);
  showToast("Xác nhận đặt hàng (demo) thành công!", "success");
}

/** ================= Product modal ================= */
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
    image: "https://via.placeholder.com/56x56.png?text=IMG",
  },
  {
    code: "CTSP309",
    name: "Balo FPT",
    color: "Xanh dương",
    size: "Lớn (50x35x20 cm)",
    stock: 10,
    price: 1000000,
    image: "https://via.placeholder.com/56x56.png?text=IMG",
  },
  {
    code: "CTSP307",
    name: "Balo Cute",
    color: "Trắng",
    size: "Trung bình (40x30x15 cm)",
    stock: 98,
    price: 5000000,
    image: "https://via.placeholder.com/56x56.png?text=IMG",
  },
]);

const filteredProducts = computed(() => {
  const kw = productKw.value.trim().toLowerCase();
  if (!kw) return demoProducts.value;
  return demoProducts.value.filter((p) =>
    [p.code, p.name, p.color, p.size].some((x) =>
      String(x).toLowerCase().includes(kw),
    ),
  );
});

function openProductModal() {
  if (!activeOrder.value) return;
  showProductModal.value = true;
  document.body.classList.add("modal-open");
}

function closeProductModal() {
  showProductModal.value = false;
  document.body.classList.remove("modal-open");
}

function chooseProduct(p) {
  const o = activeOrder.value;
  if (!o) return;

  const existed = o.cart.find((x) => x.code === p.code);
  if (existed) {
    if (Number.isFinite(existed.stock) && existed.qty + 1 > existed.stock) {
      showToast("Số lượng vượt tồn kho.", "danger");
      return;
    }
    existed.qty += 1;
  } else {
    o.cart.push({
      key: p.code + "-" + Date.now(),
      code: p.code,
      name: p.name,
      meta: `size ${p.size} / ${p.color}`,
      image: p.image,
      price: p.price,
      qty: 1,
      stock: p.stock,
    });
  }

  closeProductModal();
}

/** ================= Customer modal ================= */
const showCustomerModal = ref(false);
const customerKw = ref("");

const demoCustomers = ref([
  {
    name: "Nguyễn Văn A",
    phone: "0123456789",
    address: "125, Phường Tăng Nhơn Phú A, TP Thủ Đức",
  },
  { name: "Trần Thị B", phone: "0987654321", address: "234, Tân Phú, TP.HCM" },
  {
    name: "Phạm Văn C",
    phone: "0909123123",
    address: "789, Bình Thạnh, TP.HCM",
  },
]);

const filteredCustomers = computed(() => {
  const kw = customerKw.value.trim().toLowerCase();
  if (!kw) return demoCustomers.value;
  return demoCustomers.value.filter((c) =>
    [c.name, c.phone, c.address].some((x) =>
      String(x).toLowerCase().includes(kw),
    ),
  );
});

function openCustomerModal() {
  if (!activeOrder.value) return;
  customerTargetOrderId.value = activeId.value; // khóa đúng hóa đơn đang mở modal
  showCustomerModal.value = true;
  document.body.classList.add("modal-open");
}

function closeCustomerModal() {
  showCustomerModal.value = false;
  customerTargetOrderId.value = null;
  document.body.classList.remove("modal-open");
}


function chooseCustomer(c) {
  const o =
    orders.value.find((x) => x.id === customerTargetOrderId.value) ||
    activeOrder.value;

  if (!o) return;

  o.customer = { ...c };

  // auto fill tối thiểu (đỡ “chọn mà không thấy gì”)
  o.customerDraft.phone = c.phone || "";
  o.customerDraft.address = c.address || o.customerDraft.address;

  closeCustomerModal();
}

</script>
<style scoped>
/* TAB chưa active = “đơn đang chờ” */
/* Tabs: áp cho cả active và chưa active */
.nav-pills .nav-link.order-tab {
  color: #1f2a44 !important;
  background: #ffffff !important;
  border: 1px solid #dee2e6 !important;
  border-radius: 10px !important;
  transition: all 0.15s ease-in-out;
}

/* Hover: chữ trắng nền #1F2A44 */
.nav-pills .nav-link.order-tab:hover {
  color: #ffffff !important;
  background: #1f2a44 !important;
  border-color: #1f2a44 !important;
}

/* ACTIVE: KHÔNG xanh bootstrap nữa, có viền nổi bật */
.nav-pills .nav-link.order-tab.active,
.nav-pills .nav-link.order-tab.active:focus {
  color: #1f2a44 !important;
  background: #ffffff !important;
  border: 2px solid #1f2a44 !important; /* viền đơn đang làm */
  box-shadow: 0 0 0 0.12rem rgba(31, 42, 68, 0.12) !important;
}


.nav-pills .nav-link.order-tab.active:hover {
  color: #ffffff !important;
  background: #1f2a44 !important;
  border-color: #1f2a44 !important;
}


.order-tab .tab-x {
  color: #1f2a44;
  opacity: 0.85;
}
.order-tab:hover .tab-x {
  color: #ffffff;
  opacity: 1;
}
.btn-customer {
  color: #1F2A44 !important;
  background: #ffffff !important;
  border: 1px solid #1F2A44 !important;
}

.btn-customer:hover {
  color: #ffffff !important;
  background: #1F2A44 !important;
  border-color: #1F2A44 !important;
}

.btn-customer:focus {
  box-shadow: 0 0 0 .12rem rgba(31, 42, 68, .18) !important;
}

</style>
