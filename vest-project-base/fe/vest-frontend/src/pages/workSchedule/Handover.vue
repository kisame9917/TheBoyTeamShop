<template>
  <div class="container-fluid py-3">
    <div class="d-flex align-items-center justify-content-between mb-3">
      <h4 class="m-0 fw-bold">Giao ca &amp; Kết toán</h4>
      <div class="small text-muted">{{ nowText }}</div>
    </div>

    <!-- STAFF view -->
    <template v-if="!isAdmin">
      <div v-if="loading" class="text-center text-muted py-5">Đang tải...</div>

      <div v-else-if="!phien" class="card shadow-sm">
        <div class="card-body text-center py-5">
          <div class="fs-1">🧾</div>
          <div class="fw-semibold mb-2">Bạn chưa mở ca</div>
          <div class="text-muted mb-3">Vui lòng vào trang Bán hàng để xác nhận vào ca trước.</div>
          <button class="btn btn-primary" type="button" @click="goSales">Đi tới Bán hàng</button>
        </div>
      </div>

      <div v-else>
        <div class="card shadow-sm mb-3">
          <div class="card-body d-flex align-items-center justify-content-between flex-wrap gap-2">
            <div>
              <div class="fw-semibold">Phiếu Bàn Giao Ca</div>
              <div class="small text-muted">#{{ phien.id || '-' }} • {{ fmtDateTime(phien.thoiGianMo) }}</div>
            </div>
            <div class="d-flex align-items-center gap-2">
              <div class="text-end">
                <div class="small text-muted">Nhân viên trực</div>
                <div class="fw-semibold">{{ phien.tenNhanVien || userName }}</div>
              </div>
              <div class="rounded-circle bg-success text-white d-flex align-items-center justify-content-center" style="width:36px;height:36px">
                {{ (phien.tenNhanVien || userName || 'N')[0] }}
              </div>
            </div>
          </div>
        </div>

        <div class="row g-3">
          <div class="col-12 col-lg-7">
            <div class="card shadow-sm h-100">
              <div class="card-body">
                <div class="fw-semibold mb-3">Tài chính trong ca</div>

                <div class="d-flex justify-content-between py-2 border-bottom">
                  <div class="text-muted">Tiền mặt đầu ca</div>
                  <div class="fw-semibold">{{ money(phien.tienMatDauCa) }}</div>
                </div>
                <div class="d-flex justify-content-between py-2 border-bottom">
                  <div class="text-muted">Doanh thu Tiền mặt</div>
                  <div class="fw-semibold text-success">+{{ money(phien.doanhThuTienMat) }}</div>
                </div>
                <div class="d-flex justify-content-between py-2">
                  <div class="text-muted">Doanh thu CK / Thẻ</div>
                  <div class="fw-semibold">{{ money(phien.doanhThuChuyenKhoan) }}</div>
                </div>

                <div class="mt-3 p-3 rounded-3" style="background:#e9fbf3;border:1px solid #bfead8">
                  <div class="text-center fw-semibold" style="color:#0f9d7a">TỔNG TIỀN MẶT LÝ THUYẾT TẠI KẾT</div>
                  <div class="text-center fw-bold" style="font-size:28px">{{ money(tongLyThuyet) }}</div>
                  <div class="text-center small text-muted">(Đầu ca + Doanh thu Tiền mặt - Chi phí)</div>
                </div>
              </div>
            </div>
          </div>

          <div class="col-12 col-lg-5">
            <div class="card shadow-sm h-100">
              <div class="card-body">
                <div class="fw-semibold mb-3">Kiểm kê &amp; Xác nhận</div>

                <label class="form-label fw-semibold">NHẬP TIỀN THỰC TẾ <span class="text-danger">*</span></label>
                <div class="input-group mb-3">
                  <span class="input-group-text">đ</span>
                  <input v-model.number="tienThucTe" type="number" min="0" class="form-control" placeholder="0" />
                </div>

                <div class="p-2 rounded-3 mb-3" style="background:#e9fbf3;border:1px solid #bfead8">
                  <div class="d-flex align-items-center justify-content-between">
                    <div class="fw-semibold" style="color:#0f9d7a">Khớp / Dư / Thiếu</div>
                    <div class="fw-bold">{{ money(chenhLech) }}</div>
                  </div>
                </div>

                <label class="form-label fw-semibold">GHI CHÚ</label>
                <textarea v-model="ghiChu" class="form-control" rows="4" placeholder="..."></textarea>

                <button class="btn btn-success w-100 mt-3" type="button" :disabled="submitting" @click="dongCa">
                  {{ submitting ? 'Đang xử lý...' : 'XÁC NHẬN ĐÓNG CA' }}
                </button>
                <div class="text-center small text-muted mt-2">Hệ thống sẽ tự động đăng xuất sau khi hoàn tất.</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- ADMIN view -->
    <template v-else>
      <div class="card shadow-sm mb-3">
        <div class="card-body">
          <div class="row g-2">
            <div class="col-12 col-md-4">
              <input v-model.trim="q" class="form-control" placeholder="Tìm theo nhân viên / mã ca..." />
            </div>
            <div class="col-6 col-md-3">
              <input v-model="fromDate" type="date" class="form-control" />
            </div>
            <div class="col-6 col-md-3">
              <input v-model="toDate" type="date" class="form-control" />
            </div>
            <div class="col-12 col-md-2 d-grid">
              <button class="btn btn-outline-primary" type="button" @click="loadAdmin">Lọc</button>
            </div>
          </div>
        </div>
      </div>

      <div class="card shadow-sm">
        <div class="card-body">
          <div v-if="loading" class="text-center text-muted py-5">Đang tải...</div>
          <div v-else class="table-responsive">
            <table class="table align-middle">
              <thead class="table-light">
                <tr>
                  <th>#</th>
                  <th>Nhân viên</th>
                  <th>Ca</th>
                  <th>Mở</th>
                  <th>Đóng</th>
                  <th class="text-end">DT Tiền mặt</th>
                  <th class="text-end">DT CK/Thẻ</th>
                  <th class="text-end">Tổng DT</th>
                  <th class="text-end">Chênh</th>
                  <th>Trạng thái</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="p in adminItems" :key="p.id">
                  <td class="fw-semibold">{{ p.id }}</td>
                  <td>{{ p.tenNhanVien || '-' }}</td>
                  <td>{{ p.tenCa || '-' }}</td>
                  <td>{{ fmtDateTime(p.thoiGianMo) }}</td>
                  <td>{{ fmtDateTime(p.thoiGianDong) }}</td>
                  <td class="text-end">{{ money(p.doanhThuTienMat) }}</td>
                  <td class="text-end">{{ money(p.doanhThuChuyenKhoan) }}</td>
                  <td class="text-end fw-semibold">{{ money(p.tongDoanhThu) }}</td>
                  <td class="text-end">{{ money(p.chenhLech) }}</td>
                  <td>
                    <span class="badge" :class="p.thoiGianDong ? 'bg-success' : 'bg-warning text-dark'">
                      {{ p.thoiGianDong ? 'Đã đóng' : 'Đang mở' }}
                    </span>
                  </td>
                </tr>
                <tr v-if="adminItems.length === 0">
                  <td colspan="10" class="text-center text-muted py-3">Không có dữ liệu</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </template>

    <div v-if="toast.show" class="toast-float" :class="toast.type">
      {{ toast.msg }}
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import { hienTai, dongCa as apiDongCa, adminPhienCa } from "@/services/giaoCaApi";

const router = useRouter();
const auth = useAuthStore();
const isAdmin = computed(() => auth.isAdmin);
const userName = computed(() => auth.user?.tenNhanVien || auth.user?.taiKhoan || "");

const loading = ref(false);
const submitting = ref(false);
const phien = ref(null);

const tienThucTe = ref(0);
const ghiChu = ref("");

// admin list
const adminItems = ref([]);
const q = ref("");
const fromDate = ref("");
const toDate = ref("");

const toast = reactive({ show: false, msg: "", type: "info" });
function toastShow(msg, type = "info") {
  toast.show = true;
  toast.msg = msg;
  toast.type = type;
  clearTimeout(toastShow._t);
  toastShow._t = setTimeout(() => (toast.show = false), 2200);
}

const nowText = computed(() => {
  const d = new Date();
  return d.toLocaleString("vi-VN", { weekday: "long", year: "numeric", month: "2-digit", day: "2-digit", hour: "2-digit", minute: "2-digit" });
});

function money(v) {
  const n = Number(v || 0);
  return n.toLocaleString("vi-VN") + " đ";
}

function fmtDateTime(v) {
  if (!v) return "-";
  try {
    const d = new Date(v);
    if (Number.isNaN(d.getTime())) return String(v);
    return d.toLocaleString("vi-VN");
  } catch {
    return String(v);
  }
}

const tongLyThuyet = computed(() => {
  const p = phien.value;
  if (!p) return 0;
  // Option 2: nếu backend chưa tính doanh thu ở hien-tai => sẽ hiển thị 0, chỉ cập nhật sau đóng ca.
  return Number(p.tienMatDauCa || 0) + Number(p.doanhThuTienMat || 0) - Number(p.chiPhi || 0);
});

const chenhLech = computed(() => Number(tienThucTe.value || 0) - Number(tongLyThuyet.value || 0));

function goSales() {
  router.push({ name: "sales" });
}

async function loadStaff() {
  loading.value = true;
  try {
    const payload = await hienTai();
    const data = payload?.data ?? payload;
    phien.value = data || null;
    if (phien.value?.tienMatDauCa != null) {
      // default
      tienThucTe.value = Number(tongLyThuyet.value || 0);
    }
  } catch (e) {
    toastShow(e?.message || "Không tải được phiên ca", "danger");
    phien.value = null;
  } finally {
    loading.value = false;
  }
}

async function dongCa() {
  const cash = Number(tienThucTe.value);
  if (!Number.isFinite(cash) || cash < 0) return toastShow("Tiền thực tế không hợp lệ", "warning");

  submitting.value = true;
  try {
    await apiDongCa({ tienMatThucTe: cash, ghiChu: ghiChu.value || null });
    toastShow("Đã đóng ca", "success");
    // auto logout theo yêu cầu
    auth.logout();
    router.replace({ name: "login" });
  } catch (e) {
    toastShow(e?.message || "Đóng ca thất bại", "danger");
  } finally {
    submitting.value = false;
  }
}

async function loadAdmin() {
  loading.value = true;
  try {
    const payload = await adminPhienCa({ keyword: q.value || undefined, fromDate: fromDate.value || undefined, toDate: toDate.value || undefined, page: 0, size: 20 });
    const data = payload?.data ?? payload;
    // BE có thể trả Page hoặc list; xử lý linh hoạt
    adminItems.value = Array.isArray(data) ? data : (data?.content || []);
  } catch (e) {
    toastShow(e?.message || "Không tải được danh sách phiên ca", "danger");
    adminItems.value = [];
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  if (isAdmin.value) loadAdmin();
  else loadStaff();
});
</script>

<style scoped>
.toast-float{
  position: fixed;
  right: 16px;
  bottom: 16px;
  padding: 10px 12px;
  border-radius: 10px;
  color: #fff;
  z-index: 3000;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
}
.toast-float.info{ background:#0d6efd; }
.toast-float.success{ background:#198754; }
.toast-float.warning{ background:#ffc107; color:#212529; }
.toast-float.danger{ background:#dc3545; }
</style>
