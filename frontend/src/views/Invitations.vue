<script setup>
import { onMounted, ref } from "vue";
import { useUserStore } from "../stores/user.js";
import { useBoardStore } from "../stores/boards.js";
import { useRoute, useRouter } from "vue-router";
import LoginPopUp from "../components/modal/PopUp.vue";
import { getAllBoards, responseInvite } from "../lib/fetchUtill.js";
import AuthzPopup from "../components/AuthzPopup.vue";
import { isTokenValid } from "../lib/utill";

const route = useRoute();
const router = useRouter();
const boardId = ref(route.params.boardId);
const userStore = useUserStore();
const showLoginPopup = ref(false);
const message = ref(``);
const boardStore = useBoardStore();
const showPopUp = ref(false);

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
    showLoginPopup.value = true;
  } else {
    const res = await getAllBoards();
    if (typeof res === "object") {
      boardStore.setAllBoard(res);
      const board = res.invited.find((board) => board.id === boardId.value);
      if (board === undefined) {
        message.value =
          "Sorry, we couldn't find your active invitation to this board.";
        return;
      }
      const collaborator = board.collaborators.find(
        (collab) => collab.oid === userStore.authToken?.oid
      );
      if (collaborator.isPending === true) {
        message.value = `${board.owner.name} has invited you to collaborate with ${collaborator.accessRight} access right on ${board.name} board`;
      }
    } else {
      message.value =
        "Sorry, we couldn't find your active invitation to this board.";
    }
  }
});

async function acceptInvitation() {
  if (!(await isTokenValid(userStore.encodeToken))) {
    showLoginPopup.value = true;
    return;
  }
  const res = await responseInvite(boardId.value, "accept");
  if (res === 200) {
    router.push({ name: "task", params: { boardId: boardId.value } });
  } else {
    handleResponseError(res);
  }
}

async function declineInvitation() {
  if (!(await isTokenValid(userStore.encodeToken))) {
    showLoginPopup.value = true;
    return;
  }
  const res = await responseInvite(boardId.value, "decline");
  if (res === 200) {
    router.push({ name: "board" });
  } else {
    handleResponseError(res);
  }
}
</script>

<template>
  <div class="flex flex-col justify-center items-center mt-20">
    <h1 class="text-7xl text-green-700">Invitation</h1>
    <br />
    <p class="itbkk-message text-2xl text-gray-500">
      {{ message }}
    </p>

    <div
      v-if="
        message !==
          `Sorry, we couldn't find your active invitation to this board.` &&
        message !== ``
      "
      class="flex flex-row gap-10"
    >
      <button
        @click="acceptInvitation"
        class="mt-4 bg-gray-800 text-white px-4 py-2 rounded-lg hover:bg-gray-600 transition font-bold"
      >
        Accept Invitation
      </button>
      <button
        @click="declineInvitation"
        class="mt-4 bg-gray-200 text-gray-700 px-4 py-2 rounded-lg hover:bg-gray-300 transition font-bold"
      >
        Decline
      </button>
    </div>
    <div v-else>
      <router-link :to="{ name: 'board' }">
        <button
          class="mt-4 bg-gray-800 text-white px-4 py-2 rounded-lg hover:bg-gray-600 transition font-bold"
        >
          Go to Board
        </button>
      </router-link>
    </div>
  </div>

  <LoginPopUp v-if="showLoginPopup === true">
    <template #header>
      <p class="text-lg text-gray-700">Login Required</p>
    </template>
    <template #message>
      <p class="text-lg text-gray-700">You need to Sign in to continue</p>
    </template>
    <template #button>
      <router-link
        :to="{ name: 'Login', query: { redirectTo: $route.fullPath } }"
      >
        <button
          class="mt-4 bg-gray-800 hover:bg-gray-500 text-white font-bold py-2 px-4 rounded"
        >
          Sign in to Continue
        </button>
      </router-link>
    </template>
  </LoginPopUp>

  <AuthzPopup v-if="showPopUp" />
</template>
