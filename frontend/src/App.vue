<script setup>
import HeaderView from "./views/HeaderView.vue";
</script>

<template>
  <HeaderView />
  <router-view />
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
</style>
