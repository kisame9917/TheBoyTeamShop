<template>
  <div class="container-fluid py-3">
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-arrow-left-right fs-5"></i>
        <h6 class="mb-0 fw-semibold">Giao ca &amp; Kết toán</h6>
      </div>

<!--      <RouterLink class="btn btn-outline-secondary btn-sm" to="/my-schedule">-->
<!--        <i class="bi bi-calendar3 me-1"></i> Lịch của tôi-->
<!--      </RouterLink>-->
    </div>

    <!-- ADMIN -->
    <template v-if="isAdmin">
      <!-- FILTER (tách riêng giống trang lịch của tôi) -->
      <div class="card shadow-sm mb-3 filter-card">
        <div
            class="filter-header d-flex align-items-center justify-content-between"
            data-bs-toggle="collapse"
            data-bs-target="#adminFilterBody"
            role="button"
            aria-expanded="true"
            aria-controls="adminFilterBody"
        >
          <div class="d-flex align-items-center gap-2">
            <span class="filter-icon">▼</span>
            <span class="filter-title">Bộ lọc tìm kiếm</span>
          </div>
          <small class="filter-hint">Nhấn để thu gọn/mở rộng</small>
        </div>

        <div id="adminFilterBody" class="collapse show">
          <div class="card-body filter-body">
            <div class="row g-3">
              <div class="col-12 col-lg-6">
                <label class="form-label">Tìm theo nhân viên / mã ca</label>
                <input
                    v-model.trim="keyword"
                    type="text"
                    class="form-control"
                    placeholder="Tìm theo nhân viên / mã ca..."
                    @input="triggerAutoSearch"
                />
              </div>

              <div class="col-12 col-lg-3">
                <label class="form-label">Từ ngày</label>
                <input
                    v-model="fromDate"
                    type="date"
                    class="form-control"
                    @change="triggerAutoSearch"
                />
              </div>

              <div class="col-12 col-lg-3">
                <label class="form-label">Đến ngày</label>
                <input
                    v-model="toDate"
                    type="date"
                    class="form-control"
                    @change="triggerAutoSearch"
                />
              </div>

              <div class="col-12 d-flex justify-content-end">
                <button class="btn btn-light btn-sm" type="button" :disabled="loading" @click="resetFilters">
                  <i class="bi bi-arrow-counterclockwise me-1"></i> Đặt lại
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- TABLE (tách riêng) -->
      <div class="card shadow-sm">
        <div class="card-body">
          <div v-if="loading" class="text-center py-5">
            <div class="spinner-border" role="status"></div>
            <div class="mt-2 text-muted">Đang tải...</div>
          </div>

          <template v-else>
            <div v-if="adminRows.length === 0" class="text-muted">
              Chưa có dữ liệu.
            </div>

            <div v-else class="table-wrap mt-2">
              <table class="custom-table">
                <thead>
                <tr>
                  <th style="width: 60px" class="text-center">#</th>
                  <th>Nhân viên</th>
                  <th>Ca</th>
                  <th>Mở</th>
                  <th>Đóng</th>
                  <th class="text-end">DT Tiền mặt</th>
                  <th class="text-end">DT CK/Thẻ</th>
                  <th class="text-end">Tổng DT</th>
<!--                  <th class="text-end">Chênh</th>-->
                  <th class="text-center">Trạng thái</th>
                </tr>
                </thead>

                <tbody>
                <tr v-for="(r, idx) in adminRows" :key="r.id || idx">
                  <td class="text-center">{{ r.id ?? (idx + 1) }}</td>

                  <!-- Nếu API có mã NV thì hiển thị kèm (không ảnh hưởng chức năng) -->
                  <td class="fw-semibold">
                    <span v-if="r.maNhanVien">{{ r.maNhanVien }} - </span>{{ r.tenNhanVien || "-" }}
                  </td>

                  <td>{{ r.tenCa || "-" }}</td>
                  <td>{{ fmtDt(r.thoiGianMo) }}</td>
                  <td>{{ r.thoiGianDong ? fmtDt(r.thoiGianDong) : "-" }}</td>
                  <td class="text-end">{{ money(r.doanhThuTienMat) }}</td>
                  <td class="text-end">{{ money(r.doanhThuCkThe) }}</td>
                  <td class="text-end fw-semibold">{{ money(r.tongDoanhThu) }}</td>
                  <td class="text-center">
                    <span class="status" :class="Number(r.trangThai) === 1 ? 'st-open' : 'st-closed'">
                      {{ Number(r.trangThai) === 1 ? "Đang mở" : "Đã đóng" }}
                    </span>
                  </td>
                </tr>
                </tbody>
              </table>
            </div>
          </template>
        </div>
      </div>
    </template>

    <!-- STAFF (giữ nguyên) -->
    <div v-else class="card shadow-sm">
      <div class="card-body">
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border" role="status"></div>
          <div class="mt-2 text-muted">Đang tải...</div>
        </div>

        <template v-else>
          <div v-if="!phien" class="alert alert-warning mb-0">
            Chưa có phiên ca đang mở. Nếu bạn chưa tới ca, bạn sẽ ở <b>chế độ xem</b>.
          </div>

          <template v-else>
            <div class="row g-3">
              <div class="col-12 col-lg-6">
                <div class="p-3 rounded-3 bg-light border">
                  <div class="fw-semibold">Thông tin ca</div>
                  <div class="text-muted mt-1">{{ phien.tenCa || phien.caTen || "Ca làm việc" }}</div>
                  <div class="mt-2 small text-muted">
                    Mở lúc: <b>{{ fmtDt(phien.thoiGianMo) }}</b>
                  </div>
                  <div class="small text-muted" v-if="shift.secondsToEnd !== null">
                    Còn lại: <b>{{ fmtCountdown(shift.secondsToEnd) }}</b>
                  </div>
                </div>
              </div>

              <div class="col-12 col-lg-6">
                <div class="p-3 rounded-3 bg-light border">
                  <div class="fw-semibold">Nhập tiền thực tế (kết toán)</div>
                  <div class="row g-2 mt-1">
                    <div class="col-12">
                      <label class="form-label mb-1">Tiền mặt</label>
                      <input
                          class="form-control"
                          type="text"
                          inputmode="numeric"
                          :value="tienMatThucTeText"
                          :disabled="isLocked"
                          @input="onTienMatThucTeInput"
                      />
                    </div>
                    <div class="col-12">
                      <label class="form-label mb-1">Chuyển khoản</label>
                      <input
                          class="form-control"
                          type="text"
                          inputmode="numeric"
                          :value="tienTaiKhoanThucTeText"
                          :disabled="isLocked"
                          @input="onTienTaiKhoanThucTeInput"
                      />
                    </div>
                  </div>

                  <div v-if="isLocked" class="mt-2 text-muted small">
                    Bạn đang ở <b>chế độ xem</b> / hoặc chưa tới ca. Không thể xác nhận đóng ca.
                  </div>
                </div>
              </div>
            </div>

            <div class="d-flex justify-content-end mt-3">
              <button class="btn btn-primary btn-confirm" :disabled="isLocked || submitting" @click="confirmClose">
                <span v-if="submitting" class="spinner-border spinner-border-sm me-2" role="status"></span>
                Xác nhận
              </button>
            </div>
          </template>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { useAuthStore } from "@/stores/auth";
import { useShiftStore } from "@/stores/shift";
import giaoCaApi from "@/services/giaoCaApi";
import { useToast } from "@/composables/useToast";

const auth = useAuthStore();
const shift = useShiftStore();
const toast = useToast();

const isAdmin = computed(() => auth.isAdmin);
const isLocked = computed(() => auth.isStaff && shift.isLocked);

const loading = ref(false);
const submitting = ref(false);

// STAFF
const phien = ref(null);
const tienMatThucTe = ref(0);
const tienTaiKhoanThucTe = ref(0);
const tienMatThucTeText = ref("0");
const tienTaiKhoanThucTeText = ref("0");

// ADMIN
const adminRows = ref([]);
const keyword = ref("");
const fromDate = ref("");
const toDate = ref("");

// debounce auto search
let t = null;
function triggerAutoSearch() {
  if (!isAdmin.value) return;
  clearTimeout(t);
  t = setTimeout(() => loadAdmin(), 350);
}

onBeforeUnmount(() => {
  clearTimeout(t);
});

function resetFilters() {
  keyword.value = "";
  fromDate.value = "";
  toDate.value = "";
  triggerAutoSearch();
}

function money(v) {
  const n = Number(v || 0);
  return new Intl.NumberFormat("vi-VN").format(n) + " đ";
}

function onlyDigits(value) {
  return String(value ?? "").replace(/[^\d]/g, "");
}

function formatMoneyInput(value) {
  return Number(value || 0).toLocaleString("vi-VN");
}

function onTienMatThucTeInput(event) {
  const raw = onlyDigits(event.target.value);
  const value = raw ? Number(raw) : 0;

  tienMatThucTe.value = value;
  tienMatThucTeText.value = raw ? formatMoneyInput(value) : "";
  event.target.value = tienMatThucTeText.value;
}

function onTienTaiKhoanThucTeInput(event) {
  const raw = onlyDigits(event.target.value);
  const value = raw ? Number(raw) : 0;

  tienTaiKhoanThucTe.value = value;
  tienTaiKhoanThucTeText.value = raw ? formatMoneyInput(value) : "";
  event.target.value = tienTaiKhoanThucTeText.value;
}

function fmtDt(v) {
  if (!v) return "-";
  const d = new Date(v);
  if (Number.isNaN(d.getTime())) return String(v);
  return new Intl.DateTimeFormat("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(d);
}

function fmtCountdown(sec) {
  const s = Math.max(0, Number(sec || 0));
  const h = Math.floor(s / 3600);
  const m = Math.floor((s % 3600) / 60);
  const ss = s % 60;
  if (h > 0) return `${h}h ${m}p ${ss}s`;
  if (m > 0) return `${m}p ${ss}s`;
  return `${ss}s`;
}

async function loadStaff() {
  try {
    loading.value = true;
    phien.value = await giaoCaApi.hienTai();

    tienMatThucTe.value = Number(phien.value?.tienMatThucTe || 0);
    tienTaiKhoanThucTe.value = Number(phien.value?.tienTaiKhoanThucTe || 0);

    tienMatThucTeText.value = formatMoneyInput(tienMatThucTe.value);
    tienTaiKhoanThucTeText.value = formatMoneyInput(tienTaiKhoanThucTe.value);
  } catch {
    phien.value = null;
    tienMatThucTe.value = 0;
    tienTaiKhoanThucTe.value = 0;
    tienMatThucTeText.value = "0";
    tienTaiKhoanThucTeText.value = "0";
  } finally {
    loading.value = false;
  }
}

async function confirmClose() {
  if (isLocked.value) return;
  if (!phien.value) return;

  try {
    submitting.value = true;
    await shift.closeShift({
      tienMatThucTe: Number(tienMatThucTe.value || 0),
      tienTaiKhoanThucTe: Number(tienTaiKhoanThucTe.value || 0),
    });
    toast.success("Đã kết toán và đóng ca.");
    await loadStaff();
  } catch (e) {
    toast.error(e?.response?.data?.message || e?.message || "Không đóng ca được");
  } finally {
    submitting.value = false;
  }
}

async function loadAdmin() {
  try {
    loading.value = true;
    const data = await giaoCaApi.adminList({
      keyword: keyword.value,
      fromDate: fromDate.value,
      toDate: toDate.value,
    });

    if (Array.isArray(data)) adminRows.value = data;
    else if (data && Array.isArray(data.items)) adminRows.value = data.items;
    else if (data && Array.isArray(data.content)) adminRows.value = data.content;
    else adminRows.value = [];
  } catch (e) {
    toast.error(e?.response?.data?.message || e?.message || "Không tải được danh sách phiên ca");
    adminRows.value = [];
  } finally {
    loading.value = false;
  }
}

// Nếu bạn muốn auto-load ngay khi thay filter (ngoài @input/@change)
// thì watch cũng đảm bảo chắc chắn (nhưng đã có triggerAutoSearch rồi).
watch([keyword, fromDate, toDate], () => {
  // Tránh double-call nếu bạn đã dùng @input/@change, nhưng vẫn ok vì debounce.
  triggerAutoSearch();
});

onMounted(async () => {
  if (isAdmin.value) await loadAdmin();
  else await loadStaff();
});
</script>

<style scoped>
/* ===== Filter giống trang lịch của tôi ===== */
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
  transition: transform 0.2s ease;
}
.filter-header[aria-expanded="false"] .filter-icon {
  transform: rotate(-90deg);
}
.filter-body {
  background: #f8fafc;
}
.filter-card .form-label {
  font-weight: 600;
}
.filter-card .form-control {
  border-radius: 10px;
}

/* ===== Table giống trang lịch của tôi ===== */
.table-wrap {
  border: 1px solid #dee2e6;
  border-radius: 12px;
  overflow: auto;
  background: #fff;
}
.custom-table {
  width: 100%;
  min-width: 1000px;
  border-collapse: separate;
  border-spacing: 0;
}
.custom-table th,
.custom-table td {
  padding: 12px;
  border-bottom: 1px solid #e9ecef;
  vertical-align: middle;
  white-space: nowrap;
}
.custom-table thead th {
  background: #1f2a44;
  color: #fff;
  font-weight: 700;
}

/* status badge giữ như cũ */
.status {
  display: inline-flex;
  align-items: center;
  padding: 6px 10px;
  border-radius: 999px;
  font-weight: 800;
  font-size: 12px;
}
.st-open {
  background: rgba(245, 158, 11, 0.2);
  color: #b45309;
}
.st-closed {
  background: rgba(34, 197, 94, 0.2);
  color: #15803d;
}

.btn-confirm {
  border-radius: 12px;
  font-weight: 800;
  padding: 10px 14px;
}
</style>
