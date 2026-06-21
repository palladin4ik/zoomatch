<template>
  <div class="search-view">
    <template v-if="!searchStore.selectedPetId">
      <div class="search-view__select-screen">
        <div class="search-view__select-header">
          <div>
            <h1 class="search-view__title">Поиск пары</h1>
            <p class="search-view__subtitle">Выберите питомца для поиска идеальной пары</p>
          </div>
          <router-link to="/requests" class="search-view__requests-link">
            <UiIcon name="mail" /> Заявки
          </router-link>
        </div>

        <div class="search-view__filters-row">
          <div class="search-view__search-input">
            <UiInput v-model="petSearchQuery" placeholder="Поиск по имени..." />
          </div>
          <div class="search-view__pet-types" v-if="animalTypes.length > 1">
            <button
              :class="['search-view__type-chip', { 'search-view__type-chip--active': !selectedTypeFilter }]"
              @click="selectedTypeFilter = null"
            >
              Все
            </button>
            <button
              v-for="type in animalTypes"
              :key="type.id"
              :class="['search-view__type-chip', { 'search-view__type-chip--active': selectedTypeFilter === type.id }]"
              @click="selectedTypeFilter = type.id"
            >
              {{ type.name }}
            </button>
          </div>
        </div>

        <UiLoader v-if="petsStore.loading" text="Загрузка..." />

        <div v-else-if="filteredPets.length === 0" class="search-view__empty">
          <UiEmptyState
            title="Нет питомцев"
            description="Добавьте питомца, чтобы начать поиск"
          >
            <router-link to="/pets/new">
              <UiButton variant="primary">Добавить питомца</UiButton>
            </router-link>
          </UiEmptyState>
        </div>

        <div v-else class="search-view__pets-grid">
          <div
            v-for="pet in filteredPets"
            :key="pet.id"
            class="search-view__pet-card"
            @click="selectPet(pet)"
          >
            <div class="search-view__pet-photo">
              <img v-if="getPetAvatar(pet)" :src="getPetAvatar(pet)" :alt="pet.name" />
              <div v-else class="search-view__pet-photo-placeholder">
                <UiIcon name="paw" />
              </div>
              <span :class="['search-view__pet-gender', pet.is_male ? 'search-view__pet-gender--male' : 'search-view__pet-gender--female']">
                {{ pet.is_male ? '\u2642' : '\u2640' }}
              </span>
            </div>
            <div class="search-view__pet-info">
              <span class="search-view__pet-name">{{ pet.name }}</span>
              <span class="search-view__pet-breed">{{ pet.breed?.name || pet.animal_type?.name || '' }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>

    <template v-else>
      <div class="search-view__rec-screen">
        <div class="search-view__rec-topbar">
          <button class="search-view__back-btn" @click="backToSelection">
            <UiIcon name="arrowLeft" /> Назад
          </button>
          <h1 class="search-view__rec-title">Рекомендации для {{ selectedPetName }}</h1>
          <div class="search-view__rec-topbar-actions">
            <button class="search-view__filter-btn" @click="showFilters = true">
              <UiIcon name="settings" /> Фильтры
            </button>
          </div>
        </div>

        <UiLoader v-if="searchStore.loading" text="Поиск рекомендаций..." />

        <div v-else-if="searchStore.error" class="search-view__error">
          <p>{{ searchStore.error }}</p>
          <UiButton variant="secondary" @click="loadRecommendations">Повторить</UiButton>
        </div>

        <div v-else-if="searchStore.filteredRecommendations.length === 0" class="search-view__empty">
          <UiEmptyState
            title="Рекомендаций пока нет"
            description="Попробуйте изменить фильтры или загляните позже"
          >
            <UiButton variant="secondary" @click="backToSelection">Выбрать другого питомца</UiButton>
          </UiEmptyState>
        </div>

        <template v-else>
          <div v-if="currentRecommendation" class="search-view__rec-content">
            <RecommendationCard
              :key="currentRecommendation.id"
              :pet="currentRecommendation"
              :distance="currentRecommendation.distance_km"
              @like="handleLikeCurrent"
              @skip="handleSkip"
              @view-owner="viewOwner"
            />
          </div>
          <div v-else class="search-view__empty">
            <UiEmptyState
              title="Все рекомендации просмотрены"
              description="Попробуйте изменить фильтры или выбрать другого питомца"
            >
              <UiButton variant="primary" @click="resetAndReload">Обновить</UiButton>
              <UiButton variant="secondary" @click="backToSelection">Другой питомец</UiButton>
            </UiEmptyState>
          </div>

          <div v-if="currentRecommendation" class="search-view__rec-nav">
            <button
              class="search-view__nav-btn search-view__nav-btn--skip"
              @click="handleSkip"
              title="Пропустить"
            >
              <UiIcon name="close" />
            </button>
            <span class="search-view__rec-counter">
              {{ searchStore.currentIndex + 1 }} / {{ searchStore.filteredRecommendations.length }}
            </span>
            <button
              class="search-view__nav-btn search-view__nav-btn--like"
              @click="handleLikeCurrent"
              title="Нравится"
            >
              <UiIcon name="check" />
            </button>
          </div>
        </template>
      </div>
    </template>

    <FilterModal
      v-model="showFilters"
      :initial-filters="searchStore.filters"
      @apply="handleApplyFilters"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { usePetsStore } from '../stores/pets.js'
import { useSearchStore } from '../stores/search.js'
import { useMatchesStore } from '../stores/matches.js'
import { useAnimalTypesStore } from '../stores/animalTypes.js'
import UiInput from '../components/ui/UiInput.vue'
import UiButton from '../components/ui/UiButton.vue'
import UiLoader from '../components/ui/UiLoader.vue'
import UiEmptyState from '../components/ui/UiEmptyState.vue'
import UiIcon from '../components/ui/UiIcon.vue'
import RecommendationCard from '../components/search/RecommendationCard.vue'
import FilterModal from '../components/search/FilterModal.vue'

const router = useRouter()
const petsStore = usePetsStore()
const searchStore = useSearchStore()
const matchesStore = useMatchesStore()
const animalTypesStore = useAnimalTypesStore()
const toast = inject('toast')

const showFilters = ref(false)
const petSearchQuery = ref('')
const selectedTypeFilter = ref(null)

const filteredPets = computed(() => {
  let pets = petsStore.myPets.filter(p => p.is_active !== false)
  if (petSearchQuery.value) {
    const q = petSearchQuery.value.toLowerCase()
    pets = pets.filter(p => p.name.toLowerCase().includes(q))
  }
  if (selectedTypeFilter.value) {
    pets = pets.filter(p => p.animal_type?.id === selectedTypeFilter.value)
  }
  return pets
})

const animalTypes = computed(() => {
  const types = new Map()
  petsStore.myPets.forEach(p => {
    if (p.animal_type) types.set(p.animal_type.id, p.animal_type)
  })
  return Array.from(types.values())
})

const selectedPetName = computed(() => {
  const pet = petsStore.myPets.find(p => p.id === searchStore.selectedPetId)
  return pet?.name || ''
})

const currentRecommendation = computed(() =>
  searchStore.filteredRecommendations[searchStore.currentIndex] || null
)

function getPetAvatar(pet) {
  if (!pet.avatar) return ''
  if (pet.avatar.startsWith('http')) return pet.avatar
  return `${import.meta.env.VITE_API_URL}${pet.avatar}`
}

function selectPet(pet) {
  searchStore.selectedPetId = pet.id
  searchStore.currentIndex = 0
  showFilters.value = true
}

function backToSelection() {
  searchStore.selectedPetId = null
  searchStore.currentIndex = 0
  searchStore.recommendations = []
}

async function loadRecommendations() {
  if (!searchStore.selectedPetId) return
  await searchStore.fetchRecommendations(searchStore.selectedPetId)
}

async function handleLikeCurrent() {
  if (!searchStore.selectedPetId || !currentRecommendation.value) return
  try {
    await matchesStore.createMatch(searchStore.selectedPetId, currentRecommendation.value.id)
  } catch {
    toast('Не удалось отправить запрос', 'error')
  }
  if (searchStore.currentIndex < searchStore.filteredRecommendations.length) {
    searchStore.currentIndex++
  }
}

function handleSkip() {
  if (searchStore.currentIndex < searchStore.filteredRecommendations.length) {
    searchStore.currentIndex++
  }
}

function resetAndReload() {
  searchStore.currentIndex = 0
  loadRecommendations()
}

function viewOwner(ownerId) {
  router.push(`/users/${ownerId}`)
}

function handleApplyFilters(filters) {
  searchStore.filters = { ...filters }
  loadRecommendations()
}

onMounted(() => {
  petsStore.fetchMyPets()
  animalTypesStore.fetchTypes()
})
</script>

<style scoped>
.search-view {
  max-width: 1100px;
  margin: 0 auto;
}

.search-view__select-screen {
  padding: 0;
}

.search-view__select-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;
}

.search-view__title {
  font-size: 26px;
  font-weight: 700;
  margin: 0 0 4px 0;
}

.search-view__subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

.search-view__requests-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: 14px;
  text-decoration: none;
  color: var(--text-primary);
  transition: all var(--transition-fast);
}

.search-view__requests-link:hover {
  border-color: var(--purple-primary);
  color: var(--purple-primary);
}

.search-view__filters-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.search-view__search-input {
  max-width: 280px;
  flex-shrink: 0;
}

.search-view__pet-types {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.search-view__type-chip {
  padding: 7px 16px;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-full);
  background: var(--bg-white);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.search-view__type-chip:hover {
  border-color: var(--purple-primary);
}

.search-view__type-chip--active {
  background: var(--purple-primary);
  color: var(--text-white);
  border-color: var(--purple-primary);
}

.search-view__pets-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(170px, 1fr));
  gap: 16px;
}

.search-view__pet-card {
  background: var(--bg-white);
  border: 2px solid var(--border-color);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.search-view__pet-card:hover {
  border-color: var(--purple-light);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.search-view__pet-photo {
  position: relative;
  height: 140px;
  overflow: hidden;
}

.search-view__pet-photo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.search-view__pet-photo-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--purple-bg);
  color: var(--purple-primary);
  font-size: 36px;
}

.search-view__pet-gender {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.search-view__pet-gender--male {
  background: rgba(124, 58, 237, 0.9);
  color: white;
}

.search-view__pet-gender--female {
  background: rgba(236, 72, 153, 0.9);
  color: white;
}

.search-view__pet-info {
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.search-view__pet-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.search-view__pet-breed {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.search-view__rec-screen {
  padding: 0 0 24px 0;
}

.search-view__rec-topbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.search-view__back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: 1.5px solid var(--border-color);
  padding: 8px 14px;
  border-radius: var(--radius-md);
  font-size: 14px;
  cursor: pointer;
  transition: all var(--transition-fast);
  color: var(--text-primary);
  flex-shrink: 0;
}

.search-view__back-btn:hover {
  border-color: var(--purple-primary);
  color: var(--purple-primary);
}

.search-view__rec-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.search-view__rec-topbar-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.search-view__filter-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: 1.5px solid var(--border-color);
  padding: 8px 14px;
  border-radius: var(--radius-md);
  font-size: 14px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.search-view__filter-btn:hover {
  border-color: var(--purple-primary);
  color: var(--purple-primary);
}

.search-view__rec-content {
  margin-bottom: 24px;
}

.search-view__rec-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  padding: 16px 0;
}

.search-view__nav-btn {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  border: 2px solid var(--border-color);
  background: var(--bg-white);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-fast);
  font-size: 20px;
}

.search-view__nav-btn--skip {
  color: var(--red-accent);
}

.search-view__nav-btn--skip:hover {
  background: #FEF2F2;
  border-color: var(--red-accent);
}

.search-view__nav-btn--like {
  background: var(--purple-primary);
  color: white;
  border-color: var(--purple-primary);
  font-size: 22px;
}

.search-view__nav-btn--like:hover {
  background: var(--purple-dark);
  border-color: var(--purple-dark);
}

.search-view__rec-counter {
  font-size: 14px;
  color: var(--text-muted);
  font-weight: 500;
}

.search-view__empty {
  padding: 60px 0;
}

.search-view__error {
  text-align: center;
  padding: 60px 0;
  color: var(--red-accent);
}
</style>
