import { createRouter, createWebHistory } from "vue-router";

import DefaultLayout from "../layouts/DefaultLayout.vue";
import BlankLayout from "../layouts/BlankLayout.vue";

import Dashboard from "../pages/Dashboard.vue";
import Login from "../pages/auth/Login.vue";

// ✅ thêm 2 page auth mới
import ForgotPassword from "../pages/auth/ForgotPassword.vue";
import ResetPasswordOtp from "../pages/auth/ResetPasswordOtp.vue";
import OtpVerify from "../pages/auth/OtpVerify.vue";

import ProductsList from "../pages/products/ProductsList.vue";
import ProductDetail from "../pages/products/ProductDetail.vue";
import OrdersList from "../pages/orders/OrdersList.vue";
import OrderDetail from "../pages/orders/OrderDetail.vue";

import CustomersList from "../pages/customers/CustomersList.vue";
import CustomersForm from "../pages/customers/CustomersForm.vue";
import CustomerDetail from "@/pages/customers/CustomerDetail.vue";

import StaffList from "../pages/staff/StaffList.vue";
import StaffForm from "../pages/staff/StaffForm.vue";
import StaffDetail from "../pages/staff/StaffDetail.vue";

import VouchersList from "../pages/vouchers/VouchersList.vue";
import VoucherCreate from "../pages/vouchers/VoucherCreate.vue";
import VoucherUpdate from "../pages/vouchers/VoucherUpdate.vue";

import ShiftTemplateList from "@/pages/shifts/ShiftTemplateList.vue";
import ShiftScheduler from "@/pages/shifts/ShiftScheduler.vue";
import MySchedule from "@/pages/shifts/MySchedule.vue";
import ShiftHandover from "@/pages/shifts/ShiftHandover.vue";

import Statistic from "@/pages/statistic/Statistic.vue";
import PaymentsList from "../pages/payments/PaymentsList.vue";
import NotFound from "../pages/notfound/NotFound.vue";

import { useAuthStore } from "../stores/auth";

const routes = [
  {
    path: "/",
    redirect: "/login",
  },

  // ====== AUTH (Public) ======
  {
    path: "/login",
    component: BlankLayout,
    children: [
      { path: "", name: "login", component: Login, meta: { public: true } },
    ],
  },

  // ✅ Quên mật khẩu: nhập email gửi OTP
  {
    path: "/forgot-password",
    component: BlankLayout,
    children: [
      { path: "", name: "forgot-password", component: ForgotPassword, meta: { public: true } },
    ],
  },

  // ✅ Reset mật khẩu: nhập OTP + mật khẩu mới
  {
    path: "/reset-password",
    component: BlankLayout,
    children: [
      { path: "", name: "reset-password", component: ResetPasswordOtp, meta: { public: true } },
    ],
  },

  {
    path: "/otp",
    component: BlankLayout,
    children: [{ path: "", name: "otp", component: OtpVerify, meta: { public: true } }],
  },

  // ====== APP (Private) ======
  {
    path: "/",
    component: DefaultLayout,
    meta: { requiresAuth: true },
    children: [
      // ✅ STAFF + ADMIN
      { path: "dashboard", name: "dashboard", component: Dashboard, meta: { roles: ["ADMIN", "STAFF"] } },
      // ⚠️ Windows/Linux phân biệt hoa thường -> folder là pages/Sales
      { path: "sales", name: "sales", component: () => import("../pages/Sales/SalesPage.vue"), meta: { roles: ["ADMIN", "STAFF"] } },
      { path: "orders", name: "orders", component: OrdersList, meta: { roles: ["ADMIN", "STAFF"] } },
      { path: "orders/:id", name: "order-detail", component: OrderDetail, props: true, meta: { roles: ["ADMIN", "STAFF"] } },

      { path: "customers", name: "customer-list", component: CustomersList, meta: { roles: ["ADMIN", "STAFF"] } },
      { path: "customers/new", name: "customer-new", component: CustomersForm, meta: { roles: ["ADMIN", "STAFF"] } },
      { path: "customers/:id/edit", name: "customer-edit", component: CustomersForm, props: true, meta: { roles: ["ADMIN", "STAFF"] } },
      { path: "customers/:id", name: "customer-detail", component: CustomerDetail, props: true, meta: { roles: ["ADMIN", "STAFF"] } },

      // ✅ ADMIN only
      { path: "statistic", name: "statistic", component: Statistic, meta: { roles: ["ADMIN"] } },

      { path: "products", name: "products", component: ProductsList, meta: { roles: ["ADMIN"] } },
      { path: "products/add", name: "product-add", component: () => import("../pages/products/ProductAdd.vue"), meta: { roles: ["ADMIN"] } },
      { path: "products/edit/:id", name: "product-edit", component: () => import("../pages/products/ProductAdd.vue"), props: true, meta: { roles: ["ADMIN"] } },
      { path: "products/:id", name: "product-detail", component: ProductDetail, props: true, meta: { roles: ["ADMIN"] } },

      { path: "shift-templates", name: "shift-templates", component: ShiftTemplateList, meta: { roles: ["ADMIN"] } },
      { path: "shift-scheduler", name: "shift-scheduler", component: ShiftScheduler, meta: { roles: ["ADMIN"] } },
      { path: "my-schedule", name: "my-schedule", component: MySchedule, meta: { roles: ["ADMIN", "STAFF"] } },
      { path: "shift-handover", name: "shift-handover", component: ShiftHandover, meta: { roles: ["ADMIN", "STAFF"] } },

      { path: "variants", name: "variants-list", component: () => import("../pages/products/VariantList.vue"), meta: { roles: ["ADMIN"] } },
      {
        path: "attributes/:type",
        name: "attributes",
        component: () => import("../pages/attributes/AttributeList.vue"),
        props: true,
        meta: { roles: ["ADMIN"] },
      },

      { path: "staff", name: "staff", component: StaffList, meta: { roles: ["ADMIN"] } },
      { path: "staff/new", name: "staff-new", component: StaffForm, meta: { roles: ["ADMIN"] } },
      { path: "staff/:id/edit", name: "staff-edit", component: StaffForm, props: true, meta: { roles: ["ADMIN"] } },
      { path: "staff/:id", name: "staff-detail", component: StaffDetail, props: true, meta: { roles: ["ADMIN"] } },

      // vouchers (PGG) => ADMIN-only
      { path: "vouchers", name: "vouchers", component: VouchersList, meta: { roles: ["ADMIN"] } },
      { path: "vouchers/create", name: "voucher-create", component: VoucherCreate, meta: { roles: ["ADMIN"] } },
      { path: "vouchers/update/:id", name: "voucher-update", component: VoucherUpdate, props: true, meta: { roles: ["ADMIN"] } },

      { path: "payments", name: "payments", component: PaymentsList, meta: { roles: ["ADMIN"] } },

      // ✅ Chat hỗ trợ (ADMIN + STAFF đều dùng được)
      {
        path: "chat-support",
        name: "chat-support",
        component: () => import("../chat/ChatSupportPage.vue"),
        meta: { roles: ["ADMIN", "STAFF"] },
      },
    ],
  },

  { path: "/:pathMatch(.*)*", name: "notfound", component: NotFound, meta: { public: true } },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to) => {
  const auth = useAuthStore();

  const requiresAuth = to.matched.some((r) => r.meta.requiresAuth);
  const isPublic = to.matched.some((r) => r.meta.public);

  // chưa login mà vào route cần auth => về login
  if (requiresAuth && !auth.isAuthenticated) return { name: "login" };

  // role check theo meta.roles
  const role = (auth.role || localStorage.getItem("role") || localStorage.getItem("vest_role") || "").toUpperCase();
  const allowedRoles = to.matched
    .map((r) => r.meta.roles)
    .filter(Boolean)
    .flat();

  if (!isPublic && allowedRoles.length > 0 && !allowedRoles.includes(role)) {
    // STAFF bấm nhầm trang admin-only -> đá về dashboard
    return { name: "dashboard" };
  }

  return true;
});

export default router;