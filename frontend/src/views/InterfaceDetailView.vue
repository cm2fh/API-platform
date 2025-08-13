<template>
  <div class="interface-detail" v-if="interfaceInfo">
    <a-page-header
      :title="interfaceInfo.name"
      @back="() => router.back()"
      class="detail-header"
    >
      <template #tags>
        <a-tag :color="interfaceInfo.status === 1 ? 'green' : 'orange'">
          {{ interfaceInfo.status === 1 ? '已上线' : '维护中' }}
        </a-tag>
      </template>
      <template #subtitle>
        <a-tag color="blue">{{ interfaceInfo.method }}</a-tag>
      </template>
    </a-page-header>

    <div class="detail-container">
      <a-card class="info-card" title="接口信息" bordered>
        <a-descriptions bordered size="small" :column="1">
          <a-descriptions-item label="接口描述">{{ interfaceInfo.description }}</a-descriptions-item>
          <a-descriptions-item label="接口地址">
            <a-typography-text copyable>{{ interfaceInfo.url }}</a-typography-text>
          </a-descriptions-item>
          <a-descriptions-item label="请求方法">
            <a-tag :color="getMethodColor(interfaceInfo.method)">{{ interfaceInfo.method }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="计费方式">
            <a-tag color="green">免费接口</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ formatTime(interfaceInfo.createTime) }}</a-descriptions-item>
          <a-descriptions-item label="更新时间">{{ formatTime(interfaceInfo.updateTime) }}</a-descriptions-item>
        </a-descriptions>
      </a-card>
      
      <!-- 接口开通卡片 -->
      <a-card class="action-card" title="接口调用" bordered>
        <p v-if="!isInterfaceEnabled && interfaceInfo.status === 1">您还未开通此接口，请先开通后再使用</p>
        <p v-if="interfaceInfo.status !== 1">该接口当前处于维护状态，暂时无法使用</p>
        <div class="action-buttons">
          <a-button 
            type="primary" 
            :loading="applyLoading" 
            @click="applyInterface"
            v-if="!isInterfaceEnabled && interfaceInfo.status === 1">
            免费开通接口
          </a-button>
          <a-tag v-if="isInterfaceEnabled" color="success">
            <span class="remain-info">已开通</span>
          </a-tag>
        </div>
      </a-card>

      <a-card class="request-card" title="接口文档" bordered>
        <a-tabs default-active-key="1">
          <a-tab-pane key="1" tab="请求参数">
            <pre class="code-display">{{ interfaceInfo.requestParams || '{}' }}</pre>
          </a-tab-pane>
          <a-tab-pane key="2" tab="请求头">
            <pre class="code-display">{{ interfaceInfo.requestHeader || '{}' }}</pre>
          </a-tab-pane>
          <a-tab-pane key="3" tab="响应头">
            <pre class="code-display">{{ interfaceInfo.responseHeader || '{}' }}</pre>
          </a-tab-pane>
        </a-tabs>
      </a-card>

      <!-- 添加测试表单卡片 -->
      <a-card class="test-card" title="接口测试" bordered ref="testCard">
        <a-form :model="testForm" layout="vertical">
          <a-form-item>
            <JsonEditor
              v-model="testForm.userRequestParams"
              :original-value="originalRequestParams"
            />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" :loading="loading" @click="invokeInterface">
              发送请求
            </a-button>
          </a-form-item>
        </a-form>

        <!-- 测试结果展示区域 -->
        <div v-if="testResult" class="test-result">
          <a-divider>测试结果</a-divider>
          <pre class="code-display">{{ testResult }}</pre>
        </div>
      </a-card>
    </div>
  </div>
  <a-skeleton v-else active :paragraph="{ rows: 10 }" />
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, reactive, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { interfaceService, userInterfaceInfoService } from '@/api';
import type { InterfaceInfoVO, UserInterfaceInfoVO } from '@/types';
import { message } from 'ant-design-vue';
import JsonEditor from '@/components/JsonEditor.vue';

const route = useRoute();
const router = useRouter();
const interfaceInfo = ref<InterfaceInfoVO | null>(null);
const testCard = ref<HTMLElement | null>(null);
const loading = ref(false);
const applyLoading = ref(false);
const testResult = ref('');
const isInterfaceEnabled = ref(false);
const originalRequestParams = ref('{}');

// 测试表单数据
const testForm = reactive({
  userRequestParams: '{}'
});

const fetchData = async () => {
  try {
    const id = Number(route.params.id);
    interfaceInfo.value = await interfaceService.getInterfaceById(id);
    // 获取到接口信息后，预填充请求参数示例
    if (interfaceInfo.value?.requestParams) {
      try {
        const parsed = JSON.parse(interfaceInfo.value.requestParams);
        const formattedParams = JSON.stringify(parsed, null, 2);
        testForm.userRequestParams = formattedParams;
        originalRequestParams.value = formattedParams;
      } catch (e) {
        testForm.userRequestParams = interfaceInfo.value.requestParams;
        originalRequestParams.value = interfaceInfo.value.requestParams;
      }
    }
    
    // 获取接口开通状态
    await checkInterfaceStatus(id);
  } catch (error: any) {
    message.error('获取接口信息失败: ' + error.message);
  }
};

// 检查接口是否已开通
const checkInterfaceStatus = async (id: number) => {
  try {
    const userInterfaceInfo = await userInterfaceInfoService.getUserInterfaceInfo(id);
    isInterfaceEnabled.value = userInterfaceInfo != null;
  } catch (error) {
    // 出错时表示未开通
    isInterfaceEnabled.value = false;
  }
};

// 开通接口
const applyInterface = async () => {
  if (!interfaceInfo.value) return;
  
  const id = interfaceInfo.value.id;
  applyLoading.value = true;
  
  try {
    await userInterfaceInfoService.applyInterface(id);
    message.success('接口开通成功！可以无限次调用此接口');
    isInterfaceEnabled.value = true;
  } catch (error: any) {
    message.error('开通接口失败: ' + error.message);
  } finally {
    applyLoading.value = false;
  }
};

// 调用接口方法
const invokeInterface = async () => {
  if (!interfaceInfo.value) return;
  
  // 检查接口是否已开通
  if (!isInterfaceEnabled.value) {
    message.warning('请先开通接口后再调用');
    return;
  }

  try {
    loading.value = true;
    const id = Number(route.params.id);

    const result = await interfaceService.invokeInterface({
      id,
      userRequestParams: testForm.userRequestParams
    });
    testResult.value = JSON.stringify(result, null, 2);
    message.success('接口调用成功');
  } catch (error: any) {
    message.error('接口调用失败: ' + error.message);
    testResult.value = JSON.stringify({ error: error.message }, null, 2);
  } finally {
    loading.value = false;
  }
};

const scrollToTest = () => {
  nextTick(() => {
    if (testCard.value) {
      testCard.value.scrollIntoView({ behavior: 'smooth' });
    }
  });
};

const formatTime = (time: string) => {
  if (!time) return '-';
  return new Date(time).toLocaleString('zh-CN');
};

const getMethodColor = (method: string) => {
  const methodColors: Record<string, string> = {
    GET: 'green',
    POST: 'blue',
    PUT: 'orange',
    DELETE: 'red',
    PATCH: 'purple'
  };
  return methodColors[method] || 'default';
};

onMounted(fetchData);
</script>

<style scoped>
.interface-detail {
  max-width: 1000px;
  margin: 0 auto;
}

.detail-header {
  margin-bottom: 24px;
  background-color: white;
  padding: 16px;
  border-radius: 2px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.detail-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

:deep(.ant-descriptions-item-label) {
  width: 120px;
  background-color: #fafafa;
}

:deep(.code-display) {
  background-color: #f5f5f5;
  padding: 16px;
  border-radius: 2px;
  border: 1px solid #d9d9d9;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 300px;
  overflow-y: auto;
}

.test-result {
  margin-top: 16px;
}

.test-card {
  margin-bottom: 24px;
}

.action-card {
  margin-bottom: 24px;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 8px;
}

.remain-info {
  font-size: 14px;
}

@media (max-width: 768px) {
  .interface-detail {
    padding: 0 12px;
  }

  :deep(.ant-descriptions-item-label) {
    width: 100px;
  }
}
</style>
