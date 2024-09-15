<script setup>
const props = defineProps({
  colorStatus: {
    type: String,
  },
  displayHeader: {
    type: Boolean,
    default: false,
  },
  width: {
    type: String,
    default: "w-20",
  },
  height: {
    type: String,
    default: "h-full",
  },
});
</script>

<template>
  <div
    class="flex flex-wrap flex-row justify-center items-center p-2 bg-white rounded-md border-l-4"
    :class="`${width} ${height}`"
    :style="{ 'border-color': colorStatus }"
  >
    <div class="flex justify-between" v-if="displayHeader">
      <div class="">
        <slot name="icon"></slot>
      </div>
      <div
        class="flex items-center text-[60%] px-1 py-[0.1rem] rounded-full"
        :class="`bg-${colorStatus}-200 text-${colorStatus}-800`"
      >
        <slot name="percent"></slot>
      </div>
    </div>
    <div class="flex flex-col items-center">
      <div class="font-bold text-[120%]">
        <slot name="count"></slot>
      </div>
      <div
        class="font-bold text-[70%] text-ellipsis overflow-hidden w-16 text-center"
      >
        <slot name="status"></slot>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
