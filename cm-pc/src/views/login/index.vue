<template>
  <div class="login-page">
    <!-- 装饰背景 -->
    <div class="bg-decoration">
      <div class="bg-circle bg-circle--1"></div>
      <div class="bg-circle bg-circle--2"></div>
      <div class="bg-circle bg-circle--3"></div>
    </div>

    <div class="login-card">
      <div class="login-header">
        <div class="logo-icon">
          <el-icon :size="32" color="#fff"><Box /></el-icon>
        </div>
        <h2>配件流转管理系统</h2>
        <p>内部管理平台 · 安全登录</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" class="login-input">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password
            class="login-input" @keyup.enter="handleLogin">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="login-btn" @click="handleLogin">
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <span>默认账号：admin / admin123</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) { /* handled */ } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #334155 100%);
  position: relative;
  overflow: hidden;
}

/* 装饰圆 */
.bg-decoration {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;

  &--1 {
    width: 400px;
    height: 400px;
    background: #4361ee;
    top: -100px;
    right: -100px;
    animation: float 8s ease-in-out infinite;
  }

  &--2 {
    width: 300px;
    height: 300px;
    background: #6366f1;
    bottom: -80px;
    left: -80px;
    animation: float 10s ease-in-out infinite reverse;
  }

  &--3 {
    width: 200px;
    height: 200px;
    background: #8b5cf6;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    animation: float 6s ease-in-out infinite;
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-30px) scale(1.05); }
}

/* 登录卡片 */
.login-card {
  width: 420px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 48px 40px 36px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(255, 255, 255, 0.1);
  z-index: 1;
  animation: cardIn 0.6s ease-out;
}

@keyframes cardIn {
  from { opacity: 0; transform: translateY(20px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.login-header {
  text-align: center;
  margin-bottom: 36px;

  .logo-icon {
    width: 64px;
    height: 64px;
    border-radius: 16px;
    background: linear-gradient(135deg, #4361ee 0%, #6366f1 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
    box-shadow: 0 8px 24px rgba(67, 97, 238, 0.4);
  }

  h2 {
    font-size: 22px;
    font-weight: 700;
    color: #1e293b;
    margin: 0 0 6px;
    letter-spacing: 1px;
  }

  p {
    font-size: 13px;
    color: #94a3b8;
    margin: 0;
  }
}

.login-form {
  :deep(.el-input__wrapper) {
    border-radius: 10px;
    padding: 4px 12px;
    box-shadow: 0 0 0 1px #e2e8f0 inset;
    background: #f8fafc;

    &:hover, &.is-focus {
      box-shadow: 0 0 0 1px #4361ee inset;
    }
  }

  :deep(.el-input__prefix) {
    color: #94a3b8;
  }
}

.login-btn {
  width: 100%;
  height: 46px;
  border-radius: 10px !important;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #4361ee 0%, #6366f1 100%);
  border: none;
  box-shadow: 0 4px 16px rgba(67, 97, 238, 0.4);
  transition: all 0.3s;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 20px rgba(67, 97, 238, 0.5);
  }
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 12px;
  color: #94a3b8;
}
</style>