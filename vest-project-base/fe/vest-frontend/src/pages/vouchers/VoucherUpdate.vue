<template>
  <div class="container-fluid py-3">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center gap-2">
        <h5 class="mb-0">Cập nhật phiếu giảm giá</h5>
      </div>

      <button class="btn btn-outline-secondary btn-sm" @click="goBack">
        <i class="bi bi-arrow-left me-1"></i> Quay lại danh sách
      </button>
    </div>

    <div class="card shadow-sm">
      <div class="card-body">
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border" role="status"></div>
          <div class="mt-2 text-muted">Đang tải dữ liệu...</div>
        </div>

        <div v-else>
          <!-- FORM -->
          <div class="row g-3">
            <!-- LEFT -->
            <div class="col-12 col-lg-6">
              <label class="form-label">Loại giảm</label>
              <select class="form-select" v-model="form.loaiGiam" :disabled="isEnded">
                <option :value="true">Giảm %</option>
                <option :value="false">Giảm tiền (VND)</option>
              </select>

              <div class="mt-3">
                <label class="form-label">Giá trị giảm</label>
                <div class="input-group">
                  <input
                    type="number"
                    class="form-control"
                    v-model.number="form.giaTriGiam"
                    :min="1"
                    :max="form.loaiGiam ? 100 : null"
                    :placeholder="form.loaiGiam ? 'Ví dụ: 20' : 'Ví dụ: 50000'"
                    :disabled="isEnded"
                  />
                  <span class="input-group-text">
                    {{ form.loaiGiam ? "%" : "VND" }}
                  </span>
                </div>
              </div>

              <div class="mt-3" v-if="form.loaiGiam">
                <label class="form-label">Giảm tối đa (VND)</label>
                <input
                  type="number"
                  class="form-control"
                  v-model.number="form.giaTriGiamToiDa"
                  min="1"
                  placeholder="Ví dụ: 2000000"
                  :disabled="isEnded"
                />
              </div>

              <!-- Ngày bắt đầu -->
              <div class="mt-3">
                <label class="form-label">Ngày bắt đầu</label>
                <div class="input-group date-group">
                  <input
                    ref="fromInput"
                    type="text"
                    class="form-control"
                    placeholder="dd/mm/yyyy"
                    readonly
                    :disabled="isEnded"
                  />
                  <button
                    class="btn btn-outline-secondary"
                    type="button"
                    @click="openFrom"
                    :disabled="isEnded"
                    title="Chọn ngày"
                  >
                    <i class="bi bi-calendar3"></i>
                  </button>
                  <button
                    class="btn btn-outline-secondary"
                    type="button"
                    @click="clearFrom"
                    :disabled="isEnded"
                    title="Xóa"
                  >
                    <i class="bi bi-x-lg"></i>
                  </button>
                </div>
              </div>
            </div>

            <!-- RIGHT -->
            <div class="col-12 col-lg-6">
              <label class="form-label">Tên phiếu giảm giá</label>
              <input
                class="form-control"
                v-model.trim="form.tenGiamGia"
                placeholder="Ví dụ: Sale 1/6"
                :disabled="isEnded"
              />

              <div class="mt-3">
                <label class="form-label">Số lượng</label>
                <input
                  type="number"
                  class="form-control"
                  v-model.number="form.soLuong"
                  min="1"
                  :disabled="isEnded"
                />
              </div>

              <div class="mt-3">
                <label class="form-label">Đơn hàng tối thiểu (VND)</label>
                <input
                  type="number"
                  class="form-control"
                  v-model.number="form.donHangToiThieu"
                  min="0"
                  placeholder="Ví dụ: 200000"
                  :disabled="isEnded"
                />
              </div>

              <!-- Ngày kết thúc -->
              <div class="mt-3">
                <label class="form-label">Ngày kết thúc</label>
                <div class="input-group date-group">
                  <input
                    ref="toInput"
                    type="text"
                    class="form-control"
                    placeholder="dd/mm/yyyy"
                    readonly
                    :disabled="isEnded"
                  />
                  <button
                    class="btn btn-outline-secondary"
                    type="button"
                    @click="openTo"
                    :disabled="isEnded"
                    title="Chọn ngày"
                  >
                    <i class="bi bi-calendar3"></i>
                  </button>
                  <button
                    class="btn btn-outline-secondary"
                    type="button"
                    @click="clearTo"
                    :disabled="isEnded"
                    title="Xóa"
                  >
                    <i class="bi bi-x-lg"></i>
                  </button>
                </div>
              </div>
            </div>

            <!-- MÔ TẢ -->
            <div class="col-12">
              <label class="form-label">Mô tả</label>
              <textarea
                class="form-control"
                rows="4"
                v-model.trim="form.moTa"
                placeholder="Nhập mô tả..."
                :disabled="isEnded"
              ></textarea>
            </div>

            <!-- LOẠI PHIẾU -->
            <div class="col-12">
              <label class="form-label">Loại phiếu</label>
              <div class="d-flex align-items-center gap-4 flex-wrap">
                <div class="form-check">
                  <input
                    class="form-check-input"
                    type="radio"
                    id="lp_congkhai"
                    value="CONG_KHAI"
                    v-model="form.loaiPhieu"
                    :disabled="isEnded || loaiPhieuLocked"
                  />
                  <label class="form-check-label" for="lp_congkhai">Công khai</label>
                </div>

                <div class="form-check">
                  <input
                    class="form-check-input"
                    type="radio"
                    id="lp_canhan"
                    value="CA_NHAN"
                    v-model="form.loaiPhieu"
                    :disabled="isEnded || loaiPhieuLocked"
                  />
                  <label class="form-check-label" for="lp_canhan">Cá nhân</label>
                </div>
              </div>
            </div>

            <!-- KHÁCH HÀNG (CÁ NHÂN) -->
            <div class="col-12" v-if="isCaNhan">
              <div class="card shadow-sm">
                <div class="card-body">
                  <div class="d-flex align-items-end justify-content-between gap-2 flex-wrap">
                    <div>
                      <div class="fw-semibold">Danh sách khách hàng nhận phiếu</div>
                      <div class="text-muted small">
                        Đã chọn: <b>{{ selectedKhIds.size }}</b> khách hàng
                      </div>
                    </div>

                    <div class="d-flex align-items-center gap-2 flex-wrap">
                      <input
                        class="form-control form-control-sm"
                        style="width: 320px"
                        v-model.trim="khSearch"
                        placeholder="Tìm theo mã / tên / SĐT / email..."
                        :disabled="isEnded"
                      />

                      <button
                        class="btn btn-outline-secondary btn-sm"
                        type="button"
                        @click="toggleSelectAll"
                        :disabled="isEnded || khLoading"
                      >
                        {{ isAllSelected ? "Bỏ chọn tất cả" : "Chọn tất cả" }}
                      </button>
                    </div>
                  </div>

                  <div v-if="khLoading" class="text-muted mt-3">
                    Đang tải danh sách khách hàng...
                  </div>

                  <!-- ✅ THAY TABLE => GRID BOX (đỡ bị “xa”) -->
                  <div v-else class="kh-box mt-3">
                    <div class="kh-head">
                      <div class="kh-col kh-check"></div>
                      <div class="kh-col kh-ma">Mã KH</div>
                      <div class="kh-col kh-ten">Tên KH</div>
                      <div class="kh-col kh-sdt">SĐT</div>
                      <div class="kh-col kh-email">Email</div>
                    </div>

                    <div class="kh-body">
                      <div v-if="filteredKhachHang.length === 0" class="kh-empty">
                        Không có khách hàng phù hợp.
                      </div>

                      <div v-else class="kh-row" v-for="kh in filteredKhachHang" :key="kh.id">
                        <div class="kh-col kh-check">
                          <input
                            type="checkbox"
                            class="form-check-input"
                            :checked="selectedKhIds.has(kh.id)"
                            @change="onToggleKh(kh.id)"
                            :disabled="isEnded"
                          />
                        </div>

                        <div class="kh-col kh-ma">{{ kh.maKhachHang || "-" }}</div>
                        <div class="kh-col kh-ten fw-semibold">{{ kh.tenKhachHang || "-" }}</div>
                        <div class="kh-col kh-sdt">{{ kh.soDienThoai || "-" }}</div>
                        <div class="kh-col kh-email">
                          <span class="kh-ellipsis" :title="kh.email || ''">{{ kh.email || "-" }}</span>
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
              <button class="btn btn-outline-secondary" @click="goBack" :disabled="saving">
                Hủy
              </button>

              <button
                class="btn btn-confirm"
                @click="submit"
                :disabled="saving || isEnded"
                :title="isEnded ? 'Phiếu đã kết thúc, không thể chỉnh sửa' : ''"
              >
                {{ saving ? "Đang lưu..." : "Cập nhật phiếu giảm giá" }}
              </button>
            </div>

            <div class="col-12" v-if="isEnded">
              <div class="alert alert-warning mb-0">
                Phiếu đã <b>kết thúc</b> nên không thể chỉnh sửa.
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ✅ POPUP (Alert/Confirm) -->
    <div v-if="popup.open" class="modal-overlay" @click.self="closePopup">
      <div class="modal-card">
        <h6 class="mb-2">{{ popup.title }}</h6>
        <div class="text-muted mb-3">{{ popup.message }}</div>

        <div class="d-flex justify-content-end gap-2">
          <button
            v-if="popup.mode === 'alert'"
            class="btn btn-outline-secondary btn-sm"
            type="button"
            @click="closePopup"
          >
            Đóng
          </button>

          <template v-else>
            <button
              class="btn btn-outline-secondary btn-sm"
              type="button"
              :disabled="popup.loading"
              @click="closePopup"
            >
              Hủy
            </button>

            <button
              class="btn btn-confirm btn-sm"
              type="button"
              :disabled="popup.loading"
              @click="confirmPopup"
            >
              {{ popup.loading ? "Đang xử lý..." : "Đồng ý" }}
            </button>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed, onMounted, onBeforeUnmount, nextTick } from "vue";
import axios from "axios";
import { useRouter, useRoute } from "vue-router";
import { useToast } from "@/composables/useToast";

/** ✅ Flatpickr */
import flatpickr from "flatpickr";
import { Vietnamese } from "flatpickr/dist/l10n/vn.js";
import "flatpickr/dist/flatpickr.css";

const toast = useToast();
const router = useRouter();
const route = useRoute();

const API = "http://localhost:8080/api/pgg";
const API_KH = "http://localhost:8080/api/khach-hang";

const id = route.params.id;

const loading = ref(true);
const saving = ref(false);
const error = ref("");

// ===== Popup state (confirm) =====
const popup = ref({
  open: false,
  mode: "alert", // 'alert' | 'confirm'
  title: "Thông báo",
  message: "",
  loading: false,
});
let popupAction = null;

function openAlert(message, title = "Thông báo") {
  popup.value.open = true;
  popup.value.mode = "alert";
  popup.value.title = title;
  popup.value.message = message || "";
  popup.value.loading = false;
  popupAction = null;
}

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
const originalLoaiPhieu = ref("CONG_KHAI");
const loaiPhieuLocked = computed(() => true);

const form = ref({
  tenGiamGia: "",
  soLuong: 1,
  loaiGiam: true,
  giaTriGiam: 1,
  giaTriGiamToiDa: 0,
  donHangToiThieu: 0,
  ngayBatDau: "",
  ngayKetThuc: "",
  moTa: "",
  loaiPhieu: "CONG_KHAI",
});

const isCaNhan = computed(() => form.value.loaiPhieu === "CA_NHAN");

// ===== Detail raw to compute isEnded =====
const detailRaw = ref(null);

function toDate(v) {
  if (!v) return null;
  const dt = new Date(String(v));
  return Number.isNaN(dt.getTime()) ? null : dt;
}

const isEnded = computed(() => {
  const end = toDate(detailRaw.value?.ngayKetThuc);
  if (!end) return false;
  return new Date() > end;
});

watch(
  () => form.value.loaiGiam,
  (isPercent) => {
    if (!isPercent) form.value.giaTriGiamToiDa = 0;
  }
);

function goBack() {
  router.push({ path: "/vouchers", query: route.query });
}

function normalizeDate(d) {
  if (!d) return "";
  return String(d).slice(0, 10);
}

function startOfDay(dateYMD) {
  if (!dateYMD) return null;
  return `${dateYMD}T00:00:00`;
}
function endOfDay(dateYMD) {
  if (!dateYMD) return null;
  return `${dateYMD}T23:59:59`;
}

// ===== Flatpickr =====
const fromInput = ref(null);
const toInput = ref(null);
let fpFrom = null;
let fpTo = null;

function parseYMD(ymd) {
  if (!ymd) return null;
  const [y, m, d] = String(ymd).split("-").map(Number);
  if (!y || !m || !d) return null;
  return new Date(y, m - 1, d);
}

function initPickers() {
  if (fromInput.value && !fpFrom) {
    fpFrom = flatpickr(fromInput.value, {
      locale: Vietnamese,
      dateFormat: "d/m/Y",
      allowInput: false,
      defaultDate: parseYMD(form.value.ngayBatDau),
      onChange: (selectedDates) => {
        const d = selectedDates?.[0] || null;
        form.value.ngayBatDau = d ? flatpickr.formatDate(d, "Y-m-d") : "";
      },
    });
  }

  if (toInput.value && !fpTo) {
    fpTo = flatpickr(toInput.value, {
      locale: Vietnamese,
      dateFormat: "d/m/Y",
      allowInput: false,
      defaultDate: parseYMD(form.value.ngayKetThuc),
      onChange: (selectedDates) => {
        const d = selectedDates?.[0] || null;
        form.value.ngayKetThuc = d ? flatpickr.formatDate(d, "Y-m-d") : "";
      },
    });
  }
}

function openFrom() { fpFrom?.open(); }
function openTo() { fpTo?.open(); }
function clearFrom() { form.value.ngayBatDau = ""; fpFrom?.clear(); }
function clearTo() { form.value.ngayKetThuc = ""; fpTo?.clear(); }

async function fetchDetail() {
  loading.value = true;
  error.value = "";
  try {
    const res = await axios.get(`${API}/${id}`);
    const data = res.data || {};
    detailRaw.value = data;

    form.value.tenGiamGia = data.tenGiamGia ?? "";
    form.value.soLuong = data.soLuong ?? 1;
    form.value.loaiGiam = !!data.loaiGiam;

    form.value.ngayBatDau = normalizeDate(data.ngayBatDau);
    form.value.ngayKetThuc = normalizeDate(data.ngayKetThuc);

    form.value.moTa = data.moTa ?? "";
    form.value.donHangToiThieu = data.donHangToiThieu ?? 0;

    const isCaNhanBE = !!data.loaiPhieu;
    originalLoaiPhieu.value = isCaNhanBE ? "CA_NHAN" : "CONG_KHAI";
    form.value.loaiPhieu = originalLoaiPhieu.value;

    if (form.value.loaiGiam) {
      form.value.giaTriGiam = data.giaTriPhanTram ?? 1;
      form.value.giaTriGiamToiDa = data.giaTriGiamToiDa ?? 0;
    } else {
      form.value.giaTriGiam = data.giaTriTienMat ?? 1;
      form.value.giaTriGiamToiDa = 0;
    }
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || "Không tải được dữ liệu";
    openAlert(error.value, "Lỗi");
  } finally {
    loading.value = false;
  }
}

// ===== KHÁCH HÀNG =====
const khLoading = ref(false);
const khList = ref([]);
const selectedKhIds = ref(new Set());
const khSearch = ref("");

function normText(v) {
  return String(v ?? "").trim().toLowerCase();
}

const filteredKhachHang = computed(() => {
  const q = normText(khSearch.value);
  if (!q) return khList.value;

  return khList.value.filter((kh) => {
    const s = [kh.maKhachHang, kh.tenKhachHang, kh.soDienThoai, kh.email].map(normText).join(" ");
    return s.includes(q);
  });
});

const isAllSelected = computed(() => {
  const ids = filteredKhachHang.value.map((x) => x.id);
  if (ids.length === 0) return false;
  return ids.every((id) => selectedKhIds.value.has(id));
});

function onToggleKh(khId) {
  const s = new Set(selectedKhIds.value);
  if (s.has(khId)) s.delete(khId);
  else s.add(khId);
  selectedKhIds.value = s;
}

// ✅ y hệt update trước: select all chỉ trên list đang filter
function toggleSelectAll() {
  const s = new Set(selectedKhIds.value);
  const ids = filteredKhachHang.value.map((x) => x.id);
  const all = ids.length > 0 && ids.every((id) => s.has(id));
  if (all) ids.forEach((id) => s.delete(id));
  else ids.forEach((id) => s.add(id));
  selectedKhIds.value = s;
}

async function loadKhachHangForPersonal() {
  if (!isCaNhan.value) return;

  khLoading.value = true;
  try {
    const all = await axios.get(API_KH);
    const allKh = Array.isArray(all.data) ? all.data : [];

    const khNorm = allKh.map((x) => ({
      id: x.id,
      maKhachHang: x.maKhachHang ?? x.ma_khach_hang,
      tenKhachHang: x.tenKhachHang ?? x.ten_khach_hang,
      soDienThoai: x.soDienThoai ?? x.so_dien_thoai,
      email: x.email,
    }));

    const received = await axios.get(`${API}/${id}/khach-hang`);
    const ds = Array.isArray(received.data) ? received.data : [];

    const mapByKhId = new Map();
    ds.forEach((r) => {
      const khId = r.idKhachHang ?? r.id_khach_hang;
      if (!khId) return;
      mapByKhId.set(khId, {
        soDienThoai: r.sdt ?? r.soDienThoai ?? r.so_dien_thoai,
        email: r.email,
      });
    });

    const merged = khNorm.map((kh) => {
      const join = mapByKhId.get(kh.id);
      return {
        ...kh,
        soDienThoai: kh.soDienThoai || join?.soDienThoai || null,
        email: kh.email || join?.email || null,
      };
    });

    khList.value = merged;

    const selected = new Set();
    mapByKhId.forEach((_, khId) => selected.add(khId));
    selectedKhIds.value = selected;
  } finally {
    khLoading.value = false;
  }
}

watch(isCaNhan, async (v) => {
  if (v) await loadKhachHangForPersonal();
  else {
    khList.value = [];
    selectedKhIds.value = new Set();
  }
});

// ===== SUBMIT =====
function validate() {
  if (!String(form.value.tenGiamGia || "").trim()) return "Vui lòng nhập tên phiếu";
  if (form.value.soLuong < 1) return "Số lượng phải >= 1";
  if (form.value.giaTriGiam < 1) return "Giá trị giảm phải >= 1";

  if (form.value.donHangToiThieu < 0) return "Đơn hàng tối thiểu phải >= 0";

  if (!form.value.loaiGiam && form.value.donHangToiThieu > 0 && form.value.giaTriGiam > form.value.donHangToiThieu) {
    return "Tiền giảm không được vượt quá đơn hàng tối thiểu";
  }

  if (form.value.loaiGiam) {
    if (form.value.giaTriGiam > 100) return "Giảm % tối đa 100";
    if (form.value.giaTriGiamToiDa <= 0) return "Giảm tối đa phải > 0";
  }

  if (form.value.ngayBatDau && form.value.ngayKetThuc && form.value.ngayKetThuc < form.value.ngayBatDau) {
    return "Ngày kết thúc phải >= ngày bắt đầu";
  }

  if (form.value.loaiPhieu !== originalLoaiPhieu.value) {
    return "Không thể đổi loại phiếu khi cập nhật";
  }

  if (form.value.loaiPhieu === "CA_NHAN" && selectedKhIds.value.size === 0) {
    return "Phiếu cá nhân phải chọn ít nhất 1 khách hàng";
  }

  return "";
}

async function doUpdate() {
  const payload = {
    tenGiamGia: form.value.tenGiamGia,
    soLuong: form.value.soLuong,
    loaiGiam: form.value.loaiGiam,
    ngayBatDau: startOfDay(form.value.ngayBatDau),
    ngayKetThuc: endOfDay(form.value.ngayKetThuc),
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

  await axios.put(`${API}/update/${id}`, payload);

  if (form.value.loaiPhieu === "CA_NHAN") {
    const req = { khachHangIds: Array.from(selectedKhIds.value) };
    await axios.put(`${API}/${id}/khach-hang`, req);
  }
}

async function submit() {
  error.value = "";
  const msg = validate();
  if (msg) {
    toast.warning(msg);
    return;
  }

  openConfirm("Bạn có chắc muốn cập nhật phiếu giảm giá này không?", async () => {
    saving.value = true;
    try {
      await doUpdate();
      toast.success("Cập nhật phiếu giảm giá thành công");
      goBack();
    } catch (e) {
      const m = e?.response?.data?.message || e?.message || "Cập nhật thất bại";
      error.value = m;
      toast.error(m);
      throw e;
    } finally {
      saving.value = false;
    }
  });
}

onMounted(async () => {
  await fetchDetail();
  await nextTick();
  initPickers();

  if (isCaNhan.value) await loadKhachHangForPersonal();
});

onBeforeUnmount(() => {
  try { fpFrom?.destroy(); } catch {}
  try { fpTo?.destroy(); } catch {}
});
</script>

<style scoped>
/* ✅ Fix lệch input-group ngày */
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

/* ✅ nút confirm màu cũ */
.btn-confirm{
  background: #1f2a44;
  border-color: #1f2a44;
  color: #fff;
  font-weight: 700;
}
.btn-confirm:hover{ filter: brightness(0.95); }
.btn-confirm:disabled{ opacity: .6; cursor: not-allowed; }

/* ===== Modal ===== */
.modal-overlay{
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,.35);
  display:flex;
  align-items:center;
  justify-content:center;
  z-index: 9999;
}
.modal-card{
  width: min(440px, calc(100% - 32px));
  background:#fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 10px 30px rgba(0,0,0,.15);
}

/* =========================
   ✅ KH BOX (GRID) - đẹp, cách đều
   (không dùng .table để tránh base.css)
   ========================= */
.kh-box{
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
}

/* header */
.kh-head{
  display: grid;
  grid-template-columns: 44px 140px 1.6fr 160px 2fr;
  padding: 10px 12px;
  background: #1f2a44;
  color: white;
  font-weight: 700;
  font-size: 13px;
  border-bottom: 1px solid #e5e7eb;
}

/* body scroll */
.kh-body{
  max-height: 360px;
  overflow: auto;
}

/* row */
.kh-row{
  display: grid;
  grid-template-columns: 44px 140px 1.6fr 160px 2fr;
  padding: 10px 12px;
  font-size: 13px;
  border-bottom: 1px solid #e5e7eb;
  align-items: center;
}
.kh-row:hover{ background: #f8fafc; }

.kh-col{ min-width: 0; }
.kh-ellipsis{
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.kh-empty{
  padding: 16px 12px;
  text-align: center;
  color: #6b7280;
  font-weight: 600;
}
</style>
