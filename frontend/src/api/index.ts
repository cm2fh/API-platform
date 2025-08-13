import axios from 'axios';
import { message as antdMessage } from 'ant-design-vue';
import type { UserVO } from '@/types';

export interface ApiResponse<T = any> {
    code: number;
    data: T;
    message: string;
}

interface LoginRequest {
    userAccount: string;
    userPassword: string;
}

interface RegisterRequest {
    userAccount: string;
    userPassword: string;
    checkPassword: string;
}

interface UpdateUserRequest {
    userAvatar?: string;
    userName?: string;
}

interface InterfaceInfoRequest {
    name?: string;
    description?: string;
    url?: string;
    method?: string;
    requestParams?: string;
    requestHeader?: string;
    responseHeader?: string;
    status?: number;
    id?: number;
}

interface InvokeInterfaceRequest {
    id: number;
    userRequestParams: string;
}

interface PageParams {
    current?: number;
    pageSize?: number;
    sortField?: string;
    sortOrder?: 'ascend' | 'descend';
    [key: string]: any;
}

const instance = axios.create({
    baseURL: '/api',
    timeout: 10000,
});

// 请求拦截器
instance.interceptors.request.use(config => {
    return config;
}, error => {
    return Promise.reject(error);
});

// 响应拦截器
instance.interceptors.response.use(response => {
    const res: ApiResponse = response.data;
    if (res.code !== 0) {
        antdMessage.error(res.message || 'Error');
        return Promise.reject(new Error(res.message || 'Error'));
    } else {
        return res.data;
    }
}, error => {
    antdMessage.error('网络错误');
    return Promise.reject(error);
});

export const fileService = {
    uploadFile: (file: File, biz: string) => {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('biz', biz);
        return instance.post<any, string>('/file/upload', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
    }
};

export const userService = {
    login: (data: LoginRequest): Promise<UserVO> => instance.post<any, UserVO>('/user/login', data),
    register: (data: RegisterRequest): Promise<void> => instance.post<any, void>('/user/register', data),
    getLoginUser: (): Promise<UserVO> => instance.get<any, UserVO>('/user/get/login'),
    logout: (): Promise<void> => instance.post<any, void>('/user/logout'),
    regenerateKey: (): Promise<string> => instance.post<any, string>('/user/regenerate'),
    updateMyUser: (data: UpdateUserRequest): Promise<boolean> => instance.post<any, boolean>('/user/update/my', data)
};

export const interfaceService = {
    listInterfaces: (params: Record<string, any>): Promise<any[]> => instance.get<any, any[]>('/interfaceInfo/list', { params }),
    listInterfacesByPage: (params: PageParams): Promise<{ records: any[], total: number }> => instance.get<any, { records: any[], total: number }>('/interfaceInfo/list/page/public', { params }),
    listInterfacesByPageAdmin: (params: PageParams): Promise<{ records: any[], total: number }> => instance.get<any, { records: any[], total: number }>('/interfaceInfo/list/page', { params }),
    getInterfaceById: (id: number): Promise<any> => instance.get<any, any>('/interfaceInfo/get', { params: { id } }),
    invokeInterface: (data: InvokeInterfaceRequest): Promise<any> => instance.post<any, any>('/interfaceInfo/invoke', data),
    addInterface: (data: InterfaceInfoRequest): Promise<number> => instance.post<any, number>('/interfaceInfo/add', data),
    updateInterface: (data: InterfaceInfoRequest): Promise<boolean> => instance.post<any, boolean>('/interfaceInfo/update', data),
    deleteInterface: (id: number): Promise<boolean> => instance.post<any, boolean>('/interfaceInfo/delete', { id }),
    onlineInterface: (id: number): Promise<boolean> => instance.post<any, boolean>('/interfaceInfo/online', { id }),
    offlineInterface: (id: number): Promise<boolean> => instance.post<any, boolean>('/interfaceInfo/offline', { id }),
};

export const userInterfaceInfoService = {
    listMyInvokeRecordsByPage: (params: PageParams): Promise<{ records: any[], total: number }> => instance.get<any, { records: any[], total: number }>('/userInterfaceInfo/my/list/page', { params }),
    getUserInterfaceInfo: (interfaceInfoId: number): Promise<any> => instance.get<any, any>('/userInterfaceInfo/get/interface', { params: { interfaceInfoId } }),
    applyInterface: (id: number): Promise<boolean> => instance.post<any, boolean>('/userInterfaceInfo/apply', { id }),
}; 