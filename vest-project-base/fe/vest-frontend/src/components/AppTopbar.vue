<template>
  <header class="topbar" @click.stop>
    <!-- LEFT -->
    <div class="left">
      <div class="title">{{ resolvedTitle }}</div>
      <div class="subtitle">{{ resolvedSubtitle }}</div>
    </div>

    <!-- RIGHT -->
    <div class="right">
      <!-- STAFF chế độ xem (tách khỏi icon chuông) -->
      <!-- ✅ Nhấn để mở lại modal vào ca (tránh trường hợp user lỡ bấm 'Chế độ xem' rồi không biết mở lại) -->
      <button
          v-if="showViewBadge"
          class="view-mode-pill"
          type="button"
          title="Nhấn để mở lại màn hình vào ca"
          @click="openGate"
      >
        Chế độ xem
      </button>

      <!-- Bell / Notifications -->
      <div class="dd-wrap">
        <button class="icon-btn" type="button" @click.stop="toggleNoti" aria-label="Thông báo">
          <svg viewBox="0 0 24 24" class="icon" aria-hidden="true">
            <path
                d="M12 22a2.5 2.5 0 0 0 2.45-2h-4.9A2.5 2.5 0 0 0 12 22Zm7-6V11a7 7 0 1 0-14 0v5l-2 2v1h18v-1l-2-2Z"
                fill="currentColor"
            />
          </svg>
          <span v-if="unreadCount > 0" class="badge">{{ unreadCount }}</span>
        </button>

        <div v-if="showNoti" class="dropdown">
          <div class="dropdown-head">
            <div class="dropdown-title">Thông báo</div>
            <button class="link-btn" type="button" @click="markAllRead" :disabled="notificationsLocal.length === 0">
              Đã đọc tất cả
            </button>
          </div>

          <div v-if="notificationsLocal.length === 0" class="empty">
            Không có thông báo.
          </div>

          <ul v-else class="list">
            <li
                v-for="n in notificationsLocal"
                :key="n.id"
                class="item"
                :class="{ unread: !n.read }"
                @click="openNoti(n)"
            >
              <div class="item-main">
                <div class="item-title">{{ n.title }}</div>
                <div class="item-time">{{ n.time }}</div>
              </div>

              <button
                  v-if="!n.read"
                  class="mini"
                  type="button"
                  title="Đánh dấu đã đọc"
                  @click.stop="markRead(n.id)"
              >
                ✓
              </button>
            </li>
          </ul>

          <div class="dropdown-foot">
            <RouterLink class="view-all" to="/admin/notifications">Xem tất cả</RouterLink>
          </div>
        </div>
      </div>

      <!-- User -->
      <div class="dd-wrap">
        <button class="user-btn" type="button" @click.stop="toggleUser">
          <span class="avatar">{{ initials }}</span>
          <span class="user-name">{{ admin.name }}</span>

          <svg viewBox="0 0 24 24" class="chev" aria-hidden="true">
            <path d="M7 10l5 5 5-5" fill="none" stroke="currentColor" stroke-width="2" />
          </svg>
        </button>

        <div v-if="showUser" class="dropdown user-dd">
          <div class="user-card">
            <div class="u-name">{{ admin.name }}</div>
            <div class="u-mail">{{ admin.email }}</div>
          </div>

          <div class="divider"></div>

          <RouterLink class="menu" to="/admin/profile">Hồ sơ</RouterLink>
          <RouterLink class="menu" to="/admin/settings">Cài đặt</RouterLink>

          <div class="divider"></div>

          <!-- ✅ Logout thật -->
          <button class="menu danger" type="button" @click="logout">
            Đăng xuất
          </button>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useShiftStore } from '@/stores/shift'

const props = defineProps({
  title: { type: String, default: '' },
  subtitle: { type: String, default: '' },
  notifications: {
    type: Array,
    default: () => ([
      { id: 1, title: 'Có đơn hàng mới', time: 'Vừa xong', read: false, link: '/app/orders' },
      { id: 2, title: 'Phiếu giảm giá sắp hết hạn', time: 'Hôm nay', read: true, link: '/app/vouchers' }
    ])
  }
})

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const shift = useShiftStore()

const showViewBadge = computed(() => auth.isStaff && shift.isLocked)

function openGate() {
  // mở gate ngay (modal hiện tức thì) và đồng thời refresh trạng thái từ BE
  shift.bootstrap(true)
}

// function openGate() {
//   // mở gate ngay + sync lại state từ BE
//   shift.bootstrap(true)
// }

// ===== Role helper =====
const role = computed(() => {
  // ưu tiên lấy từ store nếu có
  const storeRole = auth?.role
  if (storeRole) return String(storeRole).toUpperCase()

  // fallback localStorage (đổi key nếu bạn dùng key khác)
  const lsRole = localStorage.getItem('role') || localStorage.getItem('vest_role') || ''
  return String(lsRole).toUpperCase()
})

const displayNameByRole = computed(() => (role.value === 'STAFF' ? 'Staff' : 'Admin'))

// Title hiển thị
const resolvedTitle = computed(() => props.title || route.meta?.title || 'Vest Shop')

// User hiển thị
const admin = ref({
  name: displayNameByRole.value,
  email: role.value === 'STAFF' ? 'staff@vestshop.local' : 'admin@vestshop.local'
})

// Khi role thay đổi (login/logout), update lại user hiển thị
watch(displayNameByRole, (name) => {
  admin.value.name = name
  admin.value.email = role.value === 'STAFF' ? 'staff@vestshop.local' : 'admin@vestshop.local'
})

// copy notifications để có thể mark read mà không mutate props trực tiếp
const notificationsLocal = ref([...props.notifications])
watch(
    () => props.notifications,
    (val) => (notificationsLocal.value = [...(val || [])]),
    { deep: true }
)

const showNoti = ref(false)
const showUser = ref(false)

// ===== TIME =====
const now = ref(new Date())
let timer

onMounted(() => {
  timer = setInterval(() => {
    now.value = new Date()
  }, 60_000)
  document.addEventListener('click', closeAll)
})

onBeforeUnmount(() => {
  clearInterval(timer)
  document.removeEventListener('click', closeAll)
})

const resolvedSubtitle = computed(() => {
  if (props.subtitle) return props.subtitle
  if (route.meta?.subtitle) return route.meta.subtitle

  const datePart = new Intl.DateTimeFormat('vi-VN', {
    weekday: 'long',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  }).format(now.value)

  const timePart = new Intl.DateTimeFormat('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  }).format(now.value)

  return `${datePart} • ${timePart}`
})

const unreadCount = computed(() => notificationsLocal.value.filter(n => !n.read).length)

const initials = computed(() => {
  const name = (admin.value.name || '').trim() || 'AD'
  const parts = name.split(/\s+/).slice(0, 2)
  return parts.map(p => p[0]?.toUpperCase()).join('')
})

function toggleNoti() {
  showNoti.value = !showNoti.value
  if (showNoti.value) showUser.value = false
}
function toggleUser() {
  showUser.value = !showUser.value
  if (showUser.value) showNoti.value = false
}

function closeAll() {
  showNoti.value = false
  showUser.value = false
}

function markRead(id) {
  const n = notificationsLocal.value.find(x => x.id === id)
  if (n) n.read = true
}
function markAllRead() {
  notificationsLocal.value.forEach(n => (n.read = true))
}

function openNoti(n) {
  if (!n.read) n.read = true
  closeAll()
  if (n.link) router.push(n.link)
}

// Logout
function logout() {
  closeAll()
  auth.logout()
  router.replace('/login')
}
</script>

<style scoped>
.topbar{
  position: sticky;
  top: 0;
  z-index: 100;

  display:flex;
  align-items:center;
  justify-content:space-between;
  gap: 16px;

  height: 56px;
  padding: 0 16px;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
}

.left{ display:flex; flex-direction:column; line-height:1.1; }
.title{ font-weight: 800; font-size: 16px; color:#111827; }
.subtitle{ margin-top:4px; font-size: 12.5px; color:#6b7280; }

.right{ display:flex; align-items:center; gap: 10px; }

.view-mode-pill{
  height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  background: #0ea5e9;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  display:flex;
  align-items:center;
  border: none;
  cursor: pointer;
}
.view-mode-pill:hover{ filter: brightness(0.95); }
.dd-wrap{ position: relative; }

.icon-btn{
  position: relative;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background:#fff;
  cursor:pointer;
  display:flex;
  align-items:center;
  justify-content:center;
}
.icon-btn:hover{ background:#f9fafb; }
.icon{ width: 20px; height: 20px; color:#111827; }

.badge{
  position:absolute;
  top: -6px;
  right: -6px;
  min-width: 18px;
  height: 18px;
  padding: 0 6px;
  border-radius: 999px;
  background:#ef4444;
  color:#fff;
  font-size: 11px;
  display:flex;
  align-items:center;
  justify-content:center;
  border: 2px solid #fff;
}

.mode-badge{
  position:absolute;
  top: -6px;
  left: -6px;
  height: 18px;
  padding: 0 6px;
  border-radius: 999px;
  background:#0ea5e9;
  color:#fff;
  font-size: 11px;
  font-weight: 800;
  display:flex;
  align-items:center;
  justify-content:center;
  border: 2px solid #fff;
}

.user-btn{
  height: 40px;
  padding: 0 10px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background:#fff;
  cursor:pointer;
  display:flex;
  align-items:center;
  gap: 8px;
}
.user-btn:hover{ background:#f9fafb; }

.avatar{
  width: 26px;
  height: 26px;
  border-radius: 999px;
  background:#111827;
  color:#fff;
  display:flex;
  align-items:center;
  justify-content:center;
  font-weight: 800;
  font-size: 12px;
}
.user-name{ font-weight: 700; font-size: 14px; color:#111827; }
.chev{ width: 18px; height: 18px; color:#6b7280; }

.dropdown{
  position: absolute;
  right: 0;
  top: calc(100% + 10px);
  width: 320px;
  background:#fff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  box-shadow: 0 12px 30px rgba(0,0,0,.10);
  overflow: hidden;
}

.dropdown-head{
  display:flex;
  justify-content:space-between;
  align-items:center;
  padding: 10px 12px;
  border-bottom: 1px solid #f3f4f6;
}
.dropdown-title{ font-weight: 800; color:#111827; }

.link-btn{
  border:none;
  background:transparent;
  cursor:pointer;
  color:#2563eb;
  font-size: 13px;
}
.link-btn:disabled{ opacity:.5; cursor:not-allowed; }

.empty{ padding: 12px; color:#6b7280; font-size: 13px; }

.list{
  list-style:none;
  margin:0;
  padding: 6px;
  max-height: 320px;
  overflow:auto;
}
.item{
  display:flex;
  align-items:flex-start;
  justify-content:space-between;
  gap: 10px;
  padding: 10px;
  border-radius: 12px;
  cursor:pointer;
}
.item:hover{ background:#f9fafb; }
.item.unread{ background:#eff6ff; }

.item-title{ font-weight: 700; color:#111827; font-size: 13px; }
.item-time{ margin-top: 2px; font-size: 12px; color:#6b7280; }

.mini{
  width: 28px; height: 28px;
  border-radius: 10px;
  border: 1px solid #d1d5db;
  background:#fff;
  cursor:pointer;
}
.mini:hover{ background:#f9fafb; }

.dropdown-foot{
  padding: 10px 12px;
  border-top: 1px solid #f3f4f6;
}
.view-all{ color:#2563eb; font-size: 13px; text-decoration:none; }
.view-all:hover{ text-decoration:underline; }

.user-dd{ width: 240px; }
.user-card{ padding: 12px; }
.u-name{ font-weight: 900; color:#111827; }
.u-mail{ margin-top: 2px; font-size: 12.5px; color:#6b7280; }
.divider{ height:1px; background:#f3f4f6; }
.menu{
  display:block;
  padding: 10px 12px;
  text-decoration:none;
  color:#111827;
  background:#fff;
  border:none;
  width:100%;
  text-align:left;
  cursor:pointer;
  font-weight: 600;
}
.menu:hover{ background:#f9fafb; }
.menu.danger{ color:#dc2626; }
</style>
