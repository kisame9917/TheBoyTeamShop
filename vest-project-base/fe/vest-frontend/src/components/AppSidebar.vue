<template>
  <aside class="sidebar d-flex flex-column bg-white border-end">
    <!-- Logo -->
<div class="p-3 border-bottom text-center logo-wrap">
  <div class="brand-card mx-auto">
    <div class="brand-logo">
      <img src="../images/logo.jpg" alt="Logo" />
    </div>

    <div class="brand-text">
      <div class="brand-name">TheBoyTeam</div>
      
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

<script setup>
import { reactive } from "vue";

const openGroups = reactive({
  products: true,
  attributes: false,
  accounts: false,
});

function toggleGroup(key) {
  openGroups[key] = !openGroups[key];
}
</script>
<style scoped>
/* ===== Layout ===== */
.sidebar{
  width: 270px;
  height: 100%;
  overflow-y: auto;
  background: #fff;
}

/* ===== Common link style ===== */
.nav-link-item{
  display:flex;
  align-items:center;
  gap:12px;
  padding:10px 12px;
  border-radius:14px;
  color:#475569;
  text-decoration:none;
  margin:6px 8px;
  font-size:14.5px;
  line-height:1;
  user-select:none;

  background: rgba(255,255,255,0.8);
  border: 1px solid rgba(2,6,23,0.06);

  transition:
    background-color .18s ease,
    border-color .18s ease,
    box-shadow .18s ease,
    transform .06s ease,
    color .18s ease;
}

.nav-link-item:hover{
  background: rgba(31,42,68,0.06);
  color:#1f2a44;
  border-color: rgba(31,42,68,0.14);
  box-shadow: 0 6px 18px rgba(2,6,23,0.06);
}

.nav-link-item:active{
  transform: translateY(1px);
}

.nav-link-item.is-static{
  cursor: default;
  opacity: .85;
}

/* ===== Active (nhạt hơn) ===== */
.nav-link-item.active{
  background: rgba(31,42,68,0.10);
  color:#1f2a44;
  border-color: rgba(31,42,68,0.22);
  box-shadow: inset 0 0 0 1px rgba(31,42,68,0.10);
}

.nav-link-item.active:hover{
  background: rgba(31,42,68,0.12);
  border-color: rgba(31,42,68,0.26);
}

/* ===== Icon sizing ===== */
.icon{
  font-size:18px;
  width:22px;
  display:inline-flex;
  align-items:center;
  justify-content:center;
  opacity:.95;
  color: inherit;
}

.label{
  flex:1;
  font-weight:650;
}

/* ===== Group caret ===== */
.caret{
  font-size:12px;
  opacity:.70;
  transition: transform .18s ease, opacity .18s ease;
}
.nav-link-item:hover .caret{ opacity: .95; }
.caret.rotate{ transform: rotate(180deg); }

/* ===== Sub menu ===== */
.nav-group{ margin-top: 2px; }

.sub-wrap{
  margin: 2px 12px 10px 18px;
  padding: 8px 0 6px 10px;
  border-left: 2px solid rgba(31,42,68,0.10);
  position: relative;
}

.sub-link{
  display:flex;
  align-items:center;
  gap:10px;
  padding:8px 12px;
  margin:4px 0;
  border-radius:12px;
  color:#64748b;
  text-decoration:none;
  font-size:13.5px;

  border: 1px solid transparent;
  background: transparent;

  transition:
    background-color .18s ease,
    border-color .18s ease,
    box-shadow .18s ease,
    color .18s ease;
}

.sub-link:hover{
  background: rgba(31,42,68,0.06);
  color:#1f2a44;
  border-color: rgba(31,42,68,0.12);
  box-shadow: 0 6px 16px rgba(2,6,23,0.05);
}

.sub-icon{
  font-size:14px;
  width:18px;
  display:inline-flex;
  align-items:center;
  justify-content:center;
  opacity:.9;
}

/* Active sub nhấn nhẹ + có chấm indicator */
.sub-link.active-sub{
  background: rgba(31,42,68,0.10);
  color:#1f2a44;
  font-weight:750;
  border-color: rgba(31,42,68,0.18);
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
  background: rgba(31,42,68,0.55);
}

/* ===== Scrollbar (optional) ===== */
.sidebar::-webkit-scrollbar{ width:10px; }
.sidebar::-webkit-scrollbar-thumb{
  background: rgba(2,6,23,0.14);
  border-radius:999px;
  border:3px solid #fff;
}.logo-wrap{
  background: linear-gradient(180deg, rgba(31,42,68,0.06), rgba(255,255,255,0));
}

.brand-card{
  width: 100%;
  max-width: 210px;
  padding: 14px 12px;
  border-radius: 16px;
  border: 1px solid rgba(2,6,23,0.06);
  background: rgba(255,255,255,0.85);
  box-shadow: 0 10px 26px rgba(2,6,23,0.06);
}

.brand-logo{
  width: 96px;
  height: 96px;
  border-radius: 18px;
  overflow: hidden;
  margin: 0 auto 10px;
  background: #fff;
  border: 1px solid rgba(2,6,23,0.08);
  box-shadow: inset 0 0 0 1px rgba(255,255,255,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-logo img{
  width: 100%;
  height: 100%;
  object-fit: cover; /* nếu muốn giữ nguyên không crop: đổi thành contain */
}

.brand-text{
  line-height: 1.1;
}

.brand-name{
  font-weight: 800;
  font-size: 18px;
  color: #1f2a44;
  letter-spacing: 0.2px;
}

.brand-sub{
  margin-top: 6px;
  font-size: 12px;
  color: #64748b;
}
.sidebar{
  width: 270px;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-right: 1px solid rgba(2,6,23,0.08);
  overflow: hidden; /* ✅ không cho aside clip shadow của logo */
}

/* ✅ CHỈ MENU MỚI SCROLL */
.sidebar-nav{
  flex: 1;
  overflow-y: auto;
  overflow-x: visible; /* để shadow/outline không bị cắt ngang */
  padding: 8px;
}
.logo-wrap{
  padding: 18px 16px; /* ✅ tăng padding để bóng có chỗ */
  overflow: visible;  /* ✅ chắc chắn */
}
.brand-card{
  box-shadow: 0 14px 30px rgba(2,6,23,0.10);
}

</style>
