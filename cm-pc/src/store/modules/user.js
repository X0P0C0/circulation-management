import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    username: '',
    realName: '',
    userId: null
  }),
  actions: {
    async login(loginForm) {
      const { data } = await loginApi(loginForm)
      this.token = data.token
      this.username = data.username
      this.realName = data.realName
      this.userId = data.userId
      setToken(data.token)
    },
    async logout() {
      try { await logoutApi() } catch (e) { /* ignore */ }
      this.token = ''
      this.username = ''
      this.realName = ''
      this.userId = null
      removeToken()
    }
  }
})