<script setup>
import { ref, watch, onMounted, onUnmounted } from "vue";
import MoreActionIcon from "../icon/MoreActionIcon.vue";
import SortIcon from "../icon/SortIcon.vue";
import TaskTableLoading from "../loading/TaskTableLoading.vue";
import { useStatusStore } from "../../stores/statuses.js";
import { getFilteredTask } from "../../lib/fetchUtill.js";
import { useRoute, useRouter } from "vue-router";
import AuthzPopup from "../AuthzPopup.vue";
import EditIcon from "../icon/EditIcon.vue";
import DeleteIcon from "../icon/DeleteIcon.vue";

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
  <!-- Table -->
  <TaskTableLoading v-if="showLoading" class="w-full" />

  <div v-else class="w-full rounded-md shadow-xl">
    <table class="w-full rounded-md">
      <thead
        class="text-xs text-gray-700 uppercase bg-gray-50 w-full rounded-t-md"
      >
        <tr class="flex w-full rounded-t-md">
          <th class="px-6 py-4 w-[5%] max-md:hidden border-l-4 rounded-tl-md">
            No.
          </th>
          <th
            class="px-6 py-4 w-[40%] max-md:w-[65%] max-md:px-2 max-md:py-3 max-md:border-l-4 max-md:rounded-t-md"
          >
            Title
          </th>
          <th class="px-6 py-4 w-[25%] max-md:w-[40%] max-md:px-2 max-md:py-3">
            Assignees
          </th>
          <th
            @click="switchSortType"
            class="px-2 py-4 w-[10%] max-md:hidden cursor-pointer flex items-center justify-center"
          >
            <div class="itbkk-status-sort flex gap-1">
              <span>Status</span>
              <SortIcon sortType="default" v-if="sortType === 'default'" />
              <SortIcon sortType="asc" v-else-if="sortType === 'asc'" />
              <SortIcon sortType="desc" v-else-if="sortType === 'desc'" />
            </div>
          </th>
          <th
            class="px-4 py-4 w-[20%] max-md:w-[30%] max-md:px-2 max-md:py-3 rounded-tr-md"
          >
            Action
          </th>
        </tr>
      </thead>
      <tbody
        class="h-[60vh] max-sm:h-[50vh] flex flex-col items-center w-full overflow-x-hidden overflow-y-auto"
      >
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
            class="h-full w-[40%] px-6 py-4 max-md:w-[65%] max-md:px-2 max-md:py-3 hover:bg-neutral-100"
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
            class="w-[25%] px-6 py-4 break-all max-md:w-[40%] max-md:px-2 max-md:py-3"
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
              <router-link
                :to="{ name: 'EditTask', params: { taskId: task.id } }"
                ><div class="itbkk-button-edit cursor-pointer"><EditIcon /></div
              ></router-link>
              <div
                class="itbkk-button-delete cursor-pointer"
                @click="$emit('removeTask', index)"
              >
                <DeleteIcon />
              </div>
            </div>
          </td>
        </tr>

        <div v-if="showErrorMSG" class="flex h-[100%] items-center w-full">
          <div
            class="flex items-center justify-center max-md:border-l-4 h-full w-full"
          >
            <span class="text-lg text-slate-700 opacity-50">
              Missing Load Resource
            </span>
          </div>
        </div>

        <div
          v-else-if="taskFiltered.length === 0 && !showLoading"
          class="flex h-[100%] items-center"
        >
          <span class="text-lg text-slate-700 opacity-50">
            No Record Found
          </span>
        </div>
      </tbody>
    </table>
  </div>
  <AuthzPopup v-if="showPopUp" />
</template>

<style scoped></style>
