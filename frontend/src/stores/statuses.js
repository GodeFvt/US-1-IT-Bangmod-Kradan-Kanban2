import { defineStore, acceptHMRUpdate } from "pinia";

export const useStatusStore = defineStore("statusStore", {
  state: () => ({
    allStatus: [],
    isLimit: false,
    maximumTask: undefined,
    noOftask: {},
  }),
  actions: {
    addStatus(status) {
      this.allStatus.push(status);
    },
    editStatus(index, status) {
      this.allStatus[index] = { ...status };
    },
    deleteStatus(index) {
      this.allStatus.splice(index, 1);
    },
    setAllStatus(newAllStatus) {
      this.allStatus = [...newAllStatus];
    },
    setLimitStatus(isLimit) {
      this.isLimit = isLimit;
    },
    setMaximumTaskStatus(maximumTask) {
      this.maximumTask = maximumTask;
    },
    setNoOftask(noOftask) {
      this.noOftask = noOftask;
    },
    getColorStatus(statusName) {
      const statusObj = this.allStatus.find((s) => s.name === statusName);
      if (statusObj === null || statusObj === undefined) {
        return "#828282";
      } else {
        return statusObj.color !== null ? statusObj.color : "#828282";
      }
    },
  },
});

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useStatusStore, import.meta.hot));
}
