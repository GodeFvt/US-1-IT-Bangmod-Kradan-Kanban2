<script setup>
import { ref, computed,onMounted } from "vue";
import {
  getAllBoards,
} from "../lib/fetchUtill.js";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "../stores/user.js";
import AuthzPopup from '../components/AuthzPopup.vue';
import { isTokenValid } from "../lib/utill.js";

const userStore = useUserStore();
const route = useRoute();
const router = useRouter();
const showPopUp =ref(false);
const countBoard = computed(() => {
  return userStore.boards.length;
})

function signOut(){
    userStore.clearAuthToken()
    userStore.updateIsAuthen(false)
  router.push({ name: "Login"});
}
onMounted(async () => {
   userStore.initializeAuthToken();
  if (!isTokenValid(userStore.authToken)) {
        showPopUp.value = true;
        return;
      } else {
  if(userStore.boards.length ===0){
     const resBoard = await getAllBoards();
     if(resBoard === 401){
        showPopUp.value = true
      }
     else {
       userStore.setAllBoard(resBoard);
      }
    }
    }
  
});


</script>

<template>
  <div
    class="flex h-screen flex-col justify-between border-e w-[17rem] bg-gray-800 border-r border-gray-500"
  >
    <div class="px-4 h-full">
      <div class="flex h-[9%] items-center py-3 border-b border-gray-100">
        <div class="flex items-center mx-auto h-full justify-center">
          <span
            class="text-white text-3xl font-semibold whitespace-nowrap slide-right"
            style="animation-delay: 0s"
          >
            US-1 Kanban
          </span>
        </div>
      </div>
      <ul class="mt-6 space-y-1">
        <router-link :to="{ name: 'board' }">
        <li>
          <!-- bg-gray เวลา route อยู่หน้า board -->
          <div
          :class="{
            'bg-gray-100 text-gray-700': route.name === 'board',
            'text-white hover:bg-gray-100 hover:text-gray-700': route.name !== 'board'
          }"
          class="block rounded-lg px-4 py-2 text-sm font-medium"
        >
          
            General
          </div>
        </li>
          </router-link>
        <li>
          <details
            class="slide-right group [&_summary::-webkit-details-marker]:hidden"
            style="animation-delay: 0.2s"
          >
          <summary
          :class="{
            'bg-gray-200 text-gray-700': route.name === 'task',
            'text-white hover:bg-gray-100 hover:text-gray-700': route.name !== 'task'
          }"
          class="flex cursor-pointer items-center justify-between rounded-lg px-4 py-2 text-sm font-medium"
        >
              <span class="text-sm font-medium"> All Boards ({{countBoard}})</span>

              <span
                class="shrink-0 transition duration-300 group-open:-rotate-180"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  class="size-5"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                >
                  <path
                    fill-rule="evenodd"
                    d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
                    clip-rule="evenodd"
                  />
                </svg>
              </span>
            </summary>

            <ul class="mt-2 space-y-1 px-4">
              <li v-for="board in userStore.boards" :key="board.id">
                <router-link  :to="{ name: 'task', params: { boardId: board.id } }" 
                :class="{
                'bg-gray-100 text-gray-700': route.params.boardId === board.id,
                'text-white hover:bg-gray-100 hover:text-gray-700': route.params.boardId !== board.id
              }"
              class="block rounded-lg px-4 py-2 text-sm font-medium"
            >
                {{ board.name }}
               </router-link>
              </li>
            </ul>
          </details>
        </li>

        <li>
          <details
            class="slide-right group [&_summary::-webkit-details-marker]:hidden"
            style="animation-delay: 0.6s"
          >
            <summary
              class="flex cursor-pointer items-center justify-between rounded-lg px-4 py-2 text-white hover:bg-gray-100 hover:text-gray-700"
            >
              <span class="text-sm font-medium"> Account </span>

              <span
                class="shrink-0 transition duration-300 group-open:-rotate-180"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  class="size-5"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                >
                  <path
                    fill-rule="evenodd"
                    d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
                    clip-rule="evenodd"
                  />
                </svg>
              </span>
            </summary>

            <ul class="mt-2 space-y-1 px-4">
              <li>
                <a
                  href="#"
                  class="block rounded-lg px-4 py-2 text-sm font-medium text-white hover:bg-gray-100 hover:text-gray-700"
                >
                  Details
                </a>
              </li>

              <li>
                <a
                  href="#"
                  class="block rounded-lg px-4 py-2 text-sm font-medium text-white hover:bg-gray-100 hover:text-gray-700"
                >
                  Security
                </a>
              </li>

              <li>
                <form action="#">
                  <button
                    type="submit"
                    class="flex items-center w-full rounded-lg px-4 py-2 text-sm font-medium [text-align:_inherit] hover:bg-gray-100 hover:text-gray-700 text-red-500"
                  >
                    <span @click="signOut" class="cursor-pointer w-full">
                      Logout</span
                    >
                    <span @click="signOut" class="cursor-pointer w-[5%]">
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="14"
                        height="14"
                        viewBox="0 0 20 20"
                      >
                        <path
                          fill="currentColor"
                          d="m19 10l-6-5v3H6v4h7v3zM3 3h8V1H3c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H3z"
                        />
                      </svg>
                    </span>
                  </button>
                </form>
              </li>
            </ul>
          </details>
        </li>
      </ul>
    </div>

    <div
      class="flex justify-center items-center sticky inset-x-0 bottom-0 border-t border-gray-100 text-white"
    >
      <a
        href="#"
        class="flex items-center gap-2 bg-gray-800 p-4 hover:bg-gray-700 w-full h-full"
      >
        <img
          alt=""
          src="https://minotar.net/helm/sorrapong2521/64"
          class="size-10 rounded-full object-cover"
        />

        <div>
          <p class="text-xs">
            <strong class="itbkk-fullname block font-medium">{{ userStore.authToken?.name || 'Anonymous' }}</strong>

            <span> {{ userStore.authToken?.email || '' }} </span>
          </p>
        </div>
      </a>
      <div
        class="w-[15%] flex justify-center items-center bg-gray-800 p-4 hover:bg-gray-700 h-full"
      >
        <span @click="signOut" class="cursor-pointer">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="14"
            height="14"
            viewBox="0 0 20 20"
          >
            <path
              fill="currentColor"
              d="m19 10l-6-5v3H6v4h7v3zM3 3h8V1H3c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H3z"
            />
          </svg>
        </span>
      </div>
    </div>
  </div>
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
