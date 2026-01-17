import http from './http'

export async function listNhanVien(page = 0, size = 10) {
    const res = await http.get('/api/nhan-vien', { params: { page, size } })
    return res.data
}

export async function createNhanVien(payload) {
    const res = await http.post('/api/nhan-vien', payload)
    return res.data
}

export async function updateNhanVien(id, payload) {
    const res = await http.put(`/api/nhan-vien/${id}`, payload)
    return res.data
}

export async function softDeleteNhanVien(id) {
    const res = await http.delete(`/api/nhan-vien/${id}`)
    return res.data
}
