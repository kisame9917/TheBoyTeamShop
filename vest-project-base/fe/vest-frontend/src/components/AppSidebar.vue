<template>
  <aside class="sidebar d-flex flex-column bg-white border-end">
    <!-- Logo -->
    <div class="brand-box p-3 border-bottom text-center">
      <img
        src="../images/tbt_4.png"
        alt="TheBoyTeam Logo"
        class="brand-logo img-fluid"
      />

      <div class="brand-text mt-2">
        <div class="brand-name">
<!--          <span class="brand-strong">TheBoyTeam</span>-->
        </div>
      </div>
    </div>

    <!-- Nav -->
    <nav class="p-2 flex-grow-1">
      <!-- Trang chủ (role-based) -->
      <RouterLink
  to="/dashboard"
  class="nav-link-item"
  active-class="active"
  exact-active-class="active"
>
  <i class="bi bi-house-door icon"></i>
  <span class="label">Trang Chủ</span>
</RouterLink>

      <!-- ADMIN only: Thống kê -->
      <RouterLink
        v-if="isAdmin"
        to="/statistic"
        class="nav-link-item"
        active-class="active"
      >
        <i class="bi bi-bar-chart icon"></i>
        <span class="label">Thống kê</span>
      </RouterLink>

      <!-- STAFF + ADMIN -->
      <RouterLink to="/sales" class="nav-link-item" active-class="active">
        <i class="bi bi-cart3 icon"></i>
        <span class="label">Bán Hàng</span>
      </RouterLink>

      <RouterLink to="/orders" class="nav-link-item" active-class="active">
        <i class="bi bi-receipt-cutoff icon"></i>
        <span class="label">Hóa đơn</span>
      </RouterLink>

      <!-- STAFF + ADMIN: Khách hàng (staff chỉ thấy cái này trong "Tài khoản") -->
      <RouterLink
        v-if="isStaff"
        to="/customers"
        class="nav-link-item"
        active-class="active"
      >
        <i class="bi bi-person-lines-fill icon"></i>
        <span class="label">Khách hàng</span>
      </RouterLink>

      <!-- ADMIN only: Group Products -->
      <div v-if="isAdmin" class="nav-group">
        <button
          type="button"
          class="nav-link-item w-100 justify-content-between"
          @click="toggleGroup('products')"
          :class="{ active: openGroups.products }"
        >
          <span class="d-flex align-items-center gap-2">
            <i class="bi bi-box-seam icon"></i>
            <span class="label">Sản phẩm</span>
          </span>
          <i class="bi bi-chevron-down caret" :class="{ rotate: openGroups.products }"></i>
        </button>

        <div v-if="openGroups.products" class="sub-wrap">
          <RouterLink to="/products" class="sub-link" active-class="active-sub">
            <i class="bi bi-list-ul sub-icon"></i>
            Danh sách sản phẩm
          </RouterLink>
          <RouterLink to="/variants" class="sub-link" active-class="active-sub">
            <i class="bi bi-grid-3x3-gap sub-icon"></i>
            Danh sách biến thể
          </RouterLink>
        </div>
      </div>

      <!-- ADMIN only: Group Attributes -->
      <div v-if="isAdmin" class="nav-group">
        <button
          type="button"
          class="nav-link-item w-100 justify-content-between"
          @click="toggleGroup('attributes')"
          :class="{ active: openGroups.attributes }"
        >
          <span class="d-flex align-items-center gap-2">
            <i class="bi bi-ui-checks-grid icon"></i>
            <span class="label">Thuộc tính</span>
          </span>
          <i class="bi bi-chevron-down caret" :class="{ rotate: openGroups.attributes }"></i>
        </button>

        <div v-if="openGroups.attributes" class="sub-wrap">
          <RouterLink to="/attributes/loai-san-pham" class="sub-link" active-class="active-sub">
            <i class="bi bi-tags sub-icon"></i> Loại sản phẩm
          </RouterLink>
          <RouterLink to="/attributes/thuong-hieu" class="sub-link" active-class="active-sub">
            <i class="bi bi-award sub-icon"></i> Thương hiệu
          </RouterLink>
          <RouterLink to="/attributes/so-khuy" class="sub-link" active-class="active-sub">
            <i class="bi bi-circle-square sub-icon"></i> Số khuy
          </RouterLink>
          <RouterLink to="/attributes/kieu-tui" class="sub-link" active-class="active-sub">
            <i class="bi bi-handbag sub-icon"></i> Kiểu túi
          </RouterLink>
          <RouterLink to="/attributes/ve-ao" class="sub-link" active-class="active-sub">
            <i class="bi bi-person-badge sub-icon"></i> Ve áo
          </RouterLink>
          <RouterLink to="/attributes/xe-ta" class="sub-link" active-class="active-sub">
            <i class="bi bi-scissors sub-icon"></i> Xẻ tà
          </RouterLink>
          <RouterLink to="/attributes/xuat-xu" class="sub-link" active-class="active-sub">
            <i class="bi bi-globe-asia-australia sub-icon"></i> Xuất xứ
          </RouterLink>
          <RouterLink to="/attributes/fit" class="sub-link" active-class="active-sub">
            <i class="bi bi-person-arms-up sub-icon"></i> Kiểu dáng
          </RouterLink>
          <RouterLink to="/attributes/chat-lieu" class="sub-link" active-class="active-sub">
            <i class="bi bi-layers sub-icon"></i> Chất liệu
          </RouterLink>
          <RouterLink to="/attributes/mau-sac" class="sub-link" active-class="active-sub">
            <i class="bi bi-palette sub-icon"></i> Màu sắc
          </RouterLink>
          <RouterLink to="/attributes/kich-co" class="sub-link" active-class="active-sub">
            <i class="bi bi-rulers sub-icon"></i> Kích cỡ
          </RouterLink>
        </div>
      </div>

      <div class="nav-group">
        <button
            type="button"
            class="nav-link-item w-100 justify-content-between"
            @click="toggleGroup('shifts')"
            :class="{ active: openGroups.shifts }"
        >
    <span class="d-flex align-items-center gap-2">
      <i class="bi bi-calendar-week icon"></i>
      <span class="label">Lịch làm việc</span>
    </span>
          <i class="bi bi-chevron-down caret" :class="{ rotate: openGroups.shifts }"></i>
        </button>

        <div v-if="openGroups.shifts" class="sub-wrap">

          <RouterLink v-if="isAdmin" to="/shift-templates" class="sub-link" active-class="active-sub">
            <i class="bi bi-calendar2 sub-icon"></i> Ca làm việc
          </RouterLink>

          <RouterLink v-if="isAdmin" to="/shift-scheduler" class="sub-link" active-class="active-sub">
            <i class="bi bi-calendar-check sub-icon"></i> Lịch nhân viên
          </RouterLink>

          <RouterLink to="/my-schedule" class="sub-link" active-class="active-sub">
            <i class="bi bi-calendar-fill sub-icon"></i> Lịch của tôi
          </RouterLink>

          <RouterLink to="/shift-handover" class="sub-link" active-class="active-sub">
            <i class="bi bi-arrow-left-right sub-icon"></i> Doanh thu ca làm
          </RouterLink>

        </div>
      </div>

      <RouterLink
        v-if="isAdmin"
        to="/vouchers"
        class="nav-link-item"
        active-class="active"
      >
        <i class="bi bi-ticket-perforated icon"></i>
        <span class="label">Phiếu Giảm giá</span>
      </RouterLink>

      <!-- ADMIN only: Group Accounts -->
      <div v-if="isAdmin" class="nav-group">
        <button
          type="button"
          class="nav-link-item w-100 justify-content-between"
          @click="toggleGroup('accounts')"
          :class="{ active: openGroups.accounts }"
        >
          <span class="d-flex align-items-center gap-2">
            <i class="bi bi-people icon"></i>
            <span class="label">Tài khoản</span>
          </span>
          <i class="bi bi-chevron-down caret" :class="{ rotate: openGroups.accounts }"></i>
        </button>

        <div v-if="openGroups.accounts" class="sub-wrap">
          <RouterLink to="/staff" class="sub-link" active-class="active-sub">
            <i class="bi bi-person-badge sub-icon"></i> Nhân viên
          </RouterLink>
          <RouterLink to="/customers" class="sub-link" active-class="active-sub">
            <i class="bi bi-person-lines-fill sub-icon"></i> Khách hàng
          </RouterLink>
        </div>
      </div>
    </nav>
  </aside>
</template>

<script setup>
import { reactive, watch, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const role = computed(() => {
  const r = (auth.role || localStorage.getItem("role") || localStorage.getItem("vest_role") || "").toUpperCase();
  if (r) return r;
  if (auth.isAdmin) return "ADMIN";
  if (auth.isAuthenticated) return "STAFF";
  return "";
});

const isAdmin = computed(() => role.value === "ADMIN");
const isStaff = computed(() => role.value === "STAFF");

// ✅ thêm shifts
const openGroups = reactive({
  products: false,
  attributes: false,
  accounts: false,
  shifts: false,
});

function closeAllGroups() {
  Object.keys(openGroups).forEach((k) => (openGroups[k] = false));
}

// route mặc định cho ADMIN
const groupDefaultRoute = {
  products: "/products",
  attributes: "/attributes/thuong-hieu",
  accounts: "/staff",
};

async function toggleGroup(key) {
  const isOpening = !openGroups[key];

  closeAllGroups();
  openGroups[key] = isOpening;

  if (!isOpening) return;

  // ✅ Shifts: STAFF cũng được mở
  if (key === "shifts") {
    const target = isAdmin.value ? "/shift-templates" : "/my-schedule";
    if (route.path !== target) await router.push(target);
    return;
  }

  // các group khác: ADMIN only
  if (!isAdmin.value) return;

  const target = groupDefaultRoute[key];
  if (target && route.path !== target) {
    await router.push(target);
  }
}

function syncGroupsWithRoute() {
  closeAllGroups();

  const p = route.path;

  if (p.startsWith("/products") || p.startsWith("/variants")) openGroups.products = true;
  else if (p.startsWith("/attributes")) openGroups.attributes = true;
  else if (p.startsWith("/staff") || p.startsWith("/customers")) openGroups.accounts = true;
  else if (p.includes("/shift") || p.includes("/my-schedule") || p.includes("/ca-lam-viec")) openGroups.shifts = true;
}

watch(() => route.path, syncGroupsWithRoute, { immediate: true });
</script>


<style scoped>
/* ===== Sidebar ===== */
.sidebar{
  position: sticky;
  top: 0;
  height: 100vh;
  overflow-y: auto;
}

/* ===== Menu item (cha) ===== */
.nav-link-item{
  width: 100%;
  box-sizing: border-box;

  display:flex;
  align-items:center;
  gap:12px;

  height: 44px;
  padding: 0 12px;

  border-radius: 12px;
  margin: 6px 8px;

  border: 1px solid transparent;
  background: transparent;

  color:#475569;
  text-decoration:none;
  user-select:none;

  transition: background-color .18s ease, color .18s ease, border-color .18s ease, box-shadow .18s ease, transform .06s ease;
}

.nav-link-item.is-static{
  cursor: default;
  opacity: .85;
}

.nav-link-item:is(:hover, :focus-visible){
  background: rgba(41,84,184,0.08);
  color:#1f2a44;
  border-color: rgba(41,84,184,0.18);
  box-shadow: 0 8px 18px rgba(2,6,23,0.08);
  outline: 0;
}

.nav-link-item:active{
  transform: translateY(1px);
}

/* Active (cha) */
.nav-link-item.active{
  background:#2954b8;
  color:#fff;
  border-color: rgba(255,255,255,0.16);
  box-shadow: 0 10px 24px rgba(41,84,184,0.22);
}

.nav-link-item.active:is(:hover, :focus-visible){
  background:#2954b8;
  color:#fff;
  border-color: rgba(255,255,255,0.20);
  box-shadow: 0 10px 24px rgba(41,84,184,0.22);
}

/* Icon */
.icon{
  font-size: 18px;
  width: 22px;
  display:inline-flex;
  align-items:center;
  justify-content:center;
  opacity: .95;
  color: inherit;
}

.label{
  flex:1;
  font-weight: 600;
}

/* Caret */
.caret{
  font-size: 12px;
  opacity: .85;
  transition: transform .18s ease, opacity .18s ease;
}
.caret.rotate{ transform: rotate(180deg); }

/* ===== Sub menu ===== */
.sub-wrap{
  margin: 2px 8px 10px 14px;
  padding: 8px 0 6px 12px;
  border-left: 2px solid rgba(41,84,184,0.14);
}

/* Sub link (con) */
.sub-link{
  width: 100%;
  box-sizing: border-box;

  display:flex;
  align-items:center;
  gap:10px;

  height: 38px;
  padding: 0 6px;

  margin: 4px 0;
  border-radius: 12px;

  color:#64748b;
  text-decoration:none;
  font-size: 13.5px;

  border: 1px solid transparent;
  background: transparent;

  transition: background-color .18s ease, color .18s ease, border-color .18s ease;
}

.sub-link:hover{
  background: rgba(41,84,184,0.08);
  color:#1f2a44;
  border-color: rgba(41,84,184,0.16);
}

.sub-icon{
  font-size: 14px;
  width: 18px;
  display:inline-flex;
  align-items:center;
  justify-content:center;
  opacity: .9;
  color: inherit;
}

/* Active sub (con) */
.sub-link.active-sub{
  background: rgba(41,84,184,0.12);
  color:#2954b8;
  font-weight: 700;
  border-color: rgba(41,84,184,0.22);
  position: relative;
}

.sub-link.active-sub::before{
  content:"";
  position:absolute;
  left:-10px;
  top:50%;
  width:6px;
  height:6px;
  border-radius:999px;
  transform: translateY(-50%);
  background:#2954b8;
}

/* ===== Scrollbar ===== */
.sidebar::-webkit-scrollbar{ width: 10px; }
.sidebar::-webkit-scrollbar-thumb{
  background: rgba(2, 6, 23, 0.15);
  border-radius: 999px;
  border: 3px solid #fff;
}

/* Brand */
.brand-box{
  padding-top: 18px !important;   /* sửa lỗi "padding-top: px" */
  padding-bottom: 18px !important;
}

.brand-text{ line-height: 1.1; }

.brand-name{
  font-size: 25px;
  font-weight: 800;
  letter-spacing: 0.4px;
}

.brand-strong{ color:#0f172a; }

.brand-tagline{
  margin-top: 6px;
  font-size: 12px;
  color:#64748b;
  font-weight: 600;
  letter-spacing: 1.2px;
  text-transform: uppercase;
}
</style>
