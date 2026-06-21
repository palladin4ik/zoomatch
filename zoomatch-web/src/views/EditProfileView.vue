<template>
  <div class="edit-profile">
    <div class="edit-profile__header">
      <router-link to="/profile" class="edit-profile__back">←</router-link>
      <h1 class="edit-profile__title">Редактирование профиля</h1>
    </div>

    <UiLoader v-if="loading" text="Загрузка..." />

    <form v-else class="edit-profile__form" @submit.prevent="handleSave">
      <UiInput
        v-model="form.firstname"
        label="Имя"
        placeholder="Введите имя"
        :error="errors.firstname"
      />
      <UiInput
        v-model="form.lastname"
        label="Фамилия"
        placeholder="Введите фамилию"
        :error="errors.lastname"
      />
      <UiTextarea
        v-model="form.description"
        label="Описание"
        placeholder="Расскажите о себе"
        :rows="3"
      />
      <UiInput
        v-model="form.email"
        label="Email"
        type="email"
        placeholder="Введите email"
        :error="errors.email"
      />
      <UiInput
        v-model="form.phone_number"
        label="Телефон"
        type="tel"
        placeholder="+7 (999) 123-45-67"
        :error="errors.phone_number"
      />

      <div class="edit-profile__actions">
        <UiButton variant="secondary" @click="$router.push('/profile')">Отмена</UiButton>
        <UiButton type="submit" variant="primary" :loading="saving">Сохранить</UiButton>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user.js'
import UiInput from '../components/ui/UiInput.vue'
import UiTextarea from '../components/ui/UiTextarea.vue'
import UiButton from '../components/ui/UiButton.vue'
import UiLoader from '../components/ui/UiLoader.vue'

const router = useRouter()
const userStore = useUserStore()
const toast = inject('toast')

const saving = ref(false)
const loading = ref(true)
const form = reactive({
  firstname: '',
  lastname: '',
  description: '',
  email: '',
  phone_number: '',
})
const errors = reactive({
  firstname: '',
  lastname: '',
  email: '',
  phone_number: '',
})

onMounted(async () => {
  loading.value = true
  try {
    await userStore.fetchProfile()
    form.firstname = userStore.firstname
    form.lastname = userStore.lastname
    form.description = userStore.description || ''
    form.email = userStore.email
    form.phone_number = userStore.phone_number || ''
  } catch {
    // error handled by store + interceptor
  } finally {
    loading.value = false
  }
})

function validate() {
  let valid = true
  errors.firstname = ''
  errors.lastname = ''
  errors.email = ''
  errors.phone_number = ''

  if (!form.firstname.trim()) {
    errors.firstname = 'Имя обязательно'
    valid = false
  }
  if (!form.lastname.trim()) {
    errors.lastname = 'Фамилия обязательна'
    valid = false
  }
  if (form.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.email = 'Некорректный email'
    valid = false
  }
  if (form.phone_number && form.phone_number.replace(/\D/g, '').length < 10) {
    errors.phone_number = 'Некорректный номер'
    valid = false
  }
  return valid
}

async function handleSave() {
  if (!validate()) return
  saving.value = true
  try {
    await userStore.updateProfile({
      firstname: form.firstname.trim(),
      lastname: form.lastname.trim(),
      description: form.description?.trim() || '',
      email: form.email.trim(),
      phone_number: form.phone_number?.trim() || '',
    })
    router.push('/profile')
  } catch (e) {
    const data = e.response?.data
    if (data) {
      if (data.firstname) errors.firstname = Array.isArray(data.firstname) ? data.firstname[0] : data.firstname
      if (data.lastname) errors.lastname = Array.isArray(data.lastname) ? data.lastname[0] : data.lastname
      if (data.email) errors.email = Array.isArray(data.email) ? data.email[0] : data.email
      if (data.phone_number) errors.phone_number = Array.isArray(data.phone_number) ? data.phone_number[0] : data.phone_number
      if (data.detail) toast(data.detail, 'error')
    }
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.edit-profile {
  max-width: 500px;
  margin: 0 auto;
}

.edit-profile__header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 28px;
}

.edit-profile__back {
  font-size: 22px;
  color: var(--text-primary);
  text-decoration: none;
  padding: 4px 8px;
}

.edit-profile__title {
  font-size: 20px;
  font-weight: 600;
}

.edit-profile__form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.edit-profile__actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 12px;
}
</style>
