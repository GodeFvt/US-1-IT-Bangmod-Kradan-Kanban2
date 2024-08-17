<script setup>
import { loginAccount } from "../lib/fetchUtill.js";
import { ref, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import SettingIcon from "../components/icon/SettingIcon.vue";
import TaskStatusCard from "../components/status/TaskStatusCard.vue";

const toggleIcon = ref(false);
const user = ref({
  userName: "",
  password: ""
})
const showMessage = ref(false);
const router = useRouter();
const route = useRoute();


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


async function signInOnClick(userLogin) {
  let res;
  if (userLogin.userName.length > 0 && userLogin.password.length > 0) {
    res = await loginAccount(userLogin);
    if (typeof res === 'object') 
  {
    localStorage.setItem('authToken', res.access_token);
    router.push({ name: "task"});
  } else if (res === 400 || res === 401) {
    console.log(localStorage);
    showMessage.value = true;
    messageShow.value = "Username or Password is incorrect..";
  } else {
    showMessage.value = true;
    messageShow.value = "There is a problem.Please try again.";
  }
  }
  
}
</script>

<template>
  <div class="grid grid-rows-1 grid-cols-3 grid-flow-col max-lg:flex">
    <section class="bg-gray-200 max-lg:mx-0 max-lg:my-0 max-lg:grow">
      <div
        class="flex flex-col items-center justify-center px-6 py-8 mx-auto h-screen lg:py-0 max-sm:h-screen"
      >
        <div class="flex items-center mb-6 text-2xl text-gray-900 font-bold">
          <img class="w-8 h-8 mr-2" src="../../public/kanban.ico" alt="logo" />
          Welcome
        </div>
        <div
          class="w-full rounded-lg shadow dark:border md:mt-0 sm:max-w-md xl:p-0 bg-gray-700 border-gray-800"
        >
          <div class="p-6 space-y-4 md:space-y-6 sm:p-8">
            <h1
              class="text-xl font-bold leading-tight tracking-tight md:text-2xl text-gray-50"
            >
              Sign in to your account
            </h1>

            <div>
              <label class="block mb-2 text-sm font-medium text-gray-100"
                >Username</label
              >
              <input
                type="text"
                class="itbkk-username bg-gray-50 border border-gray-300 text-gray-900 rounded-lg focus:outline-none focus:ring focus:ring-pink-500 focus:border-gray-100 block w-full p-2.5"
                placeholder="username"
                maxlength="50"
                v-model="user.userName"
              />
            </div>
            <div class="max-w-lg">
              <label class="block mb-2 text-sm font-medium text-gray-100"
                >Password</label
              >
              <div class="relative">
                <input
                  :type="typePassword"
                  placeholder="••••••••"
                  maxlength="14"
                  v-model="user.password"
                  spellcheck="false"
                  class="itbkk-password py-3 ps-4 pe-10 text-gray-900 rounded-lg bg-gray-50 border border-gray-300 focus:outline-none focus:ring focus:ring-pink-500 focus:border-gray-100 block w-full p-2.5"
                />
                <button
                  @mousedown="toggleIcon = true"
                  @mouseup="toggleIcon = false"
                  type="button"
                  autocapitalize="none"
                  class="absolute inset-y-0 end-0 flex items-center z-20 px-3 cursor-pointer text-gray-400 rounded-e-md focus:outline-none focus:text-blue-600 dark:text-neutral-600 dark:focus:text-blue-500"
                >
                  <svg
                    v-if="toggleIcon === true"
                    xmlns="http://www.w3.org/2000/svg"
                    width="28"
                    height="28"
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
                    width="28"
                    height="28"
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

            <div class="flex items-center justify-between">
              <div class="flex items-start">
                <div v-if="showMessage" class="itbkk-message text-red-500">
                  <p>{{ messageShow }}</p>
                </div>
                <!-- <div class="flex items-center h-5">
                            <input id="remember" aria-describedby="remember" type="checkbox" class="w-4 h-4 border border-gray-300 rounded bg-gray-50 focus:ring-3 focus:ring-primary-300 dark:bg-gray-700 dark:border-gray-600 dark:focus:ring-primary-600 dark:ring-offset-gray-800" required="">
                          </div> -->
                <!-- <div class="ml-3 text-sm">
                            <label for="remember" class="text-gray-500 dark:text-gray-300">Remember me</label>
                          </div> -->
              </div>
              <!-- <a href="#" class="text-sm font-medium text-primary-600 hover:underline text-white">Forgot password?</a> -->
            </div>

            <button
              :disabled="isNotValid"
              @click="signInOnClick(user)"
              type="submit"
              :class="{
                'bg-pink-600 hover:bg-pink-700 focus:ring-pink-300':
                  !isNotValid,
                'bg-gray-400 cursor-not-allowed': isNotValid,
              }"
              class="itbkk-button-signin w-full text-white font-medium rounded-lg text-sm px-5 py-2.5 text-center focus:outline-none"
            >
              Sign in
            </button>
            <!-- <p class="text-sm font-light text-gray-500 dark:text-gray-400">
                      Don’t have an account yet? <a href="#" class="font-medium text-primary-600 hover:underline text-slate-300">Sign up</a>
                  </p> -->
          </div>
        </div>
      </div>
    </section>

    <section class="flex items-center bg-gray-500 col-span-2 max-lg:hidden">
      <div class="mx-auto my-auto h-[65%] w-[75%] bg-gray-50 z-10">
        <!-- header -->
        <header>
          <nav
            class="border-gray-200 px-4 lg:px-6 py-2.5 bg-gray-800 shadow-xl"
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

        <!-- nav -->
        <div class="flex flex-row mt-5 max-sm:w-full px-[10px] max-sm:px-2">
          <div class="m-[2px] flex sm:items-center items-end">
            <div
              class="text-gray-800 text-[1rem] hover:underline hover:decoration-1 font-bold"
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
                  class="itbkk-button-add bg-gray-800 hover:bg-gray-500 text-white font-bold py-2 px-4 rounded-lg text-[0.9rem] max-sm:text-[0.89rem]"
                >
                  Add Task
                </button>
              </div>

              <!--DropDown-->
              <div class="dropdown dropdown-bottom">
                <button
                  tabindex="0"
                  class="flex gap-1 justify-center items-center bg-gray-200 hover:bg-gray-500 text-gray-800 font-bold py-2 px-4 rounded-lg text-[0.9rem] max-sm:text-[0.89rem]"
                >
                  Filter
                </button>
              </div>
              <!-- status setting -->
              <div class="">
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
          class="flex justify-center mt-4 gap-3 w-full ">
          <div>
            <button
              class="mb-1 w-[60px] bg-gray-800 hover:bg-gray-500 text-white font-bold py-2 rounded-lg text-[12px]"
            >
              Manage Status
            </button>

            <div
              class="flex flex-col gap-[0.2rem] h-[40vh] w-full overflow-y-auto overflow-x-hidden pr-1"
            >
              <TaskStatusCard
                colorStatus="#828282"
                class="h-[40px] w-[60px] text-xs"
              >
                <template #count> 1 </template>
                <template #status> No Status </template>
              </TaskStatusCard>

              <TaskStatusCard
                colorStatus="#FFFF66"
                class="h-[40px] w-[60px] text-xs"
              >
                <template #count> 1 </template>
                <template #status> To Do </template>
              </TaskStatusCard>

              <TaskStatusCard
                colorStatus="#0000FF"
                class="h-[40px] w-[60px] text-xs"
              >
                <template #count> 1 </template>
                <template #status> In Progress </template>
              </TaskStatusCard>

              <TaskStatusCard
                colorStatus="#228B22"
                class="h-[40px] w-[60px] text-xs"
              >
                <template #count> 1 </template>
                <template #status> Done </template>
              </TaskStatusCard>
            </div>
          </div>

          <!-- Content -->
         
            <div class="bg-white px-6 py-0 rounded-lg shadow-lg h-[22rem] max-[1625px]:h-[80%]">
              <!-- Task List -->
              <table class="w-full mt-6 bg-white ">
                <thead class="bg-gray-100">
                  <tr>
                    <th
                      class="px-6 max-[1400px]:px-3 max-[1207px]:px-2 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      No.
                    </th>
                    <th
                      class="px-6 max-[1400px]:px-3 max-[1207px]:px-2 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      Title
                    </th>
                    <th
                      class="px-6 max-[1400px]:px-3 max-[1207px]:px-2 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      Assignees
                    </th>
                    <th
                      class="px-4 max-[1400px]:px-3 max-[1207px]:px-2 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      Status
                    </th>
                    <th
                      class="px-6 max-[1400px]:px-3 max-[1207px]:px-2 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      Action
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr class="bg-white">
                    <td
                      class="px-6 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm font-medium text-gray-900"
                    >
                      1
                    </td>
                    <td
                      class="px-6 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm text-gray-500"
                    >
                      NS01
                    </td>
                    <td
                      class="px-6 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm text-gray-500"
                    >
                      Unassigned
                    </td>
                    <td class="px-4 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm">
                      <span
                        class="px-4 max-[1400px]:px-3 max-[1207px]:px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-[#828282] text-slate-50"
                        >No Status</span
                      >
                    </td>
                    <td
                      class="px-6 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-right text-sm font-medium"
                    >
                      ...
                    </td>
                  </tr>

                  <tr class="bg-white">
                    <td
                      class="px-6 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm font-medium text-gray-900"
                    >
                      2
                    </td>
                    <td
                      class="px-6 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm text-gray-500"
                    >
                      TD01
                    </td>
                    <td
                      class="px-6 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm text-gray-500"
                    >
                      John
                    </td>
                    <td class="px-4 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm">
                      <span
                        class="px-4 max-[1400px]:px-3 max-[1207px]:px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-[#FFFF66] text-slate-800"
                        >To Do</span
                      >
                    </td>
                    <td
                      class="px-6 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-right text-sm font-medium"
                    >
                      ...
                    </td>
                  </tr>

                  <tr class="bg-white">
                    <td
                      class="px-6 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm font-medium text-gray-900"
                    >
                      3
                    </td>
                    <td
                      class="px-6 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm text-gray-500"
                    >
                      IP01
                    </td>
                    <td
                      class="px-6 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm text-gray-500"
                    >
                      Unassigned
                    </td>
                    <td class="px-4 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm">
                      <span
                        class="px-4 max-[1400px]:px-3 max-[1207px]:px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-[#0000FF] text-slate-50"
                        >In Progress</span
                      >
                    </td>
                    <td
                      class="px-6 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-right text-sm font-medium"
                    >
                      ...
                    </td>
                  </tr>

                  <tr class="bg-white">
                    <td
                      class="px-6 max-[1400px]:px-3-6 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm font-medium text-gray-900"
                    >
                      4
                    </td>
                    <td
                      class="px-6 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm text-gray-500"
                    >
                      TD02
                    </td>
                    <td
                      class="px-6 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm text-gray-500"
                    >
                      Adum
                    </td>
                    <td class="px-4 max-[1400px]:px-3 max-[1207px]:px-2 py-3 whitespace-nowrap text-sm">
                      <span
                        class="px-4 max-[1400px]:px-3 max-[1207px]:px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-[#228B22] text-slate-50"
                        >Done</span
                      >
                    </td>
                    <td
                      class="px-6 max-[1400px]:px-3  max-[1207px]:px-2  py-3 whitespace-nowrap text-right text-sm font-medium"
                    >
                      ...
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
        
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
input[type="password"]::-ms-reveal {
  display: none;
}
</style>
