<template>
  <div class="home">
    <UiLoader v-if="loading" text="Загрузка..." />

    <div v-else-if="error" class="home__error">
      <p>{{ error }}</p>
      <UiButton variant="secondary" @click="loadData">Повторить</UiButton>
    </div>

    <template v-else>
      <div class="home__stats">
        <div class="home__stat-card">
          <div class="home__stat-icon-wrap home__stat-icon-wrap--heart">
            <UiIcon name="heart" />
          </div>
          <div class="home__stat-body">
            <span class="home__stat-value">{{ matchesCount }}</span>
            <span class="home__stat-label">Мэтчи</span>
            <span class="home__stat-sub">+{{ matchesGrowth }} за вчера</span>
          </div>
        </div>

        <div class="home__stat-card">
          <div class="home__stat-icon-wrap home__stat-icon-wrap--star">
            <UiIcon name="star" />
          </div>
          <div class="home__stat-body">
            <span class="home__stat-value">{{ userRating }}</span>
            <span class="home__stat-label">Рейтинг</span>
          </div>
        </div>

        <div class="home__stat-card">
          <div class="home__stat-icon-wrap home__stat-icon-wrap--chat">
            <UiIcon name="chat" />
          </div>
          <div class="home__stat-body">
            <span class="home__stat-value">{{ chatsCount }}</span>
            <span class="home__stat-label">Чаты</span>
            <span class="home__stat-sub">{{ unreadCount }} новых</span>
          </div>
        </div>

        <div class="home__stat-card">
          <div class="home__stat-icon-wrap home__stat-icon-wrap--eye">
            <UiIcon name="eye" />
          </div>
          <div class="home__stat-body">
            <span class="home__stat-value">{{ viewsCount }}</span>
            <span class="home__stat-label">Просмотры</span>
            <span class="home__stat-sub">{{ viewsPeriodLabel }}</span>
          </div>
        </div>
      </div>

      <div class="home__activity">
        <div class="home__activity-header">
          <h2 class="home__section-title">Активность профиля</h2>
          <div class="home__activity-tabs">
            <button
              v-for="tab in chartTabs"
              :key="tab.key"
              :class="['home__activity-tab', { 'home__activity-tab--active': activeChartTab === tab.key }]"
              @click="activeChartTab = tab.key"
            >
              {{ tab.label }}
            </button>
          </div>
        </div>

        <div class="home__activity-card">
          <div class="home__activity-card-header">
            <span class="home__activity-label">Динамика совпадений</span>
            <span class="home__activity-growth">+{{ growthPercent }}% {{ growthPeriodLabel }}</span>
          </div>
          <div class="home__chart">
            <div
              v-for="(bar, index) in chartData"
              :key="index"
              class="home__chart-bar-wrapper"
            >
              <div class="home__chart-bar-track">
                <div
                  class="home__chart-bar"
                  :style="{ height: bar.height + '%' }"
                />
              </div>
              <span class="home__chart-label">{{ bar.label }}</span>
            </div>
          </div>
        </div>

        <div class="home__activity-stats">
          <div class="home__activity-stat">
            <span class="home__activity-stat-label">ЛАЙКИ</span>
            <span class="home__activity-stat-value">{{ likesCount }}</span>
          </div>
          <div class="home__activity-stat">
            <span class="home__activity-stat-label">КОНВЕРСИЯ</span>
            <span class="home__activity-stat-value">{{ conversionRate }}%</span>
          </div>
          <div class="home__activity-stat">
            <span class="home__activity-stat-label">РОСТ</span>
            <span class="home__activity-stat-value home__activity-stat-value--green">+{{ growthPercent }}%</span>
          </div>
        </div>
      </div>

      <div class="home__recommendation">
        <h2 class="home__section-title">Рекомендуемые действия</h2>
        <div class="home__recommendation-card">
          <div class="home__recommendation-header">
            <div class="home__recommendation-icon">
              <UiIcon name="paw" />
            </div>
            <div class="home__recommendation-info">
              <span class="home__recommendation-name">{{ recommendationTitle }}</span>
              <span :class="['home__recommendation-badge', recommendationBadgeClass]">{{ recommendationBadge }}</span>
            </div>
          </div>
          <p class="home__recommendation-desc">{{ recommendationDescription }}</p>
          <router-link :to="recommendationLink" class="home__recommendation-btn">
            {{ recommendationButtonText }}
          </router-link>
        </div>
      </div>

      <div class="home__pets-section">
        <div class="home__pets-header">
          <h2 class="home__section-title">Ваши питомцы</h2>
          <router-link to="/pets" class="home__manage-link">Управление списком</router-link>
        </div>

        <div class="home__pets-grid">
          <div
            v-for="pet in displayPets"
            :key="pet.id"
            class="home__pet-card"
          >
            <div class="home__pet-photo">
              <img v-if="getPetAvatar(pet)" :src="getPetAvatar(pet)" :alt="pet.name" />
              <UiIcon v-else name="paw" />
            </div>
            <div class="home__pet-info">
              <span class="home__pet-name">{{ pet.name }}</span>
              <span class="home__pet-age">{{ getAge(pet) }}</span>
              <span class="home__pet-breed">{{ getBreed(pet) }}</span>
            </div>
          </div>

          <router-link to="/pets/new" class="home__pet-card home__pet-card--add">
            <div class="home__pet-add-icon">
              <UiIcon name="plus" />
            </div>
            <span class="home__pet-add-text">Добавить ещё</span>
          </router-link>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../stores/user.js'
import { usePetsStore } from '../stores/pets.js'
import { useChatsStore } from '../stores/chats.js'
import { useMatchesStore } from '../stores/matches.js'
import UiLoader from '../components/ui/UiLoader.vue'
import UiButton from '../components/ui/UiButton.vue'
import UiIcon from '../components/ui/UiIcon.vue'

const userStore = useUserStore()
const petsStore = usePetsStore()
const chatsStore = useChatsStore()
const matchesStore = useMatchesStore()

const loading = ref(true)
const error = ref(null)
const activeChartTab = ref('day')
const viewsCount = ref(0)

const viewsPeriodLabel = computed(() => {
  if (activeChartTab.value === 'day') return 'За сегодня'
  if (activeChartTab.value === 'week') return 'За эту неделю'
  return 'За этот месяц'
})

const growthPeriodLabel = computed(() => {
  if (activeChartTab.value === 'day') return 'к вчера'
  if (activeChartTab.value === 'week') return 'к прошлой неделе'
  return 'к прошлому месяцу'
})

const acceptedMatches = computed(() => matchesStore.matches.filter(m => m.status === 1))

const matchesCount = computed(() => {
  const now = new Date()
  const matches = acceptedMatches.value
  if (activeChartTab.value === 'day') {
    const dayOfWeek = (now.getDay() + 6) % 7
    const start = new Date(now); start.setDate(now.getDate() - dayOfWeek); start.setHours(0, 0, 0, 0)
    const end = new Date(now); end.setHours(23, 59, 59, 999)
    return matches.filter(m => { const d = new Date(m.created_at); return d >= start && d <= end }).length
  }
  if (activeChartTab.value === 'week') {
    const start = new Date(now.getFullYear(), now.getMonth(), 1, 0, 0, 0, 0)
    const end = new Date(now); end.setHours(23, 59, 59, 999)
    return matches.filter(m => { const d = new Date(m.created_at); return d >= start && d <= end }).length
  }
  const start = new Date(now.getFullYear(), 0, 1, 0, 0, 0, 0)
  const end = new Date(now); end.setHours(23, 59, 59, 999)
  return matches.filter(m => { const d = new Date(m.created_at); return d >= start && d <= end }).length
})
const chatsCount = computed(() => chatsStore.chats.length)
const unreadCount = computed(() => chatsStore.totalUnread || 0)

const matchesGrowth = computed(() => {
  const now = new Date()
  return acceptedMatches.value.filter(m => {
    const d = new Date(m.created_at)
    const diff = Math.floor((now - d) / 86400000)
    return diff <= 1
  }).length
})

const userRating = computed(() => userStore.user?.rating || '0')

const likesCount = computed(() => Math.max(matchesCount.value, 0))

const conversionRate = computed(() => {
  if (viewsCount.value === 0) return '0'
  return ((matchesCount.value / viewsCount.value) * 100).toFixed(1)
})

const chartTabs = [
  { key: 'day', label: 'День' },
  { key: 'week', label: 'Неделя' },
  { key: 'month', label: 'Месяц' },
]

const chartData = computed(() => {
  const matches = acceptedMatches.value
  const now = new Date()

  const parsedMatches = matches.map(m => {
    try { return new Date(m.created_at.substring(0, 10)) }
    catch { return null }
  }).filter(d => d !== null)

  if (activeChartTab.value === 'day') {
    const labels = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс']
    const dayOfWeek = (now.getDay() + 6) % 7
    const startOfWeek = new Date(now)
    startOfWeek.setDate(now.getDate() - dayOfWeek)
    startOfWeek.setHours(0, 0, 0, 0)

    const values = labels.map((_, i) => {
      const targetDay = new Date(startOfWeek)
      targetDay.setDate(startOfWeek.getDate() + i)
      const start = new Date(targetDay); start.setHours(0, 0, 0, 0)
      const end = new Date(targetDay); end.setHours(23, 59, 59, 999)
      return parsedMatches.filter(d => d >= start && d <= end).length
    })
    const max = Math.max(...values, 1)
    return labels.map((label, i) => ({ label, height: Math.max((values[i] / max) * 100, 4) }))
  }

  if (activeChartTab.value === 'week') {
    const month = now.getMonth(), year = now.getFullYear()
    const startOfMonth = new Date(year, month, 1, 0, 0, 0, 0)
    const weeksInMonth = new Date(year, month + 1, 0).getDate() <= 28 ? 4 : 5
    const labels = Array.from({ length: weeksInMonth }, (_, i) => `Нед ${i + 1}`)
    const values = labels.map((_, wi) => {
      const ws = new Date(startOfMonth); ws.setDate(startOfMonth.getDate() + wi * 7); ws.setHours(0, 0, 0, 0)
      const we = new Date(ws); we.setDate(ws.getDate() + 6); we.setHours(23, 59, 59, 999)
      return parsedMatches.filter(d => d >= ws && d <= we).length
    })
    const max = Math.max(...values, 1)
    return labels.map((label, i) => ({ label, height: Math.max((values[i] / max) * 100, 4) }))
  }

  const monthLabels = ['Янв', 'Фев', 'Мар', 'Апр', 'Май', 'Июн', 'Июл', 'Авг', 'Сен', 'Окт', 'Ноя', 'Дек']
  const year = now.getFullYear()
  const values = monthLabels.map((_, m) => {
    const ms = new Date(year, m, 1, 0, 0, 0, 0)
    const me = new Date(year, m + 1, 0, 23, 59, 59, 999)
    return parsedMatches.filter(d => d >= ms && d <= me).length
  })
  const max = Math.max(...values, 1)
  return monthLabels.map((label, i) => ({ label, height: Math.max((values[i] / max) * 100, 4) }))
})

const growthPercent = computed(() => {
  const matches = acceptedMatches.value
  if (matches.length === 0) return 0
  const now = new Date()

  const parsedMatches = matches.map(m => {
    try { return new Date(m.created_at) }
    catch { return null }
  }).filter(d => d !== null)

  if (activeChartTab.value === 'day') {
    const todayStart = new Date(now); todayStart.setHours(0, 0, 0, 0)
    const todayEnd = new Date(now); todayEnd.setHours(23, 59, 59, 999)
    const yesterdayStart = new Date(todayStart); yesterdayStart.setDate(todayStart.getDate() - 1)
    const yesterdayEnd = new Date(todayStart); yesterdayEnd.setDate(todayStart.getDate() - 1); yesterdayEnd.setHours(23, 59, 59, 999)
    const today = parsedMatches.filter(d => d >= todayStart && d <= todayEnd).length
    const yesterday = parsedMatches.filter(d => d >= yesterdayStart && d <= yesterdayEnd).length
    if (today === 0) return 0
    if (yesterday === 0) return 100
    return Math.round(((today - yesterday) / yesterday) * 100)
  }

  if (activeChartTab.value === 'week') {
    const dayOfWeek = (now.getDay() + 6) % 7
    const thisWeekStart = new Date(now); thisWeekStart.setDate(now.getDate() - dayOfWeek); thisWeekStart.setHours(0, 0, 0, 0)
    const thisWeekEnd = new Date(thisWeekStart); thisWeekEnd.setDate(thisWeekStart.getDate() + 6); thisWeekEnd.setHours(23, 59, 59, 999)
    const lastWeekStart = new Date(thisWeekStart); lastWeekStart.setDate(thisWeekStart.getDate() - 7)
    const lastWeekEnd = new Date(thisWeekStart); lastWeekEnd.setDate(thisWeekStart.getDate() - 1); lastWeekEnd.setHours(23, 59, 59, 999)
    const thisWeek = parsedMatches.filter(d => d >= thisWeekStart && d <= thisWeekEnd).length
    const lastWeek = parsedMatches.filter(d => d >= lastWeekStart && d <= lastWeekEnd).length
    if (thisWeek === 0) return 0
    if (lastWeek === 0) return 100
    return Math.round(((thisWeek - lastWeek) / lastWeek) * 100)
  }

  const thisMonth = now.getMonth(), thisYear = now.getFullYear()
  const lastMonth = thisMonth === 0 ? 11 : thisMonth - 1
  const lastYear = thisMonth === 0 ? thisYear - 1 : thisYear
  const thisMonthCount = parsedMatches.filter(d => d.getMonth() === thisMonth && d.getFullYear() === thisYear).length
  const lastMonthCount = parsedMatches.filter(d => d.getMonth() === lastMonth && d.getFullYear() === lastYear).length
  if (thisMonthCount === 0) return 0
  if (lastMonthCount === 0) return 100
  return Math.round(((thisMonthCount - lastMonthCount) / lastMonthCount) * 100)
})

const totalPetsCount = computed(() => petsStore.myPets.length)
const approvedPetsCount = computed(() => petsStore.myPets.filter(p => p.moderation_status === 'approved').length)

const recommendationTitle = computed(() => {
  if (totalPetsCount.value === 0) return 'Добавьте питомца'
  if (approvedPetsCount.value === 0) return 'Питомцы на модерации'
  return 'Найдите пару'
})
const recommendationBadge = computed(() => {
  if (totalPetsCount.value === 0) return 'Начало'
  if (approvedPetsCount.value === 0) return 'Ожидание'
  return 'Поиск'
})
const recommendationBadgeClass = computed(() => {
  if (totalPetsCount.value === 0) return 'home__recommendation-badge--start'
  if (approvedPetsCount.value === 0) return 'home__recommendation-badge--wait'
  return 'home__recommendation-badge--search'
})
const recommendationDescription = computed(() => {
  if (totalPetsCount.value === 0) return 'Добавьте своего первого питомца, чтобы начать поиск идеальной пары.'
  if (approvedPetsCount.value === 0) {
    const count = totalPetsCount.value
    return `Ваш${count === 1 ? '' : 'и'} ${count} ${petDeclension(count)} проходят модерацию. После одобрения вы сможете начать поиск.`
  }
  const count = approvedPetsCount.value
  return `У вас ${count} ${petDeclension(count)}. Начните поиск идеальной пары!`
})
const recommendationLink = computed(() => totalPetsCount.value === 0 ? '/pets/new' : '/search')
const recommendationButtonText = computed(() => {
  if (totalPetsCount.value === 0) return 'Добавить питомца'
  if (approvedPetsCount.value === 0) return 'Ожидание модерации'
  return 'Начать поиск'
})

const petDeclension = (count) => {
  const abs = Math.abs(count)
  const mod100 = abs % 100
  const mod10 = abs % 10
  if (mod100 >= 11 && mod100 <= 19) return 'питомцев'
  if (mod10 === 1) return 'питомец'
  if (mod10 >= 2 && mod10 <= 4) return 'питомца'
  return 'питомцев'
}

const displayPets = computed(() => {
  const activePets = petsStore.myPets.filter(p => p.is_active && p.moderation_status === 'approved')
  const shuffled = [...activePets].sort(() => Math.random() - 0.5)
  return shuffled.slice(0, 3)
})

function getPetAvatar(pet) {
  if (!pet.avatar) return ''
  if (pet.avatar.startsWith('http')) return pet.avatar
  return `${import.meta.env.VITE_API_URL}${pet.avatar}`
}

function getAge(pet) {
  if (pet.age != null) {
    const y = pet.age
    return `${y} ${y === 1 ? 'год' : y < 5 ? 'года' : 'лет'}`
  }
  return ''
}

function getBreed(pet) {
  if (pet.breed?.name) return pet.breed.name
  if (pet.animal_type?.name) return pet.animal_type.name
  return ''
}

async function loadData() {
  loading.value = true
  error.value = null
  try {
    await Promise.all([
      userStore.fetchProfile(),
      petsStore.fetchMyPets(),
      chatsStore.fetchChats(),
      matchesStore.fetchMatches(),
    ])
  } catch (e) {
    error.value = e.response?.data?.detail || 'Не удалось загрузить данные'
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.home { max-width: 800px; margin: 0 auto; }
.home__error { text-align: center; padding: 40px; color: var(--red-accent); }
.home__stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 24px; }
.home__stat-card { background: var(--bg-white); border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: 16px; display: flex; align-items: flex-start; gap: 12px; }
.home__stat-icon-wrap { width: 40px; height: 40px; border-radius: var(--radius-md); display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.home__stat-icon-wrap--heart { background: #FEE2E2; color: #DC2626; }
.home__stat-icon-wrap--star { background: #FEF3C7; color: #D97706; }
.home__stat-icon-wrap--chat { background: #DBEAFE; color: #2563EB; }
.home__stat-icon-wrap--eye { background: #E0E7FF; color: #4F46E5; }
.home__stat-body { display: flex; flex-direction: column; gap: 2px; }
.home__stat-value { font-size: 24px; font-weight: 700; color: var(--text-primary); line-height: 1.1; }
.home__stat-label { font-size: 12px; color: var(--text-secondary); }
.home__stat-sub { font-size: 11px; color: var(--text-muted); margin-top: 2px; }

.home__activity { margin-bottom: 24px; }
.home__activity-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.home__section-title { font-size: 18px; font-weight: 700; color: var(--text-primary); }
.home__activity-tabs { display: flex; gap: 4px; }
.home__activity-tab { padding: 6px 14px; border: 1.5px solid var(--border-color); border-radius: var(--radius-full); background: var(--bg-white); font-size: 12px; font-weight: 500; cursor: pointer; transition: all var(--transition-fast); color: var(--text-secondary); }
.home__activity-tab:hover { border-color: var(--purple-primary); color: var(--purple-primary); }
.home__activity-tab--active { background: var(--purple-primary); color: var(--text-white); border-color: var(--purple-primary); }
.home__activity-card { background: var(--bg-white); border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: 16px; }
.home__activity-card-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.home__activity-label { font-size: 14px; font-weight: 700; color: var(--text-primary); }
.home__activity-growth { font-size: 12px; font-weight: 600; color: var(--green-accent); }
.home__chart { display: flex; align-items: flex-end; gap: 6px; height: 160px; }
.home__chart-bar-wrapper { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 4px; height: 100%; }
.home__chart-bar-track { flex: 1; width: 100%; display: flex; align-items: flex-end; justify-content: center; }
.home__chart-bar { width: 100%; max-width: 28px; background: var(--purple-primary); border-radius: 4px 4px 0 0; min-height: 4px; transition: height 0.3s ease; }
.home__chart-label { font-size: 10px; color: var(--text-muted); }
.home__activity-stats { display: flex; gap: 24px; margin-top: 12px; padding-top: 12px; border-top: 1px solid var(--border-color); }
.home__activity-stat { display: flex; flex-direction: column; gap: 2px; }
.home__activity-stat-label { font-size: 10px; font-weight: 600; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.5px; }
.home__activity-stat-value { font-size: 18px; font-weight: 700; color: var(--text-primary); }
.home__activity-stat-value--green { color: var(--green-accent); }

.home__recommendation { margin-bottom: 24px; }
.home__recommendation-card { background: var(--bg-white); border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: 16px; }
.home__recommendation-header { display: flex; align-items: center; gap: 12px; }
.home__recommendation-icon { width: 48px; height: 48px; background: var(--purple-bg); border-radius: var(--radius-md); display: flex; align-items: center; justify-content: center; color: var(--purple-primary); }
.home__recommendation-info { display: flex; flex-direction: column; gap: 4px; }
.home__recommendation-name { font-size: 16px; font-weight: 700; color: var(--text-primary); }
.home__recommendation-badge { display: inline-block; padding: 2px 8px; border-radius: var(--radius-full); font-size: 11px; font-weight: 700; text-transform: uppercase; align-self: flex-start; }
.home__recommendation-badge--start { background: var(--purple-bg); color: var(--purple-primary); }
.home__recommendation-badge--wait { background: #FFFBEB; color: #D97706; }
.home__recommendation-badge--search { background: #ECFDF5; color: var(--green-dark); }
.home__recommendation-desc { font-size: 13px; color: var(--text-secondary); line-height: 1.4; margin: 12px 0; }
.home__recommendation-btn { display: block; width: 100%; padding: 12px; background: var(--purple-primary); color: var(--text-white); border-radius: var(--radius-md); font-size: 14px; font-weight: 700; text-decoration: none; text-align: center; transition: background var(--transition-fast); }
.home__recommendation-btn:hover { background: var(--purple-dark); text-decoration: none; }

.home__pets-section { margin-bottom: 20px; }
.home__pets-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.home__manage-link { font-size: 14px; font-weight: 500; color: var(--purple-primary); text-decoration: none; }
.home__manage-link:hover { text-decoration: underline; }
.home__pets-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.home__pet-card { background: var(--bg-white); border: 1px solid var(--border-color); border-radius: var(--radius-lg); overflow: hidden; display: flex; flex-direction: column; }
.home__pet-photo { height: 120px; background: var(--purple-bg); display: flex; align-items: center; justify-content: center; color: var(--purple-primary); overflow: hidden; }
.home__pet-photo img { width: 100%; height: 100%; object-fit: cover; }
.home__pet-info { padding: 10px; display: flex; flex-direction: column; gap: 2px; }
.home__pet-name { font-size: 13px; font-weight: 600; color: var(--text-primary); }
.home__pet-age { font-size: 11px; color: var(--text-secondary); }
.home__pet-breed { font-size: 11px; color: var(--text-muted); }
.home__pet-card--add { border-style: dashed; border-color: var(--purple-primary); background: transparent; cursor: pointer; align-items: center; justify-content: center; min-height: 170px; text-decoration: none; }
.home__pet-card--add:hover { background: var(--purple-bg); }
.home__pet-add-icon { width: 40px; height: 40px; border-radius: 50%; background: var(--purple-bg); display: flex; align-items: center; justify-content: center; color: var(--purple-primary); margin-bottom: 8px; }
.home__pet-add-text { font-size: 12px; font-weight: 500; color: var(--purple-primary); text-align: center; }

@media (max-width: 768px) { .home__stats { grid-template-columns: repeat(2, 1fr); } .home__pets-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
