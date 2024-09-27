<script setup>
import TaskTableLoading from "../loading/TaskTableLoading.vue";
import { useStatusStore } from "../../stores/statuses.js";
import { useUserStore } from "../../stores/user.js";
import { useRoute, useRouter } from "vue-router";

import { ref, watch } from "vue";
import EditIcon from "../icon/EditIcon.vue";
import DeleteIcon from "../icon/DeleteIcon.vue";
defineEmits(["removeStatus"]);
const props = defineProps({
  allStatus: {
    type: Array,
    required: true,
  },
  showErrorMSG: {
    type: Boolean,
    default: false,
  },
  showLoading: {
    type: Boolean,
    default: true,
  },
});

const statusStore = useStatusStore();
const userStore = useUserStore();
const router = useRouter();

const isVisible = ref([]);
watch(
  () => props.allStatus,
  () => {
    props.allStatus.forEach((task, index) => {
      setTimeout(() => {
        isVisible.value[index] = true;
      }, (index + 1) * 150);
    });
  },
  { deep: true }
);

function editStatus(id , name) {
  console.log(userStore.isCanEdit);
  if (userStore.isCanEdit) {
   // ***
   console.log("e");
   console.log(name);
   console.log(name !=="No Status" && name !=="Done");
    if (name !=="No Status" && name !=="Done") {
      console.log("ewd");
    // editMode.value = !editMode.value;
     console.log(id);
    router.push({ name: "EditStatus", params: {statusId: id } });
    }
  }
}


</script>

<template>
  <!-- Table -->
  <TaskTableLoading v-if="showLoading" class="w-full" />

  <div v-else class="w-full rounded-md shadow-xl">
    <table class="w-full rounded-md">
      <thead
        class="text-xs text-gray-700 uppercase bg-gray-50 w-full rounded-t-md"
      >
        <tr class="flex w-full rounded-t-md">
          <th class="px-6 py-4 w-[5%] max-lg:hidden border-l-4 rounded-tl-md">
            No.
          </th>
          <th
            class="px-6 py-4 w-[25%] max-lg:w-[30%] max-lg:px-2 max-lg:py-3 max-lg:border-l-4"
          >
            Name
          </th>
          <th class="px-6 py-4 w-[50%] max-lg:w-[45%] max-lg:px-2 max-lg:py-3">
            Description
          </th>

          <th
            class="px-4 py-4 w-[20%] max-lg:w-[25%] max-lg:px-2 max-lg:py-3 rounded-tr-md"
          >
            Action
          </th>
        </tr>
      </thead>
      <tbody
        class="h-[73vh] max-sm:h-[50vh] flex flex-col items-center overflow-y-auto w-full overflow-x-hidden"
      >
        <tr
          class="itbkk-item task-row-wrapper flex w-full items-center justify-center bg-white border-l-4 border-b"
          :style="{
            'border-color': statusStore.getColorStatus(status.name),
            'border-bottom-color': 'rgb(229 231 235 / 0.5)',
          }"
          v-for="(status, index) in allStatus"
          :key="index"
          :class="{ 'slide-in': isVisible[index] }"
        >
          <td class="px-6 py-4 max-lg:hidden w-[5%]">
            {{ index + 1 }}
          </td>

          <td
            class="h-full w-[25%] px-6 py-4 max-lg:w-[30%] max-lg:px-2 max-lg:py-3 hover:bg-neutral-100"
          >
          
            <router-link
              :to="{
                name: 'StatusDetail',
                params: { statusId: status.id },
              }"
            >
              <div
                class="cursor-pointer h-full w-full items-center flex break-all"
              >
                <span class="itbkk-status-name"> {{ status.name }} </span>
              </div>
            </router-link>
          </td>

          <td
            class="w-[50%] px-6 py-4 max-lg:[45%] max-lg:px-2 max-lg:py-3 break-all"
            :class="
              status.description === null || status.description?.length === 0
                ? 'italic text-gray-600'
                : ''
            "
          >
            <span class="itbkk-status-description">
              {{
                status.description === null || status.description?.length === 0
                  ? "No description is provided."
                  : status.description
              }}
            </span>
          </td>
          <td
            class="w-[20%] px-4 py-4 max-lg:w-[25%] max-lg:px-2 max-lg:py-3 cursor-pointer flex justify-center items-center"
          >
            <div
              v-if="status.name !== 'No Status' && status.name !== 'Done'"
              class="flex flex-row gap-4 max-sm:flex-col"
            >
             <div
                  :class="
                    userStore.isCanEdit
                      ? ''
                      : 'tooltip tooltip-top tooltip-hover'
                  "
                  data-tip="You need to be board owner to perform this action."
                >
                <div class="itbkk-button-edit"
                :class="
                      userStore.isCanEdit
                        ? 'cursor-pointer'
                        : 'cursor-not-allowed disabled'
                    "
                    @click="editStatus(status.id ,status.name)"
                >
                  <EditIcon 
                  class="fill-zinc-500"
                  :class="
                      userStore.isCanEdit
                        ? ' hover:fill-zinc-700'
                        : ' hover:fill-zinc-500'
                    "
                  />
                </div>
           
              </div>
              <div
                  :class="
                    userStore.isCanEdit
                      ? ''
                      : 'tooltip tooltip-top tooltip-hover'
                  "
                  data-tip="You need to be board owner to perform this action."
                >
              <div
              class="itbkk-button-delete  text-white fill-rose-300"
       
                @click="$emit('removeStatus', index)"
                :class="
                      userStore.isCanEdit
                      ? 'cursor-pointer'
                        : 'cursor-not-allowed disabled'
                    "
              >
                <DeleteIcon 
                :class="
                      userStore.isCanEdit
                        ? ' hover:fill-rose-400'
                        : ' hover:fill-rose-300'
                    "
                />
              </div>
            </div>
            </div>
          </td>
        </tr>

        <div v-if="showErrorMSG" class="flex h-[100%] items-center w-full">
          <div
            class="flex items-center justify-center max-lg:border-l-4 h-full w-full"
          >
            <span class="text-lg text-slate-700 opacity-50">
              Missing Load Resource
            </span>
          </div>
        </div>

        <div
          v-else-if="allStatus.length === 0 && !showLoading"
          class="flex h-[100%] items-center"
        >
          <span class="text-lg text-slate-700 opacity-50">
            No Record Found
          </span>
        </div>
      </tbody>
    </table>
  </div>
</template>

<style scoped></style>
