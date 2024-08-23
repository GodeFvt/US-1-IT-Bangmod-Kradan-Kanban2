import { defineStore, acceptHMRUpdate } from "pinia";

export const useUserStore = defineStore("userStore", {
  state: () => ({
    authToken: (() => {
      const token = JSON.parse(localStorage.getItem("authToken")) || null;
      const currentTime = Math.floor(Date.now() / 1000); //แปลงจาก milisec to sec
      if (token && token.exp < currentTime) {
        localStorage.removeItem("authToken");
        return null;
      }
      return token;
    })(),
  }),
  actions: {
    setAuthToken(token) {
      console.log(token);
      this.authToken = { ...token };
      localStorage.setItem("authToken", JSON.stringify(token));
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
