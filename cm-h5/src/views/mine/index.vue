<template>
  <div>
    <van-nav-bar title="我的" />
    <van-cell-group inset style="margin-top: 12px">
      <van-cell title="用户名" :value="userStore.username" />
      <van-cell title="姓名" :value="userStore.realName" />
    </van-cell-group>
    <div style="margin: 24px 16px">
      <van-button round block type="danger" @click="handleLogout">退出登录</van-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import { showConfirmDialog } from 'vant'

const router = useRouter()
const userStore = useUserStore()

const handleLogout = async () => {
  await showConfirmDialog({ title: '提示', message: '确认退出登录？' })
  userStore.logout()
  router.push('/login')
}
</script>