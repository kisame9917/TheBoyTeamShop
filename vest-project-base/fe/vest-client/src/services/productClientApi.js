import axios from 'axios';

const api = axios.create({
  baseURL: (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').trim(),
  timeout: 15000,
});

export async function getProducts(params = {}) {
  const res = await api.get('/api/san-pham', { params });
  return res.data;
}

export async function getProductById(productId) {
  const res = await api.get(`/api/san-pham/${productId}`);
  return res.data;
}

export async function getProductVariantsByProductId(productId) {
  const res = await api.get(`/api/san-pham-chi-tiet/by-product/${productId}`);
  return res.data;
}
export async function getMauSacList() {
  const res = await api.get("/api/mau-sac/list");
  return res.data;
}
export async function getGiaMaxDb() {
  const res = await api.get('/api/san-pham/gia-max');
  return res.data;
}

export async function getLoaiSanPhamList() {
  const res = await api.get('/api/loai-san-pham/list');
  return res.data;
}
export default {
  getProducts,
  getProductById,
  getProductVariantsByProductId,
  getMauSacList,
  getGiaMaxDb,
  getLoaiSanPhamList,
};