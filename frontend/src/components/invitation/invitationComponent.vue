<script setup>
import { onMounted,ref } from 'vue';
import { useUserStore } from '../../stores/user.js';
import { useBoardStore } from '../../stores/boards.js';
import { useRoute, useRouter } from 'vue-router';
import LoginPopUp from '../modal/PopUp.vue';
import { getAllBoards, responseInvite } from '../../lib/fetchUtill.js';
import AuthzPopup from "../AuthzPopup.vue";

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
  if(userStore.authToken === null) {
    showLoginPopup.value = true;
  }
  else{
    const res = await getAllBoards();
    if(typeof res === "object"){
      console.log(res);
      
      boardStore.setAllBoard(res);
      const collaborator = res.invited.collaborators.find(collab => collab.oid === userStore.authToken.oid);
      if(collaborator.isPending === true){
        message.value = `${res.invited.owner.name} has invited you to collaborate with ${collaborator.accessRight} access right on ${res.invited.name} board`;
      }
      else{
        message.value = "Sorry, we couldn't find your active invitation to this board.";
      }
    }

    else{
      message.value = "Sorry, we couldn't find your active invitation to this board.";
    }
   
  }
  
});

async function acceptInvitation(){
  const res = await responseInvite(boardId.value, "accept");
  if(res === 200){
    router.push({ name: "task", params: { boardId: boardId.value } });
  }
  else{
      handleResponseError(res);
   
  }
}

async function declineInvitation(){
  const res = await responseInvite(boardId.value, "decline");
  if(res === 200){
    router.push({ name: "board" });
  }
  else{
    handleResponseError(res);
  }
}

</script>

<template>
 <div class="flex flex-col justify-center items-center mt-20">
  <h1 class="text-7xl text-green-700">
    Invitation
  </h1>
  <br />
    <p
      class="itbkk-message text-2xl text-gray-500"
    >
      {{ message }}
    </p>

    <div v-if ="message !== `Sorry, we couldn't find your active invitation to this board.` && message !== `` "
    class="flex flex-row gap-10">


  <button
  @click="acceptInvitation"
    class="mt-4 bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded"
  >
    Accept Invitation
  </button>



  <button
  @click="declineInvitation"
    class="mt-4 bg-red-500 hover:bg-red-800 text-white font-bold py-2 px-4 rounded"
  >
    Decline
  </button>

  </div> 
</div>

<LoginPopUp v-if="showLoginPopup === true">
  <template #header>
    <p class="text-lg text-gray-700">
    Login Required
  </p>
  </template>
    <template #message>
      <p class="text-lg text-gray-700">
       You need to Sign in to continue
      </p>
    </template>
    <template #button>
      <router-link :to="{ name: 'Login' }">
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

<style scoped></style>
