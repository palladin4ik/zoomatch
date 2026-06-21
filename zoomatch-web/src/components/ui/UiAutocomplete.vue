<template>
  <div :class="['ui-autocomplete', { 'ui-autocomplete--error': error, 'ui-autocomplete--disabled': disabled }]">
    <label v-if="label" class="ui-autocomplete__label">{{ label }}</label>
    <div class="ui-autocomplete__wrapper">
      <input
        ref="inputRef"
        type="text"
        :value="inputValue"
        :placeholder="placeholder"
        :disabled="disabled"
        class="ui-autocomplete__field"
        autocomplete="off"
        @input="onInput"
        @focus="open = true"
      />
      <span v-if="modelValue" class="ui-autocomplete__clear" @click="clearSelection">&times;</span>
    </div>
    <Transition name="dropdown">
      <div v-if="open && !disabled" class="ui-autocomplete__dropdown">
        <div
          v-for="option in filteredOptions"
          :key="option.value"
          :class="['ui-autocomplete__option', { 'ui-autocomplete__option--selected': option.value === modelValue }]"
          @mousedown.prevent="selectOption(option.value, option.label)"
        >
          {{ option.label }}
        </div>
        <div
          v-if="customLabel"
          class="ui-autocomplete__option ui-autocomplete__option--custom"
          @mousedown.prevent="selectCustom"
        >
          «{{ inputValue || customLabel }}» — свой вариант (модерация)
        </div>
        <div v-if="filteredOptions.length === 0 && !customLabel" class="ui-autocomplete__empty">
          Нет вариантов
        </div>
      </div>
    </Transition>
    <p v-if="error" class="ui-autocomplete__error">{{ error }}</p>
    <p v-else-if="helper" class="ui-autocomplete__helper">{{ helper }}</p>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  modelValue: { type: [Number, String, null], default: null },
  options: { type: Array, default: () => [] },
  label: { type: String, default: '' },
  placeholder: { type: String, default: 'Введите...' },
  error: { type: String, default: '' },
  helper: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
  customOptionLabel: { type: String, default: '' },
})

const emit = defineEmits(['update:modelValue', 'update:inputValue', 'custom'])

const inputRef = ref(null)
const open = ref(false)
const inputValue = ref('')

const customLabel = computed(() => {
  if (!props.customOptionLabel) return ''
  return props.customOptionLabel
})

const filteredOptions = computed(() => {
  if (!inputValue.value) return props.options
  const q = inputValue.value.toLowerCase()
  return props.options.filter(o => o.label.toLowerCase().includes(q))
})

watch(() => props.modelValue, (val) => {
  if (val === null || val === '') {
    inputValue.value = ''
    return
  }
  const opt = props.options.find(o => o.value === val)
  if (opt) inputValue.value = opt.label
}, { immediate: true })

watch(() => props.options, () => {
  if (props.modelValue !== null && props.modelValue !== '') {
    const opt = props.options.find(o => o.value === props.modelValue)
    if (opt) inputValue.value = opt.label
  }
})

function onInput(e) {
  inputValue.value = e.target.value
  open.value = true
  const exact = props.options.find(o => o.label.toLowerCase() === inputValue.value.toLowerCase())
  if (exact) {
    emit('update:modelValue', exact.value)
  } else {
    emit('update:modelValue', null)
  }
  emit('update:inputValue', inputValue.value)
}

function selectOption(value, label) {
  emit('update:modelValue', value)
  inputValue.value = label
  open.value = false
}

function selectCustom() {
  emit('update:modelValue', null)
  emit('custom', inputValue.value)
  open.value = false
}

function clearSelection() {
  emit('update:modelValue', null)
  inputValue.value = ''
  emit('update:inputValue', '')
  inputRef.value?.focus()
}

function handleClickOutside(e) {
  if (!e.target.closest('.ui-autocomplete')) open.value = false
}

onMounted(() => document.addEventListener('click', handleClickOutside))
onUnmounted(() => document.removeEventListener('click', handleClickOutside))
</script>

<style scoped>
.ui-autocomplete {
  display: flex;
  flex-direction: column;
  gap: 6px;
  position: relative;
}

.ui-autocomplete__label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.ui-autocomplete__wrapper {
  display: flex;
  align-items: center;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-white);
  transition: border-color var(--transition-fast);
}

.ui-autocomplete__wrapper:focus-within {
  border-color: var(--border-focus);
}

.ui-autocomplete--error .ui-autocomplete__wrapper {
  border-color: var(--red-accent);
}

.ui-autocomplete--disabled .ui-autocomplete__wrapper {
  background: var(--bg-hover);
  opacity: 0.6;
}

.ui-autocomplete__field {
  flex: 1;
  border: none;
  outline: none;
  padding: 10px 12px;
  font-size: 14px;
  background: transparent;
  min-width: 0;
}

.ui-autocomplete__field::placeholder {
  color: var(--text-muted);
}

.ui-autocomplete__clear {
  padding: 0 10px;
  font-size: 16px;
  color: var(--text-muted);
  cursor: pointer;
  line-height: 1;
}

.ui-autocomplete__clear:hover {
  color: var(--red-accent);
}

.ui-autocomplete__dropdown {
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

.ui-autocomplete__option {
  padding: 10px 12px;
  font-size: 14px;
  cursor: pointer;
  transition: background var(--transition-fast);
}

.ui-autocomplete__option:hover {
  background: var(--bg-hover);
}

.ui-autocomplete__option--selected {
  background: var(--purple-bg);
  color: var(--purple-primary);
  font-weight: 500;
}

.ui-autocomplete__option--custom {
  color: var(--text-muted);
  font-style: italic;
  border-top: 1px solid var(--border-color);
}

.ui-autocomplete__option--custom:hover {
  background: var(--purple-bg);
  color: var(--purple-primary);
}

.ui-autocomplete__empty {
  padding: 10px 12px;
  font-size: 14px;
  color: var(--text-muted);
  text-align: center;
}

.ui-autocomplete__error {
  font-size: 12px;
  color: var(--red-accent);
}

.ui-autocomplete__helper {
  font-size: 12px;
  color: var(--text-muted);
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
