<template>
  <div class="card">
    <!-- Header -->
    <div style="display:flex; justify-content:space-between; align-items:center; gap:12px; margin-bottom:12px;">
      <h2 style="margin:0;">Hóa đơn</h2>

      <div style="display:flex; gap:8px; align-items:center; flex-wrap:wrap;">
        <input
          v-model="keyword"
          placeholder="Tìm theo mã hoá đơn / tên KH / SĐT"
          style="padding:8px 10px; border:1px solid #e5e7eb; border-radius:10px; min-width:320px;"
          @keyup.enter="fetchOrders(0)"
        />

        <select
          v-model="trangThaiDon"
          style="padding:8px 10px; border:1px solid #e5e7eb; border-radius:10px;"
        >
          <option :value="''">Tất cả trạng thái</option>
          <option :value="0">Chờ xác nhận</option>
          <option :value="1">Đang giao</option>
          <option :value="2">Hoàn thành</option>
          <option :value="3">Hoàn hàng</option>
          <option :value="4">Đã huỷ</option>
        </select>

        <button class="btn btn-dark btn-sm" @click="fetchOrders(0)">Tìm</button>
        <button class="btn btn-outline-secondary btn-sm" @click="openQr">Quét QR</button>
        <button class="btn btn-outline-success btn-sm" @click="exportExcelCurrentPage">
          Xuất Excel (Trang này)
        </button>
      </div>
    </div>

    <!-- States -->
    <div v-if="loading" style="color:#6b7280;">Đang tải...</div>
    <div v-else-if="error" style="color:#ef4444;">{{ error }}</div>

    <!-- Table -->
    <div v-else>
      <table v-if="rows.length" style="width:100%; border-collapse:collapse;">
        <thead>
          <tr style="text-align:left; border-bottom:1px solid #e5e7eb;">
            <th style="padding:10px;">Mã</th>
            <th style="padding:10px;">Khách hàng</th>
            <th style="padding:10px;">SĐT</th>
            <th style="padding:10px;">Loại</th>
            <th style="padding:10px;">Trạng thái</th>
            <th style="padding:10px; text-align:right;">Tổng tiền</th>
            <th style="padding:10px;">Ngày tạo</th>
            <th style="padding:10px; width:180px;">Hành động</th>
          </tr>
        </thead>

        <tbody>
          <tr v-for="r in rows" :key="r.id" style="border-bottom:1px solid #f3f4f6;">
            <td style="padding:10px; font-weight:600;">{{ r.maHoaDon }}</td>
            <td style="padding:10px;">{{ r.tenKhachHang }}</td>
            <td style="padding:10px;">{{ r.soDienThoai }}</td>
            <td style="padding:10px;">
              <span class="badge" :class="r.loaiDon ? 'bg-primary' : 'bg-secondary'">
                {{ r.loaiDon ? "Online" : "Tại quầy" }}
              </span>
            </td>
            <td style="padding:10px;">
              <span class="badge" :class="statusBadgeClass(r.trangThaiDon)">
                {{ r.tenTrangThaiDon ?? r.trangThaiDon }}
              </span>
            </td>
            <td style="padding:10px; text-align:right;">{{ formatMoney(r.tongTienSauGiam) }}</td>
            <td style="padding:10px;">{{ formatDate(r.ngayTao) }}</td>
            <td style="padding:10px;">
              <div style="display:flex; gap:8px;">
                <button class="btn btn-outline-dark btn-sm" @click="openDetail(r)">Chi tiết</button>
                <button class="btn btn-outline-success btn-sm" @click="exportExcelOne(r)">Excel</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-else style="color:#6b7280;">Chưa có hoá đơn.</div>

      <!-- Pagination -->
      <div style="display:flex; justify-content:flex-end; gap:8px; margin-top:12px;">
        <button
          :disabled="page <= 0"
          @click="fetchOrders(page - 1)"
          class="btn btn-outline-secondary btn-sm"
        >
          Prev
        </button>

        <div style="display:flex; align-items:center; color:#6b7280;">
          Page {{ page + 1 }} / {{ totalPages || 1 }}
        </div>

        <button
          :disabled="page >= totalPages - 1"
          @click="fetchOrders(page + 1)"
          class="btn btn-outline-secondary btn-sm"
        >
          Next
        </button>
      </div>
    </div>
  </div>

  <!-- ===================== DETAIL: SUB-PAGE CENTER OVERLAY (2 columns) ===================== -->
  <div v-if="detailOpen" class="od-overlay" @click.self="closeDetail">
    <div class="od-panel" @click.stop>
      <!-- Header đẹp -->
      <div class="od-header">
        <div class="od-header-left">
          <div class="od-subtitle">Chi tiết hóa đơn</div>
          <div class="od-title-row">
            <div class="od-title">{{ detailData?.maHoaDon || "-" }}</div>

            <span v-if="detailData" class="badge" :class="statusBadgeClass(detailData.trangThaiDon)">
              {{ detailData.tenTrangThaiDon ?? detailData.trangThaiDon }}
            </span>

            <span v-if="detailData" class="badge" :class="detailData.loaiDon ? 'bg-primary' : 'bg-secondary'">
              {{ detailData.loaiDon ? "Online" : "Tại quầy" }}
            </span>
          </div>
        </div>

        <div class="d-flex gap-2">
          <button
            class="btn btn-outline-success btn-sm"
            :disabled="!detailData"
            @click="exportExcelDetail(detailData)"
          >
            Xuất Excel
          </button>
          <button class="btn btn-dark btn-sm" @click="closeDetail">Đóng</button>
        </div>
      </div>

      <!-- Body: scroll chỉ trong panel -->
      <div class="od-body">
        <div v-if="detailLoading" class="text-muted">Đang tải chi tiết...</div>
        <div v-else-if="detailError" class="alert alert-danger py-2">{{ detailError }}</div>
        <div v-else-if="!detailData" class="alert alert-warning py-2">Không có dữ liệu chi tiết.</div>

        <template v-else>
          <div class="od-grid">
            <!-- LEFT: Thông tin hóa đơn -->
            <div class="od-left">
              <div class="od-card">
                <div class="od-card-title">Thông tin</div>

                <div class="od-kv">
                  <div class="od-k">Khách hàng</div>
                  <div class="od-v fw-semibold">{{ detailData.tenKhachHang || "-" }}</div>
                </div>

                <div class="od-kv">
                  <div class="od-k">SĐT</div>
                  <div class="od-v">{{ detailData.soDienThoai || "-" }}</div>
                </div>

                <div class="od-kv">
                  <div class="od-k">Ngày tạo</div>
                  <div class="od-v">{{ formatDate(detailData.ngayTao) }}</div>
                </div>

                <div class="od-kv">
                  <div class="od-k">Tổng tiền</div>
                  <div class="od-v od-money">{{ formatMoney(detailData.tongTienSauGiam) }}</div>
                </div>

                <div class="od-kv">
                  <div class="od-k">Ghi chú</div>
                  <div class="od-v text-muted">{{ detailData.ghiChu || "-" }}</div>
                </div>
              </div>

              <div class="od-card">
                <div class="od-card-title">Tóm tắt</div>

                <div class="d-flex justify-content-between align-items-center">
                  <div class="text-muted">Số dòng sản phẩm</div>
                  <div class="fw-semibold">{{ detailItems(detailData).length }}</div>
                </div>

                <div class="d-flex justify-content-between align-items-center mt-2">
                  <div class="text-muted">Loại đơn</div>
                  <div class="fw-semibold">{{ detailData.loaiDon ? "Online" : "Tại quầy" }}</div>
                </div>
              </div>
            </div>

            <!-- RIGHT: Danh sách sản phẩm -->
            <div class="od-right">
              <div class="od-card">
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <div class="od-card-title mb-0">Danh sách sản phẩm</div>
                  <div class="text-muted small">{{ detailItems(detailData).length }} dòng</div>
                </div>

                <div class="table-responsive">
                  <table class="table table-hover table-bordered align-middle mb-0">
                    <thead class="table-light">
                      <tr>
                        <th style="width:46%;">Sản phẩm</th>
                        <th class="text-end" style="width:10%;">SL</th>
                        <th class="text-end" style="width:16%;">Đơn giá</th>
                        <th class="text-end" style="width:16%;">Thành tiền</th>
                        <th style="width:12%;">Ghi chú</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="it in detailItems(detailData)" :key="it.id">
                        <td>
                          <div class="fw-semibold">
                            {{ it.tenSanPham ?? it.sanPham ?? it.tenSanPhamChiTiet ?? "-" }}
                          </div>
                          <div class="text-muted small">
                            {{ it.mauSac ? `Màu: ${it.mauSac}` : "" }}
                            {{ it.kichCo ? ` • Size: ${it.kichCo}` : "" }}
                          </div>
                        </td>
                        <td class="text-end">{{ it.soLuong ?? it.soLuongMua ?? 0 }}</td>
                        <td class="text-end">{{ formatMoney(it.donGia ?? it.gia ?? 0) }}</td>
                        <td class="text-end">
                          {{
                            formatMoney(
                              it.thanhTien ??
                                (Number(it.soLuong ?? 0) * Number(it.donGia ?? 0))
                            )
                          }}
                        </td>
                        <td class="text-muted small">{{ it.ghiChu ?? "" }}</td>
                      </tr>

                      <tr v-if="!detailItems(detailData).length">
                        <td colspan="5" class="text-center text-muted py-3">Không có sản phẩm</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              <div class="od-footer-note text-muted small">
                Nhấn nền tối để đóng hoặc bấm nút “Đóng”.
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>

  <!-- ===================== QR MODAL ===================== -->
  <div v-if="qrOpen" class="modal fade show d-block" tabindex="-1" @click.self="closeQr">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">Quét QR</h5>
          <button type="button" class="btn-close" @click="closeQr"></button>
        </div>

        <div class="modal-body">
          <div v-if="qrError" class="alert alert-danger py-2">{{ qrError }}</div>
          <div id="qr-reader" style="width:100%;"></div>
          <div class="text-muted small mt-2">
            Quét xong sẽ tự điền vào ô tìm kiếm và tự tải danh sách.
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary btn-sm" @click="closeQr">Đóng</button>
        </div>
      </div>
    </div>
  </div>
  <div v-if="qrOpen" class="modal-backdrop fade show"></div>
</template>

<script setup>
import { ref, watch, onMounted, nextTick, onBeforeUnmount } from "vue";
import { hoaDonApi } from "../../services/hoaDonApi";

import * as XLSX from "xlsx";
import { Html5Qrcode } from "html5-qrcode";

/* ===== State ===== */
const loading = ref(false);
const error = ref("");
const rows = ref([]);

const page = ref(0);
const size = ref(10);
const totalPages = ref(0);

const keyword = ref("");
const trangThaiDon = ref(""); // '' = all
let debounceTimer = null;

/* ===== Auto filter when change status ===== */
watch(trangThaiDon, () => {
  fetchOrders(0);
});

/* ===== Auto load when typing keyword (debounce) ===== */
watch(keyword, () => {
  clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => {
    fetchOrders(0);
  }, 300);
});

/* ===== Utils ===== */
function formatMoney(v) {
  if (v == null) return "";
  return new Intl.NumberFormat("vi-VN").format(Number(v)) + " ₫";
}

function formatDate(date) {
  if (!date) return "";
  const d = new Date(date);
  const day = String(d.getDate()).padStart(2, "0");
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const year = d.getFullYear();
  return `${day}/${month}/${year}`;
}

function statusBadgeClass(st) {
  switch (Number(st)) {
    case 0: return "bg-warning text-dark";
    case 1: return "bg-primary";
    case 2: return "bg-success";
    case 3: return "bg-info text-dark";
    case 4: return "bg-danger";
    default: return "bg-secondary";
  }
}

/* ===== API ===== */
async function fetchOrders(p = 0) {
  loading.value = true;
  error.value = "";
  try {
    const params = {
      page: p,
      size: size.value,
      keyword: keyword.value || undefined,
      trangThaiDon: trangThaiDon.value === "" ? undefined : Number(trangThaiDon.value),
    };

    const res = await hoaDonApi.search(params);

    rows.value = res.data?.content ?? [];
    page.value = res.data?.number ?? p;
    totalPages.value = res.data?.totalPages ?? 0;
  } catch (e) {
    error.value =
      e?.response?.data?.message ||
      e?.response?.data ||
      e?.message ||
      "Gọi API thất bại";
  } finally {
    loading.value = false;
  }
}

/* ===================== DETAIL ===================== */
const detailOpen = ref(false);
const detailLoading = ref(false);
const detailError = ref("");
const detailData = ref(null);

function openDetail(row) {
  detailOpen.value = true;
  loadDetail(row?.id);
}

function closeDetail() {
  detailOpen.value = false;
  detailData.value = null;
  detailError.value = "";
}

function detailItems(d) {
  // fallback nhiều tên field (tránh rỗng do BE đặt tên khác)
  return d?.items || d?.hoaDonChiTiet || d?.chiTiet || d?.details || [];
}

async function loadDetail(id) {
  if (!id) return;
  detailLoading.value = true;
  detailError.value = "";
  try {
    const res = await hoaDonApi.detail(id);
    detailData.value = res.data ?? res;
  } catch (e) {
    detailError.value =
      e?.response?.data?.message ||
      e?.response?.data ||
      e?.message ||
      "Tải chi tiết thất bại";
  } finally {
    detailLoading.value = false;
  }
}

/* ===================== EXCEL ===================== */
function exportExcelCurrentPage() {
  const data = rows.value.map((r) => ({
    "Mã hóa đơn": r.maHoaDon,
    "Khách hàng": r.tenKhachHang,
    "SĐT": r.soDienThoai,
    "Loại": r.loaiDon ? "Online" : "Tại quầy",
    "Trạng thái": r.tenTrangThaiDon ?? r.trangThaiDon,
    "Tổng tiền": r.tongTienSauGiam,
    "Ngày tạo": formatDate(r.ngayTao),
  }));

  const ws = XLSX.utils.json_to_sheet(data);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, "HoaDon");
  XLSX.writeFile(wb, `hoa-don_trang-${page.value + 1}.xlsx`);
}

function exportExcelOne(r) {
  const data = [{
    "Mã hóa đơn": r.maHoaDon,
    "Khách hàng": r.tenKhachHang,
    "SĐT": r.soDienThoai,
    "Loại": r.loaiDon ? "Online" : "Tại quầy",
    "Trạng thái": r.tenTrangThaiDon ?? r.trangThaiDon,
    "Tổng tiền": r.tongTienSauGiam,
    "Ngày tạo": formatDate(r.ngayTao),
  }];

  const ws = XLSX.utils.json_to_sheet(data);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, "HoaDon");
  XLSX.writeFile(wb, `hoa-don_${r.maHoaDon || r.id}.xlsx`);
}

function exportExcelDetail(d) {
  const wb = XLSX.utils.book_new();

  const s1 = XLSX.utils.json_to_sheet([{
    "Mã hóa đơn": d.maHoaDon,
    "Khách hàng": d.tenKhachHang,
    "SĐT": d.soDienThoai,
    "Loại": d.loaiDon ? "Online" : "Tại quầy",
    "Trạng thái": d.tenTrangThaiDon ?? d.trangThaiDon,
    "Tổng tiền": d.tongTienSauGiam,
    "Ngày tạo": formatDate(d.ngayTao),
  }]);
  XLSX.utils.book_append_sheet(wb, s1, "ThongTin");

  const items = detailItems(d).map((it) => ({
    "Sản phẩm": it.tenSanPham ?? it.sanPham ?? it.tenSanPhamChiTiet ?? "-",
    "Số lượng": it.soLuong ?? it.soLuongMua ?? 0,
    "Đơn giá": it.donGia ?? it.gia ?? 0,
    "Thành tiền": it.thanhTien ?? (Number(it.soLuong ?? 0) * Number(it.donGia ?? 0)),
  }));
  const s2 = XLSX.utils.json_to_sheet(items);
  XLSX.utils.book_append_sheet(wb, s2, "ChiTiet");

  XLSX.writeFile(wb, `hoa-don_chi-tiet_${d.maHoaDon || d.id}.xlsx`);
}

/* ===================== QR ===================== */
const qrOpen = ref(false);
const qrError = ref("");
let qrInstance = null;

async function openQr() {
  qrError.value = "";
  qrOpen.value = true;
  await nextTick();

  try {
    qrInstance = new Html5Qrcode("qr-reader");
    await qrInstance.start(
      { facingMode: "environment" },
      { fps: 10, qrbox: { width: 250, height: 250 } },
      (decodedText) => {
        keyword.value = decodedText;
        fetchOrders(0);
        closeQr();
      },
      () => {}
    );
  } catch (e) {
    qrError.value = e?.message || "Không mở được camera. Hãy cấp quyền camera cho trình duyệt.";
  }
}

async function closeQr() {
  qrOpen.value = false;
  try {
    if (qrInstance) {
      await qrInstance.stop().catch(() => {});
      await qrInstance.clear().catch(() => {});
    }
  } finally {
    qrInstance = null;
  }
}

onBeforeUnmount(() => {
  if (qrInstance) {
    qrInstance.stop?.().catch(() => {});
    qrInstance.clear?.().catch(() => {});
    qrInstance = null;
  }
});

/* ===== Init ===== */
onMounted(() => fetchOrders(0));
</script>

<style scoped>
/* overlay đậm hơn chút */
.od-overlay{
  position: fixed;
  inset: 0;
  z-index: 2000;
  display:flex;
  align-items:center;
  justify-content:center;
  background: rgba(0,0,0,.55); /* đậm hơn */
  backdrop-filter: blur(8px);
}

/* panel giữa màn hình */
.od-panel{
  width: min(1200px, 96vw);
  height: min(86vh, 920px);
  background: rgba(255,255,255,0.96);
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 24px 70px rgba(0,0,0,.35);
  border: 1px solid rgba(255,255,255,.25);
}

.od-header{
  padding: 14px 16px;
  display:flex;
  justify-content:space-between;
  align-items:center;
  border-bottom: 1px solid rgba(0,0,0,.08);
  background: rgba(255,255,255,.9);
}

.od-header-left{
  min-width: 0;
}
.od-subtitle{
  font-size: 12px;
  color: #6b7280;
}
.od-title-row{
  display:flex;
  align-items:center;
  gap: 10px;
  flex-wrap: wrap;
}
.od-title{
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

.od-body{
  height: calc(min(86vh, 920px) - 58px);
  overflow: auto;
  padding: 16px;
}

/* 2 cột */
.od-grid{
  display:grid;
  grid-template-columns: 360px 1fr;
  gap: 14px;
}

@media (max-width: 992px){
  .od-grid{
    grid-template-columns: 1fr;
  }
}

.od-card{
  background: rgba(255,255,255,0.95);
  border: 1px solid rgba(0,0,0,.08);
  border-radius: 14px;
  padding: 14px;
  box-shadow: 0 10px 24px rgba(0,0,0,.06);
}

.od-card-title{
  font-weight: 700;
  color: #111827;
  margin-bottom: 10px;
}

.od-kv{
  display:flex;
  justify-content:space-between;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px dashed rgba(0,0,0,.08);
}
.od-kv:last-child{
  border-bottom: none;
}

.od-k{
  color: #6b7280;
  font-size: 13px;
}
.od-v{
  text-align:right;
  color: #111827;
}
.od-money{
  font-size: 18px;
  font-weight: 800;
}

.od-footer-note{
  margin-top: 10px;
  text-align: right;
}
</style> 