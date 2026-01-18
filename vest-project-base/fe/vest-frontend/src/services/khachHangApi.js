import http from './http'

export async function listKhachHang(page = 0, size = 10) {
    const res = await http.get('/api/khach-hang', { params: { page, size } })
    return res.data
}

export async function createKhachHang(payload) {
    const res = await http.post('/api/khach-hang', payload)
    return res.data
}

export async function updateKhachHang(id, payload) {
    const res = await http.put(`/api/khach-hang/${id}`, payload)
    return res.data
}

// xóa mềm: BE của bạn đang xử lý soft-delete ở DELETE
export async function softDeleteKhachHang(id) {
    const res = await http.delete(`/api/khach-hang/${id}`)
    return res.data
}

