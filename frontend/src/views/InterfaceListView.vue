<template>
  <div class="interface-list">
    <div class="page-header">
      <h1>API 接口市场</h1>
      <p class="sub-title">发现更多优质 API 接口，快速构建应用</p>
    </div>

    <div class="search-filter">
      <a-input-search
        v-model:value="searchText"
        placeholder="搜索接口"
        class="search-input"
        @search="onSearch"
      />
    </div>

    <a-empty
      v-if="list.length === 0 && !loading"
      description="暂无接口数据"
    >
      <template #description>
        <span>暂时没有符合条件的接口</span>
      </template>
    </a-empty>

    <a-list
      v-else
      :grid="{ gutter: 24, xs: 1, sm: 2, md: 3, lg: 3, xl: 4, xxl: 4 }"
      :data-source="list"
      :loading="loading"
    >
      <template #renderItem="{ item }">
        <a-list-item>
          <a-card hoverable class="interface-card" @click="handleCardClick(item.id)">
            <template #cover>
              <div class="card-header" :style="{ backgroundColor: ColorUtils.getRandomColor(item.name) }">
                <ApiOutlined class="icon" />
              </div>
            </template>
            <a-card-meta :title="item.name">
              <template #description>
                <p class="description">{{ item.description }}</p>
                <div class="card-footer">
                  <a-space>
                    <a-tag>{{ item.method }}</a-tag>
                  </a-space>
                  <span class="status">
                    <a-badge :status="item.status === 1 ? 'success' : 'default'" :text="item.status === 1 ? '已上线' : '维护中'" />
                  </span>
                </div>
              </template>
            </a-card-meta>
          </a-card>
        </a-list-item>
      </template>
    </a-list>

    <a-pagination
      v-if="pagination.total > 0"
      v-model:current="pagination.current"
      v-model:pageSize="pagination.pageSize"
      :total="pagination.total"
      @change="onPageChange"
      class="pagination"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { interfaceService } from '@/api';
import type { InterfaceInfoVO } from '@/types';
import { ColorUtils } from '@/utils';
import { message } from 'ant-design-vue';
import { ApiOutlined } from '@ant-design/icons-vue';

const router = useRouter();
const list = ref<InterfaceInfoVO[]>([]);
const loading = ref(true);
const searchText = ref('');
const pagination = reactive({
  current: 1,
  pageSize: 8,
  total: 0,
});

const fetchData = async (page = 1, pageSize = 8) => {
  loading.value = true;
  try {
    const params: any = {
      current: page,
      pageSize: pageSize,
    };

    // 如果有搜索文本，添加到查询参数中
    if (searchText.value && searchText.value.trim()) {
      params.name = searchText.value.trim();
    }

    const res = await interfaceService.listInterfacesByPage(params);
    list.value = res.records;
    pagination.total = res.total;
  } catch (error: any) {
    message.error('获取接口列表失败');
  } finally {
    loading.value = false;
  }
};

const onPageChange = (page: number, pageSize: number) => {
  pagination.current = page;
  pagination.pageSize = pageSize;
  fetchData(page, pageSize);
};

const onSearch = (value: string) => {
  searchText.value = value;
  pagination.current = 1;
  fetchData();
};
const handleCardClick = (id: number) => {
  router.push(`/interface/${id}`);
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.interface-list {
  max-width: 1200px;
  margin: 0 auto;
}

.search-input {
  width: 280px;
}

.interface-card {
  height: 100%;
  transition: all var(--motion-duration-slow) var(--motion-ease-out);
  cursor: pointer;
}

.interface-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.card-header {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-header .icon {
  font-size: 40px;
  color: var(--text-color-inverse);
}

.description {
  height: 44px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  color: var(--text-color-tertiary);
  margin-bottom: var(--spacing-lg);
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: var(--spacing-sm);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-input {
    width: 100%;
  }
}

@media (max-width: 576px) {
  .interface-list {
    padding: 0 var(--spacing-lg);
  }
}
</style> 