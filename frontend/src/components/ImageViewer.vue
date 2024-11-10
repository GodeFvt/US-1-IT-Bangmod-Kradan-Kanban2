<template>
  <div
    v-if="visible"
    class="fixed inset-0 bg-black bg-opacity-70 flex items-center justify-center z-50 w-screen h-screen"
  >
    <button @click="close" class="absolute top-4 right-4 text-white text-lg">
      X
    </button>
    <div class="relative max-w-full max-h-full p-4">
      <!-- Close button -->

      <!-- Image -->
      <img
        :src="imageSrc"
        alt="Large preview"
        class="max-w-full max-h-[80vh] object-contain cursor-pointer transition-transform duration-200"
        :style="{ transform: `scale(${scale})` }"
        @wheel="handleScroll"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from "vue";

const props = defineProps({
  imageSrc: {
    type: String,
    required: true,
  },
  visible: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["close"]);
const scale = ref(1);

watch(
  () => props.visible,
  (newValue) => {
    if (!newValue) scale.value = 1;
  }
);

function close() {
  emit("close");
}

function handleScroll(event) {
  event.preventDefault();
  scale.value += event.deltaY * -0.001;
  scale.value = Math.min(Math.max(0.5, scale.value), 3); // Limit zoom between 0.5x and 3x
}
</script>

<style scoped>
.fixed {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
