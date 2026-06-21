import axios from 'axios'

const api = axios.create({
    baseURL: (import.meta.env.VITE_API_URL || '') + '/api/v1/',
})

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('access_token')
    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})

api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('access_token')
            localStorage.removeItem('refresh_token')
            window.location.href = '/login'
        }
        return Promise.reject(error)
    }
)

export const login = (email, password) =>
    api.post('/jwt/create/', { email, password })

export const register = (data) =>
    api.post('/register/', data)

export const getModerationRequests = () =>
    api.get('moderation/')

export const approveRequest = (id) =>
    api.post(`moderation/${id}/approve/`)

export const rejectRequest = (id) =>
    api.post(`moderation/${id}/reject/`)

export const getAnimalTypes = () =>
    api.get('animal-type/')

export const createAnimalType = (data) =>
    api.post('animal-type/', data)

export const deleteAnimalType = (id) =>
    api.delete(`animal-type/${id}/`)

export const getBreeds = () =>
    api.get('breed/')

export const createBreed = (data) =>
    api.post('breed/', data)

export const deleteBreed = (id) =>
    api.delete(`breed/${id}/`)

export const getUser = (id) =>
    api.get(`users/${id}/`)

export const getMessages = (params) =>
    api.get('messages/', { params })

export const uploadMessageMedia = (messageId, file) => {
    const formData = new FormData()
    formData.append('media', file)
    return api.post(`messages/${messageId}/media/`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
    })
}

export const changePassword = (data) =>
    api.post('users/change_password/', data)

export const deleteMe = () =>
    api.delete('users/me/')

export const uploadDocument = (petId, file) => {
    const formData = new FormData()
    formData.append('pedigree_documents', file)
    return api.patch(`pets/${petId}/documents/`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
    })
}

export default api
