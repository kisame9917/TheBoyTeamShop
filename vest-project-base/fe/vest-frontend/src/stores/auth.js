import { defineStore } from 'pinia'
import http from '../services/http'

const TOKEN_KEY = 'vest_token'
const ROLE_KEY = 'vest_role'
const USER_KEY = 'vest_user'

function safeParse(json) {
  try { return JSON.parse(json) } catch { return null }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    role: localStorage.getItem(ROLE_KEY) || '',
    user: safeParse(localStorage.getItem(USER_KEY)) || null,
  }),

  getters: {
    isAuthenticated: (s) => !!s.token,
    isAdmin: (s) => s.role === 'ADMIN',
    isStaff: (s) => s.role === 'STAFF',
  },

  actions: {
    hydrate() {
      if (this.token) http.defaults.headers.common['Authorization'] = `Bearer ${this.token}`
      else delete http.defaults.headers.common['Authorization']
    },

    async login({ taiKhoan, matKhau }) {
      const res = await http.post('/api/auth/login', { taiKhoan, matKhau })
      const payload = res?.data ?? res

      const token = payload?.token
      const role = payload?.role

      if (!token || !role) throw new Error('Response login thiếu token/role')

      this.token = token
      this.role = role
      this.user = { taiKhoan, role }

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
