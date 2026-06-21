<template>
  <div class="requests-view">
    <div class="requests-view__header">
      <router-link to="/search" class="requests-view__back">←</router-link>
      <h1 class="requests-view__title">Запросы на мэтч</h1>
    </div>

    <UiLoader v-if="matchesStore.loading" text="Загрузка..." />

    <div v-else-if="matchesStore.error" class="requests-view__error">
      <p>{{ matchesStore.error }}</p>
      <UiButton variant="secondary" @click="loadData">Повторить</UiButton>
    </div>

    <div v-else-if="matchesStore.incomingRequests.length === 0" class="requests-view__empty">
      <UiEmptyState
        title="Нет входящих запросов"
        description="Запросы на мэтч появятся когда кто-то проявит интерес к вашему питомцу"
      />
    </div>

    <div v-else class="requests-view__list">
      <div v-for="request in matchesStore.incomingRequests" :key="request.id" class="request-card">
        <div class="request-card__pet" v-if="request.pet_from_data">
          <UiAvatar
            :src="getPetAvatar(request.pet_from_data)"
            :name="request.pet_from_data.name"
            size="lg"
          />
          <div class="request-card__info">
            <h3 class="request-card__name">{{ request.pet_from_data.name }}</h3>
            <p class="request-card__breed">{{ request.pet_from_data.breed?.name || '' }}</p>
            <p class="request-card__owner" v-if="request.pet_from_data.owner">
              {{ request.pet_from_data.owner.firstname }} {{ request.pet_from_data.owner.lastname }}
            </p>
          </div>
        </div>
        <span :class="['request-card__status', `request-card__status--${statusKey(request.status)}`]">
          {{ statusLabel(request.status) }}
        </span>
        <div class="request-card__actions" v-if="request.status === 0">
          <UiButton
            variant="primary"
            size="sm"
            :loading="processingId === request.id"
            @click="handleAccept(request.id)"
          >
            Принять
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
</template>

<script setup>
import { ref, onMounted, inject } from 'vue'
import { useMatchesStore } from '../stores/matches.js'
import UiAvatar from '../components/ui/UiAvatar.vue'
import UiButton from '../components/ui/UiButton.vue'
import UiLoader from '../components/ui/UiLoader.vue'
import UiEmptyState from '../components/ui/UiEmptyState.vue'

const matchesStore = useMatchesStore()
const processingId = ref(null)
const toast = inject('toast')

function getPetAvatar(pet) {
  if (!pet?.avatar) return ''
  if (pet.avatar.startsWith('http')) return pet.avatar
  return `${import.meta.env.VITE_API_URL}${pet.avatar}`
}

function statusLabel(status) {
  if (status === 0) return 'Ожидание'
  if (status === 1) return 'Принят'
  if (status === 2) return 'Отклонён'
  return ''
}

function statusKey(status) {
  if (status === 0) return 'pending'
  if (status === 1) return 'accepted'
  if (status === 2) return 'rejected'
  return 'unknown'
}

async function handleAccept(id) {
  processingId.value = id
  try {
    await matchesStore.acceptMatch(id)
  } catch {
    toast('Не удалось принять запрос', 'error')
  } finally {
    processingId.value = null
  }
}

async function handleReject(id) {
  processingId.value = id
  try {
    await matchesStore.rejectMatch(id)
  } catch {
    toast('Не удалось отклонить запрос', 'error')
  } finally {
    processingId.value = null
  }
}

async function loadData() {
  await matchesStore.fetchIncomingRequests()
}

onMounted(loadData)
</script>

<style scoped>
.requests-view {
  max-width: 600px;
  margin: 0 auto;
}

.requests-view__header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.requests-view__back {
  font-size: 22px;
  color: var(--text-primary);
  text-decoration: none;
}

.requests-view__title {
  font-size: 22px;
  font-weight: 700;
}

.requests-view__empty {
  padding: 40px;
}

.requests-view__error {
  text-align: center;
  padding: 40px;
  color: var(--red-accent);
}

.requests-view__list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.request-card {
  background: var(--bg-white);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.request-card__pet {
  display: flex;
  align-items: center;
  gap: 14px;
}

.request-card__info {
  min-width: 0;
}

.request-card__name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 2px;
}

.request-card__breed {
  font-size: 13px;
  color: var(--text-secondary);
}

.request-card__owner {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 2px;
}

.request-card__actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.request-card__status {
  display: inline-block;
  padding: 4px 12px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.request-card__status--pending {
  background: #FEF3C7;
  color: #D97706;
}

.request-card__status--accepted {
  background: #D1FAE5;
  color: #10B981;
}

.request-card__status--rejected {
  background: #FEE2E2;
  color: #EF4444;
}
</style>
