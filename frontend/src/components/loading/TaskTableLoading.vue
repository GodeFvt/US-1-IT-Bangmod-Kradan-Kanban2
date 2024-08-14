<script setup></script>

<template>
  <!-- Table -->
  <div class="m-4 w-3/4 rounded-md lg:shadow-xl lg:border-l-4">
    <table class="w-full">
      <thead class="bg-gray-50 w-full">
        <tr class="flex w-full">
          <th class="px-6 py-4 w-[5%] max-lg:hidden">
            <div class="h-4 bg-gray-300 animate-pulse rounded-md"></div>
          </th>
          <th
            class="px-6 py-4 w-[50%] max-lg:w-[65%] max-lg:px-2 max-lg:py-3 max-lg:border-l-4"
          >
            <div class="h-4 bg-gray-300 animate-pulse rounded-md"></div>
          </th>
          <th class="px-6 py-4 w-[25%] max-lg:w-[40%] max-lg:px-2 max-lg:py-3">
            <div class="h-4 bg-gray-300 animate-pulse rounded-md"></div>
          </th>
          <th class="px-6 py-4 w-[10%] max-lg:hidden">
            <div class="h-4 bg-gray-300 animate-pulse rounded-md"></div>
          </th>
          <th class="px-4 py-4 w-[10%] max-lg:w-[30%] max-lg:px-2 max-lg:py-3">
            <div class="h-4 bg-gray-300 animate-pulse rounded-md"></div>
          </th>
        </tr>
      </thead>
      <tbody
        class="h-[60vh] max-sm:h-[50vh] flex flex-col items-center w-full overflow-y-scroll overflow-x-hidden"
      >
        <tr
          class="flex w-full items-center justify-center bg-white border-b max-lg:border-l-4"
          v-for="i in 12"
        >
          <td class="px-6 py-4 max-lg:hidden w-[5%]">
            <div class="h-4 bg-gray-100 animate-pulse rounded-md"></div>
          </td>
          <td
            class="h-full w-[50%] px-6 py-4 max-lg:w-[65%] max-lg:px-2 max-lg:py-3"
          >
            <div class="h-4 bg-gray-100 animate-pulse rounded-md"></div>
          </td>
          <td
            class="w-[25%] px-6 py-4 break-all max-lg:w-[40%] max-lg:px-2 max-lg:py-3"
          >
            <div class="h-4 bg-gray-100 animate-pulse rounded-md"></div>
          </td>
          <td class="itbkk-status w-[10%] px-6 py-4 max-lg:hidden">
            <div class="h-4 bg-gray-100 animate-pulse rounded-md"></div>
          </td>
          <td
            class="itbkk-status w-[10%] px-4 py-4 max-lg:w-[30%] max-lg:px-2 max-lg:py-3"
          >
            <div class="h-4 bg-gray-100 animate-pulse rounded-md"></div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped></style>
