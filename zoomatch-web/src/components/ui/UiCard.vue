<template>
  <div :class="['ui-card', { 'ui-card--hover': hover, 'ui-card--clickable': clickable }]" @click="$emit('click', $event)">
    <slot />
  </div>
</template>

<script setup>
defineProps({
  hover: { type: Boolean, default: false },
  clickable: { type: Boolean, default: false },
})

defineEmits(['click'])
</script>

<style scoped>
.ui-card {
  background: var(--bg-white);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  box-shadow: var(--shadow-sm);
}

.ui-card--hover:hover {
  box-shadow: var(--shadow-md);
}

.ui-card--clickable {
  cursor: pointer;
  transition: box-shadow var(--transition-fast);
}
</style>
