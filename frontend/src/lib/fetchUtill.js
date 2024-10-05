import { useUserStore } from "../stores/user.js";
import { tokenIsNull } from "./utill.js";

const BASE_URL = import.meta.env.VITE_API_ROOT;

async function getFilteredTask(boardId, [...filter] = "", sortBy = "id") {
  let res;
  const userStore = useUserStore();
  const token = userStore.encodeToken;
  try {
    res = await fetch(
      `${BASE_URL}/v3/boards/${boardId}/tasks?sortBy=${sortBy}&filterStatuses=${filter}`,
      {
        method: "GET",
        headers: tokenIsNull(token),
      }
    );
    if (res.status === 200) {
      const tasks = await res.json();
      return tasks;
    }
    if (res.status === 401 || res.status === 404) {
      return res.status;
    } else {
      return undefined;
    }
  } catch (error) {
    return undefined;
  }
}

async function toggleLimitTask(boardId, maximum, isLimit) {
  let res;
  const userStore = useUserStore();
  const token = userStore.encodeToken;
  try {
    res = await fetch(
      `${BASE_URL}/v3/boards/${boardId}/statuses/all/maximum-task?maximumTask=${maximum}&isLimit=${isLimit}`,
      {
        method: "PATCH",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      }
    );

    if (res.status === 200) {
      const data = await res.json();
      return data;
    } else {
      return res.status;
    }
  } catch (error) {
    return undefined;
  }
}

async function toggleVisibility(boardId, visibility) {
  let res;
  const userStore = useUserStore();
  const token = userStore.encodeToken;
  try {
    res = await fetch(`${BASE_URL}/v3/boards/${boardId}`, {
      method: "PATCH",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(visibility),
    });
    if (res.status === 200) {
      const data = await res.json();
      return data;
    } else {
      return res.status;
    }
  } catch (error) {
    return undefined;
  }
}

async function getTaskById(boardId, taskId) {
  let res;
  const userStore = useUserStore();
  const token = userStore.encodeToken;
  try {
    res = await fetch(`${BASE_URL}/v3/boards/${boardId}/tasks/${taskId}`, {
      method: "GET",
      headers: tokenIsNull(token),
    });
    if (res.status === 200) {
      const task = await res.json();
      return task;
    } else {
      return res.status;
    }
  } catch (error) {
    return undefined;
  }
}

async function getTaskByStatus(boardId, statusId) {
  let res;
  const userStore = useUserStore();
  const token = userStore.encodeToken;
  try {
    res = await fetch(
      `${BASE_URL}/v3/boards/${boardId}/tasks/count/status/${statusId}`,
      {
        method: "GET",
        headers: tokenIsNull(token),
      }
    );
    if (res.status === 200) {
      const task = await res.json();
      return task;
    } else {
      return res.status;
    }
  } catch (error) {
    return "error";
  }
}

async function createTask(boardId, task) {
  let res;
  const userStore = useUserStore();
  const token = userStore.encodeToken;
  try {
    res = await fetch(`${BASE_URL}/v3/boards/${boardId}/tasks`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(task),
    });
    if (res.status === 201) {
      const task = await res.json();
      return task;
    } else {
      return res.status;
    }
  } catch (error) {
    return undefined;
  }
}

async function updateTask(boardId, task) {
  let res;
  const userStore = useUserStore();
  const token = userStore.encodeToken;
  try {
    res = await fetch(`${BASE_URL}/v3/boards/${boardId}/tasks/${task.id}`, {
      method: "PUT",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(task),
    });
    if (res.status === 200) {
      const task = await res.json();
      return task;
    } else {
      return res.status;
    }
  } catch (error) {
    return undefined;
  }
}

async function deleteTask(boardId, taskId) {
  let res;
  const userStore = useUserStore();
  const token = userStore.encodeToken;
  try {
    res = await fetch(`${BASE_URL}/v3/boards/${boardId}/tasks/${taskId}`, {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    });
    return res.status;
  } catch (error) {
    return undefined;
  }
}

async function getAllStatus(boardId) {
  let res;
  const userStore = useUserStore();
  const token = userStore.encodeToken;
  try {
    res = await fetch(`${BASE_URL}/v3/boards/${boardId}/statuses`, {
      method: "GET",
      headers: tokenIsNull(token),
    });
    if (res.status === 200) {
      const tasks = await res.json();
      return tasks;
    }
    if (res.status === 401 || res.status === 404) {
      return res.status;
    } else {
      return undefined;
    }
  } catch (error) {
    return undefined;
  }
}

async function getStatusById(boardId, statusId) {
  let res;
  const userStore = useUserStore();
  const token = userStore.encodeToken;
  try {
    res = await fetch(`${BASE_URL}/v3/boards/${boardId}/statuses/${statusId}`, {
      method: "GET",
      headers: tokenIsNull(token),
    });
    if (res.status === 200) {
      const task = await res.json();
      return task;
    } else {
      return res.status;
    }
  } catch (error) {
    return undefined;
  }
}

async function createStatus(boardId, Statuses) {
  let res;
  const userStore = useUserStore();
  const token = userStore.encodeToken;
  try {
    res = await fetch(`${BASE_URL}/v3/boards/${boardId}/statuses`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(Statuses),
    });
    if (res.status === 201) {
      const statuses = await res.json();
      return statuses;
    } else {
      return res.status;
    }
  } catch (error) {
    return undefined;
  }
}

async function updateStatus(boardId, Statuses) {
  let res;
  const userStore = useUserStore();
  const token = userStore.encodeToken;
  try {
    res = await fetch(
      `${BASE_URL}/v3/boards/${boardId}/statuses/${Statuses.id}`,
      {
        method: "PUT",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(Statuses),
      }
    );
    if (res.status === 200) {
      const statuses = await res.json();
      return statuses;
    } else {
      return res.status;
    }
  } catch (error) {
    return undefined;
  }
}

async function deleteStatus(boardId, statusId) {
  let res;
  const userStore = useUserStore();
  const token = userStore.encodeToken;
  try {
    res = await fetch(`${BASE_URL}/v3/boards/${boardId}/statuses/${statusId}`, {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    });
    return res.status;
  } catch (error) {
    return undefined;
  }
}
async function deleteStatusAndTranfer(boardId, OldStatusId, newStatusId) {
  let res;
  const userStore = useUserStore();
  const token = userStore.encodeToken;
  try {
    res = await fetch(
      `${BASE_URL}/v3/boards/${boardId}/statuses/${OldStatusId}/${newStatusId}`,
      {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      }
    );
    return res.status;
  } catch (error) {
    return undefined;
  }
}

async function getLimit(boardId) {
  let res;
  const userStore = useUserStore();
  const token = userStore.encodeToken;
  try {
    res = await fetch(`${BASE_URL}/v3/boards/${boardId}/statuses/limit`, {
      method: "GET",
      headers: tokenIsNull(token),
    });
    if (res.status === 200) {
      const statusLimit = await res.json();
      return statusLimit;
    } else {
      return res.status;
    }
  } catch (error) {
    return undefined;
  }
}

async function loginAccount(user) {
  let res;
  try {
    res = await fetch(`${BASE_URL}/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(user),
    });
    if (res.status === 200) {
      const token = await res.json();
      return token;
    } else {
      return res.status;
    }
  } catch (error) {
    return undefined;
  }
}

async function getAllBoards() {
  let res;
  const userStore = useUserStore();
  const token = userStore.encodeToken;
  try {
    res = await fetch(`${BASE_URL}/v3/boards`, {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    });
    if (res.status === 200) {
      const allBoard = await res.json();
      return allBoard;
    }
    if (res.status === 401) {
      return res.status;
    } else {
      return undefined;
    }
  } catch (error) {
    return undefined;
  }
}

async function createBoard(board) {
  let res;
  const userStore = useUserStore();
  const token = userStore.encodeToken;

  try {
    res = await fetch(`${BASE_URL}/v3/boards`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(board),
    });
    if (res.status === 201) {
      const board = await res.json();
      return board;
    } else {
      return res.status;
    }
  } catch (error) {
    return undefined;
  }
}

async function getBoardsById(boardId) {
  let res;
  const userStore = useUserStore();
  const token = userStore.encodeToken;
  try {
    res = await fetch(`${BASE_URL}/v3/boards/${boardId}`, {
      method: "GET",
      headers: tokenIsNull(token), // Pass the dynamic headers object here
    });
    if (res.status === 200) {
      const board = await res.json();
      return board;
    } else {
      return res.status;
    }
  } catch (error) {
    return undefined;
  }
}

async function updateBoard(boardId, board) {
  let res;
  const userStore = useUserStore();
  const token = userStore.encodeToken;
  try {
    res = await fetch(`${BASE_URL}/v3/boards/${boardId}`, {
      method: "PUT",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(board),
    });
    if (res.status === 200) {
      const board = await res.json();
      return board;
    } else {
      return res.status;
    }
  } catch (error) {
    return undefined;
  }
}

async function deleteBoard(boardId) {
  let res;
  const userStore = useUserStore();
  const token = userStore.encodeToken;
  try {
    res = await fetch(`${BASE_URL}/v3/boards/${boardId}`, {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    });
    return res.status;
  } catch (error) {
    return undefined;
  }
}
async function refreshAccessToken() {
  let res;
  const refresh_Token = localStorage.getItem("refresh_token");
  try {
    res = await fetch(`${BASE_URL}/token`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${refresh_Token}`,
        "Content-Type": "application/json",
      },
    });
    if (res.status === 200) {
      const access_token = await res.json();
      return access_token;
    } else {
      return res.status;
    }
  } catch (error) {
    return undefined;
  }
}

async function getCollabs(boardId) {
  let res;
  const userStore = useUserStore();
  const token = userStore.encodeToken;
  try {
    res = await fetch(`${BASE_URL}/v3/boards/${boardId}/collabs`, {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    });
    if (res.status === 200) {
      const board = await res.json();
      return board;
    } else {
      return res.status;
    }
  } catch (error) {
    return undefined;
  }
}

async function addCollabs(boardId, collabs) {
  let res;
  const userStore = useUserStore();
  const token = userStore.encodeToken;
  try {
    res = await fetch(`${BASE_URL}/v3/boards/${boardId}/collabs`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(collabs),
    });
    if (res.status === 201) {
      const collab = await res.json();
      return collab;
    } else {
      return res.status;
    }
  } catch (error) {
    return undefined;
  }
}

async function updateCollabs(boardId, collabs) {
  let res;
  const userStore = useUserStore();
  const token = userStore.encodeToken;
  try {
    res = await fetch(
      `${BASE_URL}/v3/boards/${boardId}/collabs/${collabs.oid}`,
      {
        method: "PATCH",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(collabs),
      }
    );
    if (res.status === 200) {
      const collab = await res.json();
      return collab;
    } else {
      return res.status;
    }
  } catch (error) {
    return undefined;
  }
}

async function deleteCollabs(boardId, collabId) {
  let res;
  const userStore = useUserStore();
  const token = userStore.encodeToken;
  try {
    res = await fetch(`${BASE_URL}/v3/boards/${boardId}/collabs/${collabId}`, {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    });
    return res.status;
  } catch (error) {
    return undefined;
  }
}


export {
  getTaskByStatus,
  getTaskById,
  createTask,
  updateTask,
  deleteTask,
  getAllStatus,
  getStatusById,
  createStatus,
  updateStatus,
  deleteStatus,
  deleteStatusAndTranfer,
  getFilteredTask,
  toggleLimitTask,
  getLimit,
  loginAccount,
  getAllBoards,
  createBoard,
  getBoardsById,
  updateBoard,
  deleteBoard,
  refreshAccessToken,
  toggleVisibility,
  getCollabs,
  addCollabs,
  updateCollabs,
  deleteCollabs,
};
