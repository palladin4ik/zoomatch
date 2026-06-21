import { defineStore } from 'pinia'
import { login, register } from '../api/index.js'

export const useAuthStore = defineStore('auth', {
    state: () => ({
        access_token: localStorage.getItem('access_token') || null,
        refresh_token: localStorage.getItem('refresh_token') || null,
        isModerator: false,
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

        async registerAction(firstname, lastname, email, password) {
            await register({ firstname, lastname, email, password })
            await this.loginAction(email, password)
        },

        logout() {
            this.access_token = null
            this.refresh_token = null
            this.isModerator = false
            localStorage.removeItem('access_token')
            localStorage.removeItem('refresh_token')
        },

        setModerator(value) {
            this.isModerator = value
        }
    }
})
