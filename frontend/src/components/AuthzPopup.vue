<script setup>
import { ref, onMounted,onBeforeUnmount } from "vue";
import PopUp from "../components/modal/PopUp.vue";
import { useRouter } from "vue-router";
import { useUserStore } from "../stores/user.js";
import VueJwtDecode from "vue-jwt-decode";

const showPopUp = ref(false);
const timeCount = ref(3);
const intervals = [];
const router = useRouter();
const userStore = useUserStore();

function tokenNotPass() { 
   showPopUp.value = true
    intervals.push(
    setTimeout(() => {
      console.log("popup show");
        router.push({ name: "Login"});
    }, 3000)  
    
  );
intervals.push(
    setInterval(() => {
        console.log("1")
      timeCount.value--;
    }, 1000)
  );
  }
onMounted(() => {  
  checkAuthen();
  tokenNotPass();

});
onBeforeUnmount(() => {
    intervals.forEach(clearInterval);
});
function checkAuthen() {
  let token = localStorage.getItem("authToken") || null;
 
    if (token) {
        const decodeToken = VueJwtDecode.decode(token);
        const currentTime = Math.floor(Date.now() / 1000); //แปลงจาก milisec to sec
        if (decodeToken.exp < currentTime) {
          localStorage.removeItem("authToken");
          userStore.updateIsAuthen(false)
        }
      }
   }

</script>
<template>
    <PopUp v-if="showPopUp">
      <template #message>  
        <p class="text-lg text-gray-700">
          Oops! something went wrong. Please try logging in again. 
        </p>
      </template>
      <template #button>    
        <router-link :to="{ name: 'Login' }">
          <button class="mt-4 bg-gray-800 hover:bg-gray-500 text-white font-bold py-2 px-4 rounded">
            Try Login Again
          </button>
        </router-link> 
        <p class="text-sm text-gray-500">
          Redirecting to home in <span class="text-red-700">{{ timeCount }}</span> seconds...
        </p>
      </template>
    </PopUp>
  </template>
<style scoped>

</style>