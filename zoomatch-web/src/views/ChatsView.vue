<template>
  <div class="chats-view">
    <div class="chats-view__header">
      <h1 class="chats-view__title">Чаты</h1>
      <span v-if="chatsStore.totalUnread > 0" class="chats-view__unread-badge">
        {{ chatsStore.totalUnread }}
      </span>
    </div>

    <UiLoader v-if="chatsStore.loading" text="Загрузка чатов..." />

    <div v-else-if="chatsStore.error" class="chats-view__error">
      <p>{{ chatsStore.error }}</p>
      <UiButton variant="secondary" @click="loadData">Повторить</UiButton>
    </div>

    <div v-else-if="chatsStore.chats.length === 0" class="chats-view__empty">
      <UiEmptyState
        title="Пока нет чатов"
        description="Начните общение после взаимной симпатии"
      />
    </div>

    <div v-else class="chats-view__list">
      <ChatListItem
        v-for="chat in chatsStore.chats"
        :key="chat.interlocutorId"
        :chat="chat"
        @click="$router.push(`/chats/${chat.interlocutorId}`)"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '../stores/user.js'
import { useChatsStore } from '../stores/chats.js'
import UiLoader from '../components/ui/UiLoader.vue'
import UiButton from '../components/ui/UiButton.vue'
import UiEmptyState from '../components/ui/UiEmptyState.vue'
import ChatListItem from '../components/chats/ChatListItem.vue'

const userStore = useUserStore()
const chatsStore = useChatsStore()

async function loadData() {
  await userStore.fetchProfile()
  await chatsStore.fetchChats()
}

onMounted(loadData)
</script>

<style scoped>
.chats-view {
  max-width: 600px;
  margin: 0 auto;
}

.chats-view__header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.chats-view__title {
  font-size: 24px;
  font-weight: 700;
}

.chats-view__unread-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 22px;
  height: 22px;
  padding: 0 6px;
  background: var(--red-accent);
  color: var(--text-white);
  font-size: 12px;
  font-weight: 600;
  border-radius: var(--radius-full);
}

.chats-view__list {
  display: flex;
  flex-direction: column;
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  box-shadow: var(--card-shadow);
  overflow: hidden;
}

.chats-view__empty {
  padding: 40px;
}

.chats-view__error {
  text-align: center;
  padding: 40px;
  color: var(--red-accent);
}
</style>
