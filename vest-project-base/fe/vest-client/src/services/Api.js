import axios from "axios";

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export const clientApi = axios.create({
  baseURL: BASE_URL,
  headers: { "Content-Type": "application/json" },
});

clientApi.interceptors.request.use((config) => {
  const token =
    localStorage.getItem("USER_ACCESS_TOKEN") ||
    sessionStorage.getItem("USER_ACCESS_TOKEN") ||
    localStorage.getItem("vest_token");

  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

export function getHomeProducts(size = 10) {
  return clientApi.get("/api/client/home", { params: { size } });
}

export function searchClientProducts(params = {}) {
  return clientApi.get("/api/client/products", { params });
}

export function getClientProductDetail(id) {
  return clientApi.get(`/api/client/products/${id}`);
}

export function getClientProfile() {
  return clientApi.get("/api/client/auth/me");
}

export function updateClientProfile(payload) {
  return clientApi.put("/api/client/auth/me", payload);
}

export function getMyOrders() {
  return clientApi.get("/api/client/orders/my");
}

export function getMyOrderDetail(id) {
  return clientApi.get(`/api/client/orders/my/${id}`);
}

export async function cancelMyOrder(orderId, payload) {
  const token =
    localStorage.getItem("USER_ACCESS_TOKEN") ||
    sessionStorage.getItem("USER_ACCESS_TOKEN") ||
    localStorage.getItem("vest_token");

  const res = await fetch(`http://localhost:8080/api/client/orders/my/${orderId}/cancel`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify(payload || {}),
  });

  const data = await res.json().catch(() => ({}));

  if (!res.ok) {
    throw new Error(data?.message || "Hủy đơn thất bại");
  }

  return data;
}

export function updateMyOrderShipping(orderId, payload) {
  return clientApi.patch(`/api/client/orders/my/${orderId}/shipping-info`, payload);
}

export function updateMyOrderItems(orderId, payload) {
  return clientApi.patch(`/api/client/orders/my/${orderId}/items`, payload);
}
