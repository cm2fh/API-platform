<template>
  <div class="ant-json-editor">
    <div class="editor-toolbar">
      <a-space>
        <a-button @click="handleFormat" size="small">格式化</a-button>
        <a-button @click="handleReset" size="small">还原</a-button>
        <a-button @click="handleClear" size="small" danger>清空</a-button>
      </a-space>
    </div>
    <a-textarea
      v-model:value="content"
      :rows="10"
      class="editor-textarea"
      placeholder="请输入JSON格式的参数"
      @blur="handleValidate"
    />
    <a-alert
      v-if="error"
      :message="error"
      type="error"
      show-icon
      class="error-alert"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { message } from 'ant-design-vue';

const props = defineProps<{
  modelValue: string;
  originalValue: string;
}>();

const emit = defineEmits(['update:modelValue']);

const content = ref(props.modelValue);
const error = ref('');

watch(() => props.modelValue, (newValue) => {
  if (newValue !== content.value) {
    content.value = newValue;
    handleValidate();
  }
});

watch(content, (newContent) => {
  emit('update:modelValue', newContent);
});

const handleFormat = () => {
  try {
    const parsed = JSON.parse(content.value);
    content.value = JSON.stringify(parsed, null, 2);
    error.value = '';
  } catch (e) {
    message.error('JSON格式错误，无法格式化');
  }
};

const handleReset = () => {
  content.value = props.originalValue;
  message.success('内容已还原');
};

const handleClear = () => {
  content.value = '{}';
};

const handleValidate = () => {
  if (!content.value || content.value.trim() === '') {
    error.value = '';
    return;
  }
  try {
    JSON.parse(content.value);
    error.value = '';
  } catch (e: any) {
    error.value = `JSON格式错误: ${e.message}`;
  }
};
</script>

<style scoped>
.ant-json-editor {
  border: 1px solid #d9d9d9;
  border-radius: 4px;
}

.editor-toolbar {
  padding: 8px;
  background-color: #fafafa;
  border-bottom: 1px solid #d9d9d9;
  border-radius: 4px 4px 0 0;
}

.editor-textarea {
  border: none;
  box-shadow: none !important;
  border-radius: 0 0 4px 4px;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace;
}

.error-alert {
  margin: 8px;
  border-radius: 4px;
}
</style> 