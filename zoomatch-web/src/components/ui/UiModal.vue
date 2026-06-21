<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="modelValue" class="ui-modal__overlay" @click.self="close">
        <div :class="['ui-modal', `ui-modal--${size}`]">
          <div v-if="title" class="ui-modal__header">
            <h3 class="ui-modal__title">{{ title }}</h3>
            <button class="ui-modal__close" @click="close"><UiIcon name="close" /></button>
          </div>
          <div class="ui-modal__body">
            <slot />
          </div>
          <div v-if="$slots.footer" class="ui-modal__footer">
            <slot name="footer" />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '' },
  size: { type: String, default: 'md' },
})

import UiIcon from './UiIcon.vue'

const emit = defineEmits(['update:modelValue'])

function close() {
  emit('update:modelValue', false)
}
</script>

<style scoped>
.ui-modal__overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.ui-modal {
  background: var(--bg-white);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  max-height: 90vh;
  overflow-y: auto;
}

.ui-modal--sm { width: 400px; }
.ui-modal--md { width: 560px; }
.ui-modal--lg { width: 720px; }

.ui-modal__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px 0;
}

.ui-modal__title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.ui-modal__close {
  background: none;
  border: none;
  font-size: 18px;
  color: var(--text-muted);
  padding: 4px;
  cursor: pointer;
}

.ui-modal__close:hover {
  color: var(--text-primary);
}

.ui-modal__body {
  padding: 20px 24px;
}

.ui-modal__footer {
  padding: 0 24px 20px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s ease;
}

.modal-enter-active .ui-modal,
.modal-leave-active .ui-modal {
  transition: transform 0.2s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .ui-modal,
.modal-leave-to .ui-modal {
  transform: scale(0.95);
}
</style>
