import { defineStore } from 'pinia'
import api from '../api/index.js'

export const useAnimalTypesStore = defineStore('animalTypes', {
  state: () => ({
    types: [],
    breeds: [],
    loading: false,
    error: null,
  }),

  actions: {
    async fetchTypes() {
      this.loading = true
      this.error = null
      try {
        const response = await api.get('/animal-type/')
        this.types = response.data.results ?? response.data
      } catch (e) {
        this.error = e.response?.data?.detail || 'Не удалось загрузить типы'
      } finally {
        this.loading = false
      }
    },

    async fetchBreeds(typeId) {
      this.loading = true
      this.error = null
      try {
        const params = typeId ? { animal_type: typeId } : {}
        const response = await api.get('/breed/', { params })
        this.breeds = response.data.results ?? response.data
      } catch (e) {
        this.error = e.response?.data?.detail || 'Не удалось загрузить породы'
      } finally {
        this.loading = false
      }
    },
  },
})
