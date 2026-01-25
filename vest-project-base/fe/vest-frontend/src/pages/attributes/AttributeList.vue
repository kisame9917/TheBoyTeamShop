<template>
  <div class="attribute-page">
    <!-- Header -->
    <div class="header-section">
      <div class="header-left">
        <h2>Quản lý Sản phẩm / {{ title }}</h2>
      </div>
    </div>

    <!-- Filter / Actions -->
    <div class="card filter-card">
      <div class="filter-row">
        <div class="form-group search-group">
          <label>Tìm kiếm</label>
          <input
            v-model="searchQuery"
            class="form-input"
            :placeholder="`Tìm kiếm ${title.toLowerCase()}...`"
          />
        </div>

        <div class="action-group">
          <button class="btn btn-secondary" type="button" @click="exportExcel">Xuất Excel</button>
          <button class="btn btn-primary" type="button" @click="openModal('create')">
            + Thêm {{ title.toLowerCase() }}
          </button>
        </div>
      </div>
    </div>

    <!-- Table -->
    <div class="card table-card">
      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr>
              <th class="text-center" style="width: 80px;">STT</th>
              <th class="text-center" style="width: 160px;">Mã</th>
              <th>{{ title }}</th>
              <th class="text-center" style="width: 160px;">Trạng thái</th>
              <th class="text-center" style="width: 180px;">Hành động</th>
            </tr>
          </thead>

          <tbody>
            <tr v-if="loading">
              <td colspan="5" class="text-center">Đang tải dữ liệu...</td>
            </tr>

            <tr v-else-if="pagedItems.length === 0">
              <td colspan="5" class="text-center">Không có dữ liệu</td>
            </tr>

            <tr v-else v-for="(item, index) in pagedItems" :key="item.id">
              <td class="text-center">{{ startIndex + index + 1 }}</td>
              <td class="text-center">{{ item.ma }}</td>
              <td>{{ displayName(item) }}</td>
              <td class="text-center">
                <span :class="['badge', item.trangThai ? 'badge-success' : 'badge-danger']">
                  {{ item.trangThai ? 'Hoạt động' : 'Ngừng hoạt động' }}
                </span>
              </td>
              <td class="text-center">
                <div class="action-cell">
                  <button class="btn-icon blue" type="button" @click="openModal('edit', item)" title="Sửa">
                    ✏️
                  </button>

                  <label class="switch" title="Đổi trạng thái">
                    <input
                      type="checkbox"
                      :checked="!!item.trangThai"
                      :disabled="togglingIds.has(item.id)"
                      @change="toggleStatus(item)"
                    />
                    <span class="slider"></span>
                  </label>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination (giống style trang sản phẩm/biến thể) -->
      <div class="pager" v-if="!loading && filteredItems.length > 0">
        <div class="pager-left">
          Hiển thị {{ pagedItems.length }} / tổng {{ filteredItems.length }} bản ghi
        </div>

        <div class="pager-center">
          <button class="page-btn" :disabled="currentPage === 1" @click="goPage(currentPage - 1)">
            ‹
          </button>
          <span class="page-label">Trang</span>
          <input
            class="page-input"
            type="number"
            min="1"
            :max="totalPages"
            v-model.number="pageInput"
            @keyup.enter="applyPageInput"
            @blur="applyPageInput"
          />
          <span class="page-total">/ {{ totalPages }}</span>
          <button class="page-btn" :disabled="currentPage === totalPages" @click="goPage(currentPage + 1)">
            ›
          </button>
        </div>

        <div class="pager-right">
          <select class="page-size" v-model.number="pageSize">
            <option :value="5">5 bản ghi / trang</option>
            <option :value="10">10 bản ghi / trang</option>
            <option :value="20">20 bản ghi / trang</option>
            <option :value="50">50 bản ghi / trang</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modals">
        <div class="modal-header">
          <h3>{{ modalMode === 'create' ? 'Thêm' : 'Cập nhật' }} {{ title }}</h3>
          <button class="close-btn" type="button" @click="closeModal">×</button>
        </div>

        <form @submit.prevent="submitForm" class="modal-body">
          <div class="form-group">
            <label class="required">{{ type === 'kich-co' ? 'Kích cỡ' : 'Tên' }}</label>
            <input
              v-model="form.ten"
              class="form-input"
              :type="type === 'kich-co' ? 'number' : 'text'"
              :min="type === 'kich-co' ? 0 : undefined"
              required
            />
          </div>

          <div class="modal-actions">
            <button class="btn btn-secondary" type="button" @click="closeModal">Hủy</button>
            <button class="btn btn-primary" type="submit">Lưu</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import attributeService from '../../services/attributeService'
import { useToast } from '../../composables/useToast'

const { success, error } = useToast()
const route = useRoute()

const type = computed(() => String(route.params.type || ''))
const TITLES = {
  'thuong-hieu': 'Thương hiệu',
  'chat-lieu': 'Chất liệu',
  'kich-co': 'Kích cỡ',
  'mau-sac': 'Màu sắc',
  'loai-san-pham': 'Loại sản phẩm',
  'so-khuy': 'Số khuy',
  'kieu-tui': 'Kiểu túi',
  've-ao': 'Ve áo',
  'xe-ta': 'Xẻ tà',
  'xuat-xu': 'Xuất xứ',
  'fit': 'Fit'
}
const title = computed(() => TITLES[type.value] || 'Thuộc tính')

const items = ref([])
const loading = ref(false)
const searchQuery = ref('')

/* ===== Pagination ===== */
const currentPage = ref(1)
const pageSize = ref(10)
const pageInput = ref(1)

const filteredItems = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return items.value
  return items.value.filter((i) => {
    const name = String(displayName(i) || '').toLowerCase()
    const code = String(i.ma || '').toLowerCase()
    return name.includes(q) || code.includes(q)
  })
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredItems.value.length / pageSize.value)))
const startIndex = computed(() => (currentPage.value - 1) * pageSize.value)

const pagedItems = computed(() => {
  const start = startIndex.value
  return filteredItems.value.slice(start, start + pageSize.value)
})

function goPage(p) {
  const next = Math.min(Math.max(1, p), totalPages.value)
  currentPage.value = next
  pageInput.value = next
}

function applyPageInput() {
  if (!pageInput.value || Number.isNaN(pageInput.value)) pageInput.value = currentPage.value
  goPage(pageInput.value)
}

watch([searchQuery, pageSize, type], () => {
  currentPage.value = 1
  pageInput.value = 1
})

/* ===== Modal ===== */
const showModal = ref(false)
const modalMode = ref('create') // create | edit
const form = reactive({ id: null, ma: '', ten: '', trangThai: true })

function openModal(mode, item = null) {
  modalMode.value = mode
  if (mode === 'edit' && item) {
    form.id = item.id
    form.ma = item.ma
    form.trangThai = !!item.trangThai
    form.ten = String(displayName(item) ?? '')
  } else {
    form.id = null
    form.ma = ''
    form.ten = ''
    form.trangThai = true
  }
  showModal.value = true
}
function closeModal() {
  showModal.value = false
}

/* ===== Helpers ===== */
function displayName(item) {
  if (!item) return ''
  // API size thường là soSize
  if (type.value === 'kich-co') return item.ten ?? item.soSize ?? ''
  return item.ten ?? ''
}

function removeVietnameseTones(str) {
  return String(str || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/đ/g, 'd')
    .replace(/Đ/g, 'D')
}

function genCodeFromName(name) {
  let code = removeVietnameseTones(name).trim()
  code = code.replace(/\s+/g, '_').toUpperCase()
  code = code.replace(/[^A-Z0-9_]/g, '')
  // unique
  let unique = code || 'CODE'
  let c = 1
  while (items.value.some((i) => i.ma === unique && i.id !== form.id)) {
    unique = `${code}_${c++}`
  }
  return unique
}

/* ===== CRUD ===== */
async function fetchData() {
  loading.value = true
  try {
    const res = await attributeService.getAllList(type.value)
    // chuẩn hoá để hiển thị: kích cỡ dùng soSize => ten
    items.value = (res.data || []).map((it) => {
      if (type.value === 'kich-co') return { ...it, ten: it.ten ?? it.soSize }
      return it
    })
  } catch (e) {
    console.error(e)
    error('Không thể tải dữ liệu')
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
watch(type, fetchData)

/* ===== Submit ===== */
async function submitForm() {
  const name = String(form.ten ?? '').trim()
  if (!name) return

  // check trùng tên
  const dup = items.value.find(
    (i) => String(displayName(i)).trim().toLowerCase() === name.toLowerCase() && i.id !== form.id
  )
  if (dup) return error(`Đã tồn tại "${name}"`)

  const payload = {
    id: form.id,
    ten: type.value === 'kich-co' ? undefined : name,
    soSize: type.value === 'kich-co' ? name : undefined,
    ma: form.ma || genCodeFromName(name),
    trangThai: form.trangThai
  }

  try {
    if (modalMode.value === 'create') {
      await attributeService.create(type.value, payload)
      success(`Thêm ${title.value} thành công`)
    } else {
      await attributeService.update(type.value, form.id, payload)
      success(`Cập nhật ${title.value} thành công`)
    }
    closeModal()
    await fetchData()
  } catch (e) {
    console.error(e)
    const msg = e?.response?.data?.message || e?.message || 'Có lỗi xảy ra'
    error(msg)
  }
}

/* ===== Toggle status ===== */
const togglingIds = ref(new Set())

async function toggleStatus(item) {
  if (!item?.id) return
  if (togglingIds.value.has(item.id)) return
  togglingIds.value.add(item.id)

  const old = !!item.trangThai
  const next = !old
  item.trangThai = next

  try {
    const payload = {
      ...item,
      trangThai: next,
      ten: type.value === 'kich-co' ? undefined : displayName(item),
      soSize: type.value === 'kich-co' ? displayName(item) : undefined
    }
    await attributeService.update(type.value, item.id, payload)
    await fetchData()
    success(`Đã đổi trạng thái "${displayName(item)}" thành ${next ? 'Hoạt động' : 'Ngừng hoạt động'}`)
  } catch (e) {
    console.error(e)
    item.trangThai = old
    const msg = e?.response?.data?.message || e?.message || 'Không thể cập nhật trạng thái'
    error(msg)
  } finally {
    togglingIds.value.delete(item.id)
  }
}

/* ===== Excel stub ===== */
function exportExcel() {
  // TODO: nếu bạn có API export thì gọi ở đây
  success('Chức năng xuất Excel sẽ bổ sung sau')
}
</script>

<style scoped>
.attribute-page {
  padding: 20px;
  background: #f3f4f6;
  min-height: 100vh;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.header-section h2 {
  margin: 0;
  font-size: 1.35rem;
  font-weight: 700;
  color: #111827;
}

.card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}

.filter-row {
  display: flex;
  align-items: flex-end;
  gap: 14px;
  flex-wrap: wrap;
}
.search-group {
  flex: 1;
  min-width: 260px;
}
.action-group {
  display: flex;
  gap: 10px;
}

.form-group {
  display: flex;
  flex-direction: column;
}
.form-group label {
  font-size: 0.875rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 6px;
}
.required::after { content: " *"; color: #ef4444; }

.form-input {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
  width: 100%;
  box-sizing: border-box;
}

.btn {
  padding: 8px 14px;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  font-size: 0.875rem;
}
.btn-secondary { background: #e5e7eb; color: #374151; }
.btn-secondary:hover { background: #d1d5db; }
.btn-primary { background: #1e40af; color: #fff; }
.btn-primary:hover { background: #1e3a8a; }

.table-responsive { overflow-x: auto; }
.table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
}
.table th {
  background: #1e293b;
  color: #fff;
  padding: 12px;
  text-align: left;
  font-weight: 600;
  border-bottom: 1px solid #e5e7eb;
}
.table td {
  padding: 12px;
  border-bottom: 1px solid #f3f4f6;
  vertical-align: middle;
  color: #374151;
}
.text-center { text-align: center; }

/* badge */
.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}
.badge-success { background: #d1fae5; color: #065f46; }
.badge-danger { background: #fee2e2; color: #991b1b; }

/* action cell (không lệch) */
.action-cell {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

/* icon button */
.btn-icon {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.btn-icon.blue { background: #3b82f6; }

/* switch */
.switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 22px;
}
.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}
.slider {
  position: absolute;
  inset: 0;
  cursor: pointer;
  background: #d1d5db;
  transition: 0.2s;
  border-radius: 999px;
}
.slider:before {
  content: "";
  position: absolute;
  height: 18px;
  width: 18px;
  left: 2px;
  top: 2px;
  background: #fff;
  transition: 0.2s;
  border-radius: 50%;
  box-shadow: 0 1px 2px rgba(0,0,0,0.15);
}
.switch input:checked + .slider { background: #2563eb; }
.switch input:checked + .slider:before { transform: translateX(22px); }

/* pager */
.pager {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid #eef2f7;
}
.pager-left { font-size: 12px; color: #6b7280; }
.pager-center {
  display: flex;
  align-items: center;
  gap: 8px;
}
.page-btn {
  width: 28px;
  height: 28px;
  border-radius: 4px;
  border: 1px solid #d1d5db;
  background: #fff;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.page-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.page-label { font-size: 12px; color: #6b7280; }
.page-input {
  width: 56px;
  height: 28px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  padding: 0 8px;
  font-size: 12px;
}
.page-total { font-size: 12px; color: #6b7280; }
.pager-right .page-size {
  height: 30px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 0 10px;
  background: #fff;
  font-size: 12px;
}

/* modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
.modals {
  width: 420px;
  max-width: calc(100vw - 24px);
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 15px 40px rgba(0,0,0,0.2);
  overflow: hidden;
}
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #eef2f7;
}
.modal-header h3 {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
  color: #111827;
}
.close-btn {
  border: none;
  background: transparent;
  font-size: 22px;
  cursor: pointer;
  line-height: 1;
  color: #6b7280;
}
.modal-body { padding: 16px; }
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 14px;
}
</style>
