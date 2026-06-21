import { defineStore } from 'pinia'
import api from '../api/index.js'
import { useAuthStore } from './auth.js'

export const useUserStore = defineStore('user', {
  state: () => ({
    id: null,
    email: '',
    firstname: '',
    lastname: '',
    avatar: null,
    description: '',
    phone_number: '',
    isModerator: false,
    error: null,
  }),

  getters: {
    fullName: (state) => `${state.firstname} ${state.lastname}`.trim(),
    avatarUrl: (state) => {
      if (!state.avatar) return ''
      if (state.avatar.startsWith('http')) return state.avatar
      return `${import.meta.env.VITE_API_URL}${state.avatar}`
    },
  },

  actions: {
    async fetchProfile() {
      const authStore = useAuthStore()
      this.error = null
      try {
        const response = await api.get('/me/')
        const data = response.data
        this.id = data.id
        this.email = data.email
        this.firstname = data.firstname
        this.lastname = data.lastname
        this.avatar = data.avatar
        this.description = data.description || ''
        this.phone_number = data.phone_number || ''
        this.isModerator = data.role === 2 || data.is_staff
        authStore.setModerator(data.role === 2 || data.is_staff)
      } catch (e) {
        this.error = e.response?.data?.detail || 'Не удалось загрузить профиль'
      }
    },

    async updateProfile(fields) {
      try {
        const response = await api.patch('/me/', fields)
        const data = response.data
        this.firstname = data.firstname
        this.lastname = data.lastname
        this.email = data.email
        this.description = data.description || ''
        this.phone_number = data.phone_number || ''
      } catch (e) {
        this.error = e.response?.data?.detail || 'Не удалось обновить профиль'
        throw e
      }
    },

    async uploadAvatar(file) {
      try {
        const formData = new FormData()
        formData.append('avatar', file)
        const response = await api.patch('/me/avatar/', formData, {
          headers: { 'Content-Type': 'multipart/form-data' },
        })
        this.avatar = response.data.avatar
      } catch (e) {
        this.error = e.response?.data?.detail || 'Не удалось загрузить аватар'
        throw e
      }
    },

    clear() {
      this.id = null
      this.email = ''
      this.firstname = ''
      this.lastname = ''
      this.avatar = null
      this.description = ''
      this.phone_number = ''
      this.isModerator = false
      this.error = null
    },
  },
})
