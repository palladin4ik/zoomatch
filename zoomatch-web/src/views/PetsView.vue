<template>
  <div class="pets-view">
    <div class="pets-view__header">
      <h1 class="pets-view__title">Мои питомцы</h1>
      <router-link to="/pets/new" class="pets-view__add-btn">+ Добавить</router-link>
    </div>

    <div class="pets-view__search">
      <UiInput v-model="searchQuery" placeholder="Поиск по имени..." />
    </div>

    <div class="pets-view__filters" v-if="animalTypes.length > 1">
      <button
        :class="['pets-view__filter-chip', { 'pets-view__filter-chip--active': !selectedType }]"
        @click="selectedType = null"
      >
        Все
      </button>
      <button
        v-for="type in animalTypes"
        :key="type.id"
        :class="['pets-view__filter-chip', { 'pets-view__filter-chip--active': selectedType === type.id }]"
        @click="selectedType = type.id"
      >
        {{ type.name }}
      </button>
    </div>

    <UiLoader v-if="petsStore.loading" text="Загрузка..." />

    <div v-else-if="petsStore.error" class="pets-view__error">
      <p>{{ petsStore.error }}</p>
      <UiButton variant="secondary" @click="loadData">Повторить</UiButton>
    </div>

    <div v-else-if="filteredPets.length === 0" class="pets-view__empty">
      <UiEmptyState
        title="Пока нет питомцев"
        description="Добавьте первого питомца, чтобы начать поиск"
      >
        <router-link to="/pets/new">
          <UiButton variant="primary" class="mt-4">Добавить питомца</UiButton>
        </router-link>
      </UiEmptyState>
    </div>

    <div v-else class="pets-view__grid">
      <PetCard
        v-for="pet in filteredPets"
        :key="pet.id"
        :pet="pet"
        @click="$router.push(`/pets/${pet.id}`)"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { usePetsStore } from '../stores/pets.js'
import UiInput from '../components/ui/UiInput.vue'
import UiButton from '../components/ui/UiButton.vue'
import UiLoader from '../components/ui/UiLoader.vue'
import UiEmptyState from '../components/ui/UiEmptyState.vue'
import PetCard from '../components/pets/PetCard.vue'

const petsStore = usePetsStore()

const searchQuery = ref('')
const selectedType = ref(null)

const animalTypes = computed(() => {
  const map = new Map()
  for (const pet of petsStore.myPets) {
    if (pet.animal_type && !map.has(pet.animal_type.id)) {
      map.set(pet.animal_type.id, pet.animal_type)
    }
  }
  return Array.from(map.values())
})

const filteredPets = computed(() => {
  let pets = petsStore.myPets
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    pets = pets.filter(p => p.name.toLowerCase().includes(q))
  }
  if (selectedType.value) {
    pets = pets.filter(p => p.animal_type?.id === selectedType.value)
  }
  return pets
})

async function loadData() {
  await petsStore.fetchMyPets()
}

onMounted(loadData)
</script>

<style scoped>
.pets-view {
  max-width: 800px;
  margin: 0 auto;
}

.pets-view__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.pets-view__title {
  font-size: 24px;
  font-weight: 700;
}

.pets-view__add-btn {
  background: var(--purple-primary);
  color: var(--text-white);
  padding: 8px 16px;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
}

.pets-view__add-btn:hover {
  background: var(--purple-dark);
  text-decoration: none;
}

.pets-view__search {
  margin-bottom: 16px;
  max-width: 400px;
}

.pets-view__filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}

.pets-view__filter-chip {
  padding: 6px 14px;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-full);
  background: var(--bg-white);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.pets-view__filter-chip:hover {
  border-color: var(--purple-primary);
}

.pets-view__filter-chip--active {
  background: var(--purple-primary);
  color: var(--text-white);
  border-color: var(--purple-primary);
}

.pets-view__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.pets-view__empty {
  padding: 40px;
}

.pets-view__error {
  text-align: center;
  padding: 40px;
  color: var(--red-accent);
}
</style>
