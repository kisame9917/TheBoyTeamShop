<template>
  <aside class="sidebar">
    <div class="logo-section">
      <div class="tbt-logo">
        <img src="../images/logo.jpg" alt="Logo" style="height: 120px; object-fit: contain;" />
      </div>
    </div>

    <nav class="sidebar-nav">
      <!-- Direct Links -->
      <RouterLink to="/" class="nav-item" active-class="ignore-active" exact-active-class="router-link-active">
        <span class="icon">🏠</span> Trang Chủ
      </RouterLink>

      <div class="nav-item"> <!-- Placeholder for Thống kê -->
        <span class="icon">📊</span> Thống kê
      </div>

      <div class="nav-item"> <!-- Placeholder for Bán Hàng -->
        <span class="icon">🛒</span> Bán Hàng
      </div>

      <RouterLink to="/orders" class="nav-item">
        <span class="icon">📄</span> Quản lý hóa đơn
      </RouterLink>

      <!-- Collapsible: Product Management -->
      <div class="nav-group">
        <div class="nav-item has-arrow" @click="toggleGroup('products')" :class="{ active: openGroups.products }">
          <span class="icon">❣</span> Quản lý sản phẩm
          <span class="arrow">▼</span>
        </div>
        <div class="nav-sub" v-if="openGroups.products">
          <RouterLink to="/products" class="sub-item" active-class="active-blue">Danh sách sản phẩm</RouterLink>
          <RouterLink to="/variants" class="sub-item" active-class="active-blue">Danh sách biến thể</RouterLink>
        </div>
      </div>

      <!-- Collapsible: Attribute List -->
      <div class="nav-group">
        <div class="nav-item has-arrow" @click="toggleGroup('attributes')" :class="{ active: openGroups.attributes }">
          <span class="icon">list</span> Danh sách thuộc tính
          <span class="arrow">▼</span>
        </div>
        <div class="nav-sub" v-if="openGroups.attributes">
          <RouterLink to="/attributes/thuong-hieu" class="sub-item" active-class="active-blue">Thương hiệu</RouterLink>
          <RouterLink to="/attributes/chat-lieu" class="sub-item" active-class="active-blue">Chất liệu</RouterLink>
          <RouterLink to="/attributes/kich-co" class="sub-item" active-class="active-blue">Kích cỡ</RouterLink>
          <RouterLink to="/attributes/mau-sac" class="sub-item" active-class="active-blue">Màu sắc</RouterLink>
          <RouterLink to="/attributes/loai-san-pham" class="sub-item" active-class="active-blue">Loại sản phẩm
          </RouterLink>
          <RouterLink to="/attributes/so-khuy" class="sub-item" active-class="active-blue">Số khuy</RouterLink>
          <RouterLink to="/attributes/kieu-tui" class="sub-item" active-class="active-blue">Kiểu túi</RouterLink>
          <RouterLink to="/attributes/ve-ao" class="sub-item" active-class="active-blue">Ve áo</RouterLink>
          <RouterLink to="/attributes/xe-ta" class="sub-item" active-class="active-blue">Xẻ tà</RouterLink>
          <RouterLink to="/attributes/xuat-xu" class="sub-item" active-class="active-blue">Xuất xứ</RouterLink>
          <RouterLink to="/attributes/fit" class="sub-item" active-class="active-blue">Fit</RouterLink>
        </div>
      </div>

      <RouterLink to="/vouchers" class="nav-item">
        <span class="icon">🏷️</span> Quản lý giảm giá
      </RouterLink>

      <!-- Collapsible: Account Management -->
      <div class="nav-group">
        <div class="nav-item has-arrow" @click="toggleGroup('accounts')" :class="{ active: openGroups.accounts }">
          <span class="icon">👤</span> Quản lý tài khoản
          <span class="arrow">▼</span>
        </div>
        <div class="nav-sub" v-if="openGroups.accounts">
          <RouterLink to="/staff" class="sub-item">Nhân viên</RouterLink>
          <RouterLink to="/customers" class="sub-item">Khách hàng</RouterLink>
        </div>
      </div>

    </nav>

    <!-- Logout -->
    <!-- Keeping standard logout button at bottom or integrate into Account -->
  </aside>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

const openGroups = reactive({
  products: true, // Default open for demo
  attributes: false,
  accounts: false
})

function toggleGroup(key) {
  openGroups[key] = !openGroups[key]
}
</script>

<style scoped>
.sidebar {
  background: white;
  /* Changed from dark to white based on screenshot */
  color: #333;
  width: 260px;
  border-right: 1px solid #eee;
  display: flex;
  flex-direction: column;
  height: 100%;
  /* Fill parent */
  overflow-y: auto;
}

.logo-section {
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-bottom: 1px solid #f0f0f0;
}

.tbt-logo {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #1e3a8a;
  /* Dark blue logo color */
}

.sidebar-nav {
  padding: 10px;
  display: flex;
  flex-direction: column;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  cursor: pointer;
  color: #4b5563;
  font-size: 15px;
  text-decoration: none;
  transition: all 0.2s;
  border-radius: 8px;
  margin-bottom: 4px;
}

.nav-item:hover {
  background-color: rgba(30, 58, 138, 0.1);
  color: #1e3a8a;
}

.nav-item .icon {
  margin-right: 12px;
  width: 20px;
  text-align: center;
  font-size: 18px;
  /* Using material icons font if available, or just emojis for now */
}

.nav-item.has-arrow {
  justify-content: space-between;
}

.arrow {
  font-size: 10px;
  transition: transform 0.2s;
}

.nav-item.active .arrow {
  transform: rotate(180deg);
}

.nav-sub {
  background-color: transparent;
  margin-bottom: 4px;
}

.sub-item {
  display: block;
  padding: 8px 16px 8px 48px;
  /* Indent sub items */
  color: #6b7280;
  text-decoration: none;
  font-size: 14px;
  cursor: pointer;
  border-radius: 8px;
  margin-bottom: 2px;
}

.sub-item:hover {
  color: #1e3a8a;
  background-color: rgba(30, 58, 138, 0.1);
  transition: all 0.2s;
}

/* Active State for Routing */
/* Active State for Routing - applied automatically by vue-router */
.nav-item.router-link-active,
.sub-item.router-link-active,
.active-blue {
  background-color: #1e3a8a !important;
  color: white !important;
  font-weight: 500;
}

.nav-item.router-link-active .icon,
.nav-item.router-link-active .arrow {
  color: white !important;
}

.nav-item.router-link-active:hover,
.sub-item.router-link-active:hover {
  background-color: #1e40af !important;
  color: white !important;
}
</style>
