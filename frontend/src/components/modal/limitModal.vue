<script setup>
import AlertSquareIcon from "../icon/AlertSquareIcon.vue";
import CloseIcon from "../icon/CloseIcon.vue";
import { computed } from "vue";
defineEmits(["userAction", "confirm"]);
const props = defineProps({
  allTaskLimit: {
    type: Array,
  },
  width: {
    type: String,
    default: "w-[40vh]",
  },
});
const allTaskLimit = computed(() => props.allTaskLimit);
</script>

<template>
  <div
    class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50"
  >
    <div class="bg-white rounded-lg shadow-lg p-8" :class="width">
      <div class="w-full flex justify-end">
        <div
          class="cursor-pointer text-error text-2xl text-wrap"
          @click="$emit('userAction', false)"
        >
          <CloseIcon />
        </div>
      </div>

      <div class="flex justify-center">
        <AlertSquareIcon class="w-16 h-16 opacity-40" />
      </div>
      <div class="mb-4">
        <span class="itbkk-message">
          These statuses that have reached the task limit. No additional tasks
          can be added to these statuses.
        </span>
      </div>
      <div class="mb-6">
        <div>
          <table>
            <tr class="border-y border-slate-800">
              <th class="pr-3">Status Name</th>
              <th class="pr-3">number of tasks</th>
            </tr>
            <tr v-for="status in allTaskLimit">
              <td>{{ status.name }}</td>
              <td class="text-center">{{ status.noOfTasks }}</td>
            </tr>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
