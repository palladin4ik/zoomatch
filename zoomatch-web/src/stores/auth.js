import { defineStore } from 'pinia'
import { login } from '../api/index.js'

export const useAuthStore = defineStore('auth', {
    state: () => ({
        access_token: localStorage.getItem('access_token') || null,
        refresh_token: localStorage.getItem('refresh_token') || null,
    }),

    getters: {
        isAuthenticated: (state) => !!state.access_token,
    },

    actions: {
        async loginAction(email, password) {
            const response = await login(email, password)
            
            this.access_token = response.data.access
            this.refresh_token = response.data.refresh

            localStorage.setItem('access_token', this.access_token)
            localStorage.setItem('refresh_token', this.refresh_token)
        },

        logout() {
            this.access_token = null
            this.refresh_token = null
            localStorage.removeItem('access_token')
            localStorage.removeItem('refresh_token')
        }
    }
})
