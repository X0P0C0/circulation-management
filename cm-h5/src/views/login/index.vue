<template>
  <div class="login-page">
    <div class="login-header">
      <h2>配件流转管理</h2>
      <p>管理员登录</p>
    </div>
    <van-form @submit="handleLogin">
      <van-cell-group inset>
        <van-field v-model="form.username" label="用户名" placeholder="请输入用户名"
          :rules="[{ required: true, message: '请输入用户名' }]" />
        <van-field v-model="form.password" type="password" label="密码" placeholder="请输入密码"
          :rules="[{ required: true, message: '请输入密码' }]" />
      </van-cell-group>
      <div style="margin: 16px">
        <van-button round block type="primary" native-type="submit" :loading="loading">登录</van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import { showToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const handleLogin = async () => {
  loading.value = true
  try {
    await userStore.login(form)
    showToast('登录成功')
    router.push('/')
  } catch (e) { /* handled */ } finally { loading.value = false }
}
</script>

<style scoped>
.login-page { min-height: 100vh; background: linear-gradient(135deg, #667eea, #764ba2); display: flex; flex-direction: column; align-items: center; justify-content: center; }
.login-header { text-align: center; color: #fff; margin-bottom: 32px; }
.login-header h2 { font-size: 24px; margin-bottom: 8px; }
.login-header p { font-size: 14px; opacity: 0.8; }
</style>