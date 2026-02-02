<template>
  <div class="container-fluid py-4 dashboard-page">
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <h3 class="mb-0 fw-bold text-primary">
          <i class="bi bi-graph-up-arrow me-2"></i>Thống kê & Báo cáo
        </h3>
        <div class="text-muted small mt-1">Tổng quan tình hình kinh doanh</div>
      </div>
      <button class="btn btn-outline-primary btn-sm" @click="reloadData">
        <i class="bi bi-arrow-clockwise me-1"></i> Reload toàn bộ
      </button>
    </div>

    <div class="row mb-4">
      <div class="col-12">
        <div class="card shadow-sm border-0">
          <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
            <h5 class="mb-0 fw-bold text-dark">So sánh Doanh thu theo Quý</h5>
            <span class="badge bg-light text-dark border">Đơn vị: VNĐ</span>
          </div>
          <div class="card-body">
            <div class="chart-container" style="height: 350px; position: relative;">
              <Bar v-if="chartData.labels.length > 0" :data="chartData" :options="chartOptions" />
              <div v-else class="text-center text-muted py-5 d-flex flex-column align-items-center justify-content-center h-100">
                <div class="spinner-border text-primary mb-2" role="status" v-if="loadingChart"></div>
                <div>{{ loadingChart ? 'Đang tải biểu đồ...' : 'Chưa có dữ liệu biểu đồ' }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="card shadow-sm border-0 mb-4 bg-primary bg-opacity-10">
      <div class="card-body py-3">
        <form @submit.prevent="fetchTabularData" class="row align-items-end g-3">

          <div class="col-md-3">
            <label class="form-label fw-bold small text-primary">Từ ngày</label>
            <input type="date" class="form-control border-primary" v-model="filter.from" required />
          </div>

          <div class="col-md-3">
            <label class="form-label fw-bold small text-primary">Đến ngày</label>
            <input type="date" class="form-control border-primary" v-model="filter.to" required />
          </div>

          <div class="col-md-3 d-flex gap-2">
            <button
                type="button"
                class="btn btn-outline-secondary"
                @click="resetFilter"
                title="Đặt lại mặc định"
                :disabled="loadingTable"
            >
              <i class="bi bi-arrow-counterclockwise"></i>
            </button>

            <button type="submit" class="btn btn-primary flex-grow-1 fw-bold shadow-sm" :disabled="loadingTable">
              <i class="bi bi-filter me-1"></i> {{ loadingTable ? 'Đang lọc...' : 'Lọc dữ liệu' }}
            </button>
          </div>

          <div class="col-md-3 text-end d-none d-md-block">
            <small class="text-muted fst-italic">
              <i class="bi bi-info-circle me-1"></i>Mặc định: Tháng này
            </small>
          </div>
        </form>
      </div>
    </div>

    <div class="row g-4">

      <div class="col-lg-6">
        <div class="card h-100 shadow-sm border-0">
          <div class="card-header bg-success text-white py-2 d-flex justify-content-between">
            <h6 class="mb-0 fw-bold"><i class="bi bi-trophy-fill me-2"></i>Top Bán Chạy</h6>
            <small>Top 10</small>
          </div>
          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
              <thead class="bg-light">
              <tr>
                <th scope="col" class="ps-3">Sản phẩm</th>
                <th scope="col" class="text-center">Đã bán</th>
                <th scope="col" class="text-center">Tồn</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="item in topSelling" :key="item.idSanPham">
                <td class="ps-3">
                  <div class="fw-bold text-truncate" style="max-width: 220px;" :title="item.tenSanPham">
                    {{ item.tenSanPham }}
                  </div>
                </td>
                <td class="text-center">
                  <span class="badge bg-success bg-opacity-10 text-success rounded-pill px-3">{{ item.soLuongDaBan }}</span>
                </td>
                <td class="text-center text-muted small">{{ item.soLuongTonKho }}</td>
              </tr>
              <tr v-if="topSelling.length === 0">
                <td colspan="3" class="text-center py-4 text-muted">
                  <i class="bi bi-inbox fs-4 d-block mb-1"></i> Không có dữ liệu
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="col-lg-6">
        <div class="card h-100 shadow-sm border-0">
          <div class="card-header bg-warning text-dark py-2 d-flex justify-content-between">
            <h6 class="mb-0 fw-bold"><i class="bi bi-exclamation-triangle-fill me-2"></i>Bán Chậm / Tồn Kho</h6>
            <small>Chưa bán được</small>
          </div>
          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
              <thead class="bg-light">
              <tr>
                <th scope="col" class="ps-3">Sản phẩm</th>
                <th scope="col" class="text-center">Đã bán</th>
                <th scope="col" class="text-center">Tồn</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="item in slowMoving" :key="item.idSanPham">
                <td class="ps-3">
                  <div class="fw-medium text-truncate" style="max-width: 220px;" :title="item.tenSanPham">
                    {{ item.tenSanPham }}
                  </div>
                </td>
                <td class="text-center text-muted">-</td>
                <td class="text-center fw-bold text-danger">{{ item.soLuongTonKho }}</td>
              </tr>
              <tr v-if="slowMoving.length === 0">
                <td colspan="3" class="text-center py-4 text-success">
                  <i class="bi bi-check-circle fs-4 d-block mb-1"></i> Tất cả sản phẩm đều bán tốt!
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="col-12">
        <div class="card shadow-sm border-0">
          <div class="card-header bg-primary text-white py-2 d-flex justify-content-between">
            <h6 class="mb-0 fw-bold"><i class="bi bi-people-fill me-2"></i>Khách hàng Tiềm năng (VIP)</h6>
            <small>Top chi tiêu</small>
          </div>
          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
              <thead class="bg-light">
              <tr>
                <th scope="col" class="ps-3">Khách hàng</th>
                <th scope="col">Số điện thoại</th>
                <th scope="col" class="text-center">Số đơn mua</th>
                <th scope="col" class="text-end pe-4">Tổng chi tiêu</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="(kh, index) in topCustomers" :key="kh.idKhachHang">
                <td class="ps-3">
                  <div class="d-flex align-items-center">
                    <div class="rounded-circle bg-primary text-white d-flex align-items-center justify-content-center me-2" style="width: 32px; height: 32px; font-size: 12px;">
                      {{ index + 1 }}
                    </div>
                    <span class="fw-bold text-dark">{{ kh.tenKhachHang }}</span>
                  </div>
                </td>
                <td class="text-muted small">{{ kh.soDienThoai }}</td>
                <td class="text-center"><span class="badge bg-info text-dark">{{ kh.soLanMua }}</span></td>
                <td class="text-end pe-4 fw-bold text-primary">{{ formatCurrency(kh.tongTienChiTieu) }}</td>
              </tr>
              <tr v-if="topCustomers.length === 0">
                <td colspan="4" class="text-center py-4 text-muted">
                  Chưa có dữ liệu khách hàng trong khoảng thời gian này.
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale
} from 'chart.js';
import { Bar } from 'vue-chartjs';
import thongKeApi from '@/services/thongKeApi';
import { useToast } from "@/composables/useToast";

// Đăng ký các component cho Chart.js
ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

const toast = useToast();

// --- STATE ---
const loadingChart = ref(false);
const loadingTable = ref(false);

const chartData = ref({
  labels: [],
  datasets: []
});

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { position: 'top' },
    title: { display: false }
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        callback: function(value) {
          if (value >= 1000000) return value / 1000000 + 'M';
          if (value >= 1000) return value / 1000 + 'k';
          return value;
        }
      }
    }
  }
};

const topSelling = ref([]);
const slowMoving = ref([]);
const topCustomers = ref([]);

// --- FILTER DEFAULTS ---
// Helper: Chuyển Date sang string YYYY-MM-DD theo múi giờ local
const toDateString = (date) => {
  const offset = date.getTimezoneOffset() * 60000;
  return new Date(date.getTime() - offset).toISOString().split('T')[0];
}

// Hàm lấy ngày mặc định (Đầu tháng -> Hôm nay)
const getDefaultFilter = () => {
  const today = new Date();
  const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
  return {
    from: toDateString(firstDay),
    to: toDateString(today)
  };
}

const filter = reactive(getDefaultFilter());

// --- METHODS ---

// ✅ HÀM RESET FILTER
const resetFilter = () => {
  const defaults = getDefaultFilter();
  filter.from = defaults.from;
  filter.to = defaults.to;

  // Tự động load lại dữ liệu sau khi reset
  fetchTabularData();
  toast.info("Đã đặt lại bộ lọc về mặc định.");
}

// 1. Load biểu đồ so sánh Quý
const fetchQuarterStats = async () => {
  loadingChart.value = true;
  try {
    const res = await thongKeApi.compareQuarter();
    const data = res.data;

    // Xử lý response linh hoạt (data array hoặc data.data)
    const items = Array.isArray(data) ? data : (data.data || []);

    if (items && items.length > 0) {
      chartData.value = {
        labels: items.map(d => d.thoiGian),
        datasets: [
          {
            label: 'Doanh thu (VNĐ)',
            backgroundColor: ['#0d6efd', '#adb5bd'], // Màu xanh (mới), Màu xám (cũ)
            borderRadius: 6,
            data: items.map(d => d.doanhThu)
          }
        ]
      };
    }
  } catch (error) {
    console.error("Lỗi tải biểu đồ:", error);
  } finally {
    loadingChart.value = false;
  }
};

// 2. Load các bảng dữ liệu (Table)
const fetchTabularData = async () => {
  loadingTable.value = true;
  try {
    // Gọi song song 3 API
    const [resSell, resSlow, resCust] = await Promise.all([
      thongKeApi.getTopSelling(filter.from, filter.to),
      thongKeApi.getSlowMoving(filter.from, filter.to),
      thongKeApi.getTopCustomers(filter.from, filter.to)
    ]);

    // Xử lý data an toàn
    topSelling.value = Array.isArray(resSell.data) ? resSell.data : (resSell.data?.data || []);
    slowMoving.value = Array.isArray(resSlow.data) ? resSlow.data : (resSlow.data?.data || []);
    topCustomers.value = Array.isArray(resCust.data) ? resCust.data : (resCust.data?.data || []);

    toast.success("Dữ liệu đã được cập nhật.");
  } catch (error) {
    console.error("Lỗi tải dữ liệu bảng:", error);
    toast.error("Có lỗi khi tải dữ liệu chi tiết.");
  } finally {
    loadingTable.value = false;
  }
};

const reloadData = () => {
  fetchQuarterStats();
  fetchTabularData();
};

// Utils
const formatCurrency = (value) => {
  if (!value) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

// --- LIFECYCLE ---
onMounted(() => {
  reloadData();
});
</script>

<style scoped>
.dashboard-page {
  background-color: #f5f7fb; /* Màu nền nhẹ dịu mắt */
  min-height: 100vh;
}

.card {
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 0.5rem 1rem rgba(0,0,0,0.08) !important;
}

.table th {
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 700;
  color: #6c757d;
  vertical-align: middle;
}

.table td {
  font-size: 0.9rem;
  vertical-align: middle;
}

/* Custom scrollbar cho table responsive */
.table-responsive::-webkit-scrollbar {
  height: 6px;
}
.table-responsive::-webkit-scrollbar-thumb {
  background-color: #dee2e6;
  border-radius: 4px;
}
</style>