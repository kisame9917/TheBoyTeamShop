<template>
  <div class="page">
    <!-- top bar -->
    <div class="topbar">
      <h1>Chi tiết phiếu giảm giá</h1>

      <button class="btn btn-secondary" @click="goBack">
        ← Quay lại danh sách
      </button>
    </div>

    <div class="card">
      <div v-if="loading" class="state">Đang tải chi tiết...</div>
      <div v-else-if="error" class="state err">{{ error }}</div>

      <div v-else class="detail-grid">
        <div><b>Mã:</b> {{ d.maGiamGia ?? '-' }}</div>
        <div><b>Tên:</b> {{ d.tenGiamGia ?? '-' }}</div>

        <div><b>Loại phiếu:</b> {{ getLoaiPhieuText(d) }}</div>
        <div><b>Trạng thái hệ thống:</b> {{ d.trangThai ? 'Hoạt động' : 'Đã xoá' }}</div>

        <div><b>Loại giảm:</b> {{ d.loaiGiam ? 'Giảm %' : 'Giảm tiền' }}</div>
        <div>
          <b>Giá trị:</b>
          <span v-if="d.loaiGiam">{{ formatPercent(d.giaTriPhanTram) }}</span>
          <span v-else>{{ formatMoney(d.giaTriTienMat) }}</span>
        </div>

        <div><b>Số lượng:</b> {{ d.soLuong ?? 0 }}</div>
        <div><b>Đơn hàng tối thiểu:</b> {{ formatMoney(d.donHangToiThieu) }}</div>

        <div v-if="d.loaiGiam">
          <b>Giảm tối đa:</b> {{ formatMoney(d.giaTriGiamToiDa) }}
        </div>
        <div v-else></div>

        <div><b>Ngày bắt đầu:</b> {{ formatDateTime(d.ngayBatDau) }}</div>
        <div><b>Ngày kết thúc:</b> {{ formatDateTime(d.ngayKetThuc) }}</div>

        <div><b>Ngày tạo:</b> {{ formatDateTime(d.ngayTao) }}</div>
        <div><b>Ngày cập nhật:</b> {{ formatDateTime(d.ngayCapNhat) }}</div>

        <div class="full desc">
          <b>Mô tả:</b> {{ d.moTa || '-' }}
        </div>
      </div>

      <div class="actions">
        <button class="btn btn-secondary" @click="goBack">Đóng</button>
        <button class="btn btn-primary" @click="goEdit">Sửa</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue"
import axios from "axios"
import { useRoute, useRouter } from "vue-router"

const route = useRoute()
const router = useRouter()
const API = "http://localhost:8080/api/pgg"

const loading = ref(false)
const error = ref("")
const detail = ref(null)

const id = computed(() => Number(route.params.id))

const d = computed(() => {
  const x = detail.value || {}
  return {
    ...x,
    // normalize snake_case nếu BE trả kiểu đó
    maGiamGia: x.maGiamGia ?? x.ma_giam_gia,
    tenGiamGia: x.tenGiamGia ?? x.ten_giam_gia,
    soLuong: x.soLuong ?? x.so_luong,
    trangThai: x.trangThai ?? x.trang_thai,
    loaiGiam: x.loaiGiam ?? x.loai_giam,
    giaTriPhanTram: x.giaTriPhanTram ?? x.gia_tri_phan_tram,
    giaTriTienMat: x.giaTriTienMat ?? x.gia_tri_tien_mat,
    donHangToiThieu: x.donHangToiThieu ?? x.don_hang_toi_thieu,
    giaTriGiamToiDa: x.giaTriGiamToiDa ?? x.gia_tri_giam_toi_da,
    loaiPhieu: x.loaiPhieu ?? x.loai_phieu,
    moTa: x.moTa ?? x.mo_ta,
    ngayBatDau: x.ngayBatDau ?? x.ngay_bat_dau,
    ngayKetThuc: x.ngayKetThuc ?? x.ngay_ket_thuc,
    ngayTao: x.ngayTao ?? x.ngay_tao,
    ngayCapNhat: x.ngayCapNhat ?? x.ngay_cap_nhat,
  }
})

function goBack() {
  router.push("/vouchers")
}

/** ✅ sửa theo router của bạn: { path: 'vouchers/update/:id' } */
function goEdit() {
  router.push(`/vouchers/update/${id.value}`)
}

async function load() {
  loading.value = true
  error.value = ""
  try {
    const res = await axios.get(`${API}/${id.value}`)
    detail.value = res.data
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || "Không tải được chi tiết"
  } finally {
    loading.value = false
  }
}

function getLoaiPhieuText(x) {
  const v = x?.loaiPhieu
  if (v === true) return "Cá nhân"
  if (v === false) return "Công khai"
  if (String(v).toUpperCase() === "CA_NHAN") return "Cá nhân"
  if (String(v).toUpperCase() === "CONG_KHAI") return "Công khai"
  return "-"
}

function formatPercent(v) {
  if (v === null || v === undefined || v === "") return "-"
  return `${v}%`
}

function formatMoney(v) {
  if (v === null || v === undefined || v === "") return "-"
  const n = Number(v)
  if (Number.isNaN(n)) return String(v)
  return n.toLocaleString("vi-VN") + " ₫"
}

/** ✅ DATETIME: dd/MM/yyyy HH:mm (BE: 2026-01-18T18:14:00) */
function formatDateTime(v) {
  if (!v) return "-"
  const d = new Date(String(v))
  if (Number.isNaN(d.getTime())) return String(v)

  const dd = String(d.getDate()).padStart(2, "0")
  const mm = String(d.getMonth() + 1).padStart(2, "0")
  const yyyy = d.getFullYear()
  const hh = String(d.getHours()).padStart(2, "0")
  const mi = String(d.getMinutes()).padStart(2, "0")

  return `${dd}/${mm}/${yyyy} ${hh}:${mi}`
}

onMounted(load)
</script>

<style scoped>
.page {
  padding: 18px 22px;
  background: #f5f6f8;
  min-height: 100vh;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #111827;
}

.card {
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  padding: 16px 18px;
}

.state {
  font-size: 13px;
  color: #374151;
}
.err { color: #b91c1c; }

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 18px;
  font-size: 14px;
  line-height: 1.55;
  margin-top: 6px;
}

.detail-grid .full { grid-column: 1 / -1; }
.desc { white-space: pre-line; }

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}

.btn {
  height: 38px;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  padding: 0 14px;
  font-size: 14px;
  cursor: pointer;
}

.btn-primary {
  background: #f97316;
  border-color: #f97316;
  color: #fff;
  font-weight: 700;
}

.btn-secondary {
  background: #eef2f7;
  border-color: #e5e7eb;
  color: #111827;
}
</style>
