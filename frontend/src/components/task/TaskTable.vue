<script setup></script>

<template>
  <table class="w-full rounded-md">
    <thead
      class="text-xs text-gray-700 uppercase bg-gray-50 w-full rounded-t-md"
    >
      <tr class="flex w-full rounded-t-md">
        <th class="px-6 py-4 w-[5%] max-md:hidden border-l-4 rounded-tl-md">
          No.
        </th>
        <th
          class="px-6 py-4 w-[30%] max-md:w-[65%] max-md:px-2 max-md:py-3 max-md:border-l-4 max-md:rounded-t-md"
        >
          Title
        </th>
        <th class="px-6 py-4 w-[20%] max-md:w-[40%] max-md:px-2 max-md:py-3">
          Assignees
        </th>
        <th
          class="px-4 py-4 w-[20%] max-md:w-[30%] max-md:px-2 max-md:py-3 rounded-tr-md"
        >
          Attachments
        </th>
        <th
          class="px-2 py-4 w-[10%] max-md:hidden cursor-pointer flex items-center justify-center"
        >
          <slot name="sortStatus"></slot>
        </th>
        <th
          class="px-4 py-4 w-[20%] max-md:w-[30%] max-md:px-2 max-md:py-3 rounded-tr-md"
        >
          Action
        </th>
      </tr>
    </thead>
    <tbody
      class="h-[70vh] max-sm:h-[50vh] flex flex-col items-center w-full overflow-x-hidden overflow-y-auto "
    >
      <slot name="tr-item"> </slot>

      <slot name="showErrorMSG"></slot>
      <slot name="NoRecordFound"></slot>
    </tbody>
  </table>
</template>

<style scoped></style>
