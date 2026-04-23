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
                <div class="col-12">
                  <label class="form-label">
                    Email <span class="req">*</span>
                  </label>
                  <input
                    v-model="form.email"
                    type="email"
                    class="form-control input-ui"
                    placeholder="Nhập email"
                  />
                  <div v-if="errors.email" class="text-danger small mt-1">
                    {{ errors.email }}
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
                      {{
                        provinceLoading
                          ? "Đang tải tỉnh/thành..."
                          : "Chọn tỉnh/thành phố"
                      }}
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
                    Phường/Xã/Đặc khu <span class="req">*</span>
                  </label>
                  <select
                    v-model="form.ward"
                    class="form-select input-ui"
                    :disabled="!form.province || wardLoading"
                  >
                    <option value="">
                      {{
                        wardLoading
                          ? "Đang tải phường/xã..."
                          : "Chọn phường/xã/đặc khu"
                      }}
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
                <span class="option-row__value"
                  >+ {{ money(shippingFee) }} đ</span
                >
              </label>
            </div>
          </section>

          <section class="checkout-card mb-4">
            <div class="checkout-card__header">
              <span>3. Phương thức thanh toán</span>
            </div>

            <div class="checkout-card__body">
              <label
                v-for="opt in paymentOptions"
                :key="opt.value"
                class="payment-option"
                :class="{ active: form.paymentMethod === opt.value }"
              >
                <div class="payment-option__left">
                  <input
                    class="form-check-input"
                    type="radio"
                    :value="opt.value"
                    v-model="form.paymentMethod"
                  />
                  <div class="payment-option__content">
                    <div class="payment-option__title-row">
                      <span class="payment-option__title">{{ opt.title }}</span>
                      <span v-if="opt.badge" class="payment-option__badge">
                        {{ opt.badge }}
                      </span>
                    </div>
                    <div class="payment-option__desc">{{ opt.desc }}</div>
                  </div>
                </div>

                <i
                  class="bi"
                  :class="
                    form.paymentMethod === opt.value
                      ? 'bi-check-circle-fill'
                      : 'bi-circle'
                  "
                ></i>
              </label>

              <div class="payment-helper mt-3">
                <i class="bi bi-info-circle"></i>
                <span>{{
                  selectedPaymentOption?.helper ||
                  "Vui lòng chọn phương thức thanh toán"
                }}</span>
              </div>

              <div v-if="errors.paymentMethod" class="text-danger small mt-2">
                {{ errors.paymentMethod }}
              </div>
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
              <div
                v-if="appliedVoucherEntry"
                class="small mt-2"
                :class="
                  appliedVoucherEntry.v.id === bestEligibleVoucherEntry?.v?.id
                    ? 'text-primary'
                    : 'text-primary'
                "
              >
                <template
                  v-if="
                    appliedVoucherEntry.v.id === bestEligibleVoucherEntry?.v?.id
                  "
                >
                  Áp dụng mã tốt nhất:
                </template>
                <template v-else> Đang áp dụng: </template>
                <b>{{ appliedVoucherEntry.v.ma_giam_gia }}</b>
                - giảm {{ money(appliedVoucherEntry.discount) }} đ
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
                  :key="
                    it.idSanPhamChiTiet ||
                    it.key ||
                    `${it.productId}-${it.size}-${it.color}`
                  "
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
                      {{
                        money((Number(it.price) || 0) * (Number(it.qty) || 0))
                      }}
                      đ
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
                <div class="sum-line shipping-line">
                  <div class="shipping-label">
                    <span>Vận chuyển</span>
                    <img :src="ghnLogo" alt="GHN" class="ship-fee-logo" />
                  </div>
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

                <div class="summary-actions mt-3">
                  <label class="invoice-row">
                    <input
                      v-model="form.invoice"
                      type="checkbox"
                      class="form-check-input me-2"
                    />
                    <span>Xuất file PDF sau khi đặt hàng</span>
                  </label>

                  <button
                    class="btn-preview-pdf"
                    type="button"
                    :disabled="cartItems.length === 0"
                    @click="printInvoice"
                  >
                    <i class="bi bi-printer me-2"></i>
                    Xuất PDF / In tạm tính
                  </button>
                </div>
                <div
                  v-if="appliedVoucherEntry"
                  class="voucher-saving-note mt-3"
                >
                  Bạn đang tiết kiệm <b>{{ money(discount) }} đ</b> với mã
                  <b>{{ appliedVoucherEntry.v.ma_giam_gia }}</b
                  >.
                </div>
                <div
                  v-if="errors.items"
                  class="alert alert-danger py-2 mt-3 mb-2"
                >
                  {{ errors.items }}
                </div>

                <div
                  v-if="errors.general"
                  class="alert alert-danger py-2 mt-2 mb-0"
                >
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
            <h5 class="mb-0">Quét QR để thanh toán</h5>
            <button
              class="btn-close"
              type="button"
              @click="closeQrModal"
            ></button>
          </div>

          <div class="qr-modal-body">
            <div class="payment-channel-badge mb-3">
              {{ selectedPaymentOption?.title || "Thanh toán online" }}
            </div>

            <template v-if="form.paymentMethod === 'bank_qr'">
  <div class="text-center mb-3">
    <img
      v-if="qrData.qrImageUrl"
      :src="normalizeQrUrl(qrData.qrImageUrl)"
      alt="QR thanh toán"
      class="qr-image"
    />
    <div v-else class="qr-placeholder">Chưa có ảnh QR</div>
  </div>

  <div class="qr-info-card">
    <div class="qr-info-row">
      <span class="qr-info-label">Mã đơn</span>
      <span class="qr-info-value">{{ qrData.maHoaDon || "-" }}</span>
    </div>

    <div class="qr-info-row qr-info-row--amount">
      <span class="qr-info-label">Số tiền</span>
      <span class="qr-amount">{{ money(qrData.amount || safeGrandTotal) }} đ</span>
    </div>
  </div>

  <div class="mt-3">
    <label class="form-label qr-form-label">Ghi chú xác nhận</label>
    <textarea
      v-model="qrForm.ghiChu"
      class="form-control input-ui qr-note-textarea"
      rows="3"
      placeholder="Khách đã thanh toán"
    ></textarea>
  </div>
</template>

            <template v-else>
              <div class="gateway-box">
                <div class="gateway-box__title">
                  Thanh toán qua {{ selectedPaymentOption?.title }}
                </div>

                <div class="gateway-box__desc">
                  {{ gatewayInstruction }}
                </div>

                <div class="gateway-box__meta">
                  <div><b>Mã đơn:</b> {{ qrData.maHoaDon || "-" }}</div>
                  <div>
                    <b>Số tiền:</b>
                    {{ money(qrData.amount || safeGrandTotal) }} đ
                  </div>
                </div>

                <button
                  class="btn-open-gateway mt-3"
                  type="button"
                  @click="openPaymentGateway"
                >
                  Mở trang thanh toán
                </button>

                <div class="mt-3">
                  <label class="form-label">Mã giao dịch / mã tham chiếu</label>
                  <input
                    v-model="qrForm.maGiaoDich"
                    type="text"
                    class="form-control input-ui"
                    placeholder="Ví dụ: VNPAY123456 / MOMO123456"
                  />
                </div>

                <div class="mt-3">
                  <label class="form-label">Ghi chú xác nhận</label>
                  <textarea
                    v-model="qrForm.ghiChu"
        class="form-control input-ui qr-note-textarea"
                    rows="3"
                    placeholder="Đã thanh toán online thành công"
                  ></textarea>
                </div>
              </div>
            </template>

            <div v-if="qrSuccessMessage" class="alert alert-success mt-3 mb-0">
              {{ qrSuccessMessage }}
            </div>

            <div v-if="qrError" class="alert alert-danger mt-3 mb-0">
              {{ qrError }}
            </div>
          </div>

          <div class="qr-modal-footer">
            <button class="btn-qr-close" type="button" @click="closeQrModal">
              Đóng
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

      <div class="success-note">Đang chuyển sang trang kết quả đơn hàng...</div>
    </div>
  </div>
  <div
    v-if="showVoucherModal"
    class="confirm-backdrop"
    @click.self="showVoucherModal = false"
  >
    <div class="voucher-modal">
      <div class="voucher-modal__header">
        <h5>Chọn phiếu giảm giá</h5>
        <button
          type="button"
          class="voucher-modal__close"
          @click="showVoucherModal = false"
        >
          ×
        </button>
      </div>

      <div class="voucher-modal__body">
        <div v-if="eligibleVoucherEntries.length === 0" class="text-muted">
          Không có phiếu giảm giá phù hợp với đơn hàng này.
        </div>

        <template v-if="publicEligibleVoucherEntries.length">
          <div class="voucher-group-title">Mã công khai có thể dùng</div>

          <label
            v-for="e in publicEligibleVoucherEntries"
            :key="`public-${e.v.id}`"
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
                <span class="badge-public">Công khai</span>
                <span
                  v-if="bestEligibleVoucherEntry?.v?.id === e.v.id"
                  class="badge-best"
                >
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
        </template>

        <template v-if="personalEligibleVoucherEntries.length">
          <div class="voucher-group-title mt-2">Mã cá nhân của bạn</div>

          <label
            v-for="e in personalEligibleVoucherEntries"
            :key="`personal-${e.v.id}`"
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
                <span
                  v-if="bestEligibleVoucherEntry?.v?.id === e.v.id"
                  class="badge-best"
                >
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
        </template>
      </div>

      <div class="voucher-modal__footer">
        <button
          type="button"
          class="btn btn-secondary"
          @click="showVoucherModal = false"
        >
          Đóng
        </button>
        <button
          type="button"
          class="btn btn-success"
          @click="confirmVoucherSelection"
        >
          Áp dụng
        </button>
      </div>
    </div>
  </div>
  <div class="app-toast-wrap">
    <transition name="fade">
      <div
        v-if="toast.show"
        class="app-toast"
        :class="`app-toast--${toast.type}`"
      >
        <i
          class="bi"
          :class="
            toast.type === 'success'
              ? 'bi-check-circle-fill'
              : toast.type === 'warning'
                ? 'bi-exclamation-triangle-fill'
                : 'bi-info-circle-fill'
          "
        ></i>
        <span>{{ toast.message }}</span>
      </div>
    </transition>
  </div>

  <div ref="printAreaRef" class="invoice-print-area">
    <div class="receipt">
      <div class="center bold big">HÓA ĐƠN TẠM TÍNH</div>
      <div class="center small muted">Mã đơn: {{ invoiceData.maHoaDon }}</div>
      <div class="center small muted">{{ invoiceData.createdAt }}</div>

      <div class="hr"></div>

      <div class="mt2"><b>Khách hàng:</b> {{ invoiceData.customerName }}</div>
      <div class="mt2"><b>SĐT:</b> {{ invoiceData.phone }}</div>
      <div class="mt2"><b>Địa chỉ:</b> {{ invoiceData.address }}</div>
      <div class="mt2"><b>Thanh toán:</b> {{ invoiceData.paymentLabel }}</div>
      <div class="mt2"><b>Ghi chú:</b> {{ invoiceData.note }}</div>

      <div class="hr"></div>

      <div class="items-head bold">
        <div class="w-name">Sản phẩm</div>
        <div class="w-qty right">SL</div>
        <div class="w-price right">Tiền</div>
      </div>

      <div class="item mt6" v-for="it in invoiceData.items" :key="it.key">
        <div class="w-name">
          <div>{{ it.name }}</div>
          <div class="small muted">{{ it.variantText }}</div>
        </div>
        <div class="w-qty right">{{ it.qty }}</div>
        <div class="w-price right">{{ money(it.total) }}</div>
      </div>

      <div class="hr"></div>

      <div class="row2">
        <span>Tạm tính</span>
        <span>{{ money(invoiceData.subtotal) }}</span>
      </div>
      <div class="row2">
        <span>Vận chuyển</span>
        <span>{{ money(invoiceData.shippingFee) }}</span>
      </div>
      <div class="row2">
        <span>Giảm giá</span>
        <span>-{{ money(invoiceData.discount) }}</span>
      </div>
      <div class="row2 bold mt6">
        <span>Thành tiền</span>
        <span>{{ money(invoiceData.total) }}</span>
      </div>

      <div class="hr"></div>
      <div class="center small mt8">Cảm ơn quý khách đã mua hàng!</div>
    </div>
    <ChatWidget />
  </div>
</template>

<script setup>
import {
  reactive,
  ref,
  computed,
  watch,
  onMounted,
  onUnmounted,
  nextTick,
} from "vue";
import { useRouter } from "vue-router";
import { useCart } from "../../composables/useCart";
import ghnLogo from "../../assets/ghn.webp.webp";
import vnUnitsData from "../../assets/vn_units.json"; // sửa path nếu file của bạn nằm chỗ khác
import ChatWidget from "../../components/ClientChatWidget.vue";
const router = useRouter();
const { cartItems, clearCart } = useCart();
const API_BASE = import.meta.env.VITE_API_BASE_URL || "";
const PUBLIC_WEB_ORIGIN =
  import.meta.env.VITE_PUBLIC_WEB_ORIGIN || window.location.origin;
import QRCode from "qrcode";
const provinces = ref([]);
const wards = ref([]);
const vestUser = JSON.parse(localStorage.getItem("vest_user") || "null");
const currentCustomerId = Number(vestUser?.id || 0) || null;
const provinceLoading = ref(false);
const wardLoading = ref(false);
const form = reactive({
  fullName: "",
  phone: "",
  email: "",
  province: "",
  ward: "",
  address: "",
  note: "",
  shippingMethod: "standard",
  paymentMethod: "cod",
  invoice: false,
});
const paymentOptions = [
  {
    value: "cod",
    title: "Thanh toán khi nhận hàng (COD)",
    desc: "Kiểm tra hàng rồi thanh toán cho shipper.",
    badge: "An toàn",
    helper: "Bạn chỉ cần xác nhận đơn, thanh toán sau khi nhận hàng.",
  },
  {
    value: "bank_qr",
    title: "Quét QR thanh toán",
    desc: "Quét mã QR để mở trang thanh toán giả lập.",
    badge: "Nhanh",
    helper: "Quét QR bằng điện thoại để mở mock-payment và thanh toán.",
  },
  {
    value: "vnpay",
    title: "VNPAY",
    desc: "ATM nội địa / QR / Internet Banking.",
    badge: "Nhanh",
    helper: "Chuyển hướng sang trang thánh toán của VNPAY",
  },
];
const selectedPaymentOption = computed(() => {
  return (
    paymentOptions.find((x) => x.value === form.paymentMethod) ||
    paymentOptions[0]
  );
});

const gatewayInstruction = computed(() => {
  if (form.paymentMethod === "vnpay") {
    return "Bạn sẽ được chuyển sang cổng thanh toán VNPAY để hoàn tất giao dịch.";
  }

  return "Quét mã QR và nhập mã giao dịch để xác nhận.";
});
const paymentUiHint = computed(() => {
  if (form.paymentMethod === "cod") {
    return "Thanh toán khi nhận hàng.";
  }
 if (form.paymentMethod === "bank_qr") {
  return "Quét mã QR để mở trang thanh toán giả lập.";
}
  if (form.paymentMethod === "vnpay") {
    return "Thanh toán an toàn qua cổng VNPAY.";
  }

  return "Vui lòng chọn phương thức thanh toán.";
});
const vouchers = ref([]);
const showVoucherModal = ref(false);

const selectedVoucherId = ref(null); // đang tick trong modal
const appliedVoucherId = ref(null); // đang áp dụng thật
const discount = ref(0);
const loading = ref(false);

const placedOrder = ref(null);
const placedOrderItems = ref([]);
const printAreaRef = ref(null);
const qrSuccessMessage = ref("");

const toast = reactive({
  show: false,
  message: "",
  type: "success",
});

let toastTimer = null;

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
  paymentUrl: "",
  provider: "",
});

const qrForm = reactive({
  maGiaoDich: "",
  ghiChu: "Khách đã thanh toán",
});

const errors = reactive({
  fullName: "",
  phone: "",
  email: "",
  province: "",
  ward: "",
  address: "",
  shippingMethod: "",
  paymentMethod: "",
  items: "",
  general: "",
});
const selectedProvince = computed(() => {
  return (
    provinces.value.find((p) => String(p.code) === String(form.province)) ||
    null
  );
});

const selectedWard = computed(() => {
  return wards.value.find((w) => String(w.code) === String(form.ward)) || null;
});

const shippingFee = computed(() => {
  const provinceName = String(selectedProvince.value?.name || "").toLowerCase();
  const wardName = String(selectedWard.value?.name || "").toLowerCase();

  if (!provinceName || !wardName) return 0;

  let fee = 30000;

  if (provinceName.includes("hà nội") || provinceName.includes("hồ chí minh")) {
    fee = 25000;
  }

  if (wardName.includes("xã") || wardName.includes("đặc khu")) {
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
function validateEmail(value) {
  if (!value?.trim()) return "Vui lòng nhập email";
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(value.trim())) return "Email không hợp lệ";
  return "";
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
    gia_tri_giam_toi_da: Number(
      x.giaTriGiamToiDa ?? x.gia_tri_giam_toi_da ?? 0,
    ),
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
  return (
    safeSubtotal.value +
    (Number(shippingFee.value) || 0) -
    (Number(discount.value) || 0)
  );
});
const fullDeliveryAddress = computed(() => {
  return [
    form.address?.trim(),
    selectedWard.value?.name || "",
    selectedProvince.value?.name || "",
  ]
    .filter((x) => !!x)
    .join(", ");
});

const invoiceData = computed(() => {
  const currentOrder = placedOrder.value || {};
  const sourceItems = placedOrderItems.value.length
    ? placedOrderItems.value
    : cartItems.value;

  return {
    maHoaDon:
      currentOrder.maHoaDon ||
      currentOrder.orderCode ||
      currentOrder.orderId ||
      "TAM_TINH",
    createdAt: currentOrder.createdAt || new Date().toLocaleString("vi-VN"),
    customerName:
      currentOrder.customerName || form.fullName?.trim() || "Khách lẻ",
    phone: currentOrder.phone || form.phone?.trim() || "-",
    address: currentOrder.address || fullDeliveryAddress.value || "-",
    paymentLabel:
      currentOrder.paymentLabel ||
      selectedPaymentOption.value?.title ||
      "Thanh toán khi nhận hàng",
    note: currentOrder.note || form.note?.trim() || "-",
    subtotal: Number(currentOrder.subtotal ?? safeSubtotal.value) || 0,
    shippingFee: Number(currentOrder.shippingFee ?? shippingFee.value) || 0,
    discount: Number(currentOrder.discount ?? discount.value) || 0,
    total: Number(currentOrder.total ?? safeGrandTotal.value) || 0,
    items: sourceItems.map((it, index) => {
      const qty = Number(it.qty) || 0;
      const price = Number(it.price) || 0;
      return {
        key: resolveProductDetailId(it) || it.id || index,
        name: it.name || "Sản phẩm",
        variantText: [
          it.color ? `Màu: ${it.color}` : "",
          it.size ? `Size: ${it.size}` : "",
        ]
          .filter(Boolean)
          .join(" | "),
        qty,
        price,
        total: qty * price,
      };
    }),
  };
});

function showToast(message, type = "success") {
  if (!message) return;
  if (toastTimer) clearTimeout(toastTimer);

  toast.message = message;
  toast.type = type;
  toast.show = true;

  toastTimer = setTimeout(() => {
    toast.show = false;
  }, 2800);
}
async function loadVouchers() {
  try {
    const customerId = null;
    const url = customerId
      ? `${API_BASE}/api/pgg/pos?khachHangId=${customerId}`
      : `${API_BASE}/api/pgg/pos`;

    const res = await fetch(url);
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
const publicEligibleVoucherEntries = computed(() => {
  return eligibleVoucherEntries.value.filter(
    (x) => x.v.loai_phieu === "CONG_KHAI",
  );
});

const personalEligibleVoucherEntries = computed(() => {
  return eligibleVoucherEntries.value.filter(
    (x) => x.v.loai_phieu === "CA_NHAN",
  );
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

  if (
    !selectedVoucherId.value ||
    !eligible.some((x) => x.v.id === selectedVoucherId.value)
  ) {
    selectedVoucherId.value = chosen.v.id;
  }
}

watch(
  [safeSubtotal, shippingFee],
  () => {
    syncAppliedVoucher();
  },
  { immediate: true },
);

function openVoucherModal() {
  selectedVoucherId.value =
    appliedVoucherId.value || bestEligibleVoucherEntry.value?.v?.id || null;
  showVoucherModal.value = true;
}

function confirmVoucherSelection() {
  const picked = eligibleVoucherEntries.value.find(
    (x) => x.v.id === selectedVoucherId.value,
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

    provinces.value = (Array.isArray(vnUnitsData) ? vnUnitsData : []).map(
      (p) => ({
        code: p.Code,
        name: p.FullName,
        wards: (p.Wards || []).map((w) => ({
          code: w.Code,
          name: w.FullName,
          provinceCode: w.ProvinceCode,
        })),
      }),
    );
  } catch (error) {
    console.error("fetchProvinces error:", error);
    provinces.value = [];
  } finally {
    provinceLoading.value = false;
  }
}

async function fetchWardsByProvince(provinceCode) {
  if (!provinceCode) {
    wards.value = [];
    return;
  }

  try {
    wardLoading.value = true;

    const province = provinces.value.find(
      (p) => String(p.code) === String(provinceCode),
    );

    wards.value = province?.wards || [];
  } catch (error) {
    console.error("fetchWardsByProvince error:", error);
    wards.value = [];
  } finally {
    wardLoading.value = false;
  }
}

async function onProvinceChange() {
  form.ward = "";
  wards.value = [];

  errors.province = "";
  errors.ward = "";

  if (form.province) {
    await fetchWardsByProvince(form.province);
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
  const allowed = ["cod", "bank_qr", "vnpay"];
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
  errors.email = validateEmail(form.email);
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
  },
);

watch(
  () => form.phone,
  (v) => {
    errors.phone = validatePhone(v);
  },
);
watch(
  () => form.email,
  (v) => {
    errors.email = validateEmail(v);
  },
);

watch(
  () => form.province,
  (v) => {
    errors.province = validateProvince(v);
  },
);

watch(
  () => form.ward,
  (v) => {
    errors.ward = validateWard(v);
  },
);

watch(
  () => form.address,
  (v) => {
    errors.address = validateAddress(v);
  },
);

watch(
  () => form.paymentMethod,
  (v) => {
    errors.paymentMethod = validatePaymentMethod(v);
  },
);

function buildOrderPayload() {
  const provinceName = selectedProvince.value?.name || "";
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

    idKhachHang: currentCustomerId,
    tenKhachHang: form.fullName.trim(),
    soDienThoai: form.phone.trim(),
    emailKhachHang: form.email.trim(),
    diaChiKhachHang: `${form.address.trim()}, ${wardName}, ${provinceName}`,

    idPhieuGiamGia: appliedVoucherId.value,
    giamThuCongPercent: 0,
    paid: 0,
    ghiChu: form.note?.trim() || "Khách đặt hàng online",

    paymentMethod:
      form.paymentMethod === "cod"
        ? "COD"
        : form.paymentMethod === "bank_qr"
          ? "QR"
          : form.paymentMethod.toUpperCase(),
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
  const token =
    localStorage.getItem("USER_ACCESS_TOKEN") ||
    sessionStorage.getItem("USER_ACCESS_TOKEN") ||
    localStorage.getItem("vest_token");

  const response = await fetch(`${API_BASE}/api/online-checkout`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
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
async function createVnpayPaymentUrlApi(orderId) {
  const token =
    localStorage.getItem("USER_ACCESS_TOKEN") ||
    sessionStorage.getItem("USER_ACCESS_TOKEN") ||
    localStorage.getItem("vest_token");

  const response = await fetch(
    `${API_BASE}/api/online-checkout/${orderId}/vnpay-payment-url`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
    },
  );

  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    const err = new Error(data?.message || "Không tạo được link thanh toán VNPAY");
    err.raw = data;
    throw err;
  }

  return data;
}

async function redirectToVnpay(orderData) {
  const orderId = orderData?.orderId || orderData?.id || null;

  if (!orderId) {
    throw new Error("Không tìm thấy mã đơn hàng để tạo link VNPAY");
  }

  qrData.orderId = orderId;
  qrData.maHoaDon =
    orderData?.maHoaDon || orderData?.orderCode || String(orderId);
  qrData.amount = Number(orderData?.amount ?? safeGrandTotal.value) || 0;
  qrData.provider = "vnpay";

  const vnpayData = await createVnpayPaymentUrlApi(orderId);

  qrData.paymentUrl =
    vnpayData?.paymentUrl ||
    vnpayData?.vnpayUrl ||
    vnpayData?.redirectUrl ||
    "";

  qrData.amount = Number(vnpayData?.amount ?? qrData.amount) || 0;

  if (!qrData.paymentUrl) {
    throw new Error("Backend chưa trả về link VNPAY");
  }

sessionStorage.setItem(
  "pending_checkout_customer_info",
  JSON.stringify({
    orderId,
    maHoaDon: qrData.maHoaDon,
    customerName: form.fullName?.trim() || "",
    phone: form.phone?.trim() || "",
    email: form.email?.trim() || "",
    address: fullDeliveryAddress.value || "",
    paymentMethod: "vnpay",
    paymentLabel: "VNPAY",
    total: Number(qrData.amount || 0),
  })
);
  window.location.href = qrData.paymentUrl;
}
async function confirmQrPaymentApi(orderId, payload) {
  const response = await fetch(
    `${API_BASE}/api/online-checkout/${orderId}/confirm-payment`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    },
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
  if (url.startsWith("data:image/")) return url;
  if (url.startsWith("http://") || url.startsWith("https://")) return url;
  return `http://localhost:8080${url}`;
}
function normalizePaymentUrl(url) {
  if (!url) return "";

  const rawUrl = String(url).trim();
  const frontendOrigin = window.location.origin;
  const backendOrigin = "http://localhost:8080";

  if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
    try {
      const parsed = new URL(rawUrl);

      // mock-payment là route của FE -> luôn ép về origin hiện tại của FE
      if (parsed.pathname.startsWith("/mock-payment")) {
        return `${frontendOrigin}${parsed.pathname}${parsed.search}${parsed.hash}`;
      }

      return parsed.toString();
    } catch (error) {
      return rawUrl;
    }
  }

  // backend trả path tương đối cho mock-payment
  if (rawUrl.startsWith("/mock-payment")) {
    return `${frontendOrigin}${rawUrl}`;
  }

  // các URL tương đối khác thì giữ backend origin
  return `${backendOrigin}${rawUrl.startsWith("/") ? rawUrl : `/${rawUrl}`}`;
}


async function buildMockPaymentQr(data) {
  const paymentUrl =
    `${PUBLIC_WEB_ORIGIN}/mock-payment` +
    `?orderId=${encodeURIComponent(data?.orderId || data?.id || "")}` +
    `&method=${encodeURIComponent("BANK_QR")}` +
    `&amount=${encodeURIComponent(Number(data?.amount || safeGrandTotal.value || 0))}` +
    `&maHoaDon=${encodeURIComponent(data?.maHoaDon || data?.orderCode || "")}` +
    `&customerName=${encodeURIComponent(form.fullName || "")}` +
    `&phone=${encodeURIComponent(form.phone || "")}` +
    `&address=${encodeURIComponent(fullDeliveryAddress.value || "")}`;

  const qrImageUrl = await QRCode.toDataURL(paymentUrl, {
    width: 300,
    margin: 2,
  });

  return { paymentUrl, qrImageUrl };
}
async function openQrModal(data) {
  qrData.orderId = data?.orderId || data?.id || null;
  qrData.maHoaDon = data?.maHoaDon || data?.orderCode || "";
  qrData.amount = Number(data?.amount) || Number(safeGrandTotal.value) || 0;
  qrData.provider = form.paymentMethod;

  const mockQr = await buildMockPaymentQr(data);

  qrData.paymentUrl = mockQr.paymentUrl;
  qrData.qrImageUrl = mockQr.qrImageUrl;

  qrForm.maGiaoDich = "";
  qrForm.ghiChu = "Khách đã thanh toán";
  qrError.value = "";
  qrSuccessMessage.value = "";
  showQrModal.value = true;

  startPaymentPolling();
  showToast("Đơn hàng đã được tạo. Quét QR để mở trang thanh toán.", "success");
}
function closeQrModal() {
  if (confirmingQr.value) return;
  stopPaymentPolling();
  showQrModal.value = false;
  qrError.value = "";
  qrSuccessMessage.value = "";
}

async function confirmQrPayment() {
  if (!qrData.orderId) {
    qrError.value = "Không tìm thấy mã đơn để xác nhận thanh toán";
    return;
  }

  try {
    confirmingQr.value = true;
    qrError.value = "";
    qrSuccessMessage.value = "";

    const payload = {
      maGiaoDich: qrForm.maGiaoDich?.trim() || "",
      soTien: qrData.amount,
      ghiChu:
        qrForm.ghiChu?.trim() ||
        (form.paymentMethod === "bank_qr"
          ? "Khách đã thanh toán"
          : "Khách đã thanh toán online"),
      paymentGateway:
        form.paymentMethod === "cod" ? null : form.paymentMethod.toUpperCase(),
    };

    const data = await confirmQrPaymentApi(qrData.orderId, payload);

    if (typeof clearCart === "function") {
      clearCart();
    }

    qrSuccessMessage.value =
      data?.message ||
      (form.paymentMethod === "bank_qr"
        ? "Thanh toán thành công"
        : "Thanh toán online thành công");

    showToast(qrSuccessMessage.value, "success");
    closeQrModal();

    successMessage.value = qrSuccessMessage.value;
    showSuccessModal.value = true;

    saveCheckoutSuccessData({
      orderId: qrData.orderId || "",
      maHoaDon: qrData.maHoaDon || "",
      customerName: form.fullName?.trim() || "",
      phone: form.phone?.trim() || "",
      email: form.email?.trim() || "",
      address: fullDeliveryAddress.value || "",
      paymentMethod: form.paymentMethod || "",
      paymentLabel: selectedPaymentOption.value?.title || "Thanh toán online",
      total: Number(qrData.amount || safeGrandTotal.value || 0),
    });

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
let paymentPollingTimer = null;

async function getPaymentStatusApi(orderId) {
  const res = await fetch(
    `${API_BASE}/api/online-checkout/${orderId}/payment-status`,
  );
  const data = await res.json().catch(() => ({}));

  if (!res.ok)
    throw new Error(data?.message || "Không lấy được trạng thái thanh toán");
  return data;
}

function stopPaymentPolling() {
  if (paymentPollingTimer) {
    clearInterval(paymentPollingTimer);
    paymentPollingTimer = null;
  }
}

function handlePaidSuccess(message = "Thanh toán thành công") {
  stopPaymentPolling();

  if (typeof clearCart === "function") clearCart();

  qrSuccessMessage.value = message;
  showToast(message, "success");
  closeQrModal();

  successMessage.value = message;
  showSuccessModal.value = true;

  saveCheckoutSuccessData({
    orderId: qrData.orderId || "",
    maHoaDon: qrData.maHoaDon || "",
    customerName: form.fullName?.trim() || "",
    phone: form.phone?.trim() || "",
    email: form.email?.trim() || "",
    address: fullDeliveryAddress.value || "",
    paymentMethod: form.paymentMethod || "",
    paymentLabel: selectedPaymentOption.value?.title || "Thanh toán online",
    total: Number(qrData.amount || safeGrandTotal.value || 0),
  });

  setTimeout(() => {
    router.push({
      path: "/checkout/success",
      query: { orderId: qrData.orderId || "" },
    });
  }, 1400);
}

function startPaymentPolling() {
  stopPaymentPolling();

  paymentPollingTimer = setInterval(async () => {
    if (!qrData.orderId) return;

    try {
      const data = await getPaymentStatusApi(qrData.orderId);

      if (data?.paymentStatus === "PAID" || data?.paid === true) {
        handlePaidSuccess(data?.message || "Thanh toán thành công");
      }
    } catch (err) {
      console.error("poll payment status error:", err);
    }
  }, 3000);
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
      (it) => !it.idSanPhamChiTiet || !it.soLuong,
    );

    if (hasInvalidItem) {
      errors.general = "Có sản phẩm chưa map được idSanPhamChiTiet";
      showConfirmModal.value = false;
      return;
    }

    const data = await checkoutApi(payload);

    placedOrderItems.value = JSON.parse(JSON.stringify(cartItems.value));
    placedOrder.value = {
      ...data,
      maHoaDon:
        data?.maHoaDon || data?.orderCode || data?.orderId || "TAM_TINH",
      createdAt: new Date().toLocaleString("vi-VN"),
      customerName: form.fullName?.trim() || "Khách lẻ",
      phone: form.phone?.trim() || "-",
      email: form.email?.trim() || "",
      address: fullDeliveryAddress.value || "-",
      paymentLabel:
        selectedPaymentOption.value?.title || "Thanh toán khi nhận hàng",
      note: form.note?.trim() || "-",
      subtotal: Number(safeSubtotal.value) || 0,
      shippingFee: Number(shippingFee.value) || 0,
      discount: Number(discount.value) || 0,
      total: Number(safeGrandTotal.value) || 0,
    };

    showConfirmModal.value = false;

    if (form.paymentMethod === "bank_qr") {
      await openQrModal(data);
      return;
    }

 if (form.paymentMethod === "vnpay") {
  await redirectToVnpay(data);
  return;
}

    if (typeof clearCart === "function") {
      clearCart();
    }

    successMessage.value = data?.message || "Đặt hàng thành công";
    showSuccessModal.value = true;
    showToast(successMessage.value, "success");

    saveCheckoutSuccessData({
      orderId: data?.orderId || "",
      maHoaDon: data?.maHoaDon || data?.orderCode || data?.orderId || "",
      customerName: form.fullName?.trim() || "",
      phone: form.phone?.trim() || "",
      email: form.email?.trim() || "",
      address: fullDeliveryAddress.value || "",
      paymentMethod: form.paymentMethod || "cod",
      paymentLabel:
        selectedPaymentOption.value?.title || "Thanh toán khi nhận hàng",
      total: Number(safeGrandTotal.value || 0),
    });

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
function saveCheckoutSuccessData(payload = {}) {
  sessionStorage.setItem(
    "checkout_success_data",
    JSON.stringify({
      orderId: payload.orderId || "",
      maHoaDon: payload.maHoaDon || "",
      customerName: payload.customerName || "",
      phone: payload.phone || "",
      email: payload.email || "",
      address: payload.address || "",
      paymentMethod: payload.paymentMethod || "",
      paymentLabel: payload.paymentLabel || "",
      total: Number(payload.total || 0),
    }),
  );
}

function onImgError(e) {
  e.target.src =
    "data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='120' height='120'%3E%3Crect width='100%25' height='100%25' fill='%23f1f3f5'/%3E%3Ctext x='50%25' y='52%25' dominant-baseline='middle' text-anchor='middle' fill='%2399a1aa' font-size='14'%3E%E1%BA%A2nh%3C/text%3E%3C/svg%3E";
}

onMounted(async () => {
  await Promise.all([fetchProvinces(), loadVouchers()]);
  syncAppliedVoucher();
});
onUnmounted(() => {
  stopPaymentPolling();
});
async function printInvoice() {
  if (!invoiceData.value.items.length) {
    showToast("Chưa có sản phẩm để in hóa đơn", "warning");
    return;
  }

  await nextTick();

  const area = printAreaRef.value;
  if (!area) return;

  const code = String(invoiceData.value?.maHoaDon || "TAM_TINH").replace(
    /\s+/g,
    "_",
  );
  const w = window.open("", "_blank", "width=420,height=760");

  if (!w) {
    showToast("Trình duyệt đang chặn popup in hóa đơn", "warning");
    return;
  }

  w.document.write(`
    <html>
      <head>
        <title>HoaDon_${code}</title>
        <style>
          body{ margin:0; padding:0; font-family: Arial, sans-serif; }
          .receipt{ width:80mm; padding:8mm 6mm; }
          .center{ text-align:center; }
          .right{ text-align:right; }
          .bold{ font-weight:700; }
          .big{ font-size:16px; }
          .small{ font-size:11px; }
          .muted{ color:#555; }
          .hr{ border-top:1px dashed #000; margin:6px 0; }
          .row2{ display:flex; justify-content:space-between; gap:10px; font-size:12px; }
          .mt2{ margin-top:2px; font-size:12px; }
          .mt6{ margin-top:6px; font-size:12px; }
          .mt8{ margin-top:8px; }
          .items-head, .item{ display:flex; gap:6px; font-size:12px; }
          .w-name{ flex: 1; }
          .w-qty{ width: 10mm; }
          .w-price{ width: 22mm; }
          @media print { @page { margin: 0; } }
        </style>
      </head>
      <body>${area.outerHTML}</body>
    </html>
  `);

  w.document.close();
  w.focus();
  w.onload = () => {
    w.print();
  };
  w.onafterprint = () => w.close();

  showToast("Đã mở cửa sổ in / lưu PDF!", "success");
}
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

.payment-option {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  padding: 16px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  border-radius: 18px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.payment-option + .payment-option {
  margin-top: 12px;
}

.payment-option:hover {
  border-color: #9db4ff;
  background: #fff;
}

.payment-option.active {
  border-color: #001a72;
  background: #eef2ff;
  box-shadow: inset 0 0 0 1px rgba(0, 15, 81, 0.12);
}

.payment-option__left {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  min-width: 0;
}

.payment-option__content {
  min-width: 0;
}

.payment-option__title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.payment-option__title {
  font-weight: 750;
  color: var(--text);
}

.payment-option__desc {
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.5;
}

.payment-option__badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 999px;
  background: #dbeafe;
  color: #1d4ed8;
  font-size: 11px;
  font-weight: 800;
}

.payment-helper {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px 14px;
  border: 1px dashed #cbd5e1;
  background: #f8fafc;
  border-radius: 14px;
  color: #475569;
  font-size: 13px;
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

.summary-actions {
  display: grid;
  gap: 12px;
}

.invoice-row {
  display: flex;
  align-items: center;
  color: var(--text);
  font-weight: 750;
}

.btn-preview-pdf {
  min-height: 46px;
  border-radius: 14px;
  border: 1px solid #d8dfec;
  background: #fff;
  color: var(--text);
  font-weight: 750;
  transition: all 0.2s ease;
}

.btn-preview-pdf:hover:not(:disabled) {
  border-color: #001a72;
  color: #001a72;
  background: #f8fbff;
}

.btn-preview-pdf:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.voucher-saving-note {
  padding: 12px 14px;
  border-radius: 14px;
  background: #fff7ed;
  border: 1px solid #fed7aa;
  color: #9a3412;
  font-size: 13px;
  line-height: 1.5;
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
  border-color: #9cffc0;
  background: #e5fbea;
  box-shadow: inset 0 0 0 1px rgba(178, 236, 177, 0.18);
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
  background: #bcffbf;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.voucher-group-title {
  font-size: 13px;
  font-weight: 800;
  color: #334155;
  margin-bottom: 8px;
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

.badge-public {
  display: inline-flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 999px;
  background: #dbeafe;
  color: #1d4ed8;
  font-size: 11px;
  font-weight: 800;
}

.app-toast-wrap {
  position: fixed;
  top: 24px;
  right: 24px;
  z-index: 2000;
}

.app-toast {
  min-width: 320px;
  max-width: 420px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  border-radius: 16px;
  box-shadow: 0 16px 36px rgba(2, 6, 23, 0.18);
  font-weight: 650;
}

.app-toast--success {
  background: #ecfdf5;
  border: 1px solid #86efac;
  color: #166534;
}

.app-toast--warning {
  background: #fff7ed;
  border: 1px solid #fdba74;
  color: #9a3412;
}

.app-toast--info {
  background: #eff6ff;
  border: 1px solid #93c5fd;
  color: #1d4ed8;
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.invoice-print-area {
  position: fixed;
  left: -99999px;
  top: 0;
  opacity: 0;
  pointer-events: none;
}

.shipping-line .shipping-label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.ship-fee-logo {
  height: 18px;
  width: auto;
  object-fit: contain;
}

/* ===== CLEAN QR MODAL CSS ===== */
.qr-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(2, 6, 23, 0.62);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px 12px;
  overflow: auto;
  z-index: 9999;
}

.qr-modal {
  width: min(92vw, 620px);
  max-height: 88vh;
  background: #fff;
  border-radius: 22px;
  box-shadow: 0 24px 60px rgba(2, 6, 23, 0.28);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.qr-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 20px;
  background: linear-gradient(90deg, #000f51 0%, #0f2f98 100%);
  color: #fff;
  flex-shrink: 0;
}

.qr-modal-header h5 {
  margin: 0;
  font-size: 18px;
  font-weight: 750;
  color: #fff;
}

.qr-modal-body {
  flex: 1 1 auto;
  overflow-y: auto;
  padding: 18px 20px 14px;
}

.qr-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 20px 18px;
  border-top: 1px solid #eef2f7;
  background: #fff;
  flex-shrink: 0;
}

.payment-channel-badge {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 999px;
  background: #eef2ff;
  color: #1e3a8a;
  font-weight: 750;
  font-size: 13px;
  margin-bottom: 14px;
}

.qr-image {
  display: block;
  width: min(100%, 340px);
  max-width: 340px;
  height: auto;
  margin: 0 auto;
  border-radius: 14px;
  background: #fff;
}

.qr-placeholder {
  border: 1px dashed #cbd5e1;
  background: #f8fafc;
  border-radius: 14px;
  padding: 28px 16px;
  color: #64748b;
  text-align: center;
}

.qr-info {
  display: grid;
  gap: 8px;
  margin-top: 16px;
  color: #0f172a;
  font-size: 15px;
  line-height: 1.55;
}

.qr-note-textarea {
  min-height: 84px !important;
  max-height: 140px;
  resize: vertical;
  padding-top: 12px;
}

.qr-modal .form-label {
  margin-bottom: 6px;
}

.qr-modal .input-ui {
  min-height: 44px;
}

.qr-modal-body .mt-3 {
  margin-top: 12px !important;
}

.btn-qr-close {
  min-height: 44px;
  padding: 0 20px;
  border-radius: 14px;
  border: 1px solid #d8dfec;
  background: #fff;
  color: #0f172a;
  font-weight: 750;
  transition: all 0.2s ease;
}

.btn-qr-close:hover {
  border-color: #001a72;
  color: #001a72;
}

.btn-qr-confirm {
  min-height: 44px;
  padding: 0 20px;
  border-radius: 14px;
  border: none;
  background: #000f51;
  color: #fff;
  font-weight: 750;
  box-shadow: 0 14px 28px rgba(0, 15, 81, 0.18);
  transition: all 0.2s ease;
}

.btn-qr-confirm:hover:not(:disabled) {
  background: #001a72;
}

.btn-qr-confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.gateway-box {
  padding: 14px;
  border-radius: 18px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
}

.gateway-box__title {
  color: var(--text);
  font-size: 16px;
  font-weight: 750;
  margin-bottom: 6px;
}

.gateway-box__desc {
  color: #64748b;
  font-size: 14px;
  line-height: 1.6;
}

.gateway-box__meta {
  margin-top: 12px;
  display: grid;
  gap: 8px;
  color: var(--text);
  font-size: 14px;
}

.btn-open-gateway {
  width: 100%;
  min-height: 46px;
  border: none;
  border-radius: 14px;
  background: #000f51;
  color: #fff;
  font-weight: 750;
  box-shadow: 0 14px 28px rgba(0, 15, 81, 0.18);
  transition: all 0.2s ease;
}

.btn-open-gateway:hover {
  background: #001a72;
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

  .payment-option {
    padding: 14px;
  }

  .app-toast-wrap {
    left: 16px;
    right: 16px;
    top: 16px;
  }

  .app-toast {
    min-width: 0;
    width: 100%;
    max-width: none;
  }

  .qr-backdrop {
    padding: 12px;
  }

  .qr-modal {
    width: 100%;
    max-height: calc(100vh - 24px);
    border-radius: 18px;
  }

  .qr-modal-header {
    padding: 14px 16px;
  }

  .qr-modal-header h5 {
    font-size: 16px;
  }

  .qr-modal-body {
    padding: 14px 16px 12px;
  }

  .qr-modal-footer {
    padding: 12px 16px 16px;
    gap: 8px;
  }

  .qr-image {
    width: min(100%, 280px);
    max-width: 280px;
  }

  .qr-note-textarea {
    min-height: 76px !important;
  }

  .btn-qr-close,
  .btn-qr-confirm {
    min-height: 42px;
    padding: 0 16px;
    font-size: 14px;
  }
}
/* ===== QR MODAL UI TWEAK ===== */
.qr-modal-header h5 {
  font-size: 22px;
  font-weight: 800;
}

.payment-channel-badge {
  padding: 8px 14px;
  font-size: 14px;
  font-weight: 800;
}

.qr-image {
  width: min(100%, 300px);
  max-width: 300px;
  border-radius: 18px;
  display: block;
  margin: 0 auto;
}

.qr-info-card {
  margin-top: 18px;
  padding: 16px 18px;
  border-radius: 18px;
  background: linear-gradient(180deg, #f8fbff 0%, #fdfdff 100%);
  border: 1px solid #e4ebf5;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.7);
}

.qr-info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px dashed #dbe4f0;
}

.qr-info-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.qr-info-label {
  font-size: 15px;
  font-weight: 700;
  color: #475569;
}

.qr-info-value {
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
  text-align: right;
  word-break: break-word;
}

.qr-info-row--amount {
  align-items: flex-end;
}

.qr-amount {
  font-size: 30px;
  line-height: 1.1;
  font-weight: 900;
  color: #dc2626;
  letter-spacing: 0.2px;
}

.qr-form-label {
  font-size: 16px;
  font-weight: 800;
}

.qr-note-textarea {
  min-height: 88px !important;
  font-size: 15px;
  line-height: 1.6;
  border-radius: 18px;
}

.btn-qr-close {
  min-width: 110px;
  min-height: 46px;
  font-size: 16px;
  font-weight: 800;
  border-radius: 16px;
}

@media (max-width: 767.98px) {
  .qr-modal-header h5 {
    font-size: 18px;
  }

  .qr-info-card {
    padding: 14px;
    border-radius: 16px;
  }

  .qr-info-label {
    font-size: 14px;
  }

  .qr-info-value {
    font-size: 16px;
  }

  .qr-amount {
    font-size: 24px;
  }

  .qr-note-textarea {
    min-height: 78px !important;
    font-size: 14px;
  }

  .btn-qr-close {
    min-width: 96px;
    min-height: 42px;
    font-size: 14px;
  }
}
</style>
