<script setup lang="ts">
interface ButtonProps {
  label: string;
}

defineProps<ButtonProps>();
</script>

<template>
  <button class="button">
    {{ label }}
  </button>
</template>

<style scoped>
.button {
  width: 100%;
  padding: var(--size-sm) var(--size-xl);
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--button-color);
  background-color: var(--button-bg-color);
  border-radius: var(--size-sm);
  transition: background-color .2s ease;
}

.button:hover {
  background-color: var(--button-bg-color-hover);
}

.button:active {
  background-color: var(--button-bg-color-active);
}

.button:disabled {
  color: var(--button-color-disabled);
  background: var(--button-bg-color-disabled);
  cursor: not-allowed;
}
</style>