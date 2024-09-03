<script setup>
import { ref, onMounted, watch, computed,onUnmounted } from "vue";
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
import HeaderView from "./HeaderView.vue";
import AuthzPopup from '../components/AuthzPopup.vue';


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
const toggleActive = ref(statusStore.isLimit);
const indexToRemove = ref(-1);
const messageToast = ref("");
const typeToast = ref("");
const showListStatus = ref(false);
const maximumTask = ref(statusStore.maximumTask);
const showSettingModal = ref(false);

const tranferStatus = ref("No Status");

// variable for 401
const timeCount = ref(3);
let intervals = [];


onMounted(async () => {
  const resStatus = await getAllStatus();
  if (resStatus === undefined) {
    showErrorMSG.value = true;
  } else if (resStatus === 401) {
       // go login 
       showPopUp.value = true
    } else {
    statusStore.setAllStatus(resStatus);
    allStatus.value = statusStore.allStatus;
  }
  maximumTask.value = statusStore.maximumTask;
  toggleActive.value = statusStore.isLimit;
  statusStore.setNoOftask(countStatus.value);
  if (statusStore.maximumTask === undefined) {
    const resLimit = await getLimit();
     if (resLimit === 401) {
       // go login 
       showPopUp.value = true
    }
    statusStore.setMaximumTaskStatus(resLimit.maximumTask);
    statusStore.setLimitStatus(resLimit.isLimit);
    maximumTask.value = statusStore.maximumTask;
    toggleActive.value = statusStore.isLimit;
  }
  if (taskStore.allTask.length === 0) {
    const resTask = await getFilteredTask();
    if (resTask === undefined) {
      showErrorMSG.value = true;
    } else if (resTask === 401) {
       // go login 
       showPopUp.value = true
    }  else {
      taskStore.setAllTask(resTask);
    }
  }

  showLoading.value = false;
});






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
      const res = await getStatusById(newId);
      if (res === 404 || res === 400) {
        router.push({ name: "TaskNotFound", params: { page: "Status" } });
      }  else if (res === 401) {
       // go login 
       showPopUp.value = true
       } else {
        status.value = res;
        if (route.path === `/status/${newId}/edit`) {
          isEdit.value = true;
        } else {
          isEdit.value = false;
        }
        showDetail.value = true;
      }
      showLoading.value = false;
    }
  },
  { immediate: true }
);
watch(
  () => route.path,
  (newPath, oldPath) => {
    if (newPath === "/status/add") {
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
  if (newStatus.name === null || newStatus.name === "") {
    typeToast.value = "warning";
    messageToast.value = `The name is required`;
    showToast.value = true;
  } else {
    newStatus.name = newStatus.name.trim();
    newStatus.description = newStatus.description?.trim();
    const res = await createStatus(newStatus);
    if (res === 422 || res === 400 || res === 500 || res === 404) {
      typeToast.value = "warning";
      messageToast.value = `An error has occurred, the status could not be added`;
    }  else if (res === 401) {
       // go login 
       showPopUp.value = true
    } else {
      typeToast.value = "success";
      statusStore.addStatus(res);
      messageToast.value = `The status has been added`;
    }
    showToast.value = true;
  }
}

async function editStatus(editedStatus) {
  const res = await updateStatus(editedStatus);
  if (res === 422 || res === 400 || res === 500 || res === 404) {
    typeToast.value = "warning";
    messageToast.value = `An error has occurred, the status does not exist`;
  }  else if (res === 401) {
       // go login 
       showPopUp.value = true
    }  
  else {
    typeToast.value = "success";
    const indexToUpdate = allStatus.value.findIndex(
      (status) => status.id === editedStatus.id
    );
    statusStore.editStatus(indexToUpdate, res);
    messageToast.value = `The status has been updated`;
  }
  showToast.value = true;
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
  if (action === false) {
    showSettingModal.value = false;
    toggleActive.value = statusStore.isLimit;
    maximumTask.value = statusStore.maximumTask;
  } else if (toggleActive.value && action) {
    const res = await toggleLimitTask(maximumTask.value, true);
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
    const res = await toggleLimitTask(maximumTask.value, false);
    if (res === 400 || res === 404) {
      typeToast.value = "warning";
      messageToast.value = `An error occurred disabled limit task`;
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

async function removeStatus(index, confirmDelete = false) {
  //ได้ id ที่จะ tranfer
  let newStatus = allStatus.value.find((e) => e.name === tranferStatus.value);
  let res;
  console.log(newStatus.id);
  if (confirmDelete) {
    if (showTranfer.value) {
      res = await deleteStatusAndTranfer(status.value.id, newStatus.id);
    } else {
      res = await deleteStatus(status.value.id);
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
    } else if (res === 404 || res === 400) {
      typeToast.value = "warning";
      messageToast.value = `An error has occurred, the status does not exist.`;
      statusStore.deleteStatus(index);
    }  else if (res === 401) {
       // go login 
       showPopUp.value = true
    } else {
      typeToast.value = "denger";
      messageToast.value = `An error occurred.please try again.`;
    }
    showDeleteModal.value = false;
    showToast.value = true;
  }
}

async function clickRemove(index) {
  showDeleteModal.value = true;
  status.value = allStatus.value[index];
  indexToRemove.value = index;
  const res = await getTaskByStatus(status.value.id);
  if (res === 400 || res === 404 || res === 500) {
    typeToast.value = "danger";
    messageToast.value = `An error occurred deleting the status "${status.value.name}.`;
  }  else if (res === 401) {
       // go login 
       showPopUp.value = true
    } else {
    if (res.count >= 1) {
      tranferStatus.value = "No Status";
      showTranfer.value = true;
      countTask.value = res.count;
    } else {
      showTranfer.value = false;
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
        <div class="m-[2px] flex sm:items-center items-end">
          <router-link :to="{ name: 'task' }">
            <div
              class="itbkk-button-home text-gray-800 text-[1rem] hover:underline hover:decoration-1"
            >
              Home
            </div>
          </router-link>
          <div class="mx-2 text-slate-500">/</div>

          <div class="text-gray-800 text-[1rem] font-bold">ManageStatus</div>
        </div>

        <!-- Filter -->
        <div class="flex items-end w-full justify-end">
          <div
            class="flex sm:flex-row flex-col sm:items-center items-end gap-1 sm:gap-4"
          >
            <div class="">
              <router-link :to="{ name: 'AddStatus' }">
                <button
                  class="itbkk-button-add bg-gray-800 hover:bg-gray-500 text-white font-bold py-2 px-4 rounded-lg text-[0.9rem] max-sm:text-[0.89rem]"
                >
                  Add Status
                </button>
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
          :allStatus="allStatus"
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
          <!-- Switch Container -->
          <div
            class="itbkk-limit-task w-12 h-[1.2rem] flex items-center bg-gray-300 rounded-full p-1"
            :class="toggleActive ? 'bg-gray-500' : 'bg-gray-300'"
          >
            <!-- Switch -->
            <div
              class="w-6 h-6 rounded-full shadow-md transform ease-out duration-300"
              :class="toggleActive ? 'translate-x-6 bg-black' : 'bg-gray-500'"
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
      class="z-50"
    >
    </limitModal>
    <AuthzPopup v-if="showPopUp" />

  </div>
</template>
<style scoped></style>
