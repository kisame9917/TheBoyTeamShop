import { defineStore } from 'pinia'

const TOKEN_KEY = 'vest_token'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: null,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
  },
  actions: {
    async login({ username }) {
      // Base only: no real auth yet.
      this.token = `dummy-token-${username}`
      this.user = { username }
      localStorage.setItem(TOKEN_KEY, this.token)
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
    },
  },
})
