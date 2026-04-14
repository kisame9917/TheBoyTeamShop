<template>
  <div class="my-orders-page py-4 py-lg-5">
    <div class="container">
      <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
        <div>
          <h1 class="page-title mb-1">Đơn hàng của tôi</h1>
          <p class="page-desc mb-0">
            Trang này chỉ dành cho khách đã đăng nhập. Khách chưa đăng nhập vẫn dùng trang tra cứu công khai.
          </p>
        </div>

        <router-link :to="{ name: 'OrderLookup' }" class="btn btn-outline-secondary">
          Tra cứu đơn hàng công khai
        </router-link>
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
                <span class="payment-badge ms-2" :class="paymentStatusClass(item.paymentStatus)">
                  {{ paymentStatusText(item.paymentStatus) }}
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
                  <div class="info-title">Thanh toán</div>

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
                    <strong>
                      {{ selectedOrder.paymentMethod || "-" }}
                      <span class="payment-badge ms-2" :class="paymentStatusClass(selectedOrder.paymentStatus)">
                        {{ paymentStatusText(selectedOrder.paymentStatus) }}
                      </span>
                    </strong>
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
          <input
            v-model.trim="shippingForm.tinhThanhNhanHang"
            class="form-control"
            type="text"
            :disabled="!isSelectedOrderCod"
          />
        </div>

        <div class="col-12 col-md-6">
          <label class="form-label">Quận / Huyện</label>
          <input
            v-model.trim="shippingForm.quanHuyenNhanHang"
            class="form-control"
            type="text"
            :disabled="!isSelectedOrderCod"
          />
        </div>

        <div class="col-12 col-md-6">
          <label class="form-label">Phường / Xã</label>
          <input
            v-model.trim="shippingForm.phuongXaNhanHang"
            class="form-control"
            type="text"
            :disabled="!isSelectedOrderCod"
          />
        </div>

        <div class="col-12 col-md-6">
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
      <button class="btn btn-primary" type="button" @click="submitShippingMock">Lưu thay đổi</button>
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
      <button class="btn btn-dark" type="button" @click="submitItemsMock">Lưu thay đổi</button>
    </div>
  </div>
  
</div>
<ChatWidget />
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { getMyOrderDetail, getMyOrders, cancelMyOrder } from "../../services/Api";
import ConfirmModal from "../../components/common/ConfirmModal.vue";
import ChatWidget from '../../components/ClientChatWidget.vue';
import {
  canCancelOrder,
  canEditShipping,
  canEditItems,
  isCod,
} from "../../utils/orderPermissions";

const loading = ref(true);
const detailLoading = ref(false);
const errorMessage = ref("");
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
  quanHuyenNhanHang: "",
  tinhThanhNhanHang: "",
  ghiChu: "",
});

const itemDrafts = ref([]);

const canCancelSelected = computed(() => canCancelOrder(selectedOrder.value));
const canEditShippingSelected = computed(() => canEditShipping(selectedOrder.value));
const canEditItemsSelected = computed(() => canEditItems(selectedOrder.value));
const isSelectedOrderCod = computed(() => isCod(selectedOrder.value));

const filterOptions = [
  { label: "Tất cả", value: "ALL" },
  { label: "Chờ xác nhận", value: 0 },
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
    o?.quanHuyenNhanHang,
    o?.tinhThanhNhanHang,
  ]
    .filter(Boolean)
    .join(", ");
}

function statusClass(code) {
  const n = Number(code);
  if (n === 4) return "status-success";
  if (n === 5) return "status-danger";
  if (n === 2 || n === 3) return "status-info";
  return "status-primary";
}

function paymentStatusText(value) {
  const v = String(value || "").toUpperCase();
  if (v === "PAID") return "Đã thanh toán";
  if (v === "PENDING") return "Chờ thanh toán";
  return "Chưa thanh toán";
}

function paymentStatusClass(value) {
  const v = String(value || "").toUpperCase();
  if (v === "PAID") return "payment-success";
  if (v === "PENDING") return "payment-warning";
  return "payment-default";
}

function normalizeImg(url) {
  if (!url) return "";
  if (url.startsWith("http://") || url.startsWith("https://")) return url;
  return `http://localhost:8080${url}`;
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
    window.alert("Gửi yêu cầu hủy đơn thành công.");
  } catch (error) {
    window.alert(error?.message || "Hủy đơn thất bại");
  }
}

function openShippingModal() {
  if (!selectedOrder.value || !canEditShippingSelected.value) return;

  shippingForm.value = {
    tenNguoiNhanHang: selectedOrder.value.tenNguoiNhanHang || "",
    soDienThoaiNhanHang: selectedOrder.value.soDienThoaiNhanHang || "",
    diaChiNhanHangChiTiet: selectedOrder.value.diaChiNhanHangChiTiet || "",
    phuongXaNhanHang: selectedOrder.value.phuongXaNhanHang || "",
    quanHuyenNhanHang: selectedOrder.value.quanHuyenNhanHang || "",
    tinhThanhNhanHang: selectedOrder.value.tinhThanhNhanHang || "",
    ghiChu: selectedOrder.value.ghiChu || "",
  };

  shippingModalOpen.value = true;
}

function submitShippingMock() {
  if (!selectedOrder.value) return;

  const nextOrder = {
    ...selectedOrder.value,
    tenNguoiNhanHang: shippingForm.value.tenNguoiNhanHang,
    soDienThoaiNhanHang: shippingForm.value.soDienThoaiNhanHang,
    ghiChu: shippingForm.value.ghiChu,
  };

  // COD: cho sửa full địa chỉ
  if (isSelectedOrderCod.value) {
    nextOrder.diaChiNhanHangChiTiet = shippingForm.value.diaChiNhanHangChiTiet;
    nextOrder.phuongXaNhanHang = shippingForm.value.phuongXaNhanHang;
    nextOrder.quanHuyenNhanHang = shippingForm.value.quanHuyenNhanHang;
    nextOrder.tinhThanhNhanHang = shippingForm.value.tinhThanhNhanHang;
  }

  selectedOrder.value = nextOrder;
  shippingModalOpen.value = false;
  syncSelectedIntoList();

  window.alert("Đã cập nhật thông tin giao hàng ở FE (mock).");
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
    window.alert("Đơn hàng phải còn ít nhất 1 sản phẩm.");
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

function submitItemsMock() {
  if (!selectedOrder.value) return;

  if (!itemDrafts.value.length) {
    window.alert("Đơn hàng không được rỗng.");
    return;
  }

  const normalizedItems = itemDrafts.value.map((item) => ({
    ...item,
    soLuong: Number(item.soLuong || 0),
    donGia: Number(item.donGia || 0),
    thanhTien: Number(item.donGia || 0) * Number(item.soLuong || 0),
  }));

  selectedOrder.value = recalcOrderTotals({
    ...selectedOrder.value,
    items: normalizedItems,
  });

  itemsModalOpen.value = false;
  syncSelectedIntoList();

  window.alert("Đã cập nhật sản phẩm trong đơn ở FE (mock).");
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

onMounted(loadOrders);
</script>

<style scoped>
.my-orders-page {
  background: #f6f8fb;
  min-height: calc(100vh - 140px);
}

.page-title {
  font-size: 32px;
  font-weight: 800;
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
  font-weight: 800;
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
  font-weight: 800;
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
  font-weight: 800;
  margin-bottom: 16px;
  color: #0f172a;
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
  font-weight: 800;
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
  font-weight: 800;
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
</style>