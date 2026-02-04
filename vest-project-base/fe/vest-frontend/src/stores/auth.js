import { defineStore } from 'pinia'
import http from '../services/http'

const TOKEN_KEY = 'vest_token'
const ROLE_KEY  = 'vest_role'
const USER_KEY  = 'vest_user'

function safeParse(json) {
  try { return JSON.parse(json) } catch { return null }
}

function normalizeToken(t) {
  if (!t) return ''
  const s = String(t).trim()
  if (s === 'null' || s === 'undefined') return ''
  return s
}

function normalizeRole(r) {
  if (!r) return ''
  const s = String(r).trim().toUpperCase()
  if (s === 'NULL' || s === 'UNDEFINED') return ''
  return s
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: normalizeToken(localStorage.getItem(TOKEN_KEY)),
    role: normalizeRole(localStorage.getItem(ROLE_KEY)),
    user: safeParse(localStorage.getItem(USER_KEY)) || null,
  }),

  getters: {
    isAuthenticated: (s) => !!normalizeToken(s.token),
    isAdmin: (s) => normalizeRole(s.role) === 'ADMIN',
    isStaff: (s) => normalizeRole(s.role) === 'STAFF',
    userId: (s) => s.user?.id || null,
  },

  actions: {
    hydrate() {
      const t = normalizeToken(this.token)
      if (t) http.defaults.headers.common['Authorization'] = `Bearer ${t}`
      else {
        // nếu token bẩn => dọn luôn cho sạch
        this.logout()
      }
    },

    async login({ taiKhoan, matKhau }) {
      const res = await http.post('/api/auth/login', { taiKhoan, matKhau })
      const payload = res?.data ?? res

      const token = normalizeToken(payload?.token)
      const role  = normalizeRole(payload?.role)

      if (!token || !role) throw new Error('Response login thiếu token/role')

      this.token = token
      this.role = role
      // this.user = { taiKhoan, role }
      this.user = {
        ...payload,       // Lấy hết các trường (id, tenNhanVien, email...)
        taiKhoan,         // Đảm bảo có tài khoản
        role              // Đảm bảo có role chuẩn
      }

      localStorage.setItem(TOKEN_KEY, token)
      localStorage.setItem(ROLE_KEY, role)
      localStorage.setItem(USER_KEY, JSON.stringify(this.user))

      http.defaults.headers.common['Authorization'] = `Bearer ${token}`
      return { token, role }
    },

    logout() {
      this.token = ''
      this.role = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(ROLE_KEY)
      localStorage.removeItem(USER_KEY)
      delete http.defaults.headers.common['Authorization']
    },
  },
})
