<template>
  <div class="profile-container" v-if="userStore.currentUser">
    <a-row :gutter="[24, 24]">
      <a-col :xs="24" :sm="24" :md="8" :lg="7" :xl="6">
        <a-card class="user-card" :bordered="false">
          <div class="avatar-wrapper">
            <a-avatar :size="120" :src="imageUrl">
              <template #icon v-if="!imageUrl">
                <span style="font-size: 60px;">{{ userStore.currentUser.userName ? userStore.currentUser.userName.charAt(0).toUpperCase() : 'U' }}</span>
              </template>
            </a-avatar>
          </div>
          <div class="user-info">
            <h2 class="username">{{ userStore.currentUser.userName || '未设置昵称' }}</h2>
            <div class.user-role>
              <a-tag :color="userStore.currentUser.userRole === 'admin' ? 'geekblue' : 'blue'">
                <template #icon>
                  <CrownOutlined v-if="userStore.currentUser.userRole === 'admin'" />
                  <UserOutlined v-else />
                </template>
                {{ userRoleText }}
              </a-tag>
            </div>
            <a-divider />
            <div class="user-meta">
              <p><IdcardOutlined /> <span>用户ID: {{ userStore.currentUser.id }}</span></p>
              <p><ClockCircleOutlined /> <span>注册时间: {{ formatDate(userStore.currentUser.createTime) }}</span></p>
            </div>
          </div>
        </a-card>

        <a-card class="quick-links" title="快速操作" :bordered="false">
          <a-button block class="quick-link-btn" @click="goToInterfaceList"><AppstoreOutlined /> 浏览接口市场</a-button>
          <a-button v-if="userStore.currentUser.userRole === 'admin'" block class="quick-link-btn" @click="goToAdmin"><SettingOutlined /> 后台管理</a-button>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="24" :md="16" :lg="17" :xl="18">
        <a-card class="main-content" :bordered="false">
          <a-tabs default-active-key="1">
            <a-tab-pane key="1" tab="基本信息">
              <a-row :gutter="[16,16]">
                <a-col :xs="24" :md="16">
                   <a-form :model="formState" @finish="handleUpdateUserInfo" layout="vertical">
                    <a-form-item label="昵称" name="userName">
                      <a-input
                        v-model:value="formState.userName"
                        placeholder="请输入昵称"
                        :maxLength="20"
                      >
                        <template #prefix>
                          <UserOutlined />
                        </template>
                      </a-input>
                    </a-form-item>
                  </a-form>
                </a-col>
              </a-row>

              <a-row :gutter="[16,16]">
                <a-col :xs="24" :md="8">
                  <a-form-item label="头像">
                    <a-upload
                      name="avatar"
                      list-type="picture-card"
                      class="avatar-uploader"
                      :show-upload-list="false"
                      :before-upload="beforeUpload"
                      :custom-request="customRequest"
                    >
                      <img v-if="imageUrl" :src="imageUrl" alt="avatar" style="width: 100%" />
                      <div v-else>
                        <loading-outlined v-if="uploading"></loading-outlined>
                        <plus-outlined v-else></plus-outlined>
                        <div class="ant-upload-text">上传</div>
                      </div>
                    </a-upload>
                    <div class="avatar-hint">支持 JPG/PNG, 小于 2MB</div>
                  </a-form-item>
                </a-col>
              </a-row>
              <a-form-item>
                <a-button type="primary" html-type="submit" :loading="updateLoading">保存修改</a-button>
              </a-form-item>
            </a-tab-pane>

            <a-tab-pane key="2" tab="开发者凭证">
              <a-alert
                message="安全提示"
                description="请妥善保管您的密钥，不要泄露给他人。如果您怀疑密钥已泄露，请立即重新生成。"
                type="warning"
                show-icon
                style="margin-bottom: 24px"
              />

              <a-card class="key-card" title="Access Key" size="small">
                <div class="key-container">
                  <a-typography-text class="key-text" :copyable="{ text: userStore.currentUser.accessKey }">
                    {{ showAccessKey ? userStore.currentUser.accessKey : '********************************' }}
                  </a-typography-text>
                  <a-button type="text" @click="toggleAccessKeyVisibility">
                    <template #icon>
                      <EyeOutlined v-if="!showAccessKey" />
                      <EyeInvisibleOutlined v-else />
                    </template>
                  </a-button>
                </div>
              </a-card>

              <a-card class="key-card" title="Secret Key" size="small" style="margin-top: 16px;">
                <div class="key-container">
                  <a-typography-text class="key-text" :copyable="{ text: userStore.currentUser.secretKey }">
                    {{ showSecretKey ? userStore.currentUser.secretKey : '********************************' }}
                  </a-typography-text>
                  <a-button type="text" @click="toggleSecretKeyVisibility">
                    <template #icon>
                      <EyeOutlined v-if="!showSecretKey" />
                      <EyeInvisibleOutlined v-else />
                    </template>
                  </a-button>
                </div>
              </a-card>

              <div style="margin-top: 24px;">
                <a-popconfirm
                  title="重新生成密钥将使当前密钥失效，确定要继续吗？"
                  @confirm="handleRegenerate"
                  ok-text="确定"
                  cancel-text="取消"
                  placement="right"
                >
                  <a-button type="primary" danger :loading="regenerateLoading">
                    <KeyOutlined /> 重新生成密钥
                  </a-button>
                </a-popconfirm>
              </div>
            </a-tab-pane>

            <a-tab-pane key="3" tab="使用统计">
              <a-empty v-if="!hasUsageData" description="暂无使用数据">
                <template #description>
                  <p>您还没有调用过任何接口</p>
                  <a-button type="primary" @click="goToInterfaceList">去市场看看</a-button>
                </template>
              </a-empty>
              <template v-else>
                <a-row :gutter="16" class="stats-row">
                  <a-col :span="24">
                    <a-card class="stat-card">
                      <a-statistic title="总调用次数" :value="totalInvokes" :value-style="{ color: '#1890ff' }">
                        <template #prefix><ApiOutlined /></template>
                      </a-statistic>
                    </a-card>
                  </a-col>
                </a-row>

                <a-card title="最近调用记录" style="margin-top: 24px;" :bordered="false">
                  <a-list item-layout="horizontal" :data-source="recentInvokes.filter(item => item.totalNum > 0)" :loading="loadingUsageData">
                    <template #renderItem="{ item }">
                      <a-list-item>
                        <a-list-item-meta
                          :description="formatDate(item.updateTime)"
                        >
                          <template #title>
                            <a>{{ item.name }}</a>
                          </template>
                          <template #avatar>
                            <a-avatar :style="{ backgroundColor: getRandomColor(item.name) }">
                              <template #icon><ApiOutlined /></template>
                            </a-avatar>
                          </template>
                        </a-list-item-meta>
                        <div class="list-item-extra">
                           <a-tag color="success">
                            已调用
                          </a-tag>
                        </div>
                      </a-list-item>
                    </template>
                  </a-list>
                </a-card>
              </template>
            </a-tab-pane>
          </a-tabs>
        </a-card>
      </a-col>
    </a-row>
  </div>
  <a-skeleton v-else active avatar :paragraph="{ rows: 10 }" />
</template>

<script setup lang="ts">
import { ref, watchEffect, reactive, computed, onMounted } from 'vue';
import { useUserStore } from '@/store/user';
import { userService, fileService, userInterfaceInfoService } from '@/api';
import { message } from 'ant-design-vue';
import {
  PlusOutlined,
  LoadingOutlined,
  UserOutlined,
  ClockCircleOutlined,
  KeyOutlined,
  AppstoreOutlined,
  SettingOutlined,
  ApiOutlined,
  RiseOutlined,
  CheckCircleOutlined,
  CrownOutlined,
  IdcardOutlined,
  EyeOutlined,
  EyeInvisibleOutlined
} from '@ant-design/icons-vue';
import { useRouter } from 'vue-router';
import type { InvokeRecordVO } from '@/types';


const userStore = useUserStore();
const router = useRouter();
const regenerateLoading = ref(false);
const updateLoading = ref(false);
const uploading = ref(false);
const imageUrl = ref<string>('');
const showSecretKey = ref(false);
const showAccessKey = ref(false);
const loadingUsageData = ref(false);
const recentInvokes = ref<InvokeRecordVO[]>([]);
const totalInvokes = ref(0);

const userRoleText = computed(() => {
  if (!userStore.currentUser) return '';
  return userStore.currentUser.userRole === 'admin' ? '管理员' : '普通用户';
});

const hasUsageData = computed(() => recentInvokes.value.some(item => item.totalNum > 0));

const formState = reactive({
  userName: '',
  userAvatar: ''
});

watchEffect(() => {
  if (userStore.currentUser) {
    formState.userName = userStore.currentUser.userName;
    formState.userAvatar = userStore.currentUser.userAvatar || '';
    imageUrl.value = userStore.currentUser.userAvatar || '';
  }
});

const handleRegenerate = async () => {
  regenerateLoading.value = true;
  try {
    await userService.regenerateKey();
    await userStore.fetchUser();
    message.success('密钥已重新生成');
  } catch (error: any) {
    message.error('操作失败');
  } finally {
    regenerateLoading.value = false;
  }
};

const handleUpdateUserInfo = async () => {
  updateLoading.value = true;
  try {
    await userService.updateMyUser(formState);
    await userStore.fetchUser();
    message.success('更新成功');
  } catch (error: any) {
    message.error('更新失败');
  } finally {
    updateLoading.value = false;
  }
};

const customRequest = async ({ file, onSuccess, onError }: any) => {
  uploading.value = true;
  try {
    const resUrl = await fileService.uploadFile(file, "user_avatar") as unknown as string;
    formState.userAvatar = resUrl;
    imageUrl.value = resUrl;
    if (onSuccess) {
      onSuccess(resUrl);
    }
    message.success('头像上传成功');
  } catch (e: any) {
    if (onError) {
      onError(e);
    }
    message.error('头像上传失败');
  } finally {
    uploading.value = false;
  }
};

const beforeUpload = (file: any) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
  if (!isJpgOrPng) {
    message.error('只能上传 JPG/PNG 格式的图片!');
  }
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    message.error('图片大小不能超过 2MB!');
  }
  return isJpgOrPng && isLt2M;
};

const formatDate = (dateString: string) => {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const fetchRecentInvokes = async () => {
  loadingUsageData.value = true;
  try {
    const res = await userInterfaceInfoService.listMyInvokeRecordsByPage({ pageSize: 5 });
    recentInvokes.value = res.records;
    // 计算总调用次数
    totalInvokes.value = recentInvokes.value.reduce((sum, item) => sum + (item.totalNum || 0), 0);
  } catch (e: any) {
    message.error('获取调用记录失败');
  } finally {
    loadingUsageData.value = false;
  }
};

const toggleSecretKeyVisibility = () => {
  showSecretKey.value = !showSecretKey.value;
};

const toggleAccessKeyVisibility = () => {
  showAccessKey.value = !showAccessKey.value;
};

const getRandomColor = (str: string) => {
  const colors = ['#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1', '#13c2c2'];
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash);
  }
  const index = Math.abs(hash % colors.length);
  return colors[index];
};

const goToInterfaceList = () => {
  router.push('/');
};

const goToAdmin = () => {
  router.push('/admin');
};

onMounted(() => {
  fetchRecentInvokes();
});
</script>

<style scoped>
.user-card, .quick-links, .main-content, .stat-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.09);
}

.user-card {
  text-align: center;
}

.avatar-wrapper {
  margin: 24px 0;
}

:deep(.ant-avatar) {
  border: 4px solid #fff;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.user-info {
  padding: 0 16px 24px;
}

.username {
  font-size: 20px;
  font-weight: 500;
  margin-bottom: 8px;
  color: rgba(0,0,0,0.85);
}

.user-meta {
  text-align: left;
  color: rgba(0,0,0,0.65);
  font-size: 14px;
  margin-top: 16px;
}

.user-meta p {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}

.user-meta span {
  margin-left: 8px;
}

.quick-links {
  margin-top: 24px;
}

.quick-link-btn {
  margin-bottom: 12px;
  text-align: left;
}
.quick-link-btn:last-child {
  margin-bottom: 0;
}

.avatar-uploader {
  width: 128px;
  height: 128px;
}

.avatar-hint {
  color: rgba(0,0,0,0.45);
  font-size: 12px;
  margin-top: -10px;
}

.key-card {
  background-color: #fafafa;
  border-radius: 4px;
}

.key-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace;
}

.key-text {
  font-size: 14px;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card .ant-statistic-title {
  font-size: 14px;
}
.stat-card .ant-statistic-content {
  font-size: 24px;
}

.list-item-extra {
  margin-left: 16px;
}
</style> 