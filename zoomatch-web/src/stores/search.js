import { defineStore } from 'pinia'
import api from '../api/index.js'

export const useSearchStore = defineStore('search', {
  state: () => ({
    recommendations: [],
    selectedPetId: null,
    currentIndex: 0,
    loading: false,
    error: null,
    filters: {
      radius_km: 150,
      requires_pedigree: false,
      min_age: null,
      max_age: null,
      max_months_since_mating: null,
    },
  }),

  actions: {
    async fetchRecommendations(petId) {
      this.loading = true
      this.error = null
      this.selectedPetId = petId
      this.currentIndex = 0
      try {
        const params = {
          pet_id: petId,
          ...this.filters,
        }
        Object.keys(params).forEach(key => {
          if (params[key] === null || params[key] === '') delete params[key]
        })
        const response = await api.get('/recommend/recommend/', { params })
        this.recommendations = response.data.results ?? response.data
      } catch (e) {
        this.error = e.response?.data?.detail || 'Не удалось загрузить рекомендации'
      } finally {
        this.loading = false
      }
    },
  },
})
