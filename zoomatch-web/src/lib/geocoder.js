const cache = new Map()

export async function reverseGeocode(lat, lng) {
  const key = `${lat.toFixed(5)},${lng.toFixed(5)}`
  if (cache.has(key)) return cache.get(key)

  try {
    const apiKey = import.meta.env.VITE_YANDEX_MAPS_API_KEY
    const url = `https://geocode-maps.yandex.ru/1.x/?apikey=${apiKey}&geocode=${lng},${lat}&format=json&results=1&lang=ru_RU`
    const res = await fetch(url)
    if (!res.ok) throw new Error(`Geocoder HTTP ${res.status}`)

    const data = await res.json()
    const featureMember = data.response?.GeoObjectCollection?.featureMember
    if (!featureMember || featureMember.length === 0) return null

    const geoObj = featureMember[0].GeoObject
    const address = geoObj?.metaDataProperty?.GeocoderMetaData?.text || null

    cache.set(key, address)
    return address
  } catch (e) {
    console.warn('Reverse geocode failed:', e)
    return null
  }
}