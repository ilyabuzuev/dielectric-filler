import { defineStore } from 'pinia';
import { Status } from '@/enums/Status';

export const useCalculatingStore = defineStore('calculatingStore', {
  state: () => ({
    _status: Status.IDLE,
  }),

  getters: {
    status: (state): Status => {
      return state._status;
    }
  },

  actions: {
    set(status: Status) {
      this._status = status;
    }
  }
});
