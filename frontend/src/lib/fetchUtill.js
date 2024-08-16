const BASE_URL = import.meta.env.VITE_API_ROOT;

async function getFilteredTask([...filter] = "", sortBy = "id") {
  let res;
  try {
    res = await fetch(
      `${BASE_URL}/v2/tasks?sortBy=${sortBy}&filterStatuses=${filter}`
    );
    if (res.status === 200) {
      const tasks = await res.json();
      return tasks;
    } else {
      return undefined;
    }
  } catch (error) {
    return undefined;
  }
}

async function toggleLimitTask(maximum, isLimit) {
  let res;
  try {
    res = await fetch(
      `${BASE_URL}/v2/statuses/all/maximum-task?maximumTask=${maximum}&isLimit=${isLimit}`,
      {
        method: "PATCH",
        headers: {
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

async function getTaskById(id) {
  let res;
  try {
    res = await fetch(`${BASE_URL}/v2/tasks/${id}`);
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

async function getTaskByStatus(statusid) {
  let res;
  try {
    res = await fetch(`${BASE_URL}/v2/tasks/count/status/${statusid}`);
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

async function createTask(task) {
  let res;
  try {
    res = await fetch(`${BASE_URL}/v2/tasks`, {
      method: "POST",
      headers: {
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

async function updateTask(task) {
  let res;
  try {
    res = await fetch(`${BASE_URL}/v2/tasks/${task.id}`, {
      method: "PUT",
      headers: {
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

async function deleteTask(id) {
  let res;
  try {
    res = await fetch(`${BASE_URL}/v2/tasks/${id}`, {
      method: "DELETE",
    });
    return res.status;
  } catch (error) {
    return undefined;
  }
}

async function getAllStatus() {
  let res;
  try {
    res = await fetch(`${BASE_URL}/v2/statuses`);
    if (res.status === 200) {
      const tasks = await res.json();
      return tasks;
    } else {
      return undefined;
    }
  } catch (error) {
    return undefined;
  }
}

async function getStatusById(id) {
  let res;
  try {
    res = await fetch(`${BASE_URL}/v2/statuses/${id}`);
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

async function createStatus(Statuses) {
  let res;
  try {
    res = await fetch(`${BASE_URL}/v2/statuses`, {
      method: "POST",
      headers: {
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

async function updateStatus(Statuses) {
  let res;
  try {
    res = await fetch(`${BASE_URL}/v2/statuses/${Statuses.id}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(Statuses),
    });
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

async function deleteStatus(id) {
  let res;
  try {
    res = await fetch(`${BASE_URL}/v2/statuses/${id}`, {
      method: "DELETE",
    });
    return res.status;
  } catch (error) {
    return undefined;
  }
}
async function deleteStatusAndTranfer(OldStatusId, newStatusId) {
  let res;
  try {
    res = await fetch(`${BASE_URL}/v2/statuses/${OldStatusId}/${newStatusId}`, {
      method: "DELETE",
    });
    return res.status;
  } catch (error) {
    return undefined;
  }
}

async function getLimit() {
  let res;
  try {
    res = await fetch(`${BASE_URL}/v2/statuses/limit`);
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
    res = await fetch(`${BASE_URL}/v2/login` ,{
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(user),
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
};
