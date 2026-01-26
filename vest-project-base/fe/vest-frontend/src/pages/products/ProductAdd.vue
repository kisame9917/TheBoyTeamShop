<template>
  <div class="card">
    <div class="card-header flex-between">
      <h2 class="title">{{ isEditMode ? 'Cập nhật sản phẩm' : 'Thêm sản phẩm mới' }}</h2>
      <button class="btn btn-secondary" @click="goBack" type="button">← Quay lại danh sách</button>
    </div>

    <div class="card-body">
      <!-- Section 1: Basic Info -->
      <div class="section">
        <h3 class="section-title">Thông tin cơ bản</h3>

        <div class="grid-3">
          <div class="form-group">
            <label class="required">Tên sản phẩm</label>
            <input
              type="text"
              v-model="product.tenSanPham"
              class="form-input"
              placeholder="Nhập tên sản phẩm"
              :class="{ 'error-border': errors.tenSanPham }"
            />
            <small v-if="errors.tenSanPham" class="error-text">{{ errors.tenSanPham }}</small>
          </div>

          <div class="form-group">
            <label class="required">Loại sản phẩm</label>
            <div class="flex-row-gap">
              <select v-model="product.loaiSanPhamId" class="form-input" :class="{ 'error-border': errors.loaiSanPhamId }">
                <option value="">-- Chọn Loại sản phẩm --</option>
                <option v-for="item in attributes.loaiSanPham" :key="item.id" :value="item.id">{{ item.ten }}</option>
              </select>
              <button class="btn-quick-add" @click="quickAdd('loaiSanPham', 'loai-san-pham')" type="button">+</button>
            </div>
            <small v-if="errors.loaiSanPhamId" class="error-text">{{ errors.loaiSanPhamId }}</small>
          </div>

          <div class="form-group">
            <label class="required">Thương hiệu</label>
            <div class="flex-row-gap">
              <select v-model="product.thuongHieuId" class="form-input" :class="{ 'error-border': errors.thuongHieuId }">
                <option value="">-- Chọn Thương hiệu --</option>
                <option v-for="item in attributes.thuongHieu" :key="item.id" :value="item.id">{{ item.ten }}</option>
              </select>
              <button class="btn-quick-add" @click="quickAdd('thuongHieu', 'thuong-hieu')" type="button">+</button>
            </div>
            <small v-if="errors.thuongHieuId" class="error-text">{{ errors.thuongHieuId }}</small>
          </div>

          <div class="form-group">
            <label class="required">Số khuy</label>
            <div class="flex-row-gap">
              <select v-model="product.soKhuyId" class="form-input" :class="{ 'error-border': errors.soKhuyId }">
                <option value="">-- Chọn Số khuy --</option>
                <option v-for="item in attributes.soKhuy" :key="item.id" :value="item.id">{{ item.ten }}</option>
              </select>
              <button class="btn-quick-add" @click="quickAdd('soKhuy', 'so-khuy')" type="button">+</button>
            </div>
            <small v-if="errors.soKhuyId" class="error-text">{{ errors.soKhuyId }}</small>
          </div>

          <div class="form-group">
            <label class="required">Kiểu túi</label>
            <div class="flex-row-gap">
              <select v-model="product.kieuTuiId" class="form-input" :class="{ 'error-border': errors.kieuTuiId }">
                <option value="">-- Chọn Kiểu túi --</option>
                <option v-for="item in attributes.kieuTui" :key="item.id" :value="item.id">{{ item.ten }}</option>
              </select>
              <button class="btn-quick-add" @click="quickAdd('kieuTui', 'kieu-tui')" type="button">+</button>
            </div>
            <small v-if="errors.kieuTuiId" class="error-text">{{ errors.kieuTuiId }}</small>
          </div>

          <div class="form-group">
            <label class="required">Ve áo</label>
            <div class="flex-row-gap">
              <select v-model="product.veAoId" class="form-input" :class="{ 'error-border': errors.veAoId }">
                <option value="">-- Chọn Ve áo --</option>
                <option v-for="item in attributes.veAo" :key="item.id" :value="item.id">{{ item.ten }}</option>
              </select>
              <button class="btn-quick-add" @click="quickAdd('veAo', 've-ao')" type="button">+</button>
            </div>
            <small v-if="errors.veAoId" class="error-text">{{ errors.veAoId }}</small>
          </div>

          <div class="form-group">
            <label class="required">Xẻ tà</label>
            <div class="flex-row-gap">
              <select v-model="product.xeTaId" class="form-input" :class="{ 'error-border': errors.xeTaId }">
                <option value="">-- Chọn Xẻ tà --</option>
                <option v-for="item in attributes.xeTa" :key="item.id" :value="item.id">{{ item.ten }}</option>
              </select>
              <button class="btn-quick-add" @click="quickAdd('xeTa', 'xe-ta')" type="button">+</button>
            </div>
            <small v-if="errors.xeTaId" class="error-text">{{ errors.xeTaId }}</small>
          </div>

          <div class="form-group">
            <label class="required">Xuất xứ</label>
            <div class="flex-row-gap">
              <select v-model="product.xuatXuId" class="form-input" :class="{ 'error-border': errors.xuatXuId }">
                <option value="">-- Chọn Xuất xứ --</option>
                <option v-for="item in attributes.xuatXu" :key="item.id" :value="item.id">{{ item.ten }}</option>
              </select>
              <button class="btn-quick-add" @click="quickAdd('xuatXu', 'xuat-xu')" type="button">+</button>
            </div>
            <small v-if="errors.xuatXuId" class="error-text">{{ errors.xuatXuId }}</small>
          </div>

          <div class="form-group">
            <label class="required">Kiểu dáng</label>
            <div class="flex-row-gap">
              <select v-model="product.fitId" class="form-input" :class="{ 'error-border': errors.fitId }">
                <option value="">-- Chọn kiểu dáng --</option>
                <option v-for="item in attributes.fit" :key="item.id" :value="item.id">{{ item.ten }}</option>
              </select>
              <button class="btn-quick-add" @click="quickAdd('fit', 'fit')" type="button">+</button>
            </div>
            <small v-if="errors.fitId" class="error-text">{{ errors.fitId }}</small>
          </div>
        </div>

        <div class="form-group" style="margin-top: 15px">
          <label>Mô tả sản phẩm</label>
          <textarea v-model="product.moTa" class="form-input" rows="4" placeholder="Nhập mô tả chi tiết..."></textarea>
        </div>
      </div>

      <!-- Section 2: Variants Config -->
      <div class="section">
        <h3 class="section-title">Biến thể sản phẩm</h3>

        <div class="grid-2">
          <div class="form-group">
            <label>Màu sắc</label>
            <div class="selected-list">
              <div v-for="color in selectedColors" :key="color.id" class="selected-item-block">
                <span class="color-square" :style="{ backgroundColor: getColorCode(color.ten) }"></span>
                <input type="text" :value="color.ten" readonly class="item-name-input" />
                <button class="btn-remove-block" @click="removeColor(color.id)" type="button">×</button>
              </div>
            </div>
            <button class="btn btn-orange mt-2" @click="openAttrModal('mauSac')" type="button">+ Thêm màu</button>
          </div>

          <div class="form-group">
            <label>Kích cỡ</label>
            <div class="selected-list">
              <div v-for="size in selectedSizes" :key="size.id" class="selected-item-block">
                <span class="size-square">{{ size.soSize }}</span>
                <button class="btn-remove-block" @click="removeSize(size.id)" type="button">×</button>
              </div>
            </div>
            <button class="btn btn-orange mt-2" @click="openAttrModal('kichCo')" type="button">+ Thêm kích cỡ</button>
          </div>
        </div>

        <!-- ATTRIBUTE SELECTION MODAL -->
        <div v-if="showAttrModal" class="modal-overlay">
          <div class="modal-box modal-lg">
            <h3 class="modal-title">Chọn {{ currentAttrType === 'mauSac' ? 'Màu sắc' : 'Kích cỡ' }}</h3>

            <div class="attr-grid">
              <div
                v-for="item in attributes[currentAttrType]"
                :key="item.id"
                class="attr-item"
                :class="{ selected: tempSelectedIds.has(item.id) }"
                @click="toggleAttrSelection(item)"
              >
                <template v-if="currentAttrType === 'mauSac'">
                  <div class="attr-color-preview" :style="{ background: getColorCode(item.ten) }"></div>
                  <span class="attr-name">{{ item.ten }}</span>
                </template>
                <template v-else>
                  <span class="attr-name-lg">{{ item.soSize }}</span>
                </template>
              </div>
            </div>

            <div class="modal-actions">
              <button class="btn btn-secondary" @click="showAttrModal = false" type="button">Đóng</button>
              <button class="btn btn-orange" @click="confirmAttrSelection" type="button">Xác nhận</button>
            </div>
          </div>
        </div>

        <button class="btn btn-orange full-width-btn" @click="generateVariants" type="button">⚡ Tạo biến thể tự động</button>
      </div>

      <!-- Section 3: Variants List -->
      <div class="section" v-if="generatedVariants.length > 0">
        <div class="section-header-bar">
          <h3 class="section-title-white">Danh sách biến thể</h3>
          <div class="bulk-actions">
            <button class="btn btn-outline-white" @click="openGlobalApply" type="button">
              <span class="icon">⚡</span> Áp dụng cho tất cả
            </button>

            <!-- Xóa tất cả -> Confirm Modal giữa màn -->
            <button class="btn btn-outline-danger" type="button" @click="askClearVariants">
              <span class="icon">🗑️</span> Xóa tất cả
            </button>
          </div>
        </div>

        <div v-for="group in variantsGroupedByColor" :key="group.id" class="variant-group">
          <div class="group-header">
            <div class="group-title">
              <span class="color-dot-lg" :style="{ backgroundColor: getColorCode(group.name) }"></span>
              {{ group.name }}
              <span class="count-gray">({{ group.variants.length }} kích cỡ)</span>
            </div>
            <button class="btn-quick-add-blue" @click="openGroupApply(group)" type="button">⚡ Thêm nhanh</button>
          </div>

          <div class="table-responsive">
            <table class="table variants-table">
              <thead>
                <tr>
                  <th style="width: 24%; text-align: center;">Kích cỡ</th>
                  <th style="width: 24%;">Chất liệu</th>
                  <th style="width: 24%;">Số lượng tồn</th>
                  <th style="width: 24%;">Đơn giá</th>
                  <th style="width: 4%;"></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(variant, vIndex) in group.variants" :key="vIndex">
                  <td class="text-center">
                    <span class="size-badge">{{ variant.tenKichCo }}</span>
                  </td>
                  <td style="width: 24%;">
                    <select class="form-input" v-model="variant.chatLieuId">
                      <option value="">-- Chọn --</option>
                      <option v-for="cl in attributes.chatLieu" :key="cl.id" :value="cl.id">{{ cl.ten }}</option>
                    </select>
                  </td>
                  <td>
                    <input type="number" v-model="variant.soLuongTon" class="form-input" style="width: 100%;" min="0" />
                  </td>
                  <td>
                    <input type="number" v-model="variant.donGia" class="form-input" style="width: 100%;" min="0" />
                  </td>
                  <td class="text-center">
                    <button class="btn-icon danger" @click="removeVariantObj(variant)" type="button">×</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Section 4: Color Images -->
        <div class="section-image-upload" v-if="variantsGroupedByColor.length > 0">
          <h3 class="section-title-sm">📸 Ảnh theo màu sắc</h3>
          <div class="image-upload-grid">
            <div v-for="group in variantsGroupedByColor" :key="'img-' + group.id" class="image-upload-card">
              <div class="card-upload-header">
                <span class="color-dot-lg" :style="{ backgroundColor: getColorCode(group.name) }"></span>
                {{ group.name }}
              </div>
              <label class="upload-area">
                <div class="preview-box-lg" v-if="group.variants[0]?.anh">
                  <img :src="'http://localhost:8080' + group.variants[0].anh" />
                </div>
                <div class="upload-placeholder" v-else>
                  <span class="icon-lg">🖼️</span>
                  <span>Chưa có ảnh</span>
                </div>
                <input type="file" hidden accept="image/*" @change="(e) => handleGroupImageUpload(group, e)" />
              </label>
            </div>
          </div>
        </div>
      </div>

      <!-- Group Apply Modal -->
      <div v-if="showGroupApplyModal" class="modal-overlay">
        <div class="modal-box">
          <h3 class="modal-title">Áp dụng chung ({{ currentApplyGroup?.name }})</h3>

          <div class="form-group">
            <label>Chất liệu</label>
            <select class="form-input" v-model="groupApplyData.chatLieuId">
              <option value="">-- Chọn --</option>
              <option v-for="cl in attributes.chatLieu" :key="cl.id" :value="cl.id">{{ cl.ten }}</option>
            </select>
          </div>

          <div class="grid-2">
            <div class="form-group">
              <label>Số lượng tồn</label>
              <input type="number" v-model="groupApplyData.soLuongTon" class="form-input" placeholder="Giữ nguyên" min="0" />
            </div>
            <div class="form-group">
              <label>Đơn giá</label>
              <input type="number" v-model="groupApplyData.donGia" class="form-input" placeholder="Giữ nguyên" min="0" />
            </div>
          </div>

          <div class="modal-actions">
            <button class="btn btn-secondary" @click="showGroupApplyModal = false" type="button">Hủy</button>
            <button class="btn btn-primary" @click="confirmGroupApply" type="button">Áp dụng</button>
          </div>
        </div>
      </div>

      <!-- Quick Add Attribute Modal -->
      <div v-if="showQuickAddModal" class="modal-overlay">
        <div class="modal-box">
          <h3 class="modal-title">Thêm mới {{ quickAddLabel }}</h3>
          <input
            ref="quickAddInputRef"
            v-model="quickAddValue"
            class="form-input"
            placeholder="Nhập tên..."
            @keyup.enter="confirmQuickAdd"
          />
          <div class="modal-actions">
            <button class="btn btn-secondary" @click="closeQuickAdd" type="button">Hủy</button>
            <button class="btn btn-primary" @click="confirmQuickAdd" type="button">Thêm mới</button>
          </div>
        </div>
      </div>

      <!-- Action Bar -->
      <div class="action-bar sticky-bottom">
        <button class="btn btn-secondary" @click="goBack" type="button">Hủy</button>

        <!-- Submit -> Confirm Modal giữa màn -->
        <button class="btn btn-primary lg-btn" @click="handleSubmitClick" :disabled="loading" type="button">
          {{ loading ? 'Đang xử lý...' : (isEditMode ? 'Lưu thay đổi' : 'Hoàn tất') }}
        </button>
      </div>

      <p v-if="globalError" class="error-msg text-center">{{ globalError }}</p>
    </div>
  </div>

  <!-- Toast -->
  <div v-if="toast.show" class="toast" :class="toast.type">
    {{ toast.message }}
  </div>

  <!-- CONFIRM MODAL (giữa màn - giống danh sách thuộc tính) -->
  <div v-if="confirmState.open" class="confirm-overlay" @click.self="confirmCancel">
    <div class="confirm-modal">
      <div class="confirm-header">
        <h3>{{ confirmState.title }}</h3>
        <button class="close-btn" type="button" @click="confirmCancel">×</button>
      </div>

      <div class="confirm-body">
        <p>{{ confirmState.message }}</p>
      </div>

      <div class="confirm-actions">
        <button class="btn btn-secondary" type="button" @click="confirmCancel" :disabled="loading">
          {{ confirmState.cancelText }}
        </button>

        <button
          class="btn"
          :class="confirmState.danger ? 'btn-danger' : 'btn-primary'"
          type="button"
          @click="confirmOk"
          :disabled="loading"
        >
          {{ confirmState.okText }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import attributeService from '../../services/attributeService'
import { createSanPham, updateSanPham } from '../../services/sanPhamApi'
import { uploadImage } from '../../services/sanPhamChiTietApi'

const router = useRouter()
const route = useRoute()

const props = defineProps({ id: { type: [String, Number], default: null } })
const isEditMode = computed(() => !!props.id)

const loading = ref(false)
const globalError = ref('')

/* ========= Toast ========= */
const toast = ref({ show: false, message: '', type: 'success' })
let toastTimer = null
function showToast(message, type = 'success', duration = 2200) {
  toast.value = { show: true, message, type }
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value.show = false), duration)
}

/* ========= Confirm Modal (giữa màn) ========= */
const confirmState = reactive({
  open: false,
  title: 'Xác nhận',
  message: '',
  okText: 'OK',
  cancelText: 'Hủy',
  danger: false,
  _resolve: null
})

function openConfirm({ title, message, okText, cancelText, danger } = {}) {
  confirmState.title = title ?? 'Xác nhận'
  confirmState.message = message ?? ''
  confirmState.okText = okText ?? 'OK'
  confirmState.cancelText = cancelText ?? 'Hủy'
  confirmState.danger = !!danger
  confirmState.open = true
  return new Promise((res) => (confirmState._resolve = res))
}

function confirmOk() {
  confirmState.open = false
  confirmState._resolve?.(true)
  confirmState._resolve = null
}
function confirmCancel() {
  confirmState.open = false
  confirmState._resolve?.(false)
  confirmState._resolve = null
}

/* ========= Form ========= */
const product = reactive({
  maSanPham: 'SP' + Math.floor(Math.random() * 10000),
  tenSanPham: '',
  loaiSanPhamId: '',
  thuongHieuId: '',
  chatLieuId: '',
  soKhuyId: '',
  kieuTuiId: '',
  veAoId: '',
  xeTaId: '',
  xuatXuId: '',
  fitId: '',
  trangThai: true,
  moTa: '',
  anh: ''
})

/* ========= Attributes ========= */
const attributes = reactive({
  loaiSanPham: [],
  thuongHieu: [],
  chatLieu: [],
  soKhuy: [],
  kieuTui: [],
  veAo: [],
  xeTa: [],
  xuatXu: [],
  fit: [],
  mauSac: [],
  kichCo: []
})

const attributeMap = {
  loaiSanPham: 'loai-san-pham',
  thuongHieu: 'thuong-hieu',
  chatLieu: 'chat-lieu',
  soKhuy: 'so-khuy',
  kieuTui: 'kieu-tui',
  veAo: 've-ao',
  xeTa: 'xe-ta',
  xuatXu: 'xuat-xu',
  fit: 'fit',
  mauSac: 'mau-sac',
  kichCo: 'kich-co'
}

onMounted(async () => {
  try {
    const promises = Object.keys(attributeMap).map((key) =>
      attributeService.getAllList(attributeMap[key]).then((res) => {
        // chỉ lấy active để chọn
        attributes[key] = (res.data || []).filter((item) => item.trangThai === true)
      })
    )
    await Promise.all(promises)
  } catch (e) {
    console.error(e)
    globalError.value = 'Lỗi tải dữ liệu.'
    showToast('Lỗi tải dữ liệu thuộc tính', 'error')
  }
})

/* ========= Variants ========= */
const selectedColors = ref([])
const selectedSizes = ref([])
const generatedVariants = ref([])

function removeColor(id) {
  selectedColors.value = selectedColors.value.filter((c) => c.id !== id)
}
function removeSize(id) {
  selectedSizes.value = selectedSizes.value.filter((s) => s.id !== id)
}

function generateVariants() {
  if (selectedColors.value.length === 0 || selectedSizes.value.length === 0) {
    showToast('Vui lòng chọn ít nhất 1 màu và 1 kích cỡ!', 'error')
    return
  }

  const newVariants = []
  for (const color of selectedColors.value) {
    for (const size of selectedSizes.value) {
      const exists = generatedVariants.value.some((v) => v.idMauSac === color.id && v.idKichCo === size.id)
      if (!exists) {
        newVariants.push({
          id: Date.now() + Math.random(),
          idMauSac: color.id,
          tenMauSac: color.ten,
          idKichCo: size.id,
          tenKichCo: size.soSize,
          soLuongTon: 10,
          donGia: 0,
          anh: '',
          chatLieuId: ''
        })
      }
    }
  }

  generatedVariants.value = [...generatedVariants.value, ...newVariants]
  showToast('Đã tạo biến thể tự động!', 'success')
}

function removeVariantObj(variant) {
  const idx = generatedVariants.value.indexOf(variant)
  if (idx > -1) generatedVariants.value.splice(idx, 1)
}

const variantsGroupedByColor = computed(() => {
  const groups = {}
  generatedVariants.value.forEach((v) => {
    if (!groups[v.idMauSac]) groups[v.idMauSac] = { id: v.idMauSac, name: v.tenMauSac, variants: [] }
    groups[v.idMauSac].variants.push(v)
  })
  return Object.values(groups)
})

async function handleGroupImageUpload(group, event) {
  const file = event.target.files?.[0]
  if (!file) return
  try {
    const res = await uploadImage(file)
    const url = res.data.url
    group.variants.forEach((v) => (v.anh = url))
    showToast('Upload ảnh theo màu thành công!', 'success')
  } catch (e) {
    console.error(e)
    showToast('Lỗi upload ảnh', 'error')
  }
}

/* ========= Group Apply ========= */
const showGroupApplyModal = ref(false)
const currentApplyGroup = ref(null)
const groupApplyData = reactive({ chatLieuId: '', soLuongTon: '', donGia: '' })

function openGroupApply(group) {
  currentApplyGroup.value = group
  groupApplyData.chatLieuId = ''
  groupApplyData.soLuongTon = ''
  groupApplyData.donGia = ''
  showGroupApplyModal.value = true
}
function openGlobalApply() {
  openGroupApply({ name: 'Tất cả biến thể', variants: generatedVariants.value })
}
function confirmGroupApply() {
  if (!currentApplyGroup.value) return
  currentApplyGroup.value.variants.forEach((variant) => {
    if (groupApplyData.chatLieuId) variant.chatLieuId = groupApplyData.chatLieuId
    if (groupApplyData.soLuongTon !== '') variant.soLuongTon = Number(groupApplyData.soLuongTon)
    if (groupApplyData.donGia !== '') variant.donGia = Number(groupApplyData.donGia)
  })
  showGroupApplyModal.value = false
  currentApplyGroup.value = null
  showToast('Đã áp dụng cho nhóm biến thể!', 'success')
}

/* ========= Quick Add (mã tự tăng theo prefix: MS01, TH01...) ========= */
const CODE_PREFIX = {
  'mau-sac': 'MS',
  'thuong-hieu': 'TH',
  'chat-lieu': 'CL',
  'kich-co': 'KC',
  'loai-san-pham': 'LSP',
  'so-khuy': 'SK',
  'kieu-tui': 'KT',
  've-ao': 'VA',
  'xe-ta': 'XT',
  'xuat-xu': 'XX',
  'fit': 'FIT'
}
function pad2(n) {
  return n < 10 ? `0${n}` : String(n)
}
function genNextAttrCode(typeCode, list) {
  const prefix = (CODE_PREFIX[typeCode] || 'TT').toUpperCase()
  const nums = (list || [])
    .map((i) => String(i.ma || '').toUpperCase())
    .map((ma) => {
      const m = ma.match(new RegExp(`^${prefix}(\\d+)$`))
      return m ? Number(m[1]) : null
    })
    .filter((n) => Number.isFinite(n))

  let next = (nums.length ? Math.max(...nums) : 0) + 1
  return `${prefix}${pad2(next)}`
}

const showQuickAddModal = ref(false)
const quickAddValue = ref('')
const quickAddTarget = reactive({ key: '', typeCode: '' })
const quickAddInputRef = ref(null)

const quickAddLabel = computed(() => {
  const map = {
    loaiSanPham: 'Loại sản phẩm',
    thuongHieu: 'Thương hiệu',
    chatLieu: 'Chất liệu',
    soKhuy: 'Số khuy',
    kieuTui: 'Kiểu túi',
    veAo: 'Ve áo',
    xeTa: 'Xẻ tà',
    xuatXu: 'Xuất xứ',
    fit: 'Fit',
    mauSac: 'Màu sắc',
    kichCo: 'Kích cỡ'
  }
  return map[quickAddTarget.key] || 'Thuộc tính'
})

function quickAdd(attrKey, typeCode) {
  quickAddTarget.key = attrKey
  quickAddTarget.typeCode = typeCode
  quickAddValue.value = ''
  showQuickAddModal.value = true
  nextTick(() => quickAddInputRef.value?.focus())
}
function closeQuickAdd() {
  showQuickAddModal.value = false
}

async function confirmQuickAdd() {
  const raw = String(quickAddValue.value || '').trim()
  if (!raw) {
    showToast('Vui lòng nhập tên!', 'error')
    return
  }

  try {
    // lấy ALL list để tính mã tiếp theo (kể cả inactive)
    const allRes = await attributeService.getAllList(quickAddTarget.typeCode)
    const allList = allRes.data || []
    const nextMa = genNextAttrCode(quickAddTarget.typeCode, allList)

    const payload =
      quickAddTarget.typeCode === 'kich-co'
        ? { ma: nextMa, soSize: Number(raw), trangThai: true }
        : { ma: nextMa, ten: raw, trangThai: true }

    await attributeService.create(quickAddTarget.typeCode, payload)

    // reload list active để chọn
    const res = await attributeService.getAllList(quickAddTarget.typeCode)
    const activeList = (res.data || []).filter((x) => x.trangThai === true)
    attributes[quickAddTarget.key] = activeList

    // auto chọn item mới
    const newItem =
      quickAddTarget.typeCode === 'kich-co'
        ? activeList.find((x) => Number(x.soSize) === Number(raw))
        : activeList.find((x) => String(x.ten).trim() === raw)

    if (newItem && (quickAddTarget.key + 'Id') in product) {
      product[quickAddTarget.key + 'Id'] = newItem.id
    }

    closeQuickAdd()
    showToast(`Đã thêm mới ${quickAddLabel.value}!`, 'success')
  } catch (e) {
    console.error(e)
    showToast('Lỗi thêm mới thuộc tính', 'error')
  }
}

/* ========= Attr Selection Modal ========= */
const showAttrModal = ref(false)
const currentAttrType = ref('')
const tempSelectedIds = ref(new Set())

function openAttrModal(type) {
  currentAttrType.value = type
  tempSelectedIds.value = new Set()
  const currentList = type === 'mauSac' ? selectedColors.value : selectedSizes.value
  currentList.forEach((item) => tempSelectedIds.value.add(item.id))
  showAttrModal.value = true
}

function toggleAttrSelection(item) {
  if (tempSelectedIds.value.has(item.id)) tempSelectedIds.value.delete(item.id)
  else tempSelectedIds.value.add(item.id)
}

function confirmAttrSelection() {
  const list = attributes[currentAttrType.value] || []
  const selectedItems = list.filter((item) => tempSelectedIds.value.has(item.id))
  if (currentAttrType.value === 'mauSac') selectedColors.value = selectedItems
  else selectedSizes.value = selectedItems
  showAttrModal.value = false
}

/* ========= Validation ========= */
const errors = reactive({
  tenSanPham: '',
  maSanPham: '',
  loaiSanPhamId: '',
  thuongHieuId: '',
  soKhuyId: '',
  kieuTuiId: '',
  veAoId: '',
  xeTaId: '',
  xuatXuId: '',
  fitId: ''
})

function validate() {
  Object.keys(errors).forEach((k) => (errors[k] = ''))
  let valid = true

  if (!product.tenSanPham) {
    errors.tenSanPham = 'Tên sản phẩm bắt buộc'
    valid = false
  }
  if (!product.maSanPham) {
    errors.maSanPham = 'Mã sản phẩm bắt buộc'
    valid = false
  }

  const req = ['loaiSanPhamId', 'thuongHieuId', 'soKhuyId', 'kieuTuiId', 'veAoId', 'xeTaId', 'xuatXuId', 'fitId']
  for (const r of req) {
    if (!product[r]) {
      errors[r] = 'Bắt buộc'
      valid = false
    }
  }

  return valid
}

/* ========= Submit (Confirm Modal giữa màn) ========= */
async function handleSubmitClick() {
  globalError.value = ''

  if (!validate()) {
    showToast('Vui lòng điền đầy đủ thông tin cơ bản', 'error')
    return
  }

  const message =
    generatedVariants.value.length === 0
      ? 'Bạn chưa tạo biến thể nào. Sản phẩm sẽ được tạo nhưng không có tồn kho. Tiếp tục?'
      : isEditMode.value
          ? 'Bạn có chắc chắn muốn lưu thay đổi sản phẩm không?'
          : 'Bạn có chắc chắn muốn hoàn tất thêm sản phẩm không?'

  const ok = await openConfirm({
    title: 'Xác nhận',
    message,
    okText: 'Đồng ý',
    cancelText: 'Hủy',
    danger: false
  })

  if (!ok) return
  await doSubmit()
}

async function doSubmit() {
  const variantsPayload = generatedVariants.value.map((v) => {
    const selectedMat = attributes.chatLieu.find((cl) => cl.id === v.chatLieuId)
    return {
      idKichCo: v.idKichCo,
      idMauSac: v.idMauSac,
      soLuongTon: v.soLuongTon,
      donGia: v.donGia,
      anh: v.anh,
      ghiChu: '',
      trangThai: true,
      chatLieu: selectedMat ? selectedMat.ten : ''
    }
  })

  const payload = {
    ...product,
    variants: variantsPayload,
    mauSacId: null,
    kichCoId: null,
    donGia: 0,
    soLuongTon: 0
  }

  loading.value = true
  try {
    if (isEditMode.value) await updateSanPham(props.id, payload)
    else await createSanPham(payload)

    showToast(isEditMode.value ? 'Cập nhật sản phẩm thành công!' : 'Thêm sản phẩm thành công!', 'success')
    setTimeout(() => router.push('/products'), 700)
  } catch (e) {
    console.error(e)
    globalError.value = 'Có lỗi xảy ra.'
    showToast('Không thể hoàn tất. Vui lòng thử lại!', 'error', 2600)
  } finally {
    loading.value = false
  }
}

/* ========= Clear Variants (Confirm Modal giữa màn) ========= */
async function askClearVariants() {
  if (generatedVariants.value.length === 0) {
    showToast('Không có biến thể để xóa', 'error')
    return
  }

  const ok = await openConfirm({
    title: 'Xóa tất cả biến thể',
    message: 'Bạn có chắc chắn muốn xóa tất cả biến thể không?',
    okText: 'Xóa',
    cancelText: 'Hủy',
    danger: true
  })

  if (!ok) return

  generatedVariants.value = []
  showToast('Đã xóa tất cả biến thể', 'success')
}

/* ========= Navigation ========= */
function goBack() {
  router.push('/products')
}

/* ========= Color helper ========= */
function getColorCode(name) {
  if (!name) return '#e5e7eb'
  const n = String(name).toLowerCase()
  if (n.includes('đen') || n.includes('black')) return 'black'
  if (n.includes('trắng') || n.includes('white')) return '#ffffff'
  if (n.includes('đỏ') || n.includes('red')) return '#ef4444'
  if (n.includes('xanh dương') || n.includes('blue')) return '#3b82f6'
  if (n.includes('xanh lá') || n.includes('green')) return '#22c55e'
  if (n.includes('vàng') || n.includes('yellow')) return '#eab308'
  if (n.includes('cam') || n.includes('orange')) return '#f97316'
  if (n.includes('tím') || n.includes('purple')) return '#a855f7'
  if (n.includes('hồng') || n.includes('pink')) return '#ec4899'
  if (n.includes('nâu') || n.includes('brown')) return '#78350f'
  if (n.includes('be') || n.includes('beige')) return '#f5f5dc'
  if (n.includes('xanh than') || n.includes('navy')) return '#1e3a8a'
  if (n.includes('xám') || n.includes('ghi') || n.includes('gray') || n.includes('grey')) return '#6b7280'
  const match = n.match(/\(([^)]+)\)/)
  if (match) return match[1]
  return '#e5e7eb'
}
</script>

<style scoped>
/* ===== Modal (attr modal + quick add + group apply) ===== */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-box {
  background: white;
  padding: 20px;
  border-radius: 8px;
  width: 400px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}
.modal-title {
  margin-top: 0;
  margin-bottom: 12px;
  font-size: 1.1rem;
  font-weight: 600;
  color: #1f2937;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

/* ===== Toast ===== */
.toast {
  position: fixed;
  top: 16px;
  right: 16px;
  padding: 10px 14px;
  border-radius: 10px;
  font-weight: 600;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.12);
  z-index: 9999;
  max-width: 360px;
}
.toast.success {
  background: #e9f7ef;
  color: #1e7e34;
  border: 1px solid #b7ebc6;
}
.toast.error {
  background: #fdecea;
  color: #b02a37;
  border: 1px solid #f5c2c7;
}

/* ===== Confirm Modal (giữa màn - giống danh sách thuộc tính) ===== */
.confirm-overlay{
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
}
.confirm-modal{
  width: 420px;
  max-width: calc(100vw - 24px);
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 15px 40px rgba(0,0,0,0.2);
  overflow: hidden;
}
.confirm-header{
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #eef2f7;
}
.confirm-header h3{
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
  color: #111827;
}
.close-btn{
  border: none;
  background: transparent;
  font-size: 22px;
  cursor: pointer;
  line-height: 1;
  color: #6b7280;
}
.confirm-body{
  padding: 16px;
  color: #374151;
}
.confirm-body p{
  margin: 0;
  line-height: 1.5;
}
.confirm-actions{
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 0 16px 16px;
}
.btn-danger{
  background: #ef4444;
  color: #fff;
}
.btn-danger:hover{
  background: #dc2626;
}

/* ===== Base UI (giữ giống bạn) ===== */
.title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #374151;
}
.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.section {
  margin-bottom: 30px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  border: 1px solid #e5e7eb;
}
.section-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 20px;
  color: #1f2937;
  border-bottom: 1px solid #f3f4f6;
  padding-bottom: 10px;
}
.grid-3 {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}
.grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
.form-group {
  margin-bottom: 15px;
}
.form-input {
  width: 100%;
  padding: 10px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
}
.required::after {
  content: ' *';
  color: red;
}
.error-text {
  color: red;
  font-size: 0.8rem;
}
.error-border {
  border-color: #ef4444 !important;
  box-shadow: 0 0 0 1px rgba(239, 68, 68, 0.2);
}

.btn {
  padding: 10px 20px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-weight: 500;
  transition: 0.2s;
}
.btn-secondary {
  background: #e5e7eb;
  color: #374151;
}
.btn-primary {
  background: #1e3a8a;
  color: white;
}
.btn-orange {
  background: #1e40af;
  color: white;
}
.btn-orange:hover {
  background: #1e40af;
}

.full-width-btn {
  width: 100%;
  margin-top: 10px;
}

.section-header-bar {
  background: linear-gradient(to right, #1e40af, #1e40af);
  padding: 10px 20px;
  border-radius: 6px 6px 0 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.section-title-white {
  color: white;
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
}

.btn-outline-white {
  border: 1px solid rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.2);
  color: white;
  margin-left: 10px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
}
.btn-outline-white:hover {
  background: rgba(255, 255, 255, 0.3);
}

.btn-outline-danger {
  border: 1px solid #fecaca;
  background: #fee2e2;
  color: #1e40af;
  margin-left: 10px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
}
.btn-outline-danger:hover {
  background: #fecaca;
}

.variant-group {
  background: #fdfdfd;
  border: 1px solid #e5e7eb;
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
}
.group-header {
  background: #fff7ed;
  padding: 10px 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e5e7eb;
}
.group-title {
  font-weight: 600;
  color: #1e40af;
  display: flex;
  align-items: center;
  gap: 8px;
}
.color-dot-lg {
  width: 16px;
  height: 16px;
  background: gray;
  border-radius: 50%;
  display: inline-block;
}
.count-gray {
  color: #6b7280;
  font-weight: normal;
  font-size: 0.9rem;
  margin-left: 5px;
}

.variants-table {
  width: 100%;
  border-collapse: collapse;
}
.variants-table th {
  background: #f9fafb;
  padding: 10px;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}
.variants-table td {
  padding: 10px;
  border-bottom: 1px solid #e5e7eb;
  vertical-align: middle;
}

.size-badge {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 38px;
  border: 1px solid #d1d5db;
  background: #f3f4f6;
  border-radius: 6px;
  font-weight: 600;
  color: #374151;
}

.text-center {
  text-align: center;
}
.flex-row-gap {
  display: flex;
  gap: 8px;
}

.btn-quick-add {
  border: 1px solid #d1d5db;
  background: #f9fafb;
  color: #374151;
  width: 38px;
  border-radius: 6px;
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.btn-quick-add:hover {
  background: #e5e7eb;
  color: #1e3a8a;
  border-color: #1e3a8a;
}

.selected-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 10px;
}
.selected-item-block {
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 5px 10px;
  background: white;
}

.color-square {
  width: 30px;
  height: 30px;
  border-radius: 4px;
  border: 1px solid rgba(0, 0, 0, 0.1);
}
.size-square {
  font-weight: 600;
  padding: 0 5px;
}

.item-name-input {
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 0.9rem;
  width: 100px;
  color: #374151;
  background: #f9fafb;
}

.btn-remove-block {
  background: #1e40af;
  color: white;
  border: none;
  border-radius: 4px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 1.2rem;
  line-height: 1;
}
.mt-2 { margin-top: 10px; }

.modal-lg { max-width: 600px; }
.attr-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 15px;
  margin: 20px 0;
  max-height: 400px;
  overflow-y: auto;
}
.attr-item {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 10px;
  cursor: pointer;
  text-align: center;
  transition: all 0.2s;
  background: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 80px;
}
.attr-item:hover {
  border-color: #1e40af;
  background: #fff7ed;
}
.attr-item.selected {
  border-color: #1e40af;
  background: #ffedd5;
  box-shadow: 0 0 0 1px #1e40af;
}
.attr-color-preview {
  width: 30px;
  height: 30px;
  border-radius: 4px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  margin-bottom: 5px;
}
.attr-name { font-size: 0.85rem; color: #374151; }
.attr-name-lg { font-size: 1.1rem; font-weight: 600; color: #374151; }

.section-image-upload {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}
.section-title-sm {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.image-upload-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}
.image-upload-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  background: white;
}
.card-upload-header {
  padding: 10px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
}
.upload-area {
  height: 150px;
  background: #fdfdfd;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: 0.2s;
}
.upload-area:hover { background: #f3f4f6; }
.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #9ca3af;
  gap: 5px;
  font-size: 0.85rem;
}
.icon-lg { font-size: 2rem; }
.preview-box-lg { width: 100%; height: 100%; overflow: hidden; }
.preview-box-lg img { width: 100%; height: 100%; object-fit: contain; }

.btn-quick-add-blue {
  background: #1e40af;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 6px 12px;
  font-size: 0.85rem;
  cursor: pointer;
  font-weight: 500;
}
.btn-quick-add-blue:hover { background: #1e3a8a; }

.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  background: white;
  padding: 20px;
  border-top: 1px solid #e5e7eb;
  margin-top: 20px;
}

/* icon remove variant */
.btn-icon {
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
}
.btn-icon.danger {
  background: #fee2e2;
  color: #991b1b;
}
.btn-icon.danger:hover {
  background: #fecaca;
}
</style>
