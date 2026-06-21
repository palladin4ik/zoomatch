<script setup>
import { provide, ref } from 'vue'
import UiToast from './components/ui/UiToast.vue'

const toastRef = ref(null)

const toast = {
  success(msg) { toastRef.value?.success(msg) },
  error(msg) { toastRef.value?.error(msg) },
  info(msg) { toastRef.value?.info(msg) },
}
provide('toast', toast)
</script>

<template>
  <RouterView />
  <UiToast ref="toastRef" />
</template>
