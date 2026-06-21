<template>
  <div class="profile">
    <div class="profile__header">
      <h1 class="profile__title">Профиль</h1>
      <router-link to="/settings" class="profile__settings-btn"><UiIcon name="settings" /></router-link>
    </div>

    <UiLoader v-if="loading" text="Загрузка..." />

    <div v-else-if="error" class="profile__error">
      <p>{{ error }}</p>
      <UiButton variant="secondary" @click="loadData">Повторить</UiButton>
    </div>

    <template v-else>
      <div class="profile__avatar-section">
        <div class="profile__avatar-wrapper">
          <UiAvatar :src="userStore.avatarUrl" :name="userStore.fullName" size="xl" />
          <label class="profile__avatar-edit">
            <input type="file" accept="image/*" class="profile__avatar-input" @change="handleAvatarUpload" />
            <UiIcon name="edit" />
          </label>
        </div>
      </div>

      <div class="profile__name-section">
        <h2 class="profile__name">{{ userStore.fullName || 'Имя не указано' }}</h2>
        <router-link to="/profile/edit" class="profile__name-edit"><UiIcon name="edit" /></router-link>
      </div>

      <p v-if="userStore.description" class="profile__bio">{{ userStore.description }}</p>

      <div class="profile__stats">
        <div class="profile__stat-card">
          <span class="profile__stat-value">{{ petsCount }}</span>
          <span class="profile__stat-label">Питомцев</span>
        </div>
        <div class="profile__stat-card">
          <span class="profile__stat-value">{{ matchesCount }}</span>
          <span class="profile__stat-label">Мэтчи</span>
        </div>
      </div>

      <div class="profile__rating-card">
        <h3 class="profile__rating-title">Рейтинг</h3>
        <p class="profile__rating-subtitle">На основе отзывов</p>
        <div class="profile__rating-row">
          <div class="profile__rating-item">
            <span class="profile__rating-value">—</span>
            <span class="profile__rating-label">Положительные</span>
          </div>
          <div class="profile__rating-item">
            <span class="profile__rating-value">—</span>
            <span class="profile__rating-label">Скорость ответа</span>
          </div>
        </div>
      </div>

      <div class="profile__reviews">
        <h3 class="profile__reviews-title">Отзывы</h3>
        <div class="profile__reviews-empty">
          <span class="profile__reviews-icon"><UiIcon name="chat" /></span>
          <p class="profile__reviews-text">Пока нет отзывов</p>
          <p class="profile__reviews-hint">Отзывы появятся после первых встреч</p>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../stores/user.js'
import { usePetsStore } from '../stores/pets.js'
import { useMatchesStore } from '../stores/matches.js'
import UiAvatar from '../components/ui/UiAvatar.vue'
import UiLoader from '../components/ui/UiLoader.vue'
import UiButton from '../components/ui/UiButton.vue'
import UiIcon from '../components/ui/UiIcon.vue'

const route = useRoute()
const userStore = useUserStore()
const petsStore = usePetsStore()
const matchesStore = useMatchesStore()
const toast = inject('toast')

const loading = ref(true)
const error = ref(null)

const petsCount = computed(() => petsStore.myPets.length)
const matchesCount = computed(() => matchesStore.matches.filter(m => m.status === 1).length)

async function loadData() {
  loading.value = true
  try {
    await Promise.all([
      userStore.fetchProfile(),
      petsStore.fetchMyPets(),
      matchesStore.fetchMatches(),
    ])
  } catch (e) {
    error.value = e.response?.data?.detail || 'Не удалось загрузить профиль'
  } finally {
    loading.value = false
  }
}

async function handleAvatarUpload(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) {
    toast('Файл слишком большой. Максимум: 5 МБ', 'error')
    return
  }
  try {
    await userStore.uploadAvatar(file)
  } catch {
    toast('Ошибка загрузки аватара', 'error')
  }
}

onMounted(loadData)
</script>

<style scoped>
.profile {
  max-width: 600px;
  margin: 0 auto;
}

.profile__error {
  text-align: center;
  padding: 40px;
  color: var(--red-accent);
}

.profile__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.profile__title {
  font-size: 28px;
  font-weight: 700;
  color: var(--purple-primary);
}

.profile__settings-btn {
  font-size: 24px;
  text-decoration: none;
}

.profile__avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.profile__avatar-wrapper {
  position: relative;
}

.profile__avatar-edit {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 36px;
  height: 36px;
  background: var(--bg-white);
  border: 2px solid var(--purple-primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 14px;
  box-shadow: var(--shadow-sm);
}

.profile__avatar-input {
  display: none;
}

.profile__name-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 8px;
}

.profile__name {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
}

.profile__name-edit {
  font-size: 16px;
  text-decoration: none;
}

.profile__bio {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  text-align: left;
  margin-bottom: 24px;
}

.profile__stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 16px;
}

.profile__stat-card {
  background: var(--bg-white);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 20px;
  text-align: center;
}

.profile__stat-value {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: var(--purple-primary);
}

.profile__stat-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.profile__rating-card {
  background: var(--bg-white);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 20px;
  margin-bottom: 24px;
}

.profile__rating-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 4px;
}

.profile__rating-subtitle {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 16px;
}

.profile__rating-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.profile__rating-value {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: var(--green-accent);
}

.profile__rating-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.profile__reviews-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 16px;
}

.profile__reviews-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px;
  text-align: center;
}

.profile__reviews-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.profile__reviews-text {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.profile__reviews-hint {
  font-size: 13px;
  color: var(--text-muted);
}
</style>
