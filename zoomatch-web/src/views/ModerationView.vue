<template>
  <div class="moderation-container">
    <header>
      <h1>Модерация анкет</h1>
      <button class="logout-btn" @click="handleLogout">Выйти</button>
    </header>

    <div v-if="loading" class="state-message">Загрузка...</div>
    <div v-else-if="error" class="state-message error">{{ error }}</div>
    <div v-else-if="requests.length === 0" class="state-message">
      Новых заявок нет
    </div>

    <div v-else class="requests-grid">
      <div v-for="request in requests" :key="request.id" class="request-card">
        
        <div class="pet-info">
          <h2>{{ request.pet.name }}</h2>
          <p><span>Владелец:</span> {{ request.pet.owner.name }}</p>
          <p><span>Возраст:</span> {{ request.pet.age }} лет</p>
          <p><span>Пол:</span> {{ request.pet.is_male ? 'Самец' : 'Самка' }}</p>
          <p v-if="request.pet.description">
            <span>Описание:</span> {{ request.pet.description }}
          </p>
        </div>

        <div class="moderation-info">
          <h3>Новые данные на проверку</h3>
          <p v-if="request.animal_type">
            <span>Новый тип животного:</span> {{ request.animal_type }}
          </p>
          <p v-if="request.breed">
            <span>Новая порода:</span> {{ request.breed }}
          </p>
          <p class="date">
            Подано: {{ formatDate(request.created_at) }}
          </p>
        </div>

        <div class="actions">
          <button
            class="approve-btn"
            :disabled="processingId === request.id"
            @click="handleApprove(request.id)"
          >
            {{ processingId === request.id ? 'Обработка...' : 'Одобрить' }}
          </button>
          <button
            class="reject-btn"
            :disabled="processingId === request.id"
            @click="handleReject(request.id)"
          >
            Отклонить
          </button>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { getModerationRequests, approveRequest, rejectRequest } from '../api/index.js'

const router = useRouter()
const authStore = useAuthStore()

const requests = ref([])
const loading = ref(false)
const error = ref(null)
const processingId = ref(null)  // id заявки которая сейчас обрабатывается

async function fetchRequests() {
  loading.value = true
  error.value = null
  try {
    const response = await getModerationRequests()
    requests.value = response.data.results ?? response.data
  } catch (e) {
    error.value = 'Не удалось загрузить заявки'
  } finally {
    loading.value = false
  }
}

async function handleApprove(id) {
  processingId.value = id
  try {
    await approveRequest(id)
    requests.value = requests.value.filter(r => r.id !== id)
  } catch (e) {
    error.value = 'Ошибка при одобрении заявки'
  } finally {
    processingId.value = null
  }
}

async function handleReject(id) {
  processingId.value = id
  try {
    await rejectRequest(id)
    requests.value = requests.value.filter(r => r.id !== id)
  } catch (e) {
    error.value = 'Ошибка при отклонении заявки'
  } finally {
    processingId.value = null
  }
}

function handleLogout() {
  authStore.logout()
  router.push('/login')
}

function formatDate(dateString) {
  return new Date(dateString).toLocaleDateString('ru-RU', {
    day: 'numeric', month: 'long', year: 'numeric'
  })
}

// Загружаем заявки при открытии страницы
onMounted(fetchRequests)
</script>

<style scoped>
.moderation-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
}

header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

h1 { color: #333; }

.logout-btn {
  padding: 8px 16px;
  background: none;
  border: 1px solid #ccc;
  border-radius: 4px;
  cursor: pointer;
  color: #666;
}

.logout-btn:hover { background: #f5f5f5; }

.state-message {
  text-align: center;
  padding: 40px;
  color: #888;
  font-size: 16px;
}

.state-message.error { color: #c00; }

.requests-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.request-card {
  background: white;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 24px;
  display: grid;
  grid-template-columns: 1fr 1fr auto;
  gap: 24px;
  align-items: start;
}

.pet-info h2 {
  margin: 0 0 12px;
  color: #333;
}

.pet-info p, .moderation-info p {
  margin: 6px 0;
  color: #555;
  font-size: 14px;
}

.pet-info span, .moderation-info span {
  color: #888;
  margin-right: 4px;
}

.moderation-info h3 {
  margin: 0 0 12px;
  color: #333;
  font-size: 15px;
}

.date {
  margin-top: 12px !important;
  color: #aaa !important;
  font-size: 12px !important;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.approve-btn, .reject-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  white-space: nowrap;
}

.approve-btn {
  background: #4a9b6f;
  color: white;
}

.approve-btn:hover { background: #3d8560; }

.reject-btn {
  background: #e05555;
  color: white;
}

.reject-btn:hover { background: #c94444; }

.approve-btn:disabled, .reject-btn:disabled {
  background: #aaa;
  cursor: not-allowed;
}
</style>