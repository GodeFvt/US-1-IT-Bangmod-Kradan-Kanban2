<script setup>
import { computed, ref, watch } from "vue";
import { toFormatDate } from "../../lib/utill";
import EditTaskIcon from "../icon/EditTaskIcon.vue";
import CloseIcon from "../icon/CloseIcon.vue";
import { useStatusStore } from "../../stores/statuses.js";
import { useRouter } from "vue-router";
import { validateSizeInput } from "../../lib/utill.js";
const router = useRouter();
defineEmits(["userAction", "addEdit"]);
const props = defineProps({
  task: {
    type: Object,
    required: true,
  },
  isEdit: {
    type: Boolean,
    default: false,
  },
  showLoading: {
    type: Boolean,
  },
  allTaskLimit: {
    type: Array,
  },
});
const statusStore = useStatusStore();
const duplicateTask = ref({});
const editMode = ref(props.isEdit);
const isLimit = computed(() => statusStore.isLimit);
const allTaskLimit = ref(props.allTaskLimit);
const maximumTask = computed(() => statusStore.maximumTask);
const noOftask = computed(() => statusStore.noOftask);

watch(
  () => props.task,
  (newTask) => {
    allTaskLimit.value = props.allTaskLimit;
    for (const key in newTask) {
      if (newTask[key] === null) {
        newTask[key] = "";
      }
    }
    duplicateTask.value = { ...newTask };
    duplicateTask.value.status = { ...newTask.status };
  },
  { immediate: true }
);

const isTaskChanged = computed(() => {
  return JSON.stringify(props.task) === JSON.stringify(duplicateTask.value);
});

const createDate = computed(() =>
  toFormatDate(new Date(props.task.createdOn).toLocaleString("en-GB"))
);
const updateDate = computed(() =>
  toFormatDate(new Date(props.task.updatedOn).toLocaleString("en-GB"))
);

const countTitle = computed(() => {
  return duplicateTask.value.title?.trim()?.length;
});
const countDescription = computed(() => {
  return duplicateTask.value.description?.trim()?.length;
});
const countAssignees = computed(() => {
  return duplicateTask.value.assignees?.trim()?.length;
});

const validate = ref({ title: {}, description: {}, assignees: {} });

const disabledSave = computed(() => {
  const arrStyle = validateSizeInput(
    { propName: "Title", propLenght: countTitle.value, size: 100 },
    { propName: "Description", propLenght: countDescription.value, size: 500 },
    { propName: "Assignees", propLenght: countAssignees.value, size: 30 }
  );

  validate.value.title = arrStyle[0];
  validate.value.description = arrStyle[1];
  validate.value.assignees = arrStyle[2];

  if (
    isTaskChanged.value ||
    duplicateTask.value.title === null ||
    countTitle.value <= 0
  ) {
    return true;
  } else if (
    validate.value.title.boolean ||
    validate.value.description.boolean ||
    validate.value.assignees.boolean
  ) {
    return true;
  } else if (limitThisTask.value) {
    return true;
  } else {
    return false;
  }
});

function textShow(text) {
  if (text === null) {
    return "italic text-gray-600";
  }
}
function edit(taskId) {
  editMode.value = true;
  router.push({ name: "EditTask", params: { taskId: taskId } });
}
const limitThisTask = computed(() => {
  if (isLimit.value) {
    if (
      duplicateTask.value.status.name !== "No Status" &&
      duplicateTask.value.status.name !== "Done"
    ) {
      //ไม่แสดงข้อความเตือนถ้า เลทิก สเตตัสเดิม limit
      if (
        duplicateTask.value.status.name === props.task.status.name &&
        noOftask.value[duplicateTask.value.status.name] === maximumTask.value
      ) {
        return false;
      }
      // >= เพราะ ถ้ามีอยู่ พอดีที่ limit ไว้ แล้ว มีการ add edit แล้วเท่ากับ ค่า limit แล้วจะต้องไม่สามารถทำได้
      else if (
        noOftask.value[duplicateTask.value.status.name] >= maximumTask.value
      ) {
        return true;
      } else {
        return false;
        // return allTaskLimit.value.some((e) => e?.name === duplicateTask.value.status.name);
      }
    } else {
      return false;
    }
  }
});
</script>

<template>
  <div
    class="absolute left-0 right-0 m-auto top-0 bg-black h-screen w-screen bg-opacity-50 z-50"
  >
    <div class="flex h-full items-center justify-center">
      <div
        class="itbkk-modal-task flex flex-col justify-start rounded-md bg-white h-[60%] max-h-800px w-[70rem] shadow-md relative overflow-auto"
      >
        <!-- Close Button -->
        <div class="w-full flex justify-end">
          <div
            class="cursor-pointer text-error text-2xl text-wrap pt-5 pr-5"
            @click="$emit('userAction', false)"
          >
            <CloseIcon />
          </div>
        </div>

        <div class="flex flex-col justify-start bg-white w-full">
          <!-- Header -->
          <div
            class="flex flex-row gap-5 items-end w-full sm:h-[9%] px-9 mt-2 rounded-b-lg"
          >
            <textarea
              :readonly="!editMode"
              type="text"
              name="title"
              id="title"
              :class="
                editMode
                  ? countTitle <= 0
                    ? 'border border-red-400 rounded-md p-1 focus:outline-red-500'
                    : validate.title.style
                  : 'border-b'
              "
              class="itbkk-title text-xl rounded-md p-1 font-semibold break-all h-full w-[85%] border border-gray-800 resize-none read-only:focus:outline-none placeholder:font-normal placeholder:italic"
              @dblclick="edit(task.id)"
              v-model="duplicateTask.title"
              placeholder="Insert Title Here"
            >
            {{ duplicateTask.title }}
            </textarea>
            <div
              v-show="$route.path !== '/task/add'"
              class="cursor-pointer text-2xl hover:bg-gray-200 rounded-full"
              @click="edit(task.id)"
            >
              <EditTaskIcon />
            </div>
          </div>
          <div
            class="px-9 flex flex-col justify-end items-end w-[85%]"
            v-if="editMode"
          >
            <span class="text-xs border-0" :class="validate.title.style"
              >{{ countTitle }}/100</span
            >
            <span class="text-xs text-red-500" v-if="validate.title.boolean">
              {{ validate.title.msg }}</span
            >
          </div>
          <!-- Content -->
          <div class="w-full pl-9 pr-9 mt-4 h-full">
            <div
              class="grid sm:grid-cols-4 sm:grid-rows-2 grid-cols-1 grid-rows-4 gap-4 m-auto h-full"
            >
              <!-- Description -->
              <div
                class="sm:row-span-2 sm:col-span-3 col-span-1 row-span-2 flex flex-col"
              >
                <div class="font-bold text-sm">Description</div>
                <textarea
                  class="itbkk-description read-only:focus:outline-none placeholder:text-gray-500 placeholder:italic break-all mt-2 p-2 rounded-lg border border-gray-800 h-full resize-none text-black"
                  :class="
                    (editMode ? validate.description.style : 'border-b',
                    textShow(duplicateTask.description))
                  "
                  @dblclick="edit(task.id)"
                  :readonly="!editMode"
                  type="text"
                  name="description"
                  id="description"
                  v-model="duplicateTask.description"
                  placeholder="No Description Provided"
                >
                {{
                    duplicateTask.description === null ||
                    duplicateTask.description?.length === 0
                      ? "No Description Provided"
                      : duplicateTask.description
                  }}
              </textarea
                >
                <div
                  class="flex flex-col justify-end items-end w-[100%] border-0"
                  v-if="editMode"
                >
                  <span
                    class="text-xs border-0"
                    :class="validate.description.style"
                    >{{ countDescription }}/500</span
                  >
                  <span
                    class="text-x text-red-500"
                    v-if="validate.description.boolean"
                  >
                    {{ validate.description.msg }}</span
                  >
                </div>
              </div>

              <!-- Status -->
              <div
                class="sm:row-span-2 col-span-1 row-span-2 flex flex-col h-full"
              >
                <div class="font-bold text-sm">Assignees</div>
                <textarea
                  class="itbkk-assignees read-only:focus:outline-none placeholder:text-gray-500 placeholder:italic break-all mt-2 p-2 rounded-lg border border-gray-800 h-16 resize-none text-black"
                  :class="
                    (editMode ? validate.assignees.style : 'border-b',
                    textShow(duplicateTask.assignees))
                  "
                  :readonly="!editMode"
                  @dblclick="edit(task.id)"
                  type="text"
                  name="assignees"
                  id="assignees"
                  v-model="duplicateTask.assignees"
                  placeholder="Unassigned"
                >
                {{
                    duplicateTask.assignees === null ||
                    duplicateTask.assignees?.length === 0
                      ? "Unassigned"
                      : duplicateTask.assignees
                  }}
                </textarea>
                <div
                  class="flex flex-col justify-end items-end w-[100%] border-0"
                  v-if="editMode"
                >
                  <span
                    class="text-xs border-0"
                    :class="validate.assignees.style"
                    >{{ countAssignees }}/30</span
                  >
                  <span
                    class="text-xs text-red-500"
                    v-if="validate.assignees.boolean"
                  >
                    {{ validate.assignees.msg }}</span
                  >
                </div>
                <div>
                  <p>
                    Limit :
                    <span :class="isLimit ? 'text-green-600	' : 'text-rose-600'">
                      {{ isLimit ? "On" : "Off" }}
                    </span>
                  </p>
                </div>
                <label for="category" class="font-bold text-sm mt-2"
                  >Status</label
                >
                <select
                  id="category"
                  :disabled="!editMode"
                  v-model="duplicateTask.status.name"
                  class="itbkk-status mt-2 bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-primary-500 focus:border-primary-500 block w-full p-2.5"
                >
                  <option
                    v-for="status in statusStore.allStatus"
                    :selected="
                      duplicateTask.status.name === `${status.name}` ||
                      duplicateTask.status.name === null
                    "
                    :value="`${status.name}`"
                  >
                    {{ status.name }}
                  </option>
                </select>
                <div class="text-rose-600" v-if="limitThisTask && editMode">
                  The status {{ duplicateTask.status.name }} will have too many
                  tasks. Please make progress and update status of existing
                  tasks first.
                </div>
                <div class="h-[17vh]">
                  <div v-show="!editMode">
                    <div class="font-bold text-sm mt-2">Timezone</div>
                    <div class="itbkk-timezone mt-2">
                      {{ Intl.DateTimeFormat().resolvedOptions().timeZone }}
                    </div>

                    <div class="font-bold text-sm">Created On</div>
                    <div class="itbkk-created-on mt-2">
                      {{ createDate }}
                    </div>

                    <div class="font-bold text-sm">Updated On</div>
                    <div class="itbkk-updated-on mt-2">
                      {{ updateDate }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Save Buttons, Close Buttons -->
        <div
          class="w-full flex justify-end mb-5"
          :class="editMode ? 'block ' : 'hidden'"
        >
          <div class="flex mr-10 gap-2">
            <button
              :disabled="disabledSave"
              class="itbkk-button-confirm text-white inline-flex items-center focus:ring-4 focus:outline-none font-medium rounded-lg text-sm px-5 py-2.5 text-center bg-gray-300"
              :class="
                disabledSave
                  ? 'cursor-not-allowed disabled'
                  : 'cursor-pointer bg-green-700 hover:bg-green-800 focus:ring-green-300'
              "
              @click="
                $emit('userAction', false), $emit('addEdit', duplicateTask)
              "
            >
              SAVE
            </button>
            <button
              class="itbkk-button-cancel text-white inline-flex items-center bg-red-700 hover:bg-red-800 focus:ring-4 focus:outline-none focus:ring-red-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center"
              @click="$emit('userAction', false)"
            >
              CANCEL
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@media not all and (min-height: 830px) {
  .max-h-800px {
    height: 65%;
  }
}
</style>
