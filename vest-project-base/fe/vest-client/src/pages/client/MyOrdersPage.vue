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
              <span class="status-badge" :class="statusClass(selectedOrder.trangThaiDon)">
                {{ selectedOrder.tenTrangThaiDon }}
              </span>
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
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { getMyOrderDetail, getMyOrders } from "../../services/Api";

const loading = ref(true);
const detailLoading = ref(false);
const errorMessage = ref("");
const orders = ref([]);
const selectedOrder = ref(null);
const selectedOrderId = ref(null);
const activeFilter = ref("ALL");

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