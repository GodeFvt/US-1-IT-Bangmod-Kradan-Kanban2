<script setup>
import { loginAccount } from "../lib/fetchUtill.js";
import { useUserStore } from "../stores/user.js";
import { ref, computed, onMounted, onBeforeUnmount } from "vue";
import { useRouter, useRoute } from "vue-router";
import SettingIcon from "../components/icon/SettingIcon.vue";
import TaskStatusCard from "../components/status/TaskStatusCard.vue";
import HeaderView from "./HeaderView.vue";
import { getAllBoards } from "../lib/fetchUtill.js";

const toggleIcon = ref(false);
const user = ref({
  userName: "",
  password: "",
});
const userStore = useUserStore();
const showMessage = ref(false);
const router = useRouter();
const cardAnimate = ref("");
const borderAnimate = ref("");
const miniTaskBoardAnimate = ref("");
const kanbanAnimate = ref("");
const textAnimation = ref("");
let intervalId;
let timeoutId;

onMounted(() => {
  animation();

  intervalId = setInterval(() => {
    animation();
  }, 18000);
});

onBeforeUnmount(() => {
  clearInterval(intervalId);
  clearTimeout(timeoutId);
});

function animation() {
  borderAnimate.value = `border-animate`;
  cardAnimate.value = `card-animate`;
  timeoutId = setTimeout(() => {
    miniTaskBoardAnimate.value = `miniTaskBoard-animate`;
  }, 6000);
  timeoutId = setTimeout(() => {
    kanbanAnimate.value = `kanban`;
  }, 7000);
  timeoutId = setTimeout(() => {
    textAnimation.value = `textAnimation`;
  }, 8000);
  timeoutId = setTimeout(() => {
    kanbanAnimate.value = `kanbanToMiniTaskBoard`;
  }, 15000);
  timeoutId = setTimeout(() => {
    miniTaskBoardAnimate.value = `miniTaskBoard-animate-show`;
  }, 17000);
}

const typePassword = computed(() => {
  if (toggleIcon.value) {
    return "text";
  } else {
    return "password";
  }
});
const isNotValid = computed(() => {
  return user.value.userName.length === 0 || user.value.password.length === 0;
});

const messageShow = ref("");
const inert = ref(false);

async function signInOnClick(userLogin) {
  let res;
  if (userLogin.userName.length > 0 && userLogin.password.length > 0) {
    res = await loginAccount(userLogin);
    if (typeof res === "object") {
      inert.value = true;
      localStorage.setItem("refresh_token", res.refresh_token);
      userStore.setAuthToken(res.access_token);
      const resBoard = await getAllBoards();
      userStore.setAllBoard(resBoard);
      if (resBoard.length === 1) {
        router.push({
          name: "task",
          params: { boardId: userStore.boards[0].id },
        });
      } else {
        router.push({ name: "board" });
      }
    } else if (res === 400 || res === 401) {
      showMessage.value = true;
      messageShow.value = "Username or Password is incorrect.";
    } else {
      showMessage.value = true;
      messageShow.value = "There is a problem.Please try again.";
    }
  }
}
</script>

<template>
  <div class="h-screen w-screen">
    <div class="h-[8%] hidden max-lg:block">
      <HeaderView class="h-full w-full" />
    </div>
    <div
      class="h-[100%] max-lg:h-[92%] bg-[url('/bg-img3.jpg')] bg-cover bg-center bg-no-repeat"
    >
      <div class="h-[100%] w-full">
        <div class="h-full flex max-lg:flex w-full">
          <section
            class="flex items-center justify-center max-lg:mx-0 max-lg:my-0 max-lg:grow w-[35%]"
          >
            <div class="flex flex-col items-center justify-center w-[100%]">
              <div
                class="w-full rounded-lg shadow md:mt-0 sm:max-w-md xl:p-0 backdrop-blur-xl bg-white/30"
              >
                <div class="p-6 space-y-4 md:space-y-6 sm:p-8">
                  <div class="flex flex-col items-center gap-3">
                    <div
                      class="flex items-center text-2xl text-gray-900 font-bold"
                    >
                      <img class="w-8 h-8 mr-2" src="/kanban.ico" alt="logo" />
                      Welcome
                    </div>
                    <h1
                      class="text-xl font-bold leading-tight tracking-tight md:text-2xl text-gray-900"
                    >
                      Sign in to your account
                    </h1>
                  </div>
                  <form action="#">
                    <div>
                      <label
                        class="block mb-2 text-sm font-medium text-gray-900"
                        >Username</label
                      >
                      <input
                        type="text"
                        class="itbkk-username bg-gray-50 border border-gray-300 text-gray-900 rounded-lg focus:outline-none focus:ring focus:ring-gray-800 focus:border-gray-100 block w-full p-2.5"
                        placeholder="username"
                        maxlength="50"
                        v-model="user.userName"
                        autocomplete="username"
                        :inert="inert"
                      />
                    </div>
                    <div class="max-w-lg">
                      <div class="flex items-center">
                        <label
                          class="block mb-2 text-sm font-medium text-gray-900"
                          >Password</label
                        >
                        <a
                          href="#"
                          className="ml-auto inline-block text-sm underline text-gray-900 mb-2"
                        >
                          Forgot password?
                        </a>
                      </div>
                      <div class="relative">
                        <input
                          :type="typePassword"
                          placeholder="••••••••"
                          maxlength="14"
                          v-model="user.password"
                          name="password"
                          autocomplete="current-password"
                          class="itbkk-password py-3 ps-4 pe-10 text-gray-900 rounded-lg bg-gray-50 border border-gray-300 focus:outline-none focus:ring focus:ring-gray-800 focus:border-gray-100 block w-full p-2.5"
                        />
                        <button
                          @mousedown="toggleIcon = true"
                          @mouseup="toggleIcon = false"
                          type="button"
                          autocapitalize="none"
                          class="absolute inset-y-0 end-0 flex items-center z-20 px-3 cursor-pointer text-gray-400 rounded-e-md focus:outline-none focus:text-blue-600"
                        >
                          <svg
                            v-if="toggleIcon === true"
                            xmlns="http://www.w3.org/2000/svg"
                            width="20"
                            height="20"
                            viewBox="0 0 20 20"
                          >
                            <path
                              fill="#808080"
                              d="M10 4.4C3.439 4.4 0 9.232 0 10c0 .766 3.439 5.6 10 5.6c6.56 0 10-4.834 10-5.6c0-.768-3.44-5.6-10-5.6m0 9.907c-2.455 0-4.445-1.928-4.445-4.307S7.545 5.691 10 5.691s4.444 1.93 4.444 4.309s-1.989 4.307-4.444 4.307M10 10c-.407-.447.663-2.154 0-2.154c-1.228 0-2.223.965-2.223 2.154s.995 2.154 2.223 2.154s2.223-.965 2.223-2.154c0-.547-1.877.379-2.223 0"
                            />
                          </svg>
                          <svg
                            v-if="toggleIcon === false"
                            xmlns="http://www.w3.org/2000/svg"
                            width="20"
                            height="20"
                            viewBox="0 0 20 20"
                          >
                            <path
                              fill="#808080"
                              d="M18.521 1.478a1 1 0 0 0-1.414 0L1.48 17.107a1 1 0 1 0 1.414 1.414L18.52 2.892a1 1 0 0 0 0-1.414zM3.108 13.498l2.56-2.56A4.2 4.2 0 0 1 5.555 10c0-2.379 1.99-4.309 4.445-4.309c.286 0 .564.032.835.082l1.203-1.202A13 13 0 0 0 10 4.401C3.44 4.4 0 9.231 0 10c0 .423 1.057 2.09 3.108 3.497zm13.787-6.993l-2.562 2.56c.069.302.111.613.111.935c0 2.379-1.989 4.307-4.444 4.307c-.284 0-.56-.032-.829-.081l-1.204 1.203c.642.104 1.316.17 2.033.17c6.56 0 10-4.833 10-5.599c0-.424-1.056-2.09-3.105-3.495"
                            />
                          </svg>
                        </button>
                      </div>
                    </div>
                  </form>
                  <div class="flex items-start">
                    <div v-if="showMessage" class="itbkk-message text-red-500">
                      <p>{{ messageShow }}</p>
                    </div>
                  </div>

                  <button
                    :disabled="isNotValid"
                    @click="signInOnClick(user)"
                    type="submit"
                    :class="{
                      'bg-gray-800 hover:bg-gray-900 cursor-pointer':
                        !isNotValid,
                      'bg-gray-400 cursor-not-allowed disabled': isNotValid,
                    }"
                    class="itbkk-button-signin w-full text-white font-medium rounded-lg text-sm px-5 py-2.5 text-center focus:outline-none"
                  >
                    Sign in
                  </button>
                  <button
                    type="submit"
                    class="w-full text-gray-900 font-medium rounded-lg text-sm px-5 py-2.5 text-center focus:outline-none bg-white hover:bg-gray-100"
                  >
                    Sign in with Microsoft
                  </button>
                  <div class="mt-4 text-center text-sm">
                    Don't have an account?
                    <a href="#" class="underline"> Sign up </a>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <section class="flex items-center max-lg:hidden w-[65%]">
            <div
              class="mx-auto h-[30%] w-[65%] z-10 rounded-lg shadow-lg bg-gray-50 hidden"
              :class="kanbanAnimate"
            >
              <div class="flex h-full w-full justify-center items-center">
                <div :class="textAnimation" class="flex flex-col gap-6">
                  <div
                    class="text-7xl hidden max-[1700px]:text-6xl max-[1400px]:text-5xl"
                    data-text="INTEGRATED&nbsp;PROJECT"
                  >
                    INTEGRATED PROJECT
                  </div>
                  <div class="text-4xl hidden" data-text="KANBAN&nbsp;BOARD">
                    KANBAN BOARD
                  </div>
                </div>
              </div>
            </div>
            <div
              class="mx-auto my-auto h-[65%] w-[80%] bg-gray-50 z-10 rounded-lg shadow-lg"
              :class="miniTaskBoardAnimate"
            >
              <!-- header -->
              <header>
                <nav
                  class="border-gray-200 px-4 lg:px-6 py-2.5 bg-gray-800 rounded-t-lg"
                >
                  <div
                    class="flex flex-wrap justify-center items-center mx-auto mt-3 mb-3 w-3/4"
                  >
                    <span
                      class="text-white self-center text-2xl font-bold whitespace-nowrap"
                    >
                      Task Board
                    </span>
                  </div>
                </nav>
              </header>
              <div class="flex flex-col items-center">
                <!-- nav -->
                <div class="flex flex-row mt-5 px-[10px] w-[95%]">
                  <div class="m-[2px] flex sm:items-center items-end">
                    <div
                      class="text-gray-800 text-[0.9rem] hover:underline hover:decoration-1 font-bold"
                    >
                      Home
                    </div>

                    <div class="mx-2 text-slate-500">/</div>
                  </div>
                  <!-- Filter -->
                  <div class="flex items-end w-full justify-end sm:mt-0 mt-5">
                    <div class="flex flex-row items-center gap-1">
                      <div class="">
                        <button
                          class="bg-gray-800 text-white font-bold py-1 px-3 rounded-lg text-[0.9rem] max-sm:text-[0.89rem]"
                        >
                          Add Task
                        </button>
                      </div>

                      <!--DropDown-->
                      <div class="dropdown dropdown-bottom">
                        <button
                          tabindex="0"
                          class="flex gap-1 justify-center items-center bg-gray-200 text-gray-800 font-bold py-1 px-3 rounded-lg text-[0.9rem] max-sm:text-[0.89rem]"
                        >
                          Filter
                        </button>
                      </div>
                      <!-- status setting -->
                      <div class="">
                        <button
                          class="itbkk-status-setting bg-gray-200 text-white font-bold py-1 px-3 rounded-lg"
                        >
                          <SettingIcon />
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
                <!-- Content -->
                <div
                  class="flex flex-col justify-center mt-4 gap-3 w-[95%] px-[10px]"
                >
                  <div class="flex flex-row gap-1">
                    <button
                      class="mb-1 w-[60px] bg-gray-800 text-white font-bold py-2 rounded-lg text-[12px]"
                    >
                      Manage Status
                    </button>

                    <div
                      class="flex flex-row gap-[0.2rem] w-full overflow-y-auto overflow-x-hidden pr-1"
                    >
                      <TaskStatusCard
                        colorStatus="#828282"
                        class="h-[40px] w-[60px] text-xs"
                        :class="cardAnimate"
                      >
                        <template #count> 1 </template>
                        <template #status> No Status </template>
                      </TaskStatusCard>

                      <TaskStatusCard
                        colorStatus="#FFFF66"
                        class="h-[40px] w-[60px] text-xs"
                        :class="cardAnimate"
                      >
                        <template #count> 1 </template>
                        <template #status> To Do </template>
                      </TaskStatusCard>

                      <TaskStatusCard
                        colorStatus="#0000FF"
                        class="h-[40px] w-[60px] text-xs"
                        :class="cardAnimate"
                      >
                        <template #count> 1 </template>
                        <template #status> In Progress </template>
                      </TaskStatusCard>

                      <TaskStatusCard
                        colorStatus="#228B22"
                        class="h-[40px] w-[60px] text-xs"
                        :class="cardAnimate"
                      >
                        <template #count> 1 </template>
                        <template #status> Done </template>
                      </TaskStatusCard>
                    </div>
                  </div>

                  <div
                    class="bg-white py-0 rounded-lg shadow-lg h-[16rem] max-h-800px w-[100%] max-[1625px]:h-[90%]"
                  >
                    <!-- Task List -->
                    <table class="w-full bg-white">
                      <thead class="bg-gray-100">
                        <tr class="bg-white">
                          <th
                            class="w-[5%] px-2 max-[1400px]:px-3 max-[1207px]:px-2 py-2 text-center text-xs font-medium text-gray-500 uppercase tracking-wider"
                          >
                            No.
                          </th>
                          <th
                            class="w-[50%] px-2 max-[1400px]:px-3 max-[1207px]:px-2 py-2 text-center text-xs font-medium text-gray-500 uppercase tracking-wider"
                          >
                            Title
                          </th>
                          <th
                            class="w-[15%] px-2 max-[1400px]:px-3 max-[1207px]:px-2 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                          >
                            Assignees
                          </th>
                          <th
                            class="w-[25%] text-center px-2 max-[1400px]:px-3 max-[1207px]:px-2 py-2 text-xs font-medium text-gray-500 uppercase tracking-wider"
                          >
                            Status
                          </th>
                          <th
                            class="w-[5%] px-2 max-[1400px]:px-3 max-[1207px]:px-2 py-2 text-center text-xs font-medium text-gray-500 uppercase tracking-wider"
                          >
                            Action
                          </th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr class="bg-white" :class="borderAnimate">
                          <td
                            class="w-[5%] px-2 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm font-medium text-gray-900 text-center"
                          >
                            1
                          </td>
                          <td
                            class="w-[50%] px-2 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm text-gray-500"
                          >
                            NS01
                          </td>
                          <td
                            class="w-[15%] px-2 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm text-gray-500"
                          >
                            Unassigned
                          </td>
                          <td
                            class="w-[25%] px-2 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm text-center"
                          >
                            <span
                              class="px-2 max-[1400px]:px-3 max-[1207px]:px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-[#828282] text-slate-50"
                              >No Status</span
                            >
                          </td>
                          <td
                            class="w-[5%] px-2 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-center text-sm font-medium"
                          >
                            ...
                          </td>
                        </tr>

                        <tr class="bg-white" :class="borderAnimate">
                          <td
                            class="w-[5%] px-2 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm font-medium text-gray-900 text-center"
                          >
                            2
                          </td>
                          <td
                            class="w-[50%] px-2 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm text-gray-500"
                          >
                            TD01
                          </td>
                          <td
                            class="w-[15%] px-2 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm text-gray-500"
                          >
                            John
                          </td>
                          <td
                            class="w-[25%] px-2 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm text-center"
                          >
                            <span
                              class="px-2 max-[1400px]:px-3 max-[1207px]:px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-[#FFFF66] text-slate-800"
                              >To Do</span
                            >
                          </td>
                          <td
                            class="w-[5%] px-2 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-center text-sm font-medium"
                          >
                            ...
                          </td>
                        </tr>

                        <tr class="bg-white" :class="borderAnimate">
                          <td
                            class="w-[5%] px-2 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm font-medium text-gray-900 text-center"
                          >
                            3
                          </td>
                          <td
                            class="w-[50%] px-2 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm text-gray-500"
                          >
                            IP01
                          </td>
                          <td
                            class="w-[15%] px-2 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm text-gray-500"
                          >
                            Unassigned
                          </td>
                          <td
                            class="w-[25%] px-2 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm text-center"
                          >
                            <span
                              class="px-2 max-[1400px]:px-3 max-[1207px]:px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-[#0000FF] text-slate-50"
                              >In Progress</span
                            >
                          </td>
                          <td
                            class="w-[5%] px-2 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-center text-sm font-medium"
                          >
                            ...
                          </td>
                        </tr>

                        <tr class="bg-white" :class="borderAnimate">
                          <td
                            class="w-[5%] px-2 max-[1400px]:px-3-6 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm font-medium text-gray-900 text-center"
                          >
                            4
                          </td>
                          <td
                            class="w-[50%] px-2 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm text-gray-500"
                          >
                            TD02
                          </td>
                          <td
                            class="w-[15%] px-2 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm text-gray-500"
                          >
                            Adum
                          </td>
                          <td
                            class="w-[25%] px-2 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm text-center"
                          >
                            <span
                              class="px-2 max-[1400px]:px-3 max-[1207px]:px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-[#228B22] text-slate-50"
                              >Done</span
                            >
                          </td>
                          <td
                            class="w-[5%] px-2 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-center text-sm font-medium"
                          >
                            ...
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
    <!-- <div class="h-[8%] bg-gray-100"></div> -->
  </div>
</template>

<style scoped>
input[type="password"]::-ms-reveal {
  display: none;
}

@keyframes cardAnimation {
  0% {
    transform: translateX(-8rem);
    opacity: 0;
  }
  50% {
    opacity: 0.75;
  }
  100% {
    box-shadow: rgba(0, 0, 0, 0.05) 0px 2px 8px, rgba(0, 0, 0, 0.05) 0px 4px 8px;
    transform: translateX(0rem);
    opacity: 1;
  }
}

@keyframes borderAnimation {
  0% {
    transform: translateY(8rem);
    opacity: 0;
    /* // box-shadow: rgba(255, 105, 180, 0.65) 0px 4px 16px, rgba(248, 200, 220, 0.75) 0px 8px 32px;  */
  }
  50% {
    opacity: 0.75;
  }
  100% {
    box-shadow: rgba(0, 0, 0, 0.05) 0px 2px 8px, rgba(0, 0, 0, 0.05) 0px 4px 8px;
    transform: translateY(0rem);
    opacity: 1;
  }
}

.card-animate {
  opacity: 0;
  animation: cardAnimation 2s ease-in forwards;
}

.card-animate:nth-child(1) {
  z-index: 30;
  animation-delay: 0s;
}

.card-animate:nth-child(2) {
  z-index: 20;
  animation-delay: 1s;
}

.card-animate:nth-child(3) {
  z-index: 10;
  animation-delay: 2s;
}
.card-animate:nth-child(4) {
  z-index: 0;
  animation-delay: 3s;
}

.border-animate {
  opacity: 0;
  animation: borderAnimation 2s ease-in forwards;
}

.border-animate:nth-child(1) {
  animation-delay: 0s;
}

.border-animate:nth-child(2) {
  animation-delay: 1s;
}

.border-animate:nth-child(3) {
  animation-delay: 2s;
}
.border-animate:nth-child(4) {
  animation-delay: 3s;
}

@keyframes taskBoardAnimation {
  0% {
    opacity: 1;
    transform: scale(1, 1);
  }
  100% {
    display: none;
    opacity: 0;
    transform: scale(1, 0);
  }
}

.miniTaskBoard-animate {
  opacity: 1;
  animation: taskBoardAnimation 1s ease-in forwards;
}

@keyframes taskBoardAnimationOpacity {
  0% {
    opacity: 1;
  }
  25% {
    display: none;
    opacity: 0;
  }
  100% {
    display: none;
    opacity: 0;
  }
}
.miniTaskBoard-animate header,
.miniTaskBoard-animate button,
.miniTaskBoard-animate div,
.miniTaskBoard-animate nav {
  animation: taskBoardAnimationOpacity 1s ease-in forwards;
}

@keyframes kanbanAnimation {
  0% {
    transform: scale(1, 0);
  }

  100% {
    transform: scale(1, 1);
  }
}
.kanban {
  display: block;
  animation: kanbanAnimation 1s ease-in forwards;
}

@keyframes kanbanAnimationText {
  0% {
    opacity: 0;
    display: none;
  }
  25% {
    display: none;
    opacity: 0;
  }
  100% {
    opacity: 1;
    display: block;
  }
}

.kanban h1 {
  animation: kanbanAnimationText 1s ease-in forwards;
}

@keyframes animate {
  0% {
    width: 0%;
  }
  70% {
    width: 100%;
  }
}
.textAnimation div {
  position: relative;
  display: block;
  -webkit-text-stroke: 0.2vw rgba(26, 26, 26, 0);
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0);
}
.textAnimation div::before {
  content: attr(data-text);
  position: absolute;
  top: 0;
  left: 0;
  width: 0;
  color: rgb(13, 13, 13);
  -webkit-text-stroke: 0vw rgba(217, 103, 103, 0);
  border-right: 2px solid #ff08f700;
  overflow: hidden;
  animation: animate 10s linear infinite;
  -webkit-animation: animate 10s linear infinite;
}

@keyframes kanbanAnimationToMiniTaskBoard {
  0% {
    transform: scale(1, 1);
  }
  75% {
    opacity: 0;
  }
  100% {
    opacity: 0;
    display: none;
    transform: scale(1, 0);
  }
}

.kanbanToMiniTaskBoard {
  display: block;
  animation: kanbanAnimationToMiniTaskBoard 1s ease-in forwards;
}

@keyframes taskBoardAnimationShow {
  0% {
    display: none;
    opacity: 0;
    transform: scale(1, 0);
  }
  25% {
    display: none;
    opacity: 0;
  }
  50% {
    display: none;
    opacity: 0;
  }
  75% {
    display: block;
    opacity: 1;
  }
  100% {
    transform: scale(1, 1);
  }
}

.miniTaskBoard-animate-show {
  opacity: 1;
  animation: taskBoardAnimationShow 1s ease-in forwards;
}
</style>
