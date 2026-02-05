<template>
  <div class="container-fluid py-3">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-3">
      <h4 class="m-0 fw-bold">Bán hàng (tại cửa hàng)</h4>

      <button class="btn btn-outline-secondary btn-sm" type="button" @click="createOrder" :disabled="orders.length >= MAX_ORDERS">
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
                        <button class="btn btn-outline-secondary btn-sm" disabled style="min-width:46px">
                          {{ it.qty }}
                        </button>
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
                      <textarea class="form-control" rows="2" placeholder="Địa Chỉ (nếu có)" v-model="activeOrder.note"></textarea>
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
                       ✅ PHIẾU GIẢM GIÁ (ĐÃ SỬA)
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

                    <!-- Tabs -->
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
                      <li class="nav-item ">
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

                    <!-- Panel -->
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
                                <span class="badge text-bg-secondary">PGG</span>
                                <span class="badge text-bg-secondary">Mã tốt nhất</span>
                                <span class="badge text-bg-secondary">{{ bestVoucherEntry.v.ma_giam_gia }}</span>
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
                                v-if="activeOrder.voucherMode === 'best' && activeOrder.pggId === bestVoucherEntry.v.id"
                                class="badge text-bg-success"
                              >
                                Đang áp dụng
                              </span>

                              <button
                                v-else
                                class="btn btn-success btn-sm"
                                type="button"
                                @click="useBestVoucher"
                              >
                                Áp dụng
                              </button>
                            </div>
                          </div>

                          <!-- input nhập mã vẫn giữ để gõ tay -->
                          <div class="mt-3">
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
                            <div class="small text-muted mt-1">
                              * Gõ mã để chọn thủ công (chuyển sang “manual”).
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

                          <!-- input nhập mã vẫn giữ -->
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

                    <!-- Suggestions -->
                    <div class="mt-3">
                      <div class="fw-bold">Gợi ý mã giảm giá <span class="text-danger">*</span></div>

                      <div v-if="altVoucherEntries.length === 0" class="text-muted small mt-1">
                        Không có mã giảm giá gợi ý khả dụng
                      </div>

                      <div v-else class="d-flex flex-wrap gap-2 mt-2">
                        <button
                          v-for="e in altVoucherEntries.slice(0, 6)"
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

                  <!-- Manual % -->
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

                  <!-- Summary -->
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
                        v-model.number="activeOrder.paid"
                        inputmode="numeric"
                      />
                    </li>

                    <li class="list-group-item d-flex justify-content-between">
                      <span class="text-muted">Tiền thừa</span>
                      <span class="fw-semibold">{{ money(changeMoney) }}</span>
                    </li>
                  </ul>

                  <button class="btn btn-success w-100" :disabled="activeOrder.cart.length === 0" @click="confirmOrder">
                    XÁC NHẬN (LOG PAYLOAD)
                  </button>

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

    <!-- ✅ Modal layer: Teleport ra body để không bị mờ/đè -->
    <teleport to="body">
      <!-- Backdrop (chỉ 1 cái) -->
      <div
        v-if="anyModalOpen"
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
    label: o?.label || `Đơn hàng ${idx} - ${ma}`,
    cart: Array.isArray(o?.cart) ? o.cart : [],
    customer: o?.customer ?? null,
    customerDraft: o?.customerDraft ?? { phone: "", email: "" },

    // ====== PGG (đã nâng cấp) ======
    voucherCode: String(o?.voucherCode || ""),
    pggId: o?.pggId ?? null,
    discountPercent: Number(o?.discountPercent || 0),

    voucherMode: o?.voucherMode ?? "best", // best | manual | none
    voucherTab: o?.voucherTab ?? "best",   // best | alt
    // ==============================

    paid: Number(o?.paid || 0),
    note: o?.note || "",
  };
}

function createOrder() {
  if (orders.value.length >= MAX_ORDERS) return toastShow(`Chỉ tối đa ${MAX_ORDERS} đơn`, "warning");

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
        customerDraft: { phone: "", email: "" },

        voucherCode: "",
        pggId: null,
        discountPercent: 0,
        voucherMode: "best",
        voucherTab: "best",

        paid: 0,
        note: "",
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
  saveDraftsNow();
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

/** ========= CART ========= */
function incQty(i) {
  const o = activeOrder.value;
  if (!o) return;
  const it = o.cart[i];
  if (!it) return;

  const next = it.qty + 1;
  if (Number.isFinite(it.stock) && next > it.stock) return toastShow("Số lượng vượt tồn kho", "danger");
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

/** ========= TOTALS ========= */
function money(n) {
  const v = Number(n) || 0;
  return v.toLocaleString("vi-VN") + " đ";
}
const subTotal = computed(() => {
  const o = activeOrder.value;
  if (!o) return 0;
  return o.cart.reduce((s, it) => s + (Number(it.price) || 0) * (Number(it.qty) || 0), 0);
});

/** ========= MODALS (FIX mờ/đè) ========= */
const showProductModal = ref(false);
const showCustomerModal = ref(false);

const anyModalOpen = computed(() => showProductModal.value || showCustomerModal.value);

watch(anyModalOpen, (open) => {
  document.body.classList.toggle("modal-open", open);
});

function closeAnyModal() {
  showProductModal.value = false;
  showCustomerModal.value = false;
}
function onKeydown(e) {
  if (e.key === "Escape" && anyModalOpen.value) closeAnyModal();
}

/** ========= PRODUCTS (DB) ========= */
const products = ref([]);
const productKw = ref("");
const productLoading = ref(false);
const productPage = ref(0);
const productSize = 100;
const productHasMore = ref(true);

function mapSpct(x) {
  return {
    idSpct: x.id,
    code: x.maSanPhamChiTiet || "",
    name: x.tenSanPham || "",
    color: x.tenMauSac || "",
    size: x.tenKichCo || "",
    stock: Number(x.soLuongTon || 0),
    price: Number(x.donGia || 0),
    image: x.anh ? buildImgUrl(x.anh) : placeholderImg,
  };
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
  return products.value.filter((p) =>
    [p.code, p.name, p.color, p.size].some((x) => String(x).toLowerCase().includes(kw))
  );
});

function openProductModal() {
  showCustomerModal.value = false;
  showProductModal.value = true;
  if (products.value.length === 0) loadMoreProducts();
}
function closeProductModal() {
  showProductModal.value = false;
}

function chooseProduct(p) {
  const o = activeOrder.value;
  if (!o) return;

  const existed = o.cart.find((x) => x.idSpct === p.idSpct);
  if (existed) {
    const next = existed.qty + 1;
    if (Number.isFinite(existed.stock) && next > existed.stock) return toastShow("Số lượng vượt tồn kho", "danger");
    existed.qty = next;
  } else {
    o.cart.push({
      key: `${p.idSpct}-${Date.now()}`,
      idSpct: p.idSpct,
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

/** ========= CUSTOMERS (DB) ========= */
const customers = ref([]);
const customerKw = ref("");
const customerLoading = ref(false);

function mapCustomer(x) {
  const addr =
    x.diaChi ||
    [x.diaChiChiTiet, x.phuongXa, x.quanHuyen, x.tinhThanh].filter(Boolean).join(", ") ||
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
  return customers.value.filter((c) =>
    [c.name, c.phone, c.email, c.address].some((x) => String(x).toLowerCase().includes(kw))
  );
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
    loai_giam: !!(x.loaiGiam ?? x.loai_giam), // true: %, false: tiền
    gia_tri_phan_tram: Number(x.giaTriPhanTram ?? x.gia_tri_phan_tram ?? 0),
    gia_tri_tien_mat: Number(x.giaTriTienMat ?? x.gia_tri_tien_mat ?? 0),
    don_hang_toi_thieu: Number(x.donHangToiThieu ?? x.don_hang_toi_thieu ?? 0),
    gia_tri_giam_toi_da: Number(x.giaTriGiamToiDa ?? x.gia_tri_giam_toi_da ?? 0),
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

// Auto áp dụng mã tốt nhất (mặc định)
watch(
  [subTotal, vouchers, activeId],
  () => {
    const o = activeOrder.value;
    if (!o) return;

    if (o.voucherMode === "none") {
      o.pggId = null;
      o.voucherCode = "";
      return;
    }

    // best: luôn bám theo best
    if (o.voucherMode === "best") {
      const best = bestVoucherEntry.value?.v || null;
      o.pggId = best?.id ?? null;
      o.voucherCode = best?.ma_giam_gia || "";

      // khóa giảm thủ công -> set theo % nếu là % (giống behavior cũ)
      if (best?.loai_giam) o.discountPercent = Number(best.gia_tri_phan_tram || 0);
      return;
    }

    // manual: nếu mã manual không còn hợp lệ -> quay về best
    if (o.voucherMode === "manual") {
      const stillValid = !!eligibleVoucherEntries.value.find((e) => e.v.id === o.pggId);
      if (!stillValid) {
        o.voucherMode = "best";
        const best = bestVoucherEntry.value?.v || null;
        o.pggId = best?.id ?? null;
        o.voucherCode = best?.ma_giam_gia || "";
        if (best?.loai_giam) o.discountPercent = Number(best.gia_tri_phan_tram || 0);
      }
    }
  },
  { immediate: true }
);

function useBestVoucher() {
  const o = activeOrder.value;
  if (!o) return;
  o.voucherMode = "best";
  const best = bestVoucherEntry.value?.v || null;
  o.pggId = best?.id ?? null;
  o.voucherCode = best?.ma_giam_gia || "";
  if (best?.loai_giam) o.discountPercent = Number(best.gia_tri_phan_tram || 0);
}

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
  toastShow("Đã tắt mã giảm giá", "info");
}

function clearVoucherManual() {
  const o = activeOrder.value;
  if (!o) return;
  o.voucherCode = "";
  // về best thay vì null (vì yêu cầu mặc định best)
  o.voucherMode = "best";
  useBestVoucher();
}

async function applyPggByCode() {
  const o = activeOrder.value;
  if (!o) return;

  const code = (o.voucherCode || "").trim().toUpperCase();
  if (!code) {
    // nếu xóa input thì quay về best
    o.voucherMode = "best";
    useBestVoucher();
    return;
  }

  const found = vouchers.value.find((v) => String(v.ma_giam_gia).toUpperCase() === code);
  if (!found) {
    toastShow("Mã PGG không tồn tại", "danger");
    o.voucherMode = "best";
    useBestVoucher();
    return;
  }

  // kiểm tra hợp lệ với subtotal
  const disc = calcVoucherDiscount(subTotal.value, found);
  if (disc <= 0) {
    toastShow("Mã không áp dụng được cho đơn hiện tại", "warning");
    o.voucherMode = "best";
    useBestVoucher();
    return;
  }

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

  // không có voucher -> dùng giảm thủ công %
  const percent = Math.max(0, Math.min(100, Number(o.discountPercent || 0)));
  return Math.floor((st * percent) / 100);
});

const grandTotal = computed(() => Math.max(0, subTotal.value - discountMoney.value));

const changeMoney = computed(() => {
  const o = activeOrder.value;
  if (!o) return 0;
  return Math.max(0, Number(o.paid || 0) - grandTotal.value);
});

/** ========= CONFIRM (CHỈ LOG) ========= */
async function confirmOrder() {
  const o = activeOrder.value;
  if (!o) return;

  confirmHint.value = "";

  if (o.cart.length === 0) return toastShow("Giỏ hàng trống", "danger");

  for (const it of o.cart) {
    if (Number.isFinite(it.stock) && it.qty > it.stock) {
      return toastShow(`Sản phẩm ${it.code} vượt tồn kho`, "danger");
    }
  }

  const payload = {
    maHoaDon: o.maHoaDon,
    loaiDon: false,
    phiVanChuyen: 0,
    idKhachHang: o.customer?.id ?? null,
    idPhieuGiamGia: o.pggId ?? null,
    giamThuCongPercent: o.pggId ? 0 : Number(o.discountPercent || 0),
    tenKhachHang: o.customer?.name || "Khách lẻ",
    soDienThoai: o.customerDraft.phone || o.customer?.phone || "",
    emailKhachHang: o.customerDraft.email || o.customer?.email || "",
    diaChiKhachHang: "",
    ghiChu: o.note || "",
    paid: Number(o.paid || 0),
    items: o.cart.map((it) => ({ idSanPhamChiTiet: it.idSpct, soLuong: it.qty })),
  };

  console.log("ORDER PAYLOAD (STORE ONLY):", payload);
  toastShow("Đã tạo payload (chưa lưu DB). Xem console.", "info");
}

/** ========= LIFECYCLE ========= */
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
.tab-x {
  opacity: 0.85;
  cursor: pointer;
}
.tab-x:hover {
  opacity: 1;
}

/* ✅ card voucher giống kiểu ảnh */
.voucher-card {
  border: 1px solid #bfead8;
  background: #e9fbf3;
  border-radius: 10px;
  padding: 12px;
}

/* Chốt z-index: backdrop < modal */
:global(.modal-backdrop) { z-index: 1050; }
:global(.modal) { z-index: 1055; }

/* Tránh click xuyên */
:global(.modal) { pointer-events: none; }
:global(.modal .modal-dialog) { pointer-events: auto; }
</style>
