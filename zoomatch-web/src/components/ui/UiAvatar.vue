<template>
  <div :class="['ui-avatar', `ui-avatar--${size}`]">
    <img v-if="src" :src="src" :alt="alt" class="ui-avatar__img" @error="imgError = true" />
    <div v-if="!src || imgError" class="ui-avatar__fallback">
      {{ initials }}
    </div>
    <span v-if="badge" class="ui-avatar__badge">{{ badge }}</span>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  src: { type: String, default: '' },
  alt: { type: String, default: '' },
  name: { type: String, default: '' },
  size: { type: String, default: 'md' },
  badge: { type: [String, Number], default: '' },
})

const imgError = ref(false)

const initials = computed(() => {
  if (!props.name) return '?'
  return props.name
    .split(' ')
    .map(w => w[0])
    .join('')
    .toUpperCase()
    .slice(0, 2)
})
</script>

<style scoped>
.ui-avatar {
  position: relative;
  border-radius: 50%;
  flex-shrink: 0;
}

.ui-avatar--sm { width: 32px; height: 32px; }
.ui-avatar--md { width: 40px; height: 40px; }
.ui-avatar--lg { width: 56px; height: 56px; }
.ui-avatar--xl { width: 80px; height: 80px; }

.ui-avatar__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.ui-avatar__fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--purple-bg);
  color: var(--purple-primary);
  font-weight: 600;
  border-radius: 50%;
}

.ui-avatar--sm .ui-avatar__fallback { font-size: 12px; }
.ui-avatar--md .ui-avatar__fallback { font-size: 14px; }
.ui-avatar--lg .ui-avatar__fallback { font-size: 18px; }
.ui-avatar--xl .ui-avatar__fallback { font-size: 24px; }

.ui-avatar__badge {
  position: absolute;
  top: -2px;
  right: -2px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: var(--red-accent);
  color: var(--text-white);
  font-size: 10px;
  font-weight: 600;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
