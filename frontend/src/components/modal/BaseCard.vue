<script setup>
const props = defineProps({
  customClass: {
    type: String,
    default: '',
  },
});
 
</script>
<template>
    <div class="rounded-lg border bg-card text-card-foreground shadow-sm transition-shadow hover:shadow-md p-6 flex flex-col justify-between" :class="customClass">
      <div v-if="$slots.header || $slots.title" class="flex flex-col space-y-1.5 p-6">
        <div v-if="$slots.header">
          <slot name="header"></slot>
        </div>
        <h3 v-if="$slots.title" class="text-lg font-semibold leading-none tracking-tight">
          <slot name="title"></slot>
        </h3>
      </div>
      <div v-if="$slots.content" class="p-6 pt-0">
        <slot name="content"></slot>
      </div>
      <div v-if="$slots.default">
        <slot></slot>
      </div>
    </div>
  </template>

<style scoped>

</style>