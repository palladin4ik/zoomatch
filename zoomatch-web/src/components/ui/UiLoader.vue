<template>
  <div class="ui-loader">
    <div class="ui-loader__spinner"></div>
    <p v-if="text" class="ui-loader__text">{{ text }}</p>
  </div>
</template>

<script setup>
defineProps({
  text: { type: String, default: '' },
})
</script>

<style scoped>
.ui-loader {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 40px;
}

.ui-loader__spinner {
  width: 36px;
  height: 36px;
  border: 3px solid var(--border-color);
  border-top-color: var(--purple-primary);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

.ui-loader__text {
  font-size: 14px;
  color: var(--text-secondary);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
