import axios from 'axios'

const api = axios.create({
    baseURL: 'http://localhost:8000/api/v1/',
})

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('access_token')
    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})

export const login = (email, password) =>
    api.post('/jwt/create/', { email, password })

export const getModerationRequests = () =>
    api.get('moderation/')

export const approveRequest = (id) =>
    api.post(`moderation/${id}/approve/`)

export const rejectRequest = (id) =>
    api.post(`moderation/${id}/reject/`)

export default api
