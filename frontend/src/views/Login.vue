<script setup>
import { loginAccount } from "../lib/fetchUtill.js";
import { ref, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
// const pathIcon = ref("../../public/sleep.png");
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
              maxlength="50"
              v-model="user.userName"
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
                maxlength="14"
                v-model="user.password"
                class="itbkk-password py-3 ps-4 pe-10 text-gray-900 rounded-lg bg-gray-50 border border-gray-300 focus:outline-none focus:ring focus:ring-pink-500 focus:border-gray-100 block w-full p-2.5 "
              />
             
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

          <button :disabled="isNotValid"
            @click="signInOnClick(user)"
            type="submit"
            :class="{
              'bg-pink-600 hover:bg-pink-700 focus:ring-pink-300': !isNotValid,
              'bg-gray-400 cursor-not-allowed': isNotValid
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
</template>

<style scoped></style>
