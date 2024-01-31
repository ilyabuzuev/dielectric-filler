import { defineStore } from 'pinia';

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

interface State {
  _parameters: {
    widthX: number | string;
    widthY: number | string;
    widthZ: number | string;
    defaultCellSize: number | string;
    layerHeight: number | string;
    epsilonZero: number | string;
    epsilonMinimum: number | string;
    epsilonRate: number | string;
    extruderSize: number | string;
    extruderTemp: number | string;
    tableTemp: number | string;
    fwd: number | string;
    bck: number | string;
    [key: string]: number | string;
  };
}

export const useParametersStore = defineStore('parametersStore', {
  state: (): State => ({
    _parameters: {
      widthX: 36,
      widthY: 28,
      widthZ: 36,
      defaultCellSize: 3.6,
      layerHeight: 0.2,
      epsilonZero: 2.4,
      epsilonMinimum: 1.3,
      epsilonRate: 1.0,
      extruderSize: 0.4,
      extruderTemp: 220,
      tableTemp: 130,
      fwd: 0,
      bck: 0,
    },
  }),

  getters: {
    getAllParameters(): State['_parameters'] {
      return this._parameters;
    },

    getParameter(state) {
      return (parameter: parameterTypes) => +state._parameters[parameter];
    },
  },

  actions: {
    setParameter(parameter: parameterTypes, value: number | string) {
      this._parameters[parameter] = value;
    },
  },
});
