
import axios from "axios";

const baseURL = import.meta.env.VITE_API_URL || "http://localhost:8080";

const http = axios.create({
  baseURL,
  timeout: 20000,
});

http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token"); // nếu bạn có login thì gắn
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  },
  (err) => Promise.reject(err)
);

export default http;
