<script setup>
import { onMounted, onUnmounted, ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useUserStore } from "../stores/user.js";
const userStore = useUserStore();
const router = useRouter();
const route = useRoute();
const timeCount = ref(3);
let intervals = [];
const TaskNotFound = ref(route.params.page);
onMounted(() => {
  intervals.push(
    setTimeout(() => {
      if (TaskNotFound.value === "Status") {
        router.push({ name: "ManageStatus" });
      }
      if (TaskNotFound.value === "Board") {
        router.push({ name: "board" });
      } else {
        router.push({
          name: "task",
          params: { boardId: userStore.boards[0].id },
        });
      }
    }, 3000)
  );
  intervals.push(
    setInterval(() => {
      timeCount.value--;
    }, 1000)
  );
});

onUnmounted(() => {
  clearAllInterval();
});
function clearAllInterval() {
  intervals.map((interval) => clearInterval(interval));
  intervals = [];
}
</script>

<template>
  <div class="flex flex-col justify-center items-center mt-20">
    <h1 class="text-7xl text-red-700">404 Not Found</h1>
    <br />
    <p
      v-if="TaskNotFound === 'Task'"
      class="itbkk-message text-5xl text-red-700"
    >
      The requested task does not exist.
    </p>
    <p
      v-else-if="TaskNotFound === 'Status'"
      class="itbkk-message text-5xl text-red-700"
    >
      An error has occurred, the status does not exist
    </p>
    <p
      v-else-if="TaskNotFound === 'Board'"
      class="itbkk-message text-5xl text-red-700"
    >
      An error has occurred, the board does not exist
    </p>
    <p v-else class="itbkk-message text-5xl text-red-700">
      The page you are looking for does not exist.
    </p>
    <br />
    <p class="text-3xl text-gray-500">
      Redirecting to home in
      <span class="text-red-700">{{ timeCount }}</span> seconds...
    </p>
    <br />
    <router-link :to="{ name: 'board' }">
      <button
        class="mt-4 bg-gray-800 hover:bg-gray-500 text-white font-bold py-2 px-4 rounded"
      >
        Go to Home
      </button>
    </router-link>
  </div>
</template>

<style scoped></style>
