<script setup>
import TaskTableLoading from "../components/loading/TaskTableLoading.vue";
import { useUserStore } from "../stores/user.js";
import { useRoute, useRouter } from "vue-router";
import collabDetail from "../components/collab/collabDetail.vue";
import { onMounted, ref, watch } from "vue";
import DeleteIcon from "../components/icon/DeleteIcon.vue";
import ConfirmModal from "../components/modal/ConfirmModal.vue";
import CloseIcon from "../components/icon/CloseIcon.vue";
import {
  addCollabs,
  getCollabs,
  deleteCollabs,
  updateCollabs,
  getAllBoards,
} from "../lib/fetchUtill";
import { isTokenValid } from "../lib/utill";
import Toast from "../components/modal/Toasts.vue";
import { useBoardStore } from "../stores/boards.js";
import AuthzPopup from "../components/AuthzPopup.vue";
import CollabBoard from "@/components/collab/collabBoard.vue";

const userStore = useUserStore();
const boardStore = useBoardStore();
const showErrorMSG = ref(false);
const oldAccess = ref("");
const showToast = ref(false);
const typeToast = ref("");
const messageToast = ref("");
const router = useRouter();
const route = useRoute();
const boardId = ref(route.params.boardId);
const isShowAddCollab = ref(false);
const showConfirmModal = ref(false);
const isChangeAccess = ref(false);
const showPopUp = ref(false);
const showLoading = ref(true);
const chooseCollab = ref({});
const errorMSG = ref("");
const usernameId = ref(-1); // ตั้งค่าเริ่มต้นเป็น null


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

onMounted(async () => {
  if (!(await isTokenValid(userStore.encodeToken))) {
    if (boardStore.visibilityPublic === false) {
      showPopUp.value = true;
      return;
    }
  }
  
  if (userStore.authToken !== null) {
    
    
    if (boardStore.boards.length === 0) {
      const resBoard = await getAllBoards();
      if (resBoard === 401 || resBoard === 404 || resBoard === 403) {
        showErrorMSG.value = true;
        handleResponseError(resBoard);
      } else {
        boardStore.setAllBoard(resBoard);
      }
    }
  }
  const res = await getCollabs(boardId.value);
  if (typeof res === "object") {
    boardStore.setCollabs(res);
    
  } else {
    handleResponseError(res);
  }
  showLoading.value = false;
});


function removeCollabByEmail(collab) {
  const collabIndex = boardStore.collabs.findIndex((collaborator) => {
    return collaborator.email === collab.email && !collaborator.oid;
  });
  if (collabIndex !== -1) {
    boardStore.removeCollab(collabIndex);
  }
}

async function addCollaborator(collab) {
  if (!(await isTokenValid(userStore.encodeToken))) {
    showPopUp.value = true;
    return;
  }

  if (collab.email === null || collab.email === "") {
    typeToast.value = "warning";
    messageToast.value = `Must enter the email.`;
    showToast.value = true;
  } else {
    collab.email = collab.email.trim();
    collab.accessRight = collab.accessRight?.toUpperCase();
    const graphApiToken = localStorage.getItem("graphAPI_token");

    if(userStore.isMicroSoftLogin === 'MS'){
      collab.accessToken = graphApiToken;
    }  

let collabIndex = boardStore.collabs.findIndex((collaborator) => {
        return collaborator.email === collab.email && collaborator.oid;
      });   
    if(collabIndex !== -1){
      errorMSG.value = `The user "${collab.email}" is already the collaborator of this board.`;
      return;
    }

    boardStore.addCollab(collab);
    isShowAddCollab.value = false;
    const res = await addCollabs(boardId.value, collab);
    if (typeof res === "object") {
      if (res.emailStatus === "Failed to send email") {
        typeToast.value = "warning";
        messageToast.value = `We could not send e-mail to "${collab.email}", he/she can accept the invitation at 
         /board/${boardId.value}/collab/invitations`;
      } else {
        typeToast.value = "success";
        messageToast.value = `Collaborator "${  res.email}" added successfully.`;
      }
       collabIndex = boardStore.collabs.findIndex((collaborator) => {
        return collaborator.email === collab.email && !collaborator.oid;
      });
      boardStore.updateCollabs(collabIndex, res);
      showToast.value = true;

    } 
    else if (res === 404) {
      //The user "${collab.email}" does not exists. ที่ addModal
      typeToast.value = "danger";
      messageToast.value = `The user "${collab.email}" does not exists. or you need to Login as MS account.`;
      showToast.value = true;
      removeCollabByEmail(collab)
    } else if (res === 401) {
      handleResponseError(res);
      removeCollabByEmail(collab)
    } else if (res === 403) {
      typeToast.value = "danger";
      messageToast.value = `You do not have permission to add collaborator.`;
      showToast.value = true;
      removeCollabByEmail(collab)
    } else if (res === 409) {
      // The user "${collab.email}" is already the collaborator of this board. ที่ addModal
      typeToast.value = "danger";
      messageToast.value = `The user "${collab.email}" is already the collaborator or pending collaborator of this board.`;
      showToast.value = true;
      removeCollabByEmail(collab)
    } else {
      typeToast.value = "warning";
      messageToast.value = `There is a problem please try again later.`;
      showToast.value = true;
      removeCollabByEmail(collab)
    }
  }
}

async function changeAccessOrRemoveCollab(index,changeAccess,collab,previousAccess = 'READ') {
  if (!(await isTokenValid(userStore.encodeToken))) {
    showPopUp.value = true;
    return;
  }
  oldAccess.value = previousAccess;
  usernameId.value = index;
  isChangeAccess.value = changeAccess;
  chooseCollab.value = collab;
  boardStore.currentBoard.owner.id ===userStore.authToken?.oid? (showConfirmModal.value = true) : (showConfirmModal.value = false);
  
}

async function confirmChangeOrRemove(confirmValue = false) {
    if (isChangeAccess.value) {
    //แก้ไข access right ของ user
    let collab = boardStore.collabs[usernameId.value];
    if (confirmValue) {
      const res = await updateCollabs(boardId.value, collab);
      if (typeof res === "object") {
        typeToast.value = "success";
        messageToast.value = `Access right of "${collab.name}" changed to "${collab.accessRight}" successfully.`;
        boardStore.updateAccessCollab(usernameId.value, res.accessRight);
      } else if (res === 404) {
        typeToast.value = "danger";
        messageToast.value = `The user "${collab.name}" is not the collaborator of this board.`;
        boardStore.removeCollab(usernameId.value); //ลบ user ออกจาก boardถ้าไม่กดแล้วไม่เจอ เช่นโดนลบออกจาก board โดยเจ้าของ ณ ตอนนั้น
      } else if (res === 401) {
        handleResponseError(res);
      } else if (res === 403) {
        typeToast.value = "danger";
        messageToast.value = `You do not have permission to change collaborator access right.`;
        collab.accessRight = oldAccess.value;
      } else {
        typeToast.value = "warning";
        messageToast.value = `There is a problem please try again.`;
      }
      showToast.value = true;
    } else {
      //ถ้ากด cancle ให้ทำให้  accessSelect เป็นค่าเดิมของ user นั้นๆ

      collab.accessRight = oldAccess.value;
    }
  } else {
    //ลบ user ออกจาก board
    if (confirmValue) {
      let collabOid = boardStore.collabs[usernameId.value].oid;
      const res = await deleteCollabs(boardId.value, collabOid);
      if (res === 200) {
        typeToast.value = "success";
        messageToast.value = `Collaborator "${
          boardStore.collabs[usernameId.value].name
        }" removed successfully.`;
        boardStore.removeCollab(usernameId.value);
      } else if (res === 404) {
        typeToast.value = "danger";
        messageToast.value = `The user "${
          boardStore.collabs[usernameId.value].name
        }" is not the collaborator of this board.`;
      } else if (res === 401 || res === 403) {
        handleResponseError(res);
      } else {
        typeToast.value = "warning";
        messageToast.value = `There is a problem please try again.`;
      }
      showToast.value = true;
    }
  }
  showConfirmModal.value = false;
}
</script>

<template>
  <CollabBoard
  :showErrorMSG="showErrorMSG"
  :showLoading="showLoading"
  @removeCollab="changeAccessOrRemoveCollab"
  @addCollab="isShowAddCollab = true, errorMSG = ''"
  @changeAccess="changeAccessOrRemoveCollab"
  />

      <collabDetail
        v-if="isShowAddCollab"
        :errorMSG="errorMSG"
        @user-action="isShowAddCollab = false"
        @addEdit="addCollaborator"
      />

      <ConfirmModal
        v-if="showConfirmModal"
        :index="usernameId"
        @user-action="confirmChangeOrRemove"
        :width="'w-[60vh]'"
        class="itbkk-modal-alert z-50"
      >
        <template #header>
          <div
            class="flex flex-col justify-items-end place-items-end cursor-pointer"
          >
            <CloseIcon @click="confirmChangeOrRemove(false)" />
          </div>
          <div class="flex justify-center">
            <span class="text-gray-800 font-bold text-[1.5rem]">
              {{
                isChangeAccess ? "Change Access Right" : "Remove Collaborater"
              }}
            </span>
          </div>
        </template>
        <template #body>
          <span class="itbkk-message">
            {{
              isChangeAccess
                ? `Do you want to change access right of "${chooseCollab.name}" to "${chooseCollab.accessRight}"?`
                : chooseCollab?.isPending
                ? `Do you want to cancel the invitation to "${chooseCollab.name}"?`
                : `Do you want to remove "${chooseCollab.name}" from the board?`
            }}
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

</template>

<style scoped></style>
