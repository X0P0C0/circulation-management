import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi, getUserInfo } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    username: '',
    realName: '',
    userId: null,
    role: null,
    roleName: ''
  }),
  getters: {
    isAdmin: (state) => state.role === 1
  },
  actions: {
    async login(loginForm) {
      const { data } = await loginApi(loginForm)
      this.token = data.token
      this.username = data.username
      this.realName = data.realName
      this.userId = data.userId
      this.role = data.role
      this.roleName = data.roleName || ''
      setToken(data.token)
    },
    async fetchUserInfo() {
      try {
        const { data } = await getUserInfo()
        this.userId = data.userId
        this.username = data.username
        this.realName = data.realName || data.username
        this.role = data.role
        this.roleName = data.roleName || ''
      } catch (e) {
        this.logout()
      }
    },
    async logout() {
      try { await logoutApi() } catch (e) { /* ignore */ }
      this.token = ''
      this.username = ''
      this.realName = ''
      this.userId = null
      this.role = null
      this.roleName = ''
      removeToken()
    }
  }
})