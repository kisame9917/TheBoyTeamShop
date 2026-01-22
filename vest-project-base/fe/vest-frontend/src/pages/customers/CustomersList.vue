<template>
  <div class="customer-page">
    <div class="page-title">
      <h2>Quản lý khách hàng / Danh sách khách hàng</h2>
    </div>

    <!-- FILTER (giữ nguyên ý tưởng như hình bạn gửi) -->
    <div class="card filter-card">
      <div class="card-header">
        <span class="collapse-icon">▼</span>
        <span class="card-title">Bộ lọc tìm kiếm</span>
      </div>

      <div class="filter-wrapper">
        <div class="filter-left">
          <div class="form-group f-search">
            <label>Tìm kiếm</label>
            <input
                class="form-input"
                v-model="filters.keyword"
                placeholder="Tìm theo mã, tên, email, SĐT, tài khoản..."
            />
          </div>

          <div class="form-group f-email">
            <label>Email</label>
            <input class="form-input" v-model="filters.email" placeholder="Email" />
          </div>

          <div class="form-group f-phone">
            <label>Số điện thoại</label>
            <input class="form-input" v-model="filters.phone" placeholder="SĐT" />
          </div>

          <div class="form-group f-status">
            <label>Trạng thái hiển thị</label>
            <div class="radio-row">
              <label class="radio-item">
                <input type="radio" v-model="filters.status" :value="1" /> Hoạt động
              </label>
              <label class="radio-item">
                <input type="radio" v-model="filters.status" :value="0" /> Không hoạt động
              </label>
              <label class="radio-item">
                <input type="radio" v-model="filters.status" :value="-1" /> Tất cả
              </label>
            </div>
          </div>

          <div class="form-group f-code">
            <label>Mã khách hàng</label>
            <input class="form-input" v-model="filters.ma" placeholder="Mã KH" />
          </div>

          <div class="form-group f-acc">
            <label>Tài khoản</label>
            <input class="form-input" v-model="filters.taiKhoan" placeholder="Tài khoản" />
          </div>
        </div>

        <div class="filter-right">
          <button class="btn btn-primary" type="button" @click="goCreate">
            <span class="icon">＋</span> Thêm mới
          </button>

          <button class="btn btn-outline" type="button" @click="resetFilters">
            <span class="icon">⟲</span> Đặt lại
          </button>
        </div>
      </div>
    </div>

    <!-- TABLE -->
    <div class="card">
      <div class="table-header">
        <div class="total">Tổng số khách hàng {{ filteredList.length }}</div>
      </div>

      <div class="table-wrap">
        <table class="customer-table">
          <thead>
          <tr>
            <th class="col-stt">STT</th>
            <th class="col-avatar">Ảnh</th>
            <th class="col-code">Mã KH</th>
            <th class="col-name">Họ tên</th>
            <th class="col-gender">Giới tính</th>
            <th class="col-email">Email</th>
            <th class="col-phone">SĐT</th>
            <th class="col-address">Địa chỉ</th>
            <th class="col-status">Trạng thái</th>
            <th class="col-action">Hành động</th>
          </tr>
          </thead>

          <tbody>
          <tr v-for="(c, idx) in pagedList" :key="c.id">
            <td class="ellipsis">{{ pageIndexStart + idx + 1 }}</td>

            <td class="ellipsis">
              <div class="avatar-cell">
                <img
                    v-if="resolveAvatar(c)"
                    :src="resolveAvatar(c)"
                    class="avatar-img"
                    alt="avatar"
                    @error="onAvatarError($event, c)"
                />
                <div v-else class="avatar-fallback">{{ getInitials(c.tenKhachHang) }}</div>
              </div>
            </td>

            <td>{{ c.maKhachHang }}</td>
            <td>{{ c.tenKhachHang }}</td>
            <td class="center">{{ genderText(c.gioiTinh) }}</td>

            <td class="ellipsis" :title="c.email || ''">{{ c.email || '-' }}</td>
            <td>{{ c.soDienThoai || '-' }}</td>

            <td class="ellipsis" :title="c.diaChi || ''">{{ c.diaChi || '-' }}</td>

            <td class="center">
                <span :class="['badge', c.trangThai ? 'badge-active' : 'badge-inactive']">
                  {{ c.trangThai ? 'Hoạt động' : 'Không hoạt động' }}
                </span>
            </td>

            <td class="center">
              <div class="action-row">
                <label class="switch" title="Đổi trạng thái">
                  <input
                      type="checkbox"
                      :checked="!!c.trangThai"
                      @click.prevent="confirmToggleStatus(c)"
                  />
                  <span class="slider"></span>
                </label>

                <button class="icon-btn" type="button" title="Xem chi tiết" @click="openDetail(c)">
                  👁
                </button>

                <button class="icon-btn icon-edit" type="button" title="Sửa" @click="goEdit(c.id)">
                  ✎
                </button>
              </div>
            </td>
          </tr>

          <tr v-if="!pagedList.length">
            <td class="empty" colspan="10">Không có dữ liệu</td>
          </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="pagination" v-if="pageCount > 1">
        <button class="p-btn" :disabled="page === 1" @click="page = page - 1">‹</button>

        <button
            v-for="p in pageCount"
            :key="p"
            class="p-btn"
            :class="{ active: p === page }"
            @click="page = p"
        >
          {{ p }}
        </button>

        <button class="p-btn" :disabled="page === pageCount" @click="page = page + 1">›</button>

        <div class="page-text">Trang {{ page }}/{{ pageCount }}</div>
      </div>
    </div>

    <!-- DETAIL MODAL -->
    <div v-if="detailOpen" class="modal-overlay" @click.self="detailOpen = false">
      <div class="modal">
        <div class="modal-header">
          <div class="modal-title">Chi tiết khách hàng</div>
          <button class="modal-close" type="button" @click="detailOpen = false">×</button>
        </div>

        <div class="modal-body">
          <div class="detail-top">
            <div class="detail-avatar">
              <img
                  v-if="resolveAvatar(detailData)"
                  :src="resolveAvatar(detailData)"
                  class="detail-avatar-img"
                  alt="avatar"
                  @error="onAvatarError($event, detailData)"
              />
              <div v-else class="detail-avatar-fallback">
                {{ getInitials(detailData && detailData.tenKhachHang) }}
              </div>
            </div>

            <div class="detail-head">
              <div class="detail-name">{{ (detailData && detailData.tenKhachHang) || '-' }}</div>
              <div class="detail-badges">
                <span class="badge badge-role">{{ (detailData && detailData.maKhachHang) || '-' }}</span>
                <span class="badge badge-role">{{ genderText(detailData && detailData.gioiTinh) }}</span>
                <span :class="['badge', detailData && detailData.trangThai ? 'badge-active' : 'badge-inactive']">
                  {{ detailData && detailData.trangThai ? 'Hoạt động' : 'Không hoạt động' }}
                </span>
              </div>
            </div>
          </div>

          <div class="detail-grid">
            <div class="detail-box">
              <div class="detail-label">Email</div>
              <div class="detail-value">{{ (detailData && detailData.email) || '-' }}</div>
            </div>

            <div class="detail-box">
              <div class="detail-label">SĐT</div>
              <div class="detail-value">{{ (detailData && detailData.soDienThoai) || '-' }}</div>
            </div>

            <div class="detail-box">
              <div class="detail-label">Tài khoản</div>
              <div class="detail-value">{{ (detailData && detailData.taiKhoan) || '-' }}</div>
            </div>

            <div class="detail-box col-full">
              <div class="detail-label">Địa chỉ</div>
              <div class="detail-value">{{ (detailData && detailData.diaChi) || '-' }}</div>
            </div>

            <div class="detail-box">
              <div class="detail-label">Người nhận</div>
              <div class="detail-value">
                {{ (detailData && detailData.diaChiMacDinh && detailData.diaChiMacDinh.tenNguoiNhan) || '-' }}
              </div>
            </div>

            <div class="detail-box">
              <div class="detail-label">SĐT người nhận</div>
              <div class="detail-value">
                {{ (detailData && detailData.diaChiMacDinh && detailData.diaChiMacDinh.soDienThoai) || '-' }}
              </div>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-ghost" type="button" @click="detailOpen = false">Đóng</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import http from '../../services/http'

const router = useRouter()

const list = ref([])
const page = ref(1)
const pageSize = 10

const filters = reactive({
  keyword: '',
  email: '',
  phone: '',
  ma: '',
  taiKhoan: '',
  status: -1, // -1 tất cả, 1 active, 0 inactive
})

watch(
    () => [filters.keyword, filters.email, filters.phone, filters.ma, filters.taiKhoan, filters.status],
    () => (page.value = 1)
)

function unwrapList(data) {
  if (!data) return []
  if (Array.isArray(data)) return data
  if (data && Array.isArray(data.result)) return data.result
  if (data && Array.isArray(data.content)) return data.content
  return []
}

function normalizeCustomer(x) {
  x = x || {}
  return {
    id: x.id,
    maKhachHang: x.maKhachHang ?? x.ma_khach_hang ?? '',
    tenKhachHang: x.tenKhachHang ?? x.ten_khach_hang ?? '',
    gioiTinh: x.gioiTinh ?? x.gioi_tinh ?? null, // true/false
    email: x.email ?? '',
    soDienThoai: x.soDienThoai ?? x.so_dien_thoai ?? '',
    taiKhoan: x.taiKhoan ?? x.tai_khoan ?? '',
    trangThai: x.trangThai ?? x.trang_thai ?? true,
    anhDaiDien: x.anhDaiDien ?? x.anh_dai_dien ?? '',
    diaChi: x.diaChi ?? '',
    diaChiMacDinh: x.diaChiMacDinh ?? null,
  }
}

function safeStr(v) {
  return String(v == null ? '' : v).toLowerCase().trim()
}

function genderText(v) {
  if (v === true) return 'Nam'
  if (v === false) return 'Nữ'
  return '-'
}

function getInitials(name) {
  const s = String(name || '').trim()
  if (!s) return 'KH'
  const parts = s.split(/\s+/).filter(Boolean)
  const a = parts[0] ? parts[0][0] : 'K'
  const b = parts[parts.length - 1] ? parts[parts.length - 1][0] : 'H'
  return (a + b).toUpperCase()
}

/** ===== Avatar URL ===== */
const FALLBACK_BACKEND = 'http://localhost:8080'

function getBackendOrigin() {
  const base = String((http && http.defaults && http.defaults.baseURL) || '').trim()
  if (base.startsWith('http://') || base.startsWith('https://')) {
    try {
      return new URL(base).origin
    } catch {
      return FALLBACK_BACKEND
    }
  }
  return FALLBACK_BACKEND
}

function resolveFileUrl(url) {
  const u = String(url || '').trim()
  if (!u) return ''
  if (u.startsWith('http://') || u.startsWith('https://') || u.startsWith('data:image')) return u
  const origin = getBackendOrigin()
  return u.startsWith('/') ? origin + u : origin + '/' + u
}

function resolveAvatar(c) {
  const url = String(c?.anhDaiDien || '').trim()
  if (!url) return ''
  return resolveFileUrl(url)
}

function onAvatarError(e, c) {
  if (c) c.anhDaiDien = ''
  if (e && e.target) e.target.src = ''
}

/** ===== Data ===== */
async function fetchList() {
  const res = await http.get('/api/khach-hang')
  list.value = unwrapList(res.data).map(normalizeCustomer)
}

const filteredList = computed(() => {
  const kw = safeStr(filters.keyword)
  const email = safeStr(filters.email)
  const phone = safeStr(filters.phone)
  const ma = safeStr(filters.ma)
  const acc = safeStr(filters.taiKhoan)

  return (list.value || []).filter((c) => {
    // status
    if (filters.status !== -1) {
      if (filters.status === 1 && !c.trangThai) return false
      if (filters.status === 0 && c.trangThai) return false
    }

    if (email && !safeStr(c.email).includes(email)) return false
    if (phone && !safeStr(c.soDienThoai).includes(phone)) return false
    if (ma && !safeStr(c.maKhachHang).includes(ma)) return false
    if (acc && !safeStr(c.taiKhoan).includes(acc)) return false

    if (kw) {
      const blob =
          (String(c.maKhachHang || '') +
              ' ' +
              String(c.tenKhachHang || '') +
              ' ' +
              String(c.email || '') +
              ' ' +
              String(c.soDienThoai || '') +
              ' ' +
              String(c.taiKhoan || '')).toLowerCase()
      if (!blob.includes(kw)) return false
    }

    return true
  })
})

const pageCount = computed(() => Math.max(1, Math.ceil(filteredList.value.length / pageSize)))
const pageIndexStart = computed(() => (page.value - 1) * pageSize)
const pagedList = computed(() => {
  const start = pageIndexStart.value
  return filteredList.value.slice(start, start + pageSize)
})

function resetFilters() {
  filters.keyword = ''
  filters.email = ''
  filters.phone = ''
  filters.ma = ''
  filters.taiKhoan = ''
  filters.status = -1
  page.value = 1
}

function goCreate() {
  router.push({ name: 'customer-new' })
}

function goEdit(id) {
  router.push({ name: 'customer-edit', params: { id } })
}

async function confirmToggleStatus(c) {
  const next = !c.trangThai
  const txt = next ? 'chuyển sang Hoạt động' : 'chuyển sang Không hoạt động'
  if (!confirm('Bạn có chắc chắn ' + txt + ' không?')) return

  await http.patch('/api/khach-hang/' + c.id + '/trang-thai', { trangThai: next })
  c.trangThai = next

  // để không “mất dòng” do filter
  filters.status = -1
  page.value = 1
}

/** ===== Detail modal ===== */
const detailOpen = ref(false)
const detailData = ref(null)

async function openDetail(c) {
  // lấy detail từ API để có diaChiMacDinh đầy đủ (nếu list chưa có)
  const res = await http.get('/api/khach-hang/' + c.id)
  detailData.value = normalizeCustomer(res.data)
  detailOpen.value = true
}

onMounted(fetchList)
</script>

<style scoped>
.customer-page { font-size: 15px; }

.page-title h2 {
  margin: 0 0 12px 0;
  font-size: 18px;
  font-weight: 700;
}

.card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 14px;
  margin-bottom: 14px;
}

.filter-card .card-header{
  display:flex;
  align-items:center;
  gap:8px;
  margin-bottom:10px;
}
.card-title{ font-weight:700; }
.collapse-icon{ font-size: 12px; opacity: 0.7; }

.filter-wrapper{
  display:flex;
  gap: 16px;
  align-items: stretch;
}

.filter-left{
  flex: 1;
  display:grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 12px;
  align-items:end;
}

.f-search{ grid-column: 1 / 2; }
.f-email { grid-column: 2 / 3; }
.f-phone { grid-column: 3 / 4; }

.f-status{ grid-column: 1 / 2; }
.f-code  { grid-column: 2 / 3; }
.f-acc   { grid-column: 3 / 4; }

.filter-right{
  width: 140px;
  display:flex;
  flex-direction: column;
  gap: 10px;
  justify-content: center;
}

.form-group label{
  display:block;
  margin-bottom:6px;
  font-weight:600;
}
.form-input{
  width:100%;
  padding:9px 10px;
  border-radius:6px;
  border:1px solid #d0d7de;
  outline:none;
  font-size:15px;
  background:#fff;
  height: 38px;
}

.radio-row{
  display:flex;
  gap: 14px;
  flex-wrap: wrap;
  margin-top: 6px;
}
.radio-item{
  display:flex;
  gap: 6px;
  align-items:center;
  font-size: 15px;
}

.btn{
  height: 38px;
  padding: 0 12px;
  border-radius: 8px;
  cursor: pointer;
  border: 1px solid #d0d7de;
  background: #fff;
  font-size: 14px;
  display:inline-flex;
  align-items:center;
  justify-content:center;
  gap:8px;
  width: 100%;
}
.btn-primary{
  border-color:#1d4ed8;
  background:#fff;
}
.btn-outline{ background:#fff; }
.btn-ghost{ background:#fff; }
.icon{ font-size: 16px; line-height: 1; }

@media (max-width: 1100px){
  .filter-wrapper{ flex-direction: column; }
  .filter-right{ width: 100%; flex-direction: row; justify-content: flex-end; }
  .btn{ width: auto; }
  .filter-left{ grid-template-columns: 1fr 1fr; }
  .f-email{ grid-column: 1 / 2; }
  .f-phone{ grid-column: 2 / 3; }
  .f-status{ grid-column: 1 / 3; }
  .f-code{ grid-column: 1 / 2; }
  .f-acc{ grid-column: 2 / 3; }
}

/* TABLE */
.table-header{
  display:flex;
  justify-content:space-between;
  align-items:center;
  margin-bottom:10px;
}
.total{ font-weight:700; }

.table-wrap{ overflow:auto; }

.customer-table{
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}
.customer-table thead th{
  background:#1f2a3a;
  color:#fff;
  font-weight:700;
  padding:10px 8px;
  text-align:left;
  font-size: 14px;
}
.customer-table td{
  border-top:1px solid #eef2f7;
  padding:10px 8px;
  vertical-align: middle;
  color:#374151;
  text-align:left;
}

.customer-table thead th.col-gender,
.customer-table thead th.col-status,
.customer-table thead th.col-action {
  text-align: center;
}

.center{ text-align:center !important; }

.ellipsis{
  white-space:nowrap;
  overflow:hidden;
  text-overflow:ellipsis;
}

.col-stt{ width: 50px; }
.col-avatar{ width: 70px; }
.col-code{ width: 100px; }
.col-name{ width: 220px; }
.col-gender{ width: 110px; }
.col-email{ width: 240px; }
.col-phone{ width: 120px; }
.col-address{ width: 420px; }
.col-status{ width: 140px; }
.col-action{ width: 170px; }

.avatar-cell{
  display:flex;
  align-items:center;
  justify-content:flex-start;
}
.avatar-img{
  width: 40px;
  height: 40px;
  border-radius: 999px;
  object-fit: cover;
  border: 1px solid #e5e7eb;
}
.avatar-fallback{
  width: 34px;
  height: 34px;
  border-radius: 999px;
  background:#eef2ff;
  border: 1px solid #e5e7eb;
  display:flex;
  align-items:center;
  justify-content:center;
  font-weight:700;
  color:#1d4ed8;
  font-size: 12px;
}

.badge{
  display:inline-flex;
  align-items:center;
  justify-content:center;
  padding: 4px 10px;
  border-radius: 999px;
  font-weight: 700;
  font-size: 12px;
}
.badge-role{ background:#eef2ff; color:#1d4ed8; }
.badge-active{ background:#dbeafe; color:#1d4ed8; }     /* xanh */
.badge-inactive{ background:#e5e7eb; color:#6b7280; }   /* xám */

.action-row{
  display:flex;
  align-items:center;
  justify-content:center;
  gap:8px;
}

/* Switch */
.switch{
  position:relative;
  display:inline-block;
  width:42px;
  height:22px;
}
.switch input{ opacity:0; width:0; height:0; }
.slider{
  position:absolute;
  cursor:pointer;
  top:0; left:0; right:0; bottom:0;
  background:#cbd5e1;
  transition:.2s;
  border-radius:999px;
}
.slider:before{
  position:absolute;
  content:"";
  height:18px; width:18px;
  left:2px; top:2px;
  background:white;
  transition:.2s;
  border-radius:999px;
}
.switch input:checked + .slider{ background:#1d4ed8; }
.switch input:checked + .slider:before{ transform: translateX(20px); }

.icon-btn{
  border:1px solid #d0d7de;
  background:#fff;
  border-radius:6px;
  width:34px;
  height:30px;
  cursor:pointer;
  display:flex;
  align-items:center;
  justify-content:center;
}
.icon-edit{
  background:#1f3a8a;
  color:#fff;
  border-color:#1f3a8a;
}

.empty{
  text-align:center !important;
  padding: 16px;
  color:#6b7280;
}

/* Pagination */
.pagination{
  display:flex;
  align-items:center;
  justify-content:center;
  gap:6px;
  margin-top:12px;
}
.p-btn{
  min-width:32px;
  height:30px;
  border-radius:6px;
  border:1px solid #d0d7de;
  background:#fff;
  cursor:pointer;
}
.p-btn.active{
  background:#1f2a3a;
  color:#fff;
  border-color:#1f2a3a;
}
.p-btn:disabled{
  opacity:0.5;
  cursor:not-allowed;
}
.page-text{ margin-left:10px; opacity:0.7; }

/* MODAL */
.modal-overlay{
  position:fixed;
  inset:0;
  background:rgba(0,0,0,0.35);
  display:flex;
  align-items:center;
  justify-content:center;
  z-index:50;
}
.modal{
  width:min(920px, 95vw);
  background:#fff;
  border-radius:12px;
  border:1px solid #e5e7eb;
  overflow:hidden;
  font-size: 15px;
}
.modal-header{
  display:flex;
  align-items:center;
  justify-content:space-between;
  padding:14px 16px;
  border-bottom:1px solid #eef2f7;
}
.modal-title{ font-weight:800; font-size: 18px; }
.modal-close{
  border:none;
  background:transparent;
  cursor:pointer;
  font-size:22px;
}
.modal-body{ padding:16px; }
.modal-footer{
  padding:14px 16px;
  border-top:1px solid #eef2f7;
  display:flex;
  justify-content:flex-end;
}

.detail-top{
  display:flex;
  gap:14px;
  align-items:center;
  margin-bottom:14px;
}
.detail-avatar{
  width:64px;
  height:64px;
  border-radius:999px;
  overflow:hidden;
  border:1px solid #e5e7eb;
  display:flex;
  align-items:center;
  justify-content:center;
  background:#eef2ff;
}
.detail-avatar-img{ width:100%; height:100%; object-fit:cover; }
.detail-avatar-fallback{ font-weight:800; color:#1d4ed8; }
.detail-name{ font-size:20px; font-weight:800; margin-bottom:6px; }
.detail-badges{ display:flex; gap:8px; flex-wrap:wrap; }

.detail-grid{
  display:grid;
  grid-template-columns:1fr 1fr;
  gap:12px;
}
.detail-box{
  border:1px solid #e5e7eb;
  border-radius:10px;
  padding:12px;
}
.detail-label{ font-weight:600; opacity:0.7; margin-bottom:6px; }
.detail-value{ font-weight:400; color:#111827; }
.col-full{ grid-column: span 2; }

@media (max-width: 860px){
  .detail-grid{ grid-template-columns:1fr; }
  .col-full{ grid-column: span 1; }
}
</style>
