<template>
  <div class="user-profile">
    <div class="user-profile__header">
      <button class="user-profile__back" @click="goBack">&larr;</button>
      <h1 class="user-profile__title">Профиль владельца</h1>
    </div>

    <UiLoader v-if="loading" text="Загрузка..." />

    <template v-else-if="user">
      <div class="user-profile__avatar">
        <UiAvatar :src="avatarUrl" :name="fullName" size="xl" />
      </div>

      <h2 class="user-profile__name">{{ fullName || 'Владелец' }}</h2>

      <p v-if="user.description" class="user-profile__bio">{{ user.description }}</p>

      <div v-if="pets.length > 0" class="user-profile__pets">
        <h3 class="user-profile__section-title">Питомцы владельца</h3>
        <div class="user-profile__pets-grid">
          <PetCard
            v-for="pet in pets"
            :key="pet.id"
            :pet="pet"
            @click="$router.push(`/pets/${pet.id}?from=user-profile&ownerId=${route.params.id}`)"
          />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUser } from '../api/index.js'
import { useSearchStore } from '../stores/search.js'
import UiAvatar from '../components/ui/UiAvatar.vue'
import UiLoader from '../components/ui/UiLoader.vue'
import PetCard from '../components/pets/PetCard.vue'

const route = useRoute()
const router = useRouter()
const searchStore = useSearchStore()
const toast = inject('toast')

const user = ref(null)
const pets = ref([])
const loading = ref(true)

const fullName = computed(() => {
  if (!user.value) return ''
  return `${user.value.firstname || ''} ${user.value.lastname || ''}`.trim()
})

const avatarUrl = computed(() => {
  if (!user.value?.avatar) return ''
  if (user.value.avatar.startsWith('http')) return user.value.avatar
  return `${import.meta.env.VITE_API_URL}${user.value.avatar}`
})

onMounted(async () => {
  try {
    const response = await getUser(route.params.id)
    user.value = response.data
    pets.value = response.data.pets || []
  } catch {
    toast('Не удалось загрузить профиль', 'error')
  } finally {
    loading.value = false
  }
})

function goBack() {
  if (searchStore.selectedPetId) {
    router.push('/search')
  } else {
    router.push('/search')
  }
}
</script>

<style scoped>
.user-profile {
  max-width: 600px;
  margin: 0 auto;
}

.user-profile__header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.user-profile__back {
  font-size: 22px;
  color: var(--text-primary);
  text-decoration: none;
}

.user-profile__title {
  font-size: 20px;
  font-weight: 600;
}

.user-profile__avatar {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.user-profile__name {
  text-align: center;
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 8px;
}

.user-profile__bio {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  text-align: left;
  margin-bottom: 24px;
}

.user-profile__section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
}

.user-profile__pets-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
}
</style>
