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
}

onMounted(async () => {
  const token = route.query.token;

  if (!token) {
    await router.replace("/login?error=google_login_failed");
    return;
  }

  // lưu token
  clearAuthStorage();
  localStorage.setItem("USER_ACCESS_TOKEN", token);

  try {
    // gọi backend lấy thông tin user để hiển thị tên
    const res = await axios.get(`${BASE_URL}/api/client/auth/me`, {
      headers: { Authorization: `Bearer ${token}` },
    });

    const ten = res?.data?.tenKhachHang || "";
    localStorage.setItem("USER_NAME", ten);
  } catch (e) {
    // nếu fail thì vẫn cho vào app, chỉ là header sẽ rơi về mặc định
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