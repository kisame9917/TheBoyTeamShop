// src/stores/shift.js
import { defineStore } from "pinia";
import { computed, ref, watch } from "vue";
import { useAuthStore } from "@/stores/auth";
import giaoCaApi from "@/services/giaoCaApi";
import { useToast } from "@/composables/useToast";

const WARN_BEFORE_MIN = 10;

export const useShiftStore = defineStore("shift", () => {
  const auth = useAuthStore();
  const toast = useToast();

  function _isStaffRole(role) {
    let r = String(role || "").trim().toUpperCase();
    if (!r || r === "NULL" || r === "UNDEFINED") return false;
    if (r.startsWith("ROLE_")) r = r.slice(5);
    return r === "STAFF" || r === "NHANVIEN" || r === "NHÂNVIÊN" || r === "NV" || r === "EMPLOYEE";
  }

  function _hasOpenShift(phien) {
    if (!phien) return false;
    const id = phien.id ?? phien.phienCaId ?? phien.idPhienCa ?? phien.maPhienCa;
    if (id !== null && id !== undefined && id !== "" && id !== 0) return true; // ✅ tránh id=0 giả
    const opened = phien.thoiGianMo || phien.gioMo || phien.openedAt || phien.createdAt;
    const closed = phien.thoiGianDong || phien.gioDong || phien.closedAt;
    return !!opened && !closed;
  }

  const booted = ref(false);
  const lastToken = ref(null);

  const mode = ref("VIEW_ONLY");
  const gateOpen = ref(false);
  const gateReason = ref("NEED_OPEN"); // NEED_OPEN | NEED_CLOSE
  const caInfo = ref({});
  const checking = ref(false);

  const secondsToEnd = ref(null);
  let _tick = null;
  let _warned = false;

  const isLocked = computed(() => mode.value !== "ACTIVE");

  // chống race: chỉ nhận kết quả request mới nhất
  const reqSeq = ref(0);

  function _stopTick() {
    if (_tick) {
      clearInterval(_tick);
      _tick = null;
    }
  }

  function _startTick() {
    _stopTick();
    _warned = false;
    secondsToEnd.value = null;

    const endAt = caInfo.value?.endAt || null;
    if (!endAt) return;
    const endMs = new Date(endAt).getTime();
    if (!Number.isFinite(endMs)) return;

    _tick = setInterval(() => {
      const nowMs = Date.now();
      const sec = Math.floor((endMs - nowMs) / 1000);
      secondsToEnd.value = sec;

      const warnSec = WARN_BEFORE_MIN * 60;
      if (!_warned && sec <= warnSec && sec > 0) {
        _warned = true;
        toast.warning(`Ca làm sắp hết (${WARN_BEFORE_MIN} phút). Vui lòng chuẩn bị giao ca.`);
      }
    }, 1000);
  }

  function _normalizeEndAt(phien) {
    if (!phien) return phien;
    if (phien.endAt && Number.isFinite(new Date(phien.endAt).getTime())) return phien;

    const date = phien.ngayLamViec || phien.ngay || phien.date;
    const endTime = phien.gioKetThuc || phien.ketThuc || phien.endTime;
    const startTime = phien.gioBatDau || phien.batDau || phien.startTime;

    if (date && typeof endTime === "string" && /^\d{2}:\d{2}/.test(endTime)) {
      let end = new Date(`${date}T${endTime}`);
      if (typeof startTime === "string" && /^\d{2}:\d{2}/.test(startTime)) {
        const start = new Date(`${date}T${startTime}`);
        if (end.getTime() <= start.getTime()) end = new Date(end.getTime() + 24 * 60 * 60 * 1000);
      }
      if (!Number.isNaN(end.getTime())) return { ...phien, endAt: end.toISOString() };
    }
    return phien;
  }

  function _applyCheckResponse(data) {
    const d = data || {};
    const hasOpen = _hasOpenShift(d?.phienDangMo);

    // ✅ QUAN TRỌNG:
    // Nếu có phiên đang mở nhưng BE nói "không nằm trong block hiện tại"
    // (currentBlock == null / secondsToEnd null / blockEndTime null) => bắt buộc đóng ca.
    const outOfBlock =
        hasOpen &&
        (d.currentBlock === null || d.currentBlock === undefined) &&
        (d.secondsToEnd === null || d.secondsToEnd === undefined) &&
        (d.blockEndTime === null || d.blockEndTime === undefined);

    if (outOfBlock) {
      caInfo.value = d;
      gateOpen.value = true;
      gateReason.value = "NEED_CLOSE";
      mode.value = "VIEW_ONLY";
      _stopTick();
      return;
    }

    // 1) đang có phiên ca mở hợp lệ => ACTIVE
    if (hasOpen) {
      const phienNorm = _normalizeEndAt(d.phienDangMo);
      caInfo.value = { ...d, phienDangMo: phienNorm, endAt: phienNorm?.endAt || d?.endAt };
      gateOpen.value = false;
      gateReason.value = "NEED_OPEN";
      mode.value = "ACTIVE";
      _startTick();
      return;
    }

    // 2) nếu BE trả needClose/action
    caInfo.value = d;
    if (d?.needClose === true || String(d?.action || "").toUpperCase() === "NEED_CLOSE") {
      gateOpen.value = true;
      gateReason.value = "NEED_CLOSE";
      mode.value = "VIEW_ONLY";
      _stopTick();
      return;
    }

    // 3) chưa mở ca => bắt buộc hiển thị modal
    gateOpen.value = true;
    gateReason.value = "NEED_OPEN";
    mode.value = "VIEW_ONLY";
    _stopTick();
  }

  async function bootstrap(force = false) {
    const token = auth.token || null;
    if (!force && booted.value && lastToken.value === token) return;
    booted.value = true;
    lastToken.value = token;

    if (!token) {
      gateOpen.value = false;
      mode.value = "VIEW_ONLY";
      _stopTick();
      return;
    }

    if (!(_isStaffRole(auth.role) || auth.isStaff)) {
      gateOpen.value = false;
      mode.value = "ACTIVE";
      _stopTick();
      return;
    }

    // staff: mở gate trước, rồi checkIn cập nhật
    checking.value = true;

// ✅ Nếu đang ACTIVE (đã vào ca) thì không bật modal trong lúc check nữa -> tránh blink
    if (mode.value === "ACTIVE") {
      gateOpen.value = false;
    } else {
      // nếu chưa active thì mới cho phép gate mở để hướng dẫn NV vào ca
      gateOpen.value = true;
      gateReason.value = "NEED_OPEN";
      mode.value = "VIEW_ONLY";
    }

    const mySeq = ++reqSeq.value;

    try {
      const data = await giaoCaApi.kiemTraCa();
      if (mySeq !== reqSeq.value) return;
      _applyCheckResponse(data);
    } catch {
      if (mySeq !== reqSeq.value) return;
      gateOpen.value = true;
      gateReason.value = "NEED_OPEN";
      mode.value = "VIEW_ONLY";
      caInfo.value = {};
      _stopTick();
    } finally {
      if (mySeq === reqSeq.value) checking.value = false;
    }
  }

  function enterViewMode() {
    mode.value = "VIEW_ONLY";
    gateOpen.value = false;
    _stopTick();
  }

  async function openShift({ tienMatDauCa, tienTaiKhoanDauCa }) {
    const payload = {
      tienMatDauCa: Number(tienMatDauCa || 0),
      tienTaiKhoanDauCa: Number(tienTaiKhoanDauCa || 0),
      lichLamViecId: caInfo.value?.lichLamViecId || caInfo.value?.idLichLamViec || undefined,
      idCaLamViec: caInfo.value?.idCaLamViec || undefined,
      moTuDo: false,
      ngayLamViec: caInfo.value?.ngayLamViec || undefined,
    };

    const data = await giaoCaApi.moCa(payload);

    const check = await giaoCaApi.kiemTraCa();
    _applyCheckResponse(check);
    return data;
  }

  async function closeShift({ tienMatThucTe, tienTaiKhoanThucTe }) {
    const payload = {
      tienMatThucTe: Number(tienMatThucTe || 0),
      tienTaiKhoanThucTe: Number(tienTaiKhoanThucTe || 0),
    };
    const data = await giaoCaApi.dongCa(payload);

    const check = await giaoCaApi.kiemTraCa();
    _applyCheckResponse(check);
    return data;
  }

  watch(
      () => [auth.token, auth.role],
      ([token, role]) => {
        if (!token) {
          gateOpen.value = false;
          mode.value = "VIEW_ONLY";
          _stopTick();
          return;
        }
        if (_isStaffRole(role) || auth.isStaff) bootstrap(true);
        else {
          gateOpen.value = false;
          mode.value = "ACTIVE";
          _stopTick();
        }
      },
      { immediate: true }
  );

  return {
    mode,
    isLocked,
    gateOpen,
    gateReason,
    caInfo,
    secondsToEnd,
    checking,
    bootstrap,
    enterViewMode,
    openShift,
    closeShift,
  };
});
