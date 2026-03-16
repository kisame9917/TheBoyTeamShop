<template>
  <div class="client-layout">
    <div class="site-header">
      <header class="py-3 border-bottom" style="background-color: #000f51">
        <div class="container d-flex justify-content-between align-items-center">
          <div class="logo">
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
              <button
                  class="btn btn-primary btn-search"
                  type="button"
                  aria-label="Tìm kiếm"
                  @click="doSearch"
              >
                <i class="bi bi-search"></i>
              </button>
            </div>
          </div>

          <div class="header-icons d-flex gap-3 fs-5 align-items-center">
            <div v-if="isLoggedIn" class="user-dd" ref="userWrap">
              <button
                  class="user-btn text-white"
                  type="button"
                  @click.stop="toggleUserMenu"
                  aria-label="Tài khoản"
              >
                <i class="bi bi-person"></i>
                <span class="user-name">{{ userName }}</span>
                <i class="bi bi-caret-down-fill caret"></i>
              </button>

              <div v-if="userMenuOpen" class="user-menu">
                <div class="user-menu-header">{{ userName }}</div>
                <button class="user-menu-item" type="button" @click="openProfile">
                  Hồ sơ (demo)
                </button>
                <button class="user-menu-item danger" type="button" @click="logout">
                  Đăng xuất
                </button>
              </div>
            </div>

            <router-link v-else to="/login" class="text-white" aria-label="Tài khoản">
              <i class="bi bi-person"></i>
            </router-link>

            <a href="#" class="text-white" aria-label="Yêu thích" @click.prevent>
              <i class="bi bi-heart"></i>
            </a>

            <div class="cart-wrap" ref="cartWrap">
              <button
                  class="cart-trigger text-white position-relative"
                  type="button"
                  aria-label="Giỏ hàng"
                  @click.stop="toggleCart"
              >
                <i class="bi bi-bag"></i>
                <span
                    class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-primary cart-badge"
                >
                  {{ totalQty }}
                </span>
              </button>

              <CartMiniModal
                  v-if="cartOpen"
                  class="cart-mini"
                  @close="closeCart"
                  @view-cart="goToCart"
                  @checkout="checkout"
              />
            </div>
          </div>
        </div>
      </header>

      <nav class="nav-menu py-2 bg-light-blue">
        <div class="container d-flex justify-content-center gap-4">
          <router-link to="/shop" class="nav-link">Cửa hàng</router-link>
<!--          <router-link :to="{ name: 'Search', query: { cat: 'bo-vest-nam' } }" class="nav-link">-->
<!--            Bộ vest nam-->
<!--          </router-link>-->
<!--          <router-link :to="{ name: 'Search', query: { cat: 'trang-phuc' } }" class="nav-link">-->
<!--            Trang phục-->
<!--          </router-link>-->
          <router-link :to="{ name: 'Search', query: { cat: 'vest-nam' } }" class="nav-link">
            Vest nam
          </router-link>
          <router-link :to="{ name: 'Search', query: { cat: 'giam-gia' } }" class="nav-link">
            Giảm giá
          </router-link>
         <router-link :to="{ name: 'Contact' }" class="nav-link">
  Liên hệ
</router-link>
          <router-link :to="{ name: 'Search', query: { cat: 'tra-cuu-don-hang' } }" class="nav-link">
            TRA CỨU ĐƠN HÀNG
          </router-link>
        </div>
      </nav>
    </div>

    <main>
      <router-view />
    </main>

    <footer class="footer-bg pt-5 pb-3" style="background-color: #000f51">
      <div class="container">
        <div class="row">
          <div class="col-lg-3 col-md-6 mb-4 text-center text-lg-start">
            <img :src="logoUrl" alt="Logo" class="img-fluid" style="max-height: 100px" />
          </div>

          <div class="col-lg-2 col-md-3 col-6 mb-4 text-white">
            <h6 class="fw-bold mb-3 text-white">Hỗ trợ</h6>
            <ul class="list-unstyled">
              <li><a href="#" class="footer-link" @click.prevent>Hướng dẫn mua hàng</a></li>
              <li><a href="#" class="footer-link" @click.prevent>Chính sách đổi trả</a></li>
              <li><a href="#" class="footer-link" @click.prevent>Chính sách bảo hành</a></li>
            </ul>
          </div>

          <div class="col-lg-2 col-md-3 col-6 mb-4 text-white">
            <h6 class="fw-bold mb-3 text-white">Danh mục</h6>
            <ul class="list-unstyled">
              <li><router-link to="/shop" class="footer-link">Cửa hàng</router-link></li>
              <li>
                <router-link :to="{ name: 'Search', query: { cat: 'vest-nam' } }" class="footer-link">
                  Vest nam
                </router-link>
              </li>
              <li>
                <router-link :to="{ name: 'Search', query: { cat: 'giam-gia' } }" class="footer-link">
                  Giảm giá
                </router-link>
              </li>
            </ul>
          </div>

          <div class="col-lg-3 col-md-6 mb-4 text-white">
            <h6 class="fw-bold mb-3 text-white">Liên hệ</h6>
            <p class="mb-2">Hotline: 0123 456 789</p>
            <p class="mb-2">Email: support@vest.vn</p>
            <p class="mb-0">Địa chỉ: Hà Nội, Việt Nam</p>
          </div>

          <div class="col-lg-2 col-md-6 mb-4 text-white">
            <h6 class="fw-bold mb-3 text-white">Kết nối</h6>
            <ul class="list-unstyled d-flex gap-3 align-items-center mb-4">
              <li>
                <a href="#" class="footer-link footer-icon" aria-label="Facebook" @click.prevent>
                  <i class="bi bi-facebook"></i>
                </a>
              </li>
              <li>
                <a href="#" class="footer-link footer-icon" aria-label="Instagram" @click.prevent>
                  <i class="bi bi-instagram"></i>
                </a>
              </li>
              <li>
                <a href="#" class="footer-link footer-icon" aria-label="YouTube" @click.prevent>
                  <i class="bi bi-youtube"></i>
                </a>
              </li>
              <li>
                <a href="#" class="footer-link footer-icon" aria-label="TikTok" @click.prevent>
                  <i class="bi bi-tiktok"></i>
                </a>
              </li>
            </ul>

            <h6 class="fw-bold mb-3 text-white">Phương thức thanh toán</h6>
            <ul class="list-unstyled d-flex flex-wrap gap-2 mb-0">
              <li><span class="payment-badge" title="Thanh toán khi nhận hàng">COD</span></li>
              <li><span class="payment-badge" title="Thẻ nội địa / Internet Banking">ATM</span></li>
              <li><span class="payment-badge" title="Visa">VISA</span></li>
<!--              <li><span class="payment-badge" title="JCB">JCB</span></li>-->
            </ul>
          </div>
        </div>

        <div class="footer-bottom pt-3 border-top text-white">
          <small>© 2026 VestShop. The Boy Team.</small>
        </div>
      </div>
    </footer>

    <CartAddedToast
        :open="cartToast.open"
        :item-name="cartToast.itemName"
        :image="cartToast.image"
        :qty="cartToast.qty"
        @view-cart="goToCart"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, onBeforeUnmount, ref } from 'vue';
import { useRouter } from 'vue-router';
import CartMiniModal from '../components/cart/CartMiniModal.vue';
import CartAddedToast from '../components/common/CartAddedToast.vue';
import { useCart } from '../composables/useCart';
import { getSiteLogoUrl, resolveMediaUrl } from '../utils/media';
import { CART_ADDED_EVENT } from '../services/cartService';

const router = useRouter();

const cartOpen = ref(false);
const cartWrap = ref(null);
const { totalQty } = useCart();

function toggleCart() {
  cartOpen.value = !cartOpen.value;
  userMenuOpen.value = false;
}

function closeCart() {
  cartOpen.value = false;
}

function goToCart() {
  closeCart();
  cartToast.value.open = false;
  router.push({ name: 'Cart' });
}

function checkout() {
  closeCart();
  router.push({ name: 'Checkout' });
}

const logoUrl = computed(() => getSiteLogoUrl());

const keyword = ref('');

function doSearch() {
  const q = keyword.value.trim();
  router.push({ name: 'Search', query: q ? { q } : {} });
}

const userMenuOpen = ref(false);
const userWrap = ref(null);
const isLoggedIn = ref(false);
const userName = ref('Khách hàng');

function syncAuth() {
  const token =
      localStorage.getItem('USER_ACCESS_TOKEN') ||
      sessionStorage.getItem('USER_ACCESS_TOKEN');

  isLoggedIn.value = !!token;

  userName.value =
      localStorage.getItem('USER_NAME') ||
      sessionStorage.getItem('USER_NAME') ||
      'Khách hàng';
}

function toggleUserMenu() {
  userMenuOpen.value = !userMenuOpen.value;
  cartOpen.value = false;
}

function openProfile() {
  userMenuOpen.value = false;
  alert('Hồ sơ (demo) - sau bạn làm trang profile/me nhé.');
}

function logout() {
  userMenuOpen.value = false;

  localStorage.removeItem('USER_ACCESS_TOKEN');
  sessionStorage.removeItem('USER_ACCESS_TOKEN');
  localStorage.removeItem('USER_NAME');
  sessionStorage.removeItem('USER_NAME');

  syncAuth();
  router.push('/login');
}

function handleClickOutside(e) {
  if (userWrap.value && !userWrap.value.contains(e.target)) {
    userMenuOpen.value = false;
  }

  if (cartWrap.value && !cartWrap.value.contains(e.target)) {
    cartOpen.value = false;
  }
}

function handleStorageChange() {
  syncAuth();
}

const cartToast = ref({
  open: false,
  itemName: '',
  image: '',
  qty: 1,
});
let cartToastTimer = null;

function handleCartAdded(event) {
  const detail = event?.detail || {};
  const item = detail.item || {};

  cartToast.value = {
    open: true,
    itemName: item.name || item.tenSanPham || 'Sản phẩm',
    image: resolveMediaUrl(item.image || item.imageUrl || item.anh || item.hinhAnh || ''),
    qty: Math.max(1, Number(detail.qtyAdded || 1) || 1),
  };

  if (cartToastTimer) {
    clearTimeout(cartToastTimer);
  }

  cartToastTimer = setTimeout(() => {
    cartToast.value.open = false;
  }, 2600);
}

onMounted(() => {
  syncAuth();
  document.addEventListener('click', handleClickOutside);
  window.addEventListener('storage', handleStorageChange);
  window.addEventListener(CART_ADDED_EVENT, handleCartAdded);
});

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside);
  window.removeEventListener('storage', handleStorageChange);
  window.removeEventListener(CART_ADDED_EVENT, handleCartAdded);
  if (cartToastTimer) {
    clearTimeout(cartToastTimer);
    cartToastTimer = null;
  }
});
</script>

<style scoped>
.client-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.site-header {
  position: sticky;
  top: 0;
  z-index: 1200;
}

.logo-img {
  max-height: 54px;
  max-width: 180px;
  object-fit: contain;
}

.search-bar .form-control {
  height: 40px;
  font-size: 14px;
}

.btn-search {
  min-width: 46px;
}

.header-icons a,
.header-icons button {
  text-decoration: none;
}

.header-icons i {
  font-size: 20px;
}

.cart-badge {
  font-size: 10px;
  min-width: 18px;
  height: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.cart-wrap {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.cart-trigger {
  border: 0;
  background: transparent;
  padding: 0;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.cart-mini {
  position: absolute;
  top: calc(100% + 14px);
  right: -14px;
  z-index: 1100;
}

.user-dd {
  position: relative;
}

.user-btn {
  border: 0;
  background: transparent;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
}

.caret {
  font-size: 12px;
}

.user-menu {
  position: absolute;
  top: calc(100% + 12px);
  right: 0;
  width: 220px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.16);
  border: 1px solid rgba(0, 0, 0, 0.08);
  overflow: hidden;
  z-index: 1100;
}

.user-menu-header {
  padding: 12px 14px;
  font-size: 14px;
  font-weight: 700;
  background: #f8f9fa;
  color: #111;
}

.user-menu-item {
  width: 100%;
  border: 0;
  background: #fff;
  text-align: left;
  padding: 12px 14px;
  font-size: 14px;
  color: #111;
}

.user-menu-item:hover {
  background: #f4f6f8;
}

.user-menu-item.danger {
  color: #c1121f;
}

.nav-menu {
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.bg-light-blue {
  background: #ffffff;
}

.nav-link {
  color: #1f2937;
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
  text-transform: uppercase;
}

.nav-link:hover,
.nav-link.router-link-active {
  color: #0d6efd;
}

main {
  flex: 1;
}

.footer-link {
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
}

.footer-link:hover {
  color: #fff;
}

.footer-icon {
  font-size: 20px;
}

.payment-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 52px;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  border: 1px solid rgba(255, 255, 255, 0.18);
}

@media (max-width: 991.98px) {
  .search-bar {
    display: none;
  }

  .nav-menu .container {
    gap: 16px !important;
    flex-wrap: wrap;
  }

  .cart-mini {
    right: -8px;
  }
}

@media (max-width: 767.98px) {
  .logo-img {
    max-width: 132px;
  }

  .cart-mini {
    right: -8px;
  }
}
</style>
