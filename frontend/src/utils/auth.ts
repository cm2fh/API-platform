import { UserRole } from '@/types';
import type { UserVO } from '@/types';

/**
 * 权限检查工具类
 */
export class AuthUtils {
  /**
   * 检查用户是否已登录
   * @param user 用户信息
   * @returns 是否已登录
   */
  static isLoggedIn(user: UserVO | null): boolean {
    return !!(user && user.id && user.userRole);
  }

  /**
   * 检查用户是否为管理员
   * @param user 用户信息
   * @returns 是否为管理员
   */
  static isAdmin(user: UserVO | null): boolean {
    return user?.userRole === UserRole.ADMIN;
  }

  /**
   * 检查用户是否被封禁
   * @param user 用户信息
   * @returns 是否被封禁
   */
  static isBanned(user: UserVO | null): boolean {
    return user?.userRole === UserRole.BAN;
  }

  /**
   * 检查用户是否有访问权限
   * @param user 用户信息
   * @param requiredRole 需要的角色
   * @returns 是否有权限
   */
  static hasPermission(user: UserVO | null, requiredRole?: UserRole): boolean {
    // 如果用户被封禁，直接拒绝
    if (this.isBanned(user)) {
      return false;
    }

    // 如果不需要特定角色，只要登录即可
    if (!requiredRole) {
      return this.isLoggedIn(user);
    }

    // 检查具体角色权限
    switch (requiredRole) {
      case UserRole.USER:
        return this.isLoggedIn(user);
      case UserRole.ADMIN:
        return this.isAdmin(user);
      default:
        return false;
    }
  }

  /**
   * 获取用户角色显示文本
   * @param role 用户角色
   * @returns 角色显示文本
   */
  static getRoleText(role: UserRole): string {
    const roleMap = {
      [UserRole.USER]: '普通用户',
      [UserRole.ADMIN]: '管理员',
      [UserRole.BAN]: '已封禁'
    };
    return roleMap[role] || '未知';
  }

  /**
   * 获取用户角色对应的标签颜色
   * @param role 用户角色
   * @returns 标签颜色
   */
  static getRoleColor(role: UserRole): string {
    const colorMap = {
      [UserRole.USER]: 'blue',
      [UserRole.ADMIN]: 'geekblue',
      [UserRole.BAN]: 'red'
    };
    return colorMap[role] || 'default';
  }
}

/**
 * 路由权限配置
 */
export interface RoutePermission {
  /** 是否需要登录 */
  requiresAuth?: boolean;
  /** 需要的用户角色 */
  requiredRole?: UserRole;
  /** 自定义权限检查函数 */
  customCheck?: (user: UserVO | null) => boolean;
}

/**
 * 路由权限检查器
 */
export class RouteGuard {
  /**
   * 检查路由权限
   * @param user 当前用户
   * @param permission 权限配置
   * @returns 权限检查结果
   */
  static checkPermission(user: UserVO | null, permission: RoutePermission): {
    hasPermission: boolean;
    redirectTo?: string;
    reason?: string;
  } {
    // 如果有自定义检查函数，优先使用
    if (permission.customCheck) {
      const hasPermission = permission.customCheck(user);
      return {
        hasPermission,
        redirectTo: hasPermission ? undefined : '/user/login',
        reason: hasPermission ? undefined : '权限不足'
      };
    }

    // 检查是否需要登录
    if (permission.requiresAuth && !AuthUtils.isLoggedIn(user)) {
      return {
        hasPermission: false,
        redirectTo: '/user/login',
        reason: '请先登录'
      };
    }

    // 检查用户是否被封禁
    if (AuthUtils.isBanned(user)) {
      return {
        hasPermission: false,
        redirectTo: '/user/login',
        reason: '账户已被封禁'
      };
    }

    // 检查角色权限
    if (permission.requiredRole && !AuthUtils.hasPermission(user, permission.requiredRole)) {
      return {
        hasPermission: false,
        redirectTo: permission.requiredRole === UserRole.ADMIN ? '/' : '/user/login',
        reason: '权限不足'
      };
    }

    return { hasPermission: true };
  }

  /**
   * 从路由元信息中提取权限配置
   * @param meta 路由元信息
   * @returns 权限配置
   */
  static extractPermissionFromMeta(meta: any): RoutePermission {
    return {
      requiresAuth: meta?.requiresAuth || false,
      requiredRole: meta?.requiresAdmin ? UserRole.ADMIN : meta?.requiredRole,
      customCheck: meta?.customCheck
    };
  }
}

/**
 * Token 管理工具
 */
export class TokenManager {
  private static readonly TOKEN_KEY = 'api_platform_token';
  private static readonly REFRESH_TOKEN_KEY = 'api_platform_refresh_token';

  /**
   * 设置访问令牌
   * @param token 访问令牌
   */
  static setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  /**
   * 获取访问令牌
   * @returns 访问令牌
   */
  static getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * 设置刷新令牌
   * @param refreshToken 刷新令牌
   */
  static setRefreshToken(refreshToken: string): void {
    localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
  }

  /**
   * 获取刷新令牌
   * @returns 刷新令牌
   */
  static getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  /**
   * 清除所有令牌
   */
  static clearTokens(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
  }

  /**
   * 检查令牌是否存在
   * @returns 是否有令牌
   */
  static hasToken(): boolean {
    return !!this.getToken();
  }
}
