import { defineStore } from 'pinia'
import api from '../api/index.js'

export const usePetsStore = defineStore('pets', {
  state: () => ({
    myPets: [],
    currentPet: null,
    loading: false,
    error: null,
  }),

  actions: {
    async fetchMyPets() {
      this.loading = true
      this.error = null
      try {
        const response = await api.get('/pets/me/')
        this.myPets = response.data.results ?? response.data
      } catch (e) {
        this.error = e.response?.data?.detail || 'Не удалось загрузить питомцев'
      } finally {
        this.loading = false
      }
    },

    async fetchPet(id) {
      this.loading = true
      this.error = null
      try {
        const response = await api.get(`/pets/${id}/`)
        this.currentPet = response.data
      } catch (e) {
        this.error = e.response?.data?.detail || 'Не удалось загрузить питомца'
      } finally {
        this.loading = false
      }
    },

    async createPet(data) {
      try {
        const response = await api.post('/pets/', data)
        this.myPets.unshift(response.data)
        return response.data
      } catch (e) {
        this.error = e.response?.data?.detail || 'Не удалось создать питомца'
        throw e
      }
    },

    async updatePet(id, data) {
      try {
        const response = await api.patch(`/pets/${id}/`, data)
        const index = this.myPets.findIndex(p => p.id === id)
        if (index !== -1) this.myPets[index] = response.data
        this.currentPet = response.data
        return response.data
      } catch (e) {
        this.error = e.response?.data?.detail || 'Не удалось обновить питомца'
        throw e
      }
    },

    async deletePet(id) {
      try {
        await api.delete(`/pets/${id}/`)
        this.myPets = this.myPets.filter(p => String(p.id) !== String(id))
      } catch (e) {
        this.error = e.response?.data?.detail || 'Не удалось удалить питомца'
        throw e
      }
    },

    async uploadDocument(petId, file) {
      try {
        const formData = new FormData()
        formData.append('pedigree_documents', file)
        const response = await api.patch(`/pets/${petId}/documents/`, formData, {
          headers: { 'Content-Type': 'multipart/form-data' },
        })
        const index = this.myPets.findIndex(p => p.id === petId)
        if (index !== -1) this.myPets[index] = response.data
        return response.data
      } catch (e) {
        this.error = e.response?.data?.detail || 'Не удалось загрузить документ'
        throw e
      }
    },

    async uploadAvatar(petId, file) {
      try {
        const formData = new FormData()
        formData.append('avatar', file)
        const response = await api.patch(`/pets/${petId}/avatar/`, formData, {
          headers: { 'Content-Type': 'multipart/form-data' },
        })
        const index = this.myPets.findIndex(p => p.id === petId)
        if (index !== -1) this.myPets[index] = response.data
        return response.data
      } catch (e) {
        this.error = e.response?.data?.detail || 'Не удалось загрузить аватар'
        throw e
      }
    },
  },
})
