<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { getAllBoards } from "../lib/fetchUtill.js";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "../stores/user.js";
import { useBoardStore } from "../stores/boards.js";
import AuthzPopup from "../components/AuthzPopup.vue";
import { isTokenValid } from "../lib/utill.js";
import Themes from "../components/icon/Themes.vue";
import {
  HomeIcon,
  AccountSettingIcon,
  MoreActionIconD,
  QuillIcon,
  SharpMenuIcon,
  SharpSortIcon,
  BoardIcon,
} from "../components/icon";
import { msalInstance, state } from "../config/msalConfig.js";
import { msalService } from "../config/useAuth.js";
import ConfirmModal from "../components/modal/ConfirmModal.vue";
const { handleRedirect, logout } = msalService();

const userStore = useUserStore();
const boardStore = useBoardStore();
const route = useRoute();
const router = useRouter();
const showPopUp = ref(false);
const countBoard = computed(() => {
  return boardStore.boards.length;
});

const handleLogout = async () => {
  if (userStore.isMicroSoftLogin === "MS") {
    userStore.isMicroSoftLogin = "Guest";
    logout();
  }
  userStore.clearAuthToken();
  router.push({ name: "Login" });
};

const initialize = async () => {
  try {
    await msalInstance.initialize();
  } catch (error) {
    console.log("Initialization error", error);
  }
};

onMounted(async () => {
  await initialize();
});

const open = ref(true);
const showChangeThemes = ref(false);
const themeSelect = ref("");
const updateSidebarState = () => {
  const screenWidth = window.innerWidth;
  open.value = screenWidth >= 1000;
};

onMounted(() => {
  updateSidebarState();
  window.addEventListener("resize", updateSidebarState);
});

onUnmounted(() => {
  window.removeEventListener("resize", updateSidebarState);
});

function openChageThemes() {
  showChangeThemes.value = true;
}
function changeTheme(useraction) {
  if (useraction) {
    localStorage.setItem("theme", themeSelect.value);
    userStore.setTheme(themeSelect.value);
  }
  showChangeThemes.value = false;
}
</script>

<template>
  <div
    class="flex relative h-screen flex-col justify-between border-e bg-gray-800 border-r border-gray-500 duration-500"
    :class="{ 'w-[17rem]': open, 'w-[4rem]': !open }"
  >
    <div class="px-4 h-full">
      <div class="flex h-[9%] items-center py-3 border-b border-gray-100">
        <div class="flex h-full w-full justify-between items-center">
          <transition name="text-fade">
            <div
              class="text-white text-3xl font-semibold whitespace-nowrap"
              v-if="open"
            >
              US-1 Kanban
            </div>
          </transition>
          <transition name="fade">
            <div
              class="h-full items-center flex"
              v-if="open"
              @click="open = !open"
            >
              <SharpSortIcon
                class="text-white transform scale-x-[-1] cursor-pointer"
              />
            </div>
          </transition>

          <transition name="fade">
            <div
              class="h-full items-center flex"
              v-if="!open"
              @click="open = !open"
            >
              <SharpMenuIcon class="text-white cursor-pointer" />
            </div>
          </transition>
        </div>
      </div>
      <ul class="mt-6 space-y-1">
        <router-link :to="{ name: 'board' }">
          <li>
            <transition name="fade">
              <div
                :class="{
                  'bg-gray-100 text-gray-700': route.name === 'board',
                  'text-white hover:bg-gray-100 hover:text-gray-700':
                    route.name !== 'board',
                  'px-4 py-2': open,
                }"
                class="flex items-center rounded-lg px-1 py-2 text-sm font-medium"
              >
                <HomeIcon
                  class="size-5 duration-500 transition-all"
                  :class="{ 'mr-2 mb-1': open, 'ml-[0.1rem]': !open }"
                />
                <transition name="text-fade">
                  <div v-if="open">General</div>
                </transition>
              </div>
            </transition>
          </li>
        </router-link>
        <li>
          <details
            class="slide-right group [&_summary::-webkit-details-marker]:hidden"
            style="animation-delay: 0.2s"
            @click="open = true"
          >
            <transition name="fade">
              <summary
                :class="{
                  'bg-gray-200 text-gray-700': route.name === 'task',
                  'text-white hover:bg-gray-100 hover:text-gray-700':
                    route.name !== 'task',
                  'px-4 py-2': open,
                }"
                class="flex cursor-pointer items-center justify-between rounded-lg px-1 py-2 text-sm font-medium"
              >
                <div class="flex items-center">
                  <BoardIcon
                    class="size-5 duration-500 transition-all"
                    :class="{ 'mr-2 mb-1': open, 'ml-[0.1rem]': !open }"
                  />
                  <transition name="text-fade">
                    <span v-if="open"> All Boards ({{ countBoard }}) </span>
                  </transition>
                </div>
                <transition name="text-fade">
                  <span
                    v-if="open"
                    class="shrink-0 transition duration-300 group-open:-rotate-180"
                  >
                    <MoreActionIconD />
                  </span>
                </transition>
              </summary>
            </transition>
            <ul class="mt-2 space-y-1 px-4" v-if="open">
              <li v-for="board in boardStore.boards" :key="board.id">
                <router-link
                  :to="{ name: 'task', params: { boardId: board.id } }"
                  :class="{
                    'bg-gray-100 text-gray-700':
                      route.params.boardId === board.id,
                    'text-white hover:bg-gray-100 hover:text-gray-700':
                      route.params.boardId !== board.id,
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
            style="animation-delay: 0.2s"
            @click="open = true"
          >
            <transition name="fade">
              <summary
                :class="{
                  'px-4 py-2': open,
                }"
                @click="openChageThemes"
                class="flex cursor-pointer items-center text-white hover:bg-gray-100 fill-slate-50 justify-between rounded-lg px-1 py-2 text-sm font-medium hover:text-gray-700 hover:fill-gray-700"
              >
                <div class="flex items-center">
                  <Themes
                    class="size-5 duration-500 transition-all"
                    :class="{
                      'mr-2 mb-1 ': open,
                      'ml-[0.1rem] ': !open,
                    }"
                  />
                  <transition name="text-fade">
                    <span v-if="open">Themes</span>
                  </transition>
                </div>
              </summary>
            </transition>
          </details>
        </li>

        <li>
          <details
            class="slide-right group [&_summary::-webkit-details-marker]:hidden"
            style="animation-delay: 0.6s"
            @click="open = true"
          >
            <transition name="fade">
              <summary
                class="flex cursor-pointer items-center justify-between rounded-lg px-1 py-2 text-white hover:bg-gray-100 hover:text-gray-700"
                :class="{
                  'px-4 py-2': open,
                }"
              >
                <div class="flex items-center">
                  <AccountSettingIcon
                    class="size-5 duration-500 transition-all"
                    :class="{ 'mr-2 mb-1': open, 'ml-[0.1rem]': !open }"
                  />
                  <transition name="text-fade">
                    <span v-if="open" class="text-sm font-medium">
                      Account
                    </span>
                  </transition>
                </div>
                <span
                  v-if="open"
                  class="shrink-0 transition duration-300 group-open:-rotate-180"
                >
                  <MoreActionIconD />
                </span>
              </summary>
            </transition>
            <transition name="fade">
              <ul class="mt-2 space-y-1 px-4 duration-300" v-if="open">
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
                      <span @click="handleLogout" class="cursor-pointer w-full">
                        Logout</span
                      >
                      <span @click="handleLogout" class="cursor-pointer w-[5%]">
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
            </transition>
          </details>
        </li>
      </ul>
    </div>

    <div
      class="flex justify-center items-center sticky inset-x-0 bottom-0 border-t border-gray-100 text-white"
      @click="open = true"
    >
      <transition name="text-fade">
        <a
          href="#"
          class="flex items-center gap-2 bg-gray-800 p-4 hover:bg-gray-700 w-full h-full"
        >
          <div class="user-info flex items-center gap-2">
            <img
              alt="Profile"
              src="https://minotar.net/helm/sorrapong2521/64"
              class="size-10 rounded-full object-cover"
              :class="{ 'size-9': !open }"
            />
            <div v-if="open" class="text-xs max-w-[10rem] truncate">
              <strong class="block font-medium" title="Full name">
                {{ userStore.authToken?.name || "Anonymous" }}
              </strong>
              <span class="block truncate text-gray-400" title="Full email">
                {{
                  userStore.authToken?.email ||
                  userStore.authToken?.preferred_username ||
                  ""
                }}
              </span>
            </div>
          </div>
        </a>
      </transition>
      <transition name="text-fade">
        <div
          @click="handleLogout"
          v-if="open"
          class="itbkk-sign-out w-[15%] flex justify-center items-center bg-gray-800 p-4 hover:bg-gray-700 h-full cursor-pointer"
        >
          <span class="cursor-pointer">
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
          </span></div
      ></transition>
    </div>
  </div>

  <AuthzPopup v-if="showPopUp" />

  <ConfirmModal v-if="showChangeThemes" @user-action="changeTheme" class="z-50">
    <template #header>
      <div class="flex justify-center">
        <h2 class="font-bold">Select your themes TaskList</h2>
      </div>
    </template>
    <template #body>
      <div class="flex justify-center items-center bg-gray-100">
        <div class="bg-gray-100 p-4">
          <label class="flex-col gap-1 flex items-center justify-center">
            <img src="../../public/table.png" alt="table" />

            <input
              type="radio"
              name="option"
              value="table"
              class="h-6 w-6 form-radio text-blue-600"
              v-model="themeSelect"
              :checked="userStore.theme === 'table' ? true : false"
            />
          </label>
        </div>
        <div class="bg-gray-100 p-4">
          <label class="flex-col gap-1 flex items-center justify-center">
            <img src="../../public/card.png" alt="card" />
            <input
              type="radio"
              name="option"
              value="card"
              class="h-6 w-6 form-radio text-gray-300"
              v-model="themeSelect"
              :checked="userStore.theme === 'card' ? true : false"
            />
          </label>
        </div>
      </div>
    </template>
  </ConfirmModal>
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
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.1s ease;
}
.fade-enter,
.fade-leave-to {
  opacity: 0;
}

.text-fade-enter-active,
.text-fade-leave-active {
  transition: opacity 0.2s ease;
}
.text-fade-enter,
.text-fade-leave-to {
  opacity: 0;
}
</style>
