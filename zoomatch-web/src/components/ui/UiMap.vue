<template>
  <div class="ui-map">
    <div class="ui-map__container">
      <YMap
        v-if="ymapReady"
        :location="mapLocation"
        :behaviors="readonlyBehaviors"
      >
        <YMapDefaultSchemeLayer />
        <YMapDefaultFeaturesLayer />

        <YMapListener
          v-if="!props.readonly"
          layer="any"
          :onClick="onMapClick"
          :onUpdate="onMapUpdate"
        />

        <YMapMarker
          :coordinates="[markerLng, markerLat]"
          :draggable="!props.readonly"
          @dragend="onDragEnd"
        >
          <div v-if="hasCoords" class="marker-root">
            <div v-if="props.address" class="marker-label">{{ props.address }}</div>
            <svg width="28" height="36" viewBox="0 0 28 36">
              <path d="M14 0C6.27 0 0 6.27 0 14c0 10.5 14 22 14 22s14-11.5 14-22C28 6.27 21.73 0 14 0z" fill="#E53935"/>
              <circle cx="14" cy="14" r="6" fill="white"/>
            </svg>
          </div>
        </YMapMarker>
      </YMap>

      <UiLoader v-if="!ymapReady && !loadError" text="Загрузка карты..." />
      <div v-if="loadError" class="ui-map__error">
        <UiIcon name="location" />
        <span>{{ loadError }}</span>
      </div>
    </div>
    <button v-if="!props.readonly" class="ui-map__locate" type="button" @click="locateMe">
      <UiIcon name="location" size="sm" /> Мое местоположение
    </button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import UiIcon from './UiIcon.vue'
import UiLoader from './UiLoader.vue'

const DEFAULT_LAT = 55.7558
const DEFAULT_LNG = 37.6173

const props = defineProps({
  latitude: { type: Number, default: null },
  longitude: { type: Number, default: null },
  address: { type: String, default: '' },
  zoom: { type: Number, default: 12 },
  readonly: { type: Boolean, default: false },
})

const emit = defineEmits(['update:latitude', 'update:longitude'])

const ymapReady = ref(false)
const loadError = ref('')

const mapLocation = ref({
  center: [DEFAULT_LNG, DEFAULT_LAT],
  zoom: props.zoom,
})

const markerLat = ref(DEFAULT_LAT)
const markerLng = ref(DEFAULT_LNG)

const actualZoom = ref(props.zoom)
let updatingFromClick = false

let YMap = null
let YMapDefaultSchemeLayer = null
let YMapDefaultFeaturesLayer = null
let YMapMarker = null
let YMapListener = null

const hasCoords = computed(() => props.latitude != null && props.longitude != null)

const readonlyBehaviors = computed(() =>
  props.readonly ? [] : ['drag', 'scrollZoom', 'pinchZoom', 'dblClick']
)

function onMapUpdate({ location }) {
  if (location?.zoom != null) {
    actualZoom.value = location.zoom
  }
}

watch(() => [props.latitude, props.longitude, props.zoom], ([lat, lng, z]) => {
  if (updatingFromClick) return
  const latVal = lat ?? DEFAULT_LAT
  const lngVal = lng ?? DEFAULT_LNG
  markerLat.value = latVal
  markerLng.value = lngVal
  mapLocation.value = { center: [lngVal, latVal], zoom: z ?? props.zoom }
}, { immediate: true })

function onMapClick(_obj, event) {
  const coords = event?.coordinates
  if (coords && coords.length === 2) {
    markerLng.value = coords[0]
    markerLat.value = coords[1]
    updatingFromClick = true
    emit('update:longitude', coords[0])
    emit('update:latitude', coords[1])
    nextTick(() => { updatingFromClick = false })
  }
}

function onDragEnd(e) {
  if (e?.coordinates) {
    markerLng.value = e.coordinates[0]
    markerLat.value = e.coordinates[1]
    updatingFromClick = true
    emit('update:longitude', e.coordinates[0])
    emit('update:latitude', e.coordinates[1])
    nextTick(() => { updatingFromClick = false })
  }
}

function locateMe() {
  if (!navigator.geolocation) return
  navigator.geolocation.getCurrentPosition((pos) => {
    mapLocation.value = { center: [pos.coords.longitude, pos.coords.latitude], zoom: actualZoom.value }
    markerLat.value = pos.coords.latitude
    markerLng.value = pos.coords.longitude
    updatingFromClick = true
    emit('update:latitude', pos.coords.latitude)
    emit('update:longitude', pos.coords.longitude)
    nextTick(() => { updatingFromClick = false })
  })
}

onMounted(async () => {
  try {
    const lib = await import('../../lib/ymaps3.js')
    YMap = lib.YMap
    YMapDefaultSchemeLayer = lib.YMapDefaultSchemeLayer
    YMapDefaultFeaturesLayer = lib.YMapDefaultFeaturesLayer
    YMapMarker = lib.YMapMarker
    YMapListener = lib.YMapListener
    ymapReady.value = true
  } catch (e) {
    console.error('Failed to load Yandex Maps:', e)
    loadError.value = 'Не удалось загрузить карту'
  }
})
</script>

<style scoped>
.ui-map {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ui-map__container {
  width: 100%;
  height: 300px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  position: relative;
}

.ui-map__error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 8px;
  color: var(--text-muted);
  font-size: 13px;
}

.ui-map__locate {
  align-self: flex-start;
  display: flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: 1px solid var(--border-color);
  padding: 6px 12px;
  border-radius: var(--radius-md);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.ui-map__locate:hover {
  border-color: var(--purple-primary);
  color: var(--purple-primary);
}
</style>

<style>
.marker-root {
  display: flex;
  flex-direction: column;
  align-items: center;
  transform: translate(-50%, -100%);
  pointer-events: none;
}

.marker-label {
  white-space: nowrap;
  padding: 4px 10px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
  font-size: 12px;
  font-weight: 500;
  color: #1a1a2e;
  margin-bottom: 4px;
  max-width: 220px;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
