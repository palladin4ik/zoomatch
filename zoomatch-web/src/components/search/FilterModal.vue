<template>
  <UiModal :model-value="modelValue" title="Фильтры" size="sm" @update:model-value="$emit('update:modelValue', $event)">
    <div class="filter-modal">
      <div class="filter-modal__field">
        <label class="filter-modal__label">Радиус поиска</label>
        <div class="filter-modal__options">
          <button
            v-for="option in radiusOptions"
            :key="option.value"
            :class="['filter-modal__option', { 'filter-modal__option--active': filters.radius_km === option.value }]"
            @click="filters.radius_km = option.value"
          >
            {{ option.label }}
          </button>
        </div>
      </div>

      <div class="filter-modal__field">
        <UiToggle v-model="filters.requires_pedigree" label="Только с родословной" />
      </div>

      <div class="filter-modal__field">
        <label class="filter-modal__label">Возраст (лет)</label>
        <div class="filter-modal__row">
          <UiInput v-model.number="filters.min_age" type="number" placeholder="От" />
          <UiInput v-model.number="filters.max_age" type="number" placeholder="До" />
        </div>
      </div>

      <div class="filter-modal__field">
        <label class="filter-modal__label">Макс. месяцев с момента вязки</label>
        <UiInput v-model.number="filters.max_months_since_mating" type="number" placeholder="Не ограничено" />
      </div>
    </div>

    <template #footer>
      <UiButton variant="secondary" @click="reset">Сбросить</UiButton>
      <UiButton variant="primary" @click="apply">Применить</UiButton>
    </template>
  </UiModal>
</template>

<script setup>
import { reactive } from 'vue'
import UiModal from '../ui/UiModal.vue'
import UiToggle from '../ui/UiToggle.vue'
import UiInput from '../ui/UiInput.vue'
import UiButton from '../ui/UiButton.vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  initialFilters: { type: Object, default: () => ({}) },
})

const emit = defineEmits(['update:modelValue', 'apply'])

const filters = reactive({
  radius_km: props.initialFilters.radius_km || 150,
  requires_pedigree: props.initialFilters.requires_pedigree || false,
  min_age: props.initialFilters.min_age || null,
  max_age: props.initialFilters.max_age || null,
  max_months_since_mating: props.initialFilters.max_months_since_mating || null,
})

const radiusOptions = [
  { value: null, label: 'Любой' },
  { value: 50, label: '50 км' },
  { value: 150, label: '150 км' },
  { value: 300, label: '300 км' },
]

function apply() {
  emit('apply', { ...filters })
  emit('update:modelValue', false)
}

function reset() {
  filters.radius_km = 150
  filters.requires_pedigree = false
  filters.min_age = null
  filters.max_age = null
  filters.max_months_since_mating = null
}
</script>

<style scoped>
.filter-modal {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.filter-modal__field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-modal__label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.filter-modal__options {
  display: flex;
  gap: 8px;
}

.filter-modal__option {
  flex: 1;
  padding: 10px;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-white);
  font-size: 14px;
  cursor: pointer;
  transition: all var(--transition-fast);
  text-align: center;
}

.filter-modal__option--active {
  background: var(--purple-primary);
  color: var(--text-white);
  border-color: var(--purple-primary);
}

.filter-modal__row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.filter-modal__row > * {
  min-width: 0;
}
</style>
