<template>
  <select class="select">
    <option value="Test">Pyramid</option>
  </select>
</template>
<style scoped>
.select {
  width: 100%;
  padding: var(--size-sm) var(--size-xl);
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--select-color);
  background-color: var(--select-bg-color);
  border: 2px solid var(--select-border-color);
  border-radius: var(--size-sm);
}

.select:hover {
  border: 2px solid var(--select-border-color-hover);
}

.select:focus {
  border: 2px solid var(--select-border-color-focus);
}
</style>
