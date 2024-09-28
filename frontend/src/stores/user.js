import { defineStore, acceptHMRUpdate } from "pinia";
import VueJwtDecode from "vue-jwt-decode";

export const useUserStore = defineStore("userStore", {
  state: () => ({
    authToken: null,
    encodeToken: localStorage.getItem("authToken") || null,
    boards: [],
    visibilityPublic: false, //true คือ public
    isCanEdit: true,
    currentBoard: {},
  }),

  actions: {
    initializeToken() {
      const token = localStorage.getItem("authToken");
      const refresh_Token = localStorage.getItem("refresh_token");
      if (token || refresh_Token) {
        try {
          const decodedToken = VueJwtDecode.decode(token);
          if (
            decodedToken === "{}" ||
            decodedToken === null ||
            decodedToken === undefined
          ) {
            this.clearAuthToken();
          } else {
            this.authToken = decodedToken;
            this.encodeToken = token;
          }
        } catch (error) {}
      } else {
        this.clearAuthToken();
      }
    },

    setAuthToken(token) {
      if (token) {
        try {
          const decodedToken = VueJwtDecode.decode(token);
          if (
            decodedToken === "{}" ||
            decodedToken === null ||
            decodedToken === undefined
          ) {
            this.clearAuthToken();
          } else {
            this.authToken = decodedToken;
            this.encodeToken = token;
          }
          localStorage.setItem("authToken", token);
        } catch (error) {}
      } else {
        this.clearAuthToken();
      }
    },
    clearAuthToken() {
      this.authToken = null;
      this.encodeToken = null;
      localStorage.removeItem("authToken");
      localStorage.removeItem("refresh_token");
    },
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
  },
});
if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useUserStore, import.meta.hot));
}
