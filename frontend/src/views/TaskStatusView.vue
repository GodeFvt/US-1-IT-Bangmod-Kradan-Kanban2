<script setup>
import { ref, onMounted, watch, computed, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  getAllStatus,
  getStatusById,
  createStatus,
  updateStatus,
  getTaskByStatus,
  deleteStatus,
  deleteStatusAndTranfer,
  toggleLimitTask,
  getLimit,
  getFilteredTask,
  getBoardsById,
  getAllBoards,
} from "../lib/fetchUtill.js";
import TaskStatusTable from "../components/status/TaskStatusTable.vue";
import TaskStatusDetail from "../components/status/TaskStatusDetail.vue";
import ConfirmModal from "../components/modal/ConfirmModal.vue";
import Toast from "../components/modal/Toasts.vue";
import AlertSquareIcon from "../components/icon/AlertSquareIcon.vue";
import { useStatusStore } from "../stores/statuses.js";
import { useTaskStore } from "../stores/tasks.js";
import limitModal from "../components/modal/limitModal.vue";
import SettingIcon from "../components/icon/SettingIcon.vue";
import AuthzPopup from "../components/AuthzPopup.vue";
import { useUserStore } from "../stores/user.js";
import { isTokenValid ,isNotDisable} from "../lib/utill.js";
import Toggle from "../components/icon/Toggle.vue";
import PopUp from "../components/modal/PopUp.vue";

const userStore = useUserStore();
const statusStore = useStatusStore();
const taskStore = useTaskStore();
const router = useRouter();
const route = useRoute();

// data
const allStatus = ref([]);
const status = ref({});
const allTaskLimit = ref([]); // allTask อันที่เกิน

// show component
const showErrorMSG = ref(false);
const showLoading = ref(true);
const showTranfer = ref(false);
const showDetail = ref(false);
const showToast = ref(false);
const showAddModal = ref(false);
const showDeleteModal = ref(false);
const showPopUp = ref(false);
const isEdit = ref(false);
// const authorizAccess = ref(false);

const toggleActive = ref(statusStore.isLimit);
const indexToRemove = ref(-1);
const messageToast = ref("");
const typeToast = ref("");
const showListStatus = ref(false);
const maximumTask = ref(statusStore.maximumTask);
const showSettingModal = ref(false);
const boardId = ref(route.params.boardId);
const tranferStatus = ref("No Status");
const boardName = ref();
const toggleVisibleActive = ref(false);

function handleResponseError(responseCode) {
  if (responseCode === 401) {
    showPopUp.value = true;
  } else if (responseCode === 404 || responseCode === 500 || responseCode === 400) {
    router.push({ name: "TaskNotFound", params: { page: "Board" } });
  } else if (responseCode === 403) {
    router.push({ name: "TaskNotFound", params: { page: "authorizAccess" } });
  }
}


async function fetchData() {
  
  if(userStore.authToken !== null){
        if (userStore.boards.length === 0) {
          const resBoard = await getAllBoards();
           if (resBoard === 401 || resBoard === 403 || resBoard === 404) {
        handleResponseError(resBoard)
      } else {
            userStore.setAllBoard(resBoard);
          }
        }
      }

    const resStatus = await getAllStatus(boardId.value);
    if (resStatus === undefined) {
      showErrorMSG.value = true;
    } else if (resStatus === 401 || resStatus === 403 || resStatus === 404) {
        handleResponseError(resStatus)
      } else {
      statusStore.setAllStatus(resStatus);
      allStatus.value = statusStore.allStatus;
      maximumTask.value = statusStore.maximumTask;
      toggleActive.value = statusStore.isLimit;
      statusStore.setNoOftask(countStatus.value);

        if (statusStore.maximumTask === undefined) {
          const resLimit = await getLimit(boardId.value);
           if (resLimit === 401 || resLimit === 403 || resLimit === 404) {
        handleResponseError(resLimit)
      }
          statusStore.setMaximumTaskStatus(resLimit.maximumTask);
          statusStore.setLimitStatus(resLimit.isLimit);
          maximumTask.value = statusStore.maximumTask;
          toggleActive.value = statusStore.isLimit;
        }
        if (taskStore.allTask.length === 0) {
          const resTask = await getFilteredTask(boardId.value);
          if (resTask === undefined) {
            showErrorMSG.value = true;
          }  else if (resTask === 401 || resTask === 403 || resTask === 404) {
        handleResponseError(resTask)
      } else {
            taskStore.setAllTask(resTask);
          }
        }

        showLoading.value = false;
      }
  }


async function handleBoardDetail(){
  const res = await getBoardsById(boardId.value);
   if (typeof res !== 'object') {
        handleResponseError(res)
      }
    else {
      userStore.updatevIsibilityPublic(res.visibility ==="PUBLIC" ?  true : false );
      toggleVisibleActive.value = userStore.visibilityPublic
      boardName.value = res.name;
      const oidByGet = res.owner.id;
      const oidByToken = userStore.authToken?.oid;
      userStore.updatevIsCanEdit(isNotDisable(
        userStore.visibilityPublic,
        oidByToken,
        oidByGet
      ))
    }
}


onMounted(async () => {

if (!(await isTokenValid(userStore.encodeToken))) {
  await handleBoardDetail()
  if(userStore.visibilityPublic === false){
  showPopUp.value = true;
  return
  }
  else {
    await fetchData();
  }
}
else
{
  await handleBoardDetail()
  await fetchData();
}
});

//ไว้ทำไรวะ 2
watch(
  () => route.params.boardId,
  (newBoardId, oldBoardId) => {
    boardId.value = newBoardId;
   fetchData();
  }
);

//noOfTask ไม่มาด้วยเลยต้องมาset
const countStatus = computed(() => {
  return taskStore.allTask.reduce(
    (accumulator, currentValue) => {
      accumulator[currentValue.status.name] =
        (accumulator[currentValue.status.name] || 0) + 1;
      statusStore.setNoOftask(accumulator);
      return accumulator;
    },
    { "No Status": 0 }
  );
});

watch(
  () => route.params.statusId,
  async (newId, oldId) => {
    if (newId !== undefined) {
      if (!(await isTokenValid(userStore.encodeToken)) && userStore.visibilityPublic === false) {
        showPopUp.value = true;
        return;
      } else {
        showLoading.value = true;
        const res = await getStatusById(boardId.value, newId);
         if (res === 401 || res === 403 || res === 404) {
        handleResponseError(res)
      } else {
          status.value = res;
          if (route.path === `/board/${boardId.value}/status/${newId}/edit`) {
            if(status.value.name === "No Status" || status.value.name === "Done"){
              showDetail.value = false;
              isEdit.value = false;
              router.push({ name: "ManageStatus" });
              return
            }
            isEdit.value = true;
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
watch(
  () => route.path,
  (newPath, oldPath) => {
    if (newPath === `/board/${boardId.value}/status/add`) {
      ClickAdd();
    }
  },
  { immediate: true }
);

function closeStatus(action) {
  showDetail.value = action;
  showAddModal.value = action;
  router.push({ name: "ManageStatus" });
}

function ClickAdd() {
  showLoading.value = false;
  showDetail.value = true;
  isEdit.value = true;
  status.value = {
    name: "",
    description: "",
    color: "#828282",
  };
}

async function addEditStatus(newStatus) {
  const indexToCheck = allStatus.value.findIndex(
    (status) => status.id === newStatus.id
  );

  if (indexToCheck !== -1 && indexToCheck !== undefined) {
    await editStatus(newStatus);
  } else {
    await addStatus(newStatus);
  }
}

async function addStatus(newStatus) {
  if (!(await isTokenValid(userStore.encodeToken))) {
    showPopUp.value = true;
    return;
  } else {
    if (newStatus.name === null || newStatus.name === "") {
      typeToast.value = "warning";
      messageToast.value = `The name is required`;
      showToast.value = true;
    } else {
      newStatus.name = newStatus.name.trim();
      newStatus.description = newStatus.description?.trim();
      const res = await createStatus(boardId.value, newStatus);
      if (res === 422 || res === 400 || res === 500 || res === 404) {
        typeToast.value = "warning";
        messageToast.value = `An error has occurred, the status could not be added`;
        showToast.value = true;
      } else if (res === 401 || res === 403) {
        handleResponseError(res)
      }else {
        typeToast.value = "success";
        statusStore.addStatus(res);
        if(userStore.findBoardById(boardId.value).isCustomStatus === false){
       const resStatus = await getAllStatus(boardId.value);
       statusStore.setAllStatus(resStatus)
      }
        messageToast.value = `The status has been added`;
        showToast.value = true;
      }
    }
  }
}

async function editStatus(editedStatus) {
  if (!(await isTokenValid(userStore.encodeToken))) {
    showPopUp.value = true;
    return;
  } else {
    const res = await updateStatus(boardId.value, editedStatus);
    if (res === 422 || res === 400 || res === 500 || res === 404) {
      typeToast.value = "warning";
      messageToast.value = `An error has occurred, the status does not exist`;
    } else if (res === 401 || res === 403) {
        handleResponseError(res)
      }else {
      typeToast.value = "success";
      const indexToUpdate = allStatus.value.findIndex(
        (status) => status.id === editedStatus.id
      );
      statusStore.editStatus(indexToUpdate, res);
      if(userStore.findBoardById(boardId.value).isCustomStatus === false){
       const resStatus = await getAllStatus(boardId.value);
       statusStore.setAllStatus(resStatus)
      }
      messageToast.value = `The status has been updated`;
    }
    showToast.value = true;
  }
}

const countTask = ref(0);
const noOftask = computed(() => statusStore.noOftask);

const limitThisTask = computed(() => {
  if (
    tranferStatus.value !== "No Status" &&
    tranferStatus.value !== "Done" &&
    toggleActive.value
  ) {
    // > เพราะว่าต้องสามาร tranfer แล้วพอดีกับค่าที่ limit ไว้ได้
    if (
      (noOftask.value[tranferStatus.value] ?? 0) + countTask.value >
      maximumTask.value
    ) {
      return true;
    } else {
      return false;
    }
  } else {
    return false;
  }
});

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
      } else if (res === 401 || res === 403) {
        handleResponseError(res)
      } else if (res === 500) {
        typeToast.value = "denger";
        messageToast.value = `An error occurred.please try again.`;
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
        messageToast.value = `An error occurred disabled limit task`;
      } else if (res === 401 || res === 403) {
        handleResponseError(res)
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

async function removeStatus(index, confirmDelete = false) {
  if (!(await isTokenValid(userStore.encodeToken))) {
    showPopUp.value = true;
    return;
  } else {
    //ได้ id ที่จะ tranfer
    let newStatus = allStatus.value.find((e) => e.name === tranferStatus.value);
    let res;
    if (confirmDelete) {
      if (showTranfer.value) {
        res = await deleteStatusAndTranfer(
          boardId.value,
          status.value.id,
          newStatus.id
        );
      } else {
        res = await deleteStatus(boardId.value, status.value.id);
      }

      if (res === 200) {
        typeToast.value = "success";
        if (countTask.value >= 0) {
          messageToast.value = `${countTask.value} task(s) have been transferred and the status has been deleted`;
          // update status ใน task เฉพาะการลบที่เกิดการ tranfer
          if (showTranfer.value) {
            taskStore.setAllTaskUpdate(status.value.id, newStatus);
          }
        } else {
          messageToast.value = `The status has been deleted`;
        }
        statusStore.deleteStatus(index);
        if(userStore.findBoardById(boardId.value).isCustomStatus === false){
       const resStatus = await getAllStatus(boardId.value);
       statusStore.setAllStatus(resStatus)
      }
      } else if (res === 404 || res === 400) {
        typeToast.value = "warning";
        messageToast.value = `An error has occurred, the status does not exist.`;
        statusStore.deleteStatus(index);
      } else if (res === 401 || res === 403) {
        handleResponseError(res)
      }else {
        typeToast.value = "denger";
        messageToast.value = `An error occurred.please try again.`;
      }
      showDeleteModal.value = false;
      showToast.value = true;
    }
  }
}

async function clickRemove(index) {
  if (!(await isTokenValid(userStore.encodeToken)) && userStore.isCanEdit) {
    showPopUp.value = true;
    return;
  } else {
    userStore.isCanEdit
      ? (showDeleteModal.value = true)
      : (showDeleteModal.value = false);
    if (showDeleteModal.value) {
      status.value = allStatus.value[index];
      indexToRemove.value = index;
      const res = await getTaskByStatus(boardId.value, status.value.id);
      if (res === 400 || res === 404 || res === 500) {
        typeToast.value = "danger";
        messageToast.value = `An error occurred deleting the status "${status.value.name}.`;
      } else if (res === 401 || res === 403) {
        handleResponseError(res);
      }  else {
        if (res.count >= 1) {
          tranferStatus.value = "No Status";
          showTranfer.value = true;
          countTask.value = res.count;
        } else {
          showTranfer.value = false;
        }
      }
    }
  }
}
</script>
<template>
  <div class="flex flex-col w-full h-screen">
    <!-- <div class="h-[8%]">
        <HeaderView class="h-full" />
      </div> -->
    <!-- Task Status and Add Task Button -->
    <div class="flex flex-col items-center h-full gap-5 mt-2">
      <div class="flex flex-row w-[95%] mt-5 max-sm:w-full max-sm:px-2">
        <!-- Task Status Count -->
        <div class="m-[2px] flex sm:items-center items-end w-full">
          <router-link :to="{ name: 'task' }">
            <button
              class="flex items-center mr-2 mt-2 text-gray-600 hover:text-gray-800"
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
          <div class="itbkk-button-home text-gray-600 text-[1.5rem] font-bold">
            {{ boardName }} Personal's Board
          </div>
        </div>
        <!-- <div class="m-[2px] flex sm:items-center items-end">
          <router-link :to="{ name: 'task' }">
            <div
              class="itbkk-button-home text-gray-800 text-[1rem] hover:underline hover:decoration-1"
            >
              Home
            </div>
          </router-link>
          <div class="mx-2 text-slate-500">/</div>

          <div class="text-gray-800 text-[1rem] font-bold">ManageStatus</div>
        </div> -->

        <!-- Filter -->
        <div class="flex items-end w-full justify-end">
          <div
            class="flex sm:flex-row flex-col sm:items-center items-end gap-1 sm:gap-4"
          >
            <div class="">
              <router-link :to="{ name: 'AddStatus' }">
                <div
                  :class="
                    userStore.isCanEdit ? '' : 'tooltip tooltip-bottom tooltip-hover'
                  "
                  data-tip="You need to be board owner to perform this action."
                >
                <button
                  class="itbkk-button-add bg-gray-800 hover:bg-gray-500 text-white font-bold py-2 px-4 rounded-lg text-[0.9rem] max-sm:text-[0.89rem]"
                  :disabled="!userStore.isCanEdit"
                    :class="
                      userStore.isCanEdit
                        ? 'cursor-pointer'
                        : 'cursor-not-allowed disabled'
                    "
                >
                  Add Status
                </button>
              </div>
              </router-link>
            </div>
            <!-- status setting -->

            <div class="m-[2px]" @click="showSettingModal = true">
              <button
                class="itbkk-status-setting bg-gray-200 hover:bg-gray-500 text-white font-bold py-2 px-4 rounded-lg"
              >
                <SettingIcon />
              </button>
            </div>
          </div>
        </div>
      </div>

      <div
        class="flex justify-center mt-4 gap-3 w-[95%] max-sm:w-full max-sm:px-2 max-sm:gap-1"
      >
        <!-- Status Table -->
        <TaskStatusTable
          :allStatus="statusStore.allStatus"
          :showErrorMSG="showErrorMSG"
          :showLoading="showLoading"
          @remove-status="clickRemove"
        ></TaskStatusTable>
      </div>
      <TaskStatusDetail
        v-if="showDetail"
        @user-action="closeStatus"
        @addEdit="addEditStatus"
        :status="status"
        :isEdit="isEdit"
        :showLoading="showLoading"
      >
      </TaskStatusDetail>

      <ConfirmModal
        v-if="showDeleteModal"
        @user-action="showDeleteModal = false"
        @confirm="removeStatus"
        :index="indexToRemove"
        :width="'w-[60vh]'"
        :disabled="limitThisTask"
        class="z-50"
      >
        <template #header>
          <div class="flex justify-center">
            <AlertSquareIcon class="w-16 h-16 opacity-40" />
          </div>
        </template>
        <template #body>
          <div v-if="showTranfer">
            <div class="itbkk-message my-2">
              There are {{ countTask }} tasks in {{ status.name }} status. In
              order to delete this status, the system must transfer tasks in
              this status to existing status. Transfer tasks to
              {{
                [...statusStore.allStatus]
                  .filter((e) => e.name !== status.name)
                  .map((e) => e.name)
              }}
            </div>
            <div class="">
              Tranfer to
              <select
                class="itbkk-status border-2 border-sky-400"
                v-model="tranferStatus"
              >
                <option
                  v-for="statusStore in [...statusStore.allStatus].filter(
                    (e) => e.name !== status.name
                  )"
                  selected="No Status"
                >
                  {{ statusStore.name }}
                </option>
              </select>
              <div class="text-rose-600" v-if="limitThisTask">
                Cannot transfer to {{ tranferStatus }} status since it will
                exceed the limit. Please choose another status to transfer to.
              </div>
            </div>
          </div>
          <div v-else>
            <div class="itbkk-message flex justify-center text-gray-800">
              <span
                >Do you want to delete the
                <span class="break-all"> {{ status.name }} </span> status?</span
              >
            </div>
          </div>
        </template>
        <template #confirm v-if="showTranfer">Transfer and Delete</template>
      </ConfirmModal>
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
          <div v-if="maximumTask > 30 || maximumTask <= 0" class="text-red-500">
            <p>maximumTask must be lees then 30 and more than 0</p>
          </div>
        </div>
      </template>
    </ConfirmModal>

    <limitModal
      v-if="showListStatus"
      @user-action="showListStatus = false"
      :allTaskLimit="allTaskLimit"
      class="z-40"
    >
    </limitModal>
    <AuthzPopup v-if="showPopUp"  class="z-50"/>
  </div>
</template>
<style scoped></style>
