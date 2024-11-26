<script setup>
import TaskTableLoading from "../loading/TaskTableLoading.vue";
import { useUserStore } from "../../stores/user.js";
import { useRoute, useRouter } from "vue-router";
import { computed, ref, watch } from "vue";
import collabDetail from "./collabDetail.vue";
import DeleteIcon from "../icon/DeleteIcon.vue";
import { useBoardStore } from "../../stores/boards.js";

defineEmits(["removeCollab", "addCollab", "changeAccess"]);
const props = defineProps({
  showErrorMSG: {
    type: Boolean,
    default: false,
  },
  showLoading: {
    type: Boolean,
    // default: true,
  },
});

const userStore = useUserStore();
const boardStore = useBoardStore();
let accessRight = ref(["READ", "WRITE"]);
const oldAccess = ref("");
const isVisible = ref([]);
const isChangeAccess = ref(false);

watch(
  () => boardStore.collabs,
  () => {
    boardStore.collabs.forEach((task, index) => {
      setTimeout(() => {
        isVisible.value[index] = true;
      }, (index + 1) * 150);
    });
  },
  { deep: true }
);

watch(
  () => oldAccess.value,
  (newSelect, oldSelect) => {
    if (newSelect !== oldSelect) {
      isChangeAccess.value = true;
    } else {
      isChangeAccess.value = false;
    }
  }
);

const isOwner = computed(() => {
  return boardStore.currentBoard.owner.id === userStore.authToken?.oid;
});
</script>

<template>
  <div class="flex flex-col w-full h-screen">
    <div class="flex flex-col items-center h-full gap-4 mt-2">
      <div
        class="flex flex-row w-[95%] mt-5 max-sm:w-full max-sm:px-2 border-b border-gray-300"
      >
        <div
          class="m-[2px] my-2 flex sm:items-center items-end w-full flex-wrap"
        >
          <router-link :to="{ name: 'task' }">
            <div
              class="itbkk-board-name text-gray-600 text-2xl max-md:text-xl max-sm:text-sm font-bold"
            >
              {{
                isOwner
                  ? boardStore.currentBoard.name + " Personal's Board"
                  : boardStore.currentBoard.name + "Collaborate's Board"
              }}
            </div>
          </router-link>
          <div class="flex items-center">
            <div
              class="flex items-center mr-2 mt-2 text-gray-600 hover:text-gray-800 rotate-180"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="h-5 w-5"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M15 19l-7-7 7-7"
                />
              </svg>
            </div>
            <div
              class="text-gray-900 text-2xl max-md:text-xl max-sm:text-sm font-bold"
            >
              Collaborater
            </div>
          </div>
        </div>
      </div>
      <div class="flex flex-row w-[95%] max-sm:w-full max-sm:px-2">
        <div class="flex items-end w-full justify-end">
          <div
            class="flex sm:flex-row flex-col sm:items-center items-end gap-1 sm:gap-4"
          >
            <div
              :class="isOwner ? '' : 'tooltip tooltip-bottom tooltip-hover'"
              data-tip="You need to be board owner to perform this action."
            >
              <button
                class="itbkk-button-add bg-gray-800 hover:bg-gray-500 text-white font-bold py-2 px-4 rounded-lg text-[0.9rem] max-sm:text-[0.89rem]"
                :disabled="!isOwner"
                :class="isOwner ? 'cursor-pointer' : 'cursor-not-allowed'"
                @click="$emit('addCollab', true)"
              >
                Add Collaborater
              </button>
            </div>
          </div>
        </div>
      </div>

      <div
        class="flex justify-center gap-3 w-[95%] max-sm:w-full max-sm:px-2 max-sm:gap-1"
      >
        <!-- Status Table -->
        <TaskTableLoading v-if="showLoading" class="w-full" />

        <div v-else class="w-full rounded-md shadow-xl">
          <div
            class="flex justify-center gap-3 w-[100%] max-sm:w-full max-sm:px-2 max-sm:gap-1"
          >
            <table class="w-full rounded-md">
              <thead
                class="text-xs text-gray-700 uppercase bg-gray-50 w-full rounded-t-md"
              >
                <tr class="flex w-full rounded-t-md">
                  <th
                    class="px-6 py-4 w-[5%] max-lg:hidden border-l-4 rounded-tl-md"
                  >
                    No.
                  </th>
                  <th
                    class="px-6 py-4 w-[40%] max-lg:w-[30%] max-lg:px-2 max-lg:py-3 max-lg:border-l-4"
                  >
                    Name
                  </th>
                  <th
                    class="px-6 py-4 w-[30%] max-lg:w-[45%] max-lg:px-2 max-lg:py-3"
                  >
                    E-mail
                  </th>
                  <th
                    class="px-4 py-4 w-[20%] max-lg:w-[25%] max-lg:px-2 max-lg:py-3 rounded-tr-md"
                  >
                    Acess Right
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
                  class="itbkk-item flex w-full items-center justify-center bg-white border-l-4 border-b"
                  v-for="(collab, index) in boardStore.collabs"
                  :key="index"
                  :class="{ 'slide-in': isVisible[index] }"
                >
                  <td class="px-6 py-4 max-lg:hidden w-[5%]">
                    {{ index + 1 }}
                  </td>

                  <td
                    class="h-full w-[40%] px-6 py-4 max-lg:w-[30%] max-lg:px-2 max-lg:py-3 hover:bg-neutral-100"
                  >
                    <div
                      class="cursor-pointer h-full w-full items-center flex break-all"
                    >
                      <span class="itbkk-collab-name">{{ collab.name }}</span>

                      <!--Loading, Pending -->
                      <span
                        v-if="!collab.oid"
                        class="ml-2 px-2 py-1 text-xs font-bold text-gray-700 bg-gray-300 rounded"
                      >
                        Processing...
                      </span>

                      <span
                        v-else-if="collab.isPending"
                        class="ml-2 px-2 py-1 text-xs font-bold text-yellow-800 bg-yellow-200 rounded"
                      >
                        Pending Invite
                      </span>
                    </div>
                  </td>

                  <td
                    class="w-[30%] px-6 py-4 max-lg:[45%] max-lg:px-2 max-lg:py-3 break-all"
                    :class="
                      collab.email === null || collab.email?.length === 0
                        ? 'italic text-gray-600'
                        : ''
                    "
                  >
                    <span class="itbkk-email">
                      {{ collab.email }}
                    </span>
                  </td>
                  <td
                    class="w-[20%] px-6 py-4 max-lg:[45%] max-lg:px-2 max-lg:py-3 break-all"
                  >
                    <div
                      :class="
                        isOwner ? '' : 'tooltip tooltip-bottom tooltip-hover'
                      "
                      data-tip="You need to be board owner to perform this action."
                    >
                      <div class="itbkk-acess-right">
                        <select
                          class="itbkk-collab border-2 border-gray-500 w-[10rem] h-[30px] rounded-lg"
                          v-model="collab.accessRight"
                          @click="oldAccess = collab.accessRight"
                          @change="
                            $emit(
                              'changeAccess',
                              index,
                              isChangeAccess,
                              collab,
                              oldAccess
                            )
                          "
                          :disabled="!isOwner || !collab.oid"
                          :class="
                            isOwner && collab.oid
                              ? 'cursor-pointer'
                              : 'cursor-not-allowed disabled'
                          "
                        >
                          <option v-for="access in accessRight">
                            {{ access }}
                          </option>
                        </select>
                      </div>
                    </div>
                  </td>
                  <td
                    class="w-[20%] px-4 py-4 max-lg:w-[25%] max-lg:px-2 max-lg:py-3 flex justify-center items-center"
                  >
                    <div
                      @click="
                        (isChangeAccess = false),
                          $emit('removeCollab', index, isChangeAccess, collab)
                      "
                      class="flex flex-row gap-4 max-sm:flex-col"
                    >
                      <div>
                        <div
                          :class="
                            isOwner
                              ? ''
                              : 'tooltip tooltip-bottom tooltip-hover'
                          "
                          data-tip="You need to be board owner to perform this action."
                        >
                          <div
                            v-if="collab.isPending === false"
                            class="itbkk-collab-remove text-white fill-rose-300"
                            :disabled="!isOwner || !collab.oid"
                            :class="
                              isOwner && collab.oid
                                ? 'cursor-pointer'
                                : 'cursor-not-allowed disabled'
                            "
                          >
                            <DeleteIcon
                              :class="
                                isOwner && collab.oid
                                  ? ' hover:fill-red-500'
                                  : ' hover:fill-rose-300'
                              "
                            />
                          </div>
                          <button
                            v-else
                            class="text-white px-2 py-1 rounded"
                            :disabled="!isOwner || !collab.oid"
                            :class="
                              isOwner && collab.oid
                                ? 'cursor-pointer bg-red-500'
                                : 'cursor-not-allowed disabled bg-gray-300'
                            "
                          >
                            Cancel
                          </button>
                        </div>
                      </div>
                    </div>
                  </td>
                </tr>

                <div
                  v-if="showErrorMSG"
                  class="flex h-[100%] items-center w-full"
                >
                  <div
                    class="flex items-center justify-center max-lg:border-l-4 h-full w-full"
                  >
                    <span class="text-lg text-slate-700 opacity-50">
                      Missing Load Resource
                    </span>
                  </div>
                </div>

                <div
                  v-else-if="boardStore.collabs.length == 0 && !showLoading"
                  class="flex h-[100%] items-center"
                >
                  <span class="text-lg text-slate-700 opacity-50">
                    No Collaborator
                  </span>
                </div>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
