<template>
  <div class="page">
    <div class="card">
      <!-- Header -->
      <div class="card-header flex-between">
        <h2 class="title">{{ isEditMode ? 'Cập nhật sản phẩm' : 'Thêm sản phẩm mới' }}</h2>
        <button class="btn btn-secondary" @click="goBack" type="button">← Quay lại danh sách</button>
      </div>

      <!-- Body (scroll) -->
      <div class="card-body">
        <!-- ===== Thông tin cơ bản ===== -->
        <div class="section">
          <h3 class="section-title">Thông tin cơ bản</h3>

          <div class="grid-3">
            <div class="form-group">
              <label class="required">Tên sản phẩm</label>
              <input
                v-model="product.tenSanPham"
                class="form-input"
                placeholder="Nhập tên sản phẩm"
                :class="{ 'error-border': errors.tenSanPham }"
              />
              <small v-if="errors.tenSanPham" class="error-text">{{ errors.tenSanPham }}</small>
            </div>

            <div class="form-group">
              <label class="required">Loại sản phẩm</label>
              <div :class="['ms-wrap', { 'error-border': errors.loaiSanPhamId }]">
                <Multiselect
                  v-model="msLoaiSanPham"
                  :options="attributes.loaiSanPham"
                  track-by="id"
                  label="ten"
                  placeholder="-- Chọn Loại sản phẩm --"
                  :searchable="true"
                  :taggable="true"
                  tag-placeholder="Nhập để thêm"
                  :show-labels="false"
                  @tag="(name) => onTagCreate('loai-san-pham','loaiSanPham','loaiSanPhamId',name)"
                />
              </div>
              <small v-if="errors.loaiSanPhamId" class="error-text">{{ errors.loaiSanPhamId }}</small>
            </div>

            <div class="form-group">
              <label class="required">Thương hiệu</label>
              <div :class="['ms-wrap', { 'error-border': errors.thuongHieuId }]">
                <Multiselect
                  v-model="msThuongHieu"
                  :options="attributes.thuongHieu"
                  track-by="id"
                  label="ten"
                  placeholder="-- Chọn Thương hiệu --"
                  :searchable="true"
                  :taggable="true"
                  tag-placeholder="Nhập để thêm"
                  :show-labels="false"
                  @tag="(name) => onTagCreate('thuong-hieu','thuongHieu','thuongHieuId',name)"
                />
              </div>
              <small v-if="errors.thuongHieuId" class="error-text">{{ errors.thuongHieuId }}</small>
            </div>

            <div class="form-group">
              <label class="required">Số khuy</label>
              <div :class="['ms-wrap', { 'error-border': errors.soKhuyId }]">
                <Multiselect
                  v-model="msSoKhuy"
                  :options="attributes.soKhuy"
                  track-by="id"
                  label="ten"
                  placeholder="-- Chọn Số khuy --"
                  :searchable="true"
                  :taggable="true"
                  tag-placeholder="Nhập để thêm"
                  :show-labels="false"
                  @tag="(name) => onTagCreate('so-khuy','soKhuy','soKhuyId',name)"
                />
              </div>
              <small v-if="errors.soKhuyId" class="error-text">{{ errors.soKhuyId }}</small>
            </div>

            <div class="form-group">
              <label class="required">Kiểu túi</label>
              <div :class="['ms-wrap', { 'error-border': errors.kieuTuiId }]">
                <Multiselect
                  v-model="msKieuTui"
                  :options="attributes.kieuTui"
                  track-by="id"
                  label="ten"
                  placeholder="-- Chọn Kiểu túi --"
                  :searchable="true"
                  :taggable="true"
                  tag-placeholder="Nhập để thêm"
                  :show-labels="false"
                  @tag="(name) => onTagCreate('kieu-tui','kieuTui','kieuTuiId',name)"
                />
              </div>
              <small v-if="errors.kieuTuiId" class="error-text">{{ errors.kieuTuiId }}</small>
            </div>

            <div class="form-group">
              <label class="required">Ve áo</label>
              <div :class="['ms-wrap', { 'error-border': errors.veAoId }]">
                <Multiselect
                  v-model="msVeAo"
                  :options="attributes.veAo"
                  track-by="id"
                  label="ten"
                  placeholder="-- Chọn Ve áo --"
                  :searchable="true"
                  :taggable="true"
                  tag-placeholder="Nhập để thêm"
                  :show-labels="false"
                  @tag="(name) => onTagCreate('ve-ao','veAo','veAoId',name)"
                />
              </div>
              <small v-if="errors.veAoId" class="error-text">{{ errors.veAoId }}</small>
            </div>

            <div class="form-group">
              <label class="required">Xẻ tà</label>
              <div :class="['ms-wrap', { 'error-border': errors.xeTaId }]">
                <Multiselect
                  v-model="msXeTa"
                  :options="attributes.xeTa"
                  track-by="id"
                  label="ten"
                  placeholder="-- Chọn Xẻ tà --"
                  :searchable="true"
                  :taggable="true"
                  tag-placeholder="Nhập để thêm"
                  :show-labels="false"
                  @tag="(name) => onTagCreate('xe-ta','xeTa','xeTaId',name)"
                />
              </div>
              <small v-if="errors.xeTaId" class="error-text">{{ errors.xeTaId }}</small>
            </div>

            <div class="form-group">
              <label class="required">Xuất xứ</label>
              <div :class="['ms-wrap', { 'error-border': errors.xuatXuId }]">
                <Multiselect
                  v-model="msXuatXu"
                  :options="attributes.xuatXu"
                  track-by="id"
                  label="ten"
                  placeholder="-- Chọn Xuất xứ --"
                  :searchable="true"
                  :taggable="true"
                  tag-placeholder="Nhập để thêm"
                  :show-labels="false"
                  @tag="(name) => onTagCreate('xuat-xu','xuatXu','xuatXuId',name)"
                />
              </div>
              <small v-if="errors.xuatXuId" class="error-text">{{ errors.xuatXuId }}</small>
            </div>

            <div class="form-group">
              <label class="required">Kiểu dáng</label>
              <div :class="['ms-wrap', { 'error-border': errors.fitId }]">
                <Multiselect
                  v-model="msFit"
                  :options="attributes.fit"
                  track-by="id"
                  label="ten"
                  placeholder="-- Chọn kiểu dáng --"
                  :searchable="true"
                  :taggable="true"
                  tag-placeholder="Nhập để thêm"
                  :show-labels="false"
                  @tag="(name) => onTagCreate('fit','fit','fitId',name)"
                />
              </div>
              <small v-if="errors.fitId" class="error-text">{{ errors.fitId }}</small>
            </div>

            <div class="form-group">
              <label class="required">Chất liệu</label>
              <div :class="['ms-wrap', { 'error-border': errors.chatLieuId }]">
                <Multiselect
                  v-model="msChatLieu"
                  :options="attributes.chatLieu"
                  track-by="id"
                  label="ten"
                  placeholder="-- Chọn Chất liệu --"
                  :searchable="true"
                  :taggable="true"
                  tag-placeholder="Nhập để thêm"
                  :show-labels="false"
                  @tag="(name) => onTagCreate('chat-lieu','chatLieu','chatLieuId',name)"
                />
              </div>
              <small v-if="errors.chatLieuId" class="error-text">{{ errors.chatLieuId }}</small>
            </div>
          </div>

          <div class="form-group mt-2">
            <label>Mô tả sản phẩm</label>
            <textarea v-model="product.moTa" class="form-input" rows="4" placeholder="Nhập mô tả chi tiết..."></textarea>
          </div>
        </div>

        <!-- ===== Biến thể ===== -->
        <div class="section">
          <h3 class="section-title">Biến thể sản phẩm</h3>

          <div class="grid-2">
            <div class="form-group">
              <label>Màu sắc</label>
              <div class="selected-list">
                <div v-for="c in selectedColors" :key="c.id" class="selected-item-block">
                  <span class="color-square" :style="{ backgroundColor: getColorCode(c.ten) }"></span>
                  <input class="item-name-input" readonly :value="c.ten" />
                  <button class="btn-remove-block" type="button" @click="removeColor(c.id)">×</button>
                </div>
              </div>
              <button class="btn btn-orange mt-2" type="button" @click="openAttrModal('mauSac')">+ Thêm màu</button>
            </div>

            <div class="form-group">
              <label>Kích cỡ</label>
              <div class="selected-list">
                <div v-for="s in selectedSizes" :key="s.id" class="selected-item-block">
                  <span class="size-square">{{ s.soSize }}</span>
                  <button class="btn-remove-block" type="button" @click="removeSize(s.id)">×</button>
                </div>
              </div>
              <button class="btn btn-orange mt-2" type="button" @click="openAttrModal('kichCo')">+ Thêm kích cỡ</button>
            </div>
          </div>

          <button class="btn btn-orange full-width-btn" type="button" @click="generateVariants">⚡ Tạo biến thể tự động</button>
        </div>

        <!-- ===== Danh sách biến thể ===== -->
        <div class="section" v-if="generatedVariants.length > 0">
          <div class="section-header-bar">
            <h3 class="section-title-white">Danh sách biến thể</h3>
            <div class="bulk-actions">
              <button class="btn btn-outline-white" type="button" @click="openGlobalApply">⚡ Áp dụng cho tất cả</button>
              <button class="btn btn-outline-danger" type="button" @click="askClearVariants">🗑️ Xóa tất cả</button>
            </div>
          </div>

          <div v-for="g in variantsGroupedByColor" :key="g.id" class="variant-group">
            <div class="group-header">
              <div class="group-title">
                <span class="color-dot-lg" :style="{ backgroundColor: getColorCode(g.name) }"></span>
                {{ g.name }}
                <span class="count-gray">({{ g.variants.length }} kích cỡ)</span>
              </div>
              <button class="btn-quick-add-blue" type="button" @click="openGroupApply(g)">⚡ Thêm nhanh</button>
            </div>

            <div class="table-responsive">
              <table class="variants-table">
                <thead>
                  <tr>
                    <th style="width:24%; text-align:center;">Kích cỡ</th>
                                        <th style="width:24%;">Số lượng tồn</th>
                    <th style="width:24%;">Đơn giá</th>
                    <th style="width:4%;"></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(v, idx) in g.variants" :key="idx">
                    <td class="text-center"><span class="size-badge">{{ v.tenKichCo }}</span></td>                    <td><input class="form-input" type="number" min="0" v-model="v.soLuongTon" /></td>
                    <td><input class="form-input" type="number" min="0" v-model="v.donGia" /></td>
                    <td class="text-center">
                      <button class="btn-icon danger" type="button" @click="removeVariantObj(v)">×</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Ảnh theo màu -->
          <div class="section-image-upload" v-if="variantsGroupedByColor.length > 0">
            <h3 class="section-title-sm">📸 Ảnh theo màu sắc</h3>
            <div class="image-upload-grid">
              <div v-for="g in variantsGroupedByColor" :key="'img-'+g.id" class="image-upload-card">
                <div class="card-upload-header">
                  <span class="color-dot-lg" :style="{ backgroundColor: getColorCode(g.name) }"></span>
                  {{ g.name }}
                </div>

                <label class="upload-area">
                  <div class="preview-box-lg" v-if="g.variants[0]?.anh">
                    <img :src="'http://localhost:8080' + g.variants[0].anh" />
                  </div>
                  <div class="upload-placeholder" v-else>
                    <span class="icon-lg">🖼️</span>
                    <span>Chưa có ảnh</span>
                  </div>
                  <input type="file" hidden accept="image/*" @change="(e) => handleGroupImageUpload(g, e)" />
                </label>
              </div>
            </div>
          </div>
        </div>

        <p v-if="globalError" class="error-msg">{{ globalError }}</p>
      </div>

      <!-- Footer actions (fixed inside card) -->
      <div class="action-bar">
        <button class="btn btn-secondary" type="button" @click="goBack">Hủy</button>
        <button class="btn btn-primary" type="button" :disabled="loading" @click="handleSubmitClick">
          {{ loading ? 'Đang xử lý...' : (isEditMode ? 'Lưu thay đổi' : 'Hoàn tất') }}
        </button>
      </div>
    </div>

    <!-- ===== Modals ===== -->

    <!-- Chọn thuộc tính màu / size -->
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
          <button class="btn btn-secondary" type="button" @click="showAttrModal=false">Đóng</button>
          <button class="btn btn-orange" type="button" @click="confirmAttrSelection">Xác nhận</button>
        </div>
      </div>
    </div>

    <!-- Apply nhóm -->
    <div v-if="showGroupApplyModal" class="modal-overlay">
      <div class="modal-box">
        <h3 class="modal-title">Áp dụng chung ({{ currentApplyGroup?.name }})</h3>        <div class="grid-2">
          <div class="form-group">
            <label>Số lượng tồn</label>
            <input class="form-input" type="number" min="0" placeholder="Giữ nguyên" v-model="groupApplyData.soLuongTon" />
          </div>
          <div class="form-group">
            <label>Đơn giá</label>
            <input class="form-input" type="number" min="0" placeholder="Giữ nguyên" v-model="groupApplyData.donGia" />
          </div>
        </div>

        <div class="modal-actions">
          <button class="btn btn-secondary" type="button" @click="showGroupApplyModal=false">Hủy</button>
          <button class="btn btn-primary" type="button" @click="confirmGroupApply">Áp dụng</button>
        </div>
      </div>
    </div>

    <!-- Quick add (dấu + cho các trường) -->
    <div v-if="showQuickAddModal" class="modal-overlay">
      <div class="modal-box">
        <h3 class="modal-title">Thêm nhanh {{ quickAddLabel }}</h3>
        <input
          ref="quickAddInputRef"
          class="form-input"
          v-model="quickAddValue"
          placeholder="Nhập..."
          @keyup.enter="confirmQuickAdd"
        />
        <div class="modal-actions">
          <button class="btn btn-secondary" type="button" @click="closeQuickAdd">Hủy</button>
          <button class="btn btn-primary" type="button" @click="confirmQuickAdd">Thêm</button>
        </div>
      </div>
    </div>

    <!-- Confirm -->
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
          <button class="btn btn-secondary" type="button" @click="confirmCancel" :disabled="loading">{{ confirmState.cancelText }}</button>
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

    <!-- ===== Toast giống ảnh ===== -->
    <div v-if="toast.show" class="toast2" :class="toast.type">
      <div class="toast2-bar"></div>
      <div class="toast2-main">
        <div class="toast2-title">{{ toast.title }}</div>
        <div class="toast2-msg">{{ toast.message }}</div>
      </div>
      <button class="toast2-close" type="button" @click="hideToast">×</button>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed, nextTick } from 'vue'
import Multiselect from 'vue-multiselect'
import 'vue-multiselect/dist/vue-multiselect.css'
import { useRouter } from 'vue-router'
import attributeService from '../../services/attributeService'
import { createSanPham, updateSanPham } from '../../services/sanPhamApi'
import { uploadImage } from '../../services/sanPhamChiTietApi'

const router = useRouter()

/* ===== Props ===== */
const props = defineProps({ id: { type: [String, Number], default: null } })
const isEditMode = computed(() => !!props.id)

const loading = ref(false)
const globalError = ref('')

/* ===== Toast ===== */
const toast = ref({ show: false, title: 'Thành công', message: '', type: 'success' })
let toastTimer = null

function hideToast() {
  toast.value.show = false
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = null
}

// dùng: showToast('Đã thêm...', 'success') | showToast('Lỗi...', 'error', { title: 'Lỗi' })
function showToast(message, type = 'success', opts = {}) {
  const title = opts.title ?? (type === 'success' ? 'Thành công' : 'Lỗi')
  const duration = opts.duration ?? 2200
  toast.value = { show: true, title, message, type }
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value.show = false), duration)
}

/* ===== Confirm ===== */
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

/* ===== Form ===== */
const product = reactive({
  maSanPham: 'SP' + Math.floor(Math.random() * 10000),
  tenSanPham: '',
  loaiSanPhamId: '',
  thuongHieuId: '',
  soKhuyId: '',
  kieuTuiId: '',
  veAoId: '',
  xeTaId: '',
  xuatXuId: '',
  fitId: '',
  chatLieuId: '',
  trangThai: true,
  moTa: '',
  anh: ''
})

/* ===== Attributes ===== */
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


// ===== Select2-like (vue-multiselect) bindings =====
function bindMs(productKey, listKey) {
  return computed({
    get: () => (attributes[listKey] || []).find((i) => String(i.id) === String(product[productKey])) || null,
    set: (val) => {
      product[productKey] = val ? val.id : ''
    }
  })
}

const msLoaiSanPham = bindMs('loaiSanPhamId', 'loaiSanPham')
const msThuongHieu = bindMs('thuongHieuId', 'thuongHieu')
const msSoKhuy = bindMs('soKhuyId', 'soKhuy')
const msKieuTui = bindMs('kieuTuiId', 'kieuTui')
const msVeAo = bindMs('veAoId', 'veAo')
const msXeTa = bindMs('xeTaId', 'xeTa')
const msXuatXu = bindMs('xuatXuId', 'xuatXu')
const msFit = bindMs('fitId', 'fit')
const msChatLieu = bindMs('chatLieuId', 'chatLieu')
onMounted(async () => {
  try {
    const promises = Object.keys(attributeMap).map((key) =>
      attributeService.getAllList(attributeMap[key]).then((res) => {
        attributes[key] = (res.data || []).filter((x) => x.trangThai === true)
      })
    )
    await Promise.all(promises)
  } catch (e) {
    console.error(e)
    globalError.value = 'Lỗi tải dữ liệu.'
    showToast('Lỗi tải dữ liệu thuộc tính', 'error')
  }
})

/* ===== Variants ===== */
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
  if (!selectedColors.value.length || !selectedSizes.value.length) {
    showToast('Vui lòng chọn ít nhất 1 màu và 1 kích cỡ!', 'error')
    return
  }

  const add = []
  for (const c of selectedColors.value) {
    for (const s of selectedSizes.value) {
      const exists = generatedVariants.value.some((v) => v.idMauSac === c.id && v.idKichCo === s.id)
      if (!exists) {
        add.push({
          id: Date.now() + Math.random(),
          idMauSac: c.id,
          tenMauSac: c.ten,
          idKichCo: s.id,
          tenKichCo: s.soSize,
          soLuongTon: 10,
          donGia: 0,
          anh: '',
          chatLieuId: product.chatLieuId
        })
      }
    }
  }

  generatedVariants.value = [...generatedVariants.value, ...add]
  showToast('Đã tạo biến thể tự động!')
}

function removeVariantObj(variant) {
  const idx = generatedVariants.value.indexOf(variant)
  if (idx > -1) generatedVariants.value.splice(idx, 1)
}

const variantsGroupedByColor = computed(() => {
  const groups = {}
  for (const v of generatedVariants.value) {
    if (!groups[v.idMauSac]) groups[v.idMauSac] = { id: v.idMauSac, name: v.tenMauSac, variants: [] }
    groups[v.idMauSac].variants.push(v)
  }
  return Object.values(groups)
})

async function handleGroupImageUpload(group, event) {
  const file = event.target.files?.[0]
  if (!file) return
  try {
    const res = await uploadImage(file)
    const url = res.data.url
    group.variants.forEach((v) => (v.anh = url))
    showToast(`Upload ảnh màu "${group.name}" thành công!`)
  } catch (e) {
    console.error(e)
    showToast('Lỗi upload ảnh', 'error')
  }
}

/* ===== Group Apply ===== */
const showGroupApplyModal = ref(false)
const currentApplyGroup = ref(null)
const groupApplyData = reactive({ soLuongTon: '', donGia: '' })

function openGroupApply(group) {
  currentApplyGroup.value = group
  groupApplyData.soLuongTon = ''
  groupApplyData.donGia = ''
  showGroupApplyModal.value = true
}
function openGlobalApply() {
  openGroupApply({ name: 'Tất cả biến thể', variants: generatedVariants.value })
}
function confirmGroupApply() {
  if (!currentApplyGroup.value) return

  currentApplyGroup.value.variants.forEach((v) => {
    if (groupApplyData.soLuongTon !== '') v.soLuongTon = Number(groupApplyData.soLuongTon)
    if (groupApplyData.donGia !== '') v.donGia = Number(groupApplyData.donGia)
  })

  const isAll = currentApplyGroup.value.name === 'Tất cả biến thể'
  showGroupApplyModal.value = false
  currentApplyGroup.value = null

  showToast(isAll ? 'Đã áp dụng cho tất cả biến thể' : 'Đã áp dụng cho nhóm biến thể')
}

/* ===== Quick Add ===== */
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
  fit: 'FIT'
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
  const next = (nums.length ? Math.max(...nums) : 0) + 1
  return `${prefix}${pad2(next)}`
}



async function onTagCreate(typeCode, listKey, productKey, name) {
  const ten = String(name || '').trim()
  if (!ten) return

  // nếu đã tồn tại thì chọn luôn
  const existed = (attributes[listKey] || []).find((x) => String(x.ten || '').toLowerCase() === ten.toLowerCase())
  if (existed) {
    product[productKey] = existed.id
    return
  }

  try {
    const ma = genNextAttrCode(typeCode, attributes[listKey] || [])
    const res = await attributeService.create(typeCode, { ma, ten, trangThai: true })
    const created = res?.data
    if (!created?.id) throw new Error('Create attribute failed')

    attributes[listKey] = [...(attributes[listKey] || []), created]
    product[productKey] = created.id
    showToast(`Đã thêm "${ten}"!`)
  } catch (e) {
    console.error(e)
    showToast('Không thể thêm nhanh. Vui lòng thử lại!', 'error')
  }
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

function openQuickAdd(key, typeCode) {
  quickAddTarget.key = key
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
    showToast('Vui lòng nhập giá trị!', 'error')
    return
  }

  try {
    // lấy ALL để tính mã tiếp theo
    const allRes = await attributeService.getAllList(quickAddTarget.typeCode)
    const allList = allRes.data || []
    const nextMa = genNextAttrCode(quickAddTarget.typeCode, allList)

    const payload =
      quickAddTarget.typeCode === 'kich-co'
        ? { ma: nextMa, soSize: Number(raw), trangThai: true }
        : { ma: nextMa, ten: raw, trangThai: true }

    await attributeService.create(quickAddTarget.typeCode, payload)

    // reload list active
    const res = await attributeService.getAllList(quickAddTarget.typeCode)
    const activeList = (res.data || []).filter((x) => x.trangThai === true)
    attributes[quickAddTarget.key] = activeList

    // tìm item mới và auto chọn
    const newItem =
      quickAddTarget.typeCode === 'kich-co'
        ? activeList.find((x) => Number(x.soSize) === Number(raw))
        : activeList.find((x) => String(x.ten).trim() === raw)

    const modelKey = quickAddTarget.key + 'Id'
    if (newItem && modelKey in product) product[modelKey] = newItem.id

    closeQuickAdd()

    const valueText = quickAddTarget.typeCode === 'kich-co' ? String(Number(raw)) : raw
    showToast(`Đã thêm nhanh "${valueText}" vào ${quickAddLabel.value}`)
  } catch (e) {
    console.error(e)
    showToast('Lỗi thêm mới thuộc tính', 'error')
  }
}

/* ===== Attr modal (màu/size) ===== */
const showAttrModal = ref(false)
const currentAttrType = ref('')
const tempSelectedIds = ref(new Set())

function openAttrModal(type) {
  currentAttrType.value = type
  tempSelectedIds.value = new Set()
  const currentList = type === 'mauSac' ? selectedColors.value : selectedSizes.value
  currentList.forEach((i) => tempSelectedIds.value.add(i.id))
  showAttrModal.value = true
}
function toggleAttrSelection(item) {
  if (tempSelectedIds.value.has(item.id)) tempSelectedIds.value.delete(item.id)
  else tempSelectedIds.value.add(item.id)
}
function confirmAttrSelection() {
  const list = attributes[currentAttrType.value] || []
  const selected = list.filter((i) => tempSelectedIds.value.has(i.id))
  if (currentAttrType.value === 'mauSac') selectedColors.value = selected
  else selectedSizes.value = selected
  showAttrModal.value = false
}

/* ===== Validate ===== */
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
  fitId: '',
  chatLieuId: ''
})

function validate() {
  Object.keys(errors).forEach((k) => (errors[k] = ''))
  let ok = true

  if (!product.tenSanPham) {
    errors.tenSanPham = 'Tên sản phẩm bắt buộc'
    ok = false
  }
  if (!product.maSanPham) {
    errors.maSanPham = 'Mã sản phẩm bắt buộc'
    ok = false
  }

  const req = ['loaiSanPhamId', 'thuongHieuId', 'soKhuyId', 'kieuTuiId', 'veAoId', 'xeTaId', 'xuatXuId', 'fitId', 'chatLieuId']
  for (const k of req) {
    if (!product[k]) {
      errors[k] = 'Bắt buộc'
      ok = false
    }
  }
  return ok
}

/* ===== Submit ===== */
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
    cancelText: 'Hủy'
  })
  if (!ok) return

  await doSubmit()
}

async function doSubmit() {
  const variantsPayload = generatedVariants.value.map((v) => {
    const clId = v.chatLieuId || product.chatLieuId
    const cl = attributes.chatLieu.find((x) => x.id === clId)
    return {
      idKichCo: v.idKichCo,
      idMauSac: v.idMauSac,
      soLuongTon: Number(v.soLuongTon ?? 0),
      donGia: Number(v.donGia ?? 0),
      anh: v.anh,
      ghiChu: '',
      trangThai: true,
      chatLieu: cl ? cl.ten : ''
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

    showToast(isEditMode.value ? 'Cập nhật sản phẩm thành công!' : 'Thêm sản phẩm thành công!')
    setTimeout(() => router.push('/products'), 600)
  } catch (e) {
    console.error(e)
    globalError.value = 'Có lỗi xảy ra.'
    showToast('Không thể hoàn tất. Vui lòng thử lại!', 'error', { title: 'Lỗi' })
  } finally {
    loading.value = false
  }
}

/* ===== Clear variants ===== */
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
  showToast('Đã xóa tất cả biến thể')
}

/* ===== Navigation ===== */
function goBack() {
  router.push('/products')
}

/* ===== Color helper ===== */
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
  const m = n.match(/\(([^)]+)\)/)
  if (m) return m[1]
  return '#e5e7eb'
}
</script>

<style scoped>
/* ===== Page fixed height (tránh khoảng trắng) ===== */
:global(html, body, #app) { height: 100%; margin: 0; }
.page {
  height: 100dvh;
  overflow: hidden;
  padding: 16px;
  background: #f3f4f6;
}

.card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 14px 16px;
  margin-bottom: 12px;
}

.card-body {
  flex: 1;
  overflow: auto;
  padding-right: 6px;
}

.action-bar {
  margin-top: 12px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 14px 16px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  box-shadow: 0 6px 20px rgba(0,0,0,0.06);
}

/* ===== Base UI ===== */
.title { font-size: 1.35rem; font-weight: 800; color: #374151; }
.flex-between { display: flex; justify-content: space-between; align-items: center; gap: 12px; }

.section {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
}
.section-title {
  margin: 0 0 14px;
  font-size: 1.05rem;
  font-weight: 800;
  color: #1f2937;
  border-bottom: 1px solid #f3f4f6;
  padding-bottom: 10px;
}

.grid-3 { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; }
.grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
@media (max-width: 1100px) { .grid-3 { grid-template-columns: 1fr 1fr; } }
@media (max-width: 720px) { .grid-3, .grid-2 { grid-template-columns: 1fr; } }

.form-group { margin-bottom: 12px; }
.form-input {
  width: 100%;
  padding: 10px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  box-sizing: border-box;
}


/* ===== select2-like (vue-multiselect) ===== */
.ms-wrap :deep(.multiselect__tags){
  min-height: 40px;
  padding: 6px 40px 0 10px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  box-sizing: border-box;
}
.ms-wrap.error-border :deep(.multiselect__tags){
  border-color: #ef4444;
  box-shadow: 0 0 0 1px rgba(239,68,68,0.2);
}
.ms-wrap :deep(.multiselect__single){
  margin-top: 3px;
  font-size: 0.95rem;
}
.ms-wrap :deep(.multiselect__input){
  margin-top: 3px;
}
.ms-wrap :deep(.multiselect__select){
  height: 38px;
}

.required::after { content: ' *'; color: red; }
.error-text { color: red; font-size: 0.8rem; }
.error-border { border-color: #ef4444 !important; box-shadow: 0 0 0 1px rgba(239,68,68,0.2); }

.btn {
  padding: 10px 16px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-weight: 700;
}
.btn-secondary { background: #e5e7eb; color: #374151; }
.btn-primary { background: #1e3a8a; color: #fff; }
.btn-orange { background: #1e40af; color: #fff; }
.full-width-btn { width: 100%; margin-top: 8px; }

.flex-row-gap { display: flex; gap: 8px; align-items: center; }

.btn-quick-add {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  background: #f9fafb;
  font-size: 1.2rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.btn-quick-add:hover { background: #e5e7eb; border-color: #1e3a8a; color: #1e3a8a; }

.mt-2 { margin-top: 10px; }
.text-center { text-align: center; }

/* ===== Selected list ===== */
.selected-list { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 10px; }
.selected-item-block {
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 6px 10px;
  background: #fff;
}
.color-square { width: 28px; height: 28px; border-radius: 6px; border: 1px solid rgba(0,0,0,0.1); }
.size-square { font-weight: 900; padding: 0 6px; }
.item-name-input {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 4px 8px;
  width: 110px;
  background: #f9fafb;
}

.btn-remove-block {
  background: #1e40af;
  color: #fff;
  border: none;
  border-radius: 6px;
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
}

/* ===== Variants ===== */
.section-header-bar {
  background: #1e40af;
  padding: 10px 14px;
  border-radius: 10px 10px 0 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.section-title-white { margin: 0; color: #fff; font-size: 1rem; font-weight: 900; }
.btn-outline-white {
  border: 1px solid rgba(255,255,255,0.5);
  background: rgba(255,255,255,0.2);
  color: #fff;
}
.btn-outline-danger {
  border: 1px solid #fecaca;
  background: white;
  color: #1e40af;
}
.bulk-actions { display: flex; gap: 10px; }

.variant-group {
  margin-top: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
}
.group-header {
  background: white;
  padding: 10px 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e5e7eb;
}
.group-title { font-weight: 900; color: #1e40af; display: flex; align-items: center; gap: 8px; }
.color-dot-lg { width: 16px; height: 16px; border-radius: 50%; display: inline-block; }
.count-gray { color: #6b7280; font-weight: 700; font-size: 0.9rem; }

.table-responsive { overflow-x: auto; }
.variants-table { width: 100%; border-collapse: collapse; }
.variants-table th { background: #f9fafb; padding: 10px; text-align: left; border-bottom: 1px solid #e5e7eb; }
.variants-table td { padding: 10px; border-bottom: 1px solid #e5e7eb; vertical-align: middle; }

.size-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 38px;
  border: 1px solid #d1d5db;
  background: #f3f4f6;
  border-radius: 8px;
  font-weight: 900;
}

.btn-icon {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
}
.btn-icon.danger { background: #fee2e2; color: #991b1b; }
.btn-icon.danger:hover { background: #fecaca; }

.btn-quick-add-blue {
  background: #1e40af;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 6px 12px;
  font-size: 0.85rem;
  font-weight: 800;
  cursor: pointer;
}
.btn-quick-add-blue:hover { background: #1e3a8a; }

/* ===== Image upload ===== */
.section-image-upload { margin-top: 14px; padding-top: 14px; border-top: 1px solid #e5e7eb; }
.section-title-sm { margin: 0 0 12px; font-weight: 900; }
.image-upload-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; }
.image-upload-card { border: 1px solid #e5e7eb; border-radius: 10px; overflow: hidden; background: #fff; }
.card-upload-header { padding: 10px; background: #f9fafb; border-bottom: 1px solid #e5e7eb; font-weight: 800; display: flex; align-items: center; gap: 8px; }

.upload-area { height: 150px; display: flex; align-items: center; justify-content: center; cursor: pointer; background: #fdfdfd; }
.upload-area:hover { background: #f3f4f6; }
.upload-placeholder { display: flex; flex-direction: column; align-items: center; gap: 6px; color: #9ca3af; font-weight: 700; }
.icon-lg { font-size: 2rem; }
.preview-box-lg { width: 100%; height: 100%; overflow: hidden; }
.preview-box-lg img { width: 100%; height: 100%; object-fit: contain; }

/* ===== Modal ===== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-box {
  background: #fff;
  border-radius: 12px;
  padding: 18px;
  width: 420px;
  max-width: calc(100vw - 24px);
  box-shadow: 0 10px 30px rgba(0,0,0,0.16);
}
.modal-lg { width: 620px; }
.modal-title { margin: 0 0 12px; font-weight: 900; color: #111827; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 14px; }

.attr-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 14px;
  max-height: 400px;
  overflow: auto;
}
.attr-item {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 10px;
  min-height: 84px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: 0.15s;
}
.attr-item:hover { border-color: #1e40af; background: white; }
.attr-item.selected { border-color: #1e40af; background: white; box-shadow: 0 0 0 1px #1e40af; }

.attr-color-preview { width: 32px; height: 32px; border-radius: 10px; border: 1px solid rgba(0,0,0,0.1); margin-bottom: 6px; }
.attr-name { font-size: 0.85rem; font-weight: 800; color: #374151; }
.attr-name-lg { font-size: 1.1rem; font-weight: 900; color: #374151; }

/* ===== Confirm ===== */
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
  border-radius: 12px;
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
.confirm-header h3{ margin: 0; font-size: 1.05rem; font-weight: 900; color: #111827; }
.close-btn{ border: none; background: transparent; font-size: 22px; cursor: pointer; color: #6b7280; }
.confirm-body{ padding: 16px; color: #374151; }
.confirm-actions{ display: flex; justify-content: flex-end; gap: 10px; padding: 0 16px 16px; }
.btn-danger{ background: #ef4444; color: #fff; }
.btn-danger:hover{ background: #dc2626; }

/* ===== Toast giống ảnh ===== */
.toast2{
  position: fixed;
  top: 16px;
  right: 16px; /* muốn góc phải: đổi left -> right và set left:auto */
  display: flex;
  align-items: flex-start;
  gap: 12px;
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.14);
  z-index: 99999;
  min-width: 360px;
  max-width: min(520px, calc(100vw - 32px));
}
.toast2-bar{
  width: 6px;
  border-radius: 10px;
  align-self: stretch;
  background: #22c55e;
}
.toast2-main{ flex: 1; padding-top: 2px; }
.toast2-title{ font-weight: 900; margin-bottom: 4px; color: #16a34a; }
.toast2-msg{ color: #374151; line-height: 1.35; font-weight: 600; }
.toast2-close{
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 18px;
  line-height: 1;
  color: #6b7280;
  padding: 2px 6px;
  border-radius: 8px;
}
.toast2-close:hover{ background: #f3f4f6; }
.toast2.error .toast2-bar{ background: #ef4444; }
.toast2.error .toast2-title{ color: #dc2626; }

.error-msg { margin-top: 10px; color: #b02a37; font-weight: 900; }
</style>
