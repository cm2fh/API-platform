// 枚举定义
export enum UserRole {
  USER = 'user',
  ADMIN = 'admin',
  BAN = 'ban'
}

export enum InterfaceStatus {
  OFFLINE = 0,
  ONLINE = 1
}

export enum HttpMethod {
  GET = 'GET',
  POST = 'POST',
  PUT = 'PUT',
  DELETE = 'DELETE',
  PATCH = 'PATCH'
}

// 基础响应类型
export interface ApiResponse<T = any> {
  code: number;
  data: T;
  message: string;
}

// 分页参数
export interface PageParams {
  current?: number;
  pageSize?: number;
  sortField?: string;
  sortOrder?: 'ascend' | 'descend';
  [key: string]: any;
}

// 分页响应
export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  pageSize: number;
}

// 用户相关类型
export interface UserVO {
  id: number;
  userName: string;
  userAvatar?: string;
  userProfile?: string;
  userRole: UserRole;
  accessKey: string;
  secretKey: string;
  balance?: number;
  createTime: string;
  updateTime: string;
}

export interface LoginRequest {
  userAccount: string;
  userPassword: string;
}

export interface RegisterRequest {
  userAccount: string;
  userPassword: string;
  checkPassword: string;
}

export interface UpdateUserRequest {
  userAvatar?: string;
  userName?: string;
  userProfile?: string;
}

// 接口相关类型
export interface InterfaceInfoVO {
  id: number;
  name: string;
  description: string;
  url: string;
  requestParams?: string;
  requestHeader: string;
  responseHeader: string;
  status: InterfaceStatus;
  method: HttpMethod;
  userId: number;
  price?: number;
  createTime: string;
  updateTime: string;
}

export interface InterfaceInfoRequest {
  name?: string;
  description?: string;
  url?: string;
  method?: HttpMethod;
  requestParams?: string;
  requestHeader?: string;
  responseHeader?: string;
  status?: InterfaceStatus;
  price?: number;
  id?: number;
}

export interface InvokeInterfaceRequest {
  id: number;
  userRequestParams: string;
}

// 用户接口调用记录
export interface UserInterfaceInfoVO {
  id: number;
  userId: number;
  interfaceInfoId: number;
  totalNum: number;
  remainNum: number;
  status: number;
  createTime: string;
  updateTime: string;
}

// 接口调用记录VO
export interface InvokeRecordVO {
  id?: number;
  name: string;
  description?: string;
  url?: string;
  method?: HttpMethod;
  totalNum: number;
  status?: InterfaceStatus;
  createTime?: string;
  updateTime: string;
}