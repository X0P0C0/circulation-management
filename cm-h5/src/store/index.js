import { defineStore } from 'pinia'
import { login as loginApi } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'

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
    logout() {
      this.token = ''
      this.username = ''
      this.realName = ''
      removeToken()
    }
  }
})