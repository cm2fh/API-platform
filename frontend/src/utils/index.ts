import { HttpMethod } from '@/types';

/**
 * 颜色工具函数
 */
export class ColorUtils {
  // 预定义颜色数组
  private static readonly COLORS = [
    '#1890ff', '#52c41a', '#722ed1', '#fa8c16', '#eb2f96',
    '#13c2c2', '#faad14', '#a0d911', '#fa541c', '#f759ab'
  ];

  // HTTP方法颜色映射
  private static readonly METHOD_COLORS: Record<HttpMethod, string> = {
    [HttpMethod.GET]: 'green',
    [HttpMethod.POST]: 'blue',
    [HttpMethod.PUT]: 'orange',
    [HttpMethod.DELETE]: 'red',
    [HttpMethod.PATCH]: 'purple'
  };

  /**
   * 根据字符串生成随机颜色
   * @param str 输入字符串
   * @returns 颜色值
   */
  static getRandomColor(str: string): string {
    if (!str) return this.COLORS[0];
    const index = str.charCodeAt(0) % this.COLORS.length;
    return this.COLORS[index];
  }

  /**
   * 获取HTTP方法对应的颜色
   * @param method HTTP方法
   * @returns 颜色值
   */
  static getMethodColor(method: HttpMethod | string): string {
    return this.METHOD_COLORS[method as HttpMethod] || 'default';
  }

  /**
   * 获取状态对应的颜色
   * @param status 状态值
   * @returns 颜色值
   */
  static getStatusColor(status: number): string {
    return status === 1 ? 'success' : 'error';
  }
}

/**
 * 时间工具函数
 */
export class DateUtils {
  /**
   * 格式化时间为本地字符串
   * @param time 时间字符串或时间戳
   * @param locale 本地化设置，默认为中文
   * @returns 格式化后的时间字符串
   */
  static formatTime(time: string | number | Date, locale = 'zh-CN'): string {
    if (!time) return '-';
    try {
      return new Date(time).toLocaleString(locale);
    } catch (error) {
      console.warn('时间格式化失败:', error);
      return '-';
    }
  }

  /**
   * 格式化时间为日期字符串
   * @param time 时间字符串或时间戳
   * @param locale 本地化设置，默认为中文
   * @returns 格式化后的日期字符串
   */
  static formatDate(time: string | number | Date, locale = 'zh-CN'): string {
    if (!time) return '-';
    try {
      return new Date(time).toLocaleDateString(locale);
    } catch (error) {
      console.warn('日期格式化失败:', error);
      return '-';
    }
  }

  /**
   * 获取相对时间描述
   * @param time 时间字符串或时间戳
   * @returns 相对时间描述
   */
  static getRelativeTime(time: string | number | Date): string {
    if (!time) return '-';
    try {
      const now = new Date().getTime();
      const targetTime = new Date(time).getTime();
      const diff = now - targetTime;
      
      const minute = 60 * 1000;
      const hour = 60 * minute;
      const day = 24 * hour;
      const week = 7 * day;
      const month = 30 * day;
      
      if (diff < minute) return '刚刚';
      if (diff < hour) return `${Math.floor(diff / minute)}分钟前`;
      if (diff < day) return `${Math.floor(diff / hour)}小时前`;
      if (diff < week) return `${Math.floor(diff / day)}天前`;
      if (diff < month) return `${Math.floor(diff / week)}周前`;
      return `${Math.floor(diff / month)}个月前`;
    } catch (error) {
      console.warn('相对时间计算失败:', error);
      return '-';
    }
  }
}

/**
 * 字符串工具函数
 */
export class StringUtils {
  /**
   * 截断字符串
   * @param str 原字符串
   * @param maxLength 最大长度
   * @param suffix 后缀，默认为...
   * @returns 截断后的字符串
   */
  static truncate(str: string, maxLength: number, suffix = '...'): string {
    if (!str || str.length <= maxLength) return str || '';
    return str.substring(0, maxLength - suffix.length) + suffix;
  }

  /**
   * 首字母大写
   * @param str 输入字符串
   * @returns 首字母大写的字符串
   */
  static capitalize(str: string): string {
    if (!str) return '';
    return str.charAt(0).toUpperCase() + str.slice(1);
  }

  /**
   * 驼峰转短横线
   * @param str 驼峰字符串
   * @returns 短横线字符串
   */
  static camelToKebab(str: string): string {
    return str.replace(/([A-Z])/g, '-$1').toLowerCase();
  }

  /**
   * 短横线转驼峰
   * @param str 短横线字符串
   * @returns 驼峰字符串
   */
  static kebabToCamel(str: string): string {
    return str.replace(/-([a-z])/g, (_, letter) => letter.toUpperCase());
  }
}

/**
 * 数字工具函数
 */
export class NumberUtils {
  /**
   * 格式化数字为千分位
   * @param num 数字
   * @returns 格式化后的字符串
   */
  static formatNumber(num: number): string {
    if (typeof num !== 'number' || isNaN(num)) return '0';
    return num.toLocaleString();
  }

  /**
   * 格式化文件大小
   * @param bytes 字节数
   * @returns 格式化后的文件大小
   */
  static formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  /**
   * 格式化金额
   * @param amount 金额
   * @param currency 货币符号
   * @returns 格式化后的金额
   */
  static formatCurrency(amount: number, currency = '¥'): string {
    if (typeof amount !== 'number' || isNaN(amount)) return `${currency}0.00`;
    return `${currency}${amount.toFixed(2)}`;
  }
}

/**
 * 验证工具函数
 */
export class ValidationUtils {
  /**
   * 验证邮箱格式
   * @param email 邮箱地址
   * @returns 是否有效
   */
  static isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  /**
   * 验证手机号格式
   * @param phone 手机号
   * @returns 是否有效
   */
  static isValidPhone(phone: string): boolean {
    const phoneRegex = /^1[3-9]\d{9}$/;
    return phoneRegex.test(phone);
  }

  /**
   * 验证URL格式
   * @param url URL地址
   * @returns 是否有效
   */
  static isValidUrl(url: string): boolean {
    try {
      new URL(url);
      return true;
    } catch {
      return false;
    }
  }

  /**
   * 验证密码强度
   * @param password 密码
   * @returns 强度等级 (0-3)
   */
  static getPasswordStrength(password: string): number {
    if (!password) return 0;
    
    let strength = 0;
    if (password.length >= 8) strength++;
    if (/[a-z]/.test(password)) strength++;
    if (/[A-Z]/.test(password)) strength++;
    if (/[0-9]/.test(password)) strength++;
    if (/[^A-Za-z0-9]/.test(password)) strength++;
    
    return Math.min(strength, 3);
  }
}

/**
 * 存储工具函数
 */
export class StorageUtils {
  /**
   * 设置localStorage
   * @param key 键
   * @param value 值
   */
  static setLocal(key: string, value: any): void {
    try {
      localStorage.setItem(key, JSON.stringify(value));
    } catch (error) {
      console.warn('localStorage设置失败:', error);
    }
  }

  /**
   * 获取localStorage
   * @param key 键
   * @returns 值
   */
  static getLocal<T = any>(key: string): T | null {
    try {
      const item = localStorage.getItem(key);
      return item ? JSON.parse(item) : null;
    } catch (error) {
      console.warn('localStorage获取失败:', error);
      return null;
    }
  }

  /**
   * 删除localStorage
   * @param key 键
   */
  static removeLocal(key: string): void {
    try {
      localStorage.removeItem(key);
    } catch (error) {
      console.warn('localStorage删除失败:', error);
    }
  }

  /**
   * 清空localStorage
   */
  static clearLocal(): void {
    try {
      localStorage.clear();
    } catch (error) {
      console.warn('localStorage清空失败:', error);
    }
  }
}
