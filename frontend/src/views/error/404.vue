<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-icon">
        <a-result
          status="404"
          title="404"
          sub-title="抱歉，您访问的页面不存在"
        >
          <template #extra>
            <a-space>
              <a-button type="primary" @click="goHome">
                返回首页
              </a-button>
              <a-button @click="goBack">
                返回上页
              </a-button>
            </a-space>
          </template>
        </a-result>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.go(-1)
}
</script>

<style scoped>
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-color-secondary);
}

.error-content {
  text-align: center;
  padding: var(--spacing-xxxl);
}
</style>
