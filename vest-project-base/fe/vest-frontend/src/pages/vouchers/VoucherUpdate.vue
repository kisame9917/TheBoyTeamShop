  <template>
    <div class="page">
      <!-- top bar -->
      <div class="topbar">
        <h1>Cập nhật phiếu giảm giá</h1>

        <button class="btn btn-secondary" @click="goBack">
          ← Quay lại danh sách
        </button>
      </div>

      <div class="card">
        <div v-if="loading" class="muted">Đang tải dữ liệu...</div>

        <div v-else>
          <div class="form-grid">
            <!-- LEFT -->
            <div class="col">
              <div class="form-group">
                <label>Loại giảm</label>
                <select class="input" v-model="form.loaiGiam" :disabled="isEnded">
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
                    :disabled="isEnded"
                  />
                  <span class="suffix">{{ form.loaiGiam ? "%" : "VND" }}</span>
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
                  :disabled="isEnded"
                />
              </div>

              <div class="form-group">
                <label>Ngày bắt đầu</label>
                <input
                  type="date"
                  class="input input-sm"
                  v-model="form.ngayBatDau"
                  :disabled="isEnded"
                />
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
                  :disabled="isEnded"
                />
              </div>

              <div class="form-group">
                <label>Số lượng</label>
                <input
                  type="number"
                  class="input"
                  v-model.number="form.soLuong"
                  min="1"
                  :disabled="isEnded"
                />
              </div>

              <div class="form-group">
                <label>Đơn hàng tối thiểu (VND)</label>
                <input
                  type="number"
                  class="input"
                  v-model.number="form.donHangToiThieu"
                  min="0"
                  placeholder="Ví dụ: 200000"
                  :disabled="isEnded"
                />
              </div>

              <div class="form-group">
                <label>Ngày kết thúc</label>
                <input
                  type="date"
                  class="input input-sm"
                  v-model="form.ngayKetThuc"
                  :disabled="isEnded"
                />
              </div>
            </div>

            <!-- MÔ TẢ -->
            <div class="form-group full">
              <label>Mô tả</label>
              <textarea
                class="textarea"
                v-model.trim="form.moTa"
                placeholder="Nhập mô tả..."
                :disabled="isEnded"
              ></textarea>
            </div>

            <!-- ✅ LOẠI PHIẾU -->
            <div class="form-group full">
              <label>Loại phiếu</label>

              <div class="radio-group">
                <label class="radio">
                  <input
                    type="radio"
                    value="CONG_KHAI"
                    v-model="form.loaiPhieu"
                    :disabled="isEnded || loaiPhieuLocked"
                  />
                  <span>Công khai</span>
                </label>

                <label class="radio">
                  <input
                    type="radio"
                    value="CA_NHAN"
                    v-model="form.loaiPhieu"
                    :disabled="isEnded || loaiPhieuLocked"
                  />
                  <span>Cá nhân</span>
                </label>
              </div>

              <div class="muted2" v-if="loaiPhieuLocked && !isEnded">
              </div>
            </div>

            <!-- ✅ KHÁCH HÀNG (chỉ hiện khi cá nhân) -->
            <div v-if="isCaNhan" class="form-group full">
              <div class="kh-header">
                <div>
                  <label style="margin: 0;">Danh sách khách hàng nhận phiếu</label>
                </div>

                <div class="kh-actions">
                  <input
                    class="input input-kh"
                    v-model.trim="khSearch"
                    placeholder="Tìm theo mã / tên / SĐT / email..."
                    :disabled="isEnded"
                  />

                  <button
                    class="btn btn-outline btn-kh"
                    @click="toggleSelectAll"
                    :disabled="isEnded || khLoading"
                    type="button"
                  >
                    {{ isAllSelected ? "Bỏ chọn tất cả" : "Chọn tất cả" }}
                  </button>
                </div>
              </div>

              <div v-if="khLoading" class="muted" style="margin-top:8px;">
                Đang tải danh sách khách hàng...
              </div>

              <div v-else class="kh-table-wrap">
                <table class="kh-table">
                  <thead>
                    <tr>
                      <th class="col-check"></th>
                      <th class="col-ma">Mã KH</th>
                      <th class="col-ten">Tên KH</th>
                      <th class="col-sdt">SĐT</th>
                      <th class="col-email">Email</th>
                    </tr>
                  </thead>

                  <tbody>
                    <tr v-for="kh in filteredKhachHang" :key="kh.id">
                      <td class="col-check">
                        <input
                          type="checkbox"
                          :checked="selectedKhIds.has(kh.id)"
                          @change="onToggleKh(kh.id)"
                          :disabled="isEnded"
                        />
                      </td>
                      <td class="col-ma">{{ kh.maKhachHang || "-" }}</td>
                      <td class="col-ten text-bold">{{ kh.tenKhachHang || "-" }}</td>
                      <td class="col-sdt">{{ kh.soDienThoai || "-" }}</td>
                      <td class="col-email ellipsis" :title="kh.email || ''">
                        {{ kh.email || "-" }}
                      </td>
                    </tr>

                    <tr v-if="filteredKhachHang.length === 0">
                      <td colspan="5" class="empty">Không có khách hàng phù hợp.</td>
                    </tr>
                  </tbody>
                </table>

                <div class="kh-footer">
                  <div>Đã chọn: <b>{{ selectedKhIds.size }}</b> khách hàng</div>
                </div>
              </div>
            </div>
          </div>

          <div v-if="error" class="error">{{ error }}</div>

          <!-- footer actions -->
          <div class="actions">
            <button class="btn btn-secondary" @click="goBack" :disabled="saving">
              Hủy
            </button>

            <button
              class="btn btn-primary"
              @click="submit"
              :disabled="saving || isEnded"
              :title="isEnded ? 'Phiếu đã kết thúc, không thể chỉnh sửa' : ''"
            >
              {{ saving ? "Đang lưu..." : "Cập nhật phiếu giảm giá" }}
            </button>
          </div>

          <div v-if="isEnded" class="ended-note">
            Phiếu đã <b>kết thúc</b> nên không thể chỉnh sửa.
          </div>
        </div>
      </div>

      <!-- ✅ POPUP (Alert/Confirm) -->
      <div v-if="popup.open" class="modal-overlay" @click.self="closePopup">
        <div class="modal-card">
          <h3 class="modal-title">{{ popup.title }}</h3>
          <p class="modal-desc">{{ popup.message }}</p>

          <div class="modal-actions">
            <!-- ALERT -->
            <button
              v-if="popup.mode === 'alert'"
              class="btn btn-secondary"
              type="button"
              @click="closePopup"
            >
              Đóng
            </button>

            <!-- CONFIRM -->
            <template v-else>
              <button
                class="btn btn-secondary"
                type="button"
                :disabled="popup.loading"
                @click="closePopup"
              >
                Hủy
              </button>
              <button
                class="btn btn-primary"
                type="button"
                :disabled="popup.loading"
                @click="confirmPopup"
              >
                {{ popup.loading ? "Đang xử lý..." : "Đồng ý" }}
              </button>
            </template>
          </div>
        </div>
      </div>
    </div>
  </template>

  <script setup>
  import { ref, watch, computed, onMounted } from "vue"
  import axios from "axios"
  import { useRouter, useRoute } from "vue-router"
  import { useToast } from "@/composables/useToast"   // ✅ đưa lên đúng chỗ

  const toast = useToast() // ✅ tạo toast instance

  const router = useRouter()
  const route = useRoute()

  const API = "http://localhost:8080/api/pgg"
  const API_KH = "http://localhost:8080/api/khach-hang"

  // ✅ Lấy id từ route: /vouchers/update/:id
  const id = route.params.id

  const loading = ref(true)
  const saving = ref(false)
  const error = ref("")

  // ===== Popup state (confirm) =====
  const popup = ref({
    open: false,
    mode: "alert", // 'alert' | 'confirm'
    title: "Thông báo",
    message: "",
    loading: false,
  })
  let popupAction = null

  function openAlert(message, title = "Thông báo") {
    popup.value.open = true
    popup.value.mode = "alert"
    popup.value.title = title
    popup.value.message = message || ""
    popup.value.loading = false
    popupAction = null
  }

  function openConfirm(message, action, title = "Xác nhận") {
    popup.value.open = true
    popup.value.mode = "confirm"
    popup.value.title = title
    popup.value.message = message || ""
    popup.value.loading = false
    popupAction = typeof action === "function" ? action : null
  }

  function closePopup() {
    if (popup.value.loading) return
    popup.value.open = false
    popupAction = null
  }

  async function confirmPopup() {
    if (!popupAction) {
      closePopup()
      return
    }
    popup.value.loading = true
    try {
      await popupAction()
    } finally {
      popup.value.loading = false
      popup.value.open = false
      popupAction = null
    }
  }

  /**
   * ✅ KHÓA ĐỔI LOẠI PHIẾU 2 CHIỀU (update không cho đổi)
   */
  const originalLoaiPhieu = ref("CONG_KHAI") // "CA_NHAN" | "CONG_KHAI"
  const loaiPhieuLocked = computed(() => true) // khóa luôn 2 chiều khi cập nhật

  const form = ref({
    tenGiamGia: "",
    soLuong: 1,
    loaiGiam: true,
    giaTriGiam: 1,
    giaTriGiamToiDa: 0,
    donHangToiThieu: 0,
    ngayBatDau: "",
    ngayKetThuc: "",
    moTa: "",
    loaiPhieu: "CONG_KHAI",
  })

  const isCaNhan = computed(() => form.value.loaiPhieu === "CA_NHAN")

  // ====== DETAIL RAW (để tính trạng thái nghiệp vụ / end) ======
  const detailRaw = ref(null)

  // trạng thái nghiệp vụ: kết thúc -> disable form
  function toDate(v) {
    if (!v) return null
    const dt = new Date(String(v))
    return Number.isNaN(dt.getTime()) ? null : dt
  }
  const isEnded = computed(() => {
    const end = toDate(detailRaw.value?.ngayKetThuc)
    if (!end) return false
    return new Date() > end
  })

  watch(
    () => form.value.loaiGiam,
    (isPercent) => {
      if (!isPercent) form.value.giaTriGiamToiDa = 0
    }
  )

  function goBack() {
    router.push("/vouchers")
  }

  /** input date cần YYYY-MM-DD */
  function normalizeDate(d) {
    if (!d) return ""
    return String(d).slice(0, 10)
  }

  /** ✅ convert date -> datetime theo yêu cầu */
  function startOfDay(dateYMD) {
    if (!dateYMD) return null
    return `${dateYMD}T00:00:00`
  }
  function endOfDay(dateYMD) {
    if (!dateYMD) return null
    return `${dateYMD}T23:59:59`
  }

  async function fetchDetail() {
    loading.value = true
    error.value = ""
    try {
      const res = await axios.get(`${API}/${id}`)
      const data = res.data || {}
      detailRaw.value = data

      form.value.tenGiamGia = data.tenGiamGia ?? ""
      form.value.soLuong = data.soLuong ?? 1
      form.value.loaiGiam = !!data.loaiGiam

      form.value.ngayBatDau = normalizeDate(data.ngayBatDau)
      form.value.ngayKetThuc = normalizeDate(data.ngayKetThuc)

      form.value.moTa = data.moTa ?? ""
      form.value.donHangToiThieu = data.donHangToiThieu ?? 0

      // ✅ BE loaiPhieu boolean: true = cá nhân, false = công khai
      const isCaNhanBE = !!data.loaiPhieu
      originalLoaiPhieu.value = isCaNhanBE ? "CA_NHAN" : "CONG_KHAI"
      form.value.loaiPhieu = originalLoaiPhieu.value

      if (form.value.loaiGiam) {
        form.value.giaTriGiam = data.giaTriPhanTram ?? 1
        form.value.giaTriGiamToiDa = data.giaTriGiamToiDa ?? 0
      } else {
        form.value.giaTriGiam = data.giaTriTienMat ?? 1
        form.value.giaTriGiamToiDa = 0
      }
    } catch (e) {
      error.value = e?.response?.data?.message || e?.message || "Không tải được dữ liệu"
      openAlert(error.value, "Lỗi")
    } finally {
      loading.value = false
    }
  }

  // ====== KHÁCH HÀNG (CÁ NHÂN) ======
  const khLoading = ref(false)
  const khList = ref([])
  const selectedKhIds = ref(new Set())
  const khSearch = ref("")

  function normText(v) {
    return String(v ?? "").trim().toLowerCase()
  }

  const filteredKhachHang = computed(() => {
    const q = normText(khSearch.value)
    if (!q) return khList.value

    return khList.value.filter((kh) => {
      const s = [kh.maKhachHang, kh.tenKhachHang, kh.soDienThoai, kh.email]
        .map(normText)
        .join(" ")
      return s.includes(q)
    })
  })

  const isAllSelected = computed(() => {
    const ids = filteredKhachHang.value.map((x) => x.id)
    if (ids.length === 0) return false
    return ids.every((id) => selectedKhIds.value.has(id))
  })

  function onToggleKh(khId) {
    const s = new Set(selectedKhIds.value)
    if (s.has(khId)) s.delete(khId)
    else s.add(khId)
    selectedKhIds.value = s
  }

  function toggleSelectAll() {
    const s = new Set(selectedKhIds.value)
    const ids = filteredKhachHang.value.map((x) => x.id)
    const all = ids.every((id) => s.has(id))
    if (all) ids.forEach((id) => s.delete(id))
    else ids.forEach((id) => s.add(id))
    selectedKhIds.value = s
  }

  async function loadKhachHangForPersonal() {
    if (!isCaNhan.value) return

    khLoading.value = true
    try {
      const all = await axios.get(API_KH)
      const allKh = Array.isArray(all.data) ? all.data : []

      const khNorm = allKh.map((x) => ({
        id: x.id,
        maKhachHang: x.maKhachHang ?? x.ma_khach_hang,
        tenKhachHang: x.tenKhachHang ?? x.ten_khach_hang,
        soDienThoai: x.soDienThoai ?? x.so_dien_thoai,
        email: x.email,
      }))

      const received = await axios.get(`${API}/${id}/khach-hang`)
      const ds = Array.isArray(received.data) ? received.data : []

      const mapByKhId = new Map()
      ds.forEach((r) => {
        const khId = r.idKhachHang ?? r.id_khach_hang
        if (!khId) return
        mapByKhId.set(khId, {
          soDienThoai: r.sdt ?? r.soDienThoai ?? r.so_dien_thoai,
          email: r.email,
        })
      })

      const merged = khNorm.map((kh) => {
        const join = mapByKhId.get(kh.id)
        return {
          ...kh,
          soDienThoai: kh.soDienThoai || join?.soDienThoai || null,
          email: kh.email || join?.email || null,
        }
      })

      khList.value = merged

      const selected = new Set()
      mapByKhId.forEach((_, khId) => selected.add(khId))
      selectedKhIds.value = selected
    } finally {
      khLoading.value = false
    }
  }

  watch(isCaNhan, async (v) => {
    if (v) {
      await loadKhachHangForPersonal()
    } else {
      khList.value = []
      selectedKhIds.value = new Set()
    }
  })

  // ====== SUBMIT ======
 function validate() {
  if (!form.value.tenGiamGia.trim()) return "Vui lòng nhập tên phiếu"
  if (form.value.soLuong < 1) return "Số lượng phải >= 1"
  if (form.value.giaTriGiam < 1) return "Giá trị giảm phải >= 1"

  // ✅ Đơn hàng tối thiểu
  if (form.value.donHangToiThieu < 0) return "Đơn hàng tối thiểu phải >= 0"

  // ✅ Tiền giảm không vượt đơn tối thiểu (chỉ áp dụng khi giảm tiền)
  if (!form.value.loaiGiam && form.value.giaTriGiam > form.value.donHangToiThieu) {
    return "Tiền giảm không được vượt quá đơn hàng tối thiểu"
  }

  if (form.value.loaiGiam) {
    if (form.value.giaTriGiam > 100) return "Giảm % tối đa 100"
    if (form.value.giaTriGiamToiDa <= 0) return "Giảm tối đa phải > 0"
  }

  if (
    form.value.ngayBatDau &&
    form.value.ngayKetThuc &&
    form.value.ngayKetThuc < form.value.ngayBatDau
  ) {
    return "Ngày kết thúc phải >= ngày bắt đầu"
  }

  if (form.value.loaiPhieu !== originalLoaiPhieu.value) {
    return "Không thể đổi loại phiếu khi cập nhật"
  }

  if (form.value.loaiPhieu === "CA_NHAN" && selectedKhIds.value.size === 0) {
    return "Phiếu cá nhân phải chọn ít nhất 1 khách hàng"
  }

  return ""
}

  async function doUpdate() {
    const payload = {
      tenGiamGia: form.value.tenGiamGia,
      soLuong: form.value.soLuong,
      loaiGiam: form.value.loaiGiam,
      ngayBatDau: startOfDay(form.value.ngayBatDau),
      ngayKetThuc: endOfDay(form.value.ngayKetThuc),
      moTa: form.value.moTa,
      donHangToiThieu: form.value.donHangToiThieu,
      loaiPhieu: form.value.loaiPhieu === "CA_NHAN",
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

    await axios.put(`${API}/update/${id}`, payload)

    if (form.value.loaiPhieu === "CA_NHAN") {
      const req = { khachHangIds: Array.from(selectedKhIds.value) }
      await axios.put(`${API}/${id}/khach-hang`, req)
    }
  }

  async function submit() {
    error.value = ""
    const msg = validate()
    if (msg) {
      toast.warning(msg) // ✅ toast cảnh báo thay vì popup
      return
    }

    openConfirm("Bạn có chắc muốn cập nhật phiếu giảm giá này không?", async () => {
      saving.value = true
      try {
        await doUpdate() // ✅ đợi API xong
        toast.success("Cập nhật phiếu giảm giá thành công") // ✅ chỉ hiện khi OK
        goBack()
      } catch (e) {
        const m = e?.response?.data?.message || e?.message || "Cập nhật thất bại"
        error.value = m
        toast.error(m)
        throw e
      } finally {
        saving.value = false
      }
    })
  }

  onMounted(async () => {
    await fetchDetail()
    if (isCaNhan.value) await loadKhachHangForPersonal()
  })
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

  .muted {
    color: #6b7280;
    font-weight: 600;
  }
  .muted2 {
    color: #6b7280;
    font-size: 12px;
    margin-top: 6px;
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
    background: #fff;
  }

  .input-sm {
    height: 32px;
  }

  .textarea {
    min-height: 120px;
    border-radius: 8px;
    border: 1px solid #d1d5db;
    padding: 10px;
    background: #fff;
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
    background: #fff;
  }

  .btn-secondary {
    background: #eef2f7;
  }
  .btn-outline {
    height: 34px;
    border-radius: 8px;
    background: #fff;
  }

  .error {
    margin-top: 10px;
    color: #b91c1c;
    font-weight: 600;
  }

  .ended-note {
    margin-top: 10px;
    padding: 10px 12px;
    border-radius: 8px;
    background: #fff7ed;
    border: 1px solid #fed7aa;
    color: #9a3412;
    font-size: 13px;
  }

  /* ===== KH TABLE ===== */
  .kh-table-wrap {
    margin-top: 10px;
    border: 1px solid #e5e7eb;
    border-radius: 10px;
    overflow: hidden;
    background: #fff;
  }

  .kh-header{
    display:flex;
    align-items:flex-end;
    justify-content:space-between;
    gap: 12px;
  }

  .kh-actions{
    display:flex;
    gap:10px;
    align-items:center;
    flex-wrap:wrap;
  }

  .input-kh{
    width: 320px;
    height: 34px;
  }

  .btn-kh{
    height: 34px;
  }

  .kh-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 13px;
    table-layout: fixed;
  }

  .kh-table thead tr {
    background: #f3f4f6;
  }

  .kh-table th,
  .kh-table td {
    padding: 12px 10px;
    border-bottom: 1px solid #e5e7eb;
    text-align: left;
    vertical-align: middle;

    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .col-check { width: 44px; }
  .col-ma    { width: 140px; }
  .col-ten   { width: 320px; }
  .col-sdt   { width: 160px; }
  .col-email { width: 220px; }

  .text-bold { font-weight: 700; }

  .ellipsis {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .kh-footer{
    padding: 10px 12px;
    font-size: 13px;
    color:#374151;
  }

  /* ✅ Modal */
  .modal-overlay{
    position: fixed;
    inset: 0;
    background: rgba(0,0,0,.45);
    display:flex;
    align-items:center;
    justify-content:center;
    z-index: 9999;
  }
  .modal-card{
    width: min(440px, calc(100% - 32px));
    background:#fff;
    border-radius:14px;
    padding:18px 18px 14px;
    box-shadow: 0 10px 30px rgba(0,0,0,.2);
  }
  .modal-title{ margin:0 0 8px; font-size:18px; font-weight:800; color:#111827; }
  .modal-desc{ margin:0 0 14px; color:#555; line-height:1.4; }
  .modal-actions{
    display:flex;
    gap:10px;
    justify-content:flex-end;
  }
  .btn-primary{
    background: #1f2a44;
    border-color: #1f2a44;
    color: #fff;
    font-weight: 700;
  }
  .btn-primary:hover{
    filter: brightness(0.95);
  }
  .btn-primary:disabled{
    opacity: .6;
    cursor: not-allowed;
  }

  </style>
