<template>
  <div class="attribute-page">
    <div class="header">
      <h2>Quản lý Sản Phẩm/{{ title }}</h2>
    </div>

    <div class="actions-bar">
      <div class="search-box">
        <input type="text" v-model="searchQuery" :placeholder="`Tìm kiếm ${title.toLowerCase()}...`" />
      </div>
      <div class="buttons">
        <button class="btn btn-excel">Xuất Excel</button>
        <button class="btn btn-primary" @click="openModal('create')">+ Thêm {{ title.toLowerCase() }}</button>
      </div>
    </div>

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>STT</th>
            <th>Mã</th>
            <th>{{ title }}</th>
            <th>Trạng thái</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in filteredItems" :key="item.id">
            <td>{{ index + 1 }}</td>
            <td>{{ item.ma }}</td>
            <td>{{ item.ten }}</td>
            <td>
              <span class="status-badge" :class="item.trangThai ? 'active' : 'inactive'">
                {{ item.trangThai ? 'Hoạt động' : 'Ngừng hoạt động' }}
              </span>
            </td>
            <td>
              <button class="btn-icon edit" @click="openModal('edit', item)">✏️</button>
              <button class="btn-icon status-toggle" @click="toggleStatus(item)" title="Đổi trạng thái">🔄</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay">
      <div class="modal">
        <h3>{{ modalMode === 'create' ? 'Thêm' : 'Cập nhật' }} {{ title }}</h3>
        <form @submit.prevent="submitForm">

          <div class="form-group">
            <label>Tên</label>
            <input type="text" v-model="formData.ten" required />
          </div>

          <div class="modal-actions">
            <button type="button" class="btn btn-secondary" @click="closeModal">Hủy</button>
            <button type="submit" class="btn btn-primary">Lưu</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import attributeService from '../../services/attributeService'
import { useToast } from '../../composables/useToast'

const { success, error } = useToast()

const route = useRoute()
const type = computed(() => route.params.type)

const items = ref([])
const searchQuery = ref('')
const showModal = ref(false)
const modalMode = ref('create')
const formData = ref({ ma: '', ten: '', trangThai: true, id: null })

const TITLES = {
  'thuong-hieu': 'Thương Hiệu',
  'chat-lieu': 'Chất Liệu',
  'kich-co': 'Kích Cỡ',
  'mau-sac': 'Màu Sắc',
  'loai-san-pham': 'Loại Sản Phẩm',
  'so-khuy': 'Số Khuy',
  'kieu-tui': 'Kiểu Túi',
  've-ao': 'Ve Áo',
  'xe-ta': 'Xẻ Tà',
  'xuat-xu': 'Xuất Xứ',
  'fit': 'Fit'
}

const actions = {
  // ...
}

// Helper to remove Vietnamese tones and special chars
function removeVietnameseTones(str) {
  str = str.replace(/à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ/g, "a");
  str = str.replace(/è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ/g, "e");
  str = str.replace(/ì|í|ị|ỉ|ĩ/g, "i");
  str = str.replace(/ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ/g, "o");
  str = str.replace(/ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ/g, "u");
  str = str.replace(/ỳ|ý|ỵ|ỷ|ỹ/g, "y");
  str = str.replace(/đ/g, "d");
  str = str.replace(/À|Á|Ạ|Ả|Ã|Â|Ầ|Ấ|Ậ|Ẩ|Ẫ|Ă|Ằ|Ắ|Ặ|Ẳ|Ẵ/g, "A");
  str = str.replace(/È|É|Ẹ|Ẻ|Ẽ|Ê|Ề|Ế|Ệ|Ể|Ễ/g, "E");
  str = str.replace(/Ì|Í|Ị|Ỉ|Ĩ/g, "I");
  str = str.replace(/Ò|Ó|Ọ|Ỏ|Õ|Ô|Ồ|Ố|Ộ|Ổ|Ỗ|Ơ|Ờ|Ớ|Ợ|Ở|Ỡ/g, "O");
  str = str.replace(/Ù|Ú|Ụ|Ủ|Ũ|Ư|Ừ|Ứ|Ự|Ử|Ữ/g, "U");
  str = str.replace(/Ỳ|Ý|Ỵ|Ỷ|Ỹ/g, "Y");
  str = str.replace(/Đ/g, "D");
  // Some system encode vietnamese combining accent as individual utf-8 characters
  // \u0300, \u0301, \u0303, \u0309, \u0323
  str = str.normalize("NFD").replace(/[\u0300-\u036f]/g, "");
  return str;
}

const title = computed(() => TITLES[type.value] || 'Thuộc tính')

async function fetchData() {
  try {
    const response = await attributeService.getAllList(type.value)
    items.value = response.data.map(item => {
      // Normalize 'soSize' to 'ten' for 'kich-co'
      if (type.value === 'kich-co' && item.soSize) {
        return { ...item, ten: item.soSize }
      }
      return item
    })
  } catch (error) {
    console.error('Failed to fetch data', error)
  }
}

watch(type, () => {
  fetchData()
})

onMounted(() => {
  fetchData()
})

const filteredItems = computed(() => {
  if (!searchQuery.value) return items.value
  const lower = searchQuery.value.toLowerCase()
  return items.value.filter(i =>
    i.ten.toLowerCase().includes(lower) ||
    (i.ma && i.ma.toLowerCase().includes(lower)) // Check ma exists
  )
})

function openModal(mode, item = null) {
  modalMode.value = mode
  if (mode === 'edit' && item) {
    formData.value = { ...item }
  } else {
    // Default Status is Active (true)
    formData.value = { ma: '', ten: '', trangThai: true, id: null }
  }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

async function submitForm() {
  try {
    const payload = { ...formData.value }

    // DUPLICATE CHECK
    const duplicate = items.value.find(i =>
      i.ten.trim().toLowerCase() === payload.ten.trim().toLowerCase() &&
      i.id !== payload.id // Exclude self if editing
    )
    if (duplicate) {
      error(`Đã tồn tại thuộc tính với tên "${payload.ten}"`)
      return
    }

    // AUTO GENERATE CODE if creating or code is empty
    if (modalMode.value === 'create' || !payload.ma) {
      // Generate code from Name: Áo Thun -> AO_THUN
      let code = removeVietnameseTones(payload.ten).trim()
      code = code.replace(/\s+/g, '_').toUpperCase()
      // Remove special chars
      code = code.replace(/[^A-Z0-9_]/g, '')

      // Ensure code uniqueness (simple check, append suffix if needed)
      let uniqueCode = code
      let counter = 1
      while (items.value.some(i => i.ma === uniqueCode && i.id !== payload.id)) {
        uniqueCode = `${code}_${counter}`
        counter++
      }
      payload.ma = uniqueCode
    }

    // Map 'ten' back to 'soSize' for 'kich-co'
    if (type.value === 'kich-co') {
      payload.soSize = payload.ten
      // delete payload.ten // Optional
    }

    if (modalMode.value === 'create') {
      await attributeService.create(type.value, payload)
      success(`Thêm ${title.value} thành công!`)
    } else {
      await attributeService.update(type.value, payload.id, payload)
      success(`Cập nhật ${title.value} thành công!`)
    }
    closeModal()
    fetchData()
  } catch (err) {
    const msg = err.response?.data?.message || err.message
    error('Có lỗi xảy ra: ' + msg)
  }
}

async function toggleStatus(item) {
  try {
    const updatedItem = { ...item, trangThai: !item.trangThai }

    // Handle special mapping for KichCo (Size) if needed
    if (type.value === 'kich-co') {
      updatedItem.soSize = updatedItem.ten
    }

    await attributeService.update(type.value, item.id, updatedItem)
    fetchData()
    success(`Đã đổi trạng thái "${item.ten}" thành ${updatedItem.trangThai ? 'Hoạt động' : 'Ngừng hoạt động'}`)
  } catch (err) {
    const msg = err.response?.data?.message || err.message
    error('Không thể cập nhật trạng thái: ' + msg)
  }
}
</script>

<style scoped>
.attribute-page {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.header h2 {
  font-size: 20px;
  margin-bottom: 20px;
  color: #333;
}

.actions-bar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.search-box input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 300px;
}

.buttons .btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-left: 10px;
}

.btn-primary {
  background-color: #ff4d4f;
  /* Red-ish color from screenshot */
  color: white;
}

.btn-excel {
  background-color: #f0f0f0;
  color: #333;
}

.table-container {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

th {
  background-color: #fafafa;
  font-weight: 600;
  color: #666;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.status-badge.active {
  background-color: #e6f7ff;
  color: #1890ff;
}

.status-badge.inactive {
  background-color: #fff1f0;
  color: #ff4d4f;
}

.btn-icon {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  margin-right: 8px;
}

/* Modal Styles */
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

.modal {
  background: white;
  padding: 24px;
  border-radius: 8px;
  width: 400px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
  /* Fix width overflow */
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 24px;
}

.btn-secondary {
  background: #f5f5f5;
  color: #333;
}
</style>
