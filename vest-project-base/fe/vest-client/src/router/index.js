import { createRouter, createWebHistory } from "vue-router";

// Layout
import ClientLayout from "../layouts/ClientLayout.vue";

// Pages (client)
import HomePage from "../pages/client/HomePage.vue";
import SearchPage from "../pages/client/SearchPage.vue";
import ProductDetail from "../pages/client/ProductDetail.vue";
import MockPaymentPage from "../pages/client/MockPayment.vue";

const CartPage = () => import("../pages/client/CartPage.vue");
const ContactPage = () => import("../pages/client/ContactPage.vue");
const MyOrdersPage = () => import("../pages/client/MyOrdersPage.vue");
const ProfilePage = () => import("../pages/client/ProfilePage.vue");

// Pages (auth)
import Login from "../pages/auth/Login.vue";

const OAuth2Redirect = () => import("../pages/OAuth2Redirect.vue");
const ForgotPassword = () => import("../pages/auth/ForgotPassword.vue");
const OtpVerify = () => import("../pages/auth/OtpVerify.vue");
const ResetPasswordOtp = () => import("../pages/auth/ResetPasswordOtp.vue");

const CheckoutPage = () => import("../pages/client/CheckoutPage.vue");
const OrderLookupPage = () => import("../pages/client/OrderLookupPage.vue");
const CheckoutSuccessPage = () => import("../pages/client/CheckoutSuccessPage.vue");

const routes = [
  {
    path: "/",
    component: ClientLayout,
    children: [
      { path: "", name: "Home", component: HomePage },
      { path: "shop", name: "Shop", component: SearchPage },
      { path: "search", name: "Search", component: SearchPage },
      { path: "cart", name: "Cart", component: CartPage },
      { path: "product/:id", name: "ProductDetail", component: ProductDetail, props: true },
      { path: "checkout", name: "Checkout", component: CheckoutPage },
      { path: "checkout/success", name: "CheckoutSuccess", component: CheckoutSuccessPage },
      {
        path: "ho-so",
        name: "ClientProfile",
        component: ProfilePage,
        meta: { requiresClientAuth: true },
      },
      {
        path: "don-hang-cua-toi",
        name: "MyOrders",
        component: MyOrdersPage,
        meta: { requiresClientAuth: true },
      },
      { path: "contact", name: "Contact", component: ContactPage },
      { path: "tra-cuu-don-hang", name: "OrderLookup", component: OrderLookupPage },
      { path: "mock-payment", name: "MockPayment", component: MockPaymentPage },
    ],
  },

  { path: "/login", name: "Login", component: Login },
  { path: "/oauth2/redirect", name: "OAuth2Redirect", component: OAuth2Redirect },
  { path: "/forgot-password", name: "ForgotPassword", component: ForgotPassword },
  { path: "/otp-verify", name: "OtpVerify", component: OtpVerify },
  { path: "/reset-password-otp", name: "ResetPasswordOtp", component: ResetPasswordOtp },

  { path: "/:pathMatch(.*)*", redirect: "/" },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 };
  },
});

router.beforeEach((to, from, next) => {
  const hasToken =
    !!localStorage.getItem("USER_ACCESS_TOKEN") ||
    !!sessionStorage.getItem("USER_ACCESS_TOKEN") ||
    !!localStorage.getItem("vest_token");

  if (to.matched.some((record) => record.meta.requiresClientAuth) && !hasToken) {
    next({ name: "Login", query: { redirect: to.fullPath } });
    return;
  }

  next();
});

export default router;