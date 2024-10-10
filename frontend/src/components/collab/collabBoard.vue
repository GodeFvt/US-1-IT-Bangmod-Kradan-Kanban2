<script setup>
import TaskTableLoading from "../loading/TaskTableLoading.vue";
import { useUserStore } from "../../stores/user.js";
import { useRoute, useRouter } from "vue-router";
import collabDetail from "./collabDetail.vue"
import {onMounted, ref, watch } from "vue";
import DeleteIcon from "../icon/DeleteIcon.vue";
import ConfirmModal from "../modal/ConfirmModal.vue"
import CloseIcon from "../icon/CloseIcon.vue"
import { addCollabs, getCollabs,deleteCollabs, updateCollabs,getAllBoards } from "../../lib/fetchUtill";
import { isTokenValid } from "../../lib/utill";
import Toast from "../modal/Toasts.vue";
import { useBoardStore } from "../../stores/boards.js";
import AuthzPopup from "../AuthzPopup.vue";

const props = defineProps({
  showErrorMSG: {
    type: Boolean,
    default: false,
  },
  showLoading: {
    type: Boolean,
    // default: true,
  },
});

const userStore = useUserStore();
const boardStore = useBoardStore();
let accessRight = ref(["READ" , "WRITE"]);
const accessSelect = ref("");
const oldAccess = ref("");
const showToast = ref(false);
const typeToast = ref("");
const messageToast = ref("");
const router = useRouter();
const route = useRoute();
const boardId = ref(route.params.boardId);
const isVisible = ref([]);
const isShowAddCollab = ref(false);
const showConfirmModal= ref(false);
const isChangeAccess= ref(false);
const showPopUp = ref(false);
const showLoading = ref(true);
const errorMSG = ref("");
const username = ref(""); // เปลี่ยนเป็น ref เพื่อให้ตอบสนองต่อการเปลี่ยนแปลง
const usernameId = ref(null); // ตั้งค่าเริ่มต้นเป็น null

function handleResponseError(responseCode) {
  if (responseCode === 401) {
    showPopUp.value = true;
  } else if (
    responseCode === 404 ||
    responseCode === 500 ||
    responseCode === 400
  ) {
    router.push({ name: "TaskNotFound", params: { page: "Board" } });
  } else if (responseCode === 403) {
    router.push({ name: "TaskNotFound", params: { page: "authorizAccess" } });
  }
}

watch(
  () => boardStore.collabs,
  () => {
    boardStore.collabs.forEach((task, index) => {
      setTimeout(() => {
        isVisible.value[index] = true;
      }, (index + 1) * 150);
    });
  },
  { deep: true }
);



watch(
  () => accessSelect.value,
  (newSelect, oldSelect) => {
    console.log("wdw",newSelect);
    console.log("wdw2",oldSelect);
   if (newSelect !== oldSelect) {
    isChangeAccess.value=true
    oldAccess.value = oldSelect
   } else {
       showConfirmModal.value= false
   }
  },
)

onMounted(async () => {
  if (!(await isTokenValid(userStore.encodeToken))) {
    showPopUp.value = true;
    return;
  }
  if (userStore.boards.length === 0) {
      const resBoard = await getAllBoards();

      if (resBoard === 401 || resBoard === 404 || resBoard === 403) {
        handleResponseError(resBoard);
      } else {
        userStore.setAllBoard(resBoard);
      }
  }
 const res = await getCollabs(boardId.value);
 if(typeof res === "object"){
  boardStore.setCollabs(res);
  const indexCollab = boardStore.collabs.findIndex(collab => collab.oid === userStore.authToken.oid); //หา index ของ user ที่ login อยู่
  accessSelect.value = boardStore.collabs[indexCollab].access; //เอา access ของ user ที่ login อยู่ ต้องทำเพราะถ้าเข้ามาแล้วเปลี่ยนค่าทันทีมันจะเปลี่ยนเป็น null ค่า default นั่นแหละ
 }
 else{
  handleResponseError(res);
}
showLoading.value = false;
});



async function addCollaborator(collab) {
  if (!(await isTokenValid(userStore.encodeToken))) {
    showPopUp.value = true;
    return;
  }
  if (collab.email === null || collab.email === "") {
      typeToast.value = "warning";
      messageToast.value = `Must enter the email.`;
    } else {
      collab.email = collab.email.trim();
      collab.access = collab.access.toUpperCase();
  const res = await addCollabs(boardId.value,collab);
  if(typeof res === "object"){
    typeToast.value = "success";
        // taskStore.addTask(res);
        messageToast.value = `Collaborator "${res.email}" added successfully.`;
        boardStore.addCollab(res);
        isShowAddCollab.value=false;
}
else if(res ===404){
 //The user "${collab.email}" does not exists. ที่ addModal
 errorMSG.value = `The user "${collab.email}" does not exists.`;
}
else if (res === 401) {
  handleResponseError(res);
}
else if(res===403){
  typeToast.value = "warning";
  messageToast.value = `You do not have permission to add collaborator.`;
  isShowAddCollab.value=false;
}
else if (res===409){
  // The user "${collab.email}" is already the collaborator of this board. ที่ addModal
  errorMSG.value = `The user "${collab.email}" is already the collaborator of this board.`;
}
else{
  typeToast.value = "danger";
  messageToast.value = `There is a problem please try again later.`;
  isShowAddCollab.value=false;
}
}
showToast.value = true;

}



async function changeAccessOrRemoveCollab(confirmValue = false) {
  if (!(await isTokenValid(userStore.encodeToken))) {
    showPopUp.value = true;
    return;
  }
  if (isChangeAccess.value) {
    //แก้ไข access right ของ user
    let collab = boardStore.collabs[usernameId.value];
     if (confirmValue) {
    const res = await updateCollabs(boardId.value,collab);
    if(typeof res === "object"){
      typeToast.value = "success";
      messageToast.value = `Access right of "${collab.name}" changed to "${collab.access}" successfully.`;
      boardStore.updateAccessCollab(usernameId.value,res.access);

    }
    else if(res === 404){
      typeToast.value = "warning";
      messageToast.value = `The user "${collab.name}" is not the collaborator of this board.`;
      boardStore.removeCollab(usernameId.value);//ลบ user ออกจาก boardถ้าไม่กดแล้วไม่เจอ เช่นโดนลบออกจาก board โดยเจ้าของ ณ ตอนนั้น
    }
    else if (res === 401) {
      handleResponseError(res);
    }
    else if(res ===403){
      typeToast.value = "warning";
      messageToast.value = `You do not have permission to change collaborator access right.`;
      collab.access = oldAccess.value;
    }
    else{
      typeToast.value = "danger";
      messageToast.value = `There is a problem please try again.`;

    }

  } else {
    //ถ้ากด cancle ให้ทำให้  accessSelect เป็นค่าเดิมของ user นั้นๆ 
    collab.access = oldAccess.value;
  }
  } else {
    //ลบ user ออกจาก board
    if (confirmValue) {
      let collabOid = boardStore.collabs[usernameId.value].oid;
      const res = await deleteCollabs(boardId.value,collabOid);
      if(res===200){
        typeToast.value = "success";
        messageToast.value = `Collaborator "${boardStore.collabs[usernameId.value].name}" removed successfully.`;
        boardStore.removeCollab(usernameId.value);
      }
      else if(res === 404){
        typeToast.value = "warning";
        messageToast.value = `The user "${boardStore.collabs[usernameId.value].name}" is not the collaborator of this board.`;
      }
      else if (res === 401 || res === 403) {
        handleResponseError(res);
      }
      else{
        typeToast.value = "danger";
        messageToast.value = `There is a problem please try again.`;
      }
  }

  }
  showConfirmModal.value=false
  showToast.value = true;
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
            <div class="itbkk-board-name text-gray-600 text-[1.5rem] font-bold">
              {{
              userStore.currentBoard.owner.id === userStore.authToken?.oid
                ? userStore.currentBoard.name + " Personal's Board"
                : userStore.currentBoard.name + "Collaborate's Board"
            }}
            </div>
          </router-link>

          <button
            class="flex items-center mr-2 mt-2 text-gray-600 hover:text-gray-800 rotate-180"
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
          <div class="text-gray-900 text-[1.5rem] font-bold">Collaborater</div>
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

                <div
                  :class="
                    userStore.isCanEdit
                      ? ''
                      : 'tooltip tooltip-bottom tooltip-hover'
                  "
                  data-tip="You need to be board owner to perform this action."
                >
                  <button
                    class="itbkk-button-add bg-gray-800 hover:bg-gray-500 text-white font-bold py-2 px-4 rounded-lg text-[0.9rem] max-sm:text-[0.89rem]"
                    :disabled="userStore.currentBoard.owner.id !== userStore.authToken?.oid"
                    :class="
                      userStore.currentBoard.owner.id === userStore.authToken?.oid
                        ? 'cursor-pointer'
                        : 'cursor-not-allowed'
                    "
                    @click="isShowAddCollab=true,errorMSG=''"
                  >
                    Add Collaborater
                  </button>
                </div>
          </div>
        </div>
      </div>

      <div
        class="flex justify-center mt-4 gap-3 w-[95%] max-sm:w-full max-sm:px-2 max-sm:gap-1"
      >
        <!-- Status Table -->
        <TaskTableLoading v-if="showLoading" class="w-full" />

        <div v-else class="w-full rounded-md shadow-xl">
          <div
            class="flex justify-center mt-4 gap-3 w-[100%] max-sm:w-full max-sm:px-2 max-sm:gap-1"
          >
            <table class="w-full rounded-md">
              <thead
                class="text-xs text-gray-700 uppercase bg-gray-50 w-full rounded-t-md"
              >
                <tr class="flex w-full rounded-t-md">
                  <th
                    class="px-6 py-4 w-[5%] max-lg:hidden border-l-4 rounded-tl-md"
                  >
                    No.
                  </th>
                  <th
                    class="px-6 py-4 w-[40%] max-lg:w-[30%] max-lg:px-2 max-lg:py-3 max-lg:border-l-4"
                  >
                    Name
                  </th>
                  <th
                    class="px-6 py-4 w-[30%] max-lg:w-[45%] max-lg:px-2 max-lg:py-3"
                  >
                    E-mail
                  </th>
                  <th
                    class="px-4 py-4 w-[20%] max-lg:w-[25%] max-lg:px-2 max-lg:py-3 rounded-tr-md"
                  >
                    Acess Right
                  </th>
                  <th
                    class="px-4 py-4 w-[20%] max-lg:w-[25%] max-lg:px-2 max-lg:py-3 rounded-tr-md"
                  >
                    Action
                  </th>
                </tr>
              </thead>
              <tbody
                class="h-[73vh] max-sm:h-[50vh] flex flex-col items-center overflow-y-auto w-full overflow-x-hidden"
              >
                <tr
                  class="itbkk-item flex w-full items-center justify-center bg-white border-l-4 border-b"
                  v-for="(collab, index) in boardStore.collabs"
                  :key="index"
                  :class="{ 'slide-in': isVisible[index] }"
                  @click="username = collab.name; usernameId = index"
                >
                  <td class="px-6 py-4 max-lg:hidden w-[5%]">
                    {{ index + 1 }}
                    
                  </td>

                  <td
                    class="h-full w-[40%] px-6 py-4 max-lg:w-[30%] max-lg:px-2 max-lg:py-3 hover:bg-neutral-100"
                  >
                    <div
                      class="cursor-pointer h-full w-full items-center flex break-all"
                    >
                      <span class="itbkk-collab-name"> {{ collab.name }} </span>
                    </div>
                  </td>

                  <td
                    class="w-[30%] px-6 py-4 max-lg:[45%] max-lg:px-2 max-lg:py-3 break-all"
                    :class="
                      collab.email === null ||
                      collab.email?.length === 0
                        ? 'italic text-gray-600'
                        : ''
                    "
                  >
                    <span class="itbkk-email">
                      {{
                        collab.email
                      }}
                    </span>
                  </td>
                  <td
                    class="w-[20%] px-6 py-4 max-lg:[45%] max-lg:px-2 max-lg:py-3 break-all"
                  >
                    <div class="itbkk-acess-right">
                      <select
                        class="itbkk-collab border-2 border-gray-500 w-[10rem] h-[30px] rounded-lg"
                        v-model="collab.access"
                        @change="accessSelect = collab.access,showConfirmModal=true,usernameId = index"
                      >
                        <option
                          v-for="access in accessRight"
                          
                        >
                          {{ access }}
                        </option>
                      </select>
                    </div>
                  </td>
                  <td
                    class="w-[20%] px-4 py-4 max-lg:w-[25%] max-lg:px-2 max-lg:py-3 cursor-pointer flex justify-center items-center"
                  >
                    <div class="flex flex-row gap-4 max-sm:flex-col">
                      <div
                        :class="
                          userStore.isCanEdit
                            ? ''
                            : 'tooltip tooltip-top tooltip-hover'
                        "
                        data-tip="You need to be board owner to perform this action."
                      >
                        <div
                          class="itbkk-collab-remove text-white fill-rose-300"
                          @click="isChangeAccess=false , usernameId = index , showConfirmModal= true"
                          :disable="!userStore.isCanEdit"
                          :class="
                            userStore.isCanEdit
                              ? 'cursor-pointer'
                              : 'cursor-not-allowed disabled'
                          "
                        >
                          <DeleteIcon
                            :class="
                              userStore.isCanEdit
                                ? ' hover:fill-rose-400'
                                : ' hover:fill-rose-300'
                            "
                          />
                        </div>
                      </div>
                    </div>
                  </td>
                </tr>

                <div
                  v-if="showErrorMSG"
                  class="flex h-[100%] items-center w-full"
                >
                  <div
                    class="flex items-center justify-center max-lg:border-l-4 h-full w-full"
                  >
                    <span class="text-lg text-slate-700 opacity-50">
                      Missing Load Resource
                    </span>
                  </div>
                </div>

                <div
                  v-else-if="boardStore.collabs.length == 0 && !showLoading"
                  class="flex h-[100%] items-center"
                >
                  <span class="text-lg text-slate-700 opacity-50">
                    No Collaborator
                  </span>
                </div>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <collabDetail 
      v-if="isShowAddCollab"
      :errorMSG="errorMSG"
      @user-action="isShowAddCollab=false"
      @addEdit="addCollaborator"
      />

      <ConfirmModal
      v-if="showConfirmModal"
      :index="usernameId"
      @user-action="changeAccessOrRemoveCollab"
      :width="'w-[60vh]'"
      class="itbkk-modal-alert z-50"
    >
      <template #header>
        <div class="flex flex-col justify-items-end	place-items-end cursor-pointer" @click="showSettingModal=false">
             <CloseIcon @click=" showConfirmModal= false" />
            </div>
        <div class="flex justify-center">   
          <span class="text-gray-800 font-bold text-[1.5rem]">
           {{ isChangeAccess ? 'Change Access Right' : 'Remove Collaborater' }} 
          </span>
        </div>
      </template>
      <template #body>
        <span class="itbkk-message">
          {{ isChangeAccess ? `Do you want to change access right of "${ username }" to "${ accessSelect }"` : `Do you want to remove "${  username }" from the board?` }}  
        </span>
      </template>
    </ConfirmModal>


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
    <AuthzPopup v-if="showPopUp" class="z-50" />
    </div>
  </div>
</template>

<style scoped></style>
