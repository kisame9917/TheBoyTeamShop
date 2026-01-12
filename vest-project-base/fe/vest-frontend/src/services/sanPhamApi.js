import http from './http'

export async function listSanPham() {
  const res = await http.get('/api/san-pham')
  return res.data?.data ?? []
}

export async function createSanPham(payload) {
  const res = await http.post('/api/san-pham', payload)
  return res.data?.data
}
