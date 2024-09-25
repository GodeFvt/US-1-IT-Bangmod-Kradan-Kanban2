import { defineStore, acceptHMRUpdate } from "pinia";
import VueJwtDecode from "vue-jwt-decode";

export const useUserStore = defineStore("userStore", {
  state: () => ({
    authToken: null,
    encodeToken: localStorage.getItem("authToken") || null,
    boards: [],
    visibilityPublic : false,
    isCanEdit : true,
    // isAuthenticated: false,
  }),

  actions: {
    initializeToken() {
      const token = localStorage.getItem("authToken");
      const refresh_Token = localStorage.getItem("refresh_token")
      if (token || refresh_Token) {
        try {
          const decodedToken = VueJwtDecode.decode(token);
          if(decodedToken === "{}"){
          this.authToken = null;
          this.encodeToken = null;
        }
          else{
           this.authToken = decodedToken;
            this.encodeToken = token;
         }
          // this.isAuthenticated = true;
        } catch (error) {
        }
      } else {
        this.clearAuthToken();
      }
    },

    setAuthToken(token) {
      const decodedToken = VueJwtDecode.decode(token);
      this.authToken = decodedToken
      this.encodeToken = token;
      localStorage.setItem("authToken", token);
      // this.isAuthenticated = true;
    },
    clearAuthToken() {
      this.authToken = null;
      this.encodeToken = null;
      // this.isAuthenticated = false;
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
    // updateIsAuthen(Boolean) {
    //   this.isAuthenticated = Boolean;
    // },
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
