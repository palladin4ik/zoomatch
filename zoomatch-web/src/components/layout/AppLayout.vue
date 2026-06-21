<template>
  <div class="app-layout">
    <Sidebar
      :collapsed="sidebarCollapsed"
      :user-avatar="userStore.avatarUrl"
      :user-name="userStore.fullName"
      :user-email="userStore.email"
    />
    <div class="app-layout__main" :class="{ 'app-layout__main--collapsed': sidebarCollapsed }">
      <header class="app-layout__header">
        <button class="app-layout__menu-btn" @click="sidebarCollapsed = !sidebarCollapsed">
          <UiIcon :name="sidebarCollapsed ? 'menu' : 'close'" />
        </button>
        <h1 class="app-layout__title">{{ pageTitle }}</h1>
        <div class="app-layout__header-actions">
          <slot name="header-actions" />
        </div>
      </header>
      <main class="app-layout__content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from './Sidebar.vue'
import UiIcon from '../ui/UiIcon.vue'
import { useUserStore } from '../../stores/user.js'

const route = useRoute()
const userStore = useUserStore()
const sidebarCollapsed = ref(false)

const pageTitle = computed(() => {
  const titles = {
    home: 'Главная',
    search: 'Поиск',
    match: 'Поиск',
    chats: 'Чаты',
    messages: 'Чат',
    pets: 'Мои питомцы',
    'pet-create': 'Новый питомец',
    'pet-edit': 'Редактирование',
    'pet-detail': 'Питомец',
    profile: 'Профиль',
    'user-profile': 'Профиль',
    settings: 'Настройки',
    moderation: 'Модерация',
  }
  return titles[route.name] || ''
})
</script>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
}

.app-layout__main {
  flex: 1;
  margin-left: var(--sidebar-width);
  transition: margin-left var(--transition-normal);
  display: flex;
  flex-direction: column;
}

.app-layout__main--collapsed {
  margin-left: var(--sidebar-collapsed);
}

.app-layout__header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 24px;
  background: var(--bg-white);
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 40;
}

.app-layout__menu-btn {
  display: none;
  background: none;
  border: none;
  font-size: 20px;
  color: var(--text-primary);
  padding: 4px 8px;
}

.app-layout__title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}

.app-layout__header-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-layout__content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

@media (max-width: 768px) {
  .app-layout__menu-btn {
    display: block;
  }

  .app-layout__main {
    margin-left: 0;
  }

  .app-layout__main--collapsed {
    margin-left: 0;
  }
}
</style>
