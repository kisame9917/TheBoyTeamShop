
import axios from "axios";

const baseURL = import.meta.env.VITE_API_URL || "http://localhost:8080";

const http = axios.create({
  baseURL,
  timeout: 20000,
});

http.interceptors.request.use(
  (config) => {
    // ✅ đồng bộ với auth store (vest_token / vest_role)
    const token = localStorage.getItem("vest_token") || localStorage.getItem("token");
    if (token) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${token}`;
    }

    // ✅ backend dùng RoleId header (theo BE đã fix)
      const roleRaw = (localStorage.getItem("vest_role") || localStorage.getItem("role") || "").toUpperCase();

// map chuỗi role -> roleId mà BE đang check
      const roleId =
          roleRaw === "ADMIN" ? "1" :
              roleRaw === "STAFF" ? "2" :
                  roleRaw; // fallback nếu BE trả sẵn số

      if (roleId) {
          config.headers = config.headers || {};
          config.headers["X-ROLE-ID"] = roleId;
      }
    return config;
  },
  (err) => Promise.reject(err)
);

export default http;
