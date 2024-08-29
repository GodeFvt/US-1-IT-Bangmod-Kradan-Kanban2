<script setup></script>

<template>
  <div
    class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50"
  >
    <div
      class="flex items-center justify-center max-lg:mx-0 max-lg:my-0 max-lg:grow w-[35%] "
    >
      <div class="flex flex-col items-center justify-center w-[100%] ">
        <div
          class="w-full rounded-lg shadow md:mt-0 sm:max-w-md xl:p-0 backdrop-blur-xl bg-slate-100"
        >
          <div class="p-6 space-y-4 md:space-y-6 sm:p-8">
            <div class="flex flex-col items-center gap-3">
              <div class="flex items-center text-2xl text-gray-900 font-bold">
                <slot name="header"></slot>
              </div>
              <slot name="message"></slot>
              <slot name="button"></slot>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
