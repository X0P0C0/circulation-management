<template>
  <div>
    <van-nav-bar title="我的" />
    <van-cell-group inset style="margin-top: 12px">
      <van-cell title="用户名" :value="userStore.username" />
      <van-cell title="姓名" :value="userStore.realName" />
      <van-cell title="角色">
        <template #value>
          <van-tag :type="userStore.role === 1 ? 'danger' : 'primary'" size="small">
            {{ userStore.roleName }}
          </van-tag>
        </template>
      </van-cell>
    </van-cell-group>
    <van-cell-group inset style="margin-top: 12px" title="安全设置">
      <van-cell title="修改密码" is-link @click="showPwdDialog = true" />
    </van-cell-group>
    <div style="margin: 24px 16px">
      <van-button round block type="danger" @click="handleLogout">退出登录</van-button>
    </div>

    <van-dialog v-model:show="showPwdDialog" title="修改密码" show-cancel-button
      :before-close="onPwdDialogClose">
      <van-form ref="pwdFormRef">
        <van-cell-group inset>
          <van-field v-model="pwdForm.oldPassword" type="password" label="原密码"
            placeholder="请输入原密码" :rules="[{ required: true, message: '请输入原密码' }]" />
          <van-field v-model="pwdForm.newPassword" type="password" label="新密码"
            placeholder="不少于6位" :rules="[{ required: true, message: '请输入新密码' }]" />
        </van-cell-group>
      </van-form>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import { changePassword } from '@/api/auth'
import { showConfirmDialog, showToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()
const showPwdDialog = ref(false)
const pwdFormRef = ref()
const pwdForm = reactive({ oldPassword: '', newPassword: '' })

const handleLogout = async () => {
  await showConfirmDialog({ title: '提示', message: '确定退出登录？' })
  userStore.logout()
  router.push('/login')
}

const onPwdDialogClose = async (action) => {
  if (action === 'confirm') {
    if (!pwdForm.oldPassword || !pwdForm.newPassword) {
      showToast('请填写完整信息')
      return false
    }
    if (pwdForm.newPassword.length < 6) {
      showToast('新密码不少于6位')
      return false
    }
    try {
      await changePassword(pwdForm)
      showToast.success('密码已修改，请重新登录')
      showPwdDialog.value = false
      userStore.logout()
      router.push('/login')
    } catch (e) {
      return false
    }
  }
  return true
}
</script>