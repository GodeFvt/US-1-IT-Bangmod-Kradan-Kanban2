import { msalInstance, state, loginRequest } from "./msalConfig";
import { useUserStore } from "../stores/user.js";

export function msalService() {
  const initialize = async () => {
    try {
      await msalInstance.initialize();
    } catch (error) {
      console.log("Initialization error", error);
    }
  };

  const login = async () => {
    try {
      if (!msalInstance) {
        throw new Error(
          "MSAL not initialized. Call initializeMsal() before using MSAL API."
        );
      }
      await msalInstance.loginRedirect(loginRequest);
      state.isAuthenticated = true;
    } catch (error) {
      console.error("Login error:", error);
    }
  };

  const logout = () => {
    if (!msalInstance) {
      throw new Error(
        "MSAL not initialized. Call initializeMsal() before using MSAL API."
      );
    }
    msalInstance.logoutRedirect(loginRequest);
   state.isAuthenticated = false;
    state.user = null;
  };

  const handleRedirect = async (router) => {
    const userStore = useUserStore();
    try {
      const res = await msalInstance.handleRedirectPromise();
      state.isAuthenticated = msalInstance.getAllAccounts().length > 0;
      state.user = msalInstance.getAllAccounts()[0];
      if (res) {
        console.log(res);
        localStorage.setItem("authToken", res.accessToken);
        userStore.setAuthToken(res.accessToken);
        router.push({ name: "board" });
      }
      if(!state.isAuthenticated){
        router.push({ name: "Login" });
      }
    } catch (error) {
      console.error("Redirect error:", error);
    }
  };

  const getToken = async () => {
    if (!msalInstance) {
      throw new Error(
        "MSAL not initialized. Call initializeMsal() before using MSAL API."
      );
    }
    try {
      const accounts = msalInstance.getAllAccounts();
      if (accounts.length === 0) {
        throw new Error("No accounts found. Please login first.");
      }
      const silentRequest = {
        scopes: loginRequest.scopes,
        
        account: accounts[0],
      };
      const silentResponse = await msalInstance.acquireTokenSilent(
        silentRequest
      );
      return silentResponse.accessToken;
    } catch (error) {
      console.error("Silent token acquisition error:", error);
    }
  };

  return {
    initialize,
    login,
    logout,
    handleRedirect,
    getToken,
  };
}
