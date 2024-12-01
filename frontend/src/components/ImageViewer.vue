<script setup>
import { onMounted, onUnmounted, ref, watch } from "vue";
import {
  CloseIcon,
  ZoomInIcon,
  ZoomOutIcon,
  ResetIcon,
  RotateLeftIcon,
  RotateRightIcon,
} from "./icon"; // เพิ่มไอคอน

const props = defineProps({
  imageSrc: {
    type: String,
  },
  visible: {
    type: Boolean,
    default: false,
  },
  showLoadingImage: {
    type: Boolean,
    default: false,
  },
  imageType: {
    type: String,
    default: "image",
  },
});

const emit = defineEmits(["close"]);
const scale = ref(1);
const rotation = ref(0);


watch(
  () => props.visible,
  (newValue) => {
    if (!newValue) {
      scale.value = 1;
      rotation.value = 0;
    }
  }
);

function close() {
  emit("close");
}

function handleScroll(event) {
  event.preventDefault();
  adjustScale(event.deltaY * -0.001);
}

function adjustScale(delta) {
  scale.value = Math.min(Math.max(0.5, scale.value + delta), 3);
}

function zoomIn() {
  adjustScale(0.1);
}

function zoomOut() {
  adjustScale(-0.1);
}

function resetZoom() {
  scale.value = 1;
}

function rotateLeft() {
  rotation.value -= 90;
}

function rotateRight() {
  rotation.value += 90;
}
</script>

<template>
  <div v-if="visible">
    <div class="fixed inset-0 bg-black bg-opacity-70 z-50 w-full h-full">
      <button
        @click="close"
        class="absolute top-7 right-7 text-white text-lg z-50"
      >
        <CloseIcon />
      </button>
      <div
        v-if="showLoadingImage"
        class="loading loading-dots loading-lg bg-white"
      ></div>
      <div class="relative max-w-full max-h-full p-4">
        <img
          v-if="imageType == 'image' && !showLoadingImage"
          :src="imageSrc"
          alt="Large preview"
          class="max-w-full max-h-[80vh] object-contain cursor-pointer transition-transform duration-200"
          :style="{
            transform: `scale(${scale}) rotate(${rotation}deg)`,
          }"
          @wheel="handleScroll"
        />
      </div>
      <embed
        v-if="imageType == 'embed' && !showLoadingImage"
        :src="imageSrc"
        class="h-[80%] w-[80%] bg-white"
      />

      <!-- Toolbar -->
      <div
        class="absolute bottom-10 bg-slate-800 left-1/2 transform -translate-x-1/2 flex p-1 gap-3 z-50 rounded-md"
        v-if="imageType == 'image'"
      >
        <button @click="zoomIn" class="text-white">
          <ZoomInIcon />
        </button>
        <button @click="zoomOut" class="text-white">
          <ZoomOutIcon />
        </button>
        <button @click="resetZoom" class="text-white">
          <ResetIcon />
        </button>
        <button @click="rotateLeft" class="text-white">
          <RotateLeftIcon />
        </button>
        <button @click="rotateRight" class="text-white">
          <RotateRightIcon />
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fixed {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
