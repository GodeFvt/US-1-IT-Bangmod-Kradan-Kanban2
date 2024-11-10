<script setup>
import { ref, watch, onMounted } from "vue";
import { useRoute } from "vue-router";
import { useUserStore } from "./stores/user.js"; 
import SideMenuView from "./views/SideMenuView.vue";

const route = useRoute();
const userStore = useUserStore();
const disabledSideMenu = ref(false);

onMounted(() => {
  userStore.initializeToken();
});

watch(
  () => route.path,
  (newPath, oldPath) => {
    if (newPath === "/login" || newPath === "/login/" || newPath === "/" || newPath.includes("TaskNotFound") || route.name === "404" || newPath.includes("invitation")  ) {
      disabledSideMenu.value = true;
    } else {
      disabledSideMenu.value = false;
    }
  },
  { immediate: true }
);
</script>

<template>
  <div class="" :class="{ 'flex flex-row': !disabledSideMenu }">
    <div class="" v-if="!disabledSideMenu">
      <SideMenuView />
    </div>
    <router-view />
  </div>
</template>

<style>
/* สำหรับ scrollbar */
::-webkit-scrollbar {
  width: 3px;
}

/* สำหรับ track */
::-webkit-scrollbar-track {
  background: #f1f1f1;
}

/* สำหรับ handle */
::-webkit-scrollbar-thumb {
  --tw-bg-opacity: 1;
  background: rgb(31 41 55 / var(--tw-bg-opacity));
}

/* สำหรับ handle on hover */
::-webkit-scrollbar-thumb:hover {
  --tw-bg-opacity: 1;
  background: rgb(31 41 55 / var(--tw-bg-opacity));
}

.task-row-wrapper {
  transform: translateX(-100%);
  opacity: 0;
  transition: transform 1.5s ease, opacity 1.5s ease;
}
.task-row-wrapper.slide-in {
  transform: translateX(0);
  opacity: 1;
}
</style>
