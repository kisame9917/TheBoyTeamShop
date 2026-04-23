import axios from 'axios';

const api = axios.create({
  baseURL: (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').trim(),
  timeout: 15000,
});

export async function getPublicVouchers() {
  const res = await api.get('/api/pgg/pos');
  return res.data;
}

export async function getMyVouchers(khachHangId) {
  const res = await api.get('/api/pgg/pos', {
    params: khachHangId ? { khachHangId } : {},
  });
  return res.data;
}

export default {
  getPublicVouchers,
  getMyVouchers,
};