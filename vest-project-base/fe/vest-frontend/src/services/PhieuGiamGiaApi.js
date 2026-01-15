import axios from 'axios'

const API = 'http://localhost:8080/api/pgg'

export const getAllPhieuGiamGia = async () => {
  const res = await axios.get(API)
  return res.data
}

export const createPhieuGiamGia = async (data) => {
  const res = await axios.post(`${API}/create`, data)
  return res.data
}

export const updatePhieuGiamGia = async (id, data) => {
  const res = await axios.put(`${API}/update/${id}`, data)
  return res.data
}

// xóa mềm = set trangThai = false
export const softDeletePhieuGiamGia = async (id) => {
  const res = await axios.put(`${API}/${id}/delete`)
  return res.data
}

