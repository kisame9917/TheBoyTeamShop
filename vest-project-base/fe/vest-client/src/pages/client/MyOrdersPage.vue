<template>
  <div class="my-orders-page py-4 py-lg-5">
    <div class="container">
      <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
        <div>
          <h1 class="page-title mb-1">Đơn hàng của tôi</h1>
         
        </div>

        <router-link :to="{ name: 'OrderLookup' }" class="btn btn-outline-secondary">
          Tra cứu đơn hàng công khai
        </router-link>
      </div>

      <div
        v-if="pageMessage"
        :class="[
          'alert',
          pageMessageType === 'success'
            ? 'alert-success'
            : pageMessageType === 'warning'
            ? 'alert-warning'
            : 'alert-danger',
          'mb-4'
        ]"
      >
        {{ pageMessage }}
      </div>

      <div class="filter-wrap mb-4">
        <button
          v-for="item in filterOptions"
          :key="item.value"
          class="filter-btn"
          :class="{ active: activeFilter === item.value }"
          type="button"
          @click="activeFilter = item.value"
        >
          {{ item.label }}
        </button>
      </div>

      <div v-if="loading" class="card shadow-sm border-0">
        <div class="card-body py-5 text-center">
          <div class="spinner-border text-primary" role="status"></div>
          <p class="mt-3 mb-0 text-muted">Đang tải danh sách đơn hàng...</p>
        </div>
      </div>

      <div v-else-if="errorMessage" class="alert alert-danger">
        {{ errorMessage }}
      </div>

      <div v-else-if="!filteredOrders.length" class="card shadow-sm border-0">
        <div class="card-body py-5 text-center text-muted">
          Không có đơn hàng nào trong mục này.
        </div>
      </div>

      <div v-else class="row g-4">
        <div class="col-12 col-lg-5">
          <div
            v-for="item in filteredOrders"
            :key="item.id"
            class="order-card"
            :class="{ active: selectedOrderId === item.id }"
            @click="viewDetail(item.id)"
          >
            <div class="d-flex justify-content-between gap-3 flex-wrap">
              <div>
                <div class="order-code">{{ item.maHoaDon }}</div>
                <div class="order-date">{{ formatDate(item.ngayTao) }}</div>
              </div>

              <span class="status-badge" :class="statusClass(item.trangThaiDon)">
                {{ item.tenTrangThaiDon }}
              </span>
            </div>

            <div class="order-meta mt-3">
              <div><strong>Người nhận:</strong> {{ item.tenNguoiNhanHang || "-" }}</div>
              <div><strong>SĐT nhận:</strong> {{ item.soDienThoaiNhanHang || "-" }}</div>
              <div><strong>Sản phẩm:</strong> {{ item.tongSanPham || 0 }}</div>
              <div>
                <strong>Thanh toán:</strong>
                {{ item.paymentMethod || "-" }}
                <span class="payment-badge ms-2" :class="paymentStatusClass(item.paymentStatus, item.trangThaiDon)">
                  {{ paymentStatusText(item.paymentStatus, item.trangThaiDon) }}
                </span>
              </div>
            </div>

            <div class="order-total mt-3">
              {{ money(item.tongTienSauGiam) }} đ
            </div>
          </div>
        </div>

        <div class="col-12 col-lg-7">
          <div v-if="detailLoading" class="card shadow-sm border-0">
            <div class="card-body py-5 text-center">
              <div class="spinner-border text-primary" role="status"></div>
              <p class="mt-3 mb-0 text-muted">Đang tải chi tiết đơn hàng...</p>
            </div>
          </div>

          <div v-else-if="selectedOrder" class="detail-box">
            <div class="detail-head">
              <div>
                <div class="detail-code">Mã đơn: {{ selectedOrder.maHoaDon }}</div>
                <div class="detail-date">Đặt lúc: {{ formatDate(selectedOrder.ngayTao) }}</div>
              </div>

              <div class="detail-actions">
                <span class="status-badge" :class="statusClass(selectedOrder.trangThaiDon)">
                  {{ selectedOrder.tenTrangThaiDon }}
                </span>

                <div class="action-row">
                  <button
                    v-if="canCancelSelected"
                    class="btn btn-outline-danger btn-sm"
                    type="button"
                    @click="openCancelModal"
                  >
                    Hủy đơn
                  </button>

                  <button
                    v-if="canEditShippingSelected"
                    class="btn btn-outline-primary btn-sm"
                    type="button"
                    @click="openShippingModal"
                  >
                    Sửa giao hàng
                  </button>

                  <button
                    v-if="canEditItemsSelected"
                    class="btn btn-dark btn-sm"
                    type="button"
                    @click="openItemsModal"
                  >
                    Sửa sản phẩm
                  </button>
                </div>
              </div>
            </div>

            <div v-if="showSelectedProgress" class="progress-card mt-3">
              <div class="progress-line">
                <div
                  v-for="step in selectedProgressSteps"
                  :key="step.key"
                  class="progress-step"
                  :class="{ active: step.active }"
                >
                  <div class="progress-step__dot"><span v-if="step.active">✓</span></div>
                  <div class="progress-step__label">{{ step.label }}</div>
                </div>
              </div>
            </div>

            <div class="row g-4 mt-1">
              <div class="col-12">
                <div class="info-card">
                  <div class="info-title">Thông tin giao hàng</div>
                  <div class="info-grid">
                    <div><strong>Khách hàng:</strong> {{ selectedOrder.tenKhachHang || "-" }}</div>
                    <div><strong>SĐT đặt:</strong> {{ selectedOrder.soDienThoai || "-" }}</div>
                    <div><strong>Người nhận:</strong> {{ selectedOrder.tenNguoiNhanHang || "-" }}</div>
                    <div><strong>SĐT nhận:</strong> {{ selectedOrder.soDienThoaiNhanHang || "-" }}</div>
                    <div class="full-row">
                      <strong>Địa chỉ:</strong> {{ fullAddress(selectedOrder) || "-" }}
                    </div>
                    <div class="full-row">
                      <strong>Ghi chú:</strong> {{ selectedOrder.ghiChu || "Không có" }}
                    </div>
                  </div>
                </div>
              </div>

              <div class="col-12">
                <div class="info-card">
                  <div class="info-title">Sản phẩm</div>

                  <div v-if="!selectedOrder.items?.length" class="text-muted">
                    Không có sản phẩm trong đơn hàng.
                  </div>

                  <div v-else class="item-list">
                    <div
                      class="item-row"
                      v-for="item in selectedOrder.items"
                      :key="item.idSanPhamChiTiet || item.maSanPhamChiTiet"
                    >
                      <img
                        :src="normalizeImg(item.anhDaiDien)"
                        alt="Ảnh sản phẩm"
                        class="item-image"
                        @error="onImgError"
                      />

                      <div class="item-content">
                        <div class="item-name">{{ item.tenSanPham || "Sản phẩm" }}</div>
                        <div class="item-meta">
                          <span v-if="item.mauSac">Màu: {{ item.mauSac }}</span>
                          <span v-if="item.kichCo"> / Size: {{ item.kichCo }}</span>
                        </div>
                        <div class="item-meta">Số lượng: {{ item.soLuong || 0 }}</div>
                      </div>

                      <div class="item-price">
                        <div>{{ money(item.donGia) }} đ</div>
                        <strong>{{ money(item.thanhTien) }} đ</strong>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="col-12">
                <div class="info-card">
                  <div class="info-title info-title-payment">
                    <span>Thanh toán</span>
                    <span
                        class="payment-badge"
                        :class="paymentStatusClass(selectedOrder.paymentStatus, selectedOrder.trangThaiDon)"
                    >
        {{ paymentStatusText(selectedOrder.paymentStatus, selectedOrder.trangThaiDon) }}
      </span>
                  </div>

                  <div class="sum-row">
                    <span>Tiền hàng</span>
                    <strong>{{ money(selectedOrder.tongTien) }} đ</strong>
                  </div>

                  <div class="sum-row">
                    <span>Giảm giá</span>
                    <strong>- {{ money(selectedOrder.tongTienGiam) }} đ</strong>
                  </div>

                  <div class="sum-row">
                    <span>Phí vận chuyển</span>
                    <strong>{{ money(selectedOrder.phiVanChuyen) }} đ</strong>
                  </div>

                  <div class="sum-row">
                    <span>Phương thức</span>
                    <strong>{{ selectedOrder.paymentMethod || "-" }}</strong>
                  </div>

                  <div class="sum-row total">
                    <span>Tổng thanh toán</span>
                    <strong>{{ money(selectedOrder.tongTienSauGiam) }} đ</strong>
                  </div>
                </div>
              </div>

            </div>
          </div>

          <div v-else class="card shadow-sm border-0">
            <div class="card-body py-5 text-center text-muted">
              Chọn một đơn hàng bên trái để xem chi tiết.
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <div v-if="cancelModalOpen" class="mock-modal-overlay" @click.self="cancelModalOpen = false">
    <div class="mock-modal">
      <div class="mock-modal__header">
        <div class="mock-modal__title">Yêu cầu hủy đơn</div>
        <button class="mock-modal__close" type="button" @click="cancelModalOpen = false">×</button>
      </div>

      <div class="mock-modal__body">
        <div class="mb-3">
          <label class="form-label">Lý do hủy đơn</label>
          <input
            v-model.trim="cancelReason"
            type="text"
            class="form-control"
            placeholder="Ví dụ: Tôi muốn đổi sang mẫu khác"
          />
        </div>

        <div>
          <label class="form-label">Ghi chú thêm</label>
          <textarea
            v-model.trim="cancelNote"
            class="form-control"
            rows="3"
            placeholder="Nhập ghi chú nếu có"
          ></textarea>
        </div>
      </div>

      <div class="mock-modal__footer">
        <button class="btn btn-light" type="button" @click="cancelModalOpen = false">Đóng</button>
        <button class="btn btn-danger" type="button" @click="confirmCancelOrder">Gửi yêu cầu hủy</button>
      </div>
    </div>
  </div>

  <div v-if="shippingModalOpen" class="mock-modal-overlay" @click.self="shippingModalOpen = false">
    <div class="mock-modal">
      <div class="mock-modal__header">
        <div class="mock-modal__title">Sửa thông tin giao hàng</div>
        <button class="mock-modal__close" type="button" @click="shippingModalOpen = false">×</button>
      </div>

      <div class="mock-modal__body">
        <div class="row g-3">
          <div class="col-12 col-md-6">
            <label class="form-label">Người nhận</label>
            <input v-model.trim="shippingForm.tenNguoiNhanHang" class="form-control" type="text" />
          </div>

          <div class="col-12 col-md-6">
            <label class="form-label">SĐT người nhận</label>
            <input v-model.trim="shippingForm.soDienThoaiNhanHang" class="form-control" type="text" />
          </div>

          <div class="col-12 col-md-6">
            <label class="form-label">Tỉnh / Thành</label>
            <div class="address-select-wrap">
              <input
                  v-model.trim="provinceSearch"
                  class="form-control"
                  type="text"
                  autocomplete="off"
                  :disabled="!isSelectedOrderCod || provinceLoading"
                  :placeholder="provinceLoading ? 'Đang tải tỉnh/thành...' : 'Chọn hoặc tìm tỉnh/thành'"
                  @focus="openProvinceDropdown"
                  @click="openProvinceDropdown"
                  @input="onShippingProvinceSearch"
                  @blur="closeAddressDropdown('province')"
              />

              <div v-if="provinceDropdownOpen && isSelectedOrderCod" class="address-dropdown">
                <button
                    v-for="province in filteredProvinces"
                    :key="province.code"
                    type="button"
                    class="address-option"
                    @mousedown.prevent="selectShippingProvince(province)"
                >
                  {{ province.name }}
                </button>

                <div v-if="!filteredProvinces.length" class="address-empty">
                  Không tìm thấy tỉnh/thành
                </div>
              </div>
            </div>
          </div>

          <div class="col-12 col-md-6">
            <label class="form-label">Phường / Xã</label>
            <div class="address-select-wrap">
              <input
                  v-model.trim="wardSearch"
                  class="form-control"
                  type="text"
                  autocomplete="off"
                  :disabled="!isSelectedOrderCod || !selectedProvinceCode || wardLoading"
                  :placeholder="
    !selectedProvinceCode
      ? 'Chọn tỉnh/thành trước'
      : wardLoading
      ? 'Đang tải phường/xã...'
      : 'Chọn hoặc tìm phường/xã'
  "
                  @focus="openWardDropdown"
                  @click="openWardDropdown"
                  @input="onShippingWardSearch"
                  @blur="closeAddressDropdown('ward')"
              />

              <div v-if="wardDropdownOpen && isSelectedOrderCod && selectedProvinceCode" class="address-dropdown">
                <button
                    v-for="ward in filteredWards"
                    :key="ward.code"
                    type="button"
                    class="address-option"
                    @mousedown.prevent="selectShippingWard(ward)"
                >
                  {{ ward.name }}
                </button>

                <div v-if="!filteredWards.length" class="address-empty">
                  Không tìm thấy phường/xã
                </div>
              </div>
            </div>
          </div>

          <div class="col-12 ">
            <label class="form-label">Địa chỉ chi tiết</label>
            <input
              v-model.trim="shippingForm.diaChiNhanHangChiTiet"
              class="form-control"
              type="text"
              :disabled="!isSelectedOrderCod"
            />
          </div>

          <div class="col-12">
            <label class="form-label">Ghi chú</label>
            <textarea v-model.trim="shippingForm.ghiChu" class="form-control" rows="3"></textarea>
          </div>
        </div>

        <div v-if="!isSelectedOrderCod" class="alert alert-warning mt-3 mb-0">
          Đơn chuyển khoản/QR: trước mắt FE chỉ cho sửa tên, SĐT và ghi chú. Địa chỉ đang khóa.
        </div>
      </div>

      <div class="mock-modal__footer">
        <button class="btn btn-light" type="button" @click="shippingModalOpen = false">Đóng</button>
        <button class="btn btn-primary" type="button" :disabled="shippingSaving" @click="submitShipping">
          {{ shippingSaving ? "Đang lưu..." : "Lưu thay đổi" }}
        </button>
      </div>
    </div>
  </div>

  <div v-if="itemsModalOpen" class="mock-modal-overlay" @click.self="itemsModalOpen = false">
    <div class="mock-modal mock-modal--wide">
      <div class="mock-modal__header">
        <div class="mock-modal__title">Sửa sản phẩm trong đơn</div>
        <button class="mock-modal__close" type="button" @click="itemsModalOpen = false">×</button>
      </div>

      <div class="mock-modal__body">
        <div v-if="!itemDrafts.length" class="text-muted">Không có sản phẩm.</div>

        <div v-else class="draft-item-list">
          <div
            v-for="(item, index) in itemDrafts"
            :key="item.idSanPhamChiTiet || item.maSanPhamChiTiet || index"
            class="draft-item-row"
          >
            <img
              :src="normalizeImg(item.anhDaiDien)"
              alt="Ảnh sản phẩm"
              class="draft-item-image"
              @error="onImgError"
            />

            <div class="draft-item-content">
              <div class="draft-item-name">{{ item.tenSanPham || "Sản phẩm" }}</div>
              <div class="draft-item-meta">
                <span v-if="item.mauSac">Màu: {{ item.mauSac }}</span>
                <span v-if="item.kichCo"> / Size: {{ item.kichCo }}</span>
              </div>
              <div class="draft-item-meta">Đơn giá: {{ money(item.donGia) }} đ</div>
            </div>

            <div class="draft-qty-box">
              <button class="btn btn-outline-secondary btn-sm" type="button" @click="decreaseQty(item)">-</button>
              <span class="draft-qty">{{ item.soLuong }}</span>
              <button class="btn btn-outline-secondary btn-sm" type="button" @click="increaseQty(item)">+</button>
            </div>

            <div class="draft-item-price">
              <strong>{{ money(item.thanhTien) }} đ</strong>
            </div>

            <button class="btn btn-outline-danger btn-sm" type="button" @click="removeDraftItem(index)">
              Xóa
            </button>
          </div>
        </div>
      </div>

      <div class="mock-modal__footer">
        <button class="btn btn-light" type="button" @click="itemsModalOpen = false">Đóng</button>
        <button class="btn btn-dark" type="button" :disabled="itemsSaving" @click="submitItems">
          {{ itemsSaving ? "Đang lưu..." : "Lưu thay đổi" }}
        </button>
      </div>
    </div>
  </div>

  <ChatWidget />
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { getMyOrderDetail, getMyOrders, cancelMyOrder, updateMyOrderShipping, updateMyOrderItems } from "../../services/Api";
import ChatWidget from "../../components/ClientChatWidget.vue";
import { resolveMediaUrl } from "../../utils/media";
import vnUnitsData from "../../assets/vn_units.json";
import {
  canCancelOrder,
  canEditShipping,
  canEditItems,
  isCod,
} from "../../utils/orderPermissions";

const loading = ref(true);
const detailLoading = ref(false);
const errorMessage = ref("");
const pageMessage = ref("");
const pageMessageType = ref("success");
const orders = ref([]);
const selectedOrder = ref(null);
const selectedOrderId = ref(null);
const activeFilter = ref("ALL");

const cancelModalOpen = ref(false);
const shippingModalOpen = ref(false);
const itemsModalOpen = ref(false);

const shippingForm = ref({
  tenNguoiNhanHang: "",
  soDienThoaiNhanHang: "",
  diaChiNhanHangChiTiet: "",
  phuongXaNhanHang: "",
  tinhThanhNhanHang: "",
  ghiChu: "",
});

const itemDrafts = ref([]);
const shippingSaving = ref(false);
const itemsSaving = ref(false);

const provinces = ref([]);
const wards = ref([]);
const provinceLoading = ref(false);
const wardLoading = ref(false);
const selectedProvinceCode = ref("");
const selectedWardCode = ref("");
const provinceSearch = ref("");
const wardSearch = ref("");
const provinceDropdownOpen = ref(false);
const wardDropdownOpen = ref(false);

const provinceUserTyping = ref(false);
const wardUserTyping = ref(false);

const canCancelSelected = computed(() => canCancelOrder(selectedOrder.value));
const canEditShippingSelected = computed(() => canEditShipping(selectedOrder.value));
const canEditItemsSelected = computed(() => canEditItems(selectedOrder.value));
const isSelectedOrderCod = computed(() => isCod(selectedOrder.value));

const filteredProvinces = computed(() => {
  if (!provinceUserTyping.value) return provinces.value;

  const keyword = normalizeText(provinceSearch.value);

  if (!keyword) return provinces.value;

  return provinces.value.filter((province) =>
      normalizeText(province.name).includes(keyword)
  );
});

const filteredWards = computed(() => {
  if (!wardUserTyping.value) return wards.value;

  const keyword = normalizeText(wardSearch.value);

  if (!keyword) return wards.value;

  return wards.value.filter((ward) =>
      normalizeText(ward.name).includes(keyword)
  );
});

const filterOptions = [
  { label: "Tất cả", value: "ALL" },
  { label: "Chờ xác nhận", value: 0 },
  { label: "Đã xác nhận", value: 8 },
  { label: "Đang xử lý", value: 1 },
  { label: "Đang giao", value: 2 },
  { label: "Đã giao", value: 3 },
  { label: "Hoàn thành", value: 4 },
  { label: "Đã hủy", value: 5 },
];

const filteredOrders = computed(() => {
  if (activeFilter.value === "ALL") return orders.value;
  return orders.value.filter((x) => Number(x.trangThaiDon) === Number(activeFilter.value));
});

let messageTimer = null;

function showMessage(message, type = "success") {
  pageMessage.value = message;
  pageMessageType.value = type;

  if (messageTimer) clearTimeout(messageTimer);
  messageTimer = setTimeout(() => {
    pageMessage.value = "";
    pageMessageType.value = "success";
  }, 3000);
}

function money(value) {
  return new Intl.NumberFormat("vi-VN").format(Number(value || 0));
}

function formatDate(value) {
  if (!value) return "-";
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleString("vi-VN");
}

function fullAddress(o) {
  return [
    o?.diaChiNhanHangChiTiet,
    o?.phuongXaNhanHang,
    o?.tinhThanhNhanHang,
  ]
    .filter(Boolean)
    .join(", ");
}

function isSpecialOrderStatus(code) {
  return [5, 6, 7, 9].includes(Number(code));
}

function getProgressIndex(code) {
  const n = Number(code);
  if (n === 0) return 0;
  if (n === 8) return 1;
  if (n === 1) return 2;
  if (n === 2) return 3;
  if (n === 3) return 4;
  if (n === 4) return 5;
  return -1;
}

function buildProgressSteps(code) {
  const currentIndex = getProgressIndex(code);
  return [
    { key: "waiting", label: "Chờ xác nhận", active: currentIndex >= 0 },
    { key: "confirmed", label: "Đã xác nhận", active: currentIndex >= 1 },
    { key: "process", label: "Đang xử lý", active: currentIndex >= 2 },
    { key: "shipping", label: "Đang giao", active: currentIndex >= 3 },
    { key: "delivered", label: "Đã giao", active: currentIndex >= 4 },
    { key: "done", label: "Hoàn thành", active: currentIndex >= 5 },
  ];
}

const showSelectedProgress = computed(() => {
  const code = Number(selectedOrder.value?.trangThaiDon);
  return !isSpecialOrderStatus(code) && code >= 0;
});

const selectedProgressSteps = computed(() => buildProgressSteps(selectedOrder.value?.trangThaiDon));

function statusClass(code) {
  const n = Number(code);
  if (n === 4) return "status-success";
  if (n === 5) return "status-danger";
  if (n === 2 || n === 3) return "status-info";
  return "status-primary";
}

function paymentStatusText(value, statusCode = null) {
  if (Number(statusCode) === 4) return "Đã thanh toán";
  const v = String(value || "").toUpperCase();
  if (v === "PAID") return "Đã thanh toán";
  if (v === "PENDING") return "Chờ thanh toán";
  return "Chưa thanh toán";
}

function paymentStatusClass(value, statusCode = null) {
  if (Number(statusCode) === 4) return "payment-success";
  const v = String(value || "").toUpperCase();
  if (v === "PAID") return "payment-success";
  if (v === "PENDING") return "payment-warning";
  return "payment-default";
}

function normalizeImg(url) {
  if (!url) return "";
  if (url.startsWith("http://") || url.startsWith("https://")) return url;
  if (url.startsWith("/")) return `http://localhost:8080${url}`;
  return `http://localhost:8080/uploads/${url}`;
}


function onImgError(e) {
  e.target.src =
    "data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='100' height='100'%3E%3Crect width='100%25' height='100%25' fill='%23eef2f7'/%3E%3Ctext x='50%25' y='50%25' dominant-baseline='middle' text-anchor='middle' fill='%2394a3b8' font-size='13'%3EẢnh%3C/text%3E%3C/svg%3E";
}

function syncSelectedIntoList() {
  if (!selectedOrder.value) return;

  const idx = orders.value.findIndex((x) => x.id === selectedOrder.value.id);
  if (idx === -1) return;

  const tongSanPham = Array.isArray(selectedOrder.value.items)
    ? selectedOrder.value.items.reduce((sum, item) => sum + Number(item.soLuong || 0), 0)
    : orders.value[idx].tongSanPham || 0;

  orders.value[idx] = {
    ...orders.value[idx],
    ...selectedOrder.value,
    tongSanPham,
  };
}

function openCancelModal() {
  if (!canCancelSelected.value) return;
  cancelModalOpen.value = true;
}

const cancelReason = ref("");
const cancelNote = ref("");

async function confirmCancelOrder() {
  if (!selectedOrder.value) return;

  try {
    const data = await cancelMyOrder(selectedOrder.value.id, {
      lyDo: cancelReason.value?.trim() || "",
      ghiChu: cancelNote.value?.trim() || "",
    });

    selectedOrder.value = data;
    cancelModalOpen.value = false;
    cancelReason.value = "";
    cancelNote.value = "";

    syncSelectedIntoList();
    showMessage("Gửi yêu cầu hủy đơn thành công.", "success");
  } catch (error) {
    showMessage(error?.response?.data?.message || error?.message || "Hủy đơn thất bại", "danger");
  }
}

async function loadProvinces() {
  try {
    provinceLoading.value = true;

    provinces.value = (Array.isArray(vnUnitsData) ? vnUnitsData : []).map((province) => ({
      code: province.Code,
      name: province.FullName,
      wards: (province.Wards || []).map((ward) => ({
        code: ward.Code,
        name: ward.FullName,
        provinceCode: ward.ProvinceCode,
      })),
    }));
  } catch (error) {
    console.error(error);
    provinces.value = [];
  } finally {
    provinceLoading.value = false;
  }
}

async function loadWardsByProvinceCode(provinceCode) {
  if (!provinceCode) {
    wards.value = [];
    return;
  }

  try {
    wardLoading.value = true;

    const province = provinces.value.find(
        (item) => String(item.code) === String(provinceCode)
    );

    wards.value = province?.wards || [];
  } catch (error) {
    console.error(error);
    wards.value = [];
  } finally {
    wardLoading.value = false;
  }
}

function normalizeText(value) {
  return String(value || "")
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .toLowerCase()
      .replace(/\s+/g, " ")
      .trim();
}

function findProvinceByCode(code) {
  return (
      provinces.value.find((item) => String(item.code) === String(code)) || null
  );
}

function findProvinceByName(name) {
  const normalized = normalizeText(name);

  return (
      provinces.value.find((item) => normalizeText(item.name) === normalized) ||
      null
  );
}

function findWardByCode(code) {
  return (
      wards.value.find((item) => String(item.code) === String(code)) || null
  );
}

function findWardByName(name) {
  const normalized = normalizeText(name);

  return (
      wards.value.find((item) => normalizeText(item.name) === normalized) || null
  );
}

function openProvinceDropdown() {
  if (!isSelectedOrderCod.value) return;

  provinceUserTyping.value = false;
  provinceDropdownOpen.value = true;
  wardDropdownOpen.value = false;
}

function openWardDropdown() {
  if (!isSelectedOrderCod.value || !selectedProvinceCode.value) return;

  wardUserTyping.value = false;
  wardDropdownOpen.value = true;
  provinceDropdownOpen.value = false;
}

function closeAddressDropdown(type) {
  setTimeout(() => {
    if (type === "province") provinceDropdownOpen.value = false;
    if (type === "ward") wardDropdownOpen.value = false;
  }, 150);
}

function onShippingProvinceSearch() {
  provinceUserTyping.value = true;
  provinceDropdownOpen.value = true;

  const matchedProvince = findProvinceByName(provinceSearch.value);

  if (!matchedProvince) {
    selectedProvinceCode.value = "";
    selectedWardCode.value = "";
    shippingForm.value.tinhThanhNhanHang = "";
    shippingForm.value.phuongXaNhanHang = "";
    wardSearch.value = "";
    wards.value = [];
  }
}

function onShippingWardSearch() {
  wardUserTyping.value = true;
  wardDropdownOpen.value = true;

  const matchedWard = findWardByName(wardSearch.value);

  if (!matchedWard) {
    selectedWardCode.value = "";
    shippingForm.value.phuongXaNhanHang = "";
  }
}

async function selectShippingProvince(province) {
  selectedProvinceCode.value = String(province.code || "");
  selectedWardCode.value = "";
  provinceSearch.value = province.name || "";
  wardSearch.value = "";
  shippingForm.value.tinhThanhNhanHang = province.name || "";
  shippingForm.value.phuongXaNhanHang = "";
  provinceDropdownOpen.value = false;
  wardDropdownOpen.value = false;
  provinceUserTyping.value = false;
  wardUserTyping.value = false;

  await loadWardsByProvinceCode(selectedProvinceCode.value);
}

function selectShippingWard(ward) {
  selectedWardCode.value = String(ward.code || "");
  wardSearch.value = ward.name || "";
  shippingForm.value.phuongXaNhanHang = ward.name || "";
  wardUserTyping.value = false;
  wardDropdownOpen.value = false;
}

async function openShippingModal() {
  if (!selectedOrder.value || !canEditShippingSelected.value) return;

  if (!provinces.value.length) {
    await loadProvinces();
  }

  shippingForm.value = {
    tenNguoiNhanHang: selectedOrder.value.tenNguoiNhanHang || "",
    soDienThoaiNhanHang: selectedOrder.value.soDienThoaiNhanHang || "",
    diaChiNhanHangChiTiet: selectedOrder.value.diaChiNhanHangChiTiet || "",
    phuongXaNhanHang: selectedOrder.value.phuongXaNhanHang || "",
    tinhThanhNhanHang: selectedOrder.value.tinhThanhNhanHang || "",
    ghiChu: selectedOrder.value.ghiChu || "",
  };

  provinceDropdownOpen.value = false;
  wardDropdownOpen.value = false;
  selectedProvinceCode.value = "";
  selectedWardCode.value = "";
  provinceSearch.value = shippingForm.value.tinhThanhNhanHang || "";
  wardSearch.value = shippingForm.value.phuongXaNhanHang || "";
  wards.value = [];

  const matchedProvince = findProvinceByName(shippingForm.value.tinhThanhNhanHang);

  if (matchedProvince?.code) {
    selectedProvinceCode.value = String(matchedProvince.code);
    provinceSearch.value = matchedProvince.name;
    shippingForm.value.tinhThanhNhanHang = matchedProvince.name;

    await loadWardsByProvinceCode(matchedProvince.code);

    const matchedWard = findWardByName(shippingForm.value.phuongXaNhanHang);

    if (matchedWard?.code) {
      selectedWardCode.value = String(matchedWard.code);
      wardSearch.value = matchedWard.name;
      shippingForm.value.phuongXaNhanHang = matchedWard.name;
    }
  }

  shippingModalOpen.value = true;
}

function closeShippingModal() {
  shippingModalOpen.value = false;
}

async function submitShipping() {
  if (!selectedOrder.value) return;

  try {
    shippingSaving.value = true;

    if (isSelectedOrderCod.value) {
      const province = findProvinceByCode(selectedProvinceCode.value);
      const ward = findWardByCode(selectedWardCode.value);

      if (!province) {
        showMessage("Vui lòng chọn tỉnh/thành từ danh sách.", "warning");
        return;
      }

      if (!ward) {
        showMessage("Vui lòng chọn phường/xã từ danh sách.", "warning");
        return;
      }

      shippingForm.value.tinhThanhNhanHang = province.name;
      shippingForm.value.phuongXaNhanHang = ward.name;
    }

    const payload = {
      tenNguoiNhanHang: shippingForm.value.tenNguoiNhanHang,
      soDienThoaiNhanHang: shippingForm.value.soDienThoaiNhanHang,
      diaChiNhanHangChiTiet: shippingForm.value.diaChiNhanHangChiTiet,
      phuongXaNhanHang: shippingForm.value.phuongXaNhanHang,
      tinhThanhNhanHang: shippingForm.value.tinhThanhNhanHang,
      ghiChu: shippingForm.value.ghiChu,
    };

    const { data } = await updateMyOrderShipping(selectedOrder.value.id, payload);
    selectedOrder.value = data;
    closeShippingModal();
    syncSelectedIntoList();
    showMessage("Cập nhật giao hàng thành công.", "success");
  } catch (e) {
    console.error(e);
    showMessage(e?.response?.data?.message || e?.message || "Cập nhật giao hàng thất bại", "danger");
  } finally {
    shippingSaving.value = false;
  }
}

function openItemsModal() {
  if (!selectedOrder.value || !canEditItemsSelected.value) return;

  itemDrafts.value = JSON.parse(JSON.stringify(selectedOrder.value.items || [])).map((item) => ({
    ...item,
    soLuong: Number(item.soLuong || 1),
    donGia: Number(item.donGia || 0),
    thanhTien: Number(item.thanhTien || 0),
  }));

  itemsModalOpen.value = true;
}

function recalcDraftItem(item) {
  item.thanhTien = Number(item.donGia || 0) * Number(item.soLuong || 0);
}

function increaseQty(item) {
  item.soLuong = Number(item.soLuong || 0) + 1;
  recalcDraftItem(item);
}

function decreaseQty(item) {
  const nextQty = Number(item.soLuong || 0) - 1;
  if (nextQty < 1) return;
  item.soLuong = nextQty;
  recalcDraftItem(item);
}

function removeDraftItem(index) {
  if (itemDrafts.value.length <= 1) {
    showMessage("Đơn hàng phải còn ít nhất 1 sản phẩm.", "warning");
    return;
  }

  itemDrafts.value.splice(index, 1);
}

function recalcOrderTotals(orderLike) {
  const tongTien = (orderLike.items || []).reduce((sum, item) => {
    return sum + Number(item.donGia || 0) * Number(item.soLuong || 0);
  }, 0);

  const tongTienGiam = Number(orderLike.tongTienGiam || 0);
  const phiVanChuyen = Number(orderLike.phiVanChuyen || 0);
  const tongTienSauGiam = tongTien - tongTienGiam + phiVanChuyen;

  return {
    ...orderLike,
    tongTien,
    tongTienSauGiam,
  };
}

function closeItemsModal() {
  itemsModalOpen.value = false;
}

async function submitItems() {
  if (!selectedOrder.value) return;

  if (!itemDrafts.value.length) {
    showMessage("Đơn hàng không được rỗng.", "warning");
    return;
  }

  try {
    itemsSaving.value = true;

    const payload = {
      items: itemDrafts.value.map((x) => ({
        idSanPhamChiTiet: x.idSanPhamChiTiet,
        soLuong: Number(x.soLuong),
      })),
    };

    const { data } = await updateMyOrderItems(selectedOrder.value.id, payload);
    selectedOrder.value = data;
    closeItemsModal();
    syncSelectedIntoList();
    showMessage("Cập nhật sản phẩm thành công.", "success");
  } catch (e) {
    console.error(e);
    showMessage(e?.response?.data?.message || e?.message || "Cập nhật sản phẩm thất bại", "danger");
  } finally {
    itemsSaving.value = false;
  }
}

async function loadOrders() {
  loading.value = true;
  errorMessage.value = "";

  try {
    const { data } = await getMyOrders();
    orders.value = Array.isArray(data) ? data : [];

    if (orders.value.length > 0) {
      await viewDetail(orders.value[0].id);
    }
  } catch (err) {
    errorMessage.value =
      err?.response?.data?.message || "Không tải được danh sách đơn hàng.";
  } finally {
    loading.value = false;
  }
}

async function viewDetail(id) {
  selectedOrderId.value = id;
  detailLoading.value = true;

  try {
    const { data } = await getMyOrderDetail(id);
    selectedOrder.value = data;
  } catch (err) {
    errorMessage.value =
      err?.response?.data?.message || "Không tải được chi tiết đơn hàng.";
  } finally {
    detailLoading.value = false;
  }
}

onMounted(async () => {
  await Promise.all([loadProvinces(), loadOrders()]);
});
</script>

<style scoped>
.my-orders-page {
  background: #f6f8fb;
  min-height: calc(100vh - 140px);
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #0f172a;
}

.page-desc {
  color: #64748b;
}

.filter-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-btn {
  border: 1px solid #dbe2ea;
  background: #fff;
  border-radius: 999px;
  padding: 10px 16px;
  font-weight: 600;
}

.filter-btn.active {
  background: #000f51;
  color: #fff;
  border-color: #000f51;
}

.order-card,
.detail-box,
.info-card {
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.08);
}

.order-card {
  padding: 20px;
  cursor: pointer;
  border: 2px solid transparent;
}

.order-card + .order-card {
  margin-top: 16px;
}

.order-card.active {
  border-color: #12379d;
}

.order-code,
.detail-code {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}

.order-date,
.detail-date {
  color: #64748b;
  margin-top: 6px;
}

.order-meta {
  color: #334155;
  line-height: 1.8;
}

.order-total {
  font-size: 20px;
  font-weight: 700;
  color: #dc2626;
}

.detail-box {
  padding: 24px;
}

.detail-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.info-card {
  padding: 20px;
}

.info-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 16px;
  color: #0f172a;
}

.info-title-payment {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px 18px;
}

.full-row {
  grid-column: 1 / -1;
}

.item-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.item-row {
  display: flex;
  gap: 14px;
  align-items: center;
  border-bottom: 1px solid #eef2f7;
  padding-bottom: 14px;
}

.item-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.item-image {
  width: 82px;
  height: 82px;
  object-fit: cover;
  border-radius: 14px;
  background: #f8fafc;
}

.item-content {
  flex: 1;
}

.item-name {
  font-weight: 700;
  color: #0f172a;
}

.item-meta {
  color: #64748b;
  margin-top: 4px;
}

.item-price {
  text-align: right;
  min-width: 130px;
}

.sum-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #eef2f7;
}

.sum-row.total {
  border-bottom: none;
  font-size: 18px;
  font-weight: 700;
  color: #dc2626;
}

.status-badge,
.payment-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  padding: 8px 14px;
  font-weight: 700;
  font-size: 13px;
}

.status-primary {
  background: #e8efff;
  color: #1242b3;
}

.status-info {
  background: #e0f2fe;
  color: #0369a1;
}

.status-success {
  background: #dcfce7;
  color: #166534;
}

.status-danger {
  background: #fee2e2;
  color: #b91c1c;
}

.payment-success {
  background: #dcfce7;
  color: #166534;
}

.payment-warning {
  background: #fef3c7;
  color: #92400e;
}

.payment-default {
  background: #eef2f7;
  color: #475569;
}

.progress-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 18px;
  padding: 18px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
}

.progress-line {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}

.progress-step {
  position: relative;
  text-align: center;
}

.progress-step:not(:first-child)::before {
  content: "";
  position: absolute;
  top: 8px;
  left: calc(-50% - 6px);
  width: calc(100% + 12px);
  height: 2px;
  background: #dbe2ea;
  z-index: 0;
}

.progress-step.active:not(:first-child)::before {
  background: #12379d;
}

.progress-step__dot {
  position: relative;
  z-index: 1;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #dbe2ea;
  margin: 0 auto 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 11px;
  font-weight: 900;
}

.progress-step.active .progress-step__dot {
  background: #12379d;
}

.progress-step__label {
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
}

.progress-step.active .progress-step__label {
  color: #0f172a;
  font-weight: 700;
}

.detail-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}

.action-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
}

.mock-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 16px;
}

.mock-modal {
  width: 720px;
  max-width: 100%;
  background: #fff;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 24px 50px rgba(15, 23, 42, 0.24);
}

.mock-modal--wide {
  width: 980px;
}

.mock-modal__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 18px;
  background: #000f51;
  color: #fff;
}

.mock-modal__title {
  font-size: 18px;
  font-weight: 700;
}

.mock-modal__close {
  border: none;
  background: transparent;
  color: #fff;
  font-size: 28px;
  line-height: 1;
}

.mock-modal__body {
  padding: 18px;
  max-height: 70vh;
  overflow-y: auto;
}

.mock-modal__footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 18px 18px;
  border-top: 1px solid #eef2f7;
}

.draft-item-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.draft-item-row {
  display: grid;
  grid-template-columns: 90px 1fr auto auto auto;
  gap: 14px;
  align-items: center;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 12px;
}

.draft-item-image {
  width: 82px;
  height: 82px;
  object-fit: cover;
  border-radius: 12px;
  background: #f8fafc;
}

.draft-item-name {
  font-weight: 700;
  color: #0f172a;
}

.draft-item-meta {
  color: #64748b;
  margin-top: 4px;
}

.draft-qty-box {
  display: flex;
  align-items: center;
  gap: 8px;
}

.draft-qty {
  min-width: 28px;
  text-align: center;
  font-weight: 700;
}

.draft-item-price {
  min-width: 110px;
  text-align: right;
}

@media (max-width: 768px) {
  .progress-line {
    grid-template-columns: repeat(2, 1fr);
  }

  .detail-actions {
    align-items: stretch;
    width: 100%;
  }

  .action-row {
    justify-content: flex-start;
  }

  .draft-item-row {
    grid-template-columns: 1fr;
  }

  .draft-item-price {
    text-align: left;
  }
}

@media (max-width: 767.98px) {
  .page-title {
    font-size: 26px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .item-row {
    align-items: flex-start;
  }

  .item-price {
    min-width: unset;
  }
}

.address-select-wrap {
  position: relative;
}

.address-dropdown {
  position: absolute;
  left: 0;
  right: 0;
  top: calc(100% + 4px);
  z-index: 10000;
  max-height: 220px;
  overflow-y: auto;
  background: #fff;
  border: 1px solid #dbe2ea;
  border-radius: 10px;
  box-shadow: 0 16px 35px rgba(15, 23, 42, 0.16);
  padding: 6px;
}

.address-option {
  width: 100%;
  border: none;
  background: transparent;
  text-align: left;
  padding: 9px 10px;
  border-radius: 8px;
  color: #0f172a;
  font-size: 14px;
}

.address-option:hover {
  background: #eef4ff;
  color: #12379d;
}

.address-empty {
  padding: 10px;
  color: #64748b;
  font-size: 14px;
}
</style>