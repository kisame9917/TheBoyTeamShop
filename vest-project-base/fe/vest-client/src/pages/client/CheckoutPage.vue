<template>
  <div class="checkout container py-4">
    <nav aria-label="breadcrumb" class="mb-3">
      <ol class="breadcrumb mb-0">
        <li class="breadcrumb-item">
          <router-link to="/" class="text-muted text-decoration-none">
            Trang chủ
          </router-link>
        </li>
        <li class="breadcrumb-item active text-dark" aria-current="page">
          Thanh toán
        </li>
      </ol>
    </nav>

    <div class="row g-4">
      <div class="col-lg-7">
        <h1 class="page-title mb-4">Thanh toán</h1>

        <section class="box mb-4">
          <div class="box-title">1. Địa chỉ giao hàng</div>

          <div class="row g-3">
            <div class="col-12">
              <label class="form-label">
                Họ và Tên <span class="req">*</span>
              </label>
              <input
                v-model="form.fullName"
                type="text"
                class="form-control"
                placeholder="Nhập họ tên"
              />
              <div v-if="errors.fullName" class="text-danger small mt-1">
                {{ errors.fullName }}
              </div>
            </div>

            <div class="col-12">
              <label class="form-label">
                Số điện thoại <span class="req">*</span>
              </label>
              <input
                v-model="form.phone"
                type="text"
                class="form-control"
                placeholder="Nhập số điện thoại"
              />
              <div v-if="errors.phone" class="text-danger small mt-1">
                {{ errors.phone }}
              </div>
            </div>

            <div class="col-12">
              <label class="form-label">
                Tỉnh/Thành phố <span class="req">*</span>
              </label>
              <select
                v-model="form.province"
                class="form-select"
                @change="onProvinceChange"
              >
                <option value="">Chọn tỉnh/thành phố</option>
                <option
                  v-for="province in provinces"
                  :key="province.Code"
                  :value="province.Code"
                >
                  {{ province.FullName }}
                </option>
              </select>
              <div v-if="errors.province" class="text-danger small mt-1">
                {{ errors.province }}
              </div>
            </div>

            <div class="col-12">
              <label class="form-label">
                Phường/xã <span class="req">*</span>
              </label>
              <select
                v-model="form.ward"
                class="form-select"
                :disabled="!form.province"
              >
                <option value="">Vui lòng chọn phường/xã</option>
                <option
                  v-for="ward in wards"
                  :key="ward.Code"
                  :value="ward.Code"
                >
                  {{ ward.FullName }}
                </option>
              </select>
              <div v-if="errors.ward" class="text-danger small mt-1">
                {{ errors.ward }}
              </div>
            </div>

            <div class="col-12">
              <label class="form-label">
                Địa chỉ chi tiết <span class="req">*</span>
              </label>
              <input
                v-model="form.address"
                type="text"
                class="form-control"
                placeholder="Số nhà, tên đường..."
              />
              <div v-if="errors.address" class="text-danger small mt-1">
                {{ errors.address }}
              </div>
            </div>

            <div class="col-12">
              <label class="form-label">Ghi chú</label>
              <textarea
                v-model="form.note"
                class="form-control"
                rows="3"
                placeholder="Ghi chú"
              ></textarea>
            </div>
          </div>
        </section>

        <section class="box mb-4">
          <div class="box-title">2. Vận chuyển</div>

          <label class="ship-row">
            <input
              class="form-check-input me-2"
              type="radio"
              value="standard"
              v-model="form.shippingMethod"
            />
            <span class="ship-name">Giao hàng - Tiêu chuẩn</span>
            <span class="ship-fee">+ {{ money(shippingFee) }} đ</span>
          </label>
        </section>

        <section class="box mb-4">
          <div class="box-title">3. Phương thức thanh toán</div>

          <label class="pay-row">
            <input
              class="form-check-input me-2"
              type="radio"
              value="cod"
              v-model="form.paymentMethod"
            />
            <span>Thanh toán khi nhận hàng (COD)</span>
          </label>

          <label class="pay-row">
            <input
              class="form-check-input me-2"
              type="radio"
              value="qr"
              v-model="form.paymentMethod"
            />
            <span>Chuyển khoản QR</span>
          </label>
        </section>

        <section class="box mb-4">
          <div class="box-title">4. Áp dụng mã giảm giá</div>

          <div class="coupon">
            <input
              v-model="couponCode"
              class="form-control"
              placeholder="Nhập mã giảm giá"
            />
            <button
              class="btn btn-dark coupon-btn"
              type="button"
              @click="applyCoupon"
            >
              Sử dụng
            </button>
          </div>

          <button class="btn btn-link p-0 mt-2 coupon-toggle" type="button">
            MÃ GIẢM GIÁ <i class="bi bi-chevron-down ms-1"></i>
          </button>
        </section>

        <section class="box">
          <div class="total-row">
            <span class="total-label">THÀNH TIỀN</span>
            <span class="total-value">{{ money(safeGrandTotal) }} đ</span>
          </div>

          <label class="invoice mt-3">
            <input
              v-model="form.invoice"
              type="checkbox"
              class="form-check-input me-2"
            />
            <span>Xuất hóa đơn công ty</span>
          </label>

          <div v-if="errors.items" class="alert alert-danger py-2 mt-3 mb-2">
            {{ errors.items }}
          </div>

          <div v-if="errors.general" class="alert alert-danger py-2 mt-2 mb-0">
            {{ errors.general }}
          </div>

          <button
            class="btn btn-order w-100 mt-3"
            type="button"
            :disabled="cartItems.length === 0 || loading"
            @click="placeOrder"
          >
            {{ loading ? "ĐANG XỬ LÝ..." : "ĐẶT HÀNG" }}
          </button>
        </section>
      </div>

      <div class="col-lg-5">
        <div class="summary">
          <div class="summary-title">Đơn hàng</div>

          <div v-if="cartItems.length === 0" class="summary-empty">
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
                <tr
                  v-for="it in cartItems"
                  :key="it.idSanPhamChiTiet || it.key || `${it.productId}-${it.size}-${it.color}`"
                >
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
                        <div
                          class="small text-muted"
                          v-if="resolveProductDetailId(it)"
                        >
                          SPCT: {{ resolveProductDetailId(it) }}
                        </div>
                      </div>
                    </div>
                  </td>
                  <td class="text-center">{{ it.qty }}</td>
                  <td class="text-end">{{ money(it.price) }} đ</td>
                  <td class="text-end">
                    {{ money((Number(it.price) || 0) * (Number(it.qty) || 0)) }} đ
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="summary-footer">
            <div class="sum-line">
              <span>Tổng sản phẩm</span>
              <span>{{ safeTotalQty }}</span>
            </div>
            <div class="sum-line">
              <span>Tổng tiền</span>
              <span>{{ money(safeSubtotal) }} đ</span>
            </div>
            <div class="sum-line">
              <span>Vận chuyển</span>
              <span>{{ money(shippingFee) }} đ</span>
            </div>
            <div class="sum-line total">
              <span>THÀNH TIỀN</span>
              <span>{{ money(safeGrandTotal) }} đ</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showQrModal" class="qr-backdrop" @click.self="closeQrModal">
      <div class="qr-modal">
        <div class="qr-modal-header">
          <h5 class="mb-0">Thanh toán chuyển khoản QR</h5>
          <button class="btn-close" type="button" @click="closeQrModal"></button>
        </div>

        <div class="qr-modal-body">
          <div class="text-center mb-3">
            <img
              v-if="qrData.qrImageUrl"
              :src="normalizeQrUrl(qrData.qrImageUrl)"
              alt="QR thanh toán"
              class="qr-image"
            />
            <div v-else class="qr-placeholder">
              Chưa có ảnh QR
            </div>
          </div>

          <div class="qr-info">
            <div><b>Mã đơn:</b> {{ qrData.maHoaDon || "-" }}</div>
            <div><b>Ngân hàng:</b> {{ qrData.bankName || "Techcombank" }}</div>
            <div><b>Chủ tài khoản:</b> {{ qrData.bankAccountName || "-" }}</div>
            <div><b>Số tài khoản:</b> {{ qrData.bankAccountNo || "-" }}</div>
            <div><b>Số tiền:</b> {{ money(qrData.amount) }} đ</div>
            <div><b>Nội dung CK:</b> {{ qrData.transferContent || "-" }}</div>
          </div>

          <div class="mt-3">
            <label class="form-label">Mã giao dịch (nếu có)</label>
            <input
              v-model="qrForm.maGiaoDich"
              type="text"
              class="form-control"
              placeholder="Ví dụ: TCB123456789"
            />
          </div>

          <div class="mt-3">
            <label class="form-label">Ghi chú xác nhận</label>
            <textarea
              v-model="qrForm.ghiChu"
              class="form-control"
              rows="3"
              placeholder="Khách đã chuyển khoản"
            ></textarea>
          </div>

          <div v-if="qrError" class="alert alert-danger mt-3 mb-0">
            {{ qrError }}
          </div>
        </div>

        <div class="qr-modal-footer">
          <button class="btn btn-outline-secondary" type="button" @click="closeQrModal">
            Đóng
          </button>
          <button
            class="btn btn-dark"
            type="button"
            :disabled="confirmingQr"
            @click="confirmQrPayment"
          >
            {{ confirmingQr ? "ĐANG XÁC NHẬN..." : "Tôi đã chuyển khoản" }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, watch } from "vue";
import { useRouter } from "vue-router";
import { useCart } from "../../composables/useCart";
import units from "../../assets/vn_units.json";

const router = useRouter();
const { cartItems, clearCart } = useCart();

const provinces = units;

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
const discount = ref(0);
const loading = ref(false);

const showQrModal = ref(false);
const confirmingQr = ref(false);
const qrError = ref("");

const qrData = reactive({
  orderId: null,
  maHoaDon: "",
  qrImageUrl: "",
  bankName: "",
  bankAccountName: "",
  bankAccountNo: "",
  transferContent: "",
  amount: 0,
});

const qrForm = reactive({
  maGiaoDich: "",
  ghiChu: "Khách đã chuyển khoản",
});

const errors = reactive({
  fullName: "",
  phone: "",
  province: "",
  ward: "",
  address: "",
  shippingMethod: "",
  paymentMethod: "",
  items: "",
  general: "",
});

const selectedProvince = computed(() => {
  return provinces.find((p) => String(p.Code) === String(form.province)) || null;
});

const wards = computed(() => {
  return selectedProvince.value?.Wards || [];
});

const shippingFee = computed(() => 60000);

const safeTotalQty = computed(() => {
  return cartItems.value.reduce((sum, it) => sum + (Number(it.qty) || 0), 0);
});

const safeSubtotal = computed(() => {
  return cartItems.value.reduce((sum, it) => {
    const price = Number(it.price) || 0;
    const qty = Number(it.qty) || 0;
    return sum + price * qty;
  }, 0);
});

const safeGrandTotal = computed(() => {
  return safeSubtotal.value + (Number(shippingFee.value) || 0) - (Number(discount.value) || 0);
});

function onProvinceChange() {
  form.ward = "";
  errors.province = "";
  errors.ward = "";
}

function money(v) {
  const n = Number(v) || 0;
  return n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
}

function applyCoupon() {
  discount.value = 0;
  alert("Mã giảm giá demo - bạn sẽ nối API coupon sau.");
}

function resetErrors() {
  Object.keys(errors).forEach((key) => {
    errors[key] = "";
  });
}

function validateFullName(value) {
  if (!value?.trim()) return "Vui lòng nhập họ và tên";
  if (value.trim().length < 2) return "Họ tên phải có ít nhất 2 ký tự";
  if (value.trim().length > 100) return "Họ tên không được vượt quá 100 ký tự";
  return "";
}

function validatePhone(value) {
  if (!value?.trim()) return "Vui lòng nhập số điện thoại";
  const normalized = value.trim();
  const phoneRegex = /^(0|\+84)[0-9]{9,10}$/;
  if (!phoneRegex.test(normalized)) return "Số điện thoại không hợp lệ";
  return "";
}

function validateProvince(value) {
  if (!value) return "Vui lòng chọn tỉnh/thành phố";
  return "";
}

function validateWard(value) {
  if (!value) return "Vui lòng chọn phường/xã";
  return "";
}

function validateAddress(value) {
  if (!value?.trim()) return "Vui lòng nhập địa chỉ chi tiết";
  if (value.trim().length < 5) return "Địa chỉ quá ngắn";
  if (value.trim().length > 255) return "Địa chỉ không được vượt quá 255 ký tự";
  return "";
}

function validateShippingMethod(value) {
  const allowed = ["standard"];
  if (!allowed.includes(value)) return "Phương thức vận chuyển không hợp lệ";
  return "";
}

function validatePaymentMethod(value) {
  const allowed = ["cod", "qr"];
  if (!allowed.includes(value)) return "Phương thức thanh toán không hợp lệ";
  return "";
}

function resolveProductDetailId(it) {
  return (
    it.idSanPhamChiTiet ||
    it.productDetailId ||
    it.idSpct ||
    it.variantId ||
    null
  );
}

function validateCartItems() {
  if (!Array.isArray(cartItems.value) || cartItems.value.length === 0) {
    return "Giỏ hàng đang trống";
  }

  for (const item of cartItems.value) {
    if (!item.name || !String(item.name).trim()) {
      return "Có sản phẩm không hợp lệ trong giỏ hàng";
    }

    if (!Number.isFinite(Number(item.price)) || Number(item.price) <= 0) {
      return `Giá sản phẩm "${item.name}" không hợp lệ`;
    }

    if (!Number.isInteger(Number(item.qty)) || Number(item.qty) <= 0) {
      return `Số lượng sản phẩm "${item.name}" không hợp lệ`;
    }

    if (!resolveProductDetailId(item)) {
      return `Sản phẩm "${item.name}" chưa có idSanPhamChiTiet`;
    }
  }

  return "";
}

function validateForm() {
  resetErrors();

  errors.fullName = validateFullName(form.fullName);
  errors.phone = validatePhone(form.phone);
  errors.province = validateProvince(form.province);
  errors.ward = validateWard(form.ward);
  errors.address = validateAddress(form.address);
  errors.shippingMethod = validateShippingMethod(form.shippingMethod);
  errors.paymentMethod = validatePaymentMethod(form.paymentMethod);
  errors.items = validateCartItems();

  const hasError = Object.values(errors).some((v) => !!v);
  return !hasError;
}

watch(
  () => form.fullName,
  (v) => {
    errors.fullName = validateFullName(v);
  }
);

watch(
  () => form.phone,
  (v) => {
    errors.phone = validatePhone(v);
  }
);

watch(
  () => form.province,
  (v) => {
    errors.province = validateProvince(v);
  }
);

watch(
  () => form.ward,
  (v) => {
    errors.ward = validateWard(v);
  }
);

watch(
  () => form.address,
  (v) => {
    errors.address = validateAddress(v);
  }
);

watch(
  () => form.paymentMethod,
  (v) => {
    errors.paymentMethod = validatePaymentMethod(v);
  }
);

function buildOrderPayload() {
  const provinceName = selectedProvince.value?.FullName || "";
  const wardName =
    wards.value.find((w) => String(w.Code) === String(form.ward))?.FullName || "";

  const normalizedItems = cartItems.value.map((it) => {
    return {
      idSanPhamChiTiet: resolveProductDetailId(it),
      soLuong: Number(it.qty) || 0,
    };
  });

  const calculatedSubtotal = cartItems.value.reduce((sum, it) => {
    const price = Number(it.price) || 0;
    const qty = Number(it.qty) || 0;
    return sum + price * qty;
  }, 0);

  const calculatedShippingFee = Number(shippingFee.value) || 0;
  const calculatedDiscount = Number(discount.value) || 0;
  const calculatedGrandTotal =
    calculatedSubtotal + calculatedShippingFee - calculatedDiscount;

  return {
    maHoaDon: null,
    loaiDon: true,
    phiVanChuyen: calculatedShippingFee,

    idKhachHang: null,
    tenKhachHang: form.fullName.trim(),
    soDienThoai: form.phone.trim(),
    emailKhachHang: "",
    diaChiKhachHang: `${form.address.trim()}, ${wardName}, ${provinceName}`,

    idPhieuGiamGia: null,
    giamThuCongPercent: 0,
    paid: 0,
    ghiChu: form.note?.trim() || "Khách đặt hàng online",

    paymentMethod: form.paymentMethod === "cod" ? "COD" : "QR",
    maGiaoDich: null,
    ghiChuThanhToan: null,

    tenNguoiNhanHang: form.fullName.trim(),
    soDienThoaiNhanHang: form.phone.trim(),
    tinhThanhNhanHang: provinceName,
    phuongXaNhanHang: wardName,
    quanHuyenNhanHang: "",
    diaChiNhanHangChiTiet: form.address.trim(),

    tongTien: calculatedSubtotal,
    tongTienGiam: calculatedDiscount,
    tongTienSauGiam: calculatedGrandTotal,

    items: normalizedItems,
  };
}

async function checkoutApi(payload) {
  const response = await fetch("http://localhost:8080/api/online-checkout", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    const err = new Error(data?.message || "Checkout thất bại");
    err.validationErrors = data?.errors || null;
    err.raw = data;
    throw err;
  }

  return data;
}

async function confirmQrPaymentApi(orderId, payload) {
  const response = await fetch(
    `http://localhost:8080/api/online-checkout/${orderId}/confirm-payment`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    }
  );

  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    const err = new Error(data?.message || "Xác nhận thanh toán thất bại");
    err.raw = data;
    throw err;
  }

  return data;
}

function normalizeQrUrl(url) {
  if (!url) return "";
  if (url.startsWith("http://") || url.startsWith("https://")) return url;
  return `http://localhost:8080${url}`;
}

function openQrModal(data) {
  qrData.orderId = data?.orderId || null;
  qrData.maHoaDon = data?.maHoaDon || "";
  qrData.qrImageUrl = data?.qrImageUrl || "";
  qrData.bankName = data?.bankName || "";
  qrData.bankAccountName = data?.bankAccountName || "";
  qrData.bankAccountNo = data?.bankAccountNo || "";
  qrData.transferContent = data?.transferContent || "";
  qrData.amount = Number(data?.amount) || 0;

  qrForm.maGiaoDich = "";
  qrForm.ghiChu = "Khách đã chuyển khoản";
  qrError.value = "";
  showQrModal.value = true;
}

function closeQrModal() {
  if (confirmingQr.value) return;
  showQrModal.value = false;
  qrError.value = "";
}

async function confirmQrPayment() {
  if (!qrData.orderId) {
    qrError.value = "Không tìm thấy mã đơn để xác nhận thanh toán";
    return;
  }

  try {
    confirmingQr.value = true;
    qrError.value = "";

    const payload = {
      maGiaoDich: qrForm.maGiaoDich?.trim() || "",
      soTien: qrData.amount,
      ghiChu: qrForm.ghiChu?.trim() || "Khách đã chuyển khoản",
    };

    const data = await confirmQrPaymentApi(qrData.orderId, payload);

    if (typeof clearCart === "function") {
      clearCart();
    }

    alert(data?.message || "Xác nhận thanh toán QR thành công");
    closeQrModal();

    router.push({
      path: "/checkout/success",
      query: {
        orderId: qrData.orderId || "",
      },
    });
  } catch (error) {
    console.error("CONFIRM QR ERROR:", error);
    qrError.value = error.message || "Xác nhận thanh toán thất bại";
  } finally {
    confirmingQr.value = false;
  }
}

async function placeOrder() {
  if (!validateForm()) {
    errors.general = "Vui lòng kiểm tra lại thông tin đặt hàng";
    return;
  }

  try {
    loading.value = true;
    errors.general = "";

    const payload = buildOrderPayload();

    console.log("RAW CART ITEMS:", JSON.parse(JSON.stringify(cartItems.value)));
    console.log("ORDER PAYLOAD:", payload);

    const hasInvalidItem = payload.items.some(
      (it) => !it.idSanPhamChiTiet || !it.soLuong
    );

    if (hasInvalidItem) {
      errors.general = "Có sản phẩm chưa map được idSanPhamChiTiet";
      alert(errors.general);
      return;
    }

    const data = await checkoutApi(payload);

    if (form.paymentMethod === "qr") {
      openQrModal(data);
      return;
    }

    if (typeof clearCart === "function") {
      clearCart();
    }

    alert(data?.message || "Đặt hàng thành công");

    router.push({
      path: "/checkout/success",
      query: {
        orderId: data?.orderId || "",
      },
    });
  } catch (error) {
    console.error("CHECKOUT ERROR:", error);
    console.error("CHECKOUT ERROR RAW:", error?.raw);

    errors.general = error.message || "Đặt hàng thất bại";
    alert(errors.general);
  } finally {
    loading.value = false;
  }
}

function onImgError(e) {
  e.target.src =
    "data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='120' height='120'%3E%3Crect width='100%25' height='100%25' fill='%23f1f3f5'/%3E%3Ctext x='50%25' y='52%25' dominant-baseline='middle' text-anchor='middle' fill='%2399a1aa' font-size='14'%3E%E1%BA%A2nh%3C/text%3E%3C/svg%3E";
}
</script>

<style scoped>
.checkout {
  --navy: #000f51;
  --muted: #6c757d;
}

.page-title {
  font-size: 26px;
  font-weight: 700;
  color: #111;
}

.box {
  border-top: 1px solid rgba(0, 0, 0, 0.1);
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

.req {
  color: #d11;
}

.form-control,
.form-select {
  border-radius: 2px;
  background: #f3f5f7;
  border: 1px solid rgba(0, 0, 0, 0.08);
  font-size: 13.5px;
}

.ship-row,
.pay-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 0;
  font-size: 13.5px;
  color: #111;
  margin: 0;
}

.ship-name {
  font-weight: 600;
}

.ship-fee {
  color: var(--muted);
}

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
  color: #b28a2a;
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

.btn-order:disabled {
  opacity: 0.6;
}

.summary {
  border-left: 1px solid rgba(0, 0, 0, 0.12);
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
  border-bottom: 1px solid rgba(0, 0, 0, 0.12);
}

.summary-table td {
  font-size: 13px;
  color: #111;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
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
  border: 1px solid rgba(0, 0, 0, 0.08);
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

.summary-footer {
  padding-top: 12px;
  margin-top: 12px;
  border-top: 1px solid rgba(0, 0, 0, 0.12);
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

.qr-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 1050;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.qr-modal {
  width: 100%;
  max-width: 520px;
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.qr-modal-header,
.qr-modal-footer {
  padding: 14px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
}

.qr-modal-footer {
  border-bottom: none;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.qr-modal-body {
  padding: 16px;
}

.qr-image {
  width: 260px;
  max-width: 100%;
  height: auto;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 8px;
}

.qr-placeholder {
  height: 260px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  border-radius: 8px;
  color: #6c757d;
}

.qr-info {
  display: grid;
  gap: 6px;
  font-size: 14px;
}
</style>