<template>
  <aside class="sidebar d-flex flex-column bg-white border-end">
    <!-- Logo -->
    <div class="p-3 border-bottom text-center">
      <img
        src="../images/logo.jpg"
        alt="Logo"
        class="img-fluid"
        style="height: 120px; object-fit: contain;"
      />
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

<script setup>
import { reactive, watch } from "vue";
import { useRoute } from "vue-router";

const route = useRoute();

const openGroups = reactive({
  products: false,
  attributes: false,
  accounts: false,
});

function closeAllGroups() {
  Object.keys(openGroups).forEach((k) => (openGroups[k] = false));
}

function toggleGroup(key) {
  const isOpening = !openGroups[key];
  closeAllGroups();
  openGroups[key] = isOpening;
}

/** Mở đúng group theo route hiện tại */
function syncGroupsWithRoute() {
  closeAllGroups();

  const p = route.path;

  if (p.startsWith("/products") || p.startsWith("/variants")) {
    openGroups.products = true;
    return;
  }

  if (p.startsWith("/attributes")) {
    openGroups.attributes = true;
    return;
  }

  if (p.startsWith("/staff") || p.startsWith("/customers")) {
    openGroups.accounts = true;
    return;
  }

  // Các route khác (Trang chủ, Orders, Vouchers...) => đóng hết
}

watch(
  () => route.path,
  () => syncGroupsWithRoute(),
  { immediate: true }
);

</script>

<style scoped>
/* ===== Layout ===== */
.sidebar {
  width: 270px;
  height: 100%;
  overflow-y: auto;
}

/* ===== Common link style ===== */
.nav-link-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 12px;
  color: #475569;
  text-decoration: none;
  transition: background-color .18s ease, color .18s ease, transform .06s ease;
  margin: 4px 6px;
  font-size: 14.5px;
  line-height: 1;
  user-select: none;
  border: 1px solid transparent;
  background: transparent;
}

.nav-link-item:hover {
  background: rgba(31, 42, 68, 0.08);
  color: #1f2a44;
  border-color: rgba(31, 42, 68, 0.10);
}

.nav-link-item:active {
  transform: translateY(1px);
}

.nav-link-item.is-static {
  cursor: default;
  opacity: 0.85;
}

/* ===== Active ===== */
.nav-link-item.active {
  background: #2954b8;
  color: #fff;
  border-color: rgba(255,255,255,0.12);
}

.nav-link-item.active:hover {
  background: #182038;
  color: #fff;
}

/* ===== Icon sizing ===== */
.icon {
  font-size: 18px;
  width: 22px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  opacity: 0.95;
}

.label {
  flex: 1;
  font-weight: 600;
}

/* ===== Group caret ===== */
.caret {
  font-size: 12px;
  opacity: 0.85;
  transition: transform .18s ease;
}
.caret.rotate {
  transform: rotate(180deg);
}

/* ===== Sub menu ===== */
.sub-wrap {
  margin: 2px 6px 8px 6px;
  padding-left: 6px;
  border-left: 2px solid rgba(31, 42, 68, 0.12);
}

.sub-link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  margin: 3px 0 3px 10px;
  border-radius: 12px;
  color: #64748b;
  text-decoration: none;
  font-size: 13.5px;
  transition: background-color .18s ease, color .18s ease;
}

.sub-link:hover {
  background: rgba(31, 42, 68, 0.08);
  color: #2566fd;
}

.sub-icon {
  font-size: 14px;
  width: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  opacity: 0.9;
}

.sub-link.active-sub {
  background: rgba(31, 42, 68, 0.12);
  color: #1f2a44;
  font-weight: 700;
}

/* ===== Better scrollbar (optional) ===== */
.sidebar::-webkit-scrollbar {
  width: 10px;
}
.sidebar::-webkit-scrollbar-thumb {
  background: rgba(2, 6, 23, 0.15);
  border-radius: 999px;
  border: 3px solid #fff;
}

.nav-link-item{
  width: 100%;
  box-sizing: border-box;

  display:flex;
  align-items:center;
  gap:12px;

  height: 44px;
  padding: 0 12px;

  border-radius: 12px;
  margin: 4px 6px;

  border: 1px solid transparent;
  background: transparent;
  color:#475569;
  text-decoration:none;

  transition: background-color .18s ease, color .18s ease, border-color .18s ease, box-shadow .18s ease;
}

/* CHỈ 1 kiểu cho hover + focus */
.nav-link-item:is(:hover, :focus-visible){
  background: rgba(31, 42, 68, 0.07);
  color: #1f2a44;
  border-color: rgba(31, 42, 68, 0.12);
  box-shadow: 0 6px 16px rgba(2, 6, 23, 0.08);
  outline: 0;
}

/* Active full-width */
.nav-link-item.active{
  background:#2954b8;
  color:#fff;
  border-color: rgba(255,255,255,0.14);
  box-shadow: 0 10px 24px rgba(41, 84, 184, 0.22);
}

/* Khi active thì hover/focus vẫn giữ “active”, không bị đổi màu */
.nav-link-item.active:is(:hover, :focus-visible){
  background:#2954b8;
  color:#fff;
  border-color: rgba(255,255,255,0.18);
  box-shadow: 0 10px 24px rgba(41, 84, 184, 0.22);
}


</style>
