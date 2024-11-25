<script setup>
import TaskCard from "../task/TaskCard.vue";
</script>

<template>
  <TaskCard>
    <template #eachStatus>
      <!-- Card -->
      <div
        class="w-[319px] h-[38rem] flex flex-col border-t-[5px] bg-gray-100 rounded-lg shadow-md shrink-0 snap-always snap-center"
        v-for="i in 4"
      >
        <div
          class="font-medium bg-white min-h-min pb-2 pt-2 flex justify-center border-b-2"
        >
          <p class="bg-gray-300 animate-pulse rounded-md h-[1.5rem] w-[10rem]"></p>
        </div>

        <div
          class="scroll-ml-4 hover:overflow-y-auto lg:overflow-y-auto 2xl:overflow-y-hidden overflow-x-hidden touch-auto gap-3 p-4 flex flex-col"
        >
          <div
            v-for="i in 4"
            class="rounded-lg p-0 bg-gray-200 animate-pulse"
          >
            <div class="rounded-lg p-4 h-[7rem]">
              <p class="font-medium"></p>
              <div>
                <span class="itbkk-assignees"></span>
              </div>
              <div class="action justify-self-end">
                <div
                  class="itbkk-button-action flex flex-row gap-2 max-sm:flex-col"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </TaskCard>
</template>

<style scoped></style>
