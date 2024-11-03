import VueJwtDecode from "vue-jwt-decode";
import { refreshAccessToken } from "./fetchUtill.js";
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

async function refreshTokenAndReturn() {
  const userStore = useUserStore();
  const refreshTokenSuccess = await refreshAccessToken();
  if (typeof refreshTokenSuccess !== "object") {
    userStore.setAuthToken(null);
    return false;
  } else {
    userStore.setAuthToken(refreshTokenSuccess.access_token);
    return true;
  }
}

async function isTokenValid(token) {
  let decodedToken;
  const refresh_token = localStorage.getItem("refresh_token");

  // Validate the token format
  if (!token && !refresh_token) {
    return false;
  }
  try {
    decodedToken = VueJwtDecode.decode(token);
  } catch (error) {
    return await refreshTokenAndReturn();
  }

  if (!decodedToken || !decodedToken.exp) {
    return await refreshTokenAndReturn();
  }

  const currentTime = Math.floor(Date.now() / 1000);
  if (decodedToken.exp < currentTime) {
    return await refreshTokenAndReturn();
  } else {
    return true;
  }
}

function isNotDisable(isPublic, user, owner,collaBorator) {
  //isPublic ถ้า True จะเป็น public
  if (isPublic) {
    if (user !== undefined && (user === owner || (user === collaBorator?.oid && collaBorator?.accessRight === "WRITE" && collaBorator?.isPending === false))) {
      //ถ้า เป็น public แต้ owner = user ก็แก้ไข
      return true;
    } else {
      // board เป็น public แก้ไขไม่ได้
      return false;
    }
  } else {
    if (user !== undefined && (user === owner || (user === collaBorator?.oid && collaBorator?.accessRight === "WRITE" && collaBorator?.isPending === false))) {
      //ถ้า เป็น private แต้ owner = user ก็แก้ไข
      return true;
    } else {
      //ถ้า เป็น private แต้เป็น owner ก็แก้ไขไม่ได้
      return false;
    }
  }
}

//ไม่ส่ง header ถ้า Token is null
function tokenIsNull(token) {
  return {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
}

export {
  convertString,
  toFormatDate,
  validateSizeInput,
  isTokenValid,
  tokenIsNull,
  isNotDisable,
  refreshTokenAndReturn,
};
