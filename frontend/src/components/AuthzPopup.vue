<script setup>
import { ref, onMounted, onBeforeUnmount } from "vue";
import PopUp from "../components/modal/PopUp.vue";
import { useRouter } from "vue-router";
import { useUserStore } from "../stores/user.js";

const showPopUp = ref(false);
const timeCount = ref(3);
const intervals = [];
const router = useRouter();
const userStore = useUserStore();

function tokenNotPass() {
  userStore.clearAuthToken();
  showPopUp.value = true;
  intervals.push(
    setTimeout(() => {
      router.push({ name: "Login" });
    }, 3000)
  );
  intervals.push(
    setInterval(() => {
      timeCount.value--;
    }, 1000)
  );
}
onMounted(() => {
  tokenNotPass();
});
onBeforeUnmount(() => {
  intervals.forEach(clearInterval);
});
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
        <button
          class="mt-4 bg-gray-800 hover:bg-gray-500 text-white font-bold py-2 px-4 rounded"
        >
          Try Login Again
        </button>
      </router-link>
      <p class="text-sm text-gray-500">
        Redirecting to home in
        <span class="text-red-700">{{ timeCount }}</span> seconds...
      </p>
    </template>
  </PopUp>
</template>
<style scoped></style>
