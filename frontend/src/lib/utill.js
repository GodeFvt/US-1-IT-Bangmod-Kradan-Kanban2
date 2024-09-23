import VueJwtDecode from "vue-jwt-decode";
import {
  refreshAccessToken,
} from "./fetchUtill.js";
import { useUserStore } from "../stores/user.js";


function convertString(string) {
  return string
    ?.toLowerCase()
    .split("_")
    .map((e) => e.at(0).toUpperCase() + e.slice(1))
    .join(" ");
}

function convertToUpperString(string) {
  return string?.toUpperCase().replace(/\s+/g, "_");
}

function toFormatDate(date) {
  return date?.split(",").join("");
}

function validateSizeInput(...properties) {
  const style =
    "border border-red-400 rounded-md p-1 focus:outline-red-500 text-red-500";
  const messages = (propertyName) => `${propertyName} character too long `;
  return properties.map((curr) => {
    if (curr.propLenght > curr.size) {
      return { boolean: true, msg: messages(curr.propName), style };
    } else {
      return {
        boolean: false,
        msg: "",
        stlye: "border border-gray-800 text-black",
      };
    }
  });
}

async function isTokenValid(token) {
  const userStore = useUserStore();

  if (!token) {
    return false; // ไม่มีToken
  }

  let decodedToken;
  try {
    decodedToken = VueJwtDecode.decode(token);
  } catch (error) {
    return false; // Tokenไม่สามารถ decode ได้
  }

  if (!decodedToken || !decodedToken.exp) {
    const refreshTokenSuccess = await refreshAccessToken();

    if (refreshTokenSuccess) {
      userStore.setAuthToken(refreshTokenSuccess.access_token)
      return true;
    } else {
      return false;
    }
  }

  const currentTime = Math.floor(Date.now() / 1000);
  if (!decodedToken.exp || decodedToken.exp < currentTime) {
    // ถ้าโทเค็นหมดอายุ พยายามรีเฟรชโทเค็น
    const refreshTokenSuccess = await refreshAccessToken();
    if (typeof refreshTokenSuccess === "string") {
      userStore.setAuthToken(refreshTokenSuccess.access_token)
      return true;
    }
     else {
      return false;
    }
  }

  return true; // Tokenยังใช้ได้
}


export { convertString, toFormatDate, validateSizeInput, isTokenValid };
