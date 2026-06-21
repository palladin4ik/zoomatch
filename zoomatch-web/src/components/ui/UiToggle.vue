<template>
  <label :class="['ui-toggle', { 'ui-toggle--active': modelValue, 'ui-toggle--disabled': disabled }]">
    <span class="ui-toggle__track">
      <span class="ui-toggle__thumb"></span>
    </span>
    <input
      type="checkbox"
      :checked="modelValue"
      :disabled="disabled"
      class="ui-toggle__input"
      @change="$emit('update:modelValue', $event.target.checked)"
    />
    <span v-if="label" class="ui-toggle__label">{{ label }}</span>
  </label>
</template>

<script setup>
defineProps({
  modelValue: { type: Boolean, default: false },
  label: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
})

defineEmits(['update:modelValue'])
</script>

<style scoped>
.ui-toggle {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.ui-toggle--disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.ui-toggle__input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}

.ui-toggle__track {
  position: relative;
  width: 44px;
  height: 24px;
  background: var(--border-color);
  border-radius: var(--radius-full);
  transition: background var(--transition-fast);
}

.ui-toggle--active .ui-toggle__track {
  background: var(--purple-primary);
}

.ui-toggle__thumb {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 20px;
  height: 20px;
  background: var(--bg-white);
  border-radius: 50%;
  transition: transform var(--transition-fast);
  box-shadow: var(--shadow-sm);
}

.ui-toggle--active .ui-toggle__thumb {
  transform: translateX(20px);
}

.ui-toggle__label {
  font-size: 14px;
  color: var(--text-primary);
}
</style>
