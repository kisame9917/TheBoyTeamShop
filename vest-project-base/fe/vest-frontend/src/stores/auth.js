import { defineStore } from 'pinia'
import http from '../services/http'

const TOKEN_KEY = 'vest_token'
const USER_KEY = 'vest_user'

function safeParse(json) {
  try {
    return JSON.parse(json)
  } catch {
    return null
  }
}

function pickRoleIdFromUser(u) {
  if (!u) return null

  const id = Number(u.quyenHanId ?? u.quyenHan?.id ?? u.roleId ?? 0)
  if (id) return id

  const name = String(u.tenQuyenHan ?? u.quyenHan?.tenQuyenHan ?? '').toUpperCase()
  if (name.includes('ADMIN')) return 1
  if (name) return 2
  return null
}

/* ------------------- DEV DEFAULT USER (MẶC ĐỊNH ADMIN) ------------------- */
const DEV_DEFAULT_ROLE_ID = 1 // 1=ADMIN, 2=NHÂN_VIÊN (đổi số này nếu muốn mặc định staff)
const DEV_ADMIN_USER = {
  id: 1,
  username: 'admin',
  taiKhoan: 'admin',
  quyenHanId: 1,
  tenQuyenHan: 'ADMIN',
}
const DEV_STAFF_USER = {
  id: 2,
  username: 'staff',
  taiKhoan: 'staff',
  quyenHanId: 2,
  tenQuyenHan: 'NHÂN VIÊN',
}

function getDevDefaultUser() {
  return DEV_DEFAULT_ROLE_ID === 1 ? DEV_ADMIN_USER : DEV_STAFF_USER
}
/* ------------------------------------------------------------------------ */

export const useAuthStore = defineStore('auth', {
  state: () => {
    const token = localStorage.getItem(TOKEN_KEY) || ''
    const user = safeParse(localStorage.getItem(USER_KEY)) || null

    /* ------------------- AUTO LOGIN DEV (NẾU CHƯA LOGIN) ------------------- */
    // Nếu chưa có token/user trong localStorage => tự set theo DEV_DEFAULT_ROLE_ID
    if (!token || !user) {
      const devUser = getDevDefaultUser()
      const devToken = `dummy-token-${devUser.taiKhoan}`

      localStorage.setItem(TOKEN_KEY, devToken)
      localStorage.setItem(USER_KEY, JSON.stringify(devUser))

      // set header role cho BE
      http.defaults.headers.common['X-ROLE-ID'] = String(devUser.quyenHanId)

      return {
        token: devToken,
        user: devUser,
      }
    }
    /* ---------------------------------------------------------------------- */

    return { token, user }
  },

  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    isAdmin: (state) => pickRoleIdFromUser(state.user) === 1,
    isStaff: (state) => pickRoleIdFromUser(state.user) === 2,
    roleId: (state) => pickRoleIdFromUser(state.user),
  },

  actions: {
    hydrate() {
      const roleId = this.roleId
      if (roleId) http.defaults.headers.common['X-ROLE-ID'] = String(roleId)
      else delete http.defaults.headers.common['X-ROLE-ID']
    },

    /* ------------------- DEV DEMO: CHUYỂN ROLE NHANH ------------------- */
    devLoginAsAdmin() {
      const u = DEV_ADMIN_USER
      const t = `dummy-token-${u.taiKhoan}`
      this.token = t
      this.user = u
      localStorage.setItem(TOKEN_KEY, t)
      localStorage.setItem(USER_KEY, JSON.stringify(u))
      http.defaults.headers.common['X-ROLE-ID'] = String(u.quyenHanId)
    },

    devLoginAsStaff() {
      const u = DEV_STAFF_USER
      const t = `dummy-token-${u.taiKhoan}`
      this.token = t
      this.user = u
      localStorage.setItem(TOKEN_KEY, t)
      localStorage.setItem(USER_KEY, JSON.stringify(u))
      http.defaults.headers.common['X-ROLE-ID'] = String(u.quyenHanId)
    },
    /* ------------------------------------------------------------------- */

    async login({ username }) {
      const res = await http.get('/api/nhan-vien')
      const arr = Array.isArray(res.data) ? res.data : (res.data?.result || res.data?.content || [])
      const found = (arr || []).find(x => String(x?.taiKhoan || '').trim() === String(username || '').trim())

      if (!found) {
        throw new Error('Không tìm thấy nhân viên theo tài khoản đã nhập')
      }

      const roleId = pickRoleIdFromUser(found)
      if (!roleId) {
        throw new Error('Nhân viên không có quyền hạn (quyenHanId) hợp lệ')
      }

      this.token = `dummy-token-${username}`
      this.user = {
        id: found.id,
        username: found.taiKhoan,
        taiKhoan: found.taiKhoan,
        quyenHanId: found.quyenHanId ?? found.quyenHan?.id ?? roleId,
        tenQuyenHan: found.tenQuyenHan ?? found.quyenHan?.tenQuyenHan ?? '',
      }

      localStorage.setItem(TOKEN_KEY, this.token)
      localStorage.setItem(USER_KEY, JSON.stringify(this.user))
      http.defaults.headers.common['X-ROLE-ID'] = String(roleId)
    },

    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
      delete http.defaults.headers.common['X-ROLE-ID']
    },
  },
})
