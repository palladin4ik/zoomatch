<template>
  <div
    :class="['message-bubble', { 'message-bubble--outgoing': isOutgoing }]"
    @contextmenu.prevent="onContextMenu"
    @touchstart.passive="onTouchStart"
    @touchend="onTouchEnd"
    @touchmove.passive="onTouchMove"
  >
    <div v-if="message.deleted_by_sender || message.deleted_by_receiver" class="message-bubble__deleted">
      Сообщение удалено
    </div>
    <template v-else>
      <div v-if="message.media && isImage" class="message-bubble__media">
        <img :src="mediaUrl" :alt="'Изображение'" class="message-bubble__image" @load="imageLoaded = true" />
        <div v-if="!imageLoaded" class="message-bubble__image-placeholder">
          <div class="message-bubble__spinner"></div>
        </div>
      </div>
      <div v-else-if="message.media" class="message-bubble__file" @click="$emit('download', message)">
        <span class="message-bubble__file-icon"><UiIcon name="file" /></span>
        <span class="message-bubble__file-name">{{ fileName }}</span>
      </div>
      <p v-if="message.text && !message.media" class="message-bubble__text">{{ message.text }}</p>
    </template>
    <div class="message-bubble__meta">
      <span v-if="message.is_edited" class="message-bubble__edited">ред.</span>
      <span class="message-bubble__time">{{ timeFormatted }}</span>
      <span v-if="isOutgoing" class="message-bubble__status">
        {{ message.is_read ? '&#10003;&#10003;' : message.is_delivered ? '&#10003;&#10003;' : '&#10003;' }}
      </span>
    </div>

    <Teleport to="body">
      <Transition name="ctx-menu">
        <div
          v-if="showMenu"
          class="message-ctx-menu"
          :style="menuStyle"
          @click.stop
        >
          <button
            v-if="isOutgoing && !message.deleted_by_sender && !message.deleted_by_receiver"
            class="message-ctx-menu__item"
            @click="handleEdit"
          >
            Редактировать
          </button>
          <button
            class="message-ctx-menu__item message-ctx-menu__item--danger"
            @click="handleDelete"
          >
            Удалить
          </button>
        </div>
      </Transition>
    </Teleport>
    <div v-if="showMenu" class="message-ctx-menu-overlay" @click="closeMenu" @contextmenu.prevent="closeMenu" />
  </div>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue'
import UiIcon from '../ui/UiIcon.vue'

const props = defineProps({
  message: { type: Object, required: true },
  isOutgoing: { type: Boolean, default: false },
})

const emit = defineEmits(['download', 'edit', 'delete'])

const imageLoaded = ref(false)
const showMenu = ref(false)
const menuPos = ref({ x: 0, y: 0 })

let longPressTimer = null
const LONG_PRESS_DELAY = 500

const mediaUrl = computed(() => {
  if (!props.message.media) return ''
  if (props.message.media.startsWith('http')) return props.message.media
  return `${import.meta.env.VITE_API_URL}${props.message.media}`
})

const isImage = computed(() => {
  const url = props.message.media || ''
  return /\.(jpg|jpeg|png|gif|webp)$/i.test(url)
})

const fileName = computed(() => {
  if (!props.message.media) return 'Файл'
  if (props.message.text) return props.message.text
  const parts = props.message.media.split('/')
  return parts[parts.length - 1] || 'Файл'
})

const timeFormatted = computed(() => {
  const date = props.message.created_at
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })
})

const menuStyle = computed(() => ({
  left: `${menuPos.value.x}px`,
  top: `${menuPos.value.y}px`,
}))

function onContextMenu(e) {
  openMenu(e.clientX, e.clientY)
}

function onTouchStart(e) {
  longPressTimer = setTimeout(() => {
    const touch = e.touches[0]
    openMenu(touch.clientX, touch.clientY)
  }, LONG_PRESS_DELAY)
}

function onTouchEnd() {
  clearTimeout(longPressTimer)
}

function onTouchMove() {
  clearTimeout(longPressTimer)
}

function openMenu(x, y) {
  const menuW = 180
  const menuH = 80
  const vw = window.innerWidth
  const vh = window.innerHeight

  if (x + menuW > vw) x = vw - menuW - 8
  if (y + menuH > vh) y = vh - menuH - 8

  menuPos.value = { x, y }
  showMenu.value = true
}

function closeMenu() {
  showMenu.value = false
}

function handleEdit() {
  closeMenu()
  emit('edit', props.message)
}

function handleDelete() {
  closeMenu()
  emit('delete', props.message)
}

onUnmounted(() => {
  clearTimeout(longPressTimer)
})
</script>

<style scoped>
.message-bubble {
  width: fit-content;
  max-width: 60%;
  padding: 10px 14px;
  border-radius: 16px 16px 16px 4px;
  background: var(--bg-white);
  color: var(--purple-dark);
  border: 1px solid var(--border-color);
  margin-bottom: 4px;
  position: relative;
  user-select: none;
  -webkit-user-select: none;
}

.message-bubble--outgoing {
  margin-left: auto;
  background: var(--purple-primary);
  color: var(--text-white);
  border-color: var(--purple-primary);
  border-radius: 16px 16px 4px 16px;
}

.message-bubble__deleted {
  font-style: italic;
  opacity: 0.6;
  font-size: 13px;
}

.message-bubble__text {
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.message-bubble__media {
  margin-bottom: 4px;
}

.message-bubble__image {
  max-width: 260px;
  border-radius: 12px;
  display: block;
}

.message-bubble__image-placeholder {
  width: 200px;
  height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-hover);
  border-radius: 12px;
}

.message-bubble__file {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--bg-hover);
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 4px;
  max-width: 240px;
}

.message-bubble--outgoing .message-bubble__file {
  background: rgba(255, 255, 255, 0.2);
}

.message-bubble__file-icon {
  font-size: 20px;
}

.message-bubble__file-name {
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-bubble__meta {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  margin-top: 4px;
}

.message-bubble__time {
  font-size: 11px;
  opacity: 0.7;
}

.message-bubble__status {
  font-size: 12px;
}

.message-bubble__edited {
  font-size: 11px;
  opacity: 0.7;
}

.message-ctx-menu-overlay {
  position: fixed;
  inset: 0;
  z-index: 999;
}

.message-ctx-menu {
  position: fixed;
  z-index: 1000;
  background: var(--bg-white);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  min-width: 160px;
  padding: 4px 0;
}

.message-ctx-menu__item {
  display: block;
  width: 100%;
  padding: 10px 16px;
  background: none;
  border: none;
  text-align: left;
  font-size: 14px;
  color: var(--text-primary);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.message-ctx-menu__item:hover {
  background: var(--bg-hover);
}

.message-ctx-menu__item--danger {
  color: var(--red-accent);
}

.message-ctx-menu__item--danger:hover {
  background: #fef2f2;
}

.ctx-menu-enter-active,
.ctx-menu-leave-active {
  transition: opacity 0.12s ease, transform 0.12s ease;
}

.ctx-menu-enter-from,
.ctx-menu-leave-to {
  opacity: 0;
  transform: scale(0.95);
}
</style>
