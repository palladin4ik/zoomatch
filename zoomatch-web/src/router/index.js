import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

import LoginView from '../views/LoginView.vue'
import ModerationView from '../views/ModerationView.vue'

const routes = [
    {
        path: '/login',
        name: 'login',
        component: LoginView,
    },
    {
        path: '/moderation',
        name: 'moderation',
        component: ModerationView,
        meta: { requiresAuth: true },  // защищённый маршрут
    },
    {
        path: '/',
        redirect: '/moderation',  // по умолчанию редиректим на модерацию
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

// Перед каждым переходом проверяем авторизацию
router.beforeEach((to, from, next) => {
    const authStore = useAuthStore()

    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
        next('/login')  // не авторизован — на логин
    } else {
        next()  // всё хорошо — пропускаем
    }
})

export default router