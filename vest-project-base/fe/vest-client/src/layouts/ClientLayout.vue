<template>
  <div class="client-layout">
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
                  Hồ sơ
                </button>

                <button
                  class="user-menu-item"
                  type="button"
                  @click="openMyOrders"
                >
                  Đơn hàng của tôi
                </button>

                <button
                  class="user-menu-item danger"
                  type="button"
                  @click="openLogoutConfirm"
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

            <div
              v-if="isLoggedIn"
              class="client-noti-wrap"
              ref="notificationWrap"
            >
              <button
                class="client-noti-trigger text-white position-relative"
                type="button"
                aria-label="Thông báo"
                @click.stop="toggleNotification"
              >
                <i class="bi bi-bell"></i>
                <span
                  v-if="unreadCount > 0"
                  class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger client-noti-badge"
                >
                  {{ unreadCount }}
                </span>
              </button>

              <div v-if="notificationOpen" class="client-noti-menu">
                <div class="client-noti-head">
                  <div class="client-noti-title">Thông báo</div>

                  <button
                    class="client-noti-read-all"
                    type="button"
                    :disabled="
                      notificationsLocal.length === 0 || unreadCount === 0
                    "
                    @click="markAllRead"
                  >
                    Đã đọc tất cả
                  </button>
                </div>

                <div
                  v-if="notificationsLocal.length === 0"
                  class="client-noti-empty"
                >
                  Không có thông báo.
                </div>

                <ul v-else class="client-noti-list">
                  <li
                    v-for="n in notificationsLocal"
                    :key="n.id"
                    class="client-noti-item"
                    :class="{ unread: !n.read }"
                    @click="openNotification(n)"
                  >
                    <div class="client-noti-main">
                      <div class="client-noti-item-title">
                        {{ n.title }}
                      </div>

                      <div class="client-noti-item-time">
                        {{ n.time }}
                      </div>
                    </div>

                    <button
                      v-if="!n.read"
                      class="client-noti-mini"
                      type="button"
                      title="Đánh dấu đã đọc"
                      @click.stop="markRead(n.id)"
                    >
                      ✓
                    </button>
                  </li>
                </ul>
              </div>
            </div>

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
          <router-link
            to="/"
            class="nav-link"
            :class="{ 'is-active': isShopActive }"
          >
            Cửa hàng
          </router-link>

          <router-link
            :to="{ name: 'Search', query: { cat: 'vest-nam' } }"
            class="nav-link"
            :class="{ 'is-active': isVestNamActive }"
          >
            Vest nam
          </router-link>

          <router-link
            :to="discountRoute"
            class="nav-link"
            :class="{ 'is-active': isDiscountActive }"
          >
            Giảm giá
          </router-link>

          <router-link
            :to="{ name: 'Contact' }"
            class="nav-link"
            :class="{ 'is-active': isContactActive }"
          >
            Liên hệ
          </router-link>

          <router-link
            :to="{ name: 'OrderLookup' }"
            class="nav-link"
            :class="{ 'is-active': isOrderLookupActive }"
          >
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
                <a href="#" class="footer-link" @click.prevent>
                  Hướng dẫn mua hàng
                </a>
              </li>
              <li>
                <a href="#" class="footer-link" @click.prevent>
                  Chính sách đổi trả
                </a>
              </li>
              <li>
                <a href="#" class="footer-link" @click.prevent>
                  Chính sách bảo hành
                </a>
              </li>
            </ul>
          </div>

          <div class="col-lg-2 col-md-3 col-6 mb-4 text-white">
            <h6 class="fw-bold mb-3 text-white">Danh mục</h6>
            <ul class="list-unstyled">
              <li>
                <router-link to="/shop" class="footer-link">
                  Cửa hàng
                </router-link>
              </li>
              <li>
                <router-link
                  :to="{ name: 'Search', query: { cat: 'vest-nam' } }"
                  class="footer-link"
                >
                  Vest nam
                </router-link>
              </li>
              <li>
                <router-link :to="discountRoute" class="footer-link">
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
                <span class="payment-badge" title="Thanh toán khi nhận hàng">
                  COD
                </span>
              </li>
              <li>
                <span
                  class="payment-badge"
                  title="Thẻ nội địa / Internet Banking"
                >
                  ATM
                </span>
              </li>
              <li>
                <span class="payment-badge" title="Visa"> VISA </span>
              </li>
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

    <div
      v-if="logoutConfirmOpen"
      class="logout-confirm-backdrop"
      role="dialog"
      aria-modal="true"
      aria-labelledby="logoutConfirmTitle"
      @click.self="closeLogoutConfirm"
    >
      <div class="logout-confirm-modal">
        <div class="logout-confirm-icon">
          <i class="bi bi-box-arrow-right"></i>
        </div>

        <h5 id="logoutConfirmTitle" class="logout-confirm-title">
          Xác nhận đăng xuất
        </h5>

        <p class="logout-confirm-text">
          Bạn có chắc chắn muốn đăng xuất không?
        </p>

        <div class="logout-confirm-actions">
          <button
            type="button"
            class="btn-cancel-logout"
            @click="closeLogoutConfirm"
          >
            Hủy
          </button>

          <button type="button" class="btn-confirm-logout" @click="logout">
            Đăng xuất
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onBeforeUnmount, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import CartMiniModal from "../components/cart/CartMiniModal.vue";
import CartAddedToast from "../components/common/CartAddedToast.vue";
import { useCart } from "../composables/useCart";
import { getSiteLogoUrl, resolveMediaUrl } from "../utils/media";
import { CART_ADDED_EVENT } from "../services/cartService";
import { useNotificationStore } from "../stores/notification";

const router = useRouter();
const route = useRoute();
const notificationStore = useNotificationStore();

const cartOpen = ref(false);
const cartWrap = ref(null);
const notificationWrap = ref(null);
const notificationOpen = ref(false);
const { totalQty, clearCart, syncCart } = useCart();

const notificationsLocal = computed(() => notificationStore.items || []);
const unreadCount = computed(() => notificationStore.unreadCount || 0);

function toggleCart() {
  cartOpen.value = !cartOpen.value;
  userMenuOpen.value = false;
  notificationOpen.value = false;
}

function closeCart() {
  cartOpen.value = false;
}

function goToCart() {
  closeCart();
  notificationOpen.value = false;
  cartToast.value.open = false;
  router.push({ name: "Cart" });
}

function checkout() {
  closeCart();
  notificationOpen.value = false;
  router.push({ name: "Checkout" });
}

const keyword = ref("");
const logoUrl = computed(() => getSiteLogoUrl());

const discountRoute = computed(() => {
  try {
    const route = { name: "Discount" };
    const resolved = router.resolve(route);

    if (resolved?.matched?.length) {
      return route;
    }
  } catch {}

  return { name: "Search", query: { cat: "giam-gia" } };
});

const isShopActive = computed(() => {
  return (
    route.path === "/" ||
    route.path === "/shop" ||
    route.name === "Home" ||
    route.name === "Shop"
  );
});

const isVestNamActive = computed(() => {
  return route.name === "Search" && route.query?.cat === "vest-nam";
});

const isDiscountActive = computed(() => {
  return (
    route.name === "Discount" ||
    route.path === "/discount" ||
    (route.name === "Search" && route.query?.cat === "giam-gia")
  );
});

const isContactActive = computed(() => {
  return route.name === "Contact";
});

const isOrderLookupActive = computed(() => {
  return route.name === "OrderLookup";
});

function doSearch() {
  const q = keyword.value.trim();
  router.push({ name: "Search", query: q ? { q } : {} });
}

const userMenuOpen = ref(false);
const userWrap = ref(null);
const isLoggedIn = ref(false);
const userName = ref("Khách hàng");
const logoutConfirmOpen = ref(false);

function syncAuth() {
  const token =
    localStorage.getItem("USER_ACCESS_TOKEN") ||
    sessionStorage.getItem("USER_ACCESS_TOKEN") ||
    localStorage.getItem("vest_token") ||
    sessionStorage.getItem("vest_token");

  isLoggedIn.value = !!token;

  userName.value =
    localStorage.getItem("USER_NAME") ||
    sessionStorage.getItem("USER_NAME") ||
    getNameFromVestUser() ||
    "Khách hàng";
}

function initNotification() {
  if (isLoggedIn.value) {
    notificationStore.init?.();
  } else {
    notificationOpen.value = false;
    notificationStore.disconnect?.();
  }
}

function getNameFromVestUser() {
  const raw =
    localStorage.getItem("vest_user") || sessionStorage.getItem("vest_user");

  if (!raw) return "";

  try {
    const user = JSON.parse(raw);
    return (
      user?.hoTen ||
      user?.tenKhachHang ||
      user?.fullName ||
      user?.name ||
      user?.email ||
      ""
    );
  } catch {
    return "";
  }
}

function toggleNotification() {
  notificationOpen.value = !notificationOpen.value;
  userMenuOpen.value = false;
  cartOpen.value = false;
}

function markRead(id) {
  notificationStore.markRead?.(id);
}

function markAllRead() {
  notificationStore.markAllRead?.();
}

function openNotification(n) {
  if (!n?.read && n?.id) {
    notificationStore.markRead?.(n.id);
  }

  notificationOpen.value = false;
  userMenuOpen.value = false;
  cartOpen.value = false;

  if (n?.link) {
    router.push(n.link);
  }
}

function toggleUserMenu() {
  userMenuOpen.value = !userMenuOpen.value;
  cartOpen.value = false;
  notificationOpen.value = false;
}

function openProfile() {
  userMenuOpen.value = false;
  notificationOpen.value = false;
  router.push({ name: "ClientProfile" });
}

function openMyOrders() {
  userMenuOpen.value = false;
  notificationOpen.value = false;
  router.push({ name: "MyOrders" });
}

function openLogoutConfirm() {
  userMenuOpen.value = false;
  cartOpen.value = false;
  notificationOpen.value = false;
  logoutConfirmOpen.value = true;
}

function closeLogoutConfirm() {
  logoutConfirmOpen.value = false;
}

function removeStorageKeys(storage, keys) {
  keys.forEach((key) => storage.removeItem(key));
}

function removeStorageKeysByPrefix(storage, prefixes) {
  const keysToRemove = [];

  for (let i = 0; i < storage.length; i += 1) {
    const key = storage.key(i);

    if (key && prefixes.some((prefix) => key.startsWith(prefix))) {
      keysToRemove.push(key);
    }
  }

  keysToRemove.forEach((key) => storage.removeItem(key));
}

function removeStorageKeysByKeyword(storage, keywords) {
  const keysToRemove = [];

  for (let i = 0; i < storage.length; i += 1) {
    const key = storage.key(i);
    const lowerKey = key ? key.toLowerCase() : "";

    if (key && keywords.some((keyword) => lowerKey.includes(keyword))) {
      keysToRemove.push(key);
    }
  }

  keysToRemove.forEach((key) => storage.removeItem(key));
}

function clearClientAuthStorage() {
  const authKeys = [
    "USER_ACCESS_TOKEN",
    "USER_NAME",
    "USER_ID",
    "USER_EMAIL",
    "USER_ROLE",
    "vest_user",
    "vest_token",
    "vest_role",
    "accessToken",
    "refreshToken",
    "token",
    "user",
  ];

  removeStorageKeys(localStorage, authKeys);
  removeStorageKeys(sessionStorage, authKeys);
}

function clearChatConversationStorage() {
  const chatKeys = [
    "conversationId",
    "conversation_id",
    "chatConversationId",
    "chat_conversation_id",
    "CLIENT_CHAT_CONVERSATION_ID",
    "clientChatConversationId",
  ];

  const prefixes = [
    "conversationId:",
    "conversation_id:",
    "chatConversationId:",
    "chat_conversation_id:",
    "CLIENT_CHAT_CONVERSATION_ID:",
    "clientChatConversationId:",
  ];

  const keywords = [
    "conversationid",
    "conversation_id",
    "chatconversation",
    "chat_conversation",
    "client_chat_conversation",
    "clientchatconversation",
    "convoid",
    "convo_id",
  ];

  removeStorageKeys(localStorage, chatKeys);
  removeStorageKeys(sessionStorage, chatKeys);

  removeStorageKeysByPrefix(localStorage, prefixes);
  removeStorageKeysByPrefix(sessionStorage, prefixes);

  removeStorageKeysByKeyword(localStorage, keywords);
  removeStorageKeysByKeyword(sessionStorage, keywords);
}

function logout() {
  logoutConfirmOpen.value = false;
  userMenuOpen.value = false;
  cartOpen.value = false;
  notificationOpen.value = false;
  cartToast.value.open = false;

  notificationStore.disconnect?.();

  clearCart();
  syncCart();

  clearChatConversationStorage();
  clearClientAuthStorage();

  syncAuth();

  window.dispatchEvent(new Event("auth-changed"));
  window.dispatchEvent(new Event("cart-changed"));

  router.push({ name: "Home" });
}

function handleClickOutside(e) {
  if (userWrap.value && !userWrap.value.contains(e.target)) {
    userMenuOpen.value = false;
  }

  if (cartWrap.value && !cartWrap.value.contains(e.target)) {
    cartOpen.value = false;
  }

  if (notificationWrap.value && !notificationWrap.value.contains(e.target)) {
    notificationOpen.value = false;
  }
}

function handleStorageChange() {
  syncAuth();
  initNotification();
}

const cartToast = ref({
  open: false,
  itemName: "",
  image: "",
  qty: 1,
});

let cartToastTimer = null;

function handleCartAdded(event) {
  const detail = event?.detail || {};
  const item = detail.item || {};

  cartToast.value = {
    open: true,
    itemName: item.name || item.tenSanPham || "Sản phẩm",
    image: resolveMediaUrl(
      item.image || item.imageUrl || item.anh || item.hinhAnh || "",
    ),
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
  initNotification();

  document.addEventListener("click", handleClickOutside);
  window.addEventListener("storage", handleStorageChange);
  window.addEventListener(CART_ADDED_EVENT, handleCartAdded);
});

onBeforeUnmount(() => {
  document.removeEventListener("click", handleClickOutside);
  window.removeEventListener("storage", handleStorageChange);
  window.removeEventListener(CART_ADDED_EVENT, handleCartAdded);

  notificationStore.disconnect?.();

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

.client-noti-wrap {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.client-noti-trigger {
  border: 0;
  background: transparent;
  padding: 0;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.client-noti-badge {
  font-size: 10px;
  min-width: 18px;
  height: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.client-noti-menu {
  position: absolute;
  top: calc(100% + 12px);
  right: 0;
  width: 330px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 14px 34px rgba(0, 0, 0, 0.18);
  border: 1px solid rgba(0, 0, 0, 0.08);
  overflow: hidden;
  z-index: 1100;
  color: #111827;
}

.client-noti-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-bottom: 1px solid #f1f5f9;
  background: #fff;
}

.client-noti-title {
  font-size: 15px;
  font-weight: 800;
  color: #111827;
}

.client-noti-read-all {
  border: 0;
  background: transparent;
  color: #0d6efd;
  font-size: 13px;
  font-weight: 600;
  padding: 0;
  cursor: pointer;
}

.client-noti-read-all:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.client-noti-empty {
  padding: 14px;
  color: #6b7280;
  font-size: 13px;
}

.client-noti-list {
  list-style: none;
  margin: 0;
  padding: 8px;
  max-height: 340px;
  overflow: auto;
}

.client-noti-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  padding: 10px;
  border-radius: 12px;
  cursor: pointer;
}

.client-noti-item:hover {
  background: #f8fafc;
}

.client-noti-item.unread {
  background: #eff6ff;
}

.client-noti-main {
  min-width: 0;
  flex: 1;
}

.client-noti-item-title {
  font-size: 13px;
  line-height: 1.4;
  font-weight: 700;
  color: #111827;
}

.client-noti-item-time {
  margin-top: 3px;
  font-size: 12px;
  color: #6b7280;
}

.client-noti-mini {
  width: 28px;
  height: 28px;
  border-radius: 10px;
  border: 1px solid #d1d5db;
  background: #fff;
  color: #111827;
  cursor: pointer;
  flex-shrink: 0;
  font-size: 13px;
  line-height: 1;
}

.client-noti-mini:hover {
  background: #f9fafb;
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
.nav-link.is-active {
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

.logout-confirm-backdrop {
  position: fixed;
  inset: 0;
  z-index: 3000;
  background: rgba(15, 23, 42, 0.46);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 18px;
}

.logout-confirm-modal {
  width: min(420px, 100%);
  background: #fff;
  border-radius: 18px;
  padding: 26px 24px 22px;
  text-align: center;
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.28);
  animation: logoutModalIn 0.18s ease;
}

.logout-confirm-icon {
  width: 58px;
  height: 58px;
  margin: 0 auto 14px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff1f2;
  color: #dc2626;
  font-size: 26px;
}

.logout-confirm-title {
  margin: 0 0 8px;
  font-size: 19px;
  font-weight: 800;
  color: #111827;
}

.logout-confirm-text {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
  line-height: 1.55;
}

.logout-confirm-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 22px;
}

.btn-cancel-logout,
.btn-confirm-logout {
  border: 0;
  border-radius: 999px;
  padding: 10px 22px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
}

.btn-cancel-logout {
  background: #f3f4f6;
  color: #374151;
}

.btn-cancel-logout:hover {
  background: #e5e7eb;
}

.btn-confirm-logout {
  background: #dc2626;
  color: #fff;
}

.btn-confirm-logout:hover {
  background: #b91c1c;
}

@keyframes logoutModalIn {
  from {
    opacity: 0;
    transform: translateY(8px) scale(0.98);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
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

  .client-noti-menu {
    width: min(330px, calc(100vw - 24px));
    right: -96px;
  }

  .cart-mini {
    right: -8px;
  }

  .logout-confirm-actions {
    flex-direction: column;
  }

  .btn-cancel-logout,
  .btn-confirm-logout {
    width: 100%;
  }
}
</style>
