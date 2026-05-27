<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-circle bg-circle--1"></div>
      <div class="bg-circle bg-circle--2"></div>
    </div>
    <div class="login-content">
      <div class="login-header">
        <div class="logo-icon">
          <van-icon name="logistics" size="28" color="#fff" />
        </div>
        <h2>配件流转管理</h2>
        <p>内部管理平台</p>
      </div>
      <van-form @submit="handleLogin">
        <van-cell-group inset class="login-form">
          <van-field v-model="form.username" label="用户名" placeholder="请输入用户名" left-icon="manager-o"
            :rules="[{ required: true, message: '请输入用户名' }]" />
          <van-field v-model="form.password" type="password" label="密码" placeholder="请输入密码" left-icon="lock"
            :rules="[{ required: true, message: '请输入密码' }]" />
        </van-cell-group>
        <div style="margin: 24px 16px 12px">
          <van-button round block type="primary" native-type="submit" :loading="loading" class="login-btn">
            登 录
          </van-button>
        </div>
      </van-form>
      <div class="login-tip">默认账号：admin / admin123</div>
    </div>
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
    showToast.success('登录成功')
    router.push('/')
  } catch (e) { /* handled */ } finally { loading.value = false }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.4;
}

.bg-circle--1 {
  width: 250px;
  height: 250px;
  background: #4361ee;
  top: -50px;
  right: -50px;
}

.bg-circle--2 {
  width: 200px;
  height: 200px;
  background: #6366f1;
  bottom: 100px;
  left: -60px;
}

.login-content {
  position: relative;
  z-index: 1;
  padding: 80px 24px 40px;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-icon {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  background: linear-gradient(135deg, #4361ee 0%, #6366f1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  box-shadow: 0 8px 24px rgba(67, 97, 238, 0.4);
}

.login-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: #f1f5f9;
  margin: 0 0 6px;
}

.login-header p {
  font-size: 13px;
  color: #94a3b8;
  margin: 0;
}

.login-form {
  border-radius: 12px !important;
  overflow: hidden;
}

.login-btn {
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #4361ee 0%, #6366f1 100%);
  border: none;
}

.login-tip {
  text-align: center;
  font-size: 12px;
  color: #64748b;
  margin-top: 16px;
}
</style>