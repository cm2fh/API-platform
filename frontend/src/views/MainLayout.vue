<template>
  <a-layout style="min-height: 100vh">
    <a-layout-header class="header">
      <div class="logo-container">
        <img src="/logo.svg" class="logo" alt="logo" />
        <span class="title">API 开放平台</span>
      </div>
      <a-menu
        theme="dark"
        mode="horizontal"
        class="header-menu"
        v-model:selectedKeys="selectedKeys"
      >
        <a-menu-item key="home" @click="router.push('/')">接口市场</a-menu-item>
        <a-menu-item v-if="AuthUtils.isAdmin(userStore.currentUser)" key="admin" @click="router.push('/admin')">
          后台管理
        </a-menu-item>
      </a-menu>
      <div class="user-info">
        <a-dropdown>
          <a class="ant-dropdown-link" @click.prevent>
            <span class="username">{{ userStore.currentUser?.userName || '用户' }}</span>
            <DownOutlined style="font-size: 12px; margin-left: 10px" />
          </a>
          <template #overlay>
            <a-menu>
              <a-menu-item key="profile" @click="router.push('/profile')">
                <UserOutlined />
                <span style="margin-left: 10px">个人中心</span>
              </a-menu-item>
              <a-menu-divider />
              <a-menu-item key="logout" @click="handleLogout">
                <LogoutOutlined />
                <span style="margin-left: 10px">退出登录</span>
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>
    </a-layout-header>
    <a-layout-content class="main-content">
      <div class="content-container">
        <router-view></router-view>
      </div>
    </a-layout-content>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { RouterView, useRouter, useRoute } from 'vue-router';
import { useUserStore } from '@/store/user';
import { AuthUtils } from '@/utils/auth';
import { message } from 'ant-design-vue';
import { UserOutlined, DownOutlined, LogoutOutlined } from '@ant-design/icons-vue';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const selectedKeys = ref<string[]>(['home']);

watch(
  () => route.name,
  (newName) => {
    if (newName === 'home' || newName === 'interface-detail') {
      selectedKeys.value = ['home'];
    } else if (newName === 'profile') {
      selectedKeys.value = ['profile'];
    } else if (newName === 'admin') {
      selectedKeys.value = ['admin'];
    }
  },
  { immediate: true }
);

const handleLogout = async () => {
  await userStore.logout();
  message.success('退出成功');
  await router.push('/user/login');
};
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 var(--spacing-xxl);
  box-shadow: var(--shadow-2);
  position: sticky;
  top: 0;
  z-index: var(--z-index-affix);
}

.header-menu {
  line-height: var(--layout-header-height);
  flex: 1;
  min-width: 520px;
}

.logo-container {
  display: flex;
  align-items: center;
  color: var(--text-color-inverse);
  margin-right: var(--spacing-xxxl);
}

.logo {
  height: 50px;
  margin-right: var(--spacing-md);
}

.title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  white-space: nowrap;
}

.user-info {
  display: flex;
  align-items: center;
  color: var(--text-color-inverse);
  margin-left: var(--spacing-lg);
}

.username {
  color: var(--text-color-inverse);
  font-size: var(--font-size-base);
}

.main-content {
  padding: var(--spacing-xxl);
  background-color: var(--bg-color-secondary);
}

.content-container {
  background: var(--bg-color-container);
  padding: var(--spacing-xxl);
  min-height: calc(100vh - var(--layout-header-height) - 69px);
  border-radius: var(--border-radius-sm);
  box-shadow: var(--shadow-1);
}

.footer-links a {
  color: var(--text-color-tertiary);
  transition: color var(--motion-duration-mid) var(--motion-ease-out);
}

.footer-links a:hover {
  color: var(--primary-color);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header {
    padding: 0 var(--spacing-lg);
  }

  .logo-container {
    margin-right: var(--spacing-lg);
  }

  .title {
    display: none;
  }

  .header-menu {
    min-width: auto;
  }

  .main-content {
    padding: var(--spacing-lg);
  }

  .content-container {
    padding: var(--spacing-lg);
  }
}
</style>
