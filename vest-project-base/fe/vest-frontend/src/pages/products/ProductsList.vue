<template>
  <div class="card">
    <div style="display:flex; justify-content:space-between; align-items:center; gap:12px;">
      <div>
        <h2 style="margin:0;">Sản phẩm</h2>
        <p style="margin:4px 0 0; color:#6b7280">Danh sách sản phẩm (call BE: <code>/api/san-pham</code>)</p>
      </div>
      <button class="btn" @click="reload">Reload</button>
    </div>
  </div>

  <div style="height:12px" />

  <div class="card">
    <table class="table">
      <thead>
        <tr>
          <th>ID</th>
          <th>Mã</th>
          <th>Tên</th>
          <th>Trạng thái</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="p in items" :key="p.id" @click="goDetail(p.id)" style="cursor:pointer;">
          <td>{{ p.id }}</td>
          <td>{{ p.maSanPham }}</td>
          <td>{{ p.tenSanPham }}</td>
          <td>{{ p.trangThai }}</td>
        </tr>
        <tr v-if="!loading && items.length === 0">
          <td colspan="4" style="color:#6b7280">Chưa có dữ liệu</td>
        </tr>
        <tr v-if="loading">
          <td colspan="4" style="color:#6b7280">Đang tải...</td>
        </tr>
      </tbody>
    </table>

    <p v-if="error" style="margin:12px 0 0; color:#b91c1c">{{ error }}</p>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listSanPham } from '../../services/sanPhamApi'

const router = useRouter()
const items = ref([])
const loading = ref(false)
const error = ref('')

function goDetail(id) {
  router.push(`/products/${id}`)
}

async function reload() {
  loading.value = true
  error.value = ''
  try {
    items.value = await listSanPham()
  } catch (e) {
    error.value = 'Không gọi được API. Kiểm tra BE đang chạy ở http://localhost:8080'
  } finally {
    loading.value = false
  }
}

onMounted(reload)
</script>
