
<template>
  <div class="container-fluid py-3">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-file-text fs-4"></i>
        <h5 class="mb-0">Chi tiết hóa đơn</h5>
      </div>

      <div class="d-flex align-items-center gap-2">
        <button class="btn btn-outline-secondary btn-sm" @click="goBack">
          <i class="bi bi-arrow-left me-1"></i> Quay lại
        </button>
      </div>
    </div>

    <!-- Stepper (giống ảnh) -->
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
        <button class="btn btn-primary btn-sm" :disabled="!canConfirmDeliver" @click="confirmDeliver">
          Xác nhận đã giao hàng
        </button>
      </div>

      <div class="d-flex gap-2">
        <button class="btn btn-outline-secondary btn-sm" @click="openHistoryModal">
          Chi tiết
        </button>
        <button class="btn btn-outline-success btn-sm" @click="exportDetailExcel">
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
            <div class="mb-2"><span class="text-muted">Mã:</span> <span class="fw-semibold">{{ hd?.maHoaDon }}</span></div>
            <div class="mb-2"><span class="text-muted">Tên khách hàng:</span> {{ hd?.tenKhachHang || "Khách lẻ" }}</div>
            <div class="mb-2"><span class="text-muted">Trạng thái:</span> {{ hd?.tenTrangThaiDon }}</div>
            <div class="mb-2"><span class="text-muted">Ghi chú:</span> {{ hd?.ghiChu || "-" }}</div>
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
              <button class="btn btn-outline-primary btn-sm" :disabled="!hd" @click="doChangeStatus">
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
                <th style="width:140px">Số tiền</th>
                <th style="width:180px">Thời gian</th>
                <th style="width:160px">Mã giao dịch</th>
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
                <th style="width:60px">#</th>
                <th style="width:110px">Ảnh</th>
                <th>Sản phẩm</th>
                <th style="width:120px">Số lượng</th>
                <th style="width:160px">Thành tiền</th>
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
                    {{ it.mauSac || "-" }} - {{ it.kichCo || "-" }} <span v-if="it.maSanPhamChiTiet"> • {{ it.maSanPhamChiTiet }}</span>
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

    <!-- History modal (giống ảnh) -->
    <div class="modal fade" id="historyModal" tabindex="-1" aria-hidden="true" ref="historyModalRef">
      <div class="modal-dialog modal-xl modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h6 class="modal-title">Chi tiết lịch sử</h6>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>

          <div class="modal-body">
            <div class="table-responsive">
              <table class="table align-middle">
                <thead class="table-light">
                  <tr>
                    <th style="width:160px">Trạng thái</th>
                    <th style="width:180px">Thời gian</th>
                    <th style="width:220px">Hành động</th>
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
            <button class="btn btn-light" data-bs-dismiss="modal">Cancel</button>
            <button class="btn btn-primary" data-bs-dismiss="modal">OK</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Toast -->
    <div class="toast-container position-fixed top-0 end-0 p-3" style="z-index: 9999;">
      <div class="toast align-items-center text-bg-success border-0" ref="toastRef" role="alert" aria-live="assertive" aria-atomic="true">
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
import * as XLSX from "xlsx";
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

// step giống ảnh (bạn có thể đổi label theo hệ bạn)
const stepper = computed(() => {
  const list = [
    { code: 0, label: "Hóa đơn chờ" },
    { code: 1, label: "Đang xử lý" },
    { code: 2, label: "Chờ giao hàng" },
    { code: 3, label: "Đang vận chuyển" },
    { code: 4, label: "Thành công" },
  ];

  // lấy time từ lichSuHoaDon nếu có
  const history = (hd.value?.lichSuHoaDon || []);
  const byCodeTime = (code) => {
    // nếu bạn lưu hanhDong là enum name, khó map -> fallback để trống
    // ở đây mình lấy bản ghi mới nhất làm demo
    const found = history.find(() => false);
    return found?.thoiGian || null;
  };

  return list.map((x) => ({
    ...x,
    timeText: byCodeTime(x.code) ? formatDateTimeVN(byCodeTime(x.code)) : "",
  }));
});

const canConfirmDeliver = computed(() => {
  const st = Number(hd.value?.trangThaiDon ?? -1);
  // ví dụ: đang giao (2) -> xác nhận đã giao (3)
  return st === 2;
});

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

function formatCurrency(v) {
  const n = Number(v ?? 0);
  return n.toLocaleString("vi-VN") + " đ";
}

function formatDateTimeVN(isoDateTime) {
  if (!isoDateTime) return "";
  const d = new Date(isoDateTime);
  const hh = String(d.getHours()).padStart(2, "0");
  const mi = String(d.getMinutes()).padStart(2, "0");
  const ss = String(d.getSeconds()).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const yyyy = d.getFullYear();
  return `${hh}:${mi}:${ss} ${dd}/${mm}/${yyyy}`;
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
  // đang giao (2) -> đã giao (3)
  await hoaDonApi.changeStatus(props.id, { trangThaiDon: 3, ghiChu: "Xác nhận đã giao hàng" });
  await fetchDetail();
  showToast("Cập nhật trạng thái thành công!");
}

async function doChangeStatus() {
  await hoaDonApi.changeStatus(props.id, { trangThaiDon: changeStatusCode.value, ghiChu: "Cập nhật trạng thái" });
  await fetchDetail();
  showToast("Cập nhật trạng thái thành công!");
}

function mapHistoryToStatusLabel(hanhDong) {
  if (!hanhDong) return "-";
  // bạn có thể map theo enum TrangThaiDonHang.name() mà BE đang set
  // ví dụ: "DA_HOAN" -> "Đã hoàn"
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

/** History modal */
const historyModalRef = ref(null);
let bsHistoryModal = null;

function openHistoryModal() {
  const el = historyModalRef.value;
  if (!el) return;
  const Modal = window.bootstrap?.Modal;
  if (Modal) {
    bsHistoryModal = Modal.getOrCreateInstance(el);
    bsHistoryModal.show();
  }
}

/** Excel export detail */
async function exportDetailExcel() {
  const d = hd.value;
  if (!d) return;

  const info = [
    { "Trường": "Mã hóa đơn", "Giá trị": d.maHoaDon },
    { "Trường": "Khách hàng", "Giá trị": d.tenKhachHang || "Khách lẻ" },
    { "Trường": "SĐT", "Giá trị": d.soDienThoai || "" },
    { "Trường": "Loại", "Giá trị": d.loaiDon ? "Online" : "Tại quầy" },
    { "Trường": "Trạng thái", "Giá trị": d.tenTrangThaiDon },
    { "Trường": "Tổng tiền", "Giá trị": Number(d.tongTien ?? 0) },
    { "Trường": "Giảm", "Giá trị": Number(d.tongTienGiam ?? 0) },
    { "Trường": "Phí VC", "Giá trị": Number(d.phiVanChuyen ?? 0) },
    { "Trường": "Sau giảm", "Giá trị": Number(d.tongTienSauGiam ?? 0) },
  ];

  const items = (d.items || []).map((x, i) => ({
    "#": i + 1,
    "Mã SPCT": x.maSanPhamChiTiet,
    "Sản phẩm": x.tenSanPham,
    "Màu": x.mauSac,
    "Size": x.kichCo,
    "SL": x.soLuong,
    "Đơn giá": Number(x.donGia ?? 0),
    "Thành tiền": Number(x.thanhTien ?? 0),
  }));

  const pay = (d.lichSuThanhToan || []).map((x, i) => ({
    "#": i + 1,
    "Số tiền": Number(x.soTien ?? 0),
    "Thời gian": formatDateTimeVN(x.ngayThanhToan),
    "Mã GD": x.maGiaoDich || "",
    "Hình thức": x.hinhThucThanhToan || "",
    "Ghi chú": x.ghiChu || "",
  }));

  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(info), "ThongTin");
  XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(items), "SanPham");
  XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(pay), "ThanhToan");
  XLSX.writeFile(wb, `hoa_don_${d.maHoaDon}.xlsx`);
  showToast("Xuất hóa đơn thành công!");
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
</style>
