<template>
  <div :class="['chat-list-item', { 'chat-list-item--unread': chat.unreadCount > 0 }]" @click="$emit('click')">
    <UiAvatar :src="avatarUrl" :name="chat.name" size="md" :badge="unreadBadge" />
    <div class="chat-list-item__content">
      <div class="chat-list-item__header">
        <h3 class="chat-list-item__name">{{ chat.name }}</h3>
      </div>
      <p :class="['chat-list-item__last-message', { 'chat-list-item__last-message--unread': chat.unreadCount > 0 }]">
        {{ lastMessageText }}
      </p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import UiAvatar from '../ui/UiAvatar.vue'

const props = defineProps({
  chat: { type: Object, required: true },
})

defineEmits(['click'])

const avatarUrl = computed(() => {
  if (!props.chat.avatar) return ''
  if (props.chat.avatar.startsWith('http')) return props.chat.avatar
  return `${import.meta.env.VITE_API_URL}${props.chat.avatar}`
})

const unreadBadge = computed(() => {
  return props.chat.unreadCount > 0 ? props.chat.unreadCount : ''
})

const lastMessageText = computed(() => {
  const msg = props.chat.lastMessageText
  if (!msg) return 'Нет сообщений'
  return msg
})
</script>

<style scoped>
.chat-list-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background var(--transition-fast);
  border-bottom: 1px solid var(--border-color);
}

.chat-list-item:last-child {
  border-bottom: none;
}

.chat-list-item:hover {
  background: var(--bg-hover);
}

.chat-list-item--unread {
  background: var(--purple-bg);
}

.chat-list-item--unread:hover {
  background: #EDE9FE;
}

.chat-list-item__content {
  flex: 1;
  min-width: 0;
}

.chat-list-item__header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 2px;
}

.chat-list-item__name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-list-item--unread .chat-list-item__name {
  font-weight: 700;
}

.chat-list-item__last-message {
  font-size: 13px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-list-item__last-message--unread {
  color: var(--text-primary);
  font-weight: 500;
}
</style>
