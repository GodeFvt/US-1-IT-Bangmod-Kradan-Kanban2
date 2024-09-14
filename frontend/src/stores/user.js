import { defineStore, acceptHMRUpdate } from "pinia";
import VueJwtDecode from "vue-jwt-decode";
import { isTokenValid } from "../lib/utill.js";

export const useUserStore = defineStore("userStore", {
  state: () => ({
    authToken: (() => {
      const token = localStorage.getItem("authToken") || null;
      if (isTokenValid(token)) {
        const decodeToken = VueJwtDecode.decode(token);
        return decodeToken;
      } else {
        localStorage.removeItem("authToken");
        return null;
      }
    })(),
    boards: [],
    isAuthenticated : !!localStorage.getItem('authToken'),
  }),
  
  actions: {
    setAuthToken(token) {
      const decodeToken = VueJwtDecode.decode(token)
      this.authToken = { ...decodeToken };
      localStorage.setItem("authToken", token);
    },
    clearAuthToken() {
      this.authToken = null;
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
