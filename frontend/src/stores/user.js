import { defineStore, acceptHMRUpdate } from "pinia";
import VueJwtDecode from "vue-jwt-decode";

export const useUserStore = defineStore("userStore", {
  state: () => ({
    authToken: null,
    encodeToken: localStorage.getItem("authToken") || null,
    isMicroSoftLogin: "Guest",
    theme: localStorage.getItem("theme") || "table", 
  }),

  actions: {
    initializeToken() {
      console.log("initial");
      const token = localStorage?.getItem("authToken");
      const refresh_Token = localStorage?.getItem("refresh_token");
      const keyMicrosoft = Object.keys(localStorage).some((key) =>
        key.includes("login.windows.net")
      );

      if (token || refresh_Token || keyMicrosoft) {
        try {
          let decodedToken = null;
          if (token === null) {
            if (refresh_Token || keyMicrosoft) {
              this.authToken = decodedToken;
              this.encodeToken = token;
            } else {
              this.clearAuthToken();
            }
          } else {
            decodedToken = VueJwtDecode.decode(token);
            if (
              (decodedToken === "{}" ||
                decodedToken === null ||
                decodedToken === undefined) &&
              (!refresh_Token || !keyMicrosoft)
            ) {
              this.clearAuthToken();
            } else {
              this.authToken = decodedToken;
              this.encodeToken = token;
            }
          }

          if (
            this?.authToken?.iss.includes("login.microsoftonline.com") ||
            keyMicrosoft
          ) {
            this.isMicroSoftLogin = "MS";
          } else {
            this.isMicroSoftLogin = "Guest";
          }
        } catch (error) {
          console.log("error", error);
        }
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
            console.log("clearStorage");
            this.clearAuthToken();
          } else {
            this.authToken = decodedToken;
            this.encodeToken = token;
          }
          localStorage.setItem("authToken", token);
        } catch (error) {}
      } else {
        console.log("clearStorage 2");
        this.clearAuthToken();
      }
    },
    clearAuthToken() {
      this.authToken = null;
      this.encodeToken = null;
      this.isMicroSoftLogin = 'Guest';
      localStorage.clear();
    },

    updateIsMicrosoftLogin(string) {
      this.isMicroSoftLogin = string;
    },

    setTheme(newTheme) {
      this.theme = newTheme;
    },

  },
});
if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useUserStore, import.meta.hot));
}
