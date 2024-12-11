<script setup>
defineEmits(["userAction", "confirm"]);
const props = defineProps({
  index: {
    type: Number,
    required: false,
  },
  width: {
    type: String,
    default: "w-[40vh]",
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  canEdit:{
    type: Boolean,
    default:true,
  },
  canShow:{
    type: Boolean,
    default:true,
  },
});
</script>

<template>
  <div
    :id="`confirm-modal-${index}`"
    class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50"
  >
    <div class="bg-white rounded-lg shadow-lg p-8" :class="width">
      <div class="mb-4">
        <slot name="header"></slot>
      </div>
      <div class="mb-6">
        <slot name="body"></slot>
      </div>
      <div class="mt-6 flex justify-center gap-2" v-if="canEdit">
        <button
          type="submit"
          class="itbkk-button-confirm text-white inline-flex items-center focus:ring-4 focus:outline-none font-medium rounded-lg text-sm px-5 py-2.5 text-center"
          @click="$emit('userAction', true), $emit('confirm', index, true)"
          :disabled="disabled"
          :class="
            disabled
              ? 'cursor-not-allowed disabled bg-slate-300'
              : 'cursor-pointer bg-green-700 hover:bg-green-800 focus:ring-green-300'
          "
        >
          <slot name="confirm">Confirm</slot>
        </button>

        <button
          type="Cancel"
          class="itbkk-button-cancel text-white inline-flex items-center bg-red-700 hover:bg-red-800 focus:ring-4 focus:outline-none focus:ring-red-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center"
          @click="$emit('userAction', false)"
        >
          <slot name="cancel">Cancel</slot>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
