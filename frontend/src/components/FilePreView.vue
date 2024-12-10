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
  <img
    @click="$emit('openImage')"
    v-if="fileCanPreview(filename) === 'img'"
    :src="fileurl"
    alt="upload preview"
    class="w-full h-full object-cover rounded-md z-20 hover:opacity-80"
  />
  <embed
    v-else-if="fileCanPreview(filename) === 'embed'"
    :src="fileurl"
    alt="previewImagesURL"
    class="w-full h-full object-cover rounded-md hover:opacity-80"
  />
  <div v-else class="items-center justify-center flex w-full h-full pb-6">
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
</template>

<style scoped></style>
