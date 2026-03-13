<template>
  <div class="checkout-page">
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

      <div class="checkout-head mb-4">
        <div>
          <h1 class="page-title mb-1">THANH TOÁN</h1>
          <div class="page-subtitle">
            Hoàn tất thông tin giao hàng và kiểm tra lại đơn hàng của bạn
          </div>
        </div>
        <div class="checkout-badge">{{ safeTotalQty }} sản phẩm</div>
      </div>

      <div class="row g-4">
        <div class="col-lg-7">
          <section class="checkout-card mb-4">
            <div class="checkout-card__header">
              <span>1. Địa chỉ giao hàng</span>
            </div>

            <div class="checkout-card__body">
              <div class="row g-3">
                <div class="col-12">
                  <label class="form-label">
                    Họ và Tên <span class="req">*</span>
                  </label>
                  <input
                      v-model="form.fullName"
                      type="text"
                      class="form-control input-ui"
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
                      class="form-control input-ui"
                      placeholder="Nhập số điện thoại"
                  />
                  <div v-if="errors.phone" class="text-danger small mt-1">
                    {{ errors.phone }}
                  </div>
                </div>

                <div class="col-md-6">
                  <label class="form-label">
                    Tỉnh/Thành phố <span class="req">*</span>
                  </label>
                  <select
                      v-model="form.province"
                      class="form-select input-ui"
                      :disabled="provinceLoading"
                      @change="onProvinceChange"
                  >
                    <option value="">
                      {{ provinceLoading ? "Đang tải tỉnh/thành..." : "Chọn tỉnh/thành phố" }}
                    </option>
                    <option
                        v-for="province in provinces"
                        :key="province.code"
                        :value="province.code"
                    >
                      {{ province.name }}
                    </option>
                  </select>
                  <div v-if="errors.province" class="text-danger small mt-1">
                    {{ errors.province }}
                  </div>
                </div>

                <div class="col-md-6">
                  <label class="form-label">
                    Quận/Huyện <span class="req">*</span>
                  </label>
                  <select
                      v-model="form.district"
                      class="form-select input-ui"
                      :disabled="!form.province || districtLoading"
                      @change="onDistrictChange"
                  >
                    <option value="">
                      {{ districtLoading ? "Đang tải quận/huyện..." : "Chọn quận/huyện" }}
                    </option>
                    <option
                        v-for="district in districts"
                        :key="district.code"
                        :value="district.code"
                    >
                      {{ district.name }}
                    </option>
                  </select>
                  <div v-if="errors.district" class="text-danger small mt-1">
                    {{ errors.district }}
                  </div>
                </div>

                <div class="col-md-6">
                  <label class="form-label">
                    Phường/Xã <span class="req">*</span>
                  </label>
                  <select
                      v-model="form.ward"
                      class="form-select input-ui"
                      :disabled="!form.district || wardLoading"
                  >
                    <option value="">
                      {{ wardLoading ? "Đang tải phường/xã..." : "Chọn phường/xã" }}
                    </option>
                    <option
                        v-for="ward in wards"
                        :key="ward.code"
                        :value="ward.code"
                    >
                      {{ ward.name }}
                    </option>
                  </select>
                  <div v-if="errors.ward" class="text-danger small mt-1">
                    {{ errors.ward }}
                  </div>
                </div>

                <div class="col-md-6">
                  <label class="form-label">
                    Địa chỉ chi tiết <span class="req">*</span>
                  </label>
                  <input
                      v-model="form.address"
                      type="text"
                      class="form-control input-ui"
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
                      class="form-control input-ui textarea-ui"
                      rows="4"
                      placeholder="Ghi chú cho đơn hàng"
                  ></textarea>
                </div>
              </div>
            </div>
          </section>

          <section class="checkout-card mb-4">
            <div class="checkout-card__header">
              <span>2. Vận chuyển</span>
            </div>

            <div class="checkout-card__body">
              <label class="option-row">
                <div class="option-row__left">
                  <input
                      class="form-check-input me-2"
                      type="radio"
                      value="standard"
                      v-model="form.shippingMethod"
                  />
                  <span class="option-row__title">Giao hàng tiêu chuẩn</span>
                </div>
                <span class="option-row__value">+ {{ money(shippingFee) }} đ</span>
              </label>
            </div>
          </section>

          <section class="checkout-card mb-4">
            <div class="checkout-card__header">
              <span>3. Phương thức thanh toán</span>
            </div>

            <div class="checkout-card__body">
              <label class="option-row">
                <div class="option-row__left">
                  <input
                      class="form-check-input me-2"
                      type="radio"
                      value="cod"
                      v-model="form.paymentMethod"
                  />
                  <span class="option-row__title">Thanh toán khi nhận hàng (COD)</span>
                </div>
              </label>

              <label class="option-row">
                <div class="option-row__left">
                  <input
                      class="form-check-input me-2"
                      type="radio"
                      value="qr"
                      v-model="form.paymentMethod"
                  />
                  <span class="option-row__title">Chuyển khoản QR</span>
                </div>
              </label>
            </div>
          </section>

          <section class="checkout-card mb-4">
  <div class="checkout-card__header">
    <span>4. Áp dụng mã giảm giá</span>
  </div>

  <div class="checkout-card__body">
    <div class="coupon-box">
      <input
        :value="appliedVoucherDisplay"
        class="form-control input-ui"
        placeholder="Chưa có mã giảm giá phù hợp"
        readonly
      />
      <button
        class="coupon-btn"
        type="button"
        @click="openVoucherModal"
        :disabled="eligibleVoucherEntries.length === 0"
      >
        Chọn
      </button>
    </div>

    <div v-if="bestEligibleVoucherEntry" class="small text-success mt-2">
      Tự động áp dụng tốt nhất:
      <b>{{ bestEligibleVoucherEntry.v.ma_giam_gia }}</b>
      - giảm {{ money(bestEligibleVoucherEntry.discount) }} đ
    </div>

    <div v-else class="small text-muted mt-2">
      Không có phiếu giảm giá phù hợp với đơn hàng này.
    </div>
  </div>
</section>
        </div>

        <div class="col-lg-5">
          <div class="summary-card">
            <div class="summary-card__header">TÓM TẮT ĐƠN HÀNG</div>

            <div v-if="cartItems.length === 0" class="summary-empty">
              Chưa có sản phẩm trong giỏ hàng.
            </div>

            <div v-else class="summary-card__body">
              <div class="summary-list">
                <div
                    class="summary-item"
                    v-for="it in cartItems"
                    :key="it.idSanPhamChiTiet || it.key || `${it.productId}-${it.size}-${it.color}`"
                >
                  <div class="summary-item__left">
                    <img
                        :src="it.image"
                        class="summary-item__img"
                        alt="sp"
                        @error="onImgError"
                    />

                    <div class="summary-item__info">
                      <div class="summary-item__name">{{ it.name }}</div>

                      <div class="summary-item__meta">
                        <span v-if="it.color">Màu: {{ it.color }}</span>
                        <span v-if="it.size">Kích cỡ: {{ it.size }}</span>
                      </div>

                      <div
                          class="summary-item__spct"
                          v-if="resolveProductDetailId(it)"
                      >
                        SPCT: {{ resolveProductDetailId(it) }}
                      </div>

                      <div class="summary-item__qty">
                        Số lượng: {{ it.qty }}
                      </div>
                    </div>
                  </div>

                  <div class="summary-item__right">
                    <div class="summary-item__price">
                      {{ money(it.price) }} đ
                    </div>
                    <div class="summary-item__total">
                      {{ money((Number(it.price) || 0) * (Number(it.qty) || 0)) }} đ
                    </div>
                  </div>
                </div>
              </div>

              <div class="summary-footer">
                <div class="sum-line">
                  <span>Tổng sản phẩm</span>
                  <strong>{{ safeTotalQty }}</strong>
                </div>
                <div class="sum-line">
                  <span>Tổng tiền</span>
                  <strong>{{ money(safeSubtotal) }} đ</strong>
                </div>
                <div class="sum-line">
                  <span>Vận chuyển</span>
                  <strong>{{ money(shippingFee) }} đ</strong>
                </div>
                <div class="sum-line" v-if="discount > 0">
  <span>Giảm giá</span>
  <strong>- {{ money(discount) }} đ</strong>
</div>
                <div class="sum-line total">
                  <span>THÀNH TIỀN</span>
                  <strong>{{ money(safeGrandTotal) }} đ</strong>
                </div>

                <label class="invoice-row mt-3">
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
                    class="btn-order w-100 mt-3"
                    type="button"
                    :disabled="cartItems.length === 0 || loading"
                    @click="placeOrder"
                >
                  {{ loading ? "ĐANG XỬ LÝ..." : "ĐẶT HÀNG" }}
                </button>
              </div>
            </div>
          </div>

          <div class="checkout-benefits mt-3">
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
                  class="form-control input-ui"
                  placeholder="Ví dụ: TCB123456789"
              />
            </div>

            <div class="mt-3">
              <label class="form-label">Ghi chú xác nhận</label>
              <textarea
                  v-model="qrForm.ghiChu"
                  class="form-control input-ui textarea-ui"
                  rows="3"
                  placeholder="Khách đã chuyển khoản"
              ></textarea>
            </div>

            <div v-if="qrError" class="alert alert-danger mt-3 mb-0">
              {{ qrError }}
            </div>
          </div>

          <div class="qr-modal-footer">
         <button class="btn-qr-close" type="button" @click="closeQrModal">
  Đóng
</button>
<button
    class="btn-qr-confirm"
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
  </div>
  <!-- Modal xác nhận đặt hàng -->
  <div
      v-if="showConfirmModal"
      class="confirm-backdrop"
      @click.self="closeConfirmModal"
  >
    <div class="confirm-modal">
      <div class="confirm-modal__header">
        <h5 class="mb-0">Xác nhận đặt hàng</h5>
        <button
            class="btn-close btn-close-white"
            type="button"
            @click="closeConfirmModal"
        ></button>
      </div>

      <div class="confirm-modal__body">
        <div class="confirm-icon">
          <i class="bi bi-bag-check"></i>
        </div>

        <div class="confirm-title">Bạn có chắc muốn đặt đơn hàng này?</div>

        <div class="confirm-desc">
          Đơn hàng sẽ được tạo với tổng thanh toán là
          <strong>{{ money(safeGrandTotal) }} đ</strong>.
        </div>
      </div>

      <div class="confirm-modal__footer">
        <button
            class="btn-cancel"
            type="button"
            @click="closeConfirmModal"
            :disabled="confirmSubmitting"
        >
          Hủy
        </button>

        <button
            class="btn-confirm"
            type="button"
            @click="submitOrder"
            :disabled="confirmSubmitting"
        >
          {{ confirmSubmitting ? "ĐANG XỬ LÝ..." : "Xác nhận đặt hàng" }}
        </button>
      </div>
    </div>
  </div>

  <!-- Modal thành công -->
  <div
      v-if="showSuccessModal"
      class="success-backdrop"
      @click.self="closeSuccessModal"
  >
    <div class="success-modal">
      <div class="success-icon-wrap">
        <div class="success-icon">
          <i class="bi bi-check2"></i>
        </div>
      </div>

      <div class="success-title">Thành công</div>
      <div class="success-desc">
        {{ successMessage }}
      </div>

      <div class="success-note">
        Đang chuyển sang trang kết quả đơn hàng...
      </div>
    </div>
  </div>
 <div v-if="showVoucherModal" class="confirm-backdrop" @click.self="showVoucherModal = false">
  <div class="voucher-modal">
    <div class="voucher-modal__header">
      <h5>Chọn phiếu giảm giá</h5>
      <button type="button" class="voucher-modal__close" @click="showVoucherModal = false">
        ×
      </button>
    </div>

    <div class="voucher-modal__body">
      <div v-if="eligibleVoucherEntries.length === 0" class="text-muted">
        Không có phiếu giảm giá phù hợp với đơn hàng này.
      </div>

      <label
        v-for="e in eligibleVoucherEntries"
        :key="e.v.id"
        class="voucher-item"
        :class="{ active: selectedVoucherId === e.v.id }"
      >
        <input
          type="radio"
          :value="e.v.id"
          v-model="selectedVoucherId"
          hidden
        />

        <div class="voucher-item__left">
          <div class="voucher-item__code">
            {{ e.v.ma_giam_gia }}
            <span v-if="bestEligibleVoucherEntry?.v?.id === e.v.id" class="badge-best">
              Tốt nhất
            </span>
          </div>

          <div class="voucher-item__name">{{ e.v.ten_giam_gia }}</div>

          <div class="voucher-item__discount text-danger">
            Giảm {{ money(e.discount) }} đ
          </div>

          <div class="voucher-item__meta">
            Đơn tối thiểu: {{ money(e.v.don_hang_toi_thieu) }} đ
          </div>
        </div>

        <div class="voucher-item__right">
          <span v-if="selectedVoucherId === e.v.id">✔</span>
        </div>
      </label>
    </div>

    <div class="voucher-modal__footer">
      <button type="button" class="btn btn-secondary" @click="showVoucherModal = false">
        Đóng
      </button>
      <button type="button" class="btn btn-danger" @click="confirmVoucherSelection">
        Áp dụng
      </button>
    </div>
  </div>
</div>
</template>

<script setup>
import { reactive, ref, computed, watch, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useCart } from "../../composables/useCart";

const router = useRouter();
const { cartItems, clearCart } = useCart();

const PROVINCE_API_BASE = "https://provinces.open-api.vn/api/v1";

const provinces = ref([]);
const districts = ref([]);
const wards = ref([]);

const provinceLoading = ref(false);
const districtLoading = ref(false);
const wardLoading = ref(false);

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

const vouchers = ref([]);
const showVoucherModal = ref(false);

const selectedVoucherId = ref(null);   // đang tick trong modal
const appliedVoucherId = ref(null);    // đang áp dụng thật
const discount = ref(0);
const loading = ref(false);

const showQrModal = ref(false);
const confirmingQr = ref(false);
const qrError = ref("");
const showConfirmModal = ref(false);
const confirmSubmitting = ref(false);

const showSuccessModal = ref(false);
const successMessage = ref("");
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
  district: "",
  ward: "",
  address: "",
  shippingMethod: "",
  paymentMethod: "",
  items: "",
  general: "",
});

const selectedProvince = computed(() => {
  return provinces.value.find((p) => String(p.code) === String(form.province)) || null;
});

const selectedDistrict = computed(() => {
  return districts.value.find((d) => String(d.code) === String(form.district)) || null;
});

const selectedWard = computed(() => {
  return wards.value.find((w) => String(w.code) === String(form.ward)) || null;
});

const shippingFee = computed(() => {
  const provinceName = String(selectedProvince.value?.name || "").toLowerCase();
  const districtName = String(selectedDistrict.value?.name || "").toLowerCase();
  const wardName = String(selectedWard.value?.name || "").toLowerCase();

  if (!provinceName || !districtName || !wardName) return 0;

  let fee = 30000;

  // nội thành HN/HCM
  if (
    provinceName.includes("hà nội") ||
    provinceName.includes("hồ chí minh")
  ) {
    fee = 25000;
  }

  // nếu là quận thì giảm thêm
  if (districtName.includes("quận")) {
    fee -= 5000;
  }

  // nếu là huyện/thị xã/thành phố thuộc tỉnh thì cộng thêm
  if (
    districtName.includes("huyện") ||
    districtName.includes("thị xã") ||
    districtName.includes("thành phố")
  ) {
    fee += 5000;
  }

  // nếu là xã thì cộng thêm nữa
  if (wardName.includes("xã")) {
    fee += 5000;
  }

  return Math.max(fee, 0);
});

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
function normalizeKhIds(raw) {
  if (!raw) return null;

  if (Array.isArray(raw)) {
    const ids = raw
      .map((x) => {
        if (typeof x === "number") return x;
        if (typeof x === "string") return Number(x);
        return Number(x?.id ?? x?.khachHangId ?? x?.khach_hang_id);
      })
      .filter((n) => Number.isFinite(n));
    return ids.length ? ids : null;
  }

  if (typeof raw === "string") {
    if (raw.includes(",")) {
      const ids = raw
        .split(",")
        .map((s) => Number(s.trim()))
        .filter((n) => Number.isFinite(n));
      return ids.length ? ids : null;
    }
    const one = Number(raw);
    return Number.isFinite(one) ? [one] : null;
  }

  return null;
}

function normalizeVoucher(x) {
  const khIdsRaw = x.khachHangIds ?? x.khach_hang_ids ?? x.khachHangs ?? null;
  const khSingle = x.khachHangId ?? x.khach_hang_id ?? x.idKhachHang ?? null;

  return {
    id: x.id,
    ma_giam_gia: x.maGiamGia ?? x.ma_giam_gia ?? "",
    ten_giam_gia: x.tenGiamGia ?? x.ten_giam_gia ?? "",
    trang_thai: x.trangThai ?? x.trang_thai ?? true,
    so_luong: Number(x.soLuong ?? x.so_luong ?? 0),
    loai_giam: !!(x.loaiGiam ?? x.loai_giam),
    gia_tri_phan_tram: Number(x.giaTriPhanTram ?? x.gia_tri_phan_tram ?? 0),
    gia_tri_tien_mat: Number(x.giaTriTienMat ?? x.gia_tri_tien_mat ?? 0),
    don_hang_toi_thieu: Number(x.donHangToiThieu ?? x.don_hang_toi_thieu ?? 0),
    gia_tri_giam_toi_da: Number(x.giaTriGiamToiDa ?? x.gia_tri_giam_toi_da ?? 0),
    loai_phieu: x.loaiPhieu ?? x.loai_phieu ?? null,
    khach_hang_ids: normalizeKhIds(khIdsRaw),
    khach_hang_id: khSingle != null ? Number(khSingle) : null,
    ngay_bat_dau: x.ngayBatDau ?? x.ngay_bat_dau ?? null,
    ngay_ket_thuc: x.ngayKetThuc ?? x.ngay_ket_thuc ?? null,
  };
}

function toTime(v) {
  if (!v) return null;
  const t = new Date(v).getTime();
  return Number.isFinite(t) ? t : null;
}

function isVoucherInDateRange(v) {
  const now = Date.now();
  const start = toTime(v?.ngay_bat_dau);
  const end = toTime(v?.ngay_ket_thuc);
  if (start !== null && now < start) return false;
  if (end !== null && now > end) return false;
  return true;
}

function calcVoucherDiscount(subtotal, v) {
  const st = Number(subtotal) || 0;
  if (st <= 0) return 0;
  if (!v?.trang_thai) return 0;
  if ((Number(v.so_luong) || 0) <= 0) return 0;
  if (!isVoucherInDateRange(v)) return 0;
  if (st < (Number(v.don_hang_toi_thieu) || 0)) return 0;

  let disc = 0;
  if (v.loai_giam) {
    disc = (st * (Number(v.gia_tri_phan_tram) || 0)) / 100;
  } else {
    disc = Number(v.gia_tri_tien_mat) || 0;
  }

  const cap = Number(v.gia_tri_giam_toi_da) || 0;
  if (cap > 0) disc = Math.min(disc, cap);

  disc = Math.max(0, Math.min(disc, st));
  return Math.floor(disc);
}

const safeGrandTotal = computed(() => {
  return safeSubtotal.value + (Number(shippingFee.value) || 0) - (Number(discount.value) || 0);
});

async function loadVouchers() {
  try {
    const res = await fetch("http://localhost:8080/api/pgg/pos");
    const data = await res.json();
    vouchers.value = Array.isArray(data) ? data.map(normalizeVoucher) : [];
  } catch (error) {
    console.error("loadVouchers error:", error);
    vouchers.value = [];
  }
}

const eligibleVoucherEntries = computed(() => {
  return vouchers.value
    .map((v) => ({ v, discount: calcVoucherDiscount(safeSubtotal.value, v) }))
    .filter((x) => x.discount > 0)
    .sort((a, b) => b.discount - a.discount);
});

const bestEligibleVoucherEntry = computed(() => {
  return eligibleVoucherEntries.value[0] || null;
});

const appliedVoucherEntry = computed(() => {
  if (!appliedVoucherId.value) return null;
  const found = vouchers.value.find((v) => v.id === appliedVoucherId.value);
  if (!found) return null;
  return {
    v: found,
    discount: calcVoucherDiscount(safeSubtotal.value, found),
  };
});

const appliedVoucherDisplay = computed(() => {
  const entry = appliedVoucherEntry.value;
  if (!entry) return "";
  return `${entry.v.ma_giam_gia} - giảm ${money(entry.discount)} đ`;
});

function syncAppliedVoucher() {
  const eligible = eligibleVoucherEntries.value;

  if (!eligible.length) {
    appliedVoucherId.value = null;
    selectedVoucherId.value = null;
    discount.value = 0;
    return;
  }

  const current = eligible.find((x) => x.v.id === appliedVoucherId.value);
  const chosen = current || eligible[0];

  appliedVoucherId.value = chosen.v.id;
  discount.value = chosen.discount;

  if (!selectedVoucherId.value || !eligible.some((x) => x.v.id === selectedVoucherId.value)) {
    selectedVoucherId.value = chosen.v.id;
  }
}

watch(
  [safeSubtotal, shippingFee],
  () => {
    syncAppliedVoucher();
  },
  { immediate: true }
);

function openVoucherModal() {
  selectedVoucherId.value =
    appliedVoucherId.value || bestEligibleVoucherEntry.value?.v?.id || null;
  showVoucherModal.value = true;
}

function confirmVoucherSelection() {
  const picked = eligibleVoucherEntries.value.find(
    (x) => x.v.id === selectedVoucherId.value
  );

  if (!picked) {
    appliedVoucherId.value = null;
    discount.value = 0;
  } else {
    appliedVoucherId.value = picked.v.id;
    discount.value = picked.discount;
  }

  showVoucherModal.value = false;
}

async function fetchProvinces() {
  try {
    provinceLoading.value = true;
    const res = await fetch(`${PROVINCE_API_BASE}/p/`);
    const data = await res.json();
    provinces.value = Array.isArray(data) ? data : [];
  } catch (error) {
    console.error("fetchProvinces error:", error);
    provinces.value = [];
  } finally {
    provinceLoading.value = false;
  }
}

async function fetchDistrictsByProvince(provinceCode) {
  if (!provinceCode) {
    districts.value = [];
    return;
  }

  try {
    districtLoading.value = true;
    const res = await fetch(`${PROVINCE_API_BASE}/p/${provinceCode}?depth=2`);
    const data = await res.json();
    districts.value = Array.isArray(data?.districts) ? data.districts : [];
  } catch (error) {
    console.error("fetchDistrictsByProvince error:", error);
    districts.value = [];
  } finally {
    districtLoading.value = false;
  }
}

async function fetchWardsByDistrict(districtCode) {
  if (!districtCode) {
    wards.value = [];
    return;
  }

  try {
    wardLoading.value = true;
    const res = await fetch(`${PROVINCE_API_BASE}/d/${districtCode}?depth=2`);
    const data = await res.json();
    wards.value = Array.isArray(data?.wards) ? data.wards : [];
  } catch (error) {
    console.error("fetchWardsByDistrict error:", error);
    wards.value = [];
  } finally {
    wardLoading.value = false;
  }
}

async function onProvinceChange() {
  form.district = "";
  form.ward = "";
  districts.value = [];
  wards.value = [];

  errors.province = "";
  errors.district = "";
  errors.ward = "";

  if (form.province) {
    await fetchDistrictsByProvince(form.province);
  }
}

async function onDistrictChange() {
  form.ward = "";
  wards.value = [];

  errors.district = "";
  errors.ward = "";

  if (form.district) {
    await fetchWardsByDistrict(form.district);
  }
}

function money(v) {
  const n = Number(v) || 0;
  return n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
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
  const phoneRegex = /^(0|\\+84)[0-9]{9,10}$/;
  if (!phoneRegex.test(normalized)) return "Số điện thoại không hợp lệ";
  return "";
}

function validateProvince(value) {
  if (!value) return "Vui lòng chọn tỉnh/thành phố";
  return "";
}

function validateDistrict(value) {
  if (!value) return "Vui lòng chọn quận/huyện";
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
  errors.district = validateDistrict(form.district);
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
    () => form.district,
    (v) => {
      errors.district = validateDistrict(v);
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
  const provinceName = selectedProvince.value?.name || "";
  const districtName = selectedDistrict.value?.name || "";
  const wardName = selectedWard.value?.name || "";

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
    diaChiKhachHang: `${form.address.trim()}, ${wardName}, ${districtName}, ${provinceName}`,

    idPhieuGiamGia: appliedVoucherId.value,
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
    quanHuyenNhanHang: districtName,
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

    successMessage.value = data?.message || "Xác nhận thanh toán QR thành công";
    showSuccessModal.value = true;

    setTimeout(() => {
      router.push({
        path: "/checkout/success",
        query: {
          orderId: qrData.orderId || "",
        },
      });
    }, 1400);
  } catch (error) {
    console.error("CONFIRM QR ERROR:", error);
    qrError.value = error.message || "Xác nhận thanh toán thất bại";
  } finally {
    confirmingQr.value = false;
  }
}

function placeOrder() {
  if (!validateForm()) {
    errors.general = "Vui lòng kiểm tra lại thông tin đặt hàng";
    return;
  }

  showConfirmModal.value = true;
}

async function submitOrder() {
  try {
    confirmSubmitting.value = true;
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
      showConfirmModal.value = false;
      return;
    }

    const data = await checkoutApi(payload);

    showConfirmModal.value = false;

    if (form.paymentMethod === "qr") {
      openQrModal(data);
      return;
    }

    if (typeof clearCart === "function") {
      clearCart();
    }

    successMessage.value = data?.message || "Đặt hàng thành công";
    showSuccessModal.value = true;

    setTimeout(() => {
      router.push({
        path: "/checkout/success",
        query: {
          orderId: data?.orderId || "",
        },
      });
    }, 1400);
  } catch (error) {
    console.error("CHECKOUT ERROR:", error);
    console.error("CHECKOUT ERROR RAW:", error?.raw);

    errors.general = error.message || "Đặt hàng thất bại";
    showConfirmModal.value = false;
  } finally {
    confirmSubmitting.value = false;
    loading.value = false;
  }
}

function closeConfirmModal() {
  if (confirmSubmitting.value) return;
  showConfirmModal.value = false;
}

function closeSuccessModal() {
  showSuccessModal.value = false;
}

function onImgError(e) {
  e.target.src =
      "data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='120' height='120'%3E%3Crect width='100%25' height='100%25' fill='%23f1f3f5'/%3E%3Ctext x='50%25' y='52%25' dominant-baseline='middle' text-anchor='middle' fill='%2399a1aa' font-size='14'%3E%E1%BA%A2nh%3C/text%3E%3C/svg%3E";
}

onMounted(async () => {
  await Promise.all([fetchProvinces(), loadVouchers()]);
  syncAppliedVoucher();
});
</script>

<style scoped>
.checkout-page {
  --navy: #000f51;
  --navy-2: #0f2f98;
  --text: #0f172a;
  --muted: #64748b;
  --border: rgba(15, 23, 42, 0.08);
  background: linear-gradient(180deg, #f5f7fc 0%, #f3f4f8 100%);
  min-height: 100vh;
}

.checkout {
}

.checkout-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.page-title {
  font-size: 28px;
  font-weight: 750;
  color: var(--text);
  margin: 0;
}

.page-subtitle {
  color: var(--muted);
  font-size: 14px;
}

.checkout-badge {
  padding: 10px 14px;
  border-radius: 999px;
  background: #eef2ff;
  color: #1e3a8a;
  font-weight: 750;
  font-size: 14px;
}

.checkout-card,
.summary-card,
.checkout-benefits {
  background: #fff;
  border-radius: 22px;
  border: 1px solid var(--border);
  box-shadow: 0 16px 36px rgba(10, 24, 74, 0.06);
  overflow: hidden;
}

.checkout-card__header,
.summary-card__header {
  padding: 16px 20px;
  background: linear-gradient(90deg, var(--navy) 0%, var(--navy-2) 100%);
  color: #fff;
  font-weight: 750;
  font-size: 16px;
  letter-spacing: 0.2px;
}

.checkout-card__body,
.summary-card__body {
  padding: 20px;
}

.form-label {
  font-size: 15px;
  font-weight: 750;
  color: var(--text);
  margin-bottom: 8px;
}

.req {
  color: #dc2626;
}

.input-ui {
  min-height: 48px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid #dbe2ee;
  color: var(--text);
  font-size: 15px;
  padding-left: 14px;
  padding-right: 14px;
  box-shadow: none !important;
}

.input-ui:focus {
  border-color: #9db4ff;
  background: #fff;
}

.form-select.input-ui {
  padding-right: 40px;
  cursor: pointer;
}

.form-select.input-ui:disabled {
  background-color: #f1f5f9;
  cursor: not-allowed;
  opacity: 1;
}

.textarea-ui {
  min-height: 110px;
  padding-top: 12px;
}

.option-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  border-radius: 16px;
  margin: 0;
}

.option-row + .option-row {
  margin-top: 12px;
}

.option-row__left {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text);
}

.option-row__title {
  font-weight: 750;
}

.option-row__value {
  color: var(--muted);
  font-weight: 750;
}

.coupon-box {
  display: grid;
  grid-template-columns: 1fr 120px;
  gap: 12px;
}

.coupon-btn {
  min-height: 48px;
  border: none;
  border-radius: 16px;
  background: #000f51;
  color: #fff;
  font-weight: 750;
  box-shadow: 0 14px 28px rgba(0, 15, 81, 0.18);
  transition: all 0.2s ease;
}

.coupon-btn:hover {
  background: #001a72;
}

.invoice-row {
  display: flex;
  align-items: center;
  color: var(--text);
  font-weight: 750;
}

.btn-order {
  min-height: 52px;
  border: none;
  border-radius: 16px;
  background: #000f51;
  color: #fff;
  font-weight: 750;
  box-shadow: 0 14px 28px rgba(0, 15, 81, 0.18);
  transition: all 0.2s ease;
}

.btn-order:hover:not(:disabled) {
  background: #001a72;
}

.btn-order:disabled {
  opacity: 0.6;
}

.summary-empty {
  padding: 20px;
  color: var(--muted);
}

.summary-list {
  display: grid;
  gap: 14px;
}

.summary-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  padding: 14px;
  border-radius: 18px;
  background: #fbfcff;
  border: 1px solid rgba(148, 163, 184, 0.14);
}

.summary-item__left {
  display: flex;
  gap: 12px;
  min-width: 0;
  flex: 1;
}

.summary-item__img {
  width: 84px;
  height: 84px;
  object-fit: cover;
  border-radius: 14px;
  background: #f1f3f5;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.summary-item__info {
  min-width: 0;
}

.summary-item__name {
  font-size: 15px;
  font-weight: 750;
  color: var(--text);
  line-height: 1.45;
  margin-bottom: 4px;
}

.summary-item__meta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  color: var(--muted);
  font-size: 14px;
  margin-bottom: 4px;
}

.summary-item__spct,
.summary-item__qty {
  font-size: 13px;
  color: #475569;
}

.summary-item__right {
  text-align: right;
  min-width: 110px;
}

.summary-item__price {
  color: #475569;
  font-size: 14px;
  margin-bottom: 6px;
}

.summary-item__total {
  color: var(--navy);
  font-size: 16px;
  font-weight: 750;
}

.summary-footer {
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px dashed rgba(148, 163, 184, 0.35);
}

.sum-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  color: #334155;
}

.sum-line.total {
  margin-top: 4px;
}

.sum-line.total strong {
  color: var(--navy);
  font-size: 20px;
  font-weight: 750;
}

.checkout-benefits {
  padding: 16px 18px;
  display: grid;
  gap: 12px;
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #475569;
  font-weight: 750;
}

.benefit-item i {
  color: var(--navy);
}

.qr-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(2, 6, 23, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
  padding: 16px;
}

.qr-modal {
  width: 100%;
  max-width: 520px;
  background: #fff;
  border-radius: 22px;
  overflow: hidden;
  box-shadow: 0 24px 48px rgba(2, 6, 23, 0.28);
}

.qr-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 20px;
  background: linear-gradient(90deg, var(--navy) 0%, var(--navy-2) 100%);
  color: #fff;
}

.qr-modal-body {
  padding: 20px;
}

.qr-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 0 20px 20px;
}

.qr-image {
  max-width: 100%;
  max-height: 320px;
  border-radius: 14px;
}

.qr-placeholder {
  border: 1px dashed #cbd5e1;
  background: #f8fafc;
  border-radius: 14px;
  padding: 32px 16px;
  color: var(--muted);
}

.qr-info {
  display: grid;
  gap: 8px;
  color: var(--text);
  font-size: 14px;
}

@media (max-width: 991.98px) {
  .summary-item {
    flex-direction: column;
  }

  .summary-item__right {
    width: 100%;
    text-align: left;
    min-width: 0;
  }
}

@media (max-width: 767.98px) {
  .coupon-box {
    grid-template-columns: 1fr;
  }
}

.confirm-backdrop,
.success-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(2, 6, 23, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1060;
  padding: 16px;
}

.confirm-modal,
.success-modal {
  width: 100%;
  max-width: 460px;
  background: #fff;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 24px 60px rgba(2, 6, 23, 0.28);

}

.confirm-modal__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 20px;
  background: linear-gradient(90deg, #000f51 0%, #0f2f98 100%);
  color: #fff;
}

.confirm-modal__header h5 {
  margin: 0;
  color: #fff;
  font-size: 18px;
  font-weight: 750;
}

.confirm-modal__body {
  padding: 28px 22px 20px;
  text-align: center;
}

.confirm-icon {
  width: 72px;
  height: 72px;
  margin: 0 auto 16px;
  border-radius: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eef2ff;
  color: var(--navy);
  font-size: 30px;

}

.confirm-title {
  font-size: 22px;
  font-weight: 750;
  color: #0f172a;
  margin-bottom: 10px;
}

.confirm-desc {
  color: var(--muted);
  line-height: 1.7;
  font-size: 15px;
}

.confirm-desc strong {
  color: #000f51;
  font-weight: 750;
}

.confirm-modal__footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding: 0 22px 22px;
}

.btn-cancel,
.btn-confirm {
  min-height: 46px;
  border-radius: 14px;
  padding: 0 18px;
  font-weight: 750;
}

.btn-cancel {
  border: 1px solid #d8dfec;
  background: #fff;
  color: var(--text);
}

.btn-confirm {
  border: none;
  background: #000f51;
  color: #fff;
  box-shadow: 0 14px 28px rgba(0, 15, 81, 0.18);
  transition: all 0.2s ease;
}

.btn-confirm:hover:not(:disabled) {
  background: #001a72;
}

.success-modal {
  padding: 30px 22px;
  text-align: center;
}

.success-icon-wrap {
  display: flex;
  justify-content: center;
  margin-bottom: 14px;
}

.success-icon {
  width: 82px;
  height: 82px;
  border-radius: 50%;
  background: linear-gradient(135deg, #16a34a 0%, #22c55e 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 38px;
  box-shadow: 0 18px 32px rgba(34, 197, 94, 0.24);
}

.success-title {
  font-size: 26px;
  font-weight: 750;
  color: var(--text);
  margin-bottom: 10px;
}

.success-desc {
  font-size: 15px;
  color: #334155;
  line-height: 1.7;
  margin-bottom: 10px;
}

.success-note {
  color: #64748b;
  font-size: 14px;
}
.btn-qr-close,
.btn-qr-confirm {
  min-height: 44px;
  border-radius: 14px;
  padding: 0 18px;
  font-weight: 750;
  transition: all 0.2s ease;
}

.btn-qr-close {
  border: 1px solid #d8dfec;
  background: #fff;
  color: #0f172a;
}

.btn-qr-close:hover {
  border-color: #001a72;
  color: #001a72;
}

.btn-qr-confirm {
  border: none;
  background: #000f51;
  color: #fff;
  box-shadow: 0 14px 28px rgba(0, 15, 81, 0.18);
}

.btn-qr-confirm:hover:not(:disabled) {
  background: #001a72;
}

.btn-qr-confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.voucher-modal {
  width: 100%;
  max-width: 560px;
  background: #fff;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 24px 60px rgba(2, 6, 23, 0.28);
}

.voucher-modal__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 20px;
  background: linear-gradient(90deg, #000f51 0%, #0f2f98 100%);
  color: #fff;
}

.voucher-modal__header h5 {
  margin: 0;
  font-size: 18px;
  font-weight: 750;
}

.voucher-modal__close {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
  font-size: 22px;
  line-height: 1;
}

.voucher-modal__body {
  padding: 18px 20px;
  display: grid;
  gap: 12px;
  max-height: 420px;
  overflow: auto;
}

.voucher-modal__footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 0 20px 20px;
}

.voucher-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 16px;
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  background: #fff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.voucher-item:hover {
  border-color: #f59e0b;
  background: #fffaf0;
}

.voucher-item.active {
  border-color: #f97316;
  background: #fff1f2;
  box-shadow: inset 0 0 0 1px rgba(249, 115, 22, 0.18);
}

.voucher-item__left {
  min-width: 0;
}

.voucher-item__code {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 17px;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 4px;
}

.voucher-item__name {
  color: #475569;
  font-size: 14px;
  margin-bottom: 6px;
}

.voucher-item__meta {
  color: #64748b;
  font-size: 13px;
}

.voucher-item__right {
  min-width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #ef4444;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.badge-best {
  display: inline-flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 999px;
  background: #dcfce7;
  color: #166534;
  font-size: 11px;
  font-weight: 800;
}
</style>