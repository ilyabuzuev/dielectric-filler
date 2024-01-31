<script setup lang="ts">
import { ref } from 'vue';
import { storeToRefs } from 'pinia';
import { useParametersStore } from '@/stores/parameters.store';
import { useCalculatingStore } from '@/stores/calculating.store';
import { Status } from '@/enums/Status';
import InputField from '@/components/InputField.vue';
import Button from '@/components/Button.vue';
import Select from '@/components/Select.vue';

const parametersStore = useParametersStore();
const calculatingStore = useCalculatingStore();

const { getParameter } = storeToRefs(parametersStore);

const widthX = ref(getParameter.value('widthX'));
const widthY = ref(getParameter.value('widthY'));
const widthZ = ref(getParameter.value('widthZ'));
const defaultCellSize = ref(getParameter.value('defaultCellSize'));
const layerHeight = ref(getParameter.value('layerHeight'));
const fwd = ref(getParameter.value('fwd'));
const epsilonZero = ref(getParameter.value('epsilonZero'));
const epsilonMinimum = ref(getParameter.value('epsilonMinimum'));
const extruderSize = ref(getParameter.value('extruderSize'));
const extruderTemp = ref(getParameter.value('extruderTemp'));
const tableTemp = ref(getParameter.value('tableTemp'));
const bck = ref(getParameter.value('bck'));

class GCodeUtils {
  private fastMovementSpeed: number;
  private slowMovementSpeed: number;
  private materialCount: number;
  private aAxis: number;

  constructor() {
    this.fastMovementSpeed = 1500;
    this.slowMovementSpeed = 4600;
    this.materialCount = 0.0328;
    this.aAxis = 0.01;
  }

  /**
   * @param z - текущий слой
   * @description Возвращает скорость передвижения: если слой больше 2, 
   * то возвращаемая скорость передвижения будет равна 1500, иначе 4600
   */
  public getMovementSpeed(z: number) {
    return z > 2 ? this.fastMovementSpeed : this.slowMovementSpeed;
  }

  public getMaterialCount() {
    return this.materialCount;
  }

  public getAAxisValue() {
    return this.aAxis;
  }

  public setAAxisValue(value: number) {
    this.aAxis += value;
  }

  public setDefaultAAxisValue() {
    this.aAxis = 0.01;
  }
}

const gCodeUtils = new GCodeUtils();



/*---------------------------------------------------*/
function getParameters() {
  calcEpsilonRate();

  console.log(parametersStore.getAllParameters);
}

function calcEpsilonRate() {
  const w = Math.pow(getParameter.value('widthX') / 2, 2) + Math.pow(getParameter.value('widthY') / 2, 2);
  const height_sq = Math.pow(getParameter.value('widthZ'), 2);
  const cos2 = height_sq / (height_sq + w);

  parametersStore.setParameter('epsilonRate', getParameter.value('epsilonMinimum') / cos2 / getParameter.value('epsilonZero'));

  return getParameter.value('epsilonMinimum') / cos2 / getParameter.value('epsilonZero');
}

function calculate() {
  if (calcEpsilonRate() > 1) {
    calculatingStore.set(Status.REJECTED);
  } else {
    calculatingStore.set(Status.CALCULATING);

    gCodeUtils.setDefaultAAxisValue();

    setTimeout(() => {
      calculatingStore.set(Status.RESOLVED);
    }, 3000)
  }

}
</script>

<template>
  <div class="card">
    <div class="card__body">
      <div class="card__section">
        <InputField
          v-model="widthX"
          label="Width X"
          modelValueName="widthX"
          :id="1"
        />
        <InputField
          v-model="widthY"
          label="Width Y"
          modelValueName="widthY"
          :id="2"
        />
        <InputField
          v-model="widthZ"
          label="Height"
          modelValueName="widthZ"
          :id="3"
        />
        <InputField
          v-model="defaultCellSize"
          label="Default cell size"
          modelValueName="defaultCellSize"
          step="0.1"
          :id="4"
        />
        <InputField
          v-model="layerHeight"
          label="Layer height"
          modelValueName="layerHeight"
          step="0.1"
          :id="5"
        />
        <InputField
          v-model="fwd"
          label="Forward extr."
          modelValueName="fwd"
          :id="6"
        />
      </div>
      <div class="card__section">
        <InputField
          v-model="epsilonZero"
          label="Epsilon 0"
          modelValueName="epsilonZero"
          step="0.1"
          :id="7"
        />
        <InputField
          v-model="epsilonMinimum"
          label="Epsilon Minimum"
          modelValueName="epsilonMinimum"
          step="0.1"
          :id="8"
        />
        <InputField
          v-model="extruderSize"
          label="Extruder Size"
          modelValueName="extruderSize"
          step="0.1"
          :id="9"
        />
        <InputField
          v-model="extruderTemp"
          label="Extruder Temperature"
          modelValueName="extruderTemp"
          max="250"
          :id="10"
        />
        <InputField
          v-model="tableTemp"
          label="Table Temperature"
          modelValueName="tableTemp"
          max="140"
          :id="11"
        />
        <InputField
          v-model="bck"
          label="Backward extr."
          modelValueName="bck"
          :id="12"
        />
      </div>
    </div>
    <div class="card__actions">
      <Select />
      <Button @click="calculate" label="Calculate" :disabled="calculatingStore.status === Status.CALCULATING" />
    </div>
    <div>
      <div v-if="calculatingStore.status === Status.IDLE">
        Версия: 0.1
      </div>
      <div v-else-if="calculatingStore.status === Status.CALCULATING">
        Calculating...
      </div>
      <div v-else-if="calculatingStore.status === Status.RESOLVED">
        Done!
      </div>
      <div v-else-if="calculatingStore.status === Status.REJECTED">
        Error. Epsilon rate over 1.0. Choose another material (Epsilon 0).
      </div>
    </div>
  </div>
</template>

<style scoped>
.card {
  display: flex;
  flex-direction: column;
  gap: var(--size-xl);
  padding: var(--size-md);
  background-color: var(--bg-color-mute);
  border-radius: var(--size-sm);
  box-shadow: var(--shadow-sm);
}

.card__body {
  display: flex;
  gap: var(--size-xl);
}

.card__section {
  display: flex;
  flex-direction: column;
  gap: var(--size-md);
}

.card__actions {
  display: flex;
  justify-content: stretch;
  gap: var(--size-xl);
}
</style>
