<template>
  <div class="container-fluid py-3">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-file-text fs-4"></i>
        <h5 class="mb-0">Chi tiết hóa đơn</h5>
      </div>

      <div class="d-flex align-items-center gap-2">
        <button type="button" class="btn btn-outline-secondary btn-sm" @click="goBack">
          <i class="bi bi-arrow-left me-1"></i> Quay lại
        </button>
      </div>
    </div>

    <!-- Stepper -->
    <div class="card shadow-sm mb-3">
      <div class="card-body">
        <div class="d-flex flex-wrap gap-3 align-items-center">
          <div v-for="st in stepper" :key="st.code" class="step-item">
            <div class="d-flex align-items-center gap-2">
              <div class="step-dot" :class="{ active: isStepActive(st.code) }">
                <i v-if="isStepActive(st.code)" class="bi bi-check-lg"></i>
              </div>
              <div>
                <div class="fw-semibold">{{ st.label }}</div>
                <div class="text-muted small">{{ st.timeText }}</div>
              </div>
            </div>
            <div class="step-line"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Actions -->
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div>
        <button type="button" class="btn btn-primary btn-sm" :disabled="!canConfirmDeliver" @click="confirmDeliver">
          Xác nhận đã giao hàng
        </button>
      </div>

      <div class="d-flex gap-2">
        <button type="button" class="btn btn-outline-secondary btn-sm" @click="openHistoryModal">
          Chi tiết
        </button>
        <button type="button" class="btn btn-outline-success btn-sm" @click="openPrintModal">
          Xuất hóa đơn
        </button>
      </div>
    </div>

    <!-- Info -->
    <div class="card shadow-sm mb-3">
      <div class="card-body">
        <div class="d-flex align-items-center justify-content-between">
          <h6 class="mb-0">Thông tin đơn hàng - {{ hd?.loaiDon ? "Đơn hàng Online" : "Đơn hàng Tại quầy" }}</h6>
          <span class="badge" :class="statusBadgeClass(hd?.trangThaiDon)">
            {{ hd?.tenTrangThaiDon || "-" }}
          </span>
        </div>
        <hr />

        <div class="row g-3">
          <div class="col-12 col-lg-6">
            <div class="mb-2">
              <span class="text-muted">Mã:</span>
              <span class="fw-semibold">{{ hd?.maHoaDon }}</span>
            </div>
            <div class="mb-2">
              <span class="text-muted">Tên khách hàng:</span>
              {{ hd?.tenKhachHang || "Khách lẻ" }}
            </div>
            <div class="mb-2">
              <span class="text-muted">Trạng thái:</span>
              {{ hd?.tenTrangThaiDon }}
            </div>
            <div class="mb-2">
              <span class="text-muted">Ghi chú:</span>
              {{ hd?.ghiChu || "-" }}
            </div>
          </div>

          <div class="col-12 col-lg-6">
            <div class="mb-2"><span class="text-muted">SĐT người nhận:</span> {{ hd?.soDienThoai || "-" }}</div>
            <div class="mb-2"><span class="text-muted">Địa chỉ người nhận:</span> {{ hd?.diaChiKhachHang || "-" }}</div>
            <div class="mb-2"><span class="text-muted">Email:</span> {{ hd?.emailKhachHang || "-" }}</div>

            <div class="d-flex align-items-center gap-2 mt-3">
              <select class="form-select form-select-sm" style="max-width: 260px" v-model.number="changeStatusCode">
                <option v-for="s in statusOptions" :key="s.code" :value="s.code">
                  {{ s.label }}
                </option>
              </select>

              <!-- ✅ ĐỔI: bấm cập nhật sẽ mở popup confirm -->
              <button
                type="button"
                class="btn btn-outline-primary btn-sm"
                :disabled="!hd"
                @click="openConfirmStatusModal"
              >
                Cập nhật
              </button>
            </div>
          </div>
        </div>

        <hr />
        <div class="row g-3">
          <div class="col-12 col-lg-3">
            <div class="text-muted small">Tổng tiền</div>
            <div class="fw-semibold">{{ formatCurrency(hd?.tongTien) }}</div>
          </div>
          <div class="col-12 col-lg-3">
            <div class="text-muted small">Giảm</div>
            <div class="fw-semibold">{{ formatCurrency(hd?.tongTienGiam) }}</div>
          </div>
          <div class="col-12 col-lg-3">
            <div class="text-muted small">Phí vận chuyển</div>
            <div class="fw-semibold">{{ formatCurrency(hd?.phiVanChuyen) }}</div>
          </div>
          <div class="col-12 col-lg-3">
            <div class="text-muted small">Tổng sau giảm</div>
            <div class="fw-semibold">{{ formatCurrency(hd?.tongTienSauGiam) }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Payment history -->
    <div class="card shadow-sm mb-3">
      <div class="card-body">
        <h6 class="mb-3">Lịch sử thanh toán</h6>
        <div class="table-responsive">
          <table class="table align-middle">
            <thead class="table-light">
              <tr>
                <th style="width: 140px">Số tiền</th>
                <th style="width: 180px">Thời gian</th>
                <th style="width: 160px">Mã giao dịch</th>
                <th>Phương thức thanh toán</th>
                <th>Ghi chú</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="(hd?.lichSuThanhToan || []).length === 0">
                <td colspan="5" class="text-center text-muted py-4">Chưa có thanh toán</td>
              </tr>
              <tr v-for="p in (hd?.lichSuThanhToan || [])" :key="p.id">
                <td class="fw-semibold">{{ formatCurrency(p.soTien) }}</td>
                <td>{{ formatDateTimeVN(p.ngayThanhToan) }}</td>
                <td>{{ p.maGiaoDich || "-" }}</td>
                <td>{{ p.hinhThucThanhToan || "-" }}</td>
                <td>{{ p.ghiChu || "-" }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Items -->
    <div class="card shadow-sm">
      <div class="card-body">
        <h6 class="mb-3">Sản phẩm</h6>
        <div class="table-responsive">
          <table class="table align-middle">
            <thead class="table-light">
              <tr>
                <th style="width: 60px">#</th>
                <th style="width: 110px">Ảnh</th>
                <th>Sản phẩm</th>
                <th style="width: 120px">Số lượng</th>
                <th style="width: 160px">Thành tiền</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="(hd?.items || []).length === 0">
                <td colspan="5" class="text-center text-muted py-4">Không có sản phẩm</td>
              </tr>
              <tr v-for="(it, idx) in (hd?.items || [])" :key="idx">
                <td>{{ idx + 1 }}</td>
                <td>
                  <div class="img-box">
                    <img v-if="it.anhDaiDien" :src="it.anhDaiDien" alt="img" />
                    <div v-else class="text-muted small">No image</div>
                  </div>
                </td>
                <td>
                  <div class="fw-semibold">{{ it.tenSanPham || "-" }}</div>
                  <div class="text-muted small">
                    {{ it.mauSac || "-" }} - {{ it.kichCo || "-" }}
                    <span v-if="it.maSanPhamChiTiet"> • {{ it.maSanPhamChiTiet }}</span>
                  </div>
                  <div class="text-danger fw-semibold mt-1">{{ formatCurrency(it.donGia) }}</div>
                </td>
                <td>{{ it.soLuong }}</td>
                <td class="fw-semibold text-danger">{{ formatCurrency(it.thanhTien) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- History modal -->
    <div class="modal fade" id="historyModal" tabindex="-1" aria-hidden="true" ref="historyModalRef">
      <div class="modal-dialog modal-xl modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h6 class="modal-title">Chi tiết lịch sử</h6>
            <button type="button" class="btn-close" aria-label="Close" @click="closeHistoryModal"></button>
          </div>

          <div class="modal-body">
            <div class="table-responsive">
              <table class="table align-middle">
                <thead class="table-light">
                  <tr>
                    <th style="width: 160px">Trạng thái</th>
                    <th style="width: 180px">Thời gian</th>
                    <th style="width: 220px">Hành động</th>
                    <th>Mô tả</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="(hd?.lichSuHoaDon || []).length === 0">
                    <td colspan="4" class="text-center text-muted py-4">Chưa có lịch sử</td>
                  </tr>

                  <tr v-for="h in (hd?.lichSuHoaDon || [])" :key="h.id">
                    <td>
                      <span class="badge text-bg-light border">
                        {{ mapHistoryToStatusLabel(h.hanhDong) }}
                      </span>
                    </td>
                    <td>{{ formatDateTimeVN(h.thoiGian) }}</td>
                    <td class="fw-semibold">{{ h.hanhDong || "-" }}</td>
                    <td>{{ h.ghiChu || "-" }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-light" @click="closeHistoryModal">Cancel</button>
            <button type="button" class="btn btn-primary" @click="closeHistoryModal">OK</button>
          </div>
        </div>
      </div>
    </div>

    <!-- ✅ Confirm Change Status Modal -->
    <div class="modal fade" id="confirmStatusModal" tabindex="-1" aria-hidden="true" ref="confirmStatusModalRef">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h6 class="modal-title">
              <i class="bi bi-exclamation-triangle me-2"></i>Xác nhận đổi trạng thái
            </h6>
            <button type="button" class="btn-close" aria-label="Close" @click="closeConfirmStatusModal"></button>
          </div>

          <div class="modal-body">
            <div class="mb-2">
              Bạn muốn đổi trạng thái hóa đơn
              <b>{{ hd?.maHoaDon }}</b>
              từ
              <span class="badge bg-secondary">{{ hd?.tenTrangThaiDon || "-" }}</span>
              sang
              <span class="badge bg-primary">{{ statusLabel(changeStatusCode) }}</span>
              ?
            </div>

            <div class="small text-muted">
              Hành động này sẽ cập nhật trạng thái và ghi lịch sử hóa đơn.
            </div>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-light" @click="closeConfirmStatusModal">Hủy</button>
            <button type="button" class="btn btn-primary" @click="confirmChangeStatus">
              Xác nhận
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Print modal (thermal receipt) -->
    <div class="modal fade" id="printModal" tabindex="-1" aria-hidden="true" ref="printModalRef">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h6 class="modal-title">Xem trước hóa đơn (in nhiệt)</h6>
            <button type="button" class="btn-close" aria-label="Close" @click="closePrintModal"></button>
          </div>

          <div class="modal-body">
            <div ref="printAreaRef" class="receipt">
              <div class="center bold big">VEST SHOP</div>
              <div class="center">37 Đ. Nguyễn Văn Huyên, Quan Hoa, Cầu Giấy</div>
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

              <div v-for="(it, idx) in (hd?.items || [])" :key="idx" class="item">
                <div class="w-name">
                  <div class="bold">{{ it.tenSanPham || "-" }}</div>
                  <div class="muted small">
                    {{ it.mauSac || "-" }} / {{ it.kichCo || "-" }}
                    <span v-if="it.maSanPhamChiTiet"> • {{ it.maSanPhamChiTiet }}</span>
                  </div>
                  <div class="small">ĐG: {{ formatMoneyNumber(it.donGia) }} đ</div>
                </div>
                <div class="w-qty right">{{ it.soLuong ?? 0 }}</div>
                <div class="w-price right">{{ formatMoneyNumber(it.thanhTien) }} đ</div>
              </div>

              <div class="hr"></div>

              <div class="row2">
                <div>Tổng tiền</div>
                <div class="right">{{ formatMoneyNumber(hd?.tongTien) }} đ</div>
              </div>
              <div class="row2">
                <div>Giảm giá</div>
                <div class="right">{{ formatMoneyNumber(hd?.tongTienGiam) }} đ</div>
              </div>
              <div class="row2">
                <div>Phí vận chuyển</div>
                <div class="right">{{ formatMoneyNumber(hd?.phiVanChuyen) }} đ</div>
              </div>

              <div class="hr"></div>

              <div class="row2 bold">
                <div>TỔNG THANH TOÁN</div>
                <div class="right">{{ formatMoneyNumber(hd?.tongTienSauGiam) }} đ</div>
              </div>

              <div class="hr"></div>

              <div class="center mt8">
                <img v-if="qrDataUrl" :src="qrDataUrl" class="qr" alt="qr" />
                <div class="small muted mt2">Quét để tra cứu: {{ hd?.maHoaDon }}</div>
              </div>

              <div class="center mt10">Cảm ơn quý khách!</div>
            </div>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-light" @click="closePrintModal">Đóng</button>
            <button type="button" class="btn btn-warning text-dark" @click="printInvoice">
              In / Lưu PDF
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Toast -->
    <div class="toast-container position-fixed top-0 end-0 p-3" style="z-index: 9999">
      <div
        class="toast align-items-center text-bg-success border-0"
        ref="toastRef"
        role="alert"
        aria-live="assertive"
        aria-atomic="true"
      >
        <div class="d-flex">
          <div class="toast-body">{{ toastMsg }}</div>
          <button type="button" class="btn-close btn-close-white me-2 m-auto" @click="hideToast"></button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import QRCode from "qrcode";
import hoaDonApi from "@/services/hoaDonApi";

const props = defineProps({
  id: { type: [String, Number], required: true },
});

const router = useRouter();

const hd = ref(null);
const changeStatusCode = ref(0);

const statusOptions = [
  { code: 0, label: "Chờ xác nhận" },
  { code: 1, label: "Đang xử lý" },
  { code: 2, label: "Đang giao" },
  { code: 3, label: "Đã giao" },
  { code: 4, label: "Hoàn thành" },
  { code: 5, label: "Đã huỷ" },
  { code: 6, label: "Yêu cầu hoàn" },
  { code: 7, label: "Đã hoàn" },
];

const stepper = computed(() => {
  const list = [
    { code: 0, label: "Hóa đơn chờ" },
    { code: 1, label: "Đang xử lý" },
    { code: 2, label: "Đang giao" },
    { code: 3, label: "Đã giao" },
    { code: 4, label: "Hoàn thành" },
  ];

  const history = hd.value?.lichSuHoaDon || [];
  const mapActionToCode = (hanhDong) => {
    const m = {
      CHO_XAC_NHAN: 0,
      DANG_XU_LY: 1,
      DANG_GIAO: 2,
      DA_GIAO: 3,
      HOAN_THANH: 4,
    };
    return m[hanhDong];
  };

  const byCodeTime = (code) => {
    const found = history.find((h) => mapActionToCode(h.hanhDong) === code);
    return found?.thoiGian || null;
  };

  return list.map((x) => ({
    ...x,
    timeText: byCodeTime(x.code) ? formatDateTimeVN(byCodeTime(x.code)) : "",
  }));
});

const canConfirmDeliver = computed(() => Number(hd.value?.trangThaiDon ?? -1) === 2);

function isStepActive(code) {
  const st = Number(hd.value?.trangThaiDon ?? 0);
  return st >= code;
}

function statusBadgeClass(code) {
  switch (Number(code)) {
    case 4: return "text-bg-success";
    case 3: return "text-bg-primary";
    case 2: return "text-bg-info";
    case 1: return "text-bg-warning";
    case 0: return "text-bg-secondary";
    case 5: return "text-bg-dark";
    case 6: return "text-bg-warning";
    case 7: return "text-bg-secondary";
    default: return "text-bg-secondary";
  }
}

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

async function fetchDetail() {
  const res = await hoaDonApi.detail(props.id);
  hd.value = res.data;
  changeStatusCode.value = Number(hd.value?.trangThaiDon ?? 0);
}

function goBack() {
  router.back();
}

async function confirmDeliver() {
  await hoaDonApi.changeStatus(props.id, { trangThaiDon: 3, ghiChu: "Xác nhận đã giao hàng" });
  await fetchDetail();
  showToast("Cập nhật trạng thái thành công!");
}

/** ====== ĐỔI TRẠNG THÁI: POPUP CONFIRM ====== */
const confirmStatusModalRef = ref(null);
let bsConfirmStatusModal = null;

function statusLabel(code) {
  const found = statusOptions.find((x) => Number(x.code) === Number(code));
  return found?.label || String(code ?? "-");
}

function openConfirmStatusModal() {
  if (!hd.value) return;

  // nếu không đổi trạng thái thì khỏi hỏi
  if (Number(changeStatusCode.value) === Number(hd.value?.trangThaiDon)) {
    showToast("Trạng thái không thay đổi!");
    return;
  }

  const el = confirmStatusModalRef.value;
  if (!el) return;

  const Modal = window.bootstrap?.Modal;
  if (Modal) {
    bsConfirmStatusModal = Modal.getOrCreateInstance(el, { backdrop: true, keyboard: true });
    bsConfirmStatusModal.show();
  } else {
    el.classList.add("show");
    el.style.display = "block";
    el.removeAttribute("aria-hidden");
    document.body.classList.add("modal-open");

    // tạo backdrop fallback
    const backdrop = document.createElement("div");
    backdrop.className = "modal-backdrop fade show";
    document.body.appendChild(backdrop);
  }
}

function closeConfirmStatusModal() {
  const el = confirmStatusModalRef.value;
  if (!el) return;

  if (bsConfirmStatusModal) {
    bsConfirmStatusModal.hide();
  } else {
    el.classList.remove("show");
    el.style.display = "none";
    el.setAttribute("aria-hidden", "true");
    document.body.classList.remove("modal-open");
    document.querySelectorAll(".modal-backdrop").forEach((b) => b.remove());
  }
}

async function confirmChangeStatus() {
  try {
    closeConfirmStatusModal();

    await hoaDonApi.changeStatus(props.id, {
      trangThaiDon: changeStatusCode.value,
      ghiChu: "Cập nhật trạng thái",
    });

    await fetchDetail();
    showToast("Cập nhật trạng thái thành công!");
  } catch (e) {
    console.error(e);
  }
}

/** ===== History Modal (open/close chắc chắn) ===== */
const historyModalRef = ref(null);
let bsHistoryModal = null;

function openHistoryModal() {
  const el = historyModalRef.value;
  if (!el) return;

  const Modal = window.bootstrap?.Modal;
  if (Modal) {
    bsHistoryModal = Modal.getOrCreateInstance(el, { backdrop: true, keyboard: true });
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

  if (bsHistoryModal) {
    bsHistoryModal.hide();
  } else {
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

/** ===== Print Modal + QR ===== */
const printModalRef = ref(null);
const printAreaRef = ref(null);
let bsPrintModal = null;

const qrDataUrl = ref("");

async function openPrintModal() {
  if (!hd.value) return;

  qrDataUrl.value = await QRCode.toDataURL(String(hd.value.maHoaDon || ""), { margin: 1, width: 220 });

  const el = printModalRef.value;
  if (!el) return;

  const Modal = window.bootstrap?.Modal;
  if (Modal) {
    bsPrintModal = Modal.getOrCreateInstance(el, { backdrop: true, keyboard: true });
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

  if (bsPrintModal) {
    bsPrintModal.hide();
  } else {
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
  w.print(); // Ctrl+P -> Save as PDF
  w.close();

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
  try { bsToast?.hide?.(); } catch {}
}

onMounted(() => {
  fetchDetail();
});
</script>

<style scoped>
.card {
  border-radius: 14px;
}

.step-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 240px;
}
.step-dot {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 2px solid #cfd4da;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: #cfd4da;
}
.step-dot.active {
  border-color: #0d6efd;
  background: #0d6efd;
}
.step-line {
  position: absolute;
  left: 36px;
  right: -20px;
  top: 14px;
  height: 2px;
  background: #e9ecef;
  z-index: -1;
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
.receipt .center { text-align: center; }
.receipt .right { text-align: right; }
.receipt .bold { font-weight: 700; }
.receipt .big { font-size: 16px; }
.receipt .small { font-size: 11px; }
.receipt .muted { color: #555; }
.receipt .hr { border-top: 1px dashed #000; margin: 6px 0; }
.receipt .row2 { display: flex; justify-content: space-between; gap: 10px; }
.receipt .mt2 { margin-top: 2px; }
.receipt .mt6 { margin-top: 6px; }
.receipt .mt8 { margin-top: 8px; }
.receipt .mt10 { margin-top: 10px; }
.receipt .items-head, .receipt .item { display: flex; gap: 6px; }
.receipt .w-name { flex: 1; }
.receipt .w-qty { width: 10mm; }
.receipt .w-price { width: 22mm; }
.receipt .qr { width: 28mm; height: 28mm; }
</style>
