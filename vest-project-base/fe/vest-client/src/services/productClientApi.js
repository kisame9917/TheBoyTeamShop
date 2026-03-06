import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
  timeout: 15000,
});

export async function getProducts(params = {}) {
  const res = await api.get("/san-pham", { params });
  return res.data;
}

export async function getProductById(productId) {
  const res = await api.get(`/san-pham/${productId}`);
  return res.data;
}

export async function getProductVariantsByProductId(productId) {
  const res = await api.get(`/san-pham-chi-tiet/by-product/${productId}`);
  return res.data;
}

export async function getGiaMaxDb() {
  const res = await api.get("/san-pham/gia-max");
  return res.data;
}

export async function getLoaiSanPhamList() {
  const res = await api.get("/loai-san-pham/list");
  return res.data;
}

export default {
  getProducts,
  getProductById,
  getProductVariantsByProductId,
  getGiaMaxDb,
  getLoaiSanPhamList,
};