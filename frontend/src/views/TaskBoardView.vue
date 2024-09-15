<script setup>
import { ref, onMounted, watch, computed } from "vue";
// import lib
import {
  deleteTask,
  getTaskById,
  createTask,
  updateTask,
  getAllStatus,
  getFilteredTask,
  toggleLimitTask,
  getLimit,
  getBoardsById,
} from "../lib/fetchUtill.js";
// import router
import { useRoute, useRouter } from "vue-router";
// import components
import TaskDetail from "../components/task/TaskDetail.vue";
import TaskStatusCard from "../components/status/TaskStatusCard.vue";
import SettingIcon from "../components/icon/SettingIcon.vue";
import CloseIcon from "../components/icon/CloseIcon.vue";
import ConfirmModal from "../components/modal/ConfirmModal.vue";
import limitModal from "../components/modal/limitModal.vue";
import Toast from "../components/modal/Toasts.vue";
import TaskTable from "../components/task/TaskTable.vue";
import AlertSquareIcon from "../components/icon/AlertSquareIcon.vue";
import { useTaskStore } from "../stores/tasks.js";
import { useStatusStore } from "../stores/statuses.js";
import AuthzPopup from '../components/AuthzPopup.vue';
import { useUserStore } from "../stores/user.js";
import { isTokenValid } from "../lib/utill.js";

// import HeaderView from "./HeaderView.vue";
// import SideMenuView from "./SideMenuView.vue";
const userStore = useUserStore();
const statusStore = useStatusStore();
const taskStore = useTaskStore();
const router = useRouter();
const route = useRoute();
const allTask = ref([]);
const statusFilter = ref([]);
const showErrorMSG = ref(false);
const showLoading = ref(true);
const showDetail = ref(false);
const showToast = ref(false);
const showAddModal = ref(false);
const messageToast = ref("");
const typeToast = ref("");
const isEdit = ref(false);
const indexToRemove = ref(-1);
const boardName = ref();
const task = ref({});
const isVisible = ref([]);

// show component
const showDeleteModal = ref(false);
const showSettingModal = ref(false);
const showListStatus = ref(false);
const showPopUp = ref(false);
const boardId = ref(route.params.boardId);
const maximumTask = ref(statusStore.maximumTask);
const toggleActive = ref(false);
const allTaskLimit = ref([]); // allTask อันที่เกิน



const countStatus = computed(() => {
  return allTask.value.reduce(
    (accumulator, currentValue) => {
      accumulator[currentValue.status.name] =
        (accumulator[currentValue.status.name] || 0) + 1;
      statusStore.setNoOftask(accumulator);
      return accumulator;
    },
    { "No Status": 0 }
  );
});

async function fetchData() {
  if (!isTokenValid(userStore.authToken)) {
        showPopUp.value = true;
        return;
      } else {
    const oidByToken = userStore.authToken.oid;
    const res = await getBoardsById(boardId.value);
    if (res === 404 || res === 400 || res === 500) {
        router.push({ name: "TaskNotFound", params: { page: "Board" } });
      }
      else if(res === 401){
        showPopUp.value = true;
      }
      else{
      boardName.value = res.name;
       const oidByGet = res.owner.id;
    if(oidByGet !== oidByToken){
        router.push({ name: "TaskNotFound", params: { page: "Board" } });
       } 
      }

  const resTask = await getFilteredTask(boardId.value);
  if (resTask === undefined) {
    showErrorMSG.value = true;
  } else if (resTask === 401) {
    showPopUp.value = true;
  } else {
    taskStore.setAllTask(resTask);
    allTask.value = taskStore.allTask;
  }

  maximumTask.value = statusStore.maximumTask;
  toggleActive.value = statusStore.isLimit;
  statusStore.setNoOftask(countStatus.value);

  if (statusStore.allStatus.length === 0) {
    const resStatus = await getAllStatus(boardId.value);
    if (resStatus === undefined) {
      showErrorMSG.value = true;
    } else if (resStatus === 401) {
      showPopUp.value = true;
    } else {
      statusStore.setAllStatus(resStatus);
    }
  }

  if (statusStore.maximumTask === undefined) {
    const resLimit = await getLimit(boardId.value);
    if (resLimit === 401) {
      showPopUp.value = true;
    }
    statusStore.setMaximumTaskStatus(resLimit.maximumTask);
    statusStore.setLimitStatus(resLimit.isLimit);
    maximumTask.value = statusStore.maximumTask;
    toggleActive.value = statusStore.isLimit;
  }

  showLoading.value = false;
}
}

function countStatuses() {
  const countStatusKeys = Object.keys(countStatus);
  countStatusKeys.forEach((status, index) => {
    setTimeout(() => {
      isVisible.value[index] = true;
    }, index * 150);
  });
}

onMounted(() => {
  fetchData();
  countStatuses();
});

watch(
  () => route.params.boardId,
  (newBoardId, oldBoardId) => {
    boardId.value = newBoardId;
    fetchData();
    countStatuses();
  }
);

// react to route changes
watch(
  () => route.params.taskId,
  async (newId, oldId) => {
    if (newId !== undefined) {
      if (!isTokenValid(userStore.authToken)) {
        showPopUp.value = true;
        return;
      } else {
      const res = await getTaskById(boardId.value,newId);
      if (res === 404 || res === 400 || res === 500) {
        router.push({ name: "TaskNotFound", params: { page: "Task" } });
      } else if (res === 401) {
       // go login 
       showPopUp.value = true
      } else {
        task.value = res;
        if (route.path === `/board/${boardId.value}/task/${newId}/edit`) {
          isEdit.value = true;
          console.log(isEdit.value)
        } else {
          isEdit.value = false;
        }
        showDetail.value = true;
      }
      showLoading.value = false;
    }
  }
},
  { immediate: true }
);

async function confirmLimit(action) {
  if (!isTokenValid(userStore.authToken)) {
        showPopUp.value = true;
        return;
      } else {
  if (action === false) {
    showSettingModal.value = false;
    toggleActive.value = statusStore.isLimit;
    maximumTask.value = statusStore.maximumTask;
  } else if (toggleActive.value && action) {
    const res = await toggleLimitTask(boardId.value,maximumTask.value, true);
    if (res === 400 || res === 404) {
      typeToast.value = "warning";
      messageToast.value = `An error occurred enable limit task`;
    } else if (res === 500) {
      typeToast.value = "denger";
      messageToast.value = `An error occurred.please try again.`;
    }  else if (res === 401) {
      // go login 
      showPopUp.value = true
    } else {
      statusStore.setLimitStatus(true);
      statusStore.setMaximumTaskStatus(maximumTask.value);
      allTaskLimit.value = res.filter((e) => e.noOfTasks > maximumTask.value);
      // statusStore.setAllStatusLimit(allTaskLimit.value);
      if (allTaskLimit.value.length !== 0 && allTaskLimit.value !== undefined) {
        showListStatus.value = true;
      }
      typeToast.value = "success";
      messageToast.value = `The Kanban board now limits ${maximumTask.value} tasks in each status`;
    }
    showToast.value = true;
  } else if (toggleActive.value === false && action) {
    const res = await toggleLimitTask(boardId.value,maximumTask.value, false);
    if (res === 400 || res === 404) {
      typeToast.value = "warning";
      messageToast.value = `An error occurred enable limit task`;
    }  else if (res === 401) {
      // go login 
      showPopUp.value = true
        }
     else if (res === 500) {
      typeToast.value = "denger";
      messageToast.value = `An error occurred.please try again.`;
    } else {
      statusStore.setLimitStatus(false);
      allTaskLimit.value = [];
      typeToast.value = "success";
      messageToast.value = `The Kanban board has disabled the task limit in each status.`;
    }
    showToast.value = true;
  }
  showSettingModal.value = false;
}
}
// react to route changes path add task
watch(
  () => route.path,
  (newPath, oldPath) => {
    if (newPath === `/board/${boardId.value}/task/add`) {
      ClickAdd();
    }
  },
  { immediate: true }
);

function closeTask(action) {
  showDetail.value = action;
  showAddModal.value = action;
  router.push({ name: "task" });
}

function ClickAdd() {
  showLoading.value = false;
  showDetail.value = true;
  isEdit.value = true;
  task.value = {
    title: "",
    description: "",
    assignees: "",
    status: { name: "No Status" },
  };
}

async function addEditTask(newTask) {
  const indexToCheck = allTask.value.findIndex(
    (task) => task.id === newTask.id
  );

  if (indexToCheck !== -1 && indexToCheck !== undefined) {
    console.log("edit T");
    await editTask(newTask);
  } else {
    console.log("add T");
    await addTask(newTask);
  }
}

async function addTask(newTask) {
  if (!isTokenValid(userStore.authToken)) {
        showPopUp.value = true;
        return;
      } else {
  if (newTask.title === null || newTask.title === "") {
    typeToast.value = "warning";
    messageToast.value = `The title is required`;
    showToast.value = true;
  } else {
    newTask.title = newTask.title.trim();
    newTask.description = newTask.description?.trim();
    newTask.assignees = newTask.assignees?.trim();
    newTask.status = newTask.status.name;
    const res = await createTask(boardId.value,newTask);
    if (res === 422 || res === 400 || res === 500 || res === 404) {
      typeToast.value = "warning";
      messageToast.value = `An error occurred adding the task`;
      showToast.value = true;
    }  else if (res === 401) {
       // go login 
       showPopUp.value = true
    } else {
      typeToast.value = "success";
      taskStore.addTask(res);
      statusStore.setNoOftask(countStatus.value);
      messageToast.value = `Task "${res.title}" added successfully`;
      showToast.value = true;
      
    }
  
  }
}
}

async function editTask(editedTask) {
  if (!isTokenValid(userStore.authToken)) {
        showPopUp.value = true;
        return;
      } else {
  editedTask.status = editedTask.status.name;
  const res = await updateTask(boardId.value,editedTask);
  if (res === 422 || res === 400 || res === 500 || res === 404) {
    typeToast.value = "warning";
    messageToast.value = `An error occurred updating the task "${editedTask.title}"`;
  }  else if (res === 401) {
       // go login 
       showPopUp.value = true
    } else {
    typeToast.value = "success";
    const indexToUpdate = allTask.value.findIndex(
      (task) => task.id === editedTask.id
    );
    taskStore.editTask(indexToUpdate, res);
    statusStore.setNoOftask(countStatus.value);
    messageToast.value = `Task "${editedTask.title}" updated successfully`;
  }
  showToast.value = true;
}
}

async function removeTask(index, confirmDelete = false) {
  if (!isTokenValid(userStore.authToken)) {
        showPopUp.value = true;
        return;
      } else {
  showDeleteModal.value = true;
  task.value = allTask.value[index];
  indexToRemove.value = index;
  if (confirmDelete) {
    const res = await deleteTask(boardId.value,task.value.id);
    if (res === 200) {
      statusStore.setNoOftask(countStatus.value);
      taskStore.deleteTask(index);
      typeToast.value = "success";
      messageToast.value = `The task has been deleted`;
    } else if (res === 400 || res === 404) {
      taskStore.deleteTask(index);
      typeToast.value = "warning";
      messageToast.value = `An error has occurred, the task does not exist.`;
    }  else if (res === 401) {
       // go login 
       showPopUp.value = true
    }
     else {
      typeToast.value = "danger";
      messageToast.value = `An error occurred deleting the task "${task.value.title}."`;
    }
    showDeleteModal.value = false;
    showToast.value = true;
  }
}
}
</script>

<template>
    <div class="flex flex-col w-full h-screen">
      <!-- <div class="h-[8%]">
        <HeaderView class="h-full" />
      </div> -->
      <div class="flex flex-col items-center h-full gap-5 mt-2">
        <!-- Task Status and Add Task Button -->
        <div class="itbkk-button-home flex flex-row w-[95%] mt-5 max-sm:w-full max-sm:px-2 border-b border-gray-300 slide-right">
          
          <div class="m-[2px] flex sm:items-center items-end w-full ">
            <router-link :to="{ name: 'board' }">
            <button class="flex items-center mr-2 mt-2 text-gray-600 hover:text-gray-800">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
        </svg>
      </button>
    </router-link>
              <div
                class="itbkk-BoardName text-gray-600 text-[1.5rem] font-bold"
              >
                {{boardName}} Personal's Board
              </div>
              
          </div>
          <!-- Filter -->
          <div class="flex items-end w-full justify-end sm:mt-0 mt-5 mb-2">
            <div class="flex flex-row items-center gap-1">
              <div class="">
                <router-link :to="{ name: 'AddTask' }">
                  <button
                    class="itbkk-button-add bg-gray-800 hover:bg-gray-500 text-white font-bold py-2 px-4 rounded-lg text-[0.9rem] max-sm:text-[0.89rem]"
                  >
                    Add Task
                  </button>
                </router-link>
              </div>

              <!--DropDown-->
              <div class="itbkk-status-filter dropdown dropdown-bottom">
                <button
                  tabindex="0"
                  class="flex gap-1 justify-center items-center bg-gray-200 hover:bg-gray-500 text-gray-800 font-bold py-2 px-4 rounded-lg text-[0.9rem] max-sm:text-[0.89rem]"
                >
                  Filter
                  <div
                    v-if="statusFilter.length !== 0"
                    class="bg-gray-500 rounded-lg px-[0.3rem] text-white"
                  >
                    {{ statusFilter.length }}
                  </div>
                  <div
                    class="itbkk-filter-clear cursor-pointer hover:text-red-400"
                  >
                    <CloseIcon
                      v-if="statusFilter.length !== 0"
                      @click="statusFilter = []"
                    />
                  </div>
                </button>
                <ul
                  tabindex="0"
                  class="dropdown-content flex flex-col gap-2 p-2 shadow bg-base-100 rounded-box w-[10rem] z-50 h-64 overflow-y-auto overflow-x-hidden"
                >
                  <li
                    v-for="status in statusStore.allStatus"
                    :key="status.name"
                    class="itbkk-filter-item p-2 hover:bg-gray-100 rounded-md"
                  >
                    <label class="itbkk-status-choice flex items-center">
                      <input
                        type="checkbox"
                        :value="status.name"
                        v-model="statusFilter"
                        class="itbkk-filter-item-clear checkbox"
                      />
                      <span class="ml-2 break-all">{{ status.name }}</span>
                    </label>
                  </li>
                </ul>
              </div>
              <!-- status setting -->
              <div class="" @click="showSettingModal = true">
                <button
                  class="itbkk-status-setting bg-gray-200 hover:bg-gray-500 text-white font-bold py-2 px-4 rounded-lg"
                >
                  <SettingIcon />
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Task Table -->
        <div
          class="flex flex-col justify-center mt-4 gap-3 w-[95%] max-sm:w-full max-sm:px-2 max-sm:gap-1"
        >
          <!-- Task Status Count -->
          <div class="flex">
            <div
              class="flex flex-row gap-[0.2rem] w-full overflow-y-auto overflow-x-hidden pr-1 drop-shadow-md"
            >
              <div class="z-50">
                <router-link :to="{ name: 'ManageStatus' }">
                  <button
                    class="h-full itbkk-manage-status mb-1 w-20 bg-gray-800 hover:bg-gray-500 text-white font-bold py-2 rounded-lg text-[0.9rem] max-sm:text-[0.89rem]"
                  >
                    Manage Status
                  </button>
                </router-link>
              </div>
              <div
                v-for="(status, index) in Object.keys(countStatus)"
                :key="status"
                :class="{ 'slide-in': isVisible[index] }"
                class="task-status-wrapper z-0"
              >
                <TaskStatusCard
                  :colorStatus="statusStore.getColorStatus(status)"
                >
                  <template #count>{{ countStatus[status] }}</template>
                  <template #status>{{ status }}</template>
                </TaskStatusCard>
              </div>
            </div>
          </div>

          <TaskTable
            :statusFilter="statusFilter"
            :allTask="taskStore.allTask"
            :showErrorMSG="showErrorMSG"
            :showLoading="showLoading"
            :allTaskLimit="allTaskLimit"
            @remove-task="removeTask"
          >
          </TaskTable>
        </div>

        <TaskDetail
          v-if="showDetail"
          @user-action="closeTask"
          @addEdit="addEditTask"
          :task="task"
          :isEdit="isEdit"
          :showLoading="showLoading"
          :allTaskLimit="allTaskLimit"
        >
        </TaskDetail>

        <ConfirmModal
          v-if="showDeleteModal"
          @user-action="showDeleteModal = false"
          @confirm="removeTask"
          :index="indexToRemove"
          class="z-50"
          width="w-[42vh]"
        >
          <template #header>
            <div class="flex justify-center">
              <AlertSquareIcon class="w-16 h-16 opacity-40" />
            </div>
          </template>
          <template #body>
            <span class="itbkk-message">
              Do you want to delete the task number
            </span>
            <span class="break-all">
              : {{ indexToRemove + 1 }}<br/>
              <div class="flex justify-center">
                "<span class="itbkk-message">{{ task.title }}</span
                >"
              </div>
            </span>
          </template>
        </ConfirmModal>

        <ConfirmModal
          v-if="showSettingModal"
          @user-action="confirmLimit"
          :width="'w-[60vh]'"
          :disabled="maximumTask > 30 || maximumTask <= 0"
          class="itbkk-modal-setting z-50"
        >
          <template #header>
            <div class="flex justify-center">
              <span class="text-gray-800 font-bold text-[1.5rem]">
                Status Settings
              </span>
            </div>
          </template>
          <template #body>
            <span class="itbkk-message">
              User can limit the number of task in status by setting the Maximum
              task in each status ( except "No Status" and "Done" statuses. )
            </span>
            <div
              class="flex flex-col items-center cursor-pointer gap-2 mt-2"
              @click="toggleActive = !toggleActive"
            >
              <span>Limit task in this status</span>
              <!-- Switch Container -->
              <div
                class="w-12 h-[1.2rem] flex items-center bg-gray-300 rounded-full p-1"
                :class="toggleActive ? 'bg-gray-500' : 'bg-gray-300'"
              >
                <!-- Switch -->
                <div
                  class="w-6 h-6 rounded-full shadow-md transform ease-out duration-300"
                  :class="
                    toggleActive ? 'translate-x-6 bg-black' : 'bg-gray-500'
                  "
                ></div>
              </div>
              <!-- Switch Container End -->
            </div>
            <div class="flex flex-col items-center mt-2 gap-2">
              <span>Maximum tasks</span>
              <input
                type="number"
                max="30"
                min="0"
                class="itbkk-max-task border border-black rounded-md px-1"
                v-model="maximumTask"
              />
              <div
                v-if="maximumTask > 30 || maximumTask <= 0"
                class="text-red-500"
              >
                <p>maximumTask must be lees then 30 and more than 0</p>
              </div>
            </div>
          </template>
        </ConfirmModal>

        <limitModal
          v-if="showListStatus"
          @user-action="showListStatus = false"
          :allTaskLimit="allTaskLimit"
          class="z-50"
        >
        </limitModal>

        <AuthzPopup v-if="showPopUp" />

      </div>
      <div
        class="fixed flex items-center w-full max-w-xs right-5 bottom-5"
        v-if="showToast"
      >
        <Toast :toast="typeToast" @close-toast="showToast = false">
          <template #message>
            <span class="itbkk-message break-all">{{ messageToast }}</span>
          </template>
        </Toast>
      </div>
    </div>
    <!-- <identifyUser
      :boardId="boardId"
      >
    </identifyUser> -->
</template>
<style scoped>
.task-status-wrapper {
  transform: translateX(-100%);
  opacity: 0;
  transition: transform 0.5s ease, opacity 0.5s ease;
}
.task-status-wrapper.slide-in {
  transform: translateX(0);
  opacity: 1;
}
.slide-right {
  -webkit-animation: slide-right 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94) both;
  animation: slide-right 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94) both;
}

@keyframes slide-right {
  0% {
    -webkit-transform: translateX(-100px);
    transform: translateX(-100px);
  }
  100% {
    -webkit-transform: translateX(0);
    transform: translateX(0);
  }
}
</style>
