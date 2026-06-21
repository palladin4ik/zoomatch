<template>
  <div class="pet-detail">
    <div class="pet-detail__header">
      <button class="pet-detail__back" @click="goBack">&larr;</button>
      <h1 class="pet-detail__title">Питомец</h1>
      <router-link v-if="pet && isOwner" :to="`/pets/${pet.id}/edit`" class="pet-detail__edit"><UiIcon name="edit" /></router-link>
    </div>

    <UiLoader v-if="petsStore.loading" text="Загрузка..." />

    <div v-else-if="petsStore.error" class="pet-detail__error">
      <p>{{ petsStore.error }}</p>
      <UiButton variant="secondary" @click="loadData">Повторить</UiButton>
    </div>

    <template v-else-if="pet">
      <div class="pet-detail__photo-section">
        <img
          v-if="petAvatar"
          :src="petAvatar"
          :alt="pet.name"
          class="pet-detail__photo"
        />
        <div v-else class="pet-detail__photo pet-detail__photo--placeholder"><UiIcon name="paw" size="lg" /></div>
      </div>

      <div class="pet-detail__info">
        <div class="pet-detail__name-row">
          <h2 class="pet-detail__name">{{ pet.name }}</h2>
          <span class="pet-detail__gender" :class="{ 'pet-detail__gender--female': !pet.is_male }">
            <UiIcon :name="pet.is_male ? 'male' : 'female'" />
          </span>
        </div>

        <p class="pet-detail__type">{{ typeName }} · {{ breedName }}</p>
        <p class="pet-detail__age">{{ pet.age }} {{ ageLabel }}</p>

        <span :class="['pet-detail__status', `pet-detail__status--${statusClass}`]">
          {{ statusText }}
        </span>

        <p v-if="pet.description" class="pet-detail__description">{{ pet.description }}</p>

        <div v-if="pet.latitude && pet.longitude" class="pet-detail__map">
          <h3 class="pet-detail__section-title">Местоположение</h3>
          <p v-if="resolvedAddress" class="pet-detail__address">{{ resolvedAddress }}</p>
          <UiMap
            :latitude="Number(pet.latitude)"
            :longitude="Number(pet.longitude)"
            :zoom="12"
            readonly
          />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePetsStore } from '../stores/pets.js'
import { useUserStore } from '../stores/user.js'
import UiAvatar from '../components/ui/UiAvatar.vue'
import UiLoader from '../components/ui/UiLoader.vue'
import UiButton from '../components/ui/UiButton.vue'
import UiIcon from '../components/ui/UiIcon.vue'
import UiMap from '../components/ui/UiMap.vue'
import { reverseGeocode } from '../lib/geocoder.js'

const route = useRoute()
const router = useRouter()
const petsStore = usePetsStore()
const userStore = useUserStore()

const isOwner = computed(() => pet.value?.owner?.id && pet.value.owner.id === userStore.id)

const pet = computed(() => petsStore.currentPet)

const resolvedAddress = ref(null)

async function fetchAddress() {
  const lat = pet.value?.latitude
  const lng = pet.value?.longitude
  if (lat && lng) {
    const addr = await reverseGeocode(Number(lat), Number(lng))
    if (addr) resolvedAddress.value = addr
  }
}

watch(() => pet.value?.location, () => {
  resolvedAddress.value = null
  fetchAddress()
})

onMounted(() => {
  fetchAddress()
})

const petAvatar = computed(() => {
  if (!pet.value?.avatar) return ''
  if (pet.value.avatar.startsWith('http')) return pet.value.avatar
  return `${import.meta.env.VITE_API_URL}${pet.value.avatar}`
})

const ownerAvatar = computed(() => {
  if (!pet.value?.owner?.avatar) return ''
  if (pet.value.owner.avatar.startsWith('http')) return pet.value.owner.avatar
  return `${import.meta.env.VITE_API_URL}${pet.value.owner.avatar}`
})

const ownerName = computed(() => {
  if (!pet.value?.owner) return ''
  return `${pet.value.owner.firstname || ''} ${pet.value.owner.lastname || ''}`.trim()
})

const typeName = computed(() => pet.value?.animal_type?.name || pet.value?.animal_type_custom || '')
const breedName = computed(() => pet.value?.breed?.name || pet.value?.breed_custom || '')

const ageLabel = computed(() => {
  const age = pet.value?.age
  if (!age) return ''
  const lastDigit = age % 10
  const lastTwo = age % 100
  if (lastTwo >= 11 && lastTwo <= 19) return 'лет'
  if (lastDigit === 1) return 'год'
  if (lastDigit >= 2 && lastDigit <= 4) return 'года'
  return 'лет'
})

const statusClass = computed(() => {
  if (pet.value?.moderation_status === 'pending') return 'pending'
  if (pet.value?.moderation_status === 'rejected') return 'rejected'
  if (pet.value?.is_active) return 'active'
  return 'inactive'
})

const statusText = computed(() => {
  if (pet.value?.moderation_status === 'pending') return 'На модерации'
  if (pet.value?.moderation_status === 'rejected') return 'Отклонено'
  if (pet.value?.is_active) return 'В активном поиске'
  return 'Не ищет'
})

async function loadData() {
  await petsStore.fetchPet(route.params.id)
}

function goBack() {
  if (route.query.from === 'user-profile' && route.query.ownerId) {
    router.push(`/users/${route.query.ownerId}`)
  } else {
    router.push('/pets')
  }
}

onMounted(loadData)
</script>

<style scoped>
.pet-detail {
  max-width: 600px;
  margin: 0 auto;
}

.pet-detail__error {
  text-align: center;
  padding: 40px;
  color: var(--red-accent);
}

.pet-detail__header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.pet-detail__back {
  font-size: 22px;
  color: var(--text-primary);
  text-decoration: none;
}

.pet-detail__title {
  font-size: 20px;
  font-weight: 600;
  flex: 1;
}

.pet-detail__edit {
  font-size: 20px;
  text-decoration: none;
}

.pet-detail__photo-section {
  margin-bottom: 24px;
}

.pet-detail__photo {
  width: 100%;
  height: 300px;
  object-fit: cover;
  border-radius: var(--radius-lg);
}

.pet-detail__photo--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--purple-bg);
  font-size: 64px;
}

.pet-detail__info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pet-detail__name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pet-detail__name {
  font-size: 24px;
  font-weight: 700;
}

.pet-detail__gender {
  font-size: 20px;
  color: var(--purple-primary);
}

.pet-detail__gender--female {
  color: #EC4899;
}

.pet-detail__type {
  font-size: 15px;
  color: var(--text-secondary);
}

.pet-detail__age {
  font-size: 14px;
  color: var(--text-muted);
}

.pet-detail__status {
  display: inline-block;
  padding: 4px 12px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 600;
  align-self: flex-start;
}

.pet-detail__status--active { background: #ECFDF5; color: var(--green-dark); }
.pet-detail__status--pending { background: #FFFBEB; color: #D97706; }
.pet-detail__status--rejected { background: #FEF2F2; color: var(--red-accent); }
.pet-detail__status--inactive { background: var(--bg-hover); color: var(--text-muted); }

.pet-detail__description {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-secondary);
  margin-top: 8px;
}

.pet-detail__owner {
  margin-top: 16px;
}

.pet-detail__section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}

.pet-detail__address {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 12px 0;
}

.pet-detail__owner-link {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: var(--text-primary);
  font-size: 15px;
  font-weight: 500;
}

.pet-detail__owner-link:hover {
  text-decoration: none;
}

.pet-detail__actions {
  margin-top: 20px;
}

.pet-detail__action-btn {
  display: inline-block;
  padding: 12px 24px;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
}

.pet-detail__action-btn--primary {
  background: var(--purple-primary);
  color: var(--text-white);
}

.pet-detail__action-btn--primary:hover {
  background: var(--purple-dark);
  text-decoration: none;
}
</style>
