<script setup>
import EditIcon from "./icon/EditIcon.vue";
import DeleteIcon from "./icon/DeleteIcon.vue";
import { useBoardStore } from "../stores/boards.js";
const boardStore = useBoardStore();
defineEmits(["remove","edit"]);


</script>


<template>
  <div class="itbkk-button-action flex flex-row gap-2 max-sm:flex-col">
    <div
      :class="boardStore.isCanEdit ? '' : 'tooltip tooltip-bottom tooltip-hover'"
      data-tip="You need to be board owner to perform this action."
    >
      <div
        class="itbkk-button-edit"
        :class="
          boardStore.isCanEdit ? 'cursor-pointer' : 'cursor-not-allowed disabled'
        "
        @click="$emit('edit')"
      >
        <EditIcon
          class=""
          :class="
            boardStore.isCanEdit
              ? ' fill-zinc-700'
              : ' fill-gray-300'
          "
        />
      </div>
    </div>
    <div
      :class="boardStore.isCanEdit ? '' : 'tooltip tooltip-bottom tooltip-hover'"
      data-tip="You need to be board owner to perform this action."
    >
      <div
        class="itbkk-button-delete"
        @click="$emit('remove')"
        :class="
          boardStore.isCanEdit ? 'cursor-pointer' : 'cursor-not-allowed disabled'
        "
      >
        <DeleteIcon
          :class="boardStore.isCanEdit ? ' fill-rose-500' : ' fill-gray-300'"
        />
      </div>
    </div>
  </div>
</template>

<style scoped></style>
