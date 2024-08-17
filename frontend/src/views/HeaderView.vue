<script setup>
import { ref, nextTick,watch } from 'vue';
import VueJwtDecode from 'vue-jwt-decode';
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();
const user = ref('Anonymous');
watch(
  () => route.path,
  async (newPath) => {
    await nextTick(); //wait untill DOM is ready so useful wa

    const token = localStorage.getItem('authToken');
    if (token) {
      try {
        const decodedToken = VueJwtDecode.decode(token);
        user.value = decodedToken.name;
      } catch (error) {
        user.value = "Error decoding token";
      }
    } else {
      user.value = "Anonymous";
    }
  },
  { immediate: true }
);

function signOut(){
  localStorage.removeItem('authToken');
  router.push({ name: "Login"});
}

</script>

<template>
  <header>
    <nav class="border-gray-200 px-4 lg:px-6 py-2.5 bg-gray-800 shadow-xl h-full">
      <div class="flex items-center mx-auto w-[95%] h-full justify-between">
        <!-- Center: Task Board title -->
        <div class="flex text-center">
          <span class="text-white text-3xl font-semibold whitespace-nowrap">
            US-1 Task Board
          </span>
        </div>
        
        <div class="flex items-center space-x-2 text-gray-300">
          <span @click="signOut" class="cursor-pointer">
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 20 20">
              <path fill="currentColor" d="m19 10l-6-5v3H6v4h7v3zM3 3h8V1H3c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H3z"/>
            </svg>
          </span>
          <span class="text-l">{{ user }}</span>
        </div>

      </div>
    </nav>
  </header>
</template>

<style scoped>
</style>
