<template>
  <div class="page">
    <div class="card">
      <div class="card-header flex-between">
        <h2 class="title">{{ isEditMode ? 'Cập nhật sản phẩm' : 'Thêm sản phẩm mới' }}</h2>
        <button class="btn btn-secondary" type="button" @click="goBack">← Quay lại danh sách</button>
      </div>

      <div class="card-body">
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
              <div class="field-row">
                <div :class="['ms-wrap', { 'error-border': errors.loaiSanPhamId }]">
                  <Multiselect
                    v-model="msLoaiSanPham"
                    :options="attributes.loaiSanPham"
                    track-by="id"
                    label="ten"
                    placeholder="-- Chọn loại sản phẩm --"
                    :searchable="true"
                    :taggable="false"
                    :show-labels="false"
                  >
                    <template #noResult><span class="ms-empty">Không tìm thấy.</span></template>
                    <template #noOptions><span class="ms-empty">Chưa có dữ liệu.</span></template>
                  </Multiselect>
                </div>

                <button
                  class="btn-plus"
                  type="button"
                  title="Thêm nhanh"
                  @click="openAddModal({ typeCode:'loai-san-pham', listKey:'loaiSanPham', productKey:'loaiSanPhamId', label:'Loại sản phẩm' })"
                >
                  +
                </button>
              </div>
              <small v-if="errors.loaiSanPhamId" class="error-text">{{ errors.loaiSanPhamId }}</small>
            </div>

            <div class="form-group">
              <label class="required">Thương hiệu</label>
              <div class="field-row">
                <div :class="['ms-wrap', { 'error-border': errors.thuongHieuId }]">
                  <Multiselect
                    v-model="msThuongHieu"
                    :options="attributes.thuongHieu"
                    track-by="id"
                    label="ten"
                    placeholder="-- Chọn thương hiệu --"
                    :searchable="true"
                    :taggable="false"
                    :show-labels="false"
                  >
                    <template #noResult><span class="ms-empty">Không tìm thấy.</span></template>
                    <template #noOptions><span class="ms-empty">Chưa có dữ liệu.</span></template>
                  </Multiselect>
                </div>

                <button
                  class="btn-plus"
                  type="button"
                  title="Thêm nhanh"
                  @click="openAddModal({ typeCode:'thuong-hieu', listKey:'thuongHieu', productKey:'thuongHieuId', label:'Thương hiệu' })"
                >
                  +
                </button>
              </div>
              <small v-if="errors.thuongHieuId" class="error-text">{{ errors.thuongHieuId }}</small>
            </div>

            <div class="form-group">
              <label class="required">Số khuy</label>
              <div class="field-row">
                <div :class="['ms-wrap', { 'error-border': errors.soKhuyId }]">
                  <Multiselect
                    v-model="msSoKhuy"
                    :options="attributes.soKhuy"
                    track-by="id"
                    label="ten"
                    placeholder="-- Chọn số khuy --"
                    :searchable="true"
                    :taggable="false"
                    :show-labels="false"
                  >
                    <template #noResult><span class="ms-empty">Không tìm thấy.</span></template>
                    <template #noOptions><span class="ms-empty">Chưa có dữ liệu.</span></template>
                  </Multiselect>
                </div>

                <button
                  class="btn-plus"
                  type="button"
                  title="Thêm nhanh"
                  @click="openAddModal({ typeCode:'so-khuy', listKey:'soKhuy', productKey:'soKhuyId', label:'Số khuy' })"
                >
                  +
                </button>
              </div>
              <small v-if="errors.soKhuyId" class="error-text">{{ errors.soKhuyId }}</small>
            </div>

            <div class="form-group">
              <label class="required">Kiểu túi</label>
              <div class="field-row">
                <div :class="['ms-wrap', { 'error-border': errors.kieuTuiId }]">
                  <Multiselect
                    v-model="msKieuTui"
                    :options="attributes.kieuTui"
                    track-by="id"
                    label="ten"
                    placeholder="-- Chọn kiểu túi --"
                    :searchable="true"
                    :taggable="false"
                    :show-labels="false"
                  >
                    <template #noResult><span class="ms-empty">Không tìm thấy.</span></template>
                    <template #noOptions><span class="ms-empty">Chưa có dữ liệu.</span></template>
                  </Multiselect>
                </div>

                <button
                  class="btn-plus"
                  type="button"
                  title="Thêm nhanh"
                  @click="openAddModal({ typeCode:'kieu-tui', listKey:'kieuTui', productKey:'kieuTuiId', label:'Kiểu túi' })"
                >
                  +
                </button>
              </div>
              <small v-if="errors.kieuTuiId" class="error-text">{{ errors.kieuTuiId }}</small>
            </div>

            <div class="form-group">
              <label class="required">Ve áo</label>
              <div class="field-row">
                <div :class="['ms-wrap', { 'error-border': errors.veAoId }]">
                  <Multiselect
                    v-model="msVeAo"
                    :options="attributes.veAo"
                    track-by="id"
                    label="ten"
                    placeholder="-- Chọn ve áo --"
                    :searchable="true"
                    :taggable="false"
                    :show-labels="false"
                  >
                    <template #noResult><span class="ms-empty">Không tìm thấy.</span></template>
                    <template #noOptions><span class="ms-empty">Chưa có dữ liệu.</span></template>
                  </Multiselect>
                </div>

                <button
                  class="btn-plus"
                  type="button"
                  title="Thêm nhanh"
                  @click="openAddModal({ typeCode:'ve-ao', listKey:'veAo', productKey:'veAoId', label:'Ve áo' })"
                >
                  +
                </button>
              </div>
              <small v-if="errors.veAoId" class="error-text">{{ errors.veAoId }}</small>
            </div>

            <div class="form-group">
              <label class="required">Xẻ tà</label>
              <div class="field-row">
                <div :class="['ms-wrap', { 'error-border': errors.xeTaId }]">
                  <Multiselect
                    v-model="msXeTa"
                    :options="attributes.xeTa"
                    track-by="id"
                    label="ten"
                    placeholder="-- Chọn xẻ tà --"
                    :searchable="true"
                    :taggable="false"
                    :show-labels="false"
                  >
                    <template #noResult><span class="ms-empty">Không tìm thấy.</span></template>
                    <template #noOptions><span class="ms-empty">Chưa có dữ liệu.</span></template>
                  </Multiselect>
                </div>

                <button
                  class="btn-plus"
                  type="button"
                  title="Thêm nhanh"
                  @click="openAddModal({ typeCode:'xe-ta', listKey:'xeTa', productKey:'xeTaId', label:'Xẻ tà' })"
                >
                  +
                </button>
              </div>
              <small v-if="errors.xeTaId" class="error-text">{{ errors.xeTaId }}</small>
            </div>

            <div class="form-group">
              <label class="required">Xuất xứ</label>
              <div class="field-row">
                <div :class="['ms-wrap', { 'error-border': errors.xuatXuId }]">
                  <Multiselect
                    v-model="msXuatXu"
                    :options="attributes.xuatXu"
                    track-by="id"
                    label="ten"
                    placeholder="-- Chọn xuất xứ --"
                    :searchable="true"
                    :taggable="false"
                    :show-labels="false"
                  >
                    <template #noResult><span class="ms-empty">Không tìm thấy.</span></template>
                    <template #noOptions><span class="ms-empty">Chưa có dữ liệu.</span></template>
                  </Multiselect>
                </div>

                <button
                  class="btn-plus"
                  type="button"
                  title="Thêm nhanh"
                  @click="openAddModal({ typeCode:'xuat-xu', listKey:'xuatXu', productKey:'xuatXuId', label:'Xuất xứ' })"
                >
                  +
                </button>
              </div>
              <small v-if="errors.xuatXuId" class="error-text">{{ errors.xuatXuId }}</small>
            </div>

            <div class="form-group">
              <label class="required">Kiểu dáng</label>
              <div class="field-row">
                <div :class="['ms-wrap', { 'error-border': errors.fitId }]">
                  <Multiselect
                    v-model="msFit"
                    :options="attributes.fit"
                    track-by="id"
                    label="ten"
                    placeholder="-- Chọn kiểu dáng --"
                    :searchable="true"
                    :taggable="false"
                    :show-labels="false"
                  >
                    <template #noResult><span class="ms-empty">Không tìm thấy.</span></template>
                    <template #noOptions><span class="ms-empty">Chưa có dữ liệu.</span></template>
                  </Multiselect>
                </div>

                <button
                  class="btn-plus"
                  type="button"
                  title="Thêm nhanh"
                  @click="openAddModal({ typeCode:'fit', listKey:'fit', productKey:'fitId', label:'Kiểu dáng' })"
                >
                  +
                </button>
              </div>
              <small v-if="errors.fitId" class="error-text">{{ errors.fitId }}</small>
            </div>

            <div class="form-group">
              <label class="required">Chất liệu</label>
              <div class="field-row">
                <div :class="['ms-wrap', { 'error-border': errors.chatLieuId }]">
                  <Multiselect
                    v-model="msChatLieu"
                    :options="attributes.chatLieu"
                    track-by="id"
                    label="ten"
                    placeholder="-- Chọn chất liệu --"
                    :searchable="true"
                    :taggable="false"
                    :show-labels="false"
                  >
                    <template #noResult><span class="ms-empty">Không tìm thấy.</span></template>
                    <template #noOptions><span class="ms-empty">Chưa có dữ liệu.</span></template>
                  </Multiselect>
                </div>

                <button
                  class="btn-plus"
                  type="button"
                  title="Thêm nhanh"
                  @click="openAddModal({ typeCode:'chat-lieu', listKey:'chatLieu', productKey:'chatLieuId', label:'Chất liệu' })"
                >
                  +
                </button>
              </div>
              <small v-if="errors.chatLieuId" class="error-text">{{ errors.chatLieuId }}</small>
            </div>
          </div>

          <div class="form-group mt-2">
            <label>Mô tả sản phẩm</label>
            <textarea
              v-model="product.moTa"
              class="form-input"
              rows="4"
              placeholder="Nhập mô tả chi tiết..."
              :class="{ 'error-border': errors.moTa }"
            ></textarea>
            <small v-if="errors.moTa" class="error-text">{{ errors.moTa }}</small>
          </div>
        </div>

        <div class="section">
          <h3 class="section-title">Biến thể sản phẩm</h3>

          <div class="grid-2">
            <div class="form-group">
              <label class="required">Màu sắc</label>
              <div :class="['ms-wrap', 'ms-wrap-multi', { 'error-border': errors.mauSac }]">
                <Multiselect
                  v-model="selectedColors"
                  :options="attributes.mauSac"
                  track-by="id"
                  label="ten"
                  placeholder="Tìm và chọn màu sắc"
                  :searchable="true"
                  :multiple="true"
                  :close-on-select="false"
                  :show-labels="false"
                  :taggable="false"
                >
                  <template #noResult><span class="ms-empty">Không tìm thấy.</span></template>
                  <template #noOptions><span class="ms-empty">Chưa có dữ liệu.</span></template>

                  <template #option="{ option }">
                    <div class="opt-row">
                      <span class="color-dot" :style="{ background: getColorCode(option.ten) }"></span>
                      <span>{{ option.ten }}</span>
                    </div>
                  </template>

                  <template #tag="{ option, remove }">
                    <span class="ms-tag">
                      <span class="color-dot" :style="{ background: getColorCode(option.ten) }"></span>
                      {{ option.ten }}
                      <span class="ms-tag-x" @click.stop="remove(option)">×</span>
                    </span>
                  </template>
                </Multiselect>
              </div>
              <small v-if="errors.mauSac" class="error-text">{{ errors.mauSac }}</small>
            </div>

            <div class="form-group">
              <label class="required">Kích cỡ</label>
              <div :class="['ms-wrap', 'ms-wrap-multi', { 'error-border': errors.kichCo }]">
                <Multiselect
                  v-model="selectedSizes"
                  :options="attributes.kichCo"
                  track-by="id"
                  label="soSize"
                  placeholder="Tìm và chọn kích cỡ"
                  :searchable="true"
                  :multiple="true"
                  :close-on-select="false"
                  :show-labels="false"
                  :taggable="false"
                  @select="onSizeSelect"
                >
                  <template #noResult><span class="ms-empty">Không tìm thấy.</span></template>
                  <template #noOptions><span class="ms-empty">Chưa có dữ liệu.</span></template>

                  <template #option="{ option }">
                    <div class="opt-row" :class="{ 'opt-invalid': isSizeInvalid(option) }">
                      <span>{{ option.soSize }}</span>
                      <small v-if="isSizeInvalid(option) && showPolicyHint" class="size-hint">
                        (Hợp lệ: {{ allowedSizeText }})
                      </small>
                    </div>
                  </template>

                  <template #tag="{ option, remove }">
                    <span class="ms-tag" :class="{ 'tag-invalid': isSizeInvalid(option) }">
                      {{ option.soSize }}
                      <span class="ms-tag-x" @click.stop="remove(option)">×</span>
                    </span>
                  </template>
                </Multiselect>
              </div>

              <small v-if="errors.kichCo" class="error-text">{{ errors.kichCo }}</small>

              <small v-if="showPolicyHint" class="hint">
                Kích cỡ đang áp dụng cho
                <b>{{ policyTargetText }}</b>:
                <b>{{ allowedSizeText }}</b>
              </small>
            </div>
          </div>

          <button class="btn btn-orange full-width-btn" type="button" @click="generateVariants">
            ⚡ Tạo biến thể tự động
          </button>
        </div>

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
              <button class="btn-quick-add-blue" type="button" @click="openGroupApply(g)">⚡ Áp dụng nhóm</button>
            </div>

            <div class="table-responsive">
              <table class="variants-table">
                <thead>
                  <tr>
                    <th style="width:28%; text-align:center;">Kích cỡ</th>
                    <th style="width:34%;">Số lượng tồn</th>
                    <th style="width:34%;">Đơn giá</th>
                    <th style="width:4%;"></th>
                  </tr>
                </thead>

                <tbody>
                  <tr v-for="v in g.variants" :key="v._key">
                    <td class="text-center"><span class="size-badge">{{ v.tenKichCo }}</span></td>

                    <td>
                      <input class="form-input" type="number" min="0" step="1" v-model.number="v.soLuongTon" />
                      <small v-if="v._qtyErr" class="error-text">{{ v._qtyErr }}</small>
                    </td>

                    <td>
                      <input
                        class="form-input"
                        type="text"
                        inputmode="numeric"
                        placeholder="0"
                        :value="v.donGiaText"
                        @input="(e) => onMoneyInputVariant(e, v)"
                        @blur="() => normalizeMoneyVariant(v)"
                      />
                      <small v-if="v._priceErr" class="error-text">{{ v._priceErr }}</small>
                    </td>

                    <td class="text-center">
                      <button class="btn-icon danger" type="button" @click="removeVariantObj(v)">×</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="section-image-upload" v-if="variantsGroupedByColor.length > 0">
            <h3 class="section-title-sm">📸 Ảnh theo màu sắc</h3>

            <div class="image-upload-grid">
              <div v-for="g in variantsGroupedByColor" :key="'img-' + g.id" class="image-upload-card">
                <div class="card-upload-header">
                  <span class="color-dot-lg" :style="{ backgroundColor: getColorCode(g.name) }"></span>
                  {{ g.name }}
                </div>

                <label class="upload-area">
                  <div class="preview-box-lg" v-if="g.variants[0]?.anh">
                    <img :src="resolveMediaUrl(g.variants[0].anh)" />
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

      <div class="action-bar">
        <button class="btn btn-secondary" type="button" @click="goBack">Hủy</button>
        <button class="btn btn-primary" type="button" :disabled="loading" @click="handleSubmitClick">
          {{ loading ? 'Đang xử lý...' : (isEditMode ? 'Lưu thay đổi' : 'Hoàn tất') }}
        </button>
      </div>
    </div>

    <div v-if="addModal.open" class="modal-overlay" @click.self="closeAddModal">
      <div class="modal-box">
        <h3 class="modal-title">Thêm nhanh {{ addModal.label }}</h3>

        <div class="form-group">
          <label>Nhập tên</label>
          <input class="form-input" v-model="addModal.value" placeholder="Nhập..." @keyup.enter="confirmAddModal" />
          <small v-if="addModal.error" class="error-text">{{ addModal.error }}</small>
        </div>

        <div class="modal-actions">
          <button class="btn btn-secondary" type="button" @click="closeAddModal">Hủy</button>
          <button class="btn btn-primary" type="button" :disabled="!addModalCanSubmit" @click="confirmAddModal">
            Thêm
          </button>
        </div>
      </div>
    </div>

    <div v-if="showApplyModal" class="modal-overlay" @click.self="closeApplyModal">
      <div class="modal-box">
        <h3 class="modal-title">Áp dụng chung ({{ applyTarget?.name }})</h3>

        <div class="grid-2">
          <div class="form-group">
            <label>Số lượng tồn</label>
            <input class="form-input" type="number" min="0" step="1" placeholder="Giữ nguyên" v-model="applyForm.qty" />
            <small v-if="applyForm._qtyErr" class="error-text">{{ applyForm._qtyErr }}</small>
          </div>

          <div class="form-group">
            <label>Đơn giá</label>
            <input
              class="form-input"
              type="text"
              inputmode="numeric"
              placeholder="Giữ nguyên"
              :value="applyForm.priceText"
              @input="onMoneyInputApply"
              @blur="normalizeMoneyApply"
            />
            <small v-if="applyForm._priceErr" class="error-text">{{ applyForm._priceErr }}</small>
          </div>
        </div>

        <div class="modal-actions">
          <button class="btn btn-secondary" type="button" @click="closeApplyModal">Hủy</button>
          <button class="btn btn-primary" type="button" @click="confirmApply">Áp dụng</button>
        </div>
      </div>
    </div>

    <div v-if="confirmState.open" class="confirm-overlay" @click.self="confirmCancel">
      <div class="confirm-modal">
        <div class="confirm-header">
          <h3>{{ confirmState.title }}</h3>
          <button class="close-btn" type="button" @click="confirmCancel">×</button>
        </div>
        <div class="confirm-body"><p>{{ confirmState.message }}</p></div>
        <div class="confirm-actions">
          <button class="btn btn-secondary" type="button" @click="confirmCancel" :disabled="loading">
            {{ confirmState.cancelText }}
          </button>
          <button class="btn" :class="confirmState.danger ? 'btn-danger' : 'btn-primary'" type="button" @click="confirmOk" :disabled="loading">
            {{ confirmState.okText }}
          </button>
        </div>
      </div>
    </div>

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
import { reactive, ref, computed, watch, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import Multiselect from 'vue-multiselect'
import 'vue-multiselect/dist/vue-multiselect.css'

import attributeService from '../../services/attributeService'
import { createSanPham, updateSanPham } from '../../services/sanPhamApi'
import { uploadImage } from '../../services/sanPhamChiTietApi'
import { normalizeUploadResponse, resolveMediaUrl } from '@/utils/media'

const router = useRouter()
const props = defineProps({ id: { type: [String, Number], default: null } })
const isEditMode = computed(() => !!props.id)

const loading = ref(false)
const globalError = ref('')

const toast = ref({ show: false, title: 'Thành công', message: '', type: 'success' })
let toastTimer = null

function hideToast() {
  toast.value.show = false
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = null
}

function showToast(message, type = 'success', opts = {}) {
  const title = opts.title ?? (type === 'success' ? 'Thành công' : 'Lỗi')
  const duration = opts.duration ?? 2200
  toast.value = { show: true, title, message, type }
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value.show = false), duration)
}

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

const MSG_LEADING_SPACE = 'Không được nhập khoảng trắng ở đầu'
const RE = {
  NAME: /^[\p{L}\p{M}\p{N} ]+$/u,
  DESC: /^[\p{L}\p{M}\p{N}\s.,;:!?()\-/%'"“”‘’]+$/u
}

function hasLeadingSpace(v) {
  return /^\s/.test(String(v ?? ''))
}

function cleanText(s) {
  return String(s ?? '').normalize('NFC').replace(/\s+/g, ' ').trim()
}

function cleanTextarea(s) {
  return String(s ?? '')
    .normalize('NFC')
    .split('\n')
    .map((line) => line.replace(/\s+/g, ' ').trim())
    .join('\n')
    .trim()
}

function validateInt0(label, n) {
  if (n === '' || n === null || n === undefined) return `${label} không được để trống`
  const x = Number(n)
  if (!Number.isFinite(x) || x < 0 || !Number.isInteger(x)) return `${label} phải là số nguyên >= 0`
  return true
}

function stripNonDigits(s) {
  return String(s ?? '').replace(/[^\d]/g, '')
}

function formatDotsFromDigits(digits) {
  if (!digits) return ''
  digits = digits.replace(/^0+(?=\d)/, '')
  return digits.replace(/\B(?=(\d{3})+(?!\d))/g, '.')
}

function parseMoneyToNumber(text) {
  const digits = stripNonDigits(text)
  if (!digits) return 0
  const n = Number(digits)
  return Number.isFinite(n) ? n : 0
}

function onMoneyInputVariant(e, v) {
  const raw = e?.target?.value ?? ''
  v._priceErr = ''
  if (hasLeadingSpace(raw)) v._priceErr = MSG_LEADING_SPACE
  const digits = stripNonDigits(raw)
  v.donGiaText = formatDotsFromDigits(digits)
  v.donGia = parseMoneyToNumber(v.donGiaText)
}

function normalizeMoneyVariant(v) {
  v.donGiaText = formatDotsFromDigits(stripNonDigits(v.donGiaText))
  v.donGia = parseMoneyToNumber(v.donGiaText)
}

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
  moTa: ''
})

const errors = reactive({
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
  mauSac: '',
  kichCo: '',
  moTa: ''
})

function clearErrors() {
  Object.keys(errors).forEach((k) => (errors[k] = ''))
}

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

function pickActiveOrAll(list) {
  const arr = Array.isArray(list) ? list : []
  const active = arr.filter((x) => x?.trangThai === true)
  return active.length ? active : arr
}

function getSizeValue(item) {
  const raw = item?.soSize ?? item?.ten ?? item?.name ?? ''
  const num = Number(String(raw).replace(',', '.'))
  return Number.isFinite(num) ? num : Number.MAX_SAFE_INTEGER
}

function sortSizes(list = []) {
  return [...list].sort((a, b) => {
    const byNumber = getSizeValue(a) - getSizeValue(b)
    if (byNumber !== 0) return byNumber
    return String(a?.soSize ?? a?.ten ?? '').localeCompare(String(b?.soSize ?? b?.ten ?? ''), 'vi')
  })
}

function bindMs(productKey, listKey) {
  return computed({
    get: () => (attributes[listKey] || []).find((i) => String(i.id) === String(product[productKey])) || null,
    set: (val) => (product[productKey] = val ? val.id : '')
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

const selectedColors = ref([])
const selectedSizes = ref([])

const sizePolicySeed = ref(Date.now() + Math.floor(Math.random() * 1000000))

function normKey(s) {
  return String(s || '')
    .trim()
    .toLowerCase()
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .replace(/\s+/g, ' ')
}

function stableHash(s) {
  let h = 2166136261
  const text = String(s || '')
  for (let i = 0; i < text.length; i += 1) {
    h ^= text.charCodeAt(i)
    h = Math.imul(h, 16777619)
  }
  return Math.abs(h)
}


function formatAllowedSizes(allowed) {
  if (!allowed) return 'chưa áp dụng'
  const arr = Array.from(allowed).sort((a, b) => a - b)
  if (arr.length === 0) return 'không có size phù hợp trong danh sách hiện có'
  if (arr.length === 1) return String(arr[0])

  const isStep1 = arr.every((v, i) => i === 0 || v - arr[i - 1] === 1)
  if (isStep1) return `${arr[0]}–${arr[arr.length - 1]}`
  return arr.join(', ')
}

const existingSizeNumbers = computed(() => {
  return sortSizes(attributes.kichCo || [])
    .map((x) => Number(x?.soSize ?? x?.ten))
    .filter((x) => Number.isFinite(x))
})


function getFitSizePool(sizes, fitKey) {
  const arr = Array.isArray(sizes) ? sizes : []
  if (arr.length <= 3) return arr

  const key = normKey(fitKey)
  if (key.includes('big') || key.includes('oversize') || key.includes('plus') || key.includes('rong')) {
    return arr.slice(Math.max(0, Math.floor(arr.length * 0.45)))
  }

  if (key.includes('slim') || key.includes('om') || key.includes('body')) {
    return arr.slice(0, Math.max(3, Math.ceil(arr.length * 0.7)))
  }

  if (key.includes('regular') || key.includes('reguler') || key.includes('classic') || key.includes('standard')) {
    const from = Math.max(0, Math.floor(arr.length * 0.15))
    const to = Math.min(arr.length, Math.ceil(arr.length * 0.85))
    return arr.slice(from, to)
  }

  return arr
}

function pickRandomSizes(sizes, seed) {
  const arr = Array.isArray(sizes) ? sizes : []
  if (!arr.length) return []
  if (arr.length <= 3) return arr

  const count = Math.min(arr.length, Math.max(2, Math.ceil(arr.length * 0.65)))
  const start = seed % arr.length
  const rotated = [...arr.slice(start), ...arr.slice(0, start)]

  return rotated.slice(0, count).sort((a, b) => a - b)
}

function buildDynamicSizeSet() {
  const existing = existingSizeNumbers.value
  if (!existing.length) return new Set()

  const pool = getFitSizePool(existing, fitNameNorm.value)
  const source = pool.length ? pool : existing
  const seed = stableHash(`${brandNameNorm.value}|${fitNameNorm.value}|${sizePolicySeed.value}|${existing.join(',')}`)

  return new Set(pickRandomSizes(source, seed))
}

const brandNameNorm = computed(() => normKey(msThuongHieu.value?.ten))
const fitNameNorm = computed(() => normKey(msFit.value?.ten))

const sizePolicyMeta = computed(() => {
  const hasBrand = !!product.thuongHieuId
  const hasFit = !!product.fitId
  if (!hasBrand && !hasFit) return { set: null, source: 'none' }

  const set = buildDynamicSizeSet()

  if (hasBrand && hasFit) return { set, source: 'both' }
  if (hasBrand) return { set, source: 'brand' }
  return { set, source: 'fit' }
})

const allowedSizeSet = computed(() => sizePolicyMeta.value.set)
const policySource = computed(() => sizePolicyMeta.value.source)
const showPolicyHint = computed(() => policySource.value !== 'none')
const allowedSizeText = computed(() => formatAllowedSizes(allowedSizeSet.value))
const policyTargetText = computed(() => {
  const brand = msThuongHieu.value?.ten?.trim()
  const fit = msFit.value?.ten?.trim()

  if (brand && fit) return `"${brand}" + "${fit}"`
  if (brand) return `"${brand}"`
  if (fit) return `"${fit}"`
  return 'thương hiệu/kiểu dáng'
})

function isSizeInvalid(option) {
  const allowed = allowedSizeSet.value
  if (!allowed) return false
  return !allowed.has(Number(option?.soSize))
}

async function onSizeSelect(option) {
  const allowed = allowedSizeSet.value
  if (!allowed) return

  const s = Number(option?.soSize)
  if (!Number.isFinite(s)) return

  if (!allowed.has(s)) {
    await nextTick()
    selectedSizes.value = selectedSizes.value.filter((x) => String(x.id) !== String(option.id))
    errors.kichCo = `Size ${s} không phù hợp. Hợp lệ: ${allowedSizeText.value}`
    showToast(errors.kichCo, 'error', { title: 'Không hợp lệ' })
  } else if (errors.kichCo) {
    errors.kichCo = ''
  }
}

watch(
  [
    () => product.thuongHieuId,
    () => product.fitId,
    () => (attributes.kichCo || []).map((x) => `${x?.id}-${x?.soSize ?? x?.ten}`).join('|')
  ],
  () => {
    if (product.thuongHieuId || product.fitId) {
      sizePolicySeed.value = Date.now() + Math.floor(Math.random() * 1000000)
    }

    const allowed = allowedSizeSet.value
    if (!allowed) return
    const invalid = selectedSizes.value.filter((x) => !allowed.has(Number(x.soSize)))
    if (invalid.length) {
      showToast(
        `Đang có size không phù hợp: ${invalid.map((x) => x.soSize).join(', ')}. Hợp lệ: ${allowedSizeText.value}`,
        'error',
        { title: 'Cảnh báo' }
      )
    }
  }
)

function ensureSizesValidOrShow() {
  const allowed = allowedSizeSet.value
  if (!allowed) return true
  const invalid = selectedSizes.value.filter((x) => !allowed.has(Number(x.soSize)))
  if (invalid.length) {
    errors.kichCo = `Size không hợp lệ: ${invalid.map((x) => x.soSize).join(', ')}. Hợp lệ: ${allowedSizeText.value}`
    showToast(errors.kichCo, 'error')
    return false
  }
  return true
}

const addModal = reactive({
  open: false,
  typeCode: '',
  listKey: '',
  productKey: '',
  label: '',
  value: '',
  error: ''
})

function openAddModal({ typeCode, listKey, productKey = '', label }) {
  addModal.open = true
  addModal.typeCode = typeCode
  addModal.listKey = listKey
  addModal.productKey = productKey
  addModal.label = label
  addModal.value = ''
  addModal.error = ''
}

function closeAddModal() {
  addModal.open = false
  addModal.value = ''
  addModal.error = ''
}

function normalizeCompareName(s) {
  return cleanText(s).toLowerCase()
}

function isDuplicateName(list, name) {
  const n = normalizeCompareName(name)
  return (list || []).some((x) => normalizeCompareName(x.ten || '') === n)
}

const CODE_PREFIX = {
  'mau-sac': 'MS',
  'thuong-hieu': 'TH',
  'chat-lieu': 'CL',
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

function computeAddModalError() {
  if (!addModal.open) return ''
  if (hasLeadingSpace(addModal.value)) return MSG_LEADING_SPACE

  const ten = cleanText(addModal.value)
  const list = attributes[addModal.listKey] || []

  if (!ten) return 'Vui lòng nhập tên'
  if (ten.length < 2) return 'Tên tối thiểu 2 ký tự'
  if (ten.length > 60) return 'Tên tối đa 60 ký tự'
  if (!RE.NAME.test(ten)) return 'Tên không được chứa ký tự đặc biệt'
  if (isDuplicateName(list, ten)) return 'Tên đã tồn tại'
  return ''
}

const addModalCanSubmit = computed(() => addModal.open && !computeAddModalError())

watch(
  () => [addModal.open, addModal.value, addModal.listKey],
  () => (addModal.error = computeAddModalError()),
  { immediate: true }
)

async function confirmAddModal() {
  const err = computeAddModalError()
  addModal.error = err
  if (err) {
    showToast(err, 'error')
    return
  }

  const list = attributes[addModal.listKey] || []
  const ten = cleanText(addModal.value)

  try {
    const ma = genNextAttrCode(addModal.typeCode, list)
    const res = await attributeService.create(addModal.typeCode, {
      ma,
      ten,
      trangThai: true
    })

    const created = res?.data
    if (!created?.id) throw new Error('Create failed')

    const reload = await attributeService.getAllList(addModal.typeCode)
    const nextList = pickActiveOrAll(reload.data || [])

    attributes[addModal.listKey] =
      addModal.listKey === 'kichCo'
        ? sortSizes(nextList)
        : [...nextList].sort((a, b) => Number(b?.id || 0) - Number(a?.id || 0))

    if (addModal.productKey) {
      product[addModal.productKey] = created.id
    }

    showToast(`Đã thêm "${ten}"`)
    closeAddModal()
  } catch (e) {
    console.error(e)
    addModal.error = 'Không thể thêm mới, vui lòng thử lại'
    showToast(addModal.error, 'error')
  }
}

const generatedVariants = ref([])

function generateVariants() {
  errors.mauSac = ''
  errors.kichCo = ''
  errors.chatLieuId = ''

  if (!selectedColors.value.length) {
    errors.mauSac = 'Vui lòng chọn ít nhất 1 màu'
    showToast(errors.mauSac, 'error')
    return
  }
  if (!selectedSizes.value.length) {
    errors.kichCo = 'Vui lòng chọn ít nhất 1 kích cỡ'
    showToast(errors.kichCo, 'error')
    return
  }
  if (!product.chatLieuId) {
    errors.chatLieuId = 'Vui lòng chọn chất liệu'
    showToast(errors.chatLieuId, 'error')
    return
  }
  if (!ensureSizesValidOrShow()) return

  const add = []
  for (const c of selectedColors.value) {
    for (const s of selectedSizes.value) {
      const exists = generatedVariants.value.some(
        (v) => String(v.idMauSac) === String(c.id) && String(v.idKichCo) === String(s.id)
      )
      if (!exists) {
        add.push({
          _key: `${c.id}-${s.id}-${Date.now()}-${Math.random()}`,
          idMauSac: c.id,
          tenMauSac: c.ten,
          idKichCo: s.id,
          tenKichCo: s.soSize,
          soLuongTon: 10,
          donGia: 0,
          donGiaText: '0',
          anh: '',
          mediaPrimaryId: null,
          _qtyErr: '',
          _priceErr: ''
        })
      }
    }
  }

  generatedVariants.value = [...generatedVariants.value, ...add]
  showToast('Đã tạo biến thể tự động!')
}

function removeVariantObj(v) {
  generatedVariants.value = generatedVariants.value.filter((x) => x !== v)
}

const variantsGroupedByColor = computed(() => {
  const groups = {}
  for (const v of generatedVariants.value) {
    const id = String(v.idMauSac)
    if (!groups[id]) groups[id] = { id, name: v.tenMauSac, variants: [] }
    groups[id].variants.push(v)
  }
  return Object.values(groups).map((g) => {
    g.variants.sort((a, b) => Number(a.tenKichCo) - Number(b.tenKichCo))
    return g
  })
})

async function handleGroupImageUpload(group, event) {
  const file = event.target.files?.[0]
  if (!file) return
  try {
    const uploaded = normalizeUploadResponse(await uploadImage(file))
    if (!uploaded.url) throw new Error('No url')
    group.variants.forEach((v) => {
      v.anh = uploaded.url
      v.mediaPrimaryId = uploaded.mediaAssetId
    })
    showToast(`Upload ảnh màu "${group.name}" thành công!`)
  } catch (e) {
    console.error(e)
    showToast('Lỗi upload ảnh', 'error')
  } finally {
    event.target.value = ''
  }
}

const showApplyModal = ref(false)
const applyTarget = ref(null)
const applyForm = reactive({
  qty: '',
  priceText: '',
  _qtyErr: '',
  _priceErr: ''
})

function openGroupApply(group) {
  applyTarget.value = group
  applyForm.qty = ''
  applyForm.priceText = ''
  applyForm._qtyErr = ''
  applyForm._priceErr = ''
  showApplyModal.value = true
}

function openGlobalApply() {
  openGroupApply({ name: 'Tất cả biến thể', variants: generatedVariants.value })
}

function closeApplyModal() {
  showApplyModal.value = false
  applyTarget.value = null
}

function onMoneyInputApply(e) {
  const raw = e?.target?.value ?? ''
  applyForm._priceErr = ''
  if (hasLeadingSpace(raw)) applyForm._priceErr = MSG_LEADING_SPACE
  applyForm.priceText = formatDotsFromDigits(stripNonDigits(raw))
}

function normalizeMoneyApply() {
  applyForm.priceText = formatDotsFromDigits(stripNonDigits(applyForm.priceText))
}

function confirmApply() {
  if (!applyTarget.value) return
  applyForm._qtyErr = ''
  applyForm._priceErr = ''

  let qtyApply = null
  if (applyForm.qty !== '' && applyForm.qty !== null && applyForm.qty !== undefined) {
    const msg = validateInt0('Số lượng tồn', applyForm.qty)
    if (msg !== true) {
      applyForm._qtyErr = msg
      return
    }
    qtyApply = Number(applyForm.qty)
  }

  let priceApply = null
  if (applyForm.priceText.trim() !== '') {
    if (hasLeadingSpace(applyForm.priceText)) {
      applyForm._priceErr = MSG_LEADING_SPACE
      return
    }
    priceApply = parseMoneyToNumber(applyForm.priceText)
    if (!Number.isFinite(priceApply) || priceApply < 0) {
      applyForm._priceErr = 'Đơn giá phải là số >= 0'
      return
    }
  }

  applyTarget.value.variants.forEach((v) => {
    if (qtyApply !== null) v.soLuongTon = qtyApply
    if (priceApply !== null) {
      v.donGia = priceApply
      v.donGiaText = formatDotsFromDigits(String(priceApply))
    }
  })

  showApplyModal.value = false
  applyTarget.value = null
  showToast('Đã áp dụng thành công!')
}

onMounted(async () => {
  try {
    const tasks = Object.keys(attributeMap).map((key) =>
      attributeService.getAllList(attributeMap[key]).then((res) => {
        const nextList = pickActiveOrAll(res.data || [])
        attributes[key] = key === 'kichCo' ? sortSizes(nextList) : nextList
      })
    )
    await Promise.all(tasks)
  } catch (e) {
    console.error(e)
    globalError.value = 'Lỗi tải dữ liệu.'
    showToast('Lỗi tải dữ liệu thuộc tính', 'error')
  }
})

function validateForm() {
  clearErrors()
  let ok = true

  if (hasLeadingSpace(product.tenSanPham)) {
    errors.tenSanPham = MSG_LEADING_SPACE
    ok = false
  } else {
    const ten = cleanText(product.tenSanPham)
    product.tenSanPham = ten
    if (!ten) {
      errors.tenSanPham = 'Tên sản phẩm bắt buộc'
      ok = false
    } else if (ten.length < 2 || ten.length > 80) {
      errors.tenSanPham = 'Tên sản phẩm 2–80 ký tự'
      ok = false
    } else if (!RE.NAME.test(ten)) {
      errors.tenSanPham = 'Tên sản phẩm không được chứa ký tự đặc biệt'
      ok = false
    }
  }

  if (hasLeadingSpace(product.moTa)) {
    errors.moTa = MSG_LEADING_SPACE
    ok = false
  } else {
    const moTa = cleanTextarea(product.moTa)
    product.moTa = moTa
    if (moTa && moTa.length > 500) {
      errors.moTa = 'Mô tả tối đa 500 ký tự'
      ok = false
    } else if (moTa && !RE.DESC.test(moTa)) {
      errors.moTa = 'Mô tả có ký tự không hợp lệ'
      ok = false
    }
  }

  const req = [
    ['loaiSanPhamId', 'loại sản phẩm'],
    ['thuongHieuId', 'thương hiệu'],
    ['soKhuyId', 'số khuy'],
    ['kieuTuiId', 'kiểu túi'],
    ['veAoId', 've áo'],
    ['xeTaId', 'xẻ tà'],
    ['xuatXuId', 'xuất xứ'],
    ['fitId', 'kiểu dáng'],
    ['chatLieuId', 'chất liệu']
  ]

  for (const [k, label] of req) {
    if (!product[k]) {
      errors[k] = `Vui lòng chọn ${label}`
      ok = false
    }
  }

  if (!selectedColors.value.length) {
    errors.mauSac = 'Vui lòng chọn ít nhất 1 màu'
    ok = false
  }
  if (!selectedSizes.value.length) {
    errors.kichCo = 'Vui lòng chọn ít nhất 1 kích cỡ'
    ok = false
  }

  if (selectedSizes.value.length && !ensureSizesValidOrShow()) ok = false

  return ok
}

function validateVariantsBeforeSubmit() {
  for (const v of generatedVariants.value) {
    v._qtyErr = ''
    v._priceErr = ''

    const qtyMsg = validateInt0(`Số lượng tồn (size ${v.tenKichCo})`, v.soLuongTon)
    if (qtyMsg !== true) {
      v._qtyErr = qtyMsg
      return qtyMsg
    }

    if (hasLeadingSpace(v.donGiaText)) {
      v._priceErr = MSG_LEADING_SPACE
      return `Đơn giá (${v.tenMauSac} - size ${v.tenKichCo}): ${MSG_LEADING_SPACE}`
    }

    const price = Number(v.donGia)
    if (!Number.isFinite(price) || price < 0) {
      v._priceErr = 'Đơn giá phải là số >= 0'
      return `Đơn giá (${v.tenMauSac} - size ${v.tenKichCo}) phải là số >= 0`
    }
  }
  return true
}

async function handleSubmitClick() {
  globalError.value = ''

  if (!validateForm()) {
    showToast('Vui lòng kiểm tra lại thông tin', 'error')
    return
  }

  if (generatedVariants.value.length) {
    const vMsg = validateVariantsBeforeSubmit()
    if (vMsg !== true) {
      showToast(vMsg, 'error')
      return
    }
  }

  const okConfirm = await openConfirm({
    title: 'Xác nhận',
    message: isEditMode.value ? 'Bạn có chắc chắn muốn lưu thay đổi?' : 'Bạn có chắc chắn muốn hoàn tất thêm sản phẩm?',
    okText: 'Đồng ý',
    cancelText: 'Hủy'
  })
  if (!okConfirm) return

  await doSubmit()
}

async function doSubmit() {
  const cl = attributes.chatLieu.find((x) => String(x.id) === String(product.chatLieuId))

  const variantsPayload = generatedVariants.value.map((v) => ({
    idKichCo: v.idKichCo,
    idMauSac: v.idMauSac,
    soLuongTon: Number(v.soLuongTon ?? 0),
    donGia: Number(v.donGia ?? 0),
    anh: v.anh,
    mediaPrimaryId: v.mediaPrimaryId ?? null,
    ghiChu: '',
    trangThai: true,
    chatLieu: cl ? cl.ten : ''
  }))

  const payload = {
    ...product,
    tenSanPham: cleanText(product.tenSanPham),
    moTa: cleanTextarea(product.moTa),
    variants: variantsPayload,
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

function goBack() {
  router.push('/products')
}

function normalizeColorName(name) {
  return String(name || '')
    .trim()
    .toLowerCase()
    .replace(/đ/g, 'd')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/\(.*?\)/g, '')
    .replace(/_/g, ' ')
    .replace(/-/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
}

const COLOR_MAP = {
  den: '#111827',
  black: '#111827',
  charcoal: '#1f2937',
  than: '#374151',

  trang: '#ffffff',
  white: '#ffffff',
  sua: '#fffdfa',
  ivory: '#fffff0',
  kem: '#fff7ed',
  cream: '#fff7ed',
  be: '#f5f5dc',
  beige: '#f5f5dc',
  nude: '#eec9a5',

  xam: '#9ca3af',
  ghi: '#9ca3af',
  gray: '#9ca3af',
  grey: '#9ca3af',
  'xam dam': '#6b7280',
  'ghi dam': '#6b7280',
  'xam nhat': '#d1d5db',
  'ghi nhat': '#d1d5db',

  bac: '#c0c0c0',
  silver: '#c0c0c0',

  do: '#ef4444',
  red: '#ef4444',
  'do tuoi': '#ff3b30',
  'do do': '#b91c1c',
  'do dam': '#b91c1c',
  burgundy: '#800020',
  bordo: '#800020',
  maroon: '#800000',
  wine: '#722f37',
  'do ruou': '#722f37',

  hong: '#ec4899',
  pink: '#ec4899',
  'hong nhat': '#f9a8d4',
  rose: '#f43f5e',
  'hong sen': '#db2777',
  magenta: '#ff00ff',
  fuchsia: '#ff00ff',

  tim: '#8b5cf6',
  purple: '#8b5cf6',
  violet: '#7c3aed',
  lavender: '#c4b5fd',
  lilac: '#c8a2c8',

  vang: '#eab308',
  yellow: '#eab308',
  gold: '#d4af37',
  golden: '#d4af37',
  mustard: '#d97706',
  'vang chanh': '#facc15',

  cam: '#f97316',
  orange: '#f97316',
  coral: '#fb7185',
  peach: '#fdba74',

  nau: '#8b5e3c',
  brown: '#8b5e3c',
  chocolate: '#7b3f00',
  coffee: '#6f4e37',
  cafe: '#6f4e37',
  caramel: '#b45309',
  mocha: '#7c5a43',

  xanh: '#3b82f6',
  blue: '#3b82f6',
  'xanh duong': '#3b82f6',
  'xanh da troi': '#0ea5e9',
  sky: '#0ea5e9',
  skyblue: '#0ea5e9',
  'xanh coban': '#2563eb',
  cobalt: '#2563eb',
  royal: '#4169e1',
  'royal blue': '#4169e1',
  'xanh navy': '#1e3a8a',
  'xanh than': '#1e3a8a',
  navy: '#1e3a8a',
  'midnight blue': '#191970',

  'xanh la': '#22c55e',
  green: '#22c55e',
  'xanh luc': '#16a34a',
  lime: '#84cc16',
  olive: '#708238',
  mint: '#6ee7b7',
  'xanh mint': '#6ee7b7',
  'xanh reu': '#4d7c0f',
  moss: '#4d7c0f',

  'xanh ngoc': '#14b8a6',
  teal: '#0f766e',
  turquoise: '#40e0d0',
  cyan: '#06b6d4',
  aqua: '#06b6d4',

  kemsua: '#fff8dc',
  'da bo': '#c68642'
}

function getColorCode(name) {
  if (!name) return '#e5e7eb'

  const raw = String(name || '').trim()
  const normalized = normalizeColorName(raw)

  const hexMatch = raw.match(/#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})\b/)
  if (hexMatch) return hexMatch[0]

  if (COLOR_MAP[normalized]) return COLOR_MAP[normalized]

  if (normalized.includes('navy') || normalized.includes('than')) return COLOR_MAP['xanh navy']
  if (normalized.includes('coban') || normalized.includes('cobalt')) return COLOR_MAP['xanh coban']
  if (normalized.includes('royal')) return COLOR_MAP.royal
  if (normalized.includes('da troi') || normalized.includes('sky')) return COLOR_MAP['xanh da troi']

  if (normalized.includes('xanh') && (normalized.includes('la') || normalized.includes('luc'))) return COLOR_MAP['xanh la']
  if (normalized.includes('xanh') && normalized.includes('reu')) return COLOR_MAP['xanh reu']
  if (normalized.includes('xanh') && normalized.includes('mint')) return COLOR_MAP['xanh mint']
  if (normalized.includes('xanh') && normalized.includes('ngoc')) return COLOR_MAP['xanh ngoc']
  if (normalized.includes('xanh') && normalized.includes('duong')) return COLOR_MAP['xanh duong']
  if (normalized.includes('xanh')) return COLOR_MAP.xanh

  if (normalized.includes('den') || normalized.includes('black')) return COLOR_MAP.den
  if (normalized.includes('trang') || normalized.includes('white')) return COLOR_MAP.trang
  if (normalized.includes('xam') || normalized.includes('ghi') || normalized.includes('gray') || normalized.includes('grey')) return COLOR_MAP.xam
  if (normalized.includes('bac') || normalized.includes('silver')) return COLOR_MAP.bac

  if (normalized.includes('do') || normalized.includes('red')) return COLOR_MAP.do
  if (normalized.includes('hong') || normalized.includes('pink')) return COLOR_MAP.hong
  if (normalized.includes('tim') || normalized.includes('purple') || normalized.includes('violet')) return COLOR_MAP.tim

  if (normalized.includes('vang') || normalized.includes('yellow') || normalized.includes('gold')) return COLOR_MAP.vang
  if (normalized.includes('cam') || normalized.includes('orange') || normalized.includes('coral') || normalized.includes('peach')) return COLOR_MAP.cam

  if (
    normalized.includes('nau') ||
    normalized.includes('brown') ||
    normalized.includes('cafe') ||
    normalized.includes('coffee') ||
    normalized.includes('chocolate') ||
    normalized.includes('caramel') ||
    normalized.includes('mocha')
  ) {
    return COLOR_MAP.nau
  }

  if (
    normalized.includes('be') ||
    normalized.includes('beige') ||
    normalized.includes('kem') ||
    normalized.includes('cream') ||
    normalized.includes('ivory') ||
    normalized.includes('nude')
  ) {
    return COLOR_MAP.be
  }

  return '#e5e7eb'
}
</script>

<style scoped>
:global(html, body, #app) {
  min-height: 100%;
  margin: 0;
}

.page {
  min-height: 100dvh;
  overflow: auto;
  padding: 16px;
  background: #f3f4f6;
}

.card {
  min-height: calc(100dvh - 32px);
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
  overflow: visible;
  padding-right: 0;
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
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
}

.title {
  font-size: 1.35rem;
  font-weight: 800;
  color: #374151;
}

.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.section {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.section-title {
  margin: 0 0 14px;
  font-size: 1.05rem;
  font-weight: 800;
  color: #1f2937;
  border-bottom: 1px solid #f3f4f6;
  padding-bottom: 10px;
}

.grid-3 {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.grid-1 {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
}

@media (max-width: 1100px) {
  .grid-3 {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 720px) {
  .grid-3,
  .grid-2 {
    grid-template-columns: 1fr;
  }
}

.form-group {
  margin-bottom: 12px;
}

.form-input {
  width: 100%;
  padding: 10px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  box-sizing: border-box;
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

.hint {
  display: block;
  margin-top: 6px;
  color: #6b7280;
  font-weight: 700;
}

.btn {
  padding: 10px 16px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-weight: 700;
}

.btn-secondary {
  background: #e5e7eb;
  color: #374151;
}

.btn-primary {
  background: #1e3a8a;
  color: #fff;
}

.btn-orange {
  background: #1e40af;
  color: #fff;
}

.full-width-btn {
  width: 100%;
  margin-top: 8px;
}

.field-row {
  display: flex;
  gap: 8px;
  align-items: stretch;
}

.btn-plus {
  width: 40px;
  min-width: 40px;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  background: #f9fafb;
  font-size: 18px;
  font-weight: 900;
  cursor: pointer;
}

.btn-plus:hover {
  background: #e5e7eb;
  border-color: #1e3a8a;
  color: #1e3a8a;
}

.ms-wrap {
  flex: 1;
  min-width: 0;
}

.ms-wrap :deep(.multiselect) {
  width: 100%;
  min-height: 40px;
  position: relative;
  display: block;
}

.ms-wrap :deep(.multiselect__tags) {
  min-height: 40px;
  padding: 6px 40px 6px 10px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  box-sizing: border-box;
  background: #fff;
}

.ms-wrap.error-border :deep(.multiselect__tags) {
  border-color: #ef4444;
  box-shadow: 0 0 0 1px rgba(239, 68, 68, 0.2);
}

.ms-wrap :deep(.multiselect__single),
.ms-wrap :deep(.multiselect__input) {
  margin: 0 !important;
  font-size: 0.95rem;
}

.ms-wrap :deep(.multiselect__select) {
  height: 38px;
}

.ms-wrap :deep(.multiselect__content-wrapper) {
  z-index: 80;
}

.ms-wrap-multi :deep(.multiselect__tags) {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  min-height: 40px;
  padding: 6px 40px 6px 10px;
  position: relative;
}

.ms-wrap-multi :deep(.multiselect__tags-wrap) {
  display: inline-flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}

.ms-wrap-multi :deep(.multiselect__input) {
  display: inline-block;
  width: auto !important;
  min-width: 120px;
  flex: 1 1 120px;
  margin: 0 !important;
  padding: 0 !important;
  line-height: 24px;
  border: none !important;
  background: transparent !important;
  box-shadow: none !important;
}

.ms-wrap-multi :deep(.multiselect__input::placeholder) {
  color: transparent !important;
}

.ms-wrap-multi :deep(.multiselect__placeholder) {
  position: absolute;
  top: 50%;
  left: 10px;
  transform: translateY(-50%);
  margin: 0 !important;
  padding: 0 !important;
  line-height: 24px;
  color: #9ca3af;
  pointer-events: none;
}

.ms-wrap-multi :deep(.multiselect--active .multiselect__placeholder) {
  display: none;
}

.ms-wrap-multi :deep(.multiselect__tags-wrap:not(:empty) ~ .multiselect__placeholder) {
  display: none;
}

.ms-empty {
  display: block;
  padding: 10px 12px;
  color: #6b7280;
  font-weight: 700;
}

.opt-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 2px 0;
}

.opt-invalid {
  opacity: 0.65;
}

.size-hint {
  margin-left: 8px;
  color: #dc2626;
  font-weight: 800;
  font-size: 0.8rem;
}

.color-dot,
.color-dot-lg {
  border: 1px solid #d1d5db;
}

.color-dot {
  width: 12px;
  height: 12px;
  border-radius: 999px;
}

.ms-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: #eef2ff;
  color: #1e3a8a;
  border-radius: 999px;
  padding: 2px 8px;
  margin: 2px 6px 2px 0;
  font-weight: 800;
}

.ms-tag-x {
  cursor: pointer;
  padding: 0 4px;
}

.tag-invalid {
  background: #fee2e2 !important;
  color: #991b1b !important;
  border: 1px solid #fecaca;
}

.ms-wrap :deep(.multiselect__option--highlight),
.ms-wrap :deep(.multiselect__option--selected.multiselect__option--highlight) {
  background: #0284c7 !important;
  color: #fff !important;
}

.ms-wrap :deep(.multiselect__option--highlight:after),
.ms-wrap :deep(.multiselect__option--selected.multiselect__option--highlight:after) {
  background: #0284c7 !important;
  color: #fff !important;
}

.ms-wrap :deep(.multiselect__option--selected) {
  background: #e0f2fe !important;
  color: #075985 !important;
}

.section-header-bar {
  background: #1e40af;
  padding: 10px 14px;
  border-radius: 10px 10px 0 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title-white {
  margin: 0;
  color: #fff;
  font-size: 1rem;
  font-weight: 900;
}

.btn-outline-white {
  border: 1px solid rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.btn-outline-danger {
  border: 1px solid #fecaca;
  background: white;
  color: #1e40af;
}

.bulk-actions {
  display: flex;
  gap: 10px;
}

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

.group-title {
  font-weight: 900;
  color: #1e40af;
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-dot-lg {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  display: inline-block;
}

.count-gray {
  color: #6b7280;
  font-weight: 700;
  font-size: 0.9rem;
}

.table-responsive {
  overflow-x: auto;
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

.btn-icon.danger {
  background: #fee2e2;
  color: #991b1b;
}

.btn-icon.danger:hover {
  background: #fecaca;
}

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

.btn-quick-add-blue:hover {
  background: #1e3a8a;
}

.section-image-upload {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid #e5e7eb;
}

.section-title-sm {
  margin: 0 0 12px;
  font-weight: 900;
}

.image-upload-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.image-upload-card {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
}

.card-upload-header {
  padding: 10px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
  font-weight: 800;
  display: flex;
  align-items: center;
  gap: 8px;
}

.upload-area {
  height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background: #fdfdfd;
}

.upload-area:hover {
  background: #f3f4f6;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  color: #9ca3af;
  font-weight: 700;
}

.icon-lg {
  font-size: 2rem;
}

.preview-box-lg {
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.preview-box-lg img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
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
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.16);
}

.modal-title {
  margin: 0 0 12px;
  font-weight: 900;
  color: #111827;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 14px;
}

.confirm-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
}

.confirm-modal {
  width: 420px;
  max-width: calc(100vw - 24px);
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.confirm-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #eef2f7;
}

.confirm-header h3 {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 900;
  color: #111827;
}

.close-btn {
  border: none;
  background: transparent;
  font-size: 22px;
  cursor: pointer;
  color: #6b7280;
}

.confirm-body {
  padding: 16px;
  color: #374151;
}

.confirm-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 0 16px 16px;
}

.btn-danger {
  background: #ef4444;
  color: #fff;
}

.btn-danger:hover {
  background: #dc2626;
}

.toast2 {
  position: fixed;
  top: 16px;
  right: 16px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.14);
  z-index: 99999;
  min-width: 360px;
  max-width: min(520px, calc(100vw - 32px));
}

.toast2-bar {
  width: 6px;
  border-radius: 10px;
  align-self: stretch;
  background: #22c55e;
}

.toast2-main {
  flex: 1;
  padding-top: 2px;
}

.toast2-title {
  font-weight: 900;
  margin-bottom: 4px;
  color: #16a34a;
}

.toast2-msg {
  color: #374151;
  line-height: 1.35;
  font-weight: 600;
}

.toast2-close {
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 18px;
  line-height: 1;
  color: #6b7280;
  padding: 2px 6px;
  border-radius: 8px;
}

.toast2-close:hover {
  background: #f3f4f6;
}

.toast2.error .toast2-bar {
  background: #ef4444;
}

.toast2.error .toast2-title {
  color: #dc2626;
}

.error-msg {
  margin-top: 10px;
  color: #b02a37;
  font-weight: 900;
}
</style>