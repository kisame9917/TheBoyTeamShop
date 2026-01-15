<template>
  <div class="card">
    <!-- HEADER -->
    <div class="page-header">
      <div>
        <h2 class="page-title">Phiếu giảm giá</h2>
        <p class="page-desc">Quản lý phiếu giảm giá cho hệ thống VestShop</p>
      </div>

      <button class="btn btn-primary" @click="openCreate">
        + Thêm phiếu
      </button>
    </div>

    <!-- FILTER -->
    <div class="filter-box">
      <div class="form-group">
        <label>Mã / Tên</label>
        <input
          class="form-input"
          v-model="keyword"
          placeholder="Nhập mã hoặc tên giảm giá"
        />
      </div>

      <div class="form-group">
        <label>Loại giảm</label>
        <select class="form-input" v-model="filterLoai">
          <option value="">Tất cả</option>
          <option value="true">Giảm %</option>
          <option value="false">Giảm tiền</option>
        </select>
      </div>

      <div class="form-group form-action">
        <button class="btn" @click="load">Tìm kiếm</button>
      </div>
    </div>

    <!-- TABLE -->
    <div class="table-wrapper">
      <table class="table">
        <thead>
          <tr>
            <th>Mã</th>
            <th>Tên</th>
            <th>Loại</th>
            <th>Giá trị</th>
            <th>Số lượng</th>
            <th>Thời gian</th>
            <th>Trạng thái</th>
            <th width="160">Hành động</th>
          </tr>
        </thead>

        <tbody>
          <tr v-for="item in filteredList" :key="item.id">
            <td>{{ item.maGiamGia }}</td>
            <td>{{ item.tenGiamGia }}</td>

            <td>
              <span v-if="item.loaiGiam">%</span>
              <span v-else>Tiền</span>
            </td>

            <td>
              <span v-if="item.loaiGiam">{{ item.giaTriPhanTram ?? '-' }}%</span>
              <span v-else>{{ item.giaTriTienMat ?? '-' }}</span>
            </td>

            <td>{{ item.soLuong ?? 0 }}</td>

            <td>
              {{ formatDate(item.ngayBatDau) }}
              →
              {{ formatDate(item.ngayKetThuc) }}
            </td>

            <td>
              <span class="status" :class="item.trangThai ? 'active' : 'inactive'">
                {{ item.trangThai ? 'Hoạt động' : 'Đã xoá' }}
              </span>
            </td>

            <td>
              <button class="btn btn-sm" @click="openEdit(item)">Sửa</button>
              <button
                class="btn btn-sm btn-danger"
                @click="remove(item.id)"
                v-if="item.trangThai"
              >
                Xoá
              </button>
            </td>
          </tr>

          <tr v-if="!loading && filteredList.length === 0">
            <td colspan="8" class="empty">Không có dữ liệu</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="loading" style="margin-top: 10px; color: #6b7280;">
      Đang tải dữ liệu...
    </div>

    <div v-if="error" style="margin-top: 10px; color: #b91c1c;">
      {{ error }}
    </div>

    <!-- FORM MODAL -->
    <VoucherForm
      v-if="showForm"
      :data="selected"
      @close="closeForm"
      @saved="onSaved"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  getAllPhieuGiamGia,
  softDeletePhieuGiamGia
} from '../../services/PhieuGiamGiaApi.js'


import VoucherForm from './VoucherForm.vue'

const list = ref([])
const keyword = ref('')
const filterLoai = ref('') // '' | 'true' | 'false'
const showForm = ref(false)
const selected = ref(null)

const loading = ref(false)
const error = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await getAllPhieuGiamGia()
    list.value = Array.isArray(data) ? data : []
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || 'Không tải được dữ liệu'
  } finally {
    loading.value = false
  }
}

const filteredList = computed(() => {
  const kw = keyword.value?.trim().toLowerCase()

  return list.value.filter((i) => {
    // lọc loại giảm
    if (filterLoai.value !== '') {
      const wantLoai = filterLoai.value === 'true' // boolean muốn lọc
      if (i.loaiGiam !== wantLoai) return false
    }

    // lọc keyword
    if (kw) {
      const ma = String(i.maGiamGia ?? '').toLowerCase()
      const ten = String(i.tenGiamGia ?? '').toLowerCase()
      if (!ma.includes(kw) && !ten.includes(kw)) return false
    }

    return true
  })
})

function openCreate() {
  selected.value = null
  showForm.value = true
}

function openEdit(item) {
  selected.value = item
  showForm.value = true
}

function closeForm() {
  showForm.value = false
}

async function onSaved() {
  // form emit saved -> đóng modal + reload list
  closeForm()
  await load()
}

async function remove(id) {
  if (!confirm('Xoá (mềm) phiếu giảm giá này?')) return

  try {
    await softDeletePhieuGiamGia(id)
    await load()
  } catch (e) {
    alert(e?.response?.data?.message || e?.message || 'Xoá thất bại')
  }
}

function formatDate(d) {
  if (!d) return '-'
  return String(d).replace('T', ' ').slice(0, 16)
}

onMounted(load)
</script>
