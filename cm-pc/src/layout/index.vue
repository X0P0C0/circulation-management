<template>
  <div class="app-layout">
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="sidebar-logo">
        <img src="" alt="" style="display:none" />
        <span v-show="!isCollapsed" class="logo-text">配件流转管理</span>
        <span v-show="isCollapsed" class="logo-text">CM</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        :collapse-transition="false"
        router
        class="sidebar-menu"
      >
        <template v-for="route in menuRoutes" :key="route.path">
          <el-menu-item :index="'/' + route.path">
            <el-icon><component :is="route.meta?.icon" /></el-icon>
            <template #title>{{ route.meta?.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>
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
          <span class="username">{{ userStore.realName || userStore.username }}</span>
          <el-button link @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            退出
          </el-button>
        </div>
      </header>

      <main class="app-main">
        <router-view v-slot="{ Component }">
          <keep-alive>
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapsed = ref(false)

const activeMenu = computed(() => route.path)
const currentRoute = computed(() => route)

const menuRoutes = computed(() => {
  const mainRoute = router.options.routes.find(r => r.path === '/')
  return mainRoute?.children?.filter(r => !r.meta?.hidden) || []
})

const handleLogout = async () => {
  await ElMessageBox.confirm('确认退出系统？', '提示', { type: 'warning' })
  await userStore.logout()
  router.push('/login')
}
</script>

<style scoped lang="scss">
.app-layout {
  display: flex;
  height: 100vh;
}

.sidebar {
  width: 220px;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  transition: width 0.3s;
  overflow: hidden;

  &.collapsed {
    width: 64px;
  }
}

.sidebar-logo {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #e4e7ed;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
}

.sidebar-menu {
  border-right: none;
  height: calc(100vh - 50px);
  overflow-y: auto;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.navbar {
  height: 50px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;

  &:hover {
    color: #409eff;
  }
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.username {
  font-size: 14px;
  color: #606266;
}

.app-main {
  flex: 1;
  overflow-y: auto;
  background: #f5f7fa;
}
</style>