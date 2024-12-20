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
  toggleVisibility,
  getAllBoards,
  downloadfile,
  addAttachments,
  previewfile,
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
import TaskBoard from "../components/task/TaskBoard.vue";
import AlertSquareIcon from "../components/icon/AlertSquareIcon.vue";
import { useTaskStore } from "../stores/tasks.js";
import { useStatusStore } from "../stores/statuses.js";
import AuthzPopup from "../components/AuthzPopup.vue";
import { useUserStore } from "../stores/user.js";
import { useBoardStore } from "../stores/boards.js";
import { isTokenValid, isNotDisable } from "../lib/utill.js";
import Toggle from "../components/icon/Toggle.vue";
import Themes from "../components/icon/Themes.vue";
import LoaderFile from "../components/icon/LoaderFile.vue";
// import HeaderView from "./HeaderView.vue";
// import SideMenuView from "./SideMenuView.vue";
const userStore = useUserStore();
const boardStore = useBoardStore();
const statusStore = useStatusStore();
const taskStore = useTaskStore();
const router = useRouter();
const route = useRoute();
const allTask = ref([]);
const statusFilter = ref([]);
const messageToast = ref("");
const typeToast = ref("");
const isEdit = ref(false);
const indexToRemove = ref(-1);
const controller = ref(null);
const showLoadingUpload = ref(false);

const task = ref({});
const isVisible = ref([]);

// show component
const showErrorMSG = ref(false);
const showLoading = ref(true);
const showLoadingDetail = ref(true);
const showLoadingFile = ref(true);
const showDetail = ref(false);
const showAttachments = ref(false);
const showToast = ref(false);
const showAddModal = ref(false);
const showDeleteModal = ref(false);
const showSettingModal = ref(false);
const showListStatus = ref(false);
const showPopUp = ref(false);
const showVisibilityModal = ref(false);

const boardId = ref(route.params.boardId);
const taskId = ref(1);
const maximumTask = ref(statusStore.maximumTask);
const toggleActive = ref(false);
const toggleVisibleActive = ref(false);
const boardName = ref(boardStore.currentBoard.name);
const fileURL = ref([]);

const allTaskLimit = ref([]); // allTask อันที่เกิน
const countStatus = computed(() => {
  return allTask.value.reduce(
    (accumulator, currentValue) => {
      accumulator[currentValue.status.name] =
        (accumulator[currentValue.status.name] || 0) + 1;
      statusStore.setNoOftask(accumulator);
      return accumulator;
    },
    { "No Status": 0, Done: 0 }
  );
});

function handleResponseError(responseCode) {
  if (responseCode === 401) {
    showPopUp.value = true;
  } else if (responseCode === 404) {
    router.push({ name: "TaskNotFound", params: { page: "Board" } });
  } else if (responseCode === 403) {
    router.push({ name: "TaskNotFound", params: { page: "authorizAccess" } });
  }
}

async function fetchData() {
  showLoading.value = true;
  if (userStore.authToken !== null) {
    if (boardStore.boards.length === 0) {
      const resBoard = await getAllBoards();

      if (resBoard === 401 || resBoard === 404 || resBoard === 403) {
        handleResponseError(resBoard);
      } else {
        boardStore.setAllBoard(resBoard);
      }
    }
  }
  toggleVisibleActive.value = boardStore.visibilityPublic;

  const resTask = await getFilteredTask(boardId.value);
  if (resTask === undefined) {
    showErrorMSG.value = true;
  } else if (resTask === 401 || resTask === 404 || resTask === 403) {
    handleResponseError(resTask);
  } else {
    taskStore.setAllTask(resTask);
    allTask.value = taskStore.allTask;
    maximumTask.value = statusStore.maximumTask;
    toggleActive.value = statusStore.isLimit;
    statusStore.setNoOftask(countStatus.value);

    if (statusStore.allStatus.length === 0) {
      const resStatus = await getAllStatus(boardId.value);
      if (resStatus === undefined) {
        showErrorMSG.value = true;
      } else if (resStatus === 401 || resStatus === 404 || resStatus === 403) {
        handleResponseError(resStatus);
      } else {
        statusStore.setAllStatus(resStatus);
      }
    }
    // if (statusStore.maximumTask === undefined) {
    const resLimit = await getLimit(boardId.value);
    if (resLimit === 401 || resLimit === 404 || resLimit === 403) {
      handleResponseError(resLimit);
    }
    statusStore.setMaximumTaskStatus(resLimit.maximumTask);
    statusStore.setLimitStatus(resLimit.isLimit);
    maximumTask.value = statusStore.maximumTask;
    toggleActive.value = statusStore.isLimit;
    // }

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

onMounted(async () => {
  if (!(await isTokenValid(userStore.encodeToken))) {
    // await handleBoardDetail();
    if (boardStore.visibilityPublic === false) {
      showPopUp.value = true;
      return;
    } else {
      await fetchData();
      countStatuses();
    }
  } else {
    // await handleBoardDetail();
    await fetchData();
    countStatuses();
  }
});

//เวลาเปลี่ยน board จาก sidebar
watch(
  () => route.params.boardId,
  (newBoardId, oldBoardId) => {
    boardId.value = newBoardId;
    boardName.value = boardStore.currentBoard.name;
    fetchData();
    countStatuses();
  }
);

const fileCanPreview = (name) => {
  if (/\.(png|jpeg|jpg|gif|bmp|svg|txt|rtf|pdf)$/g.test(name)) {
    return true;
  } else {
    return false;
  }
};

// react to route changes
watch(
  () => route.params.taskId,
  async (newId, oldId) => {
    if (newId !== undefined) {
      if (
        !(await isTokenValid(userStore.encodeToken)) &&
        boardStore.visibilityPublic === false
      ) {
        showPopUp.value = true;
        return;
      } else {
        showDetail.value = true;
        showAttachments.value = true;
        if (route.path === `/board/${boardId.value}/task/${newId}/edit`) {
          isEdit.value = true;
        } else {
          isEdit.value = false;
        }
        showLoadingDetail.value = true;
        showLoadingFile.value = true;
        const res = await getTaskById(boardId.value, newId);
        if (typeof res !== "object") {
          handleResponseError(res);
        } else {
          task.value = res;
          showLoadingDetail.value = false;
          task.value.attachments?.length > 0
            ? (showLoadingFile.value = true)
            : (showLoadingFile.value = false);
          const promises = task.value.attachments.map(async (file) => {
            if (fileCanPreview(file.filename)) {
              const resFile = await previewfile(
                boardId.value,
                newId,
                file.filename
              );
              if (typeof resFile !== "number") {
                fileURL.value.push({ name: file.filename, url: resFile });
              } else {
                fileURL.value.push({
                  name: file.filename,
                  url: "https://api.iconify.design/ic:baseline-sim-card-alert.svg",
                });
              }
            } else {
              fileURL.value.push({
                name: file.filename,
                url: "https://api.iconify.design/ic:baseline-sim-card-alert.svg",
              });
            }
          });

          // รอให้ promises ทั้งหมดทำงานเสร็จ
          await Promise.all(promises);
          showLoadingFile.value = false;
        }
      }
    }
  },
  { immediate: true }
);

async function confirmLimit(action) {
  if (!(await isTokenValid(userStore.encodeToken))) {
    showPopUp.value = true;
    return;
  } else {
    if (action === false) {
      showSettingModal.value = false;
      toggleActive.value = statusStore.isLimit;
      maximumTask.value = statusStore.maximumTask;
    } else if (toggleActive.value && action) {
      const res = await toggleLimitTask(boardId.value, maximumTask.value, true);
      if (res === 400 || res === 404) {
        typeToast.value = "warning";
        messageToast.value = `An error occurred enable limit task`;
      } else if (res === 500) {
        typeToast.value = "denger";
        messageToast.value = `An error occurred.please try again.`;
      } else if (res === 401 || res === 403) {
        handleResponseError(res);
      } else {
        statusStore.setLimitStatus(true);
        statusStore.setMaximumTaskStatus(maximumTask.value);
        allTaskLimit.value = res.filter((e) => e.noOfTasks > maximumTask.value);
        // statusStore.setAllStatusLimit(allTaskLimit.value);
        if (
          allTaskLimit.value.length !== 0 &&
          allTaskLimit.value !== undefined
        ) {
          showListStatus.value = true;
        }
        typeToast.value = "success";
        messageToast.value = `The Kanban board now limits ${maximumTask.value} tasks in each status`;
      }
      showToast.value = true;
    } else if (toggleActive.value === false && action) {
      const res = await toggleLimitTask(
        boardId.value,
        maximumTask.value,
        false
      );
      if (res === 400 || res === 404) {
        typeToast.value = "warning";
        messageToast.value = `An error occurred enable limit task`;
      } else if (res === 401 || res === 403) {
        handleResponseError(res);
      } else if (res === 500) {
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

async function confirmVisibility(action) {
  if (!(await isTokenValid(userStore.encodeToken))) {
    showPopUp.value = true;
    return;
  } else {
    toggleVisibleActive.value = !toggleVisibleActive.value;
    if (action === false) {
      showVisibilityModal.value = false;
      toggleVisibleActive.value = boardStore.visibilityPublic;
    } else if (action) {
      let visibility = toggleVisibleActive.value
        ? { visibility: "PUBLIC" }
        : { visibility: "PRIVATE" };
      const res = await toggleVisibility(boardId.value, visibility);
      if (res === 400 || res === 404) {
        typeToast.value = "warning";
        messageToast.value = `An error occurred enable visibility`;
        showToast.value = true;
      } else if (res === 403) {
        typeToast.value = "warning";
        messageToast.value = `You do not have permission to change board visibility mode.`;
        showToast.value = true;
      } else if (res === 500) {
        typeToast.value = "denger";
        messageToast.value = `There is a problem.Please try again later.`;
        showToast.value = true;
      } else if (res === 401) {
        handleResponseError(res);
      } else {
        boardStore.updatevIsibilityPublic(
          res.visibility === "PUBLIC" ? true : false
        );
        toggleVisibleActive.value = boardStore.visibilityPublic;
        if (res.visibility === "PUBLIC") {
          navigator.clipboard.writeText(window.location.href);
        }
        boardStore.setIsVisibilityCurrentBoard(res.visibility);
      }
      showVisibilityModal.value = false;
    }
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
  fileURL.value = [];
}

function ClickAdd() {
  showLoadingDetail.value = false;
  showDetail.value = true;
  isEdit.value = true;
  showAttachments.value = false;
  task.value = {
    title: "",
    description: "",
    assignees: "",
    status: { name: "No Status" },
  };
}

async function addEditTask(newTask, file, fileName) {
  const indexToCheck = allTask.value.findIndex(
    (task) => task.id === newTask.id
  );

  if (indexToCheck !== -1 && indexToCheck !== undefined) {
    showLoadingUpload.value = true;
    await editTask(newTask, file, fileName);
    showLoadingUpload.value = false;
  } else {
    await addTask(newTask);
  }
}

function cancelFetch() {
  controller.value.abort();
  controller.value = null;
  showLoadingUpload.value = false;
}

async function addTask(newTask) {
  if (!(await isTokenValid(userStore.encodeToken))) {
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
      const res = await createTask(boardId.value, newTask);
      if (res === 422 || res === 400 || res === 500 || res === 404) {
        typeToast.value = "warning";
        messageToast.value = `An error occurred adding the task`;
        showToast.value = true;
      } else if (res === 401 || res === 403) {
        handleResponseError(res);
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

async function editTask(editedTask, files, fileName) {
  if (!(await isTokenValid(userStore.encodeToken))) {
    showPopUp.value = true;
    return;
  } else {
    editedTask.status = editedTask.status.name;
    controller.value = new AbortController();
    const signal = controller.value.signal;

    const res = await updateTask(
      boardId.value,
      editedTask,
      files.map((e) => e.url),
      fileName,
      signal
    );

    if (res === 422 || res === 400 || res === 500 || res === 404) {
      typeToast.value = "warning";
      messageToast.value = `An error occurred updating the task "${editedTask.title}"`;
    } else if (res === 401 || res === 403) {
      handleResponseError(res);
    } else {
      const indexToUpdate = allTask.value.findIndex(
        (task) => task.id === editedTask.id
      );
      taskStore.editTask(indexToUpdate, res);
      statusStore.setNoOftask(countStatus.value);
      if (res.filesErrors.length !== 0) {
        typeToast.value = "warning";
        messageToast.value = `An error occurred, and the file update was unsuccessful`;
      } else {
        typeToast.value = "success";
        messageToast.value = `Task "${editedTask.title}" updated successfully`;
      }
    }
    showToast.value = true;
  }
}

async function removeTask(index, confirmDelete = false) {
  if (
    !(await isTokenValid(userStore.encodeToken)) &&
    boardStore.isCanEdit === true
  ) {
    showPopUp.value = true;
    return;
  } else {
    boardStore.isCanEdit
      ? (showDeleteModal.value = true)
      : (showDeleteModal.value = false);
    task.value = allTask.value[index];
    indexToRemove.value = index;
    if (confirmDelete) {
      const res = await deleteTask(boardId.value, task.value.id);
      if (res === 200) {
        statusStore.setNoOftask(countStatus.value);
        taskStore.deleteTask(index);
        typeToast.value = "success";
        messageToast.value = `The task has been deleted`;
      } else if (res === 400 || res === 404) {
        taskStore.deleteTask(index);
        typeToast.value = "warning";
        messageToast.value = `An error has occurred, the task does not exist.`;
      } else if (res === 401 || res === 403) {
        handleResponseError(res);
      } else {
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
    <div class="flex flex-col items-center h-full gap-5 mt-2">
      <div
        class="itbkk-button-home flex flex-row w-[95%] mt-5 max-sm:w-full max-sm:px-2 border-b border-gray-300 max-md:flex-col"
      >
        <div class="m-[2px] flex sm:items-center items-end w-full my-2">
          <router-link :to="{ name: 'board' }">
            <button
              class="flex items-center mx-2 text-gray-600 hover:text-gray-800"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="h-5 w-5"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M15 19l-7-7 7-7"
                />
              </svg>
            </button>
          </router-link>
          <div
            class="itbkk-BoardName text-gray-600 text-2xl max-md:text-xl max-sm:text-sm font-bold"
          >
            {{
              boardStore.currentBoard.owner.id === userStore.authToken?.oid
                ? boardStore.currentBoard.name + " Personal's Board"
                : boardStore.currentBoard.name + "Collaborate's Board"
            }}
          </div>
        </div>
        <!-- Filter -->
      </div>

      <!-- Task Table -->
      <div class="overflow-y-auto w-full overflow-x-hidden">
        <div class="flex justify-center mb-10">
          <div
            class="flex flex-col justify-center gap-3 w-[95%] max-sm:w-full max-sm:px-2 max-sm:gap-1"
          >
            <!-- Task Status Count -->
            <div class="flex flex-wrap justify-between">
              <div class="flex flex-row pr-1 drop-shadow-md">
                <div class="flex flex-wrap gap-[0.2rem]">
                  <div class="">
                    <router-link :to="{ name: 'ManageStatus' }">
                      <button
                        class="h-full cursor-pointer itbkk-manage-status mb-1 w-20 bg-gray-800 hover:bg-gray-500 text-white font-bold py-2 rounded-lg text-[0.9rem] max-sm:text-[0.89rem]"
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
              <div class="flex sm:mt-0 mb-2">
                <div class="flex flex-row items-end gap-1 flex-wrap mt-2">
                  <div
                    class="itbkk-board-visibility flex flex-col items-center cursor-pointer mr-2 disabled"
                    @click="
                      boardStore.currentBoard.owner.id ===
                      userStore.authToken?.oid
                        ? (showVisibilityModal = true)
                        : (showVisibilityModal = false)
                    "
                  >
                    <div
                      :class="
                        boardStore.currentBoard.owner.id ===
                        userStore.authToken?.oid
                          ? ''
                          : 'tooltip tooltip-bottom tooltip-hover'
                      "
                      data-tip="You need to be board owner to perform this action"
                    >
                      <span
                        class="font-bold visibility cursor-default mb-4"
                        @click.stop
                      >
                        {{
                          boardStore.visibilityPublic ? "Public" : "Private"
                        }}</span
                      >
                      <Toggle
                        :toggleActive="toggleVisibleActive"
                        :class="
                          boardStore.currentBoard.owner.id ===
                          userStore.authToken?.oid
                            ? 'cursor-pointer'
                            : 'cursor-not-allowed'
                        "
                      />
                    </div>
                  </div>

                  <div class="">
                    <router-link :to="{ name: 'AddTask' }">
                      <div
                        :class="
                          boardStore.isCanEdit
                            ? ''
                            : 'tooltip tooltip-bottom tooltip-hover'
                        "
                        data-tip="You need to be board owner or has write access to perform this action"
                      >
                        <button
                          class="itbkk-button-add bg-gray-800 hover:bg-gray-500 text-white font-bold py-2 px-4 rounded-lg text-[0.9rem] max-sm:text-[0.89rem]"
                          :disabled="!boardStore.isCanEdit"
                          :class="
                            boardStore.isCanEdit
                              ? 'cursor-pointer bg-gray-800'
                              : 'cursor-not-allowed disabled bg-gray-500'
                          "
                        >
                          Add Task
                        </button>
                      </div>
                    </router-link>
                  </div>
                  <div class="">
                    <router-link :to="{ name: 'ManageCollabView' }">
                      <div>
                        <button
                          class="itbkk-manage-collaborater bg-gray-800 hover:bg-gray-500 text-white font-bold py-2 px-4 rounded-lg text-[0.9rem] max-sm:text-[0.89rem]"
                        >
                          Manage Collabotater
                        </button>
                      </div>
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
                      class="dropdown-content z-50 flex flex-col gap-2 p-2 shadow bg-base-100 rounded-box w-[10rem] overflow-y-auto overflow-x-hidden"
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

            <TaskBoard
              :statusFilter="statusFilter"
              :allTask="taskStore.allTask"
              :showErrorMSG="showErrorMSG"
              :showLoading="showLoading"
              :allTaskLimit="allTaskLimit"
              @remove-task="removeTask"
            >
            </TaskBoard>
          </div>
        </div>
      </div>
      <TaskDetail
        v-if="showDetail"
        @user-action="closeTask"
        @addEdit="addEditTask"
        :task="task"
        :isEdit="isEdit"
        :showLoading="showLoadingDetail"
        :showLoadingFile="showLoadingFile"
        :showAttachment="showAttachments"
        :allTaskLimit="allTaskLimit"
        :fileUrl="fileURL"
      >
      </TaskDetail>
      <div
        class="absolute left-0 right-0 m-auto top-0 bg-black h-screen w-screen bg-opacity-50 z-50"
        v-if="showLoadingUpload"
      >
        <div class="itbkk-modal-task flex h-full items-center justify-center">
          <div
            class="itbkk-modal-task bg-gray-100 rounded-e-md h-[50%] w-[30rem] shadow-md overflow-y-auto border-l items-center justify-center flex flex-col gap-2 rounded-md relative"
          >
            <LoaderFile />
            <span>Uploading...</span>
            <div
              @click="cancelFetch()"
              class="absolute top-3 right-3 text-rose-600 hover:text-rose-400 cursor-pointer"
            >
              <CloseIcon />
            </div>
          </div>
        </div>
      </div>
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
            : {{ indexToRemove + 1 }}<br />
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
        :canEdit="boardStore.isCanEdit"
        :disabled="maximumTask > 10 || maximumTask <= 0"
        class="itbkk-modal-setting z-50"
      >
        <template #header>
          <div
            class="flex flex-col justify-items-end place-items-end cursor-pointer"
            @click="showSettingModal = false"
          >
            <CloseIcon />
          </div>
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
            <Toggle :toggleActive="toggleActive" />
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
              v-if="maximumTask > 10 || maximumTask <= 0"
              class="text-red-500"
            >
              <p>maximumTask must be less then or equal 10 and more then 0</p>
            </div>
          </div>
        </template>
      </ConfirmModal>

      <ConfirmModal
        v-if="showVisibilityModal"
        @user-action="confirmVisibility"
        :width="'w-[60vh]'"
        class="itbkk-modal-alert z-50"
      >
        <template #header>
          <div class="flex justify-center">
            <span class="text-gray-800 font-bold text-[1.5rem]">
              Board Visibility Changed ?
            </span>
          </div>
        </template>
        <template #body>
          <span class="itbkk-message">
            {{
              toggleVisibleActive
                ? "In private, only board owner can access/control board. Do you want to change board visibility to private?"
                : "In public, any one can view the board, task list and task detail of tasks in the board. Do you want to change board visibility to public?"
            }}
          </span>
        </template>
      </ConfirmModal>

      <limitModal
        v-if="showListStatus"
        @user-action="showListStatus = false"
        :allTaskLimit="allTaskLimit"
        class="z-40"
      >
      </limitModal>

      <AuthzPopup v-if="showPopUp" class="z-50" />
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
