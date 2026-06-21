import { defineStore } from 'pinia'
import api from '../api/index.js'

export const useMatchesStore = defineStore('matches', {
  state: () => ({
    matches: [],
    incomingRequests: [],
    loading: false,
    error: null,
  }),

  actions: {
    async fetchMatches() {
      this.loading = true
      this.error = null
      try {
        const response = await api.get('/matches/')
        this.matches = response.data.results ?? response.data
      } catch (e) {
        this.error = e.response?.data?.detail || 'Не удалось загрузить матчи'
      } finally {
        this.loading = false
      }
    },

    async fetchIncomingRequests() {
      this.loading = true
      this.error = null
      try {
        const response = await api.get('/matches/', { params: { type: 'received' } })
        this.incomingRequests = response.data.results ?? response.data
      } catch (e) {
        this.error = e.response?.data?.detail || 'Не удалось загрузить запросы'
      } finally {
        this.loading = false
      }
    },

    async createMatch(petFromId, petToId) {
      try {
        const response = await api.post('/matches/', {
          pet_from: petFromId,
          pet_to: petToId,
        })
        return response.data
      } catch (e) {
        this.error = e.response?.data?.detail || 'Не удалось отправить запрос'
        throw e
      }
    },

    async acceptMatch(matchId) {
      try {
        const response = await api.patch(`/matches/${matchId}/`, { status: 1 })
        await this.fetchIncomingRequests()
        return response.data
      } catch (e) {
        this.error = e.response?.data?.detail || 'Не удалось принять запрос'
        throw e
      }
    },

    async rejectMatch(matchId) {
      try {
        const response = await api.patch(`/matches/${matchId}/`, { status: 2 })
        await this.fetchIncomingRequests()
        return response.data
      } catch (e) {
        this.error = e.response?.data?.detail || 'Не удалось отклонить запрос'
        throw e
      }
    },
  },
})
