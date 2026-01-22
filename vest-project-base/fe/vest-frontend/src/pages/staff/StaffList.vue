<template>
  <div class="staff-page">
    <div class="page-title">
      <h2>Quản lý nhân viên / Danh sách nhân viên</h2>
    </div>

    <!-- FILTER -->
    <div class="card filter-card">
      <div class="card-header">
        <span class="collapse-icon">▼</span>
        <span class="card-title">Bộ lọc tìm kiếm</span>
      </div>

      <div class="filter-wrapper">
        <!-- LEFT -->
        <div class="filter-left">
          <div class="form-group f-search">
            <label>Tìm kiếm</label>
            <input
                class="form-input"
                v-model="filters.keyword"
                placeholder="Tìm theo mã, tên, email, SĐT..."
            />
          </div>

          <div class="form-group f-role">
            <label>Chức vụ</label>
            <select class="form-input" v-model="filters.role">
              <option value="">Tất cả</option>
              <option value="ADMIN">ADMIN</option>
              <option value="NHAN_VIEN">NHÂN VIÊN</option>
            </select>
          </div>

          <div class="form-group f-email">
            <label>Email</label>
            <input class="form-input" v-model="filters.email" placeholder="Email" />
          </div>

          <div class="form-group f-status">
            <label>Trạng thái</label>
            <div class="radio-row">
              <label class="radio-item">
                <input type="radio" v-model="filters.status" :value="1" /> Đang làm
              </label>
              <label class="radio-item">
                <input type="radio" v-model="filters.status" :value="0" /> Đã nghỉ
              </label>
              <label class="radio-item">
                <input type="radio" v-model="filters.status" :value="-1" /> Tất cả
              </label>
            </div>
          </div>

          <div class="form-group f-phone">
            <label>SĐT</label>
            <input class="form-input" v-model="filters.phone" placeholder="SĐT" />
          </div>
        </div>

        <!-- RIGHT -->
        <div class="filter-right">
          <button class="btn btn-primary" type="button" @click="goCreate" :disabled="!isAdmin">
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
        <div class="total">Tổng số nhân viên {{ filteredList.length }}</div>
      </div>

      <div class="table-wrap">
        <table class="staff-table">
          <thead>
          <tr>
            <th class="col-stt">STT</th>
            <th class="col-avatar">Ảnh</th>
            <th class="col-code">Mã NV</th>
            <th class="col-name">Họ tên</th>
            <th class="col-email">Email</th>
            <th class="col-phone">SĐT</th>
            <th class="col-address">Địa chỉ</th>
            <th class="col-role">Chức vụ</th>
            <th class="col-status">Trạng thái</th>
            <th class="col-action">Hành động</th>
          </tr>
          </thead>

          <tbody>
          <tr v-for="(s, idx) in pagedList" :key="s.id">
            <td class="ellipsis">{{ pageIndexStart + idx + 1 }}</td>

            <td class="ellipsis">
              <div class="avatar-cell">
                <img
                    v-if="resolveAvatar(s)"
                    :src="resolveAvatar(s)"
                    class="avatar-img"
                    alt="avatar"
                    @error="onAvatarError($event, s)"
                />
                <div v-else class="avatar-fallback">{{ getInitials(s.tenNhanVien) }}</div>
              </div>
            </td>

            <td>{{ s.maNhanVien }}</td>
            <td>{{ s.tenNhanVien }}</td>
            <td class="ellipsis" :title="s.email || ''">{{ s.email || '-' }}</td>
            <td>{{ s.soDienThoai || '-' }}</td>
            <td class="ellipsis" :title="s.diaChi || ''">{{ s.diaChi || '-' }}</td>

            <td class="center">
              <span class="badge badge-role">{{ getRoleText(s) }}</span>
            </td>

            <td class="center">
                <span :class="['badge', s.trangThai ? 'badge-working' : 'badge-off']">
                  {{ s.trangThai ? 'Đang làm' : 'Đã nghỉ' }}
                </span>
            </td>

            <td class="center">
              <div class="action-row">
                <label class="switch" :class="{ 'switch-disabled': !isAdmin }" title="Đổi trạng thái">
                  <input
                      type="checkbox"
                      :checked="!!s.trangThai"
                      :disabled="!isAdmin"
                      @click.prevent="confirmToggleStatus(s)"
                  />
                  <span class="slider"></span>
                </label>

                <button class="icon-btn" type="button" title="Xem chi tiết" @click="openDetail(s)">
                  👁
                </button>

                <button
                    class="icon-btn icon-edit"
                    type="button"
                    title="Sửa"
                    @click="goEdit(s.id)"
                    :disabled="!isAdmin"
                >
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
          <div class="modal-title">Chi tiết nhân viên</div>
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
                {{ getInitials(detailData && detailData.tenNhanVien) }}
              </div>
            </div>

            <div class="detail-head">
              <div class="detail-name">{{ (detailData && detailData.tenNhanVien) || '-' }}</div>
              <div class="detail-badges">
                <span class="badge badge-role">{{ (detailData && detailData.maNhanVien) || '-' }}</span>
                <span class="badge badge-role">{{ getRoleText(detailData) }}</span>
                <span :class="['badge', detailData && detailData.trangThai ? 'badge-working' : 'badge-off']">
                  {{ detailData && detailData.trangThai ? 'Đang làm' : 'Đã nghỉ' }}
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
              <div class="detail-label">CCCD</div>
              <div class="detail-value">{{ (detailData && detailData.cccd) || '-' }}</div>
            </div>

            <div class="detail-box">
              <div class="detail-label">Ngày sinh</div>
              <div class="detail-value">{{ formatDate(detailData && detailData.ngaySinh) }}</div>
            </div>

            <div class="detail-box">
              <div class="detail-label">Giới tính</div>
              <div class="detail-value">{{ mapGender(detailData && detailData.gioiTinh) }}</div>
            </div>

            <div class="detail-box">
              <div class="detail-label">Tài khoản</div>
              <div class="detail-value">{{ (detailData && detailData.taiKhoan) || '-' }}</div>
            </div>

            <div class="detail-box col-full">
              <div class="detail-label">Địa chỉ</div>
              <div class="detail-value">{{ (detailData && detailData.diaChi) || '-' }}</div>
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
import { useAuthStore } from '../../stores/auth'


const auth = useAuthStore()
const isAdmin = computed(() => !!auth.isAdmin)
const router = useRouter()

const list = ref([])
const page = ref(1)
const pageSize = 10

const filters = reactive({
  keyword: '',
  role: '',
  email: '',
  phone: '',
  status: -1 // -1 = tất cả
})

watch(
    () => [filters.keyword, filters.role, filters.email, filters.phone, filters.status],
    () => {
      page.value = 1
    }
)

function unwrapList(data) {
  if (!data) return []
  if (Array.isArray(data)) return data
  if (data && Array.isArray(data.result)) return data.result
  if (data && Array.isArray(data.content)) return data.content
  return []
}

function normalizeStaff(x) {
  x = x || {}
  const quyenHan = x.quyenHan || {}
  return {
    id: x.id,
    maNhanVien: x.maNhanVien != null ? x.maNhanVien : '',
    tenNhanVien: x.tenNhanVien != null ? x.tenNhanVien : '',
    soDienThoai: x.soDienThoai != null ? x.soDienThoai : '',
    cccd: x.cccd != null ? x.cccd : '',
    email: x.email != null ? x.email : '',
    taiKhoan: x.taiKhoan != null ? x.taiKhoan : '',
    ngaySinh: x.ngaySinh != null ? x.ngaySinh : null,
    gioiTinh: x.gioiTinh != null ? x.gioiTinh : null,
    diaChi: x.diaChi != null ? x.diaChi : '',
    trangThai: x.trangThai != null ? x.trangThai : true,
    tenQuyenHan:
        x.tenQuyenHan != null ? x.tenQuyenHan : (quyenHan.tenQuyenHan != null ? quyenHan.tenQuyenHan : ''),
    quyenHanId: x.quyenHanId != null ? x.quyenHanId : (quyenHan.id != null ? quyenHan.id : null),
    anhDaiDien: x.anhDaiDien != null ? x.anhDaiDien : (x.anh_dai_dien != null ? x.anh_dai_dien : '')
  }
}

function safeStr(v) {
  return String(v == null ? '' : v).toLowerCase().trim()
}

/** ✅ ROLE HELPERS (CHỈ 1 BỘ - KHÔNG BỊ TRÙNG) */
function getRoleCode(s) {
  if (Number(s?.quyenHanId) === 1) return 'ADMIN'
  if (Number(s?.quyenHanId) === 2) return 'NHAN_VIEN'

  const roleName = String(s?.tenQuyenHan || '').toUpperCase()
  if (roleName.includes('ADMIN')) return 'ADMIN'
  return 'NHAN_VIEN'
}

function getRoleText(s) {
  return getRoleCode(s) === 'ADMIN' ? 'ADMIN' : 'NHÂN VIÊN'
}

function mapGender(v) {
  if (v === true) return 'Nam'
  if (v === false) return 'Nữ'
  return 'Khác'
}

function formatDate(d) {
  if (!d) return '-'
  return String(d).slice(0, 10)
}

function getInitials(name) {
  const s = String(name || '').trim()
  if (!s) return 'NV'
  const parts = s.split(/\s+/).filter(Boolean)
  const a = parts[0] ? parts[0][0] : 'N'
  const b = parts[parts.length - 1] ? parts[parts.length - 1][0] : 'V'
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

function resolveAvatar(s) {
  s = s || {}
  const url = String(s.anhDaiDien || '').trim()
  if (!url) return ''
  return resolveFileUrl(url)
}

function onAvatarError(e, s) {
  if (s) s.anhDaiDien = ''
  if (e && e.target) e.target.src = ''
}

/** ===== Data ===== */
async function fetchList() {
  const res = await http.get('/api/nhan-vien')
  list.value = unwrapList(res.data).map(normalizeStaff)
}

const filteredList = computed(() => {
  const kw = safeStr(filters.keyword)
  const email = safeStr(filters.email)
  const phone = safeStr(filters.phone)

  return (list.value || []).filter((s) => {
    // status
    if (filters.status !== -1) {
      if (filters.status === 1 && !s.trangThai) return false
      if (filters.status === 0 && s.trangThai) return false
    }

    // role
    if (filters.role) {
      const rl = getRoleCode(s) // ADMIN | NHAN_VIEN
      if (rl !== String(filters.role)) return false
    }

    if (email && !safeStr(s.email).includes(email)) return false
    if (phone && !safeStr(s.soDienThoai).includes(phone)) return false

    if (kw) {
      const blob =
          (String(s.maNhanVien || '') +
              ' ' +
              String(s.tenNhanVien || '') +
              ' ' +
              String(s.email || '') +
              ' ' +
              String(s.soDienThoai || '')).toLowerCase()
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
  filters.role = ''
  filters.email = ''
  filters.phone = ''
  filters.status = -1
  page.value = 1
}

function goCreate() {
  if (!isAdmin.value) return alert('Chỉ ADMIN mới được thêm mới.')
  router.push({ name: 'staff-new' })
}

function goEdit(id) {
  if (!isAdmin.value) return alert('Chỉ ADMIN mới được sửa.')
  router.push({ name: 'staff-edit', params: { id } })
}

async function confirmToggleStatus(s) {
  if (!isAdmin.value) return alert('Chỉ ADMIN mới được đổi trạng thái.')

  const next = !s.trangThai
  const txt = next ? 'chuyển sang Đang làm' : 'chuyển sang Đã nghỉ'
  if (!confirm('Bạn có chắc chắn ' + txt + ' không ?')) return

  await http.patch('/api/nhan-vien/' + s.id + '/trang-thai', { trangThai: next })
  s.trangThai = next

  // để không “mất dòng” do filter
  filters.status = -1
  page.value = 1
}

/** ===== Detail modal ===== */
const detailOpen = ref(false)
const detailData = ref(null)

function openDetail(s) {
  detailData.value = Object.assign({}, s)
  detailOpen.value = true
}

onMounted(fetchList)
</script>

<style scoped>
.staff-page { font-size: 15px; }

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

/* layout filter như khoanh đỏ */
.filter-left{
  flex: 1;
  display:grid;
  grid-template-columns: 2.6fr 1.2fr 1.2fr;
  gap: 12px;
  align-items:end;
}
.f-search{ grid-column: 1 / 2; }
.f-role  { grid-column: 2 / 3; }
.f-email { grid-column: 3 / 4; }

.f-status{ grid-column: 1 / 2; }
.f-phone { grid-column: 2 / 3; }

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
  .f-email{ grid-column: 1 / 3; }
  .f-status{ grid-column: 1 / 3; }
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

.staff-table{
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}
.staff-table thead th{
  background:#1f2a3a;
  color:#fff;
  font-weight:700;
  padding:10px 8px;
  text-align:left;
  font-size: 14px;
}
.staff-table td{
  border-top:1px solid #eef2f7;
  padding:10px 8px;
  vertical-align: middle;
  color:#374151;
  text-align:left;
}

.staff-table thead th.col-role,
.staff-table thead th.col-status,
.staff-table thead th.col-action {
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
.col-code{ width: 90px; }
.col-name{ width: 200px; }
.col-email{ width: 220px; }
.col-phone{ width: 120px; }
.col-address{ width: 420px; }
.col-role{ width: 120px; }
.col-status{ width: 120px; }
.col-action{ width: 160px; }

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
.badge-working{ background:#dbeafe; color:#1d4ed8; }
.badge-off{ background:#e5e7eb; color:#6b7280; }

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

.btn:disabled,
.icon-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.switch-disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.switch input:disabled + .slider {
  cursor: not-allowed;
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
  width:84px;
  height:84px;
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
