import { defineStore, acceptHMRUpdate } from "pinia";

export const useBoardStore = defineStore("useBoard", {
    state: () => ({
      boards: [],
      visibilityPublic: false, //true คือ public
      isCanEdit: true,
      currentBoard: {},
      collabBoard:[],
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
        this.boards = [...newAllBoard];
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
        this.currentBoard.visibility = visibility
      } ,
    },
  });
  if (import.meta.hot) {
    import.meta.hot.accept(acceptHMRUpdate(useBoardStore, import.meta.hot));
  }