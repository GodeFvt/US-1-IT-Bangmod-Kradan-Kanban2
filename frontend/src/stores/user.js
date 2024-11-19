import { defineStore, acceptHMRUpdate } from "pinia";
import VueJwtDecode from "vue-jwt-decode";

export const useUserStore = defineStore("userStore", {
  state: () => ({
    authToken: null,
    encodeToken: localStorage.getItem("authToken") || null,
    isMicroSoftLogin: false,
  }),

  actions: {
    initializeToken() {
      const token = localStorage?.getItem("authToken");
      const refresh_Token = localStorage?.getItem("refresh_token");
      if (token || refresh_Token) {
        try {
          const decodedToken = VueJwtDecode.decode(token);
          if (
            (decodedToken === "{}" ||
            decodedToken === null ||
            decodedToken === undefined)&& !refresh_Token
          ) {
            this.clearAuthToken();
          } else {
            this.authToken = decodedToken;
            this.encodeToken = token;
            if(this.authToken?.iss.includes("sts.windows.net")){
              this.isMicroSoftLogin = true;
            }
            else{
              this.isMicroSoftLogin = false;
            }
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
      localStorage.clear();
      sessionStorage.clear();
    },
  },
});
if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useUserStore, import.meta.hot));
}
