// src/api/authApi.js
import http from "@/api/http";

export const authApi = {
  login: async (taiKhoan, matKhau) => {
    const res = await http.post("/api/auth/login", { taiKhoan, matKhau });
    const data = res?.data ?? res; // { token, role, id, tenNhanVien, email }

    // ✅ lưu dạng object đầy đủ
    localStorage.setItem("vest_user", JSON.stringify(data));

    // ✅ vẫn giữ role/token riêng nếu bạn cần
    localStorage.setItem("vest_token", data.token || "");
    localStorage.setItem("vest_role", data.role || "");

    // (tuỳ chọn) key cũ
    localStorage.setItem("token", data.token || "");
    localStorage.setItem("role", data.role || "");
    localStorage.setItem("username", taiKhoan);

    return data;
  },

  logout: () => {
    localStorage.removeItem("vest_user");
    localStorage.removeItem("vest_token");
    localStorage.removeItem("vest_role");

    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("username");
  },

  role: () => localStorage.getItem("vest_role") || localStorage.getItem("role"),
  isLoggedIn: () => !!(localStorage.getItem("vest_token") || localStorage.getItem("token")),
};