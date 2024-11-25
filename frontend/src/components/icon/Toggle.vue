<script setup>
const props = defineProps({
    toggleActive: {
    type: Boolean,
    default: false,
  },
});
</script>

<template>
  <!-- Switch Container -->
  <div
    class="w-12 h-6 flex items-center bg-gray-300 rounded-full p-1 "
    :class="toggleActive ? 'bg-gray-500' : 'bg-gray-300'"
  >
    <!-- Switch -->
    <div
      class="w-6 h-6 rounded-full shadow-md transform ease-out duration-200"
      :class="toggleActive ? 'translate-x-6 bg-black' : 'bg-gray-500 translate-x-[-4px]'"
    ></div>
  </div>
  <!-- Switch Container End -->
</template>

<style scoped></style>
