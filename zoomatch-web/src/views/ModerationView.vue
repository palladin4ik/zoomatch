<template>
  <div class="moderation">
    <header class="moderation__header">
      <h1 class="moderation__title">Модерация</h1>
    </header>

    <div class="moderation__tabs">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        :class="['moderation__tab', { 'moderation__tab--active': activeTab === tab.key }]"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
        <span v-if="tab.key === 'requests' && store.requests.length" class="moderation__tab-badge">
          {{ store.requests.length }}
        </span>
      </button>
    </div>

    <div v-if="store.loading && !initialLoaded" class="moderation__loading">
      <UiLoader text="Загрузка..." />
    </div>

    <template v-else>
      <!-- Tab: Заявки -->
      <div v-if="activeTab === 'requests'">
        <UiEmptyState
          v-if="store.requests.length === 0"
          title="Нет заявок на модерацию"
          description="Все анкеты проверены"
        />

        <div v-else class="moderation__list">
          <div v-for="request in store.requests" :key="request.id" class="moderation-card">
            <div class="moderation-card__main">
              <UiAvatar
                :src="getPetAvatar(request.pet)"
                :name="request.pet?.name"
                size="lg"
              />
              <div class="moderation-card__info">
                <h3 class="moderation-card__name">{{ request.pet?.name }}</h3>
                <p class="moderation-card__detail">
                  <span class="moderation-card__label">Владелец:</span>
                  {{ request.pet?.owner?.firstname }} {{ request.pet?.owner?.lastname }}
                </p>
                <p class="moderation-card__detail">
                  <span class="moderation-card__label">Возраст:</span>
                  {{ request.pet?.age }} лет
                </p>
                <p class="moderation-card__detail">
                  <span class="moderation-card__label">Пол:</span>
                  {{ request.pet?.is_male ? 'Самец' : 'Самка' }}
                </p>
                <p v-if="request.pet?.description" class="moderation-card__detail">
                  <span class="moderation-card__label">Описание:</span>
                  {{ request.pet.description }}
                </p>
              </div>
            </div>

            <div class="moderation-card__moderation">
              <h4>Данные на проверку</h4>
              <p v-if="request.animal_type" class="moderation-card__tag">
                <span class="moderation-card__label">Новый тип:</span> {{ request.animal_type }}
              </p>
              <p v-if="request.breed" class="moderation-card__tag">
                <span class="moderation-card__label">Новая порода:</span> {{ request.breed }}
              </p>
              <p class="moderation-card__date">
                {{ formatDate(request.created_at) }}
              </p>
            </div>

            <div class="moderation-card__actions">
              <UiButton
                variant="primary"
                size="sm"
                :loading="processingId === request.id"
                @click="handleApprove(request.id)"
              >
                Одобрить
              </UiButton>
              <UiButton
                variant="danger"
                size="sm"
                :loading="processingId === request.id"
                @click="handleReject(request.id)"
              >
                Отклонить
              </UiButton>
            </div>
          </div>
        </div>
      </div>

      <!-- Tab: Виды -->
      <div v-if="activeTab === 'types'">
        <div class="moderation__add-form">
          <UiInput
            v-model="newTypeName"
            label="Название вида"
            placeholder="Например: Кошка"
            @keyup.enter="handleAddType"
          />
          <UiButton
            variant="primary"
            size="sm"
            :loading="addingType"
            :disabled="!newTypeName.trim()"
            @click="handleAddType"
          >
            Добавить
          </UiButton>
        </div>

        <UiEmptyState
          v-if="store.animalTypes.length === 0"
          icon="pet"
          title="Виды отсутствуют"
          description="Добавьте первый вид животного"
        />

        <div v-else class="moderation__list">
          <div v-for="type in store.animalTypes" :key="type.id" class="moderation-item">
            <span class="moderation-item__name">{{ type.name }}</span>
            <UiButton
              variant="ghost"
              size="sm"
              @click="confirmDeleteType(type)"
            >
              Удалить
            </UiButton>
          </div>
        </div>
      </div>

      <!-- Tab: Породы -->
      <div v-if="activeTab === 'breeds'">
        <div class="moderation__add-form">
          <UiSelect
            v-model="newBreedTypeId"
            :options="typeOptions"
            label="Вид животного"
            placeholder="Выберите вид"
          />
          <UiInput
            v-model="newBreedName"
            label="Название породы"
            placeholder="Например: Мейн-кун"
            @keyup.enter="handleAddBreed"
          />
          <UiButton
            variant="primary"
            size="sm"
            :loading="addingBreed"
            :disabled="!newBreedName.trim() || !newBreedTypeId"
            @click="handleAddBreed"
          >
            Добавить
          </UiButton>
        </div>

        <UiSelect
          v-model="filterTypeId"
          :options="[{ value: null, label: 'Все виды' }, ...typeOptions]"
          label="Фильтр по виду"
          placeholder="Все виды"
          class="moderation__filter"
        />

        <UiEmptyState
          v-if="filteredBreeds.length === 0"
          icon="pet"
          title="Породы отсутствуют"
          :description="filterTypeId ? 'Нет пород для выбранного вида' : 'Добавьте первую породу'"
        />

        <div v-else class="moderation__list">
          <div v-for="breed in filteredBreeds" :key="breed.id" class="moderation-item">
            <span class="moderation-item__name">{{ breed.name }}</span>
            <span class="moderation-item__type">{{ getTypeName(breed.animal_type) }}</span>
            <UiButton
              variant="ghost"
              size="sm"
              @click="confirmDeleteBreed(breed)"
            >
              Удалить
            </UiButton>
          </div>
        </div>
      </div>
    </template>

    <!-- Delete confirmation modal -->
    <UiModal v-model="showDeleteModal" :title="deleteModalTitle" size="sm">
      <p>{{ deleteModalMessage }}</p>
      <template #footer>
        <UiButton variant="ghost" @click="showDeleteModal = false">Отмена</UiButton>
        <UiButton
          variant="danger"
          :loading="deleting"
          @click="handleDeleteConfirm"
        >
          Удалить
        </UiButton>
      </template>
    </UiModal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { useModerationStore } from '../stores/moderation.js'
import UiButton from '../components/ui/UiButton.vue'
import UiInput from '../components/ui/UiInput.vue'
import UiSelect from '../components/ui/UiSelect.vue'
import UiModal from '../components/ui/UiModal.vue'
import UiAvatar from '../components/ui/UiAvatar.vue'
import UiEmptyState from '../components/ui/UiEmptyState.vue'
import UiLoader from '../components/ui/UiLoader.vue'

const router = useRouter()
const authStore = useAuthStore()
const store = useModerationStore()
const toast = inject('toast')

const initialLoaded = ref(false)
const activeTab = ref('requests')
const processingId = ref(null)

const tabs = [
  { key: 'requests', label: 'Заявки' },
  { key: 'types', label: 'Виды' },
  { key: 'breeds', label: 'Породы' },
]

const newTypeName = ref('')
const addingType = ref(false)

const newBreedName = ref('')
const newBreedTypeId = ref(null)
const addingBreed = ref(false)
const filterTypeId = ref(null)

const showDeleteModal = ref(false)
const deleteModalTitle = ref('')
const deleteModalMessage = ref('')
const deleteTarget = ref(null)
const deleteType = ref('')
const deleting = ref(false)

const typeOptions = computed(() =>
  store.animalTypes.map(t => ({ value: t.id, label: t.name }))
)

const filteredBreeds = computed(() => {
  if (!filterTypeId.value) return store.breeds
  return store.breeds.filter(b => b.animal_type === filterTypeId.value)
})

function getPetAvatar(pet) {
  if (!pet?.avatar) return ''
  if (pet.avatar.startsWith('http')) return pet.avatar
  return `${import.meta.env.VITE_API_URL}${pet.avatar}`
}

function getTypeName(typeId) {
  const type = store.animalTypes.find(t => t.id === typeId)
  return type?.name || ''
}

function formatDate(dateString) {
  return new Date(dateString).toLocaleDateString('ru-RU', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  })
}

async function handleApprove(id) {
  processingId.value = id
  try {
    await store.approve(id)
    toast?.success('Заявка одобрена')
  } catch {
    toast?.error('Не удалось одобрить заявку')
  } finally {
    processingId.value = null
  }
}

async function handleReject(id) {
  processingId.value = id
  try {
    await store.reject(id)
    toast?.success('Заявка отклонена')
  } catch {
    toast?.error('Не удалось отклонить заявку')
  } finally {
    processingId.value = null
  }
}

async function handleAddType() {
  if (!newTypeName.value.trim()) return
  addingType.value = true
  try {
    await store.addAnimalType({ name: newTypeName.value.trim() })
    newTypeName.value = ''
    toast?.success('Вид добавлен')
  } catch {
    toast?.error('Не удалось добавить вид')
  } finally {
    addingType.value = false
  }
}

function confirmDeleteType(type) {
  deleteTarget.value = type
  deleteType.value = 'type'
  deleteModalTitle.value = 'Удалить вид?'
  deleteModalMessage.value = `Вы уверены, что хотите удалить «${type.name}»?`
  showDeleteModal.value = true
}

async function handleAddBreed() {
  if (!newBreedName.value.trim() || !newBreedTypeId.value) return
  addingBreed.value = true
  try {
    await store.addBreed({
      name: newBreedName.value.trim(),
      animal_type: newBreedTypeId.value,
    })
    newBreedName.value = ''
    newBreedTypeId.value = null
    toast?.success('Порода добавлена')
  } catch {
    toast?.error('Не удалось добавить породу')
  } finally {
    addingBreed.value = false
  }
}

function confirmDeleteBreed(breed) {
  deleteTarget.value = breed
  deleteType.value = 'breed'
  deleteModalTitle.value = 'Удалить породу?'
  deleteModalMessage.value = `Вы уверены, что хотите удалить «${breed.name}»?`
  showDeleteModal.value = true
}

async function handleDeleteConfirm() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    if (deleteType.value === 'type') {
      await store.removeAnimalType(deleteTarget.value.id)
      toast?.success('Вид удалён')
    } else {
      await store.removeBreed(deleteTarget.value.id)
      toast?.success('Порода удалена')
    }
    showDeleteModal.value = false
  } catch {
    toast?.error('Не удалось удалить')
  } finally {
    deleting.value = false
  }
}

onMounted(async () => {
  if (!authStore.isModerator) {
    router.replace('/')
    return
  }
  await Promise.all([
    store.fetchRequests(),
    store.fetchAnimalTypes(),
    store.fetchBreeds(),
  ])
  initialLoaded.value = true
})
</script>

<style scoped>
.moderation {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px;
}

.moderation__header {
  margin-bottom: 24px;
}

.moderation__title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}

.moderation__tabs {
  display: flex;
  gap: 4px;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 24px;
}

.moderation__tab {
  position: relative;
  padding: 10px 16px;
  background: none;
  border: none;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  cursor: pointer;
  transition: color var(--transition-fast);
}

.moderation__tab:hover {
  color: var(--text-primary);
}

.moderation__tab--active {
  color: var(--purple-primary);
}

.moderation__tab--active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 2px;
  background: var(--purple-primary);
  border-radius: 1px 1px 0 0;
}

.moderation__tab-badge {
  margin-left: 6px;
  background: var(--red-accent);
  color: var(--text-white);
  font-size: 11px;
  font-weight: 600;
  padding: 1px 6px;
  border-radius: var(--radius-full);
}

.moderation__loading {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

.moderation__add-form {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  margin-bottom: 20px;
}

.moderation__filter {
  margin-bottom: 20px;
  max-width: 300px;
}

.moderation__list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.moderation-card {
  background: var(--bg-white);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 20px;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 16px;
}

.moderation-card__main {
  display: flex;
  gap: 16px;
  grid-column: 1;
}

.moderation-card__info {
  flex: 1;
}

.moderation-card__name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.moderation-card__detail {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 4px 0;
}

.moderation-card__label {
  color: var(--text-muted);
  margin-right: 4px;
}

.moderation-card__moderation {
  grid-column: 1;
  background: var(--bg-hover);
  border-radius: var(--radius-md);
  padding: 12px 16px;
}

.moderation-card__moderation h4 {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.moderation-card__tag {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 4px 0;
}

.moderation-card__date {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 8px;
}

.moderation-card__actions {
  grid-column: 2;
  grid-row: 1 / 3;
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-self: start;
}

.moderation-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--bg-white);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
}

.moderation-item__name {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.moderation-item__type {
  font-size: 13px;
  color: var(--text-muted);
}
</style>
