<template>
  <div class="page">
    <div class="card">
      <!-- Header -->
      <div class="card-header flex-between">
        <h2 class="title">{{ isEditMode ? 'Cập nhật sản phẩm' : 'Thêm sản phẩm mới' }}</h2>
        <button class="btn btn-secondary" type="button" @click="goBack">← Quay lại danh sách</button>
      </div>

      <!-- Body -->
      <div class="card-body">
        <!-- ===== Thông tin cơ bản ===== -->
        <div class="section">
          <h3 class="section-title">Thông tin cơ bản</h3>

          <div class="grid-3">
            <!-- Tên -->
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

            <!-- Loại SP -->
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
                    <template #noResult>
                      <span class="ms-empty">Không tìm thấy loại sản phẩm theo từ khóa đã nhập.</span>
                    </template>
                    <template #noOptions>
                      <span class="ms-empty">Chưa có loại sản phẩm.</span>
                    </template>
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

            <!-- Thương hiệu -->
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
                    <template #noResult>
                      <span class="ms-empty">Không tìm thấy thương hiệu theo từ khóa đã nhập.</span>
                    </template>
                    <template #noOptions>
                      <span class="ms-empty">Chưa có thương hiệu.</span>
                    </template>
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

            <!-- Số khuy -->
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
                    <template #noResult>
                      <span class="ms-empty">Không tìm thấy số khuy theo từ khóa đã nhập.</span>
                    </template>
                    <template #noOptions>
                      <span class="ms-empty">Chưa có số khuy.</span>
                    </template>
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

            <!-- Kiểu túi -->
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
                    <template #noResult>
                      <span class="ms-empty">Không tìm thấy kiểu túi theo từ khóa đã nhập.</span>
                    </template>
                    <template #noOptions>
                      <span class="ms-empty">Chưa có kiểu túi.</span>
                    </template>
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

            <!-- Ve áo -->
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
                    <template #noResult>
                      <span class="ms-empty">Không tìm thấy ve áo theo từ khóa đã nhập.</span>
                    </template>
                    <template #noOptions>
                      <span class="ms-empty">Chưa có ve áo.</span>
                    </template>
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

            <!-- Xẻ tà -->
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
                    <template #noResult>
                      <span class="ms-empty">Không tìm thấy xẻ tà theo từ khóa đã nhập.</span>
                    </template>
                    <template #noOptions>
                      <span class="ms-empty">Chưa có xẻ tà.</span>
                    </template>
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

            <!-- Xuất xứ -->
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
                    <template #noResult>
                      <span class="ms-empty">Không tìm thấy xuất xứ theo từ khóa đã nhập.</span>
                    </template>
                    <template #noOptions>
                      <span class="ms-empty">Chưa có xuất xứ.</span>
                    </template>
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

            <!-- Fit -->
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
                    <template #noResult>
                      <span class="ms-empty">Không tìm thấy kiểu dáng theo từ khóa đã nhập.</span>
                    </template>
                    <template #noOptions>
                      <span class="ms-empty">Chưa có kiểu dáng.</span>
                    </template>
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

            <!-- Chất liệu -->
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
                    <template #noResult>
                      <span class="ms-empty">Không tìm thấy chất liệu theo từ khóa đã nhập.</span>
                    </template>
                    <template #noOptions>
                      <span class="ms-empty">Chưa có chất liệu.</span>
                    </template>
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
            />
            <small v-if="errors.moTa" class="error-text">{{ errors.moTa }}</small>
          </div>
        </div>

        <!-- ===== Biến thể ===== -->
        <div class="section">
          <h3 class="section-title">Biến thể sản phẩm</h3>

          <!-- Màu trên, size dưới (KHÔNG thêm nhanh) -->
          <div class="grid-1">
            <div class="form-group">
              <label class="required">Màu sắc</label>
              <div :class="['ms-wrap', { 'error-border': errors.mauSac }]">
                <Multiselect
                  v-model="selectedColors"
                  :options="attributes.mauSac"
                  track-by="id"
                  label="ten"
                  placeholder="-- Chọn màu sắc --"
                  :searchable="true"
                  :multiple="true"
                  :close-on-select="false"
                  :show-labels="false"
                  :taggable="false"
                >
                  <template #noResult>
                    <span class="ms-empty">Không tìm thấy màu sắc theo từ khóa đã nhập.</span>
                  </template>
                  <template #noOptions>
                    <span class="ms-empty">Chưa có màu sắc.</span>
                  </template>

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
                      <span class="ms-tag-x" @click="remove(option)">×</span>
                    </span>
                  </template>
                </Multiselect>
              </div>
              <small v-if="errors.mauSac" class="error-text">{{ errors.mauSac }}</small>
            </div>

            <div class="form-group">
              <label class="required">Kích cỡ</label>
              <div :class="['ms-wrap', { 'error-border': errors.kichCo }]">
                <Multiselect
                  v-model="selectedSizes"
                  :options="attributes.kichCo"
                  track-by="id"
                  label="soSize"
                  placeholder="-- Chọn kích cỡ --"
                  :searchable="true"
                  :multiple="true"
                  :close-on-select="false"
                  :show-labels="false"
                  :taggable="false"
                >
                  <template #noResult>
                    <span class="ms-empty">Không tìm thấy kích cỡ theo từ khóa đã nhập.</span>
                  </template>
                  <template #noOptions>
                    <span class="ms-empty">Chưa có kích cỡ.</span>
                  </template>
                   <template #tag="{ option, remove }">
    <span class="ms-tag">
      {{ option.soSize }}
      <span class="ms-tag-x" @click="remove(option)">×</span>
    </span>
  </template>
                </Multiselect>
              </div>
              <small v-if="errors.kichCo" class="error-text">{{ errors.kichCo }}</small>
            </div>
          </div>

          <button class="btn btn-orange full-width-btn" type="button" @click="generateVariants">
            ⚡ Tạo biến thể tự động
          </button>
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
                    <th style="width: 28%; text-align: center;">Kích cỡ</th>
                    <th style="width: 34%;">Số lượng tồn</th>
                    <th style="width: 34%;">Đơn giá</th>
                    <th style="width: 4%;"></th>
                  </tr>
                </thead>

                <tbody>
                  <tr v-for="v in g.variants" :key="v._key">
                    <td class="text-center"><span class="size-badge">{{ v.tenKichCo }}</span></td>

                    <td>
                      <input
                        class="form-input"
                        type="number"
                        min="0"
                        step="1"
                        v-model.number="v.soLuongTon"
                      />
                      <small v-if="v._qtyErr" class="error-text">{{ v._qtyErr }}</small>
                    </td>

                    <!-- Giá: nhập có dấu chấm ngăn cách -->
                    <td>
                      <input
                        class="form-input"
                        type="text"
                        inputmode="numeric"
                        placeholder="0"
                        :value="v.donGiaText"
                        @input="(e) => onMoneyInput(e, v)"
                        @blur="() => normalizeMoneyText(v)"
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

          <!-- Ảnh theo màu -->
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
                    <img :src="resolveFileUrl(g.variants[0].anh)" />
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

      <!-- Footer -->
      <div class="action-bar">
        <button class="btn btn-secondary" type="button" @click="goBack">Hủy</button>
        <button class="btn btn-primary" type="button" :disabled="loading" @click="handleSubmitClick">
          {{ loading ? 'Đang xử lý...' : (isEditMode ? 'Lưu thay đổi' : 'Hoàn tất') }}
        </button>
      </div>
    </div>

    <!-- ===== Modal thêm nhanh attribute (dấu +) ===== -->
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

    <!-- ===== Apply nhóm / tất cả ===== -->
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

    <!-- ===== Confirm ===== -->
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

    <!-- ===== Toast ===== -->
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
import { reactive, ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import Multiselect from 'vue-multiselect'
import 'vue-multiselect/dist/vue-multiselect.css'

import attributeService from '../../services/attributeService'
import { createSanPham, updateSanPham } from '../../services/sanPhamApi'
import { uploadImage } from '../../services/sanPhamChiTietApi'

/* ========= Router / Props ========= */
const router = useRouter()
const props = defineProps({ id: { type: [String, Number], default: null } })
const isEditMode = computed(() => !!props.id)

/* ========= Base ========= */
const loading = ref(false)
const globalError = ref('')
const fileBase = (import.meta?.env?.VITE_FILE_BASE_URL || 'http://localhost:8080').replace(/\/$/, '')

function resolveFileUrl(pathOrUrl) {
  if (!pathOrUrl) return ''
  if (/^https?:\/\//i.test(pathOrUrl)) return pathOrUrl
  return fileBase + pathOrUrl
}

/* ========= Toast ========= */
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

/* ========= Confirm ========= */
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

/* ===================== VALIDATION HELPERS ===================== */
const MSG_LEADING_SPACE = 'Không được nhập khoảng trắng ở đầu'
function hasLeadingSpace(v) {
  return /^\s/.test(String(v ?? ''))
}
function cleanText(s) {
  return String(s ?? '')
    .normalize('NFC')
    .replace(/\s+/g, ' ')
    .trim()
}
function cleanTextarea(s) {
  return String(s ?? '')
    .normalize('NFC')
    .split('\n')
    .map((line) => line.replace(/\s+/g, ' ').trim())
    .join('\n')
    .trim()
}

// Cho phép chữ + dấu (Mark) + số + khoảng trắng
const RE = {
  NAME: /^[\p{L}\p{M}\p{N} ]+$/u,
  // mô tả: cho phép thêm dấu câu cơ bản
  DESC: /^[\p{L}\p{M}\p{N}\s.,;:!?()\-/%'"“”‘’]+$/u
}

function clearErrors() {
  Object.keys(errors).forEach((k) => (errors[k] = ''))
}
function setErr(key, msg) {
  errors[key] = msg
}
function validateRequiredSelect(key, label, val) {
  if (!val) {
    setErr(key, `Vui lòng chọn ${label}`)
    return false
  }
  return true
}

function validateNameField(key, label, raw, { required = true, min = 2, max = 80 } = {}) {
  if (hasLeadingSpace(raw)) {
    setErr(key, MSG_LEADING_SPACE)
    return { ok: false, value: raw }
  }

  const v = cleanText(raw)

  if (required && !v) {
    setErr(key, `${label} bắt buộc`)
    return { ok: false, value: v }
  }
  if (v) {
    if (v.length < min) {
      setErr(key, `${label} tối thiểu ${min} ký tự`)
      return { ok: false, value: v }
    }
    if (v.length > max) {
      setErr(key, `${label} tối đa ${max} ký tự`)
      return { ok: false, value: v }
    }
    if (!RE.NAME.test(v)) {
      setErr(key, `${label} không được chứa ký tự đặc biệt`)
      return { ok: false, value: v }
    }
  }

  return { ok: true, value: v }
}

function validateDescField(key, label, raw, { required = false, max = 500 } = {}) {
  if (hasLeadingSpace(raw)) {
    setErr(key, MSG_LEADING_SPACE)
    return { ok: false, value: raw }
  }

  const v = cleanTextarea(raw)

  if (required && !v) {
    setErr(key, `${label} bắt buộc`)
    return { ok: false, value: v }
  }
  if (v) {
    if (v.length > max) {
      setErr(key, `${label} tối đa ${max} ký tự`)
      return { ok: false, value: v }
    }
    if (!RE.DESC.test(v)) {
      setErr(key, `${label} có ký tự không hợp lệ`)
      return { ok: false, value: v }
    }
  }
  return { ok: true, value: v }
}

function validateInt0(label, n) {
  if (n === '' || n === null || n === undefined) return `${label} không được để trống`
  const x = Number(n)
  if (!Number.isFinite(x) || x < 0 || !Number.isInteger(x)) return `${label} phải là số nguyên >= 0`
  return true
}

/* ===================== MONEY (dấu chấm) ===================== */
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

function onMoneyInput(e, v) {
  const raw = e?.target?.value ?? ''
  v._priceErr = ''
  if (hasLeadingSpace(raw)) v._priceErr = MSG_LEADING_SPACE

  const digits = stripNonDigits(raw)
  v.donGiaText = formatDotsFromDigits(digits)
  v.donGia = parseMoneyToNumber(v.donGiaText)
}
function normalizeMoneyText(v) {
  v.donGiaText = formatDotsFromDigits(stripNonDigits(v.donGiaText))
  v.donGia = parseMoneyToNumber(v.donGiaText)
}

/* ========= Form ========= */
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

/* ========= Bind multiselect single ========= */
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

/* ========= Colors / Sizes (multiple) ========= */
const selectedColors = ref([])
const selectedSizes = ref([])

/* ========= Add modal (+) ========= */
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

// check trùng: gộp space + lower
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
  () => {
    addModal.error = computeAddModalError()
  },
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
    const res = await attributeService.create(addModal.typeCode, { ma, ten, trangThai: true })
    const created = res?.data
    if (!created?.id) throw new Error('Create failed')

    const reload = await attributeService.getAllList(addModal.typeCode)
    attributes[addModal.listKey] = (reload.data || []).filter((x) => x.trangThai === true)

    if (addModal.productKey) product[addModal.productKey] = created.id

    showToast(`Đã thêm "${ten}"`)
    closeAddModal()
  } catch (e) {
    console.error(e)
    addModal.error = 'Không thể thêm mới, vui lòng thử lại'
  }
}

/* ========= Variants ========= */
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

  const add = []
  for (const c of selectedColors.value) {
    for (const s of selectedSizes.value) {
      const exists = generatedVariants.value.some((v) => v.idMauSac === c.id && v.idKichCo === s.id)
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
    if (!groups[v.idMauSac]) groups[v.idMauSac] = { id: v.idMauSac, name: v.tenMauSac, variants: [] }
    groups[v.idMauSac].variants.push(v)
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
    const res = await uploadImage(file)
    const url = res?.data?.url || res?.data || res?.url
    if (!url) throw new Error('No url')
    group.variants.forEach((v) => (v.anh = url))
    showToast(`Upload ảnh màu "${group.name}" thành công!`)
  } catch (e) {
    console.error(e)
    showToast('Lỗi upload ảnh', 'error')
  } finally {
    event.target.value = ''
  }
}

/* ========= Apply modal ========= */
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

  // qty: cho phép rỗng
  let qtyApply = null
  if (applyForm.qty !== '' && applyForm.qty !== null && applyForm.qty !== undefined) {
    const msg = validateInt0('Số lượng tồn', applyForm.qty)
    if (msg !== true) {
      applyForm._qtyErr = msg
      return
    }
    qtyApply = Number(applyForm.qty)
  }

  // price: cho phép rỗng
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

/* ========= Load attributes ========= */
onMounted(async () => {
  try {
    const tasks = Object.keys(attributeMap).map((key) =>
      attributeService.getAllList(attributeMap[key]).then((res) => {
        attributes[key] = (res.data || []).filter((x) => x.trangThai === true)
      })
    )
    await Promise.all(tasks)
  } catch (e) {
    console.error(e)
    globalError.value = 'Lỗi tải dữ liệu.'
    showToast('Lỗi tải dữ liệu thuộc tính', 'error')
  }
})

/* ========= Validate form ========= */
function validateForm() {
  clearErrors()
  let ok = true

  // text fields: cấm khoảng trắng đầu
  const tenRes = validateNameField('tenSanPham', 'Tên sản phẩm', product.tenSanPham, { min: 2, max: 80 })
  product.tenSanPham = tenRes.value
  ok = tenRes.ok && ok

  const descRes = validateDescField('moTa', 'Mô tả', product.moTa, { required: false, max: 500 })
  product.moTa = descRes.value
  ok = descRes.ok && ok

  // select required
  ok = validateRequiredSelect('loaiSanPhamId', 'loại sản phẩm', product.loaiSanPhamId) && ok
  ok = validateRequiredSelect('thuongHieuId', 'thương hiệu', product.thuongHieuId) && ok
  ok = validateRequiredSelect('soKhuyId', 'số khuy', product.soKhuyId) && ok
  ok = validateRequiredSelect('kieuTuiId', 'kiểu túi', product.kieuTuiId) && ok
  ok = validateRequiredSelect('veAoId', 've áo', product.veAoId) && ok
  ok = validateRequiredSelect('xeTaId', 'xẻ tà', product.xeTaId) && ok
  ok = validateRequiredSelect('xuatXuId', 'xuất xứ', product.xuatXuId) && ok
  ok = validateRequiredSelect('fitId', 'kiểu dáng', product.fitId) && ok
  ok = validateRequiredSelect('chatLieuId', 'chất liệu', product.chatLieuId) && ok

  // colors / sizes
  if (!selectedColors.value.length) {
    errors.mauSac = 'Vui lòng chọn ít nhất 1 màu'
    ok = false
  }
  if (!selectedSizes.value.length) {
    errors.kichCo = 'Vui lòng chọn ít nhất 1 kích cỡ'
    ok = false
  }

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

    // giá: cấm khoảng trắng đầu
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

/* ========= Submit ========= */
async function handleSubmitClick() {
  globalError.value = ''

  if (!validateForm()) {
    showToast('Vui lòng kiểm tra lại thông tin', 'error')
    return
  }

  const vMsg = validateVariantsBeforeSubmit()
  if (vMsg !== true) {
    showToast(vMsg, 'error')
    return
  }

  // chưa có biến thể thì hỏi
  if (generatedVariants.value.length === 0) {
    const okNoVariants = await openConfirm({
      title: 'Chưa có biến thể',
      message: 'Bạn chưa tạo biến thể nào. Vẫn muốn tiếp tục lưu sản phẩm?',
      okText: 'Tiếp tục',
      cancelText: 'Hủy'
    })
    if (!okNoVariants) return
  } else {
    const okConfirm = await openConfirm({
      title: 'Xác nhận',
      message: isEditMode.value ? 'Bạn có chắc chắn muốn lưu thay đổi?' : 'Bạn có chắc chắn muốn hoàn tất thêm sản phẩm?',
      okText: 'Đồng ý',
      cancelText: 'Hủy'
    })
    if (!okConfirm) return
  }

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

/* ========= Clear variants ========= */
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
  const m = n.match(/\(([^)]+)\)/)
  if (m) return m[1]
  return '#e5e7eb'
}
</script>

<style scoped>
:global(html, body, #app) { height: 100%; margin: 0; }
.page { height: 100dvh; overflow: hidden; padding: 16px; background: #f3f4f6; }
.card { height: 100%; display: flex; flex-direction: column; }
.card-header { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; padding: 14px 16px; margin-bottom: 12px; }
.card-body { flex: 1; overflow: auto; padding-right: 6px; }
.action-bar { margin-top: 12px; background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; padding: 14px 16px; display: flex; justify-content: flex-end; gap: 12px; box-shadow: 0 6px 20px rgba(0,0,0,0.06); }

.title { font-size: 1.35rem; font-weight: 800; color: #374151; }
.flex-between { display: flex; justify-content: space-between; align-items: center; gap: 12px; }

.section { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; padding: 16px; margin-bottom: 12px; box-shadow: 0 1px 2px rgba(0,0,0,0.04); }
.section-title { margin: 0 0 14px; font-size: 1.05rem; font-weight: 800; color: #1f2937; border-bottom: 1px solid #f3f4f6; padding-bottom: 10px; }

.grid-3 { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; }
.grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.grid-1 { display: grid; grid-template-columns: 1fr; gap: 14px; }
@media (max-width: 1100px) { .grid-3 { grid-template-columns: 1fr 1fr; } }
@media (max-width: 720px) { .grid-3, .grid-2 { grid-template-columns: 1fr; } }

.form-group { margin-bottom: 12px; }
.form-input { width: 100%; padding: 10px; border: 1px solid #d1d5db; border-radius: 8px; box-sizing: border-box; }
.required::after { content: ' *'; color: red; }
.error-text { color: red; font-size: 0.8rem; }
.error-border { border-color: #ef4444 !important; box-shadow: 0 0 0 1px rgba(239,68,68,0.2); }

.btn { padding: 10px 16px; border-radius: 8px; border: none; cursor: pointer; font-weight: 700; }
.btn-secondary { background: #e5e7eb; color: #374151; }
.btn-primary { background: #1e3a8a; color: #fff; }
.btn-orange { background: #1e40af; color: #fff; }
.full-width-btn { width: 100%; margin-top: 8px; }

.field-row{ display:flex; gap:8px; align-items:stretch; }
.btn-plus{
  width: 40px;
  min-width: 40px;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  background: #f9fafb;
  font-size: 18px;
  font-weight: 900;
  cursor: pointer;
}
.btn-plus:hover{ background:#e5e7eb; border-color:#1e3a8a; color:#1e3a8a; }

.ms-wrap{ flex:1; }
.ms-wrap :deep(.multiselect__tags){
  min-height: 40px;
  padding: 6px 40px 0 10px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  box-sizing: border-box;
}
.ms-wrap.error-border :deep(.multiselect__tags){ border-color:#ef4444; box-shadow:0 0 0 1px rgba(239,68,68,0.2); }
.ms-wrap :deep(.multiselect__single){ margin-top: 3px; font-size: 0.95rem; }
.ms-wrap :deep(.multiselect__input){ margin-top: 3px; }
.ms-wrap :deep(.multiselect__select){ height: 38px; }

.ms-empty{
  display:block;
  padding: 10px 12px;
  color:#6b7280;
  font-weight: 700;
}

.opt-row{ display:flex; align-items:center; gap:8px; padding:2px 0; }
.color-dot{ width:12px; height:12px; border-radius:999px; border:1px solid rgba(0,0,0,0.12); }
.ms-tag{
  display:inline-flex;
  align-items:center;
  gap:6px;
  background:#eef2ff;
  color:#1e3a8a;
  border-radius:999px;
  padding:2px 8px;
  margin:2px 6px 2px 0;
  font-weight:800;
}
.ms-tag-x{ cursor:pointer; padding:0 4px; }

/* ===== Variants ===== */
.section-header-bar { background: #1e40af; padding: 10px 14px; border-radius: 10px 10px 0 0; display: flex; justify-content: space-between; align-items: center; }
.section-title-white { margin: 0; color: #fff; font-size: 1rem; font-weight: 900; }
.btn-outline-white { border: 1px solid rgba(255,255,255,0.5); background: rgba(255,255,255,0.2); color: #fff; }
.btn-outline-danger { border: 1px solid #fecaca; background: white; color: #1e40af; }
.bulk-actions { display: flex; gap: 10px; }

.variant-group { margin-top: 12px; border: 1px solid #e5e7eb; border-radius: 10px; overflow: hidden; background: #fff; }
.group-header { background: white; padding: 10px 12px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #e5e7eb; }
.group-title { font-weight: 900; color: #1e40af; display: flex; align-items: center; gap: 8px; }
.color-dot-lg { width: 16px; height: 16px; border-radius: 50%; display: inline-block; border:1px solid rgba(0,0,0,0.12); }
.count-gray { color: #6b7280; font-weight: 700; font-size: 0.9rem; }

.table-responsive { overflow-x: auto; }
.variants-table { width: 100%; border-collapse: collapse; }
.variants-table th { background: #f9fafb; padding: 10px; text-align: left; border-bottom: 1px solid #e5e7eb; }
.variants-table td { padding: 10px; border-bottom: 1px solid #e5e7eb; vertical-align: middle; }

.size-badge { display: flex; align-items: center; justify-content: center; height: 38px; border: 1px solid #d1d5db; background: #f3f4f6; border-radius: 8px; font-weight: 900; }
.btn-icon { width: 32px; height: 32px; border: none; border-radius: 8px; cursor: pointer; font-size: 16px; line-height: 1; }
.btn-icon.danger { background: #fee2e2; color: #991b1b; }
.btn-icon.danger:hover { background: #fecaca; }

.btn-quick-add-blue { background: #1e40af; color: #fff; border: none; border-radius: 8px; padding: 6px 12px; font-size: 0.85rem; font-weight: 800; cursor: pointer; }
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
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-box { background: #fff; border-radius: 12px; padding: 18px; width: 420px; max-width: calc(100vw - 24px); box-shadow: 0 10px 30px rgba(0,0,0,0.16); }
.modal-title { margin: 0 0 12px; font-weight: 900; color: #111827; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 14px; }

/* ===== Confirm ===== */
.confirm-overlay{ position: fixed; inset: 0; background: rgba(0,0,0,0.45); display: flex; align-items: center; justify-content: center; z-index: 10000; }
.confirm-modal{ width: 420px; max-width: calc(100vw - 24px); background: #fff; border-radius: 12px; box-shadow: 0 15px 40px rgba(0,0,0,0.2); overflow: hidden; }
.confirm-header{ display: flex; align-items: center; justify-content: space-between; padding: 14px 16px; border-bottom: 1px solid #eef2f7; }
.confirm-header h3{ margin: 0; font-size: 1.05rem; font-weight: 900; color: #111827; }
.close-btn{ border: none; background: transparent; font-size: 22px; cursor: pointer; color: #6b7280; }
.confirm-body{ padding: 16px; color: #374151; }
.confirm-actions{ display: flex; justify-content: flex-end; gap: 10px; padding: 0 16px 16px; }
.btn-danger{ background: #ef4444; color: #fff; }
.btn-danger:hover{ background: #dc2626; }

/* ===== Toast ===== */
.toast2{
  position: fixed;
  top: 16px;
  right: 16px;
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
.toast2-bar{ width: 6px; border-radius: 10px; align-self: stretch; background: #22c55e; }
.toast2-main{ flex: 1; padding-top: 2px; }
.toast2-title{ font-weight: 900; margin-bottom: 4px; color: #16a34a; }
.toast2-msg{ color: #374151; line-height: 1.35; font-weight: 600; }
.toast2-close{ border: none; background: transparent; cursor: pointer; font-size: 18px; line-height: 1; color: #6b7280; padding: 2px 6px; border-radius: 8px; }
.toast2-close:hover{ background: #f3f4f6; }
.toast2.error .toast2-bar{ background: #ef4444; }
.toast2.error .toast2-title{ color: #dc2626; }

.error-msg { margin-top: 10px; color: #b02a37; font-weight: 900; }
</style>
