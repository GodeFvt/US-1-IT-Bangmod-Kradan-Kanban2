import VueJwtDecode from "vue-jwt-decode";

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

function isTokenValid(token) {
  if (!token) {
    console.log("No token found");
    return false; // No token found
  }

  let decodeToken;

  if (typeof token === 'string') {
    const parts = token.split('.');
    if (parts.length !== 3) {
      console.log("Token is not well-formed");
      return false; // Token is not well-formed (JWT must have three parts)
    }

    try {
      decodeToken = VueJwtDecode.decode(token);
    } catch (error) {
      console.log("Error decoding token:", error);
      return false; // Error in decoding token or invalid JSON
    }
  } else if (typeof token === 'object') {
    decodeToken = token;
  } else {
    console.log("Token is not a valid format");
    return false; // Token is not a valid format
  }

  if (!decodeToken.exp) {
    console.log("No expiration field found");
    return false; // No expiration field found
  }

  const currentTime = Math.floor(Date.now() / 1000);
  if (decodeToken.exp < currentTime) {
    console.log("Token is expired");
    return false; // Token is expired
  }

  console.log("Token is valid");
  return true; // Token is valid
}

export { convertString, toFormatDate, validateSizeInput,isTokenValid };
