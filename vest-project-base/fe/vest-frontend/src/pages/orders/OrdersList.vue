<template>
  <div class="container-fluid py-3">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-receipt fs-4"></i>
        <h5 class="mb-0">Danh sách hóa đơn</h5>
      </div>

      <div class="d-flex align-items-center gap-2">
        <button class="btn btn-outline-secondary btn-sm" @click="openQrModal">
          <i class="bi bi-qr-code-scan me-1"></i> Quét QR
        </button>
        <button class="btn btn-outline-primary btn-sm" @click="exportListExcel">
          <i class="bi bi-file-earmark-excel me-1"></i> Export trang
        </button>
      </div>
    </div>

    <!-- Filters -->
    <div class="card shadow-sm mb-3 filter-card">
      <!-- Header (click để thu gọn/mở rộng) -->
      <div
        class="filter-header d-flex align-items-center justify-content-between"
        data-bs-toggle="collapse"
        data-bs-target="#filterBody"
        role="button"
        aria-expanded="true"
        aria-controls="filterBody"
      >
        <div class="d-flex align-items-center gap-2">
          <span class="filter-icon">▼</span>
          <span class="filter-title">Bộ lọc tìm kiếm</span>
        </div>

        <small class="filter-hint">Nhấn để thu gọn/mở rộng</small>
      </div>

      <!-- Body -->
      <div id="filterBody" class="collapse show">
        <div class="card-body filter-body">
          <div class="row g-3">
            <div class="col-12 col-lg-6">
              <label class="form-label">Tìm kiếm</label>
              <input
                v-model.trim="filters.keyword"
                type="text"
                class="form-control"
                placeholder="Nhập mã hóa đơn / tên khách / SĐT..."
                @input="autoApplyFilters()"
                @keyup.enter="applyFilters"
              />
            </div>

            <div class="col-12 col-lg-3">
              <label class="form-label">Trạng thái</label>
              <select
                v-model="filters.trangThaiDon"
                class="form-select"
                @change="applyFilters"
              >
                <option :value="null">Tất cả</option>
                <option
                  v-for="s in statusOptions"
                  :key="s.code"
                  :value="s.code"
                >
                  {{ s.label }}
                </option>
              </select>
            </div>

            <div class="col-12 col-lg-3">
              <label class="form-label">Loại hóa đơn</label>
              <div class="d-flex align-items-center gap-3 mt-2 flex-wrap">
                <div class="form-check">
                  <input
                    class="form-check-input"
                    type="radio"
                    id="ld_all"
                    value=""
                    v-model="filters.loaiDonMode"
                    @change="applyFilters"
                  />
                  <label class="form-check-label" for="ld_all">Tất cả</label>
                </div>

                <div class="form-check">
                  <input
                    class="form-check-input"
                    type="radio"
                    id="ld_tq"
                    value="taiquay"
                    v-model="filters.loaiDonMode"
                    @change="applyFilters"
                  />
                  <label class="form-check-label" for="ld_tq">Tại quầy</label>
                </div>

                <div class="form-check">
                  <input
                    class="form-check-input"
                    type="radio"
                    id="ld_on"
                    value="online"
                    v-model="filters.loaiDonMode"
                    @change="applyFilters"
                  />
                  <label class="form-check-label" for="ld_on">Online</label>
                </div>
              </div>
            </div>

            <!-- ✅ Từ ngày -->
            <div class="col-12 col-lg-3">
              <label class="form-label">Từ ngày</label>
              <div class="input-group">
                <input
                  ref="fromPickerRef"
                  type="text"
                  class="form-control"
                  placeholder="dd/mm/yyyy"
                />
                <button
                  class="btn btn-outline-secondary"
                  type="button"
                  @click="openFromPicker"
                  title="Chọn ngày"
                >
                  <i class="bi bi-calendar3"></i>
                </button>
                <button
                  class="btn btn-outline-secondary"
                  type="button"
                  @click="clearFromDate"
                  title="Xóa"
                >
                  <i class="bi bi-x-lg"></i>
                </button>
              </div>
            </div>

            <!-- ✅ Đến ngày -->
            <div class="col-12 col-lg-3">
              <label class="form-label">Đến ngày</label>
              <div class="input-group">
                <input
                  ref="toPickerRef"
                  type="text"
                  class="form-control"
                  placeholder="dd/mm/yyyy"
                />
                <button
                  class="btn btn-outline-secondary"
                  type="button"
                  @click="openToPicker"
                  title="Chọn ngày"
                >
                  <i class="bi bi-calendar3"></i>
                </button>
                <button
                  class="btn btn-outline-secondary"
                  type="button"
                  @click="clearToDate"
                  title="Xóa"
                >
                  <i class="bi bi-x-lg"></i>
                </button>
              </div>
            </div>

            <div class="col-12 col-lg-3">
              <label class="form-label">Min tổng</label>
              <input
                v-model.number="filters.minTotal"
                type="number"
                class="form-control"
                placeholder="VD: 100000"
                @input="autoApplyFilters()"
                @keyup.enter="applyFilters"
              />
            </div>

            <div class="col-12 col-lg-3">
              <label class="form-label">Max tổng</label>
              <input
                v-model.number="filters.maxTotal"
                type="number"
                class="form-control"
                placeholder="VD: 1000000"
                @input="autoApplyFilters()"
                @keyup.enter="applyFilters"
              />
            </div>

            <div class="col-12 d-flex justify-content-end gap-2">
              <button class="btn btn-light" @click="resetFilters">
                <i class="bi bi-arrow-counterclockwise me-1"></i> Reset
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Table -->
    <div class="card shadow-sm">
      <div class="card-body">
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border" role="status"></div>
          <div class="mt-2 text-muted">Đang tải...</div>
        </div>

        <div v-else class="table-responsive table-wrap">
          <table class="table align-middle mb-0">
            <thead class="thead-dark-custom">
              <tr>
                <th style="width: 60px">STT</th>
                <th class="col-ma">Mã hóa đơn</th>
                <th>Khách hàng</th>
                <th>Số điện thoại</th>
                <th style="width: 140px">Loại hóa đơn</th>
                <th style="width: 160px">Tổng tiền</th>
                <th style="width: 140px">Ngày tạo</th>
                <th style="width: 160px">Trạng thái</th>
                <th style="width: 140px" class="text-end">Hành động</th>
              </tr>
            </thead>

            <tbody>
              <tr v-if="rows.length === 0">
                <td colspan="9" class="text-center text-muted py-4">
                  Không có dữ liệu
                </td>
              </tr>

              <tr v-for="(r, idx) in rows" :key="r.id">
                <td>{{ page.page * page.size + idx + 1 }}</td>
                <td class="fw-semibold col-ma">
  <span class="ma-ellipsis" :title="r.maHoaDon">{{ r.maHoaDon }}</span>
</td>

                <td>{{ r.tenKhachHang || "Khách lẻ" }}</td>
                <td>{{ r.soDienThoai || "-" }}</td>
                <td>
                  <span class="badge text-bg-light border">
                    {{ r.loaiDon ? "Online" : "Tại quầy" }}
                  </span>
                </td>
                <td class="fw-semibold">
                  {{ formatCurrency(r.tongTienSauGiam) }}
                </td>
                <td>{{ formatDateVN(r.ngayTao) }}</td>
                <td>
                  <span class="badge" :class="statusBadgeClass(r.trangThaiDon)">
                    {{ r.tenTrangThaiDon }}
                  </span>
                </td>
                <td class="text-end">
                  <button
                    class="btn btn-outline-primary btn-sm me-2"
                    @click="goDetail(r.id)"
                    title="Chi tiết hóa đơn"
                  >
                    <i class="bi bi-eye"></i>
                  </button>
                  <button
                    class="btn btn-outline-success btn-sm"
                    @click="exportOneExcel(r.id)"
                    title="Export hóa đơn"
                  >
                    <i class="bi bi-file-earmark-excel"></i>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <!-- Pagination -->
        <div
          class="d-flex align-items-center mt-3 flex-column flex-md-row gap-2"
        >
          <!-- Left -->
          <div class="text-muted flex-grow-1">
            Hiển thị {{ rows.length }} / tổng {{ page.totalElements }} bản ghi
          </div>

          <!-- Center -->
          <div
            class="d-flex align-items-center gap-2 justify-content-center flex-grow-1"
          >
            <button
              class="btn btn-outline-secondary btn-sm"
              :disabled="page.page === 0"
              @click="setPage(page.page - 1)"
            >
              <i class="bi bi-chevron-left"></i>
            </button>

            <div class="input-group input-group-sm" style="width: 100px">
              <span class="input-group-text">Trang</span>
              <input
                type="number"
                min="1"
                :max="page.totalPages || 1"
                class="form-control"
                v-model.number="pageInput"
                @keyup.enter="jumpPage"
              />
            </div>

            <button
              class="btn btn-outline-secondary btn-sm"
              :disabled="page.page >= page.totalPages - 1"
              @click="setPage(page.page + 1)"
            >
              <i class="bi bi-chevron-right"></i>
            </button>
          </div>

          <!-- Right -->
          <div class="d-flex justify-content-md-end flex-grow-1">
            <select
              class="form-select form-select-sm"
              style="width: 160px"
              v-model.number="page.size"
              @change="applyFilters"
            >
              <option :value="10">10 bản ghi / trang</option>
              <option :value="20">20 bản ghi / trang</option>
              <option :value="50">50 bản ghi / trang</option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <!-- QR Modal -->
    <div
      class="modal fade"
      id="qrModal"
      tabindex="-1"
      aria-hidden="true"
      ref="qrModalRef"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h6 class="modal-title">
              <i class="bi bi-qr-code-scan me-2"></i>Quét QR hóa đơn
            </h6>
            <button
              type="button"
              class="btn-close"
              aria-label="Close"
              @click="closeQrModal"
            ></button>
          </div>
          <div class="modal-body">
            <div class="text-muted small mb-2">
              Đưa QR vào khung để quét mã hóa đơn.
            </div>
            <div id="qr-reader" style="width: 100%"></div>

            <div v-if="qrError" class="alert alert-warning mt-3 mb-0">
              {{ qrError }}
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-light" @click="closeQrModal">Đóng</button>
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
import { onBeforeUnmount, onMounted, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import * as XLSX from "xlsx";
import { Html5Qrcode } from "html5-qrcode";
import hoaDonApi from "@/services/hoaDonApi";

/** ✅ Flatpickr */
import flatpickr from "flatpickr";
import { Vietnamese } from "flatpickr/dist/l10n/vn.js";
import "flatpickr/dist/flatpickr.css";

// bootstrap modal/toast
let bsModal = null;
let bsToast = null;

const router = useRouter();

const loading = ref(false);
const rows = ref([]);

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

const filters = reactive({
  keyword: "",
  trangThaiDon: null,
  loaiDonMode: "", // "", "taiquay", "online"
  fromDate: "", // ✅ lưu yyyy-MM-dd
  toDate: "", // ✅ lưu yyyy-MM-dd
  minTotal: null,
  maxTotal: null,
});

const page = reactive({
  page: 0,
  size: 10,
  totalElements: 0,
  totalPages: 0,
  sortBy: "id",
  sortDir: "desc",
});

const pageInput = ref(1);
// =======================
// Auto search (debounce) - GIỮ NGUYÊN LOGIC CŨ
// =======================
let autoTimer = null;
function autoApplyFilters(delay = 450) {
  clearTimeout(autoTimer);
  autoTimer = setTimeout(() => {
    applyFilters(); // vẫn gọi applyFilters() như cũ
  }, delay);
}

function statusBadgeClass(code) {
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

function formatCurrency(v) {
  const n = Number(v ?? 0);
  return n.toLocaleString("vi-VN") + " đ";
}

function formatDateVN(isoDateTime) {
  if (!isoDateTime) return "";
  const d = new Date(isoDateTime);
  const dd = String(d.getDate()).padStart(2, "0");
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const yyyy = d.getFullYear();
  return `${dd}/${mm}/${yyyy}`;
}

/** =======================
 * ✅ Flatpickr: dd/mm/yyyy UI - yyyy-MM-dd data
 * ======================= */
const fromPickerRef = ref(null);
const toPickerRef = ref(null);
let fpFrom = null;
let fpTo = null;

function parseYMD(ymd) {
  if (!ymd) return null;
  const [y, m, d] = String(ymd).split("-").map(Number);
  if (!y || !m || !d) return null;
  return new Date(y, m - 1, d);
}

function initPickers() {
  if (fromPickerRef.value && !fpFrom) {
    fpFrom = flatpickr(fromPickerRef.value, {
      locale: Vietnamese,
      dateFormat: "d/m/Y", // hiển thị
      allowInput: true,
      defaultDate: parseYMD(filters.fromDate),
      onChange: (selectedDates) => {
        const d = selectedDates?.[0] || null;
        filters.fromDate = d ? flatpickr.formatDate(d, "Y-m-d") : "";
        autoApplyFilters();
      },
    });
  }

  if (toPickerRef.value && !fpTo) {
    fpTo = flatpickr(toPickerRef.value, {
      locale: Vietnamese,
      dateFormat: "d/m/Y",
      allowInput: true,
      defaultDate: parseYMD(filters.toDate),
      onChange: (selectedDates) => {
        const d = selectedDates?.[0] || null;
        filters.toDate = d ? flatpickr.formatDate(d, "Y-m-d") : "";
        autoApplyFilters();
      },
    });
  }
}

function openFromPicker() {
  fpFrom?.open();
}
function openToPicker() {
  fpTo?.open();
}
function clearFromDate() {
  filters.fromDate = "";
  fpFrom?.clear();
  autoApplyFilters();
}
function clearToDate() {
  filters.toDate = "";
  fpTo?.clear();
  autoApplyFilters();
}

// (optional) khóa range: from <= to
watch(
  () => filters.fromDate,
  (v) => {
    if (fpTo) fpTo.set("minDate", v ? parseYMD(v) : null);
  },
);
watch(
  () => filters.toDate,
  (v) => {
    if (fpFrom) fpFrom.set("maxDate", v ? parseYMD(v) : null);
  },
);

/** =======================
 * Data fetch
 * ======================= */
async function fetchData() {
  loading.value = true;
  try {
    const loaiDon =
      filters.loaiDonMode === "online"
        ? true
        : filters.loaiDonMode === "taiquay"
          ? false
          : undefined;

    const params = {
      page: page.page,
      size: page.size,
      sortBy: page.sortBy,
      sortDir: page.sortDir,
      keyword: filters.keyword || undefined,
      trangThaiDon:
        filters.trangThaiDon === null || filters.trangThaiDon === undefined
          ? undefined
          : filters.trangThaiDon,
      loaiDon,

      // ✅ giữ nguyên BE: yyyy-MM-dd
      from: filters.fromDate || undefined,
      to: filters.toDate || undefined,

      minTotal:
        filters.minTotal === null ? undefined : Number(filters.minTotal),
      maxTotal:
        filters.maxTotal === null ? undefined : Number(filters.maxTotal),
    };

    const res = await hoaDonApi.search(params);
    const p = res.data;

    rows.value = p.content || [];
    page.totalElements = p.totalElements ?? 0;
    page.totalPages = p.totalPages ?? 0;
    pageInput.value = page.page + 1;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

function applyFilters() {
  page.page = 0;
  fetchData();
}

function resetFilters() {
  filters.keyword = "";
  filters.trangThaiDon = null;
  filters.loaiDonMode = "";
  filters.fromDate = "";
  filters.toDate = "";
  filters.minTotal = null;
  filters.maxTotal = null;

  fpFrom?.clear();
  fpTo?.clear();
  if (fpTo) fpTo.set("minDate", null);
  if (fpFrom) fpFrom.set("maxDate", null);

  page.page = 0;
  fetchData();
}

function setPage(p) {
  if (p < 0) return;
  if (page.totalPages && p > page.totalPages - 1) return;
  page.page = p;
  fetchData();
}

function jumpPage() {
  const max = Math.max(1, page.totalPages || 1);
  const target = Math.min(Math.max(1, pageInput.value || 1), max);
  page.page = target - 1;
  fetchData();
}

function goDetail(id) {
  router.push({ name: "order-detail", params: { id } });
}

/** =======================
 * EXCEL
 * ======================= */
async function exportListExcel() {
  const data = rows.value.map((r, idx) => ({
    "#": page.page * page.size + idx + 1,
    "Mã hóa đơn": r.maHoaDon,
    "Khách hàng": r.tenKhachHang || "Khách lẻ",
    SĐT: r.soDienThoai || "",
    Loại: r.loaiDon ? "Online" : "Tại quầy",
    "Tổng tiền": Number(r.tongTienSauGiam ?? 0),
    "Ngày tạo": formatDateVN(r.ngayTao),
    "Trạng thái": r.tenTrangThaiDon,
  }));

  const ws = XLSX.utils.json_to_sheet(data);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, "HoaDon");
  XLSX.writeFile(wb, `hoa_don_trang_${page.page + 1}.xlsx`);
}

async function exportOneExcel(id) {
  try {
    const res = await hoaDonApi.detail(id);
    const hd = res.data;

    const info = [
      { Trường: "Mã hóa đơn", "Giá trị": hd.maHoaDon },
      { Trường: "Khách hàng", "Giá trị": hd.tenKhachHang || "Khách lẻ" },
      { Trường: "SĐT", "Giá trị": hd.soDienThoai || "" },
      { Trường: "Loại", "Giá trị": hd.loaiDon ? "Online" : "Tại quầy" },
      { Trường: "Trạng thái", "Giá trị": hd.tenTrangThaiDon },
      { Trường: "Tổng tiền", "Giá trị": Number(hd.tongTien ?? 0) },
      { Trường: "Giảm", "Giá trị": Number(hd.tongTienGiam ?? 0) },
      { Trường: "Sau giảm", "Giá trị": Number(hd.tongTienSauGiam ?? 0) },
      { Trường: "Ngày tạo", "Giá trị": formatDateVN(hd.ngayTao) },
    ];

    const items = (hd.items || []).map((x, i) => ({
      "#": i + 1,
      "Mã SPCT": x.maSanPhamChiTiet,
      "Sản phẩm": x.tenSanPham,
      Màu: x.mauSac,
      Size: x.kichCo,
      SL: x.soLuong,
      "Đơn giá": Number(x.donGia ?? 0),
      "Thành tiền": Number(x.thanhTien ?? 0),
    }));

    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(
      wb,
      XLSX.utils.json_to_sheet(info),
      "ThongTin",
    );
    XLSX.utils.book_append_sheet(
      wb,
      XLSX.utils.json_to_sheet(items),
      "ChiTiet",
    );
    XLSX.writeFile(wb, `hoa_don_${hd.maHoaDon}.xlsx`);
    showToast("Export hóa đơn thành công!");
  } catch (e) {
    console.error(e);
  }
}

/** =======================
 * QR
 * ======================= */
const qrModalRef = ref(null);
let qr = null;
const qrError = ref("");

function openQrModal() {
  const modalEl = qrModalRef.value;
  if (!modalEl) return;

  document.querySelectorAll(".modal-backdrop").forEach((b) => b.remove());
  document.body.classList.remove("modal-open");

  const Modal = window.bootstrap?.Modal;
  if (Modal) {
    bsModal = Modal.getOrCreateInstance(modalEl);
    bsModal.show();
  } else {
    modalEl.classList.add("show");
    modalEl.style.display = "block";
  }

  startQr();
}

function closeQrModal() {
  stopQr();

  const modalEl = qrModalRef.value;
  if (!modalEl) return;

  if (bsModal) {
    bsModal.hide();
    return;
  }

  modalEl.classList.remove("show");
  modalEl.style.display = "none";
  modalEl.setAttribute("aria-hidden", "true");
  document.body.classList.remove("modal-open");

  const backdrop = document.querySelector(".modal-backdrop");
  if (backdrop) backdrop.remove();
}

async function startQr() {
  qrError.value = "";
  try {
    if (!qr) qr = new Html5Qrcode("qr-reader");

    const cameras = await Html5Qrcode.getCameras();
    if (!cameras || cameras.length === 0) {
      qrError.value = "Không tìm thấy camera.";
      return;
    }

    await qr.start(
      { deviceId: { exact: cameras[0].id } },
      { fps: 10, qrbox: { width: 250, height: 250 } },
      async (decodedText) => {
        await onQrDecoded(decodedText);
      },
    );
  } catch (e) {
    console.error(e);
    qrError.value = "Không mở được camera / quyền camera bị chặn.";
  }
}

async function onQrDecoded(text) {
  try {
    await stopQr();
    const ma = String(text || "").trim();
    if (!ma) return;

    const res = await hoaDonApi.byMa(ma);
    const hd = res.data;

    showToast("Quét thành công!");
    if (bsModal) bsModal.hide();
    router.push({ name: "order-detail", params: { id: hd.id } });
  } catch (e) {
    console.error(e);
    qrError.value = "Không tìm thấy hóa đơn theo QR.";
  }
}

async function stopQr() {
  try {
    if (qr && (await qr.getState()) === 2) {
      await qr.stop();
      await qr.clear();
    }
  } catch {
    // ignore
  }
}

/** =======================
 * Toast
 * ======================= */
const toastRef = ref(null);
const toastMsg = ref("");

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

onMounted(() => {
  fetchData();
  initPickers();
});

onBeforeUnmount(() => {
  stopQr();
  try {
    fpFrom?.destroy();
  } catch {}
  try {
    fpTo?.destroy();
  } catch {}
});
</script>

<style scoped>
.card {
  border-radius: 14px;
}
.table thead th {
  font-weight: 600;
}
.thead-dark-custom th {
  background-color: #1f2a44 !important;
  color: #fff !important;
}
.table-wrap {
  border: 1px solid #dee2e6;
  border-radius: 12px;
  overflow: hidden; /* để bo góc ăn cả thead */
}

/* header màu #1f2a44 */
.thead-dark-custom th {
  background-color: #1f2a44 !important;
  color: #fff !important;
  border-color: rgba(255, 255, 255, 0.15) !important;
}

/* (tuỳ chọn) đường kẻ trong bảng nhìn gọn */
.table td,
.table th {
  border-color: #e9ecef;
}

.filter-card {
  border-radius: 14px;
  overflow: hidden; /* bo góc ăn cả header */
  border: 1px solid #e9ecef;
}

.filter-header {
  background: #1f2a44;
  color: #fff;
  padding: 12px 16px;
  cursor: pointer;
  user-select: none;
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
}

.filter-title {
  font-weight: 700;
}

.filter-hint {
  opacity: 0.75;
}

.filter-icon {
  display: inline-flex;
  width: 26px;
  height: 26px;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 12px;
  line-height: 1;
  transition: transform 0.2s ease;
}

/* Icon xoay khi collapse đóng */
.filter-header[aria-expanded="false"] .filter-icon {
  transform: rotate(-90deg);
}

.filter-body {
  background: #f8fafc; /* nền nhẹ */
}

.filter-card .form-label {
  font-weight: 600;
}

.filter-card .form-control,
.filter-card .form-select {
  border-radius: 10px;
}
.ellipsis{
  display: inline-block;
  max-width: 120px;      /* chỉnh rộng/hẹp tuỳ bạn */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: bottom;
}

</style>
