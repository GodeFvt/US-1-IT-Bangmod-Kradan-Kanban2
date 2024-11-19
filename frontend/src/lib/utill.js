import VueJwtDecode from "vue-jwt-decode";
import { refreshAccessToken } from "./fetchUtill.js";
import { useUserStore } from "../stores/user.js";
import { msalService } from "../config/useAuth.js";

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
  if (userStore.isMicroSoftLogin) {
    // ใช้ getToken เพื่อดึง Access Token ใหม่
    if (newAccessToken) {
      userStore.setAuthToken(newAccessToken.accessToken);
      return true;
    } else {
      userStore.setAuthToken(null);
      return false;
    }
  }
  else{
  const refreshTokenSuccess = await refreshAccessToken();
  if (typeof refreshTokenSuccess !== "object") {
    userStore.setAuthToken(null);
    return false;
  } else {
    userStore.setAuthToken(refreshTokenSuccess.access_token);
    return true;
  }

}
}

async function isTokenValid(token) {
  let decodedToken;
  const userStore = useUserStore();
  const refresh_token = localStorage.getItem("refresh_token");

  if (!token && userStore.isMicroSoftLogin) {
    console.log('validateToken 1 ');
    return await refreshTokenAndReturn();
  }
  // Validate the token format
  if (!token && !refresh_token) {
    console.log('validateToken 2 ');
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

function isNotDisable(isPublic, user, owner, collaBorator) {
  //isPublic ถ้า True จะเป็น public
  if (isPublic) {
    if (
      user !== undefined &&
      (user === owner ||
        (user === collaBorator?.oid &&
          collaBorator?.accessRight === "WRITE" &&
          collaBorator?.isPending === false))
    ) {
      //ถ้า เป็น public แต้ owner = user ก็แก้ไข
      return true;
    } else {
      // board เป็น public แก้ไขไม่ได้
      return false;
    }
  } else {
    if (
      user !== undefined &&
      (user === owner ||
        (user === collaBorator?.oid &&
          collaBorator?.accessRight === "WRITE" &&
          collaBorator?.isPending === false))
    ) {
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

const previewBinary = (binaryObject) => {
  return URL.createObjectURL(binaryObject);
};

const removeURL = (binaryObject) => {
  console.log("removePreview");
  return URL.revokeObjectURL(binaryObject);
};

function validateFile(file , fileURL  ,index) {
  let result = {
    maxSize: { msg: '', filename: [] },
    maxFile: { msg: '', filename: [] },
    dupFile: { msg: '', filename: [] },
  };
  if (file.size > 20 * 1024 * 1024) {
    result.maxSize.msg = 'Each file cannot be larger than 20 MB. The following files are not added: '
    result.maxSize.filename.push(file.name) ;
  } 
   if (
    fileURL.length >= 10 ||
    index + fileURL.length > 10) {
      result.maxFile.msg =  'Each task can have at most 10 files. The following files are not added: '
      result.maxFile.filename.push(file.name) ;
  } 
    if (
    fileURL.filter((e) => e.name === file.name)
      .length >= 1
  ) {
    result.dupFile.msg =  'File with the same filename cannot be added or updated to the attachments. Please delete the attachment and add again to update the file: '
    result.dupFile.filename.push(file.name) ;

  }
  return  result 
}

export {
  convertString,
  toFormatDate,
  validateSizeInput,
  isTokenValid,
  tokenIsNull,
  isNotDisable,
  previewBinary,
  removeURL,
  refreshTokenAndReturn,
  validateFile,
};
