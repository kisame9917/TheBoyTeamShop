<template>
  <div v-if="visible" class="gate-overlay" @click.self="noop">
    <div class="gate-card">
      <div class="gate-head">
        <div class="head-title">MỞ CA LÀM VIỆC</div>
        <div class="head-sub">Hệ thống quản lý bán hàng Vest Shop</div>
      </div>

      <!-- NEED_CLOSE: bắt buộc đi kết toán -->
      <template v-if="shift.gateReason === 'NEED_CLOSE'">
        <div class="gate-body">
          <div class="alert alert-warning mb-0">
            Bạn đang còn một ca chưa được kết toán. Vui lòng vào <b>Giao ca &amp; Kết toán</b> để xác nhận đóng ca.
          </div>
        </div>

        <div class="gate-actions">
          <button class="btn btn-outline-secondary" type="button" @click="enterView">
            Chế độ xem
          </button>
          <button class="btn btn-primary" type="button" @click="gotoHandover">
            Đi tới giao ca
          </button>
        </div>
      </template>

      <!-- NEED_OPEN: mở ca hoặc xem -->
      <template v-else>
        <div class="gate-body">
          <div class="person">
            <div class="person-name">{{ staffName }}</div>
            <div class="person-meta">
              <span v-if="staffCode">Mã: <b>{{ staffCode }}</b></span>
              <span v-if="serverNowText">• {{ serverNowText }}</span>
            </div>
          </div>

          <div class="box">
            <div class="info-row">
              <div class="info-label">Ca</div>
              <div class="info-value">{{ caName }}</div>
            </div>
            <div class="info-row" v-if="timeRange">
              <div class="info-label">Thời gian</div>
              <div class="info-value">{{ timeRange }}</div>
            </div>
            <div class="info-row" v-if="countdownText">
              <div class="info-label">Bộ đếm</div>
              <div class="info-value">{{ countdownText }}</div>
            </div>
            <div v-if="hintText" class="hint">{{ hintText }}</div>
          </div>

          <div class="row g-2 mt-3">
            <div class="col-12">
              <label class="form-label mb-1">Tiền mặt đầu ca</label>
              <input
                  class="form-control"
                  type="text"
                  inputmode="numeric"
                  :value="tienMatDauCaText"
                  :disabled="opening"
                  @input="onTienMatInput"
              />
              <div class="form-text" v-if="expectedCash !== null">
                Số dự kiến từ ca trước: <b>{{ money(expectedCash) }}</b>
              </div>
            </div>
            <div class="col-12">
              <label class="form-label mb-1">Tiền tài khoản đầu ca</label>
              <input
                  class="form-control"
                  type="text"
                  inputmode="numeric"
                  :value="tienTaiKhoanDauCaText"
                  :disabled="opening"
                  @input="onTienTaiKhoanInput"
              />
              <div class="form-text" v-if="expectedTransfer !== null">
                Số dự kiến từ ca trước: <b>{{ money(expectedTransfer) }}</b>
              </div>
              <div v-if="errorMsg" class="text-danger small mt-1">{{ errorMsg }}</div>
            </div>
          </div>
        </div>

        <div class="gate-actions">
          <button class="btn btn-outline-secondary" type="button" @click="enterView" :disabled="opening">
            Chế độ xem
          </button>

          <button
              class="btn btn-primary"
              type="button"
              @click="confirmOpen"
              :disabled="opening || !allowOpen"
              :title="allowOpen ? '' : 'Chưa tới ca, không thể vào ca'"
          >
            <span v-if="opening" class="spinner-border spinner-border-sm me-2" role="status"></span>
            Xác nhận vào ca
          </button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import { useShiftStore } from "@/stores/shift";

const router = useRouter();
const auth = useAuthStore();
const shift = useShiftStore();

const visible = computed(() => auth.isStaff && shift.gateOpen && !shift.checking);

const opening = ref(false);
const tienMatDauCa = ref(0);
const tienTaiKhoanDauCa = ref(0);
const tienMatDauCaText = ref("0");
const tienTaiKhoanDauCaText = ref("0");
const errorMsg = ref("");

const countdownSeconds = ref(0);
const countdownHadSource = ref(false);
let countdownTimer = null;

function readCountdownSeconds() {
  const raw = shift.caInfo?.secondsToStart ?? shift.caInfo?.secondsToOpen;
  const sec = Number(raw);
  if (!Number.isFinite(sec)) return 0;
  return Math.max(0, Math.ceil(sec));
}

function resetCountdown() {
  countdownSeconds.value = readCountdownSeconds();
  countdownHadSource.value =
      shift.caInfo?.secondsToStart !== undefined ||
      shift.caInfo?.secondsToOpen !== undefined;
}

function stopCountdown() {
  if (countdownTimer) {
    clearInterval(countdownTimer);
    countdownTimer = null;
  }
}

function startCountdown() {
  stopCountdown();
  resetCountdown();

  if (!visible.value) return;

  countdownTimer = setInterval(() => {
    if (countdownSeconds.value > 0) {
      countdownSeconds.value -= 1;
    } else {
      countdownSeconds.value = 0;
    }
  }, 1000);
}

watch(
    () => [
      visible.value,
      shift.caInfo?.secondsToStart,
      shift.caInfo?.secondsToOpen,
      shift.gateOpen,
    ],
    ([isVisible]) => {
      if (isVisible) {
        startCountdown();
      } else {
        stopCountdown();
      }
    },
    { immediate: true }
);

onBeforeUnmount(() => {
  stopCountdown();
});

function parseMoneyInput(value) {
  const raw = String(value ?? "").replace(/[^\d]/g, "");
  return raw ? Number(raw) : 0;
}

function formatMoneyInput(value) {
  const numberValue = Number(value || 0);
  return numberValue.toLocaleString("vi-VN");
}

function onTienMatInput(event) {
  const value = parseMoneyInput(event.target.value);
  tienMatDauCa.value = value;
  tienMatDauCaText.value = value ? formatMoneyInput(value) : "";
  event.target.value = tienMatDauCaText.value;
}

function onTienTaiKhoanInput(event) {
  const value = parseMoneyInput(event.target.value);
  tienTaiKhoanDauCa.value = value;
  tienTaiKhoanDauCaText.value = value ? formatMoneyInput(value) : "";
  event.target.value = tienTaiKhoanDauCaText.value;
}

const staffName = computed(() => {
  return shift.caInfo?.tenNhanVien || auth.user?.tenNhanVien || auth.user?.taiKhoan || "Nhân viên";
});

const staffCode = computed(() => {
  return shift.caInfo?.maNhanVien || auth.user?.id || auth.user?.maNhanVien || "";
});

const serverNowText = computed(() => {
  const v = shift.caInfo?.serverNow || shift.caInfo?.now;
  if (!v) return "";

  const d = new Date(v);
  if (Number.isNaN(d.getTime())) return String(v);

  return new Intl.DateTimeFormat("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour12: false,
  }).format(d);
});

const countdownText = computed(() => {
  const sec = Number(countdownSeconds.value || 0);
  if (!Number.isFinite(sec) || sec <= 0) return "";

  const h = Math.floor(sec / 3600);
  const m = Math.floor((sec % 3600) / 60);
  const s = Math.floor(sec % 60);

  return `Bắt đầu sau ${String(h).padStart(2, "0")}:${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
});

const allowOpen = computed(() => {
  if (shift.caInfo?.coLichPhanCong === false) return false;

  const v = shift.caInfo?.allowOpen ?? shift.caInfo?.canOpen ?? shift.caInfo?.duocMoCa;

  if (v === undefined) return true;
  if (v) return true;

  return countdownHadSource.value && countdownSeconds.value <= 0;
});

const hintText = computed(() => {
  const hasSchedule = shift.caInfo?.coLichPhanCong;
  if (hasSchedule === false) {
    return "Bạn không có lịch phân công. Bạn chỉ có thể vào chế độ xem.";
  }

  if (!allowOpen.value) {
    const msg = shift.caInfo?.message || shift.caInfo?.thongBao;
    return msg ? String(msg) : "Chưa tới ca. Bạn có thể vào chế độ xem để tham khảo.";
  }

  return "";
});

const caName = computed(() => {
  return shift.caInfo?.tenCa || shift.caInfo?.caTen || shift.caInfo?.name || "Ca làm việc";
});

const timeRange = computed(() => {
  const s = shift.caInfo?.startAt || shift.caInfo?.gioBatDau || shift.caInfo?.batDau;
  const e = shift.caInfo?.endAt || shift.caInfo?.gioKetThuc || shift.caInfo?.ketThuc;

  if (!s || !e) return "";

  return `${fmtTime(s)} - ${fmtTime(e)}`;
});

const expectedCash = computed(() => {
  const v =
      shift.caInfo?.expectedTienMatDauCa ??
      shift.caInfo?.tienMatDuKien ??
      shift.caInfo?.soTienMatDauCaDuKien ??
      shift.caInfo?.duKienTienMat;

  if (v === undefined || v === null || v === "") return null;

  return Number(v);
});

const expectedTransfer = computed(() => {
  const v =
      shift.caInfo?.expectedTienTaiKhoanDauCa ??
      shift.caInfo?.tienTaiKhoanDuKien ??
      shift.caInfo?.soTienTaiKhoanDauCaDuKien ??
      shift.caInfo?.duKienTienTaiKhoan;

  if (v === undefined || v === null || v === "") return null;

  return Number(v);
});

watch(
    () => shift.gateOpen,
    (open) => {
      if (!open) return;

      opening.value = false;
      errorMsg.value = "";

      tienMatDauCa.value = expectedCash.value ?? 0;
      tienTaiKhoanDauCa.value = expectedTransfer.value ?? 0;

      tienMatDauCaText.value = formatMoneyInput(tienMatDauCa.value);
      tienTaiKhoanDauCaText.value = formatMoneyInput(tienTaiKhoanDauCa.value);
    },
    { immediate: true }
);

function money(v) {
  const n = Number(v || 0);
  return new Intl.NumberFormat("vi-VN").format(n) + " đ";
}

function fmtTime(v) {
  if (v === null || v === undefined) return "";

  if (typeof v === "string") {
    if (/^\d{2}:\d{2}/.test(v)) return v.slice(0, 5);

    if (v.includes("T")) {
      const d = new Date(v);
      if (!Number.isNaN(d.getTime())) {
        return new Intl.DateTimeFormat("vi-VN", {
          hour: "2-digit",
          minute: "2-digit",
          hour12: false,
        }).format(d);
      }
    }

    return v;
  }

  const d = new Date(v);

  if (!Number.isNaN(d.getTime())) {
    return new Intl.DateTimeFormat("vi-VN", {
      hour: "2-digit",
      minute: "2-digit",
      hour12: false,
    }).format(d);
  }

  return String(v);
}

function noop() {}

function enterView() {
  shift.enterViewMode();
}

function gotoHandover() {
  router.push({ name: "shift-handover" });
}

async function confirmOpen() {
  if (!allowOpen.value) return;

  errorMsg.value = "";

  if (expectedCash.value !== null) {
    const input = Number(tienMatDauCa.value || 0);
    const exp = Number(expectedCash.value || 0);

    if (Math.abs(input - exp) > 0.0001) {
      errorMsg.value = `Tiền mặt đầu ca không khớp. Số tiền đúng là ${money(exp)}.`;
      return;
    }
  }

  if (expectedTransfer.value !== null) {
    const input = Number(tienTaiKhoanDauCa.value || 0);
    const exp = Number(expectedTransfer.value || 0);

    if (Math.abs(input - exp) > 0.0001) {
      errorMsg.value = `Tiền tài khoản đầu ca không khớp. Số tiền đúng là ${money(exp)}.`;
      return;
    }
  }

  try {
    opening.value = true;

    await shift.openShift({
      tienMatDauCa: Number(tienMatDauCa.value || 0),
      tienTaiKhoanDauCa: Number(tienTaiKhoanDauCa.value || 0),
    });
  } catch (e) {
    const msg =
        e?.response?.data?.message ||
        e?.response?.data?.error ||
        e?.message ||
        "Không thể vào ca. Vui lòng thử lại.";

    errorMsg.value = String(msg);
  } finally {
    opening.value = false;
  }
}
</script>

<style scoped>
.gate-overlay{
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.55);
  z-index: 99999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.gate-card{
  width: min(520px, 100%);
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 20px 60px rgba(0,0,0,.25);
  overflow: hidden;
  border: 1px solid rgba(2,132,199,.25);
}

.gate-head{
  padding: 14px 16px;
  background: #0b74c8;
  border-bottom: 1px solid rgba(2,132,199,.35);
  text-align: center;
}

.head-title{
  color: #fff;
  font-weight: 900;
  letter-spacing: .6px;
  font-size: 18px;
}
.head-sub{
  color: rgba(255,255,255,.92);
  font-size: 12px;
  margin-top: 4px;
}

.gate-body{ padding: 16px; }

.person{ text-align: center; }
.person-name{ font-weight: 900; font-size: 18px; color:#0f172a; }
.person-meta{ margin-top: 4px; font-size: 12px; color:#64748b; }

.box{
  margin-top: 12px;
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  background: #f8fafc;
  padding: 12px;
}
.hint{ margin-top: 10px; font-size: 12.5px; color:#334155; }

.info-row{ display:flex; justify-content: space-between; gap: 12px; margin-bottom: 6px; }
.info-label{ font-size: 12px; color:#64748b; }
.info-value{ font-size: 12px; color:#0f172a; font-weight: 700; text-align: right; }

.gate-actions{
  display:flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 0 16px 16px;
}

.btn{
  border-radius: 12px;
  font-weight: 800;
  padding: 10px 14px;
}

.btn-primary{
  background: #0b74c8;
  border-color: #0b74c8;
}
.btn-primary:hover{
  background: #085fa6;
  border-color: #085fa6;
}
</style>
