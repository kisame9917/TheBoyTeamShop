import http from './http'

export async function listSanPham(page = 0, size = 10) {
  const res = await http.get('/api/san-pham', {
    params: { page, size }
  })
  return res.data // Return the whole Page object (content, totalPages, etc.)
}

export async function createSanPham(payload) {
  const res = await http.post('/api/san-pham', payload)
  return res.data?.data
}
