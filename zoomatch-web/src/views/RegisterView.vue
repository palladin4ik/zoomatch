<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-logo">
        <UiIcon name="paw" size="lg" class="register-logo__icon" />
        <h1 class="register-logo__text">ZooMatch</h1>
      </div>
      <p class="register-subtitle">Создайте аккаунт</p>

      <div v-if="error" class="register-error">{{ error }}</div>

      <form class="register-form" @submit.prevent="handleRegister">
        <UiInput v-model="firstname" placeholder="Имя" />
        <UiInput v-model="lastname" placeholder="Фамилия" />
        <UiInput v-model="email" type="email" placeholder="Email" />
        <UiInput v-model="password" type="password" placeholder="Пароль" />
        <UiInput v-model="passwordConfirm" type="password" placeholder="Подтвердите пароль" />
        <UiButton type="submit" variant="primary" block :loading="loading">
          Зарегистрироваться
        </UiButton>
      </form>

      <p class="register-login">
        Уже есть аккаунт?
        <router-link to="/login">Войти</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import UiInput from '../components/ui/UiInput.vue'
import UiButton from '../components/ui/UiButton.vue'
import UiIcon from '../components/ui/UiIcon.vue'

const router = useRouter()
const authStore = useAuthStore()

const firstname = ref('')
const lastname = ref('')
const email = ref('')
const password = ref('')
const passwordConfirm = ref('')
const error = ref(null)
const loading = ref(false)

async function handleRegister() {
  error.value = null
  if (!firstname.value.trim()) {
    error.value = 'Введите имя'
    return
  }
  if (!lastname.value.trim()) {
    error.value = 'Введите фамилию'
    return
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value.trim())) {
    error.value = 'Некорректный формат email'
    return
  }
  if (password.value.length < 6) {
    error.value = 'Пароль должен быть не менее 6 символов'
    return
  }
  if (password.value !== passwordConfirm.value) {
    error.value = 'Пароли не совпадают'
    return
  }
  loading.value = true
  try {
    await authStore.registerAction(firstname.value.trim(), lastname.value.trim(), email.value.trim(), password.value)
    router.push('/')
  } catch (e) {
    error.value = e.response?.data?.detail || 'Ошибка регистрации'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--bg-main);
  padding: 20px;
}

.register-box {
  background: var(--bg-white);
  padding: 40px;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  width: 100%;
  max-width: 400px;
}

.register-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 8px;
}

.register-logo__icon {
  font-size: 32px;
}

.register-logo__text {
  font-size: 28px;
  font-weight: 700;
  color: var(--purple-primary);
}

.register-subtitle {
  text-align: center;
  color: var(--text-secondary);
  margin-bottom: 28px;
  font-size: 14px;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.register-error {
  background: #FEF2F2;
  color: var(--red-accent);
  padding: 12px;
  border-radius: var(--radius-md);
  margin-bottom: 16px;
  font-size: 14px;
  text-align: center;
}

.register-login {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: var(--text-secondary);
}

.register-login a {
  color: var(--purple-primary);
  font-weight: 500;
}
</style>
