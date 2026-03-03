import { createRouter, createWebHistory } from "vue-router";

// Layout
import ClientLayout from "../layouts/ClientLayout.vue";

// Pages (client)
import HomePage from "../pages/client/HomePage.vue";
import SearchPage from "../pages/client/SearchPage.vue";
import ProductDetail from "../pages/client/ProductDetail.vue";

// Pages (auth)
import Login from "../pages/auth/Login.vue";

// ✅ OAuth redirect page
const OAuth2Redirect = () => import("../pages/OAuth2Redirect.vue");

// ✅ Forgot password flow (pages/auth)
const ForgotPassword = () => import("../pages/auth/ForgotPassword.vue");
const OtpVerify = () => import("../pages/auth/OtpVerify.vue");
const ResetPasswordOtp = () => import("../pages/auth/ResetPasswordOtp.vue");

const routes = [
  {
    path: "/",
    component: ClientLayout,
    children: [
      { path: "", name: "Home", component: HomePage },
      { path: "shop", name: "Shop", component: SearchPage },
      { path: "search", name: "Search", component: SearchPage },
      { path: "product/:id", name: "ProductDetail", component: ProductDetail, props: true },
    ],
  },

  // ✅ Auth routes
  { path: "/login", name: "Login", component: Login },

  // ✅ Google OAuth2 redirect
  { path: "/oauth2/redirect", name: "OAuth2Redirect", component: OAuth2Redirect },

  // ✅ Forgot password flow
  { path: "/forgot-password", name: "ForgotPassword", component: ForgotPassword },
  { path: "/otp-verify", name: "OtpVerify", component: OtpVerify },
  { path: "/reset-password-otp", name: "ResetPasswordOtp", component: ResetPasswordOtp },

  // ✅ fallback luôn để cuối
  { path: "/:pathMatch(.*)*", redirect: "/" },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 };
  },
});

export default router;