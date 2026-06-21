<template>
  <div
    :class="['ui-file-upload', { 'ui-file-upload--dragging': dragging, 'ui-file-upload--has-file': !!modelValue }]"
    @dragover.prevent="dragging = true"
    @dragleave="dragging = false"
    @drop.prevent="handleDrop"
    @click="openPicker"
  >
    <input
      ref="inputRef"
      type="file"
      :accept="accept"
      class="ui-file-upload__input"
      @change="handleChange"
    />
    <div v-if="modelValue" class="ui-file-upload__preview">
      <span class="ui-file-upload__name">{{ modelValue.name }}</span>
      <button class="ui-file-upload__remove" @click.stop="removeFile"><UiIcon name="close" size="sm" /></button>
    </div>
    <div v-else class="ui-file-upload__placeholder">
      <span class="ui-file-upload__icon"><UiIcon name="attach" /></span>
      <span class="ui-file-upload__text">{{ text }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import UiIcon from './UiIcon.vue'

const props = defineProps({
  modelValue: { type: Object, default: null },
  accept: { type: String, default: '*' },
  text: { type: String, default: 'Нажмите или перетащите файл' },
  maxSizeMB: { type: Number, default: 10 },
})

const emit = defineEmits(['update:modelValue', 'error'])

const inputRef = ref(null)
const dragging = ref(false)

function validateFile(file) {
  if (!file) return false
  if (props.maxSizeMB && file.size > props.maxSizeMB * 1024 * 1024) {
    emit('error', `Файл слишком большой. Максимум: ${props.maxSizeMB} МБ`)
    return false
  }
  return true
}

function openPicker() {
  inputRef.value?.click()
}

function handleChange(e) {
  const file = e.target.files?.[0]
  if (file && validateFile(file)) emit('update:modelValue', file)
  if (inputRef.value) inputRef.value.value = ''
}

function handleDrop(e) {
  dragging.value = false
  const file = e.dataTransfer.files?.[0]
  if (file && validateFile(file)) emit('update:modelValue', file)
}

function removeFile() {
  emit('update:modelValue', null)
  if (inputRef.value) inputRef.value.value = ''
}
</script>

<style scoped>
.ui-file-upload {
  border: 2px dashed var(--border-color);
  border-radius: var(--radius-md);
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.ui-file-upload:hover,
.ui-file-upload--dragging {
  border-color: var(--purple-primary);
  background: var(--purple-bg);
}

.ui-file-upload--has-file {
  border-style: solid;
  border-color: var(--purple-primary);
}

.ui-file-upload__input {
  display: none;
}

.ui-file-upload__placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.ui-file-upload__icon {
  font-size: 24px;
}

.ui-file-upload__text {
  font-size: 14px;
  color: var(--text-secondary);
}

.ui-file-upload__preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.ui-file-upload__name {
  font-size: 14px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ui-file-upload__remove {
  background: none;
  border: none;
  font-size: 16px;
  color: var(--text-muted);
  cursor: pointer;
  padding: 0;
}

.ui-file-upload__remove:hover {
  color: var(--red-accent);
}
</style>
