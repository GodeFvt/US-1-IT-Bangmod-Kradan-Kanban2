<script setup>
import FileIcon from "./icon/FileIcon.vue";
defineEmits(["openImage"]);
import { previewBinary } from "../lib/utill";

const props = defineProps({
  filename: {
    type: String,
  },
  fileurl: {
    type: String,
  },
  chooseFile: {
    type: Boolean,
    default: false,
  },
});

const fileCanPreview = (name) => {
  if (/\.(png|jpeg|jpg|gif|bmp|svg)$/g.test(name)) {
    return "img";
  } else if (/\.(txt)$/g.test(name)) {
    return "embed";
  } else if (/\.(rtf)$/g.test(name)) {
    if (props.chooseFile) {
      return "any";
    } else {
      return "embed";
    }
  } else if (/\.(pdf)$/g.test(name)) {
    if (props.chooseFile) {
      return "embed";
    } else {
      return "img";
    }
  } else {
    return "any";
  }
};
</script>

<template>
  <div
    class="flex flex-col items-center cursor-pointer"
    @click="$emit('openImage')"
  >
    <img
      v-if="fileCanPreview(filename) === 'img'"
      :src="fileurl"
      alt="previewImagesURL"
      class="h-10 w-10 mt-1 object-cover"
    />
    <embed
      v-else-if="fileCanPreview(filename) === 'embed'"
      :src="fileurl"
      alt="previewImagesURL"
      class="h-10 w-10 mt-1"
    />
    <div v-else>
      <a
        v-if="chooseFile"
        :href="fileurl"
        target="_blank"
        class="text-blue-500 underline"
        :download="filename"
        ><FileIcon class="h-10 w-10 fill-gray-800 mt-1"
      /></a>
      <div v-else>
        <FileIcon class="h-10 w-10 fill-gray-800 mt-1" />
      </div>
    </div>

    <!-- Truncated File Name -->

    <p class="text-xs text-center truncate w-full mt-1">
      {{ filename }}
    </p>
  </div>
</template>

<style scoped></style>
