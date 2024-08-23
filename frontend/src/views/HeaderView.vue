<script setup>
import { ref, nextTick, watch } from "vue";
import { useUserStore } from "../stores/user.js";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// watch(
//   () => route.path,
//   async (newPath) => {
//     await nextTick(); //wait untill DOM is ready so useful wa

//     const token = localStorage.getItem('authToken');
//     if (token) {
//       try {
//         const decodedToken = VueJwtDecode.decode(token);
//         user.value = decodedToken.name;
//       } catch (error) {
//         user.value = "Error decoding token";
//       }
//     } else {
//       user.value = "Anonymous";
//     }
//   },
//   { immediate: true }
// );

function signOut() {
  userStore.clearAuthToken();
  router.push({ name: "Login" });
}
</script>

<template>
  <header>
    <nav
      class="border-gray-200 px-4 lg:px-6 py-2.5 bg-gray-800 shadow-xl h-full"
    >
      <div class="flex items-center mx-auto w-[100%] h-full justify-between">
        <!-- Center: Task Board title -->
        <div class="flex text-center pl-14">
          <span class="text-white text-3xl font-semibold whitespace-nowrap">
            US-1 Kanban
          </span>
        </div>

        <!-- <div class="flex items-center space-x-2 text-gray-300">
          <div @click="signOut" class="cursor-pointer">
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
          </div>
          <span class="text-l">
            <span class="fullname">
              {{ userStore.authToken?.name || "Anonymous" }}</span
            >
          </span>
        </div> -->
      </div>
    </nav>
  </header>
</template>

<style scoped></style>
