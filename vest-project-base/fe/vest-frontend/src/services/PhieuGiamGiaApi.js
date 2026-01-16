import axios from 'axios'

const API = 'http://localhost:8080/api/pgg'

// ===== LIST =====
export const getAllPhieuGiamGia = async () => {
  const res = await axios.get(API)
  return res.data
}

// ===== DETAIL =====
export const getDetailPhieuGiamGia = async (id) => {
  const res = await axios.get(`${API}/${id}`)
  return res.data
}

// ===== CREATE =====
export const createPhieuGiamGia = async (data) => {
  const res = await axios.post(`${API}/create`, data)
  return res.data
}

// ===== UPDATE =====
export const updatePhieuGiamGia = async (id, data) => {
  const res = await axios.put(`${API}/update/${id}`, data)
  return res.data
}

// ===== SOFT DELETE =====
export const softDeletePhieuGiamGia = async (id) => {
  const res = await axios.put(`${API}/update/${id}`, {
    trangThai: false
  })
  return res.data
}
