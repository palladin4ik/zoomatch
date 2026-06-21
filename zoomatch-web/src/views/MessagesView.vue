<template>
  <div class="messages-view">
    <div class="messages-view__header">
      <router-link to="/chats" class="messages-view__back">&larr;</router-link>
      <UiAvatar :src="interlocutorAvatar" :name="interlocutorName" size="sm" />
      <div class="messages-view__interlocutor">
        <h2 class="messages-view__name">{{ interlocutorName }}</h2>
        <span v-if="wsConnected" class="messages-view__status messages-view__status--online">в сети</span>
        <span v-else class="messages-view__status">не в сети</span>
      </div>
    </div>

    <div ref="messagesContainer" class="messages-view__messages" @scroll="handleScroll">
      <UiLoader v-if="messagesStore.loading && !loadingMore" text="Загрузка..." />
      <template v-else>
        <MessageBubble
          v-for="msg in messagesStore.messages"
          :key="msg.id"
          :message="msg"
          :is-outgoing="msg.sender?.id === currentUserId || msg.sender_id === currentUserId"
          @download="handleDownload"
          @edit="handleEdit"
          @delete="handleDelete"
        />
      </template>
    </div>

    <MessageInput
      v-model="newMessage"
      :editing-message="messagesStore.editingMessage"
      :uploading="uploadingFile"
      @send="handleSend"
      @attach="handleAttach"
      @cancel-edit="messagesStore.clearEditing()"
    />

    <input ref="fileInput" type="file" class="messages-view__file-input" @change="handleFileUpload" />

    <UiModal v-model="showDeleteModal" title="Удалить сообщение?" size="sm">
      <p>Сообщение будет удалено для вас.</p>
      <template #footer>
        <UiButton variant="ghost" @click="showDeleteModal = false">Отмена</UiButton>
        <UiButton variant="danger" :loading="deleting" @click="confirmDelete">Удалить</UiButton>
      </template>
    </UiModal>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick, inject } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { useUserStore } from '../stores/user.js'
import { useMessagesStore } from '../stores/messages.js'
import { useChatsStore } from '../stores/chats.js'
import { useWebSocket } from '../composables/useWebSocket.js'
import { getMessages, uploadMessageMedia, getUser } from '../api/index.js'
import UiAvatar from '../components/ui/UiAvatar.vue'
import UiLoader from '../components/ui/UiLoader.vue'
import UiModal from '../components/ui/UiModal.vue'
import UiButton from '../components/ui/UiButton.vue'
import MessageBubble from '../components/chats/MessageBubble.vue'
import MessageInput from '../components/chats/MessageInput.vue'

const route = useRoute()
const authStore = useAuthStore()
const userStore = useUserStore()
const messagesStore = useMessagesStore()
const chatsStore = useChatsStore()
const toast = inject('toast')

const interlocutorId = computed(() => Number(route.params.interlocutorId))
const messagesContainer = ref(null)
const fileInput = ref(null)
const newMessage = ref('')
const hasMore = ref(true)
const loadingMore = ref(false)
const uploadingFile = ref(false)
const nextCursor = ref(null)
const interlocutorInfo = ref(null)
const showDeleteModal = ref(false)
const deleteTarget = ref(null)
const deleting = ref(false)

const interlocutorChat = computed(() =>
  chatsStore.chats.find(c => c.interlocutorId === interlocutorId.value)
)

const interlocutorName = computed(() => {
  if (interlocutorChat.value?.name) return interlocutorChat.value.name
  if (interlocutorInfo.value) {
    return [interlocutorInfo.value.firstname, interlocutorInfo.value.lastname].filter(Boolean).join(' ') || 'Пользователь'
  }
  return 'Пользователь'
})

const interlocutorAvatar = computed(() => {
  const avatar = interlocutorChat.value?.avatar || interlocutorInfo.value?.avatar
  if (!avatar) return ''
  if (avatar.startsWith('http')) return avatar
  return `${import.meta.env.VITE_API_URL}${avatar}`
})

const currentUserId = computed(() => userStore.id)

const { connected: wsConnected, lastMessage, connect, disconnect, send: wsSend, sendRead, sendDelivered } = useWebSocket(
  () => interlocutorId.value,
  () => authStore.access_token
)

async function scrollToBottom() {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

async function loadMessages() {
  nextCursor.value = null
  const cursor = await messagesStore.fetchMessages(interlocutorId.value)
  nextCursor.value = cursor
  hasMore.value = !!cursor
  scrollToBottom()
  markUnreadAsRead()
}

function extractCursor(url) {
  if (!url) return null
  try { return new URL(url).searchParams.get('cursor') } catch { return null }
}

async function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  try {
    const params = { receiver: interlocutorId.value }
    if (nextCursor.value) params.cursor = nextCursor.value
    const response = await getMessages(params)
    const data = response.data
    const newMessages = data.results ?? data
    nextCursor.value = extractCursor(data.next)
    hasMore.value = !!data.next
    if (newMessages.length > 0) {
      messagesStore.messages.unshift(...newMessages.slice().reverse())
    }
  } catch {
    // error handled by interceptor
  } finally {
    loadingMore.value = false
  }
}

function handleScroll() {
  const el = messagesContainer.value
  if (el && el.scrollTop < 50 && hasMore.value && !loadingMore.value) {
    loadMore()
  }
}

function markUnreadAsRead() {
  const myId = currentUserId.value
  if (!myId) return
  messagesStore.messages.forEach(msg => {
    const senderId = msg.sender?.id ?? msg.sender_id
    if (senderId !== myId && !msg.is_read) {
      messagesStore.markAsRead(msg.id)
      sendRead(msg.id)
    }
  })
}

async function handleSend() {
  const text = newMessage.value.trim()
  if (!text) return

  if (messagesStore.editingMessage) {
    try {
      await messagesStore.editMessage(messagesStore.editingMessage.id, text)
      newMessage.value = ''
      messagesStore.clearEditing()
    } catch {
      toast('Не удалось изменить сообщение', 'error')
    }
    return
  }

  const receiverId = interlocutorId.value

  if (wsSend({ type: 'message', text, receiver_id: receiverId })) {
    newMessage.value = ''
    scrollToBottom()
  } else {
    try {
      await messagesStore.sendMessage(text, receiverId)
      newMessage.value = ''
      scrollToBottom()
    } catch {
      toast('Не удалось отправить сообщение', 'error')
    }
  }
}

function handleAttach() {
  fileInput.value?.click()
}

async function handleFileUpload(e) {
  const file = e.target.files?.[0]
  if (!file) return

  if (file.size > 50 * 1024 * 1024) {
    toast('Файл слишком большой. Максимум: 50 МБ', 'error')
    if (fileInput.value) fileInput.value.value = ''
    return
  }

  uploadingFile.value = true
  try {
    const tempMsg = await messagesStore.sendMessage(file.name, interlocutorId.value)
    const uploadResponse = await uploadMessageMedia(tempMsg.id, file)
    messagesStore.updateMessage(tempMsg.id, { media: uploadResponse.data.media })
    wsSend({ type: 'media_message', message_id: tempMsg.id })
  } catch {
    toast('Ошибка загрузки файла', 'error')
  } finally {
    uploadingFile.value = false
  }

  if (fileInput.value) fileInput.value.value = ''
}

function handleDownload(message) {
  const url = message.media?.startsWith('http')
    ? message.media
    : `${import.meta.env.VITE_API_URL}${message.media}`
  window.open(url, '_blank')
}

function handleEdit(message) {
  messagesStore.setEditing(message)
  newMessage.value = message.text || ''
}

function handleDelete(message) {
  deleteTarget.value = message
  showDeleteModal.value = true
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await messagesStore.deleteMessage(deleteTarget.value.id)
    showDeleteModal.value = false
    toast?.success('Сообщение удалено')
  } catch {
    toast?.error('Не удалось удалить сообщение')
  } finally {
    deleting.value = false
  }
}

watch(lastMessage, (msg) => {
  if (!msg) return
  if (msg.type === 'message') {
    const messageData = {
      id: msg.message_id,
      text: msg.text,
      sender: { id: msg.sender_id },
      receiver: { id: currentUserId.value },
      created_at: msg.created_at,
      is_read: false,
      is_delivered: false,
    }
    if (msg.has_media && msg.media_url) {
      messageData.media = msg.media_url
    }
    messagesStore.addMessage(messageData)
    if (msg.sender_id !== currentUserId.value) {
      messagesStore.markAsRead(msg.message_id)
      sendRead(msg.message_id)
    }
    scrollToBottom()
  } else if (msg.type === 'read') {
    messagesStore.updateMessage(msg.message_id, { is_read: true })
  } else if (msg.type === 'delivered') {
    messagesStore.updateMessage(msg.message_id, { is_delivered: true })
  }
})

onMounted(async () => {
  await userStore.fetchProfile()
  if (!interlocutorChat.value) {
    try {
      const response = await getUser(interlocutorId.value)
      interlocutorInfo.value = response.data
    } catch {
      // fallback to default name
    }
  }
  connect()
  loadMessages()
})
</script>

<style scoped>
.messages-view {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
}

.messages-view__header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 12px;
}

.messages-view__back {
  font-size: 22px;
  color: var(--text-primary);
  text-decoration: none;
}

.messages-view__interlocutor {
  display: flex;
  flex-direction: column;
}

.messages-view__name {
  font-size: 16px;
  font-weight: 600;
}

.messages-view__status {
  font-size: 12px;
  color: var(--text-muted);
}

.messages-view__status--online {
  color: var(--green-accent);
}

.messages-view__messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px 0;
  display: flex;
  flex-direction: column;
}

.messages-view__file-input {
  display: none;
}
</style>
