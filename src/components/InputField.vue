<script setup lang="ts">
import { useParametersStore } from '@/stores/parameters.store';

type parameterTypes =
  | 'widthX'
  | 'widthY'
  | 'widthZ'
  | 'defaultCellSize'
  | 'layerHeight'
  | 'epsilonZero'
  | 'epsilonMinimum'
  | 'epsilonRate'
  | 'extruderSize'
  | 'extruderTemp'
  | 'tableTemp'
  | 'fwd'
  | 'bck';

interface InputFieldProps {
  id: number;
  label: string;
  min?: number | string;
  max?: number | string;
  step?: number | string;
  modelValue: number | string;
  modelValueName: parameterTypes;
}

const props = withDefaults(defineProps<InputFieldProps>(), {
  min: 0,
  max: 127,
  step: 1,
});

const emit = defineEmits(['update:modelValue']);

const parametersStore = useParametersStore();

function updateValue(evt: Event) {
  const target = evt.target as HTMLInputElement;

  parametersStore.setParameter(props.modelValueName, target.value);

  emit('update:modelValue', target.value);
}
</script>

<template>
  <div class="input-field">
    <label class="input-field__box">
      <div class="input-field__label">
        {{ label }}
      </div>
      <input
        class="input-field__input"
        @input="updateValue($event)"
        :value="modelValue"
        :min="min"
        :max="max"
        :step="step"
        type="number"
      />
    </label>
  </div>
</template>

<style scoped>
.input-field__label,
.input-field__input {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--text-color-soft);
}

.input-field__box {
  max-width: 340px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--size-sm);
}

.input-field__input {
  max-width: 64px;
  padding: var(--size-sm);
  background-color: var(--bg-color-mute);
  outline: 2px solid var(--input-border-color);
  border-radius: var(--size-sm);
  transition: border-color 0.3 ease;
}

.input-field__input:hover {
  outline: 2px solid var(--input-border-color-hover);
}

.input-field__input:focus {
  outline: 2px solid var(--input-border-color-focus);
}
</style>
