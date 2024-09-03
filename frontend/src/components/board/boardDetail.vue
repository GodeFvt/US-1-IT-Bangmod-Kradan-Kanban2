<script setup>
import { computed, ref, watch } from "vue";
import EditTaskIcon from "../icon/EditTaskIcon.vue";
import CloseIcon from "../icon/CloseIcon.vue";
import { useRouter } from "vue-router";
import { validateSizeInput } from "../../lib/utill.js";
import { useUserStore } from "../../stores/user.js";

const boardStore = useUserStore();
defineEmits(["userAction", "addEdit"]);
const props = defineProps({
  board: {
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
const duplicateBoard = ref({});
const editMode = ref(props.isEdit);
const validate = ref({ name: {}, description: {} });
const allBoard = ref([]);

console.log( props.board);

watch(
  () => props.board,
  (newBoard) => {
    allBoard.value = boardStore.boards;
    for (const key in newBoard) {
      if (newBoard[key] === null) {
        newBoard[key] = "";
      }
    }
    duplicateBoard.value = { ...newBoard };
  },
  { immediate: true }
);

const isStatusChanged = computed(() => {
  // FALSE คือเปลี่ยน , true ไม่เปลี่ยน
  return JSON.stringify(props.board) === JSON.stringify(duplicateBoard.value);
});

const countBoardName = computed(() => {
  return duplicateBoard.value.name?.trim()?.length;
});

function textShow(text) {
  if (text === null) {
    return "italic text-gray-600";
  }
}
function edit(boardId) {
    console.log(boardId);
//   if (boardId !== null) {
//     editMode.value = !editMode.value;
//     router.push({ name: "EditStatus", params: { statusId: statusId  , boardId : "1"} });
//   }
}

const duplicateName = computed(() => {
  if (isStatusChanged.value === false) {
    return allBoard.value
      .filter((e) => e.name !== props.board.name)
      .some(
        (e) => e.name.toLowerCase() === duplicateBoard.value.name.toLowerCase()
      );
  } else {
    return false;
  }
});

const disabledSave = computed(() => {
  const arrStyle = validateSizeInput(
    { propName: "Board name", propLenght: countBoardName.value, size: 120 }
  );

  validate.value.name = arrStyle[0];

  if (
    isStatusChanged.value ||
    duplicateBoard.value.name === null ||
    countBoardName.value <= 0
  ) {
    return true;
  } else if (validate.value.name.boolean ) {
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
        class="itbkk-modal-board flex flex-col justify-start rounded-md bg-white h-[55%] max-h-800px w-[50rem] shadow-md relative overflow-auto"
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
                <span>Board Name</span>
              </div>
              <!-- <div
                v-show="board.id !== 1"
                @click="edit(board.id)"
                class="cursor-pointer ml-1"
              >
                <EditTaskIcon class="w-[1rem] h-[1rem]" />
              </div> -->
            </div>
            <textarea
              class="itbkk-status-name read-only:focus:outline-none placeholder:text-gray-500 placeholder:italic break-all mt-2 p-2 rounded-lg border border-gray-800 h-full resize-none"
              :class="
                (editMode
                  ? countBoardName <= 0
                    ? 'border border-red-400 rounded-md p-1 focus:outline-red-500'
                    : validate.name.style
                  : 'border-b',
                textShow(duplicateBoard.name))
              "
              :readonly="!editMode"
              type="text"
              name="statusName"
              id="statusName"
              v-model="duplicateBoard.name"
              placeholder="No Name Provided"
            >
                {{
                duplicateBoard.name === null ||
                duplicateBoard.name?.length === 0
                  ? "No Name Provided"
                  : duplicateBoard.name
              }}
              </textarea
            >
            <div
              class="flex flex-col justify-end items-end w-[100%]"
              v-if="editMode"
            >
              <span class="text-xs border-0" :class="validate.name.style"
                >{{ countBoardName }}/120</span
              >

              <div>
                <span class="text-xs text-red-500" v-if="validate.name.boolean">
                  {{ validate.name.msg }}</span
                >
                <span class="text-xs text-red-500" v-if="duplicateName">
                  Board name must be uniques, please choose another name.
                </span>
              </div>
            </div>
          </div>
          <!-- Description -->
          <div class="flex flex-col">
            <div class="font-bold text-lg">Description</div>
            <textarea
              class="itbkk-status-description read-only:focus:outline-none placeholder:text-gray-500 placeholder:italic break-all mt-2 p-2 rounded-lg border border-gray-800 h-24 resize-none"
              
              :readonly="!editMode"
              type="text"
              name="description"
              id="description"
              v-model="duplicateBoard.description"
              placeholder="No Description Provided"
            >
                {{
                duplicateBoard.description === null ||
                duplicateBoard.description?.length === 0
                  ? "No Description Provided"
                  : duplicateBoard.description
              }}
                </textarea
            >
        
          </div>
          <!-- <div class="flex flex-col">
            <div class="font-bold text-sm mt-2">Color</div>
            <div class="mt-2">
              <input
                type="color"
                v-model="duplicateBoard.color"
                :disabled="!editMode"
              />
            </div>
          </div> -->
        </div>

        <!-- Save Buttons, Close Buttons -->
        <div
          class="w-full flex justify-end mb-5"
          :class="editMode ? 'block ' : 'hidden'"
        >
          <div class="flex mr-10 gap-2">
            <button
              :disabled="disabledSave || duplicateName"
              class="itbkk-button-ok text-white inline-flex items-center focus:ring-4 focus:outline-none font-medium rounded-lg text-sm px-5 py-2.5 text-center bg-gray-300"
              :class="
                disabledSave || duplicateName
                  ? 'cursor-not-allowed disabled'
                  : 'cursor-pointer bg-green-700 hover:bg-green-800 focus:ring-green-300'
              "
              @click="
                $emit('userAction', false), $emit('addEdit', duplicateBoard)
              "
            >
             {{ !board.id? "CREATE" : "SAVE"   }} 
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
