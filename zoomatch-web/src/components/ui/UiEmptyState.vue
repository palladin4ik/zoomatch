<template>
  <div class="ui-empty-state">
    <div class="ui-empty-state__icon">{{ icon }}</div>
    <h3 class="ui-empty-state__title">{{ title }}</h3>
    <p v-if="description" class="ui-empty-state__description">{{ description }}</p>
    <slot />
  </div>
</template>

<script setup>
defineProps({
  icon: { type: String, default: '' },
  title: { type: String, default: 'Ничего не найдено' },
  description: { type: String, default: '' },
})
</script>

<style scoped>
.ui-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 48px 24px;
  text-align: center;
}

.ui-empty-state__icon {
  font-size: 48px;
}

.ui-empty-state__title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.ui-empty-state__description {
  font-size: 14px;
  color: var(--text-secondary);
  max-width: 320px;
}
</style>
