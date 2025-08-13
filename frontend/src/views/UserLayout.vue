<template>
  <div class="user-layout-container">
    <div class="user-layout-content">
      <div class="top">
        <div class="header">
          <a href="/">
            <img src="/logo.svg" class="logo" alt="logo">
            <span class="title">API 开放平台</span>
          </a>
        </div>
        <div class="desc">简单、快速、安全地调用第三方接口</div>
      </div>
      <div class="main">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<style scoped>
.user-layout-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-image: url('https://gw.alipayobjects.com/zos/rmsportal/TVYTbAXWheQpRcWDaDMu.svg');
  background-size: 100%;
  background-position: center 110px;
  background-repeat: no-repeat;
}

.user-layout-content {
  flex: 1;
  padding: 32px 0;
  max-width: 450px;
  min-height: 100%;
  display: flex;
  flex-direction: column;
}

.top {
  text-align: center;
  margin-bottom: 40px;
}

.header {
  height: 44px;
  line-height: 44px;
  margin-bottom: 16px;
}

.header a {
  text-decoration: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo {
  height: 50px;
  vertical-align: top;
  margin-right: 16px;
}

.title {
  font-size: 33px;
  color: rgba(0, 0, 0, .85);
  font-family: 'Avenir', 'Helvetica Neue', Arial, Helvetica, sans-serif;
  font-weight: 600;
  position: relative;
}

.desc {
  font-size: 14px;
  color: rgba(0, 0, 0, .45);
  margin-top: 12px;
}

.main {
  flex: 1;
  width: 368px;
  margin: 0 auto;
}

.links a {
  font-size: 14px;
  color: rgba(0, 0, 0, .45);
  margin: 0 8px;
  text-decoration: none;
}

.links a:hover {
  color: #1890ff;
}

@media (max-width: 768px) {
  .user-layout-content {
    max-width: 95%;
  }

  .main {
    width: 100%;
    padding: 0 16px;
  }

  .title {
    font-size: 28px;
  }
}
</style>
