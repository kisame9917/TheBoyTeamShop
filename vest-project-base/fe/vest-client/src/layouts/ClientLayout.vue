<template>
  <div class="client-layout">
    <!-- Header -->
    <header class="bg-white py-3 border-bottom">
      <div class="container d-flex justify-content-between align-items-center">
        <div class="logo">
          <!-- Logo -> Home -->
          <router-link to="/" aria-label="Trang chủ">
            <img :src="logoUrl" alt="Logo" class="logo-img" />
          </router-link>
        </div>

        <div class="search-bar w-50">
          <div class="input-group">
            <input
                v-model="keyword"
                @keyup.enter="doSearch"
                type="text"
                class="form-control bg-light border-0"
                placeholder="Tìm kiếm vest nam..."
                aria-label="Tìm kiếm"
            />
            <button class="btn btn-primary btn-search" type="button" aria-label="Tìm kiếm" @click="doSearch">
              <i class="bi bi-search"></i>
            </button>
          </div>
        </div>

        <div class="header-icons d-flex gap-3 fs-5">
          <!-- Person -> Login -->
          <router-link to="/login" class="text-dark" aria-label="Tài khoản">
            <i class="bi bi-person"></i>
          </router-link>

          <!-- Heart: chưa có page -> tạm để # (bạn có route thì đổi sau) -->
          <a href="#" class="text-dark" aria-label="Yêu thích" @click.prevent>
            <i class="bi bi-heart"></i>
          </a>

          <!-- Cart: chưa có page -> tạm để # -->
          <a href="#" class="text-dark position-relative" aria-label="Giỏ hàng" @click.prevent>
            <i class="bi bi-bag"></i>
            <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-primary cart-badge">
              0
            </span>
          </a>
        </div>
      </div>
    </header>

    <!-- Menu -->
    <nav class="nav-menu py-2 bg-light-blue">
      <div class="container d-flex justify-content-center gap-4">
        <router-link to="/shop" class="nav-link">Cửa hàng</router-link>

        <!-- các mục chưa có page riêng -> chuyển sang Search theo cat để demo -->
        <router-link :to="{ name: 'Search', query: { cat: 'bo-vest-nam' } }" class="nav-link">Bộ vest nam</router-link>
        <router-link :to="{ name: 'Search', query: { cat: 'trang-phuc' } }" class="nav-link">Trang phục</router-link>
        <router-link :to="{ name: 'Search', query: { cat: 'vest-nam' } }" class="nav-link">Vest nam</router-link>
        <router-link :to="{ name: 'Search', query: { cat: 'doc-quyen-online' } }" class="nav-link">Độc quyền online</router-link>
        <router-link :to="{ name: 'Search', query: { cat: 'slim-fit' } }" class="nav-link">Slim fit</router-link>
        <router-link :to="{ name: 'Search', query: { cat: 'lien-he' } }" class="nav-link">Liên hệ</router-link>
        <router-link :to="{ name: 'Search', query: { cat: 'ankasa' } }" class="nav-link">Ankasa</router-link>
      </div>
    </nav>

    <!-- Nội dung trang -->
    <main>
      <router-view />
    </main>

    <!-- Footer -->
    <footer class="footer-bg pt-5 pb-3">
      <div class="container">
        <div class="row">
          <div class="col-lg-3 col-md-6 mb-4 text-center text-lg-start">
            <img :src="logoUrl" alt="Logo" class="img-fluid" style="max-height: 100px" />
          </div>

          <div class="col-lg-2 col-md-3 col-6 mb-4 text-start">
            <h6 class="fw-bold mb-3">Hỗ trợ</h6>
            <ul class="list-unstyled">
              <li><a href="#" class="footer-link" @click.prevent>Hướng dẫn mua hàng</a></li>
              <li><a href="#" class="footer-link" @click.prevent>Chính sách đổi trả</a></li>
              <li><a href="#" class="footer-link" @click.prevent>Chính sách bảo hành</a></li>
            </ul>
          </div>

          <div class="col-lg-2 col-md-3 col-6 mb-4 text-start">
            <h6 class="fw-bold mb-3">Danh mục</h6>
            <ul class="list-unstyled">
              <li><router-link to="/shop" class="footer-link">Cửa hàng</router-link></li>
              <li><router-link :to="{ name: 'Search', query: { cat: 'vest-nam' } }" class="footer-link">Vest nam</router-link></li>
              <li><router-link :to="{ name: 'Search', query: { cat: 'slim-fit' } }" class="footer-link">Slim fit</router-link></li>
            </ul>
          </div>

          <div class="col-lg-5 col-md-12 mb-4 text-start">
            <h6 class="fw-bold mb-3">Liên hệ</h6>
            <p class="mb-2">Hotline: 0123 456 789</p>
            <p class="mb-2">Email: support@vest.vn</p>
            <p class="mb-0">Địa chỉ: Hà Nội, Việt Nam</p>
          </div>
        </div>

        <div class="text-center pt-3 border-top">
          <small>© 2026 Vest. All rights reserved.</small>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// Logo lấy từ backend/public folder (VD: http://localhost:8080/uploads/logo.jpg)
const logoUrl = `${import.meta.env.VITE_API_BASE_URL || ''}/uploads/logo.jpg`

const keyword = ref('')

function doSearch() {
  const q = keyword.value.trim()
  router.push({ name: 'Search', query: q ? { q } : {} })
}
</script>

<style>
/* Biến màu dùng chung toàn site */
:root {
  --sky-aqua: #07c8f9;
  --fresh-sky: #09a6f3;
  --brilliant-azure: #0a85ed;
  --royal-blue: #0c63e7;
  --bright-indigo: #0d41e1;
  --pale-blue-bg: #e8f6fa;
}

/* Nới rộng khung hiển thị (Bootstrap .container) */
.container {
  max-width: 1600px;
}
@media (min-width: 1800px) {
  .container {
    max-width: 1720px;
  }
}

/* Base */
.client-layout {
  font-family: "Helvetica Neue", Arial, sans-serif;
}
.text-royal-blue {
  color: var(--royal-blue);
}
.bg-light-blue {
  background-color: var(--pale-blue-bg);
}
.btn-primary {
  background-color: var(--brilliant-azure);
  border-color: var(--brilliant-azure);
}
.btn-primary:hover {
  background-color: var(--royal-blue);
}

/* Header */
.logo-img {
  height: 72px; /* tăng size logo */
  width: auto;
  object-fit: contain;
}
@media (max-width: 576px) {
  .logo-img {
    height: 56px;
  }
}
.btn-search {
  background-color: var(--brilliant-azure);
  border-radius: 0 4px 4px 0;
}
.form-control:focus {
  box-shadow: none;
}
.cart-badge {
  font-size: 0.6rem;
  background-color: var(--royal-blue) !important;
}

/* Nav Menu */
.nav-link {
  color: #333;
  font-weight: 600;
  text-transform: uppercase;
  font-size: 0.9rem;
  text-decoration: none;
}
.nav-link:hover {
  color: var(--royal-blue);
}

/* Footer */
.footer-bg {
  background: #f8f9fa;
}
.footer-link {
  color: #333;
  text-decoration: none;
  display: inline-block;
  margin-bottom: 6px;
}
.footer-link:hover {
  color: var(--royal-blue);
}
</style>