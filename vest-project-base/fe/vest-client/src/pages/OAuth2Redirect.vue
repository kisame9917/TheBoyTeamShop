<template>
  <div class="wrap">
    <div class="card">
      <div class="title">Đang đăng nhập...</div>
      <div class="desc">Vui lòng chờ trong giây lát.</div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import axios from "axios";

const route = useRoute();
const router = useRouter();

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

function clearAuthStorage() {
  localStorage.removeItem("USER_ACCESS_TOKEN");
  sessionStorage.removeItem("USER_ACCESS_TOKEN");
  localStorage.removeItem("USER_NAME");
  sessionStorage.removeItem("USER_NAME");

  // clear kiểu vest_ cũ luôn cho sạch
  localStorage.removeItem("vest_user");
  localStorage.removeItem("vest_token");
  localStorage.removeItem("vest_role");
}

onMounted(async () => {
  const token = route.query.token;

  if (!token) {
    await router.replace("/login?error=google_login_failed");
    return;
  }

  clearAuthStorage();
  localStorage.setItem("USER_ACCESS_TOKEN", token);
  localStorage.setItem("vest_token", token);

  try {
    const res = await axios.get(`${BASE_URL}/api/client/auth/me`, {
      headers: { Authorization: `Bearer ${token}` },
    });

    const me = res?.data || {};

    localStorage.setItem("USER_NAME", me.tenKhachHang || "");

    // QUAN TRỌNG: chat widget đang đọc vest_user
    localStorage.setItem(
      "vest_user",
      JSON.stringify({
        id: me.taiKhoan,
        taiKhoan: me.taiKhoan,
        tenKhachHang: me.tenKhachHang,
        email: me.email,
        role: "CLIENT",
      })
    );
    localStorage.setItem("vest_role", "CLIENT");

    // báo cho UI/chat biết auth đã đổi
    window.dispatchEvent(new Event("auth-changed"));
  } catch (e) {
    console.warn("Không lấy được thông tin khách hàng:", e?.response?.data || e?.message);
  }

  const redirect = route.query.redirect || "/";
  await router.replace(redirect);
});
</script>

<style scoped>
.wrap {
  min-height: calc(100vh - 140px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 12px;
}
.card {
  width: 100%;
  max-width: 420px;
  border: 1px solid #eee;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.06);
  background: #fff;
  text-align: center;
}
.title {
  font-size: 18px;
  font-weight: 700;
}
.desc {
  margin-top: 8px;
  color: #666;
  font-size: 14px;
}
</style>