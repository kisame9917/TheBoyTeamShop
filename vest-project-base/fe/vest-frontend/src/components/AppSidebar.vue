<template>
  <aside class="sidebar-shell">
    <div class="sidebar d-flex flex-column bg-white border-end">
      <div class="brand-box p-3 border-bottom text-center">
        <img
          src="../images/tbt_4.png"
          alt="TheBoyTeam Logo"
          class="brand-logo img-fluid"
        />

        <div class="brand-text mt-2">
          <div class="brand-name"></div>
        </div>
      </div>

      <nav class="p-2 flex-grow-1">
        <RouterLink
          to="/dashboard"
          class="nav-link-item"
          active-class="active"
          exact-active-class="active"
        >
          <i class="bi bi-house-door icon"></i>
          <span class="label">Trang Chủ</span>
        </RouterLink>

        <RouterLink
          v-if="isAdmin"
          to="/statistic"
          class="nav-link-item"
          active-class="active"
        >
          <i class="bi bi-bar-chart icon"></i>
          <span class="label">Thống kê</span>
        </RouterLink>

        <RouterLink to="/sales" class="nav-link-item" active-class="active">
          <i class="bi bi-cart3 icon"></i>
          <span class="label">Bán Hàng</span>
        </RouterLink>

        <RouterLink to="/orders" class="nav-link-item" active-class="active">
          <i class="bi bi-receipt-cutoff icon"></i>
          <span class="label">Hóa đơn</span>
        </RouterLink>

        <RouterLink
          v-if="isStaff"
          to="/customers"
          class="nav-link-item"
          active-class="active"
        >
          <i class="bi bi-person-lines-fill icon"></i>
          <span class="label">Khách hàng</span>
        </RouterLink>

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
            <i
              class="bi bi-chevron-down caret"
              :class="{ rotate: openGroups.products }"
            ></i>
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
            <i
              class="bi bi-chevron-down caret"
              :class="{ rotate: openGroups.attributes }"
            ></i>
          </button>

          <div v-if="openGroups.attributes" class="sub-wrap">
            <RouterLink
              to="/attributes/loai-san-pham"
              class="sub-link"
              active-class="active-sub"
            >
              <i class="bi bi-tags sub-icon"></i>
              Loại sản phẩm
            </RouterLink>

            <RouterLink
              to="/attributes/thuong-hieu"
              class="sub-link"
              active-class="active-sub"
            >
              <i class="bi bi-award sub-icon"></i>
              Thương hiệu
            </RouterLink>

            <RouterLink
              to="/attributes/so-khuy"
              class="sub-link"
              active-class="active-sub"
            >
              <i class="bi bi-circle-square sub-icon"></i>
              Số khuy
            </RouterLink>

            <RouterLink
              to="/attributes/kieu-tui"
              class="sub-link"
              active-class="active-sub"
            >
              <i class="bi bi-handbag sub-icon"></i>
              Kiểu túi
            </RouterLink>

            <RouterLink
              to="/attributes/ve-ao"
              class="sub-link"
              active-class="active-sub"
            >
              <i class="bi bi-person-badge sub-icon"></i>
              Ve áo
            </RouterLink>

            <RouterLink
              to="/attributes/xe-ta"
              class="sub-link"
              active-class="active-sub"
            >
              <i class="bi bi-scissors sub-icon"></i>
              Xẻ tà
            </RouterLink>

            <RouterLink
              to="/attributes/xuat-xu"
              class="sub-link"
              active-class="active-sub"
            >
              <i class="bi bi-globe-asia-australia sub-icon"></i>
              Xuất xứ
            </RouterLink>

            <RouterLink
              to="/attributes/fit"
              class="sub-link"
              active-class="active-sub"
            >
              <i class="bi bi-person-arms-up sub-icon"></i>
              Kiểu dáng
            </RouterLink>

            <RouterLink
              to="/attributes/chat-lieu"
              class="sub-link"
              active-class="active-sub"
            >
              <i class="bi bi-layers sub-icon"></i>
              Chất liệu
            </RouterLink>

            <RouterLink
              to="/attributes/mau-sac"
              class="sub-link"
              active-class="active-sub"
            >
              <i class="bi bi-palette sub-icon"></i>
              Màu sắc
            </RouterLink>

            <RouterLink
              to="/attributes/kich-co"
              class="sub-link"
              active-class="active-sub"
            >
              <i class="bi bi-rulers sub-icon"></i>
              Kích cỡ
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
            <i
              class="bi bi-chevron-down caret"
              :class="{ rotate: openGroups.shifts }"
            ></i>
          </button>

          <div v-if="openGroups.shifts" class="sub-wrap">
            <RouterLink
              v-if="isAdmin"
              to="/shift-templates"
              class="sub-link"
              active-class="active-sub"
            >
              <i class="bi bi-calendar2 sub-icon"></i>
              Ca làm việc
            </RouterLink>

            <RouterLink
              v-if="isAdmin"
              to="/shift-scheduler"
              class="sub-link"
              active-class="active-sub"
            >
              <i class="bi bi-calendar-check sub-icon"></i>
              Lịch nhân viên
            </RouterLink>

            <RouterLink
              to="/my-schedule"
              class="sub-link"
              active-class="active-sub"
            >
              <i class="bi bi-calendar-fill sub-icon"></i>
              Lịch của tôi
            </RouterLink>

            <RouterLink
              to="/shift-handover"
              class="sub-link"
              active-class="active-sub"
            >
              <i class="bi bi-arrow-left-right sub-icon"></i>
              Doanh thu ca làm
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
            <i
              class="bi bi-chevron-down caret"
              :class="{ rotate: openGroups.accounts }"
            ></i>
          </button>

          <div v-if="openGroups.accounts" class="sub-wrap">
            <RouterLink to="/staff" class="sub-link" active-class="active-sub">
              <i class="bi bi-person-badge sub-icon"></i>
              Nhân viên
            </RouterLink>

            <RouterLink
              to="/customers"
              class="sub-link"
              active-class="active-sub"
            >
              <i class="bi bi-person-lines-fill sub-icon"></i>
              Khách hàng
            </RouterLink>
          </div>
        </div>

        <RouterLink to="/chat-support" class="nav-link-item" active-class="active">
          <i class="bi bi-chat-dots icon"></i>
          <span class="label">Chat hỗ trợ</span>
          <span v-if="unreadCount > 0" class="badge-unread">{{ unreadCount }}</span>
        </RouterLink>
      </nav>
    </div>
  </aside>
</template>

<script setup>
import { reactive, watch, computed, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const unreadCount = ref(0);

const role = computed(() => {
  const r = (
    auth.role ||
    localStorage.getItem("role") ||
    localStorage.getItem("vest_role") ||
    ""
  ).toUpperCase();

  if (r) return r;
  if (auth.isAdmin) return "ADMIN";
  if (auth.isAuthenticated) return "STAFF";
  return "";
});

const isAdmin = computed(() => role.value === "ADMIN");
const isStaff = computed(() => role.value === "STAFF");

const openGroups = reactive({
  products: false,
  attributes: false,
  accounts: false,
  shifts: false,
});

const groupDefaultRoute = {
  products: "/products",
  attributes: "/attributes/loai-san-pham",
  accounts: "/staff",
};

function closeAllGroups() {
  Object.keys(openGroups).forEach((k) => {
    openGroups[k] = false;
  });
}

async function toggleGroup(key) {
  const isOpening = !openGroups[key];

  closeAllGroups();
  openGroups[key] = isOpening;

  if (!isOpening) return;

  if (key === "shifts") {
    const target = isAdmin.value ? "/shift-templates" : "/my-schedule";
    if (route.path !== target) await router.push(target);
    return;
  }

  if (!isAdmin.value) return;

  const target = groupDefaultRoute[key];

  if (target && route.path !== target) {
    await router.push(target);
  }
}

function syncGroupsWithRoute() {
  closeAllGroups();

  const p = route.path;

  if (p.startsWith("/products") || p.startsWith("/variants")) {
    openGroups.products = true;
  } else if (p.startsWith("/attributes")) {
    openGroups.attributes = true;
  } else if (p.startsWith("/staff") || p.startsWith("/customers")) {
    openGroups.accounts = true;
  } else if (
    p.includes("/shift") ||
    p.includes("/my-schedule") ||
    p.includes("/ca-lam-viec")
  ) {
    openGroups.shifts = true;
  }
}

watch(() => route.path, syncGroupsWithRoute, { immediate: true });
</script>

<style scoped>
.sidebar-shell {
  width: 260px;
  min-width: 260px;
  flex: 0 0 260px;
  height: 100vh;
}

.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: 280px;
  height: 100vh;
  overflow-y: hidden;
  overflow-x: hidden;
  z-index: 200;
  background:
    radial-gradient(circle at 18% 8%, rgba(37, 99, 235, 0.12), transparent 30%),
    linear-gradient(180deg, #f8fbff 0%, #ffffff 38%, #f3f7ff 100%) !important;
  border-right: 1px solid rgba(37, 99, 235, 0.12) !important;
  box-shadow: 12px 0 30px rgba(15, 23, 42, 0.06);
  overscroll-behavior: contain;
}

.sidebar:hover {
  overflow-y: auto;
}

.brand-box {
  padding: 18px 14px 16px !important;
  //background:
  // linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(239, 246, 255, 0.9));
  border-bottom: 1px solid rgba(37, 99, 235, 0.14) !important;
  box-shadow: 0 10px 22px rgba(37, 99, 235, 0.05);
}

.brand-logo {
  max-width: 170px;
  filter: drop-shadow(0 10px 18px rgba(15, 23, 42, 0.12));
}

.brand-text {
  line-height: 1.1;
}

.brand-name {
  font-size: 25px;
  font-weight: 700;
  letter-spacing: 0.4px;
}

.brand-strong {
  color: #0f172a;
}

.brand-tagline {
  margin-top: 6px;
  font-size: 12px;
  color: #64748b;
  font-weight: 600;
  letter-spacing: 1.2px;
  text-transform: uppercase;
}

nav {
  padding: 12px 10px 16px !important;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.4), rgba(239, 246, 255, 0.28));
}

.nav-link-item {
  width: calc(100% - 12px);
  box-sizing: border-box;
  display: flex;
  align-items: center;
  gap: 12px;
  height: 44px;
  padding: 0 12px;
  border-radius: 14px;
  margin: 6px 6px;
  border: 1px solid transparent;
  background: transparent;
  color: #475569;
  text-decoration: none;
  user-select: none;
  position: relative;
  overflow: hidden;
  transition:
    background-color 0.18s ease,
    color 0.18s ease,
    border-color 0.18s ease,
    box-shadow 0.18s ease,
    transform 0.08s ease;
}

.nav-link-item::before {
  content: "";
  position: absolute;
  inset: 7px auto 7px 0;
  width: 3px;
  border-radius: 999px;
  background: transparent;
  transition: background-color 0.18s ease;
}

.nav-link-item.is-static {
  cursor: default;
  opacity: 0.85;
}

.nav-link-item:is(:hover, :focus-visible) {
  background: rgba(37, 99, 235, 0.08);
  color: #1e3a8a;
  border-color: rgba(37, 99, 235, 0.14);
  box-shadow: 0 10px 22px rgba(15, 23, 42, 0.06);
  outline: 0;
}

.nav-link-item:is(:hover, :focus-visible)::before {
  background: rgba(37, 99, 235, 0.5);
}

.nav-link-item:active {
  transform: translateY(1px);
}

.nav-link-item.active {
  background: linear-gradient(135deg, #1d4ed8 0%, #2954b8 55%, #1e3a8a 100%);
  color: #ffffff;
  border-color: rgba(255, 255, 255, 0.18);
  box-shadow: 0 12px 26px rgba(37, 99, 235, 0.28);
}

.nav-link-item.active::before {
  background: #ffffff;
}

.nav-link-item.active:is(:hover, :focus-visible) {
  background: linear-gradient(135deg, #1d4ed8 0%, #2954b8 55%, #1e3a8a 100%);
  color: #ffffff;
  border-color: rgba(255, 255, 255, 0.22);
  box-shadow: 0 14px 30px rgba(37, 99, 235, 0.32);
}

.icon {
  font-size: 18px;
  width: 24px;
  height: 24px;
  border-radius: 9px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  opacity: 0.98;
  color: inherit;
  background: rgba(15, 23, 42, 0.04);
  transition:
    background-color 0.18s ease,
    color 0.18s ease;
}

.nav-link-item:hover .icon {
  background: rgba(37, 99, 235, 0.1);
}

.nav-link-item.active .icon {
  background: rgba(255, 255, 255, 0.18);
  color: #ffffff;
}

.label {
  flex: 1;
  font-weight: 700;
  font-size: 14px;
}

.badge-unread {
  min-width: 22px;
  height: 22px;
  padding: 0 7px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #ef4444, #dc2626);
  box-shadow: 0 6px 12px rgba(239, 68, 68, 0.24);
}

.caret {
  font-size: 12px;
  opacity: 0.8;
  transition:
    transform 0.18s ease,
    opacity 0.18s ease;
}

.caret.rotate {
  transform: rotate(180deg);
  opacity: 1;
}

.sub-wrap {
  margin: 4px 8px 12px 18px;
  padding: 8px 0 6px 12px;
  border-left: 2px solid rgba(37, 99, 235, 0.16);
  background: linear-gradient(90deg, rgba(37, 99, 235, 0.04), transparent 72%);
  border-radius: 0 14px 14px 0;
}

.sub-link {
  width: 100%;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 38px;
  padding: 0 10px;
  margin: 4px 0;
  border-radius: 12px;
  color: #64748b;
  text-decoration: none;
  font-size: 13.5px;
  font-weight: 600;
  border: 1px solid transparent;
  background: transparent;
  transition:
    background-color 0.18s ease,
    color 0.18s ease,
    border-color 0.18s ease,
    box-shadow 0.18s ease;
}

.sub-link:hover {
  background: rgba(37, 99, 235, 0.08);
  color: #1e3a8a;
  border-color: rgba(37, 99, 235, 0.14);
}

.sub-icon {
  font-size: 14px;
  width: 20px;
  height: 20px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  opacity: 0.95;
  color: inherit;
  background: rgba(15, 23, 42, 0.04);
}

.sub-link.active-sub {
  background: #eef4ff;
  color: #1d4ed8;
  font-weight: 700;
  border-color: rgba(37, 99, 235, 0.22);
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.08);
  position: relative;
}

.sub-link.active-sub .sub-icon {
  background: rgba(37, 99, 235, 0.12);
}

.sub-link.active-sub::before {
  content: "";
  position: absolute;
  left: -16px;
  top: 50%;
  width: 7px;
  height: 7px;
  border-radius: 999px;
  transform: translateY(-50%);
  background: #2563eb;
  box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.12);
}

.sidebar::-webkit-scrollbar {
  width: 10px;
}

.sidebar::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar::-webkit-scrollbar-thumb {
  background: rgba(37, 99, 235, 0.18);
  border-radius: 999px;
  border: 3px solid #ffffff;
}

.sidebar::-webkit-scrollbar-thumb:hover {
  background: rgba(37, 99, 235, 0.32);
}
</style>