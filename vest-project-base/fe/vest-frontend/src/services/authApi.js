// src/api/authApi.js
import http from '@/api/http' // đường dẫn tuỳ bạn

export const authApi = {
  login: async (taiKhoan, matKhau) => {
    const data = await http.post('/api/auth/login', { taiKhoan, matKhau })
    // data = { token, role }
    localStorage.setItem('token', data.token)
    localStorage.setItem('role', data.role) // ADMIN / STAFF
    localStorage.setItem('username', taiKhoan)
    return data
  },
  logout: () => {
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    localStorage.removeItem('username')
  },
  role: () => localStorage.getItem('role'),
  isLoggedIn: () => !!localStorage.getItem('token')
}
