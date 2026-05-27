import { defineStore } from 'pinia'
import { login as loginApi } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'
import request from '@/utils/request'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    username: '',
    realName: '',
    role: null,
    roleName: ''
  }),
  getters: {
    isAdmin: (state) => state.role === 1
  },
  actions: {
    async login(form) {
      const { data } = await loginApi(form)
      this.token = data.token
      this.username = data.username
      this.realName = data.realName
      this.role = data.role
      this.roleName = data.roleName || ''
      setToken(data.token)
    },
    async fetchUserInfo() {
      try {
        const { data } = await request.get('/api/auth/info')
        this.username = data.username
        this.realName = data.realName || data.username
        this.role = data.role
        this.roleName = data.roleName || ''
      } catch (e) {
        this.logout()
      }
    },
    logout() {
      this.token = ''
      this.username = ''
      this.realName = ''
      this.role = null
      this.roleName = ''
      removeToken()
    }
  }
})