import { defineStore, acceptHMRUpdate } from "pinia";
import VueJwtDecode from "vue-jwt-decode";

export const useUserStore = defineStore("userStore", {
  state: () => ({
    authToken: (() => {
      const token = localStorage.getItem("authToken") || null;
      if (token) {
        const decodeToken = VueJwtDecode.decode(token);
        const currentTime = Math.floor(Date.now() / 1000); //แปลงจาก milisec to sec
        if (decodeToken.exp < currentTime) {
          localStorage.removeItem("authToken");
          return null;
        }
        return decodeToken;
      }
    })(),
    boards: [],
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
    editTask(index, board) {
      this.boards[index] = { ...board };
    },
    deleteTask(index) {
      this.boards.splice(index, 1);
    },
    setAllBoard(newAllBoard) {
      this.boards = [...newAllBoard];
    },
  },
});
if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useUserStore, import.meta.hot));
}
