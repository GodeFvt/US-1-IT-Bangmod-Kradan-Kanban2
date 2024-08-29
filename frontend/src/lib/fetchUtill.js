const BASE_URL = import.meta.env.VITE_API_ROOT;


async function getFilteredTask([...filter] = "", sortBy = "id") {
  let res;
 const token = localStorage.getItem('authToken');
  try {
    res = await fetch(
      `${BASE_URL}/tasks?sortBy=${sortBy}&filterStatuses=${filter}`,
       {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        }
      }
    );
    if (res.status === 200) {
      const tasks = await res.json();
      return tasks;
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

async function toggleLimitTask(maximum, isLimit) {
  let res;
  const token= localStorage.getItem('authToken');
  try {
    res = await fetch(
      `${BASE_URL}/statuses/all/maximum-task?maximumTask=${maximum}&isLimit=${isLimit}`,
      {
        method: "PATCH",
        headers: {
          'Authorization': `Bearer ${token}`,
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
  const token= localStorage.getItem('authToken');
  try {
    res = await fetch(`${BASE_URL}/tasks/${id}`,{
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      }
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

async function getTaskByStatus(statusid) {
  let res;
  const token= localStorage.getItem('authToken');
  try {
    res = await fetch(`${BASE_URL}/tasks/count/status/${statusid}`,{
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      }
    });
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
  const token= localStorage.getItem('authToken');
  try {
    res = await fetch(`${BASE_URL}/tasks`, {
      method: "POST",
      headers: {
        'Authorization': `Bearer ${token}`,
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
  const token= localStorage.getItem('authToken');
  try {
    res = await fetch(`${BASE_URL}/tasks/${task.id}`, {
      method: "PUT",
      headers: {
        'Authorization': `Bearer ${token}`,
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
  const token= localStorage.getItem('authToken');
  try {
    res = await fetch(`${BASE_URL}/tasks/${id}`, {
      method: "DELETE",
      headers: {
        'Authorization': `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    });
    return res.status;
  } catch (error) {
    return undefined;
  }
}

async function getAllStatus() {
  let res;
  const token= localStorage.getItem('authToken');
  try {
    res = await fetch(`${BASE_URL}/statuses`,{
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      }
    }
  );
    if (res.status === 200) {
      const tasks = await res.json();
      return tasks;
    } 
    if (res.status === 401) {
      return res.status;
    } 
    else {
      return undefined;
    }
  } catch (error) {
    return undefined;
  }
}

async function getStatusById(id) {
  let res;
  const token= localStorage.getItem('authToken');
  try {
    res = await fetch(`${BASE_URL}/statuses/${id}`,
      {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        }
      }
    );
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
  const token= localStorage.getItem('authToken');
  try {
    res = await fetch(`${BASE_URL}/statuses`, {
      method: "POST",
      headers: {
        'Authorization': `Bearer ${token}`,
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
  const token= localStorage.getItem('authToken');
  try {
    res = await fetch(`${BASE_URL}/statuses/${Statuses.id}`, {
      method: "PUT",
      headers: {
        'Authorization': `Bearer ${token}`,
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
  const token= localStorage.getItem('authToken');
  try {
    res = await fetch(`${BASE_URL}/statuses/${id}`, {
      method: "DELETE",
      headers: {
        'Authorization': `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    });
    return res.status;
  } catch (error) {
    return undefined;
  }
}
async function deleteStatusAndTranfer(OldStatusId, newStatusId) {
  let res;
  const token= localStorage.getItem('authToken');
  try {
    res = await fetch(`${BASE_URL}/statuses/${OldStatusId}/${newStatusId}`, {
      method: "DELETE",
      headers: {
        'Authorization': `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    });
    return res.status;
  } catch (error) {
    return undefined;
  }
}

async function getLimit() {
  let res;
  const token= localStorage.getItem('authToken');
  try {
    res = await fetch(`${BASE_URL}/statuses/limit`,
      {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        }
      }
    );
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
    res = await fetch(`${BASE_URL}/login` ,{
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(user),
    });
    if(res.status ===200){
      const token = await res.json();
      return token
    }
    else {
      return res.status
    }
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
