import { ref } from 'vue'

export function useGeolocation() {
  const latitude = ref(null)
  const longitude = ref(null)
  const error = ref(null)
  const loading = ref(false)

  function request() {
    if (!navigator.geolocation) {
      error.value = 'Геолокация не поддерживается'
      return
    }

    loading.value = true
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        latitude.value = pos.coords.latitude
        longitude.value = pos.coords.longitude
        loading.value = false
      },
      (err) => {
        error.value = err.message
        loading.value = false
      },
      { enableHighAccuracy: true, timeout: 10000 }
    )
  }

  return { latitude, longitude, error, loading, request }
}
