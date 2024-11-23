<script setup>
import { onMounted, onUnmounted, ref } from "vue";
import { useRouter, useRoute } from "vue-router";
const router = useRouter();
const route = useRoute();
const timeCount = ref(3);
let intervals = [];
const TaskNotFound = ref(route.params.page);
onMounted(() => {
  intervals.push(
    setTimeout(() => {
      if (TaskNotFound.value === "Status") {
        console.log("Status");
        router.push({ name: "ManageStatus" });
      } else if (TaskNotFound.value === "Board") {
        router.push({ name: "Login" });
      } else if (TaskNotFound.value === "authorizAccess") {
        console.log("authorizAccess");
        router.push({ name: "Login" });
      } else if (TaskNotFound.value === "Task") {
        console.log("Task");
        router.push({
          name: "task",
          params: { boardId: route.params.boardId },
        });
      } else {
        router.push({
          name: "board",
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
  <div class="flex justify-center items-center h-screen w-screen ">
    <div class="flex flex-row h-full w-full justify-center items-center gap-10">
    <div class="h-100% w-100%">
      
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="200"
        height="200"
        viewBox="0 0 32 32"
      >
        <path fill="currentColor" d="M18 10h2v2h-2zm-6 0h2v2h-2z" />
        <path
          fill="currentColor"
          d="M26 20h-5v-2h1a2 2 0 0 0 2-2v-4h2v-2h-2V8a2 2 0 0 0-2-2h-2V2h-2v4h-4V2h-2v4h-2a2 2 0 0 0-2 2v2H6v2h2v4a2 2 0 0 0 2 2h1v2H6a2 2 0 0 0-2 2v8h2v-8h20v8h2v-8a2 2 0 0 0-2-2M10 8h12v8H10Zm3 10h6v2h-6Z"
        />
      </svg>

    </div>
    <div class="flex flex-col justify-center h-100% w-100%  items-start gap-5">
      <div class="text-red-500">
        <h1 class="text-7xl font-bold">{{
          TaskNotFound === "authorizAccess"
            ? "403"
            : "404"
        }}</h1>
       
      </div>
      <div>
        <h5 class="text-3xl font-semibold text-red-600">{{
          TaskNotFound === "authorizAccess"
            ? "Access denied"
            : "Not Found"
        }}</h5>
      </div>
      <div class="itbkk-message text-red-800 ">
        <span class="text-lg font-normal mt-10  ">{{
          TaskNotFound === "authorizAccess"
            ? "You do not have permission to access this page."
            : "The page you are trying to access doesn't exist."
        }}
        <p
        v-if="TaskNotFound === 'Task'"
      >
        The requested task does not exist.
      </p>
      <p
        v-else-if="TaskNotFound === 'Status'"
      >
        An error has occurred, the status does not exist
      </p>
      <p
        v-else-if="TaskNotFound === 'Board'"
      >
        An error has occurred, the board does not exist
      </p>
      <p
        v-else-if="TaskNotFound === 'authorizAccess'"
      >
        Access denied, you do not have permission to view this page
      </p>
      <p v-else>
        The page you are looking for does not exist.
      </p></span>

      </div>
   
      <p class="text-base text-gray-500">
        We're sending you to the home page  in
        <span class="text-red-700">{{ timeCount }}</span> seconds!
      </p>
      <router-link :to="{ name: 'Login' }">
        <button
          class="mt-4 bg-gray-800 hover:bg-gray-500 text-white font-bold py-2 px-4 rounded-full"
        >
          Go to Home
        </button>
      </router-link>
    </div>
  </div>
  </div>
</template>

<style scoped></style>
