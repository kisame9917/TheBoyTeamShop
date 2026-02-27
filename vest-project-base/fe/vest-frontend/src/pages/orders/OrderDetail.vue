<template>
  <div class="container-fluid py-3">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-file-text fs-4"></i>
        <h5 class="mb-0">Chi tiết hóa đơn</h5>
      </div>

      <button
        type="button"
        class="btn btn-outline-secondary btn-sm"
        @click="goBack"
      >
        <i class="bi bi-arrow-left me-1"></i> Quay lại
      </button>
    </div>

    <!-- Stepper -->
    <div class="order-stepper mb-3" v-if="hd">
      <div
        class="order-stepper__track"
        :style="{
          '--progress': progressPercent,
          '--steps': stepperSteps.length,
        }"
      >
        <div
          v-for="s in stepperSteps"
          :key="s.code"
          class="os-step"
          :class="stepStateClass(s.code)"
        >
          <!-- chữ nằm trên line -->
          <div class="os-label">
            <div class="os-text">{{ s.label }}</div>
            <div class="os-time" v-if="s.timeText">{{ s.timeText }}</div>
          </div>

          <!-- circle nằm đúng trên thanh progress -->
          <div class="os-circle">
            <i v-if="isDoneStep(s.code)" class="bi bi-check-lg"></i>
            <span v-else class="os-dot"></span>
          </div>
        </div>
      </div>
    </div>

    <!-- Actions -->
    <div
      class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3"
    >
      <div class="d-flex flex-wrap gap-2">
        <button
          type="button"
          class="btn btn-primary btn-sm"
          :disabled="!canAdvanceStatus"
          @click="openConfirmAdvanceModal"
        >
          <i class="bi bi-arrow-right-circle me-1"></i>
          {{ advanceBtnText }}
        </button>

        <button
          v-if="canCancel"
          type="button"
          class="btn btn-outline-danger btn-sm"
          @click="openConfirmCancelModal"
        >
          <i class="bi bi-x-circle me-1"></i> Huỷ đơn
        </button>

        <button
          v-if="canRequestRefund"
          type="button"
          class="btn btn-outline-warning btn-sm text-dark"
          @click="openConfirmRefundModal"
        >
          <i class="bi bi-arrow-counterclockwise me-1"></i> Hoàn đơn
        </button>
      </div>

      <div class="d-flex gap-2">
        <button
          type="button"
          class="btn btn-outline-secondary btn-sm"
          @click="openHistoryModal"
        >
          <i class="bi bi-clock-history me-1"></i> Chi tiết
        </button>

        <button
          type="button"
          class="btn btn-outline-success btn-sm"
          @click="openPrintModal"
        >
          <i class="bi bi-printer me-1"></i> Xuất hóa đơn
        </button>
      </div>
    </div>

    <!-- Order Info (professional) -->
    <div class="card shadow-sm mb-3">
      <div class="card-body">
        <div
          class="d-flex align-items-start justify-content-between flex-wrap gap-3"
        >
          <div>
            <div class="d-flex align-items-center flex-wrap gap-2">
              <h6 class="mb-0">Thông tin đơn hàng</h6>
              <span class="badge" :class="statusBadgeClass(hd?.trangThaiDon)">
                {{ statusLabel(hd?.trangThaiDon) }}
              </span>
              <span class="badge text-bg-light border">
                {{ orderTypeText }}
              </span>
            </div>

            <div class="text-muted small mt-1">
              Mã: <b>{{ hd?.maHoaDon || "-" }}</b>
              <span class="mx-1">•</span>
              Tạo lúc: <b>{{ formatDateTimeVN(hd?.ngayTao) || "-" }}</b>
              <span class="mx-1">•</span>
              NV xử lý: <b>{{ staffCode }}</b> - <b>{{ staffName }}</b>
            </div>
          </div>

          <div class="text-end">
            <div class="text-muted small">Tổng thanh toán</div>
            <div class="fs-5 fw-bold text-danger">
              {{ formatCurrency(hd?.tongTienSauGiam) }}
            </div>
            <div class="text-muted small">
              Đã thanh toán: <b>{{ formatCurrency(paidTotal) }}</b>
            </div>
          </div>
        </div>

        <hr class="my-3" />

        <div class="row g-3">
          <div class="col-12 col-lg-4">
            <div class="info-box h-100">
              <div class="info-title">
                <i class="bi bi-person me-2"></i>Khách hàng
              </div>

              <div class="info-row">
                <span>Họ tên</span>
                <b>{{ hd?.tenKhachHang || "Khách lẻ" }}</b>
              </div>
              <div class="info-row">
                <span>SĐT</span>
                <b>{{ hd?.soDienThoai || "-" }}</b>
              </div>
              <div class="info-row">
                <span>Email</span>
                <b
                  class="text-end text-truncate d-inline-block"
                  style="max-width: 210px"
                >
                  {{ hd?.emailKhachHang || "-" }}
                </b>
              </div>
            </div>
          </div>

          <div class="col-12 col-lg-5">
            <div class="info-box h-100">
              <div class="info-title">
                <i class="bi bi-truck me-2"></i>Giao nhận
              </div>

              <div class="info-row align-items-start">
                <span>Địa chỉ</span>
                <b class="text-end">
                  {{ hd?.diaChiKhachHang || "-" }}
                </b>
              </div>

              <div class="info-row">
                <span>Phí vận chuyển</span>
                <b>{{ formatCurrency(hd?.phiVanChuyen) }}</b>
              </div>

              <div class="info-row align-items-start">
                <span>Ghi chú</span>
                <b class="text-end">{{ hd?.ghiChu || "-" }}</b>
              </div>
            </div>
          </div>

          <div class="col-12 col-lg-3">
            <div class="info-box h-100">
              <div class="info-title">
                <i class="bi bi-cash-coin me-2"></i>Giá trị đơn
              </div>

              <div class="info-row">
                <span>Tổng tiền</span>
                <b>{{ formatCurrency(hd?.tongTien) }}</b>
              </div>
              <div class="info-row">
                <span>Giảm giá</span>
                <b>{{ formatCurrency(hd?.tongTienGiam) }}</b>
              </div>
              <div class="info-row">
                <span>Phải trả</span>
                <b class="text-danger">{{
                  formatCurrency(hd?.tongTienSauGiam)
                }}</b>
              </div>
            </div>
          </div>
        </div>

        <!-- Quick summary chips -->
        <div class="summary-grid mt-3">
          <div class="summary-item">
            <div class="text-muted small">Trạng thái hiện tại</div>
            <div class="fw-semibold">{{ statusLabel(hd?.trangThaiDon) }}</div>
          </div>
          <div class="summary-item">
            <div class="text-muted small">Số SP</div>
            <div class="fw-semibold">{{ (hd?.items || []).length }}</div>
          </div>
          <div class="summary-item">
            <div class="text-muted small">Đơn giá trị</div>
            <div class="fw-semibold">{{ formatCurrency(hd?.tongTien) }}</div>
          </div>
          <div class="summary-item">
            <div class="text-muted small">Còn lại</div>
            <div class="fw-semibold">
              {{
                formatCurrency(
                  Math.max(
                    0,
                    Number(hd?.tongTienSauGiam ?? 0) - Number(paidTotal ?? 0),
                  ),
                )
              }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Payment history -->
    <div class="card shadow-sm mb-3">
      <div class="card-body">
        <div class="d-flex align-items-center justify-content-between mb-2">
          <h6 class="mb-0">Lịch sử thanh toán</h6>
        </div>

        <div class="table-responsive table-wrap">
          <table class="table table-fixed align-middle mb-0">
            <thead class="thead-dark-custom">
              <tr>
                <th class="col-money">Số tiền</th>
                <th class="col-time">Thời gian</th>
                <th class="col-code">Mã giao dịch</th>
                <th class="col-method">Phương thức</th>
                <th class="col-note">Ghi chú</th>
              </tr>
            </thead>

            <tbody>
              <tr v-if="(hd?.lichSuThanhToan || []).length === 0">
                <td colspan="5" class="text-center text-muted py-4">
                  Chưa có thanh toán
                </td>
              </tr>

              <tr v-for="p in hd?.lichSuThanhToan || []" :key="p.id">
                <td class="fw-semibold">{{ formatCurrency(p.soTien) }}</td>
                <td>{{ formatDateTimeVN(p.ngayThanhToan) }}</td>
                <td class="text-truncate">{{ p.maGiaoDich || "-" }}</td>
                <td class="text-truncate">{{ p.hinhThucThanhToan || "-" }}</td>
                <td class="text-truncate">{{ p.ghiChu || "-" }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

   <!-- Items -->
<div class="card shadow-sm">
  <div class="card-body">
    <h6 class="mb-2">Sản phẩm</h6>

    <div class="table-responsive table-wrap">
      <table class="table table-fixed align-middle mb-0">
        <thead class="thead-dark-custom">
          <tr>
            <th class="col-stt">STT</th>
            <th class="col-img">Ảnh</th>
            <th class="col-product">Sản phẩm</th>
            <th class="col-qty">Số lượng</th>
            <th class="col-total">Thành tiền</th>
          </tr>
        </thead>

        <tbody>
          <tr v-if="items.length === 0">
            <td colspan="5" class="text-center text-muted py-4">
              Không có sản phẩm
            </td>
          </tr>

          <tr
            v-for="(it, idx) in pagedItems"
            :key="(currentPage - 1) * pageSize + idx"
          >
            <td>{{ (currentPage - 1) * pageSize + idx + 1 }}</td>

            <td>
              <div class="img-box">
                <img v-if="it.anhDaiDien" :src="it.anhDaiDien" alt="img" />
                <div v-else class="text-muted small">No image</div>
              </div>
            </td>

            <td>
              <div class="fw-semibold text-truncate">{{ it.tenSanPham || "-" }}</div>
              <div class="text-muted small text-truncate">
                {{ it.mauSac || "-" }} - {{ it.kichCo || "-" }}
                <span v-if="it.maSanPhamChiTiet"> • {{ it.maSanPhamChiTiet }}</span>
              </div>
              <div class="text-danger fw-semibold mt-1">
                {{ formatCurrency(it.donGia) }}
              </div>
            </td>

            <td class="fw-semibold">{{ it.soLuong }}</td>

            <td class="fw-semibold text-danger">
              {{ formatCurrency(it.thanhTien) }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- ✅ PHÂN TRANG: đặt ở đây (ngoài table-wrap, trong card-body) -->
    <nav v-if="items.length > pageSize" class="mt-3 d-flex justify-content-end">
      <ul class="pagination pagination-sm mb-0">
        <li class="page-item" :class="{ disabled: currentPage === 1 }">
          <button class="page-link" @click="prevPage">‹</button>
        </li>

        <li class="page-item disabled">
          <span class="page-link">
            Trang {{ currentPage }} / {{ totalPages }}
          </span>
        </li>

        <li class="page-item" :class="{ disabled: currentPage === totalPages }">
          <button class="page-link" @click="nextPage">›</button>
        </li>
      </ul>
    </nav>
  </div>
</div>
    <!-- History modal -->
    <div
      class="modal fade"
      id="historyModal"
      tabindex="-1"
      aria-hidden="true"
      ref="historyModalRef"
    >
      <div class="modal-dialog modal-xl modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h6 class="modal-title">Chi tiết lịch sử</h6>
            <button
              type="button"
              class="btn-close"
              aria-label="Close"
              @click="closeHistoryModal"
            ></button>
          </div>

          <div class="modal-body">
            <div class="table-responsive">
              <table class="table align-middle">
                <thead class="table-light">
                  <tr>
                    <th style="width: 160px">Trạng thái</th>
                    <th style="width: 180px">Thời gian</th>
                    <th style="width: 140px">Mã NV</th>
                    <th style="width: 220px">Tên NV</th>
                    <th style="width: 220px">Hành động</th>
                    <th>Mô tả</th>
                  </tr>
                </thead>

                <tbody>
                  <tr v-if="(hd?.lichSuHoaDon || []).length === 0">
                    <td colspan="6" class="text-center text-muted py-4">
                      Chưa có lịch sử
                    </td>
                  </tr>

                  <tr v-for="h in hd?.lichSuHoaDon || []" :key="h.id">
                    <td>
                      <span class="badge text-bg-light border">
                        {{ mapHistoryToStatusLabel(h.hanhDong) }}
                      </span>
                    </td>
                    <td>{{ formatDateTimeVN(h.thoiGian) }}</td>
                    <td class="text-truncate">{{ historyStaffCode(h) }}</td>
                    <td class="text-truncate">{{ historyStaffName(h) }}</td>
                    <td class="fw-semibold">{{ h.hanhDong || "-" }}</td>
                    <td>{{ h.ghiChu || "-" }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-light"
              @click="closeHistoryModal"
            >
              Đóng
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Confirm Action Modal (advance/cancel/refund) -->
    <div
      class="modal fade"
      id="confirmActionModal"
      tabindex="-1"
      aria-hidden="true"
      ref="confirmActionModalRef"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h6 class="modal-title">
              <i class="bi bi-exclamation-triangle me-2"></i>{{ confirmTitle }}
            </h6>
            <button
              type="button"
              class="btn-close"
              aria-label="Close"
              @click="closeConfirmActionModal"
            ></button>
          </div>

          <div class="modal-body">
            <div class="mb-2">
              Hóa đơn: <b>{{ hd?.maHoaDon }}</b>
            </div>

            <div class="mb-2">
              Trạng thái hiện tại:
              <span class="badge bg-secondary">{{
                statusLabel(hd?.trangThaiDon)
              }}</span>
              <i class="bi bi-arrow-right mx-2"></i>
              Trạng thái mới:
              <span class="badge bg-primary">{{
                statusLabel(confirmTargetStatus)
              }}</span>
            </div>

            <div class="small text-muted">
              {{ confirmDesc }}
            </div>
          </div>

          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-light"
              @click="closeConfirmActionModal"
            >
              Hủy
            </button>
            <button
              type="button"
              class="btn btn-primary text-dark"
              @click="confirmDoAction"
            >
              Xác nhận
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Print modal (thermal receipt) -->
    <div
      class="modal fade"
      id="printModal"
      tabindex="-1"
      aria-hidden="true"
      ref="printModalRef"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h6 class="modal-title">Xem trước hóa đơn (in nhiệt)</h6>
            <button
              type="button"
              class="btn-close"
              aria-label="Close"
              @click="closePrintModal"
            ></button>
          </div>

          <div class="modal-body">
            <div ref="printAreaRef" class="receipt">
              <div class="center bold big">VEST SHOP</div>
              <div class="center">
                37 Đ. Nguyễn Văn Huyên, Quan Hoa, Cầu Giấy
              </div>
              <div class="center">Hà Nội, Việt Nam</div>
              <div class="center">Hotline: 0968038313</div>
              <div class="hr"></div>
              <div class="center bold">HÓA ĐƠN BÁN HÀNG</div>
              <div class="hr"></div>

              <div class="row2">
                <div>Ngày: {{ formatDateOnlyVN(hd?.ngayTao) }}</div>
                <div>Số: {{ hd?.maHoaDon || "-" }}</div>
              </div>
              <div class="row2">
                <div>In lúc: {{ formatTimeOnlyVN(new Date()) }}</div>
                <div>Loại: {{ hd?.loaiDon ? "Online" : "Tại quầy" }}</div>
              </div>

              <div class="mt6">Khách: {{ hd?.tenKhachHang || "Khách lẻ" }}</div>
              <div class="mt2">SĐT: {{ hd?.soDienThoai || "-" }}</div>
              <div class="mt2">Đ/c: {{ hd?.diaChiKhachHang || "-" }}</div>

              <div class="hr"></div>

              <div class="items-head">
                <div class="w-name bold">Tên hàng</div>
                <div class="w-qty bold right">SL</div>
                <div class="w-price bold right">Tiền</div>
              </div>

              <div v-for="(it, idx) in hd?.items || []" :key="idx" class="item">
                <div class="w-name">
                  <div class="bold">{{ it.tenSanPham || "-" }}</div>
                  <div class="muted small">
                    {{ it.mauSac || "-" }} / {{ it.kichCo || "-" }}
                    <span v-if="it.maSanPhamChiTiet">
                      • {{ it.maSanPhamChiTiet }}</span
                    >
                  </div>
                  <div class="small">
                    ĐG: {{ formatMoneyNumber(it.donGia) }} đ
                  </div>
                </div>
                <div class="w-qty right">{{ it.soLuong ?? 0 }}</div>
                <div class="w-price right">
                  {{ formatMoneyNumber(it.thanhTien) }} đ
                </div>
              </div>

              <div class="hr"></div>

              <div class="row2">
                <div>Tổng tiền</div>
                <div class="right">{{ formatMoneyNumber(hd?.tongTien) }} đ</div>
              </div>
              <div class="row2">
                <div>Giảm giá</div>
                <div class="right">
                  {{ formatMoneyNumber(hd?.tongTienGiam) }} đ
                </div>
              </div>
              <div class="row2">
                <div>Phí vận chuyển</div>
                <div class="right">
                  {{ formatMoneyNumber(hd?.phiVanChuyen) }} đ
                </div>
              </div>

              <div class="hr"></div>

              <div class="row2 bold">
                <div>TỔNG THANH TOÁN</div>
                <div class="right">
                  {{ formatMoneyNumber(hd?.tongTienSauGiam) }} đ
                </div>
              </div>

              <div class="hr"></div>

              <div class="center mt8">
                <img v-if="qrDataUrl" :src="qrDataUrl" class="qr" alt="qr" />
                <div class="small muted mt2">
                  Quét để tra cứu: {{ hd?.maHoaDon }}
                </div>
              </div>

              <div class="center mt10">Cảm ơn quý khách!</div>
            </div>
          </div>

          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-light"
              @click="closePrintModal"
            >
              Đóng
            </button>
            <button
              type="button"
              class="btn btn-warning text-dark"
              @click="printInvoice"
            >
              In / Lưu PDF
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Toast -->
    <div
      class="toast-container position-fixed top-0 end-0 p-3"
      style="z-index: 9999"
    >
      <div
        class="toast align-items-center text-bg-success border-0"
        ref="toastRef"
        role="alert"
        aria-live="assertive"
        aria-atomic="true"
      >
        <div class="d-flex">
          <div class="toast-body">{{ toastMsg }}</div>
          <button
            type="button"
            class="btn-close btn-close-white me-2 m-auto"
            @click="hideToast"
          ></button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, nextTick } from "vue";
import { useRouter, useRoute } from "vue-router";
import QRCode from "qrcode";
import hoaDonApi from "@/services/hoaDonApi";

const props = defineProps({
  id: { type: [String, Number], required: true },
});
const router = useRouter();
const route = useRoute();

const hd = ref(null);

/** ====== STATUS LABELS ====== */
function statusLabel(code) {
  if (code === null || code === undefined || code === "") return "-";
  const m = {
    0: "Chờ xác nhận",
    1: "Đang xử lý",
    2: "Đang giao",
    3: "Đã giao",
    4: "Hoàn thành",
    5: "Đã huỷ",
    6: "Yêu cầu hoàn",
    7: "Đã hoàn",
  };
  return m[Number(code)] ?? "-";
}

function statusBadgeClass(code) {
  if (code === null || code === undefined || code === "")
    return "text-bg-secondary";
  switch (Number(code)) {
    case 4:
      return "text-bg-success";
    case 3:
      return "text-bg-primary";
    case 2:
      return "text-bg-info";
    case 1:
      return "text-bg-warning";
    case 0:
      return "text-bg-secondary";
    case 5:
      return "text-bg-dark";
    case 6:
      return "text-bg-warning";
    case 7:
      return "text-bg-secondary";
    default:
      return "text-bg-secondary";
  }
}

/** ====== FORMATTERS ====== */
function formatMoneyNumber(v) {
  const n = Number(v ?? 0);
  return n.toLocaleString("vi-VN");
}
function formatCurrency(v) {
  return formatMoneyNumber(v) + " đ";
}
function formatDateTimeVN(isoDateTime) {
  if (!isoDateTime) return "";
  const d = new Date(isoDateTime);
  const hh = String(d.getHours()).padStart(2, "0");
  const mi = String(d.getMinutes()).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const yyyy = d.getFullYear();
  return `${hh}:${mi} ${dd}/${mm}/${yyyy}`;
}
function formatDateOnlyVN(isoDateTime) {
  if (!isoDateTime) return "";
  const d = new Date(isoDateTime);
  const dd = String(d.getDate()).padStart(2, "0");
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const yyyy = d.getFullYear();
  return `${dd}/${mm}/${yyyy}`;
}
function formatTimeOnlyVN(dateObj) {
  const d = dateObj instanceof Date ? dateObj : new Date();
  const hh = String(d.getHours()).padStart(2, "0");
  const mi = String(d.getMinutes()).padStart(2, "0");
  return `${hh}:${mi}`;
}

/** ====== DATA FETCH ====== */
async function fetchDetail() {
  try {
    const res = await hoaDonApi.detail(props.id);
    hd.value = res.data;
    currentPage.value = 1;
  } catch (e) {
    console.error(e);
    showToast("Không tải được chi tiết hóa đơn!");
  }
}

function goBack() {
  router.back();
}

/** ====== COMPUTEDS ====== */
const currentStatus = computed(() => Number(hd.value?.trangThaiDon ?? -1));

const orderTypeText = computed(() => {
  return hd.value?.loaiDon ? "Đơn hàng Online" : "Đơn hàng Tại quầy";
});

const paidTotal = computed(() => {
  const list = hd.value?.lichSuThanhToan || [];
  return list.reduce((s, p) => s + Number(p?.soTien ?? 0), 0);
});
/** ===== Pagination items (10 per page) ===== */
const pageSize = 10;
const currentPage = ref(1);

const items = computed(() => hd.value?.items || []);

const totalPages = computed(() => {
  const len = items.value.length;
  return Math.max(1, Math.ceil(len / pageSize));
});

const pagedItems = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return items.value.slice(start, start + pageSize);
});

function goToPage(p) {
  const tp = totalPages.value;
  currentPage.value = Math.min(tp, Math.max(1, p));
}

function prevPage() {
  goToPage(currentPage.value - 1);
}
function nextPage() {
  goToPage(currentPage.value + 1);
}
/** ===== NV xử lý đơn (Mã, tên) ===== */
const staffCode = computed(() => {
  const v = hd.value;
  return (
    v?.nhanVienXuLy?.maNhanVien ??
    v?.maNhanVienXuLy ??
    v?.nhanVien?.maNhanVien ??
    v?.maNhanVien ??
    "-"
  );
});

const staffName = computed(() => {
  const v = hd.value;
  return (
    v?.nhanVienXuLy?.tenNhanVien ??
    v?.tenNhanVienXuLy ??
    v?.nhanVien?.tenNhanVien ??
    v?.tenNhanVien ??
    "-"
  );
});

/**
 * Advance logic:
 * 0 -> 1 -> 2 -> 3 -> 4
 * 6 -> 7 (refund flow)
 */
const nextStatus = computed(() => {
  const map = { 0: 1, 1: 2, 2: 3, 3: 4, 6: 7 };
  return map[currentStatus.value] ?? null;
});

const canAdvanceStatus = computed(() => nextStatus.value !== null);

const advanceBtnText = computed(() => {
  const ns = nextStatus.value;
  if (ns === null) return "Đổi trạng thái đơn";
  return `Đổi trạng thái: ${statusLabel(ns)}`;
});

const canCancel = computed(() => [0, 1, 2].includes(currentStatus.value));
const canRequestRefund = computed(() => [3, 4].includes(currentStatus.value));

/** ===== STEPPER (auto steps) ===== */
const baseStepper = [
  { code: 0, label: "Chờ xác nhận đơn" },
  { code: 1, label: "Đang xử lý đơn hàng" },
  { code: 2, label: "Đang giao" },
  { code: 3, label: "Đã giao" },
  { code: 4, label: "Hoàn thành" },
];

const actionToStepCode = (hanhDong) => {
  const m = {
    CHO_XAC_NHAN: 0,
    DANG_XU_LY: 1,
    DANG_GIAO: 2,
    DA_GIAO: 3,
    HOAN_THANH: 4,
  };
  return m[hanhDong];
};

const stepCodes = computed(() => baseStepper.map((s) => s.code));

/**
 * step hiện tại để tô stepper (0..4)
 * nếu trạng thái là hủy/hoàn (5/6/7) thì lấy “bước cao nhất” từ lịch sử để hiển thị hợp lý
 */
const currentStepIndex = computed(() => {
  const st = Number(hd.value?.trangThaiDon ?? -1);

  if (stepCodes.value.includes(st)) return stepCodes.value.indexOf(st);

  if ([5, 6, 7].includes(st)) {
    const history = hd.value?.lichSuHoaDon || [];
    const codes = history
      .map((h) => actionToStepCode(h.hanhDong))
      .filter((c) => stepCodes.value.includes(c));
    if (codes.length) return stepCodes.value.indexOf(Math.max(...codes));
  }

  return -1;
});

const stepperSteps = computed(() => {
  const history = hd.value?.lichSuHoaDon || [];
  const latestTimeByStep = (code) => {
    const matched = history
      .filter((h) => actionToStepCode(h.hanhDong) === code && h.thoiGian)
      .sort((a, b) => new Date(b.thoiGian) - new Date(a.thoiGian));
    return matched[0]?.thoiGian || null;
  };

  return baseStepper.map((s) => ({
    ...s,
    timeText: latestTimeByStep(s.code)
      ? formatDateTimeVN(latestTimeByStep(s.code))
      : "",
  }));
});

// progress theo index (0..n-1) => 0..100
const progressPercent = computed(() => {
  const idx = currentStepIndex.value;
  if (idx < 0) return 0;

  const n = baseStepper.length; // số step
  return (idx / (n - 1)) * 100;
});
const isLastStep = computed(
  () => currentStepIndex.value === baseStepper.length - 1,
);

const isDoneStep = (code) => {
  const idx = currentStepIndex.value;
  if (idx < 0) return false;

  const codeIdx = stepCodes.value.indexOf(code);

  // Nếu đang ở bước cuối (Hoàn thành) => bước cuối cũng xem như DONE để hiện tick
  if (isLastStep.value) return codeIdx <= idx;

  // Còn lại: chỉ những bước nhỏ hơn bước hiện tại
  return codeIdx < idx;
};

const isCurrentStep = (code) => {
  const codeIdx = stepCodes.value.indexOf(code);

  // Nếu là bước cuối rồi thì không cần "current" nữa (để nó tick luôn)
  if (isLastStep.value) return false;

  return codeIdx === currentStepIndex.value;
};
const stepStateClass = (code) => {
  if (isDoneStep(code)) return "is-done";
  if (isCurrentStep(code)) return "is-current";
  return "is-todo";
};
/** ====== CONFIRM ACTION MODAL (ONE FOR ALL) ====== */
const confirmActionModalRef = ref(null);
let bsConfirmActionModal = null;

const confirmTitle = ref("Xác nhận");
const confirmDesc = ref("");
const confirmTargetStatus = ref(null);
const confirmNote = ref("");

function openConfirmActionModal({ title, desc, targetStatus, note }) {
  if (!hd.value) return;

  confirmTitle.value = title;
  confirmDesc.value = desc;
  confirmTargetStatus.value = targetStatus;
  confirmNote.value = note;

  const el = confirmActionModalRef.value;
  if (!el) return;

  const Modal = window.bootstrap?.Modal;
  if (Modal) {
    bsConfirmActionModal = Modal.getOrCreateInstance(el, {
      backdrop: true,
      keyboard: true,
    });
    bsConfirmActionModal.show();
  } else {
    el.classList.add("show");
    el.style.display = "block";
    el.removeAttribute("aria-hidden");
    document.body.classList.add("modal-open");
    const backdrop = document.createElement("div");
    backdrop.className = "modal-backdrop fade show";
    document.body.appendChild(backdrop);
  }
}

function closeConfirmActionModal() {
  const el = confirmActionModalRef.value;
  if (!el) return;

  if (bsConfirmActionModal) {
    bsConfirmActionModal.hide();
  } else {
    el.classList.remove("show");
    el.style.display = "none";
    el.setAttribute("aria-hidden", "true");
    document.body.classList.remove("modal-open");
    document.querySelectorAll(".modal-backdrop").forEach((b) => b.remove());
  }
}

async function confirmDoAction() {
  try {
    closeConfirmActionModal();

    await hoaDonApi.changeStatus(props.id, {
      trangThaiDon: confirmTargetStatus.value,
      ghiChu: confirmNote.value || "Cập nhật trạng thái",
    });

    await fetchDetail();
    showToast("Cập nhật trạng thái thành công!");
  } catch (e) {
    console.error(e);
    showToast("Cập nhật trạng thái thất bại!");
  }
}

/** Buttons handlers */
function openConfirmAdvanceModal() {
  const ns = nextStatus.value;
  if (ns === null) return;

  openConfirmActionModal({
    title: "Xác nhận đổi trạng thái",
    desc: "Hành động này sẽ cập nhật trạng thái và ghi lịch sử hóa đơn.",
    targetStatus: ns,
    note: `Chuyển trạng thái: ${statusLabel(currentStatus.value)} -> ${statusLabel(ns)}`,
  });
}

function openConfirmCancelModal() {
  openConfirmActionModal({
    title: "Xác nhận huỷ đơn",
    desc: "Đơn sẽ chuyển sang trạng thái 'Đã huỷ'. Hãy chắc chắn trước khi thực hiện.",
    targetStatus: 5,
    note: "Huỷ đơn",
  });
}

function openConfirmRefundModal() {
  openConfirmActionModal({
    title: "Xác nhận hoàn đơn",
    desc: "Đơn sẽ chuyển sang trạng thái 'Yêu cầu hoàn'. Sau đó bấm 'Đổi trạng thái đơn' để xác nhận 'Đã hoàn'.",
    targetStatus: 6,
    note: "Yêu cầu hoàn đơn",
  });
}

/** ===== History Modal ===== */
const historyModalRef = ref(null);
let bsHistoryModal = null;

function openHistoryModal() {
  const el = historyModalRef.value;
  if (!el) return;
  const Modal = window.bootstrap?.Modal;
  if (Modal) {
    bsHistoryModal = Modal.getOrCreateInstance(el, {
      backdrop: true,
      keyboard: true,
    });
    bsHistoryModal.show();
  } else {
    el.classList.add("show");
    el.style.display = "block";
    el.removeAttribute("aria-hidden");
    document.body.classList.add("modal-open");
    const backdrop = document.createElement("div");
    backdrop.className = "modal-backdrop fade show";
    document.body.appendChild(backdrop);
  }
}

function closeHistoryModal() {
  const el = historyModalRef.value;
  if (!el) return;
  if (bsHistoryModal) bsHistoryModal.hide();
  else {
    el.classList.remove("show");
    el.style.display = "none";
    el.setAttribute("aria-hidden", "true");
    document.body.classList.remove("modal-open");
    document.querySelectorAll(".modal-backdrop").forEach((b) => b.remove());
  }
}

function mapHistoryToStatusLabel(hanhDong) {
  if (!hanhDong) return "-";
  const m = {
    CHO_XAC_NHAN: "Chờ xác nhận",
    DANG_XU_LY: "Đang xử lý",
    DANG_GIAO: "Đang giao",
    DA_GIAO: "Đã giao",
    HOAN_THANH: "Hoàn thành",
    DA_HUY: "Đã huỷ",
    YEU_CAU_HOAN: "Yêu cầu hoàn",
    DA_HOAN: "Đã hoàn",
  };
  return m[hanhDong] || hanhDong;
}

/** Lịch sử: Mã NV + Tên NV thao tác */
const historyStaffCode = (h) =>
  h?.maNhanVien ??
  h?.nhanVien?.maNhanVien ??
  h?.nhanVienThaoTac?.maNhanVien ??
  "-";

const historyStaffName = (h) =>
  h?.tenNhanVien ??
  h?.nhanVien?.tenNhanVien ??
  h?.nhanVienThaoTac?.tenNhanVien ??
  "-";

/** ===== Print Modal + QR ===== */
const printModalRef = ref(null);
const printAreaRef = ref(null);
let bsPrintModal = null;
const qrDataUrl = ref("");

async function openPrintModal() {
  if (!hd.value) return;

  try {
    qrDataUrl.value = await QRCode.toDataURL(String(hd.value.maHoaDon || ""), {
      margin: 1,
      width: 220,
    });
  } catch {
    qrDataUrl.value = "";
  }

  const el = printModalRef.value;
  if (!el) return;

  const Modal = window.bootstrap?.Modal;
  if (Modal) {
    bsPrintModal = Modal.getOrCreateInstance(el, {
      backdrop: true,
      keyboard: true,
    });
    bsPrintModal.show();
  } else {
    el.classList.add("show");
    el.style.display = "block";
    el.removeAttribute("aria-hidden");
    document.body.classList.add("modal-open");
    const backdrop = document.createElement("div");
    backdrop.className = "modal-backdrop fade show";
    document.body.appendChild(backdrop);
  }
}

function closePrintModal() {
  const el = printModalRef.value;
  if (!el) return;
  if (bsPrintModal) bsPrintModal.hide();
  else {
    el.classList.remove("show");
    el.style.display = "none";
    el.setAttribute("aria-hidden", "true");
    document.body.classList.remove("modal-open");
    document.querySelectorAll(".modal-backdrop").forEach((b) => b.remove());
  }
}

function printInvoice() {
  const area = printAreaRef.value;
  if (!area) return;

  const w = window.open("", "_blank");
  if (!w) return;

  w.document.write(`
    <html>
      <head>
        <title>HoaDon_${hd.value?.maHoaDon}</title>
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
          .mt10{ margin-top:10px; }
          .items-head, .item{ display:flex; gap:6px; font-size:12px; }
          .w-name{ flex: 1; }
          .w-qty{ width: 10mm; }
          .w-price{ width: 22mm; }
          .qr{ width:28mm; height:28mm; }
          @media print { @page { margin: 0; } }
        </style>
      </head>
      <body>${area.outerHTML}</body>
    </html>
  `);

  w.document.close();
  w.focus();
  w.onafterprint = () => w.close();
  w.print();

  showToast("Đã mở cửa sổ in / lưu PDF!");
}

/** Toast */
const toastRef = ref(null);
const toastMsg = ref("");
let bsToast = null;

function showToast(msg) {
  toastMsg.value = msg;
  const el = toastRef.value;
  if (!el) return;
  const Toast = window.bootstrap?.Toast;
  if (Toast) {
    bsToast = Toast.getOrCreateInstance(el, { delay: 2000 });
    bsToast.show();
  }
}
function hideToast() {
  try {
    bsToast?.hide?.();
  } catch {}
}

/** Mount */
onMounted(async () => {
  await fetchDetail();

  if (route.query.print === "1") {
    await nextTick();
    await openPrintModal();
    const { print, ...rest } = route.query;
    router.replace({ query: rest });
  }
});
</script>

<style scoped>
.card {
  border-radius: 14px;
}

/* Info blocks */
.info-box {
  border: 1px solid #eef1f5;
  border-radius: 14px;
  padding: 12px 12px;
  background: #fbfcfe;
}
.info-title {
  font-weight: 700;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}
.info-row {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  padding: 6px 0;
  border-top: 1px dashed #e8edf3;
}
.info-row:first-of-type {
  border-top: none;
  padding-top: 0;
}
.info-row span {
  color: #6c757d;
  font-size: 12px;
  min-width: 90px;
}

/* Summary line */
.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}
.summary-item {
  border: 1px solid #eef1f5;
  border-radius: 12px;
  padding: 10px 12px;
  background: #fff;
}
@media (max-width: 992px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

/* Tables */
.table-wrap {
  border: 1px solid #dee2e6;
  border-radius: 12px;
  overflow: hidden;
}
.table-fixed {
  table-layout: fixed;
  width: 100%;
}
.thead-dark-custom th {
  background-color: #1f2a44 !important;
  color: #fff !important;
  border-color: rgba(255, 255, 255, 0.15) !important;
}
.table td,
.table th {
  border-color: #e9ecef;
  vertical-align: middle;
}
.text-truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Payment columns (even) */
.col-money {
  width: 16%;
}
.col-time {
  width: 18%;
}
.col-code {
  width: 20%;
}
.col-method {
  width: 20%;
}
.col-note {
  width: 26%;
}

/* Items columns (even) */
.col-stt {
  width: 6%;
}
.col-img {
  width: 12%;
}
.col-product {
  width: 52%;
}
.col-qty {
  width: 12%;
}
.col-total {
  width: 18%;
}

.img-box {
  width: 84px;
  height: 64px;
  border: 1px solid #eee;
  border-radius: 10px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}
.img-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* Receipt preview */
.receipt {
  width: 80mm;
  margin: 0 auto;
  font-family: Arial, sans-serif;
  font-size: 12px;
}
.receipt .center {
  text-align: center;
}
.receipt .right {
  text-align: right;
}
.receipt .bold {
  font-weight: 700;
}
.receipt .big {
  font-size: 16px;
}
.receipt .small {
  font-size: 11px;
}
.receipt .muted {
  color: #555;
}
.receipt .hr {
  border-top: 1px dashed #000;
  margin: 6px 0;
}
.receipt .row2 {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}
.receipt .mt2 {
  margin-top: 2px;
}
.receipt .mt6 {
  margin-top: 6px;
}
.receipt .mt8 {
  margin-top: 8px;
}
.receipt .mt10 {
  margin-top: 10px;
}
.receipt .items-head,
.receipt .item {
  display: flex;
  gap: 6px;
}
.receipt .w-name {
  flex: 1;
}
.receipt .w-qty {
  width: 10mm;
}
.receipt .w-price {
  width: 22mm;
}
.receipt .qr {
  width: 28mm;
  height: 28mm;
}

/* ===== Stepper: chữ ở trên thanh progress xanh (AUTO steps) ===== */
.order-stepper {
  background: #fff;
  border: 1px solid #e6e9ef;
  border-radius: 12px;
  padding: 12px 14px;
  box-shadow: 0 8px 22px rgba(31, 42, 68, 0.06);
}

.order-stepper__track {
  --progress: 0; /* 0..100 (JS set) */
  --steps: 4; /* stepperSteps.length sẽ override */
  --label-h: 44px; /* chiều cao vùng chữ */
  --gap: 8px; /* khoảng cách chữ -> circle */
  --dot: 30px;
  --dot-half: 15px;

  position: relative;
  display: grid;
  grid-template-columns: repeat(var(--steps), minmax(0, 1fr));
  align-items: start;
  padding: 6px 8px 6px;
}

/* line nền: chạy từ tâm step 1 tới tâm step cuối */
.order-stepper__track::before {
  content: "";
  position: absolute;
  left: calc(100% / (2 * var(--steps)));
  right: calc(100% / (2 * var(--steps)));
  top: calc(var(--label-h) + var(--gap) + var(--dot-half));
  height: 4px;
  border-radius: 999px;
  background: #e9ecef;
  z-index: 0;
}

/* line progress xanh: co theo số step */
.order-stepper__track::after {
  content: "";
  position: absolute;
  left: calc(100% / (2 * var(--steps)));
  top: calc(var(--label-h) + var(--gap) + var(--dot-half));
  height: 4px;
  width: calc((100% - (100% / var(--steps))) * var(--progress) / 100);
  border-radius: 999px;
  background: linear-gradient(90deg, #0d6efd, #20c997);
  box-shadow: 0 10px 18px rgba(13, 110, 253, 0.15);
  z-index: 0;
}

.os-step {
  position: relative;
  z-index: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* chữ nằm trên line */
.os-label {
  height: var(--label-h);
  text-align: center;
  padding: 0 6px;
  overflow: hidden;
}

.os-text {
  font-weight: 700;
  font-size: 13px;
  color: #1f2a44;
  line-height: 1.1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.os-time {
  margin-top: 3px;
  font-size: 11px;
  color: #8a93a1;
  line-height: 1.1;
  min-height: 14px;
}

/* circle nằm đúng trên thanh progress */
.os-circle {
  margin-top: var(--gap);
  width: var(--dot);
  height: var(--dot);
  border-radius: 999px;
  border: 2px solid #cfd4da;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #adb5bd;
  box-shadow: 0 10px 22px rgba(0, 0, 0, 0.06);
  flex: 0 0 auto;
}

.os-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: currentColor;
  opacity: 0.9;
}

/* states */
.os-step.is-done .os-circle {
  background: #0d6efd;
  border-color: #0d6efd;
  color: #fff;
}

/* current: vòng rỗng + chấm xanh + halo (giống ảnh) */
.os-step.is-current .os-circle {
  background: #fff;
  border-color: #0d6efd;
  color: #0d6efd;
  box-shadow:
    0 0 0 6px rgba(13, 110, 253, 0.14),
    0 14px 28px rgba(13, 110, 253, 0.22);
}
.os-step.is-current .os-dot {
  background: #0d6efd;
}

.os-step.is-todo .os-text {
  color: #6c757d;
  font-weight: 600;
}
.os-step.is-todo .os-circle {
  background: #f7f8fa;
}

/* responsive: nhỏ thì wrap và tắt line cho gọn */
@media (max-width: 992px) {
  .order-stepper__track {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px 0;
  }
  .order-stepper__track::before,
  .order-stepper__track::after {
    display: none;
  }
  .os-text {
    white-space: normal;
  }
}
</style>
