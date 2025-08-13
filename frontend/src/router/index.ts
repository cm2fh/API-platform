import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import { RouteGuard } from '@/utils/auth'
import { UserRole } from '@/types'
import { message } from 'ant-design-vue'
import MainLayout from '@/views/MainLayout.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/user',
      name: 'user',
      component: () => import('@/views/UserLayout.vue'),
      meta: {
        title: '用户中心',
        hideInMenu: true
      },
      children: [
        {
          path: 'login',
          name: 'login',
          component: () => import('@/views/user/LoginView.vue'),
          meta: {
            title: '用户登录',
            hideInMenu: true
          }
        },
        {
          path: 'register',
          name: 'register',
          component: () => import('@/views/user/RegisterView.vue'),
          meta: {
            title: '用户注册',
            hideInMenu: true
          }
        }
      ]
    },
    {
      path: '/',
      component: MainLayout,
      meta: {
        requiresAuth: true,
        title: 'API开放平台'
      },
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('@/views/InterfaceListView.vue'),
          meta: {
            title: '接口市场',
            requiresAuth: true
          }
        },
        {
          path: 'interface/:id',
          name: 'interface-detail',
          component: () => import('@/views/InterfaceDetailView.vue'),
          props: true,
          meta: {
            title: '接口详情',
            requiresAuth: true,
            hideInMenu: true
          }
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('@/views/ProfileView.vue'),
          meta: {
            title: '个人中心',
            requiresAuth: true
          }
        },
        {
          path: 'admin',
          name: 'admin',
          component: () => import('@/views/AdminView.vue'),
          meta: {
            title: '后台管理',
            requiresAuth: true,
            requiredRole: UserRole.ADMIN
          }
        },
      ]
    },
    {
      path: '/403',
      name: 'forbidden',
      component: () => import('@/views/error/403.vue'),
      meta: {
        title: '权限不足',
        hideInMenu: true
      }
    },
    {
      path: '/404',
      name: 'not-found',
      component: () => import('@/views/error/404.vue'),
      meta: {
        title: '页面不存在',
        hideInMenu: true
      }
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/404'
    }
  ]
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - API开放平台`
  }

  // 如果用户未登录，尝试获取用户信息
  if (userStore.currentUser === null) {
    try {
      await userStore.fetchUser()
    } catch (error) {
      // 获取用户信息失败，清除可能存在的无效token
      console.log('获取用户信息失败:', error)
    }
  }

  // 提取路由权限配置
  const permission = RouteGuard.extractPermissionFromMeta(to.meta)

  // 检查权限
  const { hasPermission, redirectTo, reason } = RouteGuard.checkPermission(userStore.currentUser, permission)

  if (!hasPermission) {
    if (reason) {
      message.warning(reason)
    }

    // 如果是权限不足且不是去登录页，跳转到403页面
    if (redirectTo === '/' && to.path !== '/403') {
      next('/403')
      return
    }

    // 其他情况跳转到指定页面
    next(redirectTo || '/user/login')
    return
  }

  // 如果已登录用户访问登录/注册页，重定向到首页
  if ((to.name === 'login' || to.name === 'register') && userStore.currentUser) {
    next('/')
    return
  }

  next()
})

// 路由错误处理
router.onError((error) => {
  console.error('路由错误')
  message.error('页面加载失败，请刷新重试')
})

export default router
