<template>
  <a-form
    ref="formRef"
    :model="formState"
    layout="vertical"
    @finish="onFinish"
  >
    <a-form-item name="name" label="接口名称" :rules="[{ required: true }]">
      <a-input v-model:value="formState.name" />
    </a-form-item>
    <a-form-item name="description" label="描述">
      <a-textarea v-model:value="formState.description" />
    </a-form-item>
    <a-form-item name="url" label="URL" :rules="[{ required: true }]">
      <a-input v-model:value="formState.url" />
    </a-form-item>
    <a-form-item name="method" label="请求方法" :rules="[{ required: true }]">
      <a-select v-model:value="formState.method">
        <a-select-option value="GET">GET</a-select-option>
        <a-select-option value="POST">POST</a-select-option>
      </a-select>
    </a-form-item>
    <a-form-item name="requestParams" label="请求参数">
      <JsonEditor
        v-model="formState.requestParams"
        :original-value="originalValues.requestParams || '{}'"
      />
    </a-form-item>
    <a-form-item name="requestHeader" label="请求头">
      <JsonEditor
        v-model="formState.requestHeader"
        :original-value="originalValues.requestHeader || '{}'"
      />
    </a-form-item>
    <a-form-item name="responseHeader" label="响应头">
      <JsonEditor
        v-model="formState.responseHeader"
        :original-value="originalValues.responseHeader || '{}'"
      />
    </a-form-item>
  </a-form>
</template>

<script setup lang="ts">
import { ref, reactive, watch, defineEmits, defineExpose } from 'vue';
import type { InterfaceInfoVO } from '@/types';
import JsonEditor from './JsonEditor.vue';

interface FormState {
  name: string;
  description: string;
  url: string;
  method: string;
  requestParams: string;
  requestHeader: string;
  responseHeader: string;
}

const props = defineProps<{
  initialValues: InterfaceInfoVO | null | Record<string, never>
}>();

const emit = defineEmits(['onFinish']);

const formRef = ref();

const formState = reactive<FormState>({
  name: '',
  description: '',
  url: '',
  method: 'GET',
  requestParams: '{}',
  requestHeader: '{}',
  responseHeader: '{}',
});

const originalValues = reactive<Partial<FormState>>({});

const formatJson = (jsonString: string, defaultValue = '{}') => {
  try {
    const parsed = JSON.parse(jsonString);
    return JSON.stringify(parsed, null, 2);
  } catch (e) {
    return defaultValue;
  }
};

watch(() => props.initialValues, (newVal) => {
  if (newVal && newVal.id) {
    Object.assign(formState, {
      ...newVal,
      requestParams: formatJson(newVal.requestParams, '{}'),
      requestHeader: formatJson(newVal.requestHeader ?? '{}'),
      responseHeader: formatJson(newVal.responseHeader ?? '{}'),
    });
    Object.assign(originalValues, {
      requestParams: formatJson(newVal.requestParams, '{}'),
      requestHeader: formatJson(newVal.requestHeader ?? '{}'),
      responseHeader: formatJson(newVal.responseHeader ?? '{}'),
    });
  } else {
    const defaultRequestHeader = formatJson(JSON.stringify({ 'Content-Type': 'application/json;charset=UTF-8' }));
    const defaultResponseHeader = formatJson(JSON.stringify({ 'Content-Type': 'application/json;charset=UTF-8' }));
    
    Object.assign(formState, {
      name: '',
      description: '',
      url: '',
      method: 'GET',
      requestParams: '{}',
      requestHeader: defaultRequestHeader,
      responseHeader: defaultResponseHeader,
    });
    Object.assign(originalValues, {
      requestParams: '{}',
      requestHeader: defaultRequestHeader,
      responseHeader: defaultResponseHeader,
    });
  }
}, { immediate: true });

const onFinish = (values: FormState) => {
  emit('onFinish', values);
};

const submit = async () => {
  try {
    const values = await formRef.value.validateFields();
    emit('onFinish', values);
    return true;
  } catch (error) {
    console.error('表单验证失败');
    return false;
  }
};

const validateFields = () => {
  return formRef.value.validateFields();
};

defineExpose({ submit, validateFields });

</script> 