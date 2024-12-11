<script setup>
import { FileIcon, EyeTrackingIcon, DeleteIcon, DownloadIcon } from "./icon";
defineEmits(["openImage", "deleteFile", "downloadFile"]);
import { previewBinary } from "../lib/utill";

const props = defineProps({
  filename: {
    type: String,
  },
  fileurl: {
    type: String,
  },
  isDeleteFile: {
    type: Boolean,
    default: false,
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
    class="w-full h-full rounded-md focus:outline-none focus:shadow-outline bg-gray-100 cursor-pointer relative shadow-md group/item border-gray-300"
  >
    <img
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

    <div
      class="flex text-xs w-full absolute bottom-0 items-end justify-between bg-gray-800 rounded-b-md z-20"
    >
      <h1 class="text-white truncate p-1">{{ filename }}</h1>
      <button
        class="hover:bg-gray-300 text-white hover:text-gray-800 h-7 w-7 pl-[1.5px]"
        @click="$emit('deleteFile')"
        v-if="!chooseFile"
      >
        <DownloadIcon class="h-5 w-5" @click="$emit('downloadFile')" />
      </button>
      <button
        class="hover:bg-gray-300 fill-rose-500 hover:rounded-br-md h-7 w-7"
        @click="$emit('deleteFile')"
        v-if="isDeleteFile"
      >
        <DeleteIcon class="h-[26px] w-[26px]" />
      </button>
    </div>

    <div class="group/item">
      <div
        v-if="fileCanPreview(filename) != 'any'"
        class="w-full h-full absolute top-0 group-hover/item:bg-black group-hover/item:bg-opacity-15 p-2 rounded-md invisible group/edit group-hover/item:visible z-10 gap-2"
        @click="$emit('openImage')"
      ></div>

      <a
        v-if="fileCanPreview(filename) == 'any' && chooseFile"
        :href="fileurl"
        target="_blank"
        class="w-full h-full absolute top-0 group-hover/item:bg-black group-hover/item:bg-opacity-15 p-2 rounded-md invisible group/edit group-hover/item:visible z-10 gap-2"
        :download="filename"
      ></a>

      <div
        class="flex text-xs absolute right-0 top-0 items-start justify-end p-2 rounded-t-md invisible group/edit group-hover/item:visible z-10 gap-2"
      >
        <button
          class="focus:outline-none hover:text-gray-700 text-gray-800 hover:rounded-br-md z"
        >
          <a
            v-if="chooseFile"
            :href="fileurl"
            target="_blank"
            class=""
            :download="filename"
            ><DownloadIcon
          /></a>
          <div v-else>
            <DownloadIcon @click="$emit('downloadFile')" />
          </div>
        </button>
        <button
          class="focus:outline-none hover:text-gray-700 text-gray-800 hover:rounded-br-md"
          v-if="fileCanPreview(filename) != 'any'"
          @click="$emit('openImage')"
        >
          <EyeTrackingIcon class="h-5 w-5" />
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
