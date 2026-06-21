<template>
  <div class="rec-detail">
    <div class="rec-detail__left">
      <div class="rec-detail__photo-card">
        <img
          v-if="petAvatar"
          :src="petAvatar"
          :alt="pet.name"
          class="rec-detail__photo"
        />
        <div v-else class="rec-detail__photo rec-detail__photo--placeholder">
          <UiIcon name="paw" size="lg" />
        </div>
      </div>

      <div class="rec-detail__pet-section">
        <div class="rec-detail__pet-header">
          <div class="rec-detail__pet-title-row">
            <h2 class="rec-detail__pet-name">{{ pet.name }}, {{ ageText }}</h2>
          </div>
          <div v-if="pet.location" class="rec-detail__location">
            <UiIcon name="location" />
            <span>{{ displayLocation }}</span>
          </div>
        </div>

        <div class="rec-detail__tags">
          <span v-if="typeName" class="rec-detail__tag">{{ typeName }}</span>
          <span v-if="breedName" class="rec-detail__tag">{{ breedName }}</span>
          <span v-if="pet.is_male !== undefined" class="rec-detail__tag">{{ pet.is_male ? 'Мальчик' : 'Девочка' }}</span>
          <span v-if="pet.distance_km" class="rec-detail__tag">{{ pet.distance_km }} км</span>
        </div>

        <div class="rec-detail__about">
          <h3 class="rec-detail__about-title">О {{ pet.name }}</h3>
          <p class="rec-detail__about-text">
            {{ breedName ? `${typeName}, ${breedName}` : typeName || 'Питомец' }}.
            <template v-if="pet.age">{{ ageText }}.</template>
            <template v-if="pet.location"> Расположение: {{ displayLocation }}.</template>
          </p>
        </div>
      </div>
    </div>

    <div class="rec-detail__middle">
      <div v-if="pet.owner" class="rec-detail__owner-card">
        <div class="rec-detail__owner-avatar-wrapper">
          <UiAvatar :src="ownerAvatar" :name="ownerName" size="lg" />
        </div>
        <h3 class="rec-detail__owner-name">{{ ownerName }}</h3>
        <button class="rec-detail__profile-btn" @click="$emit('view-owner', pet.owner.id)">
          Посмотреть профиль
        </button>
      </div>

      <div class="rec-detail__map-card">
        <h4 class="rec-detail__map-title">Расположение</h4>
        <div class="rec-detail__map-wrapper">
          <UiMap
            v-if="petLatitude && petLongitude"
            :latitude="petLatitude"
            :longitude="petLongitude"
            :zoom="10"
            :readonly="true"
          />
          <div v-else class="rec-detail__map-placeholder">
            <UiIcon name="location" />
            <span>{{ displayLocation }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="rec-detail__right">
      <div class="rec-detail__info-card">
        <div class="rec-detail__info-row" v-if="pet.age">
          <span class="rec-detail__info-label">Возраст</span>
          <span class="rec-detail__info-value">{{ ageText }}</span>
        </div>
        <div class="rec-detail__info-row" v-if="typeName">
          <span class="rec-detail__info-label">Вид</span>
          <span class="rec-detail__info-value">{{ typeName }}</span>
        </div>
        <div class="rec-detail__info-row" v-if="breedName">
          <span class="rec-detail__info-label">Порода</span>
          <span class="rec-detail__info-value">{{ breedName }}</span>
        </div>
        <div class="rec-detail__info-row" v-if="pet.distance_km">
          <span class="rec-detail__info-label">Расстояние</span>
          <span class="rec-detail__info-value">{{ pet.distance_km }} км</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch, onMounted } from 'vue'
import UiAvatar from '../ui/UiAvatar.vue'
import UiIcon from '../ui/UiIcon.vue'
import UiMap from '../ui/UiMap.vue'
import { reverseGeocode } from '../../lib/geocoder.js'

const props = defineProps({
  pet: { type: Object, required: true },
  distance: { type: Number, default: null },
})

defineEmits(['like', 'skip', 'view-owner'])

const resolvedAddress = ref(null)

const petAvatar = computed(() => {
  if (!props.pet.avatar) return ''
  if (props.pet.avatar.startsWith('http')) return props.pet.avatar
  return `${import.meta.env.VITE_API_URL}${props.pet.avatar}`
})

const ownerAvatar = computed(() => {
  if (!props.pet.owner?.avatar) return ''
  if (props.pet.owner.avatar.startsWith('http')) return props.pet.owner.avatar
  return `${import.meta.env.VITE_API_URL}${props.pet.owner.avatar}`
})

const ownerName = computed(() => {
  if (!props.pet.owner) return ''
  return `${props.pet.owner.firstname || ''} ${props.pet.owner.lastname || ''}`.trim() || 'Владелец'
})

const typeName = computed(() => props.pet.animal_type?.name || '')
const breedName = computed(() => props.pet.breed?.name || '')

const petLatitude = computed(() => {
  if (props.pet.latitude) return Number(props.pet.latitude)
  if (props.pet.location) {
    const parts = props.pet.location.split(',').map(s => s.trim())
    if (parts.length === 2) {
      const lat = parseFloat(parts[0])
      if (!isNaN(lat)) return lat
    }
  }
  return null
})

const petLongitude = computed(() => {
  if (props.pet.longitude) return Number(props.pet.longitude)
  if (props.pet.location) {
    const parts = props.pet.location.split(',').map(s => s.trim())
    if (parts.length === 2) {
      const lng = parseFloat(parts[1])
      if (!isNaN(lng)) return lng
    }
  }
  return null
})

async function fetchAddress() {
  if (petLatitude.value && petLongitude.value) {
    const addr = await reverseGeocode(petLatitude.value, petLongitude.value)
    if (addr) resolvedAddress.value = addr
  }
}

watch([petLatitude, petLongitude], () => {
  resolvedAddress.value = null
  fetchAddress()
})

onMounted(() => {
  fetchAddress()
})

const displayLocation = computed(() => {
  if (resolvedAddress.value) return resolvedAddress.value
  if (petLatitude.value && petLongitude.value) {
    return `${petLatitude.value.toFixed(4)}, ${petLongitude.value.toFixed(4)}`
  }
  return props.pet.location || 'Не указано'
})

const ageText = computed(() => {
  const age = props.pet.age
  if (age === undefined || age === null) return ''
  const lastDigit = age % 10
  const lastTwoDigits = age % 100
  if (lastTwoDigits >= 11 && lastTwoDigits <= 19) return `${age} лет`
  if (lastDigit === 1) return `${age} год`
  if (lastDigit >= 2 && lastDigit <= 4) return `${age} года`
  return `${age} лет`
})
</script>

<style scoped>
.rec-detail {
  display: grid;
  grid-template-columns: 1fr 300px 260px;
  gap: 20px;
  background: var(--bg-white);
  border-radius: var(--radius-xl);
  padding: 24px;
  box-shadow: var(--shadow-sm);
}

.rec-detail__left {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-width: 0;
}

.rec-detail__photo-card {
  background: var(--purple-bg);
  border-radius: var(--radius-lg);
  overflow: hidden;
  aspect-ratio: 4/3;
}

.rec-detail__photo {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.rec-detail__photo--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--purple-primary);
  font-size: 64px;
}

.rec-detail__pet-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.rec-detail__pet-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rec-detail__pet-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.rec-detail__pet-name {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.rec-detail__pet-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.rec-detail__icon-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1.5px solid var(--border-color);
  background: var(--bg-white);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-fast);
  color: var(--text-secondary);
}

.rec-detail__icon-btn:hover {
  border-color: var(--purple-primary);
  color: var(--purple-primary);
}

.rec-detail__icon-btn--like {
  background: var(--purple-primary);
  border-color: var(--purple-primary);
  color: white;
}

.rec-detail__icon-btn--like:hover {
  background: var(--purple-dark);
}

.rec-detail__location {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--text-secondary);
}

.rec-detail__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.rec-detail__tag {
  padding: 6px 14px;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-full);
  font-size: 13px;
  color: var(--text-primary);
  background: var(--bg-white);
}

.rec-detail__about {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rec-detail__about-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.rec-detail__about-text {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
}

.rec-detail__middle {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.rec-detail__owner-card {
  background: var(--purple-bg);
  border-radius: var(--radius-lg);
  padding: 24px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.rec-detail__owner-avatar-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  overflow: hidden;
  background: var(--bg-white);
  display: flex;
  align-items: center;
  justify-content: center;
}

.rec-detail__owner-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  text-align: center;
}

.rec-detail__contact-btn {
  width: 100%;
  padding: 10px 16px;
  background: var(--purple-primary);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background var(--transition-fast);
}

.rec-detail__contact-btn:hover {
  background: var(--purple-dark);
}

.rec-detail__profile-btn {
  width: 100%;
  padding: 10px 16px;
  background: var(--bg-white);
  color: var(--purple-primary);
  border: 1.5px solid var(--purple-primary);
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.rec-detail__profile-btn:hover {
  background: var(--purple-bg);
}

.rec-detail__map-card {
  background: var(--bg-white);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 16px;
}

.rec-detail__map-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 12px 0;
}

.rec-detail__map-wrapper {
  height: 200px;
  border-radius: var(--radius-md);
  overflow: hidden;
}

.rec-detail__map-placeholder {
  height: 120px;
  background: var(--purple-bg);
  border-radius: var(--radius-md);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 13px;
}

.rec-detail__right {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.rec-detail__info-card {
  background: var(--bg-white);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 20px;
}

.rec-detail__info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid var(--border-color);
}

.rec-detail__info-row:last-child {
  border-bottom: none;
}

.rec-detail__info-label {
  font-size: 13px;
  color: var(--text-secondary);
  flex-shrink: 0;
  margin-right: 12px;
}

.rec-detail__info-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  text-align: right;
  min-width: 0;
  word-break: break-word;
}

@media (max-width: 900px) {
  .rec-detail {
    grid-template-columns: 1fr;
  }
}
</style>
