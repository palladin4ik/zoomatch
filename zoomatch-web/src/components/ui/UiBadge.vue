<template>
  <span :class="['ui-badge', `ui-badge--${color}`]">
    <slot />
  </span>
</template>

<script setup>
defineProps({
  color: { type: String, default: 'purple' },
})
</script>

<style scoped>
.ui-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 2px 8px;
  font-size: 12px;
  font-weight: 600;
  border-radius: var(--radius-full);
  white-space: nowrap;
}

.ui-badge--purple {
  background: var(--purple-bg);
  color: var(--purple-primary);
}

.ui-badge--green {
  background: #ECFDF5;
  color: var(--green-dark);
}

.ui-badge--red {
  background: #FEF2F2;
  color: var(--red-accent);
}

.ui-badge--gray {
  background: var(--bg-hover);
  color: var(--text-secondary);
}
</style>
