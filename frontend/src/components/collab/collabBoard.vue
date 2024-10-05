<script setup>
import TaskTableLoading from "../loading/TaskTableLoading.vue";
import { useStatusStore } from "../../stores/statuses.js";
import { useUserStore } from "../../stores/user.js";
import { useRoute, useRouter } from "vue-router";
import collabDetail from "./collabDetail.vue"
import { ref, watch } from "vue";
import EditIcon from "../icon/EditIcon.vue";
import DeleteIcon from "../icon/DeleteIcon.vue";
import ConfirmModal from "../modal/ConfirmModal.vue"
import CloseIcon from "../icon/CloseIcon.vue"
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

let allStatus = ref([{ id: "0", name: "1", description: "2" }]);
let accessRight = ref(["Read" , "Write"]);
const accessSelect = ref("Read")
const statusStore = useStatusStore();
const userStore = useUserStore();
const router = useRouter();

const isVisible = ref([]);

const isShowAddCollab = ref(false);
const showAccessModal= ref(false);
const isChangeAccess= ref(false);
let username ="เอาใส่ด้วย"
let usernameId = ref(50)
console.log(usernameId.value);
// watch(
//   () => props.allStatus,
//   () => {
//     props.allStatus.forEach((task, index) => {
//       setTimeout(() => {
//         isVisible.value[index] = true;
//       }, (index + 1) * 150);
//     });
//   },
//   { deep: true }
// );

// function editStatus(id, name) {
//   console.log(userStore.isCanEdit);
//   if (userStore.isCanEdit) {
//     // ***
//     console.log("e");
//     console.log(name);
//     console.log(name !== "No Status" && name !== "Done");
//     if (name !== "No Status" && name !== "Done") {
//       console.log("ewd");
//       // editMode.value = !editMode.value;
//       console.log(id);
//       router.push({ name: "EditStatus", params: { statusId: id } });
//     }
//   }
// }
watch(
  () => accessSelect.value,
  (newSelect, oldSelect) => {
    console.log("wdw",newSelect);
    console.log("wdw2",oldSelect);
   if (newSelect !== oldSelect) {
    showAccessModal.value= true
    isChangeAccess.value=true
   } else {
       showAccessModal.value= false
   }
  //  console.log("wdw2efe",accessSelect.value);
   // newSelect === oldSelect
  }
  // ,
  // { immediate: true }
)

function close(e) {
  isShowAddCollab.value=e
}
function confirmChange(e) {
  if (isChangeAccess.value) {
    //แก้ไข access right ของ user
     if (e) {
    console.log("เปลี่ยน access ได้");
    showAccessModal.value=false

  } else {
    console.log("ไม่เปลี่ยน access เพราะกด cancle");
    showAccessModal.value=false
    console.log(accessSelect.value);
    //ถ้ากด cancle ให้ทำให้  accessSelect เป็นค่าเดิมของ user นั้นๆ 
    accessSelect.value = "Read"
    console.log(accessSelect.value);

 
  }
  } else {
    //ลบ user ออกจาก board
    console.log(usernameId.value);
    if (e) {
    console.log("user ออกจาก board");
  } else {
    console.log("ไม่user ออกจาก board");
    showAccessModal.value=false
  }
  }
 

}

</script>

<template>
  <div class="flex flex-col w-full h-screen">
    <!-- <div class="h-[8%]">
        <HeaderView class="h-full" />
      </div> -->
    <!-- Task Status and Add Task Button -->
    <div class="flex flex-col items-center h-full gap-5 mt-2">
      <div class="flex flex-row w-[95%] mt-5 max-sm:w-full max-sm:px-2">
        <!-- Task Status Count -->
        <div class="m-[2px] flex sm:items-center items-end w-full">
          <router-link :to="{ name: 'task' }">
            <div class="itbkk-board-name text-gray-600 text-[1.5rem] font-bold">
              xxxx Personal's Board
            </div>
          </router-link>

          <button
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
          </button>
          <div class="text-gray-900 text-[1.5rem] font-bold">Collaborater</div>
        </div>
        <!-- <div class="m-[2px] flex sm:items-center items-end">
          <router-link :to="{ name: 'task' }">
            <div
              class="itbkk-button-home text-gray-800 text-[1rem] hover:underline hover:decoration-1"
            >
              Home
            </div>
          </router-link>
          <div class="mx-2 text-slate-500">/</div>

          <div class="text-gray-800 text-[1rem] font-bold">ManageStatus</div>
        </div> -->

        <!-- Filter -->
        <div class="flex items-end w-full justify-end">
          <div
            class="flex sm:flex-row flex-col sm:items-center items-end gap-1 sm:gap-4"
          >

                <div
                  :class="
                    userStore.isCanEdit
                      ? ''
                      : 'tooltip tooltip-bottom tooltip-hover'
                  "
                  data-tip="You need to be board owner to perform this action."
                >
                  <button
                    class="itbkk-button-add bg-gray-800 hover:bg-gray-500 text-white font-bold py-2 px-4 rounded-lg text-[0.9rem] max-sm:text-[0.89rem]"
                    :disabled="!userStore.isCanEdit"
                    :class="
                      userStore.isCanEdit
                        ? 'cursor-pointer'
                        : 'cursor-not-allowed disabled'
                    "
                    @click="isShowAddCollab=true"
                  >
                    Add Collaborater
                  </button>
                </div>
          </div>
        </div>
      </div>

      <div
        class="flex justify-center mt-4 gap-3 w-[95%] max-sm:w-full max-sm:px-2 max-sm:gap-1"
      >
        <!-- Status Table -->
        <TaskTableLoading v-if="showLoading" class="w-full" />

        <div v-else class="w-full rounded-md shadow-xl">
          <div
            class="flex justify-center mt-4 gap-3 w-[100%] max-sm:w-full max-sm:px-2 max-sm:gap-1"
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
                  v-for="(status, index) in allStatus"
                  :key="index"
                  :class="{ 'slide-in': isVisible[index] }"
                >
                  <td class="px-6 py-4 max-lg:hidden w-[5%]">
                    <!-- {{ index + 1 }} -->
                    1
                  </td>

                  <td
                    class="h-full w-[40%] px-6 py-4 max-lg:w-[30%] max-lg:px-2 max-lg:py-3 hover:bg-neutral-100"
                  >
                    <div
                      class="cursor-pointer h-full w-full items-center flex break-all"
                    >
                      <span class="itbkk-status-name"> {{ status.name }} </span>
                    </div>
                  </td>

                  <td
                    class="w-[30%] px-6 py-4 max-lg:[45%] max-lg:px-2 max-lg:py-3 break-all"
                    :class="
                      status.description === null ||
                      status.description?.length === 0
                        ? 'italic text-gray-600'
                        : ''
                    "
                  >
                    <span class="itbkk-email">
                      {{
                        status.description === null ||
                        status.description?.length === 0
                          ? "No description is provided."
                          : status.description
                      }}
                    </span>
                  </td>
                  <td
                    class="w-[20%] px-6 py-4 max-lg:[45%] max-lg:px-2 max-lg:py-3 break-all"
                  >
                    <div class="itbkk-acess-right">
                      <select
                        class="itbkk-status border-2 border-gray-500 w-[10rem] h-[30px] rounded-lg"
                        v-model="accessSelect"
                      >
                        <option
                          v-for="access in accessRight"
                        >
                          {{ access }}
                        </option>
                      </select>
                    </div>
                  </td>
                  <td
                    class="w-[20%] px-4 py-4 max-lg:w-[25%] max-lg:px-2 max-lg:py-3 cursor-pointer flex justify-center items-center"
                  >
                    <div class="flex flex-row gap-4 max-sm:flex-col">
                      <div
                        :class="
                          userStore.isCanEdit
                            ? ''
                            : 'tooltip tooltip-top tooltip-hover'
                        "
                        data-tip="You need to be board owner to perform this action."
                      >
                        <div
                          class="itbkk-collab-remove text-white fill-rose-300"
                          @click="isChangeAccess=false , usernameId = index , showAccessModal= true"
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
        </div>
      </div>

      <collabDetail 
      v-if="isShowAddCollab"
      @user-action="close"
      />

      <ConfirmModal
      v-if="showAccessModal"
      @user-action="confirmChange"
      :width="'w-[60vh]'"
      class="itbkk-modal-alert z-50"
    >
      <template #header>
        <div class="flex flex-col justify-items-end	place-items-end cursor-pointer" @click="showSettingModal=false">
             <CloseIcon @click=" showAccessModal= false" />
            </div>
        <div class="flex justify-center">   
          <span class="text-gray-800 font-bold text-[1.5rem]">
           {{ isChangeAccess ? 'Change Access Right' : 'Remove Collaborater' }} 
          </span>
        </div>
      </template>
      <template #body>
        <span class="itbkk-message">
          {{ isChangeAccess ? `Do you want to change access right of "${ username }" to "${ accessSelect }"` : `Do you want to remove "${  username }" from the board?` }}  
        </span>
      </template>
    </ConfirmModal>

    </div>
  </div>
</template>

<style scoped></style>
