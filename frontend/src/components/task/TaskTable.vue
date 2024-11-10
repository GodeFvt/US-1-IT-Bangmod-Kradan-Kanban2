<script setup>

</script>
 
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
            <!-- <div class="itbkk-status-sort flex gap-1">
              <span>Status</span> -->
             
              <!-- <SortIcon sortType="default" v-if="sortType === 'default'" />
              <SortIcon sortType="asc" v-else-if="sortType === 'asc'" />
              <SortIcon sortType="desc" v-else-if="sortType === 'desc'" /> -->
            <!-- </div> -->
          </th>
          <th
            class="px-4 py-4 w-[20%] max-md:w-[30%] max-md:px-2 max-md:py-3 rounded-tr-md"
          >
            Action
          </th>
        </tr>
      </thead>
      <tbody
        class="h-[60vh] max-sm:h-[50vh] flex flex-col items-center w-full overflow-x-hidden overflow-y-auto"
      >
      
      <slot name="tr-item">
       
      </slot>  
     
         <slot name="showErrorMSG"></slot>
         <slot name="NoRecordFound"></slot>

        <!-- <div v-if="showErrorMSG" class="flex h-[100%] items-center w-full">
          <div
            class="flex items-center justify-center max-md:border-l-4 h-full w-full"
          >
            <span class="text-lg text-slate-700 opacity-50">
              Missing Load Resource
            </span>
          </div>
        </div>

        <div
          v-else-if="taskFiltered.length === 0 && !showLoading"
          class="flex h-[100%] items-center"
        >
          <span class="text-lg text-slate-700 opacity-50">
            No Record Found
          </span>
        </div> -->
      </tbody>
    </table>
</template>
 
<style scoped>

</style>