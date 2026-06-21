<template>
  <div class="message-input">
    <div v-if="editingMessage" class="message-input__editing">
      <span>Редактирование: {{ editingMessage.text }}</span>
      <button class="message-input__cancel-edit" @click="$emit('cancel-edit')"><UiIcon name="close" /></button>
    </div>
    <div v-if="uploading" class="message-input__uploading">
      <div class="message-input__spinner"></div>
      <span>Отправка файла...</span>
    </div>
    <div class="message-input__row">
      <button class="message-input__attach" @click="$emit('attach')"><UiIcon name="attach" /></button>
      <input
        ref="inputRef"
        :value="modelValue"
        class="message-input__field"
        placeholder="Сообщение..."
        @input="$emit('update:modelValue', $event.target.value)"
        @keydown.enter="$emit('send')"
      />
      <button
        class="message-input__send"
        :disabled="!modelValue.trim()"
        @click="$emit('send')"
      >
        <UiIcon name="send" />
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import UiIcon from '../ui/UiIcon.vue'

defineProps({
  modelValue: { type: String, default: '' },
  editingMessage: { type: Object, default: null },
  uploading: { type: Boolean, default: false },
})

defineEmits(['update:modelValue', 'send', 'attach', 'cancel-edit'])

const inputRef = ref(null)

function focus() {
  inputRef.value?.focus()
}

defineExpose({ focus })
</script>

<style scoped>
.message-input {
  border-top: 1px solid var(--border-color);
  background: var(--bg-white);
}

.message-input__editing {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  background: var(--purple-bg);
  font-size: 13px;
  color: var(--purple-primary);
}

.message-input__cancel-edit {
  background: none;
  border: none;
  font-size: 14px;
  color: var(--text-muted);
  cursor: pointer;
}

.message-input__row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
}

.message-input__attach {
  background: none;
  border: none;
  font-size: 20px;
  color: var(--text-muted);
  cursor: pointer;
  padding: 4px;
}

.message-input__attach:hover {
  color: var(--purple-primary);
}

.message-input__field {
  flex: 1;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-full);
  padding: 10px 16px;
  font-size: 14px;
  outline: none;
  transition: border-color var(--transition-fast);
}

.message-input__field:focus {
  border-color: var(--border-focus);
}

.message-input__send {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: var(--purple-primary);
  color: var(--text-white);
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background var(--transition-fast);
}

.message-input__send:hover:not(:disabled) {
  background: var(--purple-dark);
}

.message-input__send:disabled {
  background: var(--border-color);
  cursor: not-allowed;
}

.message-input__uploading {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: var(--purple-bg);
  font-size: 13px;
  color: var(--purple-primary);
}

.message-input__spinner {
  width: 16px;
  height: 16px;
  border: 2px solid var(--purple-light);
  border-top-color: var(--purple-primary);
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
