<template>
  <div class="container-fluid py-3">
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

              <span v-if="getOrderItemCount(o) > 0" class="order-count-badge">
                {{ getOrderItemCount(o) }}
              </span>

              <span class="tab-x" title="Đóng" @click.stop="closeOrder(o.id)"
                >×</span
              >
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
                @click="openScanQrModal"
              >
                <i class="bi bi-qr-code-scan me-1"></i> Quét QR
              </button>
            </div>
          </div>

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

                      <div
                        v-if="it.priceChangedLocked"
                        class="small text-danger fw-semibold mt-1"
                      >
                        Giá sản phẩm đã thay đổi:
                        <span class="text-decoration-line-through">{{
                          money(it.price)
                        }}</span>
                        →
                        <span>{{ money(it.newPrice) }}</span>
                      </div>

                      <div
                        v-if="it.lineStatus === 'missing'"
                        class="small text-danger fw-semibold mt-1"
                      >
                        Biến thể không còn hợp lệ, vui lòng xóa khỏi giỏ.
                      </div>
                    </td>

                    <td class="text-end fw-semibold">{{ money(it.price) }}</td>

                    <td class="text-center">
                      <div class="btn-group" role="group">
                        <button
                          class="btn btn-outline-secondary btn-sm"
                          @click="decQty(idx)"
                          :disabled="
                            it.priceChangedLocked || it.lineStatus === 'missing'
                          "
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
                          :disabled="
                            it.priceChangedLocked || it.lineStatus === 'missing'
                          "
                        />

                        <button
                          class="btn btn-outline-secondary btn-sm"
                          @click="incQty(idx)"
                          :disabled="
                            it.priceChangedLocked || it.lineStatus === 'missing'
                          "
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

          <div class="row g-3">
            <div class="col-12 col-lg-6">
              <div class="card h-100">
                <div
                  class="card-header bg-white d-flex align-items-center justify-content-between flex-wrap gap-2"
                >
                  <div>
                    <div class="fw-bold">
                      {{
                        activeOrder.loaiDon
                          ? "Thông tin giao hàng"
                          : "Thông tin khách hàng"
                      }}
                    </div>
                    <div class="text-muted small">
                      Mã HĐ:
                      <span class="fw-semibold font-monospace">
                        {{ activeOrder.maHoaDon }}
                      </span>
                    </div>
                  </div>

                  <div class="d-flex align-items-center gap-2 flex-wrap">
                    <button
                      class="btn btn-outline-primary btn-sm"
                      type="button"
                      @click="chooseWalkInCustomer"
                      :disabled="!activeOrder?.customer?.id"
                    >
                      Khách vãng lai
                    </button>

                    <button
                      class="btn btn-outline-primary btn-sm"
                      type="button"
                      @click="openCustomerModal"
                    >
                      Chọn khách hàng
                    </button>

                    <button
                      v-if="activeOrder.loaiDon"
                      class="btn btn-outline-primary btn-sm"
                      type="button"
                      :disabled="!activeOrder.customer?.id"
                      @click="openAddressModal"
                    >
                      Chọn địa chỉ
                    </button>
                  </div>
                </div>

                <div class="card-body">
                  <template v-if="!activeOrder.loaiDon">
                    <div class="customer-summary-box">
                      <div class="summary-line">
                        <span class="summary-label">Tên khách hàng</span>
                        <span class="summary-value fw-bold">
                          {{ activeOrder.customer?.name || "Khách lẻ" }}
                        </span>
                      </div>

                      <div v-if="activeOrder.customer?.id" class="summary-line">
                        <span class="summary-label">Số điện thoại</span>
                        <span class="summary-value">
                          {{ activeOrder.customerDraft.phone || "—" }}
                        </span>
                      </div>

                      <div v-if="activeOrder.customer?.id" class="summary-line">
                        <span class="summary-label">Email</span>
                        <span class="summary-value">
                          {{ activeOrder.customerDraft.email || "—" }}
                        </span>
                      </div>

                      <div v-if="activeOrder.customer?.id" class="summary-line">
                        <span class="summary-label">Địa chỉ</span>
                        <span class="summary-value">
                          {{ activeOrder.diaChi || "Chưa có địa chỉ" }}
                        </span>
                      </div>
                    </div>
                  </template>

                  <template v-else>
                    <div class="customer-summary-box mb-3">
                      <div class="summary-line">
                        <span class="summary-label">Tên khách hàng</span>
                        <span class="summary-value fw-bold">
                          {{ activeOrder.customer?.name || "Khách lẻ" }}
                        </span>
                      </div>

                      <div class="summary-line">
                        <span class="summary-label">Số điện thoại</span>
                        <span class="summary-value">
                          {{
                            activeOrder.customerDraft.phone ||
                            activeOrder.customer?.phone ||
                            "—"
                          }}
                        </span>
                      </div>

                      <div class="summary-line">
                        <span class="summary-label">Địa chỉ khách hàng</span>
                        <span class="summary-value">
                          {{
                            selectedAddressDisplay ||
                            activeOrder.diaChi ||
                            "Chưa chọn địa chỉ"
                          }}
                        </span>
                      </div>
                    </div>

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

                        <div class="col-12">
                          <input
                            class="form-control"
                            placeholder="Email người nhận"
                            v-model.trim="activeOrder.emailNguoiNhanHang"
                          />
                        </div>

                        <div class="col-12 col-md-6">
                          <label class="form-label mb-1">Tỉnh/Thành</label>
                          <Multiselect
                            :model-value="
                              activeOrder.ghnProvinceId
                                ? String(activeOrder.ghnProvinceId)
                                : null
                            "
                            @update:model-value="onProvinceChange"
                            :options="provinceOptions"
                            value-prop="value"
                            label="label"
                            track-by="label"
                            placeholder="-- Chọn tỉnh/thành --"
                            :searchable="true"
                            :can-clear="true"
                            :can-deselect="true"
                            :disabled="provincesLoading"
                            no-options-text="Không có dữ liệu"
                            no-results-text="Không tìm thấy"
                          />
                        </div>

                        <div class="col-12 col-md-6">
                          <label class="form-label mb-1">Phường/Xã</label>
                          <Multiselect
                            :model-value="
                              activeOrder.ghnWardCode
                                ? String(activeOrder.ghnWardCode)
                                : null
                            "
                            @update:model-value="onWardChange"
                            :options="wardOptions"
                            value-prop="value"
                            label="label"
                            track-by="label"
                            placeholder="-- Chọn phường/xã --"
                            :searchable="true"
                            :can-clear="true"
                            :can-deselect="true"
                            :disabled="
                              !activeOrder.ghnProvinceId || wardsLoading
                            "
                            no-options-text="Không có dữ liệu"
                            no-results-text="Không tìm thấy"
                          />
                        </div>

                        <div class="col-12">
                          <label class="form-label mb-1"
                            >Địa chỉ chi tiết</label
                          >
                          <input
                            class="form-control"
                            placeholder="Số nhà, tên đường..."
                            v-model.trim="activeOrder.diaChiNhanHangChiTiet"
                            @blur="refreshShipFee"
                          />
                        </div>
                      </div>
                    </div>
                  </template>
                </div>
              </div>
            </div>

            <div class="col-12 col-lg-6">
              <div class="card h-100">
                <div
                  class="card-header bg-white d-flex align-items-center justify-content-between"
                >
                  <div class="fw-bold">Thanh toán</div>
                  <div class="small text-muted">Chỉ tại cửa hàng</div>
                </div>

                <div class="card-body">
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
                        {{ activeOrder.loaiDon ? "Giao hàng" : "Bán tại quầy" }}
                      </label>
                    </div>
                  </div>

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
                          @click="
                            reloadActiveOrderVouchers({ showModal: true })
                          "
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
                                <span class="badge text-bg-dark">
                                  {{ bestVoucherEntryUI.v.ma_giam_gia }}
                                </span>

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
                                <span class="ms-2 fw-bold text-danger">
                                  - {{ money(bestVoucherEntryUI.discount) }}
                                </span>
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
                            <span class="me-2">Hết hạn:</span>
                            <b>{{
                              formatDateVN(bestVoucherEntryUI.v.ngay_ket_thuc)
                            }}</b>
                          </div>
                        </div>

                        <div v-else class="text-muted small py-2">
                          Không có mã giảm giá phù hợp với đơn hiện tại.
                        </div>
                      </template>

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
                                  <span class="ms-2 fw-bold text-danger">
                                    - {{ money(e.discount) }}
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
                              để dùng <b>{{ e.v.ma_giam_gia }}</b>
                              (giảm khoảng
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
                      class="list-group-item d-flex justify-content-between align-items-center"
                    >
                      <div class="d-flex align-items-center gap-2">
                        <span class="text-muted">Phí vận chuyển</span>
                        <img :src="ghnLogo" alt="GHN" class="ship-fee-logo" />
                      </div>

                      <div class="d-flex align-items-center gap-2">
                        <span class="fw-semibold">{{ money(shipMoney) }}</span>
                      </div>
                    </li>

                    <li class="list-group-item d-flex justify-content-between">
                      <span class="text-muted">Giảm giá</span>
                      <span class="fw-semibold text-danger">
                        - {{ money(discountMoney) }}
                      </span>
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

                  <div class="d-grid gap-2">
                    <button
  class="btn btn-success w-100"
  :disabled="activeOrder.cart.length === 0 || submitting"
  @click="confirmOrderCash"
>
  {{ submitting ? "Đang xử lý..." : "Thanh toán (tiền mặt)" }}
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
        </div>
      </div>
    </div>

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

    <teleport to="body">
      <div
        v-if="backdropOpen"
        class="modal-backdrop fade show"
        @click="closeBackdropModal"
        style="z-index: 1050"
      ></div>

      <div
        v-if="showScanQrModal"
        class="qr-scan-modal"
        role="dialog"
        aria-modal="true"
      >
        <div class="qr-scan-dialog">
          <div class="qr-scan-header">
            <h5 class="m-0 fw-bold">Quét QR sản phẩm</h5>
            <button
              type="button"
              class="btn-close"
              @click="closeScanQrModal"
            ></button>
          </div>

          <div class="qr-scan-body">
            <div class="text-muted small mb-2">
              Đưa mã QR của biến thể vào khung để thêm vào giỏ hàng.
            </div>

            <div id="product-qr-reader" class="qr-reader-box"></div>

            <div v-if="scanQrError" class="alert alert-warning mt-3 mb-0">
              {{ scanQrError }}
            </div>
          </div>
        </div>
      </div>

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
                      <Multiselect
                        v-model="productFilters.color"
                        :options="productColorOptions"
                        placeholder="-- Chọn màu sắc --"
                        :searchable="true"
                        :can-clear="true"
                        :can-deselect="true"
                        no-options-text="Không có màu sắc"
                        no-results-text="Không tìm thấy màu sắc"
                      />
                    </div>

                    <div class="col-12 col-md-6 col-lg-3">
                      <label class="form-label mb-1">Size</label>
                      <Multiselect
                        v-model="productFilters.size"
                        :options="productSizeOptions"
                        placeholder="-- Chọn size --"
                        :searchable="true"
                        :can-clear="true"
                        :can-deselect="true"
                        no-options-text="Không có size"
                        no-results-text="Không tìm thấy size"
                      />
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
                          >Hiển thị: <b>{{ pagedProducts.length }}</b></span
                        >
                        <span
                          >Tổng sau lọc: <b>{{ productTotal }}</b></span
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
                    <tr v-for="(p, i) in pagedProducts" :key="p.idSpct">
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
                          :disabled="!p.active || p.stock <= 0"
                          @click="chooseProduct(p)"
                        >
                          Chọn
                        </button>
                      </td>
                    </tr>

                    <tr v-if="pagedProducts.length === 0 && !productLoading">
                      <td colspan="9" class="text-center text-muted py-3">
                        Không có dữ liệu
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <div class="d-flex align-items-center mt-2">
                <div class="text-muted small" style="min-width: 220px">
                  Hiển thị {{ pagedProducts.length }} / tổng
                  {{ productTotal }} bản ghi
                </div>

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

                <button
                  class="btn btn-outline-dark"
                  type="button"
                  @click="chooseWalkInCustomer"
                >
                  Khách vãng lai
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

              <div class="d-flex align-items-center mt-2">
                <div class="text-muted small" style="min-width: 220px">
                  Hiển thị {{ pagedCustomers.length }} / tổng
                  {{ customerTotal }} bản ghi
                </div>

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

      <div
        v-if="showAddressModal"
        class="modal fade show"
        tabindex="-1"
        role="dialog"
        aria-modal="true"
        style="display: block; z-index: 1056"
      >
        <div class="modal-dialog modal-lg modal-dialog-scrollable">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title fw-bold">Chọn địa chỉ của khách</h5>
              <button
                type="button"
                class="btn-close"
                @click="closeAddressModal"
              ></button>
            </div>

            <div class="modal-body">
              <div class="address-new-box mb-3">
                <div
                  class="d-flex align-items-center justify-content-between gap-2 flex-wrap mb-2"
                >
                  <div>
                    <div class="fw-bold text-primary">Thêm địa chỉ mới</div>
                    <div class="small text-muted">
                      Thêm địa chỉ cho khách đang chọn
                    </div>
                  </div>
                  <button
                    class="btn btn-success btn-sm"
                    type="button"
                    @click="saveNewAddress"
                  >
                    Thêm địa chỉ
                  </button>
                </div>

                <div class="row g-2">
                  <div class="col-12 col-md-6">
                    <input
                      class="form-control"
                      placeholder="Tên người nhận"
                      v-model.trim="addressDraft.receiverName"
                    />
                  </div>

                  <div class="col-12 col-md-6">
                    <input
                      class="form-control"
                      placeholder="Số điện thoại"
                      v-model.trim="addressDraft.phone"
                    />
                  </div>

                  <div class="col-12 col-md-6">
                    <label class="form-label mb-1">Tỉnh/Thành phố</label>
                    <v-select
                      :options="provinces"
                      label="provinceName"
                      :reduce="(p) => p.provinceId"
                      v-model="addressDraft.provinceId"
                      :clearable="true"
                      :searchable="true"
                      :disabled="provincesLoading"
                      placeholder="Chọn tỉnh/thành"
                      @update:modelValue="onAddressProvinceChange"
                    />

                    <v-select
                      :options="addressDraftWards"
                      label="wardName"
                      :reduce="(w) => w.wardCode"
                      v-model="addressDraft.wardCode"
                      :clearable="true"
                      :searchable="true"
                      :disabled="!addressDraft.provinceId || wardsLoading"
                      placeholder="Chọn phường/xã"
                    />
                  </div>

                  <div class="col-12">
                    <label class="form-label mb-1">Địa chỉ chi tiết</label>
                    <input
                      class="form-control"
                      placeholder="Số nhà, tên đường..."
                      v-model.trim="addressDraft.detail"
                    />
                  </div>

                  <div class="col-12">
                    <div class="form-check">
                      <input
                        class="form-check-input"
                        type="checkbox"
                        id="addr-default"
                        v-model="addressDraft.makeDefault"
                      />
                      <label class="form-check-label" for="addr-default">
                        Đặt làm mặc định
                      </label>
                    </div>
                  </div>
                </div>
              </div>

              <div
                v-if="activeOrderAddressBook.length === 0"
                class="small text-muted"
              >
                Chưa có địa chỉ nào. Hãy thêm mới địa chỉ.
              </div>

              <div v-else class="d-flex flex-column gap-2">
                <div
                  v-for="addr in activeOrderAddressBook"
                  :key="addr.id"
                  class="address-card"
                  :class="{
                    'address-card-active':
                      activeOrder.selectedAddressId === addr.id,
                  }"
                >
                  <div class="d-flex justify-content-between gap-2 flex-wrap">
                    <div>
                      <div class="fw-semibold">
                        {{ addr.receiverName }}
                        <span class="text-muted"
                          >· {{ addr.phone || "—" }}</span
                        >
                        <span
                          v-if="addr.isDefault"
                          class="badge text-bg-primary ms-2"
                        >
                          Mặc định
                        </span>
                      </div>
                      <div class="small mt-1">{{ addr.fullAddress }}</div>
                    </div>

                    <div class="d-flex gap-2">
                      <button
                        class="btn btn-outline-secondary btn-sm"
                        type="button"
                        @click="setDefaultAddress(addr.id)"
                        :disabled="addr.isDefault"
                      >
                        Đặt mặc định
                      </button>
                      <button
                        class="btn btn-dark btn-sm"
                        type="button"
                        @click="pickAddress(addr.id)"
                      >
                        Chọn
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div
        v-if="showQrPayModal"
        class="modal fade show"
        tabindex="-1"
        role="dialog"
        aria-modal="true"
        style="display: block; z-index: 1065"
      >
        <div class="modal-dialog modal-dialog-centered qr-pay-dialog">
          <div class="modal-content qr-pay-modal">
            <div class="modal-header qr-pay-header">
              <div>
                <h5 class="modal-title fw-bold mb-1">Thanh toán bằng QR</h5>
                <div class="qr-pay-subtitle">
                  Quét mã hoặc chuyển sang trang thanh toán
                </div>
              </div>

              <button
                type="button"
                class="btn-close"
                @click="closeQrPay"
              ></button>
            </div>

            <div class="modal-body qr-pay-body">
              <div class="qr-pay-top">
                <div class="qr-pay-meta-card">
                  <div class="qr-pay-meta-label">Mã hóa đơn</div>
                  <div class="qr-pay-meta-value font-monospace">
                    {{ activeOrder?.maHoaDon }}
                  </div>
                </div>

                <div class="qr-pay-amount-card">
                  <div class="qr-pay-meta-label">Số tiền thanh toán</div>
                  <div class="qr-pay-amount">{{ money(grandTotal) }}</div>
                </div>
              </div>

              <div class="qr-stage">
                <div class="qr-stage-glow"></div>

                <div class="qr-frame">
                  <img :src="techcombankQr" alt="QR Pay" class="qr-image" />
                </div>
              </div>

              <div class="qr-pay-info-card">
                <div class="qr-pay-info-row qr-pay-info-row-top">
                  <span class="qr-pay-info-label">Mã đơn</span>
                  <span class="qr-pay-info-value font-monospace qr-pay-code">
                    {{ qrContent }}
                  </span>
                </div>
              </div>

              <div class="mt-3">
                <label class="form-label qr-pay-input-label">Ghi chú</label>
                <input
                  class="form-control qr-pay-input"
                  v-model="qrNoteDraft"
                  placeholder="VD: VNPAY sandbox - HDxxxx"
                />
              </div>
            </div>

            <div class="modal-footer qr-pay-footer">
              <button
                class="btn qr-btn qr-btn-success"
                type="button"
                @click="markPaidAndCheckout"
                :disabled="submitting"
              >
                {{ submitting ? "Đang xử lý..." : "Xác nhận thanh toán" }}
              </button>

              <button
                class="btn qr-btn qr-btn-secondary"
                type="button"
                @click="closeQrPay"
              >
                Đóng
              </button>
            </div>
          </div>
        </div>
      </div>

      <div
        v-if="showPaymentConfirmModal"
        class="modal-backdrop fade show payment-confirm-backdrop"
        style="z-index: 1067"
      ></div>

      <div
        v-if="showPaymentConfirmModal"
        class="modal fade show"
        tabindex="-1"
        role="dialog"
        aria-modal="true"
        style="display: block; z-index: 1068"
      >
        <div class="modal-dialog modal-dialog-centered payment-confirm-dialog">
          <div class="modal-content payment-confirm-modal">
            <div class="modal-header">
              <h5 class="modal-title fw-bold">Xác nhận thanh toán</h5>
              <button
                type="button"
                class="btn-close"
                @click="resolvePaymentConfirm(false)"
              ></button>
            </div>

            <div class="modal-body">
              <div class="fw-semibold payment-confirm-text">
                {{ paymentConfirmText }}
              </div>
            </div>

            <div class="modal-footer payment-confirm-footer">
              <button
                class="btn btn-outline-secondary payment-confirm-cancel-btn"
                type="button"
                @click="resolvePaymentConfirm(false)"
              >
                Hủy
              </button>

              <button
                class="btn btn-success payment-confirm-ok-btn"
                type="button"
                @click="resolvePaymentConfirm(true)"
              >
                Xác nhận
              </button>
            </div>
          </div>
        </div>
      </div>

      <div
        v-if="showPreCheckoutModal"
        class="modal fade show"
        tabindex="-1"
        role="dialog"
        aria-modal="true"
        style="display: block; z-index: 1060"
      >
        <div class="modal-dialog modal-lg modal-dialog-centered">
          <div class="modal-content voucher-notify-modal">
            <div class="modal-header voucher-notify-header">
              <div>
                <h5 class="modal-title fw-bold mb-1">
                  {{ preCheckoutUi.title }}
                </h5>
                <div class="small text-muted" v-if="preCheckoutUi.subtitle">
                  {{ preCheckoutUi.subtitle }}
                </div>
              </div>

              <button
                type="button"
                class="btn-close"
                @click="resolvePreCheckout(preCheckoutUi.mode === 'ack')"
              ></button>
            </div>

            <div class="modal-body">
              <div
                class="voucher-notify-banner mb-3"
                :class="{
                  'voucher-notify-banner-danger':
                    preCheckoutUi.type === 'danger',
                  'voucher-notify-banner-info': preCheckoutUi.type === 'info',
                  'voucher-notify-banner-success':
                    preCheckoutUi.type === 'success',
                }"
              >
                {{ preCheckoutUi.message }}
              </div>

              <div v-if="preCheckoutUi.detail" class="small text-muted mb-3">
                {{ preCheckoutUi.detail }}
              </div>

              <div
                v-if="preCheckoutUi.rows?.length"
                class="voucher-notify-table"
              >
                <div
                  v-for="row in preCheckoutUi.rows"
                  :key="row.label"
                  class="voucher-notify-row"
                >
                  <div class="voucher-notify-label">{{ row.label }}</div>
                  <div
                    class="voucher-notify-value"
                    :class="row.valueClass || ''"
                  >
                    {{ row.value }}
                  </div>
                </div>
              </div>
            </div>

            <div class="modal-footer voucher-notify-footer">
              <template v-if="preCheckoutUi.mode === 'confirm'">
                <button
                  class="btn btn-outline-secondary voucher-notify-cancel-btn"
                  type="button"
                  @click="resolvePreCheckout(false)"
                >
                  {{ preCheckoutUi.cancelText || "Không" }}
                </button>

                <button
                  class="btn btn-danger voucher-notify-ok-btn"
                  type="button"
                  @click="resolvePreCheckout(true)"
                >
                  {{ preCheckoutUi.confirmText || "Xác nhận" }}
                </button>
              </template>

              <template v-else>
                <button
                  class="btn btn-danger voucher-notify-ok-btn"
                  type="button"
                  @click="resolvePreCheckout(true)"
                >
                  {{ preCheckoutUi.confirmText || "Đã hiểu" }}
                </button>
              </template>
            </div>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup>
import { Html5Qrcode } from "html5-qrcode";
import { getDetailByCode } from "@/services/sanPhamChiTietApi";
import { nextTick } from "vue";

import {
  computed,
  ref,
  reactive,
  watch,
  onMounted,
  onBeforeUnmount,
} from "vue";
import { useRouter } from "vue-router";
import http from "@/services/http";
import ghnLogo from "@/assets/ghn-logo.png.webp";
import vnUnits from "@/assets/vn_units.json";
import { onTabSync, TAB_SYNC_EVENTS } from "@/utils/tabSync";
import {
  getAllDetails,
  decreaseStock,
  increaseStock,
} from "@/services/sanPhamChiTietApi";
import { listKhachHang } from "@/services/khachHangApi";
import { resolveMediaUrl } from "@/utils/media";
import Multiselect from "@vueform/multiselect";
import "@vueform/multiselect/themes/default.css";
import techcombankQr from "@/assets/techcombank-qr.png";

const PUBLIC_WEB_ORIGIN =
  import.meta.env.VITE_PUBLIC_WEB_ORIGIN || window.location.origin;
const qrRequestCode = ref("");
let posQrPollTimer = null;
const qrPaymentUrl = ref("");
function stopPosQrPolling() {
  if (posQrPollTimer) {
    clearInterval(posQrPollTimer);
    clearTimeout(posQrPollTimer);
    posQrPollTimer = null;
  }
}
const qrContent = ref("");
const qrNoteDraft = ref("");
const MAX_ORDERS = 10;
const STORAGE_KEY = "sales_store_only_v7_merged";
const router = useRouter();
const showScanQrModal = ref(false);
const scanQrError = ref("");
let productQrScanner = null;

function getDateKeyLocal(d = new Date()) {
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd}`;
}

const placeholderImg = "https://via.placeholder.com/56x56.png?text=IMG";
function buildImgUrl(path) {
  return resolveMediaUrl(path) || placeholderImg;
}

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
  toast.title =
    title ||
    (type === "success"
      ? "Thành công"
      : type === "warning"
        ? "Cảnh báo"
        : type === "info"
          ? "Thông báo"
          : "Thất bại");

  clearTimeout(toastShow._t);
  toastShow._t = setTimeout(() => (toast.show = false), 2600);
}
const toastInfo = (m) => toastShow(m, "info");

const orders = ref([]);
const activeId = ref(null);
const orderSeq = ref(1);
const submitting = ref(false);
const activeOrderVouchers = ref([]);

const activeOrder = computed(
  () => orders.value.find((o) => o.id === activeId.value) || null,
);

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

async function openScanQrModal() {
  showScanQrModal.value = true;
  scanQrError.value = "";
  await nextTick();
  await startProductQr();
}

async function closeScanQrModal() {
  await stopProductQr();
  showScanQrModal.value = false;
}

async function startProductQr() {
  try {
    if (!productQrScanner) {
      productQrScanner = new Html5Qrcode("product-qr-reader");
    }

    const cameras = await Html5Qrcode.getCameras();
    if (!cameras?.length) {
      scanQrError.value = "Không tìm thấy camera.";
      return;
    }

    await productQrScanner.start(
      { deviceId: { exact: cameras[0].id } },
      { fps: 10, qrbox: { width: 250, height: 250 } },
      async (decodedText) => {
        await onProductQrDecoded(decodedText);
      },
    );
  } catch (e) {
    console.error(e);
    scanQrError.value = "Không mở được camera hoặc bị chặn quyền.";
  }
}

async function stopProductQr() {
  try {
    if (productQrScanner && (await productQrScanner.getState()) === 2) {
      await productQrScanner.stop();
      await productQrScanner.clear();
    }
  } catch (_) {}
}

async function onProductQrDecoded(decodedText) {
  try {
    const code = String(decodedText || "").trim();
    if (!code) return;

    await stopProductQr();

    await fetchAllProducts();

    let found = allProducts.value.find(
      (x) => String(x.code || "").toLowerCase() === code.toLowerCase(),
    );

    if (!found) {
      const res = await getDetailByCode(code);
      found = mapSpct(res?.data ?? res);
    }

    if (!isProductSelectable(found)) {
      scanQrError.value = Number(found?.stock || 0) <= 0
        ? "Sản phẩm đã hết hàng nên không thể thêm vào đơn."
        : "Biến thể đang tắt hoạt động nên không thể thêm vào đơn.";
      await startProductQr();
      return;
    }

    await chooseProduct(found);
    showScanQrModal.value = false;
  } catch (e) {
    console.error(e);
    scanQrError.value = "Không tìm thấy sản phẩm theo mã QR.";
    await startProductQr();
  }
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

function normalizeCartItem(it) {
  return {
    ...it,
    stockBase: Number(it?.stockBase || it?.stock || 0),
    stock: Number(it?.stock || it?.stockBase || 0),
    price: Number(it?.price || 0),
    qty: Number(it?.qty || 0),
    priceChangedLocked: !!it?.priceChangedLocked,
    newPrice: it?.newPrice != null ? Number(it.newPrice) : null,
    lineStatus: it?.lineStatus || "ok",
  };
}

function normalizeOrder(o) {
  const ma = o?.maHoaDon || genMaHoaDon();
  return {
    id: o?.id ?? Date.now() + Math.random(),
    dbId: o?.dbId ?? null,
    maHoaDon: ma,
    label: o?.label || `Hóa Đơn - ${ma}`,
    cart: Array.isArray(o?.cart) ? o.cart.map(normalizeCartItem) : [],

    customer: o?.customer ?? null,
    customerDraft: o?.customerDraft ?? { phone: "", email: "" },
    diaChi: o?.diaChi || "",

    addressBook: Array.isArray(o?.addressBook) ? o.addressBook : [],
    selectedAddressId: o?.selectedAddressId ?? null,

    ghiChu: o?.ghiChu || "POS checkout",

    loaiDon: !!o?.loaiDon,
    phiVanChuyen: Number(o?.phiVanChuyen || 0),

    tenNguoiNhanHang: o?.tenNguoiNhanHang || "",
    soDienThoaiNhanHang: o?.soDienThoaiNhanHang || "",
    emailNguoiNhanHang: o?.emailNguoiNhanHang || o?.customerDraft?.email || "",
    tinhThanhNhanHang: o?.tinhThanhNhanHang || "",
    quanHuyenNhanHang: o?.quanHuyenNhanHang || "",
    phuongXaNhanHang: o?.phuongXaNhanHang || "",
    diaChiNhanHangChiTiet: o?.diaChiNhanHangChiTiet || "",

    ghnProvinceId: o?.ghnProvinceId ?? null,
    ghnDistrictId: o?.ghnDistrictId ?? null,
    ghnWardCode: o?.ghnWardCode ?? "",

    voucherCode: String(o?.voucherCode || ""),
    pggId: o?.pggId ?? null,
    voucherMode: o?.voucherMode ?? "best",
    voucherTab: o?.voucherTab ?? "best",
    voucherSnapshot: o?.voucherSnapshot ?? null,
    voucherSuggestionDismissedKey: o?.voucherSuggestionDismissedKey ?? "",

    discountPercent: Number(o?.discountPercent || 0),
    paid: Number(o?.paid || 0),

    paymentMethod: o?.paymentMethod || null,
    maGiaoDich: o?.maGiaoDich || null,
    ghiChuThanhToan: o?.ghiChuThanhToan || null,
  };
}

async function createOrder() {
  if (orders.value.length >= MAX_ORDERS) {
    return toastShow(`Chỉ tối đa ${MAX_ORDERS} đơn`, "warning");
  }

  const localId = Date.now() + Math.random();
  const maHoaDon = genUniqueMaHoaDon();

  try {
    const res = await http.post("/api/hoa-don/taohoadon", { maHoaDon });
    const data = res?.data || {};

    orders.value.push(
      normalizeOrder({
        id: localId,
        dbId: data.id,
        maHoaDon: data.maHoaDon || maHoaDon,
        label: `Hóa Đơn - ${data.maHoaDon || maHoaDon}`,
      }),
    );

    activeId.value = localId;
    orderSeq.value++;

    const createdOrder = orders.value.find((x) => x.id === localId);
    if (createdOrder) scheduleSyncDraft(createdOrder);
  } catch (e) {
    console.error(e);
    toastShow("Không tạo được hóa đơn", "danger");
  }
}

async function closeOrder(id) {
  const idx = orders.value.findIndex((o) => o.id === id);
  if (idx === -1) return;

  const o = orders.value[idx];

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
    }
  }

  orders.value.splice(idx, 1);
  if (activeId.value === id) activeId.value = orders.value[0]?.id ?? null;
  saveDraftsNow();
}

function saveDraftsNow() {
  try {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        dateKey: getDateKeyLocal(),
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
      clearDraftsFE();
      return;
    }

    orders.value = data.orders.map((o) => normalizeOrder(o));
    activeId.value = data.activeId ?? orders.value[0]?.id ?? null;
    orderSeq.value = Number(data.orderSeq) || orders.value.length + 1;
  } catch {}
}

let _saveT = null;
function scheduleSave() {
  clearTimeout(_saveT);
  _saveT = setTimeout(saveDraftsNow, 200);
}
watch(orders, scheduleSave, { deep: true });
watch(activeId, scheduleSave);
watch(orderSeq, scheduleSave);

let midnightTimer = null;
async function handleMidnightReset() {
  clearDraftsFE();
}
function scheduleMidnightReset() {
  if (midnightTimer) clearTimeout(midnightTimer);
  const now = new Date();
  const next = new Date(now);
  next.setHours(24, 0, 0, 0);
  midnightTimer = setTimeout(async () => {
    await handleMidnightReset();
    scheduleMidnightReset();
  }, next - now);
}

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

const showProductModal = ref(false);
const showCustomerModal = ref(false);
const showAddressModal = ref(false);
const showQrPayModal = ref(false);
const showPreCheckoutModal = ref(false);
const showPaymentConfirmModal = ref(false);
const backdropOpen = computed(
  () =>
    showProductModal.value ||
    showCustomerModal.value ||
    showAddressModal.value ||
    showQrPayModal.value ||
    showScanQrModal.value,
);

const anyModalOpen = computed(
  () =>
    showProductModal.value ||
    showCustomerModal.value ||
    showAddressModal.value ||
    showQrPayModal.value ||
    showScanQrModal.value ||
    showPreCheckoutModal.value ||
    showPaymentConfirmModal.value,
);

watch(anyModalOpen, (open) => {
  document.body.classList.toggle("modal-open", open);
});

async function closeBackdropModal() {
  showProductModal.value = false;
  showCustomerModal.value = false;
  showAddressModal.value = false;
  showQrPayModal.value = false;

  if (showScanQrModal.value) {
    await closeScanQrModal();
  }
}

const allProducts = ref([]);
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

function normalizeVariantStatus(value) {
  if (value === undefined || value === null || value === "") return true;
  if (typeof value === "boolean") return value;
  if (typeof value === "number") return value === 1;

  const normalized = String(value)
    .trim()
    .toLowerCase()
    .replace(/đ/g, "d")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "");

  if (["true", "1", "active", "enabled", "hoat dong", "con hang"].includes(normalized)) return true;
  if (["false", "0", "inactive", "disabled", "ngung hoat dong", "het hang", "tat"].includes(normalized)) return false;

  return true;
}

function isVariantActive(p) {
  return normalizeVariantStatus(p?.active ?? p?.trangThai ?? p?.trang_thai ?? p?.status);
}

function isProductSelectable(p) {
  return isVariantActive(p) && Number(p?.stock || 0) > 0;
}

function mapSpct(x) {
  const stock = Number(x.soLuongTon || 0);
  const active = normalizeVariantStatus(
    x.trangThai ?? x.trang_thai ?? x.active ?? x.status ?? true,
  );

  return {
    idSpct: Number(x.id),
    code: x.maSanPhamChiTiet || "",
    name: x.tenSanPham || "",
    color: x.tenMauSac || "",
    size: x.tenKichCo || "",
    stock,
    _baseStock: stock,
    active,
    price: Number(x.donGia || 0),
    image: buildImgUrl(
      x.anh || x.anhDaiDien || x.primaryImageUrl || x.mediaAsset,
    ),
  };
}

function getVariantFingerprint(obj) {
  return [
    String(obj?.name || "")
      .trim()
      .toLowerCase(),
    String(obj?.color || "")
      .trim()
      .toLowerCase(),
    String(obj?.size || "")
      .trim()
      .toLowerCase(),
  ].join("|");
}

function findModalProductById(idSpct) {
  const id = Number(idSpct);
  return allProducts.value.find((x) => Number(x.idSpct) === id) || null;
}

function findRepricedProductInList(cartItem, list) {
  const sameId = list.find(
    (p) =>
      Number(p.idSpct) === Number(cartItem.idSpct) &&
      !sameMoney(p.price, cartItem.price),
  );
  if (sameId) return sameId;

  const fp = getVariantFingerprint(cartItem);
  return (
    list.find(
      (p) =>
        getVariantFingerprint(p) === fp && !sameMoney(p.price, cartItem.price),
    ) || null
  );
}

async function fetchAllProducts() {
  if (productLoading.value) return;
  productLoading.value = true;
  try {
    const firstRes = await getAllDetails(0, 100);
    const firstData = firstRes?.data ?? firstRes;

    if (Array.isArray(firstData)) {
      allProducts.value = firstData.map(mapSpct).filter(isProductSelectable);
      return;
    }

    const totalPages = Number(firstData?.totalPages || 1);
    let all = [...(firstData?.content || []).map(mapSpct).filter(isProductSelectable)];

    for (let page = 1; page < totalPages; page++) {
      const res = await getAllDetails(page, 100);
      const data = res?.data ?? res;
      all = all.concat((data?.content || []).map(mapSpct).filter(isProductSelectable));
    }

    allProducts.value = all.filter(isProductSelectable);
  } catch (e) {
    console.error(e);
    toastShow("Không tải được danh sách biến thể", "danger");
  } finally {
    productLoading.value = false;
  }
}

async function reloadProducts() {
  await fetchAllProducts();
  productPage.value = 0;
}

function productPrev() {
  if (productPage.value <= 0) return;
  productPage.value--;
}

function productNext() {
  if (productLast.value) return;
  productPage.value++;
}

function onProductSizeChange() {
  productPage.value = 0;
}

const productPriceBounds = computed(() => {
  if (!allProducts.value.length) return { min: 0, max: 100000000 };
  const prices = allProducts.value.map((p) => Number(p.price || 0));
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
  return [
    ...new Set(allProducts.value.map((p) => p.color).filter(Boolean)),
  ].sort((a, b) => String(a).localeCompare(String(b), "vi"));
});

function getSizeSortValue(value) {
  const number = Number(String(value ?? "").replace(",", "."));
  return Number.isFinite(number) ? number : Number.MAX_SAFE_INTEGER;
}

const productSizeOptions = computed(() => {
  return [
    ...new Set(allProducts.value.map((p) => p.size).filter(Boolean)),
  ].sort((a, b) => {
    const byNumber = getSizeSortValue(a) - getSizeSortValue(b);
    if (byNumber !== 0) return byNumber;
    return String(a).localeCompare(String(b), "vi");
  });
});

const filteredProductsAll = computed(() => {
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

  return allProducts.value.filter((p) => {
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

const pagedProducts = computed(() => {
  const start = productPage.value * productSize.value;
  const end = start + productSize.value;
  return filteredProductsAll.value.slice(start, end);
});

const productTotal = computed(() => filteredProductsAll.value.length);

const productTotalPages = computed(() =>
  Math.max(1, Math.ceil(filteredProductsAll.value.length / productSize.value)),
);

const productLast = computed(
  () => productPage.value >= productTotalPages.value - 1,
);

watch(
  () => [
    productFilters.keyword,
    productFilters.color,
    productFilters.size,
    productFilters.stockStatus,
    productPriceRange.min,
    productPriceRange.max,
  ],
  () => {
    productPage.value = 0;
  },
);

function resetProductFilters() {
  productFilters.keyword = "";
  productFilters.color = "";
  productFilters.size = "";
  productFilters.stockStatus = "";
  productPriceRange.min = productPriceBounds.value.min;
  productPriceRange.max = productPriceBounds.value.max;
  productPage.value = 0;
}

async function openProductModal() {
  showCustomerModal.value = false;
  showAddressModal.value = false;
  showProductModal.value = true;
  await fetchAllProducts();
  resetProductFilters();
}

function closeProductModal() {
  showProductModal.value = false;
}

function clampInt(n, min, max) {
  n = Number.isFinite(Number(n)) ? Math.floor(Number(n)) : min;
  return Math.max(min, Math.min(max, n));
}

function sameMoney(a, b) {
  return Math.round(Number(a || 0)) === Math.round(Number(b || 0));
}

function syncAllCartStocks(order = activeOrder.value) {
  if (!order) return;

  const totalMap = new Map();
  for (const it of order.cart) {
    const id = Number(it.idSpct);
    totalMap.set(id, (totalMap.get(id) || 0) + Number(it.qty || 0));
  }

  for (const it of order.cart) {
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

  if (it.priceChangedLocked || it.lineStatus === "missing") {
    return toastShow(
      `Sản phẩm ${it.code} đã đổi giá hoặc không còn hợp lệ, vui lòng xóa dòng cũ và chọn lại`,
      "warning",
    );
  }

  const nextQty = clampInt(nextQtyRaw, 1, 999999);
  const curQty = Number(it.qty || 0);
  if (nextQty === curQty) return;

  const delta = nextQty - curQty;

  try {
    if (delta > 0) {
      await decreaseStock(Number(it.idSpct), delta);
    } else if (delta < 0) {
      await increaseStock(Number(it.idSpct), Math.abs(delta));
    }
  } catch (err) {
    const rawMsg = err?.response?.data?.message || err?.response?.data || "";
    const remain = Math.max(0, Number(it.stock || 0));
    const fallbackMsg =
      remain <= 0
        ? `Sản phẩm ${it.code} hiện đã hết tồn kho, không thể tăng thêm số lượng`
        : `Số lượng mua không được vượt quá số lượng tồn kho còn lại tồn ${remain}`;

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
  syncAllCartStocks(o);
}

async function incQty(i) {
  const o = activeOrder.value;
  if (!o) return;
  const it = o.cart[i];
  if (!it || it.priceChangedLocked || it.lineStatus === "missing") return;
  await setQtyByInput(i, Number(it.qty || 0) + 1);
}

async function decQty(i) {
  const o = activeOrder.value;
  if (!o) return;
  const it = o.cart[i];
  if (!it || it.priceChangedLocked || it.lineStatus === "missing") return;
  await setQtyByInput(i, Math.max(1, Number(it.qty || 0) - 1));
}

async function removeItem(i) {
  const o = activeOrder.value;
  if (!o) return;
  const it = o.cart[i];
  if (!it) return;

  try {
    if (it.lineStatus !== "missing") {
      await increaseStock(Number(it.idSpct), Number(it.qty || 0));
    }
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
  syncAllCartStocks(o);
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

  if (it.priceChangedLocked || it.lineStatus === "missing") {
    e.target.value = it.qty;
    return;
  }

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
  if (!isVariantActive(p))
    return toastShow(`Biến thể ${p.code || ""} đang tắt trạng thái nên không thể thêm vào đơn`, "warning");
  if ((Number(p.stock) || 0) <= 0)
    return toastShow(`Sản phẩm ${p.code} hiện đã hết tồn kho`, "warning");

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
      color: p.color,
      size: p.size,
      meta: `size ${p.size} / ${p.color}`,
      image: p.image,
      price: Number(p.price || 0),
      qty: 0,
      stockBase: baseBefore,
      stock: baseBefore,
      priceChangedLocked: false,
      newPrice: null,
      lineStatus: "ok",
    });
    idx = o.cart.length - 1;
  }

  await setQtyByInput(idx, Number(o.cart[idx].qty || 0) + 1);
  toastShow(`Đã thêm ${p.code}`, "success");

  await revalidateActiveOrderVoucher({ showModal: false });
}

const subTotal = computed(() => {
  const o = activeOrder.value;
  if (!o) return 0;
  return o.cart.reduce(
    (s, it) => s + (Number(it.price) || 0) * (Number(it.qty) || 0),
    0,
  );
});

const customers = ref([]);
const customerKw = ref("");
const customerLoading = ref(false);
const customerPage = ref(0);
const customerSize = ref(10);

function normalizeCustomerStatus(value) {
  if (value === undefined || value === null || value === "") return true;
  if (typeof value === "boolean") return value;
  if (typeof value === "number") return value === 1;

  const normalized = String(value)
    .trim()
    .toLowerCase()
    .replace(/đ/g, "d")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "");

  if (["true", "1", "active", "enabled", "hoat dong", "con hoat dong"].includes(normalized)) return true;
  if (["false", "0", "inactive", "disabled", "ngung hoat dong", "khong hoat dong", "tat"].includes(normalized)) return false;

  return true;
}

function isCustomerActive(c) {
  return normalizeCustomerStatus(
    c?.active ?? c?.trangThai ?? c?.trang_thai ?? c?.status ??
    c?.raw?.trangThai ?? c?.raw?.trang_thai ?? c?.raw?.active ?? c?.raw?.status
  );
}

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
    active: normalizeCustomerStatus(x.trangThai ?? x.trang_thai ?? x.active ?? x.status ?? true),
    raw: x,
  };
}

function getCustomerScopedVouchers(order, vouchers) {
  const customerId = order?.customer?.id ?? null;
  return (vouchers || []).filter((v) =>
    voucherBelongsToCustomer(v, customerId),
  );
}

async function fetchCustomersAll() {
  if (customerLoading.value) return;
  customerLoading.value = true;
  try {
    const firstRes = await listKhachHang(0, 100);
    const firstData = firstRes?.data ?? firstRes;

    if (Array.isArray(firstData)) {
      customers.value = firstData.map(mapCustomer).filter(isCustomerActive);
      customerPage.value = 0;
      return;
    }

    const totalPages = Number(firstData?.totalPages || 1);
    let all = [...(firstData?.content || []).map(mapCustomer).filter(isCustomerActive)];

    for (let page = 1; page < totalPages; page++) {
      const res = await listKhachHang(page, 100);
      const data = res?.data ?? res;
      all = all.concat((data?.content || []).map(mapCustomer).filter(isCustomerActive));
    }

    customers.value = all.filter(isCustomerActive);
    customerPage.value = 0;
  } catch (e) {
    console.error(e);
    toastShow("Không tải được danh sách khách hàng", "danger");
  } finally {
    customerLoading.value = false;
  }
}

async function reloadCustomers() {
  await fetchCustomersAll();
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

const customerTotalPages = computed(() =>
  Math.max(1, Math.ceil(filteredCustomers.value.length / customerSize.value)),
);
const customerLast = computed(
  () => customerPage.value >= customerTotalPages.value - 1,
);
const customerTotal = computed(() => filteredCustomers.value.length);

watch(customerKw, () => {
  customerPage.value = 0;
});

function openCustomerModal() {
  showProductModal.value = false;
  showAddressModal.value = false;
  showCustomerModal.value = true;
  fetchCustomersAll();
}

function closeCustomerModal() {
  showCustomerModal.value = false;
}

async function chooseCustomer(c) {
  const o = activeOrder.value;
  if (!o) return;

  if (!isCustomerActive(c)) {
    return toastShow("Khách hàng đã tắt hoạt động nên không thể chọn", "warning");
  }

  o.customer = {
    id: c.id,
    name: c.name,
    phone: c.phone,
    email: c.email,
    address: c.address,
  };

  o.customerDraft.phone = c.phone || "";
  o.customerDraft.email = c.email || "";

  if (!String(o.diaChi || "").trim()) {
    o.diaChi = c.address || "";
  }

  await loadCustomerAddressBook(o.customer);

  closeCustomerModal();
  await reloadActiveOrderVouchers({ showModal: true });
}

async function chooseWalkInCustomer() {
  const o = activeOrder.value;
  if (!o) return;

  o.customer = null;
  o.customerDraft = { phone: "", email: "" };
  o.diaChi = "";
  o.addressBook = [];
  o.selectedAddressId = null;

  o.tenNguoiNhanHang = "";
  o.soDienThoaiNhanHang = "";
  o.emailNguoiNhanHang = "";
  o.tinhThanhNhanHang = "";
  o.quanHuyenNhanHang = "";
  o.phuongXaNhanHang = "";
  o.diaChiNhanHangChiTiet = "";

  o.ghnProvinceId = null;
  o.ghnDistrictId = null;
  o.ghnWardCode = "";

  o.phiVanChuyen = 0;
  wards.value = [];
  resetAddressDraft();

  customerKw.value = "";
  customerPage.value = 0;

  closeCustomerModal();
  closeAddressModal();

  await reloadActiveOrderVouchers({ showModal: true });
  toastShow("Đã chuyển về Khách vãng lai", "info");
}

const addressDraft = reactive({
  receiverName: "",
  phone: "",
  provinceId: null,
  wardCode: "",
  detail: "",
  makeDefault: false,
});

const activeOrderAddressBook = computed(
  () => activeOrder.value?.addressBook || [],
);

const selectedAddress = computed(() => {
  const o = activeOrder.value;
  if (!o) return null;
  return (
    (o.addressBook || []).find((x) => x.id === o.selectedAddressId) || null
  );
});

const selectedAddressDisplay = computed(() => {
  const a = selectedAddress.value;
  return a?.fullAddress || "";
});

function resetAddressDraft() {
  addressDraft.receiverName = "";
  addressDraft.phone = "";
  addressDraft.provinceId = null;
  addressDraft.wardCode = "";
  addressDraft.detail = "";
  addressDraft.makeDefault = false;
}

async function openAddressModal() {
  const o = activeOrder.value;
  if (!o?.customer?.id) {
    return toastShow("Vui lòng chọn khách hàng trước", "warning");
  }

  showCustomerModal.value = false;
  await loadCustomerAddressBook(o.customer);
  showAddressModal.value = true;
}

function closeAddressModal() {
  showAddressModal.value = false;
}

async function pickAddress(addressId) {
  const o = activeOrder.value;
  if (!o) return;

  const addr = (o.addressBook || []).find((x) => x.id === addressId);
  if (!addr) return;

  o.selectedAddressId = addr.id;
  o.tenNguoiNhanHang = addr.receiverName || o.customer?.name || "";
  o.soDienThoaiNhanHang = addr.phone || o.customerDraft.phone || "";
  o.emailNguoiNhanHang = o.customerDraft.email || "";
  o.diaChi = addr.fullAddress || "";

  const resolvedProvinceId =
    addr.provinceId || resolveProvinceIdByName(addr.provinceName);

  o.ghnProvinceId = resolvedProvinceId ? String(resolvedProvinceId) : null;
  o.quanHuyenNhanHang = "";
  o.diaChiNhanHangChiTiet = addr.detail || "";

  const province = findProvinceById(o.ghnProvinceId);
  o.tinhThanhNhanHang = province?.provinceName || addr.provinceName || "";

  wards.value = (province?.wards || []).map((w) => ({
    wardCode: w.wardCode,
    wardName: w.wardName,
  }));

  const resolvedWardCode =
    addr.wardCode || resolveWardCodeByName(o.ghnProvinceId, addr.wardName);

  o.ghnWardCode = resolvedWardCode ? String(resolvedWardCode) : "";

  const ward = findWardByCode(o.ghnProvinceId, o.ghnWardCode);
  o.phuongXaNhanHang = ward?.wardName || addr.wardName || "";

  closeAddressModal();
  await refreshShipFee();
  toastShow("Đã chọn địa chỉ", "success");
}

function setDefaultAddress(addressId) {
  const o = activeOrder.value;
  if (!o) return;
  o.addressBook = (o.addressBook || []).map((x) => ({
    ...x,
    isDefault: x.id === addressId,
  }));
  toastShow("Đã cập nhật địa chỉ mặc định", "success");
}

function saveNewAddress() {
  const o = activeOrder.value;
  if (!o?.customer?.id) {
    return toastShow("Vui lòng chọn khách hàng trước", "warning");
  }

  if (!addressDraft.receiverName.trim()) {
    return toastShow("Vui lòng nhập tên người nhận", "warning");
  }
  if (!addressDraft.phone.trim()) {
    return toastShow("Vui lòng nhập số điện thoại", "warning");
  }
  if (!addressDraft.provinceId) {
    return toastShow("Vui lòng chọn Tỉnh/Thành phố", "warning");
  }
  if (!String(addressDraft.wardCode || "").trim()) {
    return toastShow("Vui lòng chọn Phường/Xã", "warning");
  }
  if (!addressDraft.detail.trim()) {
    return toastShow("Vui lòng nhập địa chỉ chi tiết", "warning");
  }

  const provinceName = provinceNameById(addressDraft.provinceId);
  const wardName = wardNameByCodeByProvince(
    addressDraft.provinceId,
    addressDraft.wardCode,
  );

  const fullAddress = [addressDraft.detail.trim(), wardName, provinceName]
    .filter(Boolean)
    .join(", ");

  const newAddr = {
    id: Date.now() + Math.random(),
    receiverName: addressDraft.receiverName.trim(),
    phone: addressDraft.phone.trim(),
    provinceId: String(addressDraft.provinceId),
    provinceName,
    wardCode: String(addressDraft.wardCode),
    wardName,
    detail: addressDraft.detail.trim(),
    fullAddress,
    isDefault: !!addressDraft.makeDefault,
  };

  if (newAddr.isDefault) {
    o.addressBook = (o.addressBook || []).map((x) => ({
      ...x,
      isDefault: false,
    }));
  }

  o.addressBook = [...(o.addressBook || []), newAddr];

  if (newAddr.isDefault || !o.selectedAddressId) {
    o.selectedAddressId = newAddr.id;
  }

  pickAddress(newAddr.id);
  resetAddressDraft();
  toastShow("Đã thêm địa chỉ mới", "success");
}

function wardNameByCodeByProvince(provinceId, wardCode) {
  const province = findProvinceById(provinceId);
  return (
    province?.wards?.find((w) => String(w.wardCode) === String(wardCode))
      ?.wardName || ""
  );
}

function normalizeText(value) {
  return String(value || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/\s+/g, " ")
    .trim();
}

function resolveProvinceIdByName(provinceName) {
  const normalized = normalizeText(provinceName);
  const province = provinceLookup.value.find(
    (item) => normalizeText(item.provinceName) === normalized,
  );
  return province ? String(province.provinceId) : null;
}

function resolveWardCodeByName(provinceId, wardName) {
  if (!provinceId || !wardName) return "";
  const province = findProvinceById(provinceId);
  if (!province) return "";

  const normalized = normalizeText(wardName);
  const ward = (province.wards || []).find(
    (item) => normalizeText(item.wardName) === normalized,
  );

  return ward ? String(ward.wardCode) : "";
}

function buildFallbackAddressBook(customer) {
  if (!customer?.address) return [];

  return [
    {
      id: Date.now(),
      receiverName: customer.name || "",
      phone: customer.phone || "",
      provinceId: null,
      provinceName: "",
      wardCode: "",
      wardName: "",
      detail: customer.address || "",
      fullAddress: customer.address || "",
      isDefault: true,
    },
  ];
}

function mapCustomerAddress(raw, customer) {
  const provinceName = String(raw?.tinhThanh || "").trim();
  const wardName = String(raw?.phuongXa || "").trim();
  const detail = String(raw?.diaChiChiTiet || "").trim();

  const provinceId = resolveProvinceIdByName(provinceName);
  const wardCode = resolveWardCodeByName(provinceId, wardName);

  const fullAddress = [detail, wardName, provinceName, raw?.quocGia]
    .map((x) => String(x || "").trim())
    .filter(Boolean)
    .join(", ");

  return {
    id: raw?.id ?? Date.now() + Math.random(),
    receiverName: raw?.tenNguoiNhan || customer?.name || "",
    phone: raw?.soDienThoai || customer?.phone || "",
    provinceId,
    provinceName,
    wardCode,
    wardName,
    detail,
    fullAddress,
    isDefault: !!raw?.laMacDinh,
  };
}

async function loadCustomerAddressBook(customer) {
  const o = activeOrder.value;
  if (!o?.customer?.id) return;

  try {
    const res = await http.get(`/api/khach-hang/${customer.id}/dia-chi`);
    const rows = Array.isArray(res?.data) ? res.data : [];

    o.addressBook = rows.map((item) => mapCustomerAddress(item, customer));

    if (!o.addressBook.length) {
      o.addressBook = buildFallbackAddressBook(customer);
    }

    const defaultAddr =
      o.addressBook.find((item) => item.isDefault) || o.addressBook[0] || null;

    o.selectedAddressId = defaultAddr?.id ?? null;
  } catch (e) {
    console.error("loadCustomerAddressBook error:", e);
    o.addressBook = buildFallbackAddressBook(customer);
    o.selectedAddressId = o.addressBook[0]?.id ?? null;
  }
}

const provinces = ref([]);
const wards = ref([]);
const provinceOptions = computed(() =>
  (provinces.value || []).map((p) => ({
    value: String(p.provinceId),
    label: p.provinceName,
  })),
);

const wardOptions = computed(() =>
  (wards.value || []).map((w) => ({
    value: String(w.wardCode),
    label: w.wardName,
  })),
);

const provincesLoading = ref(false);
const wardsLoading = ref(false);
const shippingFeeLoading = ref(false);

const provinceLookup = ref([]);

function safeArray(v) {
  return Array.isArray(v) ? v : [];
}

function pickUnitName(obj) {
  return (
    obj?.FullName ||
    obj?.fullName ||
    obj?.Name ||
    obj?.name ||
    obj?.ProvinceName ||
    obj?.provinceName ||
    obj?.DistrictName ||
    obj?.districtName ||
    obj?.WardName ||
    obj?.wardName ||
    ""
  );
}

function pickProvinceCode(obj) {
  return String(
    obj?.ProvinceCode ??
      obj?.provinceCode ??
      obj?.Code ??
      obj?.code ??
      obj?.Id ??
      obj?.id ??
      "",
  ).trim();
}

function pickDistrictCode(obj) {
  return String(
    obj?.DistrictCode ??
      obj?.districtCode ??
      obj?.Code ??
      obj?.code ??
      obj?.Id ??
      obj?.id ??
      "",
  ).trim();
}

function pickWardCode(obj) {
  return String(
    obj?.WardCode ??
      obj?.wardCode ??
      obj?.Code ??
      obj?.code ??
      obj?.Id ??
      obj?.id ??
      "",
  ).trim();
}

function normalizeVnUnits(raw) {
  return safeArray(raw).map((p) => ({
    provinceId: pickProvinceCode(p),
    provinceName: pickUnitName(p),
    wards: safeArray(p?.Wards || p?.wards).map((w) => ({
      wardCode: pickWardCode(w),
      wardName: pickUnitName(w),
      provinceId: pickProvinceCode(p),
    })),
  }));
}

async function loadGhnProvinces() {
  provincesLoading.value = true;
  try {
    provinceLookup.value = normalizeVnUnits(vnUnits);
    provinces.value = provinceLookup.value.map((p) => ({
      provinceId: p.provinceId,
      provinceName: p.provinceName,
    }));
  } catch (e) {
    console.error(e);
    provinces.value = [];
    provinceLookup.value = [];
    toastShow("Không tải được dữ liệu tỉnh/thành", "warning");
  } finally {
    provincesLoading.value = false;
  }
}

function findProvinceById(id) {
  return (
    provinceLookup.value.find((x) => String(x.provinceId) === String(id)) ||
    null
  );
}

function findWardByCode(provinceId, wardCode) {
  const province = findProvinceById(provinceId);
  if (!province) return null;
  return (
    province.wards.find((x) => String(x.wardCode) === String(wardCode)) || null
  );
}

async function onProvinceChange(provinceId) {
  const o = activeOrder.value;
  if (!o) return;

  o.ghnProvinceId = provinceId ? String(provinceId) : null;
  o.ghnDistrictId = null;
  o.ghnWardCode = "";
  o.tinhThanhNhanHang = "";
  o.quanHuyenNhanHang = "";
  o.phuongXaNhanHang = "";
  wards.value = [];

  const province = findProvinceById(o.ghnProvinceId);
  o.tinhThanhNhanHang = province?.provinceName || "";

  if (!o.ghnProvinceId) {
    o.phiVanChuyen = 0;
    return;
  }

  wardsLoading.value = true;
  try {
    wards.value = (province?.wards || []).map((w) => ({
      wardCode: w.wardCode,
      wardName: w.wardName,
    }));
  } finally {
    wardsLoading.value = false;
  }

  await refreshShipFee();
}

async function onWardChange(wardCode) {
  const o = activeOrder.value;
  if (!o) return;

  o.ghnWardCode = String(wardCode || "");
  const ward = findWardByCode(o.ghnProvinceId, o.ghnWardCode);
  o.phuongXaNhanHang = ward?.wardName || "";
  o.quanHuyenNhanHang = "";

  await refreshShipFee();
}
async function refreshShipFee() {
  const o = activeOrder.value;
  if (!o || !o.loaiDon) return;

  if (!o.ghnProvinceId || !o.ghnWardCode) {
    o.phiVanChuyen = 0;
    return;
  }

  shippingFeeLoading.value = true;
  try {
    const province = findProvinceById(o.ghnProvinceId);
    const ward = findWardByCode(o.ghnProvinceId, o.ghnWardCode);

    if (!province || !ward) {
      o.phiVanChuyen = 0;
      return;
    }

    // mock phí ship để test trước
    let fee = 30000;

    const provinceName = String(province.provinceName || "").toLowerCase();
    const wardName = String(ward.wardName || "").toLowerCase();

    if (
      provinceName.includes("hà nội") ||
      provinceName.includes("hồ chí minh")
    ) {
      fee = 20000;
    }

    if (wardName.includes("xã")) {
      fee += 5000;
    }

    o.phiVanChuyen = fee;
  } catch (e) {
    console.error(e);
    o.phiVanChuyen = 0;
    toastShow("Không tính được phí vận chuyển", "warning");
  } finally {
    shippingFeeLoading.value = false;
  }
}
const addressDraftWards = computed(() => {
  const province = findProvinceById(addressDraft.provinceId);
  return (province?.wards || []).map((w) => ({
    wardCode: w.wardCode,
    wardName: w.wardName,
  }));
});

function onAddressProvinceChange(provinceId) {
  addressDraft.provinceId = provinceId ? String(provinceId) : null;
  addressDraft.wardCode = "";
}

async function toggleShip(e) {
  const o = activeOrder.value;
  if (!o) return;

  o.loaiDon = !!e.target.checked;

  if (o.loaiDon) {
    o.phiVanChuyen = Number(o.phiVanChuyen || 0);
    if (!provinces.value.length) {
      await loadGhnProvinces();
    }
  } else {
    o.phiVanChuyen = 0;
    o.tenNguoiNhanHang = "";
    o.soDienThoaiNhanHang = "";
    o.emailNguoiNhanHang = "";
    o.tinhThanhNhanHang = "";
    o.quanHuyenNhanHang = "";
    o.phuongXaNhanHang = "";
    o.diaChiNhanHangChiTiet = "";
    o.ghnProvinceId = null;
    o.ghnDistrictId = null;
    o.ghnWardCode = "";
    wards.value = [];
  }
}

watch(
  () =>
    JSON.stringify(
      (activeOrder.value?.cart || []).map((it) => ({
        id: it.idSpct,
        qty: it.qty,
      })),
    ),
  async () => {
    if (activeOrder.value?.loaiDon) {
      await refreshShipFee();
    }
  },
);

function pickId(obj) {
  if (!obj || typeof obj !== "object") return null;
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
    khach_hang_ids: normalizeKhIds(khIdsRaw),
    khach_hang_id: khSingle != null ? Number(khSingle) : null,
    ngay_bat_dau: x.ngayBatDau ?? x.ngay_bat_dau ?? null,
    ngay_ket_thuc: x.ngayKetThuc ?? x.ngay_ket_thuc ?? null,
  };
}

async function loadVouchersByCustomer(customerId) {
  try {
    const res = await http.get("/api/pgg/pos", {
      params: { khachHangId: customerId ?? null },
    });

    return (res.data || [])
      .map(normalizeVoucher)
      .filter((v) => voucherBelongsToCustomer(v, customerId));
  } catch (e) {
    console.error(e);
    return [];
  }
}

async function reloadActiveOrderVouchers({ showModal = false } = {}) {
  const order = activeOrder.value;
  if (!order) {
    activeOrderVouchers.value = [];
    return;
  }

  const orderVouchers = await loadVouchersByCustomer(
    order.customer?.id ?? null,
  );
  activeOrderVouchers.value = orderVouchers;

  const result = evaluateVoucherState(order, orderVouchers);

  if (showModal) {
    await handleVoucherStateWithModal(result, orderVouchers);
  } else if (result.status === "best_voucher_ready" && result.best?.voucher) {
    applyBestVoucherNowForOrder(order, orderVouchers, "best");
  } else if (result.status === "valid" && result.currentVoucher) {
    setVoucherSnapshot(order, result.currentVoucher);
  }
}
function isPersonalVoucher(v) {
  const lp = v?.loai_phieu;
  if (lp === true) return true;
  if (lp === false) return false;
  const s = String(lp || "").toUpperCase();
  return s === "CA_NHAN" || s === "PERSONAL";
}

function voucherBelongsToCustomer(v, customerId) {
  if (!isPersonalVoucher(v)) return true;
  if (!customerId) return false;
  if (Array.isArray(v.khach_hang_ids) && v.khach_hang_ids.length) {
    return v.khach_hang_ids.includes(Number(customerId));
  }
  if (v.khach_hang_id != null) {
    return Number(v.khach_hang_id) === Number(customerId);
  }
  return true;
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

function clearVoucherSuggestionDismissed(o) {
  if (o) o.voucherSuggestionDismissedKey = "";
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
function keepVoucherWaitingForBest(o) {
  if (!o) return;
  o.voucherMode = "best";
  o.pggId = null;
  o.voucherCode = "";
  o.discountPercent = 0;
  clearVoucherSuggestionDismissed(o);
  // giữ snapshot để biết trước đó user từng có mã
}

function getVoucherEntriesForOrder(order, vouchers) {
  const st = order
    ? order.cart.reduce(
        (s, it) => s + (Number(it.price) || 0) * (Number(it.qty) || 0),
        0,
      )
    : 0;

  return vouchers
    .map((v) => ({ v, discount: calcVoucherDiscount(st, v) }))
    .sort((a, b) => b.discount - a.discount);
}

const eligibleVoucherEntries = computed(() =>
  getVoucherEntriesForOrder(
    activeOrder.value,
    activeOrderVouchers.value,
  ).filter((x) => x.discount > 0),
);

const bestEligibleVoucherEntry = computed(
  () => eligibleVoucherEntries.value[0] || null,
);

const altEligibleVoucherEntries = computed(() => {
  return [...eligibleVoucherEntries.value].sort(
    (a, b) => a.discount - b.discount,
  );
});

const appliedVoucher = computed(() => {
  const o = activeOrder.value;
  if (!o?.pggId) return null;
  return activeOrderVouchers.value.find((x) => x.id === o.pggId) || null;
});

const appliedVoucherEntry = computed(() => {
  const o = activeOrder.value;
  if (!o?.pggId) return null;
  const v = activeOrderVouchers.value.find((x) => x.id === o.pggId) || null;
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

watch(
  [
    subTotal,
    activeOrderVouchers,
    activeId,
    () => activeOrder.value?.customer?.id,
  ],
  () => {
    const o = activeOrder.value;
    if (!o) return;

    if (o.voucherMode === "none") {
      o.pggId = null;
      o.voucherCode = "";
      o.voucherSnapshot = null;
      return;
    }

    // KHÔNG sync best ở đây nữa
    // để giữ state cũ cho evaluateVoucherState kiểm tra
  },
  { immediate: true },
);

watch(
  [activeId, () => activeOrder.value?.customer?.id],
  async () => {
    await reloadActiveOrderVouchers({ showModal: true });
  },
  { immediate: true },
);

const voucherSuggestions = computed(() => {
  const o = activeOrder.value;
  const cid = o?.customer?.id ?? null;
  const st = subTotal.value;
  if (st <= 0) return [];

  const bestNow = bestEligibleVoucherEntry.value?.discount || 0;

  return activeOrderVouchers.value
    .filter((v) => voucherBelongsToCustomer(v, cid))
    .filter(
      (v) =>
        v?.trang_thai &&
        (Number(v.so_luong) || 0) > 0 &&
        isVoucherInDateRange(v),
    )
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

  const cid = o?.customer?.id;
  if (isPersonalVoucher(v)) {
    if (!cid)
      return toastShow(
        "Voucher cá nhân: vui lòng chọn khách hàng trước",
        "warning",
      );
    if (!voucherBelongsToCustomer(v, cid)) {
      return toastShow(
        "Voucher này không thuộc khách hàng hiện tại",
        "warning",
      );
    }
  }

  o.voucherMode = "manual";
  o.pggId = v.id;
  o.voucherCode = v.ma_giam_gia || "";
  o.discountPercent = 0;
  clearVoucherSuggestionDismissed(o);
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
  clearVoucherSuggestionDismissed(o);
  toastShow("Đã tắt mã giảm giá", "info");
}

function applyBestVoucherNowForOrder(o, vouchers, mode = "best") {
  if (!o) return;

  const best = getVoucherEntriesForOrder(o, vouchers).filter(
    (x) => x.discount > 0,
  )[0];
  if (!best?.v) return;

  o.voucherMode = mode;
  o.pggId = best.v.id;
  o.voucherCode = best.v.ma_giam_gia || "";
  o.discountPercent = 0;
  clearVoucherSuggestionDismissed(o);
  setVoucherSnapshot(o, best.v);
}

function removeVoucherNowForOrder(o, { keepSnapshot = false } = {}) {
  if (!o) return;
  o.voucherMode = "none";
  o.pggId = null;
  o.voucherCode = "";

  if (!keepSnapshot) {
    o.voucherSnapshot = null;
  }

  clearVoucherSuggestionDismissed(o);
}

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

const paymentConfirmText = ref("");
let _resolvePaymentConfirm = null;
let _resolveConfirm = null;
const confirmHint = ref("");
const voucherSyncModalLock = ref(false);

const preCheckoutUi = reactive({
  type: "info",
  title: "Thông báo phiếu giảm giá",
  subtitle: "",
  message: "",
  detail: "",
  rows: [],
  mode: "ack", // ack | confirm
  confirmText: "Xác nhận",
  cancelText: "Hủy",
});

function openConfirm({
  type = "info",
  title = "Thông báo phiếu giảm giá",
  subtitle = "",
  message = "",
  detail = "",
  rows = [],
  mode = "ack",
  confirmText = "Đã hiểu",
  cancelText = "Không",
}) {
  preCheckoutUi.type = type;
  preCheckoutUi.title = title;
  preCheckoutUi.subtitle = subtitle;
  preCheckoutUi.message = message;
  preCheckoutUi.detail = detail;
  preCheckoutUi.rows = Array.isArray(rows) ? rows : [];
  preCheckoutUi.mode = mode;
  preCheckoutUi.confirmText = confirmText;
  preCheckoutUi.cancelText = cancelText;

  showPreCheckoutModal.value = true;

  return new Promise((resolve) => {
    _resolveConfirm = resolve;
  });
}

function resolvePreCheckout(ok = true) {
  showPreCheckoutModal.value = false;
  const r = _resolveConfirm;
  _resolveConfirm = null;
  if (r) r(!!ok);
}
function openPaymentConfirm(text) {
  paymentConfirmText.value =
    text || "Bạn có chắc muốn thanh toán hóa đơn này không?";
  showPaymentConfirmModal.value = true;

  return new Promise((resolve) => {
    _resolvePaymentConfirm = resolve;
  });
}

function resolvePaymentConfirm(ok) {
  showPaymentConfirmModal.value = false;
  const r = _resolvePaymentConfirm;
  _resolvePaymentConfirm = null;
  if (r) r(!!ok);
}

function getVoucherInvalidReason(v, subtotal) {
  if (!v) return { code: "NOT_FOUND", message: "Voucher không tồn tại" };
  if (!v.trang_thai) return { code: "DISABLED", message: "Voucher đã bị tắt" };
  if ((Number(v.so_luong) || 0) <= 0)
    return { code: "OUT_OF_STOCK", message: "Voucher đã hết lượt" };
  if (!isVoucherInDateRange(v))
    return { code: "EXPIRED", message: "Voucher đã hết hạn / chưa bắt đầu" };
  if ((Number(subtotal) || 0) < (Number(v.don_hang_toi_thieu) || 0)) {
    return {
      code: "MIN_ORDER_NOT_MET",
      message: "Đơn hàng chưa đạt đơn tối thiểu",
    };
  }
  return null;
}
function getVoucherChangedFields(snap, vNow) {
  if (!snap || !vNow) return [];
  const changes = [];

  const minOld = Number(snap.minOrder || 0);
  const minNew = Number(vNow.don_hang_toi_thieu || 0);
  if (minOld !== minNew) {
    changes.push(`Đơn tối thiểu: ${money(minOld)} → ${money(minNew)}`);
  }

  const endOld = String(snap.end || "");
  const endNew = String(vNow.ngay_ket_thuc || "");
  if (endOld !== endNew) {
    changes.push(`Hạn dùng: ${formatDateVN(endOld)} → ${formatDateVN(endNew)}`);
  }

  const qtyOld = Number(snap.soLuong || 0);
  const qtyNew = Number(vNow.so_luong || 0);
  if (qtyOld !== qtyNew) {
    changes.push(`Số lượng còn: ${qtyOld} → ${qtyNew}`);
  }

  const stOld = !!snap.trangThai;
  const stNew = !!vNow.trang_thai;
  if (stOld !== stNew) {
    changes.push(
      `Trạng thái: ${stOld ? "Bật" : "Tắt"} → ${stNew ? "Bật" : "Tắt"}`,
    );
  }

  return changes;
}

function getBestVoucherForOrder(order, vouchers) {
  const bestEntry =
    getVoucherEntriesForOrder(order, vouchers).filter(
      (x) => x.discount > 0,
    )[0] || null;

  return bestEntry
    ? {
        id: bestEntry.v.id,
        code: bestEntry.v.ma_giam_gia,
        discount: bestEntry.discount,
        voucher: bestEntry.v,
      }
    : null;
}
function syncBestVoucherForActiveOrder() {
  const o = activeOrder.value;
  if (!o || o.voucherMode !== "best") return;

  const best = bestEligibleVoucherEntry.value?.v || null;

  o.pggId = best?.id ?? null;
  o.voucherCode = best?.ma_giam_gia || "";
  clearVoucherSuggestionDismissed(o);

  if (best) setVoucherSnapshot(o, best);
  else o.voucherSnapshot = null;
}

function buildVoucherNotifyRows({
  oldCode = "—",
  oldDiscount = 0,
  newCode = "—",
  newDiscount = 0,
  extraRows = [],
}) {
  return [
    { label: "Phiếu trước đó", value: oldCode || "—" },
    {
      label: "Giảm trước đó",
      value: `- ${money(oldDiscount)}`,
      valueClass: "text-danger fw-semibold",
    },
    { label: "Phiếu cập nhật", value: newCode || "Không có phiếu thay thế" },
    {
      label: "Giảm sau cập nhật",
      value: `- ${money(newDiscount)}`,
      valueClass: "text-danger fw-semibold",
    },
    ...extraRows,
  ];
}

function evaluateVoucherState(order, orderVouchers) {
  if (!order) return { status: "no_order" };

  const subtotal = order.cart.reduce(
    (s, it) => s + Number(it.price || 0) * Number(it.qty || 0),
    0,
  );

  const snapshot = order.voucherSnapshot || null;

  const currentVoucher = order.pggId
    ? orderVouchers.find((v) => Number(v.id) === Number(order.pggId)) || null
    : null;

  const snapshotVoucher = snapshot?.id
    ? orderVouchers.find((v) => Number(v.id) === Number(snapshot.id)) || null
    : null;

  const best = getBestVoucherForOrder(order, orderVouchers);

  // ƯU TIÊN: trước đó từng có voucher, giờ voucher đó biến mất
  if (snapshot && !snapshotVoucher) {
    return {
      status: "voucher_missing",
      order,
      best,
      currentVoucher: null,
      snapshot,
      subtotal,
    };
  }

  // Nếu đang giữ pggId mà voucher hiện tại không còn
  if (order.pggId && !currentVoucher) {
    return {
      status: "voucher_missing",
      order,
      best,
      currentVoucher: null,
      snapshot,
      subtotal,
    };
  }

  // Nếu đang ở mode best và chưa có pggId nhưng đã có voucher phù hợp
  if (!order.pggId && order.voucherMode === "best" && best?.voucher) {
    return {
      status: "best_voucher_ready",
      order,
      best,
      currentVoucher: null,
      snapshot,
      subtotal,
    };
  }

  if (!order.pggId && !snapshot) {
    return { status: "no_voucher" };
  }

  const checkingVoucher = currentVoucher || snapshotVoucher;

  if (checkingVoucher) {
    const invalidReason = getVoucherInvalidReason(checkingVoucher, subtotal);
    if (invalidReason) {
      return {
        status: "voucher_invalid",
        order,
        best,
        currentVoucher: checkingVoucher,
        snapshot,
        subtotal,
        invalidReason,
      };
    }
  }

  if (snapshot && checkingVoucher) {
    const changes = getVoucherChangedFields(snapshot, checkingVoucher);
    if (changes.length > 0) {
      return {
        status: "voucher_changed",
        order,
        best,
        currentVoucher: checkingVoucher,
        snapshot,
        subtotal,
        changes,
      };
    }
  }

  if (
    checkingVoucher &&
    best &&
    Number(best.id) !== Number(checkingVoucher.id)
  ) {
    const currentDiscount = calcVoucherDiscount(subtotal, checkingVoucher);
    if (best.discount > currentDiscount) {
      return {
        status: "better_voucher_available",
        order,
        best,
        currentVoucher: checkingVoucher,
        snapshot,
        subtotal,
        currentDiscount,
      };
    }
  }

  return {
    status: "valid",
    order,
    best,
    currentVoucher: checkingVoucher,
    snapshot,
    subtotal,
  };
}

async function handleVoucherStateWithModal(result, orderVouchers) {
  if (!result || result.status === "no_order" || result.status === "no_voucher")
    return;

  if (result.status === "valid") {
    if (result.currentVoucher)
      setVoucherSnapshot(result.order, result.currentVoucher);
    return;
  }

  if (voucherSyncModalLock.value) return;
  voucherSyncModalLock.value = true;

  try {
    const order = result.order;
    if (result.status === "best_voucher_ready") {
      applyBestVoucherNowForOrder(order, orderVouchers, "best");
      return;
    }
    if (result.status === "voucher_missing") {
      const oldCode = result.snapshot?.code || order.voucherCode || "—";
      const oldDiscount = 0;
      const newCode = result.best?.code || "Không có phiếu thay thế";
      const newDiscount = result.best?.discount || 0;

      await openConfirm({
        type: "danger",
        title: "Phiếu giảm giá đã ngừng hoạt động",
        subtitle: "Hệ thống vừa kiểm tra lại trạng thái phiếu giảm giá.",
        message: `Phiếu giảm giá "${oldCode}" không còn khả dụng.`,
        detail: result.best
          ? "Phiếu cũ đã hết hiệu lực hoặc không còn dùng được."
          : "Phiếu cũ đã hết hiệu lực và hiện không còn phiếu phù hợp để áp dụng.",
        rows: buildVoucherNotifyRows({
          oldCode,
          oldDiscount,
          newCode,
          newDiscount,
        }),
      });

      if (result.best?.voucher) {
        applyBestVoucherNowForOrder(order, orderVouchers, "best");
      } else {
        removeVoucherNowForOrder(order, { keepSnapshot: true });
      }

      return;
    }

    if (result.status === "voucher_invalid") {
      const oldCode =
        result.currentVoucher?.ma_giam_gia || order.voucherCode || "—";
      const invalid = result.invalidReason || {};
      const oldDiscount = calcVoucherDiscount(
        result.subtotal,
        result.currentVoucher,
      );
      const newCode = result.best?.code || "Không có phiếu thay thế";
      const newDiscount = result.best?.discount || 0;

      await openConfirm({
        type: "danger",
        title: "Phiếu giảm giá không còn hợp lệ",
        subtitle: "Hệ thống vừa đồng bộ lại dữ liệu phiếu giảm giá.",
        message: `Phiếu giảm giá "${oldCode}" không còn hợp lệ.`,
        detail:
          invalid.message ||
          "Phiếu giảm giá này không còn thỏa điều kiện áp dụng.",
        rows: buildVoucherNotifyRows({
          oldCode,
          oldDiscount,
          newCode:
            invalid.code === "MIN_ORDER_NOT_MET"
              ? "Chờ đủ điều kiện để áp dụng lại mã tốt nhất"
              : newCode,
          newDiscount: invalid.code === "MIN_ORDER_NOT_MET" ? 0 : newDiscount,
        }),
      });

      if (invalid.code === "MIN_ORDER_NOT_MET") {
        keepVoucherWaitingForBest(order);
      } else if (
        result.best?.voucher &&
        result.best.id !== result.currentVoucher?.id
      ) {
        applyBestVoucherNowForOrder(order, orderVouchers, "best");
      } else {
        removeVoucherNowForOrder(order);
      }

      return;
    }

    // voucher_changed: chỉ cập nhật snapshot, không bật modal
    if (result.status === "voucher_changed") {
      if (result.currentVoucher) {
        setVoucherSnapshot(order, result.currentVoucher);
      }
      return;
    }

    // better_voucher_available: KHÔNG bật modal ở realtime
    if (result.status === "better_voucher_available") {
      return;
    }
  } finally {
    voucherSyncModalLock.value = false;
  }
}

async function revalidateActiveOrderVoucher({ showModal = true } = {}) {
  if (voucherSyncModalLock.value) return;
  const order = activeOrder.value;
  if (!order) return;

  const orderVouchers = await loadVouchersByCustomer(
    order?.customer?.id ?? null,
  );
  activeOrderVouchers.value = orderVouchers;

  const result = evaluateVoucherState(order, orderVouchers);

  if (showModal) {
    await handleVoucherStateWithModal(result, orderVouchers);
  } else if (result.status === "best_voucher_ready" && result.best?.voucher) {
    applyBestVoucherNowForOrder(order, orderVouchers, "best");
  } else if (result.status === "valid" && result.currentVoucher) {
    setVoucherSnapshot(order, result.currentVoucher);
  }
}

async function runVoucherPrecheckFlow() {
  const o = activeOrder.value;
  if (!o) return false;

  if (!o.pggId && !o.voucherSnapshot && o.voucherMode !== "best") return true;

  const orderVouchers = await loadVouchersByCustomer(o?.customer?.id ?? null);
  activeOrderVouchers.value = orderVouchers;

  const currentVoucher = o.pggId
    ? orderVouchers.find((v) => v.id === o.pggId) || null
    : null;

  const bestEntry =
    getVoucherEntriesForOrder(o, orderVouchers).filter(
      (x) => x.discount > 0,
    )[0] || null;

  const best = bestEntry
    ? {
        id: bestEntry.v.id,
        code: bestEntry.v.ma_giam_gia,
        discount: bestEntry.discount,
        voucher: bestEntry.v,
      }
    : null;
  if (!o.pggId && o.voucherMode === "best" && best?.voucher) {
    applyBestVoucherNowForOrder(o, orderVouchers, "best");
    return true;
  }

  const subtotal = subTotal.value;
  const isBestMode = o.voucherMode === "best";
  const isManualMode = o.voucherMode === "manual";

  // 1. Voucher bị mất / ngừng hoạt động
  if (o.pggId && !currentVoucher) {
    const oldCode = o.voucherCode || o.voucherSnapshot?.code || "—";

    await openConfirm({
      type: "danger",
      title: "Phiếu giảm giá đã ngừng hoạt động",
      subtitle: "Hệ thống vừa kiểm tra lại trạng thái phiếu giảm giá.",
      message: `Phiếu giảm giá "${oldCode}" không còn khả dụng.`,
      detail: best
        ? `Sau khi đóng thông báo, hệ thống sẽ cập nhật sang phiếu "${best.code}" với mức giảm ${money(best.discount)}.`
        : "Hiện không còn phiếu phù hợp để áp dụng.",
      rows: buildVoucherNotifyRows({
        oldCode,
        oldDiscount: 0,
        newCode: best?.code || "Không có phiếu thay thế",
        newDiscount: best?.discount || 0,
      }),
    });

    if (best?.voucher) {
      if (isBestMode) applyBestVoucherNowForOrder(o, orderVouchers, "best");
      else applyVoucherManual(best.voucher);
    } else {
      removeVoucherNowForOrder(o, { keepSnapshot: true });
    }

    return true;
  }

  // 2. Voucher không hợp lệ
  if (currentVoucher) {
    const invalidReason = getVoucherInvalidReason(currentVoucher, subtotal);
    if (invalidReason) {
      const oldCode = currentVoucher.ma_giam_gia || o.voucherCode || "—";
      const oldDiscount = calcVoucherDiscount(subtotal, currentVoucher);

      await openConfirm({
        type: "danger",
        title: "Phiếu giảm giá không còn hợp lệ",
        subtitle: "Hệ thống vừa đồng bộ lại dữ liệu phiếu giảm giá.",
        message: `Phiếu giảm giá "${oldCode}" không còn hợp lệ.`,
        detail: invalidReason.message,
        rows: buildVoucherNotifyRows({
          oldCode,
          oldDiscount,
          newCode:
            invalidReason.code === "MIN_ORDER_NOT_MET"
              ? "Chờ đủ điều kiện để áp dụng lại mã tốt nhất"
              : best?.code || "Không có phiếu thay thế",
          newDiscount:
            invalidReason.code === "MIN_ORDER_NOT_MET"
              ? 0
              : best?.discount || 0,
        }),
      });

      if (invalidReason.code === "MIN_ORDER_NOT_MET") {
        keepVoucherWaitingForBest(o);
      } else if (best?.voucher && best.id !== currentVoucher.id) {
        if (isBestMode) applyBestVoucherNowForOrder(o, orderVouchers, "best");
        else applyVoucherManual(best.voucher);
      } else {
        removeVoucherNowForOrder(o);
      }

      return true;
    }
  }

  // 3. Điều kiện thay đổi -> chỉ cập nhật snapshot, không show như lỗi đột xuất
  if (o.voucherSnapshot && currentVoucher) {
    const changes = getVoucherChangedFields(o.voucherSnapshot, currentVoucher);
    if (changes.length > 0) {
      setVoucherSnapshot(o, currentVoucher);
    }
  }

  // 4. Có mã tốt hơn -> CHỈ hỏi lúc thanh toán
  if (currentVoucher && best && best.id !== currentVoucher.id) {
    const currentDiscount = calcVoucherDiscount(subtotal, currentVoucher);

    if (best.discount > currentDiscount) {
      // mode best thì tự đồng bộ, không hỏi
      if (isBestMode) {
        applyBestVoucherNowForOrder(o, orderVouchers, "best");
        return true;
      }

      // mode manual thì chỉ lúc thanh toán mới hỏi
      if (isManualMode) {
        const ok = await openConfirm({
          type: "info",
          title: "Đã tìm thấy phiếu giảm giá tốt hơn",
          subtitle:
            "Hệ thống kiểm tra thấy có mã tối ưu hơn trước khi thanh toán.",
          message: `Hiện có phiếu giảm giá tốt hơn cho đơn hàng này. Bạn có muốn áp dụng không?`,
          detail: `Bạn đang dùng "${currentVoucher.ma_giam_gia}" giảm ${money(currentDiscount)}. Nếu đổi sang "${best.code}" thì sẽ giảm ${money(best.discount)}.`,
          rows: buildVoucherNotifyRows({
            oldCode: currentVoucher.ma_giam_gia || "—",
            oldDiscount: currentDiscount,
            newCode: best.code || "—",
            newDiscount: best.discount || 0,
          }),
          mode: "confirm",
          confirmText: "Áp dụng",
          cancelText: "Giữ mã hiện tại",
        });

        if (ok) {
          applyVoucherManual(best.voucher);
        }
        return true;
      }
    }
  }

  if (currentVoucher) {
    setVoucherSnapshot(o, currentVoucher);
  }

  return true;
}
async function openQrPay() {
  const o = activeOrder.value;
  if (!o) return;

  const err = validateCheckout(o);

  if (err && !String(err).includes("Khách thanh toán chưa đủ")) {
    return toastShow(err, "warning");
  }

  if (!o.dbId) {
    return toastShow(
      "Đơn hàng chưa có ID hệ thống. Vui lòng tạo lại đơn rồi thanh toán QR.",
      "warning",
    );
  }

  try {
    o.paymentMethod = "QR";

    const qrText = String(o.maHoaDon || "").trim();

    qrRequestCode.value = "";
    qrPaymentUrl.value = "";
    qrContent.value = qrText;
    qrNoteDraft.value = `Khách đã thanh toán QR - ${qrText}`;

    showQrPayModal.value = true;

    clearTimeout(_syncDraftT);
    _syncDraftT = null;

    await http.post(`/api/hoa-don/draft/${o.dbId}/sync-pos`, buildSyncPayload(o));
    await http.post(`/api/hoa-don/draft/${o.dbId}/push-qr`, {
      qrCode: "",
      message: "Hiển thị QR thanh toán",
    });
  } catch (e) {
    console.error(e);
    toastShow("App chưa nhận được QR. Kiểm tra lại kết nối realtime.", "warning");
  }
}

function closeQrPay() {
  showQrPayModal.value = false;
  qrContent.value = "";
  qrPaymentUrl.value = "";
  qrRequestCode.value = "";
  qrNoteDraft.value = "";
}
async function markPaidAndCheckout() {
  const o = activeOrder.value;
  if (!o || submitting.value) return;

  o.paymentMethod = "QR";
  o.paid = Number(grandTotal.value || 0);
  o.maGiaoDich = `QR-${o.maHoaDon}-${Date.now()}`;
  o.ghiChuThanhToan = (
    qrNoteDraft.value || `Khách đã thanh toán QR - ${o.maHoaDon}`
  ).trim();

  const err = validateCheckout(o);
  if (err) {
    toastShow(err, err.includes("chưa đủ") ? "warning" : "danger");
    return;
  }

  if (o.dbId) {
    try {
      await http.post(`/api/hoa-don/draft/${o.dbId}/qr-paid`, {
        message: "Thanh toán QR thành công",
      });
    } catch (e) {
      console.error("push QR paid to app error", e);
    }
  }

  closeQrPay();
  toastShow("Đang xử lý thanh toán...", "info");

  await confirmOrder();
}

function validateCheckout(o) {
  if (!o) return "Không có đơn hàng đang chọn";
  if (!Array.isArray(o.cart) || o.cart.length === 0) return "Giỏ hàng trống";

  for (const it of o.cart) {
    if (it.priceChangedLocked) {
      return `Sản phẩm ${it.code} đã thay đổi giá, vui lòng xóa dòng cũ và chọn lại dòng giá mới`;
    }
    if (it.lineStatus === "missing") {
      return `Sản phẩm ${it.code} không còn hợp lệ, vui lòng xóa khỏi giỏ`;
    }
    const qty = Number(it.qty || 0);
    if (qty <= 0) return `Số lượng không hợp lệ: ${it.code}`;
    if (Number.isFinite(it.stockBase) && qty > Number(it.stockBase)) {
      return `Sản phẩm ${it.code} vượt tồn kho`;
    }
  }

  if (o.loaiDon) {
    const ten = String(o.tenNguoiNhanHang || "").trim();
    const sdt = String(o.soDienThoaiNhanHang || "").trim();

    if (!ten) return "Vui lòng nhập Tên người nhận";
    if (!sdt) return "Vui lòng nhập SĐT người nhận";

    const digits = sdt.replace(/[^\d]/g, "");
    if (digits.length < 9 || digits.length > 15)
      return "SĐT người nhận không hợp lệ";

    const emailNguoiNhan = String(o.emailNguoiNhanHang || "").trim();
    if (!emailNguoiNhan) return "Vui lòng nhập Email người nhận";
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailNguoiNhan)) {
      return "Email người nhận không hợp lệ";
    }

    if (!o.ghnProvinceId) return "Vui lòng chọn Tỉnh/Thành";
    if (!String(o.ghnWardCode || "").trim()) return "Vui lòng chọn Phường/Xã";
    if (!String(o.diaChiNhanHangChiTiet || "").trim())
      return "Vui lòng nhập Địa chỉ chi tiết";
  }

  const paid = Number(o.paid || 0);
  if (paid < grandTotal.value) return "Khách thanh toán chưa đủ";

  return null;
}

function provinceNameById(id) {
  return (
    provinces.value.find((p) => String(p.provinceId) === String(id))
      ?.provinceName || ""
  );
}

function wardNameByCode(code) {
  const o = activeOrder.value;
  if (!o?.ghnProvinceId) return "";
  if (!o.dbId) {
    return toastShow(
      "Đơn hàng chưa có ID hệ thống. Vui lòng xóa đơn này, tạo đơn mới rồi thanh toán QR.",
      "warning",
    );
  }

  return wardNameByCodeByProvince(o.ghnProvinceId, code);
}
async function confirmOrderCash() {
  const o = activeOrder.value;
  if (!o || submitting.value) return;

  o.paymentMethod = "CASH";
  o.maGiaoDich = null;
  o.ghiChuThanhToan = null;

  const err = validateCheckout(o);
  if (err) {
    toastShow(err, err.includes("chưa đủ") ? "warning" : "danger");
    return;
  }

  const ok = await openPaymentConfirm(
    `Xác nhận thanh toán tiền mặt hóa đơn ${o.maHoaDon}?`,
  );

  if (!ok) return;

  await confirmOrder();
}
async function confirmOrder() {
  const o = activeOrder.value;
  confirmHint.value = "";

  if (!o || submitting.value) return;

  clearTimeout(_syncDraftT);
  _syncDraftT = null;
  submitting.value = true;

  try {
    const err = validateCheckout(o);
    if (err) {
      toastShow(err, err.includes("chưa đủ") ? "warning" : "danger");
      return;
    }

  const ok = await runVoucherPrecheckFlow();
if (!ok) return;

const errAfterVoucher = validateCheckout(o);
if (errAfterVoucher) {
  toastShow(
    errAfterVoucher,
    errAfterVoucher.includes("chưa đủ") ? "warning" : "danger",
  );
  return;
}

if (!o?.dbId) {
  toastShow("Hóa đơn chưa được tạo", "danger");
  return;
}

    const payload = buildPosPayload(o);

    const res = await http.post(
      `/api/hoa-don/draft/${o.dbId}/checkout`,
      payload,
    );

    const paidInvoice = res?.data || {};

    if (o.paymentMethod === "QR") {
      closeQrPay();
    }

    toastShow(`Thanh toán thành công: ${o.maHoaDon}`, "success");

    await resetOrderAfterPaid(o);

    const invoiceId =
      paidInvoice.id ?? paidInvoice.hoaDonId ?? paidInvoice.idHoaDon ?? o.dbId;

    if (invoiceId) {
      router.push({
        name: "order-detail",
        params: { id: String(invoiceId) },
      });
    } else {
      router.push({ name: "orders" });
    }
  } catch (e) {
    console.error(e);

    const msg =
      e?.response?.data?.message ||
      e?.response?.data?.error ||
      e?.response?.data ||
      e?.message ||
      "Thanh toán thất bại";

    toastShow(String(msg), "danger");
  } finally {
    submitting.value = false;
  }
}
function buildPosPayload(o) {
  const isShip = !!o.loaiDon;

  return {
    maHoaDon: o.maHoaDon,
    loaiDon: isShip,
    phiVanChuyen: isShip ? Number(o.phiVanChuyen || 0) : 0,

    idKhachHang: o.customer?.id ?? null,
    tenKhachHang: o.customer?.name || "Khách lẻ",
    soDienThoai: (o.customerDraft?.phone || "").trim(),
    emailKhachHang: (o.customerDraft?.email || "").trim(),
    diaChiKhachHang: (o.diaChi || "").trim(),

    idPhieuGiamGia: o.pggId ?? null,
    giamThuCongPercent: Number(o.discountPercent || 0),

    paid: Number(o.paid || 0),
    ghiChu: (o.ghiChu || "POS checkout").trim(),

    maGiaoDich: o.paymentMethod === "QR" ? o.maGiaoDich || null : null,
    ghiChuThanhToan:
      o.paymentMethod === "QR" ? o.ghiChuThanhToan || null : null,

    tenNguoiNhanHang: isShip ? (o.tenNguoiNhanHang || "").trim() : null,
    soDienThoaiNhanHang: isShip ? (o.soDienThoaiNhanHang || "").trim() : null,
    emailNguoiNhanHang: isShip ? (o.emailNguoiNhanHang || "").trim() : null,
    tinhThanhNhanHang: isShip ? provinceNameById(o.ghnProvinceId) : null,
    quanHuyenNhanHang: null,
    phuongXaNhanHang: isShip ? wardNameByCode(o.ghnWardCode) : null,
    diaChiNhanHangChiTiet: isShip
      ? (o.diaChiNhanHangChiTiet || "").trim()
      : null,

    items: o.cart.map((it) => ({
      idSanPhamChiTiet: Number(it.idSpct),
      soLuong: clampInt(it.qty, 1, 999999),
    })),
  };
}

function buildSyncPayload(o) {
  return {
    loaiDon: !!o.loaiDon,
    phiVanChuyen: Number(o.phiVanChuyen || 0),

    idKhachHang: o.customer?.id ?? null,
    tenKhachHang: o.customer?.name || "Khách lẻ",
    soDienThoai: (o.customerDraft?.phone || "").trim(),
    emailKhachHang: (o.customerDraft?.email || "").trim(),
    diaChiKhachHang: (o.diaChi || "").trim(),

    idPhieuGiamGia: o.pggId ?? null,
    giamThuCongPercent: Number(o.discountPercent || 0),
    ghiChu: (o.ghiChu || "POS draft sync").trim(),

    tenNguoiNhanHang: (o.tenNguoiNhanHang || "").trim(),
    soDienThoaiNhanHang: (o.soDienThoaiNhanHang || "").trim(),
    emailNguoiNhanHang: (o.emailNguoiNhanHang || "").trim(),
    tinhThanhNhanHang: o.ghnProvinceId ? provinceNameById(o.ghnProvinceId) : "",
    quanHuyenNhanHang: "",
    phuongXaNhanHang: o.ghnWardCode ? wardNameByCode(o.ghnWardCode) : "",
    diaChiNhanHangChiTiet: (o.diaChiNhanHangChiTiet || "").trim(),

    items: (o.cart || []).map((it) => ({
      idSanPhamChiTiet: Number(it.idSpct),
      soLuong: Number(it.qty || 0),
    })),
  };
}

let _syncDraftT = null;
function scheduleSyncDraft(order = activeOrder.value) {
  if (submitting.value) return;

  clearTimeout(_syncDraftT);
  _syncDraftT = setTimeout(async () => {
    if (submitting.value) return;
    if (!order?.dbId) return;

    try {
      await http.post(
        `/api/hoa-don/draft/${order.dbId}/sync-pos`,
        buildSyncPayload(order),
      );
    } catch (e) {
      console.error("sync draft error", e);
    }
  }, 300);
}

async function resetOrderAfterPaid(o) {
  clearTimeout(_syncDraftT);
  _syncDraftT = null;

  const idx = orders.value.findIndex((x) => x.id === o.id);
  if (idx !== -1) orders.value.splice(idx, 1);

  if (activeId.value === o.id) {
    activeId.value = orders.value[0]?.id ?? null;
  }

  saveDraftsNow();
}

function onKeydown(e) {
  if (e.key !== "Escape") return;
  if (showPaymentConfirmModal.value) return resolvePaymentConfirm(false);
  if (showPreCheckoutModal.value) return resolvePreCheckout(false);
  if (showScanQrModal.value) return closeScanQrModal();
  if (backdropOpen.value) closeBackdropModal();
}

let removeTabSyncListener = null;
let _refreshVisibleT = null;
let voucherRevalidateT = null;

function queueRefreshWhenVisible() {
  clearTimeout(_refreshVisibleT);
  _refreshVisibleT = setTimeout(() => refreshWhenVisible(), 150);
}

function handleDocumentVisibility() {
  if (document.visibilityState === "hidden") {
    saveDraftsNow();
    return;
  }
  if (document.visibilityState === "visible") {
    queueRefreshWhenVisible();
  }
}

async function refreshProductsInCart() {
  await fetchAllProducts();

  for (const order of orders.value) {
    if (!order || !Array.isArray(order.cart)) continue;

    for (const item of order.cart) {
      const latest =
        allProducts.value.find(
          (p) => Number(p.idSpct) === Number(item.idSpct),
        ) || null;

      const repriced = findRepricedProductInList(item, allProducts.value);

      if (!latest && !repriced) {
        item.stockBase = 0;
        item.stock = 0;
        item.priceChangedLocked = true;
        item.newPrice = null;
        item.lineStatus = "missing";
        continue;
      }

      item.lineStatus = "ok";
      const latestStock = Number((latest?.stock ?? repriced?.stock) || 0);

      // stockBase là "tồn trước khi giữ chỗ của dòng hiện tại"
      // nên phải cộng lại số lượng item hiện có trong giỏ
      item.stockBase = latestStock + Number(item.qty || 0);

      const sameVariantPriceChanged =
        !!latest && !sameMoney(latest.price, item.price);
      const repricedToOtherLine =
        !!repriced &&
        (!latest || Number(repriced.idSpct) !== Number(item.idSpct));

      if (sameVariantPriceChanged || repricedToOtherLine) {
        item.priceChangedLocked = true;
        item.stock = 0;
        item.newPrice = Number((latest?.price ?? repriced?.price) || 0);
      } else {
        item.priceChangedLocked = false;
        item.newPrice = null;
      }
    }

    syncAllCartStocks(order);
  }
}

async function processVoucherChangesForAllOrders() {
  for (const order of orders.value) {
    if (!order || order.id === activeId.value) continue;
    if (!order?.pggId && !order?.voucherSnapshot) continue;

    const orderVouchers = await loadVouchersByCustomer(
      order?.customer?.id ?? null,
    );
    const result = evaluateVoucherState(order, orderVouchers);

    if (result.status === "best_voucher_ready") {
      if (result.best?.voucher) {
        applyBestVoucherNowForOrder(order, orderVouchers, "best");
      }
      continue;
    }

    if (result.status === "voucher_missing") {
      if (result.best?.voucher) {
        order.voucherMode = "best";
        order.pggId = result.best.voucher.id;
        order.voucherCode = result.best.voucher.ma_giam_gia || "";
        order.discountPercent = 0;
        setVoucherSnapshot(order, result.best.voucher);
      } else {
        removeVoucherNowForOrder(order);
      }
      continue;
    }

    if (result.status === "voucher_invalid") {
      const invalid = result.invalidReason || {};
      if (invalid.code === "MIN_ORDER_NOT_MET") {
        keepVoucherWaitingForBest(order);
      } else if (result.best?.voucher) {
        order.voucherMode = "best";
        order.pggId = result.best.voucher.id;
        order.voucherCode = result.best.voucher.ma_giam_gia || "";
        order.discountPercent = 0;
        setVoucherSnapshot(order, result.best.voucher);
      } else {
        removeVoucherNowForOrder(order);
      }
      continue;
    }

    if (result.status === "voucher_changed") {
      if (result.currentVoucher)
        setVoucherSnapshot(order, result.currentVoucher);
      continue;
    }

    if (result.status === "valid" && result.currentVoucher) {
      setVoucherSnapshot(order, result.currentVoucher);
    }
  }
}

async function refreshWhenVisible() {
  await processVoucherChangesForAllOrders();
  await refreshProductsInCart();
  await revalidateActiveOrderVoucher({ showModal: true });
}

async function handleTabSync(event) {
  const msg = event?.data;
  if (!msg?.type) return;

  if (msg.type === TAB_SYNC_EVENTS.VOUCHER_CHANGED) {
    await processVoucherChangesForAllOrders();
    await revalidateActiveOrderVoucher({ showModal: true });
    return;
  }

  if (
    msg.type === TAB_SYNC_EVENTS.PRODUCT_CHANGED ||
    msg.type === TAB_SYNC_EVENTS.PRODUCT_STOCK_CHANGED
  ) {
    await refreshProductsInCart();
    await revalidateActiveOrderVoucher({ showModal: true });
  }
}

watch(
  () => [activeId.value, subTotal.value],
  () => {
    clearTimeout(voucherRevalidateT);
    voucherRevalidateT = setTimeout(() => {
      revalidateActiveOrderVoucher({ showModal: true });
    }, 250);
  },
);

watch(
  activeOrder,
  (order) => {
    if (submitting.value) return;
    if (!order?.dbId) return;
    scheduleSyncDraft(order);
  },
  { deep: true },
);

onMounted(async () => {
  loadDrafts();
  scheduleMidnightReset();
  await loadGhnProvinces();
  await reloadCustomers();
  await fetchAllProducts();
  await reloadActiveOrderVouchers({ showModal: true });

  window.addEventListener("beforeunload", saveDraftsNow);
  window.addEventListener("keydown", onKeydown);
  window.addEventListener("focus", queueRefreshWhenVisible);
  document.addEventListener("visibilitychange", handleDocumentVisibility);

  removeTabSyncListener = onTabSync(handleTabSync);
});

onBeforeUnmount(() => {
  try {
    void stopProductQr();
  } catch {}

  try {
    stopPosQrPolling();
  } catch {}

  try {
    window.removeEventListener("beforeunload", saveDraftsNow);
    window.removeEventListener("keydown", onKeydown);
    window.removeEventListener("focus", queueRefreshWhenVisible);
    document.removeEventListener("visibilitychange", handleDocumentVisibility);
  } catch {}

  try {
    if (midnightTimer) clearTimeout(midnightTimer);
    if (_saveT) clearTimeout(_saveT);
    if (_syncDraftT) clearTimeout(_syncDraftT);
    if (removeTabSyncListener) removeTabSyncListener();
  } catch {}

  try {
    document.body.classList.remove("modal-open");
    document.querySelectorAll(".modal-backdrop").forEach((b) => b.remove());
  } catch {}

  try {
    saveDraftsNow();
  } catch {}
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

.customer-summary-box {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 14px;
  background: #fff;
}

.summary-line + .summary-line {
  margin-top: 10px;
}

.summary-label {
  display: block;
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 4px;
}

.summary-value {
  color: #111827;
  line-height: 1.45;
}

.address-card {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 12px;
  background: #fff;
}

.address-card-active {
  border-color: #198754;
  box-shadow: 0 0 0 1px rgba(25, 135, 84, 0.15);
}

:global(.modal-backdrop) {
  z-index: 1050;
}
:global(.modal) {
  z-index: 1055;
}
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

.address-new-box {
  border: 1px dashed #cbd5e1;
  border-radius: 12px;
  padding: 14px;
  background: #f8fafc;
}

.ship-fee-logo {
  height: 18px;
  width: auto;
  object-fit: contain;
}
.voucher-notify-modal {
  border-radius: 18px;
  overflow: hidden;
}

.voucher-notify-header {
  padding: 18px 20px 14px;
  border-bottom: 1px solid #f1f5f9;
  background: #fff;
}

.voucher-notify-banner {
  border-radius: 12px;
  padding: 14px 16px;
  font-size: 14px;
  line-height: 1.5;
  font-weight: 500;
}

.voucher-notify-banner-danger {
  background: #fff1f2;
  border: 1px solid #fecdd3;
  color: #be123c;
}

.voucher-notify-banner-info {
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  color: #1d4ed8;
}

.voucher-notify-banner-success {
  background: #ecfdf5;
  border: 1px solid #bbf7d0;
  color: #15803d;
}

.voucher-notify-table {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  overflow: hidden;
  background: #fff;
}

.voucher-notify-row {
  display: grid;
  grid-template-columns: 180px 1fr;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid #f1f5f9;
}

.voucher-notify-row:last-child {
  border-bottom: 0;
}

.voucher-notify-label {
  color: #6b7280;
  font-size: 14px;
}

.voucher-notify-value {
  color: #111827;
  font-weight: 600;
  font-size: 14px;
  text-align: right;
}

.voucher-notify-footer {
  border-top: 0;
  padding: 8px 20px 20px;
}

.voucher-notify-ok-btn {
  min-width: 110px;
  border-radius: 12px;
  padding: 10px 18px;
}

@media (max-width: 576px) {
  .voucher-notify-row {
    grid-template-columns: 1fr;
  }

  .voucher-notify-value {
    text-align: left;
  }
}
.voucher-notify-cancel-btn {
  min-width: 120px;
  border-radius: 12px;
  padding: 10px 18px;
}

.voucher-notify-ok-btn {
  min-width: 120px;
  border-radius: 12px;
  padding: 10px 18px;
}
:deep(.multiselect) {
  --ms-ring-color: rgba(13, 110, 253, 0.2);
  --ms-border-color: #ced4da;
  --ms-placeholder-color: #6c757d;
  --ms-bg: #fff;
  --ms-tag-bg: #0d6efd;
  --ms-tag-color: #fff;
  --ms-option-bg-pointed: #e7f1ff;
  --ms-option-color-pointed: #0a58ca;
  --ms-option-bg-selected: #0d6efd;
  --ms-option-color-selected: #fff;
  --ms-spinner-color: #0d6efd;
}

:deep(.multiselect.is-active) {
  border-color: #86b7fe;
  box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);
}

:deep(.multiselect-option.is-pointed) {
  background: #e7f1ff !important;
  color: #0a58ca !important;
}

:deep(.multiselect-option.is-selected) {
  background: #0d6efd !important;
  color: #fff !important;
}

:deep(.multiselect-tag) {
  background: #0d6efd !important;
  color: #fff !important;
}

:deep(.multiselect-caret),
:deep(.multiselect-clear) {
  color: #6c757d;
}

:deep(.multiselect-placeholder) {
  color: #6c757d;
}
.qr-pay-modal {
  border: 0;
  border-radius: 22px;
  overflow: hidden;
  box-shadow:
    0 24px 60px rgba(15, 23, 42, 0.18),
    0 10px 24px rgba(15, 23, 42, 0.1);
  background: linear-gradient(180deg, #ffffff 0%, #fbfdff 100%);
}

.qr-pay-header {
  padding: 18px 20px 14px;
  border-bottom: 1px solid #eef2f7;
  background:
    radial-gradient(
      circle at top left,
      rgba(37, 99, 235, 0.12),
      transparent 34%
    ),
    radial-gradient(
      circle at top right,
      rgba(34, 197, 94, 0.12),
      transparent 30%
    ),
    #ffffff;
}

.qr-pay-eyebrow {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #2563eb;
  margin-bottom: 4px;
}

.qr-pay-subtitle {
  font-size: 13px;
  color: #6b7280;
}

.qr-pay-body {
  padding: 18px 20px 16px;
}

.qr-pay-top {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 16px;
}

.qr-pay-meta-card,
.qr-pay-amount-card {
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 12px 14px;
  background: #fff;
}

.qr-pay-meta-label {
  font-size: 15px;
  color: #6b7280;
  margin-bottom: 4px;
}

.qr-pay-meta-value {
  font-size: 14px;
  font-weight: 700;
  color: #111827;
  word-break: break-word;
}

.qr-pay-amount {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
  color: #dc2626;
}

.qr-stage-glow {
  position: absolute;
  inset: auto;
  top: -50px;
  left: 50%;
  transform: translateX(-50%);
  width: 220px;
  height: 220px;
  border-radius: 999px;
  background: radial-gradient(
    circle,
    rgba(59, 130, 246, 0.18),
    transparent 70%
  );
  pointer-events: none;
}

.qr-stage-caption {
  position: relative;
  z-index: 1;
}

.qr-stage-title {
  font-size: 15px;
  font-weight: 700;
  color: #111827;
}

.qr-stage-note {
  font-size: 13px;
  color: #6b7280;
  margin-top: 4px;
}

.qr-pay-info-card {
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  background: #ffffff;
  overflow: hidden;
}

.qr-pay-info-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  padding: 12px 14px;
}

.qr-pay-info-row-top {
  border-top: 1px solid #f1f5f9;
}

.qr-pay-info-value {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
  text-align: right;
}

.qr-pay-code {
  word-break: break-all;
}

.qr-pay-input-label {
  font-size: 15px;
  font-weight: 700;
  color: #374151;
}

.qr-pay-input {
  min-height: 44px;
  border-radius: 14px;
  border-color: #dbe3ee;
  box-shadow: none;
}

.qr-pay-input:focus {
  border-color: #86b7fe;
  box-shadow: 0 0 0 0.2rem rgba(13, 110, 253, 0.12);
}

.qr-pay-footer {
  border-top: 1px solid #eef2f7;
  padding: 14px 20px 20px;
  display: grid;
  grid-template-columns: 1fr 1.3fr 1.3fr;
  gap: 10px;
}

.qr-btn {
  min-height: 46px;
  border-radius: 14px;
  font-weight: 700;
  border: 0;
}

.qr-btn-secondary {
  background: #f3f4f6;
  color: #374151;
}

.qr-btn-secondary:hover {
  background: #e5e7eb;
  color: #111827;
}

.qr-btn-primary {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  color: #fff;
  box-shadow: 0 10px 20px rgba(37, 99, 235, 0.22);
}

.qr-btn-primary:hover {
  color: #fff;
  transform: translateY(-1px);
}

.qr-btn-success {
  background: linear-gradient(135deg, #15803d 0%, #22c55e 100%);
  color: #fff;
  box-shadow: 0 10px 20px rgba(34, 197, 94, 0.22);
}

.qr-btn-success:hover {
  color: #fff;
  transform: translateY(-1px);
}

.qr-btn:disabled {
  opacity: 0.7;
  transform: none !important;
  box-shadow: none;
}

@media (max-width: 576px) {
  .qr-pay-top {
    grid-template-columns: 1fr;
  }

  .qr-pay-footer {
    grid-template-columns: 1fr;
  }

  .qr-pay-info-row {
    flex-direction: column;
  }

  .qr-pay-info-value {
    text-align: left;
  }
}

.qr-scan-modal {
  position: fixed;
  inset: 0;
  z-index: 1062;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.qr-scan-dialog {
  width: min(720px, 100%);
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.18);
}

.qr-scan-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px;
  border-bottom: 1px solid #eef2f7;
}

.qr-scan-body {
  padding: 16px 18px 18px;
}

.qr-reader-box {
  width: 100%;
  min-height: 280px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  background: #000;
}
.qr-pay-dialog {
  max-width: 720px;
  width: min(720px, calc(100vw - 32px));
  margin: 1rem auto;
}

.qr-pay-modal {
  border: 0;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 18px 50px rgba(0, 0, 0, 0.18);
}

.qr-pay-header {
  padding: 16px 18px 12px;
  border-bottom: 1px solid #e9ecef;
  background: linear-gradient(90deg, #ffffff 0%, #f3fff8 100%);
}

.qr-pay-subtitle {
  font-size: 13px;
  color: #6c757d;
}

.qr-pay-body {
  padding: 16px 18px;
}

.qr-pay-top {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 14px;
}

.qr-pay-meta-card,
.qr-pay-amount-card {
  border: 1px solid #dde3ea;
  border-radius: 14px;
  padding: 12px 14px;
  background: #fff;
  min-width: 0;
}

.qr-pay-meta-label {
  font-size: 15px;
  color: #7b8794;
  margin-bottom: 6px;
}

.qr-pay-meta-value {
  font-size: 17px;
  font-weight: 700;
  color: #1f2937;
  word-break: break-all;
}

.qr-pay-amount {
  font-size: 27px;
  line-height: 1.1;
  font-weight: 700;
  color: #e03131;
  word-break: break-word;
}

.qr-stage-glow {
  position: absolute;
  inset: 0;
  pointer-events: none;
  border-radius: inherit;
  background: radial-gradient(
    circle at top,
    rgba(34, 197, 94, 0.08),
    transparent 60%
  );
}

.qr-stage-caption {
  margin-top: 10px;
}

.qr-stage-title {
  font-size: 15px;
  font-weight: 700;
  color: #111827;
}

.qr-pay-info-card {
  border: 1px solid #e3e8ef;
  border-radius: 14px;
  padding: 12px 14px;
  background: #fff;
}

.qr-pay-info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.qr-pay-info-label {
  font-size: 16px;
  color: #6b7280;
  flex: 0 0 auto;
}

.qr-pay-info-value {
  font-size: 17px;
  font-weight: 700;
  color: #1f2937;
  text-align: right;
}

.qr-pay-code {
  word-break: break-all;
}

.qr-pay-input-label {
  font-weight: 600;
  color: #374151;
}

.qr-pay-input {
  min-height: 44px;
  border-radius: 12px;
}

.qr-pay-footer {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: space-between;
  padding: 14px 18px 18px;
  border-top: 0;
  background: #fff;
}

.qr-btn {
  min-height: 46px;
  padding: 10px 18px;
  border-radius: 12px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  white-space: normal;
  text-align: center;
  line-height: 1.25;
  flex: 1 1 180px;
}

.qr-btn-primary {
  background: #2f6fed;
  border-color: #2f6fed;
  color: #fff;
}

.qr-btn-primary:hover {
  background: #255ed0;
  border-color: #255ed0;
  color: #fff;
}

.qr-btn-success {
  background: #1db954;
  border-color: #1db954;
  color: #fff;
}

.qr-btn-success:hover {
  background: #19a14a;
  border-color: #19a14a;
  color: #fff;
}

.qr-btn-secondary {
  background: #f3f4f6;
  border-color: #f3f4f6;
  color: #374151;
}

.qr-btn-secondary:hover {
  background: #e5e7eb;
  border-color: #e5e7eb;
  color: #1f2937;
}

.payment-confirm-dialog {
  max-width: 420px;
  width: min(420px, calc(100vw - 32px));
  margin: 1rem auto;
}

.payment-confirm-modal {
  border: 0;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 18px 50px rgba(0, 0, 0, 0.22);
}

.payment-confirm-text {
  font-size: 15px;
  line-height: 1.5;
  color: #1f2937;
}

.payment-confirm-footer {
  gap: 10px;
}

.payment-confirm-cancel-btn,
.payment-confirm-ok-btn {
  min-width: 96px;
  min-height: 40px;
  border-radius: 10px;
  font-weight: 600;
}

@media (max-width: 768px) {
  .qr-pay-dialog {
    max-width: calc(100vw - 20px);
    width: calc(100vw - 20px);
  }

  .qr-pay-top {
    grid-template-columns: 1fr;
  }

  .qr-pay-amount {
    font-size: 26px;
  }

  .qr-pay-footer {
    flex-direction: column;
  }

  .qr-btn {
    width: 100%;
    flex: 1 1 100%;
  }

  .payment-confirm-dialog {
    max-width: calc(100vw - 20px);
    width: calc(100vw - 20px);
  }
}

.qr-stage {
  position: relative;
  border-radius: 18px;
  border: 1px solid #d9e2e7;
  background: #f8fafb;
  padding: 16px;
}

.qr-frame {
  width: 100%;
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qr-image {
  width: 320px;
  height: 320px;
  max-width: 100%;
  object-fit: contain;
  display: block;
  margin: 0 auto;
  
  border-radius: 14px;
  background: #fff;
  padding: 0;
  border: 1px solid #e5e7eb;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}
.customer-pick-btn:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.customer-table tbody tr:has(.customer-pick-btn:disabled) {
  background: #f8fafc;
}

.customer-table tbody tr:has(.customer-pick-btn:disabled) td {
  color: #6b7280;
}
</style>
