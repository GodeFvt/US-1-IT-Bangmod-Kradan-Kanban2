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
  },
});
if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useUserStore, import.meta.hot));
}
