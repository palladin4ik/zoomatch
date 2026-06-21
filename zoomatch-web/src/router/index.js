import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

import AppLayout from '../components/layout/AppLayout.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'

const routes = [
    {
        path: '/login',
        name: 'login',
        component: LoginView,
    },
    {
        path: '/register',
        name: 'register',
        component: RegisterView,
    },
    {
        path: '/',
        component: AppLayout,
        meta: { requiresAuth: true },
        children: [
            {
                path: '',
                name: 'home',
                component: () => import('../views/HomeView.vue'),
            },
            {
                path: 'search',
                name: 'search',
                component: () => import('../views/SearchView.vue'),
            },
            {
                path: 'requests',
                name: 'requests',
                component: () => import('../views/RequestsView.vue'),
            },
            {
                path: 'chats',
                name: 'chats',
                component: () => import('../views/ChatsView.vue'),
            },
            {
                path: 'chats/:interlocutorId',
                name: 'messages',
                component: () => import('../views/MessagesView.vue'),
            },
            {
                path: 'pets',
                name: 'pets',
                component: () => import('../views/PetsView.vue'),
            },
            {
                path: 'pets/new',
                name: 'pet-create',
                component: () => import('../views/PetEditView.vue'),
            },
            {
                path: 'pets/:id',
                name: 'pet-detail',
                component: () => import('../views/PetDetailView.vue'),
            },
            {
                path: 'pets/:id/edit',
                name: 'pet-edit',
                component: () => import('../views/PetEditView.vue'),
            },
            {
                path: 'profile',
                name: 'profile',
                component: () => import('../views/ProfileView.vue'),
            },
            {
                path: 'profile/edit',
                name: 'edit-profile',
                component: () => import('../views/EditProfileView.vue'),
            },
            {
                path: 'users/:id',
                name: 'user-profile',
                component: () => import('../views/UserProfileView.vue'),
            },
            {
                path: 'settings',
                name: 'settings',
                component: () => import('../views/SettingsView.vue'),
            },
            {
                path: 'moderation',
                name: 'moderation',
                component: () => import('../views/ModerationView.vue'),
            },
        ],
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'not-found',
        component: () => import('../views/NotFoundView.vue'),
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

router.beforeEach((to, from, next) => {
    const authStore = useAuthStore()

    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
        next('/login')
    } else if (to.name === 'login' && authStore.isAuthenticated) {
        next('/')
    } else if (to.name === 'moderation' && !authStore.isModerator) {
        next('/')
    } else {
        next()
    }
})

export default router
