<template>
  <div class="page">
    <!-- top bar -->
    <div class="topbar">
      <h1>Thêm phiếu giảm giá</h1>

      <button class="btn btn-secondary" @click="goBack">
        ← Quay lại danh sách
      </button>
    </div>

    <div class="card">
      <div class="form-grid">
        <!-- LEFT -->
        <div class="col">
          <div class="form-group">
            <label>Loại giảm</label>
            <select class="input" v-model="form.loaiGiam">
              <option :value="true">Giảm %</option>
              <option :value="false">Giảm tiền (VND)</option>
            </select>
          </div>

          <div class="form-group">
            <label>Giá trị giảm</label>
            <div class="with-suffix">
              <input
                type="number"
                class="input"
                v-model.number="form.giaTriGiam"
                :min="1"
                :max="form.loaiGiam ? 100 : null"
                :placeholder="form.loaiGiam ? 'Ví dụ: 20' : 'Ví dụ: 50000'"
              />
              <span class="suffix">{{ form.loaiGiam ? '%' : 'VND' }}</span>
            </div>
          </div>

          <div class="form-group" v-if="form.loaiGiam">
            <label>Giảm tối đa (VND)</label>
            <input
              type="number"
              class="input"
              v-model.number="form.giaTriGiamToiDa"
              min="1"
              placeholder="Ví dụ: 2000000"
            />
          </div>

          <div class="form-group">
            <label>Ngày bắt đầu</label>
            <input
              type="date"
              class="input input-sm"
              v-model="form.ngayBatDau"
            />
            <small class="hint" v-if="form.ngayBatDau">
              Tự gửi: {{ form.ngayBatDau }}T00:00:00
            </small>
          </div>
        </div>

        <!-- RIGHT -->
        <div class="col">
          <div class="form-group">
            <label>Tên phiếu giảm giá</label>
            <input
              class="input"
              v-model.trim="form.tenGiamGia"
              placeholder="Ví dụ: Sale 1/6"
            />
          </div>

          <div class="form-group">
            <label>Số lượng</label>
            <input
              type="number"
              class="input"
              v-model.number="form.soLuong"
              min="1"
            />
            <small class="hint" v-if="form.loaiPhieu === 'CA_NHAN'">
              Gợi ý: số lượng thường = số khách hàng đã chọn
            </small>
          </div>

          <div class="form-group">
            <label>Đơn hàng tối thiểu (VND)</label>
            <input
              type="number"
              class="input"
              v-model.number="form.donHangToiThieu"
              min="0"
              placeholder="Ví dụ: 200000"
            />
          </div>

          <div class="form-group">
            <label>Ngày kết thúc</label>
            <input
              type="date"
              class="input input-sm"
              v-model="form.ngayKetThuc"
            />
            <small class="hint" v-if="form.ngayKetThuc">
              Tự gửi: {{ form.ngayKetThuc }}T23:59:59
            </small>
          </div>
        </div>

        <!-- MÔ TẢ -->
        <div class="form-group full">
          <label>Mô tả</label>
          <textarea
            class="textarea"
            v-model.trim="form.moTa"
            placeholder="Nhập mô tả..."
          ></textarea>
        </div>

        <!-- ✅ LOẠI PHIẾU -->
        <div class="form-group full">
          <label>Loại phiếu</label>
          <div class="radio-group">
            <label class="radio">
              <input type="radio" value="CONG_KHAI" v-model="form.loaiPhieu" />
              <span>Công khai</span>
            </label>
            <label class="radio">
              <input type="radio" value="CA_NHAN" v-model="form.loaiPhieu" />
              <span>Cá nhân</span>
            </label>
          </div>
        </div>

        <!-- ✅ BẢNG KHÁCH HÀNG (chỉ hiện khi Cá nhân) -->
        <div v-if="form.loaiPhieu === 'CA_NHAN'" class="form-group full">
          <label>Chọn khách hàng áp dụng</label>

          <div class="kh-toolbar">
            <input
              class="input"
              v-model.trim="customerKeyword"
              placeholder="Tìm theo mã / tên / sđt / email..."
            />
            <button class="btn btn-secondary" type="button" @click="toggleSelectAll">
              {{ isAllSelected ? 'Bỏ chọn tất cả' : 'Chọn tất cả' }}
            </button>
          </div>

          <div class="hint" style="margin-top: 6px;">
            Đã chọn: <b>{{ selectedCustomerIds.length }}</b> khách hàng
          </div>

          <div class="kh-table-wrap">
            <div v-if="loadingCustomers" class="muted">Đang tải khách hàng...</div>
            <div v-else-if="customersError" class="muted err">{{ customersError }}</div>

            <table v-else class="kh-table">
              <thead>
                <tr>
                  <th style="width: 44px;"></th>
                  <th>Mã</th>
                  <th>Tên</th>
                  <th>SĐT</th>
                  <th>Email</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="c in filteredCustomers" :key="c.id">
                  <td>
                    <input
                      type="checkbox"
                      :checked="selectedCustomerIds.includes(c.id)"
                      @change="toggleCustomer(c.id)"
                    />
                  </td>
                  <td>{{ c.maKhachHang ?? c.ma ?? c.id }}</td>
                  <td>{{ c.tenKhachHang ?? c.ten ?? '-' }}</td>
                  <td>{{ c.soDienThoai ?? c.sdt ?? '-' }}</td>
                  <td>{{ c.email ?? '-' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div v-if="error" class="error">{{ error }}</div>

      <!-- footer actions -->
      <div class="actions">
        <button class="btn btn-secondary" @click="goBack">Hủy</button>
        <button class="btn btn-primary" @click="submit" :disabled="saving">
          {{ saving ? 'Đang lưu...' : 'Lưu phiếu giảm giá' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from "vue"
import axios from "axios"
import { useRouter } from "vue-router"

const router = useRouter()
const API = "http://localhost:8080/api/pgg"
const KH_API = "http://localhost:8080/api/khach-hang"

const saving = ref(false)
const error = ref("")

// ===== KH state =====
const customers = ref([])
const loadingCustomers = ref(false)
const customersError = ref("")
const customerKeyword = ref("")
const selectedCustomerIds = ref([]) // array of Long

const isAllSelected = computed(() => {
  const list = filteredCustomers.value
  if (!list.length) return false
  return list.every(c => selectedCustomerIds.value.includes(c.id))
})

const filteredCustomers = computed(() => {
  const kw = customerKeyword.value.trim().toLowerCase()
  if (!kw) return customers.value
  return customers.value.filter(c => {
    const s = `${c.maKhachHang ?? c.ma ?? ""} ${c.tenKhachHang ?? c.ten ?? ""} ${c.soDienThoai ?? c.sdt ?? ""} ${c.email ?? ""}`.toLowerCase()
    return s.includes(kw)
  })
})

function toggleCustomer(id) {
  const ids = [...selectedCustomerIds.value]
  const idx = ids.indexOf(id)
  if (idx >= 0) ids.splice(idx, 1)
  else ids.push(id)
  selectedCustomerIds.value = ids
}

function toggleSelectAll() {
  const listIds = filteredCustomers.value.map(c => c.id)
  if (!listIds.length) return

  if (isAllSelected.value) {
    // bỏ chọn toàn bộ trong filtered list
    selectedCustomerIds.value = selectedCustomerIds.value.filter(id => !listIds.includes(id))
  } else {
    // chọn toàn bộ trong filtered list
    const set = new Set(selectedCustomerIds.value)
    listIds.forEach(id => set.add(id))
    selectedCustomerIds.value = Array.from(set)
  }
}

async function loadCustomers() {
  loadingCustomers.value = true
  customersError.value = ""
  try {
    const res = await axios.get(KH_API)
    customers.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    customersError.value = e?.response?.data?.message || e?.message || "Không tải được khách hàng"
  } finally {
    loadingCustomers.value = false
  }
}

// ===== Form =====
const form = ref({
  tenGiamGia: "",
  soLuong: 1,
  loaiGiam: true,            // true = %, false = tiền
  giaTriGiam: 1,
  giaTriGiamToiDa: 0,
  donHangToiThieu: 0,
  ngayBatDau: "",            // input date: YYYY-MM-DD
  ngayKetThuc: "",           // input date: YYYY-MM-DD
  moTa: "",
  loaiPhieu: "CONG_KHAI"     // UI radio
})

watch(() => form.value.loaiGiam, (isPercent) => {
  if (!isPercent) form.value.giaTriGiamToiDa = 0
})

// Khi chọn Cá nhân -> load KH
watch(() => form.value.loaiPhieu, async (v) => {
  if (v === "CA_NHAN") {
    if (customers.value.length === 0) await loadCustomers()
  } else {
    // reset chọn KH
    selectedCustomerIds.value = []
    customerKeyword.value = ""
  }
})

function goBack() {
  router.push("/vouchers")
}

// ===== DateTime helpers =====
function toStartOfDay(dateYMD) {
  if (!dateYMD) return null
  return `${dateYMD}T00:00:00`
}
function toEndOfDay(dateYMD) {
  if (!dateYMD) return null
  return `${dateYMD}T23:59:59`
}

function validate() {
  if (!form.value.tenGiamGia.trim()) return "Vui lòng nhập tên phiếu"
  if (form.value.soLuong < 1) return "Số lượng phải >= 1"
  if (form.value.giaTriGiam < 1) return "Giá trị giảm phải >= 1"

  if (form.value.loaiGiam) {
    if (form.value.giaTriGiam > 100) return "Giảm % tối đa 100"
    if (form.value.giaTriGiamToiDa <= 0) return "Giảm tối đa phải > 0"
  }

  // so sánh ngày theo chuỗi YYYY-MM-DD ok
  if (form.value.ngayBatDau && form.value.ngayKetThuc && form.value.ngayKetThuc < form.value.ngayBatDau) {
    return "Ngày kết thúc phải >= ngày bắt đầu"
  }

  if (form.value.loaiPhieu === "CA_NHAN" && selectedCustomerIds.value.length === 0) {
    return "Phiếu cá nhân phải chọn ít nhất 1 khách hàng"
  }

  return ""
}

async function submit() {
  error.value = ""
  const msg = validate()
  if (msg) return alert(msg)

  saving.value = true
  try {
    const payload = {
      tenGiamGia: form.value.tenGiamGia,
      soLuong: form.value.soLuong,
      loaiGiam: form.value.loaiGiam,

      // ✅ gửi datetime (00:00:00 / 23:59:59)
      ngayBatDau: toStartOfDay(form.value.ngayBatDau),
      ngayKetThuc: toEndOfDay(form.value.ngayKetThuc),

      moTa: form.value.moTa,
      donHangToiThieu: form.value.donHangToiThieu,
      loaiPhieu: form.value.loaiPhieu === "CA_NHAN" // backend: true=CA_NHAN
    }

    if (payload.loaiGiam) {
      payload.giaTriPhanTram = form.value.giaTriGiam
      payload.giaTriGiamToiDa = form.value.giaTriGiamToiDa
      payload.giaTriTienMat = null
    } else {
      payload.giaTriTienMat = form.value.giaTriGiam
      payload.giaTriPhanTram = null
      payload.giaTriGiamToiDa = null
    }

    // ✅ Phiếu cá nhân: gửi danh sách KH
    if (payload.loaiPhieu === true) {
      payload.khachHangIds = [...selectedCustomerIds.value]
    }

    await axios.post(`${API}/create`, payload)
    alert("Tạo phiếu giảm giá thành công")
    goBack()
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || "Tạo thất bại"
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.page {
  padding: 18px 22px;
  background: #f5f6f8;
  min-height: 100vh;
}

.topbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.card {
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  padding: 16px 18px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px 18px;
}

.col {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group.full {
  grid-column: 1 / -1;
}

.radio-group {
  display: flex;
  gap: 20px;
}

.radio {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
}

.input {
  height: 38px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 0 12px;
}

.input-sm {
  height: 32px;
}

.textarea {
  min-height: 120px;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  padding: 10px;
}

.with-suffix {
  position: relative;
}
.with-suffix .input {
  padding-right: 72px;
}
.suffix {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  font-weight: 700;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 14px;
}

.btn {
  height: 38px;
  border-radius: 8px;
  padding: 0 14px;
  border: 1px solid #d1d5db;
  cursor: pointer;
}

.btn-primary {
  background: #f97316;
  border-color: #f97316;
  color: #fff;
  font-weight: 700;
}

.btn-secondary {
  background: #eef2f7;
}

.error {
  margin-top: 10px;
  color: #b91c1c;
}

.hint {
  font-size: 12px;
  color: #6b7280;
}

/* ===== KH table ===== */
.kh-toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
}

.kh-table-wrap {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  overflow: auto;
  max-height: 320px;
  background: #fff;
  margin-top: 10px;
}

.kh-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.kh-table thead tr {
  background: #f3f4f6;
}

.kh-table th,
.kh-table td {
  padding: 10px 10px;
  border-bottom: 1px solid #e5e7eb;
  text-align: left;
  white-space: nowrap;
}

.muted {
  color: #6b7280;
  font-weight: 600;
  padding: 10px;
}

.err { color: #b91c1c; }
</style>
