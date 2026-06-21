<template>
  <div :class="['ui-textarea', { 'ui-textarea--error': error }]">
    <label v-if="label" class="ui-textarea__label">{{ label }}</label>
    <textarea
      :value="modelValue"
      :placeholder="placeholder"
      :rows="rows"
      :disabled="disabled"
      class="ui-textarea__field"
      @input="$emit('update:modelValue', $event.target.value)"
    ></textarea>
    <p v-if="error" class="ui-textarea__error">{{ error }}</p>
  </div>
</template>

<script setup>
defineProps({
  modelValue: { type: String, default: '' },
  label: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  rows: { type: Number, default: 4 },
  error: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
})

defineEmits(['update:modelValue'])
</script>

<style scoped>
.ui-textarea {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ui-textarea__label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.ui-textarea__field {
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 10px 12px;
  font-size: 14px;
  resize: vertical;
  min-height: 80px;
  outline: none;
  transition: border-color var(--transition-fast);
}

.ui-textarea__field:focus {
  border-color: var(--border-focus);
}

.ui-textarea--error .ui-textarea__field {
  border-color: var(--red-accent);
}

.ui-textarea__field:disabled {
  background: var(--bg-hover);
  opacity: 0.6;
}

.ui-textarea__error {
  font-size: 12px;
  color: var(--red-accent);
}
</style>
