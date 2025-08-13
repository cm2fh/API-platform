<template>
  <div class="admin-container">
    <div class="page-header">
      <h2>接口管理后台</h2>
      <a-button type="primary" @click="showModal(null)">
        <PlusOutlined />新增接口
      </a-button>
    </div>

    <a-card class="table-card">
      <a-row class="table-header">
        <a-col :span="16">
          <a-input-search
            v-model:value="searchText"
            placeholder="搜索接口名称"
            style="width: 300px;"
            @search="onSearch"
          />
        </a-col>
        <a-col :span="8" style="text-align: right;">
          <a-select
            v-model:value="statusFilter"
            style="width: 120px; margin-right: 8px;"
            placeholder="状态筛选"
            @change="handleStatusFilterChange"
          >
            <a-select-option value="all">全部状态</a-select-option>
            <a-select-option value="1">已上线</a-select-option>
            <a-select-option value="0">已下线</a-select-option>
          </a-select>
          <a-button type="primary" @click="fetchData">
            <ReloadOutlined />
          </a-button>
        </a-col>
      </a-row>

      <a-table
        :columns="columns"
        :data-source="data"
        row-key="id"
        :loading="loading"
        :pagination="{
          current: page,
          pageSize: pageSize,
          total: total,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (totalCount: number) => `共 ${totalCount} 条`,
          onChange: handleTableChange,
          onShowSizeChange: handleTableChange
        }"
        class="interface-table"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-badge :status="record.status === 1 ? 'success' : 'error'" :text="record.status === 1 ? '上线' : '下线'" />
          </template>
          <template v-if="column.key === 'method'">
            <a-tag :color="getMethodColor(record.method)">{{ record.method }}</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space size="small">
              <a-tooltip title="编辑">
                <a-button type="link" size="small" @click="showModal(record)">
                  <EditOutlined />
                </a-button>
              </a-tooltip>
              <a-tooltip v-if="record.status === 0" title="上线">
                <a-button type="link" size="small" @click="confirmAction('online', record.id)">
                  <CheckCircleOutlined style="color: #52c41a" />
                </a-button>
              </a-tooltip>
              <a-tooltip v-if="record.status === 1" title="下线">
                <a-button type="link" size="small" @click="confirmAction('offline', record.id)">
                  <StopOutlined style="color: #faad14" />
                </a-button>
              </a-tooltip>
              <a-tooltip title="删除">
                <a-button type="link" size="small" danger @click="confirmAction('delete', record.id)">
                  <DeleteOutlined />
                </a-button>
              </a-tooltip>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="isModalVisible"
      :title="modalTitle"
      :confirm-loading="modalLoading"
      @ok="handleOk"
      @cancel="handleCancel"
      width="700px"
    >
      <InterfaceForm ref="interfaceFormRef" :initial-values="editingRecord" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue';
import { interfaceService } from '@/api';
import { message, Modal } from 'ant-design-vue';
import InterfaceForm from '@/components/InterfaceForm.vue';
import type { InterfaceInfoVO } from '@/types';
import {
  EditOutlined,
  DeleteOutlined,
  CheckCircleOutlined,
  StopOutlined,
  PlusOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue';

const data = ref<InterfaceInfoVO[]>([]);
const loading = ref(true);
const isModalVisible = ref(false);
const modalLoading = ref(false);
const editingRecord = ref<InterfaceInfoVO | null>(null);
const interfaceFormRef = ref();
const searchText = ref('');
const statusFilter = ref('all');
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);

const modalTitle = computed(() => editingRecord.value ? '编辑接口' : '新增接口');

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '接口名称', dataIndex: 'name', key: 'name', ellipsis: true },
  { title: '描述', dataIndex: 'description', key: 'description', ellipsis: true },
  { title: 'URL', dataIndex: 'url', key: 'url', ellipsis: true },
  { title: '请求方法', dataIndex: 'method', key: 'method', width: 100 },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' },
];

const fetchData = async () => {
  loading.value = true;
  try {
    const params = {
      current: page.value,
      pageSize: pageSize.value,
      name: searchText.value || undefined,
      status: statusFilter.value !== 'all' ? statusFilter.value : undefined
    };

    const res = await interfaceService.listInterfacesByPageAdmin(params);
    data.value = res.records;
    total.value = res.total;
  } catch (error: any) {
    message.error('获取列表失败');
  } finally {
    loading.value = false;
  }
};

const showModal = (record: InterfaceInfoVO | null) => {
  editingRecord.value = record;
  isModalVisible.value = true;
};

const handleOk = async () => {
  try {
    if (!interfaceFormRef.value) {
      message.error('表单组件未正确加载');
      return;
    }

    // 直接调用表单验证
    const values = await interfaceFormRef.value.validateFields();
    
    // 如果 isFree 为 true，则强制设置 price 为 -1
    if (interfaceFormRef.value.isFree) {
      values.price = -1;
    }
    
    await handleFormFinish(values);
  } catch (error) {
    console.error('表单提交失败');
  }
};

const handleCancel = () => {
  isModalVisible.value = false;
};

const handleFormFinish = async (values: any) => {
  modalLoading.value = true;
  try {
    if (editingRecord.value) {
      await interfaceService.updateInterface({ ...values, id: editingRecord.value.id });
      message.success('更新成功');
    } else {
      await interfaceService.addInterface(values);
      message.success('新增成功');
    }
    isModalVisible.value = false;
    await fetchData();
  } catch (error: any) {
    message.error('操作失败');
  } finally {
    modalLoading.value = false;
  }
};

const confirmAction = (action: 'online' | 'offline' | 'delete', id: number) => {
  const actions = {
    online: { title: '上线接口', content: '确定要上线该接口吗？上线后将对用户可见。', success: '上线成功' },
    offline: { title: '下线接口', content: '确定要下线该接口吗？下线后用户将无法调用。', success: '下线成功' },
    delete: { title: '删除接口', content: '确定要删除该接口吗？删除后不可恢复！', success: '删除成功' }
  };

  Modal.confirm({
    title: actions[action].title,
    content: actions[action].content,
    okText: '确定',
    okType: action === 'delete' ? 'danger' : 'primary',
    cancelText: '取消',
    async onOk() {
      try {
        if (action === 'online') {
          await handleOnline(id);
        } else if (action === 'offline') {
          await handleOffline(id);
        } else if (action === 'delete') {
          await handleDelete(id);
        }
        message.success(actions[action].success);
      } catch (error: any) {
        message.error('操作失败');
      }
    }
  });
};

const handleOnline = async (id: number) => {
  await interfaceService.onlineInterface(id);
  await fetchData();
};

const handleOffline = async (id: number) => {
  await interfaceService.offlineInterface(id);
  await fetchData();
};

const handleDelete = async (id: number) => {
  await interfaceService.deleteInterface(id);
  await fetchData();
};

const onSearch = () => {
  page.value = 1;
  fetchData();
};

const handleStatusFilterChange = () => {
  page.value = 1;
  fetchData();
};

const handleTableChange = (currentPage: number, currentPageSize: number) => {
  page.value = currentPage;
  pageSize.value = currentPageSize;
  fetchData();
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
.admin-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  color: rgba(0, 0, 0, 0.85);
  font-weight: 500;
}

.table-card {
  margin-bottom: 24px;
  border-radius: 2px;
}

.table-header {
  margin-bottom: 16px;
}

.interface-table {
  margin-top: 16px;
}

:deep(.ant-table-thead > tr > th) {
  background-color: #fafafa;
}

:deep(.ant-pagination) {
  margin-top: 16px;
}
</style> 