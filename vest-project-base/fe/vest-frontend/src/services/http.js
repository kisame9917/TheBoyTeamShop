import axios from "axios";

// Đổi đúng port BE của bạn (ví dụ Spring boot 8080)
const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080",
  timeout: 30000,
});

// Gắn token nếu bạn có đăng nhập (localStorage key tuỳ bạn)
http.interceptors.request.use((config) => {
  const token =
    localStorage.getItem("token") ||
    localStorage.getItem("access_token") ||
    "";
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Bắt lỗi để OrdersList show được error.value đẹp
http.interceptors.response.use(
  (res) => res,
  (err) => Promise.reject(err)
);

export default http;
