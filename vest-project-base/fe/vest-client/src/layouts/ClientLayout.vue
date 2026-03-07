<template>
  <div class="client-layout">
    <!-- Header (fixed) -->
    <div class="site-header">
      <header class="py-3 border-bottom" style="background-color: #000f51">
        <div
          class="container d-flex justify-content-between align-items-center"
        >
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
            <!-- USER: nếu chưa login thì link /login, nếu đã login thì dropdown -->
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
                <button
                  class="user-menu-item"
                  type="button"
                  @click="openProfile"
                >
                  Hồ sơ (demo)
                </button>
                <button
                  class="user-menu-item danger"
                  type="button"
                  @click="logout"
                >
                  Đăng xuất
                </button>
              </div>
            </div>

            <router-link
              v-else
              to="/login"
              class="text-white"
              aria-label="Tài khoản"
            >
              <i class="bi bi-person"></i>
            </router-link>

            <!-- Heart -->
            <a
              href="#"
              class="text-white"
              aria-label="Yêu thích"
              @click.prevent
            >
              <i class="bi bi-heart"></i>
            </a>

            <!-- Cart -->
            <div class="cart-wrap" ref="cartWrap">
              <div class="d-flex align-items-center gap-2">
                <router-link
                  :to="{ name: 'Cart' }"
                  class="text-white position-relative"
                  aria-label="Giỏ hàng"
                >
                  <i class="bi bi-bag"></i>
                  <span
                    class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-primary cart-badge"
                  >
                    {{ totalQty }}
                  </span>
                </router-link>

                <button
                  class="mini-cart-trigger text-white"
                  type="button"
                  aria-label="Mở giỏ hàng nhanh"
                  @click.stop="toggleCart"
                >
                  <i class="bi bi-chevron-down"></i>
                </button>
              </div>

              <div
                v-if="cartOpen"
                class="cart-overlay"
                @click="closeCart"
              ></div>
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

      <!-- Menu -->
      <nav class="nav-menu py-2 bg-light-blue">
        <div class="container d-flex justify-content-center gap-4">
          <router-link to="/shop" class="nav-link">Cửa hàng</router-link>
          <router-link
            :to="{ name: 'Search', query: { cat: 'bo-vest-nam' } }"
            class="nav-link"
            >Bộ vest nam</router-link
          >
          <router-link
            :to="{ name: 'Search', query: { cat: 'trang-phuc' } }"
            class="nav-link"
            >Trang phục</router-link
          >
          <router-link
            :to="{ name: 'Search', query: { cat: 'vest-nam' } }"
            class="nav-link"
            >Vest nam</router-link
          >
          <router-link
            :to="{ name: 'Search', query: { cat: 'doc-quyen-online' } }"
            class="nav-link"
            >Độc quyền online</router-link
          >
          <router-link
            :to="{ name: 'Search', query: { cat: 'slim-fit' } }"
            class="nav-link"
            >Slim fit</router-link
          >
          <router-link
            :to="{ name: 'Search', query: { cat: 'lien-he' } }"
            class="nav-link"
            >Liên hệ</router-link
          >
          <router-link
            :to="{ name: 'Search', query: { cat: 'ankasa' } }"
            class="nav-link"
            >Ankasa</router-link
          >
        </div>
      </nav>
    </div>

    <main>
      <router-view />
    </main>

    <!-- Footer -->
    <footer class="footer-bg pt-5 pb-3" style="background-color: #000f51">
      <div class="container">
        <div class="row">
          <div class="col-lg-3 col-md-6 mb-4 text-center text-lg-start">
            <img
              :src="logoUrl"
              alt="Logo"
              class="img-fluid"
              style="max-height: 100px"
            />
          </div>

          <div class="col-lg-2 col-md-3 col-6 mb-4 text-white">
            <h6 class="fw-bold mb-3 text-white">Hỗ trợ</h6>
            <ul class="list-unstyled">
              <li>
                <a href="#" class="footer-link" @click.prevent
                  >Hướng dẫn mua hàng</a
                >
              </li>
              <li>
                <a href="#" class="footer-link" @click.prevent
                  >Chính sách đổi trả</a
                >
              </li>
              <li>
                <a href="#" class="footer-link" @click.prevent
                  >Chính sách bảo hành</a
                >
              </li>
            </ul>
          </div>

          <div class="col-lg-2 col-md-3 col-6 mb-4 text-white">
            <h6 class="fw-bold mb-3 text-white">Danh mục</h6>
            <ul class="list-unstyled">
              <li>
                <router-link to="/shop" class="footer-link"
                  >Cửa hàng</router-link
                >
              </li>
              <li>
                <router-link
                  :to="{ name: 'Search', query: { cat: 'vest-nam' } }"
                  class="footer-link"
                  >Vest nam</router-link
                >
              </li>
              <li>
                <router-link
                  :to="{ name: 'Search', query: { cat: 'slim-fit' } }"
                  class="footer-link"
                  >Slim fit</router-link
                >
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
                <a
                  href="#"
                  class="footer-link footer-icon"
                  aria-label="Facebook"
                  @click.prevent
                >
                  <i class="bi bi-facebook"></i>
                </a>
              </li>
              <li>
                <a
                  href="#"
                  class="footer-link footer-icon"
                  aria-label="Instagram"
                  @click.prevent
                >
                  <i class="bi bi-instagram"></i>
                </a>
              </li>
              <li>
                <a
                  href="#"
                  class="footer-link footer-icon"
                  aria-label="YouTube"
                  @click.prevent
                >
                  <i class="bi bi-youtube"></i>
                </a>
              </li>
              <li>
                <a
                  href="#"
                  class="footer-link footer-icon"
                  aria-label="TikTok"
                  @click.prevent
                >
                  <i class="bi bi-tiktok"></i>
                </a>
              </li>
            </ul>

            <h6 class="fw-bold mb-3 text-white">Phương thức thanh toán</h6>
            <ul class="list-unstyled d-flex flex-wrap gap-2 mb-0">
              <li>
                <span class="payment-badge" title="Thanh toán khi nhận hàng"
                  >COD</span
                >
              </li>
              <li>
                <span
                  class="payment-badge"
                  title="Thẻ nội địa / Internet Banking"
                  >ATM</span
                >
              </li>
              <li><span class="payment-badge" title="Visa">VISA</span></li>
              <li><span class="payment-badge" title="JCB">JCB</span></li>
            </ul>
          </div>
        </div>

        <div class="footer-bottom pt-3 border-top text-white">
          <small>© 2026 VestShop. The Boy Team.</small>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";
import CartMiniModal from "../components/cart/CartMiniModal.vue";
import { useCart } from "../composables/useCart";

const router = useRouter();

/** ====== CART (UI trước: localStorage) ====== */
const cartOpen = ref(false);
const cartWrap = ref(null);
const { totalQty } = useCart();

function toggleCart() {
  cartOpen.value = !cartOpen.value;
}

function closeCart() {
  cartOpen.value = false;
}

function goToCart() {
  closeCart();
  router.push({ name: "Cart" });
}

function checkout() {
  closeCart();
  router.push({ name: "Checkout" });
}

// Logo
const logoUrl = `${import.meta.env.VITE_API_BASE || ""}/uploads/tbt_4_white.png`;

const keyword = ref("");

function doSearch() {
  const q = keyword.value.trim();
  router.push({ name: "Search", query: q ? { q } : {} });
}

/** ====== USER MENU (REACTIVE) ====== */
const userMenuOpen = ref(false);
const userWrap = ref(null);

const isLoggedIn = ref(false);
const userName = ref("Khách hàng");

function syncAuth() {
  const token =
    localStorage.getItem("USER_ACCESS_TOKEN") ||
    sessionStorage.getItem("USER_ACCESS_TOKEN");

  isLoggedIn.value = !!token;

  userName.value =
    localStorage.getItem("USER_NAME") ||
    sessionStorage.getItem("USER_NAME") ||
    "Khách hàng";
}

// Chỉ xóa guestId tạm thời, KHÔNG xóa toàn bộ conversationId:*
// để chat widget còn có thể giữ/lấy lại hội thoại cũ
function clearChatStorage() {
  localStorage.removeItem("guestId");
}

function toggleUserMenu() {
  userMenuOpen.value = !userMenuOpen.value;
}

function openProfile() {
  userMenuOpen.value = false;
  alert("Hồ sơ (demo) - sau bạn làm trang profile/me nhé.");
}

function logout() {
  userMenuOpen.value = false;

  localStorage.removeItem("USER_ACCESS_TOKEN");
  sessionStorage.removeItem("USER_ACCESS_TOKEN");
  localStorage.removeItem("USER_NAME");
  sessionStorage.removeItem("USER_NAME");

  localStorage.removeItem("vest_user");
  localStorage.removeItem("vest_token");
  localStorage.removeItem("vest_role");

  clearChatStorage();

  window.dispatchEvent(new Event("auth-changed"));

  syncAuth();
  router.push("/");
}

function onDocClick(e) {
  if (!userWrap.value) return;
  if (!userWrap.value.contains(e.target)) userMenuOpen.value = false;

  if (cartOpen.value && cartWrap.value && !cartWrap.value.contains(e.target)) {
    cartOpen.value = false;
  }
}

function onAuthChanged() {
  syncAuth();
}

onMounted(() => {
  syncAuth();
  document.addEventListener("click", onDocClick);
  window.addEventListener("auth-changed", onAuthChanged);
  window.addEventListener("storage", onAuthChanged);
});

onBeforeUnmount(() => {
  document.removeEventListener("click", onDocClick);
  window.removeEventListener("auth-changed", onAuthChanged);
  window.removeEventListener("storage", onAuthChanged);
});
</script>

<style>
:root {
  --sky-aqua: #07c8f9;
  --fresh-sky: #09a6f3;
  --brilliant-azure: #0a85ed;
  --royal-blue: #0c63e7;
  --bright-indigo: #0d41e1;
  --pale-blue-bg: #e8f6fa;
}

.container {
  max-width: 1600px;
}
@media (min-width: 1800px) {
  .container {
    max-width: 1720px;
  }
}

.client-layout {
  font-family: "Helvetica Neue", Arial, sans-serif;
}

/* Fixed header like OWEN */
.site-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}

main {
  /* chừa chỗ cho header + menu cố định */
  padding-top: 132px;
}

@media (max-width: 576px) {
  main {
    padding-top: 120px;
  }
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
  height: 72px;
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

/* Mini cart */
.cart-wrap {
  position: relative;
}

.cart-overlay {
  position: fixed;
  inset: 0;
  background: transparent;
  z-index: 1090;
}

.cart-mini {
  position: fixed;
  top: 88px; /* ngay dưới header */
  right: 18px;
  z-index: 1100;
}

@media (max-width: 576px) {
  .cart-mini {
    right: 10px;
    top: 84px;
  }
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
  color: #fff;
  text-decoration: none;
  display: inline-block;
  margin-bottom: 6px;
}
.footer-link:hover {
  color: var(--sky-aqua);
}

.footer-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 0;
  font-size: 18px;
  line-height: 1;
}
.footer-icon i {
  font-size: 18px;
  line-height: 1;
}

.footer-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.footer-contact-wrap {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 18px;
}

.footer-connect {
  text-align: right;
  min-width: 160px;
}

.footer-connect-title {
  font-weight: 800;
  margin-bottom: 10px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  display: inline-block;
  padding-bottom: 6px;
  border-bottom: 2px solid rgba(255, 255, 255, 0.75);
}

@media (max-width: 576px) {
  .footer-bottom {
    flex-direction: column;
  }

  .footer-contact-wrap {
    flex-direction: column;
  }

  .footer-connect {
    text-align: left;
  }
}

.footer-social {
  display: inline-flex;
  align-items: center;
  gap: 14px;
  font-size: 18px;
}

.footer-social-link {
  color: #fff;
  text-decoration: none;
  opacity: 0.95;
}

.footer-social-link:hover {
  color: var(--sky-aqua);
  opacity: 1;
}

/* Payment methods (layout like OWEN) */
.footer-payment {
  margin-top: 16px;
}

.footer-payment-title {
  margin-top: 18px;
}

.footer-payment-list {
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
}

.payment-badge {
  background: #ffffff;
  color: #000;
  border-radius: 3px;
  padding: 4px 6px;
  font-size: 11px;
  font-weight: 800;
  line-height: 1;
}

@media (max-width: 576px) {
  .footer-payment-list {
    justify-content: flex-start;
  }
}

/* ===== USER DROPDOWN (HEADER) ===== */
.user-dd {
  position: relative;
  display: inline-flex;
}

.user-btn {
  border: 0;
  background: transparent;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 6px;
  border-radius: 10px;
}

.user-btn:hover {
  background: rgba(0, 0, 0, 0.06);
}

.user-name {
  font-size: 14px;
  font-weight: 700;
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.caret {
  font-size: 12px;
  opacity: 0.7;
}

.user-menu {
  position: absolute;
  right: 0;
  top: calc(100% + 10px);
  min-width: 220px;
  border: 1px solid #eee;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.12);
  padding: 6px;
  z-index: 99;
}

.user-menu-header {
  padding: 10px 10px 8px;
  font-weight: 800;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 6px;
}

.user-menu-item {
  width: 100%;
  border: 0;
  background: transparent;
  padding: 10px 10px;
  text-align: left;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
}

.user-menu-item:hover {
  background: #f5f5f5;
}

.user-menu-item.danger {
  color: #b42318;
}
.mini-cart-trigger {
  border: 0;
  background: transparent;
  padding: 0;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
</style>