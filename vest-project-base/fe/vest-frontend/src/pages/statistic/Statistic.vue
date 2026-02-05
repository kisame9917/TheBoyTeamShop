<template>
  <div class="container-fluid py-3">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-bar-chart-line fs-4"></i>
        <h5 class="mb-0">Thống kê</h5>
      </div>
    </div>

    <!-- ================= DOANH THU ================= -->
    <div class="card shadow-sm mb-3">
      <div class="card-body">
        <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-2">
          <div class="d-flex align-items-center gap-2">
            <i class="bi bi-cash-coin fs-5 text-success"></i>
            <div class="fw-semibold">Doanh thu</div>

            <select
                class="form-select form-select-sm ms-2"
                style="width: 140px"
                v-model="chartFilter.type"
                @change="onChangeChartType"
            >
              <option value="THANG">Theo tháng</option>
              <option value="QUY">Theo quý</option>
              <option value="NAM">Theo năm</option>
            </select>

            <!-- THÁNG -->
            <template v-if="chartFilter.type === 'THANG'">
              <input
                  type="month"
                  class="form-control form-control-sm"
                  style="width: 160px"
                  v-model="chartFilter.monthValue"
                  @change="loadRevenue(false)"
              />
            </template>

            <!-- QUÝ -->
            <template v-else-if="chartFilter.type === 'QUY'">
              <select
                  class="form-select form-select-sm"
                  style="width: 120px"
                  v-model.number="chartFilter.quarter"
                  @change="loadRevenue(false)"
              >
                <option v-for="q in [1,2,3,4]" :key="q" :value="q">Q{{ q }}</option>
              </select>

              <select
                  class="form-select form-select-sm"
                  style="width: 120px"
                  v-model.number="chartFilter.year"
                  @change="loadRevenue(false)"
              >
                <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}</option>
              </select>
            </template>

            <!-- NĂM -->
            <template v-else>
              <select
                  class="form-select form-select-sm"
                  style="width: 120px"
                  v-model.number="chartFilter.year"
                  @change="loadRevenue(false)"
              >
                <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}</option>
              </select>
            </template>
          </div>

          <div class="d-flex align-items-center gap-2">
            <button class="btn btn-outline-primary btn-sm" type="button" @click="openCompareModal">
              <i class="bi bi-arrow-left-right me-1"></i> So sánh
            </button>

            <button
                v-if="compareMeta.active"
                class="btn btn-outline-secondary btn-sm"
                type="button"
                @click="removeCompare"
                title="Bỏ so sánh"
            >
              <i class="bi bi-x-lg me-1"></i> Bỏ so sánh
            </button>
          </div>
        </div>

        <!-- Chart -->
        <div class="chart-wrap">
          <div v-if="loadingChart" class="d-flex align-items-center justify-content-center text-muted h-100">
            <div class="spinner-border spinner-border-sm me-2" role="status"></div>
            Đang tải dữ liệu...
          </div>

          <Line v-else-if="chartData?.labels?.length" :data="chartData" :options="chartOptions" />

          <div v-else class="d-flex align-items-center justify-content-center text-muted h-100">
            Chưa có dữ liệu
          </div>
        </div>

        <!-- Tổng doanh thu dưới biểu đồ -->
        <div class="mt-2 d-flex flex-wrap align-items-center justify-content-between gap-2">
          <div class="text-muted total-revenue-line">
            <span class="fw-semibold">Tổng doanh thu {{ currentPeriodLabel }}:</span>
            <span class="ms-1 total-revenue-value">{{ formatCurrency(totalRevenue) }}</span>

            <span v-if="compareMeta.active" class="ms-2">
              <span class="text-muted">| So sánh:</span>
              <span class="ms-1 fw-semibold text-dark">{{ compareMeta.labelA }}</span>
              <span class="mx-1 text-muted">vs</span>
              <span class="fw-semibold text-dark">{{ compareMeta.labelB }}</span>
            </span>
          </div>

          <div class="text-muted small">
            <i class="bi bi-info-circle me-1"></i> Đơn vị: VND
          </div>
        </div>
      </div>
    </div>

    <!-- ================= FILTER RANGE (flatpickr giống trang khác) ================= -->
    <div class="card shadow-sm mb-3">
      <div class="card-body py-3">
        <form @submit.prevent="fetchTabularData" class="row g-3 align-items-end">
          <!-- Từ ngày -->
          <div class="col-12 col-lg-3">
            <label class="form-label">Từ ngày</label>
            <div class="input-group">
              <input ref="fromPickerRef" type="text" class="form-control" placeholder="dd/mm/yyyy" />
              <button class="btn btn-outline-secondary" type="button" @click="openFromPicker" title="Chọn ngày">
                <i class="bi bi-calendar3"></i>
              </button>
              <button class="btn btn-outline-secondary" type="button" @click="clearFromDate" title="Xóa">
                <i class="bi bi-x-lg"></i>
              </button>
            </div>
          </div>

          <!-- Đến ngày -->
          <div class="col-12 col-lg-3">
            <label class="form-label">Đến ngày</label>
            <div class="input-group">
              <input ref="toPickerRef" type="text" class="form-control" placeholder="dd/mm/yyyy" />
              <button class="btn btn-outline-secondary" type="button" @click="openToPicker" title="Chọn ngày">
                <i class="bi bi-calendar3"></i>
              </button>
              <button class="btn btn-outline-secondary" type="button" @click="clearToDate" title="Xóa">
                <i class="bi bi-x-lg"></i>
              </button>
            </div>
          </div>

          <!-- Actions -->
          <div class="col-12 col-lg-6 d-flex justify-content-end gap-2">
            <button type="submit" class="btn btn-primary btn-sm" :disabled="loadingTable || !filter.from || !filter.to">
              <i class="bi bi-funnel me-1"></i>
              {{ loadingTable ? "Đang lọc..." : "Lọc dữ liệu" }}
            </button>

            <button type="button" class="btn btn-outline-secondary btn-sm" @click="resetFilter" title="Đặt lại mặc định">
              <i class="bi bi-arrow-counterclockwise me-1"></i> Đặt lại
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- ================= TOP BÁN CHẠY + ĐƠN HÀNG (6/6) ================= -->
    <div class="row g-3 mb-3">
      <div class="col-12 col-lg-6">
        <div class="card shadow-sm h-100">
          <div class="card-header stats-card-header">
            <div class="d-flex align-items-center justify-content-between">
              <div class="d-flex align-items-center gap-2">
                <i class="bi bi-trophy text-warning"></i>
                <div class="fw-semibold">Top bán chạy</div>
              </div>
              <span class="badge bg-light text-dark">Top 10</span>
            </div>
          </div>

          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
              <thead class="table-light">
              <tr>
                <th class="ps-3">Sản phẩm</th>
                <th class="text-center" style="width: 120px">Đã bán</th>
                <th class="text-center" style="width: 120px">Tồn</th>
              </tr>
              </thead>

              <tbody>
              <tr v-for="(item, idx) in topSelling" :key="item.idSanPham ?? idx">
                <td class="ps-3">
                  <div class="d-flex align-items-center gap-2">
                    <span class="badge text-bg-light border" style="min-width: 34px; text-align:center;">#{{ idx + 1 }}</span>
                    <span class="text-truncate" style="max-width: 360px;" :title="item.tenSanPham">
                        {{ item.tenSanPham }}
                      </span>
                  </div>
                </td>

                <td class="text-center">
                    <span class="badge text-bg-success bg-opacity-10 text-success border border-success-subtle">
                      {{ item.soLuongDaBan ?? 0 }}
                    </span>
                </td>

                <td class="text-center">
                    <span class="badge text-bg-secondary bg-opacity-10 text-secondary border border-secondary-subtle">
                      {{ item.soLuongTonKho ?? 0 }}
                    </span>
                </td>
              </tr>

              <tr v-if="!loadingTable && topSelling.length === 0">
                <td colspan="3" class="text-center text-muted py-4">Không có dữ liệu</td>
              </tr>

              <tr v-if="loadingTable && topSelling.length === 0">
                <td colspan="3" class="text-center text-muted py-4">
                  <div class="spinner-border spinner-border-sm me-2" role="status"></div>
                  Đang tải...
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="col-12 col-lg-6">
        <div class="card shadow-sm h-100">
          <div class="card-header stats-card-header">
            <div class="d-flex align-items-center justify-content-between">
              <div class="d-flex align-items-center gap-2">
                <i class="bi bi-receipt text-primary"></i>
                <div class="fw-semibold">Đơn hàng</div>
              </div>
              <span class="badge bg-light text-dark">Tổng: {{ totalOrders }}</span>
            </div>
          </div>

          <div class="card-body">
            <div class="mb-2">
              <div class="d-flex align-items-center justify-content-between">
                <div class="text-muted small">Tỷ lệ hoàn thành</div>
                <div class="fw-semibold small">{{ completionRate.toFixed(1) }}%</div>
              </div>

              <div class="progress mt-1" style="height: 8px;">
                <div class="progress-bar" role="progressbar" :style="{ width: completionRate + '%' }"></div>
              </div>
            </div>

            <div class="order-states">
              <div v-for="st in orderStats" :key="st.code" class="order-state">
                <div class="d-flex align-items-center gap-2">
                  <span class="state-icon" :class="iconBgClass(st.kieuDang)">
                    <i class="bi" :class="iconClass(st)"></i>
                  </span>

                  <div class="flex-grow-1">
                    <div class="small fw-semibold">{{ st.tenTrangThai }}</div>
                  </div>

                  <div class="fw-bold">{{ st.soLuong ?? 0 }}</div>
                </div>
              </div>
            </div>

            <div class="row g-2 mt-2">
              <div class="col-6">
                <div class="p-2 rounded border bg-light">
                  <div class="text-muted small">Hoàn thành</div>
                  <div class="fw-bold text-success">{{ completedCount }}</div>
                </div>
              </div>
              <div class="col-6">
                <div class="p-2 rounded border bg-light">
                  <div class="text-muted small">Đã huỷ</div>
                  <div class="fw-bold text-danger">{{ canceledCount }}</div>
                </div>
              </div>
            </div>

            <div v-if="!loadingTable && orderStats.length === 0" class="text-muted text-center mt-3">
              Chưa có dữ liệu đơn hàng trong khoảng này.
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ================= KHÁCH HÀNG VIP + BÁN CHẬM/TỒN KHO (6/6) ================= -->
    <div class="row g-3">
      <div class="col-12 col-lg-6">
        <div class="card shadow-sm h-100">
          <div class="card-header stats-card-header">
            <div class="d-flex align-items-center justify-content-between">
              <div class="d-flex align-items-center gap-2">
                <i class="bi bi-people text-info"></i>
                <div class="fw-semibold">Khách hàng tiềm năng</div>
              </div>
              <span class="badge bg-light text-dark">Top chi tiêu</span>
            </div>
          </div>

          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
              <thead class="table-light">
              <tr>
                <th class="ps-3">Khách hàng</th>
                <th class="text-center" style="width: 120px">Số đơn</th>
                <th class="text-end pe-3" style="width: 170px">Tổng chi tiêu</th>
              </tr>
              </thead>

              <tbody>
              <tr v-for="kh in topCustomers" :key="kh.idKhachHang">
                <td class="ps-3">
                  <div class="fw-semibold">{{ kh.tenKhachHang }}</div>
                  <div class="text-muted small">{{ kh.soDienThoai }}</div>
                </td>

                <td class="text-center">
                    <span class="badge text-bg-info bg-opacity-10 text-info border border-info-subtle">
                      {{ kh.soLanMua ?? 0 }}
                    </span>
                </td>

                <td class="text-end pe-3 fw-bold">
                  {{ formatCurrency(kh.tongTienChiTieu) }}
                </td>
              </tr>

              <tr v-if="!loadingTable && topCustomers.length === 0">
                <td colspan="3" class="text-center text-muted py-4">
                  Chưa có dữ liệu khách hàng trong khoảng thời gian này.
                </td>
              </tr>

              <tr v-if="loadingTable && topCustomers.length === 0">
                <td colspan="3" class="text-center text-muted py-4">
                  <div class="spinner-border spinner-border-sm me-2" role="status"></div>
                  Đang tải...
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="col-12 col-lg-6">
        <div class="card shadow-sm h-100">
          <div class="card-header stats-card-header">
            <div class="d-flex align-items-center justify-content-between">
              <div class="d-flex align-items-center gap-2">
                <i class="bi bi-box-seam text-warning"></i>
                <div class="fw-semibold">Bán chậm & tồn kho</div>
              </div>
              <span class="badge bg-light text-dark">Chưa bán được</span>
            </div>
          </div>

          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
              <thead class="table-light">
              <tr>
                <th class="ps-3">Sản phẩm</th>
                <th class="text-center" style="width: 120px">Đã bán</th>
                <th class="text-center" style="width: 120px">Tồn</th>
              </tr>
              </thead>

              <tbody>
              <tr v-for="sp in slowMoving" :key="sp.idSanPham">
                <td class="ps-3">
                    <span class="text-truncate d-inline-block" style="max-width: 360px;" :title="sp.tenSanPham">
                      {{ sp.tenSanPham }}
                    </span>
                </td>

                <td class="text-center">
                    <span class="badge text-bg-secondary bg-opacity-10 text-secondary border border-secondary-subtle">
                      {{ sp.soLuongDaBan ?? 0 }}
                    </span>
                </td>

                <td class="text-center">
                  <span class="fw-bold text-danger">{{ sp.soLuongTonKho ?? 0 }}</span>
                </td>
              </tr>

              <tr v-if="!loadingTable && slowMoving.length === 0">
                <td colspan="3" class="text-center text-muted py-4">
                  Không có dữ liệu bán chậm trong khoảng thời gian này.
                </td>
              </tr>

              <tr v-if="loadingTable && slowMoving.length === 0">
                <td colspan="3" class="text-center text-muted py-4">
                  <div class="spinner-border spinner-border-sm me-2" role="status"></div>
                  Đang tải...
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- ================= MODAL SO SÁNH (BOOTSTRAP CHUẨN - ĐẸP NHƯ BAN ĐẦU) ================= -->
    <div
        v-if="compareModal.open"
        class="modal fade show d-block"
        tabindex="-1"
        role="dialog"
        aria-modal="true"
        @mousedown.self="closeCompareModal"
    >
      <div class="modal-dialog modal-dialog-centered" role="document">
        <div class="modal-content shadow">
          <div class="modal-header">
            <h6 class="modal-title fw-semibold">
              <i class="bi bi-arrow-left-right me-2"></i>So sánh doanh thu
            </h6>
            <button type="button" class="btn-close" @click="closeCompareModal"></button>
          </div>

          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label">So sánh theo</label>
              <select class="form-select" v-model="compareModal.type" @change="onCompareTypeChange">
                <option value="THANG">Theo tháng</option>
                <option value="QUY">Theo quý</option>
                <option value="NAM">Theo năm</option>
              </select>
            </div>

            <template v-if="compareModal.type === 'THANG'">
              <div class="row g-2">
                <div class="col-12 col-md-6">
                  <label class="form-label">Tháng A</label>
                  <select class="form-select" v-model="compareModal.monthA">
                    <option v-for="m in monthOptions" :key="'a-'+m.value" :value="m.value">{{ m.label }}</option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label">Tháng B</label>
                  <select class="form-select" v-model="compareModal.monthB">
                    <option v-for="m in monthOptions" :key="'b-'+m.value" :value="m.value">{{ m.label }}</option>
                  </select>
                </div>
              </div>
            </template>

            <template v-else-if="compareModal.type === 'QUY'">
              <div class="row g-2">
                <div class="col-12 col-md-6">
                  <label class="form-label">Quý A</label>
                  <select class="form-select" v-model="compareModal.quarterA">
                    <option v-for="q in quarterOptions" :key="'qa-'+q.value" :value="q.value">{{ q.label }}</option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label">Quý B</label>
                  <select class="form-select" v-model="compareModal.quarterB">
                    <option v-for="q in quarterOptions" :key="'qb-'+q.value" :value="q.value">{{ q.label }}</option>
                  </select>
                </div>
              </div>
            </template>

            <template v-else>
              <div class="row g-2">
                <div class="col-12 col-md-6">
                  <label class="form-label">Năm A</label>
                  <select class="form-select" v-model.number="compareModal.yearA">
                    <option v-for="y in yearOptions" :key="'ya-'+y" :value="y">{{ y }}</option>
                  </select>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label">Năm B</label>
                  <select class="form-select" v-model.number="compareModal.yearB">
                    <option v-for="y in yearOptions" :key="'yb-'+y" :value="y">{{ y }}</option>
                  </select>
                </div>
              </div>
            </template>

            <div class="text-muted small mt-2">
              <i class="bi bi-lightbulb me-1"></i>
              Sau khi so sánh, biểu đồ sẽ hiển thị 2 đường doanh thu.
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn btn-light" type="button" @click="closeCompareModal">Hủy</button>
            <button class="btn btn-primary" type="button" @click="applyCompare" :disabled="compareModal.loading">
              <span v-if="compareModal.loading" class="spinner-border spinner-border-sm me-2" role="status"></span>
              So sánh
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="compareModal.open" class="modal-backdrop fade show" @click="closeCompareModal"></div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch, nextTick } from "vue";
import thongKeApi from "@/services/thongKeApi";

import flatpickr from "flatpickr";
import { Vietnamese } from "flatpickr/dist/l10n/vn.js";
import "flatpickr/dist/flatpickr.css";

import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Tooltip,
  Legend,
} from "chart.js";
import { Line } from "vue-chartjs";

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Tooltip, Legend);

/** =================== STATE =================== */
const loadingChart = ref(false);
const loadingTable = ref(false);

const totalRevenue = ref(0);

const topSelling = ref([]);
const slowMoving = ref([]);
const topCustomers = ref([]);
const orderStats = ref([]);

/** ======= Chart filter (mặc định: tháng hiện tại) ======= */
const now = new Date();
const currentYear = now.getFullYear();
const currentMonth = String(now.getMonth() + 1).padStart(2, "0");
const currentMonthValue = `${currentYear}-${currentMonth}`;
const currentQuarter = Math.floor(now.getMonth() / 3) + 1;

const chartFilter = reactive({
  type: "THANG",
  monthValue: currentMonthValue,
  quarter: currentQuarter,
  year: currentYear,
});

/** ======= Filter range ISO ======= */
const filter = reactive({ from: "", to: "" });

/** ======= Flatpickr refs ======= */
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
      dateFormat: "d/m/Y",
      allowInput: true,
      // ✅ fix vị trí: input bên trái -> xổ dưới và canh phải để không đè sidebar
      position: "below right",
      defaultDate: parseYMD(filter.from),
      onChange: (selectedDates) => {
        const d = selectedDates?.[0] || null;
        filter.from = d ? flatpickr.formatDate(d, "Y-m-d") : "";
      },
    });
  }

  if (toPickerRef.value && !fpTo) {
    fpTo = flatpickr(toPickerRef.value, {
      locale: Vietnamese,
      dateFormat: "d/m/Y",
      allowInput: true,
      // ✅ input bên phải -> canh trái
      position: "below left",
      defaultDate: parseYMD(filter.to),
      onChange: (selectedDates) => {
        const d = selectedDates?.[0] || null;
        filter.to = d ? flatpickr.formatDate(d, "Y-m-d") : "";
      },
    });
  }

  // sync min/max
  if (fpTo) fpTo.set("minDate", filter.from ? parseYMD(filter.from) : null);
  if (fpFrom) fpFrom.set("maxDate", filter.to ? parseYMD(filter.to) : null);
}

function openFromPicker() {
  fpFrom?.open();
}
function openToPicker() {
  fpTo?.open();
}
function clearFromDate() {
  filter.from = "";
  fpFrom?.clear();
  if (fpTo) fpTo.set("minDate", null);
}
function clearToDate() {
  filter.to = "";
  fpTo?.clear();
  if (fpFrom) fpFrom.set("maxDate", null);
}

watch(
    () => filter.from,
    (v) => {
      if (fpTo) fpTo.set("minDate", v ? parseYMD(v) : null);
    }
);
watch(
    () => filter.to,
    (v) => {
      if (fpFrom) fpFrom.set("maxDate", v ? parseYMD(v) : null);
    }
);

onBeforeUnmount(() => {
  try { fpFrom?.destroy(); } catch {}
  try { fpTo?.destroy(); } catch {}
});

/** =================== OPTIONS =================== */
const yearOptions = computed(() => {
  const y = new Date().getFullYear();
  return Array.from({ length: 10 }, (_, i) => y - i);
});

const monthOptions = computed(() => {
  const opts = [];
  const d = new Date();
  d.setDate(1);
  for (let i = 0; i < 36; i++) {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, "0");
    opts.push({ value: `${y}-${m}`, label: `${m}/${y}` });
    d.setMonth(d.getMonth() - 1);
  }
  return opts;
});

const quarterOptions = computed(() => {
  const opts = [];
  const d = new Date();
  const y0 = d.getFullYear();
  const q0 = Math.floor(d.getMonth() / 3) + 1;

  let y = y0;
  let q = q0;

  for (let i = 0; i < 12; i++) {
    opts.push({ value: `${y}-${q}`, label: `Q${q}/${y}` });
    q -= 1;
    if (q === 0) {
      q = 4;
      y -= 1;
    }
  }
  return opts;
});

/** =================== CHART =================== */
const chartData = ref({ labels: [], datasets: [] });

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  interaction: { mode: "index", intersect: false },
  plugins: {
    legend: {
      position: "top",
      align: "end",
      labels: {
        // ✅ legend to hơn
        font: { size: 13, weight: "600" },
        boxWidth: 28,
        boxHeight: 10,
      },
    },
    tooltip: {
      callbacks: {
        label: (ctx) => {
          const label = ctx.dataset.label ? `${ctx.dataset.label}: ` : "";
          return label + new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" })
              .format(ctx.parsed.y || 0);
        },
      },
    },
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        callback: (val) => {
          const n = Number(val);
          if (n >= 1_000_000_000) return `${(n / 1_000_000_000).toFixed(1)}B`;
          if (n >= 1_000_000) return `${(n / 1_000_000).toFixed(1)}M`;
          return n.toLocaleString("vi-VN");
        },
      },
    },
  },
};

/** =================== COMPARE =================== */
const compareMeta = reactive({ active: false, labelA: "", labelB: "" });

const compareModal = reactive({
  open: false,
  loading: false,
  type: "THANG",
  monthA: currentMonthValue,
  monthB: monthOptions.value?.[1]?.value || currentMonthValue,
  quarterA: `${currentYear}-${currentQuarter}`,
  quarterB: quarterOptions.value?.[1]?.value || `${currentYear}-${currentQuarter}`,
  yearA: currentYear,
  yearB: currentYear - 1,
});

watch(
    () => compareModal.open,
    (open) => {
      // hiệu ứng giống bootstrap modal (khóa scroll)
      document.body.style.overflow = open ? "hidden" : "";
    }
);

/** =================== COMPUTED =================== */
const currentPeriodLabel = computed(() => {
  if (chartFilter.type === "THANG") return `tháng ${formatMonthLabel(chartFilter.monthValue)}`;
  if (chartFilter.type === "QUY") return `Q${chartFilter.quarter}/${chartFilter.year}`;
  return `năm ${chartFilter.year}`;
});

const totalOrders = computed(() => orderStats.value.reduce((s, x) => s + Number(x.soLuong || 0), 0));
const completedCount = computed(() => Number(orderStats.value.find((t) => t.trangThai === "HOAN_THANH")?.soLuong || 0));
const canceledCount = computed(() => Number(orderStats.value.find((t) => t.trangThai === "DA_HUY")?.soLuong || 0));
const completionRate = computed(() => {
  const total = totalOrders.value;
  if (!total) return 0;
  return (completedCount.value / total) * 100;
});

/** =================== HELPERS =================== */
function formatCurrency(val) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(Number(val || 0));
}

function formatMonthLabel(yyyyMM) {
  if (!yyyyMM) return "";
  const [y, m] = yyyyMM.split("-");
  return `${m}/${y}`;
}

function parseMonthValue(yyyyMM) {
  const [y, m] = (yyyyMM || "").split("-");
  return { year: Number(y), month: Number(m) };
}

function parseQuarterValue(val) {
  if (!val) return { year: NaN, quarter: NaN };
  if (val.includes("-")) {
    const [y, q] = val.split("-");
    return { year: Number(y), quarter: Number(q) };
  }
  const m = String(val).match(/Q\s*(\d)\s*\/\s*(\d{4})/i);
  if (m) return { quarter: Number(m[1]), year: Number(m[2]) };
  return { year: NaN, quarter: NaN };
}

function sumRevenue(list) {
  return (list || []).reduce((s, x) => s + Number(x.doanhThu || 0), 0);
}

function sortLabelsByType(type, labels) {
  if (type === "THANG") return labels.map(String).sort((a, b) => Number(a) - Number(b));
  return labels.map(String).sort((a, b) => Number(a.replace("T", "")) - Number(b.replace("T", "")));
}

function buildSeriesByLabels(list, labels) {
  const map = new Map();
  for (const item of list || []) map.set(String(item.thoiGian), Number(item.doanhThu || 0));
  return labels.map((lb) => map.get(String(lb)) ?? 0);
}

/** icon helpers */
function iconClass(st) {
  const k = String(st?.trangThai || "");
  if (k === "CHO_XAC_NHAN") return "bi-clock";
  if (k === "DANG_XU_LY") return "bi-gear";
  if (k === "DANG_GIAO") return "bi-truck";
  if (k === "DA_GIAO") return "bi-box-seam";
  if (k === "HOAN_THANH") return "bi-check2-circle";
  if (k === "DA_HUY") return "bi-x-circle";
  if (k === "YEU_CAU_HOAN") return "bi-arrow-counterclockwise";
  if (k === "DA_HOAN") return "bi-arrow-return-left";
  return "bi-dot";
}

function iconBgClass(style) {
  if (style === "success") return "bg-success-subtle text-success";
  if (style === "danger") return "bg-danger-subtle text-danger";
  if (style === "warning") return "bg-warning-subtle text-warning";
  if (style === "info") return "bg-info-subtle text-info";
  if (style === "secondary") return "bg-secondary-subtle text-secondary";
  return "bg-primary-subtle text-primary";
}

/** =================== API LOADERS =================== */
async function loadRevenue(keepCompare = false) {
  loadingChart.value = true;
  try {
    const params = buildRevenueParamsFromChartFilter();
    const data = await thongKeApi.getDoanhThu(params);

    totalRevenue.value = sumRevenue(data);

    if (!keepCompare) {
      compareMeta.active = false;
      compareMeta.labelA = "";
      compareMeta.labelB = "";
    }

    chartData.value = {
      labels: (data || []).map((x) => x.thoiGian),
      datasets: [
        {
          label: `Doanh thu (${currentPeriodLabel.value})`,
          data: (data || []).map((x) => Number(x.doanhThu || 0)),
          borderColor: "#0d6efd",
          backgroundColor: "transparent",
          tension: 0.35,
          pointRadius: 2,
          pointHoverRadius: 4,
        },
      ],
    };
  } finally {
    loadingChart.value = false;
  }
}

function buildRevenueParamsFromChartFilter() {
  if (chartFilter.type === "THANG") {
    const { year, month } = parseMonthValue(chartFilter.monthValue);
    return { type: "THANG", year, month };
  }
  if (chartFilter.type === "QUY") {
    return { type: "QUY", year: Number(chartFilter.year), quarter: Number(chartFilter.quarter) };
  }
  return { type: "NAM", year: Number(chartFilter.year) };
}

async function fetchTabularData() {
  if (!filter.from || !filter.to) {
    alert("Vui lòng chọn đầy đủ Từ ngày / Đến ngày");
    return;
  }

  loadingTable.value = true;
  try {
    const { from, to } = filter;

    const [sell, slow, cust, orders] = await Promise.all([
      thongKeApi.getTopSelling(from, to),
      thongKeApi.getSlowMoving(from, to),
      thongKeApi.getTopCustomers(from, to),
      thongKeApi.getThongKeDonHangRange(from, to),
    ]);

    topSelling.value = sell || [];
    slowMoving.value = slow || [];
    topCustomers.value = cust || [];
    orderStats.value = orders || [];
  } finally {
    loadingTable.value = false;
  }
}

/** =================== FILTER METHODS =================== */
function resetFilter() {
  const today = new Date();
  const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);

  const dateToISO = (d) => {
    const local = new Date(d.getTime() - d.getTimezoneOffset() * 60000);
    return local.toISOString().split("T")[0];
  };

  filter.from = dateToISO(firstDay);
  filter.to = dateToISO(today);

  // sync UI pickers nếu đã init
  fpFrom?.setDate(parseYMD(filter.from), false);
  fpTo?.setDate(parseYMD(filter.to), false);
  if (fpTo) fpTo.set("minDate", parseYMD(filter.from));
  if (fpFrom) fpFrom.set("maxDate", parseYMD(filter.to));

  fetchTabularData();
}

/** =================== CHART TYPE CHANGE =================== */
function onChangeChartType() {
  const d = new Date();
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const q = Math.floor(d.getMonth() / 3) + 1;

  if (chartFilter.type === "THANG") {
    chartFilter.monthValue = `${y}-${m}`;
  } else if (chartFilter.type === "QUY") {
    chartFilter.year = y;
    chartFilter.quarter = q;
  } else {
    chartFilter.year = y;
  }

  loadRevenue(false);
}

/** =================== COMPARE MODAL =================== */
function openCompareModal() {
  compareModal.open = true;
  compareModal.loading = false;

  compareModal.type = chartFilter.type;

  if (compareModal.type === "THANG") {
    compareModal.monthA = chartFilter.monthValue || currentMonthValue;
    compareModal.monthB = monthOptions.value?.[1]?.value || compareModal.monthA;
  } else if (compareModal.type === "QUY") {
    compareModal.quarterA = `${chartFilter.year}-${chartFilter.quarter}`;
    compareModal.quarterB = quarterOptions.value?.[1]?.value || compareModal.quarterA;
  } else {
    compareModal.yearA = chartFilter.year;
    compareModal.yearB = Number(chartFilter.year) - 1;
  }
}

function closeCompareModal() {
  compareModal.open = false;
  compareModal.loading = false;
}

function onCompareTypeChange() {
  if (compareModal.type === "THANG") {
    compareModal.monthA = currentMonthValue;
    compareModal.monthB = monthOptions.value?.[1]?.value || currentMonthValue;
  } else if (compareModal.type === "QUY") {
    compareModal.quarterA = `${currentYear}-${currentQuarter}`;
    compareModal.quarterB = quarterOptions.value?.[1]?.value || `${currentYear}-${currentQuarter}`;
  } else {
    compareModal.yearA = currentYear;
    compareModal.yearB = currentYear - 1;
  }
}

function removeCompare() {
  compareMeta.active = false;
  compareMeta.labelA = "";
  compareMeta.labelB = "";
  loadRevenue(false);
}

async function applyCompare() {
  compareModal.loading = true;
  try {
    const type = compareModal.type;

    let paramsA = null;
    let paramsB = null;
    let labelA = "";
    let labelB = "";

    if (type === "THANG") {
      const a = parseMonthValue(compareModal.monthA);
      const b = parseMonthValue(compareModal.monthB);
      paramsA = { type: "THANG", year: a.year, month: a.month };
      paramsB = { type: "THANG", year: b.year, month: b.month };
      labelA = formatMonthLabel(compareModal.monthA);
      labelB = formatMonthLabel(compareModal.monthB);
    } else if (type === "QUY") {
      const a = parseQuarterValue(compareModal.quarterA);
      const b = parseQuarterValue(compareModal.quarterB);
      paramsA = { type: "QUY", year: a.year, quarter: a.quarter };
      paramsB = { type: "QUY", year: b.year, quarter: b.quarter };
      labelA = `Q${a.quarter}/${a.year}`;
      labelB = `Q${b.quarter}/${b.year}`;
    } else {
      paramsA = { type: "NAM", year: Number(compareModal.yearA) };
      paramsB = { type: "NAM", year: Number(compareModal.yearB) };
      labelA = String(compareModal.yearA);
      labelB = String(compareModal.yearB);
    }

    const [dataA, dataB] = await Promise.all([
      thongKeApi.getDoanhThu(paramsA),
      thongKeApi.getDoanhThu(paramsB),
    ]);

    const labels = sortLabelsByType(
        type,
        Array.from(new Set([...(dataA || []).map((x) => x.thoiGian), ...(dataB || []).map((x) => x.thoiGian)]))
    );

    const seriesA = buildSeriesByLabels(dataA, labels);
    const seriesB = buildSeriesByLabels(dataB, labels);

    // đồng bộ filter UI theo kỳ A
    chartFilter.type = type;
    if (type === "THANG") {
      chartFilter.monthValue = compareModal.monthA;
      const { year } = parseMonthValue(compareModal.monthA);
      chartFilter.year = year;
    } else if (type === "QUY") {
      const { year, quarter } = parseQuarterValue(compareModal.quarterA);
      chartFilter.year = year;
      chartFilter.quarter = quarter;
    } else {
      chartFilter.year = Number(compareModal.yearA);
    }

    totalRevenue.value = seriesA.reduce((s, v) => s + Number(v || 0), 0);

    chartData.value = {
      labels,
      datasets: [
        {
          label: `Doanh thu (${labelA})`,
          data: seriesA,
          borderColor: "#0d6efd",
          backgroundColor: "transparent",
          tension: 0.35,
          pointRadius: 2,
          pointHoverRadius: 4,
        },
        {
          label: `So sánh (${labelB})`,
          data: seriesB,
          borderColor: "#2ecc71",
          backgroundColor: "transparent",
          tension: 0.35,
          borderDash: [6, 4],
          pointRadius: 2,
          pointHoverRadius: 4,
        },
      ],
    };

    compareMeta.active = true;
    compareMeta.labelA = labelA;
    compareMeta.labelB = labelB;

    compareModal.open = false;
  } catch (err) {
    console.error(err);
    const msg = err?.response?.data?.message || err?.response?.data?.error || err?.message || "So sánh thất bại";
    alert(msg);
  } finally {
    compareModal.loading = false;
  }
}

/** =================== INIT =================== */
onMounted(async () => {
  loadRevenue(false);
  resetFilter();

  await nextTick();
  initPickers();

  // sync picker theo filter đã reset
  fpFrom?.setDate(parseYMD(filter.from), false);
  fpTo?.setDate(parseYMD(filter.to), false);
});
</script>

<style scoped>
.chart-wrap {
  height: 340px;
  position: relative;
}

/* tổng doanh thu to hơn */
.total-revenue-line {
  font-size: 1.02rem;
}
.total-revenue-value {
  font-size: 1.12rem;
  font-weight: 600; /* ✅ mảnh hơn */
  color: #111;
}

/* Order widget */
.order-states {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.order-state {
  padding: 8px 10px;
  border: 1px solid #eee;
  border-radius: 10px;
  background: #fff;
}
.state-icon {
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  font-size: 14px;
}

.stats-card-header {
  background: #1f2a3a !important;
  color: #ffffff !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.stats-card-header .fw-semibold,
.stats-card-header .badge,
.stats-card-header i {
  color: #ffffff !important;
}

/* badge nền sáng vẫn giữ chữ đen */
.stats-card-header .badge.bg-light {
  color: #111 !important;
}

/* đảm bảo modal/backdrop luôn nằm trên chart */
.modal { z-index: 1055; }
.modal-backdrop { z-index: 1050; }

/* nếu có tooltip chart “đè” lên modal (hiếm), giảm ưu tiên tooltip */
:deep(.chartjs-tooltip),
:deep(.chartjs-tooltip-key) {
  z-index: 1 !important;
}
</style>
