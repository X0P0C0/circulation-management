import { defineStore } from 'pinia'
import { login as loginApi } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'
import request from '@/utils/request'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    username: '',
    realName: ''
  }),
  actions: {
    async login(form) {
      const { data } = await loginApi(form)
      this.token = data.token
      this.username = data.username
      this.realName = data.realName
      setToken(data.token)
    },
    async fetchUserInfo() {
      try {
        const { data } = await request.get('/api/auth/info')
        this.username = data.username
        this.realName = data.realName || data.username
      } catch (e) {
        this.logout()
      }
    },
    logout() {
      this.token = ''
      this.username = ''
      this.realName = ''
      removeToken()
    }
  }
})