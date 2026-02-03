import { createRouter, createWebHistory } from "vue-router";

import DefaultLayout from "../layouts/DefaultLayout.vue";
import BlankLayout from "../layouts/BlankLayout.vue";

import Dashboard from "../pages/Dashboard.vue";
import Login from "../pages/auth/Login.vue";

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

import Statistic from "@/pages/statistic/Statistic.vue";
import PaymentsList from "../pages/payments/PaymentsList.vue";
import NotFound from "../pages/notfound/NotFound.vue";

import { useAuthStore } from "../stores/auth";

const routes = [
  {
    path: "/",
    redirect: () => (localStorage.getItem("vest_token") ? "/dashboard" : "/login"),
  },

  {
    path: "/login",
    component: BlankLayout,
    children: [{ path: "", name: "login", component: Login, meta: { public: true } }],
  },

  {
    path: "/",
    component: DefaultLayout,
    meta: { requiresAuth: true },
    children: [
      // ✅ STAFF + ADMIN
      { path: "dashboard", name: "dashboard", component: Dashboard, meta: { roles: ["ADMIN", "STAFF"] } },
      { path: "sales", name: "sales", component: () => import("../pages/sales/SalesPage.vue"), meta: { roles: ["ADMIN", "STAFF"] } },
      { path: "orders", name: "orders", component: OrdersList, meta: { roles: ["ADMIN", "STAFF"] } },
      { path: "orders/:id", name: "order-detail", component: OrderDetail, props: true, meta: { roles: ["ADMIN", "STAFF"] } },

      { path: "customers", name: "customer-list", component: CustomersList, meta: { roles: ["ADMIN", "STAFF"] } },
      { path: "customers/new", name: "customer-new", component: CustomersForm, meta: { roles: ["ADMIN", "STAFF"] } },
      { path: "customers/:id/edit", name: "customer-edit", component: CustomersForm, props: true, meta: { roles: ["ADMIN", "STAFF"] } },
      { path: "customers/:id", name: "customer-detail", component: CustomerDetail, props: true, meta: { roles: ["ADMIN", "STAFF"] } },

      // ✅ ADMIN-ONLY (bạn có thể đổi lại nếu STAFF được xem)
      { path: "statistic", name: "statistic", component: Statistic, meta: { roles: ["ADMIN"] } },

      { path: "products", name: "products", component: ProductsList, meta: { roles: ["ADMIN"] } },
      { path: "products/add", name: "product-add", component: () => import("../pages/products/ProductAdd.vue"), meta: { roles: ["ADMIN"] } },
      { path: "products/edit/:id", name: "product-edit", component: () => import("../pages/products/ProductAdd.vue"), props: true, meta: { roles: ["ADMIN"] } },
      { path: "products/:id", name: "product-detail", component: ProductDetail, props: true, meta: { roles: ["ADMIN"] } },

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

      // vouchers (PGG) => thường ADMIN-only
      { path: "vouchers", name: "vouchers", component: VouchersList, meta: { roles: ["ADMIN"] } },
      { path: "vouchers/create", name: "voucher-create", component: VoucherCreate, meta: { roles: ["ADMIN"] } },
      { path: "vouchers/update/:id", name: "voucher-update", component: VoucherUpdate, props: true, meta: { roles: ["ADMIN"] } },

      { path: "payments", name: "payments", component: PaymentsList, meta: { roles: ["ADMIN"] } },
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

  // đã login mà vào login => về dashboard
  if (to.name === "login" && auth.isAuthenticated) return { name: "dashboard" };

  // role check theo meta.roles
  const role = (auth.role || localStorage.getItem("role") || "").toUpperCase(); // tuỳ bạn lưu role ở đâu
  const allowedRoles = to.matched
    .map((r) => r.meta.roles)
    .filter(Boolean)
    .flat();

  if (!isPublic && allowedRoles.length > 0 && !allowedRoles.includes(role)) {
    // STAFF bấm nhầm trang admin-only -> đá về dashboard (hoặc sales)
    return { name: "dashboard" };
  }

  return true;
});

export default router;
