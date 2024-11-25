<script setup>
import { computed, ref, watch } from "vue";
import EditTaskIcon from "../icon/EditTaskIcon.vue";
import CloseIcon from "../icon/CloseIcon.vue";
import { useRouter } from "vue-router";
import { validateSizeInput } from "../../lib/utill.js";
import { useStatusStore } from "../../stores/statuses.js";
import { useBoardStore } from "../../stores/boards.js";

const statusStore = useStatusStore();
defineEmits(["userAction", "addEdit"]);
const props = defineProps({
  status: {
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
});
const router = useRouter();
const duplicateStatus = ref({});
const editMode = ref(props.isEdit);
const validate = ref({ name: {}, description: {} });
const alltask = ref([]);
const boardStore = useBoardStore();

watch(
  () => props.status,
  (newStatus) => {
    alltask.value = statusStore.allStatus;
    for (const key in newStatus) {
      if (newStatus[key] === null) {
        newStatus[key] = "";
      }
    }
    duplicateStatus.value = { ...newStatus };
  },
  { immediate: true }
);

const isStatusChanged = computed(() => {
  // FALSE คือเปลี่ยน , true ไม่เปลี่ยน
  return JSON.stringify(props.status) === JSON.stringify(duplicateStatus.value);
});

const countStatusName = computed(() => {
  return duplicateStatus.value.name?.trim()?.length;
});
const countStatusDescription = computed(() => {
  return duplicateStatus.value.description?.trim()?.length;
});

function textShow(text) {
  if (text === null) {
    return "italic text-gray-600";
  }
}
function edit(statusId) {
  if (boardStore.isCanEdit) {
    if (props.status.name !== "No Status" && props.status.name !== "Done") {
      editMode.value = !editMode.value;
      router.push({ name: "EditStatus", params: { statusId: statusId } });
    }
  }
}

const duplicateName = computed(() => {
  if (isStatusChanged.value === false) {
    return alltask.value
      .filter((e) => e.name !== props.status.name)
      .some(
        (e) => e.name.toLowerCase() === duplicateStatus.value.name.toLowerCase()
      );
  } else {
    return false;
  }
});

const disabledSave = computed(() => {
  const arrStyle = validateSizeInput(
    { propName: "Status name", propLenght: countStatusName.value, size: 50 },
    {
      propName: "Status Description",
      propLenght: countStatusDescription.value,
      size: 200,
    }
  );

  validate.value.name = arrStyle[0];
  validate.value.description = arrStyle[1];

  if (
    isStatusChanged.value ||
    duplicateStatus.value.title === null ||
    countStatusName.value <= 0
  ) {
    return true;
  } else if (
    validate.value.name.boolean ||
    validate.value.description.boolean
  ) {
    return true;
  } else {
    return false;
  }
});
</script>

<template>
  <div
    class="absolute left-0 right-0 m-auto top-0 bg-black h-screen w-screen bg-opacity-50 z-50"
  >
    <div class="flex h-full items-center justify-center">
      <div
        class="itbkk-modal-status flex flex-col justify-start rounded-md bg-white h-[55%] max-h-800px w-[50rem] shadow-md relative overflow-auto"
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

        <!-- Content -->
        <div class="flex flex-col w-full pl-9 pr-9 mt-1">
          <!-- Status Name -->
          <div class="flex flex-col">
            <div class="flex items-center">
              <div class="font-bold text-xl">
                <span>Status Name</span>
              </div>
              <div
                :class="
                  boardStore.isCanEdit
                    ? ''
                    : 'tooltip tooltip-bottom tooltip-hover '
                "
                data-tip="You need to be board owner to perform this action."
              >
                <div
                  v-show="
                    status.name !== 'No Status' &&
                    status.name !== 'Done' &&
                    !editMode
                  "
                  @click="edit(status.id)"
                  class="ml-1"
                  :class="
                    boardStore.isCanEdit
                      ? 'cursor-pointer'
                      : 'cursor-not-allowed disabled'
                  "
                >
                  <EditTaskIcon class="w-[1rem] h-[1rem]" />
                </div>
              </div>
            </div>
            <div
              v-if="showLoading"
              class="animate-pulse bg-gray-300 mt-2 p-2 rounded-md h-[4rem]"
            ></div>
            <textarea
              v-else
              class="itbkk-status-name read-only:focus:outline-none placeholder:text-gray-500 placeholder:italic break-all mt-2 p-2 rounded-lg border border-gray-800 h-full resize-none"
              :class="
                (editMode
                  ? countStatusName <= 0
                    ? 'border border-red-400 rounded-md p-1 focus:outline-red-500'
                    : validate.name.style
                  : 'border-b',
                textShow(duplicateStatus.name))
              "
              :readonly="!editMode"
              type="text"
              name="statusName"
              id="statusName"
              v-model="duplicateStatus.name"
              placeholder="No Name Provided"
            >
                {{
                duplicateStatus.name === null ||
                duplicateStatus.name?.length === 0
                  ? "No Name Provided"
                  : duplicateStatus.name
              }}
              </textarea
            >
            <div
              class="flex flex-col justify-end items-end w-[100%]"
              v-if="editMode"
            >
              <span class="text-xs border-0" :class="validate.name.style"
                >{{ countStatusName }}/50</span
              >

              <div>
                <span class="text-xs text-red-500" v-if="validate.name.boolean">
                  {{ validate.name.msg }}</span
                >
                <span class="text-xs text-red-500" v-if="duplicateName">
                  Status name must be uniques, please choose another name.
                </span>
              </div>
            </div>
          </div>
          <!-- Status -->
          <div class="flex flex-col">
            <div class="font-bold text-lg">Description</div>
            <div
              v-if="showLoading"
              class="animate-pulse bg-gray-300 mt-2 p-2 rounded-md h-[6rem]"
            ></div>
            <textarea
              v-else
              class="itbkk-status-description read-only:focus:outline-none placeholder:text-gray-500 placeholder:italic break-all mt-2 p-2 rounded-lg border border-gray-800 h-24 resize-none"
              :class="
                (editMode ? validate.description.style : 'border-b',
                textShow(duplicateStatus.description))
              "
              :readonly="!editMode"
              type="text"
              name="description"
              id="description"
              v-model="duplicateStatus.description"
              placeholder="No Description Provided"
            >
                {{
                duplicateStatus.description === null ||
                duplicateStatus.description?.length === 0
                  ? "No Description Provided"
                  : duplicateStatus.description
              }}
                </textarea
            >
            <div
              class="flex flex-col justify-end items-end w-[100%]"
              v-if="editMode"
            >
              <span class="text-xs border-0" :class="validate.description.style"
                >{{ countStatusDescription }}/200</span
              >
              <span
                class="text-x text-red-500"
                v-if="validate.description.boolean"
              >
                {{ validate.description.msg }}</span
              >
            </div>
          </div>
          <div class="flex flex-col">
            <div class="font-bold text-sm mt-2">Color</div>
            <div class="mt-2">
              <input
                type="color"
                v-model="duplicateStatus.color"
                :disabled="!editMode"
              />
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
              :disabled="disabledSave || duplicateName"
              class="itbkk-button-confirm text-white inline-flex items-center focus:ring-4 focus:outline-none font-medium rounded-lg text-sm px-5 py-2.5 text-center bg-gray-300"
              :class="
                disabledSave || duplicateName
                  ? 'cursor-not-allowed disabled'
                  : 'cursor-pointer bg-green-700 hover:bg-green-800 focus:ring-green-300'
              "
              @click="
                $emit('userAction', false), $emit('addEdit', duplicateStatus)
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
@media not all and (max-height: 850px) {
  .max-h-800px {
    height: 50%;
  }
}
</style>
