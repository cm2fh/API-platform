<template>
  <a-config-provider
    :theme="{
      token: {
        colorPrimary: '#1677ff',
      },
    }"
  >
    <div id="app">
      <router-view />
    </div>
  </a-config-provider>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<style scoped>
#app {
  height: 100vh;
  margin: 0;
  padding: 0;
}
</style> 