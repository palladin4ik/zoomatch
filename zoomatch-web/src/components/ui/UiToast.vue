<template>
  <Teleport to="body">
    <TransitionGroup name="toast" tag="div" class="ui-toast__container">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        :class="['ui-toast', `ui-toast--${toast.type}`]"
      >
        <span class="ui-toast__icon">
          {{ toast.type === 'success' ? '+' : toast.type === 'error' ? '!' : 'i' }}
        </span>
        <span class="ui-toast__message">{{ toast.message }}</span>
        <button class="ui-toast__close" @click="remove(toast.id)"><UiIcon name="close" size="sm" /></button>
      </div>
    </TransitionGroup>
  </Teleport>
</template>

<script setup>
import { ref } from 'vue'
import UiIcon from './UiIcon.vue'

const toasts = ref([])
let nextId = 0

function add(message, type = 'info', duration = 3000) {
  const id = nextId++
  toasts.value.push({ id, message, type })
  if (duration > 0) {
    setTimeout(() => remove(id), duration)
  }
}

function remove(id) {
  toasts.value = toasts.value.filter(t => t.id !== id)
}

function success(message, duration) { add(message, 'success', duration) }
function error(message, duration) { add(message, 'error', duration) }
function info(message, duration) { add(message, 'info', duration) }

defineExpose({ add, remove, success, error, info })
</script>

<style scoped>
.ui-toast__container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 2000;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ui-toast {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  min-width: 280px;
  max-width: 400px;
}

.ui-toast--success {
  background: #ECFDF5;
  color: var(--green-dark);
  border: 1px solid #A7F3D0;
}

.ui-toast--error {
  background: #FEF2F2;
  color: var(--red-accent);
  border: 1px solid #FECACA;
}

.ui-toast--info {
  background: var(--purple-bg);
  color: var(--purple-primary);
  border: 1px solid #DDD6FE;
}

.ui-toast__icon {
  font-size: 16px;
  font-weight: 700;
  flex-shrink: 0;
}

.ui-toast__message {
  flex: 1;
  font-size: 14px;
}

.ui-toast__close {
  background: none;
  border: none;
  font-size: 14px;
  color: inherit;
  opacity: 0.5;
  cursor: pointer;
  padding: 0;
}

.ui-toast__close:hover {
  opacity: 1;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(40px);
}
</style>
