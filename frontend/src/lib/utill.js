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
    return false; // No token found
  }

  let decodeToken;

  if (typeof token === 'string') {
    const parts = token.split('.');
    if (parts.length !== 3) {
      return false; // Token is not well-formed (JWT must have three parts)
    }

    try {
      decodeToken = VueJwtDecode.decode(token);
    } catch (error) {
      return false; // Error in decoding token or invalid JSON
    }
  } else if (typeof token === 'object') {
    decodeToken = token;
  } else {
    return false; // Token is not a valid format
  }

  if (!decodeToken.exp) {
    return false; // No expiration field found
  }

  const currentTime = Math.floor(Date.now() / 1000);
  if (decodeToken.exp < currentTime) {
    return false; // Token is expired
  }

  return true; // Token is valid
}

export { convertString, toFormatDate, validateSizeInput,isTokenValid };
