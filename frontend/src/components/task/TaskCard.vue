<script setup>

</script>
 
<template>
  <!-- flex กล่องใหญ่ ที่ Todo Doing เป็น colume ๆ -->
<div class="flex flex-row gap-2 w-full h-full  flex-wrap">
    <!-- flex กล่องเล็ก ที่ของแต่ละ status ที่มี task -->
  <slot name="eachStatus">
  <!-- <div class="flex flex-row gap-2">
    <slot name="allTask"></slot>
  </div> -->
   </slot>
</div>
</template>
 
<style scoped>

</style>