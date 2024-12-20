<script setup>
import { ref, watch, onMounted, onUnmounted } from "vue";
import MoreActionIcon from "../icon/MoreActionIcon.vue";
import Action from "../Action.vue";
import SortIcon from "../icon/SortIcon.vue";
import TaskTableLoading from "../loading/TaskTableLoading.vue";
import TaskCardLoading from "../loading/TaskCardLoading.vue";
import { useStatusStore } from "../../stores/statuses.js";
import { useUserStore } from "../../stores/user.js";
import { useBoardStore } from "../../stores/boards.js";
import { getFilteredTask } from "../../lib/fetchUtill.js";
import { useRoute, useRouter } from "vue-router";
import AuthzPopup from "../AuthzPopup.vue";
import EditIcon from "../icon/EditIcon.vue";
import DeleteIcon from "../icon/DeleteIcon.vue";
import TaskTable from "./TaskTable.vue";
import TaskCard from "./TaskCard.vue";

defineEmits(["removeTask"]);
const props = defineProps({
  statusFilter: {
    type: Array,
    default: ["All"],
  },
  allTask: {
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
  allTaskLimit: {
    type: Array,
  },
});

const router = useRouter();
const route = useRoute();
const sortType = ref("default");
const statusStore = useStatusStore();
const taskFiltered = ref([]);
const windowWidth = ref(window.innerWidth);
const showPopUp = ref(false);
const isVisible = ref([]);
const userStore = useUserStore();
const boardStore = useBoardStore();

watch(
  () => props.allTask,
  () => {
    props.allTask.forEach((task, index) => {
      setTimeout(() => {
        isVisible.value[index] = true;
      }, (index + 1) * 150);
    });
  },
  { deep: true }
);
function getTextColor(hex) {
  if (hex.length !== 7 || hex[0] !== "#") {
    return "#000000";
  }

  const rgb = hex
    .substring(1)
    .match(/.{1,2}/g)
    .map((x) => parseInt(x, 16));

  const avgColor = (rgb[0] + rgb[1] + rgb[2]) / 3;

  return avgColor >= 160 ? "#000000" : "#FFFFFF";
}

watch(
  [() => props.statusFilter, () => props.allTask, () => sortType.value],
  async ([statusFilter, allTask]) => {
    if (statusFilter.length === 0) {
      taskFiltered.value = allTask;
    } else {
      const res = await getFilteredTask(route.params.boardId, statusFilter);
      if (res === 401) {
        showPopUp.value = true;
      } else {
        taskFiltered.value = res;
      }
    }
    sortByStatus();
  },
  { immediate: true, deep: true }
);

watch(windowWidth, () => {
  taskFiltered.value.forEach((task) => updateBorderStyle(task));
});

function switchSortType() {
  if (sortType.value === "default") {
    sortType.value = "asc";
  } else if (sortType.value === "asc") {
    sortType.value = "desc";
  } else {
    sortType.value = "default";
  }
}

function sortByStatus() {
  switch (sortType.value) {
    case "default":
      taskFiltered.value.sort((a, b) => a.id - b.id);
      break;
    case "asc":
      taskFiltered.value.sort((a, b) =>
        a.status.name.localeCompare(b.status.name)
      );
      break;
    case "desc":
      taskFiltered.value.sort((a, b) =>
        b.status.name.localeCompare(a.status.name)
      );
      break;
    default:
      break;
  }
}

function updateBorderStyle(name) {
  if (windowWidth.value <= 780) {
    return {
      "border-color": statusStore.getColorStatus(name),
      "border-bottom-color": "rgb(229 231 235 / 0.5)",
    };
  }
}

function editTask(id) {
  if (boardStore.isCanEdit) {
    router.push({ name: "EditTask", params: { taskId: id } });
  }
}

onMounted(() => {
  const handleResize = () => {
    windowWidth.value = window.innerWidth;
  };
  window.addEventListener("resize", handleResize);
  handleResize();
});

onUnmounted(() => {
  const handleResize = () => {
    windowWidth.value = window.innerWidth;
  };
  window.addEventListener("resize", handleResize);
  handleResize();
});
</script>

<template>
  <TaskTableLoading
    v-if="showLoading && userStore.theme === 'table'"
    class="w-full rounded-md"
  />
  <TaskCardLoading
    v-else-if="showLoading && userStore.theme === 'card'"
    class="w-full"
  />
  <div v-else class="w-full rounded-md">
    <!-- Table -->
    <div v-if="userStore.theme === 'table'" class="shadow-xl">
      <TaskTable :taskFiltered="taskFiltered">
        <template #sortStatus>
          <div class="itbkk-status-sort flex gap-1" @click="switchSortType">
            <span>Status</span>

            <SortIcon sortType="default" v-if="sortType === 'default'" />
            <SortIcon sortType="asc" v-else-if="sortType === 'asc'" />
            <SortIcon sortType="desc" v-else-if="sortType === 'desc'" />
          </div>
        </template>
        <template #tr-item>
          <tr
            class="itbkk-item task-row-wrapper flex w-full items-center justify-center border-l-4 border-b"
            :style="updateBorderStyle(task.status.name)"
            v-for="(task, index) in taskFiltered"
            :key="index"
            :class="{ 'slide-in': isVisible[index] }"
          >
            <td class="px-6 py-4 max-md:hidden w-[5%]">
              {{ index + 1 }}
            </td>

            <td
              class="h-full w-[30%] px-6 py-4 max-md:w-[65%] max-md:px-2 max-md:py-3 hover:bg-neutral-100"
            >
              <router-link
                :to="{ name: 'TaskDetail', params: { taskId: task.id } }"
              >
                <div
                  class="cursor-pointer h-full w-full items-center flex break-all"
                >
                  <span class="itbkk-title font-medium text-gray-900">
                    {{ task.title }}
                  </span>
                </div>
              </router-link>
            </td>
            <td
              class="w-[20%] px-6 py-4 break-all max-md:w-[40%] max-md:px-2 max-md:py-3 text-center"
              :class="
                task.assignees === null || task.assignees?.length === 0
                  ? 'italic text-gray-600'
                  : ''
              "
            >
              <span class="itbkk-assignees">
                {{
                  task.assignees === null || task.assignees?.length === 0
                    ? "Unassigned"
                    : task.assignees
                }}
              </span>
            </td>

            <td
              class="w-[20%] px-6 py-4 break-all max-md:w-[40%] max-md:px-2 max-md:py-3 text-center"
            >
              <span class="itbkk-attachments">{{
                task.attachments === null || task.attachments?.length === 0
                  ? "-"
                  : task.attachments?.length
              }}</span>
            </td>

            <td class="w-[10%] px-2 py-4 max-md:hidden break-all">
              <div
                class="text-slate-50 rounded-md p-[0.1rem] text-center"
                :style="{
                  'background-color': statusStore.getColorStatus(
                    task.status.name
                  ),
                  color: getTextColor(
                    statusStore.getColorStatus(task.status.name)
                  ),
                }"
              >
                <span class="itbkk-status font-bold">
                  {{ task.status.name }}
                </span>
              </div>
            </td>
            <td
              class="itbkk-status w-[20%] px-4 py-4 max-md:w-[30%] max-md:px-2 max-md:py-3 cursor-pointer flex justify-center items-center"
            >
              <div
                class="itbkk-button-action flex flex-row gap-4 max-sm:flex-col"
              >
                <Action
                  @edit="editTask(task.id)"
                  @remove="$emit('removeTask', index)"
                ></Action>
              </div>
            </td>
          </tr>
        </template>

        <template #showErrorMSG>
          <div v-if="showErrorMSG" class="flex h-[100%] items-center w-full">
            <div
              class="flex items-center justify-center max-md:border-l-4 h-full w-full"
            >
              <span class="text-lg text-slate-700 opacity-50">
                Missing Load Resource
              </span>
            </div>
          </div>
        </template>
        <template #NoRecordFound>
          <div
            v-if="taskFiltered.length === 0 && !showLoading"
            class="flex h-[100%] items-center"
          >
            <span class="text-lg text-slate-700 opacity-50">
              No Record Found
            </span>
          </div>
        </template>
      </TaskTable>
    </div>
    <!-- Card -->
    <div v-else class="card">
      <TaskCard>
        <template #eachStatus>
          <div
            class="w-[319px] h-[38rem] flex flex-col border-t-[5px] bg-gray-100 rounded-lg shadow-md shrink-0 snap-always snap-center"
            v-for="status in statusStore.allStatus"
            :style="[
              { 'border-color': statusStore.getColorStatus(status.name) },
            ]"
          >
            <div
              class="font-medium bg-white min-h-min pb-2 pt-2 text-center border-b-2"
            >
              <p>{{ status.name }}</p>
            </div>

            <div
              class="scroll-ml-4 hover:overflow-y-auto lg:overflow-y-auto 2xl:overflow-y-hidden overflow-x-hidden touch-auto gap-3 p-4 flex flex-col"
            >
              <div
                v-for="(task, index) in taskFiltered.filter(
                  (e) => e.status.name === status.name
                )"
                class="rounded-lg p-0 drop-shadow-md"
              >
                <div class="bg-slate-50 rounded-lg p-4">
                  <p class="font-medium">{{ task.title }}</p>
                  <div class="flex flex-col gap-1">
                    <span
                      class="itbkk-assignees"
                      :class="
                        task.assignees === null || task.assignees?.length === 0
                          ? 'italic text-gray-600'
                          : ''
                      "
                    >
                      {{
                        task.assignees === null || task.assignees?.length === 0
                          ? "Unassigned"
                          : task.assignees
                      }}</span
                    >

                    <span class="itbkk-attachments flex">
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="22"
                        height="22"
                        viewBox="0 0 24 24"
                        class="rotate-45 text-gray-500"
                      >
                        <path
                          fill="currentColor"
                          d="M18 15.75q0 2.6-1.825 4.425T11.75 22t-4.425-1.825T5.5 15.75V6.5q0-1.875 1.313-3.187T10 2t3.188 1.313T14.5 6.5v8.75q0 1.15-.8 1.95t-1.95.8t-1.95-.8t-.8-1.95V6h2v9.25q0 .325.213.538t.537.212t.538-.213t.212-.537V6.5q-.025-1.05-.737-1.775T10 4t-1.775.725T7.5 6.5v9.25q-.025 1.775 1.225 3.013T11.75 20q1.75 0 2.975-1.237T16 15.75V6h2z"
                        />
                      </svg>
                      
                      {{
                        task.attachments === null ||
                        task.attachments?.length === 0
                          ? "-"
                          : task.attachments?.length
                      }}</span
                    >
                  </div>
                  <div class="action justify-self-end">
                    <div
                      class="itbkk-button-action flex flex-row gap-2 max-sm:flex-col"
                    >
                      <router-link
                        :to="{
                          name: 'TaskDetail',
                          params: { taskId: task.id },
                        }"
                      >
                        <div class="rotate-90">
                          <svg
                            xmlns="http://www.w3.org/2000/svg"
                            width="25"
                            height="25"
                            viewBox="0 0 1024 1024"
                          >
                            <path
                              fill="currentColor"
                              d="M1014.64 969.04L703.71 656.207c57.952-69.408 
                        92.88-158.704 92.88-256.208c0-220.912-179.088-400-400-400s-400 
                        179.088-400 400s179.088 400 400 400c100.368 0 192.048-37.056 
                        262.288-98.144l310.496 312.448c12.496 12.497 32.769 
                        12.497 45.265 0c12.48-12.496 12.48-32.752 0-45.263zM396.59 
                        736.527c-185.856 0-336.528-150.672-336.528-336.528S210.734 
                        63.471 396.59 63.471s336.528 150.672 336.528 336.528S582.446
                        736.527 396.59 736.527"
                            />
                          </svg></div
                      ></router-link>
                      <Action
                        @edit="editTask(task.id)"
                        @remove="$emit('removeTask', index)"
                      ></Action>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
      </TaskCard>
    </div>
  </div>
  <AuthzPopup v-if="showPopUp" />
</template>

<style scoped>
/* .card::-webkit-scrollbar-track {
  border-radius: 10px;
  background-color: #0d0d0d;
}

.card::-webkit-scrollbar {
  width: 10px;
  background-color: #0d0d0d;
}

.card::-webkit-scrollbar-thumb {
  background-color: #bfbfbfe1;
}

.card::-webkit-scrollbar-thumb:active {
  border-radius: 5px;
  background-color: #f1f0f0;
} */
</style>
