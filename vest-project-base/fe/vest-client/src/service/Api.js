// src/services/clientApi.js
import axios from "axios";

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export const clientApi = axios.create({
  baseURL: BASE_URL,
  headers: { "Content-Type": "application/json" },
});

// Catalog - HOME
export function getHomeProducts(size = 10) {
  return clientApi.get("/api/client/home", { params: { size } });
}

// Catalog - SEARCH / SHOP
export function searchClientProducts(params = {}) {
  // params có thể gồm: q, loaiId, thuongHieuId, fitId, minPrice, maxPrice, page, size, sort...
  return clientApi.get("/api/client/products", { params });
}

// Catalog - PRODUCT DETAIL
export function getClientProductDetail(id) {
  return clientApi.get(`/api/client/products/${id}`);
}