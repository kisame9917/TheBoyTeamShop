<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal">
      <h3 class="modal-title">Chi tiết phiếu giảm giá</h3>

      <div v-if="loading">Đang tải chi tiết...</div>
      <div v-else-if="error" class="err">{{ error }}</div>

      <div v-else class="detail-grid">
        <div><b>Mã:</b> {{ data?.maGiamGia ?? data?.ma_giam_gia ?? '-' }}</div>
        <div><b>Tên:</b> {{ data?.tenGiamGia ?? data?.ten_giam_gia ?? '-' }}</div>

        <div><b>Loại:</b> {{ (data?.loaiGiam ?? data?.loai_giam) ? 'Giảm %' : 'Giảm tiền' }}</div>
        <div>
          <b>Giá trị:</b>
          <span v-if="(data?.loaiGiam ?? data?.loai_giam)">
            {{ formatPercent(data?.giaTriPhanTram ?? data?.gia_tri_phan_tram) }}
          </span>
          <span v-else>
            {{ formatMoney(data?.giaTriTienMat ?? data?.gia_tri_tien_mat) }}
          </span>
        </div>

        <div><b>Số lượng:</b> {{ data?.soLuong ?? data?.so_luong ?? 0 }}</div>
        <div><b>Trạng thái:</b> {{ (data?.trangThai ?? data?.trang_thai) ? 'Hoạt động' : 'Đã xoá' }}</div>

        <div class="full desc"><b>Mô tả:</b> {{ (data?.mo_ta ?? data?.moTa) || '-' }}</div>

        <div><b>Ngày bắt đầu:</b> {{ formatDate(data?.ngayBatDau ?? data?.ngay_bat_dau) }}</div>
        <div><b>Ngày kết thúc:</b> {{ formatDate(data?.ngayKetThuc ?? data?.ngay_ket_thuc) }}</div>

        <div><b>Ngày tạo:</b> {{ formatDate(data?.ngayTao ?? data?.ngay_tao) }}</div>
        <div><b>Ngày cập nhật:</b> {{ formatDate(data?.ngayCapNhat ?? data?.ngay_cap_nhat) }}</div>
      </div>

      <div class="modal-actions">
        <button class="btn" @click="$emit('close')">Đóng</button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  data: Object,
  loading: Boolean,
  error: String
})

function formatPercent(v) {
  if (v === null || v === undefined || v === '') return '-'
  return `${v}%`
}

function formatMoney(v) {
  if (v === null || v === undefined || v === '') return '-'
  const n = Number(v)
  if (Number.isNaN(n)) return String(v)
  return n.toLocaleString('vi-VN') + ' ₫'
}

function formatDate(d) {
  if (!d) return '-'
  const s = String(d).trim().replace(' ', 'T')
  const dt = new Date(s)
  if (Number.isNaN(dt.getTime())) return String(d)
  return dt.toLocaleString('vi-VN')
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

/* ✅ đồng bộ với form create/edit (width 900px, padding lớn) */
.modal {
  background: #fff;
  width: 900px;          /* đồng bộ */
  max-width: 96%;
  border-radius: 10px;   /* đồng bộ gần giống */
  padding: 22px 24px;    /* đồng bộ */
  box-shadow: 0 20px 40px rgba(0,0,0,.25);
}

.modal-title {
  margin-bottom: 14px;
  font-size: 18px;
  font-weight: 700;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 18px;
  font-size: 14px;
  line-height: 1.5;
}

.detail-grid .full { grid-column: 1 / -1; }

/* ✅ mô tả xuống dòng đẹp nếu có nhiều dòng */
.desc { white-space: pre-line; }

.modal-actions {
  margin-top: 16px;
  text-align: right;
}

.btn {
  padding: 8px 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
}

.err { color: #b91c1c; }
</style>
