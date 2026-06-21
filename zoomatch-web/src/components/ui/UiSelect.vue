<template>
  <div :class="['ui-select', { 'ui-select--error': error, 'ui-select--disabled': disabled }]">
    <label v-if="label" class="ui-select__label">{{ label }}</label>
    <div class="ui-select__wrapper" @click="toggleDropdown">
      <span class="ui-select__value">
        {{ selectedLabel || placeholder }}
      </span>
      <span :class="['ui-select__arrow', { 'ui-select__arrow--open': open }]">▾</span>
    </div>
    <Transition name="dropdown">
      <div v-if="open" class="ui-select__dropdown">
        <div
          v-for="option in options"
          :key="option.value"
          :class="['ui-select__option', { 'ui-select__option--selected': option.value === modelValue }]"
          @click="selectOption(option.value)"
        >
          {{ option.label }}
        </div>
        <div v-if="options.length === 0" class="ui-select__empty">Нет вариантов</div>
      </div>
    </Transition>
    <p v-if="error" class="ui-select__error">{{ error }}</p>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  modelValue: { type: [String, Number, null], default: null },
  options: { type: Array, default: () => [] },
  label: { type: String, default: '' },
  placeholder: { type: String, default: 'Выберите...' },
  error: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
})

const emit = defineEmits(['update:modelValue'])

const open = ref(false)

const selectedLabel = computed(() => {
  const opt = props.options.find(o => o.value === props.modelValue)
  return opt ? opt.label : ''
})

function toggleDropdown() {
  if (!props.disabled) open.value = !open.value
}

function selectOption(value) {
  emit('update:modelValue', value)
  open.value = false
}

function handleClickOutside(e) {
  if (!e.target.closest('.ui-select')) open.value = false
}

onMounted(() => document.addEventListener('click', handleClickOutside))
onUnmounted(() => document.removeEventListener('click', handleClickOutside))
</script>

<style scoped>
.ui-select {
  display: flex;
  flex-direction: column;
  gap: 6px;
  position: relative;
}

.ui-select__label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.ui-select__wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 10px 12px;
  background: var(--bg-white);
  cursor: pointer;
  transition: border-color var(--transition-fast);
}

.ui-select__wrapper:hover {
  border-color: var(--purple-light);
}

.ui-select--error .ui-select__wrapper {
  border-color: var(--red-accent);
}

.ui-select--disabled .ui-select__wrapper {
  background: var(--bg-hover);
  opacity: 0.6;
  cursor: not-allowed;
}

.ui-select__value {
  font-size: 14px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ui-select__value:empty::before {
  content: attr(data-placeholder);
  color: var(--text-muted);
}

.ui-select__arrow {
  font-size: 12px;
  color: var(--text-muted);
  transition: transform var(--transition-fast);
  margin-left: 8px;
}

.ui-select__arrow--open {
  transform: rotate(180deg);
}

.ui-select__dropdown {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  background: var(--bg-white);
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  z-index: 100;
  max-height: 200px;
  overflow-y: auto;
}

.ui-select__option {
  padding: 10px 12px;
  font-size: 14px;
  cursor: pointer;
  transition: background var(--transition-fast);
}

.ui-select__option:hover {
  background: var(--bg-hover);
}

.ui-select__option--selected {
  background: var(--purple-bg);
  color: var(--purple-primary);
  font-weight: 500;
}

.ui-select__empty {
  padding: 10px 12px;
  font-size: 14px;
  color: var(--text-muted);
  text-align: center;
}

.ui-select__error {
  font-size: 12px;
  color: var(--red-accent);
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
