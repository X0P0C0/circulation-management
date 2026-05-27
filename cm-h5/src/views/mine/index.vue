<template>
  <div>
    <van-nav-bar title="My" />
    <van-cell-group inset style="margin-top: 12px">
      <van-cell title="Username" :value="userStore.username" />
      <van-cell title="Name" :value="userStore.realName" />
    </van-cell-group>
    <van-cell-group inset style="margin-top: 12px" title="Security">
      <van-cell title="Change Password" is-link @click="showPwdDialog = true" />
    </van-cell-group>
    <div style="margin: 24px 16px">
      <van-button round block type="danger" @click="handleLogout">Logout</van-button>
    </div>

    <van-dialog v-model:show="showPwdDialog" title="Change Password" show-cancel-button
      :before-close="onPwdDialogClose">
      <van-form ref="pwdFormRef">
        <van-cell-group inset>
          <van-field v-model="pwdForm.oldPassword" type="password" label="Old Password"
            placeholder="Enter old password" :rules="[{ required: true, message: 'Required' }]" />
          <van-field v-model="pwdForm.newPassword" type="password" label="New Password"
            placeholder="Min 6 chars" :rules="[{ required: true, message: 'Required' }]" />
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
  await showConfirmDialog({ title: 'Confirm', message: 'Logout?' })
  userStore.logout()
  router.push('/login')
}

const onPwdDialogClose = async (action) => {
  if (action === 'confirm') {
    if (!pwdForm.oldPassword || !pwdForm.newPassword) {
      showToast('Please fill all fields')
      return false
    }
    if (pwdForm.newPassword.length < 6) {
      showToast('Min 6 chars')
      return false
    }
    try {
      await changePassword(pwdForm)
      showToast.success('Changed, login again')
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