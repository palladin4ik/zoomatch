<template>
  <div class="not-found">
    <h1>404</h1>
    <p>Страница не найдена</p>
    <router-link to="/">На главную</router-link>
  </div>
</template>

<style scoped>
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  gap: 16px;
}

.not-found h1 {
  font-size: 72px;
  color: var(--purple-primary);
}

.not-found p {
  font-size: 18px;
  color: var(--text-secondary);
}
</style>
