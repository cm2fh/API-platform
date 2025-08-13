<template>
  <div class="register-container">
    <a-form
      :model="formState"
      name="register"
      class="register-form"
      @finish="onFinish"
    >
      <a-form-item
        name="userAccount"
        :rules="[{ required: true, message: '请输入账户!' }]"
      >
        <a-input v-model:value="formState.userAccount" placeholder="账户" />
      </a-form-item>

      <a-form-item
        name="userPassword"
        :rules="[{ required: true, message: '请输入密码!' }]"
        has-feedback
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="密码" />
      </a-form-item>

      <a-form-item
        name="checkPassword"
        :dependencies="['userPassword']"
        :rules="validateRules"
        has-feedback
      >
        <a-input-password v-model:value="formState.checkPassword" placeholder="确认密码" />
      </a-form-item>

      <a-form-item>
        <a-button type="primary" html-type="submit" class="register-form-button" :loading="loading">
          注册
        </a-button>
        <router-link to="/user/login" style="float: right">使用已有账户登录</router-link>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { userService } from '@/api';
import { message } from 'ant-design-vue';

interface FormState {
  userAccount: string;
  userPassword: string;
  checkPassword: string;
}

type ValidatorRule = {
  required: boolean;
  message: string;
} | {
  validator: (rule: unknown, value: string) => Promise<void>;
};

const router = useRouter();
const formState = reactive<FormState>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
});
const loading = ref(false);

const validateRules = [
  { required: true, message: '请确认密码!' },
  ({ getFieldValue }: { getFieldValue: (field: string) => string }) => ({
    validator(_: unknown, value: string) {
      if (!value || getFieldValue('userPassword') === value) {
        return Promise.resolve();
      }
      return Promise.reject(new Error('两次输入的密码不匹配!'));
    },
  }),
] as ValidatorRule[];

const onFinish = async (values: FormState) => {
  loading.value = true;
  try {
    await userService.register(values);
    message.success('注册成功，请登录');
    await router.push('/user/login');
  } catch (error: any) {
    message.error('注册失败: ' + (error.message || '请重试'));
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.register-form {
  max-width: 300px;
  margin: auto;
}
.register-form-button {
  width: 100%;
}
</style> 