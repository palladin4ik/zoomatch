<template>
  <div class="settings">
    <h1 class="settings__title">Настройки</h1>

    <div class="settings__section">
      <h2 class="settings__section-title">Аккаунт</h2>
      <router-link to="/profile/edit" class="settings__link">
        <span class="settings__link-icon"><UiIcon name="edit" /></span>
        <span class="settings__link-text">Редактировать профиль</span>
        <span class="settings__link-arrow">→</span>
      </router-link>
    </div>

    <div class="settings__section">
      <h2 class="settings__section-title">Безопасность</h2>
      <div class="settings__form">
        <UiInput
          v-model="passwordForm.old_password"
          type="password"
          label="Текущий пароль"
          placeholder="Введите текущий пароль"
          :error="passwordErrors.old_password"
        />
        <UiInput
          v-model="passwordForm.new_password"
          type="password"
          label="Новый пароль"
          placeholder="Введите новый пароль"
          :error="passwordErrors.new_password"
        />
        <UiInput
          v-model="passwordForm.new_password2"
          type="password"
          label="Подтвердите пароль"
          placeholder="Повторите новый пароль"
          :error="passwordErrors.new_password2"
        />
        <UiButton variant="primary" :loading="changingPassword" @click="handleChangePassword">
          Изменить пароль
        </UiButton>
      </div>
    </div>

    <div class="settings__section">
      <h2 class="settings__section-title">Опасная зона</h2>
      <div class="settings__danger">
        <p class="settings__danger-text">
          Удаление аккаунта необратимо. Все данные будут удалены.
        </p>
        <UiButton variant="danger" :loading="deleting" @click="handleDeleteAccount">
          Удалить аккаунт
        </UiButton>
      </div>
    </div>

    <div class="settings__section">
      <UiButton variant="ghost" block @click="handleLogout">
        Выйти из аккаунта
      </UiButton>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { useUserStore } from '../stores/user.js'
import { changePassword, deleteMe } from '../api/index.js'
import UiInput from '../components/ui/UiInput.vue'
import UiButton from '../components/ui/UiButton.vue'
import UiIcon from '../components/ui/UiIcon.vue'

const router = useRouter()
const authStore = useAuthStore()
const userStore = useUserStore()
const toast = inject('toast')

const changingPassword = ref(false)
const deleting = ref(false)

const passwordForm = reactive({
  old_password: '',
  new_password: '',
  new_password2: '',
})
const passwordErrors = reactive({
  old_password: '',
  new_password: '',
  new_password2: '',
})

async function handleChangePassword() {
  passwordErrors.old_password = ''
  passwordErrors.new_password = ''
  passwordErrors.new_password2 = ''

  if (!passwordForm.old_password) {
    passwordErrors.old_password = 'Введите текущий пароль'
    return
  }
  if (passwordForm.new_password.length < 6) {
    passwordErrors.new_password = 'Пароль должен быть не менее 6 символов'
    return
  }
  if (passwordForm.new_password !== passwordForm.new_password2) {
    passwordErrors.new_password2 = 'Пароли не совпадают'
    return
  }

  changingPassword.value = true
  try {
    await changePassword({
      old_password: passwordForm.old_password,
      new_password: passwordForm.new_password,
    })
    toast('Пароль успешно изменён', 'success')
    passwordForm.old_password = ''
    passwordForm.new_password = ''
    passwordForm.new_password2 = ''
  } catch (e) {
    const data = e.response?.data
    if (data?.old_password) passwordErrors.old_password = data.old_password[0]
    if (data?.new_password) passwordErrors.new_password = data.new_password[0]
    if (data?.detail) toast(data.detail, 'error')
  } finally {
    changingPassword.value = false
  }
}

async function handleDeleteAccount() {
  if (!confirm('Вы уверены? Это действие необратимо.')) return
  deleting.value = true
  try {
    await deleteMe()
    userStore.clear()
    authStore.logout()
    router.push('/login')
  } catch {
    toast('Ошибка при удалении аккаунта', 'error')
  } finally {
    deleting.value = false
  }
}

function handleLogout() {
  userStore.clear()
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.settings {
  max-width: 500px;
  margin: 0 auto;
}

.settings__title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 28px;
}

.settings__section {
  margin-bottom: 28px;
}

.settings__section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.settings__link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: var(--bg-white);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  text-decoration: none;
  color: var(--text-primary);
  transition: background var(--transition-fast);
}

.settings__link:hover {
  background: var(--bg-hover);
  text-decoration: none;
}

.settings__link-icon {
  font-size: 18px;
}

.settings__link-text {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
}

.settings__link-arrow {
  color: var(--text-muted);
}

.settings__form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.settings__danger {
  background: #FEF2F2;
  border: 1px solid #FECACA;
  border-radius: var(--radius-md);
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.settings__danger-text {
  font-size: 14px;
  color: var(--red-accent);
  flex: 1;
}
</style>
