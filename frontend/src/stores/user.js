import { defineStore, acceptHMRUpdate } from "pinia";
import VueJwtDecode from "vue-jwt-decode";

export const useUserStore = defineStore("userStore", {
  state: () => ({
    authToken: null,
    encodeToken: localStorage.getItem("authToken") || null,  
    isAuthenticated: false,
    boards: [],
    visibilityPublic : false, // false คือ private
    isCanEdit : true,
  }),

  actions: {
    initializeToken() {
      const token = localStorage.getItem("authToken");
      if (token) {
        try {
          const decodedToken = VueJwtDecode.decode(token);
          this.authToken = decodedToken;
          this.encodeToken = token;
          this.isAuthenticated = true;

        } catch (error) {
          console.error("Error decoding token:", error);
          this.clearAuthToken();
        }
      } else {
        this.clearAuthToken();
      }
    },

    setAuthToken(token) {
      const decodedToken = VueJwtDecode.decode(token);
      this.authToken = { ...decodedToken };
      this.encodeToken = token;
      localStorage.setItem("authToken", token);
      this.isAuthenticated = true;
    },
    clearAuthToken() {
      this.authToken = null;
      this.isAuthenticated = false;
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
    updateIsAuthen(Boolean) {
      this.isAuthenticated = Boolean;
    },
    updatevIsibilityPublic(Boolean) {
      this.visibilityPublic = Boolean;
    },
    updatevIsCanEdit(Boolean) {
      this.isCanEdit = Boolean;
    },
  },
});
if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useUserStore, import.meta.hot));
}
