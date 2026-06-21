<template>
  <div class="pet-card" @click="$emit('click', pet)">
    <div class="pet-card__image-wrapper">
      <img
        v-if="petAvatar"
        :src="petAvatar"
        :alt="pet.name"
        class="pet-card__image"
      />
      <div v-else class="pet-card__image pet-card__image--placeholder">
        <UiIcon name="paw" size="lg" />
      </div>
      <span class="pet-card__gender" :class="{ 'pet-card__gender--female': !pet.is_male }">
        <UiIcon :name="pet.is_male ? 'male' : 'female'" size="sm" />
      </span>
    </div>
    <div class="pet-card__info">
      <h3 class="pet-card__name">{{ pet.name }}</h3>
      <p class="pet-card__breed">{{ breedName }}</p>
      <span :class="['pet-card__status', `pet-card__status--${statusClass}`]">
        {{ statusText }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import UiIcon from '../ui/UiIcon.vue'

const props = defineProps({
  pet: { type: Object, required: true },
})

defineEmits(['click'])

const petAvatar = computed(() => {
  if (!props.pet.avatar) return ''
  if (props.pet.avatar.startsWith('http')) return props.pet.avatar
  return `${import.meta.env.VITE_API_URL}${props.pet.avatar}`
})

const breedName = computed(() => {
  if (props.pet.breed?.name) return props.pet.breed.name
  if (props.pet.breed_custom) return props.pet.breed_custom
  return props.pet.animal_type?.name || ''
})

const statusClass = computed(() => {
  if (props.pet.moderation_status === 'pending') return 'pending'
  if (props.pet.moderation_status === 'rejected') return 'rejected'
  if (props.pet.is_active) return 'active'
  return 'inactive'
})

const statusText = computed(() => {
  if (props.pet.moderation_status === 'pending') return 'НА МОДЕРАЦИИ'
  if (props.pet.moderation_status === 'rejected') return 'ОТКЛОНЕНО'
  if (props.pet.is_active) return 'В АКТИВНОМ ПОИСКЕ'
  return 'НЕ ИЩЕТ'
})
</script>

<style scoped>
.pet-card {
  background: var(--bg-white);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow var(--transition-fast);
}

.pet-card:hover {
  box-shadow: var(--shadow-md);
}

.pet-card__image-wrapper {
  position: relative;
  height: 160px;
  background: var(--purple-bg);
}

.pet-card__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pet-card__image--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
}

.pet-card__gender {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  background: var(--purple-primary);
  color: var(--text-white);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.pet-card__gender--female {
  background: #EC4899;
}

.pet-card__info {
  padding: 12px;
}

.pet-card__name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.pet-card__breed {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pet-card__status {
  display: inline-block;
  padding: 2px 8px;
  border-radius: var(--radius-full);
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
}

.pet-card__status--active {
  background: #ECFDF5;
  color: var(--green-dark);
}

.pet-card__status--pending {
  background: #FFFBEB;
  color: #D97706;
}

.pet-card__status--rejected {
  background: #FEF2F2;
  color: var(--red-accent);
}

.pet-card__status--inactive {
  background: var(--bg-hover);
  color: var(--text-muted);
}
</style>
