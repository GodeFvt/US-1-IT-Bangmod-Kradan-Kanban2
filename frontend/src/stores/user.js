import { defineStore, acceptHMRUpdate } from "pinia";

export const useUserStore = defineStore("userStore", {
  state: () => ({
authToken: JSON.parse(localStorage.getItem('authToken')) || null,

  }),
  actions: {
    setAuthToken(token) {
        console.log(token)
        this.authToken = {...token};
        localStorage.setItem('authToken', JSON.stringify(token));
      },
    clearAuthToken() {
        this.authToken = null;
        localStorage.removeItem('authToken');
      },
  }
})
if (import.meta.hot) {
    import.meta.hot.accept(acceptHMRUpdate(useUserStore, import.meta.hot));
  }