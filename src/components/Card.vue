<script setup lang="ts">
import { ref } from 'vue';
import { storeToRefs } from 'pinia';
import { useParametersStore } from '@/stores/parameters.store';
import { useCalculatingStore } from '@/stores/calculating.store';
import { Status } from '@/enums/Status';
import InputField from '@/components/InputField.vue';
import Button from '@/components/Button.vue';
import Select from '@/components/Select.vue';
import App from '@/App.vue';

let file: string;

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

class Figure {
  private stepX: number;
  private stepY: number;
  private pathToFile: string;
  private layerType: number;

  constructor() {
    this.stepX = getParameter.value('widthX') * getParameter.value('layerHeight') / getParameter.value('widthZ');
    this.stepY = getParameter.value('widthY') * getParameter.value('layerHeight') / getParameter.value('widthZ');
    this.layerType = 0;
  }

  public checkHeader() {
    if (1) {
      this.addRoundBase();
    }
  }

  public addRoundBase() {
    const x = getParameter.value('widthX');
    const y = getParameter.value('widthY');
    const extruderSize = getParameter.value('extruderSize');


    const pointList = [];

    pointList.push(point.newCoord(-2 * extruderSize, -2 * extruderSize));
    pointList.push(point.newCoord(-2 * extruderSize, y + 2 * extruderSize));
    pointList.push(point.newCoord(x + 2 * extruderSize, y + 2 * extruderSize));
    pointList.push(point.newCoord(x + 2 * extruderSize, -2 * extruderSize));
    pointList.push(point.newCoord(-extruderSize, -2 * extruderSize));
    pointList.push(point.newCoord(-extruderSize, y + extruderSize));
    pointList.push(point.newCoord(x + extruderSize, y + extruderSize));
    pointList.push(point.newCoord(x + extruderSize, -extruderSize));
    pointList.push(point.newCoord(0, -extruderSize));

    const baseGLine: string = this.addGLine(pointList);

    file = baseGLine;
  }

  public makePyramid() {
    const layersCount = getParameter.value('widthZ') / getParameter.value('layerHeight');
    
    for (let z = 0; z < layersCount; ++z) {
      const layer = new Layer(z, this.stepX, this.stepY);

      // switch(this.layerType) {
      //   case 0:
      //     layer
      // }
      layer.calculate(true);
      layer.calculate(false);
    }
  }

  public addGLine(pointList): string {
    const z = 0;

    let str;

    pointList.forEach((pointItem) => {
      pointItem.translate(
        point.newCoord(-getParameter.value('widthX') / 2, -getParameter.value('widthY') / 2)
      );
    });

    str += `G1 X${pointList[0].getX()} Y${pointList[0].getY()} Z${0.2} F${gCodeUtils.getMovementSpeed(z)} A${gCodeUtils.getAAxisValue()}\n`;

    for (let i = 1; i < pointList.length; ++i) {
      const aAvalueMed = Math.pow(pointList[i - 1].getX() - pointList[i].getX(), 2) + Math.pow(pointList[i - 1].getY() - pointList[i].getY(), 2);
      const aValue = gCodeUtils.getAAxisValue() + Math.sqrt(aAvalueMed) * gCodeUtils.getMaterialCount();

      str += `G1 X${pointList[i].getX()} Y${pointList[i].getY()} Z${0.2} F${gCodeUtils.getMovementSpeed(z)} A${aValue}\n`;

      gCodeUtils.setAAxisValue(aValue);
    }

    return str;
  }
}

class Point {
  private x: number;
  private y: number;

  constructor(x: number, y: number) {
    this.x = x;
    this.y = y;
  }

  public newCoord(x: number, y: number) {
    return new Point(x, y);
  }

  public translate(x: number, y: number) {
    this.x += x;
    this.y += y;
  }

  public getX() {
    return this.x;
  }

  public getY() {
    return this.y;
  }
}

class Layer {
  private x: number;
  private y: number;
  private widthX: number;
  private widthY: number;
  private counterX: number;
  private counterY: number;
  private borderX: number;
  private borderY: number;
  private cellSizeX: number;
  private cellSizeY: number;
  private rotated: boolean;
  private reflected: boolean;
  private layerNumber: number;

  constructor(z: number, stepX: number, stepY: number) {
    this.layerNumber = z;
    this.x = z * stepX / 2;
    this.y = z * stepY / 2;
    this.widthX = getParameter.value('widthX') - z * stepX;
    this.widthY = getParameter.value('widthY') - z * stepY;
    this.cellSizeX = getParameter.value('defaultCellSize');
    this.cellSizeY = getParameter.value('defaultCellSize');
    this.counterX = this.widthX / this.cellSizeX;
    this.counterY = this.widthY / this.cellSizeY;
    this.borderX = (this.widthX - this.counterX * this.cellSizeX) / 2;
    this.borderY = (this.widthY - this.counterY * this.cellSizeY) / 2;
  }
  
  public calculate(axis: boolean) {
    const minimumCellSize = 2.8;

    let width;
    let count;
    let border;
    let cellSize;

    if (axis) {
      width = this.widthX;
      count = this.counterX;
      border = this.borderX;
      cellSize = this.cellSizeX;
    } else {
      width = this.widthY;
      count = this.counterY;
      border = this.borderY;
      cellSize = this.cellSizeY;
    }

    if (this.layerNumber % 2 == 1) {
      if (count % 2 == 1) {
        if (count == 1) {
          if (border < 1) {
            border = width;
          } else {
            count = 2;
            border += cellSize / 2;
          }
        } else if (count = 3 && border < 1 && border >= 0.2) {
          cellSize = minimumCellSize;
          count = 4;
          border = (width - count * minimumCellSize) / 2;

          if (border != 0) {
            border += minimumCellSize;
          }
        } else if (border < 1.8 && border >= 1) {
          ++count;
          border += 0.5 * cellSize;
        } else {
          ++count;
          border += 0.5 * cellSize;
        }
      } else if (count == 0) {
        count = 1;
        border = 3.6
      } else if (count == 2) {
        border = width / 2;
      } else if (border < 1.8 && border >= 1) {
        border += cellSize;
      } else {
        --count;
        border += 1.5 * cellSize;
      }
    } else if (count % 2 == 1) {
      if (count == 1) {
        if (border > 1) {
          count = 2;
          border += cellSize / 2;
        } else {
          border = width;
        }
      } else if (count > 2 && border != 0) {
        border += cellSize;
      }
    } else if (count == 0) {
      count = 1;
      border = 3.6;
    } else if (count == 2) {
      if (border != 0) {
        if (border < 1.8 && border > 1) {
          ++count;
          border += cellSize / 2;
        } else if (border >= 0.6) {
          cellSize = minimumCellSize;
          count = 3;
          border = (width - minimumCellSize) / 2;
        } else {
          border = width / 2;
        }
      }
    } else if (count > 3) {
      if (border != 0) {
        if (border < 1.8 && border > 1) {
          ++count;
          border += 0.5 * cellSize;
        } else {
          --count;
          border += 1.5 * cellSize;
        }
      } else {
        --count;
        border += 1.5 * cellSize;
      }
    }

    console.log(width, count, border, cellSize);
    

    if (axis) {
      this.counterX = count;
      this.borderX = border;
      this.cellSizeX = cellSize;
    } else {
      this.counterY = count;
      this.borderY = border;
      this.cellSizeY = cellSize;
    }
  }
}

const gCodeUtils = new GCodeUtils();
const point = new Point(0, 0);
const figure = new Figure();

figure.checkHeader();
figure.makePyramid();

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
