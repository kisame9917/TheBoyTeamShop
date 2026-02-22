<template>
  <div class="container-fluid py-3">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-3">
      <h4 class="m-0 fw-bold">Bán hàng (tại cửa hàng)</h4>

      <button
        class="btn btn-outline-secondary btn-sm"
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
        <div class="d-flex align-items-center justify-content-between gap-2 flex-wrap">
          <div class="d-flex flex-wrap gap-2">
            <button
              v-for="o in orders"
              :key="o.id"
              type="button"
              class="btn btn-sm"
              :class="o.id === activeId ? 'btn-dark' : 'btn-outline-dark'"
              @click="activeId = o.id"
            >
              {{ o.label }}
              <span class="ms-2 tab-x" title="Đóng" @click.stop="closeOrder(o.id)">×</span>
            </button>
          </div>

          <div class="text-muted small">{{ orders.length }}/{{ MAX_ORDERS }} đơn</div>
        </div>
      </div>

      <div class="card-body">
        <div v-if="!activeOrder" class="text-center py-5 text-muted">
          <div class="fs-1">👜</div>
          <div class="fw-semibold">No Data Found</div>
        </div>

        <div v-else>
          <!-- Products actions -->
          <div class="d-flex align-items-center justify-content-between flex-wrap gap-2 mb-2">
            <div class="fw-bold">Sản phẩm</div>
            <div class="d-flex gap-2">
              <button class="btn btn-outline-secondary btn-sm" type="button" @click="openProductModal">
                <i class="bi bi-plus-lg me-1"></i> Thêm sản phẩm
              </button>
              <button class="btn btn-outline-secondary btn-sm" type="button" @click="toastInfo('Chưa tích hợp quét QR')">
                <i class="bi bi-qr-code-scan me-1"></i> Quét QR
              </button>
            </div>
          </div>

          <!-- Cart -->
          <div class="border rounded-3 p-2 mb-3">
            <div v-if="activeOrder.cart.length === 0" class="text-center py-4 text-muted">
              <div class="fs-1">👜</div>
              <div class="fw-semibold">No Data Found</div>
            </div>

            <div v-else class="table-responsive">
              <table class="table align-middle mb-0">
                <thead class="table-light">
                  <tr>
                    <th style="width:70px">Ảnh</th>
                    <th>Sản phẩm</th>
                    <th class="text-end" style="width:140px">Đơn giá</th>
                    <th class="text-center" style="width:170px">Số lượng</th>
                    <th class="text-end" style="width:160px">Thành tiền</th>
                    <th class="text-center" style="width:80px">Xóa</th>
                  </tr>
                </thead>

                <tbody>
                  <tr v-for="(it, idx) in activeOrder.cart" :key="it.key">
                    <td>
                      <img :src="it.image || placeholderImg" class="rounded" style="width:56px;height:56px;object-fit:cover" />
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
                        <button class="btn btn-outline-secondary btn-sm" @click="decQty(idx)">-</button>

                        <input
                          class="form-control form-control-sm text-center"
                          style="width:60px"
                          :value="it.qty"
                          inputmode="numeric"
                          @input="onQtyInput(idx, $event)"
                          @blur="onQtyBlur(idx, $event)"
                        />

                        <button class="btn btn-outline-secondary btn-sm" @click="incQty(idx)">+</button>
                      </div>
                    </td>

                    <td class="text-end fw-bold">{{ money(it.price * it.qty) }}</td>

                    <td class="text-center">
                      <button class="btn btn-outline-danger btn-sm" @click="removeItem(idx)">🗑</button>
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
                <div class="card-header bg-white d-flex align-items-center justify-content-between">
                  <div>
                    <div class="fw-bold">Thông tin khách hàng</div>
                    <div class="text-muted small">
                      Mã HĐ: <span class="fw-semibold font-monospace">{{ activeOrder.maHoaDon }}</span>
                    </div>
                  </div>

                  <button class="btn btn-outline-dark btn-sm" type="button" @click="openCustomerModal">
                    Chọn khách hàng
                  </button>
                </div>

                <div class="card-body">
                  <div class="mb-2">
                    <span class="text-muted">Tên khách hàng: </span>
                    <span class="fw-bold">{{ activeOrder.customer?.name || "Khách lẻ" }}</span>
                  </div>

                  <div class="row g-2">
                    <div class="col-12 col-md-6">
                      <input class="form-control" placeholder="Số điện thoại" v-model="activeOrder.customerDraft.phone" />
                    </div>
                    <div class="col-12 col-md-6">
                      <input class="form-control" placeholder="Email (nếu có)" v-model="activeOrder.customerDraft.email" />
                    </div>

                    <div class="col-12">
                      <textarea class="form-control" rows="2" placeholder="Địa chỉ (nếu có)" v-model="activeOrder.diaChi"></textarea>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Payment -->
            <div class="col-12 col-lg-6">
              <div class="card h-100">
                <div class="card-header bg-white d-flex align-items-center justify-content-between">
                  <div class="fw-bold">Thanh toán</div>
                  <div class="small text-muted">Chỉ tại cửa hàng</div>
                </div>

                <div class="card-body">
                  <!-- =======================
                       PHIẾU GIẢM GIÁ
                       ======================= -->
                  <div class="mb-3">
                    <div class="d-flex align-items-center justify-content-between flex-wrap gap-2">
                      <label class="form-label fw-bold m-0">
                        Mã giảm giá <span class="text-danger">*</span>
                      </label>

                      <div class="d-flex gap-2">
                        <button class="btn btn-outline-secondary btn-sm" type="button" @click="loadVouchers">
                          Tải lại
                        </button>
                        <button class="btn btn-outline-danger btn-sm" type="button" @click="disableVoucher">
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
                        <div v-if="activeOrder.voucherMode === 'none'" class="text-muted small py-2">
                          Bạn đang chọn: <b>Không dùng mã</b>
                        </div>

                        <div v-else-if="bestVoucherEntry" class="voucher-card">
                          <div class="d-flex align-items-start justify-content-between gap-2">
                            <div>
                              <div class="d-flex gap-2 flex-wrap">
                                <span class="badge text-bg-primary">PGG</span>
                                <span class="badge text-bg-secondary">Mã tốt nhất</span>
                                <span class="badge text-bg-dark">{{ bestVoucherEntry.v.ma_giam_gia }}</span>
                              </div>

                              <div class="mt-2 fw-bold">{{ bestVoucherEntry.v.ten_giam_gia }}</div>

                              <div class="mt-2">
                                <span class="text-muted">Giảm:</span>
                                <span class="ms-2 fw-bold text-danger">- {{ money(bestVoucherEntry.discount) }}</span>
                              </div>

                              <div class="small text-muted mt-1">
                                <span v-if="bestVoucherEntry.v.don_hang_toi_thieu > 0">
                                  Đơn tối thiểu: <b>{{ money(bestVoucherEntry.v.don_hang_toi_thieu) }}</b>
                                </span>
                                <span v-if="bestVoucherEntry.v.gia_tri_giam_toi_da > 0" class="ms-2">
                                  Tối đa: <b>{{ money(bestVoucherEntry.v.gia_tri_giam_toi_da) }}</b>
                                </span>
                              </div>
                            </div>

                            <div class="text-end">
                              <span
                                v-if="activeOrder.voucherMode !== 'none' && activeOrder.pggId === bestVoucherEntry.v.id"
                                class="badge text-bg-success"
                              >
                                Đang áp dụng
                              </span>

                              <button
                                v-else
                                class="btn btn-success btn-sm"
                                type="button"
                                @click="applyVoucherManual(bestVoucherEntry.v)"
                              >
                                Áp dụng
                              </button>
                            </div>
                          </div>

                          <div class="mt-3 small text-muted">
                            <div>
                              <span class="me-2">Hết hạn:</span>
                              <b>{{ formatDateVN(bestVoucherEntry.v.ngay_ket_thuc) }}</b>
                            </div>
                            <div class="mt-1">
                              <span class="me-2">Đơn tối thiểu:</span>
                              <b>{{ money(bestVoucherEntry.v.don_hang_toi_thieu) }}</b>
                            </div>
                          </div>
                        </div>

                        <div v-else class="text-muted small py-2">
                          Không có mã giảm giá phù hợp với đơn hiện tại.
                        </div>
                      </template>

                      <!-- ALT -->
                      <template v-else>
                        <div v-if="altVoucherEntries.length === 0" class="text-muted small py-2">
                          Không có mã thay thế khả dụng.
                        </div>

                        <div v-else class="d-flex flex-column gap-2">
                          <div v-for="e in altVoucherEntries.slice(0, 10)" :key="e.v.id" class="voucher-card">
                            <div class="d-flex align-items-start justify-content-between gap-2">
                              <div>
                                <div class="d-flex gap-2 flex-wrap">
                                  <span class="badge text-bg-primary">PGG</span>
                                  <span class="badge text-bg-secondary">Mã thay thế</span>
                                  <span class="badge text-bg-dark">{{ e.v.ma_giam_gia }}</span>

                                  <span
                                    v-if="activeOrder.pggId === e.v.id && activeOrder.voucherMode !== 'none'"
                                    class="badge text-bg-success"
                                  >
                                    Đang chọn
                                  </span>
                                </div>

                                <div class="mt-2 fw-bold">{{ e.v.ten_giam_gia }}</div>

                                <div class="mt-2">
                                  <span class="text-muted">Giảm:</span>
                                  <span class="ms-2 fw-bold text-danger">- {{ money(e.discount) }}</span>
                                </div>

                                <div class="small text-muted mt-1">
                                  <span v-if="e.v.don_hang_toi_thieu > 0">
                                    Đơn tối thiểu: <b>{{ money(e.v.don_hang_toi_thieu) }}</b>
                                  </span>
                                  <span v-if="e.v.gia_tri_giam_toi_da > 0" class="ms-2">
                                    Tối đa: <b>{{ money(e.v.gia_tri_giam_toi_da) }}</b>
                                  </span>
                                </div>
                              </div>

                              <div class="text-end">
                                <button class="btn btn-outline-primary btn-sm" type="button" @click="applyVoucherManual(e.v)">
                                  Chọn
                                </button>
                              </div>
                            </div>
                          </div>

                          <div class="mt-2">
                            <div class="input-group">
                              <input
                                class="form-control"
                                placeholder="Nhập mã PGG (tùy chọn)"
                                v-model.trim="activeOrder.voucherCode"
                                @keyup.enter="applyPggByCode"
                                @blur="applyPggByCode"
                              />
                              <button class="btn btn-outline-secondary" type="button" @click="clearVoucherManual">
                                Xóa
                              </button>
                            </div>
                          </div>
                        </div>
                      </template>
                    </div>

                    <div v-if="voucherUpsellHint" class="small text-muted mt-2">
                      Thêm <b class="text-danger">{{ money(voucherUpsellHint.missing) }}</b>
                      để dùng mã giảm giá tốt hơn:
                      <b>{{ voucherUpsellHint.code }}</b>
                      (giảm khoảng <b class="text-danger">-{{ money(voucherUpsellHint.expectedDiscount) }}</b>
                      khi đơn ≥ {{ money(voucherUpsellHint.minOrder) }})
                    </div>

                    <div class="mt-3">
                      <div class="fw-bold">Gợi ý mã giảm giá <span class="text-danger">*</span></div>

                      <div v-if="eligibleVoucherEntries.length === 0" class="text-muted small mt-1">
                        Không có mã giảm giá gợi ý khả dụng
                      </div>

                      <div v-else class="d-flex flex-wrap gap-2 mt-2">
                        <button
                          v-for="e in eligibleVoucherEntries.slice(0, 6)"
                          :key="'chip-' + e.v.id"
                          class="btn btn-outline-secondary btn-sm"
                          type="button"
                          @click="applyVoucherManual(e.v)"
                        >
                          {{ e.v.ma_giam_gia }} • -{{ money(e.discount) }}
                        </button>
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
                      :placeholder="activeOrder.pggId ? 'Đang chọn PGG, % bị khóa' : 'Nhập % giảm (tùy chọn)'"
                    />
                  </div>

                  <ul class="list-group mb-3">
                    <li class="list-group-item d-flex justify-content-between">
                      <span class="text-muted">Tiền hàng</span>
                      <span class="fw-semibold">{{ money(subTotal) }}</span>
                    </li>

                    <li class="list-group-item d-flex justify-content-between">
                      <span class="text-muted">Giảm giá</span>
                      <span class="fw-semibold text-danger">- {{ money(discountMoney) }}</span>
                    </li>

                    <li class="list-group-item d-flex justify-content-between">
                      <span class="fw-bold">Tổng phải trả</span>
                      <span class="fw-bold text-danger">{{ money(grandTotal) }}</span>
                    </li>

                    <li class="list-group-item d-flex justify-content-between align-items-center">
                      <span class="text-muted">Khách thanh toán</span>
                      <input
                        class="form-control form-control-sm text-end"
                        style="max-width:180px"
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
                    <button class="btn btn-success w-100" :disabled="activeOrder.cart.length === 0 || submitting" @click="confirmOrder">
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

                  <div v-if="confirmHint" class="small text-muted mt-2">{{ confirmHint }}</div>
                </div>
              </div>
            </div>
          </div>
          <!-- /row -->
        </div>
      </div>
    </div>

    <!-- Toast -->
    <div v-if="toast.show" class="position-fixed bottom-0 end-0 p-3" style="z-index: 2000">
      <div class="toast show align-items-center text-white border-0" :class="toastClass">
        <div class="d-flex">
          <div class="toast-body fw-semibold">{{ toast.msg }}</div>
          <button type="button" class="btn-close btn-close-white me-2 m-auto" @click="toast.show=false"></button>
        </div>
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
        style="display:block; z-index:1055"
      >
        <div class="modal-dialog modal-xl modal-dialog-scrollable">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title fw-bold">Chọn biến thể để thêm vào đơn</h5>
              <button type="button" class="btn-close" @click="closeProductModal"></button>
            </div>

            <div class="modal-body">
              <div class="d-flex gap-2 mb-3 flex-wrap">
                <input class="form-control" placeholder="Tìm kiếm..." v-model.trim="productKw" style="max-width:420px" />
                <button class="btn btn-outline-secondary" type="button" @click="productKw=''">Đặt lại</button>
                <button class="btn btn-outline-secondary" type="button" @click="reloadProducts" :disabled="productLoading">
                  Tải lại
                </button>
                <div class="ms-auto small text-muted d-flex align-items-center gap-2">
                  <span v-if="productLoading">Đang tải...</span>
                  <span>Đã tải: <b>{{ products.length }}</b></span>
                </div>
              </div>

              <div class="table-responsive">
                <table class="table table-bordered align-middle">
                  <thead class="table-light">
                    <tr>
                      <th style="width:60px">STT</th>
                      <th style="width:130px">Mã</th>
                      <th style="width:80px">Ảnh</th>
                      <th>Tên sản phẩm</th>
                      <th style="width:120px">Màu</th>
                      <th style="width:120px">Size</th>
                      <th style="width:90px" class="text-end">Tồn</th>
                      <th style="width:140px" class="text-end">Giá</th>
                      <th style="width:110px" class="text-center">Chọn</th>
                    </tr>
                  </thead>

                  <tbody>
                    <tr v-for="(p,i) in filteredProducts" :key="p.idSpct">
                      <td>{{ i+1 }}</td>
                      <td class="fw-semibold">{{ p.code }}</td>
                      <td>
                        <img :src="p.image || placeholderImg" class="rounded" style="width:44px;height:44px;object-fit:cover" />
                      </td>
                      <td>{{ p.name }}</td>
                      <td>{{ p.color }}</td>
                      <td>{{ p.size }}</td>
                      <td class="text-end fw-semibold">{{ p.stock }}</td>
                      <td class="text-end fw-semibold">{{ money(p.price) }}</td>
                      <td class="text-center">
                        <button class="btn btn-dark btn-sm" :disabled="p.stock<=0" @click="chooseProduct(p)">Chọn</button>
                      </td>
                    </tr>

                    <tr v-if="filteredProducts.length===0 && !productLoading">
                      <td colspan="9" class="text-center text-muted py-3">Không có dữ liệu</td>
                    </tr>

                    <tr v-if="productHasMore">
                      <td colspan="9" class="text-center py-3">
                        <button class="btn btn-outline-dark btn-sm" @click="loadMoreProducts" :disabled="productLoading">
                          {{ productLoading ? 'Đang tải...' : 'Tải thêm' }}
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <div class="modal-footer">
              <button class="btn btn-outline-secondary" type="button" @click="closeProductModal">Đóng</button>
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
        style="display:block; z-index:1055"
      >
        <div class="modal-dialog modal-lg modal-dialog-scrollable">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title fw-bold">Chọn khách hàng</h5>
              <button type="button" class="btn-close" @click="closeCustomerModal"></button>
            </div>

            <div class="modal-body">
              <div class="d-flex gap-2 mb-3 flex-wrap">
                <input class="form-control" placeholder="Tìm theo tên/SĐT/email/địa chỉ..." v-model.trim="customerKw" />
                <button class="btn btn-outline-secondary" type="button" @click="customerKw=''">Đặt lại</button>
                <button class="btn btn-outline-secondary" type="button" @click="reloadCustomers" :disabled="customerLoading">
                  Tải lại
                </button>
                <div class="ms-auto small text-muted d-flex align-items-center gap-2">
                  <span v-if="customerLoading">Đang tải...</span>
                  <span>Tổng: <b>{{ customers.length }}</b></span>
                </div>
              </div>

              <div class="table-responsive">
                <table class="table table-bordered align-middle">
                  <thead class="table-light">
                    <tr>
                      <th style="width:60px">#</th>
                      <th style="width:240px">Tên khách</th>
                      <th style="width:160px">SĐT</th>
                      <th>Địa chỉ</th>
                      <th style="width:110px" class="text-center">Chọn</th>
                    </tr>
                  </thead>

                  <tbody>
                    <tr v-for="(c,i) in filteredCustomers" :key="c.id || c.phone || i">
                      <td>{{ i+1 }}</td>
                      <td class="fw-semibold">{{ c.name }}</td>
                      <td>{{ c.phone }}</td>
                      <td>{{ c.address }}</td>
                      <td class="text-center">
                        <button class="btn btn-dark btn-sm" @click="chooseCustomer(c)">Chọn</button>
                      </td>
                    </tr>

                    <tr v-if="filteredCustomers.length===0 && !customerLoading">
                      <td colspan="5" class="text-center text-muted py-3">Không có dữ liệu</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <div class="modal-footer">
              <button class="btn btn-outline-secondary" type="button" @click="closeCustomerModal">Đóng</button>
            </div>
          </div>
        </div>
      </div>

      <!-- ✅ QR Pay Modal (ĐẶT ĐỘC LẬP, KHÔNG LỒNG TRONG PRE-CHECKOUT) -->
      <div
        v-if="showQrPayModal"
        class="modal fade show"
        tabindex="-1"
        role="dialog"
        aria-modal="true"
        style="display:block; z-index:1065"
      >
        <div class="modal-dialog modal-md modal-dialog-centered">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title fw-bold">Thanh toán bằng QR</h5>
              <button type="button" class="btn-close" @click="closeQrPay"></button>
            </div>

            <div class="modal-body">
              <div class="small text-muted mb-2">
                Mã HĐ: <b class="font-monospace">{{ activeOrder?.maHoaDon }}</b>
              </div>

              <div class="fw-bold mb-2">
                Số tiền: <span class="text-danger">{{ money(grandTotal) }}</span>
              </div>

              <div class="border rounded-3 p-3 text-center">
                <div v-if="qrImg" class="d-flex justify-content-center">
                  <img :src="qrImg" alt="QR Pay" style="width:260px;height:260px;object-fit:contain" />
                </div>
                <div v-else class="text-muted small py-4">Đang tạo QR...</div>

                <div class="small text-muted mt-2">
                  Nội dung: <b class="font-monospace">{{ qrContent }}</b>
                </div>
              </div>

              <div class="mt-3">
                <label class="form-label mb-1">Ghi chú (tuỳ chọn)</label>
                <input class="form-control" v-model="qrNoteDraft" placeholder="VD: CK QR - HDxxxx" />
              </div>

              <div class="small text-muted mt-2">
                Sau khi khách chuyển khoản xong, bấm <b>Đã nhận tiền → Tạo hóa đơn</b>.
              </div>
            </div>

            <div class="modal-footer">
              <button class="btn btn-outline-secondary" type="button" @click="closeQrPay">Đóng</button>

              <button class="btn btn-success" type="button" @click="markPaidAndCheckout" :disabled="submitting">
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
        style="display:block; z-index:1060"
      >
        <div class="modal-dialog modal-md">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title fw-bold">Xác nhận phiếu giảm giá</h5>
              <button type="button" class="btn-close" @click="resolvePreCheckout(false)"></button>
            </div>

            <div class="modal-body">
              <div class="fw-semibold mb-2" :class="preCheckoutUi.type === 'danger' ? 'text-danger' : ''">
                {{ preCheckoutUi.message }}
              </div>

              <div v-if="preCheckoutUi.detail" class="small text-muted">
                {{ preCheckoutUi.detail }}
              </div>

              <div v-if="preCheckoutUi.suggest" class="mt-2 small">
                Gợi ý:
                <b>{{ preCheckoutUi.suggest.code }}</b>
                • giảm <b class="text-danger">-{{ money(preCheckoutUi.suggest.discount) }}</b>
              </div>
            </div>

            <div class="modal-footer">
              <button class="btn btn-outline-secondary" type="button" @click="resolvePreCheckout(false)">
                Không
              </button>
              <button class="btn btn-success" type="button" @click="resolvePreCheckout(true)">
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
import { computed, ref, reactive, watch, onMounted, onBeforeUnmount } from "vue";
import http from "@/services/http";
import { getAllDetails } from "@/services/sanPhamChiTietApi";
import { listKhachHang } from "@/services/khachHangApi";

/** ========= CONFIG ========= */
const MAX_ORDERS = 10;
const STORAGE_KEY = "sales_store_only_v1";

/** ========= IMG URL ========= */
const apiBaseUrl = import.meta.env.VITE_API_URL || "http://localhost:8080";
const fileBaseUrl = (import.meta.env.VITE_FILE_BASE_URL || apiBaseUrl).replace(/\/api\/?$/, "");
const placeholderImg = "https://via.placeholder.com/56x56.png?text=IMG";
function buildImgUrl(path) {
  if (!path) return "";
  const p = String(path).replace(/\\/g, "/");
  if (p.startsWith("http://") || p.startsWith("https://")) return p;
  const b = String(fileBaseUrl).replace(/\/+$/, "");
  return b + (p.startsWith("/") ? p : `/${p}`);
}

/** ========= TOAST ========= */
const toast = reactive({ show: false, msg: "", type: "danger" });
const toastClass = computed(() => {
  if (toast.type === "success") return "bg-success";
  if (toast.type === "info") return "bg-primary";
  if (toast.type === "warning") return "bg-warning";
  return "bg-danger";
});
function toastShow(msg, type = "danger") {
  toast.show = true;
  toast.msg = msg;
  toast.type = type;
  clearTimeout(toastShow._t);
  toastShow._t = setTimeout(() => (toast.show = false), 2200);
}
const toastInfo = (m) => toastShow(m, "info");

/** ========= ORDERS ========= */
const orders = ref([]);
const activeId = ref(null);
const orderSeq = ref(1);
const submitting = ref(false);
const activeOrder = computed(() => orders.value.find((o) => o.id === activeId.value) || null);

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

function normalizeOrder(o, idx = 1) {
  const ma = o?.maHoaDon || genMaHoaDon();
  return {
    id: o?.id ?? Date.now() + Math.random(),
    maHoaDon: ma,
    label: o?.label || `Hóa Đơn - ${ma}`,
    cart: Array.isArray(o?.cart) ? o.cart : [],
    customer: o?.customer ?? null,
    customerDraft: o?.customerDraft ?? { phone: "", email: "" },

    voucherCode: String(o?.voucherCode || ""),
    pggId: o?.pggId ?? null,
    discountPercent: Number(o?.discountPercent || 0),

    voucherMode: o?.voucherMode ?? "best", // best | manual | none
    voucherTab: o?.voucherTab ?? "best",   // best | alt

    paid: Number(o?.paid || 0),
    diaChi: o?.diaChi || o?.note || "",
  };
}

function createOrder() {
  if (orders.value.length >= MAX_ORDERS) return toastShow(`Chỉ tối đa ${MAX_ORDERS} đơn`, "warning");

  const id = Date.now() + Math.random();
  const maHoaDon = genUniqueMaHoaDon();
  const label = `Hóa Đơn - ${maHoaDon}`;

  orders.value.push(
    normalizeOrder(
      {
        id,
        maHoaDon,
        label,
        cart: [],
        customer: null,
        customerDraft: { phone: "", email: "" },

        voucherCode: "",
        pggId: null,
        discountPercent: 0,
        voucherMode: "best",
        voucherTab: "best",

        paid: 0,
        diaChi: "",
      },
      orderSeq.value
    )
  );

  activeId.value = id;
  orderSeq.value++;
}

function closeOrder(id) {
  const idx = orders.value.findIndex((o) => o.id === id);
  if (idx === -1) return;

  orders.value.splice(idx, 1);
  if (activeId.value === id) activeId.value = orders.value[0]?.id ?? null;

  syncModalStockWithCart();
  saveDraftsNow();
}

/** ========= QR PAY (FE-only) ========= */
const showQrPayModal = ref(false);
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
  showQrPayModal.value = false;

  await confirmOrder();
}

/** ========= DRAFT ========= */
function saveDraftsNow() {
  try {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        orders: orders.value,
        activeId: activeId.value,
        orderSeq: orderSeq.value,
        savedAt: new Date().toISOString(),
      })
    );
  } catch {}
}
let _saveT = null;
function scheduleSave() {
  clearTimeout(_saveT);
  _saveT = setTimeout(saveDraftsNow, 200);
}
function loadDrafts() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return;
    const data = JSON.parse(raw);
    if (!data?.orders) return;

    orders.value = data.orders.map((o, i) => normalizeOrder(o, i + 1));
    activeId.value = data.activeId ?? orders.value[0]?.id ?? null;
    orderSeq.value = Number(data.orderSeq) || orders.value.length + 1;
  } catch {}
}
watch(orders, scheduleSave, { deep: true });
watch(activeId, scheduleSave);
watch(orderSeq, scheduleSave);

/** ========= MONEY ========= */
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

/** ========= Khách thanh toán (format dấu chấm) ========= */
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

/** ========= MODALS ========= */
const showProductModal = ref(false);
const showCustomerModal = ref(false);
const anyModalOpen = computed(() => showProductModal.value || showCustomerModal.value || showQrPayModal.value);

watch(anyModalOpen, (open) => {
  document.body.classList.toggle("modal-open", open);
});
function closeAnyModal() {
  showProductModal.value = false;
  showCustomerModal.value = false;
  showQrPayModal.value = false;
}

/** ========= PRODUCTS (DB) ========= */
const products = ref([]);
const productKw = ref("");
const productLoading = ref(false);
const productPage = ref(0);
const productSize = 100;
const productHasMore = ref(true);

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

function syncModalStockWithCart() {
  const o = activeOrder.value;
  if (!o) return;

  const hold = new Map();
  for (const it of o.cart || []) {
    const id = Number(it.idSpct);
    hold.set(id, (hold.get(id) || 0) + Number(it.qty || 0));
  }

  for (const p of products.value) {
    if (typeof p._baseStock !== "number") p._baseStock = Number(p.stock || 0);
    const h = hold.get(Number(p.idSpct)) || 0;
    p.stock = Math.max(0, Number(p._baseStock || 0) - h);
  }
}

async function loadMoreProducts() {
  if (productLoading.value || !productHasMore.value) return;
  productLoading.value = true;

  try {
    const res = await getAllDetails(productPage.value, productSize);
    const data = res?.data ?? res;
    const content = Array.isArray(data) ? data : data?.content || [];
    const mapped = content.map(mapSpct);

    products.value.push(...mapped);

    if (!Array.isArray(data)) {
      productHasMore.value = data?.last === false && mapped.length > 0;
    } else {
      productHasMore.value = mapped.length === productSize;
    }

    productPage.value += 1;
    syncModalStockWithCart();
  } catch (e) {
    console.error(e);
    toastShow("Không tải được danh sách biến thể từ DB", "danger");
    productHasMore.value = false;
  } finally {
    productLoading.value = false;
  }
}

async function reloadProducts() {
  products.value = [];
  productPage.value = 0;
  productHasMore.value = true;
  await loadMoreProducts();
}

const filteredProducts = computed(() => {
  const kw = productKw.value.trim().toLowerCase();
  if (!kw) return products.value;
  return products.value.filter((p) => [p.code, p.name, p.color, p.size].some((x) => String(x).toLowerCase().includes(kw)));
});

function openProductModal() {
  showCustomerModal.value = false;
  showProductModal.value = true;
  if (products.value.length === 0) loadMoreProducts();
  else syncModalStockWithCart();
}
function closeProductModal() {
  showProductModal.value = false;
}

/** ========= CART STOCK ========= */
function ensureCartItemStockBase(it, base) {
  if (typeof it.stockBase !== "number") it.stockBase = Number(base || 0);
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

function clampInt(n, min, max) {
  n = Number.isFinite(Number(n)) ? Math.floor(Number(n)) : min;
  return Math.max(min, Math.min(max, n));
}

function setQtyByInput(cartIndex, nextQtyRaw) {
  const o = activeOrder.value;
  if (!o) return;
  const it = o.cart[cartIndex];
  if (!it) return;

  const nextQty = clampInt(nextQtyRaw, 1, 999999);
  const curQty = Number(it.qty || 0);
  if (nextQty === curQty) return;

  const p = findModalProductById(it.idSpct);
  const delta = nextQty - curQty;

  if (p) {
    if (delta > 0) {
      if ((Number(p.stock) || 0) < delta) return toastShow("Số lượng vượt tồn kho", "danger");
      p.stock = Math.max(0, Number(p.stock || 0) - delta);
    } else {
      p.stock = Math.min(Number(p._baseStock ?? p.stock ?? 0), Number(p.stock || 0) + Math.abs(delta));
    }
  } else {
    const base = Number(it.stockBase ?? it.stock ?? 0);
    if (base > 0 && nextQty > base) return toastShow("Số lượng vượt tồn kho", "danger");
  }

  it.qty = nextQty;
  syncAllCartStocks();
}

function incQty(i) {
  const o = activeOrder.value;
  if (!o) return;
  const it = o.cart[i];
  if (!it) return;
  setQtyByInput(i, Number(it.qty || 0) + 1);
}
function decQty(i) {
  const o = activeOrder.value;
  if (!o) return;
  const it = o.cart[i];
  if (!it) return;
  setQtyByInput(i, Math.max(1, Number(it.qty || 0) - 1));
}
function removeItem(i) {
  const o = activeOrder.value;
  if (!o) return;
  const it = o.cart[i];
  if (!it) return;

  const p = findModalProductById(it.idSpct);
  if (p) {
    p.stock = Math.min(Number(p._baseStock ?? p.stock ?? 0), Number(p.stock || 0) + Number(it.qty || 0));
  }

  o.cart.splice(i, 1);
  syncAllCartStocks();
  syncModalStockWithCart();
}

function onQtyInput(idx, e) {
  const raw = e?.target?.value ?? "";
  const num = raw === "" ? 1 : Number(String(raw).replace(/[^\d]/g, ""));
  setQtyByInput(idx, num);
}
function onQtyBlur(idx, e) {
  const o = activeOrder.value;
  if (!o) return;
  const it = o.cart[idx];
  if (!it) return;
  e.target.value = it.qty;
}

function sameMoney(a, b) {
  return Math.round(Number(a || 0)) === Math.round(Number(b || 0));
}
function chooseProduct(p) {
  const o = activeOrder.value;
  if (!o) return;
  if ((Number(p.stock) || 0) <= 0) return toastShow("Sản phẩm đã hết hàng", "warning");

  const id = Number(p.idSpct);
  let idx = o.cart.findIndex((x) => Number(x.idSpct) === id && sameMoney(x.price, p.price));

  if (idx === -1) {
    const base = Number(p._baseStock ?? 0);
    o.cart.push({
      key: `${id}-${Math.round(Number(p.price || 0))}-${Date.now()}-${Math.random()}`,
      idSpct: id,
      code: p.code,
      name: p.name,
      meta: `size ${p.size} / ${p.color}`,
      image: p.image,
      price: Number(p.price || 0),
      qty: 0,
      stockBase: base,
      stock: base,
    });
    idx = o.cart.length - 1;
  } else {
    ensureCartItemStockBase(o.cart[idx], Number(p._baseStock ?? 0));
  }

  setQtyByInput(idx, Number(o.cart[idx].qty || 0) + 1);
  toastShow(`Đã thêm ${p.code}`, "success");
}

/** ========= TOTALS ========= */
const subTotal = computed(() => {
  const o = activeOrder.value;
  if (!o) return 0;
  return o.cart.reduce((s, it) => s + (Number(it.price) || 0) * (Number(it.qty) || 0), 0);
});

/** ========= CUSTOMERS (DB) ========= */
const customers = ref([]);
const customerKw = ref("");
const customerLoading = ref(false);

function mapCustomer(x) {
  const addr = x.diaChi || [x.diaChiChiTiet, x.phuongXa, x.quanHuyen, x.tinhThanh].filter(Boolean).join(", ") || "";
  return { id: x.id, name: x.tenKhachHang || "", phone: x.soDienThoai || "", email: x.email || "", address: addr, raw: x };
}
async function reloadCustomers() {
  customerLoading.value = true;
  try {
    const res = await listKhachHang(0, 9999);
    const data = res?.data ?? res;
    const list = Array.isArray(data) ? data : data?.content || [];
    customers.value = list.map(mapCustomer);
  } catch (e) {
    console.error(e);
    toastShow("Không tải được danh sách khách hàng từ DB", "danger");
  } finally {
    customerLoading.value = false;
  }
}
const filteredCustomers = computed(() => {
  const kw = customerKw.value.trim().toLowerCase();
  if (!kw) return customers.value;
  return customers.value.filter((c) => [c.name, c.phone, c.email, c.address].some((x) => String(x).toLowerCase().includes(kw)));
});

function openCustomerModal() {
  showProductModal.value = false;
  showCustomerModal.value = true;
  if (customers.value.length === 0) reloadCustomers();
}
function closeCustomerModal() {
  showCustomerModal.value = false;
}
function chooseCustomer(c) {
  const o = activeOrder.value;
  if (!o) return;
  o.customer = { id: c.id, name: c.name, phone: c.phone, email: c.email, address: c.address };
  o.customerDraft.phone = c.phone || "";
  o.customerDraft.email = c.email || "";
  if (!String(o.diaChi || "").trim()) o.diaChi = c.address || "";
  closeCustomerModal();
}

/** ========= VOUCHERS (DB) ========= */
const vouchers = ref([]);
const confirmHint = ref("");

function normalizeVoucher(x) {
  return {
    id: x.id,
    ma_giam_gia: x.maGiamGia ?? x.ma_giam_gia ?? "",
    ten_giam_gia: x.tenGiamGia ?? x.ten_giam_gia ?? "",
    trang_thai: x.trangThai ?? x.trang_thai ?? true,
    so_luong: Number(x.soLuong ?? 0),
    loai_giam: !!(x.loaiGiam ?? x.loai_giam),
    gia_tri_phan_tram: Number(x.giaTriPhanTram ?? x.gia_tri_phan_tram ?? 0),
    gia_tri_tien_mat: Number(x.giaTriTienMat ?? x.gia_tri_tien_mat ?? 0),
    don_hang_toi_thieu: Number(x.donHangToiThieu ?? x.don_hang_toi_thieu ?? 0),
    gia_tri_giam_toi_da: Number(x.giaTriGiamToiDa ?? x.gia_tri_giam_toi_da ?? 0),

    ngay_bat_dau: x.ngayBatDau ?? x.ngay_bat_dau ?? x.startDate ?? x.start_date ?? null,
    ngay_ket_thuc: x.ngayKetThuc ?? x.ngay_ket_thuc ?? x.endDate ?? x.end_date ?? null,
  };
}
async function loadVouchers() {
  try {
    const res = await http.get("/api/pgg");
    vouchers.value = (res.data || []).map(normalizeVoucher);
  } catch (e) {
    console.error(e);
    toastShow("Không tải được danh sách PGG", "danger");
  }
}

function calcVoucherDiscount(subtotal, v) {
  const st = Number(subtotal) || 0;
  if (st <= 0) return 0;
  if (!v?.trang_thai) return 0;
  if ((Number(v.so_luong) || 0) <= 0) return 0;
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
const bestVoucherEntry = computed(() => eligibleVoucherEntries.value[0] || null);
const altVoucherEntries = computed(() => eligibleVoucherEntries.value.slice(1));

const appliedVoucher = computed(() => {
  const o = activeOrder.value;
  if (!o?.pggId) return null;
  return vouchers.value.find((x) => x.id === o.pggId) || null;
});

const voucherUpsellHint = computed(() => {
  const st = subTotal.value;
  if (st <= 0) return null;

  const bestNow = bestVoucherEntry.value?.discount || 0;

  const candidates = vouchers.value
    .filter((v) => v?.trang_thai && (Number(v.so_luong) || 0) > 0)
    .map((v) => {
      const min = Number(v.don_hang_toi_thieu || 0);
      const missing = Math.max(0, min - st);
      if (missing <= 0) return null;
      const discAtMin = calcVoucherDiscount(min, v);
      return { v, missing, discAtMin, min };
    })
    .filter(Boolean)
    .sort((a, b) => (b.discAtMin - a.discAtMin));

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

watch([subTotal, vouchers, activeId], () => {
  const o = activeOrder.value;
  if (!o) return;

  if (o.voucherMode === "none") {
    o.pggId = null;
    o.voucherCode = "";
    return;
  }

  if (o.voucherMode === "best") {
    const best = bestVoucherEntry.value?.v || null;
    o.pggId = best?.id ?? null;
    o.voucherCode = best?.ma_giam_gia || "";
    if (best?.loai_giam) o.discountPercent = Number(best.gia_tri_phan_tram || 0);
    return;
  }
}, { immediate: true });

function applyVoucherManual(v) {
  const o = activeOrder.value;
  if (!o) return;
  o.voucherMode = "manual";
  o.pggId = v.id;
  o.voucherCode = v.ma_giam_gia || "";
  if (v.loai_giam) o.discountPercent = Number(v.gia_tri_phan_tram || 0);
  else o.discountPercent = 0;
  toastShow(`Đã chọn ${v.ma_giam_gia}`, "info");
}
function disableVoucher() {
  const o = activeOrder.value;
  if (!o) return;

  o.voucherMode = "none";
  o.pggId = null;
  o.voucherCode = "";
  o.discountPercent = 0;

  toastShow("Đã tắt mã giảm giá", "info");
}
function clearVoucherManual() {
  const o = activeOrder.value;
  if (!o) return;
  o.voucherCode = "";
  o.voucherMode = "best";
  const best = bestVoucherEntry.value?.v || null;
  o.pggId = best?.id ?? null;
  o.voucherCode = best?.ma_giam_gia || "";
  if (best?.loai_giam) o.discountPercent = Number(best.gia_tri_phan_tram || 0);
}
async function applyPggByCode() {
  const o = activeOrder.value;
  if (!o) return;

  const code = (o.voucherCode || "").trim().toUpperCase();
  if (!code) {
    o.voucherMode = "best";
    const best = bestVoucherEntry.value?.v || null;
    o.pggId = best?.id ?? null;
    o.voucherCode = best?.ma_giam_gia || "";
    if (best?.loai_giam) o.discountPercent = Number(best.gia_tri_phan_tram || 0);
    return;
  }

  const found = vouchers.value.find((v) => String(v.ma_giam_gia).toUpperCase() === code);
  if (!found) return toastShow("Mã PGG không tồn tại", "danger");

  const disc = calcVoucherDiscount(subTotal.value, found);
  if (disc <= 0) return toastShow("Mã không áp dụng được cho đơn hiện tại", "warning");

  applyVoucherManual(found);
}

/** ========= DISCOUNT + TOTAL ========= */
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
const grandTotal = computed(() => Math.max(0, subTotal.value - discountMoney.value));
const changeMoney = computed(() => {
  const o = activeOrder.value;
  if (!o) return 0;
  return Math.max(0, Number(o.paid || 0) - grandTotal.value);
});

/** ========= PRE-CHECKOUT CONFIRM SYSTEM ========= */
const showPreCheckoutModal = ref(false);
const preCheckoutUi = reactive({
  type: "info",
  message: "",
  detail: "",
  suggest: null,
});
let _resolveConfirm = null;

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

function getBestEligibleNow() {
  const best = bestVoucherEntry.value;
  if (!best) return null;
  return { id: best.v.id, code: best.v.ma_giam_gia, discount: best.discount };
}
function getAppliedNow() {
  const o = activeOrder.value;
  if (!o?.pggId) return { has: false, valid: false, id: null, code: "", discount: 0 };

  const v = vouchers.value.find((x) => x.id === o.pggId) || null;
  if (!v) return { has: true, valid: false, id: o.pggId, code: o.voucherCode || "", discount: 0 };

  const disc = calcVoucherDiscount(subTotal.value, v);
  return { has: true, valid: disc > 0, id: v.id, code: v.ma_giam_gia, discount: disc };
}

function applyBestVoucherNow() {
  const o = activeOrder.value;
  if (!o) return;
  const best = bestVoucherEntry.value?.v || null;
  if (!best) return;
  o.voucherMode = "manual";
  o.pggId = best.id;
  o.voucherCode = best.ma_giam_gia || "";
  if (best.loai_giam) o.discountPercent = Number(best.gia_tri_phan_tram || 0);
  else o.discountPercent = 0;
}
function removeVoucherNow() {
  const o = activeOrder.value;
  if (!o) return;
  o.voucherMode = "none";
  o.pggId = null;
  o.voucherCode = "";
}

async function runVoucherPrecheckFlow() {
  const o = activeOrder.value;
  if (!o) return true;

  const best = getBestEligibleNow();
  const applied = getAppliedNow();

  if (applied.has && !applied.valid) {
    const ok = await openConfirm({
      type: "danger",
      message: `Phiếu giảm giá "${applied.code || o.voucherCode || ""}" không còn hiệu lực cho đơn hiện tại.`,
      detail: best ? "Bạn có muốn chọn phiếu đang tốt nhất hiện tại không?" : "Không có phiếu nào khác áp dụng được. Bạn có muốn bỏ phiếu này không?",
      suggest: best,
    });

    if (ok) {
      if (best) applyBestVoucherNow();
      else removeVoucherNow();
    } else {
      removeVoucherNow();
    }
  }

  const best2 = getBestEligibleNow();
  if (o.voucherMode === "none" && best2) {
    const ok = await openConfirm({
      type: "info",
      message: `Đơn hàng có phiếu giảm giá áp dụng được.`,
      detail: `Bạn có muốn áp dụng mã "${best2.code}" không?`,
      suggest: best2,
    });
    if (ok) applyBestVoucherNow();
  }

  const best3 = getBestEligibleNow();
  const applied3 = getAppliedNow();

  if (best3 && applied3.valid && applied3.id && best3.id !== applied3.id && best3.discount > applied3.discount) {
    const ok = await openConfirm({
      type: "info",
      message: `Có phiếu giảm giá tốt hơn mã đang áp dụng.`,
      detail: `Mã hiện tại: ${applied3.code} (-${money(applied3.discount)}). Bạn có muốn đổi sang mã tốt hơn không?`,
      suggest: best3,
    });
    if (ok) applyBestVoucherNow();
  }

  return true;
}

/** ========= CHECKOUT ========= */
function validateCheckout(o) {
  if (!o) return "Không có đơn hàng đang chọn";
  if (!Array.isArray(o.cart) || o.cart.length === 0) return "Giỏ hàng trống";

  for (const it of o.cart) {
    const qty = Number(it.qty || 0);
    if (qty <= 0) return `Số lượng không hợp lệ: ${it.code}`;
    if (Number.isFinite(it.stockBase) && qty > Number(it.stockBase)) return `Sản phẩm ${it.code} vượt tồn kho`;
  }

  const paid = Number(o.paid || 0);
  if (paid < grandTotal.value) return "Khách thanh toán chưa đủ";

  return null;
}

function buildPosPayload(o) {
  return {
    maHoaDon: o.maHoaDon,
    loaiDon: false,
    phiVanChuyen: 0,

    idKhachHang: o.customer?.id ?? null,
    idPhieuGiamGia: o.pggId ?? null,
    giamThuCongPercent: o.pggId ? null : clampInt(o.discountPercent, 0, 100),

    tenKhachHang: o.customer?.name || "Khách lẻ",
    soDienThoai: (o.customerDraft.phone || o.customer?.phone || "").trim(),
    emailKhachHang: (o.customerDraft.email || o.customer?.email || "").trim(),
    diaChiKhachHang: (o.diaChi || o.customer?.address || "").trim(),

    ghiChu: "POS checkout",
    paid: Number(o.paid || 0),

    items: o.cart.map((it) => ({
      idSanPhamChiTiet: Number(it.idSpct),
      soLuong: clampInt(it.qty, 1, 999999),
    })),
  };
}

function resetOrderAfterPaid(o) {
  o.cart = [];
  o.customer = null;
  o.customerDraft = { phone: "", email: "" };
  o.diaChi = "";

  o.voucherCode = "";
  o.pggId = null;
  o.voucherMode = "best";
  o.voucherTab = "best";

  o.discountPercent = 0;
  o.paid = 0;

  o.maHoaDon = genUniqueMaHoaDon();
  o.label = `Hóa Đơn - ${o.maHoaDon}`;

  syncModalStockWithCart();
}

async function confirmOrder() {
  const o = activeOrder.value;
  confirmHint.value = "";

  const err = validateCheckout(o);
  if (err) return toastShow(err, err.includes("chưa đủ") ? "warning" : "danger");

  await runVoucherPrecheckFlow();

  const payload = buildPosPayload(o);

  submitting.value = true;
  try {
    const res = await http.post("/api/hoa-don/pos", payload);
    const ma = res?.data?.maHoaDon || payload.maHoaDon || "Hóa đơn";
    toastShow(`Tạo hóa đơn thành công: ${ma}`, "success");
    resetOrderAfterPaid(o);
  } catch (e) {
    console.error(e);
    const msg = e?.response?.data?.message || e?.response?.data?.error || "Tạo hóa đơn thất bại";
    toastShow(msg, "danger");
  } finally {
    submitting.value = false;
  }
}

/** ========= LIFECYCLE ========= */
function onKeydown(e) {
  if (e.key === "Escape") {
    if (showPreCheckoutModal.value) return resolvePreCheckout(false);
    if (anyModalOpen.value) closeAnyModal();
  }
}

onMounted(() => {
  loadDrafts();
  if (orders.value.length === 0) createOrder();

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
  saveDraftsNow();
});
</script>

<style scoped>
.tab-x { opacity: 0.85; cursor: pointer; }
.tab-x:hover { opacity: 1; }

.voucher-card {
  border: 1px solid #bfead8;
  background: #e9fbf3;
  border-radius: 10px;
  padding: 12px;
}

/* z-index */
:global(.modal-backdrop) { z-index: 1050; }
:global(.modal) { z-index: 1055; }

/* Tránh click xuyên */
:global(.modal) { pointer-events: none; }
:global(.modal .modal-dialog) { pointer-events: auto; }
</style>