<script setup>
import { ref, onMounted, watch, computed, onUnmounted, nextTick } from "vue";
import {
  getBoardsById,
  deleteBoard,
  createBoard,
  updateBoard,
  getAllBoards,
  deleteCollabs,
} from "../lib/fetchUtill.js";
import { useRoute, useRouter } from "vue-router";
import AddButton from "../components/icon/AddButton.vue";
import { useUserStore } from "../stores/user.js";
import boardCardList from "../components/board/boardCardList.vue";
import boardDetail from "../components/board/boardDetail.vue";
import Toast from "../components/modal/Toasts.vue";
import ConfirmModal from "../components/modal/ConfirmModal.vue";
import AlertSquareIcon from "../components/icon/AlertSquareIcon.vue";
import AuthzPopup from "../components/AuthzPopup.vue";
import { isTokenValid } from "../lib/utill.js";
import { useBoardStore } from "../stores/boards.js";

const userStore = useUserStore();
const boardStore = useBoardStore();
const router = useRouter();
const route = useRoute();
const board = ref({});
const typeToast = ref("");
const messageToast = ref("");
const boardIdForDelete = ref("");

//show component
const showPopUp = ref(false);
const showBoardModal = ref(false);
const showLoading = ref(false);
const isEdit = ref(false);
const showToast = ref(false);
const showDeleteModal = ref(false);
const showLeaveModal = ref(false);

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
    showPopUp.value = true;
    return;
  } else {
    showLoading.value = true;
    const resBoard = await getAllBoards();
    showLoading.value = false;
    if (resBoard === 401 || resBoard === 404) {
      handleResponseError(resBoard);
    } else {
      boardStore.setAllBoard(resBoard);
    }
  }
});

const collabBoard = computed(() => {
  if (!userStore.authToken) return []; // ถ้า authToken เป็น null ให้คืนค่าเป็น array ว่าง เกิดปัญหา logout แล้วหาauthToken ไม่ได้
  return boardStore.boards.filter(
    (board) => board.owner.id !== userStore.authToken.oid
  );
});

const personalBoard = computed(() => {
  if (!userStore.authToken) return [];
  return boardStore.boards.filter(
    (board) => board.owner.id === userStore.authToken.oid
  );
});

watch(
  () => route.path,
  (newPath, oldPath) => {
    if (newPath === "/board/add") {
      ClickAdd();
    }
  },
  { immediate: true }
);

watch(
  () => route.params.boardId,
  async (newId, oldId) => {
    if (newId !== undefined) {
      if (!(await isTokenValid(userStore.encodeToken))) {
        showPopUp.value = true;
        return;
      } else {
        const res = await getBoardsById(newId);
        if (typeof res !== "object") {
          handleResponseError(res);
        } else {
          if (route.path === `/board/${newId}/edit`) {
            board.value = boardStore.boards.find((board) => board.id === newId);

            showBoardModal.value = true;
            isEdit.value = true;
          } else {
            isEdit.value = false;
          }
        }
      }
    }
    // showLoading.value = false;
  },
  { immediate: true }
);

function ClickAdd() {
  // showLoading.value = false;
  isEdit.value = true;
  showBoardModal.value = true;
  board.value = {
    name: "",
    description: "",
  };
}

async function addEditBoard(newBoard) {
  const indexToCheck = boardStore.boards.findIndex(
    (board) => board.id === newBoard.id
  );
  if (indexToCheck !== -1 && indexToCheck !== undefined) {
    await editBoard(board.value.id, newBoard);
  } else {
    await addBoard(newBoard);
  }
}

async function addBoard(newBoard) {
  if (!(await isTokenValid(userStore.encodeToken))) {
    showPopUp.value = true;
    return;
  } else {
    if (newBoard.name === null || newBoard.name === "") {
      typeToast.value = "warning";
      messageToast.value = `The name is required`;
      showToast.value = true;
    } else {
      newBoard.name = newBoard.name.trim();
      newBoard.description = newBoard.description?.trim();
      const res = await createBoard(newBoard);
      if (res === 422 || res === 400 || res === 500 || res === 404) {
        typeToast.value = "warning";
        messageToast.value = `There is problem.Please try again later.`;
      } else if (res === 401) {
        handleResponseError(res);
      } else {
        // if res.status = 200
        typeToast.value = "success";
        boardStore.addBoard(res);
        messageToast.value = `The board has been added`;
      }
      showToast.value = true;
    }
  }
}

async function editBoard(boardId, editedBoard) {
  if (!(await isTokenValid(userStore.encodeToken))) {
    showPopUp.value = true;
    return;
  } else {
    const res = await updateBoard(boardId, editedBoard);
    if (res === 422 || res === 400 || res === 500 || res === 404) {
      typeToast.value = "warning";
      messageToast.value = `An error has occurred, the board does not exist`;
    } else if (res === 401) {
      handleResponseError(res);
    } else {
      typeToast.value = "success";
      const indexToUpdate = boardStore.boards.findIndex(
        (board) => board.id === editedBoard.id
      );
      boardStore.editBoard(indexToUpdate, res);
      messageToast.value = `The board has been updated`;
    }
    showToast.value = true;
  }
}

function closeBoard(action) {
  showBoardModal.value = action;
  router.push({ name: "board" });
}

async function removeBoard(boardId, confirmDelete = false) {
  if (!(await isTokenValid(userStore.encodeToken))) {
    showPopUp.value = true;
    return;
  } else {
    showDeleteModal.value = true;
    if (typeof boardId === "string") {
      boardIdForDelete.value = boardId;
      board.value = boardStore.boards.find((board) => board.id === boardId);
    }
    if (confirmDelete) {
      const res = await deleteBoard(boardIdForDelete.value);
      if (res === 422 || res === 400 || res === 500 || res === 404) {
        typeToast.value = "warning";
        messageToast.value = `An error has occurred, the board could not be added`;
      } else if (res === 401) {
        handleResponseError(res);
      } else {
        // if res.status = 200
        typeToast.value = "success";
        boardStore.deleteBoard(boardId);
        messageToast.value = `The board has been deleted`;
      }
      showDeleteModal.value = false;
      showToast.value = true;
    }
  }
}

async function leaveBoard(boardId, confirmLeave = false) {
  if (!(await isTokenValid(userStore.encodeToken))) {
    showPopUp.value = true;
    return;
  }
  showLeaveModal.value = true;
  if (typeof boardId === "string") {
    boardIdForDelete.value = boardId;
    board.value = boardStore.boards.find((board) => board.id === boardId);
  }
  if (confirmLeave === true) {
    const res = await deleteCollabs(
      boardIdForDelete.value,
      userStore.authToken.oid
    );
    if (res === 200) {
      typeToast.value = "success";
      boardStore.deleteBoard(boardId);
      messageToast.value = `You have left the board`;
    } else if (res === 401) {
      handleResponseError(res);
    } else {
      typeToast.value = "warning";
      messageToast.value = `An error has occurred, the board could not be left`;
    }
    showLeaveModal.value = false;
    showToast.value = true;
  }
}

function openBoard(boardId) {
  router.push({ name: "task", params: { boardId: boardId } });
}

function invitation(boardId) {
  router.push({ name: "Invitations", params: { boardId: boardId } });
}
</script>
<template>
  <div class="flex flex-col w-full h-screen">
    <div class="flex flex-col items-center h-full mt-2">
      <div
        class="itbkk-button-home flex flex-row w-[95%] mt-5 max-sm:w-full max-sm:px-2 border-b border-gray-300"
      >
        <h2
          class="text-[1.5rem] m-[2px] my-2 text-2xl max-md:text-xl max-sm:text-sm font-bold"
        >
          Your Boards
        </h2>
      </div>
      <div class="flex flex-col items-center h-full w-full overflow-auto">
        <div
          class="flex flex-col justify-center mt-4 gap-3 w-[95%] max-sm:w-full max-sm:px-2 max-sm:gap-1"
        >
          <h2 class="itbkk-personal-board font-bold text-2xl mb-6">
            Personal Board
          </h2>
          <div class="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            <router-link :to="{ name: 'AddBoard' }">
              <div
                class="itbkk-button-create cursor-pointer rounded-lg border-2 border-dashed border-gray-300 bg-gray-100 text-card-foreground shadow-sm transition-shadow hover:shadow-md p-6 flex flex-col items-center justify-center h-full relative"
              >
                <AddButton />
                <h3 class="text-lg font-semibold mb-2 text-gray-400">
                  Create New Board
                </h3>
              </div>
            </router-link>
            <!-- board card list -->
            <boardCardList
              :allBoard="personalBoard"
              :showLoading="showLoading"
              @removeBoard="removeBoard"
              @openBoard="openBoard"
            >
            </boardCardList>
          </div>

          <!-- Collab Board -->
          <div class="mb-12"></div>
          <h2 class="itbkk-collab-board font-bold text-2xl mb-6">
            Collab Board
          </h2>
          <div class="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            <boardCardList
              :allBoard="collabBoard"
              boardType="collab"
              :showLoading="showLoading"
              @leaveBoard="leaveBoard"
              @openBoard="openBoard"
              @invitation="invitation"
            >
            </boardCardList>
          </div>
        </div>
      </div>
    </div>
    <div
      v-if="showToast"
      class="fixed flex items-center w-full max-w-xs right-5 bottom-5"
    >
      <Toast :toast="typeToast" @close-toast="showToast = false">
        <template #message>
          <span class="itbkk-message break-all">{{ messageToast }}</span>
        </template>
      </Toast>
    </div>
  </div>

  <boardDetail
    v-if="showBoardModal"
    class="itbkk-modal-new"
    @user-action="closeBoard"
    @addEdit="addEditBoard"
    :board="board"
    :isEdit="isEdit"
    :username="userStore.authToken?.name"
  >
  </boardDetail>

  <ConfirmModal
    v-if="showDeleteModal"
    @user-action="showDeleteModal = false"
    @confirm="removeBoard"
    :index="
      boardStore.boards.findIndex((board) => board.id === boardIdForDelete)
    "
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
        Do you want to delete this "<span class="font-semibold">{{
          board.name
        }}</span
        >"
      </span>
    </template>
  </ConfirmModal>

  <ConfirmModal
    v-if="showLeaveModal"
    @user-action="showLeaveModal = false"
    @confirm="leaveBoard"
    :index="
      boardStore.boards.findIndex((board) => board.id === boardIdForDelete)
    "
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
        Do you want to leave this "<span class="font-semibold">{{
          board.name
        }}</span
        >"
      </span>
    </template>
  </ConfirmModal>

  <AuthzPopup v-if="showPopUp" />
</template>

<style scoped>
.tracking-in-expand {
  -webkit-animation: tracking-in-expand 0.7s cubic-bezier(0.215, 0.61, 0.355, 1)
    both;
  animation: tracking-in-expand 0.7s cubic-bezier(0.215, 0.61, 0.355, 1) both;
}
.slide-right {
  -webkit-animation: slide-right 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94) both;
  animation: slide-right 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94) both;
}
@-webkit-keyframes slide-right {
  0% {
    -webkit-transform: translateX(-100px);
    transform: translateX(-100px);
  }
  100% {
    -webkit-transform: translateX(0);
    transform: translateX(0);
  }
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
@-webkit-keyframes tracking-in-expand {
  0% {
    letter-spacing: -0.5em;
    opacity: 0;
  }
  40% {
    opacity: 0.6;
  }
  100% {
    opacity: 1;
  }
}
@keyframes tracking-in-expand {
  0% {
    letter-spacing: -0.5em;
    opacity: 0;
  }
  40% {
    opacity: 0.6;
  }
  100% {
    opacity: 1;
  }
}
</style>
