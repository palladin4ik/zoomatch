<template>
  <div :class="['ui-input', { 'ui-input--error': error, 'ui-input--disabled': disabled }]">
    <label v-if="label" class="ui-input__label">{{ label }}</label>
    <div class="ui-input__wrapper">
      <span v-if="$slots.prefix" class="ui-input__prefix"><slot name="prefix" /></span>
      <input
        :type="type"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        class="ui-input__field"
        @input="$emit('update:modelValue', $event.target.value)"
      />
      <span v-if="$slots.suffix" class="ui-input__suffix"><slot name="suffix" /></span>
    </div>
    <p v-if="error" class="ui-input__error">{{ error }}</p>
  </div>
</template>

<script setup>
defineProps({
  modelValue: { type: [String, Number], default: '' },
  label: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  type: { type: String, default: 'text' },
  error: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
})

defineEmits(['update:modelValue'])
</script>

<style scoped>
.ui-input {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ui-input__label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.ui-input__wrapper {
  display: flex;
  align-items: center;
  width: 100%;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-white);
  transition: border-color var(--transition-fast);
}

.ui-input__wrapper:focus-within {
  border-color: var(--border-focus);
}

.ui-input--error .ui-input__wrapper {
  border-color: var(--red-accent);
}

.ui-input--disabled .ui-input__wrapper {
  background: var(--bg-hover);
  opacity: 0.6;
}

.ui-input__prefix,
.ui-input__suffix {
  display: flex;
  align-items: center;
  padding: 0 12px;
  color: var(--text-muted);
}

.ui-input__field {
  flex: 1;
  border: none;
  outline: none;
  padding: 10px 12px;
  font-size: 14px;
  background: transparent;
  min-width: 0;
}

.ui-input__field::placeholder {
  color: var(--text-muted);
}

.ui-input__error {
  font-size: 12px;
  color: var(--red-accent);
}
</style>
