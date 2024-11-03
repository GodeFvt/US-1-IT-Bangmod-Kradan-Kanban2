<script setup>
import { onMounted,ref } from 'vue';
import { useUserStore } from '../../stores/user.js';
import { useBoardStore } from '../../stores/boards.js';
import { useRoute, useRouter } from 'vue-router';
import LoginPopUp from '../modal/PopUp.vue';
import { getBoardsById,getCollabs, responseInvite } from '../../lib/fetchUtill.js';
import AuthzPopup from "../AuthzPopup.vue";

const route = useRoute();
const router = useRouter();
const boardId = ref(route.params.boardId);
const userStore = useUserStore();
const showLoginPopup = ref(false);
const message = ref("");
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


// check ว่า boardId ที่ส่งมามีไหม
async function CheckBoardIsValid(){
 const resBoard = await getBoardsById(boardId.value);
  if(resBoard === 404 || resBoard === 400 || resBoard === 500 ){
    router.push({ name: "TaskNotFound", params: { page: "Board" } });
  }
  else{
  boardStore.setCurrentBoard(resBoard);
  const resCollab = await getCollabs(boardId.value);
  if(resCollab.length >= 0){
    const collabFound = resCollab.find(collab => collab.oid === userStore.authToken.oid);
    if(collabFound === undefined){
      message.value = "Sorry, we couldn't find the invitation to this board";
    }
    else{
      message.value = `You have been invited to ${resBoard.name} board`;
      }
  }
  else{
    handleResponseError(resCollab);
  }
  }
}


onMounted(async () => {
  if(userStore.authToken === null) {
    showLoginPopup.value = true;
  }
  else{
  await CheckBoardIsValid();
  }
  
});

async function acceptInvitation(){
  const res = await responseInvite();
  if(res === 200){
    router.push({ name: "board", params: { boardId: boardId.value } });
  }
  else{
    handleResponseError(res);
  }
}

function declineInvitation(){
  console.log("acceptInvitation");
}

</script>

<template>
 <div class="flex flex-col justify-center items-center mt-20">
  <h1 class="text-7xl text-green-700">
    Invitation
  </h1>
  <br />
    <p
      class="itbkk-message text-5xl text-gray-500"
    >
      {{ message }}
    </p>

    <div v-if ="message !== `Sorry, we couldn't find the invitation to this board` "
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
