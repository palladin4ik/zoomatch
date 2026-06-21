import { defineStore } from 'pinia'
import api from '../api/index.js'

export const useMessagesStore = defineStore('messages', {
  state: () => ({
    messages: [],
    loading: false,
    editingMessage: null,
    error: null,
  }),

  actions: {
    async fetchMessages(receiverId) {
      this.loading = true
      this.error = null
      try {
        const response = await api.get('/messages/', { params: { receiver: receiverId } })
        this.messages = (response.data.results ?? response.data).slice().reverse()
        const nextUrl = response.data.next
        if (!nextUrl) return null
        try { return new URL(nextUrl).searchParams.get('cursor') } catch { return null }
      } catch (e) {
        this.error = e.response?.data?.detail || 'Не удалось загрузить сообщения'
        return null
      } finally {
        this.loading = false
      }
    },

    async sendMessage(text, receiverId) {
      try {
        const response = await api.post('/messages/', {
          text,
          receiver_id: receiverId,
        })
        this.messages.push(response.data)
        return response.data
      } catch (e) {
        this.error = e.response?.data?.detail || 'Не удалось отправить сообщение'
        throw e
      }
    },

    async editMessage(id, text) {
      try {
        const response = await api.patch(`/messages/${id}/`, { text })
        const index = this.messages.findIndex(m => m.id === id)
        if (index !== -1) this.messages[index] = response.data
        this.editingMessage = null
        return response.data
      } catch (e) {
        this.error = e.response?.data?.detail || 'Не удалось изменить сообщение'
        throw e
      }
    },

    async deleteMessage(id) {
      try {
        await api.delete(`/messages/${id}/`)
        this.messages = this.messages.filter(m => m.id !== id)
      } catch (e) {
        this.error = e.response?.data?.detail || 'Не удалось удалить сообщение'
        throw e
      }
    },

    async markAsRead(messageId) {
      try {
        await api.post(`/messages/${messageId}/read/`)
        const index = this.messages.findIndex(m => m.id === messageId)
        if (index !== -1) {
          this.messages[index] = { ...this.messages[index], is_read: true }
        }
      } catch {
        // best effort
      }
    },

    addMessage(message) {
      const exists = this.messages.find(m => m.id === message.id)
      if (!exists) this.messages.push(message)
    },

    updateMessage(id, data) {
      const index = this.messages.findIndex(m => m.id === id)
      if (index !== -1) this.messages[index] = { ...this.messages[index], ...data }
    },

    setEditing(message) {
      this.editingMessage = message
    },

    clearEditing() {
      this.editingMessage = null
    },
  },
})
