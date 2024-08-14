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

export { convertString, toFormatDate, validateSizeInput };
