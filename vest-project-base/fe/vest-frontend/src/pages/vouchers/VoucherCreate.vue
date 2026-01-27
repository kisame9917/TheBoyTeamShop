<template>
  <div class="container-fluid py-3">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center gap-2">
        <h5 class="mb-0">Thêm phiếu giảm giá</h5>
      </div>

      <button class="btn btn-outline-secondary btn-sm" @click="goBack">
        <i class="bi bi-arrow-left me-1"></i> Quay lại danh sách
      </button>
    </div>

    <div class="card shadow-sm">
      <div class="card-body">
        <div class="row g-3">
          <!-- LEFT -->
          <div class="col-12 col-lg-6">
            <label class="form-label">Tên phiếu giảm giá <span class="req">*</span></label>
            <input class="form-control text-start" v-model.trim="form.tenGiamGia" placeholder="Ví dụ: Sale 1/6" />

            <div class="mt-3">
              <label class="form-label">Ngày bắt đầu <span class="req">*</span></label>
              <div class="input-group date-group">
                <input ref="startPickerRef" type="text" class="form-control text-start" placeholder="dd/mm/yyyy" />
                <button class="btn btn-outline-secondary" type="button" @click="openStartPicker" title="Chọn ngày">
                  <i class="bi bi-calendar3"></i>
                </button>
                <button class="btn btn-outline-secondary" type="button" @click="clearStartDate" title="Xóa">
                  <i class="bi bi-x-lg"></i>
                </button>
              </div>
            </div>

            <!-- ✅ Loại giảm -->
            <div class="mt-3">
              <label class="form-label">Loại giảm </label>
              <select class="form-select" v-model="form.loaiGiam">
                <option :value="true">Giảm %</option>
                <option :value="false">Giảm tiền (VND)</option>
              </select>
            </div>

            <!-- ✅ Giảm tối đa NGAY DƯỚI Loại giảm -->
            <div class="mt-3" v-if="form.loaiGiam">
              <label class="form-label">Giảm tối đa (VND)<span class="req">*</span> </label>
              <div class="input-group">
                <input
                  type="text"
                  inputmode="numeric"
                  class="form-control text-start"
                  v-model="giaTriGiamToiDaVnd"
                />
                <span class="input-group-text">VND</span>
              </div>
            </div>
          </div>

          <!-- RIGHT -->
          <div class="col-12 col-lg-6">
            <label class="form-label">Số lượng <span class="req">*</span></label>

            <!-- Công khai: nhập tay -->
            <input
              v-if="form.loaiPhieu !== 'CA_NHAN'"
              type="number"
              class="form-control text-start"
              v-model.number="form.soLuong"
              min="1"
            />

            <!-- Cá nhân: tự động theo số khách hàng -->
            <input v-else type="number" class="form-control bg-light text-start" :value="selectedCustomerIds.length" readonly />

            <div class="mt-3">
              <label class="form-label">Ngày kết thúc <span class="req">*</span></label>
              <div class="input-group date-group">
                <input ref="endPickerRef" type="text" class="form-control text-start" placeholder="dd/mm/yyyy" />
                <button class="btn btn-outline-secondary" type="button" @click="openEndPicker" title="Chọn ngày">
                  <i class="bi bi-calendar3"></i>
                </button>
                <button class="btn btn-outline-secondary" type="button" @click="clearEndDate" title="Xóa">
                  <i class="bi bi-x-lg"></i>
                </button>
              </div>
            </div>

            <!-- ✅ Giá trị giảm -->
            <div class="mt-3">
              <label class="form-label">Giá trị giảm <span class="req">*</span></label>
              <div class="input-group">
                <!-- % -->
                <input
                  v-if="form.loaiGiam"
                  type="number"
                  class="form-control text-start"
                  v-model.number="form.giaTriGiam"
                  min="1"
                  max="100"
                  placeholder="Ví dụ: 20"
                />
                <!-- tiền -->
                <input
                  v-else
                  type="text"
                  inputmode="numeric"
                  class="form-control text-start"
                  v-model="giaTriGiamVnd"
                  placeholder="Ví dụ: 50.000"
                />
                <span class="input-group-text">{{ form.loaiGiam ? "%" : "VND" }}</span>
              </div>
            </div>

            <!-- ✅ Đơn hàng tối thiểu: có hậu tố VND giống giá trị giảm -->
            <div class="mt-3">
              <label class="form-label">Đơn hàng tối thiểu</label>
              <div class="input-group">
                <input
                  type="text"
                  inputmode="numeric"
                  class="form-control text-start"
                  v-model="donHangToiThieuVnd"
                  placeholder="Ví dụ: 200.000"
                />
                <span class="input-group-text">VND</span>
              </div>
            </div>
          </div>

          <!-- MÔ TẢ -->
          <div class="col-12">
            <label class="form-label">Mô tả</label>
            <textarea class="form-control text-start" rows="4" v-model.trim="form.moTa" placeholder="Nhập mô tả..."></textarea>
          </div>

          <!-- LOẠI PHIẾU -->
          <div class="col-12">
            <label class="form-label">Loại phiếu<span class="req">*</span></label>
            <div class="d-flex align-items-center gap-4 flex-wrap">
              <div class="form-check">
                <input class="form-check-input" type="radio" id="lp_public" value="CONG_KHAI" v-model="form.loaiPhieu" />
                <label class="form-check-label" for="lp_public">Công khai</label>
              </div>

              <div class="form-check">
                <input class="form-check-input" type="radio" id="lp_personal" value="CA_NHAN" v-model="form.loaiPhieu" />
                <label class="form-check-label" for="lp_personal">Cá nhân</label>
              </div>
            </div>
          </div>

          <!-- KHÁCH HÀNG (CÁ NHÂN) -->
          <div class="col-12" v-if="form.loaiPhieu === 'CA_NHAN'">
            <div class="card shadow-sm">
              <div class="card-body">
                <div class="d-flex align-items-end justify-content-between gap-2 flex-wrap">
                  <div>
                    <div class="fw-semibold">Danh sách khách hàng nhận phiếu</div>
                    <div class="text-muted small">
                      Đã chọn: <b>{{ selectedCustomerIds.length }}</b> khách hàng
                    </div>
                  </div>

                  <div class="d-flex align-items-center gap-2 flex-wrap">
                    <input
                      class="form-control form-control-sm text-start"
                      style="width: 320px"
                      v-model.trim="customerKeyword"
                      placeholder="Tìm theo mã / tên / SĐT / email..."
                    />

                    <button class="btn btn-outline-secondary btn-sm" type="button" @click="toggleSelectAll" :disabled="loadingCustomers">
                      {{ isAllSelected ? "Bỏ chọn tất cả" : "Chọn tất cả" }}
                    </button>
                  </div>
                </div>

                <div v-if="loadingCustomers" class="text-muted mt-3">Đang tải danh sách khách hàng...</div>
                <div v-else-if="customersError" class="text-danger mt-3">{{ customersError }}</div>

                <div v-else class="kh-box mt-3">
                  <div class="kh-head">
                    <div class="kh-col kh-check"></div>
                    <div class="kh-col kh-ma">Mã KH</div>
                    <div class="kh-col kh-ten">Tên KH</div>
                    <div class="kh-col kh-sdt">SĐT</div>
                    <div class="kh-col kh-email">Email</div>
                  </div>

                  <div class="kh-body">
                    <div v-if="filteredCustomers.length === 0" class="kh-empty">Không có khách hàng phù hợp.</div>

                    <div v-else class="kh-row" v-for="c in filteredCustomers" :key="c.id">
                      <div class="kh-col kh-check">
                        <input
                          type="checkbox"
                          class="form-check-input"
                          :checked="selectedCustomerIds.includes(c.id)"
                          @change="toggleCustomer(c.id)"
                        />
                      </div>

                      <div class="kh-col kh-ma">{{ c.maKhachHang ?? c.ma ?? "-" }}</div>
                      <div class="kh-col kh-ten fw-semibold">{{ c.tenKhachHang ?? c.ten ?? "-" }}</div>
                      <div class="kh-col kh-sdt">{{ c.soDienThoai ?? c.sdt ?? "-" }}</div>
                      <div class="kh-col kh-email">
                        <span class="kh-ellipsis" :title="c.email ?? ''">{{ c.email ?? "-" }}</span>
                      </div>
                    </div>
                  </div>
                </div>
                <!-- /kh-box -->
              </div>
            </div>
          </div>

          <!-- ERROR -->
          <div class="col-12" v-if="error">
            <div class="alert alert-danger mb-0">{{ error }}</div>
          </div>

          <!-- ACTIONS -->
          <div class="col-12 d-flex justify-content-end gap-2">
            <button class="btn btn-outline-secondary" @click="goBack" :disabled="saving">Hủy</button>

            <button class="btn btn-confirm" @click="submit" :disabled="saving">
              {{ saving ? "Đang lưu..." : "Lưu phiếu giảm giá" }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ✅ POPUP (Confirm) -->
    <teleport to="body">
      <div v-if="popup.open" class="modal-overlay" @click.self="closePopup">
        <div class="modal-card">
          <h6 class="mb-2">{{ popup.title }}</h6>
          <div class="text-muted mb-3">{{ popup.message }}</div>

          <div class="d-flex justify-content-end gap-2">
            <button v-if="popup.mode === 'alert'" class="btn btn-outline-secondary btn-sm" type="button" @click="closePopup">
              Đóng
            </button>

            <template v-else>
              <button class="btn btn-outline-secondary btn-sm" type="button" :disabled="popup.loading" @click="closePopup">
                Hủy
              </button>
              <button class="btn btn-confirm btn-sm" type="button" :disabled="popup.loading" @click="confirmPopup">
                {{ popup.loading ? "Đang xử lý..." : "Đồng ý" }}
              </button>
            </template>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup>
import { ref, watch, computed, onMounted, onBeforeUnmount } from "vue";
import axios from "axios";
import { useRouter } from "vue-router";
import { useToast } from "@/composables/useToast";

/** ✅ flatpickr */
import flatpickr from "flatpickr";
import { Vietnamese } from "flatpickr/dist/l10n/vn.js";
import "flatpickr/dist/flatpickr.css";

const toast = useToast();
const router = useRouter();

const API = "http://localhost:8080/api/pgg";
const KH_API = "http://localhost:8080/api/khach-hang";

const saving = ref(false);
const error = ref("");

// ===== Confirm popup =====
const popup = ref({
  open: false,
  mode: "alert",
  title: "Thông báo",
  message: "",
  loading: false,
});
let popupAction = null;

function openConfirm(message, action, title = "Xác nhận") {
  popup.value.open = true;
  popup.value.mode = "confirm";
  popup.value.title = title;
  popup.value.message = message || "";
  popup.value.loading = false;
  popupAction = typeof action === "function" ? action : null;
}

function closePopup() {
  if (popup.value.loading) return;
  popup.value.open = false;
  popupAction = null;
}

async function confirmPopup() {
  if (!popupAction) {
    closePopup();
    return;
  }
  popup.value.loading = true;
  try {
    await popupAction();
  } finally {
    popup.value.loading = false;
    popup.value.open = false;
    popupAction = null;
  }
}

// ===== Form =====
const form = ref({
  tenGiamGia: "",
  soLuong: 1,
  loaiGiam: true, // true = %, false = tiền
  giaTriGiam: 1,
  giaTriGiamToiDa: 0, // chỉ dùng khi %
  donHangToiThieu: 0,
  ngayBatDau: "", // yyyy-MM-dd
  ngayKetThuc: "", // yyyy-MM-dd
  moTa: "",
  loaiPhieu: "CONG_KHAI",
});

// đổi sang giảm tiền thì reset giảm tối đa
watch(
  () => form.value.loaiGiam,
  (isPercent) => {
    if (!isPercent) form.value.giaTriGiamToiDa = 0;
  }
);

function goBack() {
  router.push("/vouchers");
}

/* =========================
   ✅ Format tiền ngay khi nhập (vi-VN)
   ========================= */
const nf = new Intl.NumberFormat("vi-VN");

function formatVndInput(n, emptyIfZero = false) {
  const num = Number(n);
  if (!Number.isFinite(num)) return "";
  if (emptyIfZero && num === 0) return "";
  return nf.format(num);
}
function parseVndInput(s) {
  const digits = String(s ?? "").replace(/[^\d]/g, "");
  if (!digits) return 0;
  return Number(digits);
}

// v-model string cho ô "Giá trị giảm" khi là TIỀN
const giaTriGiamVnd = computed({
  get() {
    return formatVndInput(form.value.giaTriGiam, true);
  },
  set(v) {
    form.value.giaTriGiam = parseVndInput(v);
  },
});

// v-model string cho "Giảm tối đa"
const giaTriGiamToiDaVnd = computed({
  get() {
    return formatVndInput(form.value.giaTriGiamToiDa, true);
  },
  set(v) {
    form.value.giaTriGiamToiDa = parseVndInput(v);
  },
});

// v-model string cho "Đơn hàng tối thiểu"
const donHangToiThieuVnd = computed({
  get() {
    // 0 là hợp lệ => vẫn cho hiện 0
    return formatVndInput(form.value.donHangToiThieu, false);
  },
  set(v) {
    form.value.donHangToiThieu = parseVndInput(v);
  },
});

// ===== Date helpers =====
function toStartOfDay(dateYMD) {
  if (!dateYMD) return null;
  return `${dateYMD}T00:00:00`;
}
function toEndOfDay(dateYMD) {
  if (!dateYMD) return null;
  return `${dateYMD}T23:59:59`;
}

// ===== Flatpickr =====
const startPickerRef = ref(null);
const endPickerRef = ref(null);
let fpStart = null;
let fpEnd = null;

function parseYMD(ymd) {
  if (!ymd) return null;
  const [y, m, d] = String(ymd).split("-").map(Number);
  if (!y || !m || !d) return null;
  return new Date(y, m - 1, d);
}

function initPickers() {
  if (startPickerRef.value && !fpStart) {
    fpStart = flatpickr(startPickerRef.value, {
      locale: Vietnamese,
      dateFormat: "d/m/Y",
      allowInput: true,
      defaultDate: parseYMD(form.value.ngayBatDau),
      onChange: (selectedDates) => {
        const d = selectedDates?.[0] || null;
        form.value.ngayBatDau = d ? flatpickr.formatDate(d, "Y-m-d") : "";
        if (fpEnd) fpEnd.set("minDate", d || null);
      },
    });
  }

  if (endPickerRef.value && !fpEnd) {
    fpEnd = flatpickr(endPickerRef.value, {
      locale: Vietnamese,
      dateFormat: "d/m/Y",
      allowInput: true,
      defaultDate: parseYMD(form.value.ngayKetThuc),
      onChange: (selectedDates) => {
        const d = selectedDates?.[0] || null;
        form.value.ngayKetThuc = d ? flatpickr.formatDate(d, "Y-m-d") : "";
        if (fpStart) fpStart.set("maxDate", d || null);
      },
    });
  }

  if (fpEnd) fpEnd.set("minDate", form.value.ngayBatDau ? parseYMD(form.value.ngayBatDau) : null);
  if (fpStart) fpStart.set("maxDate", form.value.ngayKetThuc ? parseYMD(form.value.ngayKetThuc) : null);
}

function openStartPicker() {
  fpStart?.open();
}
function openEndPicker() {
  fpEnd?.open();
}

function clearStartDate() {
  form.value.ngayBatDau = "";
  fpStart?.clear();
  if (fpEnd) fpEnd.set("minDate", null);
}
function clearEndDate() {
  form.value.ngayKetThuc = "";
  fpEnd?.clear();
  if (fpStart) fpStart.set("maxDate", null);
}

// ===== KH state =====
const customers = ref([]);
const loadingCustomers = ref(false);
const customersError = ref("");
const customerKeyword = ref("");
const selectedCustomerIds = ref([]);

async function loadCustomers() {
  loadingCustomers.value = true;
  customersError.value = "";
  try {
    const res = await axios.get(KH_API);
    customers.value = Array.isArray(res.data) ? res.data : [];
  } catch (e) {
    customersError.value = e?.response?.data?.message || e?.message || "Không tải được khách hàng";
  } finally {
    loadingCustomers.value = false;
  }
}

const filteredCustomers = computed(() => {
  const kw = String(customerKeyword.value || "").trim().toLowerCase();
  if (!kw) return customers.value;
  return customers.value.filter((c) => {
    const s = `${c.maKhachHang ?? c.ma ?? ""} ${c.tenKhachHang ?? c.ten ?? ""} ${c.soDienThoai ?? c.sdt ?? ""} ${c.email ?? ""}`.toLowerCase();
    return s.includes(kw);
  });
});

const isAllSelected = computed(() => {
  const ids = filteredCustomers.value.map((x) => x.id);
  if (ids.length === 0) return false;
  return ids.every((id) => selectedCustomerIds.value.includes(id));
});

function toggleCustomer(id) {
  const arr = [...selectedCustomerIds.value];
  const idx = arr.indexOf(id);
  if (idx >= 0) arr.splice(idx, 1);
  else arr.push(id);
  selectedCustomerIds.value = arr;
}

function toggleSelectAll() {
  const ids = filteredCustomers.value.map((x) => x.id);
  if (ids.length === 0) return;

  if (isAllSelected.value) {
    selectedCustomerIds.value = selectedCustomerIds.value.filter((id) => !ids.includes(id));
  } else {
    const set = new Set(selectedCustomerIds.value);
    ids.forEach((id) => set.add(id));
    selectedCustomerIds.value = Array.from(set);
  }
}

const personalQty = computed(() => selectedCustomerIds.value.length);
const manualSoLuongBackup = ref(form.value.soLuong);

watch(
  () => form.value.loaiPhieu,
  async (v) => {
    if (v === "CA_NHAN") {
      if (customers.value.length === 0) await loadCustomers();
    } else {
      selectedCustomerIds.value = [];
      customerKeyword.value = "";
    }
  }
);

watch(
  () => form.value.loaiPhieu,
  (v) => {
    if (v === "CA_NHAN") {
      manualSoLuongBackup.value = form.value.soLuong;
      form.value.soLuong = personalQty.value;
    } else {
      form.value.soLuong = manualSoLuongBackup.value || 1;
    }
  }
);

watch(personalQty, (n) => {
  if (form.value.loaiPhieu === "CA_NHAN") {
    form.value.soLuong = n;
  }
});

// ===== Validate + Submit =====
function validate() {
  const isBlank = (v) => !String(v ?? "").trim();
  const isNum = (v) => typeof v === "number" && !Number.isNaN(v) && Number.isFinite(v);

  if (isBlank(form.value.tenGiamGia)) return "Vui lòng nhập tên phiếu giảm giá";

  if (form.value.loaiPhieu === "CA_NHAN") {
    form.value.soLuong = selectedCustomerIds.value.length;
    if (selectedCustomerIds.value.length === 0) return "Phiếu cá nhân phải chọn ít nhất 1 khách hàng";
  } else {
    if (!isNum(form.value.soLuong) || form.value.soLuong < 1) return "Số lượng phải >= 1";
  }

  if (!isNum(form.value.giaTriGiam) || form.value.giaTriGiam < 1) return "Giá trị giảm phải >= 1";

  if (form.value.loaiGiam) {
    if (form.value.giaTriGiam > 100) return "Giảm % tối đa 100";
    if (!isNum(form.value.giaTriGiamToiDa) || form.value.giaTriGiamToiDa <= 0) return "Giảm tối đa phải > 0";
  }

  if (!isNum(form.value.donHangToiThieu) || form.value.donHangToiThieu < 0) return "Đơn hàng tối thiểu phải >= 0";

  if (isBlank(form.value.ngayBatDau)) return "Vui lòng chọn ngày bắt đầu";
  if (isBlank(form.value.ngayKetThuc)) return "Vui lòng chọn ngày kết thúc";
  if (form.value.ngayKetThuc < form.value.ngayBatDau) return "Ngày kết thúc phải >= ngày bắt đầu";

  if (!form.value.loaiGiam) {
    if (form.value.donHangToiThieu > 0 && form.value.giaTriGiam > form.value.donHangToiThieu) {
      return "Tiền giảm không được vượt đơn hàng tối thiểu";
    }
  }

  if (!isBlank(form.value.moTa) && String(form.value.moTa).length > 500) return "Mô tả tối đa 500 ký tự";

  return "";
}

async function doCreate() {
  const payload = {
    tenGiamGia: form.value.tenGiamGia,
    soLuong: form.value.loaiPhieu === "CA_NHAN" ? selectedCustomerIds.value.length : form.value.soLuong,
    loaiGiam: form.value.loaiGiam,
    ngayBatDau: toStartOfDay(form.value.ngayBatDau),
    ngayKetThuc: toEndOfDay(form.value.ngayKetThuc),
    moTa: form.value.moTa,
    donHangToiThieu: form.value.donHangToiThieu,
    loaiPhieu: form.value.loaiPhieu === "CA_NHAN",
  };

  if (payload.loaiGiam) {
    payload.giaTriPhanTram = form.value.giaTriGiam;
    payload.giaTriGiamToiDa = form.value.giaTriGiamToiDa;
    payload.giaTriTienMat = null;
  } else {
    payload.giaTriTienMat = form.value.giaTriGiam;
    payload.giaTriPhanTram = null;
    payload.giaTriGiamToiDa = null;
  }

  if (payload.loaiPhieu === true) {
    payload.khachHangIds = [...selectedCustomerIds.value];
  }

  await axios.post(`${API}/create`, payload);
}

async function submit() {
  error.value = "";
  const msg = validate();
  if (msg) {
    toast.warning(msg);
    return;
  }

  openConfirm("Bạn có chắc muốn tạo phiếu giảm giá này không?", async () => {
    saving.value = true;
    try {
      await doCreate();
      toast.success("Thêm phiếu giảm giá thành công");
      goBack();
    } catch (e) {
      const m = e?.response?.data?.message || e?.message || "Tạo thất bại";
      error.value = m;
      toast.error(m);
      throw e;
    } finally {
      saving.value = false;
    }
  });
}

onMounted(() => {
  initPickers();
});

onBeforeUnmount(() => {
  try {
    fpStart?.destroy();
  } catch {}
  try {
    fpEnd?.destroy();
  } catch {}
});
</script>

<style scoped>
/* date-group giống update */
.date-group .form-control {
  height: 38px !important;
  border-radius: 8px 0 0 8px !important;
}
.date-group .btn {
  height: 38px !important;
  width: 44px;
  padding: 0 !important;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.date-group .btn:not(:last-child) {
  border-radius: 0 !important;
}
.date-group .btn:last-child {
  border-radius: 0 8px 8px 0 !important;
}

/* nút confirm */
.btn-confirm {
  background: #1d4ed8;
  border-color: #1d4ed8;
  color: #fff;
  font-weight: 700;
}
.btn-confirm:hover {
  filter: brightness(0.95);
}
.btn-confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* KH list */
.kh-box {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
}
.kh-head {
  display: grid;
  grid-template-columns: 44px 140px 1.6fr 160px 2fr;
  gap: 0;
  padding: 10px 12px;
  background: #1f2a44;
  font-weight: 700;
  font-size: 13px;
  color: #e5e7eb;
  border-bottom: 1px solid #e5e7eb;
}
.kh-body {
  max-height: 360px;
  overflow: auto;
}
.kh-row {
  display: grid;
  grid-template-columns: 44px 140px 1.6fr 160px 2fr;
  padding: 10px 12px;
  font-size: 13px;
  border-bottom: 1px solid #e5e7eb;
  align-items: center;
}
.kh-row:hover {
  background: #f8fafc;
}
.kh-col {
  min-width: 0;
}
.kh-ellipsis {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.kh-empty {
  padding: 16px 12px;
  text-align: center;
  color: #6b7280;
  font-weight: 600;
}

/* modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  z-index: 999999;
}
.modal-card {
  width: min(520px, 96vw);
  background: #fff;
  border-radius: 14px;
  padding: 16px 18px;
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.25);
}
.req{
  color: #dc2626;          /* đỏ */
  font-weight: 600;
  margin-left: 4px;
   font-size: 14px; 
}

</style>
