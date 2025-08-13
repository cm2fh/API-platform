<template>
  <div class="login-container">
    <a-form
      :model="formState"
      name="login"
      class="login-form"
      @finish="onFinish"
    >
      <a-form-item
        name="userAccount"
        :rules="[{ required: true, message: '请输入账户!' }]"
      >
        <a-input v-model:value="formState.userAccount" placeholder="账户">
          <template #prefix>
            <UserOutlined class="site-form-item-icon" />
          </template>
        </a-input>
      </a-form-item>

      <a-form-item
        name="userPassword"
        :rules="[{ required: true, message: '请输入密码!' }]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="密码">
          <template #prefix>
            <LockOutlined class="site-form-item-icon" />
          </template>
        </a-input-password>
      </a-form-item>

      <a-form-item>
        <a-button type="primary" html-type="submit" class="login-form-button" :loading="loading">
          登录
        </a-button>
        或
        <router-link to="/user/register">现在注册!</router-link>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';

const router = useRouter();
const userStore = useUserStore();
const formState = reactive({
  userAccount: '',
  userPassword: '',
});
const loading = ref(false);

const onFinish = async (values: any) => {
  loading.value = true;
  try {
    await userStore.login(values);
    message.success('登录成功');
    await router.push('/');
  } catch (error: any) {
    message.error('登录失败: ' + (error.message || '请检查您的账户和密码'));
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-form {
  max-width: 300px;
  margin: auto;
}
.login-form-button {
  width: 100%;
}
</style> 