<script setup>
import { loginAccount } from "../lib/fetchUtill.js";
import { onMounted, onUnmounted, ref, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
// const pathIcon = ref("../../public/sleep.png");
const toggleIcon = ref(false);
const username = ref("");
const password = ref("");
const pathIcon = computed(() => {
  if (toggleIcon.value) {
    return "../../public/open.png";
  } else {
    return "../../public/sleep.png";
  }
});

const typePassword = computed(() => {
  if (toggleIcon.value) {
    return "text";
  } else {
    return "password";
  }
});
const messageShow = ref("");
async function signInOnClick(usernameParamiter, passwordParamiter) {
  console.log(usernameParamiter);
  console.log(passwordParamiter); 
  let res;
  if (username.length > 0 && password.length > 0) {
    res = await loginAccount(username, password);
  } else {
     res = await validateInput(username, password);
  }

  if (res === 200) {
    console.log("do nothing");
  } else if (res === 400 || res === 401) {
    messageShow.value = "Username or Password is incorrect..";
  } else {
    messageShow.value = "There is a problem.Please try again.";
  }
}
</script>

<template>
  <section class="bg-gray-200">
    <div
      class="flex flex-col items-center justify-center px-6 py-8 mx-auto md:h-screen lg:py-0"
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
              v-model="username"
            />
          </div>
          <div class="max-w-sm">
            <label class="block mb-2 text-sm font-medium text-gray-100"
              >Password</label
            >
            <div class="relative">
              <input
                :type="typePassword"
                placeholder="••••••••"
                class="itbkk-password py-3 ps-4 pe-10 text-gray-900 rounded-lg bg-gray-50 border border-gray-300 focus:outline-none focus:ring focus:ring-pink-500 focus:border-gray-100 block w-full p-2.5"
              />
              <button
                @mousedown="toggleIcon = true"
                @mouseup="toggleIcon = false"
                type="button"
                data-hs-toggle-password='{
        "target": "#hs-toggle-password"
      }'
                class="absolute inset-y-0 end-0 flex items-center z-20 px-3 cursor-pointer text-gray-400 rounded-e-md focus:outline-none focus:text-blue-600 dark:text-neutral-600 dark:focus:text-blue-500"
              >
                <img class="w-8 h-8 mr-2" :src="pathIcon" alt="icon" />
              </button>
            </div>
          </div>

          <div class="flex items-center justify-between">
            <div class="flex items-start">
              <div v-if="true" class="itbkk-message text-red-500">
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
            @click="signInOnClick(username, password)"
            type="submit"
            class="itbkk-button-signin w-full text-white bg-pink-600 hover:bg-pink-700 focus:ring-4 focus:outline-none focus:ring-pink-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center"
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
</template>

<style scoped></style>
