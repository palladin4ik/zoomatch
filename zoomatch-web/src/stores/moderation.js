import { defineStore } from 'pinia'
import {
    getModerationRequests,
    approveRequest,
    rejectRequest,
    getAnimalTypes,
    createAnimalType,
    deleteAnimalType,
    getBreeds,
    createBreed,
    deleteBreed,
} from '../api/index.js'

export const useModerationStore = defineStore('moderation', {
    state: () => ({
        requests: [],
        animalTypes: [],
        breeds: [],
        loading: false,
        error: null,
    }),

    actions: {
        async fetchRequests() {
            this.loading = true
            this.error = null
            try {
                const response = await getModerationRequests()
                this.requests = response.data.results ?? response.data
            } catch (e) {
                this.error = e.response?.data?.detail || 'Не удалось загрузить заявки'
            } finally {
                this.loading = false
            }
        },

        async approve(id) {
            try {
                await approveRequest(id)
                this.requests = this.requests.filter(r => r.id !== id)
            } catch (e) {
                this.error = e.response?.data?.detail || 'Не удалось одобрить заявку'
                throw e
            }
        },

        async reject(id) {
            try {
                await rejectRequest(id)
                this.requests = this.requests.filter(r => r.id !== id)
            } catch (e) {
                this.error = e.response?.data?.detail || 'Не удалось отклонить заявку'
                throw e
            }
        },

        async fetchAnimalTypes() {
            this.loading = true
            this.error = null
            try {
                const response = await getAnimalTypes()
                this.animalTypes = response.data.results ?? response.data
            } catch (e) {
                this.error = e.response?.data?.detail || 'Не удалось загрузить виды'
            } finally {
                this.loading = false
            }
        },

        async addAnimalType(data) {
            try {
                const response = await createAnimalType(data)
                this.animalTypes.push(response.data)
                return response.data
            } catch (e) {
                this.error = e.response?.data?.detail || 'Не удалось создать вид'
                throw e
            }
        },

        async removeAnimalType(id) {
            try {
                await deleteAnimalType(id)
                this.animalTypes = this.animalTypes.filter(t => t.id !== id)
            } catch (e) {
                this.error = e.response?.data?.detail || 'Не удалось удалить вид'
                throw e
            }
        },

        async fetchBreeds() {
            this.loading = true
            this.error = null
            try {
                const response = await getBreeds()
                this.breeds = response.data.results ?? response.data
            } catch (e) {
                this.error = e.response?.data?.detail || 'Не удалось загрузить породы'
            } finally {
                this.loading = false
            }
        },

        async addBreed(data) {
            try {
                const response = await createBreed(data)
                this.breeds.push(response.data)
                return response.data
            } catch (e) {
                this.error = e.response?.data?.detail || 'Не удалось создать породу'
                throw e
            }
        },

        async removeBreed(id) {
            try {
                await deleteBreed(id)
                this.breeds = this.breeds.filter(b => b.id !== id)
            } catch (e) {
                this.error = e.response?.data?.detail || 'Не удалось удалить породу'
                throw e
            }
        },
    },
})
