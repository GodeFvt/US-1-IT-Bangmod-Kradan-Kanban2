/**
 * Configuration object to be passed to MSAL instance on creation.
 * For a full list of MSAL.js configuration parameters, visit:
 * https://github.com/AzureAD/microsoft-authentication-library-for-js/blob/dev/lib/msal-browser/docs/configuration.md
 */
import { PublicClientApplication } from "@azure/msal-browser";
import { reactive } from "vue";

const CLIENT_ID = import.meta.env.VITE_CLIENT_ID;
const AUTHORITY = import.meta.env.VITE_AUTHORITY;
const REDIRECT_URI = import.meta.env.VITE_REDIRECT_URI;

const msalConfig = {
  auth: {
    clientId: CLIENT_ID,
    authority: AUTHORITY,
    redirectUri: REDIRECT_URI, // Replace with your actual redirect URI
    postLogoutUri: REDIRECT_URI, // You must register this URI on App Registration. Defaults to window.location.href e.g. http://localhost:3000/
    navigateToLoginRequestUrl: false,
  },
  cache: {
    cacheLocation: "localStorage",
    storeAuthStateInCookie: false,
  },
  //   system: {
  //     loggerOptions: {
  //         loggerCallback: (level, message, containsPii) => {
  //             if (containsPii) {
  //                 return;
  //             }
  //             switch (level) {
  //                 case LogLevel.Error:
  //                     console.error(message);
  //                     return;
  //                 case LogLevel.Info:
  //                     console.info(message);
  //                     return;
  //                 case LogLevel.Verbose:
  //                     console.debug(message);
  //                     return;
  //                 case LogLevel.Warning:
  //                     console.warn(message);
  //                     return;
  //                 default:
  //                     return;
  //             }
  //         }
  //     }
  // }
};

export const loginRequest = {
  scopes: ["User.ReadBasic.All", "User.Read"],
};
export const msalInstance = new PublicClientApplication(msalConfig);

export const state = reactive({
  isAuthenticated: false,
  user: null,
});
