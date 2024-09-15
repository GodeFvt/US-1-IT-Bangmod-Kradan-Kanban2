import { defineStore, acceptHMRUpdate } from "pinia";

export const useTaskStore = defineStore("taskStore", {
  state: () => ({
    allTask: [],
  }),
  actions: {
    addTask(task) {
      this.allTask.push(task);
    },
    editTask(index, task) {
      this.allTask[index] = { ...task };
    },
    deleteTask(index) {
      this.allTask.splice(index, 1);
    },
    setAllTask(newAllTask) {
      this.allTask = [...newAllTask];
    },
    setAllTaskUpdate(oldStatusId, newStatus) {
      this.allTask.forEach((e) =>
        e.status.id === oldStatusId ? (e.status = newStatus) : e.status
      );
    },
  },
});

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useTaskStore, import.meta.hot));
}
