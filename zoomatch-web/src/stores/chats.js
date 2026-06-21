import { defineStore } from 'pinia'
import api from '../api/index.js'

export const useChatsStore = defineStore('chats', {
  state: () => ({
    chats: [],
    loading: false,
    error: null,
  }),

  getters: {
    totalUnread: (state) => state.chats.reduce((sum, c) => sum + (c.unreadCount || 0), 0),
  },

  actions: {
    async fetchChats() {
      this.loading = true
      this.error = null
      try {
        const response = await api.get('/chats/')
        const raw = response.data.results ?? response.data
        this.chats = raw.map(c => ({
          interlocutorId: c.user.id,
          name: [c.user.firstname, c.user.lastname].filter(Boolean).join(' ') || `User ${c.user.id}`,
          avatar: c.user.avatar,
          lastMessageText: c.last_message_text,
          unreadCount: c.unread_count || 0,
        }))
      } catch (e) {
        this.error = e.response?.data?.detail || 'Не удалось загрузить чаты'
      } finally {
        this.loading = false
      }
    },

    updateChat(interlocutorId, data) {
      const index = this.chats.findIndex(c => c.interlocutorId === interlocutorId)
      if (index !== -1) {
        this.chats[index] = { ...this.chats[index], ...data }
      }
    },

    markRead(interlocutorId) {
      this.updateChat(interlocutorId, { unreadCount: 0 })
    },
  },
})
