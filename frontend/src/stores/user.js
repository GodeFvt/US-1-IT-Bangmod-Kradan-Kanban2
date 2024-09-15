import { defineStore, acceptHMRUpdate } from "pinia";
import VueJwtDecode from "vue-jwt-decode";
import { isTokenValid } from "../lib/utill.js";

export const useUserStore = defineStore("userStore", {
  state: () => ({
    authToken: () => {
      const token = localStorage.getItem("authToken") || null;
      if (token) {
        const decodeToken = VueJwtDecode.decode(token);
        return decodeToken;
      }
    },
    boards: [],
    isAuthenticated: !!localStorage.getItem("authToken"),
  }),

  actions: {
    initializeAuthToken() {
      const token = localStorage.getItem("authToken");
      if (token && isTokenValid(token)) {
        const decodeToken = VueJwtDecode.decode(token);
        this.authToken = decodeToken;
        this.isAuthenticated = true;
      } else {
        localStorage.removeItem("authToken");
        this.authToken = null;
        this.isAuthenticated = false;
      }
    },
    setAuthToken(token) {
      const decodeToken = VueJwtDecode.decode(token);
      this.authToken = { ...decodeToken };
      localStorage.setItem("authToken", token);
      this.isAuthenticated = true;
    },
    clearAuthToken() {
      this.authToken = null;
      this.isAuthenticated = false;
      localStorage.removeItem("authToken");
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
    updateIsAuthen(Boolean) {
      this.isAuthenticated = Boolean;
    },
  },
});
if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useUserStore, import.meta.hot));
}
