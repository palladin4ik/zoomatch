<template>
  <button
    :class="['ui-btn', `ui-btn--${variant}`, `ui-btn--${size}`, { 'ui-btn--block': block, 'ui-btn--loading': loading }]"
    :disabled="disabled || loading"
    @click="$emit('click', $event)"
  >
    <span v-if="loading" class="ui-btn__spinner"></span>
    <span v-else class="ui-btn__content">
      <slot />
    </span>
  </button>
</template>

<script setup>
defineProps({
  variant: { type: String, default: 'primary' },
  size: { type: String, default: 'md' },
  block: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
})

defineEmits(['click'])
</script>

<style scoped>
.ui-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: var(--radius-md);
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
  white-space: nowrap;
}

.ui-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.ui-btn--sm { padding: 6px 12px; font-size: 13px; }
.ui-btn--md { padding: 10px 20px; font-size: 14px; }
.ui-btn--lg { padding: 14px 28px; font-size: 16px; }

.ui-btn--primary {
  background: var(--purple-primary);
  color: var(--text-white);
}
.ui-btn--primary:hover:not(:disabled) {
  background: var(--purple-dark);
}

.ui-btn--secondary {
  background: transparent;
  color: var(--purple-primary);
  border: 1.5px solid var(--purple-primary);
}
.ui-btn--secondary:hover:not(:disabled) {
  background: var(--purple-bg);
}

.ui-btn--danger {
  background: var(--red-accent);
  color: var(--text-white);
}
.ui-btn--danger:hover:not(:disabled) {
  background: #DC2626;
}

.ui-btn--ghost {
  background: transparent;
  color: var(--text-secondary);
}
.ui-btn--ghost:hover:not(:disabled) {
  background: var(--bg-hover);
  color: var(--text-primary);
}

.ui-btn--block {
  width: 100%;
}

.ui-btn--loading {
  pointer-events: none;
}

.ui-btn__spinner {
  width: 16px;
  height: 16px;
  border: 2px solid currentColor;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
