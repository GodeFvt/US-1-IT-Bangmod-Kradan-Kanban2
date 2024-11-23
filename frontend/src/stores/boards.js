import { defineStore, acceptHMRUpdate } from "pinia";

export const useBoardStore = defineStore("useBoard", {
  state: () => ({
    boards: [],
    visibilityPublic: false, //true คือ public
    isCanEdit: true,
    currentBoard: {},
    collabBoard: [],
    collabs: [],
  }),

  actions: {
    addBoard(board) {
      this.boards.push(board);
    },
    editBoard(index, board) {
      this.boards[index] = { ...board };
    },
    deleteBoard(index) {
      this.boards.splice(index, 1);
    },
    setAllBoard(newAllBoard) {
      this.boards = [
        ...newAllBoard.boards,
        ...newAllBoard.collab,
        ...newAllBoard.invited,
      ];
    },
    findBoardById(id) {
      return this.boards.find((board) => board.id === id);
    },
    updatevIsibilityPublic(Boolean) {
      this.visibilityPublic = Boolean;
    },
    updatevIsCanEdit(Boolean) {
      this.isCanEdit = Boolean;
    },
    setCurrentBoard(board) {
      this.currentBoard = { ...board };
    },
    setIsVisibilityCurrentBoard(visibility) {
      this.currentBoard.visibility = visibility;
    },
    setCollabs(collab) {
      this.collabs = collab;
    },
    updateCollabs(index, collab) {
      this.collabs[index] = collab;
    },
    addCollab(collab) {
      this.collabs.push(collab);
    },
    removeCollab(index) {
      this.collabs.splice(index, 1);
    },
    updateAccessCollab(index, access) {
      this.collabs[index].access = access;
    },
  },
});
if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useBoardStore, import.meta.hot));
}
