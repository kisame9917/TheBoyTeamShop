<template>
  <div class="container-fluid py-3">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-3">
      <h4 class="m-0 fw-bold">Bán hàng (tại cửa hàng)</h4>

      <button
        class="btn btn-outline-primary btn-sm"
        type="button"
        @click="createOrder"
        :disabled="orders.length >= MAX_ORDERS"
      >
        + Tạo đơn hàng
      </button>
    </div>

    <div class="card shadow-sm">
      <!-- Tabs -->
      <div class="px-3 pt-3">
        <div
          class="d-flex align-items-center justify-content-between gap-2 flex-wrap"
        >
          <div class="d-flex flex-wrap gap-2">
           <button
  v-for="o in orders"
  :key="o.id"
  type="button"
  class="btn btn-sm order-tab-btn"
  :class="o.id === activeId ? 'btn-dark' : 'btn-outline-dark'"
  @click="activeId = o.id"
>
  <span class="order-tab-label">{{ o.label }}</span>

  <span
    v-if="getOrderItemCount(o) > 0"
    class="order-count-badge"
  >
    {{ getOrderItemCount(o) }}
  </span>

  <span
    class="tab-x"
    title="Đóng"
    @click.stop="closeOrder(o.id)"
  >
    ×
  </span>
</button>
          </div>

          <div class="text-muted small">
            {{ orders.length }}/{{ MAX_ORDERS }} đơn
          </div>
        </div>
      </div>

      <div class="card-body">
        <div v-if="!activeOrder" class="text-center py-5 text-muted">
          <div class="fs-1">👜</div>
          <div class="fw-semibold">Chưa có đơn hàng nào</div>
        </div>

        <div v-else>
          <!-- Products actions -->
          <div
            class="d-flex align-items-center justify-content-between flex-wrap gap-2 mb-2"
          >
            <div class="fw-bold">Sản phẩm</div>
            <div class="d-flex gap-2">
              <button
                class="btn btn-outline-primary btn-sm"
                type="button"
                @click="openProductModal"
              >
                <i class="bi bi-plus-lg me-1"></i> Thêm sản phẩm
              </button>
              <button
                class="btn btn-outline-primary btn-sm"
                type="button"
                @click="toastInfo('Chưa tích hợp quét QR')"
              >
                <i class="bi bi-qr-code-scan me-1"></i> Quét QR
              </button>
            </div>
          </div>

          <!-- Cart -->
          <div class="border rounded-3 p-2 mb-3">
            <div
              v-if="activeOrder.cart.length === 0"
              class="text-center py-4 text-muted"
            >
              <div class="fs-1">👜</div>
              <div class="fw-semibold">Chưa có sản phẩm nào</div>
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
                        :src="it.image || placeholderImg"
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

                        <input
                          class="form-control form-control-sm text-center"
                          style="width: 60px"
                          :value="it.qty"
                          inputmode="numeric"
                          @input="onQtyInput(idx, $event)"
                          @blur="onQtyBlur(idx, $event)"
                        />

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

          <!-- Bottom -->
          <div class="row g-3">
            <!-- Customer -->
            <div class="col-12 col-lg-6">
              <div class="card h-100">
                <div
                  class="card-header bg-white d-flex align-items-center justify-content-between flex-wrap gap-2"
                >
                  <div>
                    <div class="fw-bold">
  {{ activeOrder.loaiDon ? "Thông tin giao hàng" : "Thông tin khách hàng" }}
</div>
                    <div class="text-muted small">
                      Mã HĐ:
                      <span class="fw-semibold font-monospace">{{
                        activeOrder.maHoaDon
                      }}</span>
                    </div>
                  </div>

                  <div class="d-flex align-items-center gap-2">
                    <button
                      class="btn btn-outline-secondary btn-sm customer-action-btn"
                      type="button"
                      @click="resetToWalkInCustomer"
                    >
                      Đặt lại
                    </button>

                    <button
                      class="btn btn-outline-dark btn-sm customer-action-btn"
                      type="button"
                      @click="openCustomerModal"
                    >
                      Chọn khách hàng
                    </button>
                  </div>
                </div>

               <div class="card-body">
  <template v-if="!activeOrder.loaiDon">
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

      <div class="col-12">
        <textarea
          class="form-control"
          rows="2"
          placeholder="Địa chỉ (nếu có)"
          v-model="activeOrder.diaChi"
        ></textarea>
      </div>
    </div>
  </template>

  <template v-else>
    <div class="border rounded-3 p-3">
      <div class="fw-bold mb-2">Thông tin nhận hàng</div>

      <div class="row g-2">
        <div class="col-12 col-md-6">
          <input
            class="form-control"
            placeholder="Tên người nhận"
            v-model.trim="activeOrder.tenNguoiNhanHang"
          />
        </div>
        <div class="col-12 col-md-6">
          <input
            class="form-control"
            placeholder="SĐT người nhận"
            v-model.trim="activeOrder.soDienThoaiNhanHang"
          />
        </div>

        <div class="col-12 col-md-6">
          <label class="form-label mb-1">Tỉnh/Thành</label>
          <select
            class="form-select"
            v-model="activeOrder.tinhThanhNhanHang"
            @change="onProvinceChange($event.target.value)"
            :disabled="provincesLoading"
          >
            <option value="" disabled>-- Chọn tỉnh/thành --</option>
            <option
              v-for="p in provinces"
              :key="p.code"
              :value="p.code"
            >
              {{ p.name }}
            </option>
          </select>
        </div>

        <div class="col-12 col-md-6">
          <label class="form-label mb-1">Phường/Xã</label>
          <select
            class="form-select"
            v-model="activeOrder.phuongXaNhanHang"
            :disabled="!activeOrder.tinhThanhNhanHang || wardsLoading"
          >
            <option value="" disabled>-- Chọn phường/xã --</option>
            <option
              v-for="w in wards"
              :key="w.code"
              :value="w.code"
            >
              {{ w.name }}
            </option>
          </select>
        </div>

        <div class="col-12">
          <label class="form-label mb-1">Địa chỉ chi tiết</label>
          <input
            class="form-control"
            placeholder="Số nhà, tên đường..."
            v-model.trim="activeOrder.diaChiNhanHangChiTiet"
          />
        </div>

          <div class="col-12 mt-3">
          <div class="shipping-brand-box">
            <img
  :src="ghnLogo"
  alt="GHN Express"
  class="shipping-brand-logo"
/>
          </div>
        </div>
      </div>
    </div>
  </template>
</div>
              </div>
            </div>

            <!-- Payment -->
            <div class="col-12 col-lg-6">
              <div class="card h-100">
                <div
                  class="card-header bg-white d-flex align-items-center justify-content-between"
                >
                  <div class="fw-bold">Thanh toán</div>
                  <div class="small text-muted">Chỉ tại cửa hàng</div>
                </div>

                <div class="card-body">
                  <!-- ✅ Chọn loại đơn -->
                  <div class="mb-3">
                    <label class="form-label fw-bold mb-1">Hình thức</label>

                    <div class="form-check form-switch">
                      <input
                        class="form-check-input"
                        type="checkbox"
                        :checked="!!activeOrder.loaiDon"
                        @change="toggleShip"
                        id="switch-ship"
                      />
                      <label class="form-check-label" for="switch-ship">
                        {{
                          activeOrder.loaiDon
                            ? "Bán tại quầy (có ship)"
                            : "Bán tại quầy"
                        }}
                      </label>
                    </div>

                    <div
                      v-if="activeOrder.loaiDon"
                      class="small text-muted mt-1"
                    >
                      Ship toàn quốc cố định <b>30.000đ</b>
                    </div>
                  </div>
                  <!-- =======================
                       PHIẾU GIẢM GIÁ
                       ======================= -->
                  <div class="mb-3">
                    <div
                      class="d-flex align-items-center justify-content-between flex-wrap gap-2"
                    >
                      <label class="form-label fw-bold m-0">
                        Mã giảm giá <span class="text-danger">*</span>
                      </label>

                      <div class="d-flex gap-2">
                        <button
                          class="btn btn-outline-secondary btn-sm"
                          type="button"
                          @click="loadVouchers"
                        >
                          Tải lại
                        </button>
                        <button
                          class="btn btn-outline-danger btn-sm"
                          type="button"
                          @click="disableVoucher"
                        >
                          Không dùng mã
                        </button>
                      </div>
                    </div>

                    <ul class="nav nav-tabs mt-2">
                      <li class="nav-item">
                        <button
                          class="nav-link"
                          type="button"
                          :class="{ active: activeOrder.voucherTab === 'best' }"
                          @click="activeOrder.voucherTab = 'best'"
                        >
                          Mã tốt nhất
                        </button>
                      </li>
                      <li class="nav-item">
                        <button
                          class="nav-link"
                          type="button"
                          :class="{ active: activeOrder.voucherTab === 'alt' }"
                          @click="activeOrder.voucherTab = 'alt'"
                        >
                          Mã thay thế
                        </button>
                      </li>
                    </ul>

                    <div class="border border-top-0 rounded-bottom p-2">
                      <!-- BEST -->
                      <template v-if="activeOrder.voucherTab === 'best'">
                        <div
                          v-if="activeOrder.voucherMode === 'none'"
                          class="text-muted small py-2"
                        >
                          Bạn đang chọn: <b>Không dùng mã</b>
                        </div>

                        <div
                          v-else-if="bestVoucherEntryUI"
                          class="voucher-card"
                        >
                          <div
                            class="d-flex align-items-start justify-content-between gap-2"
                          >
                            <div>
                              <div class="d-flex gap-2 flex-wrap">
                                <span class="badge text-bg-primary">PGG</span>
                                <span class="badge text-bg-secondary"
                                  >Mã tốt nhất</span
                                >
                                <span class="badge text-bg-dark">{{
                                  bestVoucherEntryUI.v.ma_giam_gia
                                }}</span>

                                <span
                                  v-if="
                                    activeOrder.pggId ===
                                      bestVoucherEntryUI.v.id &&
                                    bestVoucherEntryUI.discount <= 0
                                  "
                                  class="badge text-bg-danger"
                                >
                                  Không còn hiệu lực
                                </span>
                              </div>

                              <div class="mt-2 fw-bold">
                                {{ bestVoucherEntryUI.v.ten_giam_gia }}
                              </div>

                              <div class="mt-2">
                                <span class="text-muted">Giảm:</span>
                                <span class="ms-2 fw-bold text-danger"
                                  >-
                                  {{ money(bestVoucherEntryUI.discount) }}</span
                                >
                              </div>

                              <div class="small text-muted mt-1">
                                <span
                                  v-if="
                                    bestVoucherEntryUI.v.don_hang_toi_thieu > 0
                                  "
                                >
                                  Đơn tối thiểu:
                                  <b>{{
                                    money(
                                      bestVoucherEntryUI.v.don_hang_toi_thieu,
                                    )
                                  }}</b>
                                </span>
                                <span
                                  v-if="
                                    bestVoucherEntryUI.v.gia_tri_giam_toi_da > 0
                                  "
                                  class="ms-2"
                                >
                                  Tối đa:
                                  <b>{{
                                    money(
                                      bestVoucherEntryUI.v.gia_tri_giam_toi_da,
                                    )
                                  }}</b>
                                </span>
                              </div>
                            </div>

                            <div class="text-end">
                              <span
                                v-if="
                                  activeOrder.voucherMode !== 'none' &&
                                  activeOrder.pggId === bestVoucherEntryUI.v.id
                                "
                                class="badge text-bg-success"
                              >
                                Đang áp dụng
                              </span>

                              <button
                                v-else
                                class="btn btn-success btn-sm"
                                type="button"
                                @click="
                                  applyVoucherManual(bestVoucherEntryUI.v)
                                "
                              >
                                Áp dụng
                              </button>
                            </div>
                          </div>

                          <div class="mt-3 small text-muted">
                            <div>
                              <span class="me-2">Hết hạn:</span>
                              <b>{{
                                formatDateVN(bestVoucherEntryUI.v.ngay_ket_thuc)
                              }}</b>
                            </div>
                          </div>
                        </div>

                        <div v-else class="text-muted small py-2">
                          Không có mã giảm giá phù hợp với đơn hiện tại.
                        </div>
                      </template>

                      <!-- ALT -->
                      <template v-else>
                        <div
                          v-if="altVoucherEntriesUI.length === 0"
                          class="text-muted small py-2"
                        >
                          Không có mã thay thế khả dụng.
                        </div>

                        <div v-else class="d-flex flex-column gap-2">
                          <div
                            v-for="e in altVoucherEntriesUI.slice(0, 10)"
                            :key="e.v.id"
                            class="voucher-card"
                          >
                            <div
                              class="d-flex align-items-start justify-content-between gap-2"
                            >
                              <div>
                                <div class="d-flex gap-2 flex-wrap">
                                  <span class="badge text-bg-primary">PGG</span>
                                  <span class="badge text-bg-secondary"
                                    >Mã thay thế</span
                                  >
                                  <span class="badge text-bg-dark">{{
                                    e.v.ma_giam_gia
                                  }}</span>

                                  <span
                                    v-if="
                                      activeOrder.pggId === e.v.id &&
                                      activeOrder.voucherMode !== 'none'
                                    "
                                    class="badge text-bg-success"
                                  >
                                    Đang chọn
                                  </span>

                                  <span
                                    v-if="
                                      activeOrder.pggId === e.v.id &&
                                      e.discount <= 0
                                    "
                                    class="badge text-bg-danger"
                                  >
                                    Không còn hiệu lực
                                  </span>
                                </div>

                                <div class="mt-2 fw-bold">
                                  {{ e.v.ten_giam_gia }}
                                </div>

                                <div class="mt-2">
                                  <span class="text-muted">Giảm:</span>
                                  <span class="ms-2 fw-bold text-danger"
                                    >- {{ money(e.discount) }}</span
                                  >
                                </div>

                                <div class="small text-muted mt-1">
                                  <span v-if="e.v.don_hang_toi_thieu > 0">
                                    Đơn tối thiểu:
                                    <b>{{ money(e.v.don_hang_toi_thieu) }}</b>
                                  </span>
                                  <span
                                    v-if="e.v.gia_tri_giam_toi_da > 0"
                                    class="ms-2"
                                  >
                                    Tối đa:
                                    <b>{{ money(e.v.gia_tri_giam_toi_da) }}</b>
                                  </span>
                                </div>
                              </div>

                              <div class="text-end">
                                <button
                                  class="btn btn-outline-primary btn-sm"
                                  type="button"
                                  @click="applyVoucherManual(e.v)"
                                >
                                  Chọn
                                </button>
                              </div>
                            </div>
                          </div>
                        </div>
                      </template>
                    </div>

                    <div class="mt-3">
                      <div class="fw-bold">
                        Gợi ý mã giảm giá <span class="text-danger">*</span>
                      </div>

                      <div
                        v-if="voucherSuggestions.length === 0"
                        class="text-muted small mt-1"
                      >
                        Không có gợi ý mua thêm để đạt mã tốt hơn
                      </div>

                      <div v-else class="d-flex flex-column gap-2 mt-2">
                        <div
                          v-for="e in voucherSuggestions"
                          :key="'suggest-' + e.v.id"
                          class="border rounded-3 p-2"
                        >
                          <div
                            class="d-flex align-items-center justify-content-between gap-2 flex-wrap"
                          >
                            <div>
                              Thêm
                              <b class="text-danger">{{ money(e.missing) }}</b>
                              để dùng <b>{{ e.v.ma_giam_gia }}</b> (giảm khoảng
                              <b class="text-danger"
                                >-{{ money(e.expectedDiscount) }}</b
                              >
                              khi đơn ≥ {{ money(e.minOrder) }})
                            </div>

                            <button
                              class="btn btn-outline-secondary btn-sm"
                              type="button"
                              @click="
                                toastInfo(
                                  'Thêm sản phẩm để đủ điều kiện rồi áp dụng mã',
                                )
                              "
                            >
                              Xem
                            </button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!-- ======================= /PGG ======================= -->

                  <div class="mb-3">
                    <label class="form-label mb-1">Giảm giá thủ công (%)</label>
                    <input
                      class="form-control"
                      v-model.number="activeOrder.discountPercent"
                      inputmode="numeric"
                      :disabled="!!activeOrder.pggId"
                      :placeholder="
                        activeOrder.pggId
                          ? 'Đang chọn PGG, % bị khóa'
                          : 'Nhập % giảm (tùy chọn)'
                      "
                    />
                  </div>

                  <ul class="list-group mb-3">
                   <li class="list-group-item d-flex justify-content-between">
  <span class="text-muted">Tiền hàng</span>
  <span class="fw-semibold">{{ money(subTotal) }}</span>
</li>

<li
  v-if="activeOrder.loaiDon"
  class="list-group-item d-flex justify-content-between"
>
  <span class="text-muted">Phí ship</span>
  <span class="fw-semibold">{{ money(shipMoney) }}</span>
</li>

                    <li class="list-group-item d-flex justify-content-between">
                      <span class="text-muted">Giảm giá</span>
                      <span class="fw-semibold text-danger"
                        >- {{ money(discountMoney) }}</span
                      >
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
                        :value="formatMoneyInput(activeOrder.paid)"
                        inputmode="numeric"
                        @input="onPaidInput"
                        @blur="onPaidBlur"
                      />
                    </li>

                    <li class="list-group-item d-flex justify-content-between">
                      <span class="text-muted">Tiền thừa</span>
                      <span class="fw-semibold">{{ money(changeMoney) }}</span>
                    </li>
                  </ul>

                  <!-- Buttons -->
                  <div class="d-grid gap-2">
                    <button
                      class="btn btn-success w-100"
                      :disabled="activeOrder.cart.length === 0 || submitting"
                      @click="confirmOrderCash"
                    >
                      Thanh toán (tiền mặt)
                    </button>

                    <button
                      class="btn btn-outline-success w-100"
                      type="button"
                      :disabled="activeOrder.cart.length === 0 || submitting"
                      @click="openQrPay"
                    >
                      Thanh toán QR
                    </button>
                  </div>

                  <div v-if="confirmHint" class="small text-muted mt-2">
                    {{ confirmHint }}
                  </div>
                </div>
              </div>
            </div>
          </div>
          <!-- /row -->
        </div>
      </div>
    </div>

    <!-- Toast -->
   <div
  v-if="toast.show"
  class="position-fixed top-0 end-0 p-3"
  style="z-index: 2000"
>
  <div class="custom-toast shadow-sm" :class="toastClass">
    <div class="custom-toast-content">
      <div class="custom-toast-title">{{ toast.title }}</div>
      <div class="custom-toast-message">{{ toast.msg }}</div>
    </div>

    <button
      type="button"
      class="custom-toast-close"
      @click="toast.show = false"
      aria-label="Close"
    >
      ×
    </button>
  </div>
</div>

    <!-- ✅ Modal layer -->
    <teleport to="body">
      <!-- Backdrop (1 cái chung) -->
      <div
        v-if="anyModalOpen || showPreCheckoutModal"
        class="modal-backdrop fade show"
        @click="closeAnyModal"
        style="z-index: 1050"
      ></div>

      <!-- Product Modal -->
      <div
        v-if="showProductModal"
        class="modal fade show"
        tabindex="-1"
        role="dialog"
        aria-modal="true"
        style="display: block; z-index: 1055"
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
              <div class="card border-0 shadow-sm mb-3">
                <div class="card-body pt-3">
                  <div class="row g-3 align-items-end">
                    <div class="col-12 col-lg-6">
                      <label class="form-label mb-1">Tìm kiếm</label>
                      <input
                        class="form-control"
                        placeholder="Tìm theo mã / tên sản phẩm..."
                        v-model.trim="productFilters.keyword"
                      />
                    </div>

                    <div class="col-12 col-md-6 col-lg-3">
                      <label class="form-label mb-1">Màu sắc</label>
                      <select
                        class="form-select"
                        v-model="productFilters.color"
                      >
                        <option value="">-- Chọn màu sắc --</option>
                        <option
                          v-for="c in productColorOptions"
                          :key="c"
                          :value="c"
                        >
                          {{ c }}
                        </option>
                      </select>
                    </div>

                    <div class="col-12 col-md-6 col-lg-3">
                      <label class="form-label mb-1">Size</label>
                      <select class="form-select" v-model="productFilters.size">
                        <option value="">-- Chọn size --</option>
                        <option
                          v-for="s in productSizeOptions"
                          :key="s"
                          :value="s"
                        >
                          {{ s }}
                        </option>
                      </select>
                    </div>

                    <div class="col-12 col-lg-7">
                      <label class="form-label mb-2">Khoảng giá</label>

                      <div
                        class="d-flex align-items-center gap-2 flex-wrap mb-2"
                      >
                        <span class="fw-semibold text-success">
                          {{ money(productPriceRange.min).replace(" đ", "") }}
                        </span>
                        <span>-</span>
                        <span class="fw-semibold text-success">
                          {{ money(productPriceRange.max).replace(" đ", "") }}
                        </span>
                      </div>

                      <div class="price-range-wrap">
                        <input
                          class="range-input"
                          type="range"
                          :min="productPriceBounds.min"
                          :max="productPriceBounds.max"
                          :step="productPriceStep"
                          v-model.number="productPriceRange.min"
                          @input="onPriceRangeMinInput"
                        />

                        <input
                          class="range-input"
                          type="range"
                          :min="productPriceBounds.min"
                          :max="productPriceBounds.max"
                          :step="productPriceStep"
                          v-model.number="productPriceRange.max"
                          @input="onPriceRangeMaxInput"
                        />
                      </div>

                      <div class="small text-muted mt-1">
                        Giá tối đa hiện tại:
                        <b>{{ money(productPriceRange.max) }}</b>
                      </div>
                    </div>

                    <div class="col-12 col-lg-5">
                      <label class="form-label mb-2">Trạng thái</label>
                      <div class="product-status-group">
                        <div class="form-check">
                          <input
                            class="form-check-input"
                            type="radio"
                            value=""
                            v-model="productFilters.stockStatus"
                            id="stock-all"
                          />
                          <label class="form-check-label" for="stock-all"
                            >Tất cả</label
                          >
                        </div>

                        <div class="form-check">
                          <input
                            class="form-check-input"
                            type="radio"
                            value="in"
                            v-model="productFilters.stockStatus"
                            id="stock-in"
                          />
                          <label class="form-check-label" for="stock-in"
                            >Còn hàng</label
                          >
                        </div>

                        <div class="form-check">
                          <input
                            class="form-check-input"
                            type="radio"
                            value="out"
                            v-model="productFilters.stockStatus"
                            id="stock-out"
                          />
                          <label class="form-check-label" for="stock-out"
                            >Hết hàng</label
                          >
                        </div>
                      </div>
                    </div>

                    <div
                      class="col-12 d-flex justify-content-between align-items-center flex-wrap gap-2"
                    >
                      <div
                        class="small text-muted d-flex align-items-center gap-3 flex-wrap"
                      >
                        <span v-if="productLoading">Đang tải...</span>
                        <span
                          >Hiển thị: <b>{{ filteredProducts.length }}</b></span
                        >
                        <span
                          >Tổng: <b>{{ productTotal }}</b></span
                        >
                      </div>

                      <div class="d-flex gap-2">
                        <button
                          class="btn btn-outline-secondary"
                          type="button"
                          @click="resetProductFilters"
                        >
                          Đặt lại
                        </button>

                        <button
                          class="btn btn-outline-secondary"
                          type="button"
                          @click="reloadProducts"
                          :disabled="productLoading"
                        >
                          Tải lại
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="table-responsive">
                <table class="table table-bordered align-middle">
                  <thead class="table-light">
                    <tr>
                      <th style="width: 60px">STT</th>
                      <th style="width: 130px">Mã</th>
                      <th style="width: 80px">Ảnh</th>
                      <th>Tên sản phẩm</th>
                      <th style="width: 120px">Màu</th>
                      <th style="width: 120px">Size</th>
                      <th style="width: 90px" class="text-end">Tồn</th>
                      <th style="width: 140px" class="text-end">Giá</th>
                      <th style="width: 110px" class="text-center">Chọn</th>
                    </tr>
                  </thead>

               <tbody>
  <tr v-for="(p, i) in filteredProducts" :key="p.idSpct">
    <td>{{ i + 1 + productPage * productSize }}</td>
    <td class="fw-semibold">{{ p.code }}</td>
    <td>
      <img
        :src="p.image || placeholderImg"
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

  <tr v-if="filteredProducts.length === 0 && !productLoading">
    <td colspan="9" class="text-center text-muted py-3">
      Không có dữ liệu
    </td>
  </tr>
</tbody>
                </table>
              </div>

              <!-- Pagination -->
              <div class="d-flex align-items-center mt-2">
                <!-- Left -->
                <div class="text-muted small" style="min-width: 220px">
                  Hiển thị {{ filteredProducts.length }} / tổng
                  {{ productTotal }} bản ghi
                </div>

                <!-- Center -->
                <div
                  class="flex-grow-1 d-flex justify-content-center align-items-center gap-2"
                >
                  <button
                    class="btn btn-outline-dark btn-sm"
                    @click="productPrev"
                    :disabled="productPage <= 0 || productLoading"
                  >
                    ‹
                  </button>

                  <div class="small">
                    Trang <b>{{ productPage + 1 }}</b>
                    <span v-if="productTotalPages"
                      >/ {{ productTotalPages }}</span
                    >
                  </div>

                  <button
                    class="btn btn-outline-dark btn-sm"
                    @click="productNext"
                    :disabled="productLast || productLoading"
                  >
                    ›
                  </button>
                </div>

                <!-- Right -->
                <div
                  class="d-flex justify-content-end"
                  style="min-width: 190px"
                >
                  <select
                    class="form-select form-select-sm"
                    style="width: 150px"
                    v-model.number="productSize"
                    @change="onProductSizeChange"
                  >
                    <option :value="10">10 bản ghi / trang</option>
                    <option :value="20">20 bản ghi / trang</option>
                    <option :value="50">50 bản ghi / trang</option>
                    <option :value="100">100 bản ghi / trang</option>
                  </select>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Customer Modal -->
      <div
        v-if="showCustomerModal"
        class="modal fade show"
        tabindex="-1"
        role="dialog"
        aria-modal="true"
        style="display: block; z-index: 1055"
      >
        <div
          class="modal-dialog modal-xl modal-dialog-scrollable customer-modal-dialog"
        >
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
                  placeholder="Tìm theo tên/SĐT/email/địa chỉ..."
                  v-model.trim="customerKw"
                />

                <button
                  class="btn btn-outline-secondary"
                  type="button"
                  @click="reloadCustomers"
                  :disabled="customerLoading"
                >
                  Tải lại
                </button>

                <div
                  class="ms-auto small text-muted d-flex align-items-center gap-2"
                >
                  <span v-if="customerLoading">Đang tải...</span>
                  <span
                    >Tổng: <b>{{ customerTotal }}</b></span
                  >
                </div>
              </div>

              <div class="table-responsive customer-table-wrap">
                <table
                  class="table table-bordered align-middle mb-0 customer-table"
                >
                  <colgroup>
                    <col style="width: 8%" />
                    <col style="width: 25%" />
                    <col style="width: 18%" />
                    <col style="width: 37%" />
                    <col style="width: 12%" />
                  </colgroup>

                  <thead class="table-light">
                    <tr>
                      <th class="text-center">STT</th>
                      <th>Tên khách</th>
                      <th>SĐT</th>
                      <th>Địa chỉ</th>
                      <th class="text-center">Chọn</th>
                    </tr>
                  </thead>

                  <tbody>
                    <tr
                      v-for="(c, i) in pagedCustomers"
                      :key="c.id || c.phone || i"
                    >
                      <td class="text-center">
                        {{ i + 1 + customerPage * customerSize }}
                      </td>
                      <td class="fw-semibold">{{ c.name }}</td>
                      <td>{{ c.phone }}</td>
                      <td class="customer-address-cell">{{ c.address }}</td>
                      <td class="text-center">
                        <button
                          class="btn btn-dark btn-sm customer-pick-btn"
                          @click="chooseCustomer(c)"
                        >
                          Chọn
                        </button>
                      </td>
                    </tr>

                    <tr
                      v-if="filteredCustomers.length === 0 && !customerLoading"
                    >
                      <td colspan="5" class="text-center text-muted py-3">
                        Không có dữ liệu
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <!-- Pagination -->
              <div class="d-flex align-items-center mt-2">
                <!-- Left -->
                <div class="text-muted small" style="min-width: 220px">
                  Hiển thị {{ pagedCustomers.length }} / tổng
                  {{ customerTotal }} bản ghi
                </div>

                <!-- Center -->
                <div
                  class="flex-grow-1 d-flex justify-content-center align-items-center gap-2"
                >
                  <button
                    class="btn btn-outline-dark btn-sm"
                    @click="customerPrev"
                    :disabled="customerPage <= 0 || customerLoading"
                  >
                    ‹
                  </button>

                  <div class="small">
                    Trang <b>{{ customerPage + 1 }}</b>
                    <span v-if="customerTotalPages"
                      >/ {{ customerTotalPages }}</span
                    >
                  </div>

                  <button
                    class="btn btn-outline-dark btn-sm"
                    @click="customerNext"
                    :disabled="customerLast || customerLoading"
                  >
                    ›
                  </button>
                </div>

                <!-- Right -->
                <div
                  class="d-flex justify-content-end"
                  style="min-width: 190px"
                >
                  <select
                    class="form-select form-select-sm"
                    style="width: 150px"
                    v-model.number="customerSize"
                    @change="onCustomerSizeChange"
                  >
                    <option :value="10">10 bản ghi / trang</option>
                    <option :value="20">20 bản ghi / trang</option>
                    <option :value="50">50 bản ghi / trang</option>
                  </select>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ✅ QR Pay Modal -->
      <div
        v-if="showQrPayModal"
        class="modal fade show"
        tabindex="-1"
        role="dialog"
        aria-modal="true"
        style="display: block; z-index: 1065"
      >
        <div class="modal-dialog modal-md modal-dialog-centered">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title fw-bold">Thanh toán bằng QR</h5>
              <button
                type="button"
                class="btn-close"
                @click="closeQrPay"
              ></button>
            </div>

            <div class="modal-body">
              <div class="small text-muted mb-2">
                Mã HĐ: <b class="font-monospace">{{ activeOrder?.maHoaDon }}</b>
              </div>

              <div class="fw-bold mb-2">
                Số tiền:
                <span class="text-danger">{{ money(grandTotal) }}</span>
              </div>

              <div class="border rounded-3 p-3 text-center">
                <div v-if="qrImg" class="d-flex justify-content-center">
                  <img
                    :src="qrImg"
                    alt="QR Pay"
                    style="width: 260px; height: 260px; object-fit: contain"
                  />
                </div>
                <div v-else class="text-muted small py-4">Đang tạo QR...</div>

                <div class="small text-muted mt-2">
                  Nội dung: <b class="font-monospace">{{ qrContent }}</b>
                </div>
              </div>

              <div class="mt-3">
                <label class="form-label mb-1">Ghi chú (tuỳ chọn)</label>
                <input
                  class="form-control"
                  v-model="qrNoteDraft"
                  placeholder="VD: CK QR - HDxxxx"
                />
              </div>

              <div class="small text-muted mt-2">
                Sau khi khách chuyển khoản xong, bấm
                <b>Đã nhận tiền → Tạo hóa đơn</b>.
              </div>
            </div>

            <div class="modal-footer">
              <button
                class="btn btn-outline-secondary"
                type="button"
                @click="closeQrPay"
              >
                Đóng
              </button>

              <button
                class="btn btn-success"
                type="button"
                @click="markPaidAndCheckout"
                :disabled="submitting"
              >
                Đã nhận tiền → Tạo hóa đơn
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- ✅ Pre-checkout confirm modal -->
      <div
        v-if="showPreCheckoutModal"
        class="modal fade show"
        tabindex="-1"
        role="dialog"
        aria-modal="true"
        style="display: block; z-index: 1060"
      >
        <div class="modal-dialog modal-md">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title fw-bold">Xác nhận phiếu giảm giá</h5>
              <button
                type="button"
                class="btn-close"
                @click="resolvePreCheckout(false)"
              ></button>
            </div>

            <div class="modal-body">
              <div
                class="fw-semibold mb-2"
                :class="preCheckoutUi.type === 'danger' ? 'text-danger' : ''"
              >
                {{ preCheckoutUi.message }}
              </div>

              <div v-if="preCheckoutUi.detail" class="small text-muted">
                {{ preCheckoutUi.detail }}
              </div>

              <div v-if="preCheckoutUi.suggest" class="mt-2 small">
                Gợi ý:
                <b>{{ preCheckoutUi.suggest.code }}</b>
                • giảm
                <b class="text-danger"
                  >-{{ money(preCheckoutUi.suggest.discount) }}</b
                >
              </div>
            </div>

            <div class="modal-footer">
              <button
                class="btn btn-outline-secondary"
                type="button"
                @click="resolvePreCheckout(false)"
              >
                Không
              </button>
              <button
                class="btn btn-success"
                type="button"
                @click="resolvePreCheckout(true)"
              >
                Có
              </button>
            </div>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup>
import {
  computed,
  ref,
  reactive,
  watch,
  onMounted,
  onBeforeUnmount,
} from "vue";
import http from "@/services/http";
import ghnLogo from "@/assets/ghn-logo.png.webp";
import {
  getAllDetails,
  decreaseStock,
  increaseStock,
} from "@/services/sanPhamChiTietApi";
import { listKhachHang } from "@/services/khachHangApi";
import VN_UNITS from "@/assets/vn_units.json";
/** =======================
 * CONFIG
 * ======================= */
const MAX_ORDERS = 10;
const STORAGE_KEY = "sales_store_only_v1";
const TZ = "Asia/Bangkok"; // dùng local máy là chính, TZ chỉ để tư duy

/** =======================
 * HELPERS: DATE KEY (YYYY-MM-DD theo local)
 * ======================= */
function getDateKeyLocal(d = new Date()) {
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd}`;
}

/** =======================
 * IMG URL
 * ======================= */
const apiBaseUrl = import.meta.env.VITE_API_URL || "http://localhost:8080";
const fileBaseUrl = (import.meta.env.VITE_FILE_BASE_URL || apiBaseUrl).replace(
  /\/api\/?$/,
  "",
);
const placeholderImg = "https://via.placeholder.com/56x56.png?text=IMG";
function buildImgUrl(path) {
  if (!path) return "";
  const p = String(path).replace(/\\/g, "/");
  if (p.startsWith("http://") || p.startsWith("https://")) return p;
  const b = String(fileBaseUrl).replace(/\/+$/, "");
  return b + (p.startsWith("/") ? p : `/${p}`);
}

/** =======================
 * TOAST
 * ======================= */
const toast = reactive({
  show: false,
  title: "",
  msg: "",
  type: "danger",
});
const toastClass = computed(() => {
  if (toast.type === "success") return "custom-toast-success";
  if (toast.type === "info") return "custom-toast-info";
  if (toast.type === "warning") return "custom-toast-warning";
  return "custom-toast-danger";
});
function toastShow(msg, type = "danger", title = "") {
  toast.show = true;
  toast.msg = msg;
  toast.type = type;

  if (title) {
    toast.title = title;
  } else {
    toast.title =
      type === "success"
        ? "Thành công"
        : type === "warning"
          ? "Cảnh báo"
          : type === "info"
            ? "Thông báo"
            : "Thất bại";
  }

  clearTimeout(toastShow._t);
  toastShow._t = setTimeout(() => (toast.show = false), 2600);
}
const toastInfo = (m) => toastShow(m, "info");

/** =======================
 * ORDER STATE
 * ======================= */
const orders = ref([]);
const activeId = ref(null);
const orderSeq = ref(1);
const submitting = ref(false);

const activeOrder = computed(
  () => orders.value.find((o) => o.id === activeId.value) || null,
);

/** =======================
 * RANDOM + CODE
 * ======================= */
function randInt(max) {
  try {
    const u = new Uint32Array(1);
    crypto.getRandomValues(u);
    return u[0] % max;
  } catch {
    return Math.floor(Math.random() * max);
  }
}
function genMaHoaDon(d = new Date()) {
  const yy = String(d.getFullYear()).slice(-2);
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  const r5 = String(randInt(100000)).padStart(5, "0");
  return `HD${yy}${mm}${dd}${r5}`;
}
function genUniqueMaHoaDon() {
  const existed = new Set(orders.value.map((o) => o.maHoaDon));
  let code = genMaHoaDon();
  while (existed.has(code)) code = genMaHoaDon();
  return code;
}

function getOrderItemCount(order) {
  if (!order || !Array.isArray(order.cart)) return 0;
  return order.cart.reduce((sum, item) => sum + Number(item.qty || 0), 0);
}


/** =======================
 * NORMALIZE ORDER
 * ======================= */
function normalizeOrder(o) {
  const ma = o?.maHoaDon || genMaHoaDon();
  return {
    id: o?.id ?? Date.now() + Math.random(),
    dbId: o?.dbId ?? null,
    maHoaDon: ma,
    label: o?.label || `Hóa Đơn - ${ma}`,
    cart: Array.isArray(o?.cart) ? o.cart : [],

    customer: o?.customer ?? null,
    customerDraft: o?.customerDraft ?? { phone: "", email: "" },
    diaChi: o?.diaChi || "",

    // ✅ note
    ghiChu: o?.ghiChu || "POS checkout",

    // ✅ ship mode
    loaiDon: !!o?.loaiDon,
    phiVanChuyen: Number(o?.phiVanChuyen || 0),

    // ✅ ship fields (PHẢI CÓ)
    tenNguoiNhanHang: o?.tenNguoiNhanHang || "",
    soDienThoaiNhanHang: o?.soDienThoaiNhanHang || "",
    tinhThanhNhanHang: o?.tinhThanhNhanHang || "", // đang lưu code
    quanHuyenNhanHang: o?.quanHuyenNhanHang || "", // nếu không dùng thì để ""
    phuongXaNhanHang: o?.phuongXaNhanHang || "", // đang lưu code
    diaChiNhanHangChiTiet: o?.diaChiNhanHangChiTiet || "",

    // vouchers...
    voucherCode: String(o?.voucherCode || ""),
    pggId: o?.pggId ?? null,
    voucherMode: o?.voucherMode ?? "best",
    voucherTab: o?.voucherTab ?? "best",
    voucherSnapshot: o?.voucherSnapshot ?? null,

    discountPercent: Number(o?.discountPercent || 0),
    paid: Number(o?.paid || 0),
  };
}
/** =======================
 * CREATE ORDER (DB draft)
 * ======================= */
async function createOrder() {
  if (orders.value.length >= MAX_ORDERS)
    return toastShow(`Chỉ tối đa ${MAX_ORDERS} đơn`, "warning");

  const localId = Date.now() + Math.random();
  const maHoaDon = genUniqueMaHoaDon();

  try {
    const res = await http.post("/api/hoa-don/taohoadon", { maHoaDon });
    const data = res?.data || {};
    const dbId = data.id;
    const maDb = data.maHoaDon || maHoaDon;

    orders.value.push(
      normalizeOrder({
        id: localId,
        dbId,
        maHoaDon: maDb,
        label: `Hóa Đơn - ${maDb}`,
        cart: [],
        customer: null,
        customerDraft: { phone: "", email: "" },
        diaChi: "",
        voucherCode: "",
        pggId: null,
        voucherMode: "best",
        voucherTab: "best",
        voucherSnapshot: null,
        discountPercent: 0,
        paid: 0,
        loaiDon: false,
        phiVanChuyen: 0,
      }),
    );
    activeId.value = localId;
    orderSeq.value++;
    return;
  } catch (e) {
    console.error(e);
    toastShow("Không tạo được hóa đơn ", "danger");
  }

  // fallback tab local (không dbId)
  orders.value.push(
    normalizeOrder({
      id: localId,
      maHoaDon,
      label: `Hóa Đơn - ${maHoaDon}`,
      cart: [],
      customer: null,
      customerDraft: { phone: "", email: "" },
      diaChi: "",
      voucherCode: "",
      pggId: null,
      voucherMode: "best",
      voucherTab: "best",
      voucherSnapshot: null,
      discountPercent: 0,
      paid: 0,
    }),
  );
  activeId.value = localId;
  orderSeq.value++;
}

/** =======================
 * CLOSE ORDER: cancel draft + trả kho theo cart (BE đã có endpoint)
 * ======================= */
async function closeOrder(id) {
  const idx = orders.value.findIndex((o) => o.id === id);
  if (idx === -1) return;

  const o = orders.value[idx];

  // nếu có dbId thì gọi cancel trên BE để hoàn tồn theo nghiệp vụ BE
  if (o?.dbId) {
    try {
      const items = (o.cart || [])
        .map((it) => ({
          idSanPhamChiTiet: Number(it.idSpct),
          soLuong: Number(it.qty || 0),
        }))
        .filter((x) => x.idSanPhamChiTiet && x.soLuong > 0);

      await http.post(`/api/hoa-don/draft/${o.dbId}/cancel`, {
        reason: "Đóng tab bán hàng",
        items,
      });
    } catch (e) {
      console.error(e);
      // không chặn đóng tab
    }
  }

  orders.value.splice(idx, 1);
  if (activeId.value === id) activeId.value = orders.value[0]?.id ?? null;
  saveDraftsNow();
}

/** =======================
 * DRAFT STORAGE + DAILY RESET (FE)
 * ======================= */
function saveDraftsNow() {
  try {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        dateKey: getDateKeyLocal(), // ✅ quan trọng để reset qua ngày
        orders: orders.value,
        activeId: activeId.value,
        orderSeq: orderSeq.value,
        savedAt: new Date().toISOString(),
      }),
    );
  } catch {}
}

function clearDraftsFE() {
  try {
    localStorage.removeItem(STORAGE_KEY);
  } catch {}
  orders.value = [];
  activeId.value = null;
  orderSeq.value = 1;
}

function loadDrafts() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return;
    const data = JSON.parse(raw);
    if (!data?.orders) return;

    const todayKey = getDateKeyLocal();
    if (data?.dateKey && data.dateKey !== todayKey) {
      // ✅ qua ngày mới => reset giỏ FE
      clearDraftsFE();
      return;
    }

    orders.value = data.orders.map((o) => normalizeOrder(o));
    activeId.value = data.activeId ?? orders.value[0]?.id ?? null;
    orderSeq.value = Number(data.orderSeq) || orders.value.length + 1;
  } catch {}
}

// auto-save debounce
let _saveT = null;
function scheduleSave() {
  clearTimeout(_saveT);
  _saveT = setTimeout(saveDraftsNow, 200);
}
watch(orders, scheduleSave, { deep: true });
watch(activeId, scheduleSave);
watch(orderSeq, scheduleSave);

// ✅ reset đúng 00:00 khi đang mở trang
let midnightTimer = null;
async function handleMidnightReset() {
  // 1) FE reset
  clearDraftsFE();
}

function scheduleMidnightReset() {
  if (midnightTimer) clearTimeout(midnightTimer);

  const now = new Date();
  const next = new Date(now);
  next.setHours(24, 0, 0, 0); // next midnight theo local

  midnightTimer = setTimeout(async () => {
    await handleMidnightReset();
    scheduleMidnightReset(); // schedule tiếp
  }, next - now);
}

/** =======================
 * MONEY + DATE
 * ======================= */
function formatDateVN(d) {
  if (!d) return "—";
  const dt = new Date(d);
  if (Number.isNaN(dt.getTime())) return "—";
  const dd = String(dt.getDate()).padStart(2, "0");
  const mm = String(dt.getMonth() + 1).padStart(2, "0");
  const yy = dt.getFullYear();
  return `${dd}/${mm}/${yy}`;
}
function money(n) {
  const v = Number(n) || 0;
  return v.toLocaleString("vi-VN") + " đ";
}

/** =======================
 * PAID INPUT FORMAT
 * ======================= */
function formatMoneyInput(n) {
  const v = Number(n) || 0;
  return v.toLocaleString("vi-VN");
}
function onPaidInput(e) {
  const o = activeOrder.value;
  if (!o) return;
  const raw = String(e.target.value || "");
  const num = Number(raw.replace(/[^\d]/g, "")) || 0;
  o.paid = num;
  e.target.value = formatMoneyInput(num);
}
function onPaidBlur(e) {
  const o = activeOrder.value;
  if (!o) return;
  e.target.value = formatMoneyInput(o.paid);
}

/** =======================
 * MODALS
 * ======================= */
const showProductModal = ref(false);
const showCustomerModal = ref(false);
const showQrPayModal = ref(false);

const anyModalOpen = computed(
  () =>
    showProductModal.value || showCustomerModal.value || showQrPayModal.value,
);

watch(anyModalOpen, (open) => {
  document.body.classList.toggle("modal-open", open);
});

function closeAnyModal() {
  showProductModal.value = false;
  showCustomerModal.value = false;
  showQrPayModal.value = false;
}

/** =======================
 * PRODUCTS (DB) - PAGINATION
 * ======================= */
const products = ref([]);

const productLoading = ref(false);

const productFilters = reactive({
  keyword: "",
  color: "",
  size: "",
  stockStatus: "",
});

const productPriceRange = reactive({
  min: 0,
  max: 100000000,
});

const productPage = ref(0);
const productSize = ref(10);
const productTotal = ref(0);
const productTotalPages = ref(0);
const productLast = ref(false);

function mapSpct(x) {
  const stock = Number(x.soLuongTon || 0);
  const idSpct = Number(x.id);
  return {
    idSpct,
    code: x.maSanPhamChiTiet || "",
    name: x.tenSanPham || "",
    color: x.tenMauSac || "",
    size: x.tenKichCo || "",
    stock,
    _baseStock: stock,
    price: Number(x.donGia || 0),
    image: x.anh ? buildImgUrl(x.anh) : placeholderImg,
  };
}
function findModalProductById(idSpct) {
  const id = Number(idSpct);
  return products.value.find((x) => Number(x.idSpct) === id) || null;
}

async function fetchProducts(page = 0) {
  if (productLoading.value) return;
  productLoading.value = true;
  try {
    const res = await getAllDetails(page, productSize.value);
    const data = res?.data ?? res;

    const content = Array.isArray(data) ? data : data?.content || [];
    products.value = content.map(mapSpct);

    if (!Array.isArray(data)) {
      productTotal.value = Number(data?.totalElements || 0);
      productTotalPages.value = Number(data?.totalPages || 0);
      productPage.value = Number(data?.number ?? page);
      productLast.value = !!data?.last;
    } else {
      productTotal.value = products.value.length;
      productTotalPages.value = 1;
      productPage.value = 0;
      productLast.value = true;
    }
  } catch (e) {
    console.error(e);
    toastShow("Không tải được danh sách biến thể từ DB", "danger");
  } finally {
    productLoading.value = false;
  }
}

async function reloadProducts() {
  await fetchProducts(0);
}
function productPrev() {
  if (productPage.value <= 0) return;
  fetchProducts(productPage.value - 1);
}
function productNext() {
  if (productLast.value) return;
  fetchProducts(productPage.value + 1);
}
function onProductSizeChange() {
  fetchProducts(0);
}
const productPriceBounds = computed(() => {
  if (!products.value.length) {
    return { min: 0, max: 100000000 };
  }

  const prices = products.value.map((p) => Number(p.price || 0));
  return {
    min: Math.min(...prices),
    max: Math.max(...prices),
  };
});

const productPriceStep = computed(() => {
  const max = Number(productPriceBounds.value.max || 0);

  if (max <= 100000) return 1000;
  if (max <= 1000000) return 10000;
  return 50000;
});
watch(
  productPriceBounds,
  (bounds) => {
    if (
      productPriceRange.min < bounds.min ||
      productPriceRange.min > bounds.max
    ) {
      productPriceRange.min = bounds.min;
    }

    if (
      productPriceRange.max > bounds.max ||
      productPriceRange.max < bounds.min
    ) {
      productPriceRange.max = bounds.max;
    }

    if (productPriceRange.min > productPriceRange.max) {
      productPriceRange.min = bounds.min;
      productPriceRange.max = bounds.max;
    }
  },
  { immediate: true },
);
function onPriceRangeMinInput() {
  if (productPriceRange.min > productPriceRange.max) {
    productPriceRange.min = productPriceRange.max;
  }
}

function onPriceRangeMaxInput() {
  if (productPriceRange.max < productPriceRange.min) {
    productPriceRange.max = productPriceRange.min;
  }
}
const productColorOptions = computed(() => {
  return [...new Set(products.value.map((p) => p.color).filter(Boolean))].sort(
    (a, b) => String(a).localeCompare(String(b), "vi"),
  );
});

const productSizeOptions = computed(() => {
  return [...new Set(products.value.map((p) => p.size).filter(Boolean))].sort(
    (a, b) => String(a).localeCompare(String(b), "vi"),
  );
});

const filteredProducts = computed(() => {
  const kw = String(productFilters.keyword || "")
    .trim()
    .toLowerCase();
  const color = String(productFilters.color || "")
    .trim()
    .toLowerCase();
  const size = String(productFilters.size || "")
    .trim()
    .toLowerCase();
  const stockStatus = String(productFilters.stockStatus || "").trim();

  const minPrice = Number(productPriceRange.min || 0);
  const maxPrice = Number(productPriceRange.max || 0);

  return products.value.filter((p) => {
    const textMatched =
      !kw ||
      [p.code, p.name, p.color, p.size].some((x) =>
        String(x || "")
          .toLowerCase()
          .includes(kw),
      );

    const colorMatched =
      !color || String(p.color || "").toLowerCase() === color;

    const sizeMatched = !size || String(p.size || "").toLowerCase() === size;

    const stockMatched =
      !stockStatus ||
      (stockStatus === "in" && Number(p.stock || 0) > 0) ||
      (stockStatus === "out" && Number(p.stock || 0) <= 0);

    const price = Number(p.price || 0);
    const priceMatched = price >= minPrice && price <= maxPrice;

    return (
      textMatched && colorMatched && sizeMatched && stockMatched && priceMatched
    );
  });
});

function resetProductFilters() {
  productFilters.keyword = "";
  productFilters.color = "";
  productFilters.size = "";
  productFilters.stockStatus = "";

  productPriceRange.min = productPriceBounds.value.min;
  productPriceRange.max = productPriceBounds.value.max;
}

async function openProductModal() {
  showCustomerModal.value = false;
  showProductModal.value = true;
  await fetchProducts(0);
  resetProductFilters();
}
function closeProductModal() {
  showProductModal.value = false;
}

/** =======================
 * CART + STOCK SYNC
 * ======================= */
function clampInt(n, min, max) {
  n = Number.isFinite(Number(n)) ? Math.floor(Number(n)) : min;
  return Math.max(min, Math.min(max, n));
}

function sameMoney(a, b) {
  return Math.round(Number(a || 0)) === Math.round(Number(b || 0));
}

function syncAllCartStocks() {
  const o = activeOrder.value;
  if (!o) return;

  const totalMap = new Map();
  for (const it of o.cart) {
    const id = Number(it.idSpct);
    totalMap.set(id, (totalMap.get(id) || 0) + Number(it.qty || 0));
  }

  for (const it of o.cart) {
    const base = Number(it.stockBase || 0);
    const totalQty = totalMap.get(Number(it.idSpct)) || 0;
    it.stock = Math.max(0, base - totalQty);
  }
}

async function setQtyByInput(cartIndex, nextQtyRaw) {
  const o = activeOrder.value;
  if (!o) return;
  const it = o.cart[cartIndex];
  if (!it) return;

  const nextQty = clampInt(nextQtyRaw, 1, 999999);
  const curQty = Number(it.qty || 0);
  if (nextQty === curQty) return;

  const delta = nextQty - curQty;

  try {
    if (delta > 0) await decreaseStock(Number(it.idSpct), delta);
    else if (delta < 0) await increaseStock(Number(it.idSpct), Math.abs(delta));
  } catch (err) {
  const rawMsg =
    err?.response?.data?.message ||
    err?.response?.data ||
    "";

  const remain = Math.max(0, Number(it.stock || 0));
  const wantedQty = Number(nextQty || 0);

  let fallbackMsg = "";

  if (remain <= 0) {
    fallbackMsg = `Sản phẩm ${it.code} hiện đã hết tồn kho trong DB, không thể tăng thêm số lượng`;
  } else {
    fallbackMsg =
      `Số lượng mua không được vượt quá số lượng tồn kho còn lại tồn ${remain} `;
  }


  const msg =
    String(rawMsg).trim() === "Không đủ tồn kho"
      ? fallbackMsg
      : rawMsg || fallbackMsg;

  return toastShow(String(msg), "warning");
}

  const p = findModalProductById(it.idSpct);
  if (p) {
    p.stock = Math.max(0, Number(p.stock || 0) - delta);
  }

  it.qty = nextQty;
  syncAllCartStocks();
}

async function incQty(i) {
  const o = activeOrder.value;
  if (!o) return;
  const it = o.cart[i];
  if (!it) return;
  await setQtyByInput(i, Number(it.qty || 0) + 1);
}
async function decQty(i) {
  const o = activeOrder.value;
  if (!o) return;
  const it = o.cart[i];
  if (!it) return;
  await setQtyByInput(i, Math.max(1, Number(it.qty || 0) - 1));
}

async function removeItem(i) {
  const o = activeOrder.value;
  if (!o) return;
  const it = o.cart[i];
  if (!it) return;

  try {
    await increaseStock(Number(it.idSpct), Number(it.qty || 0));
  } catch (err) {
    const msg =
      err?.response?.data?.message ||
      err?.response?.data ||
      "Không thể hoàn tồn kho";
    return toastShow(String(msg), "warning");
  }

  const p = findModalProductById(it.idSpct);
  if (p) p.stock = Number(p.stock || 0) + Number(it.qty || 0);

  o.cart.splice(i, 1);
  syncAllCartStocks();
}

function onQtyInput(idx, e) {
  const raw = String(e?.target?.value ?? "");
  e.target.value = raw.replace(/[^\d]/g, "");
}
async function onQtyBlur(idx, e) {
  const o = activeOrder.value;
  if (!o) return;
  const it = o.cart[idx];
  if (!it) return;

  const raw = String(e?.target?.value ?? "");
  const num = Number(raw.replace(/[^\d]/g, "")) || 1;

  await setQtyByInput(idx, num);
  e.target.value = it.qty;
}

async function chooseProduct(p) {
  const o = activeOrder.value;
  if (!o) return;

  const id = Number(p.idSpct);
  if (!Number.isFinite(id))
    return toastShow("Sản phẩm không hợp lệ", "warning");
 if ((Number(p.stock) || 0) <= 0) {
  return toastShow(
    `Sản phẩm ${p.code} hiện đã hết tồn kho`,
    "warning",
  );
}

  let idx = o.cart.findIndex(
    (x) => Number(x.idSpct) === id && sameMoney(x.price, p.price),
  );

  if (idx === -1) {
    const baseBefore = Number(p.stock || 0);
    o.cart.push({
      key: `${id}-${Math.round(Number(p.price || 0))}-${Date.now()}-${Math.random()}`,
      idSpct: id,
      code: p.code,
      name: p.name,
      meta: `size ${p.size} / ${p.color}`,
      image: p.image,
      price: Number(p.price || 0),
      qty: 0,
      stockBase: baseBefore,
      stock: baseBefore,
    });
    idx = o.cart.length - 1;
  }

  await setQtyByInput(idx, Number(o.cart[idx].qty || 0) + 1);
  toastShow(`Đã thêm ${p.code}`, "success");
}

/** =======================
 * TOTALS  ✅ (GIỮ 1 LẦN DUY NHẤT)
 * ======================= */
const subTotal = computed(() => {
  const o = activeOrder.value;
  if (!o) return 0;
  return o.cart.reduce(
    (s, it) => s + (Number(it.price) || 0) * (Number(it.qty) || 0),
    0,
  );
});

/** =======================
 * CUSTOMERS (DB)
 * ======================= */
const customers = ref([]);
const customerKw = ref("");
const customerLoading = ref(false);

const customerPage = ref(0);
const customerSize = ref(10);

function mapCustomer(x) {
  const addr =
    x.diaChi ||
    [x.diaChiChiTiet, x.phuongXa, x.quanHuyen, x.tinhThanh]
      .filter(Boolean)
      .join(", ") ||
    "";
  return {
    id: x.id,
    name: x.tenKhachHang || "",
    phone: x.soDienThoai || "",
    email: x.email || "",
    address: addr,
    raw: x,
  };
}

async function fetchCustomers(page = 0) {
  if (customerLoading.value) return;
  customerLoading.value = true;
  try {
    const res = await listKhachHang(page, customerSize.value);
    const data = res?.data ?? res;

    const list = Array.isArray(data) ? data : data?.content || [];
    customers.value = list.map(mapCustomer);

    customerPage.value = 0;
  } catch (e) {
    console.error(e);
    toastShow("Không tải được danh sách khách hàng từ DB", "danger");
  } finally {
    customerLoading.value = false;
  }
}

async function reloadCustomers() {
  await fetchCustomers(0);
}
function customerPrev() {
  if (customerPage.value <= 0) return;
  customerPage.value--;
}

function customerNext() {
  if (customerLast.value) return;
  customerPage.value++;
}

function onCustomerSizeChange() {
  customerPage.value = 0;
}

const filteredCustomers = computed(() => {
  const kw = customerKw.value.trim().toLowerCase();
  if (!kw) return customers.value;

  return customers.value.filter((c) =>
    [c.name, c.phone, c.email, c.address].some((x) =>
      String(x || "")
        .toLowerCase()
        .includes(kw),
    ),
  );
});

const pagedCustomers = computed(() => {
  const start = customerPage.value * customerSize.value;
  const end = start + customerSize.value;
  return filteredCustomers.value.slice(start, end);
});

const customerTotalPages = computed(() => {
  return Math.max(
    1,
    Math.ceil(filteredCustomers.value.length / customerSize.value),
  );
});

const customerLast = computed(() => {
  return customerPage.value >= customerTotalPages.value - 1;
});

const customerTotal = computed(() => filteredCustomers.value.length);

watch(customerKw, () => {
  customerPage.value = 0;
});


function openCustomerModal() {
  showProductModal.value = false;
  showCustomerModal.value = true;
  fetchCustomers(0);
}
function closeCustomerModal() {
  showCustomerModal.value = false;
}
function chooseCustomer(c) {
  const o = activeOrder.value;
  if (!o) return;
  o.customer = {
    id: c.id,
    name: c.name,
    phone: c.phone,
    email: c.email,
    address: c.address,
  };
  o.customerDraft.phone = c.phone || "";
  o.customerDraft.email = c.email || "";
  if (!String(o.diaChi || "").trim()) o.diaChi = c.address || "";
  closeCustomerModal();
  loadVouchers();
}
function resetToWalkInCustomer() {
  const o = activeOrder.value;
  if (!o) return;

  o.customer = null;
  o.customerDraft = { phone: "", email: "" };
  o.diaChi = "";

  customerKw.value = "";
  customerPage.value = 0;

  loadVouchers();
  closeCustomerModal();

  toastShow("Đã đặt lại về Khách lẻ", "info");
}

/** =======================
 * VOUCHERS
 * ======================= */
/** =======================
 * ✅ ADDRESS CBB (OFFLINE) - 2 CẤP: Province -> Wards
 * JSON: [{ Code, FullName/Name, Wards:[{Code, FullName/Name, ProvinceCode}] }]
 * ======================= */

const provinces = ref([]);
const wards = ref([]);

const provincesLoading = ref(false);
const wardsLoading = ref(false);

function cleanCode(v) {
  const s = String(v ?? "").trim();
  if (!s || s === "undefined" || s === "null") return "";
  return s;
}
function cleanName(v) {
  return String(v ?? "").trim();
}

function normProvince(p) {
  const code = cleanCode(p?.Code);
  const name = cleanName(p?.FullName ?? p?.Name);
  if (!code || !name) return null;
  return { code, name, wardsRaw: Array.isArray(p?.Wards) ? p.Wards : [] };
}

function normWard(w) {
  const code = cleanCode(w?.Code);
  const name = cleanName(w?.FullName ?? w?.Name);
  if (!code || !name) return null;
  return { code, name };
}

const provinceNodeByCode = new Map();

function initAddressDataOffline() {
  provincesLoading.value = true;
  try {
    const root = VN_UNITS?.data ?? VN_UNITS;

    if (!Array.isArray(root)) {
      console.error("VN_UNITS root must be array. Sample:", root);
      provinces.value = [];
      wards.value = [];
      return;
    }

    const pNorm = root.map(normProvince).filter(Boolean);

    provinceNodeByCode.clear();
    for (const p of pNorm) provinceNodeByCode.set(p.code, p);

    provinces.value = pNorm.sort((a, b) => a.name.localeCompare(b.name, "vi"));

    wards.value = [];
  } finally {
    provincesLoading.value = false;
  }
}

function onProvinceChange(provinceCode) {
  const o = activeOrder.value;
  if (!o) return;

  o.tinhThanhNhanHang = cleanCode(provinceCode);

  wardsLoading.value = true;
  try {
    // reset downstream
    o.phuongXaNhanHang = "";
    // nếu vẫn có field huyện thì reset luôn cho sạch
    o.quanHuyenNhanHang = "";

    const p = provinceNodeByCode.get(o.tinhThanhNhanHang);
    wards.value = (p?.wardsRaw || [])
      .map(normWard)
      .filter(Boolean)
      .sort((a, b) => a.name.localeCompare(b.name, "vi"));
  } finally {
    wardsLoading.value = false;
  }
}
function toggleShip(e) {
  const o = activeOrder.value;
  if (!o) return;

  o.loaiDon = !!e.target.checked;

  if (o.loaiDon) {
    o.phiVanChuyen = 30000;
    if (provinces.value.length === 0) initAddressDataOffline();
  } else {
    o.phiVanChuyen = 0;

    // reset ship fields
    o.tenNguoiNhanHang = "";
    o.soDienThoaiNhanHang = "";
    o.tinhThanhNhanHang = "";
    o.quanHuyenNhanHang = "";
    o.phuongXaNhanHang = "";
    o.diaChiNhanHangChiTiet = "";

    wards.value = [];
  }
}
// ✅ Ship fee rule: nội địa 30k, quốc tế nhập tay

function onShipModeChange() {
  const o = activeOrder.value;
  if (!o) return;
  if (!o.shipQuocTe) o.phiVanChuyen = 30000;
  else o.phiVanChuyen = Number(o.phiVanChuyen || 0);
}

// input nhập phí quốc tế giống paid
function onShipFeeInput(e) {
  const o = activeOrder.value;
  if (!o) return;
  const raw = String(e.target.value || "");
  const num = Number(raw.replace(/[^\d]/g, "")) || 0;
  o.phiVanChuyen = num;
  e.target.value = formatMoneyInput(num);
}
function onShipFeeBlur(e) {
  const o = activeOrder.value;
  if (!o) return;
  e.target.value = formatMoneyInput(o.phiVanChuyen);
}

const vouchers = ref([]);
const confirmHint = ref("");
function pickId(obj) {
  if (!obj || typeof obj !== "object") return null;
  // nhiều kiểu backend hay trả
  return (
    obj.id ??
    obj.khachHangId ??
    obj.idKhachHang ??
    obj.value ??
    obj.khachHang?.id ??
    obj.customer?.id ??
    obj.khach_hang?.id ??
    null
  );
}

function normalizeKhIds(raw) {
  if (!raw) return null;

  if (Array.isArray(raw)) {
    const ids = raw
      .map((x) => {
        if (typeof x === "number") return x;
        if (typeof x === "string") return Number(x);
        const id = pickId(x);
        return id != null ? Number(id) : NaN;
      })
      .filter((n) => Number.isFinite(n));
    return ids.length ? ids : null;
  }

  if (typeof raw === "string") {
    // "1,2,3"
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
    gia_tri_giam_toi_da: Number(
      x.giaTriGiamToiDa ?? x.gia_tri_giam_toi_da ?? 0,
    ),

    loai_phieu: x.loaiPhieu ?? x.loai_phieu ?? null,

    // ✅ CHUẨN HOÁ Ở ĐÂY
    khach_hang_ids: normalizeKhIds(khIdsRaw),
    khach_hang_id: khSingle != null ? Number(khSingle) : null,

    ngay_bat_dau: x.ngayBatDau ?? x.ngay_bat_dau ?? null,
    ngay_ket_thuc: x.ngayKetThuc ?? x.ngay_ket_thuc ?? null,
  };
}

async function loadVouchers() {
  try {
    const res = await http.get("/api/pgg/pos", {
      params: { khachHangId: activeOrder.value?.customer?.id ?? null },
    });
    vouchers.value = (res.data || []).map(normalizeVoucher);
  } catch (e) {
    console.error(e);
    toastShow("Không tải được danh sách PGG", "danger");
  }
}

function isPersonalVoucher(v) {
  const lp = v?.loai_phieu;
  if (lp === true) return true;
  if (lp === false) return false;
  const s = String(lp || "").toUpperCase();
  return s === "CA_NHAN" || s === "PERSONAL";
}
function isVoucherOwnedByCustomer(v, customerId) {
  const cid = Number(customerId);
  if (!cid) return false;

  const ids = v?.khach_hang_ids;
  if (Array.isArray(ids) && ids.length) {
    return ids.map(Number).includes(cid);
  }

  const single = v?.khach_hang_id;
  if (single != null) {
    return Number(single) === cid;
  }

  // Backend /api/pgg/pos đã lọc đúng voucher cá nhân theo khách hàng rồi,
  // nếu FE không có thông tin owner thì coi như hợp lệ
  if (isPersonalVoucher(v)) return true;

  return false;
}

function setVoucherSnapshot(o, v) {
  if (!o || !v) return;
  o.voucherSnapshot = {
    id: v.id,
    code: v.ma_giam_gia || "",
    minOrder: Number(v.don_hang_toi_thieu || 0),
    soLuong: Number(v.so_luong || 0),
    trangThai: !!v.trang_thai,
    loaiGiam: !!v.loai_giam,
    phanTram: Number(v.gia_tri_phan_tram || 0),
    tienMat: Number(v.gia_tri_tien_mat || 0),
    giamToiDa: Number(v.gia_tri_giam_toi_da || 0),
    start: v.ngay_bat_dau || null,
    end: v.ngay_ket_thuc || null,
    loaiPhieu: v.loai_phieu ?? null,
    savedAt: Date.now(),
  };
}

function toTime(d) {
  if (!d) return null;
  const t = new Date(d).getTime();
  return Number.isNaN(t) ? null : t;
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
  if (v.loai_giam) disc = (st * (Number(v.gia_tri_phan_tram) || 0)) / 100;
  else disc = Number(v.gia_tri_tien_mat) || 0;

  const cap = Number(v.gia_tri_giam_toi_da) || 0;
  if (cap > 0) disc = Math.min(disc, cap);

  disc = Math.max(0, Math.min(disc, st));
  return Math.floor(disc);
}

const eligibleVoucherEntries = computed(() => {
  const st = subTotal.value;
  return vouchers.value
    .map((v) => ({ v, discount: calcVoucherDiscount(st, v) }))
    .filter((x) => x.discount > 0)
    .sort((a, b) => b.discount - a.discount);
});

const bestEligibleVoucherEntry = computed(
  () => eligibleVoucherEntries.value[0] || null,
);

// ✅ Mã thay thế: TẤT CẢ voucher áp dụng được, sort từ thấp -> cao
const altEligibleVoucherEntries = computed(() => {
  return [...eligibleVoucherEntries.value].sort(
    (a, b) => a.discount - b.discount,
  );
});

const appliedVoucher = computed(() => {
  const o = activeOrder.value;
  if (!o?.pggId) return null;
  return vouchers.value.find((x) => x.id === o.pggId) || null;
});

const appliedVoucherEntry = computed(() => {
  const o = activeOrder.value;
  if (!o?.pggId) return null;
  const v = vouchers.value.find((x) => x.id === o.pggId) || null;
  if (!v) return null;
  return { v, discount: calcVoucherDiscount(subTotal.value, v) };
});

const bestVoucherEntryUI = computed(() => {
  const o = activeOrder.value;
  const applied = appliedVoucherEntry.value;
  if (o?.pggId && applied && applied.discount <= 0) return applied;
  return bestEligibleVoucherEntry.value;
});

const altVoucherEntriesUI = computed(() => {
  const o = activeOrder.value;
  const applied = appliedVoucherEntry.value;

  if (o?.pggId && applied && applied.discount <= 0) {
    return [...eligibleVoucherEntries.value].sort(
      (a, b) => a.discount - b.discount,
    );
  }
  return altEligibleVoucherEntries.value;
});

/** ✅ FIX LOGIC: lọc thêm isVoucherInDateRange để tránh gợi ý voucher hết hạn/chưa bắt đầu */
const voucherUpsellHint = computed(() => {
  const st = subTotal.value;
  if (st <= 0) return null;

  const bestNow = bestEligibleVoucherEntry.value?.discount || 0;

  const candidates = vouchers.value
    .filter(
      (v) =>
        v?.trang_thai &&
        (Number(v.so_luong) || 0) > 0 &&
        isVoucherInDateRange(v),
    )
    .map((v) => {
      const min = Number(v.don_hang_toi_thieu || 0);
      const missing = Math.max(0, min - st);
      if (missing <= 0) return null;
      const discAtMin = calcVoucherDiscount(min, v);
      return { v, missing, discAtMin, min };
    })
    .filter(Boolean)
    .sort((a, b) => b.discAtMin - a.discAtMin);

  if (candidates.length === 0) return null;
  const bestFuture = candidates[0];
  if ((bestFuture.discAtMin || 0) <= bestNow) return null;

  return {
    missing: bestFuture.missing,
    code: bestFuture.v.ma_giam_gia,
    expectedDiscount: bestFuture.discAtMin,
    minOrder: bestFuture.min,
  };
});

// auto pick best when mode=best
watch(
  [subTotal, vouchers, activeId, () => activeOrder.value?.customer?.id],
  () => {
    const o = activeOrder.value;
    if (!o) return;

    if (o.voucherMode === "none") {
      o.pggId = null;
      o.voucherCode = "";
      o.voucherSnapshot = null;
      return;
    }

    if (o.voucherMode === "best") {
      const best = bestEligibleVoucherEntry.value?.v || null;
      o.pggId = best?.id ?? null;
      o.voucherCode = best?.ma_giam_gia || "";
      if (best) setVoucherSnapshot(o, best);
      else o.voucherSnapshot = null;
    }
  },
  { immediate: true },
);

const voucherSuggestions = computed(() => {
  const st = subTotal.value;
  if (st <= 0) return [];

  const bestNow = bestEligibleVoucherEntry.value?.discount || 0;

  const basicValid = (v) =>
    v?.trang_thai && (Number(v.so_luong) || 0) > 0 && isVoucherInDateRange(v);

  return vouchers.value
    .filter(basicValid)
    .map((v) => {
      const min = Number(v.don_hang_toi_thieu || 0);
      const missing = min - st;
      if (missing <= 0) return null;

      const expectedDiscount = calcVoucherDiscount(min, v);
      if (expectedDiscount <= bestNow) return null;

      return { v, missing, minOrder: min, expectedDiscount };
    })
    .filter(Boolean)
    .sort(
      (a, b) =>
        a.missing - b.missing || b.expectedDiscount - a.expectedDiscount,
    )
    .slice(0, 6);
});

function applyVoucherManual(v) {
  const o = activeOrder.value;
  if (!o) return;

  if (isPersonalVoucher(v)) {
    const cid = o?.customer?.id;
    if (!cid)
      return toastShow(
        "Voucher cá nhân: vui lòng chọn khách hàng trước",
        "warning",
      );
  }

  o.voucherMode = "manual";
  o.pggId = v.id;
  o.voucherCode = v.ma_giam_gia || "";
  o.discountPercent = 0;
  setVoucherSnapshot(o, v);

  toastShow(`Đã chọn ${v.ma_giam_gia}`, "info");
}

function disableVoucher() {
  const o = activeOrder.value;
  if (!o) return;

  o.voucherMode = "none";
  o.pggId = null;
  o.voucherCode = "";
  o.discountPercent = 0;
  o.voucherSnapshot = null;

  toastShow("Đã tắt mã giảm giá", "info");
}

function clearVoucherManual() {
  const o = activeOrder.value;
  if (!o) return;

  o.voucherCode = "";
  o.voucherMode = "best";

  const best = bestEligibleVoucherEntry.value?.v || null;
  o.pggId = best?.id ?? null;
  o.voucherCode = best?.ma_giam_gia || "";
  if (best) setVoucherSnapshot(o, best);
  else o.voucherSnapshot = null;
}

async function applyPggByCode() {
  const o = activeOrder.value;
  if (!o) return;

  const code = (o.voucherCode || "").trim().toUpperCase();
  if (!code) return clearVoucherManual();

  const found = vouchers.value.find(
    (v) => String(v.ma_giam_gia).toUpperCase() === code,
  );
  if (!found) return toastShow("Mã PGG không tồn tại", "danger");

  if (isPersonalVoucher(found)) {
    const cid = o?.customer?.id;
    if (!cid)
      return toastShow(
        "Voucher cá nhân: vui lòng chọn khách hàng trước",
        "warning",
      );
  }

  const disc = calcVoucherDiscount(subTotal.value, found);
  if (disc <= 0)
    return toastShow("Mã không áp dụng được cho đơn hiện tại", "warning");

  applyVoucherManual(found);
}

/** =======================
 * DISCOUNT + TOTAL
 * ======================= */
const discountMoney = computed(() => {
  const o = activeOrder.value;
  if (!o) return 0;
  const st = subTotal.value;

  if (o.pggId) {
    const v = appliedVoucher.value;
    if (!v) return 0;
    return calcVoucherDiscount(st, v);
  }

  const percent = Math.max(0, Math.min(100, Number(o.discountPercent || 0)));
  return Math.floor((st * percent) / 100);
});
const shipMoney = computed(() => {
  const o = activeOrder.value;
  if (!o) return 0;
  return o.loaiDon ? Math.max(0, Number(o.phiVanChuyen || 0)) : 0;
});

const grandTotal = computed(() =>
  Math.max(0, subTotal.value - discountMoney.value + shipMoney.value),
);
const changeMoney = computed(() => {
  const o = activeOrder.value;
  if (!o) return 0;
  return Math.max(0, Number(o.paid || 0) - grandTotal.value);
});

/** =======================
 * PRE-CHECKOUT CONFIRM (modal)
 * ======================= */
const showPreCheckoutModal = ref(false);
const preCheckoutUi = reactive({
  type: "info",
  message: "",
  detail: "",
  suggest: null,
});
let _resolveConfirm = null;
async function confirmOrderCash() {
  const o = activeOrder.value;
  if (!o) return;

  o.paymentMethod = "CASH";
  o.maGiaoDich = null;
  o.ghiChuThanhToan = null;

  await confirmOrder();
}

function openConfirm({ type = "info", message, detail = "", suggest = null }) {
  preCheckoutUi.type = type;
  preCheckoutUi.message = message;
  preCheckoutUi.detail = detail;
  preCheckoutUi.suggest = suggest;
  showPreCheckoutModal.value = true;

  return new Promise((resolve) => {
    _resolveConfirm = resolve;
  });
}
function resolvePreCheckout(ok) {
  showPreCheckoutModal.value = false;
  const r = _resolveConfirm;
  _resolveConfirm = null;
  if (r) r(!!ok);
}

function getVoucherInvalidReason(v, subtotal) {
  if (!v) return "Voucher không tồn tại";
  if (!v.trang_thai) return "Voucher đã bị tắt";
  if ((Number(v.so_luong) || 0) <= 0) return "Voucher đã hết lượt";
  if (!isVoucherInDateRange(v)) return "Voucher đã hết hạn / chưa bắt đầu";
  if ((Number(subtotal) || 0) < (Number(v.don_hang_toi_thieu) || 0))
    return "Đơn hàng chưa đạt đơn tối thiểu";
  return null;
}

function getVoucherChangedFields(snap, vNow) {
  if (!snap || !vNow) return [];
  const changes = [];

  const minOld = Number(snap.minOrder || 0);
  const minNew = Number(vNow.don_hang_toi_thieu || 0);
  if (minOld !== minNew)
    changes.push(`Đơn tối thiểu: ${money(minOld)} → ${money(minNew)}`);

  const endOld = String(snap.end || "");
  const endNew = String(vNow.ngay_ket_thuc || "");
  if (endOld !== endNew)
    changes.push(`Hạn dùng: ${formatDateVN(endOld)} → ${formatDateVN(endNew)}`);

  const qtyOld = Number(snap.soLuong || 0);
  const qtyNew = Number(vNow.so_luong || 0);
  if (qtyOld !== qtyNew) changes.push(`Số lượng còn: ${qtyOld} → ${qtyNew}`);

  const stOld = !!snap.trangThai;
  const stNew = !!vNow.trang_thai;
  if (stOld !== stNew)
    changes.push(
      `Trạng thái: ${stOld ? "Bật" : "Tắt"} → ${stNew ? "Bật" : "Tắt"}`,
    );

  const lpOld = String(snap.loaiPhieu ?? "");
  const lpNew = String(vNow.loai_phieu ?? "");
  if (lpOld !== lpNew)
    changes.push(`Loại phiếu: ${lpOld || "-"} → ${lpNew || "-"}`);

  return changes;
}

function getBestEligibleNow() {
  const best = bestEligibleVoucherEntry.value;
  if (!best) return null;
  return { id: best.v.id, code: best.v.ma_giam_gia, discount: best.discount };
}
function applyBestVoucherNow() {
  const o = activeOrder.value;
  if (!o) return;
  const best = bestEligibleVoucherEntry.value?.v || null;
  if (!best) return;
  o.voucherMode = "manual";
  o.pggId = best.id;
  o.voucherCode = best.ma_giam_gia || "";
  o.discountPercent = 0;
  setVoucherSnapshot(o, best);
}
function removeVoucherNow() {
  const o = activeOrder.value;
  if (!o) return;
  o.voucherMode = "none";
  o.pggId = null;
  o.voucherCode = "";
  o.voucherSnapshot = null;
}

async function runVoucherPrecheckFlow() {
  const o = activeOrder.value;
  if (!o || !o.pggId) return true;

  // reload danh sách voucher theo khách (như bạn đang làm)
  await loadVouchers();

  const snap = o.voucherSnapshot;
  const vNow = vouchers.value.find((x) => x.id === o.pggId) || null;

  // best hiện tại (eligible)
  const best = bestEligibleVoucherEntry.value
    ? {
        id: bestEligibleVoucherEntry.value.v.id,
        code: bestEligibleVoucherEntry.value.v.ma_giam_gia,
        discount: bestEligibleVoucherEntry.value.discount,
      }
    : null;

  // 1) Voucher đang chọn không còn nằm trong list (thường do hết hạn/bị tắt/hết lượt/không thuộc KH)
  if (!vNow) {
    const ok = await openConfirm({
      type: "danger",
      message: `Voucher "${o.voucherCode || snap?.code || ""}" hiện không còn khả dụng.`,
      detail: best
        ? `Bạn có muốn đổi sang mã tốt nhất hiện tại (${best.code}) không?`
        : "Không có mã khác áp dụng được. Bạn có muốn bỏ voucher để tiếp tục thanh toán không?",
      suggest: best,
    });

    if (!ok) return false;

    if (best) applyBestVoucherNow();
    else removeVoucherNow();
    return true;
  }

  // 2) Voucher hiện tại invalid (tắt/hết lượt/hết hạn/chưa đạt min)
  const reason = getVoucherInvalidReason(vNow, subTotal.value);
  if (reason) {
    const ok = await openConfirm({
      type: "danger",
      message: `Voucher "${o.voucherCode || snap?.code || ""}" không hợp lệ: ${reason}.`,
      detail: best
        ? `Bạn có muốn đổi sang mã tốt nhất hiện tại (${best.code}) không?`
        : "Không có mã khác áp dụng được. Bạn có muốn bỏ voucher để tiếp tục thanh toán không?",
      suggest: best,
    });

    if (!ok) return false;

    if (best) applyBestVoucherNow();
    else removeVoucherNow();
    return true;
  }

  // 3) Voucher còn hợp lệ nhưng điều kiện đã thay đổi so với snapshot
  if (snap) {
    const changes = getVoucherChangedFields(snap, vNow);
    if (changes.length > 0) {
      const ok = await openConfirm({
        type: "danger",
        message: `Điều kiện voucher "${snap.code || o.voucherCode}" đã thay đổi.`,
        detail: changes.join(" • "),
        suggest: best,
      });

      if (!ok) return false;

      // nếu có best thì đổi, không thì cập nhật snapshot mới
      if (best) applyBestVoucherNow();
      else setVoucherSnapshot(o, vNow);

      return true;
    }
  } else {
    setVoucherSnapshot(o, vNow);
  }

  // 4) Voucher đang dùng OK, nhưng có mã tốt hơn hiện tại
  const currentDiscount = calcVoucherDiscount(subTotal.value, vNow);
  if (best && best.discount > currentDiscount) {
    const ok = await openConfirm({
      type: "info",
      message: `Hiện có mã tốt hơn (${best.code}) giảm thêm -${money(best.discount - currentDiscount)}.`,
      detail: `Bạn đang dùng "${vNow.ma_giam_gia}" giảm -${money(currentDiscount)}. Đổi sang "${best.code}" sẽ giảm -${money(best.discount)}.`,
      suggest: best,
    });

    if (ok) {
      applyBestVoucherNow();
    }
  }

  return true;
}

/** =======================
 * QR PAY (FE-only)
 * ======================= */
const qrImg = ref("");
const qrContent = ref("");
const qrNoteDraft = ref("");

function buildQrContent(o) {
  const amount = grandTotal.value;
  const ma = o?.maHoaDon || "";
  const note = (qrNoteDraft.value || "").trim();
  return `PAY|MA=${ma}|AMOUNT=${amount}|NOTE=${note || ma}`;
}
function makeQrImageUrl(content) {
  const data = encodeURIComponent(content);
  return `https://quickchart.io/qr?text=${data}&size=260`;
}
function openQrPay() {
  const o = activeOrder.value;
  if (!o) return;

  // ✅ đánh dấu đây là thanh toán QR
  o.paymentMethod = "QR";

  // ✅ để BE nhận biết (BE có check startsWith("QR-"))
  o.maGiaoDich = `QR-${o.maHoaDon}-${Date.now()}`;

  // ✅ để BE fallback nhận biết QR (nếu cần)
  o.ghiChuThanhToan = `QR thanh toán - ${o.maHoaDon}`;

  // phần bạn đang có
  qrNoteDraft.value = `HD:${o.maHoaDon}`;
  const content = buildQrContent(o);
  qrContent.value = content;
  qrImg.value = makeQrImageUrl(content);

  showQrPayModal.value = true;
}
function closeQrPay() {
  showQrPayModal.value = false;
}
async function markPaidAndCheckout() {
  const o = activeOrder.value;
  if (!o) return;

  o.paid = grandTotal.value;

  // ✅ gắn dấu hiệu QR cho BE
  o.maGiaoDich = `QR-${o.maHoaDon}-${Date.now()}`;
  o.ghiChuThanhToan = (
    qrNoteDraft.value || `QR thanh toán - ${o.maHoaDon}`
  ).trim();

  showQrPayModal.value = false;
  await confirmOrder();
}

/** =======================
 * CHECKOUT
 * ======================= */
function validateCheckout(o) {
  if (!o) return "Không có đơn hàng đang chọn";
  if (!Array.isArray(o.cart) || o.cart.length === 0) return "Giỏ hàng trống";

  // validate qty / tồn
  for (const it of o.cart) {
    const qty = Number(it.qty || 0);
    if (qty <= 0) return `Số lượng không hợp lệ: ${it.code}`;
    if (Number.isFinite(it.stockBase) && qty > Number(it.stockBase))
      return `Sản phẩm ${it.code} vượt tồn kho`;
  }

  const needShip = !!o.loaiDon;

  if (needShip) {
    const ten = String(o.tenNguoiNhanHang || "").trim();
    const sdt = String(o.soDienThoaiNhanHang || "").trim();

    if (!ten) return "Vui lòng nhập Tên người nhận";
    if (!sdt) return "Vui lòng nhập SĐT người nhận";

    const digits = sdt.replace(/[^\d]/g, "");
    if (digits.length < 9 || digits.length > 15)
      return "SĐT người nhận không hợp lệ";

    if (!String(o.tinhThanhNhanHang || "").trim())
      return "Vui lòng chọn Tỉnh/Thành";
    if (!String(o.phuongXaNhanHang || "").trim())
      return "Vui lòng chọn Phường/Xã";
    if (!String(o.diaChiNhanHangChiTiet || "").trim())
      return "Vui lòng nhập Địa chỉ chi tiết";

    const ship = Number(o.phiVanChuyen || 0);
    if (ship !== 30000) return "Phí ship toàn quốc phải là 30.000đ";
  }
  // validate tiền khách trả
  const paid = Number(o.paid || 0);
  if (paid < grandTotal.value) return "Khách thanh toán chưa đủ";

  return null;
}
function provinceNameByCode(code) {
  const c = String(code || "");
  return provinces.value.find((p) => String(p.code) === c)?.name || "";
}
function wardNameByCode(code) {
  const c = String(code || "");
  return wards.value.find((w) => String(w.code) === c)?.name || "";
}

function buildPosPayload(o) {
  const isShip = !!o.loaiDon;

  // ✅ xác định QR hay tiền mặt (tuỳ bạn set ở nơi bấm QR)
  const isQr = o.paymentMethod === "QR"; // bạn tự gắn flag này khi mở QR/confirm QR

  return {
    maHoaDon: o.maHoaDon,
    loaiDon: isShip,
    phiVanChuyen: isShip ? 30000 : 0,

    idKhachHang: o.customer?.id ?? null,
    tenKhachHang: o.customer?.name || "Khách lẻ",
    soDienThoai: (o.customerDraft?.phone || "").trim(),
    emailKhachHang: (o.customerDraft?.email || "").trim(),
    diaChiKhachHang: (o.diaChi || "").trim(),

    idPhieuGiamGia: o.pggId ?? null,
    giamThuCongPercent: Number(o.discountPercent || 0),

    paid: Number(o.paid || 0),
    ghiChu: (o.ghiChu || "POS checkout").trim(),

    // ✅ thêm cho BE nhận biết QR
    maGiaoDich: o.paymentMethod === "QR" ? o.maGiaoDich || null : null,
    ghiChuThanhToan:
      o.paymentMethod === "QR" ? o.ghiChuThanhToan || null : null,

    // ✅ nếu bạn có id phương thức thanh toán QR trong DB thì gửi luôn (ổn định nhất)
    // idPhuongThucThanhToan: isQr ? QR_METHOD_ID : CASH_METHOD_ID,

    tenNguoiNhanHang: isShip ? (o.tenNguoiNhanHang || "").trim() : null,
    soDienThoaiNhanHang: isShip ? (o.soDienThoaiNhanHang || "").trim() : null,

    tinhThanhNhanHang: isShip ? provinceNameByCode(o.tinhThanhNhanHang) : null,
    phuongXaNhanHang: isShip ? wardNameByCode(o.phuongXaNhanHang) : null,

    quanHuyenNhanHang: null,

    diaChiNhanHangChiTiet: isShip
      ? (o.diaChiNhanHangChiTiet || "").trim()
      : null,

    items: o.cart.map((it) => ({
      idSanPhamChiTiet: Number(it.idSpct),
      soLuong: clampInt(it.qty, 1, 999999),
    })),
  };
}

async function resetOrderAfterPaid(o) {
  const idx = orders.value.findIndex((x) => x.id === o.id);
  if (idx !== -1) {
    orders.value.splice(idx, 1);
  }

  if (activeId.value === o.id) {
    activeId.value = orders.value[0]?.id ?? null;
  }

  saveDraftsNow();
}

async function confirmOrder() {
  const o = activeOrder.value;
  confirmHint.value = "";

  const err = validateCheckout(o);
  if (err)
    return toastShow(err, err.includes("chưa đủ") ? "warning" : "danger");

  const ok = await runVoucherPrecheckFlow();
  if (!ok) return;

  if (!o?.dbId) return toastShow("Hóa đơn chưa được tạo trong DB", "danger");

  const payload = buildPosPayload(o);
  submitting.value = true;
  try {
    await http.post(`/api/hoa-don/draft/${o.dbId}/checkout`, payload);
    toastShow(`Thanh toán thành công: ${o.maHoaDon}`, "success");

    await resetOrderAfterPaid(o);
  } catch (e) {
    console.error(e);
    const msg =
      e?.response?.data?.message ||
      e?.response?.data?.error ||
      "Thanh toán thất bại";
    toastShow(msg, "danger");
  } finally {
    submitting.value = false;
  }
}

/** =======================
 * KEYBOARD
 * ======================= */
function onKeydown(e) {
  if (e.key === "Escape") {
    if (showPreCheckoutModal.value) return resolvePreCheckout(false);
    if (anyModalOpen.value) closeAnyModal();
  }
}

/** =======================
 * LIFECYCLE
 * ======================= */
onMounted(async () => {
  loadDrafts();
  scheduleMidnightReset();
  initAddressDataOffline();
  reloadCustomers();
  loadVouchers();

  window.addEventListener("beforeunload", saveDraftsNow);
  window.addEventListener("keydown", onKeydown);

  document.addEventListener("visibilitychange", () => {
    if (document.visibilityState === "hidden") saveDraftsNow();
  });
});

onBeforeUnmount(() => {
  window.removeEventListener("beforeunload", saveDraftsNow);
  window.removeEventListener("keydown", onKeydown);

  if (midnightTimer) clearTimeout(midnightTimer);

  saveDraftsNow();
});
</script>

<style scoped>
.tab-x {
  opacity: 0.85;
  cursor: pointer;
}
.tab-x:hover {
  opacity: 1;
}

.voucher-card {
  border: 1px solid #bfead8;
  background: #e9fbf3;
  border-radius: 10px;
  padding: 12px;
}

/* z-index */
:global(.modal-backdrop) {
  z-index: 1050;
}
:global(.modal) {
  z-index: 1055;
}

/* Tránh click xuyên */
:global(.modal) {
  pointer-events: none;
}
:global(.modal .modal-dialog) {
  pointer-events: auto;
}
.product-status-group {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
  min-height: 38px;
}

.product-status-group .form-check {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 0;
}

.price-range-wrap {
  position: relative;
  height: 24px;
  display: flex;
  align-items: center;
}

.range-input {
  width: 100%;
  appearance: none;
  background: transparent;
  pointer-events: auto;
}

.range-input::-webkit-slider-runnable-track {
  height: 4px;
  border-radius: 999px;
  background: #198754;
}

.range-input::-webkit-slider-thumb {
  appearance: none;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #d1fae5;
  border: 2px solid #198754;
  margin-top: -6px;
  cursor: pointer;
}

.range-input::-moz-range-track {
  height: 4px;
  border-radius: 999px;
  background: #198754;
}

.range-input::-moz-range-thumb {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #d1fae5;
  border: 2px solid #198754;
  cursor: pointer;
}
.customer-modal-dialog {
  max-width: 1150px;
}

.customer-table-wrap {
  border: 1px solid #dee2e6;
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
}

.customer-table {
  width: 100%;
  table-layout: fixed;
}

.customer-table thead th {
  font-size: 14px;
  font-weight: 700;
  padding: 12px 10px;
  vertical-align: middle;
  white-space: nowrap;
}

.customer-table tbody td {
  font-size: 14px;
  padding: 12px 10px;
  vertical-align: middle;
}

.customer-table tbody tr:hover {
  background: #f8fafc;
}

.customer-address-cell {
  line-height: 1.45;
  white-space: normal;
  word-break: break-word;
}

.customer-pick-btn {
  min-width: 64px;
}
.order-tab-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
}

.order-tab-label {
  white-space: nowrap;
}

.order-count-badge {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: #dc3545;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  flex-shrink: 0;
}

.tab-x {
  opacity: 0.85;
  cursor: pointer;
  margin-left: 2px;
  flex-shrink: 0;
}

.tab-x:hover {
  opacity: 1;
}
.custom-toast {
  min-width: 320px;
  max-width: 420px;
  background: #fff;
  border-radius: 12px;
  border-left: 5px solid #dc3545;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 14px 14px 16px;
  color: #1f2937;
}

.custom-toast-content {
  flex: 1;
  min-width: 0;
}

.custom-toast-title {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 4px;
  color: #111827;
}

.custom-toast-message {
  font-size: 14px;
  line-height: 1.45;
  color: #4b5563;
  word-break: break-word;
}

.custom-toast-close {
  border: 0;
  background: transparent;
  color: #9ca3af;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
  padding: 0;
  margin-top: -2px;
}

.custom-toast-close:hover {
  color: #374151;
}

.custom-toast-success {
  border-left-color: #22c55e;
}

.custom-toast-info {
  border-left-color: #3b82f6;
}

.custom-toast-warning {
  border-left-color: #f59e0b;
}

.custom-toast-danger {
  border-left-color: #ef4444;
}
.shipping-brand-box {
  border: 1px dashed #d1d5db;
  border-radius: 12px;
  padding: 14px;
  background: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
}

.shipping-brand-logo {
  max-height: 52px;
  width: auto;
  object-fit: contain;
}
</style>
