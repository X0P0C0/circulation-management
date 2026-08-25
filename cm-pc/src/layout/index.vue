<template>
  <div class="app-layout">
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="sidebar-logo">
        <div class="logo-icon">
          <el-icon :size="24" color="#fff"><Box /></el-icon>
        </div>
        <transition name="fade">
          <span v-show="!isCollapsed" class="logo-text">配件流转管理</span>
        </transition>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        :collapse-transition="false"
        router
        class="sidebar-menu"
        :default-openeds="defaultOpeneds"
      >
        <template v-for="group in menuGroups" :key="group.name">
          <el-sub-menu v-if="group.children.length > 1" :index="'group-' + group.name">
            <template #title>
              <el-icon><component :is="group.icon" /></el-icon>
              <span>{{ group.name }}</span>
            </template>
            <el-menu-item v-for="route in group.children" :key="route.path" :index="'/' + route.path">
              <el-icon><component :is="route.meta?.icon" /></el-icon>
              <template #title>{{ route.meta?.title }}</template>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="'/' + group.children[0].path">
            <el-icon><component :is="group.children[0].meta?.icon" /></el-icon>
            <template #title>{{ group.children[0].meta?.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>
      <div class="sidebar-footer">
        <el-icon :size="14"><InfoFilled /></el-icon>
        <span v-show="!isCollapsed">v1.0.30</span>
      </div>
    </aside>

    <div class="main-container">
      <header class="navbar">
        <div class="navbar-left">
          <el-icon class="collapse-btn" @click="isCollapsed = !isCollapsed">
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute?.meta?.title">
              {{ currentRoute.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="navbar-right">
          <el-tag v-if="userStore.role === 1" size="small" effect="dark" round type="danger">
            <el-icon><UserFilled /></el-icon> 管理员
          </el-tag>
          <el-tag v-else size="small" effect="dark" round type="info">
            <el-icon><User /></el-icon> 操作员
          </el-tag>
          <el-divider direction="vertical" />
          <span class="username">{{ userStore.realName || userStore.username }}</span>
          <el-dropdown @command="handleCommand" trigger="click">
            <el-avatar :size="32" class="user-avatar">
              {{ (userStore.realName || userStore.username || '').charAt(0) }}
            </el-avatar>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon> 修改密码
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <keep-alive>
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </main>
    </div>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="pwdDialogVisible" title="修改密码" width="420px" :close-on-click-modal="false">
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码">
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码">
            <template #prefix><el-icon><Key /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码">
            <template #prefix><el-icon><Key /></el-icon></template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="pwdLoading" @click="handleChangePassword">确认修改</el-button>
      </template>
    </el-dialog>
    <DevDesignTool />
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import { changePassword } from '@/api/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import DevDesignTool from '@/components/DevDesignTool.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapsed = ref(false)

const activeMenu = computed(() => route.path)
const currentRoute = computed(() => route)

const groupConfig = {
  '概览': { icon: 'HomeFilled', order: 0 },
  '业务操作': { icon: 'Operation', order: 1 },
  '库存管理': { icon: 'Box', order: 2 },
  '基础数据': { icon: 'Setting', order: 3 },
  '系统管理': { icon: 'Tools', order: 4 }
}

const defaultOpeneds = computed(() => menuGroups.value.map(g => 'group-' + g.name))

const menuGroups = computed(() => {
  const mainRoute = router.options.routes.find(r => r.path === '/')
  if (!mainRoute) return []
  let children = mainRoute.children.filter(r => !r.meta?.hidden)
  if (userStore.role !== 1) {
    children = children.filter(r => !r.meta?.adminOnly)
  }
  const groups = {}
  children.forEach(r => {
    const groupName = r.meta?.group || '其他'
    if (!groups[groupName]) groups[groupName] = []
    groups[groupName].push(r)
  })
  return Object.entries(groups)
    .map(([name, items]) => ({
      name,
      icon: groupConfig[name]?.icon || 'Menu',
      order: groupConfig[name]?.order ?? 99,
      children: items
    }))
    .sort((a, b) => a.order - b.order)
})

const handleCommand = (cmd) => {
  if (cmd === 'logout') handleLogout()
  else if (cmd === 'password') openPasswordDialog()
}

const handleLogout = async () => {
  await ElMessageBox.confirm('确定退出登录？', '提示', { type: 'warning' })
  await userStore.logout()
  router.push('/login')
}

const pwdDialogVisible = ref(false)
const pwdFormRef = ref()
const pwdLoading = ref(false)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: (rule, value, callback) => {
      if (value !== pwdForm.newPassword) callback(new Error('两次输入的密码不一致'))
      else callback()
    }, trigger: 'blur' }
  ]
}

const openPasswordDialog = () => {
  Object.assign(pwdForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
  pwdDialogVisible.value = true
}

const handleChangePassword = async () => {
  await pwdFormRef.value.validate()
  pwdLoading.value = true
  try {
    await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    pwdDialogVisible.value = false
    await userStore.logout()
    router.push('/login')
  } catch (e) { /* handled */ } finally { pwdLoading.value = false }
}
</script>

<style lang="scss" scoped>
.app-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* ---- 侧边栏 ---- */
.sidebar {
  width: 220px;
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  display: flex;
  flex-direction: column;

  &.collapsed {
    width: 64px;
  }
}

.sidebar-logo {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;

  .logo-icon {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    background: linear-gradient(135deg, #4361ee 0%, #6366f1 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .logo-text {
    font-size: 15px;
    font-weight: 700;
    color: #f1f5f9;
    white-space: nowrap;
    letter-spacing: 1px;
  }
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  overflow-y: auto;
  background: transparent;
  padding: 8px;

  :deep(.el-menu-item) {
    color: #94a3b8;
    border-radius: 8px;
    margin-bottom: 2px;
    height: 44px;
    line-height: 44px;
    transition: all 0.2s;

    .el-icon {
      font-size: 18px;
    }

    &:hover {
      background: rgba(255, 255, 255, 0.06);
      color: #e2e8f0;
    }

    &.is-active {
      background: linear-gradient(135deg, #4361ee 0%, #6366f1 100%);
      color: #ffffff;
      font-weight: 600;
      box-shadow: 0 2px 8px rgba(67, 97, 238, 0.35);
    }
  }

  :deep(.el-sub-menu) {
    margin-bottom: 2px;

    .el-sub-menu__title {
      color: #94a3b8;
      border-radius: 8px;
      height: 44px;
      line-height: 44px;
      transition: all 0.2s;

      .el-icon {
        font-size: 18px;
      }

      &:hover {
        background: rgba(255, 255, 255, 0.06);
        color: #e2e8f0;
      }
    }

    .el-sub-menu__icon-arrow {
      color: #64748b;
    }

    .el-menu {
      background: transparent;
      padding: 0;
    }

    .el-menu .el-menu-item {
      padding-left: 52px;
      height: 40px;
      line-height: 40px;
    }
  }

  &::-webkit-scrollbar {
    width: 4px;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.1);
    border-radius: 2px;
  }
}

.sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  color: #475569;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

/* ---- 主内容区 ---- */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--bg-page);
}

.navbar {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: var(--text-secondary);
  padding: 4px;
  border-radius: 6px;
  transition: var(--transition);

  &:hover {
    color: var(--primary);
    background: var(--primary-bg);
  }
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.username {
  font-size: 14px;
  color: var(--text-regular);
  font-weight: 500;
}

.user-avatar {
  cursor: pointer;
  background: linear-gradient(135deg, #4361ee 0%, #6366f1 100%);
  color: #fff;
  font-weight: 600;
  font-size: 14px;
  transition: var(--transition);

  &:hover {
    transform: scale(1.08);
    box-shadow: 0 2px 8px rgba(67, 97, 238, 0.4);
  }
}

.app-main {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

/* ---- 过渡动画 ---- */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.2s ease;
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-8px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(8px);
}
</style>
