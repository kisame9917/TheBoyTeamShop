<template>
  <aside class="sidebar d-flex flex-column bg-white border-end">
    <!-- Logo -->
    <div class="brand-box p-3 border-bottom text-center">
  <img
    src="../images/logo.jpg"
    alt="TheBoyTeam Logo"
    class="brand-logo img-fluid"
  />

  <div class="brand-text mt-2">
    <div class="brand-name">
      <span class="brand-strong">TheBoyTeam</span>
    </div>
  </div>
</div>


    <!-- Nav -->
    <nav class="p-2 flex-grow-1">
      <!-- Trang chủ -->
      <RouterLink
        to="/"
        class="nav-link-item"
        active-class="active"
        exact-active-class="active"
      >
        <i class="bi bi-house-door icon"></i>
        <span class="label">Trang Chủ</span>
      </RouterLink>

      <!-- Placeholder -->
      <div class="nav-link-item is-static">
        <i class="bi bi-bar-chart icon"></i>
        <span class="label">Thống kê</span>
      </div>

      <div class="nav-link-item is-static">
        <i class="bi bi-cart3 icon"></i>
        <span class="label">Bán Hàng</span>
      </div>

      <!-- Orders -->
      <RouterLink to="/orders" class="nav-link-item" active-class="active">
        <i class="bi bi-receipt-cutoff icon"></i>
        <span class="label">Quản lý hóa đơn</span>
      </RouterLink>

      <!-- Group: Products -->
      <div class="nav-group">
        <button
          type="button"
          class="nav-link-item w-100 justify-content-between"
          @click="toggleGroup('products')"
          :class="{ active: openGroups.products }"
        >
          <span class="d-flex align-items-center gap-2">
            <i class="bi bi-box-seam icon"></i>
            <span class="label">Quản lý sản phẩm</span>
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

      <!-- Group: Attributes -->
      <div class="nav-group">
        <button
          type="button"
          class="nav-link-item w-100 justify-content-between"
          @click="toggleGroup('attributes')"
          :class="{ active: openGroups.attributes }"
        >
          <span class="d-flex align-items-center gap-2">
            <i class="bi bi-ui-checks-grid icon"></i>
            <span class="label">Danh sách thuộc tính</span>
          </span>
          <i class="bi bi-chevron-down caret" :class="{ rotate: openGroups.attributes }"></i>
        </button>

        <div v-if="openGroups.attributes" class="sub-wrap">
          <RouterLink to="/attributes/thuong-hieu" class="sub-link" active-class="active-sub">
            <i class="bi bi-award sub-icon"></i> Thương hiệu
          </RouterLink>
          <RouterLink to="/attributes/chat-lieu" class="sub-link" active-class="active-sub">
            <i class="bi bi-layers sub-icon"></i> Chất liệu
          </RouterLink>
          <RouterLink to="/attributes/kich-co" class="sub-link" active-class="active-sub">
            <i class="bi bi-rulers sub-icon"></i> Kích cỡ
          </RouterLink>
          <RouterLink to="/attributes/mau-sac" class="sub-link" active-class="active-sub">
            <i class="bi bi-palette sub-icon"></i> Màu sắc
          </RouterLink>
          <RouterLink to="/attributes/loai-san-pham" class="sub-link" active-class="active-sub">
            <i class="bi bi-tags sub-icon"></i> Loại sản phẩm
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
        </div>
      </div>

      <!-- Vouchers -->
      <RouterLink to="/vouchers" class="nav-link-item" active-class="active">
        <i class="bi bi-ticket-perforated icon"></i>
        <span class="label">Quản lý giảm giá</span>
      </RouterLink>

      <!-- Group: Accounts -->
      <div class="nav-group">
        <button
          type="button"
          class="nav-link-item w-100 justify-content-between"
          @click="toggleGroup('accounts')"
          :class="{ active: openGroups.accounts }"
        >
          <span class="d-flex align-items-center gap-2">
            <i class="bi bi-people icon"></i>
            <span class="label">Quản lý tài khoản</span>
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

<script setup>import { reactive, watch } from "vue";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();

const openGroups = reactive({
  products: false,
  attributes: false,
  accounts: false,
});

function closeAllGroups() {
  Object.keys(openGroups).forEach((k) => (openGroups[k] = false));
}

// route mặc định khi bấm vào group
const groupDefaultRoute = {
  products: "/products",
  attributes: "/attributes/thuong-hieu",
  accounts: "/staff",
};

async function toggleGroup(key) {
  const isOpening = !openGroups[key];

  closeAllGroups();
  openGroups[key] = isOpening;

  // QUAN TRỌNG: nếu đang mở group => chuyển route sang group đó
  if (isOpening) {
    const target = groupDefaultRoute[key];
    if (target && route.path !== target) {
      await router.push(target);
    }
  }
}

/** Mở đúng group theo route hiện tại */
function syncGroupsWithRoute() {
  closeAllGroups();
  const p = route.path;

  if (p.startsWith("/products") || p.startsWith("/variants")) openGroups.products = true;
  else if (p.startsWith("/attributes")) openGroups.attributes = true;
  else if (p.startsWith("/staff") || p.startsWith("/customers")) openGroups.accounts = true;
}

watch(() => route.path, syncGroupsWithRoute, { immediate: true });

</script>
<style scoped>
/* ===== Sidebar ===== */
.sidebar{
  position: sticky;
  top: 0;                 /* dính lên trên */
  height: 100vh;          /* cao full màn hình */
  overflow-y: auto;       /* menu dài thì sidebar tự cuộn */
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

/* Hover/focus (cha) */
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
  padding: 0 12px;

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
  background: rgba(41,84,184,0.08);          /* ✅ đồng bộ hover */
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
  background: rgba(41,84,184,0.12);          /* ✅ active nhạt */
  color:#2954b8;                             /* ✅ chữ xanh đậm cùng tông */
  font-weight: 700;
  border-color: rgba(41,84,184,0.22);
  position: relative;
}

/* indicator cho sub active */
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
.sidebar{
  position: sticky;
  top: 0;                 /* dính lên trên */
  height: 100vh;          /* cao full màn hình */
  overflow-y: auto;       /* menu dài thì sidebar tự cuộn */
}
.brand-box{
  padding-top: px !important;
  padding-bottom: 18px !important;
}


.brand-text{
  line-height: 1.1;
}

.brand-name{
  font-size: 25px;
  font-weight: 800;
  letter-spacing: 0.4px;
}

.brand-strong{
  color:#0f172a;          /* đen-xanh sang */
}

.brand-tagline{
  margin-top: 6px;
  font-size: 12px;
  color:#64748b;
  font-weight: 600;
  letter-spacing: 1.2px;
  text-transform: uppercase;
}

</style>
