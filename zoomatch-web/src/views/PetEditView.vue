<template>
  <div class="pet-edit">
    <div class="pet-edit__header">
      <router-link :to="isEdit ? `/pets/${petId}` : '/pets'" class="pet-edit__back">←</router-link>
      <h1 class="pet-edit__title">{{ isEdit ? 'Редактирование питомца' : 'Новый питомец' }}</h1>
    </div>

    <UiLoader v-if="isEdit && petsStore.loading" text="Загрузка..." />

    <form v-else class="pet-edit__form" @submit.prevent="handleSave">
      <div class="pet-edit__photo" @click="triggerPhotoUpload">
        <input ref="photoInput" type="file" accept="image/*" class="pet-edit__photo-input" @change="handlePhoto" />
        <img v-if="photoPreview" :src="photoPreview" class="pet-edit__photo-preview" />
        <div v-else class="pet-edit__photo-placeholder">
          <UiIcon name="plus" size="lg" />
          <p>Нажмите для загрузки фото</p>
        </div>
      </div>

      <UiInput
        v-model="form.name"
        label="Кличка"
        placeholder="Например, Арчи"
        :error="errors.name"
      />

      <UiAutocomplete
        v-model="form.animal_type"
        :options="typeOptions"
        label="Вид животного"
        placeholder="Начните вводить вид..."
        :custom-option-label="'Вид'"
        @custom="onCustomAnimalType"
        @update:model-value="onAnimalTypeSelect"
      />
      <p class="pet-edit__helper">Если вид новый, питомец уйдёт на модерацию</p>

      <UiAutocomplete
        v-model="form.breed"
        :options="breedOptions"
        label="Порода"
        placeholder="Начните вводить породу..."
        :disabled="breedDisabled"
        :custom-option-label="'Порода'"
        @custom="onCustomBreed"
      />
      <p class="pet-edit__helper">{{ !breedDisabled ? 'Если порода новая, питомец уйдёт на модерацию' : 'Сначала выберите вид животного' }}</p>

      <div class="pet-edit__field">
        <label class="pet-edit__label">Пол</label>
        <div class="pet-edit__gender">
          <button
            type="button"
            :class="['pet-edit__gender-btn', { 'pet-edit__gender-btn--active': form.is_male }]"
            @click="form.is_male = true"
          >
            <UiIcon name="male" /> Мужской
          </button>
          <button
            type="button"
            :class="['pet-edit__gender-btn', 'pet-edit__gender-btn--female', { 'pet-edit__gender-btn--active': !form.is_male }]"
            @click="form.is_male = false"
          >
            <UiIcon name="female" /> Женский
          </button>
        </div>
      </div>

      <UiInput
        v-model.number="form.age"
        label="Возраст (лет)"
        type="number"
        placeholder="0"
        :error="errors.age"
      />

      <UiTextarea
        v-model="form.description"
        label="Описание питомца"
        placeholder="Расскажите о характере, достижениях или привычках вашего питомца..."
        :rows="4"
      />

      <div class="pet-edit__field">
        <UiToggle v-model="form.is_active" label="Готов к вязке" />
        <p class="pet-edit__helper">Профиль будет виден в поиске партнеров</p>
      </div>

      <div class="pet-edit__field">
        <UiToggle v-model="form.has_pedigree" label="Есть родословная" />
      </div>

      <div class="pet-edit__field">
        <label class="pet-edit__label">Местоположение</label>
        <UiInput
          v-model="form.address"
          placeholder="Город, улица, дом..."
        />
        <UiMap
          v-model:latitude="form.latitude"
          v-model:longitude="form.longitude"
          :address="form.address"
        />
      </div>

      <div class="pet-edit__field">
        <label class="pet-edit__label">Документы питомца</label>
        <UiFileUpload
          v-model="pedigreeFile"
          :existing-url="existingDocUrl"
          @update:existing-url="existingDocUrl = $event"
          accept=".pdf"
          text="Ветпаспорт, веткнижка или документы родословной (PDF)"
        />
      </div>

      <div class="pet-edit__actions">
        <UiButton v-if="isEdit" variant="danger" type="button" @click="handleDelete">Удалить питомца</UiButton>
        <div class="pet-edit__actions-right">
          <UiButton variant="secondary" @click="$router.back()">Отмена</UiButton>
          <UiButton type="submit" variant="primary" :loading="saving">Сохранить</UiButton>
        </div>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePetsStore } from '../stores/pets.js'
import { useAnimalTypesStore } from '../stores/animalTypes.js'
import { reverseGeocode } from '../lib/geocoder.js'
import UiInput from '../components/ui/UiInput.vue'
import UiAutocomplete from '../components/ui/UiAutocomplete.vue'
import UiTextarea from '../components/ui/UiTextarea.vue'
import UiToggle from '../components/ui/UiToggle.vue'
import UiButton from '../components/ui/UiButton.vue'
import UiLoader from '../components/ui/UiLoader.vue'
import UiFileUpload from '../components/ui/UiFileUpload.vue'
import UiIcon from '../components/ui/UiIcon.vue'
import UiMap from '../components/ui/UiMap.vue'

const route = useRoute()
const router = useRouter()
const petsStore = usePetsStore()
const animalTypesStore = useAnimalTypesStore()
const toast = inject('toast')

const petId = computed(() => route.params.id)
const isEdit = computed(() => route.name === 'pet-edit')

const photoInput = ref(null)
const photoPreview = ref(null)
const photoFile = ref(null)
const pedigreeFile = ref(null)
const existingDocUrl = ref(null)
const saving = ref(false)

const form = reactive({
  name: '',
  animal_type: null,
  animal_type_custom: '',
  breed: null,
  breed_custom: '',
  is_male: true,
  age: null,
  description: '',
  has_pedigree: false,
  is_active: false,
  latitude: null,
  longitude: null,
  address: '',
})
const errors = reactive({ name: '', age: '' })

const typeOptions = computed(() =>
  animalTypesStore.types.map(t => ({ value: t.id, label: t.name }))
)

const breedOptions = computed(() => {
  const typeId = form.animal_type
  return animalTypesStore.breeds
    .filter(b => !typeId || b.animal_type?.id === typeId)
    .map(b => ({ value: b.id, label: b.name }))
})

const breedDisabled = computed(() =>
  !form.animal_type && !form.animal_type_custom
)

let loadingPet = false

watch(() => form.animal_type, (typeId) => {
  if (loadingPet) return
  form.breed = null
  form.breed_custom = ''
  if (typeId) animalTypesStore.fetchBreeds(typeId)
  else animalTypesStore.breeds = []
})

watch([() => form.latitude, () => form.longitude], async ([lat, lng]) => {
  if (lat == null || lng == null) return
  const addr = await reverseGeocode(lat, lng)
  if (addr) form.address = addr
})

function onCustomAnimalType(text) {
  form.animal_type = null
  form.animal_type_custom = text
}

function onAnimalTypeSelect(val) {
  if (val) form.animal_type_custom = ''
}

function onCustomBreed(text) {
  form.breed = null
  form.breed_custom = text
}

function triggerPhotoUpload() {
  photoInput.value?.click()
}

function handlePhoto(e) {
  const file = e.target.files?.[0]
  if (!file) return
  photoFile.value = file
  photoPreview.value = URL.createObjectURL(file)
}

function validate() {
  let valid = true
  errors.name = ''
  errors.age = ''
  if (!form.name.trim()) { errors.name = 'Введите кличку'; valid = false }
  if (!form.age || form.age <= 0) { errors.age = 'Введите возраст'; valid = false }
  return valid
}

async function handleSave() {
  if (!validate()) return
  saving.value = true
  try {
    const payload = {
      name: form.name.trim(),
      is_male: form.is_male,
      age: form.age,
      description: form.description?.trim() || '',
      has_pedigree: form.has_pedigree,
      is_active: form.is_active,
      latitude: form.latitude,
      longitude: form.longitude,
    }
    if (form.latitude == null || form.longitude == null) {
      payload.address = form.address?.trim() || ''
    }
    if (form.animal_type) {
      payload.animal_type = form.animal_type
    } else if (form.animal_type_custom) {
      payload.animal_type_custom = form.animal_type_custom
    }
    if (form.breed) {
      payload.breed = form.breed
    } else if (form.breed_custom) {
      payload.breed_custom = form.breed_custom
    }
    let savedPet
    if (isEdit.value) {
      savedPet = await petsStore.updatePet(petId.value, payload)
    } else {
      savedPet = await petsStore.createPet(payload)
    }
    if (photoFile.value && savedPet?.id) {
      await petsStore.uploadAvatar(savedPet.id, photoFile.value)
    }
    if (pedigreeFile.value && savedPet?.id) {
      await petsStore.uploadDocument(savedPet.id, pedigreeFile.value)
    }
    router.push(`/pets/${savedPet.id}`)
  } catch (e) {
    const data = e.response?.data
    if (data?.name) errors.name = Array.isArray(data.name) ? data.name[0] : data.name
    if (data?.age) errors.age = Array.isArray(data.age) ? data.age[0] : data.age
    if (data?.address) toast(Array.isArray(data.address) ? data.address[0] : data.address, 'error')
    if (data?.detail) toast(data.detail, 'error')
    if (data?.non_field_errors) toast(Array.isArray(data.non_field_errors) ? data.non_field_errors[0] : data.non_field_errors, 'error')
    if (!data?.name && !data?.age && !data?.address && !data?.detail && !data?.non_field_errors) {
      const firstKey = Object.keys(data || {})[0]
      if (firstKey) {
        const val = data[firstKey]
        toast(Array.isArray(val) ? val[0] : String(val), 'error')
      }
    }
  } finally {
    saving.value = false
  }
}

async function handleDelete() {
  if (!confirm('Удалить питомца?')) return
  try {
    await petsStore.deletePet(petId.value)
  } finally {
    petsStore.currentPet = null
    router.push('/pets')
  }
}

function parseLocation(location) {
  if (!location || typeof location !== 'string') return
  const parts = location.split(',')
  if (parts.length === 2) {
    const lat = parseFloat(parts[0].trim())
    const lng = parseFloat(parts[1].trim())
    if (!isNaN(lat) && !isNaN(lng)) {
      form.latitude = lat
      form.longitude = lng
    }
  }
}

async function loadData() {
  await animalTypesStore.fetchTypes()
  if (isEdit.value && petId.value) {
    await petsStore.fetchPet(petId.value)
    const pet = petsStore.currentPet
    if (pet) {
      loadingPet = true

      form.name = pet.name
      form.is_male = pet.is_male
      form.age = pet.age
      form.description = pet.description || ''
      form.has_pedigree = pet.has_pedigree || false
      form.is_active = pet.is_active

      if (pet.animal_type) {
        form.animal_type = pet.animal_type.id
        await animalTypesStore.fetchBreeds(pet.animal_type.id)
      } else if (pet.animal_type_custom) {
        form.animal_type_custom = pet.animal_type_custom
      }
      if (pet.breed) {
        form.breed = pet.breed.id
      } else if (pet.breed_custom) {
        form.breed_custom = pet.breed_custom
      }

      parseLocation(pet.location)

      if (form.latitude && form.longitude) {
        try {
          const { reverseGeocode } = await import('../lib/geocoder.js')
          const addr = await reverseGeocode(form.latitude, form.longitude)
          if (addr) form.address = addr
        } catch (_) {}
      }

      loadingPet = false

      if (pet.avatar) {
        const url = pet.avatar.startsWith('http') ? pet.avatar : `${import.meta.env.VITE_API_URL}${pet.avatar}`
        photoPreview.value = url
      }

      if (pet.pedigree_documents) {
        existingDocUrl.value = pet.pedigree_documents
      }
    }
  }
}

onMounted(loadData)
</script>

<style scoped>
.pet-edit {
  max-width: 500px;
  margin: 0 auto;
}

.pet-edit__header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.pet-edit__back {
  font-size: 22px;
  color: var(--text-primary);
  text-decoration: none;
  padding: 4px 8px;
}

.pet-edit__title {
  font-size: 20px;
  font-weight: 600;
}

.pet-edit__form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.pet-edit__photo {
  width: 160px;
  height: 160px;
  border: 2px dashed var(--border-color);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  transition: border-color var(--transition-fast);
}

.pet-edit__photo:hover {
  border-color: var(--purple-primary);
}

.pet-edit__photo-input {
  display: none;
}

.pet-edit__photo-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pet-edit__photo-placeholder {
  text-align: center;
  color: var(--text-muted);
}

.pet-edit__photo-placeholder span {
  font-size: 36px;
}

.pet-edit__photo-placeholder p {
  font-size: 12px;
  margin-top: 4px;
}

.pet-edit__field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.pet-edit__label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.pet-edit__helper {
  font-size: 12px;
  color: var(--text-muted);
}

.pet-edit__gender {
  display: flex;
  gap: 12px;
}

.pet-edit__gender-btn {
  flex: 1;
  padding: 10px;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-white);
  font-size: 14px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.pet-edit__gender-btn--active {
  background: var(--purple-primary);
  color: var(--text-white);
  border-color: var(--purple-primary);
}

.pet-edit__gender-btn--active.pet-edit__gender-btn--female {
  background: #EC4899;
  border-color: #EC4899;
}

.pet-edit__actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.pet-edit__actions-right {
  display: flex;
  gap: 12px;
}
</style>
